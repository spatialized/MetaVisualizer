package wmViewer;

import g4p_controls.*;
import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

public class WMV_Window {

	WMV_Display p;
	
	private int sidebarWidth = 310;
	
	public GWindow mainSidebar, selectionSidebar, statisticsSidebar;
	private GSketchPad sketchPad;

	/* Main Window */
	private GLabel lblDisplayMode;
	private GButton btnSceneView, btnMapView, btnInfoView, btnClusterView, btnControlView;
	private GButton btnSelectionMode, btnStatisticsView, btnRestart;
	
	private GButton btnImportGPSTrack;
	private GButton btnSaveLocation, btnClearMemory;
	private GButton btnJumpToRandomCluster;
	private GButton btnZoomIn, btnZoomOut;
	private GButton btnSaveImage, btnOutputFolder;
	
	private GLabel lblSubjectDistance;
	private GButton btnSubjectDistanceUp, btnSubjectDistanceDown;
	
	private GButton btnNextTimeSegment, btnPreviousTimeSegment;
	private GButton btnMoveToNearestCluster;
	private GButton btnGoToPreviousField, btnGoToNextField;
	private GButton btnFollowStart, btnFollowStop;	

	private GLabel lblGraphics, lblTime, lblAutoNavigation, lblPathNavigation, lblModel, lblOutput;
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

	/* Selection Window */
	private GLabel lblSelection, lblViewing, lblStatistics;
	private GButton btnSelectFront, btnDeselectAll, btnExitSelectionMode, btnExitStatisticsMode;
	private GCheckbox chkbxMultiSelection, chkbxSelectGroups, chkbxViewMetadata;
	
//		p.showModel = !p.showModel;
//	private GDropList textH, textV, iconH, iconV, iconP;
	
	String windowTitle = " ";

	WMV_Window( WMV_Display parent )
	{
		p = parent;

		setupMainSidebar();
		mainSidebar.setVisible(false);
	}
	
	void setupMainSidebar()
	{
		mainSidebar = GWindow.getWindow(p.p.p, windowTitle, 10, 45, sidebarWidth, p.p.p.height, PApplet.JAVA2D);
		mainSidebar.addData(new WMV_WinData());
		mainSidebar.addDrawHandler(this, "sidebarDraw");
		mainSidebar.addMouseHandler(this, "sidebarMouse");
		
		int x = 0, y = 30;

		lblTime = new GLabel(mainSidebar, x, y, mainSidebar.width, 20, "Time");
		lblTime.setLocalColorScheme(10);
		lblTime.setTextAlign(GAlign.CENTER, null);
		lblTime.setTextBold();
		lblTime.setTextItalic();

		x = 100;
		y += 25;
		
		chkbxTimeFading = new GCheckbox(mainSidebar, x, y, 100, 20, "Time Fading");
		chkbxTimeFading.tag = "TimeFading";
		chkbxTimeFading.setLocalColorScheme(10);
		
		lblCurrentTime = new GLabel(mainSidebar, x, y, mainSidebar.width, 20, "-:-- am");
		lblCurrentTime.setLocalColorScheme(10);
		lblCurrentTime.setTextAlign(GAlign.CENTER, null);
		lblCurrentTime.setTextBold();
		lblCurrentTime.setTextItalic();

		x = 0;
		y += 40;

		lblAutoNavigation = new GLabel(mainSidebar, x, y, mainSidebar.width, 20, "Auto Navigation");
		lblAutoNavigation.setLocalColorScheme(10);
		lblAutoNavigation.setTextAlign(GAlign.CENTER, null);
		lblAutoNavigation.setTextBold();
		lblAutoNavigation.setTextItalic();

		x = 90;
		y += 25;

		btnMoveToNearestCluster = new GButton(mainSidebar, x, y, 100, 20, "Nearest Cluster");
		btnMoveToNearestCluster.tag = "NearestCluster";
		btnMoveToNearestCluster.setLocalColorScheme(5);
		btnMoveToNearestCluster = new GButton(mainSidebar, x+=105, y, 100, 20, "Last Cluster");
		btnMoveToNearestCluster.tag = "LastCluster";
		btnMoveToNearestCluster.setLocalColorScheme(5);

		x = 15;
		y += 25;

		chkbxMovementTeleport = new GCheckbox(mainSidebar, x, y, 70, 20, "Teleport");
		chkbxMovementTeleport.tag = "MovementTeleport";
		chkbxMovementTeleport.setLocalColorScheme(10);
		btnJumpToRandomCluster = new GButton(mainSidebar, x+75, y, 110, 20, "Random Cluster");
		btnJumpToRandomCluster.tag = "RandomCluster";
		btnJumpToRandomCluster.setLocalColorScheme(5);

		x = 90;
		y += 25;

		btnPreviousTimeSegment = new GButton(mainSidebar, x, y, 100, 20, "Previous Time");
		btnPreviousTimeSegment.tag = "PreviousTime";
		btnPreviousTimeSegment.setLocalColorScheme(5);
		btnNextTimeSegment = new GButton(mainSidebar, x+=105, y, 100, 20, "Next Time");
		btnNextTimeSegment.tag = "NextTime";
		btnNextTimeSegment.setLocalColorScheme(5);

		x = 50;
		y += 30;

		btnImportGPSTrack = new GButton(mainSidebar, x, y, 140, 20, "Import GPS Track");
		btnImportGPSTrack.tag = "ImportGPSTrack";
		btnImportGPSTrack.setLocalColorScheme(5);		

		x = 50;
		y += 25;
		
		btnSaveLocation = new GButton(mainSidebar, x, y, 100, 20, "Save Location");
		btnSaveLocation.tag = "SaveLocation";
		btnSaveLocation.setLocalColorScheme(5);
		btnClearMemory = new GButton(mainSidebar, x+100, y, 100, 20, "Clear Memory");
		btnClearMemory.tag = "ClearMemory";
		btnClearMemory.setLocalColorScheme(0);

		
		x = 0;
		y += 30;
		lblPathNavigation = new GLabel(mainSidebar, x, y, mainSidebar.width, 20, "Path Navigation");
		lblPathNavigation.setLocalColorScheme(10);
		lblPathNavigation.setTextAlign(GAlign.CENTER, null);
		lblPathNavigation.setTextBold();
		lblPathNavigation.setTextItalic();

		x = 15;
		y += 25;
		
		optTimeline = new GOption(mainSidebar, x, y, 90, 20, "Timeline");
		optTimeline.setLocalColorScheme(10);
		optTimeline.tag = "FollowTimeline";
		optTimeline.setSelected(true);
		optGPSTrack = new GOption(mainSidebar, x+=90, y, 90, 20, "GPS Track");
		optGPSTrack.setLocalColorScheme(10);
		optGPSTrack.tag = "FollowGPSTrack";
		optMemory = new GOption(mainSidebar, x+=90, y, 90, 20, "Memory");
		optMemory.setLocalColorScheme(10);
		optMemory.tag = "FollowMemory";

		tgFollow = new GToggleGroup();
		tgFollow.addControls(optTimeline, optGPSTrack, optMemory);

		x = 15;
		y += 25;

		chkbxFollowTeleport = new GCheckbox(mainSidebar, x, y, 80, 20, "Teleporting");
		chkbxFollowTeleport.tag = "FollowTeleport";
		chkbxFollowTeleport.setLocalColorScheme(10);

		btnFollowStart = new GButton(mainSidebar, x+=90, y, 60, 20, "Start");
		btnFollowStart.tag = "FollowStart";
		btnFollowStart.setLocalColorScheme(5);
		
		btnFollowStop = new GButton(mainSidebar, x+=60, y, 60, 20, "Stop");
		btnFollowStop.tag = "FollowStop";
		btnFollowStop.setLocalColorScheme(0);

		x = 0;
		y += 40;

		lblGraphics = new GLabel(mainSidebar, x, y, mainSidebar.width, 20, "Graphics");
		lblGraphics.setLocalColorScheme(10);
		lblGraphics.setTextAlign(GAlign.CENTER, null);
		lblGraphics.setTextBold();
		lblGraphics.setTextItalic();
	
		x = 85;
		y += 25;

		chkbxOrientationMode = new GCheckbox(mainSidebar, x, y, 115, 20, "Orientation Mode");
		chkbxOrientationMode.tag = "OrientationMode";
		chkbxOrientationMode.setLocalColorScheme(10);
		chkbxOrientationMode.setSelected(false);

		x = 50;
		y += 25;

		chkbxAlphaMode = new GCheckbox(mainSidebar, x, y, 85, 20, "Alpha Mode");
		chkbxAlphaMode.tag = "AlphaMode";
		chkbxAlphaMode.setLocalColorScheme(10);
		chkbxAlphaMode.setSelected(false);

		chkbxAngleFading = new GCheckbox(mainSidebar, x+=85, y, 100, 20, "Angle Fading");
		chkbxAngleFading.tag = "AngleFading";
		chkbxAngleFading.setLocalColorScheme(10);
		chkbxAngleFading.setSelected(true);

		x = 50;
		y += 25;

		chkbxFadeEdges = new GCheckbox(mainSidebar, x, y, 85, 20, "Fade Edges");
		chkbxFadeEdges.tag = "FadeEdges";
		chkbxFadeEdges.setLocalColorScheme(10);
		chkbxFadeEdges.setSelected(true);

		chkbxAngleThinning = new GCheckbox(mainSidebar, x += 85, y, 100, 20, "Angle Thinning");
		chkbxAngleThinning.tag = "AngleThinning";
		chkbxAngleThinning.setLocalColorScheme(10);
		chkbxAngleThinning.setSelected(false);

		x = 60;
		y += 30;

		btnZoomIn = new GButton(mainSidebar, x, y, 80, 20, "Zoom In");
		btnZoomIn.tag = "ZoomIn";
		btnZoomIn.setLocalColorScheme(5);
		btnZoomOut = new GButton(mainSidebar, x+=90, y, 80, 20, "Zoom Out");
		btnZoomOut.tag = "ZoomOut";
		btnZoomOut.setLocalColorScheme(5);

		x = 8;
		y += 30;

		chkbxHideImages = new GCheckbox(mainSidebar, x, y, 90, 20, "Hide Images");
		chkbxHideImages.tag = "HideImages";
		chkbxHideImages.setLocalColorScheme(10);
		chkbxHideImages.setSelected(false);

		chkbxHideVideos = new GCheckbox(mainSidebar, x += 90, y, 85, 20, "Hide Videos");
		chkbxHideVideos.tag = "HideVideos";
		chkbxHideVideos.setLocalColorScheme(10);
		chkbxHideVideos.setSelected(false);

		chkbxHidePanoramas = new GCheckbox(mainSidebar, x += 85, y, 120, 20, "Hide Panoramas");
		chkbxHidePanoramas.tag = "HidePanoramas";
		chkbxHidePanoramas.setLocalColorScheme(10);
		chkbxHidePanoramas.setSelected(false);

//		lblMediaSize= new GLabel(window, x, y, 80, 20, "Media Size");
//		lblMediaSize.setLocalColorScheme(10);
//		sdrMediaSize = new GSlider(window, x + 115, y-30, 150, 80, 12);
//		sdrMediaSize.setLocalColorScheme(5);
//		sdrMediaSize.setLimits(p.p.defaultFocusDistance, 2.f, 40.f);
//		sdrMediaSize.setRotation(0);
//		sdrMediaSize.setTextOrientation(G4P.ORIENT_RIGHT);
//		sdrMediaSize.setEasing(10);
//		sdrMediaSize.setShowValue(true);
//		sdrMediaSize.tag = "MediaSize";

//		private GCheckbox chkbxAngleFading, chkbxAngleThinning;
//
//		private GSlider sdrMediaSize, sdrMediaBrightness, sdrSubjectDistance;
//		private GSlider sdrMediaLength;

		x = 60;
		y += 30;

		lblAlpha= new GLabel(mainSidebar, x, y, 60, 20, "Alpha");
		lblAlpha.setLocalColorScheme(10);
		lblMediaLength = new GLabel(mainSidebar, x+100, y, 80, 20, "Media Length");
		lblMediaLength .setLocalColorScheme(10);

		x = 40;
		y += 80;

		sdrAlpha = new GSlider(mainSidebar, x, y, 60, 90, 20);
		sdrAlpha.setLocalColorScheme(5);
		sdrAlpha.setLimits(p.p.alpha, 0.f, 255.f);
		sdrAlpha.setRotation(-PApplet.PI/2.f);
		sdrAlpha.setTextOrientation(G4P.ORIENT_RIGHT);
		sdrAlpha.setEasing(10);
		sdrAlpha.setShowValue(true);
		sdrAlpha.tag = "Alpha";

		sdrMediaLength = new GSlider(mainSidebar, x + 100, y, 60, 90, 20);
		sdrMediaLength.setLocalColorScheme(5);
		sdrMediaLength.setLimits(p.p.defaultMediaLength, 10, 250);
		sdrMediaLength.setRotation(-PApplet.PI/2.f);
		sdrMediaLength.setTextOrientation(G4P.ORIENT_RIGHT);
		sdrMediaLength.setEasing(10);
		sdrMediaLength.setShowValue(true);
		sdrMediaLength.tag = "MediaLength";

		x = 0;
		y += 30;
		
		lblModel = new GLabel(mainSidebar, x, y, mainSidebar.width, 20, "Model");
		lblModel.setLocalColorScheme(10);
		lblModel.setTextAlign(GAlign.CENTER, null);
		lblModel.setTextBold();
		lblModel.setTextItalic();

		x = 60;
		y += 30;
		
		btnSubjectDistanceDown = new GButton(mainSidebar, x, y, 30, 20, "-");
		btnSubjectDistanceDown.tag = "SubjectDistanceDown";
		btnSubjectDistanceDown.setLocalColorScheme(5);
		lblSubjectDistance = new GLabel(mainSidebar, x += 35, y, 110, 20, "Subject Distance");
		lblSubjectDistance.setLocalColorScheme(10);
		lblSubjectDistance.setTextBold();
		btnSubjectDistanceUp = new GButton(mainSidebar, x += 105, y, 30, 20, "+");
		btnSubjectDistanceUp.tag = "SubjectDistanceUp";
		btnSubjectDistanceUp.setLocalColorScheme(5);
		
		x = 0;
		y += 30;

		lblOutput = new GLabel(mainSidebar, x, y, mainSidebar.width, 20, "Output");
		lblOutput.setLocalColorScheme(10);
		lblOutput.setTextAlign(GAlign.CENTER, null);
		lblOutput.setTextBold();
		lblOutput.setTextItalic();

		x = 40;
		y += 30;

		btnSaveImage = new GButton(mainSidebar, x, y, 100, 20, "Export Image");
		btnSaveImage.tag = "ExportImage";
		btnSaveImage.setLocalColorScheme(5);
		btnOutputFolder = new GButton(mainSidebar, x+100, y, 120, 20, "Set Output Folder");
		btnOutputFolder.tag = "OutputFolder";
		btnOutputFolder.setLocalColorScheme(5);
		
		x = 85;
		y += 40;

		btnSelectionMode = new GButton(mainSidebar, x, y, 130, 20, "Selection Mode");
		btnSelectionMode.tag = "SelectionMode";
		btnSelectionMode.setLocalColorScheme(5);

		y += 40;

		btnStatisticsView = new GButton(mainSidebar, x, y, 130, 20, "View Statistics");
		btnStatisticsView.tag = "StatisticsView";
		btnStatisticsView.setLocalColorScheme(5);

		x = 70;
		y += 30;
		
		btnRestart = new GButton(mainSidebar, x, y, 160, 20, "Load Media Library...");
		btnRestart.tag = "Restart";
		btnRestart.setLocalColorScheme(5);

		if(p.p.getFields() != null)
		{
			x = 60;
			y += 30;
			btnGoToPreviousField = new GButton(mainSidebar, x, y, 90, 20, "Previous Field");
			btnGoToPreviousField.tag = "PreviousField";
			btnGoToPreviousField.setLocalColorScheme(5);

			btnGoToNextField = new GButton(mainSidebar, x+=90, y, 90, 20, "Next Field");
			btnGoToNextField.tag = "NextField";
			btnGoToNextField.setLocalColorScheme(5);
		}

		x = 0;
		y = mainSidebar.height - 60;
		lblDisplayMode = new GLabel(mainSidebar, x, y, mainSidebar.width, 20);						/* Display Mode Label */
		lblDisplayMode.setText("Display Mode");
		lblDisplayMode.setLocalColorScheme(10);
		lblDisplayMode.setTextAlign(GAlign.CENTER, null);
		lblDisplayMode.setTextBold();
		lblDisplayMode.setTextItalic();
		
		x = 15;
		btnSceneView = new GButton(mainSidebar, x, y+=24, 55, 20, "Scene");			/* Display Mode Buttons */
		btnSceneView.tag = "Scene";
		btnSceneView.setLocalColorScheme(5);
		btnMapView = new GButton(mainSidebar, x+=55, y, 55, 20, "Map");
		btnMapView.tag = "Map";
		btnMapView.setLocalColorScheme(5);
		btnInfoView = new GButton(mainSidebar, x+=55, y, 55, 20, "Info");
		btnInfoView.tag = "Info";
		btnInfoView.setLocalColorScheme(5);
		btnClusterView = new GButton(mainSidebar, x+=55, y, 55, 20, "Cluster");
		btnClusterView.tag = "Cluster";
		btnClusterView.setLocalColorScheme(5);
		btnControlView = new GButton(mainSidebar, x+=55, y, 55, 20, "Control");
		btnControlView.tag = "Control";
		btnControlView.setLocalColorScheme(5);
		
		mainSidebar.addKeyHandler(p.p.p, "sidebarKey");
	}
	
	void setupStatisticsSidebar()
	{
		statisticsSidebar = GWindow.getWindow(p.p.p, "Statistics", 10, 45, sidebarWidth, p.p.p.height, PApplet.JAVA2D);
		statisticsSidebar.addData(new WMV_WinData());
		statisticsSidebar.addDrawHandler(this, "statisticsSidebarDraw");
		statisticsSidebar.addMouseHandler(this, "statisticsSidebarMouse");
		
		int x = 0, y = 20;

		/* Selection Window */
		lblStatistics = new GLabel(statisticsSidebar, x, y, statisticsSidebar.width, 20, "Statistics");
		lblStatistics.setLocalColorScheme(10);
		lblStatistics.setTextAlign(GAlign.CENTER, null);
		lblStatistics.setTextBold();
		lblStatistics.setTextItalic();

		x = 40;
		y = p.p.p.height - 40;
		
		btnExitStatisticsMode = new GButton(statisticsSidebar, x, y, 180, 20, "Exit Statistics View");
		btnExitStatisticsMode.tag = "ExitStatisticsMode";
		btnExitStatisticsMode.setLocalColorScheme(0);
		
		statisticsSidebar.addKeyHandler(p.p.p, "statisticsSidebarKey");
	}

	void setupSelectionSidebar()
	{
		selectionSidebar = GWindow.getWindow(p.p.p, "Selection Mode", 10, 45, sidebarWidth, p.p.p.height, PApplet.JAVA2D);
		selectionSidebar.addData(new WMV_WinData());
		selectionSidebar.addDrawHandler(this, "selectionSidebarDraw");
		selectionSidebar.addMouseHandler(this, "selectionSidebarMouse");
		
		int x = 0, y = 20;

		/* Selection Window */
		lblSelection = new GLabel(selectionSidebar, x, y, selectionSidebar.width, 20, "Selection");
		lblSelection.setLocalColorScheme(10);
		lblSelection.setTextAlign(GAlign.CENTER, null);
		lblSelection.setTextBold();
		lblSelection.setTextItalic();

		x = 40;
		y += 25;

		btnSelectFront = new GButton(selectionSidebar, x, y, 180, 20, "Select Media in Front");
		btnSelectFront.tag = "SelectFront";
		btnSelectFront.setLocalColorScheme(5);

		x = 60;
		y += 25;

		btnSelectFront = new GButton(selectionSidebar, x, y, 180, 20, "Deselect All Media");
		btnSelectFront.tag = "DeselectAll";
		btnSelectFront.setLocalColorScheme(5);


		x = 30;
		y += 25;
		
		chkbxMultiSelection = new GCheckbox(selectionSidebar, x, y, 110, 20, "Multi-Selection");
		chkbxMultiSelection.tag = "MultiSelection";
		chkbxMultiSelection.setLocalColorScheme(10);
		chkbxSelectGroups = new GCheckbox(selectionSidebar, x+=110, y, 100, 20, "Select Segments");
		chkbxSelectGroups.tag = "SelectSegments";
		chkbxSelectGroups.setLocalColorScheme(10);
		
		x = 50;
		y += 30;
		
		chkbxViewMetadata = new GCheckbox(selectionSidebar, x, y, 120, 20, "View Metadata");
		chkbxViewMetadata.tag = "ViewMetadata";
		chkbxViewMetadata.setLocalColorScheme(10);
		
		x = 40;
		y += 25;
		
		btnExitSelectionMode = new GButton(selectionSidebar, x, y, 180, 20, "Exit Selection Mode");
		btnExitSelectionMode.tag = "ExitSelectionMode";
		btnExitSelectionMode.setLocalColorScheme(0);
		
		selectionSidebar.addKeyHandler(p.p.p, "selectionSidebarKey");
	}

	/**
	 * Handles drawing to the windows PApplet area
	 * 
	 * @param applet the PApplet object embeded into the frame
	 * @param data the data for the GWindow being used
	 */
	public void sidebarDraw(PApplet applet, GWinData data) {
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
	public void sidebarMouse(PApplet applet, GWinData data, MouseEvent event) {
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
//			p.p.text(" MediaWorldViewer v1.0 by David Gordon, Copyright © 2016", xPos, yPos += lineWidthVeryWide);

		}
		else
			p.message("Can't display statistics: currentCluster == "+p.p.viewer.getCurrentCluster()+"!!!");
		
	}

	/**
	 * Handles drawing to the windows PApplet area
	 * @param applet the PApplet object embeded into the frame
	 * @param data the data for the GWindow being used
	 */
	public void selectionSidebarDraw(PApplet applet, GWinData data) {
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
	public void selectionSidebarMouse(PApplet applet, GWinData data, MouseEvent event) {
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
	 * Handles mouse events for ALL GWindow objects
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