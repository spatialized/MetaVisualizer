package multimediaLocator;

import java.awt.Font;
import java.util.ArrayList;

import g4p_controls.*;
import processing.core.PApplet;
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
	private int windowWidth = 310, longWindowHeight = 600;
	private int shortWindowHeight = 340;
	private int delayAmount = 60;							// Delay length to avoid G4P library concurrent modification exception

	/* Windows */
	public GWindow mlWindow, navigationWindow, graphicsWindow, statisticsWindow,  helpWindow, 
				   memoryWindow, libraryWindow, importWindow, listItemWindow, textEntryWindow;
	public GWindow timeWindow, modelWindow, selectionWindow;	// -- Obsolete

	public GLabel lblMainMenu, lblNavigationWindow, lblGraphics, lblStatistics, lblHelp, lblMemory, lblLibrary, lblImport;	
	
	public boolean setupMLWindow, setupNavigationWindow = false, setupGraphicsWindow = false, setupHelpWindow = false, 
				   setupStatisticsWindow = false;
	public boolean setupImportWindow = false, showImportWindow = false;
	
	public boolean showMLWindow = false, showNavigationWindow = false, showGraphicsWindow = false, showStatisticsWindow = false, 
			showHelpWindow = false;
	public boolean setupLibraryWindow = false, showLibraryWindow = false;

	/* Margins */
	private int iLeftMargin = 15;		/* Margins */
	private int iBottomMargin = 25;
	private int iTopMargin = 15;
	
	private int iButtonSpacingWide = 32;		/* Button spacing*/
	private int iButtonSpacing = 26;
	
	/* Sizing */
	private int iSmallBoxHeight = 20;	/* GUI box object height */
	private int iMediumBoxHeight = 28;
	private int iLargeBoxHeight = 40;

	/* Text */
	int iVeryLargeTextSize = 20;			/* Button text size */
	int iLargeTextSize = 18;			/* Button text size */
	int iMediumTextSize = 16;
	int iSmallTextSize = 14;
	int iVerySmallTextSize = 12;
	
	float fLargeTextSize = 18.f;		/* Window text size */
	float fMediumTextSize = 16.f;
	float fSmallTextSize = 14.f;
	
	float lineWidthVeryWide = 18.f;		/* Window text line width */
	float lineWidthWide = 16.f;
	float lineWidth = 14.f;

	/* Main Window */
	private GButton btnNavigationWindow, btnGraphicsWindow, btnStatisticsWindow, btnHelpWindow;
	private GButton btnChooseField, btnSaveLibrary, btnSaveField, btnRestart, btnQuit;
	private GLabel lblViews, lblWindows, lblCommands, lblSpaceBar;
	public GToggleGroup tgDisplayView;	
	public GOption optWorldView, optMapView, optTimelineView;
	private int mlWindowHeight;
	
	/* Library Window */
	public GButton btnCreateLibrary, btnOpenLibrary, btnLibraryHelp;
	public GLabel lblLibraryWindowText;

	private int libraryWindowHeight;
	
	/* Import Window */
	private GButton btnImportMediaFolder, btnMakeLibrary;
	private int importWindowHeight;

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
	private String textEntryWindowText;		// Prompt text
	private String textEntryWindowUserEntry;	// User entry
	public int textEntryWindowSelectedItem = -1;
	public int textEntryWindowResultCode = -1;		// 1: GPS Track  
	
	/* Navigation Window */
	private GLabel lblTimeNavigation, lblAutoNavigation, lblKeyNavigation, lblMemoryCommands, lblPathNavigation, 	/* Navigation Controls */
				   lblTeleportLength, lblPathWaitLength;
	
	private GButton btnZoomIn, btnZoomOut;
	private GButton btnSaveLocation, btnClearMemory;
	private GButton btnJumpToRandomCluster;
	public GButton btnExportScreenshot, btnExportMedia, btnOutputFolder;
	public GSlider sdrTeleportLength, sdrPathWaitLength;

	private GButton btnNextTimeSegment, btnPreviousTimeSegment;
	private GButton btnMoveToNearestCluster, btnMoveToLastCluster;
	private GButton btnGoToPreviousField, btnGoToNextField, btnChooseGPSTrack;

	private int navigationWindowLineBreakY_1, navigationWindowLineBreakY_2;
	
	public GCheckbox chkbxPathFollowing;
	public GLabel lblCommand1;
	int navigationWindowHeight;
	
	private GLabel lblTimeWindow, lblTimeCycle, lblTimeMode;										/* Time Controls */
	public GLabel lblMediaLength, lblTimeCycleLength, lblCurrentTime, lblClusterLength;
	public GLabel lblTime;
	public GCheckbox chkbxPaused, chkbxTimeFading;
	public GSlider sdrMediaLength, sdrTimeCycleLength, sdrCurrentTime, sdrClusterLength;
	public GToggleGroup tgTimeMode;	
	public GOption optClusterTimeMode, optFieldTimeMode; //, optMediaTimeMode;
	public GLabel lblCommand2;
	int timeWindowHeight;

	/* Graphics Window */
	public GLabel lblGraphicsSettings, lblGraphicsModes, lblOutput, lblZoom;
	public GOption optTimeline, optGPSTrack, optMemory;
	public GToggleGroup tgFollow;	
	public GCheckbox chkbxMovementTeleport, chkbxFollowTeleport;
	public GCheckbox chkbxBlurMasks;
	private GLabel lblHideMedia;
	public GCheckbox chkbxHideImages, chkbxHideVideos, chkbxHidePanoramas, chkbxHideSounds;
	public GCheckbox chkbxAlphaMode;
	public GCheckbox chkbxOrientationMode, chkbxDomeView;
	public GCheckbox chkbxAngleFading, chkbxAngleThinning;

	private GLabel lblVisibleAngle, lblAlpha, lblBrightness;
	public GSlider sdrVisibleAngle, sdrAlpha, sdrBrightness;//, sdrMediaSize;

	private int graphicsWindowLineBreakY_1, graphicsWindowLineBreakY_2;

	private GLabel lblSubjectDistance;
	private GButton btnSubjectDistanceUp, btnSubjectDistanceDown;
	public GLabel lblCommand3;
	int graphicsWindowHeight;

	/* Model Window */
	private GLabel lblAdvanced, lblAltitudeScaling;
	public GCheckbox chkbxShowModel, chkbxMediaToCluster, chkbxCaptureToMedia, chkbxCaptureToCluster;
	public GSlider sdrAltitudeScaling;
	public GLabel lblCommand4;
	int modelWindowHeight;
	
	/* Memory Window */
	public GLabel lblCommand5;
	int memoryWindowHeight;
	
	/* Selection Window */
	public GLabel lblSelection, lblSelectionOptions;
	public GCheckbox chkbxSelectionMode, chkbxMultiSelection, chkbxSegmentSelection, chkbxShowMetadata;
	public GButton btnSelectFront, btnDeselectFront, btnDeselectAll, btnViewSelected, btnStitchPanorama;
	public GLabel lblCommand6;
	int selectionWindowHeight;

	/* Statistics Window */
	public GLabel lblCommand7;
	int statisticsWindowHeight;

	/* Help Window */
	int helpWindowHeight;
	public GLabel lblShift8;
	private GButton btnAboutHelp, btnImportHelp, btnCloseHelp;
	public int helpAboutText = 0;		// Whether showing  0: About Text  1: Importing Files Help Text, or 2: Keyboard Shortcuts

	private String windowTitle = "";
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
		
		mlWindowHeight = shortWindowHeight + 50;
		libraryWindowHeight = shortWindowHeight / 2;
		importWindowHeight = shortWindowHeight;
		listItemWindowHeight = shortWindowHeight;			// -- Update this
		textEntryWindowHeight = shortWindowHeight;
		
		navigationWindowHeight = parent.ml.appHeight - 50;
		graphicsWindowHeight = parent.ml.appHeight;
		statisticsWindowHeight = longWindowHeight + 100;
		helpWindowHeight = longWindowHeight + 100;
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

	public void openGraphicsWindow()
	{
		if(!setupGraphicsWindow) setupGraphicsWindow();
		showGraphicsWindow();
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

	/**
	 * Setup MultimediaLocator Window
	 */
	public void setupMLWindow()
	{
		int leftEdge = world.ml.appWidth / 2 - windowWidth / 2;
		int topEdge = world.ml.appHeight / 2 - mlWindowHeight / 2;
		
		mlWindow = GWindow.getWindow(world.ml, "Main Menu", leftEdge, topEdge, windowWidth, mlWindowHeight, PApplet.JAVA2D);
		mlWindow.addData(new ML_WinData());
		mlWindow.addDrawHandler(this, "mlWindowDraw");
		mlWindow.addMouseHandler(this, "mlWindowMouse");
		mlWindow.addKeyHandler(world.ml, "mlWindowKey");
		mlWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		world.ml.delay(delayAmount);
		
		int x = 0, y = iTopMargin;
		lblViews = new GLabel(mlWindow, x, y, mlWindow.width, iSmallBoxHeight);						/* Display Mode Label */
		lblViews.setText("View Mode");
		lblViews.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		lblViews.setLocalColorScheme(G4P.SCHEME_10);
		lblViews.setTextAlign(GAlign.CENTER, null);

		x = 25;
		y += iButtonSpacingWide;
		optWorldView = new GOption(mlWindow, x, y, 90, iSmallBoxHeight, "World (1)");
		optWorldView.setLocalColorScheme(G4P.SCHEME_10);
		optWorldView.tag = "SceneView";
		optMapView = new GOption(mlWindow, x += 100, y, 90, iSmallBoxHeight, "Map (2)");
		optMapView.setLocalColorScheme(G4P.SCHEME_10);
		optMapView.tag = "MapView";
		optTimelineView = new GOption(mlWindow, x += 95, y, 100, iSmallBoxHeight, "Time (3)");
		optTimelineView.setLocalColorScheme(G4P.SCHEME_10);
		optTimelineView.tag = "TimelineView";

		world.ml.delay(delayAmount);

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
	
		x = 0;
		y += iButtonSpacingWide;
		lblWindows = new GLabel(mlWindow, x, y, mlWindow.width, iSmallBoxHeight);						/* Display Mode Label */
		lblWindows.setText("Windows");
		lblWindows.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		lblWindows.setLocalColorScheme(G4P.SCHEME_10);
		lblWindows.setTextAlign(GAlign.CENTER, null);

		x = 95;
		y += iButtonSpacingWide;
		btnNavigationWindow = new GButton(mlWindow, x, y, 120, iSmallBoxHeight, "Navigation  ⇧1");
		btnNavigationWindow.tag = "OpenNavigationWindow";
		btnNavigationWindow.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		y += iButtonSpacing;
		btnGraphicsWindow = new GButton(mlWindow, x, y, 120, iSmallBoxHeight, "Graphics  ⇧2");
		btnGraphicsWindow.tag = "OpenGraphicsWindow";
		btnGraphicsWindow.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		y += iButtonSpacing;
		btnStatisticsWindow = new GButton(mlWindow, x, y, 120, iSmallBoxHeight, "Statistics  ⇧3");
		btnStatisticsWindow.tag = "OpenStatisticsWindow";
		btnStatisticsWindow.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 0;
		y += iButtonSpacingWide;
		lblCommands = new GLabel(mlWindow, x, y, mlWindow.width, iSmallBoxHeight);						/* Display Mode Label */
		lblCommands.setText("Commands");
		lblCommands.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		lblCommands.setLocalColorScheme(G4P.SCHEME_10);
		lblCommands.setTextAlign(GAlign.CENTER, null);

		x = 85;
		y += iButtonSpacingWide;
		btnSaveLibrary = new GButton(mlWindow, x, y, 140, iSmallBoxHeight, "Save Library  ⇧S");
		btnSaveLibrary.tag = "SaveWorld";
		btnSaveLibrary.setLocalColorScheme(G4P.CYAN_SCHEME);

		if(world.getFieldCount() > 1)
		{
//			System.out.println("btnSaveField...");
			y += iButtonSpacing;
			btnSaveField = new GButton(mlWindow, x, y, 140, iSmallBoxHeight, "Save Field  /");
			btnSaveField.tag = "SaveField";
			btnSaveField.setLocalColorScheme(G4P.CYAN_SCHEME);
		}

		y += iButtonSpacing;
		btnRestart = new GButton(mlWindow, x, y, 140, iSmallBoxHeight, "Close Library  ⇧R");
		btnRestart.tag = "CloseLibrary";
		btnRestart.setLocalColorScheme(G4P.ORANGE_SCHEME);

		x = 105;
		y += iButtonSpacing;
		btnQuit = new GButton(mlWindow, x, y, 100, iSmallBoxHeight, "Quit  ⇧Q");
		btnQuit.tag = "Quit";
		btnQuit.setLocalColorScheme(G4P.RED_SCHEME);

		y = mlWindowHeight - iBottomMargin * 5 / 2;
		btnHelpWindow = new GButton(mlWindow, windowWidth - 30 - iLeftMargin, y, 30, 30, "?");
		btnHelpWindow.tag = "OpenHelpWindow";
		btnHelpWindow.setFont(new Font("Monospaced", Font.BOLD, iLargeTextSize));
		btnHelpWindow.setLocalColorScheme(G4P.CYAN_SCHEME);

		world.ml.delay(delayAmount);

		x = 0;
		y = mlWindowHeight - iBottomMargin;
		lblSpaceBar = new GLabel(mlWindow, x, y, mlWindow.width, iSmallBoxHeight);						/* Display Mode Label */
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
		int leftEdge = world.ml.appWidth / 2 - windowWidth / 2;
		int topEdge = world.ml.appHeight / 2 - navigationWindowHeight / 2;
		
		navigationWindow = GWindow.getWindow(world.ml, windowTitle, leftEdge, topEdge, windowWidth, navigationWindowHeight, PApplet.JAVA2D);
		navigationWindow.setVisible(true);

		navigationWindow.addData(new ML_WinData());
		navigationWindow.addDrawHandler(this, "navigationWindowDraw");
		navigationWindow.addMouseHandler(this, "navigationWindowMouse");
		navigationWindow.addKeyHandler(world.ml, "navigationWindowKey");
		navigationWindow.setActionOnClose(GWindow.KEEP_OPEN);
		world.ml.delay(delayAmount);
		
		int x = 0, y = iTopMargin;
		lblNavigationWindow = new GLabel(navigationWindow, x, y, navigationWindow.width, iSmallBoxHeight, "Navigation");
		lblNavigationWindow.setLocalColorScheme(G4P.SCHEME_10);
		lblNavigationWindow.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
		lblNavigationWindow.setTextAlign(GAlign.CENTER, null);
		lblNavigationWindow.setTextBold();

		x = 0;
		y += iSmallBoxHeight * 1.5f;
		lblAutoNavigation = new GLabel(navigationWindow, x, y, navigationWindow.width, iSmallBoxHeight, "Movement");
		lblAutoNavigation.setLocalColorScheme(G4P.SCHEME_10);
		lblAutoNavigation.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblAutoNavigation.setTextAlign(GAlign.CENTER, null);
		lblAutoNavigation.setTextBold();

		y += iSmallBoxHeight * 1.5f;
		lblKeyNavigation = new GLabel(navigationWindow, x, y, navigationWindow.width, iSmallBoxHeight, "Keyboard: Use w/d/a/s keys");
		lblKeyNavigation.setLocalColorScheme(G4P.SCHEME_10);
		lblKeyNavigation.setFont(new Font("Monospaced", Font.ITALIC, iMediumTextSize));
		lblKeyNavigation.setTextAlign(GAlign.CENTER, null);
		world.ml.delay(delayAmount);

		x = 50;
		y += iButtonSpacingWide;
		btnMoveToLastCluster = new GButton(navigationWindow, x, y, 60, iSmallBoxHeight, "Last (l)");
		btnMoveToLastCluster.tag = "LastCluster";
		btnMoveToLastCluster.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 120;
		btnJumpToRandomCluster = new GButton(navigationWindow, x, y, 80, iSmallBoxHeight, "Random (j)");
		btnJumpToRandomCluster.tag = "RandomCluster";
		btnJumpToRandomCluster.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 210;
		btnMoveToNearestCluster = new GButton(navigationWindow, x, y, 60, iSmallBoxHeight, "Near (m)");
		btnMoveToNearestCluster.tag = "NearestCluster";
		btnMoveToNearestCluster.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 0;
		y += iButtonSpacingWide;
		lblTimeNavigation = new GLabel(navigationWindow, x, y, navigationWindow.width, iSmallBoxHeight, "Time");
		lblTimeNavigation.setLocalColorScheme(G4P.SCHEME_10);
		lblTimeNavigation.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblTimeNavigation.setTextAlign(GAlign.CENTER, null);
		lblTimeNavigation.setTextBold();
		world.ml.delay(delayAmount);

		x = 65;
		btnPreviousTimeSegment = new GButton(navigationWindow, x, y, 60, iSmallBoxHeight, "Back (b)");
		btnPreviousTimeSegment.tag = "PreviousTime";
		btnPreviousTimeSegment.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 190;
		btnNextTimeSegment = new GButton(navigationWindow, x, y, 85, iSmallBoxHeight, "Forward (n)");
		btnNextTimeSegment.tag = "NextTime";
		btnNextTimeSegment.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		x = 120;
		y += iButtonSpacing;
		chkbxMovementTeleport = new GCheckbox(navigationWindow, x, y, 120, iSmallBoxHeight, "Teleport (t)");
		chkbxMovementTeleport.tag = "MovementTeleport";
		chkbxMovementTeleport.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		chkbxMovementTeleport.setLocalColorScheme(G4P.SCHEME_10);
		chkbxMovementTeleport.setSelected(world.viewer.getState().movementTeleport);

		x = 75;
		y += iButtonSpacingWide;
		btnZoomOut = new GButton(navigationWindow, x, y, 50, iSmallBoxHeight, "Out (z)");
		btnZoomOut.tag = "ZoomOut";
		btnZoomOut.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 0;
		lblZoom = new GLabel(navigationWindow, x, y, navigationWindow.width, iSmallBoxHeight, "Zoom");
		lblZoom.setLocalColorScheme(G4P.SCHEME_10);
		lblZoom.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblZoom.setTextAlign(GAlign.CENTER, null);
		lblZoom.setTextBold();
		world.ml.delay(delayAmount);

		x = 190;
		btnZoomIn = new GButton(navigationWindow, x, y, 50, iSmallBoxHeight, "In (q)");
		btnZoomIn.tag = "ZoomIn";
		btnZoomIn.setLocalColorScheme(G4P.CYAN_SCHEME);

		if(world.getFields() != null)
		{
			if(world.getFieldCount() > 1)
			{
				x = 75;
				y += iButtonSpacingWide;
				btnChooseField = new GButton(navigationWindow, x, y, 140, iSmallBoxHeight, "Choose Field  ⇧C");
				btnChooseField.tag = "ChooseField";
				btnChooseField.setLocalColorScheme(G4P.CYAN_SCHEME);

				x = 40;
				y += iButtonSpacingWide;
				btnGoToPreviousField = new GButton(navigationWindow, x, y, 120, iSmallBoxHeight, "Previous Field  ⇧[");
				btnGoToPreviousField.tag = "PreviousField";
				btnGoToPreviousField.setLocalColorScheme(G4P.CYAN_SCHEME);

				btnGoToNextField = new GButton(navigationWindow, x+=125, y, 100, iSmallBoxHeight, "Next Field  ⇧]");
				btnGoToNextField.tag = "NextField";
				btnGoToNextField.setLocalColorScheme(G4P.CYAN_SCHEME);
			}
		}

		x = 40;
		y += 40;
		lblMemory = new GLabel(navigationWindow, x, y, 70, iSmallBoxHeight, "Memory");
		lblMemory.setLocalColorScheme(G4P.SCHEME_10);
		lblMemory.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblMemory.setTextBold();
		world.ml.delay(delayAmount);

		x = 120;
		btnSaveLocation = new GButton(navigationWindow, x, y, 60, iSmallBoxHeight, "Save (`)");
		btnSaveLocation.tag = "SaveLocation";
		btnSaveLocation.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 190;
		btnClearMemory = new GButton(navigationWindow, x, y, 70, iSmallBoxHeight, "Clear (y)");
		btnClearMemory.tag = "ClearMemory";
		btnClearMemory.setLocalColorScheme(G4P.ORANGE_SCHEME);
		
		x = 0;
		y += 50;
		navigationWindowLineBreakY_1 = y - 15;
		lblPathNavigation = new GLabel(navigationWindow, x, y, navigationWindow.width, iSmallBoxHeight, "Path Mode");
		lblPathNavigation.setLocalColorScheme(G4P.SCHEME_10);
		lblPathNavigation.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblPathNavigation.setTextAlign(GAlign.CENTER, null);
		lblPathNavigation.setTextBold();

		x = 100;
		y += 30;
		chkbxPathFollowing = new GCheckbox(navigationWindow, x, y, 150, iSmallBoxHeight, "On / Off  (>)");
		chkbxPathFollowing.tag = "Following";
		chkbxPathFollowing.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		chkbxPathFollowing.setLocalColorScheme(G4P.SCHEME_10);
		chkbxPathFollowing.setSelected(world.viewer.isFollowing());

		y += iSmallBoxHeight * 1.5f;
		x = 115;
		chkbxFollowTeleport = new GCheckbox(navigationWindow, x, y, 125, iSmallBoxHeight, "Teleport  (,)");
		chkbxFollowTeleport.tag = "FollowTeleport";
		chkbxFollowTeleport.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		chkbxFollowTeleport.setLocalColorScheme(G4P.SCHEME_10);
		chkbxFollowTeleport.setSelected(world.viewer.getState().followTeleport);

		x = 40;
		y += iSmallBoxHeight * 1.5f;
		optTimeline = new GOption(navigationWindow, x, y, 90, iSmallBoxHeight, "Timeline");
		optTimeline.setLocalColorScheme(G4P.SCHEME_10);
		optTimeline.tag = "FollowTimeline";
		optGPSTrack = new GOption(navigationWindow, x+=90, y, 90, iSmallBoxHeight, "GPS Track");
		optGPSTrack.setLocalColorScheme(G4P.SCHEME_10);
		optGPSTrack.tag = "FollowGPSTrack";
		optMemory = new GOption(navigationWindow, x+=90, y, 90, iSmallBoxHeight, "Memory");
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

		if(world.getCurrentField().getGPSTracks() != null)
		{
			if(world.getCurrentField().getGPSTracks().size() > 0)
			{
				x = 90;
				y += 30;
				btnChooseGPSTrack = new GButton(navigationWindow, x, y, 140, iSmallBoxHeight, "Select GPS Track");
				btnChooseGPSTrack.tag = "ChooseGPSTrack";
				btnChooseGPSTrack.setLocalColorScheme(G4P.CYAN_SCHEME);
			}
		}

		x = 150;
		y += iButtonSpacingWide;
		sdrTeleportLength = new GSlider(navigationWindow, x, y, 80, 80, iSmallBoxHeight);
		sdrTeleportLength.setLocalColorScheme(G4P.GOLD_SCHEME);
		sdrTeleportLength.setLimits(0.f, 300.f, 10.f);
		sdrTeleportLength.setValue(world.viewer.getSettings().teleportLength);
		sdrTeleportLength.setRotation(PApplet.PI/2.f);
		sdrTeleportLength.setTextOrientation(G4P.ORIENT_LEFT);
		sdrTeleportLength.setEasing(0);
		sdrTeleportLength.setShowValue(true);
		sdrTeleportLength.tag = "TeleportLength";

		x = 280;
		sdrPathWaitLength = new GSlider(navigationWindow, x, y, 80, 80, iSmallBoxHeight);
		sdrPathWaitLength.setLocalColorScheme(G4P.GOLD_SCHEME);
		sdrPathWaitLength.setLimits(0.f, 600.f, 30.f);
		sdrPathWaitLength.setValue(world.viewer.getSettings().pathWaitLength);
		sdrPathWaitLength.setRotation(PApplet.PI/2.f);
		sdrPathWaitLength.setTextOrientation(G4P.ORIENT_LEFT);
		sdrPathWaitLength.setEasing(0);
		sdrPathWaitLength.setShowValue(true);
		sdrPathWaitLength.tag = "PathWaitLength";
		
		x = iLeftMargin;
		y += iButtonSpacingWide;
		lblTeleportLength = new GLabel(navigationWindow, x, y, 100, iSmallBoxHeight, "Teleport Time");
		lblTeleportLength.setLocalColorScheme(G4P.SCHEME_10);

		x = 165;
		lblPathWaitLength = new GLabel(navigationWindow, x, y, 100, iSmallBoxHeight, "Wait Time");
		lblPathWaitLength.setLocalColorScheme(G4P.SCHEME_10);

		world.ml.delay(delayAmount);

		/* Time */
		y += 75;
		navigationWindowLineBreakY_2 = y - 15;
		lblTimeWindow = new GLabel(navigationWindow, 0, y, navigationWindow.width, iSmallBoxHeight, "Time");
		lblTimeWindow.setLocalColorScheme(G4P.SCHEME_10);
		lblTimeWindow.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
		lblTimeWindow.setTextAlign(GAlign.CENTER, null);
		lblTimeWindow.setTextBold();

		x = 0;
		y += 30;
		lblTimeMode = new GLabel(navigationWindow, x, y, navigationWindow.width, iSmallBoxHeight, "Time Mode");
		lblTimeMode.setLocalColorScheme(G4P.SCHEME_10);
		lblTimeMode.setFont(new Font("Monospaced", Font.ITALIC, iMediumTextSize));
		lblTimeMode.setTextAlign(GAlign.CENTER, null);
		lblTimeMode.setTextBold();

		x = 70;
		y += iSmallBoxHeight * 1.5f;
		optClusterTimeMode = new GOption(navigationWindow, x, y, 90, 20, "Cluster");
		optClusterTimeMode.setLocalColorScheme(G4P.SCHEME_10);
		optClusterTimeMode.setFont(new Font("Monospaced", Font.PLAIN, 13));
		optClusterTimeMode.tag = "ClusterTimeMode";
		optFieldTimeMode = new GOption(navigationWindow, x+=110, y, 90, 20, "Field");
		optFieldTimeMode.setLocalColorScheme(G4P.SCHEME_10);
		optFieldTimeMode.setFont(new Font("Monospaced", Font.PLAIN, 13));
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

		x = 0;
		y += 40;
		lblTimeCycle = new GLabel(navigationWindow, x, y, navigationWindow.width, iSmallBoxHeight, "Time Cycle");
		lblTimeCycle.setLocalColorScheme(G4P.SCHEME_10);
		lblTimeCycle.setFont(new Font("Monospaced", Font.ITALIC, iMediumTextSize));
		lblTimeCycle.setTextAlign(GAlign.CENTER, null);
		lblTimeCycle.setTextBold();

		x = 115;
		y += iSmallBoxHeight * 1.5f;
		
		chkbxTimeFading = new GCheckbox(navigationWindow, x, y, 160, iSmallBoxHeight, "Run Cycle  (⇧T)");
		chkbxTimeFading.tag = "TimeFading";
		chkbxTimeFading.setLocalColorScheme(G4P.SCHEME_10);
		chkbxTimeFading.setSelected(world.getState().timeFading);

		y += iSmallBoxHeight * 1.5f;
		chkbxPaused = new GCheckbox(navigationWindow, x, y, 160, iSmallBoxHeight, "Pause  (-)");
		chkbxPaused.tag = "Paused";
		chkbxPaused.setLocalColorScheme(G4P.SCHEME_10);
		chkbxPaused.setSelected(world.getState().paused);

		x = 120;
		y += 10;
		sdrTimeCycleLength = new GSlider(navigationWindow, x, y, 160, 80, 20);
		sdrTimeCycleLength.setLocalColorScheme(G4P.GOLD_SCHEME);
		sdrTimeCycleLength.setLimits(0.f, world.settings.timeCycleLength, 3200.f);
		sdrTimeCycleLength.setTextOrientation(G4P.ORIENT_TRACK);
		sdrTimeCycleLength.setEasing(0);
		sdrTimeCycleLength.setShowValue(true);
		sdrTimeCycleLength.tag = "TimeCycleLength";

		x = 35;
		y += 30;
		lblTimeCycleLength = new GLabel(navigationWindow, x, y, 120, iSmallBoxHeight, "Cycle Length");
		lblTimeCycleLength.setLocalColorScheme(G4P.SCHEME_10);

		x = 120;
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
		y += 30;
		lblCurrentTime = new GLabel(navigationWindow, x, y, 100, iSmallBoxHeight, "Current Time");
		lblCurrentTime.setLocalColorScheme(G4P.SCHEME_10);

		x = 120;
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
		y += 30;
		lblMediaLength = new GLabel(navigationWindow, x, y, 100, iSmallBoxHeight, "Media Length");
		lblMediaLength.setLocalColorScheme(G4P.SCHEME_10);
		
		x = 120;
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
		y += 30;
		lblClusterLength = new GLabel(navigationWindow, x, y, 120, iSmallBoxHeight, "Cluster Length");
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
		y = navigationWindowHeight - iBottomMargin;
		lblCommand1 = new GLabel(navigationWindow, x, y, navigationWindow.width, iSmallBoxHeight);						/* Display Mode Label */
		lblCommand1.setText("Press SHIFT + 1 to show / hide");
		lblCommand1.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		lblCommand1.setLocalColorScheme(G4P.SCHEME_10);
		lblCommand1.setTextAlign(GAlign.CENTER, null);

		setupNavigationWindow = true;
		world.ml.setAppIcon = true;
	}

	/**
	 * Setup the Graphics Window
	 */
	public void setupGraphicsWindow()
	{
		int leftEdge = world.ml.appWidth / 2 - windowWidth / 2;
		int topEdge = world.ml.appHeight / 2 - graphicsWindowHeight / 2;

		graphicsWindow = GWindow.getWindow(world.ml, windowTitle, leftEdge, topEdge, windowWidth, graphicsWindowHeight, PApplet.JAVA2D);
		graphicsWindow.setVisible(true);

		graphicsWindow.addData(new ML_WinData());
		graphicsWindow.addDrawHandler(this, "graphicsWindowDraw");
		graphicsWindow.addMouseHandler(this, "graphicsWindowMouse");
		graphicsWindow.addKeyHandler(world.ml, "graphicsWindowKey");
		graphicsWindow.setActionOnClose(GWindow.KEEP_OPEN);
	
		int x = 0, y = iTopMargin;
		world.ml.delay(delayAmount);

		lblGraphics = new GLabel(graphicsWindow, x, y, graphicsWindow.width, iSmallBoxHeight, "Media");
		lblGraphics.setLocalColorScheme(G4P.SCHEME_10);
		lblGraphics.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
		lblGraphics.setTextAlign(GAlign.CENTER, null);
		lblGraphics.setTextBold();
		
		x = 0;
		y += iSmallBoxHeight * 1.5f;
		lblGraphicsSettings = new GLabel(graphicsWindow, x, y, graphicsWindow.width, iSmallBoxHeight, "Settings");
		lblGraphicsSettings.setLocalColorScheme(G4P.SCHEME_10);
		lblGraphicsSettings.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblGraphicsSettings.setTextAlign(GAlign.CENTER, null);
		lblGraphicsSettings.setTextBold();

		world.ml.delay(delayAmount);

		x = 120;
		y += 10;
		sdrVisibleAngle = new GSlider(graphicsWindow, x, y, 160, 80, 20);
		sdrVisibleAngle.setLocalColorScheme(G4P.GOLD_SCHEME);
		sdrVisibleAngle.setLimits(world.viewer.getVisibleAngle(), 0.1f, (float)Math.PI * 0.5f);
		sdrVisibleAngle.setTextOrientation(G4P.ORIENT_TRACK);
		sdrVisibleAngle.setEasing(0);
		sdrVisibleAngle.setShowValue(true);
		sdrVisibleAngle.tag = "VisibleAngle";
		
		x = 30;
		y += 30;
		lblVisibleAngle = new GLabel(graphicsWindow, x, y, 120, iSmallBoxHeight, "Visible Angle");
		lblVisibleAngle.setLocalColorScheme(G4P.SCHEME_10);

		x = 120;
		y += 10;
		sdrAlpha = new GSlider(graphicsWindow, x, y, 160, 80, 20);
		sdrAlpha.setLocalColorScheme(G4P.GOLD_SCHEME);
		sdrAlpha.setLimits(world.getState().alpha, 0.f, 255.f);
		sdrAlpha.setValue(world.getState().alpha);		
		sdrAlpha.setTextOrientation(G4P.ORIENT_TRACK);
		sdrAlpha.setEasing(0);
		sdrAlpha.setShowValue(true);
		sdrAlpha.tag = "Alpha";

		world.ml.delay(delayAmount);

		x = 30;
		y += 30;
		lblAlpha= new GLabel(graphicsWindow, x, y, 60, iSmallBoxHeight, "Alpha");
		lblAlpha.setLocalColorScheme(G4P.SCHEME_10);

		x = 120;
		y += 10;
		sdrBrightness = new GSlider(graphicsWindow, x, y, 160, 80, 20);
		sdrBrightness.setLocalColorScheme(G4P.GOLD_SCHEME);
		sdrBrightness.setLimits(world.viewer.getSettings().userBrightness, 0.f, 1.f);
		sdrBrightness.setValue(world.viewer.getSettings().userBrightness);		
		sdrBrightness.setTextOrientation(G4P.ORIENT_TRACK);
		sdrBrightness.setEasing(0);
		sdrBrightness.setShowValue(true);
		sdrBrightness.tag = "Brightness";

		x = 30;
		y += 30;
		lblBrightness = new GLabel(graphicsWindow, x, y, 90, iSmallBoxHeight, "Brightness");
		lblBrightness.setLocalColorScheme(G4P.SCHEME_10);

		x = 120;
		y += 10;
		world.ml.delay(delayAmount);
		sdrAltitudeScaling = new GSlider(graphicsWindow, x, y, 160, 80, 20);
		sdrAltitudeScaling.setLocalColorScheme(G4P.GOLD_SCHEME);
		sdrAltitudeScaling.setLimits(0.f, 0.f, 1.f);
		sdrAltitudeScaling.setValue(world.settings.altitudeScalingFactor);					// -- Shouldn't be needed! Calls handler
		sdrAltitudeScaling.setTextOrientation(G4P.ORIENT_TRACK);
		sdrAltitudeScaling.setEasing(0);
		sdrAltitudeScaling.setShowValue(true);
		sdrAltitudeScaling.tag = "AltitudeScaling";
		
		x = 30;
		y += 30;
		lblAltitudeScaling = new GLabel(graphicsWindow, x, y, 100, iSmallBoxHeight, "Altitude Factor");
		lblAltitudeScaling .setLocalColorScheme(G4P.SCHEME_10);

		x = 55;
		y += 45;
		btnSubjectDistanceDown = new GButton(graphicsWindow, x, y, 30, iSmallBoxHeight, "-");
		btnSubjectDistanceDown.tag = "SubjectDistanceDown";
		btnSubjectDistanceDown.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		lblSubjectDistance = new GLabel(graphicsWindow, x += 35, y, 110, iSmallBoxHeight, "Subject Distance");
		lblSubjectDistance.setLocalColorScheme(G4P.SCHEME_10);
		lblSubjectDistance.setTextAlign(GAlign.CENTER, null);
		lblSubjectDistance.setTextBold();
		
		x = 205;
		btnSubjectDistanceUp = new GButton(graphicsWindow, x, y, 30, iSmallBoxHeight, "+");
		btnSubjectDistanceUp.tag = "SubjectDistanceUp";
		btnSubjectDistanceUp.setLocalColorScheme(G4P.CYAN_SCHEME);

		world.ml.delay(delayAmount);

		x = 45;
		y += iButtonSpacingWide;
		chkbxAlphaMode = new GCheckbox(graphicsWindow, x, y, 95, iSmallBoxHeight, "Alpha Mode");
		chkbxAlphaMode.tag = "AlphaMode";
		chkbxAlphaMode.setLocalColorScheme(G4P.SCHEME_10);
		chkbxAlphaMode.setSelected(world.getState().alphaMode);

		chkbxBlurMasks = new GCheckbox(graphicsWindow, x += 105, y, 120, iSmallBoxHeight, "Fade Borders");
		chkbxBlurMasks.tag = "FadeEdges";
		chkbxBlurMasks.setLocalColorScheme(G4P.SCHEME_10);
		chkbxBlurMasks.setSelected(world.getState().useBlurMasks);

		x = 50;
		y += iSmallBoxHeight * 1.5f;
		chkbxAngleFading = new GCheckbox(graphicsWindow, x, y, 100, iSmallBoxHeight, "Angle Fading");
		chkbxAngleFading.tag = "AngleFading";
		chkbxAngleFading.setLocalColorScheme(G4P.SCHEME_10);
		chkbxAngleFading.setSelected(world.viewer.getSettings().angleFading);

		chkbxAngleThinning = new GCheckbox(graphicsWindow, x += 110, y, 100, iSmallBoxHeight, "Angle Thinning");
		chkbxAngleThinning.tag = "AngleThinning";
		chkbxAngleThinning.setLocalColorScheme(G4P.SCHEME_10);
		chkbxAngleThinning.setSelected(world.viewer.getSettings().angleThinning);

		world.ml.delay(delayAmount);

		x = 0;
		y += 40;
		lblHideMedia = new GLabel(graphicsWindow, x, y, graphicsWindow.width, iSmallBoxHeight, "Hide Media");
		lblHideMedia.setLocalColorScheme(G4P.SCHEME_10);
		lblHideMedia.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblHideMedia.setTextAlign(GAlign.CENTER, null);
		lblHideMedia.setTextBold();

		x = iLeftMargin;
		y += iButtonSpacingWide;
		chkbxHideImages = new GCheckbox(graphicsWindow, x, y, 85, iSmallBoxHeight, "Images");
		chkbxHideImages.tag = "HideImages";
		chkbxHideImages.setLocalColorScheme(G4P.SCHEME_10);
		chkbxHideImages.setSelected(world.viewer.getSettings().hideImages);
		
		chkbxHidePanoramas = new GCheckbox(graphicsWindow, x += 65, y, 105, iSmallBoxHeight, "Panoramas");
		chkbxHidePanoramas.tag = "HidePanoramas";
		chkbxHidePanoramas.setLocalColorScheme(G4P.SCHEME_10);
		chkbxHidePanoramas.setSelected(world.viewer.getSettings().hidePanoramas);

		chkbxHideVideos = new GCheckbox(graphicsWindow, x += 95, y, 85, iSmallBoxHeight, "Videos");
		chkbxHideVideos.tag = "HideVideos";
		chkbxHideVideos.setLocalColorScheme(G4P.SCHEME_10);
		chkbxHideVideos.setSelected(world.viewer.getSettings().hideVideos);

		chkbxHideSounds = new GCheckbox(graphicsWindow, x += 65, y, 95, iSmallBoxHeight, "Sounds");
		chkbxHideSounds.tag = "HideSounds";
		chkbxHideSounds.setLocalColorScheme(G4P.SCHEME_10);
		chkbxHideSounds.setSelected(world.viewer.getSettings().hideSounds);

		x = 30;
		y += iButtonSpacingWide;
		btnOutputFolder = new GButton(graphicsWindow, x, y, 115, iSmallBoxHeight, "Set Output Folder");
		btnOutputFolder.tag = "OutputFolder";
		btnOutputFolder.setLocalColorScheme(G4P.CYAN_SCHEME);

		x += 125;
		btnExportScreenshot = new GButton(graphicsWindow, x, y, 140, iSmallBoxHeight, "Save Screenshot (O)");
		btnExportScreenshot.tag = "SaveScreenshot";
		btnExportScreenshot.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 0;
		y += 50;
		graphicsWindowLineBreakY_1 = y - 15;
		lblSelection = new GLabel(graphicsWindow, x, y, graphicsWindow.width, iSmallBoxHeight, "Selection");
		lblSelection.setLocalColorScheme(G4P.SCHEME_10);
		lblSelection.setTextAlign(GAlign.CENTER, null);
		lblSelection.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
		lblSelection.setTextBold();

		x = 105;
		y += iButtonSpacingWide;
		chkbxSelectionMode = new GCheckbox(graphicsWindow, x, y, 120, iSmallBoxHeight, "Enable (A)");
		chkbxSelectionMode.tag = "EnableSelection";
		chkbxSelectionMode.setLocalColorScheme(G4P.SCHEME_10);
		chkbxSelectionMode.setFont(new Font("Monospaced", Font.BOLD, iMediumTextSize));
		chkbxSelectionMode.setSelected(world.viewer.getSettings().selection);
		
		x = 95;
		y += 35;
		btnSelectFront = new GButton(graphicsWindow, x, y, 120, iSmallBoxHeight, "Select (x)");
		btnSelectFront.tag = "SelectFront";
		btnSelectFront.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = iLeftMargin - 3;
		y += iSmallBoxHeight * 1.5f;
		chkbxMultiSelection = new GCheckbox(graphicsWindow, x, y, 145, iSmallBoxHeight, "Allow Multiple (OPT m)");
		chkbxMultiSelection.tag = "MultiSelection";
		chkbxMultiSelection.setLocalColorScheme(G4P.SCHEME_10);
		chkbxMultiSelection.setSelected(world.viewer.getSettings().multiSelection);

		chkbxSegmentSelection = new GCheckbox(graphicsWindow, x + 148, y, 175, iSmallBoxHeight, "Select Groups (OPT s)");
		chkbxSegmentSelection.tag = "SegmentSelection";
		chkbxSegmentSelection.setLocalColorScheme(G4P.SCHEME_10);
		chkbxSegmentSelection.setSelected(world.viewer.getSettings().groupSelection);

		x = 95;
		y += iButtonSpacingWide;
		btnViewSelected = new GButton(graphicsWindow, x, y, 120, iSmallBoxHeight, "View (⏎)");
		btnViewSelected.tag = "ViewSelected";
		btnViewSelected.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = 95;
		y += iButtonSpacingWide;
		btnExportMedia = new GButton(graphicsWindow, x, y, 120, iSmallBoxHeight, "Export (o)");
		btnExportMedia.tag = "ExportMedia";
		btnExportMedia.setLocalColorScheme(G4P.CYAN_SCHEME);

		x = iLeftMargin + 10;
		y += iButtonSpacingWide;
		btnDeselectFront = new GButton(graphicsWindow, x, y, 110, iSmallBoxHeight, "Deselect (X)");
		btnDeselectFront.tag = "DeselectFront";
		btnDeselectFront.setLocalColorScheme(G4P.RED_SCHEME);

		x += 120;
		btnDeselectAll = new GButton(graphicsWindow, x, y, 150, iSmallBoxHeight, "Deselect All (OPT x)");
		btnDeselectAll.tag = "DeselectAll";
		btnDeselectAll.setLocalColorScheme(G4P.RED_SCHEME);

		x = 85;
		y += iSmallBoxHeight * 1.5f;
		chkbxShowMetadata = new GCheckbox(graphicsWindow, x, y, 200, iSmallBoxHeight, "Show Metadata (M)");
		chkbxShowMetadata.tag = "ViewMetadata";
		chkbxShowMetadata.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		chkbxShowMetadata.setLocalColorScheme(G4P.SCHEME_10);
		chkbxShowMetadata.setSelected(world.getState().showMetadata);
		
//		x = 85;
//		y += iButtonSpacingWide;
//		btnStitchPanorama = new GButton(graphicsWindow, x, y, 140, iSmallBoxHeight, "Stitch Selection  (⇧\\)");
//		btnStitchPanorama.tag = "StitchPanorama";
//		btnStitchPanorama.setLocalColorScheme(G4P.GOLD_SCHEME);
	
		y += 50;
		graphicsWindowLineBreakY_2 = y - 15;
		lblAdvanced = new GLabel(graphicsWindow, 0, y, graphicsWindow.width, iSmallBoxHeight, "Advanced");
		lblAdvanced.setLocalColorScheme(G4P.SCHEME_10);
		lblAdvanced.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
		lblAdvanced.setTextAlign(GAlign.CENTER, null);
		lblAdvanced.setTextBold();

		x = 65;
		y += iButtonSpacingWide;
		chkbxOrientationMode = new GCheckbox(graphicsWindow, x, y, 200, iSmallBoxHeight, "Orientation Mode (9)");
		chkbxOrientationMode.tag = "OrientationMode";
		chkbxOrientationMode.setLocalColorScheme(G4P.SCHEME_10);
		chkbxOrientationMode.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		chkbxOrientationMode.setSelected(world.viewer.getSettings().orientationMode);

		y += iButtonSpacingWide;
		chkbxShowModel = new GCheckbox(graphicsWindow, x, y, 160, iSmallBoxHeight, "Show Model  (5)");
		chkbxShowModel.tag = "ShowModel";
		chkbxShowModel.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		chkbxShowModel.setLocalColorScheme(G4P.SCHEME_10);
		chkbxShowModel.setSelected(world.getState().showModel);
		
		x = iLeftMargin;
		y += 35;
		chkbxMediaToCluster = new GCheckbox(graphicsWindow, x, y, 130, iSmallBoxHeight, "View Clusters (6)");
		chkbxMediaToCluster.tag = "MediaToCluster";
		chkbxMediaToCluster.setLocalColorScheme(G4P.SCHEME_10);
		chkbxMediaToCluster.setEnabled(world.getState().showMediaToCluster);
		chkbxMediaToCluster.setSelected(world.getState().showMediaToCluster);
		
		chkbxCaptureToMedia = new GCheckbox(graphicsWindow, x+130, y, 150, iSmallBoxHeight, "View GPS Locations (7)");
		chkbxCaptureToMedia.tag = "CaptureToMedia";
		chkbxCaptureToMedia.setLocalColorScheme(G4P.SCHEME_10);
		chkbxCaptureToMedia.setEnabled(world.getState().showCaptureToMedia);
		chkbxCaptureToMedia.setSelected(world.getState().showCaptureToMedia);

		y += iSmallBoxHeight * 1.5f;
		chkbxCaptureToCluster = new GCheckbox(graphicsWindow, x, y, 170, iSmallBoxHeight, "View Adjustment (8)");
		chkbxCaptureToCluster.tag = "CaptureToCluster";
		chkbxCaptureToCluster.setLocalColorScheme(G4P.SCHEME_10);
		chkbxCaptureToCluster.setEnabled(world.getState().showCaptureToCluster);
		chkbxCaptureToCluster.setSelected(world.getState().showCaptureToCluster);

//		x = 110;
//		y += iButtonSpacing;
//		chkbxDomeView = new GCheckbox(graphicsWindow, x, y, 160, iSmallBoxHeight, "Sphere View (BETA)");
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
		y = graphicsWindowHeight - iBottomMargin;
		lblCommand3 = new GLabel(graphicsWindow, x, y, graphicsWindow.width, iSmallBoxHeight);						/* Display Mode Label */
		lblCommand3.setText("Press SHIFT + 2 to show / hide");
		lblCommand3.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		lblCommand3.setLocalColorScheme(G4P.SCHEME_10);
		lblCommand3.setTextAlign(GAlign.CENTER, null);

		setupGraphicsWindow = true;
		world.ml.setAppIcon = true;
	}
	
	/**
	 * Setup the Statistics Window
	 */
	public void setupStatisticsWindow()
	{
		int leftEdge = world.ml.appWidth / 2 - windowWidth * 2;
		int topEdge = world.ml.appHeight / 2 - statisticsWindowHeight / 2;

		statisticsWindow = GWindow.getWindow(world.ml, "Statistics", leftEdge, topEdge, windowWidth * 4, statisticsWindowHeight, PApplet.JAVA2D);
		statisticsWindow.setVisible(true);
		statisticsWindow.addData(new ML_WinData());
		statisticsWindow.addDrawHandler(this, "statisticsWindowDraw");
		statisticsWindow.addMouseHandler(this, "statisticsWindowMouse");
		statisticsWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		int x = 0, y = iTopMargin;
		world.ml.delay(delayAmount);

		lblStatistics = new GLabel(statisticsWindow, x, y, statisticsWindow.width, iSmallBoxHeight, "Statistics");
		lblStatistics.setLocalColorScheme(G4P.SCHEME_10);
		lblStatistics.setTextAlign(GAlign.CENTER, null);
		lblStatistics.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblStatistics.setTextBold();

		x = 0;
		y = statisticsWindowHeight - iBottomMargin;
		lblCommand6 = new GLabel(statisticsWindow, x, y, statisticsWindow.width, iSmallBoxHeight);						/* Display Mode Label */
		lblCommand6.setText("Press SHIFT + 3 to show / hide");
		lblCommand6.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		lblCommand6.setLocalColorScheme(G4P.SCHEME_10);
		lblCommand6.setTextAlign(GAlign.CENTER, null);
		
		statisticsWindow.addKeyHandler(world.ml, "statisticsWindowKey");
		
		setupStatisticsWindow = true;
		world.ml.setAppIcon = true;
	}
	
	/**
	 * Setup the Help Window
	 */
	public void setupHelpWindow()
	{
		int leftEdge = world.ml.appWidth / 2 - windowWidth * 2;
		int topEdge = world.ml.appHeight / 2 - helpWindowHeight / 2;

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
		y = helpWindowHeight - iBottomMargin * 3;
		btnCloseHelp = new GButton(helpWindow, x, y, 120, iMediumBoxHeight, "Close");
		btnCloseHelp.tag = "CloseHelp";
		btnCloseHelp.setFont(new Font("Monospaced", Font.BOLD, iSmallTextSize));
		btnCloseHelp.setLocalColorScheme(G4P.RED_SCHEME);

//		x = 0;
//		y = helpWindowHeight - iBottomMargin;
//		lblShift8 = new GLabel(helpWindow, x, y, helpWindow.width, iSmallBoxHeight);						/* Display Mode Label */
//		lblShift8.setText("Press SHIFT + 8 to show / hide");
//		lblShift8.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
//		lblShift8.setLocalColorScheme(G4P.SCHEME_10);
//		lblShift8.setTextAlign(GAlign.CENTER, null);
		
		setupHelpWindow = true;
		world.ml.setAppIcon = true;
	}

	/**
	 * Setup and open Library Window
	 */
	public void openLibraryWindow()
	{
		int leftEdge = world.ml.appWidth / 2 - windowWidth;
		int topEdge = world.ml.appHeight / 2 - libraryWindowHeight / 2;
		
		libraryWindow = GWindow.getWindow( world.ml, windowTitle, leftEdge, topEdge, windowWidth * 2, libraryWindowHeight, PApplet.JAVA2D);
		
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
		lblLibraryWindowText = new GLabel(libraryWindow, x, y, libraryWindow.width, 22, "Please wait...");
		lblLibraryWindowText.setLocalColorScheme(G4P.SCHEME_10);
		lblLibraryWindowText.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
		lblLibraryWindowText.setTextAlign(GAlign.CENTER, null);
		lblLibraryWindowText.setVisible(false);

		x = 90;
		y += 45;
		btnCreateLibrary = new GButton(libraryWindow, x, y, 170, iLargeBoxHeight, "Create Library");
		btnCreateLibrary.tag = "CreateLibrary";
		btnCreateLibrary.setFont(new Font("Monospaced", Font.BOLD, iLargeTextSize));
		btnCreateLibrary.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnOpenLibrary = new GButton(libraryWindow, x+270, y, 155, iLargeBoxHeight, "Open Library");
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

	/**
	 * Setup and show Import Window
	 */
	public void setupImportWindow()
	{
		int leftEdge = world.ml.appWidth / 2 - windowWidth * 3 / 2;
		int topEdge = world.ml.appHeight / 2 - importWindowHeight / 2;
		
		importWindow = GWindow.getWindow( world.ml, "", leftEdge, topEdge, windowWidth * 3, 
				   importWindowHeight, PApplet.JAVA2D);

		importWindow.addData(new ML_WinData());
		importWindow.addDrawHandler(this, "importWindowDraw");
		importWindow.addMouseHandler(this, "importWindowMouse");
		importWindow.addKeyHandler(world.ml, "importWindowKey");
		importWindow.setActionOnClose(GWindow.KEEP_OPEN);

		int x = 0, y = iTopMargin * 2;
		world.ml.delay(delayAmount);
		lblImport = new GLabel(importWindow, x, y, importWindow.width, 22, "Select Media Folder(s) for Library");
		lblImport.setLocalColorScheme(G4P.SCHEME_10);
		lblImport.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
		lblImport.setTextAlign(GAlign.CENTER, null);
		lblImport.setTextBold();

		x = windowWidth * 3 / 2 - 160;
		y += 60;
		btnImportMediaFolder = new GButton(importWindow, x, y, 160, iLargeBoxHeight, "Add Folder");
		btnImportMediaFolder.tag = "AddMediaFolder";
		btnImportMediaFolder.setFont(new Font("Monospaced", Font.BOLD, iLargeTextSize));
		btnImportMediaFolder.setLocalColorScheme(G4P.CYAN_SCHEME);
		btnMakeLibrary = new GButton(importWindow, x+220, y, 100, iLargeBoxHeight, "Done");
		btnMakeLibrary.tag = "MakeLibrary";
		btnMakeLibrary.setFont(new Font("Monospaced", Font.BOLD, iLargeTextSize));
		btnMakeLibrary.setLocalColorScheme(G4P.CYAN_SCHEME);
		
		setupImportWindow = true;
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
			
			int leftEdge = world.ml.appWidth / 2 - windowWidth;
			int topEdge = world.ml.appHeight / 2 - listItemWindowHeight / 2;

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
			
			int leftEdge = world.ml.appWidth / 2 - windowWidth * 3 / 2;
			int topEdge = world.ml.appHeight / 2 - importWindowHeight / 2;

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
		
		int x = windowWidth * 3 / 2 - 80, y = 185;
		
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
					x = 60;
					for(String strFolder : display.ml.library.mediaFolders)
					{
						applet.text(strFolder, x, y);
						y += iButtonSpacingWide;
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
		y += iButtonSpacingWide * 1.5;
		
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

	/**
	 * Handles drawing to the Time Window 
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 */
	public void timeWindowDraw(PApplet applet, GWinData data) 
	{
		if(world.ml.state.running)
		{
//			float lineWidthVeryWide = 20f;
//			float lineWidthWide = 15f;
//			float lineWidth = 15f;

//			float mediumTextSize = 13.f;
			float smallTextSize = 11.f;

			float x = 10, y = 450;			
			applet.background(0);
			applet.stroke(255);
			applet.strokeWeight(1);
			applet.fill(255, 255, 255);

			applet.textSize(smallTextSize);
			
//			int mode = world.getState().getTimeMode();
//			if( mode == 0 || mode == 1 )
//			{
//				int curTime = (mode == 0) ? world.getCurrentCluster().getState().currentTime : world.getState().currentTime;
//				applet.text(" Current Time: "+ curTime, x, y += lineWidth);
//			}

			if(setupNavigationWindow)
			{
				if(world.state.timeFading && !world.state.paused)
					sdrCurrentTime.setValue(world.getCurrentTimePoint());
			}

//			applet.text(" Current Field Time: "+ world.currentTime, x, y += lineWidth);
//			applet.text(" Current Field Time Segment: "+ world.viewer.getCurrentFieldTimeSegment(), x, y += lineWidthVeryWide);
//			applet.text(" Current Field Timeline Size: "+ world.getCurrentField().getTimeline().timeline.size(), x, y += lineWidth);
//			applet.text(" Current Field Dateline Size: "+ world.getCurrentField().getDateline().size(), x, y += lineWidth);
		}
	}

	/**
	 * Handles drawing to the Navigation Window
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 */
	public void navigationWindowDraw(PApplet applet, GWinData data) 
	{
//		applet.background(10, 5, 50);
		applet.background(0);
		applet.stroke(155);
		applet.strokeWeight(1);
		applet.fill(155, 155, 155);

		applet.line(0, navigationWindowLineBreakY_1, windowWidth, navigationWindowLineBreakY_1);
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
	 * Handles drawing to the Graphics Window
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 */
	public void graphicsWindowDraw(PApplet applet, GWinData data) 
	{
		applet.background(0);
		applet.stroke(155);
		applet.strokeWeight(1);
		applet.fill(155, 155, 155);
	
		applet.line(0, graphicsWindowLineBreakY_1, windowWidth, graphicsWindowLineBreakY_1);
		applet.line(0, graphicsWindowLineBreakY_2, windowWidth, graphicsWindowLineBreakY_2);

		if(setupGraphicsWindow)
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
	public void graphicsWindowMouse(PApplet applet, GWinData data, MouseEvent event) 
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
			System.out.println("Mouse released:"+data.toString());
			break;
		case MouseEvent.DRAG:
			data2.ex = applet.mouseX;
			data2.ey = applet.mouseY;
//			System.out.println("Mouse dragged");
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

			float x = 50;
			float y = 50;			// Starting vertical position

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

				applet.textSize(fLargeTextSize);
				applet.text("Viewer", x, y);
//				applet.text("Viewer", x, y += lineWidthVeryWide * 1.5f);
				applet.textSize(fMediumTextSize);
				applet.text(" Location, x: "+PApplet.round(world.viewer.getLocation().x)+" y:"+PApplet.round(world.viewer.getLocation().y)+" z:"+
							  PApplet.round(world.viewer.getLocation().z), x, y += lineWidthVeryWide * 1.5f);		
				applet.text(" Approx. GPS Longitude: "+world.viewer.getGPSLocation().x+" Latitude:"+world.viewer.getGPSLocation().y, x, y += lineWidthVeryWide);		
				applet.text(" Field of View:"+world.viewer.getSettings().fieldOfView, x, y += lineWidthVeryWide);
				applet.text(" Clusters Visible: "+world.getVisibleClusters().size(), x, y += lineWidthVeryWide);
				applet.text(" Current Cluster #"+ (c.getID()+1)+" of "+ f.getClusters().size(), x, y += lineWidthVeryWide);
				applet.text(" 	Distance (m.): "+PVector.dist(world.viewer.getLocation(), world.getCurrentCluster().getLocation()), x, y += lineWidthVeryWide);
				if(world.state.timeMode == 0 && c.getDateline() != null)	// Cluster Time Mode
				{
					if(c.getDateline().size() > 0)
					{
						int clusterDate = world.getCurrentField().getTimeSegment(world.viewer.getCurrentFieldTimeSegment()).getClusterDateID();
						applet.text("   Current Cluster Time Segment ID: "+ (world.getCurrentField().getTimeSegment(world.viewer.getCurrentFieldTimeSegment()).getClusterTimelineID()+1)+"  of "+ c.getTimeline().timeline.size() +" in Cluster Main Timeline", x, y += lineWidthVeryWide);
						applet.text("   Date: "+ (clusterDate+1) +" of "+ c.getDateline().size(), x, y += lineWidth);
					}
				}

				applet.textSize(fLargeTextSize);
				applet.text("World", x, y += lineWidthVeryWide * 1.5f);
				applet.textSize(fMediumTextSize);
				if(world.getFieldCount() > 1)
					applet.text(" Fields in World: "+world.getFields().size(), x, y += lineWidthVeryWide * 1.5f);
				applet.text(" Current Field #"+ (f.getID()+1)+" of "+ world.getFields().size(), x, y += lineWidthVeryWide);
				applet.text(" Width: "+f.getModel().getState().fieldWidth+" Length: "+f.getModel().getState().fieldLength+" Height: "+f.getModel().getState().fieldHeight, x, y += lineWidthVeryWide);
				applet.text(" Clusters in Field: "+f.getClusters().size(), x, y += lineWidthVeryWide);
				applet.text(" Media Density (per sq. m.): "+f.getModel().getState().mediaDensity, x, y += lineWidthVeryWide);
			
				if(world.state.timeMode == 1 && f.getDateline().size() > 0)	// Field Time Mode
				{
					int fieldDate = world.getCurrentField().getTimeSegment(world.viewer.getCurrentFieldTimeSegment()).getFieldDateID();
					applet.text("   Current Field Time Segment ID: "+ world.viewer.getCurrentFieldTimeSegment()+" of "+ world.getCurrentField().getTimeline().timeline.size() +" in Main Timeline", x, y += lineWidthVeryWide);
					applet.text("   Date: "+ (fieldDate)+" of "+ world.getCurrentField().getDateline().size(), x, y += lineWidth);
//					applet.text("   Date-Specific ID: "+ world.getCurrentField().getTimeSegment(world.viewer.getCurrentFieldTimeSegment()).getFieldTimelineIDOnDate()
//							+" of "+ world.getCurrentField().getTimelines().get(fieldDate).timeline.size() + " in Timeline #"+(fieldDate), x, y += lineWidth);
				}
				
				if(f.getImageCount() > 0) applet.text(" Images: "+f.getImageCount(), x, y += lineWidthVeryWide);			
				if(f.getImagesVisible() > 0) 
				{
					applet.text("   In Visible Range: "+f.getImagesVisible(), x, y += lineWidthVeryWide);
					applet.text("   Seen: "+f.getImagesSeen(), x, y += lineWidthVeryWide);
				}
				
				if(f.getPanoramaCount() > 0) applet.text(" Panoramas: "+f.getPanoramaCount(), x, y += lineWidthVeryWide);		
				if(f.getPanoramasVisible() > 0)
				{
					applet.text("   In Visible Range: "+f.getPanoramasVisible(), x, y += lineWidthVeryWide);
					applet.text("   Seen: "+f.getPanoramasSeen(), x, y += lineWidthVeryWide);
				}

				if(f.getVideoCount() > 0) applet.text(" Videos: "+f.getVideoCount(), x, y += lineWidthVeryWide);					
				if(f.getVideosVisible() > 0)
				{
					applet.text("   In Visible Range: "+f.getVideosVisible(), x, y += lineWidthVeryWide);
					if(f.getVideosPlaying() > 0)
					{
						applet.text("   Playing: "+f.getVideosPlaying(), x, y += lineWidthVeryWide);
						if(f.getVideosSeen() > 0) applet.text("   Seen: "+f.getVideosSeen(), x, y += lineWidthVeryWide);
					}
				}
				
				if(f.getSoundCount() > 0) applet.text(" Sounds: "+f.getSoundCount(), x, y += lineWidthVeryWide);					
				if(f.getSoundsAudible() > 0)
				{
					applet.text(" In Audible Range: "+f.getSoundsAudible(), x, y += lineWidthVeryWide);
					if(f.getSoundsPlaying() > 0) 
						applet.text("   Playing: "+f.getSoundsPlaying(), x, y += lineWidthVeryWide);
					applet.text("   Heard: "+f.getSoundsHeard(), x, y += lineWidthVeryWide);
				}
				
				applet.textSize(fLargeTextSize);
				applet.text("Output", x, y += lineWidthVeryWide * 1.5f);
				applet.textSize(fMediumTextSize);
				applet.text(" Media Output Folder:"+world.outputFolder, x, y += lineWidthVeryWide * 1.5f);

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
	public void statisticsWindowMouse(PApplet applet, GWinData data, MouseEvent event) {
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
	public void memoryWindowDraw(PApplet applet, GWinData data) 
	{
		applet.background(0,0,0);
		applet.stroke(255);
		applet.strokeWeight(1);
		applet.fill(255, 255, 255);

		float x = 10;
		float y = 150;			// Starting vertical position

		applet.fill(255, 255, 255, 255);                        
		
		applet.textSize(fMediumTextSize);
		if(world.viewer.isFollowing()) applet.text("<< Currently Following Path >>", x, y += lineWidthVeryWide);

		x = 90;	
		y += lineWidthVeryWide;
		applet.text("Points in Memory: "+world.viewer.getMemoryPath().size(), x, y += lineWidthVeryWide);
		
		y += lineWidthWide * 2.f;

//		applet.textSize(mediumTextSize);
//		applet.text(" Memory", xPos, yPos += lineWidthVeryWide);
//		applet.textSize(fMediumTextSize);
//		applet.text(" Keyboard Controls ", xPos, yPos);
//		applet.textSize(fSmallTextSize);
//		applet.text(" `    Save Current View to Memory", xPos, yPos += lineWidthVeryWide);
//		applet.text(" ~    Follow Memory Path", xPos, yPos += lineWidth);
//		applet.text(" Y    Clear Memory", xPos, yPos += lineWidth);
	}

	/**
	 * Handles mouse events for Memory Window 
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void memoryWindowMouse(PApplet applet, GWinData data, MouseEvent event) {
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
//	public void showTimeWindow()
//	{
//		showTimeWindow = true;
//		if(setupTimeWindow)
//			timeWindow.setVisible(true);
//		if(showMLWindow)
//			hideMLWindow();
//	}
	public void showGraphicsWindow()
	{
		showGraphicsWindow = true;
		if(setupGraphicsWindow)
			graphicsWindow.setVisible(true);
		if(showMLWindow)
			hideMLWindow();
	}
//	public void showModelWindow()
//	{
//		showModelWindow = true;
//		if(setupModelWindow)
//			modelWindow.setVisible(true);
//		if(showMLWindow)
//			hideMLWindow();
//	}
//	public void showMemoryWindow()
//	{
//		showMemoryWindow = true;
//		if(setupMemoryWindow)
//			memoryWindow.setVisible(true);
//		if(showMLWindow)
//			hideMLWindow();
//	}
//	public void showSelectionWindow()
//	{
//		showSelectionWindow = true;
//		if(setupSelectionWindow)
//			selectionWindow.setVisible(true);
//		if(showMLWindow)
//			hideMLWindow();
//	} 
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
	public void showLibraryWindow()
	{
		showLibraryWindow = true;
		if(setupLibraryWindow)
			libraryWindow.setVisible(true);
		if(showMLWindow)
			hideMLWindow();
	}
	public void showImportWindow()
	{
		showImportWindow = true;
		if(setupImportWindow)
			importWindow.setVisible(true);
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
	public void hideGraphicsWindow()
	{
		showGraphicsWindow = false;
		if(setupGraphicsWindow)
			graphicsWindow.setVisible(false);
	}
	public void closeGraphicsWindow()
	{
		showGraphicsWindow = false;
		if(setupGraphicsWindow)
		{
			graphicsWindow.setVisible(false);
			graphicsWindow.close();
			graphicsWindow.dispose();
			setupGraphicsWindow = false;
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
	public void hideLibraryWindow()
	{
		showLibraryWindow = false;
		if(setupLibraryWindow)
			libraryWindow.setVisible(false);
	} 
	public void hideImportWindow()
	{
		showImportWindow = false;
		if(setupImportWindow)
			importWindow.setVisible(false);
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
		if(showGraphicsWindow)
			hideGraphicsWindow();
		if(showStatisticsWindow)
			hideStatisticsWindow();
		if(showHelpWindow)
			hideHelpWindow();
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