package wmViewer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Objects;

import processing.core.PApplet;
import processing.core.PVector;

/******************************************
 * @author davidgordon
 * Date when a group of media files were captured and associated timeline
 */
public class WMV_Date implements Comparable<WMV_Date>
{
	private int year, month, day, days;				// Capture Year, Month, Day, # of Days since 1980
	private PVector date;							// (Day, Month, Year) format
	private WMV_World p;
	private int id;
	
	public WMV_Date(WMV_World parent, int newID, Calendar newCalendar)
	{
		p = parent;
		id = newID;

		year = newCalendar.get(Calendar.YEAR);
		month = newCalendar.get(Calendar.MONTH);
		day = newCalendar.get(Calendar.DAY_OF_MONTH);
		date = new PVector (month, day, year);
		
		calculateDaysSince1980();
	}

	public WMV_Date(WMV_Date newDate)
	{
		p = newDate.p;
		id = newDate.id;

		year = newDate.year;
		month = newDate.month;
		day = newDate.day;
		date = newDate.date;
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
		return date;
	}
	
	/** 
	 * Calculate date as # of days from 1980 
	 **/
	private void calculateDaysSince1980()
	{
		int daysInMonth = 0, daysCount = 0;
		for (int i = 1; i < month; i++) 				// Find number of days in prior months
		{
			daysInMonth = p.p.utilities.getDaysInMonth(i, year);		// Get days in month
			daysCount += daysInMonth;
		}

		int startYear = 1980;							
		days = (year - startYear) * 365 + daysCount + day; 		
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
					
//			PVector date1 = t1.getDate();
//			PVector date2 = t2.getDate();
					
//			if(date1.z - date2.z != 0)
//				return (int)(date1.z - date2.z);		// Compare years
//			else if(date1.y - date2.y != 0)
//				return (int)(date1.y - date2.y);		// Compare months
//			return (int)(date1.x - date2.x);			// Compare days
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
	    
//	@Override
//    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result
//                + ((importantField == null) ? 0 : importantField.hashCode());
//        return result;
//    }
//
//    @Override
//    public boolean equals(final Object obj) {
//        if (this == obj)
//            return true;
//        if (obj == null)
//            return false;
//        if (getClass() != obj.getClass())
//            return false;
//        final MyClass other = (MyClass) obj;
//        if (importantField == null) {
//            if (other.importantField != null)
//                return false;
//        } else if (!importantField.equals(other.importantField))
//            return false;
//        return true;
//    }
}

