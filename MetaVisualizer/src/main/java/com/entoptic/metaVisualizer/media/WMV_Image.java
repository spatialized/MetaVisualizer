package main.java.com.entoptic.metaVisualizer.media;

import main.java.com.entoptic.metaVisualizer.MetaVisualizer;
import main.java.com.entoptic.metaVisualizer.metadata.WMV_ImageMetadata;
import main.java.com.entoptic.metaVisualizer.misc.WMV_Utilities;
import main.java.com.entoptic.metaVisualizer.model.WMV_Cluster;
import main.java.com.entoptic.metaVisualizer.model.WMV_Time;
import main.java.com.entoptic.metaVisualizer.world.WMV_Field;
import main.java.com.entoptic.metaVisualizer.world.WMV_Viewer;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/************************************
 * Rectangular image in a 3D environment
 * @author davidgordon
 */
public class WMV_Image extends WMV_Media			 
{
	/* Classes */
	private WMV_ImageState state;			// Image virtual state
	private WMV_ImageMetadata metadata;		// Image metadata

	/* Image */
	public PImage image;					// Image pixels to display
	public PImage blurMask;					// Blur mask
	public PImage blurred;					// Combined pixels 

	private float aspectWidthRatioFactor;

	/**
	 * Constructor for image in 3D space
	 * @param newID Image ID
	 * @param newImage Image file
	 * @param newMediaType Media type ID
	 * @param newImageMetadata Image metadata
	 */
	public WMV_Image ( int newID, PImage newImage, int newMediaType, WMV_ImageMetadata newImageMetadata, float newAspectWidthRatioFactor ) 
	{
		super( newID, newMediaType, newImageMetadata.name, newImageMetadata.filePath, newImageMetadata.dateTime, newImageMetadata.timeZone, 
				newImageMetadata.gpsLocation, newImageMetadata.longitudeRef, newImageMetadata.latitudeRef );

		metadata = newImageMetadata;					// Image metadata

		if(metadata.orientation == -1)
			metadata.orientation = guessOrientation();	// Guess image orientation if none found in metadata

		state = new WMV_ImageState();
		state.initialize(metadata);						// Copy metadata to image state (for exporting)

		if(newImage != null) image = newImage;			// Empty image

		if(metadata.focusDistance == -1.f) 							// Use default focus distance if none found in metadata
			metadata.focusDistance = state.defaultFocusDistance;
		else 
			metadata.focusDistance = metadata.focusDistance;

		state.origFocusDistance = metadata.focusDistance;

		initializeTime();								// Initialize time and date from metadata

		state.vertices = new PVector[4]; 				// Initialize vertices
		state.sVertices = new PVector[4]; 				// Initialize Static (Orientation) Mode vertices

		setAspectRatio( calculateAspectRatio() );		// Set image aspect ratio

		aspectWidthRatioFactor = newAspectWidthRatioFactor;
	}

	/**
	 * Calculate and set image visibility based on viewer location and orientation
	 * @param viewer Viewer
	 * @param utilities Utilities object
	 */
	public void calculateVisibility(WMV_Viewer viewer, WMV_Utilities utilities)
	{
		setVisible(false);

		if(getViewerSettings() == null)
		{
			if(getDebugSettings().image || getDebugSettings().ml) 
				System.out.println("Image.calculateVisibility()... Fixing getSettings().. error in image #"+getID());
			
			updateWorldState(viewer.p.getSettings(), viewer.p.getState(), viewer.getSettings(), viewer.getState());
		}
		
		if(viewer.getSettings().orientationMode)								// In Transitions Only Mode, visibility is based on distance of associated cluster 
		{
			if(getMediaState().getClusterID() == getViewerState().getCurrentClusterID())		// If this photo's cluster is the current (closest) cluster, it is visible
				setVisible(true);

			for(int id : getViewerState().getClustersVisible())
				if(getMediaState().getClusterID() == id)			// If associated cluster is visible, it is visible
					setVisible(true);
		}
		else 
		{
			if(viewer.getSettings().angleFading)
				setVisible( isFacingViewer(viewer.getLocation()) );					// Check if image is facing viewer	
			else
				setVisible(true);     										 		
		}

		if(isVisible())
		{
			float imageAngle = getFacingAngle(viewer.getState().getOrientationVector());			// Check if image is visible at current angle facing viewer

			if(!utilities.isNaN(imageAngle))
				setVisible( (getAngleBrightness(imageAngle) > 0.f) );

			if(!isFadingOut() && getViewerSettings().hideImages)
				setVisible(false);

			if(getMediaState().visible && !getViewerSettings().orientationMode)
				setVisible(getDistanceBrightness(viewer, viewer.getFarViewingDistance(), metadata.focusDistance) > 0.f);

			if(metadata.orientation != 0 && metadata.orientation != 90)          	// Hide state.orientations of 180 or 270 (avoid upside down images)
				setVisible(false);

			if(isBackFacing(viewer.getLocation()) || isBehindCamera(viewer.getLocation(), viewer.getState().getOrientationVector()))
				setVisible(false);
		}
	}
	
	/**
	 * Calculate visibility resulting from fading behavior
	 * @param f
	 * @param wasVisible
	 */
	public void calculateFadingVisibility(WMV_Field f, boolean wasVisible)
	{
		boolean visibilitySetToTrue = false;
		boolean visibilitySetToFalse = false;

		if(isFading())										// Update brightness while fading
		{
			if(isFadingOut())
				if(getFadingBrightness() == 0.f)
					setVisible(false);
		}
		else 
		{
			if(!wasVisible && isVisible())
				visibilitySetToTrue = true;

			if(getFadingBrightness() == 0.f && isVisible())
				visibilitySetToTrue = true;

			if(wasVisible && !isVisible())
				visibilitySetToFalse = true;

			if(getFadingBrightness() > 0.f && !isVisible())
				visibilitySetToFalse = true;
		}

		if(getViewerSettings().angleThinning)
		{
			if(isVisible() && !state.thinningVisibility && !isFading())
				fadeOut(f, false);
			
			if(!isVisible() && state.thinningVisibility && !isFading() && !getViewerSettings().hideImages) 
			{
				if(!hasFadedOut())					// Fade in if didn't just finish fading out this frame
					fadeIn(f);
			}
		}
		else
		{
			if(visibilitySetToTrue && !isFading() && !hasFadedOut() && !getViewerSettings().hideImages && getFadingBrightness() == 0.f)			// Fade in
				fadeIn(f);
		}

		if(visibilitySetToFalse)
			fadeOut(f, false);

		if(hasFadedOut()) setFadedOut(false);
	}
	
	/** 
	 * Draw the image
	 */
	public void display(MetaVisualizer ml)
	{
		if(getViewerSettings().selection)
		{
			if (isSelected())     // Draw outline
			{
				ml.stroke(state.outlineHue, state.outlineSaturation, state.outlineBrightness, state.outlineAlpha);
				ml.strokeWeight(state.outlineSize);
			}
			else
				ml.noStroke(); 
		}
		else
		{
			if(ml.world.getState().showModel)
			{
				ml.stroke(state.outlineHue, 0.f, state.outlineBrightness, state.outlineAlpha);
				ml.strokeWeight(1);
			}
			else
				ml.noStroke(); 
		}

		ml.pushMatrix();
		ml.beginShape(PApplet.POLYGON);    // Begin the shape containing the image
		ml.textureMode(PApplet.NORMAL);

		ml.noFill();

		if(getWorldState().useBlurMasks)
			ml.texture(blurred);
		else
			ml.texture(image);        			// Apply the image to the face as a texture 

		if(!getWorldState().alphaMode)
			ml.tint(getViewingBrightness(), 255);          				
		else
			ml.tint(255, PApplet.map(getViewingBrightness(), 0.f, 255.f, 0.f, getWorldState().alpha));          				

		if(getViewerSettings().orientationMode)
		{
			ml.vertex(state.sVertices[0].x, state.sVertices[0].y, state.sVertices[0].z, 0, 0);         // UPPER LEFT      
			ml.vertex(state.sVertices[1].x, state.sVertices[1].y, state.sVertices[1].z, 1, 0);         // UPPER RIGHT           
			ml.vertex(state.sVertices[2].x, state.sVertices[2].y, state.sVertices[2].z, 1, 1);			// LOWER RIGHT        
			ml.vertex(state.sVertices[3].x, state.sVertices[3].y, state.sVertices[3].z, 0, 1);         // LOWER LEFT
		}
		else
		{
			ml.vertex(state.vertices[0].x, state.vertices[0].y, state.vertices[0].z, 0, 0);            // UPPER LEFT      
			ml.vertex(state.vertices[1].x, state.vertices[1].y, state.vertices[1].z, 1, 0);            // UPPER RIGHT           
			ml.vertex(state.vertices[2].x, state.vertices[2].y, state.vertices[2].z, 1, 1);			// LOWER RIGHT        
			ml.vertex(state.vertices[3].x, state.vertices[3].y, state.vertices[3].z, 0, 1);            // LOWER LEFT
		}

		ml.endShape(PApplet.CLOSE);       // End the shape containing the image
		ml.popMatrix();
	}
	
	/**
	 * @param size Size to draw the video center
	 * Draw the video center as a colored sphere
	 */
	public void displayModel(MetaVisualizer ml)
	{
		float modelAlpha = getViewerSettings().userBrightness;

		float farViewingDistance;
		if(ml.world.viewer.getSettings().showInvisibleModels)
		{
			farViewingDistance = ml.world.viewer.getFarViewingDistance() * ml.world.getState().modelDistanceVisibilityFactorFar;
		}
		else
		{
			modelAlpha *= getFadingBrightness();
			farViewingDistance = ml.world.viewer.getFarViewingDistance() * ml.world.getState().modelDistanceVisibilityFactorClose;
		}

		float alphaDistanceFactor = getDistanceBrightness( ml.world.viewer, ml.world.viewer.getFarViewingDistance() +
															   metadata.focusDistance, farViewingDistance ); 
		modelAlpha *= alphaDistanceFactor; 					// Fade brightness based on distance to camera

		float modelBrightness = PApplet.map(modelAlpha, 0.f, 1.f, 0.f, ml.world.getState().modelBrightness);	// Scale to setting for alpha range
		modelAlpha = PApplet.map(modelAlpha, 0.f, 1.f, 0.f, ml.world.getState().modelAlpha);				// Scale to setting for alpha range

//		if( getWorldState().timeFading && time != null && !ml.world.viewer.isMoving() )
//			brightness *= getTimeBrightness(); 					// Fade model brightness based on time 	-- In progress

		/* Draw frame */
		ml.pushMatrix();
		ml.stroke(0.f, 0.f, modelBrightness, modelAlpha);	 
		ml.strokeWeight(2.f);

		ml.line( state.vertices[0].x, state.vertices[0].y, state.vertices[0].z, state.vertices[1].x, state.vertices[1].y, state.vertices[1].z );
		ml.line( state.vertices[1].x, state.vertices[1].y, state.vertices[1].z, state.vertices[2].x, state.vertices[2].y, state.vertices[2].z );
		ml.line( state.vertices[2].x, state.vertices[2].y, state.vertices[2].z, state.vertices[3].x, state.vertices[3].y, state.vertices[3].z );
		ml.line( state.vertices[3].x, state.vertices[3].y, state.vertices[3].z, state.vertices[0].x, state.vertices[0].y, state.vertices[0].z );
		ml.popMatrix();

		int clusterID = getAssociatedClusterID();
		if(clusterID >= 0 && clusterID < ml.world.getCurrentFieldClusters().size())
		{
			WMV_Cluster cluster = ml.world.getCurrentField().getCluster(getAssociatedClusterID());
			PVector c = cluster.getLocation();
			PVector loc = getLocation();
			PVector cl = getCaptureLocation();

			/* Draw media, cluster and capture location */
			ml.pushMatrix();
			if(getWorldState().showMediaToCluster)
			{
				ml.strokeWeight(3.f);
				ml.stroke(80, 135, 255, getViewingBrightness() * 0.8f);
				ml.line(c.x, c.y, c.z, loc.x, loc.y, loc.z);
			}

			if(getWorldState().showCaptureToMedia)
			{
				ml.strokeWeight(3.f);
				ml.stroke(160, 100, 255, getViewingBrightness() * 0.8f);
				ml.line(cl.x, cl.y, cl.z, loc.x, loc.y, loc.z);
			}

			if(getWorldState().showCaptureToCluster)
			{
				ml.strokeWeight(3.f);
				ml.stroke(120, 55, 255, getViewingBrightness() * 0.8f);
				ml.line(c.x, c.y, c.z, cl.x, cl.y, cl.z);
			}
			ml.popMatrix();
		}
		else
			ml.systemMessage("Image.displayModel()... Cluster requested: "+clusterID+" is out of range:"+ml.world.getCurrentField().getClusters().size()+" ...");
	}

	/** 
	 * Draw original image in Heads-Up Display
	 */
	public void display2D(MetaVisualizer ml)
	{
		ml.noStroke(); 

		ml.pushMatrix();
		ml.beginShape(PApplet.POLYGON);    // Begin the shape containing the image
		ml.textureMode(PApplet.NORMAL);

		ml.noFill();
		ml.texture(image);        			// Apply the image to the face as a texture 
		ml.tint(255, 255);          				

		int imgWidth = getWidth();
		int imgHeight = getHeight();

		ml.translate(-imgWidth / 2.f, -imgHeight / 2.f);

		ml.vertex(0, 0, 0, 0, 0);             	// UPPER LEFT      
		ml.vertex(imgWidth, 0, 0, 1, 0);              	// UPPER RIGHT           
		ml.vertex(imgWidth, imgHeight, 0, 1, 1);				// LOWER RIGHT        
		ml.vertex(0, imgHeight, 0, 0, 1);              	// LOWER LEFT

		ml.endShape(PApplet.CLOSE);       // End the shape containing the image
		ml.popMatrix();
	}
	
	/**
	 * Apply mask to image
	 * @param ml Parent app
	 * @param source Source image
	 * @param mask Mask image
	 * @return
	 */
	public PImage applyMask(MetaVisualizer ml, PImage source, PImage mask)
	{
		PImage result = ml.createImage(640, 480, PApplet.RGB);

		try
		{
			result = source.copy();
			result.mask(mask); 
		}
		catch(RuntimeException ex)
		{
			if(getDebugSettings().image || getDebugSettings().ml)
			{
				System.out.println("Image #"+getID()+" name:"+getName()+" ERROR with Image Blur Mask... "+ex+" state.horizBorderID:"+state.horizBordersID+" state.vertBorderID:"+state.vertBordersID);
				if(source != null && mask != null)
				{
					System.out.println("  source.width:"+source.width+" mask.width:"+mask.width+"  source.height:"+source.height+" mask.height:"+mask.height);
				}
				else
				{
					System.out.println("  source == null?"+(source == null));
					System.out.println("  mask == null?"+(mask == null));
				}
			}
		}

		return result;
	}


	/**
	 * Calculate image brightness given viewer to image angle
	 * @param imageAngle Current angle between viewer and image
	 * @return Amount to fade image due to angle
	 */
	public float getAngleBrightness(float imageAngle)
	{
		float angleBrightness = 0.f;

		if(getViewerSettings() != null)
		{
			if(imageAngle > getViewerSettings().visibleAngle)
				angleBrightness = 0.f;
			else if (imageAngle < getViewerSettings().visibleAngle * 0.66f)
				angleBrightness = 1.f;
			else
				angleBrightness = PApplet.constrain((1.f-PApplet.map(imageAngle, getViewerSettings().visibleAngle * 0.66f, getViewerSettings().visibleAngle, 0.f, 1.f)), 0.f, 1.f);
		}
		else
		{
			System.out.println("Image.getAngleBrightness()... Image #"+getID()+" viewerSettings is null! disabled?"+isDisabled());
		}
		return angleBrightness;
	}

	/**
	 * Measure distance between image and viewer
	 * @return Image viewing distance
	 */
	public float getViewingDistance(WMV_Viewer viewer)       // Find distance from camera to point in virtual space where photo appears           
	{
		PVector camLoc = viewer.getLocation();
		PVector loc = new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z);

		float r;

		if(metadata.focusDistance == -1.f)
			r = state.defaultFocusDistance;						// Use default if no focus distance in metadata					      
		else
			r = metadata.focusDistance;							

		float xDisp = r * (float)Math.sin(PApplet.radians(360-getDirection())) * (float)Math.sin(PApplet.radians(90-metadata.phi)); 
		float zDisp = r * (float)Math.cos(PApplet.radians(360-getDirection())) * (float)Math.sin(PApplet.radians(90-metadata.phi));  
		float yDisp = r * (float)Math.cos(PApplet.radians(90-metadata.phi)); 

		state.displacement = new PVector(-xDisp, -yDisp, -zDisp);

		loc.add(state.displacement);
		return PVector.dist(loc, camLoc);     
	}

	/** 
	 * Find image transparency due to distance (fades away in distance and as camera gets close)
	 * @return Distance visibility multiplier between 0. and 1.
	 * @param viewer Viewer 
	 * @param farViewingDistance Distance at which image starts fading out
	 * @param vanishingDistance Distance beyond far viewing distance when image fades to invisible
	 * @return Distance visibility multiplier between 0. and 1.
	 */
	public float getDistanceBrightness(WMV_Viewer viewer, float farViewingDistance, float vanishingDistance)									
	{
		float viewingDist = getViewingDistance(viewer);
//		float farViewingDistance = getViewerSettings().getFarViewingDistance();
		float nearViewingDistance = getViewerSettings().getNearViewingDistance();

		float distVisibility = 1.f;

		if(viewingDist > farViewingDistance)
		{
			float vanishingPoint = farViewingDistance + vanishingDistance;	// Distance where transparency reaches zero
//			float vanishingPoint = farViewingDistance + metadata.focusDistance;	// Distance where transparency reaches zero
			if(viewingDist < vanishingPoint)
				distVisibility = PApplet.constrain(1.f - PApplet.map(viewingDist, farViewingDistance, vanishingPoint, 0.f, 1.f), 0.f, 1.f);    // Fade out until cam.visibleFarDistance
			else
				distVisibility = 0.f;
		}
		else if(viewingDist < nearViewingDistance)								
			distVisibility = PApplet.constrain(PApplet.map(viewingDist, viewer.getNearClippingDistance(), viewer.getNearViewingDistance(), 0.f, 1.f), 0.f, 1.f);

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
		if (getDirection() != 0.) state.vertices = rotateVertices(state.vertices, 360-getDirection(), getMediaState().azimuthAxis);    // Rotate around Z axis

		if (metadata.phi != 0.) state.sVertices = rotateVertices(state.sVertices, -metadata.phi, getMediaState().verticalAxis);        // Rotate around X axis
		if (getDirection() != 0.) state.sVertices = rotateVertices(state.sVertices, 360-getDirection(), getMediaState().azimuthAxis);    // Rotate around Z axis

		if(state.vertices.length == 0) setDisabled(true);
		if(state.sVertices.length == 0) setDisabled(true);

		state.vertices = translateVertices(state.vertices, getCaptureLocation());      // Move image to photo capture location   

		if(state.vertices == null)
		{
			System.out.println("Image.calculateVertices()... state.vertices == null!... id #"+getID()+"  name:"+getName());
		}
		else
		{
			calculateLocation();
//			state.displacement = getDisplacementVector();
//			setLocation( new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z) );
//			addToLocation(state.displacement);     													 
			
			state.vertices = translateVertices(state.vertices, state.displacement);        // Translate image vertices from capture to viewing location
			state.sVertices = translateVertices(state.sVertices, state.displacement);      // Translate image static vertices from capture to viewing location

//			setLocation( new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z) );
//			addToLocation(state.displacement);     													 
		}
	}
	
	/**
	 * Calculate location given displacement vector
	 */
	public void calculateLocation()
	{
		state.displacement = getDisplacementVector();
		setLocation( new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z) );
		addToLocation(state.displacement);     													 
	}

	/**
	 * @return Image location displacement vector from capture location
	 */
	public PVector getDisplacementVector()
	{
		float r;				  				 			// Viewing sphere radius
		
		if(metadata.focusDistance == -1.f)
			r = state.defaultFocusDistance;		 			// Use default if no focus distance in metadata					      
		else
			r = metadata.focusDistance;							

		float xDisp = r * (float)Math.sin((float)Math.toRadians(360-getDirection())) * (float)Math.sin((float)Math.toRadians(90-metadata.phi)); 
		float zDisp = r * (float)Math.cos((float)Math.toRadians(360-getDirection())) * (float)Math.sin((float)Math.toRadians(90-metadata.phi));  
		float yDisp = r * (float)Math.cos((float)Math.toRadians(90-metadata.phi)); 

		return new PVector(-xDisp, -yDisp, -zDisp);			// Displacement from capture location
	}

	/**
	 * Request the image to be loaded from disk
	 * @param ml Parent app
	 */
	public void loadMedia(MetaVisualizer ml)
	{
		if( !isHidden() && !isDisabled() )
		{
//			if(ml.debug.world && ml.debug.detailed) 
//				System.out.println("Image.loadMedia()... id #"+getID());
			
			calculateVertices();
			image = ml.requestImage(getFilePath());
			setRequested(true);
		}
	}

	/**
	 * Find distance between image location and a given point
	 * @param point Point to measure distance from
	 * @return Distance between image and point
	 */
	float getImageDistanceFrom(PVector point)       // Find distance from camera to point in virtual space where photo appears           
	{
		float distance = PVector.dist(getLocation(), point);     
		return distance;
	}

	/**
	 * @return Whether image is facing the viewer
	 */	
	public boolean isFacingViewer(PVector viewerPosition)
	{
		return Math.abs(getAngleToViewer(viewerPosition)) > getViewerSettings().visibleAngle;     			// If the result is positive, then it is facing the camera.
	}

	/**
	 * @return Angle between viewer location and image
	 */	
	public float getAngleToViewer(PVector viewerPosition)
	{
		PVector centerVertex = calcCenterVertex();

		PVector cameraToFace = new PVector(  viewerPosition.x-centerVertex.x, 	//  Vector from the camera to the face.      
				viewerPosition.y-centerVertex.y, 
				viewerPosition.z-centerVertex.z   );

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
	 * @param viewerOrientation Current viewer orientation
	 * @return Angle between the image and direction the camera is facing
	 */
	public float getFacingAngle(PVector viewerOrientation)
	{
		PVector faceNormal = getFaceNormal();

		PVector crossVector = new PVector();
		if(viewerOrientation == null) System.out.println("camOrientation == NULL..."+getID());
		PVector.cross(viewerOrientation, faceNormal, crossVector);				// Cross vector gives angle between camera and image

		float result = crossVector.mag();
		return result;
	}

	/**
	 * @param vLoc Viewer location
	 * @return Whether the camera is behind the image
	 */
	public boolean isBackFacing(PVector vLoc)										
	{
		float captureToViewer = getCaptureLocation().dist(vLoc);  	// Find distance from capture location to camera
		float viewerToImage = getLocation().dist(vLoc);  					// Find distance from camera to image

		if(captureToViewer > viewerToImage + getViewerSettings().getNearClippingDistance() * 0.5f)			// If captureToCam > camToVideo, then back of video is facing the camera
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
	public void displayMetadata(MetaVisualizer ml)
	{
		String strTitleImage = "Image";
		String strTitleImage2 = "";
		String strName = "Name: "+getName();
		String strID = "ID: "+String.valueOf(getID());
		String strCluster = "Cluster: "+String.valueOf(getAssociatedClusterID());
		String strX = "Location X: "+String.valueOf(getCaptureLocation().z);
		String strY = " Y: "+String.valueOf(getCaptureLocation().x);
		String strZ = " Z: "+String.valueOf(getCaptureLocation().y);

		String strDate = "Date: "+String.valueOf(time.getMonth()) + "-" + String.valueOf(time.getDay()) + "-" + String.valueOf(time.getYear());
		String strTime = "Time: "+String.valueOf(time.getHour()) + ":" + (time.getMinute() >= 10 ? String.valueOf(time.getMinute()) : "0"+String.valueOf(time.getMinute())) + ":" + 
				(time.getSecond() >= 10 ? String.valueOf(time.getSecond()) : "0"+String.valueOf(time.getSecond()));

		String strLatitude = "GPS Latitude: "+String.valueOf(getGPSLocation().z);
		String strLongitude = " Longitude: "+String.valueOf(getGPSLocation().x);
		String strAltitude = "Altitude: "+String.valueOf(getGPSLocation().y);
		String strTheta = "Direction: "+String.valueOf(getDirection());
		String strElevation = "Vertical Angle: "+String.valueOf(metadata.phi);
		String strRotation = "Rotation: "+String.valueOf(metadata.rotation);
		String strFocusDistance = "Focus Distance: "+String.valueOf(metadata.focusDistance);

		String strTitleDebug = "--- Debugging ---";
		String strBrightness = "brightness: "+String.valueOf(getViewingBrightness());
		String strBrightnessFading = "brightnessFadingValue: "+String.valueOf(getFadingBrightness());

		int frameCount = getWorldState().frameCount;
		ml.display.metadata(frameCount, strTitleImage);
		ml.display.metadata(frameCount, strTitleImage2);
		ml.display.metadata(frameCount, "");

		ml.display.metadata(frameCount, strID);
		ml.display.metadata(frameCount, strCluster);
		ml.display.metadata(frameCount, strName);
		ml.display.metadata(frameCount, strX + strY + strZ);
		ml.display.metadata(frameCount, "");

		ml.display.metadata(frameCount, strDate);
		ml.display.metadata(frameCount, strTime);
		ml.display.metadata(frameCount, "");

		ml.display.metadata(frameCount, strLatitude + strLongitude);
		ml.display.metadata(frameCount, strAltitude);
		ml.display.metadata(frameCount, strTheta);
		ml.display.metadata(frameCount, strElevation);
		ml.display.metadata(frameCount, strRotation);
		ml.display.metadata(frameCount, strFocusDistance);

		if(getDebugSettings().image)
		{
			ml.display.metadata(frameCount, strTitleDebug);
			ml.display.metadata(frameCount, strBrightness);
			ml.display.metadata(frameCount, strBrightnessFading);
		}
	}

	public String getFilePath()
	{
		return getMetadata().filePath;
	}

	public void setFilePath(String newFilePath)
	{
		metadata.filePath = newFilePath;
	}
	
	public void updateFilePath(MetaVisualizer ml, WMV_Field parentField)
	{
		String oldFilePath = getFilePath();
		String[] parts = oldFilePath.split("/");

		parts[parts.length-4] = ml.library.getName(true);			// Library name
		parts[parts.length-3] = parentField.getName();					// Field name
		
		String newFilePath = parts[0];
		for(int i=1; i<parts.length; i++)
			newFilePath = newFilePath + "/" + parts[i];
		setFilePath(newFilePath);
	}
	
	/**
	 * Initialize time and date from metadata
	 */
	public void initializeTime()
	{
		if(metadata.dateTime == null)
		{
			try {
				metadata.dateTime = parseDateTime(metadata.dateTimeString);
				time = new WMV_Time();
				time.initialize( metadata.dateTime, metadata.dateTimeString, getID(), 0, getAssociatedClusterID(), metadata.timeZone );
			} 
			catch (Throwable t) 
			{
				System.out.println("Error in image date / time... " + t);
			}
		}
		else
		{
			time = new WMV_Time();
			time.initialize( metadata.dateTime, metadata.dateTimeString, getID(), 0, getAssociatedClusterID(), metadata.timeZone );
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

	public int guessOrientation()
	{
		if(metadata.imageWidth > metadata.imageHeight)
			return 0;
		if(metadata.imageWidth > metadata.imageHeight)
			return 90;
		else
			return 0;
	}

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
	public void fadeFocusDistance(float target, int frameCount)
	{
//		System.out.println("Image.fadeFocusDistance()... #"+getID()+" frameCount:"+frameCount+" state.fadingFocusDistanceLength:"+state.fadingFocusDistanceLength);
		setFadingFocusDistance(true);
		state.fadingFocusDistanceStartFrame = frameCount;					
		state.fadingFocusDistanceEndFrame = frameCount + state.fadingFocusDistanceLength;	
		state.fadingFocusDistanceStart = metadata.focusDistance;
		state.fadingFocusDistanceTarget = target;
	}

	/**
	 * Fade focus distance to given target while rescaling images 
	 * @param target New focus distance
	 */
	public void startFadingFocusDistance(float target, int frameCount)
	{
		setFadingFocusDistance(true);
		
//		state.fadingFocusDistanceStartFrame = getWorldState().frameCount;					
//		state.fadingFocusDistanceEndFrame = getWorldState().frameCount + 1;		// Only one frame between start and end indicates continuous fading 
		state.fadingFocusDistanceStartFrame = frameCount;					
		state.fadingFocusDistanceEndFrame = frameCount + 1;		// Only one frame between start and end indicates continuous fading 

//		System.out.println("startFadingFocusDistance()... Image ID #"+getID()+" frameCount:"+state.fadingFocusDistanceStartFrame+" state.fadingFocusDistanceEndFrame:"+state.fadingFocusDistanceEndFrame);
		
		state.fadingFocusDistanceStart = metadata.focusDistance;
		state.fadingFocusDistanceTarget = target;
	}

	/**
	 * Fade focus distance to given target while rescaling images 
	 * @param target New focus distance
	 */
	public void stopFadingFocusDistance()
	{
//		System.out.println("Image.stopFadingFocusDistance()... Image ID #"+getID());

		setFadingFocusDistance(false);
		setFocusDistance( state.fadingFocusDistanceTarget );	// Set focus distance
		calculateVertices();  								// Update vertices given new focus distance
	}

	/**
	 * Update fading of object distance (focus distance and image size together)
	 */
	public void updateFadingFocusDistance()
	{
		float newFocusDistance = 0.f;

		if (getWorldState().frameCount >= state.fadingFocusDistanceEndFrame)
		{
			if(state.fadingFocusDistanceEndFrame - state.fadingFocusDistanceStartFrame > 1)
				setFadingFocusDistance(false);
			
			newFocusDistance = state.fadingFocusDistanceTarget;
		} 
		else
		{
			newFocusDistance = PApplet.map( getWorldState().frameCount, state.fadingFocusDistanceStartFrame, 
					state.fadingFocusDistanceEndFrame, state.fadingFocusDistanceStart, 
					state.fadingFocusDistanceTarget);     				 	// Fade with distance from current time
		}
		
		setFocusDistance( newFocusDistance );	// Set focus distance
		calculateVertices();  					// Update vertices given focus distance
	}

	/**
	 * Initialize image vertices
	 * @return Vertex array
	 */
	private PVector[] initializeVertices()
	{
		float width = getImageWidthInMeters();										
		float height = width * getAspectRatio();		
		
		float left = -width * 0.5f;						
		float right = width * 0.5f;
		float top = -height * 0.5f;
		float bottom = height * 0.5f;
		
		left *= aspectWidthRatioFactor;			/* Testing */
		right *= aspectWidthRatioFactor;
		
//		if(getID() == 1)
//		{
//			System.out.println("ID #1   left:"+left);
//			System.out.println(" would have been :"+(-width * 0.5f));
//			System.out.println(" getWorldState().aspectWidthRatioFactor: "+getWorldState().aspectWidthRatioFactor);
//			System.out.println("");
//		}

		PVector[] verts = new PVector[4]; 

		if(metadata.cameraModel == 0)      				// iPhone Image
		{
			if (metadata.orientation == 90) 		 	// Vertical Image
			{
				verts[0] = new PVector( left, top, 0 );     		// UPPER LEFT  
				verts[1] = new PVector( right, top, 0 );      		// UPPER RIGHT 
				verts[2] = new PVector( right, bottom, 0 );       	// LOWER RIGHT
				verts[3] = new PVector( left, bottom, 0 );      	// LOWER LEFT
			}
			else if (metadata.orientation == 0)    		// Horizontal Image
			{
				verts[0] = new PVector( left, top, 0 );     		// UPPER LEFT  
				verts[1] = new PVector( right, top, 0 );      		// UPPER RIGHT 
				verts[2] = new PVector( right, bottom, 0 );       	// LOWER RIGHT
				verts[3] = new PVector( left, bottom, 0 );      	// LOWER LEFT
			}
			else if (metadata.orientation == 180)    	// Upside Down (Horizontal) Image
			{
				verts[0] = new PVector( right, bottom, 0 );       	// LOWER RIGHT
				verts[1] = new PVector( left, bottom, 0 );      	// LOWER LEFT
				verts[2] = new PVector( left, top, 0 );     		// UPPER LEFT  
				verts[3] = new PVector( right, top, 0 );      		// UPPER RIGHT 
			}
			else  if (metadata.orientation == 270)    	// Upside Down (Vertical) Image
			{
				verts[0] = new PVector( right, bottom, 0 );       	// LOWER RIGHT
				verts[1] = new PVector( left, bottom, 0 );      	// LOWER LEFT
				verts[2] = new PVector( left, top, 0 );     		// UPPER LEFT  
				verts[3] = new PVector( right, top, 0 );      		// UPPER RIGHT 
			}
		}
		else if(metadata.cameraModel == 1)				// Nikon image
		{
			if (metadata.orientation == 90 || metadata.orientation == 0)  				// Vertical or Horizontal Right-Side-Up Image
			{
				if (metadata.orientation == 90)
				{
					metadata.imageWidth = image.height;
					metadata.imageHeight = image.width;
				}

				verts[0] = new PVector( left, top, 0 );     		// UPPER LEFT  
				verts[1] = new PVector( right, top, 0 );      		// UPPER RIGHT 
				verts[2] = new PVector( right, bottom, 0 );       	// LOWER RIGHT
				verts[3] = new PVector( left, bottom, 0 );      	// LOWER LEFT
			}
			else if (metadata.orientation == 180 || metadata.orientation == 270)    		// Upside Down (Horizontal or Vertical) Image
			{
				if (metadata.orientation == 270 )
				{
					metadata.imageWidth = image.height;				// -- N.b. Editing metadata here
					metadata.imageHeight = image.width;
				}

				verts[0] = new PVector( right, bottom, 0 );       	// LOWER RIGHT
				verts[1] = new PVector( left, bottom, 0 );      	// LOWER LEFT
				verts[2] = new PVector( left, top, 0 );    			// UPPER LEFT  
				verts[3] = new PVector( right, top, 0 );      		// UPPER RIGHT
			}
		}

		return verts;
	}

	/**
	 * Find image width using formula:
	 * Image Width (m.) = Object Width on Sensor (mm.) / Focal Length (mm.) * Focus Distance (m.) 
	 * @return Image width in simulation (m.)
	 */
	private float getImageWidthInMeters()
	{
//		float state.subjectSizeRatio = subjectPixelWidth / original.imageWidth;		// --More accurate

		float objectWidthOnSensor = metadata.sensorSize * state.subjectSizeRatio;			// 29 * 0.18 == 5.22
		float imgWidth = objectWidthOnSensor * metadata.focusDistance / metadata.focalLength;		// 5.22 * 9 / 4.2 == 11.19	Actual: 11.320482

		return imgWidth;
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
	 * Set blur mask ID for image
	 * 	horizBorderID    0: Left  1: Center  2: Right  3: Left+Right
	 * 	vertBorderID	 0: Top  1: Center  2: Bottom  3: Top+Bottom
	 */
	public void setBlurMaskID()
	{
		if(state.horizBordersID == 0)
		{
			switch(state.vertBordersID)
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
		else if(state.horizBordersID == 1)
		{
			switch(state.vertBordersID)
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
		else if(state.horizBordersID == 2)
		{
			switch(state.vertBordersID)
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
		else if(state.horizBordersID == 3)
		{
			switch(state.vertBordersID)
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

	/**
	 * Set image state
	 * @param newState New image state
	 */
	public void setState(WMV_ImageState newState)
	{
		state = newState;						// Set state parameters
		setMediaState( state.getMediaState() );	// Set media state (general) parameters
		metadata = state.getMetadata();			// Set metadata parameters
	}

	/**
	 * @return Image state
	 */
	public WMV_ImageState getState()
	{
		return state;
	}

	/**
	 * @return Save image state for exporting
	 */
	public void captureState()
	{
		state.setMediaState( getMediaState(), metadata );
	}

	public WMV_ImageMetadata getMetadata()
	{
		return metadata;
	}

	/**
	 * Set blur mask
	 * @param newBlurMask New blur mask
	 */
	public void setBlurMask(PImage newBlurMask)
	{
		blurMask = newBlurMask;
	}

	/**
	 * Reset focus distance to original value
	 */
	public void resetFocusDistance(int frameCount)
	{
		float newFocusDistance = state.origFocusDistance;
		fadeFocusDistance(newFocusDistance, frameCount);
//		fadeFocusDistance(newFocusDistance, getWorldState().frameCount);
	}

	/**
	 * Set image compass direction
	 * @param newTheta
	 */
	public void setDirection(float newTheta)
	{
		metadata.theta = newTheta;
	}

	/**
	 * @return Image compass direction
	 */
	public float getDirection()
	{
		return metadata.theta;
	}

	/**
	 * @return Image elevation angle
	 */
	public float getElevationAngle()
	{
		return metadata.phi;
	}

	/**
	 * @return Image rotation angle
	 */
	public float getRotationAngle()
	{
		return metadata.rotation;
	}

	/**
	 * @return Image width
	 */
	public int getWidth()
	{
		return metadata.imageWidth;
	}

	/**
	 * @return Image height
	 */
	public int getHeight()
	{
		return metadata.imageHeight;
	}

	/**
	 * Set camera model
	 * @param newCameraModel New camera model ID (1: iPhone)
	 */
	public void setCameraModel(int newCameraModel)
	{
		metadata.cameraModel = newCameraModel;
	}

	/**
	 * @return Camera model ID
	 */
	public int getCameraModel()
	{
		return metadata.cameraModel;
	}

	/**
	 * Set image brightness metadata value
	 * @param newBrightness New brightness 
	 */
	public void setBrightness(float newBrightness)
	{
		metadata.brightness = newBrightness;
	}

	/**
	 * Get image brightness metadata value
	 */
	public float getBrightness()
	{
		return metadata.brightness;
	}

	/**
	 * @return If image is a placeholder, associated video ID, otherwise -1
	 */
	public int getAssociatedVideo()
	{
		if(state.isVideoPlaceHolder)
		{
			return state.assocVideoID;
		}
		else return -1;
	}

	/**
	 * @return Whether the image is a video placeholder
	 */
	public boolean isVideoPlaceHolder()
	{
		return state.isVideoPlaceHolder;
	}

	/**
	 * @return Image orientation metadata value {0: Landscape, 90: Portrait, 180: Landscape [flipped], 270 Portrait [flipped]}
	 */
	public float getOrientation()
	{
		return metadata.orientation;
	}

	/**
	 * @return Focus distance
	 */
	public float getFocusDistance()
	{
		return metadata.focusDistance;
	}

	/**
	 * @return Focal length
	 */
	public float getFocalLength()
	{
		return metadata.focalLength;
	}

	/**
	 * @return Camera sensor size
	 */
	public float getSensorSize()
	{
		return metadata.sensorSize;
	}

	public boolean hasOriginal()
	{
		return getMediaState().hasOriginal;
	}
	
	/**
	 * Set focus distance metadata value
	 * @param newFocusDistance New focus distance
	 */
	public void setFocusDistance(float newFocusDistance)
	{
		metadata.focusDistance = newFocusDistance;
	}

	/**
	 * Set focal length metadata value
	 * @param newFocalLength New focal length
	 */
	public void setFocalLength(float newFocalLength)
	{
		metadata.focalLength = newFocalLength;
	}

	/**
	 * Set sensor size
	 * @param newSensorSize New sensor size
	 */
	public void setSensorSize(float newSensorSize)
	{
		metadata.sensorSize = newSensorSize;
	}

	/**
	 * Set horizontal borders ID
	 * @param newHorizBordersID New ID
	 */
	public void setHorizBordersID(int newHorizBordersID)
	{
		state.horizBordersID = newHorizBordersID;
	}

	/**
	 * Set vertical borders ID
	 * @param newVertBordersID New ID
	 */
	public void setVertBordersID(int newVertBordersID)
	{
		state.vertBordersID = newVertBordersID;
	}

	/**
	 * Set blur mask ID
	 * @param newBlurMaskID New blur mask ID
	 */
	public void setBlurMaskID(int newBlurMaskID)
	{
		state.blurMaskID = newBlurMaskID;
	}

	/**
	 * Set image pixels 
	 * @param newImage New image
	 */
	public void setImage(PImage newImage)
	{
		image = newImage;
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
}
