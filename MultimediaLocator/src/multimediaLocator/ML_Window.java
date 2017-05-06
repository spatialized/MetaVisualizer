package multimediaLocator;

import java.awt.Font;

import g4p_controls.*;
import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

/**
 * Handles secondary program windows
 * @author davidgordon
 */
public class ML_Window {

	ML_Display p;
	WMV_Utilities utilities;
	
	private boolean delay = true;
	private int delayAmount = 180;
	
	private int windowWidth = 310, longWindowHeight = 600;
	private int shortWindowHeight = 340;
	
	/* Windows */
	public GWindow mlWindow, timeWindow, navigationWindow, graphicsWindow, modelWindow, selectionWindow, statisticsWindow, helpWindow,
				   memoryWindow;
	private GLabel lblMainMenu, lblNavigationWindow, lblGraphics, lblStatistics, lblHelp, lblMemory;				// Window headings
	public boolean showMLWindow = false, showNavigationWindow = false, showTimeWindow = false, showGraphicsWindow = false, showModelWindow = false,
				    showSelectionWindow = false, showStatisticsWindow = false, showHelpWindow = false, showMemoryWindow = false;
	public boolean setupTimeWindow = false, setupNavigationWindow = false, setupGraphicsWindow = false, setupModelWindow = false, setupHelpWindow = false, 
					setupSelectionWindow = false, setupStatisticsWindow = false, setupMemoryWindow = false;

	/* Main Menu */
	private GButton btnNavigationWindow, btnTimeWindow, btnGraphicsWindow, btnModelWindow, btnSelectionWindow,
				    btnStatisticsWindow, btnHelpWindow, btnMemoryWindow;
	private GButton btnLoadMediaLibrary;
	private GLabel lblSpaceBar;
	public GToggleGroup tgDisplayView;	
	public GOption optSceneView, optMapView, optLibraryView;
	int mlWindowHeight;

	/* Navigation Window */
	private GLabel lblClusterNavigation, lblMemoryCommands, lblPathNavigation, lblTeleportLength, lblPathWaitLength;
	private GButton btnImportGPSTrack;
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
	private GLabel lblTimeWindow, lblTimeSettings, lblTimeMode;
	private GLabel lblMediaLength, lblTimeCycleLength;
//	public GLabel lblCurrentTime;
	public GLabel lblTime;
	public GCheckbox chkbxTimeFading;
	public GSlider sdrMediaLength, sdrTimeCycleLength;
	public GToggleGroup tgTimeMode;	
	public GOption optClusterTimeMode, optFieldTimeMode, optMediaTimeMode;
	public GLabel lblCommand2;
	int timeWindowHeight;

	/* Graphics Window */
	public GLabel lblGraphicsModes, lblOutput, lblZoom;
	
	public GToggleGroup tgFollow;	
	public GOption optTimeline, optGPSTrack, optMemory;
	public GCheckbox chkbxMovementTeleport, chkbxFollowTeleport;
	public GCheckbox chkbxFadeEdges;
	private GLabel lblHideMedia;
	public GCheckbox chkbxHideImages, chkbxHideVideos, chkbxHidePanoramas;
	public GCheckbox chkbxAlphaMode;
	public GCheckbox chkbxOrientationMode;
	public GCheckbox chkbxAngleFading, chkbxAngleThinning;

	private GLabel lblAlpha, lblBrightness, lblMediaSize;
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
	public GLabel lblCommand8;
	
	private String windowTitle = " ";
	private WMV_World world;
	
	/**
	 * Constructor for secondary window handler 
	 * @param parent Parent world
	 * @param newDisplay Parent display object
	 */
	public ML_Window( WMV_World parent, ML_Display newDisplay )
	{
		p = newDisplay;
		world = parent;
		utilities = new WMV_Utilities();
		
		mlWindowHeight = shortWindowHeight;
		navigationWindowHeight = longWindowHeight;
		timeWindowHeight = longWindowHeight;
		graphicsWindowHeight = longWindowHeight;
		modelWindowHeight = shortWindowHeight + 100;
		memoryWindowHeight = shortWindowHeight;
		selectionWindowHeight = shortWindowHeight;
		statisticsWindowHeight = longWindowHeight + 100;
		helpWindowHeight = longWindowHeight;
	}
	
	void openNavigationWindow()
	{
		if(!setupNavigationWindow)
			setupNavigationWindow();
		showNavigationWindow();
//		navigationWindow.setVisible(true);
//		showNavigationWindow = true;
	}

	void openGraphicsWindow()
	{
		if(!setupGraphicsWindow)
			setupGraphicsWindow();
		showGraphicsWindow();
//		graphicsWindow.setVisible(true);
//		showGraphicsWindow = true;
	}

	void openTimeWindow()
	{
		if(!setupTimeWindow)
			setupTimeWindow();
		showTimeWindow();
//		timeWindow.setVisible(true);
//		showTimeWindow = true;
	}

	void openModelWindow()
	{
		if(!setupModelWindow)
			setupModelWindow();
		showModelWindow();
//		modelWindow.setVisible(true);
//		showModelWindow = true;
	}

	void openMemoryWindow()
	{
		if(!setupMemoryWindow)
			setupMemoryWindow();
		showMemoryWindow();
//		memoryWindow.setVisible(true);
//		showMemoryWindow = true;
	}

	void openStatisticsWindow()
	{
		if(!setupStatisticsWindow)
			setupStatisticsWindow();
//		statisticsWindow.setVisible(true);
		showStatisticsWindow();
	}
	
	void openSelectionWindow()
	{
		if(!setupSelectionWindow)
			setupSelectionWindow();
		showSelectionWindow();
	}

	void openHelpWindow()
	{
		if(!setupHelpWindow)
			setupHelpWindow();
		showHelpWindow();
	}

	void setupMLWindow()
	{
		mlWindow = GWindow.getWindow(world.p, windowTitle, 10, 45, windowWidth, mlWindowHeight, PApplet.JAVA2D);
		mlWindow.addData(new ML_WinData());
		mlWindow.addDrawHandler(this, "mlWindowDraw");
		mlWindow.addMouseHandler(this, "mlWindowMouse");
		mlWindow.addKeyHandler(world.p, "mlWindowKey");
		mlWindow.setActionOnClose(GWindow.KEEP_OPEN);
		hideMLWindow();
		
		int x = 0, y = 12;

		x = 0;

		if(delay) world.p.delay(delayAmount);
		
		lblMainMenu = new GLabel(mlWindow, x, y, mlWindow.width, 20, "Main Menu");
		lblMainMenu.setLocalColorScheme(10);
		lblMainMenu.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblMainMenu.setTextAlign(GAlign.CENTER, null);
		lblMainMenu.setTextBold();

		x = 20;
		y += 30;
		
		if(delay) world.p.delay(delayAmount);

		optSceneView = new GOption(mlWindow, x, y, 90, 20, "World  (1)");
		optSceneView.setLocalColorScheme(10);
		optSceneView.tag = "SceneView";
		optMapView = new GOption(mlWindow, x+=100, y, 90, 20, "Map  (2)");
		optMapView.setLocalColorScheme(10);
		optMapView.tag = "MapView";
		optLibraryView = new GOption(mlWindow, x+=100, y, 100, 20, "Info  (3)");
		optLibraryView.setLocalColorScheme(10);
		optLibraryView.tag = "LibraryView";

		switch(p.displayView)
		{
			case 0:
				optSceneView.setSelected(true);
				optMapView.setSelected(false);
				optLibraryView.setSelected(false);
				break;
			case 1:
				optSceneView.setSelected(false);
				optMapView.setSelected(true);
				optLibraryView.setSelected(false);
				break;
			case 2:
				optSceneView.setSelected(false);
				optMapView.setSelected(false);
				optLibraryView.setSelected(true);
				break;
		}
		
		tgDisplayView = new GToggleGroup();
		tgDisplayView.addControls(optSceneView, optMapView, optLibraryView);
	
		x = 65;
		y += 30;
//		if(delay) world.p.delay(delayAmount);

		btnLoadMediaLibrary = new GButton(mlWindow, x, y, 180, 20, "Load Media Library  ⇧R");
		btnLoadMediaLibrary.tag = "Restart";
		btnLoadMediaLibrary.setLocalColorScheme(7);

		x = 90;
		y += 30;
//		if(delay) world.p.delay(delayAmount);

		btnNavigationWindow = new GButton(mlWindow, x, y, 125, 20, "Navigation  ⇧1");
		btnNavigationWindow.tag = "OpenNavigationWindow";
		btnNavigationWindow.setLocalColorScheme(5);
		
		x = 90;
		y += 25;
		btnTimeWindow = new GButton(mlWindow, x, y, 125, 20, "Time  ⇧2");
		btnTimeWindow.tag = "OpenTimeWindow";
		btnTimeWindow.setLocalColorScheme(5);

		x = 90;
		y += 25;

		btnGraphicsWindow = new GButton(mlWindow, x, y, 125, 20, "Graphics  ⇧3");
		btnGraphicsWindow.tag = "OpenGraphicsWindow";
		btnGraphicsWindow.setLocalColorScheme(5);
		
		x = 90;
		y += 25;
		
		btnModelWindow = new GButton(mlWindow, x, y, 125, 20, "Model  ⇧4");
		btnModelWindow.tag = "OpenModelWindow";
		btnModelWindow.setLocalColorScheme(5);

		x = 90;
		y += 25;

		btnMemoryWindow = new GButton(mlWindow, x, y, 125, 20, "Memory  ⇧5");
		btnMemoryWindow.tag = "OpenMemoryWindow";
		btnMemoryWindow.setLocalColorScheme(5);
		
		x = 90;
		y += 25;

		btnSelectionWindow = new GButton(mlWindow, x, y, 125, 20, "Selection  ⇧6");
		btnSelectionWindow.tag = "OpenSelectionWindow";
		btnSelectionWindow.setLocalColorScheme(5);
		
		x = 90;
		y += 25;
		
		btnStatisticsWindow = new GButton(mlWindow, x, y, 125, 20, "Statistics  ⇧7");
		btnStatisticsWindow.tag = "OpenStatisticsWindow";
		btnStatisticsWindow.setLocalColorScheme(5);
		
		x = 90;
		y += 25;
		
		btnHelpWindow = new GButton(mlWindow, x, y, 125, 20, "Help  ⇧8");
		btnHelpWindow.tag = "OpenHelpWindow";
		btnHelpWindow.setLocalColorScheme(5);

		x = 0;
		y = mlWindowHeight - 25;
		lblSpaceBar = new GLabel(mlWindow, x, y, mlWindow.width, 20);						/* Display Mode Label */
		lblSpaceBar.setText("Press SPACEBAR to show / hide");
		lblSpaceBar.setFont(new Font("Monospaced", Font.PLAIN, 11));
		lblSpaceBar.setLocalColorScheme(10);
		lblSpaceBar.setTextAlign(GAlign.CENTER, null);
	}
	
	/**
	 * Setup the Navigation Window
	 */
	void setupNavigationWindow()
	{
		navigationWindow = GWindow.getWindow(world.p, windowTitle, 10, 45, windowWidth, navigationWindowHeight, PApplet.JAVA2D);
		navigationWindow.setVisible(true);
		
		navigationWindow.addData(new ML_WinData());
		navigationWindow.addDrawHandler(this, "navigationWindowDraw");
		navigationWindow.addMouseHandler(this, "navigationWindowMouse");
		navigationWindow.addKeyHandler(world.p, "navigationWindowKey");
		navigationWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		int x = 0, y = 12;
		if(delay) world.p.delay(delayAmount);

		lblNavigationWindow = new GLabel(navigationWindow, x, y, navigationWindow.width, 20, "Navigation");
		lblNavigationWindow.setLocalColorScheme(10);
		lblNavigationWindow.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblNavigationWindow.setTextAlign(GAlign.CENTER, null);
		lblNavigationWindow.setTextBold();

		x = 0;
		y += 30;
//		if(delay) world.p.delay(delayAmount);

		lblClusterNavigation = new GLabel(navigationWindow, x, y, navigationWindow.width, 20, "Cluster Navigation");
		lblClusterNavigation.setLocalColorScheme(10);
		lblClusterNavigation.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblClusterNavigation.setTextAlign(GAlign.CENTER, null);
		lblClusterNavigation.setTextBold();

		x = 120;
		y += 30;
//		if(delay) world.p.delay(delayAmount);
		
		chkbxMovementTeleport = new GCheckbox(navigationWindow, x, y, 70, 20, "Teleport");
		chkbxMovementTeleport.tag = "MovementTeleport";
		chkbxMovementTeleport.setLocalColorScheme(10);

		x = 40;
		y += 25;
//		if(delay) world.p.delay(delayAmount);

		btnMoveToNearestCluster = new GButton(navigationWindow, x, y, 100, 20, "Nearest  (m)");
		btnMoveToNearestCluster.tag = "NearestCluster";
		btnMoveToNearestCluster.setLocalColorScheme(5);
		btnMoveToNearestCluster = new GButton(navigationWindow, x+110, y, 100, 20, "Last  (l)");
		btnMoveToNearestCluster.tag = "LastCluster";
		btnMoveToNearestCluster.setLocalColorScheme(5);

		x = 30;
		y += 25;
//		if(delay) world.p.delay(delayAmount);

		btnPreviousTimeSegment = new GButton(navigationWindow, x, y, 120, 20, "Previous Time  (b)");
		btnPreviousTimeSegment.tag = "PreviousTime";
		btnPreviousTimeSegment.setLocalColorScheme(5);
		btnNextTimeSegment = new GButton(navigationWindow, x+=125, y, 100, 20, "Next Time  (n)");
		btnNextTimeSegment.tag = "NextTime";
		btnNextTimeSegment.setLocalColorScheme(5);
		
		x = 25;
		y += 25;
//		if(delay) world.p.delay(delayAmount);
		
		btnJumpToRandomCluster = new GButton(navigationWindow, x+75, y, 110, 20, "Random  (j)");
		btnJumpToRandomCluster.tag = "RandomCluster";
		btnJumpToRandomCluster.setLocalColorScheme(5);
		
		x = 0;
		y += 30;
//		if(delay) world.p.delay(delayAmount);

		lblPathNavigation = new GLabel(navigationWindow, x, y, navigationWindow.width, 20, "Path Navigation");
		lblPathNavigation.setLocalColorScheme(10);
		lblPathNavigation.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblPathNavigation.setTextAlign(GAlign.CENTER, null);
		lblPathNavigation.setTextBold();

		x = 120;
		y += 25;
//		if(delay) world.p.delay(delayAmount);

		chkbxFollowTeleport = new GCheckbox(navigationWindow, x, y, 80, 20, "Teleport");
		chkbxFollowTeleport.tag = "FollowTeleport";
		chkbxFollowTeleport.setLocalColorScheme(10);

		x = 15;
		y += 25;
//		if(delay) world.p.delay(delayAmount);
	
		optTimeline = new GOption(navigationWindow, x, y, 90, 20, "Timeline");
		optTimeline.setLocalColorScheme(10);
		optTimeline.tag = "FollowTimeline";
		optTimeline.setSelected(true);
		optGPSTrack = new GOption(navigationWindow, x+=90, y, 90, 20, "GPS Track");
		optGPSTrack.setLocalColorScheme(10);
		optGPSTrack.tag = "FollowGPSTrack";
		optMemory = new GOption(navigationWindow, x+=90, y, 90, 20, "Memory");
		optMemory.setLocalColorScheme(10);
		optMemory.tag = "FollowMemory";

		tgFollow = new GToggleGroup();
		tgFollow.addControls(optTimeline, optGPSTrack, optMemory);

		x = 95;
		y += 25;
//		if(delay) world.p.delay(delayAmount);

		btnFollowStart = new GButton(navigationWindow, x, y, 60, 20, "Start");
		btnFollowStart.tag = "FollowStart";
		btnFollowStart.setLocalColorScheme(5);
		
		btnFollowStop = new GButton(navigationWindow, x+=60, y, 60, 20, "Stop");
		btnFollowStop.tag = "FollowStop";
		btnFollowStop.setLocalColorScheme(0);

		x = 160;
		y += 30;

		sdrTeleportLength = new GSlider(navigationWindow, x, y, 80, 80, 20);
		sdrTeleportLength.setLocalColorScheme(7);
		sdrTeleportLength.setLimits(0.f, 300.f, 10.f);
		sdrTeleportLength.setValue(world.viewer.getSettings().teleportLength);
		sdrTeleportLength.setRotation(PApplet.PI/2.f);
		sdrTeleportLength.setTextOrientation(G4P.ORIENT_LEFT);
		sdrTeleportLength.setEasing(0);
		sdrTeleportLength.setShowValue(true);
		sdrTeleportLength.tag = "TeleportLength";

		x = 280;

		sdrPathWaitLength = new GSlider(navigationWindow, x, y, 80, 80, 20);
		sdrPathWaitLength.setLocalColorScheme(7);
		sdrPathWaitLength.setLimits(0.f, 600.f, 30.f);
		sdrPathWaitLength.setValue(world.viewer.getSettings().pathWaitLength);
		sdrPathWaitLength.setRotation(PApplet.PI/2.f);
		sdrPathWaitLength.setTextOrientation(G4P.ORIENT_LEFT);
		sdrPathWaitLength.setEasing(0);
		sdrPathWaitLength.setShowValue(true);
		sdrPathWaitLength.tag = "PathWaitLength";
		
		x = 20;
		y += 30;
		
		lblTeleportLength = new GLabel(navigationWindow, x, y, 100, 20, "Teleport Time");
		lblTeleportLength.setLocalColorScheme(10);

		x = 165;
		
		lblPathWaitLength = new GLabel(navigationWindow, x, y, 100, 20, "Wait Time");
		lblPathWaitLength.setLocalColorScheme(10);

		x = 80;
		y += 65;
//		if(delay) world.p.delay(delayAmount);

		btnImportGPSTrack = new GButton(navigationWindow, x, y, 140, 20, "Import GPS Track  (g)");
		btnImportGPSTrack.tag = "ImportGPSTrack";
		btnImportGPSTrack.setLocalColorScheme(7);		

		x = 0;
		y += 30;
//		if(delay) world.p.delay(delayAmount);

		lblMemoryCommands = new GLabel(navigationWindow, x, y, navigationWindow.width, 20, "Memory");
		lblMemoryCommands.setLocalColorScheme(10);
		lblMemoryCommands.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblMemoryCommands.setTextAlign(GAlign.CENTER, null);
		lblMemoryCommands.setTextBold();

		x = 45;
		y += 30;
//		if(delay) world.p.delay(delayAmount);
		
		btnSaveLocation = new GButton(navigationWindow, x, y, 110, 20, "Save Location  (`)");
		btnSaveLocation.tag = "SaveLocation";
		btnSaveLocation.setLocalColorScheme(5);
		btnClearMemory = new GButton(navigationWindow, x+110, y, 100, 20, "Clear Memory");
		btnClearMemory.tag = "ClearMemory";
		btnClearMemory.setLocalColorScheme(0);

		if(world.getFields() != null)
		{
			x = 40;
			y += 30;
//			if(delay) world.p.delay(delayAmount);
			
			btnGoToPreviousField = new GButton(navigationWindow, x, y, 120, 20, "Previous Field  ⇧[");
			btnGoToPreviousField.tag = "PreviousField";
			btnGoToPreviousField.setLocalColorScheme(5);

			btnGoToNextField = new GButton(navigationWindow, x+=125, y, 100, 20, "Next Field  ⇧]");
			btnGoToNextField.tag = "NextField";
			btnGoToNextField.setLocalColorScheme(5);
		}
		
		x = 0;
		y = navigationWindowHeight - 25;
//		if(delay) world.p.delay(delayAmount);

		lblCommand1 = new GLabel(navigationWindow, x, y, navigationWindow.width, 20);						/* Display Mode Label */
		lblCommand1.setText("Press ⇧1 to show / hide");
		lblCommand1.setFont(new Font("Monospaced", Font.PLAIN, 12));
		lblCommand1.setLocalColorScheme(10);
		lblCommand1.setTextAlign(GAlign.CENTER, null);

		setupNavigationWindow = true;
	}

	/**
	 * Setup the Time Window
	 */
	void setupTimeWindow()
	{
		timeWindow = GWindow.getWindow(world.p, windowTitle, 10, 45, windowWidth, timeWindowHeight, PApplet.JAVA2D);
		timeWindow.setVisible(true);
		timeWindow.addData(new ML_WinData());
		timeWindow.addDrawHandler(this, "timeWindowDraw");
		timeWindow.addMouseHandler(this, "timeWindowMouse");
		timeWindow.addKeyHandler(world.p, "timeWindowKey");
		timeWindow.setActionOnClose(GWindow.KEEP_OPEN);

		int x = 0, y = 12;
		if(delay) world.p.delay(delayAmount);

		lblTimeWindow = new GLabel(timeWindow, x, y, timeWindow.width, 20, "Time");
		lblTimeWindow.setLocalColorScheme(10);
		lblTimeWindow.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblTimeWindow.setTextAlign(GAlign.CENTER, null);
		lblTimeWindow.setTextBold();

		y += 30;
//		if(delay) world.p.delay(delayAmount);

		lblTimeSettings = new GLabel(timeWindow, x, y, timeWindow.width, 20, "Time Settings");
		lblTimeSettings.setLocalColorScheme(10);
		lblTimeSettings.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblTimeSettings.setTextAlign(GAlign.CENTER, null);
		lblTimeSettings.setTextBold();

		x = 100;
		y += 30;
//		if(delay) world.p.delay(delayAmount);
		
		chkbxTimeFading = new GCheckbox(timeWindow, x, y, 160, 20, "Time Fading  ⇧T");
		chkbxTimeFading.tag = "TimeFading";
		chkbxTimeFading.setLocalColorScheme(10);
		chkbxTimeFading.setSelected(world.getState().timeFading);

		x = 150;
		y += 35;
//		if(delay) world.p.delay(delayAmount);

		sdrMediaLength = new GSlider(timeWindow, x, y, 80, 80, 20);
		sdrMediaLength.setLocalColorScheme(7);
		sdrMediaLength.setLimits(0.f, 250.f, 10.f);
		sdrMediaLength.setValue(world.settings.defaultMediaLength);
		sdrMediaLength.setRotation(PApplet.PI/2.f);
		sdrMediaLength.setTextOrientation(G4P.ORIENT_LEFT);
		sdrMediaLength.setEasing(0);
		sdrMediaLength.setShowValue(true);
		sdrMediaLength.tag = "MediaLength";

		x = 280;
//		if(delay) world.p.delay(delayAmount);
		
		sdrTimeCycleLength = new GSlider(timeWindow, x, y, 80, 80, 20);
		sdrTimeCycleLength.setLocalColorScheme(7);
		sdrTimeCycleLength.setLimits(0.f, 5000.f, 10.f);
		switch(world.state.timeMode)
		{
			case 0:											// Cluster
				sdrTimeCycleLength.setValue(world.getCurrentCluster().getTimeCycleLength());
				if(!sdrTimeCycleLength.isVisible())
					sdrTimeCycleLength.setVisible(true);
				break;
			case 1:											// Field
				sdrTimeCycleLength.setValue(world.settings.timeCycleLength);
				if(!sdrTimeCycleLength.isVisible())
					sdrTimeCycleLength.setVisible(true);
				break;
			case 2:											// Media
				sdrTimeCycleLength.setVisible(false);
				break;
			default:
				break;
		}
		sdrTimeCycleLength.setRotation(PApplet.PI/2.f);
		sdrTimeCycleLength.setTextOrientation(G4P.ORIENT_LEFT);
		sdrTimeCycleLength.setEasing(0);
		sdrTimeCycleLength.setShowValue(true);
		sdrTimeCycleLength.tag = "TimeCycleLength";

		x = 20;
		y += 30;
//		if(delay) world.p.delay(delayAmount);
		
		lblMediaLength = new GLabel(timeWindow, x, y, 100, 20, "Media Length");
		lblMediaLength.setLocalColorScheme(10);

		x = 150;
//		if(delay) world.p.delay(delayAmount);
		
		lblTimeCycleLength = new GLabel(timeWindow, x, y, 120, 20, "Cycle Length");
		lblTimeCycleLength.setLocalColorScheme(10);

		x = 0;
		y += 90;
//		if(delay) world.p.delay(delayAmount);

		lblTimeMode = new GLabel(timeWindow, x, y, timeWindow.width, 20, "Time Mode");
		lblTimeMode.setLocalColorScheme(10);
		lblTimeMode.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblTimeMode.setTextAlign(GAlign.CENTER, null);
		lblTimeMode.setTextBold();

		x = 30;
		y += 30;
//		if(delay) world.p.delay(delayAmount);
		
		optMediaTimeMode = new GOption(timeWindow, x, y, 90, 20, "Media");
		optMediaTimeMode.setLocalColorScheme(10);
		optMediaTimeMode.tag = "MediaTimeMode";
		optClusterTimeMode = new GOption(timeWindow, x+=90, y, 90, 20, "Cluster");
		optClusterTimeMode.setLocalColorScheme(10);
		optClusterTimeMode.tag = "ClusterTimeMode";
		optFieldTimeMode = new GOption(timeWindow, x+=90, y, 90, 20, "Field");
		optFieldTimeMode.setLocalColorScheme(10);
		optFieldTimeMode.tag = "FieldTimeMode";

		switch(world.getState().getTimeMode())
		{
			case 0:
				optClusterTimeMode.setSelected(true);
				optFieldTimeMode.setSelected(false);
				optMediaTimeMode.setSelected(false);
				break;
			case 1:
				optClusterTimeMode.setSelected(false);
				optFieldTimeMode.setSelected(true);
				optMediaTimeMode.setSelected(false);
				break;
			case 2:
				optClusterTimeMode.setSelected(false);
				optFieldTimeMode.setSelected(false);
				optMediaTimeMode.setSelected(true);
				break;
		}
		
		tgTimeMode = new GToggleGroup();
		tgTimeMode.addControls(optClusterTimeMode, optFieldTimeMode, optMediaTimeMode);

		x = 40;
		y += 30;
		if(delay) world.p.delay(delayAmount);

//		lblCurrentTime = new GLabel(timeWindow, x, y, timeWindow.width, 20, "Current Time  -:-- am");
//		lblCurrentTime.setLocalColorScheme(10);
//		lblCurrentTime.setTextAlign(GAlign.CENTER, null);
//		lblCurrentTime.setTextBold();

//		x = 40;
//		y = timeWindowHeight - 40;
//		
//		btnCloseTimeWindow = new GButton(timeWindow, x, y, 180, 20, "Close Window");
//		btnCloseTimeWindow.tag = "CloseTimeWindow";
//		btnCloseTimeWindow.setLocalColorScheme(0);

		x = 0;
		y = timeWindowHeight - 25;
		lblCommand2 = new GLabel(timeWindow, x, y, timeWindow.width, 20);						/* Display Mode Label */
		lblCommand2.setText("Press ⇧2 to show / hide");
		lblCommand2.setFont(new Font("Monospaced", Font.PLAIN, 12));
		lblCommand2.setLocalColorScheme(10);
		lblCommand2.setTextAlign(GAlign.CENTER, null);

		setupTimeWindow = true;
	}
	
	/**
	 * Setup the Graphics Window
	 */
	void setupGraphicsWindow()
	{
		graphicsWindow = GWindow.getWindow(world.p, windowTitle, 10, 45, windowWidth, graphicsWindowHeight, PApplet.JAVA2D);
		graphicsWindow.setVisible(true);
		graphicsWindow.addData(new ML_WinData());
		graphicsWindow.addDrawHandler(this, "graphicsWindowDraw");
		graphicsWindow.addMouseHandler(this, "graphicsWindowMouse");
		graphicsWindow.addKeyHandler(world.p, "graphicsWindowKey");
		graphicsWindow.setActionOnClose(GWindow.KEEP_OPEN);
	
		int x = 0, y = 12;
		if(delay) world.p.delay(delayAmount);

		lblGraphics = new GLabel(graphicsWindow, x, y, graphicsWindow.width, 20, "Graphics");
		lblGraphics.setLocalColorScheme(10);
		lblGraphics.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblGraphics.setTextAlign(GAlign.CENTER, null);
		lblGraphics.setTextBold();

		x = 80;
		y += 30;
//		if(delay) world.p.delay(delayAmount);
		
		btnZoomOut = new GButton(graphicsWindow, x, y, 40, 20, "Out");
		btnZoomOut.tag = "ZoomOut";
		btnZoomOut.setLocalColorScheme(5);
		lblZoom = new GLabel(graphicsWindow, 0, y, graphicsWindow.width, 20, "Zoom");
		lblZoom.setLocalColorScheme(10);
		lblZoom.setTextAlign(GAlign.CENTER, null);
		btnZoomIn = new GButton(graphicsWindow, x += 105, y, 40, 20, "In");
		btnZoomIn.tag = "ZoomIn";
		btnZoomIn.setLocalColorScheme(5);

		/* --- Add field of view --- */
//		applet.text(" Field of View:"+world.viewer.getFieldOfView(), x, y += lineWidth);

		x = 140;
		y += 30;
		if(delay) world.p.delay(delayAmount);

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
		y += 25;

		lblAlpha= new GLabel(graphicsWindow, x, y, 60, 20, "Alpha");
		lblAlpha.setLocalColorScheme(10);

		x += 120;

		lblBrightness = new GLabel(graphicsWindow, x, y, 90, 20, "Brightness");
		lblBrightness.setLocalColorScheme(10);

		x = 0;
		y += 75;

		lblGraphicsModes = new GLabel(graphicsWindow, x, y, graphicsWindow.width, 20, "Graphics Modes");
		lblGraphicsModes.setLocalColorScheme(10);
		lblGraphicsModes.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblGraphicsModes.setTextAlign(GAlign.CENTER, null);
		lblGraphicsModes.setTextBold();

		x = 80;
		y += 25;

		chkbxAlphaMode = new GCheckbox(graphicsWindow, x, y, 85, 20, "Alpha Mode");
		chkbxAlphaMode.tag = "AlphaMode";
		chkbxAlphaMode.setLocalColorScheme(10);
		chkbxAlphaMode.setSelected(true);

		x = 80;
		y += 25;

		chkbxAngleFading = new GCheckbox(graphicsWindow, x, y, 100, 20, "Angle Fading");
		chkbxAngleFading.tag = "AngleFading";
		chkbxAngleFading.setLocalColorScheme(10);
		chkbxAngleFading.setSelected(true);

		x = 80;
		y += 25;

		chkbxFadeEdges = new GCheckbox(graphicsWindow, x, y, 85, 20, "Fade Edges");
		chkbxFadeEdges.tag = "FadeEdges";
		chkbxFadeEdges.setLocalColorScheme(10);
		chkbxFadeEdges.setSelected(true);

		x = 80;
		y += 25;

		chkbxAngleThinning = new GCheckbox(graphicsWindow, x, y, 100, 20, "Angle Thinning");
		chkbxAngleThinning.tag = "AngleThinning";
		chkbxAngleThinning.setLocalColorScheme(10);
		chkbxAngleThinning.setSelected(false);

		x = 80;
		y += 25;
		
		chkbxOrientationMode = new GCheckbox(graphicsWindow, x, y, 160, 20, "Orientation Mode (BETA)");
		chkbxOrientationMode.tag = "OrientationMode";
		chkbxOrientationMode.setLocalColorScheme(10);
		chkbxOrientationMode.setSelected(world.viewer.getSettings().orientationMode);
		
		x = 0;
		y += 40;

		lblHideMedia = new GLabel(graphicsWindow, x, y, graphicsWindow.width, 20, "Hide Media");
		lblHideMedia.setLocalColorScheme(10);
		lblHideMedia.setTextAlign(GAlign.CENTER, null);
		lblHideMedia.setTextBold();

		x = 8;
		y += 25;

		chkbxHideImages = new GCheckbox(graphicsWindow, x, y, 90, 20, "Images");
		chkbxHideImages.tag = "HideImages";
		chkbxHideImages.setLocalColorScheme(10);
		chkbxHideImages.setSelected(false);

		chkbxHideVideos = new GCheckbox(graphicsWindow, x += 90, y, 85, 20, "Videos");
		chkbxHideVideos.tag = "HideVideos";
		chkbxHideVideos.setLocalColorScheme(10);
		chkbxHideVideos.setSelected(false);

		chkbxHidePanoramas = new GCheckbox(graphicsWindow, x += 85, y, 120, 20, "Panoramas");
		chkbxHidePanoramas.tag = "HidePanoramas";
		chkbxHidePanoramas.setLocalColorScheme(10);
		chkbxHidePanoramas.setSelected(false);

		x = 0;
		y += 30;

		lblOutput = new GLabel(graphicsWindow, x, y, graphicsWindow.width, 20, "Output");
		lblOutput.setLocalColorScheme(10);
		lblOutput.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblOutput.setTextAlign(GAlign.CENTER, null);
		lblOutput.setTextBold();

		x = 40;
		y += 30;

		btnSaveImage = new GButton(graphicsWindow, x, y, 100, 20, "Export Image");
		btnSaveImage.tag = "ExportImage";
		btnSaveImage.setLocalColorScheme(5);
		btnOutputFolder = new GButton(graphicsWindow, x+100, y, 120, 20, "Set Output Folder");
		btnOutputFolder.tag = "OutputFolder";
		btnOutputFolder.setLocalColorScheme(5);
		
		x = 0;
		y = graphicsWindowHeight - 25;
		lblCommand3 = new GLabel(graphicsWindow, x, y, graphicsWindow.width, 20);						/* Display Mode Label */
		lblCommand3.setText("Press ⇧3 to show / hide");
		lblCommand3.setFont(new Font("Monospaced", Font.PLAIN, 12));
		lblCommand3.setLocalColorScheme(10);
		lblCommand3.setTextAlign(GAlign.CENTER, null);

		setupGraphicsWindow = true;
	}
	
	/**
	 * Setup the Model Window
	 */
	void setupModelWindow()
	{
		modelWindow = GWindow.getWindow(world.p, windowTitle, 10, 45, windowWidth, modelWindowHeight, PApplet.JAVA2D);
		modelWindow.setVisible(true);
		modelWindow.addData(new ML_WinData());
		modelWindow.addDrawHandler(this, "modelWindowDraw");
		modelWindow.addMouseHandler(this, "modelWindowMouse");
		modelWindow.addKeyHandler(world.p, "modelWindowKey");
		modelWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		int x = 0, y = 12;
		if(delay) world.p.delay(delayAmount);

		lblModelWindow = new GLabel(modelWindow, x, y, modelWindow.width, 20, "Model");
		lblModelWindow.setLocalColorScheme(10);
		lblModelWindow.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblModelWindow.setTextAlign(GAlign.CENTER, null);
		lblModelWindow.setTextBold();

		y += 30;
		if(delay) world.p.delay(delayAmount);

		lblModelSettings = new GLabel(modelWindow, x, y, modelWindow.width, 20, "Model Settings");
		lblModelSettings.setLocalColorScheme(10);
		lblModelSettings.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblModelSettings.setTextAlign(GAlign.CENTER, null);
		lblModelSettings.setTextBold();

		x = 210;
		y += 30;
		if(delay) world.p.delay(delayAmount);

		sdrAltitudeScaling = new GSlider(modelWindow, x, y, 80, 80, 20);
		sdrAltitudeScaling.setLocalColorScheme(7);
		sdrAltitudeScaling.setLimits(0.f, 1.f, 0.f);
		sdrAltitudeScaling.setValue(world.settings.altitudeScalingFactor);											// -- Shouldn't be needed! Calls handler
		sdrAltitudeScaling.setRotation(PApplet.PI/2.f);
		sdrAltitudeScaling.setTextOrientation(G4P.ORIENT_LEFT);
		sdrAltitudeScaling.setEasing(0);
		sdrAltitudeScaling.setShowValue(true);
		sdrAltitudeScaling.tag = "AltitudeScaling";
		
		x = 50;
		y += 25;
		
		lblAltitudeScaling = new GLabel(modelWindow, x, y, 100, 20, "Altitude Scaling");
		lblAltitudeScaling .setLocalColorScheme(10);

		y += 65;
		x = 80;

		btnSubjectDistanceDown = new GButton(modelWindow, x, y, 30, 20, "-");
		btnSubjectDistanceDown.tag = "SubjectDistanceDown";
		btnSubjectDistanceDown.setLocalColorScheme(5);
		lblSubjectDistance = new GLabel(modelWindow, x += 35, y, 110, 20, "Subject Distance");
		lblSubjectDistance.setLocalColorScheme(10);
		lblSubjectDistance.setTextAlign(GAlign.CENTER, null);
		lblSubjectDistance.setTextBold();
		btnSubjectDistanceUp = new GButton(modelWindow, x += 125, y, 30, 20, "+");
		btnSubjectDistanceUp.tag = "SubjectDistanceUp";
		btnSubjectDistanceUp.setLocalColorScheme(5);
		
		x = 0;
		y += 30;

		lblModelDisplay = new GLabel(modelWindow, x, y, modelWindow.width, 20, "Model Display");
		lblModelDisplay.setLocalColorScheme(10);
		lblModelDisplay.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblModelDisplay.setTextAlign(GAlign.CENTER, null);
		lblModelDisplay.setTextBold();

		x = 100;
		y += 30;

		chkbxShowModel = new GCheckbox(modelWindow, x, y, 120, 20, "Show Model  (5)");
		chkbxShowModel.tag = "ShowModel";
		chkbxShowModel.setLocalColorScheme(10);
		
		x = 115;
		y += 30;
		
		chkbxMediaToCluster = new GCheckbox(modelWindow, x, y, 150, 20, "Media to Cluster  (6)");
		chkbxMediaToCluster.tag = "MediaToCluster";
		chkbxMediaToCluster.setLocalColorScheme(10);
		chkbxMediaToCluster.setSelected(false);
		
		x = 115;
		y += 25;

		chkbxCaptureToMedia = new GCheckbox(modelWindow, x, y, 150, 20, "Capture to Media  (7)");
		chkbxCaptureToMedia.tag = "CaptureToMedia";
		chkbxCaptureToMedia.setLocalColorScheme(10);
		chkbxCaptureToMedia.setSelected(false);

		x = 115;
		y += 25;

		chkbxCaptureToCluster = new GCheckbox(modelWindow, x, y, 170, 20, "Capture to Cluster  (8)");
		chkbxCaptureToCluster.tag = "CaptureToCluster";
		chkbxCaptureToCluster.setLocalColorScheme(10);
		chkbxCaptureToCluster.setSelected(false);

		x = 0;
		y = modelWindowHeight - 25;
		lblCommand4 = new GLabel(modelWindow, x, y, modelWindow.width, 20);						/* Display Mode Label */
		lblCommand4.setText("Press ⇧4 to show / hide");
		lblCommand4.setFont(new Font("Monospaced", Font.PLAIN, 12));
		lblCommand4.setLocalColorScheme(10);
		lblCommand4.setTextAlign(GAlign.CENTER, null);

		setupModelWindow = true;
	}

	/**
	 * Setup the Memory Window
	 */
	void setupMemoryWindow()
	{
		memoryWindow = GWindow.getWindow(world.p, "Memory", 10, 45, windowWidth, memoryWindowHeight, PApplet.JAVA2D);
		memoryWindow.setVisible(true);
		memoryWindow.addData(new ML_WinData());
		memoryWindow.addDrawHandler(this, "memoryWindowDraw");
		memoryWindow.addMouseHandler(this, "memoryWindowMouse");
		memoryWindow.addKeyHandler(world.p, "memoryWindowKey");
		memoryWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		int x = 0, y = 10;

		lblMemory = new GLabel(memoryWindow, x, y, memoryWindow.width, 20, "Memory");
		lblMemory.setLocalColorScheme(10);
		lblMemory.setTextAlign(GAlign.CENTER, null);
		lblMemory.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblMemory.setTextBold();

		x = 0;
		y = memoryWindowHeight - 25;
		lblCommand5 = new GLabel(memoryWindow, x, y, memoryWindow.width, 20);						/* Display Mode Label */
		lblCommand5.setText("Press ⇧5 to show / hide");
		lblCommand5.setFont(new Font("Monospaced", Font.PLAIN, 12));
		lblCommand5.setLocalColorScheme(10);
		lblCommand5.setTextAlign(GAlign.CENTER, null);
		
		setupMemoryWindow = true;
	}

	/**
	 * Setup the Selection Window
	 */
	void setupSelectionWindow()
	{
		selectionWindow = GWindow.getWindow(world.p, "Selection Mode", 10, 45, windowWidth, selectionWindowHeight, PApplet.JAVA2D);
		selectionWindow.setVisible(true);
		selectionWindow.addData(new ML_WinData());
		selectionWindow.addDrawHandler(this, "selectionWindowDraw");
		selectionWindow.addMouseHandler(this, "selectionWindowMouse");
		selectionWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		int x = 0, y = 10;
		if(delay) world.p.delay(delayAmount);

		lblSelection = new GLabel(selectionWindow, x, y, selectionWindow.width, 20, "Selection");
		lblSelection.setLocalColorScheme(10);
		lblSelection.setTextAlign(GAlign.CENTER, null);
		lblSelection.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblSelection.setTextBold();

		x = 120;
		y += 30;

		chkbxSelectionMode = new GCheckbox(selectionWindow, x, y, 90, 20, "Enable");
		chkbxSelectionMode.tag = "SelectionMode";
		chkbxSelectionMode.setFont(new Font("Monospaced", Font.PLAIN, 14));
		chkbxSelectionMode.setLocalColorScheme(10);
		
		x = 100;
		y += 35;
		
		btnSelectFront = new GButton(selectionWindow, x, y, 110, 20, "Select (x)");
		btnSelectFront.tag = "SelectFront";
		btnSelectFront.setLocalColorScheme(5);

		x = 100;
		y += 30;
		
		btnDeselectFront = new GButton(selectionWindow, x, y, 110, 20, "Deselect (X)");
		btnDeselectFront.tag = "DeselectFront";
		btnDeselectFront.setLocalColorScheme(5);

		x = 95;
		y += 25;

		btnDeselectAll = new GButton(selectionWindow, x, y, 120, 20, "Deselect All...");
		btnDeselectAll.tag = "DeselectAll";
		btnDeselectAll.setLocalColorScheme(5);

		x = 0;
		y += 30;

		lblSelectionOptions = new GLabel(selectionWindow, x, y, selectionWindow.width, 20, "Options");
		lblSelectionOptions.setLocalColorScheme(10);
		lblSelectionOptions.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblSelectionOptions.setTextAlign(GAlign.CENTER, null);
		lblSelectionOptions.setTextBold();

		x = 95;
		y += 25;
		
		chkbxMultiSelection = new GCheckbox(selectionWindow, x, y, 180, 20, "Allow Multiple");
		chkbxMultiSelection.tag = "MultiSelection";
		chkbxMultiSelection.setLocalColorScheme(10);

		x = 95;
		y += 25;
		
		chkbxSegmentSelection = new GCheckbox(selectionWindow, x, y, 180, 20, "Select Groups");
		chkbxSegmentSelection.tag = "SegmentSelection";
		chkbxSegmentSelection.setLocalColorScheme(10);
		
		x = 95;
		y += 25;
		
		chkbxShowMetadata = new GCheckbox(selectionWindow, x, y, 110, 20, "View Metadata");
		chkbxShowMetadata.tag = "ViewMetadata";
		chkbxShowMetadata.setLocalColorScheme(10);

		x = 85;
		y += 25;

		btnStitchPanorama = new GButton(selectionWindow, x, y, 140, 20, "Stitch Selection  (⇧\\)");
		btnStitchPanorama.tag = "StitchPanorama";
		btnStitchPanorama.setLocalColorScheme(7);
		
		x = 0;
		y = selectionWindowHeight - 25;
		lblCommand6 = new GLabel(selectionWindow, x, y, selectionWindow.width, 20);						/* Display Mode Label */
		lblCommand6.setText("Press ⇧6 to show / hide");
		lblCommand6.setFont(new Font("Monospaced", Font.PLAIN, 12));
		lblCommand6.setLocalColorScheme(10);
		lblCommand6.setTextAlign(GAlign.CENTER, null);
		
		selectionWindow.addKeyHandler(world.p, "selectionWindowKey");
		setupSelectionWindow = true;
	}

	/**
	 * Setup the Statistics Window
	 */
	void setupStatisticsWindow()
	{
		statisticsWindow = GWindow.getWindow(world.p, "Statistics", 10, 45, windowWidth * 2, statisticsWindowHeight, PApplet.JAVA2D);
		statisticsWindow.setVisible(true);
		statisticsWindow.addData(new ML_WinData());
		statisticsWindow.addDrawHandler(this, "statisticsWindowDraw");
		statisticsWindow.addMouseHandler(this, "statisticsWindowMouse");
		statisticsWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		int x = 0, y = 10;
		if(delay) world.p.delay(delayAmount);

		lblStatistics = new GLabel(statisticsWindow, x, y, statisticsWindow.width, 20, "Statistics");
		lblStatistics.setLocalColorScheme(10);
		lblStatistics.setTextAlign(GAlign.CENTER, null);
		lblStatistics.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblStatistics.setTextBold();

		x = 0;
		y = statisticsWindowHeight - 25;
		lblCommand7 = new GLabel(statisticsWindow, x, y, statisticsWindow.width, 20);						/* Display Mode Label */
		lblCommand7.setText("Press ⇧7 to show / hide");
		lblCommand7.setFont(new Font("Monospaced", Font.PLAIN, 12));
		lblCommand7.setLocalColorScheme(10);
		lblCommand7.setTextAlign(GAlign.CENTER, null);
		
		statisticsWindow.addKeyHandler(world.p, "statisticsWindowKey");
		
		setupStatisticsWindow = true;
	}
	
	/**
	 * Setup the Help Window
	 */
	void setupHelpWindow()
	{
		helpWindow = GWindow.getWindow(world.p, "Help", 10, 45, windowWidth, helpWindowHeight, PApplet.JAVA2D);
		helpWindow.setVisible(true);
		helpWindow.addData(new ML_WinData());
		helpWindow.addDrawHandler(this, "helpWindowDraw");
		helpWindow.addMouseHandler(this, "helpWindowMouse");
		helpWindow.addKeyHandler(world.p, "helpWindowKey");
		
		int x = 0, y = 10;
		if(delay) world.p.delay(delayAmount);

		/* Selection Window */
		lblHelp = new GLabel(helpWindow, x, y, helpWindow.width, 20, "Help");
		lblHelp.setLocalColorScheme(10);
		lblHelp.setTextAlign(GAlign.CENTER, null);
		lblHelp.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblHelp.setTextBold();

		x = 0;
		y = helpWindowHeight - 25;
		lblCommand8 = new GLabel(helpWindow, x, y, helpWindow.width, 20);						/* Display Mode Label */
		lblCommand8.setText("Press ⇧8 to show / hide");
		lblCommand8.setFont(new Font("Monospaced", Font.PLAIN, 12));
		lblCommand8.setLocalColorScheme(10);
		lblCommand8.setTextAlign(GAlign.CENTER, null);
		
		setupHelpWindow = true;
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
	 * Handles drawing to the Time Window 
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 */
	public void timeWindowDraw(PApplet applet, GWinData data) {
		if(world.p.state.running)
		{
			float lineWidthVeryWide = 20f;
			float lineWidthWide = 15f;
			float lineWidth = 15f;

			float mediumTextSize = 13.f;
			float smallTextSize = 11.f;

			float x = 10, y = 300;			
			applet.background(0);
			applet.stroke(255);
			applet.strokeWeight(1);
			applet.fill(255, 255, 255);

			applet.textSize(smallTextSize);
			
			int mode = world.p.world.getState().getTimeMode();
			if( mode == 0 || mode == 1 )
			{
				int curTime = (mode == 0) ? world.getCurrentCluster().getState().currentTime : world.getState().currentTime;
				applet.text(" Current Time: "+ curTime, x, y += lineWidth);
			}

			WMV_Field f = world.getCurrentField();

			switch(mode)
			{
				case 0:
//					applet.text(" Time Mode: Cluster", x, y += lineWidthVeryWide);
					if(f.getTimeline().timeline.size() > 0 && world.viewer.getCurrentFieldTimeSegment() >= 0 && world.viewer.getCurrentFieldTimeSegment() < f.getTimeline().timeline.size())
					{
						applet.text(" Upper: "+f.getTimeSegment(world.viewer.getCurrentFieldTimeSegment()).getUpper().getTime()+
								" Center:"+f.getTimeSegment(world.viewer.getCurrentFieldTimeSegment()).getCenter().getTime()+
								" Lower: "+f.getTimeSegment(world.viewer.getCurrentFieldTimeSegment()).getLower().getTime(), x, y += lineWidthVeryWide);
						applet.text(" Current Cluster Timeline Size: "+ world.getCurrentCluster().getTimeline().timeline.size(), x, y += lineWidthWide);
					}
					else
					{
						applet.text(" Current Cluster Timeline Size: "+ world.getCurrentCluster().getTimeline().timeline.size(), x, y += lineWidthVeryWide);
					}
					applet.text(" Current Cluster Dateline Size: "+ world.getCurrentCluster().getDateline().size(), x, y += lineWidth);
					
					break;
				case 1:
//					applet.text(" Time Mode: Field", x, y += lineWidthVeryWide);
					break;
				case 2:
//					applet.text(" Time Mode: Media", x, y += lineWidthVeryWide);
					applet.text(" Current Media: "+ world.viewer.getCurrentMedia(), x, y += lineWidth);		// -- Not very meaningful.. should show media index / type
					break;
//				case 3:
//					applet.text(" Time Mode: Flexible"), x, y += lineWidthVeryWide);
//					break;
			}
			
//			applet.text(" Current Field Time: "+ world.currentTime, x, y += lineWidth);
			applet.text(" Current Field Time Segment: "+ world.viewer.getCurrentFieldTimeSegment(), x, y += lineWidthVeryWide);
			applet.text(" Current Field Timeline Size: "+ world.getCurrentField().getTimeline().timeline.size(), x, y += lineWidth);
			applet.text(" Current Field Dateline Size: "+ world.getCurrentField().getDateline().size(), x, y += lineWidth);
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
		if(world.p.state.running)
		{
			applet.background(10, 5, 50);
			applet.stroke(255);
			applet.strokeWeight(1);
			applet.fill(0, 0, 255);

			float lineWidthVeryWide = 20f;
			float lineWidth = 14f;

			float x = 10;
			float y = 50;			// Starting vertical position

			float largeTextSize = 18.f;
			float mediumTextSize = 16.f;
			float smallTextSize = 14.f;

			WMV_Field f = world.getCurrentField();
			if(world.viewer.getState().getCurrentClusterID() >= 0)
			{
				WMV_Cluster c = world.getCurrentCluster();

				applet.fill(185, 215, 255, 255);					// Set text color
				applet.textSize(largeTextSize);
				applet.text(" --- Program Modes --- ", x, y += lineWidthVeryWide);
				applet.textSize(smallTextSize);
				applet.text(" Alpha Mode:"+world.getState().alphaMode, x, y += lineWidth);
				applet.text(" Orientation Mode (BETA): "+world.viewer.getSettings().orientationMode, x, y += lineWidthVeryWide);
//				applet.text(" Altitude Scaling: "+world.altitudeScaling, x, y += lineWidth);
//				applet.text(" Lock Media to Clusters:"+world.lockMediaToClusters, x, y += lineWidth);

				applet.textSize(largeTextSize);
				applet.text(" --- World --- ", x, y += lineWidthVeryWide);
				applet.textSize(mediumTextSize);
				applet.text(" Field Count: "+world.getFields().size(), x, y += lineWidthVeryWide);
				applet.text(" Current Field: "+f.getName(), x, y += lineWidthVeryWide);
				applet.textSize(smallTextSize);
				applet.text(" ID: "+(world.viewer.getState().getField()+1)+" out of "+world.getFieldCount()+" Total Fields", x, y += lineWidthVeryWide);
				applet.text(" Width: "+f.getModel().getState().fieldWidth+" Length: "+f.getModel().getState().fieldLength+" Height: "+f.getModel().getState().fieldHeight, x, y += lineWidth);
				applet.text(" Image Count: "+f.getImageCount(), x, y += lineWidth);					// Doesn't check for dataMissing!!
				applet.text(" Panorama Count: "+f.getPanoramaCount(), x, y += lineWidth);			// Doesn't check for dataMissing!!
				applet.text(" Video Count: "+f.getVideoCount(), x, y += lineWidth);					// Doesn't check for dataMissing!!
				applet.text(" Sound Count: "+f.getSoundCount(), x, y += lineWidth);					// Doesn't check for dataMissing!!
				applet.text(" Media Density (per sq. m.): "+f.getModel().getState().mediaDensity, x, y += lineWidth);
//				applet.text(" Clusters Visible: "+world.viewer.clustersVisible+"  (Orientation Mode)", x, y += lineWidth);
			
				applet.textSize(largeTextSize);
				applet.text(" --- Viewer --- ", x, y += lineWidthVeryWide);
				applet.textSize(smallTextSize);
				applet.text(" Location, x: "+PApplet.round(world.viewer.getLocation().x)+" y:"+PApplet.round(world.viewer.getLocation().y)+" z:"+
						PApplet.round(world.viewer.getLocation().z), x, y += lineWidthVeryWide);		
				applet.text(" GPS Longitude: "+world.viewer.getGPSLocation().x+" Latitude:"+world.viewer.getGPSLocation().y, x, y += lineWidth);		

				applet.text(" Images Visible: "+f.getImagesVisible(), x, y += lineWidth);
				applet.text("   Images Seen: "+f.getImagesSeen(), x, y += lineWidth);
				applet.text(" Panoramas Visible: "+f.getPanoramasVisible(), x, y += lineWidth);
				applet.text("   Panoramas Seen: "+f.getPanoramasSeen(), x, y += lineWidth);
				applet.text(" Videos Visible: "+f.getVideosVisible(), x, y += lineWidth);
				applet.text("   Videos Seen: "+f.getVideosSeen(), x, y += lineWidth);
				applet.text("   Currently Playing: "+f.getVideosPlaying(), x, y += lineWidth);
				applet.text(" Sounds Audible: "+f.getSoundsAudible(), x, y += lineWidth);
				applet.text("   Currently Playing: "+f.getSoundsPlaying(), x, y += lineWidth);

				applet.textSize(largeTextSize);
				applet.text(" --- Output --- ", x, y += lineWidthVeryWide);
				applet.textSize(smallTextSize);
				applet.text(" Image Output Folder:"+world.outputFolder, x, y += lineWidthVeryWide);
				applet.text(" Library Folder:"+world.p.library.getLibraryFolder(), x, y += lineWidth);

				if(world.p.debugSettings.memory)
				{
					if(world.p.debugSettings.detailed)
					{
						applet.text("Total memory (bytes): " + world.p.totalMemory, x, y += lineWidth);
						applet.text("Available processors (cores): "+world.p.availableProcessors, x, y += lineWidth);
						applet.text("Maximum memory (bytes): " +  (world.p.maxMemory == Long.MAX_VALUE ? "no limit" : world.p.maxMemory), x, y += lineWidth); 
						applet.text("Total memory (bytes): " + world.p.totalMemory, x, y += lineWidth);
						applet.text("Allocated memory (bytes): " + world.p.allocatedMemory, x, y += lineWidth);
					}
					applet.text("Free memory (bytes): "+world.p.freeMemory, x, y += lineWidth);
					applet.text("Approx. usable free memory (bytes): " + world.p.approxUsableFreeMemory, x, y += lineWidth);
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

		float largeTextSize = 15.f;
		float mediumTextSize = 13.f;
		float smallTextSize = 11.f;

		applet.fill(255, 255, 255, 255);                        
		applet.textSize(largeTextSize);
		applet.text(" About ", xPos, yPos);
		
		yPos += lineWidthWide;

		applet.textSize(smallTextSize);
		applet.text(" WorldMediaViewer is a 3D interactive browsing ", xPos, yPos += lineWidth);
		applet.text(" and navigation system for large multimedia ", xPos, yPos += lineWidth);
		applet.text(" collections, based on EXIF metadata.", xPos, yPos += lineWidth);

		yPos += lineWidthVeryWide;
		
		applet.textSize(largeTextSize);
		applet.text(" Keyboard Controls ", xPos, yPos += lineWidthVeryWide);

		yPos += lineWidthWide;
		
		applet.textSize(mediumTextSize);
		applet.text(" General", xPos, yPos += lineWidthVeryWide);
		applet.textSize(smallTextSize);
		applet.text(" R    Restart WorldMediaViewer", xPos, yPos += lineWidthVeryWide);
		applet.text(" CMD + q    Quit WorldMediaViewer", xPos, yPos += lineWidth);

		/* Movement */
		applet.textSize(mediumTextSize);
		applet.text(" Movement", xPos, yPos += lineWidthVeryWide);
		applet.textSize(smallTextSize);
//		applet.text(" Navigate using the arrow keys, A, S, D and W.", xPos, yPos += lineWidth);
//		applet.text(" For additional keyboard controls, see below.", xPos, yPos += lineWidth);
		applet.text(" a d w s   Walk Left / Right / Forward / Backward ", xPos, yPos += lineWidthVeryWide);
		applet.text(" Arrows    Turn Camera ", xPos, yPos += lineWidth);
		applet.text(" q z  Zoom In / Out + / - ", xPos, yPos += lineWidth);
		
//		applet.textSize(mediumTextSize);
//		applet.text(" Display", xPos, yPos += lineWidthVeryWide);
//		applet.textSize(smallTextSize);
//		applet.text(" 1    Show/Hide Field Map   		  +SHIFT to Overlay", xPos, yPos += lineWidthVeryWide);
//		applet.text(" 2    Show/Hide Field Statistics    +SHIFT to Overlay", xPos, yPos += lineWidth);
//		applet.text(" 3    Show/Hide Cluster Statistics  +SHIFT to Overlay", xPos, yPos += lineWidth);
//		applet.text(" 4    Show/Hide Keyboard Controls   +SHIFT to Overlay", xPos, yPos += lineWidth);

		applet.textSize(mediumTextSize);
		applet.text(" Time", xPos, yPos += lineWidthVeryWide);
		applet.textSize(smallTextSize);
		applet.text(" T    Time Fading On/Off", xPos, yPos += lineWidthVeryWide);
//		applet.text(" Z    Toggle Time Fading Mode (Field/Cluster)", textXPos, textYPos += lineWidth);
		applet.text(" space Pause On/Off   ", xPos, yPos += lineWidth);
		applet.text(" &/*  Default Media Length - / +", xPos, yPos += lineWidth);
		applet.text(" ⇧ Lt/Rt   Cycle Length - / +", xPos, yPos += lineWidth);
		applet.text(" ⇧ Up/Dn   Current Time - / +", xPos, yPos += lineWidth);

//		applet.textSize(mediumTextSize);
//		applet.text(" Time Navigation", xPos, yPos += lineWidthVeryWide);
//		applet.textSize(smallTextSize);
		applet.text(" n    Move to Next Time Segment in Field", xPos, yPos += lineWidthWide);
		applet.text(" b    Move to Previous Time Segment in Field", xPos, yPos += lineWidth);

		/* Model */
		applet.textSize(mediumTextSize);
		applet.text(" Model", xPos, yPos += lineWidthVeryWide);
		applet.textSize(smallTextSize);
		applet.text(" [ ]  Altitude Scaling Adjustment  + / - ", xPos, yPos += lineWidthVeryWide);
//		applet.text(" , .  Object Distance  + / - ", xPos, yPos += lineWidth);
		applet.text(" - =  Object Distance  - / +      ", xPos, yPos += lineWidth);
		applet.text(" ⌥ -   Visible Angle  -      ", xPos, yPos += lineWidth);
		applet.text(" ⌥ =   Visible Angle  +      ", xPos, yPos += lineWidth);
		
		/* Graphics */
		applet.textSize(mediumTextSize);
		applet.text(" Graphics", xPos, yPos += lineWidthVeryWide);
		applet.textSize(smallTextSize);
		applet.text(" G    Angle Fading On/Off", xPos, yPos += lineWidthVeryWide);
		applet.text(" H    Angle Thinning On/Off", xPos, yPos += lineWidth);
		applet.text(" P    Transparency Mode  On / Off      ", xPos, yPos += lineWidth);
		applet.text(" ( )  Blend Mode  - / +      ", xPos, yPos += lineWidth);
		applet.text(" i h v  Hide images / panoramas / videos    ", xPos, yPos += lineWidth);
		applet.text(" D    Video Mode On/Off ", xPos, yPos += lineWidth);

		/* Navigation */
		applet.textSize(mediumTextSize);
		applet.text(" Navigation", xPos, yPos += lineWidthVeryWide);
		applet.textSize(smallTextSize);
		applet.text(" >    Follow Timeline", xPos, yPos += lineWidthVeryWide);
//		applet.text(" >    Follow Date-Independent Timeline", xPos, yPos += lineWidthVeryWide);
//		applet.text(" .    Follow Date-Specific Timeline", xPos, yPos += lineWidth);
		applet.text(" E    Move to Nearest Cluster", xPos, yPos += lineWidthWide);
//		applet.text(" W    Move to Nearest Cluster in Front", xPos, yPos += lineWidth);
//		applet.text(" Q    Move to Next Cluster in Time", xPos, yPos += lineWidth);
//		applet.text(" A    Move to Next Location in Memory", xPos, yPos += lineWidth);
		applet.text(" J    Teleport to Random Cluster", xPos, yPos += lineWidth);
		applet.text(" U    Move to Next Video ", xPos, yPos += lineWidthWide);
		applet.text(" u    Teleport to Next Video ", xPos, yPos += lineWidth);
		applet.text(" M    Move to Next Panorama ", xPos, yPos += lineWidth);
		applet.text(" m    Teleport to Next Panorama ", xPos, yPos += lineWidth);
//		applet.text(" C    Lock Viewer to Nearest Cluster On/Off", xPos, yPos += lineWidthWide);
//		applet.text(" l    Look At Selected Media", xPos, yPos += lineWidth);
//		applet.text(" L    Look for Media", xPos, yPos += lineWidth);
		applet.text(" { }  Teleport to Next / Previous Field ", xPos, yPos += lineWidth);

		applet.textSize(mediumTextSize);
		applet.text(" Interaction", xPos, yPos += lineWidthVeryWide);
		applet.textSize(smallTextSize);
		applet.text(" O    Selection Mode On/Off", xPos, yPos += lineWidthVeryWide);
//		applet.text(" S    Multi-Selection Mode On/Off", xPos, yPos += lineWidth);
//		applet.text(" OPT + s    Segment Selection Mode On/Off", xPos, yPos += lineWidthWide);
		applet.text(" x    Select Media in Front", xPos, yPos += lineWidth);
		applet.text(" X    Deselect Media in Front", xPos, yPos += lineWidth);
		applet.text(" OPT + x    Deselect All Media", xPos, yPos += lineWidth);

		applet.textSize(mediumTextSize);
		applet.text(" GPS Tracks", xPos, yPos += lineWidthVeryWide);
		applet.textSize(smallTextSize);
		applet.text(" g    Load GPS Track from File", xPos, yPos += lineWidthVeryWide);
		applet.text(" OPT + g    Follow GPS Track", xPos, yPos += lineWidth);
//		applet.text(" y	  Navigate Memorized Places", xPos, yPos += lineWidth);
//		applet.text(" Y    Clear Memory", xPos, yPos += lineWidth);

		applet.textSize(mediumTextSize);
//		applet.text(" Memory", xPos, yPos += lineWidthVeryWide);
//		applet.textSize(smallTextSize);
//		applet.text(" `    Save Current View to Memory", xPos, yPos += lineWidthVeryWide);
//		applet.text(" ~    Follow Memory Path", xPos, yPos += lineWidth);
//		applet.text(" Y    Clear Memory", xPos, yPos += lineWidth);

		applet.textSize(mediumTextSize);
		applet.text(" Output", xPos, yPos += lineWidthVeryWide);
		applet.textSize(smallTextSize);
		applet.text(" o    Set Image Output Folder", xPos, yPos += lineWidthVeryWide);
		applet.text(" p    Save Screen Image to Disk", xPos, yPos += lineWidth);
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

		float largeTextSize = 15.f;
		float mediumTextSize = 13.f;
		float smallTextSize = 11.f;

		applet.fill(255, 255, 255, 255);                        
		
		applet.textSize(largeTextSize);
		applet.text("Memory", xPos, yPos);

		yPos += lineWidthWide;

		applet.textSize(smallTextSize);
		applet.text("Points in Memory:"+world.viewer.getMemoryPath().size(), xPos, yPos += lineWidthVeryWide);
		
		yPos += lineWidthWide * 2.f;

		//		applet.textSize(mediumTextSize);
//		applet.text(" Memory", xPos, yPos += lineWidthVeryWide);
		applet.textSize(largeTextSize);
		applet.text(" Keyboard Controls ", xPos, yPos);
		applet.textSize(smallTextSize);
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
	
	void showWMVWindow()
	{
		showMLWindow = true;
		mlWindow.setVisible(true);
	} 
	
	void showNavigationWindow()
	{
		showNavigationWindow = true;
		if(setupNavigationWindow)
			navigationWindow.setVisible(true);
		if(showMLWindow)
			hideMLWindow();
	} 
	void showTimeWindow()
	{
		showTimeWindow = true;
		if(setupTimeWindow)
			timeWindow.setVisible(true);
		if(showMLWindow)
			hideMLWindow();
	}
	void showGraphicsWindow()
	{
		showGraphicsWindow = true;
		if(setupGraphicsWindow)
			graphicsWindow.setVisible(true);
		if(showMLWindow)
			hideMLWindow();
	}
	void showModelWindow()
	{
		showModelWindow = true;
		if(setupModelWindow)
			modelWindow.setVisible(true);
		if(showMLWindow)
			hideMLWindow();
	}
	void showMemoryWindow()
	{
		showMemoryWindow = true;
		if(setupMemoryWindow)
			memoryWindow.setVisible(true);
		if(showMLWindow)
			hideMLWindow();
	}
	void showSelectionWindow()
	{
		showSelectionWindow = true;
		if(setupSelectionWindow)
			selectionWindow.setVisible(true);
		if(showMLWindow)
			hideMLWindow();
	} 
	void showStatisticsWindow()
	{
		showStatisticsWindow = true;
		if(setupStatisticsWindow)
			statisticsWindow.setVisible(true);
		if(showMLWindow)
			hideMLWindow();
	} 
	void showHelpWindow()
	{
		showHelpWindow = true;
		if(setupHelpWindow)
			helpWindow.setVisible(true);
	}
	void hideMLWindow()
	{
		showMLWindow = false;
		mlWindow.setVisible(false);
	} 
	void hideNavigationWindow()
	{
		showNavigationWindow = false;
		if(setupNavigationWindow)
			navigationWindow.setVisible(false);
	} 
	void hideTimeWindow()
	{
		showTimeWindow = false;
		if(setupTimeWindow)
			timeWindow.setVisible(false);
	} 
	void hideGraphicsWindow()
	{
		showGraphicsWindow = false;
		if(setupGraphicsWindow)
			graphicsWindow.setVisible(false);
	}
	void hideModelWindow()
	{
		showModelWindow = false;
		if(setupModelWindow)
			modelWindow.setVisible(false);
	} 
	void hideMemoryWindow()
	{
		showMemoryWindow = false;
		if(setupMemoryWindow)
			memoryWindow.setVisible(false);
	}	
	void hideSelectionWindow()
	{
		showSelectionWindow = false;
		if(setupSelectionWindow)
			selectionWindow.setVisible(false);
	} 
	void hideStatisticsWindow()
	{
		showStatisticsWindow = false;
		if(setupStatisticsWindow)
			statisticsWindow.setVisible(false);
	} 
	void hideHelpWindow()
	{
		showHelpWindow = false;
		if(setupHelpWindow)
			helpWindow.setVisible(false);
	}
	/**
	 * Hide all windows
	 */
	void hideWindows()
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