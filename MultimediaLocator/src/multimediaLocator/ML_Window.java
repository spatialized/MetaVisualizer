package multimediaLocator;

import java.awt.Font;

import g4p_controls.*;
import processing.core.PApplet;
import processing.event.MouseEvent;

/**
 * Handles secondary program windows
 * @author davidgordon
 */
public class ML_Window {

	ML_Display display;
	WMV_Utilities utilities;
	
	private boolean delay = true;
	private int delayAmount = 180;
	
	private int windowWidth = 310, longWindowHeight = 600;
	private int shortWindowHeight = 340;
	
	/* Windows */
	public GWindow mlWindow, timeWindow, navigationWindow, graphicsWindow, modelWindow, selectionWindow, statisticsWindow, helpWindow,
				   memoryWindow, libraryWindow, importWindow;
	private GLabel lblMainMenu, lblNavigationWindow, lblGraphics, lblStatistics, lblHelp, lblMemory, lblLibrary, lblImport;	// Window headings
	public boolean showMLWindow = false, showNavigationWindow = false, showTimeWindow = false, showGraphicsWindow = false, showModelWindow = false,
				    showSelectionWindow = false, showStatisticsWindow = false, showHelpWindow = false, showMemoryWindow = false;
	public boolean setupTimeWindow = false, setupNavigationWindow = false, setupGraphicsWindow = false, setupModelWindow = false, setupHelpWindow = false, 
					setupSelectionWindow = false, setupStatisticsWindow = false, setupMemoryWindow = false;
	public boolean setupLibraryWindow = false, showLibraryWindow = false;
	public boolean setupImportWindow = false, showImportWindow = false;
	
	/* Margins */
	private int iRightMargin = 15;
	private int iBottomMargin = 25;
	private int iTopMargin = 15;
	private int iLineSpacing = 30, iLineSpacingSmall = 25;
	
	/* Sizing */
	private int iSmallBoxHeight = 20;
	private int iLargeBoxHeight = 40;

	/* Text */
	int iLargeTextSize = 18;
	int iMediumTextSize = 16;
	int iSmallTextSize = 14;
	int iVerySmallTextSize = 12;
	
	float fLargeTextSize = 18.f;
	float fMediumTextSize = 16.f;
	float fSmallTextSize = 14.f;
	
	/* Main Window */
	private GButton btnNavigationWindow, btnTimeWindow, btnGraphicsWindow, btnModelWindow, btnSelectionWindow,
				    btnStatisticsWindow, btnHelpWindow, btnMemoryWindow;
	private GButton btnLoadMediaLibrary;
	private GLabel lblSpaceBar;
	public GToggleGroup tgDisplayView;	
	public GOption optWorldView, optMapView, optTimelineView;
	int mlWindowHeight;
	
	/* Library Window */
	private GButton btnCreateLibrary, btnOpenLibrary;
	int libraryWindowHeight;
	
	/* Import Window */
	private GButton btnImportMediaFolder, btnMakeLibrary;
	int importWindowHeight;
	
	/* Navigation Window */
	private GLabel lblClusterNavigation, lblMemoryCommands, lblPathNavigation, lblTeleportLength, lblPathWaitLength;
//	private GButton btnImportGPSTrack;
	private GButton btnSaveLocation, btnClearMemory;
	private GButton btnJumpToRandomCluster;
	private GButton btnZoomIn, btnZoomOut;
	private GButton btnSaveImage, btnOutputFolder;
	public GSlider sdrTeleportLength, sdrPathWaitLength;

	private GButton btnNextTimeSegment, btnPreviousTimeSegment;
	private GButton btnMoveToNearestCluster;
	private GButton btnGoToPreviousField, btnGoToNextField;
	private GButton btnFollowStart, btnFollowStop;	
	public GLabel lblCommand1;
	int navigationWindowHeight;
	
	/* Time Window */
	private GLabel lblTimeWindow, lblTimeCycle, lblTimeMode;
	public GLabel lblMediaLength, lblTimeCycleLength, lblCurrentTime, lblVisibleInterval;
//	public GLabel lblCurrentTime;
	public GLabel lblTime;
	public GCheckbox chkbxPaused, chkbxTimeFading;
	public GSlider sdrMediaLength, sdrTimeCycleLength, sdrCurrentTime, sdrVisibleInterval;
	public GToggleGroup tgTimeMode;	
	public GOption optClusterTimeMode, optFieldTimeMode; //, optMediaTimeMode;
	public GLabel lblCommand2;
	int timeWindowHeight;

	/* Graphics Window */
	public GLabel lblGraphicsModes, lblOutput, lblZoom;
	public GOption optTimeline, optGPSTrack, optMemory;
	public GToggleGroup tgFollow;	
	public GCheckbox chkbxMovementTeleport, chkbxFollowTeleport;
	public GCheckbox chkbxFadeEdges;
	private GLabel lblHideMedia;
	public GCheckbox chkbxHideImages, chkbxHideVideos, chkbxHidePanoramas;
	public GCheckbox chkbxAlphaMode;
	public GCheckbox chkbxOrientationMode;
	public GCheckbox chkbxAngleFading, chkbxAngleThinning;

	private GLabel lblAlpha, lblBrightness;
	public GSlider sdrAlpha, sdrBrightness;//, sdrMediaSize;

	private GLabel lblSubjectDistance;
	private GButton btnSubjectDistanceUp, btnSubjectDistanceDown;
	public GLabel lblCommand3;
	int graphicsWindowHeight;

	/* Model Window */
	private GLabel lblModelWindow, lblAltitudeScaling, lblModelSettings, lblModelDisplay;
	public GCheckbox chkbxShowModel, chkbxMediaToCluster, chkbxCaptureToMedia, chkbxCaptureToCluster;
	public GSlider sdrAltitudeScaling;
	public GLabel lblCommand4;
	int modelWindowHeight;
	
	/* Memory Window */
	public GLabel lblCommand5;
	int memoryWindowHeight;
	
	/* Selection Window */
	private GLabel lblSelection, lblSelectionOptions;
	public GCheckbox chkbxSelectionMode, chkbxMultiSelection, chkbxSegmentSelection, chkbxShowMetadata;
	private GButton btnSelectFront, btnDeselectFront, btnDeselectAll, btnStitchPanorama;
	public GLabel lblCommand6;
	int selectionWindowHeight;

	/* Statistics Window */
	public GLabel lblCommand7;
	int statisticsWindowHeight;

	/* Help Window */
	int helpWindowHeight;
	public GLabel lblShift8;
	public boolean backgroundText = false;							/* 0: Scene  1: Map  2: Library  3: Timeline */

	private String windowTitle = " ";
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
		
		mlWindowHeight = shortWindowHeight;
		libraryWindowHeight = shortWindowHeight / 2;
		importWindowHeight = shortWindowHeight;

		navigationWindowHeight = longWindowHeight;
		timeWindowHeight = longWindowHeight;
		graphicsWindowHeight = longWindowHeight;
		modelWindowHeight = shortWindowHeight + 100;
		memoryWindowHeight = shortWindowHeight;
		selectionWindowHeight = shortWindowHeight;
		statisticsWindowHeight = longWindowHeight + 100;
		helpWindowHeight = longWindowHeight;
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

	public void openTimeWindow()
	{
		if(!setupTimeWindow) setupTimeWindow();
		showTimeWindow();
	}

	public void openModelWindow()
	{
		if(!setupModelWindow) setupModelWindow();
		showModelWindow();
	}

	public void openMemoryWindow()
	{
		if(!setupMemoryWindow) setupMemoryWindow();
		showMemoryWindow();
	}

	public void openStatisticsWindow()
	{
		if(!setupStatisticsWindow)
			setupStatisticsWindow();
		showStatisticsWindow();
	}
	
	public void openSelectionWindow()
	{
		if(!setupSelectionWindow)
			setupSelectionWindow();
		showSelectionWindow();
	}

	public void openHelpWindow()
	{
		if(!setupHelpWindow)
			setupHelpWindow();
		showHelpWindow();
	}

	public void setupMLWindow()
	{
		mlWindow = GWindow.getWindow(world.ml, windowTitle, 10, 45, windowWidth, mlWindowHeight, PApplet.JAVA2D);
		mlWindow.addData(new ML_WinData());
		mlWindow.addDrawHandler(this, "mlWindowDraw");
		mlWindow.addMouseHandler(this, "mlWindowMouse");
		mlWindow.addKeyHandler(world.ml, "mlWindowKey");
		mlWindow.setActionOnClose(GWindow.KEEP_OPEN);
		hideMLWindow();
		
		if(delay) world.ml.delay(delayAmount);
		
		int x = 0, y = iTopMargin;
		lblMainMenu = new GLabel(mlWindow, x, y, mlWindow.width, iSmallBoxHeight, "Main Menu");
		lblMainMenu.setLocalColorScheme(10);
		lblMainMenu.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblMainMenu.setTextAlign(GAlign.CENTER, null);
		lblMainMenu.setTextBold();

		x = iRightMargin;
		y += iLineSpacing;
		
		if(delay) world.ml.delay(delayAmount);

		optWorldView = new GOption(mlWindow, x, y, 90, iSmallBoxHeight, "World (1)");
		optWorldView.setLocalColorScheme(10);
		optWorldView.tag = "SceneView";
		optMapView = new GOption(mlWindow, x+=110, y, 90, iSmallBoxHeight, "Map (2)");
		optMapView.setLocalColorScheme(10);
		optMapView.tag = "MapView";
		optTimelineView = new GOption(mlWindow, x+=110, y, 100, iSmallBoxHeight, "Timeline (3)");
		optTimelineView.setLocalColorScheme(10);
		optTimelineView.tag = "TimelineView";

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
		y += iLineSpacing;

		btnLoadMediaLibrary = new GButton(mlWindow, x, y, 180, iSmallBoxHeight, "Load Media Library  ⇧R");
		btnLoadMediaLibrary.tag = "Restart";
		btnLoadMediaLibrary.setLocalColorScheme(7);

		x = 90;
		y += iLineSpacing;

		btnNavigationWindow = new GButton(mlWindow, x, y, 125, iSmallBoxHeight, "Navigation  ⇧1");
		btnNavigationWindow.tag = "OpenNavigationWindow";
		btnNavigationWindow.setLocalColorScheme(5);
		
		y += iLineSpacingSmall;
		btnTimeWindow = new GButton(mlWindow, x, y, 125, iSmallBoxHeight, "Time  ⇧2");
		btnTimeWindow.tag = "OpenTimeWindow";
		btnTimeWindow.setLocalColorScheme(5);

		y += iLineSpacingSmall;

		btnGraphicsWindow = new GButton(mlWindow, x, y, 125, iSmallBoxHeight, "Graphics  ⇧3");
		btnGraphicsWindow.tag = "OpenGraphicsWindow";
		btnGraphicsWindow.setLocalColorScheme(5);
		
		y += iLineSpacingSmall;
		
		btnModelWindow = new GButton(mlWindow, x, y, 125, iSmallBoxHeight, "Model  ⇧4");
		btnModelWindow.tag = "OpenModelWindow";
		btnModelWindow.setLocalColorScheme(5);
		
		y += iLineSpacingSmall;
		
		btnSelectionWindow = new GButton(mlWindow, x, y, 125, iSmallBoxHeight, "Selection  ⇧5");
		btnSelectionWindow.tag = "OpenSelectionWindow";
		btnSelectionWindow.setLocalColorScheme(5);

		y += iLineSpacingSmall;

		btnMemoryWindow = new GButton(mlWindow, x, y, 125, iSmallBoxHeight, "Memory  ⇧6");
		btnMemoryWindow.tag = "OpenMemoryWindow";
		btnMemoryWindow.setLocalColorScheme(5);
		
		y += iLineSpacingSmall;
		
		btnStatisticsWindow = new GButton(mlWindow, x, y, 125, iSmallBoxHeight, "Statistics  ⇧7");
		btnStatisticsWindow.tag = "OpenStatisticsWindow";
		btnStatisticsWindow.setLocalColorScheme(5);
		
		y += iLineSpacingSmall;
		
		btnHelpWindow = new GButton(mlWindow, x, y, 125, iSmallBoxHeight, "Help  ⇧8");
		btnHelpWindow.tag = "OpenHelpWindow";
		btnHelpWindow.setLocalColorScheme(5);

		x = 0;
		y = mlWindowHeight - iBottomMargin;
		lblSpaceBar = new GLabel(mlWindow, x, y, mlWindow.width, iSmallBoxHeight);						/* Display Mode Label */
		lblSpaceBar.setText("Press SPACEBAR to show / hide");
		lblSpaceBar.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		lblSpaceBar.setLocalColorScheme(10);
		lblSpaceBar.setTextAlign(GAlign.CENTER, null);
		world.ml.setAppIcon = true;
	}

	/**
	 * Setup the Navigation Window
	 */
	public void setupNavigationWindow()
	{
		navigationWindow = GWindow.getWindow(world.ml, windowTitle, 10, 45, windowWidth, navigationWindowHeight, PApplet.JAVA2D);
		navigationWindow.setVisible(true);
		
		navigationWindow.addData(new ML_WinData());
		navigationWindow.addDrawHandler(this, "navigationWindowDraw");
		navigationWindow.addMouseHandler(this, "navigationWindowMouse");
		navigationWindow.addKeyHandler(world.ml, "navigationWindowKey");
		navigationWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		int x = 0, y = iTopMargin;
		if(delay) world.ml.delay(delayAmount);

		lblNavigationWindow = new GLabel(navigationWindow, x, y, navigationWindow.width, iSmallBoxHeight, "Navigation");
		lblNavigationWindow.setLocalColorScheme(10);
		lblNavigationWindow.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblNavigationWindow.setTextAlign(GAlign.CENTER, null);
		lblNavigationWindow.setTextBold();

		x = 0;
		y += iLineSpacing;

		lblClusterNavigation = new GLabel(navigationWindow, x, y, navigationWindow.width, iSmallBoxHeight, "Cluster Navigation");
		lblClusterNavigation.setLocalColorScheme(10);
		lblClusterNavigation.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		lblClusterNavigation.setTextAlign(GAlign.CENTER, null);
		lblClusterNavigation.setTextBold();

		x = 120;
		y += iLineSpacing;
		
		chkbxMovementTeleport = new GCheckbox(navigationWindow, x, y, 70, iSmallBoxHeight, "Teleport");
		chkbxMovementTeleport.tag = "MovementTeleport";
		chkbxMovementTeleport.setLocalColorScheme(10);

		x = 40;
		y += iLineSpacingSmall;

		btnMoveToNearestCluster = new GButton(navigationWindow, x, y, 100, iSmallBoxHeight, "Nearest  (m)");
		btnMoveToNearestCluster.tag = "NearestCluster";
		btnMoveToNearestCluster.setLocalColorScheme(5);
		btnMoveToNearestCluster = new GButton(navigationWindow, x+110, y, 100, iSmallBoxHeight, "Last  (l)");
		btnMoveToNearestCluster.tag = "LastCluster";
		btnMoveToNearestCluster.setLocalColorScheme(5);

		x = 30;
		y += iLineSpacingSmall;

		btnPreviousTimeSegment = new GButton(navigationWindow, x, y, 120, iSmallBoxHeight, "Previous Time  (b)");
		btnPreviousTimeSegment.tag = "PreviousTime";
		btnPreviousTimeSegment.setLocalColorScheme(5);
		btnNextTimeSegment = new GButton(navigationWindow, x+=125, y, 100, iSmallBoxHeight, "Next Time  (n)");
		btnNextTimeSegment.tag = "NextTime";
		btnNextTimeSegment.setLocalColorScheme(5);
		
		x = 25;
		y += iLineSpacingSmall;
//		if(delay) world.p.delay(delayAmount);
		
		btnJumpToRandomCluster = new GButton(navigationWindow, x+75, y, 110, iSmallBoxHeight, "Random  (j)");
		btnJumpToRandomCluster.tag = "RandomCluster";
		btnJumpToRandomCluster.setLocalColorScheme(5);
		
		x = 0;
		y += iLineSpacing;
//		if(delay) world.p.delay(delayAmount);

		lblPathNavigation = new GLabel(navigationWindow, x, y, navigationWindow.width, iSmallBoxHeight, "Path Navigation");
		lblPathNavigation.setLocalColorScheme(10);
		lblPathNavigation.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		lblPathNavigation.setTextAlign(GAlign.CENTER, null);
		lblPathNavigation.setTextBold();

		x = 120;
		y += iLineSpacingSmall;

		chkbxFollowTeleport = new GCheckbox(navigationWindow, x, y, 80, iSmallBoxHeight, "Teleport");
		chkbxFollowTeleport.tag = "FollowTeleport";
		chkbxFollowTeleport.setLocalColorScheme(10);

		x = iRightMargin;
		y += iLineSpacingSmall;
	
		optTimeline = new GOption(navigationWindow, x, y, 90, iSmallBoxHeight, "Timeline");
		optTimeline.setLocalColorScheme(10);
		optTimeline.tag = "FollowTimeline";
		optTimeline.setSelected(true);
		optGPSTrack = new GOption(navigationWindow, x+=90, y, 90, iSmallBoxHeight, "GPS Track");
		optGPSTrack.setLocalColorScheme(10);
		optGPSTrack.tag = "FollowGPSTrack";
		optMemory = new GOption(navigationWindow, x+=90, y, 90, iSmallBoxHeight, "Memory");
		optMemory.setLocalColorScheme(10);
		optMemory.tag = "FollowMemory";

		tgFollow = new GToggleGroup();
		tgFollow.addControls(optTimeline, optGPSTrack, optMemory);

		x = 95;
		y += iLineSpacingSmall;

		btnFollowStart = new GButton(navigationWindow, x, y, 60, iSmallBoxHeight, "Start");
		btnFollowStart.tag = "FollowStart";
		btnFollowStart.setLocalColorScheme(5);
		
		btnFollowStop = new GButton(navigationWindow, x+=60, y, 60, iSmallBoxHeight, "Stop");
		btnFollowStop.tag = "FollowStop";
		btnFollowStop.setLocalColorScheme(0);

		x = 130;
		y += iLineSpacing;

		sdrTeleportLength = new GSlider(navigationWindow, x, y, 80, 80, iSmallBoxHeight);
		sdrTeleportLength.setLocalColorScheme(7);
		sdrTeleportLength.setLimits(0.f, 300.f, 10.f);
		sdrTeleportLength.setValue(world.viewer.getSettings().teleportLength);
		sdrTeleportLength.setRotation(PApplet.PI/2.f);
		sdrTeleportLength.setTextOrientation(G4P.ORIENT_LEFT);
		sdrTeleportLength.setEasing(0);
		sdrTeleportLength.setShowValue(true);
		sdrTeleportLength.tag = "TeleportLength";

		x = 280;

		sdrPathWaitLength = new GSlider(navigationWindow, x, y, 80, 80, iSmallBoxHeight);
		sdrPathWaitLength.setLocalColorScheme(7);
		sdrPathWaitLength.setLimits(0.f, 600.f, 30.f);
		sdrPathWaitLength.setValue(world.viewer.getSettings().pathWaitLength);
		sdrPathWaitLength.setRotation(PApplet.PI/2.f);
		sdrPathWaitLength.setTextOrientation(G4P.ORIENT_LEFT);
		sdrPathWaitLength.setEasing(0);
		sdrPathWaitLength.setShowValue(true);
		sdrPathWaitLength.tag = "PathWaitLength";
		
		x = iRightMargin;
		y += iLineSpacing;
		
		lblTeleportLength = new GLabel(navigationWindow, x, y, 100, iSmallBoxHeight, "Teleport Time");
		lblTeleportLength.setLocalColorScheme(10);

		x = 165;
		
		lblPathWaitLength = new GLabel(navigationWindow, x, y, 100, iSmallBoxHeight, "Wait Time");
		lblPathWaitLength.setLocalColorScheme(10);

		x = 0;
		y += iLineSpacing;

		lblMemoryCommands = new GLabel(navigationWindow, x, y, navigationWindow.width, 20, "Memory");
		lblMemoryCommands.setLocalColorScheme(10);
		lblMemoryCommands.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		lblMemoryCommands.setTextAlign(GAlign.CENTER, null);
		lblMemoryCommands.setTextBold();

		x = 45;
		y += iLineSpacing;
		
		btnSaveLocation = new GButton(navigationWindow, x, y, 110, iSmallBoxHeight, "Save Location  (`)");
		btnSaveLocation.tag = "SaveLocation";
		btnSaveLocation.setLocalColorScheme(5);
		btnClearMemory = new GButton(navigationWindow, x+110, y, 100, iSmallBoxHeight, "Clear Memory");
		btnClearMemory.tag = "ClearMemory";
		btnClearMemory.setLocalColorScheme(0);

		if(world.getFields() != null)
		{
			x = 40;
			y += iLineSpacing;
			
			btnGoToPreviousField = new GButton(navigationWindow, x, y, 120, iSmallBoxHeight, "Previous Field  ⇧[");
			btnGoToPreviousField.tag = "PreviousField";
			btnGoToPreviousField.setLocalColorScheme(5);

			btnGoToNextField = new GButton(navigationWindow, x+=125, y, 100, iSmallBoxHeight, "Next Field  ⇧]");
			btnGoToNextField.tag = "NextField";
			btnGoToNextField.setLocalColorScheme(5);
		}
		
		x = 0;
		y = navigationWindowHeight - iBottomMargin;

		lblCommand1 = new GLabel(navigationWindow, x, y, navigationWindow.width, iSmallBoxHeight);						/* Display Mode Label */
		lblCommand1.setText("Press ⇧1 to show / hide");
		lblCommand1.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		lblCommand1.setLocalColorScheme(10);
		lblCommand1.setTextAlign(GAlign.CENTER, null);

		setupNavigationWindow = true;
		world.ml.setAppIcon = true;
	}

	/**
	 * Setup the Time Window
	 */
	public void setupTimeWindow()
	{
		timeWindow = GWindow.getWindow(world.ml, windowTitle, 10, 45, windowWidth, timeWindowHeight, PApplet.JAVA2D);
		timeWindow.setVisible(true);
		timeWindow.addData(new ML_WinData());
		timeWindow.addDrawHandler(this, "timeWindowDraw");
		timeWindow.addMouseHandler(this, "timeWindowMouse");
		timeWindow.addKeyHandler(world.ml, "timeWindowKey");
		timeWindow.setActionOnClose(GWindow.KEEP_OPEN);

		int x = 0, y = iTopMargin;
		if(delay) world.ml.delay(delayAmount);

		lblTimeWindow = new GLabel(timeWindow, x, y, timeWindow.width, iSmallBoxHeight, "Time");
		lblTimeWindow.setLocalColorScheme(10);
		lblTimeWindow.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblTimeWindow.setTextAlign(GAlign.CENTER, null);
		lblTimeWindow.setTextBold();

		x = 0;
		y += iLineSpacing;
		lblTimeMode = new GLabel(timeWindow, x, y, timeWindow.width, iSmallBoxHeight, "Time Mode");
		lblTimeMode.setLocalColorScheme(10);
		lblTimeMode.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblTimeMode.setTextAlign(GAlign.CENTER, null);
		lblTimeMode.setTextBold();

		x = 70;
		y += iLineSpacing;
//		optMediaTimeMode = new GOption(timeWindow, x, y, 90, 20, "Media");
//		optMediaTimeMode.setLocalColorScheme(10);
//		optMediaTimeMode.tag = "MediaTimeMode";
		optClusterTimeMode = new GOption(timeWindow, x, y, 90, 20, "Cluster");
		optClusterTimeMode.setLocalColorScheme(10);
		optClusterTimeMode.setFont(new Font("Monospaced", Font.PLAIN, 13));
		optClusterTimeMode.tag = "ClusterTimeMode";
		optFieldTimeMode = new GOption(timeWindow, x+=110, y, 90, 20, "Field");
		optFieldTimeMode.setLocalColorScheme(10);
		optFieldTimeMode.setFont(new Font("Monospaced", Font.PLAIN, 13));
		optFieldTimeMode.tag = "FieldTimeMode";

		switch(world.getState().getTimeMode())
		{
			case 0:
				optClusterTimeMode.setSelected(true);
				optFieldTimeMode.setSelected(false);
//				optMediaTimeMode.setSelected(false);
				break;
			case 1:
				optClusterTimeMode.setSelected(false);
				optFieldTimeMode.setSelected(true);
//				optMediaTimeMode.setSelected(false);
				break;
//			case 2:
//				optClusterTimeMode.setSelected(false);
//				optFieldTimeMode.setSelected(false);
////				optMediaTimeMode.setSelected(true);
//				break;
		}
		
		tgTimeMode = new GToggleGroup();
		tgTimeMode.addControls(optClusterTimeMode, optFieldTimeMode);
//		tgTimeMode.addControls(optClusterTimeMode, optFieldTimeMode, optMediaTimeMode);
		if(delay) world.ml.delay(delayAmount);

		x = 0;
		y += 50;
		lblTimeCycle = new GLabel(timeWindow, x, y, timeWindow.width, iSmallBoxHeight, "Time Cycle");
		lblTimeCycle.setLocalColorScheme(10);
		lblTimeCycle.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblTimeCycle.setTextAlign(GAlign.CENTER, null);
		lblTimeCycle.setTextBold();

		x = 80;
		y += iLineSpacing;
		sdrTimeCycleLength = new GSlider(timeWindow, x, y, 160, 80, 20);
		sdrTimeCycleLength.setLocalColorScheme(7);
		sdrTimeCycleLength.setLimits(0.f, world.settings.timeCycleLength, 3200.f);
		sdrTimeCycleLength.setTextOrientation(G4P.ORIENT_TRACK);
		sdrTimeCycleLength.setEasing(0);
		sdrTimeCycleLength.setShowValue(true);
		sdrTimeCycleLength.tag = "TimeCycleLength";

		x = 130;
	
		lblTimeCycleLength = new GLabel(timeWindow, x, y, 120, iSmallBoxHeight, "Cycle Length");
		lblTimeCycleLength.setLocalColorScheme(10);

		x = 80;
		y += 60;
		sdrCurrentTime = new GSlider(timeWindow, x, y, 160, 80, 20);
		sdrCurrentTime.setLocalColorScheme(1);
		sdrCurrentTime.setLimits(0.f, 0.f, 1.f);
		sdrCurrentTime.setValue(0.f);
		sdrCurrentTime.setTextOrientation(G4P.ORIENT_TRACK);
		sdrCurrentTime.setEasing(0);
		sdrCurrentTime.setShowValue(true);
		sdrCurrentTime.tag = "CurrentTime";

		x = 130;
		lblCurrentTime = new GLabel(timeWindow, x, y, 100, iSmallBoxHeight, "Current Time");
		lblCurrentTime.setLocalColorScheme(10);

		x = 80;
		y += 60;
		sdrMediaLength = new GSlider(timeWindow, x, y, 160, 80, 20);
		sdrMediaLength.setLocalColorScheme(5);
		sdrMediaLength.setLimits(world.settings.defaultMediaLength, 0.f, 250.f);	// setLimits (int initValue, int start, int end)
		sdrMediaLength.setValue(world.settings.defaultMediaLength);
		sdrMediaLength.setTextOrientation(G4P.ORIENT_TRACK);
		sdrMediaLength.setEasing(0);
		sdrMediaLength.setShowValue(true);
		sdrMediaLength.tag = "MediaLength";

		x = 130;
		lblMediaLength = new GLabel(timeWindow, x, y, 100, iSmallBoxHeight, "Media Length");
		lblMediaLength.setLocalColorScheme(10);

		x = 80;
		y += 60;
		sdrVisibleInterval = new GSlider(timeWindow, x, y, 160, 80, 20);
		sdrVisibleInterval.setLocalColorScheme(4);
		sdrVisibleInterval.setLimits(world.settings.timeVisibleInterval, 0.f, 1.f);
		sdrVisibleInterval.setValue(world.settings.timeVisibleInterval);
		sdrVisibleInterval.setTextOrientation(G4P.ORIENT_TRACK);
		sdrVisibleInterval.setEasing(0);
		sdrVisibleInterval.setShowValue(true);
		sdrVisibleInterval.tag = "VisibleTimeInterval";
	
		x = 130;
		lblVisibleInterval = new GLabel(timeWindow, x, y, 120, iSmallBoxHeight, "Visible Interval");
		lblVisibleInterval.setLocalColorScheme(10);

		switch(world.state.timeMode)
		{
			case 0:											// Cluster
				sdrVisibleInterval.setValue(world.getCurrentCluster().getTimeCycleLength());
				if(sdrVisibleInterval.isVisible())
					sdrVisibleInterval.setVisible(false);
				if(lblVisibleInterval.isVisible())
					lblVisibleInterval.setVisible(false);
				break;
			case 1:											// Field
				sdrVisibleInterval.setValue(world.settings.timeCycleLength);
				if(!sdrVisibleInterval.isVisible())
					sdrVisibleInterval.setVisible(true);
				if(!lblVisibleInterval.isVisible())
					lblVisibleInterval.setVisible(true);
				break;
			default:
				break;
		}

		x = 100;
		y += 75;
		
		chkbxTimeFading = new GCheckbox(timeWindow, x, y, 160, iSmallBoxHeight, "Run Cycle  ⇧T");
		chkbxTimeFading.tag = "TimeFading";
		chkbxTimeFading.setLocalColorScheme(10);
		chkbxTimeFading.setSelected(world.getState().timeFading);

		y += iLineSpacing;
		
		chkbxPaused = new GCheckbox(timeWindow, x, y, 160, iSmallBoxHeight, "Pause  -");
		chkbxPaused.tag = "Paused";
		chkbxPaused.setLocalColorScheme(10);
		chkbxPaused.setSelected(world.getState().paused);

		x = 0;
		y = timeWindowHeight - iBottomMargin;
		lblCommand2 = new GLabel(timeWindow, x, y, timeWindow.width, iSmallBoxHeight);						/* Display Mode Label */
		lblCommand2.setText("Press ⇧2 to show / hide");
		lblCommand2.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		lblCommand2.setLocalColorScheme(10);
		lblCommand2.setTextAlign(GAlign.CENTER, null);

		setupTimeWindow = true;
		world.ml.setAppIcon = true;
	}
	
	/**
	 * Setup the Graphics Window
	 */
	public void setupGraphicsWindow()
	{
		graphicsWindow = GWindow.getWindow(world.ml, windowTitle, 10, 45, windowWidth, graphicsWindowHeight, PApplet.JAVA2D);
		graphicsWindow.setVisible(true);
		graphicsWindow.addData(new ML_WinData());
		graphicsWindow.addDrawHandler(this, "graphicsWindowDraw");
		graphicsWindow.addMouseHandler(this, "graphicsWindowMouse");
		graphicsWindow.addKeyHandler(world.ml, "graphicsWindowKey");
		graphicsWindow.setActionOnClose(GWindow.KEEP_OPEN);
	
		int x = 0, y = iTopMargin;
		if(delay) world.ml.delay(delayAmount);

		lblGraphics = new GLabel(graphicsWindow, x, y, graphicsWindow.width, iSmallBoxHeight, "Graphics");
		lblGraphics.setLocalColorScheme(10);
		lblGraphics.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblGraphics.setTextAlign(GAlign.CENTER, null);
		lblGraphics.setTextBold();

		x = 80;
		y += iLineSpacing;
//		if(delay) world.p.delay(delayAmount);
		
		btnZoomOut = new GButton(graphicsWindow, x, y, 40, iSmallBoxHeight, "Out");
		btnZoomOut.tag = "ZoomOut";
		btnZoomOut.setLocalColorScheme(5);
		lblZoom = new GLabel(graphicsWindow, 0, y, graphicsWindow.width, iSmallBoxHeight, "Zoom");
		lblZoom.setLocalColorScheme(10);
		lblZoom.setTextAlign(GAlign.CENTER, null);
		btnZoomIn = new GButton(graphicsWindow, x += 105, y, 40, iSmallBoxHeight, "In");
		btnZoomIn.tag = "ZoomIn";
		btnZoomIn.setLocalColorScheme(5);

		/* --- Add field of view --- */
//		applet.text(" Field of View:"+world.viewer.getFieldOfView(), x, y += lineWidth);

		x = 140;
		y += iLineSpacing;
		if(delay) world.ml.delay(delayAmount);

		sdrAlpha = new GSlider(graphicsWindow, x, y, 80, 80, 20);
		sdrAlpha.setLocalColorScheme(7);
		sdrAlpha.setLimits(world.getState().alpha, 255.f, 0.f);
		sdrAlpha.setRotation(PApplet.PI/2.f);
		sdrAlpha.setTextOrientation(G4P.ORIENT_LEFT);
		sdrAlpha.setEasing(0);
		sdrAlpha.setShowValue(true);
		sdrAlpha.tag = "Alpha";
		
		x += 140;
		sdrBrightness = new GSlider(graphicsWindow, x, y, 80, 80, 20);
		sdrBrightness.setLocalColorScheme(7);
		sdrBrightness.setLimits(world.viewer.getSettings().userBrightness, 1.f, 0.f);
		sdrBrightness.setValue(world.viewer.getSettings().userBrightness);					// Not sure why this is needed, maybe a G4P bug?
		sdrBrightness.setRotation(PApplet.PI/2.f);
		sdrBrightness.setTextOrientation(G4P.ORIENT_LEFT);
		sdrBrightness.setEasing(0);
		sdrBrightness.setShowValue(true);
		sdrBrightness.tag = "Brightness";

		x = 40;
		y += iLineSpacingSmall;

		lblAlpha= new GLabel(graphicsWindow, x, y, 60, iSmallBoxHeight, "Alpha");
		lblAlpha.setLocalColorScheme(10);

		x += 120;

		lblBrightness = new GLabel(graphicsWindow, x, y, 90, iSmallBoxHeight, "Brightness");
		lblBrightness.setLocalColorScheme(10);

		x = 0;
		y += 75;

		lblGraphicsModes = new GLabel(graphicsWindow, x, y, graphicsWindow.width, iSmallBoxHeight, "Graphics Modes");
		lblGraphicsModes.setLocalColorScheme(10);
		lblGraphicsModes.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblGraphicsModes.setTextAlign(GAlign.CENTER, null);
		lblGraphicsModes.setTextBold();

		x = 80;
		y += iLineSpacingSmall;

		chkbxAlphaMode = new GCheckbox(graphicsWindow, x, y, 85, iSmallBoxHeight, "Alpha Mode");
		chkbxAlphaMode.tag = "AlphaMode";
		chkbxAlphaMode.setLocalColorScheme(10);
		chkbxAlphaMode.setSelected(true);

		x = 80;
		y += iLineSpacingSmall;

		chkbxAngleFading = new GCheckbox(graphicsWindow, x, y, 100, iSmallBoxHeight, "Angle Fading");
		chkbxAngleFading.tag = "AngleFading";
		chkbxAngleFading.setLocalColorScheme(10);
		chkbxAngleFading.setSelected(true);

		x = 80;
		y += iLineSpacingSmall;

		chkbxFadeEdges = new GCheckbox(graphicsWindow, x, y, 85, iSmallBoxHeight, "Fade Edges");
		chkbxFadeEdges.tag = "FadeEdges";
		chkbxFadeEdges.setLocalColorScheme(10);
		chkbxFadeEdges.setSelected(true);

		x = 80;
		y += iLineSpacingSmall;

		chkbxAngleThinning = new GCheckbox(graphicsWindow, x, y, 100, iSmallBoxHeight, "Angle Thinning");
		chkbxAngleThinning.tag = "AngleThinning";
		chkbxAngleThinning.setLocalColorScheme(10);
		chkbxAngleThinning.setSelected(false);

		x = 80;
		y += iLineSpacingSmall;
		
		chkbxOrientationMode = new GCheckbox(graphicsWindow, x, y, 160, iSmallBoxHeight, "Orientation Mode (BETA)");
		chkbxOrientationMode.tag = "OrientationMode";
		chkbxOrientationMode.setLocalColorScheme(10);
		chkbxOrientationMode.setSelected(world.viewer.getSettings().orientationMode);
		
		x = 0;
		y += 40;

		lblHideMedia = new GLabel(graphicsWindow, x, y, graphicsWindow.width, iSmallBoxHeight, "Hide Media");
		lblHideMedia.setLocalColorScheme(10);
		lblHideMedia.setTextAlign(GAlign.CENTER, null);
		lblHideMedia.setTextBold();

		x = 8;
		y += iLineSpacingSmall;

		chkbxHideImages = new GCheckbox(graphicsWindow, x, y, 90, iSmallBoxHeight, "Images");
		chkbxHideImages.tag = "HideImages";
		chkbxHideImages.setLocalColorScheme(10);
		chkbxHideImages.setSelected(false);

		chkbxHideVideos = new GCheckbox(graphicsWindow, x += 90, y, 85, iSmallBoxHeight, "Videos");
		chkbxHideVideos.tag = "HideVideos";
		chkbxHideVideos.setLocalColorScheme(10);
		chkbxHideVideos.setSelected(false);

		chkbxHidePanoramas = new GCheckbox(graphicsWindow, x += 85, y, 120, iSmallBoxHeight, "Panoramas");
		chkbxHidePanoramas.tag = "HidePanoramas";
		chkbxHidePanoramas.setLocalColorScheme(10);
		chkbxHidePanoramas.setSelected(false);

		x = 0;
		y += iLineSpacing;

		lblOutput = new GLabel(graphicsWindow, x, y, graphicsWindow.width, iSmallBoxHeight, "Output");
		lblOutput.setLocalColorScheme(10);
		lblOutput.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblOutput.setTextAlign(GAlign.CENTER, null);
		lblOutput.setTextBold();

		x = 40;
		y += iLineSpacing;

		btnSaveImage = new GButton(graphicsWindow, x, y, 100, iSmallBoxHeight, "Export Image");
		btnSaveImage.tag = "ExportImage";
		btnSaveImage.setLocalColorScheme(5);
		btnOutputFolder = new GButton(graphicsWindow, x+100, y, 120, iSmallBoxHeight, "Set Output Folder");
		btnOutputFolder.tag = "OutputFolder";
		btnOutputFolder.setLocalColorScheme(5);
		
		x = 0;
		y = graphicsWindowHeight - iBottomMargin;
		lblCommand3 = new GLabel(graphicsWindow, x, y, graphicsWindow.width, iSmallBoxHeight);						/* Display Mode Label */
		lblCommand3.setText("Press ⇧3 to show / hide");
		lblCommand3.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		lblCommand3.setLocalColorScheme(10);
		lblCommand3.setTextAlign(GAlign.CENTER, null);

		setupGraphicsWindow = true;
		world.ml.setAppIcon = true;
	}
	
	/**
	 * Setup the Model Window
	 */
	public void setupModelWindow()
	{
		modelWindow = GWindow.getWindow(world.ml, windowTitle, 10, 45, windowWidth, modelWindowHeight, PApplet.JAVA2D);
		modelWindow.setVisible(true);
		modelWindow.addData(new ML_WinData());
		modelWindow.addDrawHandler(this, "modelWindowDraw");
		modelWindow.addMouseHandler(this, "modelWindowMouse");
		modelWindow.addKeyHandler(world.ml, "modelWindowKey");
		modelWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		int x = 0, y = iTopMargin;
		if(delay) world.ml.delay(delayAmount);

		lblModelWindow = new GLabel(modelWindow, x, y, modelWindow.width, iSmallBoxHeight, "Model");
		lblModelWindow.setLocalColorScheme(10);
		lblModelWindow.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblModelWindow.setTextAlign(GAlign.CENTER, null);
		lblModelWindow.setTextBold();

		y += iLineSpacing;
		if(delay) world.ml.delay(delayAmount);

		lblModelSettings = new GLabel(modelWindow, x, y, modelWindow.width, iSmallBoxHeight, "Model Settings");
		lblModelSettings.setLocalColorScheme(10);
		lblModelSettings.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblModelSettings.setTextAlign(GAlign.CENTER, null);
		lblModelSettings.setTextBold();

		x = 230;
		y += iLineSpacing;
		if(delay) world.ml.delay(delayAmount);

		sdrAltitudeScaling = new GSlider(modelWindow, x, y, 80, 80, 20);
		sdrAltitudeScaling.setLocalColorScheme(7);
		sdrAltitudeScaling.setLimits(0.f, 1.f, 0.f);
		sdrAltitudeScaling.setValue(world.settings.altitudeScalingFactor);											// -- Shouldn't be needed! Calls handler
		sdrAltitudeScaling.setRotation(PApplet.PI/2.f);
		sdrAltitudeScaling.setTextOrientation(G4P.ORIENT_LEFT);
		sdrAltitudeScaling.setEasing(0);
		sdrAltitudeScaling.setShowValue(true);
		sdrAltitudeScaling.tag = "AltitudeScaling";
		
		x = 60;
		y += iLineSpacing;
		
		lblAltitudeScaling = new GLabel(modelWindow, x, y, 100, iSmallBoxHeight, "Altitude Scaling");
		lblAltitudeScaling .setLocalColorScheme(10);

		y += 75;
		x = 60;

		btnSubjectDistanceDown = new GButton(modelWindow, x, y, 30, iSmallBoxHeight, "-");
		btnSubjectDistanceDown.tag = "SubjectDistanceDown";
		btnSubjectDistanceDown.setLocalColorScheme(5);
		lblSubjectDistance = new GLabel(modelWindow, x += 35, y, 110, iSmallBoxHeight, "Subject Distance");
		lblSubjectDistance.setLocalColorScheme(10);
		lblSubjectDistance.setTextAlign(GAlign.CENTER, null);
		lblSubjectDistance.setTextBold();
		btnSubjectDistanceUp = new GButton(modelWindow, x += 125, y, 30, iSmallBoxHeight, "+");
		btnSubjectDistanceUp.tag = "SubjectDistanceUp";
		btnSubjectDistanceUp.setLocalColorScheme(5);
		
		x = 0;
		y += 40;

		lblModelDisplay = new GLabel(modelWindow, x, y, modelWindow.width, iSmallBoxHeight, "Model Display");
		lblModelDisplay.setLocalColorScheme(10);
		lblModelDisplay.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		lblModelDisplay.setTextAlign(GAlign.CENTER, null);
		lblModelDisplay.setTextBold();

		x = 90;
		y += iLineSpacing;

		chkbxShowModel = new GCheckbox(modelWindow, x, y, 120, iSmallBoxHeight, "Show Model  (5)");
		chkbxShowModel.tag = "ShowModel";
		chkbxShowModel.setLocalColorScheme(10);
		
		x = 110;
		y += iLineSpacing;
		
		chkbxMediaToCluster = new GCheckbox(modelWindow, x, y, 150, iSmallBoxHeight, "Media to Cluster  (6)");
		chkbxMediaToCluster.tag = "MediaToCluster";
		chkbxMediaToCluster.setLocalColorScheme(10);
		chkbxMediaToCluster.setSelected(false);
		
		y += iLineSpacing;

		chkbxCaptureToMedia = new GCheckbox(modelWindow, x, y, 150, iSmallBoxHeight, "Capture to Media  (7)");
		chkbxCaptureToMedia.tag = "CaptureToMedia";
		chkbxCaptureToMedia.setLocalColorScheme(10);
		chkbxCaptureToMedia.setSelected(false);

		y += iLineSpacing;

		chkbxCaptureToCluster = new GCheckbox(modelWindow, x, y, 170, iSmallBoxHeight, "Capture to Cluster  (8)");
		chkbxCaptureToCluster.tag = "CaptureToCluster";
		chkbxCaptureToCluster.setLocalColorScheme(10);
		chkbxCaptureToCluster.setSelected(false);

		x = 0;
		y = modelWindowHeight - iBottomMargin;
		lblCommand4 = new GLabel(modelWindow, x, y, modelWindow.width, iSmallBoxHeight);						/* Display Mode Label */
		lblCommand4.setText("Press ⇧4 to show / hide");
		lblCommand4.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		lblCommand4.setLocalColorScheme(10);
		lblCommand4.setTextAlign(GAlign.CENTER, null);

		setupModelWindow = true;
		world.ml.setAppIcon = true;
	}

	/**
	 * Setup the Memory Window
	 */
	public void setupMemoryWindow()
	{
		memoryWindow = GWindow.getWindow(world.ml, "Memory", 10, 45, windowWidth, memoryWindowHeight, PApplet.JAVA2D);
		memoryWindow.setVisible(true);
		memoryWindow.addData(new ML_WinData());
		memoryWindow.addDrawHandler(this, "memoryWindowDraw");
		memoryWindow.addMouseHandler(this, "memoryWindowMouse");
		memoryWindow.addKeyHandler(world.ml, "memoryWindowKey");
		memoryWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		int x = 0, y = iTopMargin;

		lblMemory = new GLabel(memoryWindow, x, y, memoryWindow.width, iSmallBoxHeight, "Memory");
		lblMemory.setLocalColorScheme(10);
		lblMemory.setTextAlign(GAlign.CENTER, null);
		lblMemory.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblMemory.setTextBold();

		x = 0;
		y = memoryWindowHeight - iBottomMargin;
		lblCommand5 = new GLabel(memoryWindow, x, y, memoryWindow.width, iSmallBoxHeight);						/* Display Mode Label */
		lblCommand5.setText("Press ⇧6 to show / hide");
		lblCommand5.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		lblCommand5.setLocalColorScheme(10);
		lblCommand5.setTextAlign(GAlign.CENTER, null);
		
		setupMemoryWindow = true;
		world.ml.setAppIcon = true;
	}

	/**
	 * Setup the Selection Window
	 */
	public void setupSelectionWindow()
	{
		selectionWindow = GWindow.getWindow(world.ml, "Selection Mode", 10, 45, windowWidth, selectionWindowHeight, PApplet.JAVA2D);
		selectionWindow.setVisible(true);
		selectionWindow.addData(new ML_WinData());
		selectionWindow.addDrawHandler(this, "selectionWindowDraw");
		selectionWindow.addMouseHandler(this, "selectionWindowMouse");
		selectionWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		int x = 0, y = iTopMargin;
		if(delay) world.ml.delay(delayAmount);

		lblSelection = new GLabel(selectionWindow, x, y, selectionWindow.width, iSmallBoxHeight, "Selection");
		lblSelection.setLocalColorScheme(10);
		lblSelection.setTextAlign(GAlign.CENTER, null);
		lblSelection.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblSelection.setTextBold();

		x = 120;
		y += iLineSpacing;

		chkbxSelectionMode = new GCheckbox(selectionWindow, x, y, 90, iSmallBoxHeight, "Enable");
		chkbxSelectionMode.tag = "SelectionMode";
		chkbxSelectionMode.setFont(new Font("Monospaced", Font.PLAIN, 14));
		chkbxSelectionMode.setLocalColorScheme(10);
		
		x = 100;
		y += 35;
		
		btnSelectFront = new GButton(selectionWindow, x, y, 110, iSmallBoxHeight, "Select (x)");
		btnSelectFront.tag = "SelectFront";
		btnSelectFront.setLocalColorScheme(5);

		x = 100;
		y += iLineSpacing;
		
		btnDeselectFront = new GButton(selectionWindow, x, y, 110, iSmallBoxHeight, "Deselect (X)");
		btnDeselectFront.tag = "DeselectFront";
		btnDeselectFront.setLocalColorScheme(5);

		x = 95;
		y += iLineSpacingSmall;

		btnDeselectAll = new GButton(selectionWindow, x, y, 120, iSmallBoxHeight, "Deselect All...");
		btnDeselectAll.tag = "DeselectAll";
		btnDeselectAll.setLocalColorScheme(5);

		x = 0;
		y += iLineSpacing;

		lblSelectionOptions = new GLabel(selectionWindow, x, y, selectionWindow.width, 20, "Options");
		lblSelectionOptions.setLocalColorScheme(10);
		lblSelectionOptions.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		lblSelectionOptions.setTextAlign(GAlign.CENTER, null);
		lblSelectionOptions.setTextBold();

		x = 95;
		y += iLineSpacingSmall;
		
		chkbxMultiSelection = new GCheckbox(selectionWindow, x, y, 180, iSmallBoxHeight, "Allow Multiple");
		chkbxMultiSelection.tag = "MultiSelection";
		chkbxMultiSelection.setLocalColorScheme(10);

		x = 95;
		y += iLineSpacingSmall;
		
		chkbxSegmentSelection = new GCheckbox(selectionWindow, x, y, 180, iSmallBoxHeight, "Select Groups");
		chkbxSegmentSelection.tag = "SegmentSelection";
		chkbxSegmentSelection.setLocalColorScheme(10);
		
		x = 95;
		y += iLineSpacingSmall;
		
		chkbxShowMetadata = new GCheckbox(selectionWindow, x, y, 110, iSmallBoxHeight, "View Metadata");
		chkbxShowMetadata.tag = "ViewMetadata";
		chkbxShowMetadata.setLocalColorScheme(10);

		x = 85;
		y += iLineSpacingSmall;

		btnStitchPanorama = new GButton(selectionWindow, x, y, 140, iSmallBoxHeight, "Stitch Selection  (⇧\\)");
		btnStitchPanorama.tag = "StitchPanorama";
		btnStitchPanorama.setLocalColorScheme(7);
		
		x = 0;
		y = selectionWindowHeight - iBottomMargin;
		lblCommand6 = new GLabel(selectionWindow, x, y, selectionWindow.width, iSmallBoxHeight);						/* Display Mode Label */
		lblCommand6.setText("Press ⇧5 to show / hide");
		lblCommand6.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		lblCommand6.setLocalColorScheme(10);
		lblCommand6.setTextAlign(GAlign.CENTER, null);
		
		selectionWindow.addKeyHandler(world.ml, "selectionWindowKey");
		setupSelectionWindow = true;
		world.ml.setAppIcon = true;
	}

	/**
	 * Setup the Statistics Window
	 */
	public void setupStatisticsWindow()
	{
		statisticsWindow = GWindow.getWindow(world.ml, "Statistics", 10, 45, windowWidth * 2, statisticsWindowHeight, PApplet.JAVA2D);
		statisticsWindow.setVisible(true);
		statisticsWindow.addData(new ML_WinData());
		statisticsWindow.addDrawHandler(this, "statisticsWindowDraw");
		statisticsWindow.addMouseHandler(this, "statisticsWindowMouse");
		statisticsWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		int x = 0, y = iTopMargin;
		if(delay) world.ml.delay(delayAmount);

		lblStatistics = new GLabel(statisticsWindow, x, y, statisticsWindow.width, iSmallBoxHeight, "Statistics");
		lblStatistics.setLocalColorScheme(10);
		lblStatistics.setTextAlign(GAlign.CENTER, null);
		lblStatistics.setFont(new Font("Monospaced", Font.PLAIN, iSmallTextSize));
		lblStatistics.setTextBold();

		x = 0;
		y = statisticsWindowHeight - iBottomMargin;
		lblCommand7 = new GLabel(statisticsWindow, x, y, statisticsWindow.width, iSmallBoxHeight);						/* Display Mode Label */
		lblCommand7.setText("Press ⇧7 to show / hide");
		lblCommand7.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		lblCommand7.setLocalColorScheme(10);
		lblCommand7.setTextAlign(GAlign.CENTER, null);
		
		statisticsWindow.addKeyHandler(world.ml, "statisticsWindowKey");
		
		setupStatisticsWindow = true;
		world.ml.setAppIcon = true;
	}
	
	/**
	 * Setup the Help Window
	 */
	public void setupHelpWindow()
	{
		helpWindow = GWindow.getWindow(world.ml, "Help", 10, 45, windowWidth * 3, helpWindowHeight, PApplet.JAVA2D);
		helpWindow.setVisible(true);
		helpWindow.addData(new ML_WinData());
		helpWindow.addDrawHandler(this, "helpWindowDraw");
		helpWindow.addMouseHandler(this, "helpWindowMouse");
		helpWindow.addKeyHandler(world.ml, "helpWindowKey");
		
		int x = 0, y = iTopMargin;
		if(delay) world.ml.delay(delayAmount);

		/* Selection Window */
		lblHelp = new GLabel(helpWindow, x, y, helpWindow.width, iSmallBoxHeight, "Help");
		lblHelp.setLocalColorScheme(10);
		lblHelp.setTextAlign(GAlign.CENTER, null);
		lblHelp.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblHelp.setTextBold();

		x = 0;
		y = helpWindowHeight - iBottomMargin;
		lblShift8 = new GLabel(helpWindow, x, y, helpWindow.width, iSmallBoxHeight);						/* Display Mode Label */
		lblShift8.setText("Press ⇧8 to show / hide");
		lblShift8.setFont(new Font("Monospaced", Font.PLAIN, iVerySmallTextSize));
		lblShift8.setLocalColorScheme(10);
		lblShift8.setTextAlign(GAlign.CENTER, null);
		
		setupHelpWindow = true;
		world.ml.setAppIcon = true;
	}

	/**
	 * Setup and show Library Window
	 */
	public void setupLibraryWindow()
	{
		libraryWindow = GWindow.getWindow( world.ml, windowTitle, world.ml.appWidth / 2 - windowWidth, 
										   world.ml.appHeight / 2 - libraryWindowHeight / 2, windowWidth * 2, 
										   libraryWindowHeight, PApplet.JAVA2D);
		
		libraryWindow.addData(new ML_WinData());
		libraryWindow.addDrawHandler(this, "libraryWindowDraw");
		libraryWindow.addMouseHandler(this, "libraryWindowMouse");
		libraryWindow.addKeyHandler(world.ml, "libraryWindowKey");
		libraryWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		int x = 0, y = iTopMargin;

		if(delay) world.ml.delay(10);
		
		lblLibrary = new GLabel(libraryWindow, x, y, libraryWindow.width, 22, "Welcome to MultimediaLocator.");
		lblLibrary.setLocalColorScheme(10);
		lblLibrary.setFont(new Font("Monospaced", Font.PLAIN, iLargeTextSize));
		lblLibrary.setTextAlign(GAlign.CENTER, null);
		lblLibrary.setTextBold();

		x = 90;
		y += 60;

		btnCreateLibrary = new GButton(libraryWindow, x, y, 170, iLargeBoxHeight, "Create Library");
		btnCreateLibrary.tag = "CreateLibrary";
		btnCreateLibrary.setFont(new Font("Monospaced", Font.BOLD, iLargeTextSize));
		btnCreateLibrary.setLocalColorScheme(1);
		btnOpenLibrary = new GButton(libraryWindow, x+270, y, 155, iLargeBoxHeight, "Open Library");
		btnOpenLibrary.tag = "OpenLibrary";
		btnOpenLibrary.setFont(new Font("Monospaced", Font.BOLD, iLargeTextSize));
		btnOpenLibrary.setLocalColorScheme(5);
		
		setupLibraryWindow = true;
		world.ml.setAppIcon = true;
	}

	/**
	 * Setup and show Import Window
	 */
	public void setupImportWindow()
	{
		importWindow = GWindow.getWindow( world.ml, windowTitle, world.ml.appWidth / 2 - windowWidth * 3 / 2, 
				   world.ml.appHeight / 2 - importWindowHeight / 2, windowWidth * 3, 
				   importWindowHeight, PApplet.JAVA2D);

		importWindow.addData(new ML_WinData());
		importWindow.addDrawHandler(this, "importWindowDraw");
		importWindow.addMouseHandler(this, "importWindowMouse");
		importWindow.addKeyHandler(world.ml, "importWindowKey");
		importWindow.setActionOnClose(GWindow.KEEP_OPEN);

		int x = 0, y = 12;

		if(delay) world.ml.delay(delayAmount);

		lblImport = new GLabel(importWindow, x, y, importWindow.width, 22, "Choose Media for New Library");
		lblImport.setLocalColorScheme(10);
		lblImport.setFont(new Font("Monospaced", Font.PLAIN, iMediumTextSize));
		lblImport.setTextAlign(GAlign.CENTER, null);
		lblImport.setTextBold();

		x = windowWidth * 3 / 2 - 180;
		y += 60;

		btnImportMediaFolder = new GButton(importWindow, x, y, 160, 60, "Add Folder");
		btnImportMediaFolder.tag = "AddMediaFolder";
		btnImportMediaFolder.setFont(new Font("Monospaced", Font.BOLD, iLargeTextSize));
		btnImportMediaFolder.setLocalColorScheme(1);
		btnMakeLibrary = new GButton(importWindow, x+220, y, 100, 60, "Done");
		btnMakeLibrary.tag = "MakeLibrary";
		btnMakeLibrary.setFont(new Font("Monospaced", Font.BOLD, iLargeTextSize));
		btnMakeLibrary.setLocalColorScheme(5);
		
		setupImportWindow = true;
		world.ml.setAppIcon = true;
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
		applet.fill(0, 0, 255);
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
	public void libraryWindowDraw(PApplet applet, GWinData data) {
		applet.background(0);
		applet.stroke(255);
		applet.strokeWeight(1);
		applet.fill(0, 0, 255);
	}

	/**
	 * Handles mouse events for all GWindow objects
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void libraryWindowMouse(PApplet applet, GWinData data, MouseEvent event) {
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
	public void importWindowDraw(PApplet applet, GWinData data) 
	{
		float smallTextSize = 11.f;
		float mediumTextSize = 16.f;
		applet.background(0);
		applet.stroke(255, 0, 255);
		applet.strokeWeight(1);
		applet.colorMode(PApplet.HSB);
		applet.fill(0, 0, 255);
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
						y += iLineSpacing;
					}
				}
			}
		}
	}

	/**
	 * Handles mouse events for all GWindow objects
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void importWindowMouse(PApplet applet, GWinData data, MouseEvent event) {
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
	public void timeWindowDraw(PApplet applet, GWinData data) {
		if(world.ml.state.running)
		{
			float lineWidthVeryWide = 20f;
			float lineWidthWide = 15f;
			float lineWidth = 15f;

//			float mediumTextSize = 13.f;
			float smallTextSize = 11.f;

			float x = 10, y = 450;			
			applet.background(0);
			applet.stroke(255);
			applet.strokeWeight(1);
			applet.fill(255, 255, 255);

			applet.textSize(smallTextSize);
			
			int mode = world.getState().getTimeMode();
//			if( mode == 0 || mode == 1 )
//			{
//				int curTime = (mode == 0) ? world.getCurrentCluster().getState().currentTime : world.getState().currentTime;
//				applet.text(" Current Time: "+ curTime, x, y += lineWidth);
//			}

			WMV_Field f = world.getCurrentField();
			float timePoint = 0.f;						// Normalized time position between 0.f and 1.f

			switch(mode)
			{
				case 0:
//					applet.text(" Time Mode: Cluster", x, y += lineWidthVeryWide);
					timePoint = utilities.mapValue(world.getCurrentCluster().getState().currentTime, 0, world.getCurrentCluster().getState().timeCycleLength, 0.f, 1.f);
//					if(f.getTimeline().timeline.size() > 0 && world.viewer.getCurrentFieldTimeSegment() >= 0 && world.viewer.getCurrentFieldTimeSegment() < f.getTimeline().timeline.size())
//					{
//						applet.text(" Upper: "+f.getTimeSegment(world.viewer.getCurrentFieldTimeSegment()).getUpper().getTime()+
//								" Center:"+f.getTimeSegment(world.viewer.getCurrentFieldTimeSegment()).getCenter().getTime()+
//								" Lower: "+f.getTimeSegment(world.viewer.getCurrentFieldTimeSegment()).getLower().getTime(), x, y += lineWidthVeryWide);
//						applet.text(" Current Cluster Timeline Size: "+ world.getCurrentCluster().getTimeline().timeline.size(), x, y += lineWidthWide);
//					}
//					else
//					{
//						applet.text(" Current Cluster Timeline Size: "+ world.getCurrentCluster().getTimeline().timeline.size(), x, y += lineWidthVeryWide);
//					}
//					applet.text(" Current Cluster Dateline Size: "+ world.getCurrentCluster().getDateline().size(), x, y += lineWidth);
					
					break;
				case 1:
					timePoint = utilities.mapValue(world.getState().currentTime, 0, world.getSettings().timeCycleLength, 0.f, 1.f);
//					applet.text(" Time Mode: Field", x, y += lineWidthVeryWide);
					break;
				case 2:
					timePoint = utilities.mapValue(world.getState().currentTime, 0, world.getSettings().timeCycleLength, 0.f, 1.f);
//					applet.text(" Time Mode: Media", x, y += lineWidthVeryWide);
					applet.text(" Current Media: "+ world.viewer.getCurrentMedia(), x, y += lineWidth);		// -- Not very meaningful.. should show media index / type
					break;
//				case 3:
//					applet.text(" Time Mode: Flexible", x, y += lineWidthVeryWide);
//					break;
			}
	
			if(setupTimeWindow)
			{
				if(world.state.timeFading && !world.state.paused)
					sdrCurrentTime.setValue(timePoint);
			}

//			applet.text(" Current Field Time: "+ world.currentTime, x, y += lineWidth);
//			applet.text(" Current Field Time Segment: "+ world.viewer.getCurrentFieldTimeSegment(), x, y += lineWidthVeryWide);
//			applet.text(" Current Field Timeline Size: "+ world.getCurrentField().getTimeline().timeline.size(), x, y += lineWidth);
//			applet.text(" Current Field Dateline Size: "+ world.getCurrentField().getDateline().size(), x, y += lineWidth);
		}
	}

	/**
	 * Handles mouse events for Time Window  
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void timeWindowMouse(PApplet applet, GWinData data, MouseEvent event) {
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
	public void navigationWindowDraw(PApplet applet, GWinData data) {
//		applet.background(10, 5, 50);
		applet.background(0);
		applet.stroke(255);
		applet.strokeWeight(1);
		applet.fill(0, 0, 255);

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
	public void graphicsWindowDraw(PApplet applet, GWinData data) {
		applet.background(0);
		applet.stroke(255);
		applet.strokeWeight(1);
		applet.fill(0, 0, 255);
	}


	/**
	 * Handles mouse events for Graphics Window
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void graphicsWindowMouse(PApplet applet, GWinData data, MouseEvent event) {
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
	 * Handles drawing to the Model Window 
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 */
	public void modelWindowDraw(PApplet applet, GWinData data) {
//		float lineWidthVeryWide = 20f;
//		float lineWidthWide = 15f;
//		float lineWidth = 15f;
		
//		float mediumTextSize = 13.f;
//		float smallTextSize = 11.f;

//		float x = 10, y = 165;			

		applet.background(0);
		applet.stroke(255);
		applet.strokeWeight(1);
		applet.fill(255, 255, 255);
//		
//		applet.textSize(mediumTextSize);
//		applet.text(" Time ", x, y += lineWidthVeryWide);
//		applet.textSize(smallTextSize);
//		applet.text(" Time Mode: "+ ((world.p.world.timeMode == 0) ? "Cluster" : "Field"), x, y += lineWidthVeryWide);
//		
////		if(world.p.world.timeMode == 0)
//			applet.text(" Current Field Time: "+ world.currentTime, x, y += lineWidth);
//		applet.text(" Current Field Time Segment: "+ world.viewer.currentFieldTimeSegment, x, y += lineWidth);
//		applet.text(" Current Cluster Time Segment: "+ world.getCurrentCluster().getTimeline().size(), x, y += lineWidth);
////		if(world.p.world.timeMode == 1)
//			applet.text(" Current Cluster Time: "+ world.getCurrentCluster().currentTime, x, y += lineWidth);
//		applet.text(" Current Field Timeline Size: "+ world.getCurrentField().getTimeline().size(), x, y += lineWidth);
//		applet.text(" Current Field Dateline Size: "+ world.getCurrentField().getDateline().size(), x, y += lineWidth);
//
//		WMV_Field f = world.getCurrentField();
//		if(f.getTimeline().size() > 0 && world.viewer.currentFieldTimeSegment >= 0 && world.viewer.currentFieldTimeSegment < f.getTimeline().size())
//			applet.text(" Upper: "+f.getTimeline().get(world.viewer.currentFieldTimeSegment).getUpper().getTime()+
//					" Center:"+f.getTimeline().get(world.viewer.currentFieldTimeSegment).getCenter().getTime()+
//					" Lower: "+f.getTimeline().get(world.viewer.currentFieldTimeSegment).getLower().getTime(), x, y += lineWidth);
//		applet.text(" Current Cluster Timeline Size: "+ world.getCurrentCluster().getTimeline().size(), x, y += lineWidth);
//		applet.text(" Current Cluster Dateline Size: "+ world.getCurrentCluster().getDateline().size(), x, y += lineWidth);
	}

	/**
	 * Handles mouse events for Time Window  
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void modelWindowMouse(PApplet applet, GWinData data, MouseEvent event) {
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
			applet.background(10, 5, 50);
			applet.stroke(255);
			applet.strokeWeight(1);
			applet.fill(0, 0, 255);

			float lineWidthVeryWide = 20f;
			float lineWidth = 14f;

			float x = 10;
			float y = 50;			// Starting vertical position

			WMV_Field f = world.getCurrentField();
			if(world.viewer.getState().getCurrentClusterID() >= 0)
			{
				WMV_Cluster c = world.getCurrentCluster();

				applet.fill(185, 215, 255, 255);					// Set text color
				applet.textSize(fLargeTextSize);
				applet.text("Program Modes", x, y += lineWidthVeryWide);
				applet.textSize(fSmallTextSize);
				applet.text(" Alpha Mode:"+world.getState().alphaMode, x, y += lineWidth);
				applet.text(" Orientation Mode (BETA): "+world.viewer.getSettings().orientationMode, x, y += lineWidthVeryWide);
//				applet.text(" Altitude Scaling: "+world.altitudeScaling, x, y += lineWidth);
//				applet.text(" Lock Media to Clusters:"+world.lockMediaToClusters, x, y += lineWidth);

				applet.textSize(fLargeTextSize);
				applet.text("World", x, y += lineWidthVeryWide);
				applet.textSize(fSmallTextSize);
				applet.text(" Field Count: "+world.getFields().size(), x, y += lineWidthVeryWide);
				applet.text(" Current Field: "+f.getName(), x, y += lineWidthVeryWide);
				applet.text(" Cluster Count: "+f.getClusters().size(), x, y += lineWidthVeryWide);
				applet.text(" ID: "+(world.viewer.getState().getField()+1)+" out of "+world.getFieldCount()+" Total Fields", x, y += lineWidthVeryWide);
				applet.text(" Width: "+f.getModel().getState().fieldWidth+" Length: "+f.getModel().getState().fieldLength+" Height: "+f.getModel().getState().fieldHeight, x, y += lineWidth);
				applet.text(" Image Count: "+f.getImageCount(), x, y += lineWidth);					// Doesn't check for dataMissing!!
				applet.text(" Panorama Count: "+f.getPanoramaCount(), x, y += lineWidth);			// Doesn't check for dataMissing!!
				applet.text(" Video Count: "+f.getVideoCount(), x, y += lineWidth);					// Doesn't check for dataMissing!!
				applet.text(" Sound Count: "+f.getSoundCount(), x, y += lineWidth);					// Doesn't check for dataMissing!!
				applet.text(" Media Density (per sq. m.): "+f.getModel().getState().mediaDensity, x, y += lineWidth);
//				applet.text(" Clusters Visible: "+world.viewer.clustersVisible+"  (Orientation Mode)", x, y += lineWidth);
			
				applet.textSize(fLargeTextSize);
				applet.text("Viewer", x, y += lineWidthVeryWide);
				applet.textSize(fSmallTextSize);
				applet.text(" Location, x: "+PApplet.round(world.viewer.getLocation().x)+" y:"+PApplet.round(world.viewer.getLocation().y)+" z:"+
						PApplet.round(world.viewer.getLocation().z), x, y += lineWidthVeryWide);		
				applet.text(" GPS Longitude: "+world.viewer.getGPSLocation().x+" Latitude:"+world.viewer.getGPSLocation().y, x, y += lineWidth);		

				applet.text(" Clusters Visible: "+world.getVisibleClusters().size(), x, y += lineWidth);
				applet.text(" Images Visible: "+f.getImagesVisible(), x, y += lineWidth);
//				applet.text("   Images Seen: "+f.getImagesSeen(), x, y += lineWidth);
				applet.text(" Panoramas Visible: "+f.getPanoramasVisible(), x, y += lineWidth);
//				applet.text("   Panoramas Seen: "+f.getPanoramasSeen(), x, y += lineWidth);
				applet.text(" Videos Visible: "+f.getVideosVisible(), x, y += lineWidth);
//				applet.text("   Videos Seen: "+f.getVideosSeen(), x, y += lineWidth);
				applet.text("   Currently Playing: "+f.getVideosPlaying(), x, y += lineWidth);
				applet.text(" Sounds Audible: "+f.getSoundsAudible(), x, y += lineWidth);
				applet.text("   Currently Playing: "+f.getSoundsPlaying(), x, y += lineWidth);

				applet.textSize(fLargeTextSize);
				applet.text("Output", x, y += lineWidthVeryWide);
				applet.textSize(fSmallTextSize);
				applet.text(" Image Output Folder:"+world.outputFolder, x, y += lineWidthVeryWide);
				applet.text(" Library Folder:"+world.ml.library.getLibraryFolder(), x, y += lineWidth);

				if(world.ml.debugSettings.memory)
				{
					if(world.ml.debugSettings.detailed)
					{
						applet.text("Total memory (bytes): " + world.ml.totalMemory, x, y += lineWidth);
						applet.text("Available processors (cores): "+world.ml.availableProcessors, x, y += lineWidth);
						applet.text("Maximum memory (bytes): " +  (world.ml.maxMemory == Long.MAX_VALUE ? "no limit" : world.ml.maxMemory), x, y += lineWidth); 
						applet.text("Total memory (bytes): " + world.ml.totalMemory, x, y += lineWidth);
						applet.text("Allocated memory (bytes): " + world.ml.allocatedMemory, x, y += lineWidth);
					}
					applet.text("Free memory (bytes): "+world.ml.freeMemory, x, y += lineWidth);
					applet.text("Approx. usable free memory (bytes): " + world.ml.approxUsableFreeMemory, x, y += lineWidth);
				}			
			}
//			else
//				p.message(worldSettings, "Can't display statistics: currentCluster == "+world.viewer.getState().getCurrentClusterID()+"!!!");
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
	 * Handles drawing to Selection Window
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 */
	public void selectionWindowDraw(PApplet applet, GWinData data) {
		applet.background(10, 5, 50);
		applet.stroke(255);
		applet.strokeWeight(1);
		applet.fill(0, 0, 255);
	}

	/**
	 * Handles mouse events for Selection Window
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void selectionWindowMouse(PApplet applet, GWinData data, MouseEvent event) {
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
		
		float lineWidthVeryWide = 17f;
		float lineWidthWide = 15f;
		float lineWidth = 13f;
		
		float xPos = 10;
		float yPos = 50;			// Starting vertical position

		applet.fill(255, 255, 255, 255); 	// White text color                        

		if(backgroundText)
		{
			applet.textSize(fLargeTextSize);
			applet.text(" Background:", xPos, yPos += lineWidth);

			yPos += lineWidthWide;

			applet.textSize(fMediumTextSize);
			applet.text(" MultimediaLocator emerged out of my Master's project in Media, Arts, and Technology ", xPos, yPos += lineWidthWide);
			applet.text(" at the University of California, Santa Barbara, where I also completed a PhD in Music  ", xPos, yPos += lineWidthWide);
			applet.text(" Composition in 2017.  ", xPos, yPos += lineWidthWide);

			yPos += lineWidthWide;

			applet.text(" Existing systems for spatially arranging media, such as Google Street View or Microsoft", xPos, yPos += lineWidthWide);
			applet.text(" Photosynth, are intended largely for commercial, rather than artistic applications.", xPos, yPos += lineWidthWide);
			applet.text(" These programs largely rely on feature matching algorithms to produce highly seamless", xPos, yPos += lineWidthWide);
			applet.text(" renderings. While impressive for its time, Photosynth had several limitations, such as ", xPos, yPos += lineWidthWide);
			applet.text(" failing if images have too many gaps in between them, contain \"artifacts\" such as blur  ", xPos, yPos += lineWidthWide);
			applet.text(" or high contrast areas, or cover too large a geographical area. Furthermore, it offered no way", xPos, yPos += lineWidthWide);
			applet.text(" for displaying change over time, and only a text-based menu for moving between \"synths\"", xPos, yPos += lineWidthWide);
			
			yPos += lineWidthWide;

			applet.text(" While Google Street View now covers large portions of the planet, and has even incorporated ", xPos, yPos += lineWidthWide);
			applet.text(" change over time into its recent interface, it remains a proprietary system, ", xPos, yPos += lineWidthWide);
			applet.text(" which does not allow artists to import their own media. Artists wishing to use their personal ", xPos, yPos += lineWidthWide);
			applet.text(" image collections must currently design their own software. Furthermore, I am aware of no currently ", xPos, yPos += lineWidthWide);
			applet.text(" available 3D visualization software that allows artists to import not only images, but", xPos, yPos += lineWidthWide);
			applet.text(" sounds and videos as well. ", xPos, yPos += lineWidthWide);
		}
		else
		{
			applet.textSize(fLargeTextSize);
			applet.text(" About MultimediaLocator: ", xPos, yPos);

			yPos += lineWidthWide;

			applet.textSize(fMediumTextSize);
			applet.text(" MultimediaLocator is an experimental 3D multimedia browsing and navigation system ", xPos, yPos += lineWidthWide);
			applet.text(" that uses metadata to locate images, 360 panoramas, videos and sounds ", xPos, yPos += lineWidthWide);
			applet.text(" in virtual space in the approximate real-world locations they ", xPos, yPos += lineWidthWide);
			applet.text(" were captured. ", xPos, yPos += lineWidthWide);

			applet.text(" While portions of the software are still under development, it is intended as ", xPos, yPos += lineWidthWide);
			applet.text(" a platform for both aesthetic and informational visualization, which encourages", xPos, yPos += lineWidthWide);
			applet.text(" collaborations between artists across multiple media and disciplines.", xPos, yPos += lineWidthWide);

			yPos += lineWidthVeryWide;

			applet.textSize(fMediumTextSize);
			applet.text(" Keyboard Controls ", xPos, yPos += lineWidthVeryWide);

			yPos += lineWidthWide;

			applet.textSize(fMediumTextSize);
			applet.text(" General", xPos, yPos += lineWidthVeryWide);
			applet.textSize(fSmallTextSize);
			applet.text(" R    Restart WorldMediaViewer", xPos, yPos += lineWidthVeryWide);
			applet.text(" CMD + q    Quit WorldMediaViewer", xPos, yPos += lineWidth);

			/* Movement */
			applet.textSize(fMediumTextSize);
			applet.text(" Movement", xPos, yPos += lineWidthVeryWide);
			applet.textSize(fSmallTextSize);
			applet.text(" a d w s   Walk Left / Right / Forward / Backward ", xPos, yPos += lineWidthVeryWide);
			applet.text(" Arrows    Turn Camera ", xPos, yPos += lineWidth);
			applet.text(" q z  Zoom In / Out + / - ", xPos, yPos += lineWidth);

			/* Navigation */
			applet.textSize(fMediumTextSize);
			applet.text(" Navigation", xPos, yPos += lineWidthVeryWide);
			applet.textSize(fSmallTextSize);
			applet.text(" m    Move to Nearest Cluster", xPos, yPos += lineWidthWide);
			applet.text(" j    Move to Random Cluster", xPos, yPos += lineWidth);

			applet.text(" n    Move to Next Cluster in Time", xPos, yPos += lineWidthWide);
			applet.text(" b    Move to Previous Cluster in Time", xPos, yPos += lineWidth);
			applet.text(" >    Follow Timeline", xPos, yPos += lineWidthVeryWide);
			//		applet.text(" A    Move to Next Location in Memory", xPos, yPos += lineWidth);
			//		applet.text(" >    Follow Date-Independent Timeline", xPos, yPos += lineWidthVeryWide);
			//		applet.text(" .    Follow Date-Specific Timeline", xPos, yPos += lineWidth);
			applet.text(" J    Teleport to Random Cluster", xPos, yPos += lineWidth);
			applet.text(" U    Move to Next Video ", xPos, yPos += lineWidthWide);
			applet.text(" u    Teleport to Next Video ", xPos, yPos += lineWidth);
			applet.text(" M    Move to Next Panorama ", xPos, yPos += lineWidth);
			applet.text(" m    Teleport to Next Panorama ", xPos, yPos += lineWidth);
			applet.text(" { }  Teleport to Next / Previous Field ", xPos, yPos += lineWidth);
			applet.text(" L    Look for Media", xPos, yPos += lineWidth);
			//		applet.text(" C    Lock Viewer to Nearest Cluster On/Off", xPos, yPos += lineWidthWide);
			//		applet.text(" l    Look At Selected Media", xPos, yPos += lineWidth);

			/* Interaction */
			applet.textSize(fMediumTextSize);
			applet.text(" Navigation", xPos, yPos += lineWidthVeryWide);
			applet.textSize(fSmallTextSize);
			applet.text(" A    Enable / Disable Selection", xPos, yPos += lineWidthWide);
			applet.text(" x    Select Media in Front", xPos, yPos += lineWidth);
			applet.text(" X    Deselect Media in Front", xPos, yPos += lineWidth);
			//		applet.text(" S    Multi-Selection Mode On/Off", xPos, yPos += lineWidth);
			//		applet.text(" OPT + s    Segment Selection Mode On/Off", xPos, yPos += lineWidthWide);
			applet.text(" x    Select Media in Front", xPos, yPos += lineWidth);
			applet.text(" X    Deselect Media in Front", xPos, yPos += lineWidth);
			applet.text(" OPT + x    Deselect All Media", xPos, yPos += lineWidth);

			/* Time */
			applet.textSize(fMediumTextSize);
			applet.text(" Time", xPos, yPos += lineWidthVeryWide);
			applet.textSize(fSmallTextSize);
			applet.text(" T    Time Fading On/Off", xPos, yPos += lineWidthVeryWide);
			applet.text(" Z    Toggle Time Fading Mode (Cluster/Field)", xPos, yPos += lineWidth);
			applet.text(" -    Pause On/Off   ", xPos, yPos += lineWidth);
			applet.text(" &/*  Default Media Length - / +", xPos, yPos += lineWidth);
			applet.text(" ⇧ Lt/Rt   Cycle Length - / +", xPos, yPos += lineWidth);
			applet.text(" ⇧ Up/Dn   Current Time - / +", xPos, yPos += lineWidth);
			applet.text(" space  Show/Hide Menu   ", xPos, yPos += lineWidth);

			/* Graphics */
			applet.textSize(fMediumTextSize);
			applet.text(" Graphics", xPos, yPos += lineWidthVeryWide);
			applet.textSize(fSmallTextSize);
			applet.text(" P    Alpha Mode  On / Off   ", xPos, yPos += lineWidth);
			applet.text(" ( )  Alpha Level - / +      ", xPos, yPos += lineWidth);
			applet.text(" G    Angle Fading On/Off", xPos, yPos += lineWidthVeryWide);
			applet.text(" H    Angle Thinning On/Off", xPos, yPos += lineWidth);
			applet.text(" I P V S  Hide images / panoramas / videos / sounds    ", xPos, yPos += lineWidth);

			/* Model */
			applet.textSize(fMediumTextSize);
			applet.text(" Model", xPos, yPos += lineWidthVeryWide);
			applet.textSize(fSmallTextSize);
			applet.text(" [ ]  Altitude Scaling Adjustment  + / - ", xPos, yPos += lineWidthVeryWide);
			applet.text(" - =  Object Distance  - / +      ", xPos, yPos += lineWidth);
			applet.text(" ⌥ -   Visible Angle  -      ", xPos, yPos += lineWidth);
			applet.text(" ⌥ =   Visible Angle  +      ", xPos, yPos += lineWidth);

			applet.textSize(fMediumTextSize);
			applet.text(" GPS Tracks", xPos, yPos += lineWidthVeryWide);
			applet.textSize(fSmallTextSize);
			applet.text(" g    Load GPS Track", xPos, yPos += lineWidthVeryWide);
			applet.text(" OPT + g    Follow GPS Track", xPos, yPos += lineWidth);

			applet.textSize(fMediumTextSize);
			applet.text(" Memory", xPos, yPos += lineWidthVeryWide);
			applet.textSize(fSmallTextSize);
			applet.text(" `    Save Current View to Memory", xPos, yPos += lineWidthVeryWide);
			applet.text(" ~    Follow Memory Path", xPos, yPos += lineWidth);
			applet.text(" Y    Clear Memory", xPos, yPos += lineWidth);

			applet.textSize(fMediumTextSize);
			applet.text(" Output", xPos, yPos += lineWidthVeryWide);
			applet.textSize(fSmallTextSize);
			applet.text(" O    Set Image Output Folder", xPos, yPos += lineWidthVeryWide);
			applet.text(" o    Save Screen Image to Disk", xPos, yPos += lineWidth);
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
		
		float lineWidthVeryWide = 17f;
		float lineWidthWide = 15f;
		float lineWidth = 13f;
		
		float xPos = 10;
		float yPos = 50;			// Starting vertical position

//		float largeTextSize = 15.f;
//		float mediumTextSize = 13.f;
//		float smallTextSize = 11.f;

		applet.fill(255, 255, 255, 255);                        
		
		applet.textSize(fMediumTextSize);
		applet.text("Memory", xPos, yPos);

		yPos += lineWidthWide;

		applet.textSize(fSmallTextSize);
		applet.text("Points in Memory:"+world.viewer.getMemoryPath().size(), xPos, yPos += lineWidthVeryWide);
		
		yPos += lineWidthWide * 2.f;

		//		applet.textSize(mediumTextSize);
//		applet.text(" Memory", xPos, yPos += lineWidthVeryWide);
		applet.textSize(fMediumTextSize);
		applet.text(" Keyboard Controls ", xPos, yPos);
		applet.textSize(fSmallTextSize);
		applet.text(" `    Save Current View to Memory", xPos, yPos += lineWidthVeryWide);
		applet.text(" ~    Follow Memory Path", xPos, yPos += lineWidth);
		applet.text(" Y    Clear Memory", xPos, yPos += lineWidth);
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
	
	public void showWMVWindow()
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
	public void showTimeWindow()
	{
		showTimeWindow = true;
		if(setupTimeWindow)
			timeWindow.setVisible(true);
		if(showMLWindow)
			hideMLWindow();
	}
	public void showGraphicsWindow()
	{
		showGraphicsWindow = true;
		if(setupGraphicsWindow)
			graphicsWindow.setVisible(true);
		if(showMLWindow)
			hideMLWindow();
	}
	public void showModelWindow()
	{
		showModelWindow = true;
		if(setupModelWindow)
			modelWindow.setVisible(true);
		if(showMLWindow)
			hideMLWindow();
	}
	public void showMemoryWindow()
	{
		showMemoryWindow = true;
		if(setupMemoryWindow)
			memoryWindow.setVisible(true);
		if(showMLWindow)
			hideMLWindow();
	}
	public void showSelectionWindow()
	{
		showSelectionWindow = true;
		if(setupSelectionWindow)
			selectionWindow.setVisible(true);
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
	}
	public void showLibraryWindow()
	{
		showLibraryWindow = true;
		if(setupLibraryWindow)
			libraryWindow.setVisible(true);
	}
	public void showImportWindow()
	{
		showImportWindow = true;
		if(setupImportWindow)
			importWindow.setVisible(true);
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
	public void hideTimeWindow()
	{
		showTimeWindow = false;
		if(setupTimeWindow)
			timeWindow.setVisible(false);
	} 
	public void hideGraphicsWindow()
	{
		showGraphicsWindow = false;
		if(setupGraphicsWindow)
			graphicsWindow.setVisible(false);
	}
	public void hideModelWindow()
	{
		showModelWindow = false;
		if(setupModelWindow)
			modelWindow.setVisible(false);
	} 
	public void hideMemoryWindow()
	{
		showMemoryWindow = false;
		if(setupMemoryWindow)
			memoryWindow.setVisible(false);
	}	
	public void hideSelectionWindow()
	{
		showSelectionWindow = false;
		if(setupSelectionWindow)
			selectionWindow.setVisible(false);
	} 
	public void hideStatisticsWindow()
	{
		showStatisticsWindow = false;
		if(setupStatisticsWindow)
			statisticsWindow.setVisible(false);
	} 
	public void hideHelpWindow()
	{
		showHelpWindow = false;
		if(setupHelpWindow)
			helpWindow.setVisible(false);
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
		hideMLWindow();
		hideNavigationWindow();
		hideGraphicsWindow();
		hideSelectionWindow();
		hideStatisticsWindow();
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