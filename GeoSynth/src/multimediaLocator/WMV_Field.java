package multimediaLocator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;

import processing.core.PApplet;
import processing.core.PImage;
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

	/* Clusters */
	public int deepestLevel = -1;	
	public int defaultClusterDepth = 8;						// How deep in the dendrogram to look for media?	-- Set based on deepestLevel?
	public int minClusterDepth = 2;
	public int clusterDepth = defaultClusterDepth;			// How deep in the dendrogram to look for media?
	private IntList clustersByDepth;						// Number of clusters at each dendrogram depth
	
	/* Hierarchical Clustering */
	private Cluster dendrogramTop;							// Top cluster of the dendrogram
	public boolean dendrogramCreated = false;				// Dendrogram has been created
	private String[] names;									// Media names
	private double[][] distances;

	/* Data */
	private WMV_Model model;										// Dimensions and properties of current virtual space
	private ArrayList<WMV_Image> images; 					// All images in this field
	private ArrayList<WMV_Panorama> panoramas; 				// All panoramas in this field
	private ArrayList<WMV_Video> videos; 					// All videos in this field
	private ArrayList<WMV_Sound> sounds; 					// All videos in this field
	private ArrayList<WMV_Cluster> clusters;					// Spatial groupings of media in the Image3D and Video3D arrays

	private int imageErrors = 0, videoErrors = 0, panoramaErrors = 0;			// Metadata loading errors per media type
	private int indexPanoramaOffset, indexVideoOffset;		// Start of panoramas / videos in names and distances arrays

	/* Time */
//	public int frameCount = 0;
	private ArrayList<WMV_TimeSegment> timeline;						// List of time segments in this field ordered by time from 0:00 to 24:00 as a single day
	private ArrayList<ArrayList<WMV_TimeSegment>> timelines;			// Lists of time segments in field ordered by date
	private ArrayList<WMV_Date> dateline;								// List of dates in this field, whose indices correspond with timelines in timelines list
	private String timeZoneID = "America/Los_Angeles";					// Current time zone

	/* Utilities */
	WMV_Utilities utilities;					// Utility methods

//	WMV_World p;
	WMV_WorldSettings worldSettings;
	WMV_WorldState worldState;
	WMV_ViewerSettings viewerSettings;	// Update world settings
	WMV_ViewerState viewerState;	// Update world settings
	ML_DebugSettings debugSettings;	// Update world settings

	/* -- Debug -- */	
	private int disassociatedImages = 0;						// Images not associated with a cluster -- Still needed?
	private int disassociatedPanoramas = 0;
	private int disassociatedVideos = 0;

//	WMV_World parent, 
	WMV_Field( WMV_WorldSettings newWorldSettings, WMV_WorldState newWorldState, WMV_ViewerSettings newViewerSettings, WMV_ViewerState newViewerState, 
			   ML_DebugSettings newDebugSettings, String newMediaFolder, int newFieldID )
	{
//		p = parent;
		worldSettings = newWorldSettings;
		worldState = newWorldState;
		viewerSettings = newViewerSettings;
		viewerState = newViewerState;
		debugSettings = newDebugSettings;
		utilities = new WMV_Utilities();
		
		name = newMediaFolder;
		id = newFieldID;

		model = new WMV_Model(worldSettings, debugSettings);
		clusters = new ArrayList<WMV_Cluster>();
		clustersByDepth = new IntList();
		
		images = new ArrayList<WMV_Image>();
		panoramas = new ArrayList<WMV_Panorama>();
		videos = new ArrayList<WMV_Video>();		
		sounds = new ArrayList<WMV_Sound>();		

		timeline = new ArrayList<WMV_TimeSegment>();
		dateline = new ArrayList<WMV_Date>();
	}

	/**
	 * Initialize virtual space based on media GPS capture locations
	 */
	void setup()		 // Initialize field 
	{
		if (getImages().size() > 0 || getPanoramas().size() > 0 || getVideos().size() > 0)
		{
			float midLongitude = (model.highLongitude - model.lowLongitude) / 2.f;
			float midLatitude = (model.highLatitude - model.lowLatitude) / 2.f;

			if(debugSettings.field) PApplet.println("Initializing model for field #"+getID()+"...");

			/* Calculate number of valid media points */
			model.validImages = getImageCount();
			model.validPanoramas = getPanoramaCount();
			model.validVideos = getVideoCount();
			model.validMedia = model.validImages + model.validPanoramas + model.validVideos;

			if(model.validMedia > 1)
			{
				model.fieldWidth = utilities.gpsToMeters(midLatitude, model.highLongitude, midLatitude, model.lowLongitude);
				model.fieldLength = utilities.gpsToMeters(model.highLatitude, midLongitude, model.lowLatitude, midLongitude);
				model.fieldHeight = model.highAltitude - model.lowAltitude;					
			}
			else
			{
				model.fieldWidth = 1000.f;
				model.fieldLength = 1000.f;
				model.fieldHeight = 1000.f;
			}
			
			model.fieldArea = model.fieldWidth * model.fieldLength;				// Use volume instead?
			model.mediaDensity = model.validMedia / model.fieldArea;				// Media per sq. m.

			/* Increase maxClusterDistance as mediaDensity decreases */
			if(worldState.autoClusterDistances)
			{
				model.maxClusterDistance = worldSettings.maxClusterDistanceConstant / model.mediaDensity;
				if(model.maxClusterDistance > model.minClusterDistance * worldSettings.maxClusterDistanceFactor)
					model.maxClusterDistance = model.minClusterDistance * worldSettings.maxClusterDistanceFactor;
			}
			else
			{
				model.setMinClusterDistance(worldSettings.minClusterDistance); 				// Minimum distance between clusters, i.e. closer than which clusters are merged
				model.setMaxClusterDistance(worldSettings.maxClusterDistance);				// Maximum distance between clusters, i.e. farther than which single image clusters are created (set based on mediaDensity)
			}

			//			for(GMV_Image i : images)						
			//				i.maxClusterDistance = max;
			//			for(GMV_Panorama n : panoramas)				
			//				n.maxClusterDistance = max;
			//			for(GMV_Video v : videos)						
			//				v.maxClusterDistance = max;

			if(debugSettings.cluster)
				PApplet.println("------> Set maxClusterDistance:"+model.maxClusterDistance);

			if(model.highLongitude == -1000000 || model.lowLongitude == 1000000 || model.highLatitude == -1000000 || model.lowLatitude == 1000000)	// If field dimensions aren't initialized
			{
				model.lowLongitude = 1000.f;
				model.fieldLength = 1000.f;
				model.fieldHeight = 50.f;						// Altitude already in meters
			}

			if(getImageCount() == 1)
			{
				model.lowLongitude = 1000.f;
				model.fieldLength = 1000.f;
				model.fieldHeight = 50.f;						// Altitude already in meters
			}

			model.fieldAspectRatio = model.lowLongitude / model.fieldLength;

			if (debugSettings.model)
			{
				PApplet.print("Field Width:"+model.lowLongitude);
				PApplet.print(" Field Length:"+model.fieldLength);
				PApplet.println(" Field Height:"+model.fieldHeight);	
				PApplet.println("Field Area:"+model.fieldArea);

				PApplet.println("Media Density:"+model.mediaDensity);
				//				PApplet.println("Cluster Density:"+clusterDensity);
				//				PApplet.println("Old Number of Clusters:"+((int) (p.numImages * 1/24.f)));
			}
		}
		else 
		{
			if(debugSettings.field) 
			{
				PApplet.println("No images loaded! Couldn't initialize field...");
				PApplet.println("getPanoramas().size():"+getPanoramas().size());
			}
		}
	}

	public void display(WMV_World p) 				// Draw currently visible media
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
				
				m.updateSettings(worldSettings, worldState, viewerSettings, viewerState, debugSettings);
				if(!m.verticesAreNull() && (m.isFading() || m.fadingFocusDistance))
				{
					m.updateTimeBrightness(clusters.get(m.getCluster()), timeline, utilities);
					m.update(p.p, utilities);  		// Update geometry + visibility
				}

				if (distance < vanishingPoint && distance > viewerSettings.nearClippingDistance && !m.verticesAreNull()) 	// Visible	
				{
					if(!m.fadingFocusDistance && !m.isFading()) 
						m.update(p.p, utilities);  	// Update geometry + visibility
					
					m.draw(p); 		// Draw image
					imagesVisible++;
				}
			}
		}
		
		for (WMV_Panorama n : panoramas)  	// Update and display panoramas
		{
			if(!n.disabled)
			{
				float distance = n.getViewingDistance(); // Estimate image distance to camera based on capture location

				n.updateSettings(worldSettings, worldState, viewerSettings, viewerState, debugSettings);
				if(distance < vanishingPoint)			// Check if panorama is in visible range
				{
					n.updateTimeBrightness(clusters.get(n.getCluster()), timeline, utilities);
					n.update(p.p);  	// Update geometry + visibility
					n.draw(p); 		// Display panorama
					panoramasVisible++;
				}
				else if(n.isFading())
				{
					n.update(p.p);  	// Update geometry + visibility
				}
			}
		}

		for (WMV_Video v : videos)  		// Update and display videos
		{
			if(!v.disabled)
			{
				float distance = v.getViewingDistance();	 // Estimate video distance to camera based on capture location
				boolean nowVisible = (distance < vanishingPoint);

				v.updateSettings(worldSettings, worldState, viewerSettings, viewerState, debugSettings);
				if ( v.isVisible() && !nowVisible )
					v.fadeOut();
				
				if (nowVisible || v.isFading())
				{
					v.updateTimeBrightness(clusters.get(v.getCluster()), timeline, utilities);
					v.update(p.p, utilities);  	// Update geometry + visibility
					v.draw(p); 		// Display video
					videosVisible++;
				}
				else
				{
					if(v.isFading() || v.isFadingVolume())
						v.update(p.p, utilities);  	// Update geometry + visibility
					
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
//				s.updateSettings(worldSettings, viewerSettings, viewerState, debugSettings);
//				if ( s.isVisible() && !nowVisible )
//				{
////					s.fadeOut();
//				}
//				
//				if (nowVisible || s.isFading())
//				{
//					s.updateTimeBrightness(clusters.get(s.getCluster()), timeline);
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
	public boolean initialize(String library, boolean lockMediaToClusters)
	{
//		if(debugSettings.main) 
			PApplet.println("Initializing field #"+id);
		
		model.calculateFieldSize(images, panoramas, videos); 		// Calculate bounds of photo GPS locations
		model.analyzeMedia(images, panoramas, videos);				// Analyze media locations and times 
		setup(); 						// Initialize field for first time 

		boolean hierarchical = false;
//		worldState.hierarchical = false;		// -- Working?
		if(model.validMedia < 20)
			hierarchical = true;
//			worldState.hierarchical = true;		// -- Working?

		calculateMediaLocations(); 				// Set location of each photo in simulation
//		detectMultipleFields();					// Run clustering on capture locations to detect multiple fields

		// TESTING
//		divideField(3000.f, 15000.f);			
		
		findImagePlaceHolders();				// Find image place holders for videos
		calculateMediaVertices();				// Calculate all image vertices

//		if(debugSettings.main) 
			PApplet.println("Will run initial clustering for field #"+id+"...");
//		if(debugSettings.main) p.display.message("Will run initial clustering for field #"+id+"...");

		runInitialClustering(hierarchical);		// Find media clusters
//		model.findDuplicateClusterMedia();		// Find media in more than one cluster
		
		if(lockMediaToClusters)					// Center media capture locations at associated cluster locations
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

		return hierarchical;
	}
	
	/**
	 * Update field variables each frame
	 */
	public void update( WMV_WorldSettings currentWorldSettings, WMV_WorldState currentWorldState, WMV_ViewerSettings currentViewerSettings, 
						WMV_ViewerState currentViewerState)
	{
		worldSettings = currentWorldSettings;	// Update world settings
		worldState = currentWorldState;	// Update world settings
		viewerSettings = currentViewerSettings;	// Update viewer settings
		viewerState = currentViewerState;		// Update viewer state
		
		model.update( currentWorldSettings, currentWorldState, currentViewerSettings, currentViewerState );	// Update model
		
		for(WMV_Cluster c : clusters)	// Update clusters
			c.update(currentWorldSettings, currentWorldState, currentViewerSettings, currentViewerState);
	}

	public void analyzeClusterMediaDirections()
	{
		for(WMV_Cluster c : getClusters())
			if(!c.isEmpty())
				c.analyzeMediaDirections(images, videos);
	}
	
	/**
	 * Find image place holders for each video in field
	 */
	void findImagePlaceHolders()
	{
		for(WMV_Video v : videos)
			v.findPlaceholder(images);
	}

	/**
	 * Calculate location of each media file in virtual space from GPS, orientation metadata
	 */
	public void calculateMediaLocations() 
	{
		if(debugSettings.field) PApplet.println("Calculating image locations...");

		for (int i = 0; i < images.size(); i++)
			images.get(i).calculateCaptureLocation(model);
		for (int i = 0; i < panoramas.size(); i++)
			panoramas.get(i).calculateCaptureLocation(model);
		for (int i = 0; i < videos.size(); i++)
			videos.get(i).calculateCaptureLocation(model);
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
			c.create(images, panoramas, videos);
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
		
		if(mergeClusters) mergeAdjacentClusters();		// Merge clusters

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
				c.analyzeMedia(images, panoramas, videos);					// Analyze media in each cluster 
			}
		}

		for(WMV_Cluster c : clusters)
		{
			if(!c.isEmpty())
			{
				for(WMV_Image i : c.getImages(images))
				{
					i.setClusterTime(c);
					i.setClusterDate(c);
				}

				for(WMV_Panorama n : c.getPanoramas(panoramas))
				{
					n.setClusterTime(c);
					n.setClusterDate(c);
				}

				for(WMV_Video v : c.getVideos(videos))
				{
					v.setClusterTime(c);
					v.setClusterDate(c);
				}

				c.findMediaSegments(images, panoramas, videos);
			}
		}
	}
	

	 /**
	  * @param index New clusterID
	  * @param location Location
	  * @param images GMV_Image list
	  * @param panoramas GMV_Panorama list
	  * @param videos GMV_Video list
	  * @return New cluster with given media
	  */
	 public WMV_Cluster createCluster( int index, PVector location, IntList images, IntList panoramas, IntList videos )
	 {
		 WMV_Cluster gmvc = new WMV_Cluster(worldSettings, worldState, viewerSettings, debugSettings, index, location.x, location.y, location.z);

		 /* Add media to cluster */
		 for( int i : images )
		 {
			 gmvc.addImage(getImage(i));
			 gmvc.mediaCount++;
		 }
		 for( int n : panoramas )
		 {
			 gmvc.addPanorama(getPanoramas().get(n));
			 gmvc.mediaCount++;
		 }
		 for( int v : videos )
		 {
			 gmvc.addVideo(getVideo(v));
			 gmvc.mediaCount++;
		 }

		 /* Check whether the cluster is a single media cluster */
		 if( images.size() == 1 && panoramas.size() == 0 && videos.size() == 0 )
			 gmvc.setSingle(true);
		 if( images.size() == 1 && panoramas.size() == 0 && videos.size() == 0 )
			 gmvc.setSingle(true);
		 if( images.size() == 0 && panoramas.size() == 0 && videos.size() == 1 )
			 gmvc.setSingle(true);

		 return gmvc;
	 }

	/** 
	 * If any images are not associated, create a new cluster for each
	 */	void createSingleClusters()
	 {
		 int newClusterID = getClusters().size();	// Start adding clusters at end of current list 
		 int initial = newClusterID;

		 for (WMV_Image i : getImages()) 			// Find closest cluster for each image
		 {
			 if(i.cluster == -1)				// Create cluster for each single image
			 {
				 addCluster(new WMV_Cluster(worldSettings, worldState, viewerSettings, debugSettings, newClusterID, i.getCaptureLocation().x, i.getCaptureLocation().y, i.getCaptureLocation().z));
				 i.setAssociatedCluster(newClusterID);

				 getCluster(newClusterID).createSingle(i.getID(), 0);
				 newClusterID++;
			 }
		 }

		 for (WMV_Panorama n : getPanoramas()) 						// Find closest cluster for each image
		 {
			 if(n.cluster == -1)				// Create cluster for each single image
			 {
				 addCluster(new WMV_Cluster(worldSettings, worldState, viewerSettings, debugSettings, newClusterID, n.getCaptureLocation().x, n.getCaptureLocation().y, n.getCaptureLocation().z));
				 n.setAssociatedCluster(newClusterID);

				 getCluster(newClusterID).createSingle(n.getID(), 1);
				 newClusterID++;
			 }
		 }

		 for (WMV_Video v : getVideos()) 						// Find closest cluster for each image
		 {
			 if(v.cluster == -1)				// Create cluster for each single image
			 {
				 addCluster(new WMV_Cluster(worldSettings, worldState, viewerSettings, debugSettings, newClusterID, v.getCaptureLocation().x, v.getCaptureLocation().y, v.getCaptureLocation().z));
				 v.setAssociatedCluster(newClusterID);

				 getCluster(newClusterID).createSingle(v.getID(), 2);
				 newClusterID++;
			 }
		 }

		 if(debugSettings.cluster)
			 PApplet.println("Created "+(newClusterID-initial)+" clusters from single images...");
	 }

	 /**
	  * @param location New cluster location
	  * @param index New cluster ID
	  * @return New empty cluster
	  */
	 public WMV_Cluster createEmptyCluster( PVector location, int index )
	 {
		 if(location != null)
		 {
			WMV_Cluster gmvc = new WMV_Cluster(worldSettings, worldState, viewerSettings, debugSettings, index, location.x, location.y, location.z);
			 return gmvc;
		 }
		 return null;
	 }

	 /**
	  * Merge together clusters with closest neighbor below minClusterDistance threshold
	  */
	 void mergeAdjacentClusters()
	 {
		 model.mergedClusters = 0;			// Reset mergedClusters count

		 IntList[] closeNeighbors = new IntList[ getClusters().size() ];			// List array of closest neighbor distances for each cluster 
		 ArrayList<PVector> mostNeighbors = new ArrayList<PVector>();			// List of clusters with most neighbors and number of neighbors as PVector(id, neighborCount)
		 IntList absorbed = new IntList();										// List of clusters absorbed into other clusters
		 IntList merged = new IntList();											// List of clusters already merged with neighbors
		 float firstMergePct = 0.2f;												// Fraction of clusters with most neighbors to merge first

		 if((debugSettings.cluster || debugSettings.model ) && debugSettings.detailed) PApplet.println("Merging adjacent clusters... ");

		 for( WMV_Cluster c : getClusters() )					// Find distances of close neighbors to each cluster
		 {
			 closeNeighbors[c.getID()] = new IntList();	// Initialize list for this cluster
			 for( WMV_Cluster d : getClusters() )
			 {
				 float dist = PVector.dist(c.getLocation(), d.getLocation());			// Get distance between clusters
				 //				  PApplet.println("c.location:"+c.location+" d.location:"+d.location+" dist:"+dist+" min:"+minClusterDistance);

				 if(dist < model.minClusterDistance)								// If less than minimum distance
				 {
					 closeNeighbors[c.getID()].append(d.getID());		// Add d to closest clusters to c
				 }
			 }
		 }

		 int count = 0;
		 for( WMV_Cluster c : getClusters() )					// Find distances of close neighbors for each cluster
		 {
			 if(count < getClusters().size() * firstMergePct )		// Fill array with initial clusters 
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

		 for( PVector v : mostNeighbors ) 					// For clusters with most close neighbors, absorb neighbors into cluster
		 {
			 if(debugSettings.cluster && v.y > 0 && debugSettings.detailed)
				 PApplet.println("Merging cluster "+(int)v.x+" with "+(int)v.y+" neighbors...");

			 WMV_Cluster c = getCluster( (int)v.x );
			 if(!merged.hasValue(c.getID()))
			 {
				 for(int i : closeNeighbors[c.getID()])
				 {
					 if(!absorbed.hasValue(i) && c.getID() != i) 		// If cluster i hasn't already been absorbed and isn't the same cluster
					 {
						 c.absorbCluster(getCluster(i), images, panoramas, videos);				// Absorb cluster
						 absorbed.append(i);

						 merged.append(i);
						 merged.append(c.getID());
						 model.mergedClusters++;
					 }
				 }
			 }
		 }

		 for( WMV_Cluster c : getClusters() )					// Merge remaining clusters under minClusterDistance 
		 {
			 if(!merged.hasValue(c.getID()))
			 {
				 for( WMV_Cluster d : getClusters() )
				 {
					 if( !absorbed.hasValue(d.getID()) && !merged.hasValue(d.getID()) && c.getID() != d.getID() ) 	// If is different cluster and hasn't already been absorbed or merged
					 {
						 float dist = PVector.dist(c.getLocation(), d.getLocation());			// Get distance between clusters
						 if(dist < model.minClusterDistance)
						 {
							 c.absorbCluster(d, images, panoramas, videos);
							 absorbed.append(d.getID());

							 merged.append(c.getID());
							 merged.append(d.getID());
							 model.mergedClusters++;
						 }
					 }
				 }
			 }
		 }

		 if(debugSettings.cluster)
			 PApplet.println("Merged Clusters "+model.mergedClusters);
	 }


	 /**
	  * Remove empty clusters
	  * @param clusters Cluster list
	  * @return Cleaned up cluster list
	  */
	 public ArrayList<WMV_Cluster> cleanupClusters(ArrayList<WMV_Cluster> clusters)
	 {
		 ArrayList<WMV_Cluster> result = new ArrayList<WMV_Cluster>();
		 int count = 0;
		 int before = clusters.size();

		 for(WMV_Cluster c : clusters)
		 {
			 if(!c.isEmpty() && c.mediaCount > 0)
			 {
				 int oldID = c.getID();
				 c.setID(count);

				 for(WMV_Image i : getImages())
					 if(i.cluster == oldID)
						 i.setAssociatedCluster(count);
				 for(WMV_Panorama n : getPanoramas())
					 if(n.cluster == oldID)
						 n.setAssociatedCluster(count);
				 for(WMV_Video v : getVideos())
					 if(v.cluster == oldID)
						 v.setAssociatedCluster(count);
				 for(WMV_Sound s : getSounds())
					 if(s.cluster == oldID)
						 s.setAssociatedCluster(count);

				 for(WMV_TimeSegment t:c.getTimeline())
					 if(t.getClusterID() != count)
						 t.setClusterID(count);

				 for(ArrayList<WMV_TimeSegment> timeline:c.getTimelines())
					 for(WMV_TimeSegment t:timeline)
						 if(t.getClusterID() != count)
							 t.setClusterID(count);

				 result.add(c);
				 count ++;
			 }
		 }	

		 int removed = before - result.size();
		 if(debugSettings.model) PApplet.println("cleanupClusters()... Removed "+removed+" clusters from field #"+getID());

		 return result;
	 }

	 /** 
	  * Find video placeholder images, i.e. images taken just before a video to indicate same location, orientation, elevation and rotation angles
	  */	
	 public void findVideoPlaceholders()
	 {
		 for (int i = 0; i < getVideos().size(); i++) 		
		 {
			 WMV_Video v = getVideo(i);
			 if(!v.disabled)
			 {
				 int id = v.getImagePlaceholder();				// Find associated image with each video

				 if(id != -1)
				 {
					 v.cluster = getImage(id).cluster;	// Set video cluster to cluster of associated image
					 getCluster(v.cluster).video = true;	// Set cluster video property to true
					 if(debugSettings.video)
						 PApplet.println("Image placeholder for video: "+i+" is:"+id+" getCluster(v.cluster).video:"+getCluster(v.cluster).video);
				 }
				 else
				 {
					 if(debugSettings.video)
						 PApplet.println("No image placeholder found for video: "+i+" getCluster(v.cluster).video:"+getCluster(v.cluster).video);
					 v.disabled = true;
				 }
			 }
		 }
	 }

//	public void setBlurMasks()
//	{
//		for(WMV_Image image : getImages())
//		{
//			int bmID = image.blurMaskID;
//			switch(bmID)
//			{
//			case 0:
//				setImageBlurMask(image, p.blurMaskLeftTop);
//				break;
//			case 1:
//				setImageBlurMask(image, p.blurMaskLeftCenter);
//				break;
//			case 2:
//				setImageBlurMask(image, p.blurMaskLeftBottom);
//				break;
//			case 3:
//				setImageBlurMask(image, p.blurMaskLeftBoth);
//				break;
//			
//			case 4:
//				setImageBlurMask(image, p.blurMaskCenterTop);
//				break;
//			case 5:
//				setImageBlurMask(image, p.blurMaskCenterCenter);
//				break;
//			case 6:
//				setImageBlurMask(image, p.blurMaskCenterBottom);
//				break;
//			case 7:
//				setImageBlurMask(image, p.blurMaskCenterBoth);
//				break;
//		
//			case 8:
//				setImageBlurMask(image, p.blurMaskRightTop);
//				break;
//			case 9:
//				setImageBlurMask(image, p.blurMaskRightCenter);
//				break;
//			case 10:
//				setImageBlurMask(image, p.blurMaskRightBottom);
//				break;
//			case 11:
//				setImageBlurMask(image, p.blurMaskRightBoth);
//				break;
//		
//			case 12:
//				setImageBlurMask(image, p.blurMaskBothTop);
//				break;
//			case 13:
//				setImageBlurMask(image, p.blurMaskBothCenter);
//				break;
//			case 14:
//				setImageBlurMask(image, p.blurMaskBothBottom);
//				break;
//			case 15:
//				setImageBlurMask(image, p.blurMaskBothBoth);
//				break;
//			}
//		}
//	}
	
	void setImageBlurMask(WMV_Image image, PImage blurMask)
	{
		image.setBlurMask(blurMask);
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
		 for (WMV_Image i : getImages()) 
			 i.adjustCaptureLocation(getCluster(i.getCluster()));		
		 for (WMV_Panorama n : getPanoramas()) 
			 n.adjustCaptureLocation(getCluster(n.getCluster()));		
		 for (WMV_Video v : getVideos()) 
			 v.adjustCaptureLocation(getCluster(v.getCluster()));		
//		 for (WMV_Sound s : getSounds()) 
//			 s.adjustCaptureLocation(getCluster(s.getCluster()));		
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
	 * Create clusters for all media in field at startup	
	 */
	void runInitialClustering(boolean hierarchical) 					
	{
		if(debugSettings.cluster || debugSettings.model)
			PApplet.println("Running initial clustering for field: "+getName());

		clustersByDepth = new IntList();

//		/* Calculate number of valid media points */
//		model.validImages = getImageCount();
//		model.validPanoramas = getPanoramaCount();
//		model.validVideos = getVideoCount();
//		model.validMedia = model.validImages + model.validPanoramas + model.validVideos;				
//
//		if(model.validMedia < 20)
//			worldState.hierarchical = true;     fix
		
		if(hierarchical)						// If using hierarchical clustering
		{
			runHierarchicalClustering();			// Create dendrogram
			setDendrogramDepth( clusterDepth );		// Set initial dendrogram depth and initialize clusters
		}
		else										// If using k-means clustering
		{
			runKMeansClustering( worldSettings.kMeansClusteringEpsilon, model.clusterRefinement, model.clusterPopulationFactor );	// Get initial clusters using K-Means method
			setClusters( cleanupClusters( getClusters() ) );
		}

//		if(debugSettings.cluster || debugSettings.model)
//			p.p.p.display.message(p.p, "Created "+getClusterAmount()+" clusters...");
		
//		for(WMV_Cluster c : p.getClusters())
//		{
//			if(debugSettings.model && !c.isEmpty())
//				PApplet.println("Cluster #"+c.getID()+" has "+c.images.size()+" media points...");
//		}
	}

	/**
	 * Run k-means clustering on media in field to find capture locations
	 * @param epsilon Minimum cluster movement 
	 * @param refinement Number of iterations to refine clusters
	 * @param populationFactor Cluster population factor
	 */
	public void runKMeansClustering(float epsilon, int refinement, float populationFactor)
	{
		PApplet.println("Running Initial K Means clustering... epsilon:"+epsilon+" refinement:"+refinement+" populationFactor:"+populationFactor);
		setClusters( new ArrayList<WMV_Cluster>() );			// Clear current cluster list

		/* Display Status */
//		if(!display.initialSetup)
//		{
//			display.clearMessages();
//			display.message(p.p, "Running K-Means Clustering...");
//			display.message(p.p, " ");
//			display.message(p.p, "  Iterations:"+refinement);
//			display.message(p.p, "  Population Factor:"+populationFactor);
//			if(p.worldState.mergeClusters)
//			{
//				display.message(p.p, "");
//				display.message(p.p, "Cluster Merging:");
//				display.message(p.p, "  Minimum Cluster Distance:"+p.p.settings.minClusterDistance);
//				display.message(p.p, "  Maximum Cluster Distance:"+p.p.settings.maxClusterDistance);
//			}
//			display.message(p.p, " ");
//			display.displayClusteringInfo(p.p);
//		}
		
//		if(mediaDensity < XXX)			// ---Split into fields here

		/* Estimate number of clusters */
		int numClusters = PApplet.round( (1.f / PApplet.sqrt(model.mediaDensity)) * populationFactor ); 	// Calculate numClusters from media density

//		if(debugSettings.cluster && display.initialSetup)
//			PApplet.println("Creating "+numClusters+" initial clusters based on "+model.validMedia+" valid media...");
		
		/* K-means Clustering */
		if (model.validMedia > 1) 							// If there are more than 1 media point
		{
			PApplet.println("Running k-means clustering... model.validMedia:"+model.validMedia);
			initializeKMeansClusters(numClusters);		// Create initial clusters at random image locations	
			refineKMeansClusters(epsilon, refinement);	// Refine clusters over many iterations
			createSingleClusters();						// Create clusters for single media points
			
			initializeClusters(worldState.mergeClusters);						// Initialize clusters (merge, etc.)
			
//			setBlurMasks();
			
			if(getClusters().size() > 0)					// Calculate capture times for each cluster
				findVideoPlaceholders();
		}
		else PApplet.println("model.validMedia == "+model.validMedia);
//		else
//		{
//			if (getImages().size() == 0 && getPanoramas().size() == 0 && getVideos().size() == 0) 		// If there are 0 media
//			{
//				display.message(p.p, "No media loaded!  Can't run k-means clustering... Will exit.");
//				p.p.p.exit();
//			}
//			else
//			{
//				if(debugSettings.cluster)
//					display.message(p.p, "Single media point scene...");
//			}
//		}
		
//		if(!display.initialSetup)
//		{
//			display.message(p.p, " ");
//			display.message(p.p, "Created "+numClusters+" Clusters...");
//		}
	}
	
	/** 
	 * Create initial clusters at random image locations	 			-- Need to: record random seed, account for associated videos
	 */	
	void initializeKMeansClusters( int numClusters )
	{
		PApplet.println("initializeKMeansClusters...");
		Random rng = new Random(System.currentTimeMillis());
//		Random rng = new Random(clusteringRandomSeed);
		
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
				imageID = (int) (rng.nextFloat() * getImages().size());  			// Random image ID for setting cluster's start location				
				panoramaID = (int) (rng.nextFloat() * getPanoramas().size());  	// Random panorama ID for setting cluster's start location				
				videoID = (int) (rng.nextFloat() * getVideos().size());  			// Random video ID for setting cluster's start location		
				
				PApplet.println("rand imageID:"+imageID);
//				PApplet.println("rand panoramaID:"+panoramaID);
//				PApplet.println("rand videoID:"+videoID);
				
				addedImages.append(imageID);								
				addedPanoramas.append(panoramaID);								
				addedVideos.append(videoID);								

				/* Record media nearby added media*/
				for(WMV_Image img : getImages())						// Check for images near the picked one
				{
					float dist = img.getCaptureDistanceFrom(getImage(imageID).getCaptureLocation());  // Get distance
					if(dist < model.minClusterDistance)
						nearImages.append(img.getID());				// Record images nearby picked image
				}

				for(WMV_Panorama pano : getPanoramas())				// Check for panoramas near the picked one 
				{
					float dist = pano.getCaptureDistanceFrom(getPanoramas().get(panoramaID).getCaptureLocation());  // Get distance
					if(dist < model.minClusterDistance)
						nearPanoramas.append(pano.getID());			// Add to the list of nearby picked images
				}

				for(WMV_Video vid : getVideos())						// Check for videos near the picked one
				{
					float dist = vid.getCaptureDistanceFrom(getVideo(videoID).getCaptureLocation());  // Get distance
					if(dist < model.minClusterDistance)
						nearVideos.append(vid.getID());				// Add to the list of nearby picked images
				}

				/* Create the cluster */
				PVector clusterPoint = new PVector(0,0,0);
				if(getImages().size() > 0)
				{
					clusterPoint = new PVector(getImage(imageID).getCaptureLocation().x, getImage(imageID).getCaptureLocation().y, getImage(imageID).getCaptureLocation().z); // Choose random image location to start
					addCluster(new WMV_Cluster(worldSettings, worldState, viewerSettings, debugSettings, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
					i++;
				}
				if(getPanoramas().size() > 0)
				{
					clusterPoint = new PVector(getPanoramas().get(panoramaID).getCaptureLocation().x, getPanoramas().get(panoramaID).getCaptureLocation().y, getPanoramas().get(panoramaID).getCaptureLocation().z); // Choose random image location to start
					addCluster(new WMV_Cluster(worldSettings, worldState, viewerSettings, debugSettings, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
					i++;
				}
				if(getVideos().size() > 0)
				{
					clusterPoint = new PVector(getVideo(videoID).getCaptureLocation().x, getVideo(videoID).getCaptureLocation().y, getVideo(videoID).getCaptureLocation().z); // Choose random image location to start
					addCluster(new WMV_Cluster(worldSettings, worldState, viewerSettings, debugSettings, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
					i++;
				}

				if(i > 0)
					i--;
				else if(debugSettings.model)
					PApplet.println("Error in initClusters()... No media!!");
			}
			else															// Find a random media (image, panorama or video) location for new cluster
			{
				int mediaID = (int) (rng.nextFloat() * (getImages().size() + getPanoramas().size() + getVideos().size()));
				PVector clusterPoint = new PVector(0,0,0);

				if( mediaID < getImages().size() )				// If image, compare to already picked images
				{
					imageID = (int) (rng.nextFloat() * (getImages().size()));  		 			
					while(addedImages.hasValue(imageID) && nearImages.hasValue(imageID))
						imageID = (int) (rng.nextFloat() * (getImages().size()));  							

					addedImages.append(imageID);

					clusterPoint = new PVector(getImage(imageID).getCaptureLocation().x, getImage(imageID).getCaptureLocation().y, getImage(imageID).getCaptureLocation().z); // Choose random image location to start
				}
				else if( mediaID < getImages().size() + getPanoramas().size() )		// If panorama, compare to already picked panoramas
				{
					panoramaID = (int) (rng.nextFloat() * (getPanoramas().size()));  						
					while(addedPanoramas.hasValue(panoramaID) && nearPanoramas.hasValue(panoramaID))
						panoramaID = (int) (rng.nextFloat() * (getPanoramas().size()));  						

					addedPanoramas.append(panoramaID);

					clusterPoint = new PVector(getPanoramas().get(panoramaID).getCaptureLocation().x, getPanoramas().get(panoramaID).getCaptureLocation().y, getPanoramas().get(panoramaID).getCaptureLocation().z); // Choose random image location to start
				}
				else if( mediaID < getImages().size() + getPanoramas().size() + getVideos().size() )		// If video, compare to already picked videos
				{
					videoID = (int) (rng.nextFloat() * (getVideos().size()));  						
					while(addedImages.hasValue(videoID) && nearImages.hasValue(videoID))
						videoID = (int) (rng.nextFloat() * (getVideos().size()));  						

					addedVideos.append(videoID);

					clusterPoint = new PVector(getVideo(videoID).getCaptureLocation().x, getVideo(videoID).getCaptureLocation().y, getVideo(videoID).getCaptureLocation().z); // Choose random image location to start
				}

				addCluster(new WMV_Cluster(worldSettings, worldState, viewerSettings, debugSettings, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
			}
		}	
	}

	/**
	 * Refine clusters over given iterations
	 * @param epsilon Termination criterion, i.e. if all clusters moved less than epsilon after last iteration, stop refinement
	 * @param iterations Number of iterations
	 */	
	void refineKMeansClusters(float epsilon, int iterations)
	{
		int count = 0;
		boolean moved = false;						// Has any cluster moved farther than epsilon?
		
		ArrayList<WMV_Cluster> last = getClusters();
//		if(debugSettings.cluster || debugSettings.model)
			PApplet.println("--> Refining clusters... epsilon:"+epsilon+" iterations:"+iterations);
		
		while( count < iterations ) 							// Iterate to create the clusters
		{		
			for (int i = 0; i < getImages().size(); i++) 			// Find closest cluster for each image
				getImage(i).findAssociatedCluster(getClusters(), model.maxClusterDistance);		// Set associated cluster
			for (int i = 0; i < getPanoramas().size(); i++) 		// Find closest cluster for each image
				getPanorama(i).findAssociatedCluster(getClusters(), model.maxClusterDistance);		// Set associated cluster
			for (int i = 0; i < getVideos().size(); i++) 			// Find closest cluster for each image
				getVideo(i).findAssociatedCluster(getClusters(), model.maxClusterDistance);		// Set associated cluster
			for (int i = 0; i < getClusters().size(); i++) 		// Find closest cluster for each image
				getCluster(i).create(images, panoramas, videos);						// Assign clusters

			if(getClusters().size() == last.size())				// Check cluster movement
			{
				for(WMV_Cluster c : getClusters())
				{
					float closestDist = 10000.f;
					
					for(WMV_Cluster d : last)
					{
						float dist = c.getLocation().dist(d.getLocation());
						if(dist < closestDist)
							closestDist = dist;
					}
					
					if(closestDist > epsilon)
						moved = true;
				}
				
				if(!moved)
				{
//					if(debugSettings.cluster || debugSettings.model)
						PApplet.println(" Stopped refinement... no clusters moved farther than epsilon:"+epsilon);
					break;								// If all clusters moved less than epsilon, stop refinement
				}
			}
			else
			{
//				if(debugSettings.cluster || debugSettings.model)
					PApplet.println(" New clusters found... will keep refining clusters... clusters.size():"+getClusters().size()+" last.size():"+last.size());
			}
			
			count++;
		}
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
							c.absorbCluster(d, images, panoramas, videos);
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
	 * Build the dendrogram and calculate clusters at each depth
	 */
	public void runHierarchicalClustering()
	{
		PApplet.println("Running initial hierarchical clustering...");
		buildDendrogram();								// Build dendrogram 
		calculateClustersByDepth( dendrogramTop );		// Calculate number of clusters in dendrogram at each depth
		dendrogramCreated = true;
	}

	/**
	 * Calculate hieararchical clustering dendrogram for all media in field
	 */
	void buildDendrogram()
	{
		int namesIdx = 0, distIdx = 0;
		indexPanoramaOffset = getImages().size();
		indexVideoOffset = indexPanoramaOffset + getPanoramas().size();

		int size = getImages().size() + getVideos().size();
		names = new String[size];
		distances = new double[size][size];		

		PApplet.println("Creating dendrogram...");

		/* Calculate distances between each image and all other media */
		for(WMV_Image i : getImages())		
		{
			namesIdx = i.getID();
			names[namesIdx] = Integer.toString(namesIdx);

			for(WMV_Image j : getImages())
			{
				if(i != j)				// Don't compare image with itself
				{
					distIdx = j.getID();
					distances[namesIdx][distIdx] = PVector.dist(i.getCaptureLocation(), j.getCaptureLocation());
				}
			}

			for(WMV_Panorama n : getPanoramas())
			{
				distIdx = n.getID() + indexPanoramaOffset;
				distances[namesIdx][distIdx] = PVector.dist(i.getCaptureLocation(), n.getCaptureLocation());
			}

			for(WMV_Video v : getVideos())
			{
				distIdx = v.getID() + indexVideoOffset;
				distances[namesIdx][distIdx] = PVector.dist(i.getCaptureLocation(), v.getCaptureLocation());
			}
		}

		/* Calculate distances between each panorama and all other media */
		for(WMV_Panorama n : getPanoramas())		
		{
			namesIdx = n.getID() + indexPanoramaOffset;
			names[namesIdx] = Integer.toString(namesIdx);

			for(WMV_Image i : getImages())
			{
				distIdx = i.getID();
				distances[namesIdx][distIdx] = PVector.dist(n.getCaptureLocation(), i.getCaptureLocation());
			}

			for(WMV_Panorama o : getPanoramas())
			{
				if(n != o)				// Don't compare panorama with itself
				{
					distIdx = n.getID() + indexPanoramaOffset;
					distances[namesIdx][distIdx] = PVector.dist(n.getCaptureLocation(), n.getCaptureLocation());
				}
			}

			for(WMV_Video v : getVideos())
			{
				distIdx = v.getID() + indexVideoOffset;
				distances[namesIdx][distIdx] = PVector.dist(n.getCaptureLocation(), v.getCaptureLocation());
			}
		}

		/* Calculate distances between each video and all other media */
		for(WMV_Video v : getVideos())		
		{
			namesIdx = v.getID() + indexVideoOffset;
			names[namesIdx] = Integer.toString(namesIdx);

			for(WMV_Image i : getImages())
			{
				distIdx = i.getID();
				distances[namesIdx][distIdx] = PVector.dist(v.getCaptureLocation(), i.getCaptureLocation());
			}

			for(WMV_Panorama n : getPanoramas())
			{
				distIdx = n.getID() + indexPanoramaOffset;
				distances[namesIdx][distIdx] = PVector.dist(v.getCaptureLocation(), n.getCaptureLocation());
			}

			for(WMV_Video u : getVideos())
			{
				if(v != u)				// Don't compare video with itself
				{
					distIdx = u.getID() + indexVideoOffset;
					distances[namesIdx][distIdx] = PVector.dist(v.getCaptureLocation(), u.getCaptureLocation());
				}
			}
		}

		boolean error = false;
		for( int i = 0; i<size; i++)
		{
			for( int j = 0; i<size; i++)
			{
				double d = distances[i][j];
				if(utilities.isNaN(d))
				{
					PApplet.println("Not a number:"+d);
					error = true;
				}
			}
		}
		for( int i = 0; i<size; i++)
		{
			String s = names[i];
			if(s == null)
			{
				PApplet.println("String is null:"+s);
				error = true;
			}
		}

		if(!error)
		{
			try {
				ClusteringAlgorithm clusteringAlgorithm = new DefaultClusteringAlgorithm();
				PApplet.println("Performing hierarchical clustering...");
				dendrogramTop = clusteringAlgorithm.performClustering(distances, names, new AverageLinkageStrategy());
			}
			catch(Throwable t)
			{
				PApplet.println("Error while performing clustering... "+t);
			}
		}
	}

	/**
	 * @param depth Dendrogram depth level
	 * @return clusters GMV_Cluster list at given depth level based on dendrogram
	 */
	ArrayList<WMV_Cluster> createClustersFromDendrogram( int depth )
	{
		ArrayList<WMV_Cluster> gmvClusters = new ArrayList<WMV_Cluster>();	// GMV_Cluster list
		ArrayList<Cluster> clusters = new ArrayList<Cluster>();
		
		int imageCount = 0;
		int panoramaCount = 0;
		int videoCount = 0;

		if(debugSettings.cluster)
			PApplet.println("--- Getting GMV_Clusters at depth "+depth+" ---");

		if(dendrogramTop != null)
		{
			for(int d=0; d<depth; d++)
			{
				ArrayList<Cluster> dClusters = model.getDendrogramClusters( dendrogramTop, d );	// Get clusters in dendrogram up to given depth
				for(Cluster c : dClusters)
					clusters.add(c);
			}
		}
		else
		{
			if(debugSettings.cluster)
				PApplet.println("Top cluster is null!");
//			p.p.p.exit();
		}

		for( Cluster cluster : clusters )				// For each cluster at given depth
		{
			String name = cluster.getName();			// Otherwise, save the result if appropriate
			int mediaIdx = -1;

			String[] parts = name.split("#");			// Assume name in format "clstr#XX"
			boolean isMedia = false;

			IntList images = new IntList();
			IntList panoramas = new IntList();
			IntList videos = new IntList();

			if ( parts.length == 1 )					// If '#' isn't in the name, must be a media file
			{
				if(!utilities.isInteger(parts[0], 10))
				{
					if(debugSettings.cluster)
						PApplet.println("Media name error! "+name);
				}
				else isMedia = true;
			}
			else if( parts.length == 2 )				
			{
				if(!utilities.isInteger(parts[1], 10))
				{
					if(debugSettings.cluster)
						PApplet.println("Cluster name error! "+name);
				}
			}
			else
			{
				if(debugSettings.cluster)
					PApplet.println("Media or cluster name error! "+name);
			}

			PVector location;

			if(isMedia)
			{
				if(debugSettings.cluster && debugSettings.detailed)
					PApplet.println("Cluster "+cluster.getName()+" is a media file..."+name);

				mediaIdx = Integer.parseInt(name);

				if( mediaIdx < indexPanoramaOffset )
				{
					WMV_Image i = getImage(mediaIdx);
					location = i.getCaptureLocation();
					images.append(i.getID());
				}
				else if( mediaIdx < indexVideoOffset )
				{
					WMV_Panorama n = getPanorama(mediaIdx - indexPanoramaOffset);
					location = n.getCaptureLocation();
					panoramas.append(n.getID());
				}
				else
				{
					WMV_Video v = getVideo(mediaIdx - indexVideoOffset);
					location = v.getCaptureLocation();
					videos.append(v.getID());
				}
			}
			else
			{
				ArrayList<PVector> mediaPoints = new ArrayList<PVector>();

				images = getMediaInCluster(cluster, 0);
				panoramas = getMediaInCluster(cluster, 1);
				videos = getMediaInCluster(cluster, 2);

				if(images.size() > 1)
				{
					for(int i : images)
						mediaPoints.add(getImage(i).getCaptureLocation());
				}
				if(panoramas.size() > 1)
				{
					for(int i : images)
						mediaPoints.add(getImage(i).getCaptureLocation());
				}
				if(videos.size() > 1)
				{
					for(int v : videos)
						mediaPoints.add(getVideo(v).getCaptureLocation());
				}

				location = model.calculateAveragePoint(mediaPoints);					// Calculate cluster location from average of media points

				if(debugSettings.cluster && debugSettings.detailed)
					PApplet.println("Calculated Average Point: "+location);
			}

			imageCount += images.size();
			panoramaCount += panoramas.size();
			videoCount += videos.size();

			gmvClusters.add( createCluster( gmvClusters.size(), location, images, panoramas, videos ) );
		}

		PApplet.println("Got "+gmvClusters.size()+" clusters at depth "+depth+" from "+imageCount+" images, "+panoramaCount+" panoramas and "+videoCount+ "videos...");
		return gmvClusters;
	}
	
	/**
	 * @param depth New cluster depth
	 */
	void setDendrogramDepth(int depth)
	{
		clusterDepth = depth;
		setClusters( createClustersFromDendrogram( depth ) );	// Get clusters at defaultClusterDepth	 -- Set this based on media density

		for (int i = 0; i < getImages().size(); i++) 			// Find closest cluster for each image
			getImage(i).findAssociatedCluster(getClusters(), model.maxClusterDistance);
		for (int i = 0; i < getPanoramas().size(); i++) 			// Find closest cluster for each image
			getPanorama(i).findAssociatedCluster(getClusters(), model.maxClusterDistance);
		for (int i = 0; i < getVideos().size(); i++) 			// Find closest cluster for each video
			getVideo(i).findAssociatedCluster(getClusters(), model.maxClusterDistance);

		if(getClusters().size() > 0)							// Find image place holders
			findVideoPlaceholders();

//		if(!display.initialSetup)
//		{
//			/* Display Status */
//			display.clearMessages();
//			display.message(worldState, "Hierarchical Clustering Mode");
//			display.message(worldState, " ");
//			display.message(worldState, "Cluster Depth:"+clusterDepth);
//			display.message(worldState, " ");
//			display.displayClusteringInfo(worldState);
//			display.message(worldState, "Found "+getClusters().size()+" clusters...");
//		}
		
		initializeClusters(worldState.mergeClusters);					// Initialize clusters in Hierarchical Clustering Mode	 (Already done during k-means clustering)
	}

	/**
	 * @param top Dendrogram cluster to find associated media in
	 * @param mediaType Media type, 0: image 1: panorama 2: video
	 * @return List of cluster names of associated media 
	 */
	public IntList getMediaInCluster( Cluster top, int mediaType )
	{
		ArrayList<Cluster> clusterList = (ArrayList<Cluster>) top.getChildren();	// Get clusters in topCluster
		int mediaCount = 0;														// Number of images successfully found
		int depthCount = 0;														// Depth reached from top cluster
		IntList result = new IntList();

		if((clusterList.size() == 0) || clusterList == null)							// If the top cluster has no children, it is a media file														
		{
			String name = top.getName();										// Otherwise, save the result if appropriate
			int mediaIdx = Integer.parseInt(name);

			if(debugSettings.cluster)
				PApplet.println("No children in cluster "+name+" ... Already a media file!");

			if(mediaIdx < indexPanoramaOffset)
			{
				if(mediaType == 0)
				{
					result.append(mediaIdx);
					mediaCount++;
				}
			}
			else if(mediaIdx < indexVideoOffset)
			{
				if(mediaType == 1)
				{
					result.append(mediaIdx - indexPanoramaOffset);
					mediaCount++;
				}
			}
			else
			{
				if(mediaType == 2)
				{
					result.append(mediaIdx - indexVideoOffset);
					mediaCount++;
				}
			}
		}
		else if(clusterList.size() > 0)											 // Otherwise, it is a cluster of media files
		{
			boolean deepest = false;
			depthCount++;														// Move to next dendrogram level

			if(debugSettings.cluster && debugSettings.detailed)
				PApplet.println("Searching for media in cluster: "+top.getName()+"...");

			while(!deepest)														// Until the deepest level
			{
				ArrayList<Cluster> children = new ArrayList<Cluster>();			// List of children
				ArrayList<Cluster> nextDepth = new ArrayList<Cluster>();		// List of clusters at next depth level

				for( Cluster cluster : clusterList )								
				{
					children = (ArrayList<Cluster>) cluster.getChildren();		// For each cluster, look for its children 
					if(children.size() > 0)										// If there are children
					{
						for( Cluster c : children )								// Add to nextDepth clusters
							nextDepth.add(c);

						if(debugSettings.cluster && debugSettings.detailed)
						{
							PApplet.print("  Cluster "+cluster.getName()+" has "+children.size()+" children at depth "+depthCount);
							PApplet.println("  Added to next depth, array size:"+nextDepth.size()+"...");
						}
					}

					if(children.size() == 0 || (children == null))															
					{
						String name = cluster.getName();								// Otherwise, save the result if appropriate
						int mediaIdx = Integer.parseInt(name);

						if(mediaIdx < indexPanoramaOffset)
						{
							if(mediaType == 0)
							{
								result.append(mediaIdx);
								mediaCount++;
							}
						}
						else if(mediaIdx < indexVideoOffset)
						{
							if(mediaType == 1)
							{
								result.append(mediaIdx - indexPanoramaOffset);
								mediaCount++;
							}
						}
						else
						{
							if(mediaType == 2)
							{
								result.append(mediaIdx - indexVideoOffset);
								mediaCount++;
							}
						}
					}
				}

				deepest = !( children.size() > 0 );	// At deepest level when current list is empty

				if(!deepest)
				{
					clusterList = children;						// Move down one depth level 
					depthCount++;
				}
			}
		}

		if(debugSettings.cluster && debugSettings.detailed && mediaCount > 0)
			PApplet.println( "Found "+mediaCount+" media at depth "+depthCount+" result.size():"+result.size() );

		return result;
	}

	/**
	 * @param topCluster Top cluster of dendrogram
	 * Add to clustersByDepth list all dendrogram clusters at given depth 
	 */
	public void calculateClustersByDepth( Cluster topCluster )
	{
		ArrayList<Cluster> clusters = (ArrayList<Cluster>) topCluster.getChildren();	// Dendrogram clusters
		int depthCount = 0;

		if(debugSettings.cluster) PApplet.println("Counting clusters at all depth levels...");
		clustersByDepth.append(1);					// Add top cluster to clustersByDepth list

		if(clusters.size() > 0)
		{
			boolean deepest = false;
			depthCount++;															// Otherwise, we have moved deeper by one generation

			while(!deepest)														// Until the deepest level
			{
				ArrayList<Cluster> children = new ArrayList<Cluster>();			// List of children
				ArrayList<Cluster> nextDepth = new ArrayList<Cluster>();			// List of clusters at next depth level

				for( Cluster cluster : clusters )								
				{
					children = (ArrayList<Cluster>) cluster.getChildren();		// For each cluster, look for its children 

					if(children.size() > 0)										// If children exist
					{
						for( Cluster c : children )								// Add them to nextDepth clusters
							nextDepth.add(c);
					}
				}

				clustersByDepth.append(clusters.size());							// Record cluster number at depthCount

				if(debugSettings.cluster && debugSettings.detailed) PApplet.println("Found "+clusters.size()+" clusters at depth:"+depthCount);

				deepest = !( children.size() > 0 );								// At deepest level when list of chidren is empty

				if(!deepest)
				{
					clusters = children;											// Move down one depth level 
					depthCount++;
				}
			}
		}

		deepestLevel = depthCount;
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
	public void stitchAllClusters(ML_Stitcher stitcher, String libraryFolder)
	{
		for(WMV_Cluster c : clusters)
			c.stitchImages(stitcher, libraryFolder, getSelectedImages());
	}
	
	/**
	 * Detect and return multiple fields via k-means clustering 
	 * @param f Field to divide
	 * @return List of created fields
	 */
	ArrayList<WMV_Field> divideField(WMV_World world, float minFieldDistance, float maxFieldDistance)
	{
		ArrayList<WMV_Cluster> fieldClusters = new ArrayList<WMV_Cluster>();			// Clear current cluster list

		/* Estimate number of clusters */
		int numFields = 10; 								// Estimate number of clusters 
		float epsilon = worldSettings.kMeansClusteringEpsilon;
		int refinement = 60;
				
		/* K-means Clustering */
		IntList addedImages = new IntList();		// Images already added to clusters; should include all images at end
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
				long clusteringRandomSeed = (long) world.p.random(1000.f);
				world.p.randomSeed(clusteringRandomSeed);
				imageID = (int) world.p.random(getImages().size());  			// Random image ID for setting cluster's start location				
				panoramaID = (int) world.p.random(getPanoramas().size());  		// Random panorama ID for setting cluster's start location				
				videoID = (int) world.p.random(getVideos().size());  			// Random video ID for setting cluster's start location				
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
					fieldClusters.add(new WMV_Cluster(worldSettings, worldState, viewerSettings, debugSettings, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
					i++;
				}
				else if(getPanoramas().size() > 0)
				{
					PVector panoLoc = getPanorama(panoramaID).getCaptureLocation();
					clusterPoint = new PVector(panoLoc.x, panoLoc.y, panoLoc.z); // Choose random panorama location to start
					fieldClusters.add(new WMV_Cluster(worldSettings, worldState, viewerSettings, debugSettings, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
					i++;
				}
				else if(getVideos().size() > 0)
				{
					PVector vidLoc = getVideo(videoID).getCaptureLocation();
					clusterPoint = new PVector(vidLoc.x, vidLoc.y, vidLoc.z); // Choose random video location to start
					fieldClusters.add(new WMV_Cluster(worldSettings, worldState, viewerSettings, debugSettings, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
					i++;
				}
			}
			else											// Find a random media (image, panorama or video) location for new cluster
			{
				int mediaID = (int) world.p.random(getImages().size() + getPanoramas().size() + getVideos().size());
				PVector clusterPoint = new PVector(0,0,0);

				if( mediaID < getImages().size() )				// If image, compare to already picked images
				{
					imageID = (int) world.p.random(getImages().size());  						
					while(addedImages.hasValue(imageID) && nearImages.hasValue(imageID))
						imageID = (int) world.p.random(getImages().size());  						

					addedImages.append(imageID);
					
					PVector imgLoc = getImage(imageID).getCaptureLocation();
					clusterPoint = new PVector(imgLoc.x, imgLoc.y, imgLoc.z); // Choose random image location to start
				}
				else if( mediaID < getImages().size() + getPanoramas().size() )		// If panorama, compare to already picked panoramas
				{
					panoramaID = (int) world.p.random(getPanoramas().size());  						
					while(addedPanoramas.hasValue(panoramaID) && nearPanoramas.hasValue(panoramaID))
						panoramaID = (int) world.p.random(getPanoramas().size());  						

					addedPanoramas.append(panoramaID);
					
					PVector panoLoc = getPanorama(panoramaID).getCaptureLocation();
					clusterPoint = new PVector(panoLoc.x, panoLoc.y, panoLoc.z); // Choose random image location to start
				}
				else if( mediaID < getImages().size() + getPanoramas().size() + getVideos().size() )		// If video, compare to already picked videos
				{
					videoID = (int) world.p.random(getVideos().size());  						
					while(addedImages.hasValue(videoID) && nearImages.hasValue(videoID))
						videoID = (int) world.p.random(getVideos().size());  						

					addedVideos.append(videoID);
					
					PVector vidLoc = getVideo(videoID).getCaptureLocation();
					clusterPoint = new PVector(vidLoc.x, vidLoc.y, vidLoc.z); // Choose random image location to start
				}

				fieldClusters.add(new WMV_Cluster(worldSettings, worldState, viewerSettings, debugSettings, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
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
				fieldClusters.get(i).create(images, panoramas, videos);					// Assign clusters

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
	public ArrayList<WMV_Image> getSelectedImages()
	{
		ArrayList<WMV_Image> selected = new ArrayList<WMV_Image>();

		for(WMV_Image i : images)
			if(i.isSelected())
				selected.add(i);
		
		return selected;
	}

	/**
	 * @return List of IDs of currently selected images
	 */
	public IntList getSelectedImageIDs()
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
	
	/**  
	 * Export statistics on current field -- in progress
	 */
	public void exportFieldInfo()
	{
		BufferedWriter writer = null;

		try {
			// Create temp file
			String timeLog = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
			File logFile = new File(timeLog);

			// This will output the full path where the file will be written to...
			System.out.println(logFile.getCanonicalPath());

			writer = new BufferedWriter(new FileWriter(logFile));
			writer.write("Field: "+getName());
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				// Close the writer regardless of what happens...
				writer.close();
			} 
			catch (Exception e) {
			}
		}
	}

//	/**
//	 * @param depth Depth at which to draw clusters
//	 * Draw the clusters at given depth
//	 */
//	void drawClustersAtDepth( Cluster topCluster, int depth )
//	{		 
//		PApplet.println("Drawing clusters at depth level:"+depth);
//		ArrayList<Cluster> clusters = (ArrayList<Cluster>) topCluster.getChildren();	// Dendrogram clusters
//
//		for( int i = 0; i<depth; i++ )								// For each level up to given depth
//		{
//			ArrayList<Cluster> nextDepth = new ArrayList<Cluster>();	// List of clusters at this depth
//
//			for( Cluster c : clusters )								// For all clusters at current depth
//			{
//				ArrayList<Cluster> children = (ArrayList<Cluster>) c.getChildren();
//				for(Cluster d : children)								// Save children to nextDepth list
//					nextDepth.add(d);										
//			}
//
//			clusters = nextDepth;										// Move to next depth
//		}	
//
//		for( Cluster c : clusters )
//		{
//			String name = c.getName();								// Otherwise, save the result if appropriate
//			int mediaIdx = Integer.parseInt(name);
//			PVector location;
//
//			if(mediaIdx < indexPanoramaOffset)
//			{
//				location = p.getImage(mediaIdx).getCaptureLocation();
//			}
//			else if(mediaIdx < indexVideoOffset)
//			{
//				mediaIdx -= indexPanoramaOffset; 
//				location = p.getPanoramas().get(mediaIdx).getCaptureLocation();
//			}
//			else
//			{
//				mediaIdx -= indexVideoOffset; 
//				location = p.videos.get(mediaIdx).getCaptureLocation();
//			}
//
//			p.p.p.display.message("Drawing point:");
//			p.p.p.display.drawMapPoint(location, 20.f, p.p.p.display.largeMapWidth, p.p.p.display.largeMapHeight, p.p.p.display.mapClusterHue, 255, 255, p.p.p.display.mapClusterHue);
//		}
//		if(debugSettings.cluster) 
//			PApplet.println("Getting "+clusters.size()+" dendrogram clusters at depth:"+depth);
//	}
}
