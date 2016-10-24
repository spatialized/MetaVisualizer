package gmViewer;

import java.util.Calendar;

import processing.core.PApplet;
import processing.core.PVector;

/******************************************
 * GMV_TimePoint
 * @author davidgordon
 * When a media file was captured
 */
public class GMV_Time 
{
	private int year, month, day, hour, minute, second, millisecond;
	private float dayLength;
	private float date, time;
	
	private GMV_World p;
	
	GMV_Time(GMV_World parent, Calendar newCalendar)
	{
		p = parent;

		year = newCalendar.get(Calendar.YEAR);
		month = newCalendar.get(Calendar.MONTH);
		day = newCalendar.get(Calendar.DAY_OF_MONTH);
		hour = newCalendar.get(Calendar.HOUR_OF_DAY);
		minute = newCalendar.get(Calendar.MINUTE);
		second = newCalendar.get(Calendar.SECOND);
//		millisecond =;
		
		PVector simulationTime = p.p.utilities.getSimulationTime( newCalendar ); 
		date = simulationTime.x;
		time = simulationTime.y;
//		dayLength = simulationTime.z;
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
	
	public float getDate()
	{
		return date;
	}

	public float getTime()
	{
		return time;
	}
	
	public float getDayLength()
	{
		return dayLength;
	}
}

