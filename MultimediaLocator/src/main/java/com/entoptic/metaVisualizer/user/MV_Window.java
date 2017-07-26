package main.java.com.entoptic.metaVisualizer.user;

import java.awt.Canvas;
import java.awt.Font;
import java.util.ArrayList;

import com.jogamp.newt.opengl.GLWindow;

import g4p_controls.*;
import main.java.com.entoptic.metaVisualizer.misc.WMV_Utilities;
import main.java.com.entoptic.metaVisualizer.world.WMV_World;
import processing.awt.PSurfaceAWT.SmoothCanvas;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;
import processing.event.MouseEvent;

/*********************************
 * Secondary window handler
 * @author davidgordon
 */
public class MV_Window 
{
	/* Classes */
	MV_Display display;
	WMV_Utilities utilities;
	
	/* General */
	private final int delayAmount = 100;							// Delay length to avoid G4P library concurrent modification exception
	
	/* Windows */
	private final int windowWidth = 310, timeWindowWidth = 390;
	private final int shortWindowHeight = 340, mediumWindowHeight = 600;
	public boolean compressTallWindows = false;

	private final int closeWindowWaitTime = 180;			// Time to wait before closing hidden windows
	private int lastWindowHiddenFrame;						// Frame when last window was hidden

	public GWindow mainMenu, navigationWindow, mediaWindow, preferencesWindow,  helpWindow, 
				   mapWindow, timeWindow;
	public GWindow startupWindow;
	public GWindow createLibraryWindow, listItemWindow, textEntryWindow;

	public GLabel lblMainMenu, lblNavigationWindow, lblMedia, lblPrefs, lblHelp, lblMap, lblTimeline;
	public GLabel lblStartup, lblImport;	
	
	public boolean setupMainMenu, setupNavigationWindow = false, setupMediaWindow = false, setupHelpWindow = false, 
				   setupPreferencesWindow = false, setupTimeWindow = false;
	public boolean setupCreateLibraryWindow = false, setupStartupWindow = false, setupTextEntryWindow = false;
	
	public boolean showMainMenu = false, showNavigationWindow = false, showMediaWindow = false, showPreferencesWindow = false, 
				   showHelpWindow = false, showTimeWindow = false;;
	public boolean showCreateLibraryWindow, showStartupWindow = false;

	public boolean closeStartupWindow = false, closeTextEntryWindow = false, closeListItemWindow = false;
	
	private int mainMenuX = -1, mainMenuY = -1;
	private int navigationWindowX = -1, navigationWindowY = -1;
	private int mediaWindowX = -1, mediaWindowY = -1;
	private int timeWindowX = -1, timeWindowY = -1;
	private int preferencesWindowX = -1, preferencesWindowY = -1;
	private int helpWindowX = -1, helpWindowY = -1;
	
	/* Library Window */
	private int startupWindowHeight;
	public GButton btnCreateLibrary, btnOpenLibrary; //, btnLibraryHelp;
	public GCheckbox chkbxRebuildLibrary;
	public GLabel lblStartupWindowText;
	
	/* Create Library Window */
	private int createLibraryWindowHeight;
	public GButton btnImportMediaFolder, btnMakeLibrary, btnCancelCreateLibrary;
	public GLabel lblCreateLibraryWindowText, lblCreateLibraryWindowText2;

	/* List Item Window */
	public boolean showListItemWindow = false, setupListItemWindow = false;
	private int listItemWindowHeight;
	private ArrayList<String> listItemWindowList;
	private String listItemWindowText;
	public int listItemWindowSelectedItem = -1;
	public int listItemWindowResultCode = -1;		// 1: GPS Track  

	/* Text Entry Window */
	public GTextField txfInputText;
	public GButton btnEnterText;
	public GLabel lblText;
	
	public boolean showTextEntryWindow = false;
	private int textEntryWindowHeight;
//	public int textEntryWindowSelectedItem = -1;
//	private String textEntryWindowText;				// Prompt text
//	private String textEntryWindowUserEntry;		// User entry
	public int textEntryWindowResultCode = -1;		// 1: GPS Track  
	
	/* Main Window */
	private GButton btnNavigationWindow, btnMediaWindow, btnPreferencesWindow, btnTimeWindow;//, btnHelpWindow;
	public GButton btnSelectField, btnSaveLibrary, btnSaveField, btnRestart, btnQuit;
	private GLabel lblViewMode, lblOpenMenu, lblLibraryCommands, lblSpaceBar;
	public GToggleGroup tgDisplayView;	
	public GOption optWorldView, optMapView, optTimelineView, optLibraryView;
	private int mainMenuHeight;
	public GCheckbox chkbxScreenMessagesOn;

	/* Navigation Window */
	public GCheckbox chkbxPathFollowing; 															/* Navigation Window Options */
	public GCheckbox chkbxFollowTeleport;
	public GOption optMove, optTeleport;
	public GToggleGroup tgNavigationTeleport;	
//	public GCheckbox chkbxMovementTeleport, chkbxFollowTeleport;
	
	public GSlider sdrTeleportLength, sdrPathWaitLength;											/* Navigation Window Sliders */

	private GButton btnZoomIn, btnZoomOut;															/* Navigation Window Buttons */
	private GButton btnSaveLocation, btnClearMemory, btnSetHome;
	private GButton btnJumpToRandomCluster;
	public GButton btnExportScreenshot, btnExportMedia, btnOutputFolder;
	private GButton btnNextTimeSegment, btnPreviousTimeSegment, btnStopViewer;
	private GButton btnMoveToNearestCluster, btnMoveToLastCluster;
	private GButton btnMoveToNearestImage, btnMoveToNearestPanorama;
	private GButton btnMoveToNearestVideo, btnMoveToNearestSound;
	public GButton btnGoToPreviousField, btnGoToNextField;
	private GButton btnChooseGPSTrack;

	public GButton btnWorldView, btnMapView;		
	private GButton btnMoveForward, btnMoveLeft, btnMoveBackward, btnMoveRight;		
	private GButton btnMoveUp, btnMoveDown;		
	private GButton btnLookUp, btnLookDown;		
	private GButton btnLookLeft, btnLookRight;
	
	private GButton btnZoomToViewer, btnMoveToSelected;		// -- btnMoveToSeleced == In progress
	private GButton btnZoomOutToField, btnMapZoomIn, btnMapZoomOut, btnZoomToWorld;		
	public GLabel lblZoomTo, lblMapZoom, lblMove, lblLook;					
	public GOption optMapViewFieldMode, optMapViewWorldMode;
	public GToggleGroup tgMapViewMode;	
//	int mapWindowHeight;

	private GLabel lblTimeNavigation, lblManualNavigation, lblAutoNavigation, lblNearby, lblPathNavigation, 			/* Navigation Window Labels */
	lblTeleportLength, lblPathWaitLength, lblNoGPSTracks, lblGPSTrackSpeed, lblTeleportSettings;
//	public GLabel lblShift1;
	private GButton btnNavigationWindowExit, btnNavigationWindowClose;		

	private int navigationWindowLineBreakY_1, navigationWindowLineBreakY_2, navigationWindowLineBreakY_3, navigationWindowLineBreakY_4, navigationWindowLineBreakY_5;		
	int navigationWindowHeight, navigationWindowWidth;
	
	private GLabel lblTimeCycle, lblTimeMode;										/* Time Controls */
	public GLabel lblMediaLength, lblTimeCycleLength, lblCurrentTime, lblFadeLength, lblClusterLength;
	public GLabel lblTime;
	public GCheckbox chkbxPaused, chkbxTimeFading;
	public GSlider sdrMediaLength, sdrTimeCycleLength, sdrCurrentTime, sdrFadeLength, sdrClusterLength, sdrGPSTrackSpeed;
	public GToggleGroup tgTimeMode;	
	public GOption optClusterTimeMode, optFieldTimeMode; //, optMediaTimeMode;
	public GLabel lblCommand2;

	/* Media Window */
	int mediaWindowHeight, mediaWindowWidth;
	public GLabel lblGraphics, lblGraphicsModes, lblOutput, lblZoom;
	public GOption optTimeline, optGPSTrack, optMemory; 
	public GToggleGroup tgFollow;	
	public boolean subjectDistanceUpBtnDown = false;
	public boolean subjectDistanceDownBtnDown = false;

	public GCheckbox chkbxSelectionMode, chkbxMultiSelection, chkbxSegmentSelection, chkbxShowMetadata; 	/* Media Window Options */
	public GCheckbox chkbxShowModel, chkbxMediaToCluster, chkbxCaptureToMedia, chkbxCaptureToCluster;
	public GCheckbox chkbxHideImages, chkbxHideVideos, chkbxHidePanoramas, chkbxHideSounds;
	public GCheckbox chkbxAlphaMode, chkbxAngleFading, chkbxAngleThinning;
	public GCheckbox chkbxDisplayTerrain, chkbxOrientationMode, chkbxDomeView;
	public GCheckbox chkbxBlurMasks;

	public GSlider sdrAlpha, sdrBrightness, sdrFarClipping, sdrVisibleAngle, sdrModelFarClipping, 		/* Media Window Sliders */
				   sdrAltitudeFactor, sdrVideoVolume, sdrSoundVolume, sdrHearingDistance; 

	/* Media Window Buttons */
	private GButton btnSubjectDistanceUp, btnSubjectDistanceDown, btnSubjectDistanceReset;	
	public GButton btnSelectFront, btnDeselectFront;
	public GButton btnSelectPanorama, btnDeselectPanorama;
	public GButton btnDeselectAll, btnViewSelected, btnStitchPanorama;
	
	private GLabel lblAlpha, lblBrightness, lblFarClipping, lblModelFarClipping, lblVisibleAngle;		/* Media Window Labels */
	private GLabel lblSubjectDistance;
	
	private GLabel lblHideMedia, lblExportingMedia;
	public GLabel lblSound, lblVideoVolume, lblSoundVolume, lblHearingDistance;					
	public GLabel lblSelection, lblSelectionOptions;					
	private GLabel lblModel, lblAltitudeFactor, lblSelect, lblDeselect;						
//	public GLabel lblShift2;
	private GButton btnMediaWindowExit, btnMediaWindowClose;		

	private int mediaWindowLineBreakY_1, mediaWindowLineBreakY_2, mediaWindowLineBreakY_3, mediaWindowLineBreakY_4;

	
	/* Time Window */
//	public GButton btnTimeView;		
	public GButton btnTimelineReverse, btnTimelineForward;		
	public GButton btnTimelineZoomIn, btnTimelineZoomOut;		
	public GButton btnTimelineZoomToField, btnTimelineZoomToSelected, btnTimelineZoomToFull;		
	public GLabel lblTimelineZoomTo, lblTimelineScroll, lblZoomTimeline;					
//	public GLabel lblShift3;
	private GButton btnTimeWindowExit, btnTimeWindowClose;		
	int timeWindowHeight;

	/* Preferences Window */
	public GOption optLibraryViewWorldMode, optLibraryViewFieldMode, optLibraryViewClusterMode;
	public GToggleGroup tgLibraryViewMode;	
	public GButton btnLibraryView, btnPreviousCluster, btnNextCluster, btnCurrentCluster;
	public GLabel lblViewerStats, lblWorldStats, lblSelectionPreferences, lblGeneral, lblPathNavigationPreferences, lblViewPreferences, lblModelPreferences;					
	int preferencesWindowHeight;
	int prefsWindowViewerTextYOffset, prefsWindowWorldTextYOffset;
	private GButton btnPreferencesWindowExit, btnPreferencesWindowClose;		

	/* Help Window */
//	private GButton btnAboutHelp, btnImportHelp, btnCloseHelp;
	public int helpAboutText = 0;		// Whether showing  0: About Text  1: Importing Files Help Text, or 2: Keyboard Shortcuts
	int helpWindowHeight;
//	private GButton btnHelpWindowExit, btnHelpWindowClose;		

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
	public MV_Window( WMV_World parent, MV_Display newDisplay )
	{
		world = parent;
		display = newDisplay;
		utilities = new WMV_Utilities();
		
		mainMenuHeight = shortWindowHeight + 95;
		startupWindowHeight = shortWindowHeight / 2;
		createLibraryWindowHeight = shortWindowHeight + 60;
		listItemWindowHeight = shortWindowHeight;			// -- Update this
		textEntryWindowHeight = 120;
		
		navigationWindowHeight = mediumWindowHeight + 115;		
		navigationWindowWidth = windowWidth;
		mediaWindowHeight = mediumWindowHeight + 148;
		mediaWindowWidth = windowWidth;
		timeWindowHeight = shortWindowHeight + 15;
		preferencesWindowHeight = mediumWindowHeight + 130;
		helpWindowHeight = mediumWindowHeight + 100;
	}

	/**
	 * Setup Main Menu window
	 */
	public void setupMainMenu(boolean open)
	{
		int leftEdge = world.mv.displayWidth / 2 - windowWidth / 2;
		int topEdge = world.mv.displayHeight / 2 - mainMenuHeight / 2;
		
		if(mainMenuX > -1 && mainMenuY > -1)
		{
			leftEdge = mainMenuX;
			topEdge = mainMenuY;
		}

		mainMenu = GWindow.getWindow(world.mv, "Main Menu", leftEdge, topEdge, windowWidth, mainMenuHeight, PApplet.JAVA2D);
		mainMenu.addData(new ML_WinData());
		mainMenu.addDrawHandler(this, "mvWindowDraw");
		mainMenu.addMouseHandler(this, "mvWindowMouse");
		mainMenu.addKeyHandler(world.mv, "mvWindowKey");
//		mainMenu.setActionOnClose(GWindow.KEEP_OPEN);
		
		world.mv.delay(delayAmount);

		int x = 0, y = iTopMargin;
		lblViewMode = new GLabel(mainMenu, x, y, mainMenu.width, iMediumBoxHeight, "View");
		lblViewMode.setLocalColorScheme(G4P.SCHEME_10);
		lblViewMode.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblViewMode.setTextAlign(GAlign.CENTER, null);
//		lblViewMode.setTextBold();

		x = 25;
		y += iLargeBoxHeight;
		optWorldView = new GOption(mainMenu, x, y, 145, iVerySmallBoxHeight, "Environment (1)");
		optWorldView.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		optWorldView.setLocalColorScheme(G4P.SCHEME_10);
		optWorldView.tag = "SceneView";
		optMapView = new GOption(mainMenu, x += 160, y, 80, iVerySmallBoxHeight, "Map (2)");
		optMapView.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		optMapView.setLocalColorScheme(G4P.SCHEME_10);
		optMapView.tag = "MapView";
		
//		x = iLeftMargin;
//		y += iSmallBoxHeight * 0.5f;
//		lblViewMode = new GLabel(mainMenu, x, y, 65, iMediumBoxHeight);						/* Display Mode Label */
//		lblViewMode.setText("View:");
//		lblViewMode.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
//		lblViewMode.setLocalColorScheme(G4P.SCHEME_10);
		
		x = 25;
		y += iMediumBoxHeight;
		optTimelineView = new GOption(mainMenu, x, y, 125, iVerySmallBoxHeight, "Timeline (3)");
		optTimelineView.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		optTimelineView.setLocalColorScheme(G4P.SCHEME_10);
		optTimelineView.tag = "TimelineView";
		optLibraryView = new GOption(mainMenu, x += 160, y, 115, iVerySmallBoxHeight, "Library (4)");
		optLibraryView.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		optLibraryView.setLocalColorScheme(G4P.SCHEME_10);
		optLibraryView.tag = "LibraryView";

		world.mv.delay(delayAmount);

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
	
//		x = 70;
//		y += iLargeBoxHeight;
//		chkbxScreenMessagesOn = new GCheckbox(mainMenu, x, y, 220, iVerySmallBoxHeight, "Screen Messages (H)");
//		chkbxScreenMessagesOn.tag = "ScreenMessagesOn";
//		chkbxScreenMessagesOn.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize-1));
//		chkbxScreenMessagesOn.setLocalColorScheme(G4P.SCHEME_10);
//		chkbxScreenMessagesOn.setSelected(world.getSettings().screenMessagesOn);

		x = 0;
		y += iMediumBoxHeight;
		lblLibraryCommands = new GLabel(mainMenu, x, y, mainMenu.width, iSmallBoxHeight);						/* Display Mode Label */
		lblLibraryCommands.setText("Library");
		lblLibraryCommands.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblLibraryCommands.setLocalColorScheme(G4P.SCHEME_10);
		lblLibraryCommands.setTextAlign(GAlign.CENTER, null);

		x = 85;
		y += iMediumBoxHeight;
		btnSaveLibrary = new GButton(mainMenu, x, y, 140, iVerySmallBoxHeight, "Save Library  ⇧S");
		btnSaveLibrary.tag = "SaveWorld";
		btnSaveLibrary.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		y += iSmallBoxHeight;
		btnSaveField = new GButton(mainMenu, x, y, 140, iVerySmallBoxHeight, "Save Field  /");
		btnSaveField.tag = "SaveField";
		btnSaveField.setLocalColorScheme(G4P.CYAN_SCHEME);

		world.mv.delay(delayAmount);

		if(world.mv.display.getDisplayView() != 0)
		{
			btnSaveLibrary.setEnabled(false);
			btnSaveField.setEnabled(false);
		}
		
		y += iSmallBoxHeight;
		btnRestart = new GButton(mainMenu, x, y, 140, iVerySmallBoxHeight, "Close Library  ⇧R");
		btnRestart.tag = "CloseLibrary";
		btnRestart.setLocalColorScheme(G4P.RED_SCHEME);

		x = 0;
		y += iMediumBoxHeight;
		lblOpenMenu = new GLabel(mainMenu, x, y, mainMenu.width, iSmallBoxHeight);						/* Display Mode Label */
		lblOpenMenu.setText("Menus");
		lblOpenMenu.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblOpenMenu.setLocalColorScheme(G4P.SCHEME_10);
		lblOpenMenu.setTextAlign(GAlign.CENTER, null);

		world.mv.delay(delayAmount / 2);

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
		btnPreferencesWindow = new GButton(mainMenu, x, y, 120, iVerySmallBoxHeight, "Preferences  ⇧4");
		btnPreferencesWindow.tag = "OpenPreferencesWindow";
		btnPreferencesWindow.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 105;
		y += iMediumBoxHeight;
		btnQuit = new GButton(mainMenu, x, y, 100, iVerySmallBoxHeight, "Quit  ⇧Q");
		btnQuit.tag = "Quit";
		btnQuit.setLocalColorScheme(G4P.RED_SCHEME);

//		y = mainMenuHeight - iBottomTextY * 2;
//		btnHelpWindow = new GButton(mainMenu, windowWidth - 30 - iLeftMargin, y, 30, 30, "?");
//		btnHelpWindow.tag = "OpenHelpWindow";
//		btnHelpWindow.setFont(new Font("Monospaced", Font.BOLD, iLargeTextSize));
//		btnHelpWindow.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		x = 0;
		y = mainMenuHeight - iBottomTextY;
		lblSpaceBar = new GLabel(mainMenu, x, y, mainMenu.width, iVerySmallBoxHeight);						/* Display Mode Label */
		lblSpaceBar.setText("Press SPACEBAR to show / hide");
		lblSpaceBar.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		lblSpaceBar.setLocalColorScheme(G4P.SCHEME_10);
		lblSpaceBar.setTextAlign(GAlign.CENTER, null);
		
		setupMainMenu = true;
		if(open) showMainMenu();
		world.mv.setAppIcon = true;
	}

	/**
	 * Setup the Navigation Window
	 */
	public void setupNavigationWindow(boolean open)
	{
		if(world.getFields().size() == 1) 
			navigationWindowHeight = mediumWindowHeight + 115;							// Single field, fewer buttons
		else
			navigationWindowHeight = mediumWindowHeight + 155;
		
		int leftEdge = world.mv.displayWidth / 2 - windowWidth / 2;
		int topEdge = world.mv.displayHeight / 2 - navigationWindowHeight / 2;
		
		if(navigationWindowX > -1 && navigationWindowY > -1)
		{
			leftEdge = navigationWindowX;
			topEdge = navigationWindowY;
		}

		navigationWindow = GWindow.getWindow(world.mv, "Navigation", leftEdge, topEdge, navigationWindowWidth, navigationWindowHeight, PApplet.JAVA2D);
		navigationWindow.setVisible(true);

		navigationWindow.addData(new ML_WinData());
		navigationWindow.addDrawHandler(this, "navigationWindowDraw");
		navigationWindow.addMouseHandler(this, "navigationWindowMouse");
		navigationWindow.addKeyHandler(world.mv, "navigationWindowKey");
//		navigationWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		world.mv.delay(delayAmount);

		int x = iLeftMargin + 2, y = iTopMargin;

		/* Moving */
		x = 90;
		y += iMediumBoxHeight * 0.25f;

		btnMoveUp = new GButton(navigationWindow, x, y, 65, iVerySmallBoxHeight, "Up (e)");
		btnMoveUp.tag = "MoveUp";
		btnMoveUp.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnMoveUp.fireAllEvents(true);

		x = 165;
		btnMoveForward = new GButton(navigationWindow, x, y, 70, iVerySmallBoxHeight, "Ahead (w)");
		btnMoveForward.tag = "MoveForward";
		btnMoveForward.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnMoveForward.fireAllEvents(true);
		
		x = 55;
		y += iSmallBoxHeight;
		btnMoveLeft = new GButton(navigationWindow, x, y, 65, iVerySmallBoxHeight, "Left (a)");
		btnMoveLeft.tag = "MoveLeft";
		btnMoveLeft.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnMoveLeft.fireAllEvents(true);

		x = 0;
		lblMove = new GLabel(navigationWindow, x, y-3, navigationWindow.width, iSmallBoxHeight, "Move");
		lblMove.setLocalColorScheme(G4P.SCHEME_10);
		lblMove.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblMove.setTextAlign(GAlign.CENTER, null);
//		lblMove.setTextBold();

		x = 197;
		btnMoveRight = new GButton(navigationWindow, x, y, 70, iVerySmallBoxHeight, "Right (d)");
		btnMoveRight.tag = "MoveRight";
		btnMoveRight.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnMoveRight.fireAllEvents(true);
		
		x = 90;
		y += iSmallBoxHeight;
		btnMoveDown = new GButton(navigationWindow, x, y, 65, iVerySmallBoxHeight, "Down (c)");
		btnMoveDown.tag = "MoveDown";
		btnMoveDown.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnMoveDown.fireAllEvents(true);

		x = 165;
		btnMoveBackward = new GButton(navigationWindow, x, y, 70, iVerySmallBoxHeight, "Back (s)");
		btnMoveBackward.tag = "MoveBackward";
		btnMoveBackward.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnMoveBackward.fireAllEvents(true);

		/* Zooming */
		x = 70;
		y += iLargeBoxHeight;
		navigationWindowLineBreakY_2 = y;
		y+=10;
		btnZoomOut = new GButton(navigationWindow, x, y, 50, iVerySmallBoxHeight, "Out (z)");
		btnZoomOut.tag = "ZoomOut";
		btnZoomOut.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnZoomOut.fireAllEvents(true);

		x = 0;
		lblZoom = new GLabel(navigationWindow, x, y, navigationWindow.width, iVerySmallBoxHeight, "Zoom");
		lblZoom.setLocalColorScheme(G4P.SCHEME_10);
		lblZoom.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblZoom.setTextAlign(GAlign.CENTER, null);
//		lblZoom.setTextBold();

		world.mv.delay(delayAmount / 2);

		x = 195;
		btnZoomIn = new GButton(navigationWindow, x, y, 50, iVerySmallBoxHeight, "In (q)");
		btnZoomIn.tag = "ZoomIn";
		btnZoomIn.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnZoomIn.fireAllEvents(true);

		world.mv.delay(delayAmount / 2);

		/* Looking */
		x = 125;
		y += iMediumBoxHeight;
		navigationWindowLineBreakY_3 = y;
		y+=10;
		btnLookUp = new GButton(navigationWindow, x, y, 65, iVerySmallBoxHeight, "Up");
		btnLookUp.tag = "LookUp";
		btnLookUp.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnLookUp.fireAllEvents(true);
		
		x = 55;
		y += iSmallBoxHeight;
		btnLookLeft = new GButton(navigationWindow, x, y, 65, iVerySmallBoxHeight, "Left");
		btnLookLeft.tag = "LookLeft";
		btnLookLeft.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnLookLeft.fireAllEvents(true);

		x = 0;
		lblLook = new GLabel(navigationWindow, x, y-3, navigationWindow.width, iSmallBoxHeight, "Look");
		lblLook.setLocalColorScheme(G4P.SCHEME_10);
		lblLook.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblLook.setTextAlign(GAlign.CENTER, null);
//		lblLook.setTextBold();

		x = 197;
		btnLookRight = new GButton(navigationWindow, x, y, 70, iVerySmallBoxHeight, "Right");
		btnLookRight.tag = "LookRight";
		btnLookRight.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnLookRight.fireAllEvents(true);

		x = 125;
		y += iSmallBoxHeight;
		btnLookDown = new GButton(navigationWindow, x, y, 65, iVerySmallBoxHeight, "Down");
		btnLookDown.tag = "LookDown";
		btnLookDown.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnLookDown.fireAllEvents(true);

//		x = 0;
		y += iLargeBoxHeight;
		navigationWindowLineBreakY_4 = y;
		y += 10;

//		x = 200;
//		lblAutoNavigation = new GLabel(navigationWindow, x, y, 65, iSmallBoxHeight, "To:");
//		lblAutoNavigation.setLocalColorScheme(G4P.SCHEME_10);
//		lblAutoNavigation.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize-1));
//		lblAutoNavigation.setTextAlign(GAlign.CENTER, null);
//		lblAutoNavigation.setTextBold();
		
		world.mv.delay(delayAmount);

		x = 55;
		optMove = new GOption(navigationWindow, x, y, 56, iSmallBoxHeight, "Move");
		optMove.setLocalColorScheme(G4P.SCHEME_10);
		optMove.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize-1));
		optMove.tag = "NavigationMove";

		x = 120;
		optTeleport = new GOption(navigationWindow, x, y, 135, iSmallBoxHeight, "Teleport to:");
		optTeleport.setLocalColorScheme(G4P.SCHEME_10);
		optTeleport.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize-1));
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

		world.mv.delay(delayAmount);

		x = 80;
		y += iMediumBoxHeight;
		btnMoveToLastCluster = new GButton(navigationWindow, x, y, 150, iVerySmallBoxHeight, "Last Location (l)");
		btnMoveToLastCluster.tag = "LastCluster";
		btnMoveToLastCluster.setLocalColorScheme(G4P.CYAN_SCHEME);

		y += iMediumBoxHeight;
		btnJumpToRandomCluster = new GButton(navigationWindow, x, y, 150, iVerySmallBoxHeight, "Random Location (j)");
		btnJumpToRandomCluster.tag = "RandomCluster";
		btnJumpToRandomCluster.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 0;
		y += iLargeBoxHeight;
		lblTimeNavigation = new GLabel(navigationWindow, x, y, navigationWindow.width, iVerySmallBoxHeight, "Time");
		lblTimeNavigation.setLocalColorScheme(G4P.SCHEME_10);
		lblTimeNavigation.setTextAlign(GAlign.CENTER, null);
		lblTimeNavigation.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));

		x = 25;
		btnPreviousTimeSegment = new GButton(navigationWindow, x, y, 95, iVerySmallBoxHeight, "Previous (b)");
		btnPreviousTimeSegment.tag = "PreviousTime";
		btnPreviousTimeSegment.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 195;
		btnNextTimeSegment = new GButton(navigationWindow, x, y, 75, iVerySmallBoxHeight, "Next (n)");
		btnNextTimeSegment.tag = "NextTime";
		btnNextTimeSegment.setLocalColorScheme(G4P.CYAN_SCHEME);

		world.mv.delay(delayAmount / 2);

		x = 0;
		y += iLargeBoxHeight;
		lblNearby = new GLabel(navigationWindow, x, y, navigationWindow.width, iVerySmallBoxHeight, "Nearby");
		lblNearby.setLocalColorScheme(G4P.SCHEME_10);
		lblNearby.setTextAlign(GAlign.CENTER, null);
		lblNearby.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		
		x = 30;
		y += iMediumBoxHeight;
		btnMoveToNearestImage = new GButton(navigationWindow, x, y, 80, iVerySmallBoxHeight, "Image (i)");
		btnMoveToNearestImage.tag = "NearestImage";
		btnMoveToNearestImage.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		x = 205;
		btnMoveToNearestPanorama = new GButton(navigationWindow, x, y, 90, iVerySmallBoxHeight, "Pano (p)");
		btnMoveToNearestPanorama.tag = "NearestPanorama";
		btnMoveToNearestPanorama.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		x = 120;
		btnMoveToNearestCluster = new GButton(navigationWindow, x, y+iMediumBoxHeight * 0.5f, 75, iVerySmallBoxHeight, "Any (m)");
		btnMoveToNearestCluster.tag = "NearestCluster";
		btnMoveToNearestCluster.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 30;
		y += iMediumBoxHeight;
		btnMoveToNearestVideo = new GButton(navigationWindow, x, y, 80, iVerySmallBoxHeight, "Video (v)");
		btnMoveToNearestVideo.tag = "NearestVideo";
		btnMoveToNearestVideo.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 205;
		btnMoveToNearestSound = new GButton(navigationWindow, x, y, 90, iVerySmallBoxHeight, "Sound (u)");
		btnMoveToNearestSound.tag = "NearestSound";
		btnMoveToNearestSound.setLocalColorScheme(G4P.CYAN_SCHEME);

		world.mv.delay(delayAmount / 2);
		
//		x = 80;
//		y += iMediumBoxHeight;
//		btnMoveToLastCluster = new GButton(navigationWindow, x, y, 150, iVerySmallBoxHeight, "Last Location (l)");
//		btnMoveToLastCluster.tag = "LastCluster";
//		btnMoveToLastCluster.setLocalColorScheme(G4P.CYAN_SCHEME);
//
//		y += iMediumBoxHeight;
//		btnJumpToRandomCluster = new GButton(navigationWindow, x, y, 150, iVerySmallBoxHeight, "Random Location (j)");
//		btnJumpToRandomCluster.tag = "RandomCluster";
//		btnJumpToRandomCluster.setLocalColorScheme(G4P.CYAN_SCHEME);

		if(world.getFields() != null)
		{
			if(world.getFieldCount() > 1)
			{
				x = 40;
				y += iMediumBoxHeight;
				btnGoToPreviousField = new GButton(navigationWindow, x, y, 120, iVerySmallBoxHeight, "Previous Field  ⇧[");
				btnGoToPreviousField.tag = "PreviousField";
				btnGoToPreviousField.setLocalColorScheme(G4P.CYAN_SCHEME);

				btnGoToNextField = new GButton(navigationWindow, x+=125, y, 100, iVerySmallBoxHeight, "Next Field  ⇧]");
				btnGoToNextField.tag = "NextField";
				btnGoToNextField.setLocalColorScheme(G4P.CYAN_SCHEME);
				
				x = 85;
				y += iMediumBoxHeight;
				btnSelectField = new GButton(navigationWindow, x, y, 130, iVerySmallBoxHeight, "Select Field  ⇧C");
				btnSelectField.tag = "ChooseField";
				btnSelectField.setLocalColorScheme(G4P.CYAN_SCHEME);
			}
		}
		
		x = 80;
		y += iLargeBoxHeight;
		btnStopViewer = new GButton(navigationWindow, x, y, 150, iVerySmallBoxHeight + 2, "Stop (.)");
		btnStopViewer.tag = "StopViewer";
		btnStopViewer.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		btnStopViewer.setLocalColorScheme(G4P.RED_SCHEME);

		world.mv.delay(delayAmount / 2);

		y += 45;
		x = 0;
		navigationWindowLineBreakY_5 = y - 10;

		lblPathNavigation = new GLabel(navigationWindow, x, y, navigationWindow.width, iVerySmallBoxHeight, "Follow Path");
		lblPathNavigation.setLocalColorScheme(G4P.SCHEME_10);
		lblPathNavigation.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblPathNavigation.setTextAlign(GAlign.CENTER, null);
		
		world.mv.delay(delayAmount);

		x = 95;
		if(compressTallWindows) x += windowWidth;
		y += iMediumBoxHeight;
		chkbxPathFollowing = new GCheckbox(navigationWindow, x, y, 135, iVerySmallBoxHeight, "On / Off (>)");
		chkbxPathFollowing.tag = "PathFollowing";
		chkbxPathFollowing.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		chkbxPathFollowing.setLocalColorScheme(G4P.SCHEME_10);
		chkbxPathFollowing.setSelected(world.viewer.isFollowing());
		if(world.viewer.getPathNavigationMode() == 1)
			chkbxPathFollowing.setEnabled(world.viewer.getSelectedGPSTrackID() != -1);
		
		x = 25;
		if(compressTallWindows) x += windowWidth;
		y += iSmallBoxHeight;
		optGPSTrack = new GOption(navigationWindow, x, y, 115, iVerySmallBoxHeight, "GPS Track");
		optGPSTrack.setLocalColorScheme(G4P.SCHEME_10);
		optGPSTrack.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		optGPSTrack.tag = "FollowGPSTrack";
		
		world.mv.delay(delayAmount / 2);
		
		x = 135;
		if(compressTallWindows) x += windowWidth;
		lblNoGPSTracks = new GLabel(navigationWindow, x, y, 85, iVerySmallBoxHeight, "No Tracks");
		lblNoGPSTracks.setLocalColorScheme(G4P.SCHEME_10);
		lblNoGPSTracks.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		btnChooseGPSTrack = new GButton(navigationWindow, x, y, 75, iVerySmallBoxHeight, "Select");
		btnChooseGPSTrack.tag = "ChooseGPSTrack";
		btnChooseGPSTrack.setLocalColorScheme(G4P.CYAN_SCHEME);
	
		x += 80;
		y -= iMediumBoxHeight;
		sdrGPSTrackSpeed = new GSlider(navigationWindow, x, y, 80, 80, 20);
		sdrGPSTrackSpeed.setLocalColorScheme(G4P.GREEN_SCHEME);
		sdrGPSTrackSpeed.setLimits(world.viewer.getSettings().gpsTrackTransitionSpeedFactor, 0.1f, 2.f);
		sdrGPSTrackSpeed.setTextOrientation(G4P.ORIENT_TRACK);
		sdrGPSTrackSpeed.setEasing(0);
		sdrGPSTrackSpeed.setShowValue(true);
		sdrGPSTrackSpeed.tag = "GPSTrackSpeed";

		x = 235;
		y += iMediumBoxHeight * 2;
		lblGPSTrackSpeed = new GLabel(navigationWindow, x, y - 8, navigationWindow.width, iVerySmallBoxHeight, "Speed");
		lblGPSTrackSpeed.setLocalColorScheme(G4P.SCHEME_10);
		
		boolean noGPSTracks = world.getCurrentField().getGPSTracks() == null;
		if(!noGPSTracks) noGPSTracks = world.getCurrentField().getGPSTracks().size() == 0;
		if(noGPSTracks) 
		{
			btnChooseGPSTrack.setEnabled(false);
			btnChooseGPSTrack.setVisible(false);

			sdrGPSTrackSpeed.setEnabled(false);
			sdrGPSTrackSpeed.setVisible(false);
			
			lblGPSTrackSpeed.setVisible(false);
		}

		x = 25;
//		y += iMediumBoxHeight;
		if(compressTallWindows) x += windowWidth;
		optTimeline = new GOption(navigationWindow, x, y, 115, iVerySmallBoxHeight, "Timeline");
		optTimeline.setLocalColorScheme(G4P.SCHEME_10);
		optTimeline.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		optTimeline.tag = "FollowTimeline";

		x = 25;
		if(compressTallWindows) x += windowWidth;
		y += iMediumBoxHeight;
		optMemory = new GOption(navigationWindow, x, y, 110, iVerySmallBoxHeight, "Memory");
		optMemory.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		optMemory.setLocalColorScheme(G4P.SCHEME_10);
		optMemory.tag = "FollowMemory";
		
		x = 135;
		if(compressTallWindows) x += windowWidth;
		btnSaveLocation = new GButton(navigationWindow, x, y, 65, iVerySmallBoxHeight, "Save (`)");
		btnSaveLocation.tag = "SaveLocation";
		btnSaveLocation.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		x += 75;
		if(compressTallWindows) x += windowWidth;
		btnClearMemory = new GButton(navigationWindow, x-3, y, 70, iVerySmallBoxHeight, "Clear (y)");
		btnClearMemory.tag = "ClearMemory";
		btnClearMemory.setLocalColorScheme(G4P.RED_SCHEME);

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

//		x = navigationWindowWidth - 150;
		x = navigationWindowWidth / 2 - 65;
		y = navigationWindowHeight - iBottomTextY;
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
		if(open) showNavigationWindow();
		world.mv.setAppIcon = true;
	}

	/**
	 * Setup the Media Window
	 */
	public void setupMediaWindow(boolean open)
	{
		int leftEdge = world.mv.displayWidth / 2 - windowWidth / 2;
		int topEdge = world.mv.displayHeight / 2 - mediaWindowHeight / 2;

		if(mediaWindowX > -1 && mediaWindowY > -1)
		{
			leftEdge = mediaWindowX;
			topEdge = mediaWindowY;
		}

		mediaWindow = GWindow.getWindow(world.mv, "Media", leftEdge, topEdge, mediaWindowWidth, mediaWindowHeight, PApplet.JAVA2D);
		mediaWindow.setVisible(true);

		mediaWindow.addData(new ML_WinData());
		mediaWindow.addDrawHandler(this, "mediaWindowDraw");
		mediaWindow.addMouseHandler(this, "mediaWindowMouse");
		mediaWindow.addKeyHandler(world.mv, "mediaWindowKey");
//		mediaWindow.setActionOnClose(GWindow.KEEP_OPEN);
	
		int x = 0, y = iTopMargin;
		world.mv.delay(delayAmount * 2);
		
//		y += 10;
		
		x = 60;
//		y += iVeryLargeBoxHeight;
//		mediaWindowLineBreakY_3 = y - 4;
		lblSelection = new GLabel(mediaWindow, x, y, 125, iMediumBoxHeight, "Select Media");
		lblSelection.setLocalColorScheme(G4P.SCHEME_10);
		lblSelection.setTextAlign(GAlign.CENTER, null);
		lblSelection.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));

		x = 195;
		chkbxSelectionMode = new GCheckbox(mediaWindow, x, y+4, 45, iVerySmallBoxHeight, "(A)");
		chkbxSelectionMode.tag = "EnableSelection";
		chkbxSelectionMode.setLocalColorScheme(G4P.SCHEME_10);
		chkbxSelectionMode.setFont(new Font("Monospaced", Font.BOLD, iSmallTextSize));
		chkbxSelectionMode.setSelected(world.viewer.getSettings().selection);
		
		x = iLeftMargin;
		y += iLargeBoxHeight + 3;
		lblSelect = new GLabel(mediaWindow, x, y-1, 80, iSmallBoxHeight, "Select");
		lblSelect.setLocalColorScheme(G4P.SCHEME_10);
		lblSelect.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
//		lblSelect.setTextBold();

		x = 90;
		btnSelectFront = new GButton(mediaWindow, x, y, 105, iVerySmallBoxHeight, "Rectangular (x)");
		btnSelectFront.tag = "SelectFront";
		btnSelectFront.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		x += 113;
		btnSelectPanorama = new GButton(mediaWindow, x, y, 95, iVerySmallBoxHeight, "Panoramic (k)");
		btnSelectPanorama.tag = "SelectPanorama";
		btnSelectPanorama.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = iLeftMargin;
//		if(compressTallWindows) x += windowWidth;
		y += iLargeBoxHeight;
		lblDeselect = new GLabel(mediaWindow, x, y+2, 85, iSmallBoxHeight+iLargeBoxHeight*0.5f, "Deselect");
		lblDeselect.setLocalColorScheme(G4P.SCHEME_10);
		lblDeselect.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
//		lblDeselect.setTextBold();

		world.mv.delay(delayAmount / 2);

		x = 90;
		btnDeselectFront = new GButton(mediaWindow, x, y, 105, iVerySmallBoxHeight, "Rectangular (X)");
		btnDeselectFront.tag = "DeselectFront";
		btnDeselectFront.setLocalColorScheme(G4P.RED_SCHEME);

		x += 113;
		btnDeselectPanorama = new GButton(mediaWindow, x, y, 95, iVerySmallBoxHeight, "Panoramic (K)");
		btnDeselectPanorama.tag = "DeselectPanorama";
		btnDeselectPanorama.setLocalColorScheme(G4P.RED_SCHEME);

		x = 90;
		y += iLargeBoxHeight;
		btnDeselectAll = new GButton(mediaWindow, x, y-1, 105, iVerySmallBoxHeight, "All (OPT x)");
		btnDeselectAll.tag = "DeselectAll";
		btnDeselectAll.setLocalColorScheme(G4P.RED_SCHEME);
		
		x = 40;
		y += iLargeBoxHeight;
		btnViewSelected = new GButton(mediaWindow, x, y, 100, iVerySmallBoxHeight, "View (⏎)");
		btnViewSelected.tag = "ViewSelected";
		btnViewSelected.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		btnViewSelected.setLocalColorScheme(G4P.CYAN_SCHEME);

		x += 110;
		btnExportMedia = new GButton(mediaWindow, x, y, 115, iVerySmallBoxHeight, "Export (o)");
		btnExportMedia.tag = "ExportMedia";
		btnExportMedia.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		btnExportMedia.setLocalColorScheme(G4P.CYAN_SCHEME);

		setSelectionControlsEnabled(world.viewer.getSettings().selection);

		x = 0;
		y += 38;
		mediaWindowLineBreakY_1 = y - 7;
		lblModel = new GLabel(mediaWindow, x, y, mediaWindow.width, iSmallBoxHeight, "Model");
		lblModel.setLocalColorScheme(G4P.SCHEME_10);
		lblModel.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblModel.setTextAlign(GAlign.CENTER, null);

		world.mv.delay(delayAmount / 2);

		x = 65;
		y += iMediumBoxHeight;
		btnSubjectDistanceDown = new GButton(mediaWindow, x, y, 30, iVerySmallBoxHeight, "-");
		btnSubjectDistanceDown.tag = "SubjectDistanceUp";
		btnSubjectDistanceDown.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnSubjectDistanceDown.fireAllEvents(true);
		
		x += 42;
		lblSubjectDistance = new GLabel(mediaWindow, x, y, 110, iVerySmallBoxHeight, "Subject Distance");
		lblSubjectDistance.setLocalColorScheme(G4P.SCHEME_10);
//		if(!compressTallWindows) lblSubjectDistance.setTextAlign(GAlign.CENTER, null);
		lblSubjectDistance.setTextBold();

		world.mv.delay(delayAmount);

		btnSubjectDistanceUp = new GButton(mediaWindow, x+=115, y, 30, iVerySmallBoxHeight, "+");
		btnSubjectDistanceUp.tag = "SubjectDistanceDown";
		btnSubjectDistanceUp.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnSubjectDistanceUp.fireAllEvents(true);

		x = 105;
		y += iMediumBoxHeight;
		btnSubjectDistanceReset = new GButton(mediaWindow, x, y, 105, iVerySmallBoxHeight, "Reset (J)");
		btnSubjectDistanceReset.tag = "SubjectDistanceReset";
		btnSubjectDistanceReset.setLocalColorScheme(G4P.CYAN_SCHEME);
//		btnSubjectDistanceReset.fireAllEvents(true);

		x = 120;
		y += 10;
		world.mv.delay(delayAmount);
		sdrAltitudeFactor = new GSlider(mediaWindow, x, y, 160, 80, 20);
		sdrAltitudeFactor.setLocalColorScheme(G4P.GREEN_SCHEME);
		sdrAltitudeFactor.setLimits(world.settings.altitudeScalingFactor, 0.f, world.settings.altitudeScalingFactorMax);
		sdrAltitudeFactor.setValue(world.settings.altitudeScalingFactor);					// -- Shouldn't be needed! Calls handler
		sdrAltitudeFactor.setTextOrientation(G4P.ORIENT_TRACK);
		sdrAltitudeFactor.setEasing(0);
		sdrAltitudeFactor.setShowValue(true);
		sdrAltitudeFactor.tag = "AltitudeFactor";
		
		x = 25;
		y += 30;
		lblAltitudeFactor = new GLabel(mediaWindow, x, y, 100, iVerySmallBoxHeight, "Altitude Factor");
		lblAltitudeFactor.setLocalColorScheme(G4P.SCHEME_10);

		y += iVeryLargeBoxHeight;
		mediaWindowLineBreakY_2 = y - 4;
		lblGraphics = new GLabel(mediaWindow, x, y, mediaWindow.width, iVerySmallBoxHeight, "Graphics");
		lblGraphics.setLocalColorScheme(G4P.SCHEME_10);
		lblGraphics.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		if(!compressTallWindows) lblGraphics.setTextAlign(GAlign.CENTER, null);
//		lblGraphics.setTextBold();

		world.mv.delay(delayAmount / 2);

		x = 120;
		y += 10;
		sdrFarClipping = new GSlider(mediaWindow, x, y, 160, 80, 20);
		sdrFarClipping.setLocalColorScheme(G4P.GREEN_SCHEME);
		sdrFarClipping.setLimits(world.viewer.getSettings().getFarViewingDistance(), 2.f, 25.f);
		sdrFarClipping.setTextOrientation(G4P.ORIENT_TRACK);
		sdrFarClipping.setEasing(0);
		sdrFarClipping.setShowValue(true);
		sdrFarClipping.tag = "FarClipping";
		
		x = 25;
		y += 30;
		lblFarClipping = new GLabel(mediaWindow, x, y, 125, iVerySmallBoxHeight, "Visible Distance");
		lblFarClipping.setLocalColorScheme(G4P.SCHEME_10);
		
		world.mv.delay(delayAmount);

		x = 120;
		y += 10;
		sdrBrightness = new GSlider(mediaWindow, x, y, 160, 80, 20);
		sdrBrightness.setLocalColorScheme(G4P.GREEN_SCHEME);
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

		world.mv.delay(delayAmount);

		x = 120;
		y += 10;
		sdrAlpha = new GSlider(mediaWindow, x, y, 120, 80, 20);
		sdrAlpha.setLocalColorScheme(G4P.GREEN_SCHEME);
		sdrAlpha.setLimits(world.getState().alpha, 0.f, 255.f);
		sdrAlpha.setValue(world.getState().alpha);		
		sdrAlpha.setTextOrientation(G4P.ORIENT_TRACK);
		sdrAlpha.setEasing(0);
		sdrAlpha.setShowValue(true);
		sdrAlpha.tag = "Alpha";

		world.mv.delay(delayAmount / 2);

		x = 25;
		y += 30;
		lblAlpha= new GLabel(mediaWindow, x, y, 90, iVerySmallBoxHeight, "Transparency");
		lblAlpha.setLocalColorScheme(G4P.SCHEME_10);

		world.mv.delay(delayAmount);

		x = 250;
		chkbxAlphaMode = new GCheckbox(mediaWindow, x, y, 35, iVerySmallBoxHeight, "(;)");
		chkbxAlphaMode.tag = "AlphaMode";
		chkbxAlphaMode.setLocalColorScheme(G4P.SCHEME_10);
		chkbxAlphaMode.setSelected(world.getState().alphaMode);

		x = 25;
		y += 40;
		lblVisibleAngle = new GLabel(mediaWindow, x, y, 90, iVerySmallBoxHeight, "Angle Fading");
		lblVisibleAngle.setLocalColorScheme(G4P.SCHEME_10);
		world.mv.delay(delayAmount / 2);

		x = 120;
//		y -= iMediumBoxHeight;
		sdrVisibleAngle = new GSlider(mediaWindow, x, y-iMediumBoxHeight, 120, 80, 20);
		sdrVisibleAngle.setLocalColorScheme(G4P.GREEN_SCHEME);
		sdrVisibleAngle.setLimits(world.viewer.getVisibleAngle(), 0.1f, 0.95f);
		sdrVisibleAngle.setTextOrientation(G4P.ORIENT_TRACK);
		sdrVisibleAngle.setEasing(0);
		sdrVisibleAngle.setShowValue(true);
		sdrVisibleAngle.tag = "VisibleAngle";
		
		x = 250;
		chkbxAngleFading = new GCheckbox(mediaWindow, x, y, 145, iVerySmallBoxHeight, "(G)");
		chkbxAngleFading.tag = "AngleFading";
		chkbxAngleFading.setLocalColorScheme(G4P.SCHEME_10);
		chkbxAngleFading.setSelected(world.viewer.getSettings().angleFading);

		world.mv.delay(delayAmount / 2);

		x = 0;
		y += iVeryLargeBoxHeight;
		mediaWindowLineBreakY_3 = y - 7;
		lblSound = new GLabel(mediaWindow, x, y, mediaWindow.width, iMediumBoxHeight, "Sound");
		lblSound.setLocalColorScheme(G4P.SCHEME_10);
		lblSound.setTextAlign(GAlign.CENTER, null);
		lblSound.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));

		x = 130;
		y += 10;
		world.mv.delay(delayAmount);
		sdrVideoVolume = new GSlider(mediaWindow, x, y, 150, 80, 20);
		sdrVideoVolume.setLocalColorScheme(G4P.GREEN_SCHEME);

//		public float videoMaxVolume = 0.85f;				// Maximum video volume

		sdrVideoVolume.setLimits(world.settings.videoMaxVolume, 0.f, world.settings.videoMaxVolumeMax);
		sdrVideoVolume.setValue(world.settings.videoMaxVolume);					// -- Shouldn't be needed! Calls handler
		sdrVideoVolume.setTextOrientation(G4P.ORIENT_TRACK);
		sdrVideoVolume.setEasing(0);
		sdrVideoVolume.setShowValue(true);
		sdrVideoVolume.tag = "VideoVolumeMax";
		
		x = 25;
		y += 30;
		lblVideoVolume = new GLabel(mediaWindow, x, y, 100, iVerySmallBoxHeight, "Video Volume");
		lblVideoVolume.setLocalColorScheme(G4P.SCHEME_10);
		
		x = 130;
		y += 10;
		world.mv.delay(delayAmount);
		sdrSoundVolume = new GSlider(mediaWindow, x, y, 150, 80, 20);
		sdrSoundVolume.setLocalColorScheme(G4P.GREEN_SCHEME);

//		public float soundMaxVolume = 0.8f;				// Maximum sound volume

		sdrSoundVolume.setLimits(world.settings.soundMaxVolume, 0.f, world.settings.soundMaxVolumeMax);
		sdrSoundVolume.setValue(world.settings.soundMaxVolume);					// -- Shouldn't be needed! Calls handler
		sdrSoundVolume.setTextOrientation(G4P.ORIENT_TRACK);
		sdrSoundVolume.setEasing(0);
		sdrSoundVolume.setShowValue(true);
		sdrSoundVolume.tag = "SoundVolumeMax";

		x = 25;
		y += 30;
		lblSoundVolume = new GLabel(mediaWindow, x, y, 100, iVerySmallBoxHeight, "Sound Volume");
		lblSoundVolume.setLocalColorScheme(G4P.SCHEME_10);

		x = 130;
		y += 10;
		world.mv.delay(delayAmount);
		sdrHearingDistance = new GSlider(mediaWindow, x, y, 150, 80, 20);
		sdrHearingDistance.setLocalColorScheme(G4P.GREEN_SCHEME);
		sdrHearingDistance.setLimits(world.viewer.getSettings().farHearingDistance, 0.f, world.viewer.getSettings().farHearingDistanceMax);
		sdrHearingDistance.setValue(world.viewer.getSettings().farHearingDistance);				
		sdrHearingDistance.setTextOrientation(G4P.ORIENT_TRACK);
		sdrHearingDistance.setEasing(0);
		sdrHearingDistance.setShowValue(true);
		sdrHearingDistance.tag = "FarHearingDistance";
		
		x = 25;
		y += 30;
		lblHearingDistance = new GLabel(mediaWindow, x, y, 100, iVerySmallBoxHeight, "Hearing Distance");
		lblHearingDistance.setLocalColorScheme(G4P.SCHEME_10);

//		x = 80;
//		y += iVeryLargeBoxHeight;
//		mediaWindowLineBreakY_3 = y - 4;
//		lblSelection = new GLabel(mediaWindow, x, y, 95, iMediumBoxHeight, "Selection");
//		lblSelection.setLocalColorScheme(G4P.SCHEME_10);
//		lblSelection.setTextAlign(GAlign.CENTER, null);
//		lblSelection.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
//
//		x = 185;
//		chkbxSelectionMode = new GCheckbox(mediaWindow, x, y+4, 45, iVerySmallBoxHeight, "(A)");
//		chkbxSelectionMode.tag = "EnableSelection";
//		chkbxSelectionMode.setLocalColorScheme(G4P.SCHEME_10);
//		chkbxSelectionMode.setFont(new Font("Monospaced", Font.BOLD, iSmallTextSize));
//		chkbxSelectionMode.setSelected(world.viewer.getSettings().selection);
//		
//		x = iLeftMargin;
//		y += iLargeBoxHeight + 3;
//		lblSelect = new GLabel(mediaWindow, x, y-1, 80, iSmallBoxHeight, "Select");
//		lblSelect.setLocalColorScheme(G4P.SCHEME_10);
//		lblSelect.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
////		lblSelect.setTextBold();
//
//		x = 90;
//		btnSelectFront = new GButton(mediaWindow, x, y, 105, iVerySmallBoxHeight, "Rectangular (x)");
//		btnSelectFront.tag = "SelectFront";
//		btnSelectFront.setLocalColorScheme(G4P.CYAN_SCHEME);
//		
//		x += 113;
//		btnSelectPanorama = new GButton(mediaWindow, x, y, 95, iVerySmallBoxHeight, "Panoramic (k)");
//		btnSelectPanorama.tag = "SelectPanorama";
//		btnSelectPanorama.setLocalColorScheme(G4P.CYAN_SCHEME);
//
//		x = iLeftMargin;
////		if(compressTallWindows) x += windowWidth;
//		y += iLargeBoxHeight;
//		lblDeselect = new GLabel(mediaWindow, x, y+2, 85, iSmallBoxHeight+iLargeBoxHeight*0.5f, "Deselect");
//		lblDeselect.setLocalColorScheme(G4P.SCHEME_10);
//		lblDeselect.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
////		lblDeselect.setTextBold();
//
//		world.ml.delay(delayAmount / 2);
//
//		x = 90;
//		btnDeselectFront = new GButton(mediaWindow, x, y, 105, iVerySmallBoxHeight, "Rectangular (X)");
//		btnDeselectFront.tag = "DeselectFront";
//		btnDeselectFront.setLocalColorScheme(G4P.RED_SCHEME);
//
//		x += 113;
//		btnDeselectPanorama = new GButton(mediaWindow, x, y, 95, iVerySmallBoxHeight, "Panoramic (K)");
//		btnDeselectPanorama.tag = "DeselectPanorama";
//		btnDeselectPanorama.setLocalColorScheme(G4P.RED_SCHEME);
//
//		x = 90;
//		y += iLargeBoxHeight;
//		btnDeselectAll = new GButton(mediaWindow, x, y-1, 105, iVerySmallBoxHeight, "All (OPT x)");
//		btnDeselectAll.tag = "DeselectAll";
//		btnDeselectAll.setLocalColorScheme(G4P.RED_SCHEME);
//		
//		x = 40;
//		y += iLargeBoxHeight;
//		btnViewSelected = new GButton(mediaWindow, x, y, 100, iVerySmallBoxHeight, "View (⏎)");
//		btnViewSelected.tag = "ViewSelected";
//		btnViewSelected.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
//		btnViewSelected.setLocalColorScheme(G4P.CYAN_SCHEME);
//
//		x += 110;
//		btnExportMedia = new GButton(mediaWindow, x, y, 115, iVerySmallBoxHeight, "Export (o)");
//		btnExportMedia.tag = "ExportMedia";
//		btnExportMedia.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
//		btnExportMedia.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 65;
		y += iLargeBoxHeight;
		mediaWindowLineBreakY_4 = y;
		y += 10;
		btnExportScreenshot = new GButton(mediaWindow, x, y, 185, iVerySmallBoxHeight, "Save Screenshot (\\)");
		btnExportScreenshot.tag = "SaveScreenshot";
		btnExportScreenshot.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		btnExportScreenshot.setLocalColorScheme(G4P.CYAN_SCHEME);

//		setSelectionControlsEnabled(world.viewer.getSettings().selection);

//		x = 85;
//		if(compressTallWindows) x += windowWidth;
//		y += iButtonSpacingWide;
//		btnStitchPanorama = new GButton(mediaWindow, x, y, 140, iSmallBoxHeight, "Stitch Selection  (⇧\\)");
//		btnStitchPanorama.tag = "StitchPanorama";
//		btnStitchPanorama.setLocalColorScheme(G4P.CYAN_SCHEME);
//
		
		x = mediaWindowWidth / 2 - 65;
		y = mediaWindowHeight - iBottomTextY;
		btnMediaWindowExit = new GButton(mediaWindow, x, y, 130, iVerySmallBoxHeight, "Main Menu (space)");
		btnMediaWindowExit.tag = "ExitMediaWindow";
		btnMediaWindowExit.setLocalColorScheme(G4P.CYAN_SCHEME);

		setupMediaWindow = true;
		if(open) showMediaWindow();
		world.mv.setAppIcon = true;
	}
	

	/**
	 * Setup the Help Window
	 */
	public void setupTimeWindow(boolean open)
	{
		int leftEdge = world.mv.displayWidth / 2 - windowWidth / 2;
		int topEdge = world.mv.displayHeight / 2 - timeWindowHeight / 2;

		if(timeWindowX > -1 && timeWindowY > -1)
		{
			leftEdge = timeWindowX;
			topEdge = timeWindowY;
		}

		timeWindow = GWindow.getWindow(world.mv, "Time", leftEdge, topEdge, timeWindowWidth, timeWindowHeight, PApplet.JAVA2D);
		timeWindow.setVisible(true);
		timeWindow.addData(new ML_WinData());
		timeWindow.addDrawHandler(this, "timeWindowDraw");
		timeWindow.addKeyHandler(world.mv, "timelineWindowKey");
//		timeWindow.setActionOnClose(GWindow.KEEP_OPEN);

		int x = 75, y = iTopMargin + 10;
		world.mv.delay(delayAmount);

//		lblTimeWindow = new GLabel(timeWindow, x, y, timeWindow.width, iSmallBoxHeight, "Time");
//		lblTimeWindow.setLocalColorScheme(G4P.SCHEME_10);
//		lblTimeWindow.setFont(new Font("Monospaced", Font.PLAIN, iVeryLargeTextSize));
//		lblTimeWindow.setTextAlign(GAlign.CENTER, null);
//		lblTimeWindow.setTextBold();

//		x = 75;
//		y += iVeryLargeBoxHeight;
//		btnTimeView = new GButton(timeWindow, x, y, 160, iVerySmallBoxHeight, "Show Timeline (3)");
//		btnTimeView.tag = "SetTimeView";
//		btnTimeView.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
//		btnTimeView.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		x = iLeftMargin;
//		y += iMediumBoxHeight * 0.5f;
		lblTimeCycle = new GLabel(timeWindow, x, y, 85, iVerySmallBoxHeight, "Time");
		lblTimeCycle.setLocalColorScheme(G4P.SCHEME_10);
		lblTimeCycle.setFont(new Font("Monospaced", Font.BOLD, iMediumTextSize));

		x = 95;
//		y += iLargeBoxHeight;
		chkbxTimeFading = new GCheckbox(timeWindow, x, y, 155, iVerySmallBoxHeight, "On / Off (f)");
		chkbxTimeFading.tag = "TimeFading";
		chkbxTimeFading.setLocalColorScheme(G4P.SCHEME_10);
		chkbxTimeFading.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		chkbxTimeFading.setSelected(world.getState().timeFading);

		x += 160;
//		y += iMediumBoxHeight * 0.5f;
		chkbxPaused = new GCheckbox(timeWindow, x, y, 140, iVerySmallBoxHeight, "Pause (-)");
		chkbxPaused.tag = "Paused";
		chkbxPaused.setLocalColorScheme(G4P.SCHEME_10);
		chkbxPaused.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		chkbxPaused.setSelected(world.getState().paused);

		x = iLeftMargin;
		y += iLargeBoxHeight;
		lblTimeMode = new GLabel(timeWindow, x, y, 90, iVerySmallBoxHeight, "Mode");
		lblTimeMode.setLocalColorScheme(G4P.SCHEME_10);
		lblTimeMode.setFont(new Font("Monospaced", Font.BOLD, iMediumTextSize));

		x = 95;
		optClusterTimeMode = new GOption(timeWindow, x, y, 100, 20, "Location");
		optClusterTimeMode.setLocalColorScheme(G4P.SCHEME_10);
		optClusterTimeMode.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		optClusterTimeMode.tag = "ClusterTimeMode";
		
		world.mv.delay(delayAmount);

		x += 115;
		optFieldTimeMode = new GOption(timeWindow, x, y, 95, 20, "Field (=)");
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
		world.mv.delay(delayAmount);

		x = 0;
//		y += iLargeBoxHeight;
		
//		lblCurrentTime = new GLabel(timeWindow, x, y, timeWindowWidth, iVerySmallBoxHeight, "Current");
//		lblCurrentTime.setTextAlign(GAlign.CENTER, null);
//		lblCurrentTime.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
//		lblCurrentTime.setLocalColorScheme(G4P.SCHEME_10);
//		lblCurrentTime.setTextBold();

		y += iMediumBoxHeight;
		
		lblCurrentTime = new GLabel(timeWindow, x, y, timeWindowWidth, iVerySmallBoxHeight, "00:00:00");
		lblCurrentTime.setTextAlign(GAlign.CENTER, null);
		lblCurrentTime.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		lblCurrentTime.setLocalColorScheme(G4P.SCHEME_10);

		world.mv.display.updateTimeWindowCurrentTime();
		
		x = 35;
		
		sdrCurrentTime = new GSlider(timeWindow, x, y+10, timeWindowWidth - 70, 80, 24);
		sdrCurrentTime.setLocalColorScheme(G4P.CYAN_SCHEME);
//		sdrCurrentTime.setLocalColorScheme(G4P.GREEN_SCHEME);
		sdrCurrentTime.setLimits(0.f, 0.f, 1.f);
		sdrCurrentTime.setValue(world.getCurrentTime());
//		sdrCurrentTime.setValue(world.getCurrentTime() / world.getFieldTimeCycleLength());
		sdrCurrentTime.setTextOrientation(G4P.ORIENT_TRACK);
		sdrCurrentTime.setEasing(0);
		sdrCurrentTime.setShowValue(true);
		sdrCurrentTime.tag = "SetCurrentTime";

//		x = 35;
//		y += iVeryLargeBoxHeight;
//		lblCurrentTime = new GLabel(timeWindow, x, y, 100, iVerySmallBoxHeight, "Current Time");
//		lblCurrentTime.setLocalColorScheme(G4P.SCHEME_10);

		x = 140;
		y += iVeryLargeBoxHeight + 20;
		sdrMediaLength = new GSlider(timeWindow, x, y, 200, 80, 20);
		sdrMediaLength.setLocalColorScheme(G4P.GREEN_SCHEME);
		sdrMediaLength.setLimits(world.settings.staticMediaLength, 40.f, 1000.f);	// -- Update limits in realtime to match time cycle length?
		sdrMediaLength.setValue(world.settings.staticMediaLength);
		sdrMediaLength.setTextOrientation(G4P.ORIENT_TRACK);
		sdrMediaLength.setEasing(0);
		sdrMediaLength.setShowValue(true);
		sdrMediaLength.tag = "SetMediaLength";

		x = 55;
		y += 30;
		lblMediaLength = new GLabel(timeWindow, x, y, 100, iVerySmallBoxHeight, "Media Length");
		lblMediaLength.setLocalColorScheme(G4P.SCHEME_10);

		x = 140;
		y += 10;
		sdrTimeCycleLength = new GSlider(timeWindow, x, y, 200, 80, 20);
		sdrTimeCycleLength.setLocalColorScheme(G4P.GREEN_SCHEME);
		sdrTimeCycleLength.setLimits(world.getCycleLengthForCurrentTimeMode(), world.settings.minTimeCycleLength, world.settings.maxTimeCycleLength);
		sdrTimeCycleLength.setValue(world.getCycleLengthForCurrentTimeMode());
		sdrTimeCycleLength.setTextOrientation(G4P.ORIENT_TRACK);
		sdrTimeCycleLength.setEasing(0);
		sdrTimeCycleLength.setShowValue(true);
		sdrTimeCycleLength.tag = "TimeCycleLength";

		x = 55;
		y += 30;
		lblTimeCycleLength = new GLabel(timeWindow, x, y, 120, iVerySmallBoxHeight, "Cycle Length");
		lblTimeCycleLength.setLocalColorScheme(G4P.SCHEME_10);

		world.mv.delay(delayAmount / 2);
		
		x = 140;
		y += 10;
		sdrFadeLength = new GSlider(timeWindow, x, y, 200, 80, 20);
		sdrFadeLength.setLocalColorScheme(G4P.GREEN_SCHEME);
		sdrFadeLength.setLimits(world.settings.staticMediaFadeLength, world.settings.minStaticMediaFadeLength, world.settings.maxStaticMediaFadeLength);
		sdrFadeLength.setValue(world.settings.staticMediaFadeLength);
		sdrFadeLength.setTextOrientation(G4P.ORIENT_TRACK);
		sdrFadeLength.setEasing(0);
		sdrFadeLength.setShowValue(true);
		sdrFadeLength.tag = "SetFadeLength";
		
		x = 55;
		y += 30;
		lblFadeLength = new GLabel(timeWindow, x, y, 120, iVerySmallBoxHeight, "Fade Length");
		lblFadeLength.setLocalColorScheme(G4P.SCHEME_10);

		world.mv.delay(delayAmount / 2);
		
		x = 140;
		y += 10;
		sdrClusterLength = new GSlider(timeWindow, x, y, 200, 80, 20);
		sdrClusterLength.setLocalColorScheme(G4P.GREEN_SCHEME);
		sdrClusterLength.setLimits(world.settings.clusterLength, 0.f, 1.f);
		sdrClusterLength.setValue(world.settings.clusterLength);
		sdrClusterLength.setTextOrientation(G4P.ORIENT_TRACK);
		sdrClusterLength.setEasing(0);
		sdrClusterLength.setShowValue(true);
		sdrClusterLength.tag = "SetClusterLength";
		
		x = 55;
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

		x = timeWindow.width / 2 - 65;
		y = timeWindowHeight - iBottomTextY;
		btnTimeWindowExit = new GButton(timeWindow, x, y, 130, iVerySmallBoxHeight, "Main Menu (space)");
		btnTimeWindowExit.tag = "ExitTimeWindow";
		btnTimeWindowExit.setLocalColorScheme(G4P.CYAN_SCHEME);

		world.mv.delay(delayAmount);
		
		setupTimeWindow = true;
		if(open) showTimeWindow();
		world.mv.setAppIcon = true;
	}
	
	/**
	 * Setup Preferences Window
	 */
	public void setupPreferencesWindow(boolean open)
	{
		int leftEdge = world.mv.displayWidth / 2 - (windowWidth / 2);
		int topEdge = world.mv.displayHeight / 2 - preferencesWindowHeight / 2;
		
		if(preferencesWindowX > -1 && preferencesWindowY > -1)
		{
			leftEdge = preferencesWindowX;
			topEdge = preferencesWindowY;
		}

		preferencesWindow = GWindow.getWindow(world.mv, "Preferences", leftEdge, topEdge, windowWidth, preferencesWindowHeight, PApplet.JAVA2D);
		preferencesWindow.setVisible(true);
		preferencesWindow.addData(new ML_WinData());
		preferencesWindow.addDrawHandler(this, "preferencesWindowDraw");
		preferencesWindow.addKeyHandler(world.mv, "preferencesWindowKey");
//		preferencesWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		int x = 0, y = iTopMargin;
		world.mv.delay(delayAmount * 3 / 2);

		lblPrefs = new GLabel(preferencesWindow, x, y, preferencesWindow.width, iVerySmallBoxHeight, "Preferences");
		lblPrefs.setLocalColorScheme(G4P.SCHEME_10);
		lblPrefs.setTextAlign(GAlign.CENTER, null);
		lblPrefs.setFont(new Font("Monospaced", Font.PLAIN, iVeryLargeTextSize));
		lblPrefs.setTextBold();
		
		world.mv.delay(delayAmount);

		x = 0;
		y += iLargeBoxHeight;
		lblGeneral = new GLabel(preferencesWindow, x, y-3, preferencesWindow.width, iMediumBoxHeight, "General");
		lblGeneral.setLocalColorScheme(G4P.SCHEME_10);
		lblGeneral.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblGeneral.setTextAlign(GAlign.CENTER, null);
		lblGeneral.setTextBold();
		world.mv.delay(delayAmount / 2);

		x = 70;
		y += iLargeBoxHeight;
		chkbxScreenMessagesOn = new GCheckbox(preferencesWindow, x, y, 220, iVerySmallBoxHeight, "Screen Messages (H)");
		chkbxScreenMessagesOn.tag = "ScreenMessagesOn";
		chkbxScreenMessagesOn.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize-1));
		chkbxScreenMessagesOn.setLocalColorScheme(G4P.SCHEME_10);
		chkbxScreenMessagesOn.setSelected(world.getSettings().screenMessagesOn);

		x = 0;
		y += iLargeBoxHeight;
		lblPathNavigationPreferences = new GLabel(preferencesWindow, x, y-3, preferencesWindow.width, iMediumBoxHeight, "Path Navigation");
		lblPathNavigationPreferences.setLocalColorScheme(G4P.SCHEME_10);
		lblPathNavigationPreferences.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblPathNavigationPreferences.setTextAlign(GAlign.CENTER, null);
		lblPathNavigationPreferences.setTextBold();
		world.mv.delay(delayAmount / 2);

		x = 70;
		y += iLargeBoxHeight;
		chkbxFollowTeleport = new GCheckbox(preferencesWindow, x, y, 185, iVerySmallBoxHeight, "Force Teleporting (,)");
		chkbxFollowTeleport.tag = "FollowTeleport";
		chkbxFollowTeleport.setLocalColorScheme(G4P.SCHEME_10);
		chkbxFollowTeleport.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		chkbxFollowTeleport.setSelected(world.viewer.getState().followTeleport);

		y += 8;
		
		x = 65;
		sdrTeleportLength = new GSlider(preferencesWindow, x, y, 70, 80, iVerySmallBoxHeight);
		sdrTeleportLength.setLocalColorScheme(G4P.GREEN_SCHEME);
		sdrTeleportLength.setLimits(0.f, 300.f, 10.f);
		sdrTeleportLength.setValue(world.viewer.getSettings().teleportLength);
		sdrTeleportLength.setTextOrientation(G4P.ORIENT_TRACK);
//		sdrTeleportLength.setRotation(PApplet.PI/2.f);
//		sdrTeleportLength.setTextOrientation(G4P.ORIENT_LEFT);
		sdrTeleportLength.setEasing(0);
		sdrTeleportLength.setShowValue(true);
		sdrTeleportLength.tag = "TeleportLength";

		x = 215;
		sdrPathWaitLength = new GSlider(preferencesWindow, x, y, 70, 80, iVerySmallBoxHeight);
		sdrPathWaitLength.setLocalColorScheme(G4P.GREEN_SCHEME);
		sdrPathWaitLength.setLimits(0.f, 30.f, 600.f);
		sdrPathWaitLength.setValue(world.viewer.getSettings().pathWaitLength);
		sdrPathWaitLength.setTextOrientation(G4P.ORIENT_TRACK);
		sdrPathWaitLength.setEasing(0);
		sdrPathWaitLength.setShowValue(true);
		sdrPathWaitLength.tag = "PathWaitLength";
		
		x = iLeftMargin;
//		if(compressTallWindows) x += windowWidth;
		y += iMediumBoxHeight;
		lblTeleportLength = new GLabel(preferencesWindow, x, y, 80, iVerySmallBoxHeight, "Length");
		lblTeleportLength.setLocalColorScheme(G4P.SCHEME_10);
		world.mv.delay(delayAmount / 2);

		x = 155;
//		if(compressTallWindows) x += windowWidth;
		lblPathWaitLength = new GLabel(preferencesWindow, x, y, 90, iVerySmallBoxHeight, "Wait Time");
		lblPathWaitLength.setLocalColorScheme(G4P.SCHEME_10);

		x = 0;
		y += iLargeBoxHeight;
		lblViewPreferences = new GLabel(preferencesWindow, x, y-3, preferencesWindow.width, iMediumBoxHeight, "Viewing");
		lblViewPreferences.setLocalColorScheme(G4P.SCHEME_10);
		lblViewPreferences.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblViewPreferences.setTextAlign(GAlign.CENTER, null);
		lblViewPreferences.setTextBold();

		x = 55;
		y += iMediumBoxHeight;
		chkbxBlurMasks = new GCheckbox(preferencesWindow, x, y, 145, iVerySmallBoxHeight, "Fade Edges (E)");
		chkbxBlurMasks.tag = "FadeEdges";
		chkbxBlurMasks.setLocalColorScheme(G4P.SCHEME_10);
		chkbxBlurMasks.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		chkbxBlurMasks.setSelected(world.getState().useBlurMasks);
//		x = 80;
//		if(compressTallWindows) x += windowWidth;
		y += iMediumBoxHeight;
		chkbxOrientationMode = new GCheckbox(preferencesWindow, x, y, 195, iVerySmallBoxHeight, "Orientation Mode (9)");
		chkbxOrientationMode.tag = "OrientationMode";
		chkbxOrientationMode.setLocalColorScheme(G4P.SCHEME_10);
		chkbxOrientationMode.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		chkbxOrientationMode.setSelected(world.viewer.getSettings().orientationMode);

//		x = 80;
		y += iMediumBoxHeight;
		chkbxShowMetadata = new GCheckbox(preferencesWindow, x, y, 195, iVerySmallBoxHeight, "View Metadata (M)");
		chkbxShowMetadata.tag = "ViewMetadata";
		chkbxShowMetadata.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		chkbxShowMetadata.setLocalColorScheme(G4P.SCHEME_10);
		chkbxShowMetadata.setSelected(world.getState().showMetadata);

//		x = 80;
		y += iMediumBoxHeight;
		chkbxDisplayTerrain = new GCheckbox(preferencesWindow, x, y, 230, iVerySmallBoxHeight, "Terrain On / Off (BETA) (T)");
		chkbxDisplayTerrain.tag = "DisplayTerrain";
		chkbxDisplayTerrain.setLocalColorScheme(G4P.SCHEME_10);
		chkbxDisplayTerrain.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		chkbxDisplayTerrain.setSelected(world.getState().displayTerrain);

		x = 0;
		y += iLargeBoxHeight;
		lblModelPreferences = new GLabel(preferencesWindow, x, y-3, preferencesWindow.width, iMediumBoxHeight, "Model");
		lblModelPreferences.setLocalColorScheme(G4P.SCHEME_10);
		lblModelPreferences.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblModelPreferences.setTextAlign(GAlign.CENTER, null);
		lblModelPreferences.setTextBold();
		world.mv.delay(delayAmount / 2);

		x = 85;
		y += iMediumBoxHeight;
		chkbxShowModel = new GCheckbox(preferencesWindow, x, y, 140, iVerySmallBoxHeight, "Show Model (5)");
		chkbxShowModel.tag = "ShowModel";
		chkbxShowModel.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		chkbxShowModel.setLocalColorScheme(G4P.SCHEME_10);
		chkbxShowModel.setSelected(world.getState().showModel);

		x = 165;
		y += 5;
		sdrModelFarClipping = new GSlider(preferencesWindow, x, y, 125, 80, 20);
		sdrModelFarClipping.setLocalColorScheme(G4P.GREEN_SCHEME);
		sdrModelFarClipping.setLimits(world.getState().modelDistanceVisibilityFactorFar, 1.f, 36.f);
		sdrModelFarClipping.setTextOrientation(G4P.ORIENT_TRACK);
		sdrModelFarClipping.setEasing(0);
		sdrModelFarClipping.setShowValue(true);
		sdrModelFarClipping.tag = "ModelFarClipping";
		
		x = 25;
		y += iSmallBoxHeight;
		lblModelFarClipping = new GLabel(preferencesWindow, x, y, 165, iVerySmallBoxHeight, "Model Visible Distance");
		lblModelFarClipping.setLocalColorScheme(G4P.SCHEME_10);
		
		world.mv.delay(delayAmount);

		x = 70;
		y += iMediumBoxHeight;
		chkbxMediaToCluster = new GCheckbox(preferencesWindow, x, y, 215, iVerySmallBoxHeight, "View GPS Locations (6)");
		chkbxMediaToCluster.tag = "MediaToCluster";
		chkbxMediaToCluster.setLocalColorScheme(G4P.SCHEME_10);
		chkbxMediaToCluster.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
//		chkbxMediaToCluster.setEnabled(world.getState().showModel);
		chkbxMediaToCluster.setSelected(world.getState().showMediaToCluster);

		x = 0;
		y += iLargeBoxHeight;
		lblSelectionPreferences = new GLabel(preferencesWindow, x, y, preferencesWindow.width, iMediumBoxHeight, "Selection");
		lblSelectionPreferences.setLocalColorScheme(G4P.SCHEME_10);
		lblSelectionPreferences.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblSelectionPreferences.setTextAlign(GAlign.CENTER, null);
		lblSelectionPreferences.setTextBold();
		
		world.mv.delay(delayAmount / 2);

		x = 25;
		y += iMediumBoxHeight;
		chkbxMultiSelection = new GCheckbox(preferencesWindow, x, y, 155, iVerySmallBoxHeight, "Multi-Selection (OPT m)");
		chkbxMultiSelection.tag = "MultiSelection";
		chkbxMultiSelection.setLocalColorScheme(G4P.SCHEME_10);
//		chkbxMultiSelection.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize-1));
		chkbxMultiSelection.setSelected(world.viewer.getSettings().multiSelection);

		chkbxSegmentSelection = new GCheckbox(preferencesWindow, x+160, y, 135, iVerySmallBoxHeight, "Groups (OPT s)");
		chkbxSegmentSelection.tag = "SegmentSelection";
		chkbxSegmentSelection.setLocalColorScheme(G4P.SCHEME_10);
//		chkbxSegmentSelection.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize-1));
		chkbxSegmentSelection.setSelected(world.viewer.getSettings().groupSelection);

		x = 0;
		y += iMediumBoxHeight;
		lblExportingMedia = new GLabel(preferencesWindow, x, y, preferencesWindow.width, iVerySmallBoxHeight, "Exporting");
		lblExportingMedia.setLocalColorScheme(G4P.SCHEME_10);
		lblExportingMedia.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblExportingMedia.setTextAlign(GAlign.CENTER, null);
		lblExportingMedia.setTextBold();

		world.mv.delay(delayAmount / 2);

		x = 95;
		y += iMediumBoxHeight;
		btnOutputFolder = new GButton(preferencesWindow, x, y, 115, iVerySmallBoxHeight, "Set Output Folder");
		btnOutputFolder.tag = "OutputFolder";
		btnOutputFolder.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 0;
		y += iMediumBoxHeight;
		lblHideMedia = new GLabel(preferencesWindow, x, y, preferencesWindow.width, iVerySmallBoxHeight, "Hiding");
		lblHideMedia.setLocalColorScheme(G4P.SCHEME_10);
		lblHideMedia.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblHideMedia.setTextAlign(GAlign.CENTER, null);
		lblHideMedia.setTextBold();

		world.mv.delay(delayAmount / 2);

		x = 55;
		y += iMediumBoxHeight;
		chkbxHideImages = new GCheckbox(preferencesWindow, x, y, 95, iVerySmallBoxHeight, "Images (I)");
		chkbxHideImages.tag = "HideImages";
		chkbxHideImages.setLocalColorScheme(G4P.SCHEME_10);
		chkbxHideImages.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		chkbxHideImages.setSelected(world.viewer.getSettings().hideImages);
		
		chkbxHidePanoramas = new GCheckbox(preferencesWindow, x + 100, y, 115, iVerySmallBoxHeight, "Panoramas (P)");
		chkbxHidePanoramas.tag = "HidePanoramas";
		chkbxHidePanoramas.setLocalColorScheme(G4P.SCHEME_10);
		chkbxHidePanoramas.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		chkbxHidePanoramas.setSelected(world.viewer.getSettings().hidePanoramas);

		world.mv.delay(delayAmount / 2);

		y += iSmallBoxHeight;
		chkbxHideVideos = new GCheckbox(preferencesWindow, x, y, 95, iVerySmallBoxHeight, "Videos (V)");
		chkbxHideVideos.tag = "HideVideos";
		chkbxHideVideos.setLocalColorScheme(G4P.SCHEME_10);
		chkbxHideVideos.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		chkbxHideVideos.setSelected(world.viewer.getSettings().hideVideos);

		chkbxHideSounds = new GCheckbox(preferencesWindow, x += 100, y, 95, iVerySmallBoxHeight, "Sounds (U)");
		chkbxHideSounds.tag = "HideSounds";
		chkbxHideSounds.setLocalColorScheme(G4P.SCHEME_10);
		chkbxHideSounds.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		chkbxHideSounds.setSelected(world.viewer.getSettings().hideSounds);

		world.mv.delay(delayAmount / 2);

		x = preferencesWindow.width / 2 - 65;
		y = preferencesWindow.height - iBottomTextY;
		btnPreferencesWindowExit = new GButton(preferencesWindow, x, y, 130, iVerySmallBoxHeight, "Main Menu (space)");
		btnPreferencesWindowExit.tag = "ExitPreferencesWindow";
		btnPreferencesWindowExit.setLocalColorScheme(G4P.CYAN_SCHEME);

		setupPreferencesWindow = true;
		if(open) showPreferencesWindow();
		world.mv.setAppIcon = true;
		
		world.mv.delay(delayAmount / 2);
	}
	
	/**
	 * Setup the Help Window
	 */
	public void setupHelpWindow(boolean open)
	{
//		int leftEdge = world.ml.displayWidth / 2 - windowWidth * 2;
//		int topEdge = world.ml.displayHeight / 2 - helpWindowHeight / 2;
//
//		helpWindow = GWindow.getWindow(world.ml, "Help", leftEdge, topEdge, windowWidth * 4, helpWindowHeight, PApplet.JAVA2D);
//		helpWindow.setVisible(true);
//		helpWindow.addData(new ML_WinData());
//		helpWindow.addDrawHandler(this, "helpWindowDraw");
//		helpWindow.addMouseHandler(this, "helpWindowMouse");
//		helpWindow.addKeyHandler(world.ml, "helpWindowKey");
//		
//		int x = 0, y = iTopMargin;
//		world.ml.delay(delayAmount);
//
//		x = 55;
//		y = helpWindowHeight / 2 - iLargeBoxHeight - iLargeBoxHeight;
//		btnAboutHelp = new GButton(helpWindow, x, y, 100, iLargeBoxHeight, "About");
//		btnAboutHelp.tag = "AboutHelp";
//		btnAboutHelp.setFont(new Font("Monospaced", Font.BOLD, iMediumTextSize));
//		btnAboutHelp.setLocalColorScheme(G4P.CYAN_SCHEME);
//
//		x = 20;
//		y = helpWindowHeight / 2 - iLargeBoxHeight + iLargeBoxHeight;
//		btnImportHelp = new GButton(helpWindow, x, y, 170, iLargeBoxHeight, "Importing Files");
//		btnImportHelp.tag = "ImportHelp";
//		btnImportHelp.setFont(new Font("Monospaced", Font.BOLD, iMediumTextSize));
//		btnImportHelp.setLocalColorScheme(G4P.CYAN_SCHEME);
//
//		x = windowWidth * 2 - 60;
//		y = helpWindowHeight - iBottomTextY * 3;
//		btnCloseHelp = new GButton(helpWindow, x, y, 120, iVerySmallBoxHeight, "Close Window");
//		btnCloseHelp.tag = "CloseHelpWindow";
////		btnCloseHelp.setFont(new Font("Monospaced", Font.BOLD, iSmallTextSize));
//		btnCloseHelp.setLocalColorScheme(G4P.RED_SCHEME);
//
//		x = helpWindow.width - 150;
//		btnHelpWindowExit = new GButton(helpWindow, x, y, 130, iVerySmallBoxHeight, "Main Menu (space)");
//		btnHelpWindowExit.tag = "ExitHelpWindow";
//		btnHelpWindowExit.setLocalColorScheme(G4P.CYAN_SCHEME);
////		btnHelpWindowExit.setTextBold();
//
//		if(open) showHelpWindow();
//		setupHelpWindow = true;
//		world.ml.setAppIcon = true;
	}

	/**
	 * Set whether Selection Controls in Navigation Window are enabled
	 * @param enabled New Selection Controls enabled state
	 */
	public void setSelectionControlsEnabled(boolean enabled)
	{
		btnSelectFront.setEnabled(enabled);
		btnSelectPanorama.setEnabled(enabled);
		btnDeselectFront.setEnabled(enabled);
		btnDeselectPanorama.setEnabled(enabled);
		btnDeselectAll.setEnabled(enabled);
		btnViewSelected.setEnabled(enabled);
		btnExportMedia.setEnabled(enabled);
	}
	
	/**
	 * Set whether Map Controls in Navigation Window are enabled
	 * @param enabled New Map Controls enabled state
	 */
	public void setMapControlsEnabledX(boolean enabled)
	{
		if(enabled)		// Enable map controls
		{
			btnMoveForward.setEnabled(true);
			btnMoveLeft.setEnabled(true);
			btnMoveBackward.setEnabled(true);
			btnMoveRight.setEnabled(true);
			btnZoomToViewer.setEnabled(true);
//			btnZoomToSelected.setEnabled(true);
			btnZoomOutToField.setEnabled(true);
			btnZoomToWorld.setEnabled(true);
			optMapViewFieldMode.setEnabled(true);
			optMapViewWorldMode.setEnabled(true);
		}
		else			// Disable map controls
		{
			btnMoveForward.setEnabled(false);
			btnMoveLeft.setEnabled(false);
			btnMoveBackward.setEnabled(false);
			btnMoveRight.setEnabled(false);
			btnZoomToViewer.setEnabled(false);
//			btnZoomToSelected.setEnabled(false);
			btnZoomOutToField.setEnabled(false);
			btnZoomToWorld.setEnabled(false);
			optMapViewFieldMode.setEnabled(false);
			optMapViewWorldMode.setEnabled(false);
		}
	}
	
//	private void setTimeWindowControlsEnabled(boolean newState)
//	{
//		btnTimelineReverse.setEnabled(newState);
//		btnTimelineForward.setEnabled(newState);	
//		btnTimelineZoomIn.setEnabled(newState);
//		btnTimelineZoomOut.setEnabled(newState);		
//		btnTimelineZoomToField.setEnabled(newState);
//		btnTimelineZoomToSelected.setEnabled(newState);
//		btnTimelineZoomToFull.setEnabled(newState);		
//	}
//	
//	public void setLibraryViewWindowControlsEnabled(boolean newState)
//	{
//		optLibraryViewWorldMode.setEnabled(newState);
//		optLibraryViewFieldMode.setEnabled(newState);
//		optLibraryViewClusterMode.setEnabled(newState);
//		btnPreviousCluster.setEnabled(newState);
//		btnNextCluster.setEnabled(newState);
//		btnCurrentCluster.setEnabled(newState);
//	}

	/**
	 * Setup and open Library Window
	 */
	public void openStartupWindow()
	{
		int leftEdge = world.mv.displayWidth / 2 - windowWidth;
		int topEdge = world.mv.displayHeight / 2 - startupWindowHeight / 2;
		
		startupWindow = GWindow.getWindow( world.mv, " ", leftEdge, topEdge, windowWidth * 2, startupWindowHeight, PApplet.JAVA2D);
		
		startupWindow.addData(new ML_WinData());
		startupWindow.addDrawHandler(this, "startupWindowDraw");
		startupWindow.addKeyHandler(world.mv, "libraryWindowKey");
		startupWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		int x = 0, y = iTopMargin * 5 / 2;
		world.mv.delay(delayAmount);
		lblStartup = new GLabel(startupWindow, x, y, startupWindow.width, 22, "Welcome to MetaVisualizer.");
		lblStartup.setLocalColorScheme(G4P.SCHEME_10);
		lblStartup.setFont(new Font("Monospaced", Font.PLAIN, iVeryLargeTextSize));
		lblStartup.setTextAlign(GAlign.CENTER, null);

		x = 0;
		lblStartupWindowText = new GLabel(startupWindow, x, y, startupWindow.width, 22, "Opening Environment...");
		lblStartupWindowText.setLocalColorScheme(G4P.SCHEME_10);
		lblStartupWindowText.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
		lblStartupWindowText.setTextAlign(GAlign.CENTER, null);
		lblStartupWindowText.setVisible(false);

		world.mv.delay(delayAmount / 2);

		x = 55;
		y += 50;
		btnCreateLibrary = new GButton(startupWindow, x, y, 200, iMediumBoxHeight, "Create Environment");
//		btnCreateLibrary = new GButton(startupWindow, x, y, 190, iLargeBoxHeight, "Create Library");
		btnCreateLibrary.tag = "CreateLibrary";
		btnCreateLibrary.setFont(new Font("Monospaced", Font.BOLD, iMediumTextSize));
		btnCreateLibrary.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnOpenLibrary = new GButton(startupWindow, x+=220, y, 185, iMediumBoxHeight, "Open Environment");
//		btnOpenLibrary = new GButton(startupWindow, x+=220, y, 175, iLargeBoxHeight, "Open Library");
		btnOpenLibrary.tag = "OpenLibrary";
		btnOpenLibrary.setFont(new Font("Monospaced", Font.BOLD, iMediumTextSize));
		btnOpenLibrary.setLocalColorScheme(G4P.CYAN_SCHEME);
		chkbxRebuildLibrary = new GCheckbox(startupWindow, x+=195, y+5, 125, iVerySmallBoxHeight, "Rebuild");
		chkbxRebuildLibrary.tag = "RebuildLibrary";
		chkbxRebuildLibrary.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		chkbxRebuildLibrary.setLocalColorScheme(G4P.SCHEME_10);
		chkbxRebuildLibrary.setSelected(world.mv.state.rebuildLibrary);
		
//		y += 50;
//		btnLibraryHelp = new GButton(startupWindow, windowWidth * 2 - 30 - iLeftMargin, y, 30, 30, "?");
//		btnLibraryHelp.tag = "LibraryHelp";
//		btnLibraryHelp.setFont(new Font("Monospaced", Font.BOLD, iLargeTextSize));
//		btnLibraryHelp.setLocalColorScheme(G4P.CYAN_SCHEME);

		world.mv.delay(delayAmount);

		setupStartupWindow = true;
		showStartupWindow = true;
		world.mv.setAppIcon = true;
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
		int leftEdge = world.mv.displayWidth / 2 - windowWidth * 3 / 2;
		int topEdge = world.mv.displayHeight / 2 - createLibraryWindowHeight / 2;
		
		createLibraryWindow = GWindow.getWindow( world.mv, "Create Library", leftEdge, topEdge, windowWidth * 3, 
				   createLibraryWindowHeight, PApplet.JAVA2D);

		createLibraryWindow.addData(new ML_WinData());
		createLibraryWindow.addDrawHandler(this, "createLibraryWindowDraw");
		createLibraryWindow.addMouseHandler(this, "createLibraryWindowMouse");
		createLibraryWindow.addKeyHandler(world.mv, "importWindowKey");
		createLibraryWindow.setActionOnClose(GWindow.KEEP_OPEN);

		int x = 0, y = iTopMargin * 2;
		world.mv.delay(delayAmount);
		lblImport = new GLabel(createLibraryWindow, x, y, createLibraryWindow.width, 22, "Select Media Folder(s) for Library");
		lblImport.setLocalColorScheme(G4P.SCHEME_10);
		lblImport.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
		lblImport.setTextAlign(GAlign.CENTER, null);
		lblImport.setTextBold();

		x = 0;
		lblCreateLibraryWindowText = new GLabel(createLibraryWindow, x, y + 35, createLibraryWindow.width, 22, "Creating Environment...");
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
		btnMakeLibrary = new GButton(createLibraryWindow, x, y, 210, iSmallBoxHeight, "Choose Location...");
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
		
		world.mv.setAppIcon = true;
	}
	
	/**
	 * Open window to choose item from a list of strings and return index result
	 * @param list List items
	 * @return Index of chosen item from list
	 */
	public void openListItemWindow(ArrayList<String> list, String promptText, int resultCode, boolean setup)
	{
		if(list.size() > 0)
		{
			if(setup)
			{
				if(world.mv.display.disableLostFocusHook) 
					world.mv.display.disableLostFocusHook = false;
			}
			else
			{
				if(!world.mv.display.disableLostFocusHook) 
					world.mv.display.disableLostFocusHook = true;
			}

			listItemWindowList = new ArrayList<String>(list);
			listItemWindowText = promptText;
			listItemWindowHeight = 120 + listItemWindowList.size() * 30;			// -- Update this
			listItemWindowSelectedItem = 0;
			listItemWindowResultCode = resultCode;					// Flag indicating what to do with dialog result value
			
			int leftEdge = world.mv.displayWidth / 2 - windowWidth;
			int topEdge = world.mv.displayHeight / 2 - listItemWindowHeight / 2;

			listItemWindow = GWindow.getWindow( world.mv, "   ", leftEdge, topEdge, windowWidth * 2, listItemWindowHeight, PApplet.JAVA2D);

			listItemWindow.addData(new ML_WinData());
			listItemWindow.addDrawHandler(this, "listItemWindowDraw");
			listItemWindow.addKeyHandler(world.mv, "listItemWindowKey");
			listItemWindow.setActionOnClose(GWindow.KEEP_OPEN);
			
			setupListItemWindow = true;
			showListItemWindow = true;
			world.mv.setAppIcon = true;
		}
	}
	
	/**
	 * Close List Item Window
	 */
	public void closeListItemWindow()
	{
		world.mv.systemMessage("Window.closeListItemWindow()... ");
		
		if(world.mv.display.disableLostFocusHook) 
			world.mv.display.disableLostFocusHook = false;
		
		closeListItemWindow = true;
		listItemWindow.setVisible(false);
		listItemWindow.close();
		listItemWindow.dispose();
		showListItemWindow = false;
		
		world.mv.delay(1);
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

		int leftEdge = world.mv.displayWidth / 2 - windowWidth;
		int topEdge = world.mv.displayHeight / 2 - createLibraryWindowHeight / 2;

		String saveText = "";
		if(setupTextEntryWindow) saveText = txfInputText.getText();
		
		textEntryWindow = GWindow.getWindow( world.mv, "  ", leftEdge, topEdge, windowWidth * 2, textEntryWindowHeight, PApplet.JAVA2D);

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
		world.mv.setAppIcon = true;
	}

	/**
	 * Handle window with lost focus
	 * @param windowTitle Window title
	 */
	public void handleWindowLostFocus(boolean running, String windowTitle)
	{
		switch(windowTitle)
		{
			case "":					// Main Window
//				System.out.println("Window.handleWindowLostFocus()... Main Window ... running? "+running+" showCreateLibraryWindow: "+showCreateLibraryWindow+" closeStartupWindow:"+closeStartupWindow);
				break;
			case " ":				// Startup Window
				if(closeStartupWindow)
				{
					closeStartupWindow = false;
					showStartupWindow = false;
				}
				else if(showStartupWindow && !showCreateLibraryWindow)
				{
					showStartupWindow(true);
				}
				break;
			case "  ":				// Text Entry Window
				if(closeTextEntryWindow) 
				{
					closeTextEntryWindow = false;
					showTextEntryWindow = false;
				}
				else if(showTextEntryWindow)
				{
					showTextEntryWindow(true);
				}
				break;
			case "   ":				// List Item Window
				if(closeListItemWindow)
				{
					closeListItemWindow = false;
					showListItemWindow = false;
				}
				else if(showListItemWindow)
				{
					System.out.println("1 will show list item window");
					showListItemWindow(true);
				}
				break;
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
			case "Preferences":
				System.out.println("handleWindowLostFocus()... "+windowTitle+" will hide window...");
				hidePreferencesWindow();
				break;
			case "Help":
				hideHelpWindow();
				break;
			default:
//				System.out.println("handleWindowLostFocus()... Unknown windowTitle: "+windowTitle);
				break;
		}
	}
	
	
	/**
	 * Handle window with gained focus
	 * @param windowTitle Window title
	 */
	public void handleWindowGainedFocus(boolean running, String windowTitle)
	{
		switch(windowTitle)
		{
			case "":					// Main Window
				if(showStartupWindow && !showCreateLibraryWindow)
					showStartupWindow(true);							// Keep Startup Window on top
				if(showCreateLibraryWindow)
					showCreateLibraryWindow(true);					// Keep Create Library Window on top
				if(showTextEntryWindow)
					showTextEntryWindow(true);						// Keep Text Entry Window on top
				if(showListItemWindow)
					showListItemWindow(true);						// Keep List Item Window on top
				break;
			case " ":				// Startup Window
				System.out.println("handleWindowGainedFocus()... Startup Window ... running? "+running+" showCreateLibraryWindow: "+showCreateLibraryWindow+" closeStartupWindow:"+closeStartupWindow);
				break;
			case "  ":				// Text Entry Window
				System.out.println("handleWindowGainedFocus()... Text Entry Window ... running? "+running+" showTextEntryWindow: "+showTextEntryWindow+" closeTextEntryWindow:"+closeTextEntryWindow);
				break;
			case "   ":				// List Item Window
				System.out.println("handleWindowGainedFocus()... List Item Window ... running? "+running+" showListItemWindow: "+showListItemWindow+" closeListItemWindow:"+closeListItemWindow);
				break;
			case "Main Menu":
				break;
			case "Navigation":
				break;
			case "Media":
				break;
			case "Time":
				break;
			case "Library":
				break;
			case "Help":
				break;
			default:
				System.out.println("handleWindowGainedFocus()... Unknown windowTitle: "+windowTitle);
				break;
		}
	}

	/**
	 * Update secondary windows
	 */
	public void update()
	{
		if(world.mv.frameCount - lastWindowHiddenFrame > closeWindowWaitTime)		/* Close hidden window(s) after a few seconds */
		{
			if(setupMainMenu && !showMainMenu)
				closeMainMenu();
			if(setupNavigationWindow && !showNavigationWindow)
				closeNavigationWindow();
			if(setupMediaWindow && !showMediaWindow)
				closeMediaWindow();
			if(setupTimeWindow && !showTimeWindow)
				closeTimeWindow();
			if(setupPreferencesWindow && !showPreferencesWindow)
			{
				System.out.println("Window.update()... will close prefs window");
				closePreferencesWindow();
			}
			if(setupHelpWindow && !showHelpWindow)
				closeHelpWindow();
		}
	}
	
	/**
	 * Handles drawing to the ML Window
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 */
	public void mvWindowDraw(PApplet applet, GWinData data) {
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
	public void mvWindowMouse(PApplet applet, GWinData data, MouseEvent event) {
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
	 * Handles drawing to the Startup Window
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 */
	public void startupWindowDraw(PApplet applet, GWinData data) 
	{
		applet.background(0);
		applet.stroke(0, 25, 255);
		applet.strokeWeight(1);

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
		
		if(display.mv.state.selectedNewLibraryDestination || display.mv.state.selectedLibrary)
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
			if(display.mv.library == null)
			{
				applet.text("No media folders yet.", x, y);
			}
			else
			{
				if(display.mv.library.mediaFolders == null)
				{
					applet.text("No media folders yet.", x, y);
				}
				else
				{
					if( display.mv.library.mediaFolders.size() == 0 )
						applet.text("No media folders yet.", x, y);
					else
					{
						applet.textSize(smallTextSize+1);
						x = iLeftMargin * 2;
						for(String strFolder : display.mv.library.mediaFolders)
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
//		applet.line(0, navigationWindowLineBreakY_1, windowWidth, navigationWindowLineBreakY_1);
		applet.line(0, navigationWindowLineBreakY_2, windowWidth, navigationWindowLineBreakY_2);
		applet.line(0, navigationWindowLineBreakY_3, windowWidth, navigationWindowLineBreakY_3);
		applet.line(0, navigationWindowLineBreakY_4, windowWidth, navigationWindowLineBreakY_4);
		applet.line(0, navigationWindowLineBreakY_5, windowWidth, navigationWindowLineBreakY_5);
		
//		if(world.state.timeFading && !world.state.paused)			// -- Fix
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
		applet.line(0, mediaWindowLineBreakY_1, windowWidth, mediaWindowLineBreakY_1);
		applet.line(0, mediaWindowLineBreakY_2, windowWidth, mediaWindowLineBreakY_2);
		applet.line(0, mediaWindowLineBreakY_3, windowWidth, mediaWindowLineBreakY_3);
		applet.line(0, mediaWindowLineBreakY_4, windowWidth, mediaWindowLineBreakY_4);

		if(setupMediaWindow)
		{
//			if(subjectDistanceDownBtnDown)
//			{
//				world.getCurrentField().fadeFocusDistances(0.985f);
//			}
//			else if(subjectDistanceUpBtnDown)
//			{
//				world.getCurrentField().fadeFocusDistances(1.015228f);
//			}
			
			if(world.state.timeFading && !world.state.paused)
				sdrVisibleAngle.setValue(world.viewer.getFieldOfView());		// -- Move this
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
	public void preferencesWindowDraw(PApplet applet, GWinData data) 
	{
		applet.background(0);
		if(world.mv.state.running && setupPreferencesWindow && prefsWindowViewerTextYOffset > 0)
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
		if(setupNavigationWindow)
		{
			navigationWindow.setVisible(true);
			showNavigationWindow = true;
		}
	} 
	/**
	 * Show Media Window
	 */
	public void showMediaWindow()
	{
		if(setupMediaWindow)
		{
			mediaWindow.setVisible(true);
			showMediaWindow = true;
		}
	}
	public void showPreferencesWindow()
	{
		if(setupPreferencesWindow)
		{
			preferencesWindow.setVisible(true);
			showPreferencesWindow = true;
		}
	} 
	public void showHelpWindow()
	{
		if(setupHelpWindow)
		{
			helpWindow.setVisible(true);
			showHelpWindow = true;
			helpAboutText = 0;					// Always start with "About" text
		}
	}
	public void showTimeWindow()
	{
		if(setupTimeWindow)
		{
			timeWindow.setVisible(true);
			showTimeWindow = true;
		}
	}
	
	/**
	 * Show Startup Window
	 * @param force Force visibility
	 */
	public void showStartupWindow(boolean force)
	{
		if(!closeStartupWindow)
		{
			if(force)
			{
				showStartupWindow = false;
				startupWindow.setVisible(false);
				startupWindow.setVisible(true);
				showStartupWindow = true;
			}
			else
			{
				if(setupStartupWindow)
				{
					startupWindow.setVisible(true);
					showStartupWindow = true;
				}
			}
		}
	}

	/**
	 * Show Text Entry Window
	 * @param force Whether to force showing the window (true) or not (false)
	 */
	public void showTextEntryWindow(boolean force)
	{
		if(!closeTextEntryWindow)
		{
			hideWindows();
			if(force)
			{
				showTextEntryWindow = false;
				textEntryWindow.setVisible(false);
				textEntryWindow.setVisible(true);
				showTextEntryWindow = true;
			}
			else
			{
				if(setupTextEntryWindow)
				{
					textEntryWindow.setVisible(true);
					showTextEntryWindow = true;
				}
			}
		}
	}

	/**
	 * Show List Item Window
	 * @param force Whether to force showing the window (true) or not (false)
	 */
	public void showListItemWindow(boolean force)
	{
		System.out.println("showListItemWindow()... Showing list item window... force? "+force);

//		handleWindowGainedFocus()... List Item Window ... running? true showListItemWindow: true closeListItemWindow:true
		if(!closeListItemWindow)
		{
			hideWindows();
			if(force)
			{
				showListItemWindow = false;
				listItemWindow.setVisible(false);
				listItemWindow.setVisible(true);
				showListItemWindow = true;
			}
			else
			{
				if(setupListItemWindow)
				{
					listItemWindow.setVisible(true);
					showListItemWindow = true;
				}
			}
		}
	}

	/**
	 * Show Create Library Window
	 * @param force Whether to force showing the window (true) or not (false)
	 */
	public void showCreateLibraryWindow(boolean force)
	{
		hideWindows();
		if(force)
		{
			showCreateLibraryWindow = false;
			createLibraryWindow.setVisible(false);
			createLibraryWindow.setVisible(true);
			showCreateLibraryWindow = true;
		}
		else
		{
			if(setupCreateLibraryWindow)
			{
				createLibraryWindow.setVisible(true);
				showCreateLibraryWindow = true;
			}
		}
	}

	/**
	 * Hide Main Menu
	 */
	public void hideMainMenu()
	{
		showMainMenu = false;
		mainMenu.setVisible(false);
		lastWindowHiddenFrame = world.mv.frameCount;
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
		lastWindowHiddenFrame = world.mv.frameCount;
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
		lastWindowHiddenFrame = world.mv.frameCount;
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
	public void hidePreferencesWindow()
	{
		System.out.println("Window.hidePreferencesWindow()...");
		showPreferencesWindow = false;
		if(setupPreferencesWindow)
			preferencesWindow.setVisible(false);
		lastWindowHiddenFrame = world.mv.frameCount;
	} 
	public void closePreferencesWindow()
	{
		System.out.println("Window.closePreferencesWindow()...");
		showPreferencesWindow = false;
		if(setupPreferencesWindow)
		{
			preferencesWindowX = (int)getLocation(preferencesWindow).x;
			preferencesWindowY = (int)getLocation(preferencesWindow).y;
			preferencesWindow.setVisible(false);
			preferencesWindow.close();
			preferencesWindow.dispose();
			setupPreferencesWindow = false;
		}
	} 
	public void hideHelpWindow()
	{
		showHelpWindow = false;
		if(setupHelpWindow)
			helpWindow.setVisible(false);
		lastWindowHiddenFrame = world.mv.frameCount;
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
	 * Close Text Entry Window
	 */
	public void closeTextEntryWindow()
	{
		showTextEntryWindow = false;
		if(setupTextEntryWindow)
		{
			closeTextEntryWindow = true;				// Don't reopen window when loses focus
			textEntryWindow.setVisible(false);
			textEntryWindow.close();
			textEntryWindow.dispose();
			setupTextEntryWindow = false;
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
		hidePreferencesWindow();
		hideHelpWindow();
	}
	
	/**
	 * Close all windows
	 */
	public void closeAllWindows()
	{
		System.out.println("Window.closeAllWindows()...");
		closeMainMenu();
		closeNavigationWindow();
		closeMediaWindow();

		closePreferencesWindow();
		closeHelpWindow();
		closeTextEntryWindow();				// -- Added 7/13/17
		closeCreateLibraryWindow();
		closeStartupWindow();
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
	public void hideStartupWindow()
	{
		showStartupWindow = false;
		if(setupStartupWindow)
			startupWindow.setVisible(false);
	} 
	public void closeStartupWindow()
	{
//		System.out.println("closeStartupWindow()... showStartupWindow:"+showStartupWindow+" setupStartupWindow:"+setupStartupWindow);
		showStartupWindow = false;
		if(setupStartupWindow)
		{
			closeStartupWindow = true;
			startupWindow.setVisible(false);
			startupWindow.close();
			startupWindow.dispose();
			setupStartupWindow = false;
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
//			closeCreateLibraryWindow = true;
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
		if(showPreferencesWindow)
			hidePreferencesWindow();
		if(showHelpWindow)
			hideHelpWindow();
		if(showTimeWindow)
			hideTimeWindow();
	}
	/**
	 * Open Main Menu
	 */
	public void openMainMenu()
	{
		if(!setupMainMenu) setupMainMenu(true);
		else showMainMenu();
	}
	
	/**
	 * Open Navigation Window
	 */
	public void openNavigationWindow()
	{
		if(!setupNavigationWindow) setupNavigationWindow(true);
		else showNavigationWindow();
	}
	
	/**
	 * Open Media Window
	 */
	public void openMediaWindow()
	{
		if(!setupMediaWindow) setupMediaWindow(true);
		else showMediaWindow();
	}
	
	/**
	 * Open Time Window
	 */
	public void openTimeWindow()
	{
		if(!setupTimeWindow) setupTimeWindow(true);
		else showTimeWindow();
	}
	
	/**
	 * Open Preferences Window
	 */
	public void openPreferencesWindow()
	{
		if(!setupPreferencesWindow) setupPreferencesWindow(true);
		else showPreferencesWindow();
	}
	
	/**
	 * Open Help Window
	 */
	public void openHelpWindow()
	{
		if(!setupHelpWindow) setupHelpWindow(true);
		else showHelpWindow();
	}
//	public void openMapWindow()
//	{
//		if(!setupMapWindow)
//			setupMapWindow();
//		showMapWindow();
//	}
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