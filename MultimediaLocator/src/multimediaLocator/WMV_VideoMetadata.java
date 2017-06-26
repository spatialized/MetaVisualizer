package multimediaLocator;

import java.time.ZonedDateTime;

import processing.core.PVector;

/**
 * Video metadata
 * @author davidgordon
 *
 */
public class WMV_VideoMetadata extends WMV_MediaMetadata 
{
	public int cameraModel;                 	// Camera model
	public float brightness;					// Video brightness 	-- Unused
	public float theta = 0;                		// Compass direction (in Degrees N)

	public int videoWidth, videoHeight;			// Video width and height
	public float phi;			        		// Video elevation angle (Y-axis rotation in degrees N)
	public float orientation;              		// Video orientation  {0: Landscape, 90: Portrait, 180: Landscape [flipped], 270 Portrait [flipped]}
	public float rotation;				    	// Video rotation angle (Z-axis rotation in degrees)
	public float focusDistance; 	 			// Video viewing distance (rarely given in metadata)
	public float focalLength = 0; 				// Camera focal length (Zoom Level)
	public float sensorSize; 	 				// Video viewing distance (rarely given in metadata)

	/**
	 * Constructor for video metadata
	 */
	public WMV_VideoMetadata( String newName, String newFilePath, PVector newGPSLocation, ZonedDateTime newDateTime, String newDateTimeString, String newTimeZone, 
			float newTheta, float newFocalLength, float newOrientation, float newElevation, float newRotation, int newCameraModel, 
			int newWidth, int newHeight, float newBrightness, String[] newKeywords, String newSoftware )
	{
		super(newName, newFilePath, newGPSLocation, newDateTime, newDateTimeString, newTimeZone, newKeywords, newSoftware);

		videoWidth = newWidth;
		videoHeight = newHeight;
		orientation = newOrientation;
		focalLength = newFocalLength;

		theta = newTheta;
		phi = newElevation;
		rotation = newRotation;
		brightness = newBrightness;
		cameraModel = newCameraModel;
	}

	public WMV_VideoMetadata(){}
	
	/**
	 * 
	 * @param newName
	 * @param newFilePath
	 * @param newGPSLocation
	 * @param newDateTime
	 * @param newDateTimeString
	 * @param newTimeZone
	 * @param newTheta
	 * @param newFocalLength
	 * @param newOrientation
	 * @param newElevation
	 * @param newRotation
	 * @param newCameraModel
	 * @param newWidth
	 * @param newHeight
	 * @param newBrightness
	 * @param newKeywords
	 */
	public void initialize( String newName, String newFilePath, PVector newGPSLocation, ZonedDateTime newDateTime, String newDateTimeString, String newTimeZone, 
			float newTheta, float newFocalLength, float newOrientation, float newElevation, float newRotation, int newCameraModel, 
			int newWidth, int newHeight, float newBrightness, String[] newKeywords, String newSoftware )
	{
		super.init(newName, newFilePath, newGPSLocation, newDateTime, newDateTimeString, newTimeZone, newKeywords, newSoftware);

		videoWidth = newWidth;
		videoHeight = newHeight;
		orientation = newOrientation;
		focalLength = newFocalLength;

		theta = newTheta;
		phi = newElevation;
		rotation = newRotation;
		brightness = newBrightness;
		cameraModel = newCameraModel;
	}
	
	/**
	 * @return Whether metadata is valid
	 */
	public boolean isValid()
	{
		if(gpsLocation.x != 0.f && gpsLocation.z != 0.f && gpsLocation.z != 0.f)
			return true;
		else
			return false;
	}
}
