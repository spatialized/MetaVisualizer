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

import damkjer.ocd.Camera;
import processing.core.PApplet;
import processing.core.PVector;
import processing.data.FloatList;
import processing.data.IntList;
import processing.core.PImage;

/*********************************
 * The virtual viewer, with methods for navigating and interacting with 3D multimedia-based environments
 * @author davidgordon
 */
public class WMV_Viewer 
{
	/* Camera */
	private Camera camera;									// Camera object
	private WMV_WorldSettings worldSettings;				// Viewer settings
	private WMV_WorldState worldState;						// Viewer settings
	private WMV_ViewerSettings settings;					// Viewer settings
	private WMV_ViewerState state;							// Viewer settings
	private ML_DebugSettings debugSettings;					// Viewer settings

	/* Memory */
	public ArrayList<WMV_Waypoint> memory;				// Path for camera to take
	public ArrayList<WMV_Waypoint> path; 				// Record of camera path

//	/* Time */
	public ArrayList<WMV_TimeSegment> nearbyClusterTimeline;	// Combined timeline of nearby (visible) clusters

	/* Navigation */
	public WMV_Cluster attractorPoint;							// For navigation to points outside cluster list

	/* GPS Tracks */
//	private File gpsTrackFile;							// GPS track file
//	private String gpsTrackName = "";					// GPS track name
//	private boolean gpsTrackSelected = false;			// Has a GPS track been selected?
	private ArrayList<WMV_Waypoint> history;			// Stores a GPS track in virtual coordinates
	private ArrayList<WMV_Waypoint> gpsTrack;			// Stores a GPS track in virtual coordinates
	
	WMV_Field currentField;
	WMV_World p;
	public WMV_Viewer(WMV_World parent, WMV_WorldSettings newWorldSettings, WMV_WorldState newWorldState, ML_DebugSettings newDebugSettings)
	{
		p = parent;
//		currentField = newCurrentField;
		
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
		initialize(0, 0, 0, parent.NEWTEST);
	}

	/** 
	 * Initialize camera at a given virtual point
	 * @param x Initial X coordinate
	 * @param y Initial Y coordinate
	 * @param z Initial Z coordinate
	 */
	public void initialize(float x, float y, float z, PImage IMAGETEST)
	{
		camera = new Camera( p.p, x, y, z, 0.f, 0.f, 0.f, 0.f, 1.f, 0.f, settings.fieldOfView, settings.nearClippingDistance, 10000.f);
		state.location = new PVector(x, y, z);
		state.teleportGoal = new PVector(x, y, z);
//		settings.initialize(IMAGETEST);
		settings.initialize();
		state.clustersVisible = new ArrayList<Integer>();
	}
	
	public void loadViewerState(WMV_ViewerState newState)
	{
//		System.out.print("Before loadViewerState... state.location.x:"+state.location.x);
//		System.out.print(" state.location.y:"+state.location.y);
//		System.out.println(" state.location.z:"+state.location.z);
//		System.out.println(" getLocation():"+getLocation());
		state = newState;
//		System.out.print("After loadViewerState... state.location.x:"+state.location.x);
//		System.out.print(" state.location.y:"+state.location.y);
//		System.out.println(" state.location.z:"+state.location.z);
		setLocation(state.location);					// Update the camera
		setTarget(state.target);					// Update the camera
//		System.out.println(" getLocation():"+getLocation());
//		setOrientation(state.orientation);					// Update the camera
	}
	
	public void loadViewerSettings(WMV_ViewerSettings newSettings)
	{
		settings = newSettings;
//		update(worldSettings, worldState);
	}

	public void enterField(WMV_Field newField)
	{
		currentField = newField;
	}
	
	void updateState(WMV_WorldSettings newWorldSettings, WMV_WorldState newWorldState)
	{
		worldSettings = newWorldSettings;
		worldState = newWorldState;
		setOrientation();
	}
	
	/*** 
	 * Update viewer movement and interaction each frame
	 */
	void update(WMV_WorldSettings currentWorldSettings, WMV_WorldState currentWorldState)
	{
		updateState(currentWorldSettings, currentWorldState);
		
		if(!settings.orientationMode)
			state.location = new PVector(camera.position()[0], camera.position()[1], camera.position()[2]);		/* Update location */
		
		updateWalking();							/* Update walking */
		updatePhysics();							/* Update physics */
		
		if(state.teleporting) updateTeleporting();		/* Update teleporting */
		updateMovement();							/* Update navigation */
		if(state.turningX || state.turningY) updateTurning();	/* Update turning */

		currentField.getAttractingClusters().size();
		
		if(worldState.getTimeMode() == 2 && ( getState().isMoving() || isFollowing() || isWalking() ))
			p.createTimeCycle();
		if(worldSettings.timeCycleLength == -1 && worldState.frameCount % 10 == 0.f)
			p.createTimeCycle();

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

		/* Aim camera */
//		if(state.movingToAttractor)
//			camera.aim(attractorPoint.getLocation().x, attractorPoint.getLocation().y, attractorPoint.getLocation().z);
//		if(state.movingToCluster)
//		{
//			GMV_Cluster c = p.getCurrentCluster();
//			camera.aim(c.getLocation().x, c.getLocation().y, c.getLocation().z);
//		}
	}
	
	public void draw()
	{
		camera.feed();						// Send the 3D camera view to the screen
	}
	
	private void updateOrientationMode()
	{
		state.clustersVisible = new ArrayList<Integer>();

		for(WMV_Cluster c : currentField.getClusters())
		{
			if(settings.orientationModeForceVisible)
			{
				if(!c.isEmpty())
					state.clustersVisible.add(c.getID());
			}
			else
			{
				if(!c.isEmpty())
					if(c.getLocation().dist(state.location) < settings.orientationModeClusterViewingDistance)
						state.clustersVisible.add(c.getID());
			}
		}

		if(state.clustersVisible.size() > settings.maxVisibleClusters)		// Show only closest clusters if over maxVisibleClusters
		{
			List<Integer> allClusters = state.clustersVisible;
			state.clustersVisible = new ArrayList<Integer>();

			for(int i=0; i<allClusters.size(); i++)
			{
				if(state.clustersVisible.size() < (settings.orientationModeForceVisible ? settings.minVisibleClusters : settings.maxVisibleClusters))
				{
					state.clustersVisible.add(i);
				}
				else
				{
					WMV_Cluster c = currentField.getCluster(i);
					float cDist = c.getLocation().dist(state.location);
					float largest = -10000;
					int largestIdx = -1;
					int count = 0;

					for(int n : state.clustersVisible)		// Find farthest
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
						state.clustersVisible.remove(largestIdx);
						state.clustersVisible.add(i);
					}
				}
			}
		}
	}

	/**
	 * Move camera forward
	 */
	public void walkForward()
	{
		state.moveZDirection = -1;
		state.movingZ = true;
		state.slowingZ = false;
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
	 * @param fadeTransition  Use fade transition (true) or jump (false)
	 */
	public void teleportToPoint( PVector dest, boolean fadeTransition ) 
	{
		if(settings.orientationMode)
		{
			state.teleportGoal = dest;
			state.location = dest;
		}
		else
		{
			if(fadeTransition)
			{
				state.teleportGoal = dest;
				startTeleport(-1);
			}
			else
			{
				camera.jump(dest.x, dest.y, dest.z);
			}
		}
//		System.out.println("teleportToPoint() getLocation() after:"+getLocation());
	}	

	/**
	 * Set specified field as current field
	 * @param newField  Field to set as current
	 */
	public void setCurrentField(int newField)		
	{
		if(newField < p.getFieldCount())
			state.field = newField;

//		p.p.display.map2D.initializeMaps();

		if(debugSettings.field || debugSettings.viewer)		
			System.out.println("Set new field:"+state.field);

		initialize(0,0,0, p.NEWTEST);							// Initialize camera

		if(debugSettings.field || debugSettings.viewer)		
			System.out.println("Moving (teleporting) to nearest cluster:"+state.field);

		moveToNearestCluster(true);					// Teleport to new location						
	}

	/**
	 * Go to the given image capture location
	 * @param teleport  Whether to teleport (true) or navigate (false)
	 */
	void moveToImageCaptureLocation(int imageID, boolean teleport) 
	{
		if (debugSettings.viewer)
			System.out.println("Moving to capture location... "+imageID);

		PVector newLocation = p.getFieldImages().get(imageID).getCaptureLocation();
		
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
	 * Move camera to the nearest cluster
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
	 * Go to the next cluster numerically containing given media type
	 * @param teleport Whether to teleport or move
	 * @param mediaType Media type without which clusters are skipped...  -1: any 0: image, 1: panorama, 2: video
	 */
	public void moveToNextCluster(boolean teleport, int mediaType) 
	{
		setCurrentCluster(state.currentCluster + 1, -1);
		int next = state.currentCluster;
		int count = 0;
		boolean found = false;
		
		if (next >= currentField.getClusters().size())
			next = 0;
		
		if(debugSettings.viewer)
			System.out.println("moveToNextCluster()... mediaType "+mediaType);

		/* Find goal cluster */
		if(mediaType == -1)	// Any media
		{
			while( currentField.getCluster(next).isEmpty() || next == state.currentCluster )		// Increment nextCluster until different non-empty cluster found
			{
				next++;

				if (next >= currentField.getClusters().size())
				{
					next = 0;
					count++;

					if(count > 3) break;
				}

				if(currentField.getCluster(next).mediaCount != 0)
					System.out.println("Error: Cluster marked empty but mediaPoints != 0!  clusterID:"+next);
			}

			if(count <= 3)				// If a cluster was found in 2 iterations
			{
				found = true;
				if(debugSettings.viewer) System.out.println("Moving to next cluster:"+next+" from current cluster:"+state.currentCluster);
			}
		}

		if(mediaType == 1)		// Panorama
		{
			while(  !currentField.getCluster(next).panorama || 		// Increment nextCluster until different non-empty panorama cluster found
					currentField.getCluster(next).isEmpty() || 
					next == state.currentCluster )
			{
				next++;

				if(next >= currentField.getClusters().size())
				{
					next = 0;
					count++;

					if(count > 3)
					{
						if(debugSettings.viewer)
							System.out.println("No panoramas found...");
						break;
					}
				}
				
				if(currentField.getCluster(next).isEmpty() && currentField.getCluster(next).mediaCount != 0)		// Testing
					System.out.println("Error: Panorama cluster marked empty but mediaPoints != 0!  clusterID:"+next);
			}
			
			if(count <= 3)				// If a cluster was found in 2 iterations
			{
				found = true;
				if(debugSettings.viewer)
					System.out.println("Moving to next cluster with panorama:"+next+" from current cluster:"+state.currentCluster);
			}
			else
			{
				if(debugSettings.viewer)
					System.out.println("No panoramas found...");
			}
		}
		
		if(mediaType == 2)				// Video
		{
			while(  !currentField.getCluster(next).video || 		// Increment nextCluster until different non-empty video cluster found
					currentField.getCluster(next).isEmpty() || 
					next == state.currentCluster )
			{
				next++;

				if(next >= currentField.getClusters().size())
				{
					next = 0;
					count++;

					if(count > 3)
					{
						System.out.println("No videos found...");
						break;
					}
				}
				
				if(currentField.getCluster(next).isEmpty() && currentField.getCluster(next).mediaCount != 0)		// Testing
					System.out.println("Error: Video cluster marked empty but mediaPoints != 0!  clusterID:"+next);
			}
			
			if(count <= 3)				// If a cluster was found in 2 iterations
			{
				found = true;
				if(debugSettings.viewer)
					System.out.println("Moving to next cluster with video:"+next+" from current cluster:"+state.currentCluster);
			}
			else
			{
				if(debugSettings.viewer)
					System.out.println("No videos found...");
			}
		}
		
		if(found)				// If a cluster was found
		{
			if(teleport)		/* Teleport or move */
			{
				teleportToCluster(next, true, -1);
			}
			else
			{
				if(state.teleporting) state.teleporting = false;
				setAttractorCluster(state.currentCluster);
			}
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

		if(debugSettings.viewer && debugSettings.detailed)
			System.out.println("moveToTimeSegmentInField:"+f.getTimeline().get(fieldTimeSegment).getFieldTimelineID()+" f.getTimeline().size():"+f.getTimeline().size());

		if(f.getTimeline().size()>0)
		{
			int clusterID = f.getTimeline().get(fieldTimeSegment).getClusterID();
			if(clusterID == state.currentCluster && currentField.getCluster(clusterID).getClusterDistance() < worldSettings.clusterCenterSize)	// Moving to different time in same cluster
			{
				boolean success = setCurrentFieldTimeSegment(fieldTimeSegment, true);
				if(debugSettings.viewer && debugSettings.detailed)
					System.out.println("Advanced to time segment "+fieldTimeSegment+" in same cluster... ");
			}
			else
			{
				state.movingToTimeSegment = true;								// Set time segment target
				state.timeSegmentTarget = fieldTimeSegment;
				
				if(settings.teleportToFarClusters && !teleport)
				{
					if( PVector.dist(currentField.getCluster(clusterID).getLocation(), getLocation()) > settings.farClusterTeleportDistance )
						teleportToCluster(clusterID, fade, fieldTimeSegment);
					else
						setAttractorCluster(clusterID);
				}
				else
				{
					if(teleport)
						teleportToCluster(clusterID, fade, fieldTimeSegment);
					else
						setAttractorCluster(clusterID);
				}
			}
		}
	}
		
	/**
	 * Teleport the viewer to the given cluster ID
	 * @param dest Destination cluster ID
	 * @param fade Whether to fade (true) or jump (false)
	 */
	public void teleportToCluster( int dest, boolean fade, int fieldTimeSegment ) 
	{
//		System.out.println("teleportToCluster() dest:"+dest);
		if(dest >= 0 && dest < currentField.getClusters().size())
		{
			WMV_Cluster c = currentField.getCluster(dest);

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
			}
		}
		else if(debugSettings.cluster || debugSettings.field || debugSettings.viewer)
			System.out.println("ERROR: Can't teleport to cluster:"+dest+"... clusters.size() =="+currentField.getClusters().size());
	}

	public void setLocation(PVector newLocation)
	{
		if(settings.orientationMode)
			state.location = new PVector(newLocation.x, newLocation.y, newLocation.z);
		else
		{
			camera.jump(newLocation.x, newLocation.y, newLocation.z);
			state.location = getLocation();										// Update to precise camera location
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
			int newField = state.field + offset;

			if(newField >= p.getFieldCount())
				newField = 0;
			
			if(newField < 0)
				newField = p.getFieldCount() - 1;

			state.teleportGoalCluster = 0;
			setCurrentCluster( 0, -1 );

			if(debugSettings.viewer)
				System.out.println("Moving to field: "+newField+" out of "+p.getFieldCount());
			if(debugSettings.viewer)
				System.out.println("... at cluster: "+state.currentCluster+" out of "+p.getField(newField).getClusters().size());

			if(p.getField(newField).getClusters().size() > 0)			// Check whether field has clusters (is valid)
			{
				if(fade)
				{
					state.teleportGoal = new PVector(0,0,0);					// -- Change this!
					startTeleport(newField);
				}
				else
				{
					setLocation(new PVector(0,0,0));
					setCurrentField(newField);				// Set new field
					if(debugSettings.viewer)
						System.out.println(" Teleported to field "+state.teleportToField+"...");
				}
			}
			else
			{
				if(debugSettings.viewer)
					System.out.println("This field has no clusters!");
			}
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

//		if(p.p.display.window.setupGraphicsWindow)
//			p.p.display.window.chkbxHidePanoramas.setSelected(false);
	}
	
	/** 
	 * Hide all panoramas in field
	 */
	public void hidePanoramas()
	{
		settings.hidePanoramas = true;
		p.hidePanoramas();
//		for(WMV_Panorama n : panoramas)
//		{
//			if(n.visible)
//			{
//				if(n.isFading()) n.stopFading();
//				n.fadeOut();
//			}
//		}
//		
//		for(WMV_Cluster c : clusters)
//		{
//			if(c.stitchedPanoramas.size() > 0)
//			{
//				for(WMV_Panorama n : c.stitchedPanoramas)
//				{
//					if(n.isFading()) n.stopFading();
//					n.fadeOut();
//				}
//			}
//			
//			if(c.userPanoramas.size() > 0)
//			{
//				for(WMV_Panorama n : c.userPanoramas)
//				{
//					if(n.isFading()) n.stopFading();
//					n.fadeOut();
//				}
//			}
//		}
//		
//		if(p.p.display.window.setupGraphicsWindow)
//			p.p.display.window.chkbxHidePanoramas.setSelected(true);
	}
	
	/**
	 * Show any video in field if visible
	 */
	public void showVideos()
	{
		settings.hideVideos = false;
		p.showVideos();
//		if(p.p.display.window.setupGraphicsWindow)
//			p.p.display.window.chkbxHideVideos.setSelected(false);
	}
	
	/**
	 * Hide all videos in field
	 */
	public void hideVideos()
	{
		settings.hideVideos = true;
		p.hideVideos();
//		for(WMV_Video v : videos)
//		{
//			if(v.visible)
//			{
//				if(v.isFading()) v.stopFading();
//				v.fadeOut();
//			}
//		}
//		
//		if(p.p.display.window.setupGraphicsWindow)
//			p.p.display.window.chkbxHideVideos.setSelected(true);
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
	void moveToNextTimeSegment(boolean currentDate, boolean teleport, boolean fade)
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
				if(newValue >= currentField.getTimelines().get(state.currentFieldDate).size()) 		// Reached end of day
				{
					if(debugSettings.viewer) System.out.println("Reached end of day...");
					state.currentFieldDate++;
					if(state.currentFieldDate >= currentField.getDateline().size()) 
					{
						if(debugSettings.viewer) System.out.println("Reached end of year...");
						state.currentFieldDate = 0;
						setCurrentFieldTimelinesSegment(0, true);									// Return to first segment
					}
					else
					{
						while(currentField.getTimelines().get(state.currentFieldDate).size() == 0)		// Go to next non-empty date
						{
							state.currentFieldDate++;
							if(state.currentFieldDate >= currentField.getDateline().size())
								state.currentFieldDate = 0;
						}
						if(debugSettings.viewer) System.out.println("Moved to next date: "+state.currentFieldDate);
						setCurrentFieldTimelinesSegment(0, true);									// Start at first segment
					}
				}
				else
					setCurrentFieldTimelinesSegment(newValue, true);
			}
		}
		else
		{
			boolean success = setCurrentFieldTimeSegment(state.currentFieldTimeSegment+1, true);
			if(state.currentFieldTimeSegment >= currentField.getTimeline().size())
				success = setCurrentFieldTimeSegment(0, true);									// Return to first segment
		}

		moveToTimeSegmentInField(currentField.getID(), state.currentFieldTimeSegment, teleport, fade);
	}
	
	/**
	 * Move to cluster corresponding to one time segment earlier on timeline
	 * @param currentDate Whether to look only at time segments on current date
	 * @param teleport Whether to teleport or move
	 */
	void moveToPreviousTimeSegment(boolean currentDate, boolean teleport, boolean fade)
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
						boolean success = setCurrentFieldTimelinesSegment(currentField.getTimelines().get(state.currentFieldDate).size()-1, true);		// Go to last segment
					}
					else
					{
						boolean success = setCurrentFieldTimelinesSegment(currentField.getTimelines().get(state.currentFieldDate).size()-1, true);		// Start at last segment
					}
				}	
				else
				{
					boolean success = setCurrentFieldTimelinesSegment(newValue, true);
				}
			}
		}
		else
		{
			boolean success = setCurrentFieldTimeSegment(state.currentFieldTimeSegment-1, true);
			if(state.currentFieldTimeSegment < 0)
				success = setCurrentFieldTimeSegment(currentField.getTimeline().size()-1, true);
		}

		moveToTimeSegmentInField(currentField.getID(), state.currentFieldTimeSegment, teleport, fade);
	}

	/**
	 * Fade out all visible media, move to goal, then fade in media visible at that location.
	 * @param newField Goal field ID; value of -1 indicates to stay in current field
	 */
	public void startTeleport(int newField) 
	{
		currentField.fadeOutMedia();
//		System.out.println("startTeleport()...");

		state.teleporting = true;
		state.teleportStart = worldState.frameCount;
		state.teleportWaitingCount = 0;
		
		if(newField != -1)
			state.teleportToField = newField;
	}

	/**
	 * Rotate smoothly around X axis to specified angle
	 * @param angle Angle around X axis to rotate to
	 */
	void turnXToAngle(float angle, int turnDirection)
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
	void turnYToAngle(float angle, int turnDirection)
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
	void turnXByAngle(float angle)
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
	void turnYByAngle(float angle)
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
//			state.turnYIncrement = turnIncrement;
			state.turnYStartFrame = worldState.frameCount;
//			state.turnYTargetFrame = state.turnYStartFrame + (int)turnInfo.z;
			state.turningY = true;
		}
	}

	/**
	 * Turn smoothly towards given media
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
			case 3:			// Sound
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
	public void turnTowards( PVector goal ) 
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
		state.rotateXDirection = dir;
		state.rotatingX = true;
	}

	/**
	 * @param dir Direction to rotate (1: clockwise, -1: counterclockwise)
	 */
	public void rotateY(int dir)
	{
		state.rotateYDirection = dir;
		state.rotatingY = true;
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
		while(currentField.getCluster(nextCluster).getTimeline().size() < 2)
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
			System.out.println("moveToClusterWith2OrMoreTimes... setting attractor:"+currentField.getTimeline().get(nextCluster).getFieldTimelineID());

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
	 * @param nextTimeCluster Index in timeline of cluster to move to
	 */
	void moveToNearestClusterInFuture()
	{
//		int nextTimeCluster;
//		
//		IntList closest = getNearClusters(20, p.defaultFocusDistance * 4.f);											// Find 20 near clusters 	- Set number based on cluster density?
//		for(int i:closest)
//		{
//			GMV_Cluster c = currentField.getClusters().get(i);
//			for(GMV_TimeCluster t : c.clusterTimes)
//			{
//				
//			}
//		}
//		
//		if(debugSettings.viewer && debugSettings.detailed)
//			System.out.println("moveToNearestClusterInFuture... setting attractor:"+currentField.getTimeline().get(nextTimeCluster).getID());
//		setAttractorCluster(currentField.getTimeline().get(nextTimeCluster).getID());
	}

	/**
	 * Send the camera to nearest cluster in the field
	 */
	public void moveToNextClusterAlongHistoryVector()
	{
		IntList closest = getNearClusters(20, worldSettings.defaultFocusDistance * 4.f);		// Find 20 near clusters 	- Set number based on cluster density?
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
		int rand = (int) p.p.random(currentField.getClusters().size());
		while(currentField.getCluster(rand).isEmpty())
		{
			rand = (int) p.p.random(currentField.getClusters().size());
		}

		while(currentField.getCluster(rand).isEmpty() || rand == state.currentCluster)
			rand = (int) p.p.random(currentField.getClusters().size());

		if(settings.teleportToFarClusters && !teleport)
		{
			if( PVector.dist(currentField.getCluster(rand).getLocation(), getLocation()) > settings.farClusterTeleportDistance )
				teleportToCluster(rand, true, -1);
			else
				setAttractorCluster( rand );
		}
		else
		{
			if(teleport)
				teleportToCluster(rand, true, -1);
			else
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
		stopMovementTransitions();
		stopMoving();									// -- Improve by slowing down instead and then starting
		clearAttractorPoint();

//		currentField.clearAllAttractors();
		
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
			
//		saveAttitude = getOrientation();
	}

	public void clearAttractorCluster()
	{
		state.attractorCluster = -1;											// Set attractorCluster
		state.movingToCluster = false;
		state.movingToAttractor = false;
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
		if(state.teleporting)
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
		if(state.teleporting)
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
		
//		turnIncrement = PApplet.PI / 240.f;
		
		state.rotatingX = false;			// Whether the camera is rotating in X dimension (turning left or right)?
		state.rotatingY = false;			// Whether the camera is rotating in Y dimension (turning up or down)?
		state.rotatingZ = false;			// Whether the camera is rotating in Z dimension (rolling left or right)?

		/* Interaction */
//		selectionMaxDistanceFactor = 2.f;		// Scaling from defaultFocusDistanceFactor to selectionMaxDistance
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

		initialize(0, 0, 0, p.NEWTEST);
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
			state.location = new PVector(camera.position()[0], camera.position()[1], camera.position()[2]);			// Update location
			return state.location;
		}
	}

	/**
	 * @return Current viewer GPS location
	 */
	public PVector getGPSLocation()
	{
		PVector vLoc = getLocation();
//		if(settings.orientationMode)
//			vLoc = location;
//		else
//			vLoc = new PVector(camera.position()[0], camera.position()[1], camera.position()[2]);			// Update location
		
		WMV_Model m = currentField.getModel();
		
		float newX = PApplet.map( vLoc.x, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, m.lowLongitude, m.highLongitude ); 			// GPS longitude decreases from left to right
		float newY = PApplet.map( vLoc.z, -0.5f * m.fieldLength, 0.5f*m.fieldLength, m.highLatitude, m.lowLatitude ); 			// GPS latitude increases from bottom to top; negative to match P3D coordinate space

		PVector gpsLoc = new PVector(newX, newY);

		return gpsLoc;
	}
	
	public PVector getVelocity()
	{
		if(state.walking)
			return state.walkingVelocity;
		else
			return state.velocity;
	}

	public PVector getAcceleration()
	{
		if(state.walking)
			return state.walkingAcceleration;
		else
			return state.acceleration;
	}

	public void attract(PVector force)
	{
		state.attraction.add( force );		// Add attraction force to camera 
	}
	
	public PVector getAttraction()
	{
		return state.attraction;
	}
	
	/**
	 * @return Current camera X orientation (Yaw)
	 */
	public PVector getOrientation()
	{
		state.orientation = new PVector(camera.attitude()[0], camera.attitude()[1], camera.attitude()[2]);			// Update orientation
		return state.orientation;
	}

	/**
	 * @return Current camera X orientation (Yaw)
	 */
	public float getXOrientation()
	{
		state.orientation = new PVector(camera.attitude()[0], camera.attitude()[1], camera.attitude()[2]);			// Update X orientation
		return state.orientation.x;
	}

	/**
	 * @return Current camera Y orientation (Pitch)
	 */
	public float getYOrientation()
	{
		state.orientation = new PVector(camera.attitude()[0], camera.attitude()[1], camera.attitude()[2]);			// Update Y orientation
		return state.orientation.y;
	}

	/**
	 * @return Current camera Z orientation (Roll)
	 */
	public float getZOrientation()
	{
		state.orientation = new PVector(camera.attitude()[0], camera.attitude()[1], camera.attitude()[2]);			// Update Z orientation
		return state.orientation.z;
	}
	
	/**
	 * Set the current viewer orientation from the OCD camera state
	 */
	public void setOrientation()
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
		
		state.orientationVector = camOrientation;
		if(state.target == null)
			PApplet.println("Setting state.target:"+state.target);
		state.target = getTarget();
	}
	
	public void setTarget(PVector newTarget)
	{
		if(newTarget != null)
			camera.aim(newTarget.x, newTarget.y, newTarget.z);
	}
	
	public PVector getTarget()
	{
		return new PVector(camera.target()[0], camera.target()[1], camera.target()[2]);	
	}
	
	/**
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

			if(camera.attitude()[1] + state.turningVelocity.y >= PApplet.PI * 0.5f || camera.attitude()[1] - state.turningVelocity.y <= -PApplet.PI * 0.5f)	// Avoid gimbal lock
			{
				state.turningVelocity.y = 0.f;
				state.turningAcceleration.y = 0.f;
			}
			
			if(Math.abs( state.turningVelocity.mag()) > 0.f && Math.abs(state.turningVelocity.x) < settings.turningVelocityMin 
					&& (state.turnSlowingX || state.turnHaltingX) )
//			if( Math.abs(turningVelocity.x) < settings.turningVelocityMin )
				stopTurningX();

			if(Math.abs( state.turningVelocity.mag()) > 0.f && Math.abs(state.turningVelocity.y) < settings.turningVelocityMin 
							&& (state.turnSlowingY || state.turnHaltingY) )
//			if( Math.abs(turningVelocity.y) < settings.turningVelocityMin )
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
//			System.out.println("xTurnDistance:"+xTurnDistance);
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
//			System.out.println("yTurnDistance:"+yTurnDistance);
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
								goalMediaBrightness = img.viewingBrightness;
								break;
							case 1:
								WMV_Panorama pano = currentField.getPanorama((int)state.turningMediaGoal.x);
								goalMediaBrightness = pano.viewingBrightness;
								break;
							case 2:
								WMV_Video vid = currentField.getVideo((int)state.turningMediaGoal.x);
								goalMediaBrightness =  vid.viewingBrightness;
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
//				float attractionMult = settings.camHaltInc-PApplet.map(attraction.mag(), 0.f, 1.f, 0.f, settings.camHaltInc);
//				float accelerationMult = settings.camHaltInc-PApplet.map(acceleration.mag(), 0.f, 1.f, 0.f, settings.camHaltInc);
//				float velocityMult = settings.camHaltInc-PApplet.map(velocity.mag(), 0.f, 1.f, 0.f, settings.camHaltInc);
//				attraction.mult(attractionMult);
//				acceleration.mult(accelerationMult);							// Decrease acceleration
//				velocity.mult(velocityMult);								// Decrease velocity
				
//				attraction.mult(settings.camHaltInc);
//				acceleration.mult(settings.camHaltInc);							// Decrease acceleration
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

			WMV_Cluster curAttractor = new WMV_Cluster(worldSettings, worldState, settings, debugSettings, 0, 0, 0, 0);	 /* Find current attractor if one exists */
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
//					if(debugSettings.viewer && worldState.frameCount - attractionStart > 120)					/* If not slowing and attraction force exists */
//						System.out.println("Attraction taking a while... attractorCluster:"+attractorCluster+" slowing:"+slowing+" halting:"+halting+" attraction.mag():"+attraction.mag()+" acceleration.mag():"+acceleration.mag()+" null? "+(curAttractor == null));
					if(debugSettings.viewer)					/* If not slowing and attraction force exists */
						System.out.println("--> attractorCluster:"+state.attractorCluster+" slowing:"+state.slowing+" halting:"+state.halting+" attraction.mag():"+state.attraction.mag()+" acceleration.mag():"+state.acceleration.mag()+" null? "+(curAttractor == null));
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

			state.location.add(state.velocity);				// Add velocity to location
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
			
//			turnTowardsPoint(attractorPoint.getLocation());
			currentField.clearAllAttractors();
			state.movingToAttractor = false;
		}
	}

	public void updateWalking()
	{
		// Move X Transition
		if (state.movingX && !state.slowingX) 
		{
			state.walkingAcceleration.x += settings.walkingAccelInc * state.moveXDirection;
			state.lastMovementFrame = worldState.frameCount;
		}

		// Move Y Transition
		if (state.movingY && !state.slowingY) 
		{
			state.walkingAcceleration.y += settings.walkingAccelInc * state.moveYDirection;
			state.lastMovementFrame = worldState.frameCount;
		}

		// Move Z Transition
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
//			WMV_Cluster curAttractor = currentField.getCluster(attractorCluster);
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
		PVector camOrientation = state.getOrientationVector();

		IntList nearClusters = getNearClusters(20, worldSettings.defaultFocusDistance * 4.f);	// Find 20 nearest clusters -- Change based on density?
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
				if (worldState.frameCount < state.zoomStart + state.zoomLength) 
				{
					zoomCamera(settings.zoomIncrement / state.zoomLength * state.zoomDirection);
				}
				else 
					state.zooming = false;
			}
		}
		else										// If no transitions
		{
			if(worldState.frameCount % 60 == 0 && settings.optimizeVisibility)		// If not currently turning
			{
				if( !mediaAreVisible(false, 1) )	// Check whether any images are currently visible anywhere in front of camera
				{
					if(debugSettings.viewer)
						System.out.println("No images visible! will look at nearest image...");
					lookAtNearestMedia();			// Look for images around the camera
				}
				else if(currentField.getImagesVisible() + currentField.getPanoramasVisible()
						+ currentField.getVideosVisible() >= settings.mediaDensityThreshold)				// -- Update this!
				{
					if( mediaAreVisible(false, settings.mediaDensityThreshold) && !settings.angleFading )
					{
						if(debugSettings.viewer)
							System.out.println("Over "+settings.mediaDensityThreshold+" media visible... Set angle fading to true...");
						settings.angleFading = true;
					}
				}
			}
		}
	}
	
	/**
	 * Update Path Following Mode
	 */
	private void updateFollowing()
	{
		int waitLength = settings.pathWaitLength;
		if(worldState.frameCount > state.pathWaitStartFrame + waitLength )
		{
			state.waiting = false;
//			if(debugSettings.path) System.out.println("Finished waiting...");

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
							{
								startWaiting();
//								waiting = true;
							}
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
							{
								startWaiting();
//								waiting = true;
							}
						}
						else
							setAttractorPoint(state.pathGoal);
//						turnTowardsPoint(memory.get(revisitPoint).target);			// Turn towards memory target view
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
	 * Find and look at nearest media to current viewer orientation
	 */
	void lookAtNearestMedia()
	{
		float closestDist = 100000.f;
		int closestID = -1;
		int closestMediaType = -1;
		
		WMV_Cluster c = p.getCurrentCluster();
		
		if(c != null)
		{
			for(int i:c.images)
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

			for(int i:c.videos)
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
			if(c.panoramas.size() == 0)
				lookAtMedia(closestID, closestMediaType);				// Look at media with the smallest turn distance
		}
	}
	
	public void zoomCamera(float zoom)
	{
		settings.fieldOfView += zoom;
		camera.zoom(zoom);
	}

	public void resetCamera()
	{
		initialize( getLocation().x,getLocation().y,getLocation().z, p.NEWTEST );							// Initialize camera
	}
	
	/**
	 * Update teleporting interpolation values
	 */
	private void updateTeleporting()
	{
		if(worldState.frameCount >= state.teleportStart + settings.teleportLength)		// If the teleport has finished
		{
			if(debugSettings.viewer) System.out.println(" Reached teleport goal...");

//			if(teleportWaitingCount > settings.teleportLength * 2.f)
//			{
//				if(debugSettings.viewer) System.out.println(" Exceeded teleport wait time. Stopping all media...");
//				currentField.stopAllMediaFading();
//			}
			
			if( !currentField.mediaAreFading() )			// Once no more images are fading
			{
				if(debugSettings.viewer) System.out.println(" Media finished fading...");

				if(state.following && path.size() > 0)
				{
					setCurrentCluster( getNearestCluster(true), -1 );
					if(debugSettings.path)
						System.out.println("Reached path goal #"+state.pathLocationIdx+", will start waiting...");
					startWaiting();
				}

				if(state.teleportToField != -1)							// If a new field has been specified 
				{
					if(debugSettings.viewer) System.out.println(" Teleported to field "+state.teleportToField+"...");

					setCurrentField(state.teleportToField);				// Set new field
					state.teleportToField = -1;							// Reset target field
				}

				setLocation(state.teleportGoal);				// Move the camera
				state.teleporting = false;					// Change the system status
				
				if(debugSettings.viewer) System.out.println(" Teleported to x:"+state.teleportGoal.x+" y:"+state.teleportGoal.y+" z:"+state.teleportGoal.z);

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

//					if(debugSettings.viewer) System.out.println("Reached attractor... turning towards image");
//					if(attractorPoint != null)
//						turnTowardsPoint(attractorPoint.getLocation());
					
					currentField.clearAllAttractors();	// Clear current attractors
				}
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
		if(start)		// Start following timeline
		{
			if(!state.following)
			{
				path = currentField.getTimelineAsPath();			// Get timeline as path of Waypoints matching cluster IDs
				
				if(path.size() > 0)
				{
					WMV_Cluster c = p.getCurrentCluster();
					if(c != null)
					{
						state.following = true;
						state.pathLocationIdx = -1;								// Find path start

						if(fromBeginning)
						{
							state.pathLocationIdx = 0;
						}
						else
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
		else				// Stop following timeline
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
		else System.out.println("path.size() == 0!");
	}

	/**
	 * Follow current GPS track
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
	 * Wait for specified time until moving to next memory point
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
		attractorPoint = new WMV_Cluster(worldSettings, worldState, settings, debugSettings, -1, newPoint.x, newPoint.y, newPoint.z);
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
//			camera.jump(0, 0, 0);
			
			PVector target = new PVector(camera.target()[0], camera.target()[1], camera.target()[2]);
//			System.out.println("Jump to origin, target.x:"+target.x+" target.y:"+target.y+" target.z:"+target.z);
			camera.jump(0, 0, 0);
//			System.out.println("After jump target.x:"+target.x+" target.y:"+target.y+" target.z:"+target.z);
			
			target = new PVector(target.x - getLocation().x, target.y - getLocation().y, target.z - getLocation().z);
			camera.aim(target.x, target.y, target.z);
			target = new PVector(camera.target()[0], camera.target()[1], camera.target()[2]);
//			System.out.println("After camera.aim target.x:"+target.x+" target.y:"+target.y+" target.z:"+target.z);
			
//			PVector saveAttitude = new PVector(camera.attitude()[0], camera.attitude()[1], camera.attitude()[2]);
//			System.out.println("jump to origin  saveAttitude.x:"+saveAttitude.x+" saveAttitude.y:"+saveAttitude.y+" saveAttitude.z:"+saveAttitude.z);
//			camera.jump(0, 0, 0);
//			PVector newAttitude = new PVector(camera.attitude()[0], camera.attitude()[1], camera.attitude()[2]);
//			System.out.println("After jump newAttitude.x:"+newAttitude.x+" newAttitude.y:"+newAttitude.y+" newAttitude.z:"+newAttitude.z);
//			camera.pan(saveAttitude.x - newAttitude.x);
//			camera.tilt(saveAttitude.y - newAttitude.y);
//			newAttitude = new PVector(camera.attitude()[0], camera.attitude()[1], camera.attitude()[2]);
//			System.out.println("After camera.pan/tilt newAttitude.x:"+newAttitude.x+" newAttitude.y:"+newAttitude.y+" newAttitude.z:"+newAttitude.z);
		}
		else
		{
			camera.jump(state.location.x, state.location.y, state.location.z);
//			PVector saveAttitude = new PVector(camera.attitude()[0], camera.attitude()[1], camera.attitude()[2]);
//			System.out.println("jump back   saveAttitude.x:"+saveAttitude.x+" saveAttitude.y:"+saveAttitude.y+" saveAttitude.z:"+saveAttitude.z);
//			camera.jump(location.x, location.y, location.z);
//			PVector newAttitude = new PVector(camera.attitude()[0], camera.attitude()[1], camera.attitude()[2]);
//			System.out.println("After jump newAttitude.x:"+newAttitude.x+" newAttitude.y:"+newAttitude.y+" newAttitude.z:"+newAttitude.z);
//			camera.pan(saveAttitude.x - newAttitude.x);
//			camera.tilt(saveAttitude.y - newAttitude.y);
//			newAttitude = new PVector(camera.attitude()[0], camera.attitude()[1], camera.attitude()[2]);
//			System.out.println("After camera.pan/tilt newAttitude.x:"+newAttitude.x+" newAttitude.y:"+newAttitude.y+" newAttitude.z:"+newAttitude.z);
		}
		
		if(p.p.display.window.setupGraphicsWindow)
			p.p.display.window.chkbxOrientationMode.setSelected(newState);
	}
	
	/**
	 * @param front Restrict to media in front
	 * @threshold Minimum number of media to for method to return true
	 * @return Whether any media are visible and in front of camera
	 */
	public boolean mediaAreVisible( boolean front, int threshold )
	{
		IntList nearClusters = getNearClusters(10, settings.farViewingDistance + worldSettings.defaultFocusDistance); 	

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
			WMV_Cluster cluster = currentField.getCluster(clusterID);
			for( int id : cluster.images )
			{
				WMV_Image i = currentField.getImage(id);
				if(i.getViewingDistance() < settings.farViewingDistance + i.getFocusDistance() 
				&& i.getViewingDistance() > settings.nearClippingDistance * 2.f )		// Find images in range
				{
					if(!i.disabled)
						closeImages.add(i);							
				}
			}

			for( int id : cluster.panoramas )
			{
				WMV_Panorama n = currentField.getPanorama(id);
				if(n.getViewingDistance() < settings.farViewingDistance + worldSettings.defaultFocusDistance 
						&& n.getViewingDistance() > settings.nearClippingDistance * 2.f )		// Find images in range
				{
					if(!n.disabled)
						closePanoramas.add(n);							
				}
			}

			for( int id : cluster.videos )
			{
				WMV_Video v = currentField.getVideo(id);
				if(v.getViewingDistance() <= settings.farViewingDistance + v.getFocusDistance()
				&& v.getViewingDistance() > settings.nearClippingDistance * 2.f )		// Find videos in range
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
			if(!i.isBackFacing(getLocation()) && !i.isBehindCamera(getLocation(), state.getOrientationVector()))			// If image is ahead and front facing
			{
				result = Math.abs(i.getFacingAngle(state.getOrientationVector()));			// Get angle at which it faces camera

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
			if(!v.isBackFacing(getLocation()) && !v.isBehindCamera(getLocation(), state.getOrientationVector()))			// If video is ahead and front facing
			{
				result = Math.abs(v.getFacingAngle(state.getOrientationVector()));			// Get angle at which it faces camera

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
			return imagesVisible || panoramasVisible || videosVisible;
		else
			return (visImages + visVideos) >= threshold;
	}
		
	/**
	 * Add current camera location and orientation to memory
	 */
	public void addPlaceToMemory()
	{
		if(!state.teleporting && !state.walking && state.velocity.mag() == 0.f)		// Only record points when stationary
		{
			WMV_Waypoint curWaypoint = new WMV_Waypoint(path.size(), getLocation(), null);				// -- Calculate time instead of null!!
			curWaypoint.setTarget(getOrientation());
			curWaypoint.setID(state.currentCluster);						// Need to make sure camera is at current cluster!
			
			while(memory.size() > 100)								// Prevent memory path from getting too long
				memory.remove(0);
				
			memory.add(curWaypoint);
			
			if(debugSettings.viewer)
			{
				System.out.println("Added point to memory... "+curWaypoint.getLocation());
				System.out.println("Path length:"+memory.size());
			}
		}
		else if(debugSettings.viewer)
			System.out.println("Couldn't add memory point... walking? "+state.walking+" teleporting?"+state.teleporting+" velocity.mag():"+state.velocity.mag());
	}
	
	/**
	 * clearMemory()
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
	 * Act on the image or video in front of camera. In Selection Mode, selects or deselects the media file.
	 * In Normal Mode, starts or stops a video, but has no effect on an image.
	 */
	public void chooseMediaInFront(boolean select) 
	{
		ArrayList<WMV_Image> possibleImages = new ArrayList<WMV_Image>();
		for(WMV_Image i : currentField.getImages())
		{
			if(i.getViewingDistance() <= settings.selectionMaxDistance)
				if(!i.disabled)
					possibleImages.add(i);
		}

		float closestImageDist = 100000.f;
		int closestImageID = -1;

		for(WMV_Image s : possibleImages)
		{
			if(!s.isBackFacing(getLocation()) && !s.isBehindCamera(getLocation(), state.getOrientationVector()))					// If image is ahead and front facing
			{
				float result = Math.abs(s.getFacingAngle(state.getOrientationVector()));				// Get angle at which it faces camera

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
				if(!v.disabled)
					possibleVideos.add(v);
		}

		float closestVideoDist = 100000.f;
		int closestVideoID = -1;

		for(WMV_Video v : possibleVideos)
		{
			if(!v.isBackFacing(getLocation()) && !v.isBehindCamera(getLocation(), state.getOrientationVector()))					// If image is ahead and front facing
			{
				float result = Math.abs(v.getFacingAngle(state.getOrientationVector()));				// Get angle at which it faces camera

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
					WMV_Cluster c = currentField.getCluster( currentField.getImage(newSelected).cluster );
					for(WMV_MediaSegment m : c.segments)
					{
//						if(m.getImages().hasValue(newSelected))
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
				if(!v.isLoaded()) v.loadMedia(p.p);
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

//		System.out.println("setNearbyClusterTimeline  nearbyClusterTimeline.size():"+nearbyClusterTimeline.size());
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
			for(WMV_TimeSegment t : c.getTimeline())
				timeline.add(t);

		timeline.sort(WMV_TimeSegment.WMV_TimeLowerBoundComparator);				// Sort time segments 
		nearbyClusterTimeline = timeline;
	
		state.nearbyClusterTimelineMediaCount = 0;
		
		for(WMV_TimeSegment t : nearbyClusterTimeline)
			state.nearbyClusterTimelineMediaCount += t.getTimeline().size();

		if(debugSettings.time)
			System.out.println("createNearbyClusterTimeline  nearbyClusterTimeline.size():"+nearbyClusterTimeline.size());
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
						System.out.println("timelineIndex - mediaCt > nearbyClusterTimeline.get(timelineCt).getTimeline().size()!!.. timelineIndex:"+timelineIndex+" mediaCt:"+mediaCt);
					}
				}
				else
				{
					System.out.println("Searched for timelineIndex:"+ timelineIndex +" which was past end of timeline: "+(timelineCt-1));
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

		WMV_Field f = currentField;

		for (int i = 0; i < f.getImages().size(); i++) {
			if (f.getImage(i).visible) {
				float imageAngle = f.getImage(i).getFacingAngle(state.getOrientationVector());
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
		WMV_Field f = currentField;

		for (int i = 0; i < f.getImages().size(); i++) {
			if (f.getImage(i).visible) {
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
	 * @return Video closest to directly in front of the camera
	 */
	public int getFrontVideo() {
		float smallest = 100000.f;
		int smallestIdx = 0;

		WMV_Field f = currentField;

		for (int i = 0; i < f.getVideos().size(); i++) {
			if (f.getVideo(i).visible) {
				float videoAngle = f.getVideo(i).getFacingAngle(state.getOrientationVector());
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
		WMV_Field f = currentField;

		for (int i = 0; i < f.getVideos().size(); i++) {
			if (f.getVideo(i).visible) {
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
	 * Open dialog to select GPS track file
	 */
	public void importGPSTrack()
	{
		state.gpsTrackSelected = false;
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
			System.out.println("loadGPSTrack() window was closed or the user hit cancel.");
		} 
		else 
		{
			String input = selectedFile.getPath();

			if(debugSettings.viewer)
				System.out.println("User selected GPS Track: " + input);

			state.gpsTrackName = input;
			
			try
			{
				String[] parts = state.gpsTrackName.split("/");
				String fileName = parts[parts.length-1];
				
				parts = fileName.split("\\.");

				if(parts[parts.length-1].equals("gpx"))				// Check that it's a GPX file
				{
					state.gpsTrackFile = new File(state.gpsTrackName);
					state.gpsTrackSelected = true;
				}
				else
				{
					state.gpsTrackSelected = false;
					System.out.println("Bad GPS Track.. doesn't end in .GPX!:"+input);
				}
			}
			catch (Throwable t)
			{
				System.out.println("loadGPSTrack() Error... Throwable: "+t);
			}
		}

		if(state.gpsTrackSelected)
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
			Document doc = dBuilder.parse(state.gpsTrackFile);

			//http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			System.out.println("\nAnalyzing GPS Track:"+state.gpsTrackName);
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
				
				// Should be:
//				ZonedDateTime utc = ZonedDateTime.of(year, month, day, hour, minute, second, 0, ZoneId.of("UTC"));
//				WMV_Time utcTime = new WMV_Time( utc, count, -1, 0, currentField.getTimeZoneID() );

				ZonedDateTime pac = ZonedDateTime.of(year, month, day, hour, minute, second, 0, ZoneId.of("America/Los_Angeles"));
				WMV_Time pacTime = new WMV_Time( pac, count, -1, 0, currentField.getTimeZoneID() );

				float newX = 0.f, newZ = 0.f, newY = 0.f;

				WMV_Model m = currentField.getModel();
				if(m.highLongitude != -1000000 && m.lowLongitude != 1000000 && m.highLatitude != -1000000 && m.lowLatitude != 1000000 && m.highAltitude != -1000000 && m.lowAltitude != 1000000)
				{
					if(m.highLongitude != m.lowLongitude && m.highLatitude != m.lowLatitude)
					{
						newX = PApplet.map(longitude, m.lowLongitude, m.highLongitude, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth); 			// GPS longitude decreases from left to right
						newY = -PApplet.map(elevation, m.lowAltitude, m.highAltitude, 0.f, m.fieldHeight); 										// Convert altitude feet to meters, negative sign to match P3D coordinate space
						newZ = -PApplet.map(latitude, m.lowLatitude, m.highLatitude, -0.5f * m.fieldLength, 0.5f*m.fieldLength); 			// GPS latitude increases from bottom to top, minus sign to match P3D coordinate space
						if(worldSettings.altitudeScaling)	newY *= worldSettings.altitudeScalingFactor;
					}
					else
						newX = newY = newZ = 0.f;
				}

				if(debugSettings.viewer)
				{
					PApplet.print("--> latitude:"+latitude);
					PApplet.print("  longitude:"+longitude);
					System.out.println("  elevation:"+elevation);
					PApplet.print("newX:"+newX);
					PApplet.print("  newY:"+newY);
					System.out.println("  newZ:"+newZ);

					PApplet.print("hour:"+hour);
					PApplet.print("  minute:"+minute);
					PApplet.print("  second:"+second);
					PApplet.print("  year:"+year);
					PApplet.print("  month:"+month);
					System.out.println("  day:"+day);
				}

				PVector newLoc = new PVector(newX, newY, newZ);

				WMV_Waypoint wp = new WMV_Waypoint(count, newLoc, pacTime);			// GPS track node as a Waypoint
				gpsTrack.add(wp);													// Add Waypoint to gpsTrack
				
				count++;
			}
			if(debugSettings.viewer)
				System.out.println("Added "+count+" nodes to gpsTrack...");
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void getSoundLocationsFromGPSTrack()
	{
		for(WMV_Sound s : currentField.getSounds())
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
//			System.out.println("pointsAreInList()... memory:"+memory);

//			for( GMV_Waypoint w : history )
			for(int i = history.size()-1; i >= history.size()-memory; i--)		// Iterate through history from last element to 
			{
				System.out.println("i:"+i);
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
	
	
	void setCurrentCluster(int newCluster, int newFieldTimeSegment)
	{
		if(newCluster >= 0 && newCluster < currentField.getClusters().size())
		{
			state.lastCluster = state.currentCluster;
			WMV_Cluster c = p.getCurrentCluster();

			if(c != null)
				c.timeFading = false;
			
			state.currentCluster = newCluster;
			c = p.getCurrentCluster();
			if(debugSettings.viewer) System.out.println("Set new cluster to: "+newCluster+" newFieldTimeSegment:"+newFieldTimeSegment+" getLocation():"+getLocation());
			
			if(c != null)
			{
				c.timeFading = true;

				WMV_Field f = currentField;
				if(newFieldTimeSegment == -1)						// If == -1, search for time segment
				{
					for(WMV_TimeSegment t : f.getTimeline())			// Search field timeline for cluster time segment
					{
						if(c.getTimeline() != null)
						{
							if(t.equals(f.getTimeSegmentInCluster(c.getID(), 0)))			// Compare cluster time segment to field time segment
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
	}

	/**
	 * Set current field timeline segment with option to adjust currentFieldTimelinesSegment
	 * @param newCurrentFieldTimeSegment
	 */
	public boolean setCurrentFieldTimeSegment( int newCurrentFieldTimeSegment, boolean updateTimelinesSegment )
	{
		state.currentFieldTimeSegment = newCurrentFieldTimeSegment;
		p.p.display.updateCurrentSelectableTime = true;
		boolean success = true;
		if(debugSettings.viewer)
			System.out.println("Setting newCurrentFieldTimeSegment:"+newCurrentFieldTimeSegment+" getLocation():"+getLocation());
		
		if(updateTimelinesSegment)
		{
			int newFieldDate = currentField.getTimeline().get(state.currentFieldTimeSegment).getFieldDateID();
			int newFieldTimelinesSegment = currentField.getTimeline().get(state.currentFieldTimeSegment).getFieldTimelineIDOnDate();
			success = setCurrentTimeSegmentAndDate(newFieldTimelinesSegment, newFieldDate, false);
		}
		
		if(state.currentFieldTimeSegment >= 0 && state.currentFieldTimeSegment < currentField.getTimeline().size())
			return success;
		else
			return false;
	}

	/**
	 * Set current field timelines segment with option to adjust currentFieldTimelineSegment
	 * @param newCurrentFieldTimelinesSegment
	 * @param updateTimelineSegment Whether to update the current field time segment in date-specific timeline as well
	 * @return True if succeeded
	 */
	public boolean setCurrentFieldTimelinesSegment( int newCurrentFieldTimelinesSegment, boolean updateTimelineSegment )
	{
//		if(currentField.getTimelines() != null)
//			System.out.println("Setting newCurrentFieldTimelinesSegment:"+newCurrentFieldTimelinesSegment+" currentFieldDate:"+currentFieldDate+" currentField.getTimelines().get(currentFieldDate).size():"+currentField.getTimelines().get(currentFieldDate).size()+" getLocation():"+getLocation());
//		else
//			System.out.println("currentField.getTimelines() == null!!!");
			
		state.currentFieldTimeSegmentOnDate = newCurrentFieldTimelinesSegment;
		p.p.display.updateCurrentSelectableTime = true;

		if(state.currentFieldDate < currentField.getTimelines().size())
		{
			if(currentField.getTimelines().get(state.currentFieldDate).size() > 0 && state.currentFieldTimeSegmentOnDate < currentField.getTimelines().get(state.currentFieldDate).size())
			{
				if(updateTimelineSegment)
				{
					int fieldTimelineID = currentField.getTimelines().get(state.currentFieldDate).get(state.currentFieldTimeSegmentOnDate).getFieldTimelineID();
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
		boolean success = setCurrentFieldTimelinesSegment( newCurrentFieldTimelinesSegment, updateTimelineSegment );
//		System.out.println("setCurrentTimeSegmentAndDate... newCurrentFieldTimelinesSegment:"+newCurrentFieldTimelinesSegment+" newDate:"+newDate+" success? "+success+" getLocation():"+getLocation());
		return success;
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
				newDate++;
				count++;
				if(count > currentField.getDateline().size()) 
					break;
			}
			if(success)
			{
				int curFieldTimeSegment = currentField.getTimeSegmentOnDate(state.currentFieldTimeSegmentOnDate, state.currentFieldDate).getFieldTimelineID();
//				int curFieldTimeSegment = currentField.getTimelines().get(currentFieldDate).get(currentFieldTimeSegmentOnDate).getFieldTimelineID();
				moveToTimeSegmentInField(currentField.getID(), curFieldTimeSegment, true, true);		// Move to first time segment in field
			}
			else System.out.println("Couldn't move to first time segment...");
			return success;
		}
	}
	
	/**
	 * Set far viewing distance
	 * @param newFarViewingDistance New far viewing distance
	 */
	public void setFarViewingDistance( float newFarViewingDistance)
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
	
	/**
	 * Initialize 2D drawing 
	 */
	void start3DHUD()
	{
		p.p.perspective(getInitFieldOfView(), (float)p.p.width/(float)p.p.height, settings.getNearClippingDistance(), 10000);
		PVector t = new PVector(camera.position()[0], camera.position()[1], camera.position()[2]);
		p.p.translate(t.x, t.y, t.z);
		p.p.rotateY(camera.attitude()[0]);
		p.p.rotateX(-camera.attitude()[1]);
		p.p.rotateZ(camera.attitude()[2]);
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

	public boolean getSelection()
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
	
	public void setAngleThinning(boolean newAngleThinning)
	{
		settings.angleThinning = newAngleThinning;
	}
	
	public void setUserBrightness( float newValue )
	{
		settings.userBrightness = newValue;
	}
	
	/**
	 * @return Current field of view
	 */
	public float getFieldOfView()
	{
		return settings.fieldOfView = camera.fov();
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
		state.moveXDirection = dir;
		state.movingX = true;
	}
	
	public void startMoveYTransition(int dir)
	{
		state.moveYDirection = dir;
		state.movingY = true;
	}
	
	public void startMoveZTransition(int dir)
	{
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
	
	public void startZoomTransition(int dir)
	{
		state.zoomStart = worldState.frameCount;
		state.zoomDirection = dir;
		state.zooming = true;
	}
	
//	private PVector velocity, acceleration, attraction;      // Physics model parameters
//	public PVector getVelocity()
//	{
//		return velocity;
//	}
	
//	/***
//	 * jump()
//	 * @param dest   Destination to jump to
//	 * Jump to a point
//	 */
//	public void jumpTo(PVector dest)
//	{
//		camera.jump(dest.x, dest.y, dest.z);					
//	}

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
