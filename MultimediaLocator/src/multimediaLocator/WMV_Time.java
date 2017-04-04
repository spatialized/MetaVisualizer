package multimediaLocator;

import java.time.ZonedDateTime;
import java.util.Comparator;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import processing.core.PApplet;
import processing.core.PVector;

/******************************************
 * @author davidgordon
 * Time a media file was captured
 */
public class WMV_Time implements Comparable<WMV_Time>
{
	private int id, clusterID;
	private int mediaType;							/* Media Types  0: image 1: panorama 2: video 3: sound */
	private int year, month, day, hour, minute, second, millisecond;
	private float time;
	
	ZonedDateTime dateTime;
	String timeZoneID;
	
	WMV_Time(ZonedDateTime newDateTime, int newID, int newClusterID, int newMediaType, String newTimeZoneID)
	{
		dateTime = newDateTime;
		
		id = newID;
		clusterID = newClusterID;
		mediaType = newMediaType;									
		timeZoneID = newTimeZoneID;
		
		year = dateTime.getYear();
		month = dateTime.getMonthValue();
		day = dateTime.getDayOfMonth();
		hour = dateTime.getHour();
		minute = dateTime.getMinute();
		second = dateTime.getSecond();
		millisecond = dateTime.getNano();
	
		time = getSimulationTime( dateTime ); 		// Get normalized capture time (0. to 1. for 0:00 to 24:00)
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
		WMV_Date date = new WMV_Date(-1, dateTime, timeZoneID);
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
	 * @param c Calendar date
	 * @return PVector containing (date, time, dayLength)
	 * Calculate date, time and dayLength for given Calendar date
	 */
	public float getSimulationTime(ZonedDateTime c) 	
	{		
		Location location = new Location("39.9522222", "-75.1641667");
		SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, "America/Los_Angeles");

//		Calendar sr = calculator.getOfficialSunriseCalendarForDate(c);		// Get sunrise time
//		Calendar ss = calculator.getOfficialSunsetCalendarForDate(c);		// Get sunset time

		/* Adjust for sunset time */
		int cHour, cMin, cSec;
//		int srHour, srMin, srSec, ssHour, ssMin, ssSec;
//		int cDay, cMonth, cYear;

		cHour = c.getHour();
		cMin = c.getMinute();
		cSec = c.getSecond();

		float cTime = cHour * 60 + cMin + cSec/60.f;
		float time = PApplet.map(cTime, 0.f, 1439.f, 0.f, 1.f); // Time of day when photo was taken		

//		int daysInMonth = 0, daysCount = 0;

//		for (int i = 1; i < cMonth; i++) 				// Find number of days in prior months
//		{
//			daysInMonth = getDaysInMonth(i, cYear);		// Get days in month
//			daysCount += daysInMonth;
//		}

		//		int startYear = 2013;							
		//		int date = (year - startYear) * 365 + daysCount + day; 		
//		float date = daysCount + cDay; 						// Days since Jan. 1st							//	 NOTE:	need to account for leap years!		

		//		int endDate = 5000;																					
//		date = PApplet.constrain(PApplet.map(date, 0.f, 365, 0.f, 1.f), 0.f, 1.f);					//	 NOTE:	need to account for leap years!		

		return time;				// Date between 0.f and 1.f, time between 0. and 1., dayLength in minutes
	}
	

	/**
	 * @return Day length associated with this time (unused)
	 */
//	public float getDayLength()
//	{
//		return dayLength;
//	}
}
