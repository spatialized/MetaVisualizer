package multimediaLocator;

import java.time.ZonedDateTime;

import processing.core.PVector;

/**
 * Abstract media metadata, independent of type
 * @author davidgordon
 */
public abstract class WMV_MediaMetadata 
{
	/* General */
	public String name;
	public String filePath;
	
	/* Location */
	public PVector gpsLocation;
	public String longitudeRef = "", latitudeRef = "";
	
	/* Time */
	public String dateTimeString;
	public ZonedDateTime dateTime;
	public String timeZone;
	
	/* Other */
	public String[] keywords;
	public String software;
	
	/**
	 * Constructor for media metadata
	 * @param newName Media name
	 * @param newFilePath File path
	 * @param newGPSLocation GPS Location
	 * @param newDateTime Date/time
	 * @param newDateTimeString Date/time as string
	 * @param newTimeZone Time zone
	 * @param newKeywords Media keywords
	 * @param newSoftware Recording software
	 * @param newLongitudeRef Longitude reference {E or W}
	 * @param newLatitudeRef Latitude reference {N or S}
	 */
	WMV_MediaMetadata ( String newName, String newFilePath, PVector newGPSLocation, ZonedDateTime newDateTime, String newDateTimeString,
						String newTimeZone, String[] newKeywords, String newSoftware, String newLongitudeRef, String newLatitudeRef )
	{
		name = newName;
		filePath = newFilePath;
		
		if(newGPSLocation != null) gpsLocation = newGPSLocation;
		longitudeRef = newLongitudeRef;
		latitudeRef = newLatitudeRef;
		
		timeZone = newTimeZone;				
		dateTimeString = newDateTimeString;
		if(newDateTime != null) dateTime = newDateTime;
		
		if(newKeywords != null) keywords = newKeywords;
		software = newSoftware;
	}
	
	WMV_MediaMetadata(){}
	
	/**
	 * Initialize media metadata object
	 * @param newName Media name
	 * @param newFilePath Filepath
	 * @param newGPSLocation GPS location
	 * @param newDateTime Date/time
	 * @param newDateTimeString Date/time as string
	 * @param newTimeZone Time zone ID string
	 * @param newKeywords Keyword list
	 * @param newSoftware Recording software
	 * @param newLongitudeRef Longitude reference
	 * @param newLatitudeRef Latitude reference
	 */
	public void init( String newName, String newFilePath, PVector newGPSLocation, ZonedDateTime newDateTime, String newDateTimeString, 
			String newTimeZone, String[] newKeywords, String newSoftware, String newLongitudeRef, String newLatitudeRef )
	{
		name = newName;
		filePath = newFilePath;
		
		if(newGPSLocation != null) gpsLocation = newGPSLocation;
		longitudeRef = newLongitudeRef;
		latitudeRef = newLatitudeRef;

		timeZone = newTimeZone;
		dateTimeString = newDateTimeString;
		if(newDateTime != null) dateTime = newDateTime;

		if(newKeywords != null) keywords = newKeywords;
		software = newSoftware;
	}
}
