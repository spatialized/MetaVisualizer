package gmViewer;

import java.util.ArrayList;
import java.util.Calendar;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/**********************************************
 * GMV_Panorama
 * @author davidgordon
 * Represents a 360 degree panorama in virtual space
 */
public class GMV_Panorama extends GMV_Viewable 
{
	/* Graphics */
	PImage texture;								// Texture image pixels
	boolean initialized;
	
	/* EXIF Metadata */
	private float imageWidth, imageHeight;		// Width and height
//	public int origWidth, origHeight;			// Original width and height

	/* Derived Metadata */
	private float phi = 0.f;
	
	/* Panorama */
//	float[] sphereX, sphereY, sphereZ;								// Sphere vertices
	PVector[] sphere;		
	int panoramaDetail = 50;  										// Sphere detail setting
	float sinTable[];
	float cosTable[];
	float tablePrecision = 0.5f;
	int tableLength = (int)(360.0f / tablePrecision);

	GMV_Panorama ( GMV_Field parent, int newID, String newName, String newFilePath, PVector newGPSLocation, float newTheta, 
			float newElevation, int newCameraModel, int newWidth, int newHeight, float newBrightness, Calendar newCalendar, 
			PVector newLocation, PImage newTexture )
	{
		super(parent, newID, newName, newFilePath, newGPSLocation, newTheta, newCameraModel, newBrightness, newCalendar);

		p = parent;

		if(newTexture == null)
			texture = p.p.p.createImage(0,0,processing.core.PConstants.RGB);		// Create empty image
		else
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

		theta = newTheta;              										// Orientation (Yaw angle) calculated from images 
		phi = newElevation;              									// Elevation (Pitch angle) calculated from images 
	}  

	/**
=	 * Update main variables
	 */
	public void update()
	{
		if(requested && texture.width != 0)			// If requested image has loaded, initialize image 
		{
			initializeSphere(panoramaDetail);					

			requested = false;
			p.p.requestedPanoramas--;
		}

		if(getCaptureDistance() < p.p.viewer.getFarViewingDistance() && !requested)
			if(!initialized)
				loadMedia(); 

//		if(p.p.frameCount % 10 == 0)
//			PApplet.println("Update panorama..."+getID()+" visible:"+visible+" fading:"+fading);

		if(texture.width > 0 && !disabled)			
		{
			visible = (getDistanceBrightness() > 0.f);

			if(!fading && p.hidePanoramas)
				visible = false;
//				fadeOut();

			if(visible && !fading && !fadedOut && !p.hidePanoramas)					// Fade in
				fadeIn();

			if(fadedOut) fadedOut = false;

//			if(p.p.frameCount % 10 == 0)
//				PApplet.println("---Update panorama..."+getID()+" visible:"+visible+" fading:"+fading);
		}
		
		if(isFading())                       // Fade in and out with time
		{
			if(p.p.debug.panorama && p.p.debug.detailed)
				p.p.display.message("Panorama fading... id: "+getID());
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
	 * draw()
	 * Display the image or spherical panorama in virtual space
	 */
	public void draw()
	{
		float brightness = fadingBrightness;					

		if(p.p.timeFading && time != null)
		{
			float timeBrightnessFactor = getTimeBrightness();        
			brightness *= timeBrightnessFactor; 																			// Fade alpha based on time or date
		}
		
		if(p.p.dateFading && time != null)
		{
			float dateBrightnessFactor = getDateBrightness();        
			brightness *= dateBrightnessFactor; 																			// Fade alpha based on time or date
		}

		viewingBrightness = PApplet.map(brightness, 0.f, 1.f, 0.f, 255.f);	  // Fade panoramas with distance  -- CHANGE THIS / UNNECESSARY?

		if (visible && !hidden && !disabled) 
		{
			if (viewingBrightness > 0)
			{
				if(texture.width > 0 && !p.p.viewer.map3DMode)		// If image has been loaded
				{
					drawPanorama();
				}
			}
		} 
		else
		{      
			p.p.p.noFill();                  // Hide image if it isn't visible
		}

		if (visible && isSelected() && !disabled && p.p.debug.model)		// Draw panorama location for debugging or map display
			drawLocation(centerSize);
		if (visible && !disabled && p.p.viewer.map3DMode)
			drawLocation(centerSize);
	}

	/**
	 * Fade in panorama
	 */
	public void fadeIn()
	{
		if(fading || isFadingIn || isFadingOut)		// If already fading, stop at current value
//			if(!initFading)		
				stopFading();

		fadeBrightness(1.f);					// Fade in
	}

	/**
	 * Fade out panorama
	 */
	public void fadeOut()
	{
		if(fading || isFadingIn || isFadingOut)		// If already fading, stop at current value
//			if(!initFading)			
				stopFading();

		fadeBrightness(0.f);					// Fade out
	}

	/**
	 * Calculate and return viewing brightness given viewer distance 
	 * @param brightness 
	 * @return Viewing brightness of the panorama
	 */
//	public float getViewingBrightness(float brightness)
//	{
//		viewingBrightness = PApplet.map(brightness, 0.f, 1.f, 0.f, 255.f);				// Scale to setting for alpha range
//		return brightness;
//	}

	/**
	 * Select or unselect this panorama
	 * @param selection New selection
	 */
	public void setSelected(boolean selection)
	{
		//		 selected = selection;
		//		 p.selectedPanorama = id;
		//		 
		//		 if(selection)
		//		 {
		//			 if(p.p.debug.viewer && p.p.debug.detailed)
		//				 p.p.display.sendMessage("Selected image:"+id);
		//			 
		//			 displayMetadata();
		//		 }
	}

	/**
	 * Draw the panorama
	 */
	private void drawPanorama() 
	{
		p.p.p.pushMatrix();
		p.p.p.translate(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z);	// CHANGE VALUES!

		float r = p.p.defaultFocusDistance;				// Testing this
//		float r = p.p.defaultFocusDistance;				// Testing this
		int v0,v1,v2;

		p.p.p.textureMode(PApplet.IMAGE);
		p.p.p.noStroke();
		p.p.p.beginShape(PApplet.TRIANGLE_STRIP);
		p.p.p.texture(texture);

		/* Set the panorama brightness */		
		if(p.p.viewer.selection)
		{
			if(isSelected())
			{
				if(!p.p.alphaMode)
					p.p.p.tint(viewingBrightness, 255);          				
				else
					p.p.p.tint(255, viewingBrightness);          				
			}
			else
			{
				if(!p.p.alphaMode)
					p.p.p.tint(viewingBrightness * 0.4f, 255);          // Set the image transparency					
				else
					p.p.p.tint(255, viewingBrightness * 0.333f);          				
			}
		}
		else if(p.p.viewer.videoMode)
		{
			if(!p.p.alphaMode)
				p.p.p.tint(viewingBrightness * 0.66f, 255);          // Set the image transparency					
			else
				p.p.p.tint(255, viewingBrightness * 0.333f);          				
		}
		else
		{
			if(!p.p.alphaMode)
				p.p.p.tint(viewingBrightness, 255);          				
			else
				p.p.p.tint(255, viewingBrightness);          				
		}
		
		float iu = (float)(texture.width-1)/(panoramaDetail);
		float iv = (float)(texture.height-1)/(panoramaDetail);
		float u = 0, v = iv;

		for (int i = 0; i < panoramaDetail; i++) 
		{
			p.p.p.vertex(0, -r, 0,u,0);
			p.p.p.vertex(sphere[i].x * r, sphere[i].y * r, sphere[i].z * r, u, v);
			u += iu;
		}

		p.p.p.vertex(0, -r, 0, u, 0);
		p.p.p.vertex(sphere[0].x * r, sphere[0].y * r, sphere[0].z * r, u, v);
		p.p.p.endShape();   

		// Draw middle rings
		int voff = 0;
		for(int i = 2; i < panoramaDetail; i++) 
		{
			v1 = v0 = voff;
			voff += panoramaDetail;
			v2 = voff;
			u = 0;
			p.p.p.beginShape(PApplet.TRIANGLE_STRIP);
			p.p.p.texture(texture);
			for(int j = 0; j < panoramaDetail; j++) 			// Draw ring
			{
				p.p.p.vertex(sphere[v1].x * r, sphere[v1].y * r, sphere[v1++].z * r, u, v);
				p.p.p.vertex(sphere[v2].x * r, sphere[v2].y * r, sphere[v2++].z * r, u, v + iv);
				u += iu;
			}

			// Close ring
			v1 = v0;
			v2 = voff;
			p.p.p.vertex(sphere[v1].x * r, sphere[v1].y * r, sphere[v1].z * r, u, v);
			p.p.p.vertex(sphere[v2].x * r, sphere[v2].y * r, sphere[v2].z * r, u, v + iv);
			p.p.p.endShape();
			v += iv;
		}
		u = 0;

		// Draw northern "cap"
		p.p.p.beginShape(PApplet.TRIANGLE_STRIP);
		p.p.p.texture(texture);
		for(int i = 0; i < panoramaDetail; i++) 
		{
			v2 = voff + i;
			p.p.p.vertex(sphere[v2].x * r, sphere[v2].y * r, sphere[v2].z * r, u, v);
			p.p.p.vertex(0, r, 0, u, v + iv);    
			u += iu;
		}

		p.p.p.vertex(sphere[voff].x * r, sphere[voff].y * r, sphere[voff].z * r, u, v);
		p.p.p.endShape();

		p.p.p.popMatrix();
		p.p.p.textureMode(PApplet.NORMAL);
	}

	/***
	 * Initialize panorama geometry
	 */
	void initializeSphere(int resolution)
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
		
		panoramaDetail = resolution;
		initialized = true;
	}

	/**
	 * loadMedia()
	 * Request the image to be loaded from disk
	 */
	public void loadMedia()
	{
		if(p.p.debug.panorama && p.p.debug.detailed)
			p.p.display.message("Requesting panorama file:"+getName());

		if(!p.p.debug.lowMemory)			// Check enough memory available
		{
			if(p.p.transitionsOnly)
				location = new PVector (0, 0, 0);
			else
				location = new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z);

			if (p.p.utilities.isNaN(location.x) || p.p.utilities.isNaN(location.x) || p.p.utilities.isNaN(location.x))
			{
				location = new PVector (0, 0, 0);
			}

			texture = p.p.p.requestImage(filePath);
			requested = true;
			p.p.requestedPanoramas++;
		}
	}

	/** 
	 * @return Distance visibility multiplier between 0. and 1.
	 * Find panorama brightness due to distance (fades away in distance and as camera gets close)
	 */
	public float getDistanceBrightness()									
	{
		float viewDist = getViewingDistance();
		float farViewingDistance = p.p.viewer.getFarViewingDistance();
		//		float nearViewingDistance = p.p.viewer.getNearViewingDistance();

		float distVisibility = 1.f;

		if(viewDist > farViewingDistance)
		{
			float vanishingPoint = farViewingDistance + p.p.defaultFocusDistance;	// Distance where transparency reaches zero
			if(viewDist < vanishingPoint)
				distVisibility = PApplet.constrain(1.f - PApplet.map(viewDist, p.p.viewer.getFarViewingDistance(), vanishingPoint, 0.f, 1.f), 0.f, 1.f);    // Fade out until cam.visibleFarDistance
			else
				distVisibility = 0.f;
		}
		//		else if(viewDist < nearViewingDistance)								
		//		{
		//			distVisibility = PApplet.constrain(PApplet.map(viewDist, p.p.viewer.getNearClippingDistance(), p.p.viewer.getNearViewingDistance(), 0.f, 1.f), 0.f, 1.f);
		////			if(isSelected())
		//				PApplet.println("Panorama ID:"+getID()+" dist:"+viewDist+" distVisibility:"+distVisibility+" near:"+p.p.viewer.getNearClippingDistance()+" far:"+p.p.viewer.getNearViewingDistance());
		//		}

		//		PApplet.println("captureLocation.x:"+captureLocation.x+" captureLocation.y:"+captureLocation.y+" captureLocation.z:"+captureLocation.z);
		//		PApplet.println("viewer.x:"+p.p.viewer.getLocation().x+" viewer.y:"+p.p.viewer.getLocation().y+" viewer.z:"+p.p.viewer.getLocation().z);
		//		PApplet.println("viewDist:+"+viewDist+" farViewingDistance:"+farViewingDistance+" distVisibility:"+distVisibility);
		return distVisibility;
	}

	/**
	 * @return Distance from the panorama to the camera
	 */
	public float getViewingDistance()       // Find distance from camera to point in virtual space where photo appears           
	{
		PVector camLoc;

		if(p.p.transitionsOnly)
			camLoc = p.p.viewer.getLocation();
		else
			camLoc = p.p.viewer.getLocation();

		float distance;

		distance = PVector.dist(getCaptureLocation(), camLoc);

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
			p.disassociatedPanoramas++;
		}

		if(cluster != -1)
			return true;
		else
			return false;
	}

	public void setDirection( float newTheta )
	{
		theta = newTheta;
	}

	public void displayMetadata()
	{

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

	public float getWidth()
	{
		return imageWidth;
	}

	public float getHeight()
	{
		return imageHeight;
	}
}
