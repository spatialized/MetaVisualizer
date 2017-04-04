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
			ZonedDateTime newDateTime, String newTimeZone)
	{
		super(newName, newFilePath, newGPSLocation, newDateTime, newTimeZone);
		
	}
}
