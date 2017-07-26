/********************************************************************************
* MetaVisualizer v0.9.0
* @author davidgordon
* 
* A multimedia library management and visualization system using 
* spatial, temporal and orientation metadata to display and browse images,
* 360-degree panoramas, sounds, and videos as navigable 3D environments.
* 
* Built with the Processing Library
*********************************************************************************/

/************************************
* MetaVisualizer application class
* @author davidgordon 
*/
package main.java.com.entoptic.metaVisualizer;

import java.awt.AWTEvent;
import java.awt.FileDialog;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.event.AWTEventListener;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.awt.Frame;
import java.awt.Image;
import javax.imageio.ImageIO;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.net.MalformedURLException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import g4p_controls.GButton;
import g4p_controls.GEditableTextControl;
import g4p_controls.GEvent;
import g4p_controls.GToggleControl;
import g4p_controls.GValueControl;
import g4p_controls.GWinData;
import main.java.com.entoptic.metaVisualizer.gui.MV_Display;
import main.java.com.entoptic.metaVisualizer.gui.MV_Input;
import main.java.com.entoptic.metaVisualizer.gui.MV_Window;
import main.java.com.entoptic.metaVisualizer.media.WMV_Image;
import main.java.com.entoptic.metaVisualizer.media.WMV_Panorama;
import main.java.com.entoptic.metaVisualizer.media.WMV_Sound;
import main.java.com.entoptic.metaVisualizer.media.WMV_Video;
import main.java.com.entoptic.metaVisualizer.metadata.WMV_Metadata;
import main.java.com.entoptic.metaVisualizer.misc.MV_DebugSettings;
import main.java.com.entoptic.metaVisualizer.misc.MV_Stitcher;
import main.java.com.entoptic.metaVisualizer.misc.WMV_Utilities;
import main.java.com.entoptic.metaVisualizer.model.WMV_Model;
import main.java.com.entoptic.metaVisualizer.model.WMV_Waypoint;
import main.java.com.entoptic.metaVisualizer.system.MV_Library;
import main.java.com.entoptic.metaVisualizer.system.MV_SystemState;
import main.java.com.entoptic.metaVisualizer.world.WMV_Field;
import main.java.com.entoptic.metaVisualizer.world.WMV_World;
import processing.awt.PSurfaceAWT;
import processing.core.*;
import processing.opengl.PGL;
import processing.opengl.PShader;
import processing.video.Movie;

import com.apple.eawt.Application;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.newt.event.WindowUpdateEvent;
import com.jogamp.newt.opengl.GLWindow;

import ddf.minim.Minim;

/**
 * MetaVisualizer App  
 * @author davidgordon
 */
@SuppressWarnings("restriction")						// Allow setting app icon
public class MetaVisualizer extends PApplet 
{
	/* Deployment */
	private static boolean createJar = false;			// Determines how to load cubemap shader

	/* Classes */
	public MV_Library library;								// Multimedia library
	public MV_Input input;									// Mouse / keyboard input
	public MV_Stitcher stitcher;							// Panoramic stitching
	public MV_Display display;								// Displaying 2D graphics and text
	public MV_DebugSettings debug;							// Debug settings
	
	/* WorldMediaViewer */
	public WMV_World world;							// World simulation
	public WMV_Metadata metadata;					// Metadata reading and writing
	public WMV_Utilities utilities;

	/* App */
	private String appName = "MetaVisualizer 0.9.0";
//	private String appName = "MultimediaLocator 0.9.0";
	private PImage appIcon;							// App icon
	public boolean setAppIcon = true;						// Set App icon (after G4P changes it)
	private final int basicDelay = 60;	
	
	/* Main Window */
	private boolean mainWindowLostFocus = true;		// Flag for Main Window losing focus

	/* System Status */
	public MV_SystemState state = new MV_SystemState();
	public boolean createNewLibrary = false;
	public boolean cubeMapInitialized = false;

	/* Graphics */
	public PShader cubemapShader;
	public PShape domeSphere;
	public IntBuffer fbo;
	public IntBuffer rbo;
	public IntBuffer envMapTextureID;
	public PImage[] faces;
	public int cubeMapSize = 2048;   
	
	/* Sound */
	public Minim minim;

	/* Memory */
	public boolean lowMemory = false;
	public boolean performanceSlow = false;
	public long freeMemory;
	public long maxMemory;
	public long totalMemory;
	public long allocatedMemory;
	public long approxUsableFreeMemory;
	public int availableProcessors;

	/* Debugging */
	private static ArrayList<String> startupMessages = new ArrayList<String>();
	private ArrayList<String> systemMessages;
	
	/* Temp Directory */
	public static final String tempDir = System.getProperty("java.io.tmpdir")+"tmp"+System.nanoTime();		
	static {
	    File tempDirFile = new File(tempDir);
	    if(!tempDirFile.exists())
	    	tempDirFile.mkdir();

//	    if(createJar)
//	    		setupLibraries();
	}
	
	/** 
	 * Setup function called at launch
	 */
	public void setup()
	{
		/* Main Classes */
		utilities = new WMV_Utilities();

		delay(basicDelay);
		
		debug = new MV_DebugSettings();
		systemMessages = new ArrayList<String>();
		
		input = new MV_Input();
		world = new WMV_World(this);
		
		/* App Icon */
		appIcon = getImageResource("icon.png");
		
		/* HUD Display */
		display = new MV_Display(this);			
		display.window = new MV_Window(world, display);				// Setup and display interaction window

		if(debug.ml) systemMessage("Starting "+appName+" setup...");

		Toolkit.getDefaultToolkit().addAWTEventListener( new WMV_EventListener(), AWTEvent.FOCUS_EVENT_MASK );
		
//		Toolkit.getDefaultToolkit().addAWTEventListener( new WMV_EventListener(), AWTEvent.FOCUS_EVENT_MASK | 
//											AWTEvent.WINDOW_EVENT_MASK | AWTEvent.WINDOW_FOCUS_EVENT_MASK );

//		Toolkit.getDefaultToolkit().addAWTEventListener( new WMV_EventListener(), AWTEvent.KEY_EVENT_MASK );

		GLWindow glFrame = (GLWindow) surface.getNative();
		glFrame.addWindowListener( new WMV_WindowListener() );
		
//		glFrame.addWindowListener(new WindowListener() 
//		{
//	        public void windowGainedFocus(WindowEvent e) {
//	        			System.out.println("Main frame windowGainedFocus...");
//	        }
//
//	        public void windowLostFocus(WindowEvent e) {
//	        	System.out.println("Main frame windowLostFocus...");
//	        }
//
//	        public void windowDestroyNotify(WindowEvent e) {
//	        	System.out.println("Main frame windowDestroyNotify...");
//	        }
//
//	        public void windowRepaint(WindowUpdateEvent e) {
//	        	System.out.println("Main frame windowRepaint...");
//	        }
//
//	        public void windowResized(WindowEvent e) {
//	        	System.out.println("Main frame windowRepaint...");
//	        }
//	        
//	        public void windowMoved(WindowEvent e) {
//	        	System.out.println("Main frame windowRepaint...");
//	        }
//
//	        public void windowDestroyed(WindowEvent e) {
//	        	System.out.println("Main frame windowDestroyed...");
//	        }
//		});
//
//		Toolkit.getDefaultToolkit().addAWTEventListener( new WMV_MouseListener(), AWTEvent.MOUSE_EVENT_MASK | 
//														AWTEvent.FOCUS_EVENT_MASK);
//		Toolkit.getDefaultToolkit().addAWTEventListener( new WMV_MouseListener(), AWTEvent.MOUSE_EVENT_MASK | 
//					AWTEvent.FOCUS_EVENT_MASK | AWTEvent.WINDOW_EVENT_MASK | AWTEvent.WINDOW_FOCUS_EVENT_MASK);
//		Toolkit.getDefaultToolkit().addAWTEventListener( new WMV_MouseListener(), AWTEvent.MOUSE_EVENT_MASK | 
//														AWTEvent.FOCUS_EVENT_MASK | AWTEvent.WINDOW_EVENT_MASK);
//		Toolkit.getDefaultToolkit().addAWTEventListener( new WMV_MouseListener(), AWTEvent.MOUSE_EVENT_MASK | 
//														AWTEvent.WINDOW_FOCUS_EVENT_MASK);
//		Toolkit.getDefaultToolkit().addAWTEventListener( new WMV_MouseListener(), AWTEvent.MOUSE_EVENT_MASK );

		/* Panoramic Stitching */
		stitcher = new MV_Stitcher(world);

		/* Metadata */
		metadata = new WMV_Metadata(this, debug);
		loadExiftoolPath();
		
		if(debug.ml) systemMessage("Initial setup complete...");

		/* Graphics */
		colorMode(PConstants.HSB);
		rectMode(PConstants.CENTER);
		textAlign(PConstants.CENTER, PConstants.CENTER);
		delay(basicDelay);

		initCubeMap();
		delay(basicDelay);

		/* Sound */
		minim = new Minim(this);
		
		addShutdownHook();
	}
	
	/** 
	 * Main program loop 
	 */
	public void draw() 
	{
		background(0.f);										/* Clear screen */
		
		if (state.startup)
		{
			display.display(this);								/* Show startup screen */
			state.startup = false;	
		}
		else if(!state.running)
		{
			if(state.inLibrarySetup)
			{
				if(createNewLibrary)
				{
					if(state.chooseLibraryDestination)			/* Choose library destination */
						libraryDestinationDialog();
					
					display.display(this);						/* Display startup window(s) */
				}
				else
				{
					librarySelectionDialog();
				}
			}
			else
			{
				if(state.selectedNewLibraryDestination)
				{
					if(state.selectedNewLibraryMedia) createNewLibrary();
					else systemMessage("ML.draw()... ERROR: Selected library destination but no media folder selected!");
				}
				else if(state.selectedLibrary)
				{
					if(state.rebuildLibrary) rebuildSelectedLibrary();	/* Purge data directories to rebuild library during initialization */
					runWorldInitialization();							/* Initialize world once a library has been selected */
				}
				display.display(this);			/* Update display */
			}
		}
		else
		{
			if(!choosingField()) run();							/* Run MetaVisualizer */
		}
		
		if(setAppIcon) setAppIcon(appIcon);						/* Set app icon, if needed */
	}
	
	/**
	 * Whether currently choosing field
	 * @return
	 */
	public boolean choosingField()
	{
		return display.window.showListItemWindow && display.window.listItemWindowResultCode == 0;
	}
	
	/**
	 * Run program
	 */
	void run()
	{
		if( state.startedRunning )												/* If simulation just started running */
		{
			state.startedRunning = false;
			state.framesSinceStart = 0;
		}
		if ( state.exit ) 
		{
			exitProgram();														/* Exit program */		
		}
		else 
		{
			if ( !state.inFieldInitialization && !state.interactive ) 			/* Run program */
			{
				world.run();
//	 			input.updateLeapMotion();			// Update Leap Motion 
			}

			if( state.export && world.outputFolderSelected )						/* Screen capture */
				exportScreenImage();

			if( state.exportMedia && world.outputFolderSelected )					/* Media exporting */
				exportMedia();

//			if(state.exportCubeMap && world.outputFolderSelected)				/* Cubemap exporting */
//				exportCubeMap();

			if ( debug.memory && frameCount % world.getState().memoryCheckFrequency == 0 )		/* Memory debugging */
			{
				checkMemory();
				checkFrameRate();
			}
			
			state.framesSinceStart++;
		}
	}
	
	/**
	 * Initialize world, performing clustering and initialization on each field
	 */
	public void runWorldInitialization()
	{
		if(!state.inFieldInitialization)			/* Not yet initializing fields, start clustering */
		{
			if(state.interactive)					/* Run interactive clustering */
			{
				if(state.startInteractive && !state.interactive) startInteractiveClustering();						
				if(state.interactive && !state.startInteractive) runInteractiveClustering();	
			}
			else startFieldInitialization();					/* Run initial clustering */  	// -- Sets initialSetup to true	
		}
		else
		{
			if(!world.state.loadedMasks) world.loadMasks();
			runFieldInitialization();
		}
	}
	
	/**
	 * Begin and run initialization for each field 
	 */
	public void runFieldInitialization()
	{
		if( !state.fieldsInitialized )				/* Call until fields are initialized */
		{
			if (!state.initializingFields) 			/* Begin initializing fields */
			{
				world.createFields(library.getFolders());		/* Create field objects for each folder */
				state.initializingFields = true;
				display.setupProgress(0.25f);
			}
			else 
				initializeNextField();				/* Initialize next field */
		}
		else if( state.createdLibrary && !state.libraryNamed)
		{
			if(!display.window.showTextEntryWindow)
				openLibraryNamingDialog();			/* Open dialog to get library name */
		}
		else if( state.createdLibrary && !state.fieldsNamed && !state.inFieldNaming )
		{
			startFieldNaming();						/* Start field naming*/
		}
		else if( state.createdLibrary && state.inFieldNaming )							
		{
			runFieldNaming();						/* Run field naming*/
		}
		else
		{
			organizeMedia();						/* Analyze and organize media */
			finishInitialization();					/* Finish initialization and start running */
			display.setupScreen();					/* Finish display setup after App Window is visible*/
			display.setupProgress(0.f);
//			if(!appWindowVisible) showAppWindow();	/* Show App Window */
		}
	}

	/**
	 * Start initial clustering of media in fields
	 */
	public void startFieldInitialization()
	{
		display.startupMessages = new ArrayList<String>();	// Clear startup messages
		if(debug.metadata)
		{
			display.sendSetupMessage(world, "Library folder: "+library.getLibraryFolder());	// Show library folder name
			display.sendSetupMessage(world, " ");
		}
		display.display(this);
		
		if(!state.createdLibrary)
			display.window.showStartupWindow(true);			

		state.running = false;						// Stop running
		state.inFieldInitialization = true;			// Start clustering 
	}
	
	/**
	 * Initialize next field in world
	 */
	public void initializeNextField()
	{
		initializeField(world.getField(state.initializationField), true, true);		/* Initialize field */	
		
//		System.out.println( "ML.initializeNextField()... >>> showStartupWindow? "+display.window.showStartupWindow+
//				" setupStartupWindow:"+display.window.setupStartupWindow);
		
		if(debug.video) 
			systemMessage( "ML.initializeNextField()... After initialized field... Field videos: "+
							world.getField(state.initializationField).getVideoCount()+" video errors: "+
							world.getField(state.initializationField).getVideoErrors() );

		state.initializationField++;				/* Set next field to initialize */

		if( state.initializationField >= world.getFields().size() || state.singleField )	
		{
			state.fieldsInitialized = true;
			if(debug.ml) systemMessage("ML.initializeField()... " + world.getFields().size() + " field"+(world.getFields().size()>1?"s":"")+" initialized...");
			display.setupProgress(1.f);
			
			if(display.window.showCreateLibraryWindow) display.window.closeCreateLibraryWindow();
			if(display.window.setupStartupWindow) display.window.closeStartupWindow();
		}
		else
		{
			display.setupProgress(0.5f + (float)(state.initializationField-1) / (float)world.getFieldCount() * 0.5f);
		}
	}
	
	/**
	 * Initialize specified field
	 * @param f Field to initialize
	 * @param loadState Whether to load simulation state from data
	 * @param setSoundGPSLocations Whether to set sound GPS locations from GPS track
	 */
	public void initializeField(WMV_Field f, boolean loadState, boolean setSoundGPSLocations)
	{
		if(debug.ml && debug.detailed) 
			System.out.println("ML.initializeField()... fields initialized? "+state.fieldsInitialized);

		int fieldID = f.getID();
		
		if(!state.exit)
		{
			boolean success = false;
			
			if(loadState)								/* Attempt to load simulation state from data folder */
			{
				WMV_Field loadedField;
				if(fieldID + 1 >= world.getFields().size())
					loadedField = loadFieldState(f, library.getLibraryFolder(), true);	// Load field (load simulation state or, if fails, metadata), and set simulation state if exists
				else
					loadedField = loadFieldState(f, library.getLibraryFolder(), false);	// Load field (load simulation state or, if fails, metadata)
				
				if(world.getFields().size() == 0)				// Reset current viewer field
					if(world.viewer.getCurrentFieldID() > 0)
						world.viewer.setCurrentField(0, false);
				
				/* Check if field loaded correctly */
				success = (loadedField != null);												// If a field state was loaded
				if(success) world.setField(loadedField, fieldID);								// Attempt to set field from saved field state
				if(success) success = world.getField(fieldID).getClusters() != null;			// Check that clusters exist
				if(success) success = (world.getField(fieldID).getClusters().size() > 0);		
			}
			
			if(success)									/* Loaded field state from disk */
			{
				if(metadata.gpsTrackFilesFound) 			/* Load GPS tracks */
					world.getField(fieldID).setGPSTracks( metadata.loadGPSTracks( world.getField(fieldID) ) );
				
				world.getField(fieldID).setDataFolderLoaded(true);
				
				if(f.getID() == 0)
					display.window.setLibraryWindowText("Loading Environment...");		/* Change Library Window Text */

				if(debug.ml || debug.world) 
					systemMessage("ML.initializeField()... Succeeded at loading simulation state for Field #"+f.getID());
			}
			else											/* If failed to load field, initialize from metadata */
			{
				if(f.getID() == 0)						/* First field to be initialized */
					display.window.setLibraryWindowText("Building Environment...");		/* Change Library Window Text */

				if(debug.ml || debug.world) 
					systemMessage("ML.initializeField()... No simulation state to load... Initializing Field #"+f.getID());
				
				if(debug.video) 
					systemMessage( "ML.initializeField()... Before initialize... Field videos: "+
									world.getField(fieldID).getVideoCount()+" video errors: "+
									world.getField(fieldID).getVideoErrors() );

				boolean initialized = world.getField(fieldID).initialize();
				
				if(initialized)
				{
					world.getField(fieldID).setDataFolderLoaded(false);

					if(debug.video) 
						systemMessage( "ML.initializeField()... After initialize... Field videos: "+
								world.getField(fieldID).getVideoCount()+" video errors: "+
								world.getField(fieldID).getVideoErrors() );

					if(metadata.gpsTrackFilesFound) 
						world.getField(fieldID).setGPSTracks( metadata.loadGPSTracks(world.getField(fieldID)) );	// Load GPS tracks

					if(setSoundGPSLocations)
						if(world.getField(fieldID).getSounds().size() > 0)
							metadata.setSoundLocationsFromGPSTracks(world.getField(fieldID), world.getField(fieldID).getSounds());
				}
				else
				{
					systemMessage( "ML.initializeField()... ERROR initializing field #"+fieldID+"... will restart program...");

					restart();
				}
			}

			world.getField(fieldID).setLoadedState(success);		/* Set field loaded state flag */
		}
		
		world.getField(fieldID).setLoadedState(false);			/* Set field loaded state flag */
	}
	
	/**
	 * Load field state from disk
	 * @param f The field to initialize
	 * @param libraryFolder Library folder
	 * @param set Whether to set simulation state
	 * @return True if succeeded, false if failed
	 */
	private WMV_Field loadFieldState(WMV_Field f, String libraryFolder, boolean set)
	{
		boolean savedStateData = metadata.load(f, libraryFolder);		/* Load metadata from media associated with field */

		if(debug.video) 
			systemMessage("ML.loadFieldState()... Added videos: "+f.getVideoCount()+" video errors: "+f.getVideoErrors());

		if(savedStateData)		/* Attempt to load simulation state */
		{
			if(debug.ml && debug.detailed) systemMessage("ML.loadField()... Simulation State exists...");
	
			if(set)
				return world.loadAndSetSimulationState(f);
			else
				return world.loadSimulationState(f);			// -- Obsolete
		}
		else return null;
	}
	
	/**
	 * Organize media in each field
	 */
	private void organizeMedia()
	{
		ArrayList<WMV_Field> newFields = new ArrayList<WMV_Field>();
		
		for(WMV_Field f : world.getFields())
		{
			if(world.getSettings().divideFields)				/* Only divide fields if library created, not opened */
			{
				systemMessage("Attempting to divide field #"+f.getID()+"...");
				ArrayList<WMV_Field> addedFields = new ArrayList<WMV_Field>();
				
				if(state.createdLibrary)
				{
					addedFields = divideField(f);		/* Attempt to divide field */

					if(addedFields == null)				/* Check if field division succeeded */
					{
						f.organize(true);		/* If failed, analyze spatial and temporal features to create model */
					}
					else 
					{
						int count = 0;
						for(WMV_Field added : addedFields)
						{
							if(count < addedFields.size() - 1)	
								newFields.add(added);
							count++;
						}
					}
				}
				else
					f.organize(true);
			}
			else
				f.organize(true);
		}
		
		for(WMV_Field f : newFields) world.addField(f);			/* Add any new fields to world */
		world.settings.divideFields = false;	/* No need to further divide fields */
	}
	
	/**
	 * Copy all media objects from source to target field
	 * @param source Source field
	 * @param target Target field
	 */
	private void copyAllFieldMedia(WMV_Field source, WMV_Field target)		/* Re-add media from last added field */
	{
		for(WMV_Image img : source.getImages())
			target.addImage(img);
		for(WMV_Panorama pano : source.getPanoramas())
			target.addPanorama(pano);
		for(WMV_Video vid : source.getVideos())
			target.addVideo(vid);
		for(WMV_Sound snd : source.getSounds())
			target.addSound(snd);
	}
	
	/**
	 * Finish the world initialization process
	 */
	private void finishInitialization()
	{
		world.setBlurMasks();						// Set blur masks
		
		world.updateState();							// -- Only needed if field(s) loaded from data folder?
//		world.getCurrentField().updateAllMediaStates();				// -- Only needed if field(s) loaded from data folder!

		state.inFieldInitialization = false;				
		display.stopWorldSetup();
		
		state.running = true;
		state.startedRunning = true;
		
		if(debug.ml && debug.detailed) 
			systemMessage("Finishing MetaVisualizer initialization..");
		
		if(world.getFieldCount() > 1)
			world.openChooseFieldDialog();					/* Choose starting field */
		else
			world.enterFieldAtBeginning(0);						/* Enter first field */
	}

	/**
	 * Restart program and open Library dialog
	 */
	public void restart()
	{
		if(debug.ml) System.out.println("ML.restart()... Restarting...");

		display.disableLostFocusHook = true;
		display.window.closeAllWindows();

		state.reset();											// Reset to initial program state
		display.reset();										// Initialize displays

		delay(basicDelay);

		stitcher = new MV_Stitcher(world);						// Reset panoramic stitcher
		metadata = new WMV_Metadata(this, debug);				// Reset metadata loader
		loadExiftoolPath();										// Load Exiftool program path

		colorMode(PConstants.HSB);
		rectMode(PConstants.CENTER);

		systemMessages = new ArrayList<String>();				// Clear system messages
		world.reset(true);										// Reset world

		if(debug.ml) systemMessage("ML.restart()... Restart complete...");

		delay(basicDelay);

		display.window.openStartupWindow();
	}
	
	/**
	 * Export screen image or selected media files
	 */
	public void exportScreenImage()
	{
		saveFrame(world.outputFolder + "/" + world.getCurrentField().getName() + "-######.jpg");
//		System.out.println("Saved screen image: "+world.outputFolder + "/image" + "-######.jpg");
		state.export = false;
	}

	/**
	 * Export screen image or selected media files
	 */
	public void exportMedia()
	{
		if(world.viewer.getSettings().selection)
		{
			world.exportSelectedMedia();
			System.out.println("Exported image(s) to "+world.outputFolder);
		}
		state.exportMedia = false;
	}
	
	/**
	 * If large gaps are detected between media locations, divide target field into separate fields
	 * @param field Target field to divide
	 * @return Whether field was divided or not
	 */
	private ArrayList<WMV_Field> divideField(WMV_Field field)
	{
		ArrayList<WMV_Field> newFields = new ArrayList<WMV_Field>();				// List of new fields after division
		ArrayList<ArrayList<WMV_Waypoint>> gpsTracks = new ArrayList<ArrayList<WMV_Waypoint>>(field.getGPSTracks());	// Store GPS tracks

//		field.setGPSTracks(null);												// Clear GPS tracks from original field

		newFields = field.divide(world, 3000.f, 15000.f);				// Attempt to divide field

		if(newFields != null)
		{
			if(newFields.size() > 1)
			{
				int count = 0;
				for(WMV_Field f : newFields)
				{
					systemMessage("ML.divideField()... Will initialize field #"+f.getID()+" name:"+f.getName()+" of "+newFields.size()+"...");

					f.renumberMedia();							/* Renumber media in field from index 0 */
					if(count < newFields.size() - 1)
					{
						initializeField(f, false, false);			/* Initialize field */
						f.organize(true);							/* Analyze media locations and times to create model */
						library.moveFieldMediaFiles(field, f);		/* Move media into new field folders */
					}
					else												/* Initialize last field */
					{
						systemMessage("ML.divideField()... Last of new fields from dividing field id #"+field.getID());

						field.reset();								/* Clear field */
						copyAllFieldMedia(f, field);					/* Re-add media from last added field */
						initializeField(field, false, false);		/* Initialize field */
						field.organize(true);						/* Analyze media locations and times to create model */
					}
					count++;
				}

				if(gpsTracks.size() > 0)
				{
					field.setGPSTracks(null);						/* Clear GPS tracks from original field */
					for(ArrayList<WMV_Waypoint> gt : gpsTracks)		/* Look through saved GPS tracks for matching field */
					{
						for(WMV_Field f : newFields)
						{
							WMV_Model m = f.getModel();
							for(WMV_Waypoint w : gt)
							{
								if(m.containsGPSPoint(w.getGPSLocation()))	// Point is in field
									f.addGPSTrack(gt);
							}
						}
					}
				}
				return newFields;
			}
			else
				return null;
		}
		else
			return null;
	}

	/**
	 * Run interactive clustering 
	 */
	public void runInteractiveClustering()
	{
		background(0.f);					// Clear screen
		display.display(this);						// Draw text		
	}
	
	/**
	 * Finish running interactive clustering and restart simulation 		// -- Disabled
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
	 * Begin new library creation 					
	 */
	public void startCreatingNewLibrary()
	{
		state.inLibrarySetup = true;
		state.createdLibrary = false;			// Added 7-1-17
		state.chooseLibraryDestination = false;	// Added
		createNewLibrary = true;
		state.chooseMediaFolders = true;
	}
	
	/**
	 * Import media folders and create new library
	 */
	private void createNewLibrary()
	{
		if(library.mediaFolders.size() > 0)
		{
			if(debug.ml) System.out.println("ML.createNewLibrary()... Will create new library at: "+library.getLibraryFolder()+" from "+library.mediaFolders.size()+" imported media folders...");
			state.selectedLibrary = library.create(this, library.mediaFolders);	// Set selectedLibrary to true

			state.createdLibrary = true;
			state.libraryNamed = false;
			state.selectedNewLibraryDestination = false;

			if(debug.ml)
				systemMessage("ML.createNewLibrary()... After create()... state.selectedLibrary:"+state.selectedLibrary+"  state.inLibrarySetup:"+state.inLibrarySetup);

			world.state.stitchingPath = library.getLibraryFolder() + "/stitched/";
			if(debug.ml)
				systemMessage("ML.createNewLibrary()... Set stitching path:"+world.getState().stitchingPath);

			if(!state.selectedLibrary)
			{
				System.out.println("ML.createNewLibrary()... Error importing media to create library...");
//				exit();
				restart();
			}
		}
	}
	
	
	/**
	 * Prepare library for rebuilding by deleting data folders
	 */
	public void rebuildSelectedLibrary()
	{
		if(library.getFolders() != null)
		{
			ArrayList<String> libFolders = library.getFolders();
			display.window.setLibraryWindowText("Rebuilding Environment...");		// -- Not being called
			for(String strFolderName : libFolders)
			{
				File folderFile = new File(library.getLibraryFolder() + "/" + strFolderName);
				if(folderFile.exists() && folderFile.isDirectory())
				{
					File[] fileList = folderFile.listFiles();
					for(int i=0; i<fileList.length; i++)
					{
						File file = fileList[i];
						if(file.getName().equals("data") && file.isDirectory())
						{
							if(debug.ml || debug.library) 
								System.out.println("ML.rebuildSelectedLibrary()... Purging data folder:"+file.getName());

							world.utilities.purgeDirectory(file);
							file.delete();
						}
					}
				}
				else
				{
					systemMessage("ML.rebuildSelectedLibrary()... ERROR: folderFile missing or not a directory!  (library.getLibraryFolder() + strFolderName):"+library.getLibraryFolder() + "/" + strFolderName);
				}
			}
		}
		
		state.rebuildLibrary = false;
	}

	/**
	 * Open library folder when folder has been selected
	 * @param selection File object for selected folder
	 */
	public void libraryFolderSelected(File selection) 
	{
		boolean selected = (selection != null);
		if(selected)
		{
			display.window.lblStartupWindowText.setVisible(true);		// Set "Please wait..." text
			selected = openLibraryFolder(selection);
		}
		if(!selected)												// Valid library was not selected
		{
			state.inLibrarySetup = false;
			display.window.btnCreateLibrary.setVisible(true);
			display.window.btnOpenLibrary.setVisible(true);
			display.window.chkbxRebuildLibrary.setVisible(true);
//			display.window.btnLibraryHelp.setVisible(true);
			display.window.lblStartup.setVisible(true);
			display.window.lblStartupWindowText.setVisible(false);
			
			display.window.showStartupWindow(false);					// Show Startup Window again
//			display.window.showStartupWindow(true);
		}
	}

	/**
	 * Open library destination folder when folder has been selected
	 * @param selection File object for selected folder
	 */
	public void newLibraryDestinationSelected(File selection) 
	{
		if(selection != null)
		{
			if(selection.isDirectory())
			{
				File newLibraryFile = new File(selection.getAbsolutePath() + "/library.mlibrary");
				if(!newLibraryFile.exists())
					newLibraryFile.mkdir();
				openNewLibraryDestination(newLibraryFile);
			}
			else
			{
				System.out.println("ML.newLibraryDestinationSelected()... ERROR... not a directory!");
				selectFolderDialog("Select library destination:", 0);  
//				selectFolder("Select library destination:", "newLibraryDestinationSelected");		// Get filepath of Library folder
			}
		}
		else
		{
			selectFolderDialog("Select library destination:", 0);  
//			selectFolder("Select library destination:", "newLibraryDestinationSelected");		// Get filepath of Library folder
		}
	}

	/**
	 * Open media folder when folder has been selected
	 * @param selection File object for selected folder
	 */
	public void mediaFolderSelected(File selection) 
	{
		if(selection != null)
		{
			display.window.lblStartupWindowText.setVisible(true);			// Set "Please wait..." text
			openMediaFolder(selection);
		}
	}

	/**
	 * Stop the program
	 */
	public void exitProgram() 
	{
		System.out.println("Exiting "+appName+"...");
		exit();
	}
	
	/**
	 * Analyze and load media folders given user selection
	 * @param selection Selected folder
	 */
	public void openNewLibraryDestination(File selection) 
	{
		boolean selectedFolder = false;
		
		if (selection == null) {
			if (debug.ml)
				System.out.println("openLibraryDestination()... Window was closed or the user hit cancel.");
		} 
		else 
		{
			String input = selection.getPath();
//			String[] parts = input.split("/");

			if (debug.ml)
				System.out.println("User selected library destination: " + input);

			File file = new File(input);
			if(file.exists())
			{
				if(file.isDirectory())
				{
					selectedFolder = true;
					String libFilePath = input;

					library.setLibraryFolder(libFilePath);
					library.addFolder("field");
					delay(basicDelay);
				}
			}
		}
		
		if(selectedFolder)
		{
			state.selectedNewLibraryDestination = true;			// Library destination folder has been selected
			state.inLibrarySetup = false;						// Library setup complete
			display.window.btnImportMediaFolder.setVisible(false);
			display.window.btnMakeLibrary.setVisible(false);
			display.window.btnCancelCreateLibrary.setVisible(false);
			display.window.lblImport.setVisible(false);
			display.window.lblCreateLibraryWindowText.setVisible(true);			// Set "Please wait..." text
			display.window.lblCreateLibraryWindowText2.setVisible(true);			// Set "Please wait..." text
			display.window.setCreateLibraryWindowText("Creating Environment...", "This process can take a while for large media collections...");
		}
		else
		{
			state.selectedNewLibraryDestination = false;		// Not a folder or folder doesn't exist
			libraryDestinationDialog();							// Retry prompt
		}
	}

	/**
	 * Open existing media folder
	 * @param selection Selected folder
	 */
	public void openMediaFolder(File selection) 
	{
		if (selection == null) 
		{
			System.out.println("openMediaFolder()... Window was closed or the user hit cancel.");
		} 
		else 
		{
			String input = selection.getPath();

			if (debug.metadata)
				System.out.println("User selected media folder: " + input);

			File file = new File(input);
			if(file.exists())
			{
				if(file.isDirectory())
				{
					library.mediaFolders.add(input);
					delay(basicDelay);
				}
			}
		}
	}
	
	/**
	 * Analyze and load media folders given user selection
	 * @param selection Selected folder
	 */
	public boolean openLibraryFolder(File selection) 
	{
		boolean selectedFolder = false;
		
//		if(!windowVisible)
//			showAppWindow();

		if (selection != null) 
		{
			String input = selection.getPath();

			if (debug.metadata)
				System.out.println("User selected library folder: " + input);

			library = new MV_Library(input);

			String[] parts = input.split("/");
			String[] nameParts = parts[parts.length-1].split("\\.");		// Check if single field library 

//			boolean singleField;
			if(nameParts[nameParts.length-1].equals("mlibrary"))
				state.singleField = false;
			else
				state.singleField = true;
			
			String parentFilePath = "";
			if(state.singleField)
			{
				if(debug.ml) systemMessage("ML.openLibraryFolder()... Loading single field...");
				String libFilePath = "";
				for(int i=0; i<parts.length-1; i++)
				{
					libFilePath = libFilePath + parts[i] + "/";
				}

				library = new MV_Library(libFilePath);				/* Create library object */
				library.addFolder(parts[parts.length-1]);			/* Add folder */
				
				selectedFolder = true;

				for(int i=0; i<parts.length-2; i++)
					parentFilePath = parentFilePath + parts[i] + "/";
			}
			else
			{
				if(debug.ml) systemMessage("ML.openLibraryFolder()... Loading media library...");
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
			selectedFolder = true;
		}
		
		if(selectedFolder)
		{
			state.selectedLibrary = true;	// Library folder has been selected
			state.inLibrarySetup = false;	// End library setup
			return true;
		}
		else
		{
			return false;
//			state.selectedLibrary = false;	// Library in improper format if masks are missing
//			state.inLibrarySetup = true;		// Still in library setup
//			
////			if(!display.window.showStartupWindow)
//				display.window.showStartupWindow(true);
		}
	}

	public void addShutdownHook()
	{
		Runtime.getRuntime().addShutdownHook(new Thread() {
	        @Override
	        public void run() {	 									// Stackless deletion
	            if(debug.ml) systemMessage("Running Shutdown Hook");

	            try {
	            	String homeDir = System.getProperty("user.home");
	            	

//	            	DateTimeFormatter format = DateTimeFormatter.ofPattern("MMM d yyyy  hh:mm a");
	            	DateTimeFormatter format = DateTimeFormatter.ofPattern("hh_mm_a-MM_d_yy");
	            	LocalDateTime date = LocalDateTime.now();
	            	String dateStr = format.format(date);
//	            	if(debug.ml) systemMessage("addShutdownHook()... Date: "+dateStr); //2016/11/16 12:08:43
	            	format.format(date);
	            	
	                File errorTextFile = new File(homeDir + "/MetaVisualizer_Log_"+dateStr+".txt");
	                if ( !errorTextFile.exists() )
	                	errorTextFile.createNewFile();

	                FileWriter fw = new FileWriter(errorTextFile);
	            	for(String line : systemMessages)
		                fw.write(line + System.lineSeparator());
	                fw.close();

	            } catch (IOException iox) {
	                //do stuff with exception
	                iox.printStackTrace();
	                File errorFile = new File("~/MetaVisualizer_ErrorLog.txt");
	                try{
	                	PrintWriter pr = new PrintWriter(errorFile);
	                	iox.printStackTrace(pr);
	                }
	                catch(Throwable t)
	                {
	                	System.out.println("Error...");
	                }
	            }
	            String root = MetaVisualizer.tempDir;
	            Stack<String> dirStack = new Stack<String>();
	            dirStack.push(root);
	            while(!dirStack.empty()) 
	            {
	                String dir = dirStack.pop();
	                File f = new File(dir);
	                if(f.listFiles().length==0)
	                {
	                	if(debug.ml) systemMessage("Deleting f:"+f.getName());
	                    f.delete();
	                }
	                else {
	                    dirStack.push(dir);
	                    for(File ff: f.listFiles()) 
	                    {
	                        if(ff.isFile())
	                        {
	    	                	if(debug.ml) systemMessage("Deleting ff:"+ff.getName());
	                            ff.delete();
	                        }
	                        else if(ff.isDirectory())
	                            dirStack.push(ff.getPath());
	                    }
	                }
	            }
	        }
	    });
	}
	
	/**
	 * Called when image output folder has been selected
	 * @param selection
	 */
	public void outputFolderSelected(File selection) 
	{
		if (selection == null) 
		{
			if (debug.ml)
				println("Window was closed or the user hit cancel.");
		} 
		else 
		{
			String input = selection.getPath();

			if (debug.ml)
				println("----> User selected output folder: " + input);

			world.outputFolder = input;
			world.outputFolderSelected = true;
		}
	}
	
	/**
	 * Called when a new video frame is available to read
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
			if(debug.video)
				println("movieEvent() NullPointerException:"+npe);
		}
		catch(Throwable t)
		{
			if(debug.video)
				println("movieEvent() Throwable:"+t);
		}
	}
		
	/**
	 * Respond to button event
	 * @param button Button acted on
	 * @param event Button event
	 */
	public void handleButtonEvents(GButton button, GEvent event) { 
		input.handleButtonEvent(this, display, button, event);
	}
	
	/**
	 * Respond to toggle control event
	 * @param button Toggle control acted on
	 * @param event Toggle control event
	 */
	public void handleToggleControlEvents(GToggleControl option, GEvent event) 
	{
		input.handleToggleControlEvent(world, display, option, event);
	}
	
	/**
	 * Respond to slider event
	 * @param button Slider acted on
	 * @param event Slider event
	 */
	public void handleSliderEvents(GValueControl slider, GEvent event) 
	{ 
		input.handleSliderEvent(world, display, slider, event);
	}

	public void handleTextEvents(GEditableTextControl textcontrol, GEvent event) {
//		  if (textcontrol == txaDemo)
//		    lblAction.setText("TextArea: " + event);
//		  if (textcontrol == txfDemo)
//		    lblAction.setText("TextField: " + event);
		}

	/**
	 * Processing method called when a key is pressed
	 */
	public void keyPressed() 
	{
		if(state.running)
		{
			if(debug.input)
			{
				if(key == PApplet.CODED)
					systemMessage("ML.keyPressed()... coded key:"+key+" keyCode:"+keyCode);
				else
					systemMessage("ML.keyPressed()... key:"+key);
			}
			
			input.handleKeyPressed(this, key, keyCode);
		}
//		if(state.running && state.framesSinceStart > world.viewer.getSettings().teleportLength) 
//			input.handleKeyPressed(this, key, keyCode);
	}

	/**
	 * Processing method called when a key is released
	 */
	public void keyReleased() 
	{
		if(state.running) 
		{
			if(debug.input)
			{
				if(key == PApplet.CODED)
					systemMessage("ML.keyReleased()... coded key:"+key+" keyCode:"+keyCode);
				else
					systemMessage("ML.keyReleased()... key:"+key);
			}
			
			input.handleKeyReleased(this, display, key, keyCode);
		}
	}
	
	/**
	 * Respond to key pressed in MetaVisualizer Window
	 * @param applet Parent App
	 * @param windata Window data
	 * @param keyevent Key event
	 */
	synchronized public void mvWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			input.handleKeyPressed(this, keyevent.getKey(), keyevent.getKeyCode());
		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			input.handleKeyReleased(this, display, keyevent.getKey(), keyevent.getKeyCode());
	}
	
	/**
	 * Respond to key pressed in MetaVisualizer Window
	 * @param applet Parent App
	 * @param windata Window data
	 * @param keyevent Key event
	 */
	synchronized public void libraryWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			input.handleLibraryWindowKeyPressed(this, keyevent.getKey(), keyevent.getKeyCode());
		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			input.handleKeyReleased(this, display, keyevent.getKey(), keyevent.getKeyCode());
	}
	
	/**
	 * Respond to key pressed in MetaVisualizer Window
	 * @param applet Parent App
	 * @param windata Window data
	 * @param keyevent Key event
	 */
	synchronized public void importWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			input.handleKeyPressed(this, keyevent.getKey(), keyevent.getKeyCode());
		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			input.handleKeyReleased(this, display, keyevent.getKey(), keyevent.getKeyCode());
	}

	/**
	 * Respond to key pressed in List Item Window
	 * @param applet Parent App
	 * @param windata Window data
	 * @param keyevent Key event
	 */
	synchronized public void listItemWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			input.handleListItemWindowKeyPressed(this, keyevent.getKey(), keyevent.getKeyCode());
//		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
//			input.handleKeyReleased(world.viewer, display, keyevent.getKey(), keyevent.getKeyCode());
	}
	
	/**
	 * Respond to key pressed in Navigation Window
	 * @param applet Parent App
	 * @param windata Window data
	 * @param keyevent Key event
	 */
	synchronized public void navigationWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			input.handleKeyPressed(this, keyevent.getKey(), keyevent.getKeyCode());

		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			input.handleKeyReleased(this, display, keyevent.getKey(), keyevent.getKeyCode());
	}
	
	/**
	 * Respond to key pressed in Media Window
	 * @param applet Parent App
	 * @param windata Window data
	 * @param keyevent Key event
	 */
	synchronized public void mediaWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(debug.input)
			systemMessage(">>> ML.mediaWindowKey()... keyevent.getAction():"+keyevent.getAction());

//		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
		{
			if(debug.input)
			{
				if(key == PApplet.CODED)
					systemMessage("ML.mediaWindowKey()... KeyEvent.PRESS... key:"+key);
				else
					systemMessage("ML.mediaWindowKey()... KeyEvent.PRESS... coded key:"+key+" keyCode:"+keyCode);
			}

			input.handleKeyPressed(this, keyevent.getKey(), keyevent.getKeyCode());
//			delay(1);			// TESTING
		}
		else if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
		{
			if(debug.input)
			{
				if(key == PApplet.CODED)
					systemMessage("ML.mediaWindowKey()... KeyEvent.RELEASE... key:"+key);
				else
					systemMessage("ML.mediaWindowKey()... KeyEvent.RELEASE... coded key:"+key+" keyCode:"+keyCode);
			}
			input.handleKeyReleased(this, display, keyevent.getKey(), keyevent.getKeyCode());
//			delay(1);			// TESTING
		}
		else
		{
			if(debug.input)
				systemMessage("ML.mediaWindowKey()... Unknown KeyEvent... keyevent.getAction():"+keyevent.getAction()+" is it: processing.event.KeyEvent.TYPE:"+processing.event.KeyEvent.TYPE);
		}
	}

	/**
	 * Respond to key pressed in Statistics Window
	 * @param applet Parent App
	 * @param windata Window data
	 * @param keyevent Key event
	 */
	synchronized public void preferencesWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			input.handleKeyPressed(this, keyevent.getKey(), keyevent.getKeyCode());
		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			input.handleKeyReleased(this, display, keyevent.getKey(), keyevent.getKeyCode());
	}
	
	/**
	 * Respond to key pressed in Help Window
	 * @param applet Parent App
	 * @param windata Window data
	 * @param keyevent Key event
	 */
	synchronized public void helpWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			input.handleKeyPressed(this, keyevent.getKey(), keyevent.getKeyCode());
		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			input.handleKeyReleased(this, display, keyevent.getKey(), keyevent.getKeyCode());
	}
	
	/**
	 * Respond to key pressed in Media Window
	 * @param applet Parent App
	 * @param windata Window data
	 * @param keyevent Key event
	 */
	synchronized public void timelineWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			input.handleKeyPressed(this, keyevent.getKey(), keyevent.getKeyCode());
		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			input.handleKeyReleased(this, display, keyevent.getKey(), keyevent.getKeyCode());
	}
	
	/**
	 * Respond to mouse pressed event
	 */
	public void mousePressed()
	{
		input.handleMousePressed(world, display, mouseX, mouseY, frameCount);

//		if(world.viewer.mouseNavigation)
//			input.handleMousePressed(mouseX, mouseY);
		
		if(debug.mouse)
			systemMessage("ML.mousePressed()... Mouse x:"+mouseX+" y:"+mouseY);
		
		display.map2D.mousePressedFrame = frameCount;
	}

	/**
	 * Respond to mouse released event
	 */
	public void mouseReleased() 
	{
		if(display.getDisplayView() != 0)				// Mouse not used in World View
			input.handleMouseReleased(world, display, mouseX, mouseY, frameCount);
		
		if(debug.mouse)
			systemMessage("ML.mouseReleased()... Mouse x:"+mouseX+" y:"+mouseY);

//		if(world.viewer.mouseNavigation)
//			input.handleMouseReleased(mouseX, mouseY);
	}
	
	/**
	 * Respond to mouse clicked event
	 */
	public void mouseClicked() 
	{
//		if(world.viewer.mouseNavigation)
//			input.handleMouseClicked(mouseX, mouseY);

		if(debug.mouse)
			systemMessage("ML.mouseClicked()... Mouse x:"+mouseX+" y:"+mouseY);
	}
	
	/**
	 * Respond to mouse dragged event
	 */
	public void mouseDragged() {
		display.map2D.mouseDraggedFrame = frameCount;
		if(debug.mouse)
		{
			systemMessage("ML.mouseDragged()... pmouseX:"+pmouseX+" pmouseY:"+pmouseY);
			systemMessage("mouseX:"+mouseX+" mouseY:"+mouseY);
		}
//		input.handleMouseDragged(pmouseX, pmouseY);
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
	 * Open dialog to name created library
	 */
	private void openExiftoolPathDialog()
	{
		display.window.openTextEntryWindow("Please enter path to Exiftool:", "/usr/local/bin/Exiftool", 2);
		state.gettingExiftoolPath = true;
	}
	
	/**
	 * Attempt to load and set path to Exiftool from preferences
	 */
	public void loadExiftoolPath()
	{
		boolean setExiftoolPath = setExiftoolPathFromPrefs();
//		utilities.checkPath();	// -- Debugging
		
		if(!setExiftoolPath)
		{
			String exiftoolPath = "/usr/local/bin/exiftool";
			metadata.exiftoolFile = new File(exiftoolPath);						// Initialize metadata extraction class	
			if(metadata.exiftoolFile.exists())												
			{
				if(metadata.exiftoolFile.getName().equals("Exiftool"))
					saveExiftoolPath(exiftoolPath);								// Save Exiftool path if found
			}
			else
			{
				if(debug.ml) 
					systemMessage("ML.loadExiftoolPath()... Exiftool not found at exiftoolPath:"+exiftoolPath+"!  Will search...");
				
				boolean found = false;
				String programPath = utilities.getProgramPath("exiftool");

				if(programPath != null) found = true;
				if(found)
				{
					metadata.exiftoolFile = new File(utilities.getProgramPath("exiftool"));
					found = metadata.exiftoolFile.exists();
					found = metadata.exiftoolFile.getName().equals("Exiftool");
				}
				if(!found)												// Fatal error if Exiftool not found
				{
					if(debug.ml) 
						systemMessage("ML.loadExiftoolPath()... Exiftool not found in expected folders...  Will ask for user entry...");
					openExiftoolPathDialog();
				}
			}
		}
	}
	
	/**
	 * Save Exiftool file path to preferences and load Exiftool program file
	 * @param newExiftoolPath
	 */
	public void setExiftoolPath(String newExiftoolPath)
	{
		saveExiftoolPath(newExiftoolPath);
		metadata.exiftoolFile = new File(newExiftoolPath);
		
		boolean found = metadata.exiftoolFile.exists();
		found = metadata.exiftoolFile.getName().equals("Exiftool");
		if(!found)												// Fatal error if Exiftool not found
		{
			if(debug.ml) systemMessage("ML.setExiftoolPath()... Exiftool not found in specified location...  Will ask for user entry...");
		}
		else 
		{
			systemMessage("Set Exiftool File... name:"+metadata.exiftoolFile.getName());
			state.gettingExiftoolPath = false;					// Set getting Exiftool path flag to false
			state.startup = true;								// Tell startup window to open
		}
	}
	
	/**
	 * Save path to Exiftool program
	 * @param exiftoolPath
	 */
	public void saveExiftoolPath(String exiftoolPath) {
		Preferences preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.put("Exiftool", exiftoolPath);
		
		if(debug.ml) systemMessage("ML.saveExiftoolPath()... exiftoolPath:"+exiftoolPath);

		try {
			preferences.flush();
		}
		catch(BackingStoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Attempt to set Exiftool path from preferences
	 * @return Whether succeeded
	 */
	public boolean setExiftoolPathFromPrefs() 
	{
		Preferences preferences = Preferences.userNodeForPackage(this.getClass());
		String exiftoolPath = preferences.get("Exiftool", "");
		
		if(exiftoolPath != null)
		{
			if(exiftoolPath != "")
			{
//				if(debug.ml)
//					systemMessage("ML.setExiftoolPathFromPrefs()... Found exiftoolPath:"+exiftoolPath);
				metadata.exiftoolFile = new File(exiftoolPath);						// Initialize metadata extraction class	
				return true;
			}
		}
			
		return false;
	}
	
	/**
	 * Get image from resources
	 * @param fileName File name
	 * @return Mask image
	 */
	public PImage getImageResource(String fileName)
	{
		String resourcePath = "/main/resources/images/";
		BufferedImage image;
		
		URL imageURL = MetaVisualizer.class.getResource(resourcePath + fileName);
		try{
			image = ImageIO.read(imageURL.openStream());
			return utilities.bufferedImageToPImage(image);
		}
		catch(Throwable t)
		{
			systemMessage("ERROR in getImageResource... t:"+t+" imageURL == null? "+(imageURL == null));
		}
		
		return null;
	}
	
	/**
	 * Get image from resources
	 * @param fileName File name
	 * @return Mask image
	 */
	public String getScriptResource(String fileName)
	{
		String resourcePath = "/scripts/";
//		String resourcePath = "/src/main/resources/scripts/";

		String filePath = resourcePath + fileName;
		
		StringBuilder result = new StringBuilder("");
		try{
			String line;
			InputStream in = MetaVisualizer.class.getResourceAsStream(filePath); 
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			while ((line = reader.readLine()) != null) {
				result.append(line);
				result.append(System.getProperty("line.separator"));
			}
			in.close();
		}
		catch(Throwable t)
		{
			systemMessage("ERROR in getScriptResource... t:"+t+" fileName:"+fileName+" filePath:"+filePath);
			t.printStackTrace();
		}

		BufferedWriter writer = null;
		String scriptName = "Convert_to_480p.txt";
		String scriptTxtPath = tempDir + "/" + scriptName;
		String scriptPath = tempDir + "/" + "Convert_to_480p.scpt";
		
		try {
			File file = new File(scriptTxtPath);
			if(!file.exists())
				file.createNewFile();
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(result.toString());
		} 
		catch (Throwable t)
		{
			systemMessage("ERROR 2 in getScriptResource... t:"+t);
		}

		try{
			if (writer != null) writer.close();
		}
		catch(IOException io)
		{
			systemMessage("ERROR 3 in getScriptResource... t:"+io);
		}
		
		Runtime runtime = Runtime.getRuntime();
		String[] args = { "osacompile", "-o", scriptPath, scriptTxtPath };
		try
		{
			Process process = runtime.exec(args);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return scriptPath;
	}

	/**
	 * Open dialog to name created library
	 */
	private void openLibraryNamingDialog()
	{
		display.window.openTextEntryWindow("Enter new library name:", "library", 1);
//		state.inLibraryNaming = true;
	}
	
	/**
	 * Start naming fields
	 */
	private void startFieldNaming()
	{
		for(WMV_Field f : world.getFields())	
			f.setNamed(false);
		
		String curName = world.getField(state.namingField).getName();
		display.window.openTextEntryWindow("Enter field #"+(state.namingField+1)+" name...", curName, 0);						// Open text entry dialog

		state.namingField = 0;
		state.inFieldNaming = true;
		state.oldFieldName = world.getField(state.namingField).getName();
	}
	
	/**
	 * Run field naming process
	 */
	private void runFieldNaming()
	{
		if(state.namingField + 1 >= world.getFieldCount())
		{
			if(world.getField(state.namingField).getState().named)
			{
				updateFieldFolderName(state.namingField);
				state.fieldsNamed = true;
				state.inFieldNaming = false;
				library.updateFolderNames(world);		// Update library folder names to match fields
				delay(basicDelay);
			}
		}
		else
		{
			updateFieldFolderName(state.namingField);	// Update field folder name in case necessary
			state.namingField++;
			if(!display.window.showTextEntryWindow && state.namingField < world.getFieldCount())
			{
				String curName = world.getField(state.namingField).getName();		// Get current field folder name
				display.window.openTextEntryWindow("Enter field #"+state.namingField+" name...", curName, 0);						// Open text entry dialog
			}
		}
	}
	
	/**
	 * Update field folder name
	 * @param fieldIdx Field idx to update name for
	 */
	private void updateFieldFolderName(int fieldIdx)
	{
		String fieldName = world.getField(fieldIdx).getName();
		boolean result = world.utilities.renameFolder(library.getLibraryFolder() + "/" + state.oldFieldName, library.getLibraryFolder() + "/" + fieldName, false);
		world.updateMediaFilePaths();		// Update media file paths with new library name
	}

	public void mediaFolderDialog()
	{
		display.window.lblStartupWindowText.setVisible(true);
		selectFolder("Select media folder:", "mediaFolderSelected");		// Get filepath of PhotoSceneLibrary folder
	}
	
	public void libraryDestinationDialog()
	{
		state.chooseLibraryDestination = false;
//		display.window.setCreateLibraryWindowText("Please select library destination...", null);

		selectFolderDialog("Select library destination:", 0);  
//		selectFolder("Select library destination:", "newLibraryDestinationSelected");		// Get filepath of PhotoSceneLibrary folder
	}
	
	public void librarySelectionDialog()
	{
		state.inLibrarySetup = false;
		selectFolder("Select library folder:", "libraryFolderSelected");		// Get filepath of PhotoSceneLibrary folder
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
			
			if(performanceSlow && debug.memory)
				display.message(this, "Performance slow...");
		}
		else
		{
			if(performanceSlow)
				performanceSlow = false;
		}
	}

	/**
	 * Check current memory 
	 */
	public void checkMemory()
	{
		  availableProcessors = Runtime.getRuntime().availableProcessors();		/* Total number of processors or cores available to the JVM */
		  freeMemory = Runtime.getRuntime().freeMemory();		  /* Total amount of free memory available to the JVM */
		  maxMemory = Runtime.getRuntime().maxMemory();		  /* Maximum amount of memory the JVM will attempt to use */
		  totalMemory = Runtime.getRuntime().totalMemory();		  /* Total memory currently in use by the JVM */
		  allocatedMemory = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
		  approxUsableFreeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;

		  if(debug.memory)
		  {
			  if(debug.detailed)
			  {
				  systemMessage("Total memory (bytes): " + totalMemory);
				  systemMessage("Available processors (cores): "+availableProcessors);
				  systemMessage("Maximum memory (bytes): " +  (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory)); 
				  systemMessage("Total memory (bytes): " + totalMemory);
				  systemMessage("Allocated memory (bytes): " + allocatedMemory);
			  }
			  systemMessage("Free memory (bytes): "+freeMemory);
			  systemMessage("Approx. usable free memory (bytes): " + approxUsableFreeMemory);
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
//		    systemMessage("File system root: " + root.getAbsolutePath());
//		    systemMessage("Total space (bytes): " + root.getTotalSpace());
//		    systemMessage("Free space (bytes): " + root.getFreeSpace());
//		    systemMessage("Usable space (bytes): " + root.getUsableSpace());
//		  }
	}

	/**
	 * Hide main app window
	 */
//	private void hideAppWindow()
//	{
//		setSurfaceSize(3, 2);
//		appWindowVisible = false;
//	}
	
//	/**
//	 * Show main app window
//	 */
//	private void showAppWindow()
//	{
//		setSurfaceSize(displayWidth, displayHeight);		// Set surface size in fullscreen mode
//		appWindowVisible = true;
//	}

	/**
	 * Send debug message (effect depends on debug settings)
	 * @param message Message to be sent
	 */
	public void systemMessage(String message)
	{
		if(debug.print)
			System.out.println(message);
		if(debug.messages)
			display.message(this, message);
		if(debug.file)
			systemMessages.add(message);
//		if(debugSettings.output)
//			debugMessages.add(frameCount + " :" + message);
	}
	
	/**
	 * Set application icon
	 * @param img
	 */
	@SuppressWarnings("restriction")
	private void setAppIcon(PImage img) 
	{
		Application.getApplication().setDockIconImage(img.getImage());
		setAppIcon = false;
//		if(debugSettings.ml && debugSettings.detailed)
//			System.out.println("setAppIcon()... frameCount:"+frameCount);
	}

	/**
	 * Set main window size
	 * @param newWidth New width
	 * @param newHeight New height
	 */
	public void setSurfaceSize(int newWidth, int newHeight)
	{
		surface.setSize(newWidth, newHeight);
	}

	/**
	 * Set window resolution and graphics mode
	 */
	public void settings() 
	{
		fullScreen(processing.core.PConstants.P3D);										// Full screen
//		fullScreen(processing.core.PConstants.P3D, processing.core.PConstants.SPAN);	// Multi monitor setup
		
//		size(appWidth, appHeight, processing.core.PConstants.P3D);						// MacBook Pro-size Window
//		size(displayWidth, displayHeight, processing.core.PConstants.P3D);				// Screen size Window
//		size(960, 540, processing.core.PConstants.P3D);									// Web Video Large
		
		delay(basicDelay);
	}

	/** 
	 * Load the PApplet either in a window of specified size or in fullscreen
	 */
	static public void main(String[] args) 
	{
		PApplet.main("main.java.com.entoptic.metaVisualizer.MetaVisualizer");				// Open PApplet in window
//		PApplet.main("multimediaLocator.MultimediaLocator");						// Open PApplet in window
//		PApplet.main(new String[] { "--present", "wmViewer.MultimediaLocator" });	// Open PApplet in fullscreen mode
	}
	
	/**
	 * Window listener for handling Main Window lost / gained focus
	 * @author davidgordon
	 */
	private class WMV_WindowListener implements WindowListener
	{
		/**
		 * Constructor for window listener
		 */
		public WMV_WindowListener(){}

		public void windowGainedFocus(WindowEvent e) {
//			systemMessage("Main Window windowGainedFocus...");
			if(mainWindowLostFocus)
			{
				systemMessage("Main Window gained focus after losing focus...");
				if(!display.disableLostFocusHook)
				{
					if(display.window.showStartupWindow)
					{
						display.window.handleWindowLostFocus(state.running, " ");
					}
					if(display.window.showTextEntryWindow)
					{
						display.window.handleWindowLostFocus(state.running, "  ");
					}
					if(display.window.showListItemWindow)
					{
						display.window.handleWindowLostFocus(state.running, "   ");
					}
				}
			}
		}

		public void windowLostFocus(WindowEvent e) {
			systemMessage("Main Window windowLostFocus...");
			mainWindowLostFocus = true;
		}

		public void windowDestroyNotify(WindowEvent e) 
		{
			systemMessage("Main Window windowDestroyNotify...");
		}

		public void windowRepaint(WindowUpdateEvent e) 
		{
			systemMessage("Main Window windowRepaint...");
		}

		public void windowResized(WindowEvent e) 
		{
			systemMessage("Main Window windowRepaint...");
		}

		public void windowMoved(WindowEvent e) 
		{
			systemMessage("Main Window windowRepaint...");
		}

		public void windowDestroyed(WindowEvent e) 
		{
			systemMessage("Main Window windowDestroyed...");
        }
	}

	/**
	 * Event listener for detecting when secondary windows gain / lose focus
	 * @author davidgordon
	 */
	private class WMV_EventListener implements AWTEventListener
	{
		/**
		 * Called when a window event is dispatched
		 */
		public void eventDispatched(AWTEvent event) 
		{
			if (event.getSource().getClass().toString().equals("class processing.awt.PSurfaceAWT$SmoothCanvas"))
			{
				PSurfaceAWT.SmoothCanvas pSurface = (PSurfaceAWT.SmoothCanvas)event.getSource();
				Frame nativeFrame = pSurface.getFrame();
				//System.out.println(" PSurfaceAWT.SmoothCanvas Event title:"+nativeFrame.getTitle()+" id:"+event.getID());
				
				if(event.getID() == FocusEvent.FOCUS_LOST)
				{
					String windowTitle = nativeFrame.getTitle();
					
					if(!display.disableLostFocusHook)
						display.window.handleWindowLostFocus(state.running, windowTitle);
				}
				else if(event.getID() == FocusEvent.FOCUS_GAINED)
				{
					String windowTitle = nativeFrame.getTitle();
					display.window.handleWindowGainedFocus(state.running, windowTitle);
				}
//				else if(event.getID() == java.awt.event.WindowEvent.WINDOW_DEACTIVATED)
//				{
//					String windowTitle = nativeFrame.getTitle();
//					display.window.handleWindowDeactivated(state.running, windowTitle);
//				}
//				else if(event.getID() == java.awt.event.WindowEvent.WINDOW_ACTIVATED)
//				{
//					String windowTitle = nativeFrame.getTitle();
//					display.window.handleWindowActivated(state.running, windowTitle);
//				}
				else
				{
//					String windowTitle = nativeFrame.getTitle();
//					System.out.println("Unknown Window Event... windowTitle:"+windowTitle+" event.getID():"+event.getID()+ " ... " + event.toString());
				}
			}
			
//			System.out.print(MouseInfo.getPointerInfo().getLocation() + " | ");
//			System.out.println(">>> event:"+event);
//			System.out.println(">> source:"+event.getSource()+" type:"+event.getSource().getClass());
//
//			if (event.getSource().getClass().toString().equals("class javax.swing.JFrame"))
//			{
//				JFrame jFrame;
//				jFrame = (JFrame)event.getSource();
////				if(jFrame.getName().equals("frame0"))
//				{
//					if(event.getID() == FocusEvent.FOCUS_LOST)
//					{
//						systemMessage("WMV_EventListener.eventDispatched()... "+jFrame.getName()+"  lost focus...");
//					}
//					else if(event.getID() == FocusEvent.FOCUS_GAINED)
//					{
//						systemMessage("WMV_EventListener.eventDispatched()... "+jFrame.getName()+"  gained focus...");
//					}
//					else if(event.getID() == java.awt.event.WindowEvent.WINDOW_GAINED_FOCUS)
//					{
//						systemMessage("WMV_EventListener.eventDispatched()... "+jFrame.getName()+"  window gained focus...");
//					}
//					else if(event.getID() == java.awt.event.WindowEvent.WINDOW_LOST_FOCUS)
//					{
//						systemMessage("WMV_EventListener.eventDispatched()... "+jFrame.getName()+"  lost focus...");
//					}
//					else if(event.getID() == java.awt.event.WindowEvent.WINDOW_ACTIVATED)
//					{
//						systemMessage("WMV_EventListener.eventDispatched()... "+jFrame.getName()+"  activated...");
//					}
//					else if(event.getID() == java.awt.event.WindowEvent.WINDOW_DEACTIVATED)
//					{
//						systemMessage("WMV_EventListener.eventDispatched()... "+jFrame.getName()+"  deactivated...");
//					}
//					else if(event.getID() == sun.awt.TimedWindowEvent.WINDOW_GAINED_FOCUS)
//					{
//						systemMessage("WMV_EventListener.eventDispatched()... "+jFrame.getName()+"  timed window gained focus...");
//					}
//					else if(event.getID() == sun.awt.TimedWindowEvent.WINDOW_LOST_FOCUS)
//					{
//						systemMessage("WMV_EventListener.eventDispatched()... "+jFrame.getName()+"  timed window lost focus...");
//					}
//					else
//					{
//						systemMessage(">>>>>>>> WMV_EventListener.eventDispatched()... "+jFrame.getName()+"  changed... event.getID():"+event.getID()+" focus lost == "+FocusEvent.FOCUS_LOST);
//						systemMessage("     event class name :"+event.getClass().getName()+"..."+event.getClass().toString());
//					}
//				}
//			}
		}
	}

	/**
	 * Open folder selection dialog
	 * @param title Dialog title 			-- Invisible
	 * @param fileExtension Files with this extension will be visible; if "" or null, all files are visible
	 */
	public void selectFileDialog(String title, String fileExtension, int resultCode)
	{
		final String property = System.getProperty("apple.awt.fileDialogForDirectories");
		System.setProperty("apple.awt.fileDialogForDirectories", "true");
		
		try{
			String strAllowed = "";
			if(fileExtension != null)
				if(!fileExtension.equals(""))
					strAllowed = "*."+fileExtension;

			FileDialog fd = new FileDialog(this.frame, title, FileDialog.LOAD);
			fd.setDirectory("~/");
			
			if(!strAllowed.equals(""))
				fd.setFile(strAllowed);			
			fd.setVisible(true);

			String fileName = fd.getFile();
			
			if (fileName == null)
			{
				if(debug.library) System.out.println("ML.selectFolderDialog()... User cancelled dialog...");
			}
			else
			{
				String filePath = fd.getDirectory() + fileName;
				if(debug.library) System.out.println("ML.selectFolderDialog()... User chose filePath: " + filePath);

				File selectedFile = new File(filePath);
				
				switch(resultCode)
				{
				case 0:	
					newLibraryDestinationSelected(selectedFile);
					break;
				}
			}
		}
		finally {
	        if (property != null) {
	            System.setProperty("apple.awt.fileDialogForDirectories", property);
	        }
	    }
	}
	
	/**
	 * Load native library from java.library.path
	 * @param filename Native library name
	 */
	private static void loadNativeLibrary(String filename)
	{
		try{
			System.loadLibrary(filename);					// Load the library
		}
		catch(Throwable t)
		{
			startupMessages.add(" ML.loadNativeLibrary()... Error while loading library: "+filename+" t:"+t);
			System.out.println(" ML.loadNativeLibrary()... Error while loading library: "+filename+" t:"+t);
		}
	}

	/**
	 * Load native library from absolute filepath
	 * @param filepath Absolute filepath
	 */
	private static void loadNativeLibraryFromPath(String filepath)
	{
		try{
			if (!filepath.startsWith("/")) {
				throw new IllegalArgumentException("ML.loadLibrary()... The path has to start with '/'.");
			}

			startupMessages.add(" ML.loadNativeLibraryFromPath()...  Will load library from path: "+filepath+"...");
			System.out.print(" ML.loadNativeLibraryFromPath()...  Will load library from path: "+filepath+"...");

			System.load(filepath);					// Load the library
			startupMessages.add(" ML.loadNativeLibraryFromPath()...  Loaded library from path: "+filepath+" successfully...");
			System.out.println(" ML.loadNativeLibraryFromPath()...  Loaded library from path: "+filepath+" successfully...");
		}
		catch(Throwable t)
		{
			startupMessages.add(" ML.loadNativeLibraryFromPath()... Error while loading library from path: "+filepath+" t:"+t);
			System.out.println(" ML.loadNativeLibraryFromPath()... Error while loading library from path: "+filepath+" t:"+t);
		}
	}

	/**
	 * Gets the base location of the given class.
	 * If the class is directly on the file system (e.g.,
	 * "/path/to/my/package/MyClass.class") then it will return the base directory
	 * (e.g., "file:/path/to").
	 * 
	 * If the class is within a JAR file (e.g.,
	 * "/path/to/my-jar.jar!/my/package/MyClass.class") then it will return the
	 * path to the JAR (e.g., "file:/path/to/my-jar.jar").
	 *
	 * @param c The class whose location is desired.
	 * @see FileUtils#urlToFile(URL) to convert the result to a {@link File}.
	 */
	public static URL getAppLocation() 
	{
		Class<MetaVisualizer> c = MetaVisualizer.class;
//		if (c == null) return null; // could not load the class

	    // try the easy way first
	    try {
	        final URL codeSourceLocation =
	            c.getProtectionDomain().getCodeSource().getLocation();
			startupMessages.add("ML.getAppLocation()... Found codeSourceLocation: "+codeSourceLocation.toURI().toString()+" and will convert to URL...");
			System.out.println("ML.getAppLocation()... Found codeSourceLocation: "+codeSourceLocation.toURI().toString()+" and will convert to URL...");
			if (codeSourceLocation != null) return codeSourceLocation;
	    }
	    catch (final SecurityException e) {
	    	startupMessages.add("ML.getAppLocation()... SecurityException: e:"+e);
	    	System.out.println("ML.getAppLocation()... SecurityException: e:"+e);
	    	// NB: Cannot access protection domain.
	    }
	    catch (final NullPointerException e) {
	    	startupMessages.add("ML.getAppLocation()... NullPointerException: e:"+e);
	    	System.out.println("ML.getAppLocation()... NullPointerException: e:"+e);
	    	// NB: Protection domain or code source is null.
	    }
	    catch (final Throwable t) {
	    	startupMessages.add("ML.getAppLocation()... ERROR: t:"+t);
	    	System.out.println("ML.getAppLocation()... ERROR: t:"+t);
	    	t.printStackTrace();
	    	// NB: Protection domain or code source is null.
	    }

	    // NB: The easy way failed, so we try the hard way. We ask for the class
	    // itself as a resource, then strip the class's path from the URL string,
	    // leaving the base path.

	    // get the class's raw resource path
	    final URL classResource = c.getResource(c.getSimpleName() + ".class");
	    if (classResource == null) return null; // cannot find class resource

	    final String url = classResource.toString();
	    final String suffix = c.getCanonicalName().replace('.', '/') + ".class";
	    if (!url.endsWith(suffix)) return null; // weird URL

	    // strip the class's path from the URL string
	    final String base = url.substring(0, url.length() - suffix.length());

	    String path = base;

	    // remove the "jar:" prefix and "!/" suffix, if present
	    if (path.startsWith("jar:")) path = path.substring(4, path.length() - 2);

	    try {
	    	startupMessages.add("ML.getAppLocation()... Found path: "+path+" and will convert to URL...");
	    	System.out.println("ML.getAppLocation()... Found path: "+path+" and will convert to URL...");
	        return new URL(path);
	    }
	    catch (final MalformedURLException e) {
	    	startupMessages.add("ML.getAppLocation()... ERROR converting application path to URL... e:"+e);
	    	System.out.println("ML.getAppLocation()... ERROR converting application path to URL... e:"+e);
	        e.printStackTrace();
	        return null;
	    }
	} 
	
	/**
	* Adds specified path to the java library path
	* @param newPath Path to add
	* @throws Exception
	*/
	public static void addLibraryPath(String newPath) throws Exception
	{
	    final java.lang.reflect.Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
	    usrPathsField.setAccessible(true);
	 
	    final String[] paths = (String[])usrPathsField.get(null);	    // Get array of paths

	    for(String path : paths) 					// Check if the path to add is already present
	    {

	        if(path.equals(newPath)) {
	            return;
	        }
	    }
	 
	    startupMessages.add("ML.addLibraryPath()... will add newPath:"+newPath);
	    System.out.println("ML.addLibraryPath()... will add newPath:"+newPath);
	    
	    final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);	    // Add new path
	    newPaths[newPaths.length-1] = newPath;
	    usrPathsField.set(null, newPaths);
	    
	    int count = 0;
	    for(String path : paths) 
	    {
	    	startupMessages.add(" userPaths["+count+"], path: "+path.toString());
	    	System.out.println(" userPaths["+count+"], path: "+path.toString());
	    	count++;
	    }
	    
	    startupMessages.add("------");
	    startupMessages.add("");
	    System.out.println("------");
	    System.out.println("");
	}

	/* Disabled */
	
//	/**
//	 * Converts the given {@link URL} to its corresponding {@link File}.
//	 * <p>
//	 * This method is similar to calling {@code new File(url.toURI())} except that
//	 * it also handles "jar:file:" URLs, returning the path to the JAR file.
//	 * </p>
//	 * 
//	 * @param url The URL to convert.
//	 * @return A file path suitable for use with e.g. {@link FileInputStream}
//	 * @throws IllegalArgumentException if the URL does not correspond to a file.
//	 */
//	private static File urlToFile(final URL url) {
//	    return url == null ? null : urlToFile(url.toString());
//	}
//
//	/**
//	 * Converts the given URL string to its corresponding {@link File}.
//	 * 
//	 * @param url The URL to convert.
//	 * @return A file path suitable for use with e.g. {@link FileInputStream}
//	 * @throws IllegalArgumentException if the URL does not correspond to a file.
//	 */
//	private static File urlToFile(final String url) {
//	    String path = url;
//	    if (path.startsWith("jar:")) {
//	        // remove "jar:" prefix and "!/" suffix
//	        final int index = path.indexOf("!/");
//	        path = path.substring(4, index);
//	    }
//	    if (path.startsWith("file:")) {
//	        // pass through the URL as-is, minus "file:" prefix
//	        path = path.substring(5);
//	        return new File(path);
//	    }
//	    throw new IllegalArgumentException("Invalid URL: " + url);
//	}
//	
//	/**
//	 * Copy library from JAR to java.library.path
//	 * 
//	 * The file from JAR is copied into system temporary directory and then loaded. The temporary file is deleted after exiting.
//	 * Method uses String as filename because the pathname is "abstract", not system-dependent.
//	 * 
//	 * @param path The path of file inside JAR as absolute path (beginning with '/'), e.g. /package/File.ext
//	 * @throws IOException If temporary file creation or read/write operation fails
//	 * @throws IllegalArgumentException If source file (param path) does not exist
//	 * @throws IllegalArgumentException If the path is not absolute or if the filename is shorter than three characters (restriction of {@see File#createTempFile(java.lang.String, java.lang.String)}).
//	 */
//	public static void loadLibraryFromJar(String path)
//	{
//		try{
//			if (!path.startsWith("/")) {
//				throw new IllegalArgumentException("The path has to be absolute (start with '/').");
//			}
//
//			// Obtain filename from path
//			String[] parts = path.split("/");
//			String filename = (parts.length > 1) ? parts[parts.length - 1] : null;
//
//			// Split filename to prefix and extension
//			String prefix = "";
//			String suffix = null;
//			if (filename != null) {
//				parts = filename.split("\\.", 2);
//				prefix = parts[0];
//				suffix = (parts.length > 1) ? "."+parts[parts.length - 1] : null; 
//			}
//
//			// Check if the filename is okay
//			if (filename == null || prefix.length() < 3) {
//				throw new IllegalArgumentException("The filename has to be at least 3 characters long.");
//			}
//
//			// Prepare temporary file
//			File temp = File.createTempFile(prefix, suffix);
//			temp.deleteOnExit();
//
//			if (!temp.exists()) {
//				throw new FileNotFoundException("File " + temp.getAbsolutePath() + " does not exist.");
//			}
//
//			// Prepare buffer for data copying
//			byte[] buffer = new byte[1024];
//			int readBytes;
//
//			// Open and check input stream
//			InputStream is = MultimediaLocator.class.getResourceAsStream(path);
//			if (is == null) {
//				throw new FileNotFoundException("File " + path + " was not found inside JAR.");
//			}
//
//			// Open output stream and copy data between source file in JAR and the temporary file
//			OutputStream os = new FileOutputStream(temp);
//			try {
//				while ((readBytes = is.read(buffer)) != -1) {
//					os.write(buffer, 0, readBytes);
//				}
//			} finally {
//				// If read/write fails, close streams safely before throwing an exception
//				os.close();
//				is.close();
//			}
//
//			// Finally, load the library
//			System.load(temp.getAbsolutePath());
//			
//			System.out.println(">>> Loaded library: "+path+" successfully...");
//		}
//		catch(Throwable t)
//		{
//			System.out.println("Error while loading library: "+path+" t:"+t);
//		}
//	}
//
//	/**
//	 * Print library path (for debugging)
//	 */
//	private void printLibraryPath()
//	{
//		if(debug.dependencies) 
//			systemMessage("ML.printLibraryPath()... Java Library Path:");
//		
//		String property = System.getProperty("java.library.path");
//		StringTokenizer parser = new StringTokenizer(property, ";");
//		while (parser.hasMoreTokens()) {
//			if(debug.dependencies) systemMessage(parser.nextToken());
//		}
//		
////		try{
////			if(debug.dependencies) 
////			{
////				systemMessage("Ex. 1: file path: "+gStreamerPluginDirectory + "libgstwavenc.so");
////				File testFile = new File(gStreamerPluginDirectory + "libgstwavenc.so");
////				if(testFile.exists())
////					systemMessage(" EXISTS");
////				else
////					systemMessage(" DOES NOT EXIST!");
////			}
////			if(debug.dependencies)
////			{
////				systemMessage("Ex. 2: file path: "+gStreamerPluginDirectory + "libgsty4menc.so");
////				File testFile = new File(gStreamerPluginDirectory + "libgstwavenc.so");
////				if(testFile.exists())
////					systemMessage(" EXISTS");
////				else
////					systemMessage(" DOES NOT EXIST!");
////			}
////			if(debug.dependencies) systemMessage("------");
////		}
////		catch(Throwable t)
////		{
////			if(debug.dependencies) systemMessage("ML.printLibraryPath()... ERROR: t:"+t);
////		}
////
////		if(debug.dependencies) 
////		{
////			systemMessage("ML.printLibraryPath()... appPath: "+appPath);
////			systemMessage("ML.printLibraryPath()... appDirectory: "+appDirectory);
////			systemMessage("ML.printLibraryPath()... gStreamerDirectory: "+gStreamerDirectory);
////			systemMessage("ML.printLibraryPath()... gStreamerPluginDirectory: "+gStreamerPluginDirectory);
////			systemMessage("");
////			systemMessage("ML.printLibraryPath()... sketchPath(): "+sketchPath(""));
////			systemMessage("ML.printLibraryPath()... sketchPath(/lib): "+sketchPath("/lib"));
////			systemMessage("ML.printLibraryPath()... sketchPath(../lib): "+sketchPath("../lib"));
////			systemMessage("");
////			systemMessage("ML.printLibraryPath()... File.sketchPath().absPath(): "+ (new File(sketchPath("")).getAbsolutePath() ) );
////			systemMessage("ML.printLibraryPath()... File.sketchPath(/lib).absPath(): "+ (new File(sketchPath("/lib")).getAbsolutePath() ) );
////			systemMessage("ML.printLibraryPath()... File.sketchPath(../lib).absPath(): "+ (new File(sketchPath("../lib")).getAbsolutePath() ) );
////			systemMessage("");
////			systemMessage("");
////
////			File appPathFile = new File(appPath);
////			systemMessage("Testing appPath: "+appPath);
////			if(appPathFile.exists()) systemMessage(" EXISTS");
////			else systemMessage(" DOES NOT EXIST!");
////
////			File appDirectoryFile = new File(appDirectory);
////			systemMessage("Testing appDirectory: "+appDirectory);
////			if(appDirectoryFile.exists()) systemMessage(" EXISTS");
////			else systemMessage(" DOES NOT EXIST!");
////
////			File gStreamerDirectoryFile = new File(gStreamerDirectory);
////			systemMessage("Testing gStreamerDirectory: "+gStreamerDirectory);
////			if(gStreamerDirectoryFile.exists()) systemMessage(" EXISTS");
////			else systemMessage(" DOES NOT EXIST!");
////
////			File gStreamerPluginDirectoryFile = new File(gStreamerPluginDirectory);
////			systemMessage("Testing gStreamerPluginDirectory: "+gStreamerPluginDirectory);
////			if(gStreamerPluginDirectoryFile.exists()) systemMessage(" EXISTS");
////			else systemMessage(" DOES NOT EXIST!");
////		}
//	}

	/**
	 * Start interactive clustering mode
	 */
	public void startInteractiveClustering()
	{
		background(0.f);						// Clear screen
		
		state.running = false;					// Stop running simulation
		state.interactive = true;				// Start interactive clustering mode
		state.startInteractive = false;			// Have started
		
		display.map2D.initialize(world);
		display.resetDisplayModes();			// Reset display view and clear messages
		display.displayClusteringInfo(this);
		
		world.getCurrentField().blackoutAllMedia();	// Blackout all media
	}

	public void display360()
	{
		/* Start cubemap */
		PGL pgl = beginPGL();
		pgl.activeTexture(PGL.TEXTURE1);
		pgl.enable(PGL.TEXTURE_CUBE_MAP);  
		pgl.bindTexture(PGL.TEXTURE_CUBE_MAP, envMapTextureID.get(0)); 

		regenerateEnvironmentMap(pgl);

		endPGL();
		drawDomeMaster();
		pgl.bindTexture(PGL.TEXTURE_CUBE_MAP, 0);
	}
	
	void drawDomeMaster() {
		PVector cLoc = world.viewer.getLocation();
//		camera(cLoc.x, cLoc.y, cLoc.z, width/2.0f, height/2.0f, 0, 0, 1, 0);
		camera();
		ortho();
		resetMatrix();
		shader(cubemapShader);
		shape(domeSphere);
		resetShader();
	}
	
	public void regenerateEnvironmentMap(PGL pgl)
	{
		// Bind FBO
		pgl.bindFramebuffer(PGL.FRAMEBUFFER, fbo.get(0));

		// Generate six views from camera location
		pgl.viewport(0, 0, cubeMapSize, cubeMapSize);    
		perspective(90.0f * PApplet.DEG_TO_RAD, 1.0f, 1.0f, world.viewer.getFarViewingDistance());
		
//		for ( int face = PGL.TEXTURE_CUBE_MAP_POSITIVE_X; 
//				  face < PGL.TEXTURE_CUBE_MAP_NEGATIVE_Z; face++ ) 
		for ( int face = PGL.TEXTURE_CUBE_MAP_POSITIVE_X; 
				  face <= PGL.TEXTURE_CUBE_MAP_NEGATIVE_Z; face++ ) 
		{
			resetMatrix();

//			/* Facing Up Flipped X / Y Up Params */
//		    if (face == PGL.TEXTURE_CUBE_MAP_POSITIVE_X) {
//		        camera(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f);
//		      } else if (face == PGL.TEXTURE_CUBE_MAP_NEGATIVE_X) {
//		        camera(0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f);
//		      } else if (face == PGL.TEXTURE_CUBE_MAP_POSITIVE_Y) {
//		        camera(0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, -1.0f, 0.0f, 0.0f);  
//		      } else if (face == PGL.TEXTURE_CUBE_MAP_NEGATIVE_Y) {
//		        camera(0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f);
//		      } else if (face == PGL.TEXTURE_CUBE_MAP_POSITIVE_Z) {
//		        camera(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f);    
//		      } else if (face == PGL.TEXTURE_CUBE_MAP_NEGATIVE_Z) {
//		        camera(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f);
//		      }

//			/* Facing Foward (Ground Visible) (Normal w/ Flipped X and Z Up Params) */
//		    if (face == PGL.TEXTURE_CUBE_MAP_POSITIVE_X) {
//		        camera(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f);
//		      } else if (face == PGL.TEXTURE_CUBE_MAP_NEGATIVE_X) {
//		        camera(0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f);
//		      } else if (face == PGL.TEXTURE_CUBE_MAP_POSITIVE_Y) {
//		        camera(0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, -1.0f, 0.0f, 0.0f);  
//		      } else if (face == PGL.TEXTURE_CUBE_MAP_NEGATIVE_Y) {
//		        camera(0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f);
//		      } else if (face == PGL.TEXTURE_CUBE_MAP_POSITIVE_Z) {
//		        camera(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f);    
//		      } else if (face == PGL.TEXTURE_CUBE_MAP_NEGATIVE_Z) {
//		        camera(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f);
//		      }

//			/* Facing Up (Normal w/ Flipped Y and Z Up Params) */
//		    if (face == PGL.TEXTURE_CUBE_MAP_POSITIVE_X) {
//		        camera(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f);
//		      } else if (face == PGL.TEXTURE_CUBE_MAP_NEGATIVE_X) {
//		        camera(0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f);
//		      } else if (face == PGL.TEXTURE_CUBE_MAP_POSITIVE_Y) {
//		        camera(0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f);  
//		      } else if (face == PGL.TEXTURE_CUBE_MAP_NEGATIVE_Y) {
//		        camera(0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f);
//		      } else if (face == PGL.TEXTURE_CUBE_MAP_POSITIVE_Z) {
//		        camera(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f);    
//		      } else if (face == PGL.TEXTURE_CUBE_MAP_NEGATIVE_Z) {
//		        camera(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f);
//		      }
		    
			/* NORMAL Facing Forward (Ground Invisible?) */
		    if (face == PGL.TEXTURE_CUBE_MAP_POSITIVE_X) {
		        camera(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f);
		      } else if (face == PGL.TEXTURE_CUBE_MAP_NEGATIVE_X) {
		        camera(0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f);
		      } else if (face == PGL.TEXTURE_CUBE_MAP_POSITIVE_Y) {
		        camera(0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f);  
		      } else if (face == PGL.TEXTURE_CUBE_MAP_NEGATIVE_Y) {
		        camera(0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f);
		      } else if (face == PGL.TEXTURE_CUBE_MAP_POSITIVE_Z) {
		        camera(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f);    
		      } else if (face == PGL.TEXTURE_CUBE_MAP_NEGATIVE_Z) {
		        camera(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f);
		      }

//			if (face == PGL.TEXTURE_CUBE_MAP_POSITIVE_X) {
//				camera(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f);
//			} else if (face == PGL.TEXTURE_CUBE_MAP_NEGATIVE_X) {
//				camera(0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f);
//			} else if (face == PGL.TEXTURE_CUBE_MAP_POSITIVE_Y) {
//				camera(0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f);  
//			} else if (face == PGL.TEXTURE_CUBE_MAP_NEGATIVE_Y) {
//				camera(0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f);
//			} else if (face == PGL.TEXTURE_CUBE_MAP_POSITIVE_Z) {
//				camera(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f);    
//			}

			scale(-1.f, 1.f, -1.f);
//			translate(-width * 0.5f, -height * 0.5f, -500.f);

			pgl.framebufferTexture2D(PGL.FRAMEBUFFER, PGL.COLOR_ATTACHMENT0, face, envMapTextureID.get(0), 0);
			
			world.display3D();			// 3D Display
			world.display2D();			// 2D Display
			
			flush(); 				// Make sure that the geometry in the scene is pushed to the GPU    
			noLights();  			// Disabling lights to avoid adding many times
			pgl.framebufferTexture2D(PGL.FRAMEBUFFER, PGL.COLOR_ATTACHMENT0, face, 0, 0);

			if(state.exportCubeMap && world.outputFolderSelected)
				exportCubeMapFace(face, pgl);
		}
	}

	/**
	 * Save screen image or export selected media		-- In progress
	 */
	public void exportCubeMapFace(int faceID, PGL pgl)				// Starts at 34069
	{
//		System.out.println("exportCubeMap()... faceID:"+faceID);	
		
//		int idx = faceID-34068;
	    ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * Integer.SIZE / 8);

	    pgl.readPixels(0, 0, width, height, PGL.RGBA, PGL.UNSIGNED_BYTE, buffer); 

//	    ByteBuffer byteBuffer = buffer;
	    buffer.rewind();
	    Byte[] buffer1 = new Byte[buffer.capacity()];
	    int n = 0;
	    while (n < buffer.capacity()) {
	      buffer1[n] = buffer.get(n + 3);
	      buffer1[n + 1] = buffer.get(n);
	      buffer1[n + 2] = buffer.get(n + 1);
	      buffer1[n + 3] = buffer.get(n + 2);
	      n += 4;
	    }
	    buffer.rewind();
	    
//	    Image awtImage = new javax.swing.ImageIcon(buffer.array()).getImage();
	    byte[] buf = new byte[buffer.remaining()];
//	    buffer.get(b);
	    
	    Image awtImage = new javax.swing.ImageIcon(buf).getImage();
//	    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
//	    img.getRaster().setDataElements(0, 0, width, height, buffer);
//	    img = new BufferedImage(buffer);

	    PImage image;
	    
	    if (awtImage instanceof BufferedImage) {
	    	BufferedImage buffImage = (BufferedImage) awtImage;
    		System.out.println("Image is BufferedImage. buf.length:"+buf.length+" buffer1.len:"+buffer1.length);
	    	int space = buffImage.getColorModel().getColorSpace().getType();
	    	if (space == ColorSpace.TYPE_CMYK) {
	    		System.out.println("Image is a CMYK image, only RGB images are supported.");
//	    		return null;
	    	}
	    	else if(space == ColorSpace.TYPE_RGB)
	    		System.out.println("Image is a RGB image... buf.length:"+buf.length);
	    	image = new PImage(awtImage);
	    }
	    else
	    {
    		System.out.println("Image is not a BufferedImage... buffer.capacity():"+buffer.capacity()+"...");
	    	BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
	        int[] arr = new int[buffer.asIntBuffer().limit()];
//	        int[] arr = new int[buffer1];
    		System.out.println("   ... arr.length:"+arr.length);
	    	img.setRGB(0, 0, width, height, arr, 0, width);
	    	image = stitcher.bufferedImageToPImage(img);
	    }

//	    faces[idx] = new PImage(awtImage);
	    if (image.width == -1) 
	    {
	    	System.err.println("The image contains bad image data, or may not be an image.");
	    }
	    else
	    {
	    	String titleStr = world.getCurrentField().getName() + "_face"+(faceID-34068)+"-######.jpg";
	    	String filePathStr = world.outputFolder + "/";
	    	String outputPath = filePathStr + titleStr;
	    	image.save(outputPath);
//	    	faces[idx] = stitcher.bufferedImageToPImage(image);
	    	System.out.println("Saved cube map image: " + world.getCurrentField().getName() + "_face"+(faceID-34068)+"-######.jpg");
	    }
		if(faceID >= 34074)									// Stop exporting after face 6
			state.exportCubeMap = false;
	}
	
	public void initCubeMap()
	{
		sphereDetail(50);
		domeSphere = createShape(PApplet.SPHERE, height/2.0f);
		domeSphere.rotateX(PApplet.HALF_PI);
		domeSphere.setStroke(false);

		PGL pgl = beginPGL();

		envMapTextureID = IntBuffer.allocate(1);
		pgl.genTextures(1, envMapTextureID);
		
		pgl.bindTexture(PGL.TEXTURE_CUBE_MAP, envMapTextureID.get(0));
		pgl.texParameteri(PGL.TEXTURE_CUBE_MAP, PGL.TEXTURE_WRAP_S, PGL.CLAMP_TO_EDGE);
		pgl.texParameteri(PGL.TEXTURE_CUBE_MAP, PGL.TEXTURE_WRAP_T, PGL.CLAMP_TO_EDGE);
		pgl.texParameteri(PGL.TEXTURE_CUBE_MAP, PGL.TEXTURE_WRAP_R, PGL.CLAMP_TO_EDGE);
		pgl.texParameteri(PGL.TEXTURE_CUBE_MAP, PGL.TEXTURE_MIN_FILTER, PGL.NEAREST);
		pgl.texParameteri(PGL.TEXTURE_CUBE_MAP, PGL.TEXTURE_MAG_FILTER, PGL.NEAREST);
		
		for (int i = PGL.TEXTURE_CUBE_MAP_POSITIVE_X; i < PGL.TEXTURE_CUBE_MAP_POSITIVE_X + 6; i++) {
			pgl.texImage2D(i, 0, PGL.RGBA8, cubeMapSize, cubeMapSize, 0, PGL.RGBA, PGL.UNSIGNED_BYTE, null);
		}

		fbo = IntBuffer.allocate(1);
		rbo = IntBuffer.allocate(1);
		pgl.genFramebuffers(1, fbo);
		pgl.bindFramebuffer(PGL.FRAMEBUFFER, fbo.get(0));
		pgl.framebufferTexture2D(PGL.FRAMEBUFFER, PGL.COLOR_ATTACHMENT0, PGL.TEXTURE_CUBE_MAP_POSITIVE_X, envMapTextureID.get(0), 0);

		pgl.genRenderbuffers(1, rbo);
		pgl.bindRenderbuffer(PGL.RENDERBUFFER, rbo.get(0));
		pgl.renderbufferStorage(PGL.RENDERBUFFER, PGL.DEPTH_COMPONENT24, cubeMapSize, cubeMapSize);

		// Attach depth buffer to FBO
		pgl.framebufferRenderbuffer(PGL.FRAMEBUFFER, PGL.DEPTH_ATTACHMENT, PGL.RENDERBUFFER, rbo.get(0));    

		endPGL();

		// Load cubemap shader
		if(createJar)
			loadCubeMapShader();			// From JAR file
		else
		{
			cubemapShader = loadShader("main/resources/shaders/cubemapfrag.glsl", "main/resources/shaders/cubemapvert.glsl");	// In Eclipse
//			cubemapShader = loadShader("resources/shaders/cubemapfrag.glsl", "resources/shaders/cubemapvert.glsl");	// In Eclipse
			cubemapShader.set("cubemap", 1);
		}
		cubeMapInitialized = true;
	}

	private void loadCubeMapShader()
	{
		String resourcePath = "/main/resources/shaders/";
		URL fsURL = MetaVisualizer.class.getResource(resourcePath + "cubemapfrag.glsl");
		URL vsURL = MetaVisualizer.class.getResource(resourcePath + "cubemapvert.glsl");
		cubemapShader = new PShader(this, fsURL, vsURL);
		cubemapShader.set("cubemap", 1);
	}

	/**
	 * Open select folder dialog
	 * @param title Dialog title 			-- Invisible
	 * @param fileExtension Files with this extension will be visible; if "" or null, all files are visible
	 */
	public void selectFolderDialog(String title, int resultCode)
	{
		final String property = System.getProperty("apple.awt.fileDialogForDirectories");
		System.setProperty("apple.awt.fileDialogForDirectories", "true");
		
		try{
//			String strAllowed = "";
//			if(fileExtension != null)
//				if(!fileExtension.equals(""))
//					strAllowed = "*."+fileExtension;

			FileDialog fd = new FileDialog(this.frame, title, FileDialog.LOAD);
			fd.setDirectory("C:\\");
//			if(!strAllowed.equals(""))
//				fd.setFile(strAllowed);			
			fd.setVisible(true);

			String fileName = fd.getFile();
			
			if (fileName == null)
			{
				if(debug.library) System.out.println("ML.selectFolderDialog()... User cancelled dialog...");
			}
			else
			{
				String filePath = fd.getDirectory() + fileName;
				if(debug.library) System.out.println("ML.selectFolderDialog()... User chose filePath: " + filePath);

				File selectedFile = new File(filePath);
				
				switch(resultCode)
				{
				case 0:	
					newLibraryDestinationSelected(selectedFile);
					break;
				}
			}
		}
		finally {
//	        if (property != null) {
//	            System.setProperty("apple.awt.fileDialogForDirectories", property);
//	        }
	    }
	}

	/**
	 * Tell program where to look for native libraries		-- Improve this
	 */
//	private static void setupLibraries()
//	{
//		try{
//			URL appURL = getAppLocation();
//			File appFile = urlToFile(appURL);

//			if(createJar)
//			{
//				appPath = appFile.getAbsolutePath();
//				appDirectory = appFile.getParentFile().getAbsolutePath();
//				gStreamerDirectory = "/Users/davidgordon/Documents/Processing/libraries/video/library/macosx64";					// GStreamer directory
//				gStreamerPluginDirectory = "/lib/macosx64/plugins/";		// GStreamer plugin directory
//			}
//			else
//			{
////				appPath = appFile.getAbsolutePath();
////				appDirectory = appFile.getParentFile().getAbsolutePath();
//				gStreamerDirectory = "/Users/davidgordon/Documents/Processing/libraries/video/library/macosx64";					// GStreamer directory
//				gStreamerPluginDirectory = appDirectory + "/lib/macosx64/plugins/";		// GStreamer plugin directory
//			}
//			appPath = appFile.getAbsolutePath();
//			appDirectory = appFile.getParentFile().getAbsolutePath();
//			gStreamerDirectory = appDirectory + "/lib/macosx64/";					// GStreamer directory
//			gStreamerPluginDirectory = appDirectory + "/lib/macosx64/plugins/";		// GStreamer plugin directory
//		}
//		catch (Throwable t)
//		{
//			startupMessages.add("ML.setupLibraries()... Error getting application path...");
//			System.out.println("ML.setupLibraries()... Error getting application path...");
//		}
//		
//		if(!appPath.equals(""))
//		{
//			try{
////				if (appPath.endsWith("/")) 
//				{
//					addLibraryPath("/Users/davidgordon/Documents/Processing/libraries/video/library/macosx64");
//					addLibraryPath("/Users/davidgordon/Documents/Processing/libraries/video/library/macosx64/plugins");
//					addLibraryPath("/Users/davidgordon/Documents/Processing/libraries/video/library");
//				}
//			}
//			catch(Throwable t)
//			{
//				startupMessages.add("ML.setupLibraries()... Error adding library paths...");
//				System.out.println("ML.setupLibraries()... Error adding library paths...");
//			}
//		}
//		else
//		{
//			startupMessages.add("ML.setupLibraries()... No app path!");
//			System.out.println("ML.setupLibraries()... No app path!");
//			return;
//		}
		
//		try{
//			loadNativeLibraryDependencies();
//		}
//		catch(Throwable t)
//		{
//			startupMessages.add("ML.setupLibraries()... Error loading libraries... t:"+t);
//			System.out.println("ML.setupLibraries()... Error loading libraries... t:"+t);
//			t.printStackTrace();
//		}
//	}

	/**
	 * Load GStreamer, Video and JNA native library dependencies
	 */
//	private static void loadNativeLibraryDependencies()		// Ex. "/opencv/mac/libopencv_java245.dylib"
//	{
//		loadNativeLibraryFromPath(gStreamerDirectory + "libcrypto.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libdc1394.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libdca.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libdv.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libffi.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libgio-2.0.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libglib-2.0.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libgmodule-2.0.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libgobject-2.0.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libgstapp-0.10.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libgstaudio-0.10.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libgstbase-0.10.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libgstbasecamerabinsrc-0.10.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libgstbasevideo-0.10.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libgstcodecparsers-0.10.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libgstcontroller-0.10.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libgstdataprotocol-0.10.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libgstfft-0.10.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libgstinterfaces-0.10.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libgstnetbuffer-0.10.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libgstpbutils-0.10.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libgstphotography-0.10.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libgstreamer-0.10.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libgstriff-0.10.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libgstrtp-0.10.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libgstrtsp-0.10.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libgstsdp-0.10.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libgsttag-0.10.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libgstvideo-0.10.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libgthread-2.0.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libiconv.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libintl.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libjasper.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libjpeg.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libmpcdec.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libncurses.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libogg.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "liborc-0.4.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "liborc-test-0.4.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libssl.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libtheoradec.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libtheoraenc.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libusb-1.0.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libvorbis.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libvorbisenc.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libxml2.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libxvidcore.dylib");
//		loadNativeLibraryFromPath(gStreamerDirectory + "libz.dylib");
//		
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstadder.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstadpcmdec.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstadpcmenc.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstaiff.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstalaw.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstalpha.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstalphacolor.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstannodex.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstapetag.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstapexsink.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstapp.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstapplemedia.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstasfmux.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstaudioconvert.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstaudiofx.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstaudioparsers.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstaudiorate.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstaudioresample.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstaudiotestsrc.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstauparse.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstautoconvert.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstautodetect.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstavi.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstbayer.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstbz2.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstcamerabin.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstcamerabin2.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstcdxaparse.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstcoloreffects.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstcolorspace.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstcoreelements.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstcoreindexers.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstcutter.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstdataurisrc.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstdc1394.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstdccp.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstdebug.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstdebugutilsbad.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstdecodebin.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstdecodebin2.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstdeinterlace.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstdtmf.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstdtsdec.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstdv.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstdvbsuboverlay.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstdvdspu.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstefence.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgsteffectv.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstencodebin.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstequalizer.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstfestival.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstffmpeg.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstffmpegcolorspace.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstffmpegscale.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstfieldanalysis.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstflv.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstflxdec.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstfragmented.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstfreeze.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstfrei0r.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstgaudieffects.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstgdp.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstgeometrictransform.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstgio.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstgoom.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstgoom2k1.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstgsettingselements.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgsth264parse.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgsthdvparse.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgsticydemux.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstid3demux.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstid3tag.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstimagefreeze.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstinterlace.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstinterleave.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstisomp4.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstivfparse.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstjp2k.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstjp2kdecimator.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstjpegformat.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstlegacyresample.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstlevel.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstliveadder.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstmatroska.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstmpegdemux.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstmpegpsmux.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstmpegtsdemux.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstmpegtsmux.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstmpegvideoparse.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstmulaw.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstmultifile.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstmultipart.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstmusepack.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstmve.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstmxf.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstnavigationtest.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstnsf.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstnuvdemux.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstogg.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstoss4audio.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstosxaudio.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstosxvideosink.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstpatchdetect.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstpcapparse.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstplaybin.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstpnm.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstpostproc.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstrawparse.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstreal.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstreplaygain.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstrfbsrc.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstrtp.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstrtpmanager.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstrtpmux.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstrtpvp8.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstrtsp.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstscaletempoplugin.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstsdi.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstsdpelem.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstsegmentclip.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstshapewipe.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstsiren.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstsmpte.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstspectrum.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstspeed.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgststereo.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstsubenc.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstsubparse.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgsttcp.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgsttheora.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgsttta.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgsttypefindfunctions.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstudp.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstvideobox.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstvideocrop.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstvideofilter.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstvideofiltersbad.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstvideomaxrate.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstvideomeasure.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstvideomixer.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstvideoparsersbad.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstvideorate.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstvideoscale.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstvideosignal.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstvideotestsrc.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstvmnc.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstvolume.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstvorbis.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstwavenc.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstwavparse.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgstxvid.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgsty4mdec.so");
//		loadNativeLibraryFromPath(gStreamerPluginDirectory + "libgsty4menc.so");
//	}

	/* Obsolete */
	/**
	 * A simple library class which helps with loading dynamic libraries stored in the
	 * JAR archive. These libraries usualy contain implementation of some methods in
	 * native code (using JNI - Java Native Interface).
	 * 
	 * @see http://adamheinrich.com/blog/2012/how-to-load-native-jni-library-from-jar
	 * @see https://github.com/adamheinrich/native-utils
	 *
	 */

	/**
	 * Loads library from current JAR archive
	 * 
	 * The file from JAR is copied into system temporary directory and then loaded. The temporary file is deleted after exiting.
	 * Method uses String as filename because the pathname is "abstract", not system-dependent.
	 * 
	 * @param path The path of file inside JAR as absolute path (beginning with '/'), e.g. /package/File.ext
	 * @throws IOException If temporary file creation or read/write operation fails
	 * @throws IllegalArgumentException If source file (param path) does not exist
	 * @throws IllegalArgumentException If the path is not absolute or if the filename is shorter than three characters (restriction of {@see File#createTempFile(java.lang.String, java.lang.String)}).
	 */
//	public static void loadLibrary(String path)
//	{
//		try{
//			if (!path.startsWith("/")) {
//				throw new IllegalArgumentException("The path has to be absolute (start with '/').");
//			}
//
//			// Obtain filename from path
//			String[] parts = path.split("/");
//			String filename = (parts.length > 1) ? parts[parts.length - 1] : null;
//
//			// Split filename to prefix and extension
//			String prefix = "";
//			String suffix = null;
//			if (filename != null) {
//				parts = filename.split("\\.", 2);
//				prefix = parts[0];
//				suffix = (parts.length > 1) ? "."+parts[parts.length - 1] : null; 
//			}
//
//			// Check if the filename is okay
//			if (filename == null || prefix.length() < 3) {
//				throw new IllegalArgumentException("The filename has to be at least 3 characters long.");
//			}
//
//			// Prepare temporary file
//			File temp = File.createTempFile(prefix, suffix);
//			temp.deleteOnExit();
//
//			if (!temp.exists()) {
//				throw new FileNotFoundException("File " + temp.getAbsolutePath() + " does not exist.");
//			}
//
//			// Prepare buffer for data copying
//			byte[] buffer = new byte[1024];
//			int readBytes;
//
//			// Open and check input stream
//			InputStream is = MultimediaLocator.class.getResourceAsStream(path);
//			if (is == null) {
//				throw new FileNotFoundException("File " + path + " was not found inside JAR.");
//			}
//
//			// Open output stream and copy data between source file in JAR and the temporary file
//			OutputStream os = new FileOutputStream(temp);
//			try {
//				while ((readBytes = is.read(buffer)) != -1) {
//					os.write(buffer, 0, readBytes);
//				}
//			} finally {
//				// If read/write fails, close streams safely before throwing an exception
//				os.close();
//				is.close();
//			}
//
//			// Finally, load the library
//			System.load(temp.getAbsolutePath());
//			
//			System.out.println(">>> Loaded library: "+path+" successfully...");
//		}
//		catch(Throwable t)
//		{
//			System.out.println("Error while loading library: "+path+" t:"+t);
//		}
//	}
	
	/**
	 * Load native library from resources
	 * @param libraryName Library name
	 */
//	private static void loadLibrary(String libraryName) 
//	{
//		try {
//			InputStream in = null;
//			File fileOut = null;
//			String osName = System.getProperty("os.name");
////			System.out.println("ML.loadLibrary()... osName:"+osName);
//			if(osName.equals("Mac OS X")){
//				in = MultimediaLocator.class.getResourceAsStream(libraryName);		// Ex. "/opencv/mac/libopencv_java245.dylib"
//				fileOut = File.createTempFile("lib", ".dylib");
//			}
//
//			OutputStream out = FileUtils.openOutputStream(fileOut);
//			IOUtils.copy(in, out);
//			in.close();
//			out.close();
//			System.load(fileOut.toString());										// Load the library
//		} 
//		catch (Exception e) {
////			throw new RuntimeException("Failed to load native library: "+libraryName+ "   e:"+e);
//			System.out.println("ML.loadLibrary()... Failed to load native library: "+libraryName+ "   e:"+e);
//		}
//	}
	
//	public static void loadOpenCV() {
//		try {
//			InputStream inputStream = null;
//			File fileOut = null;
//			String osName = System.getProperty("os.name");
//			System.out.println(osName);
//
//			if (osName.startsWith("Windows")) {
//				int bitness = Integer.parseInt(System.getProperty("sun.arch.data.model"));
//				if (bitness == 32) {
//					inputStream = MultimediaLocator.class.getResourceAsStream("/opencv/windows/x86/opencv_java300.dll");
//					fileOut = File.createTempFile("lib", ".dll");
//				} else if (bitness == 64) {
//					inputStream = MultimediaLocator.class.getResourceAsStream("/opencv/windows/x64/opencv_java300.dll");
//					fileOut = File.createTempFile("lib", ".dll");
//				} else {
//					inputStream = MultimediaLocator.class.getResourceAsStream("/opencv/windows/x86/opencv_java300.dll");
//					fileOut = File.createTempFile("lib", ".dll");
//				}
//			} else if (osName.equals("Mac OS X")) {
//				inputStream = MultimediaLocator.class.getResourceAsStream("/opencv/mac/libopencv_java300.dylib");
//				fileOut = File.createTempFile("lib", ".dylib");
//			}
//
//			if (fileOut != null) {
//				OutputStream outputStream = new FileOutputStream(fileOut);
//				byte[] buffer = new byte[1024];
//				int length;
//
//				while ((length = inputStream.read(buffer)) > 0) {
//					outputStream.write(buffer, 0, length);
//				}
//
//				inputStream.close();
//				outputStream.close();
//				System.load(fileOut.toString());
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
//	private void setAppTitle(String title) 
//	{
//		surface.setTitle(title);
//	}
	
	/* Obsolete */
//	
//	public void setSurfaceVisible(boolean newState)
//	{
//		surface.setVisible(newState);
//	}

//	public void setSurfaceLocation(int newX, int newY)
//	{
//		surface.setLocation(newX, newY);
//	}
	
	/**
	 * Load GStreamer, Video and JNA native libraries
	 */
//	private static void loadLibraries()		// Ex. "/opencv/mac/libopencv_java245.dylib"
//	{
//		loadNativeLibrary("libcrypto.dylib");
//		loadNativeLibrary("libdc1394.dylib");
//		loadNativeLibrary("libdca.dylib");
//		loadNativeLibrary("libdv.dylib");
//		loadNativeLibrary("libffi.dylib");
//		loadNativeLibrary("libgio-2.0.dylib");
//		loadNativeLibrary("libglib-2.0.dylib");
//		loadNativeLibrary("libgmodule-2.0.dylib");
//		loadNativeLibrary("libgobject-2.0.dylib");
//		loadNativeLibrary("libgstapp-0.10.dylib");
//		loadNativeLibrary("libgstaudio-0.10.dylib");
//		loadNativeLibrary("libgstbase-0.10.dylib");
//		loadNativeLibrary("libgstbasecamerabinsrc-0.10.dylib");
//		loadNativeLibrary("libgstbasevideo-0.10.dylib");
//		loadNativeLibrary("libgstcodecparsers-0.10.dylib");
//		loadNativeLibrary("libgstcontroller-0.10.dylib");
//		loadNativeLibrary("libgstdataprotocol-0.10.dylib");
//		loadNativeLibrary("libgstfft-0.10.dylib");
//		loadNativeLibrary("libgstinterfaces-0.10.dylib");
//		loadNativeLibrary("libgstnetbuffer-0.10.dylib");
//		loadNativeLibrary("libgstpbutils-0.10.dylib");
//		loadNativeLibrary("libgstphotography-0.10.dylib");
//		loadNativeLibrary("libgstreamer-0.10.dylib");
//		loadNativeLibrary("libgstriff-0.10.dylib");
//		loadNativeLibrary("libgstrtp-0.10.dylib");
//		loadNativeLibrary("libgstrtsp-0.10.dylib");
//		loadNativeLibrary("libgstsdp-0.10.dylib");
//		loadNativeLibrary("libgsttag-0.10.dylib");
//		loadNativeLibrary("libgstvideo-0.10.dylib");
//		loadNativeLibrary("libgthread-2.0.dylib");
//		loadNativeLibrary("libiconv.dylib");
//		loadNativeLibrary("libintl.dylib");
//		loadNativeLibrary("libjasper.dylib");
//		loadNativeLibrary("libjpeg.dylib");
//		loadNativeLibrary("libmpcdec.dylib");
//		loadNativeLibrary("libncurses.dylib");
//		loadNativeLibrary("libogg.dylib");
//		loadNativeLibrary("liborc-0.4.dylib");
//		loadNativeLibrary("liborc-test-0.4.dylib");
//		loadNativeLibrary("libssl.dylib");
//		loadNativeLibrary("libtheoradec.dylib");
//		loadNativeLibrary("libtheoraenc.dylib");
//		loadNativeLibrary("libusb-1.0.dylib");
//		loadNativeLibrary("libvorbis.dylib");
//		loadNativeLibrary("libvorbisenc.dylib");
//		loadNativeLibrary("libxml2.dylib");
//		loadNativeLibrary("libxvidcore.dylib");
//		loadNativeLibrary("libz.dylib");
//		
//		loadNativeLibrary("libgstadder.so");
//		loadNativeLibrary("libgstadpcmdec.so");
//		loadNativeLibrary("libgstadpcmenc.so");
//		loadNativeLibrary("libgstaiff.so");
//		loadNativeLibrary("libgstalaw.so");
//		loadNativeLibrary("libgstalpha.so");
//		loadNativeLibrary("libgstalphacolor.so");
//		loadNativeLibrary("libgstannodex.so");
//		loadNativeLibrary("libgstapetag.so");
//		loadNativeLibrary("libgstapexsink.so");
//		loadNativeLibrary("libgstapp.so");
//		loadNativeLibrary("libgstapplemedia.so");
//		loadNativeLibrary("libgstasfmux.so");
//		loadNativeLibrary("libgstaudioconvert.so");
//		loadNativeLibrary("libgstaudiofx.so");
//		loadNativeLibrary("libgstaudioparsers.so");
//		loadNativeLibrary("libgstaudiorate.so");
//		loadNativeLibrary("libgstaudioresample.so");
//		loadNativeLibrary("libgstaudiotestsrc.so");
//		loadNativeLibrary("libgstauparse.so");
//		loadNativeLibrary("libgstautoconvert.so");
//		loadNativeLibrary("libgstautodetect.so");
//		loadNativeLibrary("libgstavi.so");
//		loadNativeLibrary("libgstbayer.so");
//		loadNativeLibrary("libgstbz2.so");
//		loadNativeLibrary("libgstcamerabin.so");
//		loadNativeLibrary("libgstcamerabin2.so");
//		loadNativeLibrary("libgstcdxaparse.so");
//		loadNativeLibrary("libgstcoloreffects.so");
//		loadNativeLibrary("libgstcolorspace.so");
//		loadNativeLibrary("libgstcoreelements.so");
//		loadNativeLibrary("libgstcoreindexers.so");
//		loadNativeLibrary("libgstcutter.so");
//		loadNativeLibrary("libgstdataurisrc.so");
//		loadNativeLibrary("libgstdc1394.so");
//		loadNativeLibrary("libgstdccp.so");
//		loadNativeLibrary("libgstdebug.so");
//		loadNativeLibrary("libgstdebugutilsbad.so");
//		loadNativeLibrary("libgstdecodebin.so");
//		loadNativeLibrary("libgstdecodebin2.so");
//		loadNativeLibrary("libgstdeinterlace.so");
//		loadNativeLibrary("libgstdtmf.so");
//		loadNativeLibrary("libgstdtsdec.so");
//		loadNativeLibrary("libgstdv.so");
//		loadNativeLibrary("libgstdvbsuboverlay.so");
//		loadNativeLibrary("libgstdvdspu.so");
//		loadNativeLibrary("libgstefence.so");
//		loadNativeLibrary("libgsteffectv.so");
//		loadNativeLibrary("libgstencodebin.so");
//		loadNativeLibrary("libgstequalizer.so");
//		loadNativeLibrary("libgstfestival.so");
//		loadNativeLibrary("libgstffmpeg.so");
//		loadNativeLibrary("libgstffmpegcolorspace.so");
//		loadNativeLibrary("libgstffmpegscale.so");
//		loadNativeLibrary("libgstfieldanalysis.so");
//		loadNativeLibrary("libgstflv.so");
//		loadNativeLibrary("libgstflxdec.so");
//		loadNativeLibrary("libgstfragmented.so");
//		loadNativeLibrary("libgstfreeze.so");
//		loadNativeLibrary("libgstfrei0r.so");
//		loadNativeLibrary("libgstgaudieffects.so");
//		loadNativeLibrary("libgstgdp.so");
//		loadNativeLibrary("libgstgeometrictransform.so");
//		loadNativeLibrary("libgstgio.so");
//		loadNativeLibrary("libgstgoom.so");
//		loadNativeLibrary("libgstgoom2k1.so");
//		loadNativeLibrary("libgstgsettingselements.so");
//		loadNativeLibrary("libgsth264parse.so");
//		loadNativeLibrary("libgsthdvparse.so");
//		loadNativeLibrary("libgsticydemux.so");
//		loadNativeLibrary("libgstid3demux.so");
//		loadNativeLibrary("libgstid3tag.so");
//		loadNativeLibrary("libgstimagefreeze.so");
//		loadNativeLibrary("libgstinterlace.so");
//		loadNativeLibrary("libgstinterleave.so");
//		loadNativeLibrary("libgstisomp4.so");
//		loadNativeLibrary("libgstivfparse.so");
//		loadNativeLibrary("libgstjp2k.so");
//		loadNativeLibrary("libgstjp2kdecimator.so");
//		loadNativeLibrary("libgstjpegformat.so");
//		loadNativeLibrary("libgstlegacyresample.so");
//		loadNativeLibrary("libgstlevel.so");
//		loadNativeLibrary("libgstliveadder.so");
//		loadNativeLibrary("libgstmatroska.so");
//		loadNativeLibrary("libgstmpegdemux.so");
//		loadNativeLibrary("libgstmpegpsmux.so");
//		loadNativeLibrary("libgstmpegtsdemux.so");
//		loadNativeLibrary("libgstmpegtsmux.so");
//		loadNativeLibrary("libgstmpegvideoparse.so");
//		loadNativeLibrary("libgstmulaw.so");
//		loadNativeLibrary("libgstmultifile.so");
//		loadNativeLibrary("libgstmultipart.so");
//		loadNativeLibrary("libgstmusepack.so");
//		loadNativeLibrary("libgstmve.so");
//		loadNativeLibrary("libgstmxf.so");
//		loadNativeLibrary("libgstnavigationtest.so");
//		loadNativeLibrary("libgstnsf.so");
//		loadNativeLibrary("libgstnuvdemux.so");
//		loadNativeLibrary("libgstogg.so");
//		loadNativeLibrary("libgstoss4audio.so");
//		loadNativeLibrary("libgstosxaudio.so");
//		loadNativeLibrary("libgstosxvideosink.so");
//		loadNativeLibrary("libgstpatchdetect.so");
//		loadNativeLibrary("libgstpcapparse.so");
//		loadNativeLibrary("libgstplaybin.so");
//		loadNativeLibrary("libgstpnm.so");
//		loadNativeLibrary("libgstpostproc.so");
//		loadNativeLibrary("libgstrawparse.so");
//		loadNativeLibrary("libgstreal.so");
//		loadNativeLibrary("libgstreplaygain.so");
//		loadNativeLibrary("libgstrfbsrc.so");
//		loadNativeLibrary("libgstrtp.so");
//		loadNativeLibrary("libgstrtpmanager.so");
//		loadNativeLibrary("libgstrtpmux.so");
//		loadNativeLibrary("libgstrtpvp8.so");
//		loadNativeLibrary("libgstrtsp.so");
//		loadNativeLibrary("libgstscaletempoplugin.so");
//		loadNativeLibrary("libgstsdi.so");
//		loadNativeLibrary("libgstsdpelem.so");
//		loadNativeLibrary("libgstsegmentclip.so");
//		loadNativeLibrary("libgstshapewipe.so");
//		loadNativeLibrary("libgstsiren.so");
//		loadNativeLibrary("libgstsmpte.so");
//		loadNativeLibrary("libgstspectrum.so");
//		loadNativeLibrary("libgstspeed.so");
//		loadNativeLibrary("libgststereo.so");
//		loadNativeLibrary("libgstsubenc.so");
//		loadNativeLibrary("libgstsubparse.so");
//		loadNativeLibrary("libgsttcp.so");
//		loadNativeLibrary("libgsttheora.so");
//		loadNativeLibrary("libgsttta.so");
//		loadNativeLibrary("libgsttypefindfunctions.so");
//		loadNativeLibrary("libgstudp.so");
//		loadNativeLibrary("libgstvideobox.so");
//		loadNativeLibrary("libgstvideocrop.so");
//		loadNativeLibrary("libgstvideofilter.so");
//		loadNativeLibrary("libgstvideofiltersbad.so");
//		loadNativeLibrary("libgstvideomaxrate.so");
//		loadNativeLibrary("libgstvideomeasure.so");
//		loadNativeLibrary("libgstvideomixer.so");
//		loadNativeLibrary("libgstvideoparsersbad.so");
//		loadNativeLibrary("libgstvideorate.so");
//		loadNativeLibrary("libgstvideoscale.so");
//		loadNativeLibrary("libgstvideosignal.so");
//		loadNativeLibrary("libgstvideotestsrc.so");
//		loadNativeLibrary("libgstvmnc.so");
//		loadNativeLibrary("libgstvolume.so");
//		loadNativeLibrary("libgstvorbis.so");
//		loadNativeLibrary("libgstwavenc.so");
//		loadNativeLibrary("libgstwavparse.so");
//		loadNativeLibrary("libgstxvid.so");
//		loadNativeLibrary("libgsty4mdec.so");
//		loadNativeLibrary("libgsty4menc.so");
//	}
}