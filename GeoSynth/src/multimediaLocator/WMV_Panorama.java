package multimediaLocator;

import java.time.ZonedDateTime;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/**********************************************
 * @author davidgordon
 * A 360-degree panorama in 3D virtual space
 */
public class WMV_Panorama extends WMV_Viewable 
{
	/* Classes */
	private WMV_PanoramaState state;
	
	/* Graphics */
	public PImage texture;								// Texture image pixels
	private boolean initialized;

	public PVector[] sphere;	
	private final float tablePrecision = 0.5f;
	private final int tableLength = (int)(360.0f / tablePrecision);
	private float sinTable[];
	private float cosTable[];
	
	WMV_Panorama ( int newID, int newMediaType, String newName, String newFilePath, PVector newGPSLocation, float newTheta, 
			float newElevation, int newCameraModel, int newWidth, int newHeight, float newBrightness, ZonedDateTime newDateTime, String newTimeZone,
			PVector newLocation, PImage newTexture )
	{
		super(newID, newMediaType, newName, newFilePath, newGPSLocation, newTheta, newCameraModel, newBrightness, newDateTime, newTimeZone);

		state = new WMV_PanoramaState();

		texture = newTexture;
		state.imageWidth = newWidth;
		state.imageHeight = newHeight;

		if(newLocation != null)
		{
			setLocation(newLocation);
			setCaptureLocation(newLocation);
		}
		
		if(newDateTime != null)
			time = new WMV_Time( newDateTime, getID(), getClusterID(), 1, newTimeZone );
		else
			time = null;

		state.phi = newElevation;              									// Elevation (Pitch angle) calculated from images 
		state.radius = state.defaultFocusDistance * state.initFocusDistanceFactor;
		state.origRadius = state.radius;
	}  

	/**
=	 * Update main variables
	 */
	public void update(MultimediaLocator ml)
	{
		if(getMediaState().requested && texture.width != 0)			// If requested image has loaded, initialize image 
		{
			initializeSphere();					
			setRequested(false);
//			p.p.vState.requestedPanoramas--;
		}

		if(getCaptureDistance() < getViewerSettings().getFarViewingDistance() && !getMediaState().requested)
			if(!initialized)
				loadMedia(ml); 

		if(texture.width > 0 && !isDisabled())			
		{
			if(getViewerSettings().orientationMode)									// With StaticMode ON, determine visibility based on distance of associated vState.cluster 
			{
				for(int id : getViewerState().getClustersVisible())
				{
					if(getMediaState().cluster == id)				// If this photo's cluster is on next closest list, it is visible	-- CHANGE THIS??!!
						setVisible(true);
				}
			}
			else 
			{
				setVisible(true);     										 		
			}
			
			setVisible(getDistanceBrightness() > 0.f);

			if(!isFading() && getViewerSettings().hidePanoramas)
				setVisible(false);

			if(isVisible() && !isFading() && !hasFadedOut() && !getViewerSettings().hidePanoramas && getFadingBrightness() == 0.f)					// Fade in
			{
				if(getDebugSettings().panorama)
					System.out.println("fadeIn()...pano id:"+getID());
				fadeIn();
			}

			if(hasFadedOut()) setFadedOut(false);
		}
		
		if(isFading())                       // Fade in and out with time
		{
//			if(getDebugSettings().panorama && getDebugSettings().detailed)
//				p.p.p.display.message(p.p, "Panorama fading... id: "+getID());
			updateFadingBrightness();

			if(getFadingBrightness() == 0.f)
				setVisible(false);
		}

		//		if(fadingObjectDistance)
		//		{
		//			updateFadingObjectDistance();
		//		}
	}

	/**
	 * Display the image or spherical panorama in virtual space
	 */
	public void draw(WMV_World world)
	{
		if(getMediaState().showMetadata) displayMetadata(world);

		float brightness = getFadingBrightness();					
		brightness *= getViewerSettings().userBrightness;

		float distanceBrightnessFactor = getDistanceBrightness(); 
		brightness *= distanceBrightnessFactor; 						// Fade brightness based on distance to camera

		if( getWorldState().timeFading && time != null && !getViewerState().isMoving() )
			brightness *= getTimeBrightness(); 					// Fade brightness based on time
		
		setViewingBrightness( PApplet.map(brightness, 0.f, 1.f, 0.f, 255.f) );				// Scale to setting for alpha range

		if (isVisible() && !isHidden() && !isDisabled()) 
		{
			if (getViewingBrightness() > 0)
			{
				if(texture.width > 0)		// If image has been loaded
				{
					drawPanorama(world);
				}
			}
		} 
//		else
//		{      
//			world.p.noFill();                  // Hide image if it isn't visible
//		}

		if(isVisible() && getWorldState().showModel && !isHidden() && !isDisabled())
			displayModel(world);

//		if (isVisible() && isSelected() && !isDisabled() && getDebugSettings().model)		// Draw panorama vState.location for debugging or map display
//			displayModel();
//		if (isVisible() && !isDisabled() && getViewerSettings().map3DMode)
//			displayModel();
	}

	/**
	 * Draw the panorama sphere
	 */
	void displayModel(WMV_World world)
	{
		world.p.pushMatrix();
		world.p.translate(getLocation().x, getLocation().y, getLocation().z);
		world.p.fill(215, 135, 255, getViewingBrightness());
		world.p.sphere(state.radius);								// -- Testing
		world.p.popMatrix();
	}
	
	/**
	 * Fade in panorama
	 */
	public void fadeIn()
	{
		if(isFading() || isFadingIn() || isFadingOut())		// If already fading, stop at current value
			stopFading();

		fadeBrightness(1.f);					// Fade in
	}

	/**
	 * Fade out panorama
	 */
	public void fadeOut()
	{
		if(isFading() || isFadingIn() || isFadingOut())		// If already fading, stop at current value
				stopFading();

		fadeBrightness(0.f);					// Fade out
	}

	/**
	 * Draw the panorama
	 */
	private void drawPanorama(WMV_World world) 
	{
		world.p.pushMatrix();
		world.p.translate(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z);	// CHANGE VALUES!

		float r = state.radius;				
		int v0,v1,v2;

		world.p.textureMode(PApplet.IMAGE);
		world.p.noStroke();
		world.p.beginShape(PApplet.TRIANGLE_STRIP);
		world.p.texture(texture);

		/* Set the panorama brightness */		
		if(getViewerSettings().selection)					// Viewer in selection mode
		{
			if(isSelected())
			{
//				System.out.println("selected getViewingBrightness():"+getViewingBrightness());
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
					world.p.tint(255, getViewingBrightness() * 0.33f);          				
			}
		}
		else
		{
			if(!getWorldState().alphaMode)
			{
				world.p.tint(getViewingBrightness(), 255);          				
			}
			else
			{
//				System.out.println("alphaMode getViewingBrightness():"+getViewingBrightness()+" final alpha:"+PApplet.map(getViewingBrightness(), 0.f, 255.f, 0.f, world.alpha));
				world.p.tint(255, PApplet.map(getViewingBrightness(), 0.f, 255.f, 0.f, getWorldState().alpha));          				
			}
		}
		
		float iu = (float)(texture.width-1)/(state.resolution);
		float iv = (float)(texture.height-1)/(state.resolution);
		float u = 0, v = iv;

		for (int i = 0; i < state.resolution; i++) 
		{
			world.p.vertex(0, -r, 0,u,0);
			world.p.vertex(sphere[i].x * r, sphere[i].y * r, sphere[i].z * r, u, v);
			u += iu;
		}

		world.p.vertex(0, -r, 0, u, 0);
		world.p.vertex(sphere[0].x * r, sphere[0].y * r, sphere[0].z * r, u, v);
		world.p.endShape();   

		// Draw middle rings
		int voff = 0;
		for(int i = 2; i < state.resolution; i++) 
		{
			v1 = v0 = voff;
			voff += state.resolution;
			v2 = voff;
			u = 0;
			world.p.beginShape(PApplet.TRIANGLE_STRIP);
			world.p.texture(texture);
			for(int j = 0; j < state.resolution; j++) 			// Draw ring
			{
				world.p.vertex(sphere[v1].x * r, sphere[v1].y * r, sphere[v1++].z * r, u, v);
				world.p.vertex(sphere[v2].x * r, sphere[v2].y * r, sphere[v2++].z * r, u, v + iv);
				u += iu;
			}

			// Close ring
			v1 = v0;
			v2 = voff;
			world.p.vertex(sphere[v1].x * r, sphere[v1].y * r, sphere[v1].z * r, u, v);
			world.p.vertex(sphere[v2].x * r, sphere[v2].y * r, sphere[v2].z * r, u, v + iv);
			world.p.endShape();
			v += iv;
		}
		u = 0;

		// Draw northern cap
		world.p.beginShape(PApplet.TRIANGLE_STRIP);
		world.p.texture(texture);
		for(int i = 0; i < state.resolution; i++) 
		{
			v2 = voff + i;
			world.p.vertex(sphere[v2].x * r, sphere[v2].y * r, sphere[v2].z * r, u, v);
			world.p.vertex(0, r, 0, u, v + iv);    
			u += iu;
		}

		world.p.vertex(sphere[voff].x * r, sphere[voff].y * r, sphere[voff].z * r, u, v);
		world.p.endShape();

		world.p.popMatrix();
		world.p.textureMode(PApplet.NORMAL);
	}

	/***
	 * Initialize panorama geometry
	 */
	void initializeSphere()
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
		
//		if (state.phi != 0.f)
//		sphere = rotateVertices(sphere, -state.phi, verticalAxis);         // Rotate around X axis
		
		if (state.phi != 0.f)
			sphere = rotateVertices(sphere, -state.phi, getMediaState().rotationAxis);     // Rotate around X axis		-- Why diff. axis than for images?

		if( getTheta() != 0.f )
			sphere = rotateVertices(sphere, 360-getTheta(), getMediaState().azimuthAxis); // Rotate around Z axis
		
//		panoramaDetail = panoramaDetail;
		initialized = true;
	}

	/**
	 * Request the image to be loaded from disk
	 */
	public void loadMedia(MultimediaLocator ml)
	{
//		if(getDebugSettings().panorama && getDebugSettings().detailed)
//			p.p.p.display.message(p.p, "Requesting panorama file:"+getName());
//
//		if(!getDebugSettings().lowMemory)			// Check enough memory available
//		{
		
//			if(getViewerSettings().orientationMode)
//				vState.location = p.p.viewer.getLocation();
//			else
//				vState.location = new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z);

//		vState.location = new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z);
//			if (p.utilities.isNaN(vState.location.x) || p.utilities.isNaN(vState.location.x) || p.utilities.isNaN(vState.location.x))
//			{
//				vState.location = new PVector (0, 0, 0);
//			}

			texture = ml.requestImage(getFilePath());
			setRequested(true);
//			p.p.vState.requestedPanoramas++;
//		}
	}

	/** 
	 * @return Distance visibility multiplier between 0. and 1.
	 * Find panorama brightness due to distance (fades away in distance and as camera gets close)
	 */
	public float getDistanceBrightness()									
	{
		float viewDist = getViewingDistance();
//		float farViewingDistance = getViewerSettings().getFarViewingDistance();

		float distVisibility = 1.f;

		if(viewDist > state.radius-getWorldSettings().clusterCenterSize*3.f)
		{
			float vanishingPoint = state.radius;	// Distance where transparency reaches zero
			if(viewDist < vanishingPoint)
				distVisibility = PApplet.constrain(1.f - PApplet.map(viewDist, vanishingPoint-getWorldSettings().clusterCenterSize*3.f, vanishingPoint, 0.f, 1.f), 0.f, 1.f);    // Fade out until cam.visibleFarDistance
			else
				distVisibility = 0.f;
		}

		return distVisibility;
	}

	/**
	 * @return Distance from the panorama to the camera
	 */
	public float getViewingDistance()       // Find distance from camera to point in virtual space where photo appears           
	{
		PVector camLoc;

		if(getViewerSettings().orientationMode)
			camLoc = getViewerState().getLocation();
		else
			camLoc = getViewerState().getLocation();

		float distance;

		distance = PVector.dist(getCaptureLocation(), camLoc);

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

		if(getClusterID() != -1)
			return true;
		else
			return false;
	}
	
	/**
	 * Draw the panorama metadata in Heads-Up Display
	 */
	public void displayMetadata(WMV_World world)
	{
		String strTitleImage = "Panorama";
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
		String strElevation = "Vertical Angle: "+String.valueOf(state.phi);

		String strTitleDebug = "--- Debugging ---";
		String strBrightness = "brightness: "+String.valueOf(getViewingBrightness());
		String strBrightnessFading = "brightnessFadingValue: "+String.valueOf(getFadingBrightness());
		
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

		if(getDebugSettings().panorama)
		{
			world.p.display.metadata(world, strTitleDebug);
			world.p.display.metadata(world, strBrightness);
			world.p.display.metadata(world, strBrightnessFading);
		}
	}

	public void setState(WMV_PanoramaState newState)
	{
		state = newState;
	}
	
	public WMV_PanoramaState getState()
	{
		return state;
	}
	
	 public void captureState()
	 {
		 state.setViewableState( getMediaState() );
	 }
	 
	
	public void setDirection( float newTheta )
	{
		setTheta(newTheta);
	}

	void resetRadius()
	{
		setRadius(state.origRadius);
	}

	void setRadius(float newRadius)
	{
		state.radius = newRadius;
	}
	
	public float getDirection()
	{
		return getMediaState().theta;
	}

	public float getWidth()
	{
		return state.imageWidth;
	}

	public float getHeight()
	{
		return state.imageHeight;
	}
	
	public float getRadius()
	{
		return state.radius;
	}
	
	public float getOrigRadius()
	{
		return state.origRadius;
	}
}
