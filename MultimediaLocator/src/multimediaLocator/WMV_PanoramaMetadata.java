package multimediaLocator;

import java.time.ZonedDateTime;

import processing.core.PVector;

/******************************************
 * Metadata for a 360-degree panorama
 * @author davidgordon
 *
 */
public class WMV_PanoramaMetadata extends WMV_MediaMetadata
{
	public int cameraModel;                 	// Camera model
	public float brightness;					// Panorama pixel brightness
	public float theta = -100000.f;             // Media Orientation (in Degrees N)

	public int imageWidth, imageHeight;			// Image width and height
	
	/**
	 * Constructor for panorama metadata
	 * @param newName Panorama name
	 * @param newFilePath Panorama file path
	 * @param newGPSLocation
	 * @param newDateTime
	 * @param newDateTimeString
	 * @param newTimeZone
	 * @param newTheta
	 * @param newCameraModel
	 * @param newWidth
	 * @param newHeight
	 * @param newBrightness
	 * @param newKeywords
	 * @param newLongitudeRef Longitude Reference {E or W}
	 * @param newLatitudeRef Latitude reference {N or S}
	 */
	WMV_PanoramaMetadata( String newName, String newFilePath, PVector newGPSLocation, ZonedDateTime newDateTime, String newDateTimeString, 
			String newTimeZone, float newTheta, int newCameraModel, int newWidth, int newHeight, float newBrightness, String[] newKeywords, 
			String newSoftware, String newLongitudeRef, String newLatitudeRef )
	{
		super( newName, newFilePath, newGPSLocation, newDateTime, newDateTimeString, newTimeZone, newKeywords, newSoftware, newLongitudeRef, 
			   newLatitudeRef );
		
		imageWidth = newWidth;
		imageHeight = newHeight;

		theta = newTheta;
		brightness = newBrightness;
		cameraModel = newCameraModel;
	}
	
	WMV_PanoramaMetadata(){}

	/**
	 * Initialize panorama metadata object
	 * @param newName
	 * @param newFilePath
	 * @param newGPSLocation
	 * @param newDateTime
	 * @param newDateTimeString
	 * @param newTimeZone
	 * @param newTheta
	 * @param newCameraModel
	 * @param newWidth
	 * @param newHeight
	 * @param newBrightness
	 * @param newKeywords
	 */
	public void initialize(String newName, String newFilePath, PVector newGPSLocation, ZonedDateTime newDateTime, String newDateTimeString, String newTimeZone, 
			float newTheta, int newCameraModel, int newWidth, int newHeight, float newBrightness, String[] newKeywords, String newSoftware,
			String newLongitudeRef, String newLatitudeRef )
	{
		super.init( newName, newFilePath, newGPSLocation, newDateTime, newDateTimeString, newTimeZone, newKeywords, newSoftware, newLongitudeRef,
				    newLatitudeRef );
		
		imageWidth = newWidth;
		imageHeight = newHeight;

		theta = newTheta;
		brightness = newBrightness;
		cameraModel = newCameraModel;
	}
	
	/**
	 * @return Whether metadata is valid
	 */
	public boolean isValid()
	{
		if(gpsLocation.x != 0.f && gpsLocation.z != 0.f && gpsLocation.z != 0.f)
			return true;
		else
			return false;
	}
}
