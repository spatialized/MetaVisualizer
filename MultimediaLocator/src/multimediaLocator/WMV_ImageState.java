package multimediaLocator;

import processing.core.PVector;

/**
 * Media type-specific parameters of an image in a field
 * @author davidgordon
 *
 */
public class WMV_ImageState 
{
	/* Classes */
	public WMV_MediaState mState;
	public WMV_ImageMetadata metadata;

	/* Graphics */
	public PVector[] vertices, sVertices;			// Vertex list
	public int blurMaskID;							// Blur mask ID
	public int horizBorderID = -1;					// Blur horizBorderID   0: Left 1: Center 2: Right  3: Left+Right
	public int vertBorderID = -1;					// Blur vertBorderID	0: Bottom 1: Center 2: Top  3: Top+Bottom
	public float outlineSize = 10.f;				// Size of the outline around a selected image

	/* Model */
	public boolean thinningVisibility = false;
	public PVector displacement = new PVector(0, 0, 0); // Displacement from capture location
	public float defaultFocusDistance = 9.0f;			// Default focus distance for images and videos (m.)
	public float origFocusDistance; 	 				// Original image viewing distance
	
	/* Interaction */
	public float fadingFocusDistanceStartFrame = 0.f, fadingFocusDistanceEndFrame = 0.f;	// Fade focus distance and image size together
	public float fadingFocusDistanceStart = 0.f, fadingFocusDistanceTarget = 0.f;
	public float fadingFocusDistanceLength = 30.f;
	
	/* Model */
	public float subjectSizeRatio = 0.18f;			// Subject portion of image plane (used in scaling from focus distance to imageSize)
	
	/* Video Association */
	public boolean isVideoPlaceHolder = false;
	public int assocVideoID = -1;
	
	WMV_ImageState(WMV_ImageMetadata newMetadata)
	{
		metadata = newMetadata;
		mState = new WMV_MediaState();
	}
	
	void setMediaState(WMV_MediaState newState, WMV_ImageMetadata newMetadata)
	{
		mState = newState;
		metadata = newMetadata;
	}
	
	public WMV_ImageMetadata getMetadata()
	{
		return metadata;
	}
}
