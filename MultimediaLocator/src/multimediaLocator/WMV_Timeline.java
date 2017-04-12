package multimediaLocator;

import java.util.ArrayList;

public class WMV_Timeline 
{
	public ArrayList<WMV_TimeSegment> timeline;				// Date-independent capture times for this cluster
	WMV_TimeSegment lower, upper;
//	private int upperIdx = -1;
//	public int upperClusterTimelineIdx = -1;				// Cluster timeline index of upper bound
//	public int upperFieldTimelineIdx = -1;					// Field timeline index of upper bound

	WMV_Timeline(ArrayList<WMV_TimeSegment> newTimeline)
	{
		if(newTimeline == null)
		{
			timeline = new ArrayList<WMV_TimeSegment>();
		}
		else
		{
			timeline = newTimeline;
			calculateBounds();
		}
	}
	
	public void finishTimeline()
	{
		calculateBounds();
	}

	/**
	 * Calculate the upper and lower bounding segments of timeline
	 */
	private void calculateBounds()
	{
//		System.out.println("----calculateBounds()--");
		if(timeline.size() > 0)
		{
			lower = timeline.get(0);									// Already sorted by lower bound
			upper = timeline.get(timeline.size()-1);

			for(WMV_TimeSegment ts : timeline)							// Find upper bound
			{
				for(WMV_Time t : ts.timeline)
				{
					if(t.getTime() > upper.getUpper().getTime())
					{
						upper = ts;
						System.out.println("----->>> Fixed upper bound for timeline <<<-----");
					}
				}
			}
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
}
