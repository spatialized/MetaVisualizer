package gmViewer;

import java.util.ArrayList;
import java.util.Arrays;

import processing.core.PApplet;
import processing.data.FloatList;
import processing.data.IntList;
//import processing.core.PVector;

/**************************************************
 * GMV_Field
 * @author davidgordon
 * Class representing media in a large geographical area
 */

public class GMV_Field 
{
	/* General */
	public int fieldID;

	/* File System */
	String name;

	/* Graphics */
	public final int maxVisiblePhotos = 600;				// Maximum visible images at one time
	public final int maxVisiblePanoramas = 3;				// Maximum visible panoramas at one time
	public final int maxVisibleVideos = 12;					// Maximum visible videos at one time
	public int imagesVisible = 0, imagesSeen = 0;			// Number of visible photos and currently seen
	public int panoramasVisible = 0, panoramasSeen = 0;		// Number of visible panoramas and currently seen
	public int videosVisible = 0, videosSeen = 0;
	
	/* Visibility */
	float visibleAngleMax = (float) 3.14, visibleAngleMin = (float) 0.05, visibleAngleInc = (float) 0.04;
	int alphaTransitionLength = 15, teleportLength = 60;

	/* Data */
	GMV_Metadata metadata;									// Image and video metadata reader for this field
	GMV_Model model;										// Dimensions and properties of current virtual space

	public ArrayList<GMV_Image> images; 					// All images in this field
	public ArrayList<GMV_Panorama> panoramas; 				// All panoramas in this field
	public ArrayList<GMV_Video> videos; 					// All videos in this field
	public ArrayList<GMV_Cluster> clusters;					// Spatial groupings of media in the Image3D and Video3D arrays

	private int imageErrors = 0, videoErrors = 0, panoramaErrors = 0;			// Metadata loading errors per media type

	/* Time */
	public final int numBins = 100; 							// Time precision
	public int[] timesHistogram = new int[numBins]; 	// Which times of the day have the most or least photos?

//	public int currentTime = 0;								// Time units since start of time cycle (day / month / year)
//	public int timeUnitLength;					// Length of time unit in frames  (e.g. 10 means every 10 frames)
//	public float timeInc;						// User time increment
	public boolean initFading = true;
	

	/* Clusters */	
	ArrayList<GMV_TimeSegment> timeline;								// Cluster timeline for this field
	public int disassociatedImages = 0;
	public int disassociatedPanoramas = 0;
	public int disassociatedVideos = 0;
	
	/* Interaction */
//	public int selectedImage = -1, selectedPanorama = -1, selectedVideo = -1;

	GeoSynth p;

	GMV_Field(GeoSynth parent, String newMediaFolder, int newFieldID)
	{
		p = parent;
		name = newMediaFolder;
		fieldID = newFieldID;

		metadata = new GMV_Metadata(this);
		model = new GMV_Model(this);
		clusters = new ArrayList<GMV_Cluster>();
		
		images = new ArrayList<GMV_Image>();
		panoramas = new ArrayList<GMV_Panorama>();
		videos = new ArrayList<GMV_Video>();		

		timeline = new ArrayList<GMV_TimeSegment>();

	}

	public void draw() 				// Draw currently visible media
	{			
		float vanishingPoint = p.viewer.getFarViewingDistance() + p.defaultFocusDistance;	// Distance where transparency reaches zero
		
		imagesVisible = 0;
		imagesSeen = 0;
		panoramasVisible = 0;
		videosVisible = 0;
		videosSeen = 0;

		p.hint(PApplet.ENABLE_DEPTH_TEST);					// Enable depth testing for drawing 3D graphics
		p.background(0.f);								// Set background

		for (int i = 0; i < images.size(); i++) 		// Update and display images
		{
			GMV_Image m = images.get(i);
			if(!m.disabled)
			{
				float distance = m.getViewingDistance(); // Estimate image distance to camera based on capture location
				
				if(!m.verticesAreNull() && (m.isFading() || m.fadingObjectDistance))
					m.update();  	// Update geometry + visibility

				if (distance < vanishingPoint && distance > p.viewer.getNearClippingDistance() && !m.verticesAreNull()) 	// Visible	
				{
					if(!m.fadingObjectDistance && !m.isFading()) 
						m.update();  	// Update geometry + visibility
					
					m.draw(); 		// Draw image
					imagesVisible++;
				}
			}
		}

		for (int i = 0; i < panoramas.size(); i++)  	// Update and display panoramas
		{
			GMV_Panorama n = panoramas.get(i);
			if(!n.disabled)
			{
				float distance = n.getViewingDistance(); // Estimate image distance to camera based on capture location

				if(distance < vanishingPoint)	// In visible range?
				{
					n.update();  	// Update geometry + visibility
					n.draw(); 		// Display panorama
					panoramasVisible++;
				}
				else if(n.isFading())
					n.update();  	// Update geometry + visibility
			}
		}

		for (int i = 0; i < videos.size(); i++)  		// Update and display videos
		{
			GMV_Video v = videos.get(i);
			if(!v.disabled)
			{
				float distance = v.getViewingDistance();	 // Estimate video distance to camera based on capture location

				if (distance < vanishingPoint)
				{
					if(p.frameCount % 60 == 0 && p.debug.video)
						p.display.message("Video within view... "+i+" disabled?"+v.disabled);

					v.update();  	// Update geometry + visibility
					v.draw(); 		// Display video
					videosVisible++;
				}
			}
		}

		// filter(THRESHOLD, 0.4);
		// filter(INVERT);
		// filter(DILATE);
	}
	
	/**
	 * Initialize field with given library folder
	 * @param library Current library folder
	 */
	public void initialize(String library)
	{
//		GMV_Field f = fields.get(field);
		String fieldPath = name;
		metadata.load(library, fieldPath);					// Import metadata for all media in field
		
		model.calculateFieldSize(); 		// Calculate bounds of photo GPS locations
		model.analyzeMedia();				// Analyze media locations and times 
		model.setup(); 					// Initialize field for first time 

		calculateMediaLocations(); 		// Set location of each photo in simulation
		findImagePlaceHolders();			// Find image place holders for videos
		calculateMediaVertices();			// Calculate all image vertices

		model.runInitialClustering();		// Find media clusters

		if(p.lockMediaToClusters)				// Center media capture locations at associated cluster locations
			model.lockMediaToClusters();	

		createTimeline();					// Create field timeline
		model.analyzeClusterMediaDirections();			// Analyze angles of all images and videos in each cluster for Thinning Visibility Mode
	}
	
	/**
	 * update()
	 * Update field variables each frame
	 */
	public void update()
	{
		attractViewer();					// Attract the viewer

//		for(GMV_Cluster c : clusters)		// Update all clusters
//			if(c.isActive())
//				c.update();
	}
	
	/**
	 * createTimeline()
	 * Create timeline for this field from cluster timelines
	 */
	public void createTimeline()
	{
		for(GMV_Cluster c : clusters)											// Find all media cluster times
		{
			ArrayList<GMV_TimeSegment> times = new ArrayList<GMV_TimeSegment>();
			int count = 0;
			
			for(float f : c.getClusterTimes())									// Iterate through cluster times
			{
				GMV_TimeSegment time = new GMV_TimeSegment(	c.getID(), f, c.getClusterTimesLowerBounds().get(count), 
														    c.getClusterTimesUpperBounds().get(count));
				times.add( time );							// Add segment to timeline
				
//				PApplet.println("-->time.getID():"+time.getID());
//				PApplet.println("time.getUpper():"+time.getUpper());
//				PApplet.println("time.getCenter():"+time.getCenter());
//				PApplet.println("time.getLower():"+time.getLower());
//				times.get(count).setID(c.getID());
//				times.get(count).setLower(c.getClusterTimesLowerBounds().get(count));
//				times.get(count).setUpper(c.getClusterTimesUpperBounds().get(count));

				count++;
			}

			for(GMV_TimeSegment t : times)										// Add indexed cluster times to timeline
				timeline.add(t);
		}

		timeline.sort(GMV_TimeSegment.GMV_TimeLowerBoundComparator);				// Sort time points 
		
		if(p.debug.time)
		{
			PApplet.println("---> First lower:"+" timeline.get(0).lower():"+timeline.get(0).getLower());
			PApplet.println("---> First center:"+" timeline.get(0).lower():"+timeline.get(0).getCenter());
			PApplet.println("---> First upper:"+" timeline.get(0).lower():"+timeline.get(0).getUpper());
			PApplet.println("---> Last lower:"+" timeline.get(timeline.size()-1).lower():"+timeline.get(timeline.size()-1).getLower());
			PApplet.println("---> Last center:"+" timeline.get(timeline.size()-1).center():"+timeline.get(timeline.size()-1).getCenter());
			PApplet.println("---> Last upper:"+" timeline.get(timeline.size()-1).upper():"+timeline.get(timeline.size()-1).getUpper());
		}
	}
	
	/**
	 * getTimelineAsPath()
	 * @return List of waypoints based on field timeline
	 */
	public ArrayList<GMV_Waypoint> getTimelineAsPath()
	{
		ArrayList<GMV_Waypoint> timelinePath = new ArrayList<GMV_Waypoint>();

		for(GMV_TimeSegment t : timeline)
		{
			GMV_Waypoint w = clusters.get(t.getID()).getClusterAsWaypoint();
			timelinePath.add(w);
		}
		if(p.debug.field)
			PApplet.println("getTimelineAsPath()... timelinePath.size():"+timelinePath.size());
		return timelinePath;
	}

	/**
	 * findImagePlaceHolders()
	 * Find image place holders for each video in field
	 */
	void findImagePlaceHolders()
	{
		for(GMV_Video v : videos)
			v.findPlaceholder();
	}

	/**
	 * Calculate location of each media file in virtual space from GPS, orientation metadata
	 */
	public void calculateMediaLocations() 
	{
		if(p.debug.field) PApplet.println("Calculating image locations...");

		for (int i = 0; i < images.size(); i++)
			images.get(i).calculateCaptureLocation();
		for (int i = 0; i < panoramas.size(); i++)
			panoramas.get(i).calculateCaptureLocation();
		for (int i = 0; i < videos.size(); i++)
			videos.get(i).calculateCaptureLocation();
	}
	
	public void createClusters()
	{
		for(GMV_Cluster c : clusters)
			c.create();
	}

	/**
	 * Merge and initialize clusters in field
	 */
	void initializeClusters()
	{
		for( GMV_Cluster c : clusters )
		{
			if(c.mediaPoints <= 0)
			{
				c.empty();
				
				if(p.debug.cluster)
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
		if(p.debug.cluster)
			PApplet.println("initializeClusterMedia() for "+clusters.size()+" clusters...");
		
		for( GMV_Cluster c : clusters )
		{
			if(!c.isEmpty())
			{
				c.analyzeMedia();					// Analyze media in each cluster 
			}
		}
		
		for(GMV_Cluster c : clusters)
		{
			if(!c.isEmpty())
			{
				for(GMV_Image i : c.getImages())
				{
					i.setClusterTime();
					i.setClusterDate();
				}

				for(GMV_Panorama n : c.getPanoramas())
				{
					n.setClusterTime();
					n.setClusterDate();
				}

				for(GMV_Video v : c.getVideos())
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
		if(p.debug.field) 	PApplet.println("Calculating media vertices...");
		
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
		if(p.debug.field) PApplet.println("Fading out media...");

		for (GMV_Image i : images)
			i.fadeOut();
		
		for (GMV_Panorama n : panoramas) 
			n.fadeOut();

		for (GMV_Video v : videos) 
			v.fadeOut();
	}

	/**
	 * blackoutMedia()
	 * Immediately set all media brightness to zero
	 */
	public void blackoutMedia()
	{
		if(p.debug.field) PApplet.println("Fading out media...");

		for (GMV_Image i : images)
			i.fadingBrightness = 0;
		
		for (GMV_Panorama n : panoramas) 
			n.fadingBrightness = 0;

		for (GMV_Video v : videos) 
			v.fadingBrightness = 0;
	}

	/**
	 * stopAllFading()
	 * Stop the media in the field from fading
	 */
	public void stopAllFading()
	{
		if(p.debug.field) PApplet.println("Stopping all fading...");

		for (GMV_Image i : images)
			i.stopFading();
		
		for (GMV_Video v : videos) 
			v.stopFading();
	}

	/**
	 * stopAllMediaFading()
	 * Stop all media from fading
	 */
	public void stopAllMediaFading()
	{
		for(GMV_Image i : images)
			if(i.isFading())
				i.stopFading();

		for(GMV_Panorama n : panoramas)
			if(n.isFading())
				n.stopFading();

		for(GMV_Video v : videos)
			if(v.isFading())
				v.stopFading();
	}
	
	/**
	 * verifyField()
	 * Check that all field parameters are ready before simulation starts
	 */
	void verifyField() {
		if(p.debug.field) PApplet.println("Verifying field...");

		boolean exit = false;

		if (model.fieldWidth <= 0 && clusters.size() > 1)
		{
			if(p.debug.model)
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
			p.exit();
		} 
		else {
			if(p.debug.field)
			PApplet.println("Checked Variables... OK");
		}
	}
	
	/**
	 * clearSelectedMedia()
	 * Deselects all media in field
	 */
	public void deselectAllMedia() 
	{
		for (int i = 0; i < images.size(); i++)
			images.get(i).setSelected(false);
		for (int i = 0; i < videos.size(); i++)
			videos.get(i).setSelected(false);

		 p.display.clearMetadata();
	}
	
	/**
	 * mediaAreFading()
	 * @return Whether any media in the field are currently fading
	 */
	public boolean mediaAreFading()
	{
		boolean fading = false;
		
		for(GMV_Image i : images)
			if(i.isFading() && !i.disabled)
				fading = true;

		if(!fading)
			for(GMV_Panorama n : panoramas)
				if(n.isFading() && !n.disabled)
					fading = true;

		if(!fading)
			for(GMV_Video v : videos)
				if(v.isFading() && !v.disabled)
					fading = true;

		if(p.debug.viewable || p.debug.field)
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
			for( GMV_Cluster c : getAttractingClusters() )
				if(c.getClusterDistance() > p.clusterCenterSize)		// If not already at attractor cluster center, attract camera 
					c.attractViewer();
		}
	}
	
	/**
	 * getAttractingClusters()
	 * @return List of attracting clusters
	 */
	public ArrayList<GMV_Cluster> getAttractingClusters()
	{
		ArrayList<GMV_Cluster> cList = new ArrayList<GMV_Cluster>();
		for(GMV_Cluster c : p.getCurrentField().clusters)		// Attract the camera to the attracting cluster(s) 
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
		
		for(GMV_Image i : images)
		{
			if(i.isActive())
				active = true;
		}
		
		for(GMV_Panorama n : panoramas)
		{
			if(n.isActive())
				active = true;
		}
		
		for(GMV_Video v : videos)
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
					p.stroke(50, 150, 250);
					p.strokeWeight(1);
					p.pushMatrix();
					p.translate(x, y, z);
					p.box(2);
					p.popMatrix();
				}
			}
		}
	}
	
	/**
	 * Try stitching panoramas for all clusters in field
	 */
	public void stitchAllClusters()
	{
		for(GMV_Cluster c : clusters)
			c.stitchImages();
	}
	
	public void showClusters()
	{
		if(p.viewer.getCurrentCluster() != -1)
			clusters.get(p.viewer.getCurrentCluster()).drawCenter(255);		// Draw current cluster
		else if(p.debug.cluster || p.debug.field)
			PApplet.println("currentCluster == -1!!!");
		
		if(p.viewer.getAttractorCluster() != -1)
			clusters.get(p.viewer.getAttractorCluster()).drawCenter(50);	// Draw attractor cluster
	}
	
	public void showUserPanoramas()
	{
		if(p.viewer.getCurrentCluster() != -1)
			clusters.get(p.viewer.getCurrentCluster()).drawUserPanoramas();		// Draw current cluster
//		else if(p.debug.cluster || p.debug.field)
//			PApplet.println("currentCluster == -1!!!");
	}

	public void showStitchedPanoramas()
	{
		if(p.viewer.getCurrentCluster() != -1)
			clusters.get(p.viewer.getCurrentCluster()).drawStitchedPanoramas();		// Draw current cluster
	}

	/**
	 * Change all clusters to non-attractors
	 */
	public void clearAllAttractors()
	{
		if(p.debug.viewer && p.debug.detailed)
			PApplet.println("Clearing all attractors...");
		
		if(p.viewer.getAttractorCluster() != -1)
		{
			p.viewer.clearAttractorCluster();

			for(GMV_Cluster c : clusters)
				if(c.isAttractor())
					c.setAttractor(false);
		}
	}
	
	/**
	 * Fade object distance for each media point in field, i.e. move closer or further from capture location and rescale
	 * @param multiple Multiple to scale object distance by
	 */
	public void fadeObjectDistances(float multiple)
	{
		for(GMV_Image i:images)
		{
			float newFocusDistance = i.getFocusDistance() * multiple;
			i.fadeObjectDistance(newFocusDistance);
		}

		for(GMV_Video v:videos)
		{
			float newFocusDistance = v.getFocusDistance() * multiple;
			v.fadeObjectDistance(newFocusDistance);
		}

//		p.viewer.setFarViewingDistance( p.viewer.getFarViewingDistance() * multiple );		// --Fade value
//		p.viewer.setNearClippingDistance( p.viewer.getNearClippingDistance() * multiple );	// --Fade value
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
	 * @return
	 */
	public int getTimeSegmentOfCluster(int clusterID, int index)
	{
		IntList times = new IntList();
		
//		if(timeline.size() > clusterID)
//		{
		int count = 0;
		for(GMV_TimeSegment t : timeline)
		{
			if(t.getID() == clusterID)
				times.append(count);

			count++;
		}

		if(times.size() > index)
			return times.get(index);
//		}

		p.display.message("Couldn't get time segment "+index+" of cluster "+clusterID+" times.size() = "+times.size());
		return -1;
	}

//	/**
//	 * @return Time segments in field timeline
//	 */
//	public ArrayList<GMV_TimeSegment> getTimeSegments()
//	{
//		ArrayList<GMV_TimeSegment> result = new ArrayList<GMV_TimeSegment>();
//		for(GMV_TimeSegment t : timeline)
//			result.add(t);
//		
//		return result;
//	}
	
	public IntList getSelectedImages()
	{
		IntList selected = new IntList();

		for(GMV_Image i : images)
			if(i.isSelected())
				selected.append(i.getID());
		
		return selected;
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

//	/**
//	 * removeCluster()
//	 * @param r Cluster to remove
//	 * Remove a cluster
//	 */
//	public void removeCluster(GMV_Cluster r)
//	{
//		clusters.remove(r);
//	}
}
