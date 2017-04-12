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
	public int id;						// Field ID
	public String name;				// Field Name

	/* Time */
	public WMV_Timeline timeline;						// List of time segments ordered by time from 0:00 to 24:00 as a single day
	public ArrayList<WMV_Date> dateline;							// List of dates, whose indices correspond with timelines in timelines list
	public String timeZoneID = "America/Los_Angeles";				// Current time zone
	
	/* Data */
	public ArrayList<WMV_ImageState> images; 				// Images for exporting/importing
	public ArrayList<WMV_PanoramaState> panoramas; 			// Panoramas for exporting/importing
	public ArrayList<WMV_VideoState> videos; 				// Videos for exporting/importing
//	public ArrayList<WMV_SoundState> sounds; 				// Sounds for exporting/importing

	/* Clusters */
	public int deepestLevel = -1;	
	public int defaultClusterDepth = 8;						// Default depth in dendrogram to look for media	-- Set based on deepestLevel?
	public int minClusterDepth = 2;							// Minimum depth in dendrogram to look for media
	public int clusterDepth = defaultClusterDepth;			// Current depth in dendrogram to look for media
	public List<Integer> clustersByDepth;					// Number of clusters at each dendrogram depth
	public boolean dendrogramCreated = false;				// Whether dendrogram has been created

	/* Media */
	public int imagesVisible = 0, imagesSeen = 0;			// Number of visible photos and currently seen
	public int panoramasVisible = 0, panoramasSeen = 0;		// Number of visible panoramas and currently seen
	public int videosVisible = 0, videosLoaded = 0, videosPlaying = 0, videosSeen = 0;
	public int imageErrors = 0, videoErrors = 0, panoramaErrors = 0;			// Metadata loading errors per media type
	public int indexPanoramaOffset, indexVideoOffset;		// Start of panoramas / videos in names and distances arrays
	public int disassociatedImages = 0;			// Images not associated with a cluster -- Obsolete?
	public int disassociatedPanoramas = 0;
	public int disassociatedVideos = 0;
	
	public WMV_FieldState(){}
	
	public void initialize()
	{
		images = new ArrayList<WMV_ImageState>();
		panoramas = new ArrayList<WMV_PanoramaState>();
		videos = new ArrayList<WMV_VideoState>();
	}
	
	public void reset()
	{
		/* General */
		id = -1;						// Field ID
		name = "";				// Field Name
		
		/* Time */
		timeZoneID = "America/Los_Angeles";					// Current time zone

		/* Data */
		imageErrors = 0; videoErrors = 0; panoramaErrors = 0;	// Metadata loading errors per media type
		indexPanoramaOffset = -1; indexVideoOffset = -1;		// Start of panoramas / videos in names and distances arrays

		/* Clusters */
		deepestLevel = -1;	
		defaultClusterDepth = 8;						// How deep in the dendrogram to look for media?	-- Set based on deepestLevel?
		minClusterDepth = 2;							// Minimum cluster depth
		clusterDepth = defaultClusterDepth;				// Current cluster depth
		clustersByDepth = new ArrayList<Integer>();		// Number of clusters at each dendrogram depth
		
		/* Hierarchical Clustering */
		dendrogramCreated = false;						// Dendrogram has been created

		/* Graphics */
		imagesVisible = 0; imagesSeen = 0;				// Number of visible photos and currently seen
		panoramasVisible = 0; panoramasSeen = 0;		// Number of visible panoramas and currently seen
		videosVisible = 0; videosLoaded = 0; videosPlaying = 0; videosSeen = 0;
		
		/* -- Debug -- */	
		disassociatedImages = 0;						// Images not associated with a cluster -- Still needed?
		disassociatedPanoramas = 0;
		disassociatedVideos = 0;

		/* Data */
//		clusters = new ArrayList<WMV_ClusterState>();			// Clusters (spatial groupings) of media 
		images = new ArrayList<WMV_ImageState>(); 				// All images in this field
		panoramas = new ArrayList<WMV_PanoramaState>(); 		// All panoramas in this field
		videos = new ArrayList<WMV_VideoState>(); 				// All videos in this field
	}
	
	public void setTimeData( WMV_Timeline newTimeline, ArrayList<WMV_Date> newDateline )
	{
		timeline = newTimeline;
		dateline = newDateline;
	}
	
	public void setMediaData( ArrayList<WMV_ImageState> newImages, ArrayList<WMV_PanoramaState> newPanoramas,
							  ArrayList<WMV_VideoState> newVideos )
	{
		images = newImages;
		panoramas = newPanoramas;
		videos = newVideos;
	}

	public void setImages(ArrayList<WMV_ImageState> newImages)
	{
		images = newImages;
	}

	public void setPanoramas(ArrayList<WMV_PanoramaState> newPanoramas)
	{
		panoramas = newPanoramas;
	}

	public void setVideos(ArrayList<WMV_VideoState> newVideos)
	{
		videos = newVideos;
	}

//	public void setSounds(ArrayList<WMV_ImageState> newImages)
//	{
//		images = newImages;
//	}
}
