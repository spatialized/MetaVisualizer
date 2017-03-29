package multimediaLocator;
import processing.core.PVector;

/*****************************
 *  Describes a waypoint in a user defined (camera) navigation path
 */

public class WMV_Waypoint {
	private int id;					// ID (Cluster)
	private PVector location;		// Camera location
	private PVector target;			// Where camera is pointing
	private WMV_Time time;
	
	WMV_Waypoint(int newID, PVector newLocation, WMV_Time newTime) 
	{
		id = newID;
		location = newLocation;
		time = newTime;
	}
	
	/**
	 * @param cPoint Waypoint to compare
	 * @return Distance between this and comparison point
	 */
	public float getDistance(WMV_Waypoint cPoint)
	{
		float dist = PVector.dist(location, cPoint.location);
		return dist;
	}
	
	public int getID()
	{
		return id;
	}
	
	public void setID(int newID)
	{
		id = newID;
	}

	public void setTarget(PVector newTarget)
	{
		target = newTarget;
	}
	
	public PVector getLocation()
	{
		return location;
	}
	
	public PVector getTarget()
	{
		return target;
	}

	public WMV_Time getTime()
	{
		return time;
	}
}  