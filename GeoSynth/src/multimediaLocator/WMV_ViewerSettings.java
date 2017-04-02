package multimediaLocator;

//import processing.core.PImage;
//import processing.core.PVector;

/*********************************
 * @author davidgordon
 * The current viewer settings
 */
public class WMV_ViewerSettings 
{
//	WMV_Viewer v;
//	public PImage IMAGETEST;
//	public PVector PVECTORTEST = new PVector(0.f, 1.f, 2.f);
//	public PVector PVECTORTEST2 = new PVector(0.1f, 1.1f, 2.1f);

	/* Camera */
	public final float initFieldOfView = (float)Math.PI * 0.375f;	// Camera field of view
	public float fieldOfView = initFieldOfView; 				// Initial camera field of view
	public float rotateIncrement = 3.1415f / 256.f;				// Rotation amount per frame when turning
	public float zoomIncrement = 3.1415f / 32.f;				// Zoom amount per frame when zooming

	public float nearClippingDistance = 3.f; 						// Distance (m.) of near clipping plane
	public float nearViewingDistance = nearClippingDistance * 2.f; 	// Near distance (m.) at which media start fading out
	public float farViewingDistance = 12.f; 						// Far distance (m.) at which media start fading out
	public float userBrightness = 1.f;

	/* Graphics */
	public final float defaultFocusDistance = 9.0f;			// Default focus distance for images and videos (m.)
	public boolean orientationMode = false;				// Orientation Mode: no simulation of viewer movement (only images fading in and out)
	public boolean angleFading = true;					// Do photos fade out as the camera turns away from them?
	public float visibleAngle = (float)Math.PI / 3.33f;		// Angle within which images and videos become visible
	public float centeredAngle = visibleAngle / 2.f;	// At what angle is the image centered?
	public boolean angleThinning = false;				// Thin images and videos of similar orientation
	public float thinningAngle = (float)Math.PI / 6.f;		// Angle to thin images and videos within
	public int alphaTransitionLength = 15;
//	public final int maxVisiblePhotos = 50;					// Maximum visible images at one time
//	public final int maxVisiblePanoramas = 2;				// Maximum visible panoramas at one time
//	public final int maxVisibleVideos = 4;					// Maximum visible videos at one time
	
	public float visibleAngleMax = (float) 3.14, visibleAngleMin = (float) 0.05, visibleAngleInc = (float) 0.04;
	public boolean hideImages = false;						// Hide images
	public boolean hidePanoramas = false;					// Hide panoramas
	public boolean hideVideos = false;						// Hide videos

	/* Video */
	public boolean autoPlayVideos = true;				// Automatically play videos near viewer
	public int autoPlayMaxVideoCount = 2;				// Maximum videos to auto play simultaneously
	
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

	/* Turning */
	final public float turningVelocityMin = 0.00005f;			// Threshold under which velocity counts as zero
	final public float turningVelocityMax = 0.05f;				// Camera maximum velocity
	final public float turningAccelerationMax = 0.005f;			// Camera maximum acceleration
	final public float turningAccelerationMin = 0.000005f;		// Threshold under which acceleration counts as zero
	final public float turningDecelInc = 0.45f;					// Camera deceleration increment
	final public float turningHaltInc = 0.0033f;				// Camera fast deceleration increment
	public float turningXAccelInc = 0.0001f;					// Turning X axis acceleration increment
	public float turningYAccelInc = 0.0001f;					// Turning Y axis acceleration increment

	/* Interaction Modes */
	public boolean selection = false;					// Allows selection, increases transparency to make selected image(s) easier to see
	public boolean optimizeVisibility = false;			// Optimize visibility automatically by turning towards media / changing graphics modes
	public boolean lockToCluster = false;				// Automatically move viewer to nearest cluster when idle
	public boolean multiSelection = false;				// User can select multiple images for stitching
	public boolean segmentSelection = false;			// Select image segments at a time
	public boolean mouseNavigation = false;				// Mouse navigation
	public boolean map3DMode = false;					// 3D Map Mode

	/* Interaction */
	public int mediaDensityThreshold = 12;				// Number of images or videos counted as high density
	public float selectionMaxDistance;					// Maximum distance user can select a photo
	public float selectionMaxDistanceFactor = 2.f;		// Scaling from defaultFocusDistanceFactor to selectionMaxDistance
	public int lockToClusterWaitLength = 100;
	
	/* Clusters */
	public int maxVisibleClusters = 4;							// Maximum visible clusters in Orientation Mode		
	public int minVisibleClusters = 1;							// Maximum visible clusters in Orientation Mode	
	public float orientationModeClusterViewingDistance = nearClippingDistance;	// Distance clusters become visible in Orientation Mode
	public boolean orientationModeForceVisible = false;			// Force <minimum visible clusters> to be seen, even if out of range
	public boolean orientationModeConstantWaitLength = true;	// Wait same length of time even if multiple time segments in one location

	/* Sound */
//	private float audibleFarDistanceMin, audibleFarDistanceMax;
//	private float audibleFarDistanceFadeStart, audibleFarDistanceFadeLength = 40, audibleFarDistanceStartVal, audibleFarDistanceDestVal;
//	private float audibleFarDistanceDiv = (float) 1.5;
//	private boolean audibleFarDistanceTransition = false;
//
//	private float audibleNearDistanceMin, audibleNearDistanceMax;
//	private float audibleNearDistanceFadeStart, audibleNearDistanceFadeLength = 40, audibleNearDistanceStartVal, audibleNearDistanceDestVal;
//	private float audibleNearDistanceDiv = (float) 1.2; 
//	private boolean audibleNearDistanceTransition = false;

	public WMV_ViewerSettings(){}
	
//	public void initialize(PImage NEWTEST)
	public void initialize()
	{
		fieldOfView = initFieldOfView; 		
		selectionMaxDistance = defaultFocusDistance * selectionMaxDistanceFactor;
//		IMAGETEST = NEWTEST;
	}
	
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

		/* Turning */
		turningXAccelInc = 0.0001f;
		turningYAccelInc = 0.0001f;

		/* Interaction Modes */
		selection = false;					// Allows selection, increases transparency to make selected image(s) easier to see
		optimizeVisibility = true;			// Optimize visibility automatically
		lockToCluster = false;				// Automatically move viewer to nearest cluster when idle
		multiSelection = false;				// User can select multiple images for stitching
		segmentSelection = false;			// Select image segments at a time
		mouseNavigation = false;			// Mouse navigation
		map3DMode = false;					// 3D Map Mode		-- Unused
//		videoMode = false;					// Highlights videos by dimming other media types	-- Unused

		/* Interaction */
		selectionMaxDistanceFactor = 2.f;		// Scaling from defaultFocusDistanceFactor to selectionMaxDistance
		lockToClusterWaitLength = 100;
	
		/* Clusters */
		maxVisibleClusters = 2;					// Maximum visible clusters in Orientation Mode
		
//		TESTINTLIST.append(1);
//		TESTINTLIST.append(3);
//		TESTINTLIST.append(2);
		
		/* Sound */
//		audibleFarDistanceFadeLength = 40;
//		audibleFarDistanceDiv = (float) 1.5;
//		audibleFarDistanceTransition = false;
//
//		audibleNearDistanceFadeLength = 40;
//		audibleNearDistanceDiv = (float) 1.2; 
//		audibleNearDistanceTransition = false;
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
