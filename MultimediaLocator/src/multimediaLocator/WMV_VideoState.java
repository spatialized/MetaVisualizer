package multimediaLocator;

import processing.core.PVector;

/**
 * State of a video in a field
 * @author davidgordon
 *
 */
public class WMV_VideoState
{
	/* Classes */
	public WMV_MediaState mState;
	private WMV_VideoMetadata metadata;

	/* Metadata */
	public int origVideoWidth = 0, origVideoHeight = 0;
	public PVector averageColor;
	public float averageBrightness;
	public float defaultFocusDistance = 9.0f;		// Default focus distance for images and videos (m.)
	public float origFocusDistance; 	 		 	// Original video viewing distance
	public float sensorSize;
	public float subjectSizeRatio = 0.18f;			// Subject portion of video plane (used in scaling from focus distance to imageSize)
	public int cameraModel;                 		// Camera model
	public float brightness;
	public float length;
	
	/* Time */
	public boolean loaded = false;
	public boolean playing = false;
	public int playbackStartFrame = -1;
	
	public final float assocVideoDistTolerance = 15.f;			// How far a photo can be taken from a video's location to become associated.
	public final float assocVideoTimeTolerance = 0.015f;		// How long a photo can be taken before a video and still become associated;
	
	/* Model*/
	public PVector disp = new PVector(0, 0, 0);    	// Displacement from capture location
	public boolean thinningVisibility = false;

	/* Graphics */
	public int blurMaskID;							// ID of blur mask 
	public int horizBorderID = -1;					// Horizontal border ID   	0: Left 1: Center 2: Right  3: Left+Right
	public int vertBorderID = -1;					// Vertical border ID		0: Bottom 1: Center 2: Top  3: Top+Bottom
	PVector[] vertices, sVertices;
	public PVector azimuthAxis = new PVector(0, 1, 0);
	public PVector verticalAxis = new PVector(1, 0, 0);
	public PVector rotationAxis = new PVector(0, 0, 1);
	public float outlineSize = 5.f;					// Selection outline line width
	public float outlineAlpha = 180.f;				// Selection outline alpha
	public float outlineHue = 90.f;					// Selection outline hue
	public final float videoFocusDistanceFactor = 0.8f;		// Scaling from defaultFocusDistance to video focus distance
	
	public float fadingFocusDistanceStartFrame = 0.f;
	public float fadingFocusDistanceEndFrame = 0.f;	// Fade focus distance and image size together
	public float fadingFocusDistanceStart = 0.f, fadingFocusDistanceTarget = 0.f;
	public float fadingFocusDistanceLength = 30.f;

	/* Sound */
	public float volume = 0.f;						// Video volume between 0. and 1.
	public boolean fadingVolume = false;
	public int volumeFadingStartFrame = 0, volumeFadingEndFrame = 0;
	public float volumeFadingStartVal = 0.f, volumeFadingTarget = 0.f;
	public final int volumeFadingLength = 60;	// Fade volume over 30 frames
	public boolean pauseAfterSoundFades = false;
	public boolean soundFadedIn = false, soundFadedOut = false;
	
	/* Navigation */
	public boolean isClose = false;				// Is the viewer in visible range?
	
	/* Placeholder Image */
	public boolean hasImagePlaceholder = false;
	public int imagePlaceholder = -1;
	
	WMV_VideoState(){}

	public void initialize(WMV_VideoMetadata newMetadata)
	{
		metadata = newMetadata;
		mState = new WMV_MediaState();
	}
	
	void setMediaState(WMV_MediaState newState, WMV_VideoMetadata newMetadata)
	{
		mState = newState;
		metadata = newMetadata;
	}
	
	public WMV_MediaState getMediaState()
	{
		return mState;
	}
	
	public WMV_VideoMetadata getMetadata()
	{
		return metadata;
	}
	
	public void resetState()
	{
		mState.resetState();
		loaded = false;
		playing = false;
		volume = 0.f;
		soundFadedIn = false;
		soundFadedOut = false;
		
		playbackStartFrame = -1;
		
		fadingFocusDistanceStartFrame = 0.f;
		fadingFocusDistanceEndFrame = 0.f;	
		fadingFocusDistanceStart = 0.f; 
		fadingFocusDistanceTarget = 0.f;
		fadingFocusDistanceLength = 30.f;
	}
}
