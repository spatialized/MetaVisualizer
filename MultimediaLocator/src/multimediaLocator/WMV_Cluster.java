package multimediaLocator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import processing.core.PApplet;
import processing.core.PVector;

/*********************************************
 * @author davidgordon
 * A spatial cluster of media files representing a virtual point of interest
 */
public class WMV_Cluster 
{
	/* Classes */
	private ML_DebugSettings debugSettings;		// Debug settings
	private WMV_WorldSettings worldSettings;	// World settings
	private WMV_WorldState worldState;			// World state
	private WMV_ViewerSettings viewerSettings;	// Viewer settings
	private WMV_ViewerState viewerState;		// Viewer state
	private WMV_Utilities utilities;			// Utility methods
	private WMV_ClusterState state;				// Cluster state

	/* Time */
	private ArrayList<WMV_Date> dateline;						// Capture dates for this cluster
	private ArrayList<WMV_TimeSegment> timeline;				// Date-independent capture times for this cluster
	private ArrayList<ArrayList<WMV_TimeSegment>> timelines;	

	/* Panoramic Stitching */
//	ArrayList<WMV_Panorama> stitched360;					// Stitched panoramas with full coverage
	ArrayList<WMV_Panorama> stitched;				// Stitched panoramas
	List<Integer> valid;									// List of images that are good stitching candidates

	/* Segmentation */
	public ArrayList<WMV_MediaSegment> segments;			// List of overlapping segments of images or videos

	WMV_Cluster( WMV_WorldSettings newWorldSettings, WMV_WorldState newWorldState, WMV_ViewerSettings newViewerSettings, 
				 ML_DebugSettings newDebugSettings, int _clusterID, float newX, float newY, float newZ) 
	{
		state = new WMV_ClusterState();
		
		state.location = new PVector(newX, newY, newZ);
		state.id = _clusterID;

		utilities = new WMV_Utilities();

		if(newWorldSettings != null) worldSettings = newWorldSettings;	// Update world settings
		if(newWorldState != null) worldState = newWorldState;	// Update world settings
		if(newViewerSettings != null) viewerSettings = newViewerSettings;	// Update viewer settings
		if(newDebugSettings != null) debugSettings = newDebugSettings;
		
		state.images = new ArrayList<Integer>();
		state.panoramas = new ArrayList<Integer>();
		state.videos = new ArrayList<Integer>();
		segments = new ArrayList<WMV_MediaSegment>();
		
//		stitched360 = new ArrayList<WMV_Panorama>();
		stitched = new ArrayList<WMV_Panorama>();

		timeline = new ArrayList<WMV_TimeSegment>();
		state.mediaCount = 0;
	}
	
	/**
	 * Group adjacent, overlapping media into segments, where each image or video is at least stitchingMinAngle from one or more others
 	 */
	void findMediaSegments(ArrayList<WMV_Image> imageList, ArrayList<WMV_Panorama> panoramaList, ArrayList<WMV_Video> videoList)
	{
		List<Integer> allImages = new ArrayList<Integer>();
		for(int i:state.images)
			allImages.add(i);
		
		boolean done = false;
		
		if(allImages.size() == 0) done = true;					// Do nothing if no images
		
		if(allImages.size() == 1)
		{
			List<Integer> curImageList = new ArrayList<Integer>();
			curImageList.add(allImages.get(0));
			
			float left = imageList.get(allImages.get(0)).getDirection();
			float right = imageList.get(allImages.get(0)).getDirection();
			float centerDirection = imageList.get(allImages.get(0)).getDirection();

			float bottom = imageList.get(allImages.get(0)).getElevation();
			float top = imageList.get(allImages.get(0)).getElevation();
			float centerElevation = imageList.get(allImages.get(0)).getElevation();

			WMV_MediaSegment newSegment = new WMV_MediaSegment( 0, curImageList, null, right, left, centerDirection, top, bottom, centerElevation, worldSettings.stitchingMinAngle );
			newSegment.findBorders(imageList);
			segments.add( newSegment );

			if(debugSettings.cluster || debugSettings.field)
				System.out.println("Added media segment in cluster: "+getID()+" with single image...");

			done = true;
		}
		
		while(!done)
		{
			List<Integer> curImages = new ArrayList<Integer>();

			float left = 360.f, right = 0.f, centerDirection;		// Left and right bounds (in degrees) of segment 
			float bottom = 100.f, top = -100.f, centerElevation;	// Top and bottom bounds (in degrees) of segment 

			List<Integer> added = new ArrayList<Integer>();		// Images added to current segment 

			if(debugSettings.cluster || debugSettings.field)
				System.out.println("Finding media segments in cluster: "+getID()+" images.size():"+state.images.size()+" allImages.size():"+allImages.size());

			int count = 0;
			for(int i : allImages)									// Search for images at least stitchingMinAngle from each image
			{
				WMV_Image img = imageList.get(i);
				
				if(curImages.size() == 0)
				{
					curImages.add(img.getID());			// Add first image	
					added.add(count);					// Remove added image from list
				}
				else 
				{
					boolean found = false;					// Have we found a media segment for the image?
					int idx = 0;

					while(!found && idx < curImages.size())
					{
						int m = curImages.get(idx);
						if(imageList.get(m).getID() != img.getID())		// Don't compare image to itself
						{
							if(debugSettings.cluster && debugSettings.detailed)
								System.out.println("Comparing image:"+img.getDirection()+" to m: "+imageList.get(m).getDirection() + " stitchingMinAngle:"+worldSettings.stitchingMinAngle);
							
							if(Math.abs(img.getDirection() - imageList.get(m).getDirection()) < worldSettings.stitchingMinAngle)
							{
								if(Math.abs(img.getElevation() - imageList.get(m).getElevation()) < worldSettings.stitchingMinAngle)
								{
									float direction = img.getDirection();
									float elevation = img.getElevation();
									
									direction = utilities.constrainWrap(direction, 0.f, 360.f);
									elevation = utilities.constrainWrap(elevation, -90.f, 90.f);
									
									if(direction < left) left = direction;
									if(direction > right) right = direction;
									if(elevation < bottom) bottom = elevation;
									if(elevation > top) top = elevation;

									if(debugSettings.cluster || debugSettings.field)
										System.out.println("Added image:"+img.getID()+" to segment...");

									if(!curImages.contains(img.getID()))
										curImages.add(img.getID());		// -- Add video too?
									
									if(!added.contains(count))
										added.add(count);				// Remove added image from list

									found = true;
								}
							}
						}
						else if(allImages.size() == 1)			// Add last image
						{
							curImages.add(img.getID());		// -- Add video too?
							added.add(count);				// Remove added image from list
						}
						
						idx++;
					}
				}
				
				count++;
			}
			
			Collections.sort(added);
			
			for(int i=added.size()-1; i>=0; i--)
			{
				int removed = allImages.remove(i);			// Remove images added to curSegment	-- NOT WORKING
				if(debugSettings.cluster && debugSettings.detailed)
					System.out.println("Removed image ID "+removed+" from allImages");
			}

			if(curImages.size() == 1)			// Only one image
			{
				left = imageList.get(curImages.get(0)).getDirection();
				right = imageList.get(curImages.get(0)).getDirection();
				centerDirection = imageList.get(curImages.get(0)).getDirection();
				
				bottom = imageList.get(allImages.get(0)).getElevation();
				top = imageList.get(allImages.get(0)).getElevation();
				centerElevation = imageList.get(allImages.get(0)).getElevation();
				
				left = utilities.constrainWrap(left, 0.f, 360.f);
				right = utilities.constrainWrap(right, 0.f, 360.f);
				centerDirection = utilities.constrainWrap(centerDirection, 0.f, 360.f);
				bottom = utilities.constrainWrap(bottom, -90.f, 90.f);
				top = utilities.constrainWrap(top, -90.f, 90.f);
				centerElevation = utilities.constrainWrap(centerElevation, -90.f, 90.f);
				
//				if(left < 0.f)
//					left += 360.f;
//				
//				if(right > 360.f)
//					right -= 360.f;
//				
//				if(right < 0.f)
//					right += 360.f;
//							
//				if(top > 90.f)
//					top -= 90.f;
//				
//				if(bottom < -90.f)
//					bottom -= 360.f;
			}
			else
			{
				centerDirection = (right + left) / 2.f;
				centerElevation = (top + bottom) / 2.f;
			}

			WMV_MediaSegment newSegment = new WMV_MediaSegment( segments.size(), curImages, null, left, right, centerDirection, bottom, top, centerElevation, worldSettings.stitchingMinAngle );
			newSegment.findBorders(imageList);
			segments.add( newSegment );

			if((debugSettings.cluster || debugSettings.field))
				System.out.println("Added segment of size: "+curImages.size()+" to cluster segments... Left:"+left+" Center:"+centerDirection+" Right:"+right);
			
			done = (allImages.size() == 1 || allImages.size() == 0);
		}

		state.numSegments = segments.size();						// Number of media segments in the cluster
		if(state.numSegments > 0)
		{
			if(debugSettings.cluster || debugSettings.field)
				System.out.println(" Created "+state.numSegments+" media segments...");

		}
		else if(debugSettings.cluster) 
			System.out.println(" No media segments added... cluster "+getID()+" has no images!");
	}

	/**
	 * @param newImage Image to add
	 * Add an image to the cluster
	 */
	void addImage(WMV_Image newImage)
	{
		if(!state.images.contains(newImage.getID()))
		{
			state.images.add(newImage.getID());
			state.mediaCount++;
		}
	}

	/**
	 * @param newPanorama Panorama to add
	 * Add a panorama to the cluster
	 */
	void addPanorama(WMV_Panorama newPanorama)
	{
		if(!state.hasPanorama) state.hasPanorama = true;
		
		if(!state.panoramas.contains(newPanorama.getID()))
		{
			state.panoramas.add(newPanorama.getID());
			state.mediaCount++;
		}
	}

	/**
	 * @param newImage Image to add
	 * Add a video to the cluster
	 */
	void addVideo(WMV_Video newVideo)
	{
		if(!state.videos.contains(newVideo.getID()))
		{
			state.videos.add(newVideo.getID());
			state.mediaCount++;
		}
	}

	/**
	 * Empty this cluster of all media
	 */
	void empty()
	{
		state.images = new ArrayList<Integer>();
		state.panoramas = new ArrayList<Integer>();
		state.videos = new ArrayList<Integer>();
		state.mediaCount = 0;
		state.active = false;
		state.empty = true;
	}
	
	/**
	 * Initialize cluster from media associated with it; calculate location and assign media to it.
	 */
	void create(ArrayList<WMV_Image> imageList, ArrayList<WMV_Panorama> panoramaList, ArrayList<WMV_Video> videoList) 						
	{			
		state.mediaCount = 0;

		PVector newLocation = new PVector((float)0.0, (float)0.0, (float)0.0);
		empty();
				
		/* Find images associated with this cluster ID */
		for (int i = 0; i < imageList.size(); i++) 
		{
			WMV_Image curImg = imageList.get(i);

			if (curImg.getMediaState().cluster == state.id) 			// If the image is assigned to this cluster
			{
				newLocation.add(curImg.getCaptureLocation());		// Move cluster towards the image
				if(!state.images.contains(curImg.getID()))
				{
					state.images.add(curImg.getID());
					state.mediaCount++;
				}
			}
		}

		/* Find panoramas associated with this cluster ID */
		for (int i = 0; i < panoramaList.size(); i++) 
		{
			WMV_Panorama curPano = panoramaList.get(i);

			if (curPano.getMediaState().cluster == state.id) 			// If the image is assigned to this cluster
			{
				newLocation.add(curPano.getCaptureLocation());		// Move cluster towards the image
				if(!state.panoramas.contains(curPano.getID()))
				{
					state.panoramas.add(curPano.getID());
					state.mediaCount++;
				}
//				if(!panoramas.hasValue(curPano.getID()))
//				{
//					panoramas.append(curPano.getID());
//					state.mediaCount++;
//				}
			}
		}

		/* Find videos associated with this cluster ID */
		for (int i = 0; i < videoList.size(); i++) 
		{
			WMV_Video curVid = videoList.get(i);

			if (curVid.getMediaState().cluster == state.id) 				// If the image is assigned to this cluster
			{
				newLocation.add(curVid.getCaptureLocation());	// Move cluster towards the image
				if(!state.videos.contains(curVid.getID()))
				{
					state.videos.add(curVid.getID());
					state.mediaCount++;
				}
//				if(!videos.hasValue(curVid.getID()))
//				{
//					videos.append(curVid.getID());
//					state.mediaCount++;
//				}
			}
		}

		/* Divide by number of associated points */
		if (state.mediaCount > 0) 				
		{
			newLocation.div(state.mediaCount);
			state.location = newLocation;

//			state.clusterMass = mediaPoints * p.p.mediaPointMass;			// Mass = 4 x number of media points
			state.active = true;
			state.empty = false;
		}
		else
		{
			state.active = false;
			state.empty = true;
		}
	}

	/**
	 * @param mediaID  Single image to determine the cluster location
	 * @param mediaType  0: image 1: panorama 2:video
	 * Create a cluster with a single image
	 */
	void createSingle(int mediaID, int mediaType) 						
	{
		switch(mediaType)
		{
			case 0:
				state.images = new ArrayList<Integer>();
				state.images.add(mediaID);
				break;
			case 1:
				state.panoramas = new ArrayList<Integer>();
				state.panoramas.add(mediaID);
				break;
			case 2:
				state.videos = new ArrayList<Integer>();
				state.videos.add(mediaID);
				break;
			default:
				break;
		}
		
		state.mediaCount = 1;

		state.active = true;
		state.empty = false;
	}

	void display(MultimediaLocator ml)
	{
		if(worldSettings.showUserPanoramas)
		{
			for(WMV_Panorama n : stitched)
			{
				n.update(ml);
				n.display(ml);
			}
		}

//		if(worldSettings.showStitchedPanoramas)
//		{
//			for(WMV_Panorama n : stitched360)
//			{
//				n.update(ml);
//				n.display(ml);
//			}
//		}
	}
	
	public void update( WMV_WorldSettings currentWorldSettings, WMV_WorldState currentWorldState, WMV_ViewerSettings currentViewerSettings, 
						WMV_ViewerState currentViewerState )
	{
		worldSettings = currentWorldSettings;	// Update world settings
		worldState = currentWorldState;	// Update world settings
		viewerSettings = currentViewerSettings;	// Update viewer settings
		viewerState = currentViewerState;		// Update viewer state
	}

	/**
	 * Stitch images based on current selection: if nothing selected, attempt to stitch all media segments in cluster
	 */
	public void stitchImages(ML_Stitcher stitcher, String libraryFolder, ArrayList<WMV_Image> selectedImages)
	{
		if(viewerSettings.multiSelection || viewerSettings.segmentSelection)		// Segment or group is selected
		{
			List<Integer> allSelected = new ArrayList<Integer>();
			List<Integer> visible = new ArrayList<Integer>();

			for( WMV_Image image : selectedImages )
			{
				allSelected.add(image.getID());
				if(image.isVisible()) visible.add(image.getID());
			}

			WMV_Panorama pano = stitcher.stitch(libraryFolder, visible, getID(), -1, allSelected);

			if(pano != null)
			{
				if(debugSettings.panorama || debugSettings.stitching)
					System.out.println("Adding panorama at location x:"+getLocation().x+" y:"+getLocation().y);

				pano.initializeSphere();
				stitched.add(pano);
			}
		}
		else
		{
			for(WMV_MediaSegment m : segments)			// Stitch panorama for each media segment
			{
				if(m.getImages().size() > 1)
				{
					ArrayList<WMV_Image> wholeSegment = new ArrayList<WMV_Image>();	
					ArrayList<WMV_Image> validImages = new ArrayList<WMV_Image>();	
					for( WMV_Image image : selectedImages )
					{
						wholeSegment.add(image);
						if(image.isVisible()) validImages.add(image);
					}

					if(viewerSettings.angleThinning)				// Remove invisible images
					{
						List<Integer> remove = new ArrayList<Integer>();		// Not needed
						
						int count = 0;
						for(WMV_Image v:validImages)
						{
							if(!v.getThinningVisibility())
								remove.add(count);
							count++;
						}

						for(int i=remove.size()-1; i>=0; i--)
							validImages.remove(i);	
					}
					
					List<Integer> valid = new ArrayList<Integer>();
					
					for(WMV_Image img : validImages) 
						valid.add(img.getID());
					
					if(valid.size() > 1)
					{					
						WMV_Panorama pano = stitcher.stitch(libraryFolder, valid, getID(), m.getID(), null);
						
						if(pano != null)
						{
							pano.initializeSphere();
							stitched.add(pano);
						}
					}
				}
			}
		}		
	}
	
	/**
	 * Analyze directions of all images and videos for Thinning Visibility Mode
	 */
	public void analyzeMediaDirections(ArrayList<WMV_Image> imageList, ArrayList<WMV_Video> videoList)
	{
		float thinningAngle = viewerSettings.thinningAngle;									// Angle to thin images and videos by
		int numPerimeterPts = Math.round((float)Math.PI * 2.f / thinningAngle);		// Number of points on perimeter == number of images visible
		int[] perimeterPoints = new int[numPerimeterPts];					// Points to compare each cluster image/video to
		float[] perimeterDistances = new float[numPerimeterPts];			// Distances of images associated with each point
		int videoIdxOffset = imageList.size();
		
		for(int i=0; i<numPerimeterPts; i++)
			perimeterPoints[i] = -1;										// Start with empty perimeter point

		for(int idx : state.images)
		{
			WMV_Image m = imageList.get(idx);
			float imgAngle = (float)Math.toRadians(m.getMetadata().theta);
			m.setThinningVisibility(false);

			for(int i=0; i<numPerimeterPts; i++)
			{
				float ppAngle = i * ((float)Math.PI * 2.f / numPerimeterPts);	// Angle of perimeter point i
				if(perimeterPoints[i] == -1)
				{
					perimeterPoints[i] = idx;
					perimeterDistances[i] = utilities.getAngularDistance(imgAngle, ppAngle);
				}
				else										
				{
					/* Compare image angular distance from point to current closest */
					float imgDist = utilities.getAngularDistance(imgAngle, ppAngle);		
					
					if(imgDist < perimeterDistances[i])
					{
						perimeterPoints[i] = m.getID();
						perimeterDistances[i] = imgDist;
					}
				}
			}
		}
		
		for(int idx : state.videos)
		{
			WMV_Video v = videoList.get(idx);
			float vidAngle = (float)Math.toRadians(v.metadata.theta);
			v.setThinningVisibility(false);

			for(int i=0; i<numPerimeterPts; i++)
			{
				float ppAngle = i * ((float)Math.PI * 2.f / numPerimeterPts);					// Angle of perimeter point i
				if(perimeterPoints[i] == -1)
				{
					perimeterPoints[i] = idx;
					perimeterDistances[i] = utilities.getAngularDistance(vidAngle, ppAngle);
				}
				else											
				{
					/* Compare image angular distance from point to current closest */
					float vidDist = utilities.getAngularDistance(vidAngle, ppAngle);		
					if(vidDist < perimeterDistances[i])
					{
						perimeterPoints[i] = v.getID() + videoIdxOffset;
						perimeterDistances[i] = vidDist;
					}
				}
			}
		}
		
		for(int i=0; i<numPerimeterPts; i++)
		{
			int idx = perimeterPoints[i];
			if(idx != -1)
			{
				if(idx < videoIdxOffset)
					imageList.get(idx).setThinningVisibility(true);
				else
					videoList.get(idx-videoIdxOffset).setThinningVisibility(true);
			}
		}
	}
	
	/**
	 * @param id Media segment ID
	 * @return Cluster media segment with given ID
	 */
	public WMV_MediaSegment getMediaSegment(int id)
	{
		return segments.get(id);
	}
	
	/**
	 * @return Cluster media segments
	 */
	public ArrayList<WMV_MediaSegment> getMediaSegments()
	{
		return segments;
	}
	
	/**
	 * @return Images associated with cluster
	 */
	public ArrayList<WMV_Image> getImages(ArrayList<WMV_Image> imageList)
	{
		ArrayList<WMV_Image> cImages = new ArrayList<WMV_Image>();
		
		for(int i : state.images)
			cImages.add(imageList.get(i));
		
		return cImages;
	}

	/**
	 * @return Panoramas associated with cluster
	 */
	public ArrayList<WMV_Panorama> getPanoramas(ArrayList<WMV_Panorama> panoramaList)
	{
		ArrayList<WMV_Panorama> cPanoramas = new ArrayList<WMV_Panorama>();
		for(int i : state.panoramas)
		{
			cPanoramas.add(panoramaList.get(i));
		}
		return cPanoramas;
	}
	
	/**
	 * @return Videos associated with cluster
	 */
	public ArrayList<WMV_Video> getVideos(ArrayList<WMV_Video> videoList)
	{
		ArrayList<WMV_Video> cVideos = new ArrayList<WMV_Video>();
		for(int i : state.videos)
		{
			cVideos.add(videoList.get(i));
		}
		return cVideos;
	}
	
	/**
	 * @return Are any images or videos in the cluster currently active?
	 */
	public boolean mediaAreActive(ArrayList<WMV_Image> imageList, ArrayList<WMV_Panorama> panoramaList, ArrayList<WMV_Video> videoList)
	{
		boolean active = false;

		if(state.images.size() > 0)
		{
			for(int imgIdx : state.images)
			{
				WMV_Image i = imageList.get(imgIdx);
				if(i.isActive())
					active = true;
			}
		}

		if(state.panoramas.size() > 0)
		{
			for(int panoIdx : state.panoramas)
			{
				WMV_Panorama n = panoramaList.get(panoIdx);
				if(n.isActive())
					active = true;
			}
		}

		if(state.videos.size() > 0)
		{
			for(int vidIdx : state.videos)
			{
				WMV_Video v = videoList.get(vidIdx);
				if(v.isActive())
					active = true;
			}
		}

		return active;
	}
	
	/**
	 * @param newBaseTimeScale New baseTimeScale
	 * Set base time scale, i.e. unit to cycle through during simulation (0 = minute, 1 = hour, 2 = day, 3 = month, 4 = year)
	 */
//	public void setBaseTimeScale(int newBaseTimeScale)
//	{
//		baseTimeScale = newBaseTimeScale;
//	}

	/**
	 * Attract the camera
	 */
	void attractViewer(WMV_Viewer viewer)			
	{
		if(state.isAttractor)												// Attractor clusters do not need to be active, but attract while empty
		{
			if(!state.empty)
			{
				PVector force = getAttractionForce();
//				System.out.println("Cluster #"+getID()+"... Adding attraction force:"+force);
//				p.p.viewer.attraction.add( force );		// Add attraction force to camera 
				viewer.attract( force );		// Add attraction force to camera 
			}
			else 
				System.out.println("Empty Attractor: "+getID());
		}
	}

	/**
	 * @return Distance from cluster center to camera
	 */
	float getClusterDistance()       // Find distance from camera to point in virtual space where photo appears           
	{
		if(viewerState != null)
			return PVector.dist(state.location, viewerState.getLocation());
		else
		{
			System.out.println("getClusterDistance().. viewerState == NULL!!");
			return 0.f;
		}
	}

	/**
	 * @return Attraction force to be applied on the camera
	 */
	public PVector getAttractionForce() 
	{
		PVector force = new PVector(0,0,0);
		
		if(state.location == null || viewerState == null)
			System.out.println("ERROR 1 getAttractionForce() id:"+getID()+" location == null? "+(state.location == null)+" viewerState == null? "+(viewerState == null));
		else if(viewerState.getLocation() == null)
			System.out.println("ERROR 2 getAttractionForce() id:"+getID()+" viewerState.getLocation() == null!");
		else
		{
			force = PVector.sub(state.location, viewerState.getLocation());
			float distance = force.mag();
			force.normalize();

			float mass, dist = getClusterDistance();
//			System.out.println("getAttractionForce, Cluster #"+getID()+"... getClusterDistance:"+PVector.dist(location, p.p.viewer.getLocation()));
			if( dist > worldSettings.clusterFarDistance )
				mass = state.clusterMass * state.farMassFactor * (float)Math.sqrt(distance);	// Increase mass with distance to ensure minimum acceleration
			else
				mass = state.clusterMass;

			float strength;

			if(distance > viewerState.getClusterNearDistance())
			{
				strength = (state.clusterGravity * mass * viewerSettings.cameraMass) / (distance * distance);	// Calculate strength
			}
			else				// Reduce strength of attraction at close distance
			{
				float diff = viewerState.getClusterNearDistance() - distance;
				float factor = 0.5f - PApplet.map(diff, 0.f, viewerState.getClusterNearDistance(), 0.f, 0.5f);
				strength = (state.clusterGravity * mass * viewerSettings.cameraMass) / (distance * distance) * factor;
			}

			force.mult(strength);
		
//		if(p.p.drawForceVector)
//			p.p.p.display.map2D.drawForceVector(force);
		}
		return force; 								// Return force to be applied
	}

	/**
	 * @return ID of first time segment in cluster
	 */
	public int getFirstTimeSegment(boolean anyDate)
	{
		if(anyDate)
			return timeline.get(0).getFieldTimelineID();
		else 
			return getFirstTimeSegmentForDate(dateline.get(0)).getFieldTimelineID();
	}
	
	/**
	 * @param cluster Cluster to merge with
	 * Merge this cluster with given cluster. Empty and make the given cluster non-active.
	 */
	void absorbCluster(WMV_Cluster cluster, ArrayList<WMV_Image> imageList, ArrayList<WMV_Panorama> panoramaList, ArrayList<WMV_Video> videoList)
	{
		if(debugSettings.cluster)
			System.out.println("Merging cluster "+getID()+" with "+cluster.getID());

		/* Find images associated with cluster */
		for (int i = 0; i < imageList.size(); i++) 
		{
			WMV_Image curImg = imageList.get(i);

			if (curImg.getMediaState().cluster == cluster.getID()) 				// If the image is assigned to this cluster
			{
				curImg.getMediaState().cluster = state.id;
				addImage(curImg);
			}
		}

		/* Find panoramas associated with cluster */
		for (int i = 0; i < panoramaList.size(); i++) 
		{
			WMV_Panorama curPano = panoramaList.get(i);

			if (curPano.getMediaState().cluster == cluster.getID()) 				// If the image is assigned to this cluster
			{
				curPano.getMediaState().cluster = state.id;
				addPanorama(curPano);
			}
		}

		/* Find videos associated with cluster */
		for (int i = 0; i < videoList.size(); i++) 
		{
			WMV_Video curVid = videoList.get(i);

			if (curVid.getMediaState().cluster == cluster.getID()) 				// If the image is assigned to this cluster
			{
				curVid.getMediaState().cluster = state.id;
				addVideo(curVid);
			}
		}

		/* Empty merged cluster */
		cluster.empty();
//		cluster.state.images = new ArrayList<Integer>();
//		cluster.state.panoramas = new ArrayList<Integer>();
//		cluster.state.videos = new ArrayList<Integer>();
//		cluster.state.mediaCount = 0;
//		cluster.state.active = false;
//		cluster.state.empty = true;
	}
	
	/**
	 * Analyze associated media capture times (Need to implement: find on which scales it operates, i.e. minute, day, month, year)
	 */
	public void analyzeMedia(ArrayList<WMV_Image> imageList, ArrayList<WMV_Panorama> panoramaList, ArrayList<WMV_Video> videoList) 
	{
		calculateDimensions(imageList, panoramaList, videoList);		// Calculate cluster dimensions (bounds)
		calculateTimes(imageList, panoramaList, videoList);				// Calculate cluster times
		createDateline(imageList, panoramaList, videoList);				// Create dates histograms and analyze for date segments
		createTimeline(imageList, panoramaList, videoList);				// Create timeline independent of date 
		createTimelines();												// Create timeline for each capture date
	}
	
	/**
	 * Create list of all media capture dates in cluster, where index corresponds to index of corresponding timeline in timelines array
	 */
	void createDateline(ArrayList<WMV_Image> imageList, ArrayList<WMV_Panorama> panoramaList, ArrayList<WMV_Video> videoList)
	{
		dateline = new ArrayList<WMV_Date>();										// List of times to analyze

		/* Get times of all media of all types in this cluster */
		for(int i : state.images)
		{
			WMV_Date date = imageList.get(i).time.getDate();
			if(!dateline.contains(date)) 				// Add date if doesn't exist
				dateline.add( date );
		}

		for(int n : state.panoramas)
		{
			WMV_Date date = panoramaList.get(n).time.getDate();
			if(!dateline.contains(date)) 
				dateline.add( date );
		}

		for(int v : state.videos) 
		{
			WMV_Date date = videoList.get(v).time.getDate();
			if(!dateline.contains(date)) 
				dateline.add( date );
		}

		dateline.sort(WMV_Date.WMV_DateComparator);				// Sort dateline  
		
		int count = 0;
		for (WMV_Date d : dateline) 		
		{
			d.setID(count);
			count++;
		}

		if(dateline.size() == 0)
		{
			System.out.println("-------> Cluster "+getID()+" dateline has no points! "+getID()+" images.size():"+state.images.size()+" dateline.size():"+dateline.size());
			empty();
		}
	}
	
	/**
	 * Create date-independent timeline for cluster
	 */
	void createTimeline(ArrayList<WMV_Image> imageList, ArrayList<WMV_Panorama> panoramaList, ArrayList<WMV_Video> videoList)
	{
		ArrayList<WMV_Time> mediaTimes = new ArrayList<WMV_Time>();							// List of times to analyze
		
		/* Get times of all media of all types in this cluster */
		for(int i : state.images)
			mediaTimes.add( imageList.get(i).time );
		for(int n : state.panoramas) 
			mediaTimes.add( panoramaList.get(n).time );
		for(int v : state.videos)
			mediaTimes.add( videoList.get(v).time );

		if(mediaTimes.size() > 0)
		{
			timeline = utilities.calculateTimeSegments(mediaTimes, worldSettings.clusterTimePrecision, getID());	// Get relative (cluster) time segments
			if(timeline != null)
				timeline.sort(WMV_TimeSegment.WMV_TimeLowerBoundComparator);				// Sort timeline points 
			
			int count = 0;
			for (WMV_TimeSegment t : timeline) 												// Number time segments in chronological order
			{
//				t.setID(count);
				t.setClusterTimelineID(count);
				count++;
			}
		}

		if(timeline != null)
		{
			if(timeline.size() == 0)
			{
				System.out.println("Cluster #"+getID()+" timeline has no points!  images.size():"+state.images.size()+" panoramas.size():"+state.panoramas.size()+" videos.size():"+state.videos.size());
				empty();
			}
		}
		else
		{
			System.out.println("Cluster #"+getID()+" timeline is NULL!  images.size():"+state.images.size()+" panoramas.size():"+state.panoramas.size()+" videos.size():"+state.videos.size());
			empty();
		}

		if(timeline.size() == 0)
		{
			System.out.println("Cluster timeline has no points! "+getID()+" images.size():"+state.images.size()+" panoramas.size():"+state.panoramas.size());
			empty();
		}
	}
	
	/**
	 * Create timeline for each date on dateline, where index of a date in dateline matches index of corresponding timeline in timelines array
	 */
	void createTimelines()
	{
		int ct = 0;
		timelines = new ArrayList<ArrayList<WMV_TimeSegment>>();
		for(WMV_Date d : dateline)			// For each date on dateline
		{
			ArrayList<WMV_TimeSegment> newTimeline = new ArrayList<WMV_TimeSegment>();
			for(WMV_TimeSegment t : timeline)		// Add each cluster time segment to this date-specific field timeline 
			{
				if(d.getDate().equals(t.timeline.get(0).getDateAsPVector()))						// Compare time segment date to current timeline date
					newTimeline.add(t);
			}
			
			if(newTimeline.size() > 0)
			{
				if(newTimeline != null) 
					newTimeline.sort(WMV_TimeSegment.WMV_TimeLowerBoundComparator);		// Sort timeline  

				int count = 0;
				for (WMV_TimeSegment t : newTimeline) 									// Number time segments for this date in chronological order
				{
//					t.setID(count);
					t.setClusterDateID(ct);
					t.setClusterTimelinesID(count);
					count++;
				}
				timelines.add( newTimeline );		// Calculate and add timeline to list

				if(debugSettings.cluster)
					System.out.println("Added timeline #"+count+" for cluster #"+getID()+" with "+newTimeline.size()+" points...");
			}
			
			ct++;
		}
	}
	
	/** 
	 * Update cluster time loop
	 */
	void updateTime()
	{
		if(state.timeFading && !state.dateFading && worldState.frameCount % state.timeUnitLength == 0)
		{
			state.currentTime++;															// Increment cluster time
			if(state.currentTime > state.timeCycleLength)
				state.currentTime = 0;
		}
		
		if(state.timeFading && state.dateFading && worldState.frameCount % state.timeUnitLength == 0)
		{
			state.currentTime++;															// Increment cluster time
			if(state.currentTime > state.timeCycleLength)
			{
				state.currentTime = 0;
				state.currentDate++;															// Increment cluster date

				if(debugSettings.cluster || debugSettings.time)
					System.out.println("Reached end of day at p.frameCount:"+worldState.frameCount);

				if(dateline != null)
				{
					if(state.currentDate > dateline.size())
					{
						state.currentDate = 0;
						if(debugSettings.cluster || debugSettings.time)
							System.out.println("Reached end of dateline at p.frameCount:"+worldState.frameCount);
					}
				}
				else if(debugSettings.cluster || debugSettings.time)
				{
					System.out.println("dateline is NULL for cluster "+getID());
				}
			}
		}
	}
	
	public WMV_TimeSegment getFirstTimeSegmentForDate(WMV_Date date)
	{
		boolean found = false;
		int timelineID = 0;
		
		if(dateline != null)
		{
			for(WMV_Date d : dateline)		// Look through cluster dates for date
			{
				if(!found)						
				{	
					if(d == date)										// If cluster has date,
					{
						found = true;												// destination cluster has been found
						//					System.out.println("found cluster with date... "+id+" curTimeSegment:"+curTimeSegment+" date:"+date);
					}
					else
						timelineID++;
				}
			}

			if(found)
			{
				WMV_TimeSegment result;
				if(dateline.size()>1)
				{
//					return timelines.get(timelineID).get(0);
					try{
						result = timelines.get(timelineID).get(0);
					}
					catch(NullPointerException e)
					{
						System.out.println("No timeline found! ... dateline.size():"+dateline.size()+" timelineID:"+timelineID+" ... cluster id:"+getID());
						result = null;
					}
				}
				else
				{
//					return timeline.get(0);
					try{
						result = timeline.get(0);
					}
					catch(NullPointerException e)
					{
						System.out.println("No timeline found!... cluster id:"+getID());
						result = null;
					}
				}
				
				return result;
			}
			else
			{
//				if(debugSettings.cluster || debugSettings.time)
//					System.out.println("Date doesn't exist in cluster #"+getID()+"... "+date);
				return null;
			}
		}
		else 
		{
			if(debugSettings.cluster || debugSettings.time)
			{
				System.out.println("Cluster #"+getID()+" has no dateline but has "+state.mediaCount+" media points!!");
			}
			return null;
		}
	}
	
	public WMV_TimeSegment getLastTimeSegmentForDate(WMV_Date date)
	{
		boolean found = false;
		int timelineID = 0;
//		float date = p.getCurrentField().dateline.get(dateSegment).getCenter();
		
		if(dateline != null)
		{
			for(int index=dateline.size()-1; index >= 0; index--)					// Look through cluster dates for date
			{
				WMV_Date d = dateline.get(index);
				if(!found)						
				{	
					if(d == date)										// If cluster has date,
						found = true;												// destination cluster has been found
					else
						timelineID++;
				}
			}

			if(found)
			{
				if(dateline.size()>1)
					return timelines.get(timelineID).get(timelines.get(timelineID).size()-1);
				else
					return timeline.get(timeline.size()-1);
			}
			else
			{
				System.out.println("Date doesn't exist in cluster #"+getID()+"... "+date.getDate());
				return null;
			}
		}
		else 
		{
			System.out.println("Cluster #"+getID()+" has no dateline... ");
			return null;
		}
	}

	/**
	 * Calculate low and high values for time and date for each media point
	 */
	void calculateTimes(ArrayList<WMV_Image> imageList, ArrayList<WMV_Panorama> panoramaList, ArrayList<WMV_Video> videoList)
	{
//		float longestDayLength = (float) -1.;			// Length of the longest day
		boolean initImageTime = true, initPanoramaTime = true, initVideoTime = true, 
				initImageDate = true, initPanoramaDate = true, initVideoDate = true;	

//		if(debugSettings.cluster && (images.size() != 0 || panoramas.size() != 0 || videos.size() != 0))
//			System.out.println("Analyzing media times in cluster "+id+" ..." + " associatedImages.size():"+images.size()+" associatedPanoramas:"+panoramas.size()+" associatedVideos:"+videos.size());
			
		ArrayList<WMV_Image> cImages = new ArrayList<WMV_Image>();
		ArrayList<WMV_Panorama> cPanoramas = new ArrayList<WMV_Panorama>();
		ArrayList<WMV_Video> cVideos = new ArrayList<WMV_Video>();
		
		for(int i : state.images)
			cImages.add(imageList.get(i));
		for(int n : state.panoramas)
			cPanoramas.add(panoramaList.get(n));
		for(int v : state.videos)
			cVideos.add(videoList.get(v));

		for (WMV_Image i : cImages) 			// Iterate over cluster images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		{
//			float fDayLength = i.time.getDayLength();

			if (initImageTime) 	// Calculate most recent and oldest image time
			{		
				state.highImageTime = i.time.getTime();
				state.lowImageTime = i.time.getTime();
				initImageTime = false;
			}

			if (initImageDate)  	// Calculate most recent and oldest image date
			{	
				state.highImageDate = i.time.getDate().getDaysSince1980();
				state.lowImageDate = i.time.getDate().getDaysSince1980();
				initImageDate = false;
			}

			if (i.time.getTime() > state.highImageTime)
				state.highImageTime = i.time.getTime();
			if (i.time.getTime() < state.lowImageTime)
				state.lowImageTime = i.time.getTime();

			if (i.time.getDate().getDaysSince1980() > state.highImageDate)
				state.highImageDate = i.time.getDate().getDaysSince1980();
			if (i.time.getDate().getDaysSince1980() < state.lowImageDate)
				state.lowImageDate = i.time.getDate().getDaysSince1980();

//			if (fDayLength > longestDayLength)		// Calculate longest day length
//				longestDayLength = fDayLength;
		}
		
		
		for (WMV_Panorama n : cPanoramas) 			// Iterate over cluster panoramas to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		{
//			float fDayLength = n.time.getDayLength();

			if (initPanoramaTime) 		// Calculate most recent and oldest panorama time
			{		
				state.highPanoramaTime = n.time.getTime();
				state.lowPanoramaTime = n.time.getTime();
				initPanoramaTime = false;
			}

			if (initPanoramaDate) 		// Calculate most recent and oldest panorama date
			{		
				state.highPanoramaDate = n.time.getDate().getDaysSince1980();
				state.lowPanoramaDate = n.time.getDate().getDaysSince1980();
				initPanoramaDate = false;
			}

			if (n.time.getTime() > state.highPanoramaTime)
				state.highPanoramaTime = n.time.getTime();
			if (n.time.getTime() < state.lowPanoramaTime)
				state.lowPanoramaTime = n.time.getTime();

			if (n.time.getDate().getDaysSince1980() > state.highPanoramaDate)
				state.highPanoramaDate = n.time.getDate().getDaysSince1980();
			if (n.time.getDate().getDaysSince1980() < state.lowPanoramaDate)
				state.lowPanoramaDate = n.time.getDate().getDaysSince1980();

//			if (fDayLength > longestPanoramaDayLength)		// Calculate longest panorama day length
//				longestPanoramaDayLength = fDayLength;
		}
		
		for (WMV_Video v : cVideos) 			// Iterate over cluster videos to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		{
//			float fDayLength = v.time.getDayLength();

			if (initVideoTime) 		// Calculate most recent and oldest video time
			{		
				state.highVideoTime = v.time.getTime();
				state.lowVideoTime = v.time.getTime();
				initVideoTime = false;
			}

			if (initVideoDate) 		// Calculate most recent and oldest video date
			{		
				state.highVideoDate = v.time.getDate().getDaysSince1980();
				state.lowVideoDate = v.time.getDate().getDaysSince1980();
				initImageDate = false;
			}

			if (v.time.getTime() > state.highVideoTime)
				state.highVideoTime = v.time.getTime();
			if (v.time.getTime() < state.lowVideoTime)
				state.lowVideoTime = v.time.getTime();

			if (v.time.getDate().getDaysSince1980() > state.highVideoDate)
				state.highVideoDate = v.time.getDate().getDaysSince1980();
			if (v.time.getDate().getDaysSince1980() < state.lowVideoDate)
				state.lowVideoDate = v.time.getDate().getDaysSince1980();

//			if (fDayLength > longestVideoDayLength)		// Calculate longest video day length
//				longestVideoDayLength = fDayLength;
		}

		state.lowTime = state.lowImageTime;
		if (state.lowPanoramaTime < state.lowTime)
			state.lowTime = state.lowPanoramaTime;
		if (state.lowVideoTime < state.lowTime)
			state.lowTime = state.lowVideoTime;

		state.highTime = state.highImageTime;
		if (state.highPanoramaTime > state.highTime)
			state.highTime = state.highPanoramaTime;
		if (state.highVideoTime > state.highTime)
			state.highTime = state.highVideoTime;

		state.lowDate = state.lowImageDate;
		if (state.lowPanoramaDate < state.lowDate)
			state.lowDate = state.lowPanoramaDate;
		if (state.lowVideoDate < state.lowDate)
			state.lowDate = state.lowVideoDate;

		state.highDate = state.highImageDate;
		if (state.highPanoramaDate > state.highDate)
			state.highDate = state.highPanoramaDate;
		if (state.highVideoDate > state.highDate)
			state.highDate = state.highVideoDate;
	}
	
	/**
	 * Display cluster data
	 */
	public void displayClusterData()
	{
		System.out.println("Cluster "+state.id+" High Longitude:" + state.highLongitude);
		System.out.println("Cluster "+state.id+" High Latitude:" + state.highLatitude);
		System.out.println("Cluster "+state.id+" High Altitude:" + state.highAltitude);
		System.out.println("Cluster "+state.id+" Low Longitude:" + state.lowLongitude);
		System.out.println("Cluster "+state.id+" Low Latitude:" + state.lowLatitude);
		System.out.println("Cluster "+state.id+" Low Altitude:" + state.lowAltitude);	
		
		System.out.println("Cluster "+state.id+" High Time:" + state.highTime);
		System.out.println("Cluster "+state.id+" High Date:" + state.highDate);
		System.out.println("Cluster "+state.id+" Low Time:" + state.lowTime);
		System.out.println("Cluster "+state.id+" Low Date:" + state.lowDate);
		
		System.out.println("Cluster "+state.id+" High Latitude:" + state.highLatitude);
		System.out.println("Cluster "+state.id+" Low Latitude:" + state.lowLatitude);
		System.out.println("Cluster "+state.id+" High Longitude:" + state.highLongitude);
		System.out.println("Cluster "+state.id+" Low Longitude:" + state.lowLongitude);
		System.out.println(" ");
	}
	
	/**
	 * Calculate high and low longitude, latitude and altitude for cluster
	 */
	void calculateDimensions(ArrayList<WMV_Image> imageList, ArrayList<WMV_Panorama> panoramaList, ArrayList<WMV_Video> videoList)
	{
		boolean init = true;	

		for (int img : state.images) 				// Iterate over associated images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		{
			WMV_Image i = imageList.get(img);
			PVector gpsLocation = i.getGPSLocation();
			
			if (init) 	
			{	
				state.highLongitude = gpsLocation.x;
				state.lowLongitude = gpsLocation.x;
			}
			if (init) 
			{	
				state.highLatitude = gpsLocation.z;
				state.lowLatitude = gpsLocation.z;
			}
			if (init) 	
			{		
				state.highAltitude = gpsLocation.y;
				state.lowAltitude = gpsLocation.y;
				init = false;
			}

			if (gpsLocation.x > state.highLongitude)
				state.highLongitude = gpsLocation.x;
			if (gpsLocation.x < state.lowLongitude)
				state.lowLongitude = gpsLocation.x;
			if (gpsLocation.y > state.highAltitude)
				state.highAltitude = gpsLocation.y;
			if (gpsLocation.y < state.lowAltitude)
				state.lowAltitude = gpsLocation.y;
			if (gpsLocation.z > state.highLatitude)
				state.highLatitude = gpsLocation.z;
			if (gpsLocation.z < state.lowLatitude)
				state.lowLatitude = gpsLocation.z;
		}

		for (int pano : state.panoramas) 				// Iterate over associated images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		{
			WMV_Panorama n = panoramaList.get(pano);
			PVector gpsLocation = n.getGPSLocation();

			if (gpsLocation.x > state.highLongitude)
				state.highLongitude = gpsLocation.x;
			if (gpsLocation.x < state.lowLongitude)
				state.lowLongitude = gpsLocation.x;
			if (gpsLocation.y > state.highAltitude)
				state.highAltitude = gpsLocation.y;
			if (gpsLocation.y < state.lowAltitude)
				state.lowAltitude = gpsLocation.y;
			if (gpsLocation.z > state.highLatitude)
				state.highLatitude = gpsLocation.z;
			if (gpsLocation.z < state.lowLatitude)
				state.lowLatitude = gpsLocation.z;
		}
		
		for (int vid : state.videos) 				// Iterate over associated images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		{
			WMV_Video v = videoList.get(vid);
			PVector gpsLocation = v.getGPSLocation();
			
			if (gpsLocation.x > state.highLongitude)
				state.highLongitude = gpsLocation.x;
			if (gpsLocation.x < state.lowLongitude)
				state.lowLongitude = gpsLocation.x;
			if (gpsLocation.y > state.highAltitude)
				state.highAltitude = gpsLocation.y;
			if (gpsLocation.y < state.lowAltitude)
				state.lowAltitude = gpsLocation.y;
			if (gpsLocation.z > state.highLatitude)
				state.highLatitude = gpsLocation.z;
			if (gpsLocation.z < state.lowLatitude)
				state.lowLatitude = gpsLocation.z;
		}
	}
	
	public void setState( WMV_ClusterState newClusterState )
	{
		state = newClusterState;
	}

	public WMV_ClusterState getState()
	{
		return state;
	}

	/**
	 * @return This cluster as a waypoint for navigation
	 */
	WMV_Waypoint getClusterAsWaypoint()
	{
		WMV_Waypoint result = new WMV_Waypoint(getID(), getLocation(), null);		// -- Calculate time instead of null!!
		return result;
	}
	
	public int getMediaCount()
	{
		int count = state.images.size() + state.videos.size() + state.panoramas.size(); // + sounds.size();
		return count;
	}
	
	public void setMediaCount(int newMediaCount)
	{
		state.mediaCount = newMediaCount;
	}

	public List<Integer> getImageIDs()
	{
		return state.images;
	}

	public List<Integer> getPanoramaIDs()
	{
		return state.panoramas;
	}

	public List<Integer> getVideoIDs()
	{
		return state.videos;
	}
	
	public ArrayList<WMV_TimeSegment> getTimeline()
	{
		return timeline;
	}

	public ArrayList<ArrayList<WMV_TimeSegment>> getTimelines()
	{
		return timelines;
	}

	public ArrayList<WMV_Date> getDateline()
	{
		return dateline;
	}
	
	public void setActive(boolean newActive)
	{
		state.active = newActive;
	}
	
	public void setEmpty(boolean newEmpty)
	{
		state.empty = newEmpty;
	}
		
	/**
	 * Set this cluster as an attractor
	 */
	public void setAttractor(boolean newState)
	{
		state.isAttractor = newState;
	}

	public void setSingle(boolean newState)
	{
		state.single = newState;
	}

	public boolean isActive()
	{
		return state.active;
	}
	
	public boolean isEmpty()
	{
		return state.empty;
	}
	
	public boolean isSingle()
	{
		return state.single;
	}
	
	public void setMass(float newMass)
	{
		state.clusterMass = newMass;
	}
	
	public boolean isAttractor()
	{
		return state.isAttractor;
	}
	
	public float getClusterMass()
	{
		return state.clusterMass;
	}
	
	public void setID(int newID)
	{
		state.id = newID;
	}
	
	public int getID()
	{
		return state.id;
	}
	
	public PVector getLocation()
	{
		return state.location;
	}
	
	public void setLocation(PVector newLocation)
	{
		state.location = newLocation;
	}
	
	public boolean isSelected()
	{
		return state.selected;
	}
	
	public void setSelected(boolean newState)
	{
		state.selected = newState;
	}
}