package multimediaLocator;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/************************************
 * @author davidgordon
 * A rectangular image in 3D virtual space
 */

class WMV_Image extends WMV_Media						 
{
	/* Classes */
	private WMV_ImageState state;
	private WMV_ImageMetadata metadata;
	
	/* Graphics */
	public PImage image, blurred;			// Image pixels
	private PImage blurMask;

	WMV_Image ( int newID, PImage newImage, int newMediaType, WMV_MediaMetadata newMediaMetadata, WMV_ImageMetadata newImageMetadata ) 
	{
		super(newID, newMediaType, newMediaMetadata);

		metadata = newImageMetadata;
		state = new WMV_ImageState(metadata);

		if(newImage != null) image = newImage;														// Empty image
		
		if(metadata.focusDistance == -1.f) 
			metadata.focusDistance = state.defaultFocusDistance;
		else 
			metadata.focusDistance = metadata.focusDistance;
		
		state.origFocusDistance = metadata.focusDistance;

		if(getMediaMetadata().dateTime != null)
			time = new WMV_Time( getMediaMetadata().dateTime, getID(), getMediaState().cluster, 0, getMediaMetadata().timeZone );		
		else
			time = null;

		state.vertices = new PVector[4]; 
		state.sVertices = new PVector[4]; 
		
		setAspectRatio( calculateAspectRatio() );
	}  

	/**
	 * Display the image in virtual space
	 */
	public void draw(WMV_World world)
	{
		if(getMediaState().showMetadata) 
			displayMetadata(world);

		float angleBrightnessFactor;							// Fade with angle
		float brightness = getFadingBrightness();					
		brightness *= getViewerSettings().userBrightness;
		
		float distanceBrightnessFactor = getDistanceBrightness(); 
		brightness *= distanceBrightnessFactor; 						// Fade iBrightness based on distance to camera

		if( getWorldState().timeFading && time != null && !getViewerState().isMoving() )
			brightness *= getTimeBrightness(); 					// Fade iBrightness based on time

//		System.out.println("Image #"+getID()+" currentTime:"+getWorldState().currentTime+" getTimeBrightness():"+getTimeBrightness());
		
		if( getViewerSettings().angleFading )
		{
			if( isSelected() )
				PApplet.println("Debugging: getViewerState().getOrientationVector():"+getViewerState().getOrientationVector());
			float imageAngle = getFacingAngle(getViewerState().getOrientationVector());
			angleBrightnessFactor = getAngleBrightness(imageAngle);                 // Fade out as turns sideways or gets too far / close
			brightness *= angleBrightnessFactor;
		}

		setViewingBrightness( PApplet.map(brightness, 0.f, 1.f, 0.f, 255.f) );				// Scale to setting for alpha range
		
		if (!isHidden() && !isDisabled()) 
		{
			if (getViewingBrightness() > 0)
			{
				if(image.width > 0)		// If image has been loaded
					displayImage(world);          // Draw the image 
			}
		} 
		else
			world.p.noFill();                  // Hide image if it isn't vState.visible

		if(getMediaState().visible && getWorldState().showModel && !isHidden() && !isDisabled())
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
			if(getDebugSettings().image || getDebugSettings().main)
				System.out.println("ERROR with Blur Mask... "+ex+" state.horizBorderID:"+state.horizBorderID+" state.vertBorderID:"+state.vertBorderID);
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
		
		world.p.stroke(0.f, 0.f, 255.f, getMediaState().viewingBrightness);	 
		world.p.strokeWeight(2.f);
		
		world.p.line(state.vertices[0].x, state.vertices[0].y, state.vertices[0].z, state.vertices[1].x, state.vertices[1].y, state.vertices[1].z);
		world.p.line(state.vertices[1].x, state.vertices[1].y, state.vertices[1].z, state.vertices[2].x, state.vertices[2].y, state.vertices[2].z);
		world.p.line(state.vertices[2].x, state.vertices[2].y, state.vertices[2].z, state.vertices[3].x, state.vertices[3].y, state.vertices[3].z);
		world.p.line(state.vertices[3].x, state.vertices[3].y, state.vertices[3].z, state.vertices[0].x, state.vertices[0].y, state.vertices[0].z);
		
		PVector c = world.getCurrentField().getCluster(getMediaState().cluster).getLocation();
		PVector loc = getMediaState().location;
		PVector cl = getCaptureLocation();
		world.p.popMatrix();

		world.p.pushMatrix();
		if(getWorldState().showMediaToCluster)
		{
			world.p.strokeWeight(3.f);
			world.p.stroke(80, 135, 255, getMediaState().viewingBrightness);
			world.p.line(c.x, c.y, c.z, loc.x, loc.y, loc.z);
		}

		if(getWorldState().showCaptureToMedia)
		{
			world.p.strokeWeight(3.f);
			world.p.stroke(160, 100, 255, getMediaState().viewingBrightness);
			world.p.line(cl.x, cl.y, cl.z, loc.x, loc.y, loc.z);
		}

		if(getWorldState().showCaptureToCluster)
		{
			world.p.strokeWeight(3.f);
			world.p.stroke(120, 55, 255, getMediaState().viewingBrightness);
			world.p.line(c.x, c.y, c.z, cl.x, cl.y, cl.z);
		}
		world.p.popMatrix();
	}

	/**
	 * Fade in image
	 */
	public void fadeIn()
	{
		if(getMediaState().fading || getMediaState().isFadingIn || getMediaState().isFadingOut)		// If already fading, stop at current value
			stopFading();

		fadeBrightness(1.f);					// Fade in
	}

	/**
	 * Fade out image
	 */
	public void fadeOut()
	{
		if(getMediaState().fading || getMediaState().isFadingIn || getMediaState().isFadingOut)		// If already fading, stop at current value
			stopFading();

		fadeBrightness(0.f);					// Fade out
	}
	
	/**
=	 * Update image geometry + visibility
	 */
	public void update(MultimediaLocator ml, WMV_Utilities utilities)
	{
		if(getMediaState().requested && image.width != 0)			// If vState.requested image has loaded, initialize image 
		{
			calculateVertices();  					// Update geometry		
			
			setAspectRatio( calculateAspectRatio() );
			blurred = applyMask(ml, image, blurMask);				// Apply blur mask once image has loaded
			setRequested(false);
//			p.p.vState.requestedImages--;
		}

		if(image.width > 0 && !isHidden() && !isDisabled())				// Image has been loaded and isn't vState.hidden or vState.disabled
		{
			boolean wasVisible = getMediaState().visible;
			boolean visibilitySetToTrue = false;
			boolean visibilitySetToFalse = false;

			setVisible(false);

			if(getViewerSettings().orientationMode)								// In Transitions Only Mode, visibility is based on distance of associated cluster 
			{
				if(getMediaState().cluster == getViewerState().getCurrentClusterID())		// If this photo's cluster is the current (closest) cluster, it is vState.visible
					setVisible(true);

				for(int id : getViewerState().getClustersVisible())
					if(getMediaState().cluster == id)			// If this photo's cluster is on next closest list, it is vState.visible	-- CHANGE THIS??!!
						setVisible(true);
			}
			else 
			{
				if(getViewerSettings().angleFading)
					setVisible( isFacingCamera(getViewerState().getLocation()) );		
				else 
					setVisible(true);     										 		
			}
			
			if(isVisible())
			{
				float imageAngle = getFacingAngle(getViewerState().getOrientationVector());			// Check if image is vState.visible at current angle facing viewer

				if(!utilities.isNaN(imageAngle))
					setVisible( (getAngleBrightness(imageAngle) > 0.f) );

				if(!isFading() && getViewerSettings().hideImages)
					setVisible(false);

				if(getMediaState().visible && !getViewerSettings().orientationMode)
					setVisible(getDistanceBrightness() > 0.f);

				if(metadata.orientation != 0 && metadata.orientation != 90)          	// Hide state.orientations of 180 or 270 (avoid upside down images)
					setVisible(false);

				if(isBackFacing(getViewerState().getLocation()) || isBehindCamera(getViewerState().getLocation(), getViewerState().getOrientationVector()))
					setVisible(false);
			}
			
			if(isFading())										// Update vState.brightness while fading
			{
				if(getMediaState().fadingBrightness == 0.f)
					setVisible(false);
			}
			else 
			{
				if(!wasVisible && isVisible())
					visibilitySetToTrue = true;

				if(getMediaState().fadingBrightness == 0.f && isVisible())
					visibilitySetToTrue = true;

				if(wasVisible && !isVisible())
					visibilitySetToFalse = true;

				if(getMediaState().fadingBrightness > 0.f && !isVisible())
					visibilitySetToFalse = true;
			}
			
			if(!getViewerSettings().angleThinning)
			{
				if(visibilitySetToTrue && !isFading() && !hasFadedOut() && !getViewerSettings().hideImages && getFadingBrightness() == 0.f)			// Fade in
					fadeIn();
			}
			else
			{
				if(getMediaState().visible && !state.thinningVisibility && !isFading())
				{
					fadeOut();
				}

				if(!isVisible() && state.thinningVisibility && !isFading() && !getViewerSettings().hideImages) 
				{
					if(!hasFadedOut())					// Fade in if didn't just finish fading out this frame
						fadeIn();
				}
			}

			if(visibilitySetToFalse)
				fadeOut();

			if(getMediaState().fadedOut) setFadedOut(false);
		}
		else
		{
			if(getViewerSettings().orientationMode)
			{
				for(int id : getViewerState().getClustersVisible())
					if(getMediaState().cluster == id  && !getMediaState().requested)			// If this photo's cluster is on next closest list, it is vState.visible	-- CHANGE THIS??!!
						loadMedia(ml);
			}
			else if(getCaptureDistance() < getViewerSettings().getFarViewingDistance() && !getMediaState().requested)
				loadMedia(ml); 					// Request image pixels from disk
		}
		
		if(isFading())                       // Fade in and out with time
			updateFadingBrightness();
		
		if(getMediaState().fadingFocusDistance)
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
			if(!getViewerSettings().selection && getDebugSettings().field)
			{
				world.p.stroke(155, 146, 255, 255);
				world.p.strokeWeight(state.outlineSize);
			}
		}

		world.p.rectMode(PApplet.CENTER);
		
		world.p.pushMatrix();
		world.p.beginShape(PApplet.POLYGON);    // Begin the shape containing the image
		world.p.textureMode(PApplet.NORMAL);
		
		world.p.noFill();

		if(getWorldState().fadeEdges)
			world.p.texture(blurred);
		else
			world.p.texture(image);        			// Apply the image to the face as a texture 

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
					world.p.tint(getViewingBrightness() * 0.4f, 255);          // Set the image transparency					
				else
					world.p.tint(255, getViewingBrightness() * 0.333f);    
			}
		}
		else
		{
			if(!getWorldState().alphaMode)
				world.p.tint(getViewingBrightness(), 255);          				
			else
			{
				world.p.tint(255, PApplet.map(getViewingBrightness(), 0.f, 255.f, 0.f, getWorldState().alpha));          				
			}
		}

		if(getViewerSettings().orientationMode)
		{
			world.p.vertex(state.sVertices[0].x, state.sVertices[0].y, state.sVertices[0].z, 0, 0);         // UPPER LEFT      
			world.p.vertex(state.sVertices[1].x, state.sVertices[1].y, state.sVertices[1].z, 1, 0);         // UPPER RIGHT           
			world.p.vertex(state.sVertices[2].x, state.sVertices[2].y, state.sVertices[2].z, 1, 1);			// LOWER RIGHT        
			world.p.vertex(state.sVertices[3].x, state.sVertices[3].y, state.sVertices[3].z, 0, 1);         // LOWER LEFT
		}
		else
		{
			world.p.vertex(state.vertices[0].x, state.vertices[0].y, state.vertices[0].z, 0, 0);            // UPPER LEFT      
			world.p.vertex(state.vertices[1].x, state.vertices[1].y, state.vertices[1].z, 1, 0);            // UPPER RIGHT           
			world.p.vertex(state.vertices[2].x, state.vertices[2].y, state.vertices[2].z, 1, 1);			// LOWER RIGHT        
			world.p.vertex(state.vertices[3].x, state.vertices[3].y, state.vertices[3].z, 0, 1);            // LOWER LEFT
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

		if(imageAngle > getViewerSettings().visibleAngle)
			angleBrightness = 0.f;
		else if (imageAngle < getViewerSettings().visibleAngle * 0.66f)
			angleBrightness = 1.f;
		else
			angleBrightness = PApplet.constrain((1.f-PApplet.map(imageAngle, getViewerSettings().visibleAngle * 0.66f, getViewerSettings().visibleAngle, 0.f, 1.f)), 0.f, 1.f);

		return angleBrightness;
	}

	/**
	 * getViewingDistance()
	 * @return How far the image is from the camera
	 */
	public float getViewingDistance()       // Find distance from camera to point in virtual space where photo appears           
	{
		PVector camLoc = getViewerState().getLocation();
		float distance;

		PVector loc = new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z);

		float r;

		if(metadata.focusDistance == -1.f)
			r = state.defaultFocusDistance;						// Use default if no focus distance in metadata					      
		else
			r = metadata.focusDistance;							

		float xDisp = r * (float)Math.sin(PApplet.radians(360-getTheta())) * (float)Math.sin(PApplet.radians(90-metadata.phi)); 
		float zDisp = r * (float)Math.cos(PApplet.radians(360-getTheta())) * (float)Math.sin(PApplet.radians(90-metadata.phi));  
		float yDisp = r * (float)Math.cos(PApplet.radians(90-metadata.phi)); 

		state.displacement = new PVector(-xDisp, -yDisp, -zDisp);

		loc.add(state.displacement);
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
		float farViewingDistance = getViewerSettings().getFarViewingDistance();
		float nearViewingDistance = getViewerSettings().getNearViewingDistance();
		
		float distVisibility = 1.f;

		if(viewDist > farViewingDistance)
		{
			float vanishingPoint = farViewingDistance + metadata.focusDistance;	// Distance where transparency reaches zero
//			float vanishingPoint = farViewingDistance + p.p.state.defaultFocusDistance;	// Distance where transparency reaches zero
			if(viewDist < vanishingPoint)
				distVisibility = PApplet.constrain(1.f - PApplet.map(viewDist, getViewerSettings().getFarViewingDistance(), vanishingPoint, 0.f, 1.f), 0.f, 1.f);    // Fade out until cam.vState.visibleFarDistance
			else
				distVisibility = 0.f;
		}
		else if(viewDist < nearViewingDistance)								
		{
			distVisibility = PApplet.constrain(PApplet.map(viewDist, getViewerSettings().getNearClippingDistance(), getViewerSettings().getNearViewingDistance(), 0.f, 1.f), 0.f, 1.f);
		}

		return distVisibility;
	}

	/**
	 * Update image geometry each frame
	 */
	public void calculateVertices()									
	{
		state.vertices = initializeVertices();					// Initialize Normal Mode state.vertices
		state.sVertices = initializeVertices();					// Initialize Orientation Mode (static) state.vertices
		
		if (metadata.phi != 0.) state.vertices = rotateVertices(state.vertices, -metadata.phi, getMediaState().verticalAxis);        	 // Rotate around X axis
		if (getTheta() != 0.) state.vertices = rotateVertices(state.vertices, 360-getTheta(), getMediaState().azimuthAxis);    // Rotate around Z axis
		
		if (metadata.phi != 0.) state.sVertices = rotateVertices(state.sVertices, -metadata.phi, getMediaState().verticalAxis);        // Rotate around X axis
		if (getTheta() != 0.) state.sVertices = rotateVertices(state.sVertices, 360-getTheta(), getMediaState().azimuthAxis);    // Rotate around Z axis
		
		if(state.vertices.length == 0) setDisabled(true);
		if(state.sVertices.length == 0) setDisabled(true);
		
		state.vertices = translateVertices(state.vertices, getCaptureLocation());               // Move image to photo capture location   

		state.displacement = getDisplacementVector();
		state.vertices = translateVertices(state.vertices, state.displacement);          // Translate image state.vertices from capture to viewing location
		state.sVertices = translateVertices(state.sVertices, state.displacement);          // Translate image state.vertices from capture to viewing location

		setLocation( new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z) );
		addVectorToLocation(state.displacement);     													 
	}
	
	public PVector getDisplacementVector()
	{
		float r;				  				 // Viewing sphere radius
		if(metadata.focusDistance == -1.f)
			r = state.defaultFocusDistance;		 // Use default if no focus distance in metadata					      
		else
			r = metadata.focusDistance;							

		float xDisp = r * (float)Math.sin((float)Math.toRadians(360-getTheta())) * (float)Math.sin((float)Math.toRadians(90-metadata.phi)); 
		float zDisp = r * (float)Math.cos((float)Math.toRadians(360-getTheta())) * (float)Math.sin((float)Math.toRadians(90-metadata.phi));  
		float yDisp = r * (float)Math.cos((float)Math.toRadians(90-metadata.phi)); 

		return new PVector(-xDisp, -yDisp, -zDisp);			// Displacement from capture location
	}

//	public PVector getLocation()
//	{
//		return getViewableState().location;
//	}

	/**
	 * Request the image to be loaded from disk
	 */
	public void loadMedia(MultimediaLocator ml)
	{
		if( !isHidden() && !isDisabled() )
		{
			calculateVertices();
			image = ml.requestImage(getFilePath());
			setRequested(true);
//			p.p.vState.requestedImages++;
		}
	}

	/**
	 * @return How far the image location is from a point
	 */
	float getImageDistanceFrom(PVector point)       // Find distance from camera to point in virtual space where photo appears           
	{
		float distance = PVector.dist(getLocation(), point);     
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
			setClusterID(closestClusterIndex);		// Associate image with cluster
		else
			setClusterID(-1);						// Create a new single image cluster here!

		if(getMediaState().cluster != -1)
			return true;
		else
			return false;
	}

	/**
	 * Set thinning visibility of image
	 * @param state New visibility
	 */
	public void setThinningVisibility(boolean newState)
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
	 * @return Whether image is facing the camera
	 */	
	public boolean isFacingCamera(PVector cameraPosition)
	{
		return Math.abs(getAngleToCamera(cameraPosition)) > getViewerSettings().visibleAngle;     			// If the result is positive, then it is facing the camera.
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
	 * @return Angle between the image and direction the camera is facing
	 */
	public float getFacingAngle(PVector camOrientation)
	{
		PVector faceNormal = getFaceNormal();

		PVector crossVector = new PVector();
		if(camOrientation == null) System.out.println("camOrientation == NULL..."+getID());
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
		float camToImage = getLocation().dist(camLoc);  					// Find distance from camera to image

//		if(captureToCam > camToImage + p.p.viewer.getNearClippingDistance())								// If captureToCam > camToPhoto, then back of the image is facing the camera
		if(captureToCam > camToImage + getViewerSettings().getNearClippingDistance() / 2.f)			// If captureToCam > camToVideo, then back of video is facing the camera
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
		if(metadata.cameraModel == 1)
		{
			if (metadata.orientation == 90)  // Vertical Image
			{
				vertex1 = state.vertices[1];
				vertex2 = state.vertices[3];
			}
			else if (metadata.orientation == 0)    // Horizontal Image
			{
				vertex1 = state.vertices[2];
				vertex2 = state.vertices[0];
			}
			else if (metadata.orientation == 180)    // Upside Down (Horizontal) Image
			{
				vertex1 = state.vertices[0];
				vertex2 = state.vertices[2];
			}
			else if (metadata.orientation == 270)    // Upside Down (Vertical) Image
			{
				vertex1 = state.vertices[3];
				vertex2 = state.vertices[1];
			}
			diff = PVector.sub(vertex1, vertex2);
			diff.mult(0.5f);
			result = PVector.add(vertex2, diff);
		}
		else
		{
			diff = PVector.sub(state.vertices[2], state.vertices[0]);
			diff.mult(0.5f);
			result = PVector.add(state.vertices[0], diff);
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

		if(metadata.cameraModel == 1)
		{
			if (metadata.orientation == 90)  // Vertical Image
			{
				vertex1 = state.vertices[3];
				vertex2 = state.vertices[0];
				vertex3 = state.vertices[1];
			}
			else if (metadata.orientation == 0)    // Horizontal Image
			{
				vertex1 = state.vertices[0];
				vertex2 = state.vertices[1];
				vertex3 = state.vertices[2];
			}
			else if (metadata.orientation == 180)    // Upside Down (Horizontal) Image
			{
				vertex1 = state.vertices[2];
				vertex2 = state.vertices[3];
				vertex3 = state.vertices[0];
			}
			else  if (metadata.orientation == 270)    // Upside Down (Vertical) Image
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
	 * Draw the image metadata in Heads-Up Display
	 */
	public void displayMetadata(WMV_World world)
	{
		String strTitleImage = "Image";
		String strTitleImage2 = "-----";
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
		String strElevation = "Vertical Angle: "+String.valueOf(metadata.phi);
		String strRotation = "Rotation: "+String.valueOf(metadata.rotation);
		String strFocusDistance = "Focus Distance: "+String.valueOf(metadata.focusDistance);

		String strTitleDebug = "--- Debugging ---";
		String strBrightness = "vState.brightness: "+String.valueOf(getViewingBrightness());
		String strBrightnessFading = "vState.brightnessFadingValue: "+String.valueOf(getFadingBrightness());
		
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

		if(getDebugSettings().image)
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
//	 * @return Average vState.brightness across all pixels
//	 */		
//	private float getAverageBrightness() 
//	{
//		image.loadPixels();
//		int b = 0;
//		for (int i=0; i<image.pixels.length; i++) {
//			float cur = p.p.p.vState.brightness(image.pixels[i]);
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
		state.isVideoPlaceHolder = true;
		state.assocVideoID = videoID;
		setHidden(true);
//		if(getDebugSettings().video) System.out.println("Image "+getID()+" is now associated with video "+videoID);
	}

	/**
	 * @return Aspect ratio of the image
	 */
	public float calculateAspectRatio()
	{
		float ratio = 0;

//		ratio = (float)(image.height)/(float)(image.width);
		ratio = (float)metadata.imageHeight / (float)metadata.imageWidth;
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
		setFadingFocusDistance(true);
		state.fadingFocusDistanceStartFrame = getWorldState().frameCount;					
		state.fadingFocusDistanceEndFrame = getWorldState().frameCount + state.fadingFocusDistanceLength;	
		state.fadingFocusDistanceStart = metadata.focusDistance;
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
		calculateVertices();  					// Update state.vertices given new width
	}
	
	void resetFocusDistance()
	{
		setFocusDistance(state.origFocusDistance);
	}
	
	private PVector[] initializeVertices()
	{
		float width = calculateImageWidth();										
		float height = width * getMediaState().aspectRatio;		

		float left = -width * 0.5f;						
		float right = width * 0.5f;
		float top = -height * 0.5f;
		float bottom = height * 0.5f;

		PVector[] verts = new PVector[4]; 

		if(metadata.cameraModel == 1)      			// If it is an iPhone Image
		{
			if (metadata.orientation == 90) 		 	// Vertical Image
			{
				verts[0] = new PVector( left, top, 0 );     			// UPPER LEFT  
				verts[1] = new PVector( right, top, 0 );      		// UPPER RIGHT 
				verts[2] = new PVector( right, bottom, 0 );       	// LOWER RIGHT
				verts[3] = new PVector( left, bottom, 0 );      		// LOWER LEFT
			}
			else if (metadata.orientation == 0)    	// Horizontal Image
			{
				verts[0] = new PVector( left, top, 0 );     			// UPPER LEFT  
				verts[1] = new PVector( right, top, 0 );      		// UPPER RIGHT 
				verts[2] = new PVector( right, bottom, 0 );       	// LOWER RIGHT
				verts[3] = new PVector( left, bottom, 0 );      		// LOWER LEFT
			}
			else if (metadata.orientation == 180)    // Upside Down (Horizontal) Image
			{
				verts[0] = new PVector( right, bottom, 0 );       	// LOWER RIGHT
				verts[1] = new PVector( left, bottom, 0 );      		// LOWER LEFT
				verts[2] = new PVector( left, top, 0 );     			// UPPER LEFT  
				verts[3] = new PVector( right, top, 0 );      		// UPPER RIGHT 
			}
			else  if (metadata.orientation == 270)    // Upside Down (Vertical) Image
			{
				verts[0] = new PVector( right, bottom, 0 );       	// LOWER RIGHT
				verts[1] = new PVector( left, bottom, 0 );      		// LOWER LEFT
				verts[2] = new PVector( left, top, 0 );     			// UPPER LEFT  
				verts[3] = new PVector( right, top, 0 );      		// UPPER RIGHT 
			}
		}
		else
		{
			if (metadata.orientation == 90 || metadata.orientation == 0)  				// Vertical or Horizontal Right-Side-Up Image
			{
				if (metadata.orientation == 90)
				{
					metadata.imageWidth = image.height;
					metadata.imageHeight = image.width;
				}

				verts[0] = new PVector( left, top, 0 );     			// UPPER LEFT  
				verts[1] = new PVector( right, top, 0 );      		// UPPER RIGHT 
				verts[2] = new PVector( right, bottom, 0 );       	// LOWER RIGHT
				verts[3] = new PVector( left, bottom, 0 );      		// LOWER LEFT
			}
			else if (metadata.orientation == 180 || metadata.orientation == 270)    		// Upside Down (Horizontal or Vertical) Image
			{
				if (metadata.orientation == 270 )
				{
					state.metadata.imageWidth = image.height;				// -- N.b. Editing metadata here
					state.metadata.imageHeight = image.width;
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
//		float state.subjectSizeRatio = subjectPixelWidth / originalstate.imageWidth;		// --More accurate

		float objectWidthOnSensor = metadata.sensorSize * state.subjectSizeRatio;			// 29 * 0.18 == 5.22
		float imgWidth = objectWidthOnSensor * metadata.focusDistance / metadata.focalLength;		// 5.22 * 9 / 4.2 == 11.19	Actual: 11.320482

		return imgWidth;
	}

	 /**
	  * @return Whether the state.vertices are null
	  */
	 public boolean verticesAreNull()
	 {
		 if(state.vertices[0] != null && state.vertices[1] != null && state.vertices[2] != null && state.vertices[3] != null)
			 return false;
		 else
			 return true;
	 }

	 // state.horizBorderID    0: Left  1: Center  2: Right  3: Left+Right
	 // state.vertBorderID	 0: Top  1: Center  2: Bottom  3: Top+Bottom
	 public void setBlurMaskID()
	 {
		 if(state.horizBorderID == 0)
		 {
			 switch(state.vertBorderID)
			 {
			 case 0:
//				 blurMask = p.p.blurMaskLeftTop;
				 state.blurMaskID = 0;
				 break;
			 case 1:
//				 blurMask = p.p.blurMaskLeftCenter;
				 state.blurMaskID = 1;
				 break;
			 case 2:
//				 blurMask = p.p.blurMaskLeftBottom;
				 state.blurMaskID = 2;
				 break;
			 case 3:
			 default:
//				 blurMask = p.p.blurMaskLeftBoth;
				 state.blurMaskID = 3;
				 break;
			 }
		 }
		 else if(state.horizBorderID == 1)
		 {
			 switch(state.vertBorderID)
			 {
			 case 0:
//				 blurMask = p.p.blurMaskCenterTop;
				 state.blurMaskID = 4;
				 break;
			 case 1:
//				 blurMask = p.p.blurMaskCenterCenter;
				 state.blurMaskID = 5;
				 break;
			 case 2:
//				 blurMask = p.p.blurMaskCenterBottom;
				 state.blurMaskID = 6;
				 break;
			 case 3:
			 default:
//				 blurMask = p.p.blurMaskCenterBoth;
				 state.blurMaskID = 7;
				 break;
			 }
		 }
		 else if(state.horizBorderID == 2)
		 {
			 switch(state.vertBorderID)
			 {
			 case 0:
//				 blurMask = p.p.blurMaskRightTop;
				 state.blurMaskID = 8;
				 break;
			 case 1:
//				 blurMask = p.p.blurMaskRightCenter;
				 state.blurMaskID = 9;
				 break;
			 case 2:
//				 blurMask = p.p.blurMaskRightBottom;
				 state.blurMaskID = 10;
				 break;
			 case 3:
			 default:
//				 blurMask = p.p.blurMaskRightBoth;
				 state.blurMaskID = 11;
				 break;
			 }
		 }
		 else if(state.horizBorderID == 3)
		 {
			 switch(state.vertBorderID)
			 {
			 case 0:
//				 blurMask = p.p.blurMaskBothTop;
				 state.blurMaskID = 12;
				 break;
			 case 1:
//				 blurMask = p.p.blurMaskBothCenter;
				 state.blurMaskID = 13;
				 break;
			 case 2:
//				 blurMask = p.p.blurMaskBothBottom;
				 state.blurMaskID = 14;
				 break;
			 case 3:
			 default:
//				 blurMask = p.p.blurMaskBothBoth;
				 state.blurMaskID = 15;
				 break;
			 }
		 }
	 }

	 public void setState(WMV_ImageState newState)
	 {
		 state = newState;
	 }

	 public WMV_ImageState getState()
	 {
		 return state;
	 }

	 public void captureState()
	 {
		 setMediaMetadata();
		 state.setMediaState( getMediaState(), metadata );
	 }
	 
	 public WMV_ImageMetadata getMetadata()
	 {
		 return metadata;
	 }
	 
	 public void setBlurMask(PImage newBlurMask)
	 {
		 blurMask = newBlurMask;
	 }
	 
	 public float getDirection()
	 {
		 return metadata.theta;
	 }

	 public float getVerticalAngle()
	 {
		 return metadata.phi;
	 }

	 public float getRotation()
	 {
		 return metadata.rotation;
	 }

	 public int getWidth()
	 {
		 return metadata.imageWidth;
	 }

	 public int getHeight()
	 {
		 return metadata.imageHeight;
	 }

	 public void setTheta(float newTheta)
	 {
		 state.metadata.theta = newTheta;
	 }

	 public float getTheta()
	 {
		 return metadata.theta;
	 }

	 public void setCameraModel(int newCameraModel)
	 {
		 state.metadata.cameraModel = newCameraModel;
	 }

	 public int getCameraModel()
	 {
		 return metadata.cameraModel;
	 }
	 
	 public void setBrightness(float newBrightness)
	 {
		 state.metadata.brightness = newBrightness;
	 }
	 
	 public float getBrightness()
	 {
		 return metadata.brightness;
	 }

	 public int getAssociatedVideo()
	 {
		 if(state.isVideoPlaceHolder)
		 {
			 return state.assocVideoID;
		 }
		 else return -1;
	 }

	 public boolean isVideoPlaceHolder()
	 {
		 return state.isVideoPlaceHolder;
	 }

	 public float getElevation()
	 {
		 return metadata.phi;
	 }

	 public float getOrientation()
	 {
		 return metadata.orientation;
	 }

	 public float getFocusDistance()
	 {
		 return metadata.focusDistance;
	 }

	 public float getFocalLength()
	 {
		 return metadata.focalLength;
	 }

	 public float getSensorSize()
	 {
		 return metadata.sensorSize;
	 }

	 public void setFocusDistance(float newFocusDistance)
	 {
		 metadata.focusDistance = newFocusDistance;
	 }

	 public void setFocalLength(float newFocalLength)
	 {
		 state.metadata.focalLength = newFocalLength;
	 }

	 public void setSensorSize(float newSensorSize)
	 {
		 state.metadata.sensorSize = newSensorSize;
	 }
	 
	 public void setHorizBorderID(int newHorizBorderID)
	 {
		 state.horizBorderID = newHorizBorderID;
	 }

	 public void setVertBorderID(int newVertBorderID)
	 {
		 state.vertBorderID = newVertBorderID;
	 }
	 
	 public void setBlurMaskID(int newBlurMaskID)
	 {
		 state.blurMaskID = newBlurMaskID;
	 }
	 
//	 private PImage getDesaturated(PImage in, float amt) 
//	 {
//		 PImage out = in.get();
//		 for (int i = 0; i < out.pixels.length; i++) {
//			 int c = out.pixels[i];
//			 float h = p.p.p.hue(c);
//			 float s = p.p.p.saturation(c) * amt;
//			 float b = p.p.p.vState.brightness(c);
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
//			 float b = p.p.p.vState.brightness(c) * amt;
//			 out.pixels[i] = p.p.p.color(h, s, b);
//		 }
//		 return out;
//	 }
}
