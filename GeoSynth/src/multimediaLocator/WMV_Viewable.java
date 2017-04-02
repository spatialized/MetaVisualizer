package multimediaLocator;

import java.time.ZonedDateTime;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;

import toxi.math.CircularInterpolation;
import toxi.math.InterpolateStrategy;
import toxi.math.ScaleMap;

/***************************************
 * @author davidgordon
 * An object viewable in 3D space
 */
public abstract class WMV_Viewable
{
	/* Classes */
	private WMV_WorldSettings worldSettings;
	private WMV_WorldState worldState;
	private WMV_ViewerSettings viewerSettings;	// Update world settings
	private WMV_ViewerState viewerState;	// Update world settings
	private ML_DebugSettings debugSettings;	// Update world settings
	private WMV_MediaState mState;
	
	/* Time */
	private ScaleMap timeLogMap;
	private InterpolateStrategy circularEaseOut = new CircularInterpolation(false);		// Steepest ascent at beginning
	public WMV_Time time;

	WMV_Viewable ( int newID, int newMediaType, String newName, String newFilePath, PVector newGPSLocation, float newTheta, 
			int newCameraModel, float newBrightness, ZonedDateTime newDateTime, String newTimeZone )
	{
		mState = new WMV_MediaState();
		mState.name = newName;
		mState.id = newID; 
		mState.mediaType = newMediaType;
		mState.filePath = newFilePath;

		mState.gpsLocation = newGPSLocation;
		mState.captureLocation = new PVector(0, 0, 0);

		mState.cameraModel = newCameraModel;
		mState.theta = newTheta;              
		mState.brightness = newBrightness;

		mState.fadingBrightness = 0.f;			
		mState.fadingStart = 0.f;

		mState.dateTime = newDateTime;

		timeLogMap = new ScaleMap(0.f, 1.f, 0.f, 1.f);		/* Time fading interpolation */
		timeLogMap.setMapFunction(circularEaseOut);
		
		mState.timeZone = newTimeZone;
	}  

	abstract void loadMedia(MultimediaLocator ml);
	abstract void draw(WMV_World world);
	abstract void displayModel(WMV_World world);
	abstract void displayMetadata(WMV_World world);

	public void updateSettings( WMV_WorldSettings newWorldSettings, WMV_WorldState newWorldState, WMV_ViewerSettings newViewerSettings, 
								WMV_ViewerState newViewerState, ML_DebugSettings newDebugSettings )
	{
		worldSettings = newWorldSettings;
		worldState = newWorldState;
		viewerSettings = newViewerSettings;
		viewerState = newViewerState;
		debugSettings = newDebugSettings;
	}

	/**
	 * Set clusterDate for this media based on media times in associated cluster
	 */
	void setClusterDate(WMV_Cluster c)
	{
		if(mState.cluster != -1)
		{
			if(c.getState().lowImageDate == c.getState().highImageDate)
				mState.clusterDate = c.getState().lowImageDate;
			else
				mState.clusterDate = PApplet.map(time.getDate().getDaysSince1980(), c.getState().lowImageDate, c.getState().highImageDate, 0.f, 1.f);			// -- Use dateLength?
		}
	}

	/**
	 * Set clusterTime for this image based on media times in associated cluster
	 */
	void setClusterTime( WMV_Cluster c )
	{
		if(mState.cluster != -1)
		{
			if(c.getState().lowImageTime == c.getState().highImageTime)
				mState.clusterTime = c.getState().lowImageTime;
			else
				mState.clusterTime = PApplet.map(time.getTime(), c.getState().lowImageTime, c.getState().highImageTime, 0.f, 1.f);			// -- Use dayLength?
		}
	}

	/**
	 * Transition alpha from current to given value
	 */
	void fadeBrightness(float target)
	{
		if(target != mState.fadingBrightness)			// Check if already at target
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
		{
			mState.fading = false;
		}
	}

	/**
	 * Stop fading in or out
	 */
	public void stopFading()
	{
		mState.fadingEndFrame = worldState.frameCount;
		mState.fadingStart = mState.fadingBrightness;
		mState.fading = false;

		if(mState.isFadingOut) mState.isFadingOut = false;
		if(mState.isFadingIn) mState.isFadingIn = false;
	}

	/**
	 * @return Distance from the image capture state.location to the camera
	 */
	float getCaptureDistance()       // Find distance from camera to point in virtual space where photo appears           
	{
		PVector camLoc = viewerState.getLocation();
		float distance = PVector.dist(mState.captureLocation, camLoc);     
		return distance;
	}


	/**
	 * @return How far the image capture state.location is from a point
	 */
	float getCaptureDistanceFrom(PVector point)       // Find distance from camera to point in virtual space where photo appears           
	{
		float distance = PVector.dist(mState.captureLocation, point);     
		return distance;
	}

	public void updateTimeBrightness(WMV_Cluster c, ArrayList<WMV_TimeSegment> fieldTimeline, WMV_Utilities utilities)
	{
		int cycleLength = worldSettings.timeCycleLength;				// Length of main time loop
		float centerTime = -1;								// Midpoint of visibility for this media 		
		setTimeBrightness(0.f);

		float length = worldSettings.defaultMediaLength;				// Start with default length

		int fadeInStart = 0;								// When image starts fading in
		int fadeInEnd = 0;									// When image reaches full state.brightness
		int fadeOutStart = 0;								// When image starts fading out
		int fadeOutEnd = 0;									// When image finishes fading out

		boolean error = false;
		float lower = 0, upper = 0;
		
		int curTime = 0;
		
		switch(worldState.getTimeMode())
		{
			case 0:
				curTime = c.getState().currentTime;						// Set image time from cluster
				
				if(c.getDateline() != null)
				{
					if(c.getDateline().size() == 1)
					{
						// -- Time bug happens here -- should get all nearby clusters, not just current + check if current is closeby!
						lower = c.getTimeline().get(0).getLower().getTime();						// Get cluster timeline lower bound
						upper = c.getTimeline().get(c.getTimeline().size()-1).getUpper().getTime();	// Get cluster timeline upper bound
					}
					else
					{
						lower = c.getTimelines().get(0).get(0).getLower().getTime();							// Get cluster timeline lower bound
						int lastIdx = c.getTimelines().size()-1;
						upper = c.getTimelines().get(lastIdx).get(c.getTimelines().get(lastIdx).size()-1).getUpper().getTime();			// Get cluster timeline upper bound
					}
				}
				else setTimeBrightness(0.f);
			break;
		
			case 1:												// Time Mode: Field
				curTime = worldState.currentTime;
				lower = fieldTimeline.get(0).getLower().getTime();		// Check division					// Get cluster timeline lower bound
				upper = fieldTimeline.get(fieldTimeline.size()-1).getUpper().getTime();		// Get cluster timeline upper bound
				break;
				
			case 2:
				curTime = worldState.currentTime;
				break;
		}
		
		if(worldState.getTimeMode() == 0 || worldState.getTimeMode() == 1)
		{
			float timelineLength = upper - lower;

			if(getMediaType() == 1)
				System.out.println("--> ID:"+getID()+" time:"+time.getTime()+" ---> lower:"+lower+" upper:"+upper+" timelineLength:"+timelineLength+" curTime:"+curTime);

			if(lower == upper)						// Only one cluster segment
			{
				centerTime = cycleLength / 2.f;
				length = worldSettings.timeCycleLength;		// -- Should depend on cluster it belongs to 

				if(getMediaType() == 1)
				{
					System.out.println("Only one cluster time segment, full length:"+length);
					System.out.println("time:"+time.getTime()+" centerTime:"+centerTime+" dayLength:"+cycleLength);
				}

				fadeInStart = 0;											// Frame media starts fading in
				fadeInEnd = Math.round(centerTime - length / 4.f);		// Frame media reaches full state.brightness
				fadeOutStart = Math.round(centerTime + length / 4.f);	// Frame media starts fading out
				fadeOutEnd = cycleLength;									// Frame media finishes fading out
			}
			else
			{
				float mediaTime = utilities.round(time.getTime(), 4);		// Get time of this media file
				centerTime = Math.round(PApplet.map( mediaTime, lower, upper, length, cycleLength - length) );	// Calculate center time in cluster timeline

				fadeInStart = Math.round(centerTime - length / 2.f);		// Frame media starts fading in
				fadeInEnd = Math.round(centerTime - length / 4.f);		// Frame media reaches full state.brightness
				fadeOutStart = Math.round(centerTime + length / 4.f);	// Frame media starts fading out
				fadeOutEnd = Math.round(centerTime + length / 2.f);		// Frame media finishes fading out

				if(getMediaType() == 1)
				{
					System.out.println(" media length:"+length+" centerTime:"+centerTime+" cycleLength:"+cycleLength);
					System.out.println(" lower:"+lower+" upper:"+upper);
					System.out.println(" fadeInStart:"+fadeInStart+" fadeInEnd:"+fadeInEnd+" fadeOutStart:"+fadeOutStart+" fadeOutEnd:"+fadeOutEnd);
				}
			}	

			/* Debugging */
			if(fadeInStart < 0)
			{
				error = true;
				System.out.println(">>> Error: fadeInStart before day start!!");
				System.out.println(" media length:"+length+" centerTime:"+centerTime+" cycleLength:"+cycleLength+" getMediaType():"+getMediaType());
				System.out.println(" lower:"+lower+" upper:"+upper);
				System.out.println(" fadeInStart:"+fadeInStart+" fadeInEnd:"+fadeInEnd+" fadeOutStart:"+fadeOutStart+" fadeOutEnd:"+fadeOutEnd);
			}

			if(fadeInStart > cycleLength)
			{
				error = true;
				System.out.println("Error: fadeInStart after day end!!");
				System.out.println(" media length:"+length+" centerTime:"+centerTime+" cycleLength:"+cycleLength+" media type:"+getMediaType());
				System.out.println(" utilities.round(time.getTime(), 4):"+utilities.round(time.getTime(), 4)+" lower:"+lower+" upper:"+upper);
				System.out.println(" fadeInStart:"+fadeInStart+" fadeInEnd:"+fadeInEnd+" fadeOutStart:"+fadeOutStart+" fadeOutEnd:"+fadeOutEnd);
			}

			if(fadeInEnd > cycleLength)
			{
				error = true;
				System.out.println("------Error: fadeInEnd after day end-----time:"+time.getTime()+" centerTime:"+centerTime+" lower:"+lower+" upper:"+upper+" dayLength:"+cycleLength);
				System.out.println("-----fadeInStart:"+fadeInStart+" fadeInEnd:"+fadeInEnd+" fadeOutStart:"+fadeOutStart+" fadeOutEnd:"+fadeOutEnd);
				System.out.println("-----cluster:"+mState.cluster+" media type:"+getMediaType());
			}

			if(fadeOutStart > cycleLength)
			{
				error = true;
				System.out.println("------Error: fadeOutStart after day end-----time:"+time.getTime()+" centerTime:"+centerTime+" lower:"+lower+" upper:"+upper+" dayLength:"+cycleLength);
			}

			if(fadeOutEnd > cycleLength)
			{
				error = true;
				System.out.println("------Error: fadeOutEnd after day end-----time:"+time.getTime()+" centerTime:"+centerTime+" lower:"+lower+" upper:"+upper+" dayLength:"+cycleLength+" media type:"+getMediaType());
			}

			if(getMediaType() == 1)
			{
				System.out.println("time:"+time.getTime()+" centerTime:"+centerTime+" dayLength:"+cycleLength+"fadeInStart:"+fadeInStart+" fadeInEnd:"+fadeInEnd+" fadeOutStart:"+fadeOutStart+" fadeOutEnd:"+fadeOutEnd);
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
			{
				setTimeBrightness(0.f);
			}
		}
		
		/* Set state.timeBrightness */
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
				if(debugSettings.panorama && getMediaType() == 1)
					System.out.println(" Panorama Fading In..."+mState.id);
			}
			else if(curTime > fadeOutStart)					// During fade out
			{
				if(debugSettings.panorama && getMediaType() == 1)
					System.out.println(" Panorama Fading Out..."+mState.id);
				setTimeBrightness( PApplet.constrain(1.f - PApplet.map(curTime, fadeOutStart, fadeOutEnd, 0.f, 1.f), 0.f, 1.f) ); 
				if(worldState.getTimeMode() == 2 && mState.isCurrentMedia)
				{
					if(fadeOutEnd - curTime == 1)
						mState.isCurrentMedia = false;	// No longer the current media in Single Time Mode
				}
			}
			else													// After fade in, before fade out
			{
				if(debugSettings.panorama && getMediaType() == 1)
					System.out.println(" Panorama Full visibility...");				// Full visibility
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

		if(isSelected() && debugSettings.time)
			System.out.println("Media id:" + getID()+" state.timeBrightness"+mState.timeBrightness);

		if(error)
			setTimeBrightness( 0.f );
	}

	/**
	 * @return Time state.brightness factor between 0. and 1.
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
	 * Update state.fadingBrightness each frame
	 */
	void updateFadingBrightness()
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
			newFadeValue = mState.fadingTarget;

			if(mState.isFadingOut)
			{
				mState.isFadingOut = false;
				mState.fadedOut = true;
			}
			if(mState.isFadingIn)
			{
				mState.isFadingIn = false;
				mState.fadedIn = true;
			}
		} 
		else
		{
			newFadeValue = PApplet.map(worldState.frameCount, mState.fadingStartFrame, mState.fadingEndFrame, mState.fadingStart, mState.fadingTarget);      // Fade with distance from current time
		}

		mState.fadingBrightness = newFadeValue;
	}

	/**
	 * Calculate media capture state.location in virtual space based on GPS state.location
	 */
	void calculateCaptureLocation(WMV_Model model)                                  
	{
		float newX = 0.f, newZ = 0.f, newY = 0.f;
		
		if(model.highLongitude != -1000000 && model.lowLongitude != 1000000 && model.highLatitude != -1000000 && model.lowLatitude != 1000000 && model.highAltitude != -1000000 && model.lowAltitude != 1000000)
		{
			if(model.highLongitude != model.lowLongitude && model.highLatitude != model.lowLatitude)
			{
				newX = PApplet.map(mState.gpsLocation.x, model.lowLongitude, model.highLongitude, -0.5f * model.fieldWidth, 0.5f*model.fieldWidth); 			// GPS longitude decreases from left to right
				newY = -PApplet.map(mState.gpsLocation.y, model.lowAltitude, model.highAltitude, 0.f, model.fieldHeight); 										// Convert altitude feet to meters, negative sign to match P3D coordinate space
				newZ = PApplet.map(mState.gpsLocation.z, model.lowLatitude, model.highLatitude, 0.5f*model.fieldLength, -0.5f * model.fieldLength); 			// GPS latitude increases from bottom to top, reversed to match P3D coordinate space
				
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
	 * Move the capture state.location to the associated cluster state.location
	 */
	public void adjustCaptureLocation(WMV_Cluster mediaCluster)
	{
		if(mState.cluster != -1)
			mState.captureLocation = mediaCluster.getLocation();
		else
			mState.disabled = true;
	}

	/**
	 * @param newCluster New associated cluster
	 * Set nearest cluster to the capture state.location to be the associated cluster
	 */	
	void setAssociatedCluster(int newCluster)    				 // Associate cluster that is closest to photo
	{
		mState.cluster = newCluster;
	}

	void setID(int newID)
	{
		mState.id = newID;
	}

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

	public void setFadedIn(boolean newState)
	{
		mState.fadedIn = newState;
	}

	public void setFadedOut(boolean newState)
	{
		mState.fadedOut = newState;
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

	public void setDisabled(boolean newState)
	{
		mState.disabled = newState;
	}

	/**
	 * @return Whether the media is vState.disabled
	 */
	public boolean isDisabled()
	{
		return mState.disabled;
	}

	public void setHidden(boolean newState)
	{
		mState.hidden = newState;
	}

	/**
	 * @return Whether the media is vState.hidden
	 */
	public boolean isHidden()
	{
		return mState.hidden;
	}

	public int getID()
	{
		return mState.id;
	}

	public float getTime()
	{
		return time.getTime();
	}

	public WMV_Date getDate()
	{
		return time.getDate();
	}

	public String getName()
	{
		return mState.name;
	}

	public String getFilePath()
	{
		return mState.filePath;
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
	
	public void setClusterID(int newCluster)
	{
		mState.cluster = newCluster;
	}
	
	public int getClusterID()
	{
		return mState.cluster;
	}

	public void setTheta(float newTheta)
	{
		mState.theta = newTheta;
	}
	
	public float getTheta()
	{
		return mState.theta;
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

	public void setCameraModel(int newCameraModel)
	{
		mState.cameraModel = newCameraModel;
	}

	public int getCameraModel()
	{
		return mState.cameraModel;
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

	public float getBrightness()
	{
		return mState.brightness;
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

	public int getAssociatedCluster()
	{
		return mState.cluster;
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
					System.out.println("verts["+i+"] is null!!");
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
			System.out.println("Failed rotating vertices!");
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
	
	public void setMediaType(int newMediaType)
	{
		mState.mediaType = newMediaType;
	}
	
	public int getMediaType()
	{
		return mState.mediaType;
	}
	
	public WMV_MediaState getMediaState()
	{
		return mState;
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
}
