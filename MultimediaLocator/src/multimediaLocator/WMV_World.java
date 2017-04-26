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
import processing.data.IntList;
//import processing.core.PVector;
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
	public PImage blurMaskLeftTop, blurMaskLeftCenter, blurMaskLeftBottom, blurMaskLeftBoth;  	// Blur masks
	public PImage blurMaskCenterTop, blurMaskCenterCenter, blurMaskCenterBottom, blurMaskCenterBoth;
	public PImage blurMaskRightTop, blurMaskRightCenter, blurMaskRightBottom, blurMaskRightBoth;
	public PImage blurMaskBothTop, blurMaskBothCenter, blurMaskBothBottom, blurMaskBothBoth;
	public PImage videoBlurMaskLeftTop, videoBlurMaskLeftCenter, videoBlurMaskLeftBottom, videoBlurMaskLeftBoth;  	// Blur masks
	public PImage videoBlurMaskCenterTop, videoBlurMaskCenterCenter, videoBlurMaskCenterBottom, videoBlurMaskCenterBoth;
	public PImage videoBlurMaskRightTop, videoBlurMaskRightCenter, videoBlurMaskRightBottom, videoBlurMaskRightBoth;
	public PImage videoBlurMaskBothTop, videoBlurMaskBothCenter, videoBlurMaskBothBottom, videoBlurMaskBothBoth;

	/* Interpolation */
	ScaleMap distanceFadeMap, timeFadeMap;
	InterpolateStrategy circularEaseOut = new CircularInterpolation(false);		// Steepest ascent at beginning
	InterpolateStrategy circularEaseIn = new CircularInterpolation(true);		// Steepest ascent toward end value
	InterpolateStrategy zoomLens = new ZoomLensInterpolation();
	InterpolateStrategy linear = new LinearInterpolation();
	
	/* File System */
	public String outputFolder;
	public boolean outputFolderSelected = false;

	/* Debugging */
	public boolean drawForceVector = true;				// Show attraction vector on map (mostly for debugging)

	MultimediaLocator p;								// Parent App
	
	/**
	 * Constructor for world object
	 * @param parent Parent App
	 */
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
		if(p.debugSettings.main && p.debugSettings.detailed) System.out.println("Initializing WMV_World...");

		/* Create main classes */
		settings = new WMV_WorldSettings();
		state = new WMV_WorldState();
		viewer = new WMV_Viewer(this, settings, state, p.debugSettings);			// Initialize navigation + viewer
		
		timeFadeMap = new ScaleMap(0., 1., 0., 1.);				// Fading with time interpolation
		timeFadeMap.setMapFunction(circularEaseOut);

		distanceFadeMap = new ScaleMap(0., 1., 0., 1.);			// Fading with distance interpolation
		distanceFadeMap.setMapFunction(circularEaseIn);
	}

	/**
	 * Enter given field
	 * @param fieldIdx The field to enter
	 * @param moveToFirstTimeSegment Whether to move to first time segment after entering
	 */
	void enter(int fieldIdx, boolean moveToFirstTimeSegment)
	{
		viewer.enterField( fieldIdx );								// Update navigation
		viewer.updateState(settings, state);
		if(moveToFirstTimeSegment)
			viewer.moveToFirstTimeSegment(false);
		viewer.updateNavigation();									// Update navigation

		p.enteredField = true;
		state.waitingToFadeInTerrainAlpha = true;
	}
	
	/**
	 * Display the current field in 3D view
	 */
	void display3D()
	{
		/* 3D Display */
		updateViewerAttraction();									// Attract the viewer
		
		if(p.display.displayView == 0)
		{
			p.hint(PApplet.ENABLE_DEPTH_TEST);				// Enable depth testing for drawing 3D graphics
			p.background(0.f);								// Set background
			getCurrentField().display(p);					// Display media in current field
			if(settings.showUserPanoramas || settings.showStitchedPanoramas)
			{
				ArrayList<WMV_Cluster> clusters = getCurrentField().getClusters();
				if(clusters.size()>0 && viewer.getState().getCurrentClusterID() < clusters.size())
					clusters.get(viewer.getState().getCurrentClusterID()).displayUserPanoramas(p);		// Draw current cluster
			}

		}
		
		if(state.displayTerrain)						// Draw terrain as wireframe grid
			displayTerrain();
		
		viewer.updateNavigation();					// Update navigation
		if(p.display.displayView == 0)	
			if(p.state.running)
				viewer.show();						// Show the 3D view to the viewer
	}

	/**
	 * Display 2D text and graphics
	 */
	void display2D()
	{
		/* 2D Display */
		p.display.display(this);										// Draw 2D display after 3D graphics
		
		if(state.fadingAlpha) updateFadingAlpha();			// Fade alpha	-- Obsolete??
		if(state.fadingTerrainAlpha) updateFadingTerrainAlpha();			// Fade grid

		if(viewer.getSettings().mouseNavigation)
			input.updateMouseNavigation(viewer, p.mouseX, p.mouseY, p.frameCount);
	}

	/**
	 * Update viewer attraction to attracting cluster(s)
	 */
	public void updateViewerAttraction()
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
							if(p.debugSettings.main && p.debugSettings.detailed) System.out.println("Media still active...");
						}
						else
						{
							state.currentTime = 0;
							if(p.debugSettings.main && p.debugSettings.detailed) System.out.println("Reached end of day at frameCount:"+state.frameCount);
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
							setMediaTimeModeCurrentMedia(viewer.getCurrentMedia() + 1);		
						}
						else
						{
							if(p.debugSettings.main)
								System.out.println("Reached end of last media with "+(settings.timeCycleLength - state.currentTime)+ " frames to go...");
							state.currentTime = 0;
							startMediaTimeModeCycle();
						}
					}
					
					if(state.currentTime > settings.timeCycleLength)
					{
						state.currentTime = 0;
						startMediaTimeModeCycle();
					}
				}
				break;
				
			case 3:													// Flexible Time Mode
				break;
		}
	}
	

	/**
	 * Update the viewer and media about the world state
	 */
	void updateState()
	{
		state.frameCount = p.frameCount;
		viewer.updateState(settings, state);
		getCurrentField().update(settings, state, viewer.getSettings(), viewer.getState());				// Update clusters in current field
	}

	/**
	 * Begin cycle in Media Time Mode (2)
	 */
	void startMediaTimeModeCycle()
	{
		setMediaTimeModeCurrentMedia(0);
	}
	
	/**
	 * Set current visible media in Media Time Mode
	 * @param timeSegmentIndex
	 */
	void setMediaTimeModeCurrentMedia(int timeSegmentIndex)
	{
		viewer.setCurrentMedia( timeSegmentIndex );
		if(viewer.getNearbyClusterTimeline().size() > 0)
		{
			WMV_Time time = viewer.getNearbyTimeByIndex(timeSegmentIndex);
			
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
				System.out.println("ERROR in setSingleTimeModeCurrentMedia... time == null!!... timelineIndex:"+timeSegmentIndex);
			}
		}
		else
			System.out.println("ERROR in setSingleTimeModeCurrentMedia  viewer.nearbyClusterTimeline.size() == 0!!");
	}
	
	public void updateAllMediaSettings()
	{
		WMV_Field f = getCurrentField();
		for(WMV_Image img : f.getImages())
			img.updateSettings(settings, state, viewer.getSettings(), viewer.getState());
		for(WMV_Panorama pano : f.getPanoramas())
			pano.updateSettings(settings, state, viewer.getSettings(), viewer.getState());
		for(WMV_Video vid : f.getVideos())
			vid.updateSettings(settings, state, viewer.getSettings(), viewer.getState());
//		for(WMV_Sound snd : f.getSounds())
//			img.updateSettings(world.settings, world.state, world.viewer.getSettings(), world.viewer.getState(), debugSettings);
	}

	
	/**
	 * Draw terrain as wireframe grid
	 */
	private void displayTerrain()
	{
		ArrayList<ArrayList<PVector>> gridPoints = new ArrayList<ArrayList<PVector>>();		// Points to draw
		PVector vLoc = viewer.getLocation();
		PVector gridLoc = vLoc;
		gridLoc = new PVector(utilities.round(vLoc.x, 0), vLoc.y, utilities.round(vLoc.z, 0));
		if((int)gridLoc.x % 2 == 0) gridLoc.x++;
		if((int)gridLoc.z % 2 == 0) gridLoc.z++;

		float gridSize = settings.defaultFocusDistance * 5.f;
		float gridHeight = settings.defaultFocusDistance * 0.9f;
		float defaultHeight = gridLoc.y + gridHeight;				// -- Get this from media points!	
		
		for(int x=0; x<gridSize; x+=2)
		{
			ArrayList<PVector> row = new ArrayList<PVector>();
//			for(int z=0; z<25; z++)
			for(int z=0; z<gridSize; z+=2)
			{
				float xStart = gridLoc.x - gridSize * 0.5f;
				float zStart = gridLoc.z - gridSize * 0.5f;
				float xEnd = gridLoc.x + gridSize * 0.5f;
				float zEnd = gridLoc.z + gridSize * 0.5f;

				PVector pLoc = new PVector(0,defaultHeight,0);
				pLoc.x = utilities.mapValue(x, 0, gridSize, xStart, xEnd);
				pLoc.z = utilities.mapValue(z, 0, gridSize, zStart, zEnd);

				row.add(pLoc);
			}
			gridPoints.add(row);
		}

//		IntList nearClusters = viewer.getNearClusters(-1, gridSize * 0.5f);		// --Not precise enough?
//		for(int i : nearClusters)	// Adjust points within cluster viewing distance to cluster height
		for(WMV_Cluster c : getCurrentField().getClusters())	// Adjust points within cluster viewing distance to cluster height
		{
			PVector cLoc = c.getLocation();
			for(ArrayList<PVector> row : gridPoints)
			{
				for(PVector pv : row)
				{
					if(pv.dist(c.getLocation()) < settings.defaultFocusDistance * 1.5f)
					{
						pv.y = cLoc.y + gridHeight;
					}
				}
			}
		}

		int row = 0;
		int col;
		for(ArrayList<PVector> pvList : gridPoints)
		{
			col = 0;
			for(PVector pv : pvList)
			{
				if(pv.y == defaultHeight)
					p.stroke(0.f, 0.f, 155.f, state.terrainAlpha * 0.33f);
				else
					p.stroke(0.f, 0.f, 255.f, state.terrainAlpha);

				p.strokeWeight(6.f);
				p.point(pv.x, pv.y, pv.z);				
				
				p.strokeWeight(1.f);
				if(col-1 >= 0)
				{
					PVector pt2 = gridPoints.get(row).get(col-1);
					p.line(pv.x, pv.y, pv.z, pt2.x, pt2.y, pt2.z);
				}
				if(col+1 < pvList.size())
				{
					PVector pt2 = gridPoints.get(row).get(col+1);
					p.line(pv.x, pv.y, pv.z, pt2.x, pt2.y, pt2.z);
				}
				if(row-1 >= 0)
				{
					PVector pt2 = gridPoints.get(row-1).get(col);
					p.line(pv.x, pv.y, pv.z, pt2.x, pt2.y, pt2.z);
				}
				if(row+1 < gridPoints.size())
				{
					PVector pt2 = gridPoints.get(row+1).get(col);
					p.line(pv.x, pv.y, pv.z, pt2.x, pt2.y, pt2.z);
				}
				
				col++;
			}
			
			row++;
		}
	}

	/**
	 * Stop any currently playing videos
	 */
	public void stopAllVideos()
	{
		for(int i=0;i<getCurrentField().getVideos().size();i++)
		{
			if(getCurrentField().getVideo(i) != null && getCurrentField().getVideo(i).video != null)
				getCurrentField().getVideo(i).stopVideo();
		}
	}

	public void fadeOutAllMedia()
	{
		getCurrentField().fadeOutAllMedia();
		fadeOutTerrain(false);
	}
	
	public void fadeOutTerrain(boolean turnOff)
	{
		if(state.terrainAlpha != 0.f)
		{
			state.fadingTerrainAlpha = true;		
			state.fadingTerrainStart = state.terrainAlpha;
			state.fadingTerrainTarget = 0.f;
			state.fadingTerrainStartFrame = p.frameCount;
			state.fadingTerrainEndFrame = p.frameCount + state.fadingTerrainLength; 
			state.turnOffTerrainAfterFadingOut = true;
		}
	}

	public void fadeInTerrain()
	{
		if(state.terrainAlpha != state.terrainAlphaMax)
		{
			state.fadingTerrainAlpha = true;		
			state.fadingTerrainStart = state.terrainAlpha;
			state.fadingTerrainTarget = state.terrainAlphaMax;
			state.fadingTerrainStartFrame = p.frameCount;
			state.fadingTerrainEndFrame = p.frameCount + state.fadingTerrainLength; 
			state.waitingToFadeInTerrainAlpha = false;
		}
	}

	/**
	 * Update state.fadingBrightness each frame
	 */
	void updateFadingTerrainAlpha()
	{
		float newFadeValue = 0.f;
		
		if (p.frameCount >= state.fadingTerrainEndFrame)
		{
			state.fadingTerrainAlpha = false;
			newFadeValue = state.fadingTerrainTarget;
			if(newFadeValue == 0.f)
			{
				if(state.turnOffTerrainAfterFadingOut)
					state.displayTerrain = false;
				else
					state.waitingToFadeInTerrainAlpha = true;
			}
		} 
		else
		{
			newFadeValue = PApplet.map(p.frameCount, state.fadingTerrainStartFrame, state.fadingTerrainEndFrame, 
					state.fadingTerrainStart, state.fadingTerrainTarget);  	    // Fade with distance from current time
		}

		state.terrainAlpha = newFadeValue;
	}


	/**
	 * Save the current world, field and viewer states and settings to file
	 */
	public void saveSimulationState()
	{
		String folderPath = p.library.getDataFolder(getCurrentField().getID());
		String clusterDataPath = folderPath + "ml_library_clusterStates/";
		String imageDataPath = folderPath + "ml_library_imageStates/";
		String panoramaDataPath = folderPath + "ml_library_panoramaStates/";
		String videoDataPath = folderPath + "ml_library_videoStates/";
		String soundDataPath = folderPath + "ml_library_soundStates/";
		
		File dataDirectory = new File(folderPath);
		if(!dataDirectory.exists()) dataDirectory.mkdir();			// Create directory if doesn't exist

		File clusterDirectory = new File(clusterDataPath);
		if(!clusterDirectory.exists()) clusterDirectory.mkdir();			// Create directory if doesn't exist

		File imageDirectory = new File(imageDataPath);
		if(!imageDirectory.exists()) imageDirectory.mkdir();			// Create directory if doesn't exist

		File panoramaDirectory = new File(panoramaDataPath);
		if(!panoramaDirectory.exists()) panoramaDirectory.mkdir();			// Create directory if doesn't exist

		File videoDirectory = new File(videoDataPath);
		if(!videoDirectory.exists()) videoDirectory.mkdir();			// Create directory if doesn't exist

		if(p.debugSettings.main)
			PApplet.println("Saving Simulation State to: "+folderPath);
		
		WMV_Field f = getCurrentField();
		f.captureState();											// Capture current state, i.e. save timeline and dateline

		WMV_ClusterStateList csl = f.captureClusterStates();
		WMV_ImageStateList isl = f.captureImageStates();
		WMV_PanoramaStateList psl = f.capturePanoramaStates();
		WMV_VideoStateList vsl = f.captureVideoStates();
//		WMV_SoundStateList ssl = f.captureSoundStates();
		
		p.library.saveWorldSettings(settings, folderPath+"ml_library_worldSettings.json");
		p.library.saveWorldState(state, folderPath+"ml_library_worldState.json");
		p.library.saveViewerSettings(viewer.getSettings(), folderPath+"ml_library_viewerSettings.json");
		p.library.saveViewerState(viewer.getState(), folderPath+"ml_library_viewerState.json");
		p.library.saveFieldState(f.getState(), folderPath+"ml_library_fieldState.json");
		p.library.saveClusterStateList(csl, clusterDataPath+"ml_library_clusterStates.json");
		p.library.saveImageStateList(isl, imageDataPath+"ml_library_imageStates.json");
		p.library.savePanoramaStateList(psl, panoramaDataPath+"ml_library_panoramaStates.json");
		p.library.saveVideoStateList(vsl, videoDataPath+"ml_library_videoStates.json");
	}


	/**
	 * Save the current world, field and viewer states and settings to file
	 */
	void saveAllSimulationStates()
	{
		for(WMV_Field f : fields)
		{
			String folderPath = p.library.getDataFolder(f.getID());
			String clusterDataPath = folderPath + "ml_library_clusterStates/";
			String imageDataPath = folderPath + "ml_library_imageStates/";
			String panoramaDataPath = folderPath + "ml_library_panoramaStates/";
			String videoDataPath = folderPath + "ml_library_videoStates/";
			String soundDataPath = folderPath + "ml_library_soundStates/";
			
			File dataDirectory = new File(folderPath);
			if(dataDirectory.exists()) 
			{
				File[] fileList = dataDirectory.listFiles();
				for(File file : fileList) 
				{
					if(file.isDirectory())
					{
						File[] fileList2 = file.listFiles();
						for(File file2 : fileList2) 
						{
							if(file2.isDirectory())
							{
								File[] fileList3 = file2.listFiles();
								for(File file3 : fileList3) 
									file3.delete();
							}
							else
							{
								file2.delete();
							}
						}
					}
					else
					{
						file.delete();
					}
				}
			}
			else
			{
				dataDirectory.mkdir();			// Create directory if doesn't exist
			}
			
			File clusterDirectory = new File(clusterDataPath);
			if(!clusterDirectory.exists()) 
				clusterDirectory.mkdir();			// Create directory if doesn't exist

			File imageDirectory = new File(imageDataPath);
			if(!imageDirectory.exists()) 
				imageDirectory.mkdir();			// Create directory if doesn't exist

			File panoramaDirectory = new File(panoramaDataPath);
			if(!panoramaDirectory.exists()) 
				panoramaDirectory.mkdir();			// Create directory if doesn't exist

			File videoDirectory = new File(videoDataPath);
			if(!videoDirectory.exists()) 
				videoDirectory.mkdir();			// Create directory if doesn't exist

			if(p.debugSettings.main) PApplet.println("Saving Simulation State to: "+folderPath);
			
			f.captureState();											// Capture current state, i.e. save timeline and dateline

			WMV_ClusterStateList csl = f.captureClusterStates();
			WMV_ImageStateList isl = f.captureImageStates();
			WMV_PanoramaStateList psl = f.capturePanoramaStates();
			WMV_VideoStateList vsl = f.captureVideoStates();
//			WMV_SoundStateList ssl = f.captureSoundStates();

			p.library.saveWorldSettings(settings, folderPath+"ml_library_worldSettings.json");
			p.library.saveWorldState(state, folderPath+"ml_library_worldState.json");
			p.library.saveViewerSettings(f.getViewerSettings(), folderPath+"ml_library_viewerSettings.json");
			p.library.saveViewerState(f.getViewerState(), folderPath+"ml_library_viewerState.json");
			p.library.saveFieldState(f.getState(), folderPath+"ml_library_fieldState.json");
			p.library.saveClusterStateList(csl, clusterDataPath+"ml_library_clusterStates.json");
			p.library.saveImageStateList(isl, imageDataPath+"ml_library_imageStates.json");
			p.library.savePanoramaStateList(psl, panoramaDataPath+"ml_library_panoramaStates.json");
			p.library.saveVideoStateList(vsl, videoDataPath+"ml_library_videoStates.json");
			
			if(p.debugSettings.main) System.out.println("Saved simulation state for field #"+f.getID());
		}
	}

	/**
	 * Load world, field and viewer states and settings from file
	 */
	public WMV_Field loadAndSetSimulationState(WMV_SimulationState newSimulationState, WMV_Field curField)
	{
		if(p.debugSettings.main && p.debugSettings.detailed)
			PApplet.println("Loading and setting Simulation State... Field #"+curField.getID());

		loadAndSetState(curField.getID());
		loadAndSetSettings(curField.getID());
		loadAndSetViewerState(curField.getID());
		loadAndSetViewerSettings(curField.getID());
		state.frameCount = p.frameCount;
		viewer.setFrameCount(p.frameCount);
		viewer.setCurrentFieldID(curField.getID());
		viewer.resetTimeState();
		
		/* Check world and viewer state/settings */
		if(p.debugSettings.main && p.debugSettings.detailed)
		{
			if(state != null) System.out.println("WorldState exists...");
			if(settings != null) System.out.println("WorldSettings exists...");
			if(viewer.getState() != null) System.out.println("ViewerState exists...");
			if(viewer.getSettings() != null) System.out.println("ViewerSettings exists...");
		}
		String fieldName = curField.getName();
		int fieldID = curField.getID();
		curField = new WMV_Field(settings, state, viewer.getSettings(), viewer.getState(), p.debugSettings, fieldName, fieldID);
		
		curField = loadFieldState(curField);
		curField.setID(fieldID);
		
		if(p.debugSettings.main && p.debugSettings.detailed)
			System.out.println("Loaded and Set Field State... Field #"+curField.getID()+" clusters:"+curField.getClusters().size());

		return curField;
	}
	
	/**
	 * Save the current world, field and viewer states and settings to file
	 */
	public WMV_Field loadSimulationState(WMV_SimulationState newSimulationState, WMV_Field curField)
	{
		PApplet.println("Loading Simulation State... Field #"+curField.getID());

		WMV_WorldState newWorldState = loadState(curField.getID());
		WMV_WorldSettings newWorldSettings = loadSettings(curField.getID());
		WMV_ViewerState newViewerState = loadViewerState(curField.getID());
		WMV_ViewerSettings newViewerSettings = loadViewerSettings(curField.getID());
		newWorldState.frameCount = p.frameCount;
//		newViewerState.frameCount = p.frameCount;

		/* Check world and viewer state/settings */
		if(p.debugSettings.main && p.debugSettings.detailed)
		{
			if(newWorldState != null) System.out.println("WorldState exists...");
			if(newWorldSettings != null) System.out.println("WorldSettings exists...");
			if(newViewerState != null) System.out.println("ViewerState exists...");
			if(newViewerSettings != null) System.out.println("ViewerSettings exists...");
		}
		
		String fieldName = curField.getName();
		int fieldID = curField.getID();
		curField = new WMV_Field(newWorldSettings, newWorldState, newViewerSettings, newViewerState, p.debugSettings, fieldName, fieldID);
		curField= loadFieldState(curField);
		curField.setID(fieldID);
		
		if(p.debugSettings.main && p.debugSettings.detailed)
			System.out.println("Loaded Field State... Field #"+curField.getID()+" clusters:"+curField.getClusters().size());

		return curField;
	}

	void setSimulationStateFromField(WMV_Field field)
	{
		setState(field.getWorldState());
		setSettings(field.getWorldSettings());
		viewer.setState(field.getViewerState());
		viewer.setSettings(field.getViewerSettings());
		
		state.frameCount = p.frameCount;
		viewer.setFrameCount(p.frameCount);
		if(field.getID() < fields.size())
		{
//			System.out.println("Will set viewer current field ID to:"+field.getID()+" fields.size():"+fields.size());
			viewer.setCurrentFieldID(field.getID());
		}
		else
		{
			if(fields.size() == 1)
			{
				field.setID(0);
				viewer.setCurrentFieldID(0);
//				if(p.debugSettings.main) System.out.println("Loading single field of a library... set field ID to:"+field.getID()+" fields.size():"+fields.size());
			}
			else
			{
				System.out.println("Error in setting field ID... field.getID():"+field.getID()+" fields.size():"+fields.size());
				p.exit();
			}
		}

		viewer.resetTimeState();

		/* Check world and viewer state/settings */
		if(p.debugSettings.main)
			System.out.println("Set Simulation State from Field #"+field.getID());
		
		if(p.debugSettings.main && p.debugSettings.detailed)
		{
			if(state != null) System.out.println("WorldState exists...");
			if(settings != null) System.out.println("WorldSettings exists...");
			if(viewer.getState() != null) System.out.println("ViewerState exists...");
			if(viewer.getSettings() != null) System.out.println("ViewerSettings exists...");
			System.out.println("Viewer.state.currentCluster:"+viewer.getState().currentCluster+" out of: "+getCurrentField().getClusters().size());
		}
		
		updateState();
		getCurrentField().updateAllMediaSettings();
	}

	public WMV_Field loadFieldState(WMV_Field field)
	{
		String dataFolderPath = p.library.getDataFolder(field.getID());
		String clusterDataPath = dataFolderPath + "ml_library_clusterStates/";
		String imageDataPath = dataFolderPath + "ml_library_imageStates/";
		String panoramaDataPath = dataFolderPath + "ml_library_panoramaStates/";
		String videoDataPath = dataFolderPath + "ml_library_videoStates/";
//		String soundDataPath = dataFolderPath + "ml_library_soundStates/";

		WMV_ClusterStateList csl = p.library.loadClusterStateLists(clusterDataPath);
		
//		for(WMV_ClusterState cs : csl.clusters)
//		{
//			if(cs.dateline != null)
//			{
//				for(WMV_Date d : cs.dateline)
//				{
//					if(d.dateTime == null)
//						System.out.println("d.dateTime == null!");
//				}
//			}
//			else
//				System.out.println("cs.dateline == null!");
//			if(cs.timeline != null)
//			{
//			}
//			else
//				System.out.println("cs.timeline == null!");
//			if(cs.timelines != null)
//			{
//			}
//			else
//				System.out.println("cs.timelines == null!");
//		}
		
		WMV_ImageStateList isl = p.library.loadImageStateLists(imageDataPath);
		WMV_PanoramaStateList psl = p.library.loadPanoramaStateList(panoramaDataPath+"ml_library_panoramaStates.json");
		WMV_VideoStateList vsl = p.library.loadVideoStateList(videoDataPath+"ml_library_videoStates.json");
//		WMV_SoundStateList ssl = p.library.loadSoundStateList(soundDataPath+"ml_library_soundStates.json");

		field.setState(p, p.library.loadFieldState(dataFolderPath+"ml_library_fieldState.json"), csl, isl, psl, vsl);
		return field;
	}

	public void loadAndSetSettings(int fieldID)
	{
		String dataFolder = p.library.getDataFolder(fieldID);
		setSettings(p.library.loadWorldSettings(dataFolder+"ml_library_worldSettings.json"));
	}

	public void loadAndSetState(int fieldID)
	{
		String dataFolder = p.library.getDataFolder(fieldID);
		setState(p.library.loadWorldState(dataFolder+"ml_library_worldState.json"));
	}

	public void loadAndSetViewerSettings(int fieldID)
	{
		String dataFolder = p.library.getDataFolder(fieldID);
		viewer.setSettings(p.library.loadViewerSettings(dataFolder+"ml_library_viewerSettings.json"));
	}

	public void loadAndSetViewerState(int fieldID)
	{
		String dataFolder = p.library.getDataFolder(fieldID);
		viewer.setState(p.library.loadViewerState(dataFolder+"ml_library_viewerState.json"));
	}

	public WMV_WorldSettings loadSettings(int fieldID)
	{
		String dataFolder = p.library.getDataFolder(fieldID);
		return p.library.loadWorldSettings(dataFolder+"ml_library_worldSettings.json");
	}

	public WMV_WorldState loadState(int fieldID)
	{
		String dataFolder = p.library.getDataFolder(fieldID);
		return p.library.loadWorldState(dataFolder+"ml_library_worldState.json");
	}

	public WMV_ViewerSettings loadViewerSettings(int fieldID)
	{
		String dataFolder = p.library.getDataFolder(fieldID);
		return p.library.loadViewerSettings(dataFolder+"ml_library_viewerSettings.json");
	}

	public WMV_ViewerState loadViewerState(int fieldID)
	{
		String dataFolder = p.library.getDataFolder(fieldID);
		return p.library.loadViewerState(dataFolder+"ml_library_viewerState.json");
	}

	/**
	 * Reset variables
	 */
	void reset(boolean system)
	{
		if(system)
		{
			p.state.initializationField = 0;			// Field to be initialized this frame
			p.state.startedRunning = false;				// Program just started running
			p.state.initialSetup = false;				// Performing initial setup 
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

		state.useBlurMasks = true;						// Blur image edges
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
	void createFieldsFromFolders(ArrayList<String> fieldFolders)
	{
		fields = new ArrayList<WMV_Field>();			// Initialize fields array
		int count = 0;
		
		for(String fieldFolder : fieldFolders)
		{
			fields.add(new WMV_Field(settings, state, viewer.getSettings(), viewer.getState(), p.debugSettings, fieldFolder, count));
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
		int cluster = viewer.getCurrentClusterID();
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
		if(p.debugSettings.main && p.debugSettings.detailed)
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
		{
			System.out.println("world.getField() Error: fieldIndex:"+fieldIndex+" fields.size():"+fields.size());
			return null;
		}
	}

	/**
	 * @param newField New field to set
	 * @param fieldIndex Field ID 
	 */
	public void setField(WMV_Field newField, int fieldIndex)
	{
		fields.set(fieldIndex, newField);
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
		else if(state.timeMode == 2)		/* Time cycle length is flexible according to visible cluster media lengths */
		{
			ArrayList<WMV_Cluster> cl = viewer.getVisibleClusters(getCurrentField());
			settings.timeCycleLength = 0;
			
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
			{
				if(cl.get(0).getTimeline().timeline.size() > 0)
					viewer.setNearbyClusterTimeline(cl.get(0).getTimeline().timeline);
				else
					System.out.println("CreateTimeCycle Error... Cluster #"+cl.get(0).getID()+"  getTimeline().size() == 0!");
			}
			else if(cl.size() > 1)
			{
				viewer.createNearbyClusterTimeline(cl);
			}
				
			if(cl.size() == 0)
				settings.timeCycleLength = -1;				// Flag for Viewer to keep calling this method until clusters are visible
			else
				startMediaTimeModeCycle();
		}
		else if(state.timeMode == 3)						/* Time cycle length is flexible according to visible cluster timelines */
		{
			float highest = -100000.f;
			float lowest = 100000.f;
			ArrayList<WMV_Cluster> cl = viewer.getVisibleClusters( getCurrentField() );
			
			for(WMV_Cluster c : cl)
			{
				float low = c.getTimeline().timeline.get(0).getLower().getTime();
				if(low < lowest)
					lowest = low;
				float high = c.getTimeline().timeline.get(c.getTimeline().timeline.size()-1).getUpper().getTime();
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
				case 0:														// Cluster
					p.display.window.optClusterTimeMode.setSelected(true);
					p.display.window.optFieldTimeMode.setSelected(false);
					p.display.window.optMediaTimeMode.setSelected(false);
					p.display.window.sdrTimeCycleLength.setValue(getCurrentCluster().getTimeCycleLength());
					if(!p.display.window.sdrTimeCycleLength.isVisible())
						p.display.window.sdrTimeCycleLength.setVisible(true);
					break;
				case 1:														// Field
					p.display.window.optClusterTimeMode.setSelected(false);
					p.display.window.optFieldTimeMode.setSelected(true);
					p.display.window.optMediaTimeMode.setSelected(false);
					p.display.window.sdrTimeCycleLength.setValue(settings.timeCycleLength);
					if(!p.display.window.sdrTimeCycleLength.isVisible())
						p.display.window.sdrTimeCycleLength.setVisible(true);
					break;
				case 2:														// Media
					p.display.window.optClusterTimeMode.setSelected(false);
					p.display.window.optFieldTimeMode.setSelected(false);
					p.display.window.optMediaTimeMode.setSelected(true);
					p.display.window.sdrTimeCycleLength.setVisible(false);
					break;
			}		
		}
	}
	
	public void setAllClustersTimeCycleLength(int newTimeCycleLength)
	{
		for(WMV_Cluster c : getCurrentField().getClusters())
		{
			if(!c.getState().empty)
			{
				c.setTimeCycleLength( newTimeCycleLength );

				c.updateAllMediaSettings(getCurrentField().getImages(), getCurrentField().getPanoramas(), getCurrentField().getVideos(),
						settings, state, viewer.getSettings(), viewer.getState());
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
//	public void displayGrid(float dist) 
//	{
//		WMV_ModelState m = getCurrentModel().getState();
//		for (float y = 0; y < m.fieldHeight / 2; y += dist) {
//			for (float x = 0; x < m.fieldWidth / 2; x += dist) {
//				for (float z = 0; z < m.fieldLength / 2; z += dist) {
//					p.stroke(50, 150, 250, state.fadingGridBrightness);
//					p.strokeWeight(1);
//					p.pushMatrix();
//					p.translate(x, y, z);
//					p.box(2);
//					p.popMatrix();
//				}
//			}
//		}
//	}

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

	public void setVideoBlurMask(WMV_Video video, int blurMaskID)
	{
		WMV_Field f = getCurrentField();
		switch(blurMaskID)
		{
		case 0:
			f.setVideoBlurMask(video, videoBlurMaskLeftTop);
			break;
		case 1:
			f.setVideoBlurMask(video, videoBlurMaskLeftCenter);
			break;
		case 2:
			f.setVideoBlurMask(video, videoBlurMaskLeftBottom);
			break;
		case 3:
			f.setVideoBlurMask(video, videoBlurMaskLeftBoth);
			break;
		
		case 4:
			f.setVideoBlurMask(video, videoBlurMaskCenterTop);
			break;
		case 5:
			f.setVideoBlurMask(video, videoBlurMaskCenterCenter);
			break;
		case 6:
			f.setVideoBlurMask(video, videoBlurMaskCenterBottom);
			break;
		case 7:
			f.setVideoBlurMask(video, videoBlurMaskCenterBoth);
			break;
	
		case 8:
			f.setVideoBlurMask(video, videoBlurMaskRightTop);
			break;
		case 9:
			f.setVideoBlurMask(video, videoBlurMaskRightCenter);
			break;
		case 10:
			f.setVideoBlurMask(video, videoBlurMaskRightBottom);
			break;
		case 11:
			f.setVideoBlurMask(video, videoBlurMaskRightBoth);
			break;
	
		case 12:
			f.setVideoBlurMask(video, videoBlurMaskBothTop);
			break;
		case 13:
			f.setVideoBlurMask(video, videoBlurMaskBothCenter);
			break;
		case 14:
			f.setVideoBlurMask(video, videoBlurMaskBothBottom);
			break;
		case 15:
			f.setVideoBlurMask(video, videoBlurMaskBothBoth);
			break;
		}
	}

	public void setBlurMasks()
	{
		for(WMV_Field f : fields)
		{
			for(WMV_Image image : f.getImages())
			{
				int bmID = image.getState().blurMaskID;
				setBlurMask(image, bmID);
			}
			for(WMV_Video video : f.getVideos())
			{
				int bmID = video.getState().blurMaskID;
				setVideoBlurMask(video, bmID);
			}
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
	
	/**
	 * Load video masks
	 */
	public void loadVideoMasks()
	{
		String maskPath = "video_masks/";
		File maskFolder = new File(maskPath);
		String[] maskFolderList = maskFolder.list();
		
		if(maskFolder.list() == null)
		{
			System.out.println("Video masks folder is empty!");
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
	
//	public String getDataFolder()
//	{
//		return p.library.getLibraryFolder() + "/" + getCurrentField().getName() + "/data/";
//	}
	
	public void setSettings(WMV_WorldSettings newSettings)
	{
		settings = newSettings;
	}
	
	public void setState(WMV_WorldState newState)
	{
		state = newState;
	}
}
