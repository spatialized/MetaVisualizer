package wmViewer;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;
import toxi.math.ScaleMap;

/***********************************
 * @author davidgordon
 * Class for displaying interactive 2D map of media-based virtual environment
 */

public class WMV_Map 
{
	/* Map Modes */
	int mapMode = 1;							// 	1:  All   2: Clusters + Media   3: Clusters + Capture Locations  4: Capture Locations + Media
												//	5:  Clusters Only   6: Media Only   7: Capture Locations Only

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

	PVector mapVectorOrigin, mapVectorVector;

	WMV_Display p;

	WMV_Map(WMV_Display parent)
	{
		p = parent;

		largeMapXOffset = -p.p.p.width * 0.5f;
		largeMapYOffset = -p.p.p.height * 0.5f;
		largeMapMaxWidth = p.p.p.width * 0.95f;
		largeMapMaxHeight = p.p.p.height * 0.95f;

		smallMapMaxWidth = p.p.p.width / 4.f;
		smallMapMaxHeight = p.p.p.height / 4.f;

		logoXOffset = p.p.p.width - p.p.p.width / 3.f;
		logoYOffset = p.p.p.height / 2.5f;
		

		smallPointSize = 0.0000022f * p.p.p.width;
		mediumPointSize = 0.0000028f * p.p.p.width;
		largePointSize = 0.0000032f * p.p.p.width;
		hugePointSize = 0.0000039f * p.p.p.width;
		cameraPointSize = 0.004f * p.p.p.width;
	}


	void initializeLargeMap()
	{
		float fr = p.p.getCurrentField().model.fieldAspectRatio;			//	Field ratio == fieldWidth / fieldLength;

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
		initializeLargeMap();
	}
	
	/**
	 * Draw large 2D map
	 */
	void drawLargeMap()
	{
		p.p.p.pushMatrix();
		p.beginHUD();									// Begin 2D drawing
		
		p.p.p.fill(55, 0, 255, 255);
		p.p.p.textSize(p.largeTextSize);
		float textXPos = p.centerTextXOffset;
		float textYPos = p.topTextYOffset;

		if(p.p.interactive)
		{
			p.p.p.text("Interactive "+(p.p.hierarchical ? "Hierarchical" : "K-Means")+" Clustering", textXPos, textYPos, p.hudDistance);
		}
		else
			p.p.p.text(p.p.getCurrentField().name, textXPos, textYPos, p.hudDistance);

		p.p.p.popMatrix();
		
		drawMap(largeMapWidth, largeMapHeight, largeMapXOffset, largeMapYOffset);
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
		if((mapMode == 1 || mapMode == 2 || mapMode == 4 || mapMode == 6) && mapImages && !p.p.getCurrentField().hideImages)
			for ( WMV_Image i : p.p.getCurrentField().images )		// Draw images on 2D Map
				drawImageOnMap(i, mapWidth, mapHeight, false);

		if((mapMode == 1 || mapMode == 2 || mapMode == 4 || mapMode == 6) && mapPanoramas && !p.p.getCurrentField().hidePanoramas)
			for ( WMV_Panorama n : p.p.getCurrentField().panoramas )	// Draw panoramas on 2D Map
				drawPanoramaOnMap(n, mapWidth, mapHeight, false);

		if((mapMode == 1 || mapMode == 2 || mapMode == 4 || mapMode == 6) && mapVideos && !p.p.getCurrentField().hideVideos)
			for (WMV_Video v : p.p.getCurrentField().videos)			// Draw videos on 2D Map
				drawVideoOnMap(v, mapWidth, mapHeight, false);

		if((mapMode == 1 || mapMode == 3 || mapMode == 4 || mapMode == 7) && mapImages && !p.p.getCurrentField().hideImages)
			for ( WMV_Image i : p.p.getCurrentField().images )		// Draw image capture locations on 2D Map
				drawImageOnMap(i, mapWidth, mapHeight, true);

		if((mapMode == 1 || mapMode == 3 || mapMode == 4 || mapMode == 7) && mapPanoramas && !p.p.getCurrentField().hidePanoramas)
			for ( WMV_Panorama n : p.p.getCurrentField().panoramas )	// Draw panorama capture locations on 2D Map
				drawPanoramaOnMap(n, mapWidth, mapHeight, true);

		if((mapMode == 1 || mapMode == 3 || mapMode == 4 || mapMode == 7) && mapVideos && !p.p.getCurrentField().hideVideos)
			for (WMV_Video v : p.p.getCurrentField().videos)			// Draw video capture locations on 2D Map
				drawVideoOnMap(v, mapWidth, mapHeight, true);

		/* Clusters */
		if((mapMode == 1 || mapMode == 2 || mapMode == 3 || mapMode == 5) && mapClusters)
			for( WMV_Cluster c : p.p.getCurrentField().clusters )							
				drawFuzzyMapPoint( c.getLocation(), PApplet.sqrt(c.mediaPoints) * 0.5f, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, mapMediaTransparency );

		if(!p.p.interactive)				// While not in Clustering Mode
		{
			if(mapMode == 1 || mapMode == 2 || mapMode == 3 || mapMode == 5)
			{
				if(p.p.viewer.getAttractorCluster() != -1 && p.p.viewer.getAttractorCluster() < p.p.getFieldClusters().size())
					drawMapPoint( p.p.getAttractorCluster().getLocation(), hugePointSize * mapWidth, mapWidth, mapHeight, mapAttractorClusterHue, 255.f, 255.f, mapMediaTransparency );

				if(p.p.viewer.getCurrentCluster() != -1 && p.p.viewer.getCurrentCluster() < p.p.getFieldClusters().size())
					drawMapPoint( p.p.getCurrentCluster().getLocation(), hugePointSize * mapWidth, mapWidth, mapHeight, mapAttractorClusterHue, 255.f, 255.f, mapMediaTransparency );
			}
			
			drawCameraOnMap(mapWidth, mapHeight);
		}
		
//		if(p.p.viewer.getPath().size() > 0)
//			drawPathOnMap(p.p.viewer.getPath(), mapWidth, mapHeight);
		if(p.p.viewer.getGPSTrack().size() > 0)
			drawPathOnMap(p.p.viewer.getGPSTrack(), mapWidth, mapHeight);
			
//		drawOriginOnMap(mapWidth, mapHeight);
	}

	/**
	 * @param image GMV_Image to draw
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @param capture Draw capture location (true) or viewing location (false)
	 * Draw image location on map of specified size
	 */
	void drawImageOnMap(WMV_Image image, float mapWidth, float mapHeight, boolean capture)
	{
		float pointSize = smallPointSize * mapWidth;
		
		float saturation;

		float imageDistance = image.getViewingDistance();   // Get photo distance from current camera position

		// IMPLEMENT TIME
		// p.p.fill(mapVideoHue, 100, 255, 255);                         // If out of visible range and not at a visible time, lightest color
		// p.p.p.stroke(mapVideoHue, 100, 255, 255);                                        

		if (imageDistance < p.p.viewer.getFarViewingDistance() && imageDistance > p.p.viewer.getNearClippingDistance())    // If image is in visible range
			saturation = 100.f;                                              
		else      																				// If out of visible range
			saturation = maxSaturation;                                              

		if(image.location != null && !image.disabled && !image.hidden)
		{
			if(capture)
				drawMapPoint( image.getCaptureLocation(), pointSize * (image.isSelected() ? 10.f : 1.f), mapWidth, mapHeight, mapImageCaptureHue, saturation, 255.f, mapMediaTransparency );
			else
				drawMapPoint( image.getLocation(), pointSize, mapWidth, mapHeight, mapImageHue, saturation, 255.f, mapMediaTransparency );
		}
		
	}

	/**
	 * @param panorama GMV_Panorama to draw
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * Draw image location on map of specified size
	 */
	void drawPanoramaOnMap(WMV_Panorama panorama, float mapWidth, float mapHeight, boolean capture)
	{
		float pointSize = mediumPointSize * mapWidth;
		
		float saturation = 255.f;
		float panoramaDistance = panorama.getViewingDistance();   // Get photo distance from current camera position

		// IMPLEMENT TIME
		// p.p.fill(mapVideoHue, 100, 255, 255);                         // If out of visible range and not at a visible time, lightest color
		// p.p.p.stroke(mapVideoHue, 100, 255, 255);                                        

		if (panoramaDistance < p.p.viewer.getFarViewingDistance() && panoramaDistance > p.p.viewer.getNearClippingDistance())    // If panorama is in visible range
			saturation = 100.f;                                              
		else      																				// If out of visible range
			saturation = maxSaturation;                                              

		if(panorama.location != null && !panorama.disabled && !panorama.hidden)
		{
			if(capture)
				drawMapPoint( panorama.getCaptureLocation(), pointSize * (panorama.isSelected() ? 10.f : 1.f), mapWidth, mapHeight, mapPanoramaCaptureHue, saturation, 255.f, mapMediaTransparency );
			else
				drawMapPoint( panorama.getLocation(), pointSize, mapWidth, mapHeight, mapPanoramaHue, saturation, 255.f, mapMediaTransparency );
		}
	}

	/**
	 * @param video GMV_Video to draw
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * Draw image location on map of specified size
	 */
	void drawVideoOnMap(WMV_Video video, float mapWidth, float mapHeight, boolean capture)
	{
		float pointSize = mediumPointSize * mapWidth;
		
		float saturation;
		float videoDistance = video.getViewingDistance();   // Get photo distance from current camera position

		// IMPLEMENT TIME
		// p.p.fill(mapVideoHue, 100, 255, 255);                         // If out of visible range and not at a visible time, lightest color
		// p.p.p.stroke(mapVideoHue, 100, 255, 255);                                        

		if (videoDistance < p.p.viewer.getFarViewingDistance() && videoDistance > p.p.viewer.getNearClippingDistance())    // If video is in visible range
			saturation = minSaturation;                                              
		else      																				// If out of visible range
			saturation = maxSaturation;                                              

		if(video.location != null && !video.disabled && !video.hidden)
		{
			if(capture)
				drawMapPoint( video.getCaptureLocation(), pointSize * (video.isSelected() ? 10.f : 1.f), mapWidth, mapHeight, mapVideoCaptureHue, saturation, 255.f, mapMediaTransparency );
			else
				drawMapPoint( video.getLocation(), pointSize, mapWidth, mapHeight, mapVideoHue, saturation, 255.f, mapMediaTransparency );
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
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * Draw current viewer location and orientation on map of specified size
	 */
	void drawCameraOnMap(float mapWidth, float mapHeight)
	{
		PVector camLoc = p.p.viewer.getLocation();
		float camYaw = -p.p.viewer.getXOrientation() - 0.5f * PApplet.PI;

		drawMapPoint( camLoc, cameraPointSize, mapWidth, mapHeight, mapCameraHue, 255.f, 255.f, mapMediaTransparency );
		float ptSize = cameraPointSize;
				
		ScaleMap logMap;
		logMap = new ScaleMap(6., 60., 6., 60.);		/* Time fading interpolation */
		logMap.setMapFunction(p.p.circularEaseOut);

		/* Change viewer arrow based on fieldWidth -- should be fieldLength??  -- should depend on zoom level too! */
//		int arrowLength = (int)logMap.p.getMappedValueFor( PApplet.map( p.p.getCurrentField().model.fieldWidth, 100.f, 10000.f, 6.f, 60.f ) );
		int arrowLength = 30;
		
		logMap = new ScaleMap(0.f, 0.25f, 0.f, 0.25f);		/* Time fading interpolation */
		logMap.setMapFunction(p.p.circularEaseOut);
		
		float shrinkFactor = PApplet.map(arrowLength, 6.f, 60.f, 0.f, 0.25f);
		shrinkFactor = (float)logMap.getMappedValueFor(shrinkFactor);
		shrinkFactor = 0.95f - (0.25f - shrinkFactor);		// Reverse mapping high to low
				
		for(int i=1; i<arrowLength; i++)
		{
			p.p.p.textSize(ptSize);
			float x = i * cameraPointSize * 0.5f * (float)Math.cos( camYaw );
			float y = i * cameraPointSize * 0.5f * (float)Math.sin( camYaw );
			
			PVector arrowPoint = new PVector(camLoc.x + x, 0, camLoc.z + y);
			drawMapPoint( arrowPoint, ptSize, mapWidth, mapHeight, mapCameraHue, 120.f, 255.f, 255.f );

			ptSize *= shrinkFactor;
		}
	}
	

	/**
	 * @param path Path to draw
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @param capture Draw capture location (true) or viewing location (false)
	 * Draw image location on map of specified size
	 */
	void drawPathOnMap(ArrayList<WMV_Waypoint> path, float mapWidth, float mapHeight)
	{
		PApplet.println("drawPathOnMap..."+path.size());
		float pointSize = smallPointSize * mapWidth;
		
		float saturation = maxSaturation;                                              

		for(WMV_Waypoint w : path)
		{
			drawMapPoint( w.getLocation(), pointSize * 4.f, mapWidth, mapHeight, 30, saturation, 255.f, mapMediaTransparency );
			PApplet.println("Path ---> location.x:"+w.getLocation().x+" y:"+w.getLocation().y);
		}
	}

	
	/**
	 * @param origin Vector starting point
	 * @param vector Direction vector
	 * Draw current viewer location and orientation on map of specified size
	 */
	void drawForceVector(PVector vector)
	{
		if(!p.drawForceVector)
			p.drawForceVector = true;

//		mapVectorOrigin = origin;
		mapVectorVector = vector;
	}

	/**
	 * Draw the clusters at given depth
	 */
	void drawGMVClusters()
	{		 
		if(  !p.initialSetup && !p.mapOverlay && !p.controlOverlay && !p.info 
				&& p.messages.size() < 0 && p.metadata.size() < 0	 )
		{
			p.p.p.hint(PApplet.DISABLE_DEPTH_TEST);						// Disable depth testing for drawing HUD
		}

		for( WMV_Cluster c : p.p.getCurrentField().clusters )								// For all clusters at current depth
		{
			drawMapPoint( c.getLocation(), 5.f, largeMapWidth, largeMapHeight, mapClusterHue, 255.f, 255.f, mapMediaTransparency );
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
		WMV_Model m = p.p.getCurrentField().model;

		if(!p.p.p.utilities.isNaN(point.x) && !p.p.p.utilities.isNaN(point.y) && !p.p.p.utilities.isNaN(point.z))
		{
			/* Find 2D map coordinates for this image */
			mapLocX = PApplet.map( point.x, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, 0, mapWidth * mapZoom );		
			mapLocY = PApplet.map( point.z, -0.5f * m.fieldLength, 0.5f*m.fieldLength, 0, mapHeight * mapZoom );

			if(mapLocX < mapWidth && mapLocX > 0 && mapLocY < mapHeight && mapLocY > 0)
			{
				p.p.p.stroke(hue, saturation, brightness, transparency);
				p.p.p.pushMatrix();
				p.beginHUD();

				p.p.p.strokeWeight(pointSize);
				p.p.p.translate(mapLeftEdge, mapTopEdge);
				p.p.p.point(largeMapXOffset + mapLocX, largeMapYOffset + mapLocY, p.hudDistance);

				p.p.p.popMatrix();
			}
		}
		else p.message("Map point is NaN!:"+point+" hue:"+hue);
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
	 * Draw camera attraction force vector
	 */
	public void drawForceVector()
	{
		mapVectorOrigin = p.p.viewer.getLocation();
		
		float ptSize = cameraPointSize * 2.f;
		drawMapPoint( mapVectorOrigin, ptSize, largeMapWidth, largeMapHeight, mapCameraHue, 255.f, 255.f, mapMediaTransparency );
		
		int arrowLength = 30;
		
		ScaleMap logMap;
		logMap = new ScaleMap(6., 60., 6., 60.);		/* Time fading interpolation */
		logMap.setMapFunction(p.p.circularEaseOut);

//		arrowLength = (int)logMap.p.getMappedValueFor(arrowLength);
		
		logMap = new ScaleMap(0.f, 0.25f, 0.f, 0.25f);		/* Time fading interpolation */
		logMap.setMapFunction(p.p.circularEaseOut);
		
		float shrinkFactor = PApplet.map(arrowLength, 6.f, 60.f, 0.f, 0.25f);
		shrinkFactor = (float)logMap.getMappedValueFor(shrinkFactor);
		shrinkFactor = 0.95f - (0.25f - shrinkFactor);		// Reverse mapping high to low
		PVector current = mapVectorVector;
		
		for(int i=1; i<arrowLength; i++)
		{
			p.p.p.textSize(ptSize);
			float mult = (i+6) * 0.025f;
			current = mapVectorVector.mult( mult );
			
			PVector arrowPoint = new PVector(mapVectorOrigin.x + current.x, 0, mapVectorOrigin.z + current.z);
			drawMapPoint( arrowPoint, ptSize, largeMapWidth, largeMapHeight, 255.f-mapCameraHue, 170.f, 255.f, 255.f );

			ptSize *= shrinkFactor;
		}
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
}
