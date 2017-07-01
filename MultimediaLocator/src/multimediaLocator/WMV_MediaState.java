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
	public boolean visible = false;				// Media is currently visible 
	public boolean seen = false;				// Media is currently seen and will be drawn
	public boolean active = false;				// True when the image has faded in and isn't fading out	-- Needed?
	public boolean disabled = false;			// Disabled due to errors or user and will not be drawn
	public boolean hidden = false;				// Hidden from view											-- Needed?
	public boolean requested = false;			// Indicates a recent request to load media from disk
	public boolean showMetadata = false;		// Show metadata

	/* General */
	public int id;								// Media ID
	public String originalPath = "";			// Path to original (full size) media
	public boolean hasOriginal = false;			// Whether has original (full size) media
	
	/* Metadata */
	public String name = "";					// Media filename
	public PVector gpsLocation;            		// Location in original GPS coordinates {longitude, altitude, latitude}
	public String longitudeRef, latitudeRef;	// Longitude / latitude reference (i.e. sign)
	public int mediaType;						// Media Type  {0: image 1: panorama 2: video 3: sound}

	/* Time */
	public float clusterLowDate, clusterLowTime;		// Low date and time of other media in cluster (position between 0. and 1.)
	public float clusterHighDate, clusterHighTime;		// High date and time of other media in cluster (position between 0. and 1.)
	public boolean isCurrentMedia;
	public ZonedDateTime dateTime;				// Media date and time
	public String timeZone;						// Media time zone
	public float timeBrightness = 0.f;			// Current brightness due to time
	
	/* Model */
	public PVector captureLocation;				// Media capture location in simulation – EXIF GPS coords scaled to fieldSize.
	public PVector location;        			// Media virtual location 
	public int cluster = -1;				 	// Cluster it belongs to	
	public boolean fadingFocusDistance = false;
	public boolean beginFadingObjectDistance = false;			// Whether fading distance of object in image
	public final float defaultAltitudeScalingFactor = 0.33f;	// Adjust altitude for ease of viewing
	
	/* Graphics */
	public float aspectRatio = 0.666f;							// Aspect ratio of image or texture
	public PVector azimuthAxis = new PVector(0, 1, 0);
	public PVector verticalAxis = new PVector(1, 0, 0);
	public PVector rotationAxis = new PVector(0, 0, 1);
	public float centerSize = 0.5f;

	/* Transparency */
	public float viewingBrightness = 0;						// Viewing brightness (alpha if in useAlphaFading mode) 
	public float fadingBrightness;							// Media transparency due to fading in / out
	public boolean isFadingIn = false, isFadingOut = false;
	public boolean beginFading = false, fading = false;		
	public float fadingStart = 0.f, fadingTarget = 0.f, fadingStartFrame = 0.f, fadingEndFrame = 0.f; 
	public boolean fadedOut = false;						// Recently faded out
	public boolean fadedIn = false;							// Recently faded in	
	public boolean hideAfterFadingOut = false;				// Hide media after fading out
	
	/* Interaction */
	public boolean selected = false;
	
	WMV_MediaState(){}
	
	public void resetState()
	{
		/* Status Modes */
		visible = false;			
		active = false;				
		disabled = false;			
		hidden = false;				
		requested = false;			
		showMetadata = false;		
		
		/* Time */
		timeBrightness = 0.f;		
		isCurrentMedia = false;

		/* Model */
		fadingFocusDistance = false;
		beginFadingObjectDistance = false;		

		/* Transparency */
		viewingBrightness = 0.f;	
		fadingBrightness = 0.f;	
		
		/* Fading */
		isFadingIn = false; isFadingOut = false;
		beginFading = false;
		fading = false;		
		fadingStart = 0.f; fadingTarget = 0.f;
		fadingStartFrame = 0.f; fadingEndFrame = 0.f; 
		fadedOut = false;			
		fadedIn = false;
		
		/* Interaction */
		selected = false;
	}
	
	public int getClusterID()
	{
		return cluster;
	}

	public void setClusterID(int newClusterID)
	{
//		if(newClusterID == -1) System.out.println("Setting cluster for media #"+id+" type "+mediaType+" from "+cluster+" to:"+newClusterID);
		cluster = newClusterID;
	}
}
