package wmViewer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.data.IntList;
import toxi.math.CircularInterpolation;
import toxi.math.InterpolateStrategy;
import toxi.math.LinearInterpolation;
import toxi.math.ScaleMap;
import toxi.math.ZoomLensInterpolation;

/********************************************
 * @author davidgordon
 * Class representing a world, with a viewer and fields to be displayed
 */

public class WMV_World 
{
	/* System Status */
	public boolean startedRunning = false;			// Program just started running
	private boolean initialSetup = false;			// Performing initial setup 
	private boolean creatingFields = false;			// Initializing media folders
	private boolean fieldsCreated = false;			// Initialized media folders
	private boolean saveImage = false;
	private int initializationField = 0;				// Field to be initialized this frame
	public int setupProgress = 0;						// Setup progress (0 to 100)
	
	/* Classes */
	WMV_WorldSettings settings;	// World settings
	WMV_Input input;					// Handles input
	WMV_Display display;				// Handles heads up display
	WMV_Viewer viewer;					// Handles viewer location
	
	/* Media */
	private ArrayList<WMV_Field> fields;				// List of fields, i.e. large geographical areas for 3D display
	
	/* Stitching */
	String stitchingPath;

	/* Clustering Modes */
	public boolean hierarchical = false;				// Use hierarchical clustering (true) or k-means clustering (false) 
	public boolean interactive = false;					// In user clustering mode?
	public boolean startInteractive = false;			// Start user clustering

	/* Time */
	private int timeMode = 2;							// Time Mode (0 = cluster; 1 = field; 2 = single)
	public boolean timeFading = false;					// Does time affect media brightness? 
	public boolean paused = false;						// Time is paused

	public int currentTime = 0;							// Time units since start of time cycle (day / month / year)
	public int currentDate = 0;							// Date units since start of date cycle (day / month / year)

	/* Graphics */
	public float hudDistance = -1000.f;					// Distance of the Heads-Up Display from the virtual camera

	public boolean alphaMode = true;					// Use alpha fading (true) or brightness fading (false)
	public float alpha = 195.f;							// Transparency
	private boolean beginFadingAlpha = false, fadingAlpha = false;
	private int fadingAlphaStartFrame = 0, fadingAlphaEndFrame = 0, fadingAlphaLength = 20;	
	private float fadingAlphaStart, fadingAlphaTarget;

	public boolean fadeEdges = true;					// Blur image edges
	public PImage blurMaskLeftTop, blurMaskLeftCenter, 	// Blur masks
				  blurMaskLeftBottom, blurMaskLeftBoth;
	public PImage blurMaskCenterTop, blurMaskCenterCenter, 	// Blur masks
	  blurMaskCenterBottom, blurMaskCenterBoth;
	public PImage blurMaskRightTop, blurMaskRightCenter, 	// Blur masks
	  blurMaskRightBottom, blurMaskRightBoth;
	public PImage blurMaskBothTop, blurMaskBothCenter, 	// Blur masks
	  blurMaskBothBottom, blurMaskBothBoth;
	public boolean drawForceVector = true;				// Show attraction vector on map (mostly for debugging)

	/* Model */
	public float defaultFocusDistance = 9.0f;			// Default focus distance for images and videos (m.)
	public float subjectSizeRatio = 0.18f;				// Subject portion of image / video plane (used in scaling from focus distance to imageSize)
	public float panoramaFocusDistanceFactor = 0.9f;	// Scaling from defaultFocusDistance to panorama radius
	public float videoFocusDistanceFactor = 0.9f;		// Scaling from defaultFocusDistance to video focus distance

	public boolean altitudeScaling = true;				// Scale media height by altitude (m.) EXIF field 
	public float altitudeScalingFactor = 0.33f;			// Adjust altitude for ease of viewing
	public final float altitudeScalingFactorInit = 0.33f;		
	
	public boolean showModel = false;					// Activate Model Display 
	public boolean showMediaToCluster = false;			// Draw line from each media point to cluster
	public boolean showCaptureToMedia = false;			// Draw line from each media point to its capture location
	public boolean showCaptureToCluster = false;		// Draw line from each media capture location to associated cluster

	/* Clusters */
	public boolean mergeClusters = true;				// Merge nearby clusters?
	public boolean autoClusterDistances = false;		// Automatically set minClusterDistance + maxClusterDistance based on mediaDensity?
	public float kMeansClusteringEpsilon = 0.005f;		// If no clusters move farther than this threshold, stop cluster refinement
	public boolean lockMediaToClusters = false;			// Align media with the nearest cluster (to fix GPS uncertainty error)
	
	public final float clusterCenterSize = 1.f;			// Size of cluster center, where autoNavigation stops
	public float mediaPointMass = 0.05f;				// Mass contribution of each media point
	public final float farDistanceFactor = 4.f;			// Multiplier for defaultFocusDistance to get farDistance
	public float clusterFarDistance = defaultFocusDistance * farDistanceFactor;			// Distance to apply greater attraction force on viewer
	public float minClusterDistance = 1.f; 				// Minimum distance between clusters, i.e. closer than which clusters are merged
	public float maxClusterDistance = 10.f;				// Maximum distance between cluster and media, i.e. farther than which single media clusters are created
	public final float maxClusterDistanceConstant = 0.33f;	// Divisor to set maxClusterDistance based on mediaDensity
	public float maxClusterDistanceFactor = 5.f;			// Limit on maxClusterDistance as multiple of min. as media spread increases

//	/* Viewer */
//	public boolean firstTeleport = false;

	/* Metadata */
	public boolean showMetadata = false;
	
	/* Memory */
	public int minAvailableMemory = 50000000;			// Minimum available memory
	public int memoryCheckFrequency = 50;
	public int minFrameRate = 10;	

	/* Interpolation */
	ScaleMap distanceFadeMap, timeFadeMap;
	InterpolateStrategy circularEaseOut = new CircularInterpolation(false);		// Steepest ascent at beginning
	InterpolateStrategy circularEaseIn = new CircularInterpolation(true);		// Steepest ascent toward end value
	InterpolateStrategy zoomLens = new ZoomLensInterpolation();
	InterpolateStrategy linear = new LinearInterpolation();
	
	/* File System */
	public String outputFolder;
	public boolean outputFolderSelected = false;
	public int requestedImages = 0;						// Count of images currently requested to be loaded from disk
	public int requestedPanoramas = 0;					// Count of panoramas currently requested to be loaded from disk	

	WorldMediaViewer p;
	
	WMV_World(WorldMediaViewer parent)
	{
		p = parent;
	}
	
	/**
	 * Set up the main classes and variables
	 */
	void initialize() 
	{
		if(p.debug.main)
			PApplet.println("Initializing world...");
		
		/* Create main classes */
		settings = new WMV_WorldSettings(this);
		input = new WMV_Input(this);
		viewer = new WMV_Viewer(this);			// Initialize navigation + viewer
		display = new WMV_Display(this);		// Initialize displays
		
		/* Initialize graphics and text parameters */
		p.colorMode(PConstants.HSB);
		p.rectMode(PConstants.CENTER);
		p.textAlign(PConstants.CENTER, PConstants.CENTER);

		timeFadeMap = new ScaleMap(0., 1., 0., 1.);				// Fading with time interpolation
		timeFadeMap.setMapFunction(circularEaseOut);

		distanceFadeMap = new ScaleMap(0., 1., 0., 1.);			// Fading with distance interpolation
		distanceFadeMap.setMapFunction(circularEaseIn);
	}

	void run()
	{
		if ( !initialSetup && !interactive && !p.exit ) 		/* Running the program */
		{
			draw3D();						// 3D Display
			draw2D();						// 2D Display
			updateTime();								// Update time cycle
			// updateLeapMotion();			// Update Leap Motion 
		}
		
		if ( p.exit ) 											/* Stopping the program */
		{
			if(p.debug.detailed)
				PApplet.println("Exit command! about to quit...");
			
			p.stopWorldMediaViewer();								//  Exit simulation
		}
		
		if ( p.debug.memory && p.frameCount % memoryCheckFrequency == 0 )		/* Memory debugging */
		{
			p.debug.checkMemory();
			p.debug.checkFrameRate();
		}
		
		if(saveImage && outputFolderSelected)		/* Image exporting */
		{
			if(viewer.settings.selection)
			{
				exportSelectedImages();
				PApplet.println("Saved image(s) to "+outputFolder);
			}
			else
			{
				p.saveFrame(outputFolder + "/" + getCurrentField().name + "-######.jpg");
				PApplet.println("Saved image: "+outputFolder + "/image" + "-######.jpg");
			}
			saveImage = false;
		}
	}
	
	/**
	 * Create each field and run initial clustering
	 */
	public void setup()
	{
		float fieldProgressInc = 100.f;
	
		if (p.openLibraryDialog)
		{
			p.selectFolderPrompt();
			p.openLibraryDialog = false;
		}
		
		/* Create and initialize fields from folders, perform initial clustering, finish setup */
		if (p.selectedLibrary && initialSetup && !creatingFields && !p.running)
		{
			createFieldsFromFolders(p.library.getFolders());		// Create empty field for each media folder	

			display.sendSetupMessage(" ");	// Show startup message
			display.sendSetupMessage("Creating "+fields.size()+(fields.size()>1?" fields...":" field..."));	// Show startup message
			
			if(fields.size() > 5) display.sendSetupMessage("This may take several minutes...");	// Show long startup time warning

			display.draw();											

			if(!p.basic)
				display.setupWMVWindow();									// Setup sidebar window
		
			fieldProgressInc = PApplet.round(100.f / fields.size());				// Amount to increment progress bar for each field
			creatingFields = true;
		}

		if (p.selectedLibrary && initialSetup && creatingFields && !fieldsCreated)	// Initialize fields
		{
			if(!fieldsCreated && !p.exit)
			{
				WMV_Field f = getField(initializationField);

				p.metadata.load(f);								// Import metadata for all media in field
				f.initialize(p.library.getLibraryFolder());		// Initialize field
				
				setupProgress += fieldProgressInc;				// Update progress bar
				display.draw();									// Draw progress bar
			}
			
			initializationField++;
			if( initializationField >= fields.size() )			// Initialize each field until all are finished
			{
				fieldsCreated = true;
			}
		}
		
		if (fieldsCreated && initialSetup && !p.running)
		{
			if(p.debug.main)
				PApplet.println("Finishing WMV_World setup()...");

			finishSetup();
		}

		if(p.selectedLibrary && !initialSetup && !interactive && !p.running)	/* Initial clustering once library is selected */
			startInitialClustering();							
		
		if(startInteractive && !interactive && !p.running)		/* Start interactive clustering */
			startInteractiveClustering();						
		
		if(interactive && !startInteractive && !p.running)		/* Running interactive clustering */
			runInteractiveClustering();							
	}
	
	/**
	 * Update the current field in 3D and display to viewer
	 */
	void draw3D()
	{
		/* 3D Display */
		getCurrentField().update();					// Update clusters in current field

		if(display.displayView == 0)		
			getCurrentField().draw();				// Display media in current field
		
		viewer.update();							// Update navigation
		viewer.camera.feed();						// Send the 3D camera view to the screen
	}

	void draw2D()
	{
		/* 2D Display */
		display.draw();								// Draw 2D display after 3D graphics
		
		if(fadingAlpha)                      		// Fade alpha
			updateFadingAlpha();

		if(startedRunning)							// If simulation just started running
		{
			viewer.moveToTimeSegmentInField(0, 0, true);	// Move to first time segment in field
			startedRunning = false;
		}
		
		if(viewer.settings.mouseNavigation)
			input.updateMouseNavigation(p.mouseX, p.mouseY);
	}
	
	/** 
	 * Update main time loop
	 */
	void updateTime()
	{
		switch(timeMode)
		{
			case 0:													// Cluster Time Mode
				for(WMV_Cluster c : getCurrentField().clusters)
					if(c.timeFading)
						c.updateTime();
				break;
			
			case 1:													// Field Time Mode
				if(timeFading && p.frameCount % settings.timeUnitLength == 0)
				{
					currentTime++;
	
					if(currentTime > settings.timeCycleLength)
						currentTime = 0;
	
					if(p.debug.field && currentTime > settings.timeCycleLength + settings.defaultMediaLength * 0.25f)
					{
						if(getCurrentField().mediaAreActive())
						{
							if(p.debug.detailed)
								PApplet.println("Media still active...");
						}
						else
						{
							currentTime = 0;
							if(p.debug.detailed)
								PApplet.println("Reached end of day at p.frameCount:"+p.frameCount);
						}
					}
				}
				break;

			case 2:													// Single Time Mode
				if(timeFading && p.frameCount % settings.timeUnitLength == 0)
				{
					currentTime++;
					
					if(p.debug.time && p.debug.detailed)
						PApplet.println("currentTime:"+currentTime);

					if(currentTime >= viewer.nextMediaStartFrame)
					{
						if(viewer.currentMedia + 1 < viewer.nearbyClusterTimelineMediaCount)
						{
							setSingleTimeModeCurrentMedia(viewer.currentMedia + 1);		
						}
						else
						{
							PApplet.println("Reached end of last media with "+(settings.timeCycleLength - currentTime)+ " frames to go...");
//							PApplet.println("  viewer.currentMedia "+viewer.currentMedia+ " viewer.nearbyClusterTimelineMediaCount:"+viewer.nearbyClusterTimelineMediaCount);
							currentTime = 0;
							startSingleTimeModeCycle();
						}
					}
					
					if(currentTime > settings.timeCycleLength)
					{
						currentTime = 0;
						startSingleTimeModeCycle();
					}
				}
				break;
				
			case 3:													// Flexible Time Mode
				break;
		}
	}
	
	void startSingleTimeModeCycle()
	{
		setSingleTimeModeCurrentMedia(0);
	}
	
	void setSingleTimeModeCurrentMedia(int timelineIndex)
	{
		viewer.currentMedia = timelineIndex;
		
		if(p.debug.time && p.debug.detailed)
			PApplet.println("viewer.currentMedia:"+viewer.currentMedia);
		
		if(viewer.nearbyClusterTimeline.size() > 0)
		{
			WMV_Time time = viewer.getNearbyTimeByIndex(timelineIndex);
			
			if(time != null)
			{
				int curMediaID = time.getID();
				int curMediaType = time.getMediaType();

				switch(curMediaType)
				{
				case 0:
					WMV_Image i = getCurrentField().images.get(curMediaID);
					i.currentMedia = true;
					viewer.currentMediaStartTime = currentTime;
					viewer.nextMediaStartFrame = currentTime + settings.defaultMediaLength;
					if(viewer.lookAtCurrentMedia)
						viewer.lookAtMedia(i.getID(), 0);
					break;
				case 1:
					WMV_Panorama n = getCurrentField().panoramas.get(curMediaID);
					n.currentMedia = true;
					viewer.currentMediaStartTime = currentTime;
					viewer.nextMediaStartFrame = currentTime + settings.defaultMediaLength;
//					viewer.lookAtMedia(n.getID(), 1);
					break;
				case 2:	
					WMV_Video v = getCurrentField().videos.get(curMediaID);
					v.currentMedia = true;
					viewer.currentMediaStartTime = currentTime;
					viewer.nextMediaStartFrame = currentTime + PApplet.round( getCurrentField().videos.get(curMediaID).getLength() * 29.98f );
					if(viewer.lookAtCurrentMedia)
						viewer.lookAtMedia(v.getID(), 2);
					break;
//				case 3:	
//					getCurrentField().sounds.get(curMediaID).currentMedia = true;
//					viewer.nextMediaStartFrame = p.frameCount + PApplet.round( getCurrentField().sounds.get(curMediaID).getLength() * 29.98f );
//					break;
				}
			}
			else
			{
				PApplet.println("ERROR in setSingleTimeModeCurrentMedia... time == null!!... timelineIndex:"+timelineIndex);
			}
		}
		else
			PApplet.println("ERROR in setSingleTimeModeCurrentMedia  viewer.nearbyClusterTimeline.size() == 0!!");
	}
	
	/**
	 * Load image masks
	 */
	public boolean loadImageMasks(String libFolder)
	{
		String maskPath = libFolder + "masks/";
		stitchingPath = libFolder + "stitched/";
		File maskFolder = new File(maskPath);
		String[] maskFolderList = maskFolder.list();
		
		if(maskFolder.list() == null)
		{
			return false;
		}
		else
		{
			for(String mask : maskFolderList)
			{
				if(mask.equals("blurMaskLeftTop.jpg"))
					blurMaskLeftTop = p.loadImage(maskPath + mask);
				if(mask.equals("blurMaskLeftCenter.jpg"))
					blurMaskLeftCenter = p.loadImage(maskPath + mask);
				if(mask.equals("blurMaskLeftBottom.jpg"))
					blurMaskLeftBottom = p.loadImage(maskPath + mask);
				if(mask.equals("blurMaskLeftBoth.jpg"))
					blurMaskLeftBoth = p.loadImage(maskPath + mask);
				if(mask.equals("blurMaskCenterTop.jpg"))
					blurMaskCenterTop = p.loadImage(maskPath + mask);
				if(mask.equals("blurMaskCenterCenter.jpg"))
					blurMaskCenterCenter = p.loadImage(maskPath + mask);
				if(mask.equals("blurMaskCenterBottom.jpg"))
					blurMaskCenterBottom = p.loadImage(maskPath + mask);
				if(mask.equals("blurMaskCenterBoth.jpg"))
					blurMaskCenterBoth = p.loadImage(maskPath + mask);
				if(mask.equals("blurMaskRightTop.jpg"))
					blurMaskRightTop = p.loadImage(maskPath + mask);
				if(mask.equals("blurMaskRightCenter.jpg"))
					blurMaskRightCenter = p.loadImage(maskPath + mask);
				if(mask.equals("blurMaskRightBottom.jpg"))
					blurMaskRightBottom = p.loadImage(maskPath + mask);
				if(mask.equals("blurMaskRightBoth.jpg"))
					blurMaskRightBoth = p.loadImage(maskPath + mask);
				if(mask.equals("blurMaskBothTop.jpg"))
					blurMaskBothTop = p.loadImage(maskPath + mask);
				if(mask.equals("blurMaskBothCenter.jpg"))
					blurMaskBothCenter = p.loadImage(maskPath + mask);
				if(mask.equals("blurMaskBothBottom.jpg"))
					blurMaskBothBottom = p.loadImage(maskPath + mask);
				if(mask.equals("blurMaskBothBoth.jpg"))
					blurMaskBothBoth = p.loadImage(maskPath + mask);
			}
		}
		
		return true;
	}
	
	/**
	 * Finish the setup process
	 */
	void finishSetup()
	{
		PApplet.println("Finishing setup...");

//		createTimeCycle();
		display.window.setupWMVWindow();
		
		initialSetup = false;				
		display.initialSetup = false;
		
		setupProgress = 100;

		p.running = true;
		startedRunning = true;
		
	}
	
	/**
	 * Stops any currently playing videos
	 */
	void stopAllVideos()
	{
		for(int i=0;i<getCurrentField().videos.size();i++)
		{
			if(getCurrentField().videos.get(i) != null && getCurrentField().videos.get(i).video != null)
				getCurrentField().videos.get(i).stopVideo();
		}
	}

	/**
	 * Reset variables
	 */
	void reset()
	{
		initializationField = 0;				// Field to be initialized this frame
		setupProgress = 0;						// Setup progress (0 to 100)
		
		startedRunning = false;			// Program just started running
		initialSetup = false;			// Performing initial setup 
		creatingFields = false;			// Initializing media folders
		fieldsCreated = false;			// Initialized media folders
		saveImage = false;

		settings.reset();
//		maxStitchingImages = 30;						// Maximum number of images to try to stitch
//		stitchingMinAngle = 30.f;						// Angle in degrees that determines media segments for stitching 
//		persistentStitching = false;			// Keep trying to stitch, removing one image at a time until it works or no images left
//
//		showUserPanoramas = true;			// Show panoramas stitched from user selected media
//		showStitchedPanoramas = true;		// Show panoramas stitched from media segments

		/* Clustering Modes */
		hierarchical = false;					// Use hierarchical clustering (true) or k-means clustering (false) 
		interactive = false;					// In user clustering mode?
		startInteractive = false;				// Start user clustering

		/* Time */
//		timeMode = 0;							// Time Mode (0 = cluster; 1 = field)
		timeFading = false;						// Does time affect media brightness? 
		paused = false;							// Time is paused

		currentTime = 0;							// Time units since start of time cycle (day / month / year)
//		settings.timeCycleLength = 250;					// Length of main time loop in frames
//		timeUnitLength = 1;						// How many frames between time increments
//		settings.timeInc = settings.timeCycleLength / 30.f;			

		currentDate = 0;							// Date units since start of date cycle (day / month / year)
//		dateCycleLength = 500;					// Length of main date loop in frames
//		dateUnitLength = 1;						// How many frames between date increments
//		dateInc = dateCycleLength / 30.f;			
//
//		settings.defaultMediaLength = initsettings.defaultMediaLength;			// Default frame length of media in time cycle

		/* Graphics */
		hudDistance = -1000.f;					// Distance of the Heads-Up Display from the virtual camera

		alphaMode = true;						// Use alpha fading (true) or brightness fading (false)
		alpha = 195.f;							// Transparency
		beginFadingAlpha = false;
		fadingAlpha = false;
		fadingAlphaStartFrame = 0; 
		fadingAlphaEndFrame = 0; 
		fadingAlphaLength = 20;	

		fadeEdges = true;						// Blur image edges
		drawForceVector = true;					// Show attraction vector on map (mostly for debugging)
		
		/* Video */
		initializationField = 0;				// Field to be initialized this frame
		setupProgress = 0;						// Setup progress (0 to 100)
		
		/* Viewer */
//		orientationMode = false;				// Orientation Mode: no simulation of viewer movement (only images fading in and out)
//		angleFading = true;						// Do photos fade out as the camera turns away from them?
//
//		visibleAngle = PApplet.PI / 3.33f;		// Angle within which images and videos become visible
//		centeredAngle = visibleAngle / 2.f;		// At what angle is the image centered?
//		angleThinning = false;					// Thin images and videos of similar orientation
//		thinningAngle = PApplet.PI / 6.f;		// Angle to thin images and videos within

		/* Model */
		defaultFocusDistance = 9.0f;			// Default focus distance for images and videos (m.)
		subjectSizeRatio = 0.18f;				// Subject portion of image / video plane (used in scaling from focus distance to imageSize)
		panoramaFocusDistanceFactor = 0.9f;		// Scaling from defaultFocusDistance to panorama radius
		videoFocusDistanceFactor = 0.9f;		// Scaling from defaultFocusDistance to video focus distance
		
		altitudeScaling = true;					// Scale media height by altitude (m.) EXIF field 
		altitudeScalingFactor = 0.33f;			// Adjust altitude for ease of viewing	-- Work more on this...
		
		showModel = false;						// Activate Model Display 
		showMediaToCluster = false;				// Draw line from each media point to cluster
		showCaptureToMedia = false;				// Draw line from each media point to its capture location
		showCaptureToCluster = false;			// Draw line from each media capture location to associated cluster

		/* Clusters */
		mergeClusters = true;					// Merge nearby clusters?
		autoClusterDistances = false;			// Automatically set minClusterDistance + maxClusterDistance based on mediaDensity?
		kMeansClusteringEpsilon = 0.005f;		// If no clusters move farther than this threshold, stop cluster refinement
		lockMediaToClusters = false;			// Align media with the nearest cluster (to fix GPS uncertainty error)
		
		mediaPointMass = 0.05f;					// Mass contribution of each media point
		clusterFarDistance = defaultFocusDistance * farDistanceFactor;			// Distance to apply greater attraction force on viewer
		minClusterDistance = 4.f; 				// Minimum distance between clusters, i.e. closer than which clusters are merged
		maxClusterDistance = 10.f;				// Maximum distance between cluster and media, i.e. farther than which single media clusters are created
		maxClusterDistanceFactor = 5.f;			// Limit on maxClusterDistance as multiple of min. as media spread increases

		if(p.debug.main)
			PApplet.println("Resetting world...");
		
		/* Create main classes */
		viewer.reset();			// Initialize navigation + viewer
		display.reset();		// Initialize displays

		/* Initialize graphics and text parameters */
		p.colorMode(PConstants.HSB);
		p.rectMode(PConstants.CENTER);
		p.textAlign(PConstants.CENTER, PConstants.CENTER);

		timeFadeMap = new ScaleMap(0., 1., 0., 1.);				// Fading with time interpolation
		timeFadeMap.setMapFunction(circularEaseOut);

		distanceFadeMap = new ScaleMap(0., 1., 0., 1.);			// Fading with distance interpolation
		distanceFadeMap.setMapFunction(circularEaseIn);
		
//		p.selectFolderPrompt();
	}
	
	/**
	 * Start initial clustering process
	 */
	public void startInitialClustering()
	{
		display.startupMessages = new ArrayList<String>();	// Clear startup messages
		if(p.debug.metadata)
		{
			display.sendSetupMessage("Library folder: "+p.library.getLibraryFolder());	// Show library folder name
			display.sendSetupMessage(" ");
		}
		
		display.sendSetupMessage("Starting WorldMediaViewer v1.0...");	// Show startup message
		display.draw();											

		p.running = false;					// Stop running
		initialSetup = true;				// Start clustering mode
	}
	
	/**
	 * Start interactive clustering mode
	 */
	public void startInteractiveClustering()
	{
		p.background(0.f);					// Clear screen
		
		p.running = false;					// Stop running simulation
		interactive = true;					// Start interactive clustering mode
		startInteractive = false;			// Have started
		
//		display.initializeSmallMap();
		display.map2D.initializeLargeMap();
		
		display.resetDisplayModes();		// Clear messages
		display.displayClusteringInfo();
		
		getCurrentField().blackoutMedia();	// Blackout all media
	}
	
	/**
	 * Run user clustering 
	 */
	public void runInteractiveClustering()
	{
		p.background(0.f);					// Clear screen
		display.draw();						// Draw text		
	}
	
	/**
	 * Finish running Interactive Clustering and restart simulation 
	 */
	public void finishInteractiveClustering()
	{
		p.background(0.f);
		
		viewer.clearAttractorCluster();

		interactive = false;				// Stop interactive clustering mode
		startedRunning = true;				// Start GMViewer running
		p.running = true;	
		
		viewer.setCurrentCluster( viewer.getNearestCluster(false), -1 );
		getCurrentField().blackoutMedia();
	}
	
	/**
	 * Create fields from the media folders
	 */
	void createFieldsFromFolders(ArrayList<String> folders)
	{
		fields = new ArrayList<WMV_Field>();			// Initialize fields array
		int count = 0;
		
		for(String s : folders)
		{
			fields.add(new WMV_Field(this, s, count));
			count++;
		}
	}
	
	/**
	 * Transition alpha from current to given value
	 */
	void fadeAlpha(float target)
	{
		if(target != alpha)			// Check if already at target
		{
			beginFadingAlpha = true;
			fadingAlpha = true;   
			fadingAlphaStart = alpha;
			fadingAlphaTarget = target;
			fadingAlphaStartFrame = p.frameCount;
			fadingAlphaEndFrame = fadingAlphaStartFrame + fadingAlphaLength;
		}
		else
		{
			fadingAlpha = false;
		}
	}
	
	/**
	 * Update alpha each frame
	 */
	void updateFadingAlpha()
	{
		float newAlphaFadeValue = 0.f;

		if(beginFadingAlpha)
		{
			fadingAlphaStartFrame = p.frameCount;					
			fadingAlphaEndFrame = p.frameCount + fadingAlphaLength;	
			beginFadingAlpha = false;
		}

		if (p.frameCount >= fadingAlphaEndFrame)
		{
			fadingAlpha = false;
			newAlphaFadeValue = fadingAlphaTarget;
		} 
		else
		{
			newAlphaFadeValue = PApplet.map(p.frameCount, fadingAlphaStartFrame, fadingAlphaEndFrame, fadingAlphaStart, fadingAlphaTarget);      // Fade with distance from current time
		}

		alpha = newAlphaFadeValue;
	}

	/**
	 * @return Current field
	 */
	public WMV_Field getCurrentField()
	{
		WMV_Field f = fields.get(viewer.getField());
		return f;
	}
	
	/**
	 * @return All fields in library
	 */
	public ArrayList<WMV_Field> getFields()
	{
		return fields;
	}
	
	/**
	 * @return Model of current field
	 */
	public WMV_Model getCurrentModel()
	{
		WMV_Model m = getCurrentField().model;
		return m;
	}
	
	/**
	 * @return The current cluster
	 */
	public WMV_Cluster getCurrentCluster()
	{
		WMV_Cluster c;
		if(viewer.getCurrentClusterID() > 0 && viewer.getCurrentClusterID() < getCurrentField().clusters.size())
		{
			c = getCurrentField().clusters.get(viewer.getCurrentClusterID());
			return c;
		}
		else return null;
	}
	
	/**
	 * @return The current attractor cluster
	 */
	public WMV_Cluster getAttractorCluster()
	{
		WMV_Cluster c = getCurrentField().clusters.get(viewer.getAttractorCluster());
		return c;
	}
	
	/**
	 * @return All images in current field
	 */
	public ArrayList<WMV_Image> getFieldImages()
	{
		ArrayList<WMV_Image> iList = getCurrentField().images;
		return iList;
	}
	
	/**
	 * @return All panoramas in field
	 */
	public ArrayList<WMV_Panorama> getFieldPanoramas()
	{
		ArrayList<WMV_Panorama> pList = getCurrentField().panoramas;
		return pList;
	}
	
	/**
	 * @return All videos in current field
	 */
	public ArrayList<WMV_Video> getFieldVideos()
	{
		ArrayList<WMV_Video> iList = getCurrentField().videos;
		return iList;
	}
	
	/**
	 * @return All clusters in current field
	 */
	public ArrayList<WMV_Cluster> getFieldClusters()
	{
		ArrayList<WMV_Cluster> clusters = getCurrentField().clusters;
		return clusters;
	}

	/**
	 * @return Active clusters in current field
	 */
	public ArrayList<WMV_Cluster> getActiveClusters()
	{
		ArrayList<WMV_Cluster> clusters = new ArrayList<WMV_Cluster>();

		for(WMV_Cluster c : getCurrentField().clusters)
			if(c.isActive() && !c.isEmpty())
				clusters.add(c);
		
		return clusters;
	}

	/**
	 * @return Active clusters in current field
	 */
	public ArrayList<WMV_Cluster> getVisibleClusters()
	{
		ArrayList<WMV_Cluster> clusters = new ArrayList<WMV_Cluster>();

		for(int i : viewer.getNearClusters(-1, defaultFocusDistance))
		{
			WMV_Cluster c = getCluster(i);
			if(c.isActive() && !c.isEmpty())
				clusters.add(c);
		}
		
		return clusters;
	}

	/**
	 * @param id Cluster ID
	 * @return Specified cluster from current field
	 */
	WMV_Cluster getCluster(int id)
	{
		WMV_Cluster c = getCurrentField().clusters.get(id);
		return c;
	}

	/**
	 * Manually move back in time
	 */
	void decrementTime()
	{
		currentTime -= settings.timeInc;
		if (currentTime < 0)
			currentTime = 0;
	}
	
	/**
	 * Manually move forward in time
	 */
	void incrementTime()
	{
		currentTime += settings.timeInc;
		if (currentTime > settings.timeCycleLength)
			currentTime = settings.timeCycleLength - 200;
	}
	
	/**
	 * Decrement time cycle length
	 */
	void decrementCycleLength()
	{
		if(settings.timeCycleLength - 20 > 40.f)
		{
			settings.timeCycleLength -= 20.f;
			settings.timeInc = settings.timeCycleLength / 30.f;			
		}
	}
	
	/**
	 * Increment time cycle length
	 */
	void incrementCycleLength()
	{
		if(settings.timeCycleLength + 20 > 1000.f)
		{
			settings.timeCycleLength += 20.f;
			settings.timeInc = settings.timeCycleLength / 30.f;			
		}
	}

	/**
	 * Save current screen view to disk
	 */
	public void saveToDisk() 
	{
		if(p.debug.main)
			PApplet.println("Will output image to disk.");
		saveImage = true;
	}
	
	public void exportSelectedImages()
	{
		IntList selected = getCurrentField().getSelectedMedia(0);
		for(int i:selected)
		{
			InputStream is = null;
			OutputStream os = null;
			try {
				File imgFilePath = new File(getCurrentField().images.get(i).getFilePath());
				is = new FileInputStream(imgFilePath);
				os = new FileOutputStream(new File(outputFolder + "/" + getCurrentField().images.get(i).getName()));
				byte[] buffer = new byte[1024];
				int length;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
			} 
			catch(Throwable t)
			{
				PApplet.println("ERROR 1 in exportSelectedImages:"+t);
			}
			finally 
			{
				try 
				{
					is.close();
					os.close(); 
				} 
				catch (IOException e) 
				{
					PApplet.println("ERROR 2 in exportSelectedImages:"+e);
				}
			}

		}
	}

	/**
	 * @return Number of fields in simulation
	 */
	public int getFieldCount()
	{
		return fields.size();
	}

	/**
	 * @param fieldIndex Field ID 
	 * @return The specified field 
	 */
	public WMV_Field getField(int fieldIndex)
	{
		if(fieldIndex >= 0 && fieldIndex < fields.size())
			return fields.get(fieldIndex);
		else
			return null;
	}
	

	/**
	 * -- TO DO!!
	 * Create fields from detected k-means clusters in single media folder 
	 * @param mediaFolder Folder containing the media
	 */
	public void createFieldsFromFolder(String mediaFolder)
	{
		fields = new ArrayList<WMV_Field>();			// Initialize fields array
//		ArrayList<GMV_Cluster> clusters;		
//		int count = 0;
//		for(String s : clusters)
//		{
//			fields.add(new GMV_Field(this, s, count));
//			count++;
//		}
//		PApplet.println("Created "+getCurrentField().clusters.size()+"fields from "+xxx+" clusters...");
	}
	
	public void createTimeCycle()
	{
		if(timeMode == 0 || timeMode == 1)
		{
			settings.timeCycleLength = settings.defaultTimeCycleLength;
		}
		else if(timeMode == 2)
		{
			ArrayList<WMV_Cluster> cl = getVisibleClusters();
			settings.timeCycleLength = 0;
			
			for(WMV_Cluster c : cl)						// Time cycle length is flexible according to visible cluster media lengths
			{
				settings.timeCycleLength += c.getImages().size() * settings.defaultMediaLength;
				settings.timeCycleLength += c.getPanoramas().size() * settings.defaultMediaLength;
				for(WMV_Video v: c.getVideos())
					settings.timeCycleLength += PApplet.round( v.getLength() * 29.98f );
//				for(WMV_Sound s: c.getSounds())
//					settings.timeCycleLength += PApplet.round( s.getLength() * 29.98f );
			}
			
			if(cl.size() == 1)
				viewer.setNearbyClusterTimeline(cl.get(0).getTimeline());
			else if(cl.size() > 1)
				viewer.createNearbyClusterTimeline(cl);
				
			if(cl.size() == 0)
				settings.timeCycleLength = -1;				// Flag for Viewer to keep calling this method until clusters are visible
			else
				startSingleTimeModeCycle();
		}
		else if(timeMode == 3)						// Time cycle length is flexible according to visible cluster timelines
		{
			float highest = -100000.f;
			float lowest = 100000.f;
			ArrayList<WMV_Cluster> cl = getVisibleClusters();
			
			for(WMV_Cluster c : cl)
			{
				float low = c.timeline.get(0).getLower().getTime();
				if(low < lowest)
					lowest = low;
				float high = c.timeline.get(c.timeline.size()-1).getUpper().getTime();
				if(high > highest)
					highest = high;
			}
			
			float val = PApplet.map(highest - lowest, 0.f, 1.f, 0.f, settings.defaultMediaLength);
			if(cl.size() == 0)
				settings.timeCycleLength = -1;
		}
	}
	
	/**
	 * Set Time Mode
	 * @param newTimeMode New time mode (0: Cluster, 1:Field, 2: Media)
	 */
	public void setTimeMode(int newTimeMode)
	{
		timeMode = newTimeMode;
		
		if(timeMode == 2)
			createTimeCycle();
		
		if(display.window.setupTimeWindow)
		{
			switch(timeMode)
			{
				case 0:
					display.window.optClusterTimeMode.setSelected(true);
					display.window.optFieldTimeMode.setSelected(false);
					display.window.optMediaTimeMode.setSelected(false);
					break;
				case 1:
					display.window.optClusterTimeMode.setSelected(false);
					display.window.optFieldTimeMode.setSelected(true);
					display.window.optMediaTimeMode.setSelected(false);
					break;
				case 2:
					display.window.optClusterTimeMode.setSelected(false);
					display.window.optFieldTimeMode.setSelected(false);
					display.window.optMediaTimeMode.setSelected(true);
					break;
			}		
		}
	}
	
	public int getTimeMode()
	{
		return timeMode;
	}
}
