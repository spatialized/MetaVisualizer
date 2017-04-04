package multimediaLocator;

import java.util.ArrayList;
import java.util.List;

/***************
 * @author davidgordon
 * Segment of images or videos overlapping without any gaps
 */
public class WMV_MediaSegment 
{
	private int id;
	List<Integer> images;					// Images in segment
//	List<Integer> videos;					// Images in segment
	
	private float left, right, centerDirection;		// Upper and lower bounds for direction (in degrees)
	private float bottom, top, centerElevation;		// Upper and lower bounds (in degrees)
	private boolean hidden;

	private float stitchingMinAngle;
	
	WMV_MediaSegment( int newID, List<Integer> newImages, List<Integer> newVideos, float newLower, float newUpper, float newCenter,
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
	 * Associate each media item with a center or edge (left, right, bottom or top) position
	 */
	public void findBorders(ArrayList<WMV_Image> imageList)
	{
		if(images != null)
		{
			for(int i:images)
			{
				int horizBorderID = 1;					// horizBorderID    0: Left  1: Center  2: Right  3: Left+Right
				int vertBorderID = 1;					// vertBorderID		0: Top  1: Center  2: Bottom  3: Top+Bottom

				WMV_Image img = imageList.get(i);
				float xDir = img.getDirection();
				float yDir = img.getElevation();

				if(xDir - left < stitchingMinAngle)
					horizBorderID = 0;				// Left

				if(right - xDir < stitchingMinAngle)
				{
					if(horizBorderID == 0)
						horizBorderID = 3;			// Left+Right
					else
						horizBorderID = 2;			// Right
				}

				if(yDir - top < stitchingMinAngle)
					vertBorderID = 0;				// Top

				if(bottom - yDir < stitchingMinAngle)
				{
					if(vertBorderID == 0)
						vertBorderID = 3;			// Top+Bottom
					else
						vertBorderID = 2;			// Bottom
				}

				img.setHorizBorderID(horizBorderID);
				img.setVertBorderID(vertBorderID);
				img.setBlurMaskID();
//				img.setBlurMask();

//				if(p.p.p.p.debug.image)
//					PApplet.println("Found image "+img.getID()+" borders horiz:"+horizBorderID+" vert:"+vertBorderID);
			}
		}
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

