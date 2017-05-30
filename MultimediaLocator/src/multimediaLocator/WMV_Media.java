package multimediaLocator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;

import toxi.math.CircularInterpolation;
import toxi.math.InterpolateStrategy;
import toxi.math.ScaleMap;

/***************************************
 * Media object viewable in a 3D environment
 * @author davidgordon
 * 
 */
public abstract class WMV_Media
{
	/* Classes */
	private WMV_MediaState mState;				// Media state
	private WMV_WorldSettings worldSettings;	// World settings
	private WMV_WorldState worldState;			// World State
	private WMV_ViewerSettings viewerSettings;	// Viewer settings
	private WMV_ViewerState viewerState;		// Viewer state
	private ML_DebugSettings debugSettings;		// Debug settings
	
	/* Time */
	private ScaleMap timeLogMap;
	private InterpolateStrategy circularEaseOut = new CircularInterpolation(false);		// Steepest ascent at beginning
	public WMV_Time time;						// Media time						

	/**
	 * Constructor for abstract media object
	 * @param newID Media ID
	 * @param newMediaType Media type ID
	 * @param newName File name
	 * @param newFilePath File path
	 * @param newDateTime Capture date/time
	 * @param newTimeZone Time zone ID
	 * @param newGPSLocation GPS location
	 */
	public WMV_Media( int newID, int newMediaType, String newName, String newFilePath, ZonedDateTime newDateTime, String newTimeZone,
				PVector newGPSLocation )
	{
		mState = new WMV_MediaState();

		mState.name = newName;								// -- Temporary
		mState.filePath = newFilePath;
		mState.dateTime = newDateTime;
		mState.timeZone = newTimeZone;
		mState.gpsLocation = newGPSLocation;
	
		mState.id = newID; 
		mState.mediaType = newMediaType;
		mState.captureLocation = new PVector(0, 0, 0);

		mState.fadingBrightness = 0.f;			
		mState.fadingStart = 0.f;

		timeLogMap = new ScaleMap(0.f, 1.f, 0.f, 1.f);		/* Time fading interpolation */
		timeLogMap.setMapFunction(circularEaseOut);
		
		debugSettings = new ML_DebugSettings();
	}  

	abstract void loadMedia(MultimediaLocator ml);
	abstract void display(MultimediaLocator ml);
	abstract void displayModel(MultimediaLocator ml);
	abstract void displayMetadata(MultimediaLocator ml);

	/**
	 * Update media about world and viewer states
	 * @param newWorldSettings
	 * @param newWorldState
	 * @param newViewerSettings
	 * @param newViewerState
	 */
	public void updateWorldState( WMV_WorldSettings newWorldSettings, WMV_WorldState newWorldState, WMV_ViewerSettings newViewerSettings, 
								WMV_ViewerState newViewerState )
	{
		worldSettings = newWorldSettings;
		worldState = newWorldState;
		viewerSettings = newViewerSettings;
		viewerState = newViewerState;
	}

	/**
	 * Update media time brightness 
	 * @param c Associated cluster
	 * @param fieldTimeline Field timeline
	 * @param utilities Utilities object
	 */
	public void updateTimeBrightness(WMV_Cluster c, WMV_Timeline fieldTimeline, WMV_Utilities utilities)
	{
		float centerTime = -1;								// Midpoint of visibility for this media 		
		float length = worldSettings.defaultMediaLength;				// Start with default length

		int fadeInStart = 0;								// When image starts fading in
		int fadeInEnd = 0;									// When image reaches full brightness
		int fadeOutStart = 0;								// When image starts fading out
		int fadeOutEnd = 0;									// When image finishes fading out

		boolean error = false;
		float lower = 0, upper = 0;
		int curTime = 0;
		
		setTimeBrightness(0.f);

		switch( worldState.getTimeMode() )
		{
			case 0:															// Time Mode: Cluster
				curTime = c.getState().currentTime;									// Set image time from cluster
				lower = c.getTimeline().getLower().getLower().getTime();			// Get cluster timeline lower bound
				upper = c.getTimeline().getUpper().getUpper().getTime();			// Get cluster timeline upper bound
			break;
		
			case 1:															// Time Mode: Field
				curTime = worldState.currentTime;
				lower = fieldTimeline.getLower().getLower().getTime();		// Check division					// Get cluster timeline lower bound
				upper = fieldTimeline.getUpper().getUpper().getTime();		// Get cluster timeline upper bound
				break;
				
			case 2:															// Time Mode: (Single) Media
				curTime = worldState.currentTime;
				break;
		}
		
		if(worldState.getTimeMode() == 0 || worldState.getTimeMode() == 1)
		{
			if(lower == upper)				// Only one time segment in cluster; fade for full timeline length
			{
				centerTime = worldSettings.timeCycleLength / 2.f;
				length = worldSettings.timeCycleLength;						 

				if(debugSettings.video && getType() == 2 && debugSettings.detailed)
					System.out.println("Only one cluster time segment, full length:"+length+" -- time:"+time.getTime()+" centerTime:"+centerTime+" dayLength:"+worldSettings.timeCycleLength);

				fadeInStart = 0;											// Frame media starts fading in
				fadeInEnd = Math.round(centerTime - length / 4.f);			// Frame media reaches full state.brightness
				fadeOutStart = Math.round(centerTime + length / 4.f);		// Frame media starts fading out
				fadeOutEnd = worldSettings.timeCycleLength;									// Frame media finishes fading out
			}
			else							// Multiple time segments in cluster
			{
				if(time == null)
					System.out.println("time == null!!");
				
				float mediaTime = time.getTime();							// Get media time 
				
				if(mediaTime < lower)
				{
					System.out.println("-- mediaTime < lower!!  cluster:"+mState.getClusterID()+" == "+c.getID()+" time: "+mediaTime+" lower: "+lower+" worldState.getTimeMode():"+worldState.getTimeMode());
					error = true;
				}
				
				if(mediaTime > upper)
				{
					System.out.println("-- mediaTime > upper!!  cluster:"+mState.getClusterID()+" == "+c.getID()+" time: "+mediaTime+" upper: "+upper+" worldState.getTimeMode():"+worldState.getTimeMode());
					error = true;
				}

				centerTime = Math.round(PApplet.map( mediaTime, lower, upper, length, worldSettings.timeCycleLength - length) );	// Calculate center time in cluster timeline

				fadeInStart = Math.round(centerTime - length / 2.f);		// Frame media starts fading in
				fadeInEnd = Math.round(centerTime - length / 4.f);			// Frame media reaches full state.brightness
				fadeOutStart = Math.round(centerTime + length / 4.f);		// Frame media starts fading out
				fadeOutEnd = Math.round(centerTime + length / 2.f);			// Frame media finishes fading out
				if(fadeOutEnd > worldSettings.timeCycleLength)
					fadeOutEnd = worldSettings.timeCycleLength;
			}	

			/* Debugging */
			if(fadeInStart < 0)
			{
				error = true;
				if(debugSettings.ml)
				{
					System.out.println(">>> Error: fadeInStart before cycle start-----time:"+time.getTime()+" centerTime:"+centerTime+" lower:"+lower+" upper:"+upper+" dayLength:"+worldSettings.timeCycleLength);
					System.out.println(" ------ fadeInStart:"+fadeInStart+" fadeInEnd:"+fadeInEnd+" fadeOutStart:"+fadeOutStart+" fadeOutEnd:"+fadeOutEnd);
					System.out.println(" ----- cluster:"+mState.getClusterID()+" media type:"+getType()+" id:"+getID()+" time.getTime():"+time.getTime()+" lower:"+lower+" upper:"+upper+" ----- media length:"+length);
				}
			}

			if(fadeInStart > worldSettings.timeCycleLength)
			{
				error = true;
				if(debugSettings.ml)
				{
					System.out.println(">>> Error: fadeInStart after cycle end-----time:"+time.getTime()+" centerTime:"+centerTime+" lower:"+lower+" upper:"+upper+" dayLength:"+worldSettings.timeCycleLength);
					System.out.println("----- fadeInStart:"+fadeInStart+" fadeInEnd:"+fadeInEnd+" fadeOutStart:"+fadeOutStart+" fadeOutEnd:"+fadeOutEnd+" worldState.getTimeMode():"+worldState.getTimeMode());
					System.out.println("-----cluster:"+mState.getClusterID()+" media type:"+getType()+" id:"+getID()+" time.getTime():"+time.getTime()+" lower:"+lower+" upper:"+upper+" media length:"+length);
				}
			}

			if(fadeInEnd > worldSettings.timeCycleLength)
			{
				error = true;
				if(debugSettings.ml)
				{
					System.out.println(">>> Error: fadeInEnd after cycle end-----time:"+time.getTime()+" centerTime:"+centerTime+" lower:"+lower+" upper:"+upper+" dayLength:"+worldSettings.timeCycleLength);
					System.out.println("-----fadeInStart:"+fadeInStart+" fadeInEnd:"+fadeInEnd+" fadeOutStart:"+fadeOutStart+" fadeOutEnd:"+fadeOutEnd+"-----cluster:"+mState.getClusterID()+" media type:"+getType()+" id:"+getID()+" worldState.getTimeMode():"+worldState.getTimeMode()+" media length:"+length);
				}
			}

			if(fadeOutStart > worldSettings.timeCycleLength)
			{
				error = true;
				if(debugSettings.ml)
				{
					System.out.println(">>> Error: fadeOutStart after cycle end-----time:"+time.getTime()+" centerTime:"+centerTime+" lower:"+lower+" upper:"+upper+" dayLength:"+worldSettings.timeCycleLength);
					System.out.println("-----fadeInStart:"+fadeInStart+" fadeInEnd:"+fadeInEnd+" fadeOutStart:"+fadeOutStart+" fadeOutEnd:"+fadeOutEnd+"-----cluster:"+mState.getClusterID()+" media type:"+getType()+" id:"+getID()+" worldState.getTimeMode():"+worldState.getTimeMode()+" media length:"+length);
				}
			}

			if(fadeOutEnd > worldSettings.timeCycleLength)
			{
				error = true;
				if(debugSettings.ml)
				{
					System.out.println(">>> Error: fadeOutEnd after cycle end-----time:"+time.getTime()+" centerTime:"+centerTime+" lower:"+lower+" upper:"+upper+" dayLength:"+worldSettings.timeCycleLength+" media type:"+getType());
					System.out.println("-----fadeInStart:"+fadeInStart+" fadeInEnd:"+fadeInEnd+" fadeOutStart:"+fadeOutStart+" fadeOutEnd:"+fadeOutEnd+"-----cluster:"+mState.getClusterID()+" media type:"+getType()+" id:"+getID()+" worldState.getTimeMode():"+worldState.getTimeMode());
				}
			}
		}
		else if(worldState.getTimeMode() == 2)
		{
			if(mState.isCurrentMedia)
			{
				fadeInStart = viewerState.getCurrentMediaStartTime();				// Frame media starts fading in
				fadeInEnd = Math.round(fadeInStart + length / 4.f);		// Frame media reaches full state.brightness
				fadeOutEnd = fadeInStart + worldSettings.defaultMediaLength;									// Frame media finishes fading out
				fadeOutStart = Math.round(fadeOutEnd - length / 4.f);	// Frame media starts fading out
			}
			else
				setTimeBrightness(0.f);
		}
		
		/* Calculate and set time brightness */
		calculateAndSetTimeBrightness(curTime, worldSettings.timeCycleLength, fadeInStart, fadeInEnd, fadeOutStart, fadeOutEnd);

		if(error)
		{
			if(debugSettings.ml) System.out.println("Time Brightness Error for media id:" + getID()+" type:"+ getType()+" set timeBrightness to :"+mState.timeBrightness);

			setTimeBrightness( 1.f );
		}
	}

	/**
	 * Calculate and set time brightness
	 * @param curTime Current time
	 * @param cycleLength Time cycle length
	 * @param fadeInStart Fade in start
	 * @param fadeInEnd Fade in end
	 * @param fadeOutStart Fade in start
	 * @param fadeOutEnd Fade out end
	 */
	private void calculateAndSetTimeBrightness( float curTime, float cycleLength, float fadeInStart,
												float fadeInEnd, float fadeOutStart, float fadeOutEnd )
	{
		if(curTime <= cycleLength)
		{
			if(curTime < fadeInStart || curTime > fadeOutEnd)			// If before fade in or after fade out
			{
				if(mState.active)							// If image was vState.active
					mState.active = false;					// Set to invState.active

				setTimeBrightness(0.f);			   					// Zero visibility
				
				if(worldState.getTimeMode() == 2) 
					if(mState.isCurrentMedia)
						mState.isCurrentMedia = false;	// No longer the current media in Single Time Mode
			}
			else if(curTime < fadeInEnd)						// During fade in
			{
				if(!mState.active)							// If image was not vState.active
					mState.active = true;

				setTimeBrightness( PApplet.constrain(PApplet.map(curTime, fadeInStart, fadeInEnd, 0.f, 1.f), 0.f, 1.f) );   
			}
			else if(curTime > fadeOutStart)					// During fade out
			{
				setTimeBrightness( PApplet.constrain(1.f - PApplet.map(curTime, fadeOutStart, fadeOutEnd, 0.f, 1.f), 0.f, 1.f) ); 
				if(worldState.getTimeMode() == 2 && mState.isCurrentMedia)
				{
					if(fadeOutEnd - curTime == 1)
						mState.isCurrentMedia = false;	// No longer the current media in Single Time Mode
				}
			}
			else													// After fade in, before fade out
			{
				mState.timeBrightness = 1.f;								
			}
		}
		else
		{
			if(curTime > fadeOutEnd)					// If image was vState.active and has faded out
			{
				if(mState.active) mState.active = false;									// Set to invState.active
				if(worldState.getTimeMode() == 2) 
				{
					mState.isCurrentMedia = false;	// No longer the current media in Single Time Mode
				}			
			}
		}

		setTimeBrightness( (float)timeLogMap.getMappedValueFor(mState.timeBrightness) );   		// Logarithmic scaling
		if(isSelected() && debugSettings.time) System.out.println("Media id:" + getID()+" state.timeBrightness"+mState.timeBrightness);

	}

	/**
	 * Fade in media and add to visible list
	 */
	public void fadeIn(WMV_Field f)
	{
		if(isFading() || isFadingIn() || isFadingOut())		// If already fading, stop at current value
			stopFading();

		startFading(1.f);					// Fade in
		switch(mState.mediaType)			// Media Type,  0: image 1: panorama 2: video 3: sound 
		{
			case 0:
				f.visibleImages.add(getID());
				break;
			case 1:
				f.visiblePanoramas.add(getID());
				break;
			case 2:
				f.visibleVideos.add(getID());
				break;
			case 3:
				f.audibleSounds.add(getID());
				break;
		}
	}

	/**
	 * Fade out media and remove from visible list
	 */
	public void fadeOut(WMV_Field f)
	{
		if(isFading() || isFadingIn() || isFadingOut())		// If already fading, stop at current value
			stopFading();

		if(isSeen()) setSeen(false);
		startFading(0.f);					// Fade out
	}

	/**
	 * Start alpha transition from current to given value
	 * @param target Target alpha value
	 */
	void startFading(float target)
	{
		if(target != mState.fadingBrightness)			// Check if already at brightness target
		{
			mState.beginFading = true;
			mState.fading = true;   
			mState.fadingStart = mState.fadingBrightness;
			mState.fadingTarget = target;
			mState.fadingStartFrame = worldState.frameCount;
			mState.fadingEndFrame = mState.fadingStartFrame + viewerSettings.teleportLength;

			if(target > mState.fadingBrightness)
				mState.isFadingIn = true;
			else
				mState.isFadingOut = true;
		}
		else
			mState.fading = false;
	}

	/**
	 * Stop fading in or out
	 */
	public void stopFading()
	{
		mState.fadingEndFrame = worldState.frameCount;
		mState.fadingStart = mState.fadingBrightness;
		mState.fading = false;

		if(isFadingOut()) mState.isFadingOut = false;
		if(isFadingIn()) mState.isFadingIn = false;
	}
	
	/**
	 * Update fading brightness
	 */
	void updateFadingBehavior(WMV_Field f)
	{
		float newFadeValue = 0.f;

		if(mState.beginFading)
		{
			mState.fadingStartFrame = worldState.frameCount;					
			mState.fadingEndFrame = worldState.frameCount + viewerSettings.teleportLength;	
			mState.beginFading = false;
		}

		if (worldState.frameCount >= mState.fadingEndFrame)
		{
			mState.fading = false;
			if(isFadingIn())
			{
				if(mState.fadingTarget == 1.f)
				{
					newFadeValue = mState.fadingTarget;
					mState.isFadingIn = false;
					mState.fadedIn = true;
				}
				else
					System.out.println("Fading in but target == "+mState.fadingTarget);
			}
			else if(isFadingOut())
			{
				if(mState.fadingTarget == 0.f)
				{
					newFadeValue = mState.fadingTarget;
					mState.isFadingOut = false;
					mState.fadedOut = true;
					
					switch(mState.mediaType)				// Remove from visible / audible list once faded out 
					{
						case 0:
							if(f.visibleImages.contains(getID()))
								f.visibleImages.remove(f.visibleImages.indexOf(getID()));
							break;
						case 1:
							if(f.visiblePanoramas.contains(getID()))
								f.visiblePanoramas.remove(f.visiblePanoramas.indexOf(getID()));
							break;
						case 2:
							if(f.visibleVideos.contains(getID()))
								f.visibleVideos.remove(f.visibleVideos.indexOf(getID()));
							break;
						case 3:
							if(f.audibleSounds.contains(getID()))
								f.audibleSounds.remove(f.audibleSounds.indexOf(getID()));
							break;
					}
				}
				else
					System.out.println("Fading out but target == "+mState.fadingTarget);
			}
		} 
		else
		{
			newFadeValue = PApplet.map(worldState.frameCount, mState.fadingStartFrame, mState.fadingEndFrame, mState.fadingStart, mState.fadingTarget);      // Fade with distance from current time
		}

		mState.fadingBrightness = newFadeValue;
//		if(mState.fadingBrightness == 0.f) setVisible(false);
	}

	/**
	 * Calculate media capture state.location in virtual space based on GPS location
	 * @param model Field model
	 */
	void calculateCaptureLocation(WMV_Model model)                                  
	{
		float newX = 0.f, newZ = 0.f, newY = 0.f;
		
		if(model.getState().highLongitude != -1000000 && model.getState().lowLongitude != 1000000 && model.getState().highLatitude != -1000000 && model.getState().lowLatitude != 1000000 && model.getState().highAltitude != -1000000 && model.getState().lowAltitude != 1000000)
		{
			if(model.getState().highLongitude != model.getState().lowLongitude && model.getState().highLatitude != model.getState().lowLatitude)
			{
				newX = PApplet.map(mState.gpsLocation.x, model.getState().lowLongitude, model.getState().highLongitude, -0.5f * model.getState().fieldWidth, 0.5f*model.getState().fieldWidth); 			// GPS longitude decreases from left to right
				newY = -PApplet.map(mState.gpsLocation.y, model.getState().lowAltitude, model.getState().highAltitude, 0.f, model.getState().fieldHeight); 										// Convert altitude feet to meters, negative sign to match P3D coordinate space
				newZ = PApplet.map(mState.gpsLocation.z, model.getState().lowLatitude, model.getState().highLatitude, 0.5f*model.getState().fieldLength, -0.5f * model.getState().fieldLength); 			// GPS latitude increases from bottom to top, reversed to match P3D coordinate space
				
				if(worldSettings != null)
				{
					if(worldSettings.altitudeScaling)	
						newY *= worldSettings.altitudeScalingFactor;
				}
				else
					newY *= mState.defaultAltitudeScalingFactor;
			}
			else
			{
				newX = newY = newZ = 0.f;
			}
		}

		mState.captureLocation = new PVector(newX, newY, newZ);
	}
	
	/**
	 * Find and set associated cluster for this media from given cluster list
	 * @return Whether associated field was successfully found
	 */	
	public boolean findAssociatedCluster(ArrayList<WMV_Cluster> clusterList, float maxClusterDistance)    				 // Associate cluster that is closest to photo
	{
		int closestClusterIndex = 0;
		float closestDistance = 100000;

//		System.out.println("findAssociatedCluster()... Media #"+getID()+" Media type:"+getMediaType()+" clusterList.size():"+clusterList.size()+" getCaptureLocation():"+getCaptureLocation());
		for (int i = 0; i < clusterList.size(); i++) 
		{     
			WMV_Cluster curCluster = clusterList.get(i);
			float distance = getCaptureLocation().dist(curCluster.getLocation());
//			System.out.println("   :"+getCaptureLocation()+" Cluster #"+i+" location:"+curCluster.getLocation()+" distance:"+distance+" closestDistance:"+closestDistance+" maxClusterDistance:"+maxClusterDistance);

			if (distance < closestDistance)
			{
				closestClusterIndex = i;
				closestDistance = distance;
			}
		}

		if(closestDistance < maxClusterDistance)
			setAssociatedClusterID(closestClusterIndex);		// Associate image with cluster
		else
			setAssociatedClusterID(-1);							// Create a new single image cluster here!

		if(getAssociatedClusterID() != -1)
			return true;
		else
			return false;
	}

	/**
	 * Move the capture state.location to the associated cluster state.location
	 */
	public void adjustCaptureLocation(WMV_Cluster mediaCluster)
	{
		if(mState.getClusterID() != -1)
			mState.captureLocation = mediaCluster.getLocation();
		else
			mState.disabled = true;
	}

	/**
	 * @return Distance from the image capture state.location to the camera
	 */
	public float getCaptureDistance()       // Find distance from camera to media capture location
	{
		PVector camLoc = viewerState.getLocation();
		float distance = PVector.dist(mState.captureLocation, camLoc);     
		return distance;
	}

	/**
	 * @return How far the image capture state.location is from a point
	 */
	public float getCaptureDistanceFrom(PVector point)       // Find distance from camera to point in virtual space where photo appears           
	{
		float distance = PVector.dist(mState.captureLocation, point);     
		return distance;
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
//				else
//					System.out.println("verts["+i+"] is null!!");
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
			System.out.println("NullPointerException: "+e);
			failed = true;
		}
		if(!failed)
		{
			return dst;
		}
		else
		{
//			System.out.println("Failed rotating vertices! for media id #"+getID()+" type:"+getMediaType());
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

	/**
	 * Set media state
	 * @param newMediaState New media state
	 */
	public void setMediaState( WMV_MediaState newMediaState )
	{
		mState = newMediaState;
	}

	/**
	 * @return Current media state
	 */
	public WMV_MediaState getMediaState()
	{
		return mState;
	}

	/**
	 * @return Time brightness factor between 0. and 1.
	 */
	public float getTimeBrightness()												
	{
		return mState.timeBrightness;
	}

	/**
	 * Set time brightness
	 */
	public void setTimeBrightness(float newTimeBrightness)												
	{
		mState.timeBrightness = newTimeBrightness;
	}

	/**
	 * Set cluster date for this media based on media times in associated cluster
	 */
	void setClusterDates(WMV_Cluster c)
	{
		if(mState.getClusterID() != -1)
		{
			mState.clusterLowDate = c.getState().lowDate;
			mState.clusterHighDate = c.getState().highDate;
		}
	}

	/**
	 * Set cluster time for this image based on media times in associated cluster
	 */
	void setClusterTimes( WMV_Cluster c )
	{
		if(mState.getClusterID() != -1)
		{
			mState.clusterLowTime = c.getState().lowTime;
			mState.clusterHighTime = c.getState().highTime;
		}
	}

	/**
	 * Set media id
	 * @param newID New media id
	 */
	void setID(int newID)
	{
		mState.id = newID;
	}

	/**
	 * Set whether media is visible
	 * @param newState New visibility state
	 */
	void setVisible(boolean newState)
	{
		mState.visible = newState;
	}
	
	/**
	 * @return Whether the media is visible
	 */
	public boolean isVisible()
	{
		return mState.visible;
	}
	
	/**
	 * Set whether media is being displayed
	 * @param newState New seen state
	 */
	void setSeen(boolean newState)
	{
		mState.seen = newState;
	}
	
	/**
	 * @return Whether the media is being displayed
	 */
	public boolean isSeen()
	{
		return mState.seen;
	}
	
	/**
	 * @return Whether the media is vState.active at the current time
	 */
	public boolean isActive()
	{
		return mState.active;
	}

	/**
	 * @return Whether the image is currently fading in or out
	 */
	public boolean isFading()
	{
		if(mState.disabled || mState.hidden)
			return false;

		if(mState.fading)
			return true;
		else if(mState.isFadingIn)
			return true;
		else if(mState.isFadingOut)
			return true;
		else
			return false;
	}

	/**
	 * Set whether media has faded in recently
	 * @param newState New faded in state
	 */
	public void setFadedIn(boolean newState)
	{
		mState.fadedIn = newState;
	}

	/**
	 * Set whether media has faded out recently
	 * @param newState New faded out state
	 */
	public void setFadedOut(boolean newState)
	{
		mState.fadedOut = newState;
	}
	
	public void setGPSLocation(PVector newGPSLocation)
	{
		mState.gpsLocation = newGPSLocation;
	}
	
	public boolean isFadingIn()
	{
		return mState.isFadingIn;
	}

	public boolean hasFadedIn()
	{
		return mState.fadedIn;
	}

	public boolean isFadingOut()
	{
		return mState.isFadingOut;
	}

	public boolean hasFadedOut()
	{
		return mState.fadedOut;
	}

	/**
	 * Set whether media is disabled 
	 * @param newState New disabled state
	 */
	public void setDisabled(boolean newState)
	{
		mState.disabled = newState;
	}

	/**
	 * @return Whether the media is disabled
	 */
	public boolean isDisabled()
	{
		return mState.disabled;
	}

	/**
	 * Set whether media is hidden
	 * @param newState New hidden state
	 */
	public void setHidden(boolean newState)
	{
		mState.hidden = newState;
	}

	/**
	 * @return Whether the media is hidden
	 */
	public boolean isHidden()
	{
		return mState.hidden;
	}

	public int getID()
	{
		return mState.id;
	}
	
	public int getMediaType()
	{
		return mState.mediaType;
	}

	public float getTime()
	{
		return time.getTime();
	}

	public WMV_Date getDate()
	{
		return time.asDate();
	}

	public String getName()
	{
		return mState.name;
	}

	public String getFilePath()
	{
		return mState.filePath;
	}

	public void setFilePath(String newFilePath)
	{
		mState.filePath = newFilePath;
	}
	
	public void setAspectRatio(float newAspectRatio)
	{
		mState.aspectRatio = newAspectRatio;
	}
	
	public float getAspectRatio()
	{
		return mState.aspectRatio;
	}
	
	public void setRequested(boolean newState)
	{
		mState.requested = newState;
	}
	
	public boolean isRequested()
	{
		return mState.requested;
	}
	
	/**
	 * Set nearest cluster to the capture state.location to be the associated cluster
	 * @param newCluster New associated cluster
	 */	
	public void setAssociatedClusterID(int newCluster)
	{
//		if(newCluster == -1) System.out.println("Media.setAssociatedClusterID()... Setting cluster for media #"+mState.id+" type "+mState.mediaType+" from "+mState.cluster+" to:"+newCluster);
		mState.setClusterID( newCluster );
	}
	
	/**
	 * @return Associated cluster
	 */
	public int getAssociatedClusterID()
	{
		return mState.getClusterID();
	}

	public void setLocation(PVector newLocation)
	{
		mState.location = newLocation;
	}
	
	public void addVectorToLocation(PVector disp)
	{
		mState.location.add(disp);     													 
	}
	
	public PVector getLocation()
	{
		return mState.location;
	}
	
	public void setCaptureLocation(PVector newCaptureLocation)
	{
		mState.captureLocation = newCaptureLocation;
	}

	public PVector getCaptureLocation()
	{
		return mState.captureLocation;
	}

	public PVector getGPSLocation()
	{
		return mState.gpsLocation;
	}

	public void setFadingFocusDistance(boolean newState)
	{
		mState.fadingFocusDistance = newState;
	}
	
	public boolean isFadingFocusDistance()
	{
		return mState.fadingFocusDistance;
	}

	public void setViewingBrightness(float newViewingBrightness)
	{
		mState.viewingBrightness = newViewingBrightness;
	}

	public float getViewingBrightness()
	{
		return mState.viewingBrightness;
	}

	public float getFadingBrightness()
	{
		return mState.fadingBrightness;
	}

	/**
	 * Select or deselect this image
	 * @param selection Whether to select or deselect
	 */
	public void setSelected(boolean selection)
	{
		mState.selected = selection;
		if(selection) mState.showMetadata = selection;
	}

	public boolean isSelected()
	{
		return mState.selected;
	}

	public void setMediaType(int newMediaType)
	{
		mState.mediaType = newMediaType;
	}
	
	public int getType()
	{
		return mState.mediaType;
	}
	
	public WMV_WorldSettings getWorldSettings()
	{
		return worldSettings;
	}
	
	public WMV_WorldState getWorldState()
	{
		return worldState;
	}
	
	public WMV_ViewerSettings getViewerSettings()
	{
		return viewerSettings;
	}
	
	public WMV_ViewerState getViewerState()
	{
		return viewerState;
	}
	
	public ML_DebugSettings getDebugSettings()
	{
		return debugSettings;
	}
	
	/**
	 * Parse date/time string from metadata given media time zone
	 * @param input String to parse
	 * @return ZonedDateTime object corresponding to given string
	 */
	public ZonedDateTime parseDateTime(String input) 					// 2016:04:10 17:52:39
	{		
		String[] parts = input.split(":");

		int year = Integer.valueOf(parts[0].trim());
		int month = Integer.valueOf(parts[1]);
		int min = Integer.valueOf(parts[3]);
		int sec = Integer.valueOf(parts[4]);
		input = parts[2];
		parts = input.split(" ");
		int day = Integer.valueOf(parts[0]);
		int hour = Integer.valueOf(parts[1]);

		ZonedDateTime pac = ZonedDateTime.of(year, month, day, hour, min, sec, 0, ZoneId.of(mState.timeZone));
		return pac;
	}
}
