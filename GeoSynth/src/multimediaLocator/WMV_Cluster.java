package multimediaLocator;
import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PVector;
//import processing.data.FloatList;
import processing.data.IntList;
//import processing.video.Movie;

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
	
	/* Interaction */
	private boolean selected = false;

	/* Panorama */
	ArrayList<WMV_Panorama> stitchedPanoramas, userPanoramas;

	/* Physics */
	private boolean isAttractor;				// Whether cluster is attracting viewer
	private float clusterGravity = 0.1333f;		// Cluster gravitational pull
	private float clusterMass = 1.5f;			// Cluster mass		-- No longer tied to value of mediaPoints
	private float farMassFactor = 8.f;			// How much more mass to give distant attractors to speed up navigation?
	
	/* Time */
	public boolean timeFading = false;					// Does time affect photos' brightness? (true = yes; false = no)
	public boolean dateFading = true;					// Does time affect photos' brightness? (true = yes; false = no)
	public boolean paused = false;						// Time is paused

	public boolean showAllTimeSegments = true;			// Show all time segments (true) or show only current cluster (false)?

	public int currentTime = 0;							// Time units since start of time cycle (day / month / year)
	public int timeCycleLength = 250;					// Length of main time loop in frames
	public int timeUnitLength = 1;						// How many frames between time increments
	public float timeInc = timeCycleLength / 30.f;			

	public int currentDate = 0;							// Current date in timeline	-- Need to implement!!
	
	public int defaultMediaLength = 125;					// Default frame length of media in time cycle
	
	private ArrayList<WMV_Date> dateline;								// Capture dates for this cluster
	private ArrayList<WMV_TimeSegment> timeline;						// Date-independent capture times for this cluster
	private ArrayList<ArrayList<WMV_TimeSegment>> timelines;	

	/* Segmentation */
	public ArrayList<WMV_MediaSegment> segments;		// List of arrays corresponding to each segment of images
	private int numSegments = 0;						// Number of segments of the cluster
	
	/* Media */
	public int mediaCount;			// No. of media associated with this cluster
	public IntList images;			// Images associated with this cluster
	public IntList panoramas;			// Panoramas associated with this cluster
	public IntList videos;			// Videos associated with this cluster
	
	private float highLongitude, lowLongitude, highLatitude, lowLatitude, 		// - NEED TO CALCULATE!	
			  	 highAltitude, lowAltitude;		
	
	public float highTime, lowTime, highDate, lowDate;
	public float highImageTime = -1000000, lowImageTime = 1000000, 
		  highPanoramaTime = -1000000, lowPanoramaTime = 1000000, 
		  highVideoTime = -1000000, lowVideoTime = 1000000; 	
	public float highImageDate = -1000000, lowImageDate = 1000000, 
		  highPanoramaDate = -1000000, lowPanoramaDate = 1000000,
	 	  highVideoDate = -1000000, lowVideoDate = 1000000;
	
//	private float longestImageDayLength = -1000000, longestPanoramaDayLength = -1000000, longestVideoDayLength = -1000000;	

	/* Video */
	boolean video = false;
	
	/* Panorama */
	public boolean panorama = false;					// Cluster has panorama files?
	IntList valid;										// List of images that are good stitching candidates
		
	WMV_Field p;										// Parent field

	WMV_Cluster(WMV_Field parent, int _clusterID, float _x, float _y, float _z) {
		p = parent;
		location = new PVector(_x, _y, _z);
		id = _clusterID;

		images = new IntList();
		panoramas = new IntList();
		videos = new IntList();
		segments = new ArrayList<WMV_MediaSegment>();
		
		stitchedPanoramas = new ArrayList<WMV_Panorama>();
		userPanoramas = new ArrayList<WMV_Panorama>();
		mediaCount = 0;

		timeline = new ArrayList<WMV_TimeSegment>();
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
			
			float left = p.getImages().get(allImages.get(0)).getDirection();
			float right = p.getImages().get(allImages.get(0)).getDirection();
			float centerDirection = p.getImages().get(allImages.get(0)).getDirection();

			float bottom = p.getImages().get(allImages.get(0)).getElevation();
			float top = p.getImages().get(allImages.get(0)).getElevation();
			float centerElevation = p.getImages().get(allImages.get(0)).getElevation();

			segments.add( new WMV_MediaSegment( this, 0, curImages, null, right, left, centerDirection, 
												top, bottom, centerElevation) );

			if(p.debugSettings.cluster || p.debugSettings.model)
				PApplet.println("Added media segment in cluster: "+getID()+" with single image...");

			done = true;
		}
		
		while(!done)
		{
			IntList curImages = new IntList();
			float left = 360.f, right = 0.f, centerDirection;		// Left and right bounds (in degrees) of segment 
			float bottom = 100.f, top = -100.f, centerElevation;	// Top and bottom bounds (in degrees) of segment 

			IntList added = new IntList();			// Images added to current segment 

			if(p.debugSettings.cluster || p.debugSettings.model)
				PApplet.println("Finding media segments in cluster: "+getID()+" images.size():"+images.size()+" allImages.size():"+allImages.size());

			int count = 0;
			for(int i : allImages)							// Search for images at least stitchingMinAngle from each image
			{
				WMV_Image img = p.getImage(i);
			
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
						if(p.getImage(m).getID() != img.getID())		// Don't compare image to itself
						{
//							if((p.debugSettings.cluster || p.debugSettings.model) && p.debugSettings.detailed)
//								PApplet.println("Comparing image:"+img.getDirection()+" to m: "+p.getImage(m).getDirection() + " p.p.stitchingMinAngle:"+p.worldSettings.stitchingMinAngle);
							
							if(PApplet.abs(img.getDirection() - p.getImage(m).getDirection()) < p.worldSettings.stitchingMinAngle)
							{
								if(PApplet.abs(img.getElevation() - p.getImage(m).getElevation()) < p.worldSettings.stitchingMinAngle)
								{
									float direction = img.getDirection();
									float elevation = img.getElevation();
									
									direction = p.utilities.constrainWrap(direction, 0.f, 360.f);
									elevation = p.utilities.constrainWrap(elevation, -90.f, 90.f);
									
									if(direction < left) left = direction;
									if(direction > right) right = direction;
									if(elevation < bottom) bottom = elevation;
									if(elevation > top) top = elevation;

									if((p.debugSettings.cluster || p.debugSettings.model) && p.debugSettings.detailed)
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
				if((p.debugSettings.cluster || p.debugSettings.model) && p.debugSettings.detailed)
					PApplet.println("Removing image ID:"+allImages.get(added.get(i)));
				allImages.remove(added.get(i));		// Remove images added to curSegment
			}
	
			if(curImages.size() == 1)			// Only one image
			{
				left = p.getImage(curImages.get(0)).getDirection();
				right = p.getImage(curImages.get(0)).getDirection();
				centerDirection = p.getImage(curImages.get(0)).getDirection();
				
				bottom = p.getImage(allImages.get(0)).getElevation();
				top = p.getImage(allImages.get(0)).getElevation();
				centerElevation = p.getImage(allImages.get(0)).getElevation();
				
				left = p.utilities.constrainWrap(left, 0.f, 360.f);
				right = p.utilities.constrainWrap(right, 0.f, 360.f);
				centerDirection = p.utilities.constrainWrap(centerDirection, 0.f, 360.f);
				bottom = p.utilities.constrainWrap(bottom, -90.f, 90.f);
				top = p.utilities.constrainWrap(top, -90.f, 90.f);
				centerElevation = p.utilities.constrainWrap(centerElevation, -90.f, 90.f);
				
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

			if((p.debugSettings.cluster || p.debugSettings.model))
				PApplet.println("Added segment of size: "+curImages.size()+" to cluster segments... Left:"+left+" Center:"+centerDirection+" Right:"+right);
			
			done = (allImages.size() == 1 || allImages.size() == 0);
		}
		
		for(int i : panoramas)
		{
			float left = p.utilities.constrainWrap(p.getPanorama(i).getDirection(), 0.f, 360.f);
			float right = p.utilities.constrainWrap(left, 0.f, 360.f);
			float centerDirection = p.utilities.constrainWrap(left, 0.f, 360.f);
			float bottom = -90.f;
			float top = 90.f;
			float centerElevation = 0.f;

			segments.add( new WMV_MediaSegment( this, segments.size(), null, null, left, right, centerDirection, bottom, 
					top, centerElevation) );
		}

		numSegments = segments.size();						// Number of media segments in the cluster
		if(numSegments > 0)
		{
			if(p.debugSettings.cluster || p.debugSettings.model)
				PApplet.println(" Created "+numSegments+" media segments...");

		}
		else if(p.debugSettings.cluster) 
			PApplet.println(" No media segments added... cluster "+getID()+" has no images!");
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
			mediaCount++;
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
			mediaCount++;
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
			mediaCount++;
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
		mediaCount = 0;
		active = false;
		empty = true;
	}
	
	/**
	 * Initialize cluster from media associated with it; calculate location and assign media to it.
	 */
	void create() 						
	{			
		mediaCount = 0;

		PVector newLocation = new PVector((float)0.0, (float)0.0, (float)0.0);
		empty();
				
		/* Find images associated with this cluster ID */
		for (int i = 0; i < p.getImages().size(); i++) 
		{
			WMV_Image curImg = (WMV_Image) p.getImage(i);

			if (curImg.cluster == id) 			// If the image is assigned to this cluster
			{
				newLocation.add(curImg.getCaptureLocation());		// Move cluster towards the image
				if(!images.hasValue(curImg.getID()))
				{
					images.append(curImg.getID());
					mediaCount++;
				}
			}
		}

		/* Find panoramas associated with this cluster ID */
		for (int i = 0; i < p.getPanoramas().size(); i++) 
		{
			WMV_Panorama curPano = (WMV_Panorama) p.getPanorama(i);

			if (curPano.cluster == id) 			// If the image is assigned to this cluster
			{
				newLocation.add(curPano.getCaptureLocation());		// Move cluster towards the image
				if(!panoramas.hasValue(curPano.getID()))
				{
					if(p.debugSettings.panorama)
					{
						PApplet.println("Adding panorama "+curPano.getID()+" to cluster:"+getID());
						PApplet.println("associatedPanoramas.size(): "+panoramas.size());
					}

					panoramas.append(curPano.getID());
					mediaCount++;
				}
			}
		}

		/* Find videos associated with this cluster ID */
		for (int i = 0; i < p.getVideos().size(); i++) 
		{
			WMV_Video curVid = (WMV_Video) p.getVideo(i);

			if (curVid.cluster == id) 				// If the image is assigned to this cluster
			{
				newLocation.add(curVid.getCaptureLocation());	// Move cluster towards the image
				if(!videos.hasValue(curVid.getID()))
				{
					videos.append(curVid.getID());
					mediaCount++;
				}
			}
		}

		/* Divide by number of associated points */
		if (mediaCount > 0) 				
		{
			newLocation.div(mediaCount);
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
		
		mediaCount = 1;
//		clusterMass = mediaPoints * p.p.mediaPointMass;			// Mass = 4 x number of media points

		active = true;
		empty = false;
	}

	void draw(WMV_World world)
	{
		if(p.worldSettings.showUserPanoramas)
		{
			for(WMV_Panorama p : userPanoramas)
			{
				p.update(world.p);
				p.draw(world);
			}
		}

		if(p.worldSettings.showStitchedPanoramas)
		{
			for(WMV_Panorama p : stitchedPanoramas)
			{
				p.update(world.p);
				p.draw(world);
			}
		}
	}

	/**
	 * Stitch images based on current selection: if nothing selected, attempt to stitch all media segments in cluster
	 */
	public void stitchImages(ML_Stitcher stitcher, String libraryFolder)
	{
		if(p.viewerSettings.multiSelection || p.viewerSettings.segmentSelection)
		{
			IntList valid = new IntList();
			for( int i : p.getSelectedImages() )
			{
				if(p.getImage(i).isVisible())
					valid.append(i);
			}

//			if(p.debugSettings.stitching) p.p.p.display.message("Stitching panorama out of "+valid.size()+" selected images from cluster #"+getID());
			
//			WMV_Panorama pano = p.p.p.stitcher.stitch(p.p.p.library.getLibraryFolder(), valid, getID(), -1, p.getSelectedImages());
			WMV_Panorama pano = stitcher.stitch(libraryFolder, valid, getID(), -1, p.getSelectedImages());

			if(pano != null)
			{
				if(p.debugSettings.panorama || p.debugSettings.stitching)
					PApplet.println("Adding panorama at location x:"+getLocation().x+" y:"+getLocation().y);

				pano.initializeSphere();
				
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
//					userPanoramas.set(0, p.utilities.stitcher.combinePanoramas(userPanoramas.get(0), pano));	
//				}
			}
		}
		else
		{
//			if(p.debugSettings.stitching) p.p.p.display.message("Stitching "+segments.size()+" panoramas from media segments of cluster #"+getID());

			for(WMV_MediaSegment m : segments)			// Stitch panorama for each media segment
			{
				if(m.getImages().size() > 1)
				{
					IntList valid = new IntList();
					for( int i : m.getImages() )
					{
						if(p.getImage(i).isVisible())
							valid.append(i);
					}
					
//					if(p.debugSettings.stitching && p.debugSettings.detailed) p.p.p.display.message(" Found "+valid.size()+" media in media segment #"+m.getID());
					
					if(p.p.viewer.getSettings().angleThinning)				// Remove invisible images
					{
						IntList remove = new IntList();
						
						int count = 0;
						for(int v:valid)
						{
							if(!p.getImage(v).getThinningVisibility())
								remove.append(count);
							count++;
						}

						remove.sort();
						for(int i=remove.size()-1; i>=0; i--)
							valid.remove(remove.get(i));	
					}
					
					if(valid.size() > 1)
					{					
//						WMV_Panorama pano = p.p.p.stitcher.stitch(p.p.p.library.getLibraryFolder(), valid, getID(), m.getID(), null);
						WMV_Panorama pano = stitcher.stitch(libraryFolder, valid, getID(), m.getID(), null);
						
						if(pano != null)
						{
							pano.initializeSphere();
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
//								stitchedPanoramas.set(0, p.utilities.stitcher.combinePanoramas(stitchedPanoramas.get(0), pano)); -- To finish
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
//		if(p.debugSettings.cluster || p.debugSettings.model)
//			PApplet.println("analyzeAngles()... cluster images.size():"+images.size());
		float thinningAngle = p.p.viewer.getSettings().thinningAngle;									// Angle to thin images and videos by
		int numPerimeterPts = PApplet.round(PApplet.PI * 2.f / thinningAngle);		// Number of points on perimeter == number of images visible
		int[] perimeterPoints = new int[numPerimeterPts];					// Points to compare each cluster image/video to
		float[] perimeterDistances = new float[numPerimeterPts];			// Distances of images associated with each point
		int videoIdxOffset = p.getImages().size();
		
		for(int i=0; i<numPerimeterPts; i++)
			perimeterPoints[i] = -1;										// Start with empty perimeter point

		for(int idx : images)
		{
			WMV_Image m = p.getImage(idx);
			float imgAngle = PApplet.radians(m.theta);
			m.setThinningVisibility(false);

			for(int i=0; i<numPerimeterPts; i++)
			{
				float ppAngle = i * (PApplet.PI * 2.f / numPerimeterPts);	// Angle of perimeter point i
				if(perimeterPoints[i] == -1)
				{
					perimeterPoints[i] = idx;
					perimeterDistances[i] = p.utilities.getAngularDistance(imgAngle, ppAngle);
				}
				else										
				{
					/* Compare image angular distance from point to current closest */
					float imgDist = p.utilities.getAngularDistance(imgAngle, ppAngle);		
					
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
			WMV_Video v = p.getVideo(idx);
			float vidAngle = PApplet.radians(v.theta);
			v.setThinningVisibility(false);

			for(int i=0; i<numPerimeterPts; i++)
			{
				float ppAngle = i * (PApplet.PI * 2.f / numPerimeterPts);					// Angle of perimeter point i
				if(perimeterPoints[i] == -1)
				{
					perimeterPoints[i] = idx;
					perimeterDistances[i] = p.utilities.getAngularDistance(vidAngle, ppAngle);
				}
				else											
				{
					/* Compare image angular distance from point to current closest */
					float vidDist = p.utilities.getAngularDistance(vidAngle, ppAngle);		
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
					p.getImage(idx).setThinningVisibility(true);
				}
				else
				{
//					PApplet.println("Thinning visibility true for video:"+(idx-videoIdxOffset)+" i:"+i);
					p.getVideo(idx-videoIdxOffset).setThinningVisibility(true);
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
			cImages.add(p.getImage(i));
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
			cPanoramas.add(p.getPanorama(i));
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
			cVideos.add(p.getVideo(i));
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
				WMV_Image i = p.getImage(imgIdx);
				if(i.isActive())
					active = true;
			}
		}

		if(panoramas.size() > 0)
		{
			for(int panoIdx : panoramas)
			{
				WMV_Panorama n = p.getPanorama(panoIdx);
				if(n.isActive())
					active = true;
			}
		}

		if(videos.size() > 0)
		{
			for(int vidIdx : videos)
			{
				WMV_Video v = p.getVideo(vidIdx);
				if(v.isActive())
					active = true;
			}
		}

		return active;
	}
	
	public int getMediaCount()
	{
		int count = images.size() + videos.size() + panoramas.size(); // + sounds.size();
		return count;
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
		if(isAttractor)												// Attractor clusters do not need to be active, but attract while empty
		{
			if(!empty)
			{
				PVector force = getAttractionForce();
//				PApplet.println("Cluster #"+getID()+"... Adding attraction force:"+force);
//				p.p.viewer.attraction.add( force );		// Add attraction force to camera 
				p.p.viewer.attract( force );		// Add attraction force to camera 
			}
			else 
				PApplet.println("Empty Attractor: "+getID());
		}
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
//		PApplet.println("getAttractionForce, Cluster #"+getID()+"... getClusterDistance:"+PVector.dist(location, p.p.viewer.getLocation()));
		if( dist > p.worldSettings.clusterFarDistance )
			mass = clusterMass * farMassFactor * PApplet.sqrt(distance);	// Increase mass with distance to ensure minimum acceleration
		else
			mass = clusterMass;
		
//		PApplet.println("PApplet.sqrt(distance):"+PApplet.sqrt(distance));
		float strength;
		
		if(distance > p.p.viewer.getClusterNearDistance())
		{
			strength = (clusterGravity * mass * p.p.viewer.getSettings().cameraMass) / (distance * distance);	// Calculate strength
		}
		else				// Reduce strength of attraction at close distance
		{
			float diff = p.p.viewer.getClusterNearDistance() - distance;
			float factor = 0.5f - PApplet.map(diff, 0.f, p.p.viewer.getClusterNearDistance(), 0.f, 0.5f);
			strength = (clusterGravity * mass * p.p.viewer.getSettings().cameraMass) / (distance * distance) * factor;
		}
		
		force.mult(strength);
		
		if(p.p.drawForceVector)
			p.p.p.display.map2D.drawForceVector(force);
		
		return force; 								// Return force to be applied
	}

	/**
	 * @return ID of first time segment in cluster
	 */
	public int getFirstTimeSegment(boolean anyDate)
	{
		if(anyDate)
			return timeline.get(0).getFieldTimelineID();
		else 
			return getFirstTimeSegmentForDate(dateline.get(0)).getFieldTimelineID();
	}
	
	/**
	 * @param cluster Cluster to merge with
	 * Merge this cluster with given cluster. Empty and make the given cluster non-active.
	 */
	void absorbCluster(WMV_Cluster cluster)
	{
		if(p.debugSettings.cluster)
			PApplet.println("Merging cluster "+getID()+" with "+cluster.getID());

		/* Find images associated with cluster */
		for (int i = 0; i < p.getImages().size(); i++) 
		{
			WMV_Image curImg = p.getImage(i);

			if (curImg.cluster == cluster.getID()) 				// If the image is assigned to this cluster
			{
				curImg.cluster = id;
				addImage(curImg);
			}
		}

		/* Find panoramas associated with cluster */
		for (int i = 0; i < p.getPanoramas().size(); i++) 
		{
			WMV_Panorama curPano = p.getPanorama(i);

			if (curPano.cluster == cluster.getID()) 				// If the image is assigned to this cluster
			{
				curPano.cluster = id;
				addPanorama(curPano);
			}
		}

		/* Find videos associated with cluster */
		for (int i = 0; i < p.getVideos().size(); i++) 
		{
			WMV_Video curVid = p.getVideo(i);

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
		cluster.mediaCount = 0;
		cluster.active = false;
		cluster.empty = true;
	}
	
	/**
	 * Analyze associated media capture times (Need to implement: find on which scales it operates, i.e. minute, day, month, year)
	 */
	public void analyzeMedia() 
	{
		calculateDimensions();			// Calculate cluster dimensions (bounds)
		calculateTimes();				// Calculate cluster times
		createDateline();				// Create dates histograms and analyze for date segments
		createTimeline();				// Create timeline independent of date 
		createTimelines();				// Create timeline for each capture date
	}
	
	/**
	 * Create list of all media capture dates in cluster, where index corresponds to index of corresponding timeline in timelines array
	 */
	void createDateline()
	{
		dateline = new ArrayList<WMV_Date>();										// List of times to analyze

		/* Get times of all media of all types in this cluster */
		for(int i : images)
		{
			WMV_Date date = p.getImage(i).time.getDate();
			if(!dateline.contains(date)) 				// Add date if doesn't exist
				dateline.add( date );
		}

		for(int n : panoramas)
		{
			WMV_Date date = p.getPanorama(n).time.getDate();
			if(!dateline.contains(date)) 
				dateline.add( date );
		}

		for(int v : videos) 
		{
			WMV_Date date = p.getVideo(v).time.getDate();
			if(!dateline.contains(date)) 
				dateline.add( date );
		}

		dateline.sort(WMV_Date.WMV_DateComparator);				// Sort dateline  
		
		int count = 0;
		for (WMV_Date d : dateline) 		
		{
			d.setID(count);
			count++;
		}

		if(dateline.size() == 0)
		{
			PApplet.println("-------> Cluster "+getID()+" dateline has no points! "+getID()+" images.size():"+images.size()+" dateline.size():"+dateline.size());
			empty();
		}
	}
	
	/**
	 * Create date-independent timeline for cluster
	 */
	void createTimeline()
	{
		ArrayList<WMV_Time> mediaTimes = new ArrayList<WMV_Time>();							// List of times to analyze
		
		/* Get times of all media of all types in this cluster */
		for(int i : images)
			mediaTimes.add( p.getImage(i).time );
		for(int n : panoramas) 
			mediaTimes.add( p.getPanorama(n).time );
		for(int v : videos)
			mediaTimes.add( p.getVideo(v).time );

		if(mediaTimes.size() > 0)
		{
			timeline = p.utilities.calculateTimeSegments(mediaTimes, p.worldSettings.clusterTimePrecision, getID());	// Get relative (cluster) time segments
			if(timeline != null)
				timeline.sort(WMV_TimeSegment.WMV_TimeLowerBoundComparator);				// Sort timeline points 
			
			int count = 0;
			for (WMV_TimeSegment t : timeline) 												// Number time segments in chronological order
			{
//				t.setID(count);
				t.setClusterTimelineID(count);
				count++;
			}
		}

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

		if(timeline.size() == 0)
		{
			PApplet.println("Cluster timeline has no points! "+getID()+" images.size():"+images.size()+" panoramas.size():"+panoramas.size());
			empty();
		}
	}
	
	/**
	 * Create timeline for each date on dateline, where index of a date in dateline matches index of corresponding timeline in timelines array
	 */
	void createTimelines()
	{
		int ct = 0;
		timelines = new ArrayList<ArrayList<WMV_TimeSegment>>();
		for(WMV_Date d : dateline)			// For each date on dateline
		{
			/* Old Method */
//			ArrayList<WMV_Time> mediaTimes = new ArrayList<WMV_Time>();							// List of times to analyze

//			for(int i : images)
//			{
//				WMV_Image img = p.getImage(i);
//				if((img.getDate()).equals(d))
//					mediaTimes.add( img.time );
//			}
//			
//			for(int n : panoramas) 
//			{
//				WMV_Panorama pano = p.getPanorama(n);
//				if(pano.getDate().equals(d))
//					mediaTimes.add( pano.time );
//			}
//			for(int v : videos)
//			{
//				WMV_Video vid = p.getVideo(v);
//				if(vid.getDate().equals(d))
//					mediaTimes.add( vid.time );
//			}
//			for(int s : sounds)
//			{
//				WMV_Sound snd = p.getSound(s);
//				if(snd.getDate().equals(d))
//					mediaTimes.add( snd.time );
//			}

//			if(mediaTimes.size() > 0)
//			{
//				ArrayList<WMV_TimeSegment> newTimeline = p.utilities.calculateTimeSegments(mediaTimes, p.worldSettings.clusterTimePrecision, getID());
//
//				if(newTimeline != null) 
//					newTimeline.sort(WMV_TimeSegment.WMV_TimeLowerBoundComparator);		// Sort timeline  
//				
//				int count = 0;
//				for (WMV_TimeSegment t : newTimeline) 		
//				{
//					t.setID(count);
//					count++;
//				}
//				
//				timelines.add( newTimeline );		// Calculate and add timeline to list
//			}
			
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
					t.setClusterDateID(ct);
					t.setClusterTimelinesID(count);
					count++;
				}
				timelines.add( newTimeline );		// Calculate and add timeline to list
//				PApplet.println("Added timeline #"+count+" for cluster #"+getID()+" with "+newTimeline.size()+" points...");
			}
//			else
//				PApplet.println("No timeline #"+count+" for cluster #"+getID());
			
			ct++;
		}
	}
	
	/** 
	 * Update cluster time loop
	 */
	void updateTime()
	{
		if(timeFading && !dateFading && p.frameCount % timeUnitLength == 0)
		{
			currentTime++;															// Increment cluster time
			if(currentTime > timeCycleLength)
				currentTime = 0;
		}
		
		if(timeFading && dateFading && p.frameCount % timeUnitLength == 0)
		{
			currentTime++;															// Increment cluster time
			if(currentTime > timeCycleLength)
			{
				currentTime = 0;
				currentDate++;															// Increment cluster date

				if(p.debugSettings.cluster || p.debugSettings.time)
					PApplet.println("Reached end of day at p.frameCount:"+p.frameCount);

				if(dateline != null)
				{
					if(currentDate > dateline.size())
					{
						currentDate = 0;
						if(p.debugSettings.cluster || p.debugSettings.time)
							PApplet.println("Reached end of dateline at p.frameCount:"+p.frameCount);
					}
				}
				else if(p.debugSettings.cluster || p.debugSettings.time)
				{
					PApplet.println("dateline is NULL for cluster "+getID());
				}
			}
		}
	}
	
	public WMV_TimeSegment getFirstTimeSegmentForDate(WMV_Date date)
	{
		boolean found = false;
		int timelineID = 0;
		
		if(dateline != null)
		{
			for(WMV_Date d : dateline)		// Look through cluster dates for date
			{
				if(!found)						
				{	
					if(d == date)										// If cluster has date,
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
				WMV_TimeSegment result;
				if(dateline.size()>1)
				{
//					return timelines.get(timelineID).get(0);
					try{
						result = timelines.get(timelineID).get(0);
					}
					catch(NullPointerException e)
					{
						PApplet.println("No timeline found! ... dateline.size():"+dateline.size()+" timelineID:"+timelineID+" ... cluster id:"+getID());
						result = null;
					}
				}
				else
				{
//					return timeline.get(0);
					try{
						result = timeline.get(0);
					}
					catch(NullPointerException e)
					{
						PApplet.println("No timeline found!... cluster id:"+getID());
						result = null;
					}
				}
				
				return result;
			}
			else
			{
//				if(p.debugSettings.cluster || p.debugSettings.time)
//					PApplet.println("Date doesn't exist in cluster #"+getID()+"... "+date);
				return null;
			}
		}
		else 
		{
			if(p.debugSettings.cluster || p.debugSettings.time)
			{
				PApplet.println("Cluster #"+getID()+" has no dateline but has "+mediaCount+" media points!!");
			}
			return null;
		}
	}
	
	public WMV_TimeSegment getLastTimeSegmentForDate(WMV_Date date)
	{
		boolean found = false;
		int timelineID = 0;
//		float date = p.getCurrentField().dateline.get(dateSegment).getCenter();
		
		if(dateline != null)
		{
			for(int index=dateline.size()-1; index >= 0; index--)					// Look through cluster dates for date
			{
				WMV_Date d = dateline.get(index);
				if(!found)						
				{	
					if(d == date)										// If cluster has date,
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
				PApplet.println("Date doesn't exist in cluster #"+getID()+"... "+date.getDate());
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

//		if(p.debugSettings.cluster && (images.size() != 0 || panoramas.size() != 0 || videos.size() != 0))
//			PApplet.println("Analyzing media times in cluster "+id+" ..." + " associatedImages.size():"+images.size()+" associatedPanoramas:"+panoramas.size()+" associatedVideos:"+videos.size());
			
		ArrayList<WMV_Image> cImages = new ArrayList<WMV_Image>();
		ArrayList<WMV_Panorama> cPanoramas = new ArrayList<WMV_Panorama>();
		ArrayList<WMV_Video> cVideos = new ArrayList<WMV_Video>();
		
		for(int i : images)
			cImages.add(p.getImage(i));
		for(int i : panoramas)
			cPanoramas.add(p.getPanorama(i));
		for(int i : videos)
			cVideos.add(p.getVideo(i));

		for (WMV_Image i : cImages) 			// Iterate over cluster images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		{
//			float fDayLength = i.time.getDayLength();

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

//			if (fDayLength > longestDayLength)		// Calculate longest day length
//				longestDayLength = fDayLength;
		}
		
		
		for (WMV_Panorama n : cPanoramas) 			// Iterate over cluster panoramas to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		{
//			float fDayLength = n.time.getDayLength();

			if (initPanoramaTime) 		// Calculate most recent and oldest panorama time
			{		
				highPanoramaTime = n.time.getTime();
				lowPanoramaTime = n.time.getTime();
				initPanoramaTime = false;
			}

			if (initPanoramaDate) 		// Calculate most recent and oldest panorama date
			{		
				highPanoramaDate = n.time.getDate().getDaysSince1980();
				lowPanoramaDate = n.time.getDate().getDaysSince1980();
				initPanoramaDate = false;
			}

			if (n.time.getTime() > highPanoramaTime)
				highPanoramaTime = n.time.getTime();
			if (n.time.getTime() < lowPanoramaTime)
				lowPanoramaTime = n.time.getTime();

			if (n.time.getDate().getDaysSince1980() > highPanoramaDate)
				highPanoramaDate = n.time.getDate().getDaysSince1980();
			if (n.time.getDate().getDaysSince1980() < lowPanoramaDate)
				lowPanoramaDate = n.time.getDate().getDaysSince1980();

//			if (fDayLength > longestPanoramaDayLength)		// Calculate longest panorama day length
//				longestPanoramaDayLength = fDayLength;
		}
		
		for (WMV_Video v : cVideos) 			// Iterate over cluster videos to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		{
//			float fDayLength = v.time.getDayLength();

			if (initVideoTime) 		// Calculate most recent and oldest video time
			{		
				highVideoTime = v.time.getTime();
				lowVideoTime = v.time.getTime();
				initVideoTime = false;
			}

			if (initVideoDate) 		// Calculate most recent and oldest video date
			{		
				highVideoDate = v.time.getDate().getDaysSince1980();
				lowVideoDate = v.time.getDate().getDaysSince1980();
				initImageDate = false;
			}

			if (v.time.getTime() > highVideoTime)
				highVideoTime = v.time.getTime();
			if (v.time.getTime() < lowVideoTime)
				lowVideoTime = v.time.getTime();

			if (v.time.getDate().getDaysSince1980() > highVideoDate)
				highVideoDate = v.time.getDate().getDaysSince1980();
			if (v.time.getDate().getDaysSince1980() < lowVideoDate)
				lowVideoDate = v.time.getDate().getDaysSince1980();

//			if (fDayLength > longestVideoDayLength)		// Calculate longest video day length
//				longestVideoDayLength = fDayLength;
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
		System.out.println(" ");
	}
	
	/**
	 * Calculate high and low longitude, latitude and altitude for cluster
	 */
	void calculateDimensions()
	{
		boolean init = true;	

		for (int img : images) 				// Iterate over associated images to calculate X,Y,Z and T (longitude, latitude, altitude and time)
		{
			WMV_Image i = p.getImage(img);
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
			WMV_Panorama n = p.getPanorama(pano);
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
			WMV_Video v = p.getVideo(vid);
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
		WMV_Waypoint result = new WMV_Waypoint(getID(), getLocation(), null);		// -- Calculate time instead of null!!
		return result;
	}

	public IntList getImageIDs()
	{
		return images;
	}

	public IntList getPanoramaIDs()
	{
		return panoramas;
	}

	public IntList getVideoIDs()
	{
		return videos;
	}
	
//	public IntList getSounds()
//	{
//		return sounds;
//	}
	
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
//		if(p.debugSettings.viewer)
//		if(isAttractor())
//			p.p.p.display.message("Set cluster isAttractor to true:"+getID()+" attraction force mag:"+getAttractionForce().mag());
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
	
	public void setID(int newID)
	{
		id = newID;
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
	
	public boolean isSelected()
	{
		return selected;
	}
	
	public void setSelected(boolean state)
	{
		selected = state;
	}
}