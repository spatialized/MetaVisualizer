package multimediaLocator;

import java.util.ArrayList;
import java.util.Comparator;

/*********************************************
 * @author davidgordon
 * A time span of media objects
 */
public class WMV_TimeSegment implements Comparable<WMV_TimeSegment>									
{
//	IntList images, panoramas, videos;		// Associated media
	
	private WMV_Time center;			// Center time 	 	  -- Mean or median??
	private WMV_Time lower, upper;		// Upper and lower bounds of cluster
	
	private int clusterID;			// Time segment ID and cluster ID
	private int clusterTimelineID;		// ID of segment on cluster single day timeline
	private int clusterTimelineIDOnDate;		// ID within cluster date-specific timelines
	private int clusterDateID;			// Cluster date-specific timeline for this segment
	private int fieldTimelineID;		// ID of segment on field single day timeline
	private int fieldDateID;			// Field date-specific timeline for this segment
	private int fieldTimelineIDOnDate;		// ID within field date-specific timelines
	
	ArrayList<WMV_Time> timeline;
	private boolean hasImage;
	private boolean hasPanorama;
	private boolean hasVideo;
	private boolean hasSound;
	
	WMV_TimeSegment( int newClusterID, int newClusterTimelineID, int newClusterTimelinesID, int newClusterDateID, 
					 int newFieldTimelineID, int newFieldTimelinesID, int newFieldDateID,
					 WMV_Time newCenter, WMV_Time newUpper, WMV_Time newLower, ArrayList<WMV_Time> newTimeline )
	{
//		id = newID;
		clusterID = newClusterID;
		
		clusterTimelineID = newClusterTimelineID;				
		clusterTimelineIDOnDate = newClusterTimelinesID;
		clusterDateID = newClusterDateID;						
		
		fieldTimelineID = newFieldTimelineID;				
		fieldTimelineIDOnDate = newFieldTimelinesID;
		fieldDateID = newFieldDateID;						
		
		center = newCenter;
		upper = newUpper;
		lower = newLower;
		
		timeline = newTimeline;
		analyzeMediaTypes();
	}

	/**
	 * Analyze media types in time segment
	 */
	private void analyzeMediaTypes()
	{
		hasImage = false;
		hasPanorama = false;
		hasVideo = false;
		hasSound = false;
		
		for( WMV_Time t : timeline )
		{
			if(!hasImage)
				if(t.getMediaType() == 0)
					hasImage = true;
			if(!hasPanorama)
				if(t.getMediaType() == 1)
					hasPanorama = true;
			if(!hasVideo)
				if(t.getMediaType() == 2)
					hasVideo = true;
			if(!hasSound)
				if(t.getMediaType() == 3)
					hasSound = true;
		}
	}
	
	/** 
	 * @param newID New time segment ID
	 */
//	public void setID(int newID)
//	{
//		id = newID;
//	}
	
	/** 
	 * @param newID New cluster ID
	 */
	public void setClusterID(int newClusterID)
	{
		clusterID = newClusterID;
	}

	/** 
	 * @param newID New cluster timeline ID
	 */
	public void setClusterTimelineID(int newClusterTimelineID)
	{
		clusterTimelineID = newClusterTimelineID;
	}

	/** 
	 * @param newID New cluster timelines ID
	 */
	public void setClusterTimelinesID(int newClusterTimelinesID)
	{
		clusterTimelineIDOnDate = newClusterTimelinesID;
	}

	/** 
	 * @param newID New cluster date ID
	 */
	public void setClusterDateID(int newClusterDateID)
	{
		clusterDateID = newClusterDateID;
	}

	/** 
	 * @param newID New field timeline ID
	 */
	public void setFieldTimelineID(int newFieldTimelineID)
	{
		fieldTimelineID = newFieldTimelineID;
	}

	/** 
	 * @param newID New field timelines ID
	 */
	public void setFieldTimelinesID(int newFieldTimelinesID)
	{
		fieldTimelineIDOnDate = newFieldTimelinesID;
	}

	/** 
	 * @param newID New field date ID
	 */
	public void setFieldDateID(int newFieldDateID)
	{
		fieldDateID = newFieldDateID;
	}

	/** 
	 * @param newUpper New upper bound
	 */
	public void setUpper(WMV_Time newUpper)
	{
		upper = newUpper;
	}

	/** 
	 * @param newUpper New upper bound
	 */
	public void setCenter(WMV_Time newCenter)
	{
		center = newCenter;
	}
	
	/** 
	 * @param newLower New lower bound
	 */
	public void setLower(WMV_Time newLower)
	{
		lower = newLower;
	}

	/** 
	 * @return Time segment ID
	 */
	public ArrayList<WMV_Time> getTimeline()
	{
		return timeline;
	}
	
	/** 
	 * @return Cluster ID
	 */
	public int getClusterID()
	{
		return clusterID;
	}

	public int getClusterTimelineID()
	{
		return clusterTimelineID;
	}

	public int getClusterDateID()
	{
		return clusterDateID;
	}
	
	public int getClusterTimelineIDOnDate()
	{
		return clusterTimelineIDOnDate;
	}

	public int getFieldTimelineID()
	{
		return fieldTimelineID;
	}

	public int getFieldDateID()
	{
		return fieldDateID;
	}
	
	public int getFieldTimelineIDOnDate()
	{
		return fieldTimelineIDOnDate;
	}

	public boolean hasImage()
	{
		return hasImage;
	}

	public boolean hasPanorama()
	{
		return hasPanorama;
	}

	public boolean hasVideo()
	{
		return hasVideo;
	}

	public boolean hasSound()
	{
		return hasSound;
	}

	/** 
	 * @return Center time 
	 */
	public WMV_Time getCenter()
	{
		return center;
	}
	/** 
	 * @return Upper bound
	 */
	public WMV_Time getUpper()
	{
		return upper;
	}
	
	/** 
	 * getLower()
	 * @return Lower bound
	 */
	public WMV_Time getLower()
	{
		return lower;
	}
	
//	/**
//	 * @return Date associated with this time				// -- Need to implement for cases crossing between two dates
//	 */
//	public WMV_Date getDate()
//	{
//		WMV_Date date = new WMV_Date(p, -1, dateTime);
//		return date;
//	}
	
	public String getTimespanAsString( boolean showSeconds, boolean military )
	{
		int lowerHour = getLower().getHour();
		int lowerMinute = getLower().getMinute();
		int lowerSecond = getLower().getSecond();
		int upperHour = getUpper().getHour();
		int upperMinute = getUpper().getMinute();
		int upperSecond = getUpper().getSecond();
		String strLower = "", strUpper = "";

		if((lowerHour <= 11 && upperHour <= 11) || (lowerHour >= 12 && upperHour >= 12))	// Either both am or both pm
		{
			strLower = getTimeAsString(lowerHour, lowerMinute, lowerSecond, showSeconds, military, false);
			strUpper = getTimeAsString(upperHour, upperMinute, upperSecond, showSeconds, military, true);
		}
		else																// Spans am to pm boundary
		{
			strLower = getTimeAsString(lowerHour, lowerMinute, lowerSecond, showSeconds, military, true);
			strUpper = getTimeAsString(upperHour, upperMinute, upperSecond, showSeconds, military, true);
		}
		
//		strLower = getTimeAsString(lowerHour, lowerMinute, lowerSecond, showSeconds, military);
//		strUpper = getTimeAsString(upperHour, upperMinute, upperSecond, showSeconds, military);

		if(lowerHour == upperHour && lowerMinute == upperMinute && !showSeconds)
			return strLower;
		else 
			return strLower + "-" + strUpper;
	}
	
	private String getTimeAsString( int hour, int minute, int second, boolean showSeconds, boolean military, boolean showAmPm )
	{
		boolean pm = false;
		
		if(!military) 
		{
			if(hour == 0) hour = 12;
			
			if(hour > 12)
			{
				hour -= 12;
				if(hour < 12) pm = true;
			}
			else if(hour == 12)
				pm = true;
		}

		String strHour = String.valueOf(hour);
		
		String strMinute = String.valueOf(minute);
		if(minute < 10) strMinute = "0"+strMinute;
		
		if(showSeconds)
		{
			String strSecond = String.valueOf(second);
			if(second < 10) strSecond = "0"+strSecond;
			
			if(military)
				return strHour + ":" + strMinute + ":" + strSecond;
			else
			{
				if(showAmPm)
					return strHour + ":" + strMinute + ":" + strSecond + (pm ? " pm" : " am");
				else
					return strHour + ":" + strMinute + ":" + strSecond;
			}
		}
		else				
		{
			if(military)
				return strHour + ":" + strMinute;
			else
			{
				if(showAmPm)
					return strHour + ":" + strMinute + (pm ? " pm" : " am");
				else
					return strHour + ":" + strMinute;
			}
		}
	}

	/**
	 * compareTo()
	 * @param t Time segment to compare to
	 * Compare this time segment with given one
	 */
	public int compareTo(WMV_TimeSegment t)
	{
		return Float.compare(this.center.getTime(), t.center.getTime());		
	}

	public static Comparator<WMV_TimeSegment> WMV_TimeMidpointComparator = new Comparator<WMV_TimeSegment>() 
	{
		public int compare(WMV_TimeSegment t1, WMV_TimeSegment t2) 
		{

			float time1 = t1.center.getTime();
			float time2 = t2.center.getTime();

			time1 *= 1000000.f;
			time2 *= 1000000.f;
			
			return (int)(time1 - time2);
		}
	};
	
	public static Comparator<WMV_TimeSegment> WMV_TimeLowerBoundComparator = new Comparator<WMV_TimeSegment>() 
	{
		public int compare(WMV_TimeSegment t1, WMV_TimeSegment t2) 
		{

			float lower1 = t1.lower.getTime();
			float lower2 = t2.lower.getTime();

			lower1 *= 1000000.f;
			lower2 *= 1000000.f;
			
			return (int)(lower1 - lower2);
		}
	};

	public static Comparator<WMV_TimeSegment> WMV_TimeUpperBoundComparator = new Comparator<WMV_TimeSegment>() 
	{
		public int compare(WMV_TimeSegment t1, WMV_TimeSegment t2) 
		{

			float upper1 = t1.upper.getTime();
			float upper2 = t2.upper.getTime();

			upper1 *= 1000000.f;
			upper2 *= 1000000.f;
			
			return (int)(upper1 - upper2);
		}
	};
}