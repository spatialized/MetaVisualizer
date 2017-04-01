package multimediaLocator;

import java.time.ZonedDateTime;
import java.util.ArrayList;
//import java.util.Calendar;

import processing.core.*;

/************************************
 * @author davidgordon
 * A rectangular image in 3D virtual space
 */

class WMV_Image extends WMV_Viewable						 
{
	/* Graphics */
	public PImage image, blurred;			// Image pixels
	public PVector[] vertices, sVertices;	// Vertex list
	
	private int horizBorderID = -1;					// Blur horizBorderID   0: Left 1: Center 2: Right  3: Left+Right
	private int vertBorderID = -1;					// Blur vertBorderID	0: Bottom 1: Center 2: Top  3: Top+Bottom

	private PImage blurMask;
	public int blurMaskID;
	private float outlineSize = 10.f;		// Size of the outline around a selected image

	private PVector disp = new PVector(0, 0, 0);   // Displacement from capture location
	private float fadingFocusDistanceStartFrame = 0.f, fadingFocusDistanceEndFrame = 0.f;	// Fade focus distance and image size together
	private float fadingFocusDistanceStart = 0.f, fadingFocusDistanceTarget = 0.f;
	private float fadingFocusDistanceLength = 30.f;
//	private float fadingImageSizeFactorStart = 0.f, fadingImageSizeFactorTarget = 0.f;	
	
	private boolean thinningVisibility = false;

	/* Metadata */
	private float imageWidth, imageHeight;				// Image width and height
	private float phi;			        				// Image Elevation (in Degrees N)
	private float orientation;              			// Landscape = 0, Portrait = 90, Upside Down Landscape = 180, Upside Down Portrait = 270
	private float rotation;				    			// Elevation angle and Z-axis rotation
	private float focalLength = 0; 						// Zoom Level 
	private float defaultFocusDistance = 9.0f;			// Default focus distance for images and videos (m.)
	private float focusDistance; 	 					// Image viewing distance (or estimated object distance, if given in metadata)
	private float origFocusDistance; 	 				// Original image viewing distance
	
	private float sensorSize;							// Approx. size of sensor in mm.
	private float subjectSizeRatio = 0.18f;				// Subject portion of image plane (used in scaling from focus distance to imageSize)
//	private float brightness;
	
	/* Video Association */
	private boolean isVideoPlaceHolder = false;
	private int assocVideoID = -1;

	WMV_Image ( int newID, PImage newImage, int newMediaType, String newName, String newFilePath, PVector newGPSLocation, float newTheta, float newFocalLength, 
			float newOrientation, float newElevation, float newRotation, float newFocusDistance, float newSensorSize, int newCameraModel, 
			int newWidth, int newHeight, float newBrightness, ZonedDateTime newDateTime, String newTimeZone ) 
	{
		super(newID, newMediaType, newName, newFilePath, newGPSLocation, newTheta, newCameraModel, newBrightness, newDateTime, newTimeZone);

		filePath = newFilePath;

		image = newImage;														// Empty image
		imageWidth = newWidth;
		imageHeight = newHeight;
		
		vertices = new PVector[4]; 
		sVertices = new PVector[4]; 

		if(newFocusDistance == -1.f) focusDistance = defaultFocusDistance;
		else focusDistance = newFocusDistance;

		gpsLocation = newGPSLocation;
		origFocusDistance = focusDistance;
		sensorSize = newSensorSize;
		brightness = newBrightness;

		theta = newTheta;              		// GPS Orientation (Yaw angle)
		phi = newElevation;            		// Pitch angle
		rotation = newRotation;             // Rotation angle
		orientation = newOrientation;       // Vertical (90) or Horizontal (0)

		focalLength = newFocalLength;
		cameraModel = newCameraModel;
		
		if(newDateTime != null)
			time = new WMV_Time( newDateTime, getID(), cluster, 0, newTimeZone );		
		else
			time = null;

		aspectRatio = getAspectRatio();
	}  

	/**
	 * Register this image for drawing by the world object (?)
	 */
	public void registerForDrawing()
	{
		
	}
	
	/**
	 * Display the image in virtual space
	 */
	public void draw(WMV_World world)
	{
		if(showMetadata) displayMetadata(world);

		float distanceBrightnessFactor; 						// Fade with distance
		float angleBrightnessFactor;							// Fade with angle

		float brightness = fadingBrightness;					
		brightness *= viewerSettings.userBrightness;
		
		distanceBrightnessFactor = getDistanceBrightness(); 
		brightness *= distanceBrightnessFactor; 						// Fade brightness based on distance to camera

		if( worldState.timeFading && time != null && !viewerState.isMoving() )
			brightness *= getTimeBrightness(); 					// Fade brightness based on time

		if( viewerSettings.angleFading )
		{
			float imageAngle = getFacingAngle(viewerState.getOrientationVector());
			angleBrightnessFactor = getAngleBrightness(imageAngle);                 // Fade out as turns sideways or gets too far / close
			brightness *= angleBrightnessFactor;
		}

		viewingBrightness = PApplet.map(brightness, 0.f, 1.f, 0.f, 255.f);				// Scale to setting for alpha range
		
		if (!hidden && !disabled) 
		{
			if (viewingBrightness > 0)
			{
				if(image.width > 0 && !viewerSettings.map3DMode)		// If image has been loaded
					displayImage(world);          // Draw the image 
			}
		} 
		else
			world.p.noFill();                  // Hide image if it isn't visible

		if(visible && worldState.showModel && !hidden && !disabled)
			displayModel(world);
	}

	private PImage applyMask(MultimediaLocator ml, PImage source, PImage mask)
	{
		PImage result = ml.createImage(640, 480, PApplet.RGB);
		
		try
		{
			result = source.copy();
			result.mask(mask); 
		}
		catch(RuntimeException ex)
		{
			if(debugSettings.image || debugSettings.main)
			{
				PApplet.println("ERROR with Blur Mask... "+ex+" horizBorderID:"+horizBorderID+" vertBorderID:"+vertBorderID);
//				PApplet.println(" mask.width:"+mask.width);
//				PApplet.println(" mask.height:"+mask.height);
//				PApplet.println(" main.imageID:"+getID());
//				PApplet.println(" main.width:"+image.width);
//				PApplet.println(" main.height:"+image.height);
			}
		}
		
		return result;
	}
	
	/**
	 * @param size Size to draw the video center
	 * Draw the video center as a colored sphere
	 */
	void displayModel(WMV_World world)
	{
		/* Draw frame */
		world.p.pushMatrix();
		
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

		world.p.pushMatrix();
		if(worldState.showMediaToCluster)
		{
			world.p.strokeWeight(3.f);
			world.p.stroke(80, 135, 255, viewingBrightness);
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
	 * Fade in image
	 */
	public void fadeIn()
	{
		if(fading || isFadingIn || isFadingOut)		// If already fading, stop at current value
			stopFading();

		fadeBrightness(1.f);					// Fade in
	}

	/**
	 * Fade out image
	 */
	public void fadeOut()
	{
		if(fading || isFadingIn || isFadingOut)		// If already fading, stop at current value
			stopFading();

		fadeBrightness(0.f);					// Fade out
	}
	
	/**
=	 * Update image geometry + visibility
	 */
	public void update(MultimediaLocator ml, WMV_Utilities utilities)
	{
		if(requested && image.width != 0)			// If requested image has loaded, initialize image 
		{
			calculateVertices();  					// Update geometry		
			
			aspectRatio = getAspectRatio();
			blurred = applyMask(ml, image, blurMask);				// Apply blur mask once image has loaded
			requested = false;
//			p.p.requestedImages--;
		}

		if(image.width > 0 && !hidden && !disabled)				// Image has been loaded and isn't hidden or disabled
		{
			boolean wasVisible = visible;
			boolean visibilitySetToTrue = false;
			boolean visibilitySetToFalse = false;

			visible = false;

			if(viewerSettings.orientationMode)								// In Transitions Only Mode, visibility is based on distance of associated cluster 
			{
				if(cluster == viewerState.getCurrentClusterID())		// If this photo's cluster is the current (closest) cluster, it is visible
					visible = true;

				for(int id : viewerState.getClustersVisible())
					if(cluster == id)			// If this photo's cluster is on next closest list, it is visible	-- CHANGE THIS??!!
						visible = true;
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
				float imageAngle = getFacingAngle(viewerState.getOrientationVector());			// Check if image is visible at current angle facing viewer

				if(!utilities.isNaN(imageAngle))
					visible = (getAngleBrightness(imageAngle) > 0.f);

				if(!fading && viewerSettings.hideImages)
					visible = false;

				if(visible && !viewerSettings.orientationMode)
					visible = (getDistanceBrightness() > 0.f);

				if(orientation != 0 && orientation != 90)          	// Hide orientations of 180 or 270 (avoid upside down images)
					visible = false;

				if(isBackFacing(viewerState.getLocation()) || isBehindCamera(viewerState.getLocation(), viewerState.getOrientationVector()))
					visible = false;
			}
			
			if(isFading())										// Update brightness while fading
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
			
			if(!viewerSettings.angleThinning)
			{
				if(visibilitySetToTrue && !fading && !fadedOut && !viewerSettings.hideImages && fadingBrightness == 0.f)			// Fade in
					fadeIn();
			}
			else
			{
				if(visible && !thinningVisibility && !fading)
				{
					fadeOut();
				}

				if(!visible && thinningVisibility && !fading && !viewerSettings.hideImages) 
				{
					if(!fadedOut)					// Fade in if didn't just finish fading out this frame
						fadeIn();
				}
			}

			if(visibilitySetToFalse)
				fadeOut();

			if(fadedOut) fadedOut = false;
		}
		else
		{
			if(viewerSettings.orientationMode)
			{
				for(int id : viewerState.getClustersVisible())
					if(cluster == id  && !requested)			// If this photo's cluster is on next closest list, it is visible	-- CHANGE THIS??!!
						loadMedia(ml);
			}
			else if(getCaptureDistance() < viewerSettings.getFarViewingDistance() && !requested)
				loadMedia(ml); 					// Request image pixels from disk
		}
		
		if(isFading())                       // Fade in and out with time
			updateFadingBrightness();
		
		if(fadingFocusDistance)
			updateFadingFocusDistance();
	}

	/** 
	 * Draw the image
	 */
	private void displayImage(WMV_World world)
	{
		world.p.noStroke(); 
		if (isSelected())     // Draw outline
		{
			if(!viewerSettings.selection && debugSettings.field)
			{
				world.p.stroke(155, 146, 255, 255);
				world.p.strokeWeight(outlineSize);
			}
		}

		world.p.rectMode(PApplet.CENTER);
		
		world.p.pushMatrix();
		world.p.beginShape(PApplet.POLYGON);    // Begin the shape containing the image
		world.p.textureMode(PApplet.NORMAL);
		
		world.p.noFill();

		if(worldState.fadeEdges)
			world.p.texture(blurred);
		else
			world.p.texture(image);        			// Apply the image to the face as a texture 

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
					world.p.tint(viewingBrightness * 0.4f, 255);          // Set the image transparency					
				else
					world.p.tint(255, viewingBrightness * 0.333f);    
			}
		}
		else
		{
			if(!worldState.alphaMode)
				world.p.tint(viewingBrightness, 255);          				
			else
			{
				world.p.tint(255, PApplet.map(viewingBrightness, 0.f, 255.f, 0.f, worldState.alpha));          				
			}
		}

		if(viewerSettings.orientationMode)
		{
			world.p.vertex(sVertices[0].x, sVertices[0].y, sVertices[0].z, 0, 0);         // UPPER LEFT      
			world.p.vertex(sVertices[1].x, sVertices[1].y, sVertices[1].z, 1, 0);         // UPPER RIGHT           
			world.p.vertex(sVertices[2].x, sVertices[2].y, sVertices[2].z, 1, 1);			// LOWER RIGHT        
			world.p.vertex(sVertices[3].x, sVertices[3].y, sVertices[3].z, 0, 1);         // LOWER LEFT
		}
		else
		{
			world.p.vertex(vertices[0].x, vertices[0].y, vertices[0].z, 0, 0);            // UPPER LEFT      
			world.p.vertex(vertices[1].x, vertices[1].y, vertices[1].z, 1, 0);            // UPPER RIGHT           
			world.p.vertex(vertices[2].x, vertices[2].y, vertices[2].z, 1, 1);			// LOWER RIGHT        
			world.p.vertex(vertices[3].x, vertices[3].y, vertices[3].z, 0, 1);            // LOWER LEFT
		}
		
		world.p.endShape(PApplet.CLOSE);       // End the shape containing the image
		world.p.popMatrix();
		
//		p.imagesSeen++;
//		p.setImagesSeen(p.getImagesSeen() + 1);
	}
	/**
	 * Calculate and return alpha value given camera to image angle
	 * @param imageAngle 
	 * @return Fading amount due to image angle
	 */
	public float getAngleBrightness(float imageAngle)
	{
		float angleBrightness = 0.f;

		if(imageAngle > viewerSettings.visibleAngle)
			angleBrightness = 0.f;
		else if (imageAngle < viewerSettings.visibleAngle * 0.66f)
			angleBrightness = 1.f;
		else
			angleBrightness = PApplet.constrain((1.f-PApplet.map(imageAngle, viewerSettings.visibleAngle * 0.66f, viewerSettings.visibleAngle, 0.f, 1.f)), 0.f, 1.f);

		return angleBrightness;
	}

	/**
	 * getViewingDistance()
	 * @return How far the image is from the camera
	 */
	public float getViewingDistance()       // Find distance from camera to point in virtual space where photo appears           
	{
		PVector camLoc = viewerState.getLocation();
		float distance;

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
		distance = PVector.dist(loc, camLoc);     

		return distance;
	}

	/** 
	 * @return Distance visibility multiplier between 0. and 1.
	 * Find image transparency due to distance (fades away in distance and as camera gets close)
	 */
	public float getDistanceBrightness()									
	{
		float viewDist = getViewingDistance();
		float farViewingDistance = viewerSettings.getFarViewingDistance();
		float nearViewingDistance = viewerSettings.getNearViewingDistance();
		
		float distVisibility = 1.f;

		if(viewDist > farViewingDistance)
		{
			float vanishingPoint = farViewingDistance + focusDistance;	// Distance where transparency reaches zero
//			float vanishingPoint = farViewingDistance + p.p.defaultFocusDistance;	// Distance where transparency reaches zero
			if(viewDist < vanishingPoint)
				distVisibility = PApplet.constrain(1.f - PApplet.map(viewDist, viewerSettings.getFarViewingDistance(), vanishingPoint, 0.f, 1.f), 0.f, 1.f);    // Fade out until cam.visibleFarDistance
			else
				distVisibility = 0.f;
		}
		else if(viewDist < nearViewingDistance)								
		{
			distVisibility = PApplet.constrain(PApplet.map(viewDist, viewerSettings.getNearClippingDistance(), viewerSettings.getNearViewingDistance(), 0.f, 1.f), 0.f, 1.f);
		}

		return distVisibility;
	}

	/**
	 * Update image geometry each frame
	 */
	public void calculateVertices()									
	{
		vertices = initializeVertices();					// Initialize Normal Mode vertices
		sVertices = initializeVertices();					// Initialize Orientation Mode (static) vertices
		
		if (phi != 0.) vertices = rotateVertices(vertices, -phi, verticalAxis);        	 // Rotate around X axis
		if (theta != 0.) vertices = rotateVertices(vertices, 360-theta, azimuthAxis);    // Rotate around Z axis
		
		if (phi != 0.) sVertices = rotateVertices(sVertices, -phi, verticalAxis);        // Rotate around X axis
		if (theta != 0.) sVertices = rotateVertices(sVertices, 360-theta, azimuthAxis);    // Rotate around Z axis
		
		if(vertices.length == 0) disabled = true;
		if(sVertices.length == 0) disabled = true;
		
		vertices = translateVertices(vertices, getCaptureLocation());               // Move image to photo capture location   
		
		disp = getDisplacementVector();
		vertices = translateVertices(vertices, disp);          // Translate image vertices from capture to viewing location
		sVertices = translateVertices(sVertices, disp);
		
		location = new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z);
		location.add(disp);     													 
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
	 * Request the image to be loaded from disk
	 */
	public void loadMedia(MultimediaLocator ml)
	{
		if( !hidden && !disabled )
		{
			calculateVertices();
			image = ml.requestImage(filePath);
			requested = true;
//			p.p.requestedImages++;
		}
	}

	/**
	 * @return How far the image location is from a point
	 */
	float getImageDistanceFrom(PVector point)       // Find distance from camera to point in virtual space where photo appears           
	{
		float distance = PVector.dist(location, point);     
		return distance;
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
			cluster = closestClusterIndex;		// Associate image with cluster
		else
			cluster = -1;						// Create a new single image cluster here!

		if(cluster != -1)
			return true;
		else
			return false;
	}

	/**
	 * Set thinning visibility of image
	 * @param state New visibility
	 */
	public void setThinningVisibility(boolean state)
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
	 * @return Whether image is facing the camera
	 */	
	public boolean isFacingCamera(PVector cameraPosition)
	{
		return PApplet.abs(getAngleToCamera(cameraPosition)) > viewerSettings.visibleAngle;     			// If the result is positive, then it is facing the camera.
	}
	
	/**
	 * @return Angle between camera location and image
	 */	
	public float getAngleToCamera(PVector cameraPosition)
	{
//		PVector cameraPosition = viewer.getLocation();
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
	 * @return Angle between the image and direction the camera is facing
	 */
	public float getFacingAngle(PVector camOrientation)
	{
		PVector faceNormal = getFaceNormal();

		PVector crossVector = new PVector();
		if(camOrientation == null) PApplet.println("camOrientation == NULL..."+getID());
		PVector.cross(camOrientation, faceNormal, crossVector);				// Cross vector gives angle between camera and image

		float result = crossVector.mag();
		return result;
	}

	/**
	 * @return Whether the camera is behind the image
	 */
	public boolean isBackFacing(PVector camLoc)										
	{
		float captureToCam = getCaptureLocation().dist(camLoc);  	// Find distance from capture location to camera
		float camToImage = location.dist(camLoc);  					// Find distance from camera to image

//		if(captureToCam > camToImage + p.p.viewer.getNearClippingDistance())								// If captureToCam > camToPhoto, then back of the image is facing the camera
		if(captureToCam > camToImage + viewerSettings.getNearClippingDistance() / 2.f)			// If captureToCam > camToVideo, then back of video is facing the camera
			return true;
		else
			return false; 
	}

	/**
	 * @return Whether image is behind camera
	 */
	public boolean isBehindCamera(PVector camLocation, PVector camOrientation)										
	{
		PVector centerVertex = calcCenterVertex();

		PVector camToImage = new PVector(  camLocation.x-centerVertex.x, 	//  Vector from the camera to the face.      
				camLocation.y-centerVertex.y, 
				camLocation.z-centerVertex.z   );
		
		camToImage.normalize();
		
		float result = PVector.dot(camOrientation, camToImage);				// Dot product gives angle between camera and image
		
		if(result >= 0)							// If > zero, image is behind camera
			return true;
		else
			return false; 						// If < zero, image is in front of camera
	}

	/**
	 * @return Center vertex of image rectangle
	 */
	private PVector calcCenterVertex()
	{
		PVector vertex1 = new PVector(0,0,0);
		PVector vertex2 = new PVector(0,0,0);
		PVector diff = new PVector(0,0,0);
		PVector result = new PVector(0,0,0);

		// If iPhone image:
		if(cameraModel == 1)
		{
			if (orientation == 90)  // Vertical Image
			{
				vertex1 = vertices[1];
				vertex2 = vertices[3];
			}
			else if (orientation == 0)    // Horizontal Image
			{
				vertex1 = vertices[2];
				vertex2 = vertices[0];
			}
			else if (orientation == 180)    // Upside Down (Horizontal) Image
			{
				vertex1 = vertices[0];
				vertex2 = vertices[2];
			}
			else if (orientation == 270)    // Upside Down (Vertical) Image
			{
				vertex1 = vertices[3];
				vertex2 = vertices[1];
			}
			diff = PVector.sub(vertex1, vertex2);
			diff.mult(0.5f);
			result = PVector.add(vertex2, diff);
		}
		else
		{
			diff = PVector.sub(vertices[2], vertices[0]);
			diff.mult(0.5f);
			result = PVector.add(vertices[0], diff);
		} 

		return result;
	}

	/**
	 * @return Normalized vector perpendicular to the image plane
	 */
	private PVector getFaceNormal()
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
	 * Draw the image metadata in Heads-Up Display
	 */
	public void displayMetadata(WMV_World world)
	{
		String strTitleImage = "Image";
		String strTitleImage2 = "-----";
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
		String strFocusDistance = "Focus Distance: "+PApplet.str(focusDistance);

		String strTitleDebug = "--- Debugging ---";
		String strBrightness = "brightness: "+PApplet.str(viewingBrightness);
		String strBrightnessFading = "brightnessFadingValue: "+PApplet.str(fadingBrightness);
		
		world.p.display.metadata(world, strTitleImage);
		world.p.display.metadata(world, strTitleImage2);
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
		world.p.display.metadata(world, strFocusDistance);

		if(debugSettings.image)
		{
			world.p.display.metadata(world, strTitleDebug);
			world.p.display.metadata(world, strBrightness);
			world.p.display.metadata(world, strBrightnessFading);
		}
	}

	/**
	 * @return Average color across all pixels  
	 */	
	PVector getAverageColor() 
	{
		image.loadPixels();
		int r = 0, g = 0, b = 0;
		for (int i=0; i<image.pixels.length; i++) 
		{
			int c = image.pixels[i];
			r += c>>16&0xFF;
		g += c>>8&0xFF;
			b += c&0xFF;
		}
		r /= image.pixels.length;
		g /= image.pixels.length;
		b /= image.pixels.length;
		return new PVector(r, g, b);
	}

//	/**
//	 * @return Average brightness across all pixels
//	 */		
//	private float getAverageBrightness() 
//	{
//		image.loadPixels();
//		int b = 0;
//		for (int i=0; i<image.pixels.length; i++) {
//			float cur = p.p.p.brightness(image.pixels[i]);
//			b += cur;
//		}
//		b /= image.pixels.length;
//		return b;
//	}

	/**
	 * Associate this image with given video ID  
	 * @param videoID 
	 */	
	public void associateVideo(int videoID)
	{
		isVideoPlaceHolder = true;
		assocVideoID = videoID;
		hidden = true;
//		if(debugSettings.video) PApplet.println("Image "+getID()+" is now associated with video "+videoID);
	}

	/**
	 * @return Aspect ratio of the image
	 */
	public float getAspectRatio()
	{
		float ratio = 0;

//		ratio = (float)(image.height)/(float)(image.width);
		ratio = (float)imageHeight / (float)imageWidth;
//		if (ratio > 1.)
//			ratio = 0.666f;

		return ratio;
	}

	/**
	 * Fade focus distance to given target while rescaling images 
	 * @param target New focus distance
	 */
	public void fadeFocusDistance(float target)
	{
		fadingFocusDistance = true;
		fadingFocusDistanceStartFrame = worldState.frameCount;					
		fadingFocusDistanceEndFrame = worldState.frameCount + fadingFocusDistanceLength;	
		fadingFocusDistanceStart = focusDistance;
		fadingFocusDistanceTarget = target;
	}
	
	/**
	 * Update fading of object distance (focus distance and image size together)
	 */
	private void updateFadingFocusDistance()
	{
		float newFocusDistance = 0.f;

		if (worldState.frameCount >= fadingFocusDistanceEndFrame)
		{
			fadingFocusDistance = false;
			newFocusDistance = fadingFocusDistanceTarget;
		} 
		else
		{
			newFocusDistance = PApplet.map( worldState.frameCount, fadingFocusDistanceStartFrame, fadingFocusDistanceEndFrame, 
											fadingFocusDistanceStart, fadingFocusDistanceTarget);      // Fade with distance from current time
		}

		setFocusDistance( newFocusDistance );	// Set focus distance
		calculateVertices();  					// Update vertices given new width
	}
	
	void resetFocusDistance()
	{
		setFocusDistance(origFocusDistance);
	}
	
	private PVector[] initializeVertices()
	{
		float width = calculateImageWidth();										
		float height = width * aspectRatio;		

		float left = -width * 0.5f;						
		float right = width * 0.5f;
		float top = -height * 0.5f;
		float bottom = height * 0.5f;

		PVector[] verts = new PVector[4]; 

		if(cameraModel == 1)      			// If it is an iPhone Image
		{
			if (orientation == 90) 		 	// Vertical Image
			{
				verts[0] = new PVector( left, top, 0 );     			// UPPER LEFT  
				verts[1] = new PVector( right, top, 0 );      		// UPPER RIGHT 
				verts[2] = new PVector( right, bottom, 0 );       	// LOWER RIGHT
				verts[3] = new PVector( left, bottom, 0 );      		// LOWER LEFT
			}
			else if (orientation == 0)    	// Horizontal Image
			{
				verts[0] = new PVector( left, top, 0 );     			// UPPER LEFT  
				verts[1] = new PVector( right, top, 0 );      		// UPPER RIGHT 
				verts[2] = new PVector( right, bottom, 0 );       	// LOWER RIGHT
				verts[3] = new PVector( left, bottom, 0 );      		// LOWER LEFT
			}
			else if (orientation == 180)    // Upside Down (Horizontal) Image
			{
				verts[0] = new PVector( right, bottom, 0 );       	// LOWER RIGHT
				verts[1] = new PVector( left, bottom, 0 );      		// LOWER LEFT
				verts[2] = new PVector( left, top, 0 );     			// UPPER LEFT  
				verts[3] = new PVector( right, top, 0 );      		// UPPER RIGHT 
			}
			else  if (orientation == 270)    // Upside Down (Vertical) Image
			{
				verts[0] = new PVector( right, bottom, 0 );       	// LOWER RIGHT
				verts[1] = new PVector( left, bottom, 0 );      		// LOWER LEFT
				verts[2] = new PVector( left, top, 0 );     			// UPPER LEFT  
				verts[3] = new PVector( right, top, 0 );      		// UPPER RIGHT 
			}
		}
		else
		{
			if (orientation == 90 || orientation == 0)  				// Vertical or Horizontal Right-Side-Up Image
			{
				if (orientation == 90)
				{
					imageWidth = image.height;
					imageHeight = image.width;
				}

				verts[0] = new PVector( left, top, 0 );     			// UPPER LEFT  
				verts[1] = new PVector( right, top, 0 );      		// UPPER RIGHT 
				verts[2] = new PVector( right, bottom, 0 );       	// LOWER RIGHT
				verts[3] = new PVector( left, bottom, 0 );      		// LOWER LEFT
			}
			else if (orientation == 180 || orientation == 270)    		// Upside Down (Horizontal or Vertical) Image
			{
				if (orientation == 270 )
				{
					imageWidth = image.height;
					imageHeight = image.width;
				}

				verts[0] = new PVector( right, bottom, 0 );       	// LOWER RIGHT
				verts[1] = new PVector( left, bottom, 0 );      		// LOWER LEFT
				verts[2] = new PVector( left, top, 0 );    			// UPPER LEFT  
				verts[3] = new PVector( right, top, 0 );      		// UPPER RIGHT
			}
		}
		
		return verts;
	}
	
	/**
	 * Find image width from formula:
	 * Image Width (m.) = Object Width on Sensor (mm.) / Focal Length (mm.) * Focus Distance (m.) 
	 * @return Image width in simulation (m.)
	 */
	private float calculateImageWidth()
	{
//		float subjectSizeRatio = subjectPixelWidth / originalImageWidth;		// --More accurate

		float objectWidthOnSensor = sensorSize * subjectSizeRatio;			// 29 * 0.18 == 5.22
		float imgWidth = objectWidthOnSensor * focusDistance / focalLength;		// 5.22 * 9 / 4.2 == 11.19	Actual: 11.320482

		return imgWidth;
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

	 public void setBlurMaskID()
	 {
		 // horizBorderID    0: Left  1: Center  2: Right  3: Left+Right
		 // vertBorderID	 0: Top  1: Center  2: Bottom  3: Top+Bottom
		 if(horizBorderID == 0)
		 {
			 switch(vertBorderID)
			 {
			 case 0:
//				 blurMask = p.p.blurMaskLeftTop;
				 blurMaskID = 0;
				 break;
			 case 1:
//				 blurMask = p.p.blurMaskLeftCenter;
				 blurMaskID = 1;
				 break;
			 case 2:
//				 blurMask = p.p.blurMaskLeftBottom;
				 blurMaskID = 2;
				 break;
			 case 3:
			 default:
//				 blurMask = p.p.blurMaskLeftBoth;
				 blurMaskID = 3;
				 break;
			 }
		 }
		 else if(horizBorderID == 1)
		 {
			 switch(vertBorderID)
			 {
			 case 0:
//				 blurMask = p.p.blurMaskCenterTop;
				 blurMaskID = 4;
				 break;
			 case 1:
//				 blurMask = p.p.blurMaskCenterCenter;
				 blurMaskID = 5;
				 break;
			 case 2:
//				 blurMask = p.p.blurMaskCenterBottom;
				 blurMaskID = 6;
				 break;
			 case 3:
			 default:
//				 blurMask = p.p.blurMaskCenterBoth;
				 blurMaskID = 7;
				 break;
			 }
		 }
		 else if(horizBorderID == 2)
		 {
			 switch(vertBorderID)
			 {
			 case 0:
//				 blurMask = p.p.blurMaskRightTop;
				 blurMaskID = 8;
				 break;
			 case 1:
//				 blurMask = p.p.blurMaskRightCenter;
				 blurMaskID = 9;
				 break;
			 case 2:
//				 blurMask = p.p.blurMaskRightBottom;
				 blurMaskID = 10;
				 break;
			 case 3:
			 default:
//				 blurMask = p.p.blurMaskRightBoth;
				 blurMaskID = 11;
				 break;
			 }
		 }
		 else if(horizBorderID == 3)
		 {
			 switch(vertBorderID)
			 {
			 case 0:
//				 blurMask = p.p.blurMaskBothTop;
				 blurMaskID = 12;
				 break;
			 case 1:
//				 blurMask = p.p.blurMaskBothCenter;
				 blurMaskID = 13;
				 break;
			 case 2:
//				 blurMask = p.p.blurMaskBothBottom;
				 blurMaskID = 14;
				 break;
			 case 3:
			 default:
//				 blurMask = p.p.blurMaskBothBoth;
				 blurMaskID = 15;
				 break;
			 }
		 }
	 }
	 
	 public void setBlurMask(PImage newBlurMask)
	 {
		 blurMask = newBlurMask;
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
		 return imageWidth;
	 }

	 public float getHeight()
	 {
		 return imageHeight;
	 }

	 public int getAssociatedVideo()
	 {
		 if(isVideoPlaceHolder)
		 {
			 return assocVideoID;
		 }
		 else return -1;
	 }

	 public boolean isVideoPlaceHolder()
	 {
		 return isVideoPlaceHolder;
	 }

	 public float getElevation()
	 {
		 return phi;
	 }

	 public float getOrientation()
	 {
		 return orientation;
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
	 
	 public void setHorizBorderID(int newHorizBorderID)
	 {
		 horizBorderID = newHorizBorderID;
	 }

	 public void setVertBorderID(int newVertBorderID)
	 {
		 vertBorderID = newVertBorderID;
	 }
	 
//	 private PImage getDesaturated(PImage in, float amt) 
//	 {
//		 PImage out = in.get();
//		 for (int i = 0; i < out.pixels.length; i++) {
//			 int c = out.pixels[i];
//			 float h = p.p.p.hue(c);
//			 float s = p.p.p.saturation(c) * amt;
//			 float b = p.p.p.brightness(c);
//			 out.pixels[i] = p.p.p.color(h, s, b);
//		 }
//		 return out;
//	 }
//
//	 private PImage getFaintImage(PImage image, float amt) 
//	 {
//		 PImage out = image.get();
//		 for (int i = 0; i < out.pixels.length; i++) {
//			 int c = out.pixels[i];
//			 float h = p.p.p.hue(c);
//			 float s = p.p.p.saturation(c) * amt;
//			 float b = p.p.p.brightness(c) * amt;
//			 out.pixels[i] = p.p.p.color(h, s, b);
//		 }
//		 return out;
//	 }
}
