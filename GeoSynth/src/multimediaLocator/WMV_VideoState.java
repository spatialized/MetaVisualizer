package multimediaLocator;

import processing.core.PVector;

/**
 * Type-specific parameters of a video in a field
 * @author davidgordon
 *
 */
public class WMV_VideoState 
{
	public WMV_MediaState vState;
	
	public boolean loaded = false;
	public boolean playing = false;
	public boolean soundFadedIn = false, soundFadedOut = false;
	
	/* Metadata */
	int origVideoWidth = 0, origVideoHeight = 0;
	PVector averageColor;
	float averageBrightness;

	public float orientation;              		// Landscape = 0, Portrait = 90, Upside Down Landscape = 180, Upside Down Portrait = 270
	public float phi, rotation;       				// Elevation angle and Z-axis rotation
	public float focalLength = 0; 					// Zoom Level 
	public float focusDistance; 	 		 		// Video viewing distance
	public float defaultFocusDistance = 9.0f;			// Default focus distance for images and videos (m.)
	public float origFocusDistance; 	 		 		// Original video viewing distance
	public float sensorSize;
	public float subjectSizeRatio = 0.18f;			// Subject portion of video plane (used in scaling from focus distance to imageSize)
	public PVector disp = new PVector(0, 0, 0);    	// Displacement from capture location
	public float length;
	public final float assocVideoDistTolerance = 15.f;			// How far a photo can be taken from a video's location to become associated.
	public final float assocVideoTimeTolerance = 0.015f;		// How long a photo can be taken before a video and still become associated;

	/* Graphics */
	PVector[] vertices, sVertices;
	public int videoWidth = 0, videoHeight = 0;			// Video width and height
	public PVector azimuthAxis = new PVector(0, 1, 0);
	public PVector verticalAxis = new PVector(1, 0, 0);
	public PVector rotationAxis = new PVector(0, 0, 1);
	public float outlineSize = 10.f;
	public final float videoFocusDistanceFactor = 0.8f;		// Scaling from defaultFocusDistance to video focus distance
	
	public float fadingFocusDistanceStartFrame = 0.f;
	public float fadingFocusDistanceEndFrame = 0.f;	// Fade focus distance and image size together
	public float fadingFocusDistanceStart = 0.f, fadingFocusDistanceTarget = 0.f;
	public float fadingFocusDistanceLength = 30.f;

	public boolean thinningVisibility = false;
 	
	/* Sound */
	public float volume = 0.f;						// Video volume between 0. and 1.
	public boolean fadingVolume = false;
	public int volumeFadingStartFrame = 0, volumeFadingEndFrame = 0;
	public float volumeFadingStartVal = 0.f, volumeFadingTarget = 0.f;
	public final int volumeFadingLength = 60;	// Fade volume over 30 frames
	public boolean pauseAfterSoundFades = false;
	
	/* Navigation */
	public boolean isClose = false;				// Is the viewer in visible range?
	
	/* Placeholder Image */
	public boolean hasImagePlaceholder = false;
	public int imagePlaceholder = -1;

	
	WMV_VideoState()
	{
		vState = new WMV_MediaState();
	}
	
	void setViewableState(WMV_MediaState newState)
	{
		vState = newState;
	}
}
