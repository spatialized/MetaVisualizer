package multimediaLocator;

import java.time.ZonedDateTime;
import java.util.ArrayList;
//import java.util.Calendar;

import beads.*;
import processing.core.PApplet;
import processing.core.PVector;

/**************************************************
 * @author davidgordon
 * Represents a sound in 3D virtual space
 */

public class WMV_Sound extends WMV_Viewable						 
{
//	/* Classes */
//	WMV_WorldSettings worldSettings;
//	WMV_ViewerSettings viewerSettings;	// Update world settings
//	ML_DebugSettings debugSettings;	// Update world settings

//	SoundFile sound;
	
	private int id;
	private Bead sound;
	private float length;

	/* Sound */
	private float volume = 0.f;			// Video volume between 0. and 1.
	private boolean fadingVolume = false;
	private int volumeFadingStartFrame = 0, volumeFadingEndFrame = 0;
	private float volumeFadingStartVal = 0.f, volumeFadingTarget = 0.f;
	private final int volumeFadingLength = 60;	// Fade volume over 30 frames
	
	WMV_Sound ( int newID, int newMediaType, String newName, String newFilePath, PVector newGPSLocation, float newTheta, 
				int newCameraModel, float newBrightness, ZonedDateTime newDateTime, String newTimeZone )
	{
		super(newID, newMediaType, newName, newFilePath, newGPSLocation, newTheta, newCameraModel, newBrightness, newDateTime, newTimeZone);

//		p = parent;
//		id = newID;

//		worldSettings = newWorldSettings;
//		viewerSettings = newViewerSettings;
//		debugSettings = newDebugSettings;
		

		filePath = newFilePath;
		gpsLocation = newGPSLocation;
		
//		Bead sound = new Bead();
		
		if(newDateTime != null)
		{
			time = new WMV_Time( newDateTime, getID(), cluster, 3, newTimeZone );		
//			WMV_Time utcTime = new WMV_Time( p.p, newDateTime, getID(), cluster, 3 );		
//			time = p.p.utilities.utcToPacificTime(utcTime);						// Convert from UTC Time
		}
		else
			time = null;

//		theta = newTheta;              		// GPS Orientation (Yaw angle)
//		phi = newElevation;            		// Pitch angle
//		rotation = newRotation;             // Rotation angle
//		orientation = newOrientation;       // Vertical (90) or Horizontal (0)
	}  

	/**
	 * Display the image in virtual space
	 */
	public void draw(WMV_World world)
	{
		if(showMetadata) displayMetadata(world);
//		if(p.p.viewer.selection)
//			p.p.viewer.addSelectableSound(getID());
	}

	/**
	 * Load the sound from disk
	 */
	public void loadMedia(MultimediaLocator ml)
	{
//			if( p.soundsAudible < p.maxAudibleSounds && !hidden && !disabled)
//			{
//				sound = ;
//			}
	}

	/**
	 * @param size Size to draw the sound center
	 * Draw the video center as a colored sphere
	 */
	void displayModel(WMV_World world)
	{
		world.p.pushMatrix();
		world.p.translate(location.x, location.y, location.z);

		world.p.fill(30, 0, 255, 150);
		world.p.sphere(centerSize);
//		PVector c = world.getCluster(cluster).getLocation();
//		PVector loc = location;
//		PVector cl = getCaptureLocation();
		world.p.popMatrix();

//		world.p.pushMatrix();
//		if(world.showMediaToCluster)
//		{
//			world.p.strokeWeight(5.f);
//			world.p.stroke(40, 155, 255, 180);
//			world.p.line(c.x, c.y, c.z, loc.x, loc.y, loc.z);
//		}
//
//		if(world.showCaptureToMedia)
//		{
//			world.p.strokeWeight(2.f);
//			world.p.stroke(160, 100, 255, 120);
//			world.p.line(cl.x, cl.y, cl.z, loc.x, loc.y, loc.z);
//		}
//
//		if(world.showCaptureToCluster)
//		{
//			world.p.strokeWeight(3.f);
//			world.p.stroke(100, 55, 255, 180);
//			world.p.line(c.x, c.y, c.z, cl.x, cl.y, cl.z);
//		}

		world.p.popMatrix();
	}

	/**
	 * Draw the image metadata in Heads-Up Display
	 */
	public void displayMetadata(WMV_World world)
	{
		String strTitleImage = "Sound";
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
//		String strTheta = "Direction: "+PApplet.str(theta);

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

		if(world.p.debug.sound)
		{
			world.p.display.metadata(world, strTitleDebug);
			world.p.display.metadata(world, strBrightness);
			world.p.display.metadata(world, strBrightnessFading);
		}
	}
	
	/**
	 * Fade in sound
	 */
	void fadeSoundIn()
	{
		if(volume < worldSettings.videoMaxVolume)
		{
			fadingVolume = true;
			volumeFadingStartFrame = worldState.frameCount; 
			volumeFadingStartVal = volume; 
			volumeFadingEndFrame = worldState.frameCount + volumeFadingLength;		// Fade volume over 30 frames
			volumeFadingTarget = worldSettings.videoMaxVolume;
		}
	}
	
	/**
	 * Fade out sound
	 */
	void fadeSoundOut()
	{
		if(volume > 0.f)
		{
			fadingVolume = true;
			volumeFadingStartFrame = worldState.frameCount; 
			volumeFadingStartVal = volume; 
			volumeFadingEndFrame = worldState.frameCount + volumeFadingLength;		// Fade volume over 30 frames
			volumeFadingTarget = 0.f;
		}
	}
	
	/**
	 * Calculate sound location from GPS track waypoint closest to capture time
	 * @param gpsTrack
	 */
	void calculateLocationFromGPSTrack(ArrayList<WMV_Waypoint> gpsTrack)
	{
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
				
				float timeDist = PApplet.abs(wTime - sTime);
				if(timeDist <= closestDist)
				{
					closestDist = timeDist;
					closestIdx = w.getID();
				}
			}
		}
		
		if(closestIdx >= 0)
		{
			location = gpsTrack.get(closestIdx).getLocation();
//			if(p.p.p.debug.sound)
			{
				PApplet.println("Set sound #"+getID()+" location to waypoint "+closestIdx+" W hour:"+gpsTrack.get(closestIdx).getTime().getHour()+" W min:"+gpsTrack.get(closestIdx).getTime().getMinute());
				PApplet.println("S hour:"+sHour+" S min:"+sMinute);
				PApplet.println("location.x: "+location.x+" location.y:"+location.y+" location.z:"+location.z);
				PApplet.println("timeDist: "+closestDist);
			}
		}
		else 
//			if(p.p.p.debug.sound)
			PApplet.println("No gps nodes on same day!");
	}
	
	/**
	 * @return How far the video is from the camera
	 */
	public float getHearingDistance()       // Find distance from camera to point in virtual space where photo appears           
	{
		PVector camLoc;

//		if(p.p.orientationMode)
//		{
//			camLoc = p.p.viewer.getLocation();
//		}
//		else
			camLoc = viewerState.getLocation();
		
		PVector loc = new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z);
		float distance = PVector.dist(loc, camLoc);     

		return distance;
	}
}
