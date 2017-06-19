package multimediaLocator;
import processing.core.PVector;

/*****************************
 *  Waypoint in virtual path navigable by viewer
 *  @author davidgordon
 */

public class WMV_Waypoint 
{
	private WMV_Time time;
	private WMV_Orientation orientation;

	private int id;					// ID (Cluster)
	private PVector location;		// World location
	private PVector gpsLocation;	// GPS location
	private float altitude;		// GPS Altitude
//	private PVector target;			// Where camera is pointing
//	private boolean gps;
	
	WMV_Waypoint(int newID, PVector newLocation, PVector newGPSLocation, float newAltitude, WMV_Time newTime) 
	{
		id = newID;
		location = newLocation;
		time = newTime;
		gpsLocation = newGPSLocation;
		altitude = newAltitude;
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

	public void setOrientation(WMV_Orientation newOrientation)
	{
		orientation = newOrientation;
	}

	public WMV_Orientation getOrientation()
	{
		return orientation;
	}
	
	public PVector getLocation()
	{
		return location;
	}

	public WMV_Time getTime()
	{
		return time;
	}
	
	public float getAltitude()
	{
		return altitude;
	}
	
	public void setAltitude(float newAltitude)
	{
		altitude = newAltitude;
	}
	
	/**
	 * Get GPS location in format: {longitude, latitude}
	 * @return GPS Location
	 */
	public PVector getGPSLocation()
	{
		return gpsLocation;
	}

	/**
	 * Set GPS location in format [longitude, latitude]
	 * @param newGPSLocation New GPS location
	 */
	public void setGPSLocation(PVector newGPSLocation)
	{
		gpsLocation = newGPSLocation;
	}
}  