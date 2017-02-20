package wmViewer;

import java.util.ArrayList;
import java.util.Calendar;

import beads.*;
import processing.core.PApplet;
import processing.core.PVector;

/**************************************************
 * @author davidgordon
 * Represents a sound in 3D virtual space
 */

public class WMV_Sound extends WMV_Viewable						 
{
//	SoundFile sound;
	
	private Bead sound;
	private float length;

	/* Sound */
	private float volume = 0.f;			// Video volume between 0. and 1.
	private boolean fadingVolume = false;
	private int volumeFadingStartFrame = 0, volumeFadingEndFrame = 0;
	private float volumeFadingStartVal = 0.f, volumeFadingTarget = 0.f;
	private final int volumeFadingLength = 60;	// Fade volume over 30 frames
	
	WMV_Field p;					// Parent field

	WMV_Sound ( WMV_Field parent, int newID, String newName, String newFilePath, PVector newGPSLocation, float newTheta, int newCameraModel, 
			float newBrightness, Calendar newCalendar )
	{
		super(parent, newID, newName, newFilePath, newGPSLocation, newTheta, newCameraModel, newBrightness, newCalendar);

		p = parent;
		filePath = newFilePath;

		gpsLocation = newGPSLocation;
		
//		Bead sound = new Bead();
		
		if(newCalendar != null)
		{
			time = new WMV_Time( p.p, newCalendar, getID(), 0 );
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
	public void draw()
	{
//		if(p.p.viewer.selection)
//			p.p.viewer.addSelectableSound(getID());
	}

	/**
	 * Load the sound from disk
	 */
	public void loadMedia()
	{
//			if( p.soundsAudible < p.maxAudibleSounds && !hidden && !disabled)
//			{
//				sound = ;
//			}
	}
	
	/**
	 * Draw the image metadata in Heads-Up Display
	 */
	public void displayMetadata()
	{
		String strTitleImage = "Image";
		String strTitleImage2 = "-----";
		String strName = "Name: "+getName();
		String strID = "ID: "+PApplet.str(getID());
		String strCluster = "Cluster: "+PApplet.str(cluster);
		String strX = "Location X: "+PApplet.str(getCaptureLocation().z);
		String strY = " Y: "+PApplet.str(getCaptureLocation().x);
		String strZ = " Z: "+PApplet.str(getCaptureLocation().y);
		
		String strLatitude = "GPS Latitude: "+PApplet.str(gpsLocation.z);
		String strLongitude = " Longitude: "+PApplet.str(gpsLocation.x);
		String strAltitude = "Altitude: "+PApplet.str(gpsLocation.y);
//		String strTheta = "Direction: "+PApplet.str(theta);

		String strTitleDebug = "--- Debugging ---";
		String strBrightness = "brightness: "+PApplet.str(viewingBrightness);
		String strBrightnessFading = "brightnessFadingValue: "+PApplet.str(fadingBrightness);
		
		p.p.display.metadata(strTitleImage);
		p.p.display.metadata(strTitleImage2);
		p.p.display.metadata("");
		p.p.display.metadata(strID);
		p.p.display.metadata(strCluster);
		p.p.display.metadata(strName);
		p.p.display.metadata(strX + strY + strZ);

		p.p.display.metadata(strLatitude + strLongitude);
		p.p.display.metadata(strAltitude);

		if(p.p.p.debug.image)
		{
			p.p.display.metadata(strTitleDebug);
			p.p.display.metadata(strBrightness);
			p.p.display.metadata(strBrightnessFading);
		}
	}
	
	/**
	 * Fade in sound
	 */
	void fadeSoundIn()
	{
		if(volume < p.p.videoMaxVolume)
		{
			fadingVolume = true;
			volumeFadingStartFrame = p.p.p.frameCount; 
			volumeFadingStartVal = volume; 
			volumeFadingEndFrame = p.p.p.frameCount + volumeFadingLength;		// Fade volume over 30 frames
			volumeFadingTarget = p.p.videoMaxVolume;
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
			volumeFadingStartFrame = p.p.p.frameCount; 
			volumeFadingStartVal = volume; 
			volumeFadingEndFrame = p.p.p.frameCount + volumeFadingLength;		// Fade volume over 30 frames
			volumeFadingTarget = 0.f;
		}
	}
	
	void calculateLocationFromGPSTrack(ArrayList<WMV_Waypoint> gpsTrack)
	{
		
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
			camLoc = p.p.viewer.getLocation();
		
		PVector loc = new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z);
		float distance = PVector.dist(loc, camLoc);     

		return distance;
	}
}
