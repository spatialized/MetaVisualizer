package multimediaLocator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PVector;
import toxi.math.CircularInterpolation;
import toxi.math.InterpolateStrategy;
import toxi.math.LinearInterpolation;
import toxi.math.ScaleMap;
import toxi.math.ZoomLensInterpolation;

/********************************************
 * Virtual world comprised of a viewer and one or more multimedia fields
 * @author davidgordon
 */

public class WMV_World 
{
	/* Classes */
	ML_Input input;								// Handles input
	WMV_WorldSettings settings;					// World settings
	WMV_WorldState state;						// World state
	WMV_Utilities utilities;					// Utilities
	WMV_Viewer viewer;							// Virtual Viewer
	private ArrayList<WMV_Field> fields;		// Geographical areas containing media for simulation

	/* Graphics */
	public PImage blurMaskLeftTop, blurMaskLeftCenter, 	// Blur masks
				  blurMaskLeftBottom, blurMaskLeftBoth;
	public PImage blurMaskCenterTop, blurMaskCenterCenter, 	
	  blurMaskCenterBottom, blurMaskCenterBoth;
	public PImage blurMaskRightTop, blurMaskRightCenter, 	// Blur masks
	  blurMaskRightBottom, blurMaskRightBoth;
	public PImage blurMaskBothTop, blurMaskBothCenter, 	
	  blurMaskBothBottom, blurMaskBothBoth;
	public boolean drawForceVector = true;				// Show attraction vector on map (mostly for debugging)

	
	/* Interpolation */
	ScaleMap distanceFadeMap, timeFadeMap;
	InterpolateStrategy circularEaseOut = new CircularInterpolation(false);		// Steepest ascent at beginning
	InterpolateStrategy circularEaseIn = new CircularInterpolation(true);		// Steepest ascent toward end value
	InterpolateStrategy zoomLens = new ZoomLensInterpolation();
	InterpolateStrategy linear = new LinearInterpolation();
	
	/* File System */
	public String outputFolder;
	public boolean outputFolderSelected = false;

	MultimediaLocator p;
	
	WMV_World(MultimediaLocator parent)
	{
		p = parent;
		utilities = new WMV_Utilities();
	}
	
	/**
	 * Set up main classes and variables involving the world and viewer 
	 */
	void initialize() 
	{
		if(p.debugSettings.main) System.out.println("Initializing WMV_World...");

		/* Create main classes */
		settings = new WMV_WorldSettings();
		state = new WMV_WorldState();
		viewer = new WMV_Viewer(this, settings, state, p.debugSettings);			// Initialize navigation + viewer
		
		timeFadeMap = new ScaleMap(0., 1., 0., 1.);				// Fading with time interpolation
		timeFadeMap.setMapFunction(circularEaseOut);

		distanceFadeMap = new ScaleMap(0., 1., 0., 1.);			// Fading with distance interpolation
		distanceFadeMap.setMapFunction(circularEaseIn);
	}

	void enter(int fieldIdx)
	{
		viewer.enterField( getField(fieldIdx) );								// Update navigation
		viewer.updateState(settings, state);
		viewer.update();												// Update navigation
		viewer.moveToFirstTimeSegment(false);
	}

	void updateState()
	{
		state.frameCount = p.frameCount;
		viewer.updateState(settings, state);
		getCurrentField().update(settings, state, viewer.getSettings(), viewer.getState());				// Update clusters in current field
	}
	
	/**
	 * Update the current field in 3D and display to viewer
	 */
	void draw3D()
	{
		/* 3D Display */
		attractViewer();						// Attract the viewer
		
		if(p.display.displayView == 0)
		{
			p.hint(PApplet.ENABLE_DEPTH_TEST);					// Enable depth testing for drawing 3D graphics
			p.background(0.f);									// Set background
			getCurrentField().display(p);					// Display media in current field
			if(settings.showUserPanoramas || settings.showStitchedPanoramas)
			{
				ArrayList<WMV_Cluster> clusters = getCurrentField().getClusters();
				if(clusters.size()>0)
					clusters.get(viewer.getState().getCurrentClusterID()).display(p);		// Draw current cluster
			}

		}
		
		viewer.update();							// Update navigation
		if(p.display.displayView == 0)	
			if(p.state.running)
				viewer.draw();						// Send the 3D camera view to the screen
	}

	void draw2D()
	{
		/* 2D Display */
		p.display.display(this);										// Draw 2D display after 3D graphics
		
		if(state.fadingAlpha) updateFadingAlpha();				// Fade alpha

		if(viewer.getSettings().mouseNavigation)
			input.updateMouseNavigation(viewer, p.mouseX, p.mouseY, p.frameCount);
	}

	/**
	 * Attract viewer to each of the attracting clusters
	 */
	public void attractViewer()
	{
		if(viewer.getState().isMovingToAttractor())
		{
			if(viewer.getAttractorPoint() != null)
				viewer.getAttractorPoint().attractViewer(viewer);		// Attract the camera to the memory navigation goal
			else 
				System.out.println("viewer.attractorPoint == NULL!!");
		}
		else if(viewer.getState().isMovingToCluster())				// If the camera is moving to a cluster (besides memoryCluster)
		{
			for( WMV_Cluster c : getCurrentField().getAttractingClusters() )
				if(c.getClusterDistance() > settings.clusterCenterSize)		// If not already at attractor cluster center, attract camera 
					c.attractViewer(viewer);
		}
	}

	/** 
	 * Update main time loop
	 */
	void updateTime()
	{
		switch(state.timeMode)
		{
			case 0:													// Cluster Time Mode
				for(WMV_Cluster c : getCurrentField().getClusters())
					if(c.getState().timeFading)
						c.updateTime();
				break;
			
			case 1:													// Field Time Mode
				if(state.timeFading && state.frameCount % settings.timeUnitLength == 0)
				{
					state.currentTime++;
					if(state.currentTime > settings.timeCycleLength)
						state.currentTime = 0;
	
					if(p.debugSettings.field && state.currentTime > settings.timeCycleLength + settings.defaultMediaLength * 0.25f)
					{
						if(getCurrentField().mediaAreActive())
						{
							if(p.debugSettings.detailed)
								System.out.println("Media still active...");
						}
						else
						{
							state.currentTime = 0;
							if(p.debugSettings.detailed)
								System.out.println("Reached end of day at state.frameCount:"+state.frameCount);
						}
					}
				}
				break;

			case 2:													// Single Time Mode
				if(state.timeFading && state.frameCount % settings.timeUnitLength == 0)
				{
					state.currentTime++;
					if(p.debugSettings.time && p.debugSettings.detailed)
						System.out.println("currentTime:"+state.currentTime);

					if(state.currentTime >= viewer.getNextMediaStartTime())
					{
						if(viewer.getCurrentMedia() + 1 < viewer.getNearbyClusterTimelineMediaCount())
						{
							setSingleTimeModeCurrentMedia(viewer.getCurrentMedia() + 1);		
						}
						else
						{
							System.out.println("Reached end of last media with "+(settings.timeCycleLength - state.currentTime)+ " frames to go...");
							state.currentTime = 0;
							startSingleTimeModeCycle();
						}
					}
					
					if(state.currentTime > settings.timeCycleLength)
					{
						state.currentTime = 0;
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
		viewer.setCurrentMedia( timelineIndex );
		
		if(viewer.getNearbyClusterTimeline().size() > 0)
		{
			WMV_Time time = viewer.getNearbyTimeByIndex(timelineIndex);
			
			if(time != null)
			{
				int curMediaID = time.getID();
				int curMediaType = time.getMediaType();

				switch(curMediaType)
				{
				case 0:
					WMV_Image i = getCurrentField().getImage(curMediaID);
					i.getMediaState().isCurrentMedia = true;
					viewer.setCurrentMediaStartTime(state.currentTime);
					viewer.setNextMediaStartTime(state.currentTime + settings.defaultMediaLength);
					if(viewer.lookAtCurrentMedia())
						viewer.lookAtMedia(i.getID(), 0);
					break;
				case 1:
					WMV_Panorama n = getCurrentField().getPanorama(curMediaID);
					n.getMediaState().isCurrentMedia = true;
					viewer.setCurrentMediaStartTime(state.currentTime);
					viewer.setNextMediaStartTime(state.currentTime + settings.defaultMediaLength);
//					viewer.lookAtMedia(n.getID(), 1);
					break;
				case 2:	
					WMV_Video v = getCurrentField().getVideo(curMediaID);
					v.getMediaState().isCurrentMedia = true;
					viewer.setCurrentMediaStartTime(state.currentTime);
					viewer.setNextMediaStartTime(state.currentTime + PApplet.round( getCurrentField().getVideo(curMediaID).getLength() * 29.98f));
					if(viewer.lookAtCurrentMedia())
						viewer.lookAtMedia(v.getID(), 2);
					break;
//				case 3:	
//					getCurrentField().sounds.get(curMediaID).currentMedia = true;
//					viewer.nextMediaStartFrame = state.frameCount + PApplet.round( getCurrentField().sounds.get(curMediaID).getLength() * 29.98f );
//					break;
				}
			}
			else
			{
				System.out.println("ERROR in setSingleTimeModeCurrentMedia... time == null!!... timelineIndex:"+timelineIndex);
			}
		}
		else
			System.out.println("ERROR in setSingleTimeModeCurrentMedia  viewer.nearbyClusterTimeline.size() == 0!!");
	}
	
	public void updateAllMediaSettings()
	{
		WMV_Field f = getCurrentField();
		for(WMV_Image img : f.getImages())
			img.updateSettings(settings, state, viewer.getSettings(), viewer.getState(), p.debugSettings);
		for(WMV_Panorama pano : f.getPanoramas())
			pano.updateSettings(settings, state, viewer.getSettings(), viewer.getState(), p.debugSettings);
		for(WMV_Video vid : f.getVideos())
			vid.updateSettings(settings, state, viewer.getSettings(), viewer.getState(), p.debugSettings);
//		for(WMV_Sound snd : f.getSounds())
//			img.updateSettings(world.settings, world.state, world.viewer.getSettings(), world.viewer.getState(), debugSettings);
	}
	
	/**
	 * Stops any currently playing videos
	 */
	void stopAllVideos()
	{
		for(int i=0;i<getCurrentField().getVideos().size();i++)
		{
			if(getCurrentField().getVideo(i) != null && getCurrentField().getVideo(i).video != null)
				getCurrentField().getVideo(i).stopVideo();
		}
	}

	/**
	 * Save the current world, field and viewer states and settings to file
	 */
	void saveSimulationState()
	{
		String folderPath = getDataFolder();
		File directory = new File(folderPath);
		if(!directory.exists()) directory.mkdir();			// Create directory if doesn't exist
		
		PApplet.println("Saving Simulation State to: "+folderPath);
		
		getCurrentField().captureState();
		p.library.saveWorldSettings(settings, folderPath+"ml_library_worldSettings.json");
		p.library.saveWorldState(state, folderPath+"ml_library_worldState.json");
		p.library.saveViewerSettings(viewer.getSettings(), folderPath+"ml_library_viewerSettings.json");
		p.library.saveViewerState(viewer.getState(), folderPath+"ml_library_viewerState.json");
		p.library.saveFieldState(getCurrentField().getState(), folderPath+"ml_library_fieldState.json");
	}

	/**
	 * Save the current world, field and viewer states and settings to file
	 */
	public boolean loadSimulationState(WMV_SimulationState newSimulationState, WMV_Field curField)
	{
		PApplet.println("Loading Simulation State... ");

		loadState();
		loadSettings();
		loadViewerState();
		loadViewerSettings();
		
		/* Check world and viewer state/settings */
		if(settings != null) System.out.println("WorldSettings exists...");
		if(state != null) System.out.println("WorldState exists...");
		if(viewer.getSettings() != null) System.out.println("ViewerSettings exists...");
		if(viewer.getState() != null) System.out.println("ViewerState exists...");
		
		String fieldName = curField.getName();
		fields = new ArrayList<WMV_Field>();			// -- Revise to handle multiple fields
		WMV_Field newField = new WMV_Field(settings, state, viewer.getSettings(), viewer.getState(), p.debugSettings, "", 0);
		newField.setName(fieldName);
		
		fields.add(newField);
		
		boolean success = loadFieldState(fields.get(0));
		if(success) System.out.println("FieldState exists...");

//		/* Classes */
//		private WMV_WorldSettings worldSettings;	// World settings
//		private WMV_WorldState worldState;			// World state
//		private WMV_ViewerSettings viewerSettings;	// Viewer settings
//		private WMV_ViewerState viewerState;		// Viewer state
//		private ML_DebugSettings debugSettings;		// Debug settings
//		private WMV_FieldState state;				// Field state
//		private WMV_Utilities utilities;			// Utility methods
//		private WMV_Model model;										// Dimensions and properties of current virtual space
//
//		/* Time */
//		private ArrayList<WMV_TimeSegment> timeline;						// List of time segments in this field ordered by time from 0:00 to 24:00 as a single day
//		private ArrayList<ArrayList<WMV_TimeSegment>> timelines;			// Lists of time segments in field ordered by date
//		private ArrayList<WMV_Date> dateline;								// List of dates in this field, whose indices correspond with timelines in timelines list
//		
//		/* Data */
//		private ArrayList<WMV_Image> images; 					// All images in this field
//		private ArrayList<WMV_Panorama> panoramas; 				// All panoramas in this field
//		private ArrayList<WMV_Video> videos; 					// All videos in this field
//		private ArrayList<WMV_Sound> sounds; 					// All videos in this field
//		private ArrayList<WMV_Cluster> clusters;				// Clusters (spatial groupings) of media 

		return success;
	}

	public boolean loadFieldState(WMV_Field field)
	{
		return field.setState(p, p.library.loadFieldState(getDataFolder()+"ml_library_fieldState.json"));
	}

	public void loadSettings()
	{
		setSettings(p.library.loadWorldSettings(getDataFolder()+"ml_library_worldSettings.json"));
	}

	public void loadState()
	{
		setState(p.library.loadWorldState(getDataFolder()+"ml_library_worldState.json"));
	}

	public void loadViewerSettings()
	{
		viewer.setSettings(p.library.loadViewerSettings(getDataFolder()+"ml_library_viewerSettings.json"));
	}

	public void loadViewerState()
	{
		viewer.setState(p.library.loadViewerState(getDataFolder()+"ml_library_viewerState.json"));
	}

	/**
	 * Reset variables
	 */
	void reset(boolean system)
	{
		if(system)
		{
			p.state.initializationField = 0;				// Field to be initialized this frame
//			p.state.setupProgress = 0;						// Setup progress (0 to 100)
			p.state.startedRunning = false;			// Program just started running
			p.state.initialSetup = false;			// Performing initial setup 
			p.state.initializingFields = false;			// Initializing media folders
			p.state.fieldsInitialized = false;			// Initialized media folders
			p.state.export = false;
		}
		
		settings.reset();

		/* Clustering Modes */
		state.hierarchical = false;					// Use hierarchical clustering (true) or k-means clustering (false) 
		p.state.interactive = false;					// In user clustering mode?
		p.state.startInteractive = false;				// Start user clustering

		/* Time */
		state.timeFading = false;					// Does time affect media brightness? 
		state.paused = false;						// Time is paused

		state.currentTime = 0;						// Time units since start of time cycle (day / month / year)
		state.currentDate = 0;						// Current timeline ID corresponding to capture date in ordered list

		/* Graphics */
		state.hudDistance = -1000.f;				// Distance of the Heads-Up Display from the virtual camera

		state.alphaMode = true;						// Use alpha fading (true) or brightness fading (false)
		state.alpha = 195.f;						// Transparency
		state.beginFadingAlpha = false;
		state.fadingAlpha = false;
		state.fadingAlphaStartFrame = 0; 
		state.fadingAlphaEndFrame = 0; 
		state.fadingAlphaLength = 20;	

		state.fadeEdges = true;						// Blur image edges
		drawForceVector = true;						// Show attraction vector on map (mostly for debugging)
		
		/* Video */
		state.showModel = false;					// Display model 
		state.showMediaToCluster = false;			// Draw line from each media point to cluster
		state.showCaptureToMedia = false;			// Draw line from each media point to its capture location
		state.showCaptureToCluster = false;			// Draw line from each media capture location to associated cluster

		/* Clusters */
		state.mergeClusters = true;					// Merge nearby clusters?
		state.autoClusterDistances = false;			// Automatically set minClusterDistance + maxClusterDistance based on mediaDensity?
		state.lockMediaToClusters = false;			// Align media with the nearest cluster (to fix GPS uncertainty error)

		if(p.debugSettings.main)
			System.out.println("Resetting world...");
		
		/* Create main classes */
		viewer.reset();			// Initialize navigation + viewer
		p.display.reset();		// Initialize displays

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
	 * Create a field from each media folder in library
	 */
	void createFieldsFromFolders(ArrayList<String> folders)
	{
		fields = new ArrayList<WMV_Field>();			// Initialize fields array
		int count = 0;
		
		for(String s : folders)
		{
			fields.add(new WMV_Field(settings, state, viewer.getSettings(), viewer.getState(), p.debugSettings, s, count));
			count++;
		}
	}
	
	/**
	 * Transition alpha from current to given value
	 */
	void fadeAlpha(float target)
	{
		if(target != state.alpha)			// Check if already at target
		{
			state.beginFadingAlpha = true;
			state.fadingAlpha = true;   
			state.fadingAlphaStart = state.alpha;
			state.fadingAlphaTarget = target;
			state.fadingAlphaStartFrame = state.frameCount;
			state.fadingAlphaEndFrame = state.fadingAlphaStartFrame + state.fadingAlphaLength;
		}
		else
		{
			state.fadingAlpha = false;
		}
	}
	
	/**
	 * Update alpha each frame
	 */
	void updateFadingAlpha()
	{
		float newAlphaFadeValue = 0.f;

		if(state.beginFadingAlpha)
		{
			state.fadingAlphaStartFrame = state.frameCount;					
			state.fadingAlphaEndFrame = state.frameCount + state.fadingAlphaLength;	
			state.beginFadingAlpha = false;
		}

		if (state.frameCount >= state.fadingAlphaEndFrame)
		{
			state.fadingAlpha = false;
			newAlphaFadeValue = state.fadingAlphaTarget;
		} 
		else
		{
			newAlphaFadeValue = PApplet.map(state.frameCount, state.fadingAlphaStartFrame, state.fadingAlphaEndFrame, state.fadingAlphaStart, state.fadingAlphaTarget);      // Fade with distance from current time
		}

		state.alpha = newAlphaFadeValue;
	}
	
	public WMV_WorldSettings getSettings()
	{
		return settings;
	}

	public WMV_WorldState getState()
	{
		return state;
	}
	
	/**
	 * @return Current field
	 */
	public WMV_Field getCurrentField()
	{
		WMV_Field f = fields.get(viewer.getState().getField());
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
		WMV_Model m = getCurrentField().getModel();
		return m;
	}
	
	/**
	 * @return The current cluster
	 */
	public WMV_Cluster getCurrentCluster()
	{
		int cluster = viewer.getState().getCurrentClusterID();
		if(cluster >= 0 && cluster < getCurrentField().getClusters().size())
		{
			WMV_Cluster c = getCurrentField().getCluster(cluster);
			return c;
		}
		else return null;
	}
	
	/**
	 * @return All images in current field
	 */
	public ArrayList<WMV_Image> getFieldImages()
	{
		ArrayList<WMV_Image> iList = getCurrentField().getImages();
		return iList;
	}
	
	/**
	 * @return All panoramas in field
	 */
	public ArrayList<WMV_Panorama> getFieldPanoramas()
	{
		ArrayList<WMV_Panorama> pList = getCurrentField().getPanoramas();
		return pList;
	}
	
	/**
	 * @return All videos in current field
	 */
	public ArrayList<WMV_Video> getFieldVideos()
	{
		ArrayList<WMV_Video> iList = getCurrentField().getVideos();
		return iList;
	}
	
	/**
	 * @return All clusters in current field
	 */
	public ArrayList<WMV_Cluster> getFieldClusters()
	{
		ArrayList<WMV_Cluster> clusters = getCurrentField().getClusters();
		return clusters;
	}

	/**
	 * @return Active clusters in current field
	 */
	public ArrayList<WMV_Cluster> getActiveClusters()
	{
		ArrayList<WMV_Cluster> clusters = new ArrayList<WMV_Cluster>();

		for(WMV_Cluster c : getCurrentField().getClusters())
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

		for(int i : viewer.getNearClusters(-1, settings.maxClusterDistance))
		{
			WMV_Cluster c = getCurrentField().getCluster(i);
			if(c.isActive() && !c.isEmpty())
				clusters.add(c);
		}
		
		return clusters;
	}

	/**
	 * Manually move back in time
	 */
	void decrementTime()
	{
		state.currentTime -= settings.timeInc;
		if (state.currentTime < 0)
			state.currentTime = 0;
	}
	
	/**
	 * Manually move forward in time
	 */
	void incrementTime()
	{
		state.currentTime += settings.timeInc;
		if (state.currentTime > settings.timeCycleLength)
			state.currentTime = settings.timeCycleLength - 200;
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
		if(p.debugSettings.main)
			System.out.println("Will output image to disk.");
		p.state.export = true;
	}
	
	public void exportSelectedMedia()
	{
		List<Integer> selected = getCurrentField().getSelectedMedia(0);
		for(int i:selected)
		{
			InputStream is = null;
			OutputStream os = null;
			try {
				File imgFilePath = new File(getCurrentField().getImage(i).getFilePath());
				is = new FileInputStream(imgFilePath);
				os = new FileOutputStream(new File(outputFolder + "/" + getCurrentField().getImage(i).getName()));
				byte[] buffer = new byte[1024];
				int length;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
			} 
			catch(Throwable t)
			{
				System.out.println("ERROR 1 in exportSelectedImages:"+t);
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
					System.out.println("ERROR 2 in exportSelectedImages:"+e);
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
	 * Import media and create new library by detecting fields using k-means clustering
	 * @param mediaFolder Folder containing the media
	 */
	public void createLibrary(String mediaFolder)
	{
		fields = new ArrayList<WMV_Field>();			// Initialize fields array
		
//		WMV_Field largeField = createLargeFieldFromFolder(mediaFolder);
//		fields = divideField(largeField, 3000.f, 15000.f);			
		
//		ArrayList<GMV_Cluster> clusters;		
//		int count = 0;
//		for(String s : clusters)
//		{
//			fields.add(new GMV_Field(this, s, count));
//			count++;
//		}
//		System.out.println("Created "+getCurrentField().clusters.size()+"fields from "+xxx+" clusters...");
	}
	
	/**
	 * Determine and set the length of the Main Time Cycle
	 */
	public void createTimeCycle()
	{
		if(state.timeMode == 0 || state.timeMode == 1)
		{
			settings.timeCycleLength = settings.defaultTimeCycleLength;
		}
		else if(state.timeMode == 2)
		{
			ArrayList<WMV_Cluster> cl = getVisibleClusters();
			settings.timeCycleLength = 0;
			
			/* Time cycle length is flexible according to visible cluster media lengths */
			for(WMV_Cluster c : cl)
			{
				settings.timeCycleLength += c.getImages( getCurrentField().getImages() ).size() * settings.defaultMediaLength;
				settings.timeCycleLength += c.getPanoramas( getCurrentField().getPanoramas() ).size() * settings.defaultMediaLength;
				for(WMV_Video v: c.getVideos( getCurrentField().getVideos()) )
					settings.timeCycleLength += PApplet.round( v.getLength() * 29.98f );
//				for(WMV_Sound s: c.getSounds( getCurrentField().getSounds() )
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
		else if(state.timeMode == 3)						/* Time cycle length is flexible according to visible cluster timelines */
		{
			float highest = -100000.f;
			float lowest = 100000.f;
			ArrayList<WMV_Cluster> cl = getVisibleClusters();
			
			for(WMV_Cluster c : cl)
			{
				float low = c.getTimeline().get(0).getLower().getTime();
				if(low < lowest)
					lowest = low;
				float high = c.getTimeline().get(c.getTimeline().size()-1).getUpper().getTime();
				if(high > highest)
					highest = high;
			}
			
//			float val = PApplet.map(highest - lowest, 0.f, 1.f, 0.f, settings.defaultMediaLength);
			if(cl.size() == 0)
				settings.timeCycleLength = -1;
		}
	}
	
	/**
	 * @param newTimeMode New time mode (0: Cluster, 1:Field, 2: Media)
	 */
	public void setTimeMode(int newTimeMode)
	{
		state.timeMode = newTimeMode;
		
		if(state.timeMode == 2)
			createTimeCycle();
		
		if(p.display.window.setupTimeWindow)
		{
			switch(state.timeMode)
			{
				case 0:
					p.display.window.optClusterTimeMode.setSelected(true);
					p.display.window.optFieldTimeMode.setSelected(false);
					p.display.window.optMediaTimeMode.setSelected(false);
					break;
				case 1:
					p.display.window.optClusterTimeMode.setSelected(false);
					p.display.window.optFieldTimeMode.setSelected(true);
					p.display.window.optMediaTimeMode.setSelected(false);
					break;
				case 2:
					p.display.window.optClusterTimeMode.setSelected(false);
					p.display.window.optFieldTimeMode.setSelected(false);
					p.display.window.optMediaTimeMode.setSelected(true);
					break;
			}		
		}
	}
	
	/**
	 * Show any image in field if visible
	 */
	public void showImages()
	{
		if(p.display.window.setupGraphicsWindow)
			p.display.window.chkbxHideImages.setSelected(false);
	}
	
	/**
	 * Hide all images in field
	 */
	public void hideImages()
	{
		for(WMV_Image i : getCurrentField().getImages())
		{
			if(i.getMediaState().visible)
			{
				if(i.isFading()) i.stopFading();
				i.fadeOut();
			}
		}

		if(p.display.window.setupGraphicsWindow)
			p.display.window.chkbxHideImages.setSelected(true);
	}
	
	/** 
	 * Show any panorama in field if visible
	 */
	public void showPanoramas()
	{
		if(p.display.window.setupGraphicsWindow)
			p.display.window.chkbxHidePanoramas.setSelected(false);
	}
	
	/** 
	 * Hide all panoramas in field
	 */
	public void hidePanoramas()
	{
		for(WMV_Panorama n : getCurrentField().getPanoramas())
		{
			if(n.getMediaState().visible)
			{
				if(n.isFading()) n.stopFading();
				n.fadeOut();
			}
		}
		
		for(WMV_Cluster c : getCurrentField().getClusters())
		{
			if(c.stitched.size() > 0)
			{
				for(WMV_Panorama n : c.stitched)
				{
					if(n.isFading()) n.stopFading();
					n.fadeOut();
				}
			}
		}
		
		if(p.display.window.setupGraphicsWindow)
			p.display.window.chkbxHidePanoramas.setSelected(true);
	}
	
	/**
	 * Show any video in field if visible
	 */
	public void showVideos()
	{
		if(p.display.window.setupGraphicsWindow)
			p.display.window.chkbxHideVideos.setSelected(false);
	}
	
	/**
	 * Hide all videos in field
	 */
	public void hideVideos()
	{
		for(WMV_Video v : getCurrentField().getVideos())
		{
			if(v.getMediaState().visible)
			{
				if(v.isFading()) v.stopFading();
				v.fadeOut();
			}
		}
		
		if(p.display.window.setupGraphicsWindow)
			p.display.window.chkbxHideVideos.setSelected(true);
	}
	
	/**
	 * Deselect all media in field
	 */
	public void deselectAllMedia(boolean hide) 
	{
		getCurrentField().deselectAllMedia(hide);
		p.display.clearMetadata();
	}

	/**
	 * Change all clusters to non-attractors
	 */
	public void clearAllAttractors()
	{
		if(p.debugSettings.viewer && p.debugSettings.detailed)
			System.out.println("Clearing all attractors...");

		if(viewer.getAttractorClusterID() != -1)
			viewer.clearAttractorCluster();

		getCurrentField().clearAllAttractors();
	}
	
	
	/**
	 * @param dist Grid spacing
	 */
	public void drawGrid(float dist) 
	{
		WMV_ModelState m = getCurrentModel().getState();
		for (float y = 0; y < m.fieldHeight / 2; y += dist) {
			for (float x = 0; x < m.fieldWidth / 2; x += dist) {
				for (float z = 0; z < m.fieldLength / 2; z += dist) {
					p.stroke(50, 150, 250);
					p.strokeWeight(1);
					p.pushMatrix();
					p.translate(x, y, z);
					p.box(2);
					p.popMatrix();
				}
			}
		}
	}

	public void setBlurMask(WMV_Image image, int blurMaskID)
	{
		WMV_Field f = getCurrentField();
		switch(blurMaskID)
		{
		case 0:
			f.setImageBlurMask(image, blurMaskLeftTop);
			break;
		case 1:
			f.setImageBlurMask(image, blurMaskLeftCenter);
			break;
		case 2:
			f.setImageBlurMask(image, blurMaskLeftBottom);
			break;
		case 3:
			f.setImageBlurMask(image, blurMaskLeftBoth);
			break;
		
		case 4:
			f.setImageBlurMask(image, blurMaskCenterTop);
			break;
		case 5:
			f.setImageBlurMask(image, blurMaskCenterCenter);
			break;
		case 6:
			f.setImageBlurMask(image, blurMaskCenterBottom);
			break;
		case 7:
			f.setImageBlurMask(image, blurMaskCenterBoth);
			break;
	
		case 8:
			f.setImageBlurMask(image, blurMaskRightTop);
			break;
		case 9:
			f.setImageBlurMask(image, blurMaskRightCenter);
			break;
		case 10:
			f.setImageBlurMask(image, blurMaskRightBottom);
			break;
		case 11:
			f.setImageBlurMask(image, blurMaskRightBoth);
			break;
	
		case 12:
			f.setImageBlurMask(image, blurMaskBothTop);
			break;
		case 13:
			f.setImageBlurMask(image, blurMaskBothCenter);
			break;
		case 14:
			f.setImageBlurMask(image, blurMaskBothBottom);
			break;
		case 15:
			f.setImageBlurMask(image, blurMaskBothBoth);
			break;
		}
	}
	
	public void setBlurMasks()
	{
		WMV_Field f = getCurrentField();
		for(WMV_Image image : f.getImages())
		{
			int bmID = image.getState().blurMaskID;
			setBlurMask(image, bmID);
		}
	}

	/**
	 * Load image masks
	 */
	public void loadImageMasks()
	{
		String maskPath = "masks/";
		File maskFolder = new File(maskPath);
		String[] maskFolderList = maskFolder.list();
		
		if(maskFolder.list() == null)
		{
			System.out.println("Masks folder is empty!");
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
	}
	
	public String getDataFolder()
	{
		return p.library.getLibraryFolder() + getCurrentField().getName() + "/data/";
	}
	
	public void setSettings(WMV_WorldSettings newSettings)
	{
		settings = newSettings;
	}
	
	public void setState(WMV_WorldState newState)
	{
		state = newState;
	}
}
