package multimediaLocator;

import processing.core.PVector;

/**
 * State of an image in 3D space
 * @author davidgordon
 *
 */
public class WMV_ImageState 
{
	
	/* ---from Viewable--- */
	/* Status Modes */
	public boolean visible = false;				// Media is currently visible and will be drawn
	public boolean active = false;				// True when the image has faded in and isn't fading out	-- Needed?
	public boolean disabled = false;			// Disabled due to errors or user and will not be drawn
	public boolean hidden = false;				// Hidden from view											-- Needed?
	public boolean requested = false;			// Indicates a recent request to load media from disk

	/* General */
	public int id;
	public int mediaType;							/* Media Types  0: image 1: panorama 2: video 3: sound */
	public String name = "";
	public String filePath = "";

	WMV_Time time;
	public float clusterDate, clusterTime;		// Date and time relative to other images in cluster (position between 0. and 1.)
	public boolean currentMedia;
	public float timeBrightness = 0.f;
	
	/* Model */
	public PVector captureLocation;				// Media capture location in simulation – EXIF GPS coords scaled to fieldSize.
	public PVector location;        			// Media location in simulation 
	public int cluster = -1;				 			// Cluster it belongs to	
	public float theta = 0;                			// Media Orientation (in Degrees N)
	public boolean fadingFocusDistance = false, beginFadingObjectDistance = false;			// Fading distance of object in image?
	public final float defaultAltitudeScalingFactor = 0.33f;			// Adjust altitude for ease of viewing

	/* Metadata */
	public PVector gpsLocation;            		// Location in original GPS coords (longitude, altitude, latitude) 
	public int cameraModel;                 	// Camera model
	public boolean showMetadata = false;		// Show metadata
	public float brightness;
	
	/* Interaction */
	private boolean selected = false;

	/* Graphics */
	public float aspectRatio = 0.666f;	// Aspect ratio of image or texture
	public PVector azimuthAxis = new PVector(0, 1, 0);
	public PVector verticalAxis = new PVector(1, 0, 0);
	public PVector rotationAxis = new PVector(0, 0, 1);
	public float centerSize = 0.05f;

	/* Transparency */
	public float viewingBrightness = 0;			// Final image brightness (or alpha in useAlphaFading mode) 
	public boolean isFadingIn = false, isFadingOut = false;
	public float fadingBrightness;							// Media transparency due to fading in / out
	public boolean beginFading = false, fading = false;		
	public float fadingStart = 0.f, fadingTarget = 0.f, fadingStartFrame = 0.f, fadingEndFrame = 0.f; 
	public boolean fadedOut = false;			// Recently faded out
	public boolean fadedIn = false;

	/* ---from Image--- */
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
	public float imageWidth, imageHeight;				// Image width and height
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


	
	WMV_ImageState(){}
}
