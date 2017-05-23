package multimediaLocator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import processing.core.PVector;

/*********************************************
 * Cluster of media files representing a spatial point of interest
 * @author davidgordon
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

	/* Panoramic Stitching */
	ArrayList<WMV_Panorama> stitched;				// Stitched panoramas
	List<Integer> valid;									// List of images that are good stitching candidates

	/* Segmentation */
	public ArrayList<WMV_MediaSegment> segments;			// List of overlapping segments of images or videos

	/**
	 * Constructor for spatial cluster of media
	 * @param newWorldSettings World settings
	 * @param newWorldState World state
	 * @param newViewerSettings Viewer settings
	 * @param newDebugSettings Debug settings
	 * @param newClusterID Cluster ID
	 * @param newLocation Cluster location
	 */
	public WMV_Cluster( WMV_WorldSettings newWorldSettings, WMV_WorldState newWorldState, WMV_ViewerSettings newViewerSettings, 
						ML_DebugSettings newDebugSettings, int newClusterID, PVector newLocation ) 
	{
		state = new WMV_ClusterState();
		state.id = newClusterID;
		state.location = new PVector(newLocation.x, newLocation.y, newLocation.z);

		utilities = new WMV_Utilities();

		if(newWorldSettings != null) worldSettings = newWorldSettings;	// Update world settings
		if(newWorldState != null) worldState = newWorldState;	// Update world settings
		if(newViewerSettings != null) viewerSettings = newViewerSettings;	// Update viewer settings
		if(newDebugSettings != null) debugSettings = newDebugSettings;
		
		state.images = new ArrayList<Integer>();
		state.panoramas = new ArrayList<Integer>();
		state.videos = new ArrayList<Integer>();
		state.sounds = new ArrayList<Integer>();
		segments = new ArrayList<WMV_MediaSegment>();
		
		stitched = new ArrayList<WMV_Panorama>();

		state.timeline = new WMV_Timeline();
		state.timeline.initialize(null);
		
		state.mediaCount = 0;
	}

	/**
	 * Initialize cluster timeline and media time objects
	 */
	void initializeTime()
	{
		state.timeline.getLower().getLower().initializeTime();
		state.timeline.getLower().getCenter().initializeTime();
		state.timeline.getLower().getUpper().initializeTime();
		state.timeline.getUpper().getLower().initializeTime();
		state.timeline.getUpper().getCenter().initializeTime();
		state.timeline.getUpper().getUpper().initializeTime();

		for(WMV_TimeSegment ts : state.timeline.timeline)
		{
			ts.getLower().initializeTime();
			ts.getCenter().initializeTime();
			ts.getUpper().initializeTime();

			for(WMV_Time t : ts.timeline)
				t.initializeTime();
		}
		
		for(WMV_Timeline tl : state.timelines)
		{
			tl.getLower().getLower().initializeTime();
			tl.getLower().getCenter().initializeTime();
			tl.getLower().getUpper().initializeTime();
			tl.getUpper().getLower().initializeTime();
			tl.getUpper().getCenter().initializeTime();
			tl.getUpper().getUpper().initializeTime();

			for(WMV_TimeSegment ts : tl.timeline)
			{
				ts.getLower().initializeTime();
				ts.getCenter().initializeTime();
				ts.getUpper().initializeTime();

				for(WMV_Time t : ts.timeline)
					t.initializeTime();
			}
		}
		
		for(WMV_Date d : state.dateline)
		{
			if(!d.timeInitialized)
				d.initializeTime();
		}
	}
	
	/**
	 * Add an image to the cluster
	 * @param newImage Image to add
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
	 * Add a panorama to the cluster
	 * @param newPanorama Panorama to add
	 */
	void addPanorama(WMV_Panorama newPanorama)
	{
		if(!state.hasPanorama()) state.setHasPanorama(true);
		
		if(!state.panoramas.contains(newPanorama.getID()))
		{
			state.panoramas.add(newPanorama.getID());
			state.mediaCount++;
		}
	}

	/**
	 * Add a video to the cluster
	 * @param newImage Image to add
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
	 * Add a video to the cluster
	 * @param newImage Image to add
	 */
	void addSound(WMV_Sound newSound)
	{
		if(!state.sounds.contains(newSound.getID()))
		{
			state.sounds.add(newSound.getID());
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
		state.sounds = new ArrayList<Integer>();
		state.mediaCount = 0;
		state.active = false;
		state.empty = true;
	}

	/**
	 * Initialize cluster from media associated with it; calculate location and assign media to it.
	 * @param imageList Image list
	 * @param panoramaList Panorama list
	 * @param videoList Video list
	 * @param soundList Sound list
	 */
	void create(ArrayList<WMV_Image> imageList, ArrayList<WMV_Panorama> panoramaList, ArrayList<WMV_Video> videoList, ArrayList<WMV_Sound> soundList) 						
	{			
		state.mediaCount = 0;

		PVector newLocation = new PVector(0.f, 0.f, 0.f);
		empty();											// Empty cluster
				
		/* Find images associated with this cluster */
		for (int i = 0; i < imageList.size(); i++) 
		{
			WMV_Image curImg = imageList.get(i);

			if (curImg.getMediaState().getClusterID() == state.id) 			// If the image is assigned to this cluster
			{
				newLocation.add(curImg.getCaptureLocation());		// Move cluster towards the image
				if(!state.images.contains(curImg.getID()))
				{
					state.images.add(curImg.getID());
					state.mediaCount++;
				}
			}
		}

		/* Find panoramas associated with this cluster */
		for (int i = 0; i < panoramaList.size(); i++) 
		{
			WMV_Panorama curPano = panoramaList.get(i);

			if (curPano.getMediaState().getClusterID() == state.id) 			// If the image is assigned to this cluster
			{
				newLocation.add(curPano.getCaptureLocation());		// Move cluster towards the image
				if(!state.panoramas.contains(curPano.getID()))
				{
					state.panoramas.add(curPano.getID());
					state.mediaCount++;
				}
			}
		}

		/* Find videos associated with this cluster */
		for (int i = 0; i < videoList.size(); i++) 
		{
			WMV_Video curVid = videoList.get(i);

			if (curVid.getMediaState().getClusterID() == state.id) 				// If the image is assigned to this cluster
			{
				newLocation.add(curVid.getCaptureLocation());	// Move cluster towards the image
				if(!state.videos.contains(curVid.getID()))
				{
					state.videos.add(curVid.getID());
					state.mediaCount++;
				}
			}
		}

		/* Find sounds associated with this cluster */
		if(soundList != null)
		{
			for (int i = 0; i < soundList.size(); i++) 
			{
				WMV_Sound curSnd = soundList.get(i);

				if (curSnd.getMediaState().getClusterID() == state.id) 				// If the image is assigned to this cluster
				{
					newLocation.add(curSnd.getCaptureLocation());	// Move cluster towards the image
					if(!state.sounds.contains(curSnd.getID()))
					{
						state.sounds.add(curSnd.getID());
						state.mediaCount++;
					}
				}
			}
		}

		/* Divide by number of associated points */
		if (state.mediaCount > 0) 				
		{
			newLocation.div(state.mediaCount);
			state.location = newLocation;
			state.active = true;
			state.empty = false;
//			state.clusterMass = mediaPoints * p.p.mediaPointMass;			// Mass = 4 x number of media points
		}
		else
		{
			state.active = false;
			state.empty = true;
		}
	}

	/**
	 * Initialize this cluster with a single media object
	 * @param mediaID  Single image to determine the cluster location
	 * @param mediaType  0: image 1: panorama 2:video
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
			case 3:
				state.sounds = new ArrayList<Integer>();
				state.sounds.add(mediaID);
				break;
			default:
				break;
		}
		
		state.mediaCount = 1;
		state.active = true;
		state.empty = false;
	}

	/**
	 * Display user stitched panoramas in cluster
	 * @param ml Parent app
	 */
	void displayUserPanoramas(MultimediaLocator ml)
	{
		if(worldSettings.showUserPanoramas)
		{
			for(WMV_Panorama n : stitched)
			{
				ml.world.getCurrentField().updatePanorama(ml, n);
//				ml.world.getCurrentField().displayPanorama(ml, n);
			}
		}
	}
	
	/**
	 * Update current world state and settings
	 * @param currentWorldSettings
	 * @param currentWorldState
	 * @param currentViewerSettings
	 * @param currentViewerState
	 */
	public void update( WMV_WorldSettings currentWorldSettings, WMV_WorldState currentWorldState, WMV_ViewerSettings currentViewerSettings, 
						WMV_ViewerState currentViewerState )
	{
		worldSettings = currentWorldSettings;	// Update world settings
		worldState = currentWorldState;			// Update world settings
		viewerSettings = currentViewerSettings;	// Update viewer settings
		viewerState = currentViewerState;		// Update viewer state
	}

	/**
	 * Stitch images based on current selection: if nothing selected, attempt to stitch all media segments in cluster
	 * @param stitcher Stitching object to use
	 * @param libraryFolder Library folder
	 * @param selectedImages Selected images to stitch
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
	 * Analyze associated media capture times (Need to implement: find on which scales it operates, i.e. minute, day, month, year)
	 */
	public void createModel( ArrayList<WMV_Image> imageList, ArrayList<WMV_Panorama> panoramaList, ArrayList<WMV_Video> videoList,
							 ArrayList<WMV_Sound> soundList ) 
	{
		calculateDimensions(imageList, panoramaList, videoList, soundList);			// Calculate cluster dimensions (bounds)
		calculateTimes(imageList, panoramaList, videoList, soundList);				// Calculate cluster times
		createDateline(imageList, panoramaList, videoList, soundList);				// Create dates histograms and analyze for date segments
		createTimeline(imageList, panoramaList, videoList, soundList);				// Create timeline independent of date 
		createTimelines();												// Create timeline for each capture date
	}
	
	/**
	 * Create list of all media capture dates in cluster, where index corresponds to index of corresponding timeline in timelines array
	 * @param imageList Image list
	 * @param panoramaList Panorama list
	 * @param videoList Video list
	 * @param soundList Sound list
	 */
	void createDateline(ArrayList<WMV_Image> imageList, ArrayList<WMV_Panorama> panoramaList, ArrayList<WMV_Video> videoList, ArrayList<WMV_Sound> soundList)
	{
		state.dateline = new ArrayList<WMV_Date>();										// List of times to analyze

		/* Get times of all media of all types in this cluster */
		for(int i : state.images)
		{
			WMV_Date date = imageList.get(i).time.asDate();
			if(!state.dateline.contains(date)) 				// Add date if doesn't exist
				state.dateline.add( date );
		}

		for(int n : state.panoramas)
		{
			WMV_Date date = panoramaList.get(n).time.asDate();
			if(!state.dateline.contains(date)) 
				state.dateline.add( date );
		}

		for(int v : state.videos) 
		{
			WMV_Date date = videoList.get(v).time.asDate();
			if(!state.dateline.contains(date)) 
				state.dateline.add( date );
		}

		for(int s : state.sounds) 
		{
			WMV_Date date = soundList.get(s).time.asDate();
			if(!state.dateline.contains(date)) 
				state.dateline.add( date );
		}

		state.dateline.sort(WMV_Date.WMV_DateComparator);				// Sort dateline  
		
		int count = 0;
		for (WMV_Date d : state.dateline) 		
		{
			d.setID(count);
			count++;
		}

		if(state.dateline.size() == 0)
		{
			System.out.println("-------> Cluster "+getID()+" dateline has no points! "+getID()+" images.size():"+state.images.size()+" dateline.size():"+state.dateline.size());
			empty();
		}
	}
	
	/**
	 * Create date-independent timeline for cluster
	 * @param imageList Image list
	 * @param panoramaList Panorama list
	 * @param videoList Video list
	 * @param soundList Sound list
	 */
	void createTimeline(ArrayList<WMV_Image> imageList, ArrayList<WMV_Panorama> panoramaList, ArrayList<WMV_Video> videoList, ArrayList<WMV_Sound> soundList)
	{
		ArrayList<WMV_Time> mediaTimes = new ArrayList<WMV_Time>();							// List of times to analyze
		
		/* Get times of all media in this cluster */
		for(int i : state.images)
			mediaTimes.add( imageList.get(i).time );
		for(int n : state.panoramas) 
			mediaTimes.add( panoramaList.get(n).time );
		for(int v : state.videos)
			mediaTimes.add( videoList.get(v).time );
		for(int s : state.sounds)
			mediaTimes.add( soundList.get(s).time );

		if(mediaTimes.size() > 0)
		{
			state.timeline.timeline = utilities.createTimeline(mediaTimes, worldSettings.clusterTimePrecision, getID());	// Get relative (cluster) time segments
			state.timeline.finishTimeline();			// Finish timeline / set bounds

//			if(state.timeline.timeline != null)
//			{
//				if(state.timeline.timeline.size() > 0)
//				{
//					state.timeline.timeline.sort(WMV_TimeSegment.WMV_TimeLowerBoundComparator);				// Sort timeline points 
//					
//					ArrayList<WMV_Time> test = new ArrayList<WMV_Time>();
//					
//					for(WMV_TimeSegment t : state.timeline.timeline)
//						for(WMV_Time tm : t.getTimeline())
//							test.add(tm);
//
//					if(test.size() != mediaTimes.size())
//						System.out.println("Incomplete timeline created!  test.size():"+test.size()+" mediaTimes.size():"+mediaTimes.size()+"...");
//				}
//				else
//					System.out.println("No timeline created for cluster #"+getID()+"!!!");
//			}
//			else
//				System.out.println("NULL timeline created for cluster #"+getID()+"!!!!");
			
			int count = 0;
			for (WMV_TimeSegment t : state.timeline.timeline) 												// Number time segments in chronological order
			{
				t.setClusterTimelineID(count);
				count++;
			}
		}

		if(state.timeline.timeline != null)
		{
			if(state.timeline.timeline.size() == 0)
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

		if(state.timeline.timeline.size() == 0)
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
		state.timelines = new ArrayList<WMV_Timeline>();
		for(WMV_Date d : state.dateline)			// For each date on dateline
		{
			WMV_Timeline newTimeline = new WMV_Timeline();
			newTimeline.initialize(null);
			
			for(WMV_TimeSegment t : state.timeline.timeline)		// Add each cluster time segment to this date-specific field timeline 
			{
				if(d.getDate().equals(t.timeline.get(0).getDateAsPVector()))						// Compare time segment date to current timeline date
					newTimeline.timeline.add(t);
			}
			
			if(newTimeline.timeline.size() > 0)
			{
				if(newTimeline != null) 
					newTimeline.timeline.sort(WMV_TimeSegment.WMV_TimeLowerBoundComparator);		// Sort timeline  

				int count = 0;
				for (WMV_TimeSegment t : newTimeline.timeline) 									// Number time segments for this date in chronological order
				{
					t.setClusterDateID(ct);
					t.setClusterTimelinesID(count);
					count++;
				}
				
				newTimeline.finishTimeline();			// Calculate upper and lower time segment bounds
				state.timelines.add( newTimeline );		// Calculate and add timeline to list

				if(debugSettings.cluster && debugSettings.detailed)
					System.out.println("Added timeline #"+count+" for cluster #"+getID()+" with "+newTimeline.timeline.size()+" points...");
			}
			
			ct++;
		}
	}

	/**
	 * Analyze directions of all images and videos
	 * @param imageList Image list
	 * @param videoList Video list
	 */
	public void findMediaDirections(ArrayList<WMV_Image> imageList, ArrayList<WMV_Video> videoList)
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
					float imgDist = utilities.getAngularDistance(imgAngle, ppAngle);		/* Compare image angular distance from point to current closest */
					
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
				float ppAngle = i * ((float)Math.PI * 2.f / numPerimeterPts);				// Angle of perimeter point i
				if(perimeterPoints[i] == -1)
				{
					perimeterPoints[i] = idx;
					perimeterDistances[i] = utilities.getAngularDistance(vidAngle, ppAngle);
				}
				else											
				{
					float vidDist = utilities.getAngularDistance(vidAngle, ppAngle);		/* Compare video angular distance from point to current closest */
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
	 * Group adjacent overlapping media into segments where each image is at least stitchingMinAngle from one or more others
	 * @param imageList Image list
	 */
	void findMediaSegments(ArrayList<WMV_Image> imageList)
	{
		List<Integer> allImages = new ArrayList<Integer>();
		for(int i:state.images)
			allImages.add(i);
		
		boolean done = false;
		
		if(allImages.size() == 0) 
			done = true;						// Do nothing if no images
		
		if(allImages.size() == 1)
		{
			List<Integer> curImageList = new ArrayList<Integer>();
			curImageList.add(allImages.get(0));
			
			float left = imageList.get(allImages.get(0)).getDirection();
			float right = imageList.get(allImages.get(0)).getDirection();
			float centerDirection = imageList.get(allImages.get(0)).getDirection();

			float bottom = imageList.get(allImages.get(0)).getElevationAngle();
			float top = imageList.get(allImages.get(0)).getElevationAngle();
			float centerElevation = imageList.get(allImages.get(0)).getElevationAngle();

			WMV_MediaSegment newSegment = new WMV_MediaSegment( 0, curImageList, right, left, centerDirection, top, bottom, centerElevation, worldSettings.stitchingMinAngle );
			newSegment.findImageBorders(imageList);
			segments.add( newSegment );

			if(debugSettings.cluster && debugSettings.detailed)
				System.out.println("Added media segment in cluster: "+getID()+" with single image...");

			done = true;
		}
		
		while(!done)
		{
			List<Integer> curImages = new ArrayList<Integer>();

			float left = 360.f, right = 0.f, centerDirection;		// Left and right bounds (in degrees) of segment 
			float bottom = 100.f, top = -100.f, centerElevation;	// Top and bottom bounds (in degrees) of segment 

			List<Integer> added = new ArrayList<Integer>();		// Images added to current segment 

			if(debugSettings.cluster && debugSettings.detailed)
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
								if(Math.abs(img.getElevationAngle() - imageList.get(m).getElevationAngle()) < worldSettings.stitchingMinAngle)
								{
									float direction = img.getDirection();
									float elevation = img.getElevationAngle();
									
									direction = utilities.constrainWrap(direction, 0.f, 360.f);
									elevation = utilities.constrainWrap(elevation, -90.f, 90.f);
									
									if(direction < left) left = direction;
									if(direction > right) right = direction;
									if(elevation < bottom) bottom = elevation;
									if(elevation > top) top = elevation;

									if(debugSettings.cluster && debugSettings.detailed)
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
			
			Collections.sort(added);						// Sort added list
			
			for(int i=added.size()-1; i>=0; i--)
			{
				int removed = allImages.remove(i);			// Remove images added to curSegment
				if(debugSettings.cluster && debugSettings.detailed) System.out.println("Removed image ID "+removed+" from allImages");
			}

			if(curImages.size() == 1)							// Only one image
			{
				left = imageList.get(curImages.get(0)).getDirection();
				right = imageList.get(curImages.get(0)).getDirection();
				centerDirection = imageList.get(curImages.get(0)).getDirection();
				
				bottom = imageList.get(allImages.get(0)).getElevationAngle();
				top = imageList.get(allImages.get(0)).getElevationAngle();
				centerElevation = imageList.get(allImages.get(0)).getElevationAngle();
				
				left = utilities.constrainWrap(left, 0.f, 360.f);
				right = utilities.constrainWrap(right, 0.f, 360.f);
				centerDirection = utilities.constrainWrap(centerDirection, 0.f, 360.f);
				bottom = utilities.constrainWrap(bottom, -90.f, 90.f);
				top = utilities.constrainWrap(top, -90.f, 90.f);
				centerElevation = utilities.constrainWrap(centerElevation, -90.f, 90.f);
			}
			else
			{
				centerDirection = (right + left) / 2.f;
				centerElevation = (top + bottom) / 2.f;
			}

			WMV_MediaSegment newSegment = new WMV_MediaSegment( segments.size(), curImages, left, right, centerDirection, bottom, top,
																centerElevation, worldSettings.stitchingMinAngle );
			newSegment.findImageBorders(imageList);				// Find image borders in segment
			segments.add( newSegment );							// Add segment

			if((debugSettings.cluster && debugSettings.detailed))
				System.out.println("Added segment of size: "+curImages.size()+" to cluster segments... Left:"+left+" Center:"+centerDirection+" Right:"+right);
			
			done = (allImages.size() == 1 || allImages.size() == 0);
		}

		state.numSegments = segments.size();						// Number of media segments in the cluster
		if(state.numSegments > 0)
		{
			if(debugSettings.cluster && debugSettings.detailed) System.out.println(" Created "+state.numSegments+" media segments...");

		}
		else if(debugSettings.cluster && debugSettings.detailed) System.out.println(" No media segments added... cluster "+getID()+" has no images!");
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
	 * @return Media segments in cluster
	 */
	public ArrayList<WMV_MediaSegment> getMediaSegments()
	{
		return segments;
	}
	
	/**
	 * @param imageList Image List
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
	 * @param panoramaList Panorama List
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
	 * @param videoList Video list
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
	 * @param soundList Sound list
	 * @return Sounds associated with cluster
	 */
	public ArrayList<WMV_Sound> getSounds(ArrayList<WMV_Sound> soundList)
	{
		ArrayList<WMV_Sound> cSounds = new ArrayList<WMV_Sound>();
		for(int i : state.sounds)
		{
			cSounds.add(soundList.get(i));
		}
		return cSounds;
	}
	
	/**
	 * Detect whether any media in cluster are active
	 * @param imageList Image list
	 * @param panoramaList Panorama list 
	 * @param videoList Video list
	 * @param soundList Sound list
	 * @return Whether any media in the cluster are currently active
	 */
	public boolean mediaAreActive( ArrayList<WMV_Image> imageList, ArrayList<WMV_Panorama> panoramaList, ArrayList<WMV_Video> videoList, 
								   ArrayList<WMV_Sound> soundList )
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

		if(state.sounds.size() > 0)
		{
			for(int sndIdx : state.sounds)
			{
				WMV_Sound s = soundList.get(sndIdx);
				if(s.isActive())
					active = true;
			}
		}

		return active;
	}

	/**
	 * Attract the viewer towards cluster
	 * @param viewer Viewer to attract
	 */
	void attractViewer(WMV_Viewer viewer)			
	{
		if(state.isAttractor)												// Attractor clusters do not need to be active, but attract while empty
		{
			if(!state.empty)
			{
				PVector force = getAttractionForce();
//				System.out.println("Cluster #"+getID()+"... Adding attraction force:"+force);
				viewer.attract( force );		// Add attraction force to camera 
			}
			else 
				System.out.println("Empty Attractor: "+getID());
		}
	}

	/**
	 * Get distance from cluster center to viewer
	 * @return Cluster distance
	 */
	float getClusterDistance()       // Find distance from camera to point in virtual space where photo appears           
	{
		if(viewerState != null)
		{
			return PVector.dist(state.location, viewerState.getLocation());
		}
		else
		{
			System.out.println("cluster id:"+getID()+" ... getClusterDistance().. viewerState == NULL!!");
			return 0.f;
		}
	}

	/**
	 * @return Attraction force to be applied on the viewer
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
				float factor = 0.5f - utilities.mapValue(diff, 0.f, viewerState.getClusterNearDistance(), 0.f, 0.5f);
				strength = (state.clusterGravity * mass * viewerSettings.cameraMass) / (distance * distance) * factor;
			}

			force.mult(strength);
		
//		if(p.p.drawForceVector)
//			p.p.p.display.map2D.drawForceVector(force);
		}
		return force; 								// Return force to be applied
	}

	/**
	 * @param anyDate Whether to use date-independent timeline (true) or last date (false)
	 * @return ID of first time segment in cluster
	 */
	public int getFirstTimeSegmentFieldTimelineID(boolean anyDate)
	{
		if(anyDate)
			return state.timeline.timeline.get(0).getFieldTimelineID();
		else 
			return getFirstTimeSegmentForDate(state.dateline.get(0)).getFieldTimelineID();
	}

	/**
	 * @param anyDate Whether to use date-independent timeline (true) or last date (false)
	 * @return ID of last time segment in cluster
	 */
	public int getLastTimeSegmentFieldTimelineID(boolean anyDate)
	{
		System.out.println("getLastTimeSegment()... result:"+state.timeline.timeline.get(state.timeline.timeline.size()-1).getFieldTimelineID()+" idx:"+(state.timeline.timeline.size()-1));
		if(anyDate)
			return state.timeline.timeline.get(state.timeline.timeline.size()-1).getFieldTimelineID();
		else 
			return getLastTimeSegmentForDate(state.dateline.get(state.dateline.size()-1)).getFieldTimelineID();
	}

	/**
	 * @param anyDate Whether to use date-independent timeline (true) or last date (false)
	 * @return ID of first time segment in cluster
	 */
	public int getFirstTimeSegmentClusterTimelineID(boolean anyDate)
	{
		if(anyDate)
			return state.timeline.timeline.get(0).getClusterTimelineID();
		else 
			return getFirstTimeSegmentForDate(state.dateline.get(0)).getClusterTimelineID();
	}

	/**
	 * @param anyDate Whether to use date-independent timeline (true) or last date (false)
	 * @return ID of last time segment in cluster
	 */
	public int getLastTimeSegmentClusterTimelineID(boolean anyDate)
	{
		if(anyDate)
			return state.timeline.timeline.get(state.timeline.timeline.size()-1).getClusterTimelineID();
		else 
			return getLastTimeSegmentForDate(state.dateline.get(state.dateline.size()-1)).getClusterTimelineID();
	}
	
	/**
	 * @param cluster Cluster to merge with
	 * Merge this cluster with given cluster. Empty and make the given cluster non-active.
	 */
	void absorbCluster(WMV_Cluster cluster, ArrayList<WMV_Image> imageList, ArrayList<WMV_Panorama> panoramaList, ArrayList<WMV_Video> videoList, ArrayList<WMV_Sound> soundList)
	{
		if(debugSettings.cluster && debugSettings.detailed)
			System.out.println("Merging cluster "+getID()+" with "+cluster.getID());

		/* Find images associated with cluster */
		for (int i = 0; i < imageList.size(); i++) 
		{
			WMV_Image curImg = imageList.get(i);

			if (curImg.getMediaState().getClusterID() == cluster.getID()) 				// If the image is assigned to this cluster
			{
				curImg.setAssociatedClusterID( state.id );
				addImage(curImg);
			}
		}

		/* Find panoramas associated with cluster */
		for (int i = 0; i < panoramaList.size(); i++) 
		{
			WMV_Panorama curPano = panoramaList.get(i);

			if (curPano.getMediaState().getClusterID() == cluster.getID()) 				// If the panorama is assigned to this cluster
			{
				curPano.setAssociatedClusterID( state.id );
				addPanorama(curPano);
			}
		}

		/* Find videos associated with cluster */
		for (int i = 0; i < videoList.size(); i++) 
		{
			WMV_Video curVid = videoList.get(i);

			if (curVid.getMediaState().getClusterID() == cluster.getID()) 				// If the video is assigned to this cluster
			{
				curVid.setAssociatedClusterID( state.id );
				addVideo(curVid);
			}
		}

		/* Find videos associated with cluster */
		for (int i = 0; i < soundList.size(); i++) 
		{
			WMV_Sound curSnd = soundList.get(i);

			if (curSnd.getMediaState().getClusterID() == cluster.getID()) 				// If the sound is assigned to this cluster
			{
				curSnd.setAssociatedClusterID( state.id );
				addSound(curSnd);
			}
		}

		cluster.empty();																/* Empty merged cluster */
	}

	public void setTimePoint(float newTimePoint)
	{
		state.currentTime = (int) utilities.mapValue(newTimePoint, 0.f, 1.f, 0, state.timeCycleLength);
	}
	
	/** 
	 * Update cluster time loop
	 */
	public void updateTime()
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

				if(state.dateline != null)
				{
					if(state.currentDate > state.dateline.size())
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
	
	/**
	 * Get the first time segment on given date
	 * @param date Given date
	 * @return First time segment
	 */
	public WMV_TimeSegment getFirstTimeSegmentForDate(WMV_Date date)
	{
		boolean found = false;
		int timelineID = 0;
		
		if(state.dateline != null)				// -- Try just using date ID!!
		{
			for(WMV_Date d : state.dateline)		// Look through cluster dates for date
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
				if(state.dateline.size()>1)
				{
					try{
						result = state.timelines.get(timelineID).timeline.get(0);
					}
					catch(NullPointerException e)
					{
						System.out.println("No timeline found! ... dateline.size():"+state.dateline.size()+" timelineID:"+timelineID+" ... cluster id:"+getID());
						result = null;
					}
				}
				else
				{
					try{
						result = state.timeline.timeline.get(0);
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
	
	/**
	 * Get last time segment on given date
	 * @param date Given date
	 * @return Last time segment on date
	 */
	public WMV_TimeSegment getLastTimeSegmentForDate(WMV_Date date)
	{
		boolean found = false;
		int timelineID = 0;
//		float date = p.getCurrentField().dateline.get(dateSegment).getCenter();
		
		if(state.dateline != null)
		{
			for(int index=state.dateline.size()-1; index >= 0; index--)					// Look through cluster dates for date
			{
				WMV_Date d = state.dateline.get(index);
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
				if(state.dateline.size()>1)
					return state.timelines.get(timelineID).timeline.get(state.timelines.get(timelineID).timeline.size()-1);
				else
					return state.timeline.timeline.get(state.timeline.timeline.size()-1);
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
	void calculateTimes(ArrayList<WMV_Image> imageList, ArrayList<WMV_Panorama> panoramaList, ArrayList<WMV_Video> videoList, ArrayList<WMV_Sound> soundList)
	{
//		float longestDayLength = (float) -1.;			// Length of the longest day
		boolean initImageTime = true, initPanoramaTime = true, initVideoTime = true, initSoundTime = true, 
				initImageDate = true, initPanoramaDate = true, initVideoDate = true, initSoundDate = true;	

//		if(debugSettings.cluster && (images.size() != 0 || panoramas.size() != 0 || videos.size() != 0))
//			System.out.println("Analyzing media times in cluster "+id+" ..." + " associatedImages.size():"+images.size()+" associatedPanoramas:"+panoramas.size()+" associatedVideos:"+videos.size());
			
		ArrayList<WMV_Image> cImages = new ArrayList<WMV_Image>();
		ArrayList<WMV_Panorama> cPanoramas = new ArrayList<WMV_Panorama>();
		ArrayList<WMV_Video> cVideos = new ArrayList<WMV_Video>();
		ArrayList<WMV_Sound> cSounds = new ArrayList<WMV_Sound>();
		
		for(int i : state.images)
			cImages.add(imageList.get(i));
		for(int n : state.panoramas)
			cPanoramas.add(panoramaList.get(n));
		for(int v : state.videos)
			cVideos.add(videoList.get(v));
		for(int s : state.sounds)
			cSounds.add(soundList.get(s));

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
				state.highImageDate = i.time.asDate().getDaysSince1980();
				state.lowImageDate = i.time.asDate().getDaysSince1980();
				initImageDate = false;
			}

			if (i.time.getTime() > state.highImageTime)
				state.highImageTime = i.time.getTime();
			if (i.time.getTime() < state.lowImageTime)
				state.lowImageTime = i.time.getTime();

			if (i.time.asDate().getDaysSince1980() > state.highImageDate)
				state.highImageDate = i.time.asDate().getDaysSince1980();
			if (i.time.asDate().getDaysSince1980() < state.lowImageDate)
				state.lowImageDate = i.time.asDate().getDaysSince1980();

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
				state.highPanoramaDate = n.time.asDate().getDaysSince1980();
				state.lowPanoramaDate = n.time.asDate().getDaysSince1980();
				initPanoramaDate = false;
			}

			if (n.time.getTime() > state.highPanoramaTime)
				state.highPanoramaTime = n.time.getTime();
			if (n.time.getTime() < state.lowPanoramaTime)
				state.lowPanoramaTime = n.time.getTime();

			if (n.time.asDate().getDaysSince1980() > state.highPanoramaDate)
				state.highPanoramaDate = n.time.asDate().getDaysSince1980();
			if (n.time.asDate().getDaysSince1980() < state.lowPanoramaDate)
				state.lowPanoramaDate = n.time.asDate().getDaysSince1980();

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
				state.highVideoDate = v.time.asDate().getDaysSince1980();
				state.lowVideoDate = v.time.asDate().getDaysSince1980();
				initImageDate = false;
			}

			if (v.time.getTime() > state.highVideoTime)
				state.highVideoTime = v.time.getTime();
			if (v.time.getTime() < state.lowVideoTime)
				state.lowVideoTime = v.time.getTime();

			if (v.time.asDate().getDaysSince1980() > state.highVideoDate)
				state.highVideoDate = v.time.asDate().getDaysSince1980();
			if (v.time.asDate().getDaysSince1980() < state.lowVideoDate)
				state.lowVideoDate = v.time.asDate().getDaysSince1980();

//			if (fDayLength > longestVideoDayLength)		// Calculate longest video day length
//				longestVideoDayLength = fDayLength;
		}

		for (WMV_Sound s : cSounds) 			// Iterate over cluster videos to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		{
//			float fDayLength = v.time.getDayLength();

			if (initSoundTime) 		// Calculate most recent and oldest video time
			{		
				state.highSoundTime = s.time.getTime();
				state.lowSoundTime = s.time.getTime();
				initSoundTime = false;
			}

			if (initSoundDate) 		// Calculate most recent and oldest sound date
			{		
				state.highSoundDate = s.time.asDate().getDaysSince1980();
				state.lowSoundDate = s.time.asDate().getDaysSince1980();
				initImageDate = false;
			}

			if (s.time.getTime() > state.highSoundTime)
				state.highSoundTime = s.time.getTime();
			if (s.time.getTime() < state.lowSoundTime)
				state.lowSoundTime = s.time.getTime();

			if (s.time.asDate().getDaysSince1980() > state.highSoundDate)
				state.highSoundDate = s.time.asDate().getDaysSince1980();
			if (s.time.asDate().getDaysSince1980() < state.lowSoundDate)
				state.lowSoundDate = s.time.asDate().getDaysSince1980();

//			if (fDayLength > longestSoundDayLength)		// Calculate longest sound day length
//				longestSoundDayLength = fDayLength;
		}

		state.lowTime = state.lowImageTime;
		if (state.lowPanoramaTime < state.lowTime)
			state.lowTime = state.lowPanoramaTime;
		if (state.lowVideoTime < state.lowTime)
			state.lowTime = state.lowVideoTime;
		if (state.lowSoundTime < state.lowTime)
			state.lowTime = state.lowSoundTime;

		state.highTime = state.highImageTime;
		if (state.highPanoramaTime > state.highTime)
			state.highTime = state.highPanoramaTime;
		if (state.highVideoTime > state.highTime)
			state.highTime = state.highVideoTime;
		if (state.highSoundTime > state.highTime)
			state.highTime = state.highSoundTime;

		state.lowDate = state.lowImageDate;
		if (state.lowPanoramaDate < state.lowDate)
			state.lowDate = state.lowPanoramaDate;
		if (state.lowVideoDate < state.lowDate)
			state.lowDate = state.lowVideoDate;
		if (state.lowSoundDate < state.lowDate)
			state.lowDate = state.lowSoundDate;

		state.highDate = state.highImageDate;
		if (state.highPanoramaDate > state.highDate)
			state.highDate = state.highPanoramaDate;
		if (state.highVideoDate > state.highDate)
			state.highDate = state.highVideoDate;
		if (state.highSoundDate > state.highDate)
			state.highDate = state.highSoundDate;
	}

	/**
	 * Calculate high and low longitude, latitude and altitude for cluster
	 */
	void calculateDimensions( ArrayList<WMV_Image> imageList, ArrayList<WMV_Panorama> panoramaList, ArrayList<WMV_Video> videoList,
							  ArrayList<WMV_Sound> soundList )
	{
		boolean init = true;	

		for (int img : state.images) 				
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

		for (int pano : state.panoramas) 				
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
		
		for (int snd : state.sounds) 		
		{
			if(snd < soundList.size())
			{
				WMV_Sound s = soundList.get(snd);
				PVector gpsLocation = s.getGPSLocation();

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
			else
			{
				System.out.println("Cluster.calculateDimensions() error... snd >= soundList.size()");
			}
		}
	}
	
	/**
	 * Verify cluster has no duplicates
	 * @return Whether valid or not
	 */
	public boolean verify()
	{
		boolean valid = true;
		if(utilities.hasDuplicateInteger(state.images))
			valid = false;
		if(utilities.hasDuplicateInteger(state.panoramas))
			valid = false;
		if(utilities.hasDuplicateInteger(state.videos))
			valid = false;
		if(utilities.hasDuplicateInteger(state.sounds))
			valid = false;
		return valid;
	}
	
	/**
	 * Set cluster state
	 * @param newClusterState New cluster state
	 */
	public void setState( WMV_ClusterState newClusterState )
	{
		state = newClusterState;
	}

	/**
	 * @return Current cluster state
	 */
	public WMV_ClusterState getState()
	{
		return state;
	}

	/**
	 * @return Cluster location as a waypoint for navigation
	 */
	public WMV_Waypoint getClusterAsWaypoint()
	{
		WMV_Waypoint result = new WMV_Waypoint(getID(), getLocation(), null, false);		// -- Must calculate time instead of null!!
		return result;
	}
	
	/**
	 * @return Number of media in cluster
	 */
	public int getMediaCount()
	{
		int count = state.images.size() + state.videos.size() + state.panoramas.size() + state.sounds.size();
		return count;
	}
	
	/**
	 * Set cluster media count
	 * @param newMediaCount New media count
	 */
	public void setMediaCount(int newMediaCount)
	{
		state.mediaCount = newMediaCount;
	}

	/**
	 * @return List of IDs of images in cluster
	 */
	public List<Integer> getImageIDs()
	{
		return state.images;
	}

	/**
	 * @return List of IDs of panoramas in cluster
	 */
	public List<Integer> getPanoramaIDs()
	{
		return state.panoramas;
	}

	/**
	 * @return List of IDs of videos in cluster
	 */
	public List<Integer> getVideoIDs()
	{
		return state.videos;
	}

	/**
	 * @return List of IDs of sounds in cluster
	 */
	public List<Integer> getSoundIDs()
	{
		return state.sounds;
	}
	
	public void setTimeFading(boolean newTimeFading)
	{
		state.timeFading = newTimeFading;
	}
	
	public boolean isTimeFading()
	{
		return state.timeFading;
	}
	
	/**
	 * @return Date-independent timeline
	 */
	public WMV_Timeline getTimeline()
	{
		return state.timeline;
	}

	/**
	 * @return List of timelines, corresponding to dates in dateline
	 */
	public ArrayList<WMV_Timeline> getTimelines()
	{
		return state.timelines;
	}

	/**
	 * Update cluster media settings
	 * @param imageList Image list
	 * @param panoramaList Panorama list
	 * @param videoList Video list
	 * @param soundList Sound list
	 * @param newWorldSettings Current world settings
	 * @param newWorldState Current world state
	 * @param newViewerSettings Current viewer settings
	 * @param newViewerState Current viewer state
	 */
	public void updateAllMediaSettings(ArrayList<WMV_Image> imageList, ArrayList<WMV_Panorama> panoramaList, ArrayList<WMV_Video> videoList, 
			ArrayList<WMV_Sound> soundList, WMV_WorldSettings newWorldSettings, WMV_WorldState newWorldState, 
			WMV_ViewerSettings newViewerSettings, WMV_ViewerState newViewerState )
	{
		for (int i:state.images)  			// Update images
		{
			if(i < imageList.size())
			{
				if(!imageList.get(i).isDisabled())
					imageList.get(i).updateWorldState(worldSettings, worldState, viewerSettings, viewerState);
			}
			else
			{
				System.out.println("Cluster #"+getID()+" has image id:"+i+" over imageList.size():"+imageList.size());
			}
		}
	
		for (int n:state.panoramas)  		// Update panoramas
		{
			if(n < panoramaList.size())
			{
				if(!panoramaList.get(n).isDisabled())
					panoramaList.get(n).updateWorldState(worldSettings, worldState, viewerSettings, viewerState);
			}
			else
			{
				System.out.println("Cluster #"+getID()+" has panorama id:"+n+" over panoramaList.size():"+panoramaList.size());
			}
		}

		for (int v : state.videos)  		// Update videos
		{
			if(v < videoList.size())
			{
				if(!videoList.get(v).isDisabled())
					videoList.get(v).updateWorldState(worldSettings, worldState, viewerSettings, viewerState);
			}
			else
			{
				System.out.println("Cluster #"+getID()+" has video id:"+v+" over videoList.size():"+videoList.size());
			}
		}

		for (int s : state.sounds)  		// Update sounds
		{
			if(s < soundList.size())
			{
				if(!soundList.get(s).isDisabled())
					soundList.get(s).updateWorldState(worldSettings, worldState, viewerSettings, viewerState);
			}
			else
			{
				System.out.println("Cluster #"+getID()+" has sound id:"+s+" over soundList.size():"+soundList.size());
			}
		}
	}

	/**
	 * @return Dateline
	 */
	public ArrayList<WMV_Date> getDateline()
	{
		return state.dateline;
	}
	
	/**
	 * Set whether cluster is active
	 * @param newActive New active state
	 */
	public void setActive(boolean newActive)
	{
		state.active = newActive;
	}

	/**
	 * @return Whether cluster is active
	 */
	public boolean isActive()
	{
		return state.active;
	}

	/**
	 * Set whether cluster is empty
	 * @param newActive New empty state
	 */
	public void setEmpty(boolean newEmpty)
	{
		state.empty = newEmpty;
	}
	
	/**
	 * @return Whether cluster is empty
	 */
	public boolean isEmpty()
	{
		return state.empty;
	}

	/**
	 * Set this cluster as an attractor
	 * @param New attractor state
	 */
	public void setAttractor(boolean newState)
	{
		state.isAttractor = newState;
	}
	
	/**
	 * @return Whether cluster is an attractor
	 */
	public boolean isAttractor()
	{
		return state.isAttractor;
	}

	/**
	 * Set whether cluster has a single media item
	 * @param newState New single state
	 */
	public void setSingle(boolean newState)
	{
		state.single = newState;
	}
	
	/**
	 * @return Whether cluster has a single media item
	 */
	public boolean isSingle()
	{
		return state.single;
	}

	/**
	 * Set length of cluster time cycle
	 * @param newTimeCycleLength New time cycle length
	 */
	public void setTimeCycleLength(int newTimeCycleLength)
	{
		state.timeCycleLength = newTimeCycleLength;
	}

	/**
	 * @return Length of cluster time cycle
	 */
	public int getTimeCycleLength()
	{
		return state.timeCycleLength;
	}
	
	/**
	 * Set cluster mass
	 * @param newMass New cluster mass
	 */
	public void setMass(float newMass)
	{
		state.clusterMass = newMass;
	}
	
	/**
	 * @return Cluster mass
	 */
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
	
	public void setHasImage(boolean newState)
	{
		state.setHasImage(newState);
	}
	
	public boolean hasImage()
	{
		return state.hasImage();
	}
	
	public void setHasPanorama(boolean newState)
	{
		state.setHasPanorama(newState);
	}
	
	public boolean hasPanorama()
	{
		return state.hasPanorama();
	}
	
	public void setHasVideo(boolean newState)
	{
		state.setHasVideo(newState);
	}
	
	public boolean hasVideo()
	{
		return state.hasVideo();
	}
	
	public void setHasSound(boolean newState)
	{
		state.setHasSound(newState);
	}

	public boolean hasSound()
	{
		return state.hasSound();
	}
	
	/**
	 * Print cluster data
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
}