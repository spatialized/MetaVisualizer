package multimediaLocator;

import java.time.ZonedDateTime;

import processing.core.PVector;

/******************************************
 * Metadata for a 360-degree panorama
 * @author davidgordon
 *
 */
public class WMV_PanoramaMetadata extends WMV_MediaMetadata
{
	public int cameraModel;                 	// Camera model
	public float brightness;					// Panorama pixel brightness
	public float theta = 0;                		// Media Orientation (in Degrees N)

	public int imageWidth, imageHeight;			// Image width and height
	
	WMV_PanoramaMetadata(String newName, String newFilePath, PVector newGPSLocation, ZonedDateTime newDateTime, String newTimeZone, 
			float newTheta, int newCameraModel, int newWidth, int newHeight, float newBrightness, String[] newKeywords)
	{
		super(newName, newFilePath, newGPSLocation, newDateTime, newTimeZone, newKeywords);
		
		imageWidth = newWidth;
		imageHeight = newHeight;

		theta = newTheta;
		brightness = newBrightness;
		cameraModel = newCameraModel;
	}
	
	WMV_PanoramaMetadata(){}

	public void initialize(String newName, String newFilePath, PVector newGPSLocation, ZonedDateTime newDateTime, String newTimeZone, 
			float newTheta, int newCameraModel, int newWidth, int newHeight, float newBrightness, String[] newKeywords)
	{
		super.init(newName, newFilePath, newGPSLocation, newDateTime, newTimeZone, newKeywords);
		
		imageWidth = newWidth;
		imageHeight = newHeight;

		theta = newTheta;
		brightness = newBrightness;
		cameraModel = newCameraModel;
	}
}
