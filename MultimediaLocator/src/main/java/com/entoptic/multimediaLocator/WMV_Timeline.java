package main.java.com.entoptic.multimediaLocator;

import java.util.ArrayList;

public class WMV_Timeline 
{
	public ArrayList<WMV_TimeSegment> timeline;				// Date-independent capture times for this cluster
	WMV_TimeSegment lower, upper;

	public WMV_Timeline(){}
	
	public void initialize(ArrayList<WMV_TimeSegment> newTimeline)
	{
		if(newTimeline == null)
		{
			timeline = new ArrayList<WMV_TimeSegment>();
		}
		else
		{
			timeline = newTimeline;
			finish();
//			calculateBounds();
//			verify();
		}
	}
	
	public void finish()
	{
		calculateBounds();
		verify();
	}

	/**
	 * Calculate the upper and lower bounding segments of timeline
	 */
	private void calculateBounds()
	{
		if(timeline.size() > 0)
		{
			lower = timeline.get(0);									// Already sorted by lower bound
			upper = timeline.get(timeline.size()-1);

			for(WMV_TimeSegment ts : timeline)							// Find upper bound
				for(WMV_Time t : ts.timeline)
					if(t.getTime() > upper.getUpper().getTime())
						upper = ts;
		}
	}
	
	public WMV_TimeSegment getLower()
	{
		return lower;
	}

	public WMV_TimeSegment getUpper()
	{
		return upper;
	}
	
//	public ArrayList<WMV_TimeSegment> timeline;				// Date-independent capture times for this cluster
//	WMV_TimeSegment lower, upper;

	public void verify()
	{
		for(WMV_TimeSegment t : timeline)
		{
			if(t.getClusterID() == -1)
			{
				System.out.println("Timeline.verify()... ERROR: field timeline ID #"+t.getFieldTimelineID()+" cluster ID is -1!");
			}
		}
		
		if(lower.getClusterID() == -1)
			System.out.println("Timeline.verify()... ERROR: lower cluster ID is -1!");
		if(upper.getClusterID() == -1)
			System.out.println("Timeline.verify()... ERROR: upper cluster ID is -1!");
	}
}
