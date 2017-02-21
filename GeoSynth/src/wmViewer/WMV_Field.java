package wmViewer;

import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PVector;
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
	public ArrayList<WMV_Sound> sounds; 					// All videos in this field
	public ArrayList<WMV_Cluster> clusters;					// Spatial groupings of media in the Image3D and Video3D arrays

	private int imageErrors = 0, videoErrors = 0, panoramaErrors = 0;			// Metadata loading errors per media type

	/* Time */
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
		sounds = new ArrayList<WMV_Sound>();		

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
		p.p.background(0.f);									// Set background

		for (int i = 0; i < images.size(); i++) 		// Update and display images
		{
			WMV_Image m = images.get(i);
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
		
//		for (int i = 0; i < sounds.size(); i++)  		// Update and display videos
//		{
//			WMV_Sound s = sounds.get(i);
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
		if(p.p.debug.main)
			PApplet.println("Initializing field #"+fieldID);
		
		model.calculateFieldSize(); 		// Calculate bounds of photo GPS locations
		model.analyzeMedia();				// Analyze media locations and times 
		model.setup(); 						// Initialize field for first time 

		calculateMediaLocations(); 			// Set location of each photo in simulation
		detectMultipleFields();			// Run clustering on capture locations to detect multiple fields
		
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
	 * Run k-means clustering to search for multiple fields
	 */
	void detectMultipleFields()
	{
		ArrayList<WMV_Cluster> clusterList = new ArrayList<WMV_Cluster>();			// Clear current cluster list

		/* Estimate number of clusters */
		int numClusters = 12; 								// Estimate number of clusters 
		float minClusterDistance = 5000.f, maxClusterDistance = 25000.f;					
		float epsilon = p.kMeansClusteringEpsilon;
		int refinement = 60;
				
		/* K-means Clustering */
		//				initializeKMeansClusters(numClusters);		// Create initial clusters at random image locations	
		IntList addedImages = new IntList();			// Images already added to clusters; should include all images at end
		IntList nearImages = new IntList();			// Images nearby added media 

		IntList addedPanoramas = new IntList();		// Panoramas already added to clusters; should include all panoramas at end
		IntList nearPanoramas = new IntList();		// Panoramas nearby added media 

		IntList addedVideos = new IntList();			// Videos already added to clusters; should include all videos at end
		IntList nearVideos = new IntList();			// Videos nearby added media 

		for (int i = 0; i < numClusters; i++) 		// Iterate through the clusters
		{
			int imageID, panoramaID, videoID;			

			if(i == 0)			
			{
				long clusteringRandomSeed = (long) p.p.random(1000.f);
				p.p.randomSeed(clusteringRandomSeed);
				imageID = (int) p.p.random(images.size());  			// Random image ID for setting cluster's start location				
				panoramaID = (int) p.p.random(panoramas.size());  		// Random panorama ID for setting cluster's start location				
				videoID = (int) p.p.random(videos.size());  			// Random video ID for setting cluster's start location				
				addedImages.append(imageID);								
				addedPanoramas.append(panoramaID);								
				addedVideos.append(videoID);								

				/* Record media nearby added media*/
				for(WMV_Image img : images)						// Check for images near the picked one
				{
					float dist = img.getCaptureDistanceFrom(images.get(imageID).getCaptureLocation());  // Get distance
					if(dist < minClusterDistance)
						nearImages.append(img.getID());				// Record images nearby picked image
				}

				for(WMV_Panorama pano : panoramas)				// Check for panoramas near the picked one 
				{
					float dist = pano.getCaptureDistanceFrom(panoramas.get(panoramaID).getCaptureLocation());  // Get distance
					if(dist < minClusterDistance)
						nearPanoramas.append(pano.getID());			// Add to the list of nearby picked images
				}

				/* Create the first cluster */
				PVector clusterPoint = new PVector(0,0,0);
				if(images.size() > 0)
				{
					clusterPoint = new PVector(images.get(imageID).getCaptureLocation().x, images.get(imageID).getCaptureLocation().y, images.get(imageID).getCaptureLocation().z); // Choose random image location to start
					clusterList.add(new WMV_Cluster(this, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
					i++;
				}
				else if(panoramas.size() > 0)
				{
					clusterPoint = new PVector(panoramas.get(panoramaID).getCaptureLocation().x, panoramas.get(panoramaID).getCaptureLocation().y, panoramas.get(panoramaID).getCaptureLocation().z); // Choose random image location to start
					clusterList.add(new WMV_Cluster(this, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
					i++;
				}
			}
			else															// Find a random media (image, panorama or video) location for new cluster
			{
				int mediaID = (int) p.p.random(images.size() + panoramas.size() + videos.size());
				PVector clusterPoint = new PVector(0,0,0);

				if( mediaID < images.size() )				// If image, compare to already picked images
				{
					imageID = (int) p.p.random(images.size());  						
					while(addedImages.hasValue(imageID) && nearImages.hasValue(imageID))
						imageID = (int) p.p.random(images.size());  						

					addedImages.append(imageID);
					clusterPoint = new PVector(images.get(imageID).getCaptureLocation().x, images.get(imageID).getCaptureLocation().y, images.get(imageID).getCaptureLocation().z); // Choose random image location to start
				}
				else if( mediaID < images.size() + panoramas.size() )		// If panorama, compare to already picked panoramas
				{
					panoramaID = (int) p.p.random(panoramas.size());  						
					while(addedPanoramas.hasValue(panoramaID) && nearPanoramas.hasValue(panoramaID))
						panoramaID = (int) p.p.random(panoramas.size());  						

					addedPanoramas.append(panoramaID);

					clusterPoint = new PVector(panoramas.get(panoramaID).getCaptureLocation().x, panoramas.get(panoramaID).getCaptureLocation().y, panoramas.get(panoramaID).getCaptureLocation().z); // Choose random image location to start
				}
				else if( mediaID < images.size() + panoramas.size() + videos.size() )		// If video, compare to already picked videos
				{
					videoID = (int) p.p.random(videos.size());  						
					while(addedImages.hasValue(videoID) && nearImages.hasValue(videoID))
						videoID = (int) p.p.random(videos.size());  						

					addedVideos.append(videoID);

					clusterPoint = new PVector(videos.get(videoID).getCaptureLocation().x, videos.get(videoID).getCaptureLocation().y, videos.get(videoID).getCaptureLocation().z); // Choose random image location to start
				}

				clusterList.add(new WMV_Cluster(this, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
			}
		}	
		
		// Refine clusters 
		int count = 0;
		boolean moved = false;						// Has any cluster moved farther than epsilon?

		ArrayList<WMV_Cluster> last = clusterList;
		PApplet.println("--> Refining clusters...");

		while( count < refinement ) 							// Iterate to create the clusters
		{		
			for (int i = 0; i < images.size(); i++) 			// Find closest cluster for each image
				images.get(i).findAssociatedField(clusterList, maxClusterDistance);		// Set associated cluster
			for (int i = 0; i < panoramas.size(); i++) 		// Find closest cluster for each image
				panoramas.get(i).findAssociatedField(clusterList, maxClusterDistance);		// Set associated cluster
			for (int i = 0; i < panoramas.size(); i++) 		// Find closest cluster for each image
				panoramas.get(i).findAssociatedField(clusterList, maxClusterDistance);		// Set associated cluster
			for (int i = 0; i < clusterList.size(); i++) 		// Find closest cluster for each image
			{
				clusterList.get(i).create();					// Assign clusters
				PApplet.println(" clusterList.get(i).mediaPoints:"+clusterList.get(i).mediaPoints);
			}

			if(clusterList.size() == last.size())				// Check cluster movement
			{
				for(WMV_Cluster c : clusterList)
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
					PApplet.println(" Stopped refinement... no clusters moved farther than epsilon:"+epsilon);
					break;								// If all clusters moved less than epsilon, stop refinement
				}
			}
			else
			{
				PApplet.println(" New clusters found... will keep refining clusters... clusters.size():"+clusterList.size()+" last.size():"+last.size());
			}

			count++;
		}
		
//		int ct = 0;
//		for(WMV_Cluster c : clusterList)
//		{
//			PApplet.println("---->Cluster "+count+" images:"+c.images.size()+" panoramas:"+c.panoramas.size()+" videos:"+c.videos.size());
//			ct++;
//		}
			
		clusterList = mergeAdjacentClusters(clusterList, 2500.f);
		PApplet.println("Detected "+clusterList.size()+" fields...");
		
//		ct = 0;
//		for(WMV_Cluster c : clusterList)
//		{
//			PApplet.println("Cluster "+count+" images:"+c.images.size()+" panoramas:"+c.panoramas.size()+" videos:"+c.videos.size());
//			ct++;
//		}
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

//		for( PVector v : mostNeighbors ) 					// For clusters with most close neighbors, absorb neighbors into cluster
//		{
//			if(p.p.debug.cluster && v.y > 0 && p.p.debug.detailed)
//				PApplet.println("Merging cluster "+(int)v.x+" with "+(int)v.y+" neighbors...");
//
//			WMV_Cluster c = clusterList.get( (int)v.x );
//			if(!merged.hasValue(c.getID()))
//			{
//				for(int i : closeNeighbors[c.getID()])
//				{
//					if(!absorbed.hasValue(i) && c.getID() != i) 		// If cluster i hasn't already been absorbed and isn't the same cluster
//					{
//						c.absorbCluster(clusterList.get(i));				// Absorb cluster
//						absorbed.append(i);
//
//						merged.append(i);
//						merged.append(c.getID());
//						mergedClusters++;
//					}
//				}
//			}
//		}

		for( WMV_Cluster c : clusterList )					// Merge remaining clusters under minClusterDistance 
		{
//			PApplet.println("`"+c.images.size()+" id:"+c.getID());
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

		PApplet.println("Merged Clusters..."+mergedClusters);
		
//		absorbed.sort();
//		for(int i=absorbed.size()-1;i>=0;i--)
//			clusterList.remove(absorbed.get(i));
//
//		PApplet.println("Removed Clusters..."+absorbed.size());
		
		ArrayList<WMV_Cluster> newList = new ArrayList<WMV_Cluster>();
		
		for(WMV_Cluster c : clusterList)
		{
			if(!absorbed.hasValue(c.getID()))
			{
				newList.add(c);
			}
		}
		
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
		WMV_TimeSegment t = null;
		
		if(clusterID < clusters.size() && index < clusters.get(clusterID).getTimeline().size())
			t = clusters.get(clusterID).getTimeline().get(index);

		if(t == null)
			p.display.message("NULL time segment "+index+" returned by getTimeSegmentInCluster() clusterID:"+clusterID);
		
		if(clusterID != t.getClusterID())
			PApplet.println("ERROR... getTimeSegmentInCluster().. clusterID and timeSegment clusterID do not match!  clusterID:"+clusterID+" t.getClusterID():"+t.getClusterID());

		return t;
	}

	public WMV_Date getDateInCluster(int clusterID, int index)
	{
		WMV_Date d = clusters.get(clusterID).dateline.get(index);

		if(d == null)
			p.display.message("Couldn't get date "+index+" in cluster "+clusterID);
		
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
