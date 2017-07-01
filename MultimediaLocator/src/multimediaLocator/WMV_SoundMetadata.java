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
	 * @param newName Sound name
	 * @param newFilePath File path
	 * @param newGPSLocation
	 * @param newTheta
	 * @param newCameraModel
	 * @param newBrightness
	 * @param newDateTime
	 * @param newDateTimeString
	 * @param newTimeZone
	 * @param newKeywords
	 * @param newSoftware Recording software
	 * @param newLongitudeRef Longitude Reference {E or W}
	 * @param newLatitudeRef Latitude reference {N or S}
	 */
	WMV_SoundMetadata( String newName, String newFilePath, PVector newGPSLocation, float newTheta, int newCameraModel, float newBrightness, 
			ZonedDateTime newDateTime, String newDateTimeString, String newTimeZone, String[] newKeywords, String newSoftware,
			String newLongitudeRef, String newLatitudeRef )
	{
		super( newName, newFilePath, newGPSLocation, newDateTime, newDateTimeString, newTimeZone, newKeywords, newSoftware, newLongitudeRef, 
			   newLatitudeRef );
		
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
			ZonedDateTime newDateTime, String newDateTimeString, String newTimeZone, String[] newKeywords, String newSoftware, String newLongitudeRef, 
			String newLatitudeRef )
	{
		super.init( newName, newFilePath, newGPSLocation, newDateTime, newDateTimeString, newTimeZone, newKeywords, newSoftware, newLongitudeRef,
				    newLatitudeRef );
	}
	
	/**
	 * @return Whether metadata is valid
	 */
	public boolean isValid()
	{
		String parts[] = super.name.split("\\.");
		String extension = parts[1];
		
		if(extension.equals("wav") || extension.equals("WAV") || extension.equals("aiff") || extension.equals("AIFF"))
			return true;
		else
			return false;
		
//		if(gpsLocation.x != 0.f && gpsLocation.z != 0.f && gpsLocation.z != 0.f)	// -- Sound location metadata is usually missing
//			return true;
//		else
//			return false;
	}
}
