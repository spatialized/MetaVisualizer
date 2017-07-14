package main.java.com.entoptic.multimediaLocator;

import java.util.ArrayList;
import java.util.List;

import processing.core.PVector;

/*********************************
 * Viewer state
 * @author davidgordon
 */
public class WMV_ViewerState 
{
	/* Display */
	public int lastDisplayView = 0;						// Last Display View (before switching to Media View)
	
	/* Time */
	public boolean firstRunningFrame = false;			// Flag indicating first World simulation frame
	public int currentFieldTimeSegment = 0;				// Current time segment in field timeline
	public int currentFieldTimeSegmentOnDate = 0;		// Current time segment in field timelines
	public int currentFieldDate = 0;					// Current date in field dateline
	
	public int currentMedia = -1;						// In Single Time Mode, media index currently visible
	public int currentMediaStartTime = 100000;			// In Single Time Mode, frame at which next media in timeline becomes current
	public int nextMediaStartFrame = 100000;			// In Single Time Mode, frame at which next media in timeline becomes current
	public boolean followCurrentMediaInTime = false;	// In Single Time Mode, whether to turn and look at current media  -- bugs
	public int nearbyClusterTimelineMediaCount = 0;		// Number of media in nearbyClusterTimeline
	
	/* Navigation */
	public boolean movingToAttractor = false;			// Moving to attractor point anywhere in field
	public boolean movingToCluster = false;				// Moving to cluster 
	public boolean movingToTimeSegment = false;			// Moving / teleporting to target time segment
	public int timeSegmentTarget = -1;					// Field time segment goal			

	/* Clusters */
	public ArrayList<WMV_Orientation> clusterOrientations;	// List of viewer orientations at each cluster 
	public List<Integer> clustersVisibleInOrientationMode;				// Clusters visible to viewer in Orientation Mode
	private int field = 0;								// Current field
	public int currentCluster = 0;						// Cluster currently in view
	public boolean atCurrentCluster = false;			// Whether viewer is centered at current cluster (false if has moved)
	public int lastCluster = -1;						// Last cluster visited

	public int teleportGoalCluster = -1;				// Cluster to navigate to (-1 == none)
	public int attractorCluster = -1;					// ID of single cluster currently attracting viewer
	public int attractionStart = 0;						// Frame when current attractor began attracting viewer
	
	public float clusterNearDistance;					// Distance from cluster center to slow down to prevent missing the target
	public float clusterNearDistanceFactor = 2.f;		// Multiplier for clusterCenterSize to get clusterNearDistance
	
	/* Path Navigation */
	public boolean following = false;					// Is the camera currently navigating from memory
	public PVector pathGoal;							// Next goal point for camera in navigating from memory
	public int pathLocationIdx;							// Index of current cluster in memory
	public int pathWaitStartFrame;								// Frame viewer started waiting to move while following a path
	public boolean pathWaiting = false;					// Whether to stop wait at each waypoint when moving along a path
//	public boolean continueAtAttractor = false;			// Whether current attractor is a waypoint along a path	-- Disabled
	
	/* GPS Track Navigation */
	public boolean followingGPSTrack = false;					// Moving to precise center from a point less than clusterCenterSize
	public String gpsTrackName = "";								// Selected GPS track name
	public int gpsTrackID = -1;									// Selected GPS track ID
	public PVector gpsTrackGoal;									// Next goal point for camera in navigating from memory
	public int gpsTrackLocationIdx;								// Index of current cluster in memory
	public int gpsTrackTransitionLength;							// Frame length of GPS track transition
	public PVector gpsTrackStartLocation;						// Distance covered over one frame during GPS track transition 
	public float gpsTransitionLengthDistanceFactor = 16.f;		// Conversion factor to GPS track transition length from goal point distance
	public int gpsTrackTransitionStart, gpsTrackTransitionEnd;	// GPS track transition start / end frame
	
	/* Teleporting */
	public boolean navigationTeleport = false;		// Teleport when following navigation commands
	public boolean followTeleport = false;			// Teleport when in Path Navigation Mode
	public PVector teleportGoal;					// Coordinates of teleport goal
	public boolean ignoreTeleportGoal = false;		// Ignore teleport goal when teleporting (for setting location manually)
	public boolean teleporting = false;				// Transition where all images fade in or out
	public int teleportStart;						// Frame that fade transition started
	public int teleportToField = -1;				// What field ID to fade transition to	 (-1 remains in current field)
	public int teleportWaitingCount = 0;			// How long has the viewer been waiting for media to fade out before teleport?
	
	/* Physics */
	public PVector location;		// Viewer location in virtual space
	public PVector orientation;		// Viewer orientation {azimuth, elevation, roll} in radians
	public PVector target;			// Viewer target point
	public PVector velocity;		// Viewer velocity
	public PVector acceleration;	// Viewer acceleration
	public PVector attraction;      // Physics model parameters
	public PVector orientationVector;
	
	/* Movement */
	private int followMode = 0;				// {0: Timeline 1: GPS Track 2: Memory}
	public boolean walking = false;			// Whether viewer is walking
	public PVector walkingVelocity;
	public PVector walkingAcceleration;			// Physics parameters applied relative to camera direction

	public boolean slowing = false;			// Whether viewer is slowing 
	public boolean slowingX = false;			// Slowing X movement
	public boolean slowingY = false;			// Slowing Y movement
	public boolean slowingZ = false;			// Slowing Z movement
	public boolean halting = false;			// Viewer is halting
	
	public boolean movingX = false;			// Whether viewer is automatically moving in X dimension (side to side)
	public boolean movingY = false;			// Whether viewer is automatically moving in Y dimension (up or down)
	public boolean movingZ = false;			// Whether viewer is automatically moving in Z dimension (forward or backward)
	
	public float moveXDirection;									// 1 (right)   or -1 (left)
	public float moveYDirection;									// 1 (down)    or -1 (up)
	public float moveZDirection;									// 1 (forward) or -1 (backward)
	public boolean movingNearby = false;							// Moving to center from a point less than nearClusterDistance but greater than clusterCenterSize
	public boolean centering = false;							// Moving to precise center from a point less than clusterCenterSize
	public final int centeringTransitionLength = 20;				// Frame length of centering transition
	public int centeringTransitionStart, centeringTransitionEnd;	// Centering transition start / end frame
	public boolean waiting = false;								// Whether viewer is waiting to move while following a path

	/* Turning */
	public PVector turningVelocity;			// Turning velocity in X direction
	public PVector turningAcceleration;		// Turning acceleration in X direction
	public PVector turningMediaGoal;			// Media item to turn towards
	public boolean turningX = false;			// Whether the viewer is turning (right or left)
	public boolean turningY = false;			// Whether the viewer is turning (up or down)
	public boolean turnSlowingX = false;				// Slowing turn in X direction
	public boolean turnSlowingY = false;				// Slowing turn in Y direction
	public boolean turnHaltingX = false;				// Slowing turn in X direction
	public boolean turnHaltingY = false;				// Slowing turn in Y direction

	public int turnXStartFrame, turnYStartFrame, 
				turnXTargetFrame, turnYTargetFrame;
	public float turnXDirection, turnXTarget, 
				  turnXStart, turnXIncrement;
	public float turnYDirection, turnYTarget,
				  turnYStart, turnYIncrement;

	final public float turningNearDistance = 3.1415f / 12.f;
	final public float turningCenterSize = 3.1415f / 54.f;
	
	public boolean rotatingX = false;			// Whether the camera is rotating in X dimension (turning left or right)?
	public boolean rotatingY = false;			// Whether the camera is rotating in Y dimension (turning up or down)?
	public boolean rotatingZ = false;			// Whether the camera is rotating in Z dimension (rolling left or right)?
	public float rotateXDirection;				// Rotation direction in X dimension
	public float rotateYDirection;				// Rotation direction in Y dimension
	public float rotateZDirection;				// Rotation direction in Z dimension

	/* Interaction */
	public int lastMovementFrame = 500000, lastLookFrame = 500000;
	public int clusterLockIdleFrames = 0;				// How long to wait after user input before auto navigation moves the camera?
	
	/* Zooming */
	public boolean zooming = false;
	public float zoomStart, zoomDirection;
	public int zoomLength = 15;

	/**
	 * Constructor for viewer state
	 */
	public WMV_ViewerState()
	{
		clusterOrientations = new ArrayList<WMV_Orientation>();

		location = new PVector(0,0,0);
		velocity = new PVector(0,0,0);
		acceleration = new PVector(0,0,0);
		attraction = new PVector(0,0,0);
		walkingVelocity = new PVector(0,0,0);
		walkingAcceleration = new PVector(0,0,0);
		turningVelocity = new PVector(0,0,0);
		turningAcceleration = new PVector(0,0,0);

		teleportGoal = new PVector(0, 0, 0);
		field = 0;
		currentCluster = 0;
	}
	
	/**
	 * Reset viewer state
	 */
	public void reset()
	{
		lastDisplayView = 0;
		clusterOrientations = new ArrayList<WMV_Orientation>();

		currentFieldTimeSegment = 0;			// Current time segment in field timeline
		currentFieldTimeSegmentOnDate = 0;		// Current time segment in field timelines
		currentFieldDate = 0;					// Current date in field dateline
		
		currentMedia = -1;						// In Single Time Mode, media index currently visible
		currentMediaStartTime = 100000;			// In Single Time Mode, frame at which next media in timeline becomes current
		nextMediaStartFrame = 100000;			// In Single Time Mode, frame at which next media in timeline becomes current
		followCurrentMediaInTime = false;				// In Single Time Mode, whether to turn and look at current media  -- bugs
		nearbyClusterTimelineMediaCount = 0;	// Number of media in nearbyClusterTimeline
	}

	/**
	 * Save viewer orientation at given cluster
	 * @param clusterID Cluster ID
	 * @param direction Viewer direction at cluster
	 * @param elevation Viewer elevation at cluster
	 * @param rotation Viewer rotation at cluster
	 */
	public void saveClusterOrientation( int clusterID, float direction, float elevation, float rotation )
	{
		WMV_Orientation newOrientation = new WMV_Orientation(clusterID, direction, elevation, rotation);
		clusterOrientations.add(newOrientation);
//		System.out.println("ViewerState.saveClusterOrientation()... Saved viewer orientation for cluster #"+clusterID);
	}

	public WMV_Orientation getClusterOrientation(int clusterID)
	{
		for(WMV_Orientation o : clusterOrientations)
		{
			if(o.getID() == clusterID)
				return o;
		}
		
		return null;
	}

	public ArrayList<WMV_Orientation> getClusterOrientations()
	{
		return clusterOrientations;
	}

	public void clearClusterOrientations()
	{
		clusterOrientations = new ArrayList<WMV_Orientation>();
	}

	public PVector getLocation()
	{
		return location;
	}
	
	/**
	 * @return Current camera orientation as a directional unit vector
	 */
	public PVector getOrientationVector()
	{
		return orientationVector;
	}
	
	/**
	 * @return Index of current cluster
	 */
	public int getCurrentClusterID()
	{
		return currentCluster;
	}
	
	public int getCurrentMediaStartTime()
	{
		return currentMediaStartTime;
	}

	/**
	 * @return Whether the viewer is teleporting
	 */
	public boolean isTeleporting()
	{
		return teleporting;
	}
	
	/**
	 * @return Whether the viewer is moving to a cluster or attractor
	 */
	public boolean isMoving()
	{
		if(movingToCluster || movingToAttractor)
			return true;
		else
			return false;
	}

	/**
	 * @return Whether the viewer is moving to an attractor
	 */
	public boolean isMovingToAttractor()
	{
		return movingToAttractor;
	}

	/**
	 * @return Whether the viewer is moving to a cluster
	 */
	public boolean isMovingToCluster()
	{
		return movingToCluster;
	}

	/**
	 * @return Current field ID
	 */
	public void setCurrentFieldID( int newFieldID )
	{
		field = newFieldID;
	}

	/**
	 * @return Current field ID
	 */
	public int getCurrentFieldID()
	{
		return field;
	}
	
	public List<Integer> getClustersVisible()
	{
		return clustersVisibleInOrientationMode;
	}
	
	/**
	 * @return Current distance at which a cluster is considered nearby
	 */
	public float getClusterNearDistance()
	{
		return clusterNearDistance;
	}
	
	/**
	 * @return Current Path Following Mode
	 */
	public void setFollowMode(int newMode)
	{
		followMode = newMode;
	}
	
	/**
	 * @return Current Path Following Mode
	 */
	public int getFollowMode()
	{
		return followMode;
	}
}
