package wmViewer;

import java.awt.Font;

import g4p_controls.*;
import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

public class WMV_Window {

	WMV_Display p;
	
	private int windowWidth = 310, longWindowHeight = 600;
	private int shortWindowHeight = 340;
	
	/* Windows */
	public GWindow wmvWindow, timeWindow, navigationWindow, graphicsWindow, modelWindow, selectionWindow, statisticsWindow, helpWindow,
				   memoryWindow;
	private GLabel lblWMViewer, lblNavigationWindow, lblGraphics, lblStatistics, lblHelp, lblMemory;				// Window headings
	public boolean showWMVWindow = false, showNavigationWindow = false, showTimeWindow = false, showGraphicsWindow = false, showModelWindow = false,
				    showSelectionWindow = false, showStatisticsWindow = false, showHelpWindow = false, showMemoryWindow = false;
	public boolean setupTimeWindow = false, setupNavigationWindow = false, setupGraphicsWindow = false, setupModelWindow = false, setupHelpWindow = false, 
					setupSelectionWindow = false, setupStatisticsWindow = false, setupMemoryWindow = false;
	//	private GSketchPad sketchPad;

	/* Main Menu */
	private GButton btnNavigationWindow, btnTimeWindow, btnGraphicsWindow, btnModelWindow, btnSelectionWindow,
				    btnStatisticsWindow, btnHelpWindow, btnMemoryWindow;
//	private GButton btnSceneView, btnMapView, btnClusterView, btnInfoView, btnControlView;		
	private GButton btnLoadMediaLibrary;
	private GLabel lblSpaceBar;
	public GToggleGroup tgDisplayView;	
	public GOption optSceneView, optMapView, optClusterView;
	int wmvWindowHeight;

	/* Time Window */
	private GLabel lblTimeWindow, lblTimeSettings, lblTimeMode;
	private GLabel lblMediaLength;
//	public GLabel lblCurrentTime;
	public GLabel lblTime;
	public GCheckbox chkbxTimeFading;
	public GSlider sdrMediaLength;
	public GToggleGroup tgTimeMode;	
	public GOption optClusterTimeMode, optFieldTimeMode, optMediaTimeMode;
//	public GButton btnCloseTimeWindow;
	int timeWindowHeight;

	/* Navigation Window */
	private GLabel lblClusterNavigation, lblMemoryCommands, lblPathNavigation;
	private GButton btnImportGPSTrack;
	private GButton btnSaveLocation, btnClearMemory;
	private GButton btnJumpToRandomCluster;
	private GButton btnZoomIn, btnZoomOut;
	private GButton btnSaveImage, btnOutputFolder;

	private GButton btnNextTimeSegment, btnPreviousTimeSegment;
	private GButton btnMoveToNearestCluster;
	private GButton btnGoToPreviousField, btnGoToNextField;
	private GButton btnFollowStart, btnFollowStop;	
//	public GButton btnCloseNavigationWindow;
	int navigationWindowHeight;

	/* Graphics Window */
//	public GLabel lblDisplayMode;
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
//	public GButton btnCloseGraphicsWindow;
	int graphicsWindowHeight;

	/* Model Window */
	private GLabel lblModelWindow, lblAltitudeScaling, lblModelSettings, lblModelDisplay;
	public GCheckbox chkbxShowModel, chkbxMediaToCluster, chkbxCaptureToMedia, chkbxCaptureToCluster;
	public GSlider sdrAltitudeScaling;
//	public GButton btnCloseModelWindow;
	int modelWindowHeight;
	
	/* Selection Window */
	private GLabel lblSelection;
	public GCheckbox chkbxSelectionMode, chkbxMultiSelection, chkbxSegmentSelection, chkbxShowMetadata;
	private GButton btnSelectFront, btnDeselectFront, btnDeselectAll, btnStitchPanorama;
//	public GButton btnCloseSelectionWindow;
	int selectionWindowHeight;

	/* Statistics Window */
//	private GButton btnCloseStatisticsWindow;
	int statisticsWindowHeight;

	/* Memory Window */
//	private GButton btnCloseMemoryWindow;
	int memoryWindowHeight;

	/* Help Window */
	int helpWindowHeight;

//	private GButton btnCloseHelpWindow;
	
	String windowTitle = " ";

	WMV_Window( WMV_Display parent )
	{
		p = parent;

		wmvWindowHeight = shortWindowHeight;
		navigationWindowHeight = shortWindowHeight + 200;
		timeWindowHeight = longWindowHeight;
		graphicsWindowHeight = longWindowHeight - 100;
		modelWindowHeight = shortWindowHeight;
		memoryWindowHeight = shortWindowHeight;
		statisticsWindowHeight = longWindowHeight;
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
//		selectionWindow.setVisible(true);
		showSelectionWindow();
	}

	void openHelpWindow()
	{
		if(!setupHelpWindow)
			setupHelpWindow();
//		helpWindow.setVisible(true);
//		showHelpWindow = true;
		showHelpWindow();
	}

	void setupWMVWindow()
	{
		wmvWindow = GWindow.getWindow(p.p.p, windowTitle, 10, 45, windowWidth, wmvWindowHeight, PApplet.JAVA2D);
		wmvWindow.addData(new WMV_WinData());
		wmvWindow.addDrawHandler(this, "wmvWindowDraw");
		wmvWindow.addMouseHandler(this, "wmvWindowMouse");
		wmvWindow.addKeyHandler(p.p.p, "wmvWindowKey");
		wmvWindow.setActionOnClose(GWindow.KEEP_OPEN);
		hideWMVWindow();
		
		int x = 0, y = 12;

		x = 0;

		lblWMViewer = new GLabel(wmvWindow, x, y, wmvWindow.width, 20, "Main Menu");
		lblWMViewer.setLocalColorScheme(10);
		lblWMViewer.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblWMViewer.setTextAlign(GAlign.CENTER, null);
		lblWMViewer.setTextBold();

		x = 30;
		y += 30;
		
		optSceneView = new GOption(wmvWindow, x, y, 90, 20, "Scene");
		optSceneView.setLocalColorScheme(10);
		optSceneView.tag = "SceneView";
		optMapView = new GOption(wmvWindow, x+=90, y, 90, 20, "Map");
		optMapView.setLocalColorScheme(10);
		optMapView.tag = "MapView";
		optClusterView = new GOption(wmvWindow, x+=90, y, 90, 20, "Cluster");
		optClusterView.setLocalColorScheme(10);
		optClusterView.tag = "ClusterView";

		switch(p.displayView)
		{
			case 0:
				optSceneView.setSelected(true);
				optMapView.setSelected(false);
				optClusterView.setSelected(false);
				break;
			case 1:
				optSceneView.setSelected(false);
				optMapView.setSelected(true);
				optClusterView.setSelected(false);
				break;
			case 2:
				optSceneView.setSelected(false);
				optMapView.setSelected(false);
				optClusterView.setSelected(true);
				break;
		}
		
		tgDisplayView = new GToggleGroup();
		tgDisplayView.addControls(optSceneView, optMapView, optClusterView);
	
		x = 65;
		y += 30;
		
		btnLoadMediaLibrary = new GButton(wmvWindow, x, y, 180, 20, "Load Media Library  ⇧R");
		btnLoadMediaLibrary.tag = "Restart";
		btnLoadMediaLibrary.setLocalColorScheme(7);

		x = 90;
		y += 30;

		btnNavigationWindow = new GButton(wmvWindow, x, y, 125, 20, "Navigation  ⌘1");
		btnNavigationWindow.tag = "OpenNavigationWindow";
		btnNavigationWindow.setLocalColorScheme(5);
		
		x = 90;
		y += 25;
		btnTimeWindow = new GButton(wmvWindow, x, y, 125, 20, "Time  ⌘2");
		btnTimeWindow.tag = "OpenTimeWindow";
		btnTimeWindow.setLocalColorScheme(5);

		x = 90;
		y += 25;

		btnGraphicsWindow = new GButton(wmvWindow, x, y, 125, 20, "Graphics  ⌘3");
		btnGraphicsWindow.tag = "OpenGraphicsWindow";
		btnGraphicsWindow.setLocalColorScheme(5);
		
		x = 90;
		y += 25;
		
		btnModelWindow = new GButton(wmvWindow, x, y, 125, 20, "Model  ⌘4");
		btnModelWindow.tag = "OpenModelWindow";
		btnModelWindow.setLocalColorScheme(5);

		x = 90;
		y += 25;

		btnMemoryWindow = new GButton(wmvWindow, x, y, 125, 20, "Memory  ⌘5");
		btnMemoryWindow.tag = "OpenMemoryWindow";
		btnMemoryWindow.setLocalColorScheme(5);
		
		x = 90;
		y += 25;

		btnSelectionWindow = new GButton(wmvWindow, x, y, 125, 20, "Selection  ⌘6");
		btnSelectionWindow.tag = "OpenSelectionWindow";
		btnSelectionWindow.setLocalColorScheme(5);
		
		x = 90;
		y += 25;
		
		btnStatisticsWindow = new GButton(wmvWindow, x, y, 125, 20, "Statistics  ⌘7");
		btnStatisticsWindow.tag = "OpenStatisticsWindow";
		btnStatisticsWindow.setLocalColorScheme(5);
		
		x = 90;
		y += 25;
		
		btnHelpWindow = new GButton(wmvWindow, x, y, 125, 20, "Help  ⌘8");
		btnHelpWindow.tag = "OpenHelpWindow";
		btnHelpWindow.setLocalColorScheme(5);

		x = 0;
		y = wmvWindowHeight - 25;
		lblSpaceBar = new GLabel(wmvWindow, x, y, wmvWindow.width, 20);						/* Display Mode Label */
		lblSpaceBar.setText("Press SPACEBAR to show / hide");
		lblSpaceBar.setFont(new Font("Monospaced", Font.PLAIN, 11));
		lblSpaceBar.setLocalColorScheme(10);
		lblSpaceBar.setTextAlign(GAlign.CENTER, null);
	}
	
	void setupNavigationWindow()
	{
		navigationWindow = GWindow.getWindow(p.p.p, windowTitle, 10, 45, windowWidth, navigationWindowHeight, PApplet.JAVA2D);
		navigationWindow.setVisible(false);
		navigationWindow.addData(new WMV_WinData());
		navigationWindow.addDrawHandler(this, "navigationWindowDraw");
		navigationWindow.addMouseHandler(this, "navigationWindowMouse");
		navigationWindow.addKeyHandler(p.p.p, "navigationWindowKey");
		
		int x = 0, y = 12;

		lblNavigationWindow = new GLabel(navigationWindow, x, y, navigationWindow.width, 20, "Navigation");
		lblNavigationWindow.setLocalColorScheme(10);
		lblNavigationWindow.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblNavigationWindow.setTextAlign(GAlign.CENTER, null);
		lblNavigationWindow.setTextBold();

		x = 0;
		y += 30;

		lblClusterNavigation = new GLabel(navigationWindow, x, y, navigationWindow.width, 20, "Cluster Navigation");
		lblClusterNavigation.setLocalColorScheme(10);
		lblClusterNavigation.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblClusterNavigation.setTextAlign(GAlign.CENTER, null);
		lblClusterNavigation.setTextBold();

		x = 120;
		y += 30;
		
		chkbxMovementTeleport = new GCheckbox(navigationWindow, x, y, 70, 20, "Teleport");
		chkbxMovementTeleport.tag = "MovementTeleport";
		chkbxMovementTeleport.setLocalColorScheme(10);

		x = 40;
		y += 25;

		btnMoveToNearestCluster = new GButton(navigationWindow, x, y, 100, 20, "Nearest  (m)");
		btnMoveToNearestCluster.tag = "NearestCluster";
		btnMoveToNearestCluster.setLocalColorScheme(5);
		btnMoveToNearestCluster = new GButton(navigationWindow, x+110, y, 100, 20, "Last  (l)");
		btnMoveToNearestCluster.tag = "LastCluster";
		btnMoveToNearestCluster.setLocalColorScheme(5);

		x = 30;
		y += 25;

		btnPreviousTimeSegment = new GButton(navigationWindow, x, y, 120, 20, "Previous Time  (b)");
		btnPreviousTimeSegment.tag = "PreviousTime";
		btnPreviousTimeSegment.setLocalColorScheme(5);
		btnNextTimeSegment = new GButton(navigationWindow, x+=125, y, 100, 20, "Next Time  (n)");
		btnNextTimeSegment.tag = "NextTime";
		btnNextTimeSegment.setLocalColorScheme(5);
		
		x = 25;
		y += 25;
		
		btnJumpToRandomCluster = new GButton(navigationWindow, x+75, y, 110, 20, "Random  (j)");
		btnJumpToRandomCluster.tag = "RandomCluster";
		btnJumpToRandomCluster.setLocalColorScheme(5);
		
		x = 0;
		y += 30;
		lblPathNavigation = new GLabel(navigationWindow, x, y, navigationWindow.width, 20, "Path Navigation");
		lblPathNavigation.setLocalColorScheme(10);
		lblPathNavigation.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblPathNavigation.setTextAlign(GAlign.CENTER, null);
		lblPathNavigation.setTextBold();

		x = 120;
		y += 25;

		chkbxFollowTeleport = new GCheckbox(navigationWindow, x, y, 80, 20, "Teleport");
		chkbxFollowTeleport.tag = "FollowTeleport";
		chkbxFollowTeleport.setLocalColorScheme(10);

		x = 15;
		y += 25;
		
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

		btnFollowStart = new GButton(navigationWindow, x, y, 60, 20, "Start");
		btnFollowStart.tag = "FollowStart";
		btnFollowStart.setLocalColorScheme(5);
		
		btnFollowStop = new GButton(navigationWindow, x+=60, y, 60, 20, "Stop");
		btnFollowStop.tag = "FollowStop";
		btnFollowStop.setLocalColorScheme(0);
		
		x = 80;
		y += 30;

		btnImportGPSTrack = new GButton(navigationWindow, x, y, 140, 20, "Import GPS Track  (g)");
		btnImportGPSTrack.tag = "ImportGPSTrack";
		btnImportGPSTrack.setLocalColorScheme(5);		

		x = 0;
		y += 30;
		lblMemoryCommands = new GLabel(navigationWindow, x, y, navigationWindow.width, 20, "Memory");
		lblMemoryCommands.setLocalColorScheme(10);
		lblMemoryCommands.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblMemoryCommands.setTextAlign(GAlign.CENTER, null);
		lblMemoryCommands.setTextBold();

		x = 45;
		y += 30;
		
		btnSaveLocation = new GButton(navigationWindow, x, y, 110, 20, "Save Location  (`)");
		btnSaveLocation.tag = "SaveLocation";
		btnSaveLocation.setLocalColorScheme(5);
		btnClearMemory = new GButton(navigationWindow, x+110, y, 100, 20, "Clear Memory");
		btnClearMemory.tag = "ClearMemory";
		btnClearMemory.setLocalColorScheme(0);

		if(p.p.getFields() != null)
		{
			x = 40;
			y += 30;
			btnGoToPreviousField = new GButton(navigationWindow, x, y, 120, 20, "Previous Field  ⇧[");
			btnGoToPreviousField.tag = "PreviousField";
			btnGoToPreviousField.setLocalColorScheme(5);

			btnGoToNextField = new GButton(navigationWindow, x+=125, y, 100, 20, "Next Field  ⇧]");
			btnGoToNextField.tag = "NextField";
			btnGoToNextField.setLocalColorScheme(5);
		}

//		x = 40;
//		y = navigationWindowHeight - 40;
//		
//		btnCloseNavigationWindow = new GButton(navigationWindow, x, y, 180, 20, "Close Window");
//		btnCloseNavigationWindow.tag = "CloseNavigationWindow";
//		btnCloseNavigationWindow.setLocalColorScheme(0);
		
		setupNavigationWindow = true;
	}

	void setupTimeWindow()
	{
		timeWindow = GWindow.getWindow(p.p.p, windowTitle, 10, 45, windowWidth, timeWindowHeight, PApplet.JAVA2D);
		timeWindow.setVisible(true);
		timeWindow.addData(new WMV_WinData());
		timeWindow.addDrawHandler(this, "timeWindowDraw");
		timeWindow.addMouseHandler(this, "timeWindowMouse");
		timeWindow.addKeyHandler(p.p.p, "timeWindowKey");
		timeWindow.setActionOnClose(GWindow.KEEP_OPEN);

		int x = 0, y = 12;

		lblTimeWindow = new GLabel(timeWindow, x, y, timeWindow.width, 20, "Time");
		lblTimeWindow.setLocalColorScheme(10);
		lblTimeWindow.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblTimeWindow.setTextAlign(GAlign.CENTER, null);
		lblTimeWindow.setTextBold();

		y += 30;

		lblTimeSettings = new GLabel(timeWindow, x, y, timeWindow.width, 20, "Time Settings");
		lblTimeSettings.setLocalColorScheme(10);
		lblTimeSettings.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblTimeSettings.setTextAlign(GAlign.CENTER, null);
		lblTimeSettings.setTextBold();

		x = 90;
		y += 30;
		
		chkbxTimeFading = new GCheckbox(timeWindow, x, y, 120, 20, "Time Fading  ⇧T");
		chkbxTimeFading.tag = "TimeFading";
		chkbxTimeFading.setLocalColorScheme(10);
		chkbxTimeFading.setSelected(p.p.timeFading);

		x = 200;
		y += 30;

		sdrMediaLength = new GSlider(timeWindow, x, y, 80, 80, 20);
		sdrMediaLength.setLocalColorScheme(7);
		sdrMediaLength.setLimits(0.f, 250.f, 10.f);
		sdrMediaLength.setValue(p.p.settings.defaultMediaLength);
		sdrMediaLength.setRotation(PApplet.PI/2.f);
		sdrMediaLength.setTextOrientation(G4P.ORIENT_LEFT);
		sdrMediaLength.setEasing(0);
		sdrMediaLength.setShowValue(true);
		sdrMediaLength.tag = "MediaLength";

		x = 50;
		y += 30;
		
		lblMediaLength = new GLabel(timeWindow, x, y, 80, 20, "Media Length");
		lblMediaLength.setLocalColorScheme(10);

		x = 0;
		y += 90;

		lblTimeMode = new GLabel(timeWindow, x, y, timeWindow.width, 20, "Time Mode");
		lblTimeMode.setLocalColorScheme(10);
		lblTimeMode.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblTimeMode.setTextAlign(GAlign.CENTER, null);
		lblTimeMode.setTextBold();

		x = 30;
		y += 30;
		
		optClusterTimeMode = new GOption(timeWindow, x, y, 90, 20, "Cluster");
		optClusterTimeMode.setLocalColorScheme(10);
		optClusterTimeMode.tag = "ClusterTimeMode ";
		optFieldTimeMode = new GOption(timeWindow, x+=90, y, 90, 20, "Field");
		optFieldTimeMode.setLocalColorScheme(10);
		optFieldTimeMode.tag = "FieldTimeMode";
		optMediaTimeMode = new GOption(timeWindow, x+=90, y, 90, 20, "Media");
		optMediaTimeMode.setLocalColorScheme(10);
		optMediaTimeMode.tag = "MediaTimeMode";

		switch(p.p.getTimeMode())
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
		
		setupTimeWindow = true;
	}
	
	void setupGraphicsWindow()
	{
		graphicsWindow = GWindow.getWindow(p.p.p, windowTitle, 10, 45, windowWidth, graphicsWindowHeight, PApplet.JAVA2D);
		graphicsWindow.setVisible(false);
		graphicsWindow.addData(new WMV_WinData());
		graphicsWindow.addDrawHandler(this, "graphicsWindowDraw");
		graphicsWindow.addMouseHandler(this, "graphicsWindowMouse");
		graphicsWindow.addKeyHandler(p.p.p, "graphicsWindowKey");
		graphicsWindow.setActionOnClose(GWindow.KEEP_OPEN);
	
		int x = 0, y = 12;

		lblGraphics = new GLabel(graphicsWindow, x, y, graphicsWindow.width, 20, "Graphics");
		lblGraphics.setLocalColorScheme(10);
		lblGraphics.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblGraphics.setTextAlign(GAlign.CENTER, null);
		lblGraphics.setTextBold();

		x = 80;
		y += 30;
		
		btnZoomOut = new GButton(graphicsWindow, x, y, 40, 20, "Out");
		btnZoomOut.tag = "ZoomOut";
		btnZoomOut.setLocalColorScheme(5);
		lblZoom = new GLabel(graphicsWindow, 0, y, graphicsWindow.width, 20, "Zoom");
		lblZoom.setLocalColorScheme(10);
		lblZoom.setTextAlign(GAlign.CENTER, null);
		btnZoomIn = new GButton(graphicsWindow, x += 105, y, 40, 20, "In");
		btnZoomIn.tag = "ZoomIn";
		btnZoomIn.setLocalColorScheme(5);

		x = 140;
		y += 30;

		sdrAlpha = new GSlider(graphicsWindow, x, y, 80, 80, 20);
		sdrAlpha.setLocalColorScheme(7);
		sdrAlpha.setLimits(p.p.alpha, 255.f, 0.f);
		sdrAlpha.setRotation(PApplet.PI/2.f);
		sdrAlpha.setTextOrientation(G4P.ORIENT_LEFT);
		sdrAlpha.setEasing(0);
		sdrAlpha.setShowValue(true);
		sdrAlpha.tag = "Alpha";
		
		x += 140;
		sdrBrightness = new GSlider(graphicsWindow, x, y, 80, 80, 20);
		sdrBrightness.setLocalColorScheme(7);
		sdrBrightness.setLimits(p.p.viewer.settings.userBrightness, 1.f, 0.f);
		sdrBrightness.setValue(p.p.viewer.settings.userBrightness);					// Not sure why this is needed, maybe a G4P bug?
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

		x = 95;
		y += 25;
		
		chkbxOrientationMode = new GCheckbox(graphicsWindow, x, y, 115, 20, "Orientation Mode");
		chkbxOrientationMode.tag = "OrientationMode";
		chkbxOrientationMode.setLocalColorScheme(10);
		chkbxOrientationMode.setSelected(false);
		
		x = 60;
		y += 25;

		chkbxAlphaMode = new GCheckbox(graphicsWindow, x, y, 85, 20, "Alpha Mode");
		chkbxAlphaMode.tag = "AlphaMode";
		chkbxAlphaMode.setLocalColorScheme(10);
		chkbxAlphaMode.setSelected(true);

		chkbxAngleFading = new GCheckbox(graphicsWindow, x += 90, y, 100, 20, "Angle Fading");
		chkbxAngleFading.tag = "AngleFading";
		chkbxAngleFading.setLocalColorScheme(10);
		chkbxAngleFading.setSelected(true);

		x = 60;
		y += 25;

		chkbxFadeEdges = new GCheckbox(graphicsWindow, x, y, 85, 20, "Fade Edges");
		chkbxFadeEdges.tag = "FadeEdges";
		chkbxFadeEdges.setLocalColorScheme(10);
		chkbxFadeEdges.setSelected(true);

		chkbxAngleThinning = new GCheckbox(graphicsWindow, x += 90, y, 100, 20, "Angle Thinning");
		chkbxAngleThinning.tag = "AngleThinning";
		chkbxAngleThinning.setLocalColorScheme(10);
		chkbxAngleThinning.setSelected(false);

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
		
//		x = 40;
//		y = graphicsWindowHeight - 40;
//		
//		btnCloseGraphicsWindow = new GButton(graphicsWindow, x, y, 180, 20, "Close Window");
//		btnCloseGraphicsWindow.tag = "CloseGraphicsWindow";
//		btnCloseGraphicsWindow.setLocalColorScheme(0);
		
		setupGraphicsWindow = true;
	}
	

	void setupModelWindow()
	{
		modelWindow = GWindow.getWindow(p.p.p, windowTitle, 10, 45, windowWidth, modelWindowHeight, PApplet.JAVA2D);
		modelWindow.setVisible(true);
		modelWindow.addData(new WMV_WinData());
		modelWindow.addDrawHandler(this, "modelWindowDraw");
		modelWindow.addMouseHandler(this, "modelWindowMouse");
		modelWindow.addKeyHandler(p.p.p, "modelWindowKey");
		modelWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		int x = 0, y = 12;

		lblModelWindow = new GLabel(modelWindow, x, y, modelWindow.width, 20, "Model");
		lblModelWindow.setLocalColorScheme(10);
		lblModelWindow.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblModelWindow.setTextAlign(GAlign.CENTER, null);
		lblModelWindow.setTextBold();

		y += 30;

		lblModelSettings = new GLabel(modelWindow, x, y, modelWindow.width, 20, "Model Settings");
		lblModelSettings.setLocalColorScheme(10);
		lblModelSettings.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblModelSettings.setTextAlign(GAlign.CENTER, null);
		lblModelSettings.setTextBold();

		x = 210;
		y += 30;

		sdrAltitudeScaling = new GSlider(modelWindow, x, y, 80, 80, 20);
		sdrAltitudeScaling.setLocalColorScheme(7);
		sdrAltitudeScaling.setLimits(0.f, 1.f, 0.f);
		sdrAltitudeScaling.setValue(p.p.altitudeScalingFactor);											// -- Shouldn't be needed! Calls handler
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

		chkbxShowModel = new GCheckbox(modelWindow, x, y, 120, 20, "Show Model  (⇧4)");
		chkbxShowModel.tag = "ShowModel";
		chkbxShowModel.setLocalColorScheme(10);
		
		x = 115;
		y += 30;
		
		chkbxMediaToCluster = new GCheckbox(modelWindow, x, y, 150, 20, "Media to Cluster  (⇧5)");
		chkbxMediaToCluster.tag = "MediaToCluster";
		chkbxMediaToCluster.setLocalColorScheme(10);
		chkbxMediaToCluster.setSelected(false);
		
		x = 115;
		y += 25;

		chkbxCaptureToMedia = new GCheckbox(modelWindow, x, y, 150, 20, "Capture to Media  (⇧6)");
		chkbxCaptureToMedia.tag = "CaptureToMedia";
		chkbxCaptureToMedia.setLocalColorScheme(10);
		chkbxCaptureToMedia.setSelected(false);

		x = 115;
		y += 25;

		chkbxCaptureToCluster = new GCheckbox(modelWindow, x, y, 170, 20, "Capture to Cluster  (⇧7)");
		chkbxCaptureToCluster.tag = "CaptureToCluster";
		chkbxCaptureToCluster.setLocalColorScheme(10);
		chkbxCaptureToCluster.setSelected(false);

//		x = 40;
//		y = modelWindowHeight - 40;
//		
//		btnCloseModelWindow = new GButton(modelWindow, x, y, 180, 20, "Close Window");
//		btnCloseModelWindow.tag = "CloseModelWindow";
//		btnCloseModelWindow.setLocalColorScheme(0);

		setupModelWindow = true;
	}

	void setupMemoryWindow()
	{
		memoryWindow = GWindow.getWindow(p.p.p, "Memory", 10, 45, windowWidth, memoryWindowHeight, PApplet.JAVA2D);
		memoryWindow.setVisible(false);
		memoryWindow.addData(new WMV_WinData());
		memoryWindow.addDrawHandler(this, "memoryWindowDraw");
		memoryWindow.addMouseHandler(this, "memoryWindowMouse");
		memoryWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		int x = 0, y = 10;

		/* Selection Window */
		lblMemory = new GLabel(memoryWindow, x, y, memoryWindow.width, 20, "Memory");
		lblMemory.setLocalColorScheme(10);
		lblMemory.setTextAlign(GAlign.CENTER, null);
		lblMemory.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblMemory.setTextBold();

//		x = 40;
//		y = memoryWindowHeight - 40;
//		
//		btnCloseMemoryWindow = new GButton(memoryWindow, x, y, 180, 20, "Close Window");
//		btnCloseMemoryWindow.tag = "CloseMemoryWindow";
//		btnCloseMemoryWindow.setLocalColorScheme(0);
		
		memoryWindow.addKeyHandler(p.p.p, "memoryWindowKey");
		
		setupMemoryWindow = true;
	}

	void setupSelectionWindow()
	{
		selectionWindow = GWindow.getWindow(p.p.p, "Selection Mode", 10, 45, windowWidth, selectionWindowHeight, PApplet.JAVA2D);
		selectionWindow.setVisible(false);
		selectionWindow.addData(new WMV_WinData());
		selectionWindow.addDrawHandler(this, "selectionWindowDraw");
		selectionWindow.addMouseHandler(this, "selectionWindowMouse");
		selectionWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		int x = 0, y = 10;

		/* Selection Window */
		lblSelection = new GLabel(selectionWindow, x, y, selectionWindow.width, 20, "Selection");
		lblSelection.setLocalColorScheme(10);
		lblSelection.setTextAlign(GAlign.CENTER, null);
		lblSelection.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblSelection.setTextBold();

		x = 100;
		y += 30;

		chkbxSelectionMode = new GCheckbox(selectionWindow, x, y, 110, 20, "Enable Selection");
		chkbxSelectionMode.tag = "SelectionMode";
		chkbxSelectionMode.setLocalColorScheme(10);
		x = 105;
		y += 25;
		
		chkbxMultiSelection = new GCheckbox(selectionWindow, x, y, 180, 20, "Select Multiple");
		chkbxMultiSelection.tag = "MultiSelection";
		chkbxMultiSelection.setLocalColorScheme(10);

		x = 100;
		y += 25;
		
		chkbxSegmentSelection = new GCheckbox(selectionWindow, x, y, 180, 20, "Select Segments");
		chkbxSegmentSelection.tag = "SegmentSelection";
		chkbxSegmentSelection.setLocalColorScheme(10);
		
		x = 100;
		y += 25;
		
		chkbxShowMetadata = new GCheckbox(selectionWindow, x, y, 110, 20, "View Metadata");
		chkbxShowMetadata.tag = "ViewMetadata";
		chkbxShowMetadata.setLocalColorScheme(10);
		
		
		x = 100;
		y += 30;
		
		btnSelectFront = new GButton(selectionWindow, x, y, 110, 20, "Select (x)");
		btnSelectFront.tag = "SelectFront";
		btnSelectFront.setLocalColorScheme(5);

		x = 100;
		y += 25;
		
		btnDeselectFront = new GButton(selectionWindow, x, y, 110, 20, "Deselect (x)");
		btnDeselectFront.tag = "DeselectFront";
		btnDeselectFront.setLocalColorScheme(5);

		x = 95;
		y += 25;

		btnDeselectAll = new GButton(selectionWindow, x, y, 120, 20, "Deselect All...");
		btnDeselectAll.tag = "DeselectAll";
		btnDeselectAll.setLocalColorScheme(5);

		x = 85;
		y += 25;

		btnStitchPanorama = new GButton(selectionWindow, x, y, 140, 20, "Stitch Selection  (|)");
		btnStitchPanorama.tag = "StitchPanorama";
		btnStitchPanorama.setLocalColorScheme(7);
		
//		x = 40;
//		y = selectionWindowHeight - 40;
//		
//		btnCloseSelectionWindow = new GButton(selectionWindow, x, y, 180, 20, "Close Window");
//		btnCloseSelectionWindow.tag = "CloseSelectionWindow";
//		btnCloseSelectionWindow.setLocalColorScheme(0);
		
		selectionWindow.addKeyHandler(p.p.p, "selectionWindowKey");
		setupSelectionWindow = true;
	}


	void setupStatisticsWindow()
	{
		statisticsWindow = GWindow.getWindow(p.p.p, "Statistics", 10, 45, windowWidth * 2, statisticsWindowHeight, PApplet.JAVA2D);
		statisticsWindow.setVisible(false);
		statisticsWindow.addData(new WMV_WinData());
		statisticsWindow.addDrawHandler(this, "statisticsWindowDraw");
		statisticsWindow.addMouseHandler(this, "statisticsWindowMouse");
		statisticsWindow.setActionOnClose(GWindow.KEEP_OPEN);
		
		int x = 0, y = 10;

		lblStatistics = new GLabel(statisticsWindow, x, y, statisticsWindow.width, 20, "Statistics");
		lblStatistics.setLocalColorScheme(10);
		lblStatistics.setTextAlign(GAlign.CENTER, null);
		lblStatistics.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblStatistics.setTextBold();

//		x = 40;
//		y = statisticsWindowHeight - 40;
//		
//		btnCloseStatisticsWindow = new GButton(statisticsWindow, x, y, 180, 20, "Close Window");
//		btnCloseStatisticsWindow.tag = "CloseStatisticsWindow";
//		btnCloseStatisticsWindow.setLocalColorScheme(0);
		
		statisticsWindow.addKeyHandler(p.p.p, "statisticsWindowKey");
		
		setupStatisticsWindow = true;
	}
	
	void setupHelpWindow()
	{
		helpWindow = GWindow.getWindow(p.p.p, "Help", 10, 45, windowWidth, helpWindowHeight, PApplet.JAVA2D);
		helpWindow.setVisible(false);
		helpWindow.addData(new WMV_WinData());
		helpWindow.addDrawHandler(this, "helpWindowDraw");
		helpWindow.addMouseHandler(this, "helpWindowMouse");
		
		int x = 0, y = 10;

		/* Selection Window */
		lblHelp = new GLabel(helpWindow, x, y, helpWindow.width, 20, "Help");
		lblHelp.setLocalColorScheme(10);
		lblHelp.setTextAlign(GAlign.CENTER, null);
		lblHelp.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblHelp.setTextBold();

//		x = 40;
//		y = helpWindowHeight - 40;
//		
//		btnCloseHelpWindow = new GButton(helpWindow, x, y, 180, 20, "Close Window");
//		btnCloseHelpWindow.tag = "CloseHelpWindow";
//		btnCloseHelpWindow.setLocalColorScheme(0);
		
		helpWindow.addKeyHandler(p.p.p, "helpWindowKey");
		
		setupHelpWindow = true;
	}
	
	/**
	 * Handles drawing to the windows PApplet area
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 */
	public void wmvWindowDraw(PApplet applet, GWinData data) {
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
	 * Handles mouse events for ALL GWindow objects
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void wmvWindowMouse(PApplet applet, GWinData data, MouseEvent event) {
		WMV_WinData data2 = (WMV_WinData)data;
		switch(event.getAction()) {

		case MouseEvent.PRESS:
			data2.sx = data2.ex = applet.mouseX;
			data2.sy = data2.ey = applet.mouseY;
			data2.done = false;
//			PApplet.println("Mouse pressed");
			break;
		case MouseEvent.RELEASE:
			data2.ex = applet.mouseX;
			data2.ey = applet.mouseY;
			data2.done = true;
//			PApplet.println("Mouse released:"+data.toString());
			break;
		case MouseEvent.DRAG:
			data2.ex = applet.mouseX;
			data2.ey = applet.mouseY;
//			PApplet.println("Mouse dragged");
			break;
		}
	}
	

	/**
	 * Handles drawing to the Time Window 
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 */
	public void timeWindowDraw(PApplet applet, GWinData data) {
		if(p.p.p.running)
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
			applet.text(" Time Mode: "+ ((p.p.p.world.getTimeMode() == 0) ? "Cluster" : "Field"), x, y += lineWidthVeryWide);

			//		if(p.p.p.world.timeMode == 0)
			applet.text(" Current Field Time: "+ p.p.currentTime, x, y += lineWidth);
			applet.text(" Current Field Time Segment: "+ p.p.viewer.currentFieldTimeSegment, x, y += lineWidth);
			applet.text(" Current Cluster Time Segment: "+ p.p.getCurrentCluster().timeline.size(), x, y += lineWidth);
			//		if(p.p.p.world.timeMode == 1)
			applet.text(" Current Cluster Time: "+ p.p.getCurrentCluster().currentTime, x, y += lineWidth);
			applet.text(" Current Field Timeline Size: "+ p.p.getCurrentField().timeline.size(), x, y += lineWidth);
			applet.text(" Current Field Dateline Size: "+ p.p.getCurrentField().dateline.size(), x, y += lineWidth);

			WMV_Field f = p.p.getCurrentField();
			if(f.timeline.size() > 0 && p.p.viewer.currentFieldTimeSegment >= 0 && p.p.viewer.currentFieldTimeSegment < f.timeline.size())
				applet.text(" Upper: "+f.timeline.get(p.p.viewer.currentFieldTimeSegment).getUpper().getTime()+
						" Center:"+f.timeline.get(p.p.viewer.currentFieldTimeSegment).getCenter().getTime()+
						" Lower: "+f.timeline.get(p.p.viewer.currentFieldTimeSegment).getLower().getTime(), x, y += lineWidth);
			applet.text(" Current Cluster Timeline Size: "+ p.p.getCurrentCluster().timeline.size(), x, y += lineWidth);
			applet.text(" Current Cluster Dateline Size: "+ p.p.getCurrentCluster().dateline.size(), x, y += lineWidth);
		}
	}

	/**
	 * Handles mouse events for Time Window  
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void timeWindowMouse(PApplet applet, GWinData data, MouseEvent event) {
		WMV_WinData data2 = (WMV_WinData)data;
		switch(event.getAction()) {

		case MouseEvent.PRESS:
			data2.sx = data2.ex = applet.mouseX;
			data2.sy = data2.ey = applet.mouseY;
			data2.done = false;
//			PApplet.println("Mouse pressed");
			break;
		case MouseEvent.RELEASE:
			data2.ex = applet.mouseX;
			data2.ey = applet.mouseY;
			data2.done = true;
//			PApplet.println("Mouse released:"+data.toString());
			break;
		case MouseEvent.DRAG:
			data2.ex = applet.mouseX;
			data2.ey = applet.mouseY;
//			PApplet.println("Mouse dragged");
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
		WMV_WinData data2 = (WMV_WinData)data;
		switch(event.getAction()) {

		case MouseEvent.PRESS:
			data2.sx = data2.ex = applet.mouseX;
			data2.sy = data2.ey = applet.mouseY;
			data2.done = false;
//			PApplet.println("Mouse pressed");
			break;
		case MouseEvent.RELEASE:
			data2.ex = applet.mouseX;
			data2.ey = applet.mouseY;
			data2.done = true;
			PApplet.println("Mouse released:"+data.toString());
			break;
		case MouseEvent.DRAG:
			data2.ex = applet.mouseX;
			data2.ey = applet.mouseY;
//			PApplet.println("Mouse dragged");
			break;
		}
	}

	/**
	 * Handles drawing to the Graphics Window
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 */
	public void graphicsWindowDraw(PApplet applet, GWinData data) {
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
	 * Handles mouse events for Graphics Window
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void graphicsWindowMouse(PApplet applet, GWinData data, MouseEvent event) {
		WMV_WinData data2 = (WMV_WinData)data;
		switch(event.getAction()) {

		case MouseEvent.PRESS:
			data2.sx = data2.ex = applet.mouseX;
			data2.sy = data2.ey = applet.mouseY;
			data2.done = false;
//			PApplet.println("Mouse pressed");
			break;
		case MouseEvent.RELEASE:
			data2.ex = applet.mouseX;
			data2.ey = applet.mouseY;
			data2.done = true;
			PApplet.println("Mouse released:"+data.toString());
			break;
		case MouseEvent.DRAG:
			data2.ex = applet.mouseX;
			data2.ey = applet.mouseY;
//			PApplet.println("Mouse dragged");
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
//		applet.text(" Time Mode: "+ ((p.p.p.world.timeMode == 0) ? "Cluster" : "Field"), x, y += lineWidthVeryWide);
//		
////		if(p.p.p.world.timeMode == 0)
//			applet.text(" Current Field Time: "+ p.p.currentTime, x, y += lineWidth);
//		applet.text(" Current Field Time Segment: "+ p.p.viewer.currentFieldTimeSegment, x, y += lineWidth);
//		applet.text(" Current Cluster Time Segment: "+ p.p.getCurrentCluster().timeline.size(), x, y += lineWidth);
////		if(p.p.p.world.timeMode == 1)
//			applet.text(" Current Cluster Time: "+ p.p.getCurrentCluster().currentTime, x, y += lineWidth);
//		applet.text(" Current Field Timeline Size: "+ p.p.getCurrentField().timeline.size(), x, y += lineWidth);
//		applet.text(" Current Field Dateline Size: "+ p.p.getCurrentField().dateline.size(), x, y += lineWidth);
//
//		WMV_Field f = p.p.getCurrentField();
//		if(f.timeline.size() > 0 && p.p.viewer.currentFieldTimeSegment >= 0 && p.p.viewer.currentFieldTimeSegment < f.timeline.size())
//			applet.text(" Upper: "+f.timeline.get(p.p.viewer.currentFieldTimeSegment).getUpper().getTime()+
//					" Center:"+f.timeline.get(p.p.viewer.currentFieldTimeSegment).getCenter().getTime()+
//					" Lower: "+f.timeline.get(p.p.viewer.currentFieldTimeSegment).getLower().getTime(), x, y += lineWidth);
//		applet.text(" Current Cluster Timeline Size: "+ p.p.getCurrentCluster().timeline.size(), x, y += lineWidth);
//		applet.text(" Current Cluster Dateline Size: "+ p.p.getCurrentCluster().dateline.size(), x, y += lineWidth);
	}

	/**
	 * Handles mouse events for Time Window  
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void modelWindowMouse(PApplet applet, GWinData data, MouseEvent event) {
		WMV_WinData data2 = (WMV_WinData)data;
		switch(event.getAction()) {

		case MouseEvent.PRESS:
			data2.sx = data2.ex = applet.mouseX;
			data2.sy = data2.ey = applet.mouseY;
			data2.done = false;
//			PApplet.println("Mouse pressed");
			break;
		case MouseEvent.RELEASE:
			data2.ex = applet.mouseX;
			data2.ey = applet.mouseY;
			data2.done = true;
			PApplet.println("Mouse released:"+data.toString());
			break;
		case MouseEvent.DRAG:
			data2.ex = applet.mouseX;
			data2.ey = applet.mouseY;
//			PApplet.println("Mouse dragged");
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
		if(p.p.p.running)
		{
			applet.background(10, 5, 50);
			applet.stroke(255);
			applet.strokeWeight(1);
			applet.fill(0, 0, 255);

			float lineWidthVeryWide = 20f;
			//		float lineWidthWide = 16f;
			float lineWidth = 14f;

			float x = 10;
			float y = 50;			// Starting vertical position

			float mediumTextSize = 16.f;
			float smallTextSize = 14.f;

			WMV_Field f = p.p.getCurrentField();

			if(p.p.viewer.getCurrentClusterID() >= 0)
			{
				WMV_Cluster c = p.p.getCurrentCluster();
				float[] camTar = p.p.viewer.camera.target();

				applet.fill(185, 215, 255, 255);					// Set text color

//				applet.textSize(mediumTextSize);
//				applet.text(" Program Modes ", x, y += lineWidthVeryWide);
//				applet.textSize(smallTextSize);
//				applet.text(" Orientation Mode: "+p.p.viewer.settings.orientationMode, x, y += lineWidthVeryWide);
//				applet.text(" Alpha Mode:"+p.p.alphaMode, x, y += lineWidth);
//				applet.text(" Time Fading: "+ p.p.timeFading, x, y += lineWidth);
//				applet.text(" Altitude Scaling: "+p.p.altitudeScaling, x, y += lineWidth);
//				applet.text(" Lock Media to Clusters:"+p.p.lockMediaToClusters, x, y += lineWidth);

//				applet.textSize(mediumTextSize);
//				applet.text(" Graphics ", x, y += lineWidthVeryWide);
//				applet.textSize(smallTextSize);
//				applet.text(" Alpha:"+p.p.alpha, x, y += lineWidthVeryWide);
//				applet.text(" Default Media Length:"+p.p.settings.defaultMediaLength, x, y += lineWidth);
//				applet.text(" Media Angle Fading: "+p.p.viewer.settings.angleFading, x, y += lineWidth);
//				applet.text(" Media Angle Thinning: "+p.p.viewer.settings.angleThinning, x, y += lineWidth);
//				if(p.p.viewer.settings.angleThinning)
//					applet.text(" Media Thinning Angle:"+p.p.viewer.settings.thinningAngle, x, y += lineWidth);
//				applet.text(" Image Size Factor:"+p.p.subjectSizeRatio, x, y += lineWidth);
//				applet.text(" Subject Distance (m.):"+p.p.defaultFocusDistance, x, y += lineWidth);

				applet.textSize(mediumTextSize);
//				applet.text(" Field", x, y += lineWidthVeryWide);
				applet.text(" Field: "+f.name, x, y += lineWidthVeryWide);
				applet.textSize(smallTextSize);
				applet.text(" ID: "+(p.p.viewer.getField()+1)+" out of "+p.p.getFieldCount()+" Total Fields", x, y += lineWidthVeryWide);
				applet.text(" Width: "+f.model.fieldWidth+" Length: "+f.model.fieldLength+" Height: "+f.model.fieldHeight, x, y += lineWidth);
				applet.text(" Image Count: "+f.getImageCount(), x, y += lineWidth);					// Doesn't check for dataMissing!!
				applet.text(" Panorama Count: "+f.getPanoramaCount(), x, y += lineWidth);			// Doesn't check for dataMissing!!
				applet.text(" Video Count: "+f.getVideoCount(), x, y += lineWidth);					// Doesn't check for dataMissing!!
//				applet.text(" Sound Count: "+f.getSoundCount(), x, y += lineWidth);					// Doesn't check for dataMissing!!
				applet.text(" Media Density (per sq. m.): "+f.model.mediaDensity, x, y += lineWidth);
				
//				applet.text(" Clusters Visible: "+p.p.viewer.clustersVisible+"  (Orientation Mode)", x, y += lineWidth);
				
				applet.text(" Images Visible: "+f.imagesVisible, x, y += lineWidth);
				applet.text(" Panoramas Visible: "+f.panoramasVisible, x, y += lineWidth);
				applet.text(" Videos Visible: "+f.videosVisible, x, y += lineWidth);
				applet.text("   Currently Playing: "+f.videosPlaying, x, y += lineWidth);
//				applet.text(" Sounds Audible: "+f.soundsAudible, x, y += lineWidth);
//				applet.text("   Currently Playing: "+f.soundsPlaying, x, y += lineWidth);

				applet.textSize(mediumTextSize);
				applet.text(" Model ", x, y += lineWidthVeryWide);
				applet.textSize(smallTextSize);

				applet.text(" Clusters:"+(f.clusters.size()-f.model.mergedClusters), x, y += lineWidthVeryWide);
				applet.text(" Merged: "+f.model.mergedClusters+" out of "+f.clusters.size()+" Total", x, y += lineWidth);
				applet.text(" Minimum Distance: "+p.p.minClusterDistance, x, y += lineWidth);
				applet.text(" Maximum Distance: "+p.p.maxClusterDistance, x, y += lineWidth);
				if(p.p.altitudeScaling)
					applet.text(" Altitude Scaling Factor: "+p.p.altitudeScalingFactor+"  (Altitude Scaling)", x, y += lineWidthVeryWide);
				applet.text(" Clustering Method : "+ ( p.p.hierarchical ? "Hierarchical" : "K-Means" ), x, y += lineWidth);
				applet.text(" Population Factor: "+f.model.clusterPopulationFactor, x, y += lineWidth);
				if(p.p.hierarchical) applet.text(" Current Cluster Depth: "+f.model.clusterDepth, x, y += lineWidth);

				applet.textSize(mediumTextSize);
				applet.text(" Viewer ", x, y += lineWidthVeryWide);
				applet.textSize(smallTextSize);
				applet.text(" Location, x: "+PApplet.round(p.p.viewer.getLocation().x)+" y:"+PApplet.round(p.p.viewer.getLocation().y)+" z:"+
						PApplet.round(p.p.viewer.getLocation().z), x, y += lineWidthVeryWide);		
				applet.text(" GPS Longitude: "+p.p.viewer.getGPSLocation().x+" Latitude:"+p.p.viewer.getGPSLocation().y, x, y += lineWidth);		

				applet.text(" Current Cluster: "+p.p.viewer.getCurrentClusterID(), x, y += lineWidthVeryWide);
				applet.text("   Media Points: "+c.mediaCount, x, y += lineWidth);
				applet.text("   Media Segments: "+p.p.getCurrentCluster().segments.size(), x, y += lineWidth);
				applet.text("   Distance: "+PApplet.round(PVector.dist(c.getLocation(), p.p.viewer.getLocation())), x, y += lineWidth);
				applet.text("   Auto Stitched Panoramas: "+p.p.getCurrentCluster().stitchedPanoramas.size(), x, y += lineWidth);
				applet.text("   User Stitched Panoramas: "+p.p.getCurrentCluster().userPanoramas.size(), x, y += lineWidth);
				if(p.p.viewer.getAttractorCluster() != -1)
				{
					applet.text(" Destination Cluster : "+p.p.viewer.getAttractorCluster(), x, y += lineWidth);
					applet.text(" Destination Media Points: "+p.p.getCluster(p.p.viewer.getAttractorCluster()).mediaCount, x, y += lineWidth);
					applet.text("    Destination Distance: "+PApplet.round( PVector.dist(f.clusters.get(p.p.viewer.getAttractorCluster()).getLocation(), p.p.viewer.getLocation() )), x, y += lineWidth);
				}

				if(p.p.p.debug.viewer) 
				{
					applet.text(" Debug: Current Attraction: "+p.p.viewer.attraction.mag(), x, y += lineWidth);
					applet.text(" Debug: Current Acceleration: "+(p.p.viewer.isWalking() ? p.p.viewer.walkingAcceleration.mag() : p.p.viewer.acceleration.mag()), x, y += lineWidth);
					applet.text(" Debug: Current Velocity: "+ (p.p.viewer.isWalking() ? p.p.viewer.walkingVelocity.mag() : p.p.viewer.velocity.mag()) , x, y += lineWidth);
					applet.text(" Debug: Moving? " + p.p.viewer.isMoving(), x, y += lineWidth);
					applet.text(" Debug: Slowing? " + p.p.viewer.isSlowing(), x, y += lineWidth);
					applet.text(" Debug: Halting? " + p.p.viewer.isHalting(), x, y += lineWidth);
				}

				if(p.p.p.debug.viewer)
				{
					applet.text(" Debug: X Orientation (Yaw):" + p.p.viewer.getXOrientation(), x, y += lineWidth);
					applet.text(" Debug: Y Orientation (Pitch):" + p.p.viewer.getYOrientation(), x, y += lineWidth);
					applet.text(" Debug: Target Point x:" + camTar[0] + ", y:" + camTar[1] + ", z:" + camTar[2], x, y += lineWidth);
				}
				else
				{
					applet.text(" Compass Direction:" + p.p.p.utilities.angleToCompass(p.p.viewer.getXOrientation())+" Angle: "+p.p.viewer.getXOrientation(), x, y += lineWidth);
					applet.text(" Vertical Direction:" + PApplet.degrees(p.p.viewer.getYOrientation()), x, y += lineWidth);
					applet.text(" Zoom:"+p.p.viewer.camera.fov(), x, y += lineWidth);
				}
				applet.text(" Field of View:"+p.p.viewer.camera.fov(), x, y += lineWidth);

				applet.textSize(mediumTextSize);
				applet.text(" Output ", x, y += lineWidthVeryWide);
				applet.textSize(smallTextSize);
				applet.text(" Image Output Folder:"+p.p.outputFolder, x, y += lineWidthVeryWide);
				applet.text(" Library Folder:"+p.p.p.library.getLibraryFolder(), x, y += lineWidth);

				if(p.p.p.debug.memory)
				{
					if(p.p.p.debug.detailed)
					{
						applet.text("Total memory (bytes): " + p.p.p.debug.totalMemory, x, y += lineWidth);
						applet.text("Available processors (cores): "+p.p.p.debug.availableProcessors, x, y += lineWidth);
						applet.text("Maximum memory (bytes): " +  (p.p.p.debug.maxMemory == Long.MAX_VALUE ? "no limit" : p.p.p.debug.maxMemory), x, y += lineWidth); 
						applet.text("Total memory (bytes): " + p.p.p.debug.totalMemory, x, y += lineWidth);
						applet.text("Allocated memory (bytes): " + p.p.p.debug.allocatedMemory, x, y += lineWidth);
					}
					applet.text("Free memory (bytes): "+p.p.p.debug.freeMemory, x, y += lineWidth);
					applet.text("Approx. usable free memory (bytes): " + p.p.p.debug.approxUsableFreeMemory, x, y += lineWidth);
				}			
			}
			else
				p.message("Can't display statistics: currentCluster == "+p.p.viewer.getCurrentClusterID()+"!!!");
		}
	}
	
	/**
	 * Handles mouse events for Statistics Window 
	 * @param applet the main PApplet object
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void statisticsWindowMouse(PApplet applet, GWinData data, MouseEvent event) {
		WMV_WinData wmvWinData = (WMV_WinData)data;
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
		WMV_WinData wmvWinData = (WMV_WinData)data;
		switch(event.getAction()) {

		case MouseEvent.PRESS:
			wmvWinData.sx = wmvWinData.ex = applet.mouseX;
			wmvWinData.sy = wmvWinData.ey = applet.mouseY;
			wmvWinData.done = false;
//			PApplet.println("Mouse pressed");
			break;
		case MouseEvent.RELEASE:
			wmvWinData.ex = applet.mouseX;
			wmvWinData.ey = applet.mouseY;
			wmvWinData.done = true;
//			PApplet.println("Mouse released:"+data.toString());
			break;
		case MouseEvent.DRAG:
			wmvWinData.ex = applet.mouseX;
			wmvWinData.ey = applet.mouseY;
//			PApplet.println("Mouse dragged");
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
		WMV_WinData wmvWinData = (WMV_WinData)data;
		switch(event.getAction()) {

		case MouseEvent.PRESS:
			wmvWinData.sx = wmvWinData.ex = applet.mouseX;
			wmvWinData.sy = wmvWinData.ey = applet.mouseY;
			wmvWinData.done = false;
//			PApplet.println("Mouse pressed");
			break;
		case MouseEvent.RELEASE:
			wmvWinData.ex = applet.mouseX;
			wmvWinData.ey = applet.mouseY;
			wmvWinData.done = true;
//			PApplet.println("Mouse released:"+data.toString());
			break;
		case MouseEvent.DRAG:
			wmvWinData.ex = applet.mouseX;
			wmvWinData.ey = applet.mouseY;
//			PApplet.println("Mouse dragged");
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
		applet.text("Points in Memory:"+p.p.viewer.getMemoryPath().size(), xPos, yPos += lineWidthVeryWide);
		
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
		WMV_WinData wmvWinData = (WMV_WinData)data;
		switch(event.getAction()) {

		case MouseEvent.PRESS:
			wmvWinData.sx = wmvWinData.ex = applet.mouseX;
			wmvWinData.sy = wmvWinData.ey = applet.mouseY;
			wmvWinData.done = false;
//			PApplet.println("Mouse pressed");
			break;
		case MouseEvent.RELEASE:
			wmvWinData.ex = applet.mouseX;
			wmvWinData.ey = applet.mouseY;
			wmvWinData.done = true;
//			PApplet.println("Mouse released:"+data.toString());
			break;
		case MouseEvent.DRAG:
			wmvWinData.ex = applet.mouseX;
			wmvWinData.ey = applet.mouseY;
//			PApplet.println("Mouse dragged");
			break;
		}
	}
	
	void showWMVWindow()
	{
		showWMVWindow = true;
		wmvWindow.setVisible(true);
	} 
	
	void showNavigationWindow()
	{
		showNavigationWindow = true;
		if(setupNavigationWindow)
			navigationWindow.setVisible(true);
		if(showWMVWindow)
			hideWMVWindow();
		PApplet.println("showNavigationWindow()... showNavigationWindow:"+showNavigationWindow);
	} 
	void showTimeWindow()
	{
		showTimeWindow = true;
		if(setupTimeWindow)
			timeWindow.setVisible(true);
		if(showWMVWindow)
			hideWMVWindow();
	}
	void showGraphicsWindow()
	{
		showGraphicsWindow = true;
		if(setupGraphicsWindow)
			graphicsWindow.setVisible(true);
		if(showWMVWindow)
			hideWMVWindow();
	}
	void showModelWindow()
	{
		showModelWindow = true;
		if(setupModelWindow)
			modelWindow.setVisible(true);
		if(showWMVWindow)
			hideWMVWindow();
	}
	void showMemoryWindow()
	{
		showMemoryWindow = true;
		if(setupMemoryWindow)
			memoryWindow.setVisible(true);
		if(showWMVWindow)
			hideWMVWindow();
	}
	void showSelectionWindow()
	{
		showSelectionWindow = true;
		if(setupSelectionWindow)
			selectionWindow.setVisible(true);
		if(showWMVWindow)
			hideWMVWindow();
	} 
	void showStatisticsWindow()
	{
		showStatisticsWindow = true;
		if(setupStatisticsWindow)
			statisticsWindow.setVisible(true);
		if(showWMVWindow)
			hideWMVWindow();
	} 
	void showHelpWindow()
	{
		showHelpWindow = true;
		if(setupHelpWindow)
			helpWindow.setVisible(true);
	}
	void hideWMVWindow()
	{
		showWMVWindow = false;
		wmvWindow.setVisible(false);
	} 
	void hideNavigationWindow()
	{
		showNavigationWindow = false;
		if(setupNavigationWindow)
			navigationWindow.setVisible(false);
//		PApplet.println("hideNavigationWindow()... showNavigationWindow:"+showNavigationWindow);
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
		hideWMVWindow();
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
class WMV_WinData extends GWinData {
	int sx, sy, ex, ey;
	boolean done;

	//		MyWinData(){}
}