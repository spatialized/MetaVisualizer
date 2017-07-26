package main.java.com.entoptic.multimediaLocator.metadata;

import java.time.ZonedDateTime;

import processing.core.PVector;

/**
 * Metadata of a rectangular image
 * @author davidgordon
 */
public class WMV_ImageMetadata extends WMV_MediaMetadata
{
	public int cameraModel;                 	// Camera model
	public float brightness;					// Image brightness
	public float theta = 0;                	// Compass direction (in Degrees N)

	public int imageWidth, imageHeight;		// Image width and height
	public float phi;			        		// Image elevation angle (Y-axis rotation in degrees N)
	public float orientation;              	// Image orientation  {0: Landscape, 90: Portrait, 180: Landscape [flipped], 270 Portrait [flipped]}
	public float rotation;				    	// Image rotation angle (Z-axis rotation in degrees)
	public float focusDistance; 	 			// Image focus (viewing) distance 	-- Absent in fixed focal length lenses, i.e. iPhones
	public float focalLength = 0; 			// Camera focal length (Zoom Level)
	public float sensorSize;					// Approx. size of sensor in mm.
	
	/**
	 * Constructor for image metadata
	 * @param newName Image name
	 * @param newFilePath Image file path
	 * @param newGPSLocation Image GPS location 
	 * @param newDateTime Image zoned date/time object
	 * @param newDateTimeString Image date/time metadata string
	 * @param newTimeZone Image time zone ID
	 * @param newTheta Image theta (compass direction)
	 * @param newFocalLength Camera focal length
	 * @param newOrientation Image orientation (0: horizontal, 90: vertical, 180: horizontal flipped, 270: vertical flipped)
	 * @param newElevation Image elevation
	 * @param newRotation Image rotation
	 * @param newFocusDistance Image focus distance
	 * @param newSensorSize Camera sensor size
	 * @param newCameraModel Camera model
	 * @param newWidth Image width
	 * @param newHeight Image height
	 * @param newBrightness Image brightness
	 * @param newKeywords Image keyword array
	 * @param newLongitudeRef Longitude Reference {E or W}
	 * @param newLatitudeRef Latitude reference {N or S}
	 */
	WMV_ImageMetadata( String newName, String newFilePath, PVector newGPSLocation, ZonedDateTime newDateTime, String newDateTimeString, String newTimeZone,
					   float newTheta, float newFocalLength, float newOrientation, float newElevation, float newRotation, float newFocusDistance, 
					   float newSensorSize, int newCameraModel, int newWidth, int newHeight, float newBrightness, String[] newKeywords, String newSoftware,
					   String newLongitudeRef, String newLatitudeRef )
	{
		super( newName, newFilePath, newGPSLocation, newDateTime, newDateTimeString, newTimeZone, newKeywords, newSoftware, newLongitudeRef, 
			   newLatitudeRef );
		
		imageWidth = newWidth;
		imageHeight = newHeight;
		orientation = newOrientation;
		focalLength = newFocalLength;
		focusDistance = newFocusDistance;
		sensorSize = newSensorSize;

		theta = newTheta;
		phi = newElevation;
		rotation = newRotation;
		brightness = newBrightness;
		cameraModel = newCameraModel;
	}
	
	/**
	 * Dummy constructor for image metadata
	 */
	WMV_ImageMetadata(){}

	/**
	 * Initialize image metadata object
	 * @param newName Image name
	 * @param newFilePath Image file path
	 * @param newGPSLocation Image GPS location 
	 * @param newDateTime Image zoned date/time object
	 * @param newDateTimeString Image date/time metadata string
	 * @param newTimeZone Image time zone ID
	 * @param newTheta Image theta (compass direction)
	 * @param newFocalLength Camera focal length
	 * @param newOrientation Image orientation (0: horizontal, 90: vertical, 180: horizontal flipped, 270: vertical flipped)
	 * @param newElevation Image elevation
	 * @param newRotation Image rotation
	 * @param newFocusDistance Image focus distance
	 * @param newSensorSize Camera sensor size
	 * @param newCameraModel Camera model
	 * @param newWidth Image width
	 * @param newHeight Image height
	 * @param newBrightness Image brightness
	 * @param newKeywords Image keyword array
	 */
	public void initialize( String newName, String newFilePath, PVector newGPSLocation, ZonedDateTime newDateTime, String newDateTimeString, String newTimeZone,
			float newTheta, float newFocalLength, float newOrientation, float newElevation, float newRotation, float newFocusDistance, 
			float newSensorSize, int newCameraModel, int newWidth, int newHeight, float newBrightness, String[] newKeywords, String newSoftware,
			String newLongitudeRef, String newLatitudeRef )
	{
		super.init( newName, newFilePath, newGPSLocation, newDateTime, newDateTimeString, newTimeZone, newKeywords, newSoftware, newLongitudeRef,
				    newLatitudeRef );
		
		imageWidth = newWidth;
		imageHeight = newHeight;
		orientation = newOrientation;
		focalLength = newFocalLength;
		focusDistance = newFocusDistance;
		sensorSize = newSensorSize;

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
