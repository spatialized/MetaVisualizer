package multimediaLocator;
import java.util.ArrayList;

import g4p_controls.GButton;
import processing.core.*;

/***********************************
 * @author davidgordon
 * Class for displaying 2D text and graphics
 */

class ML_Display
{
	/* Classes */
	public ML_Window window;							// Main interaction window
	public WMV_Map map2D;
	
	/* Window Modes */
	public boolean fullscreen = true;
	public boolean initializedMaps = false;
	
	/* Display Modes */
	public int displayView = 0;							// 0: Scene  1: Map  2: Cluster  3: Timeline
	public boolean satelliteMap = true;
	
	/* Debug */
	public boolean drawForceVector = false;
	
	/* Setup */
	public boolean initialSetup = true;
	GButton btnSelectLibrary;
//	PImage startupImage;
	
	/* Graphics */
	float hudDistance;									// Distance of the Heads-Up Display from the virtual camera -- Change with zoom level??
	public boolean drawGrid = false; 					// Draw 3D grid   			-- Unused

	public int blendMode = 0;							// Alpha blending mode
	public int numBlendModes = 10;						// Number of blending modes

	/* Clusters */
	public int displayCluster = 0;

	/* Messages */
	ArrayList<String> messages;							// Messages to display on screen
	ArrayList<String> metadata;							// Metadata messages to display on screen
	ArrayList<String> startupMessages;					// Messages to display on screen

	int messageStartFrame = -1;
	int metadataStartFrame = -1;
	int startupMessageStartFrame = -1;
	int messageDuration = 60;
	
	/* Timeline */
	float timelineScreenSize, timelineHeight = 100.f;
	float timelineStart = 0.f, timelineEnd = 0.f;
	float datelineStart = 0.f, datelineEnd = 0.f;
	int displayDate = -1;
	public boolean updateCurrentSelectableTime = true, updateCurrentSelectableDate = true;
	private final float timeTextSize = 44.f;
	
	private ArrayList<SelectableTimeSegment> selectableTimes;		// Selectable time segments on timeline
	private ArrayList<SelectableDate> selectableDates;		// Selectable dates on dateline
	private float minSegmentSeconds = 15.f;
	
	private boolean timelineCreated = false, datelineCreated = false, updateTimeline = true;
	private float timelineXOffset = 0.f, timelineYOffset = 0.f;
	private float datelineXOffset = 0.f, datelineYOffset = 0.f;
	private SelectableTimeSegment currentSelectableTime;
	private int selectedTime = -1, selectedCluster = -1, currentSelectableTimeID = -1, currentSelectableTimeFTSID = -1;
	private int selectedDate = -1, currentSelectableDate = -1;
		
	private boolean timelineTransition = false, timelineZooming = false, timelineScrolling = false;   
	private int transitionScrollDirection = -1, transitionZoomDirection = -1;
	private int timelineTransitionStartFrame = 0, timelineTransitionEndFrame = 0;
	private int timelineTransitionLength = 30;
	private final int initTimelineTransitionLength = 30;
	
	private float timelineStartTransitionStart = 0, timelineStartTransitionTarget = 0;
	private float timelineEndTransitionStart = 0, timelineEndTransitionTarget = 0;
	public float transitionScrollIncrement = 1750.f;
	public final float initTransitionScrollIncrement = 1750.f;	// Seconds to scroll per frame
	public float transitionZoomInIncrement = 0.95f, transitionZoomOutIncrement = 1.052f;	

	private float imageHue = 140.f;
	private float panoramaHue = 190.f;
	private float videoHue = 100.f;
	private float soundHue = 40.f;

	/* Text */
	float centerTextXOffset, topTextYOffset;
	float userMessageXOffset, userMessageYOffset, startupMessageXOffset;
	float leftTextXOffset, rightTextXOffset, metadataYOffset, startupMessageYOffset;
	float midLeftTextXOffset, midRightTextXOffset;
	float clusterImageXOffset, clusterImageYOffset;

	final float veryLargeTextSize = 64.f;
	final float largeTextSize = 56.f;
	final float mediumTextSize = 44.f;
	final float smallTextSize = 36.f;
	final float linePadding = 20.f;
	final float lineWidth = smallTextSize + linePadding;			
	final float lineWidthWide = largeTextSize + linePadding;			
	final float lineWidthVeryWide = largeTextSize * 2.f;			

	WMV_World p;

	ML_Display(WMV_World parent)
	{
		p = parent;
		
		hudDistance = p.hudDistance;
		
		messages = new ArrayList<String>();
		metadata = new ArrayList<String>();
		startupMessages = new ArrayList<String>();

		centerTextXOffset = p.p.width / 2.f;
		leftTextXOffset = 0.f;
		midLeftTextXOffset = p.p.width / 3.f;
		rightTextXOffset = 0.f;
		midRightTextXOffset = p.p.width / 1.5f;

		topTextYOffset = -p.p.height / 1.6f;
		clusterImageXOffset = -p.p.width/ 1.75f;
		clusterImageYOffset = p.p.height * 1.33f;

		userMessageXOffset = -p.p.width / 2.f;
		userMessageYOffset = 0;

		metadataYOffset = -p.p.height / 2.f;

		startupMessageXOffset = p.p.width / 2.f;
		startupMessageYOffset = -p.p.height /2.f;

		timelineScreenSize = p.p.width * 2.2f;
		timelineStart = 0.f;
		timelineEnd = p.p.utilities.getTimePVectorSeconds(new PVector(24,0,0));

		timelineXOffset = -p.p.width/ 1.66f;
		timelineYOffset = -p.p.height / 3.f;
		timelineYOffset = 0.f;
		datelineXOffset = timelineXOffset;
		datelineYOffset = p.p.height * 0.266f;
		
		map2D = new WMV_Map(this);
		currentSelectableTime = null;
		currentSelectableTimeID = -1;
		currentSelectableTimeFTSID = -1;
//		startupImage = p.p.loadImage("res/WMV_Title.jpg");
	}

	void setupWMVWindow()
	{
		window = new ML_Window(this);				// Setup and display interaction window
	}

	/**
	 * Draw Heads-Up Display elements: messages, interactive map, field statistics, metadata.
	 */
	void draw()
	{
		if(initialSetup)
		{
			p.p.hint(PApplet.DISABLE_DEPTH_TEST);												// Disable depth testing for drawing HUD
			p.p.background(0);																// Hide 3D view
			displayStartup();														// Draw startup messages
//			progressBar();
		}
		else
		{
			if( displayView != 0 || p.interactive )
			{
				p.p.hint(PApplet.DISABLE_DEPTH_TEST);												// Disable depth testing for drawing HUD
				p.p.background(0.f);																// Hide 3D view

				switch(displayView)
				{
				case 1:
					map2D.drawMainMap(!satelliteMap);
					if(map2D.scrollTransition) map2D.updateMapScrollTransition();
					if(map2D.zoomToRectangleTransition) map2D.updateZoomToRectangleTransition();
					if(p.interactive) displayInteractiveClustering();
					map2D.updateMapMouse();
					break;
				case 2:
					displayCluster();
					break;
				case 3:
					displayTimeView();
					updateFieldTimeline();
					break;
				}

			}
			else if( messages.size() > 0 || metadata.size() > 0 )
			{
				p.p.hint(PApplet.DISABLE_DEPTH_TEST);												// Disable depth testing for drawing HUD

				if(messages.size() > 0)
					displayMessages();

				if(p.showMetadata && metadata.size() > 0 && p.viewer.settings.selection)	
					displayMetadata();

//				if((displayMode == 1) && drawForceVector)						// Draw force vector
//					map2D.drawForceVector();
			}
		}
	}

	/**
	 * Display Time View in main window
	 */
	public void displayTimeView()
	{
		startHUD();
		p.p.pushMatrix();
		
		float xPos = centerTextXOffset;
		float yPos = topTextYOffset;			// Starting vertical position
		
		WMV_Field f = p.getCurrentField();
		WMV_Cluster c = p.getCurrentCluster();

		p.p.fill(0, 0, 255, 255);

		p.p.textSize(veryLargeTextSize);
		p.p.text(""+p.getCurrentField().name, xPos, yPos, hudDistance);

		p.p.textSize(largeTextSize);
		String strDisplayDate = "Showing All Dates";
		if(displayDate != -1) strDisplayDate = p.p.utilities.getDateAsString(p.getCurrentField().dateline.get(displayDate));
		p.p.text(strDisplayDate, xPos, yPos += lineWidthVeryWide * 1.5f, hudDistance);
		
		p.p.textSize(mediumTextSize);
		p.p.text(" Time Zone: "+ f.timeZoneID, xPos, yPos += lineWidthVeryWide, hudDistance);

		yPos = timelineYOffset + timelineHeight * 4.f;

		if(p.p.debug.field || p.p.debug.main)
		{
			if(f.dateline != null)
			{
				p.p.textSize(largeTextSize);
				p.p.text(" Current Field #"+ f.fieldID+" of "+ p.getFields().size(), xPos, yPos += lineWidthVeryWide, hudDistance);
				p.p.textSize(mediumTextSize);
				if(f.dateline.size() > 0)
				{
//					p.p.text(" Clusters: "+ f.clusters.size()+"  Media: "+ f.getMediaCount(), xPos, yPos += lineWidth, hudDistance);
					int fieldDate = p.getCurrentField().timeline.get(p.viewer.getCurrentFieldTimeSegment()).getFieldDateID();
					p.p.text(" Current Time Segment", xPos, yPos += lineWidthWide, hudDistance);
					p.p.text("   ID: "+ p.viewer.getCurrentFieldTimeSegment()+" of "+ p.getCurrentField().timeline.size() +" in Main Timeline", xPos, yPos += lineWidthWide, hudDistance);
					p.p.text("   Date: "+ (fieldDate)+" of "+ p.getCurrentField().dateline.size(), xPos, yPos += lineWidth, hudDistance);
					p.p.text("   Date-Specific ID: "+ p.getCurrentField().timeline.get(p.viewer.getCurrentFieldTimeSegment()).getFieldTimelineIDOnDate()
							+" of "+ p.getCurrentField().timelines.get(fieldDate).size() + " in Timeline #"+(fieldDate), xPos, yPos += lineWidth, hudDistance);
				}
			}
			if(c != null)
			{
				p.p.textSize(largeTextSize);
				p.p.text(" Current Cluster #"+ c.getID()+" of "+ f.clusters.size(), xPos, yPos += lineWidthWide, hudDistance);
				p.p.textSize(mediumTextSize);
				if(c.dateline != null)
				{
					if(c.dateline.size() > 0)
					{
						int clusterDate = p.getCurrentField().timeline.get(p.viewer.getCurrentFieldTimeSegment()).getClusterDateID();
						p.p.text(" Current Cluster Time Segment", xPos, yPos += lineWidthWide, hudDistance);
						p.p.text("   ID: "+ p.getCurrentField().timeline.get(p.viewer.getCurrentFieldTimeSegment()).getClusterTimelineID()+"  of "+ c.timeline.size() +" in Cluster Main Timeline", xPos, yPos += lineWidthWide, hudDistance);
						p.p.text("   Date: "+ (clusterDate+1) +" of "+ c.dateline.size(), xPos, yPos += lineWidth, hudDistance);
						if(c.timelines.size() > clusterDate)
							p.p.text("  Date-Specific ID: "+ p.getCurrentField().timeline.get(p.viewer.getCurrentFieldTimeSegment()).getClusterTimelineIDOnDate()+"  of "+ c.timelines.get(clusterDate).size() + " in Cluster Timeline #"+clusterDate, xPos, yPos += lineWidth, hudDistance);
						else
							p.p.text("ERROR: No Cluster Timeline for Current Cluster Date ("+clusterDate+")", xPos, yPos += lineWidth, hudDistance);
					}
				}
			}
		}
		p.p.popMatrix();
		
		if(datelineCreated) drawFieldDateline();
		if(timelineCreated) displayFieldTimeline();
		updateTimelineMouse();
	}
	
	/**
	 * Update field timeline every frame
	 */
	private void updateFieldTimeline()
	{
		if(!datelineCreated || !timelineCreated || updateTimeline)
		{
			if(!timelineTransition)
			{
				createDateline();
				createTimeline();
				updateTimeline = false;
			}
		}

		if(updateCurrentSelectableTime && !timelineTransition)
		{
			if(p.viewer.getCurrentFieldTimeSegment() >= 0)
			{
				WMV_TimeSegment t = p.getCurrentField().timeline.get(p.viewer.getCurrentFieldTimeSegment());		// TESTING
				int previous = currentSelectableTimeID;
				
				if(t != null)
				{
					currentSelectableTimeID = getSelectableTimeIDOfTimeSegment(t);						// Set current selectable time (white rectangle) from current field time segment
					if(currentSelectableTimeID != -1)
					{
						currentSelectableTime = selectableTimes.get(currentSelectableTimeID);
						currentSelectableTimeFTSID = currentSelectableTime.segment.getFieldTimelineID();						// Set current selectable time (white rectangle) from current field time segment
					}
					else
					{
						currentSelectableTimeFTSID = -1;
						currentSelectableTime = null;
					}
				}
				else
				{
					currentSelectableTimeID = -1;
					currentSelectableTimeFTSID = -1;
					currentSelectableTime = null;
				}

				if(updateCurrentSelectableDate)
				{
					if(currentSelectableTimeID != previous && currentSelectableDate > -1)						// If changed field segment and displaying a single date
					{
						int fieldDate = p.getCurrentField().timeline.get(p.viewer.getCurrentFieldTimeSegment()).getFieldDateID();		// Update date displayed
						setCurrentSelectableDate(fieldDate);
					}
				}

//				if(currentSelectableTime != -1)
//					PApplet.println("Set currentSelectableTime to:"+currentSelectableTime);

				updateCurrentSelectableTime = false;
				updateCurrentSelectableDate = false;
			}
			else PApplet.println("updateCurrentSelectableTime... No current time segment!");
		}
		
		if(timelineTransition)
			updateTimelineTransition();
	}
	
	/** 
	 * Get associated selectable time segment ID for given time segment
	 * @param t Given time segment
	 * @return Selectable time associated
	 */
	private int getSelectableTimeIDOfTimeSegment(WMV_TimeSegment t)
	{
		for(SelectableTimeSegment st : selectableTimes)
		{
			if( t.getClusterID() == st.segment.getClusterID() && t.getClusterDateID() == st.segment.getClusterDateID() &&
				t.getClusterTimelineID() == st.segment.getClusterTimelineID() && t.getFieldTimelineID() == st.segment.getFieldTimelineID() )
			{
				return st.getID();
			}
		}
		return -1;
	}

	/**
	 * Initialize dateline
	 */
	private void createDateline()
	{
		WMV_Field f = p.getCurrentField();
		
		if(f.dateline.size() > 0)
		{
			WMV_Date first = f.dateline.get(0);
			float padding = 1.f;
			int firstDay = first.getDay();
			int firstMonth = first.getMonth();
			int firstYear = first.getYear();

			WMV_Date last = f.dateline.get(f.dateline.size()-1);
			int lastDay = last.getDay();
			int lastMonth = last.getMonth();
			int lastYear = last.getYear();
			
			if(f.dateline.size() == 2)
				padding = p.p.utilities.getDaysSince1980(lastDay, lastMonth, lastYear) - p.p.utilities.getDaysSince1980(firstDay, firstMonth, firstYear);
			else if(f.dateline.size() > 2)
				padding = (p.p.utilities.getDaysSince1980(lastDay, lastMonth, lastYear) - p.p.utilities.getDaysSince1980(firstDay, firstMonth, firstYear)) * 0.33f;
			
			datelineStart = p.p.utilities.getDaysSince1980(firstDay, firstMonth, firstYear) - padding;
			datelineEnd = p.p.utilities.getDaysSince1980(lastDay, lastMonth, lastYear) + padding;
			
			createSelectableDates();
			datelineCreated = true;
		}
		else
		{
			PApplet.println("ERROR no dateline in field!!");
		}
	}
	
	private void createTimeline()
	{
		WMV_Field f = p.getCurrentField();
		selectableTimes = new ArrayList<SelectableTimeSegment>();
		
		if(f.dateline.size() == 1)
		{
			int count = 0;
			for(WMV_TimeSegment t : f.timeline)
			{
				SelectableTimeSegment st = getSelectableTime(t, count);
				if(st != null)
				{
					selectableTimes.add(st);
					count++;
				}
			}
		}
		else if(f.dateline.size() > 1)
		{
			if(displayDate == -1)
			{
				int count = 0;
				for(ArrayList<WMV_TimeSegment> ts : f.timelines)
				{
					for(WMV_TimeSegment t:ts)
					{
						SelectableTimeSegment st = getSelectableTime(t, count);
						if(st != null)
						{
							selectableTimes.add(st);
							count++;
						}
					}
				}
			}
			else
			{
				if(displayDate < f.timelines.size())
				{
					ArrayList<WMV_TimeSegment> ts = f.timelines.get(displayDate);
					int count = 0;
					
					for(WMV_TimeSegment t:ts)
					{
						SelectableTimeSegment st = getSelectableTime(t, count);
						if(st != null)
						{
							selectableTimes.add(st);
							count++;
						}
					}
				}
			}
		}
		
		timelineCreated = true;
	}

	private void createSelectableDates()
	{
		WMV_Field f = p.getCurrentField();
		selectableDates = new ArrayList<SelectableDate>();
		
		if(f.dateline.size() == 1)
		{
			int count = 0;
			SelectableDate sd = getSelectableDate(f.dateline.get(0), 0);
			if(sd != null)
				selectableDates.add(sd);
		}
		else if(f.dateline.size() > 1)
		{
			int count = 0;
			for(WMV_Date t : f.dateline)
			{
				SelectableDate sd = getSelectableDate(t, count);
				if(sd != null)
				{
					selectableDates.add(sd);
					count++;
				}
			}
		}
	}

	/**
	 * Transition map zoom from current to given value
	 */
	void timelineTransition(float newStart, float newEnd, int transitionLength)
	{
		timelineTransitionLength = transitionLength;

		if(!timelineTransition)
		{
			if(timelineStart != newStart || timelineEnd != newEnd)					// Check if already at target
			{
				timelineTransition = true;   
				timelineTransitionStartFrame = p.p.frameCount;
				timelineTransitionEndFrame = timelineTransitionStartFrame + timelineTransitionLength;
//				timelineCreated = false;
				
				if(timelineStart != newStart && timelineEnd != newEnd)
				{
					timelineStartTransitionStart = timelineStart;
					timelineStartTransitionTarget = newStart;
					timelineEndTransitionStart = timelineEnd;
					timelineEndTransitionTarget = newEnd;
//					PApplet.println("timelineStartTransitionStart:"+timelineStartTransitionStart+" timelineStartTransitionTarget:"+timelineStartTransitionTarget);
//					PApplet.println("timelineEndTransitionStart:"+timelineEndTransitionStart+" timelineEndTransitionTarget:"+timelineEndTransitionTarget);
				}
				else if(timelineStart != newStart && timelineEnd == newEnd)
				{
					timelineStartTransitionStart = timelineStart;
					timelineStartTransitionTarget = newStart;
					timelineEndTransitionTarget = timelineEnd;
				}
				else if(timelineStart == newStart && timelineEnd != newEnd)
				{
					timelineStartTransitionTarget = timelineStart;
					timelineEndTransitionStart = timelineEnd;
					timelineEndTransitionTarget = newEnd;
				}
			}
			else
			{
				timelineTransition = false;
			}
		}
	}
	
	/**
	 * Update map zoom level each frame
	 */
	void updateTimelineTransition()
	{
		float newStart = timelineStart;
		float newEnd = timelineEnd;

		if (p.p.frameCount >= timelineTransitionEndFrame)
		{
			newStart = timelineStartTransitionTarget;
			newEnd = timelineEndTransitionTarget;
			timelineTransition = false;
			updateTimeline = true;
			transitionScrollIncrement = initTransitionScrollIncrement * getZoomLevel();
		} 
		else
		{
			if(timelineStart != timelineStartTransitionTarget)
			{
				newStart = PApplet.map( p.p.frameCount, timelineTransitionStartFrame, timelineTransitionEndFrame,
									    timelineStartTransitionStart, timelineStartTransitionTarget); 
			}
			if(timelineEnd != timelineEndTransitionTarget)
			{
				newEnd = PApplet.map( p.p.frameCount, timelineTransitionStartFrame, timelineTransitionEndFrame,
									  timelineEndTransitionStart, timelineEndTransitionTarget);     			
			}
		}

		if(timelineStart != newStart)
			timelineStart = newStart;
		if(timelineEnd != newEnd)
			timelineEnd = newEnd;

		if(timelineScrolling)
			scroll(transitionScrollDirection);

		if(timelineZooming)
			zoom(transitionZoomDirection, true);

		if(p.p.debug.time)
		{
			PApplet.print("Updated timelineStart:"+timelineStart);
			PApplet.println(" timelineEnd:"+timelineEnd);
		}
	}

	/**
	 * Create and return selectable time from timeline
	 * @param t Time segment
	 * @param id Time segment id
	 * @return SelectableTime object
	 */
	private SelectableTimeSegment getSelectableTime(WMV_TimeSegment t, int id)
	{
		float lowerSeconds = p.p.utilities.getTimePVectorSeconds(t.getLower().getTimeAsPVector());
		float upperSeconds = p.p.utilities.getTimePVectorSeconds(t.getUpper().getTimeAsPVector());

		if(upperSeconds == lowerSeconds)
		{
			lowerSeconds -= minSegmentSeconds * 0.5f;
			upperSeconds += minSegmentSeconds * 0.5f;
		}
		
		float xOffset = PApplet.map(lowerSeconds, timelineStart, timelineEnd, timelineXOffset, timelineXOffset + timelineScreenSize);
		float xOffset2 = PApplet.map(upperSeconds, timelineStart, timelineEnd, timelineXOffset, timelineXOffset + timelineScreenSize);
 
		if(xOffset > timelineXOffset && xOffset2 < timelineXOffset + timelineScreenSize)
		{
			float rectLeftEdge, rectRightEdge, rectTopEdge, rectBottomEdge;
			float rectWidth = xOffset2 - xOffset;

			PVector loc = new PVector(xOffset, timelineYOffset, hudDistance);
			
			rectLeftEdge = loc.x;
			rectRightEdge = rectLeftEdge + rectWidth;
			rectTopEdge = timelineYOffset - timelineHeight * 0.5f;
			rectBottomEdge = timelineYOffset + timelineHeight * 0.5f;
			
			loc.x += (xOffset2 - xOffset) * 0.5f;
			SelectableTimeSegment st = new SelectableTimeSegment(id, t.getFieldTimelineID(), t.getClusterID(), t, loc, rectLeftEdge, rectRightEdge, rectTopEdge, rectBottomEdge);		// int newID, int newClusterID, PVector newLocation, Box newRectangle
			return st;
		}
		else return null;
	}	

	/**
	 * Create and return selectable date from dateline
	 * @param t Time segment
	 * @param id Time segment id
	 * @return SelectableTime object
	 */
	private SelectableDate getSelectableDate(WMV_Date d, int id)
	{
		float date = d.getDaysSince1980();
		float xOffset = PApplet.map(date, datelineStart, datelineEnd, datelineXOffset, datelineXOffset + timelineScreenSize);

		if(xOffset > datelineXOffset && xOffset < datelineXOffset + timelineScreenSize)
		{
			float radius = 25.f;
			PVector loc = new PVector(xOffset, datelineYOffset, hudDistance);
			SelectableDate st = new SelectableDate(id, loc, radius, d);		// int newID, int newClusterID, PVector newLocation, Box newRectangle
			return st;
		}
		else return null;
	}	

	/**
	 * Draw the field timeline
	 */
	private void displayFieldTimeline()
	{
		WMV_Field f = p.getCurrentField();
			
		p.p.tint(255);
		p.p.stroke(0.f, 0.f, 255.f, 255.f);
		p.p.strokeWeight(3.f);
		p.p.fill(0.f, 0.f, 255.f, 255.f);
		p.p.line(timelineXOffset, timelineYOffset, hudDistance, timelineXOffset + timelineScreenSize, timelineYOffset, hudDistance);
		
		String startTime = p.p.utilities.secondsToTimeAsString(timelineStart, false, false);
		String endTime = p.p.utilities.secondsToTimeAsString(timelineEnd, false, false);
		
		p.p.textSize(timeTextSize);
		float firstHour = p.p.utilities.roundSecondsToHour(timelineStart);
		if(firstHour == (int)(timelineStart / 3600.f) * 3600.f) firstHour += 3600.f;
		float lastHour = p.p.utilities.roundSecondsToHour(timelineEnd);
		if(lastHour == (int)(timelineEnd / 3600.f + 1.f) * 3600.f) lastHour -= 3600.f;
		
		float timeLength = timelineEnd - timelineStart;
		float timeToScreenRatio = timelineScreenSize / timeLength;
		
		if(lastHour / 3600.f - firstHour / 3600.f <= 16.f)
		{
			float xOffset = timelineXOffset + (firstHour - timelineStart) * timeToScreenRatio - 20.f;
			
			float pos;
			for( pos = firstHour ; pos <= lastHour ; pos += 3600.f )
			{
				String time = p.p.utilities.secondsToTimeAsString(pos, false, false);
				p.p.text(time, xOffset, timelineYOffset - timelineHeight * 0.5f - 40.f, hudDistance);
				xOffset += 3600.f * timeToScreenRatio;
			}
			
			if( (firstHour - timelineStart) * timeToScreenRatio - 20.f > 200.f)
				p.p.text(startTime, timelineXOffset, timelineYOffset - timelineHeight * 0.5f - 40.f, hudDistance);
			
			xOffset -= 3600.f * timeToScreenRatio;
			if(timelineXOffset + timelineScreenSize - 40.f - xOffset > 200.f)
				p.p.text(endTime, timelineXOffset + timelineScreenSize - 40.f, timelineYOffset - timelineHeight * 0.5f - 40.f, hudDistance);
		}
		else
		{
			float xOffset = timelineXOffset;
			for( float pos = firstHour - 3600.f ; pos <= lastHour ; pos += 7200.f )
			{
				String time = p.p.utilities.secondsToTimeAsString(pos, false, false);
				p.p.text(time, xOffset, timelineYOffset - timelineHeight * 0.5f - 40.f, hudDistance);
				xOffset += 7200.f * timeToScreenRatio;
			}
		}

		if(f.dateline.size() == 1)
		{
			int count = 0;
			for(WMV_TimeSegment t : f.timeline)
			{
				drawTimeSegment(t, count);
				count++;
			}
		}
		else if(f.dateline.size() > 1)
		{
			if(displayDate == -1)
			{
				int count = 0;
				for(ArrayList<WMV_TimeSegment> ts : f.timelines)
				{
					for(WMV_TimeSegment t:ts)
					{
						drawTimeSegment(t, count);
						count++;
					}
				}
			}
			else
			{
				if(displayDate < f.timelines.size())
				{
					ArrayList<WMV_TimeSegment> ts = f.timelines.get(displayDate);
					int count = 0;
					for(WMV_TimeSegment t:ts)
					{
						drawTimeSegment(t, count);
						count++;
					}
				}
			}
		}
		
		if(!timelineTransition)
		{
			/* Draw selected time segment */
			if(selectedTime != -1 && selectableTimes.size() > 0 && selectedTime < selectableTimes.size())
				selectableTimes.get(selectedTime).draw(40.f, 255.f, 255.f, true);

			/* Draw current time segment */
//			if(currentSelectableTimeFTSID == currentSelectableTime.segment.getFieldTimelineID())
			if(currentSelectableTimeID >= 0 && currentSelectableTimeID < selectableTimes.size())
			{
				if(currentSelectableTimeFTSID == selectableTimes.get(currentSelectableTimeID).segment.getFieldTimelineID())
				{
//					PApplet.println("currentSelectableTimeFTSID: "+currentSelectableTimeFTSID);
					if(currentSelectableTimeID != -1 && selectableTimes.size() > 0 && currentSelectableTimeID < selectableTimes.size())
					{
						if(displayDate == -1 || selectableTimes.get(currentSelectableTimeID).segment.getFieldDateID() == displayDate)
						{
							if(selectedTime == -1)
								selectableTimes.get(currentSelectableTimeID).draw(0.f, 0.f, 255.f, true);
							else
								selectableTimes.get(currentSelectableTimeID).draw(0.f, 0.f, 255.f, false);
						}
					}
				}
//				else updateCurrentSelectableTime = true;
			}
		}
	}

	private void drawFieldDateline()
	{
		WMV_Field f = p.getCurrentField();
			
		p.p.tint(255);
		p.p.stroke(0.f, 0.f, 255.f, 255.f);
		p.p.strokeWeight(3.f);
		p.p.fill(0.f, 0.f, 255.f, 255.f);
		p.p.line(datelineXOffset, datelineYOffset, hudDistance, datelineXOffset + timelineScreenSize, datelineYOffset, hudDistance);
	
		if(f.dateline.size() == 1)
		{
			drawDate(f.dateline.get(0), 0);
		}
		else if(f.dateline.size() > 1)
		{
			int count = 0;
			for(WMV_Date d : f.dateline)
			{
				drawDate(d, count);
				count++;
			}
		}
		
		if(selectedDate != -1 && selectableDates.size() > 0 && selectedDate < selectableDates.size())
			selectableDates.get(selectedDate).draw(40.f, 255.f, 255.f, true);
		if(currentSelectableDate != -1 && selectableDates.size() > 0 && currentSelectableDate < selectableDates.size())
			selectableDates.get(currentSelectableDate).draw(0.f, 0.f, 255.f, false);
	}

	/**
	 * Draw time segment on timeline
	 * @param d
	 * @param timelineLeftEdge
	 * @param timelineTopEdge
	 */
	private void drawDate(WMV_Date d, int id)
	{
		float date = d.getDaysSince1980();
		float xOffset = PApplet.map(date, datelineStart, datelineEnd, datelineXOffset, datelineXOffset + timelineScreenSize);

		if(xOffset > datelineXOffset && xOffset < datelineXOffset + timelineScreenSize)
		{
			p.p.strokeWeight(0.f);

			p.p.fill(120.f, 165.f, 245.f, 155.f);

			p.p.pushMatrix();
			p.p.stroke(120.f, 165.f, 245.f, 155.f);
			p.p.strokeWeight(25.f);
			p.p.point(xOffset, datelineYOffset, hudDistance);
			p.p.popMatrix();
		}
	}
	
	/**
	 * Draw time segment on timeline
	 * @param t
	 * @param timelineLeftEdge
	 * @param timelineTopEdge
	 */
	private void drawTimeSegment(WMV_TimeSegment t, int id)
	{
		PVector lowerTime = t.getLower().getTimeAsPVector();			// Format: PVector(hour, minute, second)
		PVector upperTime = t.getUpper().getTimeAsPVector();			

		float lowerSeconds = p.p.utilities.getTimePVectorSeconds(lowerTime);
		float upperSeconds = p.p.utilities.getTimePVectorSeconds(upperTime);
//		boolean instant = (upperSeconds == lowerSeconds);
		if(upperSeconds == lowerSeconds)
		{
			lowerSeconds -= minSegmentSeconds * 0.5f;
			upperSeconds += minSegmentSeconds * 0.5f;
		}

		ArrayList<WMV_Time> tsTimeline = t.getTimeline();				// Get timeline for this time segment
		ArrayList<PVector> times = new ArrayList<PVector>();
		for(WMV_Time ti : tsTimeline) times.add(ti.getTimeAsPVector());

		float xOffset = PApplet.map(lowerSeconds, timelineStart, timelineEnd, timelineXOffset, timelineXOffset + timelineScreenSize);
		float xOffset2 = PApplet.map(upperSeconds, timelineStart, timelineEnd, timelineXOffset, timelineXOffset + timelineScreenSize);

		if(xOffset > timelineXOffset && xOffset2 < timelineXOffset + timelineScreenSize)
		{
			p.p.pushMatrix();
			p.p.translate(0.f, timelineYOffset, hudDistance);
//			p.p.stroke(140.f, 185.f, 255.f, 155.f);
			p.p.stroke(imageHue, 185.f, 255.f, 155.f);
			p.p.strokeWeight(1.f);

			for(PVector time : times)
			{
				float seconds = p.p.utilities.getTimePVectorSeconds(time);
				float xOff = PApplet.map(seconds, timelineStart, timelineEnd, timelineXOffset, timelineXOffset + timelineScreenSize);
				if(xOff > timelineXOffset && xOff < timelineXOffset + timelineScreenSize)
					p.p.line(xOff, -timelineHeight / 2.f, 0.f, xOff, timelineHeight / 2.f, 0.f);
			}

			/* Set hue according to media type */
			float firstHue = imageHue;
			float secondHue = imageHue;
			
			if(t.hasVideo())
				firstHue = videoHue;

			if(t.hasSound())
			{
				if(firstHue == videoHue)
					secondHue = soundHue;
				else
					firstHue = soundHue;
			}
			
			if(t.hasPanorama())
			{
				if(firstHue == soundHue)
					secondHue = panoramaHue;
				else if(firstHue == videoHue)
				{
					if(secondHue == imageHue)
						secondHue = panoramaHue;
				}
				else
					firstHue = panoramaHue;
			}
			
			if(!t.hasImage())
			{
				float defaultHue = 0.f;
				if(firstHue == imageHue)
					PApplet.println("ERROR: firstHue still imageHue but segment has no image!");
				else
					defaultHue = firstHue;
				if(secondHue == imageHue)
					secondHue = defaultHue;
			}
			
			p.p.strokeWeight(2.f);

			/* Draw rectangle around time segment */
			p.p.stroke(firstHue, 165.f, 215.f, 225.f);					
			p.p.line(xOffset, -timelineHeight * 0.5f, 0.f, xOffset, timelineHeight * 0.5f, 0.f);			
			p.p.line(xOffset, timelineHeight * 0.5f, 0.f, xOffset2, timelineHeight * 0.5f, 0.f);
			p.p.stroke(secondHue, 165.f, 215.f, 225.f);					
			p.p.line(xOffset2, -timelineHeight * 0.5f, 0.f, xOffset2, timelineHeight * 0.5f, 0.f);
			p.p.line(xOffset, -timelineHeight * 0.5f, 0.f, xOffset2, -timelineHeight * 0.5f, 0.f);
			
			p.p.popMatrix();
		}
	}

	/**
	 * Get mouse 3D location from screen location
	 * @param mouseX
	 * @param mouseY
	 * @return
	 */
	private PVector getMouse3DLocation(float mouseX, float mouseY)
	{
		float wFactor = 2.55f;
		float hFactor = 2.55f;
		p.p.stroke(255, 255, 255);
		p.p.strokeWeight(4.f);
		
		PVector result = new PVector(mouseX * wFactor - p.p.width * 0.775f, mouseY * hFactor - p.p.height * 0.775f, hudDistance);
//		p.p.point(result.x, result.y, result.z);		// Show mouse location for debugging
		return result;
	}
	
	/**
	 * Get selected time segment for given mouse 3D location
	 * @param mouseScreenLoc Mouse 3D location
	 * @return Selected time segment
	 */
	private SelectableTimeSegment getSelectedTimeSegment(PVector mouseLoc)
	{
		for(SelectableTimeSegment st : selectableTimes)
			if( mouseLoc.x > st.leftEdge && mouseLoc.x < st.rightEdge && 
				mouseLoc.y > st.topEdge && mouseLoc.y < st.bottomEdge )
				return st;
		
		return null;
	}
	
	/**
	 * Get selected time segment for given mouse 3D location
	 * @param mouseScreenLoc Mouse 3D location
	 * @return Selected time segment
	 */
	private SelectableDate getSelectedDate(PVector mouseLoc)
	{
		for(SelectableDate sd : selectableDates)
		{
			if(PVector.dist(mouseLoc, sd.getLocation()) < sd.radius)
				return sd;
		}
		
		return null;
	}

	public void updateTimelineMouse()
	{
		PVector mouseLoc = getMouse3DLocation(p.p.mouseX, p.p.mouseY);
		if(selectableTimes != null)
		{
			SelectableTimeSegment timeSelected = getSelectedTimeSegment(mouseLoc);
			if(timeSelected != null)
			{
				selectedTime = timeSelected.getID();				// Set to selected
				selectedCluster = timeSelected.getClusterID();

				if(p.p.debug.time)
					PApplet.println("Selected time segment:"+selectedTime+" selectedCluster:"+selectedCluster);
				updateTimeline = true;				// Update timeline to show selected segment
			}
			else
				selectedTime = -1;
		}
		
		if(selectedTime == -1 && selectableDates != null)
		{
			SelectableDate dateSelected = getSelectedDate(mouseLoc);
			if(dateSelected != null)
			{
				selectedDate = dateSelected.getID();				// Set to selected
						
				if(p.p.debug.time) 
					PApplet.println("Selected date:"+selectedDate);

				updateTimeline = true;				// Update timeline to show selected segment
			}
			else
				selectedDate = -1;
		}
	}
	
	public void zoomToTimeline(boolean fade)
	{
		WMV_Field f = p.getCurrentField();
		
		float first = f.timeline.get(0).getLower().getTime();						// First field media time, normalized
		float last = f.timeline.get(f.timeline.size()-1).getUpper().getTime();		// Last field media time, normalized
		float day = p.p.utilities.getTimePVectorSeconds(new PVector(24,0,0));		// Seconds in a day

		first *= day;					// Convert from normalized value to seconds
		last *= day;
		
		float newTimelineStart = p.p.utilities.roundSecondsToHour(first);		// Round down to nearest hour
		if(newTimelineStart > first) newTimelineStart -= 3600;
		if(newTimelineStart < 0.f) newTimelineStart = 0.f;
		float newTimelineEnd = p.p.utilities.roundSecondsToHour(last);			// Round up to nearest hour
		if(newTimelineEnd < last) newTimelineEnd += 3600;
		if(newTimelineEnd > day) newTimelineEnd = day;

		if(fade)
		{
			timelineTransition(newTimelineStart, newTimelineEnd, initTimelineTransitionLength);
		}
		else
		{
			timelineStart = newTimelineStart;
			timelineEnd = newTimelineEnd;
		}
	}
	
	public void zoomToCurrentTimeSegment(boolean fade)
	{
		if(currentSelectableTimeID >= 0)
		{
			float first = selectableTimes.get(currentSelectableTimeID).segment.getLower().getTime();
			float last = selectableTimes.get(currentSelectableTimeID).segment.getUpper().getTime();
			float day = p.p.utilities.getTimePVectorSeconds(new PVector(24,0,0));		// Seconds in a day

	//		float first = f.timeline.get(0).getLower().getTime();						// First field media time, normalized
	//		float last = f.timeline.get(f.timeline.size()-1).getUpper().getTime();		// Last field media time, normalized
	//		float day = p.p.utilities.getTimePVectorSeconds(new PVector(24,0,0));		// Seconds in a day

			first *= day;					// Convert from normalized value to seconds
			last *= day;

			/**
			 * Round given value in seconds to nearest value given by interval parameter
			 * @param value Value to round
			 * @param interval Number of seconds to round to
			 * @return Rounded value
			 */
//			public int roundSecondsToInterval(float value, float interval)

			float newTimelineStart = p.p.utilities.roundSecondsToInterval(first, 600.f);		// Round down to nearest hour
			if(newTimelineStart > first) newTimelineStart -= 600;
			if(newTimelineStart < 0.f) newTimelineStart = 0.f;
			float newTimelineEnd = p.p.utilities.roundSecondsToInterval(last, 600.f);			// Round up to nearest hour
			if(newTimelineEnd < last) newTimelineEnd += 600;
			if(newTimelineEnd > day) newTimelineEnd = day;

			if(fade)
			{
				timelineTransition(newTimelineStart, newTimelineEnd, initTimelineTransitionLength);
			}
			else
			{
				timelineStart = newTimelineStart;
				timelineEnd = newTimelineEnd;
			}
		}
	}
	
	public void zoomToCurrentDate(boolean fade)
	{
		if(currentSelectableDate >= 0)
		{
			WMV_Field f = p.getCurrentField();
			int curDate = selectableDates.get(currentSelectableDate).getID();
			float first = f.timelines.get(curDate).get(0).getLower().getTime();
			float last = f.timelines.get(curDate).get(f.timelines.get(curDate).size()-1).getUpper().getTime();
			float day = p.p.utilities.getTimePVectorSeconds(new PVector(24,0,0));		// Seconds in a day

			first *= day;					// Convert from normalized value to seconds
			last *= day;

			float newTimelineStart = p.p.utilities.roundSecondsToInterval(first, 1800.f);		// Round down to nearest hour
			if(newTimelineStart > first) newTimelineStart -= 1800;
			if(newTimelineStart < 0.f) newTimelineStart = 0.f;
			float newTimelineEnd = p.p.utilities.roundSecondsToInterval(last, 1800.f);			// Round up to nearest hour
			if(newTimelineEnd < last) newTimelineEnd += 1800;
			if(newTimelineEnd > day) newTimelineEnd = day;

//			float newTimelineStart = p.p.utilities.roundSecondsToHour(first);		// Round down to nearest hour
//			if(newTimelineStart > first) newTimelineStart -= 3600;
//			if(newTimelineStart < 0.f) newTimelineStart = 0.f;
//			float newTimelineEnd = p.p.utilities.roundSecondsToHour(last);			// Round up to nearest hour
//			if(newTimelineEnd < last) newTimelineEnd += 3600;
//			if(newTimelineEnd > day) newTimelineEnd = day;

			if(fade)
			{
				timelineTransition(newTimelineStart, newTimelineEnd, initTimelineTransitionLength);
			}
			else
			{
				timelineStart = newTimelineStart;
				timelineEnd = newTimelineEnd;
			}
		}
	}
	
	public void resetZoom(boolean fade)
	{
		float newTimelineStart = 0.f;
		float newTimelineEnd = p.p.utilities.getTimePVectorSeconds(new PVector(24,0,0));
		
		if(fade)
		{
			timelineTransition(newTimelineStart, newTimelineEnd, initTimelineTransitionLength);
		}
		else
		{
			timelineStart = newTimelineStart;
			timelineEnd = newTimelineEnd;
		}
	}
	
	public float getZoomLevel()
	{
		float day = p.p.utilities.getTimePVectorSeconds(new PVector(24,0,0));		// Seconds in a day
		float result = (timelineEnd - timelineStart) / day;
		return result;
	}
	
	/**
	 * Start timeline zoom transition
	 * @param direction -1: In  1: Out
	 * @param fade Whether to use transition animation
	 */
	public void zoom(int direction, boolean fade)
	{
		boolean zoom = true;
		transitionZoomDirection = direction;
		float length = timelineEnd - timelineStart;
		float newLength;
		float day = p.p.utilities.getTimePVectorSeconds(new PVector(24,0,0));		// Seconds in a day
		
		if(transitionZoomDirection == -1)
			newLength = length * transitionZoomInIncrement;
		else
			newLength = length * transitionZoomOutIncrement;

		float newTimelineStart, newTimelineEnd;
		if(transitionZoomDirection == -1)
		{
			float diff = length - newLength;
			newTimelineStart = timelineStart + diff / 2.f;
			newTimelineEnd = timelineEnd - diff / 2.f;
		}
		else
		{
			float diff = newLength - length;
			newTimelineStart = timelineStart - diff / 2.f;
			newTimelineEnd = timelineEnd + diff / 2.f;
			
			if(newTimelineEnd > day)
			{
				newTimelineStart -= (newTimelineEnd - day);
				newTimelineEnd = day;
			}
			if(newTimelineStart < 0.f)
			{
				newTimelineEnd -= newTimelineStart;
				newTimelineStart = 0.f;
			}
			if(newTimelineEnd > day)
				zoom = false;
		}
		
		if(zoom)
		{
			WMV_Field f = p.getCurrentField();
			
			float first = f.timeline.get(0).getLower().getTime();						// First field media time, normalized
			float last = f.timeline.get(f.timeline.size()-1).getUpper().getTime();		// Last field media time, normalized

			if(transitionZoomDirection == 1)
			{
				if(length - newLength > 300.f)
				{
					newTimelineStart = p.p.utilities.roundSecondsToInterval(newTimelineStart, 600.f);		// Round up to nearest 10 min.
					if(newTimelineStart > first) newTimelineStart -= 600;
					newTimelineEnd = p.p.utilities.roundSecondsToInterval(newTimelineEnd, 600.f);		// Round up to nearest 10 min.
					if(newTimelineEnd < last) newTimelineEnd += 600;
				}
			}
			if(newTimelineEnd > day) newTimelineEnd = day;
			if(newTimelineStart < 0.f) newTimelineEnd = 0.f;

			if(fade)
			{
				timelineTransition(timelineStart, newTimelineEnd, 10);
				timelineZooming = true;
			}
			else
				timelineEnd = newTimelineEnd;
		}
	}
	
	public void zoomByAmount(float amount, boolean fade)
	{
		float length = timelineEnd - timelineStart;
		float newLength = length * amount;
		float result = timelineStart + newLength;
		
		if(result < p.p.utilities.getTimePVectorSeconds(new PVector(24,0,0)))
		{
			WMV_Field f = p.getCurrentField();
			float last = f.timeline.get(f.timeline.size()-1).getUpper().getTime();		// Last field media time, normalized
			float day = p.p.utilities.getTimePVectorSeconds(new PVector(24,0,0));		// Seconds in a day

			float newTimelineEnd;
			if(length - newLength > 300.f)
			{
				newTimelineEnd = p.p.utilities.roundSecondsToInterval(result, 600.f);		// Round up to nearest 10 min.
				if(newTimelineEnd < last) newTimelineEnd += 600;
			}
			else						
				newTimelineEnd = result;													// Changing length less than 5 min., no rounding								

			if(newTimelineEnd > day) newTimelineEnd = day;

			if(fade)
				timelineTransition(timelineStart, newTimelineEnd, initTimelineTransitionLength);
			else
				timelineEnd = newTimelineEnd;
		}
	}
	
	/**
	 * Start scrolling timeline in specified direction
	 * @param direction Left: -1 or Right: 1
	 */
	public void scroll(int direction)
	{
		transitionScrollDirection = direction;
		float newStart = timelineStart + transitionScrollIncrement * transitionScrollDirection;
		float newEnd = timelineEnd + transitionScrollIncrement * transitionScrollDirection;		
		float day = p.p.utilities.getTimePVectorSeconds(new PVector(24,0,0));
		
		if(newStart > 0.f && newEnd < day)
		{
			timelineScrolling = true;
			timelineTransition(newStart, newEnd, 10);			
		}
	}
	
	public void stopZooming()
	{
		timelineZooming = false;
		updateTimeline = true;
		transitionScrollIncrement = initTransitionScrollIncrement * getZoomLevel();
	}
	
	public void stopScrolling()
	{
		timelineScrolling = false;
		updateTimeline = true;
	}
	
	public void handleMouseReleased(float mouseX, float mouseY)
	{
		updateTimelineMouse();
		
		if(selectedTime != -1)
			if(selectedCluster != -1)
				p.viewer.teleportToCluster(selectedCluster, false, selectableTimes.get(selectedTime).segment.getFieldTimelineID());

		if(selectedDate != -1)
		{
			setCurrentSelectableDate(selectedDate);
		}
	}

	private void setCurrentSelectableDate(int newSelectableDate)
	{
		displayDate = newSelectableDate;
		currentSelectableDate = newSelectableDate;
		updateCurrentSelectableTime = true;
		updateCurrentSelectableDate = false;
		updateTimeline = true;
	}

	/**
	 * Draw Interactive Clustering screen
	 */
	void displayInteractiveClustering()
	{
		map2D.drawMainMap(false);
		if(messages.size() > 0) displayMessages();
	}

	/**
	 * Draw Interactive Clustering footer text
	 */
	void displayClusteringInfo()
	{
//		message("Interactive Clustering Mode: "+(p.hierarchical ?"Hierarchical Clustering":"K-Means Clustering"));
//		message(" ");
		
		if(p.hierarchical)
		{
//			message("Hierarchical Clustering");
			message(" ");
			message("Use arrow keys UP and DOWN to change clustering depth... ");
			message("Use [ and ] to change Minimum Cluster Distance... ");
		}
		else
		{
//			message("K-Means Clustering");
			message(" ");
			message("Use arrow keys LEFT and RIGHT to change Iterations... ");
			message("Use arrow keys UP and DOWN to change Population Factor... ");
			message("Use [ and ] to change Minimum Cluster Distance... ");
		}
		
		message(" ");
		message("Press <spacebar> to restart 3D viewer...");
	}

	/**
	 * Draw progress bar
	 */
	void progressBar()
	{
		int length = 100;	// total length
		int pos = p.setupProgress;	//current position

		startHUD();
		for(int i=0; i<pos; i++)
		{
			p.p.pushMatrix();
			
			p.p.fill(140, 100, 255);
			float xPos = PApplet.map(i, 0, length, 0, p.p.width * 1.f);
			float inc = PApplet.map(2, 0, length, 0, p.p.width * 1.f) - PApplet.map(1, 0, length, 0, p.p.width*1.f);
			int x = -p.p.width/2 + (int)xPos;
			int y = -p.p.height/2+p.p.height/2;

			p.p.translate(x, y, hudDistance);
			p.p.box(inc, inc*10.f, 1);    // Display 
			p.p.popMatrix();
		}
	}
	
	void reset()
	{
		/* Window Modes */
		fullscreen = true;
		initializedMaps = false;

		/* Display Mode */
		displayView = 0;
		
		/* Debug */
		drawForceVector = false;
		
		/* Status */
//		initialSetup = true;
		
		/* Graphics */
		drawGrid = false; 			// Draw 3D grid   			-- Unused

		blendMode = 0;							// Alpha blending mode
		numBlendModes = 10;						// Number of blending modes

		/* Timeline */

		timelineHeight = 80.f;
		displayDate = -1;
		
		selectedTime = -1; 
		selectedCluster = -1;
		currentSelectableTimeID = -1;
		currentSelectableTime = null;
		selectedDate = -1; 
		currentSelectableDate = -1;

		timelineScreenSize = p.p.width * 2.2f;
		timelineStart = 0.f;
		timelineEnd = p.p.utilities.getTimePVectorSeconds(new PVector(24,0,0));
		datelineStart = 0.f;
		datelineEnd = 0.f;
		updateCurrentSelectableTime = true;
		updateCurrentSelectableDate = true;
		
		timelineXOffset = -p.p.width/ 1.66f;
		timelineYOffset = -p.p.height/ 2.f;
		timelineYOffset = 0.f;
		datelineXOffset = timelineXOffset;
		datelineYOffset = p.p.height * 0.2f;

		selectableTimes = new ArrayList<SelectableTimeSegment>();
		selectableDates = new ArrayList<SelectableDate>();

		timelineCreated = false;
		datelineCreated = false;
		updateTimeline = true;

		timelineTransition = false; 
		timelineZooming = false; 
		timelineScrolling = false;   
		
		transitionScrollDirection = -1; 
		transitionZoomDirection = -1;
		timelineTransitionStartFrame = 0; 
		timelineTransitionEndFrame = 0;
		timelineTransitionLength = 30; 
//		initTimelineTransitionLength = 30;
		timelineStartTransitionStart = 0; 
		timelineStartTransitionTarget = 0;
		timelineEndTransitionStart = 0; 
		timelineEndTransitionTarget = 0;
		transitionScrollIncrement = 2000.f; 
//		initTransitionScrollIncrement = 2000.f;	// Seconds to scroll per frame
		transitionZoomInIncrement = 0.95f; transitionZoomOutIncrement = 1.052f;	

		/* Clusters */
		displayCluster = 0;

		/* Messages */
		messageStartFrame = -1;
		metadataStartFrame = -1;
		startupMessageStartFrame = -1;
		messageDuration = 60;
		
		hudDistance = p.hudDistance;
		
		messages = new ArrayList<String>();
		metadata = new ArrayList<String>();
		startupMessages = new ArrayList<String>();

		centerTextXOffset = p.p.width / 2.f;
		leftTextXOffset = 0.f;
		midLeftTextXOffset = p.p.width / 3.f;
		rightTextXOffset = 0.f;
		midRightTextXOffset = p.p.width / 1.5f;

		topTextYOffset = -p.p.height / 1.6f;
		clusterImageXOffset = -p.p.width/ 1.9f;
		clusterImageYOffset = p.p.height / 2.5f;

		userMessageXOffset = -p.p.width / 2.f;
		userMessageYOffset = 0;

		metadataYOffset = -p.p.height / 2.f;

		startupMessageXOffset = p.p.width / 2.f;
		startupMessageYOffset = -p.p.height /2.f;
		
		map2D = new WMV_Map(this);
	}

	/**
	 * Initialize 2D drawing 
	 */
	void startHUD()
	{
		p.p.perspective(p.viewer.getInitFieldOfView(), (float)p.p.width/(float)p.p.height, p.viewer.settings.nearClippingDistance, 10000.f);;
		p.p.camera();
	}

	/**
	 * Display the main key commands on screen
	 */
	void displayControls()
	{
		startHUD();
		p.p.pushMatrix();
		
		float xPos = centerTextXOffset;
		float yPos = topTextYOffset;			// Starting vertical position
		
		p.p.fill(0, 0, 255, 255);                        
		p.p.textSize(largeTextSize);
		p.p.text(" Keyboard Controls ", xPos, yPos, hudDistance);

		xPos = midLeftTextXOffset;
		p.p.textSize(mediumTextSize);
		p.p.text(" Main", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(smallTextSize);
		p.p.text(" R    Restart MultimediaLocator", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.text(" CMD + q    Quit MultimediaLocator", xPos, yPos += lineWidth, hudDistance);

		p.p.textSize(mediumTextSize);
		p.p.text(" Display", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(smallTextSize);
		p.p.text(" 1    Show/Hide Field Map   		  +SHIFT to Overlay", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.text(" 2    Show/Hide Field Statistics    +SHIFT to Overlay", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" 3    Show/Hide Cluster Statistics  +SHIFT to Overlay", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" 4    Show/Hide Keyboard Controls   +SHIFT to Overlay", xPos, yPos += lineWidth, hudDistance);

		p.p.textSize(mediumTextSize);
		p.p.text(" Time", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(smallTextSize);
		p.p.text(" T    Time Fading On/Off", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.text(" D    Date Fading On/Off", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" Z    Toggle Time Fading Mode (Field/Cluster)", textXPos, textYPos += lineWidth, hudDistance);
		p.p.text(" space Pause On/Off   ", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" &/*  Default Media Length - / +", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" SHIFT + Lt/Rt   Cycle Length - / +", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" SHIFT + Up/Dn   Current Time - / +", xPos, yPos += lineWidth, hudDistance);

		p.p.textSize(mediumTextSize);
		p.p.text(" Time Navigation", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(smallTextSize);
		p.p.text(" t    Teleport to Earliest Time in Field", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.text(" T    Move to Earliest Time in Field", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" d    Teleport to Earliest Time on Earliest Date", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" D    Move to Earliest Time on Earliest Date", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" n    Move to Next Time Segment in Field", xPos, yPos += lineWidthWide, hudDistance);
		p.p.text(" N    Move to Next Time Segment in Cluster", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" b    Move to Previous Time Segment in Field", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" B    Move to Previous Time Segment in Cluster", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" l    Move to Next Date in Field", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" L    Move to Next Date in Cluster", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" k    Move to Previous Date in Field", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" K    Move to Previous Date in Cluster", xPos, yPos += lineWidth, hudDistance);

		xPos = centerTextXOffset;
		yPos = topTextYOffset;			// Starting vertical position

		/* Model */
		p.p.textSize(mediumTextSize);
		p.p.text(" Model", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(smallTextSize);
		p.p.text(" [ ]  Altitude Scaling Adjustment  + / - ", xPos, yPos += lineWidthVeryWide, hudDistance);
//		p.p.text(" , .  Object Distance  + / - ", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" - =  Object Distance  - / +      ", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" OPTION + -   Visible Angle  -      ", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" OPTION + =   Visible Angle  +      ", xPos, yPos += lineWidth, hudDistance);
		
		/* Graphics */
		p.p.textSize(mediumTextSize);
		p.p.text(" Graphics", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(smallTextSize);
		p.p.text(" G    Angle Fading On/Off", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.text(" H    Angle Thinning On/Off", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" P    Transparency Mode  On / Off      ", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" ( )  Blend Mode  - / +      ", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" i h v  Hide images / panoramas / videos    ", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" D    Video Mode On/Off ", xPos, yPos += lineWidth, hudDistance);

		/* Movement */
		p.p.textSize(mediumTextSize);
		p.p.text(" Movement", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(smallTextSize);
		p.p.text(" a d w s   Walk Left / Right / Forward / Backward ", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.text(" Arrows    Turn Camera ", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" q z  Zoom In / Out + / - ", xPos, yPos += lineWidth, hudDistance);
		
		/* Navigation */
		p.p.textSize(mediumTextSize);
		p.p.text(" Navigation", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(smallTextSize);
		p.p.text(" >    Follow Timeline Only", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.text(" .    Follow Timeline by Date", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" OPTION + .    Follow Dateline Only", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" E    Move to Nearest Cluster", xPos, yPos += lineWidthWide, hudDistance);
		p.p.text(" W    Move to Nearest Cluster in Front", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" Q    Move to Next Cluster in Time", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" A    Move to Next Location in Memory", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" Z    Move to Random Cluster", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" U    Move to Next Video ", xPos, yPos += lineWidthWide, hudDistance);
		p.p.text(" u    Teleport to Next Video ", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" M    Move to Next Panorama ", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" m    Teleport to Next Panorama ", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" C    Lock Viewer to Nearest Cluster On/Off", xPos, yPos += lineWidthWide, hudDistance);
//		p.p.text(" l    Look At Selected Media", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" L    Look for Media", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" { }  Teleport to Next / Previous Field ", xPos, yPos += lineWidth, hudDistance);

		xPos = midRightTextXOffset;
		yPos = topTextYOffset;			// Starting vertical position

		p.p.textSize(mediumTextSize);
		p.p.text(" Interaction", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(smallTextSize);
		p.p.text(" O    Selection Mode On/Off", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.text(" S    Multi-Selection Mode On/Off", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" OPTION + s    Segment Selection Mode On/Off", xPos, yPos += lineWidthWide, hudDistance);
		p.p.text(" x    Select Media in Front", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" X    Deselect Media in Front", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" OPTION + x    Deselect All Media", xPos, yPos += lineWidth, hudDistance);

		p.p.textSize(mediumTextSize);
		p.p.text(" GPS Tracks", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(smallTextSize);
		p.p.text(" g    Load GPS Track from File", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.text(" OPTION + g    Follow GPS Track", xPos, yPos += lineWidth, hudDistance);

		p.p.textSize(mediumTextSize);
		p.p.text(" Memory", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(smallTextSize);
		p.p.text(" `    Save Current View to Memory", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.text(" ~    Follow Memory Path", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" Y    Clear Memory", xPos, yPos += lineWidth, hudDistance);

		p.p.textSize(mediumTextSize);
		p.p.text(" Output", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(smallTextSize);
		p.p.text(" o    Set Image Output Folder", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.text(" p    Save Screen Image to Disk", xPos, yPos += lineWidth, hudDistance);

		p.p.popMatrix();
	}
	
	/**
	 * Add message to queue
	 * @param message Message to send
	 */
	void message(String message)
	{
		if(p.interactive)
		{
			messages.add(message);
			while(messages.size() > 16)
				messages.remove(0);
		}
		else
		{
			messageStartFrame = p.p.frameCount;		
			messages.add(message);
			while(messages.size() > 16)
				messages.remove(0);
		}

		if(p.p.debug.print)
			PApplet.println(message);
	}
	
	/**
	 * Clear previous messages
	 */
	void clearMessages()
	{
		messages = new ArrayList<String>();			
	}
	
	/**
	 * Display current messages
	 */
	void displayMessages()
	{
		float yPos = userMessageYOffset - lineWidth;

		p.viewer.start3DHUD();
		p.p.pushMatrix();
		p.p.fill(0, 0, 255, 255);            								
		p.p.textSize(smallTextSize);

		if(p.interactive)
		{
			for(String s : messages)
				p.p.text(s, userMessageXOffset, yPos += lineWidth, hudDistance);		// Use period character to draw a point
		}
		else if(p.p.frameCount - messageStartFrame < messageDuration)
		{
			for(String s : messages)
				p.p.text(s, userMessageXOffset, yPos += lineWidth, hudDistance);		// Use period character to draw a point
		}
		else
		{
			clearMessages();														// Clear messages after duration has ended
		}

		p.p.popMatrix();
	}

	/**
	 * Add a metadata message (single line) to the display queue
	 * @param message Line of metadata 
	 */
	void metadata(String message)
	{
		metadataStartFrame = p.p.frameCount;		
		metadata.add(message);
		
		while(metadata.size() > 16)
			metadata.remove(0);
	}
	
	/**
	 * Draw current metadata messages to the screen
	 */
	void displayMetadata()
	{
		float yPos = metadataYOffset - lineWidth;

		p.viewer.start3DHUD();
		p.p.pushMatrix();

		p.p.fill(0, 0, 255, 255);                     // White text
		p.p.textSize(mediumTextSize);

		for(String s : metadata)
			p.p.text(s, leftTextXOffset, yPos += lineWidth, hudDistance);				// Use period character to draw a point

		p.p.popMatrix();
	}
	
	/**
	 * Clear previous metadata messages
	 */
	void clearMetadata()
	{
		metadata = new ArrayList<String>();							// Reset message list
	}

	/**
	 * Show startup screen
	 */
	public void showStartup()
	{
		draw();								// Draw setup display
	}
	
	/**
	 * @param message Message to be sent
	 * Add startup message to display queue
	 */
	void sendSetupMessage(String message)
	{
		if(initialSetup)																
		{
			startupMessageStartFrame = p.p.frameCount;		
			startupMessages.add(message);
			while(startupMessages.size() > 16)
				startupMessages.remove(0);

			if(p.p.debug.print)
				PApplet.println(message);
		}
	}
	
	/**
	 * Display startup 
	 */
	void displayStartup()
	{
		float yPos = startupMessageYOffset;

		startHUD();
		p.p.pushMatrix();
		p.p.fill(0, 0, 245.f, 255.f);            								
		p.p.textSize(largeTextSize * 1.5f);

		if(initialSetup)																// Showing setup startup messages
		{
			p.p.textSize(largeTextSize * 3.f);
			p.p.text("MultimediaLocator", p.p.width / 2.25f, yPos += lineWidthVeryWide, hudDistance);
			p.p.textSize(mediumTextSize * 1.4f);
			p.p.text("v0.9", p.p.width / 1.075f, yPos += lineWidth, hudDistance);
			p.p.textSize(largeTextSize * 0.88f);
			p.p.text("Entoptic Software", p.p.width / 1.2f, yPos += lineWidthVeryWide, hudDistance);
			p.p.textSize(largeTextSize * 1.2f);
			
			if(!p.p.state.selectedLibrary)
				p.p.text("Press any key to begin...", p.p.width / 2.1f, yPos += lineWidthVeryWide * 5.f, hudDistance);
			else
				p.p.text("Loading media folder(s)...", p.p.width / 2.1f, yPos += lineWidthVeryWide * 5.f, hudDistance);
			
			p.p.textSize(largeTextSize * 1.2f);
			p.p.text("For support and the latest updates, visit: www.spatializedmusic.com/MultimediaLocator", p.p.width / 2.1f, yPos += lineWidthVeryWide * 6.f, hudDistance);
		}
		else
			displayMessages();

		p.p.popMatrix();
	}
	
	/**
	 * Clear previous setup messages
	 */
	void clearSetupMessages()
	{
		startupMessages = new ArrayList<String>();
	}
	
	/**
	 * Reset (turn off) display modes and clear messages
	 */
	void resetDisplayModes()
	{
		displayView = 0;
		clearMessages();
		clearMetadata();
	}
	
	 /**
	  * Increment blendMode by given amount and call setBlendMode()
	  * @param inc Increment to blendMode number
	  */
	public void changeBlendMode(int inc) 
	{
		if(inc > 0)
		{
			if (blendMode+inc < numBlendModes) 	
				blendMode += inc;
			else 
				blendMode = 0;
		}
		else if(inc < 0)
		{
			{
				if (blendMode-inc >= 0) 
					blendMode -= inc;
				else 
					blendMode = numBlendModes - 1;
			}
		}

		if(inc != 0)
			setBlendMode(blendMode);
	}

	/**
	 * Change effect of image alpha channel on blending
	 * @param blendMode
	 */
	public void setBlendMode(int blendMode) {
		switch (blendMode) {
		case 0:
			p.p.blendMode(PApplet.BLEND);
			break;

		case 1:
			p.p.blendMode(PApplet.ADD);
			break;

		case 2:
			p.p.blendMode(PApplet.SUBTRACT);
			break;

		case 3:
			p.p.blendMode(PApplet.DARKEST);
			break;

		case 4:
			p.p.blendMode(PApplet.LIGHTEST);
			break;

		case 5:
			p.p.blendMode(PApplet.DIFFERENCE);
			break;

		case 6:
			p.p.blendMode(PApplet.EXCLUSION);
			break;

		case 7:
			p.p.blendMode(PApplet.MULTIPLY);
			break;

		case 8:
			p.p.blendMode(PApplet.SCREEN);
			break;

		case 9:
			p.p.blendMode(PApplet.REPLACE);
			break;

		case 10:
			// blend(HARD_LIGHT);
			break;

		case 11:
			// blend(SOFT_LIGHT);
			break;

		case 12:
			// blend(OVERLAY);
			break;

		case 13:
			// blend(DODGE);
			break;

		case 14:
			// blend(BURN);
			break;
		}

		if (p.p.debug.field)
			PApplet.println("blendMode:" + blendMode);
	}
	
	/**
	 * Show statistics of the current simulation
	 */
	void displayInfo()
	{
		startHUD();
		p.p.pushMatrix();
		
		float xPos = centerTextXOffset;
		float yPos = topTextYOffset;			// Starting vertical position
		
		WMV_Field f = p.getCurrentField();
		
		if(p.viewer.getCurrentClusterID() >= 0)
		{
			WMV_Cluster c = p.getCurrentCluster();
//			float[] camTar = p.viewer.camera.target();

			p.p.fill(0, 0, 255, 255);
			p.p.textSize(largeTextSize);
			p.p.text(" MultimediaLocator v0.9 ", xPos, yPos, hudDistance);
			p.p.textSize(mediumTextSize);

			xPos = midLeftTextXOffset;
			
			p.p.text(" Program Modes ", xPos, yPos += lineWidthVeryWide, hudDistance);
			p.p.textSize(smallTextSize);
			p.p.text(" Orientation Mode: "+p.viewer.settings.orientationMode, xPos, yPos += lineWidthVeryWide, hudDistance);
			p.p.text(" Alpha Mode:"+p.alphaMode, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Time Fading: "+ p.timeFading, xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Date Fading: "+ p.dateFading, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Altitude Scaling: "+p.settings.altitudeScaling, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Lock Media to Clusters:"+p.lockMediaToClusters, xPos, yPos += lineWidth, hudDistance);
		
			p.p.textSize(mediumTextSize);
			p.p.text(" Graphics ", xPos, yPos += lineWidthVeryWide, hudDistance);
			p.p.textSize(smallTextSize);
			p.p.text(" Alpha:"+p.alpha, xPos, yPos += lineWidthVeryWide, hudDistance);
			p.p.text(" Default Media Length:"+p.settings.defaultMediaLength, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Media Angle Fading: "+p.viewer.settings.angleFading, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Media Angle Thinning: "+p.viewer.settings.angleThinning, xPos, yPos += lineWidth, hudDistance);
			if(p.viewer.settings.angleThinning)
				p.p.text(" Media Thinning Angle:"+p.viewer.settings.thinningAngle, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Image Size Factor:"+p.settings.subjectSizeRatio, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Subject Distance (m.):"+p.settings.defaultFocusDistance, xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Image Size Factor:"+p.subjectSizeRatio, xPos, yPos += lineWidth, hudDistance);

			xPos = centerTextXOffset;
			yPos = topTextYOffset;			// Starting vertical position

			p.p.textSize(mediumTextSize);
			p.p.text(" Field", xPos, yPos += lineWidthVeryWide, hudDistance);
			p.p.textSize(smallTextSize);
			p.p.text(" Name: "+f.name, xPos, yPos += lineWidthVeryWide, hudDistance);
			p.p.text(" ID: "+(p.viewer.getField()+1)+" out of "+p.getFieldCount()+" Total Fields", xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Width (m.): "+f.model.fieldWidth+" Length (m.): "+f.model.fieldLength+" Height (m.): "+f.model.fieldHeight, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Total Media: "+f.getMediaCount(), xPos, yPos += lineWidth, hudDistance);					// Doesn't check for dataMissing!!
			p.p.text(" Total Images: "+f.getImageCount(), xPos, yPos += lineWidth, hudDistance);					// Doesn't check for dataMissing!!
			p.p.text(" Total Panoramas: "+f.getPanoramaCount(), xPos, yPos += lineWidth, hudDistance);			// Doesn't check for dataMissing!!
			p.p.text(" Total Videos: "+f.getVideoCount(), xPos, yPos += lineWidth, hudDistance);					// Doesn't check for dataMissing!!
			p.p.text(" Total Sounds: "+f.getSoundCount(), xPos, yPos += lineWidth, hudDistance);					// Doesn't check for dataMissing!!
			p.p.text(" Media Density per sq. m.: "+f.model.mediaDensity, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Images Visible: "+f.imagesVisible, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Panoramas Visible: "+f.panoramasVisible, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Videos Visible: "+f.videosVisible, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Videos Playing: "+f.videosPlaying, xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Sounds Audible: "+f.soundsAudible, xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Sounds Playing: "+f.soundsPlaying, xPos, yPos += lineWidth, hudDistance);
			if(p.viewer.settings.orientationMode)
				p.p.text(" Clusters Visible: "+p.viewer.clustersVisible+"  (Orientation Mode)", xPos, yPos += lineWidth, hudDistance);

			p.p.textSize(mediumTextSize);
			p.p.text(" Model ", xPos, yPos += lineWidthVeryWide, hudDistance);
			p.p.textSize(smallTextSize);
			
			p.p.text(" Clusters:"+(f.clusters.size()-f.model.mergedClusters), xPos, yPos += lineWidthVeryWide, hudDistance);
			p.p.text(" Merged: "+f.model.mergedClusters+" out of "+f.clusters.size()+" Total", xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Minimum Distance: "+p.settings.minClusterDistance, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Maximum Distance: "+p.settings.maxClusterDistance, xPos, yPos += lineWidth, hudDistance);
			if(p.settings.altitudeScaling)
				p.p.text(" Altitude Scaling Factor: "+p.settings.altitudeScalingFactor+"  (Altitude Scaling)", xPos, yPos += lineWidthVeryWide, hudDistance);
			p.p.text(" Clustering Method : "+ ( p.hierarchical ? "Hierarchical" : "K-Means" ), xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Population Factor: "+f.model.clusterPopulationFactor, xPos, yPos += lineWidth, hudDistance);
			if(p.hierarchical) p.p.text(" Current Cluster Depth: "+f.model.clusterDepth, xPos, yPos += lineWidth, hudDistance);

			p.p.textSize(mediumTextSize);
			p.p.text(" Viewer ", xPos, yPos += lineWidthVeryWide, hudDistance);
			p.p.textSize(smallTextSize);
			p.p.text(" Location, x: "+PApplet.round(p.viewer.getLocation().x)+" y:"+PApplet.round(p.viewer.getLocation().y)+" z:"+
					 PApplet.round(p.viewer.getLocation().z), xPos, yPos += lineWidthVeryWide, hudDistance);		
			p.p.text(" GPS Longitude: "+p.viewer.getGPSLocation().x+" Latitude:"+p.viewer.getGPSLocation().y, xPos, yPos += lineWidth, hudDistance);		

			p.p.text(" Current Cluster: "+p.viewer.getCurrentClusterID(), xPos, yPos += lineWidthVeryWide, hudDistance);
			p.p.text("   Media Points: "+c.mediaCount, xPos, yPos += lineWidth, hudDistance);
			p.p.text("   Media Segments: "+p.getCurrentCluster().segments.size(), xPos, yPos += lineWidth, hudDistance);
			p.p.text("   Distance: "+PApplet.round(PVector.dist(c.getLocation(), p.viewer.getLocation())), xPos, yPos += lineWidth, hudDistance);
			p.p.text("   Auto Stitched Panoramas: "+p.getCurrentCluster().stitchedPanoramas.size(), xPos, yPos += lineWidth, hudDistance);
			p.p.text("   User Stitched Panoramas: "+p.getCurrentCluster().userPanoramas.size(), xPos, yPos += lineWidth, hudDistance);
			if(p.viewer.getAttractorCluster() != -1)
			{
				p.p.text(" Destination Cluster : "+p.viewer.getAttractorCluster(), xPos, yPos += lineWidth, hudDistance);
				p.p.text(" Destination Media Points: "+p.getCluster(p.viewer.getAttractorCluster()).mediaCount, xPos, yPos += lineWidth, hudDistance);
				p.p.text("    Destination Distance: "+PApplet.round( PVector.dist(f.clusters.get(p.viewer.getAttractorCluster()).getLocation(), p.viewer.getLocation() )), xPos, yPos += lineWidth, hudDistance);
			}

			if(p.p.debug.viewer) 
			{
				p.p.text(" Debug: Current Attraction: "+p.viewer.attraction.mag(), xPos, yPos += lineWidth, hudDistance);
				p.p.text(" Debug: Current Acceleration: "+(p.viewer.isWalking() ? p.viewer.walkingAcceleration.mag() : p.viewer.acceleration.mag()), xPos, yPos += lineWidth, hudDistance);
				p.p.text(" Debug: Current Velocity: "+ (p.viewer.isWalking() ? p.viewer.walkingVelocity.mag() : p.viewer.velocity.mag()) , xPos, yPos += lineWidth, hudDistance);
				p.p.text(" Debug: Moving? " + p.viewer.isMoving(), xPos, yPos += lineWidth, hudDistance);
				p.p.text(" Debug: Slowing? " + p.viewer.isSlowing(), xPos, yPos += lineWidth, hudDistance);
				p.p.text(" Debug: Halting? " + p.viewer.isHalting(), xPos, yPos += lineWidth, hudDistance);
			}

			if(p.p.debug.viewer)
			{
				p.p.text(" Debug: X Orientation (Yaw):" + p.viewer.getXOrientation(), xPos, yPos += lineWidth, hudDistance);
				p.p.text(" Debug: Y Orientation (Pitch):" + p.viewer.getYOrientation(), xPos, yPos += lineWidth, hudDistance);
//				p.p.text(" Debug: Target Point x:" + camTar[0] + ", y:" + camTar[1] + ", z:" + camTar[2], xPos, yPos += lineWidth, hudDistance);
			}
			else
			{
				p.p.text(" Compass Direction:" + p.p.utilities.angleToCompass(p.viewer.getXOrientation())+" Angle: "+p.viewer.getXOrientation(), xPos, yPos += lineWidth, hudDistance);
				p.p.text(" Vertical Direction:" + PApplet.degrees(p.viewer.getYOrientation()), xPos, yPos += lineWidth, hudDistance);
				p.p.text(" Zoom:"+p.viewer.getFieldOfView(), xPos, yPos += lineWidth, hudDistance);
			}
			p.p.text(" Field of View:"+p.viewer.getFieldOfView(), xPos, yPos += lineWidth, hudDistance);

			xPos = midRightTextXOffset;
			yPos = topTextYOffset;			// Starting vertical position

			p.p.textSize(mediumTextSize);
			p.p.text(" Time ", xPos, yPos += lineWidthVeryWide, hudDistance);
			p.p.textSize(smallTextSize);
			p.p.text(" Time Mode: "+ ((p.p.world.getTimeMode() == 0) ? "Cluster" : "Field"), xPos, yPos += lineWidthVeryWide, hudDistance);
			
			if(p.p.world.getTimeMode() == 0)
				p.p.text(" Current Field Time: "+ p.currentTime, xPos, yPos += lineWidth, hudDistance);
			if(p.p.world.getTimeMode() == 1)
				p.p.text(" Current Cluster Time: "+ p.getCurrentCluster().currentTime, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Current Field Timeline Segments: "+ p.getCurrentField().timeline.size(), xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Current Field Time Segment: "+ p.viewer.getCurrentFieldTimeSegment(), xPos, yPos += lineWidth, hudDistance);
			if(f.timeline.size() > 0 && p.viewer.getCurrentFieldTimeSegment() >= 0 && p.viewer.getCurrentFieldTimeSegment() < f.timeline.size())
				p.p.text(" Upper: "+f.timeline.get(p.viewer.getCurrentFieldTimeSegment()).getUpper().getTime()
						+" Center:"+f.timeline.get(p.viewer.getCurrentFieldTimeSegment()).getCenter().getTime()+
						" Lower: "+f.timeline.get(p.viewer.getCurrentFieldTimeSegment()).getLower().getTime(), xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Current Cluster Timeline Segments: "+ p.getCurrentCluster().timeline.size(), xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Field Dateline Segments: "+ p.getCurrentField().dateline.size(), xPos, yPos += lineWidth, hudDistance);
			p.p.textSize(mediumTextSize);

			if(p.p.debug.memory)
			{
				if(p.p.debug.detailed)
				{
					p.p.text("Total memory (bytes): " + p.p.debug.totalMemory, xPos, yPos += lineWidth, hudDistance);
					p.p.text("Available processors (cores): "+p.p.debug.availableProcessors, xPos, yPos += lineWidth, hudDistance);
					p.p.text("Maximum memory (bytes): " +  (p.p.debug.maxMemory == Long.MAX_VALUE ? "no limit" : p.p.debug.maxMemory), xPos, yPos += lineWidth, hudDistance); 
					p.p.text("Total memory (bytes): " + p.p.debug.totalMemory, xPos, yPos += lineWidth, hudDistance);
					p.p.text("Allocated memory (bytes): " + p.p.debug.allocatedMemory, xPos, yPos += lineWidth, hudDistance);
				}
				p.p.text("Free memory (bytes): "+p.p.debug.freeMemory, xPos, yPos += lineWidth, hudDistance);
				p.p.text("Approx. usable free memory (bytes): " + p.p.debug.approxUsableFreeMemory, xPos, yPos += lineWidth, hudDistance);
			}			
		}
		else
			message("Can't display statistics: currentCluster == "+p.viewer.getCurrentClusterID()+"!!!");
		
		p.p.popMatrix();
	}

	/**
	 * Draw cluster statistics display
	 */
	void displayCluster()
	{
		startHUD();
		p.p.pushMatrix();
		
		float xPos = centerTextXOffset;
		float yPos = topTextYOffset;			// Starting vertical position
		
		WMV_Field f = p.getCurrentField();
		WMV_Cluster c = p.getCluster(displayCluster);	// Get the cluster to display info about

		p.p.fill(0, 0, 255, 255);

		p.p.textSize(veryLargeTextSize);
		p.p.text(""+p.getCurrentField().name, xPos, yPos, hudDistance);
//		p.p.text(" Cluster View", textXPos, textYPos, hudDistance);

		p.p.textSize(largeTextSize);
		WMV_Cluster cl = p.getCurrentCluster();
		p.p.text(" Cluster #"+ c.getID() + ((c.getID() == cl.getID())?" (Current Cluster)":""), xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(mediumTextSize);
		p.p.text("   Media Count: "+ c.mediaCount, xPos, yPos += lineWidthVeryWide, hudDistance);
		if(c.images.size() > 0)
			p.p.text("     Images: "+ c.images.size(), xPos, yPos += lineWidthVeryWide, hudDistance);
		if(c.panoramas.size() > 0)
			p.p.text("     Panoramas: "+ c.panoramas.size(), xPos, yPos += lineWidthVeryWide, hudDistance);
		if(c.videos.size() > 0)
			p.p.text("     Videos: "+ c.videos.size(), xPos, yPos += lineWidthVeryWide, hudDistance);
//		if(c.sounds.size() > 0)
//			p.p.text("     Sounds: "+ c.sounds.size(), textXPos, textYPos += lineWidthVeryWide, hudDistance);
//		p.p.text("     Active: "+ c.isActive(), textXPos, textYPos += lineWidth, hudDistance);
//		p.p.text("     Single: "+ c.isSingle(), textXPos, textYPos += lineWidth, hudDistance);
//		p.p.text("     Empty: "+ c.isEmpty(), textXPos, textYPos += lineWidth, hudDistance);
		p.p.text("   Location: "+ c.getLocation(), xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.text("   Viewer Distance: "+PApplet.round(PVector.dist(c.getLocation(), p.viewer.getLocation())), xPos, yPos += lineWidth, hudDistance);
		p.p.text(" ", xPos, yPos += lineWidth, hudDistance);
		p.p.text("   Media Segments: "+ c.segments.size(), xPos, yPos += lineWidth, hudDistance);
		
		if(c.timeline.size() > 0)
		{
			p.p.text(" Timeline Segments: "+ c.timeline.size(), xPos, yPos += lineWidthWide, hudDistance);
			p.p.text(" Timeline Length (sec.): "+ p.p.utilities.getTimelineLength(c.timeline), xPos, yPos += lineWidth, hudDistance);
		}
		if(c.dateline != null)
			if(c.dateline.size() > 0)
				p.p.text(" Timeline Dates: "+ c.dateline.size(), xPos, yPos += lineWidth, hudDistance);

		if(p.getCurrentCluster() != null)
		{
			p.p.text("   Auto Stitched Panoramas: "+p.getCurrentCluster().stitchedPanoramas.size(), xPos, yPos += lineWidth, hudDistance);
			p.p.text("   User Stitched Panoramas: "+p.getCurrentCluster().userPanoramas.size(), xPos, yPos += lineWidth, hudDistance);

//			p.p.text(" Current Cluster ID: "+p.viewer.getCurrentClusterID(), xPos, yPos += lineWidthVeryWide, hudDistance);
//			p.p.text("   Media Count: "+cl.mediaCount, xPos, yPos += lineWidth, hudDistance);
//			p.p.text("   Viewer Distance: "+PApplet.round(PVector.dist(cl.getLocation(), p.viewer.getLocation())), xPos, yPos += lineWidth, hudDistance);
		}
		
		if(p.p.debug.field || p.p.debug.main)
		{
			if(c != null)
			{
				p.p.textSize(largeTextSize);
				p.p.text(" Current Cluster #"+ c.getID()+" of "+ f.clusters.size(), xPos, yPos += lineWidthWide, hudDistance);
				p.p.textSize(mediumTextSize);
				if(c.dateline != null)
				{
					if(c.dateline.size() > 0)
					{
						int clusterDate = p.getCurrentField().timeline.get(p.viewer.getCurrentFieldTimeSegment()).getClusterDateID();
						p.p.text(" Current Cluster Time Segment", xPos, yPos += lineWidthWide, hudDistance);
						p.p.text("   ID: "+ p.getCurrentField().timeline.get(p.viewer.getCurrentFieldTimeSegment()).getClusterTimelineID()+"  of "+ c.timeline.size() +" in Cluster Main Timeline", xPos, yPos += lineWidthWide, hudDistance);
						p.p.text("   Date: "+ (clusterDate+1) +" of "+ c.dateline.size(), xPos, yPos += lineWidth, hudDistance);
						if(c.timelines.size() > clusterDate)
							p.p.text("  Date-Specific ID: "+ p.getCurrentField().timeline.get(p.viewer.getCurrentFieldTimeSegment()).getClusterTimelineIDOnDate()+"  of "+ c.timelines.get(clusterDate).size() + " in Cluster Timeline #"+clusterDate, xPos, yPos += lineWidth, hudDistance);
						else
							p.p.text("ERROR: No Cluster Timeline for Current Cluster Date ("+clusterDate+")", xPos, yPos += lineWidth, hudDistance);
					}
				}
			}		
			
			p.p.text(" Field Cluster Count:"+(f.clusters.size()), xPos, yPos += lineWidthVeryWide, hudDistance);
			p.p.text("   Merged: "+f.model.mergedClusters+" out of "+(f.model.mergedClusters+f.clusters.size())+" Total", xPos, yPos += lineWidth, hudDistance);
			if(p.hierarchical) p.p.text(" Current Cluster Depth: "+f.model.clusterDepth, xPos, yPos += lineWidth, hudDistance);
			p.p.text("   Minimum Distance: "+p.settings.minClusterDistance, xPos, yPos += lineWidth, hudDistance);
			p.p.text("   Maximum Distance: "+p.settings.maxClusterDistance, xPos, yPos += lineWidth, hudDistance);
			p.p.text("   Population Factor: "+f.model.clusterPopulationFactor, xPos, yPos += lineWidth, hudDistance);
			
			if(f.dateline != null)
			{
				p.p.textSize(largeTextSize);
				p.p.text(" Current Field #"+ f.fieldID+" of "+ p.getFields().size(), xPos, yPos += lineWidthVeryWide, hudDistance);
				p.p.textSize(mediumTextSize);
				if(f.dateline.size() > 0)
				{
//					p.p.text(" Clusters: "+ f.clusters.size()+"  Media: "+ f.getMediaCount(), xPos, yPos += lineWidth, hudDistance);
					int fieldDate = p.getCurrentField().timeline.get(p.viewer.getCurrentFieldTimeSegment()).getFieldDateID();
					p.p.text(" Current Time Segment", xPos, yPos += lineWidthWide, hudDistance);
					p.p.text("   ID: "+ p.viewer.getCurrentFieldTimeSegment()+" of "+ p.getCurrentField().timeline.size() +" in Main Timeline", xPos, yPos += lineWidthWide, hudDistance);
					p.p.text("   Date: "+ (fieldDate)+" of "+ p.getCurrentField().dateline.size(), xPos, yPos += lineWidth, hudDistance);
					p.p.text("   Date-Specific ID: "+ p.getCurrentField().timeline.get(p.viewer.getCurrentFieldTimeSegment()).getFieldTimelineIDOnDate()
							+" of "+ p.getCurrentField().timelines.get(fieldDate).size() + " in Timeline #"+(fieldDate), xPos, yPos += lineWidth, hudDistance);
				}
			}
		}
		
		if(p.viewer.getAttractorCluster() != -1)
		{
			p.p.text(" Destination Cluster ID: "+p.viewer.getAttractorCluster(), xPos, yPos += lineWidth, hudDistance);
			p.p.text("    Destination Distance: "+PApplet.round( PVector.dist(f.clusters.get(p.viewer.getAttractorCluster()).getLocation(), p.viewer.getLocation() )), xPos, yPos += lineWidth, hudDistance);
			if(p.p.debug.viewer) 
			{
				p.p.text(" Debug: Current Attraction:"+p.viewer.attraction.mag(), xPos, yPos += lineWidth, hudDistance);
				p.p.text(" Debug: Current Acceleration:"+(p.viewer.isWalking() ? p.viewer.walkingAcceleration.mag() : p.viewer.acceleration.mag()), xPos, yPos += lineWidth, hudDistance);
				p.p.text(" Debug: Current Velocity:"+ (p.viewer.isWalking() ? p.viewer.walkingVelocity.mag() : p.viewer.velocity.mag()) , xPos, yPos += lineWidth, hudDistance);
			}
		}

		p.p.popMatrix();
		
		drawClusterImages(c);
	}

	/**
	 * Draw thumbnails in grid of images in cluster
	 * @param cluster Cluster to preview
	 */
	private void drawClusterImages(WMV_Cluster cluster)
	{
		int count = 1;
		float imgXPos = clusterImageXOffset;
		float imgYPos = clusterImageYOffset;			// Starting vertical position

		p.p.stroke(255, 255, 255);
		p.p.strokeWeight(15);
		p.p.fill(0, 0, 255, 255);

		for(WMV_Image i : cluster.getImages())
		{
			p.p.pushMatrix();
			float origWidth = i.getWidth();
			float origHeight = i.getHeight();
			float width = 120.f;
			float height = width * origHeight / origWidth;
			
			p.p.translate(imgXPos, imgYPos, hudDistance);
			p.p.tint(255);
			
			if(count < 60)
			{
				PImage image = p.p.loadImage(i.filePath);
				p.p.image(image, 0, 0, width, height);
			}
			
			imgXPos += width * 1.5f;

			if(count % 20 == 0)
			{
				imgXPos = clusterImageXOffset;
				imgYPos += height * 1.5f;
			}
			
			p.p.popMatrix();
			count++;
		}
	}
	
	public boolean inDisplayView()
	{
		if( displayView != 0 )
			return true;
		else 
			return false;
	}
	
	public void setDisplayView(int newDisplayView)
	{
		switch(newDisplayView)
		{
			case 0:	
				displayView = 0;
				window.optSceneView.setSelected(true);
				window.optMapView.setSelected(false);
				window.optClusterView.setSelected(false);
				break;
			case 1:	
				displayView = 1;
				if(!initializedMaps) map2D.initializeMaps();
				window.optSceneView.setSelected(false);
				window.optMapView.setSelected(true);
				window.optClusterView.setSelected(false);
				break;
			case 2:	
				displayView = 2;
				window.optSceneView.setSelected(false);
				window.optMapView.setSelected(false);
				window.optClusterView.setSelected(true);
				displayCluster = p.viewer.getCurrentClusterID();
				break;
			case 3:	
				displayView = 3;
//				window.optSceneView.setSelected(false);
//				window.optMapView.setSelected(false);
//				window.optClusterView.setSelected(true);
				break;
		}
	}

	private class SelectableTimeSegment
	{
		private int id, fieldTimelineID, clusterID;
		private PVector location;
		public float leftEdge, rightEdge, topEdge, bottomEdge;
		WMV_TimeSegment segment;
		
		SelectableTimeSegment(int newID, int newFieldTimelineID, int newClusterID, WMV_TimeSegment newSegment, PVector newLocation, float newLeftEdge, float newRightEdge, float newTopEdge, float newBottomEdge)
		{
			id = newID;
			fieldTimelineID = newFieldTimelineID;
			clusterID = newClusterID;
			segment = newSegment;
			
			location = newLocation;
			leftEdge = newLeftEdge;
			rightEdge = newRightEdge;
			topEdge = newTopEdge;
			bottomEdge = newBottomEdge;
		}
		
		public int getID()
		{
			return id;
		}
		
//		public int getTimeSegmentID()
//		{
//			return fieldTimelineID;
//		}
		
		public int getClusterID()
		{
			return clusterID;
		}
		
//		public PVector getLocation()
//		{
//			return location;
//		}
		
		public void draw(float hue, float saturation, float brightness, boolean preview)
		{
			p.p.stroke(hue, saturation, brightness, 255);												// Yellow rectangle around selected time segment
			p.p.strokeWeight(3.f);

			p.p.pushMatrix();
			p.p.line(leftEdge, topEdge, hudDistance, leftEdge, bottomEdge, hudDistance);	
			p.p.line(rightEdge, topEdge, hudDistance, rightEdge, bottomEdge, hudDistance);			
			p.p.line(leftEdge, topEdge, hudDistance, rightEdge, topEdge, hudDistance);			
			p.p.line(leftEdge, bottomEdge, hudDistance, rightEdge, bottomEdge, hudDistance);			

			if(preview)
			{
				p.p.fill(hue, saturation, brightness, 255);												// Yellow rectangle around selected time segment
				p.p.textSize(smallTextSize);
				String strTimespan = segment.getTimespanAsString(false, false);
				String strPreview = String.valueOf( segment.timeline.size() ) + " media, "+strTimespan;

				float length = timelineEnd - timelineStart;
				float day = p.p.utilities.getTimePVectorSeconds(new PVector(24,0,0));		// Seconds in a day
				float xOffset = -35.f * PApplet.map(length, 0.f, day, 0.2f, 1.f);
				p.p.text(strPreview, (rightEdge+leftEdge)/2.f + xOffset, bottomEdge + 25.f, hudDistance);
			}
			p.p.popMatrix();
		}
	}

	private class SelectableDate
	{
		private int id;
		private PVector location;
		public float radius;
		WMV_Date date;

		SelectableDate(int newID, PVector newLocation, float newRadius, WMV_Date newDate)
		{
			id = newID;
//			dateID = newDateID;
			location = newLocation;
			radius = newRadius;
			date = newDate;
		}
		
		public int getID()
		{
			return id;
		}
		
//		public int getDateID()
//		{
//			return dateID;
//		}
		
		public PVector getLocation()
		{
			return location;
		}
		
		public void draw(float hue, float saturation, float brightness, boolean preview)
		{
			p.p.pushMatrix();
			
			p.p.stroke(hue, saturation, brightness, 255.f);
			p.p.strokeWeight(25.f);
			p.p.point(location.x, location.y, location.z);

			if(preview)
			{
				p.p.fill(hue, saturation, brightness, 255);												// Yellow rectangle around selected time segment
				p.p.textSize(smallTextSize);
				String strDate = date.getDateAsString();
//				String strPreview = String.valueOf( segment.timeline.size() ) + " media, "+strTimespan;
				p.p.text(strDate, location.x - 25.f, location.y + 50.f, location.z);
			}
		
			p.p.popMatrix();
		}
	}

	public void showAllDates()
	{
		displayDate = -1;
		selectedDate = -1;
		currentSelectableDate = -1;
		updateTimeline = true;
	}
	
	public int getSelectedCluster()
	{
		return selectedCluster;	
	}
	
	public int getCurrentSelectableTime()
	{
		return currentSelectableTimeID;
	}
	
//	void setFullScreen(boolean newState)
//	{
//		if(newState && !fullscreen)			// Switch to Fullscreen
//		{
////			if(!p.viewer.selection) window.viewsSidebar.setVisible(false);	
////			else window.selectionSidebar.setVisible(false);
//		}
//		if(!newState && fullscreen)			// Switch to Window Size
//		{
////			if(!p.viewer.selection) window.mainSidebar.setVisible(true);	
////			else window.selectionSidebar.setVisible(true);
//		}
//		
//		fullscreen = newState;
//	}
}

