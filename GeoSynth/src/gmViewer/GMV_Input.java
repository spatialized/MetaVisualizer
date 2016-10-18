package gmViewer;
import processing.core.*;

/**************************************
 * GMV_Input
 * @author davidgordon
 * Methods for responding to user input from keyboard or mouse
 */

public class GMV_Input
{
	public boolean shiftKey = false;
	public boolean optionKey = false;
	int mouseClickedX = 0, mouseClickedY = 0;
	int mouseOffsetX = 0, mouseOffsetY = 0;
	boolean clickedRecently = false;
	int clickedRecentlyFrame = 1000000;
	int doubleClickSpeed = 10;

	GeoSynth p;

	GMV_Input(GeoSynth parent) {
		p = parent;
	}

	/**
	 * handleKeyPressed()
	 * @param key Key that was pressed
	 * Respond to user key presses
	 */
	void handleKeyPressed(char key)
	{
		/* General */
		if (key == ' ') 
		{
//			if(!p.interactive)
//				p.pause = !p.pause;
			if(p.interactive)
			{
				p.finishInteractiveClustering();			// Restart simulation after interative clustering
			}
		}
		
		/* Display Modes */
		if (!optionKey && key == '1') 
		{
			boolean state = p.display.map;
			p.display.resetDisplayModes();
			p.display.map = !state;
		}
		
		if (!optionKey && key == '!') 
		{
			boolean state = p.display.mapOverlay;
			p.display.resetDisplayModes();
			p.display.mapOverlay = !state;
		}
		
		if (!optionKey && key == '2') 
		{
			boolean state = p.display.info;
			p.display.resetDisplayModes();
			p.display.info = !state;
		}
		
		if (!optionKey && key == '@') 
		{
			boolean state = p.display.infoOverlay;
			p.display.resetDisplayModes();
			p.display.infoOverlay = !state;
		}

		if (!optionKey && key == '3') 
		{
			boolean state = p.display.cluster;
			p.display.resetDisplayModes();
			p.display.cluster = !state;
		}
		
		if (!optionKey && key == '#') 
		{
			boolean state = p.display.clusterOverlay;
			p.display.resetDisplayModes();
			p.display.clusterOverlay = !state;
		}
		
		if (!optionKey && key == '4') 
		{
			boolean state = p.display.control;
			p.display.resetDisplayModes();
			p.display.control = !state;
		}
		
		if (!optionKey && key == '$') 
		{
			boolean state = p.display.controlOverlay;
			p.display.resetDisplayModes();
			p.display.controlOverlay = !state;
		}
		
		if (!optionKey && key == '5') 
			p.showMediaToCluster = !p.showMediaToCluster;			// Draw line from each media point to cluster
		
		if (!optionKey && key == '6') 
			p.showCaptureToMedia = !p.showCaptureToMedia;			// Draw line from each media point to its capture location
		
		if (!optionKey && key == '7') 
			p.showCaptureToCluster = !p.showCaptureToCluster;		// Draw line from each media capture location to associated cluster

		if (key == '-')
			p.showModel = !p.showModel;
		
		/* Clustering */
		if (key == 'r')
			p.startInteractiveClustering();

		if (key == 'C')
			p.startInitialClustering();				// Re-run clustering on all fields
		
		if(p.display.map || p.display.mapOverlay)	/* 2D Map View */
		{
			// Option Key
			if (optionKey && key == '1') 
				p.display.mapMode = 1;

			if (optionKey && key == '2') 
				p.display.mapMode = 2;

			if (optionKey && key == '3') 
				p.display.mapMode = 3;

			if (optionKey && key == '4') 
				p.display.mapMode = 4;

			if (optionKey && key == '5') 
				p.display.mapMode = 5;

			if (optionKey && key == '6') 
				p.display.mapMode = 6;

			if (optionKey && key == '7') 
				p.display.mapMode = 7;

			// Option + Shift Keys
			if (optionKey && key == '!') 
				p.display.mapImages = !p.display.mapImages;

			if (optionKey && key == '@') 
				p.display.mapPanoramas = !p.display.mapPanoramas;

			if (optionKey && key == '#') 
				p.display.mapVideos = !p.display.mapVideos;

			if (key == ']') {
				p.display.mapZoom *= 1.02f;
			}

			if (key == '[') {
				p.display.mapZoom *= 0.985f;
			}

			if (key == PApplet.CODED) 					
			{
				if (p.keyCode == PApplet.LEFT) 
					p.viewer.rotateX(-1);

				if (p.keyCode == PApplet.RIGHT) 
					p.viewer.rotateX(1);

				if (shiftKey && p.keyCode == PApplet.LEFT) 
					p.display.mapLeftEdge -= 10.f;

				if (shiftKey && p.keyCode == PApplet.RIGHT) 
					p.display.mapLeftEdge += 10.f;

				if (shiftKey && p.keyCode == PApplet.DOWN) 
					p.display.mapTopEdge += 10.f;

				if (shiftKey && p.keyCode == PApplet.UP) 
					p.display.mapTopEdge -= 10.f;
			}
		}
		else if(p.display.info || p.display.infoOverlay)		/* Info View */
		{
		
		}
		else if(p.display.cluster || p.display.clusterOverlay)		/* Cluster View */
		{
			if (key == PApplet.CODED) 					
			{
				if (p.keyCode == PApplet.LEFT) 
				{
					p.display.displayCluster--;
					if(p.display.displayCluster < 0)
						p.display.displayCluster = p.getFieldClusters().size() - 1;

					int count = 0;
					while(p.getCluster(p.display.displayCluster).isEmpty())
					{
						p.display.displayCluster--;
						count++;
						if(p.display.displayCluster < 0)
							p.display.displayCluster = p.getFieldClusters().size() - 1;
						
						if(count > p.getFieldClusters().size())
							break;
					}
				}

				if (p.keyCode == PApplet.RIGHT) 
				{
					p.display.displayCluster++;
					if( p.display.displayCluster >= p.getFieldClusters().size())
						p.display.displayCluster = 0;

					int count = 0;
					while(p.getCluster(p.display.displayCluster).isEmpty())
					{
						p.display.displayCluster++;
						count++;
						if( p.display.displayCluster >= p.getFieldClusters().size())
							p.display.displayCluster = 0;
						
						if(count > p.getFieldClusters().size())
							break;
					}
				}
			}
		}
		else if(p.display.control || p.display.controlOverlay)		/* Controls View */
		{

		}
		
		if (!p.interactive)		
		{
			/* 3D View Controls */
			if (key == '|')
				p.getCurrentCluster().stitchImages();    			
			
			if (optionKey && key == '[')
			{
				if(p.thinningAngle > PApplet.PI / 64.f)
					p.thinningAngle -= PApplet.PI / 128.f;
//				p.display.message("Set thinningAngle:"+p.thinningAngle);
				p.getCurrentField().model.analyzeClusterMediaDirections();
			}

			if (optionKey && key == ']')
			{
				if(p.thinningAngle < p.visibleAngle - PApplet.PI / 128.f)
					p.thinningAngle += PApplet.PI / 128.f;
//				p.display.message("Set thinningAngle:"+p.thinningAngle);
				p.getCurrentField().model.analyzeClusterMediaDirections();
			}

			if (optionKey && key == '\\')
				p.getCurrentField().stitchAllClusters();		// Teleport to cluster with > 1 times
			
			if (!optionKey && key == 'a') 
				p.viewer.startMoveXTransition(-1);

			if (!optionKey && key == 'd') 
				p.viewer.startMoveXTransition(1);

			if (!optionKey && key == 's') 
				p.viewer.startMoveZTransition(1);

			if (!optionKey && key == 'w') 
				p.viewer.walkForward();

			if (optionKey && key == 'm') 
				p.showMetadata = !p.showMetadata;

			if (key == 'Q')
				p.viewer.moveToNextCluster(false, -1);

			if (key == 'e')									// Move UP
				p.viewer.startMoveYTransition(-1);

			if (key == 'c') 									// Move DOWN
				p.viewer.startMoveYTransition(1);

			if (key == 'A') 									// Move to next location 
				p.viewer.moveToNextLocation();

			if (key == 'j') 
				p.viewer.moveToRandomCluster(true);					// Move to random cluster

			if (key == 'J') 
				p.viewer.moveToRandomCluster(false);				// Jump (teleport) to random cluster

			if (key == 'W') 
				p.viewer.moveToNearestClusterAhead(false);
			
			if (!optionKey && key == ']') {
				float value = p.altitudeAdjustmentFactor * 1.052f;
				p.altitudeAdjustmentFactor = PApplet.constrain(value, 0.f, 1.f);
				p.getCurrentField().calculateMediaLocations();		// Recalculate media locations
				p.getCurrentField().createClusters();				// Recalculate cluster locations
			}

			if (!optionKey && key == '[') {
				float value = p.altitudeAdjustmentFactor *= 0.95f;
				p.altitudeAdjustmentFactor = PApplet.constrain(value, 0.f, 1.f);
				p.getCurrentField().calculateMediaLocations();		// Recalculate media locations
				p.getCurrentField().createClusters();				// Recalculate cluster locations
			}
			
			if(optionKey)
			{
				if (key == 'f') 
					p.viewer.moveToTimeInField(p.getCurrentField().fieldID, 0, true);

				if (key == 'n')						// Teleport to current time segment
					p.viewer.moveToNextTimeSegment(true, true);

				if (key == 'b')						// Teleport to current time segment
					p.viewer.moveToPreviousTimeSegment(true, true);

				if (key == 'N')						// Teleport to next cluster time segment
					p.viewer.moveToNextTimeSegment(false, true);

				if (key == 'B')						// Teleport to previous cluster time segment
					p.viewer.moveToPreviousTimeSegment(false, true);
			}
			else
			{
				if (key == 'f')  //key == 'F') 
					p.viewer.moveToTimeInField(p.getCurrentField().fieldID, 0, false);

				if (key == 'n')						// Move to current time segment
					p.viewer.moveToNextTimeSegment(true, false);

				if (key == 'b')						// Move to current time segment
					p.viewer.moveToPreviousTimeSegment(true, false);

				if (key == 'N')						// Move to next cluster time segment
					p.viewer.moveToNextTimeSegment(false, false);

				if (key == 'B')						// Move to previous cluster time segment
					p.viewer.moveToPreviousTimeSegment(false, false);
			}
			
			if (key == 'y')
				p.viewer.followMemory();

			if (key == '>')
			{
				if(!p.viewer.isFollowing())
					p.viewer.followTimeline(true, false);
				else
					p.viewer.followTimeline(false, false);
			}

			if (key == 'u') 		// Teleport to nearest cluster with video
				p.viewer.moveToNextCluster(true, 2);

			if (key == 'U') 		// Go to nearest cluster with video
				p.viewer.moveToNextCluster(false, 2);

			if (key == 'm') 		// Teleport to nearest cluster with panorama
				p.viewer.moveToNextCluster(true, 1);

			if (key == 'M') 		// Go to nearest cluster with panorama
				p.viewer.moveToNextCluster(false, 1);

			if (key == '{')
				p.viewer.teleportToField(-1);

			if (key == '}') 
				p.viewer.teleportToField(1);

			if (key == 'E') 
				p.viewer.moveToNearestCluster(false);

			if (key == ',') 
			{
				p.getCurrentField().fadeObjectDistances(0.85f);
			}
			
			if (key == '.')
			{
				p.getCurrentField().fadeObjectDistances(1.176f);
			}

			if (key == 'q') 
				p.viewer.startZoomTransition(-1);

			if (key == 'z') 
				p.viewer.startZoomTransition(1);

			/* 3D Controls disabled in HUD view */
			if(!p.display.inDisplayView())							
			{
				if (key == 'D')
				{
					p.blurEdges = !p.blurEdges;
					PApplet.println("p.blurEdges:"+p.blurEdges);
				}

				if (key == 'h')	
					p.debug.hideImages = !p.debug.hideImages;

				if (key == 'H')	
					p.debug.hidePanoramas = !p.debug.hidePanoramas;

				if (key == 'v')	
					p.debug.hideVideos = !p.debug.hideVideos;

				if (key == 'P')
					p.alphaMode = !p.alphaMode;

				if (key == ' ') 
					p.timeFading = !p.timeFading;

				if (key == ')') {
					p.display.changeBlendMode(1);
				}

				if (key == '(') {
					p.display.changeBlendMode(-1);
				}

				if (key == ':')
				{
					p.showUserPanoramas = !p.showUserPanoramas;
					PApplet.println("showUserPanoramas:"+p.showUserPanoramas);
				}

				if (key == ';')
				{
					p.showStitchedPanoramas = !p.showStitchedPanoramas;
					PApplet.println("showStitchedPanoramas:"+p.showStitchedPanoramas);
				}

				if (key == 'O') 
				{
					p.viewer.selection = !p.viewer.selection;
					if(p.viewer.selection && p.viewer.multiSelection) p.viewer.multiSelection = false;
					if(p.viewer.selection && p.viewer.segmentSelection) p.viewer.segmentSelection = false;
				}
				
				if (optionKey && key == 'd')
					p.getCurrentField().deselectAllMedia();

//				if (key == 'D')
//				{
//					if(p.viewer.selectionMode)
//						p.viewer.selectionMode = false;
//					p.viewer.videoMode = !p.viewer.videoMode;
//				}

				if (key == '_')
					p.visibleAngle -= 3.1415f / 128.f; 

				if (key == '+')
					p.visibleAngle += 3.1415f / 128.f; 

				/* Selection */
				if (key == 'x') 
					p.viewer.selectFrontMedia(true);

				if (key == 'X')
					p.viewer.selectFrontMedia(false);
				
				if (key == 'S')
				{
					p.viewer.multiSelection = !p.viewer.multiSelection;
					if(p.viewer.multiSelection && !p.viewer.selection)
						p.viewer.selection = true;
				}

				if (optionKey && key == 's')
				{
					p.viewer.segmentSelection = !p.viewer.segmentSelection;
					if(p.viewer.segmentSelection && !p.viewer.selection)
						p.viewer.selection = true;
				}

				/* GPS */
				if (key == 'g') 
					p.viewer.importGPSTrack();				// Select a GPS tracking file from disk to load and navigate 

				/* Memory */
				if (key == '`') 
					p.viewer.addPlaceToMemory();

				if (key == 'Y') 
					p.viewer.clearMemory();

				/* Graphics */
				if (key == 'G')
				{
					p.angleFading = !p.angleFading;
//					p.angleHidingMode = p.angleFading;
				}
				
				if (key == 'T')
				{
					p.angleThinning = !p.angleThinning;
//					p.display.message("p.angleThinning:"+p.angleThinning);
				}

				if (key == '9') {
//					p.defaultImageSize += 0.1f;
//					PApplet.println("p.defaultImageSize:"+p.defaultImageSize);
				}

				if (key == '0') {
//					if(p.defaultImageSize > 2)
//						p.defaultImageSize -= 0.1f;
//					PApplet.println("p.defaultImageSize:"+p.defaultImageSize);
				}

				/* Output */
				if (key == 'o') 
					p.selectFolder("Select an output folder:", "outputFolderSelected");

				if (key == 'p') 	// Save image to disk
				{	
					if(!p.outputFolderSelected) p.selectFolder("Select an output folder:", "outputFolderSelected");
					p.saveImage();
				}

				/* Navigation */
				if (key == 'k') 
					p.viewer.lockToCluster = !p.viewer.lockToCluster;

//				if(key == 'I')
//					p.viewer.autoNavigation = !p.viewer.autoNavigation;

//				if (key == 't')		// Move and turn to face selected image
//				{
//					if(p.getCurrentField().selectedImage != -1)
//						p.viewer.moveToCaptureLocation(p.getCurrentField().selectedImage, true);
//					else
//						p.display.message("No selected image!");
//				}
				
//				if (key == 'l') 	// Turn towards selected image
//					p.viewer.turnTowardsSelected();

				if (key == 'L') 			// Look for images when none are visible
					p.viewer.lookForImages();
			}
		}
		else 						// Interactive Clustering Mode
		{
			if (!optionKey && key == 'h')
			{
				if(!p.hierarchical)
				{
					p.hierarchical = true;
					if(!p.getCurrentField().model.dendrogramCreated)
						p.getCurrentField().model.runHierarchicalClustering();
					p.getCurrentField().model.setDendrogramDepth(p.getCurrentField().model.clusterDepth);				// Initialize clusters 
					p.getCurrentField().createTimeline();					// Create field timeline
				}

			}
			
			if (!optionKey && key == 'k')
			{
				if(p.hierarchical)
				{
					p.hierarchical = false;
					GMV_Model m = p.getCurrentField().model;
					m.runKMeansClustering(p.kMeansClusteringEpsilon, m.clusterRefinement, m.clusterPopulationFactor);
					p.getCurrentField().createTimeline();					// Create field timeline
				}
			}
			
			if (key == '[') 	
			{
				if(!p.autoClusterDistances && p.minClusterDistance > 0.25f)
				{
					p.minClusterDistance -= 0.25f;
//					PApplet.println("p.minClusterDistance:"+p.minClusterDistance);
					for(GMV_Field f : p.getFields())
					{
						f.model.setMinClusterDistance(p.minClusterDistance);	
						f.model.clustersNeedUpdate = true;
					}
				}
			}
			
			if (key == ']') 	
			{
				if(!p.autoClusterDistances && p.minClusterDistance < p.maxClusterDistance - 2.f)
				{
					p.minClusterDistance += 0.25f;
//					PApplet.println("p.minClusterDistance:"+p.minClusterDistance);
					for(GMV_Field f : p.getFields())
					{
						f.model.setMinClusterDistance(p.minClusterDistance);
						f.model.clustersNeedUpdate = true;					// -- Temporary: will make minClusterDistance field specific...
					}
				}
			}
		}
		
		/* Arrow and Shift Keys */
		if (key == PApplet.CODED) 					
		{
			if(!p.display.inDisplayView())
			{
				/* Navigation */
				if (p.keyCode == PApplet.LEFT) 
					p.viewer.rotateX(-1);

				if (p.keyCode == PApplet.RIGHT) 
					p.viewer.rotateX(1);
				
				if (p.keyCode == PApplet.UP) 
					p.viewer.rotateY(-1);
				
				if (p.keyCode == PApplet.DOWN) 
					p.viewer.rotateY(1);
				
				/* Time */
				if (shiftKey && p.keyCode == PApplet.UP) 
					p.incrementTime();

				if (shiftKey && p.keyCode == PApplet.DOWN) 
					p.decrementTime();
				
				if (shiftKey && p.keyCode == PApplet.LEFT) 
					p.decrementCycleLength();

				if (shiftKey && p.keyCode == PApplet.RIGHT) 
					p.incrementCycleLength();
			}
			else
			{
				if(p.interactive)			/* Interactive Clustering */
				{
					if(p.hierarchical)
					{
						if (p.keyCode == PApplet.UP) 
						{
							int clusterDepth = p.getCurrentField().model.clusterDepth + 1;
							if(clusterDepth <= p.getCurrentField().model.deepestLevel)
								p.getCurrentField().model.setDendrogramDepth( clusterDepth );
						}

						if (p.keyCode == PApplet.DOWN) 
						{
							int clusterDepth = p.getCurrentField().model.clusterDepth - 1;
							if(clusterDepth >= p.getCurrentField().model.minClusterDepth)
								p.getCurrentField().model.setDendrogramDepth( clusterDepth );
						}
					}
					else
					{
						if (p.keyCode == PApplet.LEFT) 		
						{
							p.getCurrentField().model.clusterRefinement -= 10;
							float populationFactor = p.getCurrentField().model.clusterPopulationFactor;

							if(p.getCurrentField().model.clusterRefinement >= p.getCurrentField().model.minClusterRefinement)
							{
								p.getCurrentField().model.runKMeansClustering( p.kMeansClusteringEpsilon, p.getCurrentField().model.clusterRefinement, populationFactor );
								p.getCurrentField().initializeClusters();			
								p.display.initializeMaps();
							}
							else p.getCurrentField().model.clusterRefinement += 10;
						}

						if (p.keyCode == PApplet.RIGHT) 	
						{
							p.getCurrentField().model.clusterRefinement += 10;
							float populationFactor = p.getCurrentField().model.clusterPopulationFactor;

							if(p.getCurrentField().model.clusterRefinement <= p.getCurrentField().model.maxClusterRefinement)
							{
								p.getCurrentField().model.runKMeansClustering( p.kMeansClusteringEpsilon, p.getCurrentField().model.clusterRefinement, populationFactor );
								p.getCurrentField().initializeClusters();			
								p.display.initializeMaps();
							}
							else p.getCurrentField().model.clusterRefinement -= 10;
						}

						if (p.keyCode == PApplet.DOWN) 		
						{
							int refinementAmount = p.getCurrentField().model.clusterRefinement;
							p.getCurrentField().model.clusterPopulationFactor -= 1.f;

							if(p.getCurrentField().model.clusterPopulationFactor >= p.getCurrentField().model.minPopulationFactor)
							{
								p.getCurrentField().model.runKMeansClustering( p.kMeansClusteringEpsilon, refinementAmount, p.getCurrentField().model.clusterPopulationFactor );
								p.getCurrentField().initializeClusters();			
								p.display.initializeMaps();
							}
							else p.getCurrentField().model.clusterPopulationFactor += 1.f;
						}

						if (p.keyCode == PApplet.UP) 	
						{
							int refinementAmount = p.getCurrentField().model.clusterRefinement;
							p.getCurrentField().model.clusterPopulationFactor += 1.f;

							if(p.getCurrentField().model.clusterPopulationFactor <= p.getCurrentField().model.maxPopulationFactor)
							{
								p.getCurrentField().model.runKMeansClustering( p.kMeansClusteringEpsilon, refinementAmount, p.getCurrentField().model.clusterPopulationFactor );
								p.getCurrentField().initializeClusters();			
								p.display.initializeMaps();
							}
							else p.getCurrentField().model.clusterPopulationFactor -= 1.f;
						}
					}
				}
			}
			
			if (p.keyCode == PApplet.SHIFT) {
				shiftKey = true;
			}
			
			if (p.keyCode == PApplet.ALT) 
				optionKey = true;
		}
	}
	
	/**
	 * handleKeyReleased()
	 * Respond to user key releases
	 */
	void handleKeyReleased(char key)
	{
		/* Navigation */
		if (key == 'a') 
			p.viewer.stopMoveXTransition();
		if (key == 'd') 
			p.viewer.stopMoveXTransition();
		if (key == 's') 
			p.viewer.stopMoveZTransition();
		if (key == 'w') 
			p.viewer.stopMoveZTransition();
		if (key == 'e') 
			p.viewer.stopMoveYTransition();
		if (key == 'c') 
			p.viewer.stopMoveYTransition();

		/* Arrow and Shift Keys */
		if (key == PApplet.CODED) {
			if (p.keyCode == PApplet.LEFT) 
				p.viewer.stopRotateXTransition();
			if (p.keyCode == PApplet.RIGHT) 
				p.viewer.stopRotateXTransition();
			if (p.keyCode == PApplet.UP) 
				p.viewer.stopRotateYTransition();
			if (p.keyCode == PApplet.DOWN) 
				p.viewer.stopRotateYTransition();
			if (p.keyCode == PApplet.SHIFT) 
				shiftKey = false;
			if (p.keyCode == PApplet.ALT) 
				optionKey = false;
		}
	}

	/* Mouse */
	void updateMouseNavigation(int mouseX, int mouseY)
	{			
		if(p.frameCount - clickedRecentlyFrame > doubleClickSpeed && clickedRecently)
		{
			clickedRecently = false;
	//			PApplet.println("SET CLICKED RECENTLY TO FALSE");
		}
			
		if (mouseX < p.width * 0.25 && mouseX > -1) {
//				p.navigation.cam.rotateXStartFrame = p.frameCount;
//				p.navigation.cam.rotateXDirection = -1;
//				p.navigation.cam.rotateXTransition = true;
//				p.navigation.lastMovementFrame = p.frameCount;
//				p.navigation.idleMoveCount = 0;
//				p.navigation.idleTime = 0;
		}
		else if (mouseX > p.width * 0.75 && mouseX < p.width + 1) {
//				p.navigation.cam.rotateXStartFrame = p.frameCount;
//				p.navigation.cam.rotateXDirection = 1;
//				p.navigation.cam.rotateXTransition = true;
//				p.navigation.lastMovementFrame = p.frameCount;
		}
		else if (mouseY < p.height * 0.25 && mouseY > -1) {
//				p.navigation.cam.rotateYStartFrame = p.frameCount;
//				p.navigation.cam.rotateYDirection = -1;
//				p.navigation.cam.rotateYTransition = true;
//				p.navigation.lastMovementFrame = p.frameCount;
		}
		else if (mouseY > p.height * 0.75 && mouseY < p.height + 1) {
//				p.navigation.cam.rotateYStartFrame = p.frameCount;
//				p.navigation.cam.rotateYDirection = 1;
//				p.navigation.cam.rotateYTransition = true;
//				p.navigation.lastMovementFrame = p.frameCount;
		}
		else
		{
//				if(p.navigation.cam.rotateXTransition) p.navigation.cam.rotateXTransition = false;
//				if(p.navigation.cam.rotateYTransition) p.navigation.cam.rotateYTransition = false;
		}
	}

	void handleMousePressed(int mouseX, int mouseY)
	{
		boolean doubleClick = false, switchedViews = false;

//			PApplet.println("MousePressed!");
		if(!p.transitionsOnly && p.viewer.lastMovementFrame > 5)
		{
			if(mouseX > p.width * 0.25 && mouseX < p.width * 0.75 && mouseY < p.height * 0.75 && mouseY > p.height * 0.25)
			{
				p.viewer.walkForward();
			}
			else
			{
				mouseClickedX = mouseX;
				mouseClickedY = mouseY;
			}
			p.viewer.lastMovementFrame = p.frameCount;
		}
		else p.viewer.moveToNextCluster(false, -1);

		mouseOffsetX = 0;
		mouseOffsetY = 0;
	}

	void handleMouseReleased(int mouseX, int mouseY)
	{
		p.viewer.walkSlower();
		p.viewer.lastMovementFrame = p.frameCount;

		boolean doubleClick = false, switchedViews = false;

		if(clickedRecently)		// Double click
		{
			doubleClick = true;
		}

		if(doubleClick)			// Single click on map
		{
			p.viewer.moveToNextCluster(false, -1);
		}

	}

	void handleMouseDragged(int mouseX, int mouseY)
	{
		mouseOffsetX = mouseClickedX - mouseX;
		mouseOffsetY = mouseClickedY - mouseY;

		p.viewer.lastMovementFrame = p.frameCount;			// Turn faster if larger offset X or Y?
	}
}