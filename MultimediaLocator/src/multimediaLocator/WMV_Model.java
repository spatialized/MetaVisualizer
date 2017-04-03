package multimediaLocator;

import java.util.ArrayList;
import java.util.List;

import com.apporiented.algorithm.clustering.*;
import processing.core.PVector;

/******************
 * @author davidgordon
 * Spatial and temporal model of a 3D virtual environment
 */

public class WMV_Model
{
	/* General */
	WMV_WorldSettings worldSettings;	// World settings
	WMV_WorldState worldState;			// World state
	WMV_ViewerSettings viewerSettings;	// Viewer settings
	WMV_ViewerState viewerState;		// Viewer state
	ML_DebugSettings debugSettings;		// Debug settings
	WMV_ModelState state;
	WMV_Utilities utilities;			// Utility methods

//	/* Field */
//	public float fieldWidth; 			// Width (X) of GPS photo area (real world)
//	public float fieldHeight; 			// Height (Y) of GPS photo area (real world)
//	public float fieldLength; 			// Length (Z) of GPS photo area	
//	public float fieldArea;				// Field width * height
//	public float fieldAspectRatio = 1; 	// Aspect ratio
//
//	/* Clustering */
//	public int mergedClusters = 0;							// Number of merged clusters
//	public float minClusterDistance; 						// Minimum distance between clusters, i.e. closer than which clusters are merged
//	public float maxClusterDistance;						// Maximum distance between clusters, i.e. farther than which single image clusters are created (set based on mediaDensity)
//
//	public float clusterPopulationFactor = 10.f;						// Scaling from media spread (1/mediaDensity) to numClusters
//	public float minPopulationFactor = 1.f, maxPopulationFactor = 30.f;	// Minimum and maximum values of populationFactor
//	public int clusterRefinement = 60;									// Number of iterations used to refine clusters
//	public int minClusterRefinement = 20, maxClusterRefinement = 300;	// Minimum and maximum values of clusterRefinement
//	public long state.clusteringRandomSeed = (long)0.f;
//	public boolean clustersNeedUpdate = false;				// --NEED TO IMPLEMENT
//	
//	/* Media */
//	public int validImages, validPanoramas, validVideos;	// Number of valid images / number of valid videos
//	public int validMedia;									// Total valid media count
//	public float mediaDensity;								// Number of images as a function of field area
//
//	/* Metadata */
//	public float state.highLongitude = -1000000, state.lowLongitude = 1000000, state.highLatitude = -1000000, state.lowLatitude = 1000000,
//			state.highAltitude = -1000000, state.lowAltitude = 1000000;
//	private float highTime = -1000000, lowTime = 1000000;
//	private float highDate = -1000000, lowDate = 1000000;
//	private float state.highImageTime = -1000000, lowImageTime = 1000000;
//	private float highImageDate = -1000000, lowImageDate = 1000000;
//	private float highPanoTime = -1000000, lowPanoTime = 1000000;
//	private float highPanoDate = -1000000, lowPanoDate = 1000000;
//	private float state.highVideoTime = -1000000, lowVideoTime = 1000000;
//	private float highVideoDate = -1000000, lowVideoDate = 1000000;
//	private float longestImageDayLength = -1000000, longestPanoDayLength = -1000000, longestVideoDayLength = -1000000;	
//
//	/* Time */
//	public int minFrameRate = 15;
//	public int frameCount = 0;

	WMV_Model(WMV_WorldSettings newWorldSettings, ML_DebugSettings newDebugSettings)
	{
		state = new WMV_ModelState();
		worldSettings = newWorldSettings;
		debugSettings = newDebugSettings;

		utilities = new WMV_Utilities();					// Utility methods

		state.clusteringRandomSeed = System.currentTimeMillis();
	}

	public void update( WMV_WorldSettings currentWorldSettings, WMV_WorldState currentWorldState, WMV_ViewerSettings currentViewerSettings, 
						WMV_ViewerState currentViewerState )
	{
		worldSettings = currentWorldSettings;	// Update world settings
		worldState = currentWorldState;	// Update world settings
		viewerSettings = currentViewerSettings;	// Update viewer settings
		viewerState = currentViewerState;		// Update viewer state
	}

	void findDuplicateClusterMedia(ArrayList<WMV_Cluster> clusters)
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
	}
	
	/**
	 * @param Dendrogram depth level
	 * @return List of dendrogram clusters at given depth level
	 */
	ArrayList<Cluster> getDendrogramClusters( Cluster topCluster, int depth )
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

		if(debugSettings.cluster) 
			System.out.println("Getting "+clusters.size()+" dendrogram clusters at depth:"+depth);

		return clusters;
	}

	 /**
	  * Analyze media to determine size of the virtual space
	  */
	 void calculateFieldSize(ArrayList<WMV_Image> images, ArrayList<WMV_Panorama> panoramas, ArrayList<WMV_Video> videos) 
	 {
		 if(debugSettings.field) System.out.println("Calculating field dimensions...");

		 boolean init = true;	

		 for (WMV_Image i : images) 							// Iterate over images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		 {
			 if (init) 	// Initialize high and low longitude
			 {	
				 state.highLongitude = i.getMediaState().gpsLocation.x;
				 state.lowLongitude = i.getMediaState().gpsLocation.x;
			 }
			 if (init) 	// Initialize high and low latitude
			 {	
				 state.highLatitude = i.getMediaState().gpsLocation.z;
				 state.lowLatitude = i.getMediaState().gpsLocation.z;
			 }
			 if (init) 	// Initialize high and low altitude
			 {		
				 state.highAltitude = i.getMediaState().gpsLocation.y;
				 state.lowAltitude = i.getMediaState().gpsLocation.y;
				 init = false;
			 }

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

		 for (WMV_Panorama n : panoramas) 							// Iterate over images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
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

		 for (WMV_Video v : videos) 							// Iterate over images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
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

		 if (debugSettings.field) 							// Display results for debugging
		 {
			 System.out.println("High Longitude:" + state.highLongitude);
			 System.out.println("High Latitude:" + state.highLatitude);
			 System.out.println("High Altitude:" + state.highAltitude);
			 System.out.println("Low Longitude:" + state.lowLongitude);
			 System.out.println("Low Latitude:" + state.lowLatitude);
			 System.out.println("Low Altitude:" + state.lowAltitude);
		 }
	 }

	 /**
	  * Analyze media locations and capture times; calculate farthest media and time / date limits
	  */
	 public void analyzeMedia(ArrayList<WMV_Image> images, ArrayList<WMV_Panorama> panoramas, ArrayList<WMV_Video> videos) 
	 {
		 float longestImageDayLength = (float) -1.;			// Length of the longest day
		 boolean initImageTime = true, initImageDate = true;
		 boolean initPanoTime = true, initPanoDate = true;	
		 boolean initVideoTime = true, initVideoDate = true;	

		 if(debugSettings.field) System.out.println("Analyzing media in field...");

		 for ( WMV_Video v : videos ) 			// Iterate over videos to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		 {
			 if (initVideoTime) 		// Calculate most recent and oldest video time
			 {		
				 state.highVideoTime = v.time.getTime();
				 state.lowVideoTime = v.time.getTime();
				 initVideoTime = false;
			 }

			 if (initVideoDate) 		// Calculate most recent and oldest image date
			 {		
				 state.highVideoDate = v.time.getDate().getDaysSince1980();
				 state.lowVideoDate = v.time.getDate().getDaysSince1980();
				 initVideoDate = false;
			 }

			 if (v.time.getTime() > state.highVideoTime)
				 state.highVideoTime = v.time.getTime();
			 if (v.time.getTime() < state.lowVideoTime)
				 state.lowVideoTime = v.time.getTime();

			 if (v.time.getDate().getDaysSince1980() > state.highVideoDate)
				 state.highVideoDate = v.time.getDate().getDaysSince1980();
			 if (v.time.getDate().getDaysSince1980() < state.lowVideoDate)
				 state.lowVideoDate = v.time.getDate().getDaysSince1980();

//			 if (v.time.getDayLength() > longestVideoDayLength)		// Calculate longest video day length
//				 longestVideoDayLength = v.time.getDayLength();
		 }

		 for (WMV_Image i : images) 			// Iterate over images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		 {
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

//			 if (i.time.getDayLength() > longestImageDayLength)		// Calculate longest day length
//				 longestImageDayLength = i.time.getDayLength();
		 }

		 for (WMV_Panorama n : panoramas) 			// Iterate over images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		 {
			 if (initPanoTime) 	// Calculate most recent and oldest Pano time
			 {		
				 state.highPanoTime = n.time.getTime();
				 state.lowPanoTime = n.time.getTime();
				 initPanoTime = false;
			 }

			 if (initPanoDate)  	// Calculate most recent and oldest Pano date
			 {	
				 state.highPanoDate = n.time.getDate().getDaysSince1980();
				 state.lowPanoDate = n.time.getDate().getDaysSince1980();
				 initPanoDate = false;
			 }

			 if (n.time.getTime() > state.highPanoTime)
				 state.highPanoTime = n.time.getTime();
			 if (n.time.getTime() < state.lowPanoTime)
				 state.lowPanoTime = n.time.getTime();

			 if (n.time.getDate().getDaysSince1980() > state.highPanoDate)
				 state.highPanoDate = n.time.getDate().getDaysSince1980();
			 if (n.time.getDate().getDaysSince1980() < state.lowPanoDate)
				 state.lowPanoDate = n.time.getDate().getDaysSince1980();

//			 if (i.time.getDayLength() > longestPanoDayLength)		// Calculate longest day length
//				 longestPanoDayLength = i.time.getDayLength();
		 }

		 state.lowTime = state.lowImageTime;
		 if (state.lowPanoTime < state.lowTime)
			 state.lowTime = state.lowPanoTime;
		 if (state.lowVideoTime < state.lowTime)
			 state.lowTime = state.lowVideoTime;

		 state.highTime = state.highImageTime;
		 if (state.highPanoTime > state.highTime)
			 state.highTime = state.highPanoTime;
		 if (state.highVideoTime > state.highTime)
			 state.highTime = state.highVideoTime;

		 state.lowDate = state.lowImageDate;
		 if (state.lowPanoDate < state.lowDate)
			 state.lowDate = state.lowPanoDate;
		 if (state.lowVideoDate < state.lowDate)
			 state.lowDate = state.lowVideoDate;

		 state.highDate = state.highImageDate;
		 if (state.highPanoDate > state.highDate)
			 state.highDate = state.highPanoDate;
		 if (state.highVideoDate > state.highDate)
			 state.highDate = state.highVideoDate;

		 if (debugSettings.metadata) 							// Display results for debugging
		 {
			 System.out.println("High Image Time:" + state.highImageTime);
			 System.out.println("High Image Date:" + state.highImageDate);
			 System.out.println("High Panorama Time:" + state.highPanoTime);
			 System.out.println("High Panorama Date:" + state.highPanoDate);
			 System.out.println("High Video Time:" + state.highVideoTime);
			 System.out.println("High Video Date:" + state.highVideoDate);
			 System.out.println("Longest Image Day Length:" + longestImageDayLength);
			 System.out.println("Longest Panorama Day Length:" + state.longestPanoDayLength);
			 System.out.println("Longest Video Day Length:" + state.longestVideoDayLength);
		 }
	 }

	 /**
	  * @param points List of points to average
	  * @return Average point 
	  */
	 public PVector calculateAveragePoint(ArrayList<PVector> points) 
	 {
		 PVector result = new PVector(0, 0, 0);
		 for (PVector p : points) 
		 {
			 result.add(p);
		 }

		 result.div(points.size());
		 return result;
	 }

	public void setMinClusterDistance(float newMinClusterDistance)
	{
		state.minClusterDistance = newMinClusterDistance;	
	}
	
	public void setMaxClusterDistance(float newMaxClusterDistance)
	{
		state.maxClusterDistance = newMaxClusterDistance;	
	}
	
	public WMV_ModelState getState()
	{
		return state;
	}
}
