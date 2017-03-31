package multimediaLocator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.apporiented.algorithm.clustering.*;
//import com.apporiented.algorithm.clustering.ClusteringAlgorithm;

//import java.util.List;

import processing.core.PApplet;
import processing.core.PVector;
import processing.data.IntList;

/******************
 * @author davidgordon
 * Spatial and temporal model of a 3D virtual environment
 */

public class WMV_Model
{
	/* Field */
	public float fieldWidth; 			// Width (X) of GPS photo area (real world)
	public float fieldHeight; 			// Height (Y) of GPS photo area (real world)
	public float fieldLength; 			// Length (Z) of GPS photo area	
	public float fieldArea;				// Field width * height
	public float fieldAspectRatio = 1; 	// Aspect ratio

	/* Media */
	public int validImages, validPanoramas, validVideos;	// Number of valid images / number of valid videos
	public int validMedia;									// Total valid media count
	public float mediaDensity;								// Number of images as a function of field area

	/* Clustering */
	public int mergedClusters = 0;							// Number of merged clusters
	public float minClusterDistance; 						// Minimum distance between clusters, i.e. closer than which clusters are merged
	public float maxClusterDistance;						// Maximum distance between clusters, i.e. farther than which single image clusters are created (set based on mediaDensity)

	/* K-Means Clustering */
	public float clusterPopulationFactor = 10.f;					// Scaling from media spread (1/mediaDensity) to numClusters
	public float minPopulationFactor = 1.f, maxPopulationFactor = 30.f;	// Minimum and maximum values of populationFactor
	public int clusterRefinement = 60;								// Number of iterations used to refine clusters
	public int minClusterRefinement = 20, maxClusterRefinement = 300;	// Minimum and maximum values of clusterRefinement
	public long clusteringRandomSeed = (long)0.f;
	public boolean clustersNeedUpdate = false;				// --NEED TO IMPLEMENT
	
	/* Hierarchical Clustering */
	private Cluster dendrogramTop;							// Top cluster of the dendrogram
	public boolean dendrogramCreated = false;				// Dendrogram has been created
	private String[] names;									// Media names
	private double[][] distances;
	private int indexPanoramaOffset, indexVideoOffset;		// Start of panoramas / videos in names and distances arrays

	public int deepestLevel = -1;	
	public int defaultClusterDepth = 8;						// How deep in the dendrogram to look for media?	-- Set based on deepestLevel?
	public int minClusterDepth = 2;
	public int clusterDepth = defaultClusterDepth;			// How deep in the dendrogram to look for media?
	private IntList clustersByDepth;						// Number of clusters at each dendrogram depth

	/** Metadata **/
	public float highLongitude = -1000000, lowLongitude = 1000000, highLatitude = -1000000, lowLatitude = 1000000,
			highAltitude = -1000000, lowAltitude = 1000000;
	private float highTime = -1000000, lowTime = 1000000;
	private float highDate = -1000000, lowDate = 1000000;
	private float highImageTime = -1000000, lowImageTime = 1000000;
	private float highImageDate = -1000000, lowImageDate = 1000000;
	private float highPanoTime = -1000000, lowPanoTime = 1000000;
	private float highPanoDate = -1000000, lowPanoDate = 1000000;
	private float highVideoTime = -1000000, lowVideoTime = 1000000;
	private float highVideoDate = -1000000, lowVideoDate = 1000000;
	private float longestImageDayLength = -1000000, longestPanoDayLength = -1000000, longestVideoDayLength = -1000000;	

	/*** Misc. ***/
	public int minFrameRate = 15;

	WMV_Field p;
	WMV_Model(WMV_Field parent)
	{
		p = parent;
		
		clustersByDepth = new IntList();
		clusteringRandomSeed = (long) p.p.p.random(1000.f);
	}

	/**
	 * Initialize virtual space based on media GPS capture locations
	 */
	void setup()		 // Initialize field 
	{
		if (p.getImages().size() > 0 || p.getPanoramas().size() > 0 || p.getVideos().size() > 0)
		{
			float midLongitude = (highLongitude - lowLongitude) / 2.f;
			float midLatitude = (highLatitude - lowLatitude) / 2.f;

			if(p.p.p.debug.field) PApplet.println("Initializing model for field #"+p.fieldID+"...");

			validImages = p.getImageCount();
			validPanoramas = p.getPanoramaCount();
			validVideos = p.getVideoCount();
			validMedia = validImages + validPanoramas + validVideos;
			
			if(validMedia > 1)
			{
				fieldWidth = p.p.p.utilities.gpsToMeters(midLatitude, highLongitude, midLatitude, lowLongitude);
				fieldLength = p.p.p.utilities.gpsToMeters(highLatitude, midLongitude, lowLatitude, midLongitude);
				fieldHeight = highAltitude - lowAltitude;					
			}
			else
			{
				fieldWidth = 1000.f;
				fieldLength = 1000.f;
				fieldHeight = 1000.f;
			}
			
			fieldArea = fieldWidth * fieldLength;				// Use volume instead?
			mediaDensity = validMedia / fieldArea;				// Media per sq. m.

			/* Increase maxClusterDistance as mediaDensity decreases */
			if(p.p.autoClusterDistances)
			{
				maxClusterDistance = p.p.settings.maxClusterDistanceConstant / mediaDensity;
				if(maxClusterDistance > minClusterDistance * p.p.settings.maxClusterDistanceFactor)
					maxClusterDistance = minClusterDistance * p.p.settings.maxClusterDistanceFactor;
			}
			else
			{
				setMinClusterDistance(p.p.settings.minClusterDistance); 				// Minimum distance between clusters, i.e. closer than which clusters are merged
				setMaxClusterDistance(p.p.settings.maxClusterDistance);				// Maximum distance between clusters, i.e. farther than which single image clusters are created (set based on mediaDensity)
			}

			//			for(GMV_Image i : p.images)						
			//				i.maxClusterDistance = max;
			//			for(GMV_Panorama n : p.panoramas)				
			//				n.maxClusterDistance = max;
			//			for(GMV_Video v : p.videos)						
			//				v.maxClusterDistance = max;

			if(p.p.p.debug.cluster)
				PApplet.println("------> Set maxClusterDistance:"+maxClusterDistance);

			if(highLongitude == -1000000 || lowLongitude == 1000000 || highLatitude == -1000000 || lowLatitude == 1000000)	// If field dimensions aren't initialized
			{
				fieldWidth = 1000.f;
				fieldLength = 1000.f;
				fieldHeight = 50.f;						// Altitude already in meters
			}

			if(p.getImageCount() == 1)
			{
				fieldWidth = 1000.f;
				fieldLength = 1000.f;
				fieldHeight = 50.f;						// Altitude already in meters
			}

			fieldAspectRatio = fieldWidth / fieldLength;

			if (p.p.p.debug.model)
			{
				PApplet.print("Field Width:"+fieldWidth);
				PApplet.print(" Field Length:"+fieldLength);
				PApplet.println(" Field Height:"+fieldHeight);	
				PApplet.println("Field Area:"+fieldArea);

				PApplet.println("Media Density:"+mediaDensity);
				//				PApplet.println("Cluster Density:"+clusterDensity);
				//				PApplet.println("Old Number of Clusters:"+((int) (p.numImages * 1/24.f)));
			}
		}
		else 
		{
			if(p.p.p.debug.field) 
			{
				PApplet.println("No images loaded! Couldn't initialize field...");
				PApplet.println("p.getPanoramas().size():"+p.getPanoramas().size());
			}
		}
	}

	public void analyzeClusterMediaDirections()
	{
		for(WMV_Cluster c : p.getClusters())
			if(!c.isEmpty())
				c.analyzeMediaDirections();
	}
	
	/** 
	 * Create clusters for all media in field at startup	
	 */
	void runInitialClustering() 					
	{
		if(p.p.p.debug.cluster || p.p.p.debug.model)
			PApplet.println("Running initial clustering for field: "+p.getName());

		clustersByDepth = new IntList();

		/* Calculate number of valid media points */
		validImages = p.getImageCount();
		validPanoramas = p.getPanoramaCount();
		validVideos = p.getVideoCount();
		validMedia = validImages + validPanoramas + validVideos;				

		if(validMedia < 20)
			p.p.hierarchical = true;
		
		if(p.p.hierarchical)						// If using hierarchical clustering
		{
			runHierarchicalClustering();			// Create dendrogram
			setDendrogramDepth( clusterDepth );		// Set initial dendrogram depth and initialize clusters
		}
		else										// If using k-means clustering
		{
			runKMeansClustering( p.p.settings.kMeansClusteringEpsilon, clusterRefinement, clusterPopulationFactor );	// Get initial clusters using K-Means method
			p.setClusters( cleanupClusters(p.getClusters()) );
		}

		if(p.p.p.debug.cluster || p.p.p.debug.model)
			p.p.display.message("Created "+getClusterAmount()+" clusters...");
		
		for(WMV_Cluster c : p.getClusters())
		{
			if(p.p.p.debug.model && !c.isEmpty())
				PApplet.println("Cluster #"+c.getID()+" has "+c.images.size()+" media points...");
		}
	}
	
	/**
	 * Build the dendrogram and calculate clusters at each depth
	 */
	public void runHierarchicalClustering()
	{
		buildDendrogram();								// Build dendrogram 
		calculateClustersByDepth( dendrogramTop );		// Calculate number of clusters in dendrogram at each depth
		dendrogramCreated = true;
	}

	/**
	 * Run k-means clustering on media in field to find capture locations
	 * @param epsilon Minimum cluster movement 
	 * @param refinement Number of iterations to refine clusters
	 * @param populationFactor Cluster population factor
	 */
	public void runKMeansClustering(float epsilon, int refinement, float populationFactor)
	{
		p.setClusters( new ArrayList<WMV_Cluster>() );			// Clear current cluster list

		/* Display Status */
		if(!p.p.display.initialSetup)
		{
			p.p.display.clearMessages();
			p.p.display.message("Running K-Means Clustering...");
			p.p.display.message(" ");
			p.p.display.message("  Iterations:"+refinement);
			p.p.display.message("  Population Factor:"+populationFactor);
			if(p.p.mergeClusters)
			{
				p.p.display.message("");
				p.p.display.message("Cluster Merging:");
				p.p.display.message("  Minimum Cluster Distance:"+p.p.settings.minClusterDistance);
				p.p.display.message("  Maximum Cluster Distance:"+p.p.settings.maxClusterDistance);
			}
			p.p.display.message(" ");
			p.p.display.displayClusteringInfo();
		}
		
//		if(mediaDensity < XXX)			// ---Split into fields here

		/* Estimate number of clusters */
		int numClusters = PApplet.round( (1.f / PApplet.sqrt(mediaDensity)) * populationFactor ); 	// Calculate numClusters from media density

		if(p.p.p.debug.cluster && p.p.display.initialSetup)
			PApplet.println("Creating "+numClusters+" initial clusters based on "+validMedia+" valid media...");

//		boolean test = false;
//		if(numClusters - validMedia > 10000)
//		{
//			test = true;;
//			numClusters = validMedia;
//		}
		
		/* K-means Clustering */
		if (validMedia > 1) 							// If there are more than 1 media point
		{
			initializeKMeansClusters(numClusters);		// Create initial clusters at random image locations	
			refineKMeansClusters(epsilon, refinement);	// Refine clusters over many iterations
			createSingleClusters();						// Create clusters for single media points
			
			p.initializeClusters();						// Initialize clusters (merge, etc.)

			if(p.getClusters().size() > 0)					// Calculate capture times for each cluster
				findVideoPlaceholders();
		}
		else
		{
			if (p.getImages().size() == 0 && p.getPanoramas().size() == 0 && p.getVideos().size() == 0) 		// If there are 0 media
			{
				p.p.display.message("No media loaded!  Can't run k-means clustering... Will exit.");
				p.p.p.exit();
			}
			else
			{
				if(p.p.p.debug.cluster)
					p.p.display.message("Single media point scene...");
			}
		}
		
		if(!p.p.display.initialSetup)
		{
			p.p.display.message(" ");
			p.p.display.message("Created "+numClusters+" Clusters...");
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

		if(p.p.p.debug.cluster)
			PApplet.println("--- Getting GMV_Clusters at depth "+depth+" ---");

		if(dendrogramTop != null)
		{
			for(int d=0; d<depth; d++)
			{
				ArrayList<Cluster> dClusters = getDendrogramClusters( dendrogramTop, d );	// Get clusters in dendrogram up to given depth
				for(Cluster c : dClusters)
					clusters.add(c);
			}
		}
		else
		{
			if(p.p.p.debug.cluster)
				PApplet.println("Top cluster is null!");
			p.p.p.exit();
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
				if(!p.p.p.utilities.isInteger(parts[0], 10))
				{
					if(p.p.p.debug.cluster)
						PApplet.println("Media name error! "+name);
				}
				else isMedia = true;
			}
			else if( parts.length == 2 )				
			{
				if(!p.p.p.utilities.isInteger(parts[1], 10))
				{
					if(p.p.p.debug.cluster)
						PApplet.println("Cluster name error! "+name);
				}
			}
			else
			{
				if(p.p.p.debug.cluster)
					PApplet.println("Media or cluster name error! "+name);
			}

			PVector location;

			if(isMedia)
			{
				if(p.p.p.debug.cluster && p.p.p.debug.detailed)
					PApplet.println("Cluster "+cluster.getName()+" is a media file..."+name);

				mediaIdx = Integer.parseInt(name);

				if( mediaIdx < indexPanoramaOffset )
				{
					WMV_Image i = p.getImage(mediaIdx);
					location = i.getCaptureLocation();
					images.append(i.getID());
				}
				else if( mediaIdx < indexVideoOffset )
				{
					WMV_Panorama n = p.getPanoramas().get(mediaIdx - indexPanoramaOffset);
					location = n.getCaptureLocation();
					panoramas.append(n.getID());
				}
				else
				{
					WMV_Video v = p.getVideo(mediaIdx - indexVideoOffset);
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
						mediaPoints.add(p.getImage(i).getCaptureLocation());
				}
				if(panoramas.size() > 1)
				{
					for(int i : images)
						mediaPoints.add(p.getImage(i).getCaptureLocation());
				}
				if(videos.size() > 1)
				{
					for(int v : videos)
						mediaPoints.add(p.getVideo(v).getCaptureLocation());
				}

				location = calculateAveragePoint(mediaPoints);					// Calculate cluster location from average of media points

				if(p.p.p.debug.cluster && p.p.p.debug.detailed)
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
				
				for(WMV_Image i : p.getImages())
					if(i.cluster == oldID)
						i.setAssociatedCluster(count);
				for(WMV_Panorama n : p.getPanoramas())
					if(n.cluster == oldID)
						n.setAssociatedCluster(count);
				for(WMV_Video v : p.getVideos())
					if(v.cluster == oldID)
						v.setAssociatedCluster(count);
				for(WMV_Sound s : p.getSounds())
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
		if(p.p.p.debug.model) PApplet.println("cleanupClusters()... Removed "+removed+" clusters from field #"+p.fieldID);
		
		return result;
	}

	/**
	 * setDendrogramDepth()
	 * @param depth New cluster depth
	 */
	void setDendrogramDepth(int depth)
	{
		clusterDepth = depth;
		p.setClusters( createClustersFromDendrogram( depth ) );	// Get clusters at defaultClusterDepth	 -- Set this based on media density

		for (int i = 0; i < p.getImages().size(); i++) 			// Find closest cluster for each image
			p.getImage(i).findAssociatedCluster(maxClusterDistance);
		for (int i = 0; i < p.getPanoramas().size(); i++) 			// Find closest cluster for each image
			p.getPanorama(i).findAssociatedCluster(maxClusterDistance);
		for (int i = 0; i < p.getVideos().size(); i++) 			// Find closest cluster for each video
			p.getVideo(i).findAssociatedCluster(maxClusterDistance);

		if(p.getClusters().size() > 0)							// Find image place holders
			findVideoPlaceholders();

		if(!p.p.display.initialSetup)
		{
			/* Display Status */
			p.p.display.clearMessages();
			p.p.display.message("Hierarchical Clustering Mode");
			p.p.display.message(" ");
			p.p.display.message("Cluster Depth:"+clusterDepth);
			p.p.display.message(" ");
			p.p.display.displayClusteringInfo();
			p.p.display.message("Found "+p.getClusters().size()+" clusters...");
		}
		
		p.initializeClusters();					// Initialize clusters in Hierarchical Clustering Mode	 (Already done during k-means clustering)
	}

	void findDuplicateClusterMedia()
	{
		IntList images = new IntList();
		int count = 0;
		for(WMV_Cluster c : p.getClusters())
		{
			for(int i : c.images)
			{
				if(images.hasValue(i))
					count++;
				else
					images.append(i);
			}
		}
		PApplet.println("Images in more than one cluster::"+count);
		
		IntList videos = new IntList();
		count = 0;
		for(WMV_Cluster c : p.getClusters())
		{
			for(int v : c.videos)
			{
				if(videos.hasValue(v))
					count++;
				else
					videos.append(v);
			}
		}
		PApplet.println("Videos in more than one cluster:"+count);
	}
	
	/**
	 * @param top Dendrogram cluster to find associated media in
	 * @param mediaType Media type, 0: image 1: panorama 2: video
	 * @return List of cluster names of associated media 
	 */
	public IntList getMediaInCluster( Cluster top, int mediaType )
	{
		ArrayList<Cluster> clusters = (ArrayList<Cluster>) top.getChildren();	// Get clusters in topCluster
		int mediaCount = 0;														// Number of images successfully found
		int depthCount = 0;														// Depth reached from top cluster
		IntList result = new IntList();

		if((clusters.size() == 0) || clusters == null)							// If the top cluster has no children, it is a media file														
		{
			String name = top.getName();										// Otherwise, save the result if appropriate
			int mediaIdx = Integer.parseInt(name);

			if(p.p.p.debug.cluster)
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
		else if(clusters.size() > 0)											 // Otherwise, it is a cluster of media files
		{
			boolean deepest = false;
			depthCount++;														// Move to next dendrogram level

			if(p.p.p.debug.cluster && p.p.p.debug.detailed)
				PApplet.println("Searching for media in cluster: "+top.getName()+"...");

			while(!deepest)														// Until the deepest level
			{
				ArrayList<Cluster> children = new ArrayList<Cluster>();			// List of children
				ArrayList<Cluster> nextDepth = new ArrayList<Cluster>();		// List of clusters at next depth level

				for( Cluster cluster : clusters )								
				{
					children = (ArrayList<Cluster>) cluster.getChildren();		// For each cluster, look for its children 
					if(children.size() > 0)										// If there are children
					{
						for( Cluster c : children )								// Add to nextDepth clusters
							nextDepth.add(c);

						if(p.p.p.debug.cluster && p.p.p.debug.detailed)
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
					clusters = children;						// Move down one depth level 
					depthCount++;
				}
			}
		}

		if(p.p.p.debug.cluster && p.p.p.debug.detailed && mediaCount > 0)
			PApplet.println( "Found "+mediaCount+" media at depth "+depthCount+" result.size():"+result.size() );

		return result;
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

		if(p.p.p.debug.cluster) 
			PApplet.println("Getting "+clusters.size()+" dendrogram clusters at depth:"+depth);

		return clusters;
	}

	/**
	 * @param topCluster Top cluster of dendrogram
	 * Add to clustersByDepth list all dendrogram clusters at given depth 
	 */
	public void calculateClustersByDepth( Cluster topCluster )
	{
		ArrayList<Cluster> clusters = (ArrayList<Cluster>) topCluster.getChildren();	// Dendrogram clusters
		int depthCount = 0;

		if(p.p.p.debug.cluster) PApplet.println("Counting clusters at all depth levels...");
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

				if(p.p.p.debug.cluster && p.p.p.debug.detailed) PApplet.println("Found "+clusters.size()+" clusters at depth:"+depthCount);

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
	 * Calculate hieararchical clustering dendrogram for all media in field
	 */
	void buildDendrogram()
	{
		int namesIdx = 0, distIdx = 0;
		indexPanoramaOffset = p.getImages().size();
		indexVideoOffset = indexPanoramaOffset + p.getPanoramas().size();

		int size = p.getImages().size() + p.getVideos().size();
		names = new String[size];
		distances = new double[size][size];		

		PApplet.println("Creating dendrogram...");

		/* Calculate distances between each image and all other media */
		for(WMV_Image i : p.getImages())		
		{
			namesIdx = i.getID();
			names[namesIdx] = Integer.toString(namesIdx);

			for(WMV_Image j : p.getImages())
			{
				if(i != j)				// Don't compare image with itself
				{
					distIdx = j.getID();
					distances[namesIdx][distIdx] = PVector.dist(i.getCaptureLocation(), j.getCaptureLocation());
				}
			}

			for(WMV_Panorama n : p.getPanoramas())
			{
				distIdx = n.getID() + indexPanoramaOffset;
				distances[namesIdx][distIdx] = PVector.dist(i.getCaptureLocation(), n.getCaptureLocation());
			}

			for(WMV_Video v : p.getVideos())
			{
				distIdx = v.getID() + indexVideoOffset;
				distances[namesIdx][distIdx] = PVector.dist(i.getCaptureLocation(), v.getCaptureLocation());
			}
		}

		/* Calculate distances between each panorama and all other media */
		for(WMV_Panorama n : p.getPanoramas())		
		{
			namesIdx = n.getID() + indexPanoramaOffset;
			names[namesIdx] = Integer.toString(namesIdx);

			for(WMV_Image i : p.getImages())
			{
				distIdx = i.getID();
				distances[namesIdx][distIdx] = PVector.dist(n.getCaptureLocation(), i.getCaptureLocation());
			}

			for(WMV_Panorama o : p.getPanoramas())
			{
				if(n != o)				// Don't compare panorama with itself
				{
					distIdx = n.getID() + indexPanoramaOffset;
					distances[namesIdx][distIdx] = PVector.dist(n.getCaptureLocation(), n.getCaptureLocation());
				}
			}

			for(WMV_Video v : p.getVideos())
			{
				distIdx = v.getID() + indexVideoOffset;
				distances[namesIdx][distIdx] = PVector.dist(n.getCaptureLocation(), v.getCaptureLocation());
			}
		}

		/* Calculate distances between each video and all other media */
		for(WMV_Video v : p.getVideos())		
		{
			namesIdx = v.getID() + indexVideoOffset;
			names[namesIdx] = Integer.toString(namesIdx);

			for(WMV_Image i : p.getImages())
			{
				distIdx = i.getID();
				distances[namesIdx][distIdx] = PVector.dist(v.getCaptureLocation(), i.getCaptureLocation());
			}

			for(WMV_Panorama n : p.getPanoramas())
			{
				distIdx = n.getID() + indexPanoramaOffset;
				distances[namesIdx][distIdx] = PVector.dist(v.getCaptureLocation(), n.getCaptureLocation());
			}

			for(WMV_Video u : p.getVideos())
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
				if(p.p.p.utilities.isNaN(d))
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
	 * initializeKMeansClusters()
	 * Create initial clusters at random image locations	 			-- Need to: record random seed, account for associated videos
	 */	
	void initializeKMeansClusters( int numClusters )
	{
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
				p.p.p.randomSeed(clusteringRandomSeed);
				imageID = (int) p.p.p.random(p.getImages().size());  			// Random image ID for setting cluster's start location				
				panoramaID = (int) p.p.p.random(p.getPanoramas().size());  		// Random panorama ID for setting cluster's start location				
				videoID = (int) p.p.p.random(p.getVideos().size());  			// Random video ID for setting cluster's start location				
				addedImages.append(imageID);								
				addedPanoramas.append(panoramaID);								
				addedVideos.append(videoID);								

				/* Record media nearby added media*/
				for(WMV_Image img : p.getImages())						// Check for images near the picked one
				{
					float dist = img.getCaptureDistanceFrom(p.getImage(imageID).getCaptureLocation());  // Get distance
					if(dist < minClusterDistance)
						nearImages.append(img.getID());				// Record images nearby picked image
				}

				for(WMV_Panorama pano : p.getPanoramas())				// Check for panoramas near the picked one 
				{
					float dist = pano.getCaptureDistanceFrom(p.getPanoramas().get(panoramaID).getCaptureLocation());  // Get distance
					if(dist < minClusterDistance)
						nearPanoramas.append(pano.getID());			// Add to the list of nearby picked images
				}

				for(WMV_Video vid : p.getVideos())						// Check for videos near the picked one
				{
					float dist = vid.getCaptureDistanceFrom(p.getVideo(videoID).getCaptureLocation());  // Get distance
					if(dist < minClusterDistance)
						nearVideos.append(vid.getID());				// Add to the list of nearby picked images
				}

				/* Create the cluster */
				PVector clusterPoint = new PVector(0,0,0);
				if(p.getImages().size() > 0)
				{
					clusterPoint = new PVector(p.getImage(imageID).getCaptureLocation().x, p.getImage(imageID).getCaptureLocation().y, p.getImage(imageID).getCaptureLocation().z); // Choose random image location to start
					p.addCluster(new WMV_Cluster(p, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
					i++;
				}
				if(p.getPanoramas().size() > 0)
				{
					clusterPoint = new PVector(p.getPanoramas().get(panoramaID).getCaptureLocation().x, p.getPanoramas().get(panoramaID).getCaptureLocation().y, p.getPanoramas().get(panoramaID).getCaptureLocation().z); // Choose random image location to start
					p.addCluster(new WMV_Cluster(p, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
					i++;
				}
				if(p.getVideos().size() > 0)
				{
					clusterPoint = new PVector(p.getVideo(videoID).getCaptureLocation().x, p.getVideo(videoID).getCaptureLocation().y, p.getVideo(videoID).getCaptureLocation().z); // Choose random image location to start
					p.addCluster(new WMV_Cluster(p, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
					i++;
				}

				if(i > 0)
					i--;
				else if(p.p.p.debug.model)
					PApplet.println("Error in initClusters()... No media!!");
			}
			else															// Find a random media (image, panorama or video) location for new cluster
			{
				int mediaID = (int) p.p.p.random(p.getImages().size() + p.getPanoramas().size() + p.getVideos().size());
				PVector clusterPoint = new PVector(0,0,0);

				if( mediaID < p.getImages().size() )				// If image, compare to already picked images
				{
					imageID = (int) p.p.p.random(p.getImages().size());  						
					while(addedImages.hasValue(imageID) && nearImages.hasValue(imageID))
						imageID = (int) p.p.p.random(p.getImages().size());  						

					addedImages.append(imageID);

					clusterPoint = new PVector(p.getImage(imageID).getCaptureLocation().x, p.getImage(imageID).getCaptureLocation().y, p.getImage(imageID).getCaptureLocation().z); // Choose random image location to start
				}
				else if( mediaID < p.getImages().size() + p.getPanoramas().size() )		// If panorama, compare to already picked panoramas
				{
					panoramaID = (int) p.p.p.random(p.getPanoramas().size());  						
					while(addedPanoramas.hasValue(panoramaID) && nearPanoramas.hasValue(panoramaID))
						panoramaID = (int) p.p.p.random(p.getPanoramas().size());  						

					addedPanoramas.append(panoramaID);

					clusterPoint = new PVector(p.getPanoramas().get(panoramaID).getCaptureLocation().x, p.getPanoramas().get(panoramaID).getCaptureLocation().y, p.getPanoramas().get(panoramaID).getCaptureLocation().z); // Choose random image location to start
				}
				else if( mediaID < p.getImages().size() + p.getPanoramas().size() + p.getVideos().size() )		// If video, compare to already picked videos
				{
					videoID = (int) p.p.p.random(p.getVideos().size());  						
					while(addedImages.hasValue(videoID) && nearImages.hasValue(videoID))
						videoID = (int) p.p.p.random(p.getVideos().size());  						

					addedVideos.append(videoID);

					clusterPoint = new PVector(p.getVideo(videoID).getCaptureLocation().x, p.getVideo(videoID).getCaptureLocation().y, p.getVideo(videoID).getCaptureLocation().z); // Choose random image location to start
				}

				p.addCluster(new WMV_Cluster(p, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
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
		
		ArrayList<WMV_Cluster> last = p.getClusters();
		if(p.p.p.debug.cluster || p.p.p.debug.model)
			PApplet.println("--> Refining clusters...");
		
		while( count < iterations ) 							// Iterate to create the clusters
		{		
			for (int i = 0; i < p.getImages().size(); i++) 			// Find closest cluster for each image
				p.getImage(i).findAssociatedCluster(maxClusterDistance);		// Set associated cluster
			for (int i = 0; i < p.getPanoramas().size(); i++) 		// Find closest cluster for each image
				p.getPanorama(i).findAssociatedCluster(maxClusterDistance);		// Set associated cluster
			for (int i = 0; i < p.getVideos().size(); i++) 			// Find closest cluster for each image
				p.getVideo(i).findAssociatedCluster(maxClusterDistance);		// Set associated cluster
			for (int i = 0; i < p.getClusters().size(); i++) 		// Find closest cluster for each image
				p.getCluster(i).create();						// Assign clusters

			if(p.getClusters().size() == last.size())				// Check cluster movement
			{
				for(WMV_Cluster c : p.getClusters())
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
					if(p.p.p.debug.cluster || p.p.p.debug.model)
						PApplet.println(" Stopped refinement... no clusters moved farther than epsilon:"+epsilon);
					break;								// If all clusters moved less than epsilon, stop refinement
				}
			}
			else
			{
				if(p.p.p.debug.cluster || p.p.p.debug.model)
					PApplet.println(" New clusters found... will keep refining clusters... clusters.size():"+p.getClusters().size()+" last.size():"+last.size());
			}
			
			count++;
		}
	}

	/**
	 * Merge together clusters with closest neighbor below minClusterDistance threshold
	 */
	void mergeAdjacentClusters()
	{
		mergedClusters = 0;			// Reset mergedClusters count

		IntList[] closeNeighbors = new IntList[ p.getClusters().size() ];			// List array of closest neighbor distances for each cluster 
		ArrayList<PVector> mostNeighbors = new ArrayList<PVector>();			// List of clusters with most neighbors and number of neighbors as PVector(id, neighborCount)
		IntList absorbed = new IntList();										// List of clusters absorbed into other clusters
		IntList merged = new IntList();											// List of clusters already merged with neighbors
		float firstMergePct = 0.2f;												// Fraction of clusters with most neighbors to merge first
		
		if((p.p.p.debug.cluster || p.p.p.debug.model ) && p.p.p.debug.detailed) PApplet.println("Merging adjacent clusters... ");

		for( WMV_Cluster c : p.getClusters() )					// Find distances of close neighbors to each cluster
		{
			closeNeighbors[c.getID()] = new IntList();	// Initialize list for this cluster
			for( WMV_Cluster d : p.getClusters() )
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
		for( WMV_Cluster c : p.getClusters() )					// Find distances of close neighbors for each cluster
		{
			if(count < p.getClusters().size() * firstMergePct )		// Fill array with initial clusters 
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
			if(p.p.p.debug.cluster && v.y > 0 && p.p.p.debug.detailed)
				PApplet.println("Merging cluster "+(int)v.x+" with "+(int)v.y+" neighbors...");

			WMV_Cluster c = p.getCluster( (int)v.x );
			if(!merged.hasValue(c.getID()))
			{
				for(int i : closeNeighbors[c.getID()])
				{
					if(!absorbed.hasValue(i) && c.getID() != i) 		// If cluster i hasn't already been absorbed and isn't the same cluster
					{
						c.absorbCluster(p.getCluster(i));				// Absorb cluster
						absorbed.append(i);

						merged.append(i);
						merged.append(c.getID());
						mergedClusters++;
					}
				}
			}
		}

		for( WMV_Cluster c : p.getClusters() )					// Merge remaining clusters under minClusterDistance 
		{
			if(!merged.hasValue(c.getID()))
			{
				for( WMV_Cluster d : p.getClusters() )
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

		if(p.p.p.debug.cluster)
			PApplet.println("Merged Clusters "+mergedClusters);
	}


	/** 
	 * If any images are not associated, create a new cluster for each
	 */	void createSingleClusters()
	 {
		 int newClusterID = p.getClusters().size();	// Start adding clusters at end of current list 
		 int initial = newClusterID;

		 for (WMV_Image i : p.getImages()) 			// Find closest cluster for each image
		 {
			 if(i.cluster == -1)				// Create cluster for each single image
			 {
				 p.addCluster(new WMV_Cluster(p, newClusterID, i.getCaptureLocation().x, i.getCaptureLocation().y, i.getCaptureLocation().z));
				 i.setAssociatedCluster(newClusterID);

				 p.getCluster(newClusterID).createSingle(i.getID(), 0);
				 newClusterID++;
			 }
		 }

		 for (WMV_Panorama n : p.getPanoramas()) 						// Find closest cluster for each image
		 {
			 if(n.cluster == -1)				// Create cluster for each single image
			 {
				 p.addCluster(new WMV_Cluster(p, newClusterID, n.getCaptureLocation().x, n.getCaptureLocation().y, n.getCaptureLocation().z));
				 n.setAssociatedCluster(newClusterID);

				 p.getCluster(newClusterID).createSingle(n.getID(), 1);
				 newClusterID++;
			 }
		 }

		 for (WMV_Video v : p.getVideos()) 						// Find closest cluster for each image
		 {
			 if(v.cluster == -1)				// Create cluster for each single image
			 {
				 p.addCluster(new WMV_Cluster(p, newClusterID, v.getCaptureLocation().x, v.getCaptureLocation().y, v.getCaptureLocation().z));
				 v.setAssociatedCluster(newClusterID);

				 p.getCluster(newClusterID).createSingle(v.getID(), 2);
				 newClusterID++;
			 }
		 }

		 if(p.p.p.debug.cluster)
			 PApplet.println("Created "+(newClusterID-initial)+" clusters from single images...");
	 }

	 /** 
	  * Find video placeholder images, i.e. images taken just before a video to indicate same location, orientation, elevation and rotation angles
	  */	
	 public void findVideoPlaceholders()
	 {
		 for (int i = 0; i < p.getVideos().size(); i++) 		
		 {
			 WMV_Video v = p.getVideo(i);
			 if(!v.disabled)
			 {
				 int id = v.getImagePlaceholder();				// Find associated image with each video

				 if(id != -1)
				 {
					 v.cluster = p.getImage(id).cluster;	// Set video cluster to cluster of associated image
					 p.getCluster(v.cluster).video = true;	// Set cluster video property to true
					 if(p.p.p.debug.video)
						 PApplet.println("Image placeholder for video: "+i+" is:"+id+" p.getCluster(v.cluster).video:"+p.getCluster(v.cluster).video);
				 }
				 else
				 {
					 if(p.p.p.debug.video)
						 PApplet.println("No image placeholder found for video: "+i+" p.getCluster(v.cluster).video:"+p.getCluster(v.cluster).video);
					 v.disabled = true;
				 }
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
		 WMV_Cluster gmvc = new WMV_Cluster(p, index, location.x, location.y, location.z);

		 /* Add media to cluster */
		 for( int i : images )
		 {
			 gmvc.addImage(p.getImage(i));
			 gmvc.mediaCount++;
		 }
		 for( int n : panoramas )
		 {
			 gmvc.addPanorama(p.getPanoramas().get(n));
			 gmvc.mediaCount++;
		 }
		 for( int v : videos )
		 {
			 gmvc.addVideo(p.getVideo(v));
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
	  * @param location New cluster location
	  * @param index New cluster ID
	  * @return New empty cluster
	  */
	 public WMV_Cluster createEmptyCluster( PVector location, int index )
	 {
		 if(location != null)
		 {
			 WMV_Cluster gmvc = new WMV_Cluster(p, index, location.x, location.y, location.z);
			 return gmvc;
		 }
		 return null;
	 }

	 /**
	  * Analyze media to determine size of the virtual space
	  */
	 void calculateFieldSize() 
	 {
		 if(p.p.p.debug.field) PApplet.println("Calculating field dimensions...");

		 boolean init = true;	

		 for (WMV_Image i : p.getImages()) 							// Iterate over images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		 {
			 if (init) 	// Initialize high and low longitude
			 {	
				 highLongitude = i.gpsLocation.x;
				 lowLongitude = i.gpsLocation.x;
			 }
			 if (init) 	// Initialize high and low latitude
			 {	
				 highLatitude = i.gpsLocation.z;
				 lowLatitude = i.gpsLocation.z;
			 }
			 if (init) 	// Initialize high and low altitude
			 {		
				 highAltitude = i.gpsLocation.y;
				 lowAltitude = i.gpsLocation.y;
				 init = false;
			 }

			 if (i.gpsLocation.x > highLongitude)
				 highLongitude = i.gpsLocation.x;
			 if (i.gpsLocation.x < lowLongitude)
				 lowLongitude = i.gpsLocation.x;
			 if (i.gpsLocation.y > highAltitude)
				 highAltitude = i.gpsLocation.y;
			 if (i.gpsLocation.y < lowAltitude)
				 lowAltitude = i.gpsLocation.y;
			 if (i.gpsLocation.z > highLatitude)
				 highLatitude = i.gpsLocation.z;
			 if (i.gpsLocation.z < lowLatitude)
				 lowLatitude = i.gpsLocation.z;
		 }

		 for (WMV_Panorama n : p.getPanoramas()) 							// Iterate over images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		 {
			 if (n.gpsLocation.x > highLongitude)
				 highLongitude = n.gpsLocation.x;
			 if (n.gpsLocation.x < lowLongitude)
				 lowLongitude = n.gpsLocation.x;
			 if (n.gpsLocation.y > highAltitude)
				 highAltitude = n.gpsLocation.y;
			 if (n.gpsLocation.y < lowAltitude)
				 lowAltitude = n.gpsLocation.y;
			 if (n.gpsLocation.z > highLatitude)
				 highLatitude = n.gpsLocation.z;
			 if (n.gpsLocation.z < lowLatitude)
				 lowLatitude = n.gpsLocation.z;
		 }

		 for (WMV_Video v : p.getVideos()) 							// Iterate over images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		 {
			 if (v.gpsLocation.x > highLongitude)
				 highLongitude = v.gpsLocation.x;
			 if (v.gpsLocation.x < lowLongitude)
				 lowLongitude = v.gpsLocation.x;
			 if (v.gpsLocation.y > highAltitude)
				 highAltitude = v.gpsLocation.y;
			 if (v.gpsLocation.y < lowAltitude)
				 lowAltitude = v.gpsLocation.y;
			 if (v.gpsLocation.z > highLatitude)
				 highLatitude = v.gpsLocation.z;
			 if (v.gpsLocation.z < lowLatitude)
				 lowLatitude = v.gpsLocation.z;
		 }

		 if (p.p.p.debug.model) 							// Display results for debugging
		 {
			 System.out.println("High Longitude:" + highLongitude);
			 System.out.println("High Latitude:" + highLatitude);
			 System.out.println("High Altitude:" + highAltitude);
			 System.out.println("Low Longitude:" + lowLongitude);
			 System.out.println("Low Latitude:" + lowLatitude);
			 System.out.println("Low Altitude:" + lowAltitude);
		 }
	 }

	 /**
	  * Analyze media capture times, find on which scales it operates, i.e. minute, day, month, year   (not implemented yet)
	  */
	 public void analyzeMedia() 
	 {
		 float longestImageDayLength = (float) -1.;			// Length of the longest day
		 boolean initImageTime = true, initImageDate = true;
		 boolean initPanoTime = true, initPanoDate = true;	
		 boolean initVideoTime = true, initVideoDate = true;	

		 if(p.p.p.debug.field) PApplet.println("Analyzing media in field...");

		 for ( WMV_Video v : p.getVideos() ) 			// Iterate over videos to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		 {
			 if (initVideoTime) 		// Calculate most recent and oldest video time
			 {		
				 highVideoTime = v.time.getTime();
				 lowVideoTime = v.time.getTime();
				 initVideoTime = false;
			 }

			 if (initVideoDate) 		// Calculate most recent and oldest image date
			 {		
				 highVideoDate = v.time.getDate().getDaysSince1980();
				 lowVideoDate = v.time.getDate().getDaysSince1980();
				 initVideoDate = false;
			 }

			 if (v.time.getTime() > highVideoTime)
				 highVideoTime = v.time.getTime();
			 if (v.time.getTime() < lowVideoTime)
				 lowVideoTime = v.time.getTime();

			 if (v.time.getDate().getDaysSince1980() > highVideoDate)
				 highVideoDate = v.time.getDate().getDaysSince1980();
			 if (v.time.getDate().getDaysSince1980() < lowVideoDate)
				 lowVideoDate = v.time.getDate().getDaysSince1980();

//			 if (v.time.getDayLength() > longestVideoDayLength)		// Calculate longest video day length
//				 longestVideoDayLength = v.time.getDayLength();
		 }

		 for (WMV_Image i : p.getImages()) 			// Iterate over images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		 {
			 if (initImageTime) 	// Calculate most recent and oldest image time
			 {		
				 highImageTime = i.time.getTime();
				 lowImageTime = i.time.getTime();
				 initImageTime = false;
			 }

			 if (initImageDate)  	// Calculate most recent and oldest image date
			 {	
				 highImageDate = i.time.getDate().getDaysSince1980();
				 lowImageDate = i.time.getDate().getDaysSince1980();
				 initImageDate = false;
			 }

			 if (i.time.getTime() > highImageTime)
				 highImageTime = i.time.getTime();
			 if (i.time.getTime() < lowImageTime)
				 lowImageTime = i.time.getTime();

			 if (i.time.getDate().getDaysSince1980() > highImageDate)
				 highImageDate = i.time.getDate().getDaysSince1980();
			 if (i.time.getDate().getDaysSince1980() < lowImageDate)
				 lowImageDate = i.time.getDate().getDaysSince1980();

//			 if (i.time.getDayLength() > longestImageDayLength)		// Calculate longest day length
//				 longestImageDayLength = i.time.getDayLength();
		 }

		 for (WMV_Panorama i : p.getPanoramas()) 			// Iterate over images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		 {
			 if (initPanoTime) 	// Calculate most recent and oldest Pano time
			 {		
				 highPanoTime = i.time.getTime();
				 lowPanoTime = i.time.getTime();
				 initPanoTime = false;
			 }

			 if (initPanoDate)  	// Calculate most recent and oldest Pano date
			 {	
				 highPanoDate = i.time.getDate().getDaysSince1980();
				 lowPanoDate = i.time.getDate().getDaysSince1980();
				 initPanoDate = false;
			 }

			 if (i.time.getTime() > highPanoTime)
				 highPanoTime = i.time.getTime();
			 if (i.time.getTime() < lowPanoTime)
				 lowPanoTime = i.time.getTime();

			 if (i.time.getDate().getDaysSince1980() > highPanoDate)
				 highPanoDate = i.time.getDate().getDaysSince1980();
			 if (i.time.getDate().getDaysSince1980() < lowPanoDate)
				 lowPanoDate = i.time.getDate().getDaysSince1980();

//			 if (i.time.getDayLength() > longestPanoDayLength)		// Calculate longest day length
//				 longestPanoDayLength = i.time.getDayLength();
		 }

		 lowTime = lowImageTime;
		 if (lowPanoTime < lowTime)
			 lowTime = lowPanoTime;
		 if (lowVideoTime < lowTime)
			 lowTime = lowVideoTime;

		 highTime = highImageTime;
		 if (highPanoTime > highTime)
			 highTime = highPanoTime;
		 if (highVideoTime > highTime)
			 highTime = highVideoTime;

		 lowDate = lowImageDate;
		 if (lowPanoDate < lowDate)
			 lowDate = lowPanoDate;
		 if (lowVideoDate < lowDate)
			 lowDate = lowVideoDate;

		 highDate = highImageDate;
		 if (highPanoDate > highDate)
			 highDate = highPanoDate;
		 if (highVideoDate > highDate)
			 highDate = highVideoDate;

		 if (p.p.p.debug.metadata) 							// Display results for debugging
		 {
			 System.out.println("High Image Time:" + highImageTime);
			 System.out.println("High Image Date:" + highImageDate);
			 System.out.println("High Panorama Time:" + highPanoTime);
			 System.out.println("High Panorama Date:" + highPanoDate);
			 System.out.println("High Video Time:" + highVideoTime);
			 System.out.println("High Video Date:" + highVideoDate);
			 System.out.println("Longest Image Day Length:" + longestImageDayLength);
			 System.out.println("Longest Panorama Day Length:" + longestPanoDayLength);
			 System.out.println("Longest Video Day Length:" + longestVideoDayLength);
		 }
	 }

	 /**
	  * If image is within <threshold> from center of cluster along axes specified by mx, my and mz, 
	  * fold the image location into the cluster location along those axes.
	  */
	 public void lockMediaToClusters()
	 {
		 if(p.p.p.debug.field || p.p.p.debug.model) PApplet.println("lockMediaToClusters(): Moving media... ");
		 for (int i = 0; i < p.getImages().size(); i++) 
			 p.getImage(i).adjustCaptureLocation();		
		 for (int i = 0; i < p.getPanoramas().size(); i++) 
			 p.getPanorama(i).adjustCaptureLocation();		
		 for (int i = 0; i < p.getVideos().size(); i++) 
			 p.getVideo(i).adjustCaptureLocation();		
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

	 /**
	  * @return Number of clusters in field
	  */
	 public int getClusterAmount()
	 {
		 return p.getClusters().size() - mergedClusters;
	 }
	 
	void setMinClusterDistance(float newMinClusterDistance)
	{
		minClusterDistance = newMinClusterDistance;	
	}
	
	void setMaxClusterDistance(float newMaxClusterDistance)
	{
		maxClusterDistance = newMaxClusterDistance;	
	}
	
///--- TO DO!!
	/**  
	 * Export statistics on current field 
	 */
	void exportFieldInfo()
	{
        BufferedWriter writer = null;

		try {
            // Create temp. file
            String timeLog = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            File logFile = new File(timeLog);

            // This will output the full path where the file will be written to...
            System.out.println(logFile.getCanonicalPath());

            writer = new BufferedWriter(new FileWriter(logFile));
            writer.write("Field: "+p.getName());
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

//		/**
//		 * @param depth Depth at which to draw clusters
//		 * Draw the clusters at given depth
//		 */
//		void drawClustersAtDepth( Cluster topCluster, int depth )
//		{		 
//			PApplet.println("Drawing clusters at depth level:"+depth);
//			ArrayList<Cluster> clusters = (ArrayList<Cluster>) topCluster.getChildren();	// Dendrogram clusters
//	
//			for( int i = 0; i<depth; i++ )								// For each level up to given depth
//			{
//				ArrayList<Cluster> nextDepth = new ArrayList<Cluster>();	// List of clusters at this depth
//	
//				for( Cluster c : clusters )								// For all clusters at current depth
//				{
//					ArrayList<Cluster> children = (ArrayList<Cluster>) c.getChildren();
//					for(Cluster d : children)								// Save children to nextDepth list
//						nextDepth.add(d);										
//				}
//	
//				clusters = nextDepth;										// Move to next depth
//			}	
//	
//			for( Cluster c : clusters )
//			{
//				String name = c.getName();								// Otherwise, save the result if appropriate
//				int mediaIdx = Integer.parseInt(name);
//				PVector location;
//	
//				if(mediaIdx < indexPanoramaOffset)
//				{
//					location = p.getImage(mediaIdx).getCaptureLocation();
//				}
//				else if(mediaIdx < indexVideoOffset)
//				{
//					mediaIdx -= indexPanoramaOffset; 
//					location = p.getPanoramas().get(mediaIdx).getCaptureLocation();
//				}
//				else
//				{
//					mediaIdx -= indexVideoOffset; 
//					location = p.videos.get(mediaIdx).getCaptureLocation();
//				}
//	
//				p.p.display.message("Drawing point:");
//				p.p.display.drawMapPoint(location, 20.f, p.p.display.largeMapWidth, p.p.display.largeMapHeight, p.p.display.mapClusterHue, 255, 255, p.p.display.mapClusterHue);
//			}
//			if(p.p.p.debug.cluster) 
//				PApplet.println("Getting "+clusters.size()+" dendrogram clusters at depth:"+depth);
//		}

}
