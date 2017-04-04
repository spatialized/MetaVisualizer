package multimediaLocator;

import java.time.ZonedDateTime;

import processing.core.PVector;

/**
 * Metadata fields applicable to every media type 
 * @author davidgordon
 *
 */
public abstract class WMV_MediaMetadata 
{
	public String name;
	public String filePath;
	public ZonedDateTime dateTime;
	public String timeZone;
	public PVector gpsLocation;
	
	WMV_MediaMetadata ( String newName, String newFilePath, PVector newGPSLocation, ZonedDateTime newDateTime, String newTimeZone )
	{
		name = newName;
		filePath = newFilePath;

		timeZone = newTimeZone;
		if(newDateTime != null) dateTime = newDateTime;
		if(newGPSLocation != null) gpsLocation = newGPSLocation;
	}
}
