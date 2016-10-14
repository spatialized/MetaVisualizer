package gmViewer;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import damkjer.ocd.Camera;
import processing.core.PApplet;
//import peasy.PeasyCam;
//import processing.core.PApplet;
import processing.core.PVector;
import processing.data.FloatList;
import processing.data.IntList;

/*********************************
 * GMV_Viewer
 * @author davidgordon
 * Represents the viewer able to navigate and interact with multimedia in virtual 3D environment
 */
public class GMV_Viewer 
{
	/* Camera */
	Camera camera;									 				// Camera object
	private boolean firstCamInitialization = true;					// Whether the camera has been initialized	
	
	private float fieldOfView = PApplet.PI * 0.375f;				// Field of view
	private final float defaultFieldOfView = PApplet.PI / 3.0f;		// Default FOV
	private final float initFieldOfView = fieldOfView; 				// Initial field of view
	private float rotateIncrement = 3.1415f / 256.f;				// Rotation amount each frame when turning
	private float zoomIncrement = 3.1415f / 32.f;					// Zoom amount each frame when zooming

	private float nearClippingDistance = 3.f; 						// Distance (m.) of near clipping plane
	private float nearViewingDistance = nearClippingDistance * 2.f; // Near distance (m.) at which media start fading out
	private float farViewingDistance = 16.f; 						// Far distance (m.) at which media start fading out

	/* Time */
	public int currentFieldTimeSegment = 0;				// Current time segment in field timeline
	public int currentClusterTimeSegment = 0;			// Current time segment in cluster timeline
	
	/* Memory */
	private ArrayList<GMV_Waypoint> memory;				// Path for camera to take
	private ArrayList<GMV_Waypoint> path; 				// Record of camera path
	
	private boolean movingToAttractor = false;			// Moving to attractor point anywhere in field
	private boolean movingToCluster = false;			// Moving to cluster 
	private PVector pathGoal;							// Next goal point for camera in navigating from memory
	private boolean follow = false;						// Is the camera currently navigating from memory?
	private int pathLocationIdx;						// Index of current cluster in memory
	
	/* Clusters */
	private int field = 0;								// Current field
	public IntList clustersVisible;						// Clusters visible to camera (Static mode) 
	private int currentCluster = 0;						// Image cluster currently in view
	GMV_Cluster attractorPoint;							// For navigation to points outside cluster list
	private int attractorCluster = -1;					// Cluster attracting the camera
	private int attractionStart = 0;
	private int teleportGoalCluster = -1;				// Cluster to navigate to (-1 == none)
	private float clusterNearDistance;					// Distance from cluster center to slow down to prevent missing the target
	private float clusterNearDistanceFactor = 2.f;			// Multiplier for clusterCenterSize to get clusterNearDistance

	/* Interaction Modes */
	public boolean mapMode = false;					// 2D Map Mode
	public boolean selectionMode = false;			// Allows selection, increases transparency to make selected image(s) easier to see
	public boolean autoNavigation = false;			// Attraction towards centers of clusters
	public boolean lockToCluster = false;			// Automatically move viewer to nearest cluster when idle
	public boolean mouseNavigation = false;			// Mouse navigation
	public boolean videoMode = false;

	/* Teleporting */
	private PVector teleportGoal;				// Where in virtual space should the camera go next?
	private boolean teleporting = false;				// Transition where all images fade in or out
	private int teleportStart;							// Frame that fade transition started
	private int teleportToField = -1;					// What field ID to fade transition to	 (-1 remains in current field)
	private int teleportWaitingCount = 0;		// How long has the viewer been waiting for media to fade out before teleport?
	
	/* Physics */
	private PVector location, orientation;											// Location of the camera in virtual space
	public PVector velocity, acceleration, attraction;      // Physics model parameters

	public float lastAttractorDistance = -1.f;
	
	public float cameraMass = 0.33f;						// Camera mass for cluster attraction
	private float velocityMin = 0.00005f;						// Threshold under which velocity counts as zero
	private float velocityMax = 0.75f;						// Camera maximum velocity
	private float accelerationMax = 0.15f;					// Camera maximum acceleration
	private float accelerationMin = 0.00001f;				// Threshold under which acceleration counts as zero
	private float camDecelInc = 0.8f;						// Camera deceleration increment
	private float camHaltInc = 0.05f;						// Camera fast deceleration increment

	/* Movement */
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
	

	/* Looking */
	private boolean looking = false;				// Whether viewer is turning to look for images, since none are visible
	private int lookingStartFrameCount, lookingLength, lookingDirection;
	private int lookingRotationCount = 0;		// Amount of times viewer has rotated looking for images
	private float lookingStartAngle;				// Angle when looking started
	private boolean waiting = false;			// During memory navigation, whether the camera is waiting to move 
	private int pathWaitStartFrame, pathWaitLength = 25;

	/* Turning */
	private PVector turnTargetPoint;			// Point to turn towards
	private boolean turningX = false;			// Whether the viewer is turning (right or left)
	private boolean turningY = false;			// Whether the viewer is turning (up or down)
	
	private int turnXStartFrame, turnYStartFrame, 
				turnXTargetFrame, turnYTargetFrame;
	private float turnXDirection, turnXTarget, 
				  turnXStart, turnXIncrement;
	private float turnYDirection, turnYTarget,
				  turnYStart, turnYIncrement;
	private float turnIncrement = PApplet.PI / 90.f;
	
	private boolean rotatingX = false;			// Whether the camera is rotating in X dimension (turning left or right)?
	private boolean rotatingY = false;			// Whether the camera is rotating in Y dimension (turning up or down)?
	private boolean rotatingZ = false;			// Whether the camera is rotating in Z dimension (rolling left or right)?
	private float rotateXDirection;				// Rotation direction in X dimension
	private float rotateYDirection;				// Rotation direction in Y dimension
	private float rotateZDirection;				// Rotation direction in Z dimension

	/* Interaction */
	private float selectionMaxDistance;					// Maximum distance user can select a photo
	private float selectionMaxDistanceFactor = 2.f;		// Scaling from defaultFocusDistanceFactor to selectionMaxDistance
	public int lastMovementFrame = 500000, lastLookFrame = 500000;
	private int clusterLockIdleFrames = 0;				// How long to wait after user input before auto navigation moves the camera?
	private int lockToClusterWaitLength = 40;

	/* GPS Tracks */
	private File gpsTrackFile;							// GPS track file
	private ArrayList<GMV_Waypoint> history;			// Stores a GPS track in virtual coordinates
	private ArrayList<GMV_Waypoint> gpsTrack;			// Stores a GPS track in virtual coordinates
	private boolean gpsTrackSelected = false;			// Has a GPS track been selected?
	private String gpsTrackName = "";					// GPS track name

	/* Zooming */
	private boolean zooming = false;
	private float zoomStart, zoomDirection;
	private int zoomLength = 15;

	GeoSynth p;

	public GMV_Viewer(GeoSynth parent)
	{
		p = parent;

		location = new PVector(0,0,0);
		velocity = new PVector(0,0,0);
		acceleration = new PVector(0,0,0);
		attraction = new PVector(0,0,0);
		walkingVelocity = new PVector(0,0,0);
		walkingAcceleration = new PVector(0,0,0);

		fieldOfView = initFieldOfView; 		// Field of view

		history = new ArrayList<GMV_Waypoint>();
		gpsTrack = new ArrayList<GMV_Waypoint>();

		memory = new ArrayList<GMV_Waypoint>();
		path = new ArrayList<GMV_Waypoint>();
		teleportGoal = new PVector(0, 0, 0);

		field = 0;
		currentCluster = 0;
		clusterNearDistance = p.clusterCenterSize * clusterNearDistanceFactor;

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
		camera = new Camera( p, x, y, z, 0.f, 0.f, 0.f, 0.f, 1.f, 0.f, fieldOfView, nearClippingDistance, 10000.f);
		location = new PVector(x, y, z);
		teleportGoal = new PVector(x, y, z);
		
		fieldOfView = initFieldOfView;

		if(firstCamInitialization)
			firstCamInitialization = false;

		selectionMaxDistance = p.defaultFocusDistance * selectionMaxDistanceFactor;
	}

	/*** 
	 * Update camera movement and variables each frame
	 */
	void update()
	{
		if(!p.transitionsOnly)
			location = new PVector(camera.position()[0], camera.position()[1], camera.position()[2]);		/* Update location */

		updateWalking();						/* Update walking */
		updatePhysics();						/* Update physics */

		if(teleporting) updateTeleporting();	/* Update teleporting */
		updateMovement();						/* Update navigation */
		if(autoNavigation) updateLooking();		/* Update looking */

		p.getCurrentField().getAttractingClusters().size();
		
		if(lockToCluster && !walking)										// Update locking to nearest cluster 
		{
			if(p.getCurrentField().getAttractingClusters().size() > 0)					// If being attracted to a point
			{
				if(clusterLockIdleFrames > 0) clusterLockIdleFrames = 0;											
			}
			else										// If idle
			{
				clusterLockIdleFrames++;										// Count frames with no attracting clusters
				if(clusterLockIdleFrames > lockToClusterWaitLength)			// If after wait length, lock to nearest cluster
				{
					int nearest = getNearestCluster(true);					// Get nearest cluster location, including current cluster
					GMV_Cluster c = p.getCluster(nearest);
					if(c.getClusterDistance() > p.clusterCenterSize * 2.f)	// If the nearest cluster is farther than threshold distance
						moveToCluster(c.getID(), false);					// Move to nearest cluster
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
	 * @param dest	Destination point in 3D virtual space
	 * @param fadeTransition  Use fade transition (true) or jump (false)
	 */
	public void teleportToPoint( PVector dest, boolean fadeTransition ) 
	{
		if(p.transitionsOnly)
		{
			teleportGoal = dest;
			location = dest;
		}
		else
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
	 * @param newField  New field
	 * Set specified field as current field
	 */
	public void setCurrentField(int newField)		
	{
		if(newField < p.getFieldCount())
			field = newField;

		p.display.initializeSmallMap();
		p.display.initializeLargeMap();

		if(p.debug.field || p.debug.viewer)		
			p.display.message("Set new field:"+field);

		initialize(0,0,0);							// Initialize camera

		if(p.debug.field || p.debug.viewer)		
			p.display.message("Moving (teleporting) to nearest cluster:"+field);

		moveToNearestCluster(true);					// Teleport to new location						
	}

	/**
	 * @param teleport  Whether to teleport (true) or navigate (false)
	 * Move camera to the nearest cluster
	 */
	void moveToCaptureLocation(int imageID, boolean teleport) 
	{
		if (p.debug.viewer)
			p.display.message("Moving to capture location... "+imageID);

		PVector newLocation = p.getFieldImages().get(imageID).getCaptureLocation();
		
		if(teleport)
		{
			teleportToPoint(newLocation, true);
		}
		else
		{
			if(teleporting)	teleporting = false;
			if(p.debug.viewer && p.debug.detailed)
			p.display.message("moveToCaptureLocation... setting attractor point:"+newLocation);
			setAttractorPoint(newLocation);
		}
	}
	
	/**
	 * @param teleport  Whether to teleport (true) or navigate (false)
	 * Move camera to the nearest cluster
	 */
	void moveToCluster(int newCluster, boolean teleport) 
	{
		if (p.debug.viewer)
			p.display.message("Moving to nearest cluster... "+newCluster);

		if(teleport)
		{
			teleportGoalCluster = newCluster;
			PVector newLocation = ((GMV_Cluster) p.getCurrentField().clusters.get(newCluster)).getLocation();
			teleportToPoint(newLocation, true);
		}
		else
		{
			if(teleporting)	teleporting = false;
			currentCluster = newCluster;
			p.display.message("moveToNearestCluster... setting attractor and currentCluster:"+currentCluster);
			setAttractorCluster(currentCluster);
		}
	}
	
	/**
	 * moveToNearestCluster()
	 * @param teleport  Whether to teleport (true) or navigate (false)
	 * Move camera to the nearest cluster
	 */
	void moveToNearestCluster(boolean teleport) 
	{
		int nearest = getNearestCluster(false);		
		
		if (p.debug.viewer)
			p.display.message("Moving to nearest cluster... "+nearest);

		if(teleport)
		{
			teleportToCluster(nearest, true);
		}
		else
		{
			if(teleporting)	teleporting = false;
			if(p.debug.viewer)
				p.display.message("moveToNearestCluster... setting attractor and currentCluster:"+currentCluster);
			currentCluster = nearest;
			setAttractorCluster(currentCluster);
		}
	}
	
	/**
	 * moveToNearestClusterAhead()
	 * @param teleport  Whether to teleport (true) or navigate (false)
	 * Move camera to the nearest cluster
	 */
	void moveToNearestClusterAhead(boolean teleport) 
	{
		int ahead = getClusterAhead();		

		if(ahead != currentCluster)					// If a cluster ahead has been found
		{
			if (p.debug.viewer)
				p.display.message("moveToNearestClusterAhead goal:"+ahead);

			if(teleport)							
			{
				teleportToCluster(ahead, true);
			}
			else
			{
				if(teleporting)	teleporting = false;
				p.display.message("moveToNearestClusterAhead... setting currentCluster and attractor to same:"+currentCluster);
				setAttractorCluster(currentCluster);
			}
		}
		else p.display.message("moveToNearestClusterAhead... can't move to same cluster!... "+ahead);
	}

	/**
	 * Move to the next cluster numerically containing given media type
	 * @param teleport Teleport instead of moving to cluster?
	 * @param mediaType Return next cluster by media type: -1: any 0: image, 1: panorama, 2: video
	 */
	public void moveToNextCluster(boolean teleport, int mediaType) 
	{
		int next = currentCluster += 1;
		int count = 0;
		boolean found = false;
		
		if (next >= p.getCurrentField().clusters.size())
			next = 0;
		
		if(p.debug.viewer)
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
				if(p.debug.viewer) PApplet.println("Moving to next cluster:"+next+" from current cluster:"+currentCluster);
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
				if(p.debug.viewer)
					PApplet.println("Moving to next cluster with panorama:"+next+" from current cluster:"+currentCluster);
			}
			else
			{
				if(p.debug.viewer)
					p.display.message("No panoramas found...");
			}
		}
		
		if(mediaType == 2)		// Video
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
				if(p.debug.viewer)
					PApplet.println("Moving to next cluster with video:"+next+" from current cluster:"+currentCluster);
			}
			else
			{
				if(p.debug.viewer)
					p.display.message("No videos found...");
			}
		}
		
		/* Teleport or move */
		if(found)						// If a cluster was found
		{
			if(teleport)
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
	 * moveToTimeInField()
	 * @param nextFieldTime Index in timeline of cluster to move to
	 */
	void moveToTimeInField(int fieldID, int nextFieldTime, boolean teleport)
	{
		GMV_Field f = p.getField(fieldID);
		
		if(p.debug.viewer && p.debug.detailed)
			p.display.message("moveToTimeInField:"+f.timeline.get(nextFieldTime).getID()+" f.timeline.size():"+f.timeline.size());

		if(teleport)
		{
			teleportToCluster(f.timeline.get(nextFieldTime).getID(), true);
		}
		else
		{
			setAttractorCluster(f.timeline.get(nextFieldTime).getID());
		}
	}
	
	/**
	 * moveToTimeInCluster()
	 * @param nextFieldTime Time index in current cluster timeline to move to
	 */
	void moveToTimeInCluster(int clusterID, int nextClusterTime, boolean teleport)
	{
		GMV_Cluster c = p.getCluster(clusterID);
		
//		if(p.debug.viewer && p.debug.detailed)
			p.display.message("moveToTimeInCluster:"+c.timeline.get(nextClusterTime).getID()+" c.timeline.size():"+c.timeline.size());

		if(teleport)
		{
			teleportToCluster(c.timeline.get(nextClusterTime).getID(), true);
		}
		else
		{
			setAttractorCluster(c.timeline.get(nextClusterTime).getID());
		}
	}
	
	/**
	 * @param dest Destination cluster ID
	 * @param fade Fade (true) or jump (false)?
	 */
	public void teleportToCluster( int dest, boolean fade ) 
	{
		if(dest < p.getFieldClusters().size())
		{
		GMV_Cluster c = p.getCurrentField().clusters.get(dest);

		if(p.transitionsOnly)
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
			}
		}
		}
		else if(p.debug.cluster || p.debug.field || p.debug.viewer)
		{
			PApplet.println("ERROR: Can't teleport to cluster:"+dest+"... clusters.size() =="+p.getCurrentField().clusters.size());
		}
	}

	/**
	 * @param offset Field ID offset amount (0 stays in same field)
	 * Teleport to the field ID <inc> from current field
	 */
	public void teleportToField(int offset) 
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
			currentCluster = 0;

			if(p.debug.viewer)
				p.display.message("Moving to field: "+newField+" out of "+p.getFieldCount());
			if(p.debug.viewer)
				p.display.message("... at cluster: "+currentCluster+" out of "+p.getField(newField).clusters.size());

			if(p.getField(newField).clusters.size() > 0)			// Check whether field has clusters (is valid)
			{
				teleportGoal = new PVector(0,0,0);				// -- Change this!
				startTeleport(newField);
			}
			else
			{
				if(p.debug.viewer)
					p.display.message("This field has no clusters!");
			}
		}
	}

	/**
	 * jumpToNearestCluster()
	 * @param teleport  Whether to teleport (true) or navigate (false)
	 * Move camera to the nearest cluster
	 */
	void jumpToNearestCluster() 
	{
		int nearest = getNearestCluster(false);		
		
		if (p.debug.viewer)
			PApplet.println("Jumping to nearest cluster... "+nearest);
		
		currentCluster = nearest;
		setAttractorCluster(currentCluster);

//		jumpAndPointAtTarget(p.getCurrentCluster().getLocation(), )		// Need to implement initial direction
		jumpTo(p.getCurrentCluster().getLocation());
	}
	
	/**
	 * getNearestCluster()
	 * @return Index of nearest cluster to camera, excluding the current cluster
	 */
	int getNearestCluster(boolean inclCurrent) 	// Returns the cluster nearest to the current camera position, excluding the current cluster
	{
		PVector cPos = getLocation();
		float smallest = 100000.f;
		int smallestIdx = 0;

		if (p.getCurrentField().clusters.size() > 0) 
		{
			for (GMV_Cluster c : p.getActiveClusters()) 
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
			if(p.debug.cluster)
				PApplet.println("No clusters in field...");
		}

		return smallestIdx;
	}
	
	/**
	 * getNearClusters()
	 * @param amount Number of nearest clusters to return
	 * @param threshold If distance exceeds, will return less than <amount> nearest clusters
	 * @return List of indices nearest clusters to camera, excluding the current one
	 */
	IntList getNearClusters(int amount, float threshold) 	// Returns the cluster nearest to the current camera position, excluding the current cluster
	{
		PVector cPos = getLocation();
		IntList nearList = new IntList();
		FloatList distList = new FloatList();

		for (GMV_Cluster c : p.getActiveClusters()) 	// Iterate through the clusters
		{
			float dist = PVector.dist(cPos, c.getLocation());			// Distance of cluster to check

			if(nearList.size() < amount)			// Fill the list first
			{
				nearList.append(c.getID());
				distList.append(dist);
			}
			else									// Then compare new clusters to the list
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
				
				float fcDist = PVector.dist(cPos, p.getCluster(largestIdx).getLocation());		// Distance of farthest cluster on nearList
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
	 * teleportToGoal()
	 * @param newField Goal field ID; value of -1 indicates to stay in current field
	 * Fade out all visible media, move to goal, then fade in media visible at that location.
	 */
	public void startTeleport(int newField) 
	{
		p.getCurrentField().fadeOutMedia();
		teleporting = true;
		teleportStart = p.frameCount;
		teleportWaitingCount = 0;
		
		if(newField != -1)
			teleportToField = newField;
	}

	/**
	 * turnXToAngle()
	 * @param angle Angle around X axis to rotate to
	 * Rotate smoothly around X axis to specified angle
	 */
	void turnXToAngle(float angle, int turnDirection)
	{
		if(!turningX)
		{
			turnXStartFrame = p.frameCount;
			turnXStart = getXOrientation();
			turnXTarget = angle;
			
			PVector turnInfo = getTurnInfo(turnXStart, turnXTarget, turnDirection);
			if(turnDirection == 0)
				turnXDirection = turnInfo.x;
			else
				turnXDirection = turnDirection;
				
			turnXIncrement = turnIncrement;
			turnXTargetFrame = turnXStartFrame + (int)turnInfo.z;
			if(p.debug.viewer && p.debug.detailed)
				p.display.message("turnXStartFrame:"+turnXStartFrame+" turnXTargetFrame:"+turnXTargetFrame+" turnXDirection:"+turnXDirection);
			turningX = true;
		}
	}
	
	/**
	 * turnYToAngle()
	 * @param angle Angle around Y axis to rotate to
	 * Rotate smoothly around Y axis to specified angle
	 */
	void turnYToAngle(float angle, int turnDirection)
	{
		if(!turningY)
		{
			turnYStartFrame = p.frameCount;
			turnYStart = getYOrientation();
			turnYTarget = angle;
			
			PVector turnInfo = getTurnInfo(turnYStart, turnYTarget, turnDirection);
			if(turnDirection == 0)
				turnYDirection = turnInfo.x;
			else
				turnYDirection = turnDirection;
			
			turnYIncrement = turnIncrement;
			turnYTargetFrame = turnYStartFrame + (int)turnInfo.z;
			turningY = true;
		}
	}

	/**
	 * turnXByAngle()
	 * @param angle Angle around X axis to rotate by
	 * Rotate smoothly around X axis by specified angle
	 */
	void turnXByAngle(float angle)
	{
		if(!turningX)
		{
			turnXStartFrame = p.frameCount;
			turnXStart = getXOrientation();
			turnXTarget = turnXStart + angle;
			PVector turnInfo = getTurnInfo(turnXStart, turnXTarget, 0);
			turnXDirection = turnInfo.x;
			turnXIncrement = turnIncrement;
			turnXTargetFrame = turnXStartFrame + (int)turnInfo.z;
			if(p.debug.viewer && p.debug.detailed)
				p.display.message("turnXStartFrame:"+turnXStartFrame+" turnXTargetFrame:"+turnXTargetFrame+" turnXDirection:"+turnXDirection);
			turningX = true;
		}
	}
	
	/**
	 * turnYByAngle()
	 * @param angle Angle around Y axis to rotate by
	 * Rotate smoothly around Y axis by specified angle
	 */
	void turnYByAngle(float angle)
	{
		if(!turningY)
		{
			if(angle < 0.f)					// Keep within range 0 to 2π
				angle += 2*PApplet.PI;
			else if(angle > 2*PApplet.PI)
				angle -= 2*PApplet.PI;

			turnYStartFrame = p.frameCount;
			turnYStart = getYOrientation();
			turnYTarget = turnYStart + angle;
			PVector turnInfo = getTurnInfo(turnYStart, turnYTarget, 0);
			turnYDirection = turnInfo.x;
			turnYIncrement = turnIncrement;
			turnYTargetFrame = turnYStartFrame + (int)turnInfo.z;
			turningY = true;
		}
	}

	/**
	 * turnTowardsPoint()
	 * @param goalPoint Point to smoothly turn towards
	 */
	public void turnTowardsPoint( PVector goalPoint ) 
	{
		PVector cameraPosition = getLocation();
		PVector camOrientation = getOrientation();

		PVector cameraToPoint = new PVector(  cameraPosition.x-goalPoint.x, 	//  Vector from the camera to the point      
				cameraPosition.y-goalPoint.y, 
				cameraPosition.z-goalPoint.z   );
		
		camOrientation.normalize();
		cameraToPoint.normalize();

		float yaw = (float) Math.atan2(cameraToPoint.x, cameraToPoint.z);
		float adj = (float) Math.sqrt(Math.pow(cameraToPoint.x, 2) + Math.pow(cameraToPoint.z, 2)); 
		float pitch = -((float) Math.atan2(adj, cameraToPoint.y) - 0.5f * PApplet.PI);
		
		turnXToAngle(yaw, 0);		// Calculate which way to turn and start turning in X axis
		turnYToAngle(pitch, 0);		// Calculate which way to turn and start turning in Y axis
		
		turnTargetPoint = goalPoint;
	}
	
	/**
	 * getTurnInfo()
	 * @param startAngle	Starting angle
	 * @param targetAngle	Target angle
	 * @return				PVector (direction, increment, length in frames): direction -> 1: clockwise and -1: counterclockwise
	 * Calculates the direction, increment size and length of time it will take to turn from startingAngle to targetAngle
	 */
	PVector getTurnInfo(float startAngle, float targetAngle, int direction)
	{
		PVector result;
		float inc = turnIncrement;
		int len = 0;
		
		float diffRight = -1.f;		// Difference when turning right (dir = 1)
		float diffLeft = -1.f;		// Difference when turning left (dir = -1)

		if(targetAngle > startAngle)
		{
			diffRight = targetAngle - startAngle;
			diffLeft = (startAngle + 2.f*PApplet.PI) - targetAngle;
		}
		else if(targetAngle < startAngle)
		{
			diffRight = (targetAngle + 2.f*PApplet.PI) - startAngle;
			diffLeft = startAngle - targetAngle;
		}
		else if(targetAngle == startAngle)	// Full rotation
		{
			diffRight = 2.f*PApplet.PI;
			diffLeft = 2.f*PApplet.PI;
		}

		if(direction == 0)
		{
			if(diffRight <= diffLeft)
			{
				len = PApplet.round(diffRight / inc);		// Frames until target reached
				result = new PVector(-1, inc, len);
				return result;								// Return 1 for clockwise 
			}
			else
			{
				len = PApplet.round(diffLeft / inc);		// Frames until target reached
				result = new PVector(1, inc, len);
				return result;								// Return -1 for counterclockwise 
			}
		}
		else												// Full rotation
		{
			if(direction == 1)
				len = PApplet.round(diffLeft / inc);		// Frames until target reached
			else if(direction == -1)
				len = PApplet.round(diffRight / inc);		// Frames until target reached
			
			result = new PVector(direction, inc, len);
			return result;					 
		}
	}

	/**
	 * rotateX()
	 * @param dir Direction to rotate (1: clockwise, -1: counterclockwise)
	 */
	public void rotateX(int dir)
	{
		rotateXDirection = dir;
		rotatingX = true;
	}

	/**
	 * rotateY()
	 * @param dir Direction to rotate (1: clockwise, -1: counterclockwise)
	 */
	public void rotateY(int dir)
	{
		rotateYDirection = dir;
		rotatingY = true;
	}

	/**
	 * getClusterAlongVector()
	 * @param clusterList Clusters to search through
	 * @param direction Directional vector of camera movement
	 * @return Cluster in the approximate direction of given vector from camera. If none within 30 degrees, returns currentCluster
	 */
	public int getClusterAlongVector(ArrayList<GMV_Cluster> clusterList, PVector direction)
	{
		if(clusterList.size() == 0)
			clusterList = p.getActiveClusters();
		
		IntList clustersAlongVector = new IntList();
		
		for (GMV_Cluster c : clusterList) 							// Iterate through the clusters
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
				if(p.debug.viewer && p.debug.detailed)
					p.display.message("Cluster ID:"+c.getID()+" at angle "+result+" from camera..."+" NOT centered!");
			}
		}

		float smallest = 100000.f;
		int smallestIdx = 0;

		for (int i = 0; i < clustersAlongVector.size(); i++) 		// Compare distances of clusters in front
		{
			PVector cPos = getLocation();
			GMV_Cluster c = (GMV_Cluster) p.getCurrentField().clusters.get(i);
			if(p.debug.viewer && p.debug.detailed)
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
			if(p.debug.viewer && p.debug.detailed)
				p.display.message("No clusters found along vector!");
			return currentCluster;
		}
	}

	/**
	 * moveToNearestClusterWithTimes()
	 * @param minTimelinePoints Minimum points in timeline of cluster to move to
	 */
	void moveToNearestClusterWithTimes(int minTimelinePoints, boolean teleport)
	{
		int nextCluster;
		
//		IntList closest = getNearClusters(20, p.defaultFocusDistance * 4.f);											// Find 20 near clusters 	- Set number based on cluster density?
//		for(int i:closest)
//		{
//			GMV_Cluster c = p.getFieldClusters().get(i);
//			if(c.clusterTimes.size() > 1)
//			{
//				
//			}
//		}
		
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

		if(p.debug.viewer && p.debug.detailed)
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
	 * moveToNearestClusterInFuture()
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
//		if(p.debug.viewer && p.debug.detailed)
//			p.display.message("moveToNearestClusterInFuture... setting attractor:"+p.getCurrentField().timeline.get(nextTimeCluster).getID());
//		setAttractorCluster(p.getCurrentField().timeline.get(nextTimeCluster).getID());
	}

	/**
	 * moveToNextClusterAlongHistoryVector()
	 * Send the camera to nearest cluster in the field
	 */
	public void moveToNextClusterAlongHistoryVector()
	{
		IntList closest = getNearClusters(20, p.defaultFocusDistance * 4.f);		// Find 20 near clusters 	- Set number based on cluster density?
		ArrayList<GMV_Cluster> clusterList = clustersAreInList(closest, 10);		// Are the clusters within the last 10 waypoints in history?
		PVector hv = getHistoryVector();											// Get vector for direction of camera movement

		int newCluster = getClusterAlongVector(clusterList, hv);		
		setAttractorCluster(newCluster);
	}
	
	/**
	 * moveToRandomCluster()
	 * @param teleport Teleport (true) or navigate (false) to cluster?
	 * Send the camera to a random cluster in the field
	 */
	public void moveToRandomCluster(boolean teleport)
	{
		int rand = (int) p.random(p.getCurrentField().clusters.size());
		while(p.getCurrentField().clusters.get(rand).isEmpty())
		{
			rand = (int) p.random(p.getCurrentField().clusters.size());
		}

		if(teleport)
		{
			int goal = rand;
			while(p.getCurrentField().clusters.get(goal).isEmpty() || goal == currentCluster)
			{
				goal = (int) p.random(p.getCurrentField().clusters.size());
			}

			teleportToCluster(goal, true);
//			teleportGoal = ((GMV_Cluster) p.getCurrentField().clusters.get(goal)).getLocation();
//			startTeleport(-1);
		}
		else
		{
			currentCluster = rand;
			setAttractorCluster(rand);
		}
	}

	/**
	 * moveToNextLocation()
	 * During manual path navigation, move to next cluster on path; otherwise move to next closest cluster (besides current)
	 */
	public void moveToNextLocation()
	{
		moveToNextClusterAlongHistoryVector();				// Move to next attractor on a path (not retracing N steps) 

//		if(!manualPathNavigation)
//			moveToNearestCluster(false);			// Move to closest cluster besides current
	}
	
	/**
	 * setAttractorCluster()
	 * @param newCluster New attractor cluster
	 * Set a specific cluster as the current attractor
	 */
	public void setAttractorCluster(int newCluster)
	{
		stopMoving();									// -- Improve by slowing down instead and then starting
		p.getCurrentField().clearAllAttractors();

		attractorCluster = newCluster;											// Set attractorCluster
		currentCluster = newCluster;											// Set currentCluster
		movingToCluster = true;													// Move to cluster
		attractionStart = p.frameCount;
		
		p.getCurrentField().clusters.get(attractorCluster).setAttractor(true);
		
		if(p.debug.viewer)
			p.display.message("Setting new attractor:"+p.getCurrentField().clusters.get(attractorCluster).getID());

		for(GMV_Cluster c : p.getCurrentField().clusters)
			if(c.getID() != attractorCluster)
				c.setAttractor(false);
		
//		saveAttitude = getOrientation();
	}

	public void clearAttractorCluster()
	{
		attractorCluster = -1;											// Set attractorCluster
		movingToCluster = false;
		movingToAttractor = false;
	}
	
	/**
	 * stopMoving()
	 * Stop any current viewer movement
	 */
	public void stopMoving()
	{
		if(p.debug.viewer)
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
	 * getLocation()
	 * @return Current camera location
	 */
	public PVector getLocation()
	{
		if(p.transitionsOnly)
			return location;
		else
			location = new PVector(camera.position()[0], camera.position()[1], camera.position()[2]);			// Update location

		return location;
	}

	public PVector getGPSLocation()
	{
		PVector vLoc;
		if(p.transitionsOnly)
			vLoc = location;
		else
			vLoc = new PVector(camera.position()[0], camera.position()[1], camera.position()[2]);			// Update location
		
		GMV_Model m = p.getCurrentField().model;
		
		float newX = PApplet.map( vLoc.x, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, m.lowLongitude, m.highLongitude ); 			// GPS longitude decreases from left to right
		float newY = -PApplet.map( vLoc.z, -0.5f * m.fieldLength, 0.5f*m.fieldLength, m.lowLatitude, m.highLatitude ); 			// GPS latitude increases from bottom to top; negative to match P3D coordinate space

		PVector gpsLoc = new PVector(newX, newY);

		return gpsLoc;
	}
	
	/**
	 * turnTowardsSelected()
	 * Turn towards the selected image
	 */
	public void turnTowardsSelected()
	{
		if(p.getCurrentField().selectedImage != -1)
		{
			PVector selectedImageLoc;
			selectedImageLoc = p.getCurrentField().images.get(p.getCurrentField().selectedImage).getLocation();
			p.viewer.turnTowardsPoint(selectedImageLoc);
		}
	}
	
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
	
	/***
	 * updatePhysics()
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
			}

			if(PApplet.abs(walkingVelocity.mag()) == 0.f && (slowingX  || slowingY ||	slowingZ ) )
			{
				slowingX = false;
				slowingY = false;
				slowingZ = false;
			}
		}
		else if( movingToAttractor || movingToCluster || follow )								
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
//				float camAccelerationHalt = acceleration.mag() * 0.1f;
//				float camVelocityHalt = velocity.mag() * 0.1f;
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

			GMV_Cluster curAttractor = new GMV_Cluster(p.getCurrentField(), 0, 0, 0, 0);	 /* Find current attractor if one exists */
			boolean attractorFound = false;
			
			if( (movingToAttractor || follow) && attractorPoint != null )
			{
				if(p.frameCount - attractionStart > 60)					/* If not slowing and attraction force exists */
					p.display.message("Attraction taking a while... slowing:"+slowing+" halting:"+halting+" attraction.mag():"+attraction.mag()+" acceleration.mag():"+acceleration.mag());

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

				if(curAttractor.getClusterDistance() < clusterNearDistance)
				{
					if(PApplet.abs(velocity.mag()) > velocityMin)					/* Slow down at attractor center */
					{
						if(!slowing) slow();
					}
					else 
					{
//						if(curAttractor.getClusterDistance() < p.clusterCenterSize)
//						{
//							if(halting) halting = false;
//							if(slowing) slowing = false;
//							reachedAttractor = true;
//						}
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
						reachedAttractor = true;
					}
				}

				if(reachedAttractor) 
					handleReachedAttractor();
			}

			if(p.transitionsOnly)
			{
				location.add(velocity);	// Add velocity to staticLocation
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
				if(p.debug.viewer && attractionStart - p.frameCount > 20)
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
	 * handleReachedAttractor()
	 * Called once attractorPoint or attractorCluster has been reached
	 */
	private void handleReachedAttractor()
	{
		if(follow && path.size() > 0)
		{
			if(p.debug.viewer)
				p.display.message("Reached path goal #"+pathLocationIdx+", will start waiting...");
			startWaiting(pathWaitLength);
//			if(p.debug.viewer)
//				p.display.message("Reached attractor... turning to memory target:"+path.get(pathLocationIdx).target);
//			if(path.get(pathLocationIdx).target != null)
//			turnTowardsPoint(path.get(pathLocationIdx).target);				// Turn towards memory target view
		}

		if(movingToCluster)		// Stop attracting when reached attractorCluster
		{
			if(attractorCluster != -1)
			{
				attractorCluster = -1;
				p.getCurrentField().clearAllAttractors();
			}
			else
			{
				if(currentCluster == -1)
				{
					currentCluster = getNearestCluster(false);
				}
			}
			
			movingToCluster = false;
		}

		if(movingToAttractor)		// Stop attracting when reached attractorPoint
		{
			currentCluster = getNearestCluster(false);		// Set currentCluster to nearest
			if(p.debug.viewer && p.debug.detailed)
				p.display.message("Reached point of interest... setting current timeline point to cluster #"+currentCluster);

			currentFieldTimeSegment = p.getCurrentCluster().getFirstTimeSegment();
			
			//					turnTowardsPoint(attractorPoint.getLocation());
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
			lastMovementFrame = p.frameCount;
		}

		// Move Y Transition
		if (movingY && !slowingY) 
		{
			walkingAcceleration.y += walkingAccelInc * moveYDirection;
			lastMovementFrame = p.frameCount;
		}

		// Move Z Transition
		if (movingZ && !slowingZ) 		
		{
			walkingAcceleration.z += walkingAccelInc * moveZDirection;
			lastMovementFrame = p.frameCount;
		}

		if(slowingX || slowingY || slowingZ)
		{
			walkingVelocity.mult(camDecelInc);
			walkingAcceleration.mult(camDecelInc);
		}
	}
	
	/***
	 * walk()
	 * Apply walking velocity to viewer
	 */
	private void walk()
	{
		if(p.transitionsOnly)					// Add relativeVelocity to staticLocation
		{
			location.add(walkingVelocity);	
		}
		else 								// Move the camera
		{
			if(walkingVelocity.x != 0.f)
			{
				camera.truck(walkingVelocity.x);
				//				PApplet.println("walkingVelocity.x:"+walkingVelocity.x);
			}
			if(walkingVelocity.y != 0.f)
			{
				camera.boom(walkingVelocity.y);
				//				PApplet.println("walkingVelocity.y:"+walkingVelocity.y);
			}
			if(walkingVelocity.z != 0.f)
			{
				camera.dolly(walkingVelocity.z);
				//				PApplet.println("walkingVelocity.z:"+walkingVelocity.z);
			}
		}
	}
	
	private void slow()
	{
		if(p.debug.viewer)
			p.display.message("Slowing...");
		
		slowing = true;										// Slowing when close to attractor
	}

	private void halt()
	{
		if(p.debug.viewer)
			p.display.message("Halting...");
		
		slowing = false;
		halting = true;										// Slowing when close to attractor
	}

	/**
	 * getVectorToCluster()
	 * @param cluster Cluster to calculate vector from camera
	 * @return Current camera orientation as a directional unit vector
	 */
	public PVector getVectorToCluster(GMV_Cluster cluster)
	{
		PVector cameraPosition = getLocation();

		PVector cameraToCluster = new PVector(  cameraPosition.x-cluster.getLocation().x, 	//  Vector from the camera to the face.      
				cameraPosition.y-cluster.getLocation().y, 
				cameraPosition.z-cluster.getLocation().z   );
	
		return cameraToCluster;
	}
	
	/** 
	 * getClusterAhead()
	 * @return Nearest cluster ID in front of camera
	 */
	private int getClusterAhead() 					// Returns the visible cluster closest to the camera
	{
		PVector camOrientation = getOrientationVector();

		IntList nearClusters = getNearClusters(20, p.defaultFocusDistance * 4.f);	// Find 20 nearest clusters -- Change based on density?
		IntList frontClusters = new IntList();
		
		for (int i : nearClusters) 							// Iterate through the clusters
		{
			GMV_Cluster c = p.getCurrentField().clusters.get(i);
			PVector clusterVector = getVectorToCluster(c);
			PVector crossVector = new PVector();
			PVector.cross(camOrientation, clusterVector, crossVector);		// Cross vector gives angle between camera and image
			float result = crossVector.mag();
			
			if(PApplet.abs(result) < fieldOfView && c.getID() != currentCluster && !c.isEmpty())			// If cluster (center) is within field of view
			{
				if(p.debug.cluster || p.debug.viewer)
					p.display.message("Centered cluster:"+c.getID()+" == "+i+" at angle "+result+" from camera...");
				frontClusters.append(c.getID());
			}
			else
				if(p.debug.cluster || p.debug.viewer)
					p.display.message("Non-centered, current or empty cluster:"+c.getID()+" at angle "+result+" from camera..."+" NOT centered!");
		}

		float smallest = 100000.f;
		int smallestIdx = 0;

		for (int i = 0; i < frontClusters.size(); i++) 		// Compare distances of clusters in front
		{
			GMV_Cluster c = (GMV_Cluster) p.getCurrentField().clusters.get(i);
			if(p.debug.cluster || p.debug.viewer)
				p.display.message("Checking Centered Cluster... "+c.getID());
		
			float dist = PVector.dist(getLocation(), c.getLocation());
			if (dist < smallest) 
			{
				if(p.debug.cluster || p.debug.viewer)
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
	 * updateMovement()
	 * Update movement variables and perform interpolation
	 */
	private void updateMovement() 
	{		
		if (	rotatingX || rotatingY || rotatingZ || movingX || movingY || movingZ 
				|| zooming || turningX || turningY || waiting || zooming    )
		{
			/* Rotate X Transition */
			if (rotatingX) {
				camera.pan(rotateIncrement * rotateXDirection);
				lastLookFrame = p.frameCount;
			}

			/* Rotate Y Transition */
			if (rotatingY) {
				camera.tilt(rotateIncrement * rotateYDirection);
				lastLookFrame = p.frameCount;
			}

			/* Rotate Z Transition */
			if (rotatingZ) 
			{
				camera.roll(rotateIncrement * rotateZDirection);
				lastLookFrame = p.frameCount;
			}
			
			if(follow && waiting)				// If revisiting places in memory and currently waiting
			{				
				if(p.frameCount > pathWaitStartFrame + pathWaitLength )
				{
					waiting = false;
					if(p.debug.viewer)
						p.display.message("Finished waiting...");

					pathLocationIdx++;
					if(pathLocationIdx < path.size())
					{
						pathGoal = path.get(pathLocationIdx).getLocation();
						p.display.message("Incremented pathLocationIdx:"+pathLocationIdx);
						p.display.message("New pathGoal:"+pathGoal);
						
						if(pathLocationIdx >= 1)
						{
							if(pathGoal != path.get(pathLocationIdx-1).getLocation())
							{
								setAttractorPoint(pathGoal);
								p.display.message("Moving to next attraction point..."+attractorPoint.getLocation());
							}
							else
							{
								p.display.message("Same attraction point!");
//								turnTowardsPoint(memory.get(revisitPoint).target);			// Turn towards memory target view
							}
						}
					}
					else
					{
						p.display.message("Reached end of path... ");
						p.display.message(" ");
						stopFollowing();
					}
				}
			}

			/* Zoom Transition */
			if (zooming) 
			{
				if (p.frameCount < zoomStart + zoomLength) 
				{
					zoomCamera(zoomIncrement / zoomLength * zoomDirection);
				}
				else 
					zooming = false;
			}
			
			/* Turn Y Transition */
			if (turningY) {
				if (p.frameCount <= turnYTargetFrame) 
				{
					camera.tilt(turnYIncrement * turnYDirection);
					lastLookFrame = p.frameCount;
				}
				else
				{
					turningY = false;

					if(!turningX)
					{
						if(turnTargetPoint != null)
							camera.aim(turnTargetPoint.x, turnTargetPoint.y, turnTargetPoint.z);

//						if(follow)
//						{
//							p.display.message("Reached memory target Y orientation: "+getYOrientation());
//							startWaiting(memoryObserveLength);
//						}
					}
				}
			}

			/* Turn X Transition */
			if (turningX) {
				if (p.frameCount <= turnXTargetFrame) 
				{
					camera.pan(turnXIncrement * turnXDirection);
					lastLookFrame = p.frameCount;
				}
				else
				{
					p.display.message("Reached turn X goal! X Orientation: "+getXOrientation());
					turningX = false;

					if(!turningY)
					{
						if(turnTargetPoint != null)
							camera.aim(turnTargetPoint.x, turnTargetPoint.y, turnTargetPoint.z);

//						if(follow)
//						{
//							p.display.message("Reached memory target X orientation: "+getXOrientation());
//							startWaiting(memoryObserveLength);
//						}
					}
				}
			}

//			/* Turn Z Transition */
//			if (turnZTransition) 
//			{
//				cam.roll(rotateIncrement * rotateZDirection);
//				lastLookFrame = p.frameCount;
//			}
		}
		else										// If no transitions
		{
			if(autoNavigation && !looking)		// If not currently looking for images
			{
				if( !mediaAreVisible(false) )		// Check whether any images are currently visible anywhere in front of camera
				{
					p.display.message("No images visible! will look around...");
					lookForImages();			// Look for images around the camera
				}
			}
		}
	}
	
	public void zoomCamera(float zoom)
	{
		fieldOfView += zoom;
		camera.zoom(zoom);
//		PApplet.println("zoomCamera() fieldOfView:"+fieldOfView);
	}

	public void resetCamera()
	{
		initialize( getLocation().x,getLocation().y,getLocation().z );							// Initialize camera
	}
	
	/**
	 * updateTeleporting()
	 * Update teleporting interpolation values
	 */
	private void updateTeleporting()
	{
		if(p.frameCount >= teleportStart + p.getCurrentField().teleportLength)		// If the teleport has finished
		{
			if(p.debug.viewer)
				p.display.message(" Reached teleport goal...");

			if(teleportWaitingCount > p.getCurrentField().teleportLength * 2.f)
			{
				if(p.debug.viewer)
					p.display.message(" Exceeded teleport wait time. Stopping all media...");
				p.getCurrentField().stopAllMediaFading();
			}
			
			if( !p.getCurrentField().mediaAreFading() )			// Once no more images are fading
			{
				if(p.debug.viewer)
					p.display.message(" Media finished fading...");

				if(teleportToField != -1)							// If a new field has been specified 
				{
					if(p.debug.viewer)
						p.display.message(" Teleported to field "+teleportToField+"...");

					setCurrentField(teleportToField);				// Set new field
					teleportToField = -1;							// Reset target field
				}

				if(p.debug.viewer)
					p.display.message(" Teleported to x:"+teleportGoal.x+" y:"+teleportGoal.y+" z:"+teleportGoal.z);

				if(teleportGoalCluster != -1)
				{
					currentCluster = teleportGoalCluster;
					teleportGoalCluster = -1;
					if(p.debug.field)
						p.display.message(" After teleport, set current cluster to teleportGoalCluster:"+currentCluster);
				}
				
				camera.jump(teleportGoal.x, teleportGoal.y, teleportGoal.z);			// Move the camera
				teleporting = false;													// Change the system status
				
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
						if(currentCluster == -1)
						{
							currentCluster = getNearestCluster(false);
						}
					}
				}
				if(movingToAttractor)
				{
					movingToAttractor = false;
					currentCluster = getNearestCluster(false);		// Set currentCluster to nearest
//					if(p.debug.viewer)
//						p.display.message("Reached attractor... turning towards image");
					
//					if(attractorPoint != null)
//						turnTowardsPoint(attractorPoint.getLocation());
					
					p.getCurrentField().clearAllAttractors();	// Clear current attractors
					setAttractorCluster(currentCluster);	
				}
			}
			else
			{
				teleportWaitingCount++;
				if(p.debug.viewer && p.debug.detailed)
					PApplet.println("Waiting to finish teleport... "+teleportWaitingCount);
			}
		}
	}
	
	/** 
	 * stopAllTransitions()
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
//		if(sideStepping)
//			sideStepping = false;
		if(turningX)
			turningX = false;
		if(turningY) 
			turningY = false;
		if(zooming)
			zooming = false;

		p.getCurrentField().clearAllAttractors();

		if(waiting)
			waiting = false;
		if(looking)
			looking = false;
		if(teleporting)
			teleporting = false;
	}
	
	/**
	 * lookForImages()
	 * Turn camera side to side, at different elevation angles, until images in range become visible
	 */
	public void lookForImages()
	{
		stopAllTransitions();										// Stop all current transitions
		lookingStartAngle = getOrientation().x;
		lookingStartFrameCount = p.frameCount;
		lookingDirection = Math.round(p.random(1)) == 1 ? 1 : -1;		// Choose random direction to look
		lookingLength = PApplet.round(2.f*PApplet.PI / rotateIncrement);
		turnXToAngle(lookingStartAngle, lookingDirection);
		looking = true;
	}	
	
	/**
	 * updateLooking()
	 * Update turning to look for images 
	 */
	private void updateLooking()
	{
		if(looking)	// If looking for images
		{
			if( mediaAreVisible( true ) )				// Check whether any images are visible and centered
			{
				p.display.message("Finished rotating to look, found image(s) ");
				stopAllTransitions();			// Also sets lookingForImages to false
			}
			else
			{
				lastLookFrame = p.frameCount;

				if ( p.frameCount - lookingStartFrameCount > lookingLength )
				{
					lookingRotationCount++;							// Record camera rotations while looking for images
					p.display.message("Rotated to look "+lookingRotationCount+" times...");
					lookingStartFrameCount = p.frameCount;		// Restart the count
				}

				if (lookingRotationCount > 2) 
				{
					p.display.message("Couldn't see any images. Moving to next nearest cluster...");
					stopAllTransitions();							// Sets lookingForImages and all transitions to false
					moveToNearestCluster(false);
				}
			}
		}
	}
	
	void moveToNextTimeSegment(boolean field, boolean teleport)
	{
		if(field)
		{
			currentFieldTimeSegment++;
			if(currentFieldTimeSegment >= p.getCurrentField().timeline.size())
				currentFieldTimeSegment = 0;

			moveToTimeInField(p.getCurrentField().fieldID, currentFieldTimeSegment, teleport);
		}
		else
		{
			currentClusterTimeSegment++;
			if(currentClusterTimeSegment >= p.getCurrentCluster().timeline.size())
				currentClusterTimeSegment = 0;

			moveToTimeInCluster(p.getCurrentCluster().getID(), currentClusterTimeSegment, teleport);
		}
	}
	
	void moveToPreviousTimeSegment(boolean field, boolean teleport)
	{
		if(field)
		{
			currentFieldTimeSegment--;
			if(currentFieldTimeSegment < 0)
				currentFieldTimeSegment = p.getCurrentField().timeline.size()-1;

			moveToTimeInField(p.getCurrentField().fieldID, currentFieldTimeSegment, teleport);
		}
		else
		{
			currentClusterTimeSegment++;
			if(currentClusterTimeSegment >= p.getCurrentCluster().timeline.size())
				currentClusterTimeSegment = 0;

			moveToTimeInCluster(p.getCurrentCluster().getID(), currentClusterTimeSegment, teleport);
		}
	}
	
	/**
	 * followTimeline()
	 * Revisit all places stored in memory
	 */
	public void followTimeline()
	{
		if(!follow)
		{
			path = p.getCurrentField().getTimelineAsPath();								// Follow memory path 

			if(path.size() > 0)
			{
				follow = true;
				pathLocationIdx = 0;
				p.display.message("followTimeline()... Setting first path goal: "+path.get(pathLocationIdx).getLocation());
				pathGoal = path.get(pathLocationIdx).getLocation();
				setAttractorPoint(pathGoal);
			}
			else p.display.message("No timeline points!");
		}
		else
		{
			p.display.message("Already called followTimeline(): Stopping... "+path.get(pathLocationIdx).getLocation());
			pathLocationIdx = 0;
			follow = false;
		}
	}
	
	/**
	 * followMemory()
	 * Revisit all places stored in memory
	 */
	public void followMemory()
	{
		path = memory;								// Follow memory path 
		
		if(path.size() > 0)
		{
			follow = true;
			pathLocationIdx = 0;
			p.display.message("followMemory()... Setting first path goal: "+path.get(pathLocationIdx).getLocation());
			pathGoal = path.get(pathLocationIdx).getLocation();
			setAttractorPoint(pathGoal);
		}
		else p.display.message("No memory points!");
	}
	
	/**
	 * startWaiting()
	 * @param length Length of time to wait
	 * Wait for specified time until moving to next memory point
	 */
	private void startWaiting(int length)	
	{
		waiting = true;
		pathWaitStartFrame = p.frameCount;
		pathWaitLength = length;
	}
	
	/**
	 * setAttractorPoint()
	 * @param newPoint Point of interest to attract camera 
	 */
	private void setAttractorPoint(PVector newPoint)
	{
		stopMoving();									// -- Improve by slowing down instead and then starting
		p.getCurrentField().clearAllAttractors();
		movingToAttractor = true;
		attractorPoint = new GMV_Cluster(p.getCurrentField(), 0, newPoint.x, newPoint.y, newPoint.z);
		attractorPoint.setEmpty(false);
		attractorPoint.setAttractor(true);
		attractorPoint.setMass(p.mediaPointMass * 25.f);
		attractionStart = p.frameCount;
	}
	
	/**
	 * mediaAreVisible()
	 * @param front Restrict to media in front
	 * @return Whether any media are visible and in front of camera
	 */
	public boolean mediaAreVisible( boolean front )
	{
		boolean imagesVisible = false;
		ArrayList<GMV_Image> closeImages = new ArrayList<GMV_Image>();		// List of images in range
		boolean panoramasVisible = false;
		ArrayList<GMV_Panorama> closePanoramas = new ArrayList<GMV_Panorama>();		// List of images in range
		boolean videosVisible = false;
		ArrayList<GMV_Video> closeVideos = new ArrayList<GMV_Video>();		// List of images in range
		
		float result;
		
		for( GMV_Image i : p.getFieldImages() )
		{
			if(i.getViewingDistance() < farViewingDistance + i.getFocusDistance() 
					&& i.getViewingDistance() > nearClippingDistance * 2.f )		// Find images in range
			{
				if(!i.disabled)
					closeImages.add(i);							
			}
		}

		for( GMV_Image i : closeImages )
		{
			if(!i.isBackFacing() && !i.isBehindCamera())			// If image is ahead and front facing
			{
				result = PApplet.abs(i.getFacingAngle());			// Get angle at which it faces camera
				
				if(front)										// Look for centered or only visible image?
				{
					if(result < p.centeredAngle)					// Find closest to camera orientation
					{
						if(p.debug.viewer && p.debug.detailed)
							p.display.message("Image:"+i.getID()+" result:"+result+" is less than centeredAngle:"+p.centeredAngle);
						imagesVisible = true;
						break;
					}
				}
				else
				{
					if(result < p.visibleAngle)						// Find closest to camera orientation
					{
						imagesVisible = true;
						break;
					}
				}
			}
		}

		for( GMV_Panorama n : p.getFieldPanoramas() )
		{
			if(n.getViewingDistance() < farViewingDistance + p.defaultFocusDistance 
					&& n.getViewingDistance() > nearClippingDistance * 2.f )		// Find images in range
			{
				if(!n.disabled)
					closePanoramas.add(n);							
			}
		}

		for( GMV_Video v : p.getFieldVideos() )
		{
			if(v.getViewingDistance() <= farViewingDistance + v.getFocusDistance()
					&& v.getViewingDistance() > nearClippingDistance * 2.f )		// Find videos in range
			{
				if(!v.disabled)
					closeVideos.add(v);							
			}
		}

		for(GMV_Video v : closeVideos)
		{
			if(!v.isBackFacing() && !v.isBehindCamera())			// If video is ahead and front facing
			{
				result = PApplet.abs(v.getFacingAngle());			// Get angle at which it faces camera

				if(front)										// Look for centered or only visible image?
				{
					if(result < p.centeredAngle)					// Find closest to camera orientation
					{
						imagesVisible = true;
						break;
					}
				}
				else
				{
					if(result < p.visibleAngle)						// Find closest to camera orientation
					{
						videosVisible = true;
						break;
					}
				}
			}
		}
		
		return imagesVisible || panoramasVisible || videosVisible;
	}
		
	/**
	 * addPlaceToMemory()
	 * Add current camera location and orientation to memory
	 */
	public void addPlaceToMemory()
	{
		if(!teleporting && !walking && velocity.mag() == 0.f)		// Only record points when stationary
		{
			GMV_Waypoint curWaypoint = new GMV_Waypoint(path.size(), getLocation());
			curWaypoint.setTarget(getOrientation());
			curWaypoint.setID(currentCluster);				// Need to make sure camera is at current cluster!
			path.add(curWaypoint);
			if(p.debug.memory)
			{
				p.display.message("Added point to memory! "+curWaypoint.getLocation());
				p.display.message("Total points:"+path.size());
			}
		}
		else if(p.debug.memory)
			p.display.message("Couldn't add memory point... walking? "+walking+" teleporting?"+teleporting+" velocity.mag():"+velocity.mag());
	}
	
	/**
	 * clearMemory()
	 * Clear the current memory
	 */
	public void clearMemory()
	{
		follow = false;
		waiting = false;
		path = new ArrayList<GMV_Waypoint>();
	}

	/**
	 * stopFollowing()
	 * Stop navigation along points in memory
	 */
	public void stopFollowing()
	{
		follow = false;
		pathLocationIdx = 0;
	}

	/**
	 * selectNextImage()
	 * Selects next image numerically for viewing, exporting, etc.
	 */
	public void selectNextImage() 
	{
		int nextSelectedImage = p.getCurrentField().selectedImage + 1;

		if(nextSelectedImage >= p.getCurrentField().images.size())
			nextSelectedImage = 0;

		int count = 0;
		while (!p.getCurrentField().images.get(nextSelectedImage).visible && count < p.getCurrentField().images.size()) 
		{
			nextSelectedImage++;
			if(nextSelectedImage >= p.getCurrentField().images.size())
				nextSelectedImage = 0;
		}

		if(count > p.getCurrentField().images.size())
		{
			p.display.message("No visible images to select!");
		}
		else
		{
			p.getCurrentField().deselectAllMedia();
			p.getCurrentField().selectedImage = nextSelectedImage;
			p.getCurrentField().images.get( nextSelectedImage ).setSelected(true);
		}
	}

	/**
	 * selectFrontMedia()
	 * Select image or video in front of camera and within selection angle.	-- Need to include panoramas too!
	 */
	public void selectFrontMedia() 
	{
		ArrayList<GMV_Image> possible = new ArrayList<GMV_Image>();

		for(GMV_Image i : p.getCurrentField().images)
		{
			if(i.getViewingDistance() <= selectionMaxDistance)
				if(!i.disabled)
					possible.add(i);
		}

		float closestImageDist = 1000.f;
		int closestImageID = -1;

		for(GMV_Image s : possible)
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

		ArrayList<GMV_Video> possibleVideos = new ArrayList<GMV_Video>();

		for(GMV_Video v : p.getCurrentField().videos)
		{
			if(v.getViewingDistance() <= selectionMaxDistance)
				if(!v.disabled)
					possibleVideos.add(v);
		}

		float closestVideoDist = 1000.f;
		int closestVideoID = -1;

		for(GMV_Video v : possibleVideos)
		{
			if(!v.isBackFacing() && !v.isBehindCamera())					// If image is ahead and front facing
			{
				float result = PApplet.abs(v.getFacingAngle());				// Get angle at which it faces camera

				if(result < closestVideoDist)										// Find closest to camera orientation
				{
					closestVideoDist = result;
					closestVideoID = v.getID();
				}
			}
		}

		int newSelected;
		p.getCurrentField().deselectAllMedia();							// Deselect all media
	
		if(closestImageDist < closestVideoDist && closestImageDist != 1000.f)
		{
			newSelected = closestImageID;
			if(p.debug.viewer)
				p.display.message("Selected image in front: "+newSelected);
			p.getCurrentField().selectedImage = newSelected;	
			if(newSelected != -1)
				p.getCurrentField().images.get(newSelected).setSelected(true);
		}
		else if(closestVideoDist < closestImageDist && closestVideoDist != 1000.f)
		{
			newSelected = closestVideoID;
			if(p.debug.viewer)
				p.display.message("Selected video in front: "+newSelected);
			p.getCurrentField().selectedVideo = newSelected;	
			if(newSelected != -1)
			{
				PApplet.println("HERE");
				p.getCurrentField().videos.get(newSelected).setSelected(true);
			}
		}
	}

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

			for(GMV_Cluster c : p.getCurrentField().clusters)
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
		else
		{
			list = new IntList();
			list.append(currentCluster);
		}

		return list;
	}


	/**
	 * getFrontImage()
	 * @return Image closest to directly in front of the camera
	 */
	public int getFrontImage() {
		float smallest = 100000.f;
		int smallestIdx = 0;

		GMV_Field f = p.getCurrentField();

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
	 * getNearestImage()
	 * @return Image nearest to the camera in any direction
	 */
	public int getNearestImage() {
		float smallest = 100000.f;
		int smallestIdx = 0;
		GMV_Field f = p.getCurrentField();

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
	 * getFrontVideo()
	 * @return Image closest to directly in front of the camera
	 */
	public int getFrontVideo() {
		float smallest = 100000.f;
		int smallestIdx = 0;

		GMV_Field f = p.getCurrentField();

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
	 * getNearestVideo()
	 * @return Video nearest to the camera in any direction
	 */
	public int getNearestVideo() {
		float smallest = 100000.f;
		int smallestIdx = 0;
		GMV_Field f = p.getCurrentField();

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
	 * importGPSTrack()
	 * Open dialog to select GPS track file
	 */
	public void importGPSTrack()
	{
		gpsTrackSelected = false;
		p.selectInput("Select a GPS Track:", "gpsTrackSelected");
	}
	
	/**
	 * gpsTrackSelected()
	 * Called when user selects a GPS track file
	 * @param selection Selected GPS Track file
	 */
	public void gpsTrackSelected(File selection)
	{
		loadGPSTrack(selection);
	}

	/**
	 * loadGPSTrack()
	 * Load and analyze GPS track file in response to user selection
	 * @param selection Selected GPS track file
	 */
	public void loadGPSTrack(File selection) 
	{
		if (selection == null) 
		{
			PApplet.println("loadGPSTrack() window was closed or the user hit cancel.");
		} 
		else 
		{
			String input = selection.getPath();

			if(p.debug.viewer)
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
		}
	}

	/**
	 * analyzeGPSTrack()
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
				
				hour = p.utilities.utcToPacificTime(hour);						// Convert from UTC Time

				if(p.debug.viewer)
				{
					PApplet.print("latitude:"+latitude);
					PApplet.print("  longitude:"+longitude);
					PApplet.println("  elevation:"+elevation);

					PApplet.print("hour:"+hour);
					PApplet.print("  minute:"+minute);
					PApplet.print("  second:"+second);
					PApplet.print("  year:"+year);
					PApplet.print("  month:"+month);
					PApplet.println("  day:"+day);
				}

				PVector newLoc = new PVector(latitude,longitude,elevation);

				GMV_Waypoint wp = new GMV_Waypoint(count, newLoc);		// GPS track node as a Waypoint
				gpsTrack.add(wp);																									// Add Waypoint to gpsTrack
				
				count++;
			}
			PApplet.println("Added "+count+" nodes to gpsTrack...");
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public GMV_TimePoint getCurrentTime()
	{
		GMV_TimePoint curTime = new GMV_TimePoint(p, null);
		return curTime;
	}
	
	/**
	 * getHistoryVector()
	 * Get vector of direction of camera motion by comparing current and previous waypoints
	 * @return Vector of direction of camera motion
	 */
	public PVector getHistoryVector()
	{
		PVector hv = new PVector();
		
		if(history.size() > 1)
		{
			GMV_Waypoint w1 = history.get(history.size()-1);
			GMV_Waypoint w2 = history.get(history.size()-2);
			
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
	public ArrayList<GMV_Waypoint> pointsAreInList(ArrayList<GMV_Waypoint> check, int memory)
	{
		ArrayList<GMV_Waypoint> found = new ArrayList<GMV_Waypoint>();
		
		for( GMV_Waypoint p : check )
		{
//			PApplet.println("pointsAreInList()... memory:"+memory);

//			for( GMV_Waypoint w : history )
			for(int i = history.size()-1; i >= history.size()-memory; i--)		// Iterate through history from last element to 
			{
				PApplet.println("i:"+i);
				GMV_Waypoint w = history.get(i);
				
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
	public ArrayList<GMV_Cluster> clustersAreInList(IntList check, int memory)
	{
		ArrayList<GMV_Cluster> found = new ArrayList<GMV_Cluster>();
		
		for( int cPoint : check )
		{
			PApplet.println("clustersInList()... memory:"+memory);

			for(int i = history.size()-1; i >= history.size()-memory; i--)		// Iterate through history from last element to 
			{
				PApplet.println("i:"+i);
				GMV_Waypoint w = history.get(i);
				
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

	public boolean isMovingToAttractor()
	{
		return movingToAttractor;
	}

	public boolean isMovingToCluster()
	{
		return movingToCluster;
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

	public int getCurrentCluster()
	{
		return currentCluster;
	}
	
	public void setCurrentCluster(int newCluster)
	{
		currentCluster = newCluster;
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
		zoomStart = p.frameCount;
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
	
	/**
	 * getClusterWeight()
	 * @param idx Cluster index
	 * Determine weight of given cluster based on distance to camera viewpoint (Static Mode only) 
	 *  */
	float getClusterWeight(int idx)					
	{
		float val = -1.f;

		if(currentCluster == idx)						// curCluster has a weight of 1.0
		{
			return 1.f;
		}
		else
		{
			for(int i : clustersVisible)				// Search list for index
			{
				if(i == idx)						// If cluster ID matches
				{
					float dist = PVector.dist(p.getCurrentField().clusters.get(i).getLocation(), location);
					val = 1.f - PApplet.constrain(PApplet.map(dist, 0.f, 500.f, 0.f, 1.f), 0.f, 1.f);
				}
			}
		}

		if(val == -1.f)
		{
			return 0.f;
		}
		else
			return val;
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
