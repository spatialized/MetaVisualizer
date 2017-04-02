package multimediaLocator;

import java.util.ArrayList;
import java.util.List;

/**
 * Snapshot of a 3D environment at a particular time
 * @author davidgordon
 *
 */
public class WMV_FieldState 
{
	/* General */
	public int id;						// Field ID
	public String name;				// Field Name
	
	/* Time */
	public String timeZoneID = "America/Los_Angeles";					// Current time zone

	/* Data */
	public int imageErrors = 0, videoErrors = 0, panoramaErrors = 0;			// Metadata loading errors per media type
	public int indexPanoramaOffset, indexVideoOffset;		// Start of panoramas / videos in names and distances arrays

	/* Clusters */
	public int deepestLevel = -1;	
	public int defaultClusterDepth = 8;						// How deep in the dendrogram to look for media?	-- Set based on deepestLevel?
	public int minClusterDepth = 2;
	public int clusterDepth = defaultClusterDepth;			// How deep in the dendrogram to look for media?
	public List<Integer> clustersByDepth;						// Number of clusters at each dendrogram depth
	
	/* Hierarchical Clustering */
	public boolean dendrogramCreated = false;				// Dendrogram has been created

	/* Graphics */
	public int imagesVisible = 0, imagesSeen = 0;			// Number of visible photos and currently seen
	public int panoramasVisible = 0, panoramasSeen = 0;		// Number of visible panoramas and currently seen
	public int videosVisible = 0, videosLoaded = 0, videosPlaying = 0, videosSeen = 0;
	
	/* -- Debug -- */	
	public int disassociatedImages = 0;						// Images not associated with a cluster -- Still needed?
	public int disassociatedPanoramas = 0;
	public int disassociatedVideos = 0;

	/* Data */
	private ArrayList<WMV_ClusterState> clusters;				// Clusters (spatial groupings) of media 
	private ArrayList<WMV_ImageState> images; 					// All images in this field
	private ArrayList<WMV_PanoramaState> panoramas; 			// All panoramas in this field
	private ArrayList<WMV_VideoState> videos; 					// All videos in this field
//	private ArrayList<WMV_SoundState> sounds; 					// All sounds in this field
	
	WMV_FieldState()
	{
		clusters = new ArrayList<WMV_ClusterState>();
		images = new ArrayList<WMV_ImageState>();
		panoramas = new ArrayList<WMV_PanoramaState>();
		videos = new ArrayList<WMV_VideoState>();
	}
	
	public void setFieldData(ArrayList<WMV_ClusterState> newClusters, ArrayList<WMV_ImageState> newImages, ArrayList<WMV_PanoramaState> newPanoramas,
			ArrayList<WMV_VideoState> newVideos)
	{
		clusters = newClusters;
		images = newImages;
		panoramas = newPanoramas;
		videos = newVideos;
	}
	
	public void setClusters(ArrayList<WMV_ClusterState> newClusters)
	{
		clusters = newClusters;
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
