package main.java.com.entoptic.metaVisualizer.world;

import java.util.ArrayList;
import java.util.List;

import main.java.com.entoptic.metaVisualizer.media.WMV_Image;
import main.java.com.entoptic.metaVisualizer.media.WMV_MediaSegment;
import main.java.com.entoptic.metaVisualizer.media.WMV_Panorama;
import main.java.com.entoptic.metaVisualizer.media.WMV_Sound;
import main.java.com.entoptic.metaVisualizer.media.WMV_Video;
import main.java.com.entoptic.metaVisualizer.misc.MV_DebugSettings;
import main.java.com.entoptic.metaVisualizer.model.WMV_Cluster;
import main.java.com.entoptic.metaVisualizer.model.WMV_ModelState;
import main.java.com.entoptic.metaVisualizer.model.WMV_Orientation;
import main.java.com.entoptic.metaVisualizer.model.WMV_Time;
import main.java.com.entoptic.metaVisualizer.model.WMV_TimeSegment;
import main.java.com.entoptic.metaVisualizer.model.WMV_Waypoint;
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
	private MV_DebugSettings debug;					// Debug settings

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
	
	public WMV_World p;											// Parent world
	
	/**
	 * Constructor for viewer class
	 * @param parent Parent world
	 * @param newWorldSettings Current world settings
	 * @param newWorldState Current world state
	 * @param newDebugSettings Debug settings
	 */
	public WMV_Viewer(WMV_World parent, WMV_WorldSettings newWorldSettings, WMV_WorldState newWorldState, MV_DebugSettings newDebugSettings)
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
		
		camera = new WMV_Camera( p.mv, x, y, z, cX, cY, cZ, 0.f, 1.f, 0.f, settings.fieldOfView, 
								 settings.nearClippingDistance, settings.farClippingDistance );
		
//		camera = new WMV_Camera( p.ml, x, y, z, 0.f, 0.f, 0.f, 0.f, 1.f, 0.f, settings.fieldOfView, settings.nearClippingDistance, 10000.f);
		mediaViewCamera = new WMV_Camera( p.mv, 0.f, 0.f, 500.f, 0.f, 0.f, 0.f, 0.f, 1.f, 0.f, (float)Math.PI / 3.f, settings.nearClippingDistance, 10000.f);

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
		
		camera = new WMV_Camera( p.mv, x, y, z, cX, cY, cZ, 0.f, 1.f, 0.f, settings.fieldOfView, 
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
		if(debug.viewer) p.mv.systemMessage("Viewer.enterField()... Field id #"+fieldID);

		setCurrentField(fieldID, setState);					// Set new field and simulation state
		
		if(p.mv.display.getDisplayView() == 1)
			p.mv.display.map2D.initialize(p);				// Initialize map if in Map View
		
		if(p.mv.display.window.setupNavigationWindow)		// Reload field-specific navigation controls
			p.mv.display.window.reloadNavigationWindow();
		
		if(p.mv.display.getDisplayView() == 2)				// Zoom to timeline for new field
			p.mv.display.zoomToTimeline(p, true);
		
//		lookAtNearestMedia( p.getVisibleClusters(), !p.getState().timeFading );			// Look for images around the camera
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
		
		if( state.walking && p.mv.frameCount % 15 == 0)			/* Update current cluster while walking */
			updateCurrentCluster(false);						

		if(worldState.timeFading && worldState.getTimeMode() == 2)		// -- Disabled
		{
			if( isMoving() || isFollowing() || isWalking() )
			{
				if(worldState.frameCount % 5 == 0.f)
					p.createTimeCycle();
			}
			else if(worldSettings.timeCycleLength == -1 && worldState.frameCount % 5 == 0.f)	// Flag viewer to keep calling method until clusters are visible
			{
				p.createTimeCycle();
			}
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
	 * Update transition from one field time segment to another
	 */
	public void updateTimeTransition()
	{
		if(p.mv.frameCount >= state.timeTransitionEndFrame)
		{
			state.fadingToTime = false;
			state.setCurrentFieldTimeSegment( state.timeTransitionGoalID );
			
			if(p.mv.debug.time) 
				p.mv.systemMessage("Viewer.updateTimeTransition()... Reached End Time Point: "+state.timeTransitionGoalTimePoint);
			
			p.setCurrentTimeFromAbsolute(state.timeTransitionGoalTimePoint, true);
//			p.setCurrentTime(state.timeTransitionGoalTimePoint, true, true);
			
			if(p.mv.debug.time)
				p.mv.systemMessage("Viewer.updateTimeTransition()... New Current Time: "+p.getCurrentTime());

			state.timeTransitionStartID = -1;		 
			state.timeTransitionStartTimePoint = 0.f;		 
			state.timeTransitionGoalID = -1;				 
			state.timeTransitionGoalTimePoint = 0.f;				 
			state.timeTransitionStartFrame = -1;
		}
		else
		{
			float startTimePoint, endTimePoint;
			
			startTimePoint = state.timeTransitionStartTimePoint;
			endTimePoint = state.timeTransitionGoalTimePoint;
			
			float timePoint = p.utilities.mapValue( p.mv.frameCount, state.timeTransitionStartFrame, state.timeTransitionEndFrame, 
												   startTimePoint, endTimePoint );	
			
			p.setCurrentTimeFromAbsolute(timePoint, true);
//			p.setCurrentTime(timePoint, true, true);
		}
	}
	
	/**
	 * Update user (keyboard) time transition
	 */
	public void updateUserTimeTransition()
	{
		System.out.println("Viewer.updateUserTimeTransition()... state.fadingFieldTime:"+state.fadingFieldTime+" state.fadingClusterTime:"+state.fadingClusterTime);
		if(state.fadingFieldTime)
		{
			float timePoint = p.getCurrentFieldTime() + state.fadingTimeInc * state.fadingFieldTimeDirection;
			p.setCurrentFieldTime(timePoint, true);					// Set current Field Time Point
		}
		else if(state.fadingClusterTime)
		{
			float timePoint = p.getCurrentClusterTime() + state.fadingTimeInc * state.fadingClusterTimeDirection;
			p.setCurrentClusterTime(timePoint, true);				// Set current Cluster Time Point
		}
	}
	
	/**
	 * Update transition from one field time segment to another
	 */
	public void updateClusterTimeTransition()
	{
		if(p.mv.frameCount >= state.clusterTimeTransitionEndFrame)
		{
			state.fadingToClusterTime = false;
			
			if(p.mv.debug.time) 
				p.mv.systemMessage("Viewer.updateClusterTimeTransition()... Reached End Time Point: "+state.clusterTimeTransitionGoalTimePoint);
			
//			p.setCurrentTimeFromAbsolute(state.clusterTimeTransitionGoalTimePoint, true);
			p.setCurrentClusterTime(state.clusterTimeTransitionGoalTimePoint, true);			// Set current time from cluster
			
			if(p.mv.debug.time)
				p.mv.systemMessage("Viewer.updateClusterTimeTransition()... New Current Time: "+p.getCurrentTime());

			state.clusterTimeTransitionStartTimePoint = 0.f;		 
			state.clusterTimeTransitionGoalTimePoint = 0.f;				 
			state.clusterTimeTransitionStartFrame = -1;
		}
		else
		{
			float startTimePoint, endTimePoint;
			
			startTimePoint = state.clusterTimeTransitionStartTimePoint;
			endTimePoint = state.clusterTimeTransitionGoalTimePoint;
			
			float timePoint = p.utilities.mapValue( p.mv.frameCount, state.clusterTimeTransitionStartFrame, state.clusterTimeTransitionEndFrame, 
												   startTimePoint, endTimePoint );	
			
//			p.setCurrentTimeFromAbsolute(timePoint, true);
			p.setCurrentClusterTime(timePoint, true);										// Set current time from cluster	
		}
	}
	
	/**
	 * Set time point based on current Time Mode
	 * @param newTimePoint
	 */
//	public void setCurrentTime(float newTimePoint)
//	{
//		switch(state.timeMode)
//		{
//			case 0:													// Cluster Time Mode
//				for(WMV_Cluster c : getVisibleClusters())
//					if(c.getState().timeFading)
//						c.setTimePoint(newTimePoint);
//				break;
//			
//			case 1:													// Field Time Mode
//				setFieldTimePoint(newTimePoint);
//				break;
//
//			case 2:													// (Single) Media Time Mode
//				break;
//				
//			case 3:													// Flexible Time Mode -- In progress
//				break;
//		}
//	}
	
	/**
	 * Move to the given image capture location
	 * @imageID Specified image
	 * @param teleport  Whether to teleport (true) or navigate (false)
	 */
	void moveToImageCaptureLocation(int imageID, boolean teleport) 
	{
		if (debug.viewer)
			p.mv.systemMessage("Moving to capture location... "+imageID);

		PVector newLocation = p.getCurrentFieldImages().get(imageID).getCaptureLocation();
		
		if(teleport)
		{
			teleportToPoint(newLocation, true, true);
		}
		else
		{
			if(debug.viewer && debug.detailed)
				p.mv.systemMessage("moveToCaptureLocation... setting attractor point:"+newLocation);
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
			if(debug.viewer) p.mv.systemMessage("Moving to cluster... setting attractor:"+newCluster);
			setAttractorCluster( newCluster, true );
		}
	}
		
	/**
	 * Go to the nearest cluster
	 * @param teleport  Whether to teleport (true) or move (false)
	 */
	public void moveToNearestCluster(boolean teleport) 
	{
		int nearest = getNearestCluster(false);		
		
		if (debug.viewer)
			p.mv.systemMessage("Moving to nearest cluster... "+nearest+" from current:"+state.currentCluster);

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
				p.mv.systemMessage("moveToNearestClusterAhead goal:"+ahead);

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
				p.mv.systemMessage("Viewer.moveToNearestClusterAhead()... can't move to same cluster!... "+ahead);
		}
	}
	

	/**
	 * Move camera to the last cluster
	 * @param teleport  Whether to teleport (true) or move (false)
	 */
	public void moveToLastCluster(boolean teleport) 
	{
		if (debug.viewer)
			p.mv.systemMessage("Moving to last cluster... "+state.lastCluster);
		if(state.lastCluster > 0)
		{
			if(settings.teleportToFarClusters && !teleport)
			{
				state.teleportGoalClusterID = state.lastCluster;
				PVector newLocation = ((WMV_Cluster) p.getCurrentField().getCluster(state.lastCluster)).getLocation();
				if( PVector.dist(newLocation, getLocation()) > settings.farClusterTeleportDistance )
				{
					teleportToPoint(newLocation, true, true);
				}
				else
				{
					if(debug.viewer) p.mv.systemMessage("moveToLastCluster... setting attractor and currentCluster:"+state.currentCluster);
					setAttractorCluster( state.lastCluster, true );
				}
			}
			else
			{
				if(teleport)
				{
					state.teleportGoalClusterID = state.lastCluster;
					PVector newLocation = ((WMV_Cluster) p.getCurrentField().getCluster(state.lastCluster)).getLocation();
					teleportToPoint(newLocation, true, true);
				}
				else
				{
					if(debug.viewer) p.mv.systemMessage("moveToLastCluster... setting attractor and currentCluster:"+state.currentCluster);
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
		
		if(debug.viewer) p.mv.systemMessage("moveToNextCluster()... mediaType "+mediaType);

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
					p.mv.systemMessage("Error: Cluster marked empty but mediaPoints != 0!  clusterID:"+next);
			}

			if(iterationCount <= 3)				// If a cluster was found in 2 iterations
			{
				found = true;
				if(debug.viewer) p.mv.systemMessage("Moving to next cluster:"+next+" from current cluster:"+state.currentCluster);
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
			p.mv.systemMessage("Viewer.teleportToPoint("+dest+")...");
		
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
				jumpTo(dest, true, true);
		}
	}	
	
	/**
	 * Teleport immediately to given point
	 * @param dest Destination point
	 * @param updateCluster Whether to update current cluster
	 */
	public void jumpTo(PVector dest, boolean updateTarget, boolean updateCluster)
	{
		if(state.atCurrentCluster)
		{
			saveCurrentClusterOrientation();
			state.atCurrentCluster = false;
		}
		
		camera.teleport(dest.x, dest.y, dest.z, updateTarget);
		
		if(updateCluster)
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
		
		if(debug.viewer) p.mv.systemMessage("Viewer.moveToNearestClusterWithType()... mediaType "+mediaType+" inclCurrent:"+inclCurrent);

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
				if(debug.viewer) p.mv.systemMessage("Viewer.moveToNearestClusterWithType()... found media #:"+nearest);
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
				p.mv.systemMessage("No clusters with "+strMediaType+" found... result:"+result);
			if(p.getSettings().screenMessagesOn)
				p.mv.display.message(p.mv, "No clusters with "+strMediaType+" found...");
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
			if(debug.viewer) p.mv.systemMessage("Viewer.getNearestClusterWithType()... No media of type "+mediaType+" found...");
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
						p.mv.systemMessage("1  No media found...");
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
									p.mv.systemMessage("2 No media found...");
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
			if(debug.viewer) p.mv.systemMessage("Moving to next cluster with media type:"+mediaType+" cluster found:"+next+"... moving from current cluster:"+state.currentCluster);
			return next;
		}
		else
		{
			if(debug.viewer) p.mv.systemMessage("No media of type "+mediaType+" found...");
			return -1;
		}
	}

	/**
	 * Go to cluster corresponding to given time segment in field
	 * @param fieldID Field to move to
	 * @param fieldTimeSegment Index of time segment in field timeline to move to
	 * @param teleport Whether to teleport or move
	 */
	public void moveToTimeSegmentInField(int fieldID, int fieldTimeSegment, boolean teleport, boolean fade)
	{
		WMV_Field f = p.getField(fieldID);

		if(f.getTimeline().timeline.size()>0)
		{
			if(debug.viewer && debug.detailed)
				p.mv.systemMessage("Viewer.moveToTimeSegmentInField()... fieldID:"+fieldID+" fieldTimeSegment:"+fieldTimeSegment+" fieldTimelineID:"+f.getTimeline().timeline.get(fieldTimeSegment).getFieldTimelineID()+" f.getTimeline().size():"+f.getTimeline().timeline.size());
			
			int clusterID = f.getTimeline().timeline.get(fieldTimeSegment).getClusterID();
			
			if(debug.viewer)
				p.mv.systemMessage("Viewer.moveToTimeSegmentInField()...  Found clusterID:"+clusterID+" p.getCurrentField() cluster count:"+p.getCurrentField().getClusters().size());
			
			if( clusterID >= 0 && clusterID < f.getClusters().size() )
			{
				if( clusterID == state.currentCluster && 				// Moving to different time in same cluster
					p.getCurrentField().getCluster(clusterID).getClusterDistance() < worldSettings.clusterCenterSize )	
				{
					boolean updateTime = p.getState().getTimeMode() == 1;
					setCurrentFieldTimeSegment(fieldTimeSegment, true, true, updateTime);			// Advance immediately to time segment
					if(!updateTime)
					{
						if(p.mv.debug.time)
							p.mv.systemMessage("Viewer.moveToTimeSegmentInField()... Will transitionToTimeSegmentInClusterMode():"+ state.getCurrentFieldTimeSegment());

//						if(state.enteredField)
//						{
//							lookAtNearestMedia( p.getVisibleClusters(), !p.getState().timeFading );			// Look for images around the camera
//							state.enteredField = false;
//						}
						transitionToClusterTimeSegment(state.getCurrentFieldTimeSegment());
					}
					if(debug.viewer && debug.detailed) p.mv.systemMessage("Viewer.moveToTimeSegmentInField()... Advanced to time segment "+fieldTimeSegment+" in same cluster... ");
				}
				else			// Moving to different time in different cluster
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
							p.mv.systemMessage("Viewer.moveToTimeSegmentInField()... Error! clusterID >= p.getCurrentField().getClusters().size()! clusterID:"+clusterID+" p.getCurrentField() cluster count:"+p.getCurrentField().getClusters().size());
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
				if(debug.viewer) p.mv.systemMessage("Viewer.moveToTimeSegmentInField()... fieldTimeSegment in field #"+f.getID()+" cluster is "+clusterID+"!! Will move to cluster 0...");
				teleportToCluster(0, fade, 0);
			}
		}
		else
		{
			if(debug.viewer)
				p.mv.systemMessage("Viewer.moveToTimeSegmentInField()... timeline is empty!");
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
			p.mv.systemMessage("Viewer.moveToClusterOnMap()... Moving to cluster on map:"+clusterID);

		if(stayInMapView)
		{
			teleportToCluster(clusterID, false, -1);
		}
		else
		{
			teleportToCluster(clusterID, true, -1);
			p.mv.display.setDisplayView( p, 0 );
		}
		
		if(p.mv.display.map2D.getSelectedClusterID() != clusterID) 
			p.mv.display.map2D.setSelectedClusterID(clusterID);
	}

	/**
	 * Move to given point
	 * @param goalPoint Goal point
	 * @param teleport Whether to teleport (true) or move smoothly (false)
	 */
	public void moveToPoint(PVector goalPoint, boolean teleport)
	{
		if(debug.viewer)
			p.mv.systemMessage("Viewer.moveToPoint()... x:"+goalPoint.x+" y:"+goalPoint.y+" z:"+goalPoint.z);

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
		boolean teleport = ( PVector.dist( state.pathGoal, getLocation() ) > p.getSettings().defaultFocusDistance );
		
		if(debug.viewer)
			p.mv.systemMessage("Viewer.moveToFirstPathPoint()... x:"+state.pathGoal.x+" y:"+state.pathGoal.y+" z:"+state.pathGoal.z+" teleport? "+teleport);

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
			p.mv.systemMessage("Viewer.moveToFirstPathPoint()... x:"+state.gpsTrackGoal.x+" y:"+state.gpsTrackGoal.y+" z:"+state.gpsTrackGoal.z+" teleport? "+teleport);

		if(teleport)
			teleportToPoint(state.gpsTrackGoal, true, false);
		else
			setPathAttractorPoint(state.gpsTrackGoal, true);								// Set attractor point from path goal
	}

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
				p.mv.systemMessage("Viewer.moveToPathPoint()... x:"+goalPoint.x+" y:"+goalPoint.y+" z:"+goalPoint.z);

			if(teleport)
				teleportToPoint(goalPoint, true, true);
			else
				setPathAttractorPoint(goalPoint, false);									// Set attractor point from path goal
		}
		else
		{
			p.mv.systemMessage("Viewer.moveToPathPoint()... ERROR: invalid point id:"+id+" path.size():"+path.size());
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
		if(debug.viewer) p.mv.systemMessage("Viewer.moveToWaypoint()... x:"+waypoint.getWorldLocation().x+" y:"+waypoint.getWorldLocation().y+" z:"+waypoint.getWorldLocation().z);

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
//		chooseNextTimeSegment(currentDate);
//		moveToTimeSegmentInField( getCurrentFieldID(), state.getCurrentFieldTimeSegment(), teleport, fade );
		
//		int next = chooseNextTimeSegment(currentDate, p.getState().getTimeMode() == 1);
		int next = chooseNextFieldTimeSegment(currentDate, false);
		moveToTimeSegmentInField( getCurrentFieldID(), next, teleport, fade );
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
			p.mv.systemMessage("Viewer.teleportToCluster()... dest:"+dest+" fade:"+fade);
		
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
					setLocation( c.getLocation(), true, false );
					setCurrentCluster(dest, fieldTimeSegment);
					if(p.state.waitingToFadeInTerrainAlpha) 
						p.fadeInTerrain(false);
				}
			}
			else 
				p.mv.systemMessage("ERROR: Can't teleport to cluster:"+dest+"... clusters.size() =="+p.getCurrentField().getClusters().size());
		}
	}

	/**
	 * Teleport to field by offset from current
	 * @param offset Field index offset
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
	 * @param fade Whether to fade smoothly or jump
	 */
	public void teleportToField(int newField, boolean fade) 
	{
		if(debug.viewer)
			p.mv.systemMessage("Viewer.teleportToField()... newField:"+newField+" fade:"+fade);
		if(newField >= 0)
		{
			p.stopAllVideos();									/* Stop currently playing videos */
			p.stopAllSounds();									/* Stop currently playing sounds */
			
			if(newField >= p.getFieldCount()) newField = 0;
			
			if(debug.viewer)
				p.mv.systemMessage("teleportToField()... newField: "+newField+" out of "+p.getFieldCount());

			if(p.getField(newField).getClusters().size() > 0)
			{
				WMV_Waypoint entry = p.getField(newField).getState().entryLocation;
				boolean hasEntryPoint = false;
				
				if(entry != null)
					hasEntryPoint = entry.initialized();

				if(hasEntryPoint)
				{
//					state.teleportGoalCluster = -1;
					state.teleportGoalClusterID = entry.getClusterID();
//					here
					if(debug.viewer)
						p.mv.systemMessage("teleportToField()... Found entry point... will set Current Cluster to "+state.teleportGoalClusterID);
				}
				else
				{
//					WMV_TimeSegment goalSegment = p.getField(newField).getTimeline().getCenter();
					WMV_TimeSegment goalSegment = p.getField(newField).getTimeline().getLower();
					if(goalSegment != null)
						state.teleportGoalClusterID = goalSegment.getClusterID();
					else
						p.mv.systemMessage("teleportToField()... p.getField("+newField+").getTimeline().getLower() returns null!!");
				}

				if(fade)
				{
					if(state.teleportGoalClusterID >= 0 && state.teleportGoalClusterID < p.getField(newField).getClusters().size())
						state.teleportGoal = p.getField(newField).getCluster(state.teleportGoalClusterID).getLocation();	 // Set goal cluster 
					else
						if(debug.viewer) p.mv.systemMessage("Invalid goal cluster! "+state.teleportGoalClusterID+" field clusters.size():"+p.getField(newField).getClusters().size());
					
					if(debug.viewer) p.mv.systemMessage("  teleportToField()...  Teleported to field "+state.teleportToField+" at state.teleportGoal:"+state.teleportGoal);
					
					if(p.getSettings().screenMessagesOn) 
						p.mv.display.message(p.mv, "Moving to "+p.getField(newField).getName());
					
					teleportWithFading(null, -1, newField);
				}
				else
				{
					if(p.getSettings().screenMessagesOn) 
						p.mv.display.message(p.mv, "Moving to "+p.getField(newField).getName()+" will set state? "+p.getField(newField).hasBeenVisited());

					enterField(newField, p.getField(newField).hasBeenVisited()); 						/* Enter new field */

					if(debug.viewer) 
						p.mv.systemMessage("Viewer.teleportToField()...  Entered field #"+newField);

					if(hasEntryPoint)
					{
						if(debug.viewer) 
							p.mv.systemMessage("Viewer.teleportToField()...  Field has Entry Point... "+p.getField(newField).getState().entryLocation.getWorldLocation());
						moveToWaypoint( p.getField(newField).getState().entryLocation, false );	 // Move to waypoint and stop				
					}
					else
					{
						if(debug.viewer) 
							p.mv.systemMessage("Viewer.teleportToField()...  No Entry Point found...");
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

		state.teleportGoalClusterID = goalClusterID;
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
	 * @param set Whether to set time segment (true) or only return ID (false)
	 */
	private int chooseNextFieldTimeSegment(boolean currentDate, boolean set)
	{
		int newSegment;		// Next time segment ID
		
		if(currentDate)		// On current date
		{
			int nextFieldDate = Integer.valueOf(state.currentFieldDate);
			int nextTimelinesSegment = state.currentFieldTimeSegmentWithDate + 1;
//			newSegment = state.currentFieldTimeSegmentWithDate + 1;
			
			if(nextFieldDate >= p.getCurrentField().getTimelines().size())								// Past dateline end
			{
				nextFieldDate = 0;
				nextTimelinesSegment = 0;
				if(set)
				{
					state.currentFieldDate = nextFieldDate;
					state.currentFieldTimeSegmentWithDate = nextTimelinesSegment;
				}
				p.mv.systemMessage( "Viewer.chooseNextTimeSegment()... nextFieldDate was greater than timelines.size(): "
									+ p.getCurrentField().getTimelines().size()+"  dateline.size(): "+p.getCurrentField().getDateline().size() );
			}
			else	
			{
				if(nextTimelinesSegment >= p.getCurrentField().getTimelines().get(nextFieldDate).timeline.size()) 	// Reached end of day
				{
					if(debug.viewer) p.mv.systemMessage("Viewer.chooseNextTimeSegment()... Reached end of day...");
					nextFieldDate++;
					if(nextFieldDate >= p.getCurrentField().getDateline().size()) 
					{
						if(debug.viewer) p.mv.systemMessage("Viewer.chooseNextTimeSegment()... Reached end of year...");
						nextFieldDate = 0;
						nextTimelinesSegment = 0;
						if(set)	setCurrentFieldTimeSegmentWithDate(nextFieldDate, nextTimelinesSegment, true, true, true);		// Return to first segment
					}
					else
					{
						while(p.getCurrentField().getTimelines().get(nextFieldDate).timeline.size() == 0)		// Go to next non-empty date
						{
							nextFieldDate++;
							if(nextFieldDate >= p.getCurrentField().getDateline().size())
								nextFieldDate = 0;
						}
						if(debug.viewer) p.mv.systemMessage("Viewer.chooseNextTimeSegment()... Chose next date: "+nextFieldDate);
						nextTimelinesSegment = 0;
						if(set) setCurrentFieldTimeSegmentWithDate(nextFieldDate, nextTimelinesSegment, true, true, true);								// Start at first segment
					}
				}
				else
				{
					if(set) setCurrentFieldTimeSegmentWithDate(nextFieldDate, nextTimelinesSegment, true, true, true);
				}
			}
			
			if(!set) state.nextFieldDate = nextFieldDate;
			newSegment = p.getCurrentField().getTimelines().get(nextFieldDate).timeline.get(nextTimelinesSegment).getFieldTimelineID();		// Convert to field time segment
		}
		else				// On any date
		{
			if(debug.viewer && debug.detailed) 
				p.mv.systemMessage("Viewer.chooseNextTimeSegment()... will set current field time segment to: "+state.getCurrentFieldTimeSegment()+1);

			newSegment = state.getCurrentFieldTimeSegment()+1;
			if(set) setCurrentFieldTimeSegment(newSegment, true, true, true);
			if(state.getCurrentFieldTimeSegment() >= p.getCurrentField().getTimeline().timeline.size())
			{
				newSegment = 0;
				if(set) setCurrentFieldTimeSegment(newSegment, true, true, true);									// Return to first segment
			}
		}
		
		return newSegment;
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
//		choosePreviousTimeSegment( currentDate, p.getState().getTimeMode() == 1 );				// Choose previous time segment and set current segment if in Field Mode
		choosePreviousTimeSegment( currentDate, false );										// Choose previous time segment and set current segment if in Field Mode
		moveToTimeSegmentInField(getCurrentFieldID(), state.getCurrentFieldTimeSegment(), teleport, fade);
	}
	
	/**
	 * Choose previous field time segment
	 * @param currentDate Whether to consider only segments on current date
	 */
	private int choosePreviousTimeSegment(boolean currentDate, boolean set)
	{
		int newSegment;			// Previous time segment ID

		if(currentDate)			// On current date
		{
			int prevFieldDate = Integer.valueOf(state.currentFieldDate);
			newSegment = state.currentFieldTimeSegmentWithDate - 1;
			
			if(prevFieldDate >= p.getCurrentField().getTimelines().size())
			{
				prevFieldDate = 0;
				newSegment = 0;
				if(set)
				{
					state.currentFieldDate = prevFieldDate;
					state.currentFieldTimeSegmentWithDate = newSegment;
				}

//				state.currentFieldDate = 0;
//				state.currentFieldTimeSegmentWithDate = 0;
				p.mv.systemMessage("Viewer.choosePreviousTimeSegment()... Current field date was greater than timelines.size(): "
								+p.getCurrentField().getTimelines().size()+"  dateline.size(): "+p.getCurrentField().getDateline().size());
			}
			else
			{
				if(newSegment < 0) 															// Reached beginning of day
				{
					prevFieldDate--;
					if(prevFieldDate < 0) 
					{
						prevFieldDate = p.getCurrentField().getDateline().size()-1;			// Go to last date
						newSegment = p.getCurrentField().getTimelines().get(prevFieldDate).timeline.size()-1;
						if(set) 
							setCurrentFieldTimeSegmentWithDate( prevFieldDate, newSegment, true, true, true );		// Go to last segment
					}
					else
					{
						newSegment = p.getCurrentField().getTimelines().get(state.currentFieldDate).timeline.size()-1;
						if(set) 
							setCurrentFieldTimeSegmentWithDate( prevFieldDate, newSegment, true, true, true );		// Start at last segment
					}
				}
				else
				{
					if(set)
						setCurrentFieldTimeSegmentWithDate( prevFieldDate, newSegment, true, true, true );
				}
			}
			
			if(!set) state.nextFieldDate = prevFieldDate;

			newSegment = p.getCurrentField().getTimelines().get(prevFieldDate).timeline.get(newSegment).getFieldTimelineID();		// Convert to field time segment
		}
		else					// On any date
		{
			newSegment = state.getCurrentFieldTimeSegment()-1;
			if(set) setCurrentFieldTimeSegment(newSegment, true, true, true);
			if(newSegment < 0)
			{
				newSegment = p.getCurrentField().getTimeline().timeline.size()-1;
				if(set) setCurrentFieldTimeSegment(newSegment, true, true, true);
			}
		}
		
		return newSegment;
	}

	/**
	 * Rotate smoothly around X axis to specified angle
	 * @param angle Angle around X axis to rotate to
	 */
	public void turnXToAngle(float angle)
	{
		if(debug.viewer) p.mv.systemMessage("Viewer.turnXToAngle()... angle:"+angle);
		if(!state.turningX)
		{
			state.turnXStart = getXOrientation();
			state.turnXTarget = angle;
			
			PVector turnInfo = getTurnXInfo(state.turnXStart, state.turnXTarget);
			
			state.turnXDirection = turnInfo.x;
			
			settings.turningXAccelInc = PApplet.map(turnInfo.y, 0.f, PApplet.PI * 2.f, settings.turningAccelerationMin, settings.turningAccelerationMax * 0.2f);
			state.turnXStartFrame = p.mv.frameCount;
			state.turningX = true;
		}
	}
	
	/**
	 * Rotate smoothly around Y axis to specified angle
	 * @param angle Angle around Y axis to rotate to
	 */
	public void turnYToAngle(float angle)
	{
		if(debug.viewer) p.mv.systemMessage("ViewerturnYToAngle()... angle:"+angle);

		if(!state.turningY)
		{
			state.turnYStart = getYOrientation();
			state.turnYTarget = angle;
			
			PVector turnInfo = getTurnYInfo(state.turnYStart, state.turnYTarget);
			
			state.turnYDirection = turnInfo.x;
			
			settings.turningYAccelInc = PApplet.map(turnInfo.y, 0.f, PApplet.PI * 2.f, settings.turningAccelerationMin, settings.turningAccelerationMax * 0.2f);
			state.turnYStartFrame = p.mv.frameCount;
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
			
			PVector turnInfo = getTurnXInfo(state.turnXStart, state.turnXTarget);
			
			state.turnXDirection = turnInfo.x;
			state.turnXStartFrame = worldState.frameCount;

			if(debug.viewer && debug.detailed) p.mv.systemMessage("turnXStartFrame:"+state.turnXStartFrame+" turnXTargetFrame:"+state.turnXTargetFrame+" turnXDirection:"+state.turnXDirection);
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
			if(angle > -PApplet.PI * 0.5f + 0.05f && angle < PApplet.PI * 0.5f - 0.05f)
			{
				state.turnYStart = getYOrientation();
				state.turnYTarget = state.turnYStart + angle;
				PVector turnInfo = getTurnYInfo(state.turnYStart, state.turnYTarget);
				state.turnYDirection = turnInfo.x;
				state.turnYStartFrame = worldState.frameCount;
				state.turningY = true;
			}
		}
	}

	/**
	 * Rotate smoothly along X axis with specified turn info
	 * @param turnInfo PVector {direction, angular distance}, where direction is specified as 1: clockwise, -1: counterclockwise
	 */
	public void turnX(PVector turnInfo)
	{
		if(!state.turningX)
		{
			float angle = state.turnXStart + turnInfo.y;
			
			if(debug.viewer && debug.detailed) 
				p.mv.systemMessage("Viewer.turnX()... angle:"+angle);
			
			if(angle > -2.f*PApplet.PI && angle < 2.f*PApplet.PI)
			{
				state.turnXStart = getXOrientation();
				state.turnXTarget = angle;

				state.turnXDirection = turnInfo.x;
				state.turnXStartFrame = p.mv.frameCount;

				if(debug.viewer && debug.detailed) 
					p.mv.systemMessage(" >> state.turnXTarget:"+state.turnXTarget+" turnXDirection:"+state.turnXDirection);

				state.turningX = true;
			}
		}
	}
	
	/**
	 * Rotate smoothly along Y axis by specified angle
	 * @param turnInfo PVector {direction, angularDistance}, where direction is specified as 1: up, -1: down (?)
	 */
	public void turnY(PVector turnInfo)
	{
		if(!state.turningY)
		{
			float angle = state.turnYStart + turnInfo.y;
			
			if(debug.viewer && debug.detailed) 
				p.mv.systemMessage("Viewer.turnY()... angle:"+angle);

			if(angle > -PApplet.PI * 0.5f + 0.05f && angle < PApplet.PI * 0.5f - 0.05f)
			{
				state.turnYStart = getYOrientation();
				state.turnYTarget = angle;
				state.turnYDirection = turnInfo.x;
				state.turnYStartFrame = worldState.frameCount;
				
				if(debug.viewer && debug.detailed) 
					p.mv.systemMessage(" >> state.turnYTarget:"+state.turnYTarget+" turnYDirection:"+state.turnYDirection);

				state.turningY = true;
			}
		}
	}

	/**
	 * Turn to look at given media
	 * @param goal Point to turn towards
	 * @param mediaType Media type (for determining how to look at media)
	 * @param turnXInfo Turn X info
	 * @param turnYInfo Turn Y info
	 */
	private void lookAtMedia( int id, int mediaType, PVector turnXInfo, PVector turnYInfo ) 
	{
		PVector turnGoal = new PVector(0,0,0);
		
		if(debug.viewer)
		{
			p.mv.systemMessage("Viewer.lookAtMedia()... Will start turning towards media:"+id+" mediaType:"+mediaType);
			p.mv.systemMessage(" >> turnXInfo.x:"+turnXInfo.x+" turnYInfo.x:"+turnYInfo.x+"  turnXInfo.y:"+turnXInfo.y+" turnYInfo.y:"+turnYInfo.y);
		}

		switch(mediaType)
		{
			case 0:			// Image
				turnGoal = p.getCurrentField().getImage(id).getLocation();
				break;
			case 1:			// Panorama		-- Turn towards "center"?
//				turnGoal = currentField.getImage(id).getLocation();
				break;
			case 2:			// Video
				turnGoal = p.getCurrentField().getVideo(id).getLocation();
				break;
			case 3:			// Sound			-- Turn towards??
//				turnGoal = currentField.sounds.get(id).getLocation();
				break;
		}
		
		state.turningMediaGoal = new PVector(id, mediaType);
		turnTowards( turnGoal, turnXInfo, turnYInfo );
	}
	
	/**
	 * Turn viewer smoothly to given X direction (yaw) and Y elevation (pitch)
	 * @param direction
	 * @param elevation
	 */
	private void turnToOrientation( WMV_Orientation newOrientation )
	{
		turnXToAngle(newOrientation.getDirection());		// Calculate which way to turn and start turning in X axis
		turnYToAngle(newOrientation.getElevation());		// Calculate which way to turn and start turning in Y axis
	}
	
	/**
	 * Turn smoothly towards given point
	 * @param goal Point to smoothly turn towards
	 */
	private void turnTowards( PVector goal, PVector turnXInfo, PVector turnYInfo ) 
	{
		if(turnXInfo == null || turnYInfo == null)
		{
			if(debug.viewer) 
				p.mv.systemMessage("Viewer.turnTowards()... Turning towards goal.x:"+goal.x+" goal.y:"+goal.y+" goal.z:"+goal.z);

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

			turnXToAngle(yaw);		// Calculate which way to turn and start turning in X axis
			turnYToAngle(pitch);		// Calculate which way to turn and start turning in Y axis
		}
		else
		{
			turnX(turnXInfo);			// Turn along X axis using specified turn info
			turnY(turnYInfo);			// Turn along Y axis using specified turn info

//			turnXToAngle(yaw, turnXInfo);		// Calculate which way to turn and start turning in X axis
//			turnYToAngle(pitch, turnYInfo);		// Calculate which way to turn and start turning in Y axis
		}
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
			p.mv.systemMessage("Viewer.moveToNearestClusterWithTimes... setting attractor:"+p.getCurrentField().getTimeline().timeline.get(nextCluster).getFieldTimelineID());

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
		int rndClusterID = (int) p.mv.random(p.getCurrentField().getClusters().size());
		while(p.getCurrentField().getCluster(rndClusterID).isEmpty() || rndClusterID == state.currentCluster)
		{
			rndClusterID = (int) p.mv.random(p.getCurrentField().getClusters().size());
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
			p.mv.systemMessage("Viewer.stopMoving()... clearAttractors:"+clearAttractors);

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
		state.teleportGoalClusterID = -1;
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
	 * @return PVector {direction, angular distance}, where direction is specified as 1: clockwise, -1: counterclockwise
	 */
	private PVector getTurnXInfo(float startAngle, float targetAngle)
	{
//		if(p.mv.debug.viewer)
//			p.mv.systemMessage("Viewer.getTurnXInfo()... startAngle:"+startAngle+" targetAngle:"+targetAngle);

		PVector result;
		float angularDistance = 0;
		
		float diffRight = -1.f;		// Difference when turning right (dir = 1)
		float diffLeft = -1.f;		// Difference when turning left (dir = -1)

		float diff = 0.f;
		
		if(targetAngle < 0.f)
			targetAngle += PApplet.PI * 2.f;
		if(startAngle < 0.f)
			startAngle += PApplet.PI * 2.f;

		diff = targetAngle - startAngle;
		
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

		angularDistance = diff;		

		if(diffRight <= diffLeft)
		{
//			angularDistance = diffRight;		
			result = new PVector(-1, angularDistance);
//			if(p.mv.debug.viewer) p.mv.systemMessage(">> Result: direction: -1  angularDistance:"+angularDistance);
			return result;								// Return 1 for clockwise 
		}
		else
		{
//			angularDistance = diffLeft;		
			result = new PVector(1, angularDistance);
//			if(p.mv.debug.viewer) p.mv.systemMessage(">> Result: direction: 1  angularDistance:"+angularDistance);
			return result;								// Return -1 for counterclockwise 
		}
	}
	
	/**	 
	 * Calculate the direction, increment size and length of time needed to turn from startingAngle to targetAngle
	 * @param startAngle	Starting angle
	 * @param targetAngle	Target angle
	 * @return PVector {direction, angular distance}, where direction is specified as 1: clockwise, -1: counterclockwise
	 */
	private PVector getTurnYInfo(float startAngle, float targetAngle)
	{
		PVector result;
		float angularDistance = 0;
		
		float diff = -1.f;		// Difference when turning right (dir = 1)

//		if(p.mv.debug.viewer) p.mv.systemMessage("Viewer.getTurnYInfo()... startAngle:"+startAngle+" targetAngle:"+targetAngle);

		if(targetAngle < -PApplet.PI * 0.5f + 0.05f || targetAngle > PApplet.PI * 0.5f - 0.05f)
			return null;
		if(startAngle < -PApplet.PI * 0.5f + 0.05f || startAngle > PApplet.PI * 0.5f - 0.05f)
			return null;

		int direction = -1;
		diff = targetAngle - startAngle;
		if(targetAngle > startAngle)										// Up
			direction = 1;
		else if(targetAngle < startAngle)								// Down
			direction = -1;
		else if(targetAngle == startAngle)								// Full rotation
			diff = PApplet.PI;

		angularDistance = diff;		// Frames until target reached
		
//		if(p.mv.debug.viewer) p.mv.systemMessage(" >> Result  direction:"+direction+"   angularDistance:"+angularDistance);

		result = new PVector(direction, angularDistance);		// Return direction and transition angular distance
		return result;
	}

	/**
	 * Calculate distance needed to turn between two angles along X axis
	 * @param startAngle Starting angle
	 * @param targetAngle Target angle
	 * @param direction Direction to turn (1: clockwise, 0: fastest, -1: counterclockwise)
	 * @return Angular distance
	 */
	float getTurnXDistance(float startAngle, float targetAngle, float direction)
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
	 * Calculate distance needed to turn between two angles along Y axis
	 * @param startAngle Starting angle
	 * @param targetAngle Target angle
	 * @param direction Direction to turn {1: up, -1: down}
	 * @return Angular distance
	 */
	float getTurnYDistance(float startAngle, float targetAngle) //, float direction)
	{
		float diffRight = -1.f;		// Difference when turning up (dir = 1)
		float diffLeft = -1.f;		// Difference when turning down (dir = -1)
		float length = 0.f;
		
		if(p.mv.debug.viewer)
			p.mv.systemMessage("Viewer.getTurnYDistance()... startAngle:"+startAngle+" targetAngle:"+targetAngle);
		
		if(targetAngle < -PApplet.PI * 0.5f + 0.05f || targetAngle > PApplet.PI * 0.5f - 0.05f)
			return PApplet.PI;
		if(startAngle < -PApplet.PI * 0.5f + 0.05f || startAngle > PApplet.PI * 0.5f - 0.05f)
			return PApplet.PI;
		
		float diff = targetAngle - startAngle;
		
		if(targetAngle > startAngle)										// Clockwise
		{
			diff = targetAngle - startAngle;
//			diffLeft = (startAngle + 2.f*PApplet.PI) - targetAngle;
		}
		else if(targetAngle < startAngle)								// Counterclockwise
		{
			diff = startAngle - targetAngle;
//			diffRight = (targetAngle + 2.f*PApplet.PI) - startAngle;
		}
		
		if(targetAngle == startAngle)								// Full rotation
		{
			return PApplet.PI;
			
//			diffRight = 2.f*PApplet.PI;
//			diffLeft = 2.f*PApplet.PI;
		}

//		if(direction == 1.f)								// Turn left
//			length = diffLeft;
//		else if(direction == -1.f)						// Turn right
//			length = diffRight;

		return length;
	}

	/**
	 * Clear attractor cluster
	 */
	public void clearAttractorCluster()
	{
		state.attractorClusterID = -1;											// Set attractorCluster
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
		state.setCurrentFieldTimeSegment( 0 );		// Current time segment in field timeline
		
		/* Memory */
		state.movingToAttractor = false;			// Moving to attractor poanywhere in field
		state.movingToCluster = false;				// Moving to cluster 
		state.following = false;					// Is the camera currently navigating from memory?
		
		/* Clusters */
		state.currentCluster = 0;				// Cluster currently in view
		state.lastCluster = -1;					// Last cluster visited
		state.attractorClusterID = -1;				// Cluster attracting the camera
		state.attractionStart = 0;				// Attraction start frame
//		state.continueAtAttractor = false;		// Continue at attractor
		state.pathWaiting = false;				// Path waiting
		
		state.teleportGoalClusterID = -1;			// Cluster to navigate to (-1 == none)
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
			attractorPoint.update( p.getCurrentField(), worldSettings, worldState, settings, state );
	}

	/**
	 * Update Orientation Mode parameters
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
			
			if(debug.viewer) p.mv.systemMessage("Viewer.updateCurrentCluster()... Cleared current cluster...");
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
			float xTurnDistance = getTurnXDistance(getXOrientation(), state.turnXTarget, state.turnXDirection);
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
			float yTurnDistance = getTurnYDistance(getYOrientation(), state.turnYTarget);
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
								p.mv.systemMessage("Set angle fading to false...");
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
					p.mv.systemMessage("Viewer.updatePhysics()... Attraction but no acceleration... attraction.mag():"+state.attraction.mag()+" acceleration.mag():"+state.acceleration.mag());
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
				if(state.attractorClusterID != -1)
				{
					curAttractor = getAttractorCluster();
				}
			}
			else if( state.movingToAttractor )
			{
				if( attractorPoint != null )
				{
					curAttractor = attractorPoint;
					if(debug.viewer && debug.detailed)				
						p.mv.systemMessage("Viewer.updatePhysics()...  attractorCluster:"+state.attractorClusterID+" slowing:"+state.slowing+" halting:"+state.halting+" attraction.mag():"+state.attraction.mag()+" null? "+(curAttractor == null));
					if(debug.viewer && debug.detailed)					
						p.mv.systemMessage("-->  attractorPoint distance:"+attractorPoint.getClusterDistance()+" mass:"+attractorPoint.getMass()+" acceleration.mag():"+state.acceleration.mag()+" curAttractor dist: "+(curAttractor.getClusterDistance()));
					if(p.utilities.isNaN(state.attraction.mag()))
					{
						state.movingToAttractor = false;
						if(debug.viewer)					/* If not slowing and attraction force exists */
							p.mv.systemMessage("Viewer.updatePhysics()...  attraction was NaN... set movingToAttractor to false");
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
							if(p.mv.frameCount < state.centeringTransitionEnd)
							{
								center(curAttractor.getLocation());						/* Center at current attractor */
							}
							else
							{
								if(debug.viewer && debug.detailed)
									p.mv.systemMessage("Viewer.updatePhysics()... Centered on attractor cluster... curAttractor.getClusterDistance(): "+curAttractor.getClusterDistance()+" worldSettings.clusterCenterSize:"+worldSettings.clusterCenterSize);
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
					if(debug.viewer && debug.detailed) p.mv.systemMessage("Viewer.updatePhysics()... Waiting...");
				}
			}

			if(!state.centering)
			{
				state.location.add(state.velocity);				// Add velocity to location
				setLocation(state.location, true, false);		// Move camera
			}
		}

		if(state.attractorClusterID != -1)
		{
			float curAttractorDistance = PVector.dist( p.getCurrentField().getCluster(state.attractorClusterID).getLocation(), getLocation() );
			if(curAttractorDistance > settings.lastAttractorDistance && !state.slowing)		// If the camera is getting farther than attractor
			{
				if(debug.viewer && state.attractionStart - worldState.frameCount > 20)
				{
					p.mv.systemMessage("Viewer.updatePhysics()... Getting farther from attractor: will stop moving...");
					stop(true);
				}
			}

			/* Record last attractor distance */
			settings.lastAttractorDistance = PVector.dist(p.getCurrentField().getCluster(state.attractorClusterID).getLocation(), getLocation());
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
			p.mv.systemMessage("Viewer.handleReachedAttractor()... movingToCluster:"+state.movingToCluster+" movingToAttractor:"+state.movingToAttractor+" attractorCluster:"+state.attractorClusterID + " following:"+state.following+" path.size():"+path.size());

		if(state.following && path.size() > 0)		/* Reached attractor when following a path */	
		{
			if(debug.viewer)
				p.mv.systemMessage( "Viewer.handleReachedAttractor()... following path size:"+path.size() );

			stopMoving(true);

			setCurrentCluster( state.attractorClusterID, -1 );		/* Set current cluster to attractor cluster, if one exists */
			
			if(debug.path)
				p.mv.systemMessage("Viewer.handleReachedAttractor()... Reached path goal #"+state.pathLocationIdx+", will start waiting...");
			
			startWaiting(settings.pathWaitLength);
		}

		if(state.movingToCluster)							/* Reached attractor when moving to cluster */		
		{
			if(debug.viewer)
				p.mv.systemMessage("Viewer.handleReachedAttractor()... Moving to cluster... current:"+state.currentCluster+" attractor: "+state.attractorClusterID+"...");
			
			if(state.attractorClusterID != -1)
			{
				if(debug.viewer)												
					if(state.attractorClusterID != getNearestCluster(true))		// -- Check if attractor cluster is nearest cluster
						p.mv.systemMessage("Viewer.handleReachedAttractor()... WARNING: attractor cluster is: "+state.attractorClusterID+" but nearest cluster is different:"+getNearestCluster(true));

				if(state.movingToTimeSegment)					/* Moving to time segment in attractor cluster */
					setCurrentCluster( state.attractorClusterID, state.timeSegmentTarget );
				else
					setCurrentCluster( state.attractorClusterID, -1 );

				state.attractorClusterID = -1;
				p.getCurrentField().clearAllAttractors();		/* Stop attracting when reached attractorCluster */
			}
			else
			{
				updateCurrentCluster(true);					/* Update current cluster */
//				turnToCurrentClusterOrientation();			// -- Disabled
			}
			
			if(debug.viewer)
				p.mv.systemMessage("Viewer.handleReachedAttractor()... Reached cluster... current:"+state.currentCluster+" nearest: "+getNearestCluster(false)+" set current time segment to "+state.getCurrentFieldTimeSegment());
			
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
		
//		if(state.enteredField)
//		{
//			lookAtNearestMedia( p.getVisibleClusters(), !p.getState().timeFading );			// Look for images around the camera
//			state.enteredField = false;
//		}
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
			p.mv.systemMessage("Viewer.setCurrentField().. newField:"+newField+" setSimulationState? "+setSimulationState);

		if(newField != getCurrentFieldID() && newField < p.getFieldCount())
		{
			setCurrentFieldID( newField );

			if(debug.viewer && debug.detailed)		
				p.mv.systemMessage("Viewer.setCurrentField().. after set field ID... new state.field:"+getCurrentFieldID()+" currentField ID:"+getCurrentFieldID()+" currentCluster:"+state.currentCluster);

			if(setSimulationState)											// Set simulation state from saved
			{
				p.setStateFromField(p.getField(newField));
//				p.setSimulationStateFromField(p.getField(newField), true);
				if(debug.viewer && debug.detailed)		
					p.mv.systemMessage("Viewer.setCurrentField().. after setSimulationStateFromField...  current field:"+getCurrentFieldID()+" currentField ID:"+getCurrentFieldID()+" currentCluster:"+state.currentCluster+" location:"+getLocation());
			}
			else
			{
				p.updateState();				
//				p.getCurrentField().update(worldSettings, worldState, getSettings(), getState());
//				p.getCurrentField().updateAllMediaStates();
			}

			if(!p.getField(getCurrentFieldID()).hasBeenVisited()) 
				p.getField(getCurrentFieldID()).setVisited(true);
			
//			state.enteredField = true;			// -- Unused
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

		if(debug.viewer) p.mv.systemMessage("Setting new attractor:"+newCluster+" old attractor:"+state.attractorClusterID);
			
		state.attractionStart = worldState.frameCount;									// Set attraction starting frame 
		state.attractorClusterID = newCluster;											// Set attractor cluster
		state.movingToCluster = true;													// Move to cluster
		
		p.getCurrentField().clearAllAttractors();										// Clear all attractors
		p.getCurrentField().getCluster(state.attractorClusterID).setAttractor(true);		// Set attractor cluster
		
		if(p.getCurrentField().getCluster(state.attractorClusterID).getClusterDistance() < state.clusterNearDistance)
		{
			if(p.getCurrentField().getCluster(state.attractorClusterID).getClusterDistance() > worldSettings.clusterCenterSize)
			{
				state.movingNearby = true;
			}
			else if(p.getCurrentField().getCluster(state.attractorClusterID).getClusterDistance() > worldSettings.clusterCenterSize * 0.01f)
			{
				if(debug.viewer && debug.detailed) p.mv.systemMessage("Viewer.setAttractorCluster()... Centering at attractor cluster#"+state.attractorClusterID+"...");
				startCenteringAtAttractor();
			}
			else
			{
				if(debug.viewer) 
					p.mv.systemMessage("Viewer.setAttractorCluster()... Reached attractor cluster #"+state.attractorClusterID+" without moving...");
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
				p.mv.systemMessage("No clusters in field...");
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
					p.mv.systemMessage("Centered cluster:"+c.getID()+" == "+c.getID()+" at angle "+result+" from camera...");
				frontClusters.append(c.getID());
			}
			else
				if(debug.cluster || debug.viewer)
					p.mv.systemMessage("Non-centered, current or empty cluster:"+c.getID()+" at angle "+result+" from camera..."+" NOT centered!");
		}

		float smallest = 100000.f;
		int smallestIdx = 0;

		for (int i = 0; i < frontClusters.size(); i++) 		// Compare distances of clusters in front
		{
			WMV_Cluster c = (WMV_Cluster) p.getCurrentField().getCluster(i);
			if(debug.cluster || debug.viewer)
				p.mv.systemMessage("Checking Centered Cluster... "+c.getID());
		
			float dist = PVector.dist(getLocation(), c.getLocation());
			if (dist < smallest) 
			{
				if(debug.cluster || debug.viewer)
					p.mv.systemMessage("Cluster "+c.getID()+" is closer!");
				smallest = dist;
				smallestIdx = i;
			}
		}		
		
		if(frontClusters.size() > 0)
			return smallestIdx;
		else
		{
			p.mv.systemMessage("No clusters ahead!");
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
			if(worldState.frameCount % 30 == 0 && settings.keepMediaInFrame)	
			{
				if( !mediaAreVisible(false, 1) )	// Check whether any images are currently visible anywhere in front of camera
				{
					if(debug.viewer)
						p.mv.systemMessage("No images visible! will look at nearest image...");
					if( settings.keepMediaInFrame )
						lookAtNearestMedia( p.getVisibleClusters(), !p.getState().timeFading );			// Look for images around the camera
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
			if(debug.path) p.mv.systemMessage("Viewer.updateFollowing()... Finished waiting...");

			state.pathLocationIdx++;
			
			if(state.pathLocationIdx < path.size())
			{
				state.pathGoal = path.get(state.pathLocationIdx).getWorldLocation();
				if(debug.path) p.mv.systemMessage("Viewer.updateFollowing()... Next path location:"+state.pathGoal);
				
				if(state.pathLocationIdx >= 1)
				{
					if( state.pathGoal != path.get(state.pathLocationIdx-1).getWorldLocation() && 	// Goal is different point 
						PVector.dist(state.pathGoal, state.location) > worldSettings.clusterCenterSize)
					{
						if(debug.path) p.mv.systemMessage("Viewer.updateFollowing()... Will "+(state.followTeleport?"teleport":"move") +" to next attraction point..."+state.pathGoal);
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
						if(debug.path) p.mv.systemMessage("Viewer.updateFollowing()...Same or very close attraction point!");
						
						if(settings.orientationModeConstantWaitLength)
						{
							if(debug.path) p.mv.systemMessage("Viewer.updateFollowing()...Ignoring pathLocationIdx #"+state.pathLocationIdx+" at same location as previous...");
							
							state.pathLocationIdx++;
							state.pathGoal = path.get(state.pathLocationIdx).getWorldLocation();
							
							while(state.pathGoal == path.get(state.pathLocationIdx-1).getWorldLocation())
							{
								if(debug.path) p.mv.systemMessage("Viewer.updateFollowing()... Also ignoring pathLocationIdx #"+state.pathLocationIdx+" at same location as previous...");
								state.pathLocationIdx++;
								state.pathGoal = path.get(state.pathLocationIdx).getWorldLocation();
							}
						}
						
						if(debug.path) p.mv.systemMessage("Viewer.updateFollowing()... Set new path location:"+state.pathGoal);
						
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
					p.mv.systemMessage("Reached end of path... ");
					p.mv.systemMessage(" ");
				}
				stopFollowing();
			}
		}
		
		if(state.waiting == false && debug.path) 
			p.mv.systemMessage("Finished waiting...");
	}

	/**
	 * Look at media in given cluster list with smallest turn distance from current orientation
	 * @param clusterList Cluster list
	 */
	public void lookAtNearestMedia(ArrayList<WMV_Cluster> clusterList, boolean ignoreTime)
	{
		float closestDist = 100000.f;
		int closestID = -1;
		int closestMediaType = -1;
		PVector closestTurnXInfo = new PVector(0,0,0);
		PVector closestTurnYInfo = new PVector(0,0,0);
		boolean found = false;
		
//		WMV_Cluster c = p.getCurrentCluster();	// -- Old method
		
		for(WMV_Cluster c : clusterList)
		{
			if(c != null)
			{
				for(int i:c.getState().images)
				{
					WMV_Image img = p.getCurrentField().getImage(i);
					
					boolean valid = true;
					if(!ignoreTime) valid = img.getTimeBrightness() > 0.f;

					if(valid)
					{
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
						PVector turnXInfo = getTurnXInfo(xStart, xTarget);
						float yStart = getYOrientation();
						float yTarget = pitch;
						PVector turnYInfo = getTurnYInfo(yStart, yTarget);
						
						if(turnXInfo != null && turnYInfo != null)
						{
							float turnDist = turnXInfo.y + turnYInfo.y;
							if(turnDist < closestDist)
							{
								closestDist = turnDist;
								closestID = img.getID();
								closestMediaType = 0;
								closestTurnXInfo = turnXInfo;
								closestTurnYInfo = turnYInfo;
								found = true;
							}
						}
					}
				}

				for(int i:c.getState().videos)
				{
					WMV_Video vid = p.getCurrentField().getVideo(i);
					
					boolean valid = true;
					if(!ignoreTime) valid = vid.getTimeBrightness() > 0.f;

					if(valid)
					{
						PVector cameraPosition = getLocation();
						PVector camOrientation = getOrientation();
						PVector goal = vid.getLocation();

						PVector cameraToPoint = new PVector( cameraPosition.x-goal.x, 	//  Vector from the camera to the point      
								cameraPosition.y-goal.y, 
								cameraPosition.z-goal.z  );

						camOrientation.normalize();
						cameraToPoint.normalize();

						float yaw = (float) Math.atan2(cameraToPoint.x, cameraToPoint.z);
						float adj = (float) Math.sqrt(Math.pow(cameraToPoint.x, 2) + Math.pow(cameraToPoint.z, 2)); 
						float pitch = -((float) Math.atan2(adj, cameraToPoint.y) - 0.5f * PApplet.PI);

						float xStart = getXOrientation();
						float xTarget = yaw;
						PVector turnXInfo = getTurnXInfo(xStart, xTarget);
						float yStart = getYOrientation();
						float yTarget = pitch;
						PVector turnYInfo = getTurnYInfo(yStart, yTarget);

						if(turnXInfo != null && turnYInfo != null)
						{
							float turnDist = turnXInfo.y + turnYInfo.y;
							if(turnDist < closestDist)
							{
								closestDist = turnDist;
								closestID = vid.getID();
								closestMediaType = 2;
								closestTurnXInfo = turnXInfo;
								closestTurnYInfo = turnYInfo;
								found = true;
							}
						}
					}
				}
			}
			
			if(found && c.getState().panoramas.size() == 0)
				lookAtMedia( closestID, closestMediaType, closestTurnXInfo, closestTurnYInfo );	// Look at media with the smallest turn distance
		}
	}

//	/**
//	 * Look at nearest media to current viewer orientation
//	 */
//	public void lookAtNearestMedia()
//	{
//		float closestDist = 100000.f;
//		int closestID = -1;
//		int closestMediaType = -1;
//		
//		WMV_Cluster c = p.getCurrentCluster();
//		
//		if(c != null)
//		{
//			for(int i:c.getState().images)
//			{
//				WMV_Image img = p.getCurrentField().getImage(i);
//				PVector cameraPosition = getLocation();
//				PVector camOrientation = getOrientation();
//				PVector goal = img.getLocation();
//
//				PVector cameraToPoint = new PVector(  cameraPosition.x-goal.x, 	//  Vector from the camera to the point      
//						cameraPosition.y-goal.y, 
//						cameraPosition.z-goal.z   );
//
//				camOrientation.normalize();
//				cameraToPoint.normalize();
//
//				float yaw = (float) Math.atan2(cameraToPoint.x, cameraToPoint.z);
//				float adj = (float) Math.sqrt(Math.pow(cameraToPoint.x, 2) + Math.pow(cameraToPoint.z, 2)); 
//				float pitch = -((float) Math.atan2(adj, cameraToPoint.y) - 0.5f * PApplet.PI);
//
//				float xStart = getXOrientation();
//				float xTarget = yaw;
//				PVector turnXInfo = getTurnInfo(xStart, xTarget, 0);
//				float yStart = getXOrientation();
//				float yTarget = pitch;
//				PVector turnYInfo = getTurnInfo(yStart, yTarget, 0);
//
//				float turnDist = turnXInfo.y + turnYInfo.y;
//				if(turnDist < closestDist)
//				{
//					closestDist = turnDist;
//					closestID = img.getID();
//					closestMediaType = 0;
//				}
//			}
//
//			for(int i:c.getState().videos)
//			{
//				WMV_Video vid = p.getCurrentField().getVideo(i);
//				PVector cameraPosition = getLocation();
//				PVector camOrientation = getOrientation();
//				PVector goal = vid.getLocation();
//
//				PVector cameraToPoint = new PVector(  cameraPosition.x-goal.x, 	//  Vector from the camera to the point      
//						cameraPosition.y-goal.y, 
//						cameraPosition.z-goal.z   );
//
//				camOrientation.normalize();
//				cameraToPoint.normalize();
//
//				float yaw = (float) Math.atan2(cameraToPoint.x, cameraToPoint.z);
//				float adj = (float) Math.sqrt(Math.pow(cameraToPoint.x, 2) + Math.pow(cameraToPoint.z, 2)); 
//				float pitch = -((float) Math.atan2(adj, cameraToPoint.y) - 0.5f * PApplet.PI);
//
//				float xStart = getXOrientation();
//				float xTarget = yaw;
//				PVector turnXInfo = getTurnInfo(xStart, xTarget, 0);
//				float yStart = getXOrientation();
//				float yTarget = pitch;
//				PVector turnYInfo = getTurnInfo(yStart, yTarget, 0);
//
//				float turnDist = turnXInfo.y + turnYInfo.y;
//				if(turnDist < closestDist)
//				{
//					closestDist = turnDist;
//					closestID = vid.getID();
//					closestMediaType = 2;
//				}
//			}
//			
////			if(c.panorama)
//			if(c.getState().panoramas.size() == 0)
//				lookAtMedia(closestID, closestMediaType);				// Look at media with the smallest turn distance
//		}
//	}
	
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
			if(debug.viewer && debug.detailed) p.mv.systemMessage("Viewer.updateTeleporting()... Reached teleport goal...");
			
			if( !p.getCurrentField().mediaAreFading() )									// Once no more media are fading
			{
				if(debug.viewer && debug.detailed) p.mv.systemMessage("Viewer.updateTeleporting()... Media finished fading...");
				
				if(state.following && path.size() > 0)
				{
					setCurrentCluster( getNearestCluster(true), -1 );
					if(debug.path)
						p.mv.systemMessage("Viewer.updateTeleporting()... Reached path goal #"+state.pathLocationIdx+", will start waiting...");
					startWaiting( settings.pathWaitLength );
				}
				
				if(state.followingGPSTrack && gpsTrack.size() > 0)
				{
					setCurrentCluster( getNearestCluster(true), -1 );
					if(debug.path)
						p.mv.systemMessage("Viewer.updateTeleporting()... Reached path goal #"+state.pathLocationIdx+", will start waiting...");
					startGPSTrackNavigation();							// Start transition to second GPS track point
				}

				if(state.teleportToField != -1)							// If a new field has been specified 
				{
					if(debug.viewer) p.mv.systemMessage("Viewer.updateTeleporting()... Calling enterField()...  will set state? "+p.getField(state.teleportToField).hasBeenVisited());
					
					enterField(state.teleportToField, p.getField(state.teleportToField).hasBeenVisited());					// Enter new field
					
					if(debug.viewer) p.mv.systemMessage("Viewer.updateTeleporting()... Entered field... "+state.teleportToField);

					WMV_Waypoint entry = null;
					if(state.teleportToField >= 0 && state.teleportToField < p.getFieldCount())
						entry = p.getField(state.teleportToField).getState().entryLocation;

					boolean hasEntryPoint = false;
					if(entry != null)
						hasEntryPoint = entry.initialized();

					if(hasEntryPoint)
					{
						if(debug.viewer) 
							p.mv.systemMessage("Viewer.updateTeleporting()...  Field has Entry Point... "+p.getField(state.teleportToField).getState().entryLocation.getWorldLocation());
						
						moveToWaypoint( p.getField(state.teleportToField).getState().entryLocation, false );	 // Move to waypoint and stop				
					}
					else
					{
						if(debug.viewer) p.mv.systemMessage("Viewer.teleportToField()...  No Entry Point found...");
						moveToFirstTimeSegment(false);					// Move to first time segment if start location not set from saved data 
					}

					if(debug.viewer)  p.mv.systemMessage("Viewer.updateTeleporting()...  Teleported to field "+state.teleportToField+" goal point: x:"+state.teleportGoal.x+" y:"+state.teleportGoal.y+" z:"+state.teleportGoal.z);
					state.teleportToField = -1;
				}
				
				if(state.ignoreTeleportGoal)							
					state.ignoreTeleportGoal = false;
				else
					setLocation(state.teleportGoal, true, true);			// Jump to goal
				
				state.teleporting = false;								// Change the system status

				if(state.teleportGoalClusterID != -1)
				{
					if(state.movingToTimeSegment)
						setCurrentCluster( state.teleportGoalClusterID, state.timeSegmentTarget );
					else
						setCurrentCluster( state.teleportGoalClusterID, -1 );

					state.teleportGoalClusterID = -1;
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
				
//				if(state.enteredField)
//				{
//					lookAtNearestMedia( p.getVisibleClusters(), !p.getState().timeFading );			// Look for images around the camera
//					state.enteredField = false;
//				}
			}
			else
			{
				state.teleportWaitingCount++;
				if(debug.viewer && debug.detailed)
					p.mv.systemMessage("Waiting to finish teleport... "+state.teleportWaitingCount);
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
				if(c == null)
				{
					int nearest = getNearestCluster(true);					// Get nearest cluster location, including current cluster
					WMV_Cluster nearestCluster = p.getCurrentField().getCluster(nearest);
					if(nearestCluster.getClusterDistance() > worldSettings.defaultFocusDistance)
						c = nearestCluster;
				}
				
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
						p.mv.systemMessage("Viewer.followTimeline()... Setting first path goal: "+path.get(state.pathLocationIdx).getWorldLocation());
					
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
						p.mv.display.message(p.mv, "Started Following Timeline...");
				}
				else p.mv.systemMessage("Viewer.startFollowingTimeline()... ERROR: No current cluster!");
			}
			else p.mv.systemMessage("Viewer.startFollowingTimeline()... ERROR: No timeline points!");
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
						p.mv.systemMessage("Viewer.startFollowingMemory()... Setting first path goal: "+path.get(state.pathLocationIdx).getWorldLocation());
					
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
						p.mv.display.message(p.mv, "Started Following Memory...");
				}
				else p.mv.systemMessage("Viewer.startFollowingMemory()... No current cluster!");
			}
			else p.mv.systemMessage("Viewer.startFollowingMemory()... No timeline points!");
		}
	}

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

		if(p.mv.display.window.setupNavigationWindow)
			p.mv.display.window.chkbxPathFollowing.setSelected(true);
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
					
					if(debug.viewer || debug.gps) p.mv.systemMessage("Viewer.startFollowingGPSTrack()...  path points:"+path.size());
					
					state.following = true;
					state.pathLocationIdx = 0;
					state.pathGoal = path.get(state.pathLocationIdx).getWorldLocation();			// Set path goal from GPS track

					if(debug.viewer || debug.gps || debug.path)
					{
						p.mv.systemMessage("    path.get(state.pathLocationIdx).getCaptureLocation()"+path.get(state.pathLocationIdx).getWorldLocation());
						p.mv.systemMessage("    path.get(state.pathLocationIdx).getGPSLocation()"+path.get(state.pathLocationIdx).getGPSLocation());
						p.mv.systemMessage("    state.pathGoal: "+state.pathGoal);
					}

					moveToFirstPathPoint();

					if(p.getSettings().screenMessagesOn)
						p.mv.display.message(p.mv, "Started Following Path: GPS Track");
				}
				else
				{
					state.followingGPSTrack = true;
					
					if(debug.viewer || debug.gps) 
						p.mv.systemMessage("Viewer.startFollowingGPSTrack()...  gpsTrack points:"+gpsTrack.size());

					state.gpsTrackGoal = gpsTrack.get(state.gpsTrackLocationIdx).getWorldLocation();  // Set path goal from GPS track
					state.gpsTrackLocationIdx = 0;
//					boolean teleport = ( PVector.dist( state.gpsTrackGoal, getLocation() ) > settings.farClusterTeleportDistance );
					
					if(p.mv.debug.viewer)
						p.mv.systemMessage("Viewer.startFollowingGPSTrack()... state.gpsTrackGoal set to:"+gpsTrack.get(state.gpsTrackLocationIdx).getWorldLocation());
					if(p.mv.debug.viewer)
						p.mv.systemMessage("   Original GPS location:"+gpsTrack.get(state.gpsTrackLocationIdx).getGPSLocation() + " state.gpsTrackLocationIdx:"+state.gpsTrackLocationIdx);
					
					moveToFirstGPSTrackPoint();
				}
			}
			else p.mv.systemMessage("Viewer.startFollowingGPSTrack()... ERROR: path.size() == 0!");
		}
		else p.mv.systemMessage("Viewer.startFollowingGPSTrack()... ERROR...");
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
		if(p.mv.frameCount >= state.gpsTrackTransitionEnd)			// Reached end of transition
		{
			if(state.gpsTrackLocationIdx > 0)
				setLocation( state.gpsTrackGoal, true, true );		// Set location to path goal
			
			if(p.mv.debug.viewer || p.mv.debug.gps || p.mv.debug.path)
				if(p.mv.debug.detailed) 
					p.mv.systemMessage("updateGPSTrackFollowing()... Reached path goal: " + getLocation() + " == "+state.gpsTrackGoal);   // Debug

			state.gpsTrackLocationIdx++;
			if(state.gpsTrackLocationIdx < gpsTrack.size())
			{
				transitionToGPSTrackPointID( state.gpsTrackLocationIdx );			// Reached end of GPS track
			}
			else			
			{
				if(p.mv.debug.gps || p.mv.debug.viewer)
					p.mv.systemMessage("Reached end of GPS Track... will stop following...");		// -- Not reached??
				stop(true);
			}
		}
		else							// Perform interpolation between points
		{
			if(state.gpsTrackLocationIdx > 0)													// If past first track location
			{
				float framePosition = p.mv.frameCount - state.gpsTrackTransitionStart;				// 0 to gpsTrackTransitionLength
				float percent = p.mv.utilities.mapValue(framePosition, 0.f, state.gpsTrackTransitionLength, 0.f, 1.f);
				setLocation( p.utilities.lerp3D(state.gpsTrackStartLocation, state.gpsTrackGoal, percent), true, false );
			}
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
			p.mv.systemMessage("Viewer.startGPSTrackNavigation()... ERROR Reached end of GPS track after first point!");
	}
	
	/**
	 * Begin transition from current to next point on GPS track 
	 */
	private void transitionToGPSTrackPointID(int gpsTrackLocationIdx)
	{
		if(p.mv.debug.viewer && p.mv.debug.detailed) 
			p.mv.systemMessage("Viewer.transitionToGPSTrackPointID()... gpsTrackLocationIdx:"+gpsTrackLocationIdx);
		
		state.gpsTrackGoal = gpsTrack.get( gpsTrackLocationIdx ).getWorldLocation();
		state.gpsTrackTransitionLength = (int)(state.gpsTrackGoal.dist( getLocation() ) * state.gpsTransitionLengthDistanceFactor);
		state.gpsTrackTransitionLength /= settings.gpsTrackTransitionSpeedFactor;		// Added 7-13-17
		state.gpsTrackStartLocation = getLocation();
		state.gpsTrackTransitionStart = p.mv.frameCount;
		state.gpsTrackTransitionEnd = p.mv.frameCount + state.gpsTrackTransitionLength;
	}
	
	/**
	 * Transition current time point to time of given time segment ID
	 * @param goalID Goal time segment ID
	 */
	private void transitionToFieldTimeSegment(int startID, int goalID)
	{
		if(startID != -1 && goalID != -1)
		{
			state.fadingToTime = true;
			state.timeTransitionStartID = startID;						
			
			state.timeTransitionStartTimePoint = p.getCurrentField().getTimeline().timeline.get(startID).getCenter().getAbsoluteTime();

			if(p.mv.debug.time)
				p.mv.systemMessage("Viewer.transitionToFieldTimelineID()... Set state.timeTransitionStartTimePoint:"+state.timeTransitionStartTimePoint+" while p.getCurrentFieldTime():"+p.getCurrentFieldTime());
			
			state.timeTransitionGoalID = goalID;								// Distance covered over one frame during GPS track transition 
			state.timeTransitionGoalTimePoint = p.getCurrentField().getTimeline().timeline.get(goalID).getCenter().getAbsoluteTime();
			state.timeTransitionStartFrame = p.mv.frameCount;
			state.timeTransitionEndFrame = p.mv.frameCount + state.timeTransitionLength;
		}
		else
		{
			p.mv.systemMessage("Viewer.transitionToFieldTimelineID()... ERROR startID:"+startID+" goalID:"+goalID);
		}
	}
	
	public void transitionToClusterTimeSegment(int goalID)
	{
		if(goalID != -1)
		{
			state.fadingToClusterTime = true;
			state.clusterTimeTransitionStartTimePoint = p.getCurrentClusterTime();

			if(p.mv.debug.time)
				p.mv.systemMessage("Viewer.transitionToTimeSegmentInClusterMode()... Set clusterTimeTransitionStartTimePoint:"+state.clusterTimeTransitionStartTimePoint+" while p.getCurrentClusterTime():"+p.getCurrentClusterTime());

			float absoluteTimePoint = p.getCurrentField().getTimeline().timeline.get( goalID ).getCenter().getAbsoluteTime();	// Find goal as absolute time point

			WMV_Cluster c = p.getCurrentCluster();
			if(c != null)
			{
				float lower = c.getTimeline().getLower().getLower().getAbsoluteTime();		// Cluster lowest absolute time 
				float upper = c.getTimeline().getUpper().getUpper().getAbsoluteTime();		// Cluster highest absolute time 
				float clusterTimePoint = p.mv.utilities.mapValue(absoluteTimePoint, lower, upper, 0.f, 1.f);		// Time relative to cluster
				clusterTimePoint = PApplet.constrain(clusterTimePoint, 0.f, 1.f);				// Time relative to cluster
				if(p.mv.debug.time) p.mv.systemMessage("Viewer.transitionToTimeSegmentInClusterMode()... absoluteTimePoint:"+absoluteTimePoint+" result clusterTimePoint:"+clusterTimePoint);

				state.clusterTimeTransitionGoalTimePoint = clusterTimePoint;
			}
			else
			{
				if(p.mv.debug.time) 
					p.mv.systemMessage("World.transitionToTimeSegmentInClusterMode()...  ERROR: No current cluster...");
			}
			
			state.clusterTimeTransitionStartFrame = p.mv.frameCount;
			state.clusterTimeTransitionEndFrame = p.mv.frameCount + state.clusterTimeTransitionLength;
		}
		else
		{
			p.mv.systemMessage("Viewer.transitionToTimeSegmentInClusterMode()... ERROR... invalid goalID:"+goalID);
		}
	}

	
	/**
	 * Start time fading transition
	 */
	public void startTimeFading(float direction)
	{
		if(p.getState().getTimeMode() == 0)		// Location
		{
			state.fadingClusterTime = true;
			state.fadingClusterTimeDirection = direction;
		}
		else
		{
			state.fadingFieldTime = true;
			state.fadingFieldTimeDirection = direction;
		}
	}
	
	/**
	 * Stop time fading transition
	 */
	public void stopTimeFading()
	{
		if(state.fadingFieldTime)
			state.fadingFieldTime = false;
		if(state.fadingClusterTime)
			state.fadingClusterTime = false;
	}

	/**
	 * Choose GPS track from list and set to selected
	 */
	public void openChooseGPSTrackWindow()
	{
		ArrayList<String> tracks = p.getCurrentField().getGPSTrackNames();
		if(p.mv.display.initializedMaps)
			p.mv.display.map2D.createdGPSMarker = false;
		if(p.mv.display.window.showNavigationWindow)
			p.mv.display.window.closeNavigationWindow();
		p.mv.display.window.openListItemWindow(tracks, "Use arrow keys to select GPS track file and press ENTER", 1, false);
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
//		lookAtNearestMedia( p.getVisibleClusters(), !p.getState().timeFading );			// Look for images around the camera
	}

	/**
	 * Prompt user to select field to enter
	 */
	public void chooseFieldDialog()
	{
		ArrayList<String> fields = p.getFieldNames();
		if(p.mv.display.window.showNavigationWindow)
			p.mv.display.window.closeNavigationWindow();
		p.mv.display.window.openListItemWindow(fields, "Use arrow keys to select field and press ENTER...", 0, true);
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
		setLocation(state.location, false, false);		// Update the camera location
		setTarget(state.target);							// Update the camera target
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
			p.mv.systemMessage("Viewer.setAttractorPoint()... stopFirst: "+stopFirst+" stopFollowing:"+stopFollowing);
		
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
			p.mv.systemMessage("Viewer.setAttractorPoint()... attractorPoint mass: "+attractorPoint.getMass()+" following:"+state.following);
	}

	/**
	 * Set path attractor point
	 * @param newPoint Point of interest to attract camera 
	 */
	private void setPathAttractorPoint(PVector newPoint, boolean first)
	{
		if(debug.viewer) 
			p.mv.systemMessage("Viewer.setPathAttractorPoint()... first: "+first);

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
		if(debug.viewer) p.mv.systemMessage("Viewer.clearAttractor()...");
		
		state.movingToAttractor = false;
		attractorPoint = null;
	}
	
	/**
	 * Center viewer at attractor cluster
	 */
	public void startCenteringAtAttractor()
	{
		state.centering = true;
		state.centeringTransitionStart = p.mv.frameCount;
		state.centeringTransitionEnd = state.centeringTransitionStart + state.centeringTransitionLength;
	}
	
	public void stopCentering()
	{
		state.centering = false;
	}
	
	private void center(PVector dest)
	{
		setLocation( p.utilities.lerp3D(getLocation(), dest, 1.f/state.centeringTransitionLength), true, false );
	}

	
	private void saveCurrentClusterOrientation()
	{
		state.saveClusterOrientation(state.currentCluster, getXOrientation(), getYOrientation(), getZOrientation());
	}

	/**
	 * Turn Orientation Mode On/Off
	 * @param newState New Orientation Mode state
	 */
	public void setOrientationMode( boolean newState )
	{
		settings.orientationMode = newState;
		if(p.getSettings().screenMessagesOn)
			p.mv.display.message(p.mv, "Orientation Mode "+(newState?"ON":"OFF"));

		if(newState)		// Entering Orientation Mode
		{
			PVector target = new PVector(camera.getTarget()[0], camera.getTarget()[1], camera.getTarget()[2]);
			camera.teleport(0, 0, 0, true);
			
			target = new PVector(target.x - getLocation().x, target.y - getLocation().y, target.z - getLocation().z);
			camera.aim(target.x, target.y, target.z);
			target = new PVector(camera.getTarget()[0], camera.getTarget()[1], camera.getTarget()[2]);
			
			updateOrientationMode();
		}
		else				// Exiting Orientation Mode
		{
			camera.teleport(state.location.x, state.location.y, state.location.z, true);
		}
		
		if(p.mv.display.window.setupPreferencesWindow)
			p.mv.display.window.chkbxOrientationMode.setSelected(newState);
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
							p.mv.systemMessage("Image:"+i.getID()+" result:"+result+" is less than centeredAngle:"+settings.centeredAngle);
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
				p.mv.display.message(p.mv, "Saved Viewpoint to Memory.  Path Length:"+memory.size()+"...");
			
			if(debug.viewer) p.mv.systemMessage("Saved Viewpoint to Memory... "+curWaypoint.getWorldLocation()+" Path length:"+memory.size());
		}
		else if(debug.viewer) p.mv.systemMessage("Couldn't add memory point... walking? "+state.walking+" teleporting?"+state.teleporting+" velocity.mag():"+state.velocity.mag());
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
			p.mv.display.message(p.mv, "Cleared Memory...");
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

			if(p.mv.display.window.setupNavigationWindow)
				p.mv.display.window.chkbxPathFollowing.setSelected(false);
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
			if(p.mv.display.window.setupNavigationWindow)
				p.mv.display.window.chkbxPathFollowing.setSelected(false);
		}
	}
	
	/**
	 * Move to cluster whose associated media contains first time segment in field
	 * @param ignoreDate Move to first time segment (i.e. earliest time) on any date
	 * @return Whether succeeded
	 */
	public boolean moveToFirstTimeSegment(boolean ignoreDate)
	{
		int newDate = 0;
		if(ignoreDate)
		{
			if(debug.viewer) p.mv.systemMessage("Viewer.moveToFirstTimeSegment()... Moving to first time segment on any date");
			moveToTimeSegmentInField( getCurrentFieldID(), 0, true, true );		// Move to first time segment in field
			return true;
		}		
		else
		{
			if(debug.viewer) p.mv.systemMessage("Viewer.moveToFirstTimeSegment()... Moving to first time segment on first date");
			int count = 0;
			boolean success = false;
			while(!success)
			{
				boolean updateTime = p.getState().getTimeMode() == 1;
				success = setCurrentTimeSegmentAndDate( 0, newDate, true, updateTime );
				if(!updateTime)
				{
					if(p.mv.debug.time)
						p.mv.systemMessage("Viewer.moveToFirstTimeSegment()... Will transitionToTimeSegmentInClusterMode():"+ state.getCurrentFieldTimeSegment());

					transitionToClusterTimeSegment(state.getCurrentFieldTimeSegment());
				}
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
				if(debug.viewer && debug.detailed) p.mv.systemMessage("Viewer.moveToFirstTimeSegment()... Will move to first time segment on date "+newDate+" state.currentFieldTimeSegmentOnDate:"+state.currentFieldTimeSegmentWithDate+" state.currentFieldDate:"+state.currentFieldDate);
				int curFieldTimeSegment = p.getCurrentField().getTimeSegmentOnDate(state.currentFieldTimeSegmentWithDate, state.currentFieldDate).getFieldTimelineID();
				moveToTimeSegmentInField( getCurrentFieldID(), curFieldTimeSegment, true, true );		// Move to first time segment in field
			}
			else if(debug.viewer)
				p.mv.systemMessage("Viewer.moveToFirstTimeSegment()... Couldn't move to first time segment...");
			
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
		
		if(view) p.mv.display.setDisplayView(p, 4);			// Set current view to Media Display View if mediaID and type are valid
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
			p.mv.display.setDisplayView(p, 4);			// Set current view to Media Display View
		}
		else if(selected.size() > 1)
			p.mv.systemMessage("Viewer.startViewingSelectedMedia()... More than 1 media selected!");
		else
			p.mv.systemMessage("Viewer.startViewingSelectedMedia()... No media selected!");
	}
	
	/**
	 * Exit Media View and return to previous Display View
	 */
	public void exitMediaView()
	{
		p.mv.display.setDisplayView(p, getLastDisplayView());	// Return to previous Display View
	}
	
	public void viewImage(int id)
	{
		p.mv.display.setMediaViewItem(0, id);
	}
	
	public void viewPanorama(int id)
	{
		p.mv.display.setMediaViewItem(1, id);
	}
	
	public void viewVideo(int id)
	{
		p.mv.display.setMediaViewItem(2, id);
	}

	public void viewSound(int id)
	{
		p.mv.display.setMediaViewItem(3, id);
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
		boolean ignoreTime = !p.getState().timeFading;					// Ignore time if Time Cycle is off
		
		for(WMV_Image i : p.getCurrentField().getImages())
		{
			if(!i.getMediaState().disabled)
				if(i.getViewingDistance(this) <= settings.selectionMaxDistance)
				{
					if(ignoreTime)
						possibleImages.add(i);
					else
						if(i.getTimeBrightness() > 0.f)
							possibleImages.add(i);
				}
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
				{
					if(ignoreTime)
						possibleVideos.add(v);
					else
						if(v.getTimeBrightness() > 0.f)
							possibleVideos.add(v);
//					possibleVideos.add(v);
				}
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
				{
					if(ignoreTime)
						possibleSounds.add(s);
					else
						if(s.getTimeBrightness() > 0.f)
							possibleSounds.add(s);
//					possibleSounds.add(s);
				}
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
				if(debug.viewer) p.mv.systemMessage("Selected image in front: "+closestImageID);

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
				if(debug.viewer) 	p.mv.systemMessage("Viewer.chooseMediaInFront()... Selected video in front: "+closestVideoID);
				p.getCurrentField().getVideo(closestVideoID).setSelected(select);
			}
			else if(closestSoundDist < closestImageDist && closestSoundDist < closestVideoDist && closestSoundID != -1)	// Video closer than image
			{
				if(debug.viewer) 	p.mv.systemMessage("Viewer.chooseMediaInFront()... Selected sound in front: "+closestSoundID);
				p.getCurrentField().getSound(closestSoundID).setSelected(select);
			}
		}
		else if(closestVideoDist != 100000.f)					// In Normal Mode
		{
			WMV_Video v = p.getCurrentField().getVideo(closestVideoID);

			if(!v.isPlaying())									// Play video by choosing it
			{
				if(!v.isLoaded()) v.loadMedia(p.mv);
				v.play(p.mv);
			}
			else
				v.stopVideo();
			
			if(debug.viewer) 
				p.mv.systemMessage("Viewer.chooseMediaInFront()... Video is "+(v.isPlaying()?"playing":"not playing: ")+v.getID());
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
						p.mv.systemMessage("choosePanoramaNearby()... Selected #"+newSelected);
				}
			}
			else
			{
				if(debug.panorama)
					p.mv.systemMessage("choosePanoramaNearby()... No panoramas nearby...");
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
			p.mv.systemMessage(">>> Creating Viewer Timeline (Nearby Visible Clusters)... <<<");

		for(WMV_Cluster c : clusters)											// Find all media cluster times
			for(WMV_TimeSegment t : c.getTimeline().timeline)
				timeline.add(t);

		timeline.sort(WMV_TimeSegment.WMV_TimeLowerBoundComparator);				// Sort time segments 
		visibleClusterTimeline = timeline;
	
		state.nearbyClusterTimelineMediaCount = 0;
		
		for(WMV_TimeSegment t : visibleClusterTimeline)
			state.nearbyClusterTimelineMediaCount += t.getTimeline().size();

		if(debug.time)
			p.mv.systemMessage("createNearbyClusterTimeline  nearbyClusterTimeline.size():"+visibleClusterTimeline.size());
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
			p.mv.systemMessage("Viewer.getNearestSound()... id #"+i+" soundDist:"+soundDist);
			
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
						p.mv.systemMessage("Viewer.getNearestSound()... found smallestIdx:"+smallestIdx);

					}
					else
					{
						p.mv.systemMessage("Viewer.getNearestSound()... Smallest is at current cluster:"+getCurrentClusterID());

					}
				}
			}
		}
		p.mv.systemMessage("Viewer.getNearestSound()... result: smallestIdx:"+smallestIdx);

		return smallestIdx;
	}
	
	/**
	 * Set current cluster and current time segment
	 * @param newCluster New current cluster
	 * @param timeSegmentTarget New time segment to set (-1 to ignore this parameter)
	 */
	public void setCurrentCluster(int newCluster, int timeSegmentTarget)
	{
		if(newCluster >= 0 && newCluster < p.getCurrentField().getClusters().size())
		{
			state.lastCluster = state.currentCluster;
			WMV_Cluster c = p.getCurrentCluster();

			if(c != null) c.getState().timeFading = false;
			
			state.currentCluster = newCluster;							// Set new current cluster
			c = p.getCurrentCluster();
			
			if(debug.viewer && debug.detailed) 
				p.mv.systemMessage("Viewer.setCurrentCluster() to "+newCluster+" at field time segment "+timeSegmentTarget+"  cluster location:"+c.getLocation()+" viewer location:"+getLocation());
			
			if(c != null)
			{
				boolean updateTime = p.getState().getTimeMode() == 1;
				if(p.state.timeFading && !c.getState().timeFading) 		// If Time Fading is on, but cluster isn't time fading
					c.getState().timeFading = true;						// Set cluster timeFading to true

				WMV_Field f = p.getCurrentField();
				if(timeSegmentTarget == -1)								// If no time segment specified, search for first time segment
				{
					for(WMV_TimeSegment t : f.getTimeline().timeline)		// Search field timeline for cluster time segment
					{
						if(c.getTimeline() != null)
						{
							if(t.getFieldTimelineID() == f.getTimeSegmentInCluster(c.getID(), 0).getFieldTimelineID())			// Compare cluster time segment to field time segment
								setCurrentFieldTimeSegment(t.getFieldTimelineID(), true, true, updateTime);
							if(!updateTime)
							{
								if(p.mv.debug.time)
									p.mv.systemMessage("Viewer.setCurrentCluster()... 1  Will transitionToTimeSegmentInClusterMode():"+ state.getCurrentFieldTimeSegment());

								transitionToClusterTimeSegment(state.getCurrentFieldTimeSegment());
							}
						}
						else p.mv.systemMessage("Current Cluster timeline is NULL!:"+c.getID());
					}
				}
				else
				{
					if(debug.viewer && debug.detailed) 
						p.mv.systemMessage("Viewer.setCurrentCluster()... will set current field time segment to: "+timeSegmentTarget);

					setCurrentFieldTimeSegment(timeSegmentTarget, true, true, updateTime);
					if(!updateTime)
					{
						if(p.mv.debug.time)
							p.mv.systemMessage("Viewer.setCurrentCluster()... 2  Will transitionToTimeSegmentInClusterMode():"+ state.getCurrentFieldTimeSegment());

						transitionToClusterTimeSegment(state.getCurrentFieldTimeSegment());
					}
					state.movingToTimeSegment = false;
				}

				if(worldState.getTimeMode() == 2 && !state.teleporting)
					p.createTimeCycle();													// Update time cycle for new cluster
				
				if(c.getViewerDistance() < p.settings.clusterCenterSize) 
					state.atCurrentCluster = true;										// Viewer is at current cluster
				
				if(p.mv.display.getDisplayView() == 3 && p.mv.display.getLibraryViewMode() == 2)			// Update media grid if visible
					p.mv.display.updateSelectableMedia();
			}
			else
			{
				if(debug.viewer) p.mv.systemMessage("New current cluster is null!");
			}
		}
		else
		{
			if(newCluster == -1)
			{
				state.currentCluster = newCluster;
				if(debug.viewer) p.mv.systemMessage("Set currentCluster to -1...");
			}
			else
			{
				if(debug.viewer) p.mv.systemMessage("New cluster "+newCluster+" is invalid!");
			}
		}
	}

	/**
	 * Set current field timeline segment
	 * @param newCurrentFieldTimeSegment New current field time segment
	 * @param updateTimelinesSegment Whether to adjust currentFieldTimelinesSegment to match new current field time segment
	 * @param fade Whether to fade (true) or jump (false) to new time point	Note: only works when Time Fading is on (and paused)
	 * @param updateTime Whether to update current time to match new time segment
	 * @return Whether succeeded
	 */
	public boolean setCurrentFieldTimeSegment( int newCurrentFieldTimeSegment, boolean updateTimelinesSegment, boolean fade, boolean updateTime )
	{
		int lastFieldTimeSegment = Integer.valueOf(state.getCurrentFieldTimeSegment());
		state.setCurrentFieldTimeSegment( newCurrentFieldTimeSegment );
		
		if(lastFieldTimeSegment != state.getCurrentFieldTimeSegment())
		{
			p.mv.display.updateCurrentSelectableTimeSegment = true;
			boolean success = true;

			if(updateTimelinesSegment)
			{
				if(state.getCurrentFieldTimeSegment() != -1)
				{
					int newFieldDate = p.getCurrentField().getTimeline().timeline.get(state.getCurrentFieldTimeSegment()).getFieldDateID();
					int newFieldTimelinesSegment = p.getCurrentField().getTimeline().timeline.get(state.getCurrentFieldTimeSegment()).getFieldTimelineIDOnDate();
					success = setCurrentTimeSegmentAndDate(newFieldTimelinesSegment, newFieldDate, false, false);
				}
				else
					success = false;
			}

			if(updateTime)
			{
				if(p.state.timeFading && p.state.paused)
				{
					if(fade)		// Time transition, if possible
					{
						if(lastFieldTimeSegment != -1 && state.getCurrentFieldTimeSegment() != -1)	// Last field time segment exists, transition to new time
						{
							if(p.mv.debug.time)
								p.mv.systemMessage("Viewer.setCurrentFieldTimeSegment()... Will transitionToFieldTimelineID()... lastFieldTimeSegment:"+lastFieldTimeSegment+" state.getCurrentFieldTimeSegment():"+state.getCurrentFieldTimeSegment());

							transitionToFieldTimeSegment(lastFieldTimeSegment, state.getCurrentFieldTimeSegment());
						}
						else if(state.getCurrentFieldTimeSegment() != -1)		// No last time segment, simply set time
						{
							float goalTime = p.getCurrentField().getTimeline().timeline.get(state.getCurrentFieldTimeSegment()).getCenter().getAbsoluteTime();
							p.setCurrentTimeFromAbsolute(goalTime, true);
						}
						else
						{
							if(p.mv.debug.time)
								p.mv.systemMessage("Viewer.setCurrentFieldTimeSegment()... Couldn't update time... lastFieldTimeSegment:"+lastFieldTimeSegment+" state.getCurrentFieldTimeSegment():"+state.getCurrentFieldTimeSegment());
						}
					}
					else			// No time transition
					{
						if(state.getCurrentFieldTimeSegment() != -1)
						{
							float goalTime = p.getCurrentField().getTimeline().timeline.get(state.getCurrentFieldTimeSegment()).getCenter().getAbsoluteTime();
							p.setCurrentTimeFromAbsolute(goalTime, true);
						}
					}
				}
			}

			if(state.getCurrentFieldTimeSegment() >= 0 && state.getCurrentFieldTimeSegment() < p.getCurrentField().getTimeline().timeline.size())
			{
				return success;
			}
			else
			{
				if(debug.viewer && debug.detailed)
					p.mv.systemMessage("Couldn't set newCurrentFieldTimeSegment... currentField.getTimeline().timeline.size():"+p.getCurrentField().getTimeline().timeline.size());
				return false;
			}
		}
		else return true;
	}

	/**
	 * Set current field timelines segment with option to adjust currentFieldTimelineSegment
	 * @param newCurrentFieldTimeSegmentWithDate
	 * @param updateTimelineSegment Whether to update the current field time segment in date-specific timeline as well
	 * @param fade Whether to fade (true) or jump (false) to new time point
	 * @return Whether succeeded
	 */
	public boolean setCurrentFieldTimeSegmentWithDate( int newDate, int newCurrentFieldTimeSegmentWithDate, boolean updateTimelineSegment, boolean fade, boolean updateTime )
	{
		
		if(p.getCurrentField().getTimelines() == null)
		{
			p.mv.systemMessage("setCurrentFieldTimeSegmentOnDate() currentField.getTimelines() == null!!!");
			return false;
		}
		else if(p.getCurrentField().getTimelines().size() < 0 || state.currentFieldDate > p.getCurrentField().getTimelines().size())
		{
			p.mv.systemMessage("setCurrentFieldTimeSegmentOnDate() Error.. currentField.getTimelines().size() == "+p.getCurrentField().getTimelines().size()+" but currentFieldDate == "+state.currentFieldDate+"...");
			return false;
		}

//		if(p.getCurrentField().getTimelines() != null)
//		{
//			if(p.getCurrentField().getTimelines().size() > 0 && p.getCurrentField().getTimelines().size() > state.currentFieldDate)
//			{
////				if(debug.viewer && debug.detailed)
////					p.mv.systemMessage("setCurrentFieldTimeSegmentOnDate()... "+newCurrentFieldTimeSegmentOnDate+" currentFieldDate:"+state.currentFieldDate+" currentField.getTimelines().get(currentFieldDate).size():"+p.getCurrentField().getTimelines().get(state.currentFieldDate).timeline.size()+" getLocation():"+getLocation()+" current field:"+getField());
//			}
//			else 
//			{
//				p.mv.systemMessage("setCurrentFieldTimeSegmentOnDate() Error.. currentField.getTimelines().size() == "+p.getCurrentField().getTimelines().size()+" but currentFieldDate == "+state.currentFieldDate+"...");
//				return false;
//			}
//		}
//		else
//		{
//			p.mv.systemMessage("setCurrentFieldTimeSegmentOnDate() currentField.getTimelines() == null!!!");
//			return false;
//		}
		
		state.currentFieldDate = newDate;
		state.currentFieldTimeSegmentWithDate = newCurrentFieldTimeSegmentWithDate;
		p.mv.display.updateCurrentSelectableTimeSegment = true;

		if(newDate < p.getCurrentField().getTimelines().size())
		{
			if(p.getCurrentField().getTimelines().get(newDate).timeline.size() > 0 && state.currentFieldTimeSegmentWithDate < p.getCurrentField().getTimelines().get(state.currentFieldDate).timeline.size())
			{
				if(updateTimelineSegment)
				{
					int fieldTimelineID = p.getCurrentField().getTimelines().get(newDate).timeline.get(state.currentFieldTimeSegmentWithDate).getFieldTimelineID();
					boolean success = setCurrentFieldTimeSegment(fieldTimelineID, false, fade, updateTime);
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
	public boolean setCurrentTimeSegmentAndDate(int newCurrentFieldTimelinesSegment, int newDate, boolean updateTimelineSegment, boolean updateTime)
	{
		boolean success = setCurrentFieldTimeSegmentWithDate( newDate, newCurrentFieldTimelinesSegment, updateTimelineSegment, updateTime, updateTime );
		return success;
	}
	
	/**
	 * Ignore teleport goal -- Obsolete?
	 */
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
	 * Set far viewing distance
	 * @param newFarHearingDistance New far viewing distance
	 */
	public void setFarHearingDistance( float newFarHearingDistance )
	{
		settings.farHearingDistance = newFarHearingDistance;
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
			if(p.mv.display.initializedMaps)
			{
				if( !p.mv.display.map2D.createdGPSMarker)
				{
					p.mv.display.map2D.initialize(p);
				}
			}
			if(p.mv.display.window.setupNavigationWindow)
			{
				p.mv.display.window.chkbxPathFollowing.setEnabled(true);
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
	 * @return Whether the viewer is following a path
	 */
	public boolean isFollowingGPSTrack()
	{
		return state.followingGPSTrack;
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

	public void setSelectionMaxDistance(float newValue)
	{
		settings.selectionMaxDistance = newValue;
	}
	
	public float getSelectionMaxDistance()
	{
		return settings.selectionMaxDistance;
	}
	
	public void resetSelectionMaxDistance()
	{
		settings.selectionMaxDistance = settings.defaultFocusDistance * settings.selectionMaxDistanceFactor;
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
		return state.attractorClusterID;
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
	 * @param updateCluster Whether to update current cluster
	 */
	public void setLocation(PVector newLocation, boolean updateTarget, boolean updateCluster)
	{
		if(settings.orientationMode)
		{
			state.location = new PVector(newLocation.x, newLocation.y, newLocation.z);
		}
		else
		{
			jumpTo(newLocation, updateTarget, updateCluster);
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
		float newY = getAltitude();																							// Altitude
		float newZ = PApplet.map( vLoc.z, -0.5f * m.fieldLength, 0.5f*m.fieldLength, m.highLatitude, m.lowLatitude ); 			// GPS latitude increases from bottom to top; negative to match P3D coordinate space

		return new PVector(newX, newY, newZ);
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
				p.mv.display.message(p.mv, "Navigation Teleporting ON");
			else
				p.mv.display.message(p.mv, "Navigation Teleporting OFF");
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
		
		if(p.mv.display.window.setupMediaWindow)
			p.mv.display.window.chkbxSelectionMode.setSelected(settings.selection);

		if(p.getSettings().screenMessagesOn)
			p.mv.display.message(p.mv, "Selection "+(newSelection?"Enabled":"Disabled"));
		
		if(inSelectionMode())
		{
			if(p.mv.display.window.setupMediaWindow)
			{
				p.mv.display.window.setSelectionControlsEnabled(true);
			}
		}
		else
		{
			p.getCurrentField().deselectAllMedia(false);		// Deselect media if left Selection Mode
			if(p.mv.display.getDisplayView() == 4)
			{
				p.mv.display.setMediaViewItem(-1, -1);		// Reset current Media View object
				p.mv.display.setDisplayView(p, 0);			// Set Display View to World
			}
			if(p.mv.display.window.setupMediaWindow)
			{
				p.mv.display.window.setSelectionControlsEnabled(false);
			}
		}

		if(inSelectionMode() && getMultiSelection())
		{
			setMultiSelection( false, false );
			if(p.mv.display.window.setupPreferencesWindow)
				p.mv.display.window.chkbxMultiSelection.setSelected( false );
		}
		if(inSelectionMode() && getGroupSelection()) 
		{
			setGroupSelection( false, false );
			if(p.mv.display.window.setupPreferencesWindow)
				p.mv.display.window.chkbxSegmentSelection.setSelected( false );
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
		if(p.mv.display.window.setupPreferencesWindow)
			p.mv.display.window.chkbxSegmentSelection.setSelected(settings.groupSelection);
		if(p.getSettings().screenMessagesOn && message)
			p.mv.display.message(p.mv, "Group Selection Mode "+(newGroupSelection?"Enabled":"Disabled"));
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
		if(p.mv.display.window.setupPreferencesWindow)
			p.mv.display.window.chkbxMultiSelection.setSelected(settings.multiSelection);
		if(p.getSettings().screenMessagesOn && message)
			p.mv.display.message(p.mv, "Multiple Selection Mode "+(newMultiSelection?"Enabled":"Disabled"));
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
				p.mv.display.message(p.mv, "Path Mode Teleporting ON...");
			else
				p.mv.display.message(p.mv, "Path Mode Teleporting OFF...");
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
		if(p.mv.display.window.setupNavigationWindow)
		{
			if(newFollowMode == 1)								// GPS Track
			{
//				System.out.println("Will enable chkbxPathFollowing?"+ (getSelectedGPSTrackID() == -1)+" 2");
				if(getSelectedGPSTrackID() == -1)
					p.mv.display.window.chkbxPathFollowing.setEnabled(false);	// Enable GPS Path Navigation if track is selected
				else
					p.mv.display.window.chkbxPathFollowing.setEnabled(true);	// Disable GPS Path Navigation if track is selected
			}
			else
				p.mv.display.window.chkbxPathFollowing.setEnabled(true);	// Disable GPS Path Navigation if track is selected
		}
	}
	
	public void setGPSTrackSpeed( float newSpeed )
	{
		settings.gpsTrackTransitionSpeedFactor = newSpeed;
	}
	
	public float getGPSTrackSpeed()
	{
		return settings.gpsTrackTransitionSpeedFactor;
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
			p.mv.display.message(p.mv, "Angle Fading "+(settings.angleFading?"ON":"OFF"));
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
			p.mv.display.message(p.mv, "Angle Thinning "+(settings.angleThinning?"ON":"OFF"));
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
		return state.getCurrentFieldTimeSegment();
	}

	/**
	 * @return Current field timeline segment on date, i.e. index of current time segment in date-specific timeline 
	 */
	public int getCurrentFieldTimeSegmentOnDate()
	{
		return state.currentFieldTimeSegmentWithDate;
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
//			p.mv.systemMessage("Viewer.turnToCurrentClusterOrientation()... Found cluster #"+state.currentCluster+" orientation, x:"+o.getDirection()+" y:"+o.getElevation());
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
//				p.mv.systemMessage("Finding Distance of Centered Cluster:"+c.getID()+" at Angle "+result+" from History Vector...");
//				if(c.getID() != state.currentCluster)
//					clustersAlongVector.append(c.getID());
//			}
//			else
//			{
//				if(debug.viewer && debug.detailed)
//					p.mv.systemMessage("Cluster ID:"+c.getID()+" at angle "+result+" from camera..."+" NOT centered!");
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
//			if(debug.viewer && debug.detailed)
//				p.mv.systemMessage("Checking Centered Cluster... "+c.getID());
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
//			if(debug.viewer && debug.detailed)
//				p.mv.systemMessage("No clusters found along vector!");
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
//			p.mv.systemMessage("clustersInList()... memory:"+memory);
//
//			for(int i = history.size()-1; i >= history.size()-memory; i--)		// Iterate through history from last element to 
//			{
//				p.mv.systemMessage("i:"+i);
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
