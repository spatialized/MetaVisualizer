package gmViewer;

import java.util.Comparator;

import processing.core.PApplet;

/*********************************************
 * GMV_TimeSegment
 * @author davidgordon
 * Represents a media object or cluster time span
 */
public class GMV_TimeSegment implements Comparable<GMV_TimeSegment>									
{
	private float center;			// Time 
	private float lower, upper;	// Upper and lower bounds of cluster
	private int id = -1;		// Cluster ID
	
	GMV_TimeSegment(int newID, float newTime, float newUpper, float newLower)
	{
		id = newID;
		center = newTime;
		upper = newUpper;
		lower = newLower;
		if((lower!=0&&upper!=0)&&(center > upper || center < lower))
		{
			PApplet.println("ERROR: id:"+id+" lower:"+lower+" center:"+center+" upper:"+upper);
		}
	}
	
	/** 
	 * setID()
	 * @param newID New time cluster ID
	 */
	public void setID(int newID)
	{
		id = newID;
	}

	/** 
	 * setUpper()
	 * @param newUpper New upper bound
	 */
	public void setUpper(float newUpper)
	{
		upper = newUpper;
	}

	/** 
	 * setCenter()
	 * @param newUpper New upper bound
	 */
	public void setCenter(float newCenter)
	{
		center = newCenter;
	}
	
	/** 
	 * setLower()
	 * @param newLower New lower bound
	 */
	public void setLower(float newLower)
	{
		lower = newLower;
	}
	
	/** 
	 * @return Center time 
	 */
	public float getCenter()
	{
		return center;
	}
	
	/** 
	 * getID()
	 * @return Time segment ID
	 */
	public int getID()
	{
		return id;
	}
	
	/** 
	 * getUpper()
	 * @return Upper bound
	 */
	public float getUpper()
	{
		return upper;
	}
	
	/** 
	 * getLower()
	 * @return Lower bound
	 */
	public float getLower()
	{
		return lower;
	}

	/**
	 * compareTo()
	 * @param t Time segment to compare to
	 * Compare this time segment with given one
	 */
	public int compareTo(GMV_TimeSegment t)
	{
		return Float.compare(this.center, t.center);		
	}

	public static Comparator<GMV_TimeSegment> GMV_TimeMidpointComparator = new Comparator<GMV_TimeSegment>() 
	{
		public int compare(GMV_TimeSegment t1, GMV_TimeSegment t2) 
		{

			float time1 = t1.getCenter();
			float time2 = t2.getCenter();

			time1 *= 1000000.f;
			time2 *= 1000000.f;
			
			return (int)(time1 - time2);
		}
	};
	

	public static Comparator<GMV_TimeSegment> GMV_TimeLowerBoundComparator = new Comparator<GMV_TimeSegment>() 
	{
		public int compare(GMV_TimeSegment t1, GMV_TimeSegment t2) 
		{

			float lower1 = t1.getLower();
			float lower2 = t2.getLower();

			lower1 *= 1000000.f;
			lower2 *= 1000000.f;
			
			return (int)(lower1 - lower2);
		}
	};
}