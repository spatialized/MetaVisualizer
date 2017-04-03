package multimediaLocator;

import processing.core.PVector;

/**
 * Media type-specific parameters of an image in a field
 * @author davidgordon
 *
 */
public class WMV_ImageState 
{
	public WMV_MediaState vState;
	public int blurMaskID;
	
	public PVector[] vertices, sVertices;	// Vertex list

	public int horizBorderID = -1;					// Blur horizBorderID   0: Left 1: Center 2: Right  3: Left+Right
	public int vertBorderID = -1;					// Blur vertBorderID	0: Bottom 1: Center 2: Top  3: Top+Bottom
	public float outlineSize = 10.f;		// Size of the outline around a selected image

	public PVector displacement = new PVector(0, 0, 0);   // Displacement from capture location
	public float fadingFocusDistanceStartFrame = 0.f, fadingFocusDistanceEndFrame = 0.f;	// Fade focus distance and image size together
	public float fadingFocusDistanceStart = 0.f, fadingFocusDistanceTarget = 0.f;
	public float fadingFocusDistanceLength = 30.f;

	public boolean thinningVisibility = false;

	/* Metadata */
	public int imageWidth, imageHeight;				// Image width and height
	public float phi;			        				// Image Elevation (in Degrees N)
	public float orientation;              			// Landscape = 0, Portrait = 90, Upside Down Landscape = 180, Upside Down Portrait = 270
	public float rotation;				    			// Elevation angle and Z-axis rotation
	public float focalLength = 0; 						// Zoom Level 
	public float defaultFocusDistance = 9.0f;			// Default focus distance for images and videos (m.)
	public float focusDistance; 	 					// Image viewing distance (or estimated object distance, if given in metadata)
	public float origFocusDistance; 	 				// Original image viewing distance
	
	public float sensorSize;							// Approx. size of sensor in mm.
	public float subjectSizeRatio = 0.18f;				// Subject portion of image plane (used in scaling from focus distance to imageSize)
	
	/* Video Association */
	public boolean isVideoPlaceHolder = false;
	public int assocVideoID = -1;
	
	WMV_ImageState()
	{
		vState = new WMV_MediaState();
	}
	
	void setViewableState(WMV_MediaState newState)
	{
		vState = newState;
	}
}
