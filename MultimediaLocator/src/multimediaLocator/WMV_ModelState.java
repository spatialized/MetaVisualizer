package multimediaLocator;

/**
 * Current state of the field model
 * @author davidgordon
 *
 */
public class WMV_ModelState 
{
	/* Field */
	public float fieldWidth; 			// Width (X) of GPS photo area (real world)
	public float fieldHeight; 			// Height (Y) of GPS photo area (real world)
	public float fieldLength; 			// Length (Z) of GPS photo area	
	public float fieldArea;				// Field width * height
	public float fieldAspectRatio = 1; 	// Aspect ratio

	/* Clustering */
	public int mergedClusters = 0;							// Number of merged clusters
	public float minClusterDistance; 						// Minimum distance between clusters, i.e. closer than which clusters are merged
	public float maxClusterDistance;						// Maximum distance between clusters, i.e. farther than which single image clusters are created (set based on mediaDensity)

	public float clusterPopulationFactor = 10.f;						// Scaling from media spread (1/mediaDensity) to numClusters
	public float minPopulationFactor = 1.f, maxPopulationFactor = 30.f;	// Minimum and maximum values of populationFactor
	public int clusterRefinement = 60;									// Number of iterations used to refine clusters
	public int minClusterRefinement = 20, maxClusterRefinement = 300;	// Minimum and maximum values of clusterRefinement
	public long clusteringRandomSeed = (long)0.f;
//	public boolean clustersNeedUpdate = false;				// --NEED TO IMPLEMENT
	
	/* Media */
	public int validImages, validPanoramas, validVideos;	// Number of valid images / number of valid videos
	public int validMedia;									// Total valid media count
	public float mediaDensity;								// Number of images as a function of field area

	/* Metadata */
	public float highLongitude = -1000000, lowLongitude = 1000000, highLatitude = -1000000, lowLatitude = 1000000,
			highAltitude = -1000000, lowAltitude = 1000000;
	public float highTime = -1000000, lowTime = 1000000;
	public float highDate = -1000000, lowDate = 1000000;
	public float highImageTime = -1000000, lowImageTime = 1000000;
	public float highImageDate = -1000000, lowImageDate = 1000000;
	public float highPanoTime = -1000000, lowPanoTime = 1000000;
	public float highPanoDate = -1000000, lowPanoDate = 1000000;
	public float highVideoTime = -1000000, lowVideoTime = 1000000;
	public float highVideoDate = -1000000, lowVideoDate = 1000000;
	public float longestImageDayLength = -1000000, longestPanoDayLength = -1000000, longestVideoDayLength = -1000000;	

	/* Time */
	public int minFrameRate = 15;
	public int frameCount = 0;

	WMV_ModelState(){}
}
