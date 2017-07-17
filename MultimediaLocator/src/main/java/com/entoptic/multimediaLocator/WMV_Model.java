package main.java.com.entoptic.multimediaLocator;

import java.util.ArrayList;
import java.util.List;

import com.apporiented.algorithm.clustering.*;
import processing.core.PVector;

/******************************************
 * Media environment spatial model
 * @author davidgordon
 */

public class WMV_Model
{
	/* General */
	WMV_WorldSettings worldSettings;	// World settings
	WMV_WorldState worldState;			// World state
	WMV_ViewerSettings viewerSettings;	// Viewer settings
	WMV_ViewerState viewerState;		// Viewer state
	WMV_ModelState state;				// Model state
	WMV_Utilities utilities;			// Utility methods
	ML_DebugSettings debug;		// Debug settings

	WMV_Model(){}

	public void initialize(WMV_WorldSettings newWorldSettings, ML_DebugSettings newDebugSettings)
	{
		state = new WMV_ModelState();									// Initialize model state
		worldSettings = newWorldSettings;								// Load world settings
		debug = newDebugSettings;								// Load debug settings
		
		utilities = new WMV_Utilities();								// Initialize utility class
		state.clusteringRandomSeed = System.currentTimeMillis();		// Save clustering random seed
	}

	/**
	 * Setup virtual space based on media capture locations
	 */
	public void setup(ArrayList<WMV_Image> images, ArrayList<WMV_Panorama> panoramas, ArrayList<WMV_Video> videos, ArrayList<WMV_Sound> sounds)		
	{
		if (images.size() > 0 || panoramas.size() > 0 || videos.size() > 0 || sounds.size() > 0)
		{
			if(debug.world || debug.gps) System.out.println("Model.setup()... Initializing field model...");
			
			analyzeSpatialDimensions(images, panoramas, videos, null); 		// Calculate bounds of field from spatial metadata
																			// -- No sounds, since need model to set sound locations) -- Fix model later!
			analyzeTimeDimensions(images, panoramas, videos, sounds);		// Analyze media times and dates

			float midLongitude = (state.highLongitude - state.lowLongitude) / 2.f;
			float midLatitude = (state.highLatitude - state.lowLatitude) / 2.f;

			/* Calculate number of valid media points */
			state.validImages = images.size();
			state.validPanoramas = panoramas.size();
			state.validVideos = videos.size();
			state.validSounds = sounds.size();
			state.validMedia = state.validImages + state.validPanoramas + state.validVideos + state.validSounds;

			if(state.validMedia > 1)
			{
				state.fieldWidth = utilities.getGPSDistanceInMeters(midLatitude, state.highLongitude, midLatitude, state.lowLongitude);
				state.fieldLength = utilities.getGPSDistanceInMeters(state.highLatitude, midLongitude, state.lowLatitude, midLongitude);
				state.fieldHeight = state.highAltitude - state.lowAltitude;					
			}
			else
			{
				state.fieldWidth = 1000.f;
				state.fieldLength = 1000.f;
				state.fieldHeight = 50.f;
			}

			state.fieldArea = state.fieldWidth * state.fieldLength;				// Calculate field area    -- Also use field volume?
			state.mediaDensity = state.validMedia / state.fieldArea;			// Media per sq. m.

			setMinClusterDistance(worldSettings.minClusterDistance); 			// Minimum distance between clusters, i.e. closer than which clusters are merged
			setMaxClusterDistance(worldSettings.maxClusterDistance);			// Maximum distance between clusters, i.e. farther than which single image clusters are created (set based on mediaDensity)

			if( state.highLongitude == -1000000 || state.lowLongitude == 1000000 || state.highLatitude == -1000000
				|| state.lowLatitude == 1000000 )			// If field dimensions aren't initialized
			{
				state.lowLongitude = 1000.f;
				state.fieldLength = 1000.f;
				state.fieldHeight = 50.f;						// Altitude already in meters
			}

			if(state.validMedia == 1)
			{
				state.lowLongitude = 1000.f;
				state.fieldLength = 1000.f;
				state.fieldHeight = 50.f;						// Altitude already in meters
			}

			state.fieldAspectRatio = state.fieldWidth / state.fieldLength;

			if (debug.world && debug.detailed)
			{
				System.out.print("Model.setup()... Field Width:"+state.lowLongitude+" Field Length:"+state.fieldLength+" Field Height:"+state.fieldHeight);	
				System.out.println("Model.setup()... Field Area:"+state.fieldArea+" Media Density:"+state.mediaDensity);
			}
		}
		else 
			if(debug.world) 
				System.out.println("Model.setup()... No media loaded! Couldn't initialize field...");
	}

	/**
	 * Update current world and viewer settings
	 * @param currentWorldSettings Current world settings
	 * @param currentWorldState Current world state
	 * @param currentViewerSettings Current viewer settings
	 * @param currentViewerState Current viewer state
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
	 * Find duplicate media in clusters
	 * @param clusters Clusters to search for duplicates
	 */
	public void findDuplicateClusterMedia(ArrayList<WMV_Cluster> clusters)
	{
		List<Integer> images = new ArrayList<Integer>();
		int count = 0;
		for(WMV_Cluster c : clusters)
		{
			for(int i : c.getState().images)
			{
				if(images.contains(i)) count++;
				else images.add(i);
			}
		}
		System.out.println("Images in more than one cluster::"+count);

		List<Integer> panoramas = new ArrayList<Integer>();
		count = 0;
		for(WMV_Cluster c : clusters)
		{
			for(int n : c.getState().panoramas)
			{
				if(panoramas.contains(n)) count++;
				else panoramas.add(n);
			}
		}
		System.out.println("Panoramas in more than one cluster::"+count);

		List<Integer> videos = new ArrayList<Integer>();
		count = 0;
		for(WMV_Cluster c : clusters)
		{
			for(int v : c.getState().videos)
			{
				if(videos.contains(v)) count++;
				else videos.add(v);
			}
		}
		System.out.println("Videos in more than one cluster:"+count);
		

		List<Integer> sounds = new ArrayList<Integer>();
		count = 0;
		for(WMV_Cluster c : clusters)
		{
			for(int s : c.getState().sounds)
			{
				if(sounds.contains(s)) count++;
				else sounds.add(s);
			}
		}
		System.out.println("Sounds in more than one cluster::"+count);
	}
	
	/**
	 * Get list of dendrogram clusters at given depth
	 * @param topCluster Top dendrogram cluster
	 * @param depth Dendrogram depth level
	 * @return List of clusters at given depth level
	 */
	public ArrayList<Cluster> getDendrogramClusters( Cluster topCluster, int depth )
	{
		ArrayList<Cluster> clusters = (ArrayList<Cluster>) topCluster.getChildren();	// Dendrogram clusters

		for( int i = 0; i<depth; i++ )								// For each level up to given depth
		{
			ArrayList<Cluster> nextDepth = new ArrayList<Cluster>();	// List of clusters at this depth

			for( Cluster c : clusters )								// For all clusters at current depth
			{
				ArrayList<Cluster> children = (ArrayList<Cluster>) c.getChildren();
				for(Cluster d : children)								// Save children to nextDepth list
					nextDepth.add(d);										
			}

			clusters = nextDepth;										// Move to next depth
		}	

		if(debug.cluster) 
			System.out.println("Getting "+clusters.size()+" dendrogram clusters at depth:"+depth);

		return clusters;
	}

	/**
	 * Analyze media locations to determine dimensions of the virtual space
	 * @param images Image list
	 * @param panoramas Panorama list
	 * @param videos Video list
	 * @param sounds Sounds list
	 */
	 void analyzeSpatialDimensions(ArrayList<WMV_Image> images, ArrayList<WMV_Panorama> panoramas, ArrayList<WMV_Video> videos, ArrayList<WMV_Sound> sounds) 
	 {
		 if(debug.world) System.out.println("Calculating field dimensions...");

		 boolean init = true;	

		 for (WMV_Image i : images) 
		 {
			 if (init) 	// Initialize high and low longitude
			 {	
				 state.highLongitude = i.getMediaState().gpsLocation.x;
				 state.lowLongitude = i.getMediaState().gpsLocation.x;
			 }
			 if (init) 	// Initialize high and low altitude
			 {		
				 state.highAltitude = i.getMediaState().gpsLocation.y;
				 state.lowAltitude = i.getMediaState().gpsLocation.y;
				 init = false;
			 }
			 if (init) 	// Initialize high and low latitude
			 {	
				 state.highLatitude = i.getMediaState().gpsLocation.z;
				 state.lowLatitude = i.getMediaState().gpsLocation.z;
			 }

			 if(i.getDebugSettings().gps && i.getDebugSettings().detailed)
			 {
				 if(init) System.out.println("Model.analyzeSpatialDimensions()... ");
				 System.out.println(" Image #"+i.getID()+" mGPSLocation.x: "+i.getMediaState().gpsLocation.x+" mGPSLocation.y: "+i.getMediaState().gpsLocation.y+
				 					" mGPSLocation.z: "+i.getMediaState().gpsLocation.z+" name:"+i.getName());
				 System.out.println(" > gpsLocation.x: "+i.getGPSLocation().x+" gpsLocation.y: "+i.getGPSLocation().y+" gpsLocation.z: "+i.getGPSLocation().z);
			 }

//			 if(i.getMediaState().gpsLocation.x == 0.f)
//			 {
//				 System.out.println("ERROR:   image #"+i.getID()+" gpsLocation.x == "+i.getMediaState().gpsLocation.x+" name:"+i.getName());
//			 }

			 if (i.getMediaState().gpsLocation.x > state.highLongitude)
				 state.highLongitude = i.getMediaState().gpsLocation.x;
			 if (i.getMediaState().gpsLocation.x < state.lowLongitude)
				 state.lowLongitude = i.getMediaState().gpsLocation.x;
			 if (i.getMediaState().gpsLocation.y > state.highAltitude)
				 state.highAltitude = i.getMediaState().gpsLocation.y;
			 if (i.getMediaState().gpsLocation.y < state.lowAltitude)
				 state.lowAltitude = i.getMediaState().gpsLocation.y;
			 if (i.getMediaState().gpsLocation.z > state.highLatitude)
				 state.highLatitude = i.getMediaState().gpsLocation.z;
			 if (i.getMediaState().gpsLocation.z < state.lowLatitude)
				 state.lowLatitude = i.getMediaState().gpsLocation.z;
		 }
		 
		 for (WMV_Panorama n : panoramas) 				
		 {
			 if (n.getMediaState().gpsLocation.x > state.highLongitude)
				 state.highLongitude = n.getMediaState().gpsLocation.x;
			 if (n.getMediaState().gpsLocation.x < state.lowLongitude)
				 state.lowLongitude = n.getMediaState().gpsLocation.x;
			 if (n.getMediaState().gpsLocation.y > state.highAltitude)
				 state.highAltitude = n.getMediaState().gpsLocation.y;
			 if (n.getMediaState().gpsLocation.y < state.lowAltitude)
				 state.lowAltitude = n.getMediaState().gpsLocation.y;
			 if (n.getMediaState().gpsLocation.z > state.highLatitude)
				 state.highLatitude = n.getMediaState().gpsLocation.z;
			 if (n.getMediaState().gpsLocation.z < state.lowLatitude)
				 state.lowLatitude = n.getMediaState().gpsLocation.z;
		 }

		 for (WMV_Video v : videos) 						
		 {
			 if (v.getMediaState().gpsLocation.x > state.highLongitude)
				 state.highLongitude = v.getMediaState().gpsLocation.x;
			 if (v.getMediaState().gpsLocation.x < state.lowLongitude)
				 state.lowLongitude = v.getMediaState().gpsLocation.x;
			 if (v.getMediaState().gpsLocation.y > state.highAltitude)
				 state.highAltitude = v.getMediaState().gpsLocation.y;
			 if (v.getMediaState().gpsLocation.y < state.lowAltitude)
				 state.lowAltitude = v.getMediaState().gpsLocation.y;
			 if (v.getMediaState().gpsLocation.z > state.highLatitude)
				 state.highLatitude = v.getMediaState().gpsLocation.z;
			 if (v.getMediaState().gpsLocation.z < state.lowLatitude)
				 state.lowLatitude = v.getMediaState().gpsLocation.z;
		 }
	 
		 if(sounds != null)
		 {
			 for (WMV_Sound s : sounds) 							
			 {
				 if (s.getMediaState().gpsLocation.x > state.highLongitude)
					 state.highLongitude = s.getMediaState().gpsLocation.x;
				 if (s.getMediaState().gpsLocation.x < state.lowLongitude)
					 state.lowLongitude = s.getMediaState().gpsLocation.x;
				 if (s.getMediaState().gpsLocation.y > state.highAltitude)
					 state.highAltitude = s.getMediaState().gpsLocation.y;
				 if (s.getMediaState().gpsLocation.y < state.lowAltitude)
					 state.lowAltitude = s.getMediaState().gpsLocation.y;
				 if (s.getMediaState().gpsLocation.z > state.highLatitude)
					 state.highLatitude = s.getMediaState().gpsLocation.z;
				 if (s.getMediaState().gpsLocation.z < state.lowLatitude)
					 state.lowLatitude = s.getMediaState().gpsLocation.z;
			 }
		 }
		 
		 if(state.lowAltitude == 1000000.f && state.highAltitude != -1000000.f) 			// Adjust for fields with no altitude variation
			 state.lowAltitude = state.highAltitude;
		 else if(state.highAltitude == -1000000.f && state.lowAltitude != 1000000.f) 
			 state.highAltitude = state.lowAltitude;

		 if (debug.world && debug.detailed) 							// Display results for debugging
		 {
			 System.out.println("Model.analyzeSpatialDimensions()... High Longitude:" + state.highLongitude+" High Latitude:" + state.highLatitude);
			 System.out.println("Model.analyzeSpatialDimensions()... High Altitude:" + state.highAltitude);
			 System.out.println("Model.analyzeSpatialDimensions()... Low Longitude:" + state.lowLongitude+" Low Latitude:" + state.lowLatitude);
			 System.out.println("Model.analyzeSpatialDimensions()... Low Altitude:" + state.lowAltitude);
		 }
	 }
	 
	 /**
	  * Whether field contains a point in GPS coordinates {longitude, latitude}
	  * @param gpsPoint {longitude, latitude}
	  */
	 public boolean containsGPSPoint(PVector gpsPoint)
	 {
		 if ( gpsPoint.x < state.highLongitude && gpsPoint.x > state.lowLongitude && 
			  gpsPoint.y < state.highLatitude && gpsPoint.y > state.lowLatitude )
			 return true;
		 else
			 return false;
	 }
	 
	 /**
	  * Analyze media capture times, calculate time / date limits
	  * @param images Image list
	  * @param panoramas Panorama list
	  * @param videos Video list
	  * @param sounds Sound list
	  */
	 public void analyzeTimeDimensions(ArrayList<WMV_Image> images, ArrayList<WMV_Panorama> panoramas, ArrayList<WMV_Video> videos, ArrayList<WMV_Sound> sounds) 
	 {
		 float longestImageDayLength = (float) -1.;			// Length of the longest day
		 boolean initImageTime = true, initImageDate = true;
		 boolean initPanoTime = true, initPanoDate = true;	
		 boolean initVideoTime = true, initVideoDate = true;	
		 boolean initSoundTime = true, initSoundDate = true;	

		 if(debug.world && debug.detailed) System.out.println("Analyzing media in field...");

		 for (WMV_Image i : images) 			// Iterate over images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		 {
			 if (initImageTime) 	// Calculate most recent and oldest image time
			 {		
				 state.highImageTime = i.time.getAbsoluteTime();
				 state.lowImageTime = i.time.getAbsoluteTime();
				 initImageTime = false;
			 }

			 if (initImageDate)  	// Calculate most recent and oldest image date
			 {	
				 state.highImageDate = i.time.asDate().getDaysSince1980();
				 state.lowImageDate = i.time.asDate().getDaysSince1980();
				 initImageDate = false;
			 }

			 if (i.time.getAbsoluteTime() > state.highImageTime)
				 state.highImageTime = i.time.getAbsoluteTime();
			 if (i.time.getAbsoluteTime() < state.lowImageTime)
				 state.lowImageTime = i.time.getAbsoluteTime();

			 if (i.time.asDate().getDaysSince1980() > state.highImageDate)
				 state.highImageDate = i.time.asDate().getDaysSince1980();
			 if (i.time.asDate().getDaysSince1980() < state.lowImageDate)
				 state.lowImageDate = i.time.asDate().getDaysSince1980();
		 }

		 for (WMV_Panorama n : panoramas) 			// Iterate over images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		 {
			 if (initPanoTime) 	// Calculate most recent and oldest Pano time
			 {		
				 state.highPanoTime = n.time.getAbsoluteTime();
				 state.lowPanoTime = n.time.getAbsoluteTime();
				 initPanoTime = false;
			 }

			 if (initPanoDate)  	// Calculate most recent and oldest Pano date
			 {	
				 state.highPanoDate = n.time.asDate().getDaysSince1980();
				 state.lowPanoDate = n.time.asDate().getDaysSince1980();
				 initPanoDate = false;
			 }

			 if (n.time.getAbsoluteTime() > state.highPanoTime)
				 state.highPanoTime = n.time.getAbsoluteTime();
			 if (n.time.getAbsoluteTime() < state.lowPanoTime)
				 state.lowPanoTime = n.time.getAbsoluteTime();

			 if (n.time.asDate().getDaysSince1980() > state.highPanoDate)
				 state.highPanoDate = n.time.asDate().getDaysSince1980();
			 if (n.time.asDate().getDaysSince1980() < state.lowPanoDate)
				 state.lowPanoDate = n.time.asDate().getDaysSince1980();
		 }
		 
		 for ( WMV_Video v : videos ) 			// Iterate over videos to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		 {
			 if (initVideoTime) 		// Calculate most recent and oldest video time
			 {		
				 state.highVideoTime = v.time.getAbsoluteTime();
				 state.lowVideoTime = v.time.getAbsoluteTime();
				 initVideoTime = false;
			 }

			 if (initVideoDate) 		// Calculate most recent and oldest image date
			 {		
				 state.highVideoDate = v.time.asDate().getDaysSince1980();
				 state.lowVideoDate = v.time.asDate().getDaysSince1980();
				 initVideoDate = false;
			 }

			 if (v.time.getAbsoluteTime() > state.highVideoTime)
				 state.highVideoTime = v.time.getAbsoluteTime();
			 if (v.time.getAbsoluteTime() < state.lowVideoTime)
				 state.lowVideoTime = v.time.getAbsoluteTime();

			 if (v.time.asDate().getDaysSince1980() > state.highVideoDate)
				 state.highVideoDate = v.time.asDate().getDaysSince1980();
			 if (v.time.asDate().getDaysSince1980() < state.lowVideoDate)
				 state.lowVideoDate = v.time.asDate().getDaysSince1980();
		 }
		 
		 for ( WMV_Sound s : sounds ) 			// Iterate over videos to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		 {
			 if (initSoundTime) 		// Calculate most recent and oldest video time
			 {		
				 state.highSoundTime = s.time.getAbsoluteTime();
				 state.lowSoundTime = s.time.getAbsoluteTime();
				 initSoundTime = false;
			 }

			 if (initSoundDate) 		// Calculate most recent and oldest image date
			 {		
				 state.highSoundDate = s.time.asDate().getDaysSince1980();
				 state.lowSoundDate = s.time.asDate().getDaysSince1980();
				 initSoundDate = false;
			 }

			 if (s.time.getAbsoluteTime() > state.highSoundTime)
				 state.highSoundTime = s.time.getAbsoluteTime();
			 if (s.time.getAbsoluteTime() < state.lowSoundTime)
				 state.lowSoundTime = s.time.getAbsoluteTime();

			 if (s.time.asDate().getDaysSince1980() > state.highSoundDate)
				 state.highSoundDate = s.time.asDate().getDaysSince1980();
			 if (s.time.asDate().getDaysSince1980() < state.lowSoundDate)
				 state.lowSoundDate = s.time.asDate().getDaysSince1980();
		 }

		 state.lowTime = state.lowImageTime;
		 if (state.lowPanoTime < state.lowTime)
			 state.lowTime = state.lowPanoTime;
		 if (state.lowVideoTime < state.lowTime)
			 state.lowTime = state.lowVideoTime;
		 if (state.lowSoundTime < state.lowTime)
			 state.lowTime = state.lowSoundTime;

		 state.highTime = state.highImageTime;
		 if (state.highPanoTime > state.highTime)
			 state.highTime = state.highPanoTime;
		 if (state.highVideoTime > state.highTime)
			 state.highTime = state.highVideoTime;
		 if (state.highSoundTime > state.highTime)
			 state.highTime = state.highSoundTime;

		 state.lowDate = state.lowImageDate;
		 if (state.lowPanoDate < state.lowDate)
			 state.lowDate = state.lowPanoDate;
		 if (state.lowVideoDate < state.lowDate)
			 state.lowDate = state.lowVideoDate;
		 if (state.lowSoundDate < state.lowDate)
			 state.lowDate = state.lowSoundDate;

		 state.highDate = state.highImageDate;
		 if (state.highPanoDate > state.highDate)
			 state.highDate = state.highPanoDate;
		 if (state.highVideoDate > state.highDate)
			 state.highDate = state.highVideoDate;
		 if (state.highSoundDate > state.highDate)
			 state.highDate = state.highSoundDate;

		 if (debug.media) 							// Display results for debugging
		 {
			 System.out.println("Model.analyzeTimeDimensions():");
			 if(state.highImageTime != -1000000.f) System.out.println(" High Image Time:" + state.highImageTime);
			 if(state.highImageDate != -1000000.f) System.out.println(" High Image Date:" + state.highImageDate);
			 if(state.highPanoTime != -1000000.f) System.out.println(" High Panorama Time:" + state.highPanoTime);
			 if(state.highPanoDate != -1000000.f) System.out.println(" High Panorama Date:" + state.highPanoDate);
			 if(state.highVideoTime != -1000000.f) System.out.println(" High Video Time:" + state.highVideoTime);
			 if(state.highVideoDate != -1000000.f) System.out.println(" High Video Date:" + state.highVideoDate);
			 if(state.highSoundTime != -1000000.f) System.out.println(" High Sound Time:" + state.highSoundTime);
			 if(state.highSoundDate != -1000000.f) System.out.println(" High Sound Date:" + state.highSoundDate);
		 }
	 }

	 /**
	  * Calculate average of given point list
	  * @param points List of points to average
	  * @return Average point 
	  */
	 public PVector calculateAveragePoint(ArrayList<PVector> points) 
	 {
		 PVector result = new PVector(0, 0, 0);
		 for (PVector p : points) 
			 result.add(p);

		 result.div(points.size());
		 return result;
	 }

	 /**
	  * Set model state
	  * @param newState New model state
	  */
	 public void setState(WMV_ModelState newState)
	 {
		 state = newState;
	 }

	 /**
	  * Set min cluster distance
	  * @param newMinClusterDistance New min cluster distance
	  */
	public void setMinClusterDistance(float newMinClusterDistance)
	{
		state.minClusterDistance = newMinClusterDistance;	
	}
	
	/**
	 * Set max cluster distance
	 * @param newMaxClusterDistance New max cluster distance
	 */
	public void setMaxClusterDistance(float newMaxClusterDistance)
	{
		state.maxClusterDistance = newMaxClusterDistance;	
	}
	
	/**
	 * @return Current model state
	 */
	public WMV_ModelState getState()
	{
		return state;
	}
}
