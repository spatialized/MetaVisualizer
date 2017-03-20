package wmViewer;

/**
 * Settings for the virtual world
 * @author davidgordon
 */
public class WMV_WorldSettings 
{
	WMV_World w;

	/* Model */
	public float defaultFocusDistance = 9.0f;			// Default focus distance for images and videos (m.)
	public float subjectSizeRatio = 0.18f;				// Subject portion of image / video plane (used in scaling from focus distance to imageSize)
	public float panoramaFocusDistanceFactor = 1.1f;	// Scaling from defaultFocusDistance to panorama radius
	public float videoFocusDistanceFactor = 0.8f;		// Scaling from defaultFocusDistance to video focus distance

	public boolean altitudeScaling = true;				// Scale media height by altitude (m.) EXIF field 
	public float altitudeScalingFactor = 0.33f;			// Adjust altitude for ease of viewing
	public final float altitudeScalingFactorInit = 0.33f;		

	public float kMeansClusteringEpsilon = 0.005f;		// If no clusters move farther than this threshold, stop cluster refinement
	public final float clusterCenterSize = 1.f;			// Size of cluster center, where autoNavigation stops
	public float mediaPointMass = 0.05f;				// Mass contribution of each media point
	public final float farDistanceFactor = 4.f;			// Multiplier for defaultFocusDistance to get farDistance
	public float clusterFarDistance = defaultFocusDistance * farDistanceFactor;			// Distance to apply greater attraction force on viewer
	public float minClusterDistance = 3.f; 				// Minimum distance between clusters, i.e. closer than which clusters are merged
	public float maxClusterDistance = 11.f;				// Maximum distance between cluster center and media
	public final float maxClusterDistanceConstant = 0.33f;	// Divisor to set maxClusterDistance based on mediaDensity
	public float maxClusterDistanceFactor = 5.f;			// Limit on maxClusterDistance as multiple of min. as media spread increases

	/* Time */
	public int timeCycleLength = 250;					// Length of main time loop in frames
	final public int defaultTimeCycleLength = 250;		// Default length of main time loop in frames
	
	public int timeUnitLength = 1;						// How many frames between time increments
	public float timeInc = timeCycleLength / 30.f;			

	public int dateCycleLength = 500;					// Length of main date loop in frames
	public int dateUnitLength = 1;						// How many frames between date increments
	public float dateInc = dateCycleLength / 30.f;			

	final public int initDefaultMediaLength = 50;		// Initial frame length of media in time cycle
	public int defaultMediaLength = 50;					// Default frame length of media in time cycle

	public final int clusterTimePrecision = 10000;		// Precision of timesHistogram (no. of bins)
	public final int clusterDatePrecision = 1000;		// Precision of datesHistogram (no. of bins)
	public final int fieldTimePrecision = 10000;		// Precision of timesHistogram (no. of bins) -- Obsolete
	public final int fieldDatePrecision = 1000;			// Precision of timesHistogram (no. of bins) -- Obsolete

	/* Video */
	public final float videoMaxVolume = 0.9f;
	public float assocVideoDistTolerance = 15.f;		// How far a photo can be taken from a video's location to become associated.
	public float assocVideoTimeTolerance = 0.015f;		// How long a photo can be taken before a video and still become associated;
														// (WMV assumes videographers will take a photo with Theodolite shortly before hitting record,
														// which will serve as its "associated" photo, containing necessary elevation and rotation angle data.)

	/* Stitching */
	public int maxStitchingImages = 30;					// Maximum number of images to try to stitch
	public float stitchingMinAngle = 30.f;				// Angle in degrees that determines media segments for stitching 
	public boolean persistentStitching = false;			// Keep trying to stitch, removing one image at a time until it works or no images left
	public boolean showUserPanoramas = true;			// Show panoramas stitched from user selected media
	public boolean showStitchedPanoramas = true;		// Show panoramas stitched from media segments

	WMV_WorldSettings(WMV_World parent)
	{
		w = parent;
	}
	
	public void reset()
	{
		/* Model */
		defaultFocusDistance = 9.0f;			// Default focus distance for images and videos (m.)
		subjectSizeRatio = 0.18f;				// Subject portion of image / video plane (used in scaling from focus distance to imageSize)
		panoramaFocusDistanceFactor = 0.9f;		// Scaling from defaultFocusDistance to panorama radius
		videoFocusDistanceFactor = 0.9f;		// Scaling from defaultFocusDistance to video focus distance
		
		altitudeScaling = true;					// Scale media height by altitude (m.) EXIF field 
		altitudeScalingFactor = 0.33f;			// Adjust altitude for ease of viewing	-- Work more on this...

		kMeansClusteringEpsilon = 0.005f;		// If no clusters move farther than this threshold, stop cluster refinement
		mediaPointMass = 0.05f;					// Mass contribution of each media point
		clusterFarDistance = defaultFocusDistance * farDistanceFactor;			// Distance to apply greater attraction force on viewer
		minClusterDistance = 2.f; 				// Minimum distance between clusters, i.e. closer than which clusters are merged
		maxClusterDistance = 10.f;				// Maximum distance between cluster and media, i.e. farther than which single media clusters are created
		maxClusterDistanceFactor = 5.f;			// Limit on maxClusterDistance as multiple of min. as media spread increases

		/* Time */
		timeCycleLength = 250;					// Length of main time loop in frames
		timeUnitLength = 1;						// How many frames between time increments
		timeInc = timeCycleLength / 30.f;			

		dateCycleLength = 500;					// Length of main date loop in frames
		dateUnitLength = 1;						// How many frames between date increments
		dateInc = dateCycleLength / 30.f;			

		defaultMediaLength = initDefaultMediaLength;			// Default frame length of media in time cycle

		/* Video */
		assocVideoDistTolerance = 15.f;			// How far a photo can be taken from a video's location to become associated.
		assocVideoTimeTolerance = 0.015f;		// How long a photo can be taken before a video and still become associated;

		/* Stitching */
		maxStitchingImages = 30;				// Maximum number of images to try to stitch
		stitchingMinAngle = 30.f;				// Angle in degrees that determines media segments for stitching 
		persistentStitching = false;			// Keep trying to stitch, removing one image at a time until it works or no images left
		showUserPanoramas = true;				// Show panoramas stitched from user selected media
		showStitchedPanoramas = true;			// Show panoramas stitched from media segments

	}
}
