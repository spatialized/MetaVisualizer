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
	public WMV_ViewableState vState;
	
	/* Time */
	private ScaleMap timeLogMap;
	private InterpolateStrategy circularEaseOut = new CircularInterpolation(false);		// Steepest ascent at beginning
	public WMV_Time time;

	WMV_Viewable ( int newID, int newMediaType, String newName, String newFilePath, PVector newGPSLocation, float newTheta, 
			int newCameraModel, float newBrightness, ZonedDateTime newDateTime, String newTimeZone )
	{
		vState = new WMV_ViewableState();
		vState.name = newName;
		vState.id = newID; 
		vState.mediaType = newMediaType;
		vState.filePath = newFilePath;

		vState.gpsLocation = newGPSLocation;
		vState.captureLocation = new PVector(0, 0, 0);

		vState.cameraModel = newCameraModel;
		vState.theta = newTheta;              
		vState.brightness = newBrightness;

		vState.fadingBrightness = 0.f;			
		vState.fadingStart = 0.f;

		vState.dateTime = newDateTime;

		timeLogMap = new ScaleMap(0.f, 1.f, 0.f, 1.f);		/* Time fading interpolation */
		timeLogMap.setMapFunction(circularEaseOut);
		
		vState.timeZone = newTimeZone;
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
		if(vState.cluster != -1)
		{
			if(c.getState().lowImageDate == c.getState().highImageDate)
				vState.clusterDate = c.getState().lowImageDate;
			else
				vState.clusterDate = PApplet.map(time.getDate().getDaysSince1980(), c.getState().lowImageDate, c.getState().highImageDate, 0.f, 1.f);			// -- Use dateLength?
		}
	}

	/**
	 * Set clusterTime for this image based on media times in associated cluster
	 */
	void setClusterTime( WMV_Cluster c )
	{
		if(vState.cluster != -1)
		{
			if(c.getState().lowImageTime == c.getState().highImageTime)
				vState.clusterTime = c.getState().lowImageTime;
			else
				vState.clusterTime = PApplet.map(time.getTime(), c.getState().lowImageTime, c.getState().highImageTime, 0.f, 1.f);			// -- Use dayLength?
		}
	}

	/**
	 * Transition alpha from current to given value
	 */
	void fadeBrightness(float target)
	{
		if(target != vState.fadingBrightness)			// Check if already at target
		{
			vState.beginFading = true;
			vState.fading = true;   
			vState.fadingStart = vState.fadingBrightness;
			vState.fadingTarget = target;
			vState.fadingStartFrame = worldState.frameCount;
			vState.fadingEndFrame = vState.fadingStartFrame + viewerSettings.teleportLength;

			if(target > vState.fadingBrightness)
				vState.isFadingIn = true;
			else
				vState.isFadingOut = true;
		}
		else
		{
			vState.fading = false;
		}
	}

	/**
	 * Stop fading in or out
	 */
	public void stopFading()
	{
		vState.fadingEndFrame = worldState.frameCount;
		vState.fadingStart = vState.fadingBrightness;
		vState.fading = false;

		if(vState.isFadingOut) vState.isFadingOut = false;
		if(vState.isFadingIn) vState.isFadingIn = false;
	}

	/**
	 * @return Distance from the image capture state.location to the camera
	 */
	float getCaptureDistance()       // Find distance from camera to point in virtual space where photo appears           
	{
		PVector camLoc = viewerState.getLocation();
		float distance = PVector.dist(vState.captureLocation, camLoc);     
		return distance;
	}


	/**
	 * @return How far the image capture state.location is from a point
	 */
	float getCaptureDistanceFrom(PVector point)       // Find distance from camera to point in virtual space where photo appears           
	{
		float distance = PVector.dist(vState.captureLocation, point);     
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
				System.out.println("-----cluster:"+vState.cluster+" media type:"+getMediaType());
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

//			if(selected && debugSettings.time)
			if(getMediaType() == 1)
			{
				System.out.println("time:"+time.getTime()+" centerTime:"+centerTime+" dayLength:"+cycleLength+"fadeInStart:"+fadeInStart+" fadeInEnd:"+fadeInEnd+" fadeOutStart:"+fadeOutStart+" fadeOutEnd:"+fadeOutEnd);
			}
		}
		else if(worldState.getTimeMode() == 2)
		{
			if(vState.isCurrentMedia)
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
				if(vState.active)							// If image was vState.active
					vState.active = false;					// Set to invState.active

				setTimeBrightness(0.f);			   					// Zero visibility
				
				if(worldState.getTimeMode() == 2) 
					if(vState.isCurrentMedia)
						vState.isCurrentMedia = false;	// No longer the current media in Single Time Mode
			}
			else if(curTime < fadeInEnd)						// During fade in
			{
				if(!vState.active)							// If image was not vState.active
					vState.active = true;

				setTimeBrightness( PApplet.constrain(PApplet.map(curTime, fadeInStart, fadeInEnd, 0.f, 1.f), 0.f, 1.f) );   
				if(debugSettings.panorama && getMediaType() == 1)
					System.out.println(" Panorama Fading In..."+vState.id);
			}
			else if(curTime > fadeOutStart)					// During fade out
			{
				if(debugSettings.panorama && getMediaType() == 1)
					System.out.println(" Panorama Fading Out..."+vState.id);
				setTimeBrightness( PApplet.constrain(1.f - PApplet.map(curTime, fadeOutStart, fadeOutEnd, 0.f, 1.f), 0.f, 1.f) ); 
				if(worldState.getTimeMode() == 2 && vState.isCurrentMedia)
				{
					if(fadeOutEnd - curTime == 1)
						vState.isCurrentMedia = false;	// No longer the current media in Single Time Mode
				}
			}
			else													// After fade in, before fade out
			{
				if(debugSettings.panorama && getMediaType() == 1)
					System.out.println(" Panorama Full visibility...");				// Full visibility
				vState.timeBrightness = 1.f;								
			}
		}
		else
		{
			if(curTime > fadeOutEnd)					// If image was vState.active and has faded out
			{
				if(vState.active) vState.active = false;									// Set to invState.active
				if(worldState.getTimeMode() == 2) 
				{
					vState.isCurrentMedia = false;	// No longer the current media in Single Time Mode
				}			
			}
		}

		setTimeBrightness( (float)timeLogMap.getMappedValueFor(vState.timeBrightness) );   		// Logarithmic scaling

		if(isSelected() && debugSettings.time)
			System.out.println("Media id:" + getID()+" state.timeBrightness"+vState.timeBrightness);

		if(error)
			setTimeBrightness( 0.f );
	}

	/**
	 * @return Time state.brightness factor between 0. and 1.
	 */
	public float getTimeBrightness()												
	{
		return vState.timeBrightness;
	}

	/**
	 * Set time brightness
	 */
	public void setTimeBrightness(float newTimeBrightness)												
	{
		vState.timeBrightness = newTimeBrightness;
	}
	
	/**
	 * Update state.fadingBrightness each frame
	 */
	void updateFadingBrightness()
	{
		float newFadeValue = 0.f;

		if(vState.beginFading)
		{
			vState.fadingStartFrame = worldState.frameCount;					
			vState.fadingEndFrame = worldState.frameCount + viewerSettings.teleportLength;	
			vState.beginFading = false;
		}

		if (worldState.frameCount >= vState.fadingEndFrame)
		{
			vState.fading = false;
			newFadeValue = vState.fadingTarget;

			if(vState.isFadingOut)
			{
				vState.isFadingOut = false;
				vState.fadedOut = true;
			}
			if(vState.isFadingIn)
			{
				vState.isFadingIn = false;
				vState.fadedIn = true;
			}
		} 
		else
		{
			newFadeValue = PApplet.map(worldState.frameCount, vState.fadingStartFrame, vState.fadingEndFrame, vState.fadingStart, vState.fadingTarget);      // Fade with distance from current time
		}

		vState.fadingBrightness = newFadeValue;
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
				newX = PApplet.map(vState.gpsLocation.x, model.lowLongitude, model.highLongitude, -0.5f * model.fieldWidth, 0.5f*model.fieldWidth); 			// GPS longitude decreases from left to right
				newY = -PApplet.map(vState.gpsLocation.y, model.lowAltitude, model.highAltitude, 0.f, model.fieldHeight); 										// Convert altitude feet to meters, negative sign to match P3D coordinate space
				newZ = PApplet.map(vState.gpsLocation.z, model.lowLatitude, model.highLatitude, 0.5f*model.fieldLength, -0.5f * model.fieldLength); 			// GPS latitude increases from bottom to top, reversed to match P3D coordinate space
				
				if(worldSettings != null)
				{
					if(worldSettings.altitudeScaling)	
						newY *= worldSettings.altitudeScalingFactor;
				}
				else
					newY *= vState.defaultAltitudeScalingFactor;
			}
			else
			{
				newX = newY = newZ = 0.f;
			}
		}

		vState.captureLocation = new PVector(newX, newY, newZ);
	}

	/**
	 * Move the capture state.location to the associated cluster state.location
	 */
	public void adjustCaptureLocation(WMV_Cluster mediaCluster)
	{
		if(vState.cluster != -1)
			vState.captureLocation = mediaCluster.getLocation();
		else
			vState.disabled = true;
	}

	/**
	 * @param newCluster New associated cluster
	 * Set nearest cluster to the capture state.location to be the associated cluster
	 */	
	void setAssociatedCluster(int newCluster)    				 // Associate cluster that is closest to photo
	{
		vState.cluster = newCluster;
	}

	void setID(int newID)
	{
		vState.id = newID;
	}

	void setVisible(boolean newState)
	{
		vState.visible = newState;
	}
	/**
	 * @return Whether the media is visible
	 */
	public boolean isVisible()
	{
		return vState.visible;
	}
	
	/**
	 * @return Whether the media is vState.active at the current time
	 */
	public boolean isActive()
	{
		return vState.active;
	}

	/**
	 * @return Whether the image is currently fading in or out
	 */
	public boolean isFading()
	{
		if(vState.disabled || vState.hidden)
			return false;

		if(vState.fading)
			return true;
		else if(vState.isFadingIn)
			return true;
		else if(vState.isFadingOut)
			return true;
		else
			return false;
	}

	public void setFadedIn(boolean newState)
	{
		vState.fadedIn = newState;
	}

	public void setFadedOut(boolean newState)
	{
		vState.fadedOut = newState;
	}
	
	public boolean isFadingIn()
	{
		return vState.isFadingIn;
	}

	public boolean hasFadedIn()
	{
		return vState.fadedIn;
	}

	public boolean isFadingOut()
	{
		return vState.isFadingOut;
	}

	public boolean hasFadedOut()
	{
		return vState.fadedOut;
	}

	public void setDisabled(boolean newState)
	{
		vState.disabled = newState;
	}

	/**
	 * @return Whether the media is vState.disabled
	 */
	public boolean isDisabled()
	{
		return vState.disabled;
	}

	public void setHidden(boolean newState)
	{
		vState.hidden = newState;
	}

	/**
	 * @return Whether the media is vState.hidden
	 */
	public boolean isHidden()
	{
		return vState.hidden;
	}

	public int getID()
	{
		return vState.id;
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
		return vState.name;
	}

	public String getFilePath()
	{
		return vState.filePath;
	}
	
	public void setAspectRatio(float newAspectRatio)
	{
		vState.aspectRatio = newAspectRatio;
	}
	
	public float getAspectRatio()
	{
		return vState.aspectRatio;
	}
	
	public void setRequested(boolean newState)
	{
		vState.requested = newState;
	}
	
	public boolean isRequested()
	{
		return vState.requested;
	}
	
	public void setClusterID(int newCluster)
	{
		vState.cluster = newCluster;
	}
	
	public int getClusterID()
	{
		return vState.cluster;
	}

	public void setTheta(float newTheta)
	{
		vState.theta = newTheta;
	}
	
	public float getTheta()
	{
		return vState.theta;
	}

	public void setLocation(PVector newLocation)
	{
		vState.location = newLocation;
	}
	
	public void moveLocation(PVector disp)
	{
		vState.location.add(disp);     													 
	}
	
	public PVector getLocation()
	{
		return vState.location;
	}
	
	public void setCaptureLocation(PVector newCaptureLocation)
	{
		vState.captureLocation = newCaptureLocation;
	}

	public PVector getCaptureLocation()
	{
		return vState.captureLocation;
	}

	public PVector getGPSLocation()
	{
		return vState.gpsLocation;
	}

	public void setCameraModel(int newCameraModel)
	{
		vState.cameraModel = newCameraModel;
	}

	public int getCameraModel()
	{
		return vState.cameraModel;
	}

	public void setFadingFocusDistance(boolean newState)
	{
		vState.fadingFocusDistance = newState;
	}
	
	public boolean isFadingFocusDistance()
	{
		return vState.fadingFocusDistance;
	}

	public void setViewingBrightness(float newViewingBrightness)
	{
		vState.viewingBrightness = newViewingBrightness;
	}

	public float getViewingBrightness()
	{
		return vState.viewingBrightness;
	}

	public float getFadingBrightness()
	{
		return vState.fadingBrightness;
	}

	public float getBrightness()
	{
		return vState.brightness;
	}

	/**
	 * Select or deselect this image
	 * @param selection Whether to select or deselect
	 */
	public void setSelected(boolean selection)
	{
		vState.selected = selection;
		if(selection) vState.showMetadata = selection;
	}

	public boolean isSelected()
	{
		return vState.selected;
	}

	public int getAssociatedCluster()
	{
		return vState.cluster;
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
			rMat.rotate((float)Math.toRadians(angle), axis.x, axis.y, axis.z);

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
		vState.mediaType = newMediaType;
	}
	
	public int getMediaType()
	{
		return vState.mediaType;
	}
	
	public WMV_ViewableState getViewableState()
	{
		return vState;
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
	
//	public WMV_WorldSettings worldSettings;
//	public WMV_WorldState worldState;
//	public WMV_ViewerSettings viewerSettings;	// Update world settings
//	public WMV_ViewerState viewerState;	// Update world settings
//	public ML_DebugSettings debugSettings;	// Update world settings

}
