package multimediaLocator;

import processing.video.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import processing.data.IntList;

/**************************************************
 * @author davidgordon
 * A rectangular video in 3D virtual space
 */

class WMV_Video extends WMV_Viewable          		// Represents a video in virtual space
{
	/* Classes */
	public WMV_VideoState state;

	/* Video */
	Movie video;									// Movie object
	PImage frame;									// Frame to be displayed 
	
	WMV_Video ( int newID, Movie newVideo, int newMediaType, String newName, String newFilePath, PVector newGPSLocation, float newTheta, float newFocalLength, 
			float newOrientation, float newElevation, float newRotation, float newFocusDistance, int newCameraModel, int newVideoWidth, 
			int newVideoHeight, float newBrightness, ZonedDateTime newDateTime, String newTimeZone )
	{
		super(newID, newMediaType, newName, newFilePath, newGPSLocation, newTheta, newCameraModel, newBrightness, newDateTime, newTimeZone);

		state = new WMV_VideoState();
		
		state.vertices = new PVector[4]; 
		state.sVertices = new PVector[4]; 

		state.origVideoWidth = newVideoWidth;
		state.origVideoHeight = newVideoHeight;
		
		state.videoWidth = newVideoWidth;
		state.videoHeight = newVideoHeight;

		if(newFocusDistance == -1.f) state.focusDistance = state.defaultFocusDistance;
		else state.focusDistance = newFocusDistance;

		state.origFocusDistance = state.focusDistance;
		state.focalLength = newFocalLength;

		if(newDateTime != null)
			time = new WMV_Time( newDateTime, getID(), getClusterID(), 2, newTimeZone );		
		else
			time = null;

		state.orientation = newOrientation;       // Vertical (90) or Horizontal (0)
		state.phi = newElevation;            		// Pitch angle
		state.rotation = newRotation;             // Rotation angle
		
//		video = new Movie(p.p.p, filePath);
		video = newVideo;
		setLength( video.duration() );				// Set video length (in seconds)
		video.dispose();
		
	}  

	/**
	 * Display the video in virtual space
	 */
	void draw(WMV_World world)
	{
		if(getViewableState().showMetadata) displayMetadata(world);

		float distanceBrightness = 0.f; 					// Fade with distance
		float angleBrightness;

		float brightness = getFadingBrightness();					
		brightness *= getViewerSettings().userBrightness;

		distanceBrightness = getDistanceBrightness(); 
		brightness *= distanceBrightness; 								// Fade alpha based on distance to camera

		if( getWorldState().timeFading && time != null && !getViewerState().isMoving() )
			brightness *= getTimeBrightness(); 					// Fade brightness based on time

		if(state.isClose && distanceBrightness == 0.f)							// Video recently moved out of range
		{
			state.isClose = false;
			fadeOut();
		}

		if( getViewerSettings().angleFading )
		{
			float videoAngle = getFacingAngle(getViewerState().getOrientationVector());
//			if(p.utilities.isNaN(videoAngle))
//			{
//				videoAngle = 0;				
//				visible = false;
//				disabled = true;
//			}

			angleBrightness = getAngleBrightness(videoAngle);                 // Fade out as turns sideways or gets too far / close
			brightness *= angleBrightness;
		}

		setViewingBrightness( PApplet.map(brightness, 0.f, 1.f, 0.f, 255.f) );				// Scale to setting for alpha range

		if (!getViewableState().hidden && !getViewableState().disabled && !getViewerSettings().map3DMode) 
		{
			if (getViewingBrightness() > 0)
				if ((video.width > 1) && (video.height > 1))
					displayVideo(world);          // Draw the video 
		}
		else
			world.p.noFill();                  // Hide video if it isn't visible

		if(getViewableState().visible && getWorldState().showModel && !getViewableState().hidden && !getViewableState().disabled)
			displayModel(world);
	}

	/**
	 * @param size Size to draw the video center
	 * Draw the video center as a colored sphere
	 */
	void displayModel(WMV_World world)
	{
		/* Draw frame */
		world.p.pushMatrix();

//		world.p.stroke(0.f, 0.f, 255.f, 155.f);	 
		world.p.stroke(0.f, 0.f, 255.f, getViewingBrightness());	 
		world.p.strokeWeight(2.f);
		
		world.p.line(state.vertices[0].x, state.vertices[0].y, state.vertices[0].z, state.vertices[1].x, state.vertices[1].y, state.vertices[1].z);
		world.p.line(state.vertices[1].x, state.vertices[1].y, state.vertices[1].z, state.vertices[2].x, state.vertices[2].y, state.vertices[2].z);
		world.p.line(state.vertices[2].x, state.vertices[2].y, state.vertices[2].z, state.vertices[3].x, state.vertices[3].y, state.vertices[3].z);
		world.p.line(state.vertices[3].x, state.vertices[3].y, state.vertices[3].z, state.vertices[0].x, state.vertices[0].y, state.vertices[0].z);
		
		PVector c = world.getCurrentField().getCluster(getClusterID()).getLocation();
		PVector loc = getLocation();
		PVector cl = getCaptureLocation();
		world.p.popMatrix();
		
		/* Point only */
//		world.p.pushMatrix();
//		world.p.translate(location.x, location.y, location.z);
//
//		world.p.fill(150, 0, 255, 150);
//		world.p.sphere(centerSize);
//		PVector c = world.getCluster(cluster).getLocation();
//		PVector loc = location;
//		PVector cl = getCaptureLocation();
//		world.p.popMatrix();

		world.p.pushMatrix();
		if(getWorldState().showMediaToCluster)
		{
			world.p.strokeWeight(3.f);
			world.p.stroke(150, 135, 255, getViewingBrightness());
			world.p.line(c.x, c.y, c.z, loc.x, loc.y, loc.z);
		}

		if(getWorldState().showCaptureToMedia)
		{
			world.p.strokeWeight(3.f);
			world.p.stroke(160, 100, 255, getViewingBrightness());
			world.p.line(cl.x, cl.y, cl.z, loc.x, loc.y, loc.z);
		}

		if(getWorldState().showCaptureToCluster)
		{
			world.p.strokeWeight(3.f);
			world.p.stroke(120, 55, 255, getViewingBrightness());
			world.p.line(c.x, c.y, c.z, cl.x, cl.y, cl.z);
		}
		world.p.popMatrix();
	}

	/**
	 * Fade in video
	 */
	public void fadeIn()
	{
		if(isFading() || getViewableState().isFadingIn || getViewableState().isFadingOut)		// If already fading, stop at current value
			stopFading();

		fadeBrightness(1.f);					// Fade in
	}

	/**
	 * Fade out video
	 */
	public void fadeOut()
	{
		if(isFading() || getViewableState().isFadingIn || getViewableState().isFadingOut)		// If already fading, stop at current value
			stopFading();

		fadeBrightness(0.f);					// Fade out
	}

	/**
	 * Fade in sound
	 */
	void fadeSoundIn()
	{
		if(state.volume < getWorldSettings().videoMaxVolume)
		{
			state.fadingVolume = true;
			state.volumeFadingStartFrame = getWorldState().frameCount; 
			state.volumeFadingStartVal = state.volume; 
			state.volumeFadingEndFrame = getWorldState().frameCount + state.volumeFadingLength;		// Fade volume over 30 frames
			state.volumeFadingTarget = getWorldSettings().videoMaxVolume;
		}
	}
	
	/**
	 * Fade out sound
	 */
	void fadeSoundOut(boolean pause)
	{
		if(state.volume > 0.f)
		{
			state.fadingVolume = true;
			state.volumeFadingStartFrame = getWorldState().frameCount; 
			state.volumeFadingStartVal = state.volume; 
			state.volumeFadingEndFrame = getWorldState().frameCount + state.volumeFadingLength;		// Fade volume over 30 frames
			state.volumeFadingTarget = 0.f;
			state.pauseAfterSoundFades = pause;
		}
	}

//	public void updateSettings(WMV_WorldSettings newWorldSettings, WMV_ViewerSettings newViewerSettings, ML_DebugSettings newDebugSettings)
//	{
//		worldSettings = newWorldSettings;
//		viewerSettings = newViewerSettings;
//		debugSettings = newDebugSettings;
//	}

	/**
=	 * Update video geometry and visibility 
	 */
	void update(MultimediaLocator ml, WMV_Utilities utilities)
	{
		if(!getViewableState().disabled)			
		{
			boolean wasVisible = getViewableState().visible;
			boolean visibilitySetToTrue = false;
			boolean visibilitySetToFalse = false;
			
			setVisible(false);

			if(getViewerSettings().orientationMode)									// With StaticMode ON, determine visibility based on distance of associated cluster 
			{
				if(getClusterID() == getViewerState().getCurrentClusterID())		// If this photo's cluster is the current (closest) cluster, it is visible
					setVisible(true);

				for(int id : getViewerState().getClustersVisible())
				{
					if(getClusterID() == id)				// If this photo's cluster is on next closest list, it is visible	-- CHANGE THIS??!!
						setVisible(true);
				}
			}
			else 
			{
				if(getViewerSettings().angleFading)
					setVisible( isFacingCamera(getViewerState().getLocation()) );		
				else 
					setVisible(true);     										 		
			}

			if(getViewableState().visible)
			{
				float videoAngle = getFacingAngle(getViewerState().getOrientationVector());				

				if(!utilities.isNaN(videoAngle))
					setVisible(getAngleBrightness(videoAngle) > 0.f);	 // Check if video is visible at current angle facing viewer

				if(!isFading() && getViewerSettings().hideVideos)
					setVisible(false);
					
				if(getViewableState().visible && !getViewerSettings().orientationMode)
					setVisible(getDistanceBrightness() > 0.f);

				if(state.orientation != 0 && state.orientation != 90)          	// Hide orientations of 180 or 270 (avoid upside down images)
					setVisible(false);

				if(isBackFacing(getViewerState().getLocation()) || isBehindCamera(getViewerState().getLocation(), getViewerState().getOrientationVector()))
					setVisible(false);
			}
			
			if(isFading())									// Update brightness while fading
			{
				if(getFadingBrightness() == 0.f)
					setVisible(false);
			}
			else 
			{
				if(!wasVisible && getViewableState().visible)
					visibilitySetToTrue = true;

				if(getFadingBrightness() == 0.f && getViewableState().visible)
					visibilitySetToTrue = true;

				if(wasVisible && !getViewableState().visible)
					visibilitySetToFalse = true;

				if(getFadingBrightness() > 0.f && !getViewableState().visible)
					visibilitySetToFalse = true;
			}
			
			if(!getViewerSettings().angleThinning)										// Check Angle Thinning Mode
			{
				if(visibilitySetToTrue && !isFading() && !hasFadedOut() && !getViewerSettings().hideVideos)	// If should be visible and already fading, fade in 
				{
					if(!state.loaded) loadMedia(ml);
					fadeIn();											// Fade in
				}
			}
			else													// If in Angle Thinning Mode
			{
				if(getViewableState().visible && !state.thinningVisibility && !isFading())
					fadeOut();

				if(!getViewableState().visible && state.thinningVisibility && !isFading() && !hasFadedOut() && !getViewerSettings().hideVideos) 
				{
					if(!state.loaded) loadMedia(ml);
					fadeIn();
				}
			}
			
			if(visibilitySetToFalse)
				fadeOut();

			if(isFading())									// Update brightness while fading
				updateFadingBrightness();

			if(isFadingFocusDistance())
				updateFadingFocusDistance();

			if(hasFadedIn())		// Fade in sound once video has faded in
			{
				if(isPlaying()) fadeSoundIn();
				setFadedIn(false);						
			}

			if(hasFadedOut()) 
			{
//				if(getDebugSettings().video)
//					p.p.p.display.message(p.p, "Will fade sound out for video #"+getID());
				fadeSoundOut(false);			// Fade sound out and clear video once finished
				setFadedOut(false);						
			}
			
			if(state.soundFadedIn) state.soundFadedIn = false;
			if(state.soundFadedOut) state.soundFadedOut = false;
			
			if(state.fadingVolume && state.loaded)
				updateFadingVolume();
		}
	}
	
	/**
	 * Update video geometry each frame
	 */
	public void calculateVertices()									
	{
		state.vertices = initializeVertices();					// Initialize vertices
		state.sVertices = initializeVertices();					// Initialize vertices

		if (state.phi != 0.) state.vertices = rotateVertices(state.vertices, -state.phi, state.verticalAxis);        	 // Rotate around X axis
		if (getTheta() != 0.) state.vertices = rotateVertices(state.vertices, 360-getTheta(), state.azimuthAxis);         // Rotate around Z axis
		if (state.phi != 0.) state.sVertices = rotateVertices(state.sVertices, -state.phi, state.verticalAxis);        	 // Rotate around X axis
		if (getTheta() != 0.) state.sVertices = rotateVertices(state.sVertices, 360-getTheta(), state.azimuthAxis);         // Rotate around Z axis

		if(state.vertices.length == 0) setDisabled(true);
		if(state.sVertices.length == 0) setDisabled(true);
		
//		if(getViewerSettings().orientationMode)	
//			state.vertices = translateVertices(vertices, p.p.viewer.getLocation());
//		else
			state.vertices = translateVertices(state.vertices, getCaptureLocation());                       // Move image to photo capture location   

			state.disp = getDisplacementVector();
		state.vertices = translateVertices(state.vertices, state.disp);          // Translate image vertices from capture to viewing location

		setLocation( new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z) );	// Location in Path Mode
		moveLocation(state.disp);
//		vState.location.add(disp);     													 
	}
	
	public PVector getDisplacementVector()
	{
		float r;				  				 // Viewing sphere radius
		if(state.focusDistance == -1.f)
			r = state.defaultFocusDistance;		 // Use default if no focus distance in metadata					      
		else
			r = state.focusDistance;							

		float xDisp = r * (float)Math.sin((float)Math.toRadians(360-getTheta())) * (float)Math.sin((float)Math.toRadians(90-state.phi)); 
		float zDisp = r * (float)Math.cos((float)Math.toRadians(360-getTheta())) * (float)Math.sin((float)Math.toRadians(90-state.phi));  
		float yDisp = r * (float)Math.cos((float)Math.toRadians(90-state.phi)); 
//		float xDisp = r * PApplet.sin(PApplet.radians(360-theta)) * PApplet.sin(PApplet.radians(90-state.phi)); 
//		float zDisp = r * PApplet.cos(PApplet.radians(360-theta)) * PApplet.sin(PApplet.radians(90-state.phi));  
//		float yDisp = r * PApplet.cos(PApplet.radians(90-state.phi)); 

		return new PVector(-xDisp, -yDisp, -zDisp);			// Displacement from capture location
	}

//	public PVector getLocation()
//	{
////		if(getViewerSettings().orientationMode)
////		{
////			PVector result = new PVector(vState.location.x, vState.location.y, vState.location.z);
////			result.add(getDisplacementVector());
////			return result;
////		}
////		else
//			return getViewableState().location;
//	}

	/**
	 * Load the video file from disk
	 */
	public void loadMedia(MultimediaLocator ml)
	{
		if(!getViewableState().disabled)																	
		{
			video = new Movie(ml, getViewableState().filePath);
			setLength( video.duration() );				// Set video length (in seconds)
			
			video.loop();								// Start loop

			if(getViewerSettings().autoPlayVideos)
			{
				if(ml.world.getCurrentField().getVideosPlaying() < getViewerSettings().autoPlayMaxVideoCount)
					playVideo();
			}
			else
				pauseVideo();
			
			video.volume(0.f);
			state.volume = 0.f;
			
//			if(getDebugSettings().video)
//				ml.display.message(p.p, "Loading video file..."+filePath+" video.duration():"+video.duration());

			calculateVertices(); 
			state.loaded = true;
//			p.videosLoaded++;
//			ml.world.setVideosLoaded(ml.world.getVideosLoaded() + 1);
		}
	}

	/**
	 * Start state.playing the video
	 * @param pause 
	 */
	public void playVideo()
	{
		video.loop();					// Start loop

		state.playing = true;
//		p.setVideosPlaying(p.getVideosPlaying() + 1);
//		p.videosPlaying++;
		video.volume(0.f);
		state.volume = 0.f;
		
		fadeSoundIn();
	}
	
	/**
	 * Stop state.playing the video
	 */
	public void stopVideo()
	{
//		if(getDebugSettings().video) 
//			p.p.p.display.message(p.p, "Stopping video file..."+getID());

//		video.pause();
		fadeSoundOut(true);				// Fade sound out and pause video once finished
		
//		if(video != null)
//			video.stop();
		
//		p.videosPlaying--;
//		p.setVideosPlaying(p.getVideosPlaying() - 1);
		state.playing = false;
	}

	/**
	 * Pause the video
	 */
	public void pauseVideo()
	{
		video.pause();
		state.playing = false;
	}
	
	/**
	 * Stop state.playing and clear the video
	 */
	public void clearVideo()
	{
//		if(getDebugSettings().video) 
//			p.p.p.display.message(p.p, "Stopping and clearing video file..."+getID());

//		if(video.state.playing)
//		video.noLoop();
//		if(video != null)
//			video.stop();

//		p.videosPlaying--;
		
//		p.videosLoaded--;
//		p.setVideosLoaded(p.getVideosLoaded() - 1);

		if(video != null)
		{
			video.stop();
			video.dispose();
		}
		
		state.loaded = false;
//		videoPlaying = false;
	}

	/**
	 * Draw the video in virtual space
	 */
	private void displayVideo(WMV_World world)
	{
		world.p.rectMode(PApplet.CENTER);
		world.p.noStroke(); 

		if(isSelected())
		{
			if (!getViewerSettings().selection && getDebugSettings().field)     // Draw outline
			{
				world.p.stroke(19, 200, 150);
				world.p.strokeWeight(state.outlineSize);
			}
		}

		world.p.pushMatrix();
		world.p.beginShape(PApplet.POLYGON);    // Begin the shape containing the video
		world.p.textureMode(PApplet.IMAGE);

		if(frame != null)
			world.p.texture(frame);

		frame = new PImage(video.getImage());

		if(getViewerSettings().selection)
		{
			if(isSelected())
			{
				if(!getWorldState().alphaMode)
					world.p.tint(getViewingBrightness(), 255);          				
				else
					world.p.tint(255, getViewingBrightness());          				
			}
			else
			{
				if(!getWorldState().alphaMode)
					world.p.tint(getViewingBrightness() * 0.333f, 255);          // Set the image transparency					
				else
					world.p.tint(255, getViewingBrightness() * 0.333f);          				
			}
		}
//		else if(getViewerSettings().videoMode)
//		{
//			if(!world.alphaMode)
//				world.p.tint(viewingBrightness, 255);          				
//			else
//				world.p.tint(255, viewingBrightness);          				
//		}
		else
		{
			if(!getWorldState().alphaMode)
				world.p.tint(getViewingBrightness(), 255);          				
			else
				world.p.tint(255, PApplet.map(getViewingBrightness(), 0.f, 255.f, 0.f, getWorldState().alpha));          				
		}

		if(getViewerSettings().orientationMode)
		{
			world.p.vertex(state.sVertices[0].x, state.sVertices[0].y, state.sVertices[0].z, 0, 0);           // UPPER LEFT      
			world.p.vertex(state.sVertices[1].x, state.sVertices[1].y, state.sVertices[1].z, state.origVideoWidth, 0);           // UPPER RIGHT           
			world.p.vertex(state.sVertices[2].x, state.sVertices[2].y, state.sVertices[2].z, state.origVideoWidth, state.origVideoHeight); 		// LOWER RIGHT        
			world.p.vertex(state.sVertices[3].x, state.sVertices[3].y, state.sVertices[3].z, 0, state.origVideoHeight);           // LOWER LEFT
		}
		else
		{
			world.p.vertex(state.vertices[0].x, state.vertices[0].y, state.vertices[0].z, 0, 0);           // UPPER LEFT      
			world.p.vertex(state.vertices[1].x, state.vertices[1].y, state.vertices[1].z, state.origVideoWidth, 0);           // UPPER RIGHT           
			world.p.vertex(state.vertices[2].x, state.vertices[2].y, state.vertices[2].z, state.origVideoWidth, state.origVideoHeight); 		// LOWER RIGHT        
			world.p.vertex(state.vertices[3].x, state.vertices[3].y, state.vertices[3].z, 0, state.origVideoHeight);           // LOWER LEFT
		}
		world.p.endShape(PApplet.CLOSE);       // End the shape containing the image
		world.p.popMatrix();

//		p.videosSeen++;
//		p.setVideosSeen(p.getVideosSeen() + 1);
	}

	/**
	 * Draw the image metadata in Heads-Up Display
	 */
	public void displayMetadata(WMV_World world)
	{
		String strTitleVideo = "Video";
		String strTitleVideo2 = "-----";
		String strName = "Name: "+getName();
		String strID = "ID: "+String.valueOf(getID());
		String strCluster = "Cluster: "+String.valueOf(getClusterID());
		String strX = "Location X: "+String.valueOf(getCaptureLocation().z);
		String strY = " Y: "+String.valueOf(getCaptureLocation().x);
		String strZ = " Z: "+String.valueOf(getCaptureLocation().y);

		String strDate = "Date: "+String.valueOf(time.getMonth()) + String.valueOf(time.getDay()) + String.valueOf(time.getYear());
		String strTime = "Time: "+String.valueOf(time.getHour()) + ":" + (time.getMinute() >= 10 ? String.valueOf(time.getMinute()) : "0"+String.valueOf(time.getMinute())) + ":" + 
				 (time.getSecond() >= 10 ? String.valueOf(time.getSecond()) : "0"+String.valueOf(time.getSecond()));

		String strLatitude = "GPS Latitude: "+String.valueOf(getGPSLocation().z);
		String strLongitude = " Longitude: "+String.valueOf(getGPSLocation().x);
		String strAltitude = "Altitude: "+String.valueOf(getGPSLocation().y);
		String strTheta = "Direction: "+String.valueOf(getTheta());
		String strElevation = "Vertical Angle: "+String.valueOf(state.phi);
		String strRotation = "Rotation: "+String.valueOf(state.rotation);

		String strTitleDebug = "--- Debugging ---";
		String strBrightness = "brightness: "+String.valueOf(getViewingBrightness());
		String strBrightnessFading = "brightnessFadingValue: "+String.valueOf(getFadingBrightness());
		
		world.p.display.metadata(world, strTitleVideo);
		world.p.display.metadata(world, strTitleVideo2);
		world.p.display.metadata(world, "");

		world.p.display.metadata(world, strID);
		world.p.display.metadata(world, strCluster);
		world.p.display.metadata(world, strName);
		world.p.display.metadata(world, strX + strY + strZ);
		world.p.display.metadata(world, "");

		world.p.display.metadata(world, strDate);
		world.p.display.metadata(world, strTime);
		world.p.display.metadata(world, "");

		world.p.display.metadata(world, strLatitude + strLongitude);
		world.p.display.metadata(world, strAltitude);
		world.p.display.metadata(world, strTheta);
		world.p.display.metadata(world, strElevation);
		world.p.display.metadata(world, strRotation);

		if(getDebugSettings().video)
		{
			world.p.display.metadata(world, strTitleDebug);
			world.p.display.metadata(world, strBrightness);
			world.p.display.metadata(world, strBrightnessFading);
		}	
	}

	/**
	 * @return How far the video is from the camera
	 */
	public float getViewingDistance()       // Find distance from camera to point in virtual space where photo appears           
	{
		PVector camLoc = getViewerState().getLocation();
		PVector loc = new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z);

		float r;

		if(state.focusDistance == -1.f)
			r = state.defaultFocusDistance;						// Use default if no focus distance in metadata					      
		else
			r = state.focusDistance;							

		float xDisp = r * (float)Math.sin((float)Math.toRadians(360-getTheta())) * (float)Math.sin((float)Math.toRadians(90-state.phi)); 
		float zDisp = r * (float)Math.cos((float)Math.toRadians(360-getTheta())) * (float)Math.sin((float)Math.toRadians(90-state.phi));  
		float yDisp = r * (float)Math.cos((float)Math.toRadians(90-state.phi)); 
//		float xDisp = r * PApplet.sin(PApplet.radians(360-theta)) * PApplet.sin(PApplet.radians(90-state.phi)); 
//		float zDisp = r * PApplet.cos(PApplet.radians(360-theta)) * PApplet.sin(PApplet.radians(90-state.phi));  
//		float yDisp = r * PApplet.cos(PApplet.radians(90-state.phi)); 

		state.disp = new PVector(-xDisp, -yDisp, -zDisp);

		loc.add(state.disp);
		float distance = PVector.dist(loc, camLoc);     

		return distance;
	}

	/** 
	 * @return Distance visibility multiplier between 0. and 1.
	 * Find video visibility due to distance (fades away in distance and as camera gets close)
	 */
	public float getDistanceBrightness()								
	{
		float viewDist = getViewingDistance();
		float distVisibility = 1.f;

		float farViewingDistance = getViewerSettings().getFarViewingDistance();
		float nearViewingDistance = getViewerSettings().getNearViewingDistance();
		
		if(viewDist > farViewingDistance)
		{
			float vanishingPoint = farViewingDistance + state.focusDistance;	// Distance where transparency reaches zero
//			float vanishingPoint = farViewingDistance + p.p.state.defaultFocusDistance;	// Distance where transparency reaches zero
			if(viewDist < vanishingPoint)
				distVisibility = PApplet.constrain(1.f - PApplet.map(viewDist, farViewingDistance, vanishingPoint, 0.f, 1.f), 0.f, 1.f);    // Fade out until cam.visibleFarDistance
			else
				distVisibility = 0.f;
		}
		else if(viewDist < nearViewingDistance) 													// Near distance at which transparency reaches zero
		{
			distVisibility = PApplet.constrain(PApplet.map(viewDist, getViewerSettings().getNearClippingDistance(), nearViewingDistance, 0.f, 1.f), 0.f, 1.f);   					  // Fade out until visibleNearDistance
		}

//		if(getDebugSettings().video)
//		{
//			p.p.p.display.message("video #"+getID()+"  distVisibility:"+distVisibility);
//		}
		return distVisibility;
	}

	/**
	 * Check whether video is at an angle where it should currently be visible
	 */
//	public boolean getAngleVisibility()				 // Check if video should be visible
//	{
//		boolean visible = false;
//
//		if(p.p.transitionsOnly)					// With StaticMode ON, determine visibility based on distance of associated cluster 
//		{
//			if(cluster == p.p.viewer.getCurrentCluster())		// If this photo's cluster is the current (closest) cluster, it is visible
//				visible = true;
//
//			for(int id : p.p.viewer.clustersVisible)
//			{
//				if(cluster == id)				// If this photo's cluster is on next closest list, it is visible	-- CHANGE THIS??!!
//					visible = true;
//			}
//
//			return visible;
//		}
//		else 
//		{
//			if(p.p.angleFading)
//			{
//				return isFacingCamera();		
//			}
//			else 
//				return true;     										 		
//		}
//	}
	
	/**
	 * Set thinning visibility of video
	 * @param state New visibility
	 */
	void setThinningVisibility(boolean newState)
	{
		state.thinningVisibility = newState;
	}
	
	/**
	 * Get thinning visibility of image
	 * @param state New visibility
	 */
	public boolean getThinningVisibility()
	{
		return state.thinningVisibility;
	}

	/**
	 * getAngleToCamera()
	 * @return Angle between camera location and image 
	 */	
	public float getAngleToCamera()
	{
		PVector cameraPosition = getViewerState().getLocation();
		PVector centerVertex = calcCenterVertex();

		PVector cameraToFace = new PVector(  cameraPosition.x-centerVertex.x, 	//  Vector from the camera to the face.      
				cameraPosition.y-centerVertex.y, 
				cameraPosition.z-centerVertex.z   );

		PVector ab = new PVector(  state.vertices[1].x-state.vertices[0].x, 
				state.vertices[1].y-state.vertices[0].y, 
				state.vertices[1].z-state.vertices[0].z);
		PVector cb = new PVector(  state.vertices[1].x-state.vertices[2].x, 
				state.vertices[1].y-state.vertices[2].y, 
				state.vertices[1].z-state.vertices[2].z   );

		PVector faceNormal = new PVector();   
		PVector.cross(cb, ab, faceNormal);            						// Cross product of two sides of the face gives face normal (which direction the face is pointing)

		faceNormal.normalize();
		cameraToFace.normalize(); 

		return PVector.dot(faceNormal, cameraToFace);     					// Dot product gives the angle between the two vectors
	}

	/**
	 * @param newLength New video length
	 */
	void setLength(float newLength)
	{
		state.length = newLength;
	}

	/**
	 * @return Video length
	 */
	 public float getLength()
	 {
		 return state.length;
	 }
	 
	/**
	 * @return Whether video is facing the camera
	 */	
	public boolean isFacingCamera(PVector cameraPosition)
	{
		return PApplet.abs(getAngleToCamera()) > getViewerSettings().visibleAngle;     			// If the result is positive, then it is facing the camera.
//		return PApplet.abs(getAngleToCamera()) > p.p.defaultVisibleAngle;     			// If the result is positive, then it is facing the camera.
	}

	/**
	 * @return Is the camera behind the video?  
	 */
	public boolean isBackFacing(PVector cameraPosition)										
	{
//		PVector cameraPosition = p.p.viewer.getLocation();

		float captureToCam = getCaptureLocation().dist(cameraPosition);  	// Find distance from capture location to camera
		float camToVideo = getLocation().dist(cameraPosition);  		// Find distance from camera to image

//		if(captureToCam > camToVideo + p.p.viewer.getNearClippingDistance())			// If captureToCam > camToVideo, then back of video is facing the camera
		if(captureToCam > camToVideo + getViewerSettings().getNearClippingDistance() / 2.f)			// If captureToCam > camToVideo, then back of video is facing the camera
			return true;
		else
			return false; 
	}

	/**
	 * @return Angle between the video and direction the camera is facing
	 */
	public float getFacingAngle(PVector camOrientation)
	{
//		PVector camOrientation = p.p.viewer.getOrientationVector();
		PVector faceNormal = getFaceNormal();

		PVector crossVector = new PVector();
		PVector.cross(camOrientation, faceNormal, crossVector);				// Cross vector gives angle between camera and image

		float result = crossVector.mag();
		return result;
	}

	/**
	 * @return Whether video is behind camera
	 */
	boolean isBehindCamera(PVector camLocation, PVector camOrientation)										
	{
//		PVector camLocation = p.p.viewer.getLocation();
//		PVector camOrientation = p.p.viewer.getOrientationVector();
		PVector centerVertex = calcCenterVertex();

		PVector camToVideo = new PVector(  camLocation.x-centerVertex.x, 	//  Vector from the camera to the face.      
				camLocation.y-centerVertex.y, 
				camLocation.z-centerVertex.z   );

		camToVideo.normalize();

		float result = PVector.dot(camOrientation, camToVideo);				// Dot product gives angle between camera and image

		if(result >= 0)							// If > zero, image is behind camera
			return true;
		else
			return false; 						// If < zero, image is in front of camera
	}

	/**
	 * @return Center vertex of video rectangle
	 */
	public PVector calcCenterVertex()
	{
		PVector vertex1 = new PVector(0,0,0);
		PVector vertex2 = new PVector(0,0,0);
		PVector diff = new PVector(0,0,0);
		PVector result = new PVector(0,0,0);

		vertex1 = state.vertices[2];
		vertex2 = state.vertices[0];

		diff = PVector.sub(vertex1, vertex2);
		diff.mult(0.5f);
		result = PVector.add(vertex2, diff);

		return result;
	}

	/**
	 * getFaceNormal()
	 * @return Normalized vector perpendicular to the image plane
	 */
	public PVector getFaceNormal()
	{
		PVector vertex1, vertex2, vertex3;
		vertex1 = new PVector(0,0,0);
		vertex2 = new PVector(0,0,0);
		vertex3 = new PVector(0,0,0);

		if(getCameraModel() == 1)
		{
			if (state.orientation == 90)  // Vertical Image
			{
				vertex1 = state.vertices[3];
				vertex2 = state.vertices[0];
				vertex3 = state.vertices[1];
			}
			else if (state.orientation == 0)    // Horizontal Image
			{
				vertex1 = state.vertices[0];
				vertex2 = state.vertices[1];
				vertex3 = state.vertices[2];
			}
			else if (state.orientation == 180)    // Upside Down (Horizontal) Image
			{
				vertex1 = state.vertices[2];
				vertex2 = state.vertices[3];
				vertex3 = state.vertices[0];
			}
			else  if (state.orientation == 270)    // Upside Down (Vertical) Image
			{
				vertex1 = state.vertices[1];
				vertex2 = state.vertices[2];
				vertex3 = state.vertices[3];
			}
		}
		else
		{
			vertex1 = state.vertices[0];
			vertex2 = state.vertices[1];
			vertex3 = state.vertices[2];
		}

		PVector ab = new PVector( vertex2.x-vertex1.x, 
				vertex2.y-vertex1.y, 
				vertex2.z-vertex1.z  );
		PVector cb = new PVector( vertex2.x-vertex3.x, 
				vertex2.y-vertex3.y, 
				vertex2.z-vertex3.z  );

		PVector faceNormal = new PVector();
		PVector.cross(cb, ab, faceNormal);

		faceNormal.normalize(); 
		return faceNormal;
	}
	
	/**
	 * Find image taken immediately before this video was captured to serve as placeholder, determining elevation and state.rotation angles
	 */
	public void findPlaceholder(ArrayList<WMV_Image> images)
	{
		IntList candidates = new IntList();							// List of placeholder candidates
		
		for (int i = 0; i < images.size(); i++) 					// -- Should limit this to only cluster!!
		{
			if(time.getDate().equals(images.get(i).time.getDate()))				// Placeholder will be from same date
			{
				PVector imgLocation = images.get(i).getCaptureLocation();
				float curDist = PVector.dist(getCaptureLocation(), imgLocation);

				if(curDist < state.assocVideoDistTolerance)		// and very close in space,
					candidates.append(i);												// Add to candidates list
			}
		}
		
		int closestIdx = -1;
		float closestDist = 10000.f;
		
		if(candidates.size() == 0)
		{
			System.out.println("  Video "+getID()+" has no candidates under distance tolerance:"+state.assocVideoDistTolerance+"!");
		}
		
		for( int i : candidates )							// Compare distances of the candidates
		{
			float timeDiff = time.getTime() - images.get(i).time.getTime();

			if( timeDiff > 0.f && timeDiff < state.assocVideoTimeTolerance )			// If in very close succession with an image
			{
				if(timeDiff < closestDist)
				{
					closestDist = timeDiff;
					closestIdx = i;
				}
			}
		}
		
		if(closestIdx != -1)
		{
			System.out.println("--> Found image placeholder:"+images.get(closestIdx).getName()+"  for video:"+getName()+" placeholder ID:"+images.get(closestIdx).getID()+" closestIdx:"+closestIdx);
			boolean success = associateImagePlaceholder(images.get(closestIdx), closestDist, PApplet.abs(time.getTime() - images.get(closestIdx).time.getTime()));
			
			if(success)
			{
				System.out.println("---> Set placeholder image id:"+images.get(closestIdx).getID());
				images.get(closestIdx).associateVideo(getID());
			}
		}
		
		if(!state.hasImagePlaceholder)
		{
			System.out.println("No image placeholder found for video:"+getID()+", will set to disabled...");
			setDisabled(true);
			setHidden(true);
//			p.numVideos--;
		}
	}

	/**
	 * Associate image with given ID with this video
	 * @param imageID 
	 * @param newImageDist 
	 * @param newImageTimeDiff 
	 * @return Whether successful or not 
	 */
	public boolean associateImagePlaceholder(WMV_Image i, float newImageDist, float newImageTimeDiff)
	{
		boolean success = false;
		
		if(!state.hasImagePlaceholder)
			success = true;

		if(success)
		{
			state.hasImagePlaceholder = true;
			state.imagePlaceholder = i.getID();
			
			/* Set video parameters from image placeholder metadata */
//			setCameraModel( i.getCameraModel() );
			state.focusDistance = i.getFocusDistance();		    
			state.focalLength = i.getFocalLength();
			state.orientation = i.getOrientation();       
			setTheta(i.getDirection());  
			state.phi = i.getVerticalAngle();            		
			state.rotation = i.getRotation();             
			state.sensorSize = i.getSensorSize();
			
			setAspectRatio( calculateAspectRatio() );
			state.videoWidth = i.getWidth();									
			state.videoHeight = (int) (i.getWidth() * getAspectRatio());	
			
			calculateVertices();
		}
		
		return success;
	}

	/**
	 * Update volume fading 
	 */
	private void updateFadingVolume()
	{
		if(state.fadingVolume && getWorldState().frameCount < state.volumeFadingEndFrame)	// Still fading
		{
			state.volume = PApplet.map(getWorldState().frameCount, state.volumeFadingStartFrame, state.volumeFadingEndFrame, state.volumeFadingStartVal, state.volumeFadingTarget);
			video.volume(state.volume);
		}
		else								// Reached target
		{
			state.volume = state.volumeFadingTarget;
			state.fadingVolume = false;
			if(state.volume == 1.f)
				state.soundFadedIn = true;
			else if(state.volume == 0.f)
			{
				state.soundFadedOut = true;
			
				if(state.pauseAfterSoundFades)
					video.pause();
				else
					clearVideo();
			}
		}
	}

	/**
	 * @return Aspect ratio of the video
	 */
	float calculateAspectRatio()
	{
		float ratio = 0;

//		ratio = (float)(video.height)/(float)(video.width);
		ratio = (float) state.origVideoHeight / (float) state.origVideoWidth;

		return ratio;
	}

	/**
	 * @return Whether the vertices are null
	 */
	public boolean verticesAreNull()
	{
		if(state.vertices[0] != null && state.vertices[1] != null && state.vertices[2] != null && state.vertices[3] != null)
			return false;
		else
			return true;
	}

	/**
	 * @return Average pixel color for this frame
	 */
	PVector getAverageColor() 
	{
		video.loadPixels();
		int r = 0, g = 0, b = 0;
		for (int i=0; i<video.pixels.length; i++) {
			int c = video.pixels[i];
			r += c>>16&0xFF;
		g += c>>8&0xFF;
		b += c&0xFF;
		}
		r /= video.pixels.length;
		g /= video.pixels.length;
		b /= video.pixels.length;
		
		return new PVector(r, g, b);
	}

	/**
	 * Calculate and return alpha value given camera distance and image angle
	 * @param videoAngle Angle between video and viewer
	 */
	private float getAngleBrightness(float videoAngle)
	{
//		float angleFadeAmt = 0.f;

		float angleBrightness = 0.f;

		if(videoAngle > getViewerSettings().visibleAngle)
			angleBrightness = 0.f;
		else if (videoAngle < getViewerSettings().visibleAngle * 0.66f)
			angleBrightness = 1.f;
		else
			angleBrightness = PApplet.constrain((1.f-PApplet.map(videoAngle, getViewerSettings().visibleAngle * 0.66f, getViewerSettings().visibleAngle, 0.f, 1.f)), 0.f, 1.f);

		return angleBrightness;
	}
	
	/**
	 * Fade focus distance to given target while rescaling images 
	 * @param target New focus distance
	 */
	public void fadeFocusDistance(float target)
	{
		setFadingFocusDistance(true);
		state.fadingFocusDistanceStartFrame = getWorldState().frameCount;					
		state.fadingFocusDistanceEndFrame = getWorldState().frameCount + state.fadingFocusDistanceLength;	
		state.fadingFocusDistanceStart = state.focusDistance;
		state.fadingFocusDistanceTarget = target;
	}
	
	/**
	 * Update fading of object distance (focus distance and image size together)
	 */
	private void updateFadingFocusDistance()
	{
		float newFocusDistance = 0.f;

		if (getWorldState().frameCount >= state.fadingFocusDistanceEndFrame)
		{
			setFadingFocusDistance(false);
			newFocusDistance = state.fadingFocusDistanceTarget;
		} 
		else
		{
			newFocusDistance = PApplet.map( getWorldState().frameCount, state.fadingFocusDistanceStartFrame, state.fadingFocusDistanceEndFrame, 
											state.fadingFocusDistanceStart, state.fadingFocusDistanceTarget);      // Fade with distance from current time
		}

		setFocusDistance( newFocusDistance );	// Set focus distance
		calculateVertices();  					// Update vertices given new width
	}

	
	void resetFocusDistance()
	{
		setFocusDistance(state.origFocusDistance);
	}

	/**	
	 * Setup video rectangle geometry 
	 */
	private PVector[] initializeVertices()
	{
		float width = getVideoWidthMeters();
		float height = getVideoWidthMeters() * getAspectRatio();

		float left = -width * 0.5f;
		float right = width * 0.5f;
		float top = -height * 0.5f;
		float bottom = height * 0.5f;
		
		PVector[] verts = new PVector[4]; 

		verts[0] = new PVector( left, top, 0 );    	  // UPPER LEFT  
		verts[1] = new PVector( right, top, 0 );      // UPPER RIGHT 
		verts[2] = new PVector( right, bottom, 0 );   // LOWER RIGHT
		verts[3] = new PVector( left, bottom, 0 );    // LOWER LEFT
		
		return verts;
	}
	
	private float getVideoWidthMeters()
	{
		// 	Image Size = Sensor Width * Focus Distance / Focal Length 
		float result = state.sensorSize * state.subjectSizeRatio * state.focusDistance / state.focalLength;
		return result;
	}

	/**
	 * Search given list of clusters and associated with this image
	 * @return Whether associated field was successfully found
	 */	
	public boolean findAssociatedCluster(ArrayList<WMV_Cluster> clusterList, float maxClusterDistance)    				 // Associate cluster that is closest to photo
	{
		int closestClusterIndex = 0;
		float closestDistance = 100000;

		for (int i = 0; i < clusterList.size(); i++) 
		{     
			WMV_Cluster curCluster = clusterList.get(i);
			float distanceCheck = getCaptureLocation().dist(curCluster.getLocation());

			if (distanceCheck < closestDistance)
			{
				closestClusterIndex = i;
				closestDistance = distanceCheck;
			}
		}

		if(closestDistance < maxClusterDistance)
			setClusterID(closestClusterIndex);		// Associate image with cluster
		else
			setClusterID(-1);						// Create a new single image cluster here!

		if(getClusterID() != -1)
			return true;
		else
			return false;
	}

	public WMV_VideoState getState()
	{
		return state;
	}
	
	 public void captureState()
	 {
		 state.setViewableState(vState);
	 }
	 
	/**
	 * @return Image placeholder for this video
	 */
	public int getImagePlaceholder()
	{
		return state.imagePlaceholder;
	}
	
	 public float getDirection()
	 {
		 return getTheta();
	 }

	 public float getVerticalAngle()
	 {
		 return state.phi;
	 }
	 
	 public float getRotation()
	 {
		 return state.rotation;
	 }
	 
	 public float getWidth()
	 {
		 return state.videoWidth;
	 }

	 public float getHeight()
	 {
		 return state.videoHeight;
	 }
	 
	 public float getFocusDistance()
	 {
		 return state.focusDistance;
	 }

	 public float getFocalLength()
	 {
		 return state.focalLength;
	 }
	 
	 public void setSensorSize(float newSensorSize)
	 {
		 state.sensorSize = newSensorSize;
	 }

	 public float getSensorSize()
	 {
		 return state.sensorSize;
	 }
	 
	 public boolean isFadingVolume()
	 {
		 return state.fadingVolume;
	 }
	 
	 public boolean isLoaded()
	 {
		 return state.loaded;
	 }
	 
	 public boolean isPlaying()
	 {
		 return state.playing;
	 }

	 public void setFocusDistance(float newFocusDistance)
	 {
		 state.focusDistance = newFocusDistance;
	 }

	 public void setFocalLength(float newFocalLength)
	 {
		 state.focalLength = newFocalLength;
	 }
}