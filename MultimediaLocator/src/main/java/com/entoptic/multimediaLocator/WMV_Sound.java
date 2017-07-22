package main.java.com.entoptic.multimediaLocator;

import java.util.ArrayList;

import ddf.minim.AudioSample;

//import beads.*;
//import processing.sound.*;
//import ddf.minim.*;

import processing.core.PApplet;
//import processing.core.PImage;
import processing.core.PVector;

/**************************************************
 * Sound in a 3D environment
 * @author davidgordon
 */
public class WMV_Sound extends WMV_Media						 
{
	//	/* Classes */
	WMV_SoundMetadata metadata;
	WMV_SoundState state;

	/* Sound */
//	AudioContext ac;					/* Beads audio context */
//	private SamplePlayer player;		/* Beads sample player */
//	private Bead sound;				/* Beads sound object */
//	Gain g;							/* Gain object */

//	SoundFile sound;

	AudioSample sound;
	
	/**
	 * Constructor for sound in 3D space 
	 * @param newID
	 * @param newType
	 * @param newSoundMetadata
	 */
	WMV_Sound ( int newID, int newType, WMV_SoundMetadata newSoundMetadata )
	{
		super( newID, newType, newSoundMetadata.name, newSoundMetadata.filePath, newSoundMetadata.dateTime, newSoundMetadata.timeZone, 
				newSoundMetadata.gpsLocation, newSoundMetadata.longitudeRef, newSoundMetadata.latitudeRef );

		state = new WMV_SoundState();

		metadata = newSoundMetadata;
		state.initialize(metadata);	

		if(metadata != null)
			setGPSLocation( metadata.gpsLocation );
		else
			System.out.println("Sound metadata == null!");
		
		initializeTime();
	}  

	/**
	 * Calculate sound audibility
	 */
	public void calculateAudibility()
	{
		setVisible(true);     										 		

		if(getMediaState().visible)
		{
			if(!isFading() && getViewerSettings().hideSounds)
				setVisible(false);

			if(getMediaState().visible)
				setVisible(getDistanceAudibility() > 0.f);
		}
	}

	/**
	 * Calculate visiblity due to fading in and out
	 * @param ml Parent app
	 * @param wasVisible Whether sound was visible last frame
	 */
	public void calculateFadingVisibility(MultimediaLocator ml, boolean wasVisible)
	{
		boolean visibilitySetToTrue = false;
		boolean visibilitySetToFalse = false;
		if(isFading())									// Update brightness while fading
		{
			if(getFadingBrightness() == 0.f)
				setVisible(false);
		}
		else 
		{
			if(!wasVisible && getMediaState().visible)
				visibilitySetToTrue = true;

			if(getFadingBrightness() == 0.f && getMediaState().visible)
				visibilitySetToTrue = true;

			if(wasVisible && !getMediaState().visible)
				visibilitySetToFalse = true;

			if(getFadingBrightness() > 0.f && !getMediaState().visible)
				visibilitySetToFalse = true;
		}
		
		if(visibilitySetToTrue && !isFading() && !hasFadedOut() && !getViewerSettings().hideSounds)	// If should be visible and already fading, fade in 
		{
			if(!state.loaded) loadMedia(ml);
//			if(getDebugSettings().sound) System.out.println("Sound.calculateFadingVisibility()... visibility was set to true Will call fadeIn()");
			fadeIn(ml.world.getCurrentField());											// Fade in
//			if(getDebugSettings().sound) System.out.println("Sound.calculateFadingVisibility()... Will call fadeSoundIn() at same time");
			fadeSoundIn();
		}

		if(visibilitySetToFalse)
		{
//			if(getDebugSettings().sound) System.out.println("Sound.calculateFadingVisibility()...Sound #"+getID()+" visibility was set to false... Will call fadeOut()");
			fadeOut(ml.world.getCurrentField(), false);
//			if(getDebugSettings().sound) System.out.println("Sound.calculateFadingVisibility()...Sound #"+getID()+" Will call fadeSoundOut() at same time");
			fadeSoundOut(false);						// Fade sound out and clear sound after
		}

		if(isFading())									// Update brightness while fading
			updateFading(ml.world.getCurrentField());

		if(hasFadedIn()) setFadedIn(false);						
		if(hasFadedOut()) setFadedOut(false);						

		if(state.soundFadedIn) state.soundFadedIn = false;
		if(state.soundFadedOut) state.soundFadedOut = false;
	}
	
	/**
	 * Update volume fading in at beginning and out at end
	 * @param ml Parent app
	 */
	void updateVolume(MultimediaLocator ml)
	{
		int frameLength = getLengthInFrames(30);							// Get video frame length at 30 fps
		int framesBeforeEnd = getFramesBeforeEnd(ml.frameCount);		// Playback position in frames, i.e. frames from end
		
//		if(ml.debugSettings.sound)
//			System.out.println("Sound.updateVolume()... playing?"+isPlaying()+" frameLength:"+frameLength+" framesBeforeEnd:"+framesBeforeEnd);
		
		if(frameLength > 0)
		{
//			if( framesSinceStart == 0 && !isFadingVolume())	// Fade in at first frame
			if( framesBeforeEnd == 0 )	// Fade in at first frame
			{
				if(ml.debug.sound)
					System.out.println("  Sound.updateVolume()... First frame, will fade sound in...");
				fadeSoundIn();
				state.playbackStartFrame = ml.frameCount;
			}
			else if( framesBeforeEnd == ml.world.viewer.getSettings().soundFadingLength && !isFadingVolume())	
			{
				if(ml.debug.sound) System.out.println("  Sound.updateVolume()... Near end, will fade sound out...");
				fadeSoundOut(true);			// Fade out at <soundFadingLength> before end and pause video once finished
			}
		}
		else
		{
			if(ml.debug.sound) System.out.println("Sound.updateVolume()... ERROR... video #"+getID()+" has no length!");
		}
	}
	
	/**
	 * Update volume fading 
	 */
	public void updateFadingVolume()
	{
		if(state.fadingVolume && getWorldState().frameCount < state.volumeFadingEndFrame)	// Still fading
		{
			state.volume = PApplet.map(getWorldState().frameCount, state.volumeFadingStartFrame, state.volumeFadingEndFrame, state.volumeFadingStartVal, state.volumeFadingTarget);

			sound.setGain(state.volume);
//			g.setGain(state.volume);
		}
		else								// Reached target
		{
			state.volume = state.volumeFadingTarget;
			state.fadingVolume = false;
			
			if(state.volume == 1.f)
			{
				state.soundFadedIn = true;
			}
			else if(state.volume == 0.f)
			{
				
				if(state.pauseAfterSoundFades)
				{
					if(getDebugSettings().sound) System.out.println("Sound.updateFadingVolume() id #"+getID()+" pausing sound...");
					pauseSound();
					state.pauseAfterSoundFades = false;			// Reset pauseAfterSoundFades 
				}
				else
				{
					if(getDebugSettings().sound) System.out.println("Sound.updateFadingVolume() id #"+getID()+" clearing sound...");
					state.soundFadedOut = true;
					clearSound();
				}
			}
		}
	}

	/**
	 * Display the sound and/or metadata
	 */
	public void display(MultimediaLocator ml)
	{
		displayModel(ml);
	}

	/**
	 * Display sound capture location as a sphere in virtual space
	 * @param ml Parent app
	 */
	public void displayModel(MultimediaLocator ml)
	{
		ml.noStroke(); 

		ml.stroke(70, 220, 150);
		ml.fill(70, 220, 150);
		ml.strokeWeight(1.f);

		ml.pushMatrix();

		if(getViewerSettings().selection)
		{
			if(isSelected())
			{
				ml.tint(255, 255);          				
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
			if(ml.world.getState().showModel)
				ml.tint(255, 255);          				
			else
			{
				if(!getWorldState().alphaMode)
					ml.tint(getViewingBrightness(), 255);          				
				else
					ml.tint(255, PApplet.map(getViewingBrightness(), 0.f, 255.f, 0.f, getWorldState().alpha));    
			}
		}

		PVector loc = getLocation();
		ml.translate(loc.x, loc.y, loc.z);
		ml.sphere(getMediaState().centerSize * 2.f);
		ml.popMatrix();
	}

	/**
	 * Load sound file from disk
	 */
	public void loadMedia(MultimediaLocator ml)
	{
		if(ml.debug.sound) System.out.println("Sound loadMedia()... id #"+getID());
		
		if( !getMediaState().hidden && !getMediaState().disabled )
		{
//			ac = new AudioContext();
//			player = new SamplePlayer(ac, SampleManager.sample(getMetadata().filePath));
//			g = new Gain(ac, 2, 0.2f);
//			g.addInput(player);
//			ac.out.addInput(g);

			sound = ml.minim.loadSample(getMetadata().filePath);		// Load sound as a sample

			setLength( (float)sound.getMetaData().length() * 0.001f);	// Set sound length

//			setLength( (float)player.getSample().getLength() * 0.001f );	// Set sound length

			/* WORKED WITH Beads */
//			ac = new AudioContext();
//			player = new SamplePlayer(ac, SampleManager.sample(getMetadata().filePath));
//			g = new Gain(ac, 2, 0.2f);
//			g.addInput(player);
//			ac.out.addInput(g);
//			
//			setLength( (float)player.getSample().getLength() * 0.001f );	// Set sound length
			
			if(getViewerSettings().autoPlaySounds)
			{
				if(ml.world.getCurrentField().getVideosPlaying() < getViewerSettings().autoPlayMaxSoundCount)
					play(ml);
			}
			else
			{
				if(getDebugSettings().sound) System.out.println("Sound.loadMedia() id #"+getID()+" pausing sound...");
				pauseSound();
			}
			
			state.loaded = true;
		}
	}

	/**
	 * Start playing the sound
	 * @param pause 
	 */
	public void play(MultimediaLocator ml)
	{
		if(getDebugSettings().sound) System.out.println("Sound.play()...");
		
		sound.trigger();				// Trigger sound to play
		
//		ac.start();					// Start audio context 
		state.playing = true;
		state.volume = 0.f;
		fadeSoundIn();
		
		ml.world.getCurrentField().setSoundsPlaying(ml.world.getCurrentField().getSoundsPlaying()+1);
		ml.world.getCurrentField().setSoundsHeard(ml.world.getCurrentField().getSoundsHeard()+1);
	}

	/**
	 * Fade sound out and clear sound once finished
	 */
	public void stopSound()
	{
		if(getDebugSettings().sound) System.out.println("stopSound()...");
		fadeSoundOut(false);														
		state.playing = false;
	}

	/**
	 * Pause sound
	 */
	private void pauseSound()
	{
		if(getDebugSettings().sound) System.out.println("pauseSound()...");
		sound.stop();
//		player.pause(true);
		state.playing = false;
	}

	/**
	 * Stop playing and clear sound
	 */
	public void clearSound()
	{
		if(getDebugSettings().sound) System.out.println("clearSound()...");
		try{
			if(sound != null)
			{
				sound.stop();
				sound.close();
//				player.clearInputConnections();
			}
		}
		catch(Throwable t)
		{
			System.out.println("Throwable in clearVideo():"+t);
		}

		state.loaded = false;
	}

	/**
	 * Fade in sound
	 */
	void fadeSoundIn()
	{
		if(getDebugSettings().sound) System.out.println("Sound.fadeSoundIn()...");
		if(state.volume < getWorldSettings().soundMaxVolume)
		{
			state.fadingVolume = true;
			state.volumeFadingStartFrame = getWorldState().frameCount; 
			state.volumeFadingStartVal = state.volume; 
			state.volumeFadingEndFrame = getWorldState().frameCount + state.volumeFadingLength;		// Fade volume in over n frames
			state.volumeFadingTarget = getWorldSettings().soundMaxVolume;
		}
	}

	/**
	 * Fade out sound
	 * @param pause Whether to pause sound or dispose of samples
	 */
	void fadeSoundOut(boolean pause)
	{
		if(getDebugSettings().sound) System.out.println("Sound.fadeSoundOut()...");
		if(state.volume > 0.f)
		{
			state.fadingVolume = true;
			state.volumeFadingStartFrame = getWorldState().frameCount; 
			state.volumeFadingStartVal = state.volume; 
			state.volumeFadingEndFrame = getWorldState().frameCount + state.volumeFadingLength;		// Fade volume out over n frames
			state.volumeFadingTarget = 0.f;
			state.pauseAfterSoundFades = pause;
		}
	}

	/**
	 * Calculate sound location from GPS track waypoint closest to capture time
	 * @param gpsTrack GPS track as waypoint list
	 */
	void calculateLocationFromGPSTrack(ArrayList<WMV_Waypoint> gpsTrack, float soundGPSTimeThreshold)
	{
		if(getDebugSettings().sound) System.out.println("Sound.calculateLocationFromGPSTrack()... id#"+getID()+"...");

		float closestDist = 1000000.f;
		int closestIdx = -1;

		int sYear = time.getYear();
		int sMonth = time.getMonth();
		int sDay = time.getDay();
		int sHour = time.getHour();
		int sMinute = time.getMinute();
		int sSecond = time.getSecond();

		for(WMV_Waypoint w : gpsTrack)										/* GPS track initialized for field time zone */
		{
			int wYear = w.getTime().getYear();
			int wMonth = w.getTime().getMonth();
			int wDay = w.getTime().getDay();
			int wHour = w.getTime().getHour();
			int wMinute = w.getTime().getMinute();
			int wSecond = w.getTime().getSecond();
			
			if(wYear == sYear && wMonth == sMonth && wDay == sDay)			/* Find waypoints on same day as sound */
			{
				int sTime = sHour * 60 + sMinute * 60 + sSecond;
				int wTime = wHour * 60 + wMinute * 60 + wSecond;

				float timeDist = Math.abs(wTime - sTime);
				if(timeDist <= closestDist && timeDist < soundGPSTimeThreshold)
				{
					closestDist = timeDist;
					closestIdx = w.getID();
				}
			}
		}

		if(closestIdx >= 0)
		{
			WMV_Waypoint wp = gpsTrack.get(closestIdx);
			setAssociatedGPSTrackWaypoint( wp );
			
			setGPSLocation( wp.getGPSLocationWithAltitude() );			// Format: {longitude, altitude, latitude}
			setGPSLocationInMetadata( wp.getGPSLocationWithAltitude() );	// Format: {longitude, altitude, latitude}
			setGPSLongitudeRef( wp.longitudeRef );
			setGPSLongitudeRefInMetadata( wp.longitudeRef );
			setGPSLatitudeRef( wp.latitudeRef );
			setGPSLatitudeRefInMetadata( wp.latitudeRef );

			if(getDebugSettings().sound)
			{
				WMV_Time wpt = gpsTrack.get(closestIdx).getTime();
				System.out.println( "Sound.calculateLocationFromGPSTrack()... Set sound " + "#"+getID()+" GPS location to waypoint "+closestIdx);
				System.out.println( "  Waypoint Day:"+wpt.getDay()+" Hour:"+wpt.getHour()+"   Min:"+wpt.getMinute());
				System.out.println( "  Sound Day:"+sDay+" Hour:"+sHour+" Min:"+sMinute+" Sound GPS X:"+getGPSLocation().x+" GPS Y:"+getGPSLocation().y+" GPS Z:"+getGPSLocation().z);
			}
			
		}
		else 
			if(getDebugSettings().sound)
				System.out.println("Sound.calculateLocationFromGPSTrack()... No gps nodes on Day:"+sDay+" Month:"+sMonth+" Year:"+sYear+"!");
	}
	
	/** 
	 * @return Distance audibility multiplier between 0. and 1.
	 * Find sound audibility due to distance (fades away in distance and as camera gets close)
	 */
	public float getDistanceAudibility()								
	{
		float hearingDist = getHearingDistance();
		float audibility = 1.f;

		float inaudiblePoint = getViewerSettings().farHearingDistance;	// Distance where transparency reaches zero
		float maxVolume = getWorldSettings().soundMaxVolume;
		if(hearingDist < inaudiblePoint)
			audibility = PApplet.constrain( maxVolume - PApplet.map(hearingDist, 0.f, inaudiblePoint, 0.f, maxVolume), 0.f, maxVolume );    // Fade out until inaudible point
		else
			audibility = 0.f;

		return audibility;
	}

	/** 
	 * @return Distance visibility multiplier between 0. and 1.
	 * Find video visibility due to distance (fades away in distance and as camera gets close)
	 */
	public float getDistanceBrightness(WMV_Viewer viewer)
	{
		float viewDist = getViewingDistance(viewer);
		float distVisibility = 1.f;

		float farViewingDistance = getViewerSettings().getFarViewingDistance();
		float nearViewingDistance = getViewerSettings().getNearViewingDistance();
		
		if(viewDist > farViewingDistance)
		{
			float vanishingPoint = farViewingDistance + viewer.p.getSettings().defaultFocusDistance;	// Distance where transparency reaches zero
			if(viewDist < vanishingPoint)
				distVisibility = PApplet.constrain(1.f - PApplet.map(viewDist, farViewingDistance, vanishingPoint, 0.f, 1.f), 0.f, 1.f);    // Fade out until cam.visibleFarDistance
			else
				distVisibility = 0.f;
		}
		else if(viewDist < nearViewingDistance) 													// Near distance at which transparency reaches zero
			distVisibility = PApplet.constrain(PApplet.map(viewDist, getViewerSettings().getNearClippingDistance(), nearViewingDistance, 0.f, 1.f), 0.f, 1.f);   					  // Fade out until visibleNearDistance

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
	 * @return Distance between video and the viewer
	 */
	public float getHearingDistance()       // Find distance from camera to point in virtual space where photo appears           
	{
		PVector camLoc = getViewerState().getLocation();
		PVector loc = new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z);

		return PVector.dist(loc, camLoc);     
	}

	/**
	 * Get playback position in frames, i.e. frames from end
	 * @param curFrameCount Current frame count
	 * @return Frames until last frame of video
	 */
	public int getFramesBeforeEnd(int curFrameCount)
	{
		int frameLength = getLengthInFrames( 30 );			// -- Use actual frame rate?
		int endFrame = state.playbackStartFrame + frameLength;
//		System.out.println("Sound.getFramesBeforeEnd()... frameLength: "+frameLength+" state.playbackStartFrame:"+state.playbackStartFrame);
		
		return endFrame - curFrameCount;
	}

	public int getLengthInFrames(float frameRate)
	{
//		System.out.println("Sound.getLengthInFrames()... state.length: "+state.length+" player.sample.length (ms.): "+player.getSample().getLength());
		if(state.length != 0)
			return Math.round( state.length * frameRate );			// -- Use actual frame rate?
		else if(sound != null)
		{
//			setLengthFromSound(player.getSample());
			state.length = (float) sound.getMetaData().length() * 0.001f;
			return Math.round( state.length * frameRate );			// -- Use actual frame rate?
		}		
		else 
			return 0;
	}

	/**
	 * Initialize sound time from metadata date/time string
	 */
	public void initializeTime()
	{
		if(metadata.dateTime == null)
		{
			try {
				metadata.dateTime = parseDateTime(metadata.dateTimeString);
				time = new WMV_Time();
				time.initialize( metadata.dateTime, metadata.dateTimeString, getID(), 3, getAssociatedClusterID(), metadata.timeZone );
//				System.out.println("Sound.initializeTime()... Initialized #"+getID()+" date / time from metadata string... Hour:" + time.getHour()+" Min.:" + time.getMinute()+" Norm: "+time.getTime());
			} 
			catch (Throwable t) 
			{
				System.out.println("Error in sound date / time... " + t);
			}
		}
		else
		{
			time = new WMV_Time();
			time.initialize( metadata.dateTime, metadata.dateTimeString, getID(), 3, getAssociatedClusterID(), metadata.timeZone );
			System.out.println("Sound.initializeTime()... Initialized #"+getID()+" date / time from metadata dateTime... Hour:" + time.getHour()+" Min.:" + time.getMinute()+" Norm: "+time.getAbsoluteTime());
		}
	}

	/**
	 * Set sound state
	 * @param newState New sound state
	 */
	public void setState(WMV_SoundState newState)
	{
		state = newState;
	}

	/**
	 * @return Current sound state
	 */
	public WMV_SoundState getState()
	{
		return state;
	}

	/**
	 * @return Save sound state for exporting
	 */
	public void captureState()
	{
		state.setMediaState(getMediaState(), metadata);
	}

	/**
	 * Set GPS location in metadata from media state
	 */
	public void setGPSLocationInMetadataFromState()
	{
		metadata.gpsLocation = getMediaState().gpsLocation;
	}
	
	/**
	 * Set GPS location in metadata 
	 * @param newGPSLocation New GPS location
	 */
	public void setGPSLocationInMetadata(PVector newGPSLocation)
	{
		metadata.gpsLocation = newGPSLocation;
	}
	
	/**
	 * Set GPS longitude reference
	 * @param newLongitudeRef New longitude reference
	 */
	public void setGPSLongitudeRefInMetadata(String newLongitudeRef)
	{
		metadata.longitudeRef = newLongitudeRef;
	}
	
	/**
	 * Set GPS latitude reference
	 * @param newLatitudeRef New latitude reference
	 */
	public void setGPSLatitudeRefInMetadata(String newLatitudeRef)
	{
		metadata.latitudeRef = newLatitudeRef;
	}
	
	/**
	 * @return Whether volume is fading
	 */
	public boolean isFadingVolume()
	{
		return state.fadingVolume;
	}

	/**
	 * @return Whether sound sample is loaded from disk
	 */
	public boolean isLoaded()
	{
		return state.loaded;
	}

	/**
	 * @return Whether sound is playing
	 */
	public boolean isPlaying()
	{
		return state.playing;
	}

	/**
	 * @return Sound length
	 */
	public float getLength()
	{
		return state.length;
	}
	
	 /**
	  * @param newLength New sound length
	  */
	 void setLength(float newLength)
	 {
		 state.length = newLength;
		 System.out.println("Sound.setLength()... newLength:"+newLength+" state.length:"+state.length);
	 }
	 
//	 void setLengthFromSound( Sample sample )
//	 {
//		state.length = (float) sample.getLength() * 0.001f;
//	 }
	 
	 public WMV_SoundMetadata getMetadata()
	 {
		 return metadata;
	 }

		/**
		 * Set GPS Track waypoint associated with this sound
		 * @param newWaypoint New waypoint
		 */
		public void setAssociatedGPSTrackWaypoint( WMV_Waypoint newWaypoint )
		{
			state.associatedWaypoint = newWaypoint;
		}

		/**
		 * Set GPS Track waypoint associated with this sound
		 * @param newWaypoint New waypoint
		 */
		public WMV_Waypoint getAssociatedGPSTrackWaypoint()
		{
			return state.associatedWaypoint;
		}

	/**
	 * Draw the image metadata in Heads-Up Display
	 */
	public void displayMetadata(MultimediaLocator ml)
	{
		String strTitleImage = "Sound";
		String strTitleImage2 = "";
		String strName = "Name: "+getName();
		String strID = "ID: "+String.valueOf(getID());
		String strCluster = "Cluster: "+String.valueOf(getMediaState().getClusterID());
		String strX = "Location X: "+String.valueOf(getCaptureLocation().z);
		String strY = " Y: "+String.valueOf(getCaptureLocation().x);
		String strZ = " Z: "+String.valueOf(getCaptureLocation().y);

		String strDate = "Date: "+String.valueOf(time.getMonth()) + String.valueOf(time.getDay()) + String.valueOf(time.getYear());
		String strTime = "Time: "+String.valueOf(time.getHour()) + ":" + (time.getMinute() >= 10 ? String.valueOf(time.getMinute()) : "0"+String.valueOf(time.getMinute())) + ":" + 
				(time.getSecond() >= 10 ? String.valueOf(time.getSecond()) : "0"+String.valueOf(time.getSecond()));

		String strLatitude = "GPS Latitude: "+String.valueOf(getMediaState().gpsLocation.z);
		String strLongitude = " Longitude: "+String.valueOf(getMediaState().gpsLocation.x);
		String strAltitude = "Altitude: "+String.valueOf(getMediaState().gpsLocation.y);
		//		String strTheta = "Direction: "+String.valueOf(theta);

		String strTitleDebug = "--- Debugging ---";
		String strBrightness = "brightness: "+String.valueOf(getMediaState().viewingBrightness);
		String strBrightnessFading = "brightnessFadingValue: "+String.valueOf(getMediaState().fadingBrightness);

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

		if(ml.debug.sound)
		{
			ml.display.metadata(frameCount, strTitleDebug);
			ml.display.metadata(frameCount, strBrightness);
			ml.display.metadata(frameCount, strBrightnessFading);
		}
	}
	
	public String getFilePath()
	{
		return getMetadata().filePath;
	}

	public void setFilePath(String newFilePath)
	{
		metadata.filePath = newFilePath;
	}

	public void updateFilePath(MultimediaLocator ml, WMV_Field parentField)
	{
		String oldFilePath = getFilePath();
		String[] parts = oldFilePath.split("/");

		parts[parts.length-4] = ml.library.getName(true);			// Library name
		parts[parts.length-3] = parentField.getName();					// Field name
		
		String newFilePath = parts[0];
		for(int i=1; i<parts.length; i++)
			newFilePath = newFilePath + "/" + parts[i];
		System.out.println("Sound.updateFilePath()... Will set sound path to:"+newFilePath);
		setFilePath(newFilePath);
	}
}
