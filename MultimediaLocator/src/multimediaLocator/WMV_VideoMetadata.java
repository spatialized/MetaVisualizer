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
	public float brightness;
	public float theta = 0;                		// Media Orientation (in Degrees N)

	public int videoWidth, videoHeight;			// Image width and height
	public float phi;			        		// Image elevation angle (Y-axis rotation in degrees N)
	public float orientation;              		// Image orientation (Landscape = 0, Portrait = 90, Upside Down Landscape = 180, Upside Down Portrait = 270)
	public float rotation;				    	// Image rotation angle (Z-axis rotation in degrees)
	public float focusDistance; 	 			// Image viewing distance (rarely given in metadata)
	public float focalLength = 0; 				// Camera focal length (Zoom Level)
	public float sensorSize; 	 			// Image viewing distance (rarely given in metadata)

	public WMV_VideoMetadata( String newName, String newFilePath, PVector newGPSLocation, ZonedDateTime newDateTime, String newTimeZone, 
			float newTheta, float newFocalLength, float newOrientation, float newElevation, float newRotation, int newCameraModel, 
			int newWidth, int newHeight, float newBrightness, String[] newKeywords )
	{
		super(newName, newFilePath, newGPSLocation, newDateTime, newTimeZone, newKeywords);

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
	
	public void initialize( String newName, String newFilePath, PVector newGPSLocation, ZonedDateTime newDateTime, String newTimeZone, 
			float newTheta, float newFocalLength, float newOrientation, float newElevation, float newRotation, int newCameraModel, 
			int newWidth, int newHeight, float newBrightness, String[] newKeywords )
	{
		super.init(newName, newFilePath, newGPSLocation, newDateTime, newTimeZone, newKeywords);

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
}
