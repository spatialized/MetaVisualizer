package main.java.com.entoptic.multimediaLocator;
import processing.core.PVector;

/*****************************
 *  Waypoint in a 3D virtual path 
 *  @author davidgordon
 */
public class WMV_Waypoint 
{
	private WMV_Time time;					// Date/time matching time zone of field
	private WMV_Orientation orientation;	// Waypoint orientation		-- In progress

	private int id;
	private int clusterID;					// Cluster ID 
	private int pathID = -1;				// Path ID (GPS Track or Memory) 
	
	private PVector captureLocation;		// World location
	private PVector gpsLocation;			// GPS location {longitude, latitude}
	private PVector gpsLocationWithAltitude; // GPS location with altitude {longitude, altitude, latitude}
	private float altitude;					// GPS Altitude
	
	public String longitudeRef = "E";
	public String latitudeRef = "N";
	
	private boolean initialized = false;	// Whether waypoint has been initialized
	
	/**
	 * Constructor for waypoint without longitude / latitude ref
	 * @param newClusterID Associated cluster ID
	 * @param newPathID Associated GPS track or memory path ID
	 * @param newCaptureLocation Capture location
	 * @param newGPSLocation GPS location with signed longitude and latitude
	 * @param newAltitude Altitude (in m.)
	 * @param newTime Time
	 */
	public WMV_Waypoint(int newID, int newClusterID, int newPathID, PVector newCaptureLocation, PVector newGPSLocation, float newAltitude, WMV_Time newTime) 
	{
		id = newID;
		clusterID = newClusterID;
		pathID = newPathID;

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
		clusterID = newID;

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
		clusterID = newID;
		captureLocation = newCaptureLocation;
		time = newTime;
		gpsLocation = newGPSLocation;
		altitude = newAltitude;
		
		initialized = true;
	}

	/**
	 * @param cPoint Waypoint to compare
	 * @return Distance between this and comparison waypoint
	 */
	public float getDistance(WMV_Waypoint cPoint)
	{
		float dist = PVector.dist(captureLocation, cPoint.captureLocation);
		return dist;
	}

	/**
	 * @param cPoint World location to compare
	 * @return Distance between this and comparison point
	 */
	public float getDistanceFromPoint(PVector cPoint)
	{
		float dist = PVector.dist(captureLocation, cPoint);
		return dist;
	}
	
	/**
	 * Recalculate capture location (e.g. if moved GPS track to new field)
	 * @param utilities Utilities class
	 * @param model Field model
	 */
	public void recalculateCaptureLocation(WMV_Utilities utilities, WMV_Model model)
	{
		captureLocation = utilities.getCaptureLocationFromGPSAndAltitude(gpsLocation, altitude, longitudeRef, latitudeRef, model);
	}
	
	public int getID()
	{
		return id;
	}
	
	public void setID(int newID)
	{
		id = newID;
	}
	
	public int getClusterID()
	{
		return clusterID;
	}
	
	public void setClusterID(int newID)
	{
		clusterID = newID;
	}
	
	public int getGPSTrackID()
	{
		return pathID;
	}
	
	public void setGPSTrackID(int newID)
	{
		pathID = newID;
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