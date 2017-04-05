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
	
	/* Media */
	public int validImages, validPanoramas, validVideos;	// Number of valid images / number of valid videos
	public int validMedia;									// Total valid media count
	public float mediaDensity;								// Number of images as a function of field area

	/* Metadata */
	public float highLongitude = -1000000.f, lowLongitude = 1000000, highLatitude = -1000000.f, lowLatitude = 1000000,
			highAltitude = -1000000.f, lowAltitude = 1000000;
	public float highTime = -1000000.f, lowTime = 1000000;
	public float highDate = -1000000.f, lowDate = 1000000;
	public float highImageTime = -1000000.f, lowImageTime = 1000000;
	public float highImageDate = -1000000.f, lowImageDate = 1000000;
	public float highPanoTime = -1000000.f, lowPanoTime = 1000000;
	public float highPanoDate = -1000000.f, lowPanoDate = 1000000;
	public float highVideoTime = -1000000.f, lowVideoTime = 1000000;
	public float highVideoDate = -1000000.f, lowVideoDate = 1000000;
	public float longestImageDayLength = -1000000.f, longestPanoDayLength = -1000000.f, longestVideoDayLength = -1000000.f;	

	/* Time */
	public int minFrameRate = 15;
	public int frameCount = 0;

	WMV_ModelState(){}
}
