package main.java.com.entoptic.multimediaLocator;

/*********************************
 * Virtual world settings
 * @author davidgordon
 */
public class WMV_WorldSettings 
{
	/* General */
	public boolean screenMessagesOn = true;						// Show screen messages
	
	/* Model */
	public boolean divideFields = false;							// Whether to attempt to divide fields when loading
	
	public boolean copyLargeImageFiles = false;
	public boolean copyLargeVideoFiles = false;
	
	public final float defaultFocusDistance = 10.0f;				// Default focus distance for images and videos (m.)	-- Duplicated in viewer settings!
	public final float defaultAltitudeScalingFactor = 1.5f;		// Adjust altitude for ease of viewing
	public float subjectSizeRatio = 0.18f;						// Subject portion of image / video plane (used in scaling from focus distance to imageSize)
	public final float panoramaFocusDistanceFactor = 1.1f;		// Scaling from defaultFocusDistance to panorama radius
	public final float videoFocusDistanceFactor = 0.8f;			// Scaling from defaultFocusDistance to video focus distance

	public boolean altitudeScaling = true;								// Scale media height by altitude (m.) EXIF field 
	public final float altitudeScalingFactorMax = 4.f;					// Adjust altitude for ease of viewing
	public float altitudeScalingFactor = defaultAltitudeScalingFactor;	// Adjust altitude for ease of viewing

	public float kMeansClusteringEpsilon = 0.005f;				// If no clusters move farther than this threshold, stop cluster refinement
	public final float clusterCenterSize = 1.f;					// Size of cluster center, where autoNavigation stops
	
	public float mediaPointMass = 0.05f;							// Mass contribution of each media point
	public float attractorMass = 1.25f;							// Mass of ordinary attractor point
	public float pathAttractorMass = 0.5f;						// Mass of path attractor point
	public float attractorMassDistanceFactor = 0.25f;				// Distance factor for attractor + path attractor mass calculation
	public float minAttractorMass = 0.1f;						// Minimum attractor point mass
	public float maxAttractorMass = 20.f;						// Maximum attractor point mass
	public float minPathAttractorMass = 0.05f;					// Minimum attractor point mass
	public float maxPathAttractorMass = 10.f;					// Maximum attractor point mass
	
	public final float farDistanceFactor = 4.f;					// Multiplier for defaultFocusDistance to get farDistance
	public float clusterFarDistance = defaultFocusDistance * farDistanceFactor;			// Distance to apply greater attraction force on viewer
	public float minClusterDistance = 3.f; 						// Minimum distance between clusters, i.e. closer than which clusters are merged
	public float maxClusterDistance = 16.f;						// Maximum distance between cluster center and media
	public final float maxClusterDistanceConstant = 0.33f;		// Divisor to set maxClusterDistance based on mediaDensity
	public float maxClusterDistanceFactor = 5.f;				// Limit on maxClusterDistance as multiple of min. as media spread increases

	/* Model Display*/
	public float gpsTrackWidth = 4.f;						// Width of GPS track line when Show Model option is on
	public float gpsTrackHue = 90.f;							// GPS track line hue
	public float gpsTrackSaturation = 225.f;					// GPS track line saturation
	public float gpsTrackBrightness = 185.f;					// GPS track line brightness
	public float gpsTrackAlphaMax = 215.f;					// GPS track line maximum alpha
	public float gpsTrackVisibleDistance = 200.f;			// Distance at which a GPS track waypoint is shown with Show Model option on
	
	/* Graphics */
	public int maxVisibleClusters = -1;						// Maximum visible clusters at once (-1: no limit)
	public final int minClusterVisibility = 2;				// Minimum value for max visible clusters
	public final int maxVisibleImages = 85;					// Maximum visible images at one time
	public final int maxVisiblePanoramas = 3;				// Maximum visible panoramas at one time
	public final int maxVisibleVideos = 2;					// Maximum visible videos at one time
	public final int maxAudibleSounds = 4;					// Maximum audible sounds at one time
	public boolean depthTesting = false;						// Enable depth testing		-- Not recommended
	
	/* Time */
	public int timeCycleLength = 250;						// Length of main time loop in frames
	final public int defaultTimeCycleLength = 250;			// Default length of main time loop in frames
	
	public int timeUnitLength = 1;							// How many frames between time increments
	public float timeInc = timeCycleLength / 30.f;			// Time cycle user increment amount

	public int dateCycleLength = 500;						// Length of main date loop in frames
	public int dateUnitLength = 1;							// How many frames between date increments
	public float dateInc = dateCycleLength / 30.f;			

	public final int initDefaultMediaLength = 50;			// Initial frame length of media in time cycle
	public int defaultMediaLength = initDefaultMediaLength;						// Default frame length of media in time cycle
	public final float clusterTimePrecision = 0.0001f;		// Precision of timesHistogram (no. of bins)
	public float clusterLength = 1.f;						// Time interval for which close media become visible (in % of timeline length),
															// i.e. 1.f shows all media in range; 0.1f shows 10% of timeline for media in range, etc.
	public boolean getTimeZonesFromGoogle = false;			// Get time zone for each field center from Google Time Zone API	-- Disabled

	/* Video */
	public final float videoMaxVolume = 0.85f;				// Maximum video volume
	public final float soundMaxVolume = 0.8f;				// Maximum sound volume
	public final float assocVideoDistTolerance = 15.f;		// How far a photo can be taken from a video's location to become associated.
	public final float assocVideoTimeTolerance = 0.015f;	// How long a photo can be taken before a video and still become associated;
															// (WMV assumes videographers will take a photo with Theodolite shortly before hitting record,
															// which will serve as its "associated" photo, containing necessary elevation and rotation angle data.)

	/* Sound */
	public final float soundGPSTimeThreshold = 12.f;		// Threshold allowable between GPS track waypoint and associated sound capture times (in sec.) 
	
	/* Stitching */
	public final int maxStitchingImages = 30;						// Maximum number of images to try to stitch
	public float stitchingMinAngle = 30.f;					// Angle in degrees that determines media segments for stitching 
	public boolean persistentStitching = false;				// Keep trying to stitch, removing one image at a time until it works or no images left
	public boolean showUserPanoramas = false;				// Show panoramas stitched from user selected media
	public boolean showStitchedPanoramas = false;			// Show panoramas stitched from media segments

	WMV_WorldSettings(){}
	
	public void reset()
	{
		/* General */
		screenMessagesOn = true;						// Show screen messages
		
		/* Model */
		divideFields = false;							// Attempt to divide fields when loading?
		
		copyLargeImageFiles = false;
		copyLargeVideoFiles = false;
		
		subjectSizeRatio = 0.18f;							
		
		altitudeScaling = true;								
		altitudeScalingFactor = 0.33f;						

		kMeansClusteringEpsilon = 0.005f;					
		mediaPointMass = 0.05f;								
		clusterFarDistance = defaultFocusDistance * farDistanceFactor;
		minClusterDistance = 3.f; 							
		maxClusterDistance = 16.f;							
		maxClusterDistanceFactor = 5.f;	

		/* Graphics */
		maxVisibleClusters = -1;						// Maximum visible clusters at once (-1: no limit)
		depthTesting = false;							// Enable depth testing
		
		/* Time */
		getTimeZonesFromGoogle = false;			// Get time zone for each field center from Google Time Zone API
		timeCycleLength = 250;							
		
		timeUnitLength = 1;		
		timeInc = timeCycleLength / 30.f;	

		dateCycleLength = 500;					
		dateUnitLength = 1;				
		dateInc = dateCycleLength / 30.f;			

		defaultMediaLength = initDefaultMediaLength;		
		clusterLength = 1.f;									// Time interval for which close media become visible, as % of timeline length

		/* Stitching */
		stitchingMinAngle = 30.f;				
		persistentStitching = false;			
		showUserPanoramas = false;				
		showStitchedPanoramas = true;			
	}
}
