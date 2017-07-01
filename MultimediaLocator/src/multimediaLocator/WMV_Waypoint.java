package multimediaLocator;
import processing.core.PVector;

/*****************************
 *  Waypoint in a 3D virtual path 
 *  @author davidgordon
 */
public class WMV_Waypoint 
{
	private WMV_Time time;
	private WMV_Orientation orientation;

	private int id;							// ID (Cluster)
	private PVector captureLocation;		// World location
	private PVector gpsLocation;			// GPS location {longitude, latitude}
	private PVector gpsLocationWithAltitude; // GPS location with altitude {longitude, altitude, latitude}
	private float altitude;					// GPS Altitude
	
	public String longitudeRef = "E";
	public String latitudeRef = "N";
	
	private boolean initialized = false;	// Whether waypoint has been initialized
	
	/**
	 * Constructor for waypoint without longitude / latitude ref
	 * @param newID Waypoint ID
	 * @param newCaptureLocation Capture location
	 * @param newGPSLocation GPS location with signed longitude and latitude
	 * @param newAltitude Altitude (in m.)
	 * @param newTime Time
	 */
	public WMV_Waypoint(int newID, PVector newCaptureLocation, PVector newGPSLocation, float newAltitude, WMV_Time newTime) 
	{
		id = newID;

		/* Location */
		captureLocation = newCaptureLocation;
		gpsLocation = newGPSLocation;
		altitude = newAltitude;
		gpsLocationWithAltitude = new PVector(gpsLocation.x, altitude, gpsLocation.y);
		if( (int)Math.signum(gpsLocation.x) == -1 )
			longitudeRef = "W";
		if( (int)Math.signum(gpsLocation.y) == -1 )
			latitudeRef = "S";

		/* Time */
		time = newTime;						

		initialized = true;
	}

	
	/**
	 * Constructor for waypoint without longitude / latitude ref
	 * @param newID Waypoint ID
	 * @param newCaptureLocation Capture location
	 * @param newGPSLocation GPS location (longitude and latitude with no sign)
	 * @param newAltitude Altitude (in m.)
	 * @param newLongitudeRef Longitude reference
	 * @param newLatitudeRef Latitude reference
	 * @param newTime Time
	 */
	public WMV_Waypoint( int newID, PVector newCaptureLocation, PVector newGPSLocation, float newAltitude, String newLongitudeRef, 
						 String newLatitudeRef, WMV_Time newTime ) 
	{
		id = newID;

		/* Location */
		captureLocation = newCaptureLocation;
		gpsLocation = newGPSLocation;
		altitude = newAltitude;
		gpsLocationWithAltitude = new PVector(gpsLocation.x, altitude, gpsLocation.y);
		longitudeRef = newLongitudeRef;
		latitudeRef = newLatitudeRef;

		/* Time */
		time = newTime;						

		initialized = true;
	}

	/**
	 * Dummy constructor for waypoint
	 */
	public WMV_Waypoint(){}
	
	/**
	 * Initialize waypoint
	 * @param newID
	 * @param newCaptureLocation
	 * @param newGPSLocation
	 * @param newAltitude
	 * @param newTime
	 */
	public void initialize(int newID, PVector newCaptureLocation, PVector newGPSLocation, float newAltitude, WMV_Time newTime) 
	{
		id = newID;
		captureLocation = newCaptureLocation;
		time = newTime;
		gpsLocation = newGPSLocation;
		altitude = newAltitude;
		
		initialized = true;
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
	
	public PVector getWorldLocation()
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
		return gpsLocationWithAltitude;
	}

	/**
	 * Set GPS location in format {longitude, latitude}
	 * @param newGPSLocation New GPS location
	 */
	public void setGPSLocation(PVector newGPSLocation)
	{
		gpsLocation = newGPSLocation;
	}
	
	public boolean initialized()
	{
		return initialized;
	}
}