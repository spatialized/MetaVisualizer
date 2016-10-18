package gmViewer;

import processing.video.*;

import java.util.Calendar;

import processing.core.*;
import processing.data.IntList;

/**************************************************
 * GMV_Video
 * @author davidgordon
 * A rectangular video in 3D virtual space
 */

class GMV_Video extends GMV_Viewable          		 // Represents a video in virtual space
{
	/* Video */
	Movie video;
	private final float defaultFrameRate = 29.98f;
	private boolean videoLoaded = false;
	private boolean videoPlaying = false;
	private boolean soundFadedIn = false, soundFadedOut = false;
	
	/* Metadata */
	int origVideoWidth = 0, origVideoHeight = 0;
	PVector averageColor;
	float averageBrightness;

	private float orientation;              		// Landscape = 0, Portrait = 90, Upside Down Landscape = 180, Upside Down Portrait = 270
	private float phi, rotation;       				// Elevation angle and Z-axis rotation
	private float focalLength = 0; 					// Zoom Level 
	private float focusDistance; 	 		 		// Object Distance --> defaults to 30
	private float sensorSize;
	private PVector disp = new PVector(0, 0, 0);    		// Displacement from capture location
	private float length;

	/* Graphics */
	private float videoWidth = 0, videoHeight = 0;			// Video width and height
	PVector[] vertices;
	public PVector azimuthAxis = new PVector(0, 1, 0);
	public PVector verticalAxis = new PVector(1, 0, 0);
	public PVector rotationAxis = new PVector(0, 0, 1);
	public float outlineSize = 10.f;
	
	private float fadingObjectDistanceStartFrame = 0.f, fadingObjectDistanceEndFrame = 0.f;	// Fade focus distance and image size together
	private float fadingFocusDistanceStart = 0.f, fadingFocusDistanceTarget = 0.f;
	private float fadingImageSizeFactorStart = 0.f, fadingImageSizeFactorTarget = 0.f;	
	private float fadingObjectDistanceLength = 30.f;

	private boolean thinningVisibility = false;
 	
	/* Sound */
	private float volume = 0.f;			// Video volume between 0. and 1.
	private boolean fadingVolume = false;
	private int volumeFadingStartFrame = 0, volumeFadingEndFrame = 0;
	private float volumeFadingStartVal = 0.f, volumeFadingTarget = 0.f;
	private final int volumeFadingLength = 60;	// Fade volume over 30 frames
	
	/* Navigation */
	private boolean isClose = false;				// Is the viewer in visible range?
	
	/* Placeholder Image */
	private boolean hasImagePlaceholder = false;
	private int imagePlaceholder = -1;

	GMV_Video ( GMV_Field parent, int newID, String newName, String newFilePath, PVector newGPSLocation, float newTheta, float newFocalLength, 
			float newOrientation, float newElevation, float newRotation, float newFocusDistance, int newCameraModel, int newVideoWidth, 
			int newVideoHeight, float newBrightness, Calendar newCalendar )
	{
		super(parent, newID, newName, newFilePath, newGPSLocation, newTheta, newCameraModel, newBrightness, newCalendar);

		p = parent;
//		name = newName;
		
		vertices = new PVector[4]; 

		filePath = newFilePath;

		origVideoWidth = newVideoWidth;
		origVideoHeight = newVideoHeight;
		
		videoWidth = newVideoWidth;
		videoHeight = newVideoHeight;
		brightness = newBrightness;
		
		gpsLocation = newGPSLocation;
//		setCaptureLocationPVector(0, 0, 0);
		focusDistance = newFocusDistance;

		focalLength = newFocalLength;
		cameraModel = newCameraModel;

		theta = newTheta;              		// GPS Orientation (Yaw angle)
		orientation = newOrientation;       // Vertical (90) or Horizontal (0)
		phi = newElevation;            		// Pitch angle
		rotation = newRotation;             // Rotation angle
	}  

	/**
	 * draw()
	 * Display the video in virtual space
	 */
	void draw()
	{
		if(!verticesAreNull())
		{
			float distanceBrightnessFactor = 0.f; 					// Fade with distance
			float timeBrightnessFactor;                          // Fade with time 
			float angleBrightnessFactor;
			
			float brightness = fadingBrightness;					

			distanceBrightnessFactor = getDistanceBrightness(); 
			brightness *= distanceBrightnessFactor; 								// Fade alpha based on distance to camera

			if( p.p.timeFading )
			{
				if(!p.p.viewer.isMoving())
				{
					if(p.p.showAllTimeSegments)
					{
						if(p.p.getCluster(cluster).timeline.size() > 0)
							timeBrightnessFactor = getTimeBrightness();    
						else
						{
							timeBrightnessFactor = 0.f;
							p.p.display.message("Video Cluster: "+cluster+" has no timeline points!");
						}
						
						brightness *= timeBrightnessFactor; 					// Fade brightness based on time
					}
					else
					{
						if(p.p.viewer.getCurrentCluster() == cluster)
						{
							timeBrightnessFactor = getTimeBrightness();        
							brightness *= timeBrightnessFactor; 					// Fade brightness based on time
						}
						else														// Hide media outside current cluster
						{
							timeBrightnessFactor = 0.f;
							brightness = 0.f;
						}
					}
				}
			}

			if(isClose && distanceBrightnessFactor == 0.f)							// Video recently moved out of range
			{
				isClose = false;
				fadeOut();
			}

			if(p.p.debug.video && p.p.debug.detailed && p.p.frameCount % 30 == 0)
				p.p.display.message("Video brightness after distance:"+brightness);

			if( p.p.angleFading )
			{
				float videoAngle = getFacingAngle();
				if(p.p.utilities.isNaN(videoAngle))
				{
					videoAngle = 0;				
					visible = false;
					disabled = true;
				}

				angleBrightnessFactor = getAngleBrightness(videoAngle);                 // Fade out as turns sideways or gets too far / close
				brightness *= angleBrightnessFactor;
//				p.p.display.message("brightness:"+brightness+" viewingBrightness:"+PApplet.map(brightness, 0.f, 1.f, 0.f, 255.f));
			}

			viewingBrightness = PApplet.map(brightness, 0.f, 1.f, 0.f, 255.f);				// Scale to setting for alpha range

			if (visible && !hidden && !disabled && !p.p.viewer.map3DMode) 
			{
				if (viewingBrightness > 0)
				{
					drawVideo();          // Draw the video 
				}
			}
			else
			{      
				p.p.noFill();                  // Hide video if it isn't visible
			}

			if (visible && !disabled && (p.p.debug.model || p.p.viewer.map3DMode))
				drawLocation(centerSize);
		}
	}

	/**
	 * fadeIn()
	 * Fade in video
	 */
	public void fadeIn()
	{
		if(fading || isFadingIn || isFadingOut)		// If already fading, stop at current value
			if(!initFading)		
				stopFading();

		fadeBrightness(1.f);					// Fade in
	}

	/**
	 * fadeOut()
	 * Fade out video
	 */
	public void fadeOut()
	{
		if(fading || isFadingIn || isFadingOut)		// If already fading, stop at current value
			if(!initFading)			
				stopFading();

		fadeBrightness(0.f);					// Fade out
	}

	/**
	 * fadeSoundIn()
	 * Fade in sound
	 */
	void fadeSoundIn()
	{
		if(volume < p.p.videoMaxVolume)
		{
			fadingVolume = true;
			volumeFadingStartFrame = p.p.frameCount; 
			volumeFadingStartVal = volume; 
			volumeFadingEndFrame = p.p.frameCount + volumeFadingLength;		// Fade volume over 30 frames
			volumeFadingTarget = p.p.videoMaxVolume;
		}
	}
	
	/**
	 * fadeSoundOut()
	 * Fade out sound
	 */
	void fadeSoundOut()
	{
		if(volume > 0.f)
		{
			fadingVolume = true;
			volumeFadingStartFrame = p.p.frameCount; 
			volumeFadingStartVal = volume; 
			volumeFadingEndFrame = p.p.frameCount + volumeFadingLength;		// Fade volume over 30 frames
			volumeFadingTarget = 0.f;
		}
	}
	
	/**
	 * update()
=	 * Update main variables 
	 */
	void update()
	{
		if(!disabled)			
		{
			visible = getAngleVisibility();						// Check if video should be visible from current viewer position

			if(visible)
			{
				float videoAngle = getFacingAngle();				// Check if video is visible at current angle facing viewer
				if(!p.p.utilities.isNaN(videoAngle))
					visible = (getAngleBrightness(videoAngle) > 0.f);


				if(visible)
					visible = (getDistanceBrightness() > 0.f);

				if(orientation != 0 && orientation != 90)          	// Hide orientations of 180 or 270 (avoid upside down images)
					visible = false;

				if(isBackFacing() || isBehindCamera())
					visible = false;

				if(visible && !fading && !fadedOut)					// Fade in
				{
					if(!videoLoaded) loadMedia();
					fadeIn();
				}

				if(p.p.debug.video && p.p.frameCount % 30 == 0)
					p.p.display.message("After backFacing... "+visible);
			}
//			if(visible && !fading)				// Fade in if visible at beginning 
//			{
//				if(!videoLoaded) loadMedia();
//				fadeIn();
//			}

			boolean wasFading = false;
			if(isFading())									// Update brightness while fading
			{
				wasFading = true;
				updateFadingBrightness();

				if(fadingBrightness == 0.f)
					visible = false;
			}

			if(fadingObjectDistance)
			{
				updateFadingObjectDistance();
			}
			else if(visible)
				calculateVertices();  			// Update image parameters

//			if(fadingBrightness == 1.f && wasFading && !isFading())		// Fade in sound once video has faded in
			if(fadedIn)		// Fade in sound once video has faded in
			{
				fadeSoundIn();
				fadedIn = false;						
			}

//			if(fadingBrightness == 0.f && wasFading && !isFading())		// Fade out sound once video has faded out
			if(fadedOut) 
			{
				fadeSoundOut();
				fadedOut = false;						
			}
			
			if(soundFadedIn) soundFadedIn = false;
			if(soundFadedOut) soundFadedOut = false;
			
			if(fadingVolume && video != null)
			{
				updateFadingVolume();
			}
		}
	}
	
	/**
	 * calculateVertices()
	 * Update video geometry each frame
	 */
	public void calculateVertices()								
	{
		initializeVertices();

		if (phi != 0.)
			vertices = rotateVertices(vertices, -phi, verticalAxis);         // Rotate around X axis

		vertices = rotateVertices(vertices, 360-theta, azimuthAxis);          // Rotate around Z axis

		if(vertices.length == 0) disabled = true;

		if(!p.p.transitionsOnly)
			vertices = translateVertices(vertices, getCaptureLocation());                       // Move video to movie capture location   

		float r;				   // Radius of sphere

		if(focusDistance == -1.f)
			r = p.p.defaultFocusDistance;						// Use default if no focus distance in metadata					      
		else
			r = focusDistance;							

		float xDisp = r * PApplet.sin(PApplet.radians(360-theta)) * PApplet.sin(PApplet.radians(90-phi)); 
		float zDisp = r * PApplet.cos(PApplet.radians(360-theta)) * PApplet.sin(PApplet.radians(90-phi));  
		float yDisp = r * PApplet.cos(PApplet.radians(90-phi)); 

		disp = new PVector(-xDisp, -yDisp, -zDisp);

		vertices = translateVertices(vertices, disp);         // Translate vertices to viewing location

		if(p.p.transitionsOnly)
			location = new PVector (0, 0, 0);
		else
			location = new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z);

		location.add(disp);

		if (p.p.utilities.isNaN(location.x) || p.p.utilities.isNaN(location.x) || p.p.utilities.isNaN(location.x))
			location = new PVector (0, 0, 0);
	}

	/**
	 * loadMedia()
	 * Load the video file from disk
	 */
	public void loadMedia()
	{
		if(!p.p.debug.lowMemory)			// Check enough memory available
		{
			if(!disabled)																	
			{
				video = new Movie(p.p, filePath);
				setLength( video.duration() );				// Set video length (in seconds)

				video.play();					// Start playing
				video.loop();					// Start loop
				videoPlaying = true;
				
				if(p.p.debug.video)
					p.p.display.message("Loading video file..."+filePath+" video.duration():"+video.duration());
				
				calculateVertices(); 
				videoLoaded = true;
			}
		}
		else p.p.display.message("Low memory: cannot load video!");
	}

	/**
	 * stopVideo()
	 * Stop playing the video
	 */
	public void stopVideo()
	{
		if(p.p.debug.video) 
			p.p.display.message("Stopping video file..."+getID());

		video.noLoop();
		video.stop();
		videoPlaying = false;
	}

	/**
	 * clearVideo()
	 * Stop playing the video
	 */
	public void clearVideo()
	{
		if(p.p.debug.video) 
			p.p.display.message("Clearing video file..."+getID());

		video.noLoop();
		video.stop();
		video.dispose();
		videoLoaded = false;
		videoPlaying = false;
	}

	/**
	 * drawVideo()
	 * Draw the video in virtual space
	 */
	private void drawVideo()
	{
		p.p.rectMode(PApplet.CENTER);
		p.p.noStroke(); 

		if(isSelected())
		{
			if (!p.p.viewer.selection && p.p.debug.field)     // Draw outline
			{
				p.p.stroke(19, 200, 150);
				p.p.strokeWeight(outlineSize);
			}
		}

		p.p.pushMatrix();
		p.p.beginShape(PApplet.POLYGON);    // Begin the shape containing the video

		p.p.textureMode(PApplet.IMAGE);
		p.p.texture(video);

		if(p.p.viewer.selection)
		{
			if(isSelected())
			{
				if(!p.p.alphaMode)
					p.p.tint(viewingBrightness, 255);          				
				else
					p.p.tint(255, viewingBrightness);          				
			}
			else
			{
				if(!p.p.alphaMode)
					p.p.tint(viewingBrightness * 0.333f, 255);          // Set the image transparency					
				else
					p.p.tint(255, viewingBrightness * 0.333f);          				
			}
		}
		else if(p.p.viewer.videoMode)
		{
			if(!p.p.alphaMode)
				p.p.tint(viewingBrightness, 255);          				
			else
				p.p.tint(255, viewingBrightness);          				
		}
		else
		{
			if(!p.p.alphaMode)
				p.p.tint(viewingBrightness, 255);          				
			else
				p.p.tint(255, viewingBrightness);          				
		}
		
		p.p.vertex(vertices[0].x, vertices[0].y, vertices[0].z, 0, 0);           // UPPER LEFT      
		p.p.vertex(vertices[1].x, vertices[1].y, vertices[1].z, origVideoWidth, 0);           // UPPER RIGHT           
		p.p.vertex(vertices[2].x, vertices[2].y, vertices[2].z, origVideoWidth, origVideoHeight); 		// LOWER RIGHT        
		p.p.vertex(vertices[3].x, vertices[3].y, vertices[3].z, 0, origVideoHeight);           // LOWER LEFT

		p.p.endShape(PApplet.CLOSE);       // End the shape containing the image
		p.p.popMatrix();

		p.videosSeen++;
	}

	/**
	 * displayMetadata()
	 * Draw the image metadata in Heads-Up Display
	 */
	public void displayMetadata()
	{
		String strTitleVideo = "Video";
		String strTitleVideo2 = "-----";
		String strName = "Name: "+getName();
		String strID = "ID: "+PApplet.str(getID());
		String strCluster = "Cluster: "+PApplet.str(cluster);
		String strX = "Location X: "+PApplet.str(getCaptureLocation().z);
		String strY = " Y: "+PApplet.str(getCaptureLocation().x);
		String strZ = " Z: "+PApplet.str(getCaptureLocation().y);
		String strLatitude = "GPS Latitude: "+PApplet.str(gpsLocation.z);
		String strLongitude = " Longitude: "+PApplet.str(gpsLocation.x);
		String strAltitude = "Altitude: "+PApplet.str(gpsLocation.y);
		String strTheta = "Direction: "+PApplet.str(theta);
		String strElevation = "Vertical Angle: "+PApplet.str(phi);
		String strRotation = "Rotation: "+PApplet.str(rotation);

		String strTitleDebug = "--- Debugging ---";
		String strBrightness = "brightness: "+PApplet.str(viewingBrightness);
		String strBrightnessFading = "brightnessFadingValue: "+PApplet.str(fadingBrightness);
		
		p.p.display.metadata(strTitleVideo);
		p.p.display.metadata(strTitleVideo2);
		p.p.display.metadata("");
		p.p.display.metadata(strID);
		p.p.display.metadata(strCluster);
		p.p.display.metadata(strName);
		p.p.display.metadata(strX + strY + strZ);

//		p.p.display.metadata(strTitleMetadata);
		p.p.display.metadata(strLatitude + strLongitude);
		p.p.display.metadata(strAltitude);
		p.p.display.metadata(strTheta);
		p.p.display.metadata(strElevation);
		p.p.display.metadata(strRotation);

		if(p.p.debug.video)
		{
			p.p.display.metadata(strTitleDebug);
			p.p.display.metadata(strBrightness);
			p.p.display.metadata(strBrightnessFading);
		}	
	}

	/**
	 * getViewingDistance()
	 * @return How far the video is from the camera
	 */
	public float getViewingDistance()       // Find distance from camera to point in virtual space where photo appears           
	{
		PVector camLoc;

		if(p.p.transitionsOnly)
		{
			camLoc = p.p.viewer.getLocation();
		}
		else
		{
			camLoc = p.p.viewer.getLocation();
		}

		PVector loc = new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z);

		float r;

		if(focusDistance == -1.f)
			r = p.p.defaultFocusDistance;						// Use default if no focus distance in metadata					      
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

		float farViewingDistance = p.p.viewer.getFarViewingDistance();
		float nearViewingDistance = p.p.viewer.getNearViewingDistance();
		
		if(viewDist > farViewingDistance)
		{
			float vanishingPoint = farViewingDistance + p.p.defaultFocusDistance;	// Distance where transparency reaches zero
			if(viewDist < vanishingPoint)
				distVisibility = PApplet.constrain(1.f - PApplet.map(viewDist, farViewingDistance, vanishingPoint, 0.f, 1.f), 0.f, 1.f);    // Fade out until cam.visibleFarDistance
			else
				distVisibility = 0.f;
		}
		else if(viewDist < nearViewingDistance) 													// Near distance at which transparency reaches zero
		{
			distVisibility = PApplet.constrain(PApplet.map(viewDist, p.p.viewer.getNearClippingDistance(), nearViewingDistance, 0.f, 1.f), 0.f, 1.f);   					  // Fade out until visibleNearDistance
		}

		return distVisibility;
	}

	/**
	 * Check whether video is at an angle where it should currently be visible
	 */
	public boolean getAngleVisibility()				 // Check if video should be visible
	{
		boolean visible = false;

		if(p.p.transitionsOnly)					// With StaticMode ON, determine visibility based on distance of associated cluster 
		{
			if(cluster == p.p.viewer.getCurrentCluster())		// If this photo's cluster is the current (closest) cluster, it is visible
				visible = true;

			for(int id : p.p.viewer.clustersVisible)
			{
				if(cluster == id)			// If this photo's cluster is on next closest list, it is visible	-- CHANGE THIS??!!
					visible = true;
			}

			return visible;
		}
		else 
		{
			if(p.p.angleHidingMode)
			{
				if(p.p.angleThinning)										// Angle Thinning mode
				{
					return isFacingCamera() && thinningVisibility;		
				}
				else return isFacingCamera();	// Return true if image plane is facing the camera
			}
			else 
				return true;     										 		
		}
	}
	
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
	 * setLength
	 * @param newLength New video length
	 */
	void setLength(float newLength)
	{
		length = newLength;
	}

	 public float getLength()
	 {
		 return length;
	 }
	 
	/**
	 * isFacingCamera()
	 * @return Whether video is facing the camera
	 */	
	public boolean isFacingCamera()
	{
		return PApplet.abs(getAngleToCamera()) > p.p.visibleAngle;     			// If the result is positive, then it is facing the camera.
//		return PApplet.abs(getAngleToCamera()) > p.p.defaultVisibleAngle;     			// If the result is positive, then it is facing the camera.
	}

	/**
	 * isBackFacing()
	 * @return Is the camera behind the video?  
	 */
	public boolean isBackFacing()										
	{
		PVector camLoc = p.p.viewer.getLocation();

		float captureToCam = getCaptureLocation().dist(camLoc);  	// Find distance from capture location to camera
		float camToVideo = location.dist(camLoc);  		// Find distance from camera to image

//		if(captureToCam > camToVideo + p.p.viewer.getNearClippingDistance())			// If captureToCam > camToVideo, then back of video is facing the camera
		if(captureToCam > camToVideo + p.p.viewer.getNearClippingDistance() / 2.f)			// If captureToCam > camToVideo, then back of video is facing the camera
			return true;
		else
			return false; 
	}

	/**
	 * getFacingAngle()
	 * @return Angle between the video and direction the camera is facing
	 */
	public float getFacingAngle()
	{
		PVector camOrientation = p.p.viewer.getOrientationVector();
		PVector faceNormal = getFaceNormal();

		PVector crossVector = new PVector();
		PVector.cross(camOrientation, faceNormal, crossVector);				// Cross vector gives angle between camera and image

		float result = crossVector.mag();
		return result;
	}

	/**
	 * isBehindCamera()
	 * @return Whether video is behind camera
	 */
	boolean isBehindCamera()										
	{
		PVector camLocation = p.p.viewer.getLocation();
		PVector camOrientation = p.p.viewer.getOrientationVector();
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
	 * calcCenterVertex()
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
		
		for (int i = 0; i < p.images.size(); i++) 
		{
			if(time.getDate() == p.images.get(i).time.getDate())				// Placeholder will be from same date
			{
				PVector imgLocation = p.images.get(i).getCaptureLocation();
				float curDist = PVector.dist(getCaptureLocation(), imgLocation);

				if(curDist < p.p.assocVideoDistTolerance)		// and very close in space,
				{
					candidates.append(i);												// Add to candidates list
				}
			}
		}
		
		int closestIdx = -1;
		float closestDist = 10000.f;
		
		if(candidates.size() == 0)
		{
			PApplet.println("Video "+getID()+" has no candidates under distance tolerance:"+p.p.assocVideoDistTolerance+"!");
		}
		
		for( int i : candidates )							// Compare distances of the candidates
		{
			float timeDiff = time.getTime() - p.images.get(i).time.getTime();

			if( timeDiff > 0.f && timeDiff < p.p.assocVideoTimeTolerance )			// If in very close succession with an image
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
			if(p.p.debug.video || p.p.debug.metadata)
				PApplet.println("Found image placeholder:"+p.images.get(closestIdx).getName()+"  for video:"+getName()+" placeholder ID:"+p.images.get(closestIdx).getID()+" closestIdx:"+closestIdx);
			boolean success = associateImagePlaceholder(p.images.get(closestIdx).getID(), closestDist, PApplet.abs(time.getTime() - p.images.get(closestIdx).time.getTime()));
			
			if(success)
			{
				if(p.p.debug.video || p.p.debug.metadata)
					PApplet.println("Set placeholder image id:"+p.images.get(closestIdx).getID());
			
				p.images.get(closestIdx).associateVideo(getID());
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
	 * associateImagePlaceholder()
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
			GMV_Image i = p.images.get(imagePlaceholder);
			
			cameraModel = i.getCameraModel();
			focusDistance = i.getFocusDistance();		    
			focalLength = i.getFocalLength();
			orientation = i.getOrientation();       
			theta = i.getDirection();              	   
			phi = i.getVerticalAngle();            		
			rotation = i.getRotation();             
			sensorSize = i.getSensorSize();
			
			aspectRatio = getAspectRatio();								// Set aspect ratio from original height / width		
			videoWidth = i.getWidth();								// Use image width
			videoHeight = (int) (i.getWidth() * aspectRatio);		
		}
		
		return success;
	}

	/**
	 * outline()
	 * Draw outline around selected video
	 */
	private void outline()
	{
		p.p.stroke(100, 20, 250);
		p.p.strokeWeight(outlineSize);

		p.p.pushMatrix();
		p.p.beginShape(PApplet.QUADS);    
		p.p.noFill();

		p.p.vertex(vertices[0].x, vertices[0].y, vertices[0].z, 0, 0);        // UPPER LEFT      
		p.p.vertex(vertices[1].x, vertices[1].y, vertices[1].z, 1, 0);        // UPPER RIGHT           
		p.p.vertex(vertices[2].x, vertices[2].y, vertices[2].z, 1, 1); 		// LOWER RIGHT        
		p.p.vertex(vertices[3].x, vertices[3].y, vertices[3].z, 0, 1);        // LOWER LEFT

		p.p.endShape(); 
		p.p.popMatrix();
	}

	/**
	 * updateFadingVolume()
	 * Update volume fading 
	 */
	private void updateFadingVolume()
	{
		if(fadingVolume && p.p.frameCount < volumeFadingEndFrame)	// Still fading
		{
			volume = PApplet.map(p.p.frameCount, volumeFadingStartFrame, volumeFadingEndFrame, volumeFadingStartVal, volumeFadingTarget);
		}
		else								// Reached target
		{
			volume = volumeFadingTarget;
			fadingVolume = false;
			if(volume == 1.f)
				soundFadedIn = true;
			else if(volume == 0.f)
				soundFadedOut = true;
			
//			video.frameRate(defaultFrameRate);			// Set default frame rate
		}
	}

	/**
	 * lineToCaptureLocation()
	 * Draw capture location for the image
	 */
	void lineToCaptureLocation()
	{
		PVector centerVertex = calcCenterVertex();
		p.p.stroke(150, 150, 255, 255);
		p.p.strokeWeight(2);
		p.p.line(location.x, location.y, location.z, centerVertex.x, centerVertex.y, centerVertex.z);
	}

	/**
	 * getAspectRatio()
	 * @return Aspect ratio of the video
	 */
	float getAspectRatio()
	{
		float ratio = 0;

//		ratio = (float)(video.height)/(float)(video.width);
		ratio = (float) origVideoHeight / (float) origVideoWidth;
				
		if (ratio > 1.)
			ratio = 0.666f;

		return ratio;
	}

	/** 
	 * rotateVertices()
	 * Rotate list of vertices using matrices
	 * @param verts Vertices to rotate
	 * @param angle Angle to rotate by
	 * @param axis Axis to rotate around
	 * @return Rotated vertices
	 */
	public PVector[] rotateVertices(PVector[] verts, float angle, PVector axis) 
	{
		boolean failed = false;
		int vl = verts.length;
		PVector[] clone = new PVector[vl];
		PVector[] dst = new PVector[vl];

		try
		{
			for (int i = 0; i < vl; i++)
				clone[i] = PVector.add(verts[i], new PVector());

			PMatrix3D rMat = new PMatrix3D();
			rMat.rotate(PApplet.radians(angle), axis.x, axis.y, axis.z);

			for (int i = 0; i < vl; i++)
				dst[i] = new PVector();
			for (int i = 0; i < vl; i++)
				rMat.mult(clone[i], dst[i]);
		}
		catch(NullPointerException e)
		{
			PApplet.println("NullPointerException: "+e);
			failed = true;
		}
		if(!failed)
		{
			return dst;
		}
		else
		{
			return new PVector[0];
		}
	}

	/** 
	 * translateVertices()
	 * Translate list of vertices with matrices
	 * @param verts Vertices to translate
	 * @param dest Point to translate to
	 * @return Translated vertices
	 */
	public PVector[] translateVertices(PVector[] verts, PVector dest) // Translate vertices by a given vector
	{
		int vl = verts.length;
		PVector[] clone = new PVector[vl];

		for (int i = 0; i < vl; i++)
			clone[i] = PVector.add(verts[i], new PVector());

		PMatrix3D tMat = new PMatrix3D();
		tMat.translate(dest.x, dest.y, dest.z);

		PVector[] dst = new PVector[vl];

		for (int i = 0; i < vl; i++)
			dst[i] = new PVector();
		for (int i = 0; i < vl; i++)
			tMat.mult(clone[i], dst[i]);

		return dst;
	}

	/**
	 * verticesAreNull()
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
	 * getAverageColor()
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

		if(videoAngle > p.p.visibleAngle)
			angleBrightness = 0.f;
		else if (videoAngle < p.p.visibleAngle * 0.66f)
			angleBrightness = 1.f;
		else
			angleBrightness = PApplet.constrain((1.f-PApplet.map(videoAngle, p.p.visibleAngle * 0.66f, p.p.visibleAngle, 0.f, 1.f)), 0.f, 1.f);

		return angleBrightness;
	}
	
	/**
	 * Fade focus distance to given target while rescaling images 
	 * @param target New focus distance
	 */
	public void fadeObjectDistance(float target)
	{
		fadingObjectDistance = true;
		fadingObjectDistanceStartFrame = p.p.frameCount;					
		fadingObjectDistanceEndFrame = p.p.frameCount + fadingObjectDistanceLength;	
		fadingFocusDistanceStart = focusDistance;
		fadingFocusDistanceTarget = target;
	}
	
	/**
	 * Update fading of object distance (focus distance and image size together)
	 */
	private void updateFadingObjectDistance()
	{
		float newFocusDistance = 0.f;

		if (p.p.frameCount >= fadingObjectDistanceEndFrame)
		{
			fadingObjectDistance = false;
			newFocusDistance = fadingFocusDistanceTarget;
		} 
		else
		{
			newFocusDistance = PApplet.map( p.p.frameCount, fadingObjectDistanceStartFrame, fadingObjectDistanceEndFrame, 
											fadingFocusDistanceStart, fadingFocusDistanceTarget);      // Fade with distance from current time
		}

		setFocusDistance( newFocusDistance );	// Set focus distance
		calculateVertices();  					// Update vertices given new width
	}


	/**	
	 * initializeVertices()
	 * Setup video rectangle geometry 
	 */
	private void initializeVertices()
	{
		float width = getVideoWidthMeters();
		float height = getVideoWidthMeters() * aspectRatio;

		float left = -width * 0.5f;
		float right = width * 0.5f;
		float top = -height * 0.5f;
		float bottom = height * 0.5f;
		
		vertices = new PVector[4]; 

		vertices[0] = new PVector( left, top, 0 );     // UPPER LEFT  
		vertices[1] = new PVector( right, top, 0 );      // UPPER RIGHT 
		vertices[2] = new PVector( right, bottom, 0 );       // LOWER RIGHT
		vertices[3] = new PVector( left, bottom, 0 );      // LOWER LEFT
	}
	
	private float getVideoWidthMeters()
	{
		// 	Image Size = Sensor Width * Focus Distance / Focal Length 
		float result = sensorSize * p.p.subjectSizeRatio * focusDistance / focalLength;
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
			float cur = p.p.brightness(video.pixels[i]);
			b += cur;
		}
		b /= video.pixels.length;
		return b;
	}

	/**
	 * findAssociatedCluster()
	 * Set nearest cluster to the capture location to be the associated cluster
	 * @return Whether associated cluster was successfully found
	 */	
	public boolean findAssociatedCluster()    				 // Associate cluster that is closest to photo
	{
		int closestClusterIndex = 0;
		float closestDistance = 100000;

		for (int i = 0; i < p.clusters.size(); i++) 
		{     
			GMV_Cluster curCluster = (GMV_Cluster) p.clusters.get(i);
			float distanceCheck = getCaptureLocation().dist(curCluster.getLocation());

			if (distanceCheck < closestDistance)
			{
				closestClusterIndex = i;
				closestDistance = distanceCheck;
			}
		}

		if(closestDistance < p.p.getCurrentModel().maxClusterDistance)
			cluster = closestClusterIndex;
		else
		{
			cluster = -1;						// Create a new single image cluster here!
			p.disassociatedVideos++;
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