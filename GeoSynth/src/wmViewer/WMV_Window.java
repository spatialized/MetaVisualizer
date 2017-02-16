package wmViewer;

import java.awt.Font;

import g4p_controls.*;
import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

public class WMV_Window {

	WMV_Display p;
	
	private int sidebarWidth = 310, longSidebarHeight = 600;
	private int shortSidebarHeight = sidebarWidth;
	
	/* Windows */
	public GWindow wmvWindow, navigationWindow, graphicsWindow, selectionWindow, statisticsWindow, helpWindow;
	private GLabel lblWMViewer, lblNavigation, lblGraphics, lblStatistics, lblHelp;				// Window headings
	public boolean showWMVWindow = false, showNavigationWindow = false, showGraphicsWindow = false,
				    showSelectionWindow = false, showStatisticsWindow = false, showHelpWindow = false;
	//	private GSketchPad sketchPad;

	/* Views Window */
	private GButton btnNavigationWindow, btnGraphicsWindow;										// Buttons to open other windows
	private GButton btnSelectionWindow, btnStatisticsWindow, btnHelpWindow;
	private GButton btnSceneView, btnMapView, btnInfoView, btnClusterView, btnControlView;		// Buttons to change Display View for main window
	private GButton btnLoadMediaLibrary;

	/* Navigation Window */
	private GButton btnImportGPSTrack;
	private GButton btnSaveLocation, btnClearMemory;
	private GButton btnJumpToRandomCluster;
	private GButton btnZoomIn, btnZoomOut;
	private GButton btnSaveImage, btnOutputFolder;

	private GButton btnNextTimeSegment, btnPreviousTimeSegment;
	private GButton btnMoveToNearestCluster;
	private GButton btnGoToPreviousField, btnGoToNextField;
	private GButton btnFollowStart, btnFollowStop;	

	/* Graphics Window */
	private GLabel lblDisplayMode;
	private GLabel lblGraphicsModes, lblTime, lblNavigationCommands, lblPathFollowing, lblModel, lblOutput;
	public GLabel lblCurrentTime;
	
	private GCheckbox chkbxTimeFading;
	private GToggleGroup tgFollow;	
	private GOption optTimeline, optGPSTrack, optMemory;
	private GCheckbox chkbxMovementTeleport, chkbxFollowTeleport;
	
	private GCheckbox chkbxFadeEdges;
	private GCheckbox chkbxHideImages, chkbxHideVideos, chkbxHidePanoramas;
	private GCheckbox chkbxAlphaMode;
	private GCheckbox chkbxOrientationMode;
	private GCheckbox chkbxAngleFading, chkbxAngleThinning;

	private GLabel lblMediaSize, lblAlpha, lblMediaLength;
	private GSlider sdrAlpha;//, sdrMediaSize;
	private GSlider sdrMediaLength;

	private GLabel lblSubjectDistance;
	private GButton btnSubjectDistanceUp, btnSubjectDistanceDown;

	/* Selection Window */
	private GLabel lblSelection, lblViewing;
	private GCheckbox chkbxMultiSelection, chkbxSelectGroups, chkbxViewMetadata;
	private GButton btnSelectFront, btnDeselectFront, btnDeselectAll, btnExitSelectionMode;

	/* Statistics Window */
	private GButton btnExitStatisticsMode;

	/* Help Window */
	private GButton btnExitHelpMode;
	
//		p.showModel = !p.showModel;
//	private GDropList textH, textV, iconH, iconV, iconP;
	
	String windowTitle = " ";

	WMV_Window( WMV_Display parent )
	{
		p = parent;

		setupWMVWindow();
		setupNavigationSidebar();
		setupGraphicsSidebar();
		setupHelpWindow();
		setupStatisticsSidebar();
		setupSelectionWindow();
	}
	
	void setupWMVWindow()
	{
		wmvWindow = GWindow.getWindow(p.p.p, windowTitle, 10, 45, sidebarWidth, shortSidebarHeight, PApplet.JAVA2D);
		wmvWindow.setVisible(true);
		wmvWindow.addData(new WMV_WinData());
		wmvWindow.addDrawHandler(this, "wmvWindowDraw");
		wmvWindow.addMouseHandler(this, "wmvWindowMouse");
		wmvWindow.addKeyHandler(p.p.p, "wmvWindowKey");
		
		int x = 0, y = 12;

		x = 0;

		lblWMViewer = new GLabel(wmvWindow, x, y, wmvWindow.width, 20, "WorldMediaViewer v1.0");
		lblWMViewer.setLocalColorScheme(10);
		lblWMViewer.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblWMViewer.setTextAlign(GAlign.CENTER, null);
		lblWMViewer.setTextBold();

		x = 80;
		y += 30;

		btnNavigationWindow = new GButton(wmvWindow, x, y, 130, 20, "Navigation Controls");
		btnNavigationWindow.tag = "OpenNavigationWindow";
		btnNavigationWindow.setLocalColorScheme(5);

		x = 80;
		y += 30;

		btnGraphicsWindow = new GButton(wmvWindow, x, y, 130, 20, "Graphics Controls");
		btnGraphicsWindow.tag = "OpenGraphicsWindow";
		btnGraphicsWindow.setLocalColorScheme(5);

		x = 80;
		y += 30;

		btnStatisticsWindow = new GButton(wmvWindow, x, y, 130, 20, "Show Statistics");
		btnStatisticsWindow.tag = "OpenStatisticsWindow";
		btnStatisticsWindow.setLocalColorScheme(5);
		
		x = 70;
		y += 40;

		btnSelectionWindow = new GButton(wmvWindow, x, y, 150, 20, "Enter Selection Mode");
		btnSelectionWindow.tag = "SelectionMode";
		btnSelectionWindow.setLocalColorScheme(4);

		x = 70;
		y += 30;
		
		btnLoadMediaLibrary = new GButton(wmvWindow, x, y, 150, 20, "Load Media Library...");
		btnLoadMediaLibrary.tag = "Restart";
		btnLoadMediaLibrary.setLocalColorScheme(7);

		x = 105;
		y += 40;
		
		btnHelpWindow = new GButton(wmvWindow, x, y, 70, 20, "Help...");
		btnHelpWindow.tag = "OpenHelpWindow";
		btnHelpWindow.setLocalColorScheme(5);
		
		x = 0;
		y = wmvWindow.height - 60;
		lblDisplayMode = new GLabel(wmvWindow, x, y, wmvWindow.width, 20);						/* Display Mode Label */
		lblDisplayMode.setText("Display Mode");
		lblDisplayMode.setLocalColorScheme(10);
		lblDisplayMode.setTextAlign(GAlign.CENTER, null);
		lblDisplayMode.setTextBold();
		lblDisplayMode.setTextItalic();
		
		x = 15;
		btnSceneView = new GButton(wmvWindow, x, y+=24, 55, 20, "Scene");			/* Display Mode Buttons */
		btnSceneView.tag = "Scene";
		btnSceneView.setLocalColorScheme(5);
		btnMapView = new GButton(wmvWindow, x+=55, y, 55, 20, "Map");
		btnMapView.tag = "Map";
		btnMapView.setLocalColorScheme(5);
		btnInfoView = new GButton(wmvWindow, x+=55, y, 55, 20, "Info");
		btnInfoView.tag = "Info";
		btnInfoView.setLocalColorScheme(5);
		btnClusterView = new GButton(wmvWindow, x+=55, y, 55, 20, "Cluster");
		btnClusterView.tag = "Cluster";
		btnClusterView.setLocalColorScheme(5);
		btnControlView = new GButton(wmvWindow, x+=55, y, 55, 20, "Control");
		btnControlView.tag = "Control";
		btnControlView.setLocalColorScheme(5);
	}
	
	void setupNavigationSidebar()
	{
		navigationWindow = GWindow.getWindow(p.p.p, windowTitle, 10, shortSidebarHeight, sidebarWidth, longSidebarHeight, PApplet.JAVA2D);
		navigationWindow.setVisible(false);
		navigationWindow.addData(new WMV_WinData());
		navigationWindow.addDrawHandler(this, "navigationSidebarDraw");
		navigationWindow.addMouseHandler(this, "navigationSidebarMouse");
		navigationWindow.addKeyHandler(p.p.p, "navigationSidebarKey");
		
		int x = 0, y = 12;

		x = 0;

		lblNavigation = new GLabel(navigationWindow, x, y, navigationWindow.width, 20, "Navigation");
		lblNavigation.setLocalColorScheme(10);
		lblNavigation.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblNavigation.setTextAlign(GAlign.CENTER, null);
		lblNavigation.setTextBold();

		y += 30;

		lblNavigationCommands = new GLabel(navigationWindow, x, y, navigationWindow.width, 20, "Navigation Commands");
		lblNavigationCommands.setLocalColorScheme(10);
		lblNavigationCommands.setTextAlign(GAlign.CENTER, null);
		lblNavigationCommands.setTextBold();
		lblNavigationCommands.setTextItalic();

		x = 90;
		y += 25;

		btnMoveToNearestCluster = new GButton(navigationWindow, x, y, 100, 20, "Nearest Cluster");
		btnMoveToNearestCluster.tag = "NearestCluster";
		btnMoveToNearestCluster.setLocalColorScheme(5);
		btnMoveToNearestCluster = new GButton(navigationWindow, x+=105, y, 100, 20, "Last Cluster");
		btnMoveToNearestCluster.tag = "LastCluster";
		btnMoveToNearestCluster.setLocalColorScheme(5);

		x = 15;
		y += 25;

		chkbxMovementTeleport = new GCheckbox(navigationWindow, x, y, 70, 20, "Teleport");
		chkbxMovementTeleport.tag = "MovementTeleport";
		chkbxMovementTeleport.setLocalColorScheme(10);
		btnJumpToRandomCluster = new GButton(navigationWindow, x+75, y, 110, 20, "Random Cluster");
		btnJumpToRandomCluster.tag = "RandomCluster";
		btnJumpToRandomCluster.setLocalColorScheme(5);

		x = 90;
		y += 25;

		btnPreviousTimeSegment = new GButton(navigationWindow, x, y, 100, 20, "Previous Time");
		btnPreviousTimeSegment.tag = "PreviousTime";
		btnPreviousTimeSegment.setLocalColorScheme(5);
		btnNextTimeSegment = new GButton(navigationWindow, x+=105, y, 100, 20, "Next Time");
		btnNextTimeSegment.tag = "NextTime";
		btnNextTimeSegment.setLocalColorScheme(5);
		
		x = 0;
		y += 30;
		lblPathFollowing = new GLabel(navigationWindow, x, y, navigationWindow.width, 20, "Path Following");
		lblPathFollowing.setLocalColorScheme(10);
		lblPathFollowing.setTextAlign(GAlign.CENTER, null);
		lblPathFollowing.setTextBold();
		lblPathFollowing.setTextItalic();
		
		x = 40;
		y += 25;
		
		btnSaveLocation = new GButton(navigationWindow, x, y, 100, 20, "Save Location");
		btnSaveLocation.tag = "SaveLocation";
		btnSaveLocation.setLocalColorScheme(5);
		btnClearMemory = new GButton(navigationWindow, x+100, y, 100, 20, "Clear Memory");
		btnClearMemory.tag = "ClearMemory";
		btnClearMemory.setLocalColorScheme(0);

		x = 80;
		y += 30;

		btnImportGPSTrack = new GButton(navigationWindow, x, y, 140, 20, "Import GPS Track");
		btnImportGPSTrack.tag = "ImportGPSTrack";
		btnImportGPSTrack.setLocalColorScheme(5);		

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

		x = 15;
		y += 25;

		chkbxFollowTeleport = new GCheckbox(navigationWindow, x, y, 80, 20, "Teleporting");
		chkbxFollowTeleport.tag = "FollowTeleport";
		chkbxFollowTeleport.setLocalColorScheme(10);

		btnFollowStart = new GButton(navigationWindow, x+=90, y, 60, 20, "Start");
		btnFollowStart.tag = "FollowStart";
		btnFollowStart.setLocalColorScheme(5);
		
		btnFollowStop = new GButton(navigationWindow, x+=60, y, 60, 20, "Stop");
		btnFollowStop.tag = "FollowStop";
		btnFollowStop.setLocalColorScheme(0);

		if(p.p.getFields() != null)
		{
			x = 60;
			y += 30;
			btnGoToPreviousField = new GButton(navigationWindow, x, y, 90, 20, "Previous Field");
			btnGoToPreviousField.tag = "PreviousField";
			btnGoToPreviousField.setLocalColorScheme(5);

			btnGoToNextField = new GButton(navigationWindow, x+=90, y, 90, 20, "Next Field");
			btnGoToNextField.tag = "NextField";
			btnGoToNextField.setLocalColorScheme(5);
		}
	}
	
	
	
	void setupGraphicsSidebar()
	{
		graphicsWindow = GWindow.getWindow(p.p.p, windowTitle, 10, shortSidebarHeight, sidebarWidth, longSidebarHeight, PApplet.JAVA2D);
		graphicsWindow.setVisible(false);
		graphicsWindow.addData(new WMV_WinData());
		graphicsWindow.addDrawHandler(this, "graphicsSidebarDraw");
		graphicsWindow.addMouseHandler(this, "graphicsSidebarMouse");
		graphicsWindow.addKeyHandler(p.p.p, "graphicsSidebarKey");
	
		int x = 0, y = 12;

		lblGraphics = new GLabel(graphicsWindow, x, y, graphicsWindow.width, 20, "Graphics");
		lblGraphics.setLocalColorScheme(10);
		lblGraphics.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblGraphics.setTextAlign(GAlign.CENTER, null);
		lblGraphics.setTextBold();

		x = 60;
		y += 30;
		
		btnZoomIn = new GButton(graphicsWindow, x, y, 80, 20, "Zoom In");
		btnZoomIn.tag = "ZoomIn";
		btnZoomIn.setLocalColorScheme(5);
		btnZoomOut = new GButton(graphicsWindow, x+=90, y, 80, 20, "Zoom Out");
		btnZoomOut.tag = "ZoomOut";
		btnZoomOut.setLocalColorScheme(5);

		x = 130;
		y += 25;

		lblAlpha= new GLabel(graphicsWindow, x, y, 60, 20, "Alpha");
		lblAlpha.setLocalColorScheme(10);
//		lblAlpha= new GLabel(mainSidebar, x, y, 60, 20, "Alpha");
//		lblAlpha.setLocalColorScheme(10);

		x = 110;
		y += 80;

		sdrAlpha = new GSlider(graphicsWindow, x, y+25, 80, 80, 20);
		sdrAlpha.setLocalColorScheme(5);
		sdrAlpha.setLimits(p.p.alpha, 0.f, 255.f);
		sdrAlpha.setRotation(-PApplet.PI/2.f);
		sdrAlpha.setTextOrientation(G4P.ORIENT_LEFT);
		sdrAlpha.setEasing(10);
		sdrAlpha.setShowValue(true);
		sdrAlpha.tag = "Alpha";

//		x = 50;
//		y += 30;
//
//		x = 40;
//		y += 100;
//
//		sdrAlpha = new GSlider(mainSidebar, x, y, 90, 90, 20);
//		sdrAlpha.setLocalColorScheme(5);
//		sdrAlpha.setLimits(p.p.alpha, 0.f, 255.f);
//		sdrAlpha.setRotation(-PApplet.PI/2.f);
//		sdrAlpha.setTextOrientation(G4P.ORIENT_LEFT);
//		sdrAlpha.setEasing(10);
//		sdrAlpha.setShowValue(true);
//		sdrAlpha.tag = "Alpha";

		x = 0;
		y += 30;

		lblGraphicsModes = new GLabel(graphicsWindow, x, y, graphicsWindow.width, 20, "Modes");
		lblGraphicsModes.setLocalColorScheme(10);
		lblGraphicsModes.setTextAlign(GAlign.CENTER, null);
		lblGraphicsModes.setTextBold();
		lblGraphicsModes.setTextItalic();
		
		x = 50;
		y += 25;

		chkbxAlphaMode = new GCheckbox(graphicsWindow, x, y, 85, 20, "Alpha Mode");
		chkbxAlphaMode.tag = "AlphaMode";
		chkbxAlphaMode.setLocalColorScheme(10);
		chkbxAlphaMode.setSelected(true);

		chkbxAngleFading = new GCheckbox(graphicsWindow, x+=85, y, 100, 20, "Angle Fading");
		chkbxAngleFading.tag = "AngleFading";
		chkbxAngleFading.setLocalColorScheme(10);
		chkbxAngleFading.setSelected(true);

		x = 50;
		y += 25;

		chkbxFadeEdges = new GCheckbox(graphicsWindow, x, y, 85, 20, "Fade Edges");
		chkbxFadeEdges.tag = "FadeEdges";
		chkbxFadeEdges.setLocalColorScheme(10);
		chkbxFadeEdges.setSelected(true);

		chkbxAngleThinning = new GCheckbox(graphicsWindow, x += 85, y, 100, 20, "Angle Thinning");
		chkbxAngleThinning.tag = "AngleThinning";
		chkbxAngleThinning.setLocalColorScheme(10);
		chkbxAngleThinning.setSelected(false);

		x = 85;
		y += 25;
		
		chkbxOrientationMode = new GCheckbox(graphicsWindow, x, y, 115, 20, "Orientation Mode");
		chkbxOrientationMode.tag = "OrientationMode";
		chkbxOrientationMode.setLocalColorScheme(10);
		chkbxOrientationMode.setSelected(false);

		x = 8;
		y += 30;

		chkbxHideImages = new GCheckbox(graphicsWindow, x, y, 90, 20, "Hide Images");
		chkbxHideImages.tag = "HideImages";
		chkbxHideImages.setLocalColorScheme(10);
		chkbxHideImages.setSelected(false);

		chkbxHideVideos = new GCheckbox(graphicsWindow, x += 90, y, 85, 20, "Hide Videos");
		chkbxHideVideos.tag = "HideVideos";
		chkbxHideVideos.setLocalColorScheme(10);
		chkbxHideVideos.setSelected(false);

		chkbxHidePanoramas = new GCheckbox(graphicsWindow, x += 85, y, 120, 20, "Hide Panoramas");
		chkbxHidePanoramas.tag = "HidePanoramas";
		chkbxHidePanoramas.setLocalColorScheme(10);
		chkbxHidePanoramas.setSelected(false);

		x = 0;
		y += 30;

		lblTime = new GLabel(graphicsWindow, x, y, graphicsWindow.width, 20, "Time");
		lblTime.setLocalColorScheme(10);
		lblTime.setTextAlign(GAlign.CENTER, null);
		lblTime.setTextBold();
		lblTime.setTextItalic();

		x = 125;
		y += 25;

		lblMediaLength = new GLabel(graphicsWindow, x, y, 80, 20, "Media Length");
		lblMediaLength .setLocalColorScheme(10);

		x = 110;
		y += 80;

		sdrMediaLength = new GSlider(graphicsWindow, x, y+25, 80, 80, 20);
		sdrMediaLength.setLocalColorScheme(5);
		sdrMediaLength.setLimits(p.p.defaultMediaLength, 10.f, 250.f);
		sdrMediaLength.setRotation(-PApplet.PI/2.f);
		sdrMediaLength.setTextOrientation(G4P.ORIENT_LEFT);
		sdrMediaLength.setEasing(10);
		sdrMediaLength.setShowValue(true);
		sdrMediaLength.tag = "MediaLength";

		x = 100;
		y += 30;
		
		chkbxTimeFading = new GCheckbox(graphicsWindow, x, y, 100, 20, "Time Fading");
		chkbxTimeFading.tag = "TimeFading";
		chkbxTimeFading.setLocalColorScheme(10);
		
		lblCurrentTime = new GLabel(graphicsWindow, x, y, graphicsWindow.width, 20, "-:-- am");
		lblCurrentTime.setLocalColorScheme(10);
		lblCurrentTime.setTextAlign(GAlign.CENTER, null);
		lblCurrentTime.setTextBold();
		lblCurrentTime.setTextItalic();
		
		x = 0;
		y += 30;

		lblModel = new GLabel(graphicsWindow, x, y, graphicsWindow.width, 20, "Model");
		lblModel.setLocalColorScheme(10);
		lblModel.setTextAlign(GAlign.CENTER, null);
		lblModel.setTextBold();
		lblModel.setTextItalic();

		x = 60;
		y += 30;
		
		btnSubjectDistanceDown = new GButton(graphicsWindow, x, y, 30, 20, "-");
		btnSubjectDistanceDown.tag = "SubjectDistanceDown";
		btnSubjectDistanceDown.setLocalColorScheme(5);
		lblSubjectDistance = new GLabel(graphicsWindow, x += 35, y, 110, 20, "Subject Distance");
		lblSubjectDistance.setLocalColorScheme(10);
		lblSubjectDistance.setTextBold();
		btnSubjectDistanceUp = new GButton(graphicsWindow, x += 105, y, 30, 20, "+");
		btnSubjectDistanceUp.tag = "SubjectDistanceUp";
		btnSubjectDistanceUp.setLocalColorScheme(5);
		
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
	}
	
	void setupStatisticsSidebar()
	{
		statisticsWindow = GWindow.getWindow(p.p.p, "Statistics", 10, 45, sidebarWidth, p.p.p.height, PApplet.JAVA2D);
		statisticsWindow.setVisible(false);
		statisticsWindow.addData(new WMV_WinData());
		statisticsWindow.addDrawHandler(this, "statisticsSidebarDraw");
		statisticsWindow.addMouseHandler(this, "statisticsSidebarMouse");
		
		int x = 0, y = 10;

		lblStatistics = new GLabel(statisticsWindow, x, y, statisticsWindow.width, 20, "Statistics");
		lblStatistics.setLocalColorScheme(10);
		lblStatistics.setTextAlign(GAlign.CENTER, null);
		lblStatistics.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblStatistics.setTextBold();

		x = 40;
		y = p.p.p.height - 40;
		
		btnExitStatisticsMode = new GButton(statisticsWindow, x, y, 180, 20, "Close Window");
		btnExitStatisticsMode.tag = "CloseStatisticsWindow";
		btnExitStatisticsMode.setLocalColorScheme(0);
		
		statisticsWindow.addKeyHandler(p.p.p, "statisticsSidebarKey");
	}

	void setupSelectionWindow()
	{
		selectionWindow = GWindow.getWindow(p.p.p, "Selection Mode", 10, 45, sidebarWidth, shortSidebarHeight, PApplet.JAVA2D);
		selectionWindow.setVisible(false);
		selectionWindow.addData(new WMV_WinData());
		selectionWindow.addDrawHandler(this, "selectionWindowDraw");
		selectionWindow.addMouseHandler(this, "selectionWindowMouse");
		
		int x = 0, y = 10;

		/* Selection Window */
		lblSelection = new GLabel(selectionWindow, x, y, selectionWindow.width, 20, "Selection");
		lblSelection.setLocalColorScheme(10);
		lblSelection.setTextAlign(GAlign.CENTER, null);
		lblSelection.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblSelection.setTextBold();

		x = 50;
		y += 30;

		btnSelectFront = new GButton(selectionWindow, x, y, 180, 20, "Select Media Ahead");
		btnSelectFront.tag = "SelectFront";
		btnSelectFront.setLocalColorScheme(5);

		x = 50;
		y += 30;
		
		btnDeselectFront = new GButton(selectionWindow, x, y, 180, 20, "Deselect Media Ahead");
		btnDeselectFront.tag = "DeselectFront";
		btnDeselectFront.setLocalColorScheme(5);

		x = 50;
		y += 30;

		btnDeselectAll = new GButton(selectionWindow, x, y, 180, 20, "Deselect All Media");
		btnDeselectAll.tag = "DeselectAll";
		btnDeselectAll.setLocalColorScheme(5);

		x = 20;
		y += 40;
		
		chkbxMultiSelection = new GCheckbox(selectionWindow, x, y, 110, 20, "Multi-Selection");
		chkbxMultiSelection.tag = "MultiSelection";
		chkbxMultiSelection.setLocalColorScheme(10);

		chkbxSelectGroups = new GCheckbox(selectionWindow, x+=120, y, 125, 20, "Segment Selection");
		chkbxSelectGroups.tag = "SegmentSelection";
		chkbxSelectGroups.setLocalColorScheme(10);
		
		x = 100;
		y += 30;
		
		chkbxViewMetadata = new GCheckbox(selectionWindow, x, y, 110, 20, "View Metadata");
		chkbxViewMetadata.tag = "ViewMetadata";
		chkbxViewMetadata.setLocalColorScheme(10);
		
		x = 50;
		y += 40;
		
		btnExitSelectionMode = new GButton(selectionWindow, x, y, 180, 20, "Exit Selection Mode");
		btnExitSelectionMode.tag = "ExitSelectionMode";
		btnExitSelectionMode.setLocalColorScheme(0);
		
		selectionWindow.addKeyHandler(p.p.p, "selectionSidebarKey");
	}

	void setupHelpWindow()
	{
		helpWindow = GWindow.getWindow(p.p.p, "Help", 10, 45, sidebarWidth, p.p.p.height, PApplet.JAVA2D);
		helpWindow.setVisible(false);
		helpWindow.addData(new WMV_WinData());
		helpWindow.addDrawHandler(this, "helpSidebarDraw");
		helpWindow.addMouseHandler(this, "helpSidebarMouse");
		
		int x = 0, y = 10;

		/* Selection Window */
		lblHelp = new GLabel(helpWindow, x, y, helpWindow.width, 20, "Help");
		lblHelp.setLocalColorScheme(10);
		lblHelp.setTextAlign(GAlign.CENTER, null);
		lblHelp.setFont(new Font("Monospaced", Font.PLAIN, 14));
		lblHelp.setTextBold();

		x = 40;
		y = p.p.p.height - 40;
		
		btnExitHelpMode = new GButton(helpWindow, x, y, 180, 20, "Close Window");
		btnExitHelpMode.tag = "CloseHelpWindow";
		btnExitHelpMode.setLocalColorScheme(0);
		
		helpWindow.addKeyHandler(p.p.p, "helpSidebarKey");
	}


	/**
	 * Handles drawing to the windows PApplet area
	 * @param applet the PApplet object embeded into the frame
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
	 * @param applet the PApplet object embeded into the frame
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
	 * Handles drawing to the windows PApplet area
	 * @param applet the PApplet object embeded into the frame
	 * @param data the data for the GWindow being used
	 */
	public void navigationSidebarDraw(PApplet applet, GWinData data) {
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
	 * @param applet the PApplet object embeded into the frame
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void navigationSidebarMouse(PApplet applet, GWinData data, MouseEvent event) {
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
	 * Handles drawing to the windows PApplet area
	 * @param applet the PApplet object embeded into the frame
	 * @param data the data for the GWindow being used
	 */
	public void graphicsSidebarDraw(PApplet applet, GWinData data) {
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
	 * @param applet the PApplet object embeded into the frame
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void graphicsSidebarMouse(PApplet applet, GWinData data, MouseEvent event) {
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
	 * Handles drawing to the windows PApplet area
	 * @param applet the PApplet object embeded into the frame
	 * @param data the data for the GWindow being used
	 */
	public void statisticsSidebarDraw(PApplet applet, GWinData data) {
		applet.background(10, 5, 50);
		applet.stroke(255);
		applet.strokeWeight(1);
		applet.fill(0, 0, 255);
		
		float lineWidthVeryWide = 18f;
		float lineWidthWide = 15f;
		float lineWidth = 12f;
		
		float xPos = 10;
		float yPos = 50;			// Starting vertical position

		float largeTextSize = 14.f;
		float mediumTextSize = 12.f;
		float smallTextSize = 10.f;

		WMV_Field f = p.p.getCurrentField();
		
		if(p.p.viewer.getCurrentCluster() >= 0)
		{
			WMV_Cluster c = p.p.getCurrentCluster();
			float[] camTar = p.p.viewer.camera.target();

			applet.fill(185, 215, 255, 255);					// Set text color
			
			applet.textSize(mediumTextSize);
			applet.text(" Program Modes ", xPos, yPos += lineWidthVeryWide);
			applet.textSize(smallTextSize);
			applet.text(" Orientation Mode: "+p.p.orientationMode, xPos, yPos += lineWidthVeryWide);
			applet.text(" Alpha Mode:"+p.p.alphaMode, xPos, yPos += lineWidth);
			applet.text(" Time Fading: "+ p.p.timeFading, xPos, yPos += lineWidth);
//			applet.text(" Date Fading: "+ p.dateFading, xPos, yPos += lineWidth);
			applet.text(" Altitude Scaling: "+p.p.altitudeScaling, xPos, yPos += lineWidth);
			applet.text(" Lock Media to Clusters:"+p.p.lockMediaToClusters, xPos, yPos += lineWidth);
		
			applet.textSize(mediumTextSize);
			applet.text(" Graphics ", xPos, yPos += lineWidthVeryWide);
			applet.textSize(smallTextSize);
			applet.text(" Alpha:"+p.p.alpha, xPos, yPos += lineWidthVeryWide);
			applet.text(" Default Media Length:"+p.p.defaultMediaLength, xPos, yPos += lineWidth);
			applet.text(" Media Angle Fading: "+p.p.angleFading, xPos, yPos += lineWidth);
			applet.text(" Media Angle Thinning: "+p.p.angleThinning, xPos, yPos += lineWidth);
			if(p.p.angleThinning)
				applet.text(" Media Thinning Angle:"+p.p.thinningAngle, xPos, yPos += lineWidth);
			applet.text(" Image Size Factor:"+p.p.subjectSizeRatio, xPos, yPos += lineWidth);
			applet.text(" Subject Distance (m.):"+p.p.defaultFocusDistance, xPos, yPos += lineWidth);
//			applet.text(" Image Size Factor:"+p.subjectSizeRatio, xPos, yPos += lineWidth);

//			yPos = topTextYOffset;			// Starting vertical position

			applet.textSize(mediumTextSize);
			applet.text(" Field", xPos, yPos += lineWidthVeryWide);
			applet.textSize(smallTextSize);
			applet.text(" Name: "+f.name, xPos, yPos += lineWidthVeryWide);
			applet.text(" ID: "+(p.p.viewer.getField()+1)+" out of "+p.p.getFieldCount()+" Total Fields", xPos, yPos += lineWidth);
			applet.text(" Width: "+f.model.fieldWidth+" Length: "+f.model.fieldLength+" Height: "+f.model.fieldHeight, xPos, yPos += lineWidth);
			applet.text(" Total Images: "+f.getImageCount(), xPos, yPos += lineWidth);					// Doesn't check for dataMissing!!
			applet.text(" Total Panoramas: "+f.getPanoramaCount(), xPos, yPos += lineWidth);			// Doesn't check for dataMissing!!
			applet.text(" Total Videos: "+f.getVideoCount(), xPos, yPos += lineWidth);					// Doesn't check for dataMissing!!
			applet.text(" Media Density per sq. m.: "+f.model.mediaDensity, xPos, yPos += lineWidth);
			applet.text(" Images Visible: "+f.imagesVisible, xPos, yPos += lineWidth);
			applet.text(" Panoramas Visible: "+f.panoramasVisible, xPos, yPos += lineWidth);
			applet.text(" Videos Visible: "+f.videosVisible, xPos, yPos += lineWidth);
			applet.text(" Videos Playing: "+f.videosPlaying, xPos, yPos += lineWidth);
			if(p.p.orientationMode)
				applet.text(" Clusters Visible: "+p.p.viewer.clustersVisible+"  (Orientation Mode)", xPos, yPos += lineWidth);

			applet.textSize(mediumTextSize);
			applet.text(" Model ", xPos, yPos += lineWidthVeryWide);
			applet.textSize(smallTextSize);
			
			applet.text(" Clusters:"+(f.clusters.size()-f.model.mergedClusters), xPos, yPos += lineWidthVeryWide);
			applet.text(" Merged: "+f.model.mergedClusters+" out of "+f.clusters.size()+" Total", xPos, yPos += lineWidth);
			applet.text(" Minimum Distance: "+p.p.minClusterDistance, xPos, yPos += lineWidth);
			applet.text(" Maximum Distance: "+p.p.maxClusterDistance, xPos, yPos += lineWidth);
			if(p.p.altitudeScaling)
				applet.text(" Altitude Scaling Factor: "+p.p.altitudeScalingFactor+"  (Altitude Scaling)", xPos, yPos += lineWidthVeryWide);
			applet.text(" Clustering Method : "+ ( p.p.hierarchical ? "Hierarchical" : "K-Means" ), xPos, yPos += lineWidth);
			applet.text(" Population Factor: "+f.model.clusterPopulationFactor, xPos, yPos += lineWidth);
			if(p.p.hierarchical) applet.text(" Current Cluster Depth: "+f.model.clusterDepth, xPos, yPos += lineWidth);

			applet.textSize(mediumTextSize);
			applet.text(" Viewer ", xPos, yPos += lineWidthVeryWide);
			applet.textSize(smallTextSize);
			applet.text(" Location, x: "+PApplet.round(p.p.viewer.getLocation().x)+" y:"+PApplet.round(p.p.viewer.getLocation().y)+" z:"+
					 PApplet.round(p.p.viewer.getLocation().z), xPos, yPos += lineWidthVeryWide);		
			applet.text(" GPS Longitude: "+p.p.viewer.getGPSLocation().x+" Latitude:"+p.p.viewer.getGPSLocation().y, xPos, yPos += lineWidth);		

			applet.text(" Current Cluster: "+p.p.viewer.getCurrentCluster(), xPos, yPos += lineWidthVeryWide);
			applet.text("   Media Points: "+c.mediaPoints, xPos, yPos += lineWidth);
			applet.text("   Media Segments: "+p.p.getCurrentCluster().segments.size(), xPos, yPos += lineWidth);
			applet.text("   Distance: "+PApplet.round(PVector.dist(c.getLocation(), p.p.viewer.getLocation())), xPos, yPos += lineWidth);
			applet.text("   Auto Stitched Panoramas: "+p.p.getCurrentCluster().stitchedPanoramas.size(), xPos, yPos += lineWidth);
			applet.text("   User Stitched Panoramas: "+p.p.getCurrentCluster().userPanoramas.size(), xPos, yPos += lineWidth);
			if(p.p.viewer.getAttractorCluster() != -1)
			{
				applet.text(" Destination Cluster : "+p.p.viewer.getAttractorCluster(), xPos, yPos += lineWidth);
				applet.text(" Destination Media Points: "+p.p.getCluster(p.p.viewer.getAttractorCluster()).mediaPoints, xPos, yPos += lineWidth);
				applet.text("    Destination Distance: "+PApplet.round( PVector.dist(f.clusters.get(p.p.viewer.getAttractorCluster()).getLocation(), p.p.viewer.getLocation() )), xPos, yPos += lineWidth);
			}

			if(p.p.p.debug.viewer) 
			{
				applet.text(" Debug: Current Attraction: "+p.p.viewer.attraction.mag(), xPos, yPos += lineWidth);
				applet.text(" Debug: Current Acceleration: "+(p.p.viewer.isWalking() ? p.p.viewer.walkingAcceleration.mag() : p.p.viewer.acceleration.mag()), xPos, yPos += lineWidth);
				applet.text(" Debug: Current Velocity: "+ (p.p.viewer.isWalking() ? p.p.viewer.walkingVelocity.mag() : p.p.viewer.velocity.mag()) , xPos, yPos += lineWidth);
				applet.text(" Debug: Moving? " + p.p.viewer.isMoving(), xPos, yPos += lineWidth);
				applet.text(" Debug: Slowing? " + p.p.viewer.isSlowing(), xPos, yPos += lineWidth);
				applet.text(" Debug: Halting? " + p.p.viewer.isHalting(), xPos, yPos += lineWidth);
			}

			if(p.p.p.debug.viewer)
			{
				applet.text(" Debug: X Orientation (Yaw):" + p.p.viewer.getXOrientation(), xPos, yPos += lineWidth);
				applet.text(" Debug: Y Orientation (Pitch):" + p.p.viewer.getYOrientation(), xPos, yPos += lineWidth);
				applet.text(" Debug: Target Point x:" + camTar[0] + ", y:" + camTar[1] + ", z:" + camTar[2], xPos, yPos += lineWidth);
			}
			else
			{
				applet.text(" Compass Direction:" + p.p.p.utilities.angleToCompass(p.p.viewer.getXOrientation())+" Angle: "+p.p.viewer.getXOrientation(), xPos, yPos += lineWidth);
				applet.text(" Vertical Direction:" + PApplet.degrees(p.p.viewer.getYOrientation()), xPos, yPos += lineWidth);
				applet.text(" Zoom:"+p.p.viewer.camera.fov(), xPos, yPos += lineWidth);
			}
			applet.text(" Field of View:"+p.p.viewer.camera.fov(), xPos, yPos += lineWidth);

//			yPos = topTextYOffset;			// Starting vertical position

			applet.textSize(mediumTextSize);
			applet.text(" Time ", xPos, yPos += lineWidthVeryWide);
			applet.textSize(smallTextSize);
			applet.text(" Time Mode: "+ ((p.p.p.world.timeMode == 0) ? "Cluster" : "Field"), xPos, yPos += lineWidthVeryWide);
			
			if(p.p.p.world.timeMode == 0)
				applet.text(" Current Field Time: "+ p.p.currentTime, xPos, yPos += lineWidth);
			if(p.p.p.world.timeMode == 1)
				applet.text(" Current Cluster Time: "+ p.p.getCurrentCluster().currentTime, xPos, yPos += lineWidth);
			applet.text(" Current Field Timeline Segments: "+ p.p.getCurrentField().timeline.size(), xPos, yPos += lineWidth);
			applet.text(" Current Field Time Segment: "+ p.p.viewer.currentFieldTimeSegment, xPos, yPos += lineWidth);
			if(f.timeline.size() > 0 && p.p.viewer.currentFieldTimeSegment >= 0 && p.p.viewer.currentFieldTimeSegment < f.timeline.size())
				applet.text(" Upper: "+f.timeline.get(p.p.viewer.currentFieldTimeSegment).getUpper().getTime()+
						" Center:"+f.timeline.get(p.p.viewer.currentFieldTimeSegment).getCenter().getTime()+
						" Lower: "+f.timeline.get(p.p.viewer.currentFieldTimeSegment).getLower().getTime(), xPos, yPos += lineWidth);
			applet.text(" Current Cluster Timeline Segments: "+ p.p.getCurrentCluster().timeline.size(), xPos, yPos += lineWidth);
//			applet.text(" Current Cluster Segment: "+ p.p.viewer.currentClusterTimeSegment, textXPos, textYPos += lineWidth);
//			applet.text(" Upper: "+c.timeline.get(p.p.viewer.currentFieldTimeSegment).getUpper()+" Center:"+c.timeline.get(p.p.viewer.currentFieldTimeSegment).getCenter()+
//					 " Lower: "+c.timeline.get(p.p.viewer.currentFieldTimeSegment).getLower(), textXPos, textYPos += lineWidth);			
//			textXPos = midRightTextXOffset;
//			textYPos = topTextYOffset;			// Starting vertical position
			
			applet.text(" Field Dateline Segments: "+ p.p.getCurrentField().dateline.size(), xPos, yPos += lineWidth);
//			applet.text(" Current Segment: "+ p.p.viewer.currentFieldDateSegment, xPos, yPos += lineWidth);
//			if(f.dateline.size() > 0) 
//			{
//				if( p.p.viewer.currentFieldDateSegment >= 0 && p.p.viewer.currentFieldDateSegment < f.dateline.size())
//					applet.text(" Upper: "+f.dateline.get(p.p.viewer.currentFieldDateSegment).getUpper()+" Center:"+f.dateline.get(p.p.viewer.currentFieldDateSegment).getCenter()+
//							" Lower: "+f.dateline.get(p.p.viewer.currentFieldDateSegment).getLower(), xPos, yPos += lineWidth);
//				applet.text(" Cluster Dateline Segments: "+ p.getCurrentCluster().dateline.size(), xPos, yPos += lineWidth);
//			}
			applet.textSize(mediumTextSize);
//			applet.text(" Output ", xPos, yPos += lineWidthVeryWide);
//			applet.textSize(smallTextSize);
//			applet.text(" Image Output Folder:"+p.outputFolder, textXPos, textYPos += lineWidthVeryWide);
//			applet.text(" Library Folder:"+p.p.getLibrary(), dispLocX, textYPos += lineWidthWide);

			if(p.p.p.debug.memory)
			{
				if(p.p.p.debug.detailed)
				{
					applet.text("Total memory (bytes): " + p.p.p.debug.totalMemory, xPos, yPos += lineWidth);
					applet.text("Available processors (cores): "+p.p.p.debug.availableProcessors, xPos, yPos += lineWidth);
					applet.text("Maximum memory (bytes): " +  (p.p.p.debug.maxMemory == Long.MAX_VALUE ? "no limit" : p.p.p.debug.maxMemory), xPos, yPos += lineWidth); 
					applet.text("Total memory (bytes): " + p.p.p.debug.totalMemory, xPos, yPos += lineWidth);
					applet.text("Allocated memory (bytes): " + p.p.p.debug.allocatedMemory, xPos, yPos += lineWidth);
				}
				applet.text("Free memory (bytes): "+p.p.p.debug.freeMemory, xPos, yPos += lineWidth);
				applet.text("Approx. usable free memory (bytes): " + p.p.p.debug.approxUsableFreeMemory, xPos, yPos += lineWidth);
			}			
//			applet.text(" MediaWorldViewer v1.0 by David Gordon, Copyright © 2016", xPos, yPos += lineWidthVeryWide);

		}
		else
			p.message("Can't display statistics: currentCluster == "+p.p.viewer.getCurrentCluster()+"!!!");
		
	}
	
	/**
	 * Handles mouse events for Statistics Sidebar GWindow objects
	 * @param applet the PApplet object embeded into the frame
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void statisticsSidebarMouse(PApplet applet, GWinData data, MouseEvent event) {
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
	 * Handles drawing to the windows PApplet area
	 * @param applet the PApplet object embeded into the frame
	 * @param data the data for the GWindow being used
	 */
	public void selectionWindowDraw(PApplet applet, GWinData data) {
		applet.background(10, 5, 50);
		applet.stroke(255);
		applet.strokeWeight(1);
		applet.fill(0, 0, 255);
	}

	/**
	 * Handles mouse events for ALL GWindow objects
	 * @param applet the PApplet object embeded into the frame
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
	 * Handles drawing to the Help Sidebar PApplet area
	 * @param applet the PApplet object embeded into the frame
	 * @param data the data for the GWindow being used
	 */
	public void helpSidebarDraw(PApplet applet, GWinData data) 
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
		applet.text(" SHIFT + Lt/Rt   Cycle Length - / +", xPos, yPos += lineWidth);
		applet.text(" SHIFT + Up/Dn   Current Time - / +", xPos, yPos += lineWidth);

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
		applet.text(" OPT + -   Visible Angle  -      ", xPos, yPos += lineWidth);
		applet.text(" OPT + =   Visible Angle  +      ", xPos, yPos += lineWidth);
		
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
		applet.text(" Memory", xPos, yPos += lineWidthVeryWide);
		applet.textSize(smallTextSize);
		applet.text(" `    Save Current View to Memory", xPos, yPos += lineWidthVeryWide);
		applet.text(" ~    Follow Memory Path", xPos, yPos += lineWidth);
		applet.text(" Y    Clear Memory", xPos, yPos += lineWidth);

		applet.textSize(mediumTextSize);
		applet.text(" Output", xPos, yPos += lineWidthVeryWide);
		applet.textSize(smallTextSize);
		applet.text(" o    Set Image Output Folder", xPos, yPos += lineWidthVeryWide);
		applet.text(" p    Save Screen Image to Disk", xPos, yPos += lineWidth);
	}

	/**
	 * Handles mouse events for Help Sidebar GWindow objects
	 * @param applet the PApplet object embeded into the frame
	 * @param data the data for the GWindow being used
	 * @param event the mouse event
	 */
	public void helpSidebarMouse(PApplet applet, GWinData data, MouseEvent event) {
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
		navigationWindow.setVisible(true);
	} 
	void showGraphicsWindow()
	{
		showGraphicsWindow = true;
		graphicsWindow.setVisible(true);
	}
	void showSelectionWindow()
	{
		showSelectionWindow = true;
		selectionWindow.setVisible(true);
	} 
	void showStatisticsWindow()
	{
		showStatisticsWindow = true;
		statisticsWindow.setVisible(true);
	} 
	void showHelpWindow()
	{
		showHelpWindow = false;
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
		navigationWindow.setVisible(false);
	} 
	void hideGraphicsWindow()
	{
		showGraphicsWindow = false;
		graphicsWindow.setVisible(false);
	}
	void hideSelectionWindow()
	{
		showSelectionWindow = false;
		selectionWindow.setVisible(false);
	} 
	void hideStatisticsWindow()
	{
		showStatisticsWindow = false;
		statisticsWindow.setVisible(false);
	} 
	void hideHelpWindow()
	{
		showHelpWindow = false;
		helpWindow.setVisible(false);
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