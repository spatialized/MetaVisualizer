package wmViewer;

import processing.core.PApplet;

/*********************************
 * @author davidgordon
 * Camera graphics and physics model settings
 */
public class WMV_ViewerSettings 
{
	WMV_Viewer v;
	
	/* Camera */
	public final float initFieldOfView = PApplet.PI * 0.375f;	// Camera field of view
	public float fieldOfView = initFieldOfView; 				// Initial camera field of view
	public float rotateIncrement = 3.1415f / 256.f;				// Rotation amount per frame when turning
	public float zoomIncrement = 3.1415f / 32.f;				// Zoom amount per frame when zooming

	public float nearClippingDistance = 3.f; 						// Distance (m.) of near clipping plane
	public float nearViewingDistance = nearClippingDistance * 2.f; 	// Near distance (m.) at which media start fading out
	public float farViewingDistance = 12.f; 						// Far distance (m.) at which media start fading out
	public float userBrightness = 1.f;

	/* Graphics */
	public boolean orientationMode = false;				// Orientation Mode: no simulation of viewer movement (only images fading in and out)
	public boolean angleFading = true;					// Do photos fade out as the camera turns away from them?
	public float visibleAngle = PApplet.PI / 3.33f;		// Angle within which images and videos become visible
	public float centeredAngle = visibleAngle / 2.f;	// At what angle is the image centered?
	public boolean angleThinning = false;				// Thin images and videos of similar orientation
	public float thinningAngle = PApplet.PI / 6.f;		// Angle to thin images and videos within

	/* Physics */
	public float lastAttractorDistance = -1.f;
	public float cameraMass = 0.33f;						// Camera mass for cluster attraction
	public float velocityMin = 0.00005f;					// Threshold under which velocity counts as zero
	public float velocityMax = 0.66f;						// Camera maximum velocity
	public float accelerationMax = 0.15f;					// Camera maximum acceleration
	public float accelerationMin = 0.00001f;				// Threshold under which acceleration counts as zero
	public float camDecelInc = 0.75f;						// Camera deceleration increment
	public float camHaltInc = 0.01f;						// Camera fast deceleration increment

	/* Movement */
	public float walkingAccelInc = 0.002f;				// Camera walking acceleration increment

	/* Turning */
	final public float turningVelocityMin = 0.00005f;					// Threshold under which velocity counts as zero
	final public float turningVelocityMax = 0.08f;						// Camera maximum velocity
	final public float turningAccelerationMax = 0.008f;					// Camera maximum acceleration
	final public float turningAccelerationMin = 0.000005f;				// Threshold under which acceleration counts as zero
	final public float turningDecelInc = 0.45f;						// Camera deceleration increment
	final public float turningHaltInc = 0.0033f;						// Camera fast deceleration increment
	public float turningXAccelInc = 0.0001f;
	public float turningYAccelInc = 0.0001f;

	/* Interaction Modes */
	public boolean mouseNavigation = false;				// Mouse navigation
	public boolean map3DMode = false;					// 3D Map Mode
	public boolean selection = false;					// Allows selection, increases transparency to make selected image(s) easier to see
	public boolean optimizeVisibility = true;			// Optimize visibility automatically
	public boolean lockToCluster = false;				// Automatically move viewer to nearest cluster when idle
	public boolean multiSelection = false;				// User can select multiple images for stitching
	public boolean segmentSelection = false;			// Select image segments at a time
	public boolean videoMode = false;					// Highlights videos by dimming other media types	-- Unused

	/* Interaction */
	public int mediaDensityThreshold = 12;				// Number of images or videos counted as high density
	public float selectionMaxDistance;					// Maximum distance user can select a photo
	public float selectionMaxDistanceFactor = 2.f;		// Scaling from defaultFocusDistanceFactor to selectionMaxDistance
	public int lockToClusterWaitLength = 100;

	/* Clusters */
	public int maxVisibleClusters = 2;					// Maximum visible clusters in Orientation Mode		-- Normal Mode too!

	public WMV_ViewerSettings(WMV_Viewer parent)
	{
		v = parent;
		fieldOfView = initFieldOfView; 		
	}
	
	public void initialize()
	{
		fieldOfView = initFieldOfView; 		
		selectionMaxDistance = v.p.defaultFocusDistance * selectionMaxDistanceFactor;
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

		visibleAngle = PApplet.PI / 3.33f;		// Angle within which images and videos become visible
		centeredAngle = visibleAngle / 2.f;		// At what angle is the image centered?
		angleThinning = false;					// Thin images and videos of similar orientation
		thinningAngle = PApplet.PI / 6.f;		// Angle to thin images and videos within

		/* Physics */
		lastAttractorDistance = -1.f;
		cameraMass = 0.33f;						// Camera mass for cluster attraction
		velocityMin = 0.00005f;					// Threshold under which velocity counts as zero
		velocityMax = 0.66f;					// Camera maximum velocity
		accelerationMax = 0.15f;				// Camera maximum acceleration
		accelerationMin = 0.00001f;				// Threshold under which acceleration counts as zero
		camDecelInc = 0.75f;					// Camera deceleration increment
		camHaltInc = 0.01f;						// Camera fast deceleration increment
		walkingAccelInc = 0.002f;				// Camera walking acceleration increment

		/* Turning */
		turningXAccelInc = 0.0001f;
		turningYAccelInc = 0.0001f;

		/* Interaction Modes */
		mouseNavigation = false;			// Mouse navigation
		map3DMode = false;					// 3D Map Mode
		selection = false;					// Allows selection, increases transparency to make selected image(s) easier to see
		optimizeVisibility = true;			// Optimize visibility automatically
		lockToCluster = false;				// Automatically move viewer to nearest cluster when idle
		multiSelection = false;				// User can select multiple images for stitching
		segmentSelection = false;			// Select image segments at a time
		videoMode = false;					// Highlights videos by dimming other media types	-- Unused

		/* Interaction */
		selectionMaxDistanceFactor = 2.f;		// Scaling from defaultFocusDistanceFactor to selectionMaxDistance
		lockToClusterWaitLength = 100;
	
		/* Clusters */
		maxVisibleClusters = 2;					// Maximum visible clusters in Orientation Mode
	}
}