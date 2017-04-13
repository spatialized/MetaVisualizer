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
	WMV_SoundMetadata(String newName, String newFilePath, PVector newGPSLocation, float newTheta, int newCameraModel, float newBrightness, 
			ZonedDateTime newDateTime, String newDateTimeString, String newTimeZone, String[] newKeywords)
	{
		super(newName, newFilePath, newGPSLocation, newDateTime, newDateTimeString, newTimeZone, newKeywords);
		
	}

	WMV_SoundMetadata(){}
	
	public void initialize(String newName, String newFilePath, PVector newGPSLocation, float newTheta, int newCameraModel, float newBrightness, 
			ZonedDateTime newDateTime, String newDateTimeString, String newTimeZone, String[] newKeywords)
	{
		super.init(newName, newFilePath, newGPSLocation, newDateTime, newDateTimeString, newTimeZone, newKeywords);
	}
}
