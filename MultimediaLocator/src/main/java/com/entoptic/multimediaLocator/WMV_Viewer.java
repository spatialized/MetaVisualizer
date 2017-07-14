package main.java.com.entoptic.multimediaLocator;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PVector;
import processing.data.FloatList;
import processing.data.IntList;

/*********************************
 * Viewer to navigate and interact with virtual world
 * @author davidgordon
 */
public class WMV_Viewer 
{
	/* Camera */
	private WMV_Camera camera;								// Camera object
	private WMV_Camera mediaViewCamera;								// Camera object
	private WMV_WorldSettings worldSettings;				// World settings
	private WMV_WorldState worldState;						// World state
	private WMV_ViewerSettings settings;					// Viewer settings
	private WMV_ViewerState state;							// Viewer state
	private ML_DebugSettings debug;					// Debug settings

	/* Memory */
	public ArrayList<WMV_Waypoint> memory;					// Path for camera to take
	public ArrayList<WMV_Waypoint> path; 					// Record of camera path

	/* Time */
	public ArrayList<WMV_TimeSegment> visibleClusterTimeline;	// Combined timeline of nearby (visible) clusters

	/* Navigation */
	public WMV_Cluster attractorPoint;						// For navigation to points outside cluster list

	/* GPS Tracks */
	private ArrayList<WMV_Waypoint> gpsTrack;				// Stores a GPS track in virtual coordinates
	private ArrayList<WMV_Waypoint> history;				// Viewer history -- Disabled (?)
	
	WMV_World p;											// Parent world
	
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
		debug = newDebugSettings;
		
		settings = new WMV_ViewerSettings();
		state = new WMV_ViewerState();
		state.clusterNearDistance = worldSettings.clusterCenterSize * state.clusterNearDistanceFactor;

		history = new ArrayList<WMV_Waypoint>();
		gpsTrack = new ArrayList<WMV_Waypoint>();

		memory = new ArrayList<WMV_Waypoint>();
		path = new ArrayList<WMV_Waypoint>();

		visibleClusterTimeline = new ArrayList<WMV_TimeSegment>();
		initialize();
//		initialize(0, 0, 0);
	}

	/** 
	 * Initialize camera at location {0,0,0} with default parameters
	 */
//	public void initialize(float x, float y, float z)
	public void initialize()
	{
		float x, y, z; 
		x = y = z = 0;
		
		float cX, cY, cZ;
		cX = cY = cZ = 0.f;
		
		camera = new WMV_Camera( p.ml, x, y, z, cX, cY, cZ, 0.f, 1.f, 0.f, settings.fieldOfView, 
								 settings.nearClippingDistance, settings.farClippingDistance );
		
//		camera = new WMV_Camera( p.ml, x, y, z, 0.f, 0.f, 0.f, 0.f, 1.f, 0.f, settings.fieldOfView, settings.nearClippingDistance, 10000.f);
		mediaViewCamera = new WMV_Camera( p.ml, 0.f, 0.f, 500.f, 0.f, 0.f, 0.f, 0.f, 1.f, 0.f, (float)Math.PI / 3.f, settings.nearClippingDistance, 10000.f);

		state.location = new PVector(0.f, 0.f, 0.f);
		state.teleportGoal = new PVector(0.f, 0.f, 0.f);		// -- Needed?
		settings.initialize();
		
//		camera = new WMV_Camera( p.ml, settings.initFieldOfView, settings.nearClippingDistance, settings.farClippingDistance );			// Initialize at default location {0,0,0} with default parameters
		
		state.clustersVisibleInOrientationMode = new ArrayList<Integer>();
	}

	/** 
	 * Initialize camera at given location with default parameters
	 * @param x X coord
	 * @param y Y coord 
	 * @param z Z coord
	 */
	public void initializeAtLocation(float x, float y, float z)
	{
		float cX, cY, cZ;
		cX = cY = cZ = 0.f;
		
		camera = new WMV_Camera( p.ml, x, y, z, cX, cY, cZ, 0.f, 1.f, 0.f, settings.fieldOfView, 
								 settings.nearClippingDistance, settings.farClippingDistance );
		
		state.location = new PVector(x, y, z);
		state.teleportGoal = new PVector(x, y, z);		// -- Needed?
		settings.initialize();
		
		state.clustersVisibleInOrientationMode = new ArrayList<Integer>();
	}

	/**
	 * Enter given field
	 * @param fieldID Field to enter
	 */
	public void enterField(int fieldID, boolean setState)
	{
		if(debug.viewer) p.ml.systemMessage("Viewer.enterField()... Field id #"+fieldID);

		setCurrentField(fieldID, setState);					// Set new field and simulation state

		if(p.ml.display.getDisplayView() == 1)
			p.ml.display.map2D.initialize(p);				// Initialize map if in Map View
		
		if(p.ml.display.window.setupNavigationWindow)		// Reload field-specific navigation controls
			p.ml.display.window.reloadNavigationWindow();
		
		if(p.ml.display.getDisplayView() == 2)				// Zoom to timeline for new field
			p.ml.display.zoomToTimeline(p, true);
;
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
		
		updateWalking();										/* Update walking */
		updatePhysics();										/* Update physics */
		
		if(isTeleporting()) updateTeleporting();				/* Update teleporting */
		updateMovement();										/* Update navigation */
		if(state.turningX || state.turningY) updateTurning();		/* Update turning */
		if(state.followingGPSTrack) updateGPSTrackFollowing();	/* Update smooth GPS track following */
		
		if( state.walking && p.ml.frameCount % 15 == 0)			/* Update current cluster while walking */
			updateCurrentCluster(false);						
		
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
			if(p.getCurrentField().getAttractingClusters().size() > 0)		// If being attracted to a point
			{
				if(state.clusterLockIdleFrames > 0) state.clusterLockIdleFrames = 0;											
			}
			else															// If idle
			{
				state.clusterLockIdleFrames++;									// Count frames with no attracting clusters
				if(state.clusterLockIdleFrames > settings.lockToClusterWaitLength)			// If after wait length, lock to nearest cluster
				{
					int nearest = getNearestCluster(true);					// Get nearest cluster location, including current cluster
					WMV_Cluster c = p.getCurrentField().getCluster(nearest);
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
					startFollowingMemory(true);
				}
			}
		}
		
		if(settings.orientationMode) updateOrientationMode();
	}
	
	/**
	 * Move to the given image capture location
	 * @imageID Specified image
	 * @param teleport  Whether to teleport (true) or navigate (false)
	 */
	void moveToImageCaptureLocation(int imageID, boolean teleport) 
	{
		if (debug.viewer)
			p.ml.systemMessage("Moving to capture location... "+imageID);

		PVector newLocation = p.getCurrentFieldImages().get(imageID).getCaptureLocation();
		
		if(teleport)
		{
			teleportToPoint(newLocation, true, true);
		}
		else
		{
			if(debug.viewer && debug.detailed)
				p.ml.systemMessage("moveToCaptureLocation... setting attractor point:"+newLocation);
			setAttractorPoint(newLocation, true, true);
		}
	}
	
	/**
	 * Move to the specified cluster
	 * @param newCluster Destination cluster ID
	 * @param teleport Whether to teleport (true) or move (false)
	 */
	void moveToCluster(int newCluster, boolean teleport) 
	{
		if(teleport)
		{
			teleportToCluster( newCluster, true, -1 ); 
		}
		else
		{
			if(debug.viewer) p.ml.systemMessage("Moving to cluster... setting attractor:"+newCluster);
			setAttractorCluster( newCluster, true );
		}
	}
		
	/**
	 * Go to the nearest cluster
	 * @param teleport  Whether to teleport (true) or move (false)
	 */
	void moveToNearestCluster(boolean teleport) 
	{
		int nearest = getNearestCluster(false);		
		
		if (debug.viewer)
			p.ml.systemMessage("Moving to nearest cluster... "+nearest+" from current:"+state.currentCluster);

		if(settings.teleportToFarClusters && !teleport)
		{
			if( PVector.dist(p.getCurrentField().getCluster(nearest).getLocation(), getLocation()) > settings.farClusterTeleportDistance )
				teleportToCluster(nearest, true, -1);
			else
				setAttractorCluster( nearest, true );
		}
		else
		{
			if(teleport)
				teleportToCluster(nearest, true, -1);
			else
				setAttractorCluster( nearest, true );
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
			if (debug.viewer)
				p.ml.systemMessage("moveToNearestClusterAhead goal:"+ahead);

			if(settings.teleportToFarClusters && !teleport)
			{
				if( PVector.dist(p.getCurrentField().getCluster(ahead).getLocation(), getLocation()) > settings.farClusterTeleportDistance )
					teleportToCluster(ahead, true, -1);
				else
					setAttractorCluster( ahead, true );
			}
			else
			{
				if(teleport)							
					teleportToCluster(ahead, true, -1);
				else
					setAttractorCluster( ahead, true );
			}
		}
		else
		{
			if(debug.viewer)
				p.ml.systemMessage("Viewer.moveToNearestClusterAhead()... can't move to same cluster!... "+ahead);
		}
	}
	

	/**
	 * Move camera to the last cluster
	 * @param teleport  Whether to teleport (true) or move (false)
	 */
	void moveToLastCluster(boolean teleport) 
	{
		if (debug.viewer)
			p.ml.systemMessage("Moving to last cluster... "+state.lastCluster);
		if(state.lastCluster > 0)
		{
			if(settings.teleportToFarClusters && !teleport)
			{
				state.teleportGoalCluster = state.lastCluster;
				PVector newLocation = ((WMV_Cluster) p.getCurrentField().getCluster(state.lastCluster)).getLocation();
				if( PVector.dist(newLocation, getLocation()) > settings.farClusterTeleportDistance )
				{
					teleportToPoint(newLocation, true, true);
				}
				else
				{
					if(debug.viewer) p.ml.systemMessage("moveToLastCluster... setting attractor and currentCluster:"+state.currentCluster);
					setAttractorCluster( state.lastCluster, true );
				}
			}
			else
			{
				if(teleport)
				{
					state.teleportGoalCluster = state.lastCluster;
					PVector newLocation = ((WMV_Cluster) p.getCurrentField().getCluster(state.lastCluster)).getLocation();
					teleportToPoint(newLocation, true, true);
				}
				else
				{
					if(debug.viewer) p.ml.systemMessage("moveToLastCluster... setting attractor and currentCluster:"+state.currentCluster);
					setAttractorCluster( state.lastCluster, true );
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
		
		if(debug.viewer) p.ml.systemMessage("moveToNextCluster()... mediaType "+mediaType);

		/* Find goal cluster */
		if(mediaType == -1)		// Any media type
		{
			if (next >= p.getCurrentField().getClusters().size())
				next = 0;

			while( p.getCurrentField().getCluster(next).isEmpty() || next == state.currentCluster )		// Increment nextCluster until different non-empty cluster found
			{
				next++;
				if (next >= p.getCurrentField().getClusters().size())
				{
					next = 0;
					iterationCount++;

					if(iterationCount > 3) break;
				}

				if(p.getCurrentField().getCluster(next).getState().mediaCount != 0)
					p.ml.systemMessage("Error: Cluster marked empty but mediaPoints != 0!  clusterID:"+next);
			}

			if(iterationCount <= 3)				// If a cluster was found in 2 iterations
			{
				found = true;
				if(debug.viewer) p.ml.systemMessage("Moving to next cluster:"+next+" from current cluster:"+state.currentCluster);
			}
		}
		else 
		{
			result = getNextClusterWithType(p.getCurrentField(), mediaType, state.currentCluster);
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
				setAttractorCluster( next, true );
			}
		}
	}

	/**
	 * Teleport to given point in 3D virtual space
	 * @param dest	Destination point in world coordinates
	 * @param fade  Use fade transition (true) or jump (false)
	 */
	public void teleportToPoint( PVector dest, boolean fade, boolean stopFollowing ) 
	{
		if(debug.viewer)
			p.ml.systemMessage("Viewer.teleportToPoint("+dest+")...");
		
		if(isMoving())
			stop(stopFollowing);
		
		if(settings.orientationMode)
		{
			state.teleportGoal = dest;
			state.location = dest;
		}
		else
		{
			if(fade)
				teleportWithFading(dest, -1, -1);
			else
				jumpTo(dest, true);
		}
	}	
	
	/**
	 * Teleport immediately to given point
	 * @param dest Destination point
	 * @param update Whether to update current cluster
	 */
	public void jumpTo(PVector dest, boolean update)
	{
//		p.ml.systemMessage("Viewer.jumpTeleport()... :"+dest);
		if(state.atCurrentCluster)
		{
			saveCurrentClusterOrientation();
			state.atCurrentCluster = false;
		}
		camera.teleport(dest.x, dest.y, dest.z);
		if(update)
		{
			updateCurrentCluster(true);
//			turnToCurrentClusterOrientation();
		}
	}
	
	/**
	 * Go to the nearest cluster containing specified media type
	 * @param teleport Whether to teleport or move
	 * @param mediaType Media type without which clusters are skipped...  {0: image, 1: panorama, 2: video, 3: sound}
	 * @param inclCurrent Whether to include current cluster
	 */
	public void moveToNearestClusterWithType(int mediaType, boolean inclCurrent, boolean teleport, boolean fade) 
	{
		int nearest = state.currentCluster;
		boolean found = false;
		int result = -1;
		
		if(debug.viewer) p.ml.systemMessage("Viewer.moveToNearestClusterWithType()... mediaType "+mediaType+" inclCurrent:"+inclCurrent);

		/* Find goal cluster */
		if(mediaType < 0 || mediaType > 3)		// Incorrect media type
			return;
		else 
		{
			result = getNearestClusterWithType(p.getCurrentField(), mediaType, inclCurrent);	// Find nearest cluster with given media type
			if(result >= 0 && result < p.getCurrentField().getClusters().size())
			{
				found = true;
				nearest = result;
				if(debug.viewer) p.ml.systemMessage("Viewer.moveToNearestClusterWithType()... found media #:"+nearest);
			}
		}
		
		if(found)				/* If cluster with specified media type was found */
		{
			if(teleport)		/* Teleport or move */
			{
				teleportToCluster(nearest, fade, -1);
			}
			else
			{
				if(isTeleporting()) state.teleporting = false;
				setAttractorCluster( nearest, true );
			}
		}
		else					/* If no cluster with given media type found */
		{
			String strMediaType = "";
			switch(mediaType)			// 0: image, 1: panorama, 2: video, 3: sound
			{
				case 0:
					strMediaType = "images";
					break;
				case 1:
					strMediaType = "panoramas";
					break;
				case 2:
					strMediaType = "videos";
					break;
				case 3:
					strMediaType = "sounds";
					break;
			}
			if(debug.viewer)
				p.ml.systemMessage("No clusters with "+strMediaType+" found... result:"+result);
			if(p.getSettings().screenMessagesOn)
				p.ml.display.message(p.ml, "No clusters with "+strMediaType+" found...");
		}
	}

	/**
	 * Find cluster in field contains at least one object of given media type
	 * @param currentField Field to search
	 * @param mediaType Media type to look for
	 * @param inclCurrent Whether to include current cluster
	 * @return Whether field contains given media type
	 */
	private int getNearestClusterWithType(WMV_Field currentField, int mediaType, boolean inclCurrent)
	{
		int result = -1;
		int id = -1;
		
		switch(mediaType)
		{
			case 0:
				id = getNearestImage(inclCurrent);
				if(id >= 0 && id < currentField.getImageCount())
					result = currentField.getImage(id).getAssociatedClusterID();
				break;
			case 1:
				id = getNearestPanorama(inclCurrent);
				if(id >= 0 && id < currentField.getPanoramaCount())
					result = currentField.getPanorama(id).getAssociatedClusterID();
				
//				p.ml.systemMessage("Viewer.getNearestClusterWithType()... type Panorama... found id#"+id+" in cluster #"+result);
//				p.ml.systemMessage("	has pano?  "+currentField.getCluster(result).hasPanorama()+"	is pano in cluster?  "+currentField.getCluster(result).getPanoramas(currentField.getPanoramas()).contains(id));
				
				break;
			case 2:
				id = getNearestVideo(inclCurrent);
				if(id >= 0 && id < currentField.getVideoCount())
					result = currentField.getVideo(id).getAssociatedClusterID();
				break;
			case 3:
				id = getNearestSound(inclCurrent);
				if(id >= 0 && id < currentField.getSoundCount())
					result = currentField.getSound(id).getAssociatedClusterID();
				break;
		}

		if(result == -1)
		{
			if(debug.viewer) p.ml.systemMessage("Viewer.getNearestClusterWithType()... No media of type "+mediaType+" found...");
			return -1;
		}
		else
			return result;
	}

	/**
	 * Find cluster in field contains at least one object of given media type  -- Should look nearby, not numerically
	 * @param currentField Field to search
	 * @param mediaType Media type to look for
	 * @param startClusterID Starting cluster ID to search from
	 * @return Whether field contains given media type
	 */
	public int getNextClusterWithType(WMV_Field currentField, int mediaType, int startClusterID)
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
					if(debug.viewer)
						p.ml.systemMessage("1  No media found...");
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
			
			if(result)			/* If cluster with specified media type has been found */
			{
				end = true;
			}
			else				/* If not found */
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
								if(debug.viewer)
									p.ml.systemMessage("2 No media found...");
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
			if(debug.viewer) p.ml.systemMessage("Moving to next cluster with media type:"+mediaType+" cluster found:"+next+"... moving from current cluster:"+state.currentCluster);
			return next;
		}
		else
		{
			if(debug.viewer) p.ml.systemMessage("No media of type "+mediaType+" found...");
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
			if(debug.viewer && debug.detailed)
				p.ml.systemMessage("Viewer.moveToTimeSegmentInField()... fieldID:"+fieldID+" fieldTimeSegment:"+fieldTimeSegment+" fieldTimelineID:"+f.getTimeline().timeline.get(fieldTimeSegment).getFieldTimelineID()+" f.getTimeline().size():"+f.getTimeline().timeline.size());
			int clusterID = f.getTimeline().timeline.get(fieldTimeSegment).getClusterID();
			
			if(debug.viewer)
				p.ml.systemMessage("Viewer.moveToTimeSegmentInField()...  Found clusterID:"+clusterID+" p.getCurrentField() cluster count:"+p.getCurrentField().getClusters().size());

			if(clusterID >= 0)
			{
				if(clusterID == state.currentCluster && p.getCurrentField().getCluster(clusterID).getClusterDistance() < worldSettings.clusterCenterSize)	// Moving to different time in same cluster
				{
					setCurrentFieldTimeSegment(fieldTimeSegment, true);
					if(debug.viewer && debug.detailed)
						p.ml.systemMessage("Viewer.moveToTimeSegmentInField()... Advanced to time segment "+fieldTimeSegment+" in same cluster... ");
				}
				else
				{
					state.movingToTimeSegment = true;								// Set time segment target
					state.timeSegmentTarget = fieldTimeSegment;

					if(settings.teleportToFarClusters && !teleport)
					{
						if(clusterID < p.getCurrentField().getClusters().size())
						{
							if( PVector.dist(p.getCurrentField().getCluster(clusterID).getLocation(), getLocation()) > settings.farClusterTeleportDistance )
								teleportToCluster(clusterID, fade, fieldTimeSegment);
							else
								setAttractorCluster( clusterID, true );
						} 
						else if(debug.viewer)
							p.ml.systemMessage("Viewer.moveToTimeSegmentInField()... Error! clusterID >= p.getCurrentField().getClusters().size()! clusterID:"+clusterID+" p.getCurrentField() cluster count:"+p.getCurrentField().getClusters().size());
					}
					else
					{
						if(teleport)
							teleportToCluster(clusterID, fade, fieldTimeSegment);
						else
							setAttractorCluster( clusterID, true );
					}
				}
			}
			else
			{
				if(debug.viewer)
					p.ml.systemMessage("Viewer.moveToTimeSegmentInField()... fieldTimeSegment in field #"+f.getID()+" cluster is "+clusterID+"!! Will move to cluster 0...");
				teleportToCluster(0, fade, 0);
			}
		}
		else
		{
			if(debug.viewer)
				p.ml.systemMessage("Viewer.moveToTimeSegmentInField()... timeline is empty!");
		}
	}
	
	/**
	 * Move to cluster selected on map
	 * @param clusterID Cluster ID
	 * @param stayInMapView Whether to stay in Map View (true) or switch to World View (false)
	 */
	public void moveToClusterOnMap( int clusterID, boolean stayInMapView )
	{
		if(debug.viewer || debug.map)
			p.ml.systemMessage("Viewer.moveToClusterOnMap()... Moving to cluster on map:"+clusterID);

		if(stayInMapView)
		{
			teleportToCluster(clusterID, false, -1);
		}
		else
		{
			teleportToCluster(clusterID, true, -1);
			p.ml.display.setDisplayView( p, 0 );
		}
		
		if(p.ml.display.map2D.getSelectedClusterID() != clusterID) 
			p.ml.display.map2D.setSelectedClusterID(clusterID);
	}

	/**
	 * Move to given point
	 * @param goalPoint Goal point
	 * @param teleport Whether to teleport (true) or move smoothly (false)
	 */
	public void moveToPoint(PVector goalPoint, boolean teleport)
	{
		if(debug.viewer)
			p.ml.systemMessage("Viewer.moveToPoint()... x:"+goalPoint.x+" y:"+goalPoint.y+" z:"+goalPoint.z);

		if(teleport)
			teleportToPoint(goalPoint, true, true);
		else
			setAttractorPoint(goalPoint, true, true);						
	}

	/**
	 * Move to first point on path
	 */
	public void moveToFirstPathPoint()
	{
		boolean teleport = ( PVector.dist( state.pathGoal, getLocation() ) > settings.farClusterTeleportDistance );
		
		if(debug.viewer)
			p.ml.systemMessage("Viewer.moveToFirstPathPoint()... x:"+state.pathGoal.x+" y:"+state.pathGoal.y+" z:"+state.pathGoal.z+" teleport? "+teleport);

		if(teleport)
			teleportToPoint(state.pathGoal, true, false);
		else
			setPathAttractorPoint(state.pathGoal, true);								// Set attractor point from path goal
	}

	/**
	 * Move to first point in GPS track
	 */
	public void moveToFirstGPSTrackPoint()
	{
		boolean teleport = ( PVector.dist( state.gpsTrackGoal, getLocation() ) > settings.farClusterTeleportDistance );
		
		if(debug.viewer)
			p.ml.systemMessage("Viewer.moveToFirstPathPoint()... x:"+state.gpsTrackGoal.x+" y:"+state.gpsTrackGoal.y+" z:"+state.gpsTrackGoal.z+" teleport? "+teleport);

		if(teleport)
			teleportToPoint(state.gpsTrackGoal, true, false);
		else
			setPathAttractorPoint(state.gpsTrackGoal, true);								// Set attractor point from path goal
	}

	/**
	 * Move to given point
	 * @param goalPoint
	 * @param teleport
	 */
//	public void moveToPathPoint(PVector goalPoint)
//	{
//		if(debug.viewer) p.ml.systemMessage("Viewer.moveToPathPoint()... x:"+goalPoint.x+" y:"+goalPoint.y+" z:"+goalPoint.z);
//
//		setPathAttractorPoint(goalPoint, false);									// Set attractor point from path goal
//	}

	/**
	 * Move to given path point ID
	 * @param id Path point ID
	 * @param teleport Whether to teleport (true) or move smoothly (false)
	 */
	public void moveToPathPoint(int id)
	{
		if(id > 0 && id < path.size())
		{
			boolean teleport = ( PVector.dist(state.pathGoal, getLocation()) > settings.farClusterTeleportDistance );
			PVector goalPoint = path.get(id).getWorldLocation();

			if(debug.viewer)
				p.ml.systemMessage("Viewer.moveToPathPoint()... x:"+goalPoint.x+" y:"+goalPoint.y+" z:"+goalPoint.z);

			if(teleport)
				teleportToPoint(goalPoint, true, true);
			else
				setPathAttractorPoint(goalPoint, false);									// Set attractor point from path goal
		}
		else
		{
			p.ml.systemMessage("Viewer.moveToPathPoint()... ERROR: invalid point id:"+id+" path.size():"+path.size());
		}
	}

	/**
	 * Move viewer to waypoint
	 * @param waypoint Destination waypoint
	 * @param fade Whether to fade, if teleporting
	 * @param stop Whether to slow down and stop at waypoint
	 */
	public void moveToWaypoint(WMV_Waypoint waypoint, boolean teleport)
	{
		if(debug.viewer) p.ml.systemMessage("Viewer.moveToWaypoint()... x:"+waypoint.getWorldLocation().x+" y:"+waypoint.getWorldLocation().y+" z:"+waypoint.getWorldLocation().z);

		PVector goalPoint = waypoint.getWorldLocation();
		
//		if(stop)
		moveToPoint(goalPoint, teleport);
//		else
//			moveToPathPoint(goalPoint);
	}
	/**
	 * Move viewer to cluster corresponding to one time segment later on timeline
	 * @param currentDate Whether to consider only segments on current date
	 * @param newCluster Whether to force moving to a different cluster -- NEED TO IMPLEMENT
	 * @param teleport Whether to teleport or move
	 * @param fade Whether to fade or jump when teleporting
	 */
	public void moveToNextTimeSegment(boolean currentDate, boolean newCluster, boolean teleport, boolean fade)
	{
		chooseNextTimeSegment(currentDate);
		moveToTimeSegmentInField(getCurrentFieldID(), state.currentFieldTimeSegment, teleport, fade);
	}
	

	/**
	 * Teleport viewer to the given cluster ID
	 * @param dest Destination cluster ID
	 * @param fade Whether to fade smoothly (true) or jump (false)
	 * @param fieldTimeSegment Goal time segment in destination cluster
	 */
	public void teleportToCluster( int dest, boolean fade, int fieldTimeSegment ) 
	{
		if(debug.viewer && debug.detailed)
			p.ml.systemMessage("Viewer.teleportToCluster()... dest:"+dest+" fade:"+fade);
		
		if(!isTeleporting() && !isMoving())
		{
			if(dest >= 0 && dest < p.getCurrentField().getClusters().size())
			{
				WMV_Cluster c = p.getCurrentField().getCluster(dest);

				if(fade)
				{
					teleportWithFading(c.getLocation(), c.getID(), -1);
				}
				else
				{
					setLocation( c.getLocation(), false );
					setCurrentCluster(dest, fieldTimeSegment);
					if(p.state.waitingToFadeInTerrainAlpha) 
						p.fadeInTerrain(false);
				}
			}
			else 
				p.ml.systemMessage("ERROR: Can't teleport to cluster:"+dest+"... clusters.size() =="+p.getCurrentField().getClusters().size());
		}
	}

	/**
	 * Teleport to field by offset from current
	 * @param offset Field index offset
	 * @param moveToFirstTimeSegment Whether to move to first time segment in field
	 * @param fade Whether to fade smoothly or jump
	 */
	public void teleportToFieldOffset(int offset, boolean fade) 
	{
		if(p.getFieldCount() > 1)
			teleportToField(getCurrentFieldID() + offset, fade);
	}
	
	/**
	 * Teleport to given field ID
	 * @param newField Field ID
	 * @param moveToFirstTimeSegment Whether to move to first time segment in field
	 * @param fade Whether to fade smoothly or jump
	 */
	public void teleportToField(int newField, boolean fade) 
	{
		if(debug.viewer)
			p.ml.systemMessage("Viewer.teleportToField()... newField:"+newField+" fade:"+fade);
		if(newField >= 0)
		{
			p.stopAllVideos();									/* Stop currently playing videos */
			p.stopAllSounds();									/* Stop currently playing sounds */
			
			if(newField >= p.getFieldCount()) newField = 0;
			
			if(debug.viewer)
				p.ml.systemMessage("teleportToField()... newField: "+newField+" out of "+p.getFieldCount());

			if(p.getField(newField).getClusters().size() > 0)
			{
				WMV_Waypoint entry = p.getField(newField).getState().entryLocation;
				boolean hasEntryPoint = false;
				
				if(entry != null)
					hasEntryPoint = entry.initialized();

				if(hasEntryPoint)
				{
//					state.teleportGoalCluster = -1;
					state.teleportGoalCluster = entry.getClusterID();
//					here
					if(debug.viewer)
						p.ml.systemMessage("teleportToField()... Found entry point... will set Current Cluster to "+state.teleportGoalCluster);
				}
				else
				{
					WMV_TimeSegment goalSegment = p.getField(newField).getTimeline().getLower();
					if(goalSegment != null)
						state.teleportGoalCluster = goalSegment.getClusterID();
					else
						p.ml.systemMessage("teleportToField()... p.getField("+newField+").getTimeline().getLower() returns null!!");
				}

				if(fade)
				{
					if(state.teleportGoalCluster >= 0 && state.teleportGoalCluster < p.getField(newField).getClusters().size())
						state.teleportGoal = p.getField(newField).getCluster(state.teleportGoalCluster).getLocation();	 // Set goal cluster 
					else
						if(debug.viewer) p.ml.systemMessage("Invalid goal cluster! "+state.teleportGoalCluster+" field clusters.size():"+p.getField(newField).getClusters().size());
					
					if(debug.viewer) p.ml.systemMessage("  teleportToField()...  Teleported to field "+state.teleportToField+" at state.teleportGoal:"+state.teleportGoal);
					
					if(p.getSettings().screenMessagesOn) 
						p.ml.display.message(p.ml, "Moving to "+p.getField(newField).getName());
					
					teleportWithFading(null, -1, newField);
				}
				else
				{
					if(p.getSettings().screenMessagesOn) 
						p.ml.display.message(p.ml, "Moving to "+p.getField(newField).getName()+" will set state? "+p.getField(newField).hasBeenVisited());

					enterField(newField, p.getField(newField).hasBeenVisited()); 						/* Enter new field */

					if(debug.viewer) 
						p.ml.systemMessage("Viewer.teleportToField()...  Entered field #"+newField);

					if(hasEntryPoint)
					{
						if(debug.viewer) 
							p.ml.systemMessage("Viewer.teleportToField()...  Field has Entry Point... "+p.getField(newField).getState().entryLocation.getWorldLocation());
						moveToWaypoint( p.getField(newField).getState().entryLocation, false );	 // Move to waypoint and stop				
					}
					else
					{
						if(debug.viewer) 
							p.ml.systemMessage("Viewer.teleportToField()...  No Entry Point found...");
						moveToFirstTimeSegment(false);					// Move to first time segment if start location not set from saved data 
					}

					if(p.state.displayTerrain)
						p.state.waitingToFadeInTerrainAlpha = true;
				}
			}
		}
	}

	/**
	 * Teleport by fading: fade out visible media, move to goal, then fade in media in visible range
	 * @param newField Goal field ID; value of -1 indicates to stay in current field
	 */
	private void teleportWithFading(PVector newLocation, int goalClusterID, int newField) 
	{
//		if(worldState.frameCount >= state.teleportStart + settings.teleportLength)		// If the teleport has finished

		state.teleportGoalCluster = goalClusterID;
		if(newLocation != null)
			state.teleportGoal = newLocation;

		p.fadeOutAllMedia();

		if(state.atCurrentCluster)
		{
			saveCurrentClusterOrientation();
			state.atCurrentCluster = false;
		}

		state.teleporting = true;
		state.teleportStart = worldState.frameCount;
		state.teleportWaitingCount = 0;
		
		if(newField != -1)
			state.teleportToField = newField;
		
		if(p.state.displayTerrain)						// Added 7-3-17
			p.state.waitingToFadeInTerrainAlpha = true;
	}

	/**
	 * Choose next field time segment 
	 * @param currentDate Whether to consider only segments on current date
	 */
	private void chooseNextTimeSegment(boolean currentDate)
	{
		if(currentDate)
		{
			int newValue = state.currentFieldTimeSegmentOnDate+1;
			if(state.currentFieldDate >= p.getCurrentField().getTimelines().size())								// Past dateline end
			{
				state.currentFieldDate = 0;
				state.currentFieldTimeSegmentOnDate = 0;
				p.ml.systemMessage( "--> Current field date reset! currentFieldDate was greater than timelines.size(): "
									+ p.getCurrentField().getTimelines().size()+"  dateline.size(): "+p.getCurrentField().getDateline().size() );
			}
			else
			{
				if(newValue >= p.getCurrentField().getTimelines().get(state.currentFieldDate).timeline.size()) 	// Reached end of day
				{
					if(debug.viewer) p.ml.systemMessage("Reached end of day...");
					state.currentFieldDate++;
					if(state.currentFieldDate >= p.getCurrentField().getDateline().size()) 
					{
						if(debug.viewer) p.ml.systemMessage("Reached end of year...");
						state.currentFieldDate = 0;
						setCurrentFieldTimeSegmentOnDate(0, true);												// Return to first segment
					}
					else
					{
						while(p.getCurrentField().getTimelines().get(state.currentFieldDate).timeline.size() == 0)		// Go to next non-empty date
						{
							state.currentFieldDate++;
							if(state.currentFieldDate >= p.getCurrentField().getDateline().size())
								state.currentFieldDate = 0;
						}
						if(debug.viewer) p.ml.systemMessage("Moved to next date: "+state.currentFieldDate);
						setCurrentFieldTimeSegmentOnDate(0, true);												// Start at first segment
					}
				}
				else
					setCurrentFieldTimeSegmentOnDate(newValue, true);
			}
		}
		else
		{
			setCurrentFieldTimeSegment(state.currentFieldTimeSegment+1, true);
			if(state.currentFieldTimeSegment >= p.getCurrentField().getTimeline().timeline.size())
				setCurrentFieldTimeSegment(0, true);									// Return to first segment
		}
	}
	
	/**
	 * Move to cluster corresponding to one time segment earlier on timeline
	 * @param currentDate Whether to look only at time segments on current date
	 * @param newCluster Whether to force moving to a different cluster -- NEED TO IMPLEMENT
	 * @param teleport Whether to teleport or move
	 * @param fade Whether to fade or jump when teleporting
	 */
	public void moveToPreviousTimeSegment(boolean currentDate, boolean newCluster, boolean teleport, boolean fade)
	{
		choosePreviousTimeSegment(currentDate);
		moveToTimeSegmentInField(getCurrentFieldID(), state.currentFieldTimeSegment, teleport, fade);
	}
	
	/**
	 * Choose previous field time segment
	 * @param currentDate Whether to consider only segments on current date
	 */
	private void choosePreviousTimeSegment(boolean currentDate)
	{
		if(currentDate)
		{
			int newValue = state.currentFieldTimeSegmentOnDate-1;
			if(state.currentFieldDate >= p.getCurrentField().getTimelines().size())
			{
				state.currentFieldDate = 0;
				state.currentFieldTimeSegmentOnDate = 0;
				p.ml.systemMessage("--> Current field date reset!... was greater than timelines.size(): "
								+p.getCurrentField().getTimelines().size()+"  dateline.size(): "+p.getCurrentField().getDateline().size());
			}
			else
			{
				if(newValue < 0) 															// Reached beginning of day
				{
					state.currentFieldDate--;
					if(state.currentFieldDate < 0) 
					{
						state.currentFieldDate = p.getCurrentField().getDateline().size()-1;			// Go to last date
						setCurrentFieldTimeSegmentOnDate(p.getCurrentField().getTimelines().get(state.currentFieldDate).timeline.size()-1, true);		// Go to last segment
					}
					else
					{
						setCurrentFieldTimeSegmentOnDate(p.getCurrentField().getTimelines().get(state.currentFieldDate).timeline.size()-1, true);		// Start at last segment
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
				setCurrentFieldTimeSegment(p.getCurrentField().getTimeline().timeline.size()-1, true);
		}
	}

	/**
	 * Rotate smoothly around X axis to specified angle
	 * @param angle Angle around X axis to rotate to
	 */
	public void turnXToAngle(float angle, int turnDirection)
	{
		if(debug.viewer) p.ml.systemMessage("Viewer.turnXToAngle()... angle:"+angle);
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
		if(debug.viewer) p.ml.systemMessage("ViewerturnYToAngle()... angle:"+angle);

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

			if(debug.viewer && debug.detailed) p.ml.systemMessage("turnXStartFrame:"+state.turnXStartFrame+" turnXTargetFrame:"+state.turnXTargetFrame+" turnXDirection:"+state.turnXDirection);
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
	private void lookAtMedia( int id, int mediaType ) 
	{
		PVector turnLoc = new PVector(0,0,0);
		
		if(debug.viewer)
			p.ml.systemMessage("Looking at media:"+id+" mediaType:"+mediaType);

		switch(mediaType)
		{
			case 0:			// Image
				turnLoc = p.getCurrentField().getImage(id).getLocation();
				break;
			case 1:			// Panorama		-- Turn towards "center"?
//				turnLoc = currentField.getImage(id).getLocation();
				break;
			case 2:			// Video
				turnLoc = p.getCurrentField().getVideo(id).getLocation();
				break;
			case 3:			// Sound		-- Turn towards??
//				turnLoc = currentField.sounds.get(id).getLocation();
				break;
		}
		
		state.turningMediaGoal = new PVector(id, mediaType);
		turnTowards(turnLoc);
	}
	
	/**
	 * Turn viewer smoothly to given X direction (yaw) and Y elevation (pitch)
	 * @param direction
	 * @param elevation
	 */
	private void turnToOrientation( WMV_Orientation newOrientation )
	{
		turnXToAngle(newOrientation.getDirection(), 0);		// Calculate which way to turn and start turning in X axis
		turnYToAngle(newOrientation.getElevation(), 0);		// Calculate which way to turn and start turning in Y axis
	}
	
	/**
	 * Turn smoothly towards given point
	 * @param goal Point to smoothly turn towards
	 */
	private void turnTowards( PVector goal ) 
	{
		if(debug.viewer) 
			p.ml.systemMessage("Turning towards... goal.x:"+goal.x+" goal.y:"+goal.y+" goal.z:"+goal.z);

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
	 * Move to nearest cluster that meets minimum media count threshold
	 * @param minTimelinePoints Minimum points in timeline of cluster to move to
	 * @param teleport 
	 */
	void moveToNearestClusterWithTimes(int minTimelinePoints, boolean teleport)
	{
		int nextCluster;
		
		nextCluster = state.currentCluster + 1;
		if(nextCluster >= p.getCurrentField().getClusters().size())
			nextCluster = 0;
		int count = 0;
		boolean found = false;
		while(p.getCurrentField().getCluster(nextCluster).getTimeline().timeline.size() < 2)
		{
			nextCluster++;
			count++;
			if(nextCluster >= p.getCurrentField().getClusters().size())
				nextCluster = 0;
			if(count >= p.getCurrentField().getClusters().size())
				break;
		}

		if(count < p.getCurrentField().getClusters().size())
			found = true;

		if(debug.viewer && debug.detailed)
			p.ml.systemMessage("Viewer.moveToNearestClusterWithTimes... setting attractor:"+p.getCurrentField().getTimeline().timeline.get(nextCluster).getFieldTimelineID());

		if(found)
		{
			if(settings.teleportToFarClusters && !teleport)
			{
				if( PVector.dist(p.getCurrentField().getCluster(nextCluster).getLocation(), getLocation()) > settings.farClusterTeleportDistance )
					teleportToCluster(nextCluster, true, -1);
				else
					setAttractorCluster( nextCluster, true );
			}
			else
			{
				if(teleport)
					teleportToCluster(p.getCurrentField().getCluster(nextCluster).getID(), true, -1);
				else
					setAttractorCluster( p.getCurrentField().getCluster(nextCluster).getID(), true );
			}
		}
	}
	
	/**
	 * @param teleport Teleport (true) or navigate (false) to cluster?
	 * Send the camera to a random cluster in the field
	 */
	public void moveToRandomCluster(boolean teleport, boolean fade)
	{
		boolean failed = false;
		int count = 0;
		int rndClusterID = (int) p.ml.random(p.getCurrentField().getClusters().size());
		while(p.getCurrentField().getCluster(rndClusterID).isEmpty() || rndClusterID == state.currentCluster)
		{
			rndClusterID = (int) p.ml.random(p.getCurrentField().getClusters().size());
			count++;
			
			if(count > p.getCurrentField().getClusters().size() * 2)
			{
				failed = true;
				break;
			}
		}

		if(!failed)
		{
			if(settings.teleportToFarClusters && !teleport)
			{
				if( PVector.dist(p.getCurrentField().getCluster(rndClusterID).getLocation(), getLocation()) > settings.farClusterTeleportDistance )
					teleportToCluster(rndClusterID, fade, -1);
				else
					setAttractorCluster( rndClusterID, true );
			}
			else
			{
				if(teleport)
					teleportToCluster(rndClusterID, fade, -1);
				else
					setAttractorCluster( rndClusterID, true );
			}
		}
	}
	
	/**
	 * Start walking forward
	 */
	public void walkForward()
	{
		startMoveZTransition(-1);
	}

	/**
	 * Start walking backward
	 */
	public void walkBackward()
	{
		startMoveZTransition(1);
	}

	/**
	 * Start walking up
	 */
	public void walkUp()
	{
		startMoveYTransition(-1);
	}
	
	/**
	 * Start walking down
	 */
	public void walkDown()
	{
		startMoveYTransition(1);
	}
	
	/**
	 * Start sidestepping left
	 */
	public void sidestepLeft()
	{
		startMoveXTransition(-1);
	}
	
	/**
	 * Start sidestepping right
	 */
	public void sidestepRight()
	{
		startMoveXTransition(1);
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
	 * Stop any movement, turning, zooming and teleporting behaviors
	 * @param clearAttractors Whether to clear attractor(s)
	 */
	public void stop( boolean stopFollowing )
	{
		stopTurningTransitions();					// Stop turning
		stopMoving(true);							// Stop moving
		stopZooming();								// Stop zooming
		stopTeleporting();							// Stop teleporting
		if(stopFollowing && state.following)
			stopFollowing();							// Stop following path
		if(stopFollowing && state.followingGPSTrack) 
			stopFollowingGPSTrack();					// Stop following GPS track path
	}
	
	/**
	 * Stop moving
	 * @param clearAttractors Whether to clear attractor(s)
	 */
	public void stopMoving(boolean clearAttractors)
	{
		if(debug.viewer)
			p.ml.systemMessage("Viewer.stopMoving()... clearAttractors:"+clearAttractors);

		stopMovementTransitions();			// Stop moving
		setMovementVectorsToZero();			// Set speed, acceleration to zero
		if(clearAttractors)
		{
			p.getCurrentField().clearAllAttractors();	// Clear all current attractor(s)
			clearAttractor();
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
	 * Stop turning along X axis
	 */
	public void stopTurningX()
	{
		state.turningX = false;
		state.turnSlowingX = false;
		state.turnHaltingX = false;
		state.turningVelocity.x = 0.f;			
	}
	
	/**
	 * Stop turning along Y axis
	 */
	public void stopTurningY()
	{
		state.turningY = false;
		state.turnSlowingY = false;
		state.turnHaltingY = false;
		state.turningVelocity.y = 0.f;			
	}

	/**
	 * Stop rotating and/or turning
	 */
	private void stopTurningTransitions()
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
	 * Stop teleporting
	 */
	private void stopTeleporting()
	{
		state.teleporting = false;
		state.teleportGoalCluster = -1;
	}
	
	/**
	 * Set attraction, acceleration and velocity to zero
	 */
	private void setMovementVectorsToZero()
	{
		state.attraction = new PVector(0,0,0);						
		state.acceleration = new PVector(0,0,0);							
		state.velocity = new PVector(0,0,0);							
		state.walkingAcceleration = new PVector(0,0,0);					
		state.walkingVelocity = new PVector(0,0,0);	
	}
	
	private void stopMovementTransitions()
	{
		if(state.walking) state.walking = false;
		if(state.slowing) state.slowing = false;
		if(state.slowingX) state.slowingX = false;
		if(state.slowingY) state.slowingY = false;
		if(state.slowingZ) state.slowingZ = false;
		if(state.halting) state.halting = false;

		if(state.movingX) state.movingX = false;
		if(state.movingY) state.movingY = false;
		if(state.movingZ) state.movingZ = false;
		if(state.movingNearby) state.movingNearby = false;		// Moving to a point within nearClusterDistance
		if(state.movingToAttractor) state.movingToAttractor = false;
		if(state.movingToCluster) state.movingToCluster = false;
		if(state.zooming) state.zooming = false;

		if(state.waiting) state.waiting = false;
		if(state.teleporting) state.teleporting = false;
	}
	
	/**
	 * Get current viewer location as waypoint
	 * @return Current viewer location as waypoint
	 */
	public WMV_Waypoint getLocationAsWaypoint()
	{
		WMV_Waypoint current;
		WMV_Cluster currentCluster = p.getCurrentCluster();
		int clusterID = -1;
		
		if( currentCluster.getViewerDistance() < p.settings.clusterCenterSize )
			clusterID = currentCluster.getID();
		
		current = new WMV_Waypoint(0, clusterID, -1, getLocation(), getGPSLocation(), getAltitude(), null );	// -- Should set time
		return current;
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
	 * Clear attractor cluster
	 */
	public void clearAttractorCluster()
	{
		state.attractorCluster = -1;											// Set attractorCluster
		state.movingToCluster = false;
		state.movingToAttractor = false;
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
		state.currentFieldTimeSegment = 0;			// Current time segment in field timeline
		
		/* Memory */
		state.movingToAttractor = false;			// Moving to attractor poanywhere in field
		state.movingToCluster = false;				// Moving to cluster 
		state.following = false;					// Is the camera currently navigating from memory?
		
		/* Clusters */
		state.currentCluster = 0;				// Cluster currently in view
		state.lastCluster = -1;					// Last cluster visited
		state.attractorCluster = -1;				// Cluster attracting the camera
		state.attractionStart = 0;				// Attraction start frame
//		state.continueAtAttractor = false;		// Continue at attractor
		state.pathWaiting = false;				// Path waiting
		
		state.teleportGoalCluster = -1;			// Cluster to navigate to (-1 == none)
		state.clusterNearDistanceFactor = 2.f;	// Multiplier for clusterCenterSize to get clusterNearDistance
		
		/* Teleporting */
		state.navigationTeleport = false;		// Teleport when following navigation commands
		state.teleporting = false;			// Transition where all images fade in or out
		state.teleportToField = -1;			// What field ID to fade transition to	 (-1 remains in current field)
		state.teleportWaitingCount = 0;		// How long has the viewer been waiting for media to fade out before teleport?
		
		/* Movement */
		setPathNavigationMode( 0 );				// { 0: Timeline 1: GPS Track 2: Memory }
		state.walking = false;			// Whether viewer is walking

		state.slowing = false;			// Whether viewer is slowing 
		state.slowingX = false;			// Slowing X movement
		state.slowingY = false;			// Slowing Y movement
		state.slowingZ = false;			// Slowing Z movement
		state.halting = false;			// Viewer is halting
		
		state.movingX = false;			// Whether viewer is automatically moving in X dimension (side to side)
		state.movingY = false;			// Whether viewer is automatically moving in Y dimension (up or down)
		state.movingZ = false;			// Whether viewer is automatically moving in Z dimension (forward or backward)
		state.movingNearby = false;		// Moving to center from a point less than nearClusterDistance but greater than clusterCenterSize
		state.centering = false;		// Moving to precise center from a point less than clusterCenterSize
		state.waiting = false;			// Whether viewer is waiting to move while following a path
		
		/* Turning */
		state.turningX = false;			// Whether the viewer is turning (right or left)
		state.turningY = false;			// Whether the viewer is turning (up or down)
		
		state.rotatingX = false;		// Whether the camera is rotating in X dimension (turning left or right)
		state.rotatingY = false;		// Whether the camera is rotating in Y dimension (turning up or down)
		state.rotatingZ = false;		// Whether the camera is rotating in Z dimension (rolling left or right)

		/* Interaction */
		state.lastMovementFrame = 500000; 
		state.lastLookFrame = 500000;
		state.clusterLockIdleFrames = 0;	// How long to wait after user input before auto navigation moves the camera?

		/* GPS Tracks */
		state.gpsTrackID = -1;	// Whether a GPS track has been selected
		state.gpsTrackName = "";		// GPS track name

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

		setCurrentFieldID( 0 );						// Current field
		state.currentCluster = 0;
		state.clusterNearDistance = worldSettings.clusterCenterSize * state.clusterNearDistanceFactor;

//		initialize(0, 0, 0);
		initialize();
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
	 * Show any sound in field if visible
	 */
	public void showSounds()
	{
		settings.hideSounds = false;
		p.showSounds();
	}
	
	/** 
	 * Hide all sound in field
	 */
	public void hideSounds()
	{
		settings.hideSounds = true;
		p.hideSounds();
	}

	/**
	 * Update viewer about world state
	 * @param newWorldSettings
	 * @param newWorldState
	 */
	void updateState(WMV_WorldSettings newWorldSettings, WMV_WorldState newWorldState)
	{
		worldSettings = newWorldSettings;
		worldState = newWorldState;
		setOrientation();
		
		if(attractorPoint != null) 
			attractorPoint.update(p.getCurrentField(), worldSettings, worldState, settings, state);
	}

	/**
	 * Update Orientation (Static) Mode parameters
	 */
	private void updateOrientationMode()
	{
		state.clustersVisibleInOrientationMode = new ArrayList<Integer>();

		for(WMV_Cluster c : p.getCurrentField().getClusters())
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
					WMV_Cluster c = p.getCurrentField().getCluster(i);
					float cDist = c.getLocation().dist(state.location);
					float largest = -10000;
					int largestIdx = -1;
					int count = 0;

					for(int n : state.clustersVisibleInOrientationMode)		// Find farthest
					{
						WMV_Cluster v = p.getCurrentField().getCluster(n);
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
	 * Set current cluster if one is nearby, or else set to -1
	 * @param forceUpdate Force setting current cluster, even if already at current cluster
	 */
	private void updateCurrentCluster(boolean forceUpdate)
	{
		int nearest;
		
		if(forceUpdate) nearest = getNearestCluster(true);
		else nearest = getNearestCluster(false);
		
		if(nearest >= 0 && nearest < p.getCurrentFieldClusters().size())
		{
			WMV_Cluster c = p.getCurrentField().getCluster(nearest);
			float dist = c.getViewerDistance();
			if(dist > p.settings.maxClusterDistance)
				setCurrentCluster( nearest, -1 );
		}
		else
		{
			setCurrentCluster( -1, -1 );
			if(state.atCurrentCluster) state.atCurrentCluster = false;
			
			if(debug.viewer) p.ml.systemMessage("Viewer.updateCurrentCluster()... Cleared current cluster...");
		}
	}
	
	/**
	 * Update turning behavior
	 */
	void updateTurning()
	{
		if (state.turningX && !state.turnSlowingX) 		// Turning along X axis transition
		{
			state.turningAcceleration.x += settings.turningXAccelInc * state.turnXDirection;
			state.lastMovementFrame = worldState.frameCount;
		}

		if (state.turningY && !state.turnSlowingY) 		// Turning along Y axis transition
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

			if(Math.abs(state.turningAcceleration.x) > settings.turningAccelerationMax)			// Decelerate in X dimension if above turningAccelerationMax
				state.turningAcceleration.x *= settings.turningDecelInc;				

			if(Math.abs(state.turningAcceleration.y) > settings.turningAccelerationMax)			// Decelerate in Y dimension if above turningAccelerationMax
				state.turningAcceleration.y *= settings.turningDecelInc;				

			if(Math.abs(state.turningVelocity.x) > settings.turningVelocityMax)					// Reduce X velocity if above turningVelocityMax
				state.turningAcceleration.x *= settings.turningDecelInc;				

			if(Math.abs(state.turningVelocity.y) > settings.turningVelocityMax)					// Reduce Y velocity if above turningVelocityMax
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
			if(settings.optimizeVisibility && !p.getCurrentField().mediaAreFading())
			{
				if(state.turningMediaGoal != null)
				{
					if(!state.turningMediaGoal.equals(new PVector(-1.f, -1.f)))
					{
						float goalMediaBrightness = 0.f;
						switch((int)state.turningMediaGoal.y)
						{
							case 0:
								WMV_Image img = p.getCurrentField().getImage((int)state.turningMediaGoal.x);
								goalMediaBrightness = img.getViewingBrightness();
								break;
							case 1:
								WMV_Panorama pano = p.getCurrentField().getPanorama((int)state.turningMediaGoal.x);
								goalMediaBrightness = pano.getViewingBrightness();
								break;
							case 2:
								WMV_Video vid = p.getCurrentField().getVideo((int)state.turningMediaGoal.x);
								goalMediaBrightness =  vid.getViewingBrightness();
								break;
						}
						
						if(goalMediaBrightness == 0.f && settings.angleFading)
						{
							if(debug.viewer)
								p.ml.systemMessage("Set angle fading to false...");
							settings.angleFading = false;
						}
						
						state.turningMediaGoal = new PVector(-1.f, -1.f);
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

			if(Math.abs(state.attraction.mag()) > 0.f && !state.centering)					/* If not centering and attraction force exists */
			{
				if(Math.abs(state.acceleration.mag()) < settings.accelerationMax)			/* Apply attraction up to maximum acceleration */
					state.acceleration.add( PVector.div(state.attraction, settings.cameraMass) );	
				else
					p.ml.systemMessage("--> Attraction but no acceleration... attraction.mag():"+state.attraction.mag()+" acceleration.mag():"+state.acceleration.mag());
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

			WMV_Cluster curAttractor = new WMV_Cluster(worldSettings, worldState, settings, state, debug, 0, new PVector(0.f, 0.f, 0.f));	 /* Find current attractor if one exists */
			boolean attractorFound = false;
			
			if( state.movingToCluster )
			{
				if(state.attractorCluster != -1)
				{
					curAttractor = getAttractorCluster();
				}
			}
			else if( state.movingToAttractor )
			{
				if( attractorPoint != null )
				{
					curAttractor = attractorPoint;
					if(debug.viewer && debug.detailed)					/* If not slowing and attraction force exists */
						p.ml.systemMessage("--> attractorCluster:"+state.attractorCluster+" slowing:"+state.slowing+" halting:"+state.halting+" attraction.mag():"+state.attraction.mag()+" null? "+(curAttractor == null));
					if(debug.viewer && debug.detailed)					/* If not slowing and attraction force exists */
						p.ml.systemMessage("--> attractorPoint distance:"+attractorPoint.getClusterDistance()+" mass:"+attractorPoint.getMass()+" acceleration.mag():"+state.acceleration.mag()+" curAttractor dist: "+(curAttractor.getClusterDistance()));
					if(p.utilities.isNaN(state.attraction.mag()))
					{
						state.movingToAttractor = false;
						if(debug.viewer)					/* If not slowing and attraction force exists */
							p.ml.systemMessage("--> attraction was NaN... set movingToAttractor to false");
					}
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
						if(Math.abs(state.velocity.mag()) > settings.velocityMin)									/* Slow down near attractor center */
						{
//							if(!state.continueAtAttractor)		// Added 7-2-17
								if(!state.slowing) slow();
						}

					if(state.centering)													/* Centering within cluster */
					{
						if(curAttractor.getClusterDistance() < worldSettings.clusterCenterSize)
						{
							if(p.ml.frameCount < state.centeringTransitionEnd)
							{
								center(curAttractor.getLocation());						/* Center at current attractor */
							}
							else
							{
								if(debug.viewer && debug.detailed)
									p.ml.systemMessage("Viewer.updatePhysics()... Centered on attractor cluster... curAttractor.getClusterDistance(): "+curAttractor.getClusterDistance()+" worldSettings.clusterCenterSize:"+worldSettings.clusterCenterSize);
								reachedAttractor = true;
							}
						}
						else
						{
							stopCentering();				/* Shouldn't be centering if > than clusterCenterSize away from attractor*/
						}
					}
					else if(curAttractor.getClusterDistance() < worldSettings.clusterCenterSize)		
					{
						if(Math.abs(state.velocity.mag()) > settings.velocityMin)		/* Halt at attractor center */
						{
							if(!state.halting) halt();
						}
						else 
						{
							if(state.halting) state.halting = false;
							if(state.slowing) state.slowing = false;
							startCenteringAtAttractor();
						}
					}

					if(reachedAttractor) 
						handleReachedAttractor();
				}
				else
				{
					if(debug.viewer && debug.detailed) p.ml.systemMessage("Viewer.updatePhysics()... Waiting...");
				}
			}

			if(!state.centering)
			{
				state.location.add(state.velocity);			// Add velocity to location
				setLocation(state.location, false);			// Move camera
			}
		}

		if(state.attractorCluster != -1)
		{
			float curAttractorDistance = PVector.dist( p.getCurrentField().getCluster(state.attractorCluster).getLocation(), getLocation() );
			if(curAttractorDistance > settings.lastAttractorDistance && !state.slowing)		// If the camera is getting farther than attractor
			{
				if(debug.viewer && state.attractionStart - worldState.frameCount > 20)
				{
					p.ml.systemMessage("Viewer.updatePhysics()... Getting farther from attractor: will stop moving...");
					stop(true);
				}
			}

			/* Record last attractor distance */
			settings.lastAttractorDistance = PVector.dist(p.getCurrentField().getCluster(state.attractorCluster).getLocation(), getLocation());
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
		if(debug.viewer)
			p.ml.systemMessage("Viewer.handleReachedAttractor()... movingToCluster:"+state.movingToCluster+" movingToAttractor:"+state.movingToAttractor+" attractorCluster:"+state.attractorCluster + " following:"+state.following+" path.size():"+path.size());

		if(state.following && path.size() > 0)		/* Reached attractor when following a path */	
		{
			if(debug.viewer)
				p.ml.systemMessage( "Viewer.handleReachedAttractor()... following path size:"+path.size() );

			stopMoving(true);

			setCurrentCluster( state.attractorCluster, -1 );		// Set current cluster to attractor cluster, if one exists
			
			if(debug.path)
				p.ml.systemMessage("Viewer.handleReachedAttractor()... Reached path goal #"+state.pathLocationIdx+", will start waiting...");
			
			startWaiting(settings.pathWaitLength);
		}

		if(state.movingToCluster)					/* Reached attractor when moving to cluster */		
		{
			if(debug.viewer)
				p.ml.systemMessage("Viewer.handleReachedAttractor()... Moving to cluster... current:"+state.currentCluster+" attractor: "+state.attractorCluster+"...");
			
			if(state.attractorCluster != -1)
			{
				if(debug.viewer)												// -- Debugging:
					if(state.attractorCluster != getNearestCluster(true))		// -- Check if attractor cluster is nearest cluster
						p.ml.systemMessage("Viewer.handleReachedAttractor()... WARNING: attractor cluster is: "+state.attractorCluster+" but nearest cluster is different:"+getNearestCluster(true));

				if(state.movingToTimeSegment)
					setCurrentCluster( state.attractorCluster, state.timeSegmentTarget );
				else
					setCurrentCluster( state.attractorCluster, -1 );

				state.attractorCluster = -1;
				p.getCurrentField().clearAllAttractors();	// Stop attracting when reached attractorCluster
			}
			else
			{
				updateCurrentCluster(true);					// Force updating current cluster
//				turnToCurrentClusterOrientation();			// -- Disabled
			}
			
			if(debug.viewer)
				p.ml.systemMessage("Viewer.handleReachedAttractor()... Reached cluster... current:"+state.currentCluster+" nearest: "+getNearestCluster(false)+" set current time segment to "+state.currentFieldTimeSegment);
			
			state.movingToCluster = false;
			state.movingToAttractor = false;
		}
		else if(state.movingToAttractor)							/* Stop attracting when reached attractorPoint */
		{
			p.getCurrentField().clearAllAttractors();
			state.movingToAttractor = false;
			
			updateCurrentCluster(true);
//			turnToCurrentClusterOrientation();
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
		state.slowing = true;										// Slowing when close to attractor
	}

	/**
	 * Start halting the viewer
	 */
	private void halt()
	{
		state.slowing = false;
		state.halting = true;										// Slowing when close to attractor
	}

	/**
	 * Set specified field as current field
	 * @param newField  Field to set as current
	 */
	public void setCurrentField(int newField, boolean setSimulationState)		
	{
		if(debug.viewer && debug.detailed)		
			p.ml.systemMessage("Viewer.setCurrentField().. newField:"+newField+" setSimulationState? "+setSimulationState);

		if(newField < p.getFieldCount())
		{
			setCurrentFieldID( newField );

			if(debug.viewer && debug.detailed)		
				p.ml.systemMessage("Viewer.setCurrentField().. after set field ID... new state.field:"+getCurrentFieldID()+" currentField ID:"+getCurrentFieldID()+" currentCluster:"+state.currentCluster);

			if(setSimulationState)											// Set simulation state from saved
			{
				p.setStateFromField(p.getField(newField));
//				p.setSimulationStateFromField(p.getField(newField), true);
				if(debug.viewer && debug.detailed)		
					p.ml.systemMessage("Viewer.setCurrentField().. after setSimulationStateFromField...  state.field:"+getCurrentFieldID()+" currentField ID:"+getCurrentFieldID()+" currentCluster:"+state.currentCluster+" location:"+getLocation());
			}
			else
			{
				p.getCurrentField().updateAllMediaStates();
			}

			if(!p.getField(getCurrentFieldID()).hasBeenVisited()) 
				p.getField(getCurrentFieldID()).setVisited(true);
		}
	}

	
	/**
	 * @param newCluster New attractor cluster
	 * Set a specific cluster as the current attractor
	 */
	private void setAttractorCluster(int newCluster, boolean stop)
	{
		if(stop) stop(true);					
		
		if(state.atCurrentCluster)
		{
			saveCurrentClusterOrientation();
			state.atCurrentCluster = false;
		}

		if(debug.viewer) p.ml.systemMessage("Setting new attractor:"+newCluster+" old attractor:"+state.attractorCluster);
			
		state.attractionStart = worldState.frameCount;									// Set attraction starting frame 
		state.attractorCluster = newCluster;											// Set attractor cluster
		state.movingToCluster = true;													// Move to cluster
		
		p.getCurrentField().clearAllAttractors();										// Clear all attractors
		p.getCurrentField().getCluster(state.attractorCluster).setAttractor(true);		// Set attractor cluster
		
		if(p.getCurrentField().getCluster(state.attractorCluster).getClusterDistance() < state.clusterNearDistance)
		{
			if(p.getCurrentField().getCluster(state.attractorCluster).getClusterDistance() > worldSettings.clusterCenterSize)
			{
				state.movingNearby = true;
			}
			else if(p.getCurrentField().getCluster(state.attractorCluster).getClusterDistance() > worldSettings.clusterCenterSize * 0.01f)
			{
				if(debug.viewer && debug.detailed) p.ml.systemMessage("Viewer.setAttractorCluster()... Centering at attractor cluster#"+state.attractorCluster+"...");
				startCenteringAtAttractor();
			}
			else
			{
				if(debug.viewer) 
					p.ml.systemMessage("Viewer.setAttractorCluster()... Reached attractor cluster #"+state.attractorCluster+" without moving...");
				handleReachedAttractor();				// Reached attractor without moving
			}
		}
	}
	
	/**
	 * Get unit vector pointing towards current viewer target point 
	 * @return Current viewer target vector
	 */
	public PVector getTargetVector()
	{
		float[] cTar = camera.getTarget();			// Get camera attitude (orientation)
		float pitch = cTar[1], yaw = cTar[0];		// Ignore roll

		float sinYaw = PApplet.sin(yaw);
		float cosYaw = PApplet.cos(yaw);
		float sinPitch = PApplet.sin(-pitch);
		float cosPitch = PApplet.cos(-pitch);

		PVector camOrientation = new PVector (-cosPitch * sinYaw, sinPitch, -cosPitch * cosYaw);	
		camOrientation.normalize();
		
		return camOrientation;
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
	 * @param inclCurrent Whether to include the current cluster in search
	 * @return Index of nearest cluster to camera
	 */
	public int getNearestCluster(boolean inclCurrent) 	// Returns the cluster nearest to the current camera position, excluding the current cluster
	{
		PVector cPos = getLocation();
		float smallest = 100000.f;
		int smallestIdx = 0;

		if (p.getCurrentField().getClusters().size() > 0) 
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
			if(debug.cluster)
				p.ml.systemMessage("No clusters in field...");
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
	public ArrayList<WMV_Cluster> getNearClusters(int amount, float threshold) 			// -- Excluding the current cluster??
	{
		PVector vPos = getLocation();
		IntList nearList = new IntList();
		FloatList distList = new FloatList();
		ArrayList<WMV_Cluster> cl = new ArrayList<WMV_Cluster>( p.getActiveClusters() );
		ArrayList<WMV_Cluster> removeList = new ArrayList<WMV_Cluster>();
		
		if(amount == -1)										// No limit on number of clusters to search for
			amount = cl.size();
		
		for (WMV_Cluster c : cl) 								// Fill the list with <amount> locations under <threshold> distance from viewer
		{
			float dist = PVector.dist(vPos, c.getLocation());	// Distance from cluster to viewer
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

		ArrayList<WMV_Cluster> nearClusters = new ArrayList<WMV_Cluster>();
		for(int i:nearList)
			nearClusters.add(p.getCurrentField().getCluster(i));
		
		nearClusters.sort(WMV_Cluster.WMV_ClusterDistanceComparator);				/* Sort clusters by distance */
		
		if(nearClusters.size() > amount)
		{
			int remaining = nearClusters.size() - amount;
			
			for(int i = nearClusters.size()-1; remaining > 0; i--)
			{
				nearClusters.remove(i);
				remaining--;
			}
		}
		
		return nearClusters;
	}

	/** 
	 * @return Nearest cluster ID in front of camera
	 */
	private int getClusterAhead() 					// Returns the visible cluster closest to the camera
	{
		PVector camOrientation = getOrientationVector();

		ArrayList<WMV_Cluster> nearClusters = getNearClusters(20, worldSettings.defaultFocusDistance * 4.f);	// Find 20 nearest clusters -- Change based on density?
		IntList frontClusters = new IntList();
		
		for (WMV_Cluster c : nearClusters) 							// Iterate through the clusters
		{
//			WMV_Cluster c = p.getCurrentField().getCluster(i);
			PVector clusterVector = getVectorToCluster(c);
			PVector crossVector = new PVector();
			PVector.cross(camOrientation, clusterVector, crossVector);		// Cross vector gives angle between camera and image
			float result = crossVector.mag();
			
			if(Math.abs(result) < settings.fieldOfView && c.getID() != state.currentCluster && !c.isEmpty())			// If cluster (center) is within field of view
			{
				if(debug.cluster || debug.viewer)
					p.ml.systemMessage("Centered cluster:"+c.getID()+" == "+c.getID()+" at angle "+result+" from camera...");
				frontClusters.append(c.getID());
			}
			else
				if(debug.cluster || debug.viewer)
					p.ml.systemMessage("Non-centered, current or empty cluster:"+c.getID()+" at angle "+result+" from camera..."+" NOT centered!");
		}

		float smallest = 100000.f;
		int smallestIdx = 0;

		for (int i = 0; i < frontClusters.size(); i++) 		// Compare distances of clusters in front
		{
			WMV_Cluster c = (WMV_Cluster) p.getCurrentField().getCluster(i);
			if(debug.cluster || debug.viewer)
				p.ml.systemMessage("Checking Centered Cluster... "+c.getID());
		
			float dist = PVector.dist(getLocation(), c.getLocation());
			if (dist < smallest) 
			{
				if(debug.cluster || debug.viewer)
					p.ml.systemMessage("Cluster "+c.getID()+" is closer!");
				smallest = dist;
				smallestIdx = i;
			}
		}		
		
		if(frontClusters.size() > 0)
			return smallestIdx;
		else
		{
			p.ml.systemMessage("No clusters ahead!");
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
					if(debug.viewer)
						p.ml.systemMessage("No images visible! will look at nearest image...");
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
		
		if(worldState.frameCount > state.pathWaitStartFrame + waitLength )		// Finished waiting, will move to next path location
		{
			state.waiting = false;
			if(debug.path) p.ml.systemMessage("Viewer.updateFollowing()... Finished waiting...");

			state.pathLocationIdx++;
			
			if(state.pathLocationIdx < path.size())
			{
				state.pathGoal = path.get(state.pathLocationIdx).getWorldLocation();
				if(debug.path) p.ml.systemMessage("Viewer.updateFollowing()... Next path location:"+state.pathGoal);
				
				if(state.pathLocationIdx >= 1)
				{
					if( state.pathGoal != path.get(state.pathLocationIdx-1).getWorldLocation() && 	// Goal is different point 
						PVector.dist(state.pathGoal, state.location) > worldSettings.clusterCenterSize)
					{
						if(debug.path) p.ml.systemMessage("Viewer.updateFollowing()... Will "+(state.followTeleport?"teleport":"move") +" to next attraction point..."+state.pathGoal);
						if(state.followTeleport)
						{
							if(!p.getCurrentField().mediaAreFading())
								teleportToPoint(state.pathGoal, true, false);		// Teleport without stopping following
							else
								startWaiting(waitLength);							// Start waiting to teleport
						}
						else
							moveToPathPoint(state.pathLocationIdx);
//							setPathAttractorPoint(state.pathGoal, false);
					}
					else													// Goal is same as location
					{
						if(debug.path) p.ml.systemMessage("Viewer.updateFollowing()...Same or very close attraction point!");
						
						if(settings.orientationModeConstantWaitLength)
						{
							if(debug.path) p.ml.systemMessage("Viewer.updateFollowing()...Ignoring pathLocationIdx #"+state.pathLocationIdx+" at same location as previous...");
							
							state.pathLocationIdx++;
							state.pathGoal = path.get(state.pathLocationIdx).getWorldLocation();
							
							while(state.pathGoal == path.get(state.pathLocationIdx-1).getWorldLocation())
							{
								if(debug.path) p.ml.systemMessage("Viewer.updateFollowing()... Also ignoring pathLocationIdx #"+state.pathLocationIdx+" at same location as previous...");
								state.pathLocationIdx++;
								state.pathGoal = path.get(state.pathLocationIdx).getWorldLocation();
							}
						}
						
						if(debug.path) p.ml.systemMessage("Viewer.updateFollowing()... Set new path location:"+state.pathGoal);
						
						if(state.followTeleport)
						{
							if(!p.getCurrentField().mediaAreFading())
								teleportToPoint(state.pathGoal, true, false);
							else
								startWaiting(waitLength);
						}
						else
							moveToPathPoint(state.pathLocationIdx);
//							setPathAttractorPoint(state.pathGoal, false);
					}
				}
			}
			else
			{
				if(debug.path)
				{
					p.ml.systemMessage("Reached end of path... ");
					p.ml.systemMessage(" ");
				}
				stopFollowing();
			}
		}
		
		if(state.waiting == false && debug.path) 
			p.ml.systemMessage("Finished waiting...");
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
				WMV_Image img = p.getCurrentField().getImage(i);
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
				WMV_Video vid = p.getCurrentField().getVideo(i);
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
	 * Set viewer field of view and zoom the camera
	 * @param newFieldOfView
	 */
	public void zoomToFieldOfView(float newFieldOfView)
	{
		setFieldOfView( newFieldOfView );
		camera.setFieldOfView(newFieldOfView);
	}
	
	/**
	 * Zoom by given amount
	 * @param zoom Zoom amount
	 */
	public void zoomByAmount(float zoom)
	{
		setFieldOfView( settings.fieldOfView + zoom );
		camera.zoom(zoom);
	}

	/**
	 * Reset the 3D camera
	 */
	public void resetCamera()
	{
		initializeAtLocation( getLocation().x, getLocation().y, getLocation().z );				// Initialize camera
	}
	
	/**
	 * Update teleporting interpolation values
	 */
	private void updateTeleporting()
	{
		if(worldState.frameCount >= state.teleportStart + settings.teleportLength)		// If the teleport has finished
		{
			if(debug.viewer && debug.detailed) p.ml.systemMessage("Viewer.updateTeleporting()...  Reached teleport goal...");
			
			if( !p.getCurrentField().mediaAreFading() )									// Once no more media are fading
			{
				if(debug.viewer && debug.detailed) p.ml.systemMessage("Viewer.updateTeleporting()... Media finished fading...");
				
				if(state.following && path.size() > 0)
				{
					setCurrentCluster( getNearestCluster(true), -1 );
					if(debug.path)
						p.ml.systemMessage("Viewer.updateTeleporting()... Reached path goal #"+state.pathLocationIdx+", will start waiting...");
					startWaiting( settings.pathWaitLength );
				}
				
				if(state.followingGPSTrack && gpsTrack.size() > 0)
				{
					setCurrentCluster( getNearestCluster(true), -1 );
					if(debug.path)
						p.ml.systemMessage("Viewer.updateTeleporting()... Reached path goal #"+state.pathLocationIdx+", will start waiting...");
					startGPSTrackNavigation();							// Start transition to second GPS track point
				}

				if(state.teleportToField != -1)							// If a new field has been specified 
				{
					if(debug.viewer) p.ml.systemMessage("Viewer.updateTeleporting()... Calling enterField()...  will set state? "+p.getField(state.teleportToField).hasBeenVisited());
					
					enterField(state.teleportToField, p.getField(state.teleportToField).hasBeenVisited());					// Enter new field
					
					if(debug.viewer) p.ml.systemMessage("Viewer.updateTeleporting()... Entered field... "+state.teleportToField);

					WMV_Waypoint entry = null;
					if(state.teleportToField >= 0 && state.teleportToField < p.getFieldCount())
						entry = p.getField(state.teleportToField).getState().entryLocation;

					boolean hasEntryPoint = false;
					if(entry != null)
						hasEntryPoint = entry.initialized();

					if(hasEntryPoint)
					{
						if(debug.viewer) 
							p.ml.systemMessage("Viewer.updateTeleporting()...  Field has Entry Point... "+p.getField(state.teleportToField).getState().entryLocation.getWorldLocation());
						
						moveToWaypoint( p.getField(state.teleportToField).getState().entryLocation, false );	 // Move to waypoint and stop				
					}
					else
					{
						if(debug.viewer) p.ml.systemMessage("Viewer.teleportToField()...  No Entry Point found...");
						moveToFirstTimeSegment(false);					// Move to first time segment if start location not set from saved data 
					}

					if(debug.viewer)  p.ml.systemMessage("Viewer.updateTeleporting()...  Teleported to field "+state.teleportToField+" goal point: x:"+state.teleportGoal.x+" y:"+state.teleportGoal.y+" z:"+state.teleportGoal.z);
					state.teleportToField = -1;
				}
				
				if(state.ignoreTeleportGoal)							
					state.ignoreTeleportGoal = false;
				else
					setLocation(state.teleportGoal, true);					// Jump to goal
				
				state.teleporting = false;								// Change the system status

				if(state.teleportGoalCluster != -1)
				{
					if(state.movingToTimeSegment)
						setCurrentCluster( state.teleportGoalCluster, state.timeSegmentTarget );
					else
						setCurrentCluster( state.teleportGoalCluster, -1 );

					state.teleportGoalCluster = -1;
				}
				else
				{
					if(state.movingToCluster)							
						state.movingToCluster = false;
					if(state.movingToAttractor)
						state.movingToAttractor = false;

					setCurrentCluster( getNearestCluster(true), -1 );	// Set currentCluster to nearest
				}
				
				p.getCurrentField().clearAllAttractors();				// Clear current attractors
				
				if(p.state.waitingToFadeInTerrainAlpha) 				// Fade in terrain
					p.fadeInTerrain(false);
			}
			else
			{
				state.teleportWaitingCount++;
				if(debug.viewer && debug.detailed)
					p.ml.systemMessage("Waiting to finish teleport... "+state.teleportWaitingCount);
			}
		}
	}
	
	/**
	 * Start following current field timeline as a path
	 */
	public void startFollowingTimeline(boolean fromBeginning)
	{
		if(!state.following)
		{
			path = p.getCurrentField().getTimelineAsPath();			/* Get timeline as path of Waypoints matching cluster IDs */

			if(path.size() > 0)
			{
				WMV_Cluster c = p.getCurrentCluster();
				if(c != null)
				{
					state.following = true;

					if(fromBeginning)						/* Start at beginning */
					{
						state.pathLocationIdx = 0;
					}
					else									/* Find path location of current cluster and set as beginning */
					{
						int count = 0;
						state.pathLocationIdx = -1;
						
						for(WMV_Waypoint w : path)
						{
							if(w.getClusterID() == c.getID())
							{
								state.pathLocationIdx = count;
								break;
							}
							count++;
						}

						if(state.pathLocationIdx == -1) 
							state.pathLocationIdx = 0;
					}

					if(debug.viewer)
						p.ml.systemMessage("Viewer.followTimeline()... Setting first path goal: "+path.get(state.pathLocationIdx).getWorldLocation());
					
					state.pathGoal = path.get(state.pathLocationIdx).getWorldLocation();

					if(fromBeginning)
					{
						if(state.pathGoal.dist(getLocation()) > worldSettings.clusterCenterSize)
							moveToFirstPathPoint();
						else										// First path point is current point
						{
							boolean found = false;				// Find next path point, if one exists, not at current point
							while(!found)
							{
								state.pathLocationIdx++;
								if(state.pathLocationIdx < path.size())
								{
									state.pathGoal = path.get(state.pathLocationIdx).getWorldLocation();
									if(state.pathGoal.dist(getLocation()) > worldSettings.clusterCenterSize)
									{
										moveToPathPoint(state.pathLocationIdx);	
										found = true;
									}
								}
								else
								{
									stopFollowing();
									break;
								}
							}
						}
					}
					else
					{
						moveToPathPoint(state.pathLocationIdx);	
					}

					if(p.getSettings().screenMessagesOn)
						p.ml.display.message(p.ml, "Started Following Timeline...");
				}
				else p.ml.systemMessage("Viewer.startFollowingTimeline()... No current cluster!");
			}
			else p.ml.systemMessage("Viewer.startFollowingTimeline()... No timeline points!");
		}
	}

	/**
	 * Follow memory path
	 * @param fromBeginning Whether to start at beginning (true) or nearest point (false)
	 */
	public void startFollowingMemory(boolean fromBeginning)
	{
		if(!state.following)
		{
			path = new ArrayList<WMV_Waypoint>(memory);		/* Follow memory path */

			if(path.size() > 0)
			{
				WMV_Cluster c = p.getCurrentCluster();
				if(c != null)
				{
					state.following = true;

					if(fromBeginning)						/* Start at beginning */
					{
						state.pathLocationIdx = 0;
					}
					else										/* Find path location of current cluster and set as beginning */
					{
						int count = 0;
						state.pathLocationIdx = -1;
						
						for(WMV_Waypoint w : path)
						{
							if(w.getClusterID() == c.getID())
							{
								state.pathLocationIdx = count;
								break;
							}
							count++;
						}

						if(state.pathLocationIdx == -1) 
							state.pathLocationIdx = 0;
					}

					if(debug.viewer)
						p.ml.systemMessage("Viewer.startFollowingMemory()... Setting first path goal: "+path.get(state.pathLocationIdx).getWorldLocation());
					
					state.pathGoal = path.get(state.pathLocationIdx).getWorldLocation();

					if(fromBeginning)
					{
						if(state.pathGoal.dist(getLocation()) > worldSettings.clusterCenterSize)
							moveToFirstPathPoint();
						else										// First path point is current point
						{
							boolean found = false;				// Find next path point, if one exists, not at current point
							while(!found)
							{
								state.pathLocationIdx++;
								if(state.pathLocationIdx < path.size())
								{
									state.pathGoal = path.get(state.pathLocationIdx).getWorldLocation();
									if(state.pathGoal.dist(getLocation()) > worldSettings.clusterCenterSize)
									{
										moveToPathPoint(state.pathLocationIdx);	
										found = true;
									}
								}
								else
								{
									stopFollowing();
									break;
								}
							}
						}
					}
					else
					{
						moveToPathPoint(state.pathLocationIdx);	
					}

					if(p.getSettings().screenMessagesOn)
						p.ml.display.message(p.ml, "Started Following Memory...");
				}
				else p.ml.systemMessage("Viewer.startFollowingMemory()... No current cluster!");
			}
			else p.ml.systemMessage("Viewer.startFollowingMemory()... No timeline points!");
		}
	}

//	/**
//	 * Follow memory path
//	 * @param fromBeginning Whether to start at beginning (true) or nearest point (false -- In progress)
//	 */
//	private void startFollowingMemoryX(boolean fromBeginning)
//	{
//		path = new ArrayList<WMV_Waypoint>(memory);								// Follow memory path 
//		
//		if(path.size() > 0)
//		{
//			state.following = true;
//			state.pathLocationIdx = 0;
//			if(debug.viewer)
//				p.ml.systemMessage("Viewer.followMemory() points:"+path.size()+"... Setting first path goal: "+path.get(state.pathLocationIdx).getWorldLocation());
//			
//			state.pathGoal = path.get(state.pathLocationIdx).getWorldLocation();
//
//			if(fromBeginning)
//			{
//				moveToFirstPathPoint();
//			}
//			else
//			{
//				moveToPathPoint( state.pathLocationIdx );
//			}
//
//			if(p.getSettings().screenMessagesOn)
//				p.ml.display.message(p.ml, "Started Following Path: Memory...");
//		}
//		else p.ml.systemMessage("Viewer.followMemory()... path.size() == 0!");
//	}

	/**
	 * Start following path in current Path Navigation Mode
	 */
	public void startFollowing()
	{
		switch(getPathNavigationMode())
		{
			case 0:
				startFollowingTimeline(true);
				break;
			case 1:
				startFollowingGPSTrack(true);
				break;
			case 2:
				startFollowingMemory(true);
				break;
		}

		if(p.ml.display.window.setupNavigationWindow)
			p.ml.display.window.chkbxPathFollowing.setSelected(true);
	}
	
	/**
	 * Start following GPS track
	 */
	public void startFollowingGPSTrack(boolean fromBeginning)
	{
//		if(fromBeginning)
//		{
		if(state.gpsTrackID > -1 && state.gpsTrackID < p.getCurrentField().getGPSTracks().size())
		{
			if(gpsTrack.size() > 0)
			{
				if(state.pathWaiting)
				{
					path = new ArrayList<WMV_Waypoint>(gpsTrack);
					
					if(debug.viewer || debug.gps) p.ml.systemMessage("Viewer.startFollowingGPSTrack()...  path points:"+path.size());
					
					state.following = true;
					state.pathLocationIdx = 0;
					state.pathGoal = path.get(state.pathLocationIdx).getWorldLocation();			// Set path goal from GPS track

					if(debug.viewer || debug.gps || debug.path)
					{
						p.ml.systemMessage("    path.get(state.pathLocationIdx).getCaptureLocation()"+path.get(state.pathLocationIdx).getWorldLocation());
						p.ml.systemMessage("    path.get(state.pathLocationIdx).getGPSLocation()"+path.get(state.pathLocationIdx).getGPSLocation());
						p.ml.systemMessage("    state.pathGoal: "+state.pathGoal);
					}

					moveToFirstPathPoint();

					if(p.getSettings().screenMessagesOn)
						p.ml.display.message(p.ml, "Started Following Path: GPS Track");
				}
				else
				{
					state.followingGPSTrack = true;
					
					if(debug.viewer || debug.gps) 
						p.ml.systemMessage("Viewer.startFollowingGPSTrack()...  gpsTrack points:"+gpsTrack.size());

					state.gpsTrackGoal = gpsTrack.get(state.gpsTrackLocationIdx).getWorldLocation();  // Set path goal from GPS track
					state.gpsTrackLocationIdx = 0;
					boolean teleport = ( PVector.dist( state.gpsTrackGoal, getLocation() ) > settings.farClusterTeleportDistance );
					
					if(p.ml.debug.viewer)
						p.ml.systemMessage("Viewer.startFollowingGPSTrack()... state.gpsTrackGoal set to:"+gpsTrack.get(state.gpsTrackLocationIdx).getWorldLocation());
					if(p.ml.debug.viewer)
						p.ml.systemMessage("   Original GPS location:"+gpsTrack.get(state.gpsTrackLocationIdx).getGPSLocation() + " state.gpsTrackLocationIdx:"+state.gpsTrackLocationIdx);
					
					moveToFirstGPSTrackPoint();
				}
			}
			else p.ml.systemMessage("Viewer.startFollowingGPSTrack()... ERROR: path.size() == 0!");
		}
		else p.ml.systemMessage("Viewer.startFollowingGPSTrack()... ERROR...");
//		}
//		else
//		{
//			
//		}
	}

	/**
	 * Update GPS track following behavior
	 */
	private void updateGPSTrackFollowing()
	{
		if(p.ml.frameCount >= state.gpsTrackTransitionEnd)		// Reached end of transition
		{
			if(state.gpsTrackLocationIdx > 0)
				setLocation( state.gpsTrackGoal, true );				// Set location to path goal
			
			if(p.ml.debug.viewer || p.ml.debug.gps || p.ml.debug.path)
				if(p.ml.debug.detailed) 
					p.ml.systemMessage("updateGPSTrackFollowing()... Reached path goal: " + getLocation() + " == "+state.gpsTrackGoal);   // Debug

			state.gpsTrackLocationIdx++;
			if(state.gpsTrackLocationIdx < gpsTrack.size())
				transitionToGPSTrackPointID( state.gpsTrackLocationIdx );			// Reached end of GPS track
			else			
			{
				stop(true);
			}
		}
		else							// Perform interpolation between points
		{
			if(state.gpsTrackLocationIdx > 0)
			{
				float framePosition = p.ml.frameCount - state.gpsTrackTransitionStart;				// 0 to gpsTrackTransitionLength
				float percent = p.ml.utilities.mapValue(framePosition, 0.f, state.gpsTrackTransitionLength, 0.f, 1.f);
				setLocation( p.utilities.lerp3D(state.gpsTrackStartLocation, state.gpsTrackGoal, percent), false );
			}
			else p.ml.systemMessage("Viewer.updateGPSTrackFollowing()... path index 0... not updating location...");
		}
	}
	
	/**
	 * Start GPS track navigation after first GPS track point reached
	 */
	private void startGPSTrackNavigation()
	{
		state.gpsTrackLocationIdx++;
		if(state.gpsTrackLocationIdx < gpsTrack.size())
			transitionToGPSTrackPointID(state.gpsTrackLocationIdx);
		else
			p.ml.systemMessage("Viewer.startGPSTrackNavigation()... ERROR Reached end of GPS track after first point!");
	}
	
	/**
	 * Begin transition from current to next point on GPS track 
	 */
	private void transitionToGPSTrackPointID(int gpsTrackLocationIdx)
	{
		if(p.ml.debug.viewer && p.ml.debug.detailed) 
			p.ml.systemMessage("Viewer.transitionToGPSTrackPointID()... gpsTrackLocationIdx:"+gpsTrackLocationIdx);
		
		state.gpsTrackGoal = gpsTrack.get( gpsTrackLocationIdx ).getWorldLocation();
		state.gpsTrackTransitionLength = (int)(state.gpsTrackGoal.dist( getLocation() ) * state.gpsTransitionLengthDistanceFactor);
		state.gpsTrackTransitionLength /= settings.gpsTrackTransitionSpeedFactor;		// Added 7-13-17
		state.gpsTrackStartLocation = getLocation();
		state.gpsTrackTransitionStart = p.ml.frameCount;
		state.gpsTrackTransitionEnd = p.ml.frameCount + state.gpsTrackTransitionLength;
	}
	
	/**
	 * Choose GPS track from list and set to selected
	 */
	public void openChooseGPSTrackWindow()
	{
		ArrayList<String> tracks = p.getCurrentField().getGPSTrackNames();
		if(p.ml.display.initializedMaps)
			p.ml.display.map2D.createdGPSMarker = false;
		p.ml.display.window.openListItemWindow(tracks, "Use arrow keys to select GPS track file and press ENTER", 1);
	}
	
	/**
	 * Wait for specified time until moving to next waypoint in path
	 * @param waitLength Length of time to wait
	 */
	private void startWaiting(int waitLength)	
	{
		if(settings.pathWaitLength != waitLength)
			settings.pathWaitLength = waitLength;			// Set path wait length
		
		state.waiting = true;
		state.pathWaitStartFrame = worldState.frameCount;
	}
	
	/**
	 * Start the viewer
	 */
	public void start()
	{
		state.firstRunningFrame = true;
	}

	/**
	 * Prompt user to select field to enter
	 */
	public void chooseFieldDialog()
	{
		ArrayList<String> fields = p.getFieldNames();
		p.ml.display.window.openListItemWindow(fields, "Use arrow keys to select field and press ENTER...", 0);
	}

	/**
	 * Show what viewer sees on screen
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
		mediaViewCamera.show();						
	}

	/**
	 * Set viewer state
	 * @param newState New viewer state
	 */
	public void setState(WMV_ViewerState newState)
	{
		state = newState;
		setLocation(state.location, false);			// Update the camera location
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
	 * Reset field of view to initial value
	 */
	public void setInitialFieldOfView()
	{
		zoomToFieldOfView( getInitFieldOfView() );
	}
	
	/**
	 * Reset field of view to initial value
	 */
	public void resetPerspective()
	{
		camera.resetPerspective();
	}
	
	/**
	 * Get the current camera
	 * @return
	 */
	public WMV_Camera getCamera()
	{
		return camera;
	}
	
	/**
	 * Set the camera
	 * @param newCamera
	 */
	public void setCamera(WMV_Camera newCamera)
	{
		camera = newCamera;
	}

	/**
	 * @param newPoint Point of interest to attract camera 
	 */
	private void setAttractorPoint(PVector newPoint, boolean stopFirst, boolean stopFollowing)
	{
		if(debug.viewer) 
			p.ml.systemMessage("Viewer.setAttractorPoint()... stopFirst: "+stopFirst+" stopFollowing:"+stopFollowing);
		
		if(stopFirst) 
			stop(stopFollowing);
		else if(stopFollowing)
			stop(true);
		
		if(state.atCurrentCluster)
		{
			saveCurrentClusterOrientation();
			state.atCurrentCluster = false;
		}
		
		state.movingToAttractor = true;
		attractorPoint = new WMV_Cluster(worldSettings, worldState, settings, state, debug, -1, newPoint);
		attractorPoint.setEmpty(false);
		attractorPoint.setAttractor(true);
		
		float distance = newPoint.dist( getLocation() );					// Adjust mass for attractor distance from viewer
		float mass; 
		
		if(state.following)
		{
//			if(distance < 10.f)
//			{
				mass = worldSettings.pathAttractorMass;		
				mass = PApplet.constrain( mass * (float)Math.sqrt(distance) * worldSettings.attractorMassDistanceFactor, 
						worldSettings.minPathAttractorMass,  worldSettings.maxPathAttractorMass );			
//			}
//			else
//			{
//				mass = worldSettings.pathAttractorMass;		
//				mass = PApplet.constrain( mass * (float)Math.sqrt(distance) * worldSettings.attractorMassDistanceFactor, 
//						worldSettings.minPathAttractorMass,  worldSettings.maxPathAttractorMass );			
//			}
		}
		else 
		{
			mass = worldSettings.attractorMass;			
			mass = PApplet.constrain( mass * (float)Math.sqrt(distance) * worldSettings.attractorMassDistanceFactor, 
					worldSettings.minAttractorMass,  worldSettings.maxAttractorMass );			
		}
		
		attractorPoint.setMass(mass);									// Set new mass
		
//		if(state.following) attractorPoint.setMass(worldSettings.pathAttractorMass);		// -- Tie to distance?
//		else attractorPoint.setMass(worldSettings.attractorMass);			// -- Tie to distance?
		
		state.attractionStart = worldState.frameCount;
		
		if(debug.viewer) 
			p.ml.systemMessage("Viewer.setAttractorPoint()... attractorPoint mass: "+attractorPoint.getMass()+" following:"+state.following);
	}

	/**
	 * Set path attractor point
	 * @param newPoint Point of interest to attract camera 
	 */
	private void setPathAttractorPoint(PVector newPoint, boolean first)
	{
		if(debug.viewer) 
			p.ml.systemMessage("Viewer.setPathAttractorPoint()... first: "+first);

		if(first)
			setAttractorPoint(newPoint, true, false);
		else
			setAttractorPoint(newPoint, false, false);
	}
	
	/**
	 * Clear the current attractor point
	 */
	private void clearAttractor()
	{
		if(debug.viewer) p.ml.systemMessage("Viewer.clearAttractor()...");
		
		state.movingToAttractor = false;
		attractorPoint = null;
	}
	
	/**
	 * Center viewer at attractor cluster
	 */
	public void startCenteringAtAttractor()
	{
		state.centering = true;
		state.centeringTransitionStart = p.ml.frameCount;
		state.centeringTransitionEnd = state.centeringTransitionStart + state.centeringTransitionLength;
	}
	
	public void stopCentering()
	{
		state.centering = false;
	}
	
	private void center(PVector dest)
	{
		setLocation( p.utilities.lerp3D(getLocation(), dest, 1.f/state.centeringTransitionLength), false );
	}

	
	private void saveCurrentClusterOrientation()
	{
		state.saveClusterOrientation(state.currentCluster, getXOrientation(), getYOrientation(), getZOrientation());
	}

	public void setOrientationMode( boolean newState )
	{
		settings.orientationMode = newState;
		if(p.getSettings().screenMessagesOn)
			p.ml.display.message(p.ml, "Orientation Mode "+(newState?"ON":"OFF"));

		if(newState)		// Entering Orientation Mode
		{
			PVector target = new PVector(camera.getTarget()[0], camera.getTarget()[1], camera.getTarget()[2]);
			camera.teleport(0, 0, 0);
			
			target = new PVector(target.x - getLocation().x, target.y - getLocation().y, target.z - getLocation().z);
			camera.aim(target.x, target.y, target.z);
			target = new PVector(camera.getTarget()[0], camera.getTarget()[1], camera.getTarget()[2]);
			
			updateOrientationMode();
		}
		else				// Exiting Orientation Mode
		{
			camera.teleport(state.location.x, state.location.y, state.location.z);
		}
		
		if(p.ml.display.window.setupMediaWindow)
			p.ml.display.window.chkbxOrientationMode.setSelected(newState);
	}
	
	/**
	 * @param front Restrict to media in front
	 * @param threshold Minimum number of media to for method to return true
	 * @return Whether any media are visible and in front of camera
	 */
	public boolean mediaAreVisible( boolean front, int threshold )
	{
		ArrayList<WMV_Cluster> nearClusters = getNearClusters(10, settings.farViewingDistance + worldSettings.defaultFocusDistance); 	

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
		
		for(WMV_Cluster cluster : nearClusters)
		{
			for( int id : cluster.getState().images )
			{
				WMV_Image i = p.getCurrentField().getImage(id);
				if(!i.getMediaState().disabled)
				{
					if( i.getViewingDistance(this) < settings.farViewingDistance + i.getFocusDistance() && 
						i.getViewingDistance(this) > settings.nearClippingDistance * 2.f )		// Find images in range
						closeImages.add(i);							
				}
			}

			for( int id : cluster.getState().panoramas )
			{
				WMV_Panorama n = p.getCurrentField().getPanorama(id);
				if(!n.getMediaState().disabled)
				{
					float captureDistance = n.getCaptureLocation().dist(getLocation());
					if( captureDistance < settings.farViewingDistance + worldSettings.defaultFocusDistance &&
							captureDistance > settings.nearClippingDistance * 2.f )		// Find images in range
						closePanoramas.add(n);							
				}
			}

			for( int id : cluster.getState().videos )
			{
				WMV_Video v = p.getCurrentField().getVideo(id);
				if(!v.getMediaState().disabled)
				{
					if( v.getViewingDistance(this) <= settings.farViewingDistance + v.getFocusDistance() &&
					    v.getViewingDistance(this) > settings.nearClippingDistance * 2.f )		// Find videos in range
						closeVideos.add(v);							
				}
			}

			for( int id : cluster.getState().sounds )
			{
				WMV_Sound s = p.getCurrentField().getSound(id);
				if(!s.getMediaState().disabled)
				{
					float captureDistance = s.getCaptureLocation().dist(getLocation());
					if( captureDistance <= settings.farViewingDistance + worldSettings.defaultFocusDistance &&
						captureDistance > settings.nearClippingDistance * 2.f )		// Find videos in range
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
						if(debug.viewer && debug.detailed)
							p.ml.systemMessage("Image:"+i.getID()+" result:"+result+" is less than centeredAngle:"+settings.centeredAngle);
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
			WMV_Waypoint curWaypoint = new WMV_Waypoint(path.size(), -1, 0, getLocation(), getGPSLocation(), getAltitude(), null);		// -- Use simulation time instead of null!!
			curWaypoint.setOrientation(getOrientationAtCluster());
			curWaypoint.setClusterID(state.currentCluster);						// Need to make sure camera is at current cluster!
			
			while(memory.size() > 100)								// Prevent memory path from getting too long
				memory.remove(0);
				
			memory.add(curWaypoint);
			if(p.getSettings().screenMessagesOn)
				p.ml.display.message(p.ml, "Saved Viewpoint to Memory.  Path Length:"+memory.size()+"...");
			
			if(debug.viewer) p.ml.systemMessage("Saved Viewpoint to Memory... "+curWaypoint.getWorldLocation()+" Path length:"+memory.size());
		}
		else if(debug.viewer) p.ml.systemMessage("Couldn't add memory point... walking? "+state.walking+" teleporting?"+state.teleporting+" velocity.mag():"+state.velocity.mag());
	}
	
	/**
	 * Clear the current memory
	 */
	public void clearMemory()
	{
		if(isFollowing() && getPathNavigationMode() == 2)
			stopFollowing();
		
		memory = new ArrayList<WMV_Waypoint>();
		if(p.getSettings().screenMessagesOn)
			p.ml.display.message(p.ml, "Cleared Memory...");
	}

	/**
	 * Stop navigation along points in memory
	 */
	public void stopFollowing()
	{
		if(state.following)
		{
			state.following = false;
			state.waiting = false;
			state.pathLocationIdx = 0;
//			state.continueAtAttractor = false;
			settings.pathWaitLength = settings.pathWaitLengthInit;	// Reset path wait length

			if(p.ml.display.window.setupNavigationWindow)
				p.ml.display.window.chkbxPathFollowing.setSelected(false);
		}
	}
	
	/**
	 * Stop following GPS track
	 */
	public void stopFollowingGPSTrack()
	{
		if(state.followingGPSTrack)
		{
			state.followingGPSTrack = false;
			state.gpsTrackLocationIdx = 0;
		}
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
			if(debug.viewer) p.ml.systemMessage("Viewer.moveToFirstTimeSegment()... Moving to first time segment on any date");
			moveToTimeSegmentInField(getCurrentFieldID(), 0, true, true);		// Move to first time segment in field
			return true;
		}		
		else
		{
			if(debug.viewer) p.ml.systemMessage("Viewer.moveToFirstTimeSegment()... Moving to first time segment on first date");
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
					if(count > p.getCurrentField().getDateline().size()) 
						break;
				}
			}
			if(success)
			{
				if(debug.viewer && debug.detailed) p.ml.systemMessage("Viewer.moveToFirstTimeSegment()... Will move to first time segment on date "+newDate+" state.currentFieldTimeSegmentOnDate:"+state.currentFieldTimeSegmentOnDate+" state.currentFieldDate:"+state.currentFieldDate);
				int curFieldTimeSegment = p.getCurrentField().getTimeSegmentOnDate(state.currentFieldTimeSegmentOnDate, state.currentFieldDate).getFieldTimelineID();
				moveToTimeSegmentInField(getCurrentFieldID(), curFieldTimeSegment, true, true);		// Move to first time segment in field
			}
			else if(debug.viewer)
				p.ml.systemMessage("Viewer.moveToFirstTimeSegment()... Couldn't move to first time segment...");
			
			return success;
		}
	}
	
	/**
	 * Start viewing specified media in (2D) Media View
	 * @param mediaType Media type
	 * @param mediaID Media ID
	 */
	public void startViewingMedia(int mediaType, int mediaID)
	{
		boolean view = false;
		switch(mediaType)
		{
			case 0:
				if(mediaID < p.getCurrentFieldImages().size())
				{
					viewImage(mediaID);
					view = true;
				}
				break;
			case 1:
				if(mediaID < p.getCurrentFieldPanoramas().size())
				{
					viewPanorama(mediaID);
					view = true;
				}
				break;
			case 2:
				if(mediaID < p.getCurrentFieldVideos().size())
				{
					viewVideo(mediaID);
					view = true;
				}
				break;
		//	case 3:
		//		viewSound(selected.get(0));
		//		break;
		}
		
		if(view) p.ml.display.setDisplayView(p, 4);			// Set current view to Media Display View if mediaID and type are valid
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
			p.ml.display.setDisplayView(p, 4);			// Set current view to Media Display View
		}
		else if(selected.size() > 1)
			p.ml.systemMessage("Viewer.startViewingSelectedMedia()... More than 1 media selected!");
		else
			p.ml.systemMessage("Viewer.startViewingSelectedMedia()... No media selected!");
	}
	
	/**
	 * Exit Media View and return to previous Display View
	 */
	public void exitMediaView()
	{
//		p.ml.systemMessage("Viewer.exitMediaView()...");
		p.ml.display.setDisplayView(p, getLastDisplayView());	// Return to previous Display View
	}
	
	public void viewImage(int id)
	{
		p.ml.display.setMediaViewItem(0, id);
	}
	
	public void viewPanorama(int id)
	{
		p.ml.display.setMediaViewItem(1, id);
	}
	
	public void viewVideo(int id)
	{
		p.ml.display.setMediaViewItem(2, id);
	}

	public void viewSound(int id)
	{
		p.ml.display.setMediaViewItem(3, id);
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
		for(WMV_Image i : p.getCurrentField().getImages())
		{
			if(!i.getMediaState().disabled)
				if(i.getViewingDistance(this) <= settings.selectionMaxDistance)
					possibleImages.add(i);
		}

		float closestImageDist = 100000.f;
		int closestImageID = -1;

		for(WMV_Image i : possibleImages)
		{
			if(!i.isBackFacing(getLocation()) && !i.isBehindCamera(getLocation(), getOrientationVector()))					// If image is ahead and front facing
			{
				float result = Math.abs(i.getFacingAngle(getOrientationVector()));				// Get angle at which it faces camera

				if(result < closestImageDist)										// Find closest to camera orientation
				{
					closestImageDist = result;
					closestImageID = i.getID();
				}
			}
		}

		ArrayList<WMV_Video> possibleVideos = new ArrayList<WMV_Video>();
		for(WMV_Video v : p.getCurrentField().getVideos())
		{
			if(!v.getMediaState().disabled)
				if(v.getViewingDistance(this) <= settings.selectionMaxDistance)
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

		ArrayList<WMV_Sound> possibleSounds = new ArrayList<WMV_Sound>();
		for(WMV_Sound s : p.getCurrentField().getSounds())
		{
			if(!s.getMediaState().disabled)
				if(s.getViewingDistance(this) <= settings.selectionMaxDistance)
					possibleSounds.add(s);
		}

		float closestSoundDist = 100000.f;
		int closestSoundID = -1;

		for(WMV_Sound s : possibleSounds)
		{
			float result = s.getCaptureLocation().dist(getLocation());
			if(result < closestSoundDist)								// Find closest to camera orientation
			{
				closestSoundDist = result;
				closestSoundID = s.getID();
			}
		}

		if(settings.selection)						// In Selection Mode
		{
			if(select && !settings.multiSelection)
				p.deselectAllMedia(false);				// If selecting media, deselect all media unless in Multi Selection Mode

			if(closestImageDist < closestVideoDist && closestImageDist < closestSoundDist && closestImageID != -1)	// Image closer than video
			{
				if(debug.viewer) p.ml.systemMessage("Selected image in front: "+closestImageID);

				if(settings.groupSelection)											// Segment selection
				{
					int segmentID = -1;
					WMV_Cluster c = p.getCurrentField().getCluster( p.getCurrentField().getImage(closestImageID).getAssociatedClusterID() );
					for(WMV_MediaSegment m : c.segments)
					{
						if(m.getImages().contains(closestImageID))
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
						{
							WMV_Image img = p.getCurrentField().getImage(i);
							if(img.getAssociatedVideo() == -1)							// Select image, if not a video placeholder
							{
								img.setSelected(select);
							}
							else														// Select associated video, if image is a placeholder
							{
								WMV_Video vid = p.getCurrentField().getVideo(img.getAssociatedVideo());
								vid.setSelected(select);
							}
						}
					}
				}
				else												// Single image selection
					p.getCurrentField().getImage(closestImageID).setSelected(select);
			}
			else if(closestVideoDist < closestImageDist && closestVideoDist < closestSoundDist && closestVideoID != -1)	// Video closer than image
			{
				if(debug.viewer) 	p.ml.systemMessage("Selected video in front: "+closestVideoID);
				p.getCurrentField().getVideo(closestVideoID).setSelected(select);
			}
			else if(closestSoundDist < closestImageDist && closestSoundDist < closestVideoDist && closestSoundID != -1)	// Video closer than image
			{
				if(debug.viewer) 	p.ml.systemMessage("Selected sound in front: "+closestSoundID);
				p.getCurrentField().getSound(closestSoundID).setSelected(select);
			}
		}
		else if(closestVideoDist != 100000.f)					// In Normal Mode
		{
			WMV_Video v = p.getCurrentField().getVideo(closestVideoID);

			if(!v.isPlaying())									// Play video by choosing it
			{
				if(!v.isLoaded()) v.loadMedia(p.ml);
				v.play(p.ml);
			}
			else
				v.stopVideo();
			
			if(debug.viewer) 
				p.ml.systemMessage("Video is "+(v.isPlaying()?"playing":"not playing: ")+v.getID());
		}
	}
	
	/**
	 * Choose panorama near the viewer
	 * @param select Whether to select or deselect
	 */
	public void choosePanoramaNearby(boolean select)
	{
		float closestPanoramaDist = 100000.f;
		int closestPanoramaID = -1;

		for(WMV_Panorama i : p.getCurrentField().getPanoramas())
		{
			if(!i.getMediaState().disabled)
			{
				float result = i.getViewingDistance(this);
				if(result <= settings.selectionMaxDistance && result < closestPanoramaDist)
				{
					closestPanoramaDist = result;
					closestPanoramaID = i.getID();
				}
			}
		}
		
		if(settings.selection)						// In Selection Mode
		{
			if(closestPanoramaDist != 100000.f)
			{
				int newSelected = closestPanoramaID;
				if(select && !settings.multiSelection)
					p.deselectAllMedia(false);				// If selecting media, deselect all media unless in Multi Selection Mode

				if(newSelected != -1)
				{
					p.getCurrentField().getPanorama(newSelected).setSelected(select);
					if(debug.panorama)
						p.ml.systemMessage("choosePanoramaNearby()... Selected #"+newSelected);
				}
			}
			else
			{
				if(debug.panorama)
					p.ml.systemMessage("choosePanoramaNearby()... No panoramas nearby...");
			}
		}
	}

	/**
	 * Set nearby cluster timeline to given timeline
	 * @param newTimeline List of time segments
	 */
	void setNearbyClusterTimeline(ArrayList<WMV_TimeSegment> newTimeline)
	{
		visibleClusterTimeline = newTimeline;
		state.nearbyClusterTimelineMediaCount = 0;
		
		for(WMV_TimeSegment t : visibleClusterTimeline)
			state.nearbyClusterTimelineMediaCount += t.getTimeline().size();
	}

	/**
	 * Create nearby cluster timeline from given clusters
	 * @param clusters List of clusters
	 */
	public void createNearbyClusterTimeline(ArrayList<WMV_Cluster> clusters)
	{
		ArrayList<WMV_TimeSegment> timeline = new ArrayList<WMV_TimeSegment>();
		
		if(debug.time)
			p.ml.systemMessage(">>> Creating Viewer Timeline (Nearby Visible Clusters)... <<<");

		for(WMV_Cluster c : clusters)											// Find all media cluster times
			for(WMV_TimeSegment t : c.getTimeline().timeline)
				timeline.add(t);

		timeline.sort(WMV_TimeSegment.WMV_TimeLowerBoundComparator);				// Sort time segments 
		visibleClusterTimeline = timeline;
	
		state.nearbyClusterTimelineMediaCount = 0;
		
		for(WMV_TimeSegment t : visibleClusterTimeline)
			state.nearbyClusterTimelineMediaCount += t.getTimeline().size();

		if(debug.time)
			p.ml.systemMessage("createNearbyClusterTimeline  nearbyClusterTimeline.size():"+visibleClusterTimeline.size());
	}

	/**
	 * Get nearby time by timeline index 
	 * @param timeSegmentIndex Index to find
	 * @return Nearby time associated with index
	 */
	public WMV_Time getNearbyTimeByIndex(int timeSegmentIndex)
	{
		WMV_Time time = null;
		for(WMV_TimeSegment ts : visibleClusterTimeline)
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
	 * Get ID of image nearest to the viewer in any direction
	 * @param inclCurrent Whether to include images in current cluster
	 * @return Nearest image ID
	 */
	private int getNearestImage(boolean inclCurrent) 
	{
		float smallest = 100000.f;
		int smallestIdx = -1;
		WMV_Field f = p.getCurrentField();

		for (int i = 0; i < f.getImages().size(); i++) {
			float imageDist = f.getImage(i).getViewingDistance(this);
			if (imageDist < smallest && imageDist > settings.nearClippingDistance) 
			{
				if(inclCurrent)
				{
					smallest = imageDist;
					smallestIdx = i;
				}
				else
				{
					if(f.getImage(i).getAssociatedClusterID() != getCurrentClusterID())
					{
						smallest = imageDist;
						smallestIdx = i;
					}
				}
			}
		}

		return smallestIdx;
	}
	
	/**
	 * Get ID of panorama nearest to the viewer in any direction
	 * @param inclCurrent Whether to include panoramas in current cluster
	 * @return Nearest panorama ID
	 */
	private int getNearestPanorama(boolean inclCurrent) 
	{
		float smallest = 100000.f;
		int smallestIdx = -1;
		WMV_Field f = p.getCurrentField();

		for (int i = 0; i < f.getPanoramas().size(); i++) 
		{
			float panoramaDist = f.getPanorama(i).getCaptureLocation().dist( getLocation() );
			if (panoramaDist < smallest && panoramaDist > settings.nearClippingDistance) 
			{
				if(inclCurrent)
				{
					smallest = panoramaDist;
					smallestIdx = i;
				}
				else
				{
					if(f.getPanorama(i).getAssociatedClusterID() != getCurrentClusterID())
					{
						smallest = panoramaDist;
						smallestIdx = i;
					}
				}
			}
		}

		return smallestIdx;
	}

	/**
	 * Get ID of video nearest to the viewer in any direction
	 * @param inclCurrent Whether to include videos in current cluster
	 * @return Nearest video ID
	 */
	private int getNearestVideo(boolean inclCurrent) 
	{
		float smallest = 100000.f;
		int smallestIdx = -1;
		WMV_Field f = p.getCurrentField();

		for (int i = 0; i < f.getVideos().size(); i++) 
		{
			float videoDist = f.getVideo(i).getViewingDistance(this);
			if (!f.getVideo(i).isDisabled() && videoDist < smallest && videoDist > settings.nearClippingDistance) 
			{
				if(inclCurrent)
				{
					smallest = videoDist;
					smallestIdx = i;
				}
				else
				{
					if(f.getVideo(i).getAssociatedClusterID() != getCurrentClusterID())
					{
						smallest = videoDist;
						smallestIdx = i;
					}
				}
			}
		}
		return smallestIdx;
	}

	/**
	 * Get ID of sound nearest to the viewer in any direction
	 * @param inclCurrent Whether to include sounds in current cluster
	 * @return Nearest sound ID
	 */
	private int getNearestSound(boolean inclCurrent) 
	{
		float smallest = 100000.f;
		int smallestIdx = -1;
		WMV_Field f = p.getCurrentField();

		for (int i = 0; i < f.getSounds().size(); i++) 
		{
			float soundDist = f.getSound(i).getCaptureLocation().dist(getLocation());
//			float soundDist = f.getSound(i).getCaptureDistance();
			p.ml.systemMessage("Viewer.getNearestSound()... id #"+i+" soundDist:"+soundDist);
			
			if (soundDist < smallest) 
			{
				if(inclCurrent)
				{
					smallest = soundDist;
					smallestIdx = i;
				}
				else
				{
					if(f.getSound(i).getAssociatedClusterID() != getCurrentClusterID())
					{
						smallest = soundDist;
						smallestIdx = i;
						p.ml.systemMessage("Viewer.getNearestSound()... found smallestIdx:"+smallestIdx);

					}
					else
					{
						p.ml.systemMessage("Viewer.getNearestSound()... Smallest is at current cluster:"+getCurrentClusterID());

					}
				}
			}
		}
		p.ml.systemMessage("Viewer.getNearestSound()... result: smallestIdx:"+smallestIdx);

		return smallestIdx;
	}
	
	/**
	 * Set current cluster and current time segment
	 * @param newCluster New current cluster
	 * @param newFieldTimeSegment New time segment to set (-1 to ignore this parameter)
	 */
	void setCurrentCluster(int newCluster, int newFieldTimeSegment)
	{
		if(newCluster >= 0 && newCluster < p.getCurrentField().getClusters().size())
		{
			state.lastCluster = state.currentCluster;
			WMV_Cluster c = p.getCurrentCluster();

			if(c != null) c.getState().timeFading = false;
			
			state.currentCluster = newCluster;
			c = p.getCurrentCluster();
			
			if(debug.viewer && debug.detailed) 
				p.ml.systemMessage("viewer.setCurrentCluster() to "+newCluster+" at field time segment "+newFieldTimeSegment+"  cluster location:"+c.getLocation()+" viewer location:"+getLocation());
			
			if(c != null)
			{
				if(p.state.timeFading && !c.getState().timeFading) 		// If Time Fading is on, but cluster isn't time fading
					c.getState().timeFading = true;						// Set cluster timeFading to true

				WMV_Field f = p.getCurrentField();
				if(newFieldTimeSegment == -1)							// If == -1, search for time segment
				{
					for(WMV_TimeSegment t : f.getTimeline().timeline)			// Search field timeline for cluster time segment
					{
						if(c.getTimeline() != null)
						{
							if(t.getFieldTimelineID() == f.getTimeSegmentInCluster(c.getID(), 0).getFieldTimelineID())			// Compare cluster time segment to field time segment
								setCurrentFieldTimeSegment(t.getFieldTimelineID(), true);
						}
						else p.ml.systemMessage("Current Cluster timeline is NULL!:"+c.getID());
					}
				}
				else
				{
					setCurrentFieldTimeSegment(newFieldTimeSegment, true);
					state.movingToTimeSegment = false;
				}

				if(worldState.getTimeMode() == 2 && !state.teleporting)
					p.createTimeCycle();													// Update time cycle for new cluster
				
				if(c.getViewerDistance() < p.settings.clusterCenterSize) 
					state.atCurrentCluster = true;											// Viewer is at current cluster
			}
			else
			{
				if(debug.viewer) p.ml.systemMessage("New current cluster is null!");
			}
		}
		else
		{
			if(newCluster == -1)
			{
				state.currentCluster = newCluster;
				if(debug.viewer) p.ml.systemMessage("Set currentCluster to -1...");
			}
			else
			{
				if(debug.viewer) p.ml.systemMessage("New cluster "+newCluster+" is invalid!");
			}
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
		
//		if(debugSettings.viewer && debugSettings.detailed) p.ml.systemMessage("setCurrentFieldTimeSegment()... "+newCurrentFieldTimeSegment+" current state.currentFieldTimeSegmentOnDate:"+state.currentFieldTimeSegmentOnDate+" getLocation().x:"+getLocation().x);
		
		if(updateTimelinesSegment)
		{
			if(state.currentFieldTimeSegment != -1)
			{
				int newFieldDate = p.getCurrentField().getTimeline().timeline.get(state.currentFieldTimeSegment).getFieldDateID();
				int newFieldTimelinesSegment = p.getCurrentField().getTimeline().timeline.get(state.currentFieldTimeSegment).getFieldTimelineIDOnDate();
				success = setCurrentTimeSegmentAndDate(newFieldTimelinesSegment, newFieldDate, false);
			}
			else
				success = false;
		}
		
		if(state.currentFieldTimeSegment >= 0 && state.currentFieldTimeSegment < p.getCurrentField().getTimeline().timeline.size())
			return success;
		else
		{
			if(debug.viewer && debug.detailed)
				p.ml.systemMessage("Couldn't set newCurrentFieldTimeSegment... currentField.getTimeline().timeline.size():"+p.getCurrentField().getTimeline().timeline.size());
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
		if(p.getCurrentField().getTimelines() != null)
		{
			if(p.getCurrentField().getTimelines().size() > 0 && p.getCurrentField().getTimelines().size() > state.currentFieldDate)
			{
//				if(debugSettings.viewer && debugSettings.detailed)
//					p.ml.systemMessage("setCurrentFieldTimeSegmentOnDate()... "+newCurrentFieldTimeSegmentOnDate+" currentFieldDate:"+state.currentFieldDate+" currentField.getTimelines().get(currentFieldDate).size():"+p.getCurrentField().getTimelines().get(state.currentFieldDate).timeline.size()+" getLocation():"+getLocation()+" current field:"+getField());
			}
			else 
			{
				p.ml.systemMessage("setCurrentFieldTimeSegmentOnDate() Error.. currentField.getTimelines().size() == "+p.getCurrentField().getTimelines().size()+" but currentFieldDate == "+state.currentFieldDate+"...");
				return false;
			}
		}
		else
		{
			p.ml.systemMessage("setCurrentFieldTimeSegmentOnDate() currentField.getTimelines() == null!!!");
			return false;
		}
	
		state.currentFieldTimeSegmentOnDate = newCurrentFieldTimeSegmentOnDate;
		p.ml.display.updateCurrentSelectableTimeSegment = true;

		if(state.currentFieldDate < p.getCurrentField().getTimelines().size())
		{
			if(p.getCurrentField().getTimelines().get(state.currentFieldDate).timeline.size() > 0 && state.currentFieldTimeSegmentOnDate < p.getCurrentField().getTimelines().get(state.currentFieldDate).timeline.size())
			{
				if(updateTimelineSegment)
				{
					int fieldTimelineID = p.getCurrentField().getTimelines().get(state.currentFieldDate).timeline.get(state.currentFieldTimeSegmentOnDate).getFieldTimelineID();
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
	 * Set model far viewing distance
	 * @param newModelDistanceVisibilityFactorFar New far viewing distance
	 */
	public void setModelFarViewingDistance( float newModelDistanceVisibilityFactorFar )
	{
		p.state.modelDistanceVisibilityFactorFar = newModelDistanceVisibilityFactorFar;
//		settings.farViewingDistance = newFarViewingDistance;
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
	
	/**
	 * Reset time state
	 */
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

	public void setClusterDistanceVisibilityFactor(float newValue)
	{
		settings.clusterDistanceVisibilityFactor = newValue;
	}

	public float getClusterDistanceVisibilityFactor()
	{
		return settings.clusterDistanceVisibilityFactor;
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
	 * Set selected GPS Track ID
	 * @param gpsTrackID New GPS track path ID
	 */
	public void selectGPSTrackID(int gpsTrackID)
	{
		if(state.gpsTrackID != gpsTrackID)
		{
			state.gpsTrackID = gpsTrackID;
			gpsTrack = p.getCurrentField().getGPSTracks().get(state.gpsTrackID);	// Set viewer GPS track from selection
			if(p.ml.display.initializedMaps)
			{
				if( !p.ml.display.map2D.createdGPSMarker)
				{
//					p.ml.display.map2D.createMarkers(p);
					p.ml.display.map2D.initialize(p);
				}
			}
			if(p.ml.display.window.setupNavigationWindow)
			{
				p.ml.display.window.chkbxPathFollowing.setEnabled(true);
			}
		}
	}

	/**
	 * @return Selected GPS Track ID in list of GPS tracks for field
	 */
	public int getSelectedGPSTrackID()
	{
		return state.gpsTrackID;
	}
	
	/**
	 * @return List of waypoints representing selected GPS track path
	 */
	public ArrayList<WMV_Waypoint> getGPSTrack()
	{
		return gpsTrack;
	}

	/**
	 * Set viewer frame count
	 * @param newFrameCount New frame count
	 */
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

	public ArrayList<WMV_TimeSegment>getNearbyClusterTimeline()
	{
		return visibleClusterTimeline;
	}
	
	/**
	 * Get number of media in nearby cluster timeline
	 * @return Nearby cluster timeline media count 
	 */
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
		if(attractor >= 0 && attractor < p.getCurrentField().getClusters().size())
		{
			WMV_Cluster c = p.getCurrentField().getCluster(attractor);
			return c;
		}
		else return null;
	}

	/**
	 * Get current attractor point
	 * @return Cluster representing current attractor point
	 */
	public WMV_Cluster getAttractorPoint()
	{
		return attractorPoint;
	}

	/**
	 * Set current field ID
	 * @param newFieldID New current field ID
	 */
	public void setCurrentFieldID(int newFieldID)
	{
		state.setCurrentFieldID( newFieldID );
	}

	/**
	 * @return Current field ID
	 */
	public int getCurrentFieldID()
	{
		return state.getCurrentFieldID();
	}
	
	/**
	 * Set viewer location
	 * @param newLocation New viewer location
	 * @param update Whether to update current cluster
	 */
	public void setLocation(PVector newLocation, boolean update)
	{
		if(settings.orientationMode)
			state.location = new PVector(newLocation.x, newLocation.y, newLocation.z);
		else
		{
			jumpTo(newLocation, update);
			state.location = getLocation();										// Update to precise camera location
		}
	}

	/**
	 * Get current viewer location in field
	 * @return Viewer virtual location
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
	 * Get viewer GPS location in format: {longitude, latitude}
	 * @return Current GPS location
	 */
	public PVector getGPSLocation()
	{
		PVector vLoc = getLocation();
		WMV_ModelState m = p.getCurrentField().getModel().getState();
		
		float newX = PApplet.map( vLoc.x, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, m.lowLongitude, m.highLongitude ); 			// GPS longitude decreases from left to right
		float newY = PApplet.map( vLoc.z, -0.5f * m.fieldLength, 0.5f*m.fieldLength, m.highLatitude, m.lowLatitude ); 			// GPS latitude increases from bottom to top; negative to match P3D coordinate space

		return new PVector(newX, newY);
	}

	/**
	 * Get viewer GPS location in format: {longitude, altitude, latitude}
	 * @return Current GPS location with altitude component
	 */
	public PVector getGPSLocationWithAltitude()
	{
		PVector vLoc = getLocation();
		WMV_ModelState m = p.getCurrentField().getModel().getState();
		
		float newX = PApplet.map( vLoc.x, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, m.lowLongitude, m.highLongitude ); 			// GPS longitude decreases from left to right
		float newY = getAltitude();																								// Altitude
		float newZ = PApplet.map( vLoc.z, -0.5f * m.fieldLength, 0.5f*m.fieldLength, m.highLatitude, m.lowLatitude ); 			// GPS latitude increases from bottom to top; negative to match P3D coordinate space

		return new PVector(newX, newY);
	}
	
	/**
	 * Get current viewer altitude in meters
	 * @return Current altitude
	 */
	public float getAltitude()
	{
		return p.utilities.getAltitude(getLocation());
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
	public PVector getAttractionVector()
	{
		return state.attraction;
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
	 * @return Current camera orientation as PVector in format: Yaw, Pitch, Roll
	 */
	public PVector getOrientation()
	{
		state.orientation = new PVector(camera.getAttitude()[0], camera.getAttitude()[1], camera.getAttitude()[2]);			// Update orientation
		return state.orientation;
	}

	/**
	 * @return Current camera orientation as PVector in format: Yaw, Pitch, Roll
	 */
	public WMV_Orientation getOrientationAtCluster()
	{
		state.orientation = new PVector(camera.getAttitude()[0], camera.getAttitude()[1], camera.getAttitude()[2]);			// Update orientation
		WMV_Orientation orientation = new WMV_Orientation(state.currentCluster, state.orientation.x, state.orientation.y, state.orientation.z);
		return orientation;
	}
	
	/**
	 * Get current Show Invisible Models setting, i.e. whether to show models for images and videos not currently visible 
	 * @return Show Invisible Models setting state
	 */
	public boolean getShowInvisibleModels()
	{
		return settings.showInvisibleModels;
	}
	
	/**
	 * Set current Show Invisible Models setting, i.e. whether to show models for images and videos not currently visible 
	 * @return New Show Invisible Models setting state
	 */
	public void setShowInvisibleModels(boolean newState)
	{
		settings.showInvisibleModels = newState;
	}
	
	/**
	 * @return Index of last cluster
	 */
	public int getLastCluster()
	{
		return state.lastCluster;
	}
	
	public boolean getNavigationTeleport()
	{
		return state.navigationTeleport;
	}

	/**
	 * Get Path Navigation teleport setting
	 * @return Path teleport setting
	 */
	public boolean getFollowTeleport()
	{
		return state.followTeleport;
	}

	/**
	 * Set Navigation Teleport setting
	 * @param newNavigationTeleport New Navigation Teleport setting
	 */
	public void setNavigationTeleport(boolean newNavigationTeleport)
	{
		state.navigationTeleport = newNavigationTeleport;
		if(p.getSettings().screenMessagesOn)
		{
			if(state.navigationTeleport)
				p.ml.display.message(p.ml, "Navigation Teleporting ON");
			else
				p.ml.display.message(p.ml, "Navigation Teleporting OFF");
		}
	}

	/**
	 * @return Whether in Selection Mode
	 */
	public boolean inSelectionMode()
	{
		return settings.selection;
	}

	/**
	 * Set whether Selection Mode is enabled or disabled
	 * @param newSelection New Selection Mode state
	 */
	public void setSelection(boolean newSelection)
	{
		settings.selection = newSelection;
		
		if(p.ml.display.window.setupMediaWindow)
			p.ml.display.window.chkbxSelectionMode.setSelected(settings.selection);

		if(p.getSettings().screenMessagesOn)
			p.ml.display.message(p.ml, "Selection Mode "+(newSelection?"Enabled":"Disabled"));
		
		if(inSelectionMode())
		{
			if(p.ml.display.window.setupMediaWindow)
			{
				p.ml.display.window.setSelectionControlsEnabled(true);
			}
		}
		else
		{
			p.getCurrentField().deselectAllMedia(false);		// Deselect media if left Selection Mode
			if(p.ml.display.getDisplayView() == 4)
			{
				p.ml.display.setMediaViewItem(-1, -1);		// Reset current Media View object
				p.ml.display.setDisplayView(p, 0);			// Set Display View to World
			}
			if(p.ml.display.window.setupMediaWindow)
			{
				p.ml.display.window.setSelectionControlsEnabled(false);
			}
		}

		if(inSelectionMode() && getMultiSelection())
		{
			setMultiSelection( false, false );
			if(p.ml.display.window.setupMediaWindow)
				p.ml.display.window.chkbxMultiSelection.setSelected( false );
		}
		if(inSelectionMode() && getGroupSelection()) 
		{
			setGroupSelection( false, false );
			if(p.ml.display.window.setupMediaWindow)
				p.ml.display.window.chkbxSegmentSelection.setSelected( false );
		}
	}

	/**
	 * Set whether to select groups of overlapping media 
	 * @param newGroupSelection Whether to enable or disable Group Selection
	 * @param message Whether to send user message
	 */
	public void setGroupSelection(boolean newGroupSelection, boolean message)
	{
		settings.groupSelection = newGroupSelection;
		if(p.ml.display.window.setupMediaWindow)
			p.ml.display.window.chkbxSegmentSelection.setSelected(settings.groupSelection);
		if(p.getSettings().screenMessagesOn && message)
			p.ml.display.message(p.ml, "Group Selection Mode "+(newGroupSelection?"Enabled":"Disabled"));
	}

	/**
	 * @return Whether to select groups of overlapping media 
	 */
	public boolean getGroupSelection()
	{
		return settings.groupSelection;
	}
	
	/**
	 * Set whether to allow multiple selected media at same time
	 * @param newMultiSelection Whether to enable or disable Multi-Selection
	 * @param message Whether to send user message
	 */
	public void setMultiSelection(boolean newMultiSelection, boolean message)
	{
		settings.multiSelection = newMultiSelection;
		if(p.ml.display.window.setupMediaWindow)
			p.ml.display.window.chkbxMultiSelection.setSelected(settings.multiSelection);
		if(p.getSettings().screenMessagesOn && message)
			p.ml.display.message(p.ml, "Multiple Selection Mode "+(newMultiSelection?"Enabled":"Disabled"));
	}

	/**
	 * @return Whether to enable or disable multi-selection
	 */
	public boolean getMultiSelection()
	{
		return settings.multiSelection;
	}

	/**
	 * Set whether to force teleporting when in Path Mode
	 * @param newFollowTeleport
	 */
	public void setFollowTeleport(boolean newFollowTeleport)
	{
		state.followTeleport = newFollowTeleport;
		if(p.getSettings().screenMessagesOn)
		{
			if(newFollowTeleport)
				p.ml.display.message(p.ml, "Path Mode Teleporting ON...");
			else
				p.ml.display.message(p.ml, "Path Mode Teleporting OFF...");
		}
	}
	
	/**
	 * @param newFollowTeleport Current Path Navigation Mode {0: Timeline 1: GPS Track 2: Memory}
	 */
	public int getPathNavigationMode()
	{	
		return state.getFollowMode();
	}
	
	/**
	 * Set Path Navigation Mode
	 * @param newFollowMode New Path Navigation Mode {0: Timeline 1: GPS Track 2: Memory}
	 */
	public void setPathNavigationMode(int newFollowMode)
	{
		state.setFollowMode( newFollowMode );
		if(p.ml.display.window.setupNavigationWindow)
		{
			if(newFollowMode == 1)								// GPS Track
			{
//				System.out.println("Will enable chkbxPathFollowing?"+ (getSelectedGPSTrackID() == -1)+" 2");
				if(getSelectedGPSTrackID() == -1)
					p.ml.display.window.chkbxPathFollowing.setEnabled(false);	// Enable GPS Path Navigation if track is selected
				else
					p.ml.display.window.chkbxPathFollowing.setEnabled(true);	// Disable GPS Path Navigation if track is selected
			}
			else
				p.ml.display.window.chkbxPathFollowing.setEnabled(true);	// Disable GPS Path Navigation if track is selected
		}
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
		if(p.getSettings().screenMessagesOn)
			p.ml.display.message(p.ml, "Angle Fading "+(settings.angleFading?"ON":"OFF"));
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

	/**
	 * Set last Display View
	 * @param newLastDisplayView New value
	 */
	public void setLastDisplayView(int newLastDisplayView)
	{
		state.lastDisplayView = newLastDisplayView;
	}

	/**
	 * Get last Display View
	 * @return Last DisplayView 
	 */
	public int getLastDisplayView()
	{
		return state.lastDisplayView;
	}

	public void setAngleThinning(boolean newAngleThinning)
	{
		settings.angleThinning = newAngleThinning;
		if(p.getSettings().screenMessagesOn)
			p.ml.display.message(p.ml, "Angle Thinning "+(settings.angleThinning?"ON":"OFF"));
	}
	
	public void setUserBrightness( float newValue )
	{
		settings.userBrightness = newValue;
	}
	
	/**
	 * Set field of view. Note: Does not affect camera!
	 * Use zoomToFieldOfView() to set both the viewer and camera field of view values
	 * @param newFieldOfView
	 */
	private void setFieldOfView( float newFieldOfView )
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
	}
	
	public void startMoveYTransition(int dir)
	{
		stopSlowing();
		state.moveYDirection = dir;
		state.movingY = true;
	}
	
	public void startMoveZTransition(int dir)
	{
		stopSlowing();
		state.moveZDirection = dir;
		state.movingZ = true;
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
	 * Stop immediately			// -- Unused
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
	
	public void setIgnoreTeleportGoal( boolean newState )
	{
		state.ignoreTeleportGoal = newState;
	}

	public boolean getIgnoreTeleportGoal()
	{
		return state.ignoreTeleportGoal;
	}

	/**
	 * Get current media (In Single Time Mode)
	 * @return Current media ID
	 */
	public int getCurrentMedia()
	{
		return state.currentMedia;
	}
	
	/**
	 * Set current media (In Single Time Mode)
	 * @param newCurrentMedia New current media ID
	 */
	public void setCurrentMedia( int newCurrentMedia )
	{
		state.currentMedia = newCurrentMedia;
	}
	
	/**
	 * Get current media start time (In Single Time Mode)
	 * @return Current media start time 
	 */
	public int getCurrentMediaStartTime()
	{
		return state.currentMediaStartTime;
	}
	
	/**
	 * Set current media start time (In Single Time Mode)
	 * @param newCurrentMediaStartTime New current media start time
	 */
	public void setCurrentMediaStartTime(int newCurrentMediaStartTime)
	{
		state.currentMediaStartTime = newCurrentMediaStartTime;
	}
	
	/**
	 * @return Next media start time (In Single Time Mode)
	 */
	public int getNextMediaStartTime()
	{
		return state.nextMediaStartFrame;
	}
	
	/**
	 * @return Whether viewer is following current media (In Single Time Mode)
	 */
	public boolean isFollowingCurrentMediaInTime()		// -- Disabled
	{
		return state.followCurrentMediaInTime;
	}
	
	public void setNextMediaStartTime(int newNextMediaStartFrame)
	{
		state.nextMediaStartFrame = newNextMediaStartFrame;
	}

	/**
	 * Turn to saved orientation for current cluster, if exists
	 */
//	private void turnToCurrentClusterOrientation()
//	{
//		WMV_Orientation o = state.getClusterOrientation(state.currentCluster);
//		
//		if(o != null) 
//		{
//			p.ml.systemMessage("Viewer.turnToCurrentClusterOrientation()... Found cluster #"+state.currentCluster+" orientation, x:"+o.getDirection()+" y:"+o.getElevation());
//			turnToOrientation(o);
//		}
//	}
	
	/**
	 * Get cluster along given vector
	 * @param clusterList Clusters to search through
	 * @param direction Directional vector of camera movement
	 * @return Cluster in the approximate direction of given vector from camera. If none within 30 degrees, returns currentCluster
	 */
//	private int getClusterAlongVector(ArrayList<WMV_Cluster> clusterList, PVector direction)
//	{
//		if(clusterList.size() == 0)
//			clusterList = p.getActiveClusters();
//		
//		IntList clustersAlongVector = new IntList();
//		
//		for (WMV_Cluster c : clusterList) 							// Iterate through the clusters
//		{
//			PVector clusterVector = getVectorToCluster(c);
//			PVector crossVector = new PVector();
//			PVector.cross(direction, clusterVector, crossVector);		// Cross vector gives angle between camera and image
//			float result = crossVector.mag();
//			
//			if(Math.abs(result) < PApplet.PI / 6.f && !c.isEmpty())
//			{
//				p.ml.systemMessage("Finding Distance of Centered Cluster:"+c.getID()+" at Angle "+result+" from History Vector...");
//				if(c.getID() != state.currentCluster)
//					clustersAlongVector.append(c.getID());
//			}
//			else
//			{
//				if(debugSettings.viewer && debugSettings.detailed)
//					p.ml.systemMessage("Cluster ID:"+c.getID()+" at angle "+result+" from camera..."+" NOT centered!");
//			}
//		}
//
//		float smallest = 100000.f;
//		int smallestIdx = 0;
//
//		for (int i = 0; i < clustersAlongVector.size(); i++) 		// Compare distances of clusters in front
//		{
//			PVector cPos = getLocation();
//			WMV_Cluster c = (WMV_Cluster) p.getCurrentField().getCluster(i);
//			if(debugSettings.viewer && debugSettings.detailed)
//				p.ml.systemMessage("Checking Centered Cluster... "+c.getID());
//		
//			float dist = PVector.dist(cPos, c.getLocation());
//			if (dist < smallest) 
//			{
//				smallest = dist;
//				smallestIdx = i;
//			}
//		}		
//		
//		if(clustersAlongVector.size() > 0)
//			return smallestIdx;
//		else
//		{
//			if(debugSettings.viewer && debugSettings.detailed)
//				p.ml.systemMessage("No clusters found along vector!");
//			return state.currentCluster;
//		}
//	}

	/* Disabled */

	/**
	 * Get vector of direction of camera motion by comparing current and previous waypoints
	 * @return Vector of direction of camera motion
	 */
//	private PVector getHistoryVector()
//	{
//		PVector hv = new PVector();
//		
//		if(history.size() > 1)
//		{
//			WMV_Waypoint w1 = history.get(history.size()-1);
//			WMV_Waypoint w2 = history.get(history.size()-2);
//			
////			float dist = w1.getDistance(w2);
//			
//			hv = new PVector(  w1.getWorldLocation().x-w2.getWorldLocation().x, 	//  Vector from the camera to the face.      
//					w1.getWorldLocation().y-w2.getWorldLocation().y, 
//					w1.getWorldLocation().z-w2.getWorldLocation().z   );			
//		}
//		
//		return hv;
//	}

	/**
	 * Check whether list waypoints are in history
	 * @param check Waypoints to look for
	 * @param historyDepth How far back in history to look
	 * @return Waypoints found in history within the last <memory> waypoints
	 */
//	private ArrayList<WMV_Waypoint> waypointsAreInHistory(ArrayList<WMV_Waypoint> check, int historyDepth)
//	{
//		ArrayList<WMV_Waypoint> found = new ArrayList<WMV_Waypoint>();
//		
//		for( WMV_Waypoint p : check )
//		{
//			for(int i = history.size()-1; i >= history.size()-historyDepth; i--)		// Iterate through history from last element to 
//			{
//				WMV_Waypoint w = history.get(i);
//				
//				if(p.getWorldLocation() == w.getWorldLocation())
//					found.add(p);
//			}
//		}
//		
//		return found;
//	}
	
	/**
	 * @param check List of clusters to check
	 * @param memory How far back to look in memory 
	 * @return Clusters found within the last <memory> waypoints
	 */
//	private ArrayList<WMV_Cluster> clustersAreInHistory(IntList check, int memory)
//	{
//		ArrayList<WMV_Cluster> found = new ArrayList<WMV_Cluster>();
//		
//		for( int cPoint : check )
//		{
//			p.ml.systemMessage("clustersInList()... memory:"+memory);
//
//			for(int i = history.size()-1; i >= history.size()-memory; i--)		// Iterate through history from last element to 
//			{
//				p.ml.systemMessage("i:"+i);
//				WMV_Waypoint w = history.get(i);
//				
//				if(p.getCurrentField().getCluster(cPoint).getLocation() == w.getWorldLocation())
//				{
//					found.add(p.getCurrentField().getCluster(cPoint));
//				}
//			}
//		}
//		
//		return found;
//	}
	
	/**
	 * Get ID of closest image in front of viewer
	 * @return ID of image closest to viewer in front
	 */
//	private int getFrontImage(boolean visible) {
//		float smallest = 100000.f;
//		int smallestIdx = 0;
//
//		WMV_Field f = p.getCurrentField();
//
//		for (int i = 0; i < f.getImages().size(); i++) 
//		{
//			if (f.getImage(i).getMediaState().visible) 
//			{
//				if(visible)
//				{
//					if(f.getVideo(i).getMediaState().visible)
//					{
//						float imageAngle = f.getImage(i).getFacingAngle(getOrientationVector());
//						if (imageAngle < smallest) 
//						{
//							smallest = imageAngle;
//							smallestIdx = i;
//						}
//					}
//				}
//				else
//				{
//					float imageAngle = f.getImage(i).getFacingAngle(getOrientationVector());
//					if (imageAngle < smallest) 
//					{
//						smallest = imageAngle;
//						smallestIdx = i;
//					}
//				}
//			}
//		}
//
//		return smallestIdx;
//	}
	
	/**
	 * Get ID of closest video in front of viewer
	 * @return ID of video closest to viewer in front
	 */
//	private int getFrontVideo(boolean visible) 
//	{
//		float smallest = 100000.f;
//		int smallestIdx = -1;
//
//		WMV_Field f = p.getCurrentField();
//
//		for (int i = 0; i < f.getVideos().size(); i++) 
//		{
//			if (!f.getVideo(i).isDisabled()) 
//			{
//				if(visible)
//				{
//					if(f.getVideo(i).getMediaState().visible)
//					{
//						float videoAngle = f.getVideo(i).getFacingAngle(getOrientationVector());
//						if (videoAngle < smallest) 
//						{
//							smallest = videoAngle;
//							smallestIdx = i;
//						}
//					}
//				}
//				else
//				{
//					float videoAngle = f.getVideo(i).getFacingAngle(getOrientationVector());
//					if (videoAngle < smallest) 
//					{
//						smallest = videoAngle;
//						smallestIdx = i;
//					}
//				}
//			}
//		}
//
//		return smallestIdx;
//	}
}
