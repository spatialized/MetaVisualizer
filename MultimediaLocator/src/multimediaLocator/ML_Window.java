package multimediaLocator;

import java.awt.Canvas;
import java.awt.Font;
import java.util.ArrayList;

import com.jogamp.newt.opengl.GLWindow;

import g4p_controls.*;
import processing.awt.PSurfaceAWT.SmoothCanvas;
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
	private final int delayAmount = 100;							// Delay length to avoid G4P library concurrent modification exception
	
	
	/* Windows */
	private final int windowWidth = 310;
	private final int shortWindowHeight = 340, mediumWindowHeight = 600, tallWindowHeight = 875;
	public boolean compressTallWindows = false;
	private int compressedNavigationWindowHeight = mediumWindowHeight, compressedMediaWindowHeight = 510;

	private final int closeWindowWaitTime = 180;			// Time to wait before closing hidden windows
	private int lastWindowHiddenFrame;						// Frame when last window was hidden

	public GWindow mainMenu, navigationWindow, mediaWindow, libraryViewWindow,  helpWindow, 
				   mapWindow, timeWindow;
	public GWindow startupWindow, createLibraryWindow, listItemWindow, textEntryWindow;

	public GLabel lblMainMenu, lblNavigationWindow, lblMedia, lblLibrary, lblHelp, lblMap, lblTimeline;
	public GLabel lblStartup, lblImport;	
	
	public boolean setupMainMenu, setupNavigationWindow = false, setupMediaWindow = false, setupHelpWindow = false, 
				   setupLibraryViewWindow = false, setupTimeWindow = false;
	public boolean setupCreateLibraryWindow = false, setupLibraryWindow = false, setupTextEntryWindow = false;
	
	public boolean showMainMenu = false, showNavigationWindow = false, showMediaWindow = false, showLibraryViewWindow = false, 
				   showHelpWindow = false, showTimeWindow = false;;
	public boolean showCreateLibraryWindow, showLibraryWindow = false;

	private int mainMenuX = -1, mainMenuY = -1;
	private int navigationWindowX = -1, navigationWindowY = -1;
	private int mediaWindowX = -1, mediaWindowY = -1;
	private int timeWindowX = -1, timeWindowY = -1;
	private int libraryViewWindowX = -1, libraryViewWindowY = -1;
	private int helpWindowX = -1, helpWindowY = -1;
	
	/* Library Window */
	private int startupWindowHeight;
	public GButton btnCreateLibrary, btnOpenLibrary, btnLibraryHelp;
	public GCheckbox chkbxRebuildLibrary;
	public GLabel lblStartupWindowText;
	
	/* Create Library Window */
	private int createLibraryWindowHeight;
	public GButton btnImportMediaFolder, btnMakeLibrary, btnCancelCreateLibrary;
	public GLabel lblCreateLibraryWindowText, lblCreateLibraryWindowText2;

	/* List Item Window */
	public boolean showListItemWindow = false;
	private int listItemWindowHeight;
	private ArrayList<String> listItemWindowList;
	private String listItemWindowText;
	public int listItemWindowSelectedItem = -1;
	public int listItemWindowResultCode = -1;		// 1: GPS Track  

	/* Text Entry Window */
	GTextField txfInputText;
	GButton btnEnterText;
	GLabel lblText;
	
	public boolean showTextEntryWindow = false;
	private int textEntryWindowHeight;
//	public int textEntryWindowSelectedItem = -1;
//	private String textEntryWindowText;				// Prompt text
//	private String textEntryWindowUserEntry;		// User entry
	public int textEntryWindowResultCode = -1;		// 1: GPS Track  
	
	/* Main Window */
	private GButton btnNavigationWindow, btnMediaWindow, btnLibraryViewWindow, btnMapWindow, btnTimeWindow, btnHelpWindow;
	private GButton btnChooseField, btnSaveLibrary, btnSaveField, btnRestart, btnQuit;
	private GLabel lblViewMode, lblWindows, lblCommands, lblSpaceBar;
	public GToggleGroup tgDisplayView;	
	public GOption optWorldView, optMapView, optTimelineView, optLibraryView;
	private int mainMenuHeight;
	public GCheckbox chkbxScreenMessagesOn;

	/* Navigation Window */
	public GCheckbox chkbxPathFollowing; 															/* Navigation Window Options */
	public GOption optMove, optTeleport;
	public GToggleGroup tgNavigationTeleport;	
//	public GCheckbox chkbxMovementTeleport, chkbxFollowTeleport;
	public GCheckbox chkbxFollowTeleport;
	
	public GSlider sdrTeleportLength, sdrPathWaitLength;											/* Navigation Window Sliders */

	private GButton btnZoomIn, btnZoomOut;															/* Navigation Window Buttons */
	private GButton btnSaveLocation, btnClearMemory, btnSetHome;
	private GButton btnJumpToRandomCluster;
	public GButton btnExportScreenshot, btnExportMedia, btnOutputFolder;
	private GButton btnNextTimeSegment, btnPreviousTimeSegment, btnStopViewer;
	private GButton btnMoveToNearestCluster, btnMoveToLastCluster;
	private GButton btnMoveToNearestImage, btnMoveToNearestPanorama;
	private GButton btnMoveToNearestVideo, btnMoveToNearestSound;
	public GButton btnGoToPreviousField, btnGoToNextField, btnChooseGPSTrack;

	public GButton btnMapView;		
	private GButton btnPanUp, btnPanLeft, btnPanDown, btnPanRight;		
	private GButton btnZoomToViewer;	// btnZoomToSelected;		
	private GButton btnZoomOutToField, btnMapZoomIn, btnMapZoomOut, btnZoomToWorld;		
	public GLabel lblZoomTo, lblMapZoom, lblPan;					
	public GOption optMapViewFieldMode, optMapViewWorldMode;
	public GToggleGroup tgMapViewMode;	
	int mapWindowHeight;

	private GLabel lblTimeNavigation, lblAutoNavigation, lblNearest, lblPathNavigation, 			/* Navigation Window Labels */
	lblTeleportLength, lblPathWaitLength, lblMemory;
//	public GLabel lblShift1;
	private GButton btnNavigationWindowExit, btnNavigationWindowClose;		

	private int navigationWindowLineBreakY_1, navigationWindowLineBreakY_2, navigationWindowLineBreakY_3;		
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
	public GLabel lblGraphics, lblGraphicsModes, lblOutput, lblZoom;
	public GOption optTimeline, optGPSTrack, optMemory; 
	public GToggleGroup tgFollow;	

	public GCheckbox chkbxSelectionMode, chkbxMultiSelection, chkbxSegmentSelection, chkbxShowMetadata; 	/* Media Window Options */
	public GCheckbox chkbxShowModel, chkbxMediaToCluster, chkbxCaptureToMedia, chkbxCaptureToCluster;
	public GCheckbox chkbxHideImages, chkbxHideVideos, chkbxHidePanoramas, chkbxHideSounds;
	public GCheckbox chkbxAlphaMode, chkbxAngleFading, chkbxAngleThinning;
	public GCheckbox chkbxOrientationMode, chkbxDomeView;
	public GCheckbox chkbxBlurMasks;

	public GSlider sdrAlpha, sdrBrightness, sdrFarClipping, sdrVisibleAngle, sdrAltitudeFactor; /* Media Window Sliders */
	
	private GButton btnSubjectDistanceUp, btnSubjectDistanceDown;		/* Media Window Buttons */
	public GButton btnSelectFront, btnDeselectFront;
	public GButton btnSelectPanorama, btnDeselectPanorama;
	public GButton btnDeselectAll, btnViewSelected, btnStitchPanorama;
	
	private GLabel lblAlpha, lblBrightness, lblFarClipping, lblVisibleAngle;		/* Media Window Labels */
	private GLabel lblSubjectDistance;
	
	private GLabel lblHideMedia;
	public GLabel lblSelection, lblSelectionOptions;					
	private GLabel lblAdvanced, lblAltitudeFactor;						
//	public GLabel lblShift2;
	private GButton btnMediaWindowExit, btnMediaWindowClose;		

	private int mediaWindowLineBreakY_1, mediaWindowLineBreakY_2;

	
	/* Time Window */
	public GButton btnTimeView;		
	public GButton btnTimelineReverse, btnTimelineForward;		
	public GButton btnTimelineZoomIn, btnTimelineZoomOut;		
	public GButton btnTimelineZoomToField, btnTimelineZoomToSelected, btnTimelineZoomToFull;		
	public GLabel lblTimelineZoomTo, lblTimelineScroll;					
//	public GLabel lblShift3;
	private GButton btnTimeWindowExit, btnTimeWindowClose;		
	int timeWindowHeight;

	/* Library View Window */
	public GOption optLibraryViewWorldMode, optLibraryViewFieldMode, optLibraryViewClusterMode;
	public GToggleGroup tgLibraryViewMode;	
	public GButton btnLibraryView, btnPreviousCluster, btnNextCluster, btnCurrentCluster;
	public GLabel lblViewerStats, lblWorldStats, lblLibraryViewMode, lblLibraryViewText;					
	int libraryViewWindowHeight;
	int libraryWindowViewerTextYOffset, libraryWindowWorldTextYOffset;
//	public GLabel lblShift4;
	private GButton btnLibraryViewWindowExit, btnLibraryViewWindowClose;		

	/* Help Window */
	private GButton btnAboutHelp, btnImportHelp, btnCloseHelp;
	public int helpAboutText = 0;		// Whether showing  0: About Text  1: Importing Files Help Text, or 2: Keyboard Shortcuts
	int helpWindowHeight;
	private GButton btnHelpWindowExit, btnHelpWindowClose;		

	/* Margins */
	private int iLeftMargin = 15;			
	private int iTopMargin = 10;
	private int iBottomTextY = 30;
	
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
	 * Constructor for window handler 
	 * @param parent Parent world
	 * @param newDisplay Parent display object
	 */
	public ML_Window( WMV_World parent, ML_Display newDisplay )
	{
		world = parent;
		display = newDisplay;
		utilities = new WMV_Utilities();
		
		mainMenuHeight = mediumWindowHeight - 130;
		startupWindowHeight = shortWindowHeight / 2;
		createLibraryWindowHeight = shortWindowHeight + 60;
		listItemWindowHeight = shortWindowHeight;			// -- Update this
		textEntryWindowHeight = 120;
		
		libraryViewWindowHeight = shortWindowHeight;
		helpWindowHeight = mediumWindowHeight + 100;
		mapWindowHeight = shortWindowHeight - 25;
		timeWindowHeight = mediumWindowHeight - 40;
		
		if(world.ml.displayHeight < tallWindowHeight + 10)					// Small screen: compress windows
		{
			navigationWindowHeight = compressedNavigationWindowHeight;	
			navigationWindowWidth = windowWidth * 2;
			mediaWindowHeight = compressedMediaWindowHeight;
			mediaWindowWidth = windowWidth * 2;
			compressTallWindows = true;
		}
		else																// Large screen
		{
			navigationWindowHeight = tallWindowHeight;		
			mediaWindowHeight = tallWindowHeight;
			mediaWindowWidth = windowWidth;
			navigationWindowWidth = windowWidth;
			compressTallWindows = false;
		}
	}

	/**
	 * Setup Main Menu window
	 */
	public void setupMainMenu()
	{
		int leftEdge = world.ml.displayWidth / 2 - windowWidth / 2;
		int topEdge = world.ml.displayHeight / 2 - mainMenuHeight / 2;
		
		if(mainMenuX > -1 && mainMenuY > -1)
		{
			leftEdge = mainMenuX;
			topEdge = mainMenuY;
		}

		mainMenu = GWindow.getWindow(world.ml, "Main Menu", leftEdge, topEdge, windowWidth, mainMenuHeight, PApplet.JAVA2D);
		mainMenu.addData(new ML_WinData());
		mainMenu.addDrawHandler(this, "mlWindowDraw");
		mainMenu.addMouseHandler(this, "mlWindowMouse");
		mainMenu.addKeyHandler(world.ml, "mlWindowKey");
		mainMenu.setActionOnClose(GWindow.KEEP_OPEN);
		
		world.ml.delay(delayAmount);

		int x = 0, y = iTopMargin;
		lblMainMenu = new GLabel(mainMenu, x, y, mainMenu.width, iMediumBoxHeight, "Main Menu");
		lblMainMenu.setLocalColorScheme(G4P.SCHEME_10);
		lblMainMenu.setFont(new Font("Monospaced", Font.PLAIN, iVeryLargeTextSize));
		lblMainMenu.setTextAlign(GAlign.CENTER, null);
		lblMainMenu.setTextBold();


		x = 85;
		y += iLargeBoxHeight;
		optWorldView = new GOption(mainMenu, x, y, 95, iVerySmallBoxHeight, "World (1)");
		optWorldView.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		optWorldView.setLocalColorScheme(G4P.SCHEME_10);
		optWorldView.tag = "SceneView";
		optMapView = new GOption(mainMenu, x += 100, y, 80, iVerySmallBoxHeight, "Map (2)");
		optMapView.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		optMapView.setLocalColorScheme(G4P.SCHEME_10);
		optMapView.tag = "MapView";
		x = iLeftMargin;
		y += iSmallBoxHeight * 0.5f;
		lblViewMode = new GLabel(mainMenu, x, y, 65, iMediumBoxHeight);						/* Display Mode Label */
		lblViewMode.setText("View:");
		lblViewMode.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
		lblViewMode.setLocalColorScheme(G4P.SCHEME_10);
		x = 85;
		y += iSmallBoxHeight * 0.5f;
		optTimelineView = new GOption(mainMenu, x, y, 90, iVerySmallBoxHeight, "Time (3)");
		optTimelineView.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		optTimelineView.setLocalColorScheme(G4P.SCHEME_10);
		optTimelineView.tag = "TimelineView";
		optLibraryView = new GOption(mainMenu, x += 100, y, 115, iVerySmallBoxHeight, "Library (4)");
		optLibraryView.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		optLibraryView.setLocalColorScheme(G4P.SCHEME_10);
		optLibraryView.tag = "LibraryView";

		world.ml.delay(delayAmount);

		switch(display.getDisplayView())
		{
			case 0:
				optWorldView.setSelected(true);
				optMapView.setSelected(false);
				optTimelineView.setSelected(false);
				optLibraryView.setSelected(false);
				break;
			case 1:
				optWorldView.setSelected(false);
				optMapView.setSelected(true);
				optTimelineView.setSelected(false);
				optLibraryView.setSelected(false);
				break;
			case 2:
				optWorldView.setSelected(false);
				optMapView.setSelected(false);
				optTimelineView.setSelected(true);
				optLibraryView.setSelected(false);
				break;
			case 3:
				optWorldView.setSelected(false);
				optMapView.setSelected(false);
				optTimelineView.setSelected(false);
				optLibraryView.setSelected(true);
				break;
		}
		
		tgDisplayView = new GToggleGroup();
		tgDisplayView.addControls(optWorldView, optMapView, optTimelineView, optLibraryView);
	
		x = 70;
		y += iLargeBoxHeight;
		chkbxScreenMessagesOn = new GCheckbox(mainMenu, x, y, 220, iVerySmallBoxHeight, "Screen Messages (H)");
		chkbxScreenMessagesOn.tag = "ScreenMessagesOn";
		chkbxScreenMessagesOn.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize-1));
		chkbxScreenMessagesOn.setLocalColorScheme(G4P.SCHEME_10);
		chkbxScreenMessagesOn.setSelected(world.getSettings().screenMessagesOn);

		x = 0;
		y += iMediumBoxHeight;
		lblWindows = new GLabel(mainMenu, x, y, mainMenu.width, iSmallBoxHeight);						/* Display Mode Label */
		lblWindows.setText("Windows");
		lblWindows.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblWindows.setLocalColorScheme(G4P.SCHEME_10);
		lblWindows.setTextAlign(GAlign.CENTER, null);

		world.ml.delay(delayAmount / 2);

		x = 95;
		y += iMediumBoxHeight;
		btnNavigationWindow = new GButton(mainMenu, x, y, 120, iVerySmallBoxHeight, "Navigation  ⇧1");
		btnNavigationWindow.tag = "OpenNavigationWindow";
		btnNavigationWindow.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		y += iSmallBoxHeight;
		btnMediaWindow = new GButton(mainMenu, x, y, 120, iVerySmallBoxHeight, "Media  ⇧2");
		btnMediaWindow.tag = "OpenMediaWindow";
		btnMediaWindow.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		y += iSmallBoxHeight;
		btnTimeWindow = new GButton(mainMenu, x, y, 120, iVerySmallBoxHeight, "Time  ⇧3");
		btnTimeWindow.tag = "OpenTimeWindow";
		btnTimeWindow.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		y += iSmallBoxHeight;
		btnLibraryViewWindow = new GButton(mainMenu, x, y, 120, iVerySmallBoxHeight, "Library  ⇧4");
		btnLibraryViewWindow.tag = "OpenLibraryViewWindow";
		btnLibraryViewWindow.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 0;
		y += iMediumBoxHeight;
		lblCommands = new GLabel(mainMenu, x, y, mainMenu.width, iSmallBoxHeight);						/* Display Mode Label */
		lblCommands.setText("Commands");
		lblCommands.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblCommands.setLocalColorScheme(G4P.SCHEME_10);
		lblCommands.setTextAlign(GAlign.CENTER, null);

		x = 85;
		y += iMediumBoxHeight;
		btnSaveLibrary = new GButton(mainMenu, x, y, 140, iVerySmallBoxHeight, "Save Library  ⇧S");
		btnSaveLibrary.tag = "SaveWorld";
		btnSaveLibrary.setLocalColorScheme(G4P.CYAN_SCHEME);

		y += iSmallBoxHeight;
		btnSaveField = new GButton(mainMenu, x, y, 140, iVerySmallBoxHeight, "Save Field  /");
		btnSaveField.tag = "SaveField";
		btnSaveField.setLocalColorScheme(G4P.CYAN_SCHEME);

		world.ml.delay(delayAmount);

		y += iSmallBoxHeight;
		btnRestart = new GButton(mainMenu, x, y, 140, iVerySmallBoxHeight, "Close Library  ⇧R");
		btnRestart.tag = "CloseLibrary";
		btnRestart.setLocalColorScheme(G4P.ORANGE_SCHEME);

		x = 105;
		y += iSmallBoxHeight;
		btnQuit = new GButton(mainMenu, x, y, 100, iVerySmallBoxHeight, "Quit  ⇧Q");
		btnQuit.tag = "Quit";
		btnQuit.setLocalColorScheme(G4P.RED_SCHEME);

		y = mainMenuHeight - iBottomTextY * 2;
		btnHelpWindow = new GButton(mainMenu, windowWidth - 30 - iLeftMargin, y, 30, 30, "?");
		btnHelpWindow.tag = "OpenHelpWindow";
		btnHelpWindow.setFont(new Font("Monospaced", Font.BOLD, iLargeTextSize));
		btnHelpWindow.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		x = 0;
		y = mainMenuHeight - iBottomTextY;
		lblSpaceBar = new GLabel(mainMenu, x, y, mainMenu.width, iVerySmallBoxHeight);						/* Display Mode Label */
		lblSpaceBar.setText("Press SPACEBAR to show / hide");
		lblSpaceBar.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		lblSpaceBar.setLocalColorScheme(G4P.SCHEME_10);
		lblSpaceBar.setTextAlign(GAlign.CENTER, null);
		
		setupMainMenu = true;
		world.ml.setAppIcon = true;
	}

	/**
	 * Setup the Navigation Window
	 */
	public void setupNavigationWindow()
	{
		if(compressTallWindows)					// Compressed window
		{
			if(world.getFields().size() > 1)
				navigationWindowHeight = compressedNavigationWindowHeight + 55;			// Multiple fields, more buttons
			else
				navigationWindowHeight = compressedNavigationWindowHeight;
		}
		else									// Tall window
		{
			if(world.getFields().size() == 1) 
				navigationWindowHeight = tallWindowHeight - 45;							// Single field, fewer buttons
			else
				navigationWindowHeight = tallWindowHeight;
		}
		
		int leftEdge = world.ml.displayWidth / 2 - windowWidth / 2;
		int topEdge = world.ml.displayHeight / 2 - navigationWindowHeight / 2;
		
		if(navigationWindowX > -1 && navigationWindowY > -1)
		{
			leftEdge = navigationWindowX;
			topEdge = navigationWindowY;
		}

		navigationWindow = GWindow.getWindow(world.ml, "Navigation", leftEdge, topEdge, navigationWindowWidth, navigationWindowHeight, PApplet.JAVA2D);
		navigationWindow.setVisible(true);

		navigationWindow.addData(new ML_WinData());
		navigationWindow.addDrawHandler(this, "navigationWindowDraw");
		navigationWindow.addMouseHandler(this, "navigationWindowMouse");
		navigationWindow.addKeyHandler(world.ml, "navigationWindowKey");
		navigationWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		world.ml.delay(delayAmount);
		world.ml.delay(delayAmount);

		int x = 0, y = iTopMargin;
		
		if(compressTallWindows) x = 95;
		else x = 0;

		lblNavigationWindow = new GLabel(navigationWindow, x, y, navigationWindow.width, iMediumBoxHeight, "Navigation");
		lblNavigationWindow.setLocalColorScheme(G4P.SCHEME_10);
		lblNavigationWindow.setFont(new Font("Monospaced", Font.PLAIN, iVeryLargeTextSize));
		if(!compressTallWindows) lblNavigationWindow.setTextAlign(GAlign.CENTER, null);
		lblNavigationWindow.setTextBold();

		y += iLargeBoxHeight;
		x = 55;
		optMove = new GOption(navigationWindow, x, y, 80, iVerySmallBoxHeight, "Move");
		optMove.setLocalColorScheme(G4P.SCHEME_10);
		optMove.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		optMove.tag = "NavigationMove";
		optTeleport = new GOption(navigationWindow, x+=75, y, 150, iVerySmallBoxHeight, "Teleport  (t)");
		optTeleport.setLocalColorScheme(G4P.SCHEME_10);
		optTeleport.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		optTeleport.tag = "NavigationTeleport";
		
		if(world.viewer.getNavigationTeleport())
		{
			optMove.setSelected(false);
			optTeleport.setSelected(true);
		}
		else
		{
			optMove.setSelected(true);
			optTeleport.setSelected(false);
		}

		tgNavigationTeleport = new GToggleGroup();
		tgNavigationTeleport.addControls(optMove, optTeleport);

		world.ml.delay(delayAmount);
		
		x = iLeftMargin;
		y += iLargeBoxHeight;
		lblTimeNavigation = new GLabel(navigationWindow, x, y, navigationWindow.width, iVerySmallBoxHeight, "Time:");
		lblTimeNavigation.setLocalColorScheme(G4P.SCHEME_10);
		lblTimeNavigation.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));

		x = 105;
		btnPreviousTimeSegment = new GButton(navigationWindow, x, y, 95, iVerySmallBoxHeight, "Previous (b)");
		btnPreviousTimeSegment.tag = "PreviousTime";
		btnPreviousTimeSegment.setLocalColorScheme(G4P.CYAN_SCHEME);

		btnNextTimeSegment = new GButton(navigationWindow, x+=105, y, 85, iVerySmallBoxHeight, "Next (n)");
		btnNextTimeSegment.tag = "NextTime";
		btnNextTimeSegment.setLocalColorScheme(G4P.CYAN_SCHEME);
	
		x = iLeftMargin;
		y += iLargeBoxHeight;
		lblNearest = new GLabel(navigationWindow, x, y, 85, iVerySmallBoxHeight, "Nearest:");
		lblNearest.setLocalColorScheme(G4P.SCHEME_10);
		lblNearest.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));

		x = 105;
		btnMoveToNearestCluster = new GButton(navigationWindow, x, y, 80, iVerySmallBoxHeight, "Media (m)");
		btnMoveToNearestCluster.tag = "NearestCluster";
		btnMoveToNearestCluster.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		y += iMediumBoxHeight;
		btnMoveToNearestImage = new GButton(navigationWindow, x, y, 80, iVerySmallBoxHeight, "Image (i)");
		btnMoveToNearestImage.tag = "NearestImage";
		btnMoveToNearestImage.setLocalColorScheme(G4P.CYAN_SCHEME);

		btnMoveToNearestPanorama = new GButton(navigationWindow, x+=90, y, 100, iVerySmallBoxHeight, "Panorama (p)");
		btnMoveToNearestPanorama.tag = "NearestPanorama";
		btnMoveToNearestPanorama.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 105;
		y += iMediumBoxHeight;
		btnMoveToNearestVideo = new GButton(navigationWindow, x, y, 80, iVerySmallBoxHeight, "Video (v)");
		btnMoveToNearestVideo.tag = "NearestVideo";
		btnMoveToNearestVideo.setLocalColorScheme(G4P.CYAN_SCHEME);

		btnMoveToNearestSound = new GButton(navigationWindow, x+90, y, 100, iVerySmallBoxHeight, "Sound (u)");
		btnMoveToNearestSound.tag = "NearestSound";
		btnMoveToNearestSound.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 70;
		y += iLargeBoxHeight;
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

		x = 195;
		btnZoomIn = new GButton(navigationWindow, x, y, 50, iVerySmallBoxHeight, "In (q)");
		btnZoomIn.tag = "ZoomIn";
		btnZoomIn.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnZoomIn.fireAllEvents(true);

		world.ml.delay(delayAmount);

		x = iLeftMargin;
		y += iMediumBoxHeight;
		btnMoveToLastCluster = new GButton(navigationWindow, x, y, 70, iVerySmallBoxHeight, "Last (l)");
		btnMoveToLastCluster.tag = "LastCluster";
		btnMoveToLastCluster.setLocalColorScheme(G4P.CYAN_SCHEME);

		btnJumpToRandomCluster = new GButton(navigationWindow, x+=90, y, 90, iVerySmallBoxHeight, "Random (j)");
		btnJumpToRandomCluster.tag = "RandomCluster";
		btnJumpToRandomCluster.setLocalColorScheme(G4P.CYAN_SCHEME);

		world.ml.delay(delayAmount / 2);
		
		x += 110;
		btnStopViewer = new GButton(navigationWindow, x, y, 80, iVerySmallBoxHeight, "Stop (.)");
		btnStopViewer.tag = "StopViewer";
		btnStopViewer.setLocalColorScheme(G4P.RED_SCHEME);
		
		if(world.getFields() != null)
		{
			if(world.getFieldCount() > 1)
			{
				x = 85;
				y += iMediumBoxHeight;
				btnChooseField = new GButton(navigationWindow, x, y, 130, iVerySmallBoxHeight, "Choose Field  ⇧C");
				btnChooseField.tag = "ChooseField";
				btnChooseField.setLocalColorScheme(G4P.ORANGE_SCHEME);

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

		world.ml.delay(delayAmount / 2);
		
//		if(compressTallWindows)
//			x = 90;
//		else
//			x = 0;
	
		if(compressTallWindows)
			x = 135;
		else 
			x = 0;
		y += 40;
		navigationWindowLineBreakY_1 = y - 10;
		
		lblMap = new GLabel(navigationWindow, x, y, navigationWindow.width, iSmallBoxHeight, "Map");
		lblMap.setLocalColorScheme(G4P.SCHEME_10);
		if(!compressTallWindows) lblMap.setTextAlign(GAlign.CENTER, null);
		lblMap.setFont(new Font("Monospaced", Font.PLAIN, iVeryLargeTextSize));
		lblMap.setTextBold();

		x = 75;
//		if(compressTallWindows) x += windowWidth;
		y += iVeryLargeBoxHeight;
		btnMapView = new GButton(navigationWindow, x, y, 160, iSmallBoxHeight, "Open Map View (2)");
		btnMapView.tag = "SetMapView";
		btnMapView.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		btnMapView.setLocalColorScheme(G4P.GOLD_SCHEME);
		if(world.ml.display.getDisplayView() == 1) btnMapView.setEnabled(false);

		x = 40;
//		if(compressTallWindows) x += windowWidth;
		y += iVeryLargeBoxHeight;
		optMapViewFieldMode = new GOption(navigationWindow, x, y, 115, iVerySmallBoxHeight, "Field (F)");
		optMapViewFieldMode.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		optMapViewFieldMode.setLocalColorScheme(G4P.SCHEME_10);
		optMapViewFieldMode.tag = "SetMapViewFieldMode";
		optMapViewWorldMode = new GOption(navigationWindow, x += 125, y, 115, iVerySmallBoxHeight, "World (L)");
		optMapViewWorldMode.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
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

		x = iLeftMargin;
//		if(compressTallWindows) x += windowWidth;
		y += iLargeBoxHeight;
		lblZoomTo = new GLabel(navigationWindow, x, y, 85, iSmallBoxHeight, "Zoom To:");
		lblZoomTo.setLocalColorScheme(G4P.SCHEME_10);
		lblZoomTo.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
//		lblZoomTo.setTextBold();

		x = 110;
		y += 3;
//		if(compressTallWindows) x += windowWidth;
		btnZoomOutToField = new GButton(navigationWindow, x, y, 80, iVerySmallBoxHeight, "Field (Z)");
		btnZoomOutToField.tag = "ZoomToField";
		btnZoomOutToField.setLocalColorScheme(G4P.CYAN_SCHEME);

		x += 90;
//		if(compressTallWindows) x += windowWidth;
		btnZoomToViewer = new GButton(navigationWindow, x, y, 85, iVerySmallBoxHeight, "Viewer (z)");
		btnZoomToViewer.tag = "ZoomToViewer";					// -- Zooms to current cluster
		btnZoomToViewer.setLocalColorScheme(G4P.CYAN_SCHEME);
		
//		x = 110;
//		y += iMediumBoxHeight * 0.5f;
//		btnZoomToSelected = new GButton(navigationWindow, x, y, 80, iVerySmallBoxHeight, "Selected");
//		btnZoomToSelected.tag = "ZoomToSelected";
//		btnZoomToSelected.setLocalColorScheme(G4P.CYAN_SCHEME);
//		x += 95;
		
//		x = iLeftMargin;
//		if(compressTallWindows) x += windowWidth;
//		y += iLargeBoxHeight;
//		lblZoomTo = new GLabel(navigationWindow, x, y, 85, iSmallBoxHeight, "Zoom To:");
//		lblZoomTo.setLocalColorScheme(G4P.SCHEME_10);
//		lblZoomTo.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));

		
		x = iLeftMargin;
//		if(compressTallWindows) x += windowWidth;
		y += iMediumBoxHeight;
		lblMapZoom = new GLabel(navigationWindow, x, y, 55, iSmallBoxHeight, "Zoom:");
		lblMapZoom.setLocalColorScheme(G4P.SCHEME_10);
		lblMapZoom.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));

		y += 3;
		btnMapZoomOut = new GButton(navigationWindow, x+=65, y, 55, iVerySmallBoxHeight, "In (w)");
		btnMapZoomOut.tag = "MapZoomIn";
		btnMapZoomOut.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnZoomToWorld = new GButton(navigationWindow, x+=65, y, 75, iVerySmallBoxHeight, "Reset (r)");
		btnZoomToWorld.tag = "ResetMapZoom";
		btnZoomToWorld.setLocalColorScheme(G4P.RED_SCHEME);
		btnMapZoomIn = new GButton(navigationWindow, x+=85, y, 65, iVerySmallBoxHeight, "Out (d)");
		btnMapZoomIn.tag = "MapZoomOut";
		btnMapZoomIn.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		x = 125;
//		if(compressTallWindows) x += windowWidth;
		y += iMediumBoxHeight;
		btnPanUp = new GButton(navigationWindow, x, y, 60, iVerySmallBoxHeight, "Up");
		btnPanUp.tag = "PanUp";
		btnPanUp.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnPanUp.fireAllEvents(true);

		x = 55;
//		if(compressTallWindows) x += windowWidth;
		y += iSmallBoxHeight;
		btnPanLeft = new GButton(navigationWindow, x, y, 60, iVerySmallBoxHeight, "Left");
		btnPanLeft.tag = "PanLeft";
		btnPanLeft.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnPanLeft.fireAllEvents(true);

		if(compressTallWindows) x = 135;
		else x = 0;
		lblPan = new GLabel(navigationWindow, x, y-3, navigationWindow.width, iSmallBoxHeight, "Pan");
		lblPan.setLocalColorScheme(G4P.SCHEME_10);
		lblPan.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		if(!compressTallWindows) lblPan.setTextAlign(GAlign.CENTER, null);
		lblPan.setTextBold();
		
		x = 190;
//		if(compressTallWindows) x += windowWidth;
		btnPanRight = new GButton(navigationWindow, x, y, 70, iVerySmallBoxHeight, "Right");
		btnPanRight.tag = "PanRight";
		btnPanRight.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnPanRight.fireAllEvents(true);

		x = 123;
//		if(compressTallWindows) x += windowWidth;
		y += iSmallBoxHeight;
		btnPanDown = new GButton(navigationWindow, x, y, 65, iVerySmallBoxHeight, "Down");
		btnPanDown.tag = "PanDown";
		btnPanDown.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnPanDown.fireAllEvents(true);

		world.ml.delay(delayAmount / 2);

		x = 88;
		y += iVeryLargeBoxHeight;
		navigationWindowLineBreakY_2 = y - 10;
		
		btnSetHome = new GButton(navigationWindow, x, y, 145, iVerySmallBoxHeight, "Save Start Location (h)");
		btnSetHome.tag = "SetHome";
		btnSetHome.setLocalColorScheme(G4P.CYAN_SCHEME);

		if(compressTallWindows)
		{
			x = windowWidth + 75;
			y = iTopMargin;
		}
		else
		{
			y += 45;
			x = 0;
		}
		navigationWindowLineBreakY_3 = y - 10;

		/* Path Navigation */
		lblPathNavigation = new GLabel(navigationWindow, x, y, navigationWindow.width, iVerySmallBoxHeight, "Path Navigation");
		lblPathNavigation.setLocalColorScheme(G4P.SCHEME_10);
		lblPathNavigation.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
		if(!compressTallWindows) lblPathNavigation.setTextAlign(GAlign.CENTER, null);
		lblPathNavigation.setTextBold();

		x = 40;
		if(compressTallWindows) x += windowWidth;
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
		
		switch(world.viewer.getPathNavigationMode())			// 0: Timeline 1: GPS Track 2: Memory
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

		x = 165;
		if(compressTallWindows) x += windowWidth;
		y += iMediumBoxHeight - 3;
		chkbxPathFollowing = new GCheckbox(navigationWindow, x, y, 150, iVerySmallBoxHeight, "On / Off  (>)");
		chkbxPathFollowing.tag = "Following";
		chkbxPathFollowing.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		chkbxPathFollowing.setLocalColorScheme(G4P.SCHEME_10);
		chkbxPathFollowing.setSelected(world.viewer.isFollowing());
		if(world.viewer.getPathNavigationMode() == 1)
			chkbxPathFollowing.setEnabled(world.viewer.getSelectedGPSTrackID() != -1);

		x = 25;
		if(compressTallWindows) x += windowWidth;
		y += iMediumBoxHeight * 0.5f;
		btnChooseGPSTrack = new GButton(navigationWindow, x, y, 120, iVerySmallBoxHeight, "Select GPS Track");
		btnChooseGPSTrack.tag = "ChooseGPSTrack";
		btnChooseGPSTrack.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		x = 165;
		if(compressTallWindows) x += windowWidth;
		y += iMediumBoxHeight * 0.4f;
		chkbxFollowTeleport = new GCheckbox(navigationWindow, x, y, 135, iVerySmallBoxHeight, "Teleport  (,)");
		chkbxFollowTeleport.tag = "FollowTeleport";
		chkbxFollowTeleport.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		chkbxFollowTeleport.setLocalColorScheme(G4P.SCHEME_10);
		chkbxFollowTeleport.setSelected(world.viewer.getState().followTeleport);
	
		boolean noGPSTracks = world.getCurrentField().getGPSTracks() == null;
		if(!noGPSTracks) noGPSTracks = world.getCurrentField().getGPSTracks().size() == 0;
		if(noGPSTracks) 
		{
			btnChooseGPSTrack.setEnabled(false);
			btnChooseGPSTrack.setVisible(false);
			if(compressTallWindows) 
			{
				chkbxPathFollowing.moveTo(100 + windowWidth, chkbxPathFollowing.getY());
				chkbxFollowTeleport.moveTo(100 + windowWidth, chkbxFollowTeleport.getY());
			}
			else
			{
				chkbxPathFollowing.moveTo(100, chkbxPathFollowing.getY());
				chkbxFollowTeleport.moveTo(100, chkbxFollowTeleport.getY());
			}
		}
		
		x = 25;
		if(compressTallWindows) x += windowWidth;
		y += iLargeBoxHeight;
		lblMemory = new GLabel(navigationWindow, x, y, 70, iVerySmallBoxHeight, "Memory");
		lblMemory.setLocalColorScheme(G4P.SCHEME_10);
		lblMemory.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		
		x = 105;
		if(compressTallWindows) x += windowWidth;
		btnSaveLocation = new GButton(navigationWindow, x, y, 60, iVerySmallBoxHeight, "Save (`)");
		btnSaveLocation.tag = "SaveLocation";
		btnSaveLocation.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		x = 190;
		if(compressTallWindows) x += windowWidth;
		btnClearMemory = new GButton(navigationWindow, x, y, 70, iVerySmallBoxHeight, "Clear (y)");
		btnClearMemory.tag = "ClearMemory";
		btnClearMemory.setLocalColorScheme(G4P.ORANGE_SCHEME);

//		x = 112;
//		if(compressTallWindows) x += windowWidth;
//		y += iMediumBoxHeight;
//		btnSetHome = new GButton(navigationWindow, x, y, 140, iVerySmallBoxHeight, "Set Start Location (h)");
//		btnSetHome.tag = "SetHome";
//		btnSetHome.setLocalColorScheme(G4P.ORANGE_SCHEME);
		
		x = 160;
		if(compressTallWindows) x += windowWidth;
		y += iMediumBoxHeight;
		sdrTeleportLength = new GSlider(navigationWindow, x, y, 65, 80, iVerySmallBoxHeight);
		sdrTeleportLength.setLocalColorScheme(G4P.GOLD_SCHEME);
		sdrTeleportLength.setLimits(0.f, 300.f, 10.f);
		sdrTeleportLength.setValue(world.viewer.getSettings().teleportLength);
		sdrTeleportLength.setRotation(PApplet.PI/2.f);
		sdrTeleportLength.setTextOrientation(G4P.ORIENT_LEFT);
		sdrTeleportLength.setEasing(0);
		sdrTeleportLength.setShowValue(true);
		sdrTeleportLength.tag = "TeleportLength";

		x = 290;
		if(compressTallWindows) x += windowWidth;
		sdrPathWaitLength = new GSlider(navigationWindow, x, y, 65, 80, iVerySmallBoxHeight);
		sdrPathWaitLength.setLocalColorScheme(G4P.GOLD_SCHEME);
		sdrPathWaitLength.setLimits(0.f, 600.f, 30.f);
		sdrPathWaitLength.setValue(world.viewer.getSettings().pathWaitLength);
		sdrPathWaitLength.setRotation(PApplet.PI/2.f);
		sdrPathWaitLength.setTextOrientation(G4P.ORIENT_LEFT);
		sdrPathWaitLength.setEasing(0);
		sdrPathWaitLength.setShowValue(true);
		sdrPathWaitLength.tag = "PathWaitLength";
		
		x = 25;
		if(compressTallWindows) x += windowWidth;
		y += iSmallBoxHeight - 2;
		lblTeleportLength = new GLabel(navigationWindow, x, y, 100, iVerySmallBoxHeight, "Teleport Time");
		lblTeleportLength.setLocalColorScheme(G4P.SCHEME_10);

		x = 175;
		if(compressTallWindows) x += windowWidth;
		lblPathWaitLength = new GLabel(navigationWindow, x, y, 100, iVerySmallBoxHeight, "Wait Time");
		lblPathWaitLength.setLocalColorScheme(G4P.SCHEME_10);
		
		setMapControlsEnabled(display.getDisplayView() == 1);
		
		if(compressTallWindows)
			x = navigationWindowWidth / 2 - 60;
		else
			x = iLeftMargin;
		y = navigationWindowHeight - iBottomTextY;
		btnNavigationWindowClose = new GButton(navigationWindow, x, y, 120, iVerySmallBoxHeight, "Close Window (!)");
		btnNavigationWindowClose.tag = "CloseNavigationWindow";
		btnNavigationWindowClose.setLocalColorScheme(G4P.RED_SCHEME);
		
		x = navigationWindowWidth - 150;
		btnNavigationWindowExit = new GButton(navigationWindow, x, y, 130, iVerySmallBoxHeight, "Main Menu (space)");
		btnNavigationWindowExit.tag = "ExitNavigationWindow";
		btnNavigationWindowExit.setLocalColorScheme(G4P.CYAN_SCHEME);
//		btnNavigationWindowExit.setTextBold();

//		lblShift1 = new GLabel(navigationWindow, x, y, navigationWindow.width, iVerySmallBoxHeight);		/* Window Label */
//		lblShift1.setText("Press SHIFT + 1 to show / hide");
//		lblShift1.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
//		lblShift1.setLocalColorScheme(G4P.SCHEME_10);
//		lblShift1.setTextAlign(GAlign.CENTER, null);

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

		if(mediaWindowX > -1 && mediaWindowY > -1)
		{
			leftEdge = mediaWindowX;
			topEdge = mediaWindowY;
		}

		mediaWindow = GWindow.getWindow(world.ml, "Media", leftEdge, topEdge, mediaWindowWidth, mediaWindowHeight, PApplet.JAVA2D);
		mediaWindow.setVisible(true);

		mediaWindow.addData(new ML_WinData());
		mediaWindow.addDrawHandler(this, "mediaWindowDraw");
		mediaWindow.addMouseHandler(this, "mediaWindowMouse");
		mediaWindow.addKeyHandler(world.ml, "mediaWindowKey");
		mediaWindow.setActionOnClose(GWindow.KEEP_OPEN);
	
		int x = 0, y = iTopMargin;
		world.ml.delay(delayAmount);
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
		lblGraphics = new GLabel(mediaWindow, x, y, mediaWindow.width, iVerySmallBoxHeight, "Graphics");
		lblGraphics.setLocalColorScheme(G4P.SCHEME_10);
		lblGraphics.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		if(!compressTallWindows) lblGraphics.setTextAlign(GAlign.CENTER, null);
		lblGraphics.setTextBold();

		x = 50;
		y += iMediumBoxHeight;
		chkbxAngleFading = new GCheckbox(mediaWindow, x, y, 220, iVerySmallBoxHeight, "Optimize Visibility (G)");
		chkbxAngleFading.tag = "AngleFading";
		chkbxAngleFading.setLocalColorScheme(G4P.SCHEME_10);
		chkbxAngleFading.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		chkbxAngleFading.setSelected(world.viewer.getSettings().angleFading);

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

		x = 25;
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

		x = 25;
		y += 30;
		lblBrightness = new GLabel(mediaWindow, x, y, 90, iVerySmallBoxHeight, "Brightness");
		lblBrightness.setLocalColorScheme(G4P.SCHEME_10);

		world.ml.delay(delayAmount / 2);

		x = 120;
		y += 10;
		world.ml.delay(delayAmount);
		sdrAltitudeFactor = new GSlider(mediaWindow, x, y, 160, 80, 20);
		sdrAltitudeFactor.setLocalColorScheme(G4P.GOLD_SCHEME);
		sdrAltitudeFactor.setLimits(0.f, 0.f, 1.f);
		sdrAltitudeFactor.setValue(world.settings.altitudeScalingFactor);					// -- Shouldn't be needed! Calls handler
		sdrAltitudeFactor.setTextOrientation(G4P.ORIENT_TRACK);
		sdrAltitudeFactor.setEasing(0);
		sdrAltitudeFactor.setShowValue(true);
		sdrAltitudeFactor.tag = "AltitudeFactor";
		
		x = 25;
		y += 30;
		lblAltitudeFactor = new GLabel(mediaWindow, x, y, 100, iVerySmallBoxHeight, "Altitude Factor");
		lblAltitudeFactor.setLocalColorScheme(G4P.SCHEME_10);

		x = 120;
		y += 10;
		sdrFarClipping = new GSlider(mediaWindow, x, y, 160, 80, 20);
		sdrFarClipping.setLocalColorScheme(G4P.GOLD_SCHEME);
		sdrFarClipping.setLimits(world.viewer.getSettings().getFarViewingDistance(), 2.f, 50.f);
		sdrFarClipping.setTextOrientation(G4P.ORIENT_TRACK);
		sdrFarClipping.setEasing(0);
		sdrFarClipping.setShowValue(true);
		sdrFarClipping.tag = "FarClipping";
		
		x = 25;
		y += 30;
		lblFarClipping = new GLabel(mediaWindow, x, y, 125, iVerySmallBoxHeight, "Visible Distance");
		lblFarClipping.setLocalColorScheme(G4P.SCHEME_10);

		x = 120;
		y += 10;
		sdrVisibleAngle = new GSlider(mediaWindow, x, y, 160, 80, 20);
		sdrVisibleAngle.setLocalColorScheme(G4P.GOLD_SCHEME);
		sdrVisibleAngle.setLimits(world.viewer.getVisibleAngle(), 0.1f, (float)Math.PI * 0.5f);
		sdrVisibleAngle.setTextOrientation(G4P.ORIENT_TRACK);
		sdrVisibleAngle.setEasing(0);
		sdrVisibleAngle.setShowValue(true);
		sdrVisibleAngle.tag = "VisibleAngle";
		
		x = 25;
		y += 30;
		lblVisibleAngle = new GLabel(mediaWindow, x, y, 120, iVerySmallBoxHeight, "Visible Angle");
		lblVisibleAngle.setLocalColorScheme(G4P.SCHEME_10);

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

		world.ml.delay(delayAmount);

		x = 215;
		btnSubjectDistanceUp = new GButton(mediaWindow, x, y, 30, iVerySmallBoxHeight, "+");
		btnSubjectDistanceUp.tag = "SubjectDistanceUp";
		btnSubjectDistanceUp.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 25;
		y += iSmallBoxHeight;
		chkbxBlurMasks = new GCheckbox(mediaWindow, x, y, 115, iVerySmallBoxHeight, "Fade Edges (E)");
		chkbxBlurMasks.tag = "FadeEdges";
		chkbxBlurMasks.setLocalColorScheme(G4P.SCHEME_10);
		chkbxBlurMasks.setSelected(world.getState().useBlurMasks);
		chkbxAlphaMode = new GCheckbox(mediaWindow, x+125, y, 115, iVerySmallBoxHeight, "Alpha On/Off (;)");
		chkbxAlphaMode.tag = "AlphaMode";
		chkbxAlphaMode.setLocalColorScheme(G4P.SCHEME_10);
		chkbxAlphaMode.setSelected(world.getState().alphaMode);

//		chkbxAngleThinning = new GCheckbox(mediaWindow, x += 105, y, 100, iVerySmallBoxHeight, "Angle Thinning");
//		chkbxAngleThinning.tag = "AngleThinning";
//		chkbxAngleThinning.setLocalColorScheme(G4P.SCHEME_10);
//		chkbxAngleThinning.setSelected(world.viewer.getSettings().angleThinning);

		world.ml.delay(delayAmount);

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
		btnOutputFolder.setLocalColorScheme(G4P.ORANGE_SCHEME);

		x += 125;
		btnExportScreenshot = new GButton(mediaWindow, x, y, 135, iVerySmallBoxHeight, "Save Screenshot (\\)");
		btnExportScreenshot.tag = "SaveScreenshot";
		btnExportScreenshot.setLocalColorScheme(G4P.CYAN_SCHEME);

		world.ml.delay(delayAmount);
		if(compressTallWindows)
		{
			x = windowWidth + 100;
			y = iTopMargin;
		}
		else
		{
			x = 0;
			y += 40;
		}

		mediaWindowLineBreakY_1 = y - 10;
		lblSelection = new GLabel(mediaWindow, x, y, mediaWindow.width, iMediumBoxHeight, "Selection");
		lblSelection.setLocalColorScheme(G4P.SCHEME_10);
		if(!compressTallWindows) lblSelection.setTextAlign(GAlign.CENTER, null);
		lblSelection.setFont(new Font("Monospaced", Font.PLAIN, iVeryLargeTextSize));
		lblSelection.setTextBold();

		x = iLeftMargin;
		if(compressTallWindows) x += windowWidth;
		y += iLargeBoxHeight;
		chkbxSelectionMode = new GCheckbox(mediaWindow, x, y, 120, iVerySmallBoxHeight, "Enable (A)");
		chkbxSelectionMode.tag = "EnableSelection";
		chkbxSelectionMode.setLocalColorScheme(G4P.SCHEME_10);
		chkbxSelectionMode.setFont(new Font("Monospaced", Font.BOLD, iMediumTextSize));
		chkbxSelectionMode.setSelected(world.viewer.getSettings().selection);
		
		chkbxShowMetadata = new GCheckbox(mediaWindow, x+=125, y, 195, iVerySmallBoxHeight, "Show Metadata (M)");
		chkbxShowMetadata.tag = "ViewMetadata";
		chkbxShowMetadata.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize-1));
		chkbxShowMetadata.setLocalColorScheme(G4P.SCHEME_10);
		chkbxShowMetadata.setSelected(world.getState().showMetadata);
		
		x = iLeftMargin;
		if(compressTallWindows) x += windowWidth;
		y += iSmallBoxHeight;
		chkbxMultiSelection = new GCheckbox(mediaWindow, x, y, 150, iVerySmallBoxHeight, "Multi-Selection (OPT m)");
		chkbxMultiSelection.tag = "MultiSelection";
		chkbxMultiSelection.setLocalColorScheme(G4P.SCHEME_10);
		chkbxMultiSelection.setSelected(world.viewer.getSettings().multiSelection);

		chkbxSegmentSelection = new GCheckbox(mediaWindow, x+160, y, 125, iVerySmallBoxHeight, "Groups (OPT s)");
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
		btnSelectPanorama = new GButton(mediaWindow, x, y, 145, iVerySmallBoxHeight, "Select Panorama (k)");
		btnSelectPanorama.tag = "SelectPanorama";
		btnSelectPanorama.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = iLeftMargin;
		if(compressTallWindows) x += windowWidth;
		y += iMediumBoxHeight;
		btnViewSelected = new GButton(mediaWindow, x, y, 140, iVerySmallBoxHeight, "View Selected (⏎)");
		btnViewSelected.tag = "ViewSelected";
		btnViewSelected.setLocalColorScheme(G4P.CYAN_SCHEME);

		x += 150;
//		if(compressTallWindows) x += windowWidth;
//		y += iMediumBoxHeight;
		btnExportMedia = new GButton(mediaWindow, x, y, 130, iVerySmallBoxHeight, "Export Media (o)");
		btnExportMedia.tag = "ExportMedia";
		btnExportMedia.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = iLeftMargin;
		if(compressTallWindows) x += windowWidth;
		y += iMediumBoxHeight;
		btnDeselectFront = new GButton(mediaWindow, x, y, 110, iVerySmallBoxHeight, "Deselect (X)");
		btnDeselectFront.tag = "DeselectFront";
		btnDeselectFront.setLocalColorScheme(G4P.RED_SCHEME);

		x += 120;
		btnDeselectPanorama = new GButton(mediaWindow, x, y, 155, iVerySmallBoxHeight, "Deselect Panorama (K)");
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

		y += 38;
		mediaWindowLineBreakY_2 = y - 7;
		lblAdvanced = new GLabel(mediaWindow, x, y, mediaWindow.width, iSmallBoxHeight, "Advanced");
		lblAdvanced.setLocalColorScheme(G4P.SCHEME_10);
		lblAdvanced.setFont(new Font("Monospaced", Font.PLAIN, iVeryLargeTextSize));
		if(!compressTallWindows)
			lblAdvanced.setTextAlign(GAlign.CENTER, null);
		lblAdvanced.setTextBold();

		x = 85;
		if(compressTallWindows) x += windowWidth;
		y += iLargeBoxHeight;
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

		x = iLeftMargin;
		if(compressTallWindows) x += windowWidth;
		y += iSmallBoxHeight;
		chkbxShowModel = new GCheckbox(mediaWindow, x, y, 160, iVerySmallBoxHeight, "Show Model (5)");
		chkbxShowModel.tag = "ShowModel";
		chkbxShowModel.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		chkbxShowModel.setLocalColorScheme(G4P.SCHEME_10);
		chkbxShowModel.setSelected(world.getState().showModel);
		
		chkbxMediaToCluster = new GCheckbox(mediaWindow, x += 155, y, 130, iVerySmallBoxHeight, "View Clusters (6)");
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

		chkbxCaptureToCluster = new GCheckbox(mediaWindow, x += 155, y, 170, iVerySmallBoxHeight, "View Adjustment (8)");
		chkbxCaptureToCluster.tag = "CaptureToCluster";
		chkbxCaptureToCluster.setLocalColorScheme(G4P.SCHEME_10);
		chkbxCaptureToCluster.setEnabled(world.getState().showModel);
		chkbxCaptureToCluster.setSelected(world.getState().showCaptureToCluster);

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

//		x = mediaWindowWidth / 2 - 60;
		if(compressTallWindows)
			x = mediaWindowWidth / 2 - 60;
		else
			x = iLeftMargin;
		y = mediaWindowHeight - iBottomTextY;
		btnMediaWindowClose = new GButton(mediaWindow, x, y, 120, iVerySmallBoxHeight, "Close Window (@)");
		btnMediaWindowClose.tag = "CloseMediaWindow";
		btnMediaWindowClose.setLocalColorScheme(G4P.RED_SCHEME);
		
		x = mediaWindowWidth - 150;
		btnMediaWindowExit = new GButton(mediaWindow, x, y, 130, iVerySmallBoxHeight, "Main Menu (space)");
		btnMediaWindowExit.tag = "ExitMediaWindow";
		btnMediaWindowExit.setLocalColorScheme(G4P.CYAN_SCHEME);
//		btnMediaWindowExit.setTextBold();

//		x = 0;
//		lblShift2 = new GLabel(mediaWindow, x, y, mediaWindow.width, iVerySmallBoxHeight);						/* Display Mode Label */
//		lblShift2.setText("Press SHIFT + 2 to show / hide");
//		lblShift2.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
//		lblShift2.setLocalColorScheme(G4P.SCHEME_10);
//		lblShift2.setTextAlign(GAlign.CENTER, null);

		setupMediaWindow = true;
		world.ml.setAppIcon = true;
	}
	
	/**
	 * Setup the Statistics Window
	 */
	public void setupLibraryViewWindow()
	{
		int leftEdge = world.ml.displayWidth / 2 - (windowWidth / 2) - 50;
		int topEdge = world.ml.displayHeight / 2 - libraryViewWindowHeight / 2;
		
		if(libraryViewWindowX > -1 && libraryViewWindowY > -1)
		{
			leftEdge = libraryViewWindowX;
			topEdge = libraryViewWindowY;
		}

		libraryViewWindow = GWindow.getWindow(world.ml, "Library", leftEdge, topEdge, windowWidth, libraryViewWindowHeight, PApplet.JAVA2D);
		libraryViewWindow.setVisible(true);
		libraryViewWindow.addData(new ML_WinData());
		libraryViewWindow.addDrawHandler(this, "libraryViewWindowDraw");
		libraryViewWindow.addKeyHandler(world.ml, "libraryViewWindowKey");
		libraryViewWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		int x = 0, y = iTopMargin;
		world.ml.delay(delayAmount);

		lblLibrary = new GLabel(libraryViewWindow, x, y, libraryViewWindow.width, iVerySmallBoxHeight, "Library");
		lblLibrary.setLocalColorScheme(G4P.SCHEME_10);
		lblLibrary.setTextAlign(GAlign.CENTER, null);
		lblLibrary.setFont(new Font("Monospaced", Font.PLAIN, iVeryLargeTextSize));
		lblLibrary.setTextBold();
		
		world.ml.delay(delayAmount);

		x = 60;
		y += iVeryLargeBoxHeight;
		btnLibraryView = new GButton(libraryViewWindow, x, y, 195, iSmallBoxHeight, "Open Library View (4)");
		btnLibraryView.tag = "SetLibraryView";
		btnLibraryView.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		btnLibraryView.setLocalColorScheme(G4P.GOLD_SCHEME);

		world.ml.delay(delayAmount);

		x = 0;
		y += iLargeBoxHeight;
		lblLibraryViewMode = new GLabel(libraryViewWindow, x, y, libraryViewWindow.width, iMediumBoxHeight, "Current View:");
		lblLibraryViewMode.setLocalColorScheme(G4P.SCHEME_10);
		lblLibraryViewMode.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		lblLibraryViewMode.setTextAlign(GAlign.CENTER, null);

		x = iLeftMargin + 5;
		y += iMediumBoxHeight;
		optLibraryViewClusterMode = new GOption(libraryViewWindow, x, y, 95, iVerySmallBoxHeight, "Cluster  (C)");
		optLibraryViewClusterMode.setLocalColorScheme(G4P.SCHEME_10);
//		optLibraryViewClusterMode.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		optLibraryViewClusterMode.setSelected(world.ml.display.getLibraryViewMode() == 2);
		optLibraryViewClusterMode.tag = "LibraryViewModeCluster";
		optLibraryViewFieldMode = new GOption(libraryViewWindow, x+=105, y, 80, iVerySmallBoxHeight, "Field  (F)");
		optLibraryViewFieldMode.setLocalColorScheme(G4P.SCHEME_10);
//		optLibraryViewFieldMode.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		optLibraryViewFieldMode.setSelected(world.ml.display.getLibraryViewMode() == 1);
		optLibraryViewFieldMode.tag = "LibraryViewModeField";
		optLibraryViewWorldMode = new GOption(libraryViewWindow, x+90, y, 80, iVerySmallBoxHeight, "World  (L)");
		optLibraryViewWorldMode.setLocalColorScheme(G4P.SCHEME_10);
//		optLibraryViewLibraryMode.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		optLibraryViewWorldMode.setSelected(world.ml.display.getLibraryViewMode() == 0);
		optLibraryViewWorldMode.tag = "LibraryViewModeLibrary";

		tgLibraryViewMode = new GToggleGroup();
		tgLibraryViewMode.addControls(optLibraryViewWorldMode, optLibraryViewFieldMode, optLibraryViewClusterMode);

		x = 0;
		y += iVeryLargeBoxHeight + 5;
		lblLibraryViewText = new GLabel(libraryViewWindow, x, y, libraryViewWindow.width, iMediumBoxHeight, "Interest Point:");
		lblLibraryViewText.setLocalColorScheme(G4P.SCHEME_10);
		lblLibraryViewText.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		lblLibraryViewText.setTextAlign(GAlign.CENTER, null);

		x = 35;
		y += iLargeBoxHeight;
		btnPreviousCluster = new GButton(libraryViewWindow, x, y, 120, iVerySmallBoxHeight, "Previous (left)");
		btnPreviousCluster.tag = "PreviousCluster";
		btnPreviousCluster.setLocalColorScheme(G4P.CYAN_SCHEME);

		btnNextCluster = new GButton(libraryViewWindow, x+135, y, 100, iVerySmallBoxHeight, "Next (right)");
		btnNextCluster.tag = "NextCluster";
		btnNextCluster.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 85;
		y += iMediumBoxHeight;
		btnCurrentCluster = new GButton(libraryViewWindow, x, y, 130, iVerySmallBoxHeight, "Show Current (c)");
		btnCurrentCluster.tag = "CurrentCluster";
		btnCurrentCluster.setLocalColorScheme(G4P.CYAN_SCHEME);

		if(world.ml.display.getDisplayView() == 3)
		{
			btnLibraryView.setEnabled(false);
			setLibraryViewWindowControlsEnabled(true);

			switch(world.ml.display.getLibraryViewMode())
			{
				case 0:
					optLibraryViewWorldMode.setSelected(true);
					optLibraryViewFieldMode.setSelected(false);
					optLibraryViewClusterMode.setSelected(false);
					lblLibraryViewText.setText(world.ml.library.getName(false));
					break;
	
				case 1:
					optLibraryViewWorldMode.setSelected(false);
					optLibraryViewFieldMode.setSelected(true);
					optLibraryViewClusterMode.setSelected(false);
					lblLibraryViewText.setText("Field:");
					break;
	
				case 2:
					optLibraryViewWorldMode.setSelected(false);
					optLibraryViewFieldMode.setSelected(false);
					optLibraryViewClusterMode.setSelected(true);
					lblLibraryViewText.setText("Interest Point:");
					break;
			}
		}
		else
		{
			btnLibraryView.setEnabled(true);
			setLibraryViewWindowControlsEnabled(false);
		}

		world.ml.delay(delayAmount / 2);

//		x = libraryViewWindow.width / 2 - 60;
		x = iLeftMargin + 5;
		y = libraryViewWindowHeight - iBottomTextY;
		btnLibraryViewWindowClose = new GButton(libraryViewWindow, x, y, 120, iVerySmallBoxHeight, "Close Window ($)");
		btnLibraryViewWindowClose.tag = "CloseLibraryViewWindow";
		btnLibraryViewWindowClose.setLocalColorScheme(G4P.RED_SCHEME);
		
		x = libraryViewWindow.width - 150;
		btnLibraryViewWindowExit = new GButton(libraryViewWindow, x, y, 130, iVerySmallBoxHeight, "Main Menu (space)");
		btnLibraryViewWindowExit.tag = "ExitLibraryViewWindow";
		btnLibraryViewWindowExit.setLocalColorScheme(G4P.CYAN_SCHEME);

		setupLibraryViewWindow = true;
		world.ml.setAppIcon = true;
		
		world.ml.delay(delayAmount / 2);
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
		btnCloseHelp = new GButton(helpWindow, x, y, 120, iVerySmallBoxHeight, "Close Window");
		btnCloseHelp.tag = "CloseHelpWindow";
//		btnCloseHelp.setFont(new Font("Monospaced", Font.BOLD, iSmallTextSize));
		btnCloseHelp.setLocalColorScheme(G4P.RED_SCHEME);

		x = helpWindow.width - 150;
		btnHelpWindowExit = new GButton(helpWindow, x, y, 130, iVerySmallBoxHeight, "Main Menu (space)");
		btnHelpWindowExit.tag = "ExitHelpWindow";
		btnHelpWindowExit.setLocalColorScheme(G4P.CYAN_SCHEME);
//		btnHelpWindowExit.setTextBold();

		setupHelpWindow = true;
		world.ml.setAppIcon = true;
	}

	/**
	 * Set whether Map Controls in Navigation Window are enabled
	 * @param enable New Map Controls enabled state
	 */
	public void setMapControlsEnabled(boolean enable)
	{
//		System.out.println("Window.setMapControlsEnabled()... "+enable);

		if(enable)		// Enable map controls
		{
			btnPanUp.setEnabled(true);
			btnPanLeft.setEnabled(true);
			btnPanDown.setEnabled(true);
			btnPanRight.setEnabled(true);
			btnZoomToViewer.setEnabled(true);
//			btnZoomToSelected.setEnabled(true);
			btnZoomOutToField.setEnabled(true);
			btnZoomToWorld.setEnabled(true);
			optMapViewFieldMode.setEnabled(true);
			optMapViewWorldMode.setEnabled(true);
		}
		else			// Disable map controls
		{
			btnPanUp.setEnabled(false);
			btnPanLeft.setEnabled(false);
			btnPanDown.setEnabled(false);
			btnPanRight.setEnabled(false);
			btnZoomToViewer.setEnabled(false);
//			btnZoomToSelected.setEnabled(false);
			btnZoomOutToField.setEnabled(false);
			btnZoomToWorld.setEnabled(false);
			optMapViewFieldMode.setEnabled(false);
			optMapViewWorldMode.setEnabled(false);
		}
	}
	
	public void setTimeWindowControlsEnabled(boolean newState)
	{
		btnTimelineReverse.setEnabled(newState);
		btnTimelineForward.setEnabled(newState);	
		btnTimelineZoomIn.setEnabled(newState);
		btnTimelineZoomOut.setEnabled(newState);		
		btnTimelineZoomToField.setEnabled(newState);
		btnTimelineZoomToSelected.setEnabled(newState);
		btnTimelineZoomToFull.setEnabled(newState);		
	}
	
	public void setLibraryViewWindowControlsEnabled(boolean newState)
	{
		optLibraryViewWorldMode.setEnabled(newState);
		optLibraryViewFieldMode.setEnabled(newState);
		optLibraryViewClusterMode.setEnabled(newState);
		btnPreviousCluster.setEnabled(newState);
		btnNextCluster.setEnabled(newState);
		btnCurrentCluster.setEnabled(newState);
	}

	/**
	 * Setup the Help Window
	 */
	public void setupTimeWindow()
	{
		int leftEdge = world.ml.displayWidth / 2 - windowWidth / 2;
		int topEdge = world.ml.displayHeight / 2 - timeWindowHeight / 2;

		if(timeWindowX > -1 && timeWindowY > -1)
		{
			leftEdge = timeWindowX;
			topEdge = timeWindowY;
		}

		timeWindow = GWindow.getWindow(world.ml, "Time", leftEdge, topEdge, windowWidth, timeWindowHeight, PApplet.JAVA2D);
		timeWindow.setVisible(true);
		timeWindow.addData(new ML_WinData());
		timeWindow.addDrawHandler(this, "timeWindowDraw");
		timeWindow.addKeyHandler(world.ml, "timelineWindowKey");
		
		int x = 0, y = iTopMargin;
		world.ml.delay(delayAmount);

		lblTimeWindow = new GLabel(timeWindow, x, y, timeWindow.width, iSmallBoxHeight, "Time");
		lblTimeWindow.setLocalColorScheme(G4P.SCHEME_10);
		lblTimeWindow.setFont(new Font("Monospaced", Font.PLAIN, iVeryLargeTextSize));
		lblTimeWindow.setTextAlign(GAlign.CENTER, null);
		lblTimeWindow.setTextBold();

		x = iLeftMargin;
		y += iVeryLargeBoxHeight;
		lblTimeMode = new GLabel(timeWindow, x, y, 70, iVerySmallBoxHeight, "Mode");
		lblTimeMode.setLocalColorScheme(G4P.SCHEME_10);
		lblTimeMode.setFont(new Font("Monospaced", Font.BOLD, iMediumTextSize));

		x = 90;
		optClusterTimeMode = new GOption(timeWindow, x, y, 85, 20, "Cluster");
		optClusterTimeMode.setLocalColorScheme(G4P.SCHEME_10);
		optClusterTimeMode.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		optClusterTimeMode.tag = "ClusterTimeMode";
		
		world.ml.delay(delayAmount);

		x += 95;
		optFieldTimeMode = new GOption(timeWindow, x, y, 115, 20, "Field (=)");
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

		x = 120;
		y += iMediumBoxHeight;
		chkbxTimeFading = new GCheckbox(timeWindow, x, y, 160, iVerySmallBoxHeight, "Run (f)");
		chkbxTimeFading.tag = "TimeFading";
		chkbxTimeFading.setLocalColorScheme(G4P.SCHEME_10);
		chkbxTimeFading.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		chkbxTimeFading.setSelected(world.getState().timeFading);

		x = iLeftMargin;
		y += iMediumBoxHeight * 0.5f;
		lblTimeCycle = new GLabel(timeWindow, x, y, 85, iVerySmallBoxHeight, "Cycle");
		lblTimeCycle.setLocalColorScheme(G4P.SCHEME_10);
		lblTimeCycle.setFont(new Font("Monospaced", Font.BOLD, iMediumTextSize));

		x = 120;
		y += iMediumBoxHeight * 0.5f;
		chkbxPaused = new GCheckbox(timeWindow, x, y, 140, iVerySmallBoxHeight, "Pause (-)");
		chkbxPaused.tag = "Paused";
		chkbxPaused.setLocalColorScheme(G4P.SCHEME_10);
		chkbxPaused.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize-1));
		chkbxPaused.setSelected(world.getState().paused);

		x = 120;
		y += 5;
		sdrCurrentTime = new GSlider(timeWindow, x, y, 160, 80, 24);
		sdrCurrentTime.setLocalColorScheme(G4P.CYAN_SCHEME);
		sdrCurrentTime.setLimits(0.f, 0.f, 1.f);
		sdrCurrentTime.setValue(0.f);
//		if(world.state.timeFading) 
			sdrCurrentTime.setValue(world.getCurrentTimePoint());
		sdrCurrentTime.setTextOrientation(G4P.ORIENT_TRACK);
		sdrCurrentTime.setEasing(0);
		sdrCurrentTime.setShowValue(true);
		sdrCurrentTime.tag = "CurrentTime";

		x = 35;
		y += iLargeBoxHeight;
		lblCurrentTime = new GLabel(timeWindow, x, y, 100, iVerySmallBoxHeight, "Current Time");
		lblCurrentTime.setLocalColorScheme(G4P.SCHEME_10);

		x = 120;
		y += 15;
		sdrTimeCycleLength = new GSlider(timeWindow, x, y, 160, 80, 20);
		sdrTimeCycleLength.setLocalColorScheme(G4P.GOLD_SCHEME);
		sdrTimeCycleLength.setLimits(0.f, world.settings.timeCycleLength, 3200.f);
		sdrTimeCycleLength.setTextOrientation(G4P.ORIENT_TRACK);
		sdrTimeCycleLength.setEasing(0);
		sdrTimeCycleLength.setShowValue(true);
		sdrTimeCycleLength.tag = "TimeCycleLength";

		x = 35;
		y += 30;
		lblTimeCycleLength = new GLabel(timeWindow, x, y, 120, iVerySmallBoxHeight, "Cycle Length");
		lblTimeCycleLength.setLocalColorScheme(G4P.SCHEME_10);

		x = 120;
		y += 10;
		sdrMediaLength = new GSlider(timeWindow, x, y, 160, 80, 20);
		sdrMediaLength.setLocalColorScheme(G4P.GOLD_SCHEME);
		sdrMediaLength.setLimits(world.settings.defaultMediaLength, 0.f, 250.f);	// setLimits (int initValue, int start, int end)
		sdrMediaLength.setValue(world.settings.defaultMediaLength);
		sdrMediaLength.setTextOrientation(G4P.ORIENT_TRACK);
		sdrMediaLength.setEasing(0);
		sdrMediaLength.setShowValue(true);
		sdrMediaLength.tag = "MediaLength";

		x = 35;
		y += 30;
		lblMediaLength = new GLabel(timeWindow, x, y, 100, iVerySmallBoxHeight, "Media Length");
		lblMediaLength.setLocalColorScheme(G4P.SCHEME_10);
		
		x = 120;
		y += 10;
		sdrClusterLength = new GSlider(timeWindow, x, y, 160, 80, 20);
		sdrClusterLength.setLocalColorScheme(G4P.GOLD_SCHEME);
		sdrClusterLength.setLimits(world.settings.clusterLength, 0.f, 1.f);
		sdrClusterLength.setValue(world.settings.clusterLength);
		sdrClusterLength.setTextOrientation(G4P.ORIENT_TRACK);
		sdrClusterLength.setEasing(0);
		sdrClusterLength.setShowValue(true);
		sdrClusterLength.tag = "ClusterLength";
		
		x = 35;
		y += 30;
		lblClusterLength = new GLabel(timeWindow, x, y, 120, iVerySmallBoxHeight, "Cluster Length");
		lblClusterLength.setLocalColorScheme(G4P.SCHEME_10);

		switch(world.state.timeMode)
		{
			case 0:												// Cluster
				sdrClusterLength.setVisible(false);
				lblClusterLength.setVisible(false);
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
		y += iMediumBoxHeight;
		lblTimeline = new GLabel(timeWindow, x, y, timeWindow.width, iSmallBoxHeight, "Time View");
		lblTimeline.setLocalColorScheme(G4P.SCHEME_10);
		lblTimeline.setTextAlign(GAlign.CENTER, null);
		lblTimeline.setFont(new Font("Monospaced", Font.PLAIN, iVeryLargeTextSize));
		lblTimeline.setTextBold();
		
		x = 70;
		y += iVeryLargeBoxHeight;
		btnTimeView = new GButton(timeWindow, x, y, 175, iSmallBoxHeight, "Open Time View (3)");
		btnTimeView.tag = "SetTimeView";
		btnTimeView.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		btnTimeView.setLocalColorScheme(G4P.GOLD_SCHEME);
//		if(world.ml.display.getDisplayView() == 2) btnTimeView.setEnabled(false);
		
//		if (key == 'r')									// Zoom out to whole timeline
//			ml.display.resetZoom(ml.world, true);
//
//		if (key == 'z')									// Zoom to fit timeline
//			ml.display.zoomToTimeline(ml.world, true);
//
//		if (key == 'c')									// Zoom to fit current time segment
//			ml.display.zoomToCurrentSelectableTimeSegment(ml.world, true);
//
//		if (key == 'd')									// Zoom to fit current time segment
//			ml.display.zoomToCurrentSelectableDate(ml.world, true);
//
//		if (key == 'a')									// Timeline zoom to fit
//			ml.display.showAllDates();

		x = 35;
		y += iVeryLargeBoxHeight + 5;
		btnTimelineZoomIn = new GButton(timeWindow, x, y, 115, iVerySmallBoxHeight, "Zoom In (up)");
		btnTimelineZoomIn.tag = "TimelineZoomIn";
		btnTimelineZoomIn.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnTimelineZoomIn.fireAllEvents(true);
		x += 125;
		btnTimelineZoomOut = new GButton(timeWindow, x, y, 125, iVerySmallBoxHeight, "Zoom Out (down)");
		btnTimelineZoomOut.tag = "TimelineZoomOut";
		btnTimelineZoomOut.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnTimelineZoomOut.fireAllEvents(true);

		x = 110;
		y += iLargeBoxHeight;
		btnTimelineZoomToField = new GButton(timeWindow, x, y, 75, iVerySmallBoxHeight, "Fit (z)");
		btnTimelineZoomToField.tag = "TimelineZoomToFit";
		btnTimelineZoomToField.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnTimelineZoomToSelected = new GButton(timeWindow, x+80, y, 100, iVerySmallBoxHeight, "Selected (c)");
		btnTimelineZoomToSelected.tag = "TimelineZoomToSelected";
		btnTimelineZoomToSelected.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = iLeftMargin;
		y += iMediumBoxHeight * 0.5f;
		lblTimelineZoomTo = new GLabel(timeWindow, x, y, timeWindow.width, iSmallBoxHeight, "Zoom To:");
		lblTimelineZoomTo.setLocalColorScheme(G4P.SCHEME_10);
		lblTimelineZoomTo.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		
		x = 110;
		y += iMediumBoxHeight * 0.5f;
		btnTimelineZoomToFull = new GButton(timeWindow, x, y, 105, iVerySmallBoxHeight, "Timeline (r)");
		btnTimelineZoomToFull.tag = "TimelineZoomToFull";
		btnTimelineZoomToFull.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = iLeftMargin;
		y += iLargeBoxHeight;
		lblTimelineScroll = new GLabel(timeWindow, x, y, timeWindow.width, iSmallBoxHeight, "Scroll:");
		lblTimelineScroll.setLocalColorScheme(G4P.SCHEME_10);
		lblTimelineScroll.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));

		x = 110;
		btnTimelineReverse = new GButton(timeWindow, x, y, 75, iVerySmallBoxHeight, "Back (left)");
		btnTimelineReverse.tag = "TimelineReverse";
		btnTimelineReverse.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnTimelineReverse.fireAllEvents(true);

		x += 80;
		btnTimelineForward = new GButton(timeWindow, x, y, 105, iVerySmallBoxHeight, "Forward (right)");
		btnTimelineForward.tag = "TimelineForward";
		btnTimelineForward.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnTimelineForward.fireAllEvents(true);

		x = iLeftMargin + 5;
		y = timeWindowHeight - iBottomTextY;
		btnTimeWindowClose = new GButton(timeWindow, x, y, 120, iVerySmallBoxHeight, "Close Window (#)");
		btnTimeWindowClose.tag = "CloseTimeWindow";
		btnTimeWindowClose.setLocalColorScheme(G4P.RED_SCHEME);
		
		x = timeWindow.width - 150;
		btnTimeWindowExit = new GButton(timeWindow, x, y, 130, iVerySmallBoxHeight, "Main Menu (space)");
		btnTimeWindowExit.tag = "ExitTimeWindow";
		btnTimeWindowExit.setLocalColorScheme(G4P.CYAN_SCHEME);
//		btnTimeWindowExit.setTextBold();

//		x = 0;
//		y = timeWindowHeight - iBottomTextY;
//		lblShift4 = new GLabel(timeWindow, x, y, timeWindow.width, iSmallBoxHeight);						/* Display Mode Label */
//		lblShift4.setText("Press SHIFT + 3 to show / hide");
//		lblShift4.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
//		lblShift4.setLocalColorScheme(G4P.SCHEME_10);
//		lblShift4.setTextAlign(GAlign.CENTER, null);
		
		switch(display.getDisplayView())
		{
			case 0:					// World 
			case 1:					// Map
				btnTimeView.setEnabled(true);
				setTimeWindowControlsEnabled(false);
				break;
			case 2:					// Time
				btnTimeView.setEnabled(false);
				setTimeWindowControlsEnabled(true);
				break;
			case 3:					// Library
				btnTimeView.setEnabled(true);
				setTimeWindowControlsEnabled(false);
				break;
		}
		
		setupTimeWindow = true;
		world.ml.setAppIcon = true;
	}

	/**
	 * Setup and open Library Window
	 */
	public void openStartupWindow()
	{
		int leftEdge = world.ml.displayWidth / 2 - windowWidth;
		int topEdge = world.ml.displayHeight / 2 - startupWindowHeight / 2;
		
		startupWindow = GWindow.getWindow( world.ml, "", leftEdge, topEdge, windowWidth * 2, startupWindowHeight, PApplet.JAVA2D);
		
		startupWindow.addData(new ML_WinData());
		startupWindow.addDrawHandler(this, "libraryWindowDraw");
//		startupWindow.addMouseHandler(this, "libraryWindowMouse");
		startupWindow.addKeyHandler(world.ml, "libraryWindowKey");
		startupWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		int x = 0, y = iTopMargin * 5 / 2;
		world.ml.delay(10);
		lblStartup = new GLabel(startupWindow, x, y, startupWindow.width, 22, "Welcome to MultimediaLocator.");
		lblStartup.setLocalColorScheme(G4P.SCHEME_10);
		lblStartup.setFont(new Font("Monospaced", Font.PLAIN, iVeryLargeTextSize));
		lblStartup.setTextAlign(GAlign.CENTER, null);

		x = 0;
		lblStartupWindowText = new GLabel(startupWindow, x, y, startupWindow.width, 22, "Opening MultimediaLocator library...");
		lblStartupWindowText.setLocalColorScheme(G4P.SCHEME_10);
		lblStartupWindowText.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
		lblStartupWindowText.setTextAlign(GAlign.CENTER, null);
		lblStartupWindowText.setVisible(false);

		x = 60;
		y += 50;
		btnCreateLibrary = new GButton(startupWindow, x, y, 190, iLargeBoxHeight, "Create Library");
		btnCreateLibrary.tag = "CreateLibrary";
		btnCreateLibrary.setFont(new Font("Monospaced", Font.BOLD, iLargeTextSize));
		btnCreateLibrary.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnOpenLibrary = new GButton(startupWindow, x+=220, y, 175, iLargeBoxHeight, "Open Library");
		btnOpenLibrary.tag = "OpenLibrary";
		btnOpenLibrary.setFont(new Font("Monospaced", Font.BOLD, iLargeTextSize));
		btnOpenLibrary.setLocalColorScheme(G4P.CYAN_SCHEME);
		chkbxRebuildLibrary = new GCheckbox(startupWindow, x+=195, y+5, 125, iVerySmallBoxHeight, "Rebuild");
		chkbxRebuildLibrary.tag = "RebuildLibrary";
		chkbxRebuildLibrary.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
		chkbxRebuildLibrary.setLocalColorScheme(G4P.SCHEME_10);
		chkbxRebuildLibrary.setSelected(world.ml.state.rebuildLibrary);
		
		y += 50;
		btnLibraryHelp = new GButton(startupWindow, windowWidth * 2 - 30 - iLeftMargin, y, 30, 30, "?");
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
		lblStartupWindowText.setText(newText);
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
		createLibraryWindow.addDrawHandler(this, "createLibraryWindowDraw");
		createLibraryWindow.addMouseHandler(this, "createLibraryWindowMouse");
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
		lblCreateLibraryWindowText = new GLabel(createLibraryWindow, x, y + 35, createLibraryWindow.width, 22, "Creating MultimediaLocator library...");
		lblCreateLibraryWindowText.setLocalColorScheme(G4P.SCHEME_10);
		lblCreateLibraryWindowText.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
		lblCreateLibraryWindowText.setTextAlign(GAlign.CENTER, null);
		lblCreateLibraryWindowText.setVisible(false);

		x = 0;
		lblCreateLibraryWindowText2 = new GLabel(createLibraryWindow, x, y + 95, createLibraryWindow.width, 22, "Please wait. The process may take several minutes...");
		lblCreateLibraryWindowText2.setLocalColorScheme(G4P.SCHEME_10);
		lblCreateLibraryWindowText2.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
		lblCreateLibraryWindowText2.setTextAlign(GAlign.CENTER, null);
		lblCreateLibraryWindowText2.setVisible(false);

		x = windowWidth * 3 / 2 - 78;
		y += 45;
		btnImportMediaFolder = new GButton(createLibraryWindow, x, y, 145, iSmallBoxHeight, "Add Folder");
		btnImportMediaFolder.tag = "AddMediaFolder";
		btnImportMediaFolder.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize-1));
		btnImportMediaFolder.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = windowWidth * 3 / 2 - 115;
		y = createLibraryWindowHeight - iLargeBoxHeight * 3 / 2 - 10;
		btnMakeLibrary = new GButton(createLibraryWindow, x, y, 230, iSmallBoxHeight, "Finish and Choose Location...");
		btnMakeLibrary.tag = "MakeLibrary";
		btnMakeLibrary.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize-1));
		btnMakeLibrary.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = windowWidth * 3 - 120;
		btnCancelCreateLibrary = new GButton(createLibraryWindow, x, y, 100, iSmallBoxHeight, "Cancel");
		btnCancelCreateLibrary.tag = "CancelCreateLibrary";
		btnCancelCreateLibrary.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize-1));
		btnCancelCreateLibrary.setLocalColorScheme(G4P.RED_SCHEME);

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
			listItemWindow.setActionOnClose(GWindow.KEEP_OPEN);
			
			showListItemWindow = true;
			world.ml.setAppIcon = true;
		}
	}
	
	public void closeChooseItemDialog()
	{
		listItemWindow.setVisible(false);
		listItemWindow.close();
		listItemWindow.dispose();
		showListItemWindow = false;
	}

	/**
	 * Open window to enter text and return result
	 * @param promptText
	 * @param resultCode
	 * @return Text entered by user
	 */
	public void openTextEntryWindow(String promptText, String initText, int resultCode)
	{
		textEntryWindowResultCode = resultCode;					// Flag indicating what to do with dialog result value

		int leftEdge = world.ml.displayWidth / 2 - windowWidth;
		int topEdge = world.ml.displayHeight / 2 - createLibraryWindowHeight / 2;

		String saveText = "";
		if(setupTextEntryWindow) saveText = txfInputText.getText();
		
		textEntryWindow = GWindow.getWindow( world.ml, "", leftEdge, topEdge, windowWidth * 2, textEntryWindowHeight, PApplet.JAVA2D);

		textEntryWindow.addData(new ML_WinData());
		textEntryWindow.addDrawHandler(this, "textEntryWindowDraw");
//		textEntryWindow.addKeyHandler(world.ml, "textEntryWindowKey");
		textEntryWindow.setActionOnClose(GWindow.KEEP_OPEN);

		int x = 0, y = iTopMargin * 3;

		lblText = new GLabel(textEntryWindow, x, y, textEntryWindow.width, 22, promptText);
		lblText.setLocalColorScheme(G4P.SCHEME_10);
		lblText.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
		lblText.setTextAlign(GAlign.CENTER, null);
		lblText.setTextBold();

		y += iLargeBoxHeight;
		
		x = windowWidth / 8;
		txfInputText = new GTextField(textEntryWindow, x, y, windowWidth * 3 / 2, 30, G4P.SCROLLBARS_HORIZONTAL_ONLY | G4P.SCROLLBARS_AUTOHIDE);
		
		if(setupTextEntryWindow) txfInputText.setText(saveText);
		else txfInputText.setText(initText);
		
		txfInputText.setLocalColorScheme(G4P.GREEN_SCHEME);

		x = windowWidth * 2 - 50;
		btnEnterText = new GButton(textEntryWindow, x, y, 40, iLargeBoxHeight, "OK");
		btnEnterText.tag = "EnteredText";
		btnEnterText.setFont(new Font("Monospaced", Font.BOLD, iLargeTextSize));
		btnEnterText.setLocalColorScheme(G4P.CYAN_SCHEME);

		setupTextEntryWindow = true;
		showTextEntryWindow = true;
		world.ml.setAppIcon = true;
	}
	
	/**
	 * Close Text Entry Window
	 */
	public void closeTextEntryWindow()
	{
		textEntryWindow.setVisible(false);
		textEntryWindow.close();
		textEntryWindow.dispose();
		showTextEntryWindow = false;
		setupTextEntryWindow = false;
	}

	/**
	 * Handle window with lost focus
	 * @param windowTitle Window title
	 */
	public void handleWindowLostFocus(String windowTitle)
	{
		switch(windowTitle)
		{
			case "Main Menu":
				hideMainMenu();
				break;
			case "Navigation":
				hideNavigationWindow();
				break;
			case "Media":
				hideMediaWindow();
				break;
			case "Time":
				hideTimeWindow();
				break;
			case "Library":
				hideLibraryViewWindow();
				break;
			case "Help":
				hideHelpWindow();
				break;
		}
	}
	
	/**
	 * Update secondary windows
	 */
	public void update()
	{
//		if(world.ml.frameCount % closeWindowWaitTime == 0)							/* Close hidden windows after ~3 seconds */
		if(world.ml.frameCount - lastWindowHiddenFrame > closeWindowWaitTime)		/* Close hidden window(s) after a few seconds */
		{
			if(setupMainMenu && !showMainMenu)
				closeMainMenu();
			if(setupNavigationWindow && !showNavigationWindow)
				closeNavigationWindow();
			if(setupMediaWindow && !showMediaWindow)
				closeMediaWindow();
			if(setupTimeWindow && !showTimeWindow)
				closeTimeWindow();
			if(setupLibraryViewWindow && !showLibraryViewWindow)
				closeLibraryViewWindow();
			if(setupHelpWindow && !showHelpWindow)
				closeHelpWindow();
		}
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
					applet.line(x, startupWindowHeight - 85, x, startupWindowHeight - 45);
			}
		}
	}

	/**
	 * Handles mouse events for all GWindow objects
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void libraryWindowMouse(PApplet applet, GWinData data, MouseEvent event) 
	{
	
	}
	
	/**
	 * Handles drawing to the ML Window
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 */
	public void createLibraryWindowDraw(PApplet applet, GWinData data) 
	{
		float smallTextSize = 11.f;
		float mediumTextSize = 16.f;
		applet.background(0);
		applet.stroke(255, 0, 255);
		applet.strokeWeight(1);
		applet.fill(255, 255, 255);
		applet.textSize(mediumTextSize);
		
		int x = windowWidth * 3 / 2 - 90;
		int y = 145;
		
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
						applet.textSize(smallTextSize+1);
						x = iLeftMargin * 2;
						for(String strFolder : display.ml.library.mediaFolders)
						{
							applet.text(strFolder, x, y);
							y += iSmallBoxHeight - 5;
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
		int y = iTopMargin * 3;

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
	 * Handles drawing to the ML Window
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 */
	public void textEntryWindowDraw(PApplet applet, GWinData data) 
	{
		applet.background(0);
		applet.fill(255);
	}

	/**
	 * Handles mouse events for all GWindow objects
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void createLibraryWindowMouse(PApplet applet, GWinData data, MouseEvent event) 
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

	/**
	 * Handles drawing to the Navigation Window
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 */
	public void navigationWindowDraw(PApplet applet, GWinData data) 
	{
		applet.background(0);
		applet.colorMode(PConstants.HSB);
		applet.stroke(0, 0, 65, 255);
		applet.strokeWeight(1);
		applet.line(0, navigationWindowLineBreakY_1, windowWidth, navigationWindowLineBreakY_1);
//		applet.stroke(0, 0, 155, 255);
//		applet.strokeWeight(2);
		applet.line(0, navigationWindowLineBreakY_2, windowWidth, navigationWindowLineBreakY_2);
//		if(!compressTallWindows) applet.line(0, navigationWindowLineBreakY_2, windowWidth, navigationWindowLineBreakY_2);
		if(!compressTallWindows) applet.line(0, navigationWindowLineBreakY_3, windowWidth, navigationWindowLineBreakY_3);
		
//		if(world.state.timeFading && !world.state.paused)
//			sdrCurrentTime.setValue(world.getCurrentTimePoint());
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
		applet.stroke(0, 0, 75, 255);
		applet.strokeWeight(1);
//		applet.stroke(0, 0, 155, 255);
//		applet.strokeWeight(2);
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
//			System.out.println("Window.mediaWindowMouse()... Mouse pressed: event x:"+event.getX()+" y:"+event.getY());
//			System.out.println("  applet.mouseX:"+applet.mouseX+" applet.mouseY:"+applet.mouseY);
			break;
		case MouseEvent.RELEASE:
			data2.ex = applet.mouseX;
			data2.ey = applet.mouseY;
			data2.done = true;
//			System.out.println("Window.mediaWindowMouse()... Mouse released: event x:"+event.getX()+" y:"+event.getY());
//			System.out.println("  applet.mouseX:"+applet.mouseX+" applet.mouseY:"+applet.mouseY);
			break;
		case MouseEvent.DRAG:
			data2.ex = applet.mouseX;
			data2.ey = applet.mouseY;
//			System.out.println("Window.mediaWindowMouse()... Mouse dragged: event x:"+event.getX()+" y:"+event.getY());
//			System.out.println("  applet.mouseX:"+applet.mouseX+" applet.mouseY:"+applet.mouseY);
			break;
		}
	}

	/**
	 * Handles drawing to the Library View Window
	 * @param applet Main PApplet object
	 * @param data GWindow data 
	 */
	public void libraryViewWindowDraw(PApplet applet, GWinData data) 
	{
		applet.background(0);
		if(world.ml.state.running && setupLibraryViewWindow && libraryWindowViewerTextYOffset > 0)
		{
			applet.stroke(255);
			applet.strokeWeight(1);
			applet.fill(255, 255, 255);
		}
	}
	
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
	public void timeWindowDraw(PApplet applet, GWinData data) 
	{
		applet.background(0,0,0);
		applet.stroke(255);
		applet.strokeWeight(1);
		applet.fill(255, 255, 255);

	}

	/**
	 * Handles mouse events for Map Window 
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
//	public void timelineWindowMouse(PApplet applet, GWinData data, MouseEvent event) {
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
	 * Show Main Menu
	 */
	public void showMainMenu()
	{
		showMainMenu = true;
		mainMenu.setVisible(true);
	} 
	/**
	 * Show Navigation Window
	 */
	public void showNavigationWindow()
	{
		showNavigationWindow = true;
		if(setupNavigationWindow)
			navigationWindow.setVisible(true);
	} 
	/**
	 * Show Media Window
	 */
	public void showMediaWindow()
	{
		showMediaWindow = true;
		if(setupMediaWindow)
			mediaWindow.setVisible(true);
	}
	public void showStatisticsWindow()
	{
		showLibraryViewWindow = true;
		if(setupLibraryViewWindow)
			libraryViewWindow.setVisible(true);
	} 
	public void showHelpWindow()
	{
		showHelpWindow = true;
		if(setupHelpWindow)
			helpWindow.setVisible(true);
		helpAboutText = 0;					// Always start with "About" text
	}
//	public void showMapWindow()
//	{
//		showMapWindow = true;
//		if(setupMapWindow)
//			mapWindow.setVisible(true);
//	}
	public void showTimeWindow()
	{
		showTimeWindow = true;
		if(setupTimeWindow)
			timeWindow.setVisible(true);
	}
	public void showStartupWindow()
	{
		hideWindows();
		showLibraryWindow = true;
		if(setupLibraryWindow)
			startupWindow.setVisible(true);
	}
	/**
	 * Hide Main Menu
	 */
	public void hideMainMenu()
	{
		showMainMenu = false;
		mainMenu.setVisible(false);
		lastWindowHiddenFrame = world.ml.frameCount;
	} 
	public void closeMainMenu()
	{
		showMainMenu = false;
		if(setupMainMenu)
		{
			mainMenuX = (int)getLocation(mainMenu).x;
			mainMenuY = (int)getLocation(mainMenu).y;
			mainMenu.setVisible(false);
			mainMenu.close();
			mainMenu.dispose();
			setupMainMenu = false;
		}
	} 
	public void hideNavigationWindow()
	{
		showNavigationWindow = false;
		if(setupNavigationWindow)
			navigationWindow.setVisible(false);
		lastWindowHiddenFrame = world.ml.frameCount;
	} 
	public void closeNavigationWindow()
	{
		showNavigationWindow = false;
		if(setupNavigationWindow)
		{
			navigationWindow.setVisible(false);
			navigationWindowX = (int)getLocation(navigationWindow).x;
			navigationWindowY = (int)getLocation(navigationWindow).y;
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
		lastWindowHiddenFrame = world.ml.frameCount;
	}
	public void closeMediaWindow()
	{
		showMediaWindow = false;
		if(setupMediaWindow)
		{
			mediaWindowX = (int)getLocation(mediaWindow).x;
			mediaWindowY = (int)getLocation(mediaWindow).y;
			mediaWindow.setVisible(false);
			mediaWindow.close();
			mediaWindow.dispose();
			setupMediaWindow = false;
		}
	} 
	public void hideLibraryViewWindow()
	{
		showLibraryViewWindow = false;
		if(setupLibraryViewWindow)
			libraryViewWindow.setVisible(false);
		lastWindowHiddenFrame = world.ml.frameCount;
	} 
	public void closeLibraryViewWindow()
	{
		showLibraryViewWindow = false;
		if(setupLibraryViewWindow)
		{
			libraryViewWindowX = (int)getLocation(libraryViewWindow).x;
			libraryViewWindowY = (int)getLocation(libraryViewWindow).y;
			libraryViewWindow.setVisible(false);
			libraryViewWindow.close();
			libraryViewWindow.dispose();
			setupLibraryViewWindow = false;
		}
	} 
	public void hideHelpWindow()
	{
		showHelpWindow = false;
		if(setupHelpWindow)
			helpWindow.setVisible(false);
		lastWindowHiddenFrame = world.ml.frameCount;
	}
	public void closeHelpWindow()
	{
		showHelpWindow = false;
		if(setupHelpWindow)
		{
			helpWindowX = (int)getLocation(helpWindow).x;
			helpWindowY = (int)getLocation(helpWindow).y;
			helpWindow.setVisible(false);
			helpWindow.close();
			helpWindow.dispose();
			setupHelpWindow = false;
		}
	}
	/**
	 * Close and reload navigation window
	 */
	public void reloadNavigationWindow()
	{
		if(setupNavigationWindow)
		{
			closeNavigationWindow();
			openNavigationWindow();
		}
	}
	
	/**
	 * Hide all windows
	 */
	public void hideAllWindows()
	{
		hideMainMenu();
		hideNavigationWindow();
		hideMediaWindow();
		hideLibraryViewWindow();
		hideHelpWindow();
	}
	
	/**
	 * Close all windows
	 */
	public void closeAllWindows()
	{
		closeMainMenu();
		closeNavigationWindow();
		closeMediaWindow();
		closeLibraryViewWindow();
		closeHelpWindow();
	}
	
	/**
	 * Get location of specified window
	 * @param applet PApplet of window to get location for
	 * @return Window location 
	 */
	private PVector getLocation(PApplet applet) {
		if(applet.getGraphics().isGL())
			return new PVector(
					((GLWindow) applet.getSurface().getNative()).getX(),
					((GLWindow) applet.getSurface().getNative()).getY()
					);
		else
			try {
				return new PVector(
						((SmoothCanvas) applet.getSurface().getNative()).getFrame().getX(),
						((SmoothCanvas) applet.getSurface().getNative()).getFrame().getY()
						);
			}
		catch (Exception e) {
			return new PVector(
					(int) ((Canvas) applet.getSurface().getNative()).getLocation().getX(),
					(int) ((Canvas) applet.getSurface().getNative()).getLocation().getY()
					);
		}
	}

//	public void hideMapWindow()
//	{
//		showMapWindow = false;
//		if(setupMapWindow)
//			mapWindow.setVisible(false);
//	}
//	public void closeMapWindow()
//	{
//		showMapWindow = false;
//		if(setupMapWindow)
//		{
//			mapWindow.setVisible(false);
//			mapWindow.close();
//			mapWindow.dispose();
//			setupMapWindow = false;
//		}
//	} 
	public void hideTimeWindow()
	{
		showTimeWindow = false;
		if(setupTimeWindow)
			timeWindow.setVisible(false);
	}
	public void closeTimeWindow()
	{
		showTimeWindow = false;
		if(setupTimeWindow)
		{
			timeWindow.setVisible(false);
			timeWindow.close();
			timeWindow.dispose();
			setupTimeWindow = false;
		}
	}
	public void hideLibraryWindow()
	{
		showLibraryWindow = false;
		if(setupLibraryWindow)
			startupWindow.setVisible(false);
	} 
	public void closeLibraryWindow()
	{
		showLibraryWindow = false;
		if(setupLibraryWindow)
		{
			startupWindow.setVisible(false);
			startupWindow.close();
			startupWindow.dispose();
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
		if(showMainMenu)
			hideMainMenu();
		if(showNavigationWindow)
			hideNavigationWindow();
		if(showMediaWindow)
			hideMediaWindow();
		if(showLibraryViewWindow)
			hideLibraryViewWindow();
		if(showHelpWindow)
			hideHelpWindow();
//		if(showMapWindow)
//			hideMapWindow();
		if(showTimeWindow)
			hideTimeWindow();
	}
	public void openMainMenu()
	{
		if(!setupMainMenu) setupMainMenu();
		showMainMenu();
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
	public void openLibraryViewWindow()
	{
		if(!setupLibraryViewWindow)
			setupLibraryViewWindow();
		showStatisticsWindow();
	}
	public void openHelpWindow()
	{
		if(!setupHelpWindow)
			setupHelpWindow();
		showHelpWindow();
	}
//	public void openMapWindow()
//	{
//		if(!setupMapWindow)
//			setupMapWindow();
//		showMapWindow();
//	}
	public void openTimeWindow()
	{
		if(!setupTimeWindow)
			setupTimeWindow();
		showTimeWindow();
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

//	MyWinData(){}
}