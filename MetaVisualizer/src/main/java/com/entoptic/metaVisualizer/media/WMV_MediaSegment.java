package main.java.com.entoptic.metaVisualizer.media;

import java.util.ArrayList;
import java.util.List;

/***************
 * Segment of overlapping images or videos captured in same cluster
 * @author davidgordon
 * 
 */
public class WMV_MediaSegment
{
	private int id;
	List<Integer> images;					// Images in segment
	List<Integer> videos;					// Videos in segment

	private float left, right;				// Left and right bounding angles (in degrees)
	private float centerDirection;			// Horizontal center angle (in degrees)
	private float bottom, top;				// Upper and lower bounding angles (in degrees)
	private float centerElevation;			// Vertical center angle (in degrees)
	
	private boolean hidden;					// Whether media segment is hidden
	private float stitchingMinAngle;		// Minimum angle between images to stitch together
	
//	WMV_MediaSegment( int newID, List<Integer> newImages, List<Integer> newVideos, float newLower, float newUpper, float newCenter,
//			  float newLowerElevation, float newUpperElevation, float newCenterElevation, float newStitchingMinAngle )
	public WMV_MediaSegment( int newID, List<Integer> newImages, float newLower, float newUpper, float newCenter,
			  float newLowerElevation, float newUpperElevation, float newCenterElevation, float newStitchingMinAngle )
	{
		id = newID;
		
		images = newImages;
//		videos = newVideos;
		
		left = newLower;
		right = newUpper;
		centerDirection = newCenter;
		
		bottom = newLowerElevation;
		top = newUpperElevation;
		centerElevation = newCenterElevation;
		
		stitchingMinAngle = newStitchingMinAngle;
	}
	
	/**
	 * Find horizontal and vertical edges at which each image borders segment edge,
	 * in form of (horizBorderID, vertBorderID), where
	 * 
	 * horizBordersID key:	0: Left  1: Center  2: Right  3: Both (Left+Right) 
	 * vertBordersID	key:    0: Top   1: Center  2: Bottom 3: Both (Top+Bottom)
	 * 
	 * Example: In segment below, Image A == (Center, Top), Image B == (Bottom, Right),
	 * Image C == (Both, Both), only image in segment
	 * Image D == (Both, Top)
	 * 	_______________		_____		_____
	 * 	|	|_A_|	  |		|_C_|		|_D_|
	 * 	|		  ____|					|   |
	 * 	|_________|_B_|					|___|
	 * 
	 * Both horizBorderID and vertBorderID are used in determining image mask, 
	 * in following format: blurMask[horizBorderID][vertBorderID].jpg
	 * 
	 * @param imageList All images in field
	 */
	
//	public void findImageBorders(ArrayList<WMV_Image> imageList, ArrayList<WMV_Video> videoList)
	public void findImageBorders(ArrayList<WMV_Image> imageList)
	{
		if(images != null)
		{
			for(int i:images)		// Determine mask for image: blurMask[horizBorderID][vertBorderID].jpg
			{
				int horizBordersID = 1;					
				int vertBordersID = 1;					

				WMV_Image img = imageList.get(i);
				float xDir = img.getDirection();
				float yDir = img.getElevationAngle();

				if(xDir - left < stitchingMinAngle)
					horizBordersID = 0;				// Left

				if(right - xDir < stitchingMinAngle)
				{
					if(horizBordersID == 0)
						horizBordersID = 3;			// Both (Left+Right)
					else
						horizBordersID = 2;			// Right
				}

				if(yDir - top < stitchingMinAngle)
					vertBordersID = 0;				// Top

				if(bottom - yDir < stitchingMinAngle)
				{
					if(vertBordersID == 0)
						vertBordersID = 3;			// Both (Top+Bottom)
					else
						vertBordersID = 2;			// Bottom
				}

				img.setHorizBordersID(horizBordersID);
				img.setVertBordersID(vertBordersID);
				img.setBlurMaskID();

//				if(p.p.p.p.debug.image)
//					PApplet.println("Found image "+img.getID()+" borders horiz:"+horizBorderID+" vert:"+vertBorderID);
			}
		}
		
//		if(videos != null)
//		{
//			for(int v:videos)		// Determine mask for image: blurMask[horizBorderID][vertBorderID].jpg
//			{
//				int horizBordersID = 1;					
//				int vertBordersID = 1;					
//
//				WMV_Video vid = videoList.get(v);
//				float xDir = vid.getDirection();
//				float yDir = vid.getVerticalAngle();
//
//				if(xDir - left < stitchingMinAngle)
//					horizBordersID = 0;				// Left
//
//				if(right - xDir < stitchingMinAngle)
//				{
//					if(horizBordersID == 0)
//						horizBordersID = 3;			// Both (Left+Right)
//					else
//						horizBordersID = 2;			// Right
//				}
//
//				if(yDir - top < stitchingMinAngle)
//					vertBordersID = 0;				// Top
//
//				if(bottom - yDir < stitchingMinAngle)
//				{
//					if(vertBordersID == 0)
//						vertBordersID = 3;			// Both (Top+Bottom)
//					else
//						vertBordersID = 2;			// Bottom
//				}
//
//				vid.setHorizBorderID(horizBordersID);
//				vid.setVertBorderID(vertBordersID);
//				vid.setBlurMaskID();
//
////				if(p.p.p.p.debug.image)
////					PApplet.println("Found image "+img.getID()+" borders horiz:"+horizBorderID+" vert:"+vertBorderID);
//			}
//		}
	}

	public boolean isHidden()
	{
		return hidden;
	}
	
	public void hide(ArrayList<WMV_Image> imageList)
	{
		for(WMV_Image image : imageList)				// Set images in segment to hidden
			if(images.contains(image.getID()))
				image.setHidden(true);

		hidden = true;
	}
	
	public void show(ArrayList<WMV_Image> imageList)
	{
		for(WMV_Image image : imageList)				// Set images in segment to hidden
			if(images.contains(image.getID()))
				image.setHidden(false);

		hidden = false;
	}
	
	public int getID()
	{
		return id;
	}

	public List<Integer> getImages()
	{
		return images;
	}

//	public List<Integer> getVideos()
//	{
//		return videos;
//	}

	public float getRight()
	{
		return right;
	}

	public float getLeft()
	{
		return left;
	}

	public float getBottom()
	{
		return bottom;
	}

	public float getTop()
	{
		return top;
	}
	
	public float getCenterDirection()
	{
		return centerDirection;
	}
	
	public float getCenterElevation()
	{
		return centerElevation;
	}
}

