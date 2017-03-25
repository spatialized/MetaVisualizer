package wmViewer;
import java.util.ArrayList;

import g4p_controls.GButton;
import processing.core.*;

/***********************************
 * @author davidgordon
 * Class for displaying 2D text and graphics
 */

class WMV_Display
{
	/* Classes */
	public WMV_Window window;							// Main interaction window
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
	public boolean updateCurrentSelectableTime = true;
	private final float timeTextSize = 44.f;
	
	private ArrayList<SelectableTimeSegment> selectableTimes;		// Selectable time segments on timeline
	private ArrayList<SelectableDate> selectableDates;		// Selectable dates on dateline

	private boolean selectableTimesCreated = false, datelineCreated = false, updateTimeline = true;
	private float timelineXOffset = 0.f, timelineYOffset = 0.f;
	private float datelineXOffset = 0.f, datelineYOffset = 0.f;
	private int selectedTime = -1, selectedCluster = -1, currentSelectableTime = -1;
	private int selectedDate = -1, currentSelectableDate = -1;
		
	/* Text */
	float centerTextXOffset, topTextYOffset;
	float userMessageXOffset, userMessageYOffset, startupMessageXOffset;
	float leftTextXOffset, rightTextXOffset, metadataYOffset, startupMessageYOffset;
	float midLeftTextXOffset, midRightTextXOffset;
	float clusterImageXOffset, clusterImageYOffset;

	final float veryLargeTextSize = 64.f;
	final float largeTextSize = 56.f;
	final float mediumTextSize = 44.f;
	final float smallTextSize = 32.f;
	final float linePadding = 20.f;
	final float lineWidth = smallTextSize + linePadding;			
	final float lineWidthWide = largeTextSize + linePadding;			
	final float lineWidthVeryWide = largeTextSize * 2.f;			

	WMV_World p;

	WMV_Display(WMV_World parent)
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
		clusterImageXOffset = -p.p.width/ 1.66f;
		clusterImageYOffset = p.p.height / 2.f;

		userMessageXOffset = -p.p.width / 2.f;
		userMessageYOffset = 0;

		metadataYOffset = -p.p.height / 2.f;

		startupMessageXOffset = p.p.width / 2.f;
		startupMessageYOffset = -p.p.height /2.f;

		timelineScreenSize = p.p.width * 2.2f;
		timelineStart = 0.f;
		timelineEnd = p.p.utilities.getTimePVectorSeconds(new PVector(24,0,0));

		timelineXOffset = -p.p.width/ 1.66f;
		timelineYOffset = -p.p.height/ 2.f;
		timelineYOffset = 0.f;
		datelineXOffset = timelineXOffset;
		datelineYOffset = p.p.height * 0.2f;
		
		map2D = new WMV_Map(this);
		
//		startupImage = p.p.loadImage("res/WMV_Title.jpg");
	}

	void setupWMVWindow()
	{
		window = new WMV_Window(this);				// Setup and display interaction window
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
			if(!p.p.basic)
			{
				if(window.setupGraphicsWindow)
				{
					if(window.graphicsWindow.isVisible())
					{
						if(p.timeFading)
						{
							if(p.getTimeMode() == 0)			// Need to fix cluster date / time structures first!
							{
//								WMV_Cluster curCluster = p.getCurrentCluster();
//								int firstTimeID = curCluster.getFirstTimeSegment();
//								int dateCount = 1;
//
//								if(curCluster.dateline != null)
//									dateCount = curCluster.dateline.size();
//
//								PApplet.println("curCluster.lowDate:"+curCluster.lowDate+" curCluster.highDate:"+curCluster.highDate);
//								float fTime = (float) p.getCurrentCluster().currentTime / (float) p.getCurrentCluster().timeCycleLength;
//								PApplet.println("p.getCurrentCluster().currentTime:"+p.getCurrentCluster().currentTime+" p.getCurrentCluster().timeCycleLength: "+p.getCurrentCluster().timeCycleLength);
//								float fHour = fTime * 24.f;
//								int hour = (int)(fHour);
//								int min = PApplet.round((fHour - hour) * 60);
//								window.lblCurrentTime.setText(hour+":"+min);
//								PApplet.println("fHour:"+fHour+"  fHour - hour:"+(fHour - hour));
//								PApplet.println("fTime:"+fTime+" Time = "+hour+":"+min);
							}
							else
							{
								/* Time display in Time Window */
//								float fTime = (float) p.currentTime / (float) p.settings.timeCycleLength;
//								float fHour = fTime * 24.f;
//								int hour = (int)(fHour);
//								int min = PApplet.round((fHour - hour) * 60);
//								window.lblCurrentTime.setText((hour==0?"00":hour)+":"+(min==0?"00":min));
							}
						}
					}
				}
			}

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

		p.p.textSize(mediumTextSize);
		p.p.text(" Field #"+ f.fieldID+"  of "+ p.getFields().size(), xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.text(" Number of Clusters: "+ f.clusters.size(), xPos, yPos += lineWidthWide, hudDistance);
		p.p.text(" Number of Media: "+ f.getMediaCount(), xPos, yPos += lineWidth, hudDistance);
		
		if(f.dateline != null)
		{
			if(f.dateline.size() > 0)
			{
				int fieldDate = p.getCurrentField().timeline.get(p.viewer.getCurrentFieldTimeSegment()).getFieldDateID();
				p.p.text(" Current Field Date: "+ fieldDate+" of "+ p.getCurrentField().dateline.size(), xPos, yPos += lineWidthWide, hudDistance);
				if(displayDate == -1)
					p.p.text(" Current Field Time Segment: "+ p.viewer.getCurrentFieldTimeSegment()+"  of "+ p.getCurrentField().timeline.size() +" in Main Timeline", xPos, yPos += lineWidth, hudDistance);
				else
					p.p.text(" Current Field Time Segment: "+ p.getCurrentField().timeline.get(p.viewer.getCurrentFieldTimeSegment()).getClusterTimelinesID()+"  of "+ p.getCurrentField().timelines.get(fieldDate).size() + " in Timeline #"+fieldDate, xPos, yPos += lineWidth, hudDistance);
			}
		}

		p.p.text(" Displaying Date: "+ (displayDate == -1 ? "All" : displayDate), xPos, yPos += lineWidth, hudDistance);

		if(c != null)
		{
			p.p.textSize(mediumTextSize);
			yPos = timelineYOffset + timelineHeight * 2.f + lineWidthVeryWide;
			
			p.p.text(" Current Cluster: "+ c.getID()+" of "+ f.clusters.size(), xPos, yPos, hudDistance);

			if(c.dateline != null)
			{
				if(c.dateline.size() > 0)
				{
					int clusterDate = p.getCurrentField().timeline.get(p.viewer.getCurrentFieldTimeSegment()).getClusterDateID();
					p.p.text(" Current Cluster Date: "+ clusterDate +" of "+ c.dateline.size(), xPos, yPos += lineWidth, hudDistance);
					if(displayDate == -1)
						p.p.text(" Current Cluster Time Segment: "+ p.getCurrentField().timeline.get(p.viewer.getCurrentFieldTimeSegment()).getClusterTimelineID()+"  of "+ c.timeline.size() +" in Main Cluster Timeline", xPos, yPos += lineWidth, hudDistance);
					else
					{
						if(c.timelines.size() > clusterDate)
							p.p.text(" Current Cluster Time Segment: "+ p.getCurrentField().timeline.get(p.viewer.getCurrentFieldTimeSegment()).getClusterTimelinesID()+"  of "+ c.timelines.get(clusterDate).size() + " in Cluster Timeline #"+clusterDate, xPos, yPos += lineWidth, hudDistance);
						else
							p.p.text(" ERROR: No Cluster Timeline for Current Cluster Date ("+clusterDate+")", xPos, yPos += lineWidth, hudDistance);
					}
				}
			}
		}
		p.p.popMatrix();
		
		drawFieldDateline();
		drawFieldTimeline();
		updateTimelineMouse();
	}
	
	/**
	 * Update field timeline every frame
	 */
	private void updateFieldTimeline()
	{
		if(!datelineCreated || !selectableTimesCreated || updateTimeline)
		{
			createDateline();
			createSelectableTimes();
			updateTimeline = false;
		}

		if(updateCurrentSelectableTime)
		{
			if(p.viewer.getCurrentFieldTimeSegment() >= 0)
			{
				WMV_TimeSegment t = p.getCurrentField().timeline.get(p.viewer.getCurrentFieldTimeSegment());		// TESTING

				if(t != null)
					currentSelectableTime = getSelectableTimeIDOfTimeSegment(t);						// Set current selectable time (white rectangle) from current field time segment
				else
					currentSelectableTime = -1;

//				if(currentSelectableTime != -1)
//					PApplet.println(""+p.p.frameCount+": set currentSelectableTime to:"+currentSelectableTime);

				updateCurrentSelectableTime = false;
			}
			else PApplet.println("updateCurrentSelectableTime... No current time segment!");
		}
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
	
	private void createSelectableTimes()
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
		
		selectableTimesCreated = true;
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
	 * Create and return selectable time from timeline
	 * @param t Time segment
	 * @param id Time segment id
	 * @return SelectableTime object
	 */
	private SelectableTimeSegment getSelectableTime(WMV_TimeSegment t, int id)
	{
		float lowerSeconds = p.p.utilities.getTimePVectorSeconds(t.getLower().getTimeAsPVector());
		float upperSeconds = p.p.utilities.getTimePVectorSeconds(t.getUpper().getTimeAsPVector());
//		float centerSeconds = p.p.utilities.getTimePVectorSeconds(t.getCenter().getTimeAsPVector());

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

			SelectableDate st = new SelectableDate(id, d.getID(), loc, radius);		// int newID, int newClusterID, PVector newLocation, Box newRectangle
			return st;
		}
		else return null;
	}	

	private void drawFieldTimeline()
	{
		WMV_Field f = p.getCurrentField();
			
		p.p.tint(255);
		p.p.stroke(0.f, 0.f, 255.f, 255.f);
		p.p.strokeWeight(3.f);
		p.p.fill(0.f, 0.f, 255.f, 255.f);
		p.p.line(timelineXOffset, timelineYOffset, hudDistance, timelineXOffset + timelineScreenSize, timelineYOffset, hudDistance);

		String startTime, endTime;
		int hour = PApplet.round(timelineStart) / 3600;
		int minute = (PApplet.round(timelineStart) % 3600) / 60;
		int second = (PApplet.round(timelineStart) % 3600) % 60;

		String strHour = String.valueOf(hour);
		String strMinute = String.valueOf(minute);
		if(minute < 10) strMinute = "0"+strMinute;
		String strSecond = String.valueOf(second);
		if(second < 10) strSecond = "0"+strSecond;
		startTime = strHour + ":" + strMinute + ":" + strSecond;

		hour = PApplet.round(timelineEnd) / 3600;
		minute = (PApplet.round(timelineEnd) % 3600) / 60;
		second = (PApplet.round(timelineEnd) % 3600) % 60;
		strHour = String.valueOf(hour);
		strMinute = String.valueOf(minute);
		if(minute < 10) strMinute = "0"+strMinute;
		strSecond = String.valueOf(second);
		if(second < 10) strSecond = "0"+strSecond;
		endTime = strHour + ":" + strMinute + ":" + strSecond;
		
		p.p.textSize(timeTextSize);
		p.p.text(startTime, timelineXOffset, timelineYOffset - timelineHeight * 0.5f - 30.f, hudDistance);
		p.p.text(endTime, timelineXOffset + timelineScreenSize - 40.f, timelineYOffset - timelineHeight * 0.5f - 30.f, hudDistance);
	
		if(f.dateline.size() == 1)
		{
			int count = 0;
			for(WMV_TimeSegment t : f.timeline)
			{
				drawTimelineSegment(t, count);
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
						drawTimelineSegment(t, count);
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
						drawTimelineSegment(t, count);
						count++;
					}
				}
				else
				{
					PApplet.println("--> displayDate > timelines.size()   displayDate:"+displayDate+" f.timelines.size():"+f.timelines.size());
				}
			}
		}
		
		if(selectedTime != -1 && selectableTimes.size() > 0 && selectedTime < selectableTimes.size())
			selectableTimes.get(selectedTime).draw(40.f, 255.f, 255.f);
		if(currentSelectableTime != -1 && selectableTimes.size() > 0 && currentSelectableTime < selectableTimes.size())
			selectableTimes.get(currentSelectableTime).draw(0.f, 0.f, 255.f);
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
			selectableDates.get(selectedDate).draw(40.f, 255.f, 255.f);
		if(currentSelectableDate != -1 && selectableDates.size() > 0 && currentSelectableDate < selectableDates.size())
			selectableDates.get(currentSelectableDate).draw(0.f, 0.f, 255.f);
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
		float year = d.getYear();
		float month = d.getMonth();
		float day = d.getDay();

		float xOffset = PApplet.map(date, datelineStart, datelineEnd, datelineXOffset, datelineXOffset + timelineScreenSize);
//		float xOffset2 = PApplet.map(upperSeconds, timelineStart, timelineEnd, timelineXOffset, timelineXOffset + timelineScreenSize);

		if(xOffset > datelineXOffset && xOffset < datelineXOffset + timelineScreenSize)
		{
//			p.p.pushMatrix();
			p.p.strokeWeight(0.f);

			p.p.fill(120.f, 165.f, 245.f, 155.f);
//			p.p.translate(xOffset, datelineYOffset, hudDistance);
//			p.p.sphereDetail(6);
//			p.p.sphere(25.f);
//			p.p.popMatrix();

			p.p.pushMatrix();
			p.p.stroke(120.f, 165.f, 245.f, 155.f);
			p.p.strokeWeight(25.f);
			p.p.point(xOffset, datelineYOffset, hudDistance);
//			PApplet.println("--> Date id:"+id+" month:"+month+" day:"+day+" year:"+year+"   xOffset:"+xOffset+" datelineYOffset:"+datelineYOffset+" datelineStart:"+datelineStart+" datelineEnd:"+datelineEnd);
			p.p.popMatrix();
		}
		else
		{
			PApplet.println("-----> Date out of view! date:"+date+" datelineStart:"+datelineStart+" datelineEnd:"+datelineEnd);
		}
	}
	
	/**
	 * Draw time segment on timeline
	 * @param t
	 * @param timelineLeftEdge
	 * @param timelineTopEdge
	 */
	private void drawTimelineSegment(WMV_TimeSegment t, int id)
	{
		PVector lowerTime = t.getLower().getTimeAsPVector();			// Format: PVector(hour, minute, second)
		PVector upperTime = t.getUpper().getTimeAsPVector();			
//		PVector centerTime = t.getCenter().getTimeAsPVector();			

		float lowerSeconds = p.p.utilities.getTimePVectorSeconds(lowerTime);
//		float centerSeconds = p.p.utilities.getTimePVectorSeconds(centerTime);
		float upperSeconds = p.p.utilities.getTimePVectorSeconds(upperTime);
//		float daySeconds = p.p.utilities.getTimePVectorSeconds(new PVector(24,0,0));
		
//		if(PApplet.abs(upperSeconds - lowerSeconds) > 1000.f)
//		{
//			PApplet.println("Possible ERROR: very long time segment for cluster:"+t.getClusterID()+ " result:"+PApplet.abs(upperSeconds - lowerSeconds));
//			PApplet.println(": lowerSeconds:"+lowerSeconds+" centerSeconds:"+centerSeconds+" upperSeconds:"+upperSeconds);
//		}

		ArrayList<WMV_Time> tsTimeline = t.getTimeline();				// Get timeline for this time segment
		ArrayList<PVector> times = new ArrayList<PVector>();
		for(WMV_Time ti : tsTimeline) times.add(ti.getTimeAsPVector());

		float xOffset = PApplet.map(lowerSeconds, timelineStart, timelineEnd, timelineXOffset, timelineXOffset + timelineScreenSize);
		float xOffset2 = PApplet.map(upperSeconds, timelineStart, timelineEnd, timelineXOffset, timelineXOffset + timelineScreenSize);

		if(xOffset > timelineXOffset && xOffset2 < timelineXOffset + timelineScreenSize)
		{
			p.p.pushMatrix();
			p.p.translate(0.f, timelineYOffset, hudDistance);
			p.p.stroke(140.f, 185.f, 255.f, 155.f);
			p.p.strokeWeight(1.f);

			for(PVector time : times)
			{
				float seconds = p.p.utilities.getTimePVectorSeconds(time);
				float xOff = PApplet.map(seconds, timelineStart, timelineEnd, timelineXOffset, timelineXOffset + timelineScreenSize);
				p.p.line(xOff, -timelineHeight / 2.f, 0.f, xOff, timelineHeight / 2.f, 0.f);
			}

			p.p.stroke(90, 215, 255, 255);												// White rectangle around time segment
			p.p.strokeWeight(2.f);

			p.p.line(xOffset, -timelineHeight * 0.5f, 0.f, xOffset, timelineHeight * 0.5f, 0.f);			
			p.p.line(xOffset2, -timelineHeight * 0.5f, 0.f, xOffset2, timelineHeight * 0.5f, 0.f);
			p.p.line(xOffset, -timelineHeight * 0.5f, 0.f, xOffset2, -timelineHeight * 0.5f, 0.f);
			p.p.line(xOffset, timelineHeight * 0.5f, 0.f, xOffset2, timelineHeight * 0.5f, 0.f);

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

//				if(p.p.debug.time)
//					PApplet.println("Selected time segment:"+selectedTime+" selectedCluster:"+selectedCluster);
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
//				selectedCluster = timeSelected.getClusterID();
						
//				if(p.p.debug.time) 
//					PApplet.println("Selected date:"+selectedDate);
				updateTimeline = true;				// Update timeline to show selected segment
			}
			else
				selectedDate = -1;
		}
	}
	
	public void zoomTimelineToFit()
	{
		WMV_Field f = p.getCurrentField();
		float first = f.timeline.get(0).getLower().getTime();
		float last = f.timeline.get(f.timeline.size()-1).getUpper().getTime();
		float day = p.p.utilities.getTimePVectorSeconds(new PVector(24,0,0));
		
		first *= day;
		last *= day;
		timelineStart = first - 10.f;
		timelineEnd = last + 10.f;
		
		updateTimeline = true;
//		PApplet.println("timelineStart:"+timelineStart+" timelineEnd:"+timelineEnd);
	}
	
	public void resetZoom()
	{
		timelineStart = 0.f;
		timelineEnd = p.p.utilities.getTimePVectorSeconds(new PVector(24,0,0));
		updateTimeline = true;
	}
	
	public void zoom(float amount)
	{
//		float result = p.display.timelineZoom * 1.052f;
		float length = timelineEnd - timelineStart;
		float newLength = length * amount;
		float result = timelineStart + newLength;
		
		if(result < p.p.utilities.getTimePVectorSeconds(new PVector(24,0,0)))
			timelineEnd = result;
		
		updateTimeline = true;
	}
	
	public void scroll(float amount)
	{
		float newStart = timelineStart + amount;
		float newEnd = timelineEnd + amount;
		float day = p.p.utilities.getTimePVectorSeconds(new PVector(24,0,0));
		
		if(newStart > 0.f && newEnd < day)
		{
			timelineStart = newStart;
			timelineEnd = newEnd;
		}

		updateTimeline = true;
	}
	
	public void handleMouseReleased(float mouseX, float mouseY)
	{
		updateTimelineMouse();
		if(selectedTime != -1)
		{
			if(selectedCluster != -1)
			{
				PApplet.println("Will teleport to selectedCluster:"+selectedCluster+" selectedTime:"+selectedTime+" new field time segment:"+selectableTimes.get(selectedTime).getID());
				PApplet.println(" selectedCluster:"+selectedCluster+" should == selectableTimes.get(selectedTime).getClusterID():"+selectableTimes.get(selectedTime).getClusterID());
				
				if(!p.input.shiftKey)
				{
					p.viewer.teleportToCluster(selectedCluster, true, -1); 
//					p.viewer.teleportToCluster(selectedCluster, true, selectableTimes.get(selectedTime).getTimeSegmentID());
					p.display.setDisplayView(0);
				}
				else
				{
//					p.viewer.teleportToCluster(selectedCluster, false, -1); 
//					p.viewer.teleportToCluster(selectedCluster, false, selectableTimes.get(selectedTime).getTimeSegmentID());
//					p.viewer.teleportToCluster(selectedCluster, false, selectableTimes.get(selectedTime).segment.getClusterTimelineID());
					
					p.viewer.teleportToCluster(selectedCluster, false, selectableTimes.get(selectedTime).segment.getFieldTimelineID());
				}
			}
		}
		
		if(selectedDate != -1)
		{
			displayDate = selectedDate;
			currentSelectableDate = displayDate;
			updateTimeline = true;
		}
		else
		{
//			currentSelectableDate = displayDate;
		}
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
		
		/* Sidebar Modes */
//		sidebarView = 0;					// Sidebar statistics view  0: Main  1:Statistics  2:Help  3:Selection

		/* Display Mode */
		displayView = 0;
//		map = false;					// Display map 
//		info = false;				// Display simulation info 
//		cluster = false;				// Display cluster statistics
//		control = false;				// Display controls only
//		about = false;				// Display about screen  -- need to implement
		
//		mapOverlay = false;			// Overlay map on 3D view
//		infoOverlay = false;			// Overlay simulation info on 3D view
//		clusterOverlay = false;		// Display cluster statistics over 3D view
//		controlOverlay = false;		// Display controls over 3D view
		
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
		currentSelectableTime = -1;
		selectedDate = -1; 
		currentSelectableDate = -1;

		timelineScreenSize = p.p.width * 2.2f;
		timelineStart = 0.f;
		timelineEnd = p.p.utilities.getTimePVectorSeconds(new PVector(24,0,0));
		datelineStart = 0.f;
		datelineEnd = 0.f;
		updateCurrentSelectableTime = true;

		timelineXOffset = -p.p.width/ 1.66f;
		timelineYOffset = -p.p.height/ 2.f;
		timelineYOffset = 0.f;
		datelineXOffset = timelineXOffset;
		datelineYOffset = p.p.height * 0.2f;

		selectableTimes = new ArrayList<SelectableTimeSegment>();
		selectableDates = new ArrayList<SelectableDate>();

		selectableTimesCreated = false;
		datelineCreated = false;
		updateTimeline = true;

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
		clusterImageXOffset = -p.p.width/ 1.66f;
		clusterImageYOffset = p.p.height / 2.f;

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
	 * Initialize 2D drawing 
	 */
	void start3DHUD()
	{
		p.p.perspective(p.viewer.getInitFieldOfView(), (float)p.p.width/(float)p.p.height, p.viewer.getNearClippingDistance(), 10000);
		PVector t = new PVector(p.viewer.camera.position()[0], p.viewer.camera.position()[1], p.viewer.camera.position()[2]);
		p.p.translate(t.x, t.y, t.z);
		p.p.rotateY(p.viewer.camera.attitude()[0]);
		p.p.rotateX(-p.viewer.camera.attitude()[1]);
		p.p.rotateZ(p.viewer.camera.attitude()[2]);
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
		p.p.text(" R    Restart WorldMediaViewer", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.text(" CMD + q    Quit WorldMediaViewer", xPos, yPos += lineWidth, hudDistance);

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

		start3DHUD();
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

		start3DHUD();
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
			p.p.text("WorldMediaViewer", p.p.width / 2.25f, yPos += lineWidthVeryWide, hudDistance);
			p.p.textSize(largeTextSize * 1.f);
			p.p.text("Version 1.0", p.p.width / 1.265f, yPos += lineWidthVeryWide * 2.f, hudDistance);
			p.p.textSize(largeTextSize * 1.f);
			p.p.text("Software by David Gordon", p.p.width / 1.48f, yPos += lineWidthVeryWide, hudDistance);
			p.p.textSize(largeTextSize * 1.2f);
			
			if(!p.p.state.selectedLibrary)
				p.p.text("Press any key to begin...", p.p.width / 2.1f, yPos += lineWidthVeryWide * 5.f, hudDistance);
			else
				p.p.text("Loading media folder(s)...", p.p.width / 2.1f, yPos += lineWidthVeryWide * 5.f, hudDistance);
			
			p.p.textSize(largeTextSize * 1.2f);
			p.p.text("For support and the latest updates, visit: www.spatializedmusic.com/worldmediaviewer", p.p.width / 2.1f, yPos += lineWidthVeryWide * 16.f, hudDistance);
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
			float[] camTar = p.viewer.camera.target();

			p.p.fill(0, 0, 255, 255);
			p.p.textSize(largeTextSize);
			p.p.text(" WorldMediaViewer v1.0 ", xPos, yPos, hudDistance);
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
				p.p.text(" Debug: Target Point x:" + camTar[0] + ", y:" + camTar[1] + ", z:" + camTar[2], xPos, yPos += lineWidth, hudDistance);
			}
			else
			{
				p.p.text(" Compass Direction:" + p.p.utilities.angleToCompass(p.viewer.getXOrientation())+" Angle: "+p.viewer.getXOrientation(), xPos, yPos += lineWidth, hudDistance);
				p.p.text(" Vertical Direction:" + PApplet.degrees(p.viewer.getYOrientation()), xPos, yPos += lineWidth, hudDistance);
				p.p.text(" Zoom:"+p.viewer.camera.fov(), xPos, yPos += lineWidth, hudDistance);
			}
			p.p.text(" Field of View:"+p.viewer.camera.fov(), xPos, yPos += lineWidth, hudDistance);

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
			p.p.text(" Current Time Segment: "+ p.viewer.getCurrentFieldTimeSegment(), xPos, yPos += lineWidth, hudDistance);
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
		
		float textXPos = centerTextXOffset;
		float textYPos = topTextYOffset;			// Starting vertical position
		
		WMV_Field f = p.getCurrentField();
		WMV_Cluster c = p.getCluster(displayCluster);	// Get the cluster to display info about

		p.p.fill(0, 0, 255, 255);

		p.p.textSize(veryLargeTextSize);
		p.p.text(""+p.getCurrentField().name, textXPos, textYPos, hudDistance);
//		p.p.text(" Cluster View", textXPos, textYPos, hudDistance);

		p.p.textSize(largeTextSize);
		WMV_Cluster cl = p.getCurrentCluster();
		p.p.text(" Cluster #"+ c.getID(), textXPos, textYPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(mediumTextSize);
		p.p.text("   Media Count: "+ c.mediaCount, textXPos, textYPos += lineWidthVeryWide, hudDistance);
		if(c.images.size() > 0)
			p.p.text("     Images: "+ c.images.size(), textXPos, textYPos += lineWidthVeryWide, hudDistance);
		if(c.panoramas.size() > 0)
			p.p.text("     Panoramas: "+ c.panoramas.size(), textXPos, textYPos += lineWidthVeryWide, hudDistance);
		if(c.videos.size() > 0)
			p.p.text("     Videos: "+ c.videos.size(), textXPos, textYPos += lineWidthVeryWide, hudDistance);
//		if(c.sounds.size() > 0)
//			p.p.text("     Sounds: "+ c.sounds.size(), textXPos, textYPos += lineWidthVeryWide, hudDistance);
//		p.p.text("     Active: "+ c.isActive(), textXPos, textYPos += lineWidth, hudDistance);
//		p.p.text("     Single: "+ c.isSingle(), textXPos, textYPos += lineWidth, hudDistance);
//		p.p.text("     Empty: "+ c.isEmpty(), textXPos, textYPos += lineWidth, hudDistance);
		p.p.text("   Location: "+ c.getLocation(), textXPos, textYPos += lineWidthVeryWide, hudDistance);
		p.p.text("   Viewer Distance: "+PApplet.round(PVector.dist(c.getLocation(), p.viewer.getLocation())), textXPos, textYPos += lineWidth, hudDistance);
		p.p.text(" ", textXPos, textYPos += lineWidth, hudDistance);
		p.p.text("   Media Segments: "+ c.segments.size(), textXPos, textYPos += lineWidth, hudDistance);
		
		if(c.timeline.size() > 0)
		{
			p.p.text(" Timeline Segments: "+ c.timeline.size(), textXPos, textYPos += lineWidthWide, hudDistance);
			p.p.text(" Timeline Length (sec.): "+ p.p.utilities.getTimelineLength(c.timeline), textXPos, textYPos += lineWidth, hudDistance);
		}
		if(c.dateline != null)
			if(c.dateline.size() > 0)
				p.p.text(" Timeline Dates: "+ c.dateline.size(), textXPos, textYPos += lineWidth, hudDistance);

		if(p.getCurrentCluster() != null)
		{
			p.p.text("   Auto Stitched Panoramas: "+p.getCurrentCluster().stitchedPanoramas.size(), textXPos, textYPos += lineWidth, hudDistance);
			p.p.text("   User Stitched Panoramas: "+p.getCurrentCluster().userPanoramas.size(), textXPos, textYPos += lineWidth, hudDistance);

			p.p.text(" Current Cluster ID: "+p.viewer.getCurrentClusterID(), textXPos, textYPos += lineWidthVeryWide, hudDistance);
			p.p.text("   Media Count: "+cl.mediaCount, textXPos, textYPos += lineWidth, hudDistance);
			p.p.text("   Viewer Distance: "+PApplet.round(PVector.dist(cl.getLocation(), p.viewer.getLocation())), textXPos, textYPos += lineWidth, hudDistance);
		}
		
		p.p.text(" Field Cluster Count:"+(f.clusters.size()-f.model.mergedClusters), textXPos, textYPos += lineWidthVeryWide, hudDistance);
		p.p.text("   Merged: "+f.model.mergedClusters+" out of "+f.clusters.size()+" Total", textXPos, textYPos += lineWidth, hudDistance);
		if(p.hierarchical) p.p.text(" Current Cluster Depth: "+f.model.clusterDepth, textXPos, textYPos += lineWidth, hudDistance);
		p.p.text("   Minimum Distance: "+p.settings.minClusterDistance, textXPos, textYPos += lineWidth, hudDistance);
		p.p.text("   Maximum Distance: "+p.settings.maxClusterDistance, textXPos, textYPos += lineWidth, hudDistance);
		p.p.text("   Population Factor: "+f.model.clusterPopulationFactor, textXPos, textYPos += lineWidth, hudDistance);

		if(p.viewer.getAttractorCluster() != -1)
		{
			p.p.text(" Destination Cluster ID: "+p.viewer.getAttractorCluster(), textXPos, textYPos += lineWidth, hudDistance);
			p.p.text("    Destination Distance: "+PApplet.round( PVector.dist(f.clusters.get(p.viewer.getAttractorCluster()).getLocation(), p.viewer.getLocation() )), textXPos, textYPos += lineWidth, hudDistance);
			if(p.p.debug.viewer) 
			{
				p.p.text(" Debug: Current Attraction:"+p.viewer.attraction.mag(), textXPos, textYPos += lineWidth, hudDistance);
				p.p.text(" Debug: Current Acceleration:"+(p.viewer.isWalking() ? p.viewer.walkingAcceleration.mag() : p.viewer.acceleration.mag()), textXPos, textYPos += lineWidth, hudDistance);
				p.p.text(" Debug: Current Velocity:"+ (p.viewer.isWalking() ? p.viewer.walkingVelocity.mag() : p.viewer.velocity.mag()) , textXPos, textYPos += lineWidth, hudDistance);
			}
		}

		p.p.popMatrix();
		
		drawClusterImages(c);
	}

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
			float width = 90.f;
			float height = width * origHeight / origWidth;
			
			p.p.translate(imgXPos, imgYPos, hudDistance);
			p.p.tint(255);
			
			if(count < 60)
			{
				PImage image = p.p.loadImage(i.filePath);
				p.p.image(image, 0, 0, width, height);
			}
			
			imgXPos += width * 1.5f;

			if(count % 14 == 0)
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
		
		public int getTimeSegmentID()
		{
			return fieldTimelineID;
		}
		
		public int getClusterID()
		{
			return clusterID;
		}
		
		public PVector getLocation()
		{
			return location;
		}
		
		public void draw(float hue, float saturation, float brightness)
		{
			p.p.stroke(hue, saturation, brightness, 255);												// Yellow rectangle around selected time segment
			p.p.strokeWeight(3.f);

			p.p.pushMatrix();
			
			p.p.line(leftEdge, topEdge, hudDistance, leftEdge, bottomEdge, hudDistance);	
			p.p.line(rightEdge, topEdge, hudDistance, rightEdge, bottomEdge, hudDistance);			
			p.p.line(leftEdge, topEdge, hudDistance, rightEdge, topEdge, hudDistance);			
			p.p.line(leftEdge, bottomEdge, hudDistance, rightEdge, bottomEdge, hudDistance);			

			p.p.popMatrix();
		}
	}

	private class SelectableDate
	{
		private int id, dateID, clusterID;
		private PVector location;
		public float radius;

		SelectableDate(int newID, int newDateID, PVector newLocation, float newRadius)
		{
			id = newID;
			dateID = newDateID;
			location = newLocation;
			radius = newRadius;
		}
		
		public int getID()
		{
			return id;
		}
		
		public int getDateID()
		{
			return dateID;
		}
		
		public PVector getLocation()
		{
			return location;
		}
		
		public void draw(float hue, float saturation, float brightness)
		{
			p.p.pushMatrix();
			
			p.p.stroke(hue, saturation, brightness, 255.f);
			p.p.strokeWeight(25.f);
			p.p.point(location.x, location.y, location.z);

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

