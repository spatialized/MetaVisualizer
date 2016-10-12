package gmViewer;

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
	
	/* EXIF Metadata */
	public float imageWidth, imageHeight;		// Width and height
	public int origWidth, origHeight;			// Original width and height

	/* Panorama */
	float[] sphereX, sphereY, sphereZ;								// Sphere vertices
	int panoramaDetail = 50;  										// Sphere detail setting
	float sinLUT[];
	float cosLUT[];
	float sinCosPrecision = 0.5f;
	int sinCosLength = (int)(360.0f / sinCosPrecision);

	GMV_Panorama ( GMV_Field parent, int newID, String newName, String newFilePath, PVector newGPSLocation, float newTheta, 
					int newCameraModel, int newWidth, int newHeight, float newBrightness, Calendar newCalendar )
	{
		super(parent, newID, newName, newFilePath, newGPSLocation, newTheta, newCameraModel, newBrightness, newCalendar);
		
		p = parent;

		texture = p.p.createImage(0,0,processing.core.PConstants.RGB);		// Create empty image
		
		imageWidth = newWidth;
		imageHeight = newHeight;

		filePath = newFilePath;

		gpsLocation = newGPSLocation;
		cameraModel = newCameraModel;

		theta = newTheta;              			// GPS Orientation (Yaw angle)
	}  
	
	/**
=	 * Update main variables
	 */
	public void update()
	{
		if(requested && texture.width != 0)			// If requested image has loaded, initialize image 
		{
			initializePanorama(panoramaDetail);					

			requested = false;
			p.p.requestedPanoramas--;
		}

		if(texture.width > 0 && !disabled)			
		{
			visible = (getDistanceBrightness() > 0.f);

			if(p.p.debug.hidePanoramas)
				visible = false;
			
			if(visible && !fading && !fadedOut)					// Fade in
				fadeIn();

			if(fadedOut) fadedOut = false;
			
//			if(p.p.frameCount % 10 == 0)
//				PApplet.println("Update panorama..."+getID()+" visible:"+visible+" fading:"+fading);
		}
		else if(getCaptureDistance() < p.p.viewer.getFarViewingDistance() && !requested)
		{
			loadMedia(); 
		}

		if(isFading())                       // Fade in and out with time
		{
//			p.p.display.message("Panorama fading... id: "+getID());
			updateFadingBrightness();

			if(fadingBrightness == 0.f)
				visible = false;
		}

//		if(fadingObjectDistance)
//		{
//			updateFadingObjectDistance();
//		}
//		else if(visible)
//			calculateVertices();  			// Update image parameters
	}

	/**
	 * draw()
	 * Display the image or spherical panorama in virtual space
	 */
	public void draw()
	{
		float curBrightness = fadingBrightness;					
		float timeBrightnessFactor;                          // Fade with time 

		if(p.p.timeFading)
		{
			timeBrightnessFactor = getTimeBrightness();        
			curBrightness *= timeBrightnessFactor; 																			// Fade alpha based on time or date
		}

		viewingBrightness = getViewingBrightness(curBrightness);               		  // Fade panoramas with distance  -- CHANGE THIS / UNNECESSARY?

		if (visible && !hidden && !disabled) 
		{
			if (viewingBrightness > 0)
			{
				if(texture.width > 0 && !p.p.viewer.mapMode)		// If image has been loaded
				{
					drawPanorama();
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

	/**
	 * fadeIn()
	 * Fade in panorama
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
	 * Fade out panorama
	 */
	public void fadeOut()
	{
		if(fading || isFadingIn || isFadingOut)		// If already fading, stop at current value
			if(!initFading)			
				stopFading();

		fadeBrightness(0.f);					// Fade out
	}

	/**
	 * getViewingBrightness()
	 * Calculate and return viewing brightness given viewer distance 
	 * @param viewingBrightness 
	 * @return Viewing brightness of the panorama
	 */
	public float getViewingBrightness(float viewingBrightness)
	{
		viewingBrightness = PApplet.map(viewingBrightness, 0.f, 1.f, 0.f, 255.f);				// Scale to setting for alpha range
		return viewingBrightness;
	}

	/**
	 * setSelected()
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
		p.p.pushMatrix();
		p.p.translate(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z);	// CHANGE VALUES!

		float r = p.p.defaultFocusDistance;				// Testing this
		int v0,v1,v2;

		p.p.textureMode(PApplet.IMAGE);
		p.p.noStroke();
		p.p.beginShape(PApplet.TRIANGLE_STRIP);

		p.p.texture(texture);

		/* Set the panorama brightness */		
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
		
		float iu = (float)(texture.width-1)/(panoramaDetail);
		float iv = (float)(texture.height-1)/(panoramaDetail);
		float u = 0, v = iv;

		for (int i = 0; i < panoramaDetail; i++) 
		{
			p.p.vertex(0, -r, 0,u,0);
			p.p.vertex(sphereX[i] * r, sphereY[i] * r, sphereZ[i] * r, u, v);
			u += iu;
		}

		p.p.vertex(0, -r, 0, u, 0);
		p.p.vertex(sphereX[0] * r, sphereY[0] * r, sphereZ[0] * r, u, v);
		p.p.endShape();   

		// Draw middle rings
		int voff = 0;
		for(int i = 2; i < panoramaDetail; i++) 
		{
			v1 = v0 = voff;
			voff += panoramaDetail;
			v2 = voff;
			u = 0;
			p.p.beginShape(PApplet.TRIANGLE_STRIP);
			p.p.texture(texture);
			for(int j = 0; j < panoramaDetail; j++) 			// Draw ring
			{
				p.p.vertex(sphereX[v1] * r, sphereY[v1] * r, sphereZ[v1++] * r, u, v);
				p.p.vertex(sphereX[v2] * r, sphereY[v2] * r, sphereZ[v2++] * r, u, v + iv);
				u += iu;
			}

			// Close ring
			v1 = v0;
			v2 = voff;
			p.p.vertex(sphereX[v1] * r, sphereY[v1] * r, sphereZ[v1] * r, u, v);
			p.p.vertex(sphereX[v2] * r, sphereY[v2] * r, sphereZ[v2] * r, u, v + iv);
			p.p.endShape();
			v += iv;
		}
		u = 0;

		// Draw northern "cap"
		p.p.beginShape(PApplet.TRIANGLE_STRIP);
		p.p.texture(texture);
		for(int i = 0; i < panoramaDetail; i++) 
		{
			v2 = voff + i;
			p.p.vertex(sphereX[v2] * r, sphereY[v2] * r, sphereZ[v2] * r, u, v);
			p.p.vertex(0, r, 0, u, v + iv);    
			u += iu;
		}

		p.p.vertex(sphereX[voff] * r, sphereY[voff] * r, sphereZ[voff] * r, u, v);
		p.p.endShape();

		p.p.popMatrix();
		p.p.textureMode(PApplet.NORMAL);
	}

	/***
	 * initializePanorama()
	 * Initialize panorama geometry
	 */
	void initializePanorama(int resolution)
	{
		sinLUT = new float[sinCosLength];
		cosLUT = new float[sinCosLength];

		for (int i = 0; i < sinCosLength; i++) {
			sinLUT[i] = (float) Math.sin(i * PApplet.DEG_TO_RAD * sinCosPrecision);
			cosLUT[i] = (float) Math.cos(i * PApplet.DEG_TO_RAD * sinCosPrecision);
		}

		float delta = (float)sinCosLength/resolution;
		float[] cx = new float[resolution];
		float[] cz = new float[resolution];

		// Calc unit circle in XZ plane
		for (int i = 0; i < resolution; i++) {
			cx[i] = -cosLUT[(int) (i*delta) % sinCosLength];
			cz[i] = sinLUT[(int) (i*delta) % sinCosLength];
		}

		// Computing vertexlist vertexlist starts at south pole
		int vertCount = resolution * (resolution-1) + 2;
		int currVert = 0;

		// Re-init arrays to store vertices
		sphereX = new float[vertCount];
		sphereY = new float[vertCount];
		sphereZ = new float[vertCount];
		float angle_step = (sinCosLength*0.5f)/resolution;
		float angle = angle_step;

		// Step along Y axis
		for (int i = 1; i < resolution; i++) {
			float curRadius = sinLUT[(int) angle % sinCosLength];
			float currY = -cosLUT[(int) angle % sinCosLength];
			for (int j = 0; j < resolution; j++) {
				sphereX[currVert] = cx[j] * curRadius;
				sphereY[currVert] = currY;
				sphereZ[currVert++] = cz[j] * curRadius;
			}
			angle += angle_step;
		}
		
		panoramaDetail = resolution;
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

			texture = p.p.requestImage(filePath);
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
//		else if(viewDist < nearViewingDistance)								
//		{
//			distVisibility = PApplet.constrain(PApplet.map(viewDist, p.p.viewer.getNearClippingDistance(), p.p.viewer.getNearViewingDistance(), 0.f, 1.f), 0.f, 1.f);
////			if(isSelected())
//				PApplet.println("Panorama ID:"+getID()+" dist:"+viewDist+" distVisibility:"+distVisibility+" near:"+p.p.viewer.getNearClippingDistance()+" far:"+p.p.viewer.getNearViewingDistance());
//		}

		return distVisibility;
	}

	/**
	 * @return Distance from the panorama to the camera
	 */
	public float getViewingDistance()       // Find distance from camera to point in virtual space where photo appears           
	{
		PVector camLoc;

		if(p.p.transitionsOnly)
			camLoc = p.p.viewer.location;
		else
			camLoc = p.p.viewer.location;

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
