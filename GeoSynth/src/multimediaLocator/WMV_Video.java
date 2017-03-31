package multimediaLocator;

import processing.video.*;

import java.time.ZonedDateTime;
//import java.awt.Image;
import java.util.ArrayList;
//import java.util.Calendar;

import processing.core.*;
import processing.data.IntList;

/**************************************************
 * @author davidgordon
 * A rectangular video in 3D virtual space
 */

class WMV_Video extends WMV_Viewable          		// Represents a video in virtual space
{
//	/* Classes */
//	WMV_WorldSettings worldSettings;
//	WMV_ViewerSettings viewerSettings;	// Update world settings
//	ML_DebugSettings debugSettings;	// Update world settings

	/* Video */
	Movie video;									// Movie object
	PImage frame;									// Frame to be displayed 
	
	private boolean loaded = false;
	private boolean playing = false;
	private boolean soundFadedIn = false, soundFadedOut = false;
	
	/* Metadata */
	int origVideoWidth = 0, origVideoHeight = 0;
	PVector averageColor;
	float averageBrightness;

	private float orientation;              		// Landscape = 0, Portrait = 90, Upside Down Landscape = 180, Upside Down Portrait = 270
	private float phi, rotation;       				// Elevation angle and Z-axis rotation
	private float focalLength = 0; 					// Zoom Level 
	private float focusDistance; 	 		 		// Video viewing distance
	private float defaultFocusDistance = 9.0f;			// Default focus distance for images and videos (m.)
	private float origFocusDistance; 	 		 		// Original video viewing distance
	private float sensorSize;
	private float subjectSizeRatio = 0.18f;			// Subject portion of video plane (used in scaling from focus distance to imageSize)
	private PVector disp = new PVector(0, 0, 0);    	// Displacement from capture location
	private float length;

	/* Graphics */
	private float videoWidth = 0, videoHeight = 0;			// Video width and height
	PVector[] vertices, sVertices;
	public PVector azimuthAxis = new PVector(0, 1, 0);
	public PVector verticalAxis = new PVector(1, 0, 0);
	public PVector rotationAxis = new PVector(0, 0, 1);
	public float outlineSize = 10.f;
	private final float videoFocusDistanceFactor = 0.8f;		// Scaling from defaultFocusDistance to video focus distance
	
	private float fadingFocusDistanceStartFrame = 0.f, fadingFocusDistanceEndFrame = 0.f;	// Fade focus distance and image size together
	private float fadingFocusDistanceStart = 0.f, fadingFocusDistanceTarget = 0.f;
	private float fadingFocusDistanceLength = 30.f;

	private boolean thinningVisibility = false;
 	
	/* Sound */
	private float volume = 0.f;			// Video volume between 0. and 1.
	private boolean fadingVolume = false;
	private int volumeFadingStartFrame = 0, volumeFadingEndFrame = 0;
	private float volumeFadingStartVal = 0.f, volumeFadingTarget = 0.f;
	private final int volumeFadingLength = 60;	// Fade volume over 30 frames
	private boolean pauseAfterSoundFades = false;
	
	/* Navigation */
	private boolean isClose = false;				// Is the viewer in visible range?
	
	/* Placeholder Image */
	private boolean hasImagePlaceholder = false;
	private int imagePlaceholder = -1;

	WMV_Video ( WMV_Field parent, int newID, int newMediaType, String newName, String newFilePath, PVector newGPSLocation, float newTheta, float newFocalLength, 
			float newOrientation, float newElevation, float newRotation, float newFocusDistance, int newCameraModel, int newVideoWidth, 
			int newVideoHeight, float newBrightness, ZonedDateTime newDateTime )
	{
		super(parent, newID, newMediaType, newName, newFilePath, newGPSLocation, newTheta, newCameraModel, newBrightness, newDateTime);

//		p = parent;
//		name = newName;

		vertices = new PVector[4]; 
		sVertices = new PVector[4]; 

		filePath = newFilePath;

		origVideoWidth = newVideoWidth;
		origVideoHeight = newVideoHeight;
		
		videoWidth = newVideoWidth;
		videoHeight = newVideoHeight;
		aspectRatio = getAspectRatio();							
		gpsLocation = newGPSLocation;
		brightness = newBrightness;

		if(newFocusDistance == -1.f) focusDistance = defaultFocusDistance;
		else focusDistance = newFocusDistance;

		origFocusDistance = focusDistance;
		focalLength = newFocalLength;
		cameraModel = newCameraModel;

		if(newDateTime != null)
			time = new WMV_Time( newDateTime, getID(), cluster, 2, p.getTimeZoneID() );		
		else
			time = null;

		theta = newTheta;              		// GPS Orientation (Yaw angle)
		orientation = newOrientation;       // Vertical (90) or Horizontal (0)
		phi = newElevation;            		// Pitch angle
		rotation = newRotation;             // Rotation angle
		
		video = new Movie(p.p.p, filePath);
		setLength( video.duration() );				// Set video length (in seconds)
		video.dispose();
		
	}  

	/**
	 * Display the video in virtual space
	 */
	void draw(WMV_World world)
	{
		float distanceBrightness = 0.f; 					// Fade with distance
		float angleBrightness;

		float brightness = fadingBrightness;					
		brightness *= viewerSettings.userBrightness;

		distanceBrightness = getDistanceBrightness(); 
		brightness *= distanceBrightness; 								// Fade alpha based on distance to camera

		if( worldState.timeFading && time != null && !world.viewer.isMoving() )
			brightness *= getTimeBrightness(); 					// Fade brightness based on time

		if(isClose && distanceBrightness == 0.f)							// Video recently moved out of range
		{
			isClose = false;
			fadeOut();
		}

		if(p.debugSettings.video && p.debugSettings.detailed && p.frameCount % 30 == 0)
			world.p.display.message(world, "Video brightness after distance:"+brightness);

		if( viewerSettings.angleFading )
		{
			float videoAngle = getFacingAngle(world.viewer.getOrientationVector());
			if(p.utilities.isNaN(videoAngle))
			{
				videoAngle = 0;				
				visible = false;
				disabled = true;
			}

			angleBrightness = getAngleBrightness(videoAngle);                 // Fade out as turns sideways or gets too far / close
			brightness *= angleBrightness;
		}

		viewingBrightness = PApplet.map(brightness, 0.f, 1.f, 0.f, 255.f);				// Scale to setting for alpha range

		if (!hidden && !disabled && !viewerSettings.map3DMode) 
		{
			if (viewingBrightness > 0)
				if ((video.width > 1) && (video.height > 1))
					displayVideo(world);          // Draw the video 
		}
		else
		{      
			world.p.noFill();                  // Hide video if it isn't visible
		}

		if(visible && worldState.showModel && !hidden && !disabled)
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
		world.p.stroke(0.f, 0.f, 255.f, viewingBrightness);	 
		world.p.strokeWeight(2.f);
		
		world.p.line(vertices[0].x, vertices[0].y, vertices[0].z, vertices[1].x, vertices[1].y, vertices[1].z);
		world.p.line(vertices[1].x, vertices[1].y, vertices[1].z, vertices[2].x, vertices[2].y, vertices[2].z);
		world.p.line(vertices[2].x, vertices[2].y, vertices[2].z, vertices[3].x, vertices[3].y, vertices[3].z);
		world.p.line(vertices[3].x, vertices[3].y, vertices[3].z, vertices[0].x, vertices[0].y, vertices[0].z);
		
		PVector c = world.getCurrentField().getCluster(cluster).getLocation();
		PVector loc = location;
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
		if(worldState.showMediaToCluster)
		{
			world.p.strokeWeight(3.f);
			world.p.stroke(150, 135, 255, viewingBrightness);
			world.p.line(c.x, c.y, c.z, loc.x, loc.y, loc.z);
		}

		if(worldState.showCaptureToMedia)
		{
			world.p.strokeWeight(3.f);
			world.p.stroke(160, 100, 255, viewingBrightness);
			world.p.line(cl.x, cl.y, cl.z, loc.x, loc.y, loc.z);
		}

		if(worldState.showCaptureToCluster)
		{
			world.p.strokeWeight(3.f);
			world.p.stroke(120, 55, 255, viewingBrightness);
			world.p.line(c.x, c.y, c.z, cl.x, cl.y, cl.z);
		}
		world.p.popMatrix();
	}

	/**
	 * Fade in video
	 */
	public void fadeIn()
	{
		if(fading || isFadingIn || isFadingOut)		// If already fading, stop at current value
			stopFading();

		fadeBrightness(1.f);					// Fade in
	}

	/**
	 * Fade out video
	 */
	public void fadeOut()
	{
		if(fading || isFadingIn || isFadingOut)		// If already fading, stop at current value
			stopFading();

		fadeBrightness(0.f);					// Fade out
	}

	/**
	 * Fade in sound
	 */
	void fadeSoundIn()
	{
		if(volume < worldSettings.videoMaxVolume)
		{
			fadingVolume = true;
			volumeFadingStartFrame = p.frameCount; 
			volumeFadingStartVal = volume; 
			volumeFadingEndFrame = p.frameCount + volumeFadingLength;		// Fade volume over 30 frames
			volumeFadingTarget = worldSettings.videoMaxVolume;
		}
	}
	
	/**
	 * Fade out sound
	 */
	void fadeSoundOut(boolean pause)
	{
		if(volume > 0.f)
		{
			fadingVolume = true;
			volumeFadingStartFrame = p.frameCount; 
			volumeFadingStartVal = volume; 
			volumeFadingEndFrame = p.frameCount + volumeFadingLength;		// Fade volume over 30 frames
			volumeFadingTarget = 0.f;
			pauseAfterSoundFades = pause;
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
	void update()
	{
		if(!disabled)			
		{
			boolean wasVisible = visible;
			boolean visibilitySetToTrue = false;
			boolean visibilitySetToFalse = false;
			
			visible = false;

			if(viewerSettings.orientationMode)									// With StaticMode ON, determine visibility based on distance of associated cluster 
			{
				if(cluster == viewerState.getCurrentClusterID())		// If this photo's cluster is the current (closest) cluster, it is visible
					visible = true;

				for(int id : p.p.viewer.getClustersVisible())
				{
					if(cluster == id)				// If this photo's cluster is on next closest list, it is visible	-- CHANGE THIS??!!
						visible = true;
				}
			}
			else 
			{
				if(viewerSettings.angleFading)
					visible = isFacingCamera(viewerState.getLocation());		
				else 
					visible = true;     										 		
			}

			if(visible)
			{
				float videoAngle = getFacingAngle(p.p.viewer.getOrientationVector());				

				if(!p.utilities.isNaN(videoAngle))
					visible = (getAngleBrightness(videoAngle) > 0.f);	 // Check if video is visible at current angle facing viewer

				if(!fading && viewerSettings.hideVideos)
					visible = false;
					
				if(visible && !viewerSettings.orientationMode)
					visible = (getDistanceBrightness() > 0.f);

				if(orientation != 0 && orientation != 90)          	// Hide orientations of 180 or 270 (avoid upside down images)
					visible = false;

				if(isBackFacing(viewerState.getLocation()) || isBehindCamera(viewerState.getLocation(), p.p.viewer.getOrientationVector()))
					visible = false;
			}
			
			if(isFading())									// Update brightness while fading
			{
				if(fadingBrightness == 0.f)
					visible = false;
			}
			else 
			{
				if(!wasVisible && visible)
					visibilitySetToTrue = true;

				if(fadingBrightness == 0.f && visible)
					visibilitySetToTrue = true;

				if(wasVisible && !visible)
					visibilitySetToFalse = true;

				if(fadingBrightness > 0.f && !visible)
					visibilitySetToFalse = true;
			}
			
			if(!viewerSettings.angleThinning)										// Check Angle Thinning Mode
			{
				if(visibilitySetToTrue && !fading && !fadedOut && !viewerSettings.hideVideos)	// If should be visible and already fading, fade in 
				{
					if(!loaded) loadMedia(p.p.p);
					fadeIn();											// Fade in
				}
			}
			else													// If in Angle Thinning Mode
			{
				if(visible && !thinningVisibility && !fading)
					fadeOut();

				if(!visible && thinningVisibility && !fading && !fadedOut && !viewerSettings.hideVideos) 
				{
					if(!loaded) loadMedia(p.p.p);
					fadeIn();
				}
			}
			
			if(visibilitySetToFalse)
				fadeOut();

			if(isFading())									// Update brightness while fading
				updateFadingBrightness();

			if(fadingFocusDistance)
				updateFadingFocusDistance();

			if(fadedIn)		// Fade in sound once video has faded in
			{
				if(isPlaying()) fadeSoundIn();
				fadedIn = false;						
			}

			if(fadedOut) 
			{
				if(p.debugSettings.video)
					p.p.p.display.message(p.p, "Will fade sound out for video #"+getID());
				fadeSoundOut(false);			// Fade sound out and clear video once finished
				fadedOut = false;						
			}
			
			if(soundFadedIn) soundFadedIn = false;
			if(soundFadedOut) soundFadedOut = false;
			
			if(fadingVolume && loaded)
				updateFadingVolume();
		}
	}
	
	/**
	 * Update video geometry each frame
	 */
	public void calculateVertices()									
	{
		vertices = initializeVertices();					// Initialize vertices
		sVertices = initializeVertices();					// Initialize vertices

		if (phi != 0.) vertices = rotateVertices(vertices, -phi, verticalAxis);        	 // Rotate around X axis
		if (theta != 0.) vertices = rotateVertices(vertices, 360-theta, azimuthAxis);         // Rotate around Z axis
		if (phi != 0.) sVertices = rotateVertices(sVertices, -phi, verticalAxis);        	 // Rotate around X axis
		if (theta != 0.) sVertices = rotateVertices(sVertices, 360-theta, azimuthAxis);         // Rotate around Z axis

		if(vertices.length == 0) disabled = true;
		if(sVertices.length == 0) disabled = true;
		
//		if(viewerSettings.orientationMode)	
//			vertices = translateVertices(vertices, p.p.viewer.getLocation());
//		else
			vertices = translateVertices(vertices, getCaptureLocation());                       // Move image to photo capture location   

		disp = getDisplacementVector();
		vertices = translateVertices(vertices, disp);          // Translate image vertices from capture to viewing location

//		if(viewerSettings.orientationMode)
//			location = p.p.viewer.getLocation();
//		else
			location = new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z);	// Location in Path Mode

		location.add(disp);     													 

		if (p.utilities.isNaN(location.x) || p.utilities.isNaN(location.x) || p.utilities.isNaN(location.x))
			location = new PVector (0, 0, 0);
	}
	
	public PVector getDisplacementVector()
	{
		float r;				  				 // Viewing sphere radius
		if(focusDistance == -1.f)
			r = defaultFocusDistance;		 // Use default if no focus distance in metadata					      
		else
			r = focusDistance;							

		float xDisp = r * PApplet.sin(PApplet.radians(360-theta)) * PApplet.sin(PApplet.radians(90-phi)); 
		float zDisp = r * PApplet.cos(PApplet.radians(360-theta)) * PApplet.sin(PApplet.radians(90-phi));  
		float yDisp = r * PApplet.cos(PApplet.radians(90-phi)); 

		return new PVector(-xDisp, -yDisp, -zDisp);			// Displacement from capture location
	}

	public PVector getLocation()
	{
		if(viewerSettings.orientationMode)
		{
			PVector result = new PVector(location.x, location.y, location.z);
			result.add(getDisplacementVector());
			return result;
		}
		else
			return location;
	}

	/**
	 * Load the video file from disk
	 */
	public void loadMedia(MultimediaLocator ml)
	{
		if(!disabled)																	
		{
			video = new Movie(ml, filePath);
			setLength( video.duration() );				// Set video length (in seconds)
			
			video.loop();								// Start loop

			if(viewerSettings.autoPlayVideos)
			{
				if(p.getVideosPlaying() < viewerSettings.autoPlayMaxVideoCount)
					playVideo();
			}
			else
				pauseVideo();
			
			video.volume(0.f);
			volume = 0.f;
			
			if(p.debugSettings.video)
				ml.display.message(p.p, "Loading video file..."+filePath+" video.duration():"+video.duration());

			calculateVertices(); 
			loaded = true;
//			p.videosLoaded++;
			p.setVideosLoaded(p.getVideosLoaded() + 1);
		}
	}

	/**
	 * Start playing the video
	 * @param pause 
	 */
	public void playVideo()
	{
		video.loop();					// Start loop

		playing = true;
		p.setVideosPlaying(p.getVideosPlaying() + 1);
//		p.videosPlaying++;
		video.volume(0.f);
		volume = 0.f;
		
		fadeSoundIn();
	}
	
	/**
	 * Stop playing the video
	 */
	public void stopVideo()
	{
		if(p.debugSettings.video) 
			p.p.p.display.message(p.p, "Stopping video file..."+getID());

//		video.pause();
		fadeSoundOut(true);				// Fade sound out and pause video once finished
		
//		if(video != null)
//			video.stop();
		
//		p.videosPlaying--;
		p.setVideosPlaying(p.getVideosPlaying() - 1);
		playing = false;
	}

	/**
	 * Pause the video
	 */
	public void pauseVideo()
	{
		video.pause();
		playing = false;
	}
	
	/**
	 * Stop playing and clear the video
	 */
	public void clearVideo()
	{
		if(p.debugSettings.video) 
			p.p.p.display.message(p.p, "Stopping and clearing video file..."+getID());

//		if(video.playing)
//		video.noLoop();
//		if(video != null)
//			video.stop();

//		p.videosPlaying--;
		
//		p.videosLoaded--;
		p.setVideosLoaded(p.getVideosLoaded() - 1);

		if(video != null)
		{
			video.stop();
			video.dispose();
		}
		
		loaded = false;
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
			if (!viewerSettings.selection && p.debugSettings.field)     // Draw outline
			{
				world.p.stroke(19, 200, 150);
				world.p.strokeWeight(outlineSize);
			}
		}

		world.p.pushMatrix();
		world.p.beginShape(PApplet.POLYGON);    // Begin the shape containing the video
		world.p.textureMode(PApplet.IMAGE);

		if(frame != null)
			world.p.texture(frame);

		frame = new PImage(video.getImage());

		if(viewerSettings.selection)
		{
			if(isSelected())
			{
				if(!worldState.alphaMode)
					world.p.tint(viewingBrightness, 255);          				
				else
					world.p.tint(255, viewingBrightness);          				
			}
			else
			{
				if(!worldState.alphaMode)
					world.p.tint(viewingBrightness * 0.333f, 255);          // Set the image transparency					
				else
					world.p.tint(255, viewingBrightness * 0.333f);          				
			}
		}
//		else if(viewerSettings.videoMode)
//		{
//			if(!world.alphaMode)
//				world.p.tint(viewingBrightness, 255);          				
//			else
//				world.p.tint(255, viewingBrightness);          				
//		}
		else
		{
			if(!worldState.alphaMode)
				world.p.tint(viewingBrightness, 255);          				
			else
				world.p.tint(255, PApplet.map(viewingBrightness, 0.f, 255.f, 0.f, worldState.alpha));          				
		}

		if(viewerSettings.orientationMode)
		{
			world.p.vertex(sVertices[0].x, sVertices[0].y, sVertices[0].z, 0, 0);           // UPPER LEFT      
			world.p.vertex(sVertices[1].x, sVertices[1].y, sVertices[1].z, origVideoWidth, 0);           // UPPER RIGHT           
			world.p.vertex(sVertices[2].x, sVertices[2].y, sVertices[2].z, origVideoWidth, origVideoHeight); 		// LOWER RIGHT        
			world.p.vertex(sVertices[3].x, sVertices[3].y, sVertices[3].z, 0, origVideoHeight);           // LOWER LEFT
		}
		else
		{
			world.p.vertex(vertices[0].x, vertices[0].y, vertices[0].z, 0, 0);           // UPPER LEFT      
			world.p.vertex(vertices[1].x, vertices[1].y, vertices[1].z, origVideoWidth, 0);           // UPPER RIGHT           
			world.p.vertex(vertices[2].x, vertices[2].y, vertices[2].z, origVideoWidth, origVideoHeight); 		// LOWER RIGHT        
			world.p.vertex(vertices[3].x, vertices[3].y, vertices[3].z, 0, origVideoHeight);           // LOWER LEFT
		}
		world.p.endShape(PApplet.CLOSE);       // End the shape containing the image
		world.p.popMatrix();

//		p.videosSeen++;
		p.setVideosSeen(p.getVideosSeen() + 1);
	}

	/**
	 * Draw the image metadata in Heads-Up Display
	 */
	public void displayMetadata(WMV_World world)
	{
		String strTitleVideo = "Video";
		String strTitleVideo2 = "-----";
		String strName = "Name: "+getName();
		String strID = "ID: "+PApplet.str(getID());
		String strCluster = "Cluster: "+PApplet.str(cluster);
		String strX = "Location X: "+PApplet.str(getCaptureLocation().z);
		String strY = " Y: "+PApplet.str(getCaptureLocation().x);
		String strZ = " Z: "+PApplet.str(getCaptureLocation().y);

		String strDate = "Date: "+PApplet.str(time.getMonth()) + PApplet.str(time.getDay()) + PApplet.str(time.getYear());
		String strTime = "Time: "+PApplet.str(time.getHour()) + ":" + (time.getMinute() >= 10 ? PApplet.str(time.getMinute()) : "0"+PApplet.str(time.getMinute())) + ":" + 
				 (time.getSecond() >= 10 ? PApplet.str(time.getSecond()) : "0"+PApplet.str(time.getSecond()));

		String strLatitude = "GPS Latitude: "+PApplet.str(gpsLocation.z);
		String strLongitude = " Longitude: "+PApplet.str(gpsLocation.x);
		String strAltitude = "Altitude: "+PApplet.str(gpsLocation.y);
		String strTheta = "Direction: "+PApplet.str(theta);
		String strElevation = "Vertical Angle: "+PApplet.str(phi);
		String strRotation = "Rotation: "+PApplet.str(rotation);

		String strTitleDebug = "--- Debugging ---";
		String strBrightness = "brightness: "+PApplet.str(viewingBrightness);
		String strBrightnessFading = "brightnessFadingValue: "+PApplet.str(fadingBrightness);
		
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

		if(p.debugSettings.video)
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
		PVector camLoc = viewerState.getLocation();
		PVector loc = new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z);

		float r;

		if(focusDistance == -1.f)
			r = defaultFocusDistance;						// Use default if no focus distance in metadata					      
		else
			r = focusDistance;							

		float xDisp = r * PApplet.sin(PApplet.radians(360-theta)) * PApplet.sin(PApplet.radians(90-phi)); 
		float zDisp = r * PApplet.cos(PApplet.radians(360-theta)) * PApplet.sin(PApplet.radians(90-phi));  
		float yDisp = r * PApplet.cos(PApplet.radians(90-phi)); 

		disp = new PVector(-xDisp, -yDisp, -zDisp);

		loc.add(disp);
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

		float farViewingDistance = viewerSettings.getFarViewingDistance();
		float nearViewingDistance = viewerSettings.getNearViewingDistance();
		
		if(viewDist > farViewingDistance)
		{
			float vanishingPoint = farViewingDistance + focusDistance;	// Distance where transparency reaches zero
//			float vanishingPoint = farViewingDistance + p.p.defaultFocusDistance;	// Distance where transparency reaches zero
			if(viewDist < vanishingPoint)
				distVisibility = PApplet.constrain(1.f - PApplet.map(viewDist, farViewingDistance, vanishingPoint, 0.f, 1.f), 0.f, 1.f);    // Fade out until cam.visibleFarDistance
			else
				distVisibility = 0.f;
		}
		else if(viewDist < nearViewingDistance) 													// Near distance at which transparency reaches zero
		{
			distVisibility = PApplet.constrain(PApplet.map(viewDist, viewerSettings.getNearClippingDistance(), nearViewingDistance, 0.f, 1.f), 0.f, 1.f);   					  // Fade out until visibleNearDistance
		}

//		if(p.debugSettings.video)
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
	void setThinningVisibility(boolean state)
	{
		thinningVisibility = state;
	}
	
	/**
	 * Get thinning visibility of image
	 * @param state New visibility
	 */
	public boolean getThinningVisibility()
	{
		return thinningVisibility;
	}

	/**
	 * getAngleToCamera()
	 * @return Angle between camera location and image 
	 */	
	public float getAngleToCamera()
	{
		PVector cameraPosition = p.p.viewer.getLocation();
		PVector centerVertex = calcCenterVertex();

		PVector cameraToFace = new PVector(  cameraPosition.x-centerVertex.x, 	//  Vector from the camera to the face.      
				cameraPosition.y-centerVertex.y, 
				cameraPosition.z-centerVertex.z   );

		PVector ab = new PVector(  vertices[1].x-vertices[0].x, 
				vertices[1].y-vertices[0].y, 
				vertices[1].z-vertices[0].z);
		PVector cb = new PVector(  vertices[1].x-vertices[2].x, 
				vertices[1].y-vertices[2].y, 
				vertices[1].z-vertices[2].z   );

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
		length = newLength;
	}

	/**
	 * @return Video length
	 */
	 public float getLength()
	 {
		 return length;
	 }
	 
	/**
	 * @return Whether video is facing the camera
	 */	
	public boolean isFacingCamera(PVector cameraPosition)
	{
		return PApplet.abs(getAngleToCamera()) > viewerSettings.visibleAngle;     			// If the result is positive, then it is facing the camera.
//		return PApplet.abs(getAngleToCamera()) > p.p.defaultVisibleAngle;     			// If the result is positive, then it is facing the camera.
	}

	/**
	 * @return Is the camera behind the video?  
	 */
	public boolean isBackFacing(PVector cameraPosition)										
	{
//		PVector cameraPosition = p.p.viewer.getLocation();

		float captureToCam = getCaptureLocation().dist(cameraPosition);  	// Find distance from capture location to camera
		float camToVideo = location.dist(cameraPosition);  		// Find distance from camera to image

//		if(captureToCam > camToVideo + p.p.viewer.getNearClippingDistance())			// If captureToCam > camToVideo, then back of video is facing the camera
		if(captureToCam > camToVideo + viewerSettings.getNearClippingDistance() / 2.f)			// If captureToCam > camToVideo, then back of video is facing the camera
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

		vertex1 = vertices[2];
		vertex2 = vertices[0];

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

		if(cameraModel == 1)
		{
			if (orientation == 90)  // Vertical Image
			{
				vertex1 = vertices[3];
				vertex2 = vertices[0];
				vertex3 = vertices[1];
			}
			else if (orientation == 0)    // Horizontal Image
			{
				vertex1 = vertices[0];
				vertex2 = vertices[1];
				vertex3 = vertices[2];
			}
			else if (orientation == 180)    // Upside Down (Horizontal) Image
			{
				vertex1 = vertices[2];
				vertex2 = vertices[3];
				vertex3 = vertices[0];
			}
			else  if (orientation == 270)    // Upside Down (Vertical) Image
			{
				vertex1 = vertices[1];
				vertex2 = vertices[2];
				vertex3 = vertices[3];
			}
		}
		else
		{
			vertex1 = vertices[0];
			vertex2 = vertices[1];
			vertex3 = vertices[2];
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
	 * findPlaceholder()
	 * Find image taken immediately before this video was captured to serve as placeholder, determining elevation and rotation angles
	 */
	public void findPlaceholder()
	{
		IntList candidates = new IntList();							// List of placeholder candidates
		
		for (int i = 0; i < p.getImages().size(); i++) 					// -- Should limit this to only cluster!!
		{
			if(time.getDate().equals(p.getImage(i).time.getDate()))				// Placeholder will be from same date
			{
				PVector imgLocation = p.getImage(i).getCaptureLocation();
				float curDist = PVector.dist(getCaptureLocation(), imgLocation);

				if(curDist < worldSettings.assocVideoDistTolerance)		// and very close in space,
				{
					candidates.append(i);												// Add to candidates list
				}
			}
		}
		
		int closestIdx = -1;
		float closestDist = 10000.f;
		
		if(candidates.size() == 0)
		{
			if(p.debugSettings.video)
				PApplet.println("Video "+getID()+" has no candidates under distance tolerance:"+worldSettings.assocVideoDistTolerance+"!");
		}
		
		for( int i : candidates )							// Compare distances of the candidates
		{
			float timeDiff = time.getTime() - p.getImage(i).time.getTime();

			if( timeDiff > 0.f && timeDiff < worldSettings.assocVideoTimeTolerance )			// If in very close succession with an image
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
			if(p.debugSettings.video || p.debugSettings.metadata)
				PApplet.println("Found image placeholder:"+p.getImage(closestIdx).getName()+"  for video:"+getName()+" placeholder ID:"+p.getImage(closestIdx).getID()+" closestIdx:"+closestIdx);
			boolean success = associateImagePlaceholder(p.getImage(closestIdx).getID(), closestDist, PApplet.abs(time.getTime() - p.getImage(closestIdx).time.getTime()));
			
			if(success)
			{
				if(p.debugSettings.video || p.debugSettings.metadata)
					PApplet.println("Set placeholder image id:"+p.getImage(closestIdx).getID());
			
				p.getImage(closestIdx).associateVideo(getID());
			}
		}
		
		if(!hasImagePlaceholder)
		{
			PApplet.println("No image placeholder found for video:"+getID()+", will set to disabled...");
			disabled = true;
			hidden = true;
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
	public boolean associateImagePlaceholder(int imageID, float newImageDist, float newImageTimeDiff)
	{
		boolean success = false;
		
		if(!hasImagePlaceholder)
			success = true;

		if(success)
		{
			hasImagePlaceholder = true;
			imagePlaceholder = imageID;
			WMV_Image i = p.getImage(imagePlaceholder);
			
			/* Set video parameters from image placeholder metadata */
			cameraModel = i.getCameraModel();
			focusDistance = i.getFocusDistance();		    
			focalLength = i.getFocalLength();
			orientation = i.getOrientation();       
			theta = i.getDirection();              	   
			phi = i.getVerticalAngle();            		
			rotation = i.getRotation();             
			sensorSize = i.getSensorSize();
			
			aspectRatio = getAspectRatio();								// Set aspect ratio from original height / width		
			videoWidth = i.getWidth();									
			videoHeight = (int) (i.getWidth() * aspectRatio);	
			
			calculateVertices();
		}
		
		return success;
	}

	/**
	 * Update volume fading 
	 */
	private void updateFadingVolume()
	{
		if(fadingVolume && p.frameCount < volumeFadingEndFrame)	// Still fading
		{
			volume = PApplet.map(p.frameCount, volumeFadingStartFrame, volumeFadingEndFrame, volumeFadingStartVal, volumeFadingTarget);
			video.volume(volume);
		}
		else								// Reached target
		{
			volume = volumeFadingTarget;
			fadingVolume = false;
			if(volume == 1.f)
				soundFadedIn = true;
			else if(volume == 0.f)
			{
				soundFadedOut = true;
			
				if(pauseAfterSoundFades)
					video.pause();
				else
					clearVideo();
			}
		}
	}

	/**
	 * @return Aspect ratio of the video
	 */
	float getAspectRatio()
	{
		float ratio = 0;

//		ratio = (float)(video.height)/(float)(video.width);
		ratio = (float) origVideoHeight / (float) origVideoWidth;

		return ratio;
	}

	/**
	 * @return Whether the vertices are null
	 */
	public boolean verticesAreNull()
	{
		if(vertices[0] != null && vertices[1] != null && vertices[2] != null && vertices[3] != null)
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

		if(videoAngle > viewerSettings.visibleAngle)
			angleBrightness = 0.f;
		else if (videoAngle < viewerSettings.visibleAngle * 0.66f)
			angleBrightness = 1.f;
		else
			angleBrightness = PApplet.constrain((1.f-PApplet.map(videoAngle, viewerSettings.visibleAngle * 0.66f, viewerSettings.visibleAngle, 0.f, 1.f)), 0.f, 1.f);

		return angleBrightness;
	}
	
	/**
	 * Fade focus distance to given target while rescaling images 
	 * @param target New focus distance
	 */
	public void fadeFocusDistance(float target)
	{
		fadingFocusDistance = true;
		fadingFocusDistanceStartFrame = p.frameCount;					
		fadingFocusDistanceEndFrame = p.frameCount + fadingFocusDistanceLength;	
		fadingFocusDistanceStart = focusDistance;
		fadingFocusDistanceTarget = target;
	}
	
	/**
	 * Update fading of object distance (focus distance and image size together)
	 */
	private void updateFadingFocusDistance()
	{
		float newFocusDistance = 0.f;

		if (p.frameCount >= fadingFocusDistanceEndFrame)
		{
			fadingFocusDistance = false;
			newFocusDistance = fadingFocusDistanceTarget;
		} 
		else
		{
			newFocusDistance = PApplet.map( p.frameCount, fadingFocusDistanceStartFrame, fadingFocusDistanceEndFrame, 
											fadingFocusDistanceStart, fadingFocusDistanceTarget);      // Fade with distance from current time
		}

		setFocusDistance( newFocusDistance );	// Set focus distance
		calculateVertices();  					// Update vertices given new width
	}

	
	void resetFocusDistance()
	{
		setFocusDistance(origFocusDistance);
	}

	/**	
	 * Setup video rectangle geometry 
	 */
	private PVector[] initializeVertices()
	{
		float width = getVideoWidthMeters();
		float height = getVideoWidthMeters() * aspectRatio;

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
		float result = sensorSize * subjectSizeRatio * focusDistance / focalLength;
		return result;
	}

	/**
	 * getAverageBrightness()
	 * @return Average pixel brightness for this frame
	 */
	private float getAverageBrightness() 
	{
		video.loadPixels();
		int b = 0;
		for (int i=0; i<video.pixels.length; i++) {
			float cur = p.p.p.brightness(video.pixels[i]);
			b += cur;
		}
		b /= video.pixels.length;
		return b;
	}

	/**
	 * Search associated field for nearest cluster and associate with this video
	 * @return Whether associated cluster was successfully found
	 */	
	public boolean findAssociatedCluster(float maxClusterDistance)    				 // Associate cluster that is closest to photo
	{
		int closestClusterIndex = 0;
		float closestDistance = 100000;

		for (int i = 0; i < p.getClusters().size(); i++) 
		{     
			WMV_Cluster curCluster = (WMV_Cluster) p.getClusters().get(i);
			float distanceCheck = getCaptureLocation().dist(curCluster.getLocation());

			if (distanceCheck < closestDistance)
			{
				closestClusterIndex = i;
				closestDistance = distanceCheck;
			}
		}

		if(closestDistance < maxClusterDistance)
		{
			cluster = closestClusterIndex;			// Associate cluster with this video
		}
		else
		{
			cluster = -1;						// Create a new single image cluster here!
//			p.disassociatedVideos++;
			p.setDisassociatedVideos(p.getDisassociatedVideos() + 1);
		}

		if(cluster != -1)
			return true;
		else
			return false;
	}
	
	/**
	 * Search given list of clusters and associated with this video
	 * @return Whether associated field was successfully found
	 */	
	public boolean findAssociatedCluster(ArrayList<WMV_Cluster> clusterList, float maxClusterDistance)    				 // Associate cluster that is closest to photo
	{
		int closestClusterIndex = 0;
		float closestDistance = 100000;

		for (int i = 0; i < clusterList.size(); i++) 
		{     
			WMV_Cluster curCluster = (WMV_Cluster) clusterList.get(i);
			float distanceCheck = getCaptureLocation().dist(curCluster.getLocation());

			if (distanceCheck < closestDistance)
			{
				closestClusterIndex = i;
				closestDistance = distanceCheck;
			}
		}

		if(closestDistance < maxClusterDistance)
		{
			cluster = closestClusterIndex;
		}
		else
		{
			cluster = -1;						// Create a new single image cluster here!
		}

		if(cluster != -1)
			return true;
		else
			return false;
	}

	/**
	 * getImagePlaceholder
	 * @return Image placeholder for this video
	 */
	public int getImagePlaceholder()
	{
		return imagePlaceholder;
	}
	
	 public float getDirection()
	 {
		 return theta;
	 }

	 public float getVerticalAngle()
	 {
		 return phi;
	 }
	 
	 public float getRotation()
	 {
		 return rotation;
	 }
	 
	 public float getWidth()
	 {
		 return videoWidth;
	 }

	 public float getHeight()
	 {
		 return videoHeight;
	 }
	 
	 public float getFocusDistance()
	 {
		 return focusDistance;
	 }

	 public float getFocalLength()
	 {
		 return focalLength;
	 }

	 public float getSensorSize()
	 {
		 return sensorSize;
	 }
	 
	 public boolean isFadingVolume()
	 {
		 return fadingVolume;
	 }
	 
	 public boolean isLoaded()
	 {
		 return loaded;
	 }
	 
	 public boolean isPlaying()
	 {
		 return playing;
	 }

	 public void setFocusDistance(float newFocusDistance)
	 {
		 focusDistance = newFocusDistance;
	 }

	 public void setFocalLength(float newFocalLength)
	 {
		 focalLength = newFocalLength;
	 }

	 public void setSensorSize(float newSensorSize)
	 {
		 sensorSize = newSensorSize;
	 }
}