/********************************************************************************
* MultimediaLocator v0.9.0
* @author davidgordon
* 
* Software for displaying large multimedia collections as navigable virtual
* environments based on spatial, temporal and orientation metadata. 
*********************************************************************************/

/************************************
* MultimediaLocator application class
* @author davidgordon 
*/

package multimediaLocator;
import java.io.*;
import java.util.ArrayList;

import g4p_controls.GButton;
import g4p_controls.GEvent;
import g4p_controls.GToggleControl;
import g4p_controls.GValueControl;
import g4p_controls.GWinData;
import processing.core.*;
import processing.video.Movie;

/**
 * MultimediaLocator App class 
 * @author davidgordon
 */
public class MultimediaLocator extends PApplet 
{
	/* General */
	String programName = "MultimediaLocator 0.9.0";
	
	/* System Status */
	public ML_SystemState state = new ML_SystemState();
	boolean createNewLibrary = false;
	boolean enteredField = false;

	/* MultimediaLocator */
	ML_Library library;							// Multimedia library
	ML_Input input;								// Mouse / keyboard input
	ML_Stitcher stitcher;						// Panoramic stitching
	ML_Display display;							// Displaying 2D graphics and text
	ML_DebugSettings debugSettings;				// Debug settings
	
	/* WorldMediaViewer */
	WMV_World world;							// The 3D World
	WMV_MetadataLoader metadata;				// Metadata reading and writing
	
	/* Memory */
	public boolean lowMemory = false;
	public boolean performanceSlow = false;
	public long freeMemory;
	public long maxMemory;
	public long totalMemory;
	public long allocatedMemory;
	public long approxUsableFreeMemory;
	public int availableProcessors;

	/** 
	 * Setup function called at launch
	 */
	public void setup()
	{
		debugSettings = new ML_DebugSettings();		
		if(debugSettings.main) System.out.println("Starting initial setup...");

		world = new WMV_World(this);
		world.initialize();
		
		input = new ML_Input(width, height);
		display = new ML_Display(width, height, world.getState().hudDistance);			// Initialize displays
		metadata = new WMV_MetadataLoader(this, debugSettings);
		stitcher = new ML_Stitcher(world);
		if(debugSettings.main) System.out.println("Initial setup complete...");

		colorMode(PConstants.HSB);
		rectMode(PConstants.CENTER);
		textAlign(PConstants.CENTER, PConstants.CENTER);
	}

	/** 
	 * Main program loop
	 */
	public void draw() 
	{		
		if (state.startup)
		{
			if(state.reset) restartMultimediaLocator();
			else display.display(world);						/* Startup screen */
			
			state.startup = false;	
		}
		else if(!state.running)
		{
			if (state.chooseLibrary || state.chooseLibraryDestination )
			{
				if(createNewLibrary)
				{
					if(state.chooseLibrary)					/* Choose media folder */
						mediaFolderDialog();	
					if(state.chooseLibraryDestination)			/* Choose library destination */
						libraryDestinationDialog();	
					display.display(world);
				}
				else
					librarySelectionDialog();
			}
			else
			{
				if(state.selectedLibraryDestination && !state.selectedLibrary)
				{
					if(state.selectedMediaFolder)
						importMediaFolder();
					else
						System.out.println("ERROR: Selected library destination but no media folder selected!");
				}
				
				display.display(world);
				if(state.selectedLibrary) initialize();			/* Initialize world */
			}
		}
		else run();													/* Run MultimediaLocator */
	}
	
	/**
	 * Run program each frame
	 */
	void run()
	{
		if(state.startedRunning)										/* If simulation just started running */
		{
			if(!enteredField) world.enter(0, true);						/* Enter world at field 0 */
			state.startedRunning = false;
		}
		else
		{
			if ( !state.initialSetup && !state.interactive && !state.exit ) 	/* Running the program */
			{
				world.run();
//	 			input.updateLeapMotion();			// Update Leap Motion 
			}

			if(state.export && world.outputFolderSelected)						/* Image exporting */
				export();

			if ( debugSettings.memory && frameCount % world.getState().memoryCheckFrequency == 0 )		/* Memory debugging */
			{
				checkMemory();
				checkFrameRate();
			}
		}
		
		if ( state.exit ) exitProgram();							/* Stopping the program */		
	}
	
	/**
	 * Initialize world and run clustering
	 */
	public void initialize()
	{
		if(state.initialSetup)
		{
			if( !state.fieldsInitialized )
			{
				if (!state.initializingFields) 
				{
					world.createFieldsFromFolders(library.getFolders());		// Create empty field for each media folder	
					state.initializingFields = true;
				}
				else initializeField(state.initializationField);					
			}
			else finishInitialization();
		}
		else
		{
			if(state.interactive)					/* Run interactive clustering */
			{
				if(state.startInteractive && !state.interactive) startInteractiveClustering();						
				if(state.interactive && !state.startInteractive) runInteractiveClustering();	
			}
			else startInitialClustering();			/* Run initial clustering */  	// -- Sets initialSetup to true	
		}
	}
	
	/**
	 * Start initial clustering process
	 */
	public void startInitialClustering()
	{
		display.startupMessages = new ArrayList<String>();	// Clear startup messages
		if(debugSettings.metadata)
		{
			display.sendSetupMessage(world, "Library folder: "+library.getLibraryFolder());	// Show library folder name
			display.sendSetupMessage(world, " ");
		}
		display.display(world);											

		state.running = false;			// Stop running
		state.initialSetup = true;				// Start clustering mode
	}

	/**
	 * Start interactive clustering mode
	 */
	public void startInteractiveClustering()
	{
		background(0.f);						// Clear screen
		
		state.running = false;					// Stop running simulation
		state.interactive = true;				// Start interactive clustering mode
		state.startInteractive = false;			// Have started
		
		display.map2D.initializeMaps(world);
		
		display.resetDisplayModes();			// Reset display view and clear messages
		display.displayClusteringInfo(this);
		
		world.getCurrentField().blackoutAllMedia();	// Blackout all media
	}
	
	/**
	 * Initialize current initialization field
	 */
	public void initializeField(int fieldID)
	{
		if(!state.fieldsInitialized && !state.exit)
		{
			/* Get field to initialize */
			WMV_Field f = world.getField(fieldID);	

			/* Attempt to load simulation state from data folder. If not successful, initialize field */
			WMV_Field loadedField;
			if(fieldID + 1 >= world.getFields().size())
				loadedField = loadSimulationState(f, library.getLibraryFolder(), true);		// Load metadata, attempt to load simulation state, set simulation state from field 
			else
				loadedField = loadSimulationState(f, library.getLibraryFolder(), false);	// Load metadata and attempt to load simulation state
			
			if(world.getFields().size() == 0)
				if(world.viewer.getState().field > 0)
					world.viewer.setCurrentField(0, false);
			
			boolean success = (loadedField != null);
			if(success) world.setField(loadedField, fieldID);
			if(success) success = world.getField(fieldID).getClusters() != null;
			if(success) success = (world.getField(fieldID).getClusters().size() > 0);
			if(success)
			{
				if(debugSettings.main || debugSettings.field)
					System.out.println("Succeeded at loading simulation state for Field #"+f.getID()+"... clusters:"+world.getField(fieldID).getClusters().size());
			}
			else
			{
				if(debugSettings.main || debugSettings.field)
					System.out.println("Failed at loading simulation state... Initializing field #"+f.getID());
				
				world.state.hierarchical = f.initialize(-100000L);
			}

			/* Set next field to initialize */
			state.initializationField++;										
			if( state.initializationField >= world.getFields().size() )	
			{
				state.fieldsInitialized = true;
				if(debugSettings.main) System.out.println("" + world.getFields().size() + " fields initialized...");
				world.enter(state.initializationField-1, true);			// Enter world at last initialization field
			}
		}
	}
	
	/**
	 * Load simulation state from disk
	 * @param f The field to initialize
	 * @param libraryFolder Library folder
	 * @return True if succeeded, false if failed
	 */
	private WMV_Field loadSimulationState(WMV_Field f, String libraryFolder, boolean set)
	{
		/* Load metadata from media associated with field */
		WMV_SimulationState savedState = metadata.load(f, libraryFolder);
		
		/* Attempt to load simulation state */
		if(savedState != null)
		{
			if(debugSettings.main && debugSettings.detailed) System.out.println("Valid SimulationState loaded...");
			if(set)
				return world.loadAndSetSimulationState(savedState, f);
			else
				return world.loadSimulationState(savedState, f);
		}
		else return null;
	}
	
	/**
	 * Finish the world initialization process
	 */
	void finishInitialization()
	{
		world.setBlurMasks();			// Set blur masks

		if(debugSettings.main && debugSettings.detailed) System.out.println("Finishing MultimediaLocator initialization..");

		display.initializeWindows(world);
		display.window.setupWMVWindow();
		
		if(debugSettings.main && debugSettings.detailed) System.out.println("Finished setting up WMV Window...");
		
		world.updateAllMediaSettings();					// -- Only needed if field(s) loaded from data folder!

		if(debugSettings.main && debugSettings.detailed) System.out.println("Finished setting initial media settings...");

		state.initialSetup = false;				
		display.initialSetup = false;
		
		state.running = true;
		state.startedRunning = true;
	}


	/**
	 * Run interactive clustering 
	 */
	public void runInteractiveClustering()
	{
		background(0.f);					// Clear screen
		display.display(world);						// Draw text		
	}
	
	/**
	 * Finish running interactive clustering and restart simulation 
	 */
	public void finishInteractiveClustering()
	{
		background(0.f);
		
		world.viewer.clearAttractorCluster();

		state.interactive = false;				// Stop interactive clustering mode
		state.startedRunning = true;				// Start GMViewer running
		state.running = true;	
		
		world.viewer.setCurrentCluster( world.viewer.getNearestCluster(false), -1 );
		world.getCurrentField().blackoutAllMedia();
	}
	
	/**
	 * Initialize 2D drawing 
	 */
	public void start3DHUD()
	{
		PVector camLoc = world.viewer.getLocation();
		PVector camOrientation = world.viewer.getOrientation();
		perspective(world.viewer.getInitFieldOfView(), (float)width/(float)height, world.viewer.getNearClippingDistance(), 10000);
		PVector t = new PVector(camLoc.x, camLoc.y, camLoc.z);
		translate(t.x, t.y, t.z);
		rotateY(camOrientation.x);
		rotateX(-camOrientation.y);
		rotateZ(camOrientation.z);
	}

	public void restartMultimediaLocator()
	{
		background(0.f);
		display.window.hideWindows();
		world.reset(true);
	}
	
	/**
	 * Save screen image or export selected media
	 */
	public void export()
	{
		if(world.viewer.getSettings().selection)
		{
			world.exportSelectedMedia();
			System.out.println("Exported image(s) to "+world.outputFolder);
		}
		else
		{
			saveFrame(world.outputFolder + "/" + world.getCurrentField().getName() + "-######.jpg");
			System.out.println("Saved screen image: "+world.outputFolder + "/image" + "-######.jpg");
		}
		state.export = false;
	}

	/**
	 * Open library folder when folder has been selected
	 * @param selection File object for selected folder
	 */
	public void libraryFolderSelected(File selection) 
	{
		openLibraryFolder(selection);
	}

	/**
	 * Open library destination folder when folder has been selected
	 * @param selection File object for selected folder
	 */
	public void libraryDestinationSelected(File selection) 
	{
		openLibraryDestination(selection);
	}

	/**
	 * Open media folder when folder has been selected
	 * @param selection File object for selected folder
	 */
	public void mediaFolderSelected(File selection) 
	{
		openMediaFolder(selection);
	}

	/**
	 * Stop the program
	 */
	public void exitProgram() 
	{
		System.out.println("Exiting "+programName+"...");
		exit();
	}

	/**
	 * Restart the program
	 */
	public void restart()
	{
		state.reset();
		world.viewer.initialize(0,0,0);
	}
	
	/**
	 * Analyze and load media folders given user selection
	 * @param selection Selected folder
	 */
	public void openLibraryDestination(File selection) 
	{
		boolean selectedFolder = false;
		
		if (selection == null) {
			if (debugSettings.main)
				System.out.println("openLibraryDestination()... Window was closed or the user hit cancel.");
		} 
		else 
		{
			String input = selection.getPath();
			String[] parts = input.split("/");

			if (debugSettings.main)
				System.out.println("User selected library destination: " + input);

			File file = new File(input);
			if(file.exists())
			{
				if(file.isDirectory())
				{
					selectedFolder = true;
					library.libraryDestination = parts[parts.length-1];
					
					String libFilePath = "";
					for(int i=0; i<parts.length-1; i++)
					{
						libFilePath = libFilePath + parts[i] + "/";
					}

					library.setLibraryFolder(libFilePath);
					library.addFolder(library.libraryDestination);
				}
			}
		}
		
		if(selectedFolder)
			state.selectedLibraryDestination = true;	// Library destination folder has been selected
		else
		{
			state.selectedLibraryDestination = false;		// Not a folder or folder doesn't exist
			libraryDestinationDialog();					// Retry prompt
		}
	}

	/**
	 * Open media folder
	 * @param selection Selected folder
	 */
	public void openMediaFolder(File selection) 
	{
		boolean selectedFolder = false;
		
		if (selection == null) 
		{
			System.out.println("openMediaFolder()... Window was closed or the user hit cancel.");
		} 
		else 
		{
			String input = selection.getPath();
			String[] parts = input.split("/");

//			if (debugSettings.metadata)
				System.out.println("User selected media folder: " + input);

			File file = new File(input);
			if(file.exists())
			{
				if(file.isDirectory())
				{
					library = new ML_Library("");
//					library.mediaFolder = parts[parts.length-1];
					library.mediaFolder = input;
					selectedFolder = true;
				}
			}
		}
		
		if(selectedFolder)
		{
			state.selectedMediaFolder = true;			// Media folder has been selected
			state.chooseLibraryDestination = true;		// Choose library destination folder
		}
		else
		{
			state.selectedMediaFolder = false;				// Library in improper format if masks are missing
			mediaFolderDialog();					// Retry folder prompt
		}
	}
	
	/**
	 * Analyze and load media folders given user selection
	 * @param selection Selected folder
	 */
	public void openLibraryFolder(File selection) 
	{
		boolean selectedFolder = false;
		
		if (selection == null) {
			System.out.println("Window was closed or the user hit cancel.");
		} 
		else 
		{
			String input = selection.getPath();

			if (debugSettings.metadata)
				System.out.println("User selected library folder: " + input);

			library = new ML_Library(input);

			String[] parts = input.split("/");
			
			String[] nameParts = parts[parts.length-1].split("_");		// Check if single field library 
			boolean singleField = !(nameParts[0].equals("ML") && nameParts[1].equals("Library"));
			String parentFilePath = "";
			
			if(singleField)
			{
				System.out.println("Loading single field folder...");
				String libFilePath = "";
				for(int i=0; i<parts.length-1; i++)
				{
					libFilePath = libFilePath + parts[i] + "/";
				}

				library = new ML_Library(libFilePath);				// Set library folder
				library.addFolder(parts[parts.length-1]);			// Add single folder 
				
				selectedFolder = true;

				for(int i=0; i<parts.length-2; i++)
					parentFilePath = parentFilePath + parts[i] + "/";
			}
			else
			{
				System.out.println("Loading media library...");
				File libFile = new File(library.getLibraryFolder());
				
				String[] mediaFolderList = libFile.list();
				for(String mediaFolder : mediaFolderList)
					if(!mediaFolder.equals(".DS_Store"))
						library.addFolder(mediaFolder);

				selectedFolder = true;

				for(int i=0; i<parts.length-1; i++)
					parentFilePath = parentFilePath + parts[i] + "/";
			}

			world.getState().stitchingPath = parentFilePath + "stitched/";
			world.loadImageMasks();					
			world.loadVideoMasks();
			
			selectedFolder = true;
		}
		
		if(selectedFolder)
			state.selectedLibrary = true;	// Library folder has been selected
		else
		{
			state.selectedLibrary = false;				// Library in improper format if masks are missing
			librarySelectionDialog();					// Retry folder prompt
		}
	}

	/**
	 * Import media folder into library
	 */
	private void importMediaFolder()
	{
		File mediaFolderFile = new File(library.mediaFolder);
		File[] fileList = mediaFolderFile.listFiles();
		
		boolean hasDirectory = false;
		if(fileList != null)
		{
			if(debugSettings.main) System.out.println("fileList.length:"+fileList.length);
			for(File f : fileList)
			{
				if(f.isDirectory())
				{
					hasDirectory = true;
					break;
				}
			}
		}
		else System.out.println("fileList == null... library.mediaFolder:"+library.mediaFolder);
		
		if(hasDirectory)
		{
			System.out.println("Error: Haven't built multiple directory import yet! Will exit...");
			exit();
		}
		else
		{
			ArrayList<String> folderList = new ArrayList<String>();
			System.out.println("Will create new library at: "+library.getLibraryFolder()+library.libraryDestination);
			state.selectedLibrary = library.createNewLibrary(library.mediaFolder, folderList, library.libraryDestination);

			if(!state.selectedLibrary)
			{
				System.out.println("Error creating library...");
				exit();
			}
		}
	}
	
	/**
	 * Called when image output folder has been selected
	 * @param selection
	 */
	public void outputFolderSelected(File selection) 
	{
		if (selection == null) 
		{
			if (debugSettings.main)
				println("Window was closed or the user hit cancel.");
		} 
		else 
		{
			String input = selection.getPath();

			if (debugSettings.main)
				println("----> User selected output folder: " + input);

			world.outputFolder = input;
			world.outputFolderSelected = true;
		}
	}
	
	/**
	 * Called whenever a new frame is available to read
	 * @param m Movie the event pertains to
	 */
	public void movieEvent(Movie m) 	
	{
		try{
			if(m != null)				// Testing skipping 30th frame to avoid NullPointerException
				if(m.available())		// If a frame is available,
					m.read();			// read from disk
		}
		catch(NullPointerException npe)
		{
			if(debugSettings.video)
				println("movieEvent() NullPointerException:"+npe);
		}
		catch(Throwable t)
		{
			if(debugSettings.video)
				println("movieEvent() Throwable:"+t);
		}
	}
	
	/**
	 * Respond to mouse pressed event
	 */
	public void mousePressed()
	{
//		if(world.viewer.mouseNavigation)
//			input.handleMousePressed(mouseX, mouseY);
		display.map2D.mousePressedFrame = frameCount;
	}

	/**
	 * Respond to mouse released event
	 */
	public void mouseReleased() {
//		if(world.viewer.mouseNavigation)
//			input.handleMouseReleased(mouseX, mouseY);
		if(display.displayView == 1 || (display.displayView == 2 && display.libraryViewMode == 0))
			input.handleMouseReleased(world, display, mouseX, mouseY, frameCount);
		else if(display.displayView == 3)
			input.handleMouseReleased(world, display, mouseX, mouseY, frameCount);
	}
	
	/**
	 * Respond to mouse clicked event
	 */
	public void mouseClicked() {
//		if(world.viewer.mouseNavigation)
//			input.handleMouseClicked(mouseX, mouseY);
	}
	
	/**
	 * Respond to mouse moved event
	 */
	public void mouseMoved()
	{
//		if(display.displayView == 1)
//			if(display.satelliteMap)
//				display.map2D.handleMouseMoved(mouseX, mouseY);
	}

	/**
	 * Respond to mouse dragged event
	 */
	public void mouseDragged() {
//		if(display.satelliteMap)
//		{
		display.map2D.mouseDraggedFrame = frameCount;
//		}
//		System.out.println("dragged");
//		if(world.mouseNavigation)
//		{
//			if(display.inDisplayView())
//			{
//				System.out.println("pmouseX:"+pmouseX+" pmouseY:"+pmouseY);
//				System.out.println("mouseX:"+mouseX+" mouseY:"+mouseY);
//				input.handleMouseDragged(pmouseX, pmouseY);
//			}
//		}
	}

	/**
	 * Respond to button event
	 * @param button Button acted on
	 * @param event Button event
	 */
	public void handleButtonEvent(GButton button, GEvent event) { 
		input.handleButtonEvent(this, display, button, event);
	}
	
	/**
	 * Respond to toggle control event
	 * @param button Toggle control acted on
	 * @param event Toggle control event
	 */
	public void handleToggleControlEvent(GToggleControl option, GEvent event) {
		input.handleToggleControlEvent(world, display, option, event);
	}
	
	/**
	 * Respond to slider event
	 * @param button Slider acted on
	 * @param event Slider event
	 */
	public void handleSliderEvent(GValueControl slider, GEvent event) 
	{ 
		input.handleSliderEvent(world, display, slider, event);
	}

	/**
	 * Processing method called when a key is pressed
	 */
	public void keyPressed() 
	{
		input.handleKeyPressed(this, key, keyCode);
	}

	/**
	 * Processing method called when a key is released
	 */
	public void keyReleased() 
	{
		input.handleKeyReleased(world.viewer, display, key, keyCode);
	}
	
	/**
	 * Respond to key pressed in MultimediaLocator Window
	 * @param applet Parent App
	 * @param windata Window data
	 * @param keyevent Key event
	 */
	public void mlWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			input.handleKeyPressed(this, keyevent.getKey(), keyevent.getKeyCode());
		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			input.handleKeyReleased(world.viewer, display, keyevent.getKey(), keyevent.getKeyCode());
	}

	/**
	 * Respond to key pressed in Navigation Window
	 * @param applet Parent App
	 * @param windata Window data
	 * @param keyevent Key event
	 */
	public void navigationWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			input.handleKeyPressed(this, keyevent.getKey(), keyevent.getKeyCode());
		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			input.handleKeyReleased(world.viewer, display, keyevent.getKey(), keyevent.getKeyCode());
	}
	
	/**
	 * Respond to key pressed in Time Window
	 * @param applet Parent App
	 * @param windata Window data
	 * @param keyevent Key event
	 */
	public void timeWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			input.handleKeyPressed(this, keyevent.getKey(), keyevent.getKeyCode());
		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			input.handleKeyReleased(world.viewer, display, keyevent.getKey(), keyevent.getKeyCode());
	}

	/**
	 * Respond to key pressed in Graphics Window
	 * @param applet Parent App
	 * @param windata Window data
	 * @param keyevent Key event
	 */
	public void graphicsWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			input.handleKeyPressed(this, keyevent.getKey(), keyevent.getKeyCode());
		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			input.handleKeyReleased(world.viewer, display, keyevent.getKey(), keyevent.getKeyCode());
	}
	
	/**
	 * Respond to key pressed in Memory Window
	 * @param applet Parent App
	 * @param windata Window data
	 * @param keyevent Key event
	 */
	public void memoryWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			input.handleKeyPressed(this, keyevent.getKey(), keyevent.getKeyCode());
		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			input.handleKeyReleased(world.viewer, display, keyevent.getKey(), keyevent.getKeyCode());
	}
	
	/**
	 * Respond to key pressed in Model Window
	 * @param applet Parent App
	 * @param windata Window data
	 * @param keyevent Key event
	 */
	public void modelWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			input.handleKeyPressed(this, keyevent.getKey(), keyevent.getKeyCode());
		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			input.handleKeyReleased(world.viewer, display, keyevent.getKey(), keyevent.getKeyCode());
	}
	
	/**
	 * Respond to key pressed in Selection Window
	 * @param applet Parent App
	 * @param windata Window data
	 * @param keyevent Key event
	 */
	public void selectionWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			input.handleKeyPressed(this, keyevent.getKey(), keyevent.getKeyCode());
		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			input.handleKeyReleased(world.viewer, display, keyevent.getKey(), keyevent.getKeyCode());
	}
	
	/**
	 * Respond to key pressed in Statistics Window
	 * @param applet Parent App
	 * @param windata Window data
	 * @param keyevent Key event
	 */
	public void statisticsWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			input.handleKeyPressed(this, keyevent.getKey(), keyevent.getKeyCode());
		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			input.handleKeyReleased(world.viewer, display, keyevent.getKey(), keyevent.getKeyCode());
	}
	
	/**
	 * Respond to key pressed in Help Window
	 * @param applet Parent App
	 * @param windata Window data
	 * @param keyevent Key event
	 */
	public void helpWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			input.handleKeyPressed(this, keyevent.getKey(), keyevent.getKeyCode());
		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			input.handleKeyReleased(world.viewer, display, keyevent.getKey(), keyevent.getKeyCode());
	}
	
	public void mediaFolderDialog()
	{
		state.chooseLibrary = false;
		selectFolder("Select media folder:", "mediaFolderSelected");		// Get filepath of PhotoSceneLibrary folder
	}
	
	public void libraryDestinationDialog()
	{
		state.chooseLibraryDestination = false;
		selectFolder("Select library destination:", "libraryDestinationSelected");		// Get filepath of PhotoSceneLibrary folder
	}
	
	public void librarySelectionDialog()
	{
		state.chooseLibrary = false;
		selectFolder("Select library folder:", "libraryFolderSelected");		// Get filepath of PhotoSceneLibrary folder
	}
	
	/**
	 * Called when user selects a GPS track file
	 * @param selection Selected GPS Track file
	 */
	public void gpsTrackSelected(File selection)
	{
		world.viewer.loadGPSTrack(selection);
	}
	
	public void checkMemory()
	{
		  availableProcessors = Runtime.getRuntime().availableProcessors();		/* Total number of processors or cores available to the JVM */
		  freeMemory = Runtime.getRuntime().freeMemory();		  /* Total amount of free memory available to the JVM */
		  maxMemory = Runtime.getRuntime().maxMemory();		  /* Maximum amount of memory the JVM will attempt to use */
		  totalMemory = Runtime.getRuntime().totalMemory();		  /* Total memory currently in use by the JVM */
		  allocatedMemory = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
		  approxUsableFreeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;

		  if(debugSettings.memory)
		  {
			  if(debugSettings.detailed)
			  {
				  System.out.println("Total memory (bytes): " + totalMemory);
				  System.out.println("Available processors (cores): "+availableProcessors);
				  System.out.println("Maximum memory (bytes): " +  (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory)); 
				  System.out.println("Total memory (bytes): " + totalMemory);
				  System.out.println("Allocated memory (bytes): " + allocatedMemory);
			  }
			  System.out.println("Free memory (bytes): "+freeMemory);
			  System.out.println("Approx. usable free memory (bytes): " + approxUsableFreeMemory);
		  }
		  
		  if(approxUsableFreeMemory < world.getState().minAvailableMemory && !lowMemory)
			  lowMemory = true;
		  if(approxUsableFreeMemory > world.getState().minAvailableMemory && lowMemory)
			  lowMemory = false;
		  
		  /* Possible memory tests: */
//		  MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
//		  MemoryUsage heap = memBean.getHeapMemoryUsage();
//		  MemoryUsage nonheap = memBean.getNonHeapMemoryUsage();
		  
		  /* Get a list of all filesystem roots on this system */
//		  File[] roots = File.listRoots();

		  /* For each filesystem root, print some info */
//		  for (File root : roots) {
//		    System.out.println("File system root: " + root.getAbsolutePath());
//		    System.out.println("Total space (bytes): " + root.getTotalSpace());
//		    System.out.println("Free space (bytes): " + root.getFreeSpace());
//		    System.out.println("Usable space (bytes): " + root.getUsableSpace());
//		  }
	}

	/**
	 * Check current frame rate
	 */
	public void checkFrameRate()
	{
		if(frameRate < world.getState().minFrameRate)
		{
			if(!performanceSlow)
				performanceSlow = true;
			
			if(performanceSlow && debugSettings.memory)
				display.message(this, "Performance slow...");
		}
		else
		{
			if(performanceSlow)
				performanceSlow = false;
		}
	}
	
	/**
	 * Set window resolution and graphics mode
	 */
	public void settings() 
	{
		size(1680, 960, processing.core.PConstants.P3D);		// MacBook Pro
//		size(1980, 1080, processing.core.PConstants.P3D);		// 
//		size(960, 540, processing.core.PConstants.P3D);			// Web Video Large
	}

	/** 
	 * Load the PApplet either in a window of specified size or in fullscreen
	 */
	static public void main(String[] args) 
	{
		PApplet.main("multimediaLocator.MultimediaLocator");						// Open PApplet in window
//		PApplet.main(new String[] { "--present", "wmViewer.MultimediaLocator" });	// Open PApplet in fullscreen mode
	}
	
	/* Obsolete */
//	public void setSurfaceSize(int newWidth, int newHeight)
//	{
//		surface.setResizable(true);
//		surface.setSize(newWidth, newHeight);
//		surface.setResizable(false);
//	}
//	
//	public void setSurfaceVisible(boolean newState)
//	{
//		surface.setVisible(newState);
//	}

//	public void setSurfaceLocation(int newX, int newY)
//	{
//		surface.setLocation(newX, newY);
//	}
}