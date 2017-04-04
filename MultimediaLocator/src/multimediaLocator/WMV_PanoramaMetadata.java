package multimediaLocator;

/**
 * 360-degree panorama metadata
 * @author davidgordon
 *
 */
public class WMV_PanoramaMetadata 
{
	public int cameraModel;                 	// Camera model
	public float brightness;
	public float theta = 0;                		// Media Orientation (in Degrees N)

	public int imageWidth, imageHeight;			// Image width and height

	public String[] keywords;
	
	WMV_PanoramaMetadata(float newTheta, int newCameraModel, int newWidth, int newHeight, float newBrightness, 
			String[] newKeywords)
	{
		imageWidth = newWidth;
		imageHeight = newHeight;

		theta = newTheta;
		brightness = newBrightness;
		cameraModel = newCameraModel;

		keywords = newKeywords;
	}
}
