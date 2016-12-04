package wmViewer;

import java.util.Calendar;

import processing.core.PApplet;
import processing.core.PMatrix3D;

import processing.core.PVector;
import toxi.math.ScaleMap;

/***************************************
 * @author davidgordon
 * Abstract class representing objects viewable in 3D virtual space
 */
public abstract class WMV_Viewable 
{
	/* General */
	private int id;

	/* File System */
	private String name = "";
	public String filePath = "";

	/* Model */
	public PVector captureLocation;				// Media capture location in simulation – EXIF GPS coords scaled to fieldSize.
	public PVector location;        			// Media location in simulation 
	int cluster = -1;				 			// Cluster it belongs to	
	float theta = 0;                			// Media Orientation (in Degrees N)
	public boolean fadingObjectDistance = false, beginFadingObjectDistance = false;			// Fading distance of object in image?

	/* Metadata */
	public PVector gpsLocation;            		// Location in original GPS coords
	public int cameraModel;                 	// Camera model
	public float brightness;

	/* Time */
	WMV_Time time;
	ScaleMap timeLogMap;
	public float clusterDate, clusterTime;		// Date and time relative to other images in cluster (position between 0. and 1.)

	/* Interaction */
	private boolean selected = false;

	/* Graphics */
	public float aspectRatio = (float) 0.666;	// Aspect ratio of image or texture
	public PVector azimuthAxis = new PVector(0, 1, 0);
	public PVector verticalAxis = new PVector(1, 0, 0);
	public PVector rotationAxis = new PVector(0, 0, 1);
	public float centerSize = 0.05f;

	/* Transparency */
	public float viewingBrightness = 0;			// Final image brightness (or alpha in useAlphaFading mode) 
	public boolean isFadingIn = false, isFadingOut = false;
	public float fadingBrightness;							// Media transparency due to fading in / out
	public boolean beginFading = false, fading = false;		
	public float fadingStart = 0.f, fadingTarget = 0.f, fadingStartFrame = 0.f, fadingEndFrame = 0.f; 
	public boolean fadedOut = false;			// Recently faded out
	public boolean fadedIn = false;

	/* Status Modes */
	public boolean visible = false;				// Media is currently visible and will be drawn
	public boolean active = false;				// True when the image has faded in and isn't fading out	-- Needed?
	public boolean disabled = false;			// Disabled due to errors or user and will not be drawn
	public boolean hidden = false;				// Hidden from view											-- Needed?
	public boolean requested = false;			// Indicates a recent request to load media from disk

	WMV_Field p;								// Parent field

	WMV_Viewable ( WMV_Field parent, int newID, String newName, String newFilePath, PVector newGPSLocation, float newTheta, 
			int newCameraModel, float newBrightness, Calendar newCalendar )
	{
		p = parent;

		name = newName;
		id = newID; 
		filePath = newFilePath;

		if(newCalendar != null)
			time = new WMV_Time( p.p, newCalendar );
		else
			time = null;

		gpsLocation = newGPSLocation;
		captureLocation = new PVector(0, 0, 0);

		cameraModel = newCameraModel;
		theta = newTheta;              
		brightness = newBrightness;

		fadingBrightness = 0.f;			
		fadingStart = 0.f;

		timeLogMap = new ScaleMap(0.f, 1.f, 0.f, 1.f);		/* Time fading interpolation */
		timeLogMap.setMapFunction(p.p.circularEaseOut);

		//		if(p.p.timeFading || p.p.dateFading)
		//			initFading = false;			/* No need for initial fade in during Time Fading Mode */
	}  

	abstract void draw();
	abstract void displayMetadata();
	abstract void loadMedia();

	/**
	 * Set clusterDate for this media based on media times in associated cluster
	 */
	void setClusterDate()
	{
		if(cluster != -1)
		{
			WMV_Cluster c = p.clusters.get(cluster);
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
			WMV_Cluster c = p.clusters.get(cluster);
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
			fadingStartFrame = p.p.p.frameCount;
			fadingEndFrame = fadingStartFrame + p.teleportLength;

			if(target > fadingBrightness)
				isFadingIn = true;
			else
				isFadingOut = true;
		}
		else
		{
			fading = false;
		}
	}

	/**
	 * Stop fading in / out video
	 */
	public void stopFading()
	{
		if(p.p.p.debug.viewable)
			PApplet.println("Stop fading for media:"+id);

		fadingEndFrame = p.p.p.frameCount;
		fadingStart = fadingBrightness;
		fading = false;

		if(isFadingOut) isFadingOut = false;
		if(isFadingIn) isFadingIn = false;

		//		if(initFading)
		//			initFading = false;
	}

	/**
	 * drawLocation()
	 * @param size Size to draw the video center
	 * Draw the video center as a colored sphere
	 */
	void drawLocation(float size)
	{
		p.p.p.pushMatrix();
		p.p.p.translate(location.x, location.y, location.z);

		p.p.p.fill(150, 0, 255, 150);
		p.p.p.sphere(size);
		PVector c = p.p.getCluster(cluster).getLocation();
		PVector loc = location;
		PVector cl = getCaptureLocation();
		p.p.p.popMatrix();

		p.p.p.pushMatrix();
		if(p.p.showMediaToCluster)
		{
			p.p.p.strokeWeight(5.f);
			p.p.p.stroke(40, 155, 255, 180);
			p.p.p.line(c.x, c.y, c.z, loc.x, loc.y, loc.z);
		}

		if(p.p.showCaptureToMedia)
		{
			p.p.p.strokeWeight(2.f);
			p.p.p.stroke(160, 100, 255, 120);
			p.p.p.line(cl.x, cl.y, cl.z, loc.x, loc.y, loc.z);
		}

		if(p.p.showCaptureToCluster)
		{
			p.p.p.strokeWeight(3.f);
			p.p.p.stroke(100, 55, 255, 180);
			p.p.p.line(c.x, c.y, c.z, cl.x, cl.y, cl.z);
		}

		p.p.p.popMatrix();
	}

	/**
	 * @return Distance from the image capture location to the camera
	 */
	float getCaptureDistance()       // Find distance from camera to point in virtual space where photo appears           
	{
		PVector camLoc;

//		if(p.p.orientationMode)
//			camLoc = p.p.viewer.getLocation();
//		else
		camLoc = p.p.viewer.getLocation();

		float distance = PVector.dist(captureLocation, camLoc);     

		return distance;
	}


	/**
	 * @return How far the image capture location is from a point
	 */
	float getCaptureDistanceFrom(PVector point)       // Find distance from camera to point in virtual space where photo appears           
	{
		float distance = PVector.dist(captureLocation, point);     
		return distance;
	}

	/**
	 * @return Time brightness factor between 0. and 1.
	 * Calculate media brightness based on distance (fades away in distance and as camera gets close)
	 */
	float getTimeBrightness()												
	{
		int cycleLength = p.p.timeCycleLength;				// Length of main time loop
		float centerTime = -1;								// Midpoint of visibility for this media 		
		float timeBrightness = 0.f;

		float length = p.p.defaultMediaLength;				// Start with default length

		int fadeInStart = 0;								// When image starts fading in
		int fadeInEnd = 0;									// When image reaches full brightness
		int fadeOutStart = 0;								// When image starts fading out
		int fadeOutEnd = 0;									// When image finishes fading out
		int fadeLength = fadeOutEnd - fadeOutStart;

		boolean error = false;
		float lower, upper;
		
		int curTime;
		
		if(p.p.timeMode == 0)								// Time Mode: Cluster
		{
			WMV_Cluster c = p.p.getCluster(cluster);			// Get cluster for this media

			curTime = c.currentTime;
			if(c.dateline.size() == 1)
			{
				lower = c.timeline.get(0).getLower() / p.p.clusterTimePrecision;							// Get cluster timeline lower bound
				upper = c.timeline.get(c.timeline.size()-1).getUpper() / p.p.clusterTimePrecision;			// Get cluster timeline upper bound
			}
			else
			{
				lower = c.timelines.get(0).get(0).getLower() / p.p.clusterTimePrecision;							// Get cluster timeline lower bound
				int lastIdx = c.timelines.size()-1;
				upper = c.timelines.get(lastIdx).get(c.timelines.get(lastIdx).size()-1).getUpper() / p.p.clusterTimePrecision;			// Get cluster timeline upper bound
			}
		}
		else										// Time Mode: Field
		{
			curTime = p.p.currentTime;
			lower = p.p.getCurrentField().timeline.get(0).getLower() / p.p.clusterTimePrecision;		// Check division					// Get cluster timeline lower bound
			upper = p.p.getCurrentField().timeline.get(p.p.getCurrentField().timeline.size()-1).getUpper() / p.p.clusterTimePrecision;		// Get cluster timeline upper bound
		}
		
		float timelineLength = upper - lower;
//		PApplet.println(">>> ID:"+getID()+" time:"+time.getTime()+" ---> lower:"+lower+" upper:"+upper+" timelineLength:"+timelineLength+" curTime:"+curTime);

		if(lower == upper)				// Only one cluster segment
		{
			centerTime = cycleLength / 2.f;
			length = p.p.timeCycleLength;		// -- Should depend on cluster it belongs to 

			if(selected && p.p.p.debug.time && p.p.p.debug.detailed)
			{
				PApplet.println("Only one cluster time segment, full length:"+length);
				PApplet.println("time:"+time.getTime()+" centerTime:"+centerTime+" dayLength:"+cycleLength);
			}

			fadeInStart = 0;											// Frame media starts fading in
			fadeInEnd = PApplet.round(centerTime - length / 4.f);		// Frame media reaches full brightness
			fadeOutStart = PApplet.round(centerTime + length / 4.f);	// Frame media starts fading out
			fadeOutEnd = cycleLength;									// Frame media finishes fading out
		}
		else
		{
			centerTime = PApplet.round(PApplet.map( time.getTime(), lower, upper, p.p.defaultMediaLength * 0.25f, 
					cycleLength - p.p.defaultMediaLength * 0.25f) );	// Calculate center time in cluster timeline

			fadeInStart = PApplet.round(centerTime - length / 2.f);		// Frame media starts fading in
			fadeInEnd = PApplet.round(centerTime - length / 4.f);		// Frame media reaches full brightness
			fadeOutStart = PApplet.round(centerTime + length / 4.f);	// Frame media starts fading out
			fadeOutEnd = PApplet.round(centerTime + length / 2.f);		// Frame media finishes fading out

			if(selected && p.p.p.debug.viewable && p.p.p.debug.detailed)
			{
				PApplet.println(" media length:"+length+" centerTime:"+centerTime+" cycleLength:"+cycleLength);
				PApplet.println(" lower:"+lower+" upper:"+upper);
				PApplet.println(" fadeInStart:"+fadeInStart+" fadeInEnd:"+fadeInEnd+" fadeOutStart:"+fadeOutStart+" fadeOutEnd:"+fadeOutEnd);
			}
		}	

		/* Adjust fading times to fit cycle length */
		if(fadeInStart < 0)
			PApplet.println("Error: fadeInStart before day start!!");

		if(fadeInStart > cycleLength)
			PApplet.println("Error: fadeInStart after day end!!");

		if(fadeInEnd > cycleLength)
		{
			error = true;
			if(p.p.p.debug.viewable)
			{
				PApplet.println("------Error: fadeInEnd after day end-----time:"+time.getTime()+" centerTime:"+centerTime+" lower:"+lower+" upper:"+upper+" dayLength:"+cycleLength);
				PApplet.println("-----fadeInStart:"+fadeInStart+" fadeInEnd:"+fadeInEnd+" fadeOutStart:"+fadeOutStart+" fadeOutEnd:"+fadeOutEnd);
				PApplet.println("-----cluster:"+cluster+" currentCluster:"+p.p.getCurrentCluster().getID()+" curClusterTimeSegment:"+p.p.viewer.currentClusterTimeSegment);
			}
		}

		if(fadeOutStart > cycleLength)
		{
			if(selected && p.p.p.debug.viewable)
				PApplet.println("Adjusting fadeOutStart");

			fadeOutStart = cycleLength-PApplet.round(length*0.25f);			// Adjust fadeOutStart
			fadeOutEnd = cycleLength;		// Frame media finishes fading out
		}

		if(fadeOutEnd > cycleLength)
		{
			if(selected && p.p.p.debug.viewable)
				PApplet.println("Adjusting fadeOutEnd");

			//				if(fadeOutEnd - fadeOutStart < fadeLength) 
			fadeOutStart = PApplet.round(cycleLength-length*0.25f);			// Adjust fadeOutStart
			fadeOutEnd = cycleLength;		// Frame media finishes fading out
		}

		if(selected && p.p.p.debug.viewable)
		{
			PApplet.println("time:"+time.getTime()+" centerTime:"+centerTime+" dayLength:"+cycleLength);
			PApplet.println("fadeInStart:"+fadeInStart+" fadeInEnd:"+fadeInEnd+" fadeOutStart:"+fadeOutStart+" fadeOutEnd:"+fadeOutEnd);
		}

		/* Set timeBrightness */
		if(curTime <= cycleLength)
		{
			if(curTime < fadeInStart || curTime > fadeOutEnd)			// If before fade in or after fade out
			{
				if(active)							// If image was active
					active = false;					// Set to inactive

				timeBrightness = 0.f;			   					// Zero visibility
			}
			else if(curTime < fadeInEnd)						// During fade in
			{
				if(!active)							// If image was not active
					active = true;

				timeBrightness = PApplet.constrain(PApplet.map(curTime, fadeInStart, fadeInEnd, 0.f, 1.f), 0.f, 1.f);   
				if(selected && p.p.p.debug.viewable)
					PApplet.println(" Fading In..."+id);
			}
			else if(curTime > fadeOutStart)					// During fade out
			{
				if(selected && p.p.p.debug.viewable)
					PApplet.println(" Fading Out..."+id);
				timeBrightness = PApplet.constrain(1.f - PApplet.map(curTime, fadeOutStart, fadeOutEnd, 0.f, 1.f), 0.f, 1.f);    
			}
			else													// After fade in, before fade out
			{
				if(selected && p.p.p.debug.viewable && p.p.p.debug.detailed)
					PApplet.println(" Full visibility...");				// Full visibility
				timeBrightness = 1.f;								
			}
		}
		else
		{
			if(active && curTime > fadeOutEnd)					// If image was active and has faded out
				active = false;										// Set to inactive
		}

		timeBrightness = (float)timeLogMap.getMappedValueFor(timeBrightness);   		// Logarithmic scaling

		if(selected && p.p.p.debug.viewable)
			PApplet.println("timeBrightness"+timeBrightness);

		if(!error)
			return timeBrightness;
		else
			return 0.f;
	}

	/**
	 * @return Date brightness factor between 0. and 1.
	 * Calculate media brightness based on distance (fades away in distance and as camera gets close)
	 */
	float getDateBrightness()												
	{
		int cycleLength = p.p.timeCycleLength;				// Length of main time loop
		float centerDate = -1;								// Midpoint of visibility for this media 		
		float dateBrightness = 0.f;

		float length = p.p.defaultMediaLength;				// Start with default length

		int fadeInStart = 0;								// When image starts fading in
		int fadeInEnd = 0;									// When image reaches full brightness
		int fadeOutStart = 0;								// When image starts fading out
		int fadeOutEnd = 0;									// When image finishes fading out
		int fadeLength = fadeOutEnd - fadeOutStart;

		boolean error = false;
		float lower, upper;
		
		int curTime;
		
		if(p.p.timeMode == 0)								// Time Mode: Cluster
		{
			WMV_Cluster c = p.p.getCluster(cluster);			// Get cluster for this media

			curTime = c.currentTime;
			lower = c.dateline.get(0).getLower();												// Get cluster timeline lower bound
			upper = c.dateline.get(c.timeline.size()-1).getUpper();							// Get cluster timeline upper bound
	//		float curLower = c.timeline.get(p.p.viewer.currentClusterTimeSegment).getLower();			// Get current segment lower bound
	//		float curUpper = c.timeline.get(p.p.viewer.currentClusterTimeSegment).getUpper();			// Get current segment lower bound
	//		float segmentLength = curUpper - curLower;
		}
		else										// Time Mode: Field
		{
			curTime = p.p.currentTime;
			lower = p.p.getCurrentField().dateline.get(0).getLower();												// Get cluster timeline lower bound
			upper = p.p.getCurrentField().dateline.get(p.p.getCurrentField().timeline.size()-1).getUpper();							// Get cluster timeline upper bound
		}
		float timelineLength = upper - lower;

		if(lower == upper)				// Only one cluster segment
		{
			centerDate = cycleLength / 2.f;
			length = p.p.timeCycleLength;		// -- Should depend on cluster it belongs to 

			//			if(selected && p.p.p.debug.time && p.p.p.debug.detailed)
			{
				PApplet.println("Only one cluster date segment, full length:"+length);
				PApplet.println("date:"+time.getTime()+" centerDate:"+centerDate+" dayLength:"+cycleLength);
				PApplet.println("lower:"+lower+" upper:"+upper+" timelineLength:"+timelineLength);
				//				PApplet.println("lower:"+lower+" upper:"+upper+" cLower:"+curLower+" cUpper:"+curUpper);
			}

			fadeInStart = 0;											// Frame media starts fading in
			fadeInEnd = PApplet.round(centerDate - length / 4.f);		// Frame media reaches full brightness
			fadeOutStart = PApplet.round(centerDate + length / 4.f);	// Frame media starts fading out
			fadeOutEnd = cycleLength;									// Frame media finishes fading out
		}
		else
		{
			centerDate = PApplet.round(PApplet.map( time.getTime(), lower, upper, p.p.defaultMediaLength * 0.25f, 
					cycleLength - p.p.defaultMediaLength * 0.25f) );	// Calculate center time in cluster timeline

			fadeInStart = PApplet.round(centerDate - length / 2.f);		// Frame media starts fading in
			fadeInEnd = PApplet.round(centerDate - length / 4.f);		// Frame media reaches full brightness
			fadeOutStart = PApplet.round(centerDate + length / 4.f);	// Frame media starts fading out
			fadeOutEnd = PApplet.round(centerDate + length / 2.f);		// Frame media finishes fading out

			//			if(selected && p.p.p.debug.viewable && p.p.p.debug.detailed)
			{
				PApplet.println(" Timeline length:"+timelineLength+" media length:"+length);
				
				PApplet.println(" media date:"+time.getTime()+" centerDate:"+centerDate+" cycleLength:"+cycleLength);
				PApplet.println(" lower:"+lower+" upper:"+upper);
				PApplet.println(" fadeInStart:"+fadeInStart+" fadeInEnd:"+fadeInEnd+" fadeOutStart:"+fadeOutStart+" fadeOutEnd:"+fadeOutEnd);
			}
		}	

		/* Adjust fading times to fit cycle length */
		if(fadeInStart > cycleLength)
			PApplet.println("Error: fadeInStart after day end!!");

		if(fadeInEnd > cycleLength)
		{
			error = true;
			if(p.p.p.debug.viewable)
			{
				PApplet.println("------Error: fadeInEnd after day end-----time:"+time.getTime()+" centerTime:"+centerDate+" lower:"+lower+" upper:"+upper+" dayLength:"+cycleLength);
				PApplet.println("-----fadeInStart:"+fadeInStart+" fadeInEnd:"+fadeInEnd+" fadeOutStart:"+fadeOutStart+" fadeOutEnd:"+fadeOutEnd);
				PApplet.println("-----cluster:"+cluster+" currentCluster:"+p.p.getCurrentCluster().getID()+" curClusterTimeSegment:"+p.p.viewer.currentClusterTimeSegment);
			}
		}

		if(fadeOutStart > cycleLength)
		{
			if(selected && p.p.p.debug.viewable)
				PApplet.println("Adjusting fadeOutStart");

			fadeOutStart = cycleLength-PApplet.round(length*0.25f);			// Adjust fadeOutStart
			fadeOutEnd = cycleLength;		// Frame media finishes fading out
		}

		if(fadeOutEnd > cycleLength)
		{
			if(selected && p.p.p.debug.viewable)
				PApplet.println("Adjusting fadeOutEnd");

			//				if(fadeOutEnd - fadeOutStart < fadeLength) 
			fadeOutStart = PApplet.round(cycleLength-length*0.25f);			// Adjust fadeOutStart
			fadeOutEnd = cycleLength;		// Frame media finishes fading out
		}

		if(selected && p.p.p.debug.viewable)
		{
			PApplet.println("time:"+time.getTime()+" centerTime:"+centerDate+" dayLength:"+cycleLength);
			PApplet.println("fadeInStart:"+fadeInStart+" fadeInEnd:"+fadeInEnd+" fadeOutStart:"+fadeOutStart+" fadeOutEnd:"+fadeOutEnd);
		}

		/* Set timeBrightness */
		if(curTime <= cycleLength)
		{
			if(curTime < fadeInStart || curTime > fadeOutEnd)			// If before fade in or after fade out
			{
				if(active)							// If image was active
					active = false;					// Set to inactive

				dateBrightness = 0.f;			   					// Zero visibility
			}
			else if(curTime < fadeInEnd)						// During fade in
			{
				if(!active)							// If image was not active
					active = true;

				dateBrightness = PApplet.constrain(PApplet.map(curTime, fadeInStart, fadeInEnd, 0.f, 1.f), 0.f, 1.f);   
				if(selected && p.p.p.debug.viewable)
					PApplet.println(" Fading In..."+id);
			}
			else if(curTime > fadeOutStart)					// During fade out
			{
				if(selected && p.p.p.debug.viewable)
					PApplet.println(" Fading Out..."+id);
				dateBrightness = PApplet.constrain(1.f - PApplet.map(curTime, fadeOutStart, fadeOutEnd, 0.f, 1.f), 0.f, 1.f);    
			}
			else													// After fade in, before fade out
			{
				if(selected && p.p.p.debug.viewable && p.p.p.debug.detailed)
					PApplet.println(" Full visibility...");				// Full visibility
				dateBrightness = 1.f;								
			}
		}
		else
		{
			if(active && curTime > fadeOutEnd)					// If image was active and has faded out
				active = false;										// Set to inactive
		}

		dateBrightness = (float)timeLogMap.getMappedValueFor(dateBrightness);   		// Logarithmic scaling

		if(selected && p.p.p.debug.viewable)
			PApplet.println("timeBrightness"+dateBrightness);

		if(!error)
			return dateBrightness;
		else
			return 0.f;
	}

	/**
	 * Update viewingBrightnessFadeValue each frame
	 */
	void updateFadingBrightness()
	{
		float newFadeValue = 0.f;

		if(beginFading)
		{
			fadingStartFrame = p.p.p.frameCount;					
			fadingEndFrame = p.p.p.frameCount + p.teleportLength;	
			beginFading = false;
		}

		if (p.p.p.frameCount >= fadingEndFrame)
		{
			fading = false;
			newFadeValue = fadingTarget;

			//			if(initFading) initFading = false;
			if(isFadingOut)
			{
				isFadingOut = false;
				fadedOut = true;
			}
			if(isFadingIn)
			{
				isFadingIn = false;
				fadedIn = true;
			}
		} 
		else
		{
			newFadeValue = PApplet.map(p.p.p.frameCount, fadingStartFrame, fadingEndFrame, fadingStart, fadingTarget);      // Fade with distance from current time
		}

		fadingBrightness = newFadeValue;
	}

	/**
	 * Calculate media capture location in virtual space based on GPS location
	 */
	void calculateCaptureLocation()                                  
	{
		float newX = 0.f, newZ = 0.f, newY = 0.f;

		if(p.model.highLongitude != -1000000 && p.model.lowLongitude != 1000000 && p.model.highLatitude != -1000000 && p.model.lowLatitude != 1000000 && p.model.highAltitude != -1000000 && p.model.lowAltitude != 1000000)
		{
			if(p.model.highLongitude != p.model.lowLongitude && p.model.highLatitude != p.model.lowLatitude)
			{
				newX = PApplet.map(gpsLocation.x, p.model.lowLongitude, p.model.highLongitude, -0.5f * p.model.fieldWidth, 0.5f*p.model.fieldWidth); 			// GPS longitude decreases from left to right
				newY = PApplet.map(gpsLocation.y, p.model.lowAltitude, p.model.highAltitude, 0.f, p.model.fieldHeight); 										// Convert altitude feet to meters, negative sign to match P3D coordinate space
				newZ = -PApplet.map(gpsLocation.z, p.model.lowLatitude, p.model.highLatitude, -0.5f * p.model.fieldLength, 0.5f*p.model.fieldLength); 			// GPS latitude increases from bottom to top, minus sign to match P3D coordinate space

				if(p.p.altitudeScaling)	
				{
					//					newY = 0.f;
					newY *= p.p.altitudeScalingFactor;
				}
			}
			else
			{
				newX = newY = newZ = 0.f;
			}
		}

		captureLocation = new PVector(newX, newY, newZ);
	}


	/**
	 * Select or unselect this image
	 * @param selection New selection
	 */
	public void setSelected(boolean selection)
	{
		selected = selection;
		//		p.selectedImage = id;

		if(selection)
		{
			if(p.p.p.debug.viewer && p.p.p.debug.detailed)
				p.p.display.message("Selected image:"+id);

			displayMetadata();
		}
	}

	/**
	 * Move the capture location to the associated cluster location
	 */
	public void adjustCaptureLocation()
	{
		if(cluster != -1)
		{
			captureLocation = p.clusters.get(cluster).getLocation();
		}
		else
		{
			disabled = true;
		}
	}

	/**
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

	/**
	 * @return Whether the media is visible
	 */
	public boolean isVisible()
	{
		return visible;
	}

	/**
	 * @return Whether the media is active at the current time
	 */
	public boolean isActive()
	{
		return active;
	}

	/**
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

	/**
	 * Rotate list of vertices using matrices
	 * @param verts Vertices list
	 * @param angle Angle to rotate by
	 * @param axis Axis to rotate around
	 * @return Rotated vertices
	 */
	public PVector[] rotateVertices(PVector[] verts, float angle, PVector axis) 
	{
		boolean failed = false;
		int vl = verts.length;
		PVector[] clone = new PVector[vl];
		PVector[] dst = new PVector[vl];

		try
		{
			for (int i = 0; i < vl; i++)
			{
				if(verts[i]!=null)
					clone[i] = PVector.add(verts[i], new PVector());
				else
					PApplet.println("verts["+i+"] is null!!");
			}

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
			PApplet.println("Failed rotating vertices!");
			return new PVector[0];
		}
	}

	/**
	 * Translate list of vertices using matrices
	 * @param verts Vertices list
	 * @param dest Destination vector
	 * @return Translated vertices 
	 */
	public PVector[] translateVertices(PVector[] verts, PVector dest) // Translate vertices to a designated point
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
}
