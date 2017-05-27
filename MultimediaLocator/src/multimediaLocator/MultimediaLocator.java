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
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import g4p_controls.GButton;
import g4p_controls.GEvent;
import g4p_controls.GPanel;
import g4p_controls.GToggleControl;
import g4p_controls.GValueControl;
import g4p_controls.GWinData;

import processing.core.*;
import processing.opengl.PGL;
import processing.opengl.PShader;
import processing.video.Movie;
import com.apple.eawt.Application;

/**
 * MultimediaLocator App  
 * @author davidgordon
 */
@SuppressWarnings("restriction")
public class MultimediaLocator extends PApplet 
{
	/* Deployment */
	private boolean createJar = true;
	
	/* General */
	private String programName = "MultimediaLocator 0.9.0";
	public int appWidth = 1680, appHeight = 960;		// App window dimensions
	private boolean windowVisible = false;
	private PImage appIcon;
	boolean setAppIcon = true;
	
	/* System Status */
	public ML_SystemState state = new ML_SystemState();
	boolean createNewLibrary = false;
	boolean enteredField = false;
	boolean cubeMapInitialized = false;
	
	/* MultimediaLocator */
	ML_Library library;							// Multimedia library
	ML_Input input;								// Mouse / keyboard input
	ML_Stitcher stitcher;						// Panoramic stitching
	ML_Display display;							// Displaying 2D graphics and text
	ML_DebugSettings debugSettings;				// Debug settings
	
	/* WorldMediaViewer */
	WMV_World world;							// The 3D World
	WMV_MetadataLoader metadata;				// Metadata reading and writing
	
	/* Field */
	List<Integer> removeList;
	
	/* Graphics */
	public PShader cubemapShader;
	public PShape domeSphere;
	public IntBuffer fbo;
	public IntBuffer rbo;
	public IntBuffer envMapTextureID;
	public PImage[] faces;
	public int cubeMapSize = 2048;   
	
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
		surface.setResizable(true);
		hideMainWindow();

		debugSettings = new ML_DebugSettings();		
		if(debugSettings.ml) System.out.println("Starting initial setup...");

		world = new WMV_World(this);
		world.initialize();
		appIcon = getImageResource("icon.png");
		
		input = new ML_Input(width, height);
		display = new ML_Display(this);			// Initialize displays
		display.initializeWindows(world);
		metadata = new WMV_MetadataLoader(this, debugSettings);
		stitcher = new ML_Stitcher(world);
		if(debugSettings.ml) System.out.println("Initial setup complete...");

		colorMode(PConstants.HSB);
		rectMode(PConstants.CENTER);
		textAlign(PConstants.CENTER, PConstants.CENTER);
		
		initCubeMap();
		removeList = new ArrayList<Integer>();
	}

	/**
	 * Get image from resources
	 * @param fileName File name
	 * @return Mask image
	 */
	public PImage getImageResource(String fileName)
	{
		String resourcePath = "/images/";
		BufferedImage image;
		
		URL imageURL = MultimediaLocator.class.getResource(resourcePath + fileName);
		try{
			image = ImageIO.read(imageURL.openStream());
			return world.utilities.bufferedImageToPImage(image);
		}
		catch(Throwable t)
		{
			System.out.println("ERROR in getImageResource... t:"+t+" imageURL == null? "+(imageURL == null));
		}
		
		return null;
	}

	/** 
	 * Main program loop
	 */
	public void draw() 
	{
		if(setAppIcon) setAppIcon(appIcon);						/* Set app icon */
		
		if (state.startup)
		{
			if(state.reset) restartMultimediaLocator();
			else display.display(world);						/* Startup screen */
			
			state.startup = false;	
		}
		else if(!state.running)
		{
			if (state.librarySetup)
			{
				if(createNewLibrary)
				{
					if(state.chooseLibraryDestination)			/* Choose library destination */
						libraryDestinationDialog();	
					display.display(world);
				}
				else
					librarySelectionDialog();
			}
			else
			{
//				System.out.println("state.selectedLibrary:"+state.selectedLibrary+" state.selectedLibraryDestination:"+state.selectedNewLibraryDestination+" state.selectedMediaFolder:"+state.selectedMediaFolder);
				if(state.selectedNewLibraryDestination && !state.selectedLibrary)
				{
					if(state.selectedMediaFolders)
						createNewLibraryFromMediaFolders();
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
//			if(!windowVisible)
//				showMainWindow();

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

//			if(state.exportCubeMap && world.outputFolderSelected)						/* Image exporting */
//				exportCubeMap();

			if ( debugSettings.memory && frameCount % world.getState().memoryCheckFrequency == 0 )		/* Memory debugging */
			{
				checkMemory();
				checkFrameRate();
			}
		}
		
		if ( state.exit ) exitProgram();							/* Stopping the program */		
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
	 * Initialize world and run clustering
	 */
	public void initialize()
	{
		if(state.initialSetup)
		{
			if(!windowVisible)
				showMainWindow();

			if( !state.fieldsInitialized )
			{
				if (!state.initializingFields) 
				{
					world.createFieldsFromFolders(library.getFolders());		// Create empty field for each field folder	
					state.initializingFields = true;
				}
				else
				{
					WMV_Field f = world.getField(state.initializationField);
					boolean loadedState = initializeField(f, true, true);				/* Initialize field */	
					
					/* Set next field to initialize */
					state.initializationField++;										
					if( state.initializationField >= world.getFields().size() )	
					{
						state.fieldsInitialized = true;
						if(debugSettings.ml) System.out.println("ML.initializeField()... " + world.getFields().size() + " fields initialized...");
						/* Enter last initialization field, move to first time segment if simulation state not loaded from disk */
						world.enter(state.initializationField-1, !loadedState);		
					}
				}
			}
			else
			{
				organizeMedia();
				finishInitialization();
			}
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
	public boolean initializeField(WMV_Field f, boolean loadState, boolean setSoundGPSLocations)
	{
		System.out.println("ML.initializeField()... state.fieldsInitialized:"+state.fieldsInitialized);
		
//		if(!state.fieldsInitialized && !state.exit)
		if(!state.exit)
		{
			/* Get field to initialize */
			boolean success = false;
			
			/* Attempt to load simulation state from data folder. If not successful, initialize field */
			if(loadState)
			{
				WMV_Field loadedField;
				if(f.getID() + 1 >= world.getFields().size())
					loadedField = loadField(f, library.getLibraryFolder(), true);	// Load field (load simulation state or, if fails, metadata), and set simulation state if exists
				else
					loadedField = loadField(f, library.getLibraryFolder(), false);	// Load field (load simulation state or, if fails, metadata)

				if(world.getFields().size() == 0)					// Reset current viewer field
					if(world.viewer.getState().field > 0)
						world.viewer.setCurrentField(0, false);

				/* Check if field loaded correctly */
				success = (loadedField != null);
				if(success) world.setField(loadedField, f.getID());
				if(success) success = world.getField(f.getID()).getClusters() != null;
				if(success) success = (world.getField(f.getID()).getClusters().size() > 0);
			}
			if(success)									/* Loaded field state from disk */
			{
				if(debugSettings.ml || debugSettings.world) System.out.println("ML.initializeField()... Succeeded at loading simulation state for Field #"+f.getID()+"... clusters:"+f.getClusters().size());
			}
			else										/* If failed to load field, initialize using metadata */
			{
				if(debugSettings.ml || debugSettings.world) System.out.println("ML.initializeField()... Failed at loading simulation state... Initializing field #"+f.getID());
				
				System.out.println("ML.initializeField()... will initialize field:"+f.getID()+" name:"+f.getName());
				f.initialize(-100000L);
				if(setSoundGPSLocations) metadata.setSoundGPSLocations(f, f.getSounds());
			}

			return success;
		}
		return false;
	}
	
	/**
	 * Load simulation state from disk
	 * @param f The field to initialize
	 * @param libraryFolder Library folder
	 * @return True if succeeded, false if failed
	 */
	private WMV_Field loadField(WMV_Field f, String libraryFolder, boolean set)
	{
		/* Load metadata from media associated with field */
		WMV_SimulationState savedState = metadata.load(f, libraryFolder);
		
		/* Attempt to load simulation state */
		if(savedState != null)
		{
			if(debugSettings.ml && debugSettings.detailed) System.out.println("Valid SimulationState loaded...");
			if(set)
				return world.loadAndSetSimulationState(savedState, f);
			else
				return world.loadSimulationState(savedState, f);
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
			if(world.getSettings().divideFields)
			{
				ArrayList<WMV_Field> addedFields = divideField(f);						/* Attempt to divide field */
				if(addedFields == null)					/* Check if field division succeeded */
					f.organize();						/* If failed, analyze spatial and temporal features to create model */
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
				f.organize();
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
		world.setBlurMasks();			// Set blur masks

		if(debugSettings.ml && debugSettings.detailed) System.out.println("Finishing MultimediaLocator initialization..");

//		display.initializeWindows(world);
		display.window.setupMLWindow();
		
		if(debugSettings.ml && debugSettings.detailed) System.out.println("Finished setting up WMV Window...");
		
		world.updateAllMediaSettings();					// -- Only needed if field(s) loaded from data folder!

		if(debugSettings.ml && debugSettings.detailed) System.out.println("Finished setting initial media settings...");

		state.initialSetup = false;				
		display.initialSetup = false;
		
		state.running = true;
		state.startedRunning = true;
		
//		PJOGL.setIcon("res/icon.png");
	}

	/**
	 * If large gaps are detected between media locations, divide target field into separate fields
	 * @param field Target field to divide
	 * @return Whether field was divided or not
	 */
	private ArrayList<WMV_Field> divideField(WMV_Field field)
	{
		ArrayList<WMV_Field> newFields = new ArrayList<WMV_Field>();
		newFields = field.divide(world, 3000.f, 15000.f);				// Attempt to divide field
		
		if(newFields.size() > 0)
		{
			int count = 0;
			
			for(WMV_Field f : newFields)
			{
				System.out.println("ML.divideField()... Will initialize field #"+f.getID()+" name:"+f.getName()+" of "+newFields.size()+"...");

				f.renumberMedia();							/* Renumber media in field from index 0 */
				if(count < newFields.size() - 1)
				{
					initializeField(f, false, false);			/* Initialize field */
					f.organize();								/* Analyze spatial and temporal features and create model */
					library.moveFieldMediaFiles(field, f);		/* Move media into new field folders */
				}
				else
				{
					System.out.println("ML.divideField()... Last of new fields from dividing field id #"+field.getID());
					field.reset();								/* Clear field */
					copyAllFieldMedia(f, field);					/* Re-add media from last added field */
					initializeField(field, false, false);		/* Initialize field */
					field.organize();
				}
				count++;
			}

			return newFields;
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
	 * Save screen image or export selected media		-- Not working?
	 */
	public void exportCubeMapFace(int faceID, PGL pgl)				// Starts at 34069
	{
		System.out.println("exportCubeMap()... faceID:"+faceID);	
		
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
	public void newLibraryDestinationSelected(File selection) 
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
			System.out.println("newLibraryDestinationSelected error... not a directory!");
		}
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
	public void openNewLibraryDestination(File selection) 
	{
		boolean selectedFolder = false;
		
		if (selection == null) {
			if (debugSettings.ml)
				System.out.println("openLibraryDestination()... Window was closed or the user hit cancel.");
		} 
		else 
		{
			String input = selection.getPath();
			String[] parts = input.split("/");

			if (debugSettings.ml)
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
				}
			}
		}
		
		if(selectedFolder)
		{
			state.selectedNewLibraryDestination = true;	// Library destination folder has been selected
			state.librarySetup = false;					// Library setup complete
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
		boolean selectedFolder = false;
		
//		if(!windowVisible)
//			showMainWindow();

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
					library.mediaFolders.add(input);
					selectedFolder = true;
				}
			}
		}
		
		if(selectedFolder)
		{
//			mediaFolderDialog();
//			state.selectedMediaFolders = true;			// Media folder has been selected
//			state.chooseMediaFolders = false;			// No longer choose a media folder
//			state.chooseLibraryDestination = true;		// Choose library destination folder
		}
		else
		{
//			state.selectedMediaFolders = false;			// Library in improper format if masks are missing
			mediaFolderDialog();						// Retry folder prompt
		}
	}
	
	/**
	 * Analyze and load media folders given user selection
	 * @param selection Selected folder
	 */
	public void openLibraryFolder(File selection) 
	{
		boolean selectedFolder = false;
		
//		if(!windowVisible)
//			showMainWindow();

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
			String[] nameParts = parts[parts.length-1].split("\\.");		// Check if single field library 

			boolean singleField;
			if(nameParts[nameParts.length-1].equals("mlibrary"))
				singleField = false;
			else
				singleField = true;
			
//			String[] nameParts = parts[parts.length-1].split("_");		// Check if single field library 
//			boolean singleField = !(nameParts[0].equals("ML") && nameParts[1].equals("Library"));
			
			String parentFilePath = "";
			if(singleField)
			{
				System.out.println("Loading (single) field folder...");
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
	 * Import media folders and create new library
	 */
	private void createNewLibraryFromMediaFolders()
	{
//		if(!windowVisible)
//			showMainWindow();
		
		if(library.mediaFolders.size() > 0)
		{
			if(debugSettings.ml) System.out.println("Will create new library at: "+library.getLibraryFolder()+" from "+library.mediaFolders.size()+" imported media folders...");
			state.selectedLibrary = library.createNewLibrary(this, library.mediaFolders);

			if(!state.selectedLibrary)
			{
				System.out.println("createNewLibraryFromMediaFolders()... Error importing media to create library...");
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
			if (debugSettings.ml)
				println("Window was closed or the user hit cancel.");
		} 
		else 
		{
			String input = selection.getPath();

			if (debugSettings.ml)
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

	/******* G4P *******/
	public void handlePanelEvents(GPanel panel, GEvent event)
	{
		display.handlePanelEvents(panel, event);
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
	public void handleToggleControlEvents(GToggleControl option, GEvent event) {
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
	 * Respond to key pressed in MultimediaLocator Window
	 * @param applet Parent App
	 * @param windata Window data
	 * @param keyevent Key event
	 */
	public void libraryWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			input.handleKeyPressed(this, keyevent.getKey(), keyevent.getKeyCode());
		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			input.handleKeyReleased(world.viewer, display, keyevent.getKey(), keyevent.getKeyCode());
	}
	
	/**
	 * Respond to key pressed in MultimediaLocator Window
	 * @param applet Parent App
	 * @param windata Window data
	 * @param keyevent Key event
	 */
	public void importWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
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
		selectFolder("Select media folder:", "mediaFolderSelected");		// Get filepath of PhotoSceneLibrary folder
	}
	
	public void libraryDestinationDialog()
	{
		state.chooseLibraryDestination = false;
		if(display.window.importWindow.isVisible())
			display.window.hideImportWindow();
		selectFolder("Select library destination:", "newLibraryDestinationSelected");		// Get filepath of PhotoSceneLibrary folder
	}
	
	public void librarySelectionDialog()
	{
		state.librarySetup = false;
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
			
			if(performanceSlow && debugSettings.memory)
				display.message(this, "Performance slow...");
		}
		else
		{
			if(performanceSlow)
				performanceSlow = false;
		}
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
			cubemapShader = loadShader("resources/shaders/cubemapfrag.glsl", "resources/shaders/cubemapvert.glsl");	// In Eclipse
			cubemapShader.set("cubemap", 1);
		}
		cubeMapInitialized = true;
	}

	private void loadCubeMapShader()
	{
		String resourcePath = "/shaders/";
		URL fsURL = MultimediaLocator.class.getResource(resourcePath + "cubemapfrag.glsl");
		URL vsURL = MultimediaLocator.class.getResource(resourcePath + "cubemapvert.glsl");
		cubemapShader = new PShader(this, fsURL, vsURL);
		cubemapShader.set("cubemap", 1);
	}
	
	private void hideMainWindow()
	{
		setSurfaceSize(1, 1);
		windowVisible = false;
	}
	
	private void showMainWindow()
	{
		System.out.println("showMainWindow()...");
		setSurfaceSize(appWidth, appHeight);
		windowVisible = true;
	}

	@SuppressWarnings("restriction")
	private void setAppIcon(PImage img) 
	{
		Application.getApplication().setDockIconImage(img.getImage());
		setAppIcon = false;
		if(debugSettings.ml && debugSettings.detailed) System.out.println("setAppIcon()... frameCount:"+frameCount);
	}

	public void setSurfaceSize(int newWidth, int newHeight)
	{
//		surface.setResizable(true);
		surface.setSize(newWidth, newHeight);
//		surface.setResizable(false);
	}

	/**
	 * Set window resolution and graphics mode
	 */
	public void settings() 
	{
		size(appWidth, appHeight, processing.core.PConstants.P3D);		// MacBook Pro
//		size(1680, 960, processing.core.PConstants.P3D);		// MacBook Pro
		
//		PJOGL.setIcon("resources/images/icon.png");			// -- Needed?
		
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
	
//	private void setAppTitle(String title) 
//	{
//		surface.setTitle(title);
//	}
	
//	void drawTestScene() 
//	{  
////		System.out.println("drawTestScene()...");
//		background(0.f);
//
//		stroke(255.f, 0.f, 255.f, 255.f);
//		strokeWeight(3.f);
//		
//		for (int i = -width; i < 2 * width; i += 50) {
//			line(i, -height, -100, i, 2 * height, -100);
//		}
//		for (int i = -height; i < 2 * height; i += 50) {
//			line(-width, i, -100, 2 * width, i, -100);
//		}
//
//		lights();
//		noStroke();
//		
////		translate(mouseX, mouseY, -200);
//		translate(mouseX, mouseY, 200);
//		
//		fill(255.f, 0.f, 255.f, 255.f);
//		box(100);
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
}