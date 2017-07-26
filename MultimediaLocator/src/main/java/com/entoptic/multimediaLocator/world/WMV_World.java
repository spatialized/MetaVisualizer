package main.java.com.entoptic.multimediaLocator.world;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import main.java.com.entoptic.multimediaLocator.MultimediaLocator;
import main.java.com.entoptic.multimediaLocator.gui.ML_Input;
import main.java.com.entoptic.multimediaLocator.media.WMV_Image;
import main.java.com.entoptic.multimediaLocator.media.WMV_ImageStateList;
import main.java.com.entoptic.multimediaLocator.media.WMV_Panorama;
import main.java.com.entoptic.multimediaLocator.media.WMV_PanoramaStateList;
import main.java.com.entoptic.multimediaLocator.media.WMV_Sound;
import main.java.com.entoptic.multimediaLocator.media.WMV_SoundStateList;
import main.java.com.entoptic.multimediaLocator.media.WMV_Video;
import main.java.com.entoptic.multimediaLocator.media.WMV_VideoStateList;
import main.java.com.entoptic.multimediaLocator.misc.WMV_Utilities;
import main.java.com.entoptic.multimediaLocator.model.WMV_Cluster;
import main.java.com.entoptic.multimediaLocator.model.WMV_ClusterStateList;
import main.java.com.entoptic.multimediaLocator.model.WMV_Model;
import main.java.com.entoptic.multimediaLocator.model.WMV_Time;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PVector;
//import processing.opengl.PGL;
//import processing.opengl.PShader;
//import processing.data.IntList;
//import processing.core.PVector;
import toxi.math.CircularInterpolation;
import toxi.math.InterpolateStrategy;
import toxi.math.LinearInterpolation;
import toxi.math.ScaleMap;
import toxi.math.ZoomLensInterpolation;

/********************************************
 * Virtual world consisting of a viewer and field(s) containing media
 * @author davidgordon
 */

public class WMV_World 
{
	/* Classes */
	public MultimediaLocator ml;						// Parent app
	private ArrayList<WMV_Field> fields;				// Virtual media environments 
	public WMV_Viewer viewer;						// Virtual viewer
	public WMV_WorldSettings settings;				// World settings
	public WMV_WorldState state;						// World state
	public WMV_Utilities utilities;					// Utility class
	public ML_Input input;							// Keyboard input handler
	
	/* Interpolation */
	ScaleMap distanceFadeMap, timeFadeMap;
	InterpolateStrategy circularEaseOut = new CircularInterpolation(false);		// Steepest ascent at beginning
	InterpolateStrategy circularEaseIn = new CircularInterpolation(true);		// Steepest ascent toward end value
	InterpolateStrategy zoomLens = new ZoomLensInterpolation();
	InterpolateStrategy linear = new LinearInterpolation();
	
	/* File System */
	public String outputFolder;
	public boolean outputFolderSelected = false;

	/* Masks */
	public PImage blurMaskLeftTop, blurMaskLeftCenter, blurMaskLeftBottom, blurMaskLeftBoth;  						// Image blur masks
	public PImage blurMaskCenterTop, blurMaskCenterCenter, blurMaskCenterBottom, blurMaskCenterBoth;
	public PImage blurMaskRightTop, blurMaskRightCenter, blurMaskRightBottom, blurMaskRightBoth;
	public PImage blurMaskBothTop, blurMaskBothCenter, blurMaskBothBottom, blurMaskBothBoth;
	public PImage vertBlurMaskLeftTop, vertBlurMaskLeftCenter, vertBlurMaskLeftBottom, vertBlurMaskLeftBoth;  		// Vertical image blur masks
	public PImage vertBlurMaskCenterTop, vertBlurMaskCenterCenter, vertBlurMaskCenterBottom, vertBlurMaskCenterBoth;
	public PImage vertBlurMaskRightTop, vertBlurMaskRightCenter, vertBlurMaskRightBottom, vertBlurMaskRightBoth;
	public PImage vertBlurMaskBothTop, vertBlurMaskBothCenter, vertBlurMaskBothBottom, vertBlurMaskBothBoth;
	public PImage videoBlurMaskLeftTop, videoBlurMaskLeftCenter, videoBlurMaskLeftBottom, videoBlurMaskLeftBoth;  	// Video blur masks
	public PImage videoBlurMaskCenterTop, videoBlurMaskCenterCenter, videoBlurMaskCenterBottom, videoBlurMaskCenterBoth;
	public PImage videoBlurMaskRightTop, videoBlurMaskRightCenter, videoBlurMaskRightBottom, videoBlurMaskRightBoth;
	public PImage videoBlurMaskBothTop, videoBlurMaskBothCenter, videoBlurMaskBothBottom, videoBlurMaskBothBoth;
	public PImage blurMaskPanorama;																					// Panorama blur mask

	/* Effects */
//	public boolean drawForceVector = true;				// Show attraction vector on map (mostly for debugging)

	/**
	 * Constructor for virtual world
	 * @param parent Parent App
	 */
	public WMV_World(MultimediaLocator parent)
	{
//		public MultimediaLocator ml;						// Parent app
//		private ArrayList<WMV_Field> fields;				// Virtual media environments 
//		public WMV_Viewer viewer;							// Virtual viewer
//		public WMV_WorldSettings settings;					// World settings
//		public WMV_WorldState state;						// World state
//		public WMV_Utilities utilities;						// Utility class
//		public ML_Input input;								// Keyboard input handler

//		/* File System */
//		public String outputFolder;
//		public boolean outputFolderSelected = false;
//
//		/* Masks */
//		public PImage blurMaskLeftTop, blurMaskLeftCenter, blurMaskLeftBottom, blurMaskLeftBoth;  						// Image blur masks
//		public PImage blurMaskCenterTop, blurMaskCenterCenter, blurMaskCenterBottom, blurMaskCenterBoth;
//		public PImage blurMaskRightTop, blurMaskRightCenter, blurMaskRightBottom, blurMaskRightBoth;
//		public PImage blurMaskBothTop, blurMaskBothCenter, blurMaskBothBottom, blurMaskBothBoth;
//		public PImage vertBlurMaskLeftTop, vertBlurMaskLeftCenter, vertBlurMaskLeftBottom, vertBlurMaskLeftBoth;  		// Vertical image blur masks
//		public PImage vertBlurMaskCenterTop, vertBlurMaskCenterCenter, vertBlurMaskCenterBottom, vertBlurMaskCenterBoth;
//		public PImage vertBlurMaskRightTop, vertBlurMaskRightCenter, vertBlurMaskRightBottom, vertBlurMaskRightBoth;
//		public PImage vertBlurMaskBothTop, vertBlurMaskBothCenter, vertBlurMaskBothBottom, vertBlurMaskBothBoth;
//		public PImage videoBlurMaskLeftTop, videoBlurMaskLeftCenter, videoBlurMaskLeftBottom, videoBlurMaskLeftBoth;  	// Video blur masks
//		public PImage videoBlurMaskCenterTop, videoBlurMaskCenterCenter, videoBlurMaskCenterBottom, videoBlurMaskCenterBoth;
//		public PImage videoBlurMaskRightTop, videoBlurMaskRightCenter, videoBlurMaskRightBottom, videoBlurMaskRightBoth;
//		public PImage videoBlurMaskBothTop, videoBlurMaskBothCenter, videoBlurMaskBothBottom, videoBlurMaskBothBoth;
//		public PImage blurMaskPanorama;																					// Panorama blur mask

		ml = parent;
		
		/* Create main classes */
		settings = new WMV_WorldSettings();
		utilities = new WMV_Utilities();
		
		state = new WMV_WorldState((float)ml.height / (float)ml.width);
		viewer = new WMV_Viewer(this, settings, state, ml.debug);			// Initialize navigation + viewer
		
		/* Setup interpolation variables */
		timeFadeMap = new ScaleMap(0., 1., 0., 1.);				// Fading with time interpolation
		timeFadeMap.setMapFunction(circularEaseOut);
		distanceFadeMap = new ScaleMap(0., 1., 0., 1.);			// Fading with distance interpolation
		distanceFadeMap.setMapFunction(circularEaseIn);
	}
	
	/**
	 * Update and display virtual world
	 */
	public void run()
	{
		updateState();			// Update viewer and media about the current world state
		display();				// Display 3D world and/or 2D graphics + text
		update();				// Update world 
	}
	
	/**
	 * Update virtual world
	 */
	public void update()
	{
		/* Navigation */
		updateViewerAttraction();										/* Attract the viewer */
		if(ml.display.getDisplayView() < 3) viewer.updateNavigation();	/* Update navigation */
		
		/* Graphics */
		if(state.fadingAlpha)  updateFadingAlpha();							/* Update alpha fading */
		if(state.fadingModelAlpha) updateModelAlphaFading();					/* Update model alpha fading */
		if(state.fadingTerrainAlpha)  updateFadingTerrainAlpha();			/* Update grid fading */

		/* Time */
		if(viewer.getState().fadingToTime) viewer.updateTimeTransition();		/* Update fading transition between time segments */
		if(viewer.getState().fadingToClusterTime) viewer.updateClusterTimeTransition();
		
		updateTimeBehavior();											/* Update time cycle */
		if(ml.frameCount % 120 == 0) checkForOrphanedMedia();				/* Check for orphaned media */
		ml.display.window.update();										/* Update windows */
		
//		if(viewer.getSettings().mouseNavigation)							/* Update mouse navigation   -- Disabled */
//		input.updateMouseNavigation(viewer, ml.mouseX, ml.mouseY, ml.frameCount);
	}

	/**
	 * Display 3D world and/or 2D graphics and text
	 * @param sphericalView Display in spherical view
	 */
	public void display()
	{
		ml.background(0.f);										/* Set background */
		if(ml.state.sphericalView)
		{
			if(ml.cubeMapInitialized)
				ml.display360();								/* Display Spherical View -- Disabled / In progress */
		}
		else
		{
			if(ml.display.getDisplayView() == 0) display3D();	/* Display 3D Graphics */
			display2D();										/* Display 2D Graphics */
		}
	}

	/**
	 * Update viewer and media about the current world state
	 */
	public void updateState()
	{
		state.frameCount = ml.frameCount;
		viewer.updateState(settings, state);
		getCurrentField().update(settings, state, viewer.getSettings(), viewer.getState());				// Update clusters in current field
		getCurrentField().findVisibleClusters(ml);									/* Find visible clusters */

//		System.out.println("World.updateState()... Will update media states with staticMediaLength: "+settings.staticMediaLength);
//		System.out.println("  --> Field staticMediaLength: "+getCurrentField().getWorldSettings().staticMediaLength);
		getCurrentField().updateAllMediaStates();
	}

	/**
	 * Display the current field in World View
	 */
	public void display3D()
	{
		ml.background(0.f);						/* Set background */
		if(settings.depthTesting) ml.hint(PApplet.ENABLE_DEPTH_TEST);		/* Enable depth testing for drawing 3D graphics */
		getCurrentField().display(ml);										/* Display media in current field */
		if(settings.showUserPanoramas || settings.showStitchedPanoramas)
		{
			if(viewer.getState().getCurrentClusterID() > 0 && viewer.getState().getCurrentClusterID() < getCurrentField().getClusters().size())
			{
				ArrayList<WMV_Cluster> clusters = getCurrentField().getClusters();
				if(clusters.size()>0 && viewer.getState().getCurrentClusterID() < clusters.size())
					clusters.get(viewer.getState().getCurrentClusterID()).displayUserPanoramas(ml);		// Draw current cluster
			}
		}

		if(state.displayTerrain) displayTerrain();	/* Draw terrain as wireframe grid */
		
		if(!ml.state.sphericalView)	
			if(ml.state.running)
				viewer.show();						/* Send World View to screen */
	}

	/**
	 * Display 2D text and graphics
	 */
	public void display2D()
	{
		ml.display.display(ml);										/* Display 2D Graphics */
		if(ml.display.getDisplayView() == 4) viewer.showHUD();			/* Set camera angle in Media View */
	}

	/** 
	 * Update main time loop
	 */
	void updateTimeBehavior()
	{
		switch(state.timeMode)
		{
			case 0:													// Cluster Time Mode
				if(state.timeFading) 
					updateClusterTimeMode();
				break;
			
			case 1:													// Field Time Mode
				if(state.timeFading && state.frameCount % settings.timeUnitLength == 0)
					updateFieldTimeMode();
				break;

//			case 2:													// Media Time Mode
//				if(state.timeFading && state.frameCount % settings.timeUnitLength == 0)
//					updateSingleTimeMode();
//				break;
				
			case 3:													// Flexible Time Mode -- In progress
				break;
		}
		
		if(state.timeFading && !state.paused)
		{
			if(ml.display.window.setupTimeWindow)
			{
				ml.display.window.sdrCurrentTime.setValue(getCurrentTime());
				ml.display.updateTimeWindowCurrentTime();
			}
		}
		
		if(state.turningOffTimeFading || state.turningOnTimeFading) 
			updateTimeFadingStateChange();						/* Update turning on / off Time Fading */

	}
	
	/**
	 * Update change in Time Fading state
	 */
	private void updateTimeFadingStateChange()
	{
		if(!getCurrentField().mediaAreFading())
		{
			if( ml.debug.time )
				ml.systemMessage("World.updateTimeFadingStateChange()... Media faded out... frameCount:"+ml.frameCount);

			if(state.turningOnTimeFading	)
			{
				setTimeFading(true);
				state.turningOnTimeFading = false;
			}
			if(state.turningOffTimeFading)
			{
				setTimeFading(false);
				state.turningOffTimeFading = false;
			}
		}
		else
		{
			if( ml.debug.time && ml.debug.detailed )
			{
				if(ml.frameCount % 5 == 0)
					ml.systemMessage("World.updateTimeFadingStateChange()... waiting for media to fade out... frameCount:"+ml.frameCount);
			}
		}
	}
	
	/**
	 * Check for videos and sounds playing but invisible
	 */
	private void checkForOrphanedMedia()
	{
		for(WMV_Video v : getCurrentField().getVideos())
		{
			if(v.isPlaying())
			{
				boolean outOfRange = v.getCaptureDistanceFrom(viewer.getLocation()) > viewer.getSettings().farHearingDistance;
//				if(!v.isVisible())
				if(outOfRange)
				{
					v.stopVideo();
					if(ml.debug.ml || ml.debug.video)
						ml.systemMessage("Stopped video #"+v.getID()+" which was playing but out of range...");
				}
			}
		}
		for(WMV_Sound s : getCurrentField().getSounds())
		{
			if(s.isPlaying())
			{
				boolean outOfRange = s.getCaptureDistanceFrom(viewer.getLocation()) > viewer.getSettings().farHearingDistance;
//				if(!s.isVisible())
				if(outOfRange)
				{
					s.stopSound();
					if(ml.debug.ml || ml.debug.video)
						ml.systemMessage("Stopped sound #"+s.getID()+" which was playing but out of range...");
				}
			}
		}
	}
	
	/**
	 * Get current position in Time Cycle
	 * @return Current time point
	 */
	public float getCurrentTime()
	{
		float timePoint = 0.f;						// Normalized time position {0.f: first field time, 1.f: last field time}
		switch(getState().getTimeMode())
		{
			case 0:
//				WMV_Cluster c = getClusterWithCurrentTime();
				WMV_Cluster c = getCurrentCluster();
				
				if(c != null)
					timePoint = utilities.mapValue(c.getState().currentTimeCycleFrame, 0, c.getState().timeCycleLength, 0.f, 1.f);
				break;
			case 1:
				timePoint = utilities.mapValue(getCurrentFieldTimeCyclePoint(), 0, getSettings().timeCycleLength, 0.f, 1.f);
				break;
//			case 2:
//				timePoint = utilities.mapValue(getCurrentTimeCycleFrame(), 0, getSettings().timeCycleLength, 0.f, 1.f);
//				break;
		}
		return timePoint;
	}
	
	/**
	 * Get current position in Time Cycle
	 * @return Current time point
	 */
	public float getCurrentFieldTime()
	{
		float timePoint = utilities.mapValue(getCurrentFieldTimeCyclePoint(), 0, getSettings().timeCycleLength, 0.f, 1.f);
		return timePoint;
	}

	/**
	 * @return Current cluster time
	 */
	public float getCurrentClusterTime()
	{
		return state.currentClusterTime;
	}
	
	/**
	 * Set current cluster time
	 * @param newClusterTime New cluster time
	 */
	public void setCurrentClusterTimePoint(float newClusterTime)
	{
		state.currentClusterTime = newClusterTime;
	}
	
	/**
	 * Set current time from given absolute time 
	 * @param absoluteTimePoint Absolute time {0.f: midnight, 1.f: midnight the next day}
	 */
	public void setCurrentTimeFromAbsolute(float absoluteTimePoint, boolean updateSliders)
	{
		if(state.getTimeMode() == 0)					// Cluster Mode
		{
//			WMV_Cluster c = getCurrentCluster();
//			if(c != null)
//			{
//				float lower = c.getTimeline().getLower().getLower().getAbsoluteTime();		// Cluster lowest absolute time 
//				float upper = c.getTimeline().getUpper().getUpper().getAbsoluteTime();		// Cluster highest absolute time 
//				float clusterTimePoint = ml.utilities.mapValue(absoluteTimePoint, lower, upper, 0.f, 1.f);		// Time relative to cluster
//				clusterTimePoint = PApplet.constrain(clusterTimePoint, 0.f, 1.f);				// Time relative to cluster
//				if(ml.debug.time) ml.systemMessage("World.setCurrentTimeFromAbsolute()... absoluteTimePoint:"+absoluteTimePoint+" result clusterTimePoint:"+clusterTimePoint);
//
////				setCurrentTime(clusterTimePoint, false, updateSliders);
//				setCurrentTime(clusterTimePoint, false, true);
//			}
//			else	
//			{
				if(ml.debug.time) 
					ml.systemMessage("World.setCurrentTimeFromAbsolute()... ERROR... Can't set current time for field in Cluster Mode!!!");
//			}
		}
		else											// Field Mode
		{
			float lower = getCurrentField().getTimeline().getLower().getLower().getAbsoluteTime();		// Field lowest absolute time 
			float upper = getCurrentField().getTimeline().getUpper().getUpper().getAbsoluteTime();		// Field highest absolute time 
			float fieldTimePoint = ml.utilities.mapValue(absoluteTimePoint, lower, upper, 0.f, 1.f);		// Time relative to field
			if(ml.debug.time) ml.systemMessage("World.setCurrentTimeFromAbsolute()... absoluteTimePoint:"+absoluteTimePoint+" result fieldTimePoint:"+fieldTimePoint);

//			setCurrentTime(fieldTimePoint, true, updateSliders);
			setCurrentFieldTime(fieldTimePoint, true);
		}
	}

	/**
	 * Get field time from given absolute time 
	 * @param absoluteTimePoint Absolute time {0.f: midnight, 1.f: midnight the next day}
	 */
	public float getFieldTimeFromAbsolute(float absoluteTimePoint)
	{
		float lower = getCurrentField().getTimeline().getLower().getLower().getAbsoluteTime();		// Lowest absolute field time 
		float upper = getCurrentField().getTimeline().getUpper().getUpper().getAbsoluteTime();		// Highest absolute field time 
		float fieldTimePoint = ml.utilities.mapValue(absoluteTimePoint, lower, upper, 0.f, 1.f);		// Time relative to field
		return fieldTimePoint;
	}

	/**
	 * Get absolute time from given field time 
	 * @param absoluteTimePoint Field time {0.f: first field time, 1.f: last field time}
	 */
	public float getAbsoluteTimeFromField(float fieldTimePoint)
	{
		float lower = getCurrentField().getTimeline().getLower().getLower().getAbsoluteTime();		// Lowest absolute field time 
		float upper = getCurrentField().getTimeline().getUpper().getUpper().getAbsoluteTime();		// Highest absolute field time 
		float absoluteTimePoint = ml.utilities.mapValue(fieldTimePoint, 0.f, 1.f, lower, upper);		// Time relative to field
		return absoluteTimePoint;
	}
	
	/**
	 * Set time point based on current Time Mode
	 * @param newTimePoint New time point {Between 0.f and 1.f}
	 * @param fieldTime Whether given new time point is relative to field (true) or not (false).   
	 * 	      If false in Field Time Mode, time will be interpreted as relative to field.
	 * 		  If false in Cluster Time Mode, time will be interpreted as relative to cluster. 
	 * @param updateSliders Whether to update sliders in Time Window
	 */
	public void setCurrentFieldTime(float newTimePoint, boolean updateSliders)
	{
		if(ml.debug.time) System.out.println("World.setCurrentFieldTime()... newTimePoint:"+newTimePoint+" updateSliders? "+updateSliders);

//		if(fieldTime)										// Set field time to given time point
//		{
			setCurrentFieldTimeCyclePoint( newTimePoint );				
			if(updateSliders && ml.display.window.setupTimeWindow)
			{
				ml.display.window.sdrCurrentTime.setValue( (float)(getCurrentFieldTimeCyclePoint()) / (float)(getTimeCycleLength()) );
				ml.display.updateTimeWindowCurrentTime();
			}
//		}
		
		switch(state.timeMode)
		{
			case 0:													// Cluster Time Mode
				if(ml.debug.time)
					System.out.println("World.setCurrentTime()... In Cluster Mode... newTimePoint:"+newTimePoint+" updateSliders? "+updateSliders);
				
				for(WMV_Cluster c : getVisibleClusters())			// Set times of all visible clusters		 -- Change to all clusters?
				{
					if(c.getState().timeFading)
					{
//						if(fieldTime) 			
							c.setCurrentTimeFromAbsoluteTime( getAbsoluteTimeFromField(newTimePoint) );
//						else 
//							c.setCurrentTime(newTimePoint);			
					}
				}
				
//				if(!fieldTime)									// Set current time based on given relative time 								
//				{
////					WMV_Cluster c = getClusterWithCurrentTime();
//					WMV_Cluster c = getCurrentCluster();
//					if(c != null)
//					{
//						if(!getVisibleClusters().contains(c))
//							c.setCurrentTime(newTimePoint);			// Set current cluster time if not already set
//						
//						setCurrentFieldTimeToClusterTime( c, newTimePoint, true );	// Set current field time based on given cluster time 
//						setCurrentClusterTimePoint( (float)c.getCurrentTimeCycleFrame() / (float)c.getTimeCycleLength() );
//
//						if(updateSliders && ml.display.window.setupTimeWindow)
//							ml.display.window.sdrCurrentTime.setValue( (float)c.getCurrentTimeCycleFrame() / (float)c.getTimeCycleLength() );
//					}
//				}
				
				break;
			
			case 1:													// Field Time Mode
				if(ml.debug.time)
					ml.systemMessage("World.setCurrentTime()... In Field Mode... newTimePoint:"+newTimePoint+" updateSliders? "+updateSliders);

//				if(!fieldTime)									// Set current time based on given relative time 												
//				{
////					WMV_Cluster c = getClusterWithCurrentTime();
//					WMV_Cluster c = getCurrentCluster();
//					if(c != null)
//					{
//						setCurrentFieldTimeToClusterTime( c, newTimePoint, true );	// Set field time to first time in target time segment
//						setCurrentClusterTimePoint( (float)c.getCurrentTimeCycleFrame() / (float)c.getTimeCycleLength() );
//
//						if(updateSliders && ml.display.window.setupTimeWindow)
//							ml.display.window.sdrCurrentTime.setValue( (float)c.getCurrentTimeCycleFrame() / (float)c.getTimeCycleLength() );
//					}
//				}
				
				break;

			case 2:													// (Single) Media Time Mode
				break;
				
			case 3:													// Flexible Time Mode -- In progress
				break;
		}
	}
	
	/**
	 * Set time point based on current Time Mode
	 * @param newTimePoint New time point {Between 0.f and 1.f}
	 * @param fieldTime Whether given new time point is relative to field (true) or not (false).   
	 * 	      If false in Field Time Mode, time will be interpreted as relative to field.
	 * 		  If false in Cluster Time Mode, time will be interpreted as relative to cluster. 
	 * @param updateSliders Whether to update sliders in Time Window
	 */
	public void setCurrentClusterTime(float newTimePoint, boolean updateSliders)
	{
		if(ml.debug.time) System.out.println("World.setCurrentClusterTime()... newTimePoint:"+newTimePoint+" updateSliders? "+updateSliders);

//		if(fieldTime)										// Set field time to given time point
//		{
//			setCurrentFieldTimeCyclePoint( newTimePoint );				
//			if(updateSliders && ml.display.window.setupTimeWindow)
//				ml.display.window.sdrCurrentTime.setValue( (float)(getCurrentFieldTimeCyclePoint()) / (float)(getTimeCycleLength()) );
//		}
		WMV_Cluster c = getCurrentCluster();

		switch(state.timeMode)
		{
			case 0:													// Cluster Time Mode
				if(ml.debug.time)
					System.out.println("World.setCurrentClusterTime()... In Cluster Mode... newTimePoint:"+newTimePoint+" updateSliders? "+updateSliders);
				
				for(WMV_Cluster cl : getVisibleClusters())			// Set times of all visible clusters		 -- Change to all clusters?
				{
					if(cl.getState().timeFading)
					{
//						if(fieldTime) 			
//							c.setCurrentTimeFromAbsoluteTime( getAbsoluteTimeFromField(newTimePoint) );
//						else 
							cl.setCurrentTime(newTimePoint);			
					}
				}
				
//				if(!fieldTime)									// Set current time based on given relative time 								
//				{
//					WMV_Cluster c = getClusterWithCurrentTime();
//					WMV_Cluster c = getCurrentCluster();
					if(c != null)
					{
						if(!getVisibleClusters().contains(c))
							c.setCurrentTime(newTimePoint);			// Set current cluster time if not already set
						
						setCurrentFieldTimeToClusterTime( c, newTimePoint, true );	// Set current field time based on given cluster time 
						setCurrentClusterTimePoint( (float)c.getCurrentTimeCycleFrame() / (float)c.getTimeCycleLength() );

						if(updateSliders && ml.display.window.setupTimeWindow)
						{
							ml.display.window.sdrCurrentTime.setValue( (float)c.getCurrentTimeCycleFrame() / (float)c.getTimeCycleLength() );
						}
						if(ml.display.window.setupTimeWindow)
							ml.display.updateTimeWindowCurrentTime();
					}
//				}
				
				break;
			
			case 1:													// Field Time Mode
				if(ml.debug.time)
					ml.systemMessage("World.setCurrentClusterTime()... In Field Mode... newTimePoint:"+newTimePoint+" updateSliders? "+updateSliders);

//				if(!fieldTime)									// Set current time based on given relative time 												
//				{
//					WMV_Cluster c = getClusterWithCurrentTime();
//					WMV_Cluster c = getCurrentCluster();
					if(c != null)
					{
						setCurrentFieldTimeToClusterTime( c, newTimePoint, true );	// Set field time to first time in target time segment
						setCurrentClusterTimePoint( (float)c.getCurrentTimeCycleFrame() / (float)c.getTimeCycleLength() );

						if(updateSliders && ml.display.window.setupTimeWindow)
						{
							ml.display.window.sdrCurrentTime.setValue( (float)c.getCurrentTimeCycleFrame() / (float)c.getTimeCycleLength() );
						}
						if(ml.display.window.setupTimeWindow)
							ml.display.updateTimeWindowCurrentTime();
					}
//				}
				
				break;

			case 2:													// (Single) Media Time Mode
				break;
				
			case 3:													// Flexible Time Mode -- In progress
				break;
		}
	}
	
	/**
	 * Set current field time from given time point in cluster's time cycle
	 * @param c Cluster to set time from
	 * @param clusterTimePoint Cluster time point to use to set field time, {0.f: first cluster time, 1.f: last cluster time}
	 */
	public void setCurrentFieldTimeToClusterTime( WMV_Cluster c, float clusterTimePoint, boolean updateSliders )
	{
		WMV_Time closest = c.getClosestTimeToClusterTime( clusterTimePoint );			// Find cluster associated media (absolute) time closest to given time point

		float fAbsLower = getCurrentField().getTimeline().getLower().getLower().getAbsoluteTime();		// Field lowest absolute time 
		float fAbsUpper = getCurrentField().getTimeline().getUpper().getUpper().getAbsoluteTime();		// Field highest absolute time 

		if(closest != null)
		{
			float newFieldTimePoint = closest.getRelativeTime(fAbsLower, fAbsUpper);		// Field time point associated with given cluster time point
			//		float newAbsTimePoint = closest.getAbsoluteTime();							// New field time point is closest time in absolute terms

			if(newFieldTimePoint >= 0.f && newFieldTimePoint <= 1.f)		// Time in field range
			{
				setCurrentFieldTimeCyclePoint( newFieldTimePoint );				
//				setCurrentTime( newFieldTimePoint, true, updateSliders );

				if(ml.debug.detailed && ml.debug.time)
					System.out.println("World.setCurrentTimeToClusterTime()... Cluster id #"+c.getID()+" clusterTimePoint:"+clusterTimePoint+" converted to newFieldTimePoint:"+newFieldTimePoint+" Result: getCurrentTime(): "+getCurrentTime());
			}
			else															// Time out of range
			{
				if(ml.debug.detailed && ml.debug.time)
					System.out.println("World.setCurrentTimeToClusterTime()... Cluster id #"+c.getID()+" time out of range...");

				if( newFieldTimePoint < 0.f )
					setCurrentFieldTimeCyclePoint( 0.f );				
//					setCurrentTime( 0.f, true, updateSliders );
				else if( newFieldTimePoint > 1.f )
					setCurrentFieldTimeCyclePoint( 1.f );				
//					setCurrentTime( 1.f, true, updateSliders );
			}
		}
		else
		{
			if(ml.debug.cluster || ml.debug.time)
				System.out.println("World.setCurrentTimeToClusterTime()... Cluster id #"+c.getID()+" ERROR... clusterTimePoint:"+clusterTimePoint+" closest == null!");
		}
	}
	/**
	 * Set current frame in field time cycle
	 * @param newTimePoint New time point {0.f: first frame, 1.f: last frame}
	 * @param padding Whether to set time cycle accounting for padding due to fading (true) or set raw value (false)
	 */
	private void setCurrentFieldTimeCyclePoint(float newTimePoint)
	{
		if(ml.debug.time) 
			System.out.println("World.setCurrentTimeCyclePoint()... newTimePoint:"+newTimePoint);

		int low = 0, high = getTimeCycleLength();
		
		state.setCurrentTimeCycleFrame( (int) utilities.mapValue(newTimePoint, 0.f, 1.f, low, high) );
	}
	
	/**
	 * Get current frame in field time cycle
	 * @return Current time point
	 */
	public int getCurrentFieldTimeCyclePoint()
	{
		return state.getCurrentTimeCycleFrame();
	}
	
	/**
	 * Get current field time cycle length
	 * @return Current field time cycle length
	 */
	public int getTimeCycleLength()
	{
		return settings.timeCycleLength;
	}
	
	/**
	 * Set length to display static media when fading over time
	 * @param newStaticMediaLength
	 */
	public void 	setStaticMediaLength(int newStaticMediaLength)
	{
		
		settings.staticMediaLength = newStaticMediaLength;
		
		int cycleMax = settings.maxTimeCycleLength;
//		int cycleLength = settings.timeCycleLength;		// -- Make clusterTimeCycleLength??
		int cycleLength = getCycleLengthForCurrentTimeMode();		// -- Make clusterTimeCycleLength??

		if(ml.debug.world) ml.systemMessage("World.setStaticMediaLength: "+newStaticMediaLength+" cycleLength:"+cycleLength);
		
		if(state.timeMode == 0)
		{
			if(settings.staticMediaLength > cycleLength)
			{
				int diff = settings.staticMediaLength - cycleLength;
				int newLength = PApplet.constrain( cycleLength + diff, 0, cycleMax );
				WMV_Cluster c = getCurrentCluster();
//				if(c!=null) System.out.println("  before:"+c.getTimeCycleLength());
				System.out.println("1 diff:"+diff+" newLength:"+newLength+" cycleLength:"+cycleLength+" settings.staticMediaLength:"+settings.staticMediaLength);
				setClusterTimeCycleLength( newLength, true );
				if(c!=null) System.out.println("  after:"+c.getTimeCycleLength());
			}
			else
			{
				if(ml.display.window.setupTimeWindow)
				{
					ml.display.updateTimeWindowCurrentTime();
					WMV_Cluster c = getCurrentCluster();
					if(c != null)
						ml.display.window.sdrTimeCycleLength.setValue(c.getTimeCycleLength());
				}
				updateState();				// -- Needed?
//				getCurrentField().updateAllMediaStates();
			}
			
		}
		else
		{
			if(settings.staticMediaLength > cycleLength)
			{
				int diff = settings.staticMediaLength - cycleLength;
				int newLength = PApplet.constrain( cycleLength + diff, 0, cycleMax );
				System.out.println("2 diff:"+diff+" newLength:"+newLength+" cycleLength:"+cycleLength+" settings.staticMediaLength:"+settings.staticMediaLength);
				setTimeCycleLength( newLength, true );
			}
			else
			{
				updateState();				// -- Needed?
//				getCurrentField().updateAllMediaStates();
			}
			
			if(ml.display.window.setupTimeWindow)
			{
				ml.display.updateTimeWindowCurrentTime();
				ml.display.window.sdrTimeCycleLength.setValue(settings.timeCycleLength);
			}
		}
	}
	
	public int getCycleLengthForCurrentTimeMode()
	{
		int cycleLength;
		
		if(ml.display.window.setupTimeWindow)
		{
			cycleLength = ml.display.window.sdrTimeCycleLength.getValueI();
		}
		else
		{
			if(state.timeMode == 0)
			{
				WMV_Cluster c = getCurrentCluster();
				if(c != null)
					cycleLength = c.getTimeCycleLength();
				else
					cycleLength = settings.clusterTimeCycleLength;
			}
			else
				cycleLength = settings.timeCycleLength;
		}

//		System.out.println("World.getCycleLengthForCurrentTimeMode()... cycleLength: ");

		return cycleLength;
	}

	/**
	 * Set field time cycle length
	 * @param newTimeCycleLength New time cycle length
	 */
	public void setTimeCycleLength(int newTimeCycleLength, boolean updateSlider)
	{
		settings.timeCycleLength = newTimeCycleLength;
//		settings.timeInc = settings.timeCycleLength / 30.f;	
		updateState();															// -- Needed?
		
		if(updateSlider && ml.display.window.setupTimeWindow)
			ml.display.window.sdrTimeCycleLength.setValue(settings.timeCycleLength);

//		getCurrentField().updateAllMediaStates();
	}
	
	/**
	 * Set time cycle length for all clusters in current field
	 * @param newTimeCycleLength New time cycle length
	 */
	public void setClusterTimeCycleLength(int newTimeCycleLength, boolean updateSlider)
	{
		settings.clusterTimeCycleLength = newTimeCycleLength;
		
		if(ml.debug.detailed && ml.debug.time)
			ml.systemMessage("World.setAllClustersTimeCycleLength()... new Cluster Time Cycle Length: "+settings.clusterTimeCycleLength);
		
		for(WMV_Cluster c : getCurrentField().getClusters())
			if(!c.getState().empty)
				c.setTimeCycleLength( settings.clusterTimeCycleLength );
		
		updateState();				// -- Needed?
		
		if(updateSlider && ml.display.window.setupTimeWindow)
			ml.display.window.sdrTimeCycleLength.setValue(settings.clusterTimeCycleLength);
	}
	
	/**
	 * Set cluster length
	 * @param newClusterLength New cluster length
	 */
	public void setClusterLength(float newClusterLength)
	{
		settings.clusterLength = newClusterLength;
	}
	
	/**
	 * Set time fading fade in / out length
	 * @param newFadeLength New fading length
	 */
	public void setFadeLength(int newFadeLength)
	{
		settings.staticMediaFadeLength = newFadeLength;
	}
	
	/**
	 * Update Cluster Time Mode 
	 */
	private void updateClusterTimeMode()
	{
		ArrayList<WMV_Cluster> visible = getVisibleClusters();		// Get visible clusters 
		for(WMV_Cluster c : visible)
		{
			if(!c.isTimeFading()) c.setTimeFading(true);				// Set time fading to ON
			if(!state.paused) c.updateTime();						// Update cluster time cycle
		}
		
		for(WMV_Cluster c : getCurrentField().getClusters())			// Set invisible clusters to stop time fading
		{
			if(!visible.contains(c))
				if(c.isTimeFading())
					c.setTimeFading(false);
		}
		
		WMV_Cluster c = getCurrentCluster();
		setCurrentClusterTimePoint( (float)c.getCurrentTimeCycleFrame() / (float)c.getTimeCycleLength() );		// Set global record of cluster time
	}
	
	/**
	 * Update Field Time Mode 
	 */
	private void updateFieldTimeMode()
	{
		if(!state.paused)
		{
			if(!viewer.isMoving() && !viewer.isTeleporting())
			{
				state.setCurrentTimeCycleFrame(state.getCurrentTimeCycleFrame()+1);
				if(getCurrentFieldTimeCyclePoint() > settings.timeCycleLength)
				{
					if(ml.debug.time) ml.systemMessage("World.updateFieldTimeMode()... Reached end of Field Time Cycle...");

					state.setCurrentTimeCycleFrame( 0 );
				}
			}
		}
	}
	
//	/**
//	 * Update Single Time Mode parameters -- Disabled (Obsolete)
//	 */
//	private void updateSingleTimeMode()
//	{
//		if(!state.paused)
//		{
//			state.setCurrentTimeCycleFrame(state.getCurrentTimeCycleFrame()+1);
//			if(ml.debug.time && ml.debug.detailed)
//				ml.systemMessage("currentTime:"+getCurrentFieldTimeCyclePoint());
//
//			if(getCurrentFieldTimeCyclePoint() >= viewer.getNextMediaStartTime())
//			{
//				if(viewer.getCurrentMedia() + 1 < viewer.getNearbyClusterTimelineMediaCount())
//				{
//					setMediaTimeModeCurrentMedia(viewer.getCurrentMedia() + 1);		
//				}
//				else
//				{
//					if(ml.debug.world)
//						ml.systemMessage("Reached end of last media with "+(settings.timeCycleLength - getCurrentFieldTimeCyclePoint())+ " frames to go...");
//					state.setCurrentTimeCycleFrame( 0 );
//					startMediaTimeModeCycle();
//				}
//			}
//
//			if(getCurrentFieldTimeCyclePoint() > settings.timeCycleLength)
//			{
//				state.setCurrentTimeCycleFrame( 0 );
//				startMediaTimeModeCycle();
//			}
//		}
//	}
	
	/**
	 * Move forward in time
	 */
	public void incrementTime()
	{
		float curTimePoint = getCurrentTime();
		float endTimePoint = 1.f;
		float timeInc = settings.timeIncrement;
		if(state.getTimeMode() == 0)
		{
			if (curTimePoint + timeInc > endTimePoint && curTimePoint != endTimePoint) 
				setCurrentClusterTime(endTimePoint, true);
			else 
				setCurrentClusterTime(curTimePoint + timeInc, true);
		}
		else
		{
			if (curTimePoint + timeInc > endTimePoint && curTimePoint != endTimePoint) 
				setCurrentFieldTime(endTimePoint, true);
			else 
				setCurrentFieldTime(curTimePoint + timeInc, true);
		}
	}

	/**
	 * Move back in time
	 */
	public void decrementTime()
	{
		float curTimePoint = getCurrentTime();
		float timeInc = settings.timeIncrement;
		if(state.getTimeMode() == 0)
		{
			if (curTimePoint - timeInc < 0.f && curTimePoint != 0.f) 
				setCurrentClusterTime(0.f, true);
			else
				setCurrentClusterTime(curTimePoint - timeInc, true);
		}
		else
		{
			if (curTimePoint - timeInc < 0.f && curTimePoint != 0.f) 
				setCurrentFieldTime(0.f, true);
			else
				setCurrentFieldTime(curTimePoint - timeInc, true);
		}
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
				ml.systemMessage("viewer.attractorPoint == NULL!!");
		}
		else if(viewer.getState().isMovingToCluster())				// If the camera is moving to a cluster (besides memoryCluster)
		{
			for( WMV_Cluster c : getCurrentField().getAttractingClusters() )
				if(c.getClusterDistance() > settings.clusterCenterSize)		// If not already at attractor cluster center, attract camera 
					c.attractViewer(viewer);
		}
	}
	
	/**
	 * Enter field with specified ID
	 * @param fieldIdx The field to enter
	 * @param first Whether to tell viewer this is the first frame
	 */
	public void enterFieldAtBeginning(int fieldIdx)
	{
		if(ml.debug.ml) ml.systemMessage("World.enterFieldAtBeginning()... Field #"+fieldIdx);
		viewer.enterField( fieldIdx, false );								// Enter field
		
		viewer.moveToFirstTimeSegment(false);		// Move to first time segment if start location not set from saved data 

		viewer.updateNavigation();					// Update navigation
		
		viewer.start();								// Start the viewer if this is the first frame

		if(state.displayTerrain)
			state.waitingToFadeInTerrainAlpha = true;
	}
	
	/**
	 * Get field names as list of strings
	 * @return List of field names
	 */
	public ArrayList<String> getFieldNames()
	{
		ArrayList<String> names = new ArrayList<String>();
		for(WMV_Field field : fields)
			names.add(field.getName());
		return names;
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

	/**
	 * Stop any currently playing sounds
	 */
	public void stopAllSounds()
	{
		for(int i=0;i<getCurrentField().getSounds().size();i++)
		{
			if(getCurrentField().getSound(i) != null && getCurrentField().getVideo(i).video != null)
				getCurrentField().getSound(i).stopSound();
		}
	}

	/**
	 * Fade out all media
	 */
	public void fadeOutAllMedia()
	{
		getCurrentField().fadeOutAllMedia();
		fadeOutTerrain(false, false);
	}

	/**
	 * Fade out terrain
	 * @param turnOff
	 * @param message
	 */
	public void fadeOutTerrain(boolean turnOff, boolean message)
	{
		if(state.terrainAlpha != 0.f)
		{
			state.fadingTerrainAlpha = true;		
			state.fadingTerrainStart = state.terrainAlpha;
			state.fadingTerrainTarget = 0.f;
			state.fadingTerrainStartFrame = ml.frameCount;
			state.fadingTerrainEndFrame = ml.frameCount + state.fadingTerrainLength; 
			if(turnOff)
				state.turnOffTerrainAfterFadingOut = true;
			if(message && ml.world.getSettings().screenMessagesOn)
				ml.display.message(ml, "Display Terrain OFF");
		}
	}

	/**
	 * Fade in terrain
	 * @param message
	 */
	public void fadeInTerrain(boolean message)
	{
		if(state.terrainAlpha != state.terrainAlphaMax)
		{
			state.fadingTerrainAlpha = true;		
			state.fadingTerrainStart = state.terrainAlpha;
			state.fadingTerrainTarget = state.terrainAlphaMax;
			state.fadingTerrainStartFrame = ml.frameCount;
			state.fadingTerrainEndFrame = ml.frameCount + state.fadingTerrainLength; 
			if(state.waitingToFadeInTerrainAlpha)
				state.waitingToFadeInTerrainAlpha = false;
			if(message && ml.world.getSettings().screenMessagesOn)
				ml.display.message(ml, "Display Terrain ON");
		}
	}

	/**
	 * Update terrain alpha value
	 */
	void updateFadingTerrainAlpha()
	{
		float newFadeValue = 0.f;
		
		if (ml.frameCount >= state.fadingTerrainEndFrame)
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
			newFadeValue = PApplet.map(ml.frameCount, state.fadingTerrainStartFrame, state.fadingTerrainEndFrame, 
					state.fadingTerrainStart, state.fadingTerrainTarget);  	    // Fade with distance from current time
		}

		state.terrainAlpha = newFadeValue;
	}
	
	/**
	 * Reset variables
	 */
	public void reset(boolean system)
	{
		if(ml.debug.world) ml.systemMessage("World.reset()... Resetting world...");
		
		outputFolder = null;
		outputFolderSelected = false;

		settings.reset();									// Reset world settings
		state.reset();										// Reset world state

		viewer.reset();										// Reset viewer
		fields = null;										// Clear fields

		/* Initialize graphics and text parameters */
		ml.colorMode(PConstants.HSB);
		ml.rectMode(PConstants.CENTER);
		ml.textAlign(PConstants.CENTER, PConstants.CENTER);

		timeFadeMap = new ScaleMap(0., 1., 0., 1.);				// Fading with time interpolation
		timeFadeMap.setMapFunction(circularEaseOut);

		distanceFadeMap = new ScaleMap(0., 1., 0., 1.);			// Fading with distance interpolation
		distanceFadeMap.setMapFunction(circularEaseIn);
		
//		drawForceVector = true;						// Show attraction vector on map (mostly for debugging)
	}
	
	/**
	 * Create a field from each media folder in library
	 */
	public void createFields(ArrayList<String> fieldFolders)
	{
		fields = new ArrayList<WMV_Field>();					// Initialize fields array
		int count = 0;
		
		for(String fieldFolder : fieldFolders)
		{
			if(ml.debug.world) ml.systemMessage("Adding field for folder:"+fieldFolder);
			fields.add(new WMV_Field(settings, state, viewer.getSettings(), viewer.getState(), ml.debug, fieldFolder, count));
			count++;
		}
	}

	/**
	 * Update media file paths after update to library or field folder paths
	 */
	public void updateMediaFilePaths()
	{
		for(WMV_Field f : fields)
		{
			for(WMV_Image i:f.getImages())
				i.updateFilePath(ml, f);
			for(WMV_Panorama n:f.getPanoramas())
				n.updateFilePath(ml, f);
			for(WMV_Video v:f.getVideos())
				v.updateFilePath(ml, f);
			for(WMV_Sound s:f.getSounds())
				s.updateFilePath(ml, f);
		}
	}
	
	/**
	 * Choose starting field
	 */
	public void openChooseFieldDialog()
	{
		viewer.chooseFieldDialog();
	}

	/**
	 * Save world state for current field to file
	 */
	public void saveCurrentFieldState()
	{
		saveFieldState(getCurrentField());
	}
	
	/**
	 * Save world state for given field to file
	 */
	public void saveFieldState(WMV_Field f)
	{
		ml.display.display(ml);

		String folderPath = ml.library.getDataFolder(getCurrentField().getID());
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

		File soundDirectory = new File(soundDataPath);
		if(!soundDirectory.exists()) soundDirectory.mkdir();			// Create directory if doesn't exist

		if(ml.debug.world) PApplet.println("Saving Simulation State to: "+folderPath);
		
//		WMV_Field f = getCurrentField();
		f.captureState();											// Capture current state, i.e. save timeline and dateline

		WMV_ClusterStateList csl = f.captureClusterStates();
		WMV_ImageStateList isl = f.captureImageStates();
		WMV_PanoramaStateList psl = f.capturePanoramaStates();
		WMV_VideoStateList vsl = f.captureVideoStates();
		WMV_SoundStateList ssl = f.captureSoundStates();
		
		ml.library.saveWorldSettings(settings, folderPath+"ml_library_worldSettings.json");
		ml.library.saveWorldState(state, folderPath+"ml_library_worldState.json");
		ml.library.saveViewerSettings(viewer.getSettings(), folderPath+"ml_library_viewerSettings.json");
		ml.library.saveViewerState(viewer.getState(), folderPath+"ml_library_viewerState.json");
		ml.library.saveFieldState(f.getState(), folderPath+"ml_library_fieldState.json");
		ml.library.saveClusterStateList(csl, clusterDataPath+"ml_library_clusterStates.json");
		ml.library.saveImageStateList(isl, imageDataPath+"ml_library_imageStates.json");
		ml.library.savePanoramaStateList(psl, panoramaDataPath+"ml_library_panoramaStates.json");
		ml.library.saveVideoStateList(vsl, videoDataPath+"ml_library_videoStates.json");
		ml.library.saveSoundStateList(ssl, soundDataPath+"ml_library_soundStates.json");
		
		if(ml.world.getSettings().screenMessagesOn)
			ml.display.message(ml, "Saved "+getCurrentField().getName()+"...");
	}

	/**
	 * Save world states for all fields to file
	 */
	public void saveLibrary()
	{
		if(ml.world.getSettings().screenMessagesOn)
			ml.display.message(ml, "Saved Library...");
		ml.display.display(ml);

		for(WMV_Field f : fields)
		{
			String folderPath = ml.library.getDataFolder(f.getID());
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
				dataDirectory.mkdir();			// Create data directory if doesn't exist
			}
			
			File clusterDirectory = new File(clusterDataPath);
			if(!clusterDirectory.exists()) 
				clusterDirectory.mkdir();		// Create cluster directory if doesn't exist

			File imageDirectory = new File(imageDataPath);
			if(!imageDirectory.exists()) 
				imageDirectory.mkdir();			// Create image directory if doesn't exist

			File panoramaDirectory = new File(panoramaDataPath);
			if(!panoramaDirectory.exists()) 
				panoramaDirectory.mkdir();		// Create panorama directory if doesn't exist

			File videoDirectory = new File(videoDataPath);
			if(!videoDirectory.exists()) 
				videoDirectory.mkdir();			// Create video directory if doesn't exist

			File soundDirectory = new File(soundDataPath);
			if(!soundDirectory.exists()) 
				soundDirectory.mkdir();			// Create sound directory if doesn't exist

			if(ml.debug.world && ml.debug.detailed) PApplet.println("Saving Simulation State to: "+folderPath);
			
			f.captureState();											// Capture current state, i.e. save timeline and dateline

			WMV_ClusterStateList csl = f.captureClusterStates();
			WMV_ImageStateList isl = f.captureImageStates();
			WMV_PanoramaStateList psl = f.capturePanoramaStates();
			WMV_VideoStateList vsl = f.captureVideoStates();
			WMV_SoundStateList ssl = f.captureSoundStates();

			ml.library.saveWorldSettings(settings, folderPath+"ml_library_worldSettings.json");
			ml.library.saveWorldState(state, folderPath+"ml_library_worldState.json");
			ml.library.saveViewerSettings(f.getViewerSettings(), folderPath+"ml_library_viewerSettings.json");
			ml.library.saveViewerState(f.getViewerState(), folderPath+"ml_library_viewerState.json");
			ml.library.saveFieldState(f.getState(), folderPath+"ml_library_fieldState.json");
			ml.library.saveClusterStateList(csl, clusterDataPath+"ml_library_clusterStates.json");
			ml.library.saveImageStateList(isl, imageDataPath+"ml_library_imageStates.json");
			ml.library.savePanoramaStateList(psl, panoramaDataPath+"ml_library_panoramaStates.json");
			ml.library.saveVideoStateList(vsl, videoDataPath+"ml_library_videoStates.json");
			ml.library.saveSoundStateList(ssl, soundDataPath+"ml_library_soundStates.json");
			
			if(ml.debug.world) ml.systemMessage("Saved simulation state for field #"+f.getID());
		}
	}

	/**
	 * Load world, field and viewer states and settings from file
	 * @param field Field to load simulation state for
	 * @return Whether successful
	 */
	public WMV_Field loadAndSetSimulationState(WMV_Field field)
	{
		if(ml.debug.world && ml.debug.detailed) PApplet.println("Loading and setting Simulation State... Field #"+field.getID());

		loadAndSetState(field.getID());
		loadAndSetSettings(field.getID());
		loadAndSetViewerState(field.getID());
		loadAndSetViewerSettings(field.getID());
		state.frameCount = ml.frameCount;
		viewer.setFrameCount(ml.frameCount);
		viewer.setCurrentFieldID(field.getID());
		viewer.resetTimeState();
		
		/* Check world and viewer state/settings */
		if(ml.debug.world && ml.debug.detailed)
		{
			if(state != null) ml.systemMessage("WorldState exists...");
			if(settings != null) ml.systemMessage("WorldSettings exists...");
			if(viewer.getState() != null) ml.systemMessage("ViewerState exists...");
			if(viewer.getSettings() != null) ml.systemMessage("ViewerSettings exists...");
		}
		
		String fieldName = field.getName();
		int fieldID = field.getID();
		field = new WMV_Field(settings, state, viewer.getSettings(), viewer.getState(), ml.debug, fieldName, fieldID);
		
		field = loadFieldState(field);
		field.setID(fieldID);
		
		if(ml.debug.world && ml.debug.detailed)
			ml.systemMessage("Loaded and Set Field State... Field #"+field.getID()+" clusters:"+field.getClusters().size());

		return field;
	}
	
	/**
	 * Load field's associated simulation state, i.e. world and viewer states + settings
	 * @param field Field to load state for
	 * @return Field with loaded simulation state
	 */
	public WMV_Field loadSimulationState(WMV_Field field)
	{
		PApplet.println("Loading Simulation State... Field #"+field.getID());

		WMV_WorldState newWorldState = loadState(field.getID());
		WMV_WorldSettings newWorldSettings = loadSettings(field.getID());
		WMV_ViewerState newViewerState = loadViewerState(field.getID());
		WMV_ViewerSettings newViewerSettings = loadViewerSettings(field.getID());
		newWorldState.frameCount = ml.frameCount;

		/* Check world and viewer state/settings */
		if(ml.debug.world && ml.debug.detailed)
		{
			if(newWorldState != null) ml.systemMessage("WorldState exists...");
			if(newWorldSettings != null) ml.systemMessage("WorldSettings exists...");
			if(newViewerState != null) ml.systemMessage("ViewerState exists...");
			if(newViewerSettings != null) ml.systemMessage("ViewerSettings exists...");
		}
		
		String fieldName = field.getName();
		int fieldID = field.getID();
		field = new WMV_Field(newWorldSettings, newWorldState, newViewerSettings, newViewerState, ml.debug, fieldName, fieldID);
		
		field= loadFieldState(field);
		field.setID(fieldID);
		
		if(ml.debug.world && ml.debug.detailed)
			ml.systemMessage("Loaded Field State... Field #"+field.getID()+" clusters:"+field.getClusters().size());

		return field;
	}
	
	/**
	 * Set world and viewer states from saved data in given field
	 * @param field Given field
	 */
	public void setStateFromField(WMV_Field field)
	{
		if(ml.debug.world)
			ml.systemMessage("World.setSimulationStateFromField()... Field #"+field.getID());

		setState( field.getWorldState() );
		setSettings( field.getWorldSettings() );
		viewer.setState( field.getViewerState() );
		viewer.setSettings( field.getViewerSettings() );

		state.frameCount = ml.frameCount;
		viewer.setFrameCount( ml.frameCount );

		if(field.getID() < fields.size())
		{
			viewer.setCurrentFieldID(field.getID());
		}
		else
		{
			if(fields.size() == 1)
			{
				field.setID(0);
				viewer.setCurrentFieldID(0);
			}
			else
			{
				ml.systemMessage("  Error in setting field ID... field.getID():"+field.getID()+" fields.size():"+fields.size());
				ml.exit();
			}
		}
		
		viewer.resetTimeState();

		/* Check world and viewer state/settings */
		if(ml.debug.world && ml.debug.detailed)
		{
			if(state != null) ml.systemMessage("WorldState exists...");
			if(settings != null) ml.systemMessage("WorldSettings exists...");
			if(viewer.getState() != null) ml.systemMessage("ViewerState exists...");
			if(viewer.getSettings() != null) ml.systemMessage("ViewerSettings exists...");
		}
		
		if(ml.debug.world)
		{
			if(getCurrentCluster() != null)
				ml.systemMessage("World.setSimulationStateFromField()... currentCluster id:"+getCurrentCluster().getID()+" cluster location:"+getCurrentCluster().getLocation()+" current location:"+viewer.getLocation());
			else
				ml.systemMessage("World.setSimulationStateFromField()... currentCluster is null!!!");
		}
		
		updateState();				// -- Needed?
//		getCurrentField().updateAllMediaStates();
	}

	/**
	 * Load field state from data folder
	 * @param field Field to load state for
	 * @return Field with new state loaded
	 */
	public WMV_Field loadFieldState(WMV_Field field)
	{
		String dataFolderPath = ml.library.getDataFolder(field.getID());			// Data folder

		String clusterDataPath = dataFolderPath + "ml_library_clusterStates/";
		String imageDataPath = dataFolderPath + "ml_library_imageStates/";
		String panoramaDataPath = dataFolderPath + "ml_library_panoramaStates/";
		String videoDataPath = dataFolderPath + "ml_library_videoStates/";
		String soundDataPath = dataFolderPath + "ml_library_soundStates/";

		WMV_ClusterStateList csl = ml.library.loadClusterStateLists(clusterDataPath);
		
		WMV_ImageStateList isl = ml.library.loadImageStateLists(imageDataPath);
		WMV_PanoramaStateList psl = ml.library.loadPanoramaStateList(panoramaDataPath+"ml_library_panoramaStates.json");
		WMV_VideoStateList vsl = ml.library.loadVideoStateList(videoDataPath+"ml_library_videoStates.json");
		WMV_SoundStateList ssl = ml.library.loadSoundStateList(soundDataPath+"ml_library_soundStates.json");

		/* Load and set field state */
		field.setState( ml, ml.library.loadFieldState( dataFolderPath+"ml_library_fieldState.json" ), csl, isl, psl, vsl, ssl);
		
		ml.metadata.loadGPSTrackFolder(field.getName()); 			// Load GPS track folder
		ml.metadata.loadGPSTrackFiles(field.getName());				// Load GPS track files 
		field.setGPSTracks( ml.metadata.loadGPSTracks(field) );		// Load GPS track(s) as Waypoint list(s) and store in field

		return field;
	}

	/**
	 * Load and set saved world settings for field
	 * @param fieldID Field ID 
	 */
	public void loadAndSetSettings(int fieldID)
	{
		String dataFolder = ml.library.getDataFolder(fieldID);
		setSettings(ml.library.loadWorldSettings(dataFolder+"ml_library_worldSettings.json"));
	}

	/**
	 * Load and set saved world state for field
	 * @param fieldID Field ID 
	 */
	public void loadAndSetState(int fieldID)
	{
		String dataFolder = ml.library.getDataFolder(fieldID);
		setState(ml.library.loadWorldState(dataFolder+"ml_library_worldState.json"));
	}

	/**
	 * Load and set saved viewer settings for field
	 * @param fieldID Field ID 
	 */
	public void loadAndSetViewerSettings(int fieldID)
	{
		String dataFolder = ml.library.getDataFolder(fieldID);
		viewer.setSettings(ml.library.loadViewerSettings(dataFolder+"ml_library_viewerSettings.json"));
	}

	/**
	 * Load and set saved viewer state for field
	 * @param fieldID Field ID 
	 */
	public void loadAndSetViewerState(int fieldID)
	{
		String dataFolder = ml.library.getDataFolder(fieldID);
		viewer.setState(ml.library.loadViewerState(dataFolder+"ml_library_viewerState.json"));
	}

	/**
	 * Load saved world settings for field
	 * @param fieldID Field ID
	 * @return Saved world settings for field
	 */
	public WMV_WorldSettings loadSettings(int fieldID)
	{
		String dataFolder = ml.library.getDataFolder(fieldID);
		return ml.library.loadWorldSettings(dataFolder+"ml_library_worldSettings.json");
	}

	/**
	 * Load saved world state for field
	 * @param fieldID Field ID
	 * @return Saved world state for field
	 */
	public WMV_WorldState loadState(int fieldID)
	{
		String dataFolder = ml.library.getDataFolder(fieldID);
		return ml.library.loadWorldState(dataFolder+"ml_library_worldState.json");
	}

	/**
	 * Load saved viewer settings for field
	 * @param fieldID Field ID
	 * @return Saved viewer settings for field
	 */
	public WMV_ViewerSettings loadViewerSettings(int fieldID)
	{
		String dataFolder = ml.library.getDataFolder(fieldID);
		return ml.library.loadViewerSettings(dataFolder+"ml_library_viewerSettings.json");
	}

	/**
	 * Load saved viewer state for field
	 * @param fieldID Field ID
	 * @return Saved viewer state for field
	 */
	public WMV_ViewerState loadViewerState(int fieldID)
	{
		String dataFolder = ml.library.getDataFolder(fieldID);
		return ml.library.loadViewerState(dataFolder+"ml_library_viewerState.json");
	}

	/**
	 * Transition alpha from current to given value
	 */
	public void startFadingAlpha(float target)
	{
		if(target != state.alpha)			// Check if already at target
		{
			state.beginFadingAlpha = true;
			state.fadingAlpha = true;   
			state.fadingAlphaStart = state.alpha;
			state.fadingAlphaTarget = target;
			state.fadingAlphaStartFrame = state.frameCount;
			state.fadingAlphaEndFrame = state.fadingAlphaStartFrame + state.fadingAlphaLength;
			if(getSettings().screenMessagesOn)
				ml.display.message(ml, "Set Alpha to: "+target);
		}
		else
		{
			state.fadingAlpha = false;
		}
	}
	
	/**
	 * Update alpha each frame
	 */
	public void updateFadingAlpha()
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

//	state.fadingModelAlpha = true;		
//	state.fadingModelAlphaStartFrame = ml.frameCount;
//	state.fadingModelAlphaEndFrame = ml.frameCount + state.fadingModelAlphaLength;
//	state.fadingModelAlphaStart = ml.world.state.modelAlpha;
//	state.fadingModelAlphaTarget = newAlpha;

	/**
	 * Update alpha each frame
	 */
	public void updateModelAlphaFading()
	{
		float newAlphaFadeValue = 0.f;

		if (state.frameCount >= state.fadingModelAlphaEndFrame)
		{
			state.fadingModelAlpha = false;
			newAlphaFadeValue = state.fadingModelAlphaTarget;
			
			if(newAlphaFadeValue == 0.f)			// Model faded out
				state.showModel = false;

//			System.out.println("World.updateModelAlphaFading()... >>> Reached transition end... newAlphaFadeValue:"+newAlphaFadeValue+" state.fadingModelAlphaStart:"+state.fadingModelAlphaStart+" state.fadingModelAlphaTarget:"+state.fadingModelAlphaTarget);
		} 
		else
		{
			newAlphaFadeValue = PApplet.map(state.frameCount, state.fadingModelAlphaStartFrame, state.fadingModelAlphaEndFrame, state.fadingModelAlphaStart, state.fadingModelAlphaTarget);      // Fade with distance from current time
		}
		
//		System.out.println("World.updateModelAlphaFading()... newAlphaFadeValue:"+newAlphaFadeValue+" state.fadingModelAlphaStart:"+state.fadingModelAlphaStart+" state.fadingModelAlphaTarget:"+state.fadingModelAlphaTarget);
//		System.out.println("World.updateModelAlphaFading()...  state.fadingModelAlphaStartFrame:"+state.fadingModelAlphaStartFrame+" state.fadingModelAlphaEndFrame:"+state.fadingModelAlphaEndFrame);

		state.modelAlpha = newAlphaFadeValue;
	}

	/**
	 * Get current world settings
	 * @return Get current world settings
	 */
	public WMV_WorldSettings getSettings()
	{
		return settings;
	}
	
	/**
	 * Get current world state
	 * @return Get current world state
	 */
	public WMV_WorldState getState()
	{
		return state;
	}
	
	/**
	 * Get current field
	 * @return Current field
	 */
	public WMV_Field getCurrentField()
	{
		WMV_Field f = fields.get(viewer.getCurrentFieldID());
		return f;
	}
	
	/**
	 * Get all fields in world
	 * @return All fields in world
	 */
	public ArrayList<WMV_Field> getFields()
	{
		return fields;
	}
	
	/**
	 * Get model of current field
	 * @return Model of current field
	 */
	public WMV_Model getCurrentModel()
	{
		WMV_Model m = getCurrentField().getModel();
		return m;
	}
	
	/**
	 * Get current cluster
	 * @return Current cluster
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
	 * Get cluster currently responsible for time in Cluster Time Mode (current cluster, unless moving)		-- Obsolete?
	 * @return Cluster currently responsible for time in Cluster Time Mode
	 */
	public WMV_Cluster getClusterWithCurrentTime()
	{
		WMV_Cluster c;
		if(viewer.getState().movingToCluster && viewer.getState().movingToTimeSegment)
		{
			if(viewer.isTeleporting())
				c = getCurrentField().getCluster(viewer.getState().teleportGoalClusterID);
			else 
				c = getCurrentField().getCluster(viewer.getState().attractorClusterID);
		}
		else
			c = getCurrentCluster();
		return c;
	}
	
	public int getCurrentDate()
	{
		return getState().currentDate;
	}
	
	/**
	 * Increment time cycle length
	 */
	public void incrementTimeCycleLength()
	{
		int cycleMax = settings.maxTimeCycleLength;
		int cycleLength;
		if(ml.display.window.setupTimeWindow)
		{
			cycleLength = ml.display.window.sdrTimeCycleLength.getValueI();
			cycleMax = (int) ml.display.window.sdrTimeCycleLength.getEndLimit();
		}
		else
		{
			if(state.timeMode == 0)
			{
				WMV_Cluster c = getCurrentCluster();
				if(c != null)
					cycleLength = c.getTimeCycleLength();
				else
					cycleLength = settings.timeCycleLength;
				
				if(cycleLength + 20 < cycleMax)
					setClusterTimeCycleLength(cycleLength + 20, true);
			}
			else
			{
				cycleLength = settings.timeCycleLength;
				if(cycleLength + 20 < cycleMax)
					setTimeCycleLength(cycleLength + 20, true);
			}
		}

		if(ml.display.window.setupTimeWindow)
		{
			ml.display.updateTimeWindowCurrentTime();
			ml.display.window.sdrTimeCycleLength.setValue(settings.timeCycleLength);
		}
	}
	
	/**
	 * Decrement time cycle length
	 */
	public void decrementTimeCycleLength()
	{
		int cycleLength;
		if(ml.display.window.setupTimeWindow)
			cycleLength = ml.display.window.sdrTimeCycleLength.getValueI();
		else
		{
			if(state.timeMode == 0)
			{
				WMV_Cluster c = getCurrentCluster();
				if(c != null)
					cycleLength = c.getTimeCycleLength();
				else
					cycleLength = settings.timeCycleLength;
				if(cycleLength - 20 > 0)
					setClusterTimeCycleLength(cycleLength - 20, true);
			}
			else
			{
				cycleLength = settings.timeCycleLength;
				if(cycleLength - 20 > 0)
					setTimeCycleLength(cycleLength - 20, true);
			}
		}
		
		if(ml.display.window.setupTimeWindow)
		{
			ml.display.updateTimeWindowCurrentTime();
			ml.display.window.sdrTimeCycleLength.setValue(settings.timeCycleLength);
		}
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

		ArrayList<WMV_Cluster> clusterList = getCurrentField().getClusters();
		for(WMV_Cluster c : clusterList)	// Adjust points within cluster viewing distance to cluster height
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
					ml.stroke(0.f, 0.f, 155.f, state.terrainAlpha * 0.33f);
				else
					ml.stroke(0.f, 0.f, 255.f, state.terrainAlpha);

				ml.strokeWeight(6.f);
				ml.point(pv.x, pv.y, pv.z);				
				
				ml.strokeWeight(1.f);
				if(col-1 >= 0)
				{
					PVector pt2 = gridPoints.get(row).get(col-1);
					ml.line(pv.x, pv.y, pv.z, pt2.x, pt2.y, pt2.z);
				}
				if(col+1 < pvList.size())
				{
					PVector pt2 = gridPoints.get(row).get(col+1);
					ml.line(pv.x, pv.y, pv.z, pt2.x, pt2.y, pt2.z);
				}
				if(row-1 >= 0)
				{
					PVector pt2 = gridPoints.get(row-1).get(col);
					ml.line(pv.x, pv.y, pv.z, pt2.x, pt2.y, pt2.z);
				}
				if(row+1 < gridPoints.size())
				{
					PVector pt2 = gridPoints.get(row+1).get(col);
					ml.line(pv.x, pv.y, pv.z, pt2.x, pt2.y, pt2.z);
				}
				
				col++;
			}
			
			row++;
		}
	}

	/**
	 * Save current screen view to disk
	 */
	public void exportCurrentView() 
	{
		if(ml.debug.ml && ml.debug.detailed) ml.systemMessage("Will save screenshot to disk.");
		ml.state.export = true;
	}

	/**
	 * Save current screen view to disk
	 */
	public void exportCurrentMedia() 
	{
		if(ml.debug.ml && ml.debug.detailed) ml.systemMessage("Will output selected media file(s) to disk.");
		ml.state.exportMedia = true;
	}

	/**
	 * Export currently selected media to disk
	 */
	public void exportSelectedMedia()
	{
		List<Integer> selected;
		
		selected = getCurrentField().getSelectedMediaOfType(0);
		if(selected.size() > 0) exportImages(selected);
		
		selected = getCurrentField().getSelectedMediaOfType(1);
		if(selected.size() > 0) exportPanoramas(selected);
		
		selected = getCurrentField().getSelectedMediaOfType(2);
		if(selected.size() > 0) exportVideos(selected);
		
		selected = getCurrentField().getSelectedMediaOfType(3);
		if(selected.size() > 0) exportSounds(selected);
	}

	public void exportImages(List<Integer> imageIDs)
	{
		for(int i:imageIDs)
		{
			InputStream is = null;
			OutputStream os = null;
			try {
				File imgFilePath;
				
				WMV_Image img = getCurrentField().getImage(i);
				if(img.hasOriginal())
					imgFilePath = new File(img.getOriginalPath());
				else
					imgFilePath = new File(img.getFilePath());

				is = new FileInputStream(imgFilePath);
				os = new FileOutputStream(new File(outputFolder + "/" + img.getName()));
				byte[] buffer = new byte[1024];
				int length;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
			} 
			catch(Throwable t)
			{
				ml.systemMessage("ERROR 1 in exportImages:"+t);
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
					ml.systemMessage("ERROR 2 in exportImages:"+e);
				}
			}
		}
	}

	/**
	 * Export given panoramas
	 * @param panoramaIDs List of panorama IDs
	 */
	public void exportPanoramas(List<Integer> panoramaIDs)
	{
		for(int i:panoramaIDs)
		{
			InputStream is = null;
			OutputStream os = null;
			try {
				File panoFilePath;
				
				WMV_Panorama pano = getCurrentField().getPanorama(i);
				panoFilePath = new File(pano.getFilePath());

				is = new FileInputStream(panoFilePath);
				os = new FileOutputStream(new File(outputFolder + "/" + pano.getName()));
				byte[] buffer = new byte[1024];
				int length;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
			} 
			catch(Throwable t)
			{
				ml.systemMessage("ERROR 1 in exportPanoramas:"+t);
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
					ml.systemMessage("ERROR 2 in exportPanoramas:"+e);
				}
			}
		}
	}
	
	public void exportVideos(List<Integer> videoIDs)
	{
		for(int i:videoIDs)
		{
			InputStream is = null;
			OutputStream os = null;
			try {
				File vidFilePath;
				
				WMV_Video vid = getCurrentField().getVideo(i);
				if(vid.hasOriginal())
					vidFilePath = new File(vid.getOriginalPath());
				else
					vidFilePath = new File(vid.getFilePath());

				is = new FileInputStream(vidFilePath);
				os = new FileOutputStream(new File(outputFolder + "/" + vid.getName()));
				byte[] buffer = new byte[1024];
				int length;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
			} 
			catch(Throwable t)
			{
				ml.systemMessage("ERROR 1 in exportVideos:"+t);
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
					ml.systemMessage("ERROR 2 in exportVideos:"+e);
				}
			}
		}
	}

	/**
	 * Export given sounds
	 * @param soundIDs
	 */
	public void exportSounds(List<Integer> soundIDs)
	{
		for(int i:soundIDs)
		{
			InputStream is = null;
			OutputStream os = null;
			try {
				File sndFilePath;
				
				WMV_Sound snd = getCurrentField().getSound(i);
				sndFilePath = new File(snd.getFilePath());

				is = new FileInputStream(sndFilePath);
				os = new FileOutputStream(new File(outputFolder + "/" + snd.getName()));
				byte[] buffer = new byte[1024];
				int length;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
			} 
			catch(Throwable t)
			{
				ml.systemMessage("ERROR 1 in exportSounds:"+t);
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
					ml.systemMessage("ERROR 2 in exportSounds:"+e);
				}
			}
		}
	}

	/**
	 * Save six current screen cubemap views to disk		// -- In progress
	 */
	public void saveCubeMapToDisk() 
	{
		if(ml.debug.ml && ml.debug.detailed) ml.systemMessage("Will output cubemap images to disk.");
		ml.state.exportCubeMap = true;
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
			ml.systemMessage("World.getField()... ERROR: fieldIndex:"+fieldIndex+" fields.size():"+fields.size());
			return null;
		}
	}

	/**
	 * @return All images in current field
	 */
	public ArrayList<WMV_Image> getCurrentFieldImages()
	{
		ArrayList<WMV_Image> iList = getCurrentField().getImages();
		return iList;
	}
	
	/**
	 * @return All panoramas in field
	 */
	public ArrayList<WMV_Panorama> getCurrentFieldPanoramas()
	{
		ArrayList<WMV_Panorama> pList = getCurrentField().getPanoramas();
		return pList;
	}
	
	/**
	 * @return All videos in current field
	 */
	public ArrayList<WMV_Video> getCurrentFieldVideos()
	{
		ArrayList<WMV_Video> iList = getCurrentField().getVideos();
		return iList;
	}
	
	/**
	 * @return All clusters in current field
	 */
	public ArrayList<WMV_Cluster> getCurrentFieldClusters()
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
	 * @param newField New field to set
	 * @param fieldIndex Field ID 
	 */
	public void setField(WMV_Field newField, int fieldIndex)
	{
		fields.set(fieldIndex, newField);
	}

	/**
	 * Determine and set the length of the Main Time Cycle
	 */
	public void createTimeCycle()
	{
//		if(state.timeMode == 0 || state.timeMode == 1)
//		{
////			settings.timeCycleLength = settings.defaultTimeCycleLength;
//		}
		if(state.timeMode == 2)		/* Time cycle length is flexible according to visible cluster media lengths */
		{
			ArrayList<WMV_Cluster> cl = getVisibleClusters();
			settings.timeCycleLength = 0;
			
			for(WMV_Cluster c : cl)
			{
				ArrayList<WMV_Image> imageList = getCurrentField().getImagesInCluster( c.getID(), getCurrentField().getImages() );
				if(imageList != null) settings.timeCycleLength += imageList.size() * settings.staticMediaLength;
				ArrayList<WMV_Panorama> panoramaList = getCurrentField().getPanoramasInCluster( c.getID(), getCurrentField().getPanoramas() );
				if(panoramaList != null) settings.timeCycleLength += panoramaList.size() * settings.staticMediaLength;
				
				for(WMV_Video v: getCurrentField().getVideosInCluster( c.getID(), getCurrentField().getVideos()) )
					settings.timeCycleLength += PApplet.round( v.getLength() * 29.98f );		// Add videos' actual (approx.) lengths
				
//				for(WMV_Sound s: c.getSounds( getCurrentField().getSounds() )
//					settings.timeCycleLength += PApplet.round( s.getLength() * 29.98f );		// Add sounds' actual (approx.) lengths -- Obsolete??
			}
			
			if(cl.size() == 1)
			{
				if(cl.get(0).getTimeline().timeline.size() > 0)
					viewer.setNearbyClusterTimeline(cl.get(0).getTimeline().timeline);
				else
					ml.systemMessage("CreateTimeCycle Error... Cluster #"+cl.get(0).getID()+"  getTimeline().size() == 0!");
			}
			else if(cl.size() > 1)
			{
				viewer.createNearbyClusterTimeline(cl);
			}
				
			if(cl.size() == 0)
				settings.timeCycleLength = -1;				// Flag viewer to keep calling this method until clusters are visible
			else
				startMediaTimeModeCycle();
		}
		else if(state.timeMode == 3)						/* Time cycle length is flexible according to visible cluster timelines */
		{
			float highest = -100000.f;
			float lowest = 100000.f;
			ArrayList<WMV_Cluster> cl = getVisibleClusters();
			
			for(WMV_Cluster c : cl)
			{
				float low = c.getTimeline().timeline.get(0).getLower().getAbsoluteTime();
				if(low < lowest)
					lowest = low;
				float high = c.getTimeline().timeline.get(c.getTimeline().timeline.size()-1).getUpper().getAbsoluteTime();
				if(high > highest)
					highest = high;
			}
			
			if(cl.size() == 0)
				settings.timeCycleLength = -1;
		}
	}

	/**
	 * Begin cycle in Media Time Mode
	 */
	public void startMediaTimeModeCycle()
	{
		setMediaTimeModeCurrentMedia(0);
	}
	
	/**
	 * Set current visible media in Media Time Mode
	 * @param timeSegmentIndex
	 */
	public void setMediaTimeModeCurrentMedia(int timeSegmentIndex)
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
					viewer.setCurrentMediaStartTime(getCurrentFieldTimeCyclePoint());
					viewer.setNextMediaStartTime(getCurrentFieldTimeCyclePoint() + settings.staticMediaLength);
//					if(viewer.lookAtCurrentMedia()) viewer.lookAtMedia(i.getID(), 0);
					break;
				case 1:
					WMV_Panorama n = getCurrentField().getPanorama(curMediaID);
					n.getMediaState().isCurrentMedia = true;
					viewer.setCurrentMediaStartTime(getCurrentFieldTimeCyclePoint());
					viewer.setNextMediaStartTime(getCurrentFieldTimeCyclePoint() + settings.staticMediaLength);
//					viewer.lookAtMedia(n.getID(), 1);
					break;
				case 2:	
					WMV_Video v = getCurrentField().getVideo(curMediaID);
					v.getMediaState().isCurrentMedia = true;
					viewer.setCurrentMediaStartTime(getCurrentFieldTimeCyclePoint());
					viewer.setNextMediaStartTime(getCurrentFieldTimeCyclePoint() + PApplet.round( getCurrentField().getVideo(curMediaID).getLength() * 29.98f));
//					if(viewer.lookAtCurrentMedia()) viewer.lookAtMedia(v.getID(), 2);
					break;
//				case 3:	
//					getCurrentField().sounds.get(curMediaID).currentMedia = true;
//					viewer.nextMediaStartFrame = state.frameCount + PApplet.round( getCurrentField().sounds.get(curMediaID).getLength() * 29.98f );
//					break;
				}
			}
			else
			{
				ml.systemMessage("ERROR in setSingleTimeModeCurrentMedia... time == null!!... timelineIndex:"+timeSegmentIndex);
			}
		}
		else
			ml.systemMessage("ERROR in setSingleTimeModeCurrentMedia  viewer.nearbyClusterTimeline.size() == 0!!");
	}
	
	/**
	 * Set image orientation
	 */
	public void setImageOrientation()			// -- To Do
	{
		
	}
	
	/**
	 * Set panorama orientation
	 */
	public void setPanoramaOrientation()			// -- To Do
	{
		
	}

	/**
	 * @param newTimeMode New time mode {0: Cluster, 1:Field, 2: Media}
	 * @param updateToggle
	 */
	public void setTimeMode(int newTimeMode, boolean updateToggle)
	{
		state.timeMode = newTimeMode;
		if(ml.display.window.setupTimeWindow)
		{
			switch(state.timeMode)
			{
				case 0:														/* Cluster */
					if(updateToggle) ml.display.window.optClusterTimeMode.setSelected(true);
					ml.display.window.optFieldTimeMode.setSelected(false);
					if(ml.display.window.sdrClusterLength.isVisible())
						ml.display.window.sdrClusterLength.setVisible(false);
					if(ml.display.window.lblClusterLength.isVisible())
						ml.display.window.lblClusterLength.setVisible(false);
					if(ml.world.getSettings().screenMessagesOn)
						ml.display.message(ml, "Set Time Mode to: Location");
					if(ml.display.window.setupTimeWindow)
						ml.display.updateTimeWindowCurrentTime();
					break;
				case 1:														/* Field */
					ml.display.window.optClusterTimeMode.setSelected(false);
					if(updateToggle) ml.display.window.optFieldTimeMode.setSelected(true);
					if(!ml.display.window.sdrClusterLength.isVisible())
						ml.display.window.sdrClusterLength.setVisible(true);
					if(!ml.display.window.lblClusterLength.isVisible())
						ml.display.window.lblClusterLength.setVisible(true);
					if(ml.world.getSettings().screenMessagesOn)
						ml.display.message(ml, "Set Time Mode to: Field");
					if(ml.display.window.setupTimeWindow)
						ml.display.updateTimeWindowCurrentTime();
					break;
			}		
			if(ml.world.state.timeFading) ml.display.window.sdrCurrentTime.setValue(ml.world.getCurrentTime());
		}
	}
	
	/**
	 * Show any image in field if visible
	 */
	public void showImages()
	{
		for(WMV_Image i : getCurrentField().getImages())
			if(!i.isDisabled())
				i.setHidden(false);						

		if(ml.display.window.setupPreferencesWindow)
			ml.display.window.chkbxHideImages.setSelected(false);
		
		if(getSettings().screenMessagesOn)
			ml.display.message(ml, "Hiding Images OFF");
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
				i.fadeOut(getCurrentField(), true);						// Fade out and hide after
			}
		}

		if(ml.display.window.setupPreferencesWindow)
			ml.display.window.chkbxHideImages.setSelected(true);
		
		if(getSettings().screenMessagesOn)
			ml.display.message(ml, "Hiding Images ON");
	}
	
	/** 
	 * Show any panorama in field if visible
	 */
	public void showPanoramas()
	{
		for(WMV_Panorama n : getCurrentField().getPanoramas())
			if(!n.isDisabled())
				n.setHidden(false);						

		if(ml.display.window.setupPreferencesWindow)
			ml.display.window.chkbxHidePanoramas.setSelected(false);
		
		if(getSettings().screenMessagesOn)
			ml.display.message(ml, "Hiding Panoramas OFF");
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
				n.fadeOut(getCurrentField(), true);						// Fade out and hide after
			}
		}
		
		for(WMV_Cluster c : getCurrentField().getClusters())
		{
			if(c.stitched.size() > 0)
			{
				for(WMV_Panorama n : c.stitched)
				{
					if(n.isFading()) n.stopFading();
					n.fadeOut(getCurrentField(), true);					// Fade out and hide after
				}
			}
		}
		
		if(ml.display.window.setupPreferencesWindow)
			ml.display.window.chkbxHidePanoramas.setSelected(true);
		
		if(getSettings().screenMessagesOn)
			ml.display.message(ml, "Hiding Panoramas ON");
	}
	
	/**
	 * Show any video in field if visible
	 */
	public void showVideos()
	{
		for(WMV_Video v : getCurrentField().getVideos())
			if(!v.isDisabled())
				v.setHidden(false);						

		if(ml.display.window.setupPreferencesWindow)
			ml.display.window.chkbxHideVideos.setSelected(false);
		
		if(getSettings().screenMessagesOn)
			ml.display.message(ml, "Hiding Videos OFF");
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
				v.fadeOut(getCurrentField(), true);
			}
		}
		
		if(ml.display.window.setupPreferencesWindow)
			ml.display.window.chkbxHideVideos.setSelected(true);
		
		if(getSettings().screenMessagesOn)
			ml.display.message(ml, "Hiding Videos ON");
	}

	
	/**
	 * Show any sound in field if visible
	 */
	public void showSounds()
	{
		for(WMV_Sound s : getCurrentField().getSounds())
			if(!s.isDisabled())
				s.setHidden(false);						

		if(ml.display.window.setupPreferencesWindow)
			ml.display.window.chkbxHideSounds.setSelected(false);
	}
	
	/**
	 * Hide all sounds in field
	 */
	public void hideSounds()
	{
		for(WMV_Sound s : getCurrentField().getSounds())
		{
			if(s.getMediaState().visible)
			{
				if(s.isFading()) s.stopFading();
				s.fadeOut(getCurrentField(), true);
			}
		}
		
		if(ml.display.window.setupPreferencesWindow)
			ml.display.window.chkbxHideSounds.setSelected(true);
	}

	/**
	 * Deselect all media in field
	 */
	public void deselectAllMedia(boolean hide) 
	{
		getCurrentField().deselectAllMedia(hide);
		ml.display.clearMetadata();
	}

	/**
	 * Change all clusters to non-attractors
	 */
	public void clearAllAttractors()
	{
		if(ml.debug.viewer && ml.debug.detailed)
			ml.systemMessage("Clearing all attractors...");

		if(viewer.getAttractorClusterID() != -1)
			viewer.clearAttractorCluster();

		getCurrentField().clearAllAttractors();
	}
	
	/**
	 * Set world settings
	 * @param newSettings New world settings
	 */
	public void setSettings(WMV_WorldSettings newSettings)
	{
		settings = newSettings;
	}
	
	/**
	 * Set world state
	 * @param newState New world state
	 */
	public void setState(WMV_WorldState newState)
	{
		state = newState;
	}
	
	/**
	 * Add field to world
	 * @param f Field to add
	 */
	public void addField(WMV_Field f)
	{
		fields.add(f);
	}
	
	/**
	 * Add field to world
	 * @param f Field to add
	 */
	public void removeField(WMV_Field f)
	{
		fields.remove(f);
	}
	
	/**
	 * Get visible clusters in standard viewing mode
	 * @return Active clusters in current field
	 */
	public ArrayList<WMV_Cluster> getVisibleClusters()
	{
		float maxClusterDistance = settings.maxClusterDistance * viewer.getClusterDistanceVisibilityFactor();
		
		ArrayList<WMV_Cluster> nearClusters = viewer.getNearClusters(settings.maxVisibleClusters, maxClusterDistance);
		ArrayList<WMV_Cluster> clusters = new ArrayList<WMV_Cluster>();
		
		if(nearClusters != null)
		{
			for(WMV_Cluster c : nearClusters)
			{
				if(c.isActive() && !c.isEmpty())
				{
					boolean visible = true;
					boolean error = false;
					
					if(state.timeFading)		// Time fading in Field Time Mode
					{
						if(settings.clusterLength < 1.f)
						{
							visible = false;
							switch(state.timeMode)
							{
								case 0:						// Time Mode 0: Cluster
									visible = true;			// Always visible
									break;
	
								case 1:						// Time Mode 1: Field 
									float first; 			// First visible point in time cycle
									float last;				// Last visible point in time cycle
									float center;			// Center of visibility interval
									
									int firstID = c.getFirstTimeSegmentFieldTimelineID(true);
									int lastID = c.getLastTimeSegmentFieldTimelineID(true);
									
									if(firstID >= 0 && firstID < c.getTimeline().timeline.size())
									{
										first = c.getTimeline().timeline.get( firstID ).getLower().getAbsoluteTime();
									}
									else
									{
										if(ml.debug.ml)
										{
											ml.systemMessage("World.getVisibleClusters()... ERROR: firstID: "+firstID+" > timeline.size():"+c.getTimeline().timeline.size());
											if(lastID >= 0 && lastID < c.getTimeline().timeline.size())
												ml.systemMessage("World.getVisibleClusters()... ERROR: lastID: "+lastID+" > timeline.size():"+c.getTimeline().timeline.size());
										}
										error = true;
										break;
									}
									
									if(lastID >= 0 && lastID < c.getTimeline().timeline.size())
									{
										last = c.getTimeline().timeline.get( lastID ).getUpper().getAbsoluteTime();
									}
									else
									{
										if(ml.debug.ml)
											ml.systemMessage("World.getVisibleClusters()... ERROR: lastID: "+lastID+" > timeline.size():"+c.getTimeline().timeline.size());
										error = true;
										break;
									}
									if(first == last)
										center = first;
									else
										center = first + last * 0.5f;
	
									float current = utilities.mapValue(getCurrentFieldTimeCyclePoint(), 0, settings.timeCycleLength, 0.f, 1.f);
									float timeDiff = (float)Math.abs(current-center);			// Find time offset from center
									if(timeDiff <= settings.clusterLength) 					// Compare offset to cluster length
										visible = true;
									break;
	
								case 2:
									visible = true;
									break;
							}
							if(error)
							{
								if(ml.debug.ml)
									ml.systemMessage("World.getVisibleClusters()... Cluster timeline Error 1... timeline.size():"+c.getTimeline().timeline.size());
							}
						}

						if(visible) 
							clusters.add(c);
					}
					else
						clusters.add(c);
				}
			}

			return clusters;
		}

		return new ArrayList<WMV_Cluster>();
	}

	/**
	 * Get visible clusters in standard viewing mode
	 * @return Active clusters in current field
	 */
	public List<Integer> getVisibleClusterIDs()
	{
		ArrayList<WMV_Cluster> visible = getVisibleClusters();
		List<Integer> clusters = new ArrayList<Integer>();
		
		if(visible != null)
		{
			for(WMV_Cluster c : visible)
			{
				clusters.add(c.getID());
			}
			return clusters;
		}
		else 
			return new ArrayList<Integer>();
	}

	/**
	 * Set specified panorama's blur mask
	 * @param panorama
	 */
	public void setPanoramaBlurMask(WMV_Panorama panorama)
	{
		WMV_Field f = getCurrentField();
		f.setPanoramaBlurMask(panorama, blurMaskPanorama);
	}
	
	/**
	 * Set specified horizontal image's blur mask
	 * @param image
	 * @param blurMaskID
	 */
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
//			ml.systemMessage("blurMaskBothBoth added to image #"+image.getID()+" name:"+image.getName());
//			ml.systemMessage("	mask == null?"+image.blurMask);
			break;
		}
	}

	/**
	 * Set specified vertical image's blur mask
	 * @param image
	 * @param blurMaskID
	 */
	public void setVerticalBlurMask(WMV_Image image, int blurMaskID)
	{
		WMV_Field f = getCurrentField();
		switch(blurMaskID)
		{
		case 0:
			f.setImageBlurMask(image, vertBlurMaskLeftTop);
			break;
		case 1:
			f.setImageBlurMask(image, vertBlurMaskLeftCenter);
			break;
		case 2:
			f.setImageBlurMask(image, vertBlurMaskLeftBottom);
			break;
		case 3:
			f.setImageBlurMask(image, vertBlurMaskLeftBoth);
			break;
		
		case 4:
			f.setImageBlurMask(image, vertBlurMaskCenterTop);
			break;
		case 5:
			f.setImageBlurMask(image, vertBlurMaskCenterCenter);
			break;
		case 6:
			f.setImageBlurMask(image, vertBlurMaskCenterBottom);
			break;
		case 7:
			f.setImageBlurMask(image, vertBlurMaskCenterBoth);
			break;
	
		case 8:
			f.setImageBlurMask(image, vertBlurMaskRightTop);
			break;
		case 9:
			f.setImageBlurMask(image, vertBlurMaskRightCenter);
			break;
		case 10:
			f.setImageBlurMask(image, vertBlurMaskRightBottom);
			break;
		case 11:
			f.setImageBlurMask(image, vertBlurMaskRightBoth);
			break;
	
		case 12:
			f.setImageBlurMask(image, vertBlurMaskBothTop);
			break;
		case 13:
			f.setImageBlurMask(image, vertBlurMaskBothCenter);
			break;
		case 14:
			f.setImageBlurMask(image, vertBlurMaskBothBottom);
			break;
		case 15:
			f.setImageBlurMask(image, vertBlurMaskBothBoth);
			break;
		}
	}

	/**
	 * Set specified video's blur mask
	 * @param video
	 * @param blurMaskID
	 */
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

	/**
	 * Set image blur masks
	 */
	public void setBlurMasks()
	{

		for(WMV_Field f : fields)
		{
//			ml.systemMessage("World.setBlurMasks()... f.getImages().size():"+f.getImages().size());
			
			for(WMV_Image image : f.getImages())
			{
				int bmID = image.getState().blurMaskID;
				
				if(image.getWidth() == 640 && image.getHeight() == 480) 
					setBlurMask(image, bmID);
				else if(image.getWidth() == 480 && image.getHeight() == 640) 
					setVerticalBlurMask(image, bmID);
				else
				{
					if(ml.debug.image)
					{
						ml.systemMessage("World.setBlurMasks()... ERROR: Could not set mask... image has size other than 640x480 or 480x640!"+image.getName());
						ml.systemMessage("Setting image to disabled..."+image.getName());
					}
					image.setDisabled(true);
				}
			}
			
			for(WMV_Panorama panorama : f.getPanoramas())
			{
				if(panorama.getWidth() == 5376 && panorama.getHeight() == 2688) 
				{
					setPanoramaBlurMask(panorama);				// Should check width / height if possible
				}
				else
				{
					if(ml.debug.panorama)
					{
						ml.systemMessage("World.setBlurMasks()... ERROR: Could not set mask... panorama has size other than 5376x2688!"+panorama.getName());
					}
				}
			}
			
			for(WMV_Video video : f.getVideos())
			{
				int bmID = video.getState().blurMaskID;
				if(video.getWidth() == 640 && video.getHeight() == 360) 
					setVideoBlurMask(video, bmID);				// Should check width / height if possible
				else
				{
					if(ml.debug.video)
					{
						ml.systemMessage("World.setBlurMasks()... ERROR: Could not set mask... video has size other than 640x360!"+video.getName()+
								" width:"+video.getWidth()+" height:"+video.getHeight());
						ml.systemMessage("Setting video to disabled..."+video.getName());
					}
					video.setDisabled(true);
				}
			}
		}
	}


//	public void updateSelectionMaxDistance()
//	{
//	//	public float selectionMaxDistanceFactor = 2.f;		// Scaling from defaultFocusDistanceFactor to selectionMaxDistance
//	//	public float selectionMaxDistance = defaultFocusDistance * selectionMaxDistanceFactor;			// Maximum distance user can select media item
//	
//		viewer.setSelectionMaxDistance( viewer.getSettings().selectionMaxDistanceFactor * 
//									    viewer.getSettings().defaultFocusDistance );
//	}
	

	/**
	 * Get mask image resource
	 * @param maskPath Mask path ("masks_image", "masks_image_vert", or "masks_video")
	 * @param fileName File name
	 * @return Mask image
	 */
	private PImage getMaskImageResource(String maskPath, String fileName)
	{
		BufferedImage image;
		
		URL imageURL = MultimediaLocator.class.getResource(maskPath + fileName);
		try{
			image = ImageIO.read(imageURL.openStream());
			return utilities.bufferedImageToPImage(image);
		}
		catch(Throwable t)
		{
			ml.systemMessage("World.getMaskImageResource()... ERROR t:"+t+" imageURL == null? "+(imageURL == null));
		}
		
		return null;
	}
	
	/**
	 * Set Time Fading setting
	 * @param newShowMetadata New Time Fading setting
	 */
	public void turnTimeFadingOn()
	{
		state.turningOnTimeFading = true;
		fadeOutAllMedia();
		
//		state.timeFading = true;
		
//		if(ml.world.getSettings().screenMessagesOn)
//		{
//			if(newTimeFading)
//				ml.display.message(ml, "Time Fading ON");
//			else
//				ml.display.message(ml, "Time Fading OFF");
//		}
	}
	/**
	 * Set Time Fading setting
	 * @param newShowMetadata New Time Fading setting
	 */
	public void turnTimeFadingOff()
	{
		state.turningOffTimeFading = true;
		fadeOutAllMedia();

//		state.timeFading = false;
		
//		if(ml.world.getSettings().screenMessagesOn)
//		{
//			if(newTimeFading)
//				ml.display.message(ml, "Time Fading ON");
//			else
//				ml.display.message(ml, "Time Fading OFF");
//		}
	}
	
	/**
	 * Set Time Fading setting
	 * @param newShowMetadata New Time Fading setting
	 */
	private void setTimeFading( boolean newTimeFading )
	{
		state.timeFading = newTimeFading;
		if(ml.world.getSettings().screenMessagesOn)
		{
			if(newTimeFading)
				ml.display.message(ml, "Time Fading ON");
			else
				ml.display.message(ml, "Time Fading OFF");
		}
	}
	
	/**
	 * Set Show Metadata setting
	 * @param newShowMetadata New Show Metadata setting
	 */
	public void setShowMetadata(boolean newShowMetadata)
	{
		state.showMetadata = newShowMetadata;
		if(getSettings().screenMessagesOn)
		{
			if(newShowMetadata)
				ml.display.message(ml, "Show Metadata ON");
			else
				ml.display.message(ml, "Show Metadata OFF");
		}
	}

	/**
	 * Set Show Model Option
	 * @param newShowModel New Show Model Option setting
	 */
	public void setShowModel(boolean newShowModel)
	{
		if(newShowModel && !state.showModel)
			showModel();
		else if(!newShowModel && state.showModel)
			hideModel();
		
//		state.showModel = newShowModel;
//
//		if(ml.display.window.setupPreferencesWindow)
//			ml.display.window.chkbxShowModel.setSelected(newShowModel);
	}

	public void showModel()
	{
		if(!state.showModel)
		{
			state.showModel = true;

			if(ml.display.window.setupPreferencesWindow)
				ml.display.window.chkbxShowModel.setSelected(true);
			
//			public float modelAlphaInit = 205.f;
//			public float modelAlpha = modelAlphaInit;
//			public boolean fadingModelAlpha = false;		// Global alpha fading 
//			public int fadingModelAlphaStartFrame = 0, fadingModelAlphaEndFrame = 0, fadingModelAlphaLength = 20;	
//			public float fadingModelAlphaStart, fadingModelAlphaTarget;

			transitionModelAlpha(state.modelAlphaMax);
		}
	}

	public void hideModel()
	{
		if(state.showModel)
		{
//			state.showModel = false;

			if(ml.display.window.setupPreferencesWindow)
				ml.display.window.chkbxShowModel.setSelected(false);
			
//			public float modelAlphaInit = 205.f;
//			public float modelAlpha = modelAlphaInit;
//			public boolean fadingModelAlpha = false;		// Global alpha fading 
//			public int fadingModelAlphaStartFrame = 0, fadingModelAlphaEndFrame = 0, fadingModelAlphaLength = 20;	
//			public float fadingModelAlphaStart, fadingModelAlphaTarget;

			transitionModelAlpha(0.f);
		}
	}
	
//	public float modelAlphaInit = 205.f;
//	public float modelAlpha = modelAlphaInit;
//	public boolean fadingModelAlpha = false;		// Global alpha fading 
//	public int fadingModelAlphaStartFrame = 0, fadingModelAlphaEndFrame = 0, fadingModelAlphaLength = 20;	
//	public float fadingModelAlphaStart, fadingModelAlphaTarget;

	/**
	 * Transition Model Alpha to new value
	 * @param newAlpha New Model Alpha value
	 */
	public void transitionModelAlpha(float newAlpha)
	{
		state.fadingModelAlpha = true;		
		state.fadingModelAlphaStartFrame = ml.frameCount;
		state.fadingModelAlphaEndFrame = ml.frameCount + state.fadingModelAlphaLength;
		state.fadingModelAlphaStart = Float.valueOf(ml.world.state.modelAlpha);
		state.fadingModelAlphaTarget = newAlpha;
		
//		public int fadingModelAlphaStartFrame = 0, fadingModelAlphaEndFrame = 0, fadingModelAlphaLength = 20;	
//		public float fadingModelAlphaStart, fadingModelAlphaTarget;

	}

	/**
	 * Load all image and video masks
	 */
	public void loadMasks()
	{
		loadImageMasks();					
		loadVideoMasks();
		state.loadedMasks = true;
	}
	/**
	 * Load image blur masks
	 */
	private void loadImageMasks()
	{
		String maskPath = "/main/resources/masks_image/";
		blurMaskLeftTop = getMaskImageResource(maskPath, "blurMaskLeftTop.jpg");
		blurMaskLeftCenter = getMaskImageResource(maskPath, "blurMaskLeftCenter.jpg");
		blurMaskLeftBottom = getMaskImageResource(maskPath, "blurMaskLeftBottom.jpg");
		blurMaskLeftBoth = getMaskImageResource(maskPath, "blurMaskLeftBoth.jpg");
		blurMaskCenterTop = getMaskImageResource(maskPath, "blurMaskCenterTop.jpg");
		blurMaskCenterCenter = getMaskImageResource(maskPath, "blurMaskCenterCenter.jpg");
		blurMaskCenterBottom = getMaskImageResource(maskPath, "blurMaskCenterBottom.jpg");
		blurMaskCenterBoth = getMaskImageResource(maskPath, "blurMaskCenterBoth.jpg");
		blurMaskRightTop = getMaskImageResource(maskPath, "blurMaskRightTop.jpg");
		blurMaskRightCenter = getMaskImageResource(maskPath, "blurMaskRightCenter.jpg");
		blurMaskRightBottom = getMaskImageResource(maskPath, "blurMaskRightBottom.jpg");
		blurMaskRightBoth = getMaskImageResource(maskPath, "blurMaskRightBoth.jpg");
		blurMaskBothTop = getMaskImageResource(maskPath, "blurMaskBothTop.jpg");
		blurMaskBothCenter = getMaskImageResource(maskPath, "blurMaskBothCenter.jpg");
		blurMaskBothBottom = getMaskImageResource(maskPath, "blurMaskBothBottom.jpg");
		blurMaskBothBoth = getMaskImageResource(maskPath, "blurMaskBothBoth.jpg");
		
		maskPath = "/main/resources/masks_image_vert/";

		vertBlurMaskLeftTop = getMaskImageResource(maskPath, "vertBlurMaskLeftTop.jpg");
		vertBlurMaskLeftCenter = getMaskImageResource(maskPath, "vertBlurMaskLeftCenter.jpg");
		vertBlurMaskLeftBottom = getMaskImageResource(maskPath, "vertBlurMaskLeftBottom.jpg");
		vertBlurMaskLeftBoth = getMaskImageResource(maskPath, "vertBlurMaskLeftBoth.jpg");
		vertBlurMaskCenterTop = getMaskImageResource(maskPath, "vertBlurMaskCenterTop.jpg");
		vertBlurMaskCenterCenter = getMaskImageResource(maskPath, "vertBlurMaskCenterCenter.jpg");
		vertBlurMaskCenterBottom = getMaskImageResource(maskPath, "vertBlurMaskCenterBottom.jpg");
		vertBlurMaskCenterBoth = getMaskImageResource(maskPath, "vertBlurMaskCenterBoth.jpg");
		vertBlurMaskRightTop = getMaskImageResource(maskPath, "vertBlurMaskRightTop.jpg");
		vertBlurMaskRightCenter = getMaskImageResource(maskPath, "vertBlurMaskRightCenter.jpg");
		vertBlurMaskRightBottom = getMaskImageResource(maskPath, "vertBlurMaskRightBottom.jpg");
		vertBlurMaskRightBoth = getMaskImageResource(maskPath, "vertBlurMaskRightBoth.jpg");
		vertBlurMaskBothTop = getMaskImageResource(maskPath, "vertBlurMaskBothTop.jpg");
		vertBlurMaskBothCenter = getMaskImageResource(maskPath, "vertBlurMaskBothCenter.jpg");
		vertBlurMaskBothBottom = getMaskImageResource(maskPath, "vertBlurMaskBothBottom.jpg");
		vertBlurMaskBothBoth = getMaskImageResource(maskPath, "vertBlurMaskBothBoth.jpg");
		
		maskPath = "/main/resources/masks_panorama/";
		blurMaskPanorama = getMaskImageResource(maskPath, "blurMaskPanorama.jpg");
	}
	
	/**
	 * Load video masks
	 */
	private void loadVideoMasks()
	{
		String maskPath = "/main/resources/masks_video/";
		
		videoBlurMaskLeftTop = getMaskImageResource(maskPath, "videoBlurMaskLeftTop.jpg");
		videoBlurMaskLeftCenter = getMaskImageResource(maskPath, "videoBlurMaskLeftCenter.jpg");
		videoBlurMaskLeftBottom = getMaskImageResource(maskPath, "videoBlurMaskLeftBottom.jpg");
		videoBlurMaskLeftBoth = getMaskImageResource(maskPath, "videoBlurMaskLeftBoth.jpg");
		videoBlurMaskCenterTop = getMaskImageResource(maskPath, "videoBlurMaskCenterTop.jpg");
		videoBlurMaskCenterCenter = getMaskImageResource(maskPath, "videoBlurMaskCenterCenter.jpg");
		videoBlurMaskCenterBottom = getMaskImageResource(maskPath, "videoBlurMaskCenterBottom.jpg");
		videoBlurMaskCenterBoth = getMaskImageResource(maskPath, "videoBlurMaskCenterBoth.jpg");
		videoBlurMaskRightTop = getMaskImageResource(maskPath, "videoBlurMaskRightTop.jpg");
		videoBlurMaskRightCenter = getMaskImageResource(maskPath, "videoBlurMaskRightCenter.jpg");
		videoBlurMaskRightBottom = getMaskImageResource(maskPath, "videoBlurMaskRightBottom.jpg");
		videoBlurMaskRightBoth = getMaskImageResource(maskPath, "videoBlurMaskRightBoth.jpg");
		videoBlurMaskBothTop = getMaskImageResource(maskPath, "videoBlurMaskBothTop.jpg");
		videoBlurMaskBothCenter = getMaskImageResource(maskPath, "videoBlurMaskBothCenter.jpg");
		videoBlurMaskBothBottom = getMaskImageResource(maskPath, "videoBlurMaskBothBottom.jpg");
		videoBlurMaskBothBoth = getMaskImageResource(maskPath, "videoBlurMaskBothBoth.jpg");
	}
}
