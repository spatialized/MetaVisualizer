package gmViewer;
import java.util.ArrayList;
import com.apporiented.algorithm.clustering.*;
//import com.apporiented.algorithm.clustering.ClusteringAlgorithm;

//import java.util.List;

import processing.core.PApplet;
import processing.core.PVector;
import processing.data.IntList;

/******************
 * GMV_Model
 * @author davidgordon
 * Model of current environment based on metadata
 */

public class GMV_Model
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
//	public float highPanoLongitude = -1000000, lowPanoLongitude = 1000000, highPanoLatitude = -1000000,
//			lowPanoLatitude = 1000000, highPanoAltitude = -1000000, lowPanoAltitude = 1000000;
//	public float highVideoLongitude = -1000000, lowVideoLongitude = 1000000, highVideoLatitude = -1000000,
//			lowVideoLatitude = 1000000, highVideoAltitude = -1000000, lowVideoAltitude = 1000000;
	float highTime = -1000000, lowTime = 1000000;
	float highDate = -1000000, lowDate = 1000000;
	
	float highImageTime = -1000000, lowImageTime = 1000000;
	float highImageDate = -1000000, lowImageDate = 1000000;
	float highPanoTime = -1000000, lowPanoTime = 1000000;
	float highPanoDate = -1000000, lowPanoDate = 1000000;
	float highVideoTime = -1000000, lowVideoTime = 1000000;
	float highVideoDate = -1000000, lowVideoDate = 1000000;
	float longestImageDayLength = -1000000, longestPanoDayLength = -1000000, longestVideoDayLength = -1000000;	

	/*** Misc. ***/
	public int minFrameRate = 15;

	GMV_Field p;

	GMV_Model(GMV_Field parent)
	{
		p = parent;
		clustersByDepth = new IntList();
		clusteringRandomSeed = (long) p.p.random(1000.f);
	}

	/**
	 * setup()
	 * Initialize virtual space based on media GPS capture locations
	 */
	void setup()		 // Initialize field 
	{
		if (p.images.size() > 0 || p.panoramas.size() > 0 || p.videos.size() > 0)
		{
			float midLongitude = (highLongitude - lowLongitude) / 2.f;
			float midLatitude = (highLatitude - lowLatitude) / 2.f;

			if(p.p.debug.field) PApplet.println("Initializing model for field #"+p.fieldID+"...");

			validImages = p.getImageCount();
			validPanoramas = p.getPanoramaCount();
			validVideos = p.getVideoCount();
			validMedia = validImages + validPanoramas + validVideos;
//			PApplet.println("Valid Media:"+validMedia+" images.size()"+p.images.size()+" videos.size()"+p.videos.size());
			
			if(validMedia > 1)
			{
			fieldWidth = p.p.utilities.gpsToMeters(midLatitude, highLongitude, midLatitude, lowLongitude);
			fieldLength = p.p.utilities.gpsToMeters(highLatitude, midLongitude, lowLatitude, midLongitude);
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

			//			float fieldVolume = fieldWidth * fieldLength * fieldHeight;		// --Another possibility
			//			float fieldVolumeDensity = mediaNum / fieldVolume;

			/* Increase maxClusterDistance as mediaDensity decreases */
			if(p.p.autoClusterDistances)
			{
				maxClusterDistance = p.p.maxClusterDistanceConstant / mediaDensity;
				if(maxClusterDistance > minClusterDistance * p.p.maxClusterDistanceFactor)
					maxClusterDistance = minClusterDistance * p.p.maxClusterDistanceFactor;
			}
			else
			{
				setMinClusterDistance(p.p.minClusterDistance); 				// Minimum distance between clusters, i.e. closer than which clusters are merged
				setMaxClusterDistance(p.p.maxClusterDistance);				// Maximum distance between clusters, i.e. farther than which single image clusters are created (set based on mediaDensity)
			}

			//			for(GMV_Image i : p.images)						
			//				i.maxClusterDistance = max;
			//			for(GMV_Panorama n : p.panoramas)				
			//				n.maxClusterDistance = max;
			//			for(GMV_Video v : p.videos)						
			//				v.maxClusterDistance = max;

			if(p.p.debug.cluster)
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

			if (p.p.debug.model)
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
			if(p.p.debug.field) 
				PApplet.println("No images loaded! Couldn't initialize field.");
		}
	}

	public void analyzeClusterMediaDirections()
	{
		for(GMV_Cluster c : p.clusters)
			if(!c.isEmpty())
				c.analyzeMediaDirections();
	}
	
	/** 
	 * runInitialClustering()
	 * Create clusters for all media in field at startup	
	 */
	void runInitialClustering() 					
	{
		if(p.p.debug.cluster || p.p.debug.model)
			PApplet.println("Running initial clustering for field: "+p.name);

		clustersByDepth = new IntList();

		/* Calculate number of valid media points */
		validImages = p.getImageCount();
		validPanoramas = p.getPanoramaCount();
		validVideos = p.getVideoCount();
		validMedia = validImages + validPanoramas + validVideos;				

		if(p.p.hierarchical)						// If using hierarchical clustering
		{
			runHierarchicalClustering();			// Create dendrogram
			setDendrogramDepth( clusterDepth );		// Set initial dendrogram depth and initialize clusters
		}
		else										// If using k-means clustering
		{
			runKMeansClustering( p.p.kMeansClusteringEpsilon, clusterRefinement, clusterPopulationFactor );	// Get initial clusters using K-Means method
		}

		if(p.p.debug.cluster || p.p.debug.model)
			p.p.display.message("Created "+getClusterAmount()+" clusters...");
		
		for(GMV_Cluster c : p.clusters)
		{
			if(!c.isEmpty())
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
		p.clusters = new ArrayList<GMV_Cluster>();			// Clear current cluster list

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
				p.p.display.message("  Minimum Cluster Distance:"+p.p.minClusterDistance);
				p.p.display.message("  Maximum Cluster Distance:"+p.p.maxClusterDistance);
			}
			p.p.display.message(" ");
			p.p.display.displayClusteringInfo();
		}

		/* Estimate number of clusters */
		int numClusters = PApplet.round( (1.f / PApplet.sqrt(mediaDensity)) * populationFactor ); 	// Calculate numClusters from media density

		if(p.p.debug.cluster && p.p.display.initialSetup)
			PApplet.println("Creating "+numClusters+" initial clusters based on "+validMedia+" valid media...");

		/* K-means Clustering */
		if (validMedia > 1) 							// If there are more than 1 media point
		{
			initializeKMeansClusters(numClusters);		// Create initial clusters at random image locations	
			refineKMeansClusters(epsilon, refinement);			// Refine clusters over many iterations
			createSingleClusters();						// Create clusters for single media points
			
			p.initializeClusters();					// Initialize clusters (merge, etc.)

			if(p.clusters.size() > 0)				// Calculate capture times for each cluster
				findVideoPlaceholders();
		}
		else
		{
			if (p.images.size() == 0 && p.panoramas.size() == 0 && p.videos.size() == 0) 		// If there are 0 media
			{
				p.p.display.message("No media loaded!  Can't run k-means clustering... Will exit.");
				p.p.exit();
			}
			else
			{
				if(p.p.debug.cluster)
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
	 * createClustersFromDendrogram()
	 * @param depth Dendrogram depth level
	 * @return clusters GMV_Cluster list at given depth level based on dendrogram
	 */
	ArrayList<GMV_Cluster> createClustersFromDendrogram( int depth )
	{
		ArrayList<GMV_Cluster> gmvClusters = new ArrayList<GMV_Cluster>();	// GMV_Cluster list
		ArrayList<Cluster> clusters = new ArrayList<Cluster>();
		
		int imageCount = 0;
		int panoramaCount = 0;
		int videoCount = 0;

		if(p.p.debug.cluster)
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
			if(p.p.debug.cluster)
				PApplet.println("Top cluster is null!");
			p.p.exit();
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
				if(!p.p.utilities.isInteger(parts[0], 10))
				{
					if(p.p.debug.cluster)
						PApplet.println("Media name error! "+name);
				}
				else isMedia = true;
			}
			else if( parts.length == 2 )				
			{
				if(!p.p.utilities.isInteger(parts[1], 10))
				{
					if(p.p.debug.cluster)
						PApplet.println("Cluster name error! "+name);
				}
			}
			else
			{
				if(p.p.debug.cluster)
					PApplet.println("Media or cluster name error! "+name);
			}

			PVector location;

			if(isMedia)
			{
//				if(p.p.debug.cluster && p.p.debug.detailed)
//					PApplet.println("Cluster "+cluster.getName()+" is a media file..."+name);

				mediaIdx = Integer.parseInt(name);

				if( mediaIdx < indexPanoramaOffset )
				{
					GMV_Image i = p.images.get(mediaIdx);
					location = i.getCaptureLocation();
					images.append(i.getID());
				}
				else if( mediaIdx < indexVideoOffset )
				{
					GMV_Panorama n = p.panoramas.get(mediaIdx - indexPanoramaOffset);
					location = n.getCaptureLocation();
					panoramas.append(n.getID());
				}
				else
				{
					GMV_Video v = p.videos.get(mediaIdx - indexVideoOffset);
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
						mediaPoints.add(p.images.get(i).getCaptureLocation());
				}
				if(panoramas.size() > 1)
				{
					for(int i : images)
						mediaPoints.add(p.images.get(i).getCaptureLocation());
				}
				if(videos.size() > 1)
				{
					for(int v : videos)
						mediaPoints.add(p.videos.get(v).getCaptureLocation());
				}

				location = calculateAveragePoint(mediaPoints);					// Calculate cluster location from average of media points

//				if(p.p.debug.cluster && p.p.debug.detailed)
//					PApplet.println("Calculated Average Point: "+location);
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
	 * setDendrogramDepth()
	 * @param depth New cluster depth
	 */
	void setDendrogramDepth(int depth)
	{
		clusterDepth = depth;
		p.clusters = createClustersFromDendrogram( depth );	// Get clusters at defaultClusterDepth	 -- Set this based on media density

		for (int i = 0; i < p.images.size(); i++) 			// Find closest cluster for each image
			p.images.get(i).findAssociatedCluster();
		for (int i = 0; i < p.panoramas.size(); i++) 			// Find closest cluster for each image
			p.panoramas.get(i).findAssociatedCluster();
		for (int i = 0; i < p.videos.size(); i++) 			// Find closest cluster for each video
			p.videos.get(i).findAssociatedCluster();

		if(p.clusters.size() > 0)							// Find image place holders
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
			p.p.display.message("Found "+p.clusters.size()+" clusters...");
		}
		
		p.initializeClusters();					// Initialize clusters in Hierarchical Clustering Mode	 (Already done during k-means clustering)

// DEBUGGING
//		int empty = 0;
//		for( GMV_Cluster c : p.clusters )
//		{
//			if(c.mediaPoints <= 0)
//			{
////				c.emptyCluster();
//				empty++;
//			}
//		}
//		if(p.p.debug.cluster)
//			PApplet.println("Empty clusters:"+empty);

	}

	/**
	 * getMediaInCluster()
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

			if(p.p.debug.cluster)
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

			if(p.p.debug.cluster && p.p.debug.detailed)
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

						if(p.p.debug.cluster && p.p.debug.detailed)
						{
//							PApplet.print("  Cluster "+cluster.getName()+" has "+children.size()+" children at depth "+depthCount);
//							PApplet.println("  Added to next depth, array size:"+nextDepth.size()+"...");
						}
					}

					if(children.size() == 0 || (children == null))															
					{
//						if(p.p.debug.cluster && p.p.debug.detailed)
//							PApplet.println("  Cluster "+cluster.getName()+" has no children...");

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

		if(p.p.debug.cluster && p.p.debug.detailed && mediaCount > 0)
			PApplet.println( "Found "+mediaCount+" media at depth "+depthCount+" result.size():"+result.size() );

		return result;
	}

	/**
	 * getDendrogramClusters()
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

		if(p.p.debug.cluster) 
			PApplet.println("Getting "+clusters.size()+" dendrogram clusters at depth:"+depth);

		return clusters;
	}

	/**
	 * calculateClustersByDepth()
	 * @param topCluster Top cluster of dendrogram
	 * Add to clustersByDepth list all dendrogram clusters at given depth 
	 */
	public void calculateClustersByDepth( Cluster topCluster )
	{
		ArrayList<Cluster> clusters = (ArrayList<Cluster>) topCluster.getChildren();	// Dendrogram clusters
		int depthCount = 0;

		if(p.p.debug.cluster)
			PApplet.println("Counting clusters at all depth levels...");
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

				if(p.p.debug.cluster && p.p.debug.detailed)
					PApplet.println("Found "+clusters.size()+" clusters at depth:"+depthCount);

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
	 * buildDendrogram()
	 * Calculate hieararchical clustering dendrogram for all media in field
	 */
	void buildDendrogram()
	{
		int namesIdx = 0, distIdx = 0;
		indexPanoramaOffset = p.images.size();
		indexVideoOffset = indexPanoramaOffset + p.panoramas.size();

		int size = p.images.size() + p.videos.size();
		names = new String[size];
		distances = new double[size][size];		

		PApplet.println("Creating dendrogram...");

		/* Calculate distances between each image and all other media */
		for(GMV_Image i : p.images)		
		{
			namesIdx = i.getID();
			names[namesIdx] = Integer.toString(namesIdx);

			for(GMV_Image j : p.images)
			{
				if(i != j)				// Don't compare image with itself
				{
					distIdx = j.getID();
					distances[namesIdx][distIdx] = PVector.dist(i.getCaptureLocation(), j.getCaptureLocation());
				}
			}

			for(GMV_Panorama n : p.panoramas)
			{
				distIdx = n.getID() + indexPanoramaOffset;
				distances[namesIdx][distIdx] = PVector.dist(i.getCaptureLocation(), n.getCaptureLocation());
			}

			for(GMV_Video v : p.videos)
			{
				distIdx = v.getID() + indexVideoOffset;
				distances[namesIdx][distIdx] = PVector.dist(i.getCaptureLocation(), v.getCaptureLocation());
			}
		}

		/* Calculate distances between each panorama and all other media */
		for(GMV_Panorama n : p.panoramas)		
		{
			namesIdx = n.getID() + indexPanoramaOffset;
			names[namesIdx] = Integer.toString(namesIdx);

			for(GMV_Image i : p.images)
			{
				distIdx = i.getID();
				distances[namesIdx][distIdx] = PVector.dist(n.getCaptureLocation(), i.getCaptureLocation());
			}

			for(GMV_Panorama o : p.panoramas)
			{
				if(n != o)				// Don't compare panorama with itself
				{
					distIdx = n.getID() + indexPanoramaOffset;
					distances[namesIdx][distIdx] = PVector.dist(n.getCaptureLocation(), n.getCaptureLocation());
				}
			}

			for(GMV_Video v : p.videos)
			{
				distIdx = v.getID() + indexVideoOffset;
				distances[namesIdx][distIdx] = PVector.dist(n.getCaptureLocation(), v.getCaptureLocation());
			}
		}

		/* Calculate distances between each video and all other media */
		for(GMV_Video v : p.videos)		
		{
			namesIdx = v.getID() + indexVideoOffset;
			names[namesIdx] = Integer.toString(namesIdx);

			for(GMV_Image i : p.images)
			{
				distIdx = i.getID();
				distances[namesIdx][distIdx] = PVector.dist(v.getCaptureLocation(), i.getCaptureLocation());
			}

			for(GMV_Panorama n : p.panoramas)
			{
				distIdx = n.getID() + indexPanoramaOffset;
				distances[namesIdx][distIdx] = PVector.dist(v.getCaptureLocation(), n.getCaptureLocation());
			}

			for(GMV_Video u : p.videos)
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
				if(p.p.utilities.isNaN(d))
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
				p.p.randomSeed(clusteringRandomSeed);
				imageID = (int) p.p.random(p.images.size());  			// Random image ID for setting cluster's start location				
				panoramaID = (int) p.p.random(p.panoramas.size());  		// Random panorama ID for setting cluster's start location				
				videoID = (int) p.p.random(p.videos.size());  			// Random video ID for setting cluster's start location				
				addedImages.append(imageID);								
				addedPanoramas.append(panoramaID);								
				addedVideos.append(videoID);								

				/* Record media nearby added media*/
				for(GMV_Image img : p.images)						// Check for images near the picked one
				{
					float dist = img.getCaptureDistanceFrom(p.images.get(imageID).getCaptureLocation());  // Get distance
					if(dist < minClusterDistance)
						nearImages.append(img.getID());				// Record images nearby picked image
				}

				for(GMV_Panorama pano : p.panoramas)				// Check for panoramas near the picked one 
				{
					float dist = pano.getCaptureDistanceFrom(p.panoramas.get(panoramaID).getCaptureLocation());  // Get distance
					if(dist < minClusterDistance)
						nearPanoramas.append(pano.getID());			// Add to the list of nearby picked images
				}

				for(GMV_Video vid : p.videos)						// Check for videos near the picked one
				{
					float dist = vid.getCaptureDistanceFrom(p.videos.get(videoID).getCaptureLocation());  // Get distance
					if(dist < minClusterDistance)
						nearVideos.append(vid.getID());				// Add to the list of nearby picked images
				}

				/* Create the cluster */
				PVector clusterPoint = new PVector(0,0,0);
				if(p.images.size() > 0)
				{
					clusterPoint = new PVector(p.images.get(imageID).getCaptureLocation().x, p.images.get(imageID).getCaptureLocation().y, p.images.get(imageID).getCaptureLocation().z); // Choose random image location to start
					p.clusters.add(new GMV_Cluster(p, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
					i++;
				}
				if(p.panoramas.size() > 0)
				{
					clusterPoint = new PVector(p.panoramas.get(panoramaID).getCaptureLocation().x, p.panoramas.get(panoramaID).getCaptureLocation().y, p.panoramas.get(panoramaID).getCaptureLocation().z); // Choose random image location to start
					p.clusters.add(new GMV_Cluster(p, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
					i++;
				}
				if(p.videos.size() > 0)
				{
					clusterPoint = new PVector(p.videos.get(videoID).getCaptureLocation().x, p.videos.get(videoID).getCaptureLocation().y, p.videos.get(videoID).getCaptureLocation().z); // Choose random image location to start
					p.clusters.add(new GMV_Cluster(p, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
					i++;
				}

				if(i > 0)
					i--;
				else if(p.p.debug.model)
					PApplet.println("Error in initClusters()... No media!!");
			}
			else															// Find a random media (image, panorama or video) location for new cluster
			{
				int mediaID = (int) p.p.random(p.images.size() + p.panoramas.size() + p.videos.size());
				PVector clusterPoint = new PVector(0,0,0);

				if( mediaID < p.images.size() )				// If image, compare to already picked images
				{
					imageID = (int) p.p.random(p.images.size());  						
					while(addedImages.hasValue(imageID) && nearImages.hasValue(imageID))
						imageID = (int) p.p.random(p.images.size());  						

					addedImages.append(imageID);

					clusterPoint = new PVector(p.images.get(imageID).getCaptureLocation().x, p.images.get(imageID).getCaptureLocation().y, p.images.get(imageID).getCaptureLocation().z); // Choose random image location to start
				}
				else if( mediaID < p.images.size() + p.panoramas.size() )		// If panorama, compare to already picked panoramas
				{
					panoramaID = (int) p.p.random(p.panoramas.size());  						
					while(addedPanoramas.hasValue(panoramaID) && nearPanoramas.hasValue(panoramaID))
						panoramaID = (int) p.p.random(p.panoramas.size());  						

					addedPanoramas.append(panoramaID);

					clusterPoint = new PVector(p.panoramas.get(panoramaID).getCaptureLocation().x, p.panoramas.get(panoramaID).getCaptureLocation().y, p.panoramas.get(panoramaID).getCaptureLocation().z); // Choose random image location to start
				}
				else if( mediaID < p.images.size() + p.panoramas.size() + p.videos.size() )		// If video, compare to already picked videos
				{
					videoID = (int) p.p.random(p.videos.size());  						
					while(addedImages.hasValue(videoID) && nearImages.hasValue(videoID))
						videoID = (int) p.p.random(p.videos.size());  						

					addedVideos.append(videoID);

					clusterPoint = new PVector(p.videos.get(videoID).getCaptureLocation().x, p.videos.get(videoID).getCaptureLocation().y, p.videos.get(videoID).getCaptureLocation().z); // Choose random image location to start
				}

				p.clusters.add(new GMV_Cluster(p, i, clusterPoint.x, clusterPoint.y, clusterPoint.z));
			}
		}	
	}

	/**
	 * refineClusters()
	 * Refine clusters over given iterations
	 * @param iterations Number of iterations
	 */	
	void refineKMeansClusters(float epsilon, int iterations)
	{
		int count = 0;
		boolean moved = false;						// Has any cluster moved farther than epsilon?
		
		ArrayList<GMV_Cluster> last = p.clusters;
		if(p.p.debug.cluster || p.p.debug.model)
			PApplet.println("--> Refining clusters...");
		
		while( count < iterations ) 							// Iterate to create the clusters
		{		
			for (int i = 0; i < p.images.size(); i++) 			// Find closest cluster for each image
				p.images.get(i).findAssociatedCluster();		// Set associated cluster
			for (int i = 0; i < p.panoramas.size(); i++) 		// Find closest cluster for each image
				p.panoramas.get(i).findAssociatedCluster();		// Set associated cluster
			for (int i = 0; i < p.videos.size(); i++) 			// Find closest cluster for each image
				p.videos.get(i).findAssociatedCluster();		// Set associated cluster
			for (int i = 0; i < p.clusters.size(); i++) 		// Find closest cluster for each image
				p.clusters.get(i).create();						// Assign clusters

			if(p.clusters.size() == last.size())				// Check cluster movement
			{
				for(GMV_Cluster c : p.clusters)
				{
//					int closestIdx = -1;
					float closestDist = 10000.f;
					
					for(GMV_Cluster d : last)
					{
						float dist = c.getLocation().dist(d.getLocation());
						if(dist < closestDist)
						{
							closestDist = dist;
//							closestIdx = d.getID();
						}
					}
					
					if(closestDist > epsilon)
					{
						moved = true;
					}
				}
				
				if(!moved)
				{
					if(p.p.debug.cluster || p.p.debug.model)
						PApplet.println(" Stopped refinement... no clusters moved farther than epsilon:"+epsilon);
					break;								// If all clusters moved less than epsilon, stop refinement
				}
			}
			else
			{
				if(p.p.debug.cluster || p.p.debug.model)
					PApplet.println(" New clusters found... will keep refining clusters... clusters.size():"+p.clusters.size()+" last.size():"+last.size());
			}
			
			count++;
		}
	}

	/**
	 * mergeAdjacentClusters()
	 * Merge together clusters with closest neighbor below minClusterDistance threshold
	 */
	void mergeAdjacentClusters()
	{
		mergedClusters = 0;			// Reset mergedClusters count

		IntList[] closeNeighbors = new IntList[ p.clusters.size() ];			// List array of closest neighbor distances for each cluster 
		ArrayList<PVector> mostNeighbors = new ArrayList<PVector>();			// List of clusters with most neighbors and number of neighbors as PVector(id, neighborCount)
		IntList absorbed = new IntList();										// List of clusters absorbed into other clusters
		IntList merged = new IntList();											// List of clusters already merged with neighbors
		float firstMergePct = 0.2f;												// Fraction of clusters with most neighbors to merge first
		
		if((p.p.debug.cluster || p.p.debug.model ) && p.p.debug.detailed) PApplet.println("Merging adjacent clusters... ");

		for( GMV_Cluster c : p.clusters )					// Find distances of close neighbors to each cluster
		{
			closeNeighbors[c.getID()] = new IntList();	// Initialize list for this cluster
			for( GMV_Cluster d : p.clusters )
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
		for( GMV_Cluster c : p.clusters )					// Find distances of close neighbors for each cluster
		{
			if(count < p.clusters.size() * firstMergePct )		// Fill array with initial clusters 
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
			if(p.p.debug.cluster && v.y > 0 && p.p.debug.detailed)
				PApplet.println("Merging cluster "+(int)v.x+" with "+(int)v.y+" neighbors...");

			GMV_Cluster c = p.clusters.get( (int)v.x );
			if(!merged.hasValue(c.getID()))
			{
				for(int i : closeNeighbors[c.getID()])
				{
					if(!absorbed.hasValue(i) && c.getID() != i) 		// If cluster i hasn't already been absorbed and isn't the same cluster
					{
						c.absorbCluster(p.clusters.get(i));				// Absorb cluster
						absorbed.append(i);

						merged.append(i);
						merged.append(c.getID());
						mergedClusters++;
					}
				}
			}
		}

		for( GMV_Cluster c : p.clusters )					// Merge remaining clusters under minClusterDistance 
		{
			if(!merged.hasValue(c.getID()))
			{
				for( GMV_Cluster d : p.clusters )
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

		if(p.p.debug.cluster)
			PApplet.println("Merged Clusters "+mergedClusters);
	}


	/** 
	 * createSingleClusters()
	 * If any images are not associated, create a new cluster for each
	 */	void createSingleClusters()
	 {
		 int newClusterID = p.clusters.size();	// Start adding clusters at end of current list 
		 int initial = newClusterID;

		 for (GMV_Image i : p.images) 			// Find closest cluster for each image
		 {
			 if(i.cluster == -1)				// Create cluster for each single image
			 {
				 p.clusters.add(new GMV_Cluster(p, newClusterID, i.getCaptureLocation().x, i.getCaptureLocation().y, i.getCaptureLocation().z));
				 i.setAssociatedCluster(newClusterID);

				 p.clusters.get(newClusterID).createSingle(i.getID(), 0);
				 newClusterID++;
			 }
		 }

		 for (GMV_Panorama n : p.panoramas) 						// Find closest cluster for each image
		 {
			 if(n.cluster == -1)				// Create cluster for each single image
			 {
				 p.clusters.add(new GMV_Cluster(p, newClusterID, n.getCaptureLocation().x, n.getCaptureLocation().y, n.getCaptureLocation().z));
				 n.setAssociatedCluster(newClusterID);

				 p.clusters.get(newClusterID).createSingle(n.getID(), 1);
				 newClusterID++;
			 }
		 }

		 for (GMV_Video v : p.videos) 						// Find closest cluster for each image
		 {
			 if(v.cluster == -1)				// Create cluster for each single image
			 {
				 p.clusters.add(new GMV_Cluster(p, newClusterID, v.getCaptureLocation().x, v.getCaptureLocation().y, v.getCaptureLocation().z));
				 v.setAssociatedCluster(newClusterID);

				 p.clusters.get(newClusterID).createSingle(v.getID(), 2);
				 newClusterID++;
			 }
		 }

		 if(p.p.debug.cluster)
			 PApplet.println("Created "+(newClusterID-initial)+" clusters from single images...");
	 }

	 /** 
	  * findVideoPlaceholders()
	  * Find video placeholder images, i.e. images taken just before a video to indicate same location, orientation, elevation and rotation angles
	  */	
	 public void findVideoPlaceholders()
	 {
		 for (int i = 0; i < p.videos.size(); i++) 		
		 {
			 GMV_Video v = p.videos.get(i);
			 if(!v.disabled)
			 {
				 int id = v.getImagePlaceholder();				// Find associated image with each video

				 if(id != -1)
				 {
					 v.cluster = p.images.get(id).cluster;	// Set video cluster to cluster of associated image
					 p.clusters.get(v.cluster).video = true;	// Set cluster video property to true
					 if(p.p.debug.video)
						 PApplet.println("Image placeholder for video: "+i+" is:"+id+" p.clusters.get(v.cluster).video:"+p.clusters.get(v.cluster).video);
				 }
				 else
				 {
					 if(p.p.debug.video)
						 PApplet.println("No image placeholder found for video: "+i+" p.clusters.get(v.cluster).video:"+p.clusters.get(v.cluster).video);
					 v.disabled = true;
				 }
			 }
		 }
	 }

	 /**
	  * createCluster()
	  * @param index New clusterID
	  * @param location Location
	  * @param images GMV_Image list
	  * @param panoramas GMV_Panorama list
	  * @param videos GMV_Video list
	  * @return New cluster with given media
	  */
	 public GMV_Cluster createCluster( int index, PVector location, IntList images, IntList panoramas, IntList videos )
	 {
		 GMV_Cluster gmvc = new GMV_Cluster(p, index, location.x, location.y, location.z);

		 /* Add media to cluster */
		 for( int i : images )
		 {
			 gmvc.addImage(p.images.get(i));
			 gmvc.mediaPoints++;
		 }
		 for( int n : panoramas )
		 {
			 gmvc.addPanorama(p.panoramas.get(n));
			 gmvc.mediaPoints++;
		 }
		 for( int v : videos )
		 {
			 gmvc.addVideo(p.videos.get(v));
			 gmvc.mediaPoints++;
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
	  * createEmptyCluster()
	  * @param location New cluster location
	  * @param index New cluster ID
	  * @return New empty cluster
	  */
	 public GMV_Cluster createEmptyCluster( PVector location, int index )
	 {
		 if(location != null)
		 {
			 GMV_Cluster gmvc = new GMV_Cluster(p, index, location.x, location.y, location.z);
			 return gmvc;
		 }
		 return null;
	 }

	 /**
	  * calculateFieldSize()
	  * Analyze media to determine size of the virtual space
	  */
	 void calculateFieldSize() 
	 {
		 if(p.p.debug.field) PApplet.println("Calculating field dimensions...");

		 boolean init = true;	

		 for (GMV_Image i : p.images) 							// Iterate over images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
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

		 for (GMV_Panorama n : p.panoramas) 							// Iterate over images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
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

		 for (GMV_Video v : p.videos) 							// Iterate over images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
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

		 if (p.p.debug.model) 							// Display results for debugging
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
	  * analyzeMedia()
	  * Analyze media capture times, find on which scales it operates, i.e. minute, day, month, year   (not implemented yet)
	  */
	 public void analyzeMedia() 
	 {
		 float longestImageDayLength = (float) -1.;			// Length of the longest day
		 boolean initImageTime = true, initImageDate = true;
		 boolean initPanoTime = true, initPanoDate = true;	
		 boolean initVideoTime = true, initVideoDate = true;	

		 if(p.p.debug.field) PApplet.println("Analyzing media in field...");

		 for ( GMV_Video v : p.videos ) 			// Iterate over videos to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		 {
			 if (initVideoTime) 		// Calculate most recent and oldest video time
			 {		
				 highVideoTime = v.time.getTime();
				 lowVideoTime = v.time.getTime();
				 initVideoTime = false;
			 }

			 if (initVideoDate) 		// Calculate most recent and oldest image date
			 {		
				 highVideoDate = v.time.getDate();
				 lowVideoDate = v.time.getDate();
				 initVideoDate = false;
			 }

			 if (v.time.getTime() > highVideoTime)
				 highVideoTime = v.time.getTime();
			 if (v.time.getTime() < lowVideoTime)
				 lowVideoTime = v.time.getTime();

			 if (v.time.getDate() > highVideoDate)
				 highVideoDate = v.time.getDate();
			 if (v.time.getDate() < lowVideoDate)
				 lowVideoDate = v.time.getDate();

			 if (v.time.getDayLength() > longestVideoDayLength)		// Calculate longest video day length
				 longestVideoDayLength = v.time.getDayLength();
		 }

		 for (GMV_Image i : p.images) 			// Iterate over images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		 {
			 if (initImageTime) 	// Calculate most recent and oldest image time
			 {		
				 highImageTime = i.time.getTime();
				 lowImageTime = i.time.getTime();
				 initImageTime = false;
			 }

			 if (initImageDate)  	// Calculate most recent and oldest image date
			 {	
				 highImageDate = i.time.getDate();
				 lowImageDate = i.time.getDate();
				 initImageDate = false;
			 }

			 if (i.time.getTime() > highImageTime)
				 highImageTime = i.time.getTime();
			 if (i.time.getTime() < lowImageTime)
				 lowImageTime = i.time.getTime();

			 if (i.time.getDate() > highImageDate)
				 highImageDate = i.time.getDate();
			 if (i.time.getDate() < lowImageDate)
				 lowImageDate = i.time.getDate();

			 if (i.time.getDayLength() > longestImageDayLength)		// Calculate longest day length
				 longestImageDayLength = i.time.getDayLength();
		 }

		 for (GMV_Panorama i : p.panoramas) 			// Iterate over images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		 {
			 if (initPanoTime) 	// Calculate most recent and oldest Pano time
			 {		
				 highPanoTime = i.time.getTime();
				 lowPanoTime = i.time.getTime();
				 initPanoTime = false;
			 }

			 if (initPanoDate)  	// Calculate most recent and oldest Pano date
			 {	
				 highPanoDate = i.time.getDate();
				 lowPanoDate = i.time.getDate();
				 initPanoDate = false;
			 }

			 if (i.time.getTime() > highPanoTime)
				 highPanoTime = i.time.getTime();
			 if (i.time.getTime() < lowPanoTime)
				 lowPanoTime = i.time.getTime();

			 if (i.time.getDate() > highPanoDate)
				 highPanoDate = i.time.getDate();
			 if (i.time.getDate() < lowPanoDate)
				 lowPanoDate = i.time.getDate();

			 if (i.time.getDayLength() > longestPanoDayLength)		// Calculate longest day length
				 longestPanoDayLength = i.time.getDayLength();
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

		 if (p.p.debug.metadata) 							// Display results for debugging
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
	  * lockMediaToClusters()
	  * If image is within <threshold> from center of cluster along axes specified by mx, my and mz, 
	  * fold the image location into the cluster location along those axes.
	  */
	 public void lockMediaToClusters()
	 {
		 if(p.p.debug.field || p.p.debug.model) PApplet.println("lockMediaToClusters(): Moving media... ");
		 for (int i = 0; i < p.images.size(); i++) 
			 p.images.get(i).adjustCaptureLocation();		
		 for (int i = 0; i < p.panoramas.size(); i++) 
			 p.panoramas.get(i).adjustCaptureLocation();		
		 for (int i = 0; i < p.videos.size(); i++) 
			 p.videos.get(i).adjustCaptureLocation();		
	 }

	 /**
	  * calculateAveragePoint()
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
	  * getClusterAmount()
	  * @return Number of clusters in field
	  */
	 public int getClusterAmount()
	 {
		 return p.clusters.size() - mergedClusters;
	 }
	 
	// Minimum distance between clusters, i.e. closer than which clusters are merged
	void setMinClusterDistance(float newMinClusterDistance)
	{
		minClusterDistance = newMinClusterDistance;
	}
	
	// Maximum distance between clusters, i.e. farther than which single image clusters are created (set based on mediaDensity)
	void setMaxClusterDistance(float newMaxClusterDistance)
	{
		maxClusterDistance = newMaxClusterDistance;
	}

		/**
		 * drawClustersAtDepth()
		 * @param depth Depth at which to draw clusters
		 * Draw the clusters at given depth
		 */
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
//					location = p.images.get(mediaIdx).getCaptureLocation();
//				}
//				else if(mediaIdx < indexVideoOffset)
//				{
//					mediaIdx -= indexPanoramaOffset; 
//					location = p.panoramas.get(mediaIdx).getCaptureLocation();
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
//			if(p.p.debug.cluster) 
//				PApplet.println("Getting "+clusters.size()+" dendrogram clusters at depth:"+depth);
//		}

}
