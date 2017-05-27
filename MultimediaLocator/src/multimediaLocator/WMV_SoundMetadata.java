package multimediaLocator;

import java.time.ZonedDateTime;

import processing.core.PVector;

/**
 * Metadata for a sound file
 * @author davidgordon
 *
 */
public class WMV_SoundMetadata extends WMV_MediaMetadata
{
	/**
	 * Constructor for sound metadata
	 * @param newName
	 * @param newFilePath
	 * @param newGPSLocation
	 * @param newTheta
	 * @param newCameraModel
	 * @param newBrightness
	 * @param newDateTime
	 * @param newDateTimeString
	 * @param newTimeZone
	 * @param newKeywords
	 */
	WMV_SoundMetadata(String newName, String newFilePath, PVector newGPSLocation, float newTheta, int newCameraModel, float newBrightness, 
			ZonedDateTime newDateTime, String newDateTimeString, String newTimeZone, String[] newKeywords, String newSoftware)
	{
		super(newName, newFilePath, newGPSLocation, newDateTime, newDateTimeString, newTimeZone, newKeywords, newSoftware);
		
	}

	/**
	 * Dummy constructor for sound metadata
	 */
	WMV_SoundMetadata(){}
	
	/**
	 * Initialize sound metadata object
	 * @param newName
	 * @param newFilePath
	 * @param newGPSLocation
	 * @param newTheta
	 * @param newCameraModel
	 * @param newBrightness
	 * @param newDateTime
	 * @param newDateTimeString
	 * @param newTimeZone
	 * @param newKeywords
	 */
	public void initialize(String newName, String newFilePath, PVector newGPSLocation, float newTheta, int newCameraModel, float newBrightness, 
			ZonedDateTime newDateTime, String newDateTimeString, String newTimeZone, String[] newKeywords, String newSoftware)
	{
		super.init(newName, newFilePath, newGPSLocation, newDateTime, newDateTimeString, newTimeZone, newKeywords, newSoftware);
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
