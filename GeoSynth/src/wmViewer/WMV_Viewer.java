package wmViewer;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import damkjer.ocd.Camera;
//import picking.Picker;
import processing.core.PApplet;
//import peasy.PeasyCam;
//import processing.core.PApplet;
import processing.core.PVector;
import processing.data.FloatList;
import processing.data.IntList;

/*********************************
 * @author davidgordon
 * Represents the viewer able to navigate and interact with multimedia in virtual 3D environment
 */
public class WMV_Viewer 
{
	/* Camera */
	Camera camera;									 				// Camera object
	private float fieldOfView = PApplet.PI * 0.375f;				// Field of view

	private final float initFieldOfView = fieldOfView; 				// Initial field of view
	private float rotateIncrement = 3.1415f / 256.f;				// Rotation amount each frame when turning
	private float zoomIncrement = 3.1415f / 32.f;					// Zoom amount each frame when zooming

	private float nearClippingDistance = 3.f; 						// Distance (m.) of near clipping plane
	private float nearViewingDistance = nearClippingDistance * 2.f; // Near distance (m.) at which media start fading out
	private float farViewingDistance = 12.f; 						// Far distance (m.) at which media start fading out
	public float userBrightness = 1.f;
	
	/* Time */
	public int currentFieldDate = 0;					// Current date in field dateline
	public int currentClusterDate = 0;					// Current date segment in cluster dateline
	public int currentFieldTimeSegment = 0;				// Current time segment in field timeline
	public int currentClusterTimeSegment = 0;			// Current time segment in cluster timeline
	
	public int currentMedia = -1;						// In Single Time Mode, media index currently visible
	public int nextMediaStartFrame = 100000;			// In Single Time Mode, frame at which next media in timeline becomes current
	public int currentMediaStartTime = 100000;			// In Single Time Mode, frame at which next media in timeline becomes current
	public boolean lookAtCurrentMedia = false;			// In Single Time Mode, whether to turn and look at current media  -- bugs
	ArrayList<WMV_TimeSegment> nearbyClusterTimeline;	// Combined timeline of nearby (visible) clusters
	public int nearbyClusterTimelineMediaCount = 0;		// Media count in nearbyClusterTimeline
	
	/* Memory */
	private ArrayList<WMV_Waypoint> memory;				// Path for camera to take
	private ArrayList<WMV_Waypoint> path; 				// Record of camera path
	
	private boolean movingToAttractor = false;			// Moving to attractor point anywhere in field
	private boolean movingToCluster = false;			// Moving to cluster 
	private PVector pathGoal;							// Next goal point for camera in navigating from memory
	private boolean following = false;					// Is the camera currently navigating from memory?
	private int pathLocationIdx;						// Index of current cluster in memory
	private boolean movingToTimeSegment = false;		// Moving / teleporting to target time segment
	private int timeSegmentTarget = -1;					// Field time segment goal			

	/* Clusters */
	private int field = 0;								// Current field
	public IntList clustersVisible;						// Clusters visible to camera in Orientation Mode
	public int maxVisibleClusters = 2;					// Maximum visible clusters in Orientation Mode
	private int currentCluster = 0;						// Cluster currently in view
	private int lastCluster = -1;						// Last cluster visited
	WMV_Cluster attractorPoint;							// For navigation to points outside cluster list
	private int attractorCluster = -1;					// Cluster attracting the camera
	private int attractionStart = 0;
	private int teleportGoalCluster = -1;				// Cluster to navigate to (-1 == none)
	private float clusterNearDistance;					// Distance from cluster center to slow down to prevent missing the target
	private float clusterNearDistanceFactor = 2.f;		// Multiplier for clusterCenterSize to get clusterNearDistance

	/* Sound */
	float audibleFarDistanceMin, audibleFarDistanceMax;
	float audibleFarDistanceFadeStart, audibleFarDistanceFadeLength = 40, audibleFarDistanceStartVal, audibleFarDistanceDestVal;
	float audibleFarDistanceDiv = (float) 1.5;
	boolean audibleFarDistanceTransition = false;

	float audibleNearDistanceMin, audibleNearDistanceMax;
	float audibleNearDistanceFadeStart, audibleNearDistanceFadeLength = 40, audibleNearDistanceStartVal, audibleNearDistanceDestVal;
	float audibleNearDistanceDiv = (float) 1.2; 
	boolean audibleNearDistanceTransition = false;

	/* Interaction Modes */
	public boolean mouseNavigation = false;			// Mouse navigation
	public boolean map3DMode = false;				// 3D Map Mode
	public boolean selection = false;				// Allows selection, increases transparency to make selected image(s) easier to see
	public boolean optimizeVisibility = true;		// Optimize visibility automatically
	public boolean lockToCluster = false;			// Automatically move viewer to nearest cluster when idle
	public boolean multiSelection = false;			// User can select multiple images for stitching
	public boolean segmentSelection = false;		// Select image segments at a time
	public boolean videoMode = false;				// Highlights videos by dimming other media types	-- Unused
	
	/* Teleporting */
	public boolean movementTeleport = false;		// Teleport when following navigation commands
	private PVector teleportGoal;					// Coordinates of teleport goal
	private boolean teleporting = false;			// Transition where all images fade in or out
	private int teleportStart;						// Frame that fade transition started
	private int teleportToField = -1;				// What field ID to fade transition to	 (-1 remains in current field)
	private int teleportWaitingCount = 0;			// How long has the viewer been waiting for media to fade out before teleport?
	
	/* Physics */
	private PVector location, orientation;					// Location of the camera in virtual space
	public PVector velocity, acceleration, attraction;      // Physics model parameters

	public float lastAttractorDistance = -1.f;
	public float cameraMass = 0.33f;						// Camera mass for cluster attraction
	private float velocityMin = 0.00005f;					// Threshold under which velocity counts as zero
	private float velocityMax = 0.66f;						// Camera maximum velocity
	private float accelerationMax = 0.15f;					// Camera maximum acceleration
	private float accelerationMin = 0.00001f;				// Threshold under which acceleration counts as zero
	private float camDecelInc = 0.75f;						// Camera deceleration increment
	private float camHaltInc = 0.01f;						// Camera fast deceleration increment
	private boolean waiting = false;						// Whether the camera is waiting to move while following a path
	private int pathWaitStartFrame, pathWaitLength = 100;

	/* Movement */
	public int followMode = 0;					// 0: Timeline 1: GPS Track 2: Memory
	private boolean walking = false;			// Whether viewer is walking
	public PVector walkingVelocity;
	public PVector walkingAcceleration;			// Physics parameters applied relative to camera direction
	private float walkingAccelInc = 0.002f;		// Camera walking acceleration increment

	private boolean slowing = false;			// Whether viewer is slowing 
	private boolean slowingX = false;			// Slowing X movement
	private boolean slowingY = false;			// Slowing Y movement
	private boolean slowingZ = false;			// Slowing Z movement
	private boolean halting = false;			// Viewer is halting
	
	private boolean movingX = false;			// Is viewer automatically moving in X dimension (side to side)?
	private boolean movingY = false;			// Is viewer automatically moving in Y dimension (up or down)?
	private boolean movingZ = false;			// Is viewer automatically moving in Z dimension (forward or backward)?
	private float moveXDirection;				// 1 (right)   or -1 (left)
	private float moveYDirection;				// 1 (down)    or -1 (up)
	private float moveZDirection;				// 1 (forward) or -1 (backward)
	private boolean movingNearby = false;		// Moving to a point within nearClusterDistance

	/* Turning */
	public PVector turningMediaGoal;			// Media item to turn towards
	public boolean turningX = false;			// Whether the viewer is turning (right or left)
	public boolean turningY = false;			// Whether the viewer is turning (up or down)
	private PVector turningVelocity;			// Turning velocity in X direction
	private PVector turningAcceleration;		// Turning acceleration in X direction
	private float turningXAccelInc = 0.0001f;
	private float turningYAccelInc = 0.0001f;

	final private float turningVelocityMin = 0.00005f;					// Threshold under which velocity counts as zero
	final private float turningVelocityMax = 0.08f;						// Camera maximum velocity
	final private float turningAccelerationMax = 0.008f;					// Camera maximum acceleration
	final private float turningAccelerationMin = 0.000005f;				// Threshold under which acceleration counts as zero
	final private float turningDecelInc = 0.45f;						// Camera deceleration increment
	final private float turningHaltInc = 0.0033f;						// Camera fast deceleration increment
	private boolean turnSlowingX = false;				// Slowing turn in X direction
	private boolean turnSlowingY = false;				// Slowing turn in Y direction
	private boolean turnHaltingX = false;				// Slowing turn in X direction
	private boolean turnHaltingY = false;				// Slowing turn in Y direction

	public int turnXStartFrame, turnYStartFrame, 
				turnXTargetFrame, turnYTargetFrame;
	public float turnXDirection, turnXTarget, 
				  turnXStart, turnXIncrement;
	public float turnYDirection, turnYTarget,
				  turnYStart, turnYIncrement;
//	public float turnIncrement = PApplet.PI / 240.f;
	final private float turningNearDistance = PApplet.PI / 12.f;
	final private float turningCenterSize = PApplet.PI / 54.f;
	
	private boolean rotatingX = false;			// Whether the camera is rotating in X dimension (turning left or right)?
	private boolean rotatingY = false;			// Whether the camera is rotating in Y dimension (turning up or down)?
	private boolean rotatingZ = false;			// Whether the camera is rotating in Z dimension (rolling left or right)?
	private float rotateXDirection;				// Rotation direction in X dimension
	private float rotateYDirection;				// Rotation direction in Y dimension
	private float rotateZDirection;				// Rotation direction in Z dimension

	/* Interaction */
	public int mediaDensityThreshold = 12;		// Number of images or videos counted as high density
	private float selectionMaxDistance;					// Maximum distance user can select a photo
	private float selectionMaxDistanceFactor = 2.f;		// Scaling from defaultFocusDistanceFactor to selectionMaxDistance
	public int lastMovementFrame = 500000, lastLookFrame = 500000;
	private int clusterLockIdleFrames = 0;				// How long to wait after user input before auto navigation moves the camera?
	private int lockToClusterWaitLength = 100;

	/* GPS Tracks */
	private File gpsTrackFile;							// GPS track file
	private ArrayList<WMV_Waypoint> history;			// Stores a GPS track in virtual coordinates
	private ArrayList<WMV_Waypoint> gpsTrack;			// Stores a GPS track in virtual coordinates
	private boolean gpsTrackSelected = false;			// Has a GPS track been selected?
	private String gpsTrackName = "";					// GPS track name

	/* Zooming */
	private boolean zooming = false;
	private float zoomStart, zoomDirection;
	private int zoomLength = 15;

	WMV_World p;

	public WMV_Viewer(WMV_World parent)
	{
		p = parent;

		location = new PVector(0,0,0);
		velocity = new PVector(0,0,0);
		acceleration = new PVector(0,0,0);
		attraction = new PVector(0,0,0);
		walkingVelocity = new PVector(0,0,0);
		walkingAcceleration = new PVector(0,0,0);
		turningVelocity = new PVector(0,0,0);
		turningAcceleration = new PVector(0,0,0);

		fieldOfView = initFieldOfView; 		// Field of view

		history = new ArrayList<WMV_Waypoint>();
		gpsTrack = new ArrayList<WMV_Waypoint>();

		memory = new ArrayList<WMV_Waypoint>();
		path = new ArrayList<WMV_Waypoint>();
		teleportGoal = new PVector(0, 0, 0);

		field = 0;
		currentCluster = 0;
		clusterNearDistance = p.clusterCenterSize * clusterNearDistanceFactor;
		nearbyClusterTimeline = new ArrayList<WMV_TimeSegment>();
		
		initialize(0, 0, 0);
	}

	/** 
	 * Initialize camera at a given virtual point
	 * @param x Initial X coordinate
	 * @param y Initial Y coordinate
	 * @param z Initial Z coordinate
	 */
	public void initialize(float x, float y, float z)
	{
		camera = new Camera( p.p, x, y, z, 0.f, 0.f, 0.f, 0.f, 1.f, 0.f, fieldOfView, nearClippingDistance, 10000.f);
		location = new PVector(x, y, z);
		teleportGoal = new PVector(x, y, z);
		
		fieldOfView = initFieldOfView;

		selectionMaxDistance = p.defaultFocusDistance * selectionMaxDistanceFactor;
		clustersVisible = new IntList();
	}

	/*** 
	 * Update camera movement and variables each frame
	 */
	void update()
	{
		location = new PVector(camera.position()[0], camera.position()[1], camera.position()[2]);		/* Update location */

		updateWalking();							/* Update walking */
		updatePhysics();							/* Update physics */
		
		if(teleporting) updateTeleporting();		/* Update teleporting */
		updateMovement();							/* Update navigation */
		if(turningX || turningY) updateTurning();	/* Update turning */
//		if(autoNavigation) updateLooking();			/* Update looking */

		p.getCurrentField().getAttractingClusters().size();
		
		if(p.getTimeMode() == 2 && ( isMoving() || isFollowing() || isWalking() ))
			p.createTimeCycle();
		if(p.timeCycleLength == -1 && p.p.frameCount % 10 == 0.f)
			p.createTimeCycle();

		if(lockToCluster && !walking)										// Update locking to nearest cluster 
		{
			if(p.getCurrentField().getAttractingClusters().size() > 0)		// If being attracted to a point
			{
				if(clusterLockIdleFrames > 0) clusterLockIdleFrames = 0;											
			}
			else															// If idle
			{
				clusterLockIdleFrames++;									// Count frames with no attracting clusters
				if(clusterLockIdleFrames > lockToClusterWaitLength)			// If after wait length, lock to nearest cluster
				{
					int nearest = getNearestCluster(true);					// Get nearest cluster location, including current cluster
					WMV_Cluster c = p.getCluster(nearest);
					if(c.getClusterDistance() > p.clusterCenterSize * 2.f)	// If the nearest cluster is farther than threshold distance
						moveToCluster(c.getID(), false);					// Move to nearest cluster
				}
			}
		}

		if(mouseNavigation)
		{
			if(lastMovementFrame-p.p.frameCount > 60)			// Start following memory path if idle for a few seconds
			{
				if(!isFollowing() && memory.size() > 0)
				{
					followMemory();
				}
			}
		}
		
		if(p.orientationMode)
		{
			clustersVisible = new IntList();
			
			for(WMV_Cluster c : p.getCurrentField().clusters)
			{
				if(!c.isEmpty())
					if(c.getLocation().dist(location) < farViewingDistance)
						clustersVisible.append(c.getID());
			}
			
			if(clustersVisible.size() > maxVisibleClusters)					// Show only closest clusters
			{
				IntList allClusters = clustersVisible;
				clustersVisible = new IntList();

				for(int i=0; i<allClusters.size(); i++)
				{
					if(clustersVisible.size() < maxVisibleClusters)
					{
						clustersVisible.append(i);
					}
					else
					{
						WMV_Cluster c = p.getCurrentField().clusters.get(i);
						float cDist = c.getLocation().dist(location);
						float largest = -10000;
						int largestIdx = -1;
						int count = 0;
						
						for(int n : clustersVisible)		// Find largest
						{
							WMV_Cluster v = p.getCurrentField().clusters.get(n);
							float vDist = v.getLocation().dist(location);
							if(vDist > largest)
							{
								largest = vDist;
								largestIdx = count;

								count++;
							}
						}
						
						if(cDist < largest)					// Remove largest and add new index
						{
							int res = clustersVisible.remove(largestIdx);
							clustersVisible.append(i);
						}
					}
				}
			}
		}
		
		/* Aim camera */
//		if(movingToAttractor)
//			camera.aim(attractorPoint.getLocation().x, attractorPoint.getLocation().y, attractorPoint.getLocation().z);
//		if(movingToCluster)
//		{
//			GMV_Cluster c = p.getCurrentCluster();
//			camera.aim(c.getLocation().x, c.getLocation().y, c.getLocation().z);
//		}
	}

	/**
	 * Move camera forward
	 */
	public void walkForward()
	{
		moveZDirection = -1;
		movingZ = true;
		slowingZ = false;
	}

	/**
	 * Slow viewer movement along active walking axes
	 */
	public void walkSlower()
	{
		if(movingX)
		{
			movingX = false;
			slowingX = true;
		}
		if(movingY)
		{
			movingY = false;
			slowingY = true;
		}
		if(movingZ)
		{
			movingZ = false;
			slowingZ = true;
		}
	}

	/**
	 * Teleport to given point in 3D virtual space
	 * @param dest	Destination point in world coordinates
	 * @param fadeTransition  Use fade transition (true) or jump (false)
	 */
	public void teleportToPoint( PVector dest, boolean fadeTransition ) 
	{
//		if(p.orientationMode)
//		{
//			teleportGoal = dest;
//			location = dest;
//		}
//		else
		{
			if(fadeTransition)
			{
				teleportGoal = dest;
				startTeleport(-1);
			}
			else
			{
				camera.jump(dest.x, dest.y, dest.z);
			}
		}
	}	

	/**
	 * Set specified field as current field
	 * @param newField  Field to set as current
	 */
	public void setCurrentField(int newField)		
	{
		if(newField < p.getFieldCount())
			field = newField;

		p.display.map2D.initializeMaps();

		if(p.p.debug.field || p.p.debug.viewer)		
			p.display.message("Set new field:"+field);

		initialize(0,0,0);							// Initialize camera

		if(p.p.debug.field || p.p.debug.viewer)		
			p.display.message("Moving (teleporting) to nearest cluster:"+field);

		moveToNearestCluster(true);					// Teleport to new location						
	}

	/**
	 * Go to the given image capture location
	 * @param teleport  Whether to teleport (true) or navigate (false)
	 */
	void moveToImageCaptureLocation(int imageID, boolean teleport) 
	{
		if (p.p.debug.viewer)
			p.display.message("Moving to capture location... "+imageID);

		PVector newLocation = p.getFieldImages().get(imageID).getCaptureLocation();
		
		if(teleport)
		{
			teleportToPoint(newLocation, true);
		}
		else
		{
			if(teleporting)	teleporting = false;
			if(p.p.debug.viewer && p.p.debug.detailed)
				p.display.message("moveToCaptureLocation... setting attractor point:"+newLocation);
			setAttractorPoint(newLocation);
		}
	}
	
	/**
	 * Go to the specified cluster
	 * @param newCluster Destination cluster ID
	 * @param teleport Whether to teleport (true) or move (false)
	 */
	void moveToCluster(int newCluster, boolean teleport) 
	{
		if(teleport)
		{
			teleportGoalCluster = newCluster;
			PVector newLocation = ((WMV_Cluster) p.getCurrentField().clusters.get(newCluster)).getLocation();
			teleportToPoint(newLocation, true);
		}
		else
		{
			if(teleporting)	teleporting = false;
			if(p.p.debug.viewer)
				p.display.message("Moving to nearest cluster... setting attractor:"+newCluster);
			setAttractorCluster( newCluster );
		}
	}
		
	/**
	 * Go to the nearest cluster
	 * @param teleport  Whether to teleport (true) or move (false)
	 */
	void moveToNearestCluster(boolean teleport) 
	{
		int nearest = getNearestCluster(false);		
		
		if (p.p.debug.viewer)
			p.display.message("Moving to nearest cluster... "+nearest+" from current:"+currentCluster);

		if(teleport)
		{
			teleportToCluster(nearest, true);
		}
		else
		{
			if(teleporting)	teleporting = false;
			if(p.p.debug.viewer)
				p.display.message("moveToNearestCluster... setting attractor to nearest:"+nearest);

			setAttractorCluster( nearest );
		}
	}
	
	/**
	 * Move camera to the nearest cluster
	 * @param teleport  Whether to teleport (true) or navigate (false)
	 */
	void moveToNearestClusterAhead(boolean teleport) 
	{
		int ahead = getClusterAhead();		

		if(ahead != currentCluster)					// If a cluster ahead has been found
		{
			if (p.p.debug.viewer)
				p.display.message("moveToNearestClusterAhead goal:"+ahead);

			if(teleport)							
			{
				teleportToCluster(ahead, true);
			}
			else
			{
				if(teleporting)	teleporting = false;
				if(p.p.debug.viewer)
					p.display.message("moveToNearestClusterAhead... setting currentCluster and attractor to same:"+currentCluster);
				setAttractorCluster(currentCluster);
			}
		}
		else
		{
			if(p.p.debug.viewer)
				p.display.message("moveToNearestClusterAhead... can't move to same cluster!... "+ahead);
		}
	}
	

	/**
	 * Move camera to the nearest cluster
	 * @param teleport  Whether to teleport (true) or move (false)
	 */
	void moveToLastCluster(boolean teleport) 
	{
		if (p.p.debug.viewer)
			p.display.message("Moving to last cluster... "+lastCluster);
		if(lastCluster > 0)
		{
			if(teleport)
			{
				teleportGoalCluster = lastCluster;
				PVector newLocation = ((WMV_Cluster) p.getCurrentField().clusters.get(lastCluster)).getLocation();
				teleportToPoint(newLocation, true);
			}
			else
			{
				if(teleporting)	teleporting = false;
//				setCurrentCluster( lastCluster );
				if(p.p.debug.viewer)
					p.display.message("moveToLastCluster... setting attractor and currentCluster:"+currentCluster);
				setAttractorCluster( lastCluster );
			}
		}
	}

	/**
	 * Go to the next cluster numerically containing given media type
	 * @param teleport Whether to teleport or move
	 * @param mediaType Media type without which clusters are skipped...  -1: any 0: image, 1: panorama, 2: video
	 */
	public void moveToNextCluster(boolean teleport, int mediaType) 
	{
		setCurrentCluster(currentCluster + 1, -1);
		int next = currentCluster;
		int count = 0;
		boolean found = false;
		
		if (next >= p.getCurrentField().clusters.size())
			next = 0;
		
		if(p.p.debug.viewer)
			p.display.message("moveToNextCluster()... mediaType "+mediaType);

		/* Find goal cluster */
		if(mediaType == -1)	// Any media
		{
			while( p.getCurrentField().clusters.get(next).isEmpty() || next == currentCluster )		// Increment nextCluster until different non-empty cluster found
			{
				next++;

				if (next >= p.getCurrentField().clusters.size())
				{
					next = 0;
					count++;

					if(count > 3) break;
				}

				if(p.getCurrentField().clusters.get(next).mediaPoints != 0)
					PApplet.println("Error: Cluster marked empty but mediaPoints != 0!  clusterID:"+next);
			}

			if(count <= 3)				// If a cluster was found in 2 iterations
			{
				found = true;
				if(p.p.debug.viewer) PApplet.println("Moving to next cluster:"+next+" from current cluster:"+currentCluster);
			}
		}

		if(mediaType == 1)		// Panorama
		{
			while(  !p.getCurrentField().clusters.get(next).panorama || 		// Increment nextCluster until different non-empty panorama cluster found
					p.getCurrentField().clusters.get(next).isEmpty() || 
					next == currentCluster )
			{
				next++;

				if(next >= p.getCurrentField().clusters.size())
				{
					next = 0;
					count++;

					if(count > 3)
					{
						if(p.p.debug.viewer)
							p.display.message("No panoramas found...");
						break;
					}
				}
				
				if(p.getCurrentField().clusters.get(next).isEmpty() && p.getCurrentField().clusters.get(next).mediaPoints != 0)		// Testing
					PApplet.println("Error: Panorama cluster marked empty but mediaPoints != 0!  clusterID:"+next);
			}
			
			if(count <= 3)				// If a cluster was found in 2 iterations
			{
				found = true;
				if(p.p.debug.viewer)
					PApplet.println("Moving to next cluster with panorama:"+next+" from current cluster:"+currentCluster);
			}
			else
			{
				if(p.p.debug.viewer)
					p.display.message("No panoramas found...");
			}
		}
		
		if(mediaType == 2)				// Video
		{
			while(  !p.getCurrentField().clusters.get(next).video || 		// Increment nextCluster until different non-empty video cluster found
					p.getCurrentField().clusters.get(next).isEmpty() || 
					next == currentCluster )
			{
				next++;

				if(next >= p.getCurrentField().clusters.size())
				{
					next = 0;
					count++;

					if(count > 3)
					{
						p.display.message("No videos found...");
						break;
					}
				}
				
				if(p.getCurrentField().clusters.get(next).isEmpty() && p.getCurrentField().clusters.get(next).mediaPoints != 0)		// Testing
					PApplet.println("Error: Video cluster marked empty but mediaPoints != 0!  clusterID:"+next);
			}
			
			if(count <= 3)				// If a cluster was found in 2 iterations
			{
				found = true;
				if(p.p.debug.viewer)
					PApplet.println("Moving to next cluster with video:"+next+" from current cluster:"+currentCluster);
			}
			else
			{
				if(p.p.debug.viewer)
					p.display.message("No videos found...");
			}
		}
		
		if(found)				// If a cluster was found
		{
			if(teleport)		/* Teleport or move */
			{
				teleportToCluster(next, true);
			}
			else
			{
				if(teleporting)	teleporting = false;
				setAttractorCluster(currentCluster);
			}
		}
	}

	/**
	 * Go to cluster corresponding to given time segment in field
	 * @param fieldID Field to move to
	 * @param fieldTimeSegment Index of time segment in field timeline to move to
	 * @param teleport Whether to teleport or move
	 */
	void moveToTimeSegmentInField(int fieldID, int fieldTimeSegment, boolean teleport)
	{
		WMV_Field f = p.getField(fieldID);

		if(p.p.debug.viewer && p.p.debug.detailed)
			p.display.message("moveToTimeInField:"+f.timeline.get(fieldTimeSegment).getID()+" f.timeline.size():"+f.timeline.size());

		if(f.timeline.size()>0)
		{
			int clusterID = f.timeline.get(fieldTimeSegment).getClusterID();
			if(clusterID == currentCluster && p.getCluster(clusterID).getClusterDistance() < p.clusterCenterSize)	// Moving to different time in same cluster
			{
				currentFieldTimeSegment = fieldTimeSegment;
				if(p.p.debug.viewer && p.p.debug.detailed)
					p.display.message("Advanced to time segment "+fieldTimeSegment+" in same cluster... ");
			}
			else
			{
				movingToTimeSegment = true;								// Set time segment target
				timeSegmentTarget = fieldTimeSegment;
				
				if(teleport)
					teleportToCluster(clusterID, true);
				else
					setAttractorCluster(clusterID);
			}
		}
	}
		
	/**
	 * Teleport the viewer to the given cluster ID
	 * @param dest Destination cluster ID
	 * @param fade Fade (true) or jump (false)?
	 */
	public void teleportToCluster( int dest, boolean fade ) 
	{
		if(dest >= 0 && dest < p.getFieldClusters().size())
		{
			WMV_Cluster c = p.getCurrentField().clusters.get(dest);

			if(p.orientationMode)		// -- check this
			{
				teleportGoalCluster = dest;
				teleportGoal = c.getLocation();
				location = teleportGoal;
			}
			else
			{
				if(fade)
				{
					teleportGoalCluster = dest;
					teleportGoal = c.getLocation();
					startTeleport(-1);
				}
				else
				{
					camera.jump(c.getLocation().x, c.getLocation().y, c.getLocation().z);
					setCurrentCluster(dest, -1);
				}
			}

		}
		else if(p.p.debug.cluster || p.p.debug.field || p.p.debug.viewer)
		{
			PApplet.println("ERROR: Can't teleport to cluster:"+dest+"... clusters.size() =="+p.getCurrentField().clusters.size());
		}
	}

	/**
	 * Teleport to the field ID <inc> from current field
	 * @param offset Field ID offset amount (0 stays in same field)
	 */
	public void teleportToField(int offset, boolean fade) 
	{
		if(offset != 0)
		{
			p.stopAllVideos();
			int newField = field + offset;

			if(newField >= p.getFieldCount())
				newField = 0;
			
			if(newField < 0)
				newField = p.getFieldCount() - 1;

			teleportGoalCluster = 0;
			setCurrentCluster( 0, -1 );

			if(p.p.debug.viewer)
				p.display.message("Moving to field: "+newField+" out of "+p.getFieldCount());
			if(p.p.debug.viewer)
				p.display.message("... at cluster: "+currentCluster+" out of "+p.getField(newField).clusters.size());

			if(p.getField(newField).clusters.size() > 0)			// Check whether field has clusters (is valid)
			{
				if(fade)
				{
					teleportGoal = new PVector(0,0,0);					// -- Change this!
					startTeleport(newField);
				}
				else
				{
					camera.jump(0,0,0);
					setCurrentField(newField);				// Set new field
					if(p.p.debug.viewer)
						p.display.message(" Teleported to field "+teleportToField+"...");
				}
			}
			else
			{
				if(p.p.debug.viewer)
					p.display.message("This field has no clusters!");
			}
		}
	}

	/**
	 * @param inclCurrent Whether to include the current cluster in search
	 * @return Index of nearest cluster to camera
	 */
	int getNearestCluster(boolean inclCurrent) 	// Returns the cluster nearest to the current camera position, excluding the current cluster
	{
		PVector cPos = getLocation();
		float smallest = 100000.f;
		int smallestIdx = 0;

		if (p.getCurrentField().clusters.size() > 0) 
		{
			for (WMV_Cluster c : p.getActiveClusters()) 
			{
				float dist = PVector.dist(cPos, c.getLocation());
				if (dist < smallest) 
				{
					if(inclCurrent)									// If current cluster is included
					{
						if(!c.isEmpty())
						{
							smallest = dist;
							smallestIdx = c.getID();	
						}
					}
					else if (c.getID() != currentCluster) 		// If current cluster is excluded
					{
						smallest = dist;
						smallestIdx = c.getID();
					}
				}
			}
		} 
		else
		{
			if(p.p.debug.cluster)
				PApplet.println("No clusters in field...");
		}

		return smallestIdx;
	}
	
	/**
	 * @param amount Number of nearest clusters to return
	 * @param threshold If distance exceeds, will return less than <amount> nearest clusters
	 * @param inclCurrent Include the current cluster?
	 * @return Indices of nearest clusters to camera			
	 */
	IntList getNearClusters(int amount, float threshold) 	// -- excluding the current cluster??
	{
		PVector vPos = getLocation();
		IntList nearList = new IntList();
		FloatList distList = new FloatList();
		ArrayList<WMV_Cluster> cl = new ArrayList<WMV_Cluster>(p.getActiveClusters());
		ArrayList<WMV_Cluster> removeList = new ArrayList<WMV_Cluster>();
		
		if(amount == -1)				// No limit on number of clusters to search for
			amount = cl.size();
		
		for (WMV_Cluster c : cl) 		// Fill the list with <amount> locations under <threshold> distance from viewer
		{
			float dist = PVector.dist(vPos, c.getLocation());			// Distance from cluster to viewer
			if(dist < threshold)
			{
				if(nearList.size() < amount)
				{
					nearList.append(c.getID());
					distList.append(dist);
					removeList.add(c);
				}
				else break;
			}
		}
		
		for(WMV_Cluster c : removeList)
			cl.remove(c);

		for (WMV_Cluster c : cl) 	// Test remaining clusters against list locations and update lists
		{
			float dist = PVector.dist(vPos, c.getLocation());			// Distance from cluster to viewer

//			if(nearList.size() < amount)			// Fill the list first
//			{
//				nearList.append(c.getID());
//				distList.append(dist);
//			}
//			else									// Then compare new clusters to the list
//			{
				
			if(dist < threshold)
			{
				int count = 0;
				int largestIdx = -1;
				float largest = -1000.f;

				for(float f : distList)				// Find farthest distance in nearList to compare
				{
					if(f > largest)
					{
						largestIdx = count;
						largest = f;
					}
					count++;
				}

				float fcDist = PVector.dist(vPos, p.getCluster(largestIdx).getLocation());		// Distance of farthest cluster on nearList
				if(dist < fcDist)
				{
					nearList.remove(largestIdx);
					nearList.append(c.getID());
					distList.remove(largestIdx);
					distList.append(dist);
				}
			}
		}
		
		return nearList;
	}

	/**
	 * Move to cluster corresponding to one time segment later on timeline
	 * @param currentDate Whether to look only at time segments on current date
	 * @param teleport Whether to teleport or move
	 */
	void moveToNextTimeSegment(boolean currentDate, boolean teleport)
	{
		if(!currentDate)
		{
			boolean found = false;
			currentFieldTimeSegment++;
			while(!found)
			{
				if(currentFieldTimeSegment >= p.getCurrentField().timeline.size()) 		// Reached end of day
				{
					PApplet.println("Reached end of day...");
					currentFieldDate++;
					if(currentFieldDate >= p.getCurrentField().dateline.size()) 
					{
						PApplet.println("Reached end of year...");
						currentFieldDate = 0;
						currentFieldTimeSegment = 0;
						found = true;
					}
					else
					{
						PApplet.println("Looking in next date:"+currentFieldDate);
						currentFieldTimeSegment = 0;										// Start at first segment
					}
				}
			}
		}
		else
		{
			currentFieldTimeSegment++;
			if(currentFieldTimeSegment >= p.getCurrentField().timeline.size())
				currentFieldTimeSegment = 0;
		}

		moveToTimeSegmentInField(p.getCurrentField().fieldID, currentFieldTimeSegment, teleport);
	}
	
	/**
	 * Move to cluster corresponding to one time segment earlier on timeline
	 * @param currentDate Whether to look only at time segments on current date
	 * @param teleport Whether to teleport or move
	 */
	void moveToPreviousTimeSegment(boolean currentDate, boolean teleport)
	{
		PApplet.println("Can't moveToPreviousTimeSegment... function disabled!");
		if(!currentDate)
		{
//			boolean found = false;

			currentFieldTimeSegment--;
			if(currentFieldTimeSegment < 0) 		// Reached beginning of day
			{
				currentFieldDate--;
				if(currentFieldDate < 0) 
				{
					currentFieldDate = p.getCurrentField().dateline.size()-1;		// Go to last date
					currentFieldTimeSegment = p.getCurrentField().timeline.size()-1;		// Go to last time
//					found = true;
				}
				else
				{
					currentFieldTimeSegment = p.getCurrentField().timeline.size()-1;		// Start at last segment
				}
			}			
		}
		else
		{
			currentFieldTimeSegment--;
			if(currentFieldTimeSegment < 0)
				currentFieldTimeSegment = p.getCurrentField().timeline.size()-1;
		}

		moveToTimeSegmentInField(p.getCurrentField().fieldID, currentFieldTimeSegment, teleport);
	}

	/**
	 * Fade out all visible media, move to goal, then fade in media visible at that location.
	 * @param newField Goal field ID; value of -1 indicates to stay in current field
	 */
	public void startTeleport(int newField) 
	{
		p.getCurrentField().fadeOutMedia();
		teleporting = true;
		teleportStart = p.p.frameCount;
		teleportWaitingCount = 0;
		
		if(newField != -1)
			teleportToField = newField;
	}

	/**
	 * Rotate smoothly around X axis to specified angle
	 * @param angle Angle around X axis to rotate to
	 */
	void turnXToAngle(float angle, int turnDirection)
	{
		if(!turningX)
		{
			turnXStart = getXOrientation();
			turnXTarget = angle;
			
			PVector turnInfo = getTurnInfo(turnXStart, turnXTarget, turnDirection);
			
			if(turnDirection == 0)
				turnXDirection = turnInfo.x;
			else
				turnXDirection = turnDirection;
			
			turningXAccelInc = PApplet.map(turnInfo.y, 0.f, PApplet.PI * 2.f, turningAccelerationMin, turningAccelerationMax * 0.2f);
			turnXStartFrame = p.p.frameCount;
			turningX = true;
		}
	}
	
	/**
	 * Rotate smoothly around Y axis to specified angle
	 * @param angle Angle around Y axis to rotate to
	 */
	void turnYToAngle(float angle, int turnDirection)
	{
		if(!turningY)
		{
			turnYStart = getYOrientation();
			turnYTarget = angle;
			
			PVector turnInfo = getTurnInfo(turnYStart, turnYTarget, turnDirection);
			
			if(turnDirection == 0)
				turnYDirection = turnInfo.x;
			else
				turnYDirection = turnDirection;
			
			turningYAccelInc = PApplet.map(turnInfo.y, 0.f, PApplet.PI * 2.f, turningAccelerationMin, turningAccelerationMax * 0.2f);
			turnYStartFrame = p.p.frameCount;
			turningY = true;
		}
	}

	/**
	 * Rotate smoothly around X axis by specified angle
	 * @param angle Angle around X axis to rotate by
	 */
	void turnXByAngle(float angle)
	{
		if(!turningX)
		{
			turnXStart = getXOrientation();
			turnXTarget = turnXStart + angle;
			
			PVector turnInfo = getTurnInfo(turnXStart, turnXTarget, 0);
			
			turnXDirection = turnInfo.x;
			turnXStartFrame = p.p.frameCount;

			if(p.p.debug.viewer && p.p.debug.detailed)
				p.display.message("turnXStartFrame:"+turnXStartFrame+" turnXTargetFrame:"+turnXTargetFrame+" turnXDirection:"+turnXDirection);
			turningX = true;
		}
	}
	
	/**
	 * Rotate smoothly around Y axis by specified angle
	 * @param angle Angle around Y axis to rotate by
	 */
	void turnYByAngle(float angle)
	{
		if(!turningY)
		{
			if(angle < 0.f)					// Keep within range 0 to 2π
				angle += 2*PApplet.PI;
			else if(angle > 2*PApplet.PI)
				angle -= 2*PApplet.PI;

			turnYStart = getYOrientation();
			turnYTarget = turnYStart + angle;
			PVector turnInfo = getTurnInfo(turnYStart, turnYTarget, 0);
			turnYDirection = turnInfo.x;
//			turnYIncrement = turnIncrement;
			turnYStartFrame = p.p.frameCount;
//			turnYTargetFrame = turnYStartFrame + (int)turnInfo.z;
			turningY = true;
		}
	}

	/**
	 * Turn smoothly towards given media
	 * @param goal Point to smoothly turn towards
	 */
	public void lookAtMedia( int id, int mediaType ) 
	{
		PVector turnLoc = new PVector(0,0,0);
		
		if(p.p.debug.viewer)
			PApplet.println("Looking at media:"+id+" mediaType:"+mediaType);

		switch(mediaType)
		{
			case 0:			// Image
				turnLoc = p.getCurrentField().images.get(id).getLocation();
				break;
			case 1:			// Panorama		-- Turn towards "center"?
//				turnLoc = p.getCurrentField().images.get(id).getLocation();
				break;
			case 2:			// Video
				turnLoc = p.getCurrentField().videos.get(id).getLocation();
				break;
			case 3:			// Sound
//				turnLoc = p.getCurrentField().sounds.get(id).getLocation();
				break;
		}
		
		turningMediaGoal = new PVector(id, mediaType);
		turnTowards(turnLoc);
	}
	
	/**
	 * Turn smoothly towards given point
	 * @param goal Point to smoothly turn towards
	 */
	public void turnTowards( PVector goal ) 
	{
		if(p.p.debug.viewer)
			PApplet.println("Turning towards... goal.x:"+goal.x+" goal.y:"+goal.y+" goal.z:"+goal.z);

		PVector cameraPosition = getLocation();
		PVector camOrientation = getOrientation();

		PVector cameraToPoint = new PVector(  cameraPosition.x-goal.x, 	//  Vector from the camera to the point      
				cameraPosition.y-goal.y, 
				cameraPosition.z-goal.z   );
		
		camOrientation.normalize();
		cameraToPoint.normalize();

		float yaw = (float) Math.atan2(cameraToPoint.x, cameraToPoint.z);
		float adj = (float) Math.sqrt(Math.pow(cameraToPoint.x, 2) + Math.pow(cameraToPoint.z, 2)); 
		float pitch = -((float) Math.atan2(adj, cameraToPoint.y) - 0.5f * PApplet.PI);
		
		turnXToAngle(yaw, 0);		// Calculate which way to turn and start turning in X axis
		turnYToAngle(pitch, 0);		// Calculate which way to turn and start turning in Y axis
		
//		turnTargetPoint = goal;
	}
	
	/**	 
	 * @param startAngle	Starting angle
	 * @param targetAngle	Target angle
	 * @return				PVector (direction, increment, length in frames): direction -> 1: clockwise and -1: counterclockwise
	 * Calculates the direction, increment size and length of time it will take to turn from startingAngle to targetAngle
	 */
	PVector getTurnInfo(float startAngle, float targetAngle, int direction)
	{
		PVector result;
		float length = 0;
		
		float diffRight = -1.f;		// Difference when turning right (dir = 1)
		float diffLeft = -1.f;		// Difference when turning left (dir = -1)

		if(targetAngle < 0.f)
			targetAngle += PApplet.PI * 2.f;
		if(startAngle < 0.f)
			startAngle += PApplet.PI * 2.f;

		if(targetAngle > startAngle)									// Clockwise
		{
			diffRight = targetAngle - startAngle;
			diffLeft = (startAngle + 2.f*PApplet.PI) - targetAngle;
		}
		else if(targetAngle < startAngle)								// Counterclockwise
		{
			diffRight = (targetAngle + 2.f*PApplet.PI) - startAngle;
			diffLeft = startAngle - targetAngle;
		}
		else if(targetAngle == startAngle)								// Full rotation
		{
			diffRight = 2.f*PApplet.PI;
			diffLeft = 2.f*PApplet.PI;
		}

		if(direction == 0)						// Calculate direction
		{
			if(diffRight <= diffLeft)
			{
				length = diffRight;		// Frames until target reached
				result = new PVector(-1, length);
				return result;								// Return 1 for clockwise 
			}
			else
			{
				length = diffLeft;		// Frames until target reached
				result = new PVector(1, length);
				return result;								// Return -1 for counterclockwise 
			}
		}
		else												// Full rotation
		{
			if(direction == 1)								// Turn left
				length = diffLeft;
			else if(direction == -1)						// Turn right
				length = diffRight;
			
			result = new PVector(direction, length);		// Return direction, increment value and transition frame length 
			return result;
		}
	}
	
	/**
	 * Calculate distance needed to turn between two angles
	 * @param startAngle Starting angle
	 * @param targetAngle Target angle
	 * @param direction Direction to turn (1: clockwise, 0: fastest, -1: counterclockwise)
	 * @return Angular distance
	 */
	float getTurnDistance(float startAngle, float targetAngle, float direction)
	{
		float diffRight = -1.f;		// Difference when turning right (dir = 1)
		float diffLeft = -1.f;		// Difference when turning left (dir = -1)
		float length = 0.f;
		
		if(targetAngle < 0.f)
			targetAngle += PApplet.PI * 2.f;
		if(startAngle < 0.f)
			startAngle += PApplet.PI * 2.f;
		
		if(targetAngle > startAngle)									// Clockwise
		{
			diffRight = targetAngle - startAngle;
			diffLeft = (startAngle + 2.f*PApplet.PI) - targetAngle;
		}
		else if(targetAngle < startAngle)								// Counterclockwise
		{
			diffRight = (targetAngle + 2.f*PApplet.PI) - startAngle;
			diffLeft = startAngle - targetAngle;
		}
		else if(targetAngle == startAngle)								// Full rotation
		{
			diffRight = 2.f*PApplet.PI;
			diffLeft = 2.f*PApplet.PI;
		}

		if(direction == 0)						// Calculate direction
		{
			if(diffRight <= diffLeft)
			{
				length = diffRight;		// Frames until target reached
				return length;
			}
			else
			{
				length = diffLeft;		// Frames until target reached
				return length;
			}
		}
		else												// Full rotation
		{
			if(direction == 1.f)								// Turn left
				length = diffLeft;
			else if(direction == -1.f)						// Turn right
				length = diffRight;
			
			return length;
		}
	}

	/**
	 * @param dir Direction to rotate (1: clockwise, -1: counterclockwise)
	 */
	public void rotateX(int dir)
	{
		rotateXDirection = dir;
		rotatingX = true;
	}

	/**
	 * @param dir Direction to rotate (1: clockwise, -1: counterclockwise)
	 */
	public void rotateY(int dir)
	{
		rotateYDirection = dir;
		rotatingY = true;
	}

	/**
	 * @param clusterList Clusters to search through
	 * @param direction Directional vector of camera movement
	 * @return Cluster in the approximate direction of given vector from camera. If none within 30 degrees, returns currentCluster
	 */
	public int getClusterAlongVector(ArrayList<WMV_Cluster> clusterList, PVector direction)
	{
		if(clusterList.size() == 0)
			clusterList = p.getActiveClusters();
		
		IntList clustersAlongVector = new IntList();
		
		for (WMV_Cluster c : clusterList) 							// Iterate through the clusters
		{
			PVector clusterVector = getVectorToCluster(c);
			PVector crossVector = new PVector();
			PVector.cross(direction, clusterVector, crossVector);		// Cross vector gives angle between camera and image
			float result = crossVector.mag();
			
			if(PApplet.abs(result) < PApplet.PI / 6.f && !c.isEmpty())
			{
				p.display.message("Finding Distance of Centered Cluster:"+c.getID()+" at Angle "+result+" from History Vector...");
				if(c.getID() != currentCluster)
					clustersAlongVector.append(c.getID());
			}
			else
			{
				if(p.p.debug.viewer && p.p.debug.detailed)
					p.display.message("Cluster ID:"+c.getID()+" at angle "+result+" from camera..."+" NOT centered!");
			}
		}

		float smallest = 100000.f;
		int smallestIdx = 0;

		for (int i = 0; i < clustersAlongVector.size(); i++) 		// Compare distances of clusters in front
		{
			PVector cPos = getLocation();
			WMV_Cluster c = (WMV_Cluster) p.getCurrentField().clusters.get(i);
			if(p.p.debug.viewer && p.p.debug.detailed)
				p.display.message("Checking Centered Cluster... "+c.getID());
		
			float dist = PVector.dist(cPos, c.getLocation());
			if (dist < smallest) 
			{
				smallest = dist;
				smallestIdx = i;
			}
		}		
		
		if(clustersAlongVector.size() > 0)
			return smallestIdx;
		else
		{
			if(p.p.debug.viewer && p.p.debug.detailed)
				p.display.message("No clusters found along vector!");
			return currentCluster;
		}
	}

	/**
	 * @param minTimelinePoints Minimum points in timeline of cluster to move to
	 */
	void moveToNearestClusterWithTimes(int minTimelinePoints, boolean teleport)
	{
		int nextCluster;
		
		nextCluster = p.viewer.currentCluster + 1;
		if(nextCluster >= p.getCurrentField().clusters.size())
			nextCluster = 0;
		int count = 0;
		boolean found = false;
		while(p.getCluster(nextCluster).timeline.size() < 2)
		{
			nextCluster++;
			count++;
			if(nextCluster >= p.getCurrentField().clusters.size())
				nextCluster = 0;
			if(count >= p.getCurrentField().clusters.size())
				break;
		}

		if(count < p.getCurrentField().clusters.size())
			found = true;

		if(p.p.debug.viewer && p.p.debug.detailed)
			p.display.message("moveToClusterWith2OrMoreTimes... setting attractor:"+p.getCurrentField().timeline.get(nextCluster).getID());

		if(found)
		{
			if(teleport)
				teleportToCluster(p.getCurrentField().clusters.get(nextCluster).getID(), true);
			else
				setAttractorCluster(p.getCurrentField().clusters.get(nextCluster).getID());
		}
	}
	
	/**
	 * @param nextTimeCluster Index in timeline of cluster to move to
	 */
	void moveToNearestClusterInFuture()
	{
//		int nextTimeCluster;
//		
//		IntList closest = getNearClusters(20, p.defaultFocusDistance * 4.f);											// Find 20 near clusters 	- Set number based on cluster density?
//		for(int i:closest)
//		{
//			GMV_Cluster c = p.getFieldClusters().get(i);
//			for(GMV_TimeCluster t : c.clusterTimes)
//			{
//				
//			}
//		}
//		
//		if(p.p.debug.viewer && p.p.debug.detailed)
//			p.display.message("moveToNearestClusterInFuture... setting attractor:"+p.getCurrentField().timeline.get(nextTimeCluster).getID());
//		setAttractorCluster(p.getCurrentField().timeline.get(nextTimeCluster).getID());
	}

	/**
	 * Send the camera to nearest cluster in the field
	 */
	public void moveToNextClusterAlongHistoryVector()
	{
		IntList closest = getNearClusters(20, p.defaultFocusDistance * 4.f);		// Find 20 near clusters 	- Set number based on cluster density?
		ArrayList<WMV_Cluster> clusterList = clustersAreInList(closest, 10);		// Are the clusters within the last 10 waypoints in history?
		PVector hv = getHistoryVector();											// Get vector for direction of camera movement

		int newCluster = getClusterAlongVector(clusterList, hv);		
		setAttractorCluster(newCluster);
	}
	
	/**
	 * @param teleport Teleport (true) or navigate (false) to cluster?
	 * Send the camera to a random cluster in the field
	 */
	public void moveToRandomCluster(boolean teleport)
	{
		int rand = (int) p.p.random(p.getCurrentField().clusters.size());
		while(p.getCurrentField().clusters.get(rand).isEmpty())
		{
			rand = (int) p.p.random(p.getCurrentField().clusters.size());
		}

		if(teleport)
		{
			int goal = rand;
			while(p.getCurrentField().clusters.get(goal).isEmpty() || goal == currentCluster)
			{
				goal = (int) p.p.random(p.getCurrentField().clusters.size());
			}

			teleportToCluster(goal, true);
		}
		else
		{
//			setCurrentCluster( rand );
			setAttractorCluster( rand );
		}
	}

	/**
	 * During manual path navigation, move to next cluster on path; otherwise move to next closest cluster (besides current)
	 */
	public void moveToNextLocation()
	{
		moveToNextClusterAlongHistoryVector();				// Move to next attractor on a path (not retracing N steps) 

//		if(!manualPathNavigation)
//			moveToNearestCluster(false);			// Move to closest cluster besides current
	}
	
	/**
	 * @param newCluster New attractor cluster
	 * Set a specific cluster as the current attractor
	 */
	public void setAttractorCluster(int newCluster)
	{
		stopMoving();									// -- Improve by slowing down instead and then starting
		p.getCurrentField().clearAllAttractors();
		
		if(p.p.debug.viewer)
			p.display.message("Setting new attractor:"+newCluster+" old attractor:"+attractorCluster);

		attractorCluster = newCluster;											// Set attractorCluster
		movingToCluster = true;													// Move to cluster
		attractionStart = p.p.frameCount;
		
		p.getCurrentField().clusters.get(attractorCluster).setAttractor(true);

		for(WMV_Cluster c : p.getCurrentField().clusters)
			if(c.getID() != attractorCluster)
				c.setAttractor(false);
		
		if(p.getCurrentField().clusters.get(attractorCluster).getClusterDistance() < clusterNearDistance)
		{
			if(p.getCurrentField().clusters.get(attractorCluster).getClusterDistance() > p.clusterCenterSize)
			{
//				p.display.message("Moving nearby...");
				movingNearby = true;
			}
			else
			{
//				p.display.message("Reached attractor without moving...");
				handleReachedAttractor();				// Reached attractor without moving
			}
		}
			
//		saveAttitude = getOrientation();
	}

	public void clearAttractorCluster()
	{
		attractorCluster = -1;											// Set attractorCluster
		movingToCluster = false;
		movingToAttractor = false;
	}
	
//	public void handleMouseSelection(int mouseX, int mouseY)
//	{
//		
//	}
	
	/**
	 * Stop any current viewer movement
	 */
	public void stopMoving()
	{
		if(p.p.debug.viewer)
			p.display.message("Stopping...");

		attraction = new PVector(0,0,0);						
		acceleration = new PVector(0,0,0);							
		velocity = new PVector(0,0,0);							
		walkingAcceleration = new PVector(0,0,0);					
		walkingVelocity = new PVector(0,0,0);	
		
		walking = false;
		slowing = false;
		halting = false;
		movingToAttractor = false;
		movingToCluster = false;
	}
	
	/**
	 * Reset the viewer to initial state
	 */
	public void reset()
	{
		/* Camera */
		fieldOfView = PApplet.PI * 0.375f;				// Field of view

		rotateIncrement = 3.1415f / 256.f;				// Rotation amount each frame when turning
		zoomIncrement = 3.1415f / 32.f;					// Zoom amount each frame when zooming

		nearClippingDistance = 3.f; 						// Distance (m.) of near clipping plane
		nearViewingDistance = nearClippingDistance * 2.f; // Near distance (m.) at which media start fading out
		farViewingDistance = 12.f; 						// Far distance (m.) at which media start fading out

		/* Time */
		currentFieldDate = 0;					// Current date in field dateline
		currentClusterDate = 0;				// Current date segment in cluster dateline
		currentFieldTimeSegment = 0;				// Current time segment in field timeline
		currentClusterTimeSegment = 0;			// Current time segment in cluster timeline
		
		/* Memory */
		movingToAttractor = false;			// Moving to attractor poanywhere in field
		movingToCluster = false;			// Moving to cluster 
		following = false;					// Is the camera currently navigating from memory?
		
		/* Clusters */
		field = 0;								// Current field
		maxVisibleClusters = 2;					// Maximum visible clusters in Orientation Mode
		currentCluster = 0;						// Cluster currently in view
		lastCluster = -1;						// Last cluster visited
		attractorCluster = -1;					// Cluster attracting the camera
		attractionStart = 0;
		teleportGoalCluster = -1;				// Cluster to navigate to (-1 == none)
		clusterNearDistanceFactor = 2.f;		// Multiplier for clusterCenterSize to get clusterNearDistance

		/* Sound */
		audibleFarDistanceFadeLength = 40;
		audibleFarDistanceDiv = (float) 1.5;
		audibleFarDistanceTransition = false;

		audibleNearDistanceFadeLength = 40;
		audibleNearDistanceDiv = (float) 1.2; 
		audibleNearDistanceTransition = false;

		/* Interaction Modes */
		mouseNavigation = false;			// Mouse navigation
		map3DMode = false;				// 3D Map Mode
		selection = false;				// Allows selection, increases transparency to make selected image(s) easier to see
		optimizeVisibility = true;		// Optimize visibility automatically
		lockToCluster = false;			// Automatically move viewer to nearest cluster when idle
		multiSelection = false;			// User can select multiple images for stitching
		segmentSelection = false;		// Select image segments at a time
		videoMode = false;				// Highlights videos by dimming other media types	-- Unused
		
		/* Teleporting */
		movementTeleport = false;		// Teleport when following navigation commands
		teleporting = false;			// Transition where all images fade in or out
		teleportToField = -1;				// What field ID to fade transition to	 (-1 remains in current field)
		teleportWaitingCount = 0;			// How long has the viewer been waiting for media to fade out before teleport?
		
		/* Physics */
		lastAttractorDistance = -1.f;
		cameraMass = 0.33f;						// Camera mass for cluster attraction
		velocityMin = 0.00005f;					// Threshold under which velocity counts as zero
		velocityMax = 0.66f;						// Camera maximum velocity
		accelerationMax = 0.15f;					// Camera maximum acceleration
		accelerationMin = 0.00001f;				// Threshold under which acceleration counts as zero
		camDecelInc = 0.75f;						// Camera deceleration increment
		camHaltInc = 0.01f;						// Camera fast deceleration increment
		waiting = false;						// Whether the camera is waiting to move while following a path
		pathWaitLength = 100;

		/* Movement */
		followMode = 0;					// 0: Timeline 1: GPS Track 2: Memory
		walking = false;			// Whether viewer is walking
		walkingAccelInc = 0.002f;		// Camera walking acceleration increment

		slowing = false;			// Whether viewer is slowing 
		slowingX = false;			// Slowing X movement
		slowingY = false;			// Slowing Y movement
		slowingZ = false;			// Slowing Z movement
		halting = false;			// Viewer is halting
		
		movingX = false;			// Is viewer automatically moving in X dimension (side to side)?
		movingY = false;			// Is viewer automatically moving in Y dimension (up or down)?
		movingZ = false;			// Is viewer automatically moving in Z dimension (forward or backward)?
		movingNearby = false;		// Moving to a powithin nearClusterDistance

		/* Looking */
//		looking = false;				// Whether viewer is turning to look for images, since none are visible
//		lookingRotationCount = 0;		// Amount of times viewer has rotated looking for images

		/* Turning */
		turningX = false;			// Whether the viewer is turning (right or left)
		turningY = false;			// Whether the viewer is turning (up or down)
		
//		turnIncrement = PApplet.PI / 240.f;
		
		rotatingX = false;			// Whether the camera is rotating in X dimension (turning left or right)?
		rotatingY = false;			// Whether the camera is rotating in Y dimension (turning up or down)?
		rotatingZ = false;			// Whether the camera is rotating in Z dimension (rolling left or right)?

		/* Interaction */
		selectionMaxDistanceFactor = 2.f;		// Scaling from defaultFocusDistanceFactor to selectionMaxDistance
		lastMovementFrame = 500000; 
		lastLookFrame = 500000;
		clusterLockIdleFrames = 0;				// How long to wait after user input before auto navigation moves the camera?
		lockToClusterWaitLength = 100;

		/* GPS Tracks */
		gpsTrackSelected = false;			// Has a GPS track been selected?
		gpsTrackName = "";					// GPS track name

		/* Zooming */
		zooming = false;
		zoomLength = 15;

		location = new PVector(0,0,0);
		velocity = new PVector(0,0,0);
		acceleration = new PVector(0,0,0);
		attraction = new PVector(0,0,0);
		walkingVelocity = new PVector(0,0,0);
		walkingAcceleration = new PVector(0,0,0);

		fieldOfView = initFieldOfView; 		// Field of view

		history = new ArrayList<WMV_Waypoint>();
		gpsTrack = new ArrayList<WMV_Waypoint>();

		memory = new ArrayList<WMV_Waypoint>();
		path = new ArrayList<WMV_Waypoint>();
		teleportGoal = new PVector(0, 0, 0);

		field = 0;
		currentCluster = 0;
		clusterNearDistance = p.clusterCenterSize * clusterNearDistanceFactor;

		initialize(0, 0, 0);
	}

	/**
	 * @return Current camera location
	 */
	public PVector getLocation()
	{
//		if(p.orientationMode)
//			return location;
//		else
			location = new PVector(camera.position()[0], camera.position()[1], camera.position()[2]);			// Update location

		return location;
	}

	public PVector getGPSLocation()			// Working??
	{
		PVector vLoc;
//		if(p.orientationMode)
//			vLoc = location;
//		else
			vLoc = new PVector(camera.position()[0], camera.position()[1], camera.position()[2]);			// Update location
		
		WMV_Model m = p.getCurrentField().model;
		
		float newX = PApplet.map( vLoc.x, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, m.lowLongitude, m.highLongitude ); 			// GPS longitude decreases from left to right
		float newY = -PApplet.map( vLoc.z, -0.5f * m.fieldLength, 0.5f*m.fieldLength, m.lowLatitude, m.highLatitude ); 			// GPS latitude increases from bottom to top; negative to match P3D coordinate space

		PVector gpsLoc = new PVector(newX, newY);

		return gpsLoc;
	}
	
	/**
	 * Turn towards the selected image
	 */
//	public void turnTowardsSelected(int selectedImage)
//	{
//		if(selectedImage != -1)
//		{
//			PVector selectedImageLoc;
//			selectedImageLoc = p.getCurrentField().images.get(selectedImage).getLocation();
//			p.viewer.turnTowardsPoint(selectedImageLoc);
//		}
//	}
	
//	public PVector location, orientation;											// Location of the camera in virtual space
//	public PVector velocity, acceleration, attraction;      // Physics model parameters
//	public PVector walkingVelocity, walkingAcceleration;	// Physics parameters applied relative to camera direction

	public float getVelocity()
	{
		if(walking)
			return walkingVelocity.mag();
		else
			return velocity.mag();
	}

	public float getAcceleration()
	{
		if(walking)
			return walkingAcceleration.mag();
		else
			return acceleration.mag();
	}
	
	public float getAttraction()
	{
		return attraction.mag();
	}
	
	/**
	 * getOrientation()
	 * @return Current camera X orientation (Yaw)
	 */
	public PVector getOrientation()
	{
		orientation = new PVector(camera.attitude()[0], camera.attitude()[1], camera.attitude()[2]);			// Update location
		return orientation;
	}

	/**
	 * getXOrientation()
	 * @return Current camera X orientation (Yaw)
	 */
	public float getXOrientation()
	{
		orientation = new PVector(camera.attitude()[0], camera.attitude()[1], camera.attitude()[2]);			// Update location
		return orientation.x;
	}

	/**
	 * getYOrientation()
	 * @return Current camera Y orientation (Pitch)
	 */
	public float getYOrientation()
	{
		orientation = new PVector(camera.attitude()[0], camera.attitude()[1], camera.attitude()[2]);			// Update location
		return orientation.y;
	}

	/**
	 * getZOrientation()
	 * @return Current camera Z orientation (Roll)
	 */
	public float getZOrientation()
	{
		orientation = new PVector(camera.attitude()[0], camera.attitude()[1], camera.attitude()[2]);			// Update location
		return orientation.z;
	}
	
	/**
	 * getOrientationVector()
	 * @return Current camera orientation as a directional unit vector
	 */
	public PVector getOrientationVector()
	{
		float[] cAtt = camera.attitude();			// Get camera attitude (orientation)
		float pitch = cAtt[1], yaw = cAtt[0];
//		float roll = cAtt[2];

		float sinYaw = PApplet.sin(yaw);
		float cosYaw = PApplet.cos(yaw);
		float sinPitch = PApplet.sin(-pitch);
		float cosPitch = PApplet.cos(-pitch);

		PVector camOrientation = new PVector (-cosPitch * sinYaw, sinPitch, -cosPitch * cosYaw);	
		camOrientation.normalize();
		
		return camOrientation;
	}
	
	/**
	 * getTargetVector()
	 * @return Current camera target as a directional unit vector
	 */
	public PVector getTargetVector()
	{
		float[] cTar = camera.target();			// Get camera attitude (orientation)
		float pitch = cTar[1], yaw = cTar[0];
//		float roll = cTar[2];

		float sinYaw = PApplet.sin(yaw);
		float cosYaw = PApplet.cos(yaw);
		float sinPitch = PApplet.sin(-pitch);
		float cosPitch = PApplet.cos(-pitch);

		PVector camOrientation = new PVector (-cosPitch * sinYaw, sinPitch, -cosPitch * cosYaw);	
		camOrientation.normalize();
		
		return camOrientation;
	}
	
	/**
	 * Update viewer turning
	 */
	void updateTurning()
	{
		if (turningX && !turnSlowingX) 		// Turn X Transition
		{
			turningAcceleration.x += turningXAccelInc * turnXDirection;
			lastMovementFrame = p.p.frameCount;
		}

		if (turningY && !turnSlowingY) 		// Turn Y Transition
		{
			turningAcceleration.y += turningYAccelInc * turnYDirection;
			lastMovementFrame = p.p.frameCount;
		}

		if(turnSlowingX)
		{
			turningVelocity.x *= turningDecelInc;
			turningAcceleration.x *= turningDecelInc;
		}
		
		if(turnSlowingY)
		{
			turningVelocity.y *= turningDecelInc;
			turningAcceleration.y *= turningDecelInc;
		}
	
		if(turnHaltingX)
		{
			turningVelocity.x *= turningHaltInc;
			turningAcceleration.x *= turningHaltInc;
		}
		
		if(turnHaltingY)
		{
			turningVelocity.y *= turningHaltInc;
			turningAcceleration.y *= turningHaltInc;
		}
	
		if(PApplet.abs(turningVelocity.mag()) > 0.f || PApplet.abs(turningAcceleration.mag()) > 0.f)				/* Walking if walkingVelocity or walkingAcceleration > 0 */
		{
			if(!turningX)
			{
				turningAcceleration.x = 0.f;
				turningVelocity.x = 0.f;
			}

			if(!turningY)
			{
				turningAcceleration.y = 0.f;
				turningVelocity.y = 0.f;
			}

			if(PApplet.abs(turningAcceleration.x) > turningAccelerationMax)			// Decelerate if above camMaxVelocity
				turningAcceleration.x *= turningDecelInc;				

			if(PApplet.abs(turningAcceleration.y) > turningAccelerationMax)			// Decelerate if above camMaxVelocity
				turningAcceleration.y *= turningDecelInc;				

			if(PApplet.abs(turningVelocity.x) > turningVelocityMax)			// Decelerate if above camMaxVelocity
				turningAcceleration.x *= turningDecelInc;				

			if(PApplet.abs(turningVelocity.y) > turningVelocityMax)			// Decelerate if above camMaxVelocity
				turningAcceleration.y *= turningDecelInc;				

			turningVelocity.add(turningAcceleration);							// Add acceleration to velocity

			if(PApplet.abs( turningVelocity.mag()) > 0.f && PApplet.abs(turningVelocity.x) < turningVelocityMin 
							&& (turnSlowingX || turnHaltingX) )
			{
				turningX = false;
				turnSlowingX = false;
				turnHaltingX = false;
				turningVelocity.x = 0.f;			// Clear turningVelocity.x when close to zero (below velocityMin)
			}

			if(PApplet.abs( turningVelocity.mag()) > 0.f && PApplet.abs(turningVelocity.y) < turningVelocityMin 
							&& (turnSlowingY || turnHaltingY) )
			{
				turningY = false;
				turnSlowingY = false;
				turnHaltingY = false;
				turningVelocity.y = 0.f;			// Clear turningVelocity.y when close to zero (below velocityMin)
			}

			if(PApplet.abs(turningVelocity.x) == 0.f && turnSlowingX )
				turnSlowingX = false;

			if(PApplet.abs(turningVelocity.y) == 0.f && turnSlowingY)
				turnSlowingY = false;
			
			if(PApplet.abs(turningVelocity.x) == 0.f && turnHaltingX )
				turnHaltingX = false;

			if(PApplet.abs(turningVelocity.y) == 0.f && turnHaltingY)
				turnHaltingY = false;
		}
		
		if(turningX)
		{
			float xTurnDistance = getTurnDistance(getXOrientation(), turnXTarget, turnXDirection);
			if(PApplet.abs(xTurnDistance) < turningNearDistance) // && !turningNearby)
			{
				if(PApplet.abs(xTurnDistance) > turningCenterSize)
				{
					if(PApplet.abs(turningVelocity.x) > turningVelocityMin)					/* Slow down at attractor center */
						if(turningX && !turnSlowingX) 
							turnSlowingX = true;
				}
				else
				{
					if(PApplet.abs(turningVelocity.x) > turningVelocityMin)					/* Slow down at attractor center */
						if(turningX && !turnHaltingX) 
							turnHaltingX = true;
				}
			}
		}

		if(turningY)
		{
			float yTurnDistance = getTurnDistance(getYOrientation(), turnYTarget, turnYDirection);
			if(PApplet.abs(yTurnDistance) < turningNearDistance * 0.5f) // && !turningNearby)
			{
				if(PApplet.abs(yTurnDistance) > turningCenterSize * 0.5f)
				{
					if(PApplet.abs(turningVelocity.y) > turningVelocityMin)					/* Slow down at attractor center */
						if(turningY && !turnSlowingY) 
							turnSlowingY = true;
				}
				else
				{
					if(PApplet.abs(turningVelocity.y) > turningVelocityMin)					/* Slow down at attractor center */
						if(turningY && !turnHaltingY) 
							turnHaltingY = true;
				}
			}
		}

		if( turningX || turningY )
		{
			turn();
		}
		else														// Just stopped turning
		{
			if(optimizeVisibility && !p.getCurrentField().mediaAreFading())
			{
				if(turningMediaGoal != null)
				{
					if(!turningMediaGoal.equals(new PVector(-1.f, -1.f)))
					{
						float goalMediaBrightness = 0.f;
						switch((int)turningMediaGoal.y)
						{
							case 0:
								WMV_Image img = p.getCurrentField().images.get((int)turningMediaGoal.x);
								goalMediaBrightness = img.viewingBrightness;
								break;
							case 1:
								WMV_Panorama pano = p.getCurrentField().panoramas.get((int)turningMediaGoal.x);
								goalMediaBrightness = pano.viewingBrightness;
								break;
							case 2:
								WMV_Video vid = p.getCurrentField().videos.get((int)turningMediaGoal.x);
								goalMediaBrightness =  vid.viewingBrightness;
								break;
						}
						
//						PApplet.println("keepMediaVisible... goalMediaBrightness:"+goalMediaBrightness);
						if(goalMediaBrightness == 0.f && p.angleFading)
						{
							if(p.p.debug.viewer)
								p.display.message("Set angle fading to false...");
							p.angleFading = false;
						}
						
						turningMediaGoal = new PVector(-1.f, -1.f);
					}
				}
			}
		}
	}
	
	/**
	 * Update physical model each frame
	 */
	public void updatePhysics()
	{
		if(PApplet.abs(walkingVelocity.mag()) > 0.f || PApplet.abs(walkingAcceleration.mag()) > 0.f)				/* Walking if walkingVelocity or walkingAcceleration > 0 */
		{
			if(!walking)
			{
				acceleration = new PVector(0,0,0);
				velocity = new PVector(0,0,0);
				walking = true;
			}
			
			if(PApplet.abs(walkingVelocity.mag()) > velocityMax)			// Decelerate if above camMaxVelocity
				walkingAcceleration.mult(camDecelInc);				

			walkingVelocity.add(walkingAcceleration);			// Add acceleration to velocity

			walk();												// Move the camera manually 

			if(PApplet.abs(walkingVelocity.mag()) > 0.f && PApplet.abs(walkingVelocity.mag()) < velocityMin && !movingX && !movingY && !movingZ)
			{
				slowingX = false;
				slowingY = false;
				slowingZ = false;
				walkingVelocity = new PVector(0,0,0);			// Clear walkingVelocity when reaches close to zero (below velocityMin)
				walking = false;
			}

			if(PApplet.abs(walkingVelocity.mag()) == 0.f && (slowingX  || slowingY ||	slowingZ ) )
			{
				slowingX = false;
				slowingY = false;
				slowingZ = false;
			}
		}
		else if( movingToAttractor || movingToCluster || following )								
		{
			if(walking) walking = false;

			if(PApplet.abs(attraction.mag()) > 0.f)					/* If not slowing and attraction force exists */
			{
				if(PApplet.abs(acceleration.mag()) < accelerationMax)			/* Apply attraction up to maximum acceleration */
					acceleration.add( PVector.div(attraction, cameraMass) );	
				else
					p.display.message("--> Attraction but no acceleration... attraction.mag():"+attraction.mag()+" acceleration.mag():"+acceleration.mag());
			}

			if(slowing)
			{
				attraction.mult(camDecelInc);
				acceleration.mult(camDecelInc);							// Decrease acceleration
				velocity.mult(camDecelInc);								// Decrease velocity
			}
			
			if(halting)
			{
				float attractionMult = camHaltInc-PApplet.map(attraction.mag(), 0.f, 1.f, 0.f, camHaltInc);
				float accelerationMult = camHaltInc-PApplet.map(acceleration.mag(), 0.f, 1.f, 0.f, camHaltInc);
				float velocityMult = camHaltInc-PApplet.map(velocity.mag(), 0.f, 1.f, 0.f, camHaltInc);
				attraction.mult(attractionMult);
				acceleration.mult(accelerationMult);							// Decrease acceleration
				velocity.mult(velocityMult);								// Decrease velocity
			}

			if(PApplet.abs(acceleration.mag()) > 0.f)					// Add acceleration to velocity
				velocity.add(acceleration);					
			
			if(PApplet.abs(velocity.mag()) > velocityMax)				/* If reached max velocity, slow down */
			{
				acceleration.mult(camDecelInc);							// Decrease acceleration
				velocity.mult(camDecelInc);								// Decrease velocity
			}

			if(acceleration.mag() != 0.f && PApplet.abs(acceleration.mag()) < accelerationMin)		/* Set acceleration to zero when below minimum */
				acceleration = new PVector(0,0,0);			
			
			if(velocity.mag() != 0.f && PApplet.abs(velocity.mag()) < velocityMin)		/* If reached min velocity, set velocity to zero */
				velocity = new PVector(0,0,0);							

			WMV_Cluster curAttractor = new WMV_Cluster(p.getCurrentField(), 0, 0, 0, 0);	 /* Find current attractor if one exists */
			boolean attractorFound = false;
			
			if( (movingToAttractor || following) && attractorPoint != null )
			{
//				if(p.p.debug.viewer && p.p.frameCount - attractionStart > 120)					/* If not slowing and attraction force exists */
//					p.display.message("Attraction taking a while... slowing:"+slowing+" halting:"+halting+" attraction.mag():"+attraction.mag()+" acceleration.mag():"+acceleration.mag());

				curAttractor = attractorPoint;
				attractorFound = true;
			}
			else if(attractorCluster != -1)
			{
				curAttractor = p.getCurrentField().clusters.get(attractorCluster);
				attractorFound = true;
			}

			if(attractorFound && !waiting)					
			{
				boolean reachedAttractor = false;				

				if(curAttractor.getClusterDistance() < clusterNearDistance && !movingNearby)
				{
					if(PApplet.abs(velocity.mag()) > velocityMin)					/* Slow down at attractor center */
					{
						if(!slowing) slow();
					}
				}

				if(curAttractor.getClusterDistance() < p.clusterCenterSize)
				{
					if(PApplet.abs(velocity.mag()) > velocityMin)					/* Slow down at attractor center */
					{
						halt();
					}
					else 
					{
						if(halting) halting = false;
						if(slowing) slowing = false;
						if(movingNearby) movingNearby = false;
						reachedAttractor = true;
					}
				}

				if(reachedAttractor) 
					handleReachedAttractor();
			}

			if(p.orientationMode)
			{
				location.add(velocity);		// Add velocity to location
				jumpTo(location);			// Jump to new location
			}
			else 
			{
				location.add(velocity);			// Add velocity to location
				jumpTo(location);				// Move camera
				location = getLocation();		// Update location
			}
		}

		if(attractorCluster != -1)
		{
			float curAttractorDistance = PVector.dist( p.getCurrentField().clusters.get(attractorCluster).getLocation(), getLocation() );
			if(curAttractorDistance > lastAttractorDistance && !slowing)	// If the camera is getting farther than attractor
			{
				if(p.p.debug.viewer && attractionStart - p.p.frameCount > 20)
				{
					p.display.message("Getting farther from attractor: will stop moving...");
					stopMoving();												// Stop
				}
			}

			/* Record last attractor distance */
			lastAttractorDistance = PVector.dist(p.getCurrentField().clusters.get(attractorCluster).getLocation(), getLocation());
		}

		/* Reset acceleration each frame */
		acceleration = new PVector(0,0,0);			// Clear acceleration vector
		walkingAcceleration = new PVector(0,0,0);	// Clear acceleration vector
		attraction = new PVector(0,0,0);			// Clear attraction vector

	}
	
	/**
	 * Handle when viewer has reached attractorPoint or attractorCluster
	 */
	private void handleReachedAttractor()
	{
		if(following && path.size() > 0)
		{
			setCurrentCluster( attractorCluster, -1 );
			if(p.p.debug.viewer)
				p.display.message("Reached path goal #"+pathLocationIdx+", will start waiting...");
			startWaiting(pathWaitLength);
		}

		if(movingToCluster)		
		{
			if(p.p.debug.viewer)
				p.display.message("Moving to cluster... current:"+currentCluster+" attractor: "+attractorCluster+"...");
			if(attractorCluster != -1)
			{
				if(movingToTimeSegment)
					setCurrentCluster( attractorCluster, timeSegmentTarget );
				else
					setCurrentCluster( attractorCluster, -1 );

				attractorCluster = -1;
				
				p.getCurrentField().clearAllAttractors();	// Stop attracting when reached attractorCluster
			}
			else
			{
				setCurrentCluster( getNearestCluster(true), -1 );
			}
			
			if(p.p.debug.viewer)
				p.display.message("Reached cluster... current:"+currentCluster+" nearest: "+getNearestCluster(false)+" set current time segment to "+currentFieldTimeSegment);
			movingToCluster = false;
		}

		if(movingToAttractor)		// Stop attracting when reached attractorPoint
		{
			setCurrentCluster( getNearestCluster(true), -1 );		// Set currentCluster to nearest
			
//			turnTowardsPoint(attractorPoint.getLocation());
			p.getCurrentField().clearAllAttractors();
			movingToAttractor = false;
		}
	}

	public void updateWalking()
	{
		// Move X Transition
		if (movingX && !slowingX) 
		{
			walkingAcceleration.x += walkingAccelInc * moveXDirection;
			lastMovementFrame = p.p.frameCount;
		}

		// Move Y Transition
		if (movingY && !slowingY) 
		{
			walkingAcceleration.y += walkingAccelInc * moveYDirection;
			lastMovementFrame = p.p.frameCount;
		}

		// Move Z Transition
		if (movingZ && !slowingZ) 		
		{
			walkingAcceleration.z += walkingAccelInc * moveZDirection;
			lastMovementFrame = p.p.frameCount;
		}

		if(slowingX || slowingY || slowingZ)
		{
			walkingVelocity.mult(camDecelInc);
			walkingAcceleration.mult(camDecelInc);
		}
	}
	
	/***
	 * Apply walking velocity to viewer position
	 */
	private void walk()
	{
		if(p.orientationMode)					// Add relativeVelocity to staticLocation
		{
			location.add(walkingVelocity);	
		}
		else 								// Move the camera
		{
			if(walkingVelocity.x != 0.f)
				camera.truck(walkingVelocity.x);
			if(walkingVelocity.y != 0.f)
				camera.boom(walkingVelocity.y);
			if(walkingVelocity.z != 0.f)
				camera.dolly(walkingVelocity.z);
		}
	}
	
	/***
	 * Apply turning velocity to viewer direction
	 */
	private void turn()
	{
		if(turningVelocity.x != 0.f)
		{
			camera.pan(turningVelocity.x);
//			PApplet.println("turningVelocity.x:"+turningVelocity.x);
		}
		if(turningVelocity.y != 0.f)
		{
			camera.tilt(turningVelocity.y);
//			PApplet.println("turningVelocity.y:"+turningVelocity.y);
		}
	}
	
	private void slow()
	{
		if(p.p.debug.viewer)
			p.display.message("Slowing...");
		
		slowing = true;										// Slowing when close to attractor
	}

	private void halt()
	{
		if(p.p.debug.viewer)
			p.display.message("Halting...");
		
		slowing = false;
		halting = true;										// Slowing when close to attractor
	}

	/**
	 * @param cluster Cluster to calculate vector from camera
	 * @return Current camera orientation as a directional unit vector
	 */
	public PVector getVectorToCluster(WMV_Cluster cluster)
	{
		PVector cameraPosition = getLocation();

		PVector cameraToCluster = new PVector(  cameraPosition.x-cluster.getLocation().x, 	//  Vector from the camera to the face.      
				cameraPosition.y-cluster.getLocation().y, 
				cameraPosition.z-cluster.getLocation().z   );
	
		return cameraToCluster;
	}
	
	/** 
	 * @return Nearest cluster ID in front of camera
	 */
	private int getClusterAhead() 					// Returns the visible cluster closest to the camera
	{
		PVector camOrientation = getOrientationVector();

		IntList nearClusters = getNearClusters(20, p.defaultFocusDistance * 4.f);	// Find 20 nearest clusters -- Change based on density?
		IntList frontClusters = new IntList();
		
		for (int i : nearClusters) 							// Iterate through the clusters
		{
			WMV_Cluster c = p.getCurrentField().clusters.get(i);
			PVector clusterVector = getVectorToCluster(c);
			PVector crossVector = new PVector();
			PVector.cross(camOrientation, clusterVector, crossVector);		// Cross vector gives angle between camera and image
			float result = crossVector.mag();
			
			if(PApplet.abs(result) < fieldOfView && c.getID() != currentCluster && !c.isEmpty())			// If cluster (center) is within field of view
			{
				if(p.p.debug.cluster || p.p.debug.viewer)
					p.display.message("Centered cluster:"+c.getID()+" == "+i+" at angle "+result+" from camera...");
				frontClusters.append(c.getID());
			}
			else
				if(p.p.debug.cluster || p.p.debug.viewer)
					p.display.message("Non-centered, current or empty cluster:"+c.getID()+" at angle "+result+" from camera..."+" NOT centered!");
		}

		float smallest = 100000.f;
		int smallestIdx = 0;

		for (int i = 0; i < frontClusters.size(); i++) 		// Compare distances of clusters in front
		{
			WMV_Cluster c = (WMV_Cluster) p.getCurrentField().clusters.get(i);
			if(p.p.debug.cluster || p.p.debug.viewer)
				p.display.message("Checking Centered Cluster... "+c.getID());
		
			float dist = PVector.dist(getLocation(), c.getLocation());
			if (dist < smallest) 
			{
				if(p.p.debug.cluster || p.p.debug.viewer)
					p.display.message("Cluster "+c.getID()+" is closer!");
				smallest = dist;
				smallestIdx = i;
			}
		}		
		
		if(frontClusters.size() > 0)
			return smallestIdx;
		else
		{
			p.display.message("No clusters ahead!");
			return currentCluster;
		}
	}
	
	/**
	 * Update movement variables and perform interpolation
	 */
	private void updateMovement() 
	{		
		if (	rotatingX || rotatingY || rotatingZ || movingX || movingY || movingZ || zooming || 
				turningX || turningY || waiting || zooming || movingToCluster || movingToAttractor  )
		{
			/* Rotate X Transition */
			if (rotatingX) {
				camera.pan(rotateIncrement * rotateXDirection);
				lastLookFrame = p.p.frameCount;
			}

			/* Rotate Y Transition */
			if (rotatingY) {
				camera.tilt(rotateIncrement * rotateYDirection);
				lastLookFrame = p.p.frameCount;
			}

			/* Rotate Z Transition */
			if (rotatingZ) 
			{
				camera.roll(rotateIncrement * rotateZDirection);
				lastLookFrame = p.p.frameCount;
			}
			
			if(following && waiting)				// If revisiting places in memory and currently waiting
			{				
				if(p.p.frameCount > pathWaitStartFrame + pathWaitLength )
				{
					waiting = false;
					if(p.p.debug.viewer)
						p.display.message("Finished waiting...");

					pathLocationIdx++;
					if(pathLocationIdx < path.size())
					{
						pathGoal = path.get(pathLocationIdx).getLocation();
//						p.display.message("New pathGoal:"+pathGoal);
						
						if(pathLocationIdx >= 1)
						{
							if(pathGoal != path.get(pathLocationIdx-1).getLocation())
							{
								setAttractorPoint(pathGoal);
//								p.display.message("Moving to next attraction point..."+attractorPoint.getLocation());
							}
							else
							{
//								p.display.message("Same attraction point!");
//								turnTowardsPoint(memory.get(revisitPoint).target);			// Turn towards memory target view
							}
						}
					}
					else
					{
						if(p.p.debug.viewer)
						{
							p.display.message("Reached end of path... ");
							p.display.message(" ");
						}
						stopFollowing();
					}
				}
			}

			/* Zoom Transition */
			if (zooming) 
			{
				if (p.p.frameCount < zoomStart + zoomLength) 
				{
					zoomCamera(zoomIncrement / zoomLength * zoomDirection);
				}
				else 
					zooming = false;
			}
		}
		else										// If no transitions
		{
			if(p.p.frameCount % 60 == 0 && optimizeVisibility)		// If not currently turning
			{
				if( !mediaAreVisible(false, 1) )	// Check whether any images are currently visible anywhere in front of camera
				{
					if(p.p.debug.viewer)
						p.display.message("No images visible! will look at nearest image...");
					lookAtNearestMedia();			// Look for images around the camera
				}
				else if(p.getCurrentField().imagesVisible + p.getCurrentField().videosVisible >= mediaDensityThreshold)
				{
					if( mediaAreVisible(false, mediaDensityThreshold) && !p.angleFading )
					{
						if(p.p.debug.viewer)
							p.display.message("Over "+mediaDensityThreshold+" media visible... Set angle fading to true...");
						p.angleFading = true;
					}
				}
			}
		}
	}
	
	/**
	 * Find and look at nearest media to current viewer orientation
	 */
	void lookAtNearestMedia()
	{
		float closestDist = 100000.f;
		int closestID = -1;
		int closestMediaType = -1;
		
		for(int i:p.getCurrentCluster().images)
		{
			WMV_Image img = p.getCurrentField().images.get(i);
			PVector cameraPosition = getLocation();
			PVector camOrientation = getOrientation();
			PVector goal = img.getLocation();
			
			PVector cameraToPoint = new PVector(  cameraPosition.x-goal.x, 	//  Vector from the camera to the point      
					cameraPosition.y-goal.y, 
					cameraPosition.z-goal.z   );
			
			camOrientation.normalize();
			cameraToPoint.normalize();

			float yaw = (float) Math.atan2(cameraToPoint.x, cameraToPoint.z);
			float adj = (float) Math.sqrt(Math.pow(cameraToPoint.x, 2) + Math.pow(cameraToPoint.z, 2)); 
			float pitch = -((float) Math.atan2(adj, cameraToPoint.y) - 0.5f * PApplet.PI);
			
//			turnXToAngle(yaw, 0);		// Calculate which way to turn and start turning in X axis
//			turnYToAngle(pitch, 0);		// Calculate which way to turn and start turning in Y axis
		
			float xStart = getXOrientation();
			float xTarget = yaw;
			PVector turnXInfo = getTurnInfo(xStart, xTarget, 0);
			float yStart = getXOrientation();
			float yTarget = pitch;
			PVector turnYInfo = getTurnInfo(yStart, yTarget, 0);
			
			float turnDist = turnXInfo.y + turnYInfo.y;
			if(turnDist < closestDist)
			{
				closestDist = turnDist;
				closestID = img.getID();
				closestMediaType = 0;
			}
		}
		
		for(int i:p.getCurrentCluster().videos)
		{
			WMV_Video vid = p.getCurrentField().videos.get(i);
			PVector cameraPosition = getLocation();
			PVector camOrientation = getOrientation();
			PVector goal = vid.getLocation();
			
			PVector cameraToPoint = new PVector(  cameraPosition.x-goal.x, 	//  Vector from the camera to the point      
					cameraPosition.y-goal.y, 
					cameraPosition.z-goal.z   );
			
			camOrientation.normalize();
			cameraToPoint.normalize();

			float yaw = (float) Math.atan2(cameraToPoint.x, cameraToPoint.z);
			float adj = (float) Math.sqrt(Math.pow(cameraToPoint.x, 2) + Math.pow(cameraToPoint.z, 2)); 
			float pitch = -((float) Math.atan2(adj, cameraToPoint.y) - 0.5f * PApplet.PI);
			
//			turnXToAngle(yaw, 0);		// Calculate which way to turn and start turning in X axis
//			turnYToAngle(pitch, 0);		// Calculate which way to turn and start turning in Y axis
		
			float xStart = getXOrientation();
			float xTarget = yaw;
			PVector turnXInfo = getTurnInfo(xStart, xTarget, 0);
			float yStart = getXOrientation();
			float yTarget = pitch;
			PVector turnYInfo = getTurnInfo(yStart, yTarget, 0);
			
			float turnDist = turnXInfo.y + turnYInfo.y;
			if(turnDist < closestDist)
			{
				closestDist = turnDist;
				closestID = vid.getID();
				closestMediaType = 2;
			}
		}
		
		lookAtMedia(closestID, closestMediaType);
	}
	
	public void zoomCamera(float zoom)
	{
		fieldOfView += zoom;
		camera.zoom(zoom);
	}

	public void resetCamera()
	{
		initialize( getLocation().x,getLocation().y,getLocation().z );							// Initialize camera
	}
	
	/**
	 * Update teleporting interpolation values
	 */
	private void updateTeleporting()
	{
		if(p.p.frameCount >= teleportStart + p.getCurrentField().teleportLength)		// If the teleport has finished
		{
			if(p.p.debug.viewer)
				p.display.message(" Reached teleport goal...");

			if(teleportWaitingCount > p.getCurrentField().teleportLength * 2.f)
			{
				if(p.p.debug.viewer)
					p.display.message(" Exceeded teleport wait time. Stopping all media...");
				p.getCurrentField().stopAllMediaFading();
			}
			
			if( !p.getCurrentField().mediaAreFading() )			// Once no more images are fading
			{
				if(p.p.debug.viewer)
					p.display.message(" Media finished fading...");

				if(teleportToField != -1)							// If a new field has been specified 
				{
					if(p.p.debug.viewer)
						p.display.message(" Teleported to field "+teleportToField+"...");

					setCurrentField(teleportToField);				// Set new field
					teleportToField = -1;							// Reset target field
				}

				camera.jump(teleportGoal.x, teleportGoal.y, teleportGoal.z);			// Move the camera
				teleporting = false;													// Change the system status
				
				if(p.p.debug.viewer)
					p.display.message(" Teleported to x:"+teleportGoal.x+" y:"+teleportGoal.y+" z:"+teleportGoal.z);

				if(teleportGoalCluster != -1)
				{
					if(movingToTimeSegment)
						setCurrentCluster( teleportGoalCluster, timeSegmentTarget );
					else
						setCurrentCluster( teleportGoalCluster, -1 );
					
					teleportGoalCluster = -1;
				}
				
				if(movingToCluster)
				{
					movingToCluster = false;
					if(attractorCluster != -1)
					{
						attractorCluster = -1;
						p.getCurrentField().clearAllAttractors();
					}
					else
					{
						setCurrentCluster( getNearestCluster(true), -1 );
					}
				}
				if(movingToAttractor)
				{
					movingToAttractor = false;
					setCurrentCluster( getNearestCluster(true), -1 );		// Set currentCluster to nearest

//					if(p.p.debug.viewer) p.display.message("Reached attractor... turning towards image");
//					if(attractorPoint != null)
//						turnTowardsPoint(attractorPoint.getLocation());
					
					p.getCurrentField().clearAllAttractors();	// Clear current attractors
				}
			}
			else
			{
				teleportWaitingCount++;
				if(p.p.debug.viewer && p.p.debug.detailed)
					PApplet.println("Waiting to finish teleport... "+teleportWaitingCount);
			}
		}
	}
	
	/** 
	 * Stop all currently running transitions
	 */
	public void stopAllTransitions()
	{
		if(rotatingX) 
			rotatingX = false;
		if(rotatingY)
			rotatingY = false;
		if(rotatingZ) 
			rotatingZ = false; 
		if(movingX) 
			movingX = false;
		if(movingY)
			movingY = false;
		if(movingZ)
			movingZ = false;
		if(movingToCluster) 
			movingToCluster = false;
		if(turningX)
			turningX = false;
		if(turningY) 
			turningY = false;
		if(zooming)
			zooming = false;

		p.getCurrentField().clearAllAttractors();

		if(waiting)
			waiting = false;
//		if(looking)
//			looking = false;
		if(teleporting)
			teleporting = false;
	}
	
	/**
	 * Follow the current field timeline as a path
	 */
	public void followTimeline(boolean start, boolean fromBeginning)
	{
		if(start)		// Start following timeline
		{
			if(!following)
			{
				path = p.getCurrentField().getTimelineAsPath();			// Get timeline as path of Waypoints matching cluster IDs

				if(path.size() > 0)
				{
					following = true;
					pathLocationIdx = -1;								// Find path start
					
					if(fromBeginning)
					{
						pathLocationIdx = 0;
					}
					else
					{
						int count = 0;
						for(WMV_Waypoint w : path)
						{
							if(w.getID() == p.getCurrentCluster().getID())
							{
								pathLocationIdx = count;
								break;
							}
							count++;
						}

						if(pathLocationIdx == -1) pathLocationIdx = 0;
					}
					
					if(p.p.debug.viewer)
						p.display.message("followTimeline()... Setting first path goal: "+path.get(pathLocationIdx).getLocation());
					
					pathGoal = path.get(pathLocationIdx).getLocation();
					setAttractorPoint(pathGoal);
				}
				else p.display.message("No timeline points!");
			}
			else
			{
				if(p.p.debug.viewer)
					p.display.message("Already called followTimeline(): Stopping... "+path.get(pathLocationIdx).getLocation());
				pathLocationIdx = 0;
				following = false;
			}
		}
		else				// Stop following timeline
		{
			if(following)
			{
				following = false;
				clearAttractorPoint();
			}
		}
	}

	/**
	 * Follow memory path
	 */
	public void followMemory()
	{
		path = new ArrayList<WMV_Waypoint>(memory);								// Follow memory path 
		
		if(path.size() > 0)
		{
			following = true;
			pathLocationIdx = 0;
			if(p.p.debug.viewer)
				p.display.message("--> followMemory() points:"+path.size()+"... Setting first path goal: "+path.get(pathLocationIdx).getLocation());
			pathGoal = path.get(pathLocationIdx).getLocation();
			setAttractorPoint(pathGoal);
		}
		else p.display.message("path.size() == 0!");
	}

	/**
	 * Follow current GPS track
	 */
	public void followGPSTrack()
	{
		path = new ArrayList<WMV_Waypoint>(gpsTrack);								// Follow memory path 
		
		if(path.size() > 0)
		{
			following = true;
			pathLocationIdx = 0;
			if(p.p.debug.viewer)
				p.display.message("--> followGPSTrack() points:"+path.size()+"... Setting first path goal: "+path.get(pathLocationIdx).getLocation());
			pathGoal = path.get(pathLocationIdx).getLocation();
			setAttractorPoint(pathGoal);
		}
		else p.display.message("path.size() == 0!");
	}
	
	/**
	 * Wait for specified time until moving to next memory point
	 * @param length Length of time to wait
	 */
	private void startWaiting(int length)	
	{
		waiting = true;
		pathWaitStartFrame = p.p.frameCount;
		pathWaitLength = length;
	}
	
	/**
	 * @param newPoint Point of interest to attract camera 
	 */
	private void setAttractorPoint(PVector newPoint)
	{
		stopMoving();									// -- Improve by slowing down instead and then starting
		p.getCurrentField().clearAllAttractors();
		movingToAttractor = true;
		attractorPoint = new WMV_Cluster(p.getCurrentField(), 0, newPoint.x, newPoint.y, newPoint.z);
		attractorPoint.setEmpty(false);
		attractorPoint.setAttractor(true);
		attractorPoint.setMass(p.mediaPointMass * 25.f);
		attractionStart = p.p.frameCount;
	}
	
	/**
	 * Clear the current attractor point
	 */
	private void clearAttractorPoint()
	{
		stopMoving();									// -- Improve by slowing down instead and then starting
		p.getCurrentField().clearAllAttractors();
		movingToAttractor = false;
		attractorPoint.setAttractor(false);
	}
	
	/**
	 * @param front Restrict to media in front
	 * @threshold Minimum number of media to for method to return true
	 * @return Whether any media are visible and in front of camera
	 */
	public boolean mediaAreVisible( boolean front, int threshold )
	{
		IntList nearClusters = getNearClusters(10, farViewingDistance + p.defaultFocusDistance); 	

		if(nearClusters.size() == 0)
			return false;
		
		boolean imagesVisible = false;
		ArrayList<WMV_Image> closeImages = new ArrayList<WMV_Image>();		// List of images in range
		boolean panoramasVisible = false;
		ArrayList<WMV_Panorama> closePanoramas = new ArrayList<WMV_Panorama>();		// List of images in range
		boolean videosVisible = false;
		ArrayList<WMV_Video> closeVideos = new ArrayList<WMV_Video>();		// List of images in range
		
		float result;
		
		for(int clusterID : nearClusters)
		{
			WMV_Cluster cluster = p.getCluster(clusterID);
			for( int id : cluster.images )
			{
				WMV_Image i = p.getCurrentField().images.get(id);
				if(i.getViewingDistance() < farViewingDistance + i.getFocusDistance() 
				&& i.getViewingDistance() > nearClippingDistance * 2.f )		// Find images in range
				{
					if(!i.disabled)
						closeImages.add(i);							
				}
			}

			for( int id : cluster.panoramas )
			{
				WMV_Panorama n = p.getCurrentField().panoramas.get(id);
				if(n.getViewingDistance() < farViewingDistance + p.defaultFocusDistance 
						&& n.getViewingDistance() > nearClippingDistance * 2.f )		// Find images in range
				{
					if(!n.disabled)
						closePanoramas.add(n);							
				}
			}

			for( int id : cluster.videos )
			{
				WMV_Video v = p.getCurrentField().videos.get(id);
				if(v.getViewingDistance() <= farViewingDistance + v.getFocusDistance()
				&& v.getViewingDistance() > nearClippingDistance * 2.f )		// Find videos in range
				{
					if(!v.disabled)
						closeVideos.add(v);							
				}
			}
		}
		
		if(closePanoramas.size() > 0)
		{
			panoramasVisible = true;
			return true;
		}

		int visImages = 0;
		for( WMV_Image i : closeImages )
		{
			if(!i.isBackFacing() && !i.isBehindCamera())			// If image is ahead and front facing
			{
				result = PApplet.abs(i.getFacingAngle());			// Get angle at which it faces camera

				if(front)										// Look for centered or only visible image?
				{
					if(result < p.centeredAngle)					// Find closest to camera orientation
					{
						if(p.p.debug.viewer && p.p.debug.detailed)
							p.display.message("Image:"+i.getID()+" result:"+result+" is less than centeredAngle:"+p.centeredAngle);
						imagesVisible = true;
						visImages++;
						if(visImages >= threshold)
							break;
					}
				}
				else
				{
					if(result < p.visibleAngle * 0.66f)						// Find closest to camera orientation
					{
						imagesVisible = true;
						visImages++;
						if(visImages >= threshold)
							break;
					}
				}
			}
		}
		
		int visVideos = 0;
		for(WMV_Video v : closeVideos)
		{
			if(!v.isBackFacing() && !v.isBehindCamera())			// If video is ahead and front facing
			{
				result = PApplet.abs(v.getFacingAngle());			// Get angle at which it faces camera

				if(front)											// Look for centered or only visible image?
				{
					if(result < p.centeredAngle)					// Find closest to camera orientation
					{
						videosVisible = true;
						visVideos++;
						if(visVideos + visImages >= threshold)
							break;
						break;
					}
				}
				else
				{
					if(result < p.visibleAngle * 0.66f)						// Find closest to camera orientation
					{
						videosVisible = true;
						visVideos++;
						if(visVideos + visImages >= threshold)
							break;
					}
				}
			}
		}
		
		if(threshold == 1)
			return imagesVisible || panoramasVisible || videosVisible;
		else
			return (visImages + visVideos) >= threshold;
	}
		
	/**
	 * Add current camera location and orientation to memory
	 */
	public void addPlaceToMemory()
	{
		if(!teleporting && !walking && velocity.mag() == 0.f)		// Only record points when stationary
		{
			WMV_Waypoint curWaypoint = new WMV_Waypoint(path.size(), getLocation(), null);				// -- Calculate time instead of null!!
			curWaypoint.setTarget(getOrientation());
			curWaypoint.setID(currentCluster);						// Need to make sure camera is at current cluster!
			
			while(memory.size() > 100)								// Prevent memory path from getting too long
				memory.remove(0);
				
			memory.add(curWaypoint);
			
			if(p.p.debug.viewer)
			{
				p.display.message("Added point to memory... "+curWaypoint.getLocation());
				p.display.message("Path length:"+memory.size());
			}
		}
		else if(p.p.debug.viewer)
			p.display.message("Couldn't add memory point... walking? "+walking+" teleporting?"+teleporting+" velocity.mag():"+velocity.mag());
	}
	
	/**
	 * clearMemory()
	 * Clear the current memory
	 */
	public void clearMemory()
	{
		following = false;
		waiting = false;
		memory = new ArrayList<WMV_Waypoint>();
	}

	/**
	 * Stop navigation along points in memory
	 */
	public void stopFollowing()
	{
		following = false;
		pathLocationIdx = 0;
	}
	
	/**
	 * Act on the image or video in front of camera. In Selection Mode, selects or deselects the media file.
	 * In Normal Mode, starts or stops a video, but has no effect on an image.
	 */
	public void chooseMediaInFront(boolean select) 
	{
		ArrayList<WMV_Image> possibleImages = new ArrayList<WMV_Image>();
		for(WMV_Image i : p.getCurrentField().images)
		{
			if(i.getViewingDistance() <= selectionMaxDistance)
				if(!i.disabled)
					possibleImages.add(i);
		}

		float closestImageDist = 100000.f;
		int closestImageID = -1;

		for(WMV_Image s : possibleImages)
		{
			if(!s.isBackFacing() && !s.isBehindCamera())					// If image is ahead and front facing
			{
				float result = PApplet.abs(s.getFacingAngle());				// Get angle at which it faces camera

				if(result < closestImageDist)										// Find closest to camera orientation
				{
					closestImageDist = result;
					closestImageID = s.getID();
				}
			}
		}

		ArrayList<WMV_Video> possibleVideos = new ArrayList<WMV_Video>();
		for(WMV_Video v : p.getCurrentField().videos)
		{
			if(v.getViewingDistance() <= selectionMaxDistance)
				if(!v.disabled)
					possibleVideos.add(v);
		}

		float closestVideoDist = 100000.f;
		int closestVideoID = -1;

		for(WMV_Video v : possibleVideos)
		{
			if(!v.isBackFacing() && !v.isBehindCamera())					// If image is ahead and front facing
			{
				float result = PApplet.abs(v.getFacingAngle());				// Get angle at which it faces camera

				if(result < closestVideoDist)								// Find closest to camera orientation
				{
					closestVideoDist = result;
					closestVideoID = v.getID();
				}
			}
		}

		if(selection)						// In Selection Mode
		{
			int newSelected;
			if(select && !multiSelection)
				p.getCurrentField().deselectAllMedia(false);				// If selecting media, deselect all media unless in Multi Selection Mode

			if(closestImageDist < closestVideoDist && closestImageDist != 100000.f)
			{
				newSelected = closestImageID;
				if(p.p.debug.viewer) p.display.message("Selected image in front: "+newSelected);

				if(segmentSelection)											// Segment selection
				{
					int segmentID = -1;
					WMV_Cluster c = p.getCluster( p.getCurrentField().images.get(newSelected).cluster );
					for(WMV_MediaSegment m : c.segments)
					{
						if(m.getImages().hasValue(newSelected))
						{
							segmentID = m.getID();
							break;
						}
					}

					if(select && !multiSelection)
						p.getCurrentField().deselectAllMedia(false);						// Deselect all media

					if(segmentID != -1)
					{
						for(int i : c.segments.get(segmentID).getImages())				// Set all images in selected segment to new state
							p.getCurrentField().images.get(i).setSelected(select);
					}
				}
				else												// Single image selection
				{
					if(newSelected != -1)
						p.getCurrentField().images.get(newSelected).setSelected(select);
				}
			}
			else if(closestVideoDist < closestImageDist && closestVideoDist != 100000.f)
			{
				newSelected = closestVideoID;
				if(p.p.debug.viewer) 	p.display.message("Selected video in front: "+newSelected);

				if(newSelected != -1)
					p.getCurrentField().videos.get(newSelected).setSelected(select);
			}
		}
		else if(closestVideoDist != 100000.f)					// In Normal Mode
		{
			WMV_Video v = p.getCurrentField().videos.get(closestVideoID);

			if(!v.isPlaying())
			{
				if(!v.isLoaded()) v.loadMedia();
				v.playVideo();
			}
			else
				v.stopVideo();
			
			if(p.p.debug.viewer) 
				p.display.message("Video is "+(v.isPlaying()?"playing":"not playing: ")+v.getID());
		}
	}
	
	/**
	 * Turn camera side to side, at different elevation angles, until images in range become visible
	 */
//	public void lookForImages()
//	{
//		stopAllTransitions();										// Stop all current transitions
//		lookingStartAngle = getOrientation().x;
//		lookingStartFrameCount = p.p.frameCount;
//		lookingDirection = Math.round(p.p.random(1)) == 1 ? 1 : -1;		// Choose random direction to look
//		lookingLength = PApplet.round(2.f*PApplet.PI / rotateIncrement);
//		turnXToAngle(lookingStartAngle, lookingDirection);
//		looking = true;
//	}	
	
	/**
	 * Update turning to look for images 
	 */
//	private void updateLooking()
//	{
//		if(looking)	// If looking for images
//		{
//			if( mediaAreVisible( true ) )				// Check whether any images are visible and centered
//			{
//				if(p.p.debug.viewer)
//					p.display.message("Finished rotating to look, found image(s) ");
//				stopAllTransitions();			// Also sets lookingForImages to false
//			}
//			else
//			{
//				lastLookFrame = p.p.frameCount;
//
//				if ( p.p.frameCount - lookingStartFrameCount > lookingLength )
//				{
//					lookingRotationCount++;							// Record camera rotations while looking for images
//					if(p.p.debug.viewer)
//						p.display.message("Rotated to look "+lookingRotationCount+" times...");
//					lookingStartFrameCount = p.p.frameCount;		// Restart the count
//				}
//
//				if (lookingRotationCount > 2) 
//				{
//					if(p.p.debug.viewer)
//						p.display.message("Couldn't see any images. Moving to next nearest cluster...");
//					stopAllTransitions();							// Sets lookingForImages and all transitions to false
//					moveToNearestCluster(false);
//				}
//			}
//		}
//	}

	/**
	 * Get list of closest clusters
	 * @param n Number of closest clusters to return
	 * @return Closest <n> clusters
	 */
	public IntList getClosestClusters(int n)				// Return list of IDs of n closest clusters to the current location (not including current cluster)
	{
		IntList list;
		if(n > 0)
		{
			list = new IntList();

			int leastIdx = -1;			// closest cluster ID in list
			float leastDist = 100000.f;			
			int highestIdx = -1;		// farthest cluster ID in list
			float highestDist = 0.f;		

			for(WMV_Cluster c : p.getCurrentField().clusters)
			{
				if(!c.isEmpty())
				{
					float dist = PVector.dist(c.getLocation(), location);
					float highest = 0.f;

					if(dist > highest)
						highest = dist;

					if(leastIdx == -1)				// Init leastIdx
					{
						leastIdx = c.getID();
						leastDist = dist;
					}

					if(highestIdx == -1)			// Init highestIdx
					{
						highestIdx = c.getID();
						highestDist = dist;
					}

					if(list.size() < n)				// Fill list with first 3 cluster IDs
					{
						if(!list.hasValue(c.getID()) && !(currentCluster == c.getID()))
							list.append(c.getID());
					}
					else
					{
						for(int i : list)				// Sort the list lowest to highest
						{
							float checkDist = PVector.dist(p.getCurrentField().clusters.get(i).getLocation(), location);
							if(checkDist < leastDist)
							{
								leastDist = checkDist;
								leastIdx = i;
							}
							if(checkDist > highestDist)
							{
								highestDist = checkDist;
								highestIdx = i;
							}
						}

						while(list.size() > n)		// Trim any extra elements from list
							list.removeValue(highestIdx);
					}

					if(dist < leastDist && !(currentCluster == c.getID()))		// Ignore the current cluster, since the distance is zero (?)
					{
						if(list.size() >= n)
							list.remove(n-1);				// Remove highest

						leastIdx = c.getID();
						leastDist = dist;
						if(!list.hasValue(c.getID()))
							list.append(leastIdx);			// Replace with new lowest
					}
				}
			}
		}
		else
		{
			list = new IntList();
			list.append(currentCluster);
		}

		return list;
	}

	/**
	 * Set nearby cluster timeline to given timeline
	 * @param newTimeline List of time segments
	 */
	void setNearbyClusterTimeline(ArrayList<WMV_TimeSegment> newTimeline)
	{
		nearbyClusterTimeline = newTimeline;
		nearbyClusterTimelineMediaCount = 0;
		
		for(WMV_TimeSegment t : nearbyClusterTimeline)
			nearbyClusterTimelineMediaCount += t.getTimeline().size();

//		PApplet.println("setNearbyClusterTimeline  nearbyClusterTimeline.size():"+nearbyClusterTimeline.size());
	}

	/**
	 * Create nearby cluster timeline from given clusters
	 * @param clusters List of clusters
	 */
	public void createNearbyClusterTimeline(ArrayList<WMV_Cluster> clusters)
	{
		ArrayList<WMV_TimeSegment> timeline = new ArrayList<WMV_TimeSegment>();
		
		if(p.p.debug.time)
			PApplet.println(">>> Creating Viewer Timeline (Nearby Visible Clusters)... <<<");

		for(WMV_Cluster c : clusters)											// Find all media cluster times
			for(WMV_TimeSegment t : c.getTimeline())
				timeline.add(t);

		timeline.sort(WMV_TimeSegment.WMV_TimeLowerBoundComparator);				// Sort time segments 
		nearbyClusterTimeline = timeline;
	
		nearbyClusterTimelineMediaCount = 0;
		
		for(WMV_TimeSegment t : nearbyClusterTimeline)
			nearbyClusterTimelineMediaCount += t.getTimeline().size();

		if(p.p.debug.time)
			PApplet.println("createNearbyClusterTimeline  nearbyClusterTimeline.size():"+nearbyClusterTimeline.size());
	}

	public WMV_Time getNearbyTimeByIndex(int timelineIndex)
	{
		WMV_Time time = null;
		int timelineCt = 0, mediaCt = 0;
		
		for(WMV_TimeSegment t : nearbyClusterTimeline)
		{
			int nextSegmentStart = mediaCt + t.getTimeline().size();
			if(timelineIndex < nextSegmentStart)
			{
				if(timelineCt < nearbyClusterTimeline.size())
				{
					if(nearbyClusterTimeline.get(timelineCt).getTimeline().size() > timelineIndex - mediaCt)
						time = nearbyClusterTimeline.get(timelineCt).getTimeline().get(timelineIndex - mediaCt);
					else
					{
						PApplet.println("timelineIndex - mediaCt > nearbyClusterTimeline.get(timelineCt).getTimeline().size()!!.. timelineIndex:"+timelineIndex+" mediaCt:"+mediaCt);
					}
				}
				else
				{
					PApplet.println("Searched for timelineIndex:"+ timelineIndex +" which was past end of timeline: "+(timelineCt-1));
					break;
				}
			}
			else
			{
				mediaCt += t.getTimeline().size();
			}
		}
		
//		int curMediaID = nearbyClusterTimeline.get(timelineIndex).get().getID();
//		int curMediaType = nearbyClusterTimeline.get(timelineIndex).get().getMediaType();
		return time;
	}


	/**
	 * @return Image closest to directly in front of the camera
	 */
	public int getFrontImage() {
		float smallest = 100000.f;
		int smallestIdx = 0;

		WMV_Field f = p.getCurrentField();

		for (int i = 0; i < f.images.size(); i++) {
			if (f.images.get(i).visible) {
				float imageAngle = f.images.get(i).getFacingAngle();
				if (imageAngle < smallest) {
					smallest = imageAngle;
					smallestIdx = i;
				}
			}
		}

		return smallestIdx;
	}

	/**
	 * @return Image nearest to the camera in any direction
	 */
	public int getNearestImage() {
		float smallest = 100000.f;
		int smallestIdx = 0;
		WMV_Field f = p.getCurrentField();

		for (int i = 0; i < f.images.size(); i++) {
			if (f.images.get(i).visible) {
				float imageDist = f.images.get(i).getViewingDistance();
				if (imageDist < smallest && imageDist > nearClippingDistance) {
					smallest = imageDist;
					smallestIdx = i;
				}
			}
		}

		return smallestIdx;
	}
	
	/**
	 * @return Video closest to directly in front of the camera
	 */
	public int getFrontVideo() {
		float smallest = 100000.f;
		int smallestIdx = 0;

		WMV_Field f = p.getCurrentField();

		for (int i = 0; i < f.videos.size(); i++) {
			if (f.videos.get(i).visible) {
				float videoAngle = f.videos.get(i).getFacingAngle();
				if (videoAngle < smallest) {
					smallest = videoAngle;
					smallestIdx = i;
				}
			}
		}

		return smallestIdx;
	}

	/**
	 * @return Video nearest to the camera in any direction
	 */
	public int getNearestVideo() {
		float smallest = 100000.f;
		int smallestIdx = 0;
		WMV_Field f = p.getCurrentField();

		for (int i = 0; i < f.videos.size(); i++) {
			if (f.videos.get(i).visible) {
				float videoDist = f.videos.get(i).getViewingDistance();
				if (videoDist < smallest && videoDist > nearClippingDistance) {
					smallest = videoDist;
					smallestIdx = i;
				}
			}
		}

		return smallestIdx;
	}
	
	/**
	 * Open dialog to select GPS track file
	 */
	public void importGPSTrack()
	{
		gpsTrackSelected = false;
		p.p.selectInput("Select a GPS Track:", "gpsTrackSelected");
	}

	/**
	 * Load and analyze GPS track file in response to user selection
	 * @param selectedFile Selected GPS track file
	 */
	public void loadGPSTrack(File selectedFile) 
	{
		if (selectedFile == null) 
		{
			PApplet.println("loadGPSTrack() window was closed or the user hit cancel.");
		} 
		else 
		{
			String input = selectedFile.getPath();

			if(p.p.debug.viewer)
				p.display.message("User selected GPS Track: " + input);

			gpsTrackName = input;
			
			try
			{
				String[] parts = gpsTrackName.split("/");
				String fileName = parts[parts.length-1];
				
				parts = fileName.split("\\.");

				if(parts[parts.length-1].equals("gpx"))				// Check that it's a GPX file
				{
					gpsTrackFile = new File(gpsTrackName);
					gpsTrackSelected = true;
				}
				else
				{
					gpsTrackSelected = false;
					p.display.message("Bad GPS Track.. doesn't end in .GPX!:"+input);
				}
			}
			catch (Throwable t)
			{
				PApplet.println("loadGPSTrack() Error... Throwable: "+t);
			}
		}

		if(gpsTrackSelected)
		{
			analyzeGPSTrack();
			getSoundLocationsFromGPSTrack();
		}
	}

	/**
	 * Analyze current GPS track
	 */
	public void analyzeGPSTrack()
	{
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(gpsTrackFile);

			//http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			System.out.println("\nAnalyzing GPS Track:"+gpsTrackName);
			System.out.println("Root Node:" + doc.getDocumentElement().getNodeName());
			System.out.println("----");

			NodeList allNodes = doc.getElementsByTagName("*");
			
			int len;
			int count = 0;
			
			len = allNodes.getLength();
			
			for (int h=0; h < 5; h++)												// Iterate through each item in .gpx XML file
			{
				Element e;
				e = (Element)allNodes.item(h);								// Location
				System.out.println("Node "+h+" is "+e.getTagName() + ":");
			}
			for (int h=4; h < len; h+=3)												// Iterate through each item in .gpx XML file
			{
				NamedNodeMap locationNodeMap;
				Element locationVal, elevationVal, timeVal;

				locationVal = (Element)allNodes.item(h);								// Location
				elevationVal = (Element)allNodes.item(h+1);								// Location
				timeVal = (Element)allNodes.item(h+2);								// Location

//				System.out.println("Node "+h+" Start ---> "+locationVal.getTagName() + ":");

				/* Parse Location */
				locationNodeMap = locationVal.getAttributes();

				float latitude = 0.f, longitude = 0.f;
				float elevation = 0.f;
//				float time = 0.f;

				Node latitudeVal, longitudeVal;
				latitudeVal = locationNodeMap.item(0);
				longitudeVal = locationNodeMap.item(1);

				latitude = Float.parseFloat(latitudeVal.getNodeValue());
				longitude = Float.parseFloat(longitudeVal.getNodeValue());
				elevation = Float.parseFloat(elevationVal.getTextContent());

				/* Parse Node Date */
				String dateTimeStr = timeVal.getTextContent(); 						// Ex string: 2016-05-01T23:55:33Z
																					// <time>2017-02-05T23:31:23Z</time>
				String[] parts = dateTimeStr.split("T");
				String dateStr = parts[0];			
				String timeStr = parts[1];

				parts = dateStr.split("-");
				String yearStr, monthStr, dayStr;
				yearStr = parts[0];
				monthStr = parts[1];
				dayStr = parts[2];
				int year = Integer.parseInt(yearStr);
				int month = Integer.parseInt(monthStr);
				int day = Integer.parseInt(dayStr);

				/* Parse Node Time */
				parts = timeStr.split("Z");
				timeStr = parts[0];

				parts = timeStr.split(":");
				String hourStr, minuteStr, secondStr;

				hourStr = parts[0];
				minuteStr = parts[1];
				secondStr = parts[2];
				int hour = Integer.parseInt(hourStr);
				int minute = Integer.parseInt(minuteStr);
				int second = Integer.parseInt(secondStr);
				
//				hour = p.p.utilities.utcToPacificTime(hour);						// Convert from UTC Time

				Calendar utc = Calendar.getInstance();
				utc.set(year, month, day, hour, minute, second);
				WMV_Time utcTime = new WMV_Time( p, utc, count, 0 );
				WMV_Time pacificTime = p.p.utilities.utcToPacificTime(utcTime);

				float newX = 0.f, newZ = 0.f, newY = 0.f;

				if(p.getCurrentField().model.highLongitude != -1000000 && p.getCurrentField().model.lowLongitude != 1000000 && p.getCurrentField().model.highLatitude != -1000000 && p.getCurrentField().model.lowLatitude != 1000000 && p.getCurrentField().model.highAltitude != -1000000 && p.getCurrentField().model.lowAltitude != 1000000)
				{
					if(p.getCurrentField().model.highLongitude != p.getCurrentField().model.lowLongitude && p.getCurrentField().model.highLatitude != p.getCurrentField().model.lowLatitude)
					{
						WMV_Model m = p.getCurrentModel();
						newX = PApplet.map(longitude, m.lowLongitude, m.highLongitude, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth); 			// GPS longitude decreases from left to right
						newY = -PApplet.map(elevation, m.lowAltitude, m.highAltitude, 0.f, m.fieldHeight); 										// Convert altitude feet to meters, negative sign to match P3D coordinate space
						newZ = -PApplet.map(latitude, m.lowLatitude, m.highLatitude, -0.5f * m.fieldLength, 0.5f*m.fieldLength); 			// GPS latitude increases from bottom to top, minus sign to match P3D coordinate space
						
						if(p.altitudeScaling)	
							newY *= p.altitudeScalingFactor;
					}
					else
					{
						newX = newY = newZ = 0.f;
					}
				}

				if(p.p.debug.viewer)
				{
					PApplet.print("--> latitude:"+latitude);
					PApplet.print("  longitude:"+longitude);
					PApplet.println("  elevation:"+elevation);
					PApplet.print("newX:"+newX);
					PApplet.print("  newY:"+newY);
					PApplet.println("  newZ:"+newZ);

					PApplet.print("hour:"+hour);
					PApplet.print("  minute:"+minute);
					PApplet.print("  second:"+second);
					PApplet.print("  year:"+year);
					PApplet.print("  month:"+month);
					PApplet.println("  day:"+day);
				}

				PVector newLoc = new PVector(newX, newY, newZ);

				WMV_Waypoint wp = new WMV_Waypoint(count, newLoc, pacificTime);		// GPS track node as a Waypoint
				gpsTrack.add(wp);													// Add Waypoint to gpsTrack
				
				count++;
			}
			PApplet.println("Added "+count+" nodes to gpsTrack...");
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void getSoundLocationsFromGPSTrack()
	{
		for(WMV_Sound s : p.getCurrentField().sounds)
		{
			s.calculateLocationFromGPSTrack(gpsTrack);
		}
	}
	
	/**
	 * Get current time				-- ????
	 * @return Current time
	 */
//	public GMV_Time getCurrentTime()
//	{
//		GMV_Time curTime = new GMV_Time(p, null);
//		return curTime;
//	}
	
	/**
	 * Get vector of direction of camera motion by comparing current and previous waypoints
	 * @return Vector of direction of camera motion
	 */
	public PVector getHistoryVector()
	{
		PVector hv = new PVector();
		
		if(history.size() > 1)
		{
			WMV_Waypoint w1 = history.get(history.size()-1);
			WMV_Waypoint w2 = history.get(history.size()-2);
			
//			float dist = w1.getDistance(w2);
			
			hv = new PVector(  w1.getLocation().x-w2.getLocation().x, 	//  Vector from the camera to the face.      
					w1.getLocation().y-w2.getLocation().y, 
					w1.getLocation().z-w2.getLocation().z   );			
		}
		
		return hv;
	}


	/**
	 * pointsAreInList()
	 * @param check Waypoints to check
	 * @param memory How far back in memory to look
	 * @return Waypoints found in history within the last <memory> waypoints
	 */
	public ArrayList<WMV_Waypoint> pointsAreInList(ArrayList<WMV_Waypoint> check, int memory)
	{
		ArrayList<WMV_Waypoint> found = new ArrayList<WMV_Waypoint>();
		
		for( WMV_Waypoint p : check )
		{
//			PApplet.println("pointsAreInList()... memory:"+memory);

//			for( GMV_Waypoint w : history )
			for(int i = history.size()-1; i >= history.size()-memory; i--)		// Iterate through history from last element to 
			{
				PApplet.println("i:"+i);
				WMV_Waypoint w = history.get(i);
				
				if(p.getLocation() == w.getLocation())
				{
					found.add(p);
				}
			}
		}
		
		return found;
	}
	
	/**
	 * clustersAreInList()
	 * @param check List of clusters to check
	 * @param memory How far back to look in memory 
	 * @return Clusters found within the last <memory> waypoints
	 */
	public ArrayList<WMV_Cluster> clustersAreInList(IntList check, int memory)
	{
		ArrayList<WMV_Cluster> found = new ArrayList<WMV_Cluster>();
		
		for( int cPoint : check )
		{
			PApplet.println("clustersInList()... memory:"+memory);

			for(int i = history.size()-1; i >= history.size()-memory; i--)		// Iterate through history from last element to 
			{
				PApplet.println("i:"+i);
				WMV_Waypoint w = history.get(i);
				
				if(p.getCurrentField().clusters.get(cPoint).getLocation() == w.getLocation())
				{
					found.add(p.getCurrentField().clusters.get(cPoint));
				}
			}
		}
		
		return found;
	}
	
	public void setFarViewingDistance( float newFarViewingDistance)
	{
		farViewingDistance = newFarViewingDistance;
	}

	public void setNearClippingDistance( float newNearClippingDistance)
	{
		nearClippingDistance = newNearClippingDistance;
		nearViewingDistance = nearClippingDistance * 2.f;
	}
	
	public boolean isMoving()
	{
		if(movingToCluster || movingToAttractor)
			return true;
		else
			return false;
	}
	
	public int getField()
	{
		return field;
	}

	public ArrayList<WMV_Waypoint> getPath()
	{
		return path;
	}

	public ArrayList<WMV_Waypoint> getGPSTrack()
	{
		return gpsTrack;
	}

	public boolean isMovingToAttractor()
	{
		return movingToAttractor;
	}

	public boolean isMovingToCluster()
	{
		return movingToCluster;
	}
	
	public boolean isFollowing()
	{
		return following;
	}

	public boolean isWalking()
	{
		return walking;
	}

	public boolean isSlowing()
	{
		return slowing;
	}

	public boolean isHalting()
	{
		return halting;
	}
	
	public int getAttractorCluster()
	{
		return attractorCluster;
	}

	public int getCurrentClusterID()
	{
		return currentCluster;
	}

	public int getLastCluster()
	{
		return lastCluster;
	}
	
	public float getFieldOfView()
	{
		return fieldOfView;
	}
	
	public float getInitFieldOfView()
	{
		return initFieldOfView;
	}
	
	public float getNearClippingDistance()
	{
		return nearClippingDistance;
	}
	
	public float getFarViewingDistance()
	{
		return farViewingDistance;
	}
	
	public float getNearViewingDistance()
	{
		return nearViewingDistance;
	}
	
	public float getClusterNearDistance()
	{
		return clusterNearDistance;
	}
	
	void setCurrentCluster(int newCluster, int newFieldTimeSegment)
	{
		lastCluster = currentCluster;

		WMV_Cluster c = p.getCurrentCluster();
		c.timeFading = false;

		currentCluster = newCluster;
		c = p.getCurrentCluster();
		c.timeFading = true;
		
		WMV_Field f = p.getCurrentField();
		if(newFieldTimeSegment == -1)						// If == -1, search for time segment
		{
			for(WMV_TimeSegment t : f.timeline)			// Search field timeline for cluster time segment
			{
				if(c.timeline != null)
				{
					if(t.equals(f.getTimeSegmentInCluster(c.getID(), 0)))			// Compare cluster time segment to field time segment
						currentFieldTimeSegment = t.getID();						// If match, set currentFieldTimeSegment
				}
				else
					PApplet.println("Current Cluster timeline is NULL!:"+c.getID());
			}
		}
		else
			currentFieldTimeSegment = newFieldTimeSegment;					// Set currentFieldTimeSegment to given value
		
		WMV_Date d = f.getDateInCluster(c.getID(), 0);
		if(d != null) currentFieldDate = d.getID();
		else PApplet.println("currentFieldDate would have been set to null..");
		
		if(p.getTimeMode() == 2 && !teleporting)
			p.createTimeCycle();
	}

	public void startMoveXTransition(int dir)
	{
		moveXDirection = dir;
		movingX = true;
	}
	
	public void startMoveYTransition(int dir)
	{
		moveYDirection = dir;
		movingY = true;
	}
	
	public void startMoveZTransition(int dir)
	{
		moveZDirection = dir;
		movingZ = true;
	}
	
	public void stopMoveXTransition()
	{
		movingX = false;
		slowingX = true;
	}
	
	public void stopMoveYTransition()
	{
		movingY = false;
		slowingY = true;
	}
	
	public void stopMoveZTransition()
	{
		movingZ = false;
		slowingZ = true;
	}
	
	public void stopRotateXTransition()
	{
		rotatingX = false;
	}
	
	public void stopRotateYTransition()
	{
		rotatingY = false;
	}
	
	public void stopRotateZTransition()
	{
		rotatingZ = false;
	}
	
	public void startZoomTransition(int dir)
	{
		zoomStart = p.p.frameCount;
		zoomDirection = dir;
		zooming = true;
	}
	
	/***
	 * jump()
	 * @param dest   Destination to jump to
	 * Jump to a point
	 */
	public void jumpTo(PVector dest)
	{
		camera.jump(dest.x, dest.y, dest.z);					
	}

//	/**
//	 * jumpAndPointAtTarget()
//	 * @param goal Goal point
//	 * @param target Location to point at
//	 */
//	public void jumpAndPointAtTarget(PVector goal, PVector target) 
//	{
//		initialize(goal.x, goal.y, goal.z);
//		camera.aim(target.x, target.y, target.z);
//	}
}
