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

		filePath = newFilePath;
		gpsLocation = newGPSLocation;
		
//		Bead sound = new Bead();
		
		if(newDateTime != null)
		{
			time = new WMV_Time( newDateTime, getID(), cluster, 3, newTimeZone );		
		}
		else
			time = null;
	}  

	/**
	 * Display the image in virtual space
	 */
	public void draw(WMV_World world)
	{
		if(showMetadata) displayMetadata(world);
	}

	/**
	 * Load the sound from disk
	 */
	public void loadMedia(MultimediaLocator ml)
	{
//		if( p.soundsAudible < p.maxAudibleSounds && !hidden && !disabled)
//		{
//			sound = ;
//		}
	}

	/**
	 * @param size Size to draw the sound center
	 * Draw the video center as a colored sphere
	 */
	void displayModel(WMV_World world)
	{
		world.p.pushMatrix();
		
		world.p.fill(30, 0, 255, 150);
		world.p.translate(location.x, location.y, location.z);
		world.p.sphere(centerSize);

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
		String strID = "ID: "+String.valueOf(getID());
		String strCluster = "Cluster: "+String.valueOf(cluster);
		String strX = "Location X: "+String.valueOf(getCaptureLocation().z);
		String strY = " Y: "+String.valueOf(getCaptureLocation().x);
		String strZ = " Z: "+String.valueOf(getCaptureLocation().y);
	
		String strDate = "Date: "+String.valueOf(time.getMonth()) + String.valueOf(time.getDay()) + String.valueOf(time.getYear());
		String strTime = "Time: "+String.valueOf(time.getHour()) + ":" + (time.getMinute() >= 10 ? String.valueOf(time.getMinute()) : "0"+String.valueOf(time.getMinute())) + ":" + 
				 (time.getSecond() >= 10 ? String.valueOf(time.getSecond()) : "0"+String.valueOf(time.getSecond()));

		String strLatitude = "GPS Latitude: "+String.valueOf(gpsLocation.z);
		String strLongitude = " Longitude: "+String.valueOf(gpsLocation.x);
		String strAltitude = "Altitude: "+String.valueOf(gpsLocation.y);
//		String strTheta = "Direction: "+String.valueOf(theta);

		String strTitleDebug = "--- Debugging ---";
		String strBrightness = "brightness: "+String.valueOf(viewingBrightness);
		String strBrightnessFading = "brightnessFadingValue: "+String.valueOf(fadingBrightness);
		
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
	 * Search given list of clusters and associated with this image
	 * @return Whether associated field was successfully found
	 */	
	public boolean findAssociatedCluster(ArrayList<WMV_Cluster> clusterList, float maxClusterDistance)    				 // Associate cluster that is closest to photo
	{
		int closestClusterIndex = 0;
		float closestDistance = 100000;

		for (int i = 0; i < clusterList.size(); i++) 
		{     
			WMV_Cluster curCluster = clusterList.get(i);
			float distanceCheck = getCaptureLocation().dist(curCluster.getLocation());

			if (distanceCheck < closestDistance)
			{
				closestClusterIndex = i;
				closestDistance = distanceCheck;
			}
		}

		if(closestDistance < maxClusterDistance)
			cluster = closestClusterIndex;		// Associate image with cluster
		else
			cluster = -1;						// Create a new single image cluster here!

		if(cluster != -1)
			return true;
		else
			return false;
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
			location = gpsTrack.get(closestIdx).getLocation();
			if(debugSettings.sound)
			{
				System.out.println("Set sound #"+getID()+" location to waypoint "+closestIdx+" W hour:"+gpsTrack.get(closestIdx).getTime().getHour()+" W min:"+gpsTrack.get(closestIdx).getTime().getMinute());
				System.out.println("S hour:"+sHour+" S min:"+sMinute);
				System.out.println("location.x: "+location.x+" location.y:"+location.y+" location.z:"+location.z);
				System.out.println("timeDist: "+closestDist);
			}
		}
		else 
			if(debugSettings.sound)
				System.out.println("No gps nodes on same day!");
	}
	
	/**
	 * @return How far the video is from the camera
	 */
	public float getHearingDistance()       // Find distance from camera to point in virtual space where photo appears           
	{
		PVector camLoc = viewerState.getLocation();
		
		PVector loc = new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z);
		float distance = PVector.dist(loc, camLoc);     

		return distance;
	}
}
