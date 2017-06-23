package multimediaLocator;

/**
 * Settings of the 3D world
 * @author davidgordon
 */
public class WMV_WorldSettings 
{
	/* General */
	public boolean screenMessagesOn = true;					// Show screen messages
	
	/* Model */
	public boolean divideFields = false;					// Attempt to divide fields when loading?
	
	public boolean copyLargeImageFiles = false;
	public boolean copyLargeVideoFiles = false;
	
	public final float defaultFocusDistance = 9.0f;			// Default focus distance for images and videos (m.)
	public final float defaultAltitudeScalingFactor = 0.33f;	// Adjust altitude for ease of viewing
	public float subjectSizeRatio = 0.18f;					// Subject portion of image / video plane (used in scaling from focus distance to imageSize)
	public final float panoramaFocusDistanceFactor = 1.1f;	// Scaling from defaultFocusDistance to panorama radius
	public final float videoFocusDistanceFactor = 0.8f;		// Scaling from defaultFocusDistance to video focus distance

	public boolean altitudeScaling = true;				// Scale media height by altitude (m.) EXIF field 
	public float altitudeScalingFactor = 0.33f;			// Adjust altitude for ease of viewing
	public final float altitudeScalingFactorInit = 0.33f;		

	public float kMeansClusteringEpsilon = 0.005f;		// If no clusters move farther than this threshold, stop cluster refinement
	public final float clusterCenterSize = 1.f;			// Size of cluster center, where autoNavigation stops
	public float mediaPointMass = 0.05f;				// Mass contribution of each media point
	public final float farDistanceFactor = 4.f;			// Multiplier for defaultFocusDistance to get farDistance
	public float clusterFarDistance = defaultFocusDistance * farDistanceFactor;			// Distance to apply greater attraction force on viewer
	public float minClusterDistance = 3.f; 				// Minimum distance between clusters, i.e. closer than which clusters are merged
	public float maxClusterDistance = 16.f;				// Maximum distance between cluster center and media
	public final float maxClusterDistanceConstant = 0.33f;	// Divisor to set maxClusterDistance based on mediaDensity
	public float maxClusterDistanceFactor = 5.f;			// Limit on maxClusterDistance as multiple of min. as media spread increases

	/* Graphics */
	public int maxVisibleClusters = -1;					// Maximum visible clusters at once (-1: no limit)
	public final int minClusterVisibility = 2;				// Minimum value for max visible clusters
	public boolean depthTesting = false;				// Enable depth testing
	public final int maxVisibleImages = 85;					// Maximum visible images at one time
	public final int maxVisiblePanoramas = 3;				// Maximum visible panoramas at one time
	public final int maxVisibleVideos = 2;					// Maximum visible videos at one time
	public final int maxAudibleSounds = 4;					// Maximum audible sounds at one time
	
	/* Time */
	public boolean getTimeZonesFromGoogle = false;		// Get time zone for each field center from Google Time Zone API
	public int timeCycleLength = 250;					// Length of main time loop in frames
	final public int defaultTimeCycleLength = 250;		// Default length of main time loop in frames
	
	public int timeUnitLength = 1;						// How many frames between time increments
	public float timeInc = timeCycleLength / 30.f;			

	public int dateCycleLength = 500;					// Length of main date loop in frames
	public int dateUnitLength = 1;						// How many frames between date increments
	public float dateInc = dateCycleLength / 30.f;			

	final public int initDefaultMediaLength = 50;		// Initial frame length of media in time cycle
	public int defaultMediaLength = 50;					// Default frame length of media in time cycle
	public float clusterLength = 1.f;						// Time interval for which close media become visible (in % of timeline length)
															// i.e. 1.f shows all media in range; 0.1f shows 10% of timeline for media in range, etc.
	public final float clusterTimePrecision = 0.0001f;	// Precision of timesHistogram (no. of bins)

	/* Video */
	public final float videoMaxVolume = 0.85f;
	public final float soundMaxVolume = 0.8f;
	public final float assocVideoDistTolerance = 15.f;			// How far a photo can be taken from a video's location to become associated.
	public final float assocVideoTimeTolerance = 0.015f;		// How long a photo can be taken before a video and still become associated;
														// (WMV assumes videographers will take a photo with Theodolite shortly before hitting record,
														// which will serve as its "associated" photo, containing necessary elevation and rotation angle data.)

	/* Stitching */
	public int maxStitchingImages = 30;					// Maximum number of images to try to stitch
	public float stitchingMinAngle = 30.f;				// Angle in degrees that determines media segments for stitching 
	public boolean persistentStitching = false;			// Keep trying to stitch, removing one image at a time until it works or no images left
	public boolean showUserPanoramas = false;			// Show panoramas stitched from user selected media
	public boolean showStitchedPanoramas = false;		// Show panoramas stitched from media segments

	WMV_WorldSettings(){}
	
	public void reset()
	{
		/* Model */
		subjectSizeRatio = 0.18f;				// Subject portion of image / video plane (used in scaling from focus distance to imageSize)
		
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
//		assocVideoDistTolerance = 15.f;			// How far a photo can be taken from a video's location to become associated.
//		assocVideoTimeTolerance = 0.015f;		// How long a photo can be taken before a video and still become associated;

		/* Stitching */
		maxStitchingImages = 30;				// Maximum number of images to try to stitch
		stitchingMinAngle = 30.f;				// Angle in degrees that determines media segments for stitching 
		persistentStitching = false;			// Keep trying to stitch, removing one image at a time until it works or no images left
		showUserPanoramas = true;				// Show panoramas stitched from user selected media
		showStitchedPanoramas = true;			// Show panoramas stitched from media segments

	}
}
