package multimediaLocator;

import java.time.ZonedDateTime;
import java.util.ArrayList;
//import java.util.Calendar;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/**********************************************
 * @author davidgordon
 * A 360-degree panorama in 3D virtual space
 */
public class WMV_Panorama extends WMV_Viewable 
{
//	/* Classes */
//	WMV_WorldSettings worldSettings;
//	WMV_ViewerSettings viewerSettings;	// Update world settings
//	ML_DebugSettings debugSettings;	// Update world settings

	/* Graphics */
	public PImage texture;								// Texture image pixels
	private boolean initialized;
	public final float panoramaFocusDistanceFactor = 1.1f;	// Scaling from defaultFocusDistance to panorama radius
	
	/* EXIF Metadata */
	private float imageWidth, imageHeight;		// Width and height
//	public int origWidth, origHeight;			// Original width and height

	/* Derived Metadata */
	private float phi = 0.f;
	
	/* Panorama */
	private PVector[] sphere;	
	public int resolution = 50;  										// Sphere detail setting
	private float sinTable[];
	private float cosTable[];
	private float tablePrecision = 0.5f;
	private int tableLength = (int)(360.0f / tablePrecision);
	private float defaultFocusDistance = 9.0f;			// Default focus distance for images and videos (m.)
	public float radius;
	public float origRadius;
	
	WMV_Panorama ( int newID, int newMediaType, String newName, String newFilePath, PVector newGPSLocation, float newTheta, 
			float newElevation, int newCameraModel, int newWidth, int newHeight, float newBrightness, ZonedDateTime newDateTime, String newTimeZone,
			PVector newLocation, PImage newTexture )
	{
		super(newID, newMediaType, newName, newFilePath, newGPSLocation, newTheta, newCameraModel, newBrightness, newDateTime, newTimeZone);

//		if(newTexture == null)
//			texture = p.p.p.createImage(0,0,processing.core.PConstants.RGB);		// Create empty image
//		else
			texture = newTexture;

		imageWidth = newWidth;
		imageHeight = newHeight;

		filePath = newFilePath;

		if(newLocation != null)
		{
			location = newLocation;
			captureLocation = newLocation;
		}
		
		gpsLocation = newGPSLocation;
		cameraModel = newCameraModel;

		if(newDateTime != null)
			time = new WMV_Time( newDateTime, getID(), cluster, 1, newTimeZone );
		else
			time = null;

		theta = newTheta;              										// Orientation (Yaw angle) calculated from images 
		phi = newElevation;              									// Elevation (Pitch angle) calculated from images 
		
		radius = defaultFocusDistance * panoramaFocusDistanceFactor;
		origRadius = radius;
	}  

	/**
=	 * Update main variables
	 */
	public void update(MultimediaLocator ml)
	{
		if(requested && texture.width != 0)			// If requested image has loaded, initialize image 
		{
			initializeSphere();					

			requested = false;
//			p.p.requestedPanoramas--;
		}

		if(getCaptureDistance() < viewerSettings.getFarViewingDistance() && !requested)
			if(!initialized)
				loadMedia(ml); 

		if(texture.width > 0 && !disabled)			
		{
			if(viewerSettings.orientationMode)									// With StaticMode ON, determine visibility based on distance of associated cluster 
			{
				for(int id : viewerState.getClustersVisible())
				{
					if(cluster == id)				// If this photo's cluster is on next closest list, it is visible	-- CHANGE THIS??!!
						visible = true;
				}
			}
			else 
			{
				visible = true;     										 		
			}
			
			visible = (getDistanceBrightness() > 0.f);

			if(!fading && viewerSettings.hidePanoramas)
				visible = false;

			if(visible && !fading && !fadedOut && !viewerSettings.hidePanoramas && fadingBrightness == 0.f)					// Fade in
			{
				if(debugSettings.panorama)
					PApplet.println("fadeIn()...pano id:"+getID());
				fadeIn();
			}

			if(fadedOut) fadedOut = false;
		}
		
		if(isFading())                       // Fade in and out with time
		{
//			if(debugSettings.panorama && debugSettings.detailed)
//				p.p.p.display.message(p.p, "Panorama fading... id: "+getID());
			updateFadingBrightness();

			if(fadingBrightness == 0.f)
				visible = false;
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
		if(showMetadata) displayMetadata(world);

		float brightness = fadingBrightness;					
		brightness *= viewerSettings.userBrightness;

		float distanceBrightnessFactor = getDistanceBrightness(); 
		brightness *= distanceBrightnessFactor; 						// Fade brightness based on distance to camera

		if( worldState.timeFading && time != null && !viewerState.isMoving() )
			brightness *= getTimeBrightness(); 					// Fade brightness based on time
		
		viewingBrightness = PApplet.map(brightness, 0.f, 1.f, 0.f, 255.f);	  // Fade panoramas with distance  -- CHANGE THIS / UNNECESSARY?

		if (visible && !hidden && !disabled) 
		{
			if (viewingBrightness > 0)
			{
				if(texture.width > 0 && !viewerSettings.map3DMode)		// If image has been loaded
				{
					drawPanorama(world);
				}
			}
		} 
		else
		{      
			world.p.noFill();                  // Hide image if it isn't visible
		}

		if(visible && worldState.showModel && !hidden && !disabled)
			displayModel(world);

//		if (visible && isSelected() && !disabled && debugSettings.model)		// Draw panorama location for debugging or map display
//			displayModel();
//		if (visible && !disabled && viewerSettings.map3DMode)
//			displayModel();
	}

	/**
	 * Draw the panorama sphere
	 */
	void displayModel(WMV_World world)
	{
		world.p.pushMatrix();
		world.p.translate(location.x, location.y, location.z);
		world.p.fill(215, 135, 255, viewingBrightness);
		world.p.sphere(radius);								// -- Testing
		world.p.popMatrix();
	}
	
	/**
	 * Fade in panorama
	 */
	public void fadeIn()
	{
		if(fading || isFadingIn || isFadingOut)		// If already fading, stop at current value
				stopFading();

		fadeBrightness(1.f);					// Fade in
	}

	/**
	 * Fade out panorama
	 */
	public void fadeOut()
	{
		if(fading || isFadingIn || isFadingOut)		// If already fading, stop at current value
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

		float r = radius;				
		int v0,v1,v2;

		world.p.textureMode(PApplet.IMAGE);
		world.p.noStroke();
		world.p.beginShape(PApplet.TRIANGLE_STRIP);
		world.p.texture(texture);

		/* Set the panorama brightness */		
		if(viewerSettings.selection)					// Viewer in selection mode
		{
			if(isSelected())
			{
//				PApplet.println("selected viewingBrightness:"+viewingBrightness);
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
					world.p.tint(255, viewingBrightness * 0.33f);          				
			}
		}
		else
		{
			if(!worldState.alphaMode)
			{
				world.p.tint(viewingBrightness, 255);          				
			}
			else
			{
//				PApplet.println("alphaMode viewingBrightness:"+viewingBrightness+" final alpha:"+PApplet.map(viewingBrightness, 0.f, 255.f, 0.f, world.alpha));
				world.p.tint(255, PApplet.map(viewingBrightness, 0.f, 255.f, 0.f, worldState.alpha));          				
			}
		}
		
		float iu = (float)(texture.width-1)/(resolution);
		float iv = (float)(texture.height-1)/(resolution);
		float u = 0, v = iv;

		for (int i = 0; i < resolution; i++) 
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
		for(int i = 2; i < resolution; i++) 
		{
			v1 = v0 = voff;
			voff += resolution;
			v2 = voff;
			u = 0;
			world.p.beginShape(PApplet.TRIANGLE_STRIP);
			world.p.texture(texture);
			for(int j = 0; j < resolution; j++) 			// Draw ring
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
		for(int i = 0; i < resolution; i++) 
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

		float delta = (float)tableLength/resolution;
		float[] cx = new float[resolution];
		float[] cz = new float[resolution];

		for (int i = 0; i < resolution; i++) 		// Calc unit circle in XZ plane
		{
			cx[i] = -cosTable[(int) (i*delta) % tableLength];
			cz[i] = sinTable[(int) (i*delta) % tableLength];
		}

		int vertCount = resolution * (resolution-1) + 2;			// Computing vertexlist, starting at south pole
		int currVert = 0;

		sphere = new PVector[vertCount];			// Initialize sphere vertices array

		float angle_step = (tableLength*0.5f)/resolution;
		float angle = angle_step;

		// Step along Y axis
		for (int i = 1; i < resolution; i++) 
		{
			float curRadius = sinTable[(int) angle % tableLength];
			float currY = -cosTable[(int) angle % tableLength];

			for (int j = resolution-1; j >= 0; j--) 
				sphere[currVert++] = new PVector(cx[j] * curRadius, currY, cz[j] * curRadius);

			angle += angle_step;
		}

		sphere[currVert++] = new PVector(0,0,0);
		sphere[currVert++] = new PVector(0,0,0);
		
//		if (phi != 0.f)
//		sphere = rotateVertices(sphere, -phi, verticalAxis);         // Rotate around X axis
		
		if (phi != 0.f)
			sphere = rotateVertices(sphere, -phi, rotationAxis);     // Rotate around X axis		-- Why diff. axis than for images?

		if( theta != 0.f )
			sphere = rotateVertices(sphere, 360-theta, azimuthAxis); // Rotate around Z axis
		
//		panoramaDetail = panoramaDetail;
		initialized = true;
	}

	/**
	 * Request the image to be loaded from disk
	 */
	public void loadMedia(MultimediaLocator ml)
	{
//		if(debugSettings.panorama && debugSettings.detailed)
//			p.p.p.display.message(p.p, "Requesting panorama file:"+getName());
//
//		if(!debugSettings.lowMemory)			// Check enough memory available
//		{
		
//			if(viewerSettings.orientationMode)
//				location = p.p.viewer.getLocation();
//			else
//				location = new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z);

//		location = new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z);
//			if (p.utilities.isNaN(location.x) || p.utilities.isNaN(location.x) || p.utilities.isNaN(location.x))
//			{
//				location = new PVector (0, 0, 0);
//			}

			texture = ml.requestImage(filePath);
			requested = true;
//			p.p.requestedPanoramas++;
//		}
	}

	/** 
	 * @return Distance visibility multiplier between 0. and 1.
	 * Find panorama brightness due to distance (fades away in distance and as camera gets close)
	 */
	public float getDistanceBrightness()									
	{
		float viewDist = getViewingDistance();
		float farViewingDistance = viewerSettings.getFarViewingDistance();

		float distVisibility = 1.f;

		if(viewDist > radius-worldSettings.clusterCenterSize*3.f)
		{
			float vanishingPoint = radius;	// Distance where transparency reaches zero
			if(viewDist < vanishingPoint)
				distVisibility = PApplet.constrain(1.f - PApplet.map(viewDist, vanishingPoint-worldSettings.clusterCenterSize*3.f, vanishingPoint, 0.f, 1.f), 0.f, 1.f);    // Fade out until cam.visibleFarDistance
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

		if(viewerSettings.orientationMode)
			camLoc = viewerState.getLocation();
		else
			camLoc = viewerState.getLocation();

		float distance;

		distance = PVector.dist(getCaptureLocation(), camLoc);

		return distance;
	}
	
	/**
	 * @return Whether associated cluster was successfully found
	 * Search given list of clusters and associate with this panorama
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
	 * Draw the panorama metadata in Heads-Up Display
	 */
	public void displayMetadata(WMV_World world)
	{
		String strTitleImage = "Panorama";
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

		if(debugSettings.panorama)
		{
			world.p.display.metadata(world, strTitleDebug);
			world.p.display.metadata(world, strBrightness);
			world.p.display.metadata(world, strBrightnessFading);
		}
	}

	public void setDirection( float newTheta )
	{
		theta = newTheta;
	}

	void resetRadius()
	{
		setRadius(origRadius);
	}

	void setRadius(float newRadius)
	{
		radius = newRadius;
	}
	
	/**
	 * @param newGPSLocation New GPS location
	 * Set the current GPS location
	 */
//	void setGPSLocation(PVector newGPSLocation) 
//	{
//		gpsLocation = newGPSLocation;
//		calculateCaptureLocation();
//	}

	public PVector getLocation()
	{
		return location;
	}
	
	public float getDirection()
	{
		return theta;
	}

	public float getWidth()
	{
		return imageWidth;
	}

	public float getHeight()
	{
		return imageHeight;
	}
	
	public float getRadius()
	{
		return radius;
	}
	
	public float getOrigRadius()
	{
		return origRadius;
	}
}
