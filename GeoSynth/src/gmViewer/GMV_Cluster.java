package gmViewer;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;
import processing.data.FloatList;
import processing.data.IntList;

/*********************************************
 * GMV_Cluster
 * @author davidgordon
 * Group of media files forming a spatial unit
 */
public class GMV_Cluster 
{
	/* General */
	private int id;				// Unique cluster ID
	private PVector location;			// Cluster center location
	private boolean active = false; 		// Active (i.e. not disabled)
	private boolean empty = false;		// Empty?
	private boolean single = false;		// Only one media point in cluster?

	/* Physics */
	private boolean isAttractor;					// Is it currently set as the only attractor?
	private float clusterGravity = 0.1333f;		// Cluster cravitational pull
	private float clusterMass = 0.f;				// Cluster mass, tied to value of mediaPoints
	private float farMassFactor = 8.f;		// How much more mass to give distant attractors to speed up navigation?
	
	/* Time */
	private FloatList clusterDates, clusterTimes;
	private FloatList clusterDatesLowerBounds, clusterTimesLowerBounds;
	private FloatList clusterDatesUpperBounds, clusterTimesUpperBounds;
	private FloatList fieldDates, fieldTimes;
	private FloatList fieldDatesLowerBounds, fieldTimesLowerBounds;
	private FloatList fieldDatesUpperBounds, fieldTimesUpperBounds;
	
	int[] clusterTimesHistogram, fieldTimesHistogram;					// Histogram of media times in cluster	
	ArrayList<GMV_TimeSegment> timeline;								// Timeline for this cluster
	public float timelineAngle = PApplet.PI/2.f; 	// (Not implemented yet) Span of each timeline, i.e. when showing different timelines per orientation
	public int timeUnitLength;			// Length of time unit in frames  (e.g. 10 means every 10 frames)
	public int baseTimeScale = 0; 					// (Not implemented yet) 0 = minutes, 1 = hours, 2 = days, 3 = months, 4 = years

	/* Segmentation */
	public ArrayList<int[]> imageSegments;		// List of arrays corresponding to each segment of images
	public ArrayList<int[]> videoSegments;		// List of arrays corresponding to each segment of videos
	private int numSegments = 8;						// Number of segments of the cluster
	
	/* Media */
	public float mediaPoints;			// No. of points (photos) associated with this cluster
	public IntList images;			// Images associated with this cluster
	public IntList panoramas;			// Panoramas associated with this cluster
	public IntList videos;			// Videos associated with this cluster
	
	private float highLongitude, lowLongitude, highLatitude, lowLatitude, 		// - NEED TO CALCULATE!	
			  	 highAltitude, lowAltitude;		
	private float highImageLongitude = -1000000, lowImageLongitude = 1000000, highImageLatitude = -1000000, lowImageLatitude = 1000000,		// - NEED TO CALCULATE!
			highImageAltitude = -1000000, lowImageAltitude = 1000000;
	private float highPanoramaLongitude = -1000000, lowPanoramaLongitude = 1000000, highPanoramaLatitude = -1000000,		// - NEED TO CALCULATE!
			lowPanoramaLatitude = 1000000, highPanoramaAltitude = -1000000, lowPanoramaAltitude = 1000000;
	private float highVideoLongitude = -1000000, lowVideoLongitude = 1000000, highVideoLatitude = -1000000,		// - NEED TO CALCULATE!
			lowVideoLatitude = 1000000, highVideoAltitude = -1000000, lowVideoAltitude = 1000000;
	
	private float highTime, lowTime, highDate, lowDate;
	public float highImageTime = -1000000, lowImageTime = 1000000, 
		  highPanoramaTime = -1000000, lowPanoramaTime = 1000000, 
		  highVideoTime = -1000000, lowVideoTime = 1000000; 	
	public float highImageDate = -1000000, lowImageDate = 1000000, 
		  highPanoramaDate = -1000000, lowPanoramaDate = 1000000,
	 	  highVideoDate = -1000000, lowVideoDate = 1000000;
	
	private float longestImageDayLength = -1000000, longestPanoramaDayLength = -1000000, longestVideoDayLength = -1000000;	

	/* Interaction */
	public float timeIncrement;				// User time increment

	/* Video */
	boolean video = false;

	GMV_Field p;

	GMV_Cluster(GMV_Field parent, int _clusterID, float _x, float _y, float _z) {
		p = parent;
		location = new PVector(_x, _y, _z);
		id = _clusterID;

//		dayLength = p.p.timeCycleLength; 						// Length of day in time units

		images = new IntList();
		panoramas = new IntList();
		videos = new IntList();
		
		mediaPoints = 0;
		
		clusterDates = new FloatList();
		clusterTimes = new FloatList();
		clusterTimesLowerBounds = new FloatList();
		clusterTimesUpperBounds = new FloatList();

		fieldTimes = new FloatList();
		fieldTimesLowerBounds = new FloatList();
		fieldTimesUpperBounds = new FloatList();
		fieldDates = new FloatList();
		
		clusterTimesHistogram = new int[p.p.clusterTimePrecision];
		fieldTimesHistogram = new int[p.p.fieldTimePrecision];

		timeline = new ArrayList<GMV_TimeSegment>();
		timeUnitLength = p.p.timeUnitLength;				// Length of time unit in frames  (e.g. 10 means every 10 frames)
		timeIncrement = p.p.timeInc;							// User time increment
	}

	/**
	 * addImage()
	 * @param newImage Image to add
	 * Add an image to the cluster
	 */
	void addImage(GMV_Image newImage)
	{
		if(!images.hasValue(newImage.getID()))
		{
			images.append(newImage.getID());
			mediaPoints++;
			clusterMass = mediaPoints * p.p.mediaPointMass;	
		}
	}

	/**
	 * addPanorama()
	 * @param newPanorama Panorama to add
	 * Add a panorama to the cluster
	 */
	void addPanorama(GMV_Panorama newPanorama)
	{
		if(!panoramas.hasValue(newPanorama.getID()))
		{
			panoramas.append(newPanorama.getID());
			mediaPoints++;
			clusterMass = mediaPoints * p.p.mediaPointMass;			
		}
	}

	/**
	 * addVideo()
	 * @param newImage Image to add
	 * Add a video to the cluster
	 */
	void addVideo(GMV_Video newVideo)
	{
		if(!videos.hasValue(newVideo.getID()))
		{
			videos.append(newVideo.getID());
			mediaPoints++;
			clusterMass = mediaPoints * p.p.mediaPointMass;		
		}
	}

	/**
	 * emptyCluster()
	 * Empty this cluster of all media
	 */
	void emptyCluster()
	{
		images = new IntList();
		panoramas = new IntList();
		videos = new IntList();
		mediaPoints = 0;
		active = false;
		empty = true;
	}
	
	/**
	 * create()
	 * Initialize cluster during simple k-means clustering. Calculate location and assign media to it.
	 */
	void create() 						
	{			
		mediaPoints = 0;

		PVector newLocation = new PVector((float)0.0, (float)0.0, (float)0.0);
		emptyCluster();
				
		/* Find images associated with this cluster ID */
		for (int i = 0; i < p.images.size(); i++) 
		{
			GMV_Image curImg = (GMV_Image) p.images.get(i);

			if (curImg.cluster == id) 			// If the image is assigned to this cluster
			{
				newLocation.add(curImg.getCaptureLocation());		// Move cluster towards the image
				if(!images.hasValue(curImg.getID()))
				{
					images.append(curImg.getID());
					mediaPoints++;
				}
			}
		}

		/* Find panoramas associated with this cluster ID */
		for (int i = 0; i < p.panoramas.size(); i++) 
		{
			GMV_Panorama curPano = (GMV_Panorama) p.panoramas.get(i);

			if (curPano.cluster == id) 			// If the image is assigned to this cluster
			{
				newLocation.add(curPano.getCaptureLocation());		// Move cluster towards the image
				if(!panoramas.hasValue(curPano.getID()))
				{
//					PApplet.println("Adding pano "+curPano.getID()+" to cluster:"+clusterID);
//					PApplet.println("associatedPanoramas.size(): "+associatedPanoramas.size());

					panoramas.append(curPano.getID());
					mediaPoints++;
				}
			}
		}

		/* Find videos associated with this cluster ID */
		for (int i = 0; i < p.videos.size(); i++) 
		{
			GMV_Video curVid = (GMV_Video) p.videos.get(i);

			if (curVid.cluster == id) 				// If the image is assigned to this cluster
			{
				newLocation.add(curVid.getCaptureLocation());	// Move cluster towards the image
				if(!videos.hasValue(curVid.getID()))
				{
					videos.append(curVid.getID());
					mediaPoints++;
				}

			}
		}

		/* Divide by number of associated points */
		if (mediaPoints > 0) 				
		{
			newLocation.div(mediaPoints);
			location = newLocation;

			clusterMass = mediaPoints * p.p.mediaPointMass;			// Mass = 4 x number of media points
			active = true;
			empty = false;
		}
		else
		{
			active = false;
			empty = true;
		}
	}

	/**
	 * createSingle()
	 * @param mediaID  Single image to determine the cluster location
	 * @param mediaType  0: image 1: panorama 2:video
	 * Create a cluster with a single image
	 */
	void createSingle(int mediaID, int mediaType) 						
	{
		switch(mediaType)
		{
		case 0:
			images = new IntList();
			images.append(mediaID);
			break;
		case 1:
			panoramas = new IntList();
			panoramas.append(mediaID);
			break;
		case 2:
			videos = new IntList();
			videos.append(mediaID);
			break;
		default:
			break;
		}
		
		mediaPoints = 1;
		clusterMass = mediaPoints * p.p.mediaPointMass;			// Mass = 4 x number of media points

		active = true;
		empty = false;
	}

	/**
	 * draw()
	 * Draw the cluster center
	 * @param hue 
	 */
	void draw(int hue)
	{
		//		if(!empty)
		{
			p.p.pushMatrix();
			p.p.fill(hue,255,255);
			p.p.stroke(255-hue,255,255);
			p.p.translate(location.x, location.y, location.z);
			p.p.sphere(p.p.clusterCenterSize);
			p.p.popMatrix();
		}
	}

	/**
	 * update()
	 * Update cluster variables
	 */
	void update()
	{
		clusterMass = mediaPoints;			// Set cluster mass

		if(mediaPoints == 0)
		{
			active = false;
			empty = true;
		}

		active = true;
//		updateTime();
	}

	/** 
	 * updateTime()
	 * Update time variables each frame
	 */
	public void updateTime()
	{
//		if(p.p.frameCount % timeUnitLength == 0 && !p.p.pause)
//		{
//			curTime++;							// Increment time
//
//			if(curTime > dayLength)
//			{
//				if(mediaAreActive())
//				{
//					if(p.p.debug.field)
//						PApplet.println("Media still active...");
//				}
//				else
//				{
//					curTime = 0;
//					//					if(p.p.debug.field)
//					//						PApplet.println("Reached end of day at frameCount:"+p.p.frameCount);
//				}
//			}
//		}
	}

	/**
	 * getImages()
	 * @return ArrayList of Image3Ds associated with cluster
	 */
	public ArrayList<GMV_Image> getImages()
	{
		ArrayList<GMV_Image> cImages = new ArrayList<GMV_Image>();
		for(int i : images)
		{
			cImages.add(p.images.get(i));
		}
		return cImages;
	}

	/**
	 * getPanoramas()
	 * @return ArrayList of Image3Ds associated with cluster
	 */
	public ArrayList<GMV_Panorama> getPanoramas()
	{
		ArrayList<GMV_Panorama> cPanoramas = new ArrayList<GMV_Panorama>();
		for(int i : panoramas)
		{
			cPanoramas.add(p.panoramas.get(i));
		}
		return cPanoramas;
	}
	
	/**
	 * getVideos()
	 * @return ArrayList of Video3Ds associated with cluster
	 */
	public ArrayList<GMV_Video> getVideos()
	{
		ArrayList<GMV_Video> cVideos = new ArrayList<GMV_Video>();
		for(int i : videos)
		{
			cVideos.add(p.videos.get(i));
		}
		return cVideos;
	}
	
	/**
	 * mediaAreActive()
	 * @return Are any images or videos in the cluster currently active?
	 */
	public boolean mediaAreActive()
	{
		boolean active = false;

		if(images.size() > 0)
		{
			for(int imgIdx : images)
			{
				GMV_Image i = p.images.get(imgIdx);
				if(i.isActive())
					active = true;
			}
		}

		if(panoramas.size() > 0)
		{
			for(int panoIdx : panoramas)
			{
				GMV_Panorama n = p.panoramas.get(panoIdx);
				if(n.isActive())
					active = true;
			}
		}

		if(videos.size() > 0)
		{
			for(int vidIdx : videos)
			{
				GMV_Video v = p.videos.get(vidIdx);
				if(v.isActive())
					active = true;
			}
		}

		return active;
	}

	/**
	 * setBaseTimeScale()
	 * @param newBaseTimeScale New baseTimeScale
	 * Set base time scale, i.e. unit to cycle through during simulation (0 = minute, 1 = hour, 2 = day, 3 = month, 4 = year)
	 */
	public void setBaseTimeScale(int newBaseTimeScale)
	{
		baseTimeScale = newBaseTimeScale;
	}

//	/**
//	 * decrementTime()
//	 * Decrease the current time
//	 */
//	public void decrementTime()
//	{
//		curTime -= timeIncrement;
//		if (curTime < 0)
//			curTime = 0;
//	}
//
//	/**
//	 * incrementTime()
//	 * Increase the current time
//	 */
//	void incrementTime()
//	{
//		curTime += timeIncrement;
//		if (curTime > dayLength)
//			curTime = dayLength - 200;
//	}

	/**
	 * attractViewer()
	 * Attract the camera
	 */
	void attractViewer()
	{
		if(isAttractor && !empty)												// Attractor clusters do not need to be active, but attract while empty
			p.p.viewer.attraction.add( getAttractionForce() );		// Add attraction force to camera 
	}

	/**
	 * getClusterDistance()
	 * @return Distance from cluster center to camera
	 */
	float getClusterDistance()       // Find distance from camera to point in virtual space where photo appears           
	{
		return PVector.dist(location, p.p.viewer.getLocation());
	}

	/**
	 * getAttractionForce()
	 * @return Attraction force to be applied on the camera
	 */
	public PVector getAttractionForce() 
	{
		PVector force = PVector.sub(location, p.p.viewer.getLocation());
		float distance = force.mag();
		force.normalize();
		
		float mass, dist = getClusterDistance();
		if( dist > p.p.clusterFarDistance )
			mass = clusterMass * farMassFactor * PApplet.sqrt(distance);	// Increase mass with distance to ensure minimum acceleration
		else
			mass = clusterMass;
		
//		PApplet.println("PApplet.sqrt(distance):"+PApplet.sqrt(distance));

		float strength = (clusterGravity * mass * p.p.viewer.cameraMass) / (distance * distance);	// Calculate strength
		force.mult(strength);
		
		if(p.p.drawForceVector)
			p.p.display.drawForceVector(force);
		
		return force; 								// Return force to be applied
	}

//	void calcAverageTime() 
//	{
//		float sum = (float) 0.;
//		int count = 0;
//
//		for (int i = 0; i < p.images.size(); i++) {
//			if (p.images.get(i).cluster == clusterID) {
//				sum += p.images.get(i).time.getTime();
//				count++;
//			}
//		}
//
//		averageTime = sum / count;
//	}
//
//
//	void calcAverageDate() 
//	{
//		float sum = (float) 0.;
//		int count = 0;
//
//		for (int i = 0; i < p.images.size(); i++) {
//			if (p.images.get(i).cluster == clusterID) {
//				sum += p.images.get(i).time.getDate();
//				count++;
//			}
//		}
//
//		averageDate = sum / count;
//	}
	

	/***
	 * mergeWithCluster()
	 * @param mCluster Cluster to merge with
	 * Merge this cluster with given cluster. Empty and make the given cluster non-active.
	 */
	void absorbCluster(GMV_Cluster mCluster)
	{
		//		if(p.p.debug.clusters)
		//			p.p.display.sendUserMessage("Merging cluster "+clusterID+" with "+mCluster.clusterID);

		/* Find images associated with mCluster */
		for (int i = 0; i < p.images.size(); i++) 
		{
			GMV_Image curImg = (GMV_Image) p.images.get(i);

			if (curImg.cluster == mCluster.getID()) 				// If the image is assigned to this cluster
			{
				curImg.cluster = id;
				mediaPoints++;
			}
		}

		/* Find videos associated with mCluster */
		for (int i = 0; i < p.videos.size(); i++) 
		{
			GMV_Video curVid = (GMV_Video) p.videos.get(i);

			if (curVid.cluster == mCluster.getID()) 				// If the image is assigned to this cluster
			{
				curVid.cluster = id;
				mediaPoints++;
			}
		}

		/* Empty merged cluster */
		mCluster.images = new IntList();
		mCluster.panoramas = new IntList();
		mCluster.videos = new IntList();
		mCluster.mediaPoints = 0;
		mCluster.active = false;
		mCluster.empty = true;
	}
	
	/**
	 * analyzeMedia()
	 * Analyze associated media capture times (Need to implement: find on which scales it operates, i.e. minute, day, month, year)
	 */
	public void analyzeMedia() 
	{
		calculateDimensions();		// Calculate cluster dimensions (bounds)
		calculateTimes();			// Calculate cluster times
		createTimeline();			// Create times histograms and analyze for time points 
	}
	
	/**
	 * createTimeline()
	 * Find time points for this cluster
	 */
	void createTimeline()
	{
		FloatList mediaTimes = new FloatList();				// List of times to analyze
		IntList tc;										// Temporary time point list for finding duplicates
		
		/* Get times of all media of all types in this cluster */
		for(int i : images)
			mediaTimes.append( p.images.get(i).time.getTime() );
		for(int n : panoramas)
			mediaTimes.append( p.panoramas.get(n).time.getTime() );
		for(int v : videos)
			mediaTimes.append( p.videos.get(v).time.getTime() );

		/* Create cluster-specific times histogram */
		for (int i = 0; i < p.p.clusterTimePrecision; i++) // Initialize histogram
			clusterTimesHistogram[i] = 0;
		
		for (int i = 0; i < mediaTimes.size(); i++) 				// Fill cluster times histogram
		{
			int idx = PApplet.round(PApplet.constrain(PApplet.map(mediaTimes.get(i), 0.f, 1.f, 0.f, 
									p.p.clusterTimePrecision - 1), 0.f, p.p.clusterTimePrecision - 1.f));
			clusterTimesHistogram[idx]++;
		}

		tc = getTimePoints(clusterTimesHistogram, p.p.clusterTimePrecision);	// Get relative (cluster) time points
		
		clusterTimes = new FloatList();
		clusterTimesLowerBounds = new FloatList();
		clusterTimesUpperBounds = new FloatList();
		timeline = new ArrayList<GMV_TimeSegment>();
		
		for(int t:tc)
		{
			if(!clusterTimes.hasValue(t))
			{
				/* Add cluster time */
				clusterTimes.append(PApplet.map(t, 0, p.p.clusterTimePrecision, 0.f, 1.f));
				
				/* Find upper and lower bounds for cluster times */
				int i = t;
				int val = clusterTimesHistogram[i];
				while(val != 0) 				
				{
					i--;
					if(i >= 0)
						val = clusterTimesHistogram[i];
					else
					{
						i=0;
						break;
					}
				}
				clusterTimesLowerBounds.append(PApplet.map(i, 0, p.p.clusterTimePrecision, 0.f, 1.f));
				
				i = t;
				val = clusterTimesHistogram[i];
				while(val != 0) 				
				{
					i++;
					if(i < clusterTimesHistogram.length)
						val = clusterTimesHistogram[i];
					else
					{
						i=clusterTimesHistogram.length - 1;
						break;
					}
				}
				clusterTimesUpperBounds.append(PApplet.map(i, 0, p.p.clusterTimePrecision, 0.f, 1.f));
			}
		}
//		PApplet.println("clusterTimes.size():"+clusterTimes.size());
//		PApplet.println("clusterTimesLowerBounds.size():"+clusterTimesLowerBounds.size());
//		PApplet.println("clusterTimesUpperBounds.size():"+clusterTimesUpperBounds.size());
		int count = 0;
		for( float t:clusterTimes )							// Add times to timeline
		{
//			GMV_TimeSegment(float newID, float newTime, float newUpper, float newLower)
			timeline.add(new GMV_TimeSegment(count, t, clusterTimesUpperBounds.get(count), clusterTimesLowerBounds.get(count)));
//			timeline.get(count).setID(count);
//			timeline.get(count).setUpper(clusterTimesUpperBounds.get(count));
//			timeline.get(count).setLower(clusterTimesLowerBounds.get(count));
			count++;
		}

		mediaTimes = new FloatList();

		/* Get times of all media of all types in field */
		for(GMV_Image i : p.images)
			mediaTimes.append( i.time.getTime() );
		for(GMV_Panorama n : p.panoramas)
			mediaTimes.append( n.time.getTime() );
		for(GMV_Video v : p.videos)
			mediaTimes.append( v.time.getTime() );

		for (int i = 0; i < p.p.fieldTimePrecision; i++) // Initialize histogram
			fieldTimesHistogram[i] = 0;

		for (int i = 0; i < mediaTimes.size(); i++) 				// Fill field times histogram
		{
			int idx = PApplet.round(PApplet.constrain(PApplet.map(mediaTimes.get(i), 0.f, 1.f, 0.f, 
					p.p.fieldTimePrecision - 1), 0.f, p.p.fieldTimePrecision - 1.f));
			fieldTimesHistogram[idx]++;
		}
		
		tc = new IntList();														
		tc = getTimePoints(fieldTimesHistogram, p.p.fieldTimePrecision);		// Get absolute (field) time points
		
		fieldTimes = new FloatList();
		fieldTimesLowerBounds = new FloatList();
		fieldTimesUpperBounds = new FloatList();
		
		for(int t:tc)
		{
			if(!fieldTimes.hasValue(t))
			{
				fieldTimes.append(PApplet.map(t, 0, p.p.fieldTimePrecision, 0.f, 1.f));

				/* Find upper and lower bounds for field times */
				int i = t;
				int val = fieldTimesHistogram[i];
				while(val != 0) 				
				{
					i--;
					if(i >= 0)
						val = fieldTimesHistogram[i];
					else
					{
						i=0;
						break;
					}
				}
				fieldTimesLowerBounds.append(PApplet.map(i, 0, p.p.fieldTimePrecision, 0.f, 1.f));

				i = t;
				val = fieldTimesHistogram[i];
				while(val != 0) 				
				{
					i++;
					if(i < fieldTimesHistogram.length)
						val = fieldTimesHistogram[i];
					else
					{
						i=fieldTimesHistogram.length - 1;
						break;
					}
				}
				fieldTimesUpperBounds.append(PApplet.map(i, 0, p.p.clusterTimePrecision, 0.f, 1.f));
			}
		}
		
//		count = 0;
//		for( float t:fieldTimes )							// Add times to timeline
//		{
//			fieldTimeline.add(new GMV_TimePoint(t));
//			fieldTimeline.get(count).setID(count);
//			fieldTimeline.get(count).setUpper(clusterTimesUpperBounds.get(count));
//			fieldTimeline.get(count).setLower(clusterTimesLowerBounds.get(count));
//			count++;
//		}

		clusterTimes.sort();
		fieldTimes.sort();
		timeline.sort(GMV_TimeSegment.GMV_TimeMidpointComparator);				// Sort timeline points 
		
//		fieldTimeline.sort(GMV_TimePoint.GMV_TimeMidpointComparator);			// Sort time points 

		/* Debugging */
		if(p.p.debug.cluster && clusterTimes.size()>1)
		{
			PApplet.println("--> Cluster "+id+" with "+clusterTimes.size()+" different cluster times...");

//			int count = 0;
			for(float f:clusterTimes)
			{
				PApplet.println("Cluster "+id+", cluster time #"+(count++)+": "+f);
			}
		}
//		if(clusterFieldTimes.size()>1)
//		{
//			PApplet.println("--> Cluster "+id+" with "+clusterFieldTimes.size()+"different field times...");
//
//			int count = 0;
//			for(float f:clusterFieldTimes)
//			{
//				PApplet.println("Cluster "+id+", cluster field time #"+(count++)+": "+f);
//			}
//		}
	}
	
	/**
	 * calculateTimes()
	 * Calculate low and high values for time and date for each media point
	 */
	void calculateTimes()
	{
		float longestDayLength = (float) -1.;			// Length of the longest day
		boolean initImageTime = true, initPanoramaTime = true, initVideoTime = true, 
				initImageDate = true, initPanoramaDate = true, initVideoDate = true;	

//		if(p.p.debug.cluster && (images.size() != 0 || panoramas.size() != 0 || videos.size() != 0))
//			PApplet.println("Analyzing media times in cluster "+id+" ..." + " associatedImages.size():"+images.size()+" associatedPanoramas:"+panoramas.size()+" associatedVideos:"+videos.size());
			
		ArrayList<GMV_Image> cImages = new ArrayList<GMV_Image>();
		ArrayList<GMV_Panorama> cPanoramas = new ArrayList<GMV_Panorama>();
		ArrayList<GMV_Video> cVideos = new ArrayList<GMV_Video>();
		
		for(int i : images)
			cImages.add(p.images.get(i));
		for(int i : panoramas)
			cPanoramas.add(p.panoramas.get(i));
		for(int i : videos)
			cVideos.add(p.videos.get(i));

		for (GMV_Image i : cImages) 			// Iterate over cluster images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		{
			float fDayLength = i.time.getDayLength();

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

			if (fDayLength > longestDayLength)		// Calculate longest day length
				longestDayLength = fDayLength;
		}
		
		
		for (GMV_Panorama n : cPanoramas) 			// Iterate over cluster panoramas to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		{
			float fDayLength = n.time.getDayLength();

			if (initPanoramaTime) 		// Calculate most recent and oldest panorama time
			{		
				highPanoramaTime = n.time.getTime();
				lowPanoramaTime = n.time.getTime();
				initPanoramaTime = false;
			}

			if (initPanoramaDate) 		// Calculate most recent and oldest panorama date
			{		
				highPanoramaDate = n.time.getDate();
				lowPanoramaDate = n.time.getDate();
				initPanoramaDate = false;
			}

			if (n.time.getTime() > highPanoramaTime)
				highPanoramaTime = n.time.getTime();
			if (n.time.getTime() < lowPanoramaTime)
				lowPanoramaTime = n.time.getTime();

			if (n.time.getDate() > highPanoramaDate)
				highPanoramaDate = n.time.getDate();
			if (n.time.getDate() < lowPanoramaDate)
				lowPanoramaDate = n.time.getDate();

			if (fDayLength > longestPanoramaDayLength)		// Calculate longest panorama day length
				longestPanoramaDayLength = fDayLength;
		}
		
		for (GMV_Video v : cVideos) 			// Iterate over cluster videos to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		{
			float fDayLength = v.time.getDayLength();

			if (initVideoTime) 		// Calculate most recent and oldest video time
			{		
				highVideoTime = v.time.getTime();
				lowVideoTime = v.time.getTime();
				initVideoTime = false;
			}

			if (initVideoDate) 		// Calculate most recent and oldest video date
			{		
				highVideoDate = v.time.getDate();
				lowVideoDate = v.time.getDate();
				initImageDate = false;
			}

			if (v.time.getTime() > highVideoTime)
				highVideoTime = v.time.getTime();
			if (v.time.getTime() < lowVideoTime)
				lowVideoTime = v.time.getTime();

			if (v.time.getDate() > highVideoDate)
				highVideoDate = v.time.getDate();
			if (v.time.getDate() < lowVideoDate)
				lowVideoDate = v.time.getDate();

			if (fDayLength > longestVideoDayLength)		// Calculate longest video day length
				longestVideoDayLength = fDayLength;
		}

		lowTime = lowImageTime;
		if (lowPanoramaTime < lowTime)
			lowTime = lowPanoramaTime;
		if (lowVideoTime < lowTime)
			lowTime = lowVideoTime;

		highTime = highImageTime;
		if (highPanoramaTime > highTime)
			highTime = highPanoramaTime;
		if (highVideoTime > highTime)
			highTime = highVideoTime;

		lowDate = lowImageDate;
		if (lowPanoramaDate < lowDate)
			lowDate = lowPanoramaDate;
		if (lowVideoDate < lowDate)
			lowDate = lowVideoDate;

		highDate = highImageDate;
		if (highPanoramaDate > highDate)
			highDate = highPanoramaDate;
		if (highVideoDate > highDate)
			highDate = highVideoDate;
	}
	
	/**
	 * calculateDimensions()
	 * Calculate high and low longitude, latitude and altitude for cluster
	 */
	void calculateDimensions()
	{
		if(p.p.debug.field) PApplet.println("Calculating cluster dimensions...");

		boolean init = true;	

		for (int img : images) 				// Iterate over associated images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		{
			GMV_Image i = p.images.get(img);
			PVector gpsLocation = i.getGPSLocation();
			
			if (init) 	
			{	
				highLongitude = gpsLocation.x;
				lowLongitude = gpsLocation.x;
			}
			if (init) 
			{	
				highLatitude = gpsLocation.z;
				lowLatitude = gpsLocation.z;
			}
			if (init) 	
			{		
				highAltitude = gpsLocation.y;
				lowAltitude = gpsLocation.y;
				init = false;
			}

			if (gpsLocation.x > highLongitude)
				highLongitude = gpsLocation.x;
			if (gpsLocation.x < lowLongitude)
				lowLongitude = gpsLocation.x;
			if (gpsLocation.y > highAltitude)
				highAltitude = gpsLocation.y;
			if (gpsLocation.y < lowAltitude)
				lowAltitude = gpsLocation.y;
			if (gpsLocation.z > highLatitude)
				highLatitude = gpsLocation.z;
			if (gpsLocation.z < lowLatitude)
				lowLatitude = gpsLocation.z;
		}

		for (int pano : panoramas) 				// Iterate over associated images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		{
			GMV_Panorama n = p.panoramas.get(pano);
			PVector gpsLocation = n.getGPSLocation();

			if (gpsLocation.x > highLongitude)
				highLongitude = gpsLocation.x;
			if (gpsLocation.x < lowLongitude)
				lowLongitude = gpsLocation.x;
			if (gpsLocation.y > highAltitude)
				highAltitude = gpsLocation.y;
			if (gpsLocation.y < lowAltitude)
				lowAltitude = gpsLocation.y;
			if (gpsLocation.z > highLatitude)
				highLatitude = gpsLocation.z;
			if (gpsLocation.z < lowLatitude)
				lowLatitude = gpsLocation.z;
		}
		
		for (int vid : videos) 				// Iterate over associated images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		{
			GMV_Video v = p.videos.get(vid);
			PVector gpsLocation = v.getGPSLocation();
			
			if (gpsLocation.x > highLongitude)
				highLongitude = gpsLocation.x;
			if (gpsLocation.x < lowLongitude)
				lowLongitude = gpsLocation.x;
			if (gpsLocation.y > highAltitude)
				highAltitude = gpsLocation.y;
			if (gpsLocation.y < lowAltitude)
				lowAltitude = gpsLocation.y;
			if (gpsLocation.z > highLatitude)
				highLatitude = gpsLocation.z;
			if (gpsLocation.z < lowLatitude)
				lowLatitude = gpsLocation.z;
		}
	}
	
	/**
	 * displayClusterData()
	 * Display cluster data
	 */
	public void displayClusterData()
	{
//		p.p.display.metadata("");
		System.out.println("Cluster "+id+" High Longitude:" + highLongitude);
		System.out.println("Cluster "+id+" High Latitude:" + highLatitude);
		System.out.println("Cluster "+id+" High Altitude:" + highAltitude);
		System.out.println("Cluster "+id+" Low Longitude:" + lowLongitude);
		System.out.println("Cluster "+id+" Low Latitude:" + lowLatitude);
		System.out.println("Cluster "+id+" Low Altitude:" + lowAltitude);	
		

		System.out.println("Cluster "+id+" High Time:" + highTime);
		System.out.println("Cluster "+id+" High Date:" + highDate);
		System.out.println("Cluster "+id+" Low Time:" + lowTime);
		System.out.println("Cluster "+id+" Low Date:" + lowDate);
		
		System.out.println("Cluster "+id+" High Latitude:" + highLatitude);
		System.out.println("Cluster "+id+" Low Latitude:" + lowLatitude);
		System.out.println("Cluster "+id+" High Longitude:" + highLongitude);
		System.out.println("Cluster "+id+" Low Longitude:" + lowLongitude);
//		System.out.println("Cluster "+id+" Longest Day Length (working?):" + longestDayLength);
		System.out.println(" ");
	}
	
	/**
	 * getTimeClusters()
	 * @param times List of times
	 * @param timePrecision Number of histogram bins
	 * @return Time clusters
	 */
	IntList getTimePoints(int histogram[], int timePrecision)				// -- clusterTimelineMinPoints!!								
	{
		/* Clustering to find peaks in media activity */
		
		/* Initialize list of media times */
		ArrayList<GMV_TimeSegment> mediaTimes = new ArrayList<GMV_TimeSegment>();
		
		for (int i=0; i<timePrecision; i++) 				
			for(int j=0; j<histogram[i]; j++)							// Add time to list for each media point
				mediaTimes.add(new GMV_TimeSegment(0, i, 0, 0));		// Don't need ID, upper or lower values
		
		/* Initialize clusters */
		int numTimeClusters = 8;								// Max (default) 8 time clusters
		IntList timeClusters = new IntList();
		
		for (int i = 0; i < numTimeClusters; i++) 				// Iterate through the clusters
		{
			int idx = PApplet.round(mediaTimes.get(PApplet.round(p.p.random(mediaTimes.size()-1))).getCenter());		// Random index
			int ct = 0;
			boolean created = true;
			
			while(timeClusters.hasValue(idx))					// Try repeatedly to find a random time not already in list
			{
				idx = PApplet.round(mediaTimes.get(PApplet.round(p.p.random(mediaTimes.size()-1))).getCenter());
				ct++;
				if(ct > mediaTimes.size())		// If failed after many tries
				{
					created = false;		// Give up
					break;
				}
			}
			
			if(created)						// If a new time was cound, add to timeClusters
				timeClusters.append(idx);
		}
		
		numTimeClusters = timeClusters.size();

		/* Refine clusters */
		if(numTimeClusters > 1)						 
		{
			int iterations = 60;
			int count = 0;

			while (count < iterations)
			{
				for(GMV_TimeSegment m : mediaTimes)					// Find nearest cluster for each data point
				{
					int closestIndex = 0;
					int closest = 1000000;

					for (int idx : timeClusters) 						
					{
						int distance = PApplet.abs(idx - (int)m.getCenter());		// Calculate distance

						if (distance < closest)
						{
							closestIndex = idx;
							closest = distance;
						}
					}

					m.setID(closestIndex);			
				}

				IntList newClusters = new IntList();
				for(int i:timeClusters)							// For each cluster, add and divide by number of data points
				{
					FloatList dataPoints = new FloatList();

					for(GMV_TimeSegment m:mediaTimes)					
						if(m.getID() == i)	
							dataPoints.append((float)m.getCenter());

					if(dataPoints.size() > 0)
					{
						int total = 0;
						
						for(float d:dataPoints) 
							total += d;

						int newCluster = PApplet.round(total / dataPoints.size());
						newClusters.append(newCluster);
					}
				}

				timeClusters = newClusters;
				count++;
			}
		}
		
		return timeClusters;			// Return cluster list
	}

	/**
	 * getClusterAsWaypoint()
	 * @return This cluster as a waypoint for navigation
	 */
	GMV_Waypoint getClusterAsWaypoint()
	{
		GMV_Waypoint result = new GMV_Waypoint(getID(), getLocation());
		return result;
	}
	
	public FloatList getClusterTimes()
	{
		return clusterTimes;
	}

	public FloatList getClusterTimesLowerBounds()
	{
		return clusterTimesLowerBounds;
	}

	public FloatList getClusterTimesUpperBounds()
	{
		return clusterTimesUpperBounds;
	}
	
	public void setActive(boolean newActive)
	{
		active = newActive;
	}
	
	public void setEmpty(boolean newEmpty)
	{
		empty = newEmpty;
	}
	
	public void setSingle(boolean state)
	{
		single = state;
	}

	public boolean isActive()
	{
		return active;
	}
	
	public boolean isEmpty()
	{
		return empty;
	}
	
	public boolean isSingle()
	{
		return single;
	}
	
	void setMass(float newMass)
	{
		clusterMass = newMass;
	}
	
	/**
	 * setAttractor()
	 * Set this cluster as an attractor
	 */
	void setAttractor(boolean state)
	{
		isAttractor = state;
//		if(isAttractor)
//		p.p.display.message("Set cluster isAttractor to true:"+getID());
	}

	public boolean isAttractor()
	{
		return isAttractor;
	}
	
	public float getClusterMass()
	{
		return clusterMass;
	}
	
//	private boolean isAttractor;					// Is it currently set as the only attractor?
//	private float clusterGravity = 0.1333f;		// Cluster cravitational pull
//	private float p.p.mediaPointMass = 0.05f;		// Mass contribution of each media point
//	
//	private float farMassFactor = 8.f;		// How much more mass to give distant attractors to speed up navigation?
	
	public int getID()
	{
		return id;
	}
	
	public PVector getLocation()
	{
		return location;
	}
	
	public void setLocation(PVector newLocation)
	{
		location = newLocation;
	}
}