package wmViewer;
import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PVector;
import processing.data.FloatList;
import processing.data.IntList;

/*********************************************
 * @author davidgordon
 * Class representing a group of media files forming a spatial unit
 */

public class WMV_Cluster 
{
	/* General */
	private int id;						// Cluster ID
	private PVector location;			// Cluster center location
	private boolean active = false; 	// Currently active
	private boolean empty = false;		// Currently empty
	private boolean single = false;		// Only one media point in cluster?

	/* Panorama */
	ArrayList<WMV_Panorama> stitchedPanoramas, userPanoramas;
//	private PImage stitchedPanorama;			// Stitched panorama		-- Make into arrayList
	
	/* Physics */
	private boolean isAttractor;				// Whether cluster is attracting viewer
	private float clusterGravity = 0.1333f;		// Cluster gravitational pull
	private float clusterMass = 1.5f;			// Cluster mass		-- No longer tied to value of mediaPoints
	private float farMassFactor = 8.f;			// How much more mass to give distant attractors to speed up navigation?
	
	/* Time */
	public boolean timeFading = false;					// Does time affect photos' brightness? (true = yes; false = no)
	public boolean dateFading = false;					// Does time affect photos' brightness? (true = yes; false = no)
	public boolean paused = false;						// Time is paused

	public boolean showAllDateSegments = true;			// Show all time segments (true) or show only current cluster (false)?
	public boolean showAllTimeSegments = true;			// Show all time segments (true) or show only current cluster (false)?

	public int currentTime = 0;							// Time units since start of time cycle (day / month / year)
	public int timeCycleLength = 250;					// Length of main time loop in frames
	public int timeUnitLength = 1;						// How many frames between time increments
	public float timeInc = timeCycleLength / 30.f;			

	public int currentDate = 0;							// Date units since start of date cycle (day / month / year)
	public int dateCycleLength = 500;					// Length of main date loop in frames
	public int dateUnitLength = 1;						// How many frames between date increments
	public float dateInc = dateCycleLength / 30.f;			

	public int defaultMediaLength = 125;					// Default frame length of media in time cycle
	private FloatList clusterDates, clusterTimes;
	private FloatList clusterDatesLowerBounds, clusterTimesLowerBounds;	// Obsolete
	private FloatList clusterDatesUpperBounds, clusterTimesUpperBounds;	// Obsolete
	private FloatList fieldDates, fieldTimes;	// Obsolete
	private FloatList fieldDatesLowerBounds, fieldTimesLowerBounds;	// Obsolete
	private FloatList fieldDatesUpperBounds, fieldTimesUpperBounds;	// Obsolete

	
	int[] clusterTimesHistogram, fieldTimesHistogram;					// Histogram of media times relative to cluster	/ field
	ArrayList<WMV_TimeSegment> timeline;								// Timeline for this cluster
	int[] clusterDatesHistogram, fieldDatesHistogram;					// Histogram of media times in cluster	
	ArrayList<WMV_TimeSegment> dateline;								// Date timeline for this cluster
	ArrayList<ArrayList<WMV_TimeSegment>> timelines;
	
//	public float timelineAngle = PApplet.PI/2.f; 	// (Not implemented yet) Span of each timeline, i.e. when showing different timelines per orientation

	/* Segmentation */
	public ArrayList<WMV_MediaSegment> segments;		// List of arrays corresponding to each segment of images
	private int numSegments = 0;						// Number of segments of the cluster
	
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
//	public float timeIncrement;				// User time increment

	/* Video */
	boolean video = false;
	
	/* Panorama */
	public boolean panorama = false;					// Cluster has panorama files?
	IntList valid;										// List of images that are good stitching candidates
		
	WMV_Field p;

	WMV_Cluster(WMV_Field parent, int _clusterID, float _x, float _y, float _z) {
		p = parent;
		location = new PVector(_x, _y, _z);
		id = _clusterID;

//		dayLength = p.p.timeCycleLength; 						// Length of day in time units

		images = new IntList();
		panoramas = new IntList();
		videos = new IntList();
		segments = new ArrayList<WMV_MediaSegment>();
		
		stitchedPanoramas = new ArrayList<WMV_Panorama>();
		userPanoramas = new ArrayList<WMV_Panorama>();
		mediaPoints = 0;
		
		clusterDates = new FloatList();
		clusterDatesLowerBounds = new FloatList();
		clusterDatesUpperBounds = new FloatList();

		clusterTimes = new FloatList();
		clusterTimesLowerBounds = new FloatList();
		clusterTimesUpperBounds = new FloatList();

		fieldTimes = new FloatList();
		fieldTimesLowerBounds = new FloatList();
		fieldTimesUpperBounds = new FloatList();

		fieldDates = new FloatList();
		fieldDatesLowerBounds = new FloatList();
		fieldDatesUpperBounds = new FloatList();
		
		clusterTimesHistogram = new int[p.p.clusterTimePrecision];
		fieldTimesHistogram = new int[p.p.fieldTimePrecision];

		clusterDatesHistogram = new int[p.p.clusterTimePrecision];
		fieldDatesHistogram = new int[p.p.fieldTimePrecision];

		timeline = new ArrayList<WMV_TimeSegment>();
//		timeUnitLength = p.p.timeUnitLength;					// Length of time unit in frames  (e.g. 10 means every 10 frames)
//		timeIncrement = p.p.timeInc;							// User time increment
	}
	
	/**
	 * Group adjacent, overlapping media into segments, where each image or video is at least stitchingMinAngle from one or more others
 	 */
	void findMediaSegments()
	{
		IntList allImages = new IntList();
		for(int i:images)
			allImages.append(i);
		
		boolean done = false;
		
		if(allImages.size() == 0)
			done = true;					// Do nothing if no images
		
		if(allImages.size() == 1)
		{
			IntList curImages = new IntList();
			curImages.append(allImages.get(0));
			
			float left = p.images.get(allImages.get(0)).getDirection();
			float right = p.images.get(allImages.get(0)).getDirection();
			float centerDirection = p.images.get(allImages.get(0)).getDirection();

			float bottom = p.images.get(allImages.get(0)).getElevation();
			float top = p.images.get(allImages.get(0)).getElevation();
			float centerElevation = p.images.get(allImages.get(0)).getElevation();

			segments.add( new WMV_MediaSegment( this, 0, curImages, null, right, left, centerDirection, 
												top, bottom, centerElevation) );

			if(p.p.p.debug.cluster || p.p.p.debug.model)
				PApplet.println("Added media segment in cluster: "+getID()+" with single image...");

			done = true;
		}
		
		while(!done)
		{
			IntList curImages = new IntList();
			float left = 360.f, right = 0.f, centerDirection;		// Left and right bounds (in degrees) of segment 
			float bottom = 100.f, top = -100.f, centerElevation;	// Top and bottom bounds (in degrees) of segment 

			IntList added = new IntList();			// Images added to current segment 

			if(p.p.p.debug.cluster || p.p.p.debug.model)
				PApplet.println("Finding media segments in cluster: "+getID()+" images.size():"+images.size()+" allImages.size():"+allImages.size());

			int count = 0;
			for(int i : allImages)							// Search for images at least stitchingMinAngle from each image
			{
				WMV_Image img = p.images.get(i);
			
				if(curImages.size() == 0)
				{
					curImages.append(img.getID());			// Add first image	
					added.append(count);					// Remove added image from list
				}
				else 
				{
					boolean found = false;					// Have we found a media segment for the image?
					int idx = 0;

					while(!found && idx < curImages.size())
					{
						int m = curImages.get(idx);
						if(p.images.get(m).getID() != img.getID())		// Don't compare image to itself
						{
							if((p.p.p.debug.cluster || p.p.p.debug.model) && p.p.p.debug.detailed)
								PApplet.println("Comparing image:"+img.getDirection()+" to m: "+p.images.get(m).getDirection() + " p.p.stitchingMinAngle:"+p.p.stitchingMinAngle);
							
							if(PApplet.abs(img.getDirection() - p.images.get(m).getDirection()) < p.p.stitchingMinAngle)
							{
								if(PApplet.abs(img.getElevation() - p.images.get(m).getElevation()) < p.p.stitchingMinAngle)
								{
									float direction = img.getDirection();
									float elevation = img.getElevation();
									
									direction = p.p.p.utilities.constrainWrap(direction, 0.f, 360.f);
									elevation = p.p.p.utilities.constrainWrap(elevation, -90.f, 90.f);
									
									if(direction < left) left = direction;
									if(direction > right) right = direction;
									if(elevation < bottom) bottom = elevation;
									if(elevation > top) top = elevation;

									if((p.p.p.debug.cluster || p.p.p.debug.model) && p.p.p.debug.detailed)
										PApplet.println("Added image:"+img.getID()+" to segment...");

									if(!curImages.hasValue(img.getID()))
										curImages.append(img.getID());		// -- Add video too?

									if(!added.hasValue(count))
										added.append(count);				// Remove added image from list

									found = true;
								}
							}
						}
						else if(allImages.size() == 1)			// Add last image
						{
							curImages.append(img.getID());		// -- Add video too?
							added.append(count);				// Remove added image from list
						}
						
						idx++;
					}
				}
				
				count++;
			}
			
			added.sort();
			for(int i=added.size()-1; i>=0; i--)
			{
				if((p.p.p.debug.cluster || p.p.p.debug.model) && p.p.p.debug.detailed)
					PApplet.println("Removing image ID:"+allImages.get(added.get(i)));
				allImages.remove(added.get(i));		// Remove images added to curSegment
			}
	
			if(curImages.size() == 1)			// Only one image
			{
				left = p.images.get(curImages.get(0)).getDirection();
				right = p.images.get(curImages.get(0)).getDirection();
				centerDirection = p.images.get(curImages.get(0)).getDirection();
				
				bottom = p.images.get(allImages.get(0)).getElevation();
				top = p.images.get(allImages.get(0)).getElevation();
				centerElevation = p.images.get(allImages.get(0)).getElevation();
				
				left = p.p.p.utilities.constrainWrap(left, 0.f, 360.f);
				right = p.p.p.utilities.constrainWrap(right, 0.f, 360.f);
				centerDirection = p.p.p.utilities.constrainWrap(centerDirection, 0.f, 360.f);
				bottom = p.p.p.utilities.constrainWrap(bottom, -90.f, 90.f);
				top = p.p.p.utilities.constrainWrap(top, -90.f, 90.f);
				centerElevation = p.p.p.utilities.constrainWrap(centerElevation, -90.f, 90.f);
				
//				if(left < 0.f)
//					left += 360.f;
//				
//				if(right > 360.f)
//					right -= 360.f;
//				
//				if(right < 0.f)
//					right += 360.f;
//							
//				if(top > 90.f)
//					top -= 90.f;
//				
//				if(bottom < -90.f)
//					bottom -= 360.f;
			}
			else
			{
				centerDirection = (right + left) / 2.f;
				centerElevation = (top + bottom) / 2.f;
			}

			segments.add( new WMV_MediaSegment( this, segments.size(), curImages, null, left, right, centerDirection, bottom, 
					      top, centerElevation) );

			if((p.p.p.debug.cluster || p.p.p.debug.model))
				PApplet.println("Added segment of size: "+curImages.size()+" to cluster segments... Left:"+left+" Center:"+centerDirection+" Right:"+right);
			
			done = (allImages.size() == 1 || allImages.size() == 0);
		}

		numSegments = segments.size();						// Number of media segments in the cluster
		if(numSegments > 0)
		{
			if(p.p.p.debug.cluster || p.p.p.debug.model)
				PApplet.println(" Created "+numSegments+" segments...");

		}
		else PApplet.println(" No segments added... cluster "+getID()+" has no images!");
	}

	/**
	 * @param newImage Image to add
	 * Add an image to the cluster
	 */
	void addImage(WMV_Image newImage)
	{
		if(!images.hasValue(newImage.getID()))
		{
			images.append(newImage.getID());
			mediaPoints++;
//			clusterMass = mediaPoints * p.p.mediaPointMass;	
		}
	}

	/**
	 * @param newPanorama Panorama to add
	 * Add a panorama to the cluster
	 */
	void addPanorama(WMV_Panorama newPanorama)
	{
		if(!panorama) panorama = true;
		
		if(!panoramas.hasValue(newPanorama.getID()))
		{
			panoramas.append(newPanorama.getID());
			mediaPoints++;
//			clusterMass = mediaPoints * 25.f * p.p.mediaPointMass;			
		}
	}

	/**
	 * @param newImage Image to add
	 * Add a video to the cluster
	 */
	void addVideo(WMV_Video newVideo)
	{
		if(!videos.hasValue(newVideo.getID()))
		{
			videos.append(newVideo.getID());
			mediaPoints++;
//			clusterMass = mediaPoints * p.p.mediaPointMass;		
		}
	}

	/**
	 * Empty this cluster of all media
	 */
	void empty()
	{
		images = new IntList();
		panoramas = new IntList();
		videos = new IntList();
		mediaPoints = 0;
		active = false;
		empty = true;
	}
	
	/**
	 * Initialize cluster from media associated with it; calculate location and assign media to it.
	 */
	void create() 						
	{			
		mediaPoints = 0;

		PVector newLocation = new PVector((float)0.0, (float)0.0, (float)0.0);
		empty();
				
		/* Find images associated with this cluster ID */
		for (int i = 0; i < p.images.size(); i++) 
		{
			WMV_Image curImg = (WMV_Image) p.images.get(i);

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
			WMV_Panorama curPano = (WMV_Panorama) p.panoramas.get(i);

			if (curPano.cluster == id) 			// If the image is assigned to this cluster
			{
				newLocation.add(curPano.getCaptureLocation());		// Move cluster towards the image
				if(!panoramas.hasValue(curPano.getID()))
				{
					if(p.p.p.debug.panorama)
					{
						PApplet.println("Adding panorama "+curPano.getID()+" to cluster:"+getID());
						PApplet.println("associatedPanoramas.size(): "+panoramas.size());
					}

					panoramas.append(curPano.getID());
					mediaPoints++;
				}
			}
		}

		/* Find videos associated with this cluster ID */
		for (int i = 0; i < p.videos.size(); i++) 
		{
			WMV_Video curVid = (WMV_Video) p.videos.get(i);

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

//			clusterMass = mediaPoints * p.p.mediaPointMass;			// Mass = 4 x number of media points
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
//		clusterMass = mediaPoints * p.p.mediaPointMass;			// Mass = 4 x number of media points

		active = true;
		empty = false;
	}

	/**
	 * Draw the cluster center
	 * @param hue 
	 */
	void drawCenter(int hue)
	{
		if(!empty)
		{
			p.p.p.pushMatrix();
			p.p.p.fill(hue,255,255);
			p.p.p.stroke(255-hue,255,255);
			p.p.p.translate(location.x, location.y, location.z);
			p.p.p.sphere(p.p.clusterCenterSize);
			p.p.p.popMatrix();
		}
	}

	void draw()
	{
		if(p.p.showUserPanoramas)
		{
			for(WMV_Panorama p : userPanoramas)
			{
				p.update();
				p.draw();
			}
		}

		if(p.p.showStitchedPanoramas)
		{
			for(WMV_Panorama p : stitchedPanoramas)
			{
				p.update();
				p.draw();
			}
		}
	}

	/**
	 * Stitch images based on current selection: if nothing selected, attempt to stitch all media segments in cluster
	 */
	public void stitchImages()
	{
		if(p.p.viewer.multiSelection || p.p.viewer.segmentSelection)
		{
			IntList valid = new IntList();
			for( int i : p.getSelectedImages() )
			{
				if(p.images.get(i).isVisible())
					valid.append(i);
			}

			if(p.p.p.debug.stitching) p.p.display.message("Stitching panorama out of "+valid.size()+" selected images from cluster #"+getID());
			
			WMV_Panorama pano = p.p.p.utilities.stitcher.stitch(p.p.p.library.getLibraryFolder(), valid, getID(), -1, p.getSelectedImages());

			if(pano != null)
			{
				if(p.p.p.debug.panorama || p.p.p.debug.stitching)
					PApplet.println("Adding panorama at location x:"+getLocation().x+" y:"+getLocation().y);

				pano.initializeSphere(pano.panoramaDetail);
				
				userPanoramas.add(pano);

//				p.p.viewer.selection = false;
//				p.p.viewer.multiSelection = false;
//				p.p.viewer.segmentSelection = false;
				p.deselectAllMedia(true);		// Deselect and hide all currently selected media 
				
//				if(userPanoramas.size() == 0)
//				{
//					userPanoramas.add(pano);
//				}
//				else
//				{
////					userPanoramas.set(0, pano);
//					userPanoramas.set(0, p.p.p.utilities.stitcher.combinePanoramas(userPanoramas.get(0), pano));	
//				}
			}
		}
		else
		{
			if(p.p.p.debug.stitching) p.p.display.message("Stitching "+segments.size()+" panoramas from media segments of cluster #"+getID());

			for(WMV_MediaSegment m : segments)			// Stitch panorama for each media segment
			{
				if(m.getImages().size() > 1)
				{
					IntList valid = new IntList();
					for( int i : m.getImages() )
					{
						if(p.images.get(i).isVisible())
							valid.append(i);
					}
					
					if(p.p.p.debug.stitching && p.p.p.debug.detailed) p.p.display.message(" Found "+valid.size()+" media in media segment #"+m.getID());
					
					if(p.p.angleThinning)				// Remove invisible images
					{
						IntList remove = new IntList();
						
						int count = 0;
						for(int v:valid)
						{
							if(!p.images.get(v).getThinningVisibility())
								remove.append(count);
							count++;
						}

						remove.sort();
						for(int i=remove.size()-1; i>=0; i--)
							valid.remove(remove.get(i));	
					}
					
					if(valid.size() > 1)
					{					
						WMV_Panorama pano = p.p.p.utilities.stitcher.stitch(p.p.p.library.getLibraryFolder(), valid, getID(), m.getID(), null);
						
						if(pano != null)
						{
							pano.initializeSphere(pano.panoramaDetail);
							stitchedPanoramas.add(pano);

							m.hide();

//							p.p.viewer.selection = false;
//							p.p.viewer.multiSelection = false;
//							p.p.viewer.segmentSelection = false;
							p.deselectAllMedia(false);

//							if(stitchedPanoramas.size() == 0)
//							{
//								stitchedPanoramas.add(pano);
//							}
//							else
//							{
////								stitchedPanoramas.set(0, pano);
//								stitchedPanoramas.set(0, p.p.p.utilities.stitcher.combinePanoramas(stitchedPanoramas.get(0), pano)); -- To finish
//							}
						}
					}
				}
			}
		}		
	}
	
	/**
	 * Analyze directions of all images and videos for Thinning Visibility Mode
	 */
	public void analyzeMediaDirections()
	{
//		if(p.p.p.debug.cluster || p.p.p.debug.model)
//			PApplet.println("analyzeAngles()... cluster images.size():"+images.size());
		float thinningAngle = p.p.thinningAngle;									// Angle to thin images and videos by
		int numPerimeterPts = PApplet.round(PApplet.PI * 2.f / thinningAngle);		// Number of points on perimeter == number of images visible
		int[] perimeterPoints = new int[numPerimeterPts];					// Points to compare each cluster image/video to
		float[] perimeterDistances = new float[numPerimeterPts];			// Distances of images associated with each point
		int videoIdxOffset = p.images.size();
		
		for(int i=0; i<numPerimeterPts; i++)
			perimeterPoints[i] = -1;										// Start with empty perimeter point

		for(int idx : images)
		{
			WMV_Image m = p.images.get(idx);
			float imgAngle = PApplet.radians(m.theta);
			m.setThinningVisibility(false);

			for(int i=0; i<numPerimeterPts; i++)
			{
				float ppAngle = i * (PApplet.PI * 2.f / numPerimeterPts);	// Angle of perimeter point i
				if(perimeterPoints[i] == -1)
				{
					perimeterPoints[i] = idx;
					perimeterDistances[i] = p.p.p.utilities.getAngularDistance(imgAngle, ppAngle);
				}
				else										
				{
					/* Compare image angular distance from point to current closest */
					float imgDist = p.p.p.utilities.getAngularDistance(imgAngle, ppAngle);		
					
					if(imgDist < perimeterDistances[i])
					{
						perimeterPoints[i] = m.getID();
						perimeterDistances[i] = imgDist;
					}
				}
			}
		}
		
		for(int idx : videos)
		{
			WMV_Video v = p.videos.get(idx);
			float vidAngle = PApplet.radians(v.theta);
			v.setThinningVisibility(false);

			for(int i=0; i<numPerimeterPts; i++)
			{
				float ppAngle = i * (PApplet.PI * 2.f / numPerimeterPts);					// Angle of perimeter point i
				if(perimeterPoints[i] == -1)
				{
					perimeterPoints[i] = idx;
					perimeterDistances[i] = p.p.p.utilities.getAngularDistance(vidAngle, ppAngle);
				}
				else											
				{
					/* Compare image angular distance from point to current closest */
					float vidDist = p.p.p.utilities.getAngularDistance(vidAngle, ppAngle);		
					if(vidDist < perimeterDistances[i])
					{
						perimeterPoints[i] = v.getID() + videoIdxOffset;
						perimeterDistances[i] = vidDist;
					}
				}
			}
		}
		
		for(int i=0; i<numPerimeterPts; i++)
		{
			int idx = perimeterPoints[i];
			if(idx != -1)
			{
				if(idx < videoIdxOffset)
				{
//					PApplet.println("Thinning visibility true for image:"+idx+" i:"+i+" dist:"+perimeterDistances[i]);
					p.images.get(idx).setThinningVisibility(true);
				}
				else
				{
//					PApplet.println("Thinning visibility true for video:"+(idx-videoIdxOffset)+" i:"+i);
					p.videos.get(idx-videoIdxOffset).setThinningVisibility(true);
				}
			}
		}
	}
	
	/**
	 * @param id Media segment ID
	 * @return Cluster media segment with given ID
	 */
	public WMV_MediaSegment getMediaSegment(int id)
	{
		return segments.get(id);
	}
	
	/**
	 * @return Cluster media segments
	 */
	public ArrayList<WMV_MediaSegment> getMediaSegments()
	{
		return segments;
	}
	
	/**
	 * @return Images associated with cluster
	 */
	public ArrayList<WMV_Image> getImages()
	{
		ArrayList<WMV_Image> cImages = new ArrayList<WMV_Image>();
		for(int i : images)
		{
			cImages.add(p.images.get(i));
		}
		return cImages;
	}

	/**
	 * @return Panoramas associated with cluster
	 */
	public ArrayList<WMV_Panorama> getPanoramas()
	{
		ArrayList<WMV_Panorama> cPanoramas = new ArrayList<WMV_Panorama>();
		for(int i : panoramas)
		{
			cPanoramas.add(p.panoramas.get(i));
		}
		return cPanoramas;
	}
	
	/**
	 * @return Videos associated with cluster
	 */
	public ArrayList<WMV_Video> getVideos()
	{
		ArrayList<WMV_Video> cVideos = new ArrayList<WMV_Video>();
		for(int i : videos)
		{
			cVideos.add(p.videos.get(i));
		}
		return cVideos;
	}
	
	/**
	 * @return Are any images or videos in the cluster currently active?
	 */
	public boolean mediaAreActive()
	{
		boolean active = false;

		if(images.size() > 0)
		{
			for(int imgIdx : images)
			{
				WMV_Image i = p.images.get(imgIdx);
				if(i.isActive())
					active = true;
			}
		}

		if(panoramas.size() > 0)
		{
			for(int panoIdx : panoramas)
			{
				WMV_Panorama n = p.panoramas.get(panoIdx);
				if(n.isActive())
					active = true;
			}
		}

		if(videos.size() > 0)
		{
			for(int vidIdx : videos)
			{
				WMV_Video v = p.videos.get(vidIdx);
				if(v.isActive())
					active = true;
			}
		}

		return active;
	}

	/**
	 * @param newBaseTimeScale New baseTimeScale
	 * Set base time scale, i.e. unit to cycle through during simulation (0 = minute, 1 = hour, 2 = day, 3 = month, 4 = year)
	 */
//	public void setBaseTimeScale(int newBaseTimeScale)
//	{
//		baseTimeScale = newBaseTimeScale;
//	}

	/**
	 * Attract the camera
	 */
	void attractViewer()
	{
		if(isAttractor && !empty)												// Attractor clusters do not need to be active, but attract while empty
			p.p.viewer.attraction.add( getAttractionForce() );		// Add attraction force to camera 
	}

	/**
	 * @return Distance from cluster center to camera
	 */
	float getClusterDistance()       // Find distance from camera to point in virtual space where photo appears           
	{
		return PVector.dist(location, p.p.viewer.getLocation());
	}

	/**
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
		float strength;
		
		if(distance > p.p.viewer.getClusterNearDistance())
		{
			strength = (clusterGravity * mass * p.p.viewer.cameraMass) / (distance * distance);	// Calculate strength
		}
		else				// Reduce strength of attraction at close distance
		{
			float diff = p.p.viewer.getClusterNearDistance() - distance;
			float factor = 0.5f - PApplet.map(diff, 0.f, p.p.viewer.getClusterNearDistance(), 0.f, 0.5f);
			strength = (clusterGravity * mass * p.p.viewer.cameraMass) / (distance * distance) * factor;
		}
		
		force.mult(strength);
		
		if(p.p.drawForceVector)
			p.p.display.drawForceVector(force);
		
		return force; 								// Return force to be applied
	}

	/**
	 * @return ID of first time segment in cluster
	 */
	public int getFirstTimeSegment()
	{
		if(timeline.size() > 0)
			return timeline.get(0).getID();
		else
			return -1;
	}
	
	/**
	 * @param cluster Cluster to merge with
	 * Merge this cluster with given cluster. Empty and make the given cluster non-active.
	 */
	void absorbCluster(WMV_Cluster cluster)
	{
//		if(p.p.p.debug.clusters)
//			p.p.display.sendUserMessage("Merging cluster "+clusterID+" with "+mCluster.clusterID);

		/* Find images associated with cluster */
		for (int i = 0; i < p.images.size(); i++) 
		{
			WMV_Image curImg = p.images.get(i);

			if (curImg.cluster == cluster.getID()) 				// If the image is assigned to this cluster
			{
				curImg.cluster = id;
				addImage(curImg);
			}
		}

		/* Find panoramas associated with cluster */
		for (int i = 0; i < p.panoramas.size(); i++) 
		{
			WMV_Panorama curPano = p.panoramas.get(i);

			if (curPano.cluster == cluster.getID()) 				// If the image is assigned to this cluster
			{
				curPano.cluster = id;
				addPanorama(curPano);
			}
		}

		/* Find videos associated with cluster */
		for (int i = 0; i < p.videos.size(); i++) 
		{
			WMV_Video curVid = p.videos.get(i);

			if (curVid.cluster == cluster.getID()) 				// If the image is assigned to this cluster
			{
				curVid.cluster = id;
				addVideo(curVid);
			}
		}

		/* Empty merged cluster */
		cluster.images = new IntList();
		cluster.panoramas = new IntList();
		cluster.videos = new IntList();
		cluster.mediaPoints = 0;
		cluster.active = false;
		cluster.empty = true;
	}
	
	/**
	 * Analyze associated media capture times (Need to implement: find on which scales it operates, i.e. minute, day, month, year)
	 */
	public void analyzeMedia() 
	{
		calculateDimensions();		// Calculate cluster dimensions (bounds)
		calculateTimes();			// Calculate cluster times
		createDateline();			// Create dates histograms and analyze for date segments
		if(dateline.size()>1)
			createTimelinesByDate();
		createTimeline();			// Create times histograms and analyze for time segments
	}
	
	/**
	 * Create dateline for cluster
	 */
	void createDateline()
	{
		FloatList mediaDates = new FloatList();							// List of times to analyze
		IntList dateSegments;														// Temporary time point list for finding duplicates

		/* Get times of all media of all types in this cluster */
		for(int i : images) mediaDates.append( p.images.get(i).time.getDate() );
		for(int n : panoramas) mediaDates.append( p.panoramas.get(n).time.getDate() );
		for(int v : videos) mediaDates.append( p.videos.get(v).time.getDate() );

		/* Create cluster-specific times histogram */
		for (int i = 0; i < p.p.clusterDatePrecision; i++) // Initialize histogram
			clusterDatesHistogram[i] = 0;
		
		for (int i = 0; i < mediaDates.size(); i++) 							// Fill cluster dates histogram
		{
			int idx = PApplet.round( PApplet.constrain(PApplet.map(mediaDates.get(i), 0.f, 1.f, 0.f, 
									 p.p.clusterDatePrecision), 0.f, p.p.clusterDatePrecision) );

//			PApplet.println("------------> cluster id:"+getID()+" idx:"+idx+" adding date:"+mediaDates.get(i));
			clusterDatesHistogram[idx]++;
		}

//		if(p.p.p.debug.time)
//			PApplet.println("Getting cluster date segments for cluster#"+getID());
		
		dateline = getTimeSegments(clusterDatesHistogram, p.p.clusterDatePrecision);	// Get relative (cluster) time segments
		dateline.sort(WMV_TimeSegment.GMV_TimeMidpointComparator);				// Sort dateline points 
		
		/* Debugging */
//		if(p.p.p.debug.time && clusterDates.size()>1)
//		{
//			PApplet.println("--> Cluster "+id+" with "+clusterDates.size()+" different cluster dates...");
//
//			int ct = 0;
//			for(float f:clusterDates)
//			{
//				PApplet.println("Cluster "+id+", cluster date #"+(ct++)+": "+f);
//			}
//		}
		
//		if(p.p.p.debug.time && fieldDates.size()>1)
//		{
//			PApplet.println("--> Cluster "+id+" with "+fieldDates.size()+" different field dates...");

//			int ct = 0;
//			for(float f:fieldDates)
//			{
//				PApplet.println("Cluster "+id+", cluster field date #"+(ct++)+": "+f);
//			}
//		}
		
		if(dateline.size() == 0)
		{
			PApplet.println("-------> Cluster "+getID()+" dateline has no points! "+getID()+" images.size():"+images.size()+" mediaDates.size():"+mediaDates.size());
			empty();
		}
	}
	
	/**
	 * Create timeline for each date on dateline
	 */
	void createTimelinesByDate()
	{
		timelines = new ArrayList<ArrayList<WMV_TimeSegment>>();
		int curTimeline = 0;
		for(WMV_TimeSegment d : dateline)			// For each date on dateline
		{
			int date = (int) p.p.p.utilities.round(d.getCenter()/p.p.clusterTimePrecision, 3);
			if(p.p.p.debug.time)
				PApplet.println("Cluster #"+getID()+"... creating timeline for date:"+date);
			FloatList mediaTimes = new FloatList();							// List of times to analyze

			/* Get times of all media of all types in this cluster */
			for(int i : images)
			{
				WMV_Image img = p.images.get(i);
				
//				PApplet.println("img.getDate():"+img.getDate()+" date:"+date);
//				PApplet.println("p.p.p.utilities.round(img.getDate(), 3):"+p.p.p.utilities.round(img.getDate(), 3)+" date:"+date);
				if((int)p.p.p.utilities.round(img.getDate(), 3) == date)
				{
					PApplet.println("Adding to mediaTimes.size():"+mediaTimes.size());
					mediaTimes.append( p.images.get(i).time.getTime() );
				}
				else
				{
					PApplet.println(".. not adding to mediaTimes.size() p.p.p.utilities.round(img.getDate(), 3):"+p.p.p.utilities.round(img.getDate(), 3)+" date:"+date+" mediaTimes.size()"+mediaTimes.size());
				}
			}
			for(int n : panoramas) 
			{
				WMV_Panorama pano = p.panoramas.get(n);
				if((int)p.p.p.utilities.round(pano.getDate(), 3) == date)
					mediaTimes.append( p.panoramas.get(n).time.getTime() );
			}
			for(int v : videos)
			{
				WMV_Video vid = p.videos.get(v);
				if((int)p.p.p.utilities.round(vid.getDate(), 3) == date)
					mediaTimes.append( p.videos.get(v).time.getTime() );
			}
			
			/* Create histogram of cluster media times */
			for (int i = 0; i < p.p.clusterTimePrecision; i++) // Initialize histogram
				clusterTimesHistogram[i] = 0;

			for (int i = 0; i < mediaTimes.size(); i++) 							// Fill cluster times histogram
			{
//				int idx = PApplet.round(mediaTimes.get(i));
//				PApplet.println("<-------->> idx:"+idx+" mediaTimes.get(i):"+mediaTimes.get(i));
				int idx = PApplet.round(PApplet.constrain(PApplet.map(mediaTimes.get(i), 0.f, 1.f, 0.f, 
						p.p.clusterTimePrecision - 1), 0.f, p.p.clusterTimePrecision - 1.f));

				clusterTimesHistogram[idx]++;
			}

			if(clusterTimesHistogram.length > 0)
			{
				PApplet.println("...createTimelinesByDate... mediaTimes.size():"+mediaTimes.size());
				timelines.add(getTimeSegments(clusterTimesHistogram, p.p.clusterTimePrecision));		// Calculate and add timeline to list
				if(timelines.get(curTimeline) != null)
					timelines.get(curTimeline).sort(WMV_TimeSegment.GMV_TimeMidpointComparator);		// Sort timeline points 
			}
			
			/* Debugging */
			if(p.p.p.debug.time && clusterTimes.size()>1)
			{
				PApplet.println("--> Cluster "+id+" with "+clusterTimes.size()+" different cluster times...");

				int ct = 0;
				for(float f:clusterTimes)
				{
					PApplet.println("Cluster "+id+", cluster time #"+(ct++)+": "+f);
				}
			}
//			if(p.p.p.debug.time && fieldTimes.size()>1)
//			{
//				PApplet.println("--> Cluster "+id+" with "+fieldTimes.size()+"different field times...");
//
//				int ct = 0;
//				for(float f:fieldTimes)
//				{
//					PApplet.println("Cluster "+id+", cluster field time #"+(ct++)+": "+f);
//				}
//			}

			if(timelines.get(curTimeline) != null)
			{
				if(timelines.get(curTimeline).size() == 0)
				{
					PApplet.println("Cluster #"+getID()+" timeline #"+curTimeline+" has no points!  images.size():"+images.size()+" panoramas.size():"+panoramas.size()+" videos.size():"+videos.size());
					empty();
				}
			}
			else
			{
				PApplet.println("Cluster #"+getID()+" timeline #"+curTimeline+" is NULL!  images.size():"+images.size()+" panoramas.size():"+panoramas.size()+" videos.size():"+videos.size());
			}

			curTimeline++;
		}
	}
	
	/**
	 * Create date-independent timeline for cluster
	 */
	void createTimeline()
	{
		FloatList mediaTimes = new FloatList();							// List of times to analyze
		IntList timeSegments;														// Temporary time point list for finding duplicates
		
		/* Get times of all media of all types in this cluster */
		for(int i : images)
		{
			mediaTimes.append( p.images.get(i).time.getTime() );
		}
		for(int n : panoramas) 
		{
			mediaTimes.append( p.panoramas.get(n).time.getTime() );
		}
		for(int v : videos)
		{
			mediaTimes.append( p.videos.get(v).time.getTime() );
		}
		/* Create cluster-specific times histogram */
		for (int i = 0; i < p.p.clusterTimePrecision; i++) // Initialize histogram
			clusterTimesHistogram[i] = 0;
		
		for (int i = 0; i < mediaTimes.size(); i++) 							// Fill cluster times histogram
		{
			int idx = PApplet.round(PApplet.constrain(PApplet.map(mediaTimes.get(i), 0.f, 1.f, 0.f, 
									p.p.clusterTimePrecision - 1), 0.f, p.p.clusterTimePrecision - 1.f));
			clusterTimesHistogram[idx]++;
		}

		if(clusterTimesHistogram.length > 0)
		{
			timeline = getTimeSegments(clusterTimesHistogram, p.p.clusterTimePrecision);	// Get relative (cluster) time segments
			if(timeline != null)
				timeline.sort(WMV_TimeSegment.GMV_TimeMidpointComparator);				// Sort timeline points 
		}

		/* Debugging */
		if(p.p.p.debug.time && clusterTimes.size()>1)
		{
			PApplet.println("--> Cluster "+id+" with "+clusterTimes.size()+" different cluster times...");

			int ct = 0;
			for(float f:clusterTimes)
			{
				PApplet.println("Cluster "+id+", cluster time #"+(ct++)+": "+f);
			}
		}
//		if(p.p.p.debug.time && fieldTimes.size()>1)
//		{
//			PApplet.println("--> Cluster "+id+" with "+fieldTimes.size()+"different field times...");
//
//			int ct = 0;
//			for(float f:fieldTimes)
//			{
//				PApplet.println("Cluster "+id+", cluster field time #"+(ct++)+": "+f);
//			}
//		}

		if(timeline != null)
		{
			if(timeline.size() == 0)
			{
				PApplet.println("Cluster #"+getID()+" timeline has no points!  images.size():"+images.size()+" panoramas.size():"+panoramas.size()+" videos.size():"+videos.size());
				empty();
			}
		}
		else
		{
			PApplet.println("Cluster #"+getID()+" timeline is NULL!  images.size():"+images.size()+" panoramas.size():"+panoramas.size()+" videos.size():"+videos.size());
			empty();
		}

//		if(timeline.size() == 0)
//		{
//			PApplet.println("Cluster timeline has no points! "+getID()+" images.size():"+images.size()+" panoramas.size():"+panoramas.size());
//			empty();
//		}
	}
	
	/** 
	 * Update cluster time loop
	 */
	void updateTime()
	{
		if(timeFading && !dateFading && p.p.p.frameCount % timeUnitLength == 0)
		{
			currentTime++;															// Increment cluster time

			if(currentTime > timeCycleLength)
				currentTime = 0;

			if(p.p.p.debug.field && currentTime > timeCycleLength + defaultMediaLength * 0.25f)
			{
				if(p.p.getCurrentField().mediaAreActive())
				{
					if(p.p.p.debug.detailed)
						PApplet.println("Media still active...");
				}
				else
				{
					currentTime = 0;
					if(p.p.p.debug.detailed)
						PApplet.println("Reached end of day at p.frameCount:"+p.p.p.frameCount);
				}
			}
		}
		
		if(dateFading && !timeFading && p.p.p.frameCount % dateUnitLength == 0)
		{
			currentDate++;															// Increment cluster date

			if(currentDate > dateCycleLength)
				currentDate = 0;

			if(p.p.p.debug.field && currentDate > dateCycleLength + defaultMediaLength * 0.25f)
			{
				if(p.p.getCurrentField().mediaAreActive())
				{
					if(p.p.p.debug.detailed)
						PApplet.println("Media still active...");
				}
				else
				{
					currentDate = 0;
					if(p.p.p.debug.detailed)
						PApplet.println("Reached end of day at p.frameCount:"+p.p.p.frameCount);
				}
			}
		}
	}
	
	public WMV_TimeSegment getFirstTimeSegmentForDate(float date)
	{
		boolean found = false;
		int timelineID = 0;
		
		if(dateline != null)
		{
			for(WMV_TimeSegment t : dateline)		// Look through cluster dates for date
			{
				if(!found)						
				{	
					if(t.getCenter() == date)										// If cluster has date,
					{
						found = true;												// destination cluster has been found
						//					PApplet.println("found cluster with date... "+id+" curTimeSegment:"+curTimeSegment+" date:"+date);
					}
					else
						timelineID++;
				}
			}

			if(found)
			{
				if(dateline.size()>1)
					return timelines.get(timelineID).get(0);
				else
					return timeline.get(0);
			}
			else
			{
//				if(p.p.p.debug.cluster || p.p.p.debug.time)
//					PApplet.println("Date doesn't exist in cluster #"+getID()+"... "+date);
				return null;
			}
		}
		else 
		{
			if(p.p.p.debug.cluster || p.p.p.debug.time)
			{
				PApplet.println("Cluster #"+getID()+" has no dateline but has "+mediaPoints+" media points!!");
			}
			return null;
		}
	}
	
	public WMV_TimeSegment getLastTimeSegmentForDate(float date)
	{
		boolean found = false;
		int timelineID = 0;
//		float date = p.getCurrentField().dateline.get(dateSegment).getCenter();
		
		if(dateline != null)
		{
			for(int index=dateline.size()-1; index >= 0; index--)					// Look through cluster dates for date
			{
				WMV_TimeSegment t = dateline.get(index);
				if(!found)						
				{	
					if(t.getCenter() == date)										// If cluster has date,
						found = true;												// destination cluster has been found
					else
						timelineID++;
				}
			}

			if(found)
			{
				if(dateline.size()>1)
					return timelines.get(timelineID).get(timelines.get(timelineID).size()-1);
				else
					return timeline.get(timeline.size()-1);
			}
			else
			{
				PApplet.println("Date doesn't exist in cluster #"+getID()+"... "+date);
				return null;
			}
		}
		else 
		{
			PApplet.println("Cluster #"+getID()+" has no dateline... ");
			return null;
		}
	}

	/**
	 * Calculate low and high values for time and date for each media point
	 */
	void calculateTimes()
	{
		float longestDayLength = (float) -1.;			// Length of the longest day
		boolean initImageTime = true, initPanoramaTime = true, initVideoTime = true, 
				initImageDate = true, initPanoramaDate = true, initVideoDate = true;	

//		if(p.p.p.debug.cluster && (images.size() != 0 || panoramas.size() != 0 || videos.size() != 0))
//			PApplet.println("Analyzing media times in cluster "+id+" ..." + " associatedImages.size():"+images.size()+" associatedPanoramas:"+panoramas.size()+" associatedVideos:"+videos.size());
			
		ArrayList<WMV_Image> cImages = new ArrayList<WMV_Image>();
		ArrayList<WMV_Panorama> cPanoramas = new ArrayList<WMV_Panorama>();
		ArrayList<WMV_Video> cVideos = new ArrayList<WMV_Video>();
		
		for(int i : images)
			cImages.add(p.images.get(i));
		for(int i : panoramas)
			cPanoramas.add(p.panoramas.get(i));
		for(int i : videos)
			cVideos.add(p.videos.get(i));

		for (WMV_Image i : cImages) 			// Iterate over cluster images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
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
		
		
		for (WMV_Panorama n : cPanoramas) 			// Iterate over cluster panoramas to calculate X,Y,Z and T (longitude, latitude, altitude and time)
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
		
		for (WMV_Video v : cVideos) 			// Iterate over cluster videos to calculate X,Y,Z and T (longitude, latitude, altitude and time)
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
	 * Perform clustering to find cluster time segments from media capture times
	 * @param times List of times
	 * @param timePrecision Number of histogram bins
	 * @return Time segments
	 */
	ArrayList<WMV_TimeSegment> getTimeSegments(int histogram[], int timePrecision)				// -- clusterTimelineMinPoints!!								
	{
		/* Initialize list of media times */
//		ArrayList<GMV_TimeSegment> mediaTimes = new ArrayList<GMV_TimeSegment>();
		if(p.p.p.debug.time)
			PApplet.println("-------> Cluster #"+getID()+" Creating histogram of length:"+histogram.length);
		IntList mediaTimes = new IntList();
		
		for (int i=0; i<timePrecision; i++) 				
		{
			for(int j=0; j<histogram[i]; j++)							// Create list of all media points
			{
//				PApplet.println("--- time index:"+i+" histogram[i]:"+histogram[i]+" j:"+j);
//				PApplet.println("---> i:"+i+" j:"+j+" histogram[i]:"+histogram[i]+" cluster #"+getID());
//				mediaTimes.add(new GMV_TimeSegment(0, i, 0, 0));		// Don't need ID, upper or lower values
				
				mediaTimes.append(i);
			}
		}
		
//		mediaTimes.sort(GMV_TimeSegment.GMV_TimeMidpointComparator);				// Sort media times
		
		if(mediaTimes.size() > 0)
		{
			ArrayList<WMV_TimeSegment> timeSegments = new ArrayList<WMV_TimeSegment>();

//			PApplet.println("id:"+getID()+" mediaTimes...");
//			for(int i:mediaTimes)
//			{
//				PApplet.print(" "+i);
//			}
//			PApplet.println("");
			
			int idx = 0;
			int curLower = mediaTimes.get(idx);
			int curUpper = mediaTimes.get(idx);
			int lastTime = Integer.valueOf(curLower);
			int count = 0;
			
			while(idx < mediaTimes.size())
			{
				int t = mediaTimes.get(idx);
//				PApplet.println(">> idx:"+idx+" t:"+t);

				if(t != lastTime)
				{
					if(t - lastTime == 1)					// If advanced by one since last media time, extend the segment 
					{
						curUpper = Integer.valueOf(t);		// Advance current upper bound by 1
					}
					else									// Otherwise, save the segment and start a new one
					{
						int center;
						if(curUpper == curLower)
							center = Integer.valueOf(curUpper);					// If upper and lower are same, set center to that value
						else
							center = PApplet.round((curUpper+curLower)/2);		// Average upper and lower bounds to find center

						if(p.p.p.debug.time && p.p.p.debug.detailed)
							PApplet.println("Cluster #"+getID()+"... Creating time segment... center:"+(center)+" curUpper:"+(curUpper)+" curLower:"+(curLower));
						timeSegments.add(new WMV_TimeSegment(count, center, curUpper, curLower));
						
						/* Start a new segment with current time */
						curLower = Integer.valueOf(t);
						curUpper = Integer.valueOf(t);
						count++;
					}
				}
				
				lastTime = Integer.valueOf(t);
				idx++;
				
				if(idx >= mediaTimes.size())				// Last media time
				{
					int center;
					if(curUpper == curLower)
						center = curUpper;
					else
						center = PApplet.round((curUpper+curLower)/2);

//					PApplet.println("Cluster #"+getID()+"... Creating LAST time segment... center:"+center+" curUpper:"+curUpper+" curLower:"+curLower);
					timeSegments.add(new WMV_TimeSegment(count, center, curUpper, curLower));
					count++;
				}
			}
			
			return timeSegments;			// Return cluster list
		}
		else
		{
			if(p.p.p.debug.time)
				PApplet.println("cluster:"+getID()+" getTimeSegments() == null but has mediaPoints:"+mediaPoints);
			return null;		
		}
	}
	
	
	/**
	 * Calculate high and low longitude, latitude and altitude for cluster
	 */
	void calculateDimensions()
	{
//		if(p.p.p.debug.field) PApplet.println("Calculating cluster dimensions...");

		boolean init = true;	

		for (int img : images) 				// Iterate over associated images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		{
			WMV_Image i = p.images.get(img);
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
			WMV_Panorama n = p.panoramas.get(pano);
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
			WMV_Video v = p.videos.get(vid);
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
	 * @return This cluster as a waypoint for navigation
	 */
	WMV_Waypoint getClusterAsWaypoint()
	{
		WMV_Waypoint result = new WMV_Waypoint(getID(), getLocation());
		return result;
	}

	public ArrayList<WMV_TimeSegment> getTimeline()
	{
		return timeline;
	}

	public ArrayList<WMV_TimeSegment> getDateline()
	{
		return dateline;
	}
	
//	public FloatList getClusterTimes()
//	{
//		return clusterTimes;
//	}
//
//	public FloatList getClusterTimesLowerBounds()
//	{
//		return clusterTimesLowerBounds;
//	}
//
//	public FloatList getClusterTimesUpperBounds()
//	{
//		return clusterTimesUpperBounds;
//	}

//	public FloatList getClusterDates()
//	{
//		return clusterDates;
//	}
//
//	public FloatList getClusterDatesLowerBounds()
//	{
//		return clusterDatesLowerBounds;
//	}
//
//	public FloatList getClusterDatesUpperBounds()
//	{
//		return clusterDatesUpperBounds;
//	}
	
	public void setActive(boolean newActive)
	{
		active = newActive;
	}
	
	public void setEmpty(boolean newEmpty)
	{
		empty = newEmpty;
	}
		
	/**
	 * Set this cluster as an attractor
	 */
	public void setAttractor(boolean state)
	{
		isAttractor = state;
		if(p.p.p.debug.viewer && isAttractor())
			p.p.display.message("Set cluster isAttractor to true:"+getID()+" attraction force mag:"+getAttractionForce().mag());
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
	
	public void setMass(float newMass)
	{
		clusterMass = newMass;
	}
	
	public boolean isAttractor()
	{
		return isAttractor;
	}
	
	public float getClusterMass()
	{
		return clusterMass;
	}
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