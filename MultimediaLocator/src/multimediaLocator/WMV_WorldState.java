package multimediaLocator;

/**
 * State of the virtual world
 * @author davidgordon
 *
 */
public class WMV_WorldState 
{
	/* Clustering Modes */
	public boolean hierarchical = false;			// Use hierarchical clustering (true) or k-means clustering (false) 

	/* Time */
	public int frameCount = 0;						// Frame count
	public int timeMode = 0;						// Time Mode (0 = cluster; 1 = field; 2 = single)
	public boolean timeFading = false;				// Does time affect media brightness? 
	public boolean paused = false;					// Time is paused

	public int currentTime = 0;						// Time units since start of time cycle (day / month / year)
	public int currentDate = 0;						// Date units since start of date cycle (day / month / year)

	/* Graphics */
	public float hudDistance = -1000.f;				// Distance of the Heads-Up Display from the virtual camera		-- Obsolete soon?
	
	public boolean drawTerrain = true;					// Show ground as wireframe grid
	public boolean alphaMode = true;					// Use alpha fading (true) or brightness fading (false)
	public float alpha = 195.f;							// Transparency
	public boolean beginFadingAlpha = false, fadingAlpha = false;
	public int fadingAlphaStartFrame = 0, fadingAlphaEndFrame = 0, fadingAlphaLength = 20;	
	public float fadingAlphaStart, fadingAlphaTarget;

	public boolean useBlurMasks = true;					// Fade/blur image edges
	public boolean showModel = false;					// Activate Model Display 
	public boolean showMediaToCluster = false;			// Draw line from each media point to cluster
	public boolean showCaptureToMedia = false;			// Draw line from each media point to its capture location
	public boolean showCaptureToCluster = false;		// Draw line from each media capture location to associated cluster

	/* Clusters */  
	public boolean mergeClusters = true;				// Merge nearby clusters?
	public boolean autoClusterDistances = false;		// Automatically set minClusterDistance + maxClusterDistance based on mediaDensity?
	public boolean lockMediaToClusters = false;			// Align media with the nearest cluster (to fix GPS uncertainty error)

	/* Media */
	public int requestedImages = 0;						// Count of images currently requested to be loaded from disk
	public int requestedPanoramas = 0;					// Count of panoramas currently requested to be loaded from disk	
	public boolean showMetadata = false;
	
	/* Memory */
	public int minAvailableMemory = 50000000;			// Minimum available memory
	public int memoryCheckFrequency = 50;				// Memory check rate
	public int minFrameRate = 10;						// Minimum frame rate

	/* Stitching */
	String stitchingPath;								// Stitched panorama output folder

	WMV_WorldState(){}
	
	public int getTimeMode()
	{
		return timeMode;
	}
}
