package gmViewer;

import java.util.Calendar;

import processing.core.*;

/************************************
 * GMV_Image
 * @author davidgordon
 * A rectangular image in virtual space
 */

class GMV_Image extends GMV_Viewable						 
{
	/* Graphics */
	public PImage image;					// Image pixels
	private PVector[] vertices;				// Vertex list

	private boolean blur = false;
	private PImage blurMask;
	private float outlineSize = 10.f;		// Size of the outline around a selected image

	private PVector disp = new PVector(0, 0, 0);   // Displacement from capture location
	private float fadingObjectDistanceStartFrame = 0.f, fadingObjectDistanceEndFrame = 0.f;	// Fade focus distance and image size together
	private float fadingFocusDistanceStart = 0.f, fadingFocusDistanceTarget = 0.f;
	private float fadingImageSizeFactorStart = 0.f, fadingImageSizeFactorTarget = 0.f;	
	private float fadingObjectDistanceLength = 30.f;
	
	private boolean thinningVisibility = false;

	/* Metadata */
	private float imageWidth, imageHeight;		// Image width and height
	private float phi;			        // Image Elevation (in Degrees N)
	private float orientation;              // Landscape = 0, Portrait = 90, Upside Down Landscape = 180, Upside Down Portrait = 270
	private float rotation;				    // Elevation angle and Z-axis rotation
	private float focalLength = 0; 			// Zoom Level 
	private float focusDistance; 	 		// Image viewing distance (or estimated object distance, if given in metadata)
	private float sensorSize;				// Approx. size of sensor in mm.
	
	/* Video Association */
	private boolean isVideoPlaceHolder = false;
	private int assocVideoID = -1;

//	private PVector averageColor;
//	private float averageBrightness;

	GMV_Field p;					// Parent field

	GMV_Image ( GMV_Field parent, int newID, String newName, String newFilePath, PVector newGPSLocation, float newTheta, float newFocalLength, 
			float newOrientation, float newElevation, float newRotation, float newFocusDistance, float newSensorSize, int newCameraModel, 
			int newWidth, int newHeight, float newBrightness, Calendar newCalendar)
	{
		super(parent, newID, newName, newFilePath, newGPSLocation, newTheta, newCameraModel, newBrightness, newCalendar);

		p = parent;
//		name = newName;		
		filePath = newFilePath;

		image = p.p.createImage(0, 0, processing.core.PConstants.RGB);		// Create empty image
		imageWidth = newWidth;
		imageHeight = newHeight;
		
		vertices = new PVector[4]; 

		gpsLocation = newGPSLocation;
		focusDistance = newFocusDistance;
		sensorSize = newSensorSize;
		
		theta = newTheta;              		// GPS Orientation (Yaw angle)
		phi = newElevation;            		// Pitch angle
		rotation = newRotation;             // Rotation angle
		orientation = newOrientation;       // Vertical (90) or Horizontal (0)

		focalLength = newFocalLength;
		cameraModel = newCameraModel;
		
//		blurMask = p.p.createImage(image.width, image.height, processing.core.PConstants.RGB);
		blur = p.p.blurEdges;
		blurMask = p.p.blurMask;
//		PApplet.println("Set blurMask: width:"+blurMask.width+" height:"+blurMask.height);
	}  

	/**
	 * draw()
	 * Display the image or spherical panorama in virtual space
	 */
	public void draw()
	{
		if(!verticesAreNull())
		{
			float distanceBrightnessFactor; 						// Fade with distance
			float timeBrightnessFactor;                        		// Fade with time 
			float angleBrightnessFactor;							// Fade with angle

			float brightness = fadingBrightness;					

			distanceBrightnessFactor = getDistanceBrightness(); 
			brightness *= distanceBrightnessFactor; 						// Fade brightness based on distance to camera
			
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
							p.p.display.message("Cluster: "+cluster+" has no timeline points!");
						}
						
						brightness *= timeBrightnessFactor; 					// Fade brightness based on time
					}
					else
					{
						if(p.p.viewer.currentCluster == cluster)
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

			if( p.p.angleFading )
			{
				float imageAngle = getFacingAngle();
				if(p.p.utilities.isNaN(imageAngle))
				{
					imageAngle = 0;				
					visible = false;
					disabled = true;
				}

				angleBrightnessFactor = getAngleBrightness(imageAngle);                 // Fade out as turns sideways or gets too far / close
				brightness *= angleBrightnessFactor;
			}

			viewingBrightness = PApplet.map(brightness, 0.f, 1.f, 0.f, 255.f);				// Scale to setting for alpha range

			if (visible && !hidden && !disabled) 
			{
				if (viewingBrightness > 0)
				{
					if(image.width > 0 && !p.p.viewer.mapMode)		// If image has been loaded
					{
						drawImage();          // Draw the image 
					}
				}
			} 
			else
			{      
				p.p.noFill();                  // Hide image if it isn't visible
			}

			if (visible && isSelected() && !disabled && p.p.debug.model)		// Draw image locations for debugging or map display
				drawLocation(centerSize);
			if (visible && !disabled && p.p.viewer.mapMode)
				drawLocation(centerSize);
		}
	}

	void applyMask(PImage mask)
	{
		try
		{
			image.mask(mask); 
		}
		catch(RuntimeException ex)
		{
			if(p.p.debug.image){
				PApplet.println("Blur Mask Error:"+ex);
				PApplet.println("mask.width:"+mask.width);
				PApplet.println("mask.height:"+mask.height);
				PApplet.println("image.imageID:"+getID());
				PApplet.println("image.width:"+image.width);
				PApplet.println("image.height:"+image.height);
			}
			//	      PApplet.println("orientation:"+(int)(orientation));
		}
	}

//	PImage createMask(PImage mask) // Must be same size
//	{
//	   PImage result = p.p.createImage(mask.width, mask.height, PApplet.RGB);
//	   mask.loadPixels();
//
//	   for(int i=0; i<result.pixels.length; i++)
//	   {
//	     float brightness;
//	     p.p.colorMode(PApplet.HSB);
//	     brightness = p.p.brightness(mask.pixels[i]);
//	     p.p.colorMode(PApplet.RGB);
//	     result.pixels[i] = p.p.color(0,0,PApplet.constrain(brightness, 0, 0.85f*255.f));    // Take the brightness of combined pixels and put into blue channel   
//	   }
//	   result.updatePixels();
//	   p.p.colorMode(PApplet.HSB);
//
//	   return result;
//	}
	
	/**
	 * fadeIn()
	 * Fade in image
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
	 * Fade out image
	 */
	public void fadeOut()
	{
		if(fading || isFadingIn || isFadingOut)		// If already fading, stop at current value
			if(!initFading)			
				stopFading();

		fadeBrightness(0.f);					// Fade out
	}

	/**
	 * update()
=	 * Update main variables 
	 */
	public void update()
	{
		if(requested && image.width != 0)			// If requested image has loaded, initialize image 
		{
			calculateVertices();  					// Update geometry

			aspectRatio = getAspectRatio();
//			averageColor = getAverageColor();
//			averageBrightness = getAverageBrightness();
			
			if(blur)
			{
//				blurMask = createMask(blurMask);
				applyMask(blurMask);				// Apply blur mask once image has loaded
			}

			requested = false;
			p.p.requestedImages--;
		}

		if(image.width > 0 && !disabled)			
		{
			visible = getAngleVisibility();						// Check if the image is currently visible
			
			if(p.p.debug.hideImages)
				visible = false;
			
			if(visible)
				visible = (getDistanceBrightness() > 0.f);

			if(orientation != 0 && orientation != 90)          	// Hide orientations of 180 or 270 (avoid upside down images)
				visible = false;

			if(isBackFacing() || isBehindCamera())
				visible = false;

			if(visible && !fading && !fadedOut)					// Fade in
				fadeIn();

			if(fadedOut) fadedOut = false;
		}
		else if(getCaptureDistance() < p.p.viewer.getFarViewingDistance() && !requested)
		{
			loadMedia(); 
		}

		if(isFading())                       // Fade in and out with time
		{
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
	}

	/** 
	 * Draw the image
	 */
	private void drawImage()
	{
		p.p.noStroke(); 
		p.p.rectMode(PApplet.CENTER);

		if (isSelected())     // Draw outline
		{
			if(!p.p.viewer.selectionMode && p.p.debug.field)
			{
				p.p.stroke(155, 146, 255, 255);
				p.p.strokeWeight(outlineSize);
			}
		}

		p.p.pushMatrix();
		p.p.beginShape(PApplet.POLYGON);    // Begin the shape containing the image
		p.p.textureMode(PApplet.NORMAL);
		p.p.texture(image);        			// Apply the image to the face as a texture 

		if(p.p.viewer.selectionMode)
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
					p.p.tint(viewingBrightness * 0.4f, 255);          // Set the image transparency					
				else
					p.p.tint(255, viewingBrightness * 0.333f);          				
			}
		}
		else if(p.p.viewer.videoMode)
		{
			if(!p.p.alphaMode)
				p.p.tint(viewingBrightness * 0.66f, 255);          // Set the image transparency					
			else
				p.p.tint(255, viewingBrightness * 0.333f);          				
		}
		else
		{
			if(!p.p.alphaMode)
				p.p.tint(viewingBrightness, 255);          				
			else
				p.p.tint(255, viewingBrightness);          				
		}

		p.p.vertex(vertices[0].x, vertices[0].y, vertices[0].z, 0, 0);            // UPPER LEFT      
		p.p.vertex(vertices[1].x, vertices[1].y, vertices[1].z, 1, 0);            // UPPER RIGHT           
		p.p.vertex(vertices[2].x, vertices[2].y, vertices[2].z, 1, 1);			// LOWER RIGHT        
		p.p.vertex(vertices[3].x, vertices[3].y, vertices[3].z, 0, 1);            // LOWER LEFT

		p.p.endShape(PApplet.CLOSE);       // End the shape containing the image
		p.p.popMatrix();


		p.imagesSeen++;
	}

	/**
	 * getAngleBrightness()
	 * Calculate and return alpha value given camera to image angle
	 * @param imageAngle 
	 * @return Fading amount due to image angle
	 */
	public float getAngleBrightness(float imageAngle)
	{
		float angleBrightness = 0.f;

		if(imageAngle > p.p.visibleAngle)
		{
			angleBrightness = 0.f;
		}
		else
		{
			angleBrightness = PApplet.constrain((float)p.p.angleFadeMap.getMappedValueFor(1.-PApplet.map(imageAngle, 0.f, p.p.visibleAngle, 0.f, 1.f)), 0.f, 1.f);
		}

		return angleBrightness;
	}

	/**
	 * outline()
	 * Draw outline around selected image
	 */
	void outline()
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
	 * getViewingDistance()
	 * @return How far the image is from the camera
	 */
	public float getViewingDistance()       // Find distance from camera to point in virtual space where photo appears           
	{
		PVector camLoc;

		if(p.p.transitionsOnly)
			camLoc = p.p.viewer.location;
		else
			camLoc = p.p.viewer.location;

		float distance;

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
		distance = PVector.dist(loc, camLoc);     

		return distance;
	}

	/** 
	 * getDistanceBrightness()
	 * @return Distance visibility multiplier between 0. and 1.
	 * Find image transparency due to distance (fades away in distance and as camera gets close)
	 */
	public float getDistanceBrightness()									
	{
		float viewDist = getViewingDistance();
		float farViewingDistance = p.p.viewer.getFarViewingDistance();
		float nearViewingDistance = p.p.viewer.getNearViewingDistance();
		
		float distVisibility = 1.f;

		if(viewDist > farViewingDistance)
		{
			float vanishingPoint = farViewingDistance + p.p.defaultFocusDistance;	// Distance where transparency reaches zero
			if(viewDist < vanishingPoint)
				distVisibility = PApplet.constrain(1.f - PApplet.map(viewDist, p.p.viewer.getFarViewingDistance(), vanishingPoint, 0.f, 1.f), 0.f, 1.f);    // Fade out until cam.visibleFarDistance
			else
				distVisibility = 0.f;
		}
		else if(viewDist < nearViewingDistance)								
		{
			distVisibility = PApplet.constrain(PApplet.map(viewDist, p.p.viewer.getNearClippingDistance(), p.p.viewer.getNearViewingDistance(), 0.f, 1.f), 0.f, 1.f);
//			if(isSelected())
//				PApplet.println("ID:"+getID()+" dist:"+viewDist+" distVisibility:"+distVisibility+" near:"+p.p.viewer.getNearClippingDistance()+" far:"+p.p.viewer.getNearViewingDistance());
		}

		return distVisibility;
	}

	/**
	 * calculateVertices()
	 * Update image geometry each frame
	 */
	public void calculateVertices()									
	{
		initializeVertices();					// Initialize vertices

		if (phi != 0.)
			vertices = rotateVerts(vertices, -phi, verticalAxis);         // Rotate around X axis

		vertices = rotateVerts(vertices, 360-theta, azimuthAxis);          // Rotate around Z axis

		if(vertices.length == 0) disabled = true;
		if(!p.p.transitionsOnly)	vertices = translateVerts(vertices, getCaptureLocation());                       // Move image to photo capture location   

		float r;				  				 // Viewing sphere radius
		if(focusDistance == -1.f)
			r = p.p.defaultFocusDistance;		 // Use default if no focus distance in metadata					      
		else
			r = focusDistance;							

		float xDisp = r * PApplet.sin(PApplet.radians(360-theta)) * PApplet.sin(PApplet.radians(90-phi)); 
		float zDisp = r * PApplet.cos(PApplet.radians(360-theta)) * PApplet.sin(PApplet.radians(90-phi));  
		float yDisp = r * PApplet.cos(PApplet.radians(90-phi)); 

		disp = new PVector(-xDisp, -yDisp, -zDisp);			// Displacement from capture location

		vertices = translateVerts(vertices, disp);          // Translate image vertices from capture to viewing location

		if(p.p.transitionsOnly)
			location = new PVector (0, 0, 0);													// Location in Transition Mode
		else
			location = new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z);	// Location in Path Mode

		location.add(disp);     													 

		if (p.p.utilities.isNaN(location.x) || p.p.utilities.isNaN(location.x) || p.p.utilities.isNaN(location.x))
		{
			location = new PVector (0, 0, 0);
		}
	}

	/**
	 * loadMedia()
	 * Request the image to be loaded from disk
	 */
	public void loadMedia()
	{
		if(!p.p.debug.lowMemory)			// Check enough memory available
		{
			if( p.imagesVisible < p.maxVisiblePhotos && !disabled)
			{
				calculateVertices();
				image = p.p.requestImage(filePath);
				requested = true;
				p.p.requestedImages++;
			}
		}
	}

	/**
	 * getImageDistanceFrom()
	 * @return How far the image location is from a point
	 */
	float getImageDistanceFrom(PVector point)       // Find distance from camera to point in virtual space where photo appears           
	{
		float distance = PVector.dist(location, point);     
		return distance;
	}
	
	 /**
	  * findAssociatedCluster()
	  * @return Whether associated cluster was successfully found
	  * Set nearest cluster to the capture location to be the associated cluster
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
		 {
			 cluster = closestClusterIndex;
		 }
		 else
		 {
			 cluster = -1;						// Create a new single image cluster here!
			 p.disassociatedImages++;
		 }
		 
		 if(cluster != -1)
			 return true;
		 else
			 return false;
	 }

	/**
	 * getAngleVisibility()
	 * Check whether image is at an angle where it should currently be visible
	 */
	private boolean getAngleVisibility()				 // Check if image should be visible
	{
		boolean visible = false;

		if(p.p.transitionsOnly)							// In Transitions Only Mode, visibility is based on distance of associated cluster 
		{
			if(cluster == p.p.viewer.currentCluster)		// If this photo's cluster is the current (closest) cluster, it is visible
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
	 * isFacingCamera()
	 * @return Whether image is facing the camera
	 */	
	public boolean isFacingCamera()
	{
		return PApplet.abs(getAngleToCamera()) > p.p.visibleAngle;     			// If the result is positive, then it is facing the camera.
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
	 * @return Angle between the image and direction the camera is facing
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
	 * @return Is the camera behind the image?  
	 */
	public boolean isBackFacing()										
	{
		PVector camLoc = p.p.viewer.getLocation();

		float captureToCam = getCaptureLocation().dist(camLoc);  	// Find distance from capture location to camera
		float camToImage = location.dist(camLoc);  					// Find distance from camera to image

		if(captureToCam > camToImage + p.p.viewer.getNearClippingDistance())								// If captureToCam > camToPhoto, then back of the image is facing the camera
			return true;
		else
			return false; 
	}

	/**
	 * Is the image behind the camera?  
	 * @return Whether image is behind camera
	 */
	public boolean isBehindCamera()										
	{
		PVector camLocation = p.p.viewer.getLocation();
		PVector camOrientation = p.p.viewer.getOrientationVector();
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
	 * getFaceNormal()
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
	 * displayMetadata()
	 * Draw the image metadata in Heads-Up Display
	 */
	public void displayMetadata()
	{
		String strTitleImage = "Image";
		String strTitleImage2 = "-----";
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
		String strFocusDistance = "Focus Distance: "+PApplet.str(focusDistance);

		String strTitleDebug = "--- Debugging ---";
		String strBrightness = "brightness: "+PApplet.str(viewingBrightness);
		String strBrightnessFading = "brightnessFadingValue: "+PApplet.str(fadingBrightness);
		
		p.p.display.metadata(strTitleImage);
		p.p.display.metadata(strTitleImage2);
		p.p.display.metadata("");
		p.p.display.metadata(strID);
		p.p.display.metadata(strCluster);
		p.p.display.metadata(strName);
		p.p.display.metadata(strX + strY + strZ);

		p.p.display.metadata(strLatitude + strLongitude);
		p.p.display.metadata(strAltitude);
		p.p.display.metadata(strTheta);
		p.p.display.metadata(strElevation);
		p.p.display.metadata(strRotation);
		p.p.display.metadata(strFocusDistance);

		if(p.p.debug.image)
		{
			p.p.display.metadata(strTitleDebug);
			p.p.display.metadata(strBrightness);
			p.p.display.metadata(strBrightnessFading);
		}
	}

	/**
	 * getAverageColor()
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

	/**
	 * getAverageBrightness()
	 * @return Average brightness across all pixels
	 */		
	private float getAverageBrightness() 
	{
		image.loadPixels();
		int b = 0;
		for (int i=0; i<image.pixels.length; i++) {
			float cur = p.p.brightness(image.pixels[i]);
			b += cur;
		}
		b /= image.pixels.length;
		return b;
	}

	/**
	 * associateVideo()
	 * Associate this image with given video ID  
	 * @param videoID 
	 */	
	public void associateVideo(int videoID)
	{
		isVideoPlaceHolder = true;
		assocVideoID = videoID;
		hidden = true;
		if(p.p.debug.video)
			PApplet.println("Image "+getID()+" is now associated with video "+videoID);
	}

	/**
	 * getAspectRatio()
	 * @return Aspect ratio of the image
	 */
	public float getAspectRatio()
	{
		float ratio = 0;

		ratio = (float)(image.height)/(float)(image.width);

		if (ratio > 1.)
			ratio = 0.666f;

		return ratio;
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
	 * Setup image rectangle geometry 
	 */
	private void initializeVertices()
	{
//		float width = p.p.defaultImageSize;								// CHECK THIS			
//		float height = p.p.defaultImageSize * aspectRatio;		

		float width = calculateImageWidth();								// CHECK THIS			
		float height = width * aspectRatio;		

		float left = -width * 0.5f;
		float right = width * 0.5f;
		float top = -height * 0.5f;
		float bottom = height * 0.5f;

		vertices = new PVector[4]; 

		if(cameraModel == 1)      			// If it is an iPhone Image
		{
			if (orientation == 90) 		 	// Vertical Image
			{
				vertices[0] = new PVector( left, top, 0 );     			// UPPER LEFT  
				vertices[1] = new PVector( right, top, 0 );      		// UPPER RIGHT 
				vertices[2] = new PVector( right, bottom, 0 );       	// LOWER RIGHT
				vertices[3] = new PVector( left, bottom, 0 );      		// LOWER LEFT
			}
			else if (orientation == 0)    	// Horizontal Image
			{
				vertices[0] = new PVector( left, top, 0 );     			// UPPER LEFT  
				vertices[1] = new PVector( right, top, 0 );      		// UPPER RIGHT 
				vertices[2] = new PVector( right, bottom, 0 );       	// LOWER RIGHT
				vertices[3] = new PVector( left, bottom, 0 );      		// LOWER LEFT
			}
			else if (orientation == 180)    // Upside Down (Horizontal) Image
			{
				vertices[0] = new PVector( right, bottom, 0 );       	// LOWER RIGHT
				vertices[1] = new PVector( left, bottom, 0 );      		// LOWER LEFT
				vertices[2] = new PVector( left, top, 0 );     			// UPPER LEFT  
				vertices[3] = new PVector( right, top, 0 );      		// UPPER RIGHT 
			}
			else  if (orientation == 270)    // Upside Down (Vertical) Image
			{
				vertices[0] = new PVector( right, bottom, 0 );       	// LOWER RIGHT
				vertices[1] = new PVector( left, bottom, 0 );      		// LOWER LEFT
				vertices[2] = new PVector( left, top, 0 );     			// UPPER LEFT  
				vertices[3] = new PVector( right, top, 0 );      		// UPPER RIGHT 
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

				vertices[0] = new PVector( left, top, 0 );     			// UPPER LEFT  
				vertices[1] = new PVector( right, top, 0 );      		// UPPER RIGHT 
				vertices[2] = new PVector( right, bottom, 0 );       	// LOWER RIGHT
				vertices[3] = new PVector( left, bottom, 0 );      		// LOWER LEFT
			}
			else if (orientation == 180 || orientation == 270)    		// Upside Down (Horizontal or Vertical) Image
			{
				if (orientation == 270 )
				{
					imageWidth = image.height;
					imageHeight = image.width;
				}

				vertices[0] = new PVector( right, bottom, 0 );       	// LOWER RIGHT
				vertices[1] = new PVector( left, bottom, 0 );      		// LOWER LEFT
				vertices[2] = new PVector( left, top, 0 );    			// UPPER LEFT  
				vertices[3] = new PVector( right, top, 0 );      		// UPPER RIGHT
			}
		}
	}

	/**
	 * Find image width from formula:
	 * Image Width (m.) = Object Width on Sensor (mm.) / Focal Length (mm.) * Focus Distance (m.) 
	 * @return Image width in simulation (m.)
	 */
	private float calculateImageWidth()
	{
//		float subjectSizeRatio = subjectPixelWidth / originalImageWidth;		// --More accurate
		float objectWidthOnSensor = sensorSize * p.p.subjectSizeRatio;
		return objectWidthOnSensor * focusDistance / focalLength;
	}


	/**
	 * rotateVerts()
	 * Rotate list of vertices using matrices
	 * @param verts Vertices list
	 * @param angle Angle to rotate by
	 * @param axis Axis to rotate around
	 * @return Rotated vertices
	 */
	public PVector[] rotateVerts(PVector[] verts, float angle, PVector axis) 
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
	  * translateVerts()
	  * Translate list of vertices using matrices
	  * @param verts Vertices list
	  * @param dest Destination vector
	  * @return Translated vertices 
	  */
	 private PVector[] translateVerts(PVector[] verts, PVector dest) // Translate vertices to a designated point
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
	  * lineToCaptureLocation()
	  * Draw image capture location for debugging or map display
	  */
	 public void lineToCaptureLocation()
	 {
		 PVector centerVertex = calcCenterVertex();
		 p.p.stroke(150, 150, 255, 255);
		 p.p.strokeWeight(2);
		 p.p.line(location.x, location.y, location.z, centerVertex.x, centerVertex.y, centerVertex.z);
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

	 private PImage getDesaturated(PImage in, float amt) 
	 {
		 PImage out = in.get();
		 for (int i = 0; i < out.pixels.length; i++) {
			 int c = out.pixels[i];
			 float h = p.p.hue(c);
			 float s = p.p.saturation(c) * amt;
			 float b = p.p.brightness(c);
			 out.pixels[i] = p.p.color(h, s, b);
		 }
		 return out;
	 }

	 private PImage getFaintImage(PImage image, float amt) 
	 {
		 PImage out = image.get();
		 for (int i = 0; i < out.pixels.length; i++) {
			 int c = out.pixels[i];
			 float h = p.p.hue(c);
			 float s = p.p.saturation(c) * amt;
			 float b = p.p.brightness(c) * amt;
			 out.pixels[i] = p.p.color(h, s, b);
		 }
		 return out;
	 }

	 /**
	  * setGPSLocation()
	  * @param newGPSLocation New GPS location
	  * Set the current GPS location
	  */
	 void setGPSLocation(PVector newGPSLocation) 
	 {
		 gpsLocation = newGPSLocation;
		 calculateCaptureLocation();
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
}
