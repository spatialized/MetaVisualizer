package multimediaLocator;

import java.awt.Font;
import java.util.ArrayList;

import g4p_controls.*;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;
import processing.event.MouseEvent;

/**
 * Handles secondary program windows
 * @author davidgordon
 */
public class ML_Window 
{
	/* Classes */
	ML_Display display;
	WMV_Utilities utilities;
	
	/* General */
	private int windowWidth = 310;
	private int shortWindowHeight = 340, mediumWindowHeight = 600, tallWindowHeight = 875;
	private int compressedNavigationWindowHeight = 580, compressedMediaWindowHeight = 490;
	private int delayAmount = 100;							// Delay length to avoid G4P library concurrent modification exception
		
	/* Windows */
	public boolean compressTallWindows = false;
	
	public GWindow mlWindow, navigationWindow, mediaWindow, statisticsWindow,  helpWindow, 
				   mapWindow, timelineWindow;
	public GWindow libraryWindow, createLibraryWindow, listItemWindow, textEntryWindow;

	public GLabel lblMainMenu, lblNavigationWindow, lblMedia, lblStatistics, lblHelp, lblMap, lblTimeline;
	public GLabel lblLibrary, lblImport;	
	
	public boolean setupMLWindow, setupNavigationWindow = false, setupMediaWindow = false, setupHelpWindow = false, 
				   setupStatisticsWindow = false, setupMapWindow = false, setupTimelineWindow = false;
	public boolean setupCreateLibraryWindow = false, setupLibraryWindow = false;
	
	public boolean showMLWindow = false, showNavigationWindow = false, showMediaWindow = false, showStatisticsWindow = false, 
				   showHelpWindow = false, showMapWindow = false, showTimelineWindow = false;;
	public boolean showCreateLibraryWindow, showLibraryWindow = false;

	/* Library Window */
	public GButton btnCreateLibrary, btnOpenLibrary, btnLibraryHelp;
	public GLabel lblLibraryWindowText;
	private int libraryWindowHeight;
	
	/* CreateLibrary Window */
	public GButton btnImportMediaFolder, btnMakeLibrary;
	public GLabel lblCreateLibraryWindowText, lblCreateLibraryWindowText2;
	private int createLibraryWindowHeight;

	/* List Item Window */
	public boolean showListItemWindowList = false;
	private int listItemWindowHeight;
	private ArrayList<String> listItemWindowList;
	private String listItemWindowText;
	public int listItemWindowSelectedItem = -1;
	public int listItemWindowResultCode = -1;		// 1: GPS Track  

	/* Text Entry Window */
	public boolean showTextEntryWindow = false;
	private int textEntryWindowHeight;
	public int textEntryWindowSelectedItem = -1;
	public int textEntryWindowResultCode = -1;		// 1: GPS Track  
	private String textEntryWindowText;				// Prompt text
	private String textEntryWindowUserEntry;		// User entry
	
	/* Main Window */
	private GButton btnNavigationWindow, btnMediaWindow, btnStatisticsWindow, btnHelpWindow;
	private GButton btnChooseField, btnSaveLibrary, btnSaveField, btnRestart, btnQuit;
	private GLabel lblViewMode, lblWindows, lblCommands, lblSpaceBar;
	public GToggleGroup tgDisplayView;	
	public GOption optWorldView, optMapView, optTimelineView;
	private int mlWindowHeight;
	public GCheckbox chkbxScreenMessagesOn;

	/* Navigation Window */
	public GCheckbox chkbxPathFollowing; 															/* Navigation Window Options */
	public GCheckbox chkbxMovementTeleport, chkbxFollowTeleport;
	
	public GSlider sdrTeleportLength, sdrPathWaitLength;											/* Navigation Window Sliders */

	private GButton btnZoomIn, btnZoomOut;															/* Navigation Window Buttons */
	private GButton btnSaveLocation, btnClearMemory;
	private GButton btnJumpToRandomCluster;
	public GButton btnExportScreenshot, btnExportMedia, btnOutputFolder;
	private GButton btnNextTimeSegment, btnPreviousTimeSegment;
	private GButton btnMoveToNearestCluster, btnMoveToLastCluster;
	private GButton btnMoveToNearestImage, btnMoveToNearestPanorama;
	private GButton btnMoveToNearestVideo, btnMoveToNearestSound;
	private GButton btnGoToPreviousField, btnGoToNextField, btnChooseGPSTrack;

	private GLabel lblTimeNavigation, lblAutoNavigation, lblNearest, lblPathNavigation, 			/* Navigation Window Labels */
	lblTeleportLength, lblPathWaitLength, lblMemory;
	public GLabel lblShift1;
	
	private int navigationWindowLineBreakY_1, navigationWindowLineBreakY_2;		
	int navigationWindowHeight, navigationWindowWidth;
	
	private GLabel lblTimeWindow, lblTimeCycle, lblTimeMode;										/* Time Controls */
	public GLabel lblMediaLength, lblTimeCycleLength, lblCurrentTime, lblClusterLength;
	public GLabel lblTime;
	public GCheckbox chkbxPaused, chkbxTimeFading;
	public GSlider sdrMediaLength, sdrTimeCycleLength, sdrCurrentTime, sdrClusterLength;
	public GToggleGroup tgTimeMode;	
	public GOption optClusterTimeMode, optFieldTimeMode; //, optMediaTimeMode;
	public GLabel lblCommand2;

	/* Media Window */
	int mediaWindowHeight, mediaWindowWidth;
	public GLabel lblMediaSettings, lblGraphicsModes, lblOutput, lblZoom;
	public GOption optTimeline, optGPSTrack, optMemory;
	public GToggleGroup tgFollow;	

	public GCheckbox chkbxSelectionMode, chkbxMultiSelection, chkbxSegmentSelection, chkbxShowMetadata; 	/* Media Window Options */
	public GCheckbox chkbxShowModel, chkbxMediaToCluster, chkbxCaptureToMedia, chkbxCaptureToCluster;
	public GCheckbox chkbxHideImages, chkbxHideVideos, chkbxHidePanoramas, chkbxHideSounds;
	public GCheckbox chkbxAlphaMode, chkbxAngleFading, chkbxAngleThinning;
	public GCheckbox chkbxOrientationMode, chkbxDomeView;
	public GCheckbox chkbxBlurMasks;

	public GSlider sdrAltitudeScaling;									/* Media Window Sliders */
	public GSlider sdrVisibleAngle, sdrAlpha, sdrBrightness;
	
	private GButton btnSubjectDistanceUp, btnSubjectDistanceDown;		/* Media Window Buttons */
	public GButton btnSelectFront, btnDeselectFront;
	public GButton btnSelectPanorama, btnDeselectPanorama;
	public GButton btnDeselectAll, btnViewSelected, btnStitchPanorama;
	
	private GLabel lblSubjectDistance;									/* Media Window Labels */
	private GLabel lblVisibleAngle, lblAlpha, lblBrightness;
	private GLabel lblHideMedia;
	public GLabel lblSelection, lblSelectionOptions;					
	private GLabel lblAdvanced, lblAltitudeScaling;						
	public GLabel lblShift2;

	private int mediaWindowLineBreakY_1, mediaWindowLineBreakY_2;

	/* Statistics Window */
	public GLabel lblViewerStats, lblWorldStats;					
	public GLabel lblShift3;
	int statisticsWindowHeight;
	
	/* Help Window */
	private GButton btnAboutHelp, btnImportHelp, btnCloseHelp;
	public int helpAboutText = 0;		// Whether showing  0: About Text  1: Importing Files Help Text, or 2: Keyboard Shortcuts
	public GLabel lblShift8;
	int helpWindowHeight;
	
	/* Map Window */
	private GButton btnMapView;		
	private GButton btnPanUp, btnPanLeft, btnPanDown, btnPanRight;		
	private GButton btnZoomToViewer, btnZoomToSelected;		
	private GButton btnZoomOutToField, btnZoomOutToWorld;		
	public GLabel lblZoomTo, lblPan;					
	public GLabel lblShift4;
	public GOption optMapViewFieldMode, optMapViewWorldMode;
	int mapWindowHeight;
	
	/* Time Window */
	public GToggleGroup tgMapViewMode;	
	private GButton btnTimeView;		
	private GButton btnTimelineReverse, btnTimelineForward;		
	private GButton btnTimelineZoomIn, btnTimelineZoomOut;		
	private GButton btnTimelineZoomToField, btnTimelineZoomToSelected;		
	public GLabel lblTimelineZoomTo;					
	public GLabel lblShift5;
	int timelineWindowHeight;

	/* Margins */
	private int iLeftMargin = 15;			
	private int iTopMargin = 10;
	private int iBottomTextY = 26;
	
	/* Sizing */
	private int iVerySmallBoxHeight = 22;		/* GUI box object height */
	private int iSmallBoxHeight = 28;
	private int iMediumBoxHeight = 30;	
	private int iLargeBoxHeight = 33;
	private int iVeryLargeBoxHeight = 39;

	/* Text */
	int iVeryLargeTextSize = 20;				/* Button text sizes */
	int iLargeTextSize = 18;			
	int iMediumTextSize = 16;
	int iSmallTextSize = 14;
	int iVerySmallTextSize = 12;
	float fLargeTextSize = 18.f;				/* Window text sizes */
	float fMediumTextSize = 16.f;
	float fSmallTextSize = 14.f;

	float lineWidthVeryWide = 18.f;				/* Window text line width */
	float lineWidthWide = 16.f;
	float lineWidth = 14.f;

	private WMV_World world;			
	
	/**
	 * Constructor for secondary window handler 
	 * @param parent Parent world
	 * @param newDisplay Parent display object
	 */
	public ML_Window( WMV_World parent, ML_Display newDisplay )
	{
		world = parent;
		display = newDisplay;
		utilities = new WMV_Utilities();
		
		mlWindowHeight = shortWindowHeight + 70;
		libraryWindowHeight = shortWindowHeight / 2;
		createLibraryWindowHeight = shortWindowHeight;
		listItemWindowHeight = shortWindowHeight;			// -- Update this
		textEntryWindowHeight = shortWindowHeight;
		

		if(world.ml.displayHeight < tallWindowHeight + 25)
		{
//			System.out.println("world.ml.displayHeight:"+world.ml.displayHeight+" will compress windows...");
			compressTallWindows = true;
			navigationWindowHeight = compressedNavigationWindowHeight;		// 960	
			navigationWindowWidth = windowWidth * 2;
			mediaWindowHeight = compressedMediaWindowHeight;
			mediaWindowWidth = windowWidth * 2;
		}
		else
		{
			compressTallWindows = false;
			navigationWindowHeight = tallWindowHeight;		// 960	
			navigationWindowWidth = windowWidth;
			mediaWindowHeight = tallWindowHeight;
			mediaWindowWidth = windowWidth;
		}
		
		statisticsWindowHeight = mediumWindowHeight - 60;
		helpWindowHeight = mediumWindowHeight + 100;
		mapWindowHeight = shortWindowHeight - 25;
		timelineWindowHeight = shortWindowHeight - 90;
	}
	
	public void openMLWindow()
	{
		if(!setupMLWindow) setupMLWindow();
		showMLWindow();
	}
	public void openNavigationWindow()
	{
		if(!setupNavigationWindow) setupNavigationWindow();
		showNavigationWindow();
	}

	public void openMediaWindow()
	{
		if(!setupMediaWindow) setupMediaWindow();
		showMediaWindow();
	}

	public void openStatisticsWindow()
	{
		if(!setupStatisticsWindow)
			setupStatisticsWindow();
		showStatisticsWindow();
	}
	
	public void openHelpWindow()
	{
		if(!setupHelpWindow)
			setupHelpWindow();
		showHelpWindow();
	}

	public void openMapWindow()
	{
		if(!setupMapWindow)
			setupMapWindow();
		showMapWindow();
	}

	public void openTimelineWindow()
	{
		if(!setupTimelineWindow)
			setupTimelineWindow();
		showTimelineWindow();
	}

	/**
	 * Setup MultimediaLocator Window
	 */
	public void setupMLWindow()
	{
//		-- Adjust if compressTallWindows 
		
		int leftEdge = world.ml.displayWidth / 2 - windowWidth / 2;
		int topEdge = world.ml.displayHeight / 2 - mlWindowHeight / 2;
		
		mlWindow = GWindow.getWindow(world.ml, "Main Menu", leftEdge, topEdge, windowWidth, mlWindowHeight, PApplet.JAVA2D);
		mlWindow.addData(new ML_WinData());
		mlWindow.addDrawHandler(this, "mlWindowDraw");
		mlWindow.addMouseHandler(this, "mlWindowMouse");
		mlWindow.addKeyHandler(world.ml, "mlWindowKey");
		mlWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		world.ml.delay(delayAmount);

		int x = 0, y = iTopMargin;
		lblMainMenu = new GLabel(mlWindow, x, y, mlWindow.width, iMediumBoxHeight, "Main Menu");
		lblMainMenu.setLocalColorScheme(G4P.SCHEME_10);
		lblMainMenu.setFont(new Font("Monospaced", Font.PLAIN, iVeryLargeTextSize));
		lblMainMenu.setTextAlign(GAlign.CENTER, null);
		lblMainMenu.setTextBold();

		y += iLargeBoxHeight;
		lblViewMode = new GLabel(mlWindow, x, y, mlWindow.width, iSmallBoxHeight);						/* Display Mode Label */
		lblViewMode.setText("View Mode");
		lblViewMode.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblViewMode.setLocalColorScheme(G4P.SCHEME_10);
		lblViewMode.setTextAlign(GAlign.CENTER, null);

		x = iLeftMargin + 3;
		y += iMediumBoxHeight;
		optWorldView = new GOption(mlWindow, x, y, 95, iVerySmallBoxHeight, "World (1)");
		optWorldView.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize-1));
		optWorldView.setLocalColorScheme(G4P.SCHEME_10);
		optWorldView.tag = "SceneView";
		optMapView = new GOption(mlWindow, x += 105, y, 80, iVerySmallBoxHeight, "Map (2)");
		optMapView.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize-1));
		optMapView.setLocalColorScheme(G4P.SCHEME_10);
		optMapView.tag = "MapView";
		optTimelineView = new GOption(mlWindow, x += 90, y, 100, iVerySmallBoxHeight, "Time (3)");
		optTimelineView.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize-1));
		optTimelineView.setLocalColorScheme(G4P.SCHEME_10);
		optTimelineView.tag = "TimelineView";

		world.ml.delay(delayAmount  / 2);

		switch(display.displayView)
		{
			case 0:
				optWorldView.setSelected(true);
				optMapView.setSelected(false);
				optTimelineView.setSelected(false);
				break;
			case 1:
				optWorldView.setSelected(false);
				optMapView.setSelected(true);
				optTimelineView.setSelected(false);
				break;
			case 2:
				optWorldView.setSelected(false);
				optMapView.setSelected(false);
				optTimelineView.setSelected(true);
				break;
		}
		
		tgDisplayView = new GToggleGroup();
		tgDisplayView.addControls(optWorldView, optMapView, optTimelineView);
	
		x = 65;
		y += iMediumBoxHeight;
		chkbxScreenMessagesOn = new GCheckbox(mlWindow, x, y, 220, iVerySmallBoxHeight, "Screen Messages (H)");
		chkbxScreenMessagesOn.tag = "ScreenMessagesOn";
		chkbxScreenMessagesOn.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		chkbxScreenMessagesOn.setLocalColorScheme(G4P.SCHEME_10);
		chkbxScreenMessagesOn.setSelected(world.getSettings().screenMessagesOn);

		x = 0;
		y += iMediumBoxHeight;
		lblWindows = new GLabel(mlWindow, x, y, mlWindow.width, iSmallBoxHeight);						/* Display Mode Label */
		lblWindows.setText("Windows");
		lblWindows.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblWindows.setLocalColorScheme(G4P.SCHEME_10);
		lblWindows.setTextAlign(GAlign.CENTER, null);

		world.ml.delay(delayAmount / 2);

		x = 95;
		y += iMediumBoxHeight;
		btnNavigationWindow = new GButton(mlWindow, x, y, 120, iVerySmallBoxHeight, "Navigation  ⇧1");
		btnNavigationWindow.tag = "OpenNavigationWindow";
		btnNavigationWindow.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		y += iSmallBoxHeight;
		btnMediaWindow = new GButton(mlWindow, x, y, 120, iVerySmallBoxHeight, "Media  ⇧2");
		btnMediaWindow.tag = "OpenMediaWindow";
		btnMediaWindow.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		y += iSmallBoxHeight;
		btnStatisticsWindow = new GButton(mlWindow, x, y, 120, iVerySmallBoxHeight, "Statistics  ⇧3");
		btnStatisticsWindow.tag = "OpenStatisticsWindow";
		btnStatisticsWindow.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 0;
		y += iMediumBoxHeight;
		lblCommands = new GLabel(mlWindow, x, y, mlWindow.width, iSmallBoxHeight);						/* Display Mode Label */
		lblCommands.setText("Commands");
		lblCommands.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblCommands.setLocalColorScheme(G4P.SCHEME_10);
		lblCommands.setTextAlign(GAlign.CENTER, null);

		x = 85;
		y += iMediumBoxHeight;
		btnSaveLibrary = new GButton(mlWindow, x, y, 140, iVerySmallBoxHeight, "Save Library  ⇧S");
		btnSaveLibrary.tag = "SaveWorld";
		btnSaveLibrary.setLocalColorScheme(G4P.CYAN_SCHEME);

		if(world.getFieldCount() > 1)
		{
//			System.out.println("btnSaveField...");
			y += iSmallBoxHeight;
			btnSaveField = new GButton(mlWindow, x, y, 140, iVerySmallBoxHeight, "Save Field  /");
			btnSaveField.tag = "SaveField";
			btnSaveField.setLocalColorScheme(G4P.CYAN_SCHEME);
		}

		world.ml.delay(delayAmount);

		y += iSmallBoxHeight;
		btnRestart = new GButton(mlWindow, x, y, 140, iVerySmallBoxHeight, "Close Library  ⇧R");
		btnRestart.tag = "CloseLibrary";
		btnRestart.setLocalColorScheme(G4P.ORANGE_SCHEME);

		x = 105;
		y += iSmallBoxHeight;
		btnQuit = new GButton(mlWindow, x, y, 100, iVerySmallBoxHeight, "Quit  ⇧Q");
		btnQuit.tag = "Quit";
		btnQuit.setLocalColorScheme(G4P.RED_SCHEME);

		y = mlWindowHeight - iBottomTextY * 5 / 2;
		btnHelpWindow = new GButton(mlWindow, windowWidth - 30 - iLeftMargin, y, 30, 30, "?");
		btnHelpWindow.tag = "OpenHelpWindow";
		btnHelpWindow.setFont(new Font("Monospaced", Font.BOLD, iLargeTextSize));
		btnHelpWindow.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		x = 0;
		y = mlWindowHeight - iBottomTextY;
		lblSpaceBar = new GLabel(mlWindow, x, y, mlWindow.width, iVerySmallBoxHeight);						/* Display Mode Label */
		lblSpaceBar.setText("Press SPACEBAR to show / hide");
		lblSpaceBar.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		lblSpaceBar.setLocalColorScheme(G4P.SCHEME_10);
		lblSpaceBar.setTextAlign(GAlign.CENTER, null);
		
		setupMLWindow = true;
		world.ml.setAppIcon = true;
	}

	/**
	 * Setup the Navigation Window
	 */
	public void setupNavigationWindow()
	{
		int leftEdge = world.ml.displayWidth / 2 - windowWidth / 2;
		int topEdge = world.ml.displayHeight / 2 - navigationWindowHeight / 2;
		
		navigationWindow = GWindow.getWindow(world.ml, "", leftEdge, topEdge, navigationWindowWidth, navigationWindowHeight, PApplet.JAVA2D);
		navigationWindow.setVisible(true);

		navigationWindow.addData(new ML_WinData());
		navigationWindow.addDrawHandler(this, "navigationWindowDraw");
		navigationWindow.addMouseHandler(this, "navigationWindowMouse");
		navigationWindow.addKeyHandler(world.ml, "navigationWindowKey");
		navigationWindow.setActionOnClose(GWindow.KEEP_OPEN);
		world.ml.delay(delayAmount);
		
		int x = 0, y = iTopMargin;
		
		if(compressTallWindows) x = 105;
		else x = 0;

		lblNavigationWindow = new GLabel(navigationWindow, x, y, navigationWindow.width, iMediumBoxHeight, "Navigation");
		lblNavigationWindow.setLocalColorScheme(G4P.SCHEME_10);
		lblNavigationWindow.setFont(new Font("Monospaced", Font.PLAIN, iVeryLargeTextSize));
		if(!compressTallWindows) lblNavigationWindow.setTextAlign(GAlign.CENTER, null);
		lblNavigationWindow.setTextBold();

		if(compressTallWindows) x = 120;
		else x = 0;
		y += iLargeBoxHeight;
		lblAutoNavigation = new GLabel(navigationWindow, x, y, navigationWindow.width, iVerySmallBoxHeight, "Movement");
		lblAutoNavigation.setLocalColorScheme(G4P.SCHEME_10);
		lblAutoNavigation.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		if(!compressTallWindows) lblAutoNavigation.setTextAlign(GAlign.CENTER, null);
		lblAutoNavigation.setTextBold();

		world.ml.delay(delayAmount);

		x = 140;
		y += iMediumBoxHeight;
		btnMoveToNearestCluster = new GButton(navigationWindow, x, y, 100, iVerySmallBoxHeight, "Any Media (m)");
		btnMoveToNearestCluster.tag = "NearestCluster";
		btnMoveToNearestCluster.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = iLeftMargin;
		y += iSmallBoxHeight;
		lblNearest = new GLabel(navigationWindow, x, y, 85, iVerySmallBoxHeight, "Nearest:");
		lblNearest.setLocalColorScheme(G4P.SCHEME_10);
		lblNearest.setFont(new Font("Monospaced", Font.ITALIC, iMediumTextSize));
		lblNearest.setTextBold();
		
		x += 90;
		btnMoveToNearestImage = new GButton(navigationWindow, x, y, 80, iVerySmallBoxHeight, "Image (i)");
		btnMoveToNearestImage.tag = "NearestImage";
		btnMoveToNearestImage.setLocalColorScheme(G4P.CYAN_SCHEME);

		btnMoveToNearestPanorama = new GButton(navigationWindow, x+=90, y, 100, iVerySmallBoxHeight, "Panorama (p)");
		btnMoveToNearestPanorama.tag = "NearestPanorama";
		btnMoveToNearestPanorama.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 105;
		y += iSmallBoxHeight;
		btnMoveToNearestVideo = new GButton(navigationWindow, x, y, 80, iVerySmallBoxHeight, "Video (v)");
		btnMoveToNearestVideo.tag = "NearestVideo";
		btnMoveToNearestVideo.setLocalColorScheme(G4P.CYAN_SCHEME);

		btnMoveToNearestSound = new GButton(navigationWindow, x+=90, y, 100, iVerySmallBoxHeight, "Sound (u)");
		btnMoveToNearestSound.tag = "NearestSound";
		btnMoveToNearestSound.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 80;
		y += iMediumBoxHeight;
		btnMoveToLastCluster = new GButton(navigationWindow, x, y, 65, iVerySmallBoxHeight, "Last (l)");
		btnMoveToLastCluster.tag = "LastCluster";
		btnMoveToLastCluster.setLocalColorScheme(G4P.CYAN_SCHEME);

		x += 80;
		btnJumpToRandomCluster = new GButton(navigationWindow, x, y, 85, iVerySmallBoxHeight, "Random (j)");
		btnJumpToRandomCluster.tag = "RandomCluster";
		btnJumpToRandomCluster.setLocalColorScheme(G4P.CYAN_SCHEME);

		if(compressTallWindows) x = 135;
		else x = 0;
		y += iMediumBoxHeight;
		lblTimeNavigation = new GLabel(navigationWindow, x, y, navigationWindow.width, iVerySmallBoxHeight, "Time");
		lblTimeNavigation.setLocalColorScheme(G4P.SCHEME_10);
		lblTimeNavigation.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		if(!compressTallWindows) lblTimeNavigation.setTextAlign(GAlign.CENTER, null);
		lblTimeNavigation.setTextBold();
		
		world.ml.delay(delayAmount / 2);

		x = 40;
		btnPreviousTimeSegment = new GButton(navigationWindow, x, y, 85, iVerySmallBoxHeight, "Backward (b)");
		btnPreviousTimeSegment.tag = "PreviousTime";
		btnPreviousTimeSegment.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 190;
		btnNextTimeSegment = new GButton(navigationWindow, x, y, 85, iVerySmallBoxHeight, "Forward (n)");
		btnNextTimeSegment.tag = "NextTime";
		btnNextTimeSegment.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		x = 105;
		y += iMediumBoxHeight;
		chkbxMovementTeleport = new GCheckbox(navigationWindow, x, y, 125, iVerySmallBoxHeight, "Teleport (t)");
		chkbxMovementTeleport.tag = "MovementTeleport";
		chkbxMovementTeleport.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		chkbxMovementTeleport.setLocalColorScheme(G4P.SCHEME_10);
		chkbxMovementTeleport.setSelected(world.viewer.getState().movementTeleport);

		x = 75;
		y += iMediumBoxHeight;
		btnZoomOut = new GButton(navigationWindow, x, y, 50, iVerySmallBoxHeight, "Out (z)");
		btnZoomOut.tag = "ZoomOut";
		btnZoomOut.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnZoomOut.fireAllEvents(true);

		if(compressTallWindows) x = 135;
		else x = 0;
		lblZoom = new GLabel(navigationWindow, x, y, navigationWindow.width, iVerySmallBoxHeight, "Zoom");
		lblZoom.setLocalColorScheme(G4P.SCHEME_10);
		lblZoom.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		if(!compressTallWindows) lblZoom.setTextAlign(GAlign.CENTER, null);
		lblZoom.setTextBold();

		world.ml.delay(delayAmount / 2);

		x = 190;
		btnZoomIn = new GButton(navigationWindow, x, y, 50, iVerySmallBoxHeight, "In (q)");
		btnZoomIn.tag = "ZoomIn";
		btnZoomIn.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnZoomIn.fireAllEvents(true);

		if(world.getFields() != null)
		{
			if(world.getFieldCount() > 1)
			{
				x = 85;
				y += iMediumBoxHeight;
				btnChooseField = new GButton(navigationWindow, x, y, 130, iVerySmallBoxHeight, "Choose Field  ⇧C");
				btnChooseField.tag = "ChooseField";
				btnChooseField.setLocalColorScheme(G4P.CYAN_SCHEME);

				x = 40;
				y += iMediumBoxHeight;
				btnGoToPreviousField = new GButton(navigationWindow, x, y, 120, iVerySmallBoxHeight, "Previous Field  ⇧[");
				btnGoToPreviousField.tag = "PreviousField";
				btnGoToPreviousField.setLocalColorScheme(G4P.CYAN_SCHEME);

				btnGoToNextField = new GButton(navigationWindow, x+=125, y, 100, iVerySmallBoxHeight, "Next Field  ⇧]");
				btnGoToNextField.tag = "NextField";
				btnGoToNextField.setLocalColorScheme(G4P.CYAN_SCHEME);
			}
		}

		x = 40;
		y += iMediumBoxHeight;
		lblMemory = new GLabel(navigationWindow, x, y, 70, iVerySmallBoxHeight, "Memory");
		lblMemory.setLocalColorScheme(G4P.SCHEME_10);
		lblMemory.setFont(new Font("Monospaced", Font.ITALIC, iMediumTextSize));
		lblMemory.setTextBold();
		
		world.ml.delay(delayAmount / 2);

		x = 120;
		btnSaveLocation = new GButton(navigationWindow, x, y, 60, iVerySmallBoxHeight, "Save (`)");
		btnSaveLocation.tag = "SaveLocation";
		btnSaveLocation.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 190;
		btnClearMemory = new GButton(navigationWindow, x, y, 70, iVerySmallBoxHeight, "Clear (y)");
		btnClearMemory.tag = "ClearMemory";
		btnClearMemory.setLocalColorScheme(G4P.ORANGE_SCHEME);
		
		if(compressTallWindows)
			x = 110;
		else
			x = 0;
		y += 40;
		navigationWindowLineBreakY_1 = y - 10;
		lblPathNavigation = new GLabel(navigationWindow, x, y, navigationWindow.width, iVerySmallBoxHeight, "Path Mode");
		lblPathNavigation.setLocalColorScheme(G4P.SCHEME_10);
		lblPathNavigation.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		if(!compressTallWindows) lblPathNavigation.setTextAlign(GAlign.CENTER, null);
		lblPathNavigation.setTextBold();

		x = 40;
		y += iSmallBoxHeight;
		optTimeline = new GOption(navigationWindow, x, y, 90, iVerySmallBoxHeight, "Timeline");
		optTimeline.setLocalColorScheme(G4P.SCHEME_10);
		optTimeline.tag = "FollowTimeline";
		optGPSTrack = new GOption(navigationWindow, x+=85, y, 90, iVerySmallBoxHeight, "GPS Track");
		optGPSTrack.setLocalColorScheme(G4P.SCHEME_10);
		optGPSTrack.tag = "FollowGPSTrack";
		optMemory = new GOption(navigationWindow, x+=90, y, 90, iVerySmallBoxHeight, "Memory");
		optMemory.setLocalColorScheme(G4P.SCHEME_10);
		optMemory.tag = "FollowMemory";
		
		switch(world.viewer.getState().followMode)			// 0: Timeline 1: GPS Track 2: Memory
		{
			case 0:						// Timeline
				optTimeline.setSelected(true);
				optGPSTrack.setSelected(false);
				optMemory.setSelected(false);
				break;
			case 1:						// GPS Track
				optTimeline.setSelected(false);
				optGPSTrack.setSelected(true);
				optMemory.setSelected(false);
				break;
			case 2:						// Memory
				optTimeline.setSelected(false);
				optGPSTrack.setSelected(false);
				optMemory.setSelected(true);
				break;
		}
		
		tgFollow = new GToggleGroup();
		tgFollow.addControls(optTimeline, optGPSTrack, optMemory);

		world.ml.delay(delayAmount / 2);

		x = 90;
		y += iMediumBoxHeight;
		chkbxPathFollowing = new GCheckbox(navigationWindow, x, y, 150, iVerySmallBoxHeight, "On / Off  (>)");
		chkbxPathFollowing.tag = "Following";
		chkbxPathFollowing.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		chkbxPathFollowing.setLocalColorScheme(G4P.SCHEME_10);
		chkbxPathFollowing.setSelected(world.viewer.isFollowing());

		y += iSmallBoxHeight;
		chkbxFollowTeleport = new GCheckbox(navigationWindow, x, y, 135, iVerySmallBoxHeight, "Teleport  (,)");
		chkbxFollowTeleport.tag = "FollowTeleport";
		chkbxFollowTeleport.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		chkbxFollowTeleport.setLocalColorScheme(G4P.SCHEME_10);
		chkbxFollowTeleport.setSelected(world.viewer.getState().followTeleport);

		if(world.getCurrentField().getGPSTracks() != null)
		{
			if(world.getCurrentField().getGPSTracks().size() > 0)
			{
				x = 90;
				y += 30;
				btnChooseGPSTrack = new GButton(navigationWindow, x, y, 140, iVerySmallBoxHeight, "Select GPS Track");
				btnChooseGPSTrack.tag = "ChooseGPSTrack";
				btnChooseGPSTrack.setLocalColorScheme(G4P.CYAN_SCHEME);
			}
		}

		x = 160;
		y += iMediumBoxHeight;
		sdrTeleportLength = new GSlider(navigationWindow, x, y, 80, 80, iVerySmallBoxHeight);
		sdrTeleportLength.setLocalColorScheme(G4P.GOLD_SCHEME);
		sdrTeleportLength.setLimits(0.f, 300.f, 10.f);
		sdrTeleportLength.setValue(world.viewer.getSettings().teleportLength);
		sdrTeleportLength.setRotation(PApplet.PI/2.f);
		sdrTeleportLength.setTextOrientation(G4P.ORIENT_LEFT);
		sdrTeleportLength.setEasing(0);
		sdrTeleportLength.setShowValue(true);
		sdrTeleportLength.tag = "TeleportLength";

		x = 290;
		sdrPathWaitLength = new GSlider(navigationWindow, x, y, 80, 80, iVerySmallBoxHeight);
		sdrPathWaitLength.setLocalColorScheme(G4P.GOLD_SCHEME);
		sdrPathWaitLength.setLimits(0.f, 600.f, 30.f);
		sdrPathWaitLength.setValue(world.viewer.getSettings().pathWaitLength);
		sdrPathWaitLength.setRotation(PApplet.PI/2.f);
		sdrPathWaitLength.setTextOrientation(G4P.ORIENT_LEFT);
		sdrPathWaitLength.setEasing(0);
		sdrPathWaitLength.setShowValue(true);
		sdrPathWaitLength.tag = "PathWaitLength";
		
		x = 25;
		y += iMediumBoxHeight;
		lblTeleportLength = new GLabel(navigationWindow, x, y, 100, iVerySmallBoxHeight, "Teleport Time");
		lblTeleportLength.setLocalColorScheme(G4P.SCHEME_10);

		x = 175;
		lblPathWaitLength = new GLabel(navigationWindow, x, y, 100, iVerySmallBoxHeight, "Wait Time");
		lblPathWaitLength.setLocalColorScheme(G4P.SCHEME_10);

		world.ml.delay(delayAmount / 2);

		if(compressTallWindows)
		{
			x = windowWidth + 115;
			y = iTopMargin;
		}
		else
		{
			y += 70;
			x = 0;
		}

		/* Time */
		navigationWindowLineBreakY_2 = y - 10;
		lblTimeWindow = new GLabel(navigationWindow, x, y, navigationWindow.width, iSmallBoxHeight, "Time");
		lblTimeWindow.setLocalColorScheme(G4P.SCHEME_10);
		lblTimeWindow.setFont(new Font("Monospaced", Font.PLAIN, iVeryLargeTextSize));
		if(!compressTallWindows) lblTimeWindow.setTextAlign(GAlign.CENTER, null);
		lblTimeWindow.setTextBold();

		x = 135;
		if(compressTallWindows) x += windowWidth;
		y += iLargeBoxHeight;
		optClusterTimeMode = new GOption(navigationWindow, x, y, 135, 20, "Cluster (Z)");
		optClusterTimeMode.setLocalColorScheme(G4P.SCHEME_10);
		optClusterTimeMode.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		optClusterTimeMode.tag = "ClusterTimeMode";
		
		x = iLeftMargin;
		if(compressTallWindows) x += windowWidth;
		y += iMediumBoxHeight * 0.5f;
		lblTimeMode = new GLabel(navigationWindow, x, y, 100, iVerySmallBoxHeight, "Time Mode");
		lblTimeMode.setLocalColorScheme(G4P.SCHEME_10);
		lblTimeMode.setFont(new Font("Monospaced", Font.BOLD, iMediumTextSize));
	
		world.ml.delay(delayAmount / 2);

		x = 135;
		if(compressTallWindows) x += windowWidth;
		y += iMediumBoxHeight * 0.5f;
		optFieldTimeMode = new GOption(navigationWindow, x, y, 135, 20, "Field (OPT z)");
		optFieldTimeMode.setLocalColorScheme(G4P.SCHEME_10);
		optFieldTimeMode.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		optFieldTimeMode.tag = "FieldTimeMode";
		
		switch(world.getState().getTimeMode())
		{
			case 0:
				optClusterTimeMode.setSelected(true);
				optFieldTimeMode.setSelected(false);
				break;
			case 1:
				optClusterTimeMode.setSelected(false);
				optFieldTimeMode.setSelected(true);
				break;
		}
		
		tgTimeMode = new GToggleGroup();
		tgTimeMode.addControls(optClusterTimeMode, optFieldTimeMode);
		world.ml.delay(delayAmount);

		x = 135;
		if(compressTallWindows) x += windowWidth;
		y += iMediumBoxHeight;
		chkbxTimeFading = new GCheckbox(navigationWindow, x, y, 160, iVerySmallBoxHeight, "Run Cycle (T)");
		chkbxTimeFading.tag = "TimeFading";
		chkbxTimeFading.setLocalColorScheme(G4P.SCHEME_10);
		chkbxTimeFading.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		chkbxTimeFading.setSelected(world.getState().timeFading);

		x = 35;
		if(compressTallWindows) x += windowWidth;
		y += iMediumBoxHeight * 0.5f;
		lblTimeCycle = new GLabel(navigationWindow, x, y, 85, iVerySmallBoxHeight, "Cycle");
		lblTimeCycle.setLocalColorScheme(G4P.SCHEME_10);
		lblTimeCycle.setFont(new Font("Monospaced", Font.BOLD, iMediumTextSize));

		x = 135;
		if(compressTallWindows) x += windowWidth;
		y += iMediumBoxHeight * 0.5f;
		chkbxPaused = new GCheckbox(navigationWindow, x, y, 140, iVerySmallBoxHeight, "Pause (-)");
		chkbxPaused.tag = "Paused";
		chkbxPaused.setLocalColorScheme(G4P.SCHEME_10);
		chkbxPaused.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		chkbxPaused.setSelected(world.getState().paused);

		x = 120;
		if(compressTallWindows) x += windowWidth;
		y += 15;
		sdrTimeCycleLength = new GSlider(navigationWindow, x, y, 160, 80, 20);
		sdrTimeCycleLength.setLocalColorScheme(G4P.GOLD_SCHEME);
		sdrTimeCycleLength.setLimits(0.f, world.settings.timeCycleLength, 3200.f);
		sdrTimeCycleLength.setTextOrientation(G4P.ORIENT_TRACK);
		sdrTimeCycleLength.setEasing(0);
		sdrTimeCycleLength.setShowValue(true);
		sdrTimeCycleLength.tag = "TimeCycleLength";

		x = 35;
		if(compressTallWindows) x += windowWidth;
		y += 30;
		lblTimeCycleLength = new GLabel(navigationWindow, x, y, 120, iVerySmallBoxHeight, "Cycle Length");
		lblTimeCycleLength.setLocalColorScheme(G4P.SCHEME_10);

		x = 120;
		if(compressTallWindows) x += windowWidth;
		y += 10;
		sdrCurrentTime = new GSlider(navigationWindow, x, y, 160, 80, 20);
		sdrCurrentTime.setLocalColorScheme(G4P.GOLD_SCHEME);
		sdrCurrentTime.setLimits(0.f, 0.f, 1.f);
		sdrCurrentTime.setValue(0.f);
		sdrCurrentTime.setTextOrientation(G4P.ORIENT_TRACK);
		sdrCurrentTime.setEasing(0);
		sdrCurrentTime.setShowValue(true);
		sdrCurrentTime.tag = "CurrentTime";

		x = 35;
		if(compressTallWindows) x += windowWidth;
		y += 30;
		lblCurrentTime = new GLabel(navigationWindow, x, y, 100, iVerySmallBoxHeight, "Current Time");
		lblCurrentTime.setLocalColorScheme(G4P.SCHEME_10);

		x = 120;
		if(compressTallWindows) x += windowWidth;
		y += 10;
		sdrMediaLength = new GSlider(navigationWindow, x, y, 160, 80, 20);
		sdrMediaLength.setLocalColorScheme(G4P.GOLD_SCHEME);
		sdrMediaLength.setLimits(world.settings.defaultMediaLength, 0.f, 250.f);	// setLimits (int initValue, int start, int end)
		sdrMediaLength.setValue(world.settings.defaultMediaLength);
		sdrMediaLength.setTextOrientation(G4P.ORIENT_TRACK);
		sdrMediaLength.setEasing(0);
		sdrMediaLength.setShowValue(true);
		sdrMediaLength.tag = "MediaLength";

		x = 35;
		if(compressTallWindows) x += windowWidth;
		y += 30;
		lblMediaLength = new GLabel(navigationWindow, x, y, 100, iVerySmallBoxHeight, "Media Length");
		lblMediaLength.setLocalColorScheme(G4P.SCHEME_10);
		
		x = 120;
		if(compressTallWindows) x += windowWidth;
		y += 10;
		sdrClusterLength = new GSlider(navigationWindow, x, y, 160, 80, 20);
		sdrClusterLength.setLocalColorScheme(G4P.GOLD_SCHEME);
		sdrClusterLength.setLimits(world.settings.clusterLength, 0.f, 1.f);
		sdrClusterLength.setValue(world.settings.clusterLength);
		sdrClusterLength.setTextOrientation(G4P.ORIENT_TRACK);
		sdrClusterLength.setEasing(0);
		sdrClusterLength.setShowValue(true);
		sdrClusterLength.tag = "ClusterLength";
		
		x = 35;
		if(compressTallWindows) x += windowWidth;
		y += 30;
		lblClusterLength = new GLabel(navigationWindow, x, y, 120, iVerySmallBoxHeight, "Cluster Length");
		lblClusterLength.setLocalColorScheme(G4P.SCHEME_10);

		switch(world.state.timeMode)
		{
			case 0:												// Cluster
//				sdrVisibleInterval.setValue(world.getCurrentCluster().getTimeCycleLength());
//				if(sdrClusterLength.isVisible())
					sdrClusterLength.setVisible(false);
//				if(lblClusterLength.isVisible())
					lblClusterLength.setVisible(false);
//					System.out.println(">>>>>>> world.state.timeMode:"+world.state.timeMode);
				break;
			case 1:												// Field
				sdrClusterLength.setValue(world.settings.timeCycleLength);
				if(!sdrClusterLength.isVisible())
					sdrClusterLength.setVisible(true);
				if(!lblClusterLength.isVisible())
					lblClusterLength.setVisible(true);
				break;
			default:
				break;
		}
		
		x = 0;
		y = navigationWindowHeight - iBottomTextY;
		lblShift1 = new GLabel(navigationWindow, x, y, navigationWindow.width, iVerySmallBoxHeight);						/* Display Mode Label */
		lblShift1.setText("Press SHIFT + 1 to show / hide");
		lblShift1.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		lblShift1.setLocalColorScheme(G4P.SCHEME_10);
		lblShift1.setTextAlign(GAlign.CENTER, null);

		setupNavigationWindow = true;
		world.ml.setAppIcon = true;
	}

	/**
	 * Setup the Media Window
	 */
	public void setupMediaWindow()
	{
		int leftEdge = world.ml.displayWidth / 2 - windowWidth / 2;
		int topEdge = world.ml.displayHeight / 2 - mediaWindowHeight / 2;

		mediaWindow = GWindow.getWindow(world.ml, "", leftEdge, topEdge, mediaWindowWidth, mediaWindowHeight, PApplet.JAVA2D);
		mediaWindow.setVisible(true);

		mediaWindow.addData(new ML_WinData());
		mediaWindow.addDrawHandler(this, "mediaWindowDraw");
		mediaWindow.addMouseHandler(this, "mediaWindowMouse");
		mediaWindow.addKeyHandler(world.ml, "mediaWindowKey");
		mediaWindow.setActionOnClose(GWindow.KEEP_OPEN);
	
		int x = 0, y = iTopMargin;
		world.ml.delay(delayAmount);

		if(compressTallWindows) x = 115;
		lblMedia = new GLabel(mediaWindow, x, y, mediaWindow.width, iSmallBoxHeight, "Media");
		lblMedia.setLocalColorScheme(G4P.SCHEME_10);
		lblMedia.setFont(new Font("Monospaced", Font.PLAIN, iVeryLargeTextSize));
		if(!compressTallWindows) lblMedia.setTextAlign(GAlign.CENTER, null);
		lblMedia.setTextBold();
		
		if(compressTallWindows) x = 110;
		else x = 0;
		y += iLargeBoxHeight;
		lblMediaSettings = new GLabel(mediaWindow, x, y, mediaWindow.width, iVerySmallBoxHeight, "Settings");
		lblMediaSettings.setLocalColorScheme(G4P.SCHEME_10);
		lblMediaSettings.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		if(!compressTallWindows) lblMediaSettings.setTextAlign(GAlign.CENTER, null);
		lblMediaSettings.setTextBold();

		world.ml.delay(delayAmount / 2);

		x = 120;
		y += 10;
		sdrVisibleAngle = new GSlider(mediaWindow, x, y, 160, 80, 20);
		sdrVisibleAngle.setLocalColorScheme(G4P.GOLD_SCHEME);
		sdrVisibleAngle.setLimits(world.viewer.getVisibleAngle(), 0.1f, (float)Math.PI * 0.5f);
		sdrVisibleAngle.setTextOrientation(G4P.ORIENT_TRACK);
		sdrVisibleAngle.setEasing(0);
		sdrVisibleAngle.setShowValue(true);
		sdrVisibleAngle.tag = "VisibleAngle";
		
		x = 30;
		y += 30;
		lblVisibleAngle = new GLabel(mediaWindow, x, y, 120, iVerySmallBoxHeight, "Visible Angle");
		lblVisibleAngle.setLocalColorScheme(G4P.SCHEME_10);

		x = 120;
		y += 10;
		sdrAlpha = new GSlider(mediaWindow, x, y, 160, 80, 20);
		sdrAlpha.setLocalColorScheme(G4P.GOLD_SCHEME);
		sdrAlpha.setLimits(world.getState().alpha, 0.f, 255.f);
		sdrAlpha.setValue(world.getState().alpha);		
		sdrAlpha.setTextOrientation(G4P.ORIENT_TRACK);
		sdrAlpha.setEasing(0);
		sdrAlpha.setShowValue(true);
		sdrAlpha.tag = "Alpha";

		world.ml.delay(delayAmount / 2);

		x = 30;
		y += 30;
		lblAlpha= new GLabel(mediaWindow, x, y, 60, iVerySmallBoxHeight, "Alpha");
		lblAlpha.setLocalColorScheme(G4P.SCHEME_10);

		x = 120;
		y += 10;
		sdrBrightness = new GSlider(mediaWindow, x, y, 160, 80, 20);
		sdrBrightness.setLocalColorScheme(G4P.GOLD_SCHEME);
		sdrBrightness.setLimits(world.viewer.getSettings().userBrightness, 0.f, 1.f);
		sdrBrightness.setValue(world.viewer.getSettings().userBrightness);		
		sdrBrightness.setTextOrientation(G4P.ORIENT_TRACK);
		sdrBrightness.setEasing(0);
		sdrBrightness.setShowValue(true);
		sdrBrightness.tag = "Brightness";

		x = 30;
		y += 30;
		lblBrightness = new GLabel(mediaWindow, x, y, 90, iVerySmallBoxHeight, "Brightness");
		lblBrightness.setLocalColorScheme(G4P.SCHEME_10);

		world.ml.delay(delayAmount / 2);

		x = 120;
		y += 10;
		world.ml.delay(delayAmount);
		sdrAltitudeScaling = new GSlider(mediaWindow, x, y, 160, 80, 20);
		sdrAltitudeScaling.setLocalColorScheme(G4P.GOLD_SCHEME);
		sdrAltitudeScaling.setLimits(0.f, 0.f, 1.f);
		sdrAltitudeScaling.setValue(world.settings.altitudeScalingFactor);					// -- Shouldn't be needed! Calls handler
		sdrAltitudeScaling.setTextOrientation(G4P.ORIENT_TRACK);
		sdrAltitudeScaling.setEasing(0);
		sdrAltitudeScaling.setShowValue(true);
		sdrAltitudeScaling.tag = "AltitudeScaling";
		
		x = 30;
		y += 30;
		lblAltitudeScaling = new GLabel(mediaWindow, x, y, 100, iVerySmallBoxHeight, "Altitude Factor");
		lblAltitudeScaling .setLocalColorScheme(G4P.SCHEME_10);

		x = 65;
		y += iLargeBoxHeight;
		btnSubjectDistanceDown = new GButton(mediaWindow, x, y, 30, iVerySmallBoxHeight, "-");
		btnSubjectDistanceDown.tag = "SubjectDistanceDown";
		btnSubjectDistanceDown.setLocalColorScheme(G4P.CYAN_SCHEME);

		x += 42;
		lblSubjectDistance = new GLabel(mediaWindow, x, y, 110, iVerySmallBoxHeight, "Subject Distance");
		lblSubjectDistance.setLocalColorScheme(G4P.SCHEME_10);
//		if(!compressTallWindows) lblSubjectDistance.setTextAlign(GAlign.CENTER, null);
		lblSubjectDistance.setTextBold();

		world.ml.delay(delayAmount / 2);

		x = 215;
		btnSubjectDistanceUp = new GButton(mediaWindow, x, y, 30, iVerySmallBoxHeight, "+");
		btnSubjectDistanceUp.tag = "SubjectDistanceUp";
		btnSubjectDistanceUp.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 25;
		y += iMediumBoxHeight;
		chkbxAlphaMode = new GCheckbox(mediaWindow, x, y, 135, iVerySmallBoxHeight, "Alpha Mode (OPT p)");
		chkbxAlphaMode.tag = "AlphaMode";
		chkbxAlphaMode.setLocalColorScheme(G4P.SCHEME_10);
		chkbxAlphaMode.setSelected(world.getState().alphaMode);

		chkbxBlurMasks = new GCheckbox(mediaWindow, x += 145, y, 125, iVerySmallBoxHeight, "Edge Masking (E)");
		chkbxBlurMasks.tag = "FadeEdges";
		chkbxBlurMasks.setLocalColorScheme(G4P.SCHEME_10);
		chkbxBlurMasks.setSelected(world.getState().useBlurMasks);

		x = 85;
		y += iSmallBoxHeight;
		chkbxAngleFading = new GCheckbox(mediaWindow, x, y, 120, iVerySmallBoxHeight, "Angle Fading (G)");
		chkbxAngleFading.tag = "AngleFading";
		chkbxAngleFading.setLocalColorScheme(G4P.SCHEME_10);
		chkbxAngleFading.setSelected(world.viewer.getSettings().angleFading);

//		chkbxAngleThinning = new GCheckbox(mediaWindow, x += 105, y, 100, iVerySmallBoxHeight, "Angle Thinning");
//		chkbxAngleThinning.tag = "AngleThinning";
//		chkbxAngleThinning.setLocalColorScheme(G4P.SCHEME_10);
//		chkbxAngleThinning.setSelected(world.viewer.getSettings().angleThinning);

		world.ml.delay(delayAmount / 2);

		if(compressTallWindows) x = 100;
		else x = 0;
		y += iMediumBoxHeight;
		lblHideMedia = new GLabel(mediaWindow, x, y, mediaWindow.width, iVerySmallBoxHeight, "Hide Media");
		lblHideMedia.setLocalColorScheme(G4P.SCHEME_10);
		lblHideMedia.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		if(!compressTallWindows) lblHideMedia.setTextAlign(GAlign.CENTER, null);
		lblHideMedia.setTextBold();

		x = 60;
		y += iMediumBoxHeight;
		chkbxHideImages = new GCheckbox(mediaWindow, x, y, 95, iVerySmallBoxHeight, "Images (I)");
		chkbxHideImages.tag = "HideImages";
		chkbxHideImages.setLocalColorScheme(G4P.SCHEME_10);
		chkbxHideImages.setSelected(world.viewer.getSettings().hideImages);
		
		chkbxHidePanoramas = new GCheckbox(mediaWindow, x += 105, y, 115, iVerySmallBoxHeight, "Panoramas (P)");
		chkbxHidePanoramas.tag = "HidePanoramas";
		chkbxHidePanoramas.setLocalColorScheme(G4P.SCHEME_10);
		chkbxHidePanoramas.setSelected(world.viewer.getSettings().hidePanoramas);

		x = 60;
		y += iSmallBoxHeight;
		chkbxHideVideos = new GCheckbox(mediaWindow, x, y, 85, iVerySmallBoxHeight, "Videos (V)");
		chkbxHideVideos.tag = "HideVideos";
		chkbxHideVideos.setLocalColorScheme(G4P.SCHEME_10);
		chkbxHideVideos.setSelected(world.viewer.getSettings().hideVideos);

		chkbxHideSounds = new GCheckbox(mediaWindow, x += 105, y, 95, iVerySmallBoxHeight, "Sounds (U)");
		chkbxHideSounds.tag = "HideSounds";
		chkbxHideSounds.setLocalColorScheme(G4P.SCHEME_10);
		chkbxHideSounds.setSelected(world.viewer.getSettings().hideSounds);

		x = 30;
		y += iMediumBoxHeight;
		btnOutputFolder = new GButton(mediaWindow, x, y, 115, iVerySmallBoxHeight, "Set Output Folder");
		btnOutputFolder.tag = "OutputFolder";
		btnOutputFolder.setLocalColorScheme(G4P.CYAN_SCHEME);

		x += 125;
		btnExportScreenshot = new GButton(mediaWindow, x, y, 140, iVerySmallBoxHeight, "Save Screenshot (O)");
		btnExportScreenshot.tag = "SaveScreenshot";
		btnExportScreenshot.setLocalColorScheme(G4P.CYAN_SCHEME);

		world.ml.delay(delayAmount / 2);
		if(compressTallWindows)
		{
			x = windowWidth + 100;
			y = iTopMargin;
		}
		else
		{
			x = 0;
			y += 45;
		}

		mediaWindowLineBreakY_1 = y - 10;
		lblSelection = new GLabel(mediaWindow, x, y, mediaWindow.width, iMediumBoxHeight, "Selection");
		lblSelection.setLocalColorScheme(G4P.SCHEME_10);
		if(!compressTallWindows) lblSelection.setTextAlign(GAlign.CENTER, null);
		lblSelection.setFont(new Font("Monospaced", Font.PLAIN, iVeryLargeTextSize));
		lblSelection.setTextBold();

		x = 105;
		if(compressTallWindows) x += windowWidth;
		y += iLargeBoxHeight;
		chkbxSelectionMode = new GCheckbox(mediaWindow, x, y, 120, iVerySmallBoxHeight, "Enable (A)");
		chkbxSelectionMode.tag = "EnableSelection";
		chkbxSelectionMode.setLocalColorScheme(G4P.SCHEME_10);
		chkbxSelectionMode.setFont(new Font("Monospaced", Font.BOLD, iMediumTextSize));
		chkbxSelectionMode.setSelected(world.viewer.getSettings().selection);
		
		x = 85;
		if(compressTallWindows) x += windowWidth;
		y += iSmallBoxHeight;
		chkbxShowMetadata = new GCheckbox(mediaWindow, x, y, 200, iVerySmallBoxHeight, "Show Metadata (M)");
		chkbxShowMetadata.tag = "ViewMetadata";
		chkbxShowMetadata.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		chkbxShowMetadata.setLocalColorScheme(G4P.SCHEME_10);
		chkbxShowMetadata.setSelected(world.getState().showMetadata);
		
		x = iLeftMargin - 3;
		if(compressTallWindows) x += windowWidth;
		y += iSmallBoxHeight;
		chkbxMultiSelection = new GCheckbox(mediaWindow, x, y, 145, iVerySmallBoxHeight, "Allow Multiple (OPT m)");
		chkbxMultiSelection.tag = "MultiSelection";
		chkbxMultiSelection.setLocalColorScheme(G4P.SCHEME_10);
		chkbxMultiSelection.setSelected(world.viewer.getSettings().multiSelection);

		chkbxSegmentSelection = new GCheckbox(mediaWindow, x + 148, y, 175, iVerySmallBoxHeight, "Select Groups (OPT s)");
		chkbxSegmentSelection.tag = "SegmentSelection";
		chkbxSegmentSelection.setLocalColorScheme(G4P.SCHEME_10);
		chkbxSegmentSelection.setSelected(world.viewer.getSettings().groupSelection);
		
		x = iLeftMargin;
		if(compressTallWindows) x += windowWidth;
		y += 35;
		btnSelectFront = new GButton(mediaWindow, x, y, 120, iVerySmallBoxHeight, "Select (x)");
		btnSelectFront.tag = "SelectFront";
		btnSelectFront.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		x += 130;
		btnSelectPanorama = new GButton(mediaWindow, x, y, 150, iVerySmallBoxHeight, "Select Panorama (k)");
		btnSelectPanorama.tag = "SelectPanorama";
		btnSelectPanorama.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 95;
		if(compressTallWindows) x += windowWidth;
		y += iMediumBoxHeight;
		btnViewSelected = new GButton(mediaWindow, x, y, 120, iVerySmallBoxHeight, "View (⏎)");
		btnViewSelected.tag = "ViewSelected";
		btnViewSelected.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 95;
		if(compressTallWindows) x += windowWidth;
		y += iMediumBoxHeight;
		btnExportMedia = new GButton(mediaWindow, x, y, 120, iVerySmallBoxHeight, "Export (o)");
		btnExportMedia.tag = "ExportMedia";
		btnExportMedia.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = iLeftMargin;
		if(compressTallWindows) x += windowWidth;
		y += iMediumBoxHeight;
		btnDeselectFront = new GButton(mediaWindow, x, y, 110, iVerySmallBoxHeight, "Deselect (X)");
		btnDeselectFront.tag = "DeselectFront";
		btnDeselectFront.setLocalColorScheme(G4P.RED_SCHEME);

		x += 120;
		btnDeselectPanorama = new GButton(mediaWindow, x, y, 165, iVerySmallBoxHeight, "Deselect Panorama (K)");
		btnDeselectPanorama.tag = "DeselectPanorama";
		btnDeselectPanorama.setLocalColorScheme(G4P.RED_SCHEME);

		world.ml.delay(delayAmount / 2);

		x = 90;
		if(compressTallWindows) x += windowWidth;
		y += iMediumBoxHeight;
		btnDeselectAll = new GButton(mediaWindow, x, y, 150, iVerySmallBoxHeight, "Deselect All (OPT x)");
		btnDeselectAll.tag = "DeselectAll";
		btnDeselectAll.setLocalColorScheme(G4P.RED_SCHEME);

//		x = 85;
//		if(compressTallWindows) x += windowWidth;
//		y += iButtonSpacingWide;
//		btnStitchPanorama = new GButton(mediaWindow, x, y, 140, iSmallBoxHeight, "Stitch Selection  (⇧\\)");
//		btnStitchPanorama.tag = "StitchPanorama";
//		btnStitchPanorama.setLocalColorScheme(G4P.GOLD_SCHEME);
	
//		if(compressTallWindows)
//		{
//			x = windowWidth + 100;
//			y = iTopMargin;
//		}
//		else x = 0;
		
		if(compressTallWindows) 
			x = windowWidth + 100;
		else
			x = 0;

		y += 40;
		mediaWindowLineBreakY_2 = y - 10;
		lblAdvanced = new GLabel(mediaWindow, x, y, mediaWindow.width, iSmallBoxHeight, "Advanced");
		lblAdvanced.setLocalColorScheme(G4P.SCHEME_10);
		lblAdvanced.setFont(new Font("Monospaced", Font.PLAIN, iVeryLargeTextSize));
		if(!compressTallWindows)
			lblAdvanced.setTextAlign(GAlign.CENTER, null);
		lblAdvanced.setTextBold();

		x = iLeftMargin;
		if(compressTallWindows) x += windowWidth;
		y += iLargeBoxHeight;
		chkbxShowModel = new GCheckbox(mediaWindow, x, y, 160, iVerySmallBoxHeight, "Show Model (5)");
		chkbxShowModel.tag = "ShowModel";
		chkbxShowModel.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		chkbxShowModel.setLocalColorScheme(G4P.SCHEME_10);
		chkbxShowModel.setSelected(world.getState().showModel);
		
		chkbxMediaToCluster = new GCheckbox(mediaWindow, x += 160, y, 130, iVerySmallBoxHeight, "View Clusters (6)");
		chkbxMediaToCluster.tag = "MediaToCluster";
		chkbxMediaToCluster.setLocalColorScheme(G4P.SCHEME_10);
		chkbxMediaToCluster.setEnabled(world.getState().showModel);
		chkbxMediaToCluster.setSelected(world.getState().showMediaToCluster);
		
		x = iLeftMargin;
		if(compressTallWindows) x += windowWidth;
		y += iSmallBoxHeight;
		chkbxCaptureToMedia = new GCheckbox(mediaWindow, x, y, 150, iVerySmallBoxHeight, "View GPS Locations (7)");
		chkbxCaptureToMedia.tag = "CaptureToMedia";
		chkbxCaptureToMedia.setLocalColorScheme(G4P.SCHEME_10);
		chkbxCaptureToMedia.setEnabled(world.getState().showModel);
		chkbxCaptureToMedia.setSelected(world.getState().showCaptureToMedia);

		chkbxCaptureToCluster = new GCheckbox(mediaWindow, x += 160, y, 170, iVerySmallBoxHeight, "View Adjustment (8)");
		chkbxCaptureToCluster.tag = "CaptureToCluster";
		chkbxCaptureToCluster.setLocalColorScheme(G4P.SCHEME_10);
		chkbxCaptureToCluster.setEnabled(world.getState().showModel);
		chkbxCaptureToCluster.setSelected(world.getState().showCaptureToCluster);

		x = 85;
		if(compressTallWindows) x += windowWidth;
		y += iSmallBoxHeight;
		chkbxOrientationMode = new GCheckbox(mediaWindow, x, y, 155, iVerySmallBoxHeight, "Static Mode (9)");
		chkbxOrientationMode.tag = "OrientationMode";
		chkbxOrientationMode.setLocalColorScheme(G4P.SCHEME_10);
		chkbxOrientationMode.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		chkbxOrientationMode.setSelected(world.viewer.getSettings().orientationMode);

//		x = 110;
//		if(compressTallWindows) x += windowWidth;
//		y += iMediumBoxHeight;
//		chkbxDomeView = new GCheckbox(mediaWindow, x, y, 160, iSmallBoxHeight, "Sphere View (BETA)");
//		chkbxDomeView.tag = "DomeView";
//		chkbxDomeView.setLocalColorScheme(G4P.SCHEME_10);
//		chkbxDomeView.setEnabled(world.viewer.getSettings().orientationMode);
//		chkbxDomeView.setSeleted(world.viewer.getSettings().);
		
		if(!world.viewer.getSettings().selection)
		{
			btnSelectFront.setEnabled(false);
			btnViewSelected.setEnabled(false);
			btnDeselectFront.setEnabled(false);
			btnDeselectAll.setEnabled(false);
			btnExportMedia.setEnabled(false);
			chkbxMultiSelection.setEnabled(false);
			chkbxMultiSelection.setSelected(false);
			chkbxSegmentSelection.setEnabled(false);
			chkbxSegmentSelection.setSelected(false);
			chkbxShowMetadata.setEnabled(false);
			chkbxShowMetadata.setSelected(false);
		}

		x = 0;
		y = mediaWindowHeight - iBottomTextY;
		lblShift2 = new GLabel(mediaWindow, x, y, mediaWindow.width, iVerySmallBoxHeight);						/* Display Mode Label */
		lblShift2.setText("Press SHIFT + 2 to show / hide");
		lblShift2.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		lblShift2.setLocalColorScheme(G4P.SCHEME_10);
		lblShift2.setTextAlign(GAlign.CENTER, null);

		setupMediaWindow = true;
		world.ml.setAppIcon = true;
	}
	
	/**
	 * Setup the Statistics Window
	 */
	public void setupStatisticsWindow()
	{
		int leftEdge = world.ml.displayWidth / 2 - windowWidth * 3 / 4;
		int topEdge = world.ml.displayHeight / 2 - statisticsWindowHeight / 2;

		statisticsWindow = GWindow.getWindow(world.ml, "", leftEdge, topEdge, windowWidth * 3 / 2, statisticsWindowHeight, PApplet.JAVA2D);
		statisticsWindow.setVisible(true);
		statisticsWindow.addData(new ML_WinData());
		statisticsWindow.addDrawHandler(this, "statisticsWindowDraw");
//		statisticsWindow.addMouseHandler(this, "statisticsWindowMouse");
		statisticsWindow.addKeyHandler(world.ml, "statisticsWindowKey");
		statisticsWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		int x = 0, y = iTopMargin;
		world.ml.delay(delayAmount);

		lblStatistics = new GLabel(statisticsWindow, x, y, statisticsWindow.width, iVerySmallBoxHeight, "Statistics");
		lblStatistics.setLocalColorScheme(G4P.SCHEME_10);
		lblStatistics.setTextAlign(GAlign.CENTER, null);
		lblStatistics.setFont(new Font("Monospaced", Font.PLAIN, iVeryLargeTextSize));
		lblStatistics.setTextBold();
		
		world.ml.delay(delayAmount);

		y += iLargeBoxHeight;
		lblViewerStats = new GLabel(statisticsWindow, x, y, statisticsWindow.width, iMediumBoxHeight, "Viewer");
		lblViewerStats.setLocalColorScheme(G4P.SCHEME_10);
		lblViewerStats.setTextAlign(GAlign.CENTER, null);
		lblViewerStats.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
		lblViewerStats.setTextBold();

		y = 200;
		lblWorldStats = new GLabel(statisticsWindow, x, y, statisticsWindow.width, iMediumBoxHeight, "World");
		lblWorldStats.setLocalColorScheme(G4P.SCHEME_10);
		lblWorldStats.setTextAlign(GAlign.CENTER, null);
		lblWorldStats.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
		lblWorldStats.setTextBold();

		world.ml.delay(delayAmount);

		x = 0;
		y = statisticsWindowHeight - iBottomTextY;
		lblShift3 = new GLabel(statisticsWindow, x, y, statisticsWindow.width, iVerySmallBoxHeight);						/* Display Mode Label */
		lblShift3.setText("Press SHIFT + 3 to show / hide");
		lblShift3.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		lblShift3.setLocalColorScheme(G4P.SCHEME_10);
		lblShift3.setTextAlign(GAlign.CENTER, null);
		
		setupStatisticsWindow = true;
		world.ml.setAppIcon = true;
	}
	
	/**
	 * Setup the Help Window
	 */
	public void setupHelpWindow()
	{
		int leftEdge = world.ml.displayWidth / 2 - windowWidth * 2;
		int topEdge = world.ml.displayHeight / 2 - helpWindowHeight / 2;

		helpWindow = GWindow.getWindow(world.ml, "Help", leftEdge, topEdge, windowWidth * 4, helpWindowHeight, PApplet.JAVA2D);
		helpWindow.setVisible(true);
		helpWindow.addData(new ML_WinData());
		helpWindow.addDrawHandler(this, "helpWindowDraw");
		helpWindow.addMouseHandler(this, "helpWindowMouse");
		helpWindow.addKeyHandler(world.ml, "helpWindowKey");
		
		int x = 0, y = iTopMargin;
		world.ml.delay(delayAmount);

		x = 55;
		y = helpWindowHeight / 2 - iLargeBoxHeight - iLargeBoxHeight;
		btnAboutHelp = new GButton(helpWindow, x, y, 100, iLargeBoxHeight, "About");
		btnAboutHelp.tag = "AboutHelp";
		btnAboutHelp.setFont(new Font("Monospaced", Font.BOLD, iMediumTextSize));
		btnAboutHelp.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 20;
		y = helpWindowHeight / 2 - iLargeBoxHeight + iLargeBoxHeight;
		btnImportHelp = new GButton(helpWindow, x, y, 170, iLargeBoxHeight, "Importing Files");
		btnImportHelp.tag = "ImportHelp";
		btnImportHelp.setFont(new Font("Monospaced", Font.BOLD, iMediumTextSize));
		btnImportHelp.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = windowWidth * 2 - 60;
		y = helpWindowHeight - iBottomTextY * 3;
		btnCloseHelp = new GButton(helpWindow, x, y, 120, iSmallBoxHeight, "Close");
		btnCloseHelp.tag = "CloseHelp";
		btnCloseHelp.setFont(new Font("Monospaced", Font.BOLD, iSmallTextSize));
		btnCloseHelp.setLocalColorScheme(G4P.RED_SCHEME);

		setupHelpWindow = true;
		world.ml.setAppIcon = true;
	}

	/**
	 * Setup the Help Window
	 */
	public void setupMapWindow()
	{
		int leftEdge = world.ml.displayWidth / 2 - windowWidth / 2;
		int topEdge = world.ml.displayHeight / 2 - mapWindowHeight / 2;

		mapWindow = GWindow.getWindow(world.ml, "", leftEdge, topEdge, windowWidth, mapWindowHeight, PApplet.JAVA2D);
		mapWindow.setVisible(true);
		mapWindow.addData(new ML_WinData());
		mapWindow.addDrawHandler(this, "mapWindowDraw");
		mapWindow.addMouseHandler(this, "mapWindowMouse");
		mapWindow.addKeyHandler(world.ml, "mapWindowKey");

		int x = 0, y = iTopMargin;
		world.ml.delay(delayAmount);

		lblMap = new GLabel(mapWindow, x, y, mapWindow.width, iSmallBoxHeight, "Map Controls");
		lblMap.setLocalColorScheme(G4P.SCHEME_10);
		lblMap.setTextAlign(GAlign.CENTER, null);
		lblMap.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
		lblMap.setTextBold();

		x = 78;
		y += iVeryLargeBoxHeight;
		btnMapView = new GButton(mapWindow, x, y, 160, iSmallBoxHeight, "Open Map View (2)");
		btnMapView.tag = "SetMapView";
		btnMapView.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		btnMapView.setLocalColorScheme(G4P.GOLD_SCHEME);

		x = 40;
		y += iVeryLargeBoxHeight;
		optMapViewFieldMode = new GOption(mapWindow, x, y, 115, iVerySmallBoxHeight, "Field (F)");
		optMapViewFieldMode.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize-1));
		optMapViewFieldMode.setLocalColorScheme(G4P.SCHEME_10);
		optMapViewFieldMode.tag = "SetMapViewFieldMode";
		optMapViewWorldMode = new GOption(mapWindow, x += 125, y, 115, iVerySmallBoxHeight, "World (L)");
		optMapViewWorldMode.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize-1));
		optMapViewWorldMode.setLocalColorScheme(G4P.SCHEME_10);
		optMapViewWorldMode.tag = "SetMapViewWorldMode";

		world.ml.delay(delayAmount  / 2);

		switch(display.mapViewMode)
		{
			case 0:										// World
				optMapViewWorldMode.setSelected(true);
				optMapViewFieldMode.setSelected(false);
				break;
			case 1:										// Field
				optMapViewWorldMode.setSelected(false);
				optMapViewFieldMode.setSelected(true);
				break;
		}
		
		tgMapViewMode = new GToggleGroup();
		tgMapViewMode.addControls(optMapViewFieldMode, optMapViewWorldMode);

		x = 125;
		y += iLargeBoxHeight + 2;
		btnPanUp = new GButton(mapWindow, x, y, 60, iVerySmallBoxHeight, "Up (W)");
		btnPanUp.tag = "PanUp";
		btnPanUp.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnPanUp.fireAllEvents(true);

		x = 55;
		y += iMediumBoxHeight;
		btnPanLeft = new GButton(mapWindow, x, y, 60, iVerySmallBoxHeight, "Left (A)");
		btnPanLeft.tag = "PanLeft";
		btnPanLeft.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnPanLeft.fireAllEvents(true);

		x = 0;
		lblPan = new GLabel(mapWindow, x, y-3, mapWindow.width, iSmallBoxHeight, "Pan");
		lblPan.setLocalColorScheme(G4P.SCHEME_10);
		lblPan.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblPan.setTextAlign(GAlign.CENTER, null);
		lblPan.setTextBold();
		
		x = 190;
		btnPanRight = new GButton(mapWindow, x, y, 70, iVerySmallBoxHeight, "Right (D)");
		btnPanRight.tag = "PanRight";
		btnPanRight.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnPanRight.fireAllEvents(true);

		x = 123;
		y += iMediumBoxHeight;
		btnPanDown = new GButton(mapWindow, x, y, 65, iVerySmallBoxHeight, "Down (S)");
		btnPanDown.tag = "PanDown";
		btnPanDown.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnPanDown.fireAllEvents(true);

		x = 110;
		y += iVeryLargeBoxHeight;
		btnZoomToViewer = new GButton(mapWindow, x, y, 75, iVerySmallBoxHeight, "Viewer");
		btnZoomToViewer.tag = "ZoomToViewer";					// -- Zooms to current cluster
		btnZoomToViewer.setLocalColorScheme(G4P.CYAN_SCHEME);
		x += 95;
		btnZoomOutToField = new GButton(mapWindow, x, y, 70, iVerySmallBoxHeight, "Field");
		btnZoomOutToField.tag = "ZoomToField";
		btnZoomOutToField.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = iLeftMargin;
		y += iMediumBoxHeight * 0.5f;
		lblZoomTo = new GLabel(mapWindow, x, y, mapWindow.width, iSmallBoxHeight, "Zoom To:");
		lblZoomTo.setLocalColorScheme(G4P.SCHEME_10);
		lblZoomTo.setFont(new Font("Monospaced", Font.ITALIC, iMediumTextSize));
		lblZoomTo.setTextBold();

//		x = 110;
//		y += iMediumBoxHeight * 0.5f;
//		btnZoomToSelected = new GButton(mapWindow, x, y, 80, iVerySmallBoxHeight, "Selected");
//		btnZoomToSelected.tag = "ZoomToSelected";
//		btnZoomToSelected.setLocalColorScheme(G4P.CYAN_SCHEME);
//		x += 95;
		
		x = 205;
		y += iMediumBoxHeight * 0.5f;
		btnZoomOutToWorld = new GButton(mapWindow, x, y, 70, iVerySmallBoxHeight, "World");
		btnZoomOutToWorld.tag = "ZoomToWorld";
		btnZoomOutToWorld.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 0;
		y = mapWindowHeight - iBottomTextY;
		lblShift4 = new GLabel(mapWindow, x, y, mapWindow.width, iSmallBoxHeight);						/* Display Mode Label */
		lblShift4.setText("Press SHIFT + 4 to show / hide");
		lblShift4.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		lblShift4.setLocalColorScheme(G4P.SCHEME_10);
		lblShift4.setTextAlign(GAlign.CENTER, null);
		
		setMapControlsEnabled(display.displayView == 1);
		setupMapWindow = true;
		world.ml.setAppIcon = true;
	}
	
	public void setMapControlsEnabled(boolean enable)
	{
		System.out.println("Window.setMapControlsEnabled()... "+enable);
//		private GButton btnMapView;		
//		private GButton btnPanUp, btnPanLeft, btnPanDown, btnPanRight;		
//		private GButton btnZoomToViewer, btnZoomToSelected;		
//		private GButton btnZoomOutToField, btnZoomOutToWorld;		

		if(enable)		// Enable map controls
		{
//			btnMapView.setEnabled(true);
			btnPanUp.setEnabled(true);
			btnPanLeft.setEnabled(true);
			btnPanDown.setEnabled(true);
			btnPanRight.setEnabled(true);
			btnZoomToViewer.setEnabled(true);
//			btnZoomToSelected.setEnabled(true);
			btnZoomOutToField.setEnabled(true);
			btnZoomOutToWorld.setEnabled(true);
			optMapViewFieldMode.setEnabled(true);
			optMapViewWorldMode.setEnabled(true);
		}
		else			// Disable map controls
		{
//			btnMapView.setEnabled(false);
			btnPanUp.setEnabled(false);
			btnPanLeft.setEnabled(false);
			btnPanDown.setEnabled(false);
			btnPanRight.setEnabled(false);
			btnZoomToViewer.setEnabled(false);
//			btnZoomToSelected.setEnabled(false);
			btnZoomOutToField.setEnabled(false);
			btnZoomOutToWorld.setEnabled(false);
			optMapViewFieldMode.setEnabled(false);
			optMapViewWorldMode.setEnabled(false);
		}
	}
	
	/**
	 * Setup the Help Window
	 */
	public void setupTimelineWindow()
	{
		int leftEdge = world.ml.displayWidth / 2 - windowWidth / 2;
		int topEdge = world.ml.displayHeight / 2 - timelineWindowHeight / 2;

		timelineWindow = GWindow.getWindow(world.ml, "", leftEdge, topEdge, windowWidth, timelineWindowHeight, PApplet.JAVA2D);
		timelineWindow.setVisible(true);
		timelineWindow.addData(new ML_WinData());
		timelineWindow.addDrawHandler(this, "timelineWindowDraw");
		timelineWindow.addMouseHandler(this, "timelineWindowMouse");
		timelineWindow.addKeyHandler(world.ml, "timelineWindowKey");
		
		int x = 0, y = iTopMargin;
		world.ml.delay(delayAmount);

		lblTimeline = new GLabel(timelineWindow, x, y, timelineWindow.width, iVerySmallBoxHeight, "Timeline Controls");
		lblTimeline.setLocalColorScheme(G4P.SCHEME_10);
		lblTimeline.setTextAlign(GAlign.CENTER, null);
		lblTimeline.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
		lblTimeline.setTextBold();
		
		x = 72;
		y += iVeryLargeBoxHeight;
		btnTimeView = new GButton(timelineWindow, x, y, 170, iSmallBoxHeight, "Open Time View (3)");
		btnTimeView.tag = "SetTimeView";
		btnTimeView.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		btnTimeView.setLocalColorScheme(G4P.GOLD_SCHEME);

		x = 70;
		y += iVeryLargeBoxHeight + 5;
		btnTimelineReverse = new GButton(timelineWindow, x, y, 90, iVerySmallBoxHeight, "Reverse");
		btnTimelineReverse.tag = "TimelineReverse";
		btnTimelineReverse.setLocalColorScheme(G4P.CYAN_SCHEME);

		x += 100;
		btnTimelineForward = new GButton(timelineWindow, x, y, 90, iVerySmallBoxHeight, "Forward");
		btnTimelineForward.tag = "TimelineForward";
		btnTimelineForward.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 120;
		y += iLargeBoxHeight;
		btnTimelineZoomToField = new GButton(timelineWindow, x, y, 90, iVerySmallBoxHeight, "Field");
		btnTimelineZoomToField.tag = "TimelineZoomToFit";
		btnTimelineZoomToField.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		x = iLeftMargin;
		y += iMediumBoxHeight * 0.5f;
		lblTimelineZoomTo = new GLabel(timelineWindow, x, y, timelineWindow.width, iSmallBoxHeight, "Zoom To:");
		lblTimelineZoomTo.setLocalColorScheme(G4P.SCHEME_10);
		lblTimelineZoomTo.setFont(new Font("Monospaced", Font.ITALIC, iMediumTextSize));
		lblTimelineZoomTo.setTextBold();
		
		x = 120;
		y += iMediumBoxHeight * 0.5f;
		btnTimelineZoomToSelected = new GButton(timelineWindow, x, y, 90, iVerySmallBoxHeight, "Selected");
		btnTimelineZoomToSelected.tag = "TimelineZoomToSelected";
		btnTimelineZoomToSelected.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		x = 70;
		y += iLargeBoxHeight;
		btnTimelineZoomIn = new GButton(timelineWindow, x, y, 90, iVerySmallBoxHeight, "Zoom In");
		btnTimelineZoomIn.tag = "TimelineZoomIn";
		btnTimelineZoomIn.setLocalColorScheme(G4P.CYAN_SCHEME);

		x += 100;
		btnTimelineZoomOut = new GButton(timelineWindow, x, y, 90, iVerySmallBoxHeight, "Zoom Out");
		btnTimelineZoomOut.tag = "TimelineZoomOut";
		btnTimelineZoomOut.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 0;
		y = timelineWindowHeight - iBottomTextY;
		lblShift5 = new GLabel(timelineWindow, x, y, timelineWindow.width, iSmallBoxHeight);						/* Display Mode Label */
		lblShift5.setText("Press SHIFT + 5 to show / hide");
		lblShift5.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		lblShift5.setLocalColorScheme(G4P.SCHEME_10);
		lblShift5.setTextAlign(GAlign.CENTER, null);
		
		setupTimelineWindow = true;
		world.ml.setAppIcon = true;
	}

	/**
	 * Setup and open Library Window
	 */
	public void openLibraryWindow()
	{
		int leftEdge = world.ml.displayWidth / 2 - windowWidth;
		int topEdge = world.ml.displayHeight / 2 - libraryWindowHeight / 2;
		
		libraryWindow = GWindow.getWindow( world.ml, "", leftEdge, topEdge, windowWidth * 2, libraryWindowHeight, PApplet.JAVA2D);
		
		libraryWindow.addData(new ML_WinData());
		libraryWindow.addDrawHandler(this, "libraryWindowDraw");
		libraryWindow.addMouseHandler(this, "libraryWindowMouse");
		libraryWindow.addKeyHandler(world.ml, "libraryWindowKey");
		libraryWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		int x = 0, y = iTopMargin * 5 / 2;
		world.ml.delay(10);
		lblLibrary = new GLabel(libraryWindow, x, y, libraryWindow.width, 22, "Welcome to MultimediaLocator.");
		lblLibrary.setLocalColorScheme(G4P.SCHEME_10);
		lblLibrary.setFont(new Font("Monospaced", Font.PLAIN, iVeryLargeTextSize));
		lblLibrary.setTextAlign(GAlign.CENTER, null);
//		lblLibrary.setTextBold();

		x = 0;
		lblLibraryWindowText = new GLabel(libraryWindow, x, y, libraryWindow.width, 22, "Building media library...");
		lblLibraryWindowText.setLocalColorScheme(G4P.SCHEME_10);
		lblLibraryWindowText.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
		lblLibraryWindowText.setTextAlign(GAlign.CENTER, null);
		lblLibraryWindowText.setVisible(false);

		x = 90;
		y += 45;
		btnCreateLibrary = new GButton(libraryWindow, x, y, 170, iVeryLargeBoxHeight, "Create Library");
		btnCreateLibrary.tag = "CreateLibrary";
		btnCreateLibrary.setFont(new Font("Monospaced", Font.BOLD, iLargeTextSize));
		btnCreateLibrary.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnOpenLibrary = new GButton(libraryWindow, x+270, y, 155, iVeryLargeBoxHeight, "Open Library");
		btnOpenLibrary.tag = "OpenLibrary";
		btnOpenLibrary.setFont(new Font("Monospaced", Font.BOLD, iLargeTextSize));
		btnOpenLibrary.setLocalColorScheme(G4P.CYAN_SCHEME);

		y += 50;
		btnLibraryHelp = new GButton(libraryWindow, windowWidth * 2 - 30 - iLeftMargin, y, 30, 30, "?");
		btnLibraryHelp.tag = "LibraryHelp";
		btnLibraryHelp.setFont(new Font("Monospaced", Font.BOLD, iLargeTextSize));
		btnLibraryHelp.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		setupLibraryWindow = true;
		showLibraryWindow = true;
		world.ml.setAppIcon = true;
	}
	
	/**
	 * Set library window text
	 * @param newText New text
	 */
	public void setLibraryWindowText(String newText)
	{
		lblLibraryWindowText.setText(newText);
	}

	public void setCreateLibraryWindowText(String newText1, String newText2)
	{
		lblCreateLibraryWindowText.setText(newText1);
		if(newText2 != null) lblCreateLibraryWindowText2.setText(newText2);
	}
	
	/**
	 * Setup and show Create Library Window
	 */
	public void openCreateLibraryWindow()
	{
		int leftEdge = world.ml.displayWidth / 2 - windowWidth * 3 / 2;
		int topEdge = world.ml.displayHeight / 2 - createLibraryWindowHeight / 2;
		
		createLibraryWindow = GWindow.getWindow( world.ml, "", leftEdge, topEdge, windowWidth * 3, 
				   createLibraryWindowHeight, PApplet.JAVA2D);

		createLibraryWindow.addData(new ML_WinData());
		createLibraryWindow.addDrawHandler(this, "importWindowDraw");
		createLibraryWindow.addMouseHandler(this, "importWindowMouse");
		createLibraryWindow.addKeyHandler(world.ml, "importWindowKey");
		createLibraryWindow.setActionOnClose(GWindow.KEEP_OPEN);

		int x = 0, y = iTopMargin * 2;
		world.ml.delay(delayAmount);
		lblImport = new GLabel(createLibraryWindow, x, y, createLibraryWindow.width, 22, "Select Media Folder(s) for Library");
		lblImport.setLocalColorScheme(G4P.SCHEME_10);
		lblImport.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
		lblImport.setTextAlign(GAlign.CENTER, null);
		lblImport.setTextBold();

		x = 0;
		lblCreateLibraryWindowText = new GLabel(createLibraryWindow, x, y + 60, createLibraryWindow.width, 22, "Building media library...");
		lblCreateLibraryWindowText.setLocalColorScheme(G4P.SCHEME_10);
		lblCreateLibraryWindowText.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
		lblCreateLibraryWindowText.setTextAlign(GAlign.CENTER, null);
		lblCreateLibraryWindowText.setVisible(false);

		x = 0;
		lblCreateLibraryWindowText2 = new GLabel(createLibraryWindow, x, y + 120, createLibraryWindow.width, 22, "Please wait. The process may take several minutes...");
		lblCreateLibraryWindowText2.setLocalColorScheme(G4P.SCHEME_10);
		lblCreateLibraryWindowText2.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
		lblCreateLibraryWindowText2.setTextAlign(GAlign.CENTER, null);
		lblCreateLibraryWindowText2.setVisible(false);

		x = windowWidth * 3 / 2 - 160;
		y += 60;
		btnImportMediaFolder = new GButton(createLibraryWindow, x, y, 160, iLargeBoxHeight, "Add Folder");
		btnImportMediaFolder.tag = "AddMediaFolder";
		btnImportMediaFolder.setFont(new Font("Monospaced", Font.BOLD, iLargeTextSize));
		btnImportMediaFolder.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnMakeLibrary = new GButton(createLibraryWindow, x+220, y, 100, iLargeBoxHeight, "Done");
		btnMakeLibrary.tag = "MakeLibrary";
		btnMakeLibrary.setFont(new Font("Monospaced", Font.BOLD, iLargeTextSize));
		btnMakeLibrary.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		setupCreateLibraryWindow = true;
		showCreateLibraryWindow = true;
		
		world.ml.setAppIcon = true;
	}
	
	/**
	 * Open window to choose item from a list of strings and return index result
	 * @param list List items
	 * @return Index of chosen item from list
	 */
	public void openChooseItemDialog(ArrayList<String> list, String promptText, int resultCode)
	{
		if(list.size() > 0)
		{
			listItemWindowList = new ArrayList<String>(list);
			listItemWindowText = promptText;
			listItemWindowHeight = 120 + listItemWindowList.size() * 30;			// -- Update this
			listItemWindowSelectedItem = 0;
			listItemWindowResultCode = resultCode;					// Flag indicating what to do with dialog result value
			
			int leftEdge = world.ml.displayWidth / 2 - windowWidth;
			int topEdge = world.ml.displayHeight / 2 - listItemWindowHeight / 2;

			listItemWindow = GWindow.getWindow( world.ml, "", leftEdge, topEdge, windowWidth * 2, listItemWindowHeight, PApplet.JAVA2D);

			listItemWindow.addData(new ML_WinData());
			listItemWindow.addDrawHandler(this, "listItemWindowDraw");
			listItemWindow.addKeyHandler(world.ml, "listItemWindowKey");
//			listItemWindow.addMouseHandler(this, "listItemWindowMouse");
			listItemWindow.setActionOnClose(GWindow.KEEP_OPEN);
			
			showListItemWindowList = true;
			world.ml.setAppIcon = true;
		}
	}
	
	public void closeChooseItemDialog()
	{
		listItemWindow.setVisible(false);
		listItemWindow.close();
		listItemWindow.dispose();
		showListItemWindowList = false;
	}

	/**
	 * Open window to choose item from a list of strings and return index result
	 * @param list List items
	 * @return Index of chosen item from list
	 */
	public void openTextEntryDialog(ArrayList<String> list, String promptText, int resultCode)
	{
		if(list.size() > 0)
		{
			textEntryWindowUserEntry = "";
			textEntryWindowText = promptText;
			textEntryWindowSelectedItem = 0;
			textEntryWindowResultCode = resultCode;					// Flag indicating what to do with dialog result value
			
			int leftEdge = world.ml.displayWidth / 2 - windowWidth * 3 / 2;
			int topEdge = world.ml.displayHeight / 2 - createLibraryWindowHeight / 2;

			textEntryWindow = GWindow.getWindow( world.ml, "", leftEdge, topEdge, windowWidth * 2, textEntryWindowHeight, PApplet.JAVA2D);

			textEntryWindow.addData(new ML_WinData());
			textEntryWindow.addDrawHandler(this, "textEntryWindowDraw");
			textEntryWindow.addKeyHandler(world.ml, "textEntryWindowKey");
			textEntryWindow.setActionOnClose(GWindow.KEEP_OPEN);
			
			showTextEntryWindow = true;
			world.ml.setAppIcon = true;
		}
	}
	
	public void closeTextEntryDialog()
	{
		listItemWindow.setVisible(false);
		listItemWindow.close();
		listItemWindow.dispose();
		showListItemWindowList = false;
	}

	/**
	 * Handles drawing to the ML Window
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 */
	public void mlWindowDraw(PApplet applet, GWinData data) {
		applet.background(0);
		applet.stroke(255);
		applet.strokeWeight(1);
		applet.fill(255, 255, 255);
	}

	/**
	 * Handles mouse events for all GWindow objects
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void mlWindowMouse(PApplet applet, GWinData data, MouseEvent event) {
		ML_WinData data2 = (ML_WinData)data;
		switch(event.getAction()) {

		case MouseEvent.PRESS:
			data2.sx = data2.ex = applet.mouseX;
			data2.sy = data2.ey = applet.mouseY;
			data2.done = false;
//			System.out.println("Mouse pressed");
			break;
		case MouseEvent.RELEASE:
			data2.ex = applet.mouseX;
			data2.ey = applet.mouseY;
			data2.done = true;
//			System.out.println("Mouse released:"+data.toString());
			break;
		case MouseEvent.DRAG:
			data2.ex = applet.mouseX;
			data2.ey = applet.mouseY;
//			System.out.println("Mouse dragged");
			break;
		}
	}
	
	/**
	 * Handles drawing to the ML Window
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 */
	public void libraryWindowDraw(PApplet applet, GWinData data) 
	{
		applet.background(0);
		applet.stroke(0, 25, 255);
		applet.strokeWeight(1);
//		applet.fill(255, 255, 255);
		int barWidth = windowWidth-40;
		
		if(display.setupProgress > 0.f)
		{
			for(int i=0; i<barWidth*display.setupProgress; i++)
			{
				int x = (int)utilities.round( 40 + i * 2.f, 0 );
				if(x % 2 == 0)
					applet.line(x, libraryWindowHeight - 75, x, libraryWindowHeight - 35);
			}
		}
	}

	/**
	 * Handles mouse events for all GWindow objects
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void libraryWindowMouse(PApplet applet, GWinData data, MouseEvent event) {
//		ML_WinData data2 = (ML_WinData)data;
//		switch(event.getAction()) {
//
//		case MouseEvent.PRESS:
//			data2.sx = data2.ex = applet.mouseX;
//			data2.sy = data2.ey = applet.mouseY;
//			data2.done = false;
////			System.out.println("Mouse pressed");
//			break;
//		case MouseEvent.RELEASE:
//			data2.ex = applet.mouseX;
//			data2.ey = applet.mouseY;
//			data2.done = true;
////			System.out.println("Mouse released:"+data.toString());
//			break;
//		case MouseEvent.DRAG:
//			data2.ex = applet.mouseX;
//			data2.ey = applet.mouseY;
////			System.out.println("Mouse dragged");
//			break;
//		}
	}
	
	/**
	 * Handles drawing to the ML Window
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 */
	public void importWindowDraw(PApplet applet, GWinData data) 
	{
		float smallTextSize = 11.f;
		float mediumTextSize = 16.f;
		applet.background(0);
		applet.stroke(255, 0, 255);
		applet.strokeWeight(1);
		applet.fill(255, 255, 255);
		applet.textSize(mediumTextSize);
		
		int x = windowWidth * 3 / 2 - 80, y = 165;
		
		if(display.ml.state.selectedNewLibraryDestination)
		{
			if(display.setupProgress > 0.f)
			{
				applet.stroke(0, 25, 255);
				applet.strokeWeight(1);
				int barWidth = windowWidth * 3 - 40;

				for(int i=0; i<barWidth*display.setupProgress; i++)
				{
					x = (int)utilities.round( 40 + i * 2.f, 0 );
					if(x % 2 == 0)
						applet.line(x, createLibraryWindowHeight - 175, x, createLibraryWindowHeight - 135);
				}
			}
		}
		else
		{
			if(display.ml.library == null)
			{
				applet.text("No media folders yet.", x, y);
			}
			else
			{
				if(display.ml.library.mediaFolders == null)
				{
					applet.text("No media folders yet.", x, y);
				}
				else
				{
					if( display.ml.library.mediaFolders.size() == 0 )
						applet.text("No media folders yet.", x, y);
					else
					{
						applet.textSize(smallTextSize);
						x = iLeftMargin * 2;
						for(String strFolder : display.ml.library.mediaFolders)
						{
							applet.text(strFolder, x, y);
							y += iMediumBoxHeight;
						}
					}
				}
			}
		}
	}

	/**
	 * Handles drawing to the ML Window
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 */
	public void listItemWindowDraw(PApplet applet, GWinData data) 
	{
		applet.background(0);
		applet.fill(255);
		
		int x = iLeftMargin * 3;
		int y = iTopMargin * 2;

		applet.textSize(iLargeTextSize);
		applet.text(listItemWindowText, x, y);
		applet.textSize(iMediumTextSize);

		x += iLeftMargin * 2;
		y += iMediumBoxHeight * 1.5;
		
		if(listItemWindowList.size() == 0)
		{
			applet.text("No items in list.", x, y);
		}
		else
		{
			int count = 0;
			for(String s : listItemWindowList)
			{
				if(count == listItemWindowSelectedItem)
				{
					applet.fill(255);
					applet.text(">", x - 15, y);
				}
				else applet.fill(150);
				
				applet.text(s, x, y);

				y += 30;
				count++;
			}
		}
		
		x = iLeftMargin;
		y = listItemWindowHeight - 40;

//		applet.fill(255);
//		applet.textSize(iLargeTextSize);
//		applet.text("Press ENTER to select...", x, y);
	}

	/**
	 * Handles mouse events for all GWindow objects
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void importWindowMouse(PApplet applet, GWinData data, MouseEvent event) 
	{
		ML_WinData data2 = (ML_WinData)data;
		switch(event.getAction()) {

		case MouseEvent.PRESS:
			data2.sx = data2.ex = applet.mouseX;
			data2.sy = data2.ey = applet.mouseY;
			data2.done = false;
//			System.out.println("Mouse pressed");
			break;
		case MouseEvent.RELEASE:
			data2.ex = applet.mouseX;
			data2.ey = applet.mouseY;
			data2.done = true;
//			System.out.println("Mouse released:"+data.toString());
			break;
		case MouseEvent.DRAG:
			data2.ex = applet.mouseX;
			data2.ey = applet.mouseY;
//			System.out.println("Mouse dragged");
			break;
		}
	}

//	/**
//	 * Handles drawing to the Time Window 
//	 * @param applet the main PApplet object
//	 * @param data the data for the GWindow being used
//	 */
//	public void timeWindowDraw(PApplet applet, GWinData data) 
//	{
//		if(world.ml.state.running)
//		{
////			float lineWidthVeryWide = 20f;
////			float lineWidthWide = 15f;
////			float lineWidth = 15f;
//
////			float mediumTextSize = 13.f;
//			float smallTextSize = 11.f;
//
////			float x = 10, y = 450;			
//			applet.background(0);
//			applet.stroke(255);
//			applet.strokeWeight(1);
//			applet.fill(255, 255, 255);
//
//			applet.textSize(smallTextSize);
//			
////			int mode = world.getState().getTimeMode();
////			if( mode == 0 || mode == 1 )
////			{
////				int curTime = (mode == 0) ? world.getCurrentCluster().getState().currentTime : world.getState().currentTime;
////				applet.text(" Current Time: "+ curTime, x, y += lineWidth);
////			}
//
//			if(setupNavigationWindow)
//			{
//				if(world.state.timeFading && !world.state.paused)
//					sdrCurrentTime.setValue(world.getCurrentTimePoint());
//			}
//
////			applet.text(" Current Field Time: "+ world.currentTime, x, y += lineWidth);
////			applet.text(" Current Field Time Segment: "+ world.viewer.getCurrentFieldTimeSegment(), x, y += lineWidthVeryWide);
////			applet.text(" Current Field Timeline Size: "+ world.getCurrentField().getTimeline().timeline.size(), x, y += lineWidth);
////			applet.text(" Current Field Dateline Size: "+ world.getCurrentField().getDateline().size(), x, y += lineWidth);
//		}
//	}

	/**
	 * Handles drawing to the Navigation Window
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 */
	public void navigationWindowDraw(PApplet applet, GWinData data) 
	{
//		applet.background(10, 5, 50);
		applet.background(0);
		applet.colorMode(PConstants.HSB);
		applet.stroke(0, 0, 65, 255);
		applet.strokeWeight(1);
		applet.line(0, navigationWindowLineBreakY_1, windowWidth, navigationWindowLineBreakY_1);
		applet.stroke(0, 0, 155, 255);
		applet.strokeWeight(2);
		applet.line(0, navigationWindowLineBreakY_2, windowWidth, navigationWindowLineBreakY_2);
//		int yPos = window.height - 60;
//		applet.text("WorldMediaViewer v1.0", window.width / 2 - 10, yPos);
//		applet.text("David Gordon", window.width / 2 - 10, yPos += 20);
	}


	/**
	 * Handles mouse events for Navigation Window
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void navigationWindowMouse(PApplet applet, GWinData data, MouseEvent event) {
		ML_WinData data2 = (ML_WinData)data;
		switch(event.getAction()) {

		case MouseEvent.PRESS:
			data2.sx = data2.ex = applet.mouseX;
			data2.sy = data2.ey = applet.mouseY;
			data2.done = false;
//			System.out.println("Mouse pressed");
			break;
		case MouseEvent.RELEASE:
			data2.ex = applet.mouseX;
			data2.ey = applet.mouseY;
			data2.done = true;
//			System.out.println("Mouse released:"+data.toString());
			break;
		case MouseEvent.DRAG:
			data2.ex = applet.mouseX;
			data2.ey = applet.mouseY;
//			System.out.println("Mouse dragged");
			break;
		}
	}

	/**
	 * Handles drawing to the Media Window
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 */
	public void mediaWindowDraw(PApplet applet, GWinData data) 
	{
		applet.background(0);
	
		applet.colorMode(PConstants.HSB);
		applet.stroke(0, 0, 155, 255);
		applet.strokeWeight(2);
		if(!compressTallWindows)
			applet.line(0, mediaWindowLineBreakY_1, windowWidth, mediaWindowLineBreakY_1);
		if(!compressTallWindows)
			applet.line(0, mediaWindowLineBreakY_2, windowWidth, mediaWindowLineBreakY_2);

		if(setupMediaWindow)
		{
			if(world.state.timeFading && !world.state.paused)
				sdrVisibleAngle.setValue(world.viewer.getFieldOfView());
		}
	}

	/**
	 * Handles mouse events for Graphics Window
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void mediaWindowMouse(PApplet applet, GWinData data, MouseEvent event) 
	{
		ML_WinData data2 = (ML_WinData)data;
		switch(event.getAction()) {

		case MouseEvent.PRESS:
			data2.sx = data2.ex = applet.mouseX;
			data2.sy = data2.ey = applet.mouseY;
			data2.done = false;
			break;
		case MouseEvent.RELEASE:
			data2.ex = applet.mouseX;
			data2.ey = applet.mouseY;
			data2.done = true;
//			System.out.println("Mouse released:"+data.toString());
			break;
		case MouseEvent.DRAG:
			data2.ex = applet.mouseX;
			data2.ey = applet.mouseY;
			break;
		}
	}

	/**
	 * Handles drawing to the Statistics Window
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 */
	public void statisticsWindowDraw(PApplet applet, GWinData data) 
	{
		if(world.ml.state.running)
		{
			applet.background(0);
			applet.stroke(255);
			applet.strokeWeight(1);
			applet.fill(255, 255, 255);

			float lineWidthVeryWide = 20f;

			float x = iLeftMargin;
			float y = 90;			// Starting vertical position

			WMV_Field f = world.getCurrentField();
			
			if(world.viewer.getState().getCurrentClusterID() >= 0)
			{
				WMV_Cluster c = world.getCurrentCluster();

//				applet.textSize(fLargeTextSize);
//				applet.text("General", x, y += lineWidthVeryWide);
//				applet.textSize(fMediumTextSize);
//				applet.text(" Library:"+world.ml.library.getLibraryFolder(), x, y += lineWidthVeryWide * 1.5f);

//				applet.textSize(fLargeTextSize);
//				applet.text("Graphics", x, y += lineWidthVeryWide * 1.5f);
//				applet.textSize(fMediumTextSize);
//				applet.text(" Alpha Mode:"+world.getState().alphaMode, x, y += lineWidthVeryWide * 1.5f);
//				applet.text(" Angle Fading:"+world.viewer.getSettings().angleFading, x, y += lineWidthVeryWide);
//				applet.text(" Angle Thinning:"+world.viewer.getSettings().angleThinning, x, y += lineWidthVeryWide);
//
//				applet.textSize(fLargeTextSize);
//				applet.text("Model", x, y += lineWidthVeryWide * 1.5f);
//				applet.textSize(fMediumTextSize);
//				applet.text(" Orientation Mode:"+world.viewer.getSettings().orientationMode, x, y += lineWidthVeryWide * 1.5f);
//				applet.text(" Altitude Scaling: "+world.getSettings().altitudeScaling, x, y += lineWidthVeryWide);
//				applet.text(" Altitude Scaling Factor: "+world.getSettings().altitudeScalingFactor, x, y += lineWidthVeryWide);
//				applet.text(" Lock Media to Clusters:"+world.lockMediaToClusters, x, y += lineWidthVeryWide);

//				applet.textSize(fLargeTextSize);
//				applet.text("Viewer", x, y);
//				applet.text("Viewer", x, y += lineWidthVeryWide * 1.5f);
				
				applet.textSize(fMediumTextSize);
				applet.text(" GPS Longitude:  "+utilities.round( world.viewer.getGPSLocation().x, 4 )+" Latitude:"+utilities.round( world.viewer.getGPSLocation().y, 4 ), x, y += lineWidthVeryWide);		
//				applet.text(" Location, x:  "+utilities.round(world.viewer.getLocation().x, 2)+" y:"+utilities.round(world.viewer.getLocation().y, 2)+" z:"+
//							utilities.round(world.viewer.getLocation().z, 2), x, y += lineWidthVeryWide);		
				applet.text(" Current Cluster:  "+ (c.getID()+1)+" of "+ f.getClusters().size(), x, y += lineWidthVeryWide * 1.5f);
				applet.text(" Clusters Visible:  "+world.getVisibleClusters().size(), x, y += lineWidthVeryWide);
				applet.text(" Field of View:" + utilities.round( world.viewer.getSettings().fieldOfView, 3), x, y += lineWidthVeryWide);
//				applet.text(" 	Distance (m.):  "+PVector.dist(world.viewer.getLocation(), world.getCurrentCluster().getLocation()), x, y += lineWidthVeryWide);
//				if(world.state.timeMode == 0 && c.getDateline() != null)	// Cluster Time Mode
//				{
//					if(c.getDateline().size() > 0)
//					{
//						int clusterDate = world.getCurrentField().getTimeSegment(world.viewer.getCurrentFieldTimeSegment()).getClusterDateID();
//						applet.text("   Current Cluster Time Segment ID:  "+ (world.getCurrentField().getTimeSegment(world.viewer.getCurrentFieldTimeSegment()).getClusterTimelineID()+1)+"  of "+ c.getTimeline().timeline.size() +" in Cluster Main Timeline", x, y += lineWidthVeryWide);
//						applet.text("   Date:  "+ (clusterDate+1) +" of "+ c.getDateline().size(), x, y += lineWidth);
//					}
//				}

//				applet.textSize(fLargeTextSize);
//				applet.text("World", x, y += lineWidthVeryWide * 1.5f);
				
				y = 240;
//				applet.textSize(fMediumTextSize);
				if(world.getFieldCount() > 1)
					applet.text(" Current Field #"+ (f.getID()+1)+" of "+ world.getFields().size(), x, y += lineWidthVeryWide);
				applet.text(" Field Width:  " + utilities.round( f.getModel().getState().fieldWidth, 3 ) + " Length:  "+utilities.round( f.getModel().getState().fieldLength, 3), x, y += lineWidthVeryWide);
				applet.text(" Field Height:  " + utilities.round( f.getModel().getState().fieldHeight, 3 ), x, y += lineWidthVeryWide);
				applet.text(" Points of Interest:  "+f.getClusters().size(), x, y += lineWidthVeryWide * 1.5f);
				applet.text(" Media Density (per sq. m.):  "+utilities.round( f.getModel().getState().mediaDensity, 3 ), x, y += lineWidthVeryWide);
			
//				if(world.state.timeMode == 1 && f.getDateline().size() > 0)	// Field Time Mode
//				{
//					int fieldDate = world.getCurrentField().getTimeSegment(world.viewer.getCurrentFieldTimeSegment()).getFieldDateID();
//					applet.text("   Current Field Time Segment ID:  "+ world.viewer.getCurrentFieldTimeSegment()+" of "+ world.getCurrentField().getTimeline().timeline.size() +" in Main Timeline", x, y += lineWidthVeryWide);
//					applet.text("   Date:  "+ (fieldDate)+" of "+ world.getCurrentField().getDateline().size(), x, y += lineWidth);
////					applet.text("   Date-Specific ID:  "+ world.getCurrentField().getTimeSegment(world.viewer.getCurrentFieldTimeSegment()).getFieldTimelineIDOnDate()
////							+" of "+ world.getCurrentField().getTimelines().get(fieldDate).timeline.size() + " in Timeline #"+(fieldDate), x, y += lineWidth);
//				}
				
				if(f.getImageCount() > 0) applet.text(" Images:  "+f.getImageCount(), x, y += lineWidthVeryWide);		// --  Add missing originals
				if(f.getImagesVisible() > 0) 
				{
					applet.text("   In Visible Range:  "+f.getImagesVisible(), x, y += lineWidthVeryWide);
//					applet.text("   Seen:  "+f.getImagesSeen(), x, y += lineWidthVeryWide);
				}
				
				if(f.getPanoramaCount() > 0) applet.text(" Panoramas:  "+f.getPanoramaCount(), x, y += lineWidthVeryWide);		
				if(f.getPanoramasVisible() > 0)
				{
					applet.text("   In Visible Range:  "+f.getPanoramasVisible(), x, y += lineWidthVeryWide);
//					applet.text("   Seen:  "+f.getPanoramasSeen(), x, y += lineWidthVeryWide);
				}

				if(f.getVideoCount() > 0) applet.text(" Videos:  "+f.getVideoCount(), x, y += lineWidthVeryWide);					
				if(f.getVideosVisible() > 0)
				{
					applet.text("   In Visible Range:  "+f.getVideosVisible(), x, y += lineWidthVeryWide);
					if(f.getVideosPlaying() > 0)
					{
						applet.text("   Playing:  "+f.getVideosPlaying(), x, y += lineWidthVeryWide);
//						applet.text("   Seen:  "+f.getVideosSeen(), x, y += lineWidthVeryWide);
					}
				}
				
				if(f.getSoundCount() > 0) applet.text(" Sounds:  "+f.getSoundCount(), x, y += lineWidthVeryWide);					
				if(f.getSoundsAudible() > 0)
				{
					applet.text(" In Audible Range:  "+f.getSoundsAudible(), x, y += lineWidthVeryWide);
					if(f.getSoundsPlaying() > 0) 
					{
						applet.text("   Playing:  "+f.getSoundsPlaying(), x, y += lineWidthVeryWide);
//						applet.text("   Heard:  "+f.getSoundsHeard(), x, y += lineWidthVeryWide);
					}
				}
				
				if(f.getGPSTracks() != null)
					if(f.getGPSTracks().size() > 0) 
						applet.text(" GPS Tracks: "+f.getGPSTracks().size(), x, y += lineWidthVeryWide);			
				
//				applet.textSize(fLargeTextSize);
//				applet.text("Output", x, y += lineWidthVeryWide * 1.5f);
//				applet.textSize(fMediumTextSize);
//				applet.text(" Media Output Folder:"+world.outputFolder, x, y += lineWidthVeryWide * 1.5f);

				if(world.ml.debugSettings.memory)
				{
					if(world.ml.debugSettings.detailed)
					{
						applet.text("Total memory (bytes): " + world.ml.totalMemory, x, y += lineWidthVeryWide);
						applet.text("Available processors (cores): "+world.ml.availableProcessors, x, y += lineWidthVeryWide);
						applet.text("Maximum memory (bytes): " +  (world.ml.maxMemory == Long.MAX_VALUE ? "no limit" : world.ml.maxMemory), x, y += lineWidthVeryWide); 
						applet.text("Total memory (bytes): " + world.ml.totalMemory, x, y += lineWidthVeryWide);
						applet.text("Allocated memory (bytes): " + world.ml.allocatedMemory, x, y += lineWidthVeryWide);
					}
					applet.text("Free memory (bytes): "+world.ml.freeMemory, x, y += lineWidthVeryWide);
					applet.text("Approx. usable free memory (bytes): " + world.ml.approxUsableFreeMemory, x, y += lineWidthVeryWide);
				}			
			}
		}
	}
	
	/**
	 * Handles mouse events for Statistics Window 
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
//	public void statisticsWindowMouse(PApplet applet, GWinData data, MouseEvent event) {
//		ML_WinData wmvWinData = (ML_WinData)data;
//		switch(event.getAction()) {
//
//		case MouseEvent.PRESS:
//			wmvWinData.sx = wmvWinData.ex = applet.mouseX;
//			wmvWinData.sy = wmvWinData.ey = applet.mouseY;
//			wmvWinData.done = false;
//			break;
//		case MouseEvent.RELEASE:
//			wmvWinData.ex = applet.mouseX;
//			wmvWinData.ey = applet.mouseY;
//			wmvWinData.done = true;
//			break;
//		case MouseEvent.DRAG:
//			wmvWinData.ex = applet.mouseX;
//			wmvWinData.ey = applet.mouseY;
//			break;
//		}
//	}
	
	/**
	 * Handles drawing to the Help Window PApplet area
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 */
	public void helpWindowDraw(PApplet applet, GWinData data) 
	{
		applet.background(0,0,0);
		applet.stroke(255);
		applet.strokeWeight(1);
		applet.fill(255, 255, 255);
		
		float xPos = 235;
		float yPos = 45;			// Starting vertical position

		applet.fill(255, 255, 255, 255); 	// White text color                        

		if(helpAboutText == 0)
		{
			applet.textSize(fLargeTextSize);
			applet.text(" About", xPos, yPos);

			applet.textSize(fMediumTextSize);
			applet.text(" MultimediaLocator is a media library management and visualization system that allows users to experience image, video,    ", xPos, yPos += lineWidthVeryWide * 3.0f);
			applet.text(" and sound collections as navigable virtual environments. Like existing library management programs, such as Apple's    ", xPos, yPos += lineWidthVeryWide * 1.2f);
			applet.text(" Photos app, it allows importing, displaying, and exporting media using a grid or satellite map interface. However,     ", xPos, yPos += lineWidthVeryWide * 1.2f);
			applet.text(" MultimediaLocator’s distinctive features lie in both its incorporation of time-based media, such as sounds and videos,      ", xPos, yPos += lineWidthVeryWide * 1.2f);
			applet.text(" and its innovative viewing and navigation methods, made possible only by its 3D spatial browsing framework.    ", xPos, yPos += lineWidthVeryWide * 1.2f);

			applet.text(" MultimediaLocator lets photographers, composers and artists collaborate across disciplines to create virtual tours,     ", xPos, yPos += lineWidthVeryWide * 2.0f);
			applet.text(" build interactive multimedia installations, visualize the creative process over time, or simply view one’s personal media     ", xPos, yPos += lineWidthVeryWide * 1.2f);
			applet.text(" collection in new ways, without writing custom software. Users can view and move freely around virtual environments,     ", xPos, yPos += lineWidthVeryWide * 1.2f);
			applet.text(" identify and navigate between spatial or temporal points of interest, \"play back\" media files to watch them fade     ", xPos, yPos += lineWidthVeryWide * 1.2f);
			applet.text(" chronologically, save and revisit points of interest, and change a wide array of display parameters in real-time.     ", xPos, yPos += lineWidthVeryWide * 1.2f);
			applet.text(" MultimediaLocator’s metadata-based approach creates, loads and saves environments without accessing media     ", xPos, yPos += lineWidthVeryWide * 1.2f);
			applet.text(" data (pixels, frames or samples), until files are needed for display. Since metadata is much smaller and faster to read,     ", xPos, yPos += lineWidthVeryWide * 1.2f);
			applet.text(" this approach is especially useful for large or complex media collections.     ", xPos, yPos += lineWidthVeryWide * 1.2f);

			applet.text(" Whereas existing image-based software for creating virtual environments (e.g. Photosynth) place restrictions on loading     ", xPos, yPos += lineWidthVeryWide * 2.0f);
			applet.text(" images spaced far apart, taken at different times of day, or containing  \"artifacts\" such as motion blur or high contrast     ", xPos, yPos += lineWidthVeryWide * 1.2f);
			applet.text(" areas, MultimediaLocator’s metadata method has no such restrictions. By preserving the original rectangular image and     ", xPos, yPos += lineWidthVeryWide * 1.2f);
			applet.text(" video borders, along with any shifts in light, distortions, gaps, juxtapositions and errors due to GPS uncertainty,     ", xPos, yPos += lineWidthVeryWide * 1.2f);
			applet.text(" MultimediaLocator is designed to embrace the aesthetic and perceptual qualities of change, incompleteness, uncertainty     ", xPos, yPos += lineWidthVeryWide * 1.2f);
			applet.text(" and discontinuity, while offering a flexible, time-based alternative to existing 3D media browsing systems.    ", xPos, yPos += lineWidthVeryWide * 1.2f);

			applet.text(" * Note: For most users, recording files with the correct metadata information will require some additional effort. See     ", xPos, yPos += lineWidthVeryWide * 2.0f);
			applet.text(" “Importing Files” for more information.        ", xPos, yPos += lineWidthVeryWide * 1.2f);
		}
		else if(helpAboutText == 1)
		{
			applet.textSize(fLargeTextSize);
			applet.text(" Importing Files", xPos, yPos);

			applet.textSize(fMediumTextSize);
//			applet.text(" When importing media, MultimediaLocator checks file types and looks for the required metadata, and will return an error     ", xPos, yPos += lineWidthVeryWide * 3.0f);
//			applet.text(" if key fields are missing or incomplete. Please follow the following guidelines when importing media files:    ", xPos, yPos += lineWidthVeryWide * 1.2f);

			applet.text(" Image     ", xPos, yPos += lineWidthVeryWide * 2.0f);
			applet.text(" Supported Formats: JPG    ", xPos, yPos += lineWidthVeryWide * 1.2f);
			applet.text(" Metadata: Date/Time, GPS Location, Compass Orientation, Altitude, Elevation Angle*, Rotation Angle*    ", xPos, yPos += lineWidthVeryWide * 1.2f);

			applet.text(" * Images may be taken using an iPhone with Location Services is turned on, or a Nikon DSLR with a GPS attachment. However,     ", xPos, yPos += lineWidthVeryWide * 1.7f);
			applet.text(" to correctly display images where the camera was not directly pointed at the horizon, or was held at an angle, using the      ", xPos, yPos += lineWidthVeryWide * 1.2f);
			applet.text(" iPhone Theodolite app, which records elevation and rotation angles in IPTC metadata, is required. I plan to add support       ", xPos, yPos += lineWidthVeryWide * 1.2f);
			applet.text(" for additional cameras in the future.    ", xPos, yPos += lineWidthVeryWide * 1.2f);

			applet.text(" Video    ", xPos, yPos += lineWidthVeryWide * 2.0f);
			applet.text(" Supported Formats: MOV    ", xPos, yPos += lineWidthVeryWide * 1.2f);
			applet.text(" Metadata: Date/Time, GPS Location, Compass Orientation, Altitude, Elevation Angle†, Rotation Angle†    ", xPos, yPos += lineWidthVeryWide * 1.2f);

			applet.text(" † As with images, videos can be imported from an iPhone or Nikon DSLR with GPS attachment, however if the camera     ", xPos, yPos += lineWidthVeryWide * 1.7f);
			applet.text(" was not facing the horizon or was rotated from vertical, elevation and rotation angles are necessary to correctly    ", xPos, yPos += lineWidthVeryWide * 1.2f);
			applet.text(" correctly display these videos. Since Theodolite does not currently record video with the necessary metadata, until a    ", xPos, yPos += lineWidthVeryWide * 1.2f);
			applet.text(" better solution is available, I designed a workaround to display videos with elevation and rotation angle metadata:     ", xPos, yPos += lineWidthVeryWide * 1.2f);
			applet.text(" taking a Theodolite image immediately before recording a video at the same orientation. This method will associate      ", xPos, yPos += lineWidthVeryWide * 1.2f);
			applet.text(" the image as a “placeholder,” so the video displays with the same orientation.    ", xPos, yPos += lineWidthVeryWide * 1.2f);

			applet.text(" Sound     ", xPos, yPos += lineWidthVeryWide * 2.0f);
			applet.text(" Supported Formats: WAV, AIFF    ", xPos, yPos += lineWidthVeryWide * 1.2f);
			applet.text(" Metadata: Date/Time††    ", xPos, yPos += lineWidthVeryWide * 1.2f);

			applet.text(" †† Since most currently available sound recording devices do not record GPS metadata, sound files must be imported along     ", xPos, yPos += lineWidthVeryWide * 1.7f);
			applet.text(" with an associated GPS track file in .GPX format, recorded simultaneously with the sounds. In addition, sound files must     ", xPos, yPos += lineWidthVeryWide * 1.2f);
			applet.text(" be unedited, so that the original file creation date/time remains unchanged.    ", xPos, yPos += lineWidthVeryWide * 1.2f);
		}
		else if(helpAboutText == 2)
		{
			applet.textSize(fLargeTextSize);
			applet.text(" Keyboard Controls ", xPos, yPos);

			yPos += lineWidthVeryWide;
			applet.textSize(fMediumTextSize);
			applet.text(" General", xPos, yPos += lineWidthVeryWide * 1.5f);
			applet.textSize(fSmallTextSize);
			applet.text(" space  	 	   	Show/Hide Main Menu ", xPos, yPos += lineWidthVeryWide);
			applet.text(" SHIFT + [1-8]  	Show/Hide Windows ", xPos, yPos += lineWidthVeryWide);
			applet.text(" R    		Restart", xPos, yPos += lineWidthVeryWide);
			applet.text(" CMD + q   Quit ", xPos, yPos += lineWidthVeryWide);

			/* Movement */
			applet.textSize(fMediumTextSize);
			applet.text(" Movement", xPos, yPos += lineWidthVeryWide * 1.5f);
			applet.textSize(fSmallTextSize);
			applet.text(" a d w s   Walk Lt/Rt/Fwd/Bkwd", xPos, yPos += lineWidthVeryWide * 1.5f);
			applet.text(" Arrows    Turn Lt/Rt/Up/Down", xPos, yPos += lineWidthVeryWide);
			applet.text(" q z  Zoom In / Out + / - ", xPos, yPos += lineWidthVeryWide);

			/* Navigation */
			applet.textSize(fMediumTextSize);
			applet.text(" Navigation", xPos, yPos += lineWidthVeryWide * 1.5f);
			applet.textSize(fSmallTextSize);
			applet.text(" m    Move to Nearest Cluster", xPos, yPos += lineWidthWide * 1.5f);
			applet.text(" j    Move to Random Cluster", xPos, yPos += lineWidthVeryWide);
			applet.text(" >    Follow Timeline", xPos, yPos += lineWidthVeryWide);
//			applet.text(" .    Follow Single Date Timeline", xPos, yPos += lineWidthVeryWide);
			applet.text(" { }  Teleport to Next / Previous Field ", xPos, yPos += lineWidthVeryWide);
			applet.text(" L    Look for Media", xPos, yPos += lineWidthVeryWide);
//			applet.text(" l    Look At Selected Media", xPos, yPos += lineWidthVeryWide);
//			applet.text(" A    Move to Next Location in Memory", xPos, yPos += lineWidthVeryWide);
//			applet.text(" C    Lock Viewer to Nearest Cluster On/Off", xPos, yPos += lineWidthWide);

			applet.textSize(fMediumTextSize);
			applet.text(" Time Navigation", xPos, yPos += lineWidthVeryWide * 1.5f);
			applet.textSize(fSmallTextSize);
			applet.text(" n    Move to Next Cluster in Time", xPos, yPos += lineWidthWide * 1.5f);
			applet.text(" b    Move to Previous Cluster in Time", xPos, yPos += lineWidthVeryWide);
//			applet.text(" i    Move to Next Image ", xPos, yPos += lineWidthVeryWide);
//			applet.text(" m    Move to Next Panorama ", xPos, yPos += lineWidthVeryWide);
//			applet.text(" v    Move to Next Video ", xPos, yPos += lineWidthVeryWide);
//			applet.text(" u    Move to Next Sound ", xPos, yPos += lineWidthVeryWide);

			/* Interaction */
			applet.textSize(fMediumTextSize);
			applet.text(" Interaction", xPos, yPos += lineWidthVeryWide * 1.5f);
			applet.textSize(fSmallTextSize);
			applet.text(" A    Enable / Disable Selection", xPos, yPos += lineWidthWide * 1.5f);
			applet.text(" x    Select Media in Front", xPos, yPos += lineWidthVeryWide);
			applet.text(" X    Deselect Media in Front", xPos, yPos += lineWidthVeryWide);
			applet.text(" S    Multi-Selection On/Off", xPos, yPos += lineWidthVeryWide);
			applet.text(" OPT + s    Segment Selection On/Off", xPos, yPos += lineWidthWide);
			applet.text(" OPT + x    Deselect All Media", xPos, yPos += lineWidthVeryWide);

			xPos += 340;
			yPos = 40 + lineWidthVeryWide * 2.f;			// Starting vertical position

			/* Time */
			applet.textSize(fMediumTextSize);
			applet.text(" Time", xPos, yPos += lineWidthVeryWide * 1.5f);
			applet.textSize(fSmallTextSize);
			applet.text(" T    Time Fading On/Off", xPos, yPos += lineWidthVeryWide * 1.5f);
			applet.text(" Z    Toggle Time Fading [Cluster/Field]", xPos, yPos += lineWidthVeryWide);
			applet.text(" -    Pause On/Off   ", xPos, yPos += lineWidthVeryWide);
			applet.text(" &/*  Default Media Length - / +", xPos, yPos += lineWidthVeryWide);
			applet.text(" ⇧ Lt/Rt   Cycle Length - / +", xPos, yPos += lineWidthVeryWide);
			applet.text(" ⇧ Up/Dn   Current Time - / +", xPos, yPos += lineWidthVeryWide);

			/* Graphics */
			applet.textSize(fMediumTextSize);
			applet.text(" Graphics", xPos, yPos += lineWidthVeryWide * 1.5f);
			applet.textSize(fSmallTextSize);
			applet.text(" G     Angle Fading On/Off", xPos, yPos += lineWidthVeryWide * 1.5f);
			applet.text(" H     Angle Thinning On/Off", xPos, yPos += lineWidthVeryWide);
			applet.text(" P     Alpha Mode  On/Off   ", xPos, yPos += lineWidthVeryWide);
			applet.text(" ( )   Alpha Level - / +      ", xPos, yPos += lineWidthVeryWide);
			applet.text(" I     Hide Images ", xPos, yPos += lineWidthVeryWide);
			applet.text(" P     Hide Panoramas ", xPos, yPos += lineWidthVeryWide);
			applet.text(" V     Hide Videos ", xPos, yPos += lineWidthVeryWide);
			applet.text(" S     Hide Sounds    ", xPos, yPos += lineWidthVeryWide);

			/* Model */
			applet.textSize(fMediumTextSize);
			applet.text(" Model", xPos, yPos += lineWidthVeryWide * 1.5f);
			applet.textSize(fSmallTextSize);
			applet.text(" [ ]   Altitude Scaling Factor  + / - ", xPos, yPos += lineWidthVeryWide * 1.5f);
			applet.text(" - =   Object Distance  - / +      ", xPos, yPos += lineWidthVeryWide);
			applet.text(" ⌥ -   Visible Angle  -      ", xPos, yPos += lineWidthVeryWide);
			applet.text(" ⌥ =   Visible Angle  +      ", xPos, yPos += lineWidthVeryWide);

			applet.textSize(fMediumTextSize);
			applet.text(" GPS Tracks", xPos, yPos += lineWidthVeryWide * 1.5f);
			applet.textSize(fSmallTextSize);
			applet.text(" g    		 Load GPS Track", xPos, yPos += lineWidthVeryWide * 1.5f);
			applet.text(" OPT + g    Follow GPS Track", xPos, yPos += lineWidthVeryWide);

			xPos += 340;
			yPos = 50 + lineWidthVeryWide * 2.f;			// Starting vertical position

			applet.textSize(fMediumTextSize);
			applet.text(" Memory", xPos, yPos += lineWidthVeryWide * 1.5f);
			applet.textSize(fSmallTextSize);
			applet.text(" `    Remember Current View", xPos, yPos += lineWidthVeryWide * 1.5f);
			applet.text(" ~    Follow Memory Path", xPos, yPos += lineWidthVeryWide);
			applet.text(" Y    Clear Memory", xPos, yPos += lineWidthVeryWide);

			applet.textSize(fMediumTextSize);
			applet.text(" Output", xPos, yPos += lineWidthVeryWide * 1.5f);
			applet.textSize(fSmallTextSize);
			applet.text(" O    Set Image Output Folder", xPos, yPos += lineWidthVeryWide * 1.5f);
			applet.text(" o    Export Selected Media / Screenshot", xPos, yPos += lineWidthVeryWide);
		}
	}

	/**
	 * Handles mouse events for Help Window 
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void helpWindowMouse(PApplet applet, GWinData data, MouseEvent event) {
		ML_WinData wmvWinData = (ML_WinData)data;
		switch(event.getAction()) {

		case MouseEvent.PRESS:
			wmvWinData.sx = wmvWinData.ex = applet.mouseX;
			wmvWinData.sy = wmvWinData.ey = applet.mouseY;
			wmvWinData.done = false;
//			System.out.println("Mouse pressed");
			break;
		case MouseEvent.RELEASE:
			wmvWinData.ex = applet.mouseX;
			wmvWinData.ey = applet.mouseY;
			wmvWinData.done = true;
//			System.out.println("Mouse released:"+data.toString());
			break;
		case MouseEvent.DRAG:
			wmvWinData.ex = applet.mouseX;
			wmvWinData.ey = applet.mouseY;
//			System.out.println("Mouse dragged");
			break;
		}
	}
	
	/**
	 * Handles drawing to the Memory Window 
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 */
	public void mapWindowDraw(PApplet applet, GWinData data) 
	{
		applet.background(0,0,0);
		applet.stroke(255);
		applet.strokeWeight(1);
		applet.fill(255, 255, 255);

//		float x = 10;
//		float y = 150;			// Starting vertical position
	}

	/**
	 * Handles mouse events for Map Window 
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void mapWindowMouse(PApplet applet, GWinData data, MouseEvent event) {
		ML_WinData wmvWinData = (ML_WinData)data;
		switch(event.getAction()) {

		case MouseEvent.PRESS:
			wmvWinData.sx = wmvWinData.ex = applet.mouseX;
			wmvWinData.sy = wmvWinData.ey = applet.mouseY;
			wmvWinData.done = false;
//			System.out.println("Mouse pressed");
			break;
		case MouseEvent.RELEASE:
			wmvWinData.ex = applet.mouseX;
			wmvWinData.ey = applet.mouseY;
			wmvWinData.done = true;
//			System.out.println("Mouse released:"+data.toString());
			break;
		case MouseEvent.DRAG:
			wmvWinData.ex = applet.mouseX;
			wmvWinData.ey = applet.mouseY;
//			System.out.println("Mouse dragged");
			break;
		}
	}

	/**
	 * Handles drawing to the Memory Window 
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 */
	public void timelineWindowDraw(PApplet applet, GWinData data) 
	{
		applet.background(0,0,0);
		applet.stroke(255);
		applet.strokeWeight(1);
		applet.fill(255, 255, 255);

//		float x = 10;
//		float y = 150;			// Starting vertical position

	}

	/**
	 * Handles mouse events for Map Window 
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void timelineWindowMouse(PApplet applet, GWinData data, MouseEvent event) {
		ML_WinData wmvWinData = (ML_WinData)data;
		switch(event.getAction()) {

		case MouseEvent.PRESS:
			wmvWinData.sx = wmvWinData.ex = applet.mouseX;
			wmvWinData.sy = wmvWinData.ey = applet.mouseY;
			wmvWinData.done = false;
			break;
		case MouseEvent.RELEASE:
			wmvWinData.ex = applet.mouseX;
			wmvWinData.ey = applet.mouseY;
			wmvWinData.done = true;
			break;
		case MouseEvent.DRAG:
			wmvWinData.ex = applet.mouseX;
			wmvWinData.ey = applet.mouseY;
			break;
		}
	}

	public void showMLWindow()
	{
		showMLWindow = true;
		mlWindow.setVisible(true);
	} 
	
	public void showNavigationWindow()
	{
		showNavigationWindow = true;
		if(setupNavigationWindow)
			navigationWindow.setVisible(true);
		if(showMLWindow)
			hideMLWindow();
	} 
	public void showMediaWindow()
	{
		showMediaWindow = true;
		if(setupMediaWindow)
			mediaWindow.setVisible(true);
		if(showMLWindow)
			hideMLWindow();
	}
	public void showStatisticsWindow()
	{
		showStatisticsWindow = true;
		if(setupStatisticsWindow)
			statisticsWindow.setVisible(true);
		if(showMLWindow)
			hideMLWindow();
	} 
	public void showHelpWindow()
	{
		showHelpWindow = true;
		if(setupHelpWindow)
			helpWindow.setVisible(true);
		if(showMLWindow)
			hideMLWindow();
		helpAboutText = 0;					// Always start with "About" text
	}
	public void showMapWindow()
	{
		showMapWindow = true;
		if(setupMapWindow)
			mapWindow.setVisible(true);
	}
	public void showTimelineWindow()
	{
		showTimelineWindow = true;
		if(setupTimelineWindow)
			timelineWindow.setVisible(true);
	}
	public void showLibraryWindow()
	{
		showLibraryWindow = true;
		if(setupLibraryWindow)
			libraryWindow.setVisible(true);
		if(showMLWindow)
			hideMLWindow();
	}
	public void hideMLWindow()
	{
		showMLWindow = false;
		mlWindow.setVisible(false);
	} 
	public void hideNavigationWindow()
	{
		showNavigationWindow = false;
		if(setupNavigationWindow)
			navigationWindow.setVisible(false);
	} 
	public void closeNavigationWindow()
	{
		showNavigationWindow = false;
		if(setupNavigationWindow)
		{
			navigationWindow.setVisible(false);
			navigationWindow.close();
			navigationWindow.dispose();
			setupNavigationWindow = false;
		}
	} 
	public void hideMediaWindow()
	{
		showMediaWindow = false;
		if(setupMediaWindow)
			mediaWindow.setVisible(false);
	}
	public void closeMediaWindow()
	{
		showMediaWindow = false;
		if(setupMediaWindow)
		{
			mediaWindow.setVisible(false);
			mediaWindow.close();
			mediaWindow.dispose();
			setupMediaWindow = false;
		}
	} 
	public void hideStatisticsWindow()
	{
		showStatisticsWindow = false;
		if(setupStatisticsWindow)
			statisticsWindow.setVisible(false);
	} 
	public void closeStatisticsWindow()
	{
		showStatisticsWindow = false;
		if(setupStatisticsWindow)
		{
			statisticsWindow.setVisible(false);
			statisticsWindow.close();
			statisticsWindow.dispose();
			setupStatisticsWindow = false;
		}
	} 
	public void hideHelpWindow()
	{
		showHelpWindow = false;
		if(setupHelpWindow)
			helpWindow.setVisible(false);
	}
	public void closeHelpWindow()
	{
		showHelpWindow = false;
		if(setupHelpWindow)
		{
			helpWindow.setVisible(false);
			helpWindow.close();
			helpWindow.dispose();
			setupHelpWindow = false;
		}
	} 
	public void hideMapWindow()
	{
		showMapWindow = false;
		if(setupMapWindow)
			mapWindow.setVisible(false);
	}
	public void closeMapWindow()
	{
		showMapWindow = false;
		if(setupMapWindow)
		{
			mapWindow.setVisible(false);
			mapWindow.close();
			mapWindow.dispose();
			setupMapWindow = false;
		}
	} 
	public void hideTimelineWindow()
	{
		showTimelineWindow = false;
		if(setupTimelineWindow)
			timelineWindow.setVisible(false);
	}
	public void closeTimelineWindow()
	{
		showTimelineWindow = false;
		if(setupTimelineWindow)
		{
			timelineWindow.setVisible(false);
			timelineWindow.close();
			timelineWindow.dispose();
			setupTimelineWindow = false;
		}
	} 
	public void hideLibraryWindowX()
	{
		showLibraryWindow = false;
		if(setupLibraryWindow)
			libraryWindow.setVisible(false);
	} 
	public void closeLibraryWindow()
	{
		showLibraryWindow = false;
		if(setupLibraryWindow)
		{
			libraryWindow.setVisible(false);
			libraryWindow.close();
			libraryWindow.dispose();
			setupLibraryWindow = false;
		}
	} 
	public void hideCreateLibraryWindow()
	{
		showCreateLibraryWindow = false;
		if(setupCreateLibraryWindow)
			createLibraryWindow.setVisible(false);
	} 
	
	public void closeCreateLibraryWindow()
	{
		showCreateLibraryWindow = false;
		if(setupCreateLibraryWindow)
		{
			createLibraryWindow.setVisible(false);
			createLibraryWindow.close();
			createLibraryWindow.dispose();
			setupCreateLibraryWindow = false;
		}
	} 
	/**
	 * Hide all windows
	 */
	public void hideWindows()
	{
		if(showMLWindow)
			hideMLWindow();
		if(showNavigationWindow)
			hideNavigationWindow();
		if(showMediaWindow)
			hideMediaWindow();
		if(showStatisticsWindow)
			hideStatisticsWindow();
		if(showHelpWindow)
			hideHelpWindow();
		if(showMapWindow)
			hideMapWindow();
		if(showTimelineWindow)
			hideTimelineWindow();
	}
}
	
/**
 * Simple class that extends GWinData and holds the data 
 * that is specific to a particular window.
 * @author Peter Lager
 */
class ML_WinData extends GWinData {
	int sx, sy, ex, ey;
	boolean done;

	//		MyWinData(){}
}