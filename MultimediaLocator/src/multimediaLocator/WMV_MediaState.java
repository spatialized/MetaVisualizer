package multimediaLocator;

import java.time.ZonedDateTime;
import processing.core.PVector;

/**
 * State parameters applicable to any media object type 
 * @author davidgordon
 *
 */
public class WMV_MediaState 
{
	/* Status Modes */
	public boolean visible = false;				// Media is currently visible and will be drawn
	public boolean active = false;				// True when the image has faded in and isn't fading out	-- Needed?
	public boolean disabled = false;			// Disabled due to errors or user and will not be drawn
	public boolean hidden = false;				// Hidden from view											-- Needed?
	public boolean requested = false;			// Indicates a recent request to load media from disk
	public boolean showMetadata = false;		// Show metadata

	/* Metadata */
	public PVector gpsLocation;            		// Location in original GPS coords (longitude, altitude, latitude) 
	public int id;
	public int mediaType;							/* Media Types  0: image 1: panorama 2: video 3: sound */
	public String name = "";
	public String filePath = "";

	/* Time */
	public float clusterDate, clusterTime;		// Date and time relative to other images in cluster (position between 0. and 1.)
	public boolean isCurrentMedia;
	public ZonedDateTime dateTime;				// Media date and time
	public String timeZone;						// Media time zone
	public float timeBrightness = 0.f;			// Current brightness due to time
	
	/* Model */
	public PVector captureLocation;				// Media capture location in simulation – EXIF GPS coords scaled to fieldSize.
	public PVector location;        			// Media location in simulation 
	public int cluster = -1;				 	// Cluster it belongs to	
	public boolean fadingFocusDistance = false;
	public boolean beginFadingObjectDistance = false;			// Fading distance of object in image?
	public final float defaultAltitudeScalingFactor = 0.33f;			// Adjust altitude for ease of viewing

	
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
	
	/* Interaction */
	public boolean selected = false;
	
	WMV_MediaState(){}
}
