package gmViewer;
import processing.core.PVector;

/*********************
 *  GMV_CameraWaypoint
 *  
 *  Describes a waypoint in a user defined (camera) navigation path
 */

public class GMV_Waypoint {
	int id;					// ID (Cluster)
	PVector location;		// Camera location
	PVector target;			// Where camera is pointing
	
	GMV_Waypoint(int newID, PVector newLocation) 
	{
		id = newID;
		location = newLocation;
	}
	
	/**
	 * getDistance()
	 * @param cPoint Waypoint to compare
	 * @return Distance between this and comparison point
	 */
	public float getDistance(GMV_Waypoint cPoint)
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
}  