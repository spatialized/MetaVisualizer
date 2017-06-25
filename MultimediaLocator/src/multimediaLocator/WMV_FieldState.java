package multimediaLocator;

import java.util.ArrayList;
import java.util.List;

/******************************************
 * Current field state 
 * @author davidgordon
 *
 */
public class WMV_FieldState 
{
	/* General */
	public int id;											// Field ID
	public String name;										// Field Name
	public boolean named = true;							// Whether field has been named
	public boolean visited = false;							// Whether viewer has visited field
	public boolean loadedState = false;
	
	/* Data */
	public WMV_ModelState model;							// Model state for importing / exporting
	public ArrayList<ArrayList<WMV_Waypoint>> gpsTracks;	// GPS tracks
	public boolean dataFolderLoaded = false;					// Whether a data folder was found in field folder
	
	/* Time */
	public WMV_Timeline timeline;							// Field timeline
	public ArrayList<WMV_Date> dateline;					// List of dates, whose indices correspond with timelines in WMV_Field timelines list
	public String timeZoneID = "America/Los_Angeles";		// Current time zone

	/* Clusters */
	public int deepestLevel = -1;	
	public int defaultClusterDepth = 8;						// Default depth in dendrogram to look for media	-- Set based on deepestLevel?
	public int minClusterDepth = 2;							// Minimum depth in dendrogram to look for media
	public int clusterDepth = defaultClusterDepth;			// Current depth in dendrogram to look for media
	public List<Integer> clustersByDepth;					// Number of clusters at each dendrogram depth
	public boolean dendrogramCreated = false;				// Whether dendrogram has been created

	/* Media */
	public int imagesInRange = 0;			// Number of images in visible range
	public int panoramasInRange = 0;		// Number of panoramas in visible range
	public int videosInRange = 0;			 // Number of videos in visible range
	public int soundsInRange = 0; 			// Number of sounds in audible range

	public int imagesVisible = 0, imagesSeen = 0;			// Number of visible photos / currently seen
	public int panoramasVisible = 0, panoramasSeen = 0;		// Number of visible panoramas / currently seen
	public int videosVisible = 0, videosLoaded = 0, videosPlaying = 0, videosSeen = 0; // Number of visible videos / currently seen
	public int soundsAudible = 0, soundsLoaded = 0, soundsPlaying = 0, soundsHeard = 0; // Number of audible sounds / currently heard
	public int imageErrors = 0, videoErrors = 0, panoramaErrors = 0, soundErrors = 0;			// Metadata loading errors per media type
	public int indexPanoramaOffset, indexVideoOffset, indexSoundOffset;		// Starting index of panoramas / videos / sounds in names and distances arrays
	
//	public int disassociatedVideos = 0;
//	public int disassociatedSounds = 0;
	
	public WMV_FieldState(){}
	
	public void reset()
	{
		/* General */
		id = -1;						// Field ID
		name = "";						// Field Name
		visited = false;				// Whether viewer has visited field
		
		/* Time */
		timeZoneID = "America/Los_Angeles";					// Current time zone

		/* Data */
		imageErrors = 0; videoErrors = 0; panoramaErrors = 0; soundErrors = 0;	// Metadata loading errors per media type
		indexPanoramaOffset = -1; indexVideoOffset = -1;		
		indexSoundOffset = -1;
		gpsTracks = new ArrayList<ArrayList<WMV_Waypoint>>();
		
		/* Clusters */
		deepestLevel = -1;	
		defaultClusterDepth = 8;						// How deep in the dendrogram to look for media?	-- Set based on deepestLevel?
		minClusterDepth = 2;							// Minimum cluster depth
		clusterDepth = defaultClusterDepth;				// Current cluster depth
		clustersByDepth = new ArrayList<Integer>();		// Number of clusters at each dendrogram depth
		
		/* Hierarchical Clustering */
		dendrogramCreated = false;						// Dendrogram has been created

		/* Graphics */
		imagesInRange = 0;			// Number of images in visible range
		panoramasInRange = 0;		// Number of panoramas in visible range
		videosInRange = 0;			 // Number of videos in visible range
		soundsInRange = 0; 			// Number of sounds in audible range

		imagesVisible = 0; imagesSeen = 0;				// Number of visible photos and currently seen
		panoramasVisible = 0; panoramasSeen = 0;		// Number of visible panoramas and currently seen
		videosVisible = 0; videosLoaded = 0; videosPlaying = 0; videosSeen = 0;
		soundsAudible = 0; soundsLoaded = 0; soundsPlaying = 0; soundsHeard = 0;
		
//		disassociatedImages = 0;						// Images not associated with a cluster -- Still needed?
//		disassociatedPanoramas = 0;
//		disassociatedVideos = 0;
//		disassociatedSounds = 0;
	}
	
	/**
	 * Set time data 
	 * @param newTimeline New timeline
	 * @param newDateline New dateline
	 */
	public void setTimeData( WMV_Timeline newTimeline, ArrayList<WMV_Date> newDateline )
	{
		timeline = newTimeline;
		dateline = newDateline;
	}
	
	/**
	 * Set model data
	 * @param newModelState New model state
	 */
	public void setModelData(WMV_ModelState newModelState)
	{
		model = newModelState;
	}
}
