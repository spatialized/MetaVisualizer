package main.java.com.entoptic.multimediaLocator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;

//import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
//import com.luckycatlabs.sunrisesunset.dto.Location;

import processing.core.PVector;

/******************************************
 * @author davidgordon
 * Time a media file was captured
 */
public class WMV_Time implements Comparable<WMV_Time>
{
	private int id, clusterID;
	private int mediaType;				/* Media Types  0: image 1: panorama 2: video 3: sound */
	private int year, month, day, hour, minute, second, millisecond;
	private float time;	 				/* Normalized value where 0.f is midnight on calendar date and 1.f is midnight the following day */

	
	ZonedDateTime dateTime;
	String dateTimeString, timeZoneID;
	
	/**
	 * Constructor for media time
	 */
	WMV_Time(){}

	/**
	 * Initialize media time with given parameters
	 * @param newDateTime Zoned date/time
	 * @param newDateTimeString Date/time string from metadata
	 * @param newID Associated media object ID
	 * @param newMediaType Associated media object type
	 * @param newClusterID Cluster containing associated media object
	 * @param newTimeZoneID Time zone ID
	 */
	public void initialize( ZonedDateTime newDateTime, String newDateTimeString, int newID, int newMediaType, int newClusterID, 
							String newTimeZoneID )
	{
		id = newID;
		clusterID = newClusterID;					// Currently set to -1 when created, fixed later in cluster.createTimeline()
		timeZoneID = newTimeZoneID;
		
		dateTime = newDateTime;
		dateTimeString = newDateTimeString;
		
		mediaType = newMediaType;									
		
		year = dateTime.getYear();
		month = dateTime.getMonthValue();
		day = dateTime.getDayOfMonth();
		hour = dateTime.getHour();
		minute = dateTime.getMinute();
		second = dateTime.getSecond();
		millisecond = dateTime.getNano();
	
		time = getSimulationTime( dateTime ); 		// Get normalized capture time (0. to 1. for 0:00 to 24:00)
	}
	
	/**
	 * @return Year
	 */
	public int getYear()
	{
		return year;
	}

	/**
	 * @return Month of year
	 */
	public int getMonth()
	{
		return month;
	}

	/**
	 * @return Day of month
	 */
	public int getDay()
	{
		return day;
	}

	/**
	 * @return Hour of day
	 */
	public int getHour()
	{
		return hour;
	}

	/**
	 * @return Minute
	 */
	public int getMinute()
	{
		return minute;
	}

	/**
	 * @return Second
	 */
	public int getSecond()
	{
		return second;
	}
	
	/**
	 * @return Millisecond
	 */
	public int getMillisecond()
	{
		return millisecond;
	}
	
	/**
	 * @return Associated media id
	 */
	public int getID()
	{
		return id;
	}
	
	/**
	 * @return Associated media type
	 */
	public int getMediaType()
	{
		return mediaType;
	}
	
	/**
	 * @return Associated cluster ID
	 */
	public int getClusterID()
	{
		return clusterID;
	}
	
	/**
	 * @return Date object associated with this time
	 */
	public WMV_Date asDate()
	{
		WMV_Date date = new WMV_Date();
		date.initialize(-1, dateTime, dateTimeString, timeZoneID);
		return date;
	}

	/**
	 * @return Date associated with this time as a PVector in format (month, day, year)
	 */
	public PVector getDateAsPVector()
	{
		PVector date = new PVector(month, day, year);
		return date;
	}

	/**
	 * @return Time as normalized value where 0.f is midnight on calendar date and 1.f is midnight the following day
	 */
	public float getTime()
	{
		return time;
	}

	/**
	 * @return Date associated with this time as a PVector in format (hour, minute, second)
	 */
	public PVector getTimeAsPVector()
	{
		PVector t = new PVector(hour, minute, second);
		return t;
	}
	
	/**
	 * Compare this time segment with given one
	 * @param t Time segment to compare to
	 */
	public int compareTo(WMV_Time t)
	{
		return Float.compare(this.time, t.time);		
	}

	public static Comparator<WMV_Time> WMV_SimulationTimeComparator = new Comparator<WMV_Time>() 
	{
		public int compare(WMV_Time t1, WMV_Time t2) 
		{
			float time1 = t1.getTime();
			float time2 = t2.getTime();

			time1 *= 1000000.f;
			time2 *= 1000000.f;

			return (int)(time1 - time2);
		}
	};
	
	/**
	 * Calculate date, time and dayLength for given Calendar date
	 * @param c Calendar date
	 * @return PVector containing (date, time, dayLength)
	 */
	public float getSimulationTime(ZonedDateTime c) 	
	{		
//		Location location = new Location("39.9522222", "-75.1641667");
//		SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, "America/Los_Angeles");

//		Calendar sr = calculator.getOfficialSunriseCalendarForDate(c);		// Get sunrise time
//		Calendar ss = calculator.getOfficialSunsetCalendarForDate(c);		// Get sunset time

//		int srHour, srMin, srSec, ssHour, ssMin, ssSec;		/* Adjust for sunset time */
//		int cDay, cMonth, cYear;

		int cHour, cMin, cSec;

		cHour = c.getHour();
		cMin = c.getMinute();
		cSec = c.getSecond();

		float cTime = cHour * 60 + cMin + cSec/60.f;
		float time = mapValue(cTime, 0.f, 1439.f, 0.f, 1.f); // Time of day when photo was taken	(1440 == 24 * 60)		

		return time;				// Date between 0.f and 1.f, time between 0. and 1.
	}
	
	/**
	 * Set spatial cluster ID associated with this time
	 * @param newClusterID New cluster ID
	 */
	public void setClusterID(int newClusterID)
	{
		clusterID = newClusterID;
	}
	
	/**
	 * Initialize time object from loaded date/time string
	 */
	public void initializeTime()
	{
		dateTime = parseDateTime(dateTimeString);
	}
	
	/**
	 * Parse date/time string from metadata given media time zone
	 * @param input String to parse
	 * @return ZonedDateTime object corresponding to given string
	 */
	public ZonedDateTime getDateTimeWithTimeZone(String newTimeZoneID) 					// 2016:04:10 17:52:39
	{		
		String[] parts = dateTimeString.split(":");

		int year = Integer.valueOf(parts[0].trim());
		int month = Integer.valueOf(parts[1]);
		int min = Integer.valueOf(parts[3]);
		int sec = Integer.valueOf(parts[4]);
		String input = parts[2];
		parts = input.split(" ");
		int day = Integer.valueOf(parts[0]);
		int hour = Integer.valueOf(parts[1]);

		ZonedDateTime zoned = ZonedDateTime.of(year, month, day, hour, min, sec, 0, ZoneId.of(newTimeZoneID));
		return zoned;
	}

	/**
	 * Parse date/time string from metadata given media time zone
	 * @param input String to parse
	 * @return ZonedDateTime object corresponding to given string
	 */
	public ZonedDateTime parseDateTime(String input) 					// 2016:04:10 17:52:39
	{		
		String[] parts = input.split(":");

		int year = Integer.valueOf(parts[0].trim());
		int month = Integer.valueOf(parts[1]);
		int min = Integer.valueOf(parts[3]);
		int sec = Integer.valueOf(parts[4]);
		input = parts[2];
		parts = input.split(" ");
		int day = Integer.valueOf(parts[0]);
		int hour = Integer.valueOf(parts[1]);

		ZonedDateTime zoned = ZonedDateTime.of(year, month, day, hour, min, sec, 0, ZoneId.of(timeZoneID));
		return zoned;
	}
	
	/**
	 * Map a value from given range to new range
	 * @param val Value to map
	 * @param min Initial range minimum
	 * @param max Initial range maximum
	 * @param min2 New range minimum
	 * @param max2 New range maximum
	 * @return Mapped value
	 */
	public float mapValue(float val, float min, float max, float min2, float max2)
	{
	  float res;
	  res = (((max2-min2)*(val-min))/(max-min)) + min2;
	  return res;
	}
}

