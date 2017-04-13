package multimediaLocator;

import java.time.ZonedDateTime;

import processing.core.PVector;

/**
 * General metadata for any media type 
 * @author davidgordon
 *
 */
public abstract class WMV_MediaMetadata 
{
	public String name;
	public String filePath;
	public String dateTimeString;
	public ZonedDateTime dateTime;
	public String timeZone;
	public PVector gpsLocation;
	public String[] keywords;

	WMV_MediaMetadata ( String newName, String newFilePath, PVector newGPSLocation, ZonedDateTime newDateTime, String newDateTimeString,
						String newTimeZone, String[] newKeywords )
	{
		name = newName;
		filePath = newFilePath;
		
		timeZone = newTimeZone;

		dateTimeString = newDateTimeString;
		if(newDateTime != null) dateTime = newDateTime;
		if(newGPSLocation != null) gpsLocation = newGPSLocation;
		if(newKeywords != null) keywords = newKeywords;
	}
	
	WMV_MediaMetadata(){}
	
	public void init( String newName, String newFilePath, PVector newGPSLocation, ZonedDateTime newDateTime, String newDateTimeString, 
			String newTimeZone, String[] newKeywords )
	{
		name = newName;
		filePath = newFilePath;
		
		timeZone = newTimeZone;

		dateTimeString = newDateTimeString;
		if(newDateTime != null) dateTime = newDateTime;
		if(newGPSLocation != null) gpsLocation = newGPSLocation;
		if(newKeywords != null) keywords = newKeywords;
	}
}
