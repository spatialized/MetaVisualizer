package wmViewer;

import java.util.ArrayList;
import processing.core.PApplet;
import processing.data.IntList;
//import processing.core.PVector;

/**************************************************
 * @author davidgordon
 * Class representing media in a large geographical area
 */

public class WMV_Field 
{
	/* General */
	public int fieldID;

	/* File System */
	String name;

	/* Graphics */
	public final int maxVisiblePhotos = 50;					// Maximum visible images at one time
	public final int maxVisiblePanoramas = 2;				// Maximum visible panoramas at one time
	public final int maxVisibleVideos = 4;					// Maximum visible videos at one time
	public int imagesVisible = 0, imagesSeen = 0;			// Number of visible photos and currently seen
	public int panoramasVisible = 0, panoramasSeen = 0;		// Number of visible panoramas and currently seen
	public int videosVisible = 0, videosLoaded = 0, videosPlaying = 0, videosSeen = 0;
	
	/* Visibility */
	float visibleAngleMax = (float) 3.14, visibleAngleMin = (float) 0.05, visibleAngleInc = (float) 0.04;
	int alphaTransitionLength = 15, teleportLength = 60;
	public boolean hideImages = false;						// Hide images
	public boolean hidePanoramas = false;					// Hide panoramas
	public boolean hideVideos = false;						// Hide videos

	/* Data */
	WMV_Model model;										// Dimensions and properties of current virtual space

	public ArrayList<WMV_Image> images; 					// All images in this field
	public ArrayList<WMV_Panorama> panoramas; 				// All panoramas in this field
	public ArrayList<WMV_Video> videos; 					// All videos in this field
	public ArrayList<WMV_Cluster> clusters;					// Spatial groupings of media in the Image3D and Video3D arrays

	private int imageErrors = 0, videoErrors = 0, panoramaErrors = 0;			// Metadata loading errors per media type

	/* Time */
//	public final int numBins = 100; 							// Time precision
	ArrayList<WMV_TimeSegment> timeline;						// List of date-independent time segments in this field
	ArrayList<WMV_Date> dateline;								// List of dates in this field
	ArrayList<ArrayList<WMV_TimeSegment>> timelines;			// Lists of time segments in field by date

	WMV_World p;
	
	/* -- Debug -- */	
	public int disassociatedImages = 0;						// Images not associated with a cluster -- Still needed?
	public int disassociatedPanoramas = 0;
	public int disassociatedVideos = 0;

	WMV_Field(WMV_World parent, String newMediaFolder, int newFieldID)
	{
		p = parent;
		name = newMediaFolder;
		fieldID = newFieldID;

		model = new WMV_Model(this);
		clusters = new ArrayList<WMV_Cluster>();
		
		images = new ArrayList<WMV_Image>();
		panoramas = new ArrayList<WMV_Panorama>();
		videos = new ArrayList<WMV_Video>();		

		timeline = new ArrayList<WMV_TimeSegment>();
		dateline = new ArrayList<WMV_Date>();
	}

	public void draw() 				// Draw currently visible media
	{			
		float vanishingPoint = p.viewer.getFarViewingDistance() + p.defaultFocusDistance;	// Distance where transparency reaches zero
		
		imagesVisible = 0;
		imagesSeen = 0;
		panoramasVisible = 0;
		videosVisible = 0;
		videosSeen = 0;

		p.p.hint(PApplet.ENABLE_DEPTH_TEST);					// Enable depth testing for drawing 3D graphics
		p.p.background(0.f);								// Set background

		for (int i = 0; i < images.size(); i++) 		// Update and display images
		{
			WMV_Image m = images.get(i);
			if(!m.disabled)
			{
				float distance = m.getViewingDistance(); // Estimate image distance to camera based on capture location
				
				if(!m.verticesAreNull() && (m.isFading() || m.fadingFocusDistance))
					m.update();  	// Update geometry + visibility

				if (distance < vanishingPoint && distance > p.viewer.getNearClippingDistance() && !m.verticesAreNull()) 	// Visible	
				{
					if(!m.fadingFocusDistance && !m.isFading()) 
						m.update();  	// Update geometry + visibility
					
					m.draw(); 		// Draw image
					imagesVisible++;
				}
			}
		}

		for (int i = 0; i < panoramas.size(); i++)  	// Update and display panoramas
		{
			WMV_Panorama n = panoramas.get(i);
			if(!n.disabled)
			{
				float distance = n.getViewingDistance(); // Estimate image distance to camera based on capture location

				if(distance < vanishingPoint)			// Check if panorama is in visible range
				{
					n.update();  	// Update geometry + visibility
					n.draw(); 		// Display panorama
					panoramasVisible++;
				}
				else if(n.isFading())
				{
					n.update();  	// Update geometry + visibility
				}
			}
		}

		for (int i = 0; i < videos.size(); i++)  		// Update and display videos
		{
			WMV_Video v = videos.get(i);
			if(!v.disabled)
			{
				float distance = v.getViewingDistance();	 // Estimate video distance to camera based on capture location
				boolean nowVisible = (distance < vanishingPoint);

				if ( v.isVisible() && !nowVisible )
				{
					v.fadeOut();
				}
				
				if (nowVisible || v.isFading())
				{
					v.update();  	// Update geometry + visibility
					v.draw(); 		// Display video
					videosVisible++;
				}
				else
				{
					if(v.isFading() || v.isFadingVolume())
						v.update();  	// Update geometry + visibility
					
					if(v.isVisible())
						v.fadeOut();
				}
			}
		}
		
		if(p.p.debug.model || p.viewer.map3DMode)
		{
			if(clusters.size()>0)
				showClusterCenters();									// Display field cluster centers (media capture locations) 	
		}
		
		if(p.showUserPanoramas || p.showStitchedPanoramas)
		{
			if(clusters.size()>0)
				clusters.get(p.viewer.getCurrentCluster()).draw();		// Draw current cluster
		}
	}
	
	/**
	 * Initialize field with given library folder
	 * @param library Current library folder
	 */
	public void initialize(String library)
	{
		model.calculateFieldSize(); 		// Calculate bounds of photo GPS locations
		model.analyzeMedia();				// Analyze media locations and times 
		model.setup(); 						// Initialize field for first time 

		calculateMediaLocations(); 			// Set location of each photo in simulation
		findImagePlaceHolders();			// Find image place holders for videos
		calculateMediaVertices();			// Calculate all image vertices

		if(p.p.debug.main)
			p.display.message("Will run initial clustering for field #"+fieldID+"...");

		model.runInitialClustering();		// Find media clusters

		if(p.lockMediaToClusters)				// Center media capture locations at associated cluster locations
			model.lockMediaToClusters();	

		if(p.p.debug.main)
			p.display.message("Creating timeline and dateline for field #"+fieldID+"...");

		createTimeline();								// Create date-independent timeline for field
		createDateline();								// Create field dateline
		createDateTimelines();							// Create date-specific timelines for field
		model.analyzeClusterMediaDirections();			// Analyze angles of all images and videos in each cluster for Thinning Visibility Mode
		
		if(p.p.debug.main)
			p.display.message("Finished initializing field #"+fieldID+"..."+name);
	}
	
	/**
	 * Update field variables each frame
	 */
	public void update()
	{
		attractViewer();					// Attract the viewer
	}

	/**
	 * Find image place holders for each video in field
	 */
	void findImagePlaceHolders()
	{
		for(WMV_Video v : videos)
			v.findPlaceholder();
	}

	/**
	 * Calculate location of each media file in virtual space from GPS, orientation metadata
	 */
	public void calculateMediaLocations() 
	{
		if(p.p.debug.field) PApplet.println("Calculating image locations...");

		for (int i = 0; i < images.size(); i++)
			images.get(i).calculateCaptureLocation();
		for (int i = 0; i < panoramas.size(); i++)
			panoramas.get(i).calculateCaptureLocation();
		for (int i = 0; i < videos.size(); i++)
			videos.get(i).calculateCaptureLocation();
	}
	
	public void createClusters()
	{
		for(WMV_Cluster c : clusters)
			c.create();
	}

	/**
	 * Merge and initialize clusters in field
	 */
	void initializeClusters()
	{
		for( WMV_Cluster c : clusters )
		{
			if(c.mediaPoints <= 0)
			{
				c.empty();
				
				if(p.p.debug.cluster)
					PApplet.println("Fixed empty cluster #"+c.getID()+"!!!");
			}
		}
		
		if(p.mergeClusters) model.mergeAdjacentClusters();		// Merge clusters

		initializeClusterMedia();							
		verifyField();				// Verify field parameters
	}
	
	/**
	 * initializeClusterMedia()
	 * Analyze media and initialize cluster variables for each media item 
	 */
	void initializeClusterMedia()
	{
		if(p.p.debug.cluster)
			PApplet.println("initializeClusterMedia() for "+clusters.size()+" clusters...");
		
		for( WMV_Cluster c : clusters )
		{
			if(!c.isEmpty())
			{
				c.analyzeMedia();					// Analyze media in each cluster 
			}
		}
		
		for(WMV_Cluster c : clusters)
		{
			if(!c.isEmpty())
			{
				for(WMV_Image i : c.getImages())
				{
					i.setClusterTime();
					i.setClusterDate();
				}

				for(WMV_Panorama n : c.getPanoramas())
				{
					n.setClusterTime();
					n.setClusterDate();
				}

				for(WMV_Video v : c.getVideos())
				{
					v.setClusterTime();
					v.setClusterDate();
				}

				c.findMediaSegments();
			}
		}
	}

	/**
	 * calculateMediaVertices()
	 * Calculate vertices for all images and videos in the field
	 */
	public void calculateMediaVertices() 
	{
		if(p.p.debug.field) 	PApplet.println("Calculating media vertices...");
		
		for (int i = 0; i < images.size(); i++) 
		{
			images.get(i).calculateVertices();
		}
		
		for (int i = 0; i < videos.size(); i++) 
		{
			videos.get(i).calculateVertices();
		}
	}

	/**
	 * fadeOutMedia()
	 * Fade all media brightness to zero
	 */
	public void fadeOutMedia()
	{
		if(p.p.debug.field) PApplet.println("Fading out media...");

		for (WMV_Image i : images)
			i.fadeOut();
		
		for (WMV_Panorama n : panoramas) 
			n.fadeOut();

		for (WMV_Video v : videos) 
			v.fadeOut();
	}

	/**
	 * blackoutMedia()
	 * Immediately set all media brightness to zero
	 */
	public void blackoutMedia()
	{
		if(p.p.debug.field) PApplet.println("Fading out media...");

		for (WMV_Image i : images)
			i.fadingBrightness = 0;
		
		for (WMV_Panorama n : panoramas) 
			n.fadingBrightness = 0;

		for (WMV_Video v : videos) 
			v.fadingBrightness = 0;
	}

	/**
	 * stopAllFading()
	 * Stop the media in the field from fading
	 */
	public void stopAllFading()
	{
		if(p.p.debug.field) PApplet.println("Stopping all fading...");

		for (WMV_Image i : images)
			i.stopFading();
		
		for (WMV_Video v : videos) 
			v.stopFading();
	}

	/**
	 * stopAllMediaFading()
	 * Stop all media from fading
	 */
	public void stopAllMediaFading()
	{
		for(WMV_Image i : images)
			if(i.isFading())
				i.stopFading();

		for(WMV_Panorama n : panoramas)
			if(n.isFading())
				n.stopFading();

		for(WMV_Video v : videos)
			if(v.isFading())
				v.stopFading();
	}
	
	/**
	 * verifyField()
	 * Check that all field parameters are ready before simulation starts
	 */
	void verifyField() {
		if(p.p.debug.field) PApplet.println("Verifying field...");

		boolean exit = false;

		if (model.fieldWidth <= 0 && clusters.size() > 1)
		{
			if(p.p.debug.model)
			PApplet.println("Field size <= 0! Exiting...");
			exit = true;			
		}

		if (model.fieldAspectRatio <= 0 && clusters.size() > 1)
		{
			exit = true;
			PApplet.println("Field ratio <= 0! Exiting...");
		}

		if (exit) {
			System.out.println("Fatal Error...");
			p.p.exit();
		} 
		else {
			if(p.p.debug.field)
			PApplet.println("Checked Variables... OK");
		}
	}
	
	/**
	 * clearSelectedMedia()
	 * Deselects all media in field
	 */
	public void deselectAllMedia(boolean hide) 
	{
		for (WMV_Image i : images)
		{
			if(i.isSelected())
			{
				i.setSelected(false);
				if(hide) i.hidden = true;
			}
		}
		for (WMV_Panorama n : panoramas)
		{
			if(n.isSelected())
			{
				n.setSelected(false);
//				if(hide) n.hidden = true;
			}
		}
		for (WMV_Video v : videos)
		{
			if(v.isSelected())
			{
				v.setSelected(false);
//				if(hide) v.hidden = true;
			}
		}

		 p.display.clearMetadata();
	}
	
	/**
	 * mediaAreFading()
	 * @return Whether any media in the field are currently fading
	 */
	public boolean mediaAreFading()
	{
		boolean fading = false;
		
		for(WMV_Image i : images)
			if(i.isFading() && !i.disabled)
				fading = true;

		if(!fading)
			for(WMV_Panorama n : panoramas)
				if(n.isFading() && !n.disabled)
					fading = true;

		if(!fading)
			for(WMV_Video v : videos)
				if(v.isFading() && !v.disabled)
					fading = true;

		if(p.p.debug.viewable || p.p.debug.field)
			if(fading)
				p.display.message("Still fading media...");
		
		return fading;
	}
	
	/**
	 * attractViewer()
	 * Attract viewer to each of the attracting clusters
	 */
	public void attractViewer()
	{
		if(p.viewer.isMovingToAttractor())
		{
			p.viewer.attractorPoint.attractViewer();		// Attract the camera to the memory navigation goal
		}
		else if(p.viewer.isMovingToCluster())				// If the camera is moving to a cluster (besides memoryCluster)
		{
			for( WMV_Cluster c : getAttractingClusters() )
				if(c.getClusterDistance() > p.clusterCenterSize)		// If not already at attractor cluster center, attract camera 
					c.attractViewer();
		}
	}
	
	/**
	 * getAttractingClusters()
	 * @return List of attracting clusters
	 */
	public ArrayList<WMV_Cluster> getAttractingClusters()
	{
		ArrayList<WMV_Cluster> cList = new ArrayList<WMV_Cluster>();
		for(WMV_Cluster c : p.getCurrentField().clusters)		// Attract the camera to the attracting cluster(s) 
		{
			if(c.isAttractor())										
			{
				if(c.getClusterDistance() > p.clusterCenterSize)		// If not already at attractor cluster center, attract camera 
					cList.add(c);
			}
		}
		return cList;
	}
	

	/**
	 * mediaAreActive()
	 * @return Are any images or videos currently active?
	 */
	boolean mediaAreActive()
	{
		boolean active = false;
		
		for(WMV_Image i : images)
		{
			if(i.isActive())
				active = true;
		}
		
		for(WMV_Panorama n : panoramas)
		{
			if(n.isActive())
				active = true;
		}
		
		for(WMV_Video v : videos)
		{
			if(v.isActive())
				active = true;
		}
		
		return active;
	}
	
	/**
	 * drawGrid()
	 * @param dist Grid spacing
	 */
	public void drawGrid(float dist) 
	{
		for (float y = 0; y < model.fieldHeight / 2; y += dist) {
			for (float x = 0; x < model.fieldWidth / 2; x += dist) {
				for (float z = 0; z < model.fieldLength / 2; z += dist) {
					p.p.stroke(50, 150, 250);
					p.p.strokeWeight(1);
					p.p.pushMatrix();
					p.p.translate(x, y, z);
					p.p.box(2);
					p.p.popMatrix();
				}
			}
		}
	}

	
	/**
	 * Create date-independent timeline for this field from cluster timelines
	 */
	public void createTimeline()
	{
		timeline = new ArrayList<WMV_TimeSegment>();
		
		if(p.p.debug.time)
			PApplet.println(">>> Creating Field Timeline... <<<");

		for(WMV_Cluster c : clusters)											// Find all media cluster times
			for(WMV_TimeSegment t : c.getTimeline())
				timeline.add(t);

		timeline.sort(WMV_TimeSegment.WMV_TimeLowerBoundComparator);				// Sort time segments 
		
		int count = 0;
		for (WMV_TimeSegment t : timeline) 		
		{
			t.setID(count);
			count++;
		}
	}
	
	/*
	 * Create list of all media capture dates in field
	 */
	public void createDateline()
	{
		for(WMV_Cluster c : clusters)										// Find all media cluster times
		{
			if(!c.isEmpty())
			{
				for(WMV_Date d : c.getDateline())							// Iterate through cluster dateline
				{
					WMV_Date nd = new WMV_Date(d);							// Use copy constructor to create copy
					dateline.add( nd );										// Add segment to field dateline
				}
			}
		}

		dateline.sort(WMV_Date.WMV_DateComparator);				// Sort date segments
		
		int count = 0;
		for (WMV_Date d : dateline) 		
		{
			d.setID(count);
			count++;
		}
	}
	
	/**
	 * Create timeline for each date on dateline, where index of a date in dateline matches index of corresponding timeline in timelines array
	 */
	void createDateTimelines()
	{
		timelines = new ArrayList<ArrayList<WMV_TimeSegment>>();

		for(WMV_Date d : dateline)			// For each date on dateline
		{
			ArrayList<WMV_TimeSegment> dateTimeline = new ArrayList<WMV_TimeSegment>();		// List of times to add to date-specific timeline for field

			for(WMV_Cluster c : clusters)
			{
				ArrayList<WMV_Time> clusterMediaTimes = new ArrayList<WMV_Time>();		// List of times to add to date-specific timeline for field

				if(!c.isEmpty())
				{
					for(WMV_Date cd : c.dateline)									// Search for date within cluster dateline
					{
						if(d.equals(cd))											// Compare dates
						{
							if(c.timelines.size() > d.getID())
							{
								for(WMV_TimeSegment t : c.timelines.get(d.getID()))		// Go through date-specific timeline for cluster
								{
									for(WMV_Time time : t.getTimeline())				// Add all times to list
									{
										clusterMediaTimes.add(time);
									}
								}
							}
						}
					}

					if(clusterMediaTimes.size() > 0)
					{
						ArrayList<WMV_TimeSegment> clusterTimeline = calculateTimeSegments(clusterMediaTimes, c.getID(), p.clusterTimePrecision);
						for(WMV_TimeSegment ts : clusterTimeline)
							dateTimeline.add(ts);
					}
				}
			}

			if(dateTimeline != null) 
			{
				if(dateTimeline.size() > 0)
				{
					dateTimeline.sort(WMV_TimeSegment.WMV_TimeLowerBoundComparator);		// Sort timeline  

					int count = 0;
					for (WMV_TimeSegment t : dateTimeline) 		
					{
						t.setID(count);
						count++;
					}

					timelines.add( dateTimeline );		// Add timeline to list
				}
			}
		}
		
		PApplet.println("Created "+timelines.size()+" date-specific timelines for field #"+fieldID);
	}
	
	/**
	 * Find cluster time segments from given media's capture times
	 * @param times List of times
	 * @param timePrecision Number of histogram bins
	 * @return Time segments
	 */
	ArrayList<WMV_TimeSegment> calculateTimeSegments(ArrayList<WMV_Time> mediaTimes, int clusterID, int timePrecision)				// -- clusterTimelineMinPoints!!								
	{
		mediaTimes.sort(WMV_Time.WMV_SimulationTimeComparator);			// Sort media by simulation time (normalized 0. to 1.)

		if(mediaTimes.size() > 0)
		{
			ArrayList<WMV_TimeSegment> segments = new ArrayList<WMV_TimeSegment>();
			
			int count = 0, curLowerCount = 0;
			WMV_Time curLower, curUpper, last;

			curLower = mediaTimes.get(0);
			curUpper = mediaTimes.get(0);
			last = mediaTimes.get(0);

			for(WMV_Time t : mediaTimes)
			{
				if(t.getTime() != last.getTime())
				{
					if(t.getTime() - last.getTime() < timePrecision)		// If moved by less than precision amount since last time, extend segment 
					{
						curUpper = t;
					}
					else
					{
						WMV_Time center;
						if(curUpper.getTime() == curLower.getTime())
							center = curUpper;								// If upper and lower are same, set center to that value
						else
						{
							int middle = (count-curLowerCount)/2;			// Find center
							if ((count-curLowerCount)%2 == 1) 
							    center = mediaTimes.get(middle);			// Median if even #
							else
							   center = mediaTimes.get(middle-1);			// Use lower of center pair if odd #
						}

						if(p.p.debug.time && p.p.debug.detailed)
							PApplet.println("Cluster #"+clusterID+"... Creating time segment... center:"+(center)+" curUpper:"+(curUpper)+" curLower:"+(curLower));

						ArrayList<WMV_Time> tl = new ArrayList<WMV_Time>();			// Create timeline for segment
						for(int i=curLowerCount; i<=count; i++)
							tl.add(mediaTimes.get(i));
						
						segments.add(new WMV_TimeSegment(-1, clusterID, center, curUpper, curLower, tl));	// Add time segment
						
						curLower = t;
						curUpper = t;
						curLowerCount = count + 1;
					}
				}
				
				count++;
			}
			
			if(curLowerCount == 0)
			{
				WMV_Time center;
				if(curUpper.getTime() == curLower.getTime())
					center = curUpper;								// If upper and lower are same, set center to that value
				else
				{
					int middle = (count-curLowerCount)/2;			// Find center
					if ((count-curLowerCount)%2 == 1) 
					    center = mediaTimes.get(middle);			// Median if even #
					else
					   center = mediaTimes.get(middle-1);			// Use lower of center pair if odd #
				}

				ArrayList<WMV_Time> tl = new ArrayList<WMV_Time>();			// Create timeline for segment
				for(int i=0; i<mediaTimes.size(); i++)
					tl.add(mediaTimes.get(i));
				
				segments.add(new WMV_TimeSegment(-1, clusterID, center, curUpper, curLower, tl));
			}
			
			return segments;			// Return cluster list
		}
		else
		{
			if(p.p.debug.time)
				PApplet.println("cluster:"+clusterID+" getTimeSegments() == null!!");
			return null;		
		}
	}

	/**
	 * @return List of waypoints based on field timeline
	 */
	public ArrayList<WMV_Waypoint> getTimelineAsPath()
	{
		ArrayList<WMV_Waypoint> timelinePath = new ArrayList<WMV_Waypoint>();

		for(WMV_TimeSegment t : timeline)
		{
			WMV_Waypoint w = clusters.get(t.getID()).getClusterAsWaypoint();
			timelinePath.add(w);
		}
		
		if(p.p.debug.field)
			PApplet.println("getTimelineAsPath()... timelinePath.size():"+timelinePath.size());

		return timelinePath;
	}

	public int getFirstTimeSegment()
	{
		if(timeline.size() > 0)
			return timeline.get(0).getID();
		else
			return -1;
	}

	/**
	 * Get ID of time segment <number> in field timeline matching given cluster ID 
	 * @param clusterID Cluster to get time segment from
	 * @param index Segment in cluster timeline to get
	 * @return ID of time segment
	 */
	public WMV_TimeSegment getTimeSegmentInCluster(int clusterID, int index)
	{
		WMV_TimeSegment t = clusters.get(clusterID).getTimeline().get(index);

		if(t == null)
			p.display.message("NULL time segment "+index+" returned by getTimeSegmentInCluster() clusterID:"+clusterID);
		
		if(clusterID != t.getClusterID())
		{
			PApplet.println("ERROR... getTimeSegmentInCluster().. clusterID and timeSegment clusterID do not match!  clusterID:"+clusterID+" t.getClusterID():"+t.getClusterID());
		}
		return t;
	}

	public WMV_Date getDateInCluster(int clusterID, int index)
	{
		WMV_Date d = clusters.get(clusterID).dateline.get(index);

		if(d == null)
			p.display.message("Couldn't get date "+index+" in cluster "+clusterID);
		
		return d;
	}
	
	/**
	 * Try stitching panoramas for all clusters in field
	 */
	public void stitchAllClusters()
	{
		for(WMV_Cluster c : clusters)
			c.stitchImages();
	}
	
	public void showClusterCenters()
	{
		if(p.getCurrentCluster().getID() != -1)
			clusters.get(p.viewer.getCurrentCluster()).drawCenter(255);		// Draw current cluster
		
		if(p.viewer.getAttractorCluster() != -1)
			clusters.get(p.viewer.getAttractorCluster()).drawCenter(50);	// Draw attractor cluster
	}
	
//	public void showUserPanoramas()
//	{
//		if(p.viewer.getCurrentCluster() != -1)
//			clusters.get(p.viewer.getCurrentCluster()).drawUserPanoramas();		// Draw current cluster
//		else if(p.p.debug.cluster || p.p.debug.field)
//			PApplet.println("currentCluster == -1!!!");
//	}

//	public void showStitchedPanoramasX()
//	{
//		if(p.viewer.getCurrentCluster() != -1)
//			clusters.get(p.viewer.getCurrentCluster()).drawStitchedPanoramas();		// Draw current cluster
//	}

	/**
	 * Change all clusters to non-attractors
	 */
	public void clearAllAttractors()
	{
		if(p.p.debug.viewer && p.p.debug.detailed)
			PApplet.println("Clearing all attractors...");
		
		if(p.viewer.getAttractorCluster() != -1)
		{
			p.viewer.clearAttractorCluster();

			for(WMV_Cluster c : clusters)
				if(c.isAttractor())
					c.setAttractor(false);
		}
	}
	
	/**
	 * Fade object distance for each media point in field
	 * @param multiple Multiple to scale object distance by
	 */
	public void fadeObjectDistances(float multiple)
	{
		for(WMV_Image i:images)
		{
			float newFocusDistance = i.getFocusDistance() * multiple;
			i.fadeFocusDistance(newFocusDistance);
		}

		for(WMV_Video v:videos)
		{
			float newFocusDistance = v.getFocusDistance() * multiple;
			v.fadeFocusDistance(newFocusDistance);
		}

//		p.viewer.setFarViewingDistance( p.viewer.getFarViewingDistance() * multiple );		// --Fade value
//		p.viewer.setNearClippingDistance( p.viewer.getNearClippingDistance() * multiple );	// --Fade value
	}

	/**
	 * Reset object distances for each media point in field to original
	 */
	public void resetObjectDistances()
	{
		for(WMV_Image i:images)
		{
			i.resetFocusDistance();
		}

		for(WMV_Video v:videos)
		{
			v.resetFocusDistance();
		}

		for(WMV_Panorama p:panoramas)
		{
			p.resetRadius();
		}
	}
	
	public IntList getSelectedImages()
	{
		IntList selected = new IntList();

		for(WMV_Image i : images)
			if(i.isSelected())
				selected.append(i.getID());
		
		return selected;
	}
	
	public void showImages()
	{
		hideImages = false;
	}
	
	public void hideImages()
	{
		hideImages = true;
		for(WMV_Image i : images)
		{
			if(i.visible)
			{
				if(i.isFading()) i.stopFading();
				i.fadeOut();
			}
		}
	}
	
	public void showPanoramas()
	{
		hidePanoramas = false;
	}
	
	public void hidePanoramas()
	{
		hidePanoramas = true;
		for(WMV_Panorama n : panoramas)
		{
			if(n.visible)
			{
				if(n.isFading()) n.stopFading();
				n.fadeOut();
			}
		}
		
		for(WMV_Cluster c : clusters)
		{
			if(c.stitchedPanoramas.size() > 0)
			{
				for(WMV_Panorama n : c.stitchedPanoramas)
				{
					if(n.isFading()) n.stopFading();
					n.fadeOut();
				}
			}
			
			if(c.userPanoramas.size() > 0)
			{
				for(WMV_Panorama n : c.userPanoramas)
				{
					if(n.isFading()) n.stopFading();
					n.fadeOut();
				}
			}
		}
	}
	
	public void showVideos()
	{
		hideVideos = false;
	}
	
	public void hideVideos()
	{
		hideVideos = true;
		for(WMV_Video v : videos)
		{
			if(v.visible)
			{
				if(v.isFading()) v.stopFading();
				v.fadeOut();
			}
		}
	}
	
	public void addImageError()
	{
		imageErrors++;
	}

	public void addPanoramaError()
	{
		panoramaErrors++;
	}

	public void addVideoError()
	{
		videoErrors++;
	}

	public int getImageErrors()
	{
		return imageErrors;
	}

	public int getPanoramaErrors()
	{
		return panoramaErrors;
	}

	public int getVideoErrors()
	{
		return videoErrors;
	}
	
	public int getImageCount()
	{
		return images.size() - imageErrors;
	}
	
	public int getPanoramaCount()
	{
		return panoramas.size() - panoramaErrors;
	}
	
	public int getVideoCount()
	{
		return videos.size() - videoErrors;
	}

	/**
	 * @param r Cluster to remove
	 * Remove a cluster
	 */
	public void removeCluster(WMV_Cluster r)
	{
		clusters.remove(r);
	}
}
