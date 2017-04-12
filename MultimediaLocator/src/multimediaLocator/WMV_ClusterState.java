package multimediaLocator;

import java.util.ArrayList;
import java.util.List;

import processing.core.PVector;

/**
 * Current cluster state
 * @author davidgordon
 *
 */
public class WMV_ClusterState 
{
	/* General */
	public int id;						// Cluster ID
	public PVector location;			// Cluster center location
	public boolean active = false; 		// Currently active
	public boolean empty = false;		// Currently empty
	public boolean single = false;		// Only one media point in cluster?
	
	/* Segmentation */
	public int numSegments = 0;							// Number of segments of the cluster
	
	/* Time */
	public ArrayList<WMV_Date> dateline;				// Capture dates for this cluster
	public WMV_Timeline timeline;						// Date-independent capture times for this cluster
	public ArrayList<WMV_Timeline> timelines;	
	public boolean timeFading = false;					// Does time affect photos' brightness? (true = yes; false = no)
	public boolean dateFading = true;					// Does time affect photos' brightness? (true = yes; false = no)
	public boolean paused = false;						// Time is paused
	public boolean showAllTimeSegments = true;			// Show all time segments (true) or show only current cluster (false)?

	public int currentTime = 0;							// Time units since start of time cycle (day / month / year)
	public int currentDate = 0;							// Current date in timeline	-- Need to implement!!
	public int timeCycleLength = 250;					// Length of main time loop in frames
	public int timeUnitLength = 1;						// How many frames between time increments
	public float timeInc = timeCycleLength / 30.f;			
	public int defaultMediaLength = 125;					// Default frame length of media in time cycle
	
	/* Physics */
	public boolean isAttractor;					// Whether cluster is attracting viewer
	public float clusterGravity = 0.1333f;		// Cluster gravitational pull
	public float clusterMass = 1.5f;			// Cluster mass		-- No longer tied to value of mediaPoints
	public float farMassFactor = 8.f;			// How much more mass to give distant attractors to speed up navigation?

	/* Media */
	public List<Integer> images = new ArrayList<Integer>();
	public List<Integer> panoramas = new ArrayList<Integer>();
	public List<Integer> videos = new ArrayList<Integer>();
//	public List<Integer> sounds = new ArrayList<Integer>();
	
	public int mediaCount;								// No. of media associated with this cluster
	private boolean hasImage = false;					// Cluster has panorama files?
	private boolean hasPanorama = false;					// Cluster has panorama files?
	private boolean hasVideo = false;
	private boolean hasSound = false;
	
	/* Interaction */
	public boolean selected = false;

	/* Dimensions */
	public float highLongitude, lowLongitude, highLatitude, lowLatitude, 		// - NEED TO CALCULATE!	
			  	 highAltitude, lowAltitude;		
	
	public float highTime, lowTime, highDate, lowDate;
	public float highImageTime = -1000000, lowImageTime = 1000000, 
		  highPanoramaTime = -1000000, lowPanoramaTime = 1000000, 
		  highVideoTime = -1000000, lowVideoTime = 1000000; 	
	public float highImageDate = -1000000, lowImageDate = 1000000, 
		  highPanoramaDate = -1000000, lowPanoramaDate = 1000000,
	 	  highVideoDate = -1000000, lowVideoDate = 1000000;

	WMV_ClusterState(){}
	
	public void setHasImage(boolean newState)
	{
		hasImage = true;
	}
	
	public boolean hasImage()
	{
		return hasImage;
	}
	
	public void setHasPanorama(boolean newState)
	{
		hasPanorama = true;
	}
	
	public boolean hasPanorama()
	{
		return hasPanorama;
	}
	
	public void setHasVideo(boolean newState)
	{
		hasVideo = true;
	}
	
	public boolean hasVideo()
	{
		return hasVideo;
	}
	
	public void setHasSound(boolean newState)
	{
		hasSound = true;
	}
	
	public boolean hasSound()
	{
		return hasSound;
	}
}
