package gmViewer;

import processing.data.IntList;

/***************
 * GMV_MediaSegment
 * @author davidgordon
 * 
 * Portion of a cluster containing overlapping media without any gaps
 */
public class GMV_MediaSegment 
{
	private int id;
	IntList images;					// Images in segment
//	IntList videos;					// Videos in segment
	private float left, right, centerDirection;		// Upper and lower bounds for direction (in degrees)
	private float bottom, top, centerElevation;	// Upper and lower bounds (in degrees)

	GMV_Cluster p;
	
	GMV_MediaSegment( GMV_Cluster parent, int newID, IntList newImages, IntList newVideos, float newLower, float newUpper, 
					  float newCenter, float newLowerElevation, float newUpperElevation, float newCenterElevation)
	{
		p = parent;
		id = newID;
		
		images = newImages;
//		videos = newVideos;
		
		left = newLower;
		right = newUpper;
		centerDirection = newCenter;
		
		bottom = newLowerElevation;
		top = newUpperElevation;
		centerElevation = newCenterElevation;

//		calculateDimensions();
	}
	
//	private void calculateDimensions()	// --OBSOLETE
//	{
//		float lowestDirection = 360.f, highestDirection = 0.f;
//		int lowestDirectionIdx = -1, highestDirectionIdx = -1;
//		float lowestElevation = 90.f, highestElevation = -90.f;
//		int lowestElevationIdx = -1, highestElevationIdx = -1;
//		
//		for(int i:images)
//		{
//			float current = p.p.images.get(i).getDirection();
//			if(current < lowestDirection)
//			{
//				lowestDirection = current;
//				lowestDirectionIdx = i;
//			}
//			if(current > highestDirection)
//			{
//				highestDirection = current;
//				highestDirectionIdx = i;
//			}
//		}
//		
//		for(int i:images)
//		{
//			float current = p.p.images.get(i).getElevation();
//			if(current < lowestElevation)
//			{
//				lowestElevation = current;
//				lowestElevationIdx = i;
//			}
//			if(current > highestElevation)
//			{
//				highestElevation = current;
//				highestElevationIdx = i;
//			}
//		}
//		
//		lower = lowestDirection;
//		upper = highestDirection;
//		lowerElevation = lowestElevation;
//		upperElevation = highestElevation;
//	}
	
	public int getID()
	{
		return id;
	}

	public IntList getImages()
	{
		return images;
	}

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

//	public IntList getVideos()
//	{
//		return videos;
//	}
}

