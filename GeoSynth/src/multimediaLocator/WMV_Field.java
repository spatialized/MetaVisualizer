package multimediaLocator;

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
	private int id;

	/* File System */
	private String name;

	/* Graphics */
	private int imagesVisible = 0, imagesSeen = 0;			// Number of visible photos and currently seen
	private int panoramasVisible = 0, panoramasSeen = 0;		// Number of visible panoramas and currently seen
	private int videosVisible = 0, videosLoaded = 0, videosPlaying = 0, videosSeen = 0;

	/* Data */
	private WMV_Model model;										// Dimensions and properties of current virtual space
	private ArrayList<WMV_Image> images; 					// All images in this field
	private ArrayList<WMV_Panorama> panoramas; 				// All panoramas in this field
	private ArrayList<WMV_Video> videos; 					// All videos in this field
	private ArrayList<WMV_Sound> sounds; 					// All videos in this field
	private ArrayList<WMV_Cluster> clusters;					// Spatial groupings of media in the Image3D and Video3D arrays

	private int imageErrors = 0, videoErrors = 0, panoramaErrors = 0;			// Metadata loading errors per media type

	/* Time */
	private ArrayList<WMV_TimeSegment> timeline;						// List of time segments in this field ordered by time from 0:00 to 24:00 as a single day
	private ArrayList<ArrayList<WMV_TimeSegment>> timelines;			// Lists of time segments in field ordered by date
	private ArrayList<WMV_Date> dateline;								// List of dates in this field, whose indices correspond with timelines in timelines list
	private String timeZoneID = "America/Los_Angeles";					// Current time zone

	/* Utilities */
	WMV_Utilities utilities;					// Utility methods

	WMV_World p;
	WMV_WorldSettings worldSettings;
	WMV_ViewerSettings viewerSettings;	// Update world settings
	ML_DebugSettings debugSettings;	// Update world settings

	/* -- Debug -- */	
	private int disassociatedImages = 0;						// Images not associated with a cluster -- Still needed?
	private int disassociatedPanoramas = 0;
	private int disassociatedVideos = 0;

	WMV_Field( WMV_World parent, WMV_WorldSettings newWorldSettings, WMV_ViewerSettings newViewerSettings, ML_DebugSettings newDebugSettings, 
			   String newMediaFolder, int newFieldID )
	{
		p = parent;
		utilities = new WMV_Utilities();
		
		worldSettings = newWorldSettings;
		viewerSettings = newViewerSettings;
		debugSettings = newDebugSettings;
		
		name = newMediaFolder;
		id = newFieldID;

		model = new WMV_Model(this);
		clusters = new ArrayList<WMV_Cluster>();
		
		images = new ArrayList<WMV_Image>();
		panoramas = new ArrayList<WMV_Panorama>();
		videos = new ArrayList<WMV_Video>();		
		sounds = new ArrayList<WMV_Sound>();		

		timeline = new ArrayList<WMV_TimeSegment>();
		dateline = new ArrayList<WMV_Date>();
	}

	public void display() 				// Draw currently visible media
	{
		float vanishingPoint = viewerSettings.farViewingDistance + worldSettings.defaultFocusDistance;	// Distance where transparency reaches zero
		
		imagesVisible = 0;
		imagesSeen = 0;
		panoramasVisible = 0;
		videosVisible = 0;
		videosSeen = 0;

		for (WMV_Image m : images) 		// Update and display images
		{
			if(!m.disabled)
			{
				float distance = m.getViewingDistance(); // Estimate image distance to camera based on capture location
				
				if(!m.verticesAreNull() && (m.isFading() || m.fadingFocusDistance))
					m.update();  		// Update geometry + visibility

				if (distance < vanishingPoint && distance > viewerSettings.nearClippingDistance && !m.verticesAreNull()) 	// Visible	
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

//		if(debugSettings.model || viewerSettings.map3DMode)
//		{
//			if(clusters.size()>0)
//				showClusterCenters();									// Display field cluster centers (media capture locations) 	
//		}
		
//		if(worldSettings.showUserPanoramas || worldSettings.showStitchedPanoramas)
//		{
//			if(clusters.size()>0)
//				clusters.get(p.viewer.getCurrentClusterID()).draw();		// Draw current cluster
//		}
	}
	
	/**
	 * Initialize field with given library folder
	 * @param library Current library folder
	 * @param lockMediaToClusters Center media capture locations at associated cluster locations
	 */
	public void initialize(String library, boolean lockMediaToClusters)
	{
		if(debugSettings.main) PApplet.println("Initializing field #"+id);
		
		model.calculateFieldSize(); 		// Calculate bounds of photo GPS locations
		model.analyzeMedia();				// Analyze media locations and times 
		model.setup(); 						// Initialize field for first time 

		calculateMediaLocations(); 			// Set location of each photo in simulation
//		detectMultipleFields();				// Run clustering on capture locations to detect multiple fields

		// TESTING
//		divideField(3000.f, 15000.f);			
		
		findImagePlaceHolders();			// Find image place holders for videos
		calculateMediaVertices();			// Calculate all image vertices

		if(debugSettings.main) PApplet.println("Will run initial clustering for field #"+id+"...");
//		if(debugSettings.main) p.display.message("Will run initial clustering for field #"+id+"...");

		model.runInitialClustering();		// Find media clusters
//		model.findDuplicateClusterMedia();	// Find media in more than one cluster
		
		if(lockMediaToClusters)				// Center media capture locations at associated cluster locations
			lockMediaToClusters();	

		if(debugSettings.main) PApplet.println("Creating timeline and dateline for field #"+id+"...");
//		if(debugSettings.main) p.display.message("Creating timeline and dateline for field #"+id+"...");

		if( worldSettings.getTimeZonesFromGoogle )		// Get time zone for field from Google Time Zone API
		{
			if(images.size() > 0)					
				timeZoneID = utilities.getCurrentTimeZoneID(images.get(0).getGPSLocation().z, images.get(0).getGPSLocation().x);
			else if(panoramas.size() > 0)
				timeZoneID = utilities.getCurrentTimeZoneID(panoramas.get(0).getGPSLocation().z, panoramas.get(0).getGPSLocation().x);
			else if(videos.size() > 0)
				timeZoneID = utilities.getCurrentTimeZoneID(videos.get(0).getGPSLocation().z, videos.get(0).getGPSLocation().x);
			else if(sounds.size() > 0)
				timeZoneID = utilities.getCurrentTimeZoneID(sounds.get(0).getGPSLocation().z, sounds.get(0).getGPSLocation().x);
		}

		createTimeline();								// Create date-independent timeline for field
		createDateline();								// Create field dateline
		createTimelines();								// Create date-specific timelines for field
		analyzeClusterMediaDirections();			// Analyze angles of all images and videos in each cluster for Thinning Visibility Mode
		
		if(debugSettings.main) PApplet.println("Finished initializing field #"+id+"..."+name);
//		if(debugSettings.main) p.display.message("Finished initializing field #"+id+"..."+name);
	}
	
	/**
	 * Update field variables each frame
	 */
//	public void update(WMV_WorldSettings currentWorldSettings, WMV_ViewerSettings currentViewerSettings, ML_DebugSettings currentDebugSettings)
	public void update(WMV_WorldSettings currentWorldSettings, WMV_ViewerSettings currentViewerSettings)
	{
		worldSettings = currentWorldSettings;	// Update world settings
		viewerSettings = currentViewerSettings;	// Update world settings
//		debugSettings = currentDebugSettings;	// Update world settings
	}

	public void analyzeClusterMediaDirections()
	{
		for(WMV_Cluster c : getClusters())
			if(!c.isEmpty())
				c.analyzeMediaDirections();
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
		if(debugSettings.field) PApplet.println("Calculating image locations...");

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
		for (WMV_Panorama n : panoramas)
			n.initializeSphere();
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
	void initializeClusters(boolean mergeClusters)
	{
		for( WMV_Cluster c : clusters )
		{
			if(c.mediaCount <= 0)
			{
				c.empty();
				
				if(debugSettings.cluster)
					PApplet.println("Fixed empty cluster #"+c.getID()+"!!!");
			}
		}
		
		if(mergeClusters) model.mergeAdjacentClusters();		// Merge clusters

		initializeClusterMedia();							
		verifyField();				// Verify field parameters
	}
	
	/**
	 * Analyze media and initialize cluster variables for each media item 
	 */
	void initializeClusterMedia()
	{
		if(debugSettings.cluster)
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
		if(debugSettings.field) 	PApplet.println("Calculating media vertices...");
		
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
		if(debugSettings.field) PApplet.println("Fading out media...");

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
		if(debugSettings.field) PApplet.println("Fading out media...");

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
		if(debugSettings.field) PApplet.println("Stopping all fading...");

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
		if(debugSettings.field) PApplet.println("Verifying field...");

		boolean exit = false;

		if (model.fieldWidth <= 0 && clusters.size() > 1)
		{
			if(debugSettings.model)
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
//			p.p.exit();
		} 
		else {
			if(debugSettings.field)
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
		for (WMV_Sound s : sounds)
		{
			if(s.isSelected())
			{
				s.setSelected(false);
				if(hide) s.hidden = true;
			}
		}
	}
	
	/**
	 * Return list of selected media IDs of given type
	 * @param mediaType 0: image 1: panorama 2: video 3: sound
	 */
	public IntList getSelectedMedia(int mediaType) 
	{
		IntList selected = new IntList();
		
		switch(mediaType)
		{
			case 0:
				for (WMV_Image i : images)
					if(i.isSelected())
						selected.append(i.getID());
				break;
			case 1:
				for (WMV_Panorama n : panoramas)
					if(n.isSelected())
						selected.append(n.getID());
				break;
	
			case 2:
				for (WMV_Video v : videos)
					if(v.isSelected())
						selected.append(v.getID());
				break;
	
			case 3:
				for (WMV_Sound s : sounds)
					if(s.isSelected())
						selected.append(s.getID());
				break;
		}
		
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

//		if(debugSettings.viewable || debugSettings.field)
//			if(fading)
//				p.display.message("Still fading media...");
		
		return fading;
	}

	/**
	 * @return List of attracting clusters
	 */
	public ArrayList<WMV_Cluster> getAttractingClusters()
	{
		ArrayList<WMV_Cluster> cList = new ArrayList<WMV_Cluster>();
		for(WMV_Cluster c : clusters)		// Attract the camera to the attracting cluster(s) 
		{
			if(c.isAttractor())										
			{
				if(c.getClusterDistance() > worldSettings.clusterCenterSize)		// If not already at attractor cluster center, attract camera 
					cList.add(c);
			}
		}
		return cList;
	}

	 /**
	  * If image is within <threshold> from center of cluster along axes specified by mx, my and mz, 
	  * fold the image location into the cluster location along those axes.
	  */
	 public void lockMediaToClusters()
	 {
//		 if(debugSettings.field || debugSettings.model) PApplet.println("lockMediaToClusters(): Moving media... ");
		 for (int i = 0; i < getImages().size(); i++) 
			 getImage(i).adjustCaptureLocation();		
		 for (int i = 0; i < getPanoramas().size(); i++) 
			 getPanorama(i).adjustCaptureLocation();		
		 for (int i = 0; i < getVideos().size(); i++) 
			 getVideo(i).adjustCaptureLocation();		
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
		
		if(debugSettings.cluster)
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

		if(debugSettings.field)
			PApplet.println("Merged Clusters..."+mergedClusters);
		
		ArrayList<WMV_Cluster> newList = new ArrayList<WMV_Cluster>();
		
		for(WMV_Cluster c : clusterList)
		{
			if(!absorbed.hasValue(c.getID()))
			{
				newList.add(c);
			}
		}
		
		if(debugSettings.field)
			PApplet.println("Final clusters size..."+newList.size());
		return newList;
	}

	/**
	 * Create date-independent timeline for this field from cluster timelines
	 */
	public void createTimeline()
	{
		timeline = new ArrayList<WMV_TimeSegment>();
		
		if(debugSettings.time)
			PApplet.println(">>> Creating Field Timeline... <<<");

		for(WMV_Cluster c : clusters)											// Find all media cluster times
			for(WMV_TimeSegment t : c.getTimeline())
				timeline.add(t);

		timeline.sort(WMV_TimeSegment.WMV_TimeLowerBoundComparator);			// Sort time segments 
		
		int count = 0;															// Number in chronological order on field timeline
		for (WMV_TimeSegment t : timeline) 		
		{
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
//				PApplet.println("Added timeline #"+ct+" for field #"+fieldID+" with "+newTimeline.size()+" segments...");
			}
			else
			{
				timelines.add( newTimeline );		// Add empty timeline to preserve indexing 
//				PApplet.println("Added EMPTY timeline #"+ct+" for field #"+fieldID);
			}
			
			ct++;
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
			WMV_Waypoint w = clusters.get(t.getClusterID()).getClusterAsWaypoint();
			timelinePath.add(w);
		}
		
		if(debugSettings.field)
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
//			p.display.message("NULL time segment "+index+" returned by getTimeSegmentInCluster() id:"+id+" index:"+index+" timeline size:"+clusters.get(id).getTimeline().size());
			PApplet.println("NULL time segment "+index+" returned by getTimeSegmentInCluster() id:"+id+" index:"+index+" timeline size:"+clusters.get(id).getTimeline().size());
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
			if(clusters.get(id).getDateline() != null)
				if(index >= 0 && index < clusters.get(id).getDateline().size())
					d = clusters.get(id).getDateline().get(index);
		}

		if(d == null)
			PApplet.println("Couldn't get date "+index+" in cluster "+id);
//			p.display.message("Couldn't get date "+index+" in cluster "+id);
		
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
	
	/**
	 * Detect and return multiple fields via k-means clustering 
	 * @param f Field to divide
	 * @return List of created fields
	 */
	ArrayList<WMV_Field> divideField(float minFieldDistance, float maxFieldDistance)
	{
		ArrayList<WMV_Cluster> fieldClusters = new ArrayList<WMV_Cluster>();			// Clear current cluster list

		/* Estimate number of clusters */
		int numFields = 10; 								// Estimate number of clusters 
		float epsilon = worldSettings.kMeansClusteringEpsilon;
		int refinement = 60;
				
		/* K-means Clustering */
		IntList addedImages = new IntList();			// Images already added to clusters; should include all images at end
		IntList nearImages = new IntList();			// Images nearby added media 

		IntList addedPanoramas = new IntList();		// Panoramas already added to clusters; should include all panoramas at end
		IntList nearPanoramas = new IntList();		// Panoramas nearby added media 

		IntList addedVideos = new IntList();			// Videos already added to clusters; should include all videos at end
		IntList nearVideos = new IntList();			// Videos nearby added media 

		for (int i = 0; i < numFields; i++) 		// Iterate through the clusters
		{
			int imageID, panoramaID, videoID;			

			if(i == 0)			
			{
				long clusteringRandomSeed = (long) p.p.random(1000.f);
				p.p.randomSeed(clusteringRandomSeed);
				imageID = (int) p.p.random(getImages().size());  			// Random image ID for setting cluster's start location				
				panoramaID = (int) p.p.random(getPanoramas().size());  		// Random panorama ID for setting cluster's start location				
				videoID = (int) p.p.random(getVideos().size());  			// Random video ID for setting cluster's start location				
				addedImages.append(imageID);								
				addedPanoramas.append(panoramaID);								
				addedVideos.append(videoID);								

				for(WMV_Image img : getImages())						// Check for images near the picked one
				{
					float dist = img.getCaptureDistanceFrom(getImage(imageID).getCaptureLocation());  // Get distance
					if(dist < minFieldDistance)
						nearImages.append(img.getID());				// Record images nearby picked image
				}

				for(WMV_Panorama pano : getPanoramas())				// Check for panoramas near the picked one 
				{
					float dist = pano.getCaptureDistanceFrom(getPanorama(panoramaID).getCaptureLocation());  // Get distance
					if(dist < minFieldDistance)
						nearPanoramas.append(pano.getID());			// Add to the list of nearby picked images
				}

				for(WMV_Video vid : getVideos())				// Check for panoramas near the picked one 
				{
					float dist = vid.getCaptureDistanceFrom(getVideo(videoID).getCaptureLocation());  // Get distance
					if(dist < minFieldDistance)
						nearVideos.append(vid.getID());			// Add to the list of nearby picked images
				}

				PVector clusterPoint = new PVector(0,0,0);				// Create first cluster 
				if(getImages().size() > 0)
				{
					PVector imgLoc = getImage(imageID).getCaptureLocation();
					clusterPoint = new PVector(imgLoc.x, imgLoc.y, imgLoc.z); // Choose random image location to start
					fieldClusters.add(new WMV_Cluster(this, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
					i++;
				}
				else if(getPanoramas().size() > 0)
				{
					PVector panoLoc = getPanorama(panoramaID).getCaptureLocation();
					clusterPoint = new PVector(panoLoc.x, panoLoc.y, panoLoc.z); // Choose random panorama location to start
					fieldClusters.add(new WMV_Cluster(this, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
					i++;
				}
				else if(getVideos().size() > 0)
				{
					PVector vidLoc = getVideo(videoID).getCaptureLocation();
					clusterPoint = new PVector(vidLoc.x, vidLoc.y, vidLoc.z); // Choose random video location to start
					fieldClusters.add(new WMV_Cluster(this, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
					i++;
				}
			}
			else											// Find a random media (image, panorama or video) location for new cluster
			{
				int mediaID = (int) p.p.random(getImages().size() + getPanoramas().size() + getVideos().size());
				PVector clusterPoint = new PVector(0,0,0);

				if( mediaID < getImages().size() )				// If image, compare to already picked images
				{
					imageID = (int) p.p.random(getImages().size());  						
					while(addedImages.hasValue(imageID) && nearImages.hasValue(imageID))
						imageID = (int) p.p.random(getImages().size());  						

					addedImages.append(imageID);
					
					PVector imgLoc = getImage(imageID).getCaptureLocation();
					clusterPoint = new PVector(imgLoc.x, imgLoc.y, imgLoc.z); // Choose random image location to start
				}
				else if( mediaID < getImages().size() + getPanoramas().size() )		// If panorama, compare to already picked panoramas
				{
					panoramaID = (int) p.p.random(getPanoramas().size());  						
					while(addedPanoramas.hasValue(panoramaID) && nearPanoramas.hasValue(panoramaID))
						panoramaID = (int) p.p.random(getPanoramas().size());  						

					addedPanoramas.append(panoramaID);
					
					PVector panoLoc = getPanorama(panoramaID).getCaptureLocation();
					clusterPoint = new PVector(panoLoc.x, panoLoc.y, panoLoc.z); // Choose random image location to start
				}
				else if( mediaID < getImages().size() + getPanoramas().size() + getVideos().size() )		// If video, compare to already picked videos
				{
					videoID = (int) p.p.random(getVideos().size());  						
					while(addedImages.hasValue(videoID) && nearImages.hasValue(videoID))
						videoID = (int) p.p.random(getVideos().size());  						

					addedVideos.append(videoID);
					
					PVector vidLoc = getVideo(videoID).getCaptureLocation();
					clusterPoint = new PVector(vidLoc.x, vidLoc.y, vidLoc.z); // Choose random image location to start
				}

				fieldClusters.add(new WMV_Cluster(this, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
			}
		}	
		
		/* Refine fields */
		int count = 0;
		boolean moved = false;									// Whether any cluster has moved farther than epsilon

		ArrayList<WMV_Cluster> last = fieldClusters;
		
		if(debugSettings.field)
			PApplet.println("--> Refining fields...");

		while( count < refinement ) 							// Iterate to create the clusters
		{		
			for (int i = 0; i < getImages().size(); i++) 			// Find closest cluster for each image
				getImage(i).findAssociatedCluster(fieldClusters, maxFieldDistance);		// Set associated cluster
			for (int i = 0; i < getPanoramas().size(); i++) 		// Find closest cluster for each image
				getPanorama(i).findAssociatedCluster(fieldClusters, maxFieldDistance);		// Set associated cluster
			for (int i = 0; i < getVideos().size(); i++) 		// Find closest cluster for each panorama
				getVideo(i).findAssociatedCluster(fieldClusters, maxFieldDistance);		// Set associated cluster
			for (int i = 0; i < fieldClusters.size(); i++) 		// Find closest cluster for each video
				fieldClusters.get(i).create();					// Assign clusters

			if(fieldClusters.size() == last.size())				// Check cluster movement
			{
				for(WMV_Cluster c : fieldClusters)
				{
					float closestDist = 10000.f;

					for(WMV_Cluster d : last)
					{
						float dist = c.getLocation().dist(d.getLocation());
						if(dist < closestDist)
						{
							closestDist = dist;
						}
					}

					if(closestDist > epsilon)
					{
						moved = true;
					}
				}

				if(!moved)
				{
					if(debugSettings.field)
						PApplet.println(" Stopped refinement... no clusters moved farther than epsilon:"+epsilon);
					break;								// If all clusters moved less than epsilon, stop refinement
				}
			}
			else
			{
				if(debugSettings.field)
					PApplet.println(" New clusters found... will keep refining clusters... clusters.size():"+fieldClusters.size()+" last.size():"+last.size());
			}

			count++;
		}

		fieldClusters = mergeAdjacentClusters(fieldClusters, 2500.f);

		if(debugSettings.field)
			PApplet.println("Detected "+fieldClusters.size()+" fields...");

//		ArrayList<WMV_Field> result = new ArrayList<WMV_Field>();
//		count = 0;
//		for(WMV_Cluster c : fieldClusters)
//		{
//			WMV_Field field = new WMV_Field(this, null, c, count);
//			count++;
//			result.add(field);
//		}
//		
//		return result;
		
		return null;
	}
	
	public int getFieldTimeSegmentID(WMV_TimeSegment segment)
	{
		if(dateline.size() == 1)
			return segment.getFieldTimelineID();
		else if(dateline.size() > 1)
			return segment.getFieldTimelineIDOnDate();

		return -1;
	}

	/**
	 * Change all clusters to non-attractors
	 */
	public void clearAllAttractors()
	{
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

		for(WMV_Panorama n:panoramas)
		{
			float newRadius = n.getOrigRadius() * multiple;
//			n.fadeRadius(newRadius);			// --- Need to implement
			n.setRadius(newRadius);
		}

		for(WMV_Video v:videos)
		{
			float newFocusDistance = v.getFocusDistance() * multiple;
			v.fadeFocusDistance(newFocusDistance);
		}
	}

	/**
	 * Reset object distances for each media point in field to original
	 */
	public void resetObjectDistances()
	{
		for(WMV_Image i:images)
			i.resetFocusDistance();

		for(WMV_Panorama n:panoramas)
			n.resetRadius();

		for(WMV_Video v:videos)
			v.resetFocusDistance();
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
	 * @return Index of nearest cluster to camera, excluding the current cluster
	 */
	int getNearestClusterToPoint(PVector target) 	// Returns the cluster nearest to the current camera position, excluding the current cluster
	{
		float smallest = 100000.f;
		int smallestIdx = 0;

		if (clusters.size() > 0) 
		{
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
			if(debugSettings.cluster)
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
	
	public int getID()
	{
		return id;
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
	
	public WMV_TimeSegment getTimeSegment(int idx)
	{
		return timeline.get(idx);
	}
	
	public WMV_TimeSegment getTimeSegmentOnDate(int tsIdx, int dateIdx)
	{
		return timelines.get(dateIdx).get(tsIdx);
	}
	
	public WMV_Date getDate(int idx)
	{
		return dateline.get(idx);
	}
	
	public String getTimeZoneID()
	{
		return timeZoneID;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String newName)
	{
		name = newName;
	}
	
	/**
	 * @return Model of this field
	 */
	public WMV_Model getModel()
	{
		return model;
	}

	public ArrayList<WMV_Cluster> getClusters()
	{
		return clusters;
	}
	
	public WMV_Cluster getCluster(int id)
	{
		WMV_Cluster c = clusters.get(id);
		return c;
	}

	public WMV_Image getImage(int id)
	{
		WMV_Image img = images.get(id);
		return img;
	}
	
	public ArrayList<WMV_Image> getImages()
	{
		return images;
	}

	public WMV_Panorama getPanorama(int id)
	{
		WMV_Panorama pano = panoramas.get(id);
		return pano;
	}
	
	public ArrayList<WMV_Panorama> getPanoramas()
	{
		return panoramas;
	}

	public WMV_Video getVideo(int id)
	{
		WMV_Video vid = videos.get(id);
		return vid;
	}
	
	public ArrayList<WMV_Video> getVideos()
	{
		return videos;
	}
	
	public WMV_Sound getSound(int id)
	{
		WMV_Sound snd = sounds.get(id);
		return snd;
	}
	
	public ArrayList<WMV_Sound> getSounds()
	{
		return sounds;
	}

	public void setClusters(ArrayList<WMV_Cluster> newClusters)
	{
		clusters = newClusters;
	}
	
	public void addCluster(WMV_Cluster cluster)
	{
		clusters.add(cluster);
	}
	
	public void addImage(WMV_Image image)
	{
		images.add(image);
	}

	public void addPanorama(WMV_Panorama panorama)
	{
		panoramas.add(panorama);
	}

	public void addVideo(WMV_Video video)
	{
		videos.add(video);
	}

	public void addSound(WMV_Sound sound)
	{
		sounds.add(sound);
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

//	private int imagesVisible = 0, imagesSeen = 0;			// Number of visible photos and currently seen
//	private int panoramasVisible = 0, panoramasSeen = 0;		// Number of visible panoramas and currently seen
//	private int videosVisible = 0, videosLoaded = 0, videosPlaying = 0, videosSeen = 0;

	public int getImagesVisible()
	{
		return imagesVisible;
	}
	
	public int getPanoramasVisible()
	{
		return panoramasVisible;
	}
	
	public int getVideosVisible()
	{
		return videosVisible;
	}

	public void setImagesVisible(int newValue)
	{
		imagesVisible = newValue;
	}
	
	public void setPanoramasVisible(int newValue)
	{
		panoramasVisible = newValue;
	}
	
	public void setVideosVisible(int newValue)
	{
		videosVisible = newValue;
	}

	public int getImagesSeen()
	{
		return imagesSeen;
	}
	
	public int getPanoramasSeen()
	{
		return panoramasSeen;
	}
	
	public int getVideosPlaying()
	{
		return videosPlaying;
	}
	
	public int getVideosSeen()
	{
		return videosSeen;
	}

	public int getVideosLoaded()
	{
		return videosLoaded;
	}
	
	public void setImagesSeen(int newValue)
	{
		imagesSeen = newValue;
	}
	
	public void setPanoramasSeen(int newValue)
	{
		panoramasSeen = newValue;
	}
	
	public void setVideosPlaying(int newValue)
	{
		videosPlaying = newValue;
	}
	
	public void setVideosSeen(int newValue)
	{
		videosSeen = newValue;
	}

	public void setVideosLoaded(int newValue)
	{
		videosLoaded = newValue;
	}
	
	public int getDisassociatedImages()
	{
		return disassociatedImages;
	}

	public int getDisassociatedPanoramas()
	{
		return disassociatedPanoramas;
	}

	public int getDisassociatedVideos()
	{
		return disassociatedVideos;
	}

	public void setDisassociatedImages(int newValue)
	{
		disassociatedImages = newValue;
	}

	public void setDisassociatedPanoramas(int newValue)
	{
		disassociatedPanoramas = newValue;
	}

	public void setDisassociatedVideos(int newValue)
	{
		disassociatedVideos = newValue;
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
