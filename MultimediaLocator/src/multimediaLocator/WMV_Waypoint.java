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

	private int id;							// ID (Cluster)
	private PVector captureLocation;		// World location
	private PVector gpsLocation;			// GPS location {longitude, latitude}
	private float altitude;					// GPS Altitude
	
	WMV_Waypoint(int newID, PVector newCaptureLocation, PVector newGPSLocation, float newAltitude, WMV_Time newTime) 
	{
		id = newID;
		captureLocation = newCaptureLocation;
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
		float dist = PVector.dist(captureLocation, cPoint.captureLocation);
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
	
	public void setCaptureLocation(PVector newLocation)
	{
		captureLocation = newLocation;
	}
	
	public PVector getCaptureLocation()
	{
		return captureLocation;
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
	 * Get GPS location in format: {longitude, altitude, latitude}
	 * @return GPS Location
	 */
	public PVector getGPSLocationWithAltitude()
	{
		return new PVector(gpsLocation.x, altitude, gpsLocation.y);
	}

	/**
	 * Set GPS location in format {longitude, latitude}
	 * @param newGPSLocation New GPS location
	 */
	public void setGPSLocation(PVector newGPSLocation)
	{
		gpsLocation = newGPSLocation;
	}
}  