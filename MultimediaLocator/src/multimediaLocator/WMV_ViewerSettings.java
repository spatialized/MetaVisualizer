package multimediaLocator;

/*********************************
 * @author davidgordon
 * Current settings of virtual viewer
 */
public class WMV_ViewerSettings 
{
	/* Camera */
	public final float initFieldOfView = (float)Math.PI * 0.375f;	// Camera field of view
	public float fieldOfView = initFieldOfView; 				// Initial camera field of view
	public float rotateIncrement = 3.1415f / 256.f;				// Rotation amount per frame when turning
	public float zoomIncrement = 3.1415f / 32.f;				// Zoom amount per frame when zooming
	public float nearClippingDistance = 3.f; 						// Distance (m.) of near clipping plane
	public float nearViewingDistance = nearClippingDistance * 2.f; 	// Near distance (m.) at which media start fading out
	public float farViewingDistance = 11.f; 						// Far distance (m.) at which media start fading out
	public float userBrightness = 1.f;							// User brightness

	/* Graphics */
	public final float defaultFocusDistance = 9.0f;			// Default focus distance for images and videos (m.)
	public boolean orientationMode = false;				// Viewer "moves" by standing still (images fade in and out across space)
	public boolean angleFading = true;					// Do photos fade out as the camera turns away from them?
	public float visibleAngle = (float)Math.PI / 3.33f;		// Angle within which images and videos become visible
	public float visibleAngleMax = 3.14f;
	public float visibleAngleMin = 0.05f;
	public float visibleAngleInc = 0.04f;
	
	public float centeredAngle = visibleAngle / 2.f;		// Angle at which the image is considered centered
	public boolean angleThinning = false;					// Thin images and videos of similar orientation
	public float thinningAngle = (float)Math.PI / 6.f;		// Angle to thin images and videos within
	public int alphaTransitionLength = 15;

	/* Video */
	public boolean autoPlayVideos = true;					// Automatically play videos near viewer
	public int autoPlayMaxVideoCount = 2;					// Maximum videos to auto play simultaneously

	/* Sound */
	public boolean autoPlaySounds = true;					// Automatically play videos near viewer
	public int autoPlayMaxSoundCount = 3;					// Maximum videos to auto play simultaneously
	public float farHearingDistance = 36.f; 				// Far distance (m.) at which media start fading out
	public int soundFadingLength = 30;						// Frame length of sounds, including video sound, to fade in and out 
	
//	/* Media Visibility */
//	public final int maxVisibleImages = 85;					// Maximum visible images at one time
//	public final int maxVisiblePanoramas = 3;				// Maximum visible panoramas at one time
//	public final int maxVisibleVideos = 2;					// Maximum visible videos at one time
//	public final int maxAudibleSounds = 4;					// Maximum audible sounds at one time
	
	public boolean hideImages = false;						// Hide images
	public boolean hidePanoramas = false;					// Hide panoramas
	public boolean hideVideos = false;						// Hide videos
	public boolean hideSounds = false;						// Hide videos

	/* Physics */
	public float lastAttractorDistance = -1.f;
	public float cameraMass = 0.33f;						// Camera mass for cluster attraction
	public float velocityMin = 0.00005f;					// Threshold under which velocity counts as zero
	public float velocityMax = 0.66f;						// Camera maximum velocity
	public float accelerationMax = 0.15f;					// Camera maximum acceleration
	public float accelerationMin = 0.00001f;				// Threshold under which acceleration counts as zero
	public float camDecelInc = 0.66f;						// Camera deceleration increment
	public float camHaltInc = 0.0033f;						// Camera fast deceleration increment

	/* Movement */
	public float walkingAccelInc = 0.002f;					// Camera walking acceleration increment
	public final int initPathWaitLength = 60;				// Initial pathWaitLength
	public int pathWaitLength = initPathWaitLength;			// Time to wait once reached path location before moving to next
	public int teleportLength = 30;							// Teleport transition length 
	public boolean teleportToFarClusters = true;			// Automatically teleport to far clusters
	public float farClusterTeleportDistance = 240.f;		// Distance at which cluster is considered far
	public boolean alwaysLookAtMedia = false;				// Automatically turn towards media when reached new cluster -- Fix bugs!
	
	/* Turning */
	final public float turningVelocityMin = 0.00005f;			// Threshold under which velocity counts as zero
	final public float turningVelocityMax = 0.05f;				// Camera maximum velocity
	final public float turningAccelerationMax = 0.005f;			// Camera maximum acceleration
	final public float turningAccelerationMin = 0.000005f;		// Threshold under which acceleration counts as zero
	final public float turningDecelInc = 0.45f;					// Camera deceleration increment
	final public float turningHaltInc = 0.0033f;				// Camera fast deceleration increment
	public float turningXAccelInc = 0.0001f;					// Turning X axis acceleration increment
	public float turningYAccelInc = 0.0001f;					// Turning Y axis acceleration increment

	/* Interaction */
	public boolean selection = false;					// Allows selection, increases transparency to make selected image(s) easier to see
	public boolean optimizeVisibility = false;			// Optimize visibility automatically by turning towards media / changing graphics modes
	public boolean lockToCluster = false;				// Automatically move viewer to nearest cluster when idle
	public boolean multiSelection = false;				// User can select multiple images for stitching
	public boolean groupSelection = false;			// Select image segments at a time
	public boolean mouseNavigation = false;				// Mouse navigation

	public int mediaDensityThreshold = 12;				// Number of images or videos counted as high density
	public float selectionMaxDistance;					// Maximum distance user can select a photo
	public float selectionMaxDistanceFactor = 2.f;		// Scaling from defaultFocusDistanceFactor to selectionMaxDistance
	public int lockToClusterWaitLength = 100;
	
	/* Cluster Settings */
	public int orientationModeMaxVisibleClusters = 4;					// Maximum visible clusters in Orientation Mode		
	public int orientationModeMinVisibleClusters = 1;					// Maximum visible clusters in Orientation Mode	
	public float orientationModeClusterViewingDistance = nearClippingDistance;	// Distance clusters become visible in Orientation Mode
	public boolean orientationModeForceVisible = false;			// Force <minimum visible clusters> to be seen, even if out of range
	public boolean orientationModeConstantWaitLength = true;	// Wait same length of time even if multiple time segments in one location

	/**
	 * Constructor for viewer settings
	 */
	public WMV_ViewerSettings(){}

	/**
	 * Initialize viewer settings object
	 */
	public void initialize()
	{
		fieldOfView = initFieldOfView; 		
		selectionMaxDistance = defaultFocusDistance * selectionMaxDistanceFactor;
	}
	
	/**
	 * Reset viewer settings
	 */
	public void reset()
	{
		/* Camera */
		fieldOfView = initFieldOfView; 						// Field of view
		rotateIncrement = 3.1415f / 256.f;					// Rotation amount each frame when turning
		zoomIncrement = 3.1415f / 32.f;						// Zoom amount each frame when zooming
		nearClippingDistance = 3.f; 						// Distance (m.) of near clipping plane
		nearViewingDistance = nearClippingDistance * 2.f;	// Near distance (m.) at which media start fading out
		farViewingDistance = 12.f; 							// Far distance (m.) at which media start fading out
		
		/* Graphics */
		orientationMode = false;				// Orientation Mode: no simulation of viewer movement (only images fading in and out)
		angleFading = true;						// Do photos fade out as the camera turns away from them?
		visibleAngle = (float)Math.PI / 3.33f;		// Angle within which images and videos become visible
		centeredAngle = visibleAngle / 2.f;		// At what angle is the image centered?
		angleThinning = false;					// Thin images and videos of similar orientation
		thinningAngle = (float)Math.PI / 6.f;		// Angle to thin images and videos within
		alphaTransitionLength = 15;

		/* Video */
		autoPlayVideos = true;				// Automatically play videos near viewer
		autoPlayMaxVideoCount = 2;				// Maximum videos to auto play simultaneously

		/* Sound */
		autoPlaySounds = true;				// Automatically play videos near viewer
		autoPlayMaxSoundCount = 3;			// Maximum videos to auto play simultaneously
		farHearingDistance = 24.f; 			// Far distance (m.) at which media start fading out

		/* Physics */
		lastAttractorDistance = -1.f;
		cameraMass = 0.33f;						// Camera mass for cluster attraction
		velocityMin = 0.00005f;					// Threshold under which velocity counts as zero
		velocityMax = 0.66f;					// Camera maximum velocity
		accelerationMax = 0.15f;				// Camera maximum acceleration
		accelerationMin = 0.00001f;				// Threshold under which acceleration counts as zero
		camDecelInc = 0.66f;					// Camera deceleration increment
		camHaltInc = 0.0033f;						// Camera fast deceleration increment
		walkingAccelInc = 0.002f;				// Camera walking acceleration increment

		/* Movement */
		teleportLength = 30;
		walkingAccelInc = 0.002f;				// Camera walking acceleration increment
		pathWaitLength = initPathWaitLength;
		teleportToFarClusters = true;
		farClusterTeleportDistance = 2000.f;

		/* Turn Settings */
		turningXAccelInc = 0.0001f;
		turningYAccelInc = 0.0001f;

		/* Interaction Modes */
		selection = false;					// Allows selection, increases transparency to make selected image(s) easier to see
		optimizeVisibility = true;			// Optimize visibility automatically
		lockToCluster = false;				// Automatically move viewer to nearest cluster when idle
		multiSelection = false;				// User can select multiple images for stitching
		groupSelection = false;			// Select image segments at a time
		mouseNavigation = false;			// Mouse navigation

		/* Interaction Settings */
		selectionMaxDistanceFactor = 2.f;		// Scaling from defaultFocusDistanceFactor to selectionMaxDistance
		lockToClusterWaitLength = 100;
	
		/* Cluster Settings */
		orientationModeMaxVisibleClusters = 4;					// Maximum visible clusters in Orientation Mode
		orientationModeMinVisibleClusters = 1;							// Maximum visible clusters in Orientation Mode	
	}
	
	/**
	 * @return Current near viewing distance
	 */
	public float getNearViewingDistance()
	{
		return nearViewingDistance;
	}

	/**
	 * @return Current near clipping distance
	 */
	public float getNearClippingDistance()
	{
		return nearClippingDistance;
	}
	
	/**
	 * @return Current far viewing distance
	 */
	public float getFarViewingDistance()
	{
		return farViewingDistance;
	}
}
