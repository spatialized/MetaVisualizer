package main.java.com.entoptic.metaVisualizer.media;

import main.java.com.entoptic.metaVisualizer.MetaVisualizer;
import main.java.com.entoptic.metaVisualizer.metadata.WMV_PanoramaMetadata;
import main.java.com.entoptic.metaVisualizer.model.WMV_Time;
import main.java.com.entoptic.metaVisualizer.world.WMV_Field;
import main.java.com.entoptic.metaVisualizer.world.WMV_Viewer;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/**********************************************
 * Spherical 360 degree panorama in a 3D environment
 * @author davidgordon
 */
public class WMV_Panorama extends WMV_Media 
{
	/* Classes */
	private WMV_PanoramaState state;
	private WMV_PanoramaMetadata metadata;

	/* Graphics */
	public PImage texture;								// Texture image pixels
	public PImage blurMask;		// Blur mask
	public PImage blurred;			// Combined pixels 

	public PVector[] sphere;	
	private final float tablePrecision = 0.5f;
	private final int tableLength = (int)(360.0f / tablePrecision);
	private float sinTable[];
	private float cosTable[];

	public boolean initialized;

	/**
	 * Constructor for spherical panorama in 3D space
	 * @param newID Panorama id
	 * @param newType Media type
	 * @param newElevation Elevation angle (currently set to default of 0)
	 * @param newLocation Location (optional)
	 * @param newTexture Texture image file
	 * @param newPanoMetadata Panorama metadata
	 */
	public WMV_Panorama ( int newID, int newType, float newElevation, PVector newLocation, PImage newTexture, WMV_PanoramaMetadata newPanoMetadata )
	{
		super( newID, newType, newPanoMetadata.name, newPanoMetadata.filePath, newPanoMetadata.dateTime, newPanoMetadata.timeZone, 
				newPanoMetadata.gpsLocation, newPanoMetadata.longitudeRef, newPanoMetadata.latitudeRef );

		metadata = newPanoMetadata;
		state = new WMV_PanoramaState();
		state.initialize(metadata);

		texture = newTexture;

		if(newLocation != null)					// If location was passed in constructor
		{
			setLocation(newLocation);			// Set location
			setCaptureLocation(newLocation);	// Set capture location (Panorama capture location is identical to viewing location)
		}
		else									// Otherwise, set location from metadata
		{
			setLocation( new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z) );
		}

		initializeTime();

		state.phi = newElevation;              		// Elevation (Pitch angle) for stitched panoramas  	
		state.radius = state.defaultFocusDistance * state.initFocusDistanceFactor;
		state.origRadius = state.radius;
	}  

	/**
	 * Initialize panorama time
	 */
	public void initializeTime()
	{
		if(metadata.dateTime == null)
		{
			try {
				metadata.dateTime = parseDateTime(metadata.dateTimeString);
				time = new WMV_Time();
				time.initialize( metadata.dateTime, metadata.dateTimeString, getID(), 1, getAssociatedClusterID(), metadata.timeZone );
			} 
			catch (Throwable t) 
			{
				System.out.println("Error in panorama date / time... " + t);
			}
		}
		else
		{
			time = new WMV_Time();
			time.initialize( metadata.dateTime, metadata.dateTimeString, getID(), 1, getAssociatedClusterID(), metadata.timeZone );
		}
	}

	/**
	 * Calculate visibility based on viewer position
	 * @param viewer Given viewer
	 */
	public void calculateVisibility(WMV_Viewer viewer)
	{
		if(getViewerSettings() == null)
		{
			if(getDebugSettings().image || getDebugSettings().ml) 
				System.out.println("Panorama.calculateVisibility()... Fixing getSettings().. error in panorama #"+getID());
			
			updateWorldState(viewer.p.getSettings(), viewer.p.getState(), viewer.getSettings(), viewer.getState());
		}
		
		if(getViewerSettings().orientationMode)									// With StaticMode ON, determine visibility based on distance of associated vState.cluster 
		{
			for(int id : getViewerState().getClustersVisible())
			{
				if(getMediaState().getClusterID() == id)				
					setVisible(true);
			}
		}
		else 
		{
			setVisible(true);     										 		
		}

		setVisible(getDistanceBrightness(viewer) > 0.f);

		if(!isFadingOut() && getViewerSettings().hidePanoramas)
			setVisible(false);
	}
	
	/**
	 * Calculate visibility due to fading behavior
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

		if(visibilitySetToTrue && !isFading() && !hasFadedOut() && !getViewerSettings().hidePanoramas && getFadingBrightness() == 0.f)					// Fade in
			fadeIn(f);

		if(visibilitySetToFalse)
			fadeOut(f, false);

		if(hasFadedOut()) setFadedOut(false);
	}
	
	/**
	 * Display the panorama
	 */
	public void display(MetaVisualizer ml) 
	{
		ml.pushMatrix();
		if(!getViewerSettings().orientationMode)		
			ml.translate(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z);

		float r = state.radius;				
		int v0, v1, v2;

		ml.textureMode(PApplet.IMAGE);
		ml.noStroke();
		ml.beginShape(PApplet.TRIANGLE_STRIP);
		
		if(getWorldState().useBlurMasks)
			ml.texture(blurred);
		else
			ml.texture(texture);        			

		/* Set the panorama brightness */		
		if(getViewerSettings().selection)					// Viewer in selection mode
		{
			if(isSelected())
			{
				ml.tint(255, 255);          				
//				if(!getWorldState().alphaMode)
//					ml.tint(getViewingBrightness(), 255);          				
//				else
//					ml.tint(255, getViewingBrightness());          				
			}
			else
			{
				if(!getWorldState().alphaMode)
					ml.tint(getViewingBrightness() * 0.5f, 255);          // Set the image transparency					
				else
					ml.tint(255, getViewingBrightness() * 0.4f);          				
			}
		}
		else
		{
			if(!getWorldState().alphaMode)
				ml.tint(getViewingBrightness(), 255);          				
			else
				ml.tint(255, PApplet.map(getViewingBrightness(), 0.f, 255.f, 0.f, getWorldState().alpha));          				
		}

		float iu = (float)(texture.width-1)/(state.resolution);
		float iv = (float)(texture.height-1)/(state.resolution);
		
		float u = 0, v = iv;

		for (int i = 0; i < state.resolution; i++) 
		{
			ml.vertex(0, -r, 0,u,0);
			ml.vertex(sphere[i].x * r, sphere[i].y * r, sphere[i].z * r, u, v);
			u += iu;
		}

		ml.vertex(0, -r, 0, u, 0);
		ml.vertex(sphere[0].x * r, sphere[0].y * r, sphere[0].z * r, u, v);
		ml.endShape();   

		// Draw middle rings
		int voff = 0;
		for(int i = 2; i < state.resolution; i++) 
		{
			v1 = v0 = voff;
			voff += state.resolution;
			v2 = voff;
			u = 0;
			ml.beginShape(PApplet.TRIANGLE_STRIP);
			
			if(getWorldState().useBlurMasks)
				ml.texture(blurred);
			else
				ml.texture(texture);        			

			for(int j = 0; j < state.resolution; j++) 			// Draw ring
			{
				ml.vertex(sphere[v1].x * r, sphere[v1].y * r, sphere[v1++].z * r, u, v);
				ml.vertex(sphere[v2].x * r, sphere[v2].y * r, sphere[v2++].z * r, u, v + iv);
				u += iu;
			}

			// Close ring
			v1 = v0;
			v2 = voff;
			ml.vertex(sphere[v1].x * r, sphere[v1].y * r, sphere[v1].z * r, u, v);
			ml.vertex(sphere[v2].x * r, sphere[v2].y * r, sphere[v2].z * r, u, v + iv);
			ml.endShape();
			v += iv;
		}
		u = 0;

		// Draw northern cap
		ml.beginShape(PApplet.TRIANGLE_STRIP);
		
		if(getWorldState().useBlurMasks)
			ml.texture(blurred);
		else
			ml.texture(texture);        			

		for(int i = 0; i < state.resolution; i++) 
		{
			v2 = voff + i;
			ml.vertex(sphere[v2].x * r, sphere[v2].y * r, sphere[v2].z * r, u, v);
			ml.vertex(0, r, 0, u, v + iv);    
			u += iu;
		}

		ml.vertex(sphere[voff].x * r, sphere[voff].y * r, sphere[voff].z * r, u, v);
		ml.endShape();

		ml.popMatrix();
		ml.textureMode(PApplet.NORMAL);
	}

	/** 
	 * Draw the image
	 */
	public void display2D(MetaVisualizer ml)
	{
		System.out.print("Panorama.display2D()... id #"+getID());
		ml.noStroke(); 

		ml.pushMatrix();
		ml.beginShape(PApplet.POLYGON);    // Begin the shape containing the image
		ml.textureMode(PApplet.NORMAL);

		ml.noFill();
		ml.texture(texture);        			// Apply the image to the face as a texture 
		ml.tint(255, 255);          				

		int imgWidth = getWidth();
		int imgHeight = getHeight();

		ml.translate(-imgWidth / 2.f, -imgHeight / 2.f, -3000.f);

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
		PImage result = ml.createImage(state.getMetadata().imageWidth, state.getMetadata().imageHeight, PApplet.RGB);
		
		try
		{
			result = source.copy();
			result.mask(mask); 
		}
		catch(RuntimeException ex)
		{
			if(getDebugSettings().image || getDebugSettings().ml)
			{
				System.out.println("Panorama #"+getID()+" Error with Panorama Blur Mask... "+ex);
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
	 * Draw the panorama sphere
	 */
	public void displayModel(MetaVisualizer ml)
	{
		ml.pushMatrix();
		ml.translate(getLocation().x, getLocation().y, getLocation().z);
		ml.fill(215, 135, 255, getViewingBrightness());
		ml.sphere(state.radius);								// -- Testing
		ml.popMatrix();
	}

	/**
	 * Initialize sphere vertices
	 */
	public void initializeSphere()
	{
		sinTable = new float[tableLength];
		cosTable = new float[tableLength];

		for (int i = 0; i < tableLength; i++) {
			sinTable[i] = (float) Math.sin(i * PApplet.DEG_TO_RAD * tablePrecision);
			cosTable[i] = (float) Math.cos(i * PApplet.DEG_TO_RAD * tablePrecision);
		}

		float delta = (float)tableLength/state.resolution;
		float[] cx = new float[state.resolution];
		float[] cz = new float[state.resolution];

		for (int i = 0; i < state.resolution; i++) 		// Calc unit circle in XZ plane
		{
			cx[i] = -cosTable[(int) (i*delta) % tableLength];
			cz[i] = sinTable[(int) (i*delta) % tableLength];
		}

		int vertCount = state.resolution * (state.resolution-1) + 2;			// Computing vertexlist, starting at south pole
		int currVert = 0;

		sphere = new PVector[vertCount];			// Initialize sphere vertices array

		float angle_step = (tableLength*0.5f)/state.resolution;
		float angle = angle_step;

		// Step along Y axis
		for (int i = 1; i < state.resolution; i++) 
		{
			float curRadius = sinTable[(int) angle % tableLength];
			float currY = -cosTable[(int) angle % tableLength];

			for (int j = state.resolution-1; j >= 0; j--) 
				sphere[currVert++] = new PVector(cx[j] * curRadius, currY, cz[j] * curRadius);

			angle += angle_step;
		}

		sphere[currVert++] = new PVector(0,0,0);
		sphere[currVert++] = new PVector(0,0,0);

		if (state.phi != 0.f)
			sphere = rotateVertices(sphere, -state.phi, getMediaState().rotationAxis);     // Rotate around X axis		-- Why diff. axis than for images?

		if( getDirection() != 0.f )
			sphere = rotateVertices(sphere, 360-getDirection(), getMediaState().azimuthAxis); // Rotate around Z axis

		initialized = true;
	}

	/**
	 * Request the image to be loaded from disk
	 */
	public void loadMedia(MetaVisualizer ml)
	{
		texture = ml.requestImage(getFilePath());
		setRequested(true);
	}

	/** 
	 * @return Distance visibility multiplier between 0. and 1.
	 * Find panorama brightness due to distance (fades away in distance and as camera gets close)
	 */
	public float getDistanceBrightness(WMV_Viewer viewer)							
	{
		float viewDist = getViewingDistance(viewer);

		float distVisibility = 1.f;

		if(viewDist > getWorldSettings().clusterCenterSize * 1.5f)
		{
			float vanishingPoint = state.radius - getWorldSettings().clusterCenterSize;			// Distance where transparency reaches zero
			if(viewDist < vanishingPoint)
				distVisibility = PApplet.constrain(1.f - PApplet.map(viewDist, getWorldSettings().clusterCenterSize * 1.5f, vanishingPoint, 0.f, 1.f), 0.f, 1.f);    // Fade out until cam.visibleFarDistance
			else
				distVisibility = 0.f;
		}

		return distVisibility;
	}

	/**
	 * @return Distance from the panorama to the camera
	 */
	public float getViewingDistance(WMV_Viewer viewer)       // Find distance from camera to point in virtual space where photo appears           
	{
		PVector camLoc = viewer.getLocation();
		return PVector.dist(getCaptureLocation(), camLoc);
	}

	/**
	 * Draw the panorama metadata in Heads-Up Display
	 */
	public void displayMetadata(MetaVisualizer ml)
	{
		String strTitleImage = "Panorama";
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
		String strElevation = "Vertical Angle: "+String.valueOf(state.phi);

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

		if(getDebugSettings().panorama)
		{
			ml.display.metadata(frameCount, strTitleDebug);
			ml.display.metadata(frameCount, strBrightness);
			ml.display.metadata(frameCount, strBrightnessFading);
		}
	}

	/**
	 * Set panorama state
	 * @param newState New panorama state
	 */
	public void setState(WMV_PanoramaState newState)
	{
		state = newState;							// Set state parameters
		setMediaState( state.getMediaState() );		// Set media state (general) parameters
		metadata = state.getMetadata();				// Set metadata parameters
	}

	/**
	 * @return Panorama state
	 */
	public WMV_PanoramaState getState()
	{
		return state;
	}

	/**
	 * @return Save panorama state for exporting
	 */
	public void captureState()
	{
		state.setMediaState( getMediaState(), metadata );
	}

	/**
	 * @return Panorama metadata
	 */
	public WMV_PanoramaMetadata getMetadata()
	{
		return metadata;
	}

	/**
	 * Set south pole blur mask
	 * @param newBlurMask New blur mask
	 */
	public void setBlurMask(PImage newBlurMask)
	{
		blurMask = newBlurMask;
	}

	/**
	 * Set radius to original length
	 */
	public void resetRadius()
	{
		setRadius(state.origRadius);
	}

	/**
	 * Set radius length
	 * @param newRadius New radius length
	 */
	public void setRadius(float newRadius)
	{
		state.radius = newRadius;
	}

	public void setDirection(float newTheta)
	{
		metadata.theta = newTheta;
	}

	public float getDirection()
	{
		return metadata.theta;
	}

	/**
	 * Set elevation angle
	 * @param newPhi New phi value
	 */
	public void setElevationAngle( float newPhi )
	{
		state.phi = newPhi;
	}

	/**
	 * Get elevation angle
	 */
	public float getElevationAngle()
	{
		return state.phi;
	}

	/**
	 * @return Texture width
	 */
	public int getWidth()
	{
		return metadata.imageWidth;
	}

	/**
	 * @return Texture height
	 */
	public int getHeight()
	{
		return metadata.imageHeight;
	}

	/**
	 * @return Sphere radius
	 */
	public float getRadius()
	{
		return state.radius;
	}

	/**
	 * @return Original sphere radius
	 */
	public float getOrigRadius()
	{
		return state.origRadius;
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
	 * Set texture brightness metadata value
	 * @param newBrightness New brightness value
	 */
	public void setBrightness(float newBrightness)
	{
		metadata.brightness = newBrightness;
	}

	/**
	 * Get texture brightness from metadata
	 * @return Brightness value
	 */
	public float getBrightness()
	{
		return metadata.brightness;
	}

	public void setTexture(PImage newTexture)
	{
		texture = newTexture;
	}
	
	public String getFilePath()
	{
		return getMetadata().filePath;
	}

	public void setFilePath(String newFilePath)
	{
		metadata.filePath = newFilePath;
	}

	/**
	 * Update file path
	 * @param ml
	 * @param parentField
	 */
	public void updateFilePath(MetaVisualizer ml, WMV_Field parentField)
	{
		String oldFilePath = getFilePath();
		String[] parts = oldFilePath.split("/");

		parts[parts.length-4] = ml.library.getName(true);			// Library name
		parts[parts.length-3] = parentField.getName();					// Field name
		
		String newFilePath = parts[0];
		for(int i=1; i<parts.length; i++)
			newFilePath = newFilePath + "/" + parts[i];
		System.out.println("Panorama.updateFilePath()... Will set panorama path to:"+newFilePath);
		setFilePath(newFilePath);
	}
}
