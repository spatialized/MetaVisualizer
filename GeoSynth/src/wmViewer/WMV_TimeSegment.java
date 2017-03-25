package wmViewer;

import java.util.ArrayList;
import java.util.Comparator;

import processing.core.PApplet;

/*********************************************
 * @author davidgordon
 * A time span of media objects
 */
public class WMV_TimeSegment implements Comparable<WMV_TimeSegment>									
{
//	IntList images, panoramas, videos;		// Associated media
	
	private WMV_Time center;			// Center time 	 	  -- Mean or median??
	private WMV_Time lower, upper;		// Upper and lower bounds of cluster
	
//	private int id, clusterID;			// Time segment ID and cluster ID
	private int clusterID;			// Time segment ID and cluster ID
	private int clusterTimelineID;		// ID of segment on cluster single day timeline
	private int clusterTimelinesID;		// ID within cluster date-specific timelines
	private int clusterDateID;			// Cluster date-specific timeline for this segment
	private int fieldTimelineID;		// ID of segment on field single day timeline
	private int fieldTimelinesID;		// ID within field date-specific timelines
	private int fieldDateID;			// Field date-specific timeline for this segment
	
	ArrayList<WMV_Time> timeline;
	
	WMV_TimeSegment( int newClusterID, int newClusterTimelineID, int newClusterTimelinesID, int newClusterDateID, 
					 int newFieldTimelineID, int newFieldTimelinesID, int newFieldDateID,
					 WMV_Time newCenter, WMV_Time newUpper, WMV_Time newLower, ArrayList<WMV_Time> newTimeline )
	{
//		id = newID;
		clusterID = newClusterID;
		
		clusterTimelineID = newClusterTimelineID;				
		clusterTimelinesID = newClusterTimelinesID;
		clusterDateID = newClusterDateID;						
		
		fieldTimelineID = newFieldTimelineID;				
		fieldTimelinesID = newFieldTimelinesID;
		fieldDateID = newFieldDateID;						
		
		center = newCenter;
		upper = newUpper;
		lower = newLower;
		
		timeline = newTimeline;
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
		clusterTimelinesID = newClusterTimelinesID;
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
		fieldTimelinesID = newFieldTimelinesID;
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

	public int getClusterTimelinesID()
	{
		return clusterTimelinesID;
	}

	public int getClusterDateID()
	{
		return clusterDateID;
	}

	public int getFieldTimelineID()
	{
		return fieldTimelineID;
	}

	public int getFieldTimelinesID()
	{
		return fieldTimelinesID;
	}

	public int getFieldDateID()
	{
		return fieldDateID;
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