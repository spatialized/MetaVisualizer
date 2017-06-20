package multimediaLocator;
import java.util.ArrayList;

import processing.core.*;

/***********************************
 * Object for displaying 2D text, maps and graphics
 * @author davidgordon
 */
public class ML_Display
{
	/* Classes */
	public MultimediaLocator ml;
	public ML_Window window;							/* Main interaction window */
	public ML_Map map2D;
	private WMV_Utilities utilities;					/* Utility methods */

	/* Window Modes */
	public boolean initializedMaps = false;
	public boolean initializedWorldMap = false;
	
	/* Display Views */
	public int displayView = 0;							/* 0: Scene  1: Map  2: Library  3: Timeline  4: Media */
	
	/* Debug */
	public boolean drawForceVector = false;
	
	/* Setup */
	public boolean worldSetup = true;
	public boolean dataFolderFound = false;
	public float setupProgress = 0.f;
	
	/* Graphics */
	public boolean drawGrid = false; 					/* Draw 3D grid */   			// -- Unused
//	PImage startupImage;

	public int blendMode = 0;							/* Alpha blending mode */
	private int numBlendModes = 10;						/* Number of blending modes */
	private float hudDistanceInit = -1000.f;				/* Distance of the Heads-Up Display from the virtual camera */
	private float messageHUDDistance = hudDistanceInit * 6.f;
	private int screenWidth = -1;
	private int screenHeight = -1;
	
	/* Library View */
	public int libraryViewMode = 0;						// 0: World, 1: Field, 2: Cluster
	public int currentDisplayCluster = 0;
	
	/* Time View */
	float timelineScreenSize, timelineHeight = 100.f;
	float timelineStart = 0.f, timelineEnd = 0.f;
	float datelineStart = 0.f, datelineEnd = 0.f;
	int displayDate = -1;
	public boolean updateCurrentSelectableTimeSegment = true, updateCurrentSelectableDate = true;
	private final float timeTextSize = 44.f;
	
	private ArrayList<SelectableTimeSegment> selectableTimeSegments;		// Selectable time segments on timeline
	private ArrayList<SelectableDate> selectableDates;						// Selectable dates on dateline
	private final float minSegmentSeconds = 15.f;
	
	private boolean fieldTimelineCreated = false, fieldDatelineCreated = false, updateFieldTimeline = true;
	private float timelineXOffset = 0.f, timelineYOffset = 0.f;
	private float datelineXOffset = 0.f, datelineYOffset = 0.f;
	private SelectableTimeSegment currentSelectableTimeSegment;
	private int selectedTime = -1, selectedCluster = -1, currentSelectableTimeSegmentID = -1, currentSelectableTimeSegmentFieldTimeSegmentID = -1;
	private int selectedDate = -1, currentSelectableDate = -1;
		
	private boolean timelineTransition = false, timelineZooming = false, timelineScrolling = false;   
	private int transitionScrollDirection = -1, transitionZoomDirection = -1;
	private int timelineTransitionStartFrame = 0, timelineTransitionEndFrame = 0;
	private int timelineTransitionLength = 30;
	private final int initTimelineTransitionLength = 30;
	
	private float timelineStartTransitionStart = 0, timelineStartTransitionTarget = 0;
	private float timelineEndTransitionStart = 0, timelineEndTransitionTarget = 0;
	public float transitionScrollIncrement = 1750.f;
	public final float initTransitionScrollIncrement = 1750.f;								// Seconds to scroll per frame
	public float transitionZoomInIncrement = 0.95f, transitionZoomOutIncrement = 1.052f;	

	private float imageHue = 140.f;
	private float panoramaHue = 190.f;
	private float videoHue = 100.f;
	private float soundHue = 40.f;

	/* Media View */
	private int mediaViewMediaType = -1;
	private int mediaViewMediaID = -1;

	/* Messages */
	ArrayList<String> messages;							// Messages to display on screen
	ArrayList<String> metadata;							// Metadata messages to display on screen
	ArrayList<String> startupMessages;					// Messages to display on screen
	PFont messageFont;

	int messageDuration = 40;							// Frame length to display messages
	int maxMessages = 16;								// Maximum simultaneous messages on screen

	int messageStartFrame = -1;
	int metadataStartFrame = -1;
	int startupMessageStartFrame = -1;					
	
	/* Text */
	private float centerTextXOffset, topTextYOffset;
//	private float leftTextXOffset, rightTextXOffset;
	private float messageXOffset, messageYOffset, startupMessageXOffset;
	private float metadataXOffset, metadataYOffset, startupMessageYOffset;
	private float midLeftTextXOffset, midRightTextXOffset;
	private float clusterImageXOffset, clusterImageYOffset;
	private float fieldsXScreenSize, fieldsYScreenSize, fieldsXOffset, fieldsYOffset;
	
	private final float veryLargeTextSize = 64.f;
	private final float largeTextSize = 56.f;
	private final float mediumTextSize = 44.f;
	private final float smallTextSize = 36.f;
	private final float linePadding = 20.f;
	private final float lineWidth = smallTextSize + linePadding;			
	private final float lineWidthWide = largeTextSize + linePadding;			
	private final float lineWidthVeryWide = largeTextSize * 2.f;			

	/**
	 * Constructor for 2D display object
	 * @param ml Parent app
	 * @param newScreenWidth Screen width
	 * @param newScreenHeight Screen height
	 * @param newHUDDistance HUD Distance 			-- Obsolete
	 */
	public ML_Display(MultimediaLocator parent)
	{
		ml = parent;
		
		screenWidth = ml.appWidth;
		screenHeight = ml.appHeight;
		
		utilities = new WMV_Utilities();
		
		messages = new ArrayList<String>();
		metadata = new ArrayList<String>();
		startupMessages = new ArrayList<String>();

		centerTextXOffset = screenWidth / 2.f;
//		leftTextXOffset = 0.f;
		midLeftTextXOffset = screenWidth / 3.f;
//		rightTextXOffset = 0.f;
		midRightTextXOffset = screenWidth / 1.5f;

		topTextYOffset = -screenHeight / 1.66f;
		clusterImageXOffset = -screenWidth / 1.85f;
		clusterImageYOffset = screenHeight * 1.33f;
		
		fieldsXOffset = -screenWidth / 1.88f;
		fieldsYOffset = -screenHeight / 1.33f;
		
		fieldsXScreenSize = screenWidth * 1.75f;
		fieldsYScreenSize = screenHeight * 1.33f;

		messageXOffset = screenWidth * 1.75f;
		messageYOffset = -screenHeight * 0.33f;

		metadataXOffset = -screenWidth * 1.33f;
		metadataYOffset = -screenHeight / 2.f;

		startupMessageXOffset = screenWidth / 2.f;
		startupMessageYOffset = -screenHeight /2.f;

		timelineScreenSize = screenWidth * 2.2f;
		timelineStart = 0.f;
		timelineEnd = utilities.getTimePVectorSeconds(new PVector(24,0,0));

		timelineXOffset = -screenWidth / 1.66f;
		timelineYOffset = -screenHeight / 3.f;
		timelineYOffset = 0.f;
		datelineXOffset = timelineXOffset;
		datelineYOffset = screenHeight * 0.266f;
		
		currentSelectableTimeSegment = null;
		currentSelectableTimeSegmentID = -1;
		currentSelectableTimeSegmentFieldTimeSegmentID = -1;

		map2D = new ML_Map(this);
		
		messageFont = ml.createFont("ArialNarrow-Bold", 48);
		ml.textFont(messageFont);
		
//		startupImage = p.p.loadImage("res/WMV_Title.jpg");
	}

	/**
	 * Initialize windows
	 * @param p Parent world
	 */
	void initializeWindows(WMV_World p)
	{
		window = new ML_Window(p, this);				// Setup and display interaction window
	}

	/**
	 * Display HUD elements (messages, satellite map, statistics, metadata, etc.)
	 * @param p Parent world
	 */
	void display(MultimediaLocator ml)
	{
		if(worldSetup)
		{
			ml.hint(PApplet.DISABLE_DEPTH_TEST);									// Disable depth testing for drawing HUD
//			ml.background(0);														// Hide World View
			displayStartup(ml.world, ml.state.librarySetup);						// Draw startup messages
		}
		else																		
		{
			if( displayView == 0 )													// World View						
			{
				if( messages.size() > 0 || metadata.size() > 0 )
				{
//					ml.hint(PApplet.DISABLE_DEPTH_TEST);												// Disable depth testing for drawing HUD
					if(messages.size() > 0) displayMessages(ml.world);
					if(ml.world.getState().showMetadata && metadata.size() > 0 && ml.world.viewer.getSettings().selection)	
						displayMetadata(ml.world);
				}
			}
			else																	// 2D Views
			{
				ml.hint(PApplet.DISABLE_DEPTH_TEST);								// Disable depth testing for drawing HUD
//				ml.background(0.f);												// Hide World View

				switch(displayView)
				{
					case 1:
						if(initializedMaps) map2D.displaySatelliteMap(ml.world);
						if(ml.state.interactive) displayInteractiveClustering(ml.world);
						map2D.update(ml.world);
						break;
					case 2:
						displayTimeView(ml.world);
						updateFieldTimeline(ml.world);
						break;
					case 3:
						displayLibraryView(ml.world);
						if(libraryViewMode == 0) map2D.update(ml.world);
						break;
					case 4:
						displayMediaView(ml.world);
						break;
				}

			}
		}
	}

	/**
	 * Set the current initialization progress bar position
	 * @param progress New position {0.f to 1.f}
	 */
	public void setupProgress(float progress)
	{
		setupProgress = progress;
	}

	/**
	 * Display Time View in main window
	 * @param p Parent world
	 */
	public void displayTimeView(WMV_World p)
	{
		startHUD(p);
		ml.pushMatrix();
		
		float xPos = centerTextXOffset;
		float yPos = topTextYOffset;			// Starting vertical position
		
		WMV_Field f = p.getCurrentField();
		WMV_Cluster c = p.getCurrentCluster();

		ml.fill(0, 0, 255, 255);

		ml.textSize(veryLargeTextSize);
		ml.text(""+p.getCurrentField().getName(), xPos, yPos, getMessageHUDDistance());

		ml.textSize(largeTextSize);
		String strDisplayDate = "Showing All Dates";
		if(displayDate != -1) strDisplayDate = utilities.getDateAsString(p.getCurrentField().getDate(displayDate));

		ml.text(strDisplayDate, xPos, yPos += lineWidthVeryWide * 1.5f, hudDistanceInit);
		ml.textSize(mediumTextSize);
		ml.text(" Time Zone: "+ f.getTimeZoneID(), xPos, yPos += lineWidthVeryWide, hudDistanceInit);

		yPos = timelineYOffset + timelineHeight * 4.f;

		ml.popMatrix();
		
		if(fieldDatelineCreated) displayFieldDateline(p);
		if(fieldTimelineCreated) displayFieldTimeline(p);
		updateTimelineMouse(p);
	}
	
	/**
	 * Update field timeline every frame
	 * @param p Parent world
	 */
	private void updateFieldTimeline(WMV_World p)
	{
		if(timelineTransition)
			updateTimelineTransition(p);

		if(!fieldDatelineCreated || !fieldTimelineCreated || updateFieldTimeline)
		{
			if(!timelineTransition)
			{
				createFieldDateline(p);
				createFieldTimeline(p);
				updateFieldTimeline = false;
			}
		}

		if(updateCurrentSelectableTimeSegment && !timelineTransition)
		{
			if(p.viewer.getCurrentFieldTimeSegment() >= 0)
			{
				WMV_TimeSegment t = p.getCurrentField().getTimeSegment(p.viewer.getCurrentFieldTimeSegment());		
				int previous = currentSelectableTimeSegmentID;
				
				if(t != null)
				{
					currentSelectableTimeSegmentID = getSelectableTimeIDOfFieldTimeSegment(t);						// Set current selectable time (white rectangle) from current field time segment
					if(currentSelectableTimeSegmentID != -1)
					{
						currentSelectableTimeSegment = selectableTimeSegments.get(currentSelectableTimeSegmentID);
						currentSelectableTimeSegmentFieldTimeSegmentID = currentSelectableTimeSegment.segment.getFieldTimelineID();						// Set current selectable time (white rectangle) from current field time segment
					}
					else
					{
						currentSelectableTimeSegmentFieldTimeSegmentID = -1;
						currentSelectableTimeSegment = null;
					}
				}
				else
				{
					currentSelectableTimeSegmentID = -1;
					currentSelectableTimeSegmentFieldTimeSegmentID = -1;
					currentSelectableTimeSegment = null;
				}

				if(updateCurrentSelectableDate)
				{
					if(currentSelectableTimeSegmentID != previous && currentSelectableDate > -1)						// If changed field segment and displaying a single date
					{
						int fieldDate = p.getCurrentField().getTimeSegment(p.viewer.getCurrentFieldTimeSegment()).getFieldDateID();		// Update date displayed
						setCurrentSelectableDate(fieldDate);
					}
				}

				updateCurrentSelectableTimeSegment = false;
				updateCurrentSelectableDate = false;
			}
			else 
				System.out.println("updateCurrentSelectableTimeSegment... No current time segment!");
		}
	}
	
	/**
	 * Create field dateline
	 * @param p Parent world
	 */
	private void createFieldDateline(WMV_World p)
	{
		WMV_Field f = p.getCurrentField();
		
		if(f.getDateline().size() > 0)
		{
			WMV_Date first = f.getDate(0);
			float padding = 1.f;
			int firstDay = first.getDay();
			int firstMonth = first.getMonth();
			int firstYear = first.getYear();

			WMV_Date last = f.getDate(f.getDateline().size()-1);
			int lastDay = last.getDay();
			int lastMonth = last.getMonth();
			int lastYear = last.getYear();
			
			if(f.getDateline().size() == 2)
				padding = utilities.getDaysSince1980(p.getCurrentField().getTimeZoneID(), lastDay, lastMonth, lastYear) - utilities.getDaysSince1980(p.getCurrentField().getTimeZoneID(), firstDay, firstMonth, firstYear);
			else if(f.getDateline().size() > 2)
				padding = (utilities.getDaysSince1980(p.getCurrentField().getTimeZoneID(), lastDay, lastMonth, lastYear) - utilities.getDaysSince1980(p.getCurrentField().getTimeZoneID(), firstDay, firstMonth, firstYear)) * 0.33f;
			
			datelineStart = utilities.getDaysSince1980(p.getCurrentField().getTimeZoneID(), firstDay, firstMonth, firstYear) - padding;
			datelineEnd = utilities.getDaysSince1980(p.getCurrentField().getTimeZoneID(), lastDay, lastMonth, lastYear) + padding;
			
			createFieldSelectableDates(p);
			fieldDatelineCreated = true;
		}
		else
		{
			System.out.println("ERROR no dateline in field!!");
		}
	}
	
	/**
	 * Create viewable timeline for field
	 * @param p Parent world
	 */
	private void createFieldTimeline(WMV_World p)
	{
		WMV_Field f = p.getCurrentField();
		selectableTimeSegments = new ArrayList<SelectableTimeSegment>();
		
		if(f.getDateline().size() == 1) 		/* Field contains media from single date */
		{
			int count = 0;
			for(WMV_TimeSegment t : f.getTimeline().timeline)
			{
				SelectableTimeSegment st = getSelectableTimeSegment(t, count);
				if(st != null)
				{
					selectableTimeSegments.add(st);
					count++;
				}
			}
		}
		else if(f.getDateline().size() > 1)		/* Field contains media from multiple dates */
		{
			if(displayDate == -1)
			{
				int count = 0;
				for(WMV_Timeline ts : f.getTimelines())
				{
					for(WMV_TimeSegment t:ts.timeline)
					{
						SelectableTimeSegment st = getSelectableTimeSegment(t, count);
						if(st != null)
						{
							selectableTimeSegments.add(st);
							count++;
						}
					}
				}
			}
			else
			{
				if(displayDate < f.getTimelines().size())
				{
					ArrayList<WMV_TimeSegment> ts = f.getTimelines().get(displayDate).timeline;
					int count = 0;
					
					for(WMV_TimeSegment t:ts)
					{
						SelectableTimeSegment st = getSelectableTimeSegment(t, count);
						if(st != null)
						{
							selectableTimeSegments.add(st);
							count++;
						}
					}
				}
			}
		}
		
		fieldTimelineCreated = true;
	}

	/**
	 * Create selectable dates for current field
	 * @param p Parent world
	 */
	private void createFieldSelectableDates(WMV_World p)
	{
		WMV_Field f = p.getCurrentField();
		selectableDates = new ArrayList<SelectableDate>();
		
		if(f.getDateline().size() == 1)
		{
			SelectableDate sd = getSelectableDate(f.getDate(0), 0);
			if(sd != null)
				selectableDates.add(sd);
		}
		else if(f.getDateline().size() > 1)
		{
			int count = 0;
			for(WMV_Date t : f.getDateline())
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
	 * Transition timeline zoom from current to given value
	 * @param newStart New timeline left edge value
	 * @param newEnd New timeline right edge value
	 * @param transitionLength Transition length in frames
	 * @param frameCount Current frame count
	 */
	void timelineTransition(float newStart, float newEnd, int transitionLength, int frameCount)
	{
		timelineTransitionLength = transitionLength;

		if(!timelineTransition)
		{
			if(timelineStart != newStart || timelineEnd != newEnd)					// Check if already at target
			{
				timelineTransition = true;   
				timelineTransitionStartFrame = frameCount;
				timelineTransitionEndFrame = timelineTransitionStartFrame + timelineTransitionLength;
				
				if(timelineStart != newStart && timelineEnd != newEnd)
				{
					timelineStartTransitionStart = timelineStart;
					timelineStartTransitionTarget = newStart;
					timelineEndTransitionStart = timelineEnd;
					timelineEndTransitionTarget = newEnd;
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
	 * @param p Parent world
	 */
	void updateTimelineTransition(WMV_World p)
	{
		float newStart = timelineStart;
		float newEnd = timelineEnd;

		if (ml.frameCount >= timelineTransitionEndFrame)
		{
			newStart = timelineStartTransitionTarget;
			newEnd = timelineEndTransitionTarget;
			timelineTransition = false;
			updateFieldTimeline = true;
			updateCurrentSelectableTimeSegment = true;
			transitionScrollIncrement = initTransitionScrollIncrement * getZoomLevel();
		} 
		else
		{
			if(timelineStart != timelineStartTransitionTarget)
			{
				newStart = utilities.mapValue( ml.frameCount, timelineTransitionStartFrame, timelineTransitionEndFrame,
									    timelineStartTransitionStart, timelineStartTransitionTarget); 
			}
			if(timelineEnd != timelineEndTransitionTarget)
			{
				newEnd = utilities.mapValue( ml.frameCount, timelineTransitionStartFrame, timelineTransitionEndFrame,
									  timelineEndTransitionStart, timelineEndTransitionTarget);     			
			}
		}

		if(timelineStart != newStart)
			timelineStart = newStart;
		if(timelineEnd != newEnd)
			timelineEnd = newEnd;

		if(timelineScrolling)
			scroll(p, transitionScrollDirection);

		if(timelineZooming)
			zoom(p, transitionZoomDirection, true);
	}

	/**
	 * Create and return selectable time segment from timeline
	 * @param t Time segment
	 * @param id Time segment id
	 * @return New SelectableTimeSegment object
	 */
	private SelectableTimeSegment getSelectableTimeSegment(WMV_TimeSegment t, int id)
	{
		float lowerSeconds = utilities.getTimePVectorSeconds(t.getLower().getTimeAsPVector());
		float upperSeconds = utilities.getTimePVectorSeconds(t.getUpper().getTimeAsPVector());

		if(upperSeconds == lowerSeconds)
		{
			lowerSeconds -= minSegmentSeconds * 0.5f;
			upperSeconds += minSegmentSeconds * 0.5f;
		}
		
		float xOffset = utilities.mapValue(lowerSeconds, timelineStart, timelineEnd, timelineXOffset, timelineXOffset + timelineScreenSize);
		float xOffset2 = utilities.mapValue(upperSeconds, timelineStart, timelineEnd, timelineXOffset, timelineXOffset + timelineScreenSize);
 
		if(xOffset > timelineXOffset && xOffset2 < timelineXOffset + timelineScreenSize)
		{
			float rectLeftEdge, rectRightEdge, rectTopEdge, rectBottomEdge;
			float rectWidth = xOffset2 - xOffset;

			PVector loc = new PVector(xOffset, timelineYOffset, hudDistanceInit);
			
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
		float xOffset = utilities.mapValue(date, datelineStart, datelineEnd, datelineXOffset, datelineXOffset + timelineScreenSize);

		if(xOffset > datelineXOffset && xOffset < datelineXOffset + timelineScreenSize)
		{
			float radius = 25.f;
			PVector loc = new PVector(xOffset, datelineYOffset, hudDistanceInit);
			SelectableDate st = new SelectableDate(id, loc, radius, d);		// int newID, int newClusterID, PVector newLocation, Box newRectangle
			return st;
		}
		else return null;
	}	

	/**
	 * Draw the field timeline
	 * @param p Parent world
	 */
	private void displayFieldTimeline(WMV_World p)
	{
		WMV_Field f = p.getCurrentField();
			
		ml.tint(255);
		ml.stroke(0.f, 0.f, 255.f, 255.f);
		ml.strokeWeight(3.f);
		ml.fill(0.f, 0.f, 255.f, 255.f);
		ml.line(timelineXOffset, timelineYOffset, hudDistanceInit, timelineXOffset + timelineScreenSize, timelineYOffset, hudDistanceInit);
		
		String startTime = utilities.secondsToTimeAsString(timelineStart, false, false);
		String endTime = utilities.secondsToTimeAsString(timelineEnd, false, false);
		
		ml.textSize(timeTextSize);
		float firstHour = utilities.roundSecondsToHour(timelineStart);
		if(firstHour == (int)(timelineStart / 3600.f) * 3600.f) firstHour += 3600.f;
		float lastHour = utilities.roundSecondsToHour(timelineEnd);
		if(lastHour == (int)(timelineEnd / 3600.f + 1.f) * 3600.f) lastHour -= 3600.f;
		
		float timeLength = timelineEnd - timelineStart;
		float timeToScreenRatio = timelineScreenSize / timeLength;
		
		if(lastHour / 3600.f - firstHour / 3600.f <= 16.f)
		{
			float xOffset = timelineXOffset + (firstHour - timelineStart) * timeToScreenRatio - 20.f;
			
			float pos;
			for( pos = firstHour ; pos <= lastHour ; pos += 3600.f )
			{
				String time = utilities.secondsToTimeAsString(pos, false, false);
				ml.text(time, xOffset, timelineYOffset - timelineHeight * 0.5f - 40.f, hudDistanceInit);
				xOffset += 3600.f * timeToScreenRatio;
			}
			
			if( (firstHour - timelineStart) * timeToScreenRatio - 20.f > 200.f)
				ml.text(startTime, timelineXOffset, timelineYOffset - timelineHeight * 0.5f - 40.f, hudDistanceInit);
			
			xOffset -= 3600.f * timeToScreenRatio;
			if(timelineXOffset + timelineScreenSize - 40.f - xOffset > 200.f)
				ml.text(endTime, timelineXOffset + timelineScreenSize - 40.f, timelineYOffset - timelineHeight * 0.5f - 40.f, hudDistanceInit);
		}
		else
		{
			float xOffset = timelineXOffset;
			for( float pos = firstHour - 3600.f ; pos <= lastHour ; pos += 7200.f )
			{
				String time = utilities.secondsToTimeAsString(pos, false, false);
				ml.text(time, xOffset, timelineYOffset - timelineHeight * 0.5f - 40.f, hudDistanceInit);
				xOffset += 7200.f * timeToScreenRatio;
			}
		}

		if(f.getDateline().size() == 1)
		{
			for(WMV_TimeSegment t : f.getTimeline().timeline)
				displayTimeSegment(p, t);
		}
		else if(f.getDateline().size() > 1)
		{
			if(displayDate == -1)
			{
				for(WMV_Timeline ts : f.getTimelines())
					for(WMV_TimeSegment t:ts.timeline)
						displayTimeSegment(p, t);
			}
			else
			{
				if(displayDate < f.getTimelines().size())
				{
					ArrayList<WMV_TimeSegment> ts = f.getTimelines().get(displayDate).timeline;
					for(WMV_TimeSegment t:ts)
						displayTimeSegment(p, t);
				}
			}
		}
		
		if(!timelineTransition)
		{
			if(currentSelectableTimeSegmentID >= 0 && currentSelectableTimeSegmentID < selectableTimeSegments.size())
			{
				if(currentSelectableTimeSegmentFieldTimeSegmentID == selectableTimeSegments.get(currentSelectableTimeSegmentID).segment.getFieldTimelineID())
				{
					if(currentSelectableTimeSegmentID != -1 && selectableTimeSegments.size() > 0 && currentSelectableTimeSegmentID < selectableTimeSegments.size())
					{
						if(displayDate == -1 || selectableTimeSegments.get(currentSelectableTimeSegmentID).segment.getFieldDateID() == displayDate)
						{
							if(selectedTime == -1)						/* Draw current time segment */
								selectableTimeSegments.get(currentSelectableTimeSegmentID).display(p, 0.f, 0.f, 255.f, true);
							else
								selectableTimeSegments.get(currentSelectableTimeSegmentID).display(p, 0.f, 0.f, 255.f, false);
						}
					}
				}
			}
			
			/* Draw selected time segment */
			if(selectedTime != -1 && selectableTimeSegments.size() > 0 && selectedTime < selectableTimeSegments.size())
				selectableTimeSegments.get(selectedTime).display(p, 40.f, 255.f, 255.f, true);
		}
	}

	/**
	 * Display dateline for current field
	 * @param p Parent world
	 */
	private void displayFieldDateline(WMV_World p)
	{
		WMV_Field f = p.getCurrentField();
			
		ml.tint(255);
		ml.stroke(0.f, 0.f, 255.f, 255.f);
		ml.strokeWeight(3.f);
		ml.fill(0.f, 0.f, 255.f, 255.f);
		ml.line(datelineXOffset, datelineYOffset, hudDistanceInit, datelineXOffset + timelineScreenSize, datelineYOffset, hudDistanceInit);
	
		if(f.getDateline().size() == 1)
		{
			displayDate(p, f.getDate(0));
		}
		else if(f.getDateline().size() > 1)
		{
//			int count = 0;
			for(WMV_Date d : f.getDateline())
			{
				displayDate(p, d);
//				count++;
			}
		}
		
		if(selectedDate != -1 && selectableDates.size() > 0 && selectedDate < selectableDates.size())
			selectableDates.get(selectedDate).display(p, 40.f, 255.f, 255.f, true);
		if(currentSelectableDate != -1 && selectableDates.size() > 0 && currentSelectableDate < selectableDates.size())
			selectableDates.get(currentSelectableDate).display(p, 0.f, 0.f, 255.f, false);
	}

	/**
	 * Display date on timeline
	 * @param p Parent world
	 * @param d Date to display
	 */
	private void displayDate(WMV_World p, WMV_Date d)
	{
		float date = d.getDaysSince1980();
		float xOffset = utilities.mapValue(date, datelineStart, datelineEnd, datelineXOffset, datelineXOffset + timelineScreenSize);

		if(xOffset > datelineXOffset && xOffset < datelineXOffset + timelineScreenSize)
		{
			ml.strokeWeight(0.f);
			ml.fill(120.f, 165.f, 245.f, 155.f);

			ml.pushMatrix();
			ml.stroke(120.f, 165.f, 245.f, 155.f);
			ml.strokeWeight(25.f);
			ml.point(xOffset, datelineYOffset, hudDistanceInit);
			ml.popMatrix();
		}
	}
	
	/**
	 * Display time segment on timeline
	 * @param t Time segment to display
	 * @param id 
	 */
	private void displayTimeSegment(WMV_World p, WMV_TimeSegment t)
	{
		PVector lowerTime = t.getLower().getTimeAsPVector();			// Format: PVector(hour, minute, second)
		PVector upperTime = t.getUpper().getTimeAsPVector();			

		float lowerSeconds = utilities.getTimePVectorSeconds(lowerTime);
		float upperSeconds = utilities.getTimePVectorSeconds(upperTime);

		if(upperSeconds == lowerSeconds)
		{
			lowerSeconds -= minSegmentSeconds * 0.5f;
			upperSeconds += minSegmentSeconds * 0.5f;
		}

		ArrayList<WMV_Time> tsTimeline = t.getTimeline();				// Get timeline for this time segment
		ArrayList<PVector> times = new ArrayList<PVector>();
		for(WMV_Time ti : tsTimeline) times.add(ti.getTimeAsPVector());

		float xOffset = utilities.mapValue(lowerSeconds, timelineStart, timelineEnd, timelineXOffset, timelineXOffset + timelineScreenSize);
		float xOffset2 = utilities.mapValue(upperSeconds, timelineStart, timelineEnd, timelineXOffset, timelineXOffset + timelineScreenSize);

		if(xOffset > timelineXOffset && xOffset2 < timelineXOffset + timelineScreenSize)
		{
			ml.pushMatrix();
			ml.translate(0.f, timelineYOffset, hudDistanceInit);
			ml.stroke(imageHue, 185.f, 255.f, 155.f);
			ml.strokeWeight(1.f);

			for(WMV_Time ti : tsTimeline)
			{
				PVector time = ti.getTimeAsPVector();
				float seconds = utilities.getTimePVectorSeconds(time);
				float xOff = utilities.mapValue(seconds, timelineStart, timelineEnd, timelineXOffset, timelineXOffset + timelineScreenSize);
				if(xOff > timelineXOffset && xOff < timelineXOffset + timelineScreenSize)
				{
					switch(ti.getMediaType())
					{
						case 0:
							ml.stroke(imageHue, 165.f, 215.f, 225.f);					
							break;
						case 1:
							ml.stroke(panoramaHue, 165.f, 215.f, 225.f);					
							break;
						case 2:
							ml.stroke(videoHue, 165.f, 215.f, 225.f);					
							break;
						case 3:
							ml.stroke(soundHue, 165.f, 215.f, 225.f);					
							break;
					}
					ml.line(xOff, -timelineHeight / 2.f, 0.f, xOff, timelineHeight / 2.f, 0.f);
				}
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
					System.out.println("ERROR: firstHue still imageHue but segment has no image!");
				else
					defaultHue = firstHue;
				if(secondHue == imageHue)
					secondHue = defaultHue;
			}
			
			ml.strokeWeight(2.f);

			/* Draw rectangle around time segment */
			ml.stroke(firstHue, 165.f, 215.f, 225.f);					
			ml.line(xOffset, -timelineHeight * 0.5f, 0.f, xOffset, timelineHeight * 0.5f, 0.f);			
			ml.line(xOffset, timelineHeight * 0.5f, 0.f, xOffset2, timelineHeight * 0.5f, 0.f);
			ml.stroke(secondHue, 165.f, 215.f, 225.f);					
			ml.line(xOffset2, -timelineHeight * 0.5f, 0.f, xOffset2, timelineHeight * 0.5f, 0.f);
			ml.line(xOffset, -timelineHeight * 0.5f, 0.f, xOffset2, -timelineHeight * 0.5f, 0.f);
			
			ml.popMatrix();
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
		
		PVector result = new PVector(mouseX * wFactor - screenWidth * 0.775f, mouseY * hFactor - screenHeight * 0.775f, hudDistanceInit);
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
		for(SelectableTimeSegment st : selectableTimeSegments)
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

	/**
	 * Update timeline based on current mouse position
	 * @param p Parent world
	 */
	public void updateTimelineMouse(WMV_World p)
	{
		PVector mouseLoc = getMouse3DLocation(ml.mouseX, ml.mouseY);
		if(selectableTimeSegments != null)
		{
			SelectableTimeSegment timeSelected = getSelectedTimeSegment(mouseLoc);
			if(timeSelected != null)
			{
				selectedTime = timeSelected.getID();				// Set to selected
				selectedCluster = timeSelected.getClusterID();

				if(ml.debugSettings.time && ml.debugSettings.detailed)
					System.out.println("Selected time segment:"+selectedTime+" selectedCluster:"+selectedCluster);
				updateFieldTimeline = true;				// Update timeline to show selected segment
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
				updateFieldTimeline = true;				// Update timeline to show selected segment
			}
			else
				selectedDate = -1;
		}
	}
	
	/**
	 * Zoom out to full timeline
	 * @param p Parent world
	 * @param transition Whether to use smooth zooming transition
	 */
	public void zoomToTimeline(WMV_World p, boolean transition)
	{
		WMV_Field f = p.getCurrentField();
		
		float first = f.getTimeSegment(0).getLower().getTime();						// First field media time, normalized
		float last = f.getTimeSegment(f.getTimeline().timeline.size()-1).getUpper().getTime();		// Last field media time, normalized
		float day = utilities.getTimePVectorSeconds(new PVector(24,0,0));		// Seconds in a day

		first *= day;					// Convert from normalized value to seconds
		last *= day;
		
		float newTimelineStart = utilities.roundSecondsToHour(first);		// Round down to nearest hour
		if(newTimelineStart > first) newTimelineStart -= 3600;
		if(newTimelineStart < 0.f) newTimelineStart = 0.f;
		float newTimelineEnd = utilities.roundSecondsToHour(last);			// Round up to nearest hour
		if(newTimelineEnd < last) newTimelineEnd += 3600;
		if(newTimelineEnd > day) newTimelineEnd = day;

		if(transition)
		{
			timelineTransition(newTimelineStart, newTimelineEnd, initTimelineTransitionLength, ml.frameCount);
		}
		else
		{
			timelineStart = newTimelineStart;
			timelineEnd = newTimelineEnd;
		}
	}
	
	/**
	 * Zoom to current selectable time segment
	 * @param p Parent World
	 * @param transition Whether to use smooth zooming transition
	 */
	public void zoomToCurrentSelectableTimeSegment(WMV_World p, boolean transition)
	{
		if(currentSelectableTimeSegmentID >= 0)
		{
			float first = selectableTimeSegments.get(currentSelectableTimeSegmentID).segment.getLower().getTime();
			float last = selectableTimeSegments.get(currentSelectableTimeSegmentID).segment.getUpper().getTime();
			float day = utilities.getTimePVectorSeconds(new PVector(24,0,0));		// Seconds in a day

			first *= day;					// Convert from normalized value to seconds
			last *= day;

			float newTimelineStart = utilities.roundSecondsToInterval(first, 600.f);		// Round down to nearest hour
			if(newTimelineStart > first) newTimelineStart -= 600;
			if(newTimelineStart < 0.f) newTimelineStart = 0.f;
			float newTimelineEnd = utilities.roundSecondsToInterval(last, 600.f);			// Round up to nearest hour
			if(newTimelineEnd < last) newTimelineEnd += 600;
			if(newTimelineEnd > day) newTimelineEnd = day;

			if(transition)
			{
				timelineTransition(newTimelineStart, newTimelineEnd, initTimelineTransitionLength, ml.frameCount);
			}
			else
			{
				timelineStart = newTimelineStart;
				timelineEnd = newTimelineEnd;
			}
		}
	}
	
	/**
	 * Zoom to the current selectable date
	 * @param p Parent world
	 * @param transition Whether to use smooth zooming transition
	 */
	public void zoomToCurrentSelectableDate(WMV_World p, boolean transition)
	{
		if(currentSelectableDate >= 0)
		{
			WMV_Field f = p.getCurrentField();
			int curDate = selectableDates.get(currentSelectableDate).getID();
			float first = f.getTimelines().get(curDate).timeline.get(0).getLower().getTime();
			float last = f.getTimelines().get(curDate).timeline.get(f.getTimelines().get(curDate).timeline.size()-1).getUpper().getTime();
			float day = utilities.getTimePVectorSeconds(new PVector(24,0,0));		// Seconds in a day

			first *= day;					// Convert from normalized value to seconds
			last *= day;

			float newTimelineStart = utilities.roundSecondsToInterval(first, 1800.f);		// Round down to nearest hour
			if(newTimelineStart > first) newTimelineStart -= 1800;
			if(newTimelineStart < 0.f) newTimelineStart = 0.f;
			float newTimelineEnd = utilities.roundSecondsToInterval(last, 1800.f);			// Round up to nearest hour
			if(newTimelineEnd < last) newTimelineEnd += 1800;
			if(newTimelineEnd > day) newTimelineEnd = day;

			if(transition)
			{
				timelineTransition(newTimelineStart, newTimelineEnd, initTimelineTransitionLength, ml.frameCount);
			}
			else
			{
				timelineStart = newTimelineStart;
				timelineEnd = newTimelineEnd;
			}
		}
	}
	
	public void resetZoom(WMV_World p, boolean fade)
	{
		float newTimelineStart = 0.f;
		float newTimelineEnd = utilities.getTimePVectorSeconds(new PVector(24,0,0));
		
		if(fade)
		{
			timelineTransition(newTimelineStart, newTimelineEnd, initTimelineTransitionLength, ml.frameCount);
		}
		else
		{
			timelineStart = newTimelineStart;
			timelineEnd = newTimelineEnd;
		}
	}
	
	public float getZoomLevel()
	{
		float day = utilities.getTimePVectorSeconds(new PVector(24,0,0));		// Seconds in a day
		float result = (timelineEnd - timelineStart) / day;
		return result;
	}
	
	/**
	 * Start timeline zoom transition
	 * @param p Parent world
	 * @param direction -1: In  1: Out
	 * @param transition Whether to use smooth zooming transition
	 */
	public void zoom(WMV_World p, int direction, boolean transition)
	{
		boolean zoom = true;
		transitionZoomDirection = direction;
		float length = timelineEnd - timelineStart;
		float newLength;
		float day = utilities.getTimePVectorSeconds(new PVector(24,0,0));		// Seconds in a day
		
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
			
			float first = f.getTimeSegment(0).getLower().getTime();						// First field media time, normalized
			float last = f.getTimeSegment(f.getTimeline().timeline.size()-1).getUpper().getTime();		// Last field media time, normalized

			if(transitionZoomDirection == 1)
			{
				if(length - newLength > 300.f)
				{
					newTimelineStart = utilities.roundSecondsToInterval(newTimelineStart, 600.f);		// Round up to nearest 10 min.
					if(newTimelineStart > first) newTimelineStart -= 600;
					newTimelineEnd = utilities.roundSecondsToInterval(newTimelineEnd, 600.f);		// Round up to nearest 10 min.
					if(newTimelineEnd < last) newTimelineEnd += 600;
				}
			}
			if(newTimelineEnd > day) newTimelineEnd = day;
			if(newTimelineStart < 0.f) newTimelineEnd = 0.f;

			if(transition)
			{
				timelineTransition(timelineStart, newTimelineEnd, 10, ml.frameCount);
				timelineZooming = true;
			}
			else
				timelineEnd = newTimelineEnd;
		}
	}
	
	/**
	 * Zoom by a factor
	 * @param p Parent world
	 * @param amount Factor to zoom by
	 * @param transition Whether to use smooth zooming transition 
	 */
	public void zoomByAmount(WMV_World p, float amount, boolean transition)
	{
		float length = timelineEnd - timelineStart;
		float newLength = length * amount;
		float result = timelineStart + newLength;
		
		if(result < utilities.getTimePVectorSeconds(new PVector(24,0,0)))
		{
			WMV_Field f = p.getCurrentField();
			float last = f.getTimeSegment(f.getTimeline().timeline.size()-1).getUpper().getTime();		// Last field media time, normalized
			float day = utilities.getTimePVectorSeconds(new PVector(24,0,0));		// Seconds in a day

			float newTimelineEnd;
			if(length - newLength > 300.f)
			{
				newTimelineEnd = utilities.roundSecondsToInterval(result, 600.f);		// Round up to nearest 10 min.
				if(newTimelineEnd < last) newTimelineEnd += 600;
			}
			else						
				newTimelineEnd = result;													// Changing length less than 5 min., no rounding								

			if(newTimelineEnd > day) newTimelineEnd = day;

			if(transition)
				timelineTransition(timelineStart, newTimelineEnd, initTimelineTransitionLength, ml.frameCount);
			else
				timelineEnd = newTimelineEnd;
		}
	}
	
	/**
	 * Start scrolling timeline in specified direction
	 * @param p Parent world
	 * @param direction Left: -1 or Right: 1
	 */
	public void scroll(WMV_World p, int direction)
	{
		transitionScrollDirection = direction;
		float newStart = timelineStart + transitionScrollIncrement * transitionScrollDirection;
		float newEnd = timelineEnd + transitionScrollIncrement * transitionScrollDirection;		
		float day = utilities.getTimePVectorSeconds(new PVector(24,0,0));
		
		if(newStart > 0.f && newEnd < day)
		{
			timelineScrolling = true;
			timelineTransition(newStart, newEnd, 10, ml.frameCount);			
		}
	}
	
	/**
	 * Stop zooming the timeline
	 */
	public void stopZooming()
	{
		timelineZooming = false;
		updateFieldTimeline = true;
		transitionScrollIncrement = initTransitionScrollIncrement * getZoomLevel();
	}
	
	/**
	 * Stop scrolling the timeline
	 */
	public void stopScrolling()
	{
		timelineScrolling = false;
		updateFieldTimeline = true;
	}
	
	/**
	 * Handle mouse released event
	 * @param p Parent world
	 * @param mouseX Mouse x position
	 * @param mouseY Mouse y position
	 */
	public void handleMouseReleased(WMV_World p, float mouseX, float mouseY)
	{
		updateTimelineMouse(p);
		
		if(selectedTime != -1)
			if(selectedCluster != -1)
				p.viewer.teleportToCluster(selectedCluster, false, selectableTimeSegments.get(selectedTime).segment.getFieldTimelineID());

		if(selectedDate != -1)
			setCurrentSelectableDate(selectedDate);
	}

	/**
	 * Draw Interactive Clustering screen
	 * @param p Parent world
	 */
	void displayInteractiveClustering(WMV_World p)
	{
		map2D.displaySatelliteMap(p);
		if(messages.size() > 0) displayMessages(p);
	}

	/**
	 * Draw Interactive Clustering footer text
	 * @param ml Parent app
	 */
	void displayClusteringInfo(MultimediaLocator ml)
	{
		if(ml.world.state.hierarchical)
		{
			message(ml, "Hierarchical Clustering");
			message(ml, " ");
			message(ml, "Use arrow keys UP and DOWN to change clustering depth... ");
			message(ml, "Use [ and ] to change Minimum Cluster Distance... ");
		}
		else
		{
			message(ml, "K-Means Clustering");
			message(ml, " ");
			message(ml, "Use arrow keys LEFT and RIGHT to change Iterations... ");
			message(ml, "Use arrow keys UP and DOWN to change Population Factor... ");
			message(ml, "Use [ and ] to change Minimum Cluster Distance... ");
		}
		
		message(ml, " ");
		message(ml, "Press <spacebar> to restart simulation...");
	}

	/**
	 * Reset display object
	 */
	void reset()
	{
		System.out.println("Display.reset()... ");
		/* Window Modes */
//		fullscreen = true;
		initializedMaps = false;

		/* Display Mode */
		displayView = 0;
		
		/* Debug */
		drawForceVector = false;
		
		/* Graphics */
		drawGrid = false; 			// Draw 3D grid   			-- Unused

		blendMode = 0;							// Alpha blending mode
		numBlendModes = 10;						// Number of blending modes

		/* Timeline */
		timelineHeight = 80.f;
		displayDate = -1;
		
		selectedTime = -1; 
		selectedCluster = -1;
		currentSelectableTimeSegmentID = -1;
		currentSelectableTimeSegment = null;
		selectedDate = -1; 
		currentSelectableDate = -1;

		timelineScreenSize = screenWidth * 2.2f;
		timelineStart = 0.f;
		timelineEnd = utilities.getTimePVectorSeconds( new PVector(24,0,0) );

		System.out.println("Display.reset()... 1");

		datelineStart = 0.f;
		datelineEnd = 0.f;
		
		updateCurrentSelectableTimeSegment = true;
		updateCurrentSelectableDate = true;
		
		timelineXOffset = -screenWidth/ 1.66f;
		timelineYOffset = -screenHeight/ 2.f;
		timelineYOffset = 0.f;
		datelineXOffset = timelineXOffset;
		datelineYOffset = screenHeight * 0.2f;

		selectableTimeSegments = new ArrayList<SelectableTimeSegment>();
		selectableDates = new ArrayList<SelectableDate>();

		fieldTimelineCreated = false;
		fieldDatelineCreated = false;
		updateFieldTimeline = true;

		timelineTransition = false; 
		timelineZooming = false; 
		timelineScrolling = false;   
		
		transitionScrollDirection = -1; 
		transitionZoomDirection = -1;
		timelineTransitionStartFrame = 0; 
		timelineTransitionEndFrame = 0;
		timelineTransitionLength = 30; 
		timelineStartTransitionStart = 0; 
		timelineStartTransitionTarget = 0;
		timelineEndTransitionStart = 0; 
		timelineEndTransitionTarget = 0;
		transitionScrollIncrement = 2000.f; 
		transitionZoomInIncrement = 0.95f; transitionZoomOutIncrement = 1.052f;	

		System.out.println("Display.reset()... 2");

		/* Library View */
		libraryViewMode = 0;
		currentDisplayCluster = 0;
		
		/* Messages */
		messageStartFrame = -1;
		metadataStartFrame = -1;
		startupMessageStartFrame = -1;
		messageDuration = 60;
		
		messageHUDDistance = hudDistanceInit * 6.f;
		
		messages = new ArrayList<String>();
		metadata = new ArrayList<String>();
		startupMessages = new ArrayList<String>();

		System.out.println("Display.reset()... 3");

		centerTextXOffset = screenWidth / 2.f;
//		leftTextXOffset = 0.f;
		midLeftTextXOffset = screenWidth / 3.f;
//		rightTextXOffset = 0.f;
		midRightTextXOffset = screenWidth / 1.5f;

		topTextYOffset = -screenHeight / 1.6f;
		clusterImageXOffset = -screenWidth/ 1.9f;
		clusterImageYOffset = screenHeight / 2.5f;

		messageXOffset = screenWidth;
		messageYOffset = 0;

		metadataYOffset = -screenHeight / 2.f;

		startupMessageXOffset = screenWidth / 2.f;
		startupMessageYOffset = -screenHeight /2.f;
		
		System.out.println("Display.reset()... 4");
//		map2D = new ML_Map(this);
		map2D.reset();
	}

	/**
	 * Initialize 2D drawing 
	 * @param p Parent world
	 */
	void startHUD(WMV_World p)
	{
		ml.camera();
//		ml.perspective(p.viewer.getFieldOfView(), (float)screenWidth/(float)screenHeight, p.viewer.getSettings().nearClippingDistance, 10000.f);;
//		ml.perspective(p.viewer.getInitFieldOfView(), (float)screenWidth/(float)screenHeight, p.viewer.getSettings().nearClippingDistance, 10000.f);;
	}

	/**
	 * Add message to queue
	 * @param ml Parent app
	 * @param message Message to send
	 */
	void message(MultimediaLocator ml, String message)
	{
		if(ml.state.interactive)
		{
			messages.add(message);
			while(messages.size() > maxMessages)
				messages.remove(0);
		}
		else
		{
			messageStartFrame = ml.world.getState().frameCount;		
			messages.add(message);
			while(messages.size() > maxMessages)
				messages.remove(0);
		}
	}
	
	/**
	 * Get current HUD Distance along Z-Axis
	 * @return Current HUD Z-Axis Distance
	 */
	public float getMessageHUDDistance()
	{
		float distance = messageHUDDistance;
		distance /= (Math.sqrt(ml.world.viewer.getSettings().fieldOfView));
		return distance;
	}
	
	/**
	 * Get current HUD Distance along Z-Axis
	 * @return Current HUD Z-Axis Distance
	 */
	public float getMessageTextSize()
	{
		return largeTextSize * ml.world.viewer.getSettings().fieldOfView * 3.f;
	}
	
	/**
	 * Display current messages
	 * @p Parent world
	 */
	void displayMessages(WMV_World p)
	{
		float xFactor = (float) Math.pow( ml.world.viewer.getSettings().fieldOfView * 12.f, 3) * 0.33f;
		float yFactor = ml.world.viewer.getSettings().fieldOfView * 4.f;
		float xPos = messageXOffset * xFactor; 
		float yPos = messageYOffset * yFactor - lineWidth * yFactor;

		ml.pushMatrix();
		ml.fill(0, 0, 255, 255);  
		
		ml.textFont(messageFont);
		
		float hudDist = getMessageHUDDistance();
		if(ml.state.interactive)
		{
			for(String s : messages)
			{
				yPos += lineWidth * yFactor;
				displayScreenText(ml, s, xPos, yPos += lineWidth * yFactor, getMessageTextSize());
			}
		}
		else if(ml.frameCount - messageStartFrame < messageDuration)
		{
			for(String s : messages)
			{
				yPos += lineWidth * yFactor;
				displayScreenText(ml, s, messageXOffset, yPos, getMessageTextSize());
			}
		}
		else
		{
			clearMessages();														// Clear messages after duration has ended
		}

		ml.popMatrix();
	}

	/**
	 * Add a metadata message (single line) to the display queue
	 * @param curFrameCount Current frame count
	 * @param message Line of metadata 
	 */
	void metadata(int curFrameCount, String message)
	{
		metadataStartFrame = curFrameCount;		
		metadata.add(message);
		
		while(metadata.size() > 16)
			metadata.remove(0);
	}
	
	/**
	 * Draw current metadata messages to the screen
	 * @param p Parent world
	 */
	void displayMetadata(WMV_World p)
	{
		float xFactor = (float) Math.pow( ml.world.viewer.getSettings().fieldOfView * 12.f, 3) * 0.33f;
		float yFactor = ml.world.viewer.getSettings().fieldOfView * 4.f;
		float xPos = metadataXOffset * xFactor; 
		float yPos = metadataYOffset * yFactor - lineWidth * yFactor;

		ml.pushMatrix();

		ml.fill(0, 0, 255, 255);                     // White text
		ml.textSize(mediumTextSize);

		for(String s : metadata)
		{
			yPos += lineWidth * yFactor;
			displayScreenText(ml, s, metadataXOffset, yPos, getMessageTextSize());
		}

		ml.popMatrix();
	}
	
	public void displayScreenText(MultimediaLocator ml, String text, float x, float y, float textSize)
	{
//		System.out.println("Display.displayScreenText()... Text: "+text+" x:"+x+" y:"+y);
		ml.textSize(textSize);
		startHUD(ml.world);
		ml.text(text, x, y, getMessageHUDDistance());		// Use period character to draw a point
//		ml.text(text, x, y, hudDistanceInit);		// Use period character to draw a point
	}
	
	/**
	 * @param message Message to be sent
	 * Add startup message to display queue
	 */
	void sendSetupMessage(WMV_World p, String message)
	{
		if(worldSetup)																
		{
			startupMessageStartFrame = ml.frameCount;		
			startupMessages.add(message);
			while(startupMessages.size() > 16)
				startupMessages.remove(0);

			if(ml.debugSettings.print)
				System.out.println(message);
		}
	}
	
	/**
	 * Display startup 
	 * @param Parent world
	 */
	void displayStartup(WMV_World p, boolean librarySetup)
	{
		float yPos = startupMessageYOffset;

		startHUD(p);
		ml.pushMatrix();
		ml.fill(0, 0, 245.f, 255.f);            								
		ml.textSize(largeTextSize * 1.5f);

		if(worldSetup)												// Showing setup messages + windows
		{
			if(ml.createNewLibrary)
			{
				if(ml.state.chooseMediaFolders)
				{
					ml.text("Please select media folder(s)...", screenWidth / 2.1f, yPos += lineWidthVeryWide * 5.f, hudDistanceInit);
					if(!window.setupImportWindow)
					{
						window.setupImportWindow();
						ml.library = new ML_Library("");		// Create new library
					}
				}
				else if(ml.state.selectedNewLibraryDestination)
				{
					ml.text("Creating library from media folder(s)...", screenWidth / 2.1f, yPos += lineWidthVeryWide * 5.f, hudDistanceInit);
				}
				else
				{
					ml.text("Please select new library destination...", screenWidth / 2.1f, yPos += lineWidthVeryWide * 5.f, hudDistanceInit);
				}
			}
			else
			{
				if(ml.state.startup && !ml.state.selectedLibrary)
				{
					if(!window.setupLibraryWindow)
						window.openLibraryWindow();
					else 
						if(!window.showLibraryWindow)
							window.showLibraryWindow();
					
//					yPos += lineWidthVeryWide * 11.f;
				}
				else
				{
					if(!dataFolderFound)
					{
						window.setLibraryWindowText("Loading media folder(s)...");
//						ml.text("Loading media folder(s)...", screenWidth / 2.1f, yPos += lineWidthVeryWide * 5.f, hudDistance);
					}
					else
					{
						window.setLibraryWindowText("Loading media library...");
//						ml.text("Loading media library...", screenWidth / 2.1f, yPos += lineWidthVeryWide * 5.f, hudDistance);
					}
					
//					yPos += lineWidthVeryWide * 8.f;
				}
			}
			ml.textSize(largeTextSize);
//			p.p.text("For support and the latest updates, visit: www.spatializedmusic.com/MultimediaLocator", screenWidth / 2.f, yPos, hudDistance);
		}
		else
		{
			displayMessages(p);
		}

		ml.popMatrix();
	}
	
	/**
	 * Draw Library View
	 * @param p Parent world
	 */
	void displayLibraryView(WMV_World p)
	{
		WMV_Field f = p.getCurrentField();
		if(currentDisplayCluster < 0 || currentDisplayCluster >= f.getClusters().size())
		{
			System.out.println("Fixed currentDisplayCluster out of range! was: "+currentDisplayCluster+" getClusters().size():"+f.getClusters().size());
			currentDisplayCluster = 0;
		}

		WMV_Cluster c;
		float xPos = centerTextXOffset;
		float yPos = topTextYOffset;			// Starting vertical position

		switch(libraryViewMode)
		{
			case 0:														// Fields
				startHUD(p);
				if(initializedMaps) map2D.displayWorldMap(p);
				break;
			case 1:														// Field
				startHUD(p);
				ml.pushMatrix();
				ml.fill(0, 0, 255, 255);
				ml.textSize(veryLargeTextSize);
				ml.text(""+p.getCurrentField().getName(), xPos, yPos, hudDistanceInit);
				c = p.getCurrentCluster();

				ml.textSize(largeTextSize);
				ml.text(" Current Field #"+ f.getID()+" of "+ p.getFields().size(), xPos, yPos += lineWidthVeryWide, hudDistanceInit);

				if(c != null)
				{
					ml.textSize(largeTextSize);
					ml.text(" Current Cluster #"+ c.getID()+" of "+ f.getClusters().size(), xPos, yPos += lineWidthWide, hudDistanceInit);
				}		

				ml.textSize(mediumTextSize);
				ml.text(" Field Cluster Count:"+(f.getClusters().size()), xPos, yPos += lineWidthVeryWide, hudDistanceInit);
				ml.text("   Merged: "+f.getModel().getState().mergedClusters+" out of "+(f.getModel().getState().mergedClusters+f.getClusters().size())+" Total", xPos, yPos += lineWidth, hudDistanceInit);
				if(p.getState().hierarchical) ml.text(" Current Cluster Depth: "+f.getState().clusterDepth, xPos, yPos += lineWidth, hudDistanceInit);
				ml.text("   Minimum Distance: "+p.settings.minClusterDistance, xPos, yPos += lineWidth, hudDistanceInit);
				ml.text("   Maximum Distance: "+p.settings.maxClusterDistance, xPos, yPos += lineWidth, hudDistanceInit);
				ml.text("   Population Factor: "+f.getModel().getState().clusterPopulationFactor, xPos, yPos += lineWidth, hudDistanceInit);

				if(f.getDateline() != null)
				{
					ml.textSize(mediumTextSize);
					if(f.getDateline().size() > 0)
					{
						int fieldDate = p.getCurrentField().getTimeSegment(p.viewer.getCurrentFieldTimeSegment()).getFieldDateID();
						ml.text(" Current Time Segment", xPos, yPos += lineWidthWide, hudDistanceInit);
						ml.text("   ID: "+ p.viewer.getCurrentFieldTimeSegment()+" of "+ p.getCurrentField().getTimeline().timeline.size() +" in Main Timeline", xPos, yPos += lineWidthWide, hudDistanceInit);
						ml.text("   Date: "+ (fieldDate)+" of "+ p.getCurrentField().getDateline().size(), xPos, yPos += lineWidth, hudDistanceInit);
						ml.text("   Date-Specific ID: "+ p.getCurrentField().getTimeSegment(p.viewer.getCurrentFieldTimeSegment()).getFieldTimelineIDOnDate()
								+" of "+ p.getCurrentField().getTimelines().get(fieldDate).timeline.size() + " in Timeline #"+(fieldDate), xPos, yPos += lineWidth, hudDistanceInit);
					}
				}
				ml.popMatrix();

				map2D.displaySmallBasicMap(p);
				break;
				
			case 2:								// Cluster
				startHUD(p);
				ml.pushMatrix();
				ml.fill(0, 0, 255, 255);
				ml.textSize(veryLargeTextSize);
				ml.text(""+p.getCurrentField().getName(), xPos, yPos, hudDistanceInit);
	
				ml.textSize(largeTextSize);
				c = f.getCluster(currentDisplayCluster);	// Get the cluster to display info about
				WMV_Cluster cl = p.getCurrentCluster();
				ml.text(" Cluster #"+ c.getID() + ((c.getID() == cl.getID())?" (Current Cluster)":""), xPos, yPos += lineWidthVeryWide, hudDistanceInit);
				ml.textSize(mediumTextSize);
				if(c.getState().images.size() > 0)
					ml.text("   Images: "+ c.getState().images.size(), xPos, yPos += lineWidthVeryWide, hudDistanceInit);
				if(c.getState().panoramas.size() > 0)
					ml.text("   Panoramas: "+ c.getState().panoramas.size(), xPos, yPos += lineWidthVeryWide, hudDistanceInit);
				if(c.getState().videos.size() > 0)
					ml.text("   Videos: "+ c.getState().videos.size(), xPos, yPos += lineWidthVeryWide, hudDistanceInit);
				if(c.getState().sounds.size() > 0)
					ml.text("     Sounds: "+ c.getState().sounds.size(), xPos, yPos += lineWidthVeryWide, hudDistanceInit);
				ml.text("   Total Count: "+ c.getState().mediaCount, xPos, yPos += lineWidthVeryWide, hudDistanceInit);
				ml.text("   Location: "+ c.getLocation(), xPos, yPos += lineWidthVeryWide, hudDistanceInit);
				ml.text("   Viewer Distance: "+PApplet.round(PVector.dist(c.getLocation(), p.viewer.getLocation())), xPos, yPos += lineWidth, hudDistanceInit);
				ml.text(" ", xPos, yPos += lineWidth, hudDistanceInit);
				ml.text("   Media Segments: "+ c.segments.size(), xPos, yPos += lineWidth, hudDistanceInit);
	
				if(c.getTimeline().timeline.size() > 0)
				{
					ml.text(" Timeline Segments: "+ c.getTimeline().timeline.size(), xPos, yPos += lineWidthWide, hudDistanceInit);
					ml.text(" Timeline Length (sec.): "+ utilities.getTimelineLength(c.getTimeline().timeline), xPos, yPos += lineWidth, hudDistanceInit);
				}
				if(c.getDateline() != null)
					if(c.getDateline().size() > 0)
						ml.text(" Timeline Dates: "+ c.getDateline().size(), xPos, yPos += lineWidth, hudDistanceInit);
	
				if(cl != null)
					ml.text("   Stitched Panoramas: "+cl.stitched.size(), xPos, yPos += lineWidth, hudDistanceInit);
	
				if(p.viewer.getAttractorClusterID() != -1)
				{
					ml.text(" Destination Cluster ID: "+p.viewer.getAttractorCluster(), xPos, yPos += lineWidth, hudDistanceInit);
					ml.text("    Destination Distance: "+PApplet.round( PVector.dist(f.getClusters().get(p.viewer.getAttractorClusterID()).getLocation(), p.viewer.getLocation() )), xPos, yPos += lineWidth, hudDistanceInit);
					if(ml.debugSettings.viewer) 
					{
						ml.text(" Debug: Current Attraction:"+p.viewer.getAttraction().mag(), xPos, yPos += lineWidth, hudDistanceInit);
						ml.text(" Debug: Current Acceleration:"+p.viewer.getAcceleration().mag(), xPos, yPos += lineWidth, hudDistanceInit);
						ml.text(" Debug: Current Velocity:"+ p.viewer.getVelocity().mag() , xPos, yPos += lineWidth, hudDistanceInit);
					}
				}
				
				drawClusterImages(p, c.getImages(p.getCurrentField().getImages()));
				ml.popMatrix();
				
				map2D.displaySmallBasicMap(p);
				break;
		}
	}
	
	/**
	 * Set current object viewable in Media View
	 * @param mediaType Media type
	 * @param mediaID Media ID
	 */
	public void setMediaViewObject(int mediaType, int mediaID)
	{
		mediaViewMediaType = mediaType;
		mediaViewMediaID = mediaID;
		if(mediaType == 2)
		{
			WMV_Video v = ml.world.getCurrentField().getVideo(mediaID);
			if(!v.isLoaded())
				v.loadMedia(ml);
			if(!v.isPlaying())
				v.play();
		}
	}
	
	/**
	 * Display selected media centered in window at full brightness
	 * @param p Parent world
	 */
	private void displayMediaView(WMV_World p)
	{
		if(mediaViewMediaType > -1 && mediaViewMediaID > -1)
		{
			switch(mediaViewMediaType)
			{
				case 0:
					displayImage2D(p.getCurrentField().getImage(mediaViewMediaID));
					break;
				case 1:
					displayPanorama2D(p.getCurrentField().getPanorama(mediaViewMediaID));
					break;
				case 2:
					displayVideo2D(p.getCurrentField().getVideo(mediaViewMediaID));
					break;
//				case 3:
//					displaySound2D(p.getCurrentField().getSound(mediaViewMediaID));		
//					break;
			}
		}
	}
	
	/**
	 * Display image in Media View
	 * @param image Image to display
	 */
	private void displayImage2D(WMV_Image image)
	{
		image.display2D(ml);
	}
	
	/**
	 * Display 360-degree panorama texture in Media View
	 * @param panorama Panorama to display
	 */
	private void displayPanorama2D(WMV_Panorama panorama)
	{
		panorama.display2D(ml);
	}

	/**
	 * Display video in Media View
	 * @param video Video to display
	 */
	private void displayVideo2D(WMV_Video video)
	{
		video.display2D(ml);
	}

	/**
	 * Draw thumbnails (grid) of image list
	 * @param p Parent world
	 * @param imageList Images in cluster
	 */
	private void drawClusterImages(WMV_World p, ArrayList<WMV_Image> imageList)
	{
		int count = 1;
		float imgXPos = clusterImageXOffset;
		float imgYPos = clusterImageYOffset;			// Starting vertical position

		ml.stroke(255, 255, 255);
		ml.strokeWeight(15);
		ml.fill(0, 0, 255, 255);

		for(WMV_Image i : imageList)
		{
			ml.pushMatrix();
			float origWidth = i.getWidth();
			float origHeight = i.getHeight();
			float width = 120.f;
			float height = width * origHeight / origWidth;
			
			ml.translate(imgXPos, imgYPos, hudDistanceInit);
			ml.tint(255);
			
			if(count < 60)
			{
				PImage image = ml.loadImage(i.getFilePath());
				ml.image(image, 0, 0, width, height);
			}
			
			imgXPos += width * 1.5f;

			if(count % 20 == 0)
			{
				imgXPos = clusterImageXOffset;
				imgYPos += height * 1.5f;
			}
			
			ml.popMatrix();
			count++;
		}
	}

	/**
	 * @return Whether current view mode is a 2D display mode (true) or 3D World View (false)
	 */
	public boolean inDisplayView()
	{
		if( displayView != 0 )
			return true;
		else 
			return false;
	}
	
	/**
	 * Set display view
	 * @param p Parent world
	 * @param newDisplayView New display view
	 */
	public void setDisplayView(WMV_World p, int newDisplayView)
	{
		displayView = newDisplayView;
		System.out.println("Display.setDisplayView()... displayView:"+displayView);
		switch(newDisplayView)
		{
			case 0:													// World View
				if(window.setupMLWindow)
				{
					window.optWorldView.setSelected(true);
					window.optMapView.setSelected(false);
					window.optTimelineView.setSelected(false);
				}
				break;
			case 1:													// Map View
				if(!initializedMaps) map2D.initialize(p);
				map2D.largeMarkerManager.enableDrawing();
				map2D.smallMarkerManager.enableDrawing();
				map2D.zoomToField(p, p.getCurrentField(), false);
				if(window.setupMLWindow)
				{
					window.optWorldView.setSelected(false);
					window.optMapView.setSelected(true);
					window.optTimelineView.setSelected(false);
				}
				break;
			case 2:													// Time View
				if(window.setupMLWindow)
				{
					window.optWorldView.setSelected(false);
					window.optMapView.setSelected(false);
					window.optTimelineView.setSelected(true);
				}
				break;
			case 3:													// Library View (Disabled)
				if(!initializedMaps) map2D.initialize(p);
				map2D.initializeWorldMap(p, false);
				map2D.satelliteMarkerManager.enableDrawing();
				map2D.largeMarkerManager.disableDrawing();
				map2D.smallMarkerManager.disableDrawing();
				currentDisplayCluster = p.viewer.getState().getCurrentClusterID();
				break;
			case 4:													// Media View
//				-- Start video playing
				break;
		}
	}

	/**
	 * Clear previous messages
	 */
	void clearMessages()
	{
		messages = new ArrayList<String>();			
	}
	
	/**
	 * Clear previous metadata messages
	 */
	void clearMetadata()
	{
		metadata = new ArrayList<String>();							// Reset message list
	}

	/**
	 * Clear previous setup messages
	 */
	void clearSetupMessages()
	{
		startupMessages = new ArrayList<String>();
	}
	
	/**
	 * Reset display modes and clear messages
	 */
	void resetDisplayModes()
	{
		displayView = 0;
		clearMessages();
		clearMetadata();
	}

	/** 
	 * Get associated selectable time segment ID for given time segment
	 * @param t Given time segment
	 * @return Selectable time segment ID associated with given time segment
	 */
	private int getSelectableTimeIDOfFieldTimeSegment(WMV_TimeSegment t)
	{
		for(SelectableTimeSegment st : selectableTimeSegments)
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
	 * Set current selectable date (white rectangle)
	 * @param newSelectableDate New selectable date ID
	 */
	private void setCurrentSelectableDate(int newSelectableDate)
	{
		displayDate = newSelectableDate;
		currentSelectableDate = newSelectableDate;
		updateCurrentSelectableTimeSegment = true;
		updateCurrentSelectableDate = false;
		updateFieldTimeline = true;
	}

	/**
	 * Selectable time segment on Time View timeline
	 * @author davidgordon
	 */
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
		
		 /**
		  * Increment blendMode by given amount and call setBlendMode()
		  * @param inc Increment to blendMode number
		  */
		public void changeBlendMode(WMV_World p, int inc) 
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
				setBlendMode(p, blendMode);
		}

		/**
		 * Change effect of image alpha channel on blending
		 * @param blendMode
		 */
		public void setBlendMode(WMV_World p, int blendMode) {
			switch (blendMode) {
			case 0:
				ml.blendMode(PApplet.BLEND);
				break;

			case 1:
				ml.blendMode(PApplet.ADD);
				break;

			case 2:
				ml.blendMode(PApplet.SUBTRACT);
				break;

			case 3:
				ml.blendMode(PApplet.DARKEST);
				break;

			case 4:
				ml.blendMode(PApplet.LIGHTEST);
				break;

			case 5:
				ml.blendMode(PApplet.DIFFERENCE);
				break;

			case 6:
				ml.blendMode(PApplet.EXCLUSION);
				break;

			case 7:
				ml.blendMode(PApplet.MULTIPLY);
				break;

			case 8:
				ml.blendMode(PApplet.SCREEN);
				break;

			case 9:
				ml.blendMode(PApplet.REPLACE);
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

			if (ml.debugSettings.world)
				System.out.println("blendMode:" + blendMode);
		}


		public int getID()
		{
			return id;
		}
		
		public int getClusterID()
		{
			return clusterID;
		}
		
		public void display(WMV_World p, float hue, float saturation, float brightness, boolean preview)
		{
			ml.stroke(hue, saturation, brightness, 255);												// Yellow rectangle around selected time segment
			ml.strokeWeight(3.f);

			ml.pushMatrix();
			ml.line(leftEdge, topEdge, hudDistanceInit, leftEdge, bottomEdge, hudDistanceInit);	
			ml.line(rightEdge, topEdge, hudDistanceInit, rightEdge, bottomEdge, hudDistanceInit);			
			ml.line(leftEdge, topEdge, hudDistanceInit, rightEdge, topEdge, hudDistanceInit);			
			ml.line(leftEdge, bottomEdge, hudDistanceInit, rightEdge, bottomEdge, hudDistanceInit);			

			if(preview)
			{
				ml.fill(hue, saturation, brightness, 255);												// Yellow rectangle around selected time segment
				ml.textSize(smallTextSize);
				String strTimespan = segment.getTimespanAsString(false, false);
				String strPreview = String.valueOf( segment.getTimeline().size() ) + " media, "+strTimespan;

				float length = timelineEnd - timelineStart;
				float day = utilities.getTimePVectorSeconds(new PVector(24,0,0));		// Seconds in a day
				float xOffset = -35.f * utilities.mapValue(length, 0.f, day, 0.2f, 1.f);
				ml.text(strPreview, (rightEdge+leftEdge)/2.f + xOffset, bottomEdge + 25.f, hudDistanceInit);
			}
			ml.popMatrix();
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
		
		public void display(WMV_World p, float hue, float saturation, float brightness, boolean preview)
		{
			ml.pushMatrix();
			
			ml.stroke(hue, saturation, brightness, 255.f);
			ml.strokeWeight(25.f);
			ml.point(location.x, location.y, location.z);

			if(preview)
			{
				ml.fill(hue, saturation, brightness, 255);												// Yellow rectangle around selected time segment
				ml.textSize(smallTextSize);
				String strDate = date.getDateAsString();
//				String strPreview = String.valueOf( segment.getTimeline().size() ) + " media, "+strTimespan;
				ml.text(strDate, location.x - 25.f, location.y + 50.f, location.z);
			}
		
			ml.popMatrix();
		}
	}

	public void showAllDates()
	{
		displayDate = -1;
		selectedDate = -1;
		currentSelectableDate = -1;
		updateFieldTimeline = true;
	}
	
	public int getSelectedCluster()
	{
		return selectedCluster;	
	}
	
	public int getCurrentSelectableTimeSegment()
	{
		return currentSelectableTimeSegmentID;
	}
	
	/**
	 * Display the main key commands on screen
	 */
//	void displayControls(WMV_World p)
//	{
//		startHUD(p);
//		p.p.pushMatrix();
//		
//		float xPos = centerTextXOffset;
//		float yPos = topTextYOffset;			// Starting vertical position
//		
//		p.p.fill(0, 0, 255, 255);                        
//		p.p.textSize(largeTextSize);
//		p.p.text(" Keyboard Controls ", xPos, yPos, hudDistance);
//
//		xPos = midLeftTextXOffset;
//		p.p.textSize(mediumTextSize);
//		p.p.text(" Main", xPos, yPos += lineWidthVeryWide, hudDistance);
//		p.p.textSize(smallTextSize);
//		p.p.text(" R    Restart MultimediaLocator", xPos, yPos += lineWidthVeryWide, hudDistance);
//		p.p.text(" CMD + q    Quit MultimediaLocator", xPos, yPos += lineWidth, hudDistance);
//
//		p.p.textSize(mediumTextSize);
//		p.p.text(" Display", xPos, yPos += lineWidthVeryWide, hudDistance);
//		p.p.textSize(smallTextSize);
//		p.p.text(" 1    Show/Hide Field Map   		  +SHIFT to Overlay", xPos, yPos += lineWidthVeryWide, hudDistance);
//		p.p.text(" 2    Show/Hide Field Statistics    +SHIFT to Overlay", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" 3    Show/Hide Cluster Statistics  +SHIFT to Overlay", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" 4    Show/Hide Keyboard Controls   +SHIFT to Overlay", xPos, yPos += lineWidth, hudDistance);
//
//		p.p.textSize(mediumTextSize);
//		p.p.text(" Time", xPos, yPos += lineWidthVeryWide, hudDistance);
//		p.p.textSize(smallTextSize);
//		p.p.text(" T    Time Fading On/Off", xPos, yPos += lineWidthVeryWide, hudDistance);
//		p.p.text(" D    Date Fading On/Off", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" space Pause On/Off   ", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" &/*  Default Media Length - / +", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" SHIFT + Lt/Rt   Cycle Length - / +", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" SHIFT + Up/Dn   Current Time - / +", xPos, yPos += lineWidth, hudDistance);
//
//		p.p.textSize(mediumTextSize);
//		p.p.text(" Time Navigation", xPos, yPos += lineWidthVeryWide, hudDistance);
//		p.p.textSize(smallTextSize);
//		p.p.text(" t    Teleport to Earliest Time in Field", xPos, yPos += lineWidthVeryWide, hudDistance);
//		p.p.text(" T    Move to Earliest Time in Field", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" d    Teleport to Earliest Time on Earliest Date", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" D    Move to Earliest Time on Earliest Date", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" n    Move to Next Time Segment in Field", xPos, yPos += lineWidthWide, hudDistance);
//		p.p.text(" N    Move to Next Time Segment in Cluster", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" b    Move to Previous Time Segment in Field", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" B    Move to Previous Time Segment in Cluster", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" l    Move to Next Date in Field", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" L    Move to Next Date in Cluster", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" k    Move to Previous Date in Field", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" K    Move to Previous Date in Cluster", xPos, yPos += lineWidth, hudDistance);
//
//		xPos = centerTextXOffset;
//		yPos = topTextYOffset;			// Starting vertical position
//
//		/* Model */
//		p.p.textSize(mediumTextSize);
//		p.p.text(" Model", xPos, yPos += lineWidthVeryWide, hudDistance);
//		p.p.textSize(smallTextSize);
//		p.p.text(" [ ]  Altitude Scaling Adjustment  + / - ", xPos, yPos += lineWidthVeryWide, hudDistance);
////		p.p.text(" , .  Object Distance  + / - ", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" - =  Object Distance  - / +      ", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" OPTION + -   Visible Angle  -      ", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" OPTION + =   Visible Angle  +      ", xPos, yPos += lineWidth, hudDistance);
//		
//		/* Graphics */
//		p.p.textSize(mediumTextSize);
//		p.p.text(" Graphics", xPos, yPos += lineWidthVeryWide, hudDistance);
//		p.p.textSize(smallTextSize);
//		p.p.text(" G    Angle Fading On/Off", xPos, yPos += lineWidthVeryWide, hudDistance);
//		p.p.text(" H    Angle Thinning On/Off", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" P    Transparency Mode  On / Off      ", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" ( )  Blend Mode  - / +      ", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" i h v  Hide images / panoramas / videos    ", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" D    Video Mode On/Off ", xPos, yPos += lineWidth, hudDistance);
//
//		/* Movement */
//		p.p.textSize(mediumTextSize);
//		p.p.text(" Movement", xPos, yPos += lineWidthVeryWide, hudDistance);
//		p.p.textSize(smallTextSize);
//		p.p.text(" a d w s   Walk Left / Right / Forward / Backward ", xPos, yPos += lineWidthVeryWide, hudDistance);
//		p.p.text(" Arrows    Turn Camera ", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" q z  Zoom In / Out + / - ", xPos, yPos += lineWidth, hudDistance);
//		
//		/* Navigation */
//		p.p.textSize(mediumTextSize);
//		p.p.text(" Navigation", xPos, yPos += lineWidthVeryWide, hudDistance);
//		p.p.textSize(smallTextSize);
//		p.p.text(" >    Follow Timeline Only", xPos, yPos += lineWidthVeryWide, hudDistance);
//		p.p.text(" .    Follow Timeline by Date", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" OPTION + .    Follow Dateline Only", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" E    Move to Nearest Cluster", xPos, yPos += lineWidthWide, hudDistance);
//		p.p.text(" W    Move to Nearest Cluster in Front", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" Q    Move to Next Cluster in Time", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" A    Move to Next Location in Memory", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" Z    Move to Random Cluster", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" U    Move to Next Video ", xPos, yPos += lineWidthWide, hudDistance);
//		p.p.text(" u    Teleport to Next Video ", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" M    Move to Next Panorama ", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" m    Teleport to Next Panorama ", xPos, yPos += lineWidth, hudDistance);
////		p.p.text(" C    Lock Viewer to Nearest Cluster On/Off", xPos, yPos += lineWidthWide, hudDistance);
////		p.p.text(" l    Look At Selected Media", xPos, yPos += lineWidth, hudDistance);
////		p.p.text(" L    Look for Media", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" { }  Teleport to Next / Previous Field ", xPos, yPos += lineWidth, hudDistance);
//
//		xPos = midRightTextXOffset;
//		yPos = topTextYOffset;			// Starting vertical position
//
//		p.p.textSize(mediumTextSize);
//		p.p.text(" Interaction", xPos, yPos += lineWidthVeryWide, hudDistance);
//		p.p.textSize(smallTextSize);
//		p.p.text(" O    Selection Mode On/Off", xPos, yPos += lineWidthVeryWide, hudDistance);
//		p.p.text(" S    Multi-Selection Mode On/Off", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" OPTION + s    Segment Selection Mode On/Off", xPos, yPos += lineWidthWide, hudDistance);
//		p.p.text(" x    Select Media in Front", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" X    Deselect Media in Front", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" OPTION + x    Deselect All Media", xPos, yPos += lineWidth, hudDistance);
//
//		p.p.textSize(mediumTextSize);
//		p.p.text(" GPS Tracks", xPos, yPos += lineWidthVeryWide, hudDistance);
//		p.p.textSize(smallTextSize);
//		p.p.text(" g    Load GPS Track from File", xPos, yPos += lineWidthVeryWide, hudDistance);
//		p.p.text(" OPTION + g    Follow GPS Track", xPos, yPos += lineWidth, hudDistance);
//
//		p.p.textSize(mediumTextSize);
//		p.p.text(" Memory", xPos, yPos += lineWidthVeryWide, hudDistance);
//		p.p.textSize(smallTextSize);
//		p.p.text(" `    Save Current View to Memory", xPos, yPos += lineWidthVeryWide, hudDistance);
//		p.p.text(" ~    Follow Memory Path", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" Y    Clear Memory", xPos, yPos += lineWidth, hudDistance);
//
//		p.p.textSize(mediumTextSize);
//		p.p.text(" Output", xPos, yPos += lineWidthVeryWide, hudDistance);
//		p.p.textSize(smallTextSize);
//		p.p.text(" o    Set Image Output Folder", xPos, yPos += lineWidthVeryWide, hudDistance);
//		p.p.text(" p    Save Screen Image to Disk", xPos, yPos += lineWidth, hudDistance);
//
//		p.p.popMatrix();
//	}
	
	/**
	 * Show statistics of the current simulation
	 */
//	void displayStatisticsView(WMV_World p)
//	{
//		startHUD(p);
//		p.p.pushMatrix();
//		
//		float xPos = centerTextXOffset;
//		float yPos = topTextYOffset;			// Starting vertical position
//		
//		WMV_Field f = p.getCurrentField();
//		
//		if(p.viewer.getState().getCurrentClusterID() >= 0)
//		{
//			WMV_Cluster c = p.getCurrentCluster();
////			float[] camTar = p.viewer.camera.target();
//
//			p.p.fill(0, 0, 255, 255);
//			p.p.textSize(largeTextSize);
//			p.p.text(" MultimediaLocator v0.9 ", xPos, yPos, hudDistance);
//			p.p.textSize(mediumTextSize);
//
//			xPos = midLeftTextXOffset;
//			
//			p.p.text(" Program Modes ", xPos, yPos += lineWidthVeryWide, hudDistance);
//			p.p.textSize(smallTextSize);
//			p.p.text(" Orientation Mode: "+p.viewer.getSettings().orientationMode, xPos, yPos += lineWidthVeryWide, hudDistance);
//			p.p.text(" Alpha Mode:"+p.getState().alphaMode, xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Time Fading: "+ p.getState().timeFading, xPos, yPos += lineWidth, hudDistance);
////			p.p.text(" Date Fading: "+ p.dateFading, xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Altitude Scaling: "+p.settings.altitudeScaling, xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Lock Media to Clusters:"+p.getState().lockMediaToClusters, xPos, yPos += lineWidth, hudDistance);
//		
//			p.p.textSize(mediumTextSize);
//			p.p.text(" Graphics ", xPos, yPos += lineWidthVeryWide, hudDistance);
//			p.p.textSize(smallTextSize);
//			p.p.text(" Alpha:"+p.getState().alpha, xPos, yPos += lineWidthVeryWide, hudDistance);
//			p.p.text(" Default Media Length:"+p.settings.defaultMediaLength, xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Media Angle Fading: "+p.viewer.getSettings().angleFading, xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Media Angle Thinning: "+p.viewer.getSettings().angleThinning, xPos, yPos += lineWidth, hudDistance);
//			if(p.viewer.getSettings().angleThinning)
//				p.p.text(" Media Thinning Angle:"+p.viewer.getSettings().thinningAngle, xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Image Size Factor:"+p.settings.subjectSizeRatio, xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Subject Distance (m.):"+p.settings.defaultFocusDistance, xPos, yPos += lineWidth, hudDistance);
////			p.p.text(" Image Size Factor:"+p.subjectSizeRatio, xPos, yPos += lineWidth, hudDistance);
//
//			xPos = centerTextXOffset;
//			yPos = topTextYOffset;			// Starting vertical position
//
//			p.p.textSize(mediumTextSize);
//			p.p.text(" Field", xPos, yPos += lineWidthVeryWide, hudDistance);
//			p.p.textSize(smallTextSize);
//			p.p.text(" Name: "+f.getName(), xPos, yPos += lineWidthVeryWide, hudDistance);
//			p.p.text(" ID: "+(p.viewer.getState().getField()+1)+" out of "+p.getFieldCount()+" Total Fields", xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Width (m.): "+f.getModel().getState().fieldWidth+" Length (m.): "+f.getModel().getState().fieldLength+" Height (m.): "+f.getModel().getState().fieldHeight, xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Total Media: "+f.getMediaCount(), xPos, yPos += lineWidth, hudDistance);					// Doesn't check for dataMissing!!
//			p.p.text(" Total Images: "+f.getImageCount(), xPos, yPos += lineWidth, hudDistance);					// Doesn't check for dataMissing!!
//			p.p.text(" Total Panoramas: "+f.getPanoramaCount(), xPos, yPos += lineWidth, hudDistance);			// Doesn't check for dataMissing!!
//			p.p.text(" Total Videos: "+f.getVideoCount(), xPos, yPos += lineWidth, hudDistance);					// Doesn't check for dataMissing!!
//			p.p.text(" Total Sounds: "+f.getSoundCount(), xPos, yPos += lineWidth, hudDistance);					// Doesn't check for dataMissing!!
//			p.p.text(" Media Density per sq. m.: "+f.getModel().getState().mediaDensity, xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Images Visible: "+f.getImagesVisible(), xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Images Seen: "+f.getImagesSeen(), xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Panoramas Visible: "+f.getPanoramasVisible(), xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Panoramas Seen: "+f.getPanoramasSeen(), xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Videos Visible: "+f.getVideosVisible(), xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Videos Seen: "+f.getVideosSeen(), xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Videos Playing: "+f.getVideosPlaying(), xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Videos Loaded: "+f.getVideosLoaded(), xPos, yPos += lineWidth, hudDistance);
////			p.p.text(" Sounds Audible: "+f.getSoundsAudible(), xPos, yPos += lineWidth, hudDistance);
////			p.p.text(" Sounds Playing: "+f.getSoundsPlaying(), xPos, yPos += lineWidth, hudDistance);
////			p.p.text(" Sounds Loaded: "+f.getSoundsLoaded(), xPos, yPos += lineWidth, hudDistance);
//			if(p.viewer.getSettings().orientationMode)
//				p.p.text(" Clusters Visible: "+p.viewer.getState().getClustersVisible()+"  (Orientation Mode)", xPos, yPos += lineWidth, hudDistance);
//
//			p.p.textSize(mediumTextSize);
//			p.p.text(" Model ", xPos, yPos += lineWidthVeryWide, hudDistance);
//			p.p.textSize(smallTextSize);
//			
//			p.p.text(" Clusters:"+(f.getClusters().size()-f.getModel().getState().mergedClusters), xPos, yPos += lineWidthVeryWide, hudDistance);
//			p.p.text(" Merged: "+f.getModel().getState().mergedClusters+" out of "+f.getClusters().size()+" Total", xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Minimum Distance: "+p.settings.minClusterDistance, xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Maximum Distance: "+p.settings.maxClusterDistance, xPos, yPos += lineWidth, hudDistance);
//			if(p.settings.altitudeScaling)
//				p.p.text(" Altitude Scaling Factor: "+p.settings.altitudeScalingFactor+"  (Altitude Scaling)", xPos, yPos += lineWidthVeryWide, hudDistance);
//			p.p.text(" Clustering Method : "+ ( p.getState().hierarchical ? "Hierarchical" : "K-Means" ), xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Population Factor: "+f.getModel().getState().clusterPopulationFactor, xPos, yPos += lineWidth, hudDistance);
//			if(p.getState().hierarchical) p.p.text(" Current Cluster Depth: "+f.getState().clusterDepth, xPos, yPos += lineWidth, hudDistance);
//
//			p.p.textSize(mediumTextSize);
//			p.p.text(" Viewer ", xPos, yPos += lineWidthVeryWide, hudDistance);
//			p.p.textSize(smallTextSize);
//			p.p.text(" Location, x: "+PApplet.round(p.viewer.getLocation().x)+" y:"+PApplet.round(p.viewer.getLocation().y)+" z:"+
//					 PApplet.round(p.viewer.getLocation().z), xPos, yPos += lineWidthVeryWide, hudDistance);		
//			p.p.text(" GPS Longitude: "+p.viewer.getGPSLocation().x+" Latitude:"+p.viewer.getGPSLocation().y, xPos, yPos += lineWidth, hudDistance);		
//
//			p.p.text(" Current Cluster: "+p.viewer.getState().getCurrentClusterID(), xPos, yPos += lineWidthVeryWide, hudDistance);
//			p.p.text("   Media Points: "+c.getState().mediaCount, xPos, yPos += lineWidth, hudDistance);
//			p.p.text("   Media Segments: "+p.getCurrentCluster().segments.size(), xPos, yPos += lineWidth, hudDistance);
//			p.p.text("   Distance: "+PApplet.round(PVector.dist(c.getLocation(), p.viewer.getLocation())), xPos, yPos += lineWidth, hudDistance);
//			p.p.text("   Stitched Panoramas: "+p.getCurrentCluster().stitched.size(), xPos, yPos += lineWidth, hudDistance);
//			if(p.viewer.getAttractorClusterID() != -1)
//			{
//				p.p.text(" Destination Cluster : "+p.viewer.getAttractorCluster(), xPos, yPos += lineWidth, hudDistance);
//				p.p.text(" Destination Media Points: "+p.getCurrentField().getCluster(p.viewer.getAttractorClusterID()).getState().mediaCount, xPos, yPos += lineWidth, hudDistance);
//				p.p.text("    Destination Distance: "+PApplet.round( PVector.dist(f.getClusters().get(p.viewer.getAttractorClusterID()).getLocation(), p.viewer.getLocation() )), xPos, yPos += lineWidth, hudDistance);
//			}
//
//			if(p.p.debugSettings.viewer) 
//			{
//				p.p.text(" Debug: Current Attraction: "+p.viewer.getAttraction().mag(), xPos, yPos += lineWidth, hudDistance);
//				p.p.text(" Debug: Current Acceleration: "+p.viewer.getAcceleration().mag(), xPos, yPos += lineWidth, hudDistance);
//				p.p.text(" Debug: Current Velocity: "+ p.viewer.getVelocity().mag() , xPos, yPos += lineWidth, hudDistance);
//				p.p.text(" Debug: Moving? " + p.viewer.getState().isMoving(), xPos, yPos += lineWidth, hudDistance);
//				p.p.text(" Debug: Slowing? " + p.viewer.isSlowing(), xPos, yPos += lineWidth, hudDistance);
//				p.p.text(" Debug: Halting? " + p.viewer.isHalting(), xPos, yPos += lineWidth, hudDistance);
//			}
//
//			if(p.p.debugSettings.viewer)
//			{
//				p.p.text(" Debug: X Orientation (Yaw):" + p.viewer.getXOrientation(), xPos, yPos += lineWidth, hudDistance);
//				p.p.text(" Debug: Y Orientation (Pitch):" + p.viewer.getYOrientation(), xPos, yPos += lineWidth, hudDistance);
////				p.p.text(" Debug: Target Point x:" + camTar[0] + ", y:" + camTar[1] + ", z:" + camTar[2], xPos, yPos += lineWidth, hudDistance);
//			}
//			else
//			{
//				p.p.text(" Compass Direction:" + utilities.angleToCompass(p.viewer.getXOrientation())+" Angle: "+p.viewer.getXOrientation(), xPos, yPos += lineWidth, hudDistance);
//				p.p.text(" Vertical Direction:" + PApplet.degrees(p.viewer.getYOrientation()), xPos, yPos += lineWidth, hudDistance);
//				p.p.text(" Zoom:"+p.viewer.getFieldOfView(), xPos, yPos += lineWidth, hudDistance);
//			}
//			p.p.text(" Field of View:"+p.viewer.getFieldOfView(), xPos, yPos += lineWidth, hudDistance);
//
//			xPos = midRightTextXOffset;
//			yPos = topTextYOffset;			// Starting vertical position
//
//			p.p.textSize(mediumTextSize);
//			p.p.text(" Time ", xPos, yPos += lineWidthVeryWide, hudDistance);
//			p.p.textSize(smallTextSize);
//			p.p.text(" Time Mode: "+ ((p.p.world.getState().getTimeMode() == 0) ? "Cluster" : "Field"), xPos, yPos += lineWidthVeryWide, hudDistance);
//			
//			if(p.p.world.getState().getTimeMode() == 0)
//				p.p.text(" Current Field Time: "+ p.getState().currentTime, xPos, yPos += lineWidth, hudDistance);
//			if(p.p.world.getState().getTimeMode() == 1)
//				p.p.text(" Current Cluster Time: "+ p.getCurrentCluster().getState().currentTime, xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Current Field Timeline Segments: "+ p.getCurrentField().getTimeline().timeline.size(), xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Current Field Time Segment: "+ p.viewer.getCurrentFieldTimeSegment(), xPos, yPos += lineWidth, hudDistance);
//			if(f.getTimeline().timeline.size() > 0 && p.viewer.getCurrentFieldTimeSegment() >= 0 && p.viewer.getCurrentFieldTimeSegment() < f.getTimeline().timeline.size())
//				p.p.text(" Upper: "+f.getTimeSegment(p.viewer.getCurrentFieldTimeSegment()).getUpper().getTime()
//						+" Center:"+f.getTimeSegment(p.viewer.getCurrentFieldTimeSegment()).getCenter().getTime()+
//						" Lower: "+f.getTimeSegment(p.viewer.getCurrentFieldTimeSegment()).getLower().getTime(), xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Current Cluster Timeline Segments: "+ p.getCurrentCluster().getTimeline().timeline.size(), xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Field Dateline Segments: "+ p.getCurrentField().getDateline().size(), xPos, yPos += lineWidth, hudDistance);
//			p.p.textSize(mediumTextSize);
//
//			if(p.p.debugSettings.memory)
//			{
//				if(p.p.debugSettings.detailed)
//				{
//					p.p.text("Total memory (bytes): " + p.p.debugSettings.totalMemory, xPos, yPos += lineWidth, hudDistance);
//					p.p.text("Available processors (cores): "+p.p.debugSettings.availableProcessors, xPos, yPos += lineWidth, hudDistance);
//					p.p.text("Maximum memory (bytes): " +  (p.p.debugSettings.maxMemory == Long.MAX_VALUE ? "no limit" : p.p.debugSettings.maxMemory), xPos, yPos += lineWidth, hudDistance); 
//					p.p.text("Total memory (bytes): " + p.p.debugSettings.totalMemory, xPos, yPos += lineWidth, hudDistance);
//					p.p.text("Allocated memory (bytes): " + p.p.debugSettings.allocatedMemory, xPos, yPos += lineWidth, hudDistance);
//				}
//				p.p.text("Free memory (bytes): "+p.p.debugSettings.freeMemory, xPos, yPos += lineWidth, hudDistance);
//				p.p.text("Approx. usable free memory (bytes): " + p.p.debugSettings.approxUsableFreeMemory, xPos, yPos += lineWidth, hudDistance);
//			}			
//		}
//		
//		p.p.popMatrix();
//	}

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
	
	/**
	 * Draw progress bar
	 */
//	void progressBar()
//	{
//		int length = 100;	// total length
//		int pos = p.setupProgress;	//current position
//
//		startHUD();
//		for(int i=0; i<pos; i++)
//		{
//			p.p.pushMatrix();
//			
//			p.p.fill(140, 100, 255);
//			float xPos = utilities.mapValue(i, 0, length, 0, screenWidth * 1.f);
//			float inc = utilities.mapValue(2, 0, length, 0, screenWidth * 1.f) - utilities.mapValue(1, 0, length, 0, screenWidth*1.f);
//			int x = -screenWidth/2 + (int)xPos;
//			int y = -screenHeight/2+screenHeight/2;
//
//			p.p.translate(x, y, hudDistance);
//			p.p.box(inc, inc*10.f, 1);    // Display 
//			p.p.popMatrix();
//		}
//	}

}

