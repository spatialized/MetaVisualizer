package multimediaLocator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
//import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;

//import processing.core.PApplet;
import processing.core.PVector;

/******************************************
 * @author davidgordon
 * Time of day when a media file was captured
 */
public class WMV_Time implements Comparable<WMV_Time>
{
	private int id, clusterID, mediaType;
	private int year, month, day, hour, minute, second, millisecond;
//	private float dayLength;
	private float time;
//	private Calendar calendar;
	
	private WMV_World p;
	
	ZonedDateTime dateTime;
//	ZonedDateTime date = ZonedDateTime.of(year, month, day, hour, time.getMinute(), time.getSecond(), time.getMillisecond(), ZoneId.of("America/Los_Angeles"));

//	WMV_Time(WMV_World parent, Calendar newCalendar, int newID, int newClusterID, int newMediaType)
	WMV_Time(WMV_World parent, ZonedDateTime newDateTime, int newID, int newClusterID, int newMediaType)
	{
		p = parent;
//		calendar = newCalendar;
		dateTime = newDateTime;
		
		id = newID;
		clusterID = newClusterID;
		
		mediaType = newMediaType;									// 0: image 1: panorama 2: video 3: sound
		
		year = dateTime.getYear();
		month = dateTime.getMonthValue();
		day = dateTime.getDayOfMonth();
		hour = dateTime.getHour();
		minute = dateTime.getMinute();
		second = dateTime.getSecond();
		millisecond = dateTime.getNano();
		
//		year = newCalendar.get(Calendar.YEAR);
//		month = newCalendar.get(Calendar.MONTH);
//		day = newCalendar.get(Calendar.DAY_OF_MONTH);
//		hour = newCalendar.get(Calendar.HOUR_OF_DAY);
//		minute = newCalendar.get(Calendar.MINUTE);
//		second = newCalendar.get(Calendar.SECOND);
//		millisecond = newCalendar.get(Calendar.MILLISECOND);		// Check this!!
	
//		time = p.p.utilities.getSimulationTime( newCalendar ); 		// Get normalized capture time (0. to 1. for 0:00 to 24:00)
		time = p.p.utilities.getSimulationTime( dateTime ); 		// Get normalized capture time (0. to 1. for 0:00 to 24:00)
	}
	
	public int getYear()
	{
		return year;
	}

	public int getMonth()
	{
		return month;
	}

	public int getDay()
	{
		return day;
	}

	public int getHour()
	{
		return hour;
	}

	public int getMinute()
	{
		return minute;
	}

	public int getSecond()
	{
		return second;
	}
	
	public int getMillisecond()
	{
		return millisecond;
	}
	
	public int getID()
	{
		return id;
	}
	
	public int getClusterID()
	{
		return clusterID;
	}
	
	public int getMediaType()
	{
		return mediaType;
	}
	
	/**
	 * @return Date associated with this time
	 */
	public WMV_Date getDate()
	{
		WMV_Date date = new WMV_Date(p, -1, dateTime);
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
	 * @return Time as a normalized value where 0.f is midnight on calendar date and 1.f is midnight the following day
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
	 * @param t Time segment to compare to
	 * Compare this time segment with given one
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
	 * @return Day length associated with this time (unused)
	 */
//	public float getDayLength()
//	{
//		return dayLength;
//	}
}

