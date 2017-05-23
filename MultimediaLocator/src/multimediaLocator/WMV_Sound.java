package multimediaLocator;

import java.util.ArrayList;

import beads.*;
import processing.core.PApplet;
import processing.core.PImage;
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
	AudioContext ac;				/* Beads audio context */
	private SamplePlayer player;	/* Beads sample player */
	private Bead sound;				/* Beads sound object */
	Gain g;							/* Gain object */

	/**
	 * Constructor for sound in 3D space 
	 * @param newID
	 * @param newType
	 * @param newSoundMetadata
	 */
	WMV_Sound ( int newID, int newType, WMV_SoundMetadata newSoundMetadata )
	{
		super( newID, newType, newSoundMetadata.name, newSoundMetadata.filePath, newSoundMetadata.dateTime, newSoundMetadata.timeZone, 
				newSoundMetadata.gpsLocation );

		state = new WMV_SoundState();

		if(newSoundMetadata != null)
		{
			metadata = newSoundMetadata;
			state.initialize(metadata);	
		}
		else
			System.out.println("newSoundMetadata == null!");

		if(metadata != null)
			setGPSLocation( metadata.gpsLocation );
		else
			System.out.println("Sound metadata == null!");
		initializeTime();
	}  

	/**
=	 * Update sound geometry and audibility
	 */
//	void update(MultimediaLocator ml, WMV_Utilities utilities)
//	{
//		if(!isDisabled())			
//		{
//			boolean wasVisible = isVisible();
//			calculateAudibility();
//			updateFading(ml, wasVisible);
//			
//			if(state.loaded)
//			{
//				if(state.fadingVolume)
//					updateFadingVolume();
//				else
//					updateVolume(); 								// Tie volume to fading brightness
//			}
//		}
//	}
	
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

	public void updateFading(MultimediaLocator ml, boolean wasVisible)
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
			fadeIn(ml.world.getCurrentField());											// Fade in
			fadeSoundIn();
		}

		if(visibilitySetToFalse)
		{
			System.out.println("Sound #"+getID()+" visibility was set to false...");
			fadeOut(ml.world.getCurrentField());
			fadeSoundOut();
		}

		if(isFading())									// Update brightness while fading
			updateFadingBehavior(ml.world.getCurrentField());

		if(hasFadedIn()) setFadedIn(false);						
		if(hasFadedOut()) setFadedOut(false);						

		if(state.soundFadedIn) state.soundFadedIn = false;
		if(state.soundFadedOut) state.soundFadedOut = false;
	}
	
	/**
	 * Update volume based on viewer distance from sound
	 */
	public void updateVolume()
	{
		state.volume = getDistanceAudibility();
		g.setGain(state.volume);
	}
	
	/**
	 * Update volume fading 
	 */
	public void updateFadingVolume()
	{
		if(state.fadingVolume && getWorldState().frameCount < state.volumeFadingEndFrame)	// Still fading
		{
			state.volume = PApplet.map(getWorldState().frameCount, state.volumeFadingStartFrame, state.volumeFadingEndFrame, state.volumeFadingStartVal, state.volumeFadingTarget);
			g.setGain(state.volume);
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
				if(getDebugSettings().sound) System.out.println("updateFadingVolume() for sound #"+getID()+" reached zero... will clear sound...");
				state.soundFadedOut = true;
				pauseSound();
				clearSound();
			}
		}
	}

	/**
	 * Display the sound and/or metadata
	 */
	public void display(MultimediaLocator ml)
	{
//		if(getMediaState().showMetadata) displayMetadata(ml);
//		if(getMediaState().visible)
//		{
////			System.out.println("getWorldState().showModel:"+getWorldState().showModel+" isHidden():"+isHidden()+" isDisabled():"+isDisabled()+" wtf? "+(getWorldState().showModel && !isHidden() && !!isDisabled()));
//
//			if(getWorldState().showModel && !isHidden() && !isDisabled())
//			{
////				System.out.println("Will call displayModel()...");
//				displayModel(ml);
//			}
//			else if(ml.debugSettings.sound || ml.debugSettings.field) 
//				displayModel(ml);
//		}
	}

	/**
	 * Display sound capture location as a sphere in virtual space
	 * @param ml Parent app
	 */
	public void displayModel(MultimediaLocator ml)
	{
		ml.rectMode(PApplet.CENTER);
		ml.noStroke(); 

		ml.stroke(70, 220, 150);
		ml.fill(70, 220, 150);
		ml.strokeWeight(1.f);

		ml.pushMatrix();

		if(getViewerSettings().selection)
		{
			if(isSelected())
			{
				if(!getWorldState().alphaMode)
					ml.tint(getViewingBrightness(), 255);          				
				else
					ml.tint(255, getViewingBrightness());          				
			}
			else
			{
				if(!getWorldState().alphaMode)
					ml.tint(getViewingBrightness() * 0.333f, 255);          // Set the image transparency					
				else
					ml.tint(255, getViewingBrightness() * 0.333f);          				
			}
		}
		else
		{
			if(!getWorldState().alphaMode)
				ml.tint(getViewingBrightness(), 255);          				
			else
				ml.tint(255, PApplet.map(getViewingBrightness(), 0.f, 255.f, 0.f, getWorldState().alpha));          				
		}
		

		PVector loc = getLocation();
//		System.out.println("Sound.displayModel()... loc.x:"+loc.x+" getViewingBrightness():"+getViewingBrightness()+" getMediaState().centerSize * 2.f:"+(getMediaState().centerSize * 2.f));
		ml.translate(loc.x, loc.y, loc.z);
		ml.sphere(getMediaState().centerSize * 2.f);
		ml.popMatrix();
	}

	/**
	 * Load the sound from disk
	 */
	public void loadMedia(MultimediaLocator ml)
	{
		if(ml.debugSettings.sound) System.out.println("loadMedia() for sound #"+getID());
		
		if( ml.world.getCurrentField().getState().soundsAudible < ml.world.viewer.getSettings().maxAudibleSounds &&
				!getMediaState().hidden && !getMediaState().disabled)
		{
			ac = new AudioContext();
			player = new SamplePlayer(ac, SampleManager.sample(getMediaState().filePath));
			g = new Gain(ac, 2, 0.2f);
			g.addInput(player);
			ac.out.addInput(g);
			
			if(getViewerSettings().autoPlaySounds)
			{
				if(ml.world.getCurrentField().getVideosPlaying() < getViewerSettings().autoPlayMaxSoundCount)
					playSound();
			}
			else
				pauseSound();
			
			state.loaded = true;
		}
	}

	/**
	 * Start playing the sound
	 * @param pause 
	 */
	public void playSound()
	{
		if(getDebugSettings().sound) System.out.println("playSound()...");
		ac.start();					// Start audio context 
		state.playing = true;
		state.volume = 0.f;
		fadeSoundIn();
	}

	/**
	 * Fade sound out and pause sound once finished
	 */
	public void stopSound()
	{
		if(getDebugSettings().sound) System.out.println("stopSound()...");
		fadeSoundOut();														
		state.playing = false;
	}

	/**
	 * Pause sound
	 */
	private void pauseSound()
	{
		if(getDebugSettings().sound) System.out.println("pauseSound()...");
		player.pause(true);
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
				player.clearInputConnections();
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
		if(getDebugSettings().sound) System.out.println("fadeSoundIn()...");
		if(state.volume < getWorldSettings().videoMaxVolume)
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
	 */
	void fadeSoundOut()
	{
		if(getDebugSettings().sound) System.out.println("fadeSoundOut()...");
		if(state.volume > 0.f)
		{
			state.fadingVolume = true;
			state.volumeFadingStartFrame = getWorldState().frameCount; 
			state.volumeFadingStartVal = state.volume; 
			state.volumeFadingEndFrame = getWorldState().frameCount + state.volumeFadingLength;		// Fade volume out over n frames
			state.volumeFadingTarget = 0.f;
		}
	}

	/**
	 * Calculate sound location from GPS track waypoint closest to capture time
	 * @param gpsTrack GPS track as waypoint list
	 */
	void calculateLocationFromGPSTrack(ArrayList<WMV_Waypoint> gpsTrack)
	{
		if(getDebugSettings().sound) System.out.println("calculateLocationFromGPSTrack() for sound id#"+getID()+"...");

		float closestDist = 1000000.f;
		int closestIdx = -1;

		int sYear = time.getYear();
		int sMonth = time.getMonth();
		int sDay = time.getDay();
		int sHour = time.getHour();
		int sMinute = time.getMinute();
		int sSecond = time.getSecond();

		for(WMV_Waypoint w : gpsTrack)
		{
			int wYear = w.getTime().getYear();
			int wMonth = w.getTime().getMonth();
			int wDay = w.getTime().getDay();
			int wHour = w.getTime().getHour();
			int wMinute = w.getTime().getMinute();
			int wSecond = w.getTime().getSecond();

			if(wYear == sYear && wMonth == sMonth && wDay == sDay)			// On same day
			{
				int sTime = sHour * 60 + sMinute * 60 + sSecond;
				int wTime = wHour * 60 + wMinute * 60 + wSecond;

				float timeDist = Math.abs(wTime - sTime);
				if(timeDist <= closestDist)
				{
					closestDist = timeDist;
					closestIdx = w.getID();
				}
			}
		}

		if(closestIdx >= 0)
		{
			setGPSLocation( gpsTrack.get(closestIdx).getLocation() );
			if(getDebugSettings().sound)
			{
				System.out.println("Set sound #"+getID()+" GPS location to waypoint "+closestIdx+" waypoint hour:"+gpsTrack.get(closestIdx).getTime().getHour()+"   min:"+gpsTrack.get(closestIdx).getTime().getMinute());
				System.out.println("  Sound hour:"+sHour+" Sound min:"+sMinute+" Sound GPS X:"+getGPSLocation().x+" GPS Y:"+getGPSLocation().y);
			}
		}
		else 
			if(getDebugSettings().sound)
				System.out.println("No gps nodes on same day!");
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
	 * @return Distance between video and the viewer
	 */
	public float getHearingDistance()       // Find distance from camera to point in virtual space where photo appears           
	{
		PVector camLoc = getViewerState().getLocation();
		PVector loc = new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z);

		return PVector.dist(loc, camLoc);     
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

	public void setGPSLocationInMetadata(PVector newGPSLocation)
	{
		metadata.gpsLocation = newGPSLocation;
	}
	
	public boolean isFadingVolume()
	{
		return state.fadingVolume;
	}

	public boolean isLoaded()
	{
		return state.loaded;
	}

	public boolean isPlaying()
	{
		return state.playing;
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

		if(ml.debugSettings.sound)
		{
			ml.display.metadata(frameCount, strTitleDebug);
			ml.display.metadata(frameCount, strBrightness);
			ml.display.metadata(frameCount, strBrightnessFading);
		}
	}
}
