package multimediaLocator;

import processing.core.PVector;

/**
 * State of an image in a field
 * @author davidgordon
 */
public class WMV_ImageState 
{
	/* Classes */
	public WMV_MediaState mState;					// Access to this image's media state (for exporting)
	public WMV_ImageMetadata metadata;				// Access to this image's metadata (for exporting)

	/* Graphics */
	public PVector[] vertices, sVertices;			// Vertex list
	public int blurMaskID;							// ID of blur mask 
	public int horizBordersID = -1;					// Horizontal borders ID,   0: Left 1: Center 2: Right  3: Both (Left+Right)
	public int vertBordersID = -1;					// Vertical borders ID,		0: Bottom 1: Center 2: Top  3: Both (Top+Bottom)
	public float outlineSize = 5.f;					// Size of the outline around a selected image
	public float outlineHue = 90.f;					// Selection outline hue
	public float outlineAlpha = 180.f;				// Selection outline alpha

	/* Model */
	public boolean thinningVisibility = false;				// Thinning visibility of this image
	public PVector displacement = new PVector(0, 0, 0); 	// Displacement from capture location
	public float defaultFocusDistance = 9.0f;				// Default focus distance for images and videos (m.)
	public float origFocusDistance; 	 					// Original image viewing distance
	
	/* Interaction */
	public float fadingFocusDistanceStartFrame = 0.f, fadingFocusDistanceEndFrame = 0.f;
	public float fadingFocusDistanceStart = 0.f, fadingFocusDistanceTarget = 0.f;
	public float fadingFocusDistanceLength = 30.f;
	
	/* Model */
	public float subjectSizeRatio = 0.18f;			// Subject portion of image plane for scaling from focusDistance to imageSize
	
	/* Associated Video */
	public boolean isVideoPlaceHolder = false;		// Whether image is a video placeholder
	public int assocVideoID = -1;					// Video for which this image is a placeholder
	
	/**
	 * Constructor for image state
	 */
	WMV_ImageState(){}
	
	/**
	 * Initialize image state
	 * @param newMetadata Image metadata
	 */
	void initialize(WMV_ImageMetadata newMetadata)
	{
		metadata = newMetadata;
		mState = new WMV_MediaState();
	}
	
	/**
	 * Set media state 
	 * @param newState
	 * @param newMetadata
	 */
	void setMediaState(WMV_MediaState newState, WMV_ImageMetadata newMetadata)
	{
		mState = newState;
		metadata = newMetadata;
		
		if(mState.cluster == -1)
			System.out.println("setMediaState() for image #"+mState.id+" mState.cluster == -1!!!!");
	}
	
	public WMV_MediaState getMediaState()
	{
		return mState;
	}
	
	public void setMetadata(WMV_ImageMetadata newMetadata)
	{
		metadata = newMetadata;
	}
	
	public WMV_ImageMetadata getMetadata()
	{
		return metadata;
	}
	
	public void resetState()
	{
		mState.resetState();
	}
}
