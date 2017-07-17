package main.java.com.entoptic.multimediaLocator;

/**
 * State of virtual world
 * @author davidgordon
 */
public class WMV_WorldState
{
	/* Time */
	public boolean timeFading = false;					// Does time affect media brightness? 
	public boolean paused = false;						// Time is paused
	private int currentTime = 0;							// Time units since start of time cycle (day / month / year)
	public int currentDate = 0;							// Date units since start of date cycle (day / month / year)
	public int frameCount = 0;							// Frame count
	public int timeMode = 0;								// Time Mode: {0: cluster, 1: field}		(2 = media)

	/* Model */
	public final float modelBrightness = 255.f;
	public final float modelAlpha = 205.f;
	public final float modelDistanceVisibilityFactorClose = 5.f;		// Near distance at which media model becomes invisible
	public float modelDistanceVisibilityFactorFar = 18.f;				// Far distance at which media model becomes invisible
	/* Graphics */
	public boolean loadedMasks = false;
	public float hudDistance = -1000.f;					// Distance of the Heads-Up Display from the virtual camera		-- Obsolete?
	public float screenAspectRatio;
	public float aspectWidthRatioFactor;
	
	public boolean displayTerrain = false;				// Show ground as wireframe grid
	public boolean alphaMode = true;					// Use alpha fading (true) or brightness fading (false)
	public float alpha = 195.f;							// Transparency
	
	public boolean beginFadingAlpha = false, fadingAlpha = false;		// Global alpha fading 
	public int fadingAlphaStartFrame = 0, fadingAlphaEndFrame = 0, fadingAlphaLength = 20;	
	public float fadingAlphaStart, fadingAlphaTarget;

	public boolean useBlurMasks = true;					// Fade/blur image edges
	public boolean showModel = false;					// Activate Model Display 
	public boolean showMediaToCluster = false;			// Draw line from each media point to cluster
	public boolean showCaptureToMedia = false;			// Draw line from each media point to its capture location
	public boolean showCaptureToCluster = false;		// Draw line from each media capture location to associated cluster

	public boolean fadingTerrainAlpha = false, waitingToFadeInTerrainAlpha = false, turnOffTerrainAfterFadingOut = false;		
	public float terrainAlpha = 0.f, fadingTerrainStart = 0.f, fadingTerrainTarget = 0.f;
	public final float terrainAlphaMax = 200.f;
	public int fadingTerrainStartFrame = 0, fadingTerrainEndFrame = 0; 
	public final int fadingTerrainLength = 20; 

	/* Clusters */  
	public boolean hierarchical = false;				// Use hierarchical clustering (true) or k-means clustering (false) 
	public boolean mergeClusters = true;				// Whether to merge nearby clusters
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

	public WMV_WorldState(float newScreenAspectRatio)
	{
		screenAspectRatio = newScreenAspectRatio;
		aspectWidthRatioFactor = screenAspectRatio / 0.625f;
		
		System.out.println("screenAspectRatio:"+screenAspectRatio+" aspectWidthRatioFactor:"+aspectWidthRatioFactor);
	}
	
	public int getTimeMode()
	{
		return timeMode;
	}
	
	public void reset()
	{
//		/* Clustering Modes */
//		hierarchical = false;					// Use hierarchical clustering (true) or k-means clustering (false) 

		/* Time */
		timeFading = false;					// Does time affect media brightness? 
		paused = false;						// Time is paused

		currentTime = 0;						// Time units since start of time cycle (day / month / year)
		currentDate = 0;						// Current timeline ID corresponding to capture date in ordered list
		frameCount = 0;							// Frame count
		timeMode = 0;							// Time Mode: 0 = cluster; 1 = field; 2 = (single) media

		/* Graphics */
		loadedMasks = false;
		hudDistance = -1000.f;				// Distance of the Heads-Up Display from the virtual camera

		displayTerrain = false;
		alphaMode = true;						// Use alpha fading (true) or brightness fading (false)
		alpha = 195.f;							// Transparency
		
		beginFadingAlpha = false; fadingAlpha = false;
		fadingAlphaStartFrame = 0; fadingAlphaEndFrame = 0; fadingAlphaLength = 20;	
//		public float fadingAlphaStart, fadingAlphaTarget;

		useBlurMasks = true;						// Blur image edges
		showModel = false;					// Display model 
		showMediaToCluster = false;			// Draw line from each media point to cluster
		showCaptureToMedia = false;			// Draw line from each media point to its capture location
		showCaptureToCluster = false;			// Draw line from each media capture location to associated cluster

		fadingTerrainAlpha = false; waitingToFadeInTerrainAlpha = false; turnOffTerrainAfterFadingOut = false;		
		terrainAlpha = 0.f; fadingTerrainStart = 0.f; fadingTerrainTarget = 0.f;
		fadingTerrainStartFrame = 0; fadingTerrainEndFrame = 0; 

		/* Clusters */
		hierarchical = false;				// Use hierarchical clustering (true) or k-means clustering (false) 
		mergeClusters = true;					// Merge nearby clusters?
		lockMediaToClusters = false;			// Align media with the nearest cluster (to fix GPS uncertainty error)
		
		/* Media */
		requestedImages = 0;						// Count of images currently requested to be loaded from disk
		requestedPanoramas = 0;					// Count of panoramas currently requested to be loaded from disk	
		showMetadata = false;
		
		/* Memory */
		minAvailableMemory = 50000000;			// Minimum available memory
		memoryCheckFrequency = 50;				// Memory check rate
		minFrameRate = 10;						// Minimum frame rate

		/* Stitching */
		String stitchingPath = null;								// Stitched panorama output folder
	}
	
	public int getCurrentTimeCycleFrame()
	{
		return currentTime;
	}
	
	public void setCurrentTimeCycleFrame(int newTime)
	{
		currentTime = newTime;
	}
}
