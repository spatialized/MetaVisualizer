package main.java.com.entoptic.multimediaLocator.model;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Objects;

import processing.core.PVector;

/******************************************
 * Date a group of media files were captured
 * @author davidgordon
 */
public class WMV_Date implements Comparable<WMV_Date>
{
	private int year, month, day, days;				// Capture Year, Month, Day, # of Days since 1980
	private int id;
	public boolean timeInitialized = false;

	public ZonedDateTime dateTime;
	public String dateTimeString, timeZoneID;

	public WMV_Date(){}
	
	public void initialize(int newID, ZonedDateTime newDateTime, String newDateTimeString, String newTimeZoneID)
	{
		id = newID;
		timeZoneID = newTimeZoneID;

		dateTime = newDateTime;
		dateTimeString = newDateTimeString;

		initializeTime();

		year = dateTime.getYear();
		month = dateTime.getMonthValue();

		if(month == 0)
		{
			year --;
			month = 12;
		}

		day = dateTime.getDayOfMonth();
		calculateDaysSince1980();
	}

	public WMV_Date(WMV_Date newDate)
	{
		id = newDate.id;
		timeZoneID = newDate.timeZoneID;
		
		dateTimeString = newDate.dateTimeString;
		initializeTime();
		
		year = newDate.year;
		month = newDate.month;
		day = newDate.day;
		days = newDate.days;
	}

	public void setID(int newID)
	{
		id = newID;
	}

	public int getID()
	{
		return id;
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

	public int getDaysSince1980()
	{
		return days;
	}

	public PVector getDate()
	{
		return new PVector (month, day, year);
//		return date;
	}

	/** 
	 * Calculate date as # of days from 1980 
	 **/
	private void calculateDaysSince1980()
	{
		days = findDaysSince1980(timeZoneID, day, month, year);
	}

	/**
	 * Get specified date as number of days from Jan 1, 1980
	 * @param day Day
	 * @param month Month
	 * @param year Year
	 * @return Number of days 
	 */
	public int findDaysSince1980(String timeZoneID, int day, int month, int year)
	{
		ZonedDateTime date1980 = ZonedDateTime.parse("1980-01-01T00:00:00+00:00[America/Los_Angeles]");
		ZonedDateTime date = ZonedDateTime.of(year, month, day, 0, 0, 0, 0, ZoneId.of(timeZoneID));
		Duration duration = Duration.between(date1980, date);

		return (int)duration.toDays();
	}

	/**
	 * Get date as a string in format "January 1, 2000"
	 * @return Date string
	 */
	public String getFormattedDate()
	{
		int year = getYear();
		int month = getMonth();
		int day = getDay();
		String monthStr = "";

		switch(month)
		{
		case 1:
			monthStr = "January";
			break;
		case 2:
			monthStr = "February";
			break;
		case 3:
			monthStr = "March";
			break;
		case 4:
			monthStr = "April";
			break;
		case 5:
			monthStr = "May";
			break;
		case 6:
			monthStr = "June";
			break;
		case 7:
			monthStr = "July";
			break;
		case 8:
			monthStr = "August";
			break;
		case 9:
			monthStr = "September";
			break;
		case 10:
			monthStr = "October";
			break;
		case 11:
			monthStr = "November";
			break;
		case 12:
			monthStr = "December";
			break;
		}

		String result = monthStr+" "+String.valueOf(day)+", "+String.valueOf(year);
		return result;
	}

	public void initializeTime()
	{
		dateTime = parseDateTime(dateTimeString);
		timeInitialized = true;
	}

	/**
	 * Parse date/time string from metadata given media time zone
	 * @param input String to parse
	 * @return ZonedDateTime object corresponding to given string
	 */
	public ZonedDateTime parseDateTime(String input) 					// 2016:04:10 17:52:39
	{		
//		String[] parts = input.split("-");
//		input = parts[1];
//		System.out.println("   initializeTime() for date... input:"+input);
		String[] parts = input.split(":");

		int year = Integer.valueOf(parts[0].trim());
		int month = Integer.valueOf(parts[1]);
		int min = Integer.valueOf(parts[3]);
		int sec = Integer.valueOf(parts[4]);
		input = parts[2];
		parts = input.split(" ");
		int day = Integer.valueOf(parts[0]);
		int hour = Integer.valueOf(parts[1]);

		ZonedDateTime pac = ZonedDateTime.of(year, month, day, hour, min, sec, 0, ZoneId.of(timeZoneID));
		if(pac == null)
			System.out.println("ERROR in parseDateTime for date #"+getID()+"... pac == null!");
		return pac;
	}

	/**
	 * @param t Time segment to compare to
	 * Compare this time segment with given one
	 */
	public int compareTo(WMV_Date t)
	{
		return Float.compare(this.days, t.days);		
	}

	public static Comparator<WMV_Date> WMV_DayComparator = new Comparator<WMV_Date>() 
	{
		public int compare(WMV_Date t1, WMV_Date t2) 
		{

			int day1 = t1.getDay();
			int day2 = t2.getDay();

			return day1 - day2;
		}
	};

	public static Comparator<WMV_Date> WMV_MonthComparator = new Comparator<WMV_Date>() 
	{
		public int compare(WMV_Date t1, WMV_Date t2) 
		{

			int month1 = t1.getMonth();
			int month2 = t2.getMonth();

			month1 *= 1000000.f;
			month2 *= 1000000.f;

			return month1 - month2;
		}
	};

	public static Comparator<WMV_Date> WMV_YearComparator = new Comparator<WMV_Date>() 
	{
		public int compare(WMV_Date t1, WMV_Date t2) 
		{
			int year1 = t1.getYear();
			int year2 = t2.getYear();

			return year1 - year2;
		}
	};

	public static Comparator<WMV_Date> WMV_DateComparator = new Comparator<WMV_Date>() 
	{
		public int compare(WMV_Date t1, WMV_Date t2) 
		{
			int date1 = t1.getDaysSince1980();
			int date2 = t2.getDaysSince1980();

			return date1 - date2;
		}
	};

	@Override
	public boolean equals(Object o) {

		if (o == this) return true;
		if (!(o instanceof WMV_Date)) {
			return false;
		}
		WMV_Date dt = (WMV_Date) o;

		return days == dt.days &&
				Objects.equals(day, dt.day) &&
				Objects.equals(month, dt.month) &&
				Objects.equals(year, dt.year);
	}

	@Override
	public int hashCode() {
		return Objects.hash(days, day, month, year);
	}
}

