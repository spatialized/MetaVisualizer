package gmViewer;

import java.util.ArrayList;

import damkjer.ocd.Camera;
import processing.core.*;
import processing.data.FloatList;
import toxi.math.ScaleMap;

/***********************************
 * GMV_Display
 * @author davidgordon
 * Class for displaying 2D text and graphics
 */

class GMV_Display
{
	/* Display Modes */
	public boolean map = false;					// Display map only
	public boolean info = false;				// Display simulation info 
	public boolean cluster = false;				// Display cluster statistics
	public boolean control = false;				// Display controls only
	public boolean about = false;				// Display about screen  -- need to implement
	
	public boolean mapOverlay = false;			// Overlay map on 3D view
	public boolean infoOverlay = false;			// Overlay simulation info on 3D view
	public boolean clusterOverlay = false;		// Display cluster statistics over 3D view
	public boolean controlOverlay = false;		// Display controls over 3D view
	
	/* Debug */
	public boolean drawForceVector = false;
	PVector mapVectorOrigin, mapVectorVector;
	
	/* Status */
	public boolean initialSetup = true;
	
	/* Graphics */
	float hudDistance;							// Distance of the Heads-Up Display from the virtual camera -- Change with zoom level??
	public boolean drawGrid = false; 			// Draw 3D grid   			-- Unused

	public int blendMode = 0;							// Alpha blending mode
	public int numBlendModes = 10;						// Number of blending modes

	/* Cluster Display */
	public int displayCluster = 0;

	/* Map Modes */
	int mapMode = 1;			// 	1:  All   2: Clusters + Media   3: Clusters + Capture Locations  4: Capture Locations + Media
								//	5:  Clusters Only   6: Media Only   7: Capture Locations Only
	/* Map */
	public float mapZoom = 1.f;
	public float mapLeftEdge = 0.f, mapTopEdge = 0.f;
	
	public boolean mapImages = true, mapPanoramas = true, mapVideos = true, mapClusters = true;
	private float mapImageHue = 150.f, mapImageCaptureHue = 180.f;
	private float mapPanoramaHue = 190.f, mapPanoramaCaptureHue = 220.f;
	private float mapVideoHue = 40.f, mapVideoCaptureHue = 70.f;
	
	public float mapClusterHue = 112.f;
	private float mapAttractorClusterHue = 222.f;
	
	private float mapCameraHue = 140.f;
	private float mapMediaTransparency = 120.f;
	private float minSaturation = 120.f, maxSaturation = 210.f;

	float largeMapWidth, largeMapHeight, largeMapMaxWidth, largeMapMaxHeight;
	float largeMapXOffset, largeMapYOffset;
	float largeMapZoomLevel = 1.f;
	
	float smallMapWidth, smallMapHeight, smallMapMaxWidth, smallMapMaxHeight;
	float smallMapXOffset, smallMapYOffset;
	float logoXOffset, logoYOffset;

	float smallPointSize;
	float mediumPointSize;
	float largePointSize;
	float hugePointSize;
	float cameraPointSize;
	
	/* Messages */
	ArrayList<String> messages;						// Messages to display on screen
	ArrayList<String> metadata;						// Messages to display on screen
	ArrayList<String> startupMessages;						// Messages to display on screen
	int messageStartFrame = -1;
	int metadataStartFrame = -1;
	int startupMessageStartFrame = -1;
	int messageDuration = 60;
	
	/* Text */
	float centerTextXOffset, topTextYOffset;
	float userMessageXOffset, userMessageYOffset, startupMessageXOffset;
	float leftTextXOffset, rightTextXOffset, metadataYOffset, startupMessageYOffset;
	float midLeftTextXOffset, midRightTextXOffset;

	float largeTextSize = 28.f;
	float mediumTextSize = 22.f;
	float smallTextSize = 18.f;
	float linePadding = 10.f;
	float lineWidth = smallTextSize + linePadding;			
	float lineWidthWide = largeTextSize + linePadding;			
	float lineWidthVeryWide = largeTextSize * 2.f;			

	GeoSynth p;

	GMV_Display(GeoSynth parent)
	{
		p = parent;
		
		hudDistance = p.hudDistance;
		
		messages = new ArrayList<String>();
		metadata = new ArrayList<String>();
		startupMessages = new ArrayList<String>();

		largeMapXOffset = -p.width * 0.5f;
		largeMapYOffset = -p.height * 0.5f;
		largeMapMaxWidth = p.width * 0.95f;
		largeMapMaxHeight = p.height * 0.95f;

		smallMapMaxWidth = p.width / 4.f;
		smallMapMaxHeight = p.height / 4.f;

		logoXOffset = p.width - p.width / 3.f;
		logoYOffset = p.height / 2.5f;
		
		centerTextXOffset = 0;
		leftTextXOffset = -p.width / 2.f;
		midLeftTextXOffset = -p.width / 3.f;
		rightTextXOffset = p.width / 2.f;
		midRightTextXOffset = p.width / 3.f;

		topTextYOffset = -p.height / 1.5f;

		userMessageXOffset = -p.width / 2.f;
		userMessageYOffset = 0;

		metadataYOffset = -p.height / 2.f;

		startupMessageXOffset = p.width / 2;
		startupMessageYOffset = -p.width / 3.f;

		smallPointSize = 0.0000022f * p.width;
		mediumPointSize = 0.0000028f * p.width;
		largePointSize = 0.0000032f * p.width;
		hugePointSize = 0.0000039f * p.width;
		cameraPointSize = 0.004f * p.width;
		
//		rightEdge = p.width / 2.f;
//		leftEdge = -p.width / 2.f;
//		topEdge = p.height / 2.f;
//		bottomEdge = -p.height / 2.f;
	}

	/**
	 * Draw Heads-Up Display elements: messages, interactive map, field statistics, metadata.
	 */
	void draw()
	{
		if(initialSetup)
		{
			p.hint(PApplet.DISABLE_DEPTH_TEST);												// Disable depth testing for drawing HUD
			p.background(0);																// Hide 3D view
			displayStartupMessages();														// Draw startup messages
			progressBar();
		}
		else if( map || control || info || about || cluster || p.interactive )
		{
			p.hint(PApplet.DISABLE_DEPTH_TEST);												// Disable depth testing for drawing HUD
			p.background(0.f);																// Hide 3D view

			if(map)
			{
				drawLargeMap();
				drawTimelines();
			}

			if(info)
				displayStatistics();

			if(cluster)
				displayClusterStats();
			
			if(control)
				displayControls();

//			if(about)

			if(p.interactive)
			{
				displayInteractiveClustering();
			}
		}
		else if( mapOverlay || controlOverlay || infoOverlay || clusterOverlay || messages.size() > 0 || metadata.size() > 0 )
		{
			p.hint(PApplet.DISABLE_DEPTH_TEST);												// Disable depth testing for drawing HUD
			
			if(mapOverlay)
			{
				drawLargeMap();
				drawTimelines();
			}

			if(infoOverlay)
				displayStatistics();

			if(clusterOverlay)
				displayClusterStats();				

			if(controlOverlay)
				displayControls();
			
			if(messages.size() > 0)
				displayMessages();

			if(p.showMetadata && metadata.size() > 0 && p.viewer.selection)	
				displayMetadata();

//			if(aboutOverlay)

			if((map || mapOverlay) && drawForceVector)						// Draw force vector
			{
				drawForceVector();
			}
		}
	}

	/**
	 * Draw (on 2D map) a point given in 3D world coordinates 
	 * @param point Point in 3D world coordinates
	 * @param pointSize Point size
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @param hue Point hue
	 * @param saturation Point saturation
	 * @param brightness Point brightness
	 * @param transparency Point transparency
	 */
	public void drawMapPoint( PVector point, float pointSize, float mapWidth, float mapHeight, float hue, float saturation, float brightness, float transparency )
	{		
		float mapLocX, mapLocY;
		GMV_Model m = p.getCurrentField().model;

		if(!p.utilities.isNaN(point.x) && !p.utilities.isNaN(point.y) && !p.utilities.isNaN(point.z))
		{
			/* Find 2D map coordinates for this image */
			mapLocX = PApplet.map( point.x, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, 0, mapWidth * mapZoom );		
			mapLocY = PApplet.map( point.z, -0.5f * m.fieldLength, 0.5f*m.fieldLength, 0, mapHeight * mapZoom );

			if(mapLocX < mapWidth && mapLocX > 0 && mapLocY < mapHeight && mapLocY > 0)
			{
				p.stroke(hue, saturation, brightness, transparency);
				p.pushMatrix();
				beginHUD();

				p.strokeWeight(pointSize);
				p.translate(mapLeftEdge, mapTopEdge);
				p.point(largeMapXOffset + mapLocX, largeMapYOffset + mapLocY, hudDistance);

				p.popMatrix();
			}
		}
		else message("Map point is NaN!:"+point+" hue:"+hue);
	}
	
	/**
	 * Draw (on 2D map) a point given in 3D world coordinates 
	 * @param point Point in 3D world coordinates
	 * @param pointSize Point size
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @param hue Point hue
	 * @param saturation Point saturation
	 * @param brightness Point brightness
	 * @param transparency Point transparency
	 */
	public void drawFuzzyMapPoint( PVector point, float pointSize, float mapWidth, float mapHeight, float hue, float saturation, float brightness, float transparency )
	{
		float size = pointSize;
		int iterations = PApplet.round(size);
		int sizeDiff = PApplet.round(size/iterations);
		float alpha = transparency;
		float alphaDiff = transparency / iterations;
		
		for(int i=0; i<iterations; i++)
		{
			drawMapPoint( point, size * smallPointSize * mapWidth, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, alpha * 0.33f );
			size-=sizeDiff;
			alpha-=alphaDiff;
		}
	}
	
	/**
	 * Draw Interactive Clustering screen
	 */
	void displayInteractiveClustering()
	{
		drawLargeMap();
		if(messages.size() > 0) displayMessages();
	}

	/**
	 * Draw Interactive Clustering footer text
	 */
	void displayClusteringInfo()
	{
//		message("Interactive Clustering Mode: "+(p.hierarchical ?"Hierarchical Clustering":"K-Means Clustering"));
//		message(" ");
		
		if(p.hierarchical)
		{
//			message("Hierarchical Clustering");
			message(" ");
			message("Use arrow keys UP and DOWN to change clustering depth... ");
			message("Use [ and ] to change Minimum Cluster Distance... ");
		}
		else
		{
//			message("K-Means Clustering");
			message(" ");
			message("Use arrow keys LEFT and RIGHT to change Iterations... ");
			message("Use arrow keys UP and DOWN to change Population Factor... ");
			message("Use [ and ] to change Minimum Cluster Distance... ");
		}
		
		message(" ");
		message("Press <spacebar> to restart 3D viewer...");
	}
	
	/**
	 * Draw large 2D map
	 */
	void drawLargeMap()
	{
		p.pushMatrix();
		beginHUD();									// Begin 2D drawing
		
		p.fill(55, 0, 255, 255);
		p.textSize(largeTextSize);
		float textXPos = centerTextXOffset;
		float textYPos = topTextYOffset;

		if(p.interactive)
		{
			p.text("Interactive "+(p.hierarchical ? "Hierarchical" : "K-Means")+" Clustering", textXPos, textYPos, hudDistance);
		}
		else
			p.text(p.getCurrentField().name, textXPos, textYPos, hudDistance);

		p.popMatrix();
		drawMap(largeMapWidth, largeMapHeight, largeMapXOffset, largeMapYOffset);
	}
	
	/**
	 * drawTimelines()
	 * Draw timelines
	 */
	void drawTimelines()
	{
		float y = logoYOffset * 0.75f;			// Starting vertical position
		float x = logoXOffset * 0.525f;

		p.fill(55, 0, 255, 255);
		p.stroke(55, 0, 255, 255);
		p.strokeWeight(1.f);

		GMV_Cluster c = p.getCurrentCluster();

		if(c != null)
		{
			p.pushMatrix();
			beginHUD();						
			p.textSize(mediumTextSize);
			p.text("Timeline", x, y, hudDistance);
			p.popMatrix();

			y += 100.f;			
			x = logoXOffset / 4.f;

			p.pushMatrix();
			beginHUD();						
			p.textSize(smallTextSize);
			p.text("Cluster", x, y, hudDistance);
			p.popMatrix();

			float inc = p.width * 0.3f / c.clusterTimesHistogram.length;
			int currentTime = (int)PApplet.map(p.currentTime, 0, p.timeCycleLength, 0, c.clusterTimesHistogram.length);
			
			for(int i=0; i<c.clusterTimesHistogram.length; i++)
			{
				float val = inc * PApplet.sqrt( c.clusterTimesHistogram[i] ) * 50.f / PApplet.sqrt( (p.getCurrentCluster().images.size() + 
												p.getCurrentCluster().panoramas.size() + p.getCurrentCluster().videos.size() ) );
				x = logoXOffset / 3.f + i * inc;

				p.pushMatrix();
				beginHUD();
				p.translate(x, y, hudDistance);
				p.box(inc, val, 0);
				p.popMatrix();
				
				if(i == currentTime)					// Draw current time
				{
					p.fill(145, 255, 255, 255);
					p.stroke(145, 255, 255, 255);
					
					p.pushMatrix();
					beginHUD();
					p.translate(x, y, hudDistance);
					p.box(inc, 25.f, 0);
					p.popMatrix();
					
					p.fill(55, 0, 255, 255);
					p.stroke(55, 0, 255, 255);
				}
			}

			if(p.debug.time)
			{
				p.pushMatrix();
				beginHUD();

				FloatList clusterTimes = p.getCurrentCluster().getClusterTimes();
				x = logoXOffset / 3.f + c.clusterTimesHistogram.length * inc + 30.f;
				
				if(clusterTimes.size() == 1)
					p.text(clusterTimes.get(0), x, y, hudDistance);
				if(clusterTimes.size() == 2)
					p.text(clusterTimes.get(0)+" "+clusterTimes.get(1), x, y, hudDistance);
				if(clusterTimes.size() >= 3)
					p.text(clusterTimes.get(0)+" "+clusterTimes.get(1)+" "+clusterTimes.get(2), x, y, hudDistance);
				
				p.popMatrix();
			}

			y += 100.f;			
			x = logoXOffset / 4.f;

			p.pushMatrix();
			beginHUD();						
			p.textSize(smallTextSize);
			p.text("Field", x, y, hudDistance);
			p.popMatrix();

			inc = p.width * 0.3f / c.fieldTimesHistogram.length;
			currentTime = (int)PApplet.map(p.currentTime, 0, p.timeCycleLength, 0, c.fieldTimesHistogram.length);

			for(int i=0; i<c.fieldTimesHistogram.length; i++)
			{
				float val = inc * PApplet.sqrt( c.fieldTimesHistogram[i] ) * 500.f / PApplet.sqrt( (p.getCurrentField().images.size() + 
												p.getCurrentField().panoramas.size() + p.getCurrentField().videos.size() ) );
				x = logoXOffset / 3.f + i * inc;

				p.pushMatrix();
				beginHUD();
				p.translate(x, y, hudDistance);
				p.box(inc, val, 0);
				p.popMatrix();
				
				if(i == currentTime)					// Draw current time
				{
					p.fill(145, 255, 255, 255);
					p.stroke(145, 255, 255, 255);
					
					p.pushMatrix();
					beginHUD();
					p.translate(x, y, hudDistance);
					p.box(inc, 25.f, 0);
					p.popMatrix();
					
					p.fill(55, 0, 255, 255);
					p.stroke(55, 0, 255, 255);
				}
			}
//			for(float f : c.clusterTimes)
//			{
//
//			}
//			for(float f : c.fieldTimes)
//			{
//
//			}
		}
	}

	/**
	 * progressBar()
	 * Draw progress bar
	 */
	void progressBar()
	{
		int length = 100;	// total length
		int pos = p.setupProgress;	//current position

		for(int i=0; i<pos; i++)
		{
			p.pushMatrix();
			beginHUD();
			
			p.fill(140, 100, 255);
			float xPos = PApplet.map(i, 0, length, 0, p.width * 1.33f);
			float inc = PApplet.map(2, 0, length, 0, p.width * 1.33f) - PApplet.map(1, 0, length, 0, p.width*1.33f);
			int x = -p.width/2 + (int)xPos;
			int y = -p.height/2+p.height/2;

			p.translate(x, y, hudDistance);
			p.box(inc, inc*10.f, 1);    // Display 
			p.popMatrix();
		}
	}

	/**
	 * beginHUD()
	 * Initialize 2D drawing 
	 */
	void beginHUD()
	{
		float camInitFov = p.viewer.getInitFieldOfView();
		p.perspective(camInitFov, (float)p.width/(float)p.height, p.viewer.getNearClippingDistance(), 10000);
		
		PVector t = new PVector(p.viewer.camera.position()[0], p.viewer.camera.position()[1], p.viewer.camera.position()[2]);
		p.translate(t.x, t.y, t.z);
		p.rotateY(p.viewer.camera.attitude()[0]);
		p.rotateX(-p.viewer.camera.attitude()[1]);
		p.rotateZ(p.viewer.camera.attitude()[2]);
	}

	/**
	 * displayControls()
	 * Display the main key commands on screen
	 */
	void displayControls()
	{
		p.pushMatrix();
		beginHUD();
		
		float xPos = centerTextXOffset;
		float yPos = topTextYOffset;			// Starting vertical position
		
		p.fill(0, 0, 255, 255);                        
		p.textSize(largeTextSize);
		p.text(" Keyboard Controls ", xPos, yPos, hudDistance);

		xPos = midLeftTextXOffset;
		p.textSize(mediumTextSize);
		p.text(" Display", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.textSize(smallTextSize);
		p.text(" 1  Show/Hide Field Map   		  +SHIFT to Overlay", xPos, yPos += lineWidthWide, hudDistance);
		p.text(" 2	Show/Hide Field Statistics    +SHIFT to Overlay", xPos, yPos += lineWidth, hudDistance);
		p.text(" 3	Show/Hide Cluster Statistics  +SHIFT to Overlay", xPos, yPos += lineWidth, hudDistance);
		p.text(" 4 	Show/Hide Keyboard Controls   +SHIFT to Overlay", xPos, yPos += lineWidth, hudDistance);

		p.textSize(mediumTextSize);
		p.text(" Time", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.textSize(smallTextSize);
		p.text(" OPTION + F    Time Fading On/Off", xPos, yPos += lineWidthWide, hudDistance);
//		p.text(" Z    Toggle Time Fading Mode (Field/Cluster)", textXPos, textYPos += lineWidth, hudDistance);
		p.text(" SHIFT + Up/Dn   Cycle Length - / +", xPos, yPos += lineWidth, hudDistance);
		p.text(" space Pause On/Off   ", xPos, yPos += lineWidth, hudDistance);
		p.text(" SHIFT + Lt/Rt   Current Time - / +", xPos, yPos += lineWidth, hudDistance);

		p.textSize(mediumTextSize);
		p.text(" Time Navigation", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.textSize(smallTextSize);
		p.text(" F    Move to First Field Time Segment", xPos, yPos += lineWidth, hudDistance);
		p.text(" N    Move to Next Field Time Segment", xPos, yPos += lineWidth, hudDistance);
		p.text(" B    Move to Previous Field Time Segment", xPos, yPos += lineWidth, hudDistance);
		p.text(" n 	  Move to Next Cluster Time Segment", xPos, yPos += lineWidthWide, hudDistance);
		p.text(" b    Move to Previous Cluster Time Segment", xPos, yPos += lineWidth, hudDistance);

		xPos = centerTextXOffset;
		yPos = topTextYOffset;			// Starting vertical position
		
		p.textSize(mediumTextSize);
		p.text(" Graphics", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.textSize(smallTextSize);
		p.text(" G    Angle Fading On/Off", xPos, yPos += lineWidthWide, hudDistance);
		p.text(" , .  Object Distance + / - ", xPos, yPos += lineWidth, hudDistance);
		p.text(" _ + Visible Angle  - / +      ", xPos, yPos += lineWidth, hudDistance);
		p.text(" - = Default Focus Distance  - / +      ", xPos, yPos += lineWidth, hudDistance);
		p.text(" P Transparency Mode  On / Off      ", xPos, yPos += lineWidth, hudDistance);
		p.text(" ( ) Blend Mode  - / +      ", xPos, yPos += lineWidth, hudDistance);
		p.text(" D  Video Mode On/Off ", xPos, yPos += lineWidth, hudDistance);
		p.text(" h H v  Hide images / panoramas / videos    ", xPos, yPos += lineWidth, hudDistance);

		p.textSize(mediumTextSize);
		p.text(" Movement", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.textSize(smallTextSize);
		p.text(" a d w s   Walk Left / Right / Forward / Backward ", xPos, yPos += lineWidthWide, hudDistance);
		p.text(" Arrows    Turn Camera ", xPos, yPos += lineWidth, hudDistance);
		p.text(" q z  Zoom In / Out + / - ", xPos, yPos += lineWidth, hudDistance);
		
		p.textSize(mediumTextSize);
		p.text(" Navigation", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.textSize(smallTextSize);
		p.text(" E    Move to Nearest Cluster", xPos, yPos += lineWidthWide, hudDistance);
		p.text(" W    Move to Nearest Cluster in Front", xPos, yPos += lineWidth, hudDistance);
		p.text(" Q    Move to Next Cluster in Time", xPos, yPos += lineWidth, hudDistance);
		p.text(" A    Move to Next Location in Memory", xPos, yPos += lineWidth, hudDistance);
		p.text(" Z    Move to Random Cluster", xPos, yPos += lineWidth, hudDistance);
		p.text(" u    Move to Nearest Video ", xPos, yPos += lineWidth, hudDistance);
		p.text(" C    Lock Viewer to Nearest Cluster On/Off", xPos, yPos += lineWidthWide, hudDistance);
		p.text(" l    Look At Selected Media", xPos, yPos += lineWidth, hudDistance);
		p.text(" L    Look for Media", xPos, yPos += lineWidth, hudDistance);
		p.text(" { }  Teleport to Next / Previous Field ", xPos, yPos += lineWidth, hudDistance);

		xPos = midRightTextXOffset;
		yPos = topTextYOffset;			// Starting vertical position

		p.textSize(mediumTextSize);
		p.text(" Interaction", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.textSize(smallTextSize);
		p.text(" O    Selection Mode On/Off", xPos, yPos += lineWidthWide, hudDistance);
		p.text(" f    Select Media in Front", xPos, yPos += lineWidth, hudDistance);

		p.textSize(mediumTextSize);
		p.text(" Memory", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.textSize(smallTextSize);
		p.text(" `    Save Current View to  Memory", xPos, yPos += lineWidthWide, hudDistance);
		p.text(" y	  Navigate Memorized Places", xPos, yPos += lineWidth, hudDistance);
		p.text(" Y    Clear Memory", xPos, yPos += lineWidth, hudDistance);

		p.textSize(mediumTextSize);
		p.text(" Output", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.textSize(smallTextSize);
		p.text(" o    Set Image Output Folder", xPos, yPos += lineWidthWide, hudDistance);
		p.text(" p    Save Screen Image to Disk", xPos, yPos += lineWidth, hudDistance);
		p.text(" F    Select Media in Front", xPos, yPos += lineWidth, hudDistance);
		p.text(" R    Clear Selection", xPos, yPos += lineWidth, hudDistance);
	
//		p.text(" 2 1 Camera Speed + / - ", dispLocX, textYPos += lineWidth * 2, hudDistance);
//		p.text(" 4 3 Background Brightness + / - ", dispLocX, textYPos += lineWidth, hudDistance);
//		p.text(" - = Default Focal Length - / +      ", dispLocX, textYPos += lineWidth, hudDistance);
//		p.text(" k j  Altitude Scale Factor  + / - ", dispLocX, textYPos += lineWidth, hudDistance);
//		p.text(" _ + Cluster Minimum Points - / +      ", dispLocX, textYPos += lineWidth, hudDistance);

		p.popMatrix();
	}
	
	void initializeLargeMap()
	{
		float fr = p.getCurrentField().model.fieldAspectRatio;			//	Field ratio == fieldWidth / fieldLength;

		if(fr > 1)
		{
			largeMapWidth = largeMapMaxWidth;
			largeMapHeight = largeMapWidth / fr;
		}
		else
		{
			largeMapHeight = largeMapMaxHeight;
			largeMapWidth = largeMapHeight * fr;
		}
	}

	void initializeMaps()
	{
		initializeSmallMap();
		initializeLargeMap();
	}
	
	void initializeSmallMap()
	{
		float fr = p.getCurrentField().model.fieldAspectRatio;			//	Field ratio == fieldWidth / fieldLength;

		if(fr > 1)
		{
			smallMapWidth = smallMapMaxWidth;
			smallMapHeight = smallMapWidth / fr;
		}
		else
		{
			smallMapHeight = smallMapMaxHeight;
			smallMapWidth = smallMapHeight * fr;
		}
		
		smallMapXOffset = p.width / 2.5f;
		smallMapYOffset = logoYOffset - smallMapHeight / 3.f;
	}

	/**
	 * message()
	 * Add message to queue
	 * @param message Message to send
	 */
	void message(String message)
	{
		if(p.interactive)
		{
			messages.add(message);
			while(messages.size() > 16)
				messages.remove(0);
		}
		else
		{
			messageStartFrame = p.frameCount;		
			messages.add(message);
			while(messages.size() > 16)
				messages.remove(0);
		}

		if(p.debug.print)
			PApplet.println(message);
	}
	
	/**
	 * clearMessages()
	 * Clear previous messages
	 */
	void clearMessages()
	{
		messages = new ArrayList<String>();			
	}
	
	/**
	 * displayMessages()
	 * Display current messages
	 */
	void displayMessages()
	{
		float yPos = userMessageYOffset - lineWidth;

		p.pushMatrix();
		beginHUD();
		p.fill(0, 0, 255, 255);            								
		p.textSize(smallTextSize);

		if(p.interactive)
		{
			for(String s : messages)
				p.text(s, userMessageXOffset, yPos += lineWidth, hudDistance);		// Use period character to draw a point
		}
		else if(p.frameCount - messageStartFrame < messageDuration)
		{
			for(String s : messages)
				p.text(s, userMessageXOffset, yPos += lineWidth, hudDistance);		// Use period character to draw a point
		}
		else
		{
			clearMessages();														// Clear messages after duration has ended
		}

		p.popMatrix();
	}

	/**
	 * sendMetadata()
	 * Add a metadata message (single line) to the display queue
	 * @param message Line of metadata 
	 */
	void metadata(String message)
	{
		metadataStartFrame = p.frameCount;		
		metadata.add(message);
		
		while(metadata.size() > 16)
			metadata.remove(0);
	}
	
	/**
	 * displayMetadata()
	 * Draw current metadata messages to the screen
	 */
	void displayMetadata()
	{
//		if( !(p.viewer.lastMovementFrame == p.frameCount) )				// As long as the user doesn't move, display metadata
		{
			float yPos = metadataYOffset - lineWidth;
			
			p.pushMatrix();
			beginHUD();
			
			p.fill(0, 0, 255, 255);                     // White text
			p.textSize(mediumTextSize);

			for(String s : metadata)
			{
				p.text(s, leftTextXOffset, yPos += lineWidth, hudDistance);				// Use period character to draw a point
			}
			p.popMatrix();
		}
//		else
//		{
//			metadata = new ArrayList<String>();							// Reset message list if viewer moves
//		}
	}
	
	/**
	 * clearMetadata()
	 * Clear previous metadata messages
	 */
	void clearMetadata()
	{
		metadata = new ArrayList<String>();							// Reset message list
	}
	
	/**
	 * sendSetupMessage()
	 * @param message Message to be sent
	 * Add startup message to display queue
	 */
	void sendSetupMessage(String message)
	{
		if(initialSetup)																
		{
			startupMessageStartFrame = p.frameCount;		
			startupMessages.add(message);
			while(startupMessages.size() > 16)
				startupMessages.remove(0);

			if(p.debug.print)
				PApplet.println(message);
		}
	}
	
	/**
	 * displayStartupMessages()
	 * Display startup messages in queue
	 */
	void displayStartupMessages()
	{
		float yPos = startupMessageYOffset - lineWidth;

		p.pushMatrix();
		beginHUD();
		p.fill(0, 0, 255, 255);            								
		p.textSize(largeTextSize * 1.5f);
		
		if(initialSetup)																// Showing setup startup messages
		{
			for(String s : startupMessages)
				p.text(s, startupMessageXOffset, yPos += lineWidth * 1.5f, hudDistance);		// Use period character to draw a point
		}
		else
			displayMessages();

		p.popMatrix();
	}
	
	/**
	 * clearSetupMessages()
	 * Clear previous setup messages
	 */
	void clearSetupMessages()
	{
		startupMessages = new ArrayList<String>();
	}
	
	void resetDisplayModes()
	{
		map = false;
		mapOverlay = false;
		control = false;
		controlOverlay = false;
		cluster = false;
		clusterOverlay = false;
		info = false;
		infoOverlay = false;
		
		clearMessages();
		clearMetadata();
	}
	
	/**
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @param mapXOffset Map X offset
	 * @param mapYOffset Map Y offset
	 */
	void drawMap(float mapWidth, float mapHeight, float mapXOffset, float mapYOffset)
	{
//		Map Modes  	1:  All   2: Clusters + Media   3: Clusters + Capture Locations  4: Capture Locations + Media
//					5:  Clusters Only   6: Media Only   7: Capture Locations Only
		
		/* Media */
		if((mapMode == 1 || mapMode == 2 || mapMode == 4 || mapMode == 6) && mapImages && !p.getCurrentField().hideImages)
			for ( GMV_Image i : p.getCurrentField().images )		// Draw images on 2D Map
				drawImageOnMap(i, mapWidth, mapHeight, false);

		if((mapMode == 1 || mapMode == 2 || mapMode == 4 || mapMode == 6) && mapPanoramas && !p.getCurrentField().hidePanoramas)
			for ( GMV_Panorama n : p.getCurrentField().panoramas )	// Draw panoramas on 2D Map
				drawPanoramaOnMap(n, mapWidth, mapHeight, false);

		if((mapMode == 1 || mapMode == 2 || mapMode == 4 || mapMode == 6) && mapVideos && !p.getCurrentField().hideVideos)
			for (GMV_Video v : p.getCurrentField().videos)			// Draw videos on 2D Map
				drawVideoOnMap(v, mapWidth, mapHeight, false);

		if((mapMode == 1 || mapMode == 3 || mapMode == 4 || mapMode == 7) && mapImages && !p.getCurrentField().hideImages)
			for ( GMV_Image i : p.getCurrentField().images )		// Draw image capture locations on 2D Map
				drawImageOnMap(i, mapWidth, mapHeight, true);

		if((mapMode == 1 || mapMode == 3 || mapMode == 4 || mapMode == 7) && mapPanoramas && !p.getCurrentField().hidePanoramas)
			for ( GMV_Panorama n : p.getCurrentField().panoramas )	// Draw panorama capture locations on 2D Map
				drawPanoramaOnMap(n, mapWidth, mapHeight, true);

		if((mapMode == 1 || mapMode == 3 || mapMode == 4 || mapMode == 7) && mapVideos && !p.getCurrentField().hideVideos)
			for (GMV_Video v : p.getCurrentField().videos)			// Draw video capture locations on 2D Map
				drawVideoOnMap(v, mapWidth, mapHeight, true);

		/* Clusters */
		if((mapMode == 1 || mapMode == 2 || mapMode == 3 || mapMode == 5) && mapClusters)
			for( GMV_Cluster c : p.getCurrentField().clusters )							
				drawFuzzyMapPoint( c.getLocation(), PApplet.sqrt(c.mediaPoints) * 0.5f, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, mapMediaTransparency );

		if(!p.interactive)				// While not in Clustering Mode
		{
			if(mapMode == 1 || mapMode == 2 || mapMode == 3 || mapMode == 5)
			{
				if(p.viewer.getAttractorCluster() != -1 && p.viewer.getAttractorCluster() < p.getFieldClusters().size())
					drawMapPoint( p.getAttractorCluster().getLocation(), hugePointSize * mapWidth, mapWidth, mapHeight, mapAttractorClusterHue, 255.f, 255.f, mapMediaTransparency );

				if(p.viewer.getCurrentCluster() != -1 && p.viewer.getCurrentCluster() < p.getFieldClusters().size())
					drawMapPoint( p.getCurrentCluster().getLocation(), hugePointSize * mapWidth, mapWidth, mapHeight, mapAttractorClusterHue, 255.f, 255.f, mapMediaTransparency );
			}
			
			drawCameraOnMap(mapWidth, mapHeight);
		}
		
		drawOriginOnMap(mapWidth, mapHeight);
	}

	/**
	 * @param image GMV_Image to draw
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @param capture Draw capture location (true) or viewing location (false)
	 * Draw image location on map of specified size
	 */
	void drawImageOnMap(GMV_Image image, float mapWidth, float mapHeight, boolean capture)
	{
		float pointSize = smallPointSize * mapWidth;
		
		float saturation;

		float imageDistance = image.getViewingDistance();   // Get photo distance from current camera position

		// IMPLEMENT TIME
		// p.fill(mapVideoHue, 100, 255, 255);                         // If out of visible range and not at a visible time, lightest color
		// p.stroke(mapVideoHue, 100, 255, 255);                                        

		if (imageDistance < p.viewer.getFarViewingDistance() && imageDistance > p.viewer.getNearClippingDistance())    // If image is in visible range
			saturation = 100.f;                                              
		else      																				// If out of visible range
			saturation = maxSaturation;                                              

		if(image.location != null && !image.disabled && !image.hidden)
		{
			if(capture)
				drawMapPoint( image.getCaptureLocation(), pointSize * (image.isSelected() ? 10.f : 1.f), mapWidth, mapHeight, mapImageCaptureHue, saturation, 255.f, mapMediaTransparency );
			else
				drawMapPoint( image.location, pointSize, mapWidth, mapHeight, mapImageHue, saturation, 255.f, mapMediaTransparency );
		}
		
	}
	
	/**
	 * drawPanoramaOnMap()
	 * @param panorama GMV_Panorama to draw
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * Draw image location on map of specified size
	 */
	void drawPanoramaOnMap(GMV_Panorama panorama, float mapWidth, float mapHeight, boolean capture)
	{
		float pointSize = mediumPointSize * mapWidth;
		
		float saturation = 255.f;
		float panoramaDistance = panorama.getViewingDistance();   // Get photo distance from current camera position

		// IMPLEMENT TIME
		// p.fill(mapVideoHue, 100, 255, 255);                         // If out of visible range and not at a visible time, lightest color
		// p.stroke(mapVideoHue, 100, 255, 255);                                        

		if (panoramaDistance < p.viewer.getFarViewingDistance() && panoramaDistance > p.viewer.getNearClippingDistance())    // If panorama is in visible range
			saturation = 100.f;                                              
		else      																				// If out of visible range
			saturation = maxSaturation;                                              

		if(panorama.location != null && !panorama.disabled && !panorama.hidden)
		{
			if(capture)
				drawMapPoint( panorama.getCaptureLocation(), pointSize * (panorama.isSelected() ? 10.f : 1.f), mapWidth, mapHeight, mapPanoramaCaptureHue, saturation, 255.f, mapMediaTransparency );
			else
				drawMapPoint( panorama.location, pointSize, mapWidth, mapHeight, mapPanoramaHue, saturation, 255.f, mapMediaTransparency );
		}
	}

	/**
	 * drawVideoOnMap()
	 * @param video GMV_Video to draw
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * Draw image location on map of specified size
	 */
	void drawVideoOnMap(GMV_Video video, float mapWidth, float mapHeight, boolean capture)
	{
		float pointSize = mediumPointSize * mapWidth;
		
		float saturation;
		float videoDistance = video.getViewingDistance();   // Get photo distance from current camera position

		// IMPLEMENT TIME
		// p.fill(mapVideoHue, 100, 255, 255);                         // If out of visible range and not at a visible time, lightest color
		// p.stroke(mapVideoHue, 100, 255, 255);                                        

		if (videoDistance < p.viewer.getFarViewingDistance() && videoDistance > p.viewer.getNearClippingDistance())    // If video is in visible range
			saturation = minSaturation;                                              
		else      																				// If out of visible range
			saturation = maxSaturation;                                              

		if(video.location != null && !video.disabled && !video.hidden)
		{
			if(capture)
				drawMapPoint( video.getCaptureLocation(), pointSize * (video.isSelected() ? 10.f : 1.f), mapWidth, mapHeight, mapVideoCaptureHue, saturation, 255.f, mapMediaTransparency );
			else
				drawMapPoint( video.location, pointSize, mapWidth, mapHeight, mapVideoHue, saturation, 255.f, mapMediaTransparency );
		}
	}

	/**
	 * Draw the map origin
	 * @param mapWidth
	 * @param mapHeight
	 */
	void drawOriginOnMap(float mapWidth, float mapHeight)
	{
		int size = (int)(mapWidth / 20.f);
		for(int i=-size/2; i<size/2; i+=size/20)
			drawMapPoint( new PVector(i, 0.f, 0.f), hugePointSize * mapWidth, mapWidth, mapHeight, 180.f, 30.f, 255.f, mapMediaTransparency / 2.f );
		for(int i=-size/2; i<size/2; i+=size/20)
			drawMapPoint( new PVector(0.f, 0.f, i), hugePointSize * mapWidth, mapWidth, mapHeight, 180.f, 30.f, 255.f, mapMediaTransparency / 2.f );
	}
	
	/**
	 * Interesting effect
	 * @param mapWidth
	 * @param mapHeight
	 */
	void drawMandalaOnMap(float mapWidth, float mapHeight)
	{
		int size = (int)(mapWidth / 20.f);
		for(int i=-size/2; i<size/2; i+=size/20)
			drawMapPoint( new PVector(i, 0.f, 0.f), hugePointSize * mapWidth * 20.f / (i+size/2), mapWidth, mapHeight, 180.f, 30.f, 255.f, mapMediaTransparency / 2.f );
		for(int i=-size/2; i<size/2; i+=size/20)
			drawMapPoint( new PVector(0.f, 0.f, i), hugePointSize * mapWidth * 20.f / (i+size/2), mapWidth, mapHeight, 180.f, 30.f, 255.f, mapMediaTransparency / 2.f );
	}

	/**
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * Draw current viewer location and orientation on map of specified size
	 */
	void drawCameraOnMap(float mapWidth, float mapHeight)
	{
		PVector camLoc = p.viewer.getLocation();
		float camYaw = -p.viewer.getXOrientation() - 0.5f * PApplet.PI;

		drawMapPoint( camLoc, cameraPointSize, mapWidth, mapHeight, mapCameraHue, 255.f, 255.f, mapMediaTransparency );
		float ptSize = cameraPointSize;
				
		ScaleMap logMap;
		logMap = new ScaleMap(6., 60., 6., 60.);		/* Time fading interpolation */
		logMap.setMapFunction(p.circularEaseOut);

		/* Change viewer arrow based on fieldWidth -- should be fieldLength??  -- should depend on zoom level too! */
//		int arrowLength = (int)logMap.getMappedValueFor( PApplet.map( p.getCurrentField().model.fieldWidth, 100.f, 10000.f, 6.f, 60.f ) );
		int arrowLength = 30;
		
		logMap = new ScaleMap(0.f, 0.25f, 0.f, 0.25f);		/* Time fading interpolation */
		logMap.setMapFunction(p.circularEaseOut);
		
		float shrinkFactor = PApplet.map(arrowLength, 6.f, 60.f, 0.f, 0.25f);
		shrinkFactor = (float)logMap.getMappedValueFor(shrinkFactor);
		shrinkFactor = 0.95f - (0.25f - shrinkFactor);		// Reverse mapping high to low
				
		for(int i=1; i<arrowLength; i++)
		{
			p.textSize(ptSize);
			float x = i * cameraPointSize * 0.5f * (float)Math.cos( camYaw );
			float y = i * cameraPointSize * 0.5f * (float)Math.sin( camYaw );
			
			PVector arrowPoint = new PVector(camLoc.x + x, 0, camLoc.z + y);
			drawMapPoint( arrowPoint, ptSize, mapWidth, mapHeight, mapCameraHue, 120.f, 255.f, 255.f );

			ptSize *= shrinkFactor;
		}
	}

	/**
	 * Show startup screen
	 */
	public void showStartup()
	{
		sendSetupMessage("Welcome to GeoSynth!");
		sendSetupMessage(" ");
		sendSetupMessage("Please select a library folder...");
		draw();								// Draw setup display
	}
	
	/**
	 * drawForceVector()
	 * @param origin Vector starting point
	 * @param vector Direction vector
	 * Draw current viewer location and orientation on map of specified size
	 */
	void drawForceVector(PVector vector)
	{
		if(!drawForceVector)
			drawForceVector = true;

//		mapVectorOrigin = origin;
		mapVectorVector = vector;
	}

	/**
	 * drawGMVClusters()
	 * Draw the clusters at given depth
	 */
	void drawGMVClusters()
	{		 
		if(  !initialSetup && !mapOverlay && !controlOverlay && !info 
				&& messages.size() < 0 && metadata.size() < 0	 )
		{
			p.hint(PApplet.DISABLE_DEPTH_TEST);						// Disable depth testing for drawing HUD
		}

		for( GMV_Cluster c : p.getCurrentField().clusters )								// For all clusters at current depth
		{
			drawMapPoint( c.getLocation(), 5.f, largeMapWidth, largeMapHeight, mapClusterHue, 255.f, 255.f, mapMediaTransparency );
		}
	}
	
	private void drawForceVector()
	{
		mapVectorOrigin = p.viewer.getLocation();
		
		float ptSize = cameraPointSize * 2.f;
		drawMapPoint( mapVectorOrigin, ptSize, largeMapWidth, largeMapHeight, mapCameraHue, 255.f, 255.f, mapMediaTransparency );
		
		int arrowLength = 30;
		
		ScaleMap logMap;
		logMap = new ScaleMap(6., 60., 6., 60.);		/* Time fading interpolation */
		logMap.setMapFunction(p.circularEaseOut);

//		arrowLength = (int)logMap.getMappedValueFor(arrowLength);
		
		logMap = new ScaleMap(0.f, 0.25f, 0.f, 0.25f);		/* Time fading interpolation */
		logMap.setMapFunction(p.circularEaseOut);
		
		float shrinkFactor = PApplet.map(arrowLength, 6.f, 60.f, 0.f, 0.25f);
		shrinkFactor = (float)logMap.getMappedValueFor(shrinkFactor);
		shrinkFactor = 0.95f - (0.25f - shrinkFactor);		// Reverse mapping high to low
		PVector current = mapVectorVector;
		
		for(int i=1; i<arrowLength; i++)
		{
			p.textSize(ptSize);
			float mult = (i+6) * 0.025f;
			current = mapVectorVector.mult( mult );
			
			PVector arrowPoint = new PVector(mapVectorOrigin.x + current.x, 0, mapVectorOrigin.z + current.z);
			drawMapPoint( arrowPoint, ptSize, largeMapWidth, largeMapHeight, 255.f-mapCameraHue, 170.f, 255.f, 255.f );

			ptSize *= shrinkFactor;
		}
	}
	
	 /**
	  * changeBlendMode()
	  * Increment blendMode by given amount and call setBlendMode()
	  * @param inc Increment to blendMode number
	  */
	public void changeBlendMode(int inc) 
	{
		if(inc > 0)
		{
			if (blendMode+inc < numBlendModes) 	
				blendMode += inc;
			else 
				blendMode = 0;
		}
		else if(inc < 0)
		{
			{
				if (blendMode-inc >= 0) 
					blendMode -= inc;
				else 
					blendMode = numBlendModes - 1;
			}
		}

		if(inc != 0)
			setBlendMode(blendMode);
	}

	/**
	 * setBlendMode()
	 * Change effect of image alpha channel on blending
	 * @param blendMode
	 */
	public void setBlendMode(int blendMode) {
		switch (blendMode) {
		case 0:
			p.blendMode(PApplet.BLEND);
			break;

		case 1:
			p.blendMode(PApplet.ADD);
			break;

		case 2:
			p.blendMode(PApplet.SUBTRACT);
			break;

		case 3:
			p.blendMode(PApplet.DARKEST);
			break;

		case 4:
			p.blendMode(PApplet.LIGHTEST);
			break;

		case 5:
			p.blendMode(PApplet.DIFFERENCE);
			break;

		case 6:
			p.blendMode(PApplet.EXCLUSION);
			break;

		case 7:
			p.blendMode(PApplet.MULTIPLY);
			break;

		case 8:
			p.blendMode(PApplet.SCREEN);
			break;

		case 9:
			p.blendMode(PApplet.REPLACE);
			break;

		case 10:
			// blend(HARD_LIGHT);
			break;

		case 11:
			// blend(SOFT_LIGHT);
			break;

		case 12:
			// blend(OVERLAY);
			break;

		case 13:
			// blend(DODGE);
			break;

		case 14:
			// blend(BURN);
			break;
		}

		if (p.debug.field)
			PApplet.println("blendMode:" + blendMode);
	}
	
	/**
	 * Show statistics of the current simulation
	 */
	void displayStatistics()
	{
		p.pushMatrix();
		beginHUD();
		
		float textXPos = midLeftTextXOffset;
		float textYPos = topTextYOffset;			// Starting vertical position
		
		GMV_Field f = p.getCurrentField();
		
		if(p.viewer.getCurrentCluster() >= 0)
		{
			GMV_Cluster c = p.getCurrentCluster();
			float[] camTar = p.viewer.camera.target();

			p.fill(0, 0, 255, 255);

			p.textSize(mediumTextSize);
//			if(p.pause) p.text(" --- Paused --- ", textXPos, textYPos, hudDistance);
			p.text(" Settings ", textXPos, textYPos += lineWidthVeryWide, hudDistance);
			p.textSize(smallTextSize);
			p.text("Angle Fading: "+p.angleFading, textXPos, textYPos += lineWidthVeryWide, hudDistance);
			p.text("Lock Media to Clusters:"+p.lockMediaToClusters, textXPos, textYPos += lineWidth, hudDistance);
			p.text("Alpha Mode: "+p.alphaMode, textXPos, textYPos += lineWidth, hudDistance);

			p.textSize(mediumTextSize);
			p.text(" Field ", textXPos, textYPos += lineWidthVeryWide, hudDistance);
			p.textSize(smallTextSize);
			p.text(" Name: "+f.name, textXPos, textYPos += lineWidthVeryWide, hudDistance);
			p.text(" ID: "+(p.viewer.getField()+1)+" out of "+p.getFieldCount()+" Total Fields", textXPos, textYPos += lineWidth, hudDistance);
			p.text(" Width (m.): "+f.model.fieldWidth+" Length (m.): "+f.model.fieldLength+" Height (m.): "+f.model.fieldHeight, textXPos, textYPos += lineWidth, hudDistance);
			p.text(" Total Images: "+f.getImageCount(), textXPos, textYPos += lineWidth, hudDistance);					// Doesn't check for dataMissing!!
			p.text(" Total Panoramas: "+f.getPanoramaCount(), textXPos, textYPos += lineWidth, hudDistance);			// Doesn't check for dataMissing!!
			p.text(" Total Videos: "+f.getVideoCount(), textXPos, textYPos += lineWidth, hudDistance);					// Doesn't check for dataMissing!!
			p.text(" Media Density per sq. m.: "+f.model.mediaDensity, textXPos, textYPos += lineWidth, hudDistance);
			p.text(" Images Visible: "+f.imagesVisible, textXPos, textYPos += lineWidth, hudDistance);
			p.text(" Videos Visible: "+f.videosVisible, textXPos, textYPos += lineWidth, hudDistance);

			p.textSize(mediumTextSize);
			p.text(" Model ", textXPos, textYPos += lineWidthVeryWide, hudDistance);
			p.textSize(smallTextSize);
			p.text(" Clustering Mode : "+ ( p.hierarchical ? "Hierarchical" : "K-Means" ), textXPos, textYPos += lineWidthVeryWide, hudDistance);
			p.text(" Clusters:"+(f.clusters.size()-f.model.mergedClusters), textXPos, textYPos += lineWidthVeryWide, hudDistance);
			p.text(" Merged: "+f.model.mergedClusters+" out of "+f.clusters.size()+" Total", textXPos, textYPos += lineWidth, hudDistance);
			if(p.hierarchical) p.text(" Current Cluster Depth: "+f.model.clusterDepth, textXPos, textYPos += lineWidth, hudDistance);

			textXPos = centerTextXOffset;
			textYPos = topTextYOffset;			// Starting vertical position

			p.textSize(mediumTextSize);
			p.text(" Viewer ", textXPos, textYPos += lineWidthVeryWide, hudDistance);
			p.textSize(smallTextSize);
			p.text(" Location, x: "+PApplet.round(p.viewer.getLocation().x)+" y:"+PApplet.round(p.viewer.getLocation().y)+" z:"+
					 PApplet.round(p.viewer.getLocation().z), textXPos, textYPos += lineWidthVeryWide, hudDistance);		
			p.text(" GPS Longitude: "+p.viewer.getGPSLocation().x+" Latitude:"+p.viewer.getGPSLocation().y, textXPos, textYPos += lineWidth, hudDistance);		

			p.text(" Current Cluster: "+p.viewer.getCurrentCluster(), textXPos, textYPos += lineWidthVeryWide, hudDistance);
			p.text("  Media Points: "+c.mediaPoints, textXPos, textYPos += lineWidth, hudDistance);
			p.text("  Media Segments: "+p.getCurrentCluster().segments.size(), textXPos, textYPos += lineWidth, hudDistance);
			p.text("  Distance: "+PApplet.round(PVector.dist(c.getLocation(), p.viewer.getLocation())), textXPos, textYPos += lineWidth, hudDistance);
			p.text("  Auto Stitched Panoramas: "+p.getCurrentCluster().stitchedPanoramas.size(), textXPos, textYPos += lineWidth, hudDistance);
			p.text("  User Stitched Panoramas: "+p.getCurrentCluster().userPanoramas.size(), textXPos, textYPos += lineWidth, hudDistance);
			if(p.viewer.getAttractorCluster() != -1)
			{
				p.text(" Destination Cluster : "+p.viewer.getAttractorCluster(), textXPos, textYPos += lineWidth, hudDistance);
				p.text(" Destination Media Points: "+p.getCluster(p.viewer.getAttractorCluster()).mediaPoints, textXPos, textYPos += lineWidth, hudDistance);
				p.text("    Destination Distance: "+PApplet.round( PVector.dist(f.clusters.get(p.viewer.getAttractorCluster()).getLocation(), p.viewer.getLocation() )), textXPos, textYPos += lineWidth, hudDistance);
			}

			if(p.debug.viewer) 
			{
				p.text(" Debug: Current Attraction: "+p.viewer.attraction.mag(), textXPos, textYPos += lineWidth, hudDistance);
				p.text(" Debug: Current Acceleration: "+(p.viewer.isWalking() ? p.viewer.walkingAcceleration.mag() : p.viewer.acceleration.mag()), textXPos, textYPos += lineWidth, hudDistance);
				p.text(" Debug: Current Velocity: "+ (p.viewer.isWalking() ? p.viewer.walkingVelocity.mag() : p.viewer.velocity.mag()) , textXPos, textYPos += lineWidth, hudDistance);
				p.text(" Debug: Moving? " + p.viewer.isMoving(), textXPos, textYPos += lineWidth, hudDistance);
				p.text(" Debug: Slowing? " + p.viewer.isSlowing(), textXPos, textYPos += lineWidth, hudDistance);
				p.text(" Debug: Halting? " + p.viewer.isHalting(), textXPos, textYPos += lineWidth, hudDistance);
			}

			p.textSize(mediumTextSize);
			p.text(" Graphics ", textXPos, textYPos += lineWidthVeryWide, hudDistance);
			p.textSize(smallTextSize);
			p.text(" Image Size Factor:"+p.subjectSizeRatio, textXPos, textYPos += lineWidth, hudDistance);
			p.text(" Default Focus Distance (m.):"+p.defaultFocusDistance, textXPos, textYPos += lineWidthVeryWide, hudDistance);
			p.text(" Image Size Factor:"+p.subjectSizeRatio, textXPos, textYPos += lineWidth, hudDistance);

			if(p.debug.viewer)
			{
				p.text(" Debug: X Orientation (Yaw):" + p.viewer.getXOrientation(), textXPos, textYPos += lineWidth, hudDistance);
				p.text(" Debug: Y Orientation (Pitch):" + p.viewer.getYOrientation(), textXPos, textYPos += lineWidth, hudDistance);
				p.text(" Debug: Target Point x:" + camTar[0] + ", y:" + camTar[1] + ", z:" + camTar[2], textXPos, textYPos += lineWidth, hudDistance);
			}
			else
			{
				p.text(" Compass Direction:" + p.utilities.angleToCompass(p.viewer.getXOrientation())+" Angle: "+p.viewer.getXOrientation(), textXPos, textYPos += lineWidth, hudDistance);
				p.text(" Vertical Direction:" + PApplet.degrees(p.viewer.getYOrientation()), textXPos, textYPos += lineWidth, hudDistance);
				p.text(" Zoom:"+p.viewer.camera.fov(), textXPos, textYPos += lineWidth, hudDistance);
			}
			p.text(" Field of View:"+p.viewer.camera.fov(), textXPos, textYPos += lineWidth, hudDistance);

//			if(f.selectedImage != -1)
//				p.text(" Selected Image:"+f.selectedImage, textXPos, textYPos += lineWidthVeryWide, hudDistance);
//			if(f.selectedPanorama != -1)
//				p.text(" Selected Panorama:"+f.selectedPanorama, textXPos, textYPos += lineWidthVeryWide, hudDistance);
//			if(f.selectedVideo != -1)
//				p.text(" Selected Video:"+f.selectedVideo, textXPos, textYPos += lineWidth, hudDistance);

			
			
			p.textSize(mediumTextSize);
			p.text(" Time ", textXPos, textYPos += lineWidthVeryWide, hudDistance);
			p.textSize(smallTextSize);
			p.text(" Time Fading: "+ p.timeFading, textXPos, textYPos += lineWidthVeryWide, hudDistance);
			p.text(" Timeline Segments: "+ p.getCurrentField().timeline.size(), textXPos, textYPos += lineWidth, hudDistance);
			p.text(" Current Segment: "+ p.viewer.currentFieldTimeSegment, textXPos, textYPos += lineWidth, hudDistance);
			if(f.timeline.size() > 0 && p.viewer.currentFieldTimeSegment >= 0 && p.viewer.currentFieldTimeSegment < f.timeline.size())
				p.text(" Upper: "+f.timeline.get(p.viewer.currentFieldTimeSegment).getUpper()+" Center:"+f.timeline.get(p.viewer.currentFieldTimeSegment).getCenter()+
					   " Lower: "+f.timeline.get(p.viewer.currentFieldTimeSegment).getLower(), textXPos, textYPos += lineWidth, hudDistance);
			p.text(" Cluster Segments: "+ p.getCurrentCluster().timeline.size(), textXPos, textYPos += lineWidth, hudDistance);
//			p.text(" Current Cluster Segment: "+ p.viewer.currentClusterTimeSegment, textXPos, textYPos += lineWidth, hudDistance);
//			p.text(" Upper: "+c.timeline.get(p.viewer.currentFieldTimeSegment).getUpper()+" Center:"+c.timeline.get(p.viewer.currentFieldTimeSegment).getCenter()+
//					 " Lower: "+c.timeline.get(p.viewer.currentFieldTimeSegment).getLower(), textXPos, textYPos += lineWidth, hudDistance);

//			textXPos = midRightTextXOffset;
//			textYPos = topTextYOffset;			// Starting vertical position

			p.textSize(mediumTextSize);
			p.text(" Output ", textXPos, textYPos += lineWidthVeryWide, hudDistance);
			p.textSize(smallTextSize);
			p.text(" Image Output Folder:"+p.outputFolder, textXPos, textYPos += lineWidthVeryWide, hudDistance);
//			p.text(" Library Folder:"+p.getLibrary(), dispLocX, textYPos += lineWidthWide, hudDistance);

			if(p.debug.memory)
			{
				if(p.debug.detailed)
				{
					p.text("Total memory (bytes): " + p.debug.totalMemory, textXPos, textYPos += lineWidth, hudDistance);
					p.text("Available processors (cores): "+p.debug.availableProcessors, textXPos, textYPos += lineWidth, hudDistance);
					p.text("Maximum memory (bytes): " +  (p.debug.maxMemory == Long.MAX_VALUE ? "no limit" : p.debug.maxMemory), textXPos, textYPos += lineWidth, hudDistance); 
					p.text("Total memory (bytes): " + p.debug.totalMemory, textXPos, textYPos += lineWidth, hudDistance);
					p.text("Allocated memory (bytes): " + p.debug.allocatedMemory, textXPos, textYPos += lineWidth, hudDistance);
				}
				p.text("Free memory (bytes): "+p.debug.freeMemory, textXPos, textYPos += lineWidth, hudDistance);
				p.text("Approx. usable free memory (bytes): " + p.debug.approxUsableFreeMemory, textXPos, textYPos += lineWidth, hudDistance);
			}			
			p.text(" GeoSynth v1.0 by David Gordon, Copyright © 2016", textXPos, textYPos += lineWidthVeryWide, hudDistance);

		}
		else
			message("Can't display statistics: currentCluster == "+p.viewer.getCurrentCluster()+"!!!");
		
		p.popMatrix();
	}

	/**
	 * displayClusterStats()
	 * Draw cluster statistics display
	 */
	void displayClusterStats()
	{
		p.pushMatrix();
		beginHUD();
		
		float textXPos = centerTextXOffset;
		float textYPos = topTextYOffset;			// Starting vertical position
		
		GMV_Field f = p.getCurrentField();
		GMV_Cluster c = p.getCluster(displayCluster);	// Get the cluster to display info about

		p.fill(0, 0, 255, 255);

		p.textSize(mediumTextSize);
		p.text(""+p.getCurrentField().name+ " Media Clusters", textXPos, textYPos, hudDistance);
		p.textSize(smallTextSize);
		p.text(" Clusters:"+(f.clusters.size()-f.model.mergedClusters), textXPos, textYPos += lineWidthVeryWide, hudDistance);
		p.text(" Merged: "+f.model.mergedClusters+" out of "+f.clusters.size()+" Total", textXPos, textYPos += lineWidth, hudDistance);
		if(p.hierarchical) p.text(" Current Cluster Depth: "+f.model.clusterDepth, textXPos, textYPos += lineWidth, hudDistance);
		p.text(" Minimum Distance: "+p.minClusterDistance, textXPos, textYPos += lineWidth, hudDistance);
		p.text(" Maximum Distance: "+p.maxClusterDistance, textXPos, textYPos += lineWidth, hudDistance);
		p.text(" Population Factor: "+f.model.clusterPopulationFactor, textXPos, textYPos += lineWidth, hudDistance);
		p.text(" ID: "+ c.getID(), textXPos, textYPos += lineWidthVeryWide, hudDistance);
		p.text(" Location: "+ c.getLocation(), textXPos, textYPos += lineWidth, hudDistance);
		p.text(" Media Points: "+ c.mediaPoints, textXPos, textYPos += lineWidth, hudDistance);
		p.text(" Auto Stitched Panoramas: "+p.getCurrentCluster().stitchedPanoramas.size(), textXPos, textYPos += lineWidth, hudDistance);
		p.text(" User Stitched Panoramas: "+p.getCurrentCluster().userPanoramas.size(), textXPos, textYPos += lineWidth, hudDistance);
		p.text(" Media Segments: "+ c.segments.size(), textXPos, textYPos += lineWidth, hudDistance);
		p.text(" Timeline Points: "+ c.timeline.size(), textXPos, textYPos += lineWidth, hudDistance);
		p.text(" ", textXPos, textYPos += lineWidth, hudDistance);
		p.text(" Active: "+ c.isActive(), textXPos, textYPos += lineWidth, hudDistance);
		p.text(" Single: "+ c.isSingle(), textXPos, textYPos += lineWidth, hudDistance);
		p.text(" Empty: "+ c.isEmpty(), textXPos, textYPos += lineWidth, hudDistance);
		p.text(" ", textXPos, textYPos += lineWidth, hudDistance);
		p.text(" Viewer Distance: "+PApplet.round(PVector.dist(c.getLocation(), p.viewer.getLocation())), textXPos, textYPos += lineWidth, hudDistance);
		
		if(p.debug.cluster)
		{
			p.text(" -- Debug --", textXPos, textYPos += lineWidth, hudDistance);
			p.text(" Cluster Times (Size): "+ c.getClusterTimes().size(), textXPos, textYPos += lineWidth, hudDistance);
//			p.text(" Field Timeline Times: "+ c.getFieldTimes().size(), dispLocX, textYPos += lineWidth, hudDistance);
		}
		
		FloatList clusterTimes = p.getCurrentCluster().getClusterTimes();
		
		if(clusterTimes.size() == 0)
			p.text("No timeline!", textXPos, textYPos += lineWidth, hudDistance);
		else
			p.text("Timeline:", textXPos , textYPos += lineWidth, hudDistance);

		if(clusterTimes.size() == 1)
			p.text(clusterTimes.get(0), textXPos + 50.f, textYPos, hudDistance);
		if(clusterTimes.size() == 2)
			p.text(clusterTimes.get(0)+" "+clusterTimes.get(1), textXPos + 100.f, textYPos, hudDistance);
		if(clusterTimes.size() >= 3)
			p.text(clusterTimes.get(0)+" "+clusterTimes.get(1)+" "+clusterTimes.get(2), textXPos + 150.f, textYPos, hudDistance);
				
		p.text(" ", textXPos, textYPos += lineWidth, hudDistance);
		p.text(" ", textXPos, textYPos += lineWidth, hudDistance);

		c = p.getCurrentCluster();
		p.text(" Current Cluster ID: "+p.viewer.getCurrentCluster(), textXPos, textYPos += lineWidthVeryWide, hudDistance);
		p.text("   Media Points: "+c.mediaPoints, textXPos, textYPos += lineWidth, hudDistance);
		p.text("   Viewer Distance: "+PApplet.round(PVector.dist(c.getLocation(), p.viewer.getLocation())), textXPos, textYPos += lineWidth, hudDistance);
		if(p.viewer.getAttractorCluster() != -1)
		{
			p.text(" Destination Cluster ID: "+p.viewer.getAttractorCluster(), textXPos, textYPos += lineWidth, hudDistance);
//			p.text(" Attractor Cluster Media Points: "+f.clusters.get(p.viewer.getAttractorCluster()).mediaPoints, dispLocX, textYPos += lineWidth, hudDistance);
			p.text("    Destination Distance: "+PApplet.round( PVector.dist(f.clusters.get(p.viewer.getAttractorCluster()).getLocation(), p.viewer.getLocation() )), textXPos, textYPos += lineWidth, hudDistance);
			if(p.debug.viewer) 
			{
				p.text(" Debug: Current Attraction:"+p.viewer.attraction.mag(), textXPos, textYPos += lineWidth, hudDistance);
				p.text(" Debug: Current Acceleration:"+(p.viewer.isWalking() ? p.viewer.walkingAcceleration.mag() : p.viewer.acceleration.mag()), textXPos, textYPos += lineWidth, hudDistance);
				p.text(" Debug: Current Velocity:"+ (p.viewer.isWalking() ? p.viewer.walkingVelocity.mag() : p.viewer.velocity.mag()) , textXPos, textYPos += lineWidth, hudDistance);
			}
		}

		p.popMatrix();
	}
	
	public boolean inDisplayView()
	{
		if( map || control || info || about || cluster || p.interactive ||
		mapOverlay || controlOverlay || infoOverlay || clusterOverlay )
		{
			return true;
		}
		else return false;
	}

//	p.text("   Viewer Distance: "+PApplet.round(PVector.dist(c.getLocation(), p.viewer.getLocation())), dispLocX, textYPos += lineWidth, hudDistance);
//	if(p.viewer.getAttractorCluster() != -1)
//	{
//		p.text(" Destination Cluster : "+p.viewer.getAttractorCluster(), dispLocX, textYPos += lineWidth, hudDistance);
//		//		p.text(" Attractor Cluster Media Points: "+f.clusters.get(p.viewer.getAttractorCluster()).mediaPoints, dispLocX, textYPos += lineWidth, hudDistance);
//		p.text("    Destination Distance: "+PApplet.round( PVector.dist(f.clusters.get(p.viewer.getAttractorCluster()).getLocation(), p.viewer.getLocation() )), dispLocX, textYPos += lineWidth, hudDistance);
//		if(p.debug.viewer) 
//		{
//			p.text(" Debug: Current Attraction:"+p.viewer.attraction.mag(), dispLocX, textYPos += lineWidth, hudDistance);
//			p.text(" Debug: Current Acceleration:"+(p.viewer.walking ? p.viewer.walkingAcceleration.mag() : p.viewer.acceleration.mag()), dispLocX, textYPos += lineWidth, hudDistance);
//			p.text(" Debug: Current Velocity:"+ (p.viewer.walking ? p.viewer.walkingVelocity.mag() : p.viewer.velocity.mag()) , dispLocX, textYPos += lineWidth, hudDistance);
//		}
//	}

//	public boolean isActive()
//	{
//		return active;
//	}
//	
//	public boolean isEmpty()
//	{
//		return empty;
//	}
//	
//	public boolean isSingle()
//	{
//		return single;
//	}
//	
//	void setMass(float newMass)
//	{
//		clusterMass = newMass;
//	}
//	
//	/**
//	 * setAttractor()
//	 * Set this cluster as an attractor
//	 */
//	void setAttractor(boolean state)
//	{
//		isAttractor = state;
//	}
//
//	public boolean isAttractor()
//	{
//		return isAttractor;
//	}
//	
//	public float getClusterMass()
//	{
//		return clusterMass;
//	}
//	

	/**
	 * drawSmallMap()
	 * Draw Heads-Up Display of 2D Map and Logo
	 */
	void drawSmallMap()
	{
//		float textYPos = logoYOffset;			// Starting vertical position
//
//		p.pushMatrix();
//		beginHUD();									// Begin 2D drawing
//		
//		p.fill(55, 0, 255, 255);
//		p.textSize(largeTextSize);
//
////		p.text("GeoMultimediaViewer v1.0", logoXOffset, textYPos, hudDistance);
//		p.text("3D Multimedia Display System Using Metadata", logoXOffset, textYPos += lineWidth, hudDistance);
//		p.text("by David Gordon", logoXOffset, textYPos += lineWidth, hudDistance);
//
//		p.popMatrix();
//
//		drawMap(smallMapWidth, smallMapHeight, smallMapXOffset, smallMapYOffset);
	}
}

