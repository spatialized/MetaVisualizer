package wmViewer;

import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PVector;
import processing.data.IntList;

/**************************************************
 * @author davidgordon
 * Geographical area of spatially clustered media viewable as a navigable environment, 2D map or timeline
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
	public boolean hideImages = false;						// Hide images
	public boolean hidePanoramas = false;					// Hide panoramas
	public boolean hideVideos = false;						// Hide videos

	/* Data */
	WMV_Model model;										// Dimensions and properties of current virtual space

	public ArrayList<WMV_Image> images; 					// All images in this field
	public ArrayList<WMV_Panorama> panoramas; 				// All panoramas in this field
	public ArrayList<WMV_Video> videos; 					// All videos in this field
	public ArrayList<WMV_Sound> sounds; 					// All videos in this field
	public ArrayList<WMV_Cluster> clusters;					// Spatial groupings of media in the Image3D and Video3D arrays

	private int imageErrors = 0, videoErrors = 0, panoramaErrors = 0;			// Metadata loading errors per media type

	/* Time */
	ArrayList<WMV_TimeSegment> timeline;						// List of time segments in this field ordered by time from 0:00 to 24:00 as a single day
	ArrayList<ArrayList<WMV_TimeSegment>> timelines;			// Lists of time segments in field ordered by date
	ArrayList<WMV_Date> dateline;								// List of dates in this field, whose indices correspond with timelines in timelines list
	String timeZoneID = "America/Los_Angeles";					// Current time zone
	
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
		sounds = new ArrayList<WMV_Sound>();		

		timeline = new ArrayList<WMV_TimeSegment>();
		dateline = new ArrayList<WMV_Date>();
	}

	WMV_Field( WMV_World parent, String newMediaFolder, WMV_Cluster newCluster, int newFieldID )
	{
		p = parent;
		name = newMediaFolder;
		fieldID = newFieldID;

		model = new WMV_Model(this);
		clusters = new ArrayList<WMV_Cluster>();
		
		images = new ArrayList<WMV_Image>();
		panoramas = new ArrayList<WMV_Panorama>();
		videos = new ArrayList<WMV_Video>();		
		sounds = new ArrayList<WMV_Sound>();		

		timeline = new ArrayList<WMV_TimeSegment>();
		dateline = new ArrayList<WMV_Date>();
	}

	public void draw() 				// Draw currently visible media
	{			
		float vanishingPoint = p.viewer.getFarViewingDistance() + p.settings.defaultFocusDistance;	// Distance where transparency reaches zero
		
		imagesVisible = 0;
		imagesSeen = 0;
		panoramasVisible = 0;
		videosVisible = 0;
		videosSeen = 0;

		p.p.hint(PApplet.ENABLE_DEPTH_TEST);					// Enable depth testing for drawing 3D graphics
		p.p.background(0.f);									// Set background

		for (WMV_Image m : images) 		// Update and display images
		{
			if(!m.disabled)
			{
				float distance = m.getViewingDistance(); // Estimate image distance to camera based on capture location
				
				if(!m.verticesAreNull() && (m.isFading() || m.fadingFocusDistance))
					m.update();  		// Update geometry + visibility

				if (distance < vanishingPoint && distance > p.viewer.getNearClippingDistance() && !m.verticesAreNull()) 	// Visible	
				{
					if(!m.fadingFocusDistance && !m.isFading()) 
						m.update();  	// Update geometry + visibility
					
					m.draw(); 		// Draw image
					imagesVisible++;
				}
			}
		}
		
		for (WMV_Panorama n : panoramas)  	// Update and display panoramas
		{
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

		for (WMV_Video v : videos)  		// Update and display videos
		{
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
		
//		for (WMV_Sound s : sounds)  		// Update and display sounds
//		{
//			if(!s.disabled)
//			{
//				float distance = s.getHearingDistance();	 // Estimate video distance to camera based on capture location
//				boolean nowVisible = (distance < vanishingPoint);
//
//				if ( s.isVisible() && !nowVisible )
//				{
////					s.fadeOut();
//				}
//				
//				if (nowVisible || s.isFading())
//				{
////					s.update();  	// Update geometry + visibility
//					s.draw(); 		// Display video
////					soundsAudible++;
//				}
//				else
//				{
////					if(s.isFading() || s.isFadingVolume())
////						s.update();  	// Update geometry + visibility
////					
////					if(v.isVisible())
////						s.fadeOut();
//				}
//			}
//		}

		if(p.p.debug.model || p.viewer.settings.map3DMode)
		{
			if(clusters.size()>0)
				showClusterCenters();									// Display field cluster centers (media capture locations) 	
		}
		
		if(p.settings.showUserPanoramas || p.settings.showStitchedPanoramas)
		{
			if(clusters.size()>0)
				clusters.get(p.viewer.getCurrentClusterID()).draw();		// Draw current cluster
		}
	}
	
	/**
	 * Initialize field with given library folder
	 * @param library Current library folder
	 */
	public void initialize(String library)
	{
		if(p.p.debug.main)
			PApplet.println("Initializing field #"+fieldID);
		
		model.calculateFieldSize(); 		// Calculate bounds of photo GPS locations
		model.analyzeMedia();				// Analyze media locations and times 
		model.setup(); 						// Initialize field for first time 

		calculateMediaLocations(); 			// Set location of each photo in simulation
//		detectMultipleFields();				// Run clustering on capture locations to detect multiple fields

		// TESTING
		p.divideField(this, 3000.f, 15000.f);			
		
		findImagePlaceHolders();			// Find image place holders for videos
		calculateMediaVertices();			// Calculate all image vertices

		if(p.p.debug.main)
			p.display.message("Will run initial clustering for field #"+fieldID+"...");

		model.runInitialClustering();		// Find media clusters
//		model.findDuplicateClusterMedia();	// Find media in more than one cluster
		
		if(p.lockMediaToClusters)				// Center media capture locations at associated cluster locations
			model.lockMediaToClusters();	

		if(p.p.debug.main)
			p.display.message("Creating timeline and dateline for field #"+fieldID+"...");

		if( p.settings.getTimeZonesFromGoogle )		// Get time zone for field from Google Time Zone API
		{
			if(images.size() > 0)					
				timeZoneID = p.p.utilities.getCurrentTimeZoneID(images.get(0).getGPSLocation().z, images.get(0).getGPSLocation().x);
			else if(panoramas.size() > 0)
				timeZoneID = p.p.utilities.getCurrentTimeZoneID(panoramas.get(0).getGPSLocation().z, panoramas.get(0).getGPSLocation().x);
			else if(videos.size() > 0)
				timeZoneID = p.p.utilities.getCurrentTimeZoneID(videos.get(0).getGPSLocation().z, videos.get(0).getGPSLocation().x);
			else if(sounds.size() > 0)
				timeZoneID = p.p.utilities.getCurrentTimeZoneID(sounds.get(0).getGPSLocation().z, sounds.get(0).getGPSLocation().x);
		}

		createTimeline();								// Create date-independent timeline for field
		createDateline();								// Create field dateline
		createTimelines();								// Create date-specific timelines for field
		model.analyzeClusterMediaDirections();			// Analyze angles of all images and videos in each cluster for Thinning Visibility Mode
		
		if(p.p.debug.main)
			p.display.message("Finished initializing field #"+fieldID+"..."+name);

		// TEST
//		calculateMediaLocations(); 			// Set location of each photo in simulation
//		recalculateGeometries();
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
	
	/**
	 * Recalculate vertices for all visual media in field
	 */
	public void recalculateGeometries()		
	{
		for (WMV_Image i : images)
			i.calculateVertices();
		for (WMV_Panorama p : panoramas)
			p.initializeSphere();
		for (WMV_Video v : videos)
			v.calculateVertices();
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
			if(c.mediaCount <= 0)
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
	 * Calculate vertices for all images and videos in the field
	 */
	public void calculateMediaVertices() 
	{
		if(p.p.debug.field) 	PApplet.println("Calculating media vertices...");
		
		for (int i = 0; i < images.size(); i++) 
			images.get(i).calculateVertices();
		
		for (int i = 0; i < videos.size(); i++) 
			videos.get(i).calculateVertices();
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
	 * Deselect all media in field
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
				if(hide) n.hidden = true;
			}
		}
		for (WMV_Video v : videos)
		{
			if(v.isSelected())
			{
				v.setSelected(false);
				if(hide) v.hidden = true;
			}
		}

		 p.display.clearMetadata();
	}
	
	
	/**
	 * Return list of selected media IDs of given type
	 * @param mediaType 0: image 1: panorama 2: video 3: sound
	 */
	public IntList getSelectedMedia(int mediaType) 
	{
		IntList selected = new IntList();
		
		for (WMV_Image i : images)
		{
			if(i.isSelected())
			{
				selected.append(i.getID());
			}
		}
		for (WMV_Panorama n : panoramas)
		{
			if(n.isSelected())
			{
				selected.append(n.getID());
			}
		}
		for (WMV_Video v : videos)
		{
			if(v.isSelected())
			{
				selected.append(v.getID());
			}
		}
//		for (WMV_Sound s : sounds)
//		{
//			if(s.isSelected())
//			{
//				selected.append(s.getID());
//			}
//		}

		return selected;
	}
	
	/**
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
	 * Attract viewer to each of the attracting clusters
	 */
	public void attractViewer()
	{
		if(p.viewer.isMovingToAttractor())
		{
			if(p.viewer.attractorPoint != null)
				p.viewer.attractorPoint.attractViewer();		// Attract the camera to the memory navigation goal
			else 
				PApplet.println("p.viewer.attractorPoint == NULL!!");
		}
		else if(p.viewer.isMovingToCluster())				// If the camera is moving to a cluster (besides memoryCluster)
		{
			for( WMV_Cluster c : getAttractingClusters() )
				if(c.getClusterDistance() > p.settings.clusterCenterSize)		// If not already at attractor cluster center, attract camera 
					c.attractViewer();
		}
	}
	
	/**
	 * @return List of attracting clusters
	 */
	public ArrayList<WMV_Cluster> getAttractingClusters()
	{
		ArrayList<WMV_Cluster> cList = new ArrayList<WMV_Cluster>();
		for(WMV_Cluster c : p.getCurrentField().clusters)		// Attract the camera to the attracting cluster(s) 
		{
			if(c.isAttractor())										
			{
				if(c.getClusterDistance() > p.settings.clusterCenterSize)		// If not already at attractor cluster center, attract camera 
					cList.add(c);
			}
		}
		return cList;
	}
	

	/**
	 * @return Whether any images or videos are currently active
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
	 * Merge together clusters with closest neighbor below minClusterDistance threshold
	 */
	ArrayList<WMV_Cluster> mergeAdjacentClusters(ArrayList<WMV_Cluster> clusterList, float minClusterDistance)
	{
		int mergedClusters = 0;			// Reset mergedClusters count

		IntList[] closeNeighbors = new IntList[ clusterList.size()+1 ];			// List array of closest neighbor distances for each cluster 
		ArrayList<PVector> mostNeighbors = new ArrayList<PVector>();			// List of clusters with most neighbors and number of neighbors as PVector(id, neighborCount)
		IntList absorbed = new IntList();										// List of clusters absorbed into other clusters
		IntList merged = new IntList();											// List of clusters already merged with neighbors
		float firstMergePct = 0.2f;												// Fraction of clusters with most neighbors to merge first
		
		if(p.p.debug.cluster)
			PApplet.println("Merging adjacent clusters... starting number:"+clusterList.size());

		for( WMV_Cluster c : clusterList )					// Find distances of close neighbors to each cluster
		{
//			PApplet.println("--> c.images.size():"+c.images.size()+" id:"+c.getID());
			closeNeighbors[c.getID()] = new IntList();	// Initialize list for this cluster
			for( WMV_Cluster d : clusterList )
			{
				float dist = PVector.dist(c.getLocation(), d.getLocation());			// Get distance between clusters
				//				  PApplet.println("c.location:"+c.location+" d.location:"+d.location+" dist:"+dist+" min:"+minClusterDistance);

				if(dist < minClusterDistance)								// If less than minimum distance
				{
					closeNeighbors[c.getID()].append(d.getID());		// Add d to closest clusters to c
				}
			}
		}

		int count = 0;
		for( WMV_Cluster c : clusterList )					// Find distances of close neighbors for each cluster
		{
			if(count < clusterList.size() * firstMergePct )		// Fill array with initial clusters 
			{
				mostNeighbors.add( new PVector(c.getID(), closeNeighbors[c.getID()].size()) );
			}
			else
			{
				boolean larger = false;
				for(PVector v : mostNeighbors)
				{
					float numCloseNeighbors = closeNeighbors[c.getID()].size();
					if( v.y > numCloseNeighbors ) larger = true;					// 
				}

				if(larger)
				{
					int smallestIdx = -1;							// Index in mostNeighbors array of cluster with smallest distance
					float smallest = 10000.f;						// Smallest distance

					for(int i = 0; i<mostNeighbors.size(); i++)			// Find smallest to remove
					{
						PVector v = mostNeighbors.get(i);

						if(v.y < smallest)
						{
							smallestIdx = i;
							smallest = v.y;
						}
					}
					mostNeighbors.remove( smallestIdx );
					mostNeighbors.add( new PVector(c.getID(), closeNeighbors[c.getID()].size()) );
				}
			}

			count++;
		}		

		for( WMV_Cluster c : clusterList )					// Merge remaining clusters under minClusterDistance 
		{
			if(!merged.hasValue(c.getID()))
			{
				for( WMV_Cluster d : clusterList )
				{
					if( !absorbed.hasValue(d.getID()) && !merged.hasValue(d.getID()) && c.getID() != d.getID() ) 	// If is different cluster and hasn't already been absorbed or merged
					{
						float dist = PVector.dist(c.getLocation(), d.getLocation());			// Get distance between clusters
						if(dist < minClusterDistance)
						{
							c.absorbCluster(d);
							absorbed.append(d.getID());

							merged.append(c.getID());
							merged.append(d.getID());
							mergedClusters++;
						}
					}
				}
			}
		}

		if(p.p.debug.field)
			PApplet.println("Merged Clusters..."+mergedClusters);
		
		ArrayList<WMV_Cluster> newList = new ArrayList<WMV_Cluster>();
		
		for(WMV_Cluster c : clusterList)
		{
			if(!absorbed.hasValue(c.getID()))
			{
				newList.add(c);
			}
		}
		
		if(p.p.debug.field)
			PApplet.println("Final clusters size..."+newList.size());
		return newList;
	}
	
	/**
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

		timeline.sort(WMV_TimeSegment.WMV_TimeLowerBoundComparator);			// Sort time segments 
		
		int count = 0;															// Number in chronological order on field timeline
		for (WMV_TimeSegment t : timeline) 		
		{
//			t.setID(count);
			t.setFieldTimelineID(count);
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
					if(!dateline.contains(nd))
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
	private void createTimelines()
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
					t.setFieldDateID(ct);
					t.setFieldTimelinesID(count);

					for(WMV_TimeSegment fieldTime : timeline)		 
					{
						if(t.getClusterID() == fieldTime.getClusterID() && t.getFieldTimelineID() == fieldTime.getFieldTimelineID())
						{
							fieldTime.setFieldDateID(ct);
							fieldTime.setFieldTimelinesID(count);
//							PApplet.println("Correcting fieldTime for cluster #:"+fieldTime.getClusterID()+" ct:"+ct+" count:"+count
//											+" newTimeline size:"+newTimeline.size());	
						}
					}
					
					count++;
				}
				timelines.add( newTimeline );		// Calculate and add timeline to list
				PApplet.println("Added timeline #"+ct+" for field #"+fieldID+" with "+newTimeline.size()+" segments...");
			}
			else
			{
				timelines.add( newTimeline );		// Add empty timeline to preserve indexing 
				PApplet.println("Added EMPTY timeline #"+ct+" for field #"+fieldID);
			}
			
			ct++;
		}
	}
	
//	/**
//	 * Create timeline for each date on dateline, where index of a date in dateline matches index of corresponding timeline in timelines array
//	 */
//	void createTimelines()
//	{
//		timelines = new ArrayList<ArrayList<WMV_TimeSegment>>();
//
//		for(WMV_Date d : dateline)			// For each date on dateline
//		{
//			ArrayList<WMV_TimeSegment> dateTimeline = new ArrayList<WMV_TimeSegment>();		// List of times to add to date-specific timeline for field
//			for(WMV_Cluster c : clusters)
//			{
//				ArrayList<WMV_Time> clusterMediaTimes = new ArrayList<WMV_Time>();		// List of times to add to date-specific timeline for field
//				if(!c.isEmpty())
//				{
//					/* Old method */
////					for(WMV_Date cd : c.dateline)									// Search for date within cluster dateline
////					{
////						if(d.getDate().equals(cd.getDate()))											// Compare dates
////						{
////							for(WMV_TimeSegment t : c.timelines.get(cd.getID()))		// Go through date-specific timeline for cluster
////							{
////								for(WMV_Time time : t.getTimeline())				// Add all times to list
////								{
////									clusterMediaTimes.add(time);
////								}
////							}
////						}
////					}
////					if(clusterMediaTimes.size() > 0)
////					{
////						ArrayList<WMV_TimeSegment> clusterTimeline = p.p.utilities.calculateTimeSegments(clusterMediaTimes, p.settings.clusterTimePrecision, c.getID());
////						for(WMV_TimeSegment ts : clusterTimeline)
////							dateTimeline.add(ts);
////					}
//
//					for(WMV_Date cd : c.dateline)										// Search within cluster dateline
//					{
//						if(d.getDate().equals(cd.getDate()))							// Compare cluster date to field date
//						{
//							if(c.timelines.size() > cd.getID())
//							{
//								for(WMV_TimeSegment t : c.timelines.get(cd.getID()))		// Add each cluster time segment to this date-specific field timeline 
//									dateTimeline.add(t);
//							}
//							else
//								PApplet.println("Cluster has date #"+cd.getID()+" but no timeline #"+cd.getID()+" !!!");
//						}
//					}
//				}
//			}
//
//			if(dateTimeline != null) 
//			{
//				if(dateTimeline.size() > 0)
//				{
//					dateTimeline.sort(WMV_TimeSegment.WMV_TimeLowerBoundComparator);		// Sort timeline  
//
////					int count = 0;
////					for (WMV_TimeSegment t : dateTimeline) 		
////					{
////						t.setID(count);
////						count++;
////					}
//
//					timelines.add( dateTimeline );		// Add timeline to list
//				}
//			}
//		}
//		
//		if(p.p.debug.field) PApplet.println("Created "+timelines.size()+" date-specific timelines for field #"+fieldID+" from cluster time segments...");
//	}
	
	/**
	 * @return List of waypoints based on field timeline
	 */
	public ArrayList<WMV_Waypoint> getTimelineAsPath()
	{
		ArrayList<WMV_Waypoint> timelinePath = new ArrayList<WMV_Waypoint>();

		for(WMV_TimeSegment t : timeline)
		{
			WMV_Waypoint w = clusters.get(t.getClusterID()).getClusterAsWaypoint();
			timelinePath.add(w);
		}
		
		if(p.p.debug.field)
			PApplet.println("getTimelineAsPath()... timelinePath.size():"+timelinePath.size());

		return timelinePath;
	}

	public int getFirstTimeSegment()
	{
		if(timeline.size() > 0)
			return timeline.get(0).getFieldTimelineID();
		else
			return -1;
	}

	/**
	 * Get ID of time segment <number> in field timeline matching given cluster ID 
	 * @param id Cluster to get time segment from
	 * @param index Segment in cluster timeline to get
	 * @return ID of time segment
	 */
	public WMV_TimeSegment getTimeSegmentInCluster(int id, int index)
	{
		WMV_TimeSegment t = null;
		
		if(id >= 0 && id < clusters.size())
		{
			if(clusters.get(id).getTimeline() != null)
				if(index >= 0 && index < clusters.get(id).getTimeline().size())
					t = clusters.get(id).getTimeline().get(index);
		}

		if(t == null)
		{
			p.display.message("NULL time segment "+index+" returned by getTimeSegmentInCluster() id:"+id+" index:"+index+" timeline size:"+clusters.get(id).getTimeline().size());
		}
		else if(id != t.getClusterID())
			PApplet.println("ERROR in getTimeSegmentInCluster().. clusterID and timeSegment clusterID do not match!  clusterID:"+id+" t.getClusterID():"+t.getClusterID());

		return t;
	}

	/**
	 * @param id Cluster ID
	 * @param index Date index
	 * @return Date object specified by index
	 */
	public WMV_Date getDateInCluster(int id, int index)
	{
		WMV_Date d = null;
		
		if(id >= 0 && id < clusters.size())
		{
			if(clusters.get(id).dateline != null)
				if(index >= 0 && index < clusters.get(id).dateline.size())
					d = clusters.get(id).dateline.get(index);
		}

		if(d == null)
			p.display.message("Couldn't get date "+index+" in cluster "+id);
		
		return d;
	}
	
	public int getFieldDateIndexOfDate(WMV_Date clusterDate)
	{
		int idx = -1;
		
		for(WMV_Date d : dateline)
		{
			if(d.equals(clusterDate))
			{
				idx = d.getID();
			}
		}
		
		return idx;
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
			clusters.get(p.viewer.getCurrentClusterID()).drawCenter(255);		// Draw current cluster
		
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
	
	public WMV_TimeSegment getCurrentFieldTimeSegment(int date)
	{
		return timeline.get(p.viewer.getCurrentFieldTimeSegment());
//		return getFieldTimeSegmentFromID( p.viewer.currentTimeSegment, date );
	}

//	public WMV_TimeSegment getFieldTimeSegmentFromID(int id, int date)
//	{
////		if(dateline.size() == 1)
////			return timeline.get(id);
////		else if(dateline.size() > 1)
////		{
////			if(date == -1)
////			{
////				for(ArrayList<WMV_TimeSegment> ts : timelines)
////					for(WMV_TimeSegment t:ts)
////						if(t.getID()==id)
////							if(t.getClusterID()==timeline.get(id).getClusterID())
////								return t;
////			}
////			else
////			{
////				for(WMV_TimeSegment t:timelines.get(date))
////					if(t.getID()==id)
////						if(t.getClusterID()==timeline.get(id).getClusterID())				// Check same cluster 
////							return t;
////			}
////		}		
////		return null;
//		
//		/* Old method */
//		if(dateline.size() == 1)
//		{
//			return timeline.get(id);
//		}
//		else if(dateline.size() > 1)							// Find field time segment on date specific timeline(s)
//		{
//			WMV_TimeSegment goalTS = timeline.get(id);
////			PApplet.println("New goalTS...");
//			if(date == -1)
//			{
//				for(ArrayList<WMV_TimeSegment> ts : timelines)
//				{
//					for(WMV_TimeSegment t:ts)
//					{
//						if(t.getClusterID()==goalTS.getClusterID())												// Same cluster
//						{
////							PApplet.print(" any date same cluster:"+t.getClusterID());
//							if(t.getUpper().getTimeAsPVector().equals(goalTS.getUpper().getTimeAsPVector()) && t.getLower().getTimeAsPVector().equals(goalTS.getLower().getTimeAsPVector()))
//							{
////								PApplet.println("   FOUND match: "+t.getUpper().getTimeAsPVector()+" vs "+goalTS.getUpper().getTimeAsPVector());
//								return t;																	// Found the time segment
//							}
////							else
////							{
////								PApplet.print("   but no match: "+t.getUpper().getTimeAsPVector()+" vs "+goalTS.getUpper().getTimeAsPVector());
////								PApplet.println(" ... : "+t.getLower().getTimeAsPVector()+" vs "+goalTS.getLower().getTimeAsPVector());
////							}
//						}
//					}
//				}
//			}
//			else
//			{
//				if(date < timelines.size())
//				{
//					ArrayList<WMV_TimeSegment> ts = timelines.get(date);
//
//					for(WMV_TimeSegment t:ts)
//					{
//						if(t.getClusterID()==goalTS.getClusterID())												// Same cluster
//						{
//							PApplet.println(" on date  t.getClusterID()==goalTS.getClusterID():"+t.getClusterID());
//							if(t.getUpper().getTimeAsPVector() == goalTS.getUpper().getTimeAsPVector() && t.getLower().getTimeAsPVector() == goalTS.getLower().getTimeAsPVector())
//								return t;																	// Found the time segment
//							else
//							{
//								PApplet.print("   but no match: "+t.getUpper().getTimeAsPVector()+" vs "+goalTS.getUpper().getTimeAsPVector());
//								PApplet.println(" ... : "+t.getLower().getTimeAsPVector()+" vs "+goalTS.getLower().getTimeAsPVector());
//							}
//						}
//					}
//				}
//			}
//		}
//		PApplet.println("FAILED...");
//
//		return null;
//	}


	public int getFieldTimeSegmentID(WMV_TimeSegment goal)
	{
		if(dateline.size() == 1)
		{
			return goal.getFieldTimelineID();
		}
		else if(dateline.size() > 1)
		{
			return goal.getFieldTimelineIDOnDate();
			
//			WMV_TimeSegment goalTS = timeline.get(goal.getID());
//			for(ArrayList<WMV_TimeSegment> ts : timelines)
//			{
//				for(WMV_TimeSegment t:ts)
//				{
//					if(t.getClusterID()==goalTS.getClusterID())
//						return t.getID();
//				}
//			}
			
//			for(WMV_TimeSegment t : timeline)
//			{
//				if(goal.getClusterID() == t.getClusterID())
//				{
//					if(goal.getLower() == t.getLower() && goal.getUpper() == t.getUpper() && goal.timeline.size() == t.timeline.size())
//						return t.getID();
//				}
//			}
		}

		return -1;
	}

	/**
	 * Change all clusters to non-attractors
	 */
	public void clearAllAttractors()
	{
		if(p.p.debug.viewer && p.p.debug.detailed)
			PApplet.println("Clearing all attractors...");

		if(p.viewer.getAttractorCluster() != -1)
			p.viewer.clearAttractorCluster();

		for(WMV_Cluster c : clusters)
			if(c.isAttractor())
				c.setAttractor(false);
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
	
	/**
	 * @return List of IDs of currently selected images
	 */
	public IntList getSelectedImages()
	{
		IntList selected = new IntList();

		for(WMV_Image i : images)
			if(i.isSelected())
				selected.append(i.getID());
		
		return selected;
	}
	
	/**
	 * Show any image in field if visible
	 */
	public void showImages()
	{
		hideImages = false;
		if(p.display.window.setupGraphicsWindow)
			p.display.window.chkbxHideImages.setSelected(false);
	}
	
	/**
	 * Hide all images in field
	 */
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

		if(p.display.window.setupGraphicsWindow)
			p.display.window.chkbxHideImages.setSelected(true);
	}
	
	/** 
	 * Show any panorama in field if visible
	 */
	public void showPanoramas()
	{
		hidePanoramas = false;

		if(p.display.window.setupGraphicsWindow)
			p.display.window.chkbxHidePanoramas.setSelected(false);
	}
	
	/** 
	 * Hide all panoramas in field
	 */
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
		
		if(p.display.window.setupGraphicsWindow)
			p.display.window.chkbxHidePanoramas.setSelected(true);
	}
	
	/**
	 * Show any video in field if visible
	 */
	public void showVideos()
	{
		hideVideos = false;
		if(p.display.window.setupGraphicsWindow)
			p.display.window.chkbxHideVideos.setSelected(false);
	}
	
	/**
	 * Hide all videos in field
	 */
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
		
		if(p.display.window.setupGraphicsWindow)
			p.display.window.chkbxHideVideos.setSelected(true);
	}
	
	/**
	 * @return Index of nearest cluster to camera, excluding the current cluster
	 */
	int getNearestClusterToPoint(PVector target) 	// Returns the cluster nearest to the current camera position, excluding the current cluster
	{
		float smallest = 100000.f;
		int smallestIdx = 0;

		if (p.getCurrentField().clusters.size() > 0) 
		{
//			for (WMV_Cluster c : p.getActiveClusters()) 
			for(WMV_Cluster c : clusters)
			{
				float dist = PVector.dist(target, c.getLocation());
				if (dist < smallest) 
				{
					if(!c.isEmpty())
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
	 * Get cluster at either edge of field on Z axis (latitude)
	 * @param north Whether to return cluster at North edge (true) or South edge (false)
	 * @return Specified edge cluster
	 */
	public WMV_Cluster getEdgeClusterOnZAxis(boolean north)
	{
		WMV_Cluster result = null;
		if(north)
		{
			float lowest = 100000.f;
			int lowestIdx = -1;
			
			for(WMV_Cluster c : clusters)
			{
				if(c.getLocation().z < lowest && !c.isEmpty())
				{
					lowest = c.getLocation().z;
					lowestIdx = c.getID();
				}
			}
			
			result = clusters.get(lowestIdx);
			return result;
		}
		else
		{
			float highest = -100000.f;
			int highestIdx = -1;
			
			for(WMV_Cluster c : clusters)
			{
				if(c.getLocation().z > highest && !c.isEmpty())
				{
					highest = c.getLocation().z;
					highestIdx = c.getID();
				}
			}

			result = clusters.get(highestIdx);
			return result;
		}
	}
	
	/**
	 * Get cluster at either edge of field on X axis (longitude)
	 * @param west Whether to return cluster at West edge (true) or East edge (false)
	 * @return Specified edge cluster
	 */
	public WMV_Cluster getEdgeClusterOnXAxis(boolean west)
	{
		WMV_Cluster result = null;
		if(west)
		{
			float lowest = 100000.f;
			int lowestIdx = -1;
			
			for(WMV_Cluster c : clusters)
			{
				if(c.getLocation().x < lowest && !c.isEmpty())
				{
					lowest = c.getLocation().x;
					lowestIdx = c.getID();
				}
			}
			
			result = clusters.get(lowestIdx);
			return result;
		}
		else
		{
			float highest = -100000.f;
			int highestIdx = -1;
			
			for(WMV_Cluster c : clusters)
			{
				if(c.getLocation().x > highest && !c.isEmpty())
				{
					highest = c.getLocation().x;
					highestIdx = c.getID();
				}
			}

			result = clusters.get(highestIdx);
			return result;
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

	public int getSoundCount()
	{
//		return sounds.size() - soundErrors;
		return sounds.size();
	}

	public int getMediaCount()
	{
		return getImageCount() + getPanoramaCount() + getVideoCount() + getSoundCount();
	}

	/**
	 * Remove specified cluster
	 * @param r Cluster to remove
	 */
	public void removeCluster(WMV_Cluster r)
	{
		clusters.remove(r);
	}
}
