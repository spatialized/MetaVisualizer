package multimediaLocator;

/**
 * Rectangular image metadata
 * @author davidgordon
 *
 */
public class WMV_ImageMetadata 
{
	public int cameraModel;                 	// Camera model
	public float brightness;
	public float theta = 0;                		// Media Orientation (in Degrees N)

	public int imageWidth, imageHeight;			// Image width and height
	public float phi;			        		// Image elevation angle (Y-axis rotation in degrees N)
	public float orientation;              		// Image orientation (Landscape = 0, Portrait = 90, Upside Down Landscape = 180, Upside Down Portrait = 270)
	public float rotation;				    	// Image rotation angle (Z-axis rotation in degrees)
	public float focusDistance; 	 			// Image viewing distance (rarely given in metadata)
	public float focalLength = 0; 				// Camera focal length (Zoom Level)
	public float sensorSize;					// Approx. size of sensor in mm.

	public String[] keywords;
	
	WMV_ImageMetadata( float newTheta, float newFocalLength, float newOrientation, float newElevation, float newRotation, 
			float newFocusDistance, float newSensorSize, int newCameraModel, int newWidth, int newHeight, float newBrightness, 
			String[] newKeywords )
	{
		imageWidth = newWidth;
		imageHeight = newHeight;
		orientation = newOrientation;
		focalLength = newFocalLength;
		focusDistance = newFocusDistance;
		sensorSize = newSensorSize;
		keywords = newKeywords;

		theta = newTheta;
		phi = newElevation;
		rotation = newRotation;
		brightness = newBrightness;
		cameraModel = newCameraModel;
	}
}
