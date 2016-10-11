package gmViewer;

import java.util.Calendar;

//import com.sun.xml.internal.ws.org.objectweb.asm.Type;

import processing.core.PApplet;
//import processing.core.PApplet;
import processing.core.PVector;
import toxi.math.ScaleMap;

/***************************************
 * GMV_Viewable
 * @author davidgordon
 * Abstract class representing objects viewable in 3D virtual space
 */
public abstract class GMV_Viewable 
{
	/* General */
	private int id;

	/* File System */
	private String name = "";
	public String filePath = "";

	/* Model */
	public PVector captureLocation;				// Media capture location in simulation – EXIF GPS coords scaled to fieldSize.
	public PVector location;        			// Media location in simulation 
	float theta = 0;                			// Media Orientation (in Degrees N)
	public boolean fadingObjectDistance = false, beginFadingObjectDistance = false;			// Fading distance of object in image?
	
	/* EXIF Metadata */
	public PVector gpsLocation;            		// Location in original GPS coords
	public int cameraModel;                 	// Camera model
	public float brightness;
	
	/* Time */
	GMV_TimePoint time;
	
	public float clusterDate, clusterTime;		// Date and time relative to other images in cluster (position between 0. and 1.)
	ScaleMap timeLogMap;

	/* Interaction */
	private boolean selected = false;

	/* Cluster */
	int cluster = -1;				 			// Cluster it belongs too								-- Note: Have images belong to multiple clusters??

	/* Graphics */
	public float aspectRatio = (float) 0.666;	// Aspect ratio of image
	public PVector azimuthAxis = new PVector(0, 1, 0);
	public PVector verticalAxis = new PVector(1, 0, 0);
	public PVector rotationAxis = new PVector(0, 0, 1);

	/* Transparency */
	public float viewingBrightness = 0;				// Final image brightness (or alpha in useAlphaFading mode) 
	public boolean isFadingIn = false, isFadingOut = false;
	public float fadingBrightness;							// Media transparency due to fading in / out
	public boolean beginFading = false, fading = false, initFading = true;		
	public float fadingStart = 0.f, fadingTarget = 0.f, fadingStartFrame = 0.f, fadingEndFrame = 0.f; 
	public boolean fadedOut = false;			// Recently faded out
	
	/* Status Modes */
	public boolean visible = false;				// Media is currently visible and will be drawn
	public boolean active = false;				// True when the image has faded in and isn't fading out	-- Needed?
	public boolean disabled = false;			// Disabled due to errors or user and will not be drawn
	public boolean hidden = false;				// Hidden from view											-- Needed?
	public boolean requested = false;			// Indicates a recent request to load media from disk

	/* Display */
	public float centerSize = 0.05f;

	GMV_Field p;								// Parent field

	GMV_Viewable ( GMV_Field parent, int newID, String newName, String newFilePath, PVector newGPSLocation, float newTheta, 
			int newCameraModel, float newBrightness, Calendar newCalendar )
	{
		p = parent;

		name = newName;
		id = newID; 
		filePath = newFilePath;

		time = new GMV_TimePoint( p.p, newCalendar );
		
		gpsLocation = newGPSLocation;
		captureLocation = new PVector(0, 0, 0);

		cameraModel = newCameraModel;
		theta = newTheta;              
		brightness = newBrightness;

		fadingBrightness = 0.f;			
		fadingStart = 0.f;

		timeLogMap = new ScaleMap(0., 1., p.p.minTimeBrightness, 1.f);		/* Time fading interpolation */
		timeLogMap.setMapFunction(p.p.circularEaseOut);
		
		if(p.p.timeFading)
			initFading = false;			/* No need for initial fade in during Time Fading Mode */
	}  

	abstract void draw();
	abstract void displayMetadata();
	abstract void loadMedia();
	
	/**
	 * setClusterDate()
	 * Set clusterTime for this media based on media times in associated cluster
	 */
	void setClusterDate()
	{
		if(cluster != -1)
		{
			GMV_Cluster c = p.clusters.get(cluster);
			if(c.lowImageDate == c.highImageDate)
			{
				clusterDate = c.lowImageDate;
			}
			else
			{
				clusterDate = PApplet.map(time.getDate(), c.lowImageDate, c.highImageDate, 0.f, 1.f);			// -- Use dateLength?
			}
		}
	}

	/**
	 * setClusterTime()
	 * Set clusterTime for this image based on media times in associated cluster
	 */
	void setClusterTime()
	{
		if(cluster != -1)
		{
			GMV_Cluster c = p.clusters.get(cluster);
			if(c.lowImageTime == c.highImageTime)
			{
				clusterTime = c.lowImageTime;
			}
			else
			{
				clusterTime = PApplet.map(time.getTime(), c.lowImageTime, c.highImageTime, 0.f, 1.f);			// -- Use dayLength?
			}
		}
	}

	/**
	 * calculateDate()
	 * @return Image date in virtual time
	 */
//	float calculateDate()
//	{
//		float minDate = ((GMV_Cluster)p.clusters.get(cluster)).lowImageDate;
//		float maxDate = ((GMV_Cluster)p.clusters.get(cluster)).highImageDate;
//		float d = PApplet.abs(p.p.fields.get(p.p.viewer.curField).curTime - PApplet.map(time.date, minDate, maxDate, 0.f, p.p.fields.get(p.p.viewer.curField).dayLength));   
//
//		if(minDate == maxDate) 
//			return p.p.fields.get(p.p.viewer.curField).dayLength;
//		else
//			return d;
//	}

	
	/**
	 * fadeBrightness()
	 * Transition alpha from current to given value
	 */
	void fadeBrightness(float target)
	{
		if(target != fadingBrightness)			// Check if already at target
		{
			beginFading = true;
			fading = true;    
			fadingStart = fadingBrightness;
			fadingTarget = target;
			fadingStartFrame = p.p.frameCount;
			fadingEndFrame = fadingStartFrame + p.teleportLength;

			if(target > fadingBrightness)
				isFadingIn = true;
			else
				isFadingOut = true;
		}
	}
	
	/**
	 * stopFading()
	 * Stop fading in / out video
	 */
	public void stopFading()
	{
		if(p.p.debug.viewable)
			PApplet.println("Stop fading for media:"+id);

		fadingEndFrame = p.p.frameCount;
		fadingStart = fadingBrightness;
		fading = false;

		if(isFadingOut) isFadingOut = false;
		if(isFadingIn) isFadingIn = false;

		if(initFading)
			initFading = false;
	}

	/**
	 * drawLocation()
	 * @param size Size to draw the video center
	 * Draw the video center as a colored sphere
	 */
	void drawLocation(float size)
	{
		p.p.pushMatrix();
		p.p.translate(location.x, location.y, location.z);
		p.p.stroke(200, 50, 255);
		p.p.fill(0, 0, 255, 150);
		p.p.sphere(size);
		p.p.popMatrix();
	}
	
	/**
	 * getCaptureDistance()
	 * @return Distance from the image capture location to the camera
	 */
	float getCaptureDistance()       // Find distance from camera to point in virtual space where photo appears           
	{
		PVector camLoc;

		if(p.p.transitionsOnly)
			camLoc = p.p.viewer.location;
		else
			camLoc = p.p.viewer.location;

		float distance = PVector.dist(captureLocation, camLoc);     

		return distance;
	}


	/**
	 * getCaptureDistanceFrom()
	 * @return How far the image capture location is from a point
	 */
	float getCaptureDistanceFrom(PVector point)       // Find distance from camera to point in virtual space where photo appears           
	{
		float distance = PVector.dist(captureLocation, point);     
		return distance;
	}

	/**
	 * getTimeBrightness()
	 * @return Time brightness factor between 0. and 1.
	 * Calculate media brightness based on distance (fades away in distance and as camera gets close)
	 */
	float getTimeBrightness()												
	{
		int cycleLength = p.p.timeCycleLength;				// Length of main time loop
		int centerTime = -1;								// Midpoint of visibility for this media 		
		float timeBrightness = 0.f;

		float length = p.p.defaultMediaLength;				// Start with default length
		
		int fadeInStart = 0;								// When image starts fading in
		int fadeInEnd = 0;									// When image reaches full brightness
		int fadeOutStart = 0;								// When image starts fading out
		int fadeOutEnd = 0;									// When image finishes fading out
		int fadeLength = fadeOutEnd - fadeOutStart;

		boolean error = false;
		
		GMV_Cluster c = p.p.getCluster(cluster);			// Get cluster for this media
		
//		switch(p.p.timeFadingMode)
//		{
//		case 0:					// Field Time Mode
//			centerTime = PApplet.round( PApplet.map(time.getTime(), 0.f, 1.f,  p.p.defaultMediaLength * 0.25f, 
//										cycleLength - p.p.defaultMediaLength * 0.25f) );				// Calculate center time in field timeline
//			length = p.p.defaultMediaLength;				
//			
//			fadeInStart = centerTime - (int)length / 2;		// Frame media starts fading in
//			fadeInEnd = centerTime - (int)length / 4;		// Frame media reaches full brightness
//			fadeOutStart = centerTime + (int)length / 4;	// Frame media starts fading out
//			fadeOutEnd = centerTime + (int)length / 2;		// Frame media finishes fading out
//			
//			break;
//			
//		case 1:					// Cluster Time Mode
			
			
			float lower = c.timeline.get(0).getLower();												// Get cluster timeline lower bound
			float upper = c.timeline.get(c.timeline.size()-1).getUpper();							// Get cluster timeline upper bound
			float curLower = c.timeline.get(p.p.viewer.currentClusterTimeSegment).getLower();			// Get current segment lower bound
			float curUpper = c.timeline.get(p.p.viewer.currentClusterTimeSegment).getUpper();			// Get current segment lower bound
			float timelineLength = upper - lower;
			float segmentLength = curUpper - curLower;
			
			centerTime = PApplet.round(PApplet.map( time.getTime(), lower, upper, p.p.defaultMediaLength * 0.25f, 
												    cycleLength - p.p.defaultMediaLength * 0.25f) );	// Calculate center time in cluster timeline
//			centerTime = PApplet.round(PApplet.map(time.getTime(), lower, upper, 0.f, cycleLength));	
			
			if(lower == curLower && upper == curUpper)				// Only one cluster segment
			{
				length = p.p.timeCycleLength;		// -- Should depend on cluster it belongs to 

				if(selected)
				{
					PApplet.println("Only one cluster time segment, full length:"+length);
					PApplet.println("time:"+time.getTime()+" centerTime:"+centerTime+" curTime:"+p.p.currentTime+" dayLength:"+cycleLength);
					PApplet.println("lower:"+lower+" upper:"+upper+" cLower:"+curLower+" cUpper:"+curUpper);
				}
				
				fadeInStart = 0;		// Frame media starts fading in
				fadeInEnd = centerTime - (int)length / 4;		// Frame media reaches full brightness
				fadeOutStart = centerTime + (int)length / 4;	// Frame media starts fading out
				fadeOutEnd = cycleLength;		// Frame media finishes fading out
			}
			else
			{
			 	/* Scale media length based on time segment length */
				length = PApplet.map(segmentLength, 0, timelineLength, p.p.defaultMediaLength * 0.5f, p.p.timeCycleLength);
				length = PApplet.constrain(length, p.p.defaultMediaLength * 0.5f, p.p.timeCycleLength);
				
				fadeInStart = centerTime - (int)length / 2;		// Frame media starts fading in
				fadeInEnd = centerTime - (int)length / 4;		// Frame media reaches full brightness
				fadeOutStart = centerTime + (int)length / 4;	// Frame media starts fading out
				fadeOutEnd = centerTime + (int)length / 2;		// Frame media finishes fading out
				
				if(selected)
				{
					PApplet.println("Segment length:"+segmentLength+" timeline length:"+timelineLength+" media length:"+length);
					PApplet.println(" media time:"+time.getTime()+" centerTime:"+centerTime+" currentTime:"+p.p.currentTime+" cycleLength:"+cycleLength);
					PApplet.println(" lower:"+lower+" upper:"+upper+" cLower:"+curLower+" cUpper:"+curUpper);
					PApplet.println(" fadeInStart:"+fadeInStart+" fadeInEnd:"+fadeInEnd+" fadeOutStart:"+fadeOutStart+" fadeOutEnd:"+fadeOutEnd);
				}
			}	
			
			/* Adjust fading times to fit cycle length */
			if(fadeInStart > cycleLength)
				PApplet.println("Error: fadeInStart after day end!!");
			
			if(fadeInEnd > cycleLength)
			{
				error = true;
				if(p.p.debug.viewable)
				{
					PApplet.println("------Error: fadeInEnd after day end-----time:"+time.getTime()+" centerTime:"+centerTime+" lower:"+lower+" upper:"+upper+" curTime:"+p.p.currentTime+" dayLength:"+cycleLength);
					PApplet.println("-----fadeInStart:"+fadeInStart+" fadeInEnd:"+fadeInEnd+" fadeOutStart:"+fadeOutStart+" fadeOutEnd:"+fadeOutEnd);
					PApplet.println("-----cluster:"+cluster+" currentCluster:"+p.p.getCurrentCluster().getID()+" curClusterTimeSegment:"+p.p.viewer.currentClusterTimeSegment);
				}
			}
			
			if(fadeOutStart > cycleLength)
			{
				if(selected)
					PApplet.println("Adjusting fadeOutStart");

				fadeOutStart = cycleLength-PApplet.round(length*0.25f);			// Adjust fadeOutStart
				fadeOutEnd = cycleLength;		// Frame media finishes fading out
			}
			
			if(fadeOutEnd > cycleLength)
			{
				if(selected)
					PApplet.println("Adjusting fadeOutEnd");
				
//				if(fadeOutEnd - fadeOutStart < fadeLength) 
				fadeOutStart = PApplet.round(cycleLength-length*0.25f);			// Adjust fadeOutStart
				fadeOutEnd = cycleLength;		// Frame media finishes fading out
			}

			if(selected)
			{
				PApplet.println("time:"+time.getTime()+" centerTime:"+centerTime+" curTime:"+p.p.currentTime+" dayLength:"+cycleLength);
				PApplet.println("fadeInStart:"+fadeInStart+" fadeInEnd:"+fadeInEnd+" fadeOutStart:"+fadeOutStart+" fadeOutEnd:"+fadeOutEnd);
			}
			
//			if(cluster != -1)
//			{
//				switch(p.clusters.get(cluster).baseTimeScale)					// Calculate center time based on cluster's baseTimeScale
//				{
//				case 0:
//					centerTime = (int)(time.getTime() * dayLength);			
//					break;
//				case 1:
////					centerTime = (int)(time * dayLength);			
//					break;
//				case 2:
////					centerTime = (int)(time * dayLength);			
//					break;
//				case 3:
////					centerTime = (int)(time * dayLength);			
//					break;
//				case 4:
////					centerTime = (int)(time * dayLength);			
//					break;
//				default:
//					centerTime = (int)(time.getTime() * dayLength);			
//					break;
//				}
//			}
//			else
//			{
//				if(p.p.debug.viewable)
//					PApplet.println("Error: no associated cluster!");
//			}
//			break;

//		default:				
//			break;
//		}

		// Fade in whole cluster together
//		float fTime = p.p.getCurrentField().timeline.get(p.p.viewer.currentFieldTime).getTime();	// Get field time 
//		float mediaTime = PApplet.round( PApplet.map(fTime, 0.f, 1.f, 0.f, cycleLength) );
		
		if(p.p.currentTime <= cycleLength)
		{
			if(p.p.currentTime < fadeInStart || p.p.currentTime > fadeOutEnd)			// If before fade in or after fade out
			{
				if(active)							// If image was active
					active = false;					// Set to inactive

				timeBrightness = 0.f;			   					// Zero visibility
			}
			else if(p.p.currentTime < fadeInEnd)						// During fade in
			{
				if(!active)							// If image was not active
					active = true;

				timeBrightness = PApplet.constrain(PApplet.map(p.p.currentTime, fadeInStart, fadeInEnd, 0.f, 1.f), 0.f, 1.f);   
				if(selected && p.p.debug.viewable)
					PApplet.println(" Fading In..."+id);
			}
			else if(p.p.currentTime > fadeOutStart)					// During fade out
			{
				if(selected && p.p.debug.viewable)
					PApplet.println(" Fading Out..."+id);
				timeBrightness = PApplet.constrain(1.f - PApplet.map(p.p.currentTime, fadeOutStart, fadeOutEnd, 0.f, 1.f), 0.f, 1.f);    
			}
			else													// After fade in, before fade out
			{
				if(selected && p.p.debug.viewable && p.p.debug.detailed)
					PApplet.println(" Full visibility...");				// Full visibility
				timeBrightness = 1.f;								
			}
		}
		else
		{
			if(active && p.p.currentTime > fadeOutEnd)					// If image was active and has faded out
				active = false;										// Set to inactive
		}

		timeBrightness = (float)timeLogMap.getMappedValueFor(timeBrightness);   		// Logarithmic scaling

		if(selected)
			PApplet.println("timeBrightness"+timeBrightness);

		if(!error)
			return timeBrightness;
		else
			return p.p.minTimeBrightness;
	}
	
	/**
	 * updateFadingBrightness()
	 * Update viewingBrightnessFadeValue each frame
	 */
	void updateFadingBrightness()
	{
		float newFadeValue = 0.f;

		if(beginFading)
		{
			fadingStartFrame = p.p.frameCount;					
			fadingEndFrame = p.p.frameCount + p.teleportLength;	
			beginFading = false;
		}

		if (p.p.frameCount >= fadingEndFrame)
		{
			fading = false;
			newFadeValue = fadingTarget;

			if(initFading) initFading = false;
			if(isFadingOut) isFadingOut = false;
			if(isFadingIn) isFadingIn = false;
			fadedOut = true;
		} 
		else
		{
			newFadeValue = PApplet.map(p.p.frameCount, fadingStartFrame, fadingEndFrame, fadingStart, fadingTarget);      // Fade with distance from current time
		}

		fadingBrightness = newFadeValue;
	}

	/**
	 * calculateCaptureLocation()
	 * Calculate media capture location in virtual space based on GPS location
	 */
	void calculateCaptureLocation()                                  
	{
		float newX = 0.f, newZ = 0.f, newY = 0.f;

		if(p.model.highLongitude != -1000000 && p.model.lowLongitude != 1000000 && p.model.highLatitude != -1000000 && p.model.lowLatitude != 1000000 && p.model.highAltitude != -1000000 && p.model.lowAltitude != 1000000)
		{
			if(p.model.highLongitude != p.model.lowLongitude && p.model.highLatitude != p.model.lowLatitude)
			{
				newX = PApplet.map(gpsLocation.x, p.model.lowLongitude, p.model.highLongitude, -0.5f * p.model.fieldWidth, 0.5f*p.model.fieldWidth); 				// GPS longitude decreases from left to right
				newY = -PApplet.map(gpsLocation.y, p.model.lowAltitude, p.model.highAltitude, -0.5f * p.model.fieldHeight, 0.5f*p.model.fieldHeight); 			// Convert altitude feet to meters, negative sign to match P3D coordinate space
				newZ = -PApplet.map(gpsLocation.z, p.model.lowLatitude, p.model.highLatitude, -0.5f * p.model.fieldLength, 0.5f*p.model.fieldLength); 			// GPS latitude increases from bottom to top, minus sign to match P3D coordinate space

				if(p.p.altitudeOff || p.p.viewer.mapMode)				
					newY = 0.f;
			}
			else
			{
				newX = newY = newZ = 0.f;
			}
		}

		captureLocation = new PVector(newX, newY, newZ);
	}


	/**
	 * setSelected()
	 * Select or unselect this image
	 * @param selection New selection
	 */
	public void setSelected(boolean selection)
	{
		selected = selection;
		p.selectedImage = id;

		if(selection)
		{
			if(p.p.debug.viewer && p.p.debug.detailed)
				p.p.display.message("Selected image:"+id);

			displayMetadata();
		}
	}

	/**
	 * adjustCaptureLocation()
	 * Move the capture location to the associated cluster location
	 */
	public void adjustCaptureLocation()
	{
		if(cluster != -1)
		{
			captureLocation = p.clusters.get(cluster).getLocation();
//			calculateVertices();
		}
		else
		{
			disabled = true;
		}
	}

	/**
	 * setAssociatedCluster()
	 * @param newCluster New associated cluster
	 * Set nearest cluster to the capture location to be the associated cluster
	 */	
	void setAssociatedCluster(int newCluster)    				 // Associate cluster that is closest to photo
	{
		cluster = newCluster;
	}

	void setID(int newID)
	{
		id = newID;
	}
//
//	public void setCaptureLocation(PVector newCaptureLocation)
//	{
//		captureLocation = newCaptureLocation;
//	}
//	

	/**
	 * isVisible()
	 * @return Whether the media is visible
	 */
	public boolean isVisible()
	{
		return visible;
	}

	/**
	 * isActive()
	 * @return Whether the media is active at the current time
	 */
	public boolean isActive()
	{
		return active;
	}

	/**
	 * isFading()
	 * @return Whether the image is currently fading in or out
	 */
	public boolean isFading()
	{
		if(disabled || hidden)
			return false;

		if(fading)
			return true;
		else if(isFadingIn)
			return true;
		else if(isFadingOut)
			return true;
		else
			return false;
	}

	/**
	 * isDisabled()
	 * @return Whether the media is disabled
	 */
	public boolean isDisabled()
	{
		return disabled;
	}

	/**
	 * @return Whether the media is hidden
	 */
	public boolean isHidden()
	{
		return hidden;
	}

	 public int getID()
	 {
		 return id;
	 }
	
	 public float getTime()
	 {
		 return time.getTime();
	 }
	 
	 public float getDate()
	 {
		 return time.getDate();
	 }
	 
	 public String getName()
	 {
		 return name;
	 }
	 
	 public String getFilePath()
	 {
		 return filePath;
	 }
	 
	 public PVector getLocation()
	 {
		 return location;
	 }

	 public void setLocation(PVector newLocation)
	 {
		 location = newLocation;
	 }
	 
	 public PVector getCaptureLocation()
	 {
		 return captureLocation;
	 }
	 
	 public PVector getGPSLocation()
	 {
		 return gpsLocation;
	 }

	 public int getCameraModel()
	 {
		 return cameraModel;
	 }
	 
	 public float getBrightness()
	 {
		 return brightness;
	 }
	 
	 public boolean isSelected()
	 {
		 return selected;
	 }

	 public int getCluster()
	 {
		 return cluster;
	 }
}
