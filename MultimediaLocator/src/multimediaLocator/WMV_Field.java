package multimediaLocator;

//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.text.SimpleDateFormat;
import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
//import java.util.Iterator;
//import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.data.IntList;
import processing.video.Movie;

/**************************************************
 * Multimedia environment of a particular geographical area
 * @author davidgordon
 */
public class WMV_Field 
{
	/* Classes */
	private WMV_WorldSettings worldSettings;	// World settings
	private WMV_WorldState worldState;			// World state
	private WMV_ViewerSettings viewerSettings;	// Viewer settings
	private WMV_ViewerState viewerState;		// Viewer state
	private ML_DebugSettings debugSettings;		// Debug settings
	private WMV_FieldState state;				// Field state
	private WMV_Utilities utilities;			// Utility methods
	private WMV_Model model;					// Dimensions of current virtual space

	/* Model */
	public ArrayList<PVector> border;					// Convex hull (border) of media points
	public boolean calculatedBorderPoints = false;

	/* Time */
	private WMV_Timeline timeline;						// List of time segments in this field ordered by time from 0:00 to 24:00 as a single day
	private ArrayList<WMV_Timeline> timelines;			// Lists of time segments in field ordered by date
	private ArrayList<WMV_Date> dateline;								// List of dates in this field, whose indices correspond with timelines in timelines list
	
	/* Data */
	private ArrayList<WMV_Cluster> clusters;			// Clusters (spatial groupings) of media 
	private ArrayList<WMV_Image> images; 				// All images in this field
	private ArrayList<WMV_Panorama> panoramas; 			// All panoramas in this field
	private ArrayList<WMV_Video> videos; 				// All videos in this field
	private ArrayList<WMV_Sound> sounds; 				// All videos in this field

	/* Hierarchical Clustering */
	private Cluster dendrogramTop;						// Top cluster of the dendrogram
	private String[] names;								// Media names
	private double[][] distances;						// Media distances

	
	WMV_Field( WMV_WorldSettings newWorldSettings, WMV_WorldState newWorldState, WMV_ViewerSettings newViewerSettings, WMV_ViewerState newViewerState, 
			   ML_DebugSettings newDebugSettings, String newMediaFolder, int newFieldID )
	{
		worldSettings = newWorldSettings;
		debugSettings = newDebugSettings;
		if(newWorldState != null) worldState = newWorldState;
		if(newViewerSettings != null) viewerSettings = newViewerSettings;
		if(newViewerState != null) viewerState = newViewerState;
		
		model = new WMV_Model();
		model.initialize(worldSettings, debugSettings);
		
		state = new WMV_FieldState();

		utilities = new WMV_Utilities();
		
		state.name = newMediaFolder;
		state.id = newFieldID;

		state.clustersByDepth = new ArrayList<Integer>();
		
		clusters = new ArrayList<WMV_Cluster>();
		images = new ArrayList<WMV_Image>();
		panoramas = new ArrayList<WMV_Panorama>();
		videos = new ArrayList<WMV_Video>();		
		sounds = new ArrayList<WMV_Sound>();		

		timeline = new WMV_Timeline();
		timeline.initialize(null);
		
		dateline = new ArrayList<WMV_Date>();
	}

	/**
	 * Setup virtual space based on media capture locations
	 */
	void setupModel()		
	{
		if (images.size() > 0 || panoramas.size() > 0 || videos.size() > 0)
		{
			model.calculateFieldSize(images, panoramas, videos); 		// Calculate bounds of photo GPS locations
			model.analyzeMedia(images, panoramas, videos);				// Analyze media locations and times 
			
			float midLongitude = (model.getState().highLongitude - model.getState().lowLongitude) / 2.f;
			float midLatitude = (model.getState().highLatitude - model.getState().lowLatitude) / 2.f;

			if(debugSettings.field) System.out.println("Initializing model for field #"+getID()+"...");

			/* Calculate number of valid media points */
			model.state.validImages = getImageCount();
			model.state.validPanoramas = getPanoramaCount();
			model.state.validVideos = getVideoCount();
			model.state.validMedia = model.state.validImages + model.state.validPanoramas + model.state.validVideos;

			if(model.state.validMedia > 1)
			{
				model.state.fieldWidth = utilities.gpsToMeters(midLatitude, model.getState().highLongitude, midLatitude, model.getState().lowLongitude);
				model.state.fieldLength = utilities.gpsToMeters(model.getState().highLatitude, midLongitude, model.getState().lowLatitude, midLongitude);
				model.state.fieldHeight = model.getState().highAltitude - model.getState().lowAltitude;					
			}
			else
			{
				model.state.fieldWidth = 1000.f;
				model.state.fieldLength = 1000.f;
				model.state.fieldHeight = 1000.f;
			}
			
			model.state.fieldArea = model.getState().fieldWidth * model.getState().fieldLength;				// Use volume instead?
			model.state.mediaDensity = model.getState().validMedia / model.getState().fieldArea;				// Media per sq. m.

			if(worldState.autoClusterDistances)			/* Increase maxClusterDistance as mediaDensity decreases */
			{
				model.state.maxClusterDistance = worldSettings.maxClusterDistanceConstant / model.getState().mediaDensity;
				if(model.state.maxClusterDistance > model.getState().minClusterDistance * worldSettings.maxClusterDistanceFactor)
					model.state.maxClusterDistance = model.getState().minClusterDistance * worldSettings.maxClusterDistanceFactor;
			}
			else
			{
				model.setMinClusterDistance(worldSettings.minClusterDistance); 				// Minimum distance between clusters, i.e. closer than which clusters are merged
				model.setMaxClusterDistance(worldSettings.maxClusterDistance);				// Maximum distance between clusters, i.e. farther than which single image clusters are created (set based on mediaDensity)
				if(debugSettings.cluster) System.out.println("autoClusterDistances... Set maxClusterDistance:"+model.getState().maxClusterDistance);
			}

			if(model.getState().highLongitude == -1000000 || model.getState().lowLongitude == 1000000 || model.getState().highLatitude == -1000000
				|| model.getState().lowLatitude == 1000000)			// If field dimensions aren't initialized
			{
				model.state.lowLongitude = 1000.f;
				model.state.fieldLength = 1000.f;
				model.state.fieldHeight = 50.f;						// Altitude already in meters
			}

			if(getImageCount() == 1)
			{
				model.state.lowLongitude = 1000.f;
				model.state.fieldLength = 1000.f;
				model.state.fieldHeight = 50.f;						// Altitude already in meters
			}

			model.state.fieldAspectRatio = model.getState().fieldWidth / model.getState().fieldLength;

			if (debugSettings.field)
			{
				System.out.print("Field Width:"+model.getState().lowLongitude);
				System.out.print(" Field Length:"+model.getState().fieldLength);
				System.out.println(" Field Height:"+model.getState().fieldHeight);	
				System.out.println("Field Area:"+model.getState().fieldArea);

				System.out.println("Media Density:"+model.getState().mediaDensity);
			}
		}
		else 
		{
			if(debugSettings.field) 
			{
				System.out.println("No images loaded! Couldn't initialize field...");
				System.out.println("panoramas.size():"+panoramas.size());
			}
		}
	}

	public void display(MultimediaLocator ml) 				// Draw currently visible media
	{
		float vanishingPoint = viewerSettings.farViewingDistance + worldSettings.defaultFocusDistance;	// Distance where transparency reaches zero
		
		state.imagesVisible = 0;
		state.imagesSeen = 0;
		state.panoramasVisible = 0;
		state.videosVisible = 0;
		state.videosSeen = 0;

		for (WMV_Image m : images) 		// Update and display images
		{
			if(!m.isDisabled())
			{
				float distance = m.getViewingDistance(); // Estimate image distance to camera based on capture location
				
				m.updateSettings(worldSettings, worldState, viewerSettings, viewerState);
				if(worldState.timeFading)
				{
					if(m.getAssociatedClusterID() < 0 || m.getAssociatedClusterID() >= clusters.size())
					{
						if(debugSettings.field || debugSettings.image || debugSettings.media)
							System.out.println("Error in Field.display()... cannot updateTimeBrightness: image id:"+m.getID()+" .getAssociatedCluster("+m.getAssociatedClusterID()+") < 0 || >= clusters.size():"+clusters.size()+"!!");
					}
					else 
						m.updateTimeBrightness(getCluster(m.getAssociatedClusterID()), timeline, utilities);
				}

				if(!m.verticesAreNull() && (m.isFading() || m.getMediaState().fadingFocusDistance))
					m.update(ml, utilities);  		// Update geometry + visibility

				if (distance < vanishingPoint && distance > viewerSettings.nearClippingDistance && !m.verticesAreNull()) 	// Visible	
				{
					if(!m.getMediaState().fadingFocusDistance && !m.isFading()) 
						m.update(ml, utilities);  	// Update geometry + visibility
					
					m.display(ml); 		// Draw image
					state.imagesVisible++;
				}
			}
		}
		
		for (WMV_Panorama n : panoramas)  	// Update and display panoramas
		{
			if(!n.isDisabled())
			{
				float distance = n.getViewingDistance(); // Estimate image distance to camera based on capture location

				n.updateSettings(worldSettings, worldState, viewerSettings, viewerState);
				if(worldState.timeFading)
				{
					if(n.getAssociatedClusterID() < 0 || n.getAssociatedClusterID() >= clusters.size()) 
					{
						if(debugSettings.field || debugSettings.panorama || debugSettings.media)
							System.out.println("Error in Field.display()... cannot updateTimeBrightness: pano id:"+n.getID()+" .getAssociatedCluster() ("+n.getAssociatedClusterID()+") < 0 || >= clusters.size():"+clusters.size()+"!!");
					}
					else 
						n.updateTimeBrightness(clusters.get(n.getAssociatedClusterID()), timeline, utilities);
				}
				if(distance < vanishingPoint)			// Check if panorama is in visible range
				{
					n.update(ml);  	// Update geometry + visibility
					n.display(ml); 		// Display panorama
					state.panoramasVisible++;
				}
				else if(n.isFading())
				{
					n.update(ml);  	// Update geometry + visibility
				}
			}
		}

		for (WMV_Video v : videos)  		// Update and display videos
		{
			if(!v.isDisabled())
			{
				float distance = v.getViewingDistance();	 // Estimate video distance to camera based on capture location
				boolean nowVisible = (distance < vanishingPoint);

				v.updateSettings(worldSettings, worldState, viewerSettings, viewerState);
				if ( v.isVisible() && !nowVisible )
					v.fadeOut();
				
				if(worldState.timeFading)
				{
					if(v.getAssociatedClusterID() < 0 || v.getAssociatedClusterID() >= clusters.size()) 
					{
						if(debugSettings.field || debugSettings.video || debugSettings.media)
							System.out.println("Error in Field.display()... cannot updateTimeBrightness: video id:"+v.getID()+" .getAssociatedCluster("+v.getAssociatedClusterID()+") < 0 || >= clusters.size():"+clusters.size()+"!!");
					}
					else 
						v.updateTimeBrightness(getCluster(v.getAssociatedClusterID()), timeline, utilities);
				}

//				v.updateTimeBrightness(clusters.get(v.getAssociatedClusterID()), timeline, utilities);
//				v.getMediaState().timeBrightness = 1.f;				// -- TESTING
				
				if (nowVisible || v.isFading())
				{
					if(v.getAssociatedClusterID() > clusters.size())
					{
						System.out.println("v id#"+v.getID()+" .getAssociatedCluster() > clusters.size():"+"v.getAssociatedCluster():"+v.getAssociatedClusterID() +"clusters.size():" +clusters.size());
					}
					else
					{
						v.update(ml, utilities);  	// Update geometry + visibility
						v.display(ml); 		// Display video
						state.videosVisible++;
					}
				}
				else
				{
					if(v.isFading() || v.isFadingVolume())
						v.update(ml, utilities);  	// Update geometry + visibility
					
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

//		if(worldSettings.showUserPanoramas || worldSettings.showStitchedPanoramas)
//		{
//			if(clusters.size()>0)
//				clusters.get(p.viewer.getCurrentClusterID()).draw();		// Draw current cluster
//		}
	}
	
	public void updateAllMediaSettings()
	{
		for (WMV_Image i : images)  		// Update and display videos
			if(!i.isDisabled())
				i.updateSettings(worldSettings, worldState, viewerSettings, viewerState);
	
		for (WMV_Panorama n : panoramas)  		// Update and display videos
			if(!n.isDisabled())
				n.updateSettings(worldSettings, worldState, viewerSettings, viewerState);
	
		for (WMV_Video v : videos)  		// Update and display videos
			if(!v.isDisabled())
				v.updateSettings(worldSettings, worldState, viewerSettings, viewerState);
	}
	
	/**
	 * Initialize field
	 * @param lockMediaToClusters Center media capture locations at associated cluster locations
	 */
	public boolean initialize(long randomSeed)
	{
		if( images.size()>0 || panoramas.size()>0 || videos.size()>0 || sounds.size()>0 )
		{
			if(debugSettings.main) System.out.println("Initializing field #"+state.id);

			if(randomSeed == -100000L) model.state.clusteringRandomSeed = System.currentTimeMillis();		// Save clustering random seed
			else model.state.clusteringRandomSeed = randomSeed;

			setupModel(); 						// Initialize field for first time 

			boolean hierarchical = false;		// Whether to use hierarchical clustering
			if(model.getState().validMedia < model.getState().hierarchicalMaxMedia) 
				hierarchical = true;

			calculateMediaLocations(); 				// Set location of each photo in simulation

//			detectMultipleFields();					// Run clustering on capture locations to detect multiple fields
//			divideField(3000.f, 15000.f);			

			findVideoPlaceholders();				// Find image place holders for videos
			calculateMediaVertices();				// Calculate all image vertices

			if(debugSettings.field) System.out.println("Will run initial clustering for field #"+state.id+"...");

			runInitialClustering(hierarchical);		// Find media clusters
//			model.findDuplicateClusterMedia();		// Find media in more than one cluster

			if(worldState.lockMediaToClusters)					// Center media capture locations at associated cluster locations
				lockMediaToClusters();	

			if( worldSettings.getTimeZonesFromGoogle ) 
				getTimeZoneFromGoogle();				// Get time zone for field from Google Time Zone API

			calculateBorderPoints();					// Calculate border points for field, used in Library View

			if(debugSettings.field) System.out.println("Will create timeline and dateline for field #"+state.id+"...");
			createTimeline();							// Create date-independent timeline for field
			createDateline();							// Create field dateline
			createTimelines();							// Create date-specific timelines for field
			analyzeClusterMediaDirections();			// Analyze angles of all images and videos in each cluster for Thinning Visibility Mode

			if(debugSettings.field) System.out.println("Finished initializing field #"+state.id+"..."+state.name);
			return hierarchical;
		}
		else
		{
			System.out.println("Field #"+getID()+" has no media! Cannot initialize field...");
			return false;
		}
	}
	
	public void getTimeZoneFromGoogle()
	{
		if(images.size() > 0)					
			state.timeZoneID = utilities.getCurrentTimeZoneID(images.get(0).getGPSLocation().z, images.get(0).getGPSLocation().x);
		else if(panoramas.size() > 0)
			state.timeZoneID = utilities.getCurrentTimeZoneID(panoramas.get(0).getGPSLocation().z, panoramas.get(0).getGPSLocation().x);
		else if(videos.size() > 0)
			state.timeZoneID = utilities.getCurrentTimeZoneID(videos.get(0).getGPSLocation().z, videos.get(0).getGPSLocation().x);
		else if(sounds.size() > 0)
			state.timeZoneID = utilities.getCurrentTimeZoneID(sounds.get(0).getGPSLocation().z, sounds.get(0).getGPSLocation().x);
	}
	
	/**
	 * Update field variables each frame
	 */
	public void update( WMV_WorldSettings currentWorldSettings, WMV_WorldState currentWorldState, WMV_ViewerSettings currentViewerSettings, 
						WMV_ViewerState currentViewerState)
	{
		worldSettings = currentWorldSettings;	// Update world settings
		worldState = currentWorldState;			// Update world state
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
	void findVideoPlaceholders()
	{
		for(WMV_Video v : videos)
			v.findPlaceholder(images);
	}

	 /** 
	  * Find video placeholder images, i.e. images taken just before a video to indicate same location, orientation, elevation and rotation angles
	  */	
//	 public void findVideoPlaceholders()
//	 {
//		 for (int i = 0; i < videos.size(); i++) 		
//		 {
//			 WMV_Video v = getVideo(i);
//			 if(!v.isDisabled())
//			 {
//				 int id = v.getImagePlaceholder();				// Find associated image with each video
//
//				 if(id != -1)
//				 {
//					 if(v.getAssociatedClusterID() != getImage(id).getAssociatedClusterID())
//					 {
//						 System.out.println("findVideoPlaceholders().. Will set associated cluster for video #"+v.getID()+" from "+v.getAssociatedClusterID()+" to getImage(id).getAssociatedClusterID():"+getImage(id).getAssociatedClusterID());
//						 v.setAssociatedClusterID( getImage(id).getAssociatedClusterID() );	// Set video cluster to cluster of associated image
//						 System.out.println("findVideoPlaceholders().. Have set associated cluster for video #"+v.getID()+" to "+v.getAssociatedClusterID());
//					 }
//					 clusters.get(v.getAssociatedClusterID()).getState().hasVideo = true;	// Set cluster video property to true
//					 if(debugSettings.video)
//						 System.out.println("Image placeholder for video: "+i+" is:"+id+" getCluster(v.cluster).video:"+getCluster(v.getAssociatedClusterID()).getState().hasVideo);
//				 }
//				 else
//				 {
//					 if(debugSettings.video)
//						 System.out.println("No image placeholder found for video: "+i+" getCluster(v.cluster).video:"+getCluster(v.getAssociatedClusterID()).getState().hasVideo);
//					 v.setDisabled(true);
//				 }
//			 }
//		 }
//	 }
	
	/**
	 * Calculate location of each media file in virtual space from GPS, orientation metadata
	 */
	public void calculateMediaLocations() 
	{
		if(debugSettings.field) System.out.println("Calculating image locations...");

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
	
	/**
	 * Create clusters from field media
	 */
	public void createClusters()
	{
		for(WMV_Cluster c : clusters)
			c.create(images, panoramas, videos);
	}

	/**
	 * Merge and initialize media clusters 
	 */
	void initializeClusters(boolean mergeClusters)
	{
		/* Empty clusters with no media */
		for( WMV_Cluster c : clusters )		
		{
			if(c.getState().mediaCount <= 0)
			{
				c.empty();					/* Empty cluster */
				if(debugSettings.cluster && debugSettings.detailed) System.out.println("Set cluster #"+c.getID()+" to empty...");
			}
		}
		
		/* Merge clusters */
		if(mergeClusters) clusters = mergeAdjacentClusters( clusters, model.getState().minClusterDistance );

		/* Analyze media in each cluster */
		if(debugSettings.field) System.out.println("Analyzing media in clusters...");

		for( WMV_Cluster c : clusters )
			if(!c.isEmpty())
				c.analyzeMedia(images, panoramas, videos);					

		if(debugSettings.field) System.out.println("Finished analyzing media...");
//		System.out.println("Setting cluster times/dates for media...");

		/* Set cluster date and time for each media object */
		for(WMV_Cluster c : clusters)
		{
			if(!c.isEmpty())
			{
				for(WMV_Image i : c.getImages(images))
				{
					i.setClusterTimes(c);
					i.setClusterDates(c);
				}

				for(WMV_Panorama n : c.getPanoramas(panoramas))
				{
					n.setClusterTimes(c);
					n.setClusterDates(c);
				}

				for(WMV_Video v : c.getVideos(videos))
				{
					v.setClusterTimes(c);
					v.setClusterDates(c);
				}

				c.findMediaSegments(images);
			}
		}
		
		verifyField();						/* Verify field */
	}
	
	 /**
	  * Create a new cluster from lists of image, panorama and video IDs
	  * @param index New clusterID
	  * @param location Location
	  * @param imageList GMV_Image list
	  * @param panoramas GMV_Panorama list
	  * @param videos GMV_Video list
	  * @return New cluster with given media
	  */
//	 public WMV_Cluster createCluster( int index, PVector location, List<Integer> imageList, List<Integer> panoramaList, List<Integer> videoList )
	 public WMV_Cluster createCluster( int index, PVector location, List<Integer> imageList, List<Integer> panoramaList, List<Integer> videoList )
	 {
		 WMV_Cluster newCluster = new WMV_Cluster(worldSettings, worldState, viewerSettings, debugSettings, index, location.x, location.y, location.z);

		 /* Add media to cluster */
		 for( int i : imageList )
			 newCluster.addImage(images.get(i));
		 for( int n : panoramaList )
			 newCluster.addPanorama(panoramas.get(n));
		 for( int v : videoList )
			 newCluster.addVideo(videos.get(v));

		 /* Check whether the cluster is a single media cluster */
		 if( imageList.size() == 1 && panoramaList.size() == 0 && videoList.size() == 0 )
			 newCluster.setSingle(true);
		 if( imageList.size() == 1 && panoramaList.size() == 0 && videoList.size() == 0 )
			 newCluster.setSingle(true);
		 if( imageList.size() == 0 && panoramaList.size() == 0 && videoList.size() == 1 )
			 newCluster.setSingle(true);

		 return newCluster;
	 }

	/** 
	 * If any images are not associated, create a new cluster for each
	 */	
	 void createSingleClusters()
	 {
		 int newClusterID = getClusters().size();	// Start adding clusters at end of current list 
		 int initial = newClusterID;

		 for (WMV_Image i : images) 			// Find closest cluster for each image
		 {
			 if(i.getAssociatedClusterID() == -1)				// Create cluster for each single image
			 {
				 addCluster(new WMV_Cluster(worldSettings, worldState, viewerSettings, debugSettings, newClusterID, i.getCaptureLocation().x, i.getCaptureLocation().y, i.getCaptureLocation().z));
//				 System.out.println("--- createSingleClusters()  Setting associated cluster for image:"+i.getID()+" from "+i.getMediaState().getClusterID()+" to "+newClusterID+"...");
				 i.setAssociatedClusterID(newClusterID);
				 getCluster(newClusterID).createSingle(i.getID(), 0);
				 newClusterID++;
			 }
		 }

		 for (WMV_Panorama n : panoramas) 						// Find closest cluster for each panorama
		 {
			 if(n.getAssociatedClusterID() == -1)				// Create cluster for each panorama
			 {
				 addCluster(new WMV_Cluster(worldSettings, worldState, viewerSettings, debugSettings, newClusterID, n.getCaptureLocation().x, n.getCaptureLocation().y, n.getCaptureLocation().z));
				 n.setAssociatedClusterID(newClusterID);

				 getCluster(newClusterID).createSingle(n.getID(), 1);
				 newClusterID++;
			 }
		 }

		 for (WMV_Video v : videos) 							// Find closest cluster for each video
		 {
			 if(v.getAssociatedClusterID() == -1)				// Create cluster for each single video
			 {
				 addCluster(new WMV_Cluster(worldSettings, worldState, viewerSettings, debugSettings, newClusterID, v.getCaptureLocation().x, v.getCaptureLocation().y, v.getCaptureLocation().z));

//				 System.out.println("--- createSingleClusters()  Setting associated cluster for video:"+v.getID()+" from "+v.getMediaState().getClusterID()+" to "+newClusterID+"...");
				 v.setAssociatedClusterID(newClusterID);

				 clusters.get(newClusterID).createSingle(v.getID(), 2);
				 newClusterID++;
			 }
		 }

		 if(debugSettings.cluster) System.out.println("Created "+(newClusterID-initial)+" clusters from single images...");
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
			 WMV_Cluster cluster = new WMV_Cluster(worldSettings, worldState, viewerSettings, debugSettings, index, location.x, location.y, location.z);
			 return cluster;
		 }
		 return null;
	 }

	 /**
	  * Set blur mask for image
	  * @param image Image for which to set blur mask
	  * @param blurMask Blur mask image
	  */
	 void setImageBlurMask(WMV_Image image, PImage blurMask)
	 {
		 image.setBlurMask(blurMask);
	 }

	 /**
	  * Set blur mask for image
	  * @param image Image for which to set blur mask
	  * @param blurMask Blur mask image
	  */
	 void setVideoBlurMask(WMV_Video video, PImage blurMask)
	 {
		 video.setBlurMask(blurMask);
	 }
	
	/**
	 * Calculate vertices for all images and videos in the field
	 */
	public void calculateMediaVertices() 
	{
		if(debugSettings.field) 	System.out.println("Calculating media vertices...");
		
		for (int i = 0; i < images.size(); i++) 
			images.get(i).calculateVertices();
		
		for (int i = 0; i < videos.size(); i++) 
			videos.get(i).calculateVertices();
	}

	/**
	 * Gradually fade all media brightness to zero
	 */
	public void fadeOutAllMedia()
	{
		if(debugSettings.field) System.out.println("Fading out media...");

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
	public void blackoutAllMedia()
	{
		if(debugSettings.field) System.out.println("Fading out media...");

		for (WMV_Image i : images)
			i.getMediaState().fadingBrightness = 0;
		
		for (WMV_Panorama n : panoramas) 
			n.getMediaState().fadingBrightness = 0;

		for (WMV_Video v : videos) 
			v.getMediaState().fadingBrightness = 0;
	}

	/**
	 * Stop the media in the field from fading
	 */
	public void stopAllFading()
	{
		if(debugSettings.field) System.out.println("Stopping all fading...");

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
	 * Verify all field parameters are ready before starting simulation
	 */
	void verifyField() 
	{
		if(debugSettings.field) System.out.println("Verifying field...");

		if (model.getState().fieldWidth <= 0 && clusters.size() > 1)
			System.out.println("  verifyField()... Field width <= 0!");

		if (model.getState().fieldHeight <= 0 && clusters.size() > 1)
			System.out.println("  verifyField()... Field height <= 0!");

		if (model.getState().fieldAspectRatio <= 0 && clusters.size() > 1)
			System.out.println("  verifyField()... Field ratio == "+model.getState().fieldAspectRatio+"!");
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
				if(hide) i.setHidden(true);
			}
		}
		for (WMV_Panorama n : panoramas)
		{
			if(n.isSelected())
			{
				n.setSelected(false);
				if(hide) n.setHidden(true);
			}
		}
		for (WMV_Video v : videos)
		{
			if(v.isSelected())
			{
				v.setSelected(false);
				if(hide) v.setHidden(true);
			}
		}
		for (WMV_Sound s : sounds)
		{
			if(s.isSelected())
			{
				s.setSelected(false);
				if(hide) s.setHidden(true);
			}
		}
	}
	
	/**
	 * Return list of selected media IDs of given type
	 * @param mediaType 0: image 1: panorama 2: video 3: sound
	 */
	public List<Integer> getSelectedMedia(int mediaType) 
	{
		List<Integer> selected = new ArrayList<Integer>();
		
		switch(mediaType)
		{
			case 0:
				for (WMV_Image i : images)
					if(i.isSelected())
						selected.add(i.getID());
				break;
			case 1:
				for (WMV_Panorama n : panoramas)
					if(n.isSelected())
						selected.add(n.getID());
				break;
	
			case 2:
				for (WMV_Video v : videos)
					if(v.isSelected())
						selected.add(v.getID());
				break;
	
			case 3:
				for (WMV_Sound s : sounds)
					if(s.isSelected())
						selected.add(s.getID());
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
			if(i.isFading() && !i.isDisabled())
				fading = true;

		if(!fading)
			for(WMV_Panorama n : panoramas)
				if(n.isFading() && !n.isDisabled())
					fading = true;

		if(!fading)
			for(WMV_Video v : videos)
				if(v.isFading() && !v.isDisabled())
					fading = true;

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
//		 if(debugSettings.field || debugSettings.field) System.out.println("lockMediaToClusters(): Moving media... ");
		 for (WMV_Image i : images) 
			 i.adjustCaptureLocation(clusters.get(i.getAssociatedClusterID()));		
		 for (WMV_Panorama n : panoramas) 
			 n.adjustCaptureLocation(clusters.get(n.getAssociatedClusterID()));		
		 for (WMV_Video v : videos) 
			 v.adjustCaptureLocation(clusters.get(v.getAssociatedClusterID()));		
//		 for (WMV_Sound s : getSounds()) 
//			 s.adjustCaptureLocation(clusters.get(s.getCluster()));		
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
		if(debugSettings.cluster || debugSettings.field) System.out.println("Running initial clustering for field: "+getName());

		state.clustersByDepth = new ArrayList<Integer>();
		
		if(hierarchical)						// If using hierarchical clustering
		{
			runHierarchicalClustering();			// Create dendrogram
			setDendrogramDepth( state.clusterDepth );		// Set initial dendrogram depth and initialize clusters
		}
		else										// If using k-means clustering
		{
			runKMeansClustering( worldSettings.kMeansClusteringEpsilon, model.getState().clusterRefinement, model.getState().clusterPopulationFactor );	// Get initial clusters using K-Means method
//			setClusters( cleanupClusters() );
		}
		if(debugSettings.cluster || debugSettings.field)
			System.out.println( "Created "+clusters.size()+" clusters...");
	}

	/**
	 * Run k-means clustering on media in field to find capture locations
	 * @param epsilon Minimum cluster movement 
	 * @param refinement Number of iterations to refine clusters
	 * @param populationFactor Cluster population factor
	 */
	public void runKMeansClustering(float epsilon, int refinement, float populationFactor)
	{
		if(debugSettings.cluster || debugSettings.field)
		{
			System.out.println("Running K-Means Clustering...");
			System.out.println(" ");
			System.out.println("  Iterations:"+refinement);
			System.out.println("  Population Factor:"+populationFactor);
			if(worldState.mergeClusters)
			{
				System.out.println("");
				System.out.println("Cluster Merging:");
				System.out.println("  Minimum Cluster Distance:"+worldSettings.minClusterDistance);
				System.out.println("  Maximum Cluster Distance:"+worldSettings.maxClusterDistance);
			}
			System.out.println(" ");
//			display.displayClusteringInfo(p.p);
		}
		setClusters( new ArrayList<WMV_Cluster>() );			// Clear current cluster list

		/* Estimate number of clusters */
		//	Note: mediaDensity = validMedia / fieldArea;				// Media per sq. m.
//		int numClusters = Math.round( (1.f / (float)Math.sqrt(model.getState().mediaDensity)) * populationFactor ); 	// Calculate numClusters from media density
		int numClusters = Math.round( ((float)Math.sqrt(model.getState().fieldArea)*(float)Math.sqrt(model.getState().validMedia)) * populationFactor );   // Calculate numClusters from media density

		if(debugSettings.cluster || debugSettings.field)
			System.out.println("Creating "+numClusters+" clusters based on "+model.getState().validMedia+" valid media...");
//		System.out.println("model.getState().mediaDensity: "+model.getState().mediaDensity +" populationFactor:"+populationFactor);
		
		/* K-means Clustering */
		if (model.getState().validMedia > 1) 				// If more than 1 media point
		{
			if(debugSettings.field) System.out.println("Running k-means clustering... model.validMedia:"+model.getState().validMedia);
			initializeKMeansClusters(numClusters);		// Create initial clusters at random image locations	
			refineKMeansClusters(epsilon, refinement);	// Refine clusters over many iterations
			createSingleClusters();						// Create clusters for single media points
			
			initializeClusters(worldState.mergeClusters);	// Initialize clusters (merge, etc.)
			setClusters( cleanupClusters() );				// Cleanup clusters
		}
		else System.out.println("Error in k-means clustering... model.validMedia == "+model.getState().validMedia);
		
		if(debugSettings.cluster || debugSettings.field)
			System.out.println("Created "+numClusters+" Clusters...");
	}
	
	/** 
	 * Create initial clusters at random image locations	 			-- Need to: record random seed, account for associated videos
	 */	
	void initializeKMeansClusters( int numClusters )
	{
		Random rng = new Random(System.currentTimeMillis());
		
		List<Integer> addedImages = new ArrayList<Integer>();			// Images already added to clusters; should include all images at end
		List<Integer> nearImages = new ArrayList<Integer>();			// Images nearby added media 

		List<Integer> addedPanoramas = new ArrayList<Integer>();		// Panoramas already added to clusters; should include all panoramas at end
		List<Integer> nearPanoramas = new ArrayList<Integer>();		// Panoramas nearby added media 

		List<Integer> addedVideos = new ArrayList<Integer>();			// Videos already added to clusters; should include all videos at end
		List<Integer> nearVideos = new ArrayList<Integer>();			// Videos nearby added media 

		for (int i = 0; i < numClusters; i++) 		// Iterate through the clusters
		{
			int imageID, panoramaID, videoID;			

			if(i == 0)			
			{
				imageID = (int) (rng.nextFloat() * images.size());  			// Random image ID for setting cluster's start location				
				panoramaID = (int) (rng.nextFloat() * panoramas.size());  	// Random panorama ID for setting cluster's start location				
				videoID = (int) (rng.nextFloat() * videos.size());  			// Random video ID for setting cluster's start location		
				
				addedImages.add(imageID);								
				addedPanoramas.add(panoramaID);								
				addedVideos.add(videoID);								

				/* Record media nearby added media*/
				for(WMV_Image img : images)						// Check for images near the picked one
				{
					float dist = img.getCaptureDistanceFrom(getImage(imageID).getCaptureLocation());  // Get distance
					if(dist < model.getState().minClusterDistance)
						nearImages.add(img.getID());				// Record images nearby picked image
				}

				for(WMV_Panorama pano : panoramas)				// Check for panoramas near the picked one 
				{
					float dist = pano.getCaptureDistanceFrom(panoramas.get(panoramaID).getCaptureLocation());  // Get distance
					if(dist < model.getState().minClusterDistance)
						nearPanoramas.add(pano.getID());			// Add to the list of nearby picked images
				}

				for(WMV_Video vid : videos)						// Check for videos near the picked one
				{
					float dist = vid.getCaptureDistanceFrom(getVideo(videoID).getCaptureLocation());  // Get distance
					if(dist < model.getState().minClusterDistance)
						nearVideos.add(vid.getID());				// Add to the list of nearby picked images
				}

				/* Create the cluster */
				PVector clusterPoint = new PVector(0,0,0);
				if(images.size() > 0)
				{
					clusterPoint = new PVector(getImage(imageID).getCaptureLocation().x, getImage(imageID).getCaptureLocation().y, getImage(imageID).getCaptureLocation().z); // Choose random image location to start
					addCluster(new WMV_Cluster(worldSettings, worldState, viewerSettings, debugSettings, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
					i++;
				}
				if(panoramas.size() > 0)
				{
					clusterPoint = new PVector(panoramas.get(panoramaID).getCaptureLocation().x, panoramas.get(panoramaID).getCaptureLocation().y, panoramas.get(panoramaID).getCaptureLocation().z); // Choose random image location to start
					addCluster(new WMV_Cluster(worldSettings, worldState, viewerSettings, debugSettings, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
					i++;
				}
				if(videos.size() > 0)
				{
					clusterPoint = new PVector(getVideo(videoID).getCaptureLocation().x, getVideo(videoID).getCaptureLocation().y, getVideo(videoID).getCaptureLocation().z); // Choose random image location to start
					addCluster(new WMV_Cluster(worldSettings, worldState, viewerSettings, debugSettings, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
					i++;
				}

				if(i > 0) i--;
				else if(debugSettings.field) System.out.println("Error in initClusters()... No media!!");
			}
			else															// Find a random media (image, panorama or video) location for new cluster
			{
				int mediaID = (int) (rng.nextFloat() * (images.size() + panoramas.size() + videos.size()));
				PVector clusterPoint = new PVector(0,0,0);

				if( mediaID < images.size() )				// If image, compare to already picked images
				{
					imageID = (int) (rng.nextFloat() * (images.size()));  		 			
					while(addedImages.contains(imageID) && nearImages.contains(imageID))
						imageID = (int) (rng.nextFloat() * (images.size()));  							

					addedImages.add(imageID);
					clusterPoint = new PVector(getImage(imageID).getCaptureLocation().x, getImage(imageID).getCaptureLocation().y, getImage(imageID).getCaptureLocation().z); // Choose random image location to start
				}
				else if( mediaID < images.size() + panoramas.size() )		// If panorama, compare to already picked panoramas
				{
					panoramaID = (int) (rng.nextFloat() * (panoramas.size()));  						
					while(addedPanoramas.contains(panoramaID) && nearPanoramas.contains(panoramaID))
						panoramaID = (int) (rng.nextFloat() * (panoramas.size()));  						

					addedPanoramas.add(panoramaID);
					clusterPoint = new PVector(panoramas.get(panoramaID).getCaptureLocation().x, panoramas.get(panoramaID).getCaptureLocation().y, panoramas.get(panoramaID).getCaptureLocation().z); // Choose random image location to start
				}
				else if( mediaID < images.size() + panoramas.size() + videos.size() )		// If video, compare to already picked videos
				{
					videoID = (int) (rng.nextFloat() * (videos.size()));  						
					while(addedImages.contains(videoID) && nearImages.contains(videoID))
						videoID = (int) (rng.nextFloat() * (videos.size()));  						

					addedVideos.add(videoID);
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
		if(debugSettings.cluster || debugSettings.field) System.out.println("--> Refining clusters... epsilon:"+epsilon+" iterations:"+iterations);
		
		while( count < iterations ) 							// Iterate to create the clusters
		{		
			for (WMV_Image img : images) 			// Find closest cluster for each image
			{
				boolean success = img.findAssociatedCluster(clusters, model.getState().maxClusterDistance);		// Set associated cluster
				if(debugSettings.field && debugSettings.detailed)
					if(!success) System.out.println("Couldn't find cluster for image #"+img.getID());
			}
			for (WMV_Panorama pano : panoramas) 		// Find closest cluster for each image
			{
				boolean success = pano.findAssociatedCluster(clusters, model.getState().maxClusterDistance);		// Set associated cluster
//				if(debugSettings.field && debugSettings.detailed)
					if(!success) System.out.println("Couldn't find cluster for pano #"+pano.getID());
			}
			for (WMV_Video vid : videos) 			// Find closest cluster for each image
			{
				boolean success = vid.findAssociatedCluster(clusters, model.getState().maxClusterDistance);		// Set associated cluster
//				if(debugSettings.field && debugSettings.detailed)
					if(!success) System.out.println("Couldn't find cluster for video #"+vid.getID());
			}
			for (int i = 0; i < getClusters().size(); i++) 		// Find closest cluster for each image
				clusters.get(i).create(images, panoramas, videos);						// Assign clusters

			if(getClusters().size() == last.size())				// Check cluster movement
			{
				for(WMV_Cluster c : clusters)
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
					if(debugSettings.cluster || debugSettings.field)
						System.out.println(" Stopped refinement... no clusters moved farther than epsilon: "+epsilon);
					break;								// If all clusters moved less than epsilon, stop refinement
				}
			}
			else
			{
				if(debugSettings.cluster || debugSettings.field)
					System.out.println(" New clusters found... will keep refining clusters... clusters.size():"+getClusters().size()+" last.size():"+last.size());
			}
			
			count++;
		}
		
//		for (int i = 0; i < images.size(); i++) 			// Find closest cluster for each image
//			System.out.println("Image #"+i+" cluster is "+images.get(i).getAssociatedClusterID());		// Set associated cluster
//		for (int i = 0; i < videos.size(); i++) 			// Find closest cluster for each image
//			System.out.println("Video #"+i+" cluster is "+videos.get(i).getAssociatedClusterID());		// Set associated cluster

	}

	/**
	 * Merge together clusters with closest neighbor below minClusterDistance threshold
	 */
	private ArrayList<WMV_Cluster> mergeAdjacentClusters(ArrayList<WMV_Cluster> clusterList, float minClusterDistance)
	{
		int mergedClusters = 0;			// Reset mergedClusters count

		IntList[] closeNeighbors = new IntList[ clusterList.size()+1 ];			// List array of closest neighbor distances for each cluster 
		ArrayList<PVector> mostNeighbors = new ArrayList<PVector>();			// List of clusters with most neighbors and number of neighbors as PVector(id, neighborCount)
		List<Integer> absorbed = new ArrayList<Integer>();										// List of clusters absorbed into other clusters
		List<Integer> merged = new ArrayList<Integer>();											// List of clusters already merged with neighbors
		float firstMergePct = 0.2f;												// Fraction of clusters with most neighbors to merge first
		
		if(debugSettings.cluster) System.out.println("Merging adjacent clusters... starting number:"+clusterList.size());

		for( WMV_Cluster c : clusterList )					// Find distances of close neighbors to each cluster
		{
			closeNeighbors[c.getID()] = new IntList();	// Initialize list for this cluster
			for( WMV_Cluster d : clusterList )
			{
				float dist = PVector.dist(c.getLocation(), d.getLocation());			// Get distance between clusters
				if(dist < minClusterDistance)								// If less than minimum distance
					closeNeighbors[c.getID()].append(d.getID());		// Add d to closest clusters to c
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
			if(!merged.contains(c.getID()))
			{
				for( WMV_Cluster d : clusterList )
				{
					if( !absorbed.contains(d.getID()) && !merged.contains(d.getID()) && c.getID() != d.getID() ) 	// If is different cluster and hasn't already been absorbed or merged
					{
						float dist = PVector.dist(c.getLocation(), d.getLocation());			// Get distance between clusters
						if(dist < minClusterDistance)
						{
							c.absorbCluster(d, images, panoramas, videos);
							absorbed.add(d.getID());

							merged.add(c.getID());
							merged.add(d.getID());
							mergedClusters++;
						}
					}
				}
			}
		}

		if(debugSettings.field)
			System.out.println("Merged Clusters..."+mergedClusters);
		
		ArrayList<WMV_Cluster> newList = new ArrayList<WMV_Cluster>();
		
		for(WMV_Cluster c : clusterList)
			if(!absorbed.contains(c.getID()))
				newList.add(c);
		
		if(debugSettings.field)
			System.out.println("Final clusters size..."+newList.size());
		return newList;
	}

	/**
	 * Build hieararchical clustering dendrogram and calculate clusters at each depth
	 */
	public void runHierarchicalClustering()
	{
		System.out.println("Running initial hierarchical clustering...");
		buildDendrogram();								// Build dendrogram 
		calculateClustersByDepth( dendrogramTop );		// Calculate number of clusters in dendrogram at each depth
		state.dendrogramCreated = true;
	}

	/**
	 * Calculate hieararchical clustering dendrogram for all media in field
	 */
	void buildDendrogram()
	{
		int namesIdx = 0, distIdx = 0;
		state.indexPanoramaOffset = images.size();
		state.indexVideoOffset = state.indexPanoramaOffset + panoramas.size();

		int size = images.size() + videos.size();
		names = new String[size];
		distances = new double[size][size];		

		System.out.println("Creating dendrogram...");

		/* Calculate distances between each image and all other media */
		for(WMV_Image i : images)		
		{
			namesIdx = i.getID();
			names[namesIdx] = Integer.toString(namesIdx);

			for(WMV_Image j : images)
			{
				if(i != j)				// Don't compare image with itself
				{
					distIdx = j.getID();
					distances[namesIdx][distIdx] = PVector.dist(i.getCaptureLocation(), j.getCaptureLocation());
				}
			}

			for(WMV_Panorama n : panoramas)
			{
				distIdx = n.getID() + state.indexPanoramaOffset;
				distances[namesIdx][distIdx] = PVector.dist(i.getCaptureLocation(), n.getCaptureLocation());
			}

			for(WMV_Video v : videos)
			{
				distIdx = v.getID() + state.indexVideoOffset;
				distances[namesIdx][distIdx] = PVector.dist(i.getCaptureLocation(), v.getCaptureLocation());
			}
		}

		/* Calculate distances between each panorama and all other media */
		for(WMV_Panorama n : panoramas)		
		{
			namesIdx = n.getID() + state.indexPanoramaOffset;
			names[namesIdx] = Integer.toString(namesIdx);

			for(WMV_Image i : images)
			{
				distIdx = i.getID();
				distances[namesIdx][distIdx] = PVector.dist(n.getCaptureLocation(), i.getCaptureLocation());
			}

			for(WMV_Panorama o : panoramas)
			{
				if(n != o)				// Don't compare panorama with itself
				{
					distIdx = n.getID() + state.indexPanoramaOffset;
					distances[namesIdx][distIdx] = PVector.dist(n.getCaptureLocation(), n.getCaptureLocation());
				}
			}

			for(WMV_Video v : videos)
			{
				distIdx = v.getID() + state.indexVideoOffset;
				distances[namesIdx][distIdx] = PVector.dist(n.getCaptureLocation(), v.getCaptureLocation());
			}
		}

		/* Calculate distances between each video and all other media */
		for(WMV_Video v : videos)		
		{
			namesIdx = v.getID() + state.indexVideoOffset;
			names[namesIdx] = Integer.toString(namesIdx);

			for(WMV_Image i : images)
			{
				distIdx = i.getID();
				distances[namesIdx][distIdx] = PVector.dist(v.getCaptureLocation(), i.getCaptureLocation());
			}

			for(WMV_Panorama n : panoramas)
			{
				distIdx = n.getID() + state.indexPanoramaOffset;
				distances[namesIdx][distIdx] = PVector.dist(v.getCaptureLocation(), n.getCaptureLocation());
			}

			for(WMV_Video u : videos)
			{
				if(v != u)				// Don't compare video with itself
				{
					distIdx = u.getID() + state.indexVideoOffset;
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
					System.out.println("Not a number:"+d);
					error = true;
				}
			}
		}
		for( int i = 0; i<size; i++)
		{
			String s = names[i];
			if(s == null)
			{
				System.out.println("String is null:"+s);
				error = true;
			}
		}

		if(!error)
		{
			try {
				ClusteringAlgorithm clusteringAlgorithm = new DefaultClusteringAlgorithm();
				System.out.println("Performing hierarchical clustering...");
				dendrogramTop = clusteringAlgorithm.performClustering(distances, names, new AverageLinkageStrategy());
			}
			catch(Throwable t)
			{
				System.out.println("Error while performing clustering... "+t);
			}
		}
	}

	/**
	 * @param depth Dendrogram depth level
	 * @return clusters GMV_Cluster list at given depth level based on dendrogram
	 */
	ArrayList<WMV_Cluster> createClustersFromDendrogram( int depth )
	{
		ArrayList<WMV_Cluster> wmvClusters = new ArrayList<WMV_Cluster>();	// GMV_Cluster list
		ArrayList<Cluster> clusters = new ArrayList<Cluster>();
		
		int imageCount = 0;
		int panoramaCount = 0;
		int videoCount = 0;

		if(debugSettings.cluster)
			System.out.println("--- Getting GMV_Clusters at depth "+depth+" ---");

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
				System.out.println("Top cluster is null!");
//			p.p.p.exit();
		}

		for( Cluster cluster : clusters )				// For each cluster at given depth
		{
			String name = cluster.getName();			// Otherwise, save the result if appropriate
			int mediaIdx = -1;

			String[] parts = name.split("#");			// Assume name in format "clstr#XX"
			boolean isMedia = false;

			List<Integer> images = new ArrayList<Integer>();
			List<Integer> panoramas = new ArrayList<Integer>();
			List<Integer> videos = new ArrayList<Integer>();

			if ( parts.length == 1 )					// If '#' isn't in the name, must be a media file
			{
				if(!utilities.isInteger(parts[0], 10))
				{
					if(debugSettings.cluster)
						System.out.println("Media name error! "+name);
				}
				else isMedia = true;
			}
			else if( parts.length == 2 )				
			{
				if(!utilities.isInteger(parts[1], 10))
				{
					if(debugSettings.cluster)
						System.out.println("Cluster name error! "+name);
				}
			}
			else
			{
				if(debugSettings.cluster)
					System.out.println("Media or cluster name error! "+name);
			}

			PVector location;

			if(isMedia)
			{
				if(debugSettings.cluster && debugSettings.detailed)
					System.out.println("Cluster "+cluster.getName()+" is a media file..."+name);

				mediaIdx = Integer.parseInt(name);

				if( mediaIdx < state.indexPanoramaOffset )
				{
					WMV_Image i = getImage(mediaIdx);
					location = i.getCaptureLocation();
					images.add(i.getID());
				}
				else if( mediaIdx < state.indexVideoOffset )
				{
					WMV_Panorama n = getPanorama(mediaIdx - state.indexPanoramaOffset);
					location = n.getCaptureLocation();
					panoramas.add(n.getID());
				}
				else
				{
					WMV_Video v = getVideo(mediaIdx - state.indexVideoOffset);
					location = v.getCaptureLocation();
					videos.add(v.getID());
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
					System.out.println("Calculated Average Point: "+location);
			}

			imageCount += images.size();
			panoramaCount += panoramas.size();
			videoCount += videos.size();

			wmvClusters.add( createCluster( wmvClusters.size(), location, images, panoramas, videos ) );
		}

		System.out.println("Got "+wmvClusters.size()+" clusters at depth "+depth+" from "+imageCount+" images, "+panoramaCount+" panoramas and "+videoCount+ "videos...");
		return wmvClusters;
	}
	
	/**
	 * @param depth New cluster depth
	 */
	void setDendrogramDepth(int depth)
	{
		state.clusterDepth = depth;
		setClusters( createClustersFromDendrogram( depth ) );	// Get clusters at defaultClusterDepth	 -- Set this based on media density

		for (int i = 0; i < images.size(); i++) 			// Find closest cluster for each image
			getImage(i).findAssociatedCluster(clusters, model.getState().maxClusterDistance);
		for (int i = 0; i < panoramas.size(); i++) 			// Find closest cluster for each image
			getPanorama(i).findAssociatedCluster(clusters, model.getState().maxClusterDistance);
		for (int i = 0; i < videos.size(); i++) 			// Find closest cluster for each video
			getVideo(i).findAssociatedCluster(clusters, model.getState().maxClusterDistance);

		if(getClusters().size() > 0)							// Find image place holders
			findVideoPlaceholders();

		initializeClusters(worldState.mergeClusters);					// Initialize clusters in Hierarchical Clustering Mode	 (Already done during k-means clustering)
	}

	/**
	 * @param top Dendrogram cluster to find associated media in
	 * @param mediaType Media type, 0: image 1: panorama 2: video
	 * @return List of cluster names of associated media 
	 */
	public List<Integer> getMediaInCluster( Cluster top, int mediaType )
	{
		ArrayList<Cluster> clusterList = (ArrayList<Cluster>) top.getChildren();	// Get clusters in topCluster
		int mediaCount = 0;														// Number of images successfully found
		int depthCount = 0;														// Depth reached from top cluster
		List<Integer> result = new ArrayList<Integer>();

		if((clusterList.size() == 0) || clusterList == null)							// If the top cluster has no children, it is a media file														
		{
			String name = top.getName();										// Otherwise, save the result if appropriate
			int mediaIdx = Integer.parseInt(name);

			if(debugSettings.cluster)
				System.out.println("No children in cluster "+name+" ... Already a media file!");

			if(mediaIdx < state.indexPanoramaOffset)
			{
				if(mediaType == 0)
				{
					result.add(mediaIdx);
					mediaCount++;
				}
			}
			else if(mediaIdx < state.indexVideoOffset)
			{
				if(mediaType == 1)
				{
					result.add(mediaIdx - state.indexPanoramaOffset);
					mediaCount++;
				}
			}
			else
			{
				if(mediaType == 2)
				{
					result.add(mediaIdx - state.indexVideoOffset);
					mediaCount++;
				}
			}
		}
		else if(clusterList.size() > 0)											 // Otherwise, it is a cluster of media files
		{
			boolean deepest = false;
			depthCount++;														// Move to next dendrogram level

			if(debugSettings.cluster && debugSettings.detailed)
				System.out.println("Searching for media in cluster: "+top.getName()+"...");

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
							System.out.print("  Cluster "+cluster.getName()+" has "+children.size()+" children at depth "+depthCount);
							System.out.println("  Added to next depth, array size:"+nextDepth.size()+"...");
						}
					}

					if(children.size() == 0 || (children == null))															
					{
						String name = cluster.getName();								// Otherwise, save the result if appropriate
						int mediaIdx = Integer.parseInt(name);

						if(mediaIdx < state.indexPanoramaOffset)
						{
							if(mediaType == 0)
							{
								result.add(mediaIdx);
								mediaCount++;
							}
						}
						else if(mediaIdx < state.indexVideoOffset)
						{
							if(mediaType == 1)
							{
								result.add(mediaIdx - state.indexPanoramaOffset);
								mediaCount++;
							}
						}
						else
						{
							if(mediaType == 2)
							{
								result.add(mediaIdx - state.indexVideoOffset);
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
			System.out.println( "Found "+mediaCount+" media at depth "+depthCount+" result.size():"+result.size() );

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

		if(debugSettings.cluster) System.out.println("Counting clusters at all depth levels...");
		state.clustersByDepth.add(1);					// Add top cluster to clustersByDepth list

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

				state.clustersByDepth.add(clusters.size());							// Record cluster number at depthCount

				if(debugSettings.cluster && debugSettings.detailed) System.out.println("Found "+clusters.size()+" clusters at depth:"+depthCount);

				deepest = !( children.size() > 0 );								// At deepest level when list of chidren is empty

				if(!deepest)
				{
					clusters = children;											// Move down one depth level 
					depthCount++;
				}
			}
		}

		state.deepestLevel = depthCount;
	}

	/**
	 * Create date-independent timeline for this field from cluster timelines
	 */
	public void createTimeline()
	{
		timeline = new WMV_Timeline();
		timeline.initialize(null);
		
		if(debugSettings.time) 
			System.out.println("Creating Field Timeline...");

		for(WMV_Cluster c : clusters)											// Find all media cluster times
			for(WMV_TimeSegment t : c.getTimeline().timeline)
				timeline.timeline.add(t);

		timeline.timeline.sort(WMV_TimeSegment.WMV_TimeLowerBoundComparator);			// Sort time segments 
		
		int count = 0;															// Number in chronological order on field timeline
		for (WMV_TimeSegment t : timeline.timeline) 		
		{
			t.setFieldTimelineID(count);
			count++;
		}

		timeline.finishTimeline();			// Finish timeline / set bounds
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
		timelines = new ArrayList<WMV_Timeline>();

		for(WMV_Date d : dateline)			// For each date on dateline
		{
			WMV_Timeline newTimeline = new WMV_Timeline();
			newTimeline.initialize(null);
			
//			ArrayList<WMV_TimeSegment> newTimeline = new ArrayList<WMV_TimeSegment>();
			for(WMV_TimeSegment t : timeline.timeline)		// Add each cluster time segment to this date-specific field timeline 
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
//					t.setID(count);
					t.setFieldDateID(ct);
					t.setFieldTimelinesID(count);

					for(WMV_TimeSegment fieldTime : timeline.timeline)		 
					{
						if(t.getClusterID() == fieldTime.getClusterID() && t.getFieldTimelineID() == fieldTime.getFieldTimelineID())
						{
							fieldTime.setFieldDateID(ct);
							fieldTime.setFieldTimelinesID(count);
//							System.out.println("Correcting fieldTime for cluster #:"+fieldTime.getClusterID()+" ct:"+ct+" count:"+count
//											+" newTimeline size:"+newTimeline.size());	
						}
					}
					
					count++;
				}
				
				timelines.add( newTimeline );		// Calculate and add timeline to list
				if(debugSettings.field)
					System.out.println("Added timeline #"+ct+" for field #"+getID()+" with "+newTimeline.timeline.size()+" segments...");
			}
			else
			{
				timelines.add( newTimeline );		// Add empty timeline to preserve indexing 
//				System.out.println("Added EMPTY timeline #"+ct+" for field #"+fieldID);
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

		for(WMV_TimeSegment t : timeline.timeline)
		{
			if(t.getClusterID() < clusters.size())
			{
				WMV_Waypoint w = clusters.get(t.getClusterID()).getClusterAsWaypoint();
				timelinePath.add(w);
			}
			else
			{
				System.out.println("getTimelineAsPath() Error: t.getClusterID():"+t.getClusterID()+" > clusters.size():"+clusters.size()+" timeline.timeline.size():"+timeline.timeline.size());
				System.out.println("     t.getFieldTimelineID():"+t.getFieldTimelineID()+" t.getClusterTimelineID():"+t.getClusterTimelineID());
				System.out.println("     t.getClusterTimelineIDOnDate():"+t.getClusterTimelineIDOnDate()+" t.getFieldDateID():"+t.getFieldDateID());
				System.out.println("     t.getClusterDateID():"+t.getClusterDateID()+" t.getTimespanAsString(true, true):"+t.getTimespanAsString(true, true));
			}
		}
		
		if(debugSettings.field)
			System.out.println("getTimelineAsPath()... timelinePath.size():"+timelinePath.size());

		return timelinePath;
	}

	public int getFirstTimeSegment()
	{
		if(timeline.timeline.size() > 0)
			return timeline.timeline.get(0).getFieldTimelineID();
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
		
		if(clusterID >= 0 && clusterID < clusters.size())
		{
			if(clusters.get(clusterID).getTimeline() != null)
				if(index >= 0 && index < clusters.get(clusterID).getTimeline().timeline.size())
					t = clusters.get(clusterID).getTimeline().timeline.get(index);
		}

		if(t == null)
			System.out.println("NULL time segment "+index+" returned by getTimeSegmentInCluster() id:"+clusterID+" index:"+index);
		else if(clusterID != t.getClusterID())
			System.out.println("ERROR in getTimeSegmentInCluster().. clusterID and timeSegment clusterID do not match!  clusterID:"+clusterID+" t.getClusterID():"+t.getClusterID());

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
			System.out.println("Couldn't get date "+index+" in cluster "+id);
		
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
		List<Integer> addedImages = new ArrayList<Integer>();		// Images already added to clusters; should include all images at end
		List<Integer> nearImages = new ArrayList<Integer>();			// Images nearby added media 

		List<Integer> addedPanoramas = new ArrayList<Integer>();		// Panoramas already added to clusters; should include all panoramas at end
		List<Integer> nearPanoramas = new ArrayList<Integer>();		// Panoramas nearby added media 

		List<Integer> addedVideos = new ArrayList<Integer>();			// Videos already added to clusters; should include all videos at end
		List<Integer> nearVideos = new ArrayList<Integer>();			// Videos nearby added media 

		for (int i = 0; i < numFields; i++) 		// Iterate through the clusters
		{
			int imageID, panoramaID, videoID;			

			if(i == 0)			
			{
				long clusteringRandomSeed = (long) world.p.random(1000.f);
				world.p.randomSeed(clusteringRandomSeed);
				imageID = (int) world.p.random(images.size());  			// Random image ID for setting cluster's start location				
				panoramaID = (int) world.p.random(panoramas.size());  		// Random panorama ID for setting cluster's start location				
				videoID = (int) world.p.random(videos.size());  			// Random video ID for setting cluster's start location				
				addedImages.add(imageID);								
				addedPanoramas.add(panoramaID);								
				addedVideos.add(videoID);								

				for(WMV_Image img : images)						// Check for images near the picked one
				{
					float dist = img.getCaptureDistanceFrom(getImage(imageID).getCaptureLocation());  // Get distance
					if(dist < minFieldDistance)
						nearImages.add(img.getID());				// Record images nearby picked image
				}

				for(WMV_Panorama pano : panoramas)				// Check for panoramas near the picked one 
				{
					float dist = pano.getCaptureDistanceFrom(getPanorama(panoramaID).getCaptureLocation());  // Get distance
					if(dist < minFieldDistance)
						nearPanoramas.add(pano.getID());			// Add to the list of nearby picked images
				}

				for(WMV_Video vid : videos)				// Check for panoramas near the picked one 
				{
					float dist = vid.getCaptureDistanceFrom(getVideo(videoID).getCaptureLocation());  // Get distance
					if(dist < minFieldDistance)
						nearVideos.add(vid.getID());			// Add to the list of nearby picked images
				}

				PVector clusterPoint = new PVector(0,0,0);				// Create first cluster 
				if(images.size() > 0)
				{
					PVector imgLoc = getImage(imageID).getCaptureLocation();
					clusterPoint = new PVector(imgLoc.x, imgLoc.y, imgLoc.z); // Choose random image location to start
					fieldClusters.add(new WMV_Cluster(worldSettings, worldState, viewerSettings, debugSettings, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
					i++;
				}
				else if(panoramas.size() > 0)
				{
					PVector panoLoc = getPanorama(panoramaID).getCaptureLocation();
					clusterPoint = new PVector(panoLoc.x, panoLoc.y, panoLoc.z); // Choose random panorama location to start
					fieldClusters.add(new WMV_Cluster(worldSettings, worldState, viewerSettings, debugSettings, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
					i++;
				}
				else if(videos.size() > 0)
				{
					PVector vidLoc = getVideo(videoID).getCaptureLocation();
					clusterPoint = new PVector(vidLoc.x, vidLoc.y, vidLoc.z); // Choose random video location to start
					fieldClusters.add(new WMV_Cluster(worldSettings, worldState, viewerSettings, debugSettings, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
					i++;
				}
			}
			else											// Find a random media (image, panorama or video) location for new cluster
			{
				int mediaID = (int) world.p.random(images.size() + panoramas.size() + videos.size());
				PVector clusterPoint = new PVector(0,0,0);

				if( mediaID < images.size() )				// If image, compare to already picked images
				{
					imageID = (int) world.p.random(images.size());  						
					while(addedImages.contains(imageID) && nearImages.contains(imageID))
						imageID = (int) world.p.random(images.size());  						

					addedImages.add(imageID);
					
					PVector imgLoc = getImage(imageID).getCaptureLocation();
					clusterPoint = new PVector(imgLoc.x, imgLoc.y, imgLoc.z); // Choose random image location to start
				}
				else if( mediaID < images.size() + panoramas.size() )		// If panorama, compare to already picked panoramas
				{
					panoramaID = (int) world.p.random(panoramas.size());  						
					while(addedPanoramas.contains(panoramaID) && nearPanoramas.contains(panoramaID))
						panoramaID = (int) world.p.random(panoramas.size());  						

					addedPanoramas.add(panoramaID);
					
					PVector panoLoc = getPanorama(panoramaID).getCaptureLocation();
					clusterPoint = new PVector(panoLoc.x, panoLoc.y, panoLoc.z); // Choose random image location to start
				}
				else if( mediaID < images.size() + panoramas.size() + videos.size() )		// If video, compare to already picked videos
				{
					videoID = (int) world.p.random(videos.size());  						
					while(addedImages.contains(videoID) && nearImages.contains(videoID))
						videoID = (int) world.p.random(videos.size());  						

					addedVideos.add(videoID);
					
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
			System.out.println("--> Refining fields...");

		while( count < refinement ) 							// Iterate to create the clusters
		{		
			for (int i = 0; i < images.size(); i++) 			// Find closest cluster for each image
				getImage(i).findAssociatedCluster(fieldClusters, maxFieldDistance);		// Set associated cluster
			for (int i = 0; i < panoramas.size(); i++) 		// Find closest cluster for each image
				getPanorama(i).findAssociatedCluster(fieldClusters, maxFieldDistance);		// Set associated cluster
			for (int i = 0; i < videos.size(); i++) 		// Find closest cluster for each panorama
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
						System.out.println(" Stopped refinement... no clusters moved farther than epsilon:"+epsilon);
					break;								// If all clusters moved less than epsilon, stop refinement
				}
			}
			else
			{
				if(debugSettings.field)
					System.out.println(" New clusters found... will keep refining clusters... clusters.size():"+fieldClusters.size()+" last.size():"+last.size());
			}

			count++;
		}

		fieldClusters = mergeAdjacentClusters(fieldClusters, 2500.f);

		if(debugSettings.field)
			System.out.println("Detected "+fieldClusters.size()+" fields...");

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
	public List<Integer> getSelectedImageIDs()
	{
		List<Integer> selected = new ArrayList<Integer>();

		for(WMV_Image i : images)
			if(i.isSelected())
				selected.add(i.getID());
		
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
				System.out.println("No clusters in field...");
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
	
	/**
	 * Capture current field state for exporting to file
	 */
	public void captureState()
	{
		state.setTimeData(timeline, dateline);											// Store time data
		state.setModelData(model.state);											// Store time data
	}
	
	/**
	 * Capture current field state for exporting to file
	 */
	public WMV_ClusterStateList captureClusterStates()
	{
		ArrayList<WMV_ClusterState> clusterStates = new ArrayList<WMV_ClusterState>();				

		if(debugSettings.field)
			System.out.println("captureClusterStates()... Checking all times/dates for null variables...");
		
		for(WMV_Cluster c : clusters)
		{
			boolean error = false;
			if(c.getDateline() == null)
			{
				System.out.println("  c.getDateline() == null... id:"+c.getID()+" media count:"+c.getMediaCount());
				error = true;
			}
			if(c.getTimeline() == null)
			{
				System.out.println("  c.getTimeline() == null... id:"+c.getID()+" media count:"+c.getMediaCount());
				error = true;
			}
			if(c.getTimelines() == null)
			{
				System.out.println("  c.getTimelines() == null... id:"+c.getID()+" media count:"+c.getMediaCount());
				error = true;
			}

			if(!error)
			{
				for(WMV_Date d : c.getDateline())
				{
//					if(d.dateTime == null)
//						System.out.println("  d.dateTime == null"+" timeInitialized? "+d.timeInitialized);
					if(d.timeInitialized == false)
						System.out.println("  timeInitialized == "+d.timeInitialized+" d.dateTime == null?"+(d.dateTime == null));
					if(d.dateTimeString == null)
						System.out.println("  d.dateTimeString == null");
					if(d.timeZoneID == null)
						System.out.println("  d.timeZoneID == null");
				}
//				if(!error) System.out.println("No errors...");
//				System.out.println("  Checking timeline dates for null variables...");
				error = false;
				for(WMV_TimeSegment ts : c.getTimeline().timeline)
				{
					if(ts.getLower().dateTime == null)
						System.out.println("  ts.getLower().dateTime == null");
					if(ts.getCenter().dateTime == null)
						System.out.println("  ts.getCenter().dateTime == null");
					if(ts.getUpper().dateTime == null)
						System.out.println("  ts.getUpper().dateTime == null");
					if(ts.getLower().dateTimeString == null)
						System.out.println("  ts.getLower().dateTimeString == null");
					if(ts.getCenter().dateTimeString == null)
						System.out.println("  ts.getCenter().dateTimeString == null");
					if(ts.getUpper().dateTimeString == null)
						System.out.println("  ts.getUpper().dateTimeString == null");
					for(WMV_Time t : ts.timeline)
					{
						if(t.dateTime == null)
							System.out.println("  t.dateTime == null");
						if(t.dateTimeString == null)
							System.out.println("  t.dateTimeString == null");
						if(t.timeZoneID == null)
							System.out.println("  t.timeZoneID == null");
					}
				}
//				if(!error) System.out.println("No errors...");
//				System.out.println("  Checking timelines dates for null variables...");
				error = false;
				for(WMV_Timeline tl : c.getTimelines())
				{
					if(tl.getLower() == null) System.out.println("  tl.getLower() == null");
					else
					{
						if(tl.getLower().getLower() == null)
							System.out.println("  tl.getLower().getLower() == null");
						else
						{
							if(tl.getLower().getLower().dateTime == null)
								System.out.println("  tl.getLower().getLower().dateTime == null");
							if(tl.getLower().getLower().dateTimeString == null)
								System.out.println("  tl.getLower().getLower().dateTimeString == null");
						}

						if(tl.getLower().getUpper() == null)
							System.out.println("  tl.getLower().getUpper() == null");
						else
						{
							if(tl.getLower().getUpper().dateTime == null)
								System.out.println("  tl.getLower().getUpper().dateTime == null");
							if(tl.getLower().getUpper().dateTimeString == null)
								System.out.println("  tl.getLower().getUpper().dateTimeString == null");
						}
					}
					if(tl.getUpper() == null) 
						System.out.println("  tl.getUpper() == null");
					else
					{
						if(tl.getUpper().getLower() == null)
							System.out.println("  tl.getUpper().getLower() == null");
						else
						{
							if(tl.getUpper().getLower().dateTime == null)
								System.out.println("  tl.getUpper().getLower().dateTime == null");
							if(tl.getUpper().getLower().dateTimeString == null)
								System.out.println("  tl.getUpper().getLower().dateTimeString == null");
						}

						if(tl.getUpper().getUpper() == null)
							System.out.println("  tl.getUpper().getUpper() == null");
						else
						{
							if(tl.getUpper().getUpper().dateTime == null)
								System.out.println("  tl.getUpper().getUpper().dateTime == null");
							if(tl.getUpper().getUpper().dateTimeString == null)
								System.out.println("  tl.getUpper().getUpper().dateTimeString == null");
						}
					}

					for(WMV_TimeSegment ts : tl.timeline)
					{
						if(ts.getLower().dateTime == null)
							System.out.println("  ts.getLower().dateTime == null");
						if(ts.getCenter().dateTime == null)
							System.out.println("  ts.getCenter().dateTime == null");
						if(ts.getUpper().dateTime == null)
							System.out.println("  ts.getUpper().dateTime == null");
						if(ts.getLower().dateTimeString == null)
							System.out.println("  ts.getLower().dateTimeString == null");
						if(ts.getCenter().dateTimeString == null)
							System.out.println("  ts.getCenter().dateTimeString == null");
						if(ts.getUpper().dateTimeString == null)
							System.out.println("  ts.getUpper().dateTimeString == null");
						for(WMV_Time t : ts.timeline)
						{
							if(t.dateTime == null)
								System.out.println("    t.dateTime == null");
							if(t.dateTimeString == null)
								System.out.println("    t.dateTimeString == null");
							if(t.timeZoneID == null)
								System.out.println("    t.timeZoneID == null");
						}
					}
				}
			}
			else
			{
				System.out.println("Error... cluster #"+c.getID()+" dateline, timeline or timelines == null!!!");
			}

			WMV_ClusterState cState = c.getState();
			if(cState != null)
			{
				clusterStates.add(c.getState());
			}
			else
			{
				System.out.println("  Didn't output cluster #"+c.getID()+" since state is NULL!!!");
			}
		}
		
		WMV_ClusterStateList csl = new WMV_ClusterStateList();
		csl.setClusters(clusterStates);
		return csl;
	}

	/**
	 * Capture current field state for exporting to file
	 */
	public WMV_ImageStateList captureImageStates()
	{
		ArrayList<WMV_ImageState> imageStates = new ArrayList<WMV_ImageState>(); 				

		for(WMV_Image i : images)
		{
			i.captureState();							// Save current image state for exporting
			WMV_ImageState iState = i.getState();
			iState.resetState();
			if(iState != null)
				imageStates.add(iState);
		}

		WMV_ImageStateList isl = new WMV_ImageStateList();
		isl.setImages(imageStates);
		return isl;
	}

	/**
	 * Capture current field state for exporting to file
	 */
	public WMV_PanoramaStateList capturePanoramaStates()
	{
		ArrayList<WMV_PanoramaState> panoramaStates = new ArrayList<WMV_PanoramaState>(); 			

		for(WMV_Panorama n : panoramas)					// Save current panorama state for exporting
		{
			n.captureState();
			WMV_PanoramaState pState = n.getState();
			pState.resetState();
			if(pState != null)
				panoramaStates.add(pState);
		}

		WMV_PanoramaStateList psl = new WMV_PanoramaStateList();
		psl.setPanoramas(panoramaStates);
		return psl;
	}


	/**
	 * Capture current field state for exporting to file
	 */
	public WMV_VideoStateList captureVideoStates()
	{
		ArrayList<WMV_VideoState> videoStates = new ArrayList<WMV_VideoState>(); 				

		for(WMV_Video v : videos)						// Save current video state for exporting
		{
			v.captureState();
			WMV_VideoState vState = v.getState();
			vState.resetState();
			if(vState != null)
				videoStates.add(vState);
		}

		WMV_VideoStateList vsl = new WMV_VideoStateList();
		vsl.setVideos(videoStates);
		return vsl;
	}


	/**
	 * Capture current field state for exporting to file
	 */
	public WMV_SoundStateList captureSoundStates()
	{
		ArrayList<WMV_SoundState> soundStates = new ArrayList<WMV_SoundState>(); 				

		for(WMV_Sound s : sounds)						// Save current video state for exporting
		{
			s.captureState();
			WMV_SoundState sState = s.getState();
			if(sState != null)
				soundStates.add(sState);
		}

		WMV_SoundStateList ssl = new WMV_SoundStateList();
		ssl.setSounds(soundStates);
		return ssl;
	}

	
	/**
	 * Set the current field state from file
	 */
	public boolean setState( MultimediaLocator ml, WMV_FieldState newFieldState, WMV_ClusterStateList newClusterStateList, 
			WMV_ImageStateList newImageStateList, WMV_PanoramaStateList newPanoramaStateList, WMV_VideoStateList newVideoStateList)
	{
		boolean error = false, clusterError = false;
		if( newFieldState != null && newClusterStateList.clusters != null && (newImageStateList != null 
				|| newPanoramaStateList != null || newVideoStateList != null ) ) //|| newFieldState.sounds != null) )
		{
			PImage emptyImage = ml.createImage(0,0,processing.core.PConstants.RGB);

			try{
				int curFieldID = state.id;
				state = newFieldState;
				state.id = curFieldID;
			}
			catch(Throwable t)
			{
				System.out.println("Field: "+state.name+" Error 1 in setState():"+t);
				error = true;
			}

			System.out.println("Setting media states for field #"+getID()+" ... ");
			try{

				System.out.println(" Adding Clusters... "+newClusterStateList.clusters.size());
				for(WMV_ClusterState cs : newClusterStateList.clusters)
				{
					WMV_Cluster newCluster = getClusterFromClusterState(cs);
					addCluster(newCluster);
				}
			}
			catch(Throwable t)
			{
				System.out.println("Field: "+state.name+" Error loading clusters in setState()... "+t);
				clusterError = true;
			}

			try{
				if(newImageStateList != null)
				{
					if(newImageStateList.images != null)
					{
//						System.out.println(" Adding Images... "+newImageStateList.images.size());
						for(WMV_ImageState is : newImageStateList.images)
						{
							WMV_Image newImage = getImageFromImageState(is);
							if(newImage != null)
							{
								newImage.setImage(emptyImage);
								addImage(newImage);
							}
						}
					}
				}
				
				if(newPanoramaStateList != null)
				{
					if(newPanoramaStateList.panoramas != null)
					{
//						System.out.println(" Adding Panoramas... "+newPanoramaStateList.panoramas.size());
						for(WMV_PanoramaState ps : newPanoramaStateList.panoramas)
						{
							WMV_Panorama newPanorama = getPanoramaFromPanoramaState(ps);
							if(newPanorama != null)
							{
								newPanorama.setTexture(emptyImage);
								addPanorama(newPanorama);
							}
						}
					}
				}
				if(newVideoStateList != null)
				{
					if(newVideoStateList.videos != null)
					{
//						System.out.println(" Adding Videos... "+newVideoStateList.videos.size());
						for(WMV_VideoState vs : newVideoStateList.videos)
						{
							WMV_Video newVideo = getVideoFromVideoState(vs);
							Movie newMovie = new Movie(ml, vs.getMetadata().filePath);
							newVideo.setVideoLength(newMovie);
							newVideo.setFrame(emptyImage);
							addVideo(newVideo);
						}
					}
				}
//				if(state.sounds != null)
//				{
//					for(WMV_SoundState ss : state.sounds)
//					for(WMV_SoundState ss : newSoundStateList.sounds)
//					{
//						WMV_Sound newSound = getSoundFromSoundState(ss);
//						addSound(newSound);
//					}
//				}
			}
			catch(Throwable t)
			{
				System.out.println("Field: "+state.name+" Media error in setState()... "+t);
				error = true;
			}

			if(!error)
			{
				try{
					timeline = newFieldState.timeline;
					dateline = newFieldState.dateline;
					model = new WMV_Model();
					model.initialize(worldSettings, debugSettings);
					model.setState(newFieldState.model);

					if(clusterError)		// Error loading clusters
					{
						boolean hierarchical = false;
						if(model.getState().validMedia < model.getState().hierarchicalMaxMedia)
							hierarchical = true;
						runInitialClustering(hierarchical);		// Find media clusters
					}
					
					if(clusters == null) clusters = new ArrayList<WMV_Cluster>();
					if(images == null) images = new ArrayList<WMV_Image>();
					if(panoramas == null) panoramas = new ArrayList<WMV_Panorama>();
					if(videos == null) videos = new ArrayList<WMV_Video>();
					
					/* Perform checks */
					boolean mediaLoaded = (clusters.size() > 0);
					if(mediaLoaded) mediaLoaded = (images.size() > 0 || panoramas.size() > 0 || videos.size() > 0);

					boolean timelineLoaded = (timeline.timeline.size() > 0);
					boolean datelineLoaded = (dateline.size() > 0);

					if(timelineLoaded && datelineLoaded) createTimelines();					// Create timelines array
					boolean timelinesCreated = (timelines.size() == dateline.size());

					if(mediaLoaded && timelineLoaded && timelinesCreated && datelineLoaded)
						return true;
					else
						return false;
				}
				catch(Throwable t)
				{
					System.out.println("Field: "+state.name+" Error 3 in setState():"+t);
				}

				return false;
			}
			else
				return false;
		}
		else
		{
			System.out.println("Field: "+state.name+" Error 4 in setState()");
			if(newFieldState == null)
				System.out.println("newFieldState == null");
			if(newClusterStateList.clusters == null) 
				System.out.println("newClusterStateList.clusters == null");
			if(newImageStateList == null)
				System.out.println("newImageStateList == null");
			if(newPanoramaStateList == null) 
				System.out.println("newPanoramaStateList == null");
			if(newVideoStateList == null)
				System.out.println("newVideoStateList == null");

			return false;
		}
	}

//	public WMV_ClusterStateList getClusterStateList()
//	{
//		WMV_ClusterStateList csl = new WMV_ClusterStateList();
//		csl.setClusters(clusterStates);
//	}
	
	 /**
	  * Remove empty clusters and renumber after merging adjacent clusters
	  * @param clusters Cluster list
	  * @return Cleaned up cluster list
	  */
	 public ArrayList<WMV_Cluster> cleanupClusters()
	 {
		 ArrayList<WMV_Cluster> result = new ArrayList<WMV_Cluster>();
		 int count = 0;
		 int before = clusters.size();

		 for(WMV_Cluster c : clusters)
		 {
			 if(!c.isEmpty() && c.getMediaCount() > 0)
			 {
				 int oldID = c.getID();
				 c.setID(count);

				 if(images.size() > 0) c.setHasImage(true);
				 else c.setHasImage(false);
				 if(panoramas.size() > 0) c.setHasPanorama(true);
				 else c.setHasPanorama(false);
				 if(videos.size() > 0) c.setHasVideo(true);
				 else c.setHasVideo(false);
				 if(sounds.size() > 0) c.setHasSound(true);
				 else c.setHasSound(false);
				 
				 for(WMV_Image i : images)
					 if(i.getAssociatedClusterID() == oldID)
						 i.setAssociatedClusterID(count);
				 for(WMV_Panorama n : panoramas)
					 if(n.getAssociatedClusterID() == oldID)
						 n.setAssociatedClusterID(count);
				 for(WMV_Video v : videos)
					 if(v.getAssociatedClusterID() == oldID)
						 v.setAssociatedClusterID(count);
				 for(WMV_Sound s : getSounds())
					 if(s.getAssociatedClusterID() == oldID)
						 s.setAssociatedClusterID(count);

				 for(WMV_TimeSegment t:c.getTimeline().timeline)
				 {
					 if(t.getClusterID() != count)
						 t.setClusterID(count);
					 for(WMV_Time tm:t.timeline)
					 {
						 if(tm.getClusterID() != count)
							 tm.setClusterID(count);
					 }
				 }

				 for(WMV_Timeline tl:c.getTimelines())
				 {
					 for(WMV_TimeSegment t:tl.timeline)
					 {
						 if(t.getClusterID() != count)
							 t.setClusterID(count);
						 for(WMV_Time tm:t.timeline)
						 {
							 if(tm.getClusterID() != count)
								 tm.setClusterID(count);
						 }
					 }
				 }

				 result.add(c);
				 count ++;
			 }
		 }	

		 int removed = before - result.size();
		 if(debugSettings.field)
		 {
			 System.out.println("Removed "+removed+" clusters from field #"+getID());
			 System.out.println("Finished cleaning up clusters in field #"+getID());
		 }
		 
		 return result;
	 }

	 public WMV_WorldState getWorldState()
	 {
		 return worldState;
	 }

	 public WMV_WorldSettings getWorldSettings()
	 {
		 return worldSettings;
	 }

	 public WMV_ViewerState getViewerState()
	 {
		 return viewerState;
	 }

	 public WMV_ViewerSettings getViewerSettings()
	 {
		 return viewerSettings;
	 }

	 private WMV_Cluster getClusterFromClusterState(WMV_ClusterState clusterState)
	 {
		 WMV_Cluster newCluster = new WMV_Cluster( worldSettings, worldState, viewerSettings, debugSettings, clusterState.id, 
				 clusterState.location.x, clusterState.location.y, clusterState.location.z);

		 newCluster.setState( (WMV_ClusterState) clusterState );
		 newCluster.initializeTime();
		 return newCluster;
	 }
		
	private WMV_Image getImageFromImageState(WMV_ImageState imageState)
	{
		WMV_Image newImage = new WMV_Image( imageState.getMediaState().id, null, imageState.getMediaState().mediaType, imageState.getMetadata());
		newImage.setState( imageState );
		newImage.initializeTime();
		return newImage;
	}
	
	private WMV_Panorama getPanoramaFromPanoramaState(WMV_PanoramaState panoState)
	{
		WMV_Panorama newPanorama = new WMV_Panorama( panoState.mState.id, panoState.mState.mediaType, panoState.phi, panoState.mState.location, null, 
				panoState.getMetadata() );

		newPanorama.setState( panoState );
		newPanorama.initializeTime();
		return newPanorama;
	}
	
	private WMV_Video getVideoFromVideoState(WMV_VideoState videoState)			 // --  NULL error
	{
		WMV_Video newVideo = new WMV_Video( videoState.mState.id, null, videoState.mState.mediaType, videoState.getMetadata() );
		newVideo.setState( videoState );
		newVideo.initializeTime();
		return newVideo;
	}
	
	public WMV_Sound getSoundFromSoundState(WMV_SoundState soundState)
	{
		WMV_Sound newSound = new WMV_Sound(0, 0, null);
		newSound.setState( soundState );
		newSound.initializeTime();
		return newSound;
	}
	
	public String getName()
	{
		return state.name;
	}
	
	public void setID(int newID)
	{
		state.id = newID;
	}
	
	public int getID()
	{
		return state.id;
	}
	
	public WMV_FieldState getState()
	{
		return state;
	}

	public WMV_Timeline getTimeline()
	{
		return timeline;
	}
	
	public ArrayList<WMV_Timeline> getTimelines()
	{
		return timelines;
	}
	
	public ArrayList<WMV_Date> getDateline()
	{
		return dateline;
	}
	
	public WMV_TimeSegment getTimeSegment(int idx)
	{
		return timeline.timeline.get(idx);
	}
	
	public WMV_TimeSegment getTimeSegmentOnDate(int tsIdx, int dateIdx)
	{
		return timelines.get(dateIdx).timeline.get(tsIdx);
	}
	
	public WMV_Date getDate(int idx)
	{
		return dateline.get(idx);
	}
	
	public String getTimeZoneID()
	{
		return state.timeZoneID;
	}

	public void setName(String newName)
	{
		state.name = newName;
	}
	
	public void setVisited(boolean newState)
	{
		state.visited = newState;
	}
	
	public boolean hasBeenVisited()
	{
		return state.visited;
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
		state.imageErrors++;
	}

	public void addPanoramaError()
	{
		state.panoramaErrors++;
	}

	public void addVideoError()
	{
		state.videoErrors++;
	}

	public int getImageErrors()
	{
		return state.imageErrors;
	}

	public int getPanoramaErrors()
	{
		return state.panoramaErrors;
	}

	public int getVideoErrors()
	{
		return state.videoErrors;
	}
	
	public int getImageCount()
	{
		return images.size() - state.imageErrors;
	}
	
	public int getPanoramaCount()
	{
		return panoramas.size() - state.panoramaErrors;
	}
	
	public int getVideoCount()
	{
		return videos.size() - state.videoErrors;
	}

	public int getSoundCount()
	{
		return sounds.size();
	}

	public int getMediaCount()
	{
		return getImageCount() + getPanoramaCount() + getVideoCount() + getSoundCount();
	}

	public int getImagesVisible()
	{
		return state.imagesVisible;
	}
	
	public int getPanoramasVisible()
	{
		return state.panoramasVisible;
	}
	
	public int getVideosVisible()
	{
		return state.videosVisible;
	}

	public void setImagesVisible(int newValue)
	{
		state.imagesVisible = newValue;
	}
	
	public void setPanoramasVisible(int newValue)
	{
		state.panoramasVisible = newValue;
	}
	
	public void setVideosVisible(int newValue)
	{
		state.videosVisible = newValue;
	}

	public int getImagesSeen()
	{
		return state.imagesSeen;
	}
	
	public int getPanoramasSeen()
	{
		return state.panoramasSeen;
	}
	
	public int getVideosPlaying()
	{
		return state.videosPlaying;
	}
	
	public int getVideosSeen()
	{
		return state.videosSeen;
	}

	public int getVideosLoaded()
	{
		return state.videosLoaded;
	}
	
	public void setImagesSeen(int newValue)
	{
		state.imagesSeen = newValue;
	}
	
	public void setPanoramasSeen(int newValue)
	{
		state.panoramasSeen = newValue;
	}
	
	public void setVideosPlaying(int newValue)
	{
		state.videosPlaying = newValue;
	}
	
	public void setVideosSeen(int newValue)
	{
		state.videosSeen = newValue;
	}

	public void setVideosLoaded(int newValue)
	{
		state.videosLoaded = newValue;
	}
	
	public int getDisassociatedImages()
	{
		return state.disassociatedImages;
	}

	public int getDisassociatedPanoramas()
	{
		return state.disassociatedPanoramas;
	}

	public int getDisassociatedVideos()
	{
		return state.disassociatedVideos;
	}

	public void setDisassociatedImages(int newValue)
	{
		state.disassociatedImages = newValue;
	}

	public void setDisassociatedPanoramas(int newValue)
	{
		state.disassociatedPanoramas = newValue;
	}

	public void setDisassociatedVideos(int newValue)
	{
		state.disassociatedVideos = newValue;
	}
	
	/**  
	 * Export statistics on current field -- in progress
	 */
//	public void exportFieldInfo()
//	{
//		BufferedWriter writer = null;
//
//		try {
//			String timeLog = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
//			File logFile = new File(timeLog);
//
//			System.out.println(logFile.getCanonicalPath());
//
//			writer = new BufferedWriter(new FileWriter(logFile));
//			writer.write("Field: "+getName());
//		} 
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//		finally {
//			try {
//				// Close the writer regardless of what happens...
//				writer.close();
//			} 
//			catch (Exception e) {
//			}
//		}
//	}

	/**
	 * Get convex hull of set of n points using Jarvis March algorithm.
	 * Based on: http://www.geeksforgeeks.org/convex-hull-set-1-jarviss-algorithm-or-wrapping/
	 * @param points
	 * @return
	 */
	public void calculateBorderPoints()
	{
		border = new ArrayList<PVector>();
		ArrayList<PVector> points = new ArrayList<PVector>();

//		for(WMV_Cluster c : clusters)
//		{
//			PVector cGPSLoc = utilities.getGPSLocation(this, c.getLocation());
//			points.add(new PVector(cGPSLoc.x, cGPSLoc.y));
//		}

		for(WMV_Image i : images)
		{
			PVector iGPSLoc = utilities.getGPSLocation(this, i.getLocation());
			points.add(new PVector(iGPSLoc.x, iGPSLoc.y));
		}
		for(WMV_Panorama n : panoramas)
		{
			if(n.getLocation() != null)
			{
				PVector pGPSLoc = utilities.getGPSLocation(this, n.getLocation());
				points.add(new PVector(pGPSLoc.x, pGPSLoc.y));
			}
			else
				System.out.println("Error in calculateBorderPoints()... panorama #"+n.getID()+" has no location!!!!");
		}
		for(WMV_Video v : videos)
		{
			PVector vGPSLoc = utilities.getGPSLocation(this, v.getLocation());
			points.add(new PVector(vGPSLoc.x, vGPSLoc.y));
		}

		WMV_ModelState m = getModel().getState();
		if(m.highLongitude != -1000000 && m.lowLongitude != 1000000 && m.highLatitude != -1000000 && m.lowLatitude != 1000000 && m.highAltitude != -1000000 && m.lowAltitude != 1000000)
		{
			if(m.highLongitude != m.lowLongitude && m.highLatitude != m.lowLatitude)
			{
				model.state.centerLongitude = (m.lowLongitude + m.highLongitude) * 0.5f; 	// GPS longitude decreases from left to right
				model.state.centerLatitude = (m.lowLatitude + m.highLatitude) * 0.5f; 				// GPS latitude increases from bottom to top, minus sign to match P3D coordinate space
//				System.out.println("Found field#"+getID()+" center point... model.state.centerLongitude:"+model.state.centerLongitude+" model.state.centerLatitude:"+model.state.centerLatitude);
			}
			else
			{
				System.out.println("Error finding field #"+getID()+" center point...");
			}
		}
		
//		border = findBorder(points, new PVector(model.state.centerLongitude, model.state.centerLatitude));
//		border = findBorder(points, new PVector(0,0));
		border = findBorder(points, new PVector(100000,100000));

		/* Correct border points' order */
//		int count = 0;
//		System.out.println("Unsorted Border points for field #"+getID());
//		for(PVector bp : border)
//		{
//			System.out.println(" Unsorted Point #"+count+" bp.x:"+bp.x+" bp.y:"+bp.y);
//			count++;
//		}
		
//		border = findBorder(points, new PVector(100000,100000));

//	 	DEBUGGING
//		count = 0;
//		System.out.println("Sorted Border points for field #"+getID());
//		PVector last = border.get(0);
//		for(PVector bp : border)
//		{
//			PointComparator pc = new PointComparator(new PVector(model.state.centerLatitude, model.state.centerLongitude));
//			System.out.println("  Point #"+count+" bp.x:"+bp.x+" bp.y:"+bp.y+ " compare(bp, last):"+(pc.compare(bp, last)));
//			last = bp;
//			count++;
//		}
		
		// TESTING
//		border = findBorder(points, new PVector(model.state.centerLongitude, model.state.centerLatitude));
//		count = 0;
//		System.out.println("Corrected border points for field #"+getID());
//		for(PVector bp : border)
//		{
//			System.out.println(" Corrected Point #"+count+" bp.x:"+bp.x+" bp.y:"+bp.y);
//			count++;
//		}

		calculatedBorderPoints = true;
	}
		
//	private ArrayList<PVector> findBorder(ArrayList<PVector> points, PVector centerOfHull)
//	{
//		ArrayList<PVector> borderPts = new ArrayList<PVector>();
//
//		// There must be at least 3 points
//		if (points.size() < 3) return null;
//
//		// Find the leftmost point
//		int leftmost = 0;
//		for (int i = 1; i < points.size(); i++)
//			if (points.get(i).x < points.get(leftmost).x)
//				leftmost = i;
//
//		// Start from leftmost point, keep moving counterclockwise until reach the start PVector again.  This loop runs O(h) times where h is number of points in result or output.
//		int curPoint = leftmost, q;
//		System.out.println("findBorder()  points.size():"+points.size()+" initial p:"+curPoint+" l:"+leftmost);
//
//		int whileCt = 0;
//		do
//		{
//			// Add current point to result
//			borderPts.add(points.get(curPoint));
//
//			// Search for a point 'q' such that orientation(p, x, q) is counterclockwise for all points 'x'. The idea
//			// is to keep track of last visited most counterclockwise PVector in q. 
//			// If any PVector 'i' is more counterclockwise than q, then update q.
//			q = (curPoint+1)%points.size();
//			for (int i = 0; i < points.size(); i++)
//			{
//				// If i is more counterclockwise than current q, then update q
//				int result = getPointTripletOrientation(points.get(curPoint), points.get(i), points.get(q));
//				
//				if(whileCt < 950 && result != 1)
//					System.out.println("findBorder()  result != 1: "+result+" p:"+curPoint+" i:"+i+" q:"+q);
//				
//				if (result == 2)
//				{
//					q = i;
//				}
//			}
//
//			// Now q is the most counterclockwise with respect to p. Set p as q for next iteration, so that q is added to result 'hull'
//			curPoint = q;
//			
//			whileCt++;
//			if(whileCt < 950)
//				System.out.println("  whileCt: "+whileCt+" p == l? "+(curPoint == leftmost)+" borderPts.size():"+borderPts.size()+"   p:"+curPoint+" l:"+leftmost+" q:"+q);
//		} 
//		while (curPoint != leftmost);  // While we don't come to first PVector
//
//		// Print Result
//		//	  for (int i = 0; i < hull.size(); i++)
//		//	    System.out.println( "(" + hull.get(i).x + ", "
//		//	      + hull.get(i).y + ")\n");
//
//		if(debugSettings.field)
//			System.out.println("Found media points border of size:"+border.size());
//
////		borderPts.sort(new PointComp(centerOfHull));
//		
//		IntList removeList = new IntList();
//		ArrayList<PVector> removePoints = new ArrayList<PVector>();
//		
//		int count = 0;
//		for(PVector pv : borderPts)
//		{
//			int ct = 0;
//			for(PVector comp : borderPts)
//			{
//				if(ct != count)		// Don't compare same indices
//				{
//					if(pv.equals(comp))
//					{
//						if(!removePoints.contains(pv))
//						{
//							removeList.append(count);
//							removePoints.add(pv);
//						}
//						break;
//					}
//				}
//				ct++;
//			}
//			count++;
//		}
//		
//		if(debugSettings.field) System.out.println("Will remove "+removeList.size()+" points from field #"+getID()+" border of size "+borderPts.size()+"...");
//
//		ArrayList<PVector> newPoints = new ArrayList<PVector>();
//		count = 0;
//		for(PVector pv : borderPts)
//		{
//			if(!removeList.hasValue(count))
//				newPoints.add(pv);
//			count++;
//		}
//		borderPts = newPoints;
//		
////		for(int i=removeList.size()-1; i>=0; i--)
////		{
////			borderPts.remove(i);
////			i--;
////		}
//
////		System.out.println("Points remaining: "+borderPts.size()+"...");
////		System.out.println("Will sort remaining "+borderPts.size()+" points in border...");
////		borderPts.sort(c);
//		
//		return borderPts;
//	}
	
	private ArrayList<PVector> findBorder(ArrayList<PVector> points, PVector centerOfHull)
	{
		ArrayList<PVector> borderPts = new ArrayList<PVector>();

		// There must be at least 3 points
		if (points.size() < 3) return null;

		JarvisPoints pts = new JarvisPoints(points);
		JarvisMarch jm = new JarvisMarch(pts);
//		double start = System.currentTimeMillis();
		int n = jm.calculateHull();
//		double end = System.currentTimeMillis();
//		System.out.printf("%d points found %d vertices %f seconds\n", points.size(), n, (end-start)/1000.);

		int length = jm.getHullPoints().pts.length;
		borderPts = new ArrayList<PVector>();
		
		for(int i=0; i<length; i++)
			borderPts.add( jm.getHullPoints().pts[i] );
		
		// Print Result
		//	  for (int i = 0; i < hull.size(); i++)
		//	    System.out.println( "(" + hull.get(i).x + ", "
		//	      + hull.get(i).y + ")\n");

		if(debugSettings.field)
			System.out.println("Found media points border of size:"+border.size());

//		borderPts.sort(new PointComp(centerOfHull));
		
		IntList removeList = new IntList();
		ArrayList<PVector> removePoints = new ArrayList<PVector>();
		
		int count = 0;
		for(PVector pv : borderPts)
		{
			int ct = 0;
			for(PVector comp : borderPts)
			{
				if(ct != count)		// Don't compare same indices
				{
					if(pv.equals(comp))
					{
						if(!removePoints.contains(pv))
						{
							removeList.append(count);
							removePoints.add(pv);
						}
						break;
					}
				}
				ct++;
			}
			count++;
		}
		
		if(debugSettings.field) System.out.println("Will remove "+removeList.size()+" points from field #"+getID()+" border of size "+borderPts.size()+"...");

		ArrayList<PVector> newPoints = new ArrayList<PVector>();
		count = 0;
		for(PVector pv : borderPts)
		{
			if(!removeList.hasValue(count))
				newPoints.add(pv);
			count++;
		}
		borderPts = newPoints;
		
//		for(int i=removeList.size()-1; i>=0; i--)
//		{
//			borderPts.remove(i);
//			i--;
//		}

//		System.out.println("Points remaining: "+borderPts.size()+"...");
//		System.out.println("Will sort remaining "+borderPts.size()+" points in border...");
//		borderPts.sort(c);
		
		return borderPts;
	}
	
	class JarvisMarch 
	{
	  JarvisPoints pts;
	  private JarvisPoints hullPoints = null;
	  private List<Float> hy;
	  private List<Float> hx;
	  private int startingPoint;
	  private double currentAngle;
	  private static final double MAX_ANGLE = 4;

	  JarvisMarch(JarvisPoints pts) {
	    this.pts = pts;
	  }

	  /**
	   * The Jarvis March, sometimes known as the Gift Wrap Algorithm.
	   * The next point is the point with the next largest angle.
	   * <p/>
	   * Imagine wrapping a string around a set of nails in a board.  Tie the string to the leftmost nail
	   * and hold the string vertical.  Now move the string clockwise until you hit the next, then the next, then
	   * the next.  When the string is vertical again, you will have found the hull.
	   */
	  public int calculateHull() 
	  {
	    initializeHull();

	    startingPoint = getStartingPoint();
	    currentAngle = 0;

	    addToHull(startingPoint);
	    for (int p = getNextPoint(startingPoint); p != startingPoint; p = getNextPoint(p))
	      addToHull(p);

	    buildHullPoints();
	    return hullPoints.pts.length;
	  }

	  public int getStartingPoint() {
	    return pts.startingPoint();
	  }

	  private int getNextPoint(int p) {
	    double minAngle = MAX_ANGLE;
	    int minP = startingPoint;
	    for (int i = 0; i < pts.pts.length; i++) {
	      if (i != p) {
	        double thisAngle = relativeAngle(i, p);
	        if (thisAngle >= currentAngle && thisAngle <= minAngle) {
	          minP = i;
	          minAngle = thisAngle;
	        }
	      }
	    }
	    currentAngle = minAngle;
	    return minP;
	  }

	  private double relativeAngle(int i, int p) {
		return pseudoAngle(pts.pts[i].x - pts.pts[p].x, pts.pts[i].y - pts.pts[p].y);
//	    return pseudoAngle(pts.x[i] - pts.x[p], pts.y[i] - pts.y[p]);
	  }

	  private void initializeHull() {
	    hx = new LinkedList<Float>();
	    hy = new LinkedList<Float>();
	  }

	  private void buildHullPoints() {
	    float[] ax = new float[hx.size()];
	    float[] ay = new float[hy.size()];
	    int n = 0;
	    for (Iterator<Float> ix = hx.iterator(); ix.hasNext(); )
	      ax[n++] = ix.next();

	    n = 0;
	    for (Iterator<Float> iy = hy.iterator(); iy.hasNext(); )
	      ay[n++] = iy.next();

	    ArrayList<PVector> newPts = new ArrayList<PVector>();
	    for(int i=0; i<ax.length; i++)
	    {
	    	newPts.add(new PVector(ax[i], ay[i]));
	    }
	    hullPoints = new JarvisPoints(newPts);
	  }

	  private void addToHull(int p) {
	    hx.add(pts.pts[p].x);
	    hy.add(pts.pts[p].y);
	  }

	  /**
	   * The PseudoAngle is a number that increases as the angle from vertical increases.
	   * The current implementation has the maximum pseudo angle < 4.  The pseudo angle for each quadrant is 1.
	   * The algorithm is very simple.  It just finds where the angle intesects a square and measures the
	   * perimeter of the square at that point.  The math is in my Sept '06 notebook.  UncleBob.
	   */
	  double pseudoAngle(double dx, double dy) {
	    if (dx >= 0 && dy >= 0)
	      return quadrantOnePseudoAngle(dx, dy);
	    if (dx >= 0 && dy < 0)
	      return 1 + quadrantOnePseudoAngle(Math.abs(dy), dx);
	    if (dx < 0 && dy < 0)
	      return 2 + quadrantOnePseudoAngle(Math.abs(dx), Math.abs(dy));
	    if (dx < 0 && dy >= 0)
	      return 3 + quadrantOnePseudoAngle(dy, Math.abs(dx));
	    throw new Error("Impossible");
	  }

	  double quadrantOnePseudoAngle(double dx, double dy) {
	    return dx / (dy + dx);
	  }

	  public JarvisPoints getHullPoints() {
	    return hullPoints;
	  }
	}
	
	class JarvisPoints {
		public PVector[] pts;
		
//		public double x[];
//		public double y[];

		public JarvisPoints(ArrayList<PVector> newPts) 
		{
			pts = new PVector[newPts.size()];
			for(int i=0; i<newPts.size(); i++)
				pts[i] = newPts.get(i);
		}

//		public JarvisPoints(double[] x, double[] y) {
//			this.x = x;
//			this.y = y;
//		}

		// The starting point is the point with the lowest X
		// With ties going to the lowest Y.  This guarantees
		// that the next point over is clockwise.
		int startingPoint() 
		{
			double minX = pts[0].x;
			double minY = pts[0].y;
//			double minY = y[0];
//			double minX = x[0];
			
			int iMin = 0;
			for (int i = 1; i < pts.length; i++) 
			{
				if (pts[i].x < minX) 
				{
					minX = pts[i].x;
					iMin = i;
				} 
				else if (minX == pts[i].x && pts[i].y < minY) 
				{
					minY = pts[i].y;
					iMin = i;
				}
			}
			return iMin;
		}
	}
	
	/**
	 * Find orientation of ordered triplet (p, q, r).
	 * @param p Point 1
	 * @param q Point 2
	 * @param r Point 3
	 * @return 0: colinear, 1: clockwise, 2: counterclockwise
	 */
//	private int getPointTripletOrientation(PVector p, PVector q, PVector r)
//	{
//	  float val = (q.y - p.y) * (r.x - q.x) -
//	    (q.x - p.x) * (r.y - q.y);
//
//	  if (val == 0.f) return 0;
//	  return (val > 0.f) ? 1 : 2; 
//	}
//	
//	private class PointComparator implements Comparator<PVector> {
//
//	    private PVector center;
//
//	    public PointComparator(PVector center) {
//	        this.center = center;
//	    }
//
//	    public int compare(PVector o1, PVector o2) {
////	        System.out.println("PointComp... o1.y:"+o1.y+" o1.x:"+o1.x+" o2.y:"+o2.y+" o2.x:"+o2.x);
//	        double angle1 = Math.atan2(o1.y - center.y, o1.x - center.x);
//	        double angle2 = Math.atan2(o2.y - center.y, o2.x - center.x);
////	        System.out.println("  angle1:"+angle1+" angle2:"+angle2);
//	        if (angle1 > angle2)
//	        {
////		        System.out.println("0  (angle1 < angle2):"+(angle1 < angle2));
//	            return 1;
//	        }
//	        if (angle1 < angle2)
//	        {
////		        System.out.println("1  (angle1 < angle2):"+(angle1 < angle2));
//	            return -1;
//	        }
//	        
////	        System.out.println("Angles are the same");
//	        return 0;
//	    }
//	}
	

//	/**
//	 * @param depth Depth at which to draw clusters
//	 * Draw the clusters at given depth
//	 */
//	void drawClustersAtDepth( Cluster topCluster, int depth )
//	{		 
//		System.out.println("Drawing clusters at depth level:"+depth);
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
//			if(mediaIdx < state.indexPanoramaOffset)
//			{
//				location = p.getImage(mediaIdx).getCaptureLocation();
//			}
//			else if(mediaIdx < state.indexVideoOffset)
//			{
//				mediaIdx -= state.indexPanoramaOffset; 
//				location = p.getPanoramas().get(mediaIdx).getCaptureLocation();
//			}
//			else
//			{
//				mediaIdx -= state.indexVideoOffset; 
//				location = p.videos.get(mediaIdx).getCaptureLocation();
//			}
//
//			p.p.p.display.message("Drawing point:");
//			p.p.p.display.drawMapPoint(location, 20.f, p.p.p.display.largeMapWidth, p.p.p.display.largeMapHeight, p.p.p.display.mapClusterHue, 255, 255, p.p.p.display.mapClusterHue);
//		}
//		if(debugSettings.cluster) 
//			System.out.println("Getting "+clusters.size()+" dendrogram clusters at depth:"+depth);
//	}
}
