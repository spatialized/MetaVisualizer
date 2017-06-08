package multimediaLocator;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import damkjer.ocd.Camera;
import processing.core.PApplet;
import processing.core.PVector;
import processing.data.FloatList;
import processing.data.IntList;

/*********************************
 * The virtual viewer, with methods for navigating and interacting with 3D multimedia-based environments
 * @author davidgordon
 */
public class WMV_Viewer 
{
	/* Camera */
	private WMV_Camera camera;								// Camera object
	private WMV_Camera hudCamera;								// Camera object
	private WMV_WorldSettings worldSettings;				// Viewer settings
	private WMV_WorldState worldState;						// Viewer settings
	private WMV_ViewerSettings settings;					// Viewer settings
	private WMV_ViewerState state;							// Viewer settings
	private ML_DebugSettings debugSettings;					// Viewer settings

	/* Memory */
	public ArrayList<WMV_Waypoint> memory;				// Path for camera to take
	public ArrayList<WMV_Waypoint> path; 				// Record of camera path

	/* Time */
	public ArrayList<WMV_TimeSegment> nearbyClusterTimeline;	// Combined timeline of nearby (visible) clusters

	/* Navigation */
	public WMV_Cluster attractorPoint;							// For navigation to points outside cluster list

	/* GPS Tracks */
	private ArrayList<WMV_Waypoint> history;			// Stores a GPS track in virtual coordinates
	private ArrayList<WMV_Waypoint> gpsTrack;			// Stores a GPS track in virtual coordinates
	
	WMV_Field currentField;
	WMV_World p;
	
	/**
	 * Constructor for viewer class
	 * @param parent Parent world
	 * @param newWorldSettings Current world settings
	 * @param newWorldState Current world state
	 * @param newDebugSettings Debug settings
	 */
	public WMV_Viewer(WMV_World parent, WMV_WorldSettings newWorldSettings, WMV_WorldState newWorldState, ML_DebugSettings newDebugSettings)
	{
		p = parent;
		
		worldSettings = newWorldSettings;
		worldState = newWorldState;
		debugSettings = newDebugSettings;
		
		settings = new WMV_ViewerSettings();
		state = new WMV_ViewerState();
		state.clusterNearDistance = worldSettings.clusterCenterSize * state.clusterNearDistanceFactor;

		history = new ArrayList<WMV_Waypoint>();
		gpsTrack = new ArrayList<WMV_Waypoint>();

		memory = new ArrayList<WMV_Waypoint>();
		path = new ArrayList<WMV_Waypoint>();

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
		camera = new WMV_Camera( p.ml, x, y, z, 0.f, 0.f, 0.f, 0.f, 1.f, 0.f, settings.fieldOfView, settings.nearClippingDistance, 10000.f);
		hudCamera = new WMV_Camera( p.ml, 0.f, 0.f, 500.f, 0.f, 0.f, 0.f, 0.f, 1.f, 0.f, (float)Math.PI / 3.f, settings.nearClippingDistance, 10000.f);

		state.location = new PVector(x, y, z);
		state.teleportGoal = new PVector(x, y, z);
		settings.initialize();
		state.clustersVisibleInOrientationMode = new ArrayList<Integer>();
	}

	/**
	 * Start the viewer
	 */
	public void start()
	{
		state.firstRunningFrame = true;
	}

	/**
	 * Set World View camera view angle
	 */
	public void show()
	{
		camera.show();						
	}

	/**
	 * Set Media View camera view angle
	 */
	public void showHUD()
	{
		hudCamera.show();						
	}

	/**
	 * Set viewer state
	 * @param newState New viewer state
	 */
	public void setState(WMV_ViewerState newState)
	{
		state = newState;
		setLocation(state.location);				// Update the camera location
		setTarget(state.target);					// Update the camera target
	}
	
	/**
	 * Set viewer settings
	 * @param newSettings New viewer settings
	 */
	public void setSettings(WMV_ViewerSettings newSettings)
	{
		settings = newSettings;
	}
	
	/**
	 * Enter the given field
	 * @param fieldID Field to enter
	 */
	public void enterField(int fieldID)
	{
		if(debugSettings.viewer) System.out.println("enterField()... location before:"+getLocation());
		if(p.getField(fieldID).hasBeenVisited())
			setCurrentField(fieldID, true);				// Set new field and simulation state
		else
			setCurrentField(fieldID, false);				// Set new field without setting simulation state

		if(currentField == null)
		{
			System.out.println("Didn't enter field... currentField == null!");
		}
		else
		{
			if(p.ml.display.displayView == 1)
				p.ml.display.map2D.reset(p);
			if(debugSettings.viewer) System.out.println("Entered field... "+currentField.getID()+"... location after:"+getLocation());
		}
	}
	
	void updateState(WMV_WorldSettings newWorldSettings, WMV_WorldState newWorldState)
	{
		worldSettings = newWorldSettings;
		worldState = newWorldState;
		setOrientation();
		
		if(attractorPoint != null) 
			attractorPoint.update(worldSettings, worldState, settings, state);
	}
	
	/*** 
	 * Update viewer navigation each frame
	 */
	void updateNavigation()
	{
		if(state.firstRunningFrame)
		{
			camera.pan(0.0001f);
			state.firstRunningFrame = false;
		}
		
		if(!settings.orientationMode)
			state.location = new PVector(camera.getPosition()[0], camera.getPosition()[1], camera.getPosition()[2]);		/* Update location */
		
		updateWalking();							/* Update walking */
		updatePhysics();							/* Update physics */
		
		if(isTeleporting()) updateTeleporting();		/* Update teleporting */
		updateMovement();							/* Update navigation */
		if(state.turningX || state.turningY) updateTurning();	/* Update turning */

		if(currentField != null)
			currentField.getAttractingClusters().size();
		else
			System.out.println("viewer.currentField == null!");
		
		if(worldState.getTimeMode() == 2 && ( isMoving() || isFollowing() || isWalking() ))
		{
			if(worldState.frameCount % 5 == 0.f)
				p.createTimeCycle();
		}
		else if(worldSettings.timeCycleLength == -1 && worldState.frameCount % 5 == 0.f)	// Flag viewer to keep calling method until clusters are visible
		{
			p.createTimeCycle();
		}
		
		if(settings.lockToCluster && !state.walking)								// Update locking to nearest cluster 
		{
			if(currentField.getAttractingClusters().size() > 0)		// If being attracted to a point
			{
				if(state.clusterLockIdleFrames > 0) state.clusterLockIdleFrames = 0;											
			}
			else															// If idle
			{
				state.clusterLockIdleFrames++;									// Count frames with no attracting clusters
				if(state.clusterLockIdleFrames > settings.lockToClusterWaitLength)			// If after wait length, lock to nearest cluster
				{
					int nearest = getNearestCluster(true);					// Get nearest cluster location, including current cluster
					WMV_Cluster c = currentField.getCluster(nearest);
					if(c.getClusterDistance() > worldSettings.clusterCenterSize * 2.f)	// If the nearest cluster is farther than threshold distance
						moveToCluster(c.getID(), false);					// Move to nearest cluster
				}
			}
		}

		if(settings.mouseNavigation)
		{
			if(state.lastMovementFrame-worldState.frameCount > 60)			// Start following memory path if idle for a few seconds
			{
				if(!isFollowing() && memory.size() > 0)
				{
					followMemory();
				}
			}
		}
		
		if(settings.orientationMode) updateOrientationMode();
	}
	
	/**
	 * Update Orientation Mode parameters
	 */
	private void updateOrientationMode()
	{
		state.clustersVisibleInOrientationMode = new ArrayList<Integer>();

		for(WMV_Cluster c : currentField.getClusters())
		{
			if(settings.orientationModeForceVisible)
			{
				if(!c.isEmpty())
					state.clustersVisibleInOrientationMode.add(c.getID());
			}
			else
			{
				if(!c.isEmpty())
					if(c.getLocation().dist(state.location) < settings.orientationModeClusterViewingDistance)
						state.clustersVisibleInOrientationMode.add(c.getID());
			}
		}

		if(state.clustersVisibleInOrientationMode.size() > settings.orientationModeMaxVisibleClusters)		// Show only closest clusters if over maxVisibleClusters
		{
			List<Integer> allClusters = state.clustersVisibleInOrientationMode;
			state.clustersVisibleInOrientationMode = new ArrayList<Integer>();

			for(int i=0; i<allClusters.size(); i++)
			{
				if(state.clustersVisibleInOrientationMode.size() < (settings.orientationModeForceVisible ? settings.orientationModeMinVisibleClusters : settings.orientationModeMaxVisibleClusters))
				{
					state.clustersVisibleInOrientationMode.add(i);
				}
				else
				{
					WMV_Cluster c = currentField.getCluster(i);
					float cDist = c.getLocation().dist(state.location);
					float largest = -10000;
					int largestIdx = -1;
					int count = 0;

					for(int n : state.clustersVisibleInOrientationMode)		// Find farthest
					{
						WMV_Cluster v = currentField.getCluster(n);
						float vDist = v.getLocation().dist(state.location);
						if(vDist > largest)
						{
							largest = vDist;
							largestIdx = count;

							count++;
						}
					}

					if(cDist < largest)					// Remove farthest and add new index
					{
						state.clustersVisibleInOrientationMode.remove(largestIdx);
						state.clustersVisibleInOrientationMode.add(i);
					}
				}
			}
		}
	}

	/**
	 * Move viewer forward
	 */
	public void walkForward()
	{
		startMoveZTransition(-1);

//		state.moveZDirection = -1;
//		state.movingZ = true;
//		state.slowingZ = false;
	}

	/**
	 * Move viewer backward
	 */
	public void walkBackward()
	{
		startMoveZTransition(1);

//		state.moveZDirection = -1;
//		state.movingZ = true;
//		state.slowingZ = false;
	}

	/**
	 * Slow viewer movement along active walking axes
	 */
	public void walkSlower()
	{
		if(state.movingX)
		{
			state.movingX = false;
			state.slowingX = true;
		}
		if(state.movingY)
		{
			state.movingY = false;
			state.slowingY = true;
		}
		if(state.movingZ)
		{
			state.movingZ = false;
			state.slowingZ = true;
		}
	}

	/**
	 * Teleport to given point in 3D virtual space
	 * @param dest	Destination point in world coordinates
	 * @param fade  Use fade transition (true) or jump (false)
	 */
	public void teleportToPoint( PVector dest, boolean fade ) 
	{
		if(settings.orientationMode)
		{
			state.teleportGoal = dest;
			state.location = dest;
		}
		else
		{
			if(fade)
			{
				state.teleportGoal = dest;
				startTeleport(-1);
			}
			else
			{
				camera.teleport(dest.x, dest.y, dest.z);
			}
		}
//		System.out.println("teleportToPoint() getLocation() after:"+getLocation());
	}	

	/**
	 * Set specified field as current field
	 * @param newField  Field to set as current
	 */
	public void setCurrentField(int newField, boolean setSimulationState)		
	{
		if(debugSettings.viewer && debugSettings.detailed)		
			System.out.println("viewer.setCurrentField().. newField:"+newField+" setSimulationState? "+setSimulationState);

		if(newField < p.getFieldCount())
		{
			setCurrentFieldID( newField );
			currentField = p.getField(newField);

			if(debugSettings.viewer && debugSettings.detailed)		
				System.out.println("viewer.setCurrentField().. after set field ID... new state.field:"+state.field+" currentField ID:"+currentField.getID()+" currentCluster:"+state.currentCluster);

			if(setSimulationState)											// Set simulation state from saved
			{
				p.setSimulationStateFromField(p.getField(newField), true);

				if(debugSettings.viewer && debugSettings.detailed)		
					System.out.println("  viewer.setCurrentField().. after setSimulationStateFromField...  state.field:"+state.field+" currentField ID:"+currentField.getID()+" currentCluster:"+state.currentCluster+" location:"+getLocation());
			}
			else
				p.getCurrentField().updateAllMediaWorldStates();

			if(!p.getField(state.field).hasBeenVisited()) 
				p.getField(state.field).setVisited(true);
		}
	}

	/**
	 * Set current field ID
	 * @param newFieldID New current field ID
	 */
	public void setCurrentFieldID(int newFieldID)
	{
		state.field = newFieldID;
	}
	
	/**
	 * Go to the given image capture location
	 * @param teleport  Whether to teleport (true) or navigate (false)
	 */
	void moveToImageCaptureLocation(int imageID, boolean teleport) 
	{
		if (debugSettings.viewer)
			System.out.println("Moving to capture location... "+imageID);

		PVector newLocation = p.getCurrentFieldImages().get(imageID).getCaptureLocation();
		
		if(teleport)
		{
			teleportToPoint(newLocation, true);
		}
		else
		{
			if(debugSettings.viewer && debugSettings.detailed)
				System.out.println("moveToCaptureLocation... setting attractor point:"+newLocation);
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
			state.teleportGoalCluster = newCluster;
			PVector newLocation = ((WMV_Cluster) currentField.getCluster(newCluster)).getLocation();
			teleportToPoint(newLocation, true);
		}
		else
		{
			if(debugSettings.viewer) System.out.println("Moving to cluster... setting attractor:"+newCluster);
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
		
		if (debugSettings.viewer)
			System.out.println("Moving to nearest cluster... "+nearest+" from current:"+state.currentCluster);

		if(settings.teleportToFarClusters && !teleport)
		{
			if( PVector.dist(currentField.getCluster(nearest).getLocation(), getLocation()) > settings.farClusterTeleportDistance )
				teleportToCluster(nearest, true, -1);
			else
				setAttractorCluster( nearest );
		}
		else
		{
			if(teleport)
				teleportToCluster(nearest, true, -1);
			else
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

		if(ahead != state.currentCluster)					// If a cluster ahead has been found
		{
			if (debugSettings.viewer)
				System.out.println("moveToNearestClusterAhead goal:"+ahead);

			if(settings.teleportToFarClusters && !teleport)
			{
				if( PVector.dist(currentField.getCluster(ahead).getLocation(), getLocation()) > settings.farClusterTeleportDistance )
					teleportToCluster(ahead, true, -1);
				else
					setAttractorCluster( ahead );
			}
			else
			{
				if(teleport)							
					teleportToCluster(ahead, true, -1);
				else
					setAttractorCluster(ahead);
			}
		}
		else
		{
			if(debugSettings.viewer)
				System.out.println("moveToNearestClusterAhead... can't move to same cluster!... "+ahead);
		}
	}
	

	/**
	 * Move camera to the last cluster
	 * @param teleport  Whether to teleport (true) or move (false)
	 */
	void moveToLastCluster(boolean teleport) 
	{
		if (debugSettings.viewer)
			System.out.println("Moving to last cluster... "+state.lastCluster);
		if(state.lastCluster > 0)
		{
			if(settings.teleportToFarClusters && !teleport)
			{
				state.teleportGoalCluster = state.lastCluster;
				PVector newLocation = ((WMV_Cluster) currentField.getCluster(state.lastCluster)).getLocation();
				if( PVector.dist(newLocation, getLocation()) > settings.farClusterTeleportDistance )
				{
					teleportToPoint(newLocation, true);
				}
				else
				{
					if(debugSettings.viewer) System.out.println("moveToLastCluster... setting attractor and currentCluster:"+state.currentCluster);
					setAttractorCluster( state.lastCluster );
				}
			}
			else
			{
				if(teleport)
				{
					state.teleportGoalCluster = state.lastCluster;
					PVector newLocation = ((WMV_Cluster) currentField.getCluster(state.lastCluster)).getLocation();
					teleportToPoint(newLocation, true);
				}
				else
				{
					if(debugSettings.viewer) System.out.println("moveToLastCluster... setting attractor and currentCluster:"+state.currentCluster);
					setAttractorCluster( state.lastCluster );
				}
			}
		}
	}

	/**
	 * Go to the next cluster numerically containing specified media type
	 * @param teleport Whether to teleport or move
	 * @param mediaType Media type without which clusters are skipped...  -1: any 0: image, 1: panorama, 2: video
	 */
	public void moveToNextCluster(boolean teleport, int mediaType) 
	{
		int next = state.currentCluster;
		int iterationCount = 0;
		boolean found = false;
		int result = -1;
		
		if(debugSettings.viewer) System.out.println("moveToNextCluster()... mediaType "+mediaType);

		/* Find goal cluster */
		if(mediaType == -1)		// Any media type
		{
			if (next >= currentField.getClusters().size())
				next = 0;

			while( currentField.getCluster(next).isEmpty() || next == state.currentCluster )		// Increment nextCluster until different non-empty cluster found
			{
				next++;
				if (next >= currentField.getClusters().size())
				{
					next = 0;
					iterationCount++;

					if(iterationCount > 3) break;
				}

				if(currentField.getCluster(next).getState().mediaCount != 0)
					System.out.println("Error: Cluster marked empty but mediaPoints != 0!  clusterID:"+next);
			}

			if(iterationCount <= 3)				// If a cluster was found in 2 iterations
			{
				found = true;
				if(debugSettings.viewer) System.out.println("Moving to next cluster:"+next+" from current cluster:"+state.currentCluster);
			}
		}
		else 
		{
			result = getNextClusterWithMediaType(currentField, mediaType, state.currentCluster);
			if(result >= 0)
			{
				found = true;
				next = result;
			}
		}
		
		if(found)				// If a cluster with specified media type was found
		{
			if(teleport)		/* Teleport or move */
			{
				teleportToCluster(next, true, -1);
			}
			else
			{
				if(isTeleporting()) state.teleporting = false;
				setAttractorCluster(next);
//				setAttractorCluster(state.currentCluster);
			}
		}

//		if(mediaType == 0)		// Image
//		{
//			boolean foundImage = fieldHasMediaType(currentField, mediaType);
//			boolean end = false;
//			while(  !currentField.getCluster(next).hasImage() ) 		// Increment <next> until different non-empty image cluster found
//			{
//				next++;
//				while(	currentField.getCluster(next).isEmpty() || next == state.currentCluster )
//				{
//					next++;
//
//					if(next >= currentField.getClusters().size())
//					{
//						next = 0;
//						iterationCount++;
//
//						if(iterationCount > 3)
//						{
//							if(debugSettings.viewer)
//								System.out.println("No images found...");
//							end = true;
//							break;
//						}
//					}
//				}
//				
//				if(end) break;
//			}
//			
//			if(iterationCount <= 3)				// If a cluster was found in 2 iterations
//			{
//				found = true;
//				if(debugSettings.viewer) System.out.println("Moving to next cluster with image:"+next+" from current cluster:"+state.currentCluster);
//			}
//			else if(debugSettings.viewer) System.out.println("No images found...");
//		}
//		
//		if(mediaType == 1)		// Panorama
//		{
//			while(  !currentField.getCluster(next).hasPanorama() || 		// Increment nextCluster until different non-empty panorama cluster found
//					currentField.getCluster(next).isEmpty() || 
//					next == state.currentCluster )
//			{
//				next++;
//
//				if(next >= currentField.getClusters().size())
//				{
//					next = 0;
//					iterationCount++;
//
//					if(iterationCount > 3)
//					{
//						if(debugSettings.viewer)
//							System.out.println("No panoramas found...");
//						break;
//					}
//				}
//			}
//			
//			if(iterationCount <= 3)				// If a cluster was found in 2 iterations
//			{
//				found = true;
//				if(debugSettings.viewer)
//					System.out.println("Moving to next cluster with panorama:"+next+" from current cluster:"+state.currentCluster);
//			}
//			else
//			{
//				if(debugSettings.viewer)
//					System.out.println("No panoramas found...");
//			}
//		}
//		
//		if(mediaType == 2)				// Video
//		{
//			while(  !currentField.getCluster(next).hasVideo() || 		// Increment nextCluster until different non-empty video cluster found
//					currentField.getCluster(next).isEmpty() || 
//					next == state.currentCluster )
//			{
//				next++;
//
//				if(next >= currentField.getClusters().size())
//				{
//					next = 0;
//					iterationCount++;
//
//					if(iterationCount > 3)
//					{
//						if(debugSettings.viewer)
//							System.out.println("No videos found...");
//						break;
//					}
//				}
//			}
//			
//			if(iterationCount <= 3)				// If a cluster was found in 2 iterations
//			{
//				found = true;
//				if(debugSettings.viewer)
//					System.out.println("Moving to next cluster with video:"+next+" from current cluster:"+state.currentCluster);
//			}
//			else
//			{
//				if(debugSettings.viewer)
//					System.out.println("No videos found...");
//			}
//		}
//
//		if(mediaType == 3)				// Sound
//		{
//			while(  !currentField.getCluster(next).hasSound() || 		// Increment nextCluster until different non-empty video cluster found
//					currentField.getCluster(next).isEmpty() || 
//					next == state.currentCluster )
//			{
//				next++;
//
//				if(next >= currentField.getClusters().size())
//				{
//					next = 0;
//					iterationCount++;
//
//					if(iterationCount > 3)
//					{
//						if(debugSettings.viewer)
//							System.out.println("No sounds found...");
//						break;
//					}
//				}
//			}
//			
//			if(iterationCount <= 3)				// If a cluster was found in 2 iterations
//			{
//				found = true;
//				if(debugSettings.viewer)
//					System.out.println("Moving to next cluster with sound:"+next+" from current cluster:"+state.currentCluster);
//			}
//			else
//			{
//				if(debugSettings.viewer)
//					System.out.println("No sounds found...");
//			}
//		}
	}

	/**
	 * Find whether field contains at least one object of given media type
	 * @param currentField Field to search
	 * @param mediaType Media type to look for
	 * @param startClusterID Starting cluster ID to search from
	 * @return Whether field contains given media type
	 */
	public int getNextClusterWithMediaType(WMV_Field currentField, int mediaType, int startClusterID)
	{
		boolean end = false;			// End search while loop
		int next = startClusterID + 1;					// Next cluster to check
		int iterationCount = 0;		
		boolean result = false;
		
		while( !result ) 		/* Search clusters until different cluster found with specified media type */
		{
			if(next >= currentField.getClusters().size() && !end)
			{
				next = 0;
				iterationCount++;
				
				if(iterationCount > 3)
				{
					if(debugSettings.viewer)
						System.out.println("1  No media found...");
					end = true;
					break;
				}
			}
			
			if(end) break;
			
			switch(mediaType)
			{
				case 0:
					result = currentField.getCluster(next).hasImage();
					break;
				case 1:
					result = currentField.getCluster(next).hasPanorama();
					break;
				case 2:
					result = currentField.getCluster(next).hasVideo();
					break;
				case 3:
					result = currentField.getCluster(next).hasSound();
					break;
			}
			
			if(result)	/* If cluster with specified media type has been found */
			{
				end = true;
			}
			else	/* If not found */
			{
				next++;

				if(currentField.getClusters().size() > next)
				{
					while(	currentField.getCluster(next).isEmpty() || next == state.currentCluster )
					{
						next++;
						if(next >= currentField.getClusters().size())
						{
							next = 0;
							iterationCount++;

							if(iterationCount > 3)
							{
								if(debugSettings.viewer)
									System.out.println("2 No media found...");
								end = true;
								break;
							}
						}
					}
				}
			}
		}

		if(iterationCount <= 3)				// If a cluster was found in 2 iterations
		{
			if(debugSettings.viewer) System.out.println("Moving to next cluster with media type:"+mediaType+" cluster found:"+next+"... moving from current cluster:"+state.currentCluster);
			return next;
		}
		else
		{
			if(debugSettings.viewer) System.out.println("No media of type "+mediaType+" found...");
			return -1;
		}
	}
	
	/**
	 * Go to cluster corresponding to given time segment in field
	 * @param fieldID Field to move to
	 * @param fieldTimeSegment Index of time segment in field timeline to move to
	 * @param teleport Whether to teleport or move
	 */
	void moveToTimeSegmentInField(int fieldID, int fieldTimeSegment, boolean teleport, boolean fade)
	{
		WMV_Field f = p.getField(fieldID);

		if(f.getTimeline().timeline.size()>0)
		{
			if(debugSettings.viewer)
				System.out.println("----> moveToTimeSegmentInField... fieldID:"+fieldID+" fieldTimeSegment:"+fieldTimeSegment+" fieldTimelineID:"+f.getTimeline().timeline.get(fieldTimeSegment).getFieldTimelineID()+" f.getTimeline().size():"+f.getTimeline().timeline.size());
			int clusterID = f.getTimeline().timeline.get(fieldTimeSegment).getClusterID();
			if(clusterID > 0)
			{
				if(clusterID == state.currentCluster && p.getCurrentField().getCluster(clusterID).getClusterDistance() < worldSettings.clusterCenterSize)	// Moving to different time in same cluster
				{
					setCurrentFieldTimeSegment(fieldTimeSegment, true);
					if(debugSettings.viewer)
						System.out.println("Advanced to time segment "+fieldTimeSegment+" in same cluster... ");
				}
				else
				{
					state.movingToTimeSegment = true;								// Set time segment target
					state.timeSegmentTarget = fieldTimeSegment;

					if(settings.teleportToFarClusters && !teleport)
					{
						if(debugSettings.viewer)
							System.out.println("  1 clusterID:"+clusterID+" p.getCurrentField().getCluster(clusterID).getLocation():"+p.getCurrentField().getCluster(clusterID).getLocation());
						if(clusterID < p.getCurrentField().getClusters().size())
						{
							if( PVector.dist(p.getCurrentField().getCluster(clusterID).getLocation(), getLocation()) > settings.farClusterTeleportDistance )
								teleportToCluster(clusterID, fade, fieldTimeSegment);
							else
								setAttractorCluster(clusterID);
						} 
						else 
							System.out.println("moveToTimeSegmentInField()... Error! clusterID >= p.getCurrentField().getClusters().size()! clusterID:"+clusterID+" p.getCurrentField() cluster count:"+p.getCurrentField().getClusters().size());
					}
					else
					{
						if(debugSettings.viewer)
							System.out.println("  2 p.getCurrentField() id:"+clusterID+" p.getCurrentField().getCluster(clusterID).getLocation():"+p.getCurrentField().getCluster(clusterID).getLocation());
						if(teleport)
							teleportToCluster(clusterID, fade, fieldTimeSegment);
						else
							setAttractorCluster(clusterID);
					}
				}
			}
			else
			{
				System.out.println("fieldTimeSegment in field #"+f.getID()+" cluster is -1!! Will move to cluster 0...");
				teleportToCluster(0, fade, 0);
			}
		}
		else
		{
			System.out.println("moveToTimeSegmentInField... timeline is empty!");
		}
	}
	
	/**
	 * Move to cluster selected on map
	 * @param clusterID Cluster ID
	 * @param switchTo3DView Whether to switch from Map to World View
	 */
	public void moveToClusterOnMap( int clusterID, boolean switchTo3DView )
	{
		System.out.println("Moving to cluster on map:"+clusterID);

		if(switchTo3DView)
		{
			teleportToCluster(clusterID, false, -1);
		}
		else
		{
			teleportToCluster(clusterID, true, -1);
			p.ml.display.displayView = 0;
		}
		
		if(p.ml.display.map2D.getSelectedClusterID() != clusterID) 
			p.ml.display.map2D.setSelectedCluster(clusterID);
	}

	/**
	 * Teleport viewer to the given cluster ID
	 * @param dest Destination cluster ID
	 * @param fade Whether to fade smoothly (true) or jump (false)
	 * @param fieldTimeSegment Goal time segment in destination cluster
	 */
	public void teleportToCluster( int dest, boolean fade, int fieldTimeSegment ) 
	{
		if(!isTeleporting() && !isMoving())
		{
			if(dest >= 0 && dest < p.getCurrentField().getClusters().size())
			{
				WMV_Cluster c = p.getCurrentField().getCluster(dest);

				if(fade)
				{
					state.teleportGoalCluster = dest;
					state.teleportGoal = c.getLocation();
					startTeleport(-1);
				}
				else
				{
					setLocation( c.getLocation() );
					setCurrentCluster(dest, fieldTimeSegment);
					if(p.state.waitingToFadeInTerrainAlpha) 
						p.fadeInTerrain();
				}
			}
			else 
				System.out.println("ERROR: Can't teleport to cluster:"+dest+"... clusters.size() =="+p.getCurrentField().getClusters().size());
		}
	}

	/**
	 * Teleport to field by offset from current
	 * @param offset Field index offset
	 * @param moveToFirstTimeSegment Whether to move to first time segment in field
	 * @param fade Whether to fade smoothly or jump
	 */
	public void teleportToFieldOffset(int offset, boolean moveToFirstTimeSegment, boolean fade) 
	{
		teleportToField(state.field + offset, moveToFirstTimeSegment, fade);
	}
	
	/**
	 * Teleport to given field ID
	 * @param newField Field ID
	 * @param moveToFirstTimeSegment Whether to move to first time segment in field
	 * @param fade Whether to fade smoothly or jump
	 */
	public void teleportToField(int newField, boolean moveToFirstTimeSegment, boolean fade) 
	{
		if(newField >= 0)
		{
			p.stopAllVideos();									/* Stop currently playing videos */
			p.stopAllSounds();									/* Stop currently playing sounds */
			
			if(newField >= p.getFieldCount()) newField = 0;
			
			if(debugSettings.viewer)
				System.out.println("teleportToField()... newField: "+newField+" out of "+p.getFieldCount());

			if(p.getField(newField).getClusters().size() > 0)
			{
				if(moveToFirstTimeSegment)
				{
					WMV_TimeSegment goalSegment = p.getField(newField).getTimeline().getLower();
					if(goalSegment != null)
						state.teleportGoalCluster = goalSegment.getClusterID();
					else
						System.out.println("teleportToField()... p.getField("+newField+").getTimeline().getLower() returns null!!");
				}
				else
				{
					state.teleportGoalCluster = -1;
					if(debugSettings.viewer)
						System.out.println("teleportToField()... Not moving to first time segment: will setCurrentCluster to "+state.teleportGoalCluster);
				}

				if(fade)
				{
					if(state.teleportGoalCluster >= 0 && state.teleportGoalCluster < p.getField(newField).getClusters().size())
						state.teleportGoal = p.getField(newField).getCluster(state.teleportGoalCluster).getLocation();	 // Set goal cluster 
					else
						if(debugSettings.viewer) System.out.println("Invalid goal cluster! "+state.teleportGoalCluster+" field clusters.size():"+p.getField(newField).getClusters().size());
					
					if(debugSettings.viewer) System.out.println("  teleportToField()...  Teleported to field "+state.teleportToField+" moveToFirstTimeSegment?"+moveToFirstTimeSegment+" state.teleportGoal:"+state.teleportGoal);
					startTeleport(newField);
				}
				else
				{
					enterField(newField); 				/* Enter new field */
					System.out.println("  teleportToField()...  Entered field "+newField+"... moveToFirstTimeSegment? "+moveToFirstTimeSegment);

					if(moveToFirstTimeSegment) 
					{
						WMV_TimeSegment goalSegment = p.getField(newField).getTimeline().getLower();
						if(goalSegment != null)
						{
							state.teleportGoalCluster = goalSegment.getClusterID();
							if(state.teleportGoalCluster >= 0 && state.teleportGoalCluster < p.getField(newField).getClusters().size())
								state.teleportGoal = p.getField(newField).getCluster(state.teleportGoalCluster).getLocation();
							else
								if(debugSettings.viewer) System.out.println("Invalid goal cluster! "+state.teleportGoalCluster+" field clusters.size():"+p.getField(newField).getClusters().size());
						}
						else
							System.out.println("teleportToField()... p.getField("+newField+").getTimeline().getLower() returns null!!");
//						System.out.println("  teleportToField()...  Teleported to field "+state.teleportToField+"... will teleport to new location:"+state.teleportGoal+"...");

						setCurrentCluster( state.teleportGoalCluster, goalSegment.getFieldTimelineID() );
//						System.out.println("  teleportToField()...  Will set location to state.teleportGoal:"+state.teleportGoal+"...");
						setLocation(state.teleportGoal);															// Set location
					}
					else
					{
						if(debugSettings.viewer)
							System.out.println("  teleportToField()...  not moving to first time segment.. will set location to state.currentCluster:"+state.currentCluster+"...");
						state.teleportGoalCluster = state.currentCluster;
						setLocation( p.getCurrentCluster().getLocation() );					// Set location to current cluster
						setCurrentCluster( 0, -1 );
					}
				}
			}
		}
	}

	/**
	 * @param inclCurrent Whether to include the current cluster in search
	 * @return Index of nearest cluster to camera
	 */
	public int getNearestCluster(boolean inclCurrent) 	// Returns the cluster nearest to the current camera position, excluding the current cluster
	{
		PVector cPos = getLocation();
		float smallest = 100000.f;
		int smallestIdx = 0;

		if (currentField.getClusters().size() > 0) 
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
					else if (c.getID() != state.currentCluster) 		// If current cluster is excluded
					{
						smallest = dist;
						smallestIdx = c.getID();
					}
				}
			}
		} 
		else
		{
			if(debugSettings.cluster)
				System.out.println("No clusters in field...");
		}

		return smallestIdx;
	}

	/**
	 * Get list of media clusters within a given distance threshold from the viewer
	 * @param amount Number of nearest clusters to return
	 * @param threshold If distance exceeds, will return less than <amount> nearest clusters
	 * @param inclCurrent Include the current cluster?
	 * @return List of indices of nearest clusters to camera			
	 */
	public IntList getNearClusterIDs(int amount, float threshold) 	// -- excluding the current cluster??
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

		for (WMV_Cluster c : cl) 					// Test remaining clusters against list locations and update lists
		{
			float dist = PVector.dist(vPos, c.getLocation());	// Distance from cluster to viewer
			if(dist < threshold)
			{
				int count = 0;
				int largestIdx = -1;
				float largest = -1000.f;

				for(float f : distList)							// Find farthest distance in nearList to compare
				{
					if(f > largest)
					{
						largestIdx = count;
						largest = f;
					}
					count++;
				}

				float fcDist = PVector.dist(vPos, currentField.getCluster(largestIdx).getLocation());		// Distance of farthest cluster on nearList
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
	 * @param fade Whether to fade or jump when teleporting
	 */
	public void moveToNextTimeSegment(boolean currentDate, boolean teleport, boolean fade)
	{
		if(currentDate)
		{
			int newValue = state.currentFieldTimeSegmentOnDate+1;
			if(state.currentFieldDate >= currentField.getTimelines().size())
			{
				state.currentFieldDate = 0;
				state.currentFieldTimeSegmentOnDate = 0;
				System.out.println("--> Current field date reset! currentFieldDate was greater than timelines.size(): "
						+currentField.getTimelines().size()+"  dateline.size(): "+currentField.getDateline().size());
			}
			else
			{
				if(newValue >= currentField.getTimelines().get(state.currentFieldDate).timeline.size()) 	// Reached end of day
				{
					if(debugSettings.viewer) System.out.println("Reached end of day...");
					state.currentFieldDate++;
					if(state.currentFieldDate >= currentField.getDateline().size()) 
					{
						if(debugSettings.viewer) System.out.println("Reached end of year...");
						state.currentFieldDate = 0;
						setCurrentFieldTimeSegmentOnDate(0, true);									// Return to first segment
					}
					else
					{
						while(currentField.getTimelines().get(state.currentFieldDate).timeline.size() == 0)		// Go to next non-empty date
						{
							state.currentFieldDate++;
							if(state.currentFieldDate >= currentField.getDateline().size())
								state.currentFieldDate = 0;
						}
						if(debugSettings.viewer) System.out.println("Moved to next date: "+state.currentFieldDate);
						setCurrentFieldTimeSegmentOnDate(0, true);									// Start at first segment
					}
				}
				else
					setCurrentFieldTimeSegmentOnDate(newValue, true);
			}
		}
		else
		{
			setCurrentFieldTimeSegment(state.currentFieldTimeSegment+1, true);
			if(state.currentFieldTimeSegment >= currentField.getTimeline().timeline.size())
				setCurrentFieldTimeSegment(0, true);									// Return to first segment
		}

		moveToTimeSegmentInField(currentField.getID(), state.currentFieldTimeSegment, teleport, fade);
	}
	
	/**
	 * Move to cluster corresponding to one time segment earlier on timeline
	 * @param currentDate Whether to look only at time segments on current date
	 * @param teleport Whether to teleport or move
	 */
	public void moveToPreviousTimeSegment(boolean currentDate, boolean teleport, boolean fade)
	{
		if(currentDate)
		{
			int newValue = state.currentFieldTimeSegmentOnDate-1;
			if(state.currentFieldDate >= currentField.getTimelines().size())
			{
				state.currentFieldDate = 0;
				state.currentFieldTimeSegmentOnDate = 0;
				System.out.println("--> Current field date reset!... was greater than timelines.size(): "
								+currentField.getTimelines().size()+"  dateline.size(): "+currentField.getDateline().size());
			}
			else
			{
				if(newValue < 0) 															// Reached beginning of day
				{
					state.currentFieldDate--;
					if(state.currentFieldDate < 0) 
					{
						state.currentFieldDate = currentField.getDateline().size()-1;			// Go to last date
						setCurrentFieldTimeSegmentOnDate(currentField.getTimelines().get(state.currentFieldDate).timeline.size()-1, true);		// Go to last segment
					}
					else
					{
						setCurrentFieldTimeSegmentOnDate(currentField.getTimelines().get(state.currentFieldDate).timeline.size()-1, true);		// Start at last segment
					}
				}	
				else
				{
					setCurrentFieldTimeSegmentOnDate(newValue, true);
				}
			}
		}
		else
		{
			setCurrentFieldTimeSegment(state.currentFieldTimeSegment-1, true);
			if(state.currentFieldTimeSegment < 0)
				setCurrentFieldTimeSegment(currentField.getTimeline().timeline.size()-1, true);
		}

		moveToTimeSegmentInField(currentField.getID(), state.currentFieldTimeSegment, teleport, fade);
	}

	/**
	 * Rotate smoothly around X axis to specified angle
	 * @param angle Angle around X axis to rotate to
	 */
	public void turnXToAngle(float angle, int turnDirection)
	{
		if(!state.turningX)
		{
			state.turnXStart = getXOrientation();
			state.turnXTarget = angle;
			
			PVector turnInfo = getTurnInfo(state.turnXStart, state.turnXTarget, turnDirection);
			
			if(turnDirection == 0)
				state.turnXDirection = turnInfo.x;
			else
				state.turnXDirection = turnDirection;
			
			settings.turningXAccelInc = PApplet.map(turnInfo.y, 0.f, PApplet.PI * 2.f, settings.turningAccelerationMin, settings.turningAccelerationMax * 0.2f);
			state.turnXStartFrame = worldState.frameCount;
			state.turningX = true;
		}
	}
	
	/**
	 * Rotate smoothly around Y axis to specified angle
	 * @param angle Angle around Y axis to rotate to
	 */
	public void turnYToAngle(float angle, int turnDirection)
	{
		if(!state.turningY)
		{
			state.turnYStart = getYOrientation();
			state.turnYTarget = angle;
			
			PVector turnInfo = getTurnInfo(state.turnYStart, state.turnYTarget, turnDirection);
			
			if(turnDirection == 0)
				state.turnYDirection = turnInfo.x;
			else
				state.turnYDirection = turnDirection;
			
			settings.turningYAccelInc = PApplet.map(turnInfo.y, 0.f, PApplet.PI * 2.f, settings.turningAccelerationMin, settings.turningAccelerationMax * 0.2f);
			state.turnYStartFrame = worldState.frameCount;
			state.turningY = true;
		}
	}

	/**
	 * Rotate smoothly around X axis by specified angle
	 * @param angle Angle around X axis to rotate by
	 */
	public void turnXByAngle(float angle)
	{
		if(!state.turningX)
		{
			state.turnXStart = getXOrientation();
			state.turnXTarget = state.turnXStart + angle;
			
			PVector turnInfo = getTurnInfo(state.turnXStart, state.turnXTarget, 0);
			
			state.turnXDirection = turnInfo.x;
			state.turnXStartFrame = worldState.frameCount;

			if(debugSettings.viewer && debugSettings.detailed) System.out.println("turnXStartFrame:"+state.turnXStartFrame+" turnXTargetFrame:"+state.turnXTargetFrame+" turnXDirection:"+state.turnXDirection);
			state.turningX = true;
		}
	}
	
	/**
	 * Rotate smoothly around Y axis by specified angle
	 * @param angle Angle around Y axis to rotate by
	 */
	public void turnYByAngle(float angle)
	{
		if(!state.turningY)
		{
			if(angle < 0.f)					// Keep within range 0 to 2π
				angle += 2*PApplet.PI;
			else if(angle > 2*PApplet.PI)
				angle -= 2*PApplet.PI;

			state.turnYStart = getYOrientation();
			state.turnYTarget = state.turnYStart + angle;
			PVector turnInfo = getTurnInfo(state.turnYStart, state.turnYTarget, 0);
			state.turnYDirection = turnInfo.x;
			state.turnYStartFrame = worldState.frameCount;
			state.turningY = true;
		}
	}

	/**
	 * Smoothly turn to look at given media
	 * @param goal Point to smoothly turn towards
	 */
	public void lookAtMedia( int id, int mediaType ) 
	{
		PVector turnLoc = new PVector(0,0,0);
		
		if(debugSettings.viewer)
			System.out.println("Looking at media:"+id+" mediaType:"+mediaType);

		switch(mediaType)
		{
			case 0:			// Image
				turnLoc = currentField.getImage(id).getLocation();
				break;
			case 1:			// Panorama		-- Turn towards "center"?
//				turnLoc = currentField.getImage(id).getLocation();
				break;
			case 2:			// Video
				turnLoc = currentField.getVideo(id).getLocation();
				break;
			case 3:			// Sound		-- Turn towards??
//				turnLoc = currentField.sounds.get(id).getLocation();
				break;
		}
		
		state.turningMediaGoal = new PVector(id, mediaType);
		turnTowards(turnLoc);
	}
	
	/**
	 * Turn smoothly towards given point
	 * @param goal Point to smoothly turn towards
	 */
	private void turnTowards( PVector goal ) 
	{
		if(debugSettings.viewer) 
			System.out.println("Turning towards... goal.x:"+goal.x+" goal.y:"+goal.y+" goal.z:"+goal.z);

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
	}

	/**
	 * Fade out visible media, move to goal, then fade in media in visible range
	 * @param newField Goal field ID; value of -1 indicates to stay in current field
	 */
	private void startTeleport(int newField) 
	{
		p.fadeOutAllMedia();

		state.teleporting = true;
		state.teleportStart = worldState.frameCount;
		state.teleportWaitingCount = 0;
		
		if(newField != -1)
			state.teleportToField = newField;
	}

	/**	 
	 * Calculate the direction, increment size and length of time needed to turn from startingAngle to targetAngle
	 * @param startAngle	Starting angle
	 * @param targetAngle	Target angle
	 * @return				PVector (direction, increment, length in frames): direction -> 1: clockwise and -1: counterclockwise
	 */
	private PVector getTurnInfo(float startAngle, float targetAngle, int direction)
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
	 * Rotate viewer along X axis
	 * @param dir Direction to rotate (1: clockwise, -1: counterclockwise)
	 */
	public void rotateX(int dir)
	{
		state.rotateXDirection = dir;
		state.rotatingX = true;
	}

	/**
	 * Rotate viewer along Y axis
	 * @param dir Direction to rotate (1: clockwise, -1: counterclockwise)
	 */
	public void rotateY(int dir)
	{
		state.rotateYDirection = dir;
		state.rotatingY = true;
	}

	/**
	 * Get cluster along given vector
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
			
			if(Math.abs(result) < PApplet.PI / 6.f && !c.isEmpty())
			{
				System.out.println("Finding Distance of Centered Cluster:"+c.getID()+" at Angle "+result+" from History Vector...");
				if(c.getID() != state.currentCluster)
					clustersAlongVector.append(c.getID());
			}
			else
			{
				if(debugSettings.viewer && debugSettings.detailed)
					System.out.println("Cluster ID:"+c.getID()+" at angle "+result+" from camera..."+" NOT centered!");
			}
		}

		float smallest = 100000.f;
		int smallestIdx = 0;

		for (int i = 0; i < clustersAlongVector.size(); i++) 		// Compare distances of clusters in front
		{
			PVector cPos = getLocation();
			WMV_Cluster c = (WMV_Cluster) currentField.getCluster(i);
			if(debugSettings.viewer && debugSettings.detailed)
				System.out.println("Checking Centered Cluster... "+c.getID());
		
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
			if(debugSettings.viewer && debugSettings.detailed)
				System.out.println("No clusters found along vector!");
			return state.currentCluster;
		}
	}

	/**
	 * @param minTimelinePoints Minimum points in timeline of cluster to move to
	 */
	void moveToNearestClusterWithTimes(int minTimelinePoints, boolean teleport)
	{
		int nextCluster;
		
		nextCluster = state.currentCluster + 1;
		if(nextCluster >= currentField.getClusters().size())
			nextCluster = 0;
		int count = 0;
		boolean found = false;
		while(currentField.getCluster(nextCluster).getTimeline().timeline.size() < 2)
		{
			nextCluster++;
			count++;
			if(nextCluster >= currentField.getClusters().size())
				nextCluster = 0;
			if(count >= currentField.getClusters().size())
				break;
		}

		if(count < currentField.getClusters().size())
			found = true;

		if(debugSettings.viewer && debugSettings.detailed)
			System.out.println("moveToClusterWith2OrMoreTimes... setting attractor:"+currentField.getTimeline().timeline.get(nextCluster).getFieldTimelineID());

		if(found)
		{
			if(settings.teleportToFarClusters && !teleport)
			{
				if( PVector.dist(currentField.getCluster(nextCluster).getLocation(), getLocation()) > settings.farClusterTeleportDistance )
					teleportToCluster(nextCluster, true, -1);
				else
					setAttractorCluster(nextCluster);
			}
			else
			{
				if(teleport)
					teleportToCluster(currentField.getCluster(nextCluster).getID(), true, -1);
				else
					setAttractorCluster(currentField.getCluster(nextCluster).getID());
			}
		}
	}
	
	/**
	 * @param teleport Teleport (true) or navigate (false) to cluster?
	 * Send the camera to a random cluster in the field
	 */
	public void moveToRandomCluster(boolean teleport, boolean fade)
	{
		int rand = (int) p.ml.random(currentField.getClusters().size());
		while(currentField.getCluster(rand).isEmpty())
		{
			rand = (int) p.ml.random(currentField.getClusters().size());
		}

		while(currentField.getCluster(rand).isEmpty() || rand == state.currentCluster)
			rand = (int) p.ml.random(currentField.getClusters().size());

		if(settings.teleportToFarClusters && !teleport)
		{
			if( PVector.dist(currentField.getCluster(rand).getLocation(), getLocation()) > settings.farClusterTeleportDistance )
				teleportToCluster(rand, fade, -1);
			else
				setAttractorCluster( rand );
		}
		else
		{
			if(teleport)
				teleportToCluster(rand, fade, -1);
			else
				setAttractorCluster( rand );
		}
	}
	
	/**
	 * @param newCluster New attractor cluster
	 * Set a specific cluster as the current attractor
	 */
	private void setAttractorCluster(int newCluster)
	{
		stopMovementTransitions();
		stopMoving();									// -- Improve by slowing down instead and then starting
		clearAttractorPoint();
		
		if(debugSettings.viewer)
			System.out.println("Setting new attractor:"+newCluster+" old attractor:"+state.attractorCluster);
			
		state.attractorCluster = newCluster;											// Set attractorCluster
		state.movingToCluster = true;													// Move to cluster
		state.attractionStart = worldState.frameCount;
		
		for(WMV_Cluster c : currentField.getClusters())
			c.setAttractor(false);

		currentField.getCluster(state.attractorCluster).setAttractor(true);
		
		if(currentField.getCluster(state.attractorCluster).getClusterDistance() < state.clusterNearDistance)
		{
			if(currentField.getCluster(state.attractorCluster).getClusterDistance() > worldSettings.clusterCenterSize)
			{
				if(debugSettings.viewer) System.out.println("Moving nearby...");
				state.movingNearby = true;
			}
			else
			{
				if(debugSettings.viewer) System.out.println("Reached attractor without moving...");
				handleReachedAttractor();				// Reached attractor without moving
			}
		}
	}

	public void clearAttractorCluster()
	{
		state.attractorCluster = -1;											// Set attractorCluster
		state.movingToCluster = false;
		state.movingToAttractor = false;
	}
	
	/**
	 * Set viewer location
	 * @param newLocation New location
	 */
	public void setLocation(PVector newLocation)
	{
		if(settings.orientationMode)
			state.location = new PVector(newLocation.x, newLocation.y, newLocation.z);
		else
		{
			camera.teleport(newLocation.x, newLocation.y, newLocation.z);
			state.location = getLocation();										// Update to precise camera location
		}
	}

	/**
	 * Show any image in field if visible
	 */
	public void showImages()
	{
		settings.hideImages = false;
		p.showImages();
	}
	
	/**
	 * Hide all images in field
	 */
	public void hideImages()
	{
		settings.hideImages = true;
		p.hideImages();
	}
	
	/** 
	 * Show any panorama in field if visible
	 */
	public void showPanoramas()
	{
		settings.hidePanoramas = false;
		p.showPanoramas();
	}
	
	/** 
	 * Hide all panoramas in field
	 */
	public void hidePanoramas()
	{
		settings.hidePanoramas = true;
		p.hidePanoramas();
	}
	
	/**
	 * Show any video in field at viewing distance
	 */
	public void showVideos()
	{
		settings.hideVideos = false;
		p.showVideos();
	}
	
	/**
	 * Hide all videos in field
	 */
	public void hideVideos()
	{
		settings.hideVideos = true;
		p.hideVideos();
	}

	/**
	 * Stop any current viewer movement
	 */
	public void stopMoving()
	{
		if(debugSettings.viewer)
			System.out.println("Stopping...");

		state.attraction = new PVector(0,0,0);						
		state.acceleration = new PVector(0,0,0);							
		state.velocity = new PVector(0,0,0);							
		state.walkingAcceleration = new PVector(0,0,0);					
		state.walkingVelocity = new PVector(0,0,0);	
		
		state.walking = false;
		state.slowing = false;
		state.halting = false;
		state.movingToAttractor = false;
		state.movingToCluster = false;
		
//		teleporting = false;
	}
	
	public void stopAllTurningTransitions()
	{
		if(state.rotatingX) 
			state.rotatingX = false;
		if(state.rotatingY)
			state.rotatingY = false;
		if(state.rotatingZ) 
			state.rotatingZ = false; 
		if(state.turningX)
			state.turningX = false;
		if(state.turningY) 
			state.turningY = false;
	}
	
	/** 
	 * Stop all currently running transitions
	 */
	public void stopMovementTransitions()
	{
		if(state.movingX) 
			state.movingX = false;
		if(state.movingY)
			state.movingY = false;
		if(state.movingZ)
			state.movingZ = false;
		if(state.movingToCluster) 
			state.movingToCluster = false;
		if(state.zooming)
			state.zooming = false;

		if(state.waiting)
			state.waiting = false;
		if(isTeleporting())
			state.teleporting = false;
		
		currentField.clearAllAttractors();
	}
	
	/** 
	 * Stop all currently running transitions
	 */
	public void stopAllTransitions()
	{
		if(state.rotatingX) 
			state.rotatingX = false;
		if(state.rotatingY)
			state.rotatingY = false;
		if(state.rotatingZ) 
			state.rotatingZ = false; 
		if(state.movingX) 
			state.movingX = false;
		if(state.movingY)
			state.movingY = false;
		if(state.movingZ)
			state.movingZ = false;
		if(state.turningX)
			state.turningX = false;
		if(state.turningY) 
			state.turningY = false;
		if(state.movingToCluster) 
			state.movingToCluster = false;
		if(state.zooming)
			state.zooming = false;

		if(state.waiting)
			state.waiting = false;
//		if(looking)
//			looking = false;
		if(isTeleporting())
			state.teleporting = false;
		
		currentField.clearAllAttractors();
	}
	
	/**
	 * Reset the viewer to initial state
	 */
	public void reset()
	{
		/* Camera */
		settings.reset();

		/* Time */
		state.currentFieldDate = 0;					// Current date in field dateline
		state.currentFieldTimeSegment = 0;				// Current time segment in field timeline
		
		/* Memory */
		state.movingToAttractor = false;			// Moving to attractor poanywhere in field
		state.movingToCluster = false;				// Moving to cluster 
		state.following = false;					// Is the camera currently navigating from memory?
		
		/* Clusters */
		state.field = 0;							// Current field
		state.currentCluster = 0;					// Cluster currently in view
		state.lastCluster = -1;						// Last cluster visited
		state.attractorCluster = -1;				// Cluster attracting the camera
		state.attractionStart = 0;
		state.teleportGoalCluster = -1;				// Cluster to navigate to (-1 == none)
		state.clusterNearDistanceFactor = 2.f;		// Multiplier for clusterCenterSize to get clusterNearDistance
		
		/* Teleporting */
		state.movementTeleport = false;		// Teleport when following navigation commands
		state.teleporting = false;			// Transition where all images fade in or out
		state.teleportToField = -1;				// What field ID to fade transition to	 (-1 remains in current field)
		state.teleportWaitingCount = 0;			// How long has the viewer been waiting for media to fade out before teleport?
		
		/* Movement */
		state.followMode = 0;					// 0: Timeline 1: GPS Track 2: Memory
		state.walking = false;			// Whether viewer is walking
//		state.walkingAccelInc = 0.002f;		// Camera walking acceleration increment

		state.slowing = false;			// Whether viewer is slowing 
		state.slowingX = false;			// Slowing X movement
		state.slowingY = false;			// Slowing Y movement
		state.slowingZ = false;			// Slowing Z movement
		state.halting = false;			// Viewer is halting
		
		state.movingX = false;			// Is viewer automatically moving in X dimension (side to side)?
		state.movingY = false;			// Is viewer automatically moving in Y dimension (up or down)?
		state.movingZ = false;			// Is viewer automatically moving in Z dimension (forward or backward)?
		state.movingNearby = false;		// Moving to a powithin nearClusterDistance

		state.waiting = false;						// Whether the camera is waiting to move while following a path
		
		/* Looking */
//		looking = false;				// Whether viewer is turning to look for images, since none are visible
//		lookingRotationCount = 0;		// Amount of times viewer has rotated looking for images

		/* Turning */
		state.turningX = false;			// Whether the viewer is turning (right or left)
		state.turningY = false;			// Whether the viewer is turning (up or down)
		
		state.rotatingX = false;			// Whether the camera is rotating in X dimension (turning left or right)?
		state.rotatingY = false;			// Whether the camera is rotating in Y dimension (turning up or down)?
		state.rotatingZ = false;			// Whether the camera is rotating in Z dimension (rolling left or right)?

		/* Interaction */
		state.lastMovementFrame = 500000; 
		state.lastLookFrame = 500000;
		state.clusterLockIdleFrames = 0;				// How long to wait after user input before auto navigation moves the camera?

		/* GPS Tracks */
		state.gpsTrackSelected = false;			// Has a GPS track been selected?
		state.gpsTrackName = "";					// GPS track name

		/* Zooming */
		state.zooming = false;
		state.zoomLength = 15;

		state.location = new PVector(0,0,0);
		state.velocity = new PVector(0,0,0);
		state.acceleration = new PVector(0,0,0);
		state.attraction = new PVector(0,0,0);
		state.walkingVelocity = new PVector(0,0,0);
		state.walkingAcceleration = new PVector(0,0,0);

		history = new ArrayList<WMV_Waypoint>();
		gpsTrack = new ArrayList<WMV_Waypoint>();

		memory = new ArrayList<WMV_Waypoint>();
		path = new ArrayList<WMV_Waypoint>();
		state.teleportGoal = new PVector(0, 0, 0);

		state.field = 0;
		state.currentCluster = 0;
		state.clusterNearDistance = worldSettings.clusterCenterSize * state.clusterNearDistanceFactor;

		initialize(0, 0, 0);
	}

	/**
	 * @return Current viewer world location
	 */
	public PVector getLocation()
	{
		if(settings.orientationMode)
		{
			return state.location;
		}
		else
		{
			state.location = new PVector(camera.getPosition()[0], camera.getPosition()[1], camera.getPosition()[2]);			// Update location
			return state.location;
		}
	}

	/**
	 * @return Current viewer GPS location
	 */
	public PVector getGPSLocation()
	{
		PVector vLoc = getLocation();
		WMV_ModelState m = currentField.getModel().getState();
		
		float newX = PApplet.map( vLoc.x, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, m.lowLongitude, m.highLongitude ); 			// GPS longitude decreases from left to right
		float newY = PApplet.map( vLoc.z, -0.5f * m.fieldLength, 0.5f*m.fieldLength, m.highLatitude, m.lowLatitude ); 			// GPS latitude increases from bottom to top; negative to match P3D coordinate space

		return new PVector(newX, newY);
	}
	
	/**
	 * @return Current viewer velocity
	 */
	public PVector getVelocity()
	{
		if(state.walking)
			return state.walkingVelocity;
		else
			return state.velocity;
	}

	/**
	 * @return Current viewer acceleration
	 */
	public PVector getAcceleration()
	{
		if(state.walking)
			return state.walkingAcceleration;
		else
			return state.acceleration;
	}

	/**
	 * Attract viewer with given force vector
	 * @param force Force vector
	 */
	public void attract(PVector force)
	{
		state.attraction.add( force );		// Add attraction force to camera 
	}
	
	/**
	 * @return Vector representing attracting forces on the viewer
	 */
	public PVector getAttraction()
	{
		return state.attraction;
	}
	
	/**
	 * @return Current camera orientation as PVector in format: Yaw, Pitch, Roll
	 */
	public PVector getOrientation()
	{
		state.orientation = new PVector(camera.getAttitude()[0], camera.getAttitude()[1], camera.getAttitude()[2]);			// Update orientation
		return state.orientation;
	}

	/**
	 * @return Current camera X orientation (Yaw)
	 */
	public float getXOrientation()
	{
		state.orientation = new PVector(camera.getAttitude()[0], camera.getAttitude()[1], camera.getAttitude()[2]);			// Update X orientation
		return state.orientation.x;
	}

	/**
	 * @return Current camera Y orientation (Pitch)
	 */
	public float getYOrientation()
	{
		state.orientation = new PVector(camera.getAttitude()[0], camera.getAttitude()[1], camera.getAttitude()[2]);			// Update Y orientation
		return state.orientation.y;
	}

	/**
	 * @return Current camera Z orientation (Roll)
	 */
	public float getZOrientation()
	{
		state.orientation = new PVector(camera.getAttitude()[0], camera.getAttitude()[1], camera.getAttitude()[2]);			// Update Z orientation
		return state.orientation.z;
	}
	
	/**
	 * Set the current viewer orientation from the OCD camera state
	 */
	public void setOrientation()
	{
		float[] cAtt = camera.getAttitude();			// Get camera attitude (orientation)
		float pitch = cAtt[1], yaw = cAtt[0];
//		float roll = cAtt[2];

		float sinYaw = PApplet.sin(yaw);
		float cosYaw = PApplet.cos(yaw);
		float sinPitch = PApplet.sin(-pitch);
		float cosPitch = PApplet.cos(-pitch);

		PVector camOrientation = new PVector (-cosPitch * sinYaw, sinPitch, -cosPitch * cosYaw);	
		camOrientation.normalize();
		
		state.orientationVector = camOrientation;
		state.target = getTarget();
	}
	
	/**
	 * Set viewer target (look at) point
	 * @param newTarget New target point
	 */
	public void setTarget(PVector newTarget)
	{
		if(newTarget != null)
			camera.aim(newTarget.x, newTarget.y, newTarget.z);
	}
	
	/**
	 * Get viewer target (look at) point
	 */
	public PVector getTarget()
	{
		return new PVector(camera.getTarget()[0], camera.getTarget()[1], camera.getTarget()[2]);	
	}
	
	/**
	 * Get unit vector pointing towards current viewer target point 
	 * @return Current viewer target vector
	 */
	public PVector getTargetVector()
	{
		float[] cTar = camera.getTarget();			// Get camera attitude (orientation)
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
	 * Update turning behavior
	 */
	void updateTurning()
	{
		if (state.turningX && !state.turnSlowingX) 		// Turn X Transition
		{
			state.turningAcceleration.x += settings.turningXAccelInc * state.turnXDirection;
			state.lastMovementFrame = worldState.frameCount;
		}

		if (state.turningY && !state.turnSlowingY) 		// Turn Y Transition
		{
			state.turningAcceleration.y += settings.turningYAccelInc * state.turnYDirection;
			state.lastMovementFrame = worldState.frameCount;
		}

		if(state.turnSlowingX)
		{
			state.turningVelocity.x *= settings.turningDecelInc;
			state.turningAcceleration.x *= settings.turningDecelInc;
		}
		
		if(state.turnSlowingY)
		{
			state.turningVelocity.y *= settings.turningDecelInc;
			state.turningAcceleration.y *= settings.turningDecelInc;
		}
	
		if(state.turnHaltingX)
		{
			state.turningVelocity.x *= settings.turningHaltInc;
			state.turningAcceleration.x *= settings.turningHaltInc;
		}
		
		if(state.turnHaltingY)
		{
			state.turningVelocity.y *= settings.turningHaltInc;
			state.turningAcceleration.y *= settings.turningHaltInc;
		}

//		System.out.println("updateTurning()... turningVelocity.mag():"+turningVelocity.mag()+" turningVelocity.mag()) > 0.f:"+(turningVelocity.mag() > 0.f)+"  turningX:"+ turningX+" turningY:"+turningY+"  turnSlowingX:"+turnSlowingX+" turnSlowingY:"+turnSlowingY+" turnHaltingX:"+turnHaltingX+" turnHaltingY:"+turnHaltingY);

		if(Math.abs(state.turningVelocity.mag()) > 0.f || Math.abs(state.turningAcceleration.mag()) > 0.f)				/* Walking if walkingVelocity or walkingAcceleration > 0 */
		{
			if(!state.turningX)
			{
				state.turningAcceleration.x = 0.f;
				state.turningVelocity.x = 0.f;
			}

			if(!state.turningY)
			{
				state.turningAcceleration.y = 0.f;
				state.turningVelocity.y = 0.f;
			}

			if(Math.abs(state.turningAcceleration.x) > settings.turningAccelerationMax)			// Decelerate if above camMaxVelocity
				state.turningAcceleration.x *= settings.turningDecelInc;				

			if(Math.abs(state.turningAcceleration.y) > settings.turningAccelerationMax)			// Decelerate if above camMaxVelocity
				state.turningAcceleration.y *= settings.turningDecelInc;				

			if(Math.abs(state.turningVelocity.x) > settings.turningVelocityMax)			// Decelerate if above camMaxVelocity
				state.turningAcceleration.x *= settings.turningDecelInc;				

			if(Math.abs(state.turningVelocity.y) > settings.turningVelocityMax)			// Decelerate if above camMaxVelocity
				state.turningAcceleration.y *= settings.turningDecelInc;				

			state.turningVelocity.add(state.turningAcceleration);							// Add acceleration to velocity

			if(camera.getAttitude()[1] + state.turningVelocity.y >= PApplet.PI * 0.5f || camera.getAttitude()[1] - state.turningVelocity.y <= -PApplet.PI * 0.5f)	// Avoid gimbal lock
			{
				state.turningVelocity.y = 0.f;
				state.turningAcceleration.y = 0.f;
			}
			
			if(Math.abs( state.turningVelocity.mag()) > 0.f && Math.abs(state.turningVelocity.x) < settings.turningVelocityMin 
					&& (state.turnSlowingX || state.turnHaltingX) )
				stopTurningX();

			if(Math.abs( state.turningVelocity.mag()) > 0.f && Math.abs(state.turningVelocity.y) < settings.turningVelocityMin 
							&& (state.turnSlowingY || state.turnHaltingY) )
				stopTurningY();

			if(Math.abs(state.turningVelocity.x) == 0.f && state.turnSlowingX )
				state.turnSlowingX = false;

			if(Math.abs(state.turningVelocity.y) == 0.f && state.turnSlowingY)
				state.turnSlowingY = false;
			
			if(Math.abs(state.turningVelocity.x) == 0.f && state.turnHaltingX )
				state.turnHaltingX = false;

			if(Math.abs(state.turningVelocity.y) == 0.f && state.turnHaltingY)
				state.turnHaltingY = false;
		}
		
		if(state.turningX)
		{
			float xTurnDistance = getTurnDistance(getXOrientation(), state.turnXTarget, state.turnXDirection);
			if(Math.abs(xTurnDistance) < state.turningNearDistance) // && !turningNearby)
			{
				if(Math.abs(xTurnDistance) > state.turningCenterSize)
				{
					if(Math.abs(state.turningVelocity.x) > settings.turningVelocityMin)					/* Slow down at attractor center */
						if(state.turningX && !state.turnSlowingX) 
							state.turnSlowingX = true;
				}
				else
				{
					if(Math.abs(state.turningVelocity.x) > settings.turningVelocityMin)					/* Slow down at attractor center */
						if(state.turningX && !state.turnHaltingX) 
							state.turnHaltingX = true;
				}
			}
		}

		if(state.turningY)
		{
			float yTurnDistance = getTurnDistance(getYOrientation(), state.turnYTarget, state.turnYDirection);
			if(Math.abs(yTurnDistance) < state.turningNearDistance * 0.5f) // && !turningNearby)
			{
				if(Math.abs(yTurnDistance) > state.turningCenterSize * 0.5f)
				{
					if(Math.abs(state.turningVelocity.y) > settings.turningVelocityMin)					/* Slow down at attractor center */
						if(state.turningY && !state.turnSlowingY) 
							state.turnSlowingY = true;
				}
				else
				{
					if(Math.abs(state.turningVelocity.y) > settings.turningVelocityMin)					/* Slow down at attractor center */
						if(state.turningY && !state.turnHaltingY) 
							state.turnHaltingY = true;
				}
			}
		}

		if( state.turningX || state.turningY )
		{
			turn();
		}
		else														// Just stopped turning
		{
			if(settings.optimizeVisibility && !currentField.mediaAreFading())
			{
				if(state.turningMediaGoal != null)
				{
					if(!state.turningMediaGoal.equals(new PVector(-1.f, -1.f)))
					{
						float goalMediaBrightness = 0.f;
						switch((int)state.turningMediaGoal.y)
						{
							case 0:
								WMV_Image img = currentField.getImage((int)state.turningMediaGoal.x);
								goalMediaBrightness = img.getViewingBrightness();
								break;
							case 1:
								WMV_Panorama pano = currentField.getPanorama((int)state.turningMediaGoal.x);
								goalMediaBrightness = pano.getViewingBrightness();
								break;
							case 2:
								WMV_Video vid = currentField.getVideo((int)state.turningMediaGoal.x);
								goalMediaBrightness =  vid.getViewingBrightness();
								break;
						}
						
//						System.out.println("keepMediaVisible... goalMediaBrightness:"+goalMediaBrightness);
						if(goalMediaBrightness == 0.f && settings.angleFading)
						{
							if(debugSettings.viewer)
								System.out.println("Set angle fading to false...");
							settings.angleFading = false;
						}
						
						state.turningMediaGoal = new PVector(-1.f, -1.f);
					}
				}
			}
		}
	}
	
	public void stopTurningX()
	{
		state.turningX = false;
		state.turnSlowingX = false;
		state.turnHaltingX = false;
		state.turningVelocity.x = 0.f;			
	}
	
	public void stopTurningY()
	{
		state.turningY = false;
		state.turnSlowingY = false;
		state.turnHaltingY = false;
		state.turningVelocity.y = 0.f;			
	}
	
	/**
	 * Update physical model each frame
	 */
	public void updatePhysics()
	{
		if(Math.abs(state.walkingVelocity.mag()) > 0.f || Math.abs(state.walkingAcceleration.mag()) > 0.f)				/* Walking if walkingVelocity or walkingAcceleration > 0 */
		{
			if(!state.walking)
			{
				state.acceleration = new PVector(0,0,0);
				state.velocity = new PVector(0,0,0);
				state.walking = true;
			}
			
			if(Math.abs(state.walkingVelocity.mag()) > settings.velocityMax)			// Decelerate if above camMaxVelocity
				state.walkingAcceleration.mult(settings.camDecelInc);				

			state.walkingVelocity.add(state.walkingAcceleration);			// Add acceleration to velocity

			walk();												// Move the camera manually 

			if(Math.abs(state.walkingVelocity.mag()) > 0.f && Math.abs(state.walkingVelocity.mag()) < settings.velocityMin && !state.movingX && !state.movingY && !state.movingZ)
			{
				state.slowingX = false;
				state.slowingY = false;
				state.slowingZ = false;
				state.walkingVelocity = new PVector(0,0,0);			// Clear walkingVelocity when reaches close to zero (below settings.velocityMin)
				state.walking = false;
			}

			if(Math.abs(state.walkingVelocity.mag()) == 0.f && (state.slowingX  || state.slowingY ||	state.slowingZ ) )
			{
				state.slowingX = false;
				state.slowingY = false;
				state.slowingZ = false;
			}
		}
		else if( state.movingToAttractor || state.movingToCluster || state.following )								
		{
			if(state.walking) state.walking = false;

			if(Math.abs(state.attraction.mag()) > 0.f)					/* If not slowing and attraction force exists */
			{
				if(Math.abs(state.acceleration.mag()) < settings.accelerationMax)			/* Apply attraction up to maximum acceleration */
					state.acceleration.add( PVector.div(state.attraction, settings.cameraMass) );	
				else
					System.out.println("--> Attraction but no acceleration... attraction.mag():"+state.attraction.mag()+" acceleration.mag():"+state.acceleration.mag());
			}

			if(state.slowing)
			{
				state.attraction.mult(settings.camDecelInc);
				state.acceleration.mult(settings.camDecelInc);							// Decrease acceleration
				state.velocity.mult(settings.camDecelInc);								// Decrease velocity
			}
			
			if(state.halting)
			{
				state.attraction = new PVector(0,0,0);
				state.acceleration = new PVector(0,0,0);
				state.velocity.mult(settings.camHaltInc);								// Decrease velocity
			}

			if(Math.abs(state.acceleration.mag()) > 0.f)					// Add acceleration to velocity
				state.velocity.add(state.acceleration);					
			
			if(Math.abs(state.velocity.mag()) > settings.velocityMax)				/* If reached max velocity, slow down */
			{
				state.acceleration.mult(settings.camDecelInc);							// Decrease acceleration
				state.velocity.mult(settings.camDecelInc);								// Decrease velocity
			}

			if(state.acceleration.mag() != 0.f && Math.abs(state.acceleration.mag()) < settings.accelerationMin)		/* Set acceleration to zero when below minimum */
				state.acceleration = new PVector(0,0,0);			
			
			if(state.velocity.mag() != 0.f && Math.abs(state.velocity.mag()) < settings.velocityMin)		/* If reached min velocity, set velocity to zero */
				state.velocity = new PVector(0,0,0);							

			WMV_Cluster curAttractor = new WMV_Cluster(worldSettings, worldState, settings, debugSettings, 0, new PVector(0.f, 0.f, 0.f));	 /* Find current attractor if one exists */
			boolean attractorFound = false;
			
			if( state.movingToCluster )
			{
				if(state.attractorCluster != -1)
				{
					curAttractor = getAttractorCluster();
//					System.out.println(" attractorCluster:"+attractorCluster+" is null? "+(curAttractor == null));
				}
			}
			else if( state.movingToAttractor )
			{
				if( attractorPoint != null )
				{
					curAttractor = attractorPoint;
					if(debugSettings.viewer)					/* If not slowing and attraction force exists */
						System.out.println("--> attractorCluster:"+state.attractorCluster+" slowing:"+state.slowing+" halting:"+state.halting+" attraction.mag():"+state.attraction.mag()+" null? "+(curAttractor == null));
					if(debugSettings.viewer)					/* If not slowing and attraction force exists */
						System.out.println("--> attractorPoint distance:"+attractorPoint.getClusterDistance()+" mass:"+attractorPoint.getClusterMass()+" acceleration.mag():"+state.acceleration.mag()+" curAttractor dist: "+(curAttractor.getClusterDistance()));
				}
			}
				
			if(curAttractor != null) 
				attractorFound = true;
			
			if(attractorFound)					
			{
				if(!state.waiting)
				{
					boolean reachedAttractor = false;
					
					if(curAttractor.getClusterDistance() < state.clusterNearDistance && !state.movingNearby)
						if(Math.abs(state.velocity.mag()) > settings.velocityMin)					/* Slow down at attractor center */
							if(!state.slowing) slow();

					if(curAttractor.getClusterDistance() < worldSettings.clusterCenterSize)
					{
						if(Math.abs(state.velocity.mag()) > settings.velocityMin)					/* Slow down at attractor center */
						{
							if(!state.halting) halt();
						}
						else 
						{
							if(state.halting) state.halting = false;
							if(state.slowing) state.slowing = false;
							if(state.movingNearby) state.movingNearby = false;
							reachedAttractor = true;
						}
					}

					if(reachedAttractor) 
						handleReachedAttractor();
				}
				else
				{
					if(debugSettings.viewer && debugSettings.detailed)
						System.out.println("Waiting...");
				}
			}

			state.location.add(state.velocity);			// Add velocity to location
			setLocation(state.location);				// Move camera
		}

		if(state.attractorCluster != -1)
		{
			float curAttractorDistance = PVector.dist( currentField.getCluster(state.attractorCluster).getLocation(), getLocation() );
			if(curAttractorDistance > settings.lastAttractorDistance && !state.slowing)	// If the camera is getting farther than attractor
			{
				if(debugSettings.viewer && state.attractionStart - worldState.frameCount > 20)
				{
					System.out.println("---> Getting farther from attractor: will stop moving...");
					stopMoving();												// Stop
					stopMovementTransitions();
				}
			}

			/* Record last attractor distance */
			settings.lastAttractorDistance = PVector.dist(currentField.getCluster(state.attractorCluster).getLocation(), getLocation());
		}

		/* Reset acceleration each frame */
		state.acceleration = new PVector(0,0,0);			// Clear acceleration vector
		state.walkingAcceleration = new PVector(0,0,0);	// Clear acceleration vector
		state.attraction = new PVector(0,0,0);			// Clear attraction vector
	}
	
	/**
	 * Handle when viewer has reached attractorPoint or attractorCluster
	 */
	private void handleReachedAttractor()
	{
		if(state.following && path.size() > 0)
		{
			setCurrentCluster( state.attractorCluster, -1 );
			if(debugSettings.path)
				System.out.println("Reached path goal #"+state.pathLocationIdx+", will start waiting...");
			startWaiting();
		}

		if(state.movingToCluster)		
		{
			if(debugSettings.viewer)
				System.out.println("Moving to cluster... current:"+state.currentCluster+" attractor: "+state.attractorCluster+"...");
			
			if(state.attractorCluster != -1)
			{
				if(state.movingToTimeSegment)
					setCurrentCluster( state.attractorCluster, state.timeSegmentTarget );
				else
					setCurrentCluster( state.attractorCluster, -1 );

				state.attractorCluster = -1;
				
				currentField.clearAllAttractors();	// Stop attracting when reached attractorCluster
			}
			else
			{
				setCurrentCluster( getNearestCluster(true), -1 );
			}
			
			if(debugSettings.viewer)
				System.out.println("Reached cluster... current:"+state.currentCluster+" nearest: "+getNearestCluster(false)+" set current time segment to "+state.currentFieldTimeSegment);
			state.movingToCluster = false;
		}

		if(state.movingToAttractor)		// Stop attracting when reached attractorPoint
		{
			setCurrentCluster( getNearestCluster(true), -1 );		// Set currentCluster to nearest
			currentField.clearAllAttractors();
			state.movingToAttractor = false;
		}
	}

	/**
	 * Update walking movement parameters
	 */
	public void updateWalking()
	{
		/* Move X Transition */
		if (state.movingX && !state.slowingX) 
		{
			state.walkingAcceleration.x += settings.walkingAccelInc * state.moveXDirection;
			state.lastMovementFrame = worldState.frameCount;
		}

		/* Move Y Transition */
		if (state.movingY && !state.slowingY) 
		{
			state.walkingAcceleration.y += settings.walkingAccelInc * state.moveYDirection;
			state.lastMovementFrame = worldState.frameCount;
		}

		/* Move Z Transition */
		if (state.movingZ && !state.slowingZ) 		
		{
			state.walkingAcceleration.z += settings.walkingAccelInc * state.moveZDirection;
			state.lastMovementFrame = worldState.frameCount;
		}

		if(state.slowingX || state.slowingY || state.slowingZ)
		{
			state.walkingVelocity.mult(settings.camDecelInc);
			state.walkingAcceleration.mult(settings.camDecelInc);
		}
	}
	
	/***
	 * Apply walking velocity to viewer position
	 */
	private void walk()
	{
		if(settings.orientationMode)					// Add relativeVelocity to staticLocation
			state.location.add(state.walkingVelocity);	
		else 								// Move the camera
		{
			if(state.walkingVelocity.x != 0.f)
				camera.truck(state.walkingVelocity.x);
			if(state.walkingVelocity.y != 0.f)
				camera.boom(state.walkingVelocity.y);
			if(state.walkingVelocity.z != 0.f)
				camera.dolly(state.walkingVelocity.z);
		}
	}
	
	/***
	 * Apply turning velocity to viewer direction
	 */
	private void turn()
	{
		if(state.turningVelocity.x != 0.f)
			camera.pan(state.turningVelocity.x);
		if(state.turningVelocity.y != 0.f)
			camera.tilt(state.turningVelocity.y);
	}
	
	/**
	 * Start slowing the viewer
	 */
	private void slow()
	{
		if(debugSettings.viewer && debugSettings.detailed)
		{
			WMV_Cluster curAttractor = getAttractorCluster();
			if(curAttractor != null) System.out.println("Slowing... curAttractor.getClusterDistance():"+curAttractor.getClusterDistance());
			else System.out.println("Slowing... no attractor");
		}
		
		state.slowing = true;										// Slowing when close to attractor
	}

	/**
	 * Start halting the viewer
	 */
	private void halt()
	{
		if(debugSettings.viewer && debugSettings.detailed)
		{
			WMV_Cluster curAttractor = getAttractorCluster();
			if(curAttractor != null) System.out.println("Halting...  curAttractor.getClusterDistance():"+curAttractor.getClusterDistance());
			else System.out.println("Halting... no attractor");
		}
		
		state.slowing = false;
		state.halting = true;										// Slowing when close to attractor
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

		IntList nearClusters = getNearClusterIDs(20, worldSettings.defaultFocusDistance * 4.f);	// Find 20 nearest clusters -- Change based on density?
		IntList frontClusters = new IntList();
		
		for (int i : nearClusters) 							// Iterate through the clusters
		{
			WMV_Cluster c = currentField.getCluster(i);
			PVector clusterVector = getVectorToCluster(c);
			PVector crossVector = new PVector();
			PVector.cross(camOrientation, clusterVector, crossVector);		// Cross vector gives angle between camera and image
			float result = crossVector.mag();
			
			if(Math.abs(result) < settings.fieldOfView && c.getID() != state.currentCluster && !c.isEmpty())			// If cluster (center) is within field of view
			{
				if(debugSettings.cluster || debugSettings.viewer)
					System.out.println("Centered cluster:"+c.getID()+" == "+i+" at angle "+result+" from camera...");
				frontClusters.append(c.getID());
			}
			else
				if(debugSettings.cluster || debugSettings.viewer)
					System.out.println("Non-centered, current or empty cluster:"+c.getID()+" at angle "+result+" from camera..."+" NOT centered!");
		}

		float smallest = 100000.f;
		int smallestIdx = 0;

		for (int i = 0; i < frontClusters.size(); i++) 		// Compare distances of clusters in front
		{
			WMV_Cluster c = (WMV_Cluster) currentField.getCluster(i);
			if(debugSettings.cluster || debugSettings.viewer)
				System.out.println("Checking Centered Cluster... "+c.getID());
		
			float dist = PVector.dist(getLocation(), c.getLocation());
			if (dist < smallest) 
			{
				if(debugSettings.cluster || debugSettings.viewer)
					System.out.println("Cluster "+c.getID()+" is closer!");
				smallest = dist;
				smallestIdx = i;
			}
		}		
		
		if(frontClusters.size() > 0)
			return smallestIdx;
		else
		{
			System.out.println("No clusters ahead!");
			return state.currentCluster;
		}
	}
	
	/**
	 * Update movement variables and perform interpolation
	 */
	private void updateMovement() 
	{		
		if (	state.rotatingX || state.rotatingY || state.rotatingZ || state.movingX || state.movingY || state.movingZ || state.zooming || 
				state.turningX || state.turningY || state.waiting || state.zooming || state.movingToCluster || state.movingToAttractor  )
		{
			/* Rotate X Transition */
			if (state.rotatingX) {
				camera.pan(settings.rotateIncrement * state.rotateXDirection);
				state.lastLookFrame = worldState.frameCount;
			}

			/* Rotate Y Transition */
			if (state.rotatingY) {
				camera.tilt(settings.rotateIncrement * state.rotateYDirection);
				state.lastLookFrame = worldState.frameCount;
			}

			/* Rotate Z Transition */
			if (state.rotatingZ) 
			{
				camera.roll(settings.rotateIncrement * state.rotateZDirection);
				state.lastLookFrame = worldState.frameCount;
			}
			
			if(state.following && state.waiting)				// Update following a path
				updateFollowing();

			/* Zoom Transition */
			if (state.zooming) 
			{
				float fov = getFieldOfView();
				if(fov > 0.1f && fov < 2 * Math.PI) 
					zoomByAmount(settings.zoomIncrement / state.zoomLength * state.zoomDirection);
			}
		}
		else										// If no transitions and not currently moving or turning 
		{
			if(worldState.frameCount % 30 == 0 && settings.alwaysLookAtMedia)	
			{
				if( !mediaAreVisible(false, 1) )	// Check whether any images are currently visible anywhere in front of camera
				{
					if(debugSettings.viewer)
						System.out.println("No images visible! will look at nearest image...");
					if(settings.alwaysLookAtMedia)
						lookAtNearestMedia();			// Look for images around the camera
				}
			}
		}
	}
	
	/**
	 * Update path following behavior
	 */
	private void updateFollowing()
	{
		int waitLength = settings.pathWaitLength;
		if(worldState.frameCount > state.pathWaitStartFrame + waitLength )
		{
			state.waiting = false;
			if(debugSettings.path) System.out.println("Finished waiting...");

			state.pathLocationIdx++;
			
			if(state.pathLocationIdx < path.size())
			{
				state.pathGoal = path.get(state.pathLocationIdx).getLocation();
				if(debugSettings.path) System.out.println("--> updateFollowing()... Next path location:"+state.pathGoal);
				
				if(state.pathLocationIdx >= 1)
				{
					if( state.pathGoal != path.get(state.pathLocationIdx-1).getLocation() && PVector.dist(state.pathGoal, state.location) > worldSettings.clusterCenterSize)
					{
						if(debugSettings.path) System.out.println("Will "+(state.followTeleport?"teleport":"move") +" to next attraction point..."+state.pathGoal);
						if(state.followTeleport)
						{
							if(!currentField.mediaAreFading())
								teleportToPoint(state.pathGoal, true);
							else
								startWaiting();
						}
						else
							setAttractorPoint(state.pathGoal);
					}
					else
					{
						if(debugSettings.path) System.out.println("Same or very close attraction point!");
						
						if(settings.orientationModeConstantWaitLength)
						{
							if(debugSettings.path) System.out.println("Ignoring pathLocationIdx #"+state.pathLocationIdx+" at same location as previous...");
							
							state.pathLocationIdx++;
							state.pathGoal = path.get(state.pathLocationIdx).getLocation();
							
							while(state.pathGoal == path.get(state.pathLocationIdx-1).getLocation())
							{
								if(debugSettings.path) System.out.println(" Also ignoring pathLocationIdx #"+state.pathLocationIdx+" at same location as previous...");
								state.pathLocationIdx++;
								state.pathGoal = path.get(state.pathLocationIdx).getLocation();
							}
						}
						
						if(debugSettings.path) System.out.println("--> Chose new path location:"+state.pathGoal);
						if(state.followTeleport)
						{
							if(!currentField.mediaAreFading())
								teleportToPoint(state.pathGoal, true);
							else
								startWaiting();
						}
						else
							setAttractorPoint(state.pathGoal);
					}
				}
			}
			else
			{
				if(debugSettings.path)
				{
					System.out.println("Reached end of path... ");
					System.out.println(" ");
				}
				stopFollowing();
			}
		}
		
		if(state.waiting == false && debugSettings.path) 
			System.out.println("Finished waiting...");

	}
	
	/**
	 * Look at nearest media to current viewer orientation
	 */
	public void lookAtNearestMedia()
	{
		float closestDist = 100000.f;
		int closestID = -1;
		int closestMediaType = -1;
		
		WMV_Cluster c = p.getCurrentCluster();
		
		if(c != null)
		{
			for(int i:c.getState().images)
			{
				WMV_Image img = currentField.getImage(i);
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

			for(int i:c.getState().videos)
			{
				WMV_Video vid = currentField.getVideo(i);
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
			
//			if(c.panorama)
			if(c.getState().panoramas.size() == 0)
				lookAtMedia(closestID, closestMediaType);				// Look at media with the smallest turn distance
		}
	}
	
	/**
	 * Zoom by given amount
	 * @param zoom Zoom amount
	 */
	public void zoomByAmount(float zoom)
	{
		setFieldOfView( settings.fieldOfView + zoom );
//		settings.fieldOfView += zoom;
		camera.zoom(zoom);
	}

	/**
	 * Reset the 3D camera
	 */
	public void resetCamera()
	{
		initialize( getLocation().x, getLocation().y, getLocation().z );							// Initialize camera
	}
	
	/**
	 * Update teleporting interpolation values
	 */
	private void updateTeleporting()
	{
		if(worldState.frameCount >= state.teleportStart + settings.teleportLength)		// If the teleport has finished
		{
			if(debugSettings.viewer && debugSettings.detailed) System.out.println(" Reached teleport goal...");
			
			if( !currentField.mediaAreFading() )			// Once no more images are fading
			{
				if(debugSettings.viewer && debugSettings.detailed) System.out.println(" Media finished fading...");
				
				if(state.following && path.size() > 0)
				{
					setCurrentCluster( getNearestCluster(true), -1 );
					if(debugSettings.path)
						System.out.println("Reached path goal #"+state.pathLocationIdx+", will start waiting...");
					startWaiting();
				}

				if(state.teleportToField != -1)							// If a new field has been specified 
				{
					enterField(state.teleportToField);					// Enter new field
					if(debugSettings.viewer) 
						System.out.println(" Teleported to field "+state.teleportToField+" goal point: x:"+state.teleportGoal.x+" y:"+state.teleportGoal.y+" z:"+state.teleportGoal.z);
					state.teleportToField = -1;
				}
				
				if(state.ignoreTeleportGoal)
					state.ignoreTeleportGoal = false;
				else
					setLocation(state.teleportGoal);					// Move the viewer
				
				state.teleporting = false;								// Change the system status

				if(state.teleportGoalCluster != -1)
				{
					if(state.movingToTimeSegment)
						setCurrentCluster( state.teleportGoalCluster, state.timeSegmentTarget );
					else
						setCurrentCluster( state.teleportGoalCluster, -1 );

					state.teleportGoalCluster = -1;
				}

				if(state.movingToCluster)
				{
					state.movingToCluster = false;
					if(state.attractorCluster != -1)
					{
						state.attractorCluster = -1;
						currentField.clearAllAttractors();
					}
					else
					{
						setCurrentCluster( getNearestCluster(true), -1 );
					}
				}
				
				if(state.movingToAttractor)
				{
					state.movingToAttractor = false;
					setCurrentCluster( getNearestCluster(true), -1 );		// Set currentCluster to nearest

					currentField.clearAllAttractors();						// Clear current attractors
				}
				
				if(p.state.waitingToFadeInTerrainAlpha) 		// Fade in terrain
					p.fadeInTerrain();
			}
			else
			{
				state.teleportWaitingCount++;
				if(debugSettings.viewer && debugSettings.detailed)
					System.out.println("Waiting to finish teleport... "+state.teleportWaitingCount);
			}
		}
	}
	
	/**
	 * Follow the current field timeline as a path
	 */
	public void followTimeline(boolean start, boolean fromBeginning)
	{
		if(start)								/* Start following timeline */
		{
			if(!state.following)
			{
				path = currentField.getTimelineAsPath();			/* Get timeline as path of Waypoints matching cluster IDs */
				
				if(path.size() > 0)
				{
					WMV_Cluster c = p.getCurrentCluster();
					if(c != null)
					{
						state.following = true;
						state.pathLocationIdx = -1;							

						if(fromBeginning)						/* Start at beginning */
						{
							state.pathLocationIdx = 0;
						}
						else									/* Find path location of current cluster and set as beginning */
						{
							int count = 0;
							for(WMV_Waypoint w : path)
							{
								if(w.getID() == c.getID())
								{
									state.pathLocationIdx = count;
									break;
								}
								count++;
							}

							if(state.pathLocationIdx == -1) state.pathLocationIdx = 0;
						}

						if(debugSettings.viewer)
							System.out.println("followTimeline()... Setting first path goal: "+path.get(state.pathLocationIdx).getLocation());

						state.pathGoal = path.get(state.pathLocationIdx).getLocation();
						setAttractorPoint(state.pathGoal);
					}
					else System.out.println("No current cluster!");
				}
				else System.out.println("No timeline points!");
			}
			else
			{
				if(debugSettings.viewer)
					System.out.println("Already called followTimeline(): Stopping... "+path.get(state.pathLocationIdx).getLocation());
				state.pathLocationIdx = 0;
				state.following = false;
			}
		}
		else																/* Stop following timeline */
		{
			if(state.following)
			{
				state.following = false;
				stopMovementTransitions();
				stopMoving();									// -- Improve by slowing down instead and then starting 
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
			state.following = true;
			state.pathLocationIdx = 0;
			if(debugSettings.viewer)
				System.out.println("--> followMemory() points:"+path.size()+"... Setting first path goal: "+path.get(state.pathLocationIdx).getLocation());
			state.pathGoal = path.get(state.pathLocationIdx).getLocation();
			setAttractorPoint(state.pathGoal);
		}
		else System.out.println("followMemory()... path.size() == 0!");
	}

	/**
	 * Follow GPS track
	 */
	public void followGPSTrack()
	{
		path = new ArrayList<WMV_Waypoint>(gpsTrack);								// Follow memory path 
		
		if(path.size() > 0)
		{
			state.following = true;
			state.pathLocationIdx = 0;
			if(debugSettings.viewer)
				System.out.println("--> followGPSTrack() points:"+path.size()+"... Setting first path goal: "+path.get(state.pathLocationIdx).getLocation());
			state.pathGoal = path.get(state.pathLocationIdx).getLocation();
			setAttractorPoint(state.pathGoal);
		}
		else System.out.println("path.size() == 0!");
	}
	
	/**
	 * Wait for specified time until moving to next waypoint in path
	 * @param length Length of time to wait
	 */
	private void startWaiting()	
	{
		state.waiting = true;
		state.pathWaitStartFrame = worldState.frameCount;
	}
	
	/**
	 * @param newPoint Point of interest to attract camera 
	 */
	private void setAttractorPoint(PVector newPoint)
	{
		stopMovementTransitions();
		stopMoving();					
		
		state.movingToAttractor = true;
		attractorPoint = new WMV_Cluster(worldSettings, worldState, settings, debugSettings, -1, newPoint);
		attractorPoint.setEmpty(false);
		attractorPoint.setAttractor(true);
		attractorPoint.setMass(worldSettings.mediaPointMass * 25.f);
		state.attractionStart = worldState.frameCount;
	}
	
	/**
	 * Clear the current attractor point
	 */
	private void clearAttractorPoint()
	{
		state.movingToAttractor = false;
		attractorPoint = null;
	}
	
	public void setOrientationMode( boolean newState )
	{
		settings.orientationMode = newState;
		
		if(newState)
		{
			PVector target = new PVector(camera.getTarget()[0], camera.getTarget()[1], camera.getTarget()[2]);
			camera.teleport(0, 0, 0);
			
			target = new PVector(target.x - getLocation().x, target.y - getLocation().y, target.z - getLocation().z);
			camera.aim(target.x, target.y, target.z);
			target = new PVector(camera.getTarget()[0], camera.getTarget()[1], camera.getTarget()[2]);
			
			updateOrientationMode();
		}
		else
		{
			camera.teleport(state.location.x, state.location.y, state.location.z);
		}
		
		if(p.ml.display.window.setupGraphicsWindow)
			p.ml.display.window.chkbxOrientationMode.setSelected(newState);
	}
	
	/**
	 * @param front Restrict to media in front
	 * @param threshold Minimum number of media to for method to return true
	 * @return Whether any media are visible and in front of camera
	 */
	public boolean mediaAreVisible( boolean front, int threshold )
	{
		IntList nearClusters = getNearClusterIDs(10, settings.farViewingDistance + worldSettings.defaultFocusDistance); 	

		if(nearClusters.size() == 0)
			return false;
		
		boolean imagesVisible = false;
		ArrayList<WMV_Image> closeImages = new ArrayList<WMV_Image>();				// List of images in range
		boolean panoramasVisible = false;
		ArrayList<WMV_Panorama> closePanoramas = new ArrayList<WMV_Panorama>();		// List of panoramas in range
		boolean videosVisible = false;
		ArrayList<WMV_Video> closeVideos = new ArrayList<WMV_Video>();				// List of videos in range
		boolean soundsVisible = false;
		ArrayList<WMV_Sound> closeSounds = new ArrayList<WMV_Sound>();				// List of sounds in range
		
		float result;
		
		for(int clusterID : nearClusters)
		{
			WMV_Cluster cluster = currentField.getCluster(clusterID);
			for( int id : cluster.getState().images )
			{
				WMV_Image i = currentField.getImage(id);
				if( i.getViewingDistance() < settings.farViewingDistance + i.getFocusDistance() && 
				    i.getViewingDistance() > settings.nearClippingDistance * 2.f )		// Find images in range
				{
					if(!i.getMediaState().disabled)
						closeImages.add(i);							
				}
			}

			for( int id : cluster.getState().panoramas )
			{
				WMV_Panorama n = currentField.getPanorama(id);
				if( n.getCaptureDistance() < settings.farViewingDistance + worldSettings.defaultFocusDistance &&
				    n.getCaptureDistance() > settings.nearClippingDistance * 2.f )		// Find images in range
				{
					if(!n.getMediaState().disabled)
						closePanoramas.add(n);							
				}
			}

			for( int id : cluster.getState().videos )
			{
				WMV_Video v = currentField.getVideo(id);
				if(v.getViewingDistance() <= settings.farViewingDistance + v.getFocusDistance()
				&& v.getViewingDistance() > settings.nearClippingDistance * 2.f )		// Find videos in range
				{
					if(!v.getMediaState().disabled)
						closeVideos.add(v);							
				}
			}

			for( int id : cluster.getState().sounds )
			{
				WMV_Sound s = currentField.getSound(id);
				if(s.getCaptureDistance() <= settings.farViewingDistance + worldSettings.defaultFocusDistance &&
				   s.getCaptureDistance() > settings.nearClippingDistance * 2.f )		// Find videos in range
				{
					if(!s.getMediaState().disabled)
						closeSounds.add(s);							
				}
			}
		}

		int visPanoramas = closePanoramas.size();
		int visSounds = closeSounds.size();

		if(closePanoramas.size() > 0)
		{
			panoramasVisible = true;
//			return true;
		}

		if(closeSounds.size() > 0)
		{
			soundsVisible = true;
//			return true;
		}

		int visImages = 0;
		for( WMV_Image i : closeImages )
		{
			if(!i.isBackFacing(getLocation()) && !i.isBehindCamera(getLocation(), getOrientationVector()))			// If image is ahead and front facing
			{
				result = Math.abs(i.getFacingAngle(getOrientationVector()));			// Get angle at which it faces camera

				if(front)										// Look for centered or only visible image?
				{
					if(result < settings.centeredAngle)					// Find closest to camera orientation
					{
						if(debugSettings.viewer && debugSettings.detailed)
							System.out.println("Image:"+i.getID()+" result:"+result+" is less than centeredAngle:"+settings.centeredAngle);
						imagesVisible = true;
						visImages++;
						if(visImages >= threshold)
							break;
					}
				}
				else
				{
					if(result < settings.visibleAngle * 0.66f)						// Find closest to camera orientation
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
			if(!v.isBackFacing(getLocation()) && !v.isBehindCamera(getLocation(), getOrientationVector()))			// If video is ahead and front facing
			{
				result = Math.abs(v.getFacingAngle(getOrientationVector()));			// Get angle at which it faces camera

				if(front)											// Look for centered or only visible image?
				{
					if(result < settings.centeredAngle)					// Find closest to camera orientation
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
					if(result < settings.visibleAngle * 0.66f)						// Find closest to camera orientation
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
			return imagesVisible || panoramasVisible || videosVisible || soundsVisible;
		else
			return (visImages + visPanoramas + visVideos + visSounds) >= threshold;
	}
		
	/**
	 * Add current camera location and orientation to memory
	 */
	public void addPlaceToMemory()
	{
		if(!state.teleporting && !state.walking && state.velocity.mag() == 0.f)		// Only record points when stationary
		{
			WMV_Waypoint curWaypoint = new WMV_Waypoint(path.size(), getLocation(), null, false);				// -- Must calculate time instead of null!!
			curWaypoint.setTarget(getOrientation());
			curWaypoint.setID(state.currentCluster);						// Need to make sure camera is at current cluster!
			
			while(memory.size() > 100)								// Prevent memory path from getting too long
				memory.remove(0);
				
			memory.add(curWaypoint);
			if(debugSettings.viewer) System.out.println("Added point to memory... "+curWaypoint.getLocation()+" Path length:"+memory.size());
		}
		else if(debugSettings.viewer) System.out.println("Couldn't add memory point... walking? "+state.walking+" teleporting?"+state.teleporting+" velocity.mag():"+state.velocity.mag());
	}
	
	/**
	 * Clear the current memory
	 */
	public void clearMemory()
	{
		state.following = false;
		state.waiting = false;
		memory = new ArrayList<WMV_Waypoint>();
	}

	/**
	 * Stop navigation along points in memory
	 */
	public void stopFollowing()
	{
		state.following = false;
		state.pathLocationIdx = 0;
	}
	
	/**
	 * Move to first time segment in field
	 * @param ignoreDate Move to first time segment regardless of date
	 * @return Whether succeeded
	 */
	public boolean moveToFirstTimeSegment(boolean ignoreDate)
	{
		int newDate = 0;
		if(ignoreDate)
		{
			if(debugSettings.viewer) System.out.println("Moving to first time segment on any date");
			moveToTimeSegmentInField(currentField.getID(), 0, true, true);		// Move to first time segment in field
			return true;
		}		
		else
		{
			int count = 0;
			boolean success = false;
			while(!success)
			{
				success = setCurrentTimeSegmentAndDate(0, newDate, true);
				if(success)
					break;
				else
				{
					newDate++;
					count++;
					if(count > currentField.getDateline().size()) 
						break;
				}
			}
			if(success)
			{
				if(debugSettings.viewer) System.out.println("Moving to first time segment on date "+newDate+" state.currentFieldTimeSegmentOnDate:"+state.currentFieldTimeSegmentOnDate+" state.currentFieldDate:"+state.currentFieldDate);
				int curFieldTimeSegment = currentField.getTimeSegmentOnDate(state.currentFieldTimeSegmentOnDate, state.currentFieldDate).getFieldTimelineID();
				moveToTimeSegmentInField(currentField.getID(), curFieldTimeSegment, true, true);		// Move to first time segment in field
			}
			else System.out.println("Couldn't move to first time segment...");
			return success;
		}
	}
	
	/**
	 * Start viewing selected media in (2D) Media View
	 */
	public void startViewingSelectedMedia()
	{
		List<Integer> selected = new ArrayList<Integer>();				// Find selected media
		int mediaType = 0;												// Media type found
		
		selected = p.getCurrentField().getSelectedImageIDs();
		if(selected.size() == 0)
		{
			selected = p.getCurrentField().getSelectedPanoramaIDs();
			mediaType = 1;
		}
		if(selected.size() == 0)
		{
			selected = p.getCurrentField().getSelectedVideoIDs();
			mediaType = 2;
		}
//		if(selected.size() == 0)									// -- 2D Sound Display in progress
//		{
//			selected = p.getCurrentField().getSelectedSoundIDs();
//			mediaType = 3;
//		}
		
		if(selected.size() == 1)
		{
			switch(mediaType)
			{
				case 0:
					viewImage(selected.get(0));
					break;
				case 1:
					viewPanorama(selected.get(0));
					break;
				case 2:
					viewVideo(selected.get(0));
					break;
//				case 3:
//					viewSound(selected.get(0));
//					break;
			}
		}
		else
		{
			System.out.println("More than 1 media selected!");
		}
		
		p.ml.display.setDisplayView(p, 4);			// Set current view to Media Display View
	}
	
	public void viewImage(int id)
	{
		p.ml.display.setMediaViewObject(0, id);
	}
	
	public void viewPanorama(int id)
	{
		p.ml.display.setMediaViewObject(1, id);
	}
	
	public void viewVideo(int id)
	{
		p.ml.display.setMediaViewObject(2, id);
	}

	public void viewSound(int id)
	{
		p.ml.display.setMediaViewObject(3, id);
	}
	
	/**
	 * Act on the media object (image or video) in front of camera. 				// -- Update to include panoramas + sounds
	 * @param select Whether to select media object
	 * 
	 * Note:
	 * In Selection Mode, selects or deselects the media file.
	 * In Normal Mode, starts or stops a video, but has no effect on an image.
	 */
	public void chooseMediaInFront(boolean select) 
	{
		ArrayList<WMV_Image> possibleImages = new ArrayList<WMV_Image>();
		for(WMV_Image i : currentField.getImages())
		{
			if(i.getViewingDistance() <= settings.selectionMaxDistance)
				if(!i.getMediaState().disabled)
					possibleImages.add(i);
		}

		float closestImageDist = 100000.f;
		int closestImageID = -1;

		for(WMV_Image s : possibleImages)
		{
			if(!s.isBackFacing(getLocation()) && !s.isBehindCamera(getLocation(), getOrientationVector()))					// If image is ahead and front facing
			{
				float result = Math.abs(s.getFacingAngle(getOrientationVector()));				// Get angle at which it faces camera

				if(result < closestImageDist)										// Find closest to camera orientation
				{
					closestImageDist = result;
					closestImageID = s.getID();
				}
			}
		}

		ArrayList<WMV_Video> possibleVideos = new ArrayList<WMV_Video>();
		for(WMV_Video v : currentField.getVideos())
		{
			if(v.getViewingDistance() <= settings.selectionMaxDistance)
				if(!v.getMediaState().disabled)
					possibleVideos.add(v);
		}

		float closestVideoDist = 100000.f;
		int closestVideoID = -1;

		for(WMV_Video v : possibleVideos)
		{
			if(!v.isBackFacing(getLocation()) && !v.isBehindCamera(getLocation(), getOrientationVector()))					// If image is ahead and front facing
			{
				float result = Math.abs(v.getFacingAngle(getOrientationVector()));				// Get angle at which it faces camera

				if(result < closestVideoDist)								// Find closest to camera orientation
				{
					closestVideoDist = result;
					closestVideoID = v.getID();
				}
			}
		}

		if(settings.selection)						// In Selection Mode
		{
			int newSelected;
			if(select && !settings.multiSelection)
				p.deselectAllMedia(false);				// If selecting media, deselect all media unless in Multi Selection Mode

			if(closestImageDist < closestVideoDist && closestImageDist != 100000.f)
			{
				newSelected = closestImageID;
				if(debugSettings.viewer) System.out.println("Selected image in front: "+newSelected);

				if(settings.segmentSelection)											// Segment selection
				{
					int segmentID = -1;
					WMV_Cluster c = currentField.getCluster( currentField.getImage(newSelected).getAssociatedClusterID() );
					for(WMV_MediaSegment m : c.segments)
					{
						if(m.getImages().contains(newSelected))
						{
							segmentID = m.getID();
							break;
						}
					}

					if(select && !settings.multiSelection)
						p.deselectAllMedia(false);						// Deselect all media

					if(segmentID != -1)
					{
						for(int i : c.segments.get(segmentID).getImages())				// Set all images in selected segment to new state
							currentField.getImage(i).setSelected(select);
					}
				}
				else												// Single image selection
				{
					if(newSelected != -1)
						currentField.getImage(newSelected).setSelected(select);
				}
			}
			else if(closestVideoDist < closestImageDist && closestVideoDist != 100000.f)
			{
				newSelected = closestVideoID;
				if(debugSettings.viewer) 	System.out.println("Selected video in front: "+newSelected);

				if(newSelected != -1)
					currentField.getVideo(newSelected).setSelected(select);
			}
		}
		else if(closestVideoDist != 100000.f)					// In Normal Mode
		{
			WMV_Video v = currentField.getVideo(closestVideoID);

			if(!v.isPlaying())									// Play video by choosing it
			{
				if(!v.isLoaded()) v.loadMedia(p.ml);
				v.playVideo();
			}
			else
				v.stopVideo();
			
			if(debugSettings.viewer) 
				System.out.println("Video is "+(v.isPlaying()?"playing":"not playing: ")+v.getID());
		}
	}

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

			for(WMV_Cluster c : currentField.getClusters())
			{
				if(!c.isEmpty())
				{
					float dist = PVector.dist(c.getLocation(), state.location);
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
						if(!list.hasValue(c.getID()) && !(state.currentCluster == c.getID()))
							list.append(c.getID());
					}
					else
					{
						for(int i : list)				// Sort the list lowest to highest
						{
							float checkDist = PVector.dist(currentField.getCluster(i).getLocation(), state.location);
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

					if(dist < leastDist && !(state.currentCluster == c.getID()))		// Ignore the current cluster, since the distance is zero (?)
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
			list.append(state.currentCluster);
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
		state.nearbyClusterTimelineMediaCount = 0;
		
		for(WMV_TimeSegment t : nearbyClusterTimeline)
			state.nearbyClusterTimelineMediaCount += t.getTimeline().size();
	}

	/**
	 * Create nearby cluster timeline from given clusters
	 * @param clusters List of clusters
	 */
	public void createNearbyClusterTimeline(ArrayList<WMV_Cluster> clusters)
	{
		ArrayList<WMV_TimeSegment> timeline = new ArrayList<WMV_TimeSegment>();
		
		if(debugSettings.time)
			System.out.println(">>> Creating Viewer Timeline (Nearby Visible Clusters)... <<<");

		for(WMV_Cluster c : clusters)											// Find all media cluster times
			for(WMV_TimeSegment t : c.getTimeline().timeline)
				timeline.add(t);

		timeline.sort(WMV_TimeSegment.WMV_TimeLowerBoundComparator);				// Sort time segments 
		nearbyClusterTimeline = timeline;
	
		state.nearbyClusterTimelineMediaCount = 0;
		
		for(WMV_TimeSegment t : nearbyClusterTimeline)
			state.nearbyClusterTimelineMediaCount += t.getTimeline().size();

		if(debugSettings.time)
			System.out.println("createNearbyClusterTimeline  nearbyClusterTimeline.size():"+nearbyClusterTimeline.size());
	}

	/**
	 * Get nearby time by timeline index 
	 * @param timeSegmentIndex Index to find
	 * @return Nearby time associated with index
	 */
	public WMV_Time getNearbyTimeByIndex(int timeSegmentIndex)
	{
		WMV_Time time = null;
		for(WMV_TimeSegment ts : nearbyClusterTimeline)
		{
			if(ts.getFieldTimelineID() == timeSegmentIndex)
			{
				time = ts.getTimeline().get(0);
				return time;
			}
		}
		return time;
	}

	/**
	 * Get ID of closest image in front of viewer
	 * @return ID of image closest to viewer in front
	 */
	public int getFrontImage() {
		float smallest = 100000.f;
		int smallestIdx = 0;

		WMV_Field f = currentField;

		for (int i = 0; i < f.getImages().size(); i++) {
			if (f.getImage(i).getMediaState().visible) {
				float imageAngle = f.getImage(i).getFacingAngle(getOrientationVector());
				if (imageAngle < smallest) {
					smallest = imageAngle;
					smallestIdx = i;
				}
			}
		}

		return smallestIdx;
	}

	/**
	 * Get ID of image nearest to the viewer in any direction
	 * @return Nearest image ID
	 */
	public int getNearestImage() {
		float smallest = 100000.f;
		int smallestIdx = 0;
		WMV_Field f = currentField;

		for (int i = 0; i < f.getImages().size(); i++) {
			if (f.getImage(i).getMediaState().visible) {
				float imageDist = f.getImage(i).getViewingDistance();
				if (imageDist < smallest && imageDist > settings.nearClippingDistance) {
					smallest = imageDist;
					smallestIdx = i;
				}
			}
		}

		return smallestIdx;
	}
	
	/**
	 * Get ID of panorama nearest to the viewer in any direction
	 * @return Nearest panorama ID
	 */
	public int getNearestPanorama() {
		float smallest = 100000.f;
		int smallestIdx = 0;
		WMV_Field f = currentField;

		for (int i = 0; i < f.getPanoramas().size(); i++) {
			if (f.getPanorama(i).getMediaState().visible) {
				float panoramaDist = f.getPanorama(i).getCaptureDistance();
				if (panoramaDist < smallest && panoramaDist > settings.nearClippingDistance) {
					smallest = panoramaDist;
					smallestIdx = i;
				}
			}
		}

		return smallestIdx;
	}

	/**
	 * Get ID of closest video in front of viewer
	 * @return ID of video closest to viewer in front
	 */
	public int getFrontVideo() {
		float smallest = 100000.f;
		int smallestIdx = 0;

		WMV_Field f = currentField;

		for (int i = 0; i < f.getVideos().size(); i++) {
			if (f.getVideo(i).getMediaState().visible) {
				float videoAngle = f.getVideo(i).getFacingAngle(getOrientationVector());
				if (videoAngle < smallest) {
					smallest = videoAngle;
					smallestIdx = i;
				}
			}
		}

		return smallestIdx;
	}

	/**
	 * Get ID of video nearest to the viewer in any direction
	 * @return Nearest video ID
	 */
	public int getNearestVideo() {
		float smallest = 100000.f;
		int smallestIdx = 0;
		WMV_Field f = currentField;

		for (int i = 0; i < f.getVideos().size(); i++) {
			if (f.getVideo(i).getMediaState().visible) {
				float videoDist = f.getVideo(i).getViewingDistance();
				if (videoDist < smallest && videoDist > settings.nearClippingDistance) {
					smallest = videoDist;
					smallestIdx = i;
				}
			}
		}

		return smallestIdx;
	}

	/**
	 * Get ID of sound nearest to the viewer in any direction
	 * @return Nearest sound ID
	 */
	public int getNearestSound() {
		float smallest = 100000.f;
		int smallestIdx = 0;
		WMV_Field f = currentField;

		for (int i = 0; i < f.getSounds().size(); i++) {
			if (f.getSound(i).getMediaState().visible) {
				float soundDist = f.getSound(i).getCaptureDistance();
				if (soundDist < smallest && soundDist > settings.nearClippingDistance) {
					smallest = soundDist;
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
		state.gpsTrackSelected = false;
		p.ml.selectInput("Select a GPS Track:", "gpsTrackSelected");
	}

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
	 * Check whether list waypoints are in history
	 * @param check Waypoints to look for
	 * @param historyDepth How far back in history to look
	 * @return Waypoints found in history within the last <memory> waypoints
	 */
	public ArrayList<WMV_Waypoint> waypointsAreInHistory(ArrayList<WMV_Waypoint> check, int historyDepth)
	{
		ArrayList<WMV_Waypoint> found = new ArrayList<WMV_Waypoint>();
		
		for( WMV_Waypoint p : check )
		{
			for(int i = history.size()-1; i >= history.size()-historyDepth; i--)		// Iterate through history from last element to 
			{
				System.out.println("i:"+i);
				WMV_Waypoint w = history.get(i);
				
				if(p.getLocation() == w.getLocation())
					found.add(p);
			}
		}
		
		return found;
	}
	
	/**
	 * @param check List of clusters to check
	 * @param memory How far back to look in memory 
	 * @return Clusters found within the last <memory> waypoints
	 */
	public ArrayList<WMV_Cluster> clustersAreInHistory(IntList check, int memory)
	{
		ArrayList<WMV_Cluster> found = new ArrayList<WMV_Cluster>();
		
		for( int cPoint : check )
		{
			System.out.println("clustersInList()... memory:"+memory);

			for(int i = history.size()-1; i >= history.size()-memory; i--)		// Iterate through history from last element to 
			{
				System.out.println("i:"+i);
				WMV_Waypoint w = history.get(i);
				
				if(currentField.getCluster(cPoint).getLocation() == w.getLocation())
				{
					found.add(currentField.getCluster(cPoint));
				}
			}
		}
		
		return found;
	}
	
	/**
	 * Set current cluster and current time segment
	 * @param newCluster New current cluster
	 * @param newFieldTimeSegment New time segment to set (-1 to ignore this parameter)
	 */
	void setCurrentCluster(int newCluster, int newFieldTimeSegment)
	{
		if(newCluster >= 0 && newCluster < currentField.getClusters().size())
		{
			state.lastCluster = state.currentCluster;
			WMV_Cluster c = p.getCurrentCluster();

			if(c != null) c.getState().timeFading = false;
			
			state.currentCluster = newCluster;
			c = p.getCurrentCluster();
			
			if(debugSettings.viewer && debugSettings.detailed) 
				System.out.println("viewer.setCurrentCluster() to "+newCluster+" at field time segment "+newFieldTimeSegment+"  cluster location:"+c.getLocation()+" viewer location:"+getLocation());
			
			if(c != null)
			{
				if(p.state.timeFading && !c.getState().timeFading) 
					c.getState().timeFading = true;

				WMV_Field f = currentField;
				if(newFieldTimeSegment == -1)						// If == -1, search for time segment
				{
					for(WMV_TimeSegment t : f.getTimeline().timeline)			// Search field timeline for cluster time segment
					{
						if(c.getTimeline() != null)
						{
							if(t.getFieldTimelineID() == f.getTimeSegmentInCluster(c.getID(), 0).getFieldTimelineID())			// Compare cluster time segment to field time segment
								setCurrentFieldTimeSegment(t.getFieldTimelineID(), true);
						}
						else System.out.println("Current Cluster timeline is NULL!:"+c.getID());
					}
				}
				else
				{
					setCurrentFieldTimeSegment(newFieldTimeSegment, true);
					state.movingToTimeSegment = false;
				}

				if(worldState.getTimeMode() == 2 && !state.teleporting)
					p.createTimeCycle();								// Update time cycle for new cluster
			}
			else
			{
				if(debugSettings.viewer) System.out.println("New current cluster is null!");
			}
		}
		else
		{
			if(debugSettings.viewer) System.out.println("New cluster "+newCluster+" is invalid!");
		}
	}

	/**
	 * Set current field timeline segment with option to adjust currentFieldTimelinesSegment
	 * @param newCurrentFieldTimeSegment
	 */
	public boolean setCurrentFieldTimeSegment( int newCurrentFieldTimeSegment, boolean updateTimelinesSegment )
	{
		state.currentFieldTimeSegment = newCurrentFieldTimeSegment;
		p.ml.display.updateCurrentSelectableTimeSegment = true;
		boolean success = true;
		
		if(debugSettings.viewer && debugSettings.detailed) System.out.println("setCurrentFieldTimeSegment()... "+newCurrentFieldTimeSegment+" current state.currentFieldTimeSegmentOnDate:"+state.currentFieldTimeSegmentOnDate+" getLocation().x:"+getLocation().x);
		
		if(updateTimelinesSegment)
		{
			if(state.currentFieldTimeSegment != -1)
			{
				int newFieldDate = currentField.getTimeline().timeline.get(state.currentFieldTimeSegment).getFieldDateID();
				int newFieldTimelinesSegment = currentField.getTimeline().timeline.get(state.currentFieldTimeSegment).getFieldTimelineIDOnDate();
				success = setCurrentTimeSegmentAndDate(newFieldTimelinesSegment, newFieldDate, false);
			}
			else
				success = false;
		}
		
		if(state.currentFieldTimeSegment >= 0 && state.currentFieldTimeSegment < currentField.getTimeline().timeline.size())
			return success;
		else
		{
			if(debugSettings.viewer && debugSettings.detailed)
				System.out.println("Couldn't set newCurrentFieldTimeSegment... currentField.getTimeline().timeline.size():"+currentField.getTimeline().timeline.size());
			return false;
		}
	}

	/**
	 * Set current field timelines segment with option to adjust currentFieldTimelineSegment
	 * @param newCurrentFieldTimeSegmentOnDate
	 * @param updateTimelineSegment Whether to update the current field time segment in date-specific timeline as well
	 * @return True if succeeded
	 */
	public boolean setCurrentFieldTimeSegmentOnDate( int newCurrentFieldTimeSegmentOnDate, boolean updateTimelineSegment )
	{
		if(currentField.getTimelines() != null)
		{
			if(currentField.getTimelines().size() > 0 && currentField.getTimelines().size() > state.currentFieldDate)
			{
				if(debugSettings.viewer && debugSettings.detailed)
					System.out.println("setCurrentFieldTimeSegmentOnDate()... "+newCurrentFieldTimeSegmentOnDate+" currentFieldDate:"+state.currentFieldDate+" currentField.getTimelines().get(currentFieldDate).size():"+currentField.getTimelines().get(state.currentFieldDate).timeline.size()+" getLocation():"+getLocation()+" current field:"+currentField.getID());
			}
			else 
			{
				System.out.println("setCurrentFieldTimeSegmentOnDate() Error.. currentField.getTimelines().size() == "+currentField.getTimelines().size()+" but currentFieldDate == "+state.currentFieldDate+"...");
				return false;
			}
		}
		else
		{
			System.out.println("setCurrentFieldTimeSegmentOnDate() currentField.getTimelines() == null!!!");
			return false;
		}
	
		state.currentFieldTimeSegmentOnDate = newCurrentFieldTimeSegmentOnDate;
		p.ml.display.updateCurrentSelectableTimeSegment = true;

		if(debugSettings.viewer && debugSettings.detailed)
			System.out.println("Set new state.currentFieldTimeSegmentOnDate:"+state.currentFieldTimeSegmentOnDate);

		if(state.currentFieldDate < currentField.getTimelines().size())
		{
			if(currentField.getTimelines().get(state.currentFieldDate).timeline.size() > 0 && state.currentFieldTimeSegmentOnDate < currentField.getTimelines().get(state.currentFieldDate).timeline.size())
			{
				if(updateTimelineSegment)
				{
					int fieldTimelineID = currentField.getTimelines().get(state.currentFieldDate).timeline.get(state.currentFieldTimeSegmentOnDate).getFieldTimelineID();
					boolean success = setCurrentFieldTimeSegment(fieldTimelineID, false);
					return success;
				}
				else return true;
			}
			else
				return false;
		}
		else 
			return false;
	}

	/**
	 * Set current time segment on specified field date
	 * @param newCurrentFieldTimelinesSegment Index of current field time segment in associated timelines array
	 * @param newDate Dateline index of new date
	 * @param updateTimelineSegment Whether to update the current field time segment in main timeline as well
	 * @return
	 */
	public boolean setCurrentTimeSegmentAndDate(int newCurrentFieldTimelinesSegment, int newDate, boolean updateTimelineSegment)
	{
		state.currentFieldDate = newDate;
		boolean success = setCurrentFieldTimeSegmentOnDate( newCurrentFieldTimelinesSegment, updateTimelineSegment );
		if(debugSettings.viewer && debugSettings.detailed)
			System.out.println("setCurrentTimeSegmentAndDate() newCurrentFieldTimelinesSegment:"+newCurrentFieldTimelinesSegment+" newDate:"+newDate+" success? "+success+" getLocation():"+getLocation());
		return success;
	}
	
	public void ignoreTeleportGoal()
	{
		state.ignoreTeleportGoal = true;
	}
	
	/**
	 * Set far viewing distance
	 * @param newFarViewingDistance New far viewing distance
	 */
	public void setFarViewingDistance( float newFarViewingDistance )
	{
		settings.farViewingDistance = newFarViewingDistance;
	}

	/**
	 * Set near clipping distance
	 * @param newFarViewingDistance New near clipping distance
	 */
	public void setNearClippingDistance( float newNearClippingDistance)
	{
		settings.nearClippingDistance = newNearClippingDistance;
		settings.nearViewingDistance = settings.nearClippingDistance * 2.f;
	}
	
	public void resetTimeState()
	{
		state.reset();
	}
	
	public WMV_ViewerState getState()
	{
		return state;
	}

	public WMV_ViewerSettings getSettings()
	{
		return settings;
	}

	/**
	 * @return List of waypoints representing memory path
	 */
	public ArrayList<WMV_Waypoint> getMemoryPath()
	{
		return memory;
	}

	/**
	 * @return List of waypoints representing current path being followed
	 */
	public ArrayList<WMV_Waypoint> getPath()
	{
		return path;
	}

	/**
	 * @return List of waypoints representing current GPS track path
	 */
	public ArrayList<WMV_Waypoint> getGPSTrack()
	{
		return gpsTrack;
	}
	
	public void setFrameCount(int newFrameCount)
	{
		worldState.frameCount = newFrameCount;
	}
	
	/**
	 * @return Whether the viewer is following a path
	 */
	public boolean isFollowing()
	{
		return state.following;
	}

	/**
	 * @return Whether the viewer is walking
	 */
	public boolean isWalking()
	{
		return state.walking;
	}

	/**
	 * @return Whether the viewer is slowing
	 */
	public boolean isSlowing()
	{
		return state.slowing;
	}

	public void setTurningX(boolean newTurningX)
	{
		state.turningX = newTurningX;
	}
	
	public void setTurningY(boolean newTurningY)
	{
		state.turningY = newTurningY;
	}

	public boolean turningX()
	{
		return state.turningX;
	}
	
	public boolean turningY()
	{
		return state.turningY;
	}
	
	/**
	 * @return Whether the viewer is halting
	 */
	public boolean isHalting()
	{
		return state.halting;
	}

	public int getCurrentMedia()
	{
		return state.currentMedia;
	}
	
	public void setCurrentMedia( int newCurrentMedia )
	{
		state.currentMedia = newCurrentMedia;
	}
	
	public int getCurrentMediaStartTime()
	{
		return state.currentMediaStartTime;
	}
	
	public void setCurrentMediaStartTime(int newCurrentMediaStartTime)
	{
		state.currentMediaStartTime = newCurrentMediaStartTime;
	}
	
	public int getNextMediaStartTime()
	{
		return state.nextMediaStartFrame;
	}
	
	public boolean lookAtCurrentMedia()
	{
		return state.lookAtCurrentMedia;
	}
	
	public void setNextMediaStartTime(int newNextMediaStartFrame)
	{
		state.nextMediaStartFrame = newNextMediaStartFrame;
	}
	
	public ArrayList<WMV_TimeSegment>getNearbyClusterTimeline()
	{
		return nearbyClusterTimeline;
	}
	
	public int getNearbyClusterTimelineMediaCount()
	{
		return state.nearbyClusterTimelineMediaCount;
	}
	
	/**
	 * @return Index of current attractor cluster
	 */
	public int getAttractorClusterID()
	{
		return state.attractorCluster;
	}
	
	/**
	 * @return The current attractor cluster
	 */
	public WMV_Cluster getAttractorCluster()
	{
		int attractor = getAttractorClusterID();
		if(attractor >= 0 && attractor < currentField.getClusters().size())
		{
			WMV_Cluster c = currentField.getCluster(attractor);
			return c;
		}
		else return null;
	}

	public WMV_Cluster getAttractorPoint()
	{
		return attractorPoint;
	}

	/**
	 * @return Index of last cluster
	 */
	public int getLastCluster()
	{
		return state.lastCluster;
	}
	
	public boolean getMovementTeleport()
	{
		return state.movementTeleport;
	}

	public boolean getFollowTeleport()
	{
		return state.followTeleport;
	}

	public void setMovementTeleport(boolean newMovementTeleport)
	{
		state.movementTeleport = newMovementTeleport;
	}

	public boolean inSelectionMode()
	{
		return settings.selection;
	}

	public void setSelection(boolean newSelection)
	{
		settings.selection = newSelection;
	}

	public boolean getSegmentSelection()
	{
		return settings.segmentSelection;
	}

	public void setSegmentSelection(boolean newSegmentSelection)
	{
		settings.segmentSelection = newSegmentSelection;
	}

	public boolean getMultiSelection()
	{
		return settings.multiSelection;
	}

	public void setMultiSelection(boolean newMultiSelection)
	{
		settings.multiSelection = newMultiSelection;
	}

	public void setFollowTeleport(boolean newFollowTeleport)
	{
		state.followTeleport = newFollowTeleport;
	}
	
	public int getFollowMode()
	{	
		return state.followMode;
	}
	
	public void setFollowMode(int newFollowMode)
	{
		state.followMode = newFollowMode;
	}
	
	public void setTeleportLength( int newValue )
	{
		settings.teleportLength = newValue;
	}
	
	public void setPathWaitLength( int newValue )
	{
		settings.pathWaitLength = newValue;
	}
	
	public float getVisibleAngle()
	{
		return settings.visibleAngle;
	}
	
	public void setVisibleAngle(float newValue)
	{
		settings.visibleAngle = newValue;
	}
	
	public boolean getAngleFading()
	{
		return settings.angleFading;
	}
	
	public void setAngleFading(boolean newAngleFading)
	{
		settings.angleFading = newAngleFading;
	}
	
	public float getThinningAngle()
	{
		return settings.thinningAngle;
	}
	
	public void setThinningAngle(float newValue)
	{
		settings.thinningAngle = newValue;
	}
	
	public boolean getAngleThinning()
	{
		return settings.angleThinning;
	}

	/**
	 * @return Current near viewing distance
	 */
	public float getNearViewingDistance()
	{
		return settings.nearViewingDistance;
	}

	/**
	 * @return Current near clipping distance
	 */
	public float getNearClippingDistance()
	{
		return settings.nearClippingDistance;
	}
	
	/**
	 * @return Current far viewing distance
	 */
	public float getFarViewingDistance()
	{
		return settings.farViewingDistance;
	}

	public void setAngleThinning(boolean newAngleThinning)
	{
		settings.angleThinning = newAngleThinning;
	}
	
	public void setUserBrightness( float newValue )
	{
		settings.userBrightness = newValue;
	}
	
	public void setFieldOfView( float newFieldOfView )
	{
		settings.fieldOfView = newFieldOfView;
	}
	
	/**
	 * @return Current field of view
	 */
	public float getFieldOfView()
	{
		return settings.fieldOfView = camera.getFov();
	}
	
	/**
	 * @return Initial field of view
	 */
	public float getInitFieldOfView()
	{
		return settings.initFieldOfView;
	}

	/**
	 * @return Current field time segment, i.e. index of current time segment in timeline
	 */
	public int getCurrentFieldTimeSegment()
	{
		return state.currentFieldTimeSegment;
	}

	/**
	 * @return Current field timeline segment on date, i.e. index of current time segment in date-specific timeline 
	 */
	public int getCurrentFieldTimeSegmentOnDate()
	{
		return state.currentFieldTimeSegmentOnDate;
	}
	
	public void startMoveXTransition(int dir)
	{
		stopSlowing();
		state.moveXDirection = dir;
		state.movingX = true;
//		state.slowingX = false;
	}
	
	public void startMoveYTransition(int dir)
	{
		stopSlowing();
		state.moveYDirection = dir;
		state.movingY = true;
//		state.slowingY = false;
	}
	
	public void startMoveZTransition(int dir)
	{
		stopSlowing();
		state.moveZDirection = dir;
		state.movingZ = true;
//		state.slowingZ = false;
	}
	
	public void stopMoveXTransition()
	{
		state.movingX = false;
		state.slowingX = true;
	}
	
	public void stopMoveYTransition()
	{
		state.movingY = false;
		state.slowingY = true;
	}
	
	public void stopMoveZTransition()
	{
		state.movingZ = false;
		state.slowingZ = true;
	}
	
	public void stopRotateXTransition()
	{
		state.rotatingX = false;
	}
	
	public void stopRotateYTransition()
	{
		state.rotatingY = false;
	}
	
	public void stopRotateZTransition()
	{
		state.rotatingZ = false;
	}
	
	/**
	 * Stop immediately
	 */
	public void stopImmediately()
	{
		state.walking = false;										// Whether viewer is walking
		state.velocity = new PVector(0.f, 0.f, 0.f);				// Camera walking acceleration increment
		state.acceleration = new PVector(0.f, 0.f, 0.f);			// Camera walking acceleration increment
		state.walkingAcceleration = new PVector(0.f, 0.f, 0.f);		// Camera walking acceleration increment

		state.slowing = false;			// Whether viewer is slowing 
		state.slowingX = false;			// Slowing X movement
		state.slowingY = false;			// Slowing Y movement
		state.slowingZ = false;			// Slowing Z movement
		state.halting = false;			// Viewer is halting
		
		state.movingX = false;			// Is viewer automatically moving in X dimension (side to side)?
		state.movingY = false;			// Is viewer automatically moving in Y dimension (up or down)?
		state.movingZ = false;			// Is viewer automatically moving in Z dimension (forward or backward)?
		state.movingNearby = false;		// Moving to a powithin nearClusterDistance
		state.waiting = false;			// Whether the camera is waiting to move while following a path
	}
	
	/**
	 * Stop slowing
	 */
	public void stopSlowing()
	{
		state.slowing = false;			// Whether viewer is slowing 
		state.slowingX = false;			// Slowing X movement
		state.slowingY = false;			// Slowing Y movement
		state.slowingZ = false;			// Slowing Z movement
		state.halting = false;			// Viewer is halting
	}
	
	public void zoomIn()
	{
		state.zoomStart = worldState.frameCount;
		state.zoomDirection = -1;
		state.zooming = true;
	}
	
	public void zoomOut()
	{
		state.zoomStart = worldState.frameCount;
		state.zoomDirection = 1;
		state.zooming = true;
	}
	
	public void stopZooming()
	{
		state.zooming = false;
	}
	
	/**
	 * @return Current camera orientation as a directional unit vector
	 */
	public PVector getOrientationVector()
	{
		return state.orientationVector;
	}
	
	/**
	 * @return Index of current cluster
	 */
	public int getCurrentClusterID()
	{
		return state.currentCluster;
	}
	
	/**
	 * @return Whether the viewer is teleporting
	 */
	public boolean isTeleporting()
	{
		return state.teleporting;
	}
	
	/**
	 * @return Whether the viewer is moving to a cluster or attractor
	 */
	public boolean isMoving()
	{
		if(state.movingToCluster || state.movingToAttractor)
			return true;
		else
			return false;
	}

	/**
	 * @return Whether the viewer is moving to an attractor
	 */
	public boolean isMovingToAttractor()
	{
		return state.movingToAttractor;
	}

	/**
	 * @return Whether the viewer is moving to a cluster
	 */
	public boolean isMovingToCluster()
	{
		return state.movingToCluster;
	}

	/**
	 * @return Current field ID
	 */
	public int getField()
	{
		return state.field;
	}
	
	public List<Integer> getClustersVisible()
	{
		return state.clustersVisibleInOrientationMode;
	}
	
	/**
	 * @return Current distance at which a cluster is considered nearby
	 */
	public float getClusterNearDistance()
	{
		return state.clusterNearDistance;
	}
}
