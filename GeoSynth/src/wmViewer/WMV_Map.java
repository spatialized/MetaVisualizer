package wmViewer;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.data.IntList;
import toxi.math.ScaleMap;
import shapes3d.*;

/***********************************
 * @author davidgordon
 * Class for displaying interactive 2D map of media-based virtual environment
 */

public class WMV_Map 
{
	/* Interaction */
	private int selectedCluster = -1;
	ArrayList<Ellipsoid> selectableClusters;
	IntList selectedClusterIDs;
	
	/* Graphics */
	PGraphics simpleClusters;
	private boolean simpleClustersCreated = false;

	public float mapDistance = 1.f;
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

	private float fieldRatio;
	
	float largeMapWidth, largeMapHeight;
	float largeMapXOffset, largeMapYOffset;
	float largeMapZoomLevel = 1.f;

	float zoomMapLeftEdge = 0.f, zoomMapTopEdge = 0.f;
	float zoomMapWidth, zoomMapHeight, zoomMapMaxWidth, zoomMapMaxHeight;
	float zoomMapXOffset, zoomMapYOffset;

	float smallMapWidth, smallMapHeight, smallMapMaxWidth, smallMapMaxHeight;
	float smallMapXOffset, smallMapYOffset;

	float smallPointSize;
	float mediumPointSize;
	float largePointSize;
	float hugePointSize;
	float cameraPointSize;

	public boolean beginZoomTransition = false, zoomTransition = false;
	private int zoomTransitionStartFrame = 0, zoomTransitionEndFrame = 0, zoomTransitionLength = 14;	
	private float zoomTransitionStart, zoomTransitionTarget;

	public boolean beginScrollTransition = false, scrollTransition = false;
	private int scrollTransitionStartFrame = 0, scrollTransitionEndFrame = 0, scrollTransitionLength = 16;	
	private float mapXTransitionStart, mapXTransitionTarget, mapYTransitionStart, mapYTransitionTarget;

	PVector mapVectorOrigin, mapVectorVector;

	WMV_Display p;

	WMV_Map(WMV_Display parent)
	{
		p = parent;

		largeMapXOffset = -p.p.p.width * 0.5f;
		largeMapYOffset = -p.p.p.height * 0.5f;
//		largeMapWidth = p.p.p.height * 0.95f;
//		largeMapHeight = p.p.p.height * 0.95f;

		smallMapMaxWidth = p.p.p.width / 4.f;
		smallMapMaxHeight = p.p.p.height / 4.f;

		smallPointSize = 0.0000022f * p.p.p.width;
		mediumPointSize = 0.0000028f * p.p.p.width;
		largePointSize = 0.0000032f * p.p.p.width;
		hugePointSize = 0.0000039f * p.p.p.width;
		cameraPointSize = 0.005f * p.p.p.width;
	}

	
	void initializeLargeMap()
	{
		fieldRatio = p.p.getCurrentField().model.fieldAspectRatio;			//	Field ratio == fieldWidth / fieldLength;
		
//		largeMapWidth = p.p.p.height * 0.95f;
//		largeMapHeight = p.p.p.height * 0.95f;
		
		if(fieldRatio >= 1.f)									
		{
			largeMapWidth = p.p.p.width * 0.95f;
//			largeMapHeight = p.p.p.width * 0.95f;
			largeMapHeight = p.p.p.width / fieldRatio;
		}
		else
		{
			largeMapWidth = p.p.p.height * fieldRatio;
//			largeMapWidth = p.p.p.height * 0.95f;
			largeMapHeight = p.p.p.height * 0.95f;
		}

//		largeMapXOffset = -p.p.p.width * 0.5f;
//		largeMapYOffset = -p.p.p.height * 0.5f;

//		WMV_Model m = p.p.getCurrentField().model;

//		WMV_Cluster c = p.p.getCurrentField().getEdgeClusterOnZAxis(true);
//		if(fieldRatio >= 1.f)												
//		{
//			c = p.p.getCurrentField().getEdgeClusterOnZAxis(true);
//			PVector point = getMapLocation(c.getLocation(), largeMapWidth, largeMapHeight);
//			mapTopEdge = point.y;
//			PApplet.println("new mapTopEdge:"+mapTopEdge +" largeMapWidth:"+largeMapWidth+" largeMapHeight:"+largeMapHeight);
//			c = p.p.getCurrentField().getEdgeClusterOnZAxis(false);
//			point = getMapLocation(c.getLocation(), largeMapWidth, largeMapHeight);
//			PApplet.println("lowZ:"+point.y);
//		}
//		else
//		{
//			c = p.p.getCurrentField().getEdgeClusterOnXAxis(true);
//			PVector point = getMapLocation(c.getLocation(), largeMapWidth, largeMapHeight);
//			mapLeftEdge = point.x;
//			PApplet.println("new mapLeftEdge:"+mapLeftEdge +" largeMapWidth:"+largeMapWidth+" largeMapHeight:"+largeMapHeight);
//			PApplet.println("highX:"+point.y);
//		}
		
//		p.p.p.translate(mapLeftEdge, mapTopEdge);
//		p.p.p.translate(largeMapXOffset, largeMapYOffset);
//		p.p.p.point(point.x, point.y, p.hudDistance * mapDistance);

		
//		float newX = PApplet.map( m.lowLongitude, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, m.lowLongitude, m.highLongitude ); 			// GPS longitude decreases from left to right
//		float newY = -PApplet.map( m.lowLatitude, -0.5f * m.fieldLength, 0.5f*m.fieldLength, m.lowLatitude, m.highLatitude ); 			// GPS latitude increases from bottom to top; negative to match P3D coordinate space
		
//		float mapLocX, mapLocY, mapLocZ;
//		PVector point = c.getLocation();
//		
//		
//		PVector mapLoc = new PVector(mapLocX, mapLocY, mapLocZ);
//		
//		if( mapLoc.x > 0 && mapLoc.x < zoomMapWidth && mapLoc.y > 0 && mapLoc.y < zoomMapHeight )
//		{
//			mapLoc.add(new PVector(mapLeftEdge, mapTopEdge, 0));
//			mapLoc.add(new PVector(largeMapXOffset, largeMapYOffset, p.hudDistance * mapDistance));
//			ellipsoid.moveTo(mapLoc.x, mapLoc.y, mapLoc.z);
//			ellipsoid.tagNo = c.getID();
//			selectableClusters.add(ellipsoid);
//		}
//		
//		PVector upperLeft = new PVector(newX, newY);
//		
//		largeMapXOffset = upperLeft.x;
//		largeMapYOffset = upperLeft.y;

//		newX = PApplet.map( m.highLongitude, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, m.lowLongitude, m.highLongitude ); 			// GPS longitude decreases from left to right
//		newY = -PApplet.map( m.highLatitude, -0.5f * m.fieldLength, 0.5f*m.fieldLength, m.lowLatitude, m.highLatitude ); 			// GPS latitude increases from bottom to top; negative to match P3D coordinate space
//		PVector lowerRight = new PVector(newX, newY);

//		public PVector getGPSLocation()			// Working??
//		{
//			PVector vLoc;
////			if(p.orientationMode)
////				vLoc = location;
////			else
//				vLoc = new PVector(camera.position()[0], camera.position()[1], camera.position()[2]);			// Update location
//			
//			WMV_Model m = p.getCurrentField().model;
//			
//			float newX = PApplet.map( vLoc.x, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, m.lowLongitude, m.highLongitude ); 			// GPS longitude decreases from left to right
//			float newY = -PApplet.map( vLoc.z, -0.5f * m.fieldLength, 0.5f*m.fieldLength, m.lowLatitude, m.highLatitude ); 			// GPS latitude increases from bottom to top; negative to match P3D coordinate space
//
//			PVector gpsLoc = new PVector(newX, newY);
//
//			return gpsLoc;
//		}

		zoomToRectangle(0, 0, largeMapWidth, largeMapHeight);			// Start zoomed out on whole map
	}

	void initializeMaps()
	{
		initializeLargeMap();
		p.initializedMaps = true;
	}
	
	void reset()
	{
		
	}
	
	/**
	 * Create selectable circle representing each cluster
	 * @param mapWidth
	 * @param mapHeight
	 */
	void createSelectableClusters(float mapWidth, float mapHeight)
	{
		selectableClusters = new ArrayList<Ellipsoid>();
		selectedClusterIDs = new IntList();
		
		for( WMV_Cluster c : p.p.getCurrentField().clusters )	
		{
			if(!c.isEmpty() && c.mediaPoints != 0)
			{
				PVector mapLoc = getMapLocation(c.getLocation(), mapWidth, mapHeight);
				if( pointIsVisible(mapLoc, true) )
				{
					Ellipsoid ellipsoid = new Ellipsoid(p.p.p, 5, 5);
					float radius = PApplet.sqrt(c.mediaPoints) * 0.7f;
					radius *= mapDistance;
					ellipsoid.setRadius(radius);
					ellipsoid.drawMode(S3D.SOLID);
					ellipsoid.fill(p.p.p.color(105.f, 225.f, 200.f, mapMediaTransparency));
					ellipsoid.fill(p.p.p.color(105.f, 225.f, 200.f, 255.f));
					ellipsoid.strokeWeight(0.f);

					mapLoc.add(new PVector(largeMapXOffset, largeMapYOffset, p.hudDistance * mapDistance));
					mapLoc.add(new PVector(mapLeftEdge, mapTopEdge, 0));
					ellipsoid.moveTo(mapLoc.x, mapLoc.y, mapLoc.z);
					ellipsoid.tagNo = c.getID();
					selectableClusters.add(ellipsoid);
					selectedClusterIDs.append(c.getID());
				}
			}
		}
		
		PApplet.println("==> selectableClusters SIZE:"+selectableClusters.size());
		for(Ellipsoid e : selectableClusters)
		{
			PApplet.println(e.tagNo+" at x:"+e.x()+" y:"+e.y()+" z:"+e.z()+" / ");
		}
	}
	
	PVector getMapLocation(PVector point, float mapWidth, float mapHeight)
	{
		WMV_Model m = p.p.getCurrentField().model;
		float mapLocX, mapLocY;

		if(fieldRatio >= 1.f)					
		{
			mapLocX = PApplet.map( point.x, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, 0, mapWidth );		
			mapLocY = PApplet.map( point.z, -0.5f * m.fieldLength, 0.5f*m.fieldLength, 0, mapWidth / fieldRatio );
		}
		else
		{
			mapLocX = PApplet.map( point.x, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, 0, mapHeight * fieldRatio );		
			mapLocY = PApplet.map( point.z, -0.5f * m.fieldLength, 0.5f*m.fieldLength, 0, mapHeight );
		}

		return new PVector(mapLocX, mapLocY, 0.f);
	}
	
	/**
	 * Check if a location is visible on map
	 * @param point The location to check
	 * @param mapCoords Whether the point is in map or in virtual world coordinates (m.)
	 * @return Whether the location is visible on map
	 */
	boolean pointIsVisible(PVector loc, boolean mapCoords)
	{
		PVector point = new PVector(loc.x, loc.y, loc.z);
		
		float xOff = zoomMapXOffset - largeMapXOffset;
		float yOff = zoomMapYOffset - largeMapYOffset;
		
		if(!mapCoords)														// Convert given world coords to map coords
			point = getMapLocation(loc, largeMapWidth, largeMapHeight);

		if( point.x > xOff && point.x < zoomMapWidth + xOff && point.y > yOff && point.y < zoomMapHeight + yOff )
			return true;
		else
			return false;
	}
	
	void drawSelectableClusters()
	{
		p.p.p.pushMatrix();
		for(Ellipsoid e : selectableClusters)
			e.draw();
		p.p.p.popMatrix();
	}
	
	/**
	 * Draw large map
	 */
	void drawLargeMap()
	{
		p.p.p.pushMatrix();
		p.beginHUD();											
		
		p.p.p.fill(55, 0, 255, 255);
		p.p.p.textSize(p.largeTextSize);
		float textXPos = p.centerTextXOffset;
		float textYPos = p.topTextYOffset;

		if(p.p.interactive)
			p.p.p.text("Interactive "+(p.p.hierarchical ? "Hierarchical" : "K-Means")+" Clustering", textXPos, textYPos, p.hudDistance);
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
		if(!zoomTransition && !scrollTransition)
			createSelectableClusters(mapWidth, mapHeight);
		
		p.beginHUD();

		/* Media */
		if(!zoomTransition && !scrollTransition)
		{
			if((mapImages && !p.p.getCurrentField().hideImages))
				for ( WMV_Image i : p.p.getCurrentField().images )					// Draw image capture locations on 2D Map
					drawImageOnMap(i, false, mapWidth, mapHeight, false);

			if((mapPanoramas && !p.p.getCurrentField().hidePanoramas))
				for ( WMV_Panorama n : p.p.getCurrentField().panoramas )			// Draw panorama capture locations on 2D Map
					drawPanoramaOnMap(n, false, mapWidth, mapHeight, false);

			if((mapVideos && !p.p.getCurrentField().hideVideos))
				for (WMV_Video v : p.p.getCurrentField().videos)					// Draw video capture locations on 2D Map
					drawVideoOnMap(v, false, mapWidth, mapHeight, false);
		}

		/* Clusters */
		if(mapClusters)
		{
			if(!zoomTransition && !scrollTransition)
			{
				drawSelectableClusters();
				
				if(selectedCluster >= 0)
				{
					WMV_Cluster c = p.p.getCluster(selectedCluster);
					if(!c.isEmpty() && c.mediaPoints != 0)
						highlightCluster( c.getLocation(), PApplet.sqrt(c.mediaPoints)*0.5f, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, mapMediaTransparency );
					
					if((mapImages && !p.p.getCurrentField().hideImages))
						for ( int i : c.images )									// Draw images on Map
						{
							WMV_Image img = p.p.getCurrentField().images.get(i);
							drawImageOnMap(img, true, mapWidth, mapHeight, false);
						}

					if((mapPanoramas && !p.p.getCurrentField().hidePanoramas))
						for ( int n : c.panoramas )									// Draw panoramas on Map
						{
							WMV_Panorama pano = p.p.getCurrentField().panoramas.get(n);
							drawPanoramaOnMap(pano, true, mapWidth, mapHeight, false);
						}

					if((mapVideos && !p.p.getCurrentField().hideVideos))
						for (int v : c.videos)										// Draw videos on Map
						{
							WMV_Video vid = p.p.getCurrentField().videos.get(v);
							drawVideoOnMap(vid, true, mapWidth, mapHeight, false);
						}
				}
			}
			else
			{
				if(simpleClustersCreated)
				{
					drawSimpleClusters(mapWidth, mapHeight);
				}
				else
				{
//					createSimpleClusters(mapWidth, mapHeight);
					drawSimpleClusters(mapWidth, mapHeight);					
				}
			}
		}
		
		if(!zoomTransition && !scrollTransition)
		{
			if(!p.p.interactive)													// While not in Clustering Mode
			{
				if(p.p.viewer.getAttractorCluster() != -1 && p.p.viewer.getAttractorCluster() < p.p.getFieldClusters().size())
					drawPoint( p.p.getAttractorCluster().getLocation(), hugePointSize * mapWidth, mapWidth, mapHeight, mapAttractorClusterHue, 255.f, 255.f, mapMediaTransparency );

				if(p.p.viewer.getCurrentCluster() != -1 && p.p.viewer.getCurrentCluster() < p.p.getFieldClusters().size())
					drawPoint( p.p.getCurrentCluster().getLocation(), hugePointSize * mapWidth, mapWidth, mapHeight, mapAttractorClusterHue, 255.f, 255.f, mapMediaTransparency );

				drawCameraOnMap(mapWidth, mapHeight);
			}

			if(p.p.viewer.getGPSTrack().size() > 0)
				drawPathOnMap(p.p.viewer.getGPSTrack(), mapWidth, mapHeight);
			
			if(p.p.p.debug.map)
				drawMapBorder(mapWidth, mapHeight);
			
//			drawOriginOnMap(mapWidth, mapHeight);
		}
	}
	
	void createSimpleClusters(float mapWidth, float mapHeight)
	{
		int pgWidth = PApplet.round(mapWidth);
		int pgHeight = PApplet.round(mapHeight);

		simpleClusters = p.p.p.createGraphics(pgWidth, pgHeight, PApplet.P3D);
		
		for( WMV_Cluster c : p.p.getCurrentField().clusters )	
			if(!c.isEmpty() && c.mediaPoints > 4)
			{
				WMV_Model m = p.p.getCurrentField().model;
				PVector point = c.getLocation();
				
				if(!p.p.p.utilities.isNaN(point.x) && !p.p.p.utilities.isNaN(point.y) && !p.p.p.utilities.isNaN(point.z))
				{
					PVector mapLoc = getMapLocation(point, mapWidth, mapHeight);

					if(mapLoc.x < mapWidth && mapLoc.x > 0 && mapLoc.y < mapHeight && mapLoc.y > 0)
					{
						simpleClusters.stroke(mapClusterHue, 255.f, 255.f, 255.f);
						float sw = PApplet.sqrt(c.mediaPoints) * 0.5f;
						sw *= mapDistance;
						simpleClusters.strokeWeight(sw);

						simpleClusters.beginDraw();
						simpleClusters.pushMatrix();
						simpleClusters.point(mapLoc.x, mapLoc.y, 0.f);
						simpleClusters.popMatrix();
						simpleClusters.endDraw();
					}
				}
			}
		
		simpleClustersCreated = true;
	}
	
	void drawSimpleClusters(float mapWidth, float mapHeight)
	{
		for( WMV_Cluster c : p.p.getCurrentField().clusters )	
		{
			if(!c.isEmpty() && c.mediaPoints > 1)
			{
				float radius = PApplet.sqrt(c.mediaPoints) * 0.7f;
				radius *= mapDistance;

				WMV_Model m = p.p.getCurrentField().model;
				PVector point = c.getLocation();

				if( pointIsVisible(point, false) )
				{
					drawPoint( point, radius, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, mapMediaTransparency );

//					PVector mapLoc = getMapLocation(point, mapWidth, mapHeight);
//					p.p.p.pushMatrix();
//					p.p.p.translate(mapLeftEdge, mapTopEdge, 0.f);
//					p.p.p.translate(largeMapXOffset, largeMapYOffset, p.hudDistance * mapDistance);
//					p.p.p.point(mapLoc.x, mapLoc.y, mapLoc.z);
//					p.p.p.popMatrix();
				}
			}
		}
		
//		p.p.p.pushMatrix();
//		p.p.p.translate(mapLeftEdge, mapTopEdge, 0.f);
//		p.p.p.translate(largeMapXOffset, largeMapYOffset, p.hudDistance * mapDistance);
//		p.p.p.tint(255.f, 255.f);
//		p.p.p.image(simpleClusters, 0.f, 0.f);
//		p.p.p.popMatrix();
	}

	/**
	 * @param image GMV_Image to draw
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @param capture Draw capture location (true) or viewing location (false)
	 * Draw image location on map of specified size
	 */
	void drawImageOnMap(WMV_Image image, boolean forceDisplay, float mapWidth, float mapHeight, boolean capture)
	{
		float pointSize = smallPointSize * mapWidth;
		float saturation = maxSaturation;
		float imageDistance = image.getViewingDistance();   // Get photo distance from current camera position
		boolean visible = forceDisplay;
		
		if (imageDistance < p.p.viewer.getFarViewingDistance() && imageDistance > p.p.viewer.getNearClippingDistance())    // If image is in visible range
			visible = true;                                              

		if(visible && image.location != null && !image.disabled && !image.hidden)
		{
			float alpha = 255.f;
			if(!forceDisplay)
				alpha = image.getTimeBrightness() * mapMediaTransparency;

			if(alpha > 0.f)
			{
				if(capture)
					drawPoint( image.getCaptureLocation(), pointSize, mapWidth, mapHeight, mapImageCaptureHue, saturation, 255.f, mapMediaTransparency );
				else
					drawPoint( image.getLocation(),  pointSize, mapWidth, mapHeight, mapImageHue, saturation, 255.f, mapMediaTransparency );
			}
		}
		
	}

	/**
	 * @param panorama GMV_Panorama to draw
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * Draw image location on map of specified size
	 */
	void drawPanoramaOnMap(WMV_Panorama panorama, boolean forceDisplay, float mapWidth, float mapHeight, boolean capture)
	{
		float pointSize = mediumPointSize * mapWidth;
		float saturation = maxSaturation;
		float panoramaDistance = panorama.getViewingDistance();   // Get photo distance from current camera position
		boolean visible = forceDisplay;

		if (panoramaDistance < p.p.viewer.getFarViewingDistance() && panoramaDistance > p.p.viewer.getNearClippingDistance())    // If panorama is in visible range
			visible = true;                                              

		if(visible && panorama.location != null && !panorama.disabled && !panorama.hidden)
		{
			float alpha = 255.f;
			if(!forceDisplay)
				alpha = panorama.getTimeBrightness() * mapMediaTransparency;

			if(alpha > 0.f)
			{

				if(capture)
					drawPoint( panorama.getCaptureLocation(),  pointSize, mapWidth, mapHeight, mapPanoramaCaptureHue, saturation, 255.f, mapMediaTransparency );
				else
					drawPoint( panorama.getLocation(),  pointSize, mapWidth, mapHeight, mapPanoramaHue, saturation, 255.f, mapMediaTransparency );
			}
		}
	}

	/**
	 * @param video GMV_Video to draw
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * Draw image location on map of specified size
	 */
	void drawVideoOnMap(WMV_Video video, boolean forceDisplay, float mapWidth, float mapHeight, boolean capture)
	{
		float pointSize = mediumPointSize * mapWidth;
		float saturation = maxSaturation;
		float videoDistance = video.getViewingDistance();   // Get photo distance from current camera position
		boolean visible = forceDisplay;
		
		if (videoDistance < p.p.viewer.getFarViewingDistance() && videoDistance > p.p.viewer.getNearClippingDistance())    // If video is in visible range
			visible = true;                                              

		if(visible && video.location != null && !video.disabled && !video.hidden)
		{
			float alpha = 255.f;
			if(!forceDisplay)
				alpha = video.getTimeBrightness() * mapMediaTransparency;

			if(alpha > 0.f)
			{
				if(capture)
					drawPoint( video.getCaptureLocation(), pointSize, mapWidth, mapHeight, mapVideoCaptureHue, saturation, 255.f, mapMediaTransparency );
				else
					drawPoint( video.getLocation(), pointSize, mapWidth, mapHeight, mapVideoHue, saturation, 255.f, mapMediaTransparency );
			}
		}
	}

	/**
	 * Draw the map origin
	 * @param mapWidth
	 * @param mapHeight
	 */
	void drawOriginOnMap(float mapWidth, float mapHeight)
	{
		int size = (int)(mapWidth / 40.f);
		for(int i=-size/2; i<size/2; i+=size/10)
			drawPoint( new PVector(i, 0.f, 0.f), hugePointSize * mapWidth, mapWidth, mapHeight, 180.f, 30.f, 255.f, mapMediaTransparency / 2.f );
		for(int i=-size/2; i<size/2; i+=size/10)
			drawPoint( new PVector(0.f, 0.f, i), hugePointSize * mapWidth, mapWidth, mapHeight, 180.f, 30.f, 255.f, mapMediaTransparency / 2.f );
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

		drawPoint( camLoc, cameraPointSize, mapWidth, mapHeight, mapCameraHue, 255.f, 255.f, mapMediaTransparency );
		float ptSize = cameraPointSize;
		
//		float arrowSize = 60.f;
		float arrowSize = fieldRatio >= 1 ? p.p.getCurrentModel().fieldWidth : p.p.getCurrentModel().fieldLength;

		ScaleMap logMap;
		logMap = new ScaleMap(6., arrowSize, 6., 60.);		/* Time fading interpolation */
		logMap.setMapFunction(p.p.circularEaseOut);

		/* Change viewer arrow based on fieldWidth -- should be fieldLength??  -- should depend on zoom level too! */
		int arrowLength = 20;
		
		logMap = new ScaleMap(0.f, 0.25f, 0.f, 0.25f);		/* Time fading interpolation */
		logMap.setMapFunction(p.p.circularEaseOut);
		
		float shrinkFactor = PApplet.map(arrowLength, 6.f, arrowSize, 0.f, 0.25f);
		shrinkFactor = (float)logMap.getMappedValueFor(shrinkFactor);
		shrinkFactor = 0.95f - (0.25f - shrinkFactor);		// Reverse mapping high to low
				
		for(int i=1; i<arrowLength; i++)
		{
			p.p.p.textSize(ptSize);
			float x = i * cameraPointSize * 0.25f * (float)Math.cos( camYaw );
			float y = i * cameraPointSize * 0.25f * (float)Math.sin( camYaw );
			
			PVector arrowPoint = new PVector(camLoc.x + x, 0, camLoc.z + y);
			drawPoint( arrowPoint, ptSize, mapWidth, mapHeight, mapCameraHue, 120.f, 255.f, 255.f );

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
//		PApplet.println("drawPathOnMap..."+path.size());
		float pointSize = smallPointSize * mapWidth;
		
		float saturation = maxSaturation;                                              

		for(WMV_Waypoint w : path)
		{
			drawPoint( w.getLocation(), pointSize * 4.f, mapWidth, mapHeight, 30, saturation, 255.f, mapMediaTransparency );
//			PApplet.println("Path ---> location.x:"+w.getLocation().x+" y:"+w.getLocation().y);
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
			drawPoint( c.getLocation(), 5.f, largeMapWidth, largeMapHeight, mapClusterHue, 255.f, 255.f, mapMediaTransparency );
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
	public void highlightCluster( PVector point, float pointSize, float mapWidth, float mapHeight, float hue, float saturation, float brightness, float transparency )
	{
		float size = pointSize;
		int iterations = PApplet.round(size);
		int sizeDiff = PApplet.round(size/iterations);
		float alpha = transparency;
		float alphaDiff = transparency / iterations;
		
		for(int i=0; i<iterations; i++)
		{
			drawPoint( point, size * smallPointSize * mapWidth, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, alpha * 0.33f );

			size-=sizeDiff;
			alpha-=alphaDiff;
		}
	}
	
	/**
	 * Draw (on map) a point given in 3D world coordinates 
	 * @param point Point in 3D world coordinates
	 * @param pointSize Point size
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @param hue Point hue
	 * @param saturation Point saturation
	 * @param brightness Point brightness
	 * @param transparency Point transparency
	 */
	public void drawPoint( PVector point, float pointSize, float mapWidth, float mapHeight, float hue, float saturation, float brightness, float transparency )
	{		
//		WMV_Model m = p.p.getCurrentField().model;

		if(!p.p.p.utilities.isNaN(point.x) && !p.p.p.utilities.isNaN(point.y) && !p.p.p.utilities.isNaN(point.z))
		{
			PVector mapLoc = getMapLocation(point, mapWidth, mapHeight);

			if(mapLoc.x < mapWidth && mapLoc.x > 0 && mapLoc.y < mapHeight && mapLoc.y > 0)
			{
				p.p.p.stroke(hue, saturation, brightness, transparency);
				p.p.p.strokeWeight(pointSize);

				p.p.p.pushMatrix();
				p.p.p.translate(mapLeftEdge, mapTopEdge);
				p.p.p.point(largeMapXOffset + mapLoc.x, largeMapYOffset + mapLoc.y, p.hudDistance * mapDistance);
				p.p.p.popMatrix();
			}
		}
		else p.message("Map point is NaN!:"+point+" hue:"+hue);
	}
	
	public void updateMapMouse()
	{
		Shape3D itemSelected = Shape3D.pickShape(p.p.p, p.p.p.mouseX, p.p.p.mouseY);

		int clusterID = -1;
		
		if(itemSelected == null)
		{
			selectedCluster = -1;
		}
		else
		{
			clusterID = itemSelected.tagNo;
			
			if(selectedClusterIDs.hasValue(clusterID))
			{
				if(clusterID >= 0 && clusterID < p.p.getCurrentField().clusters.size())
				{
					if(clusterID != selectedCluster)
					{
						selectedCluster = clusterID;
						if(p.p.p.debug.map) PApplet.println("Selected new cluster:"+selectedCluster +" p.p.p.mouseX:"+p.p.p.mouseX+" p.p.p.mouseY:"+p.p.p.mouseY);
						//					WMV_Cluster c = p.p.getCluster(selectedCluster);
						if(p.p.p.debug.map) PApplet.println("Location:"+itemSelected.x()+" y:"+itemSelected.y()+" z:"+ itemSelected.z());
					}
				}
			}
		}
		
//		PApplet.println("itemSelected clusterID:"+clusterID);
		
	}
	
	public void handleMouseReleased(int mouseX, int mouseY)
	{
		if(selectedCluster != -1)
		{
//			PApplet.println("handleMouseReleased:"+selectedCluster);
			p.p.viewer.teleportToCluster(selectedCluster, true);
			p.p.display.map = false;
		}
	}
	
	public void setSelectedCluster( int newCluster )
	{
		selectedCluster = newCluster;
	}
	
	/**
	 * Transition map zoom from current to given value
	 */
	void mapZoomTransition(float target)
	{
		if(target != mapDistance)			// Check if already at target
		{
			beginZoomTransition = true;
			zoomTransition = true;   
			zoomTransitionStart = mapDistance;
			zoomTransitionTarget = target;
			zoomTransitionStartFrame = p.p.p.frameCount;
			zoomTransitionEndFrame = zoomTransitionStartFrame + zoomTransitionLength;
		}
		else
		{
			zoomTransition = false;
		}
	}
	
	/**
	 * Update map zoom level each frame
	 */
	void updateMapZoomTransition()
	{
		float newZoomLevel = 0.f;

		if(beginZoomTransition)
		{
			zoomTransitionStartFrame = p.p.p.frameCount;					
			zoomTransitionEndFrame = p.p.p.frameCount + zoomTransitionLength;	
			beginZoomTransition = false;
		}

		if (p.p.p.frameCount >= zoomTransitionEndFrame)
		{
			zoomTransition = false;
			newZoomLevel = zoomTransitionTarget;
			zoomToRectangle( 0, 0, largeMapWidth * mapDistance, largeMapHeight * mapDistance);
		} 
		else
		{
			newZoomLevel = PApplet.map(p.p.p.frameCount, zoomTransitionStartFrame, zoomTransitionEndFrame, 
									   zoomTransitionStart, zoomTransitionTarget);  
		}

		mapDistance = newZoomLevel;
	}

	/**
	 * @param mapWidth
	 * @param mapHeight
	 */
	void drawMapBorder(float mapWidth, float mapHeight)
	{
		p.p.p.stroke(255,255,255,255);
		p.p.p.strokeWeight(3);

		p.p.p.pushMatrix();
		p.p.p.translate(mapLeftEdge, mapTopEdge);
		p.p.p.translate(largeMapXOffset, largeMapYOffset);
		p.p.p.line(0.f, 0.f, p.hudDistance * mapDistance, mapWidth, 0.f, p.hudDistance * mapDistance );
		p.p.p.line(mapWidth, 0, p.hudDistance * mapDistance,  mapWidth, mapHeight, p.hudDistance * mapDistance );
		p.p.p.line(mapWidth, mapHeight, p.hudDistance * mapDistance,  0.f, mapHeight, p.hudDistance * mapDistance );
		p.p.p.line(0.f, mapHeight, p.hudDistance * mapDistance,  0.f, 0.f, p.hudDistance * mapDistance );
		p.p.p.popMatrix();
		
		p.p.p.stroke(133,255,255,255);
		p.p.p.strokeWeight(3);
		
		p.p.p.pushMatrix();
		p.p.p.translate(mapLeftEdge, mapTopEdge);
		p.p.p.translate(zoomMapXOffset, zoomMapYOffset);
		p.p.p.line(0.f, 0.f, p.hudDistance * mapDistance, zoomMapWidth, 0.f, p.hudDistance * mapDistance );
		p.p.p.line(zoomMapWidth, 0, p.hudDistance * mapDistance, zoomMapWidth, zoomMapHeight, p.hudDistance * mapDistance );
		p.p.p.line(zoomMapWidth, zoomMapHeight, p.hudDistance * mapDistance, 0.f, zoomMapHeight, p.hudDistance * mapDistance );
		p.p.p.line(0.f, zoomMapHeight, p.hudDistance * mapDistance,  0.f, 0.f, p.hudDistance * mapDistance );
		p.p.p.popMatrix();
		
		if(p.p.p.debug.detailed)
		{
			p.p.p.pushMatrix();
			p.p.p.stroke(0,255,255,255);
			p.p.p.strokeWeight(25);
			WMV_Cluster c = p.p.getCurrentField().getEdgeClusterOnXAxis(true);
			PVector point = getMapLocation(c.getLocation(), largeMapWidth, largeMapHeight);
			p.p.p.translate(mapLeftEdge, mapTopEdge);
			p.p.p.translate(largeMapXOffset, largeMapYOffset);
			p.p.p.point(point.x, point.y, p.hudDistance * mapDistance);
			p.p.p.popMatrix();

			p.p.p.pushMatrix();
			p.p.p.stroke(100,255,255,255);
			p.p.p.strokeWeight(35);
			c = p.p.getCurrentField().getEdgeClusterOnZAxis(true);
			point = getMapLocation(c.getLocation(), largeMapWidth, largeMapHeight);
			p.p.p.translate(mapLeftEdge, mapTopEdge);
			p.p.p.translate(largeMapXOffset, largeMapYOffset);
			p.p.p.point(point.x, point.y, p.hudDistance * mapDistance);
			p.p.p.popMatrix();
		}
	}

	/**
	 * Zoom to rectangle in large map
	 * @param boxXOffset X offset in large map of rectangle left edge (0 to mapWidth)
	 * @param boxYOffset Y offset in large map of rectangle top edge (0 to mapHeight)
	 * @param boxWidth Width of rectangle
	 * @param boxHeight Height of rectangle
	 */
	void zoomToRectangle(float boxXOffset, float boxYOffset, float boxWidth, float boxHeight)	// -- Use clusters instead of absolute?
	{
		zoomMapLeftEdge += boxXOffset;
		zoomMapTopEdge += boxYOffset;
		zoomMapXOffset = largeMapXOffset + zoomMapLeftEdge;
		zoomMapYOffset = largeMapYOffset + zoomMapTopEdge;
		zoomMapWidth = boxWidth; 
		zoomMapHeight = boxHeight;

		mapDistance = zoomMapWidth / largeMapWidth;
		mapLeftEdge = (p.p.p.width - zoomMapWidth)/2 - zoomMapLeftEdge;
		mapTopEdge = (p.p.p.height - zoomMapHeight)/2 - zoomMapTopEdge;
		
		if(p.p.p.debug.map)
		{
			PApplet.println("Set mapLeftEdge:" + mapLeftEdge);
			PApplet.println("Set mapTopEdge:" + mapTopEdge);
		}
	}

	/**
	 * Transition map zoom from current to given value
	 */
	void zoomRectangleScrollTransition(float mapXOffset, float mapYOffset)
	{
		if(mapXOffset != 0 || mapYOffset != 0)					// Check if already at target
		{
			if(mapXOffset != 0 && mapYOffset != 0)
			{
				zoomToRectangle((1.f-mapDistance)*mapXOffset, (1.f-mapDistance)*mapYOffset, zoomMapWidth, zoomMapHeight);
			}
			else if(mapXOffset != 0 && mapYOffset == 0)
			{
				zoomToRectangle((1.f-mapDistance)*mapXOffset, 0, zoomMapWidth, zoomMapHeight);

			}
			else if(mapXOffset == 0 && mapYOffset != 0)
			{
				zoomToRectangle(0, (1.f-mapDistance)*mapYOffset, zoomMapWidth, zoomMapHeight);
			}
		}
	}
	
	/**
	 * Transition map zoom from current to given value
	 */
	void mapScrollTransition(float mapXOffset, float mapYOffset)
	{
		if(!scrollTransition)
		{
			if(mapXOffset != 0 || mapYOffset != 0)					// Check if already at target
			{
				beginScrollTransition = true;
				scrollTransition = true;   
				scrollTransitionStartFrame = p.p.p.frameCount;
				scrollTransitionEndFrame = scrollTransitionStartFrame + scrollTransitionLength;
				
				if(mapXOffset != 0 && mapYOffset != 0)
				{
					mapXTransitionStart = mapLeftEdge;
					mapXTransitionTarget = mapLeftEdge + mapXOffset;
					mapYTransitionStart = mapTopEdge;
					mapYTransitionTarget = mapTopEdge + mapYOffset;
//					zoomToRectangle((1.f-mapDistance)*mapXOffset, (1.f-mapDistance)*mapYOffset, zoomMapWidth, zoomMapHeight);
				}
				else if(mapXOffset != 0 && mapYOffset == 0)
				{
					mapXTransitionStart = mapLeftEdge;
					mapXTransitionTarget = mapLeftEdge + mapXOffset;
					mapYTransitionTarget = mapTopEdge;
//					zoomToRectangle((1.f-mapDistance)*mapXOffset, 0, zoomMapWidth, zoomMapHeight);

				}
				else if(mapXOffset == 0 && mapYOffset != 0)
				{
					mapXTransitionTarget = mapLeftEdge;
					mapYTransitionStart = mapTopEdge;
					mapYTransitionTarget = mapTopEdge + mapYOffset;
//					zoomToRectangle(0, (1.f-mapDistance)*mapYOffset, zoomMapWidth, zoomMapHeight);
				}
			}
			else
			{
				scrollTransition = false;
			}
		}
	}
	
	/**
	 * Update map zoom level each frame
	 */
	void updateMapScrollTransition()
	{
		float newMapX = mapLeftEdge;
		float newMapY = mapTopEdge;

		if(beginScrollTransition)
		{
			scrollTransitionStartFrame = p.p.p.frameCount;					
			scrollTransitionEndFrame = p.p.p.frameCount + scrollTransitionLength;	
			beginScrollTransition = false;
		}

		if (p.p.p.frameCount >= scrollTransitionEndFrame)
		{
			scrollTransition = false;
			newMapX = mapXTransitionTarget;
			newMapY = mapYTransitionTarget;
		} 
		else
		{
			if(mapLeftEdge != mapXTransitionTarget)
			{
				newMapX = PApplet.map(p.p.p.frameCount, scrollTransitionStartFrame, scrollTransitionEndFrame,
									  mapXTransitionStart, mapXTransitionTarget); 
			}
			if(mapTopEdge != mapYTransitionTarget)
			{
				newMapY = PApplet.map(p.p.p.frameCount, scrollTransitionStartFrame, scrollTransitionEndFrame,
									  mapYTransitionStart, mapYTransitionTarget);     			
			}
		}

		if(mapLeftEdge != newMapX)
			mapLeftEdge = newMapX;
		if(mapTopEdge != newMapY)
			mapTopEdge = newMapY;
		
		PApplet.println("Updated mapLeftEdge:"+mapLeftEdge);
		PApplet.println("Updated mapTopEdge:"+mapTopEdge);
	}


	/**
	 * Draw cursor location (on map) 
	 * @param point Point in 3D world coordinates
	 * @param pointSize Point size
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @param hue Point hue
	 * @param saturation Point saturation
	 * @param brightness Point brightness
	 * @param transparency Point transparency
	 */
	public void drawMousePointOnMap( PVector point, float pointSize, float mapWidth, float mapHeight, float hue, float saturation, float brightness, float transparency )
	{		
		if(!p.p.p.utilities.isNaN(point.x) && !p.p.p.utilities.isNaN(point.y) && !p.p.p.utilities.isNaN(point.z))
		{
			if(point.x < mapWidth && point.x > 0 && point.y < mapHeight && point.y > 0)
			{
				p.p.p.stroke(hue, saturation, brightness, transparency);
				p.p.p.pushMatrix();

				p.p.p.strokeWeight(pointSize);
				p.p.p.translate(mapLeftEdge, mapTopEdge);
				p.p.p.point(largeMapXOffset + point.x, largeMapYOffset + point.y, p.hudDistance);
//				p.p.p.point(largeMapXOffset + point.x, largeMapYOffset + point.y, p.hudDistance * mapZoom);
				
				p.p.p.popMatrix();
			}
		}
		else p.message("Map point is NaN!:"+point+" hue:"+hue);
	}
	
//	/**
//	 * Draw camera attraction force vector (for debugging)
//	 */
//	public void drawForceVector()
//	{
//		mapVectorOrigin = p.p.viewer.getLocation();
//		
//		float ptSize = cameraPointSize * 2.f;
//		drawPoint( mapVectorOrigin, ptSize, largeMapWidth, largeMapHeight, mapCameraHue, 255.f, 255.f, mapMediaTransparency );
//		
//		int arrowLength = 30;
//		
//		ScaleMap logMap;
//		logMap = new ScaleMap(6., 60., 6., 60.);			/* Time fading interpolation */
//		logMap.setMapFunction(p.p.circularEaseOut);
//		
//		logMap = new ScaleMap(0.f, 0.25f, 0.f, 0.25f);		/* Time fading interpolation */
//		logMap.setMapFunction(p.p.circularEaseOut);
//		
//		float shrinkFactor = PApplet.map(arrowLength, 6.f, 60.f, 0.f, 0.25f);
//		shrinkFactor = (float)logMap.getMappedValueFor(shrinkFactor);
//		shrinkFactor = 0.95f - (0.25f - shrinkFactor);		// Reverse mapping high to low
//		PVector current = mapVectorVector;
//		
//		for(int i=1; i<arrowLength; i++)
//		{
//			p.p.p.textSize(ptSize);
//			float mult = (i+6) * 0.025f;
//			current = mapVectorVector.mult( mult );
//			
//			PVector arrowPoint = new PVector(mapVectorOrigin.x + current.x, 0, mapVectorOrigin.z + current.z);
//			drawPoint( arrowPoint, ptSize, largeMapWidth, largeMapHeight, 255.f-mapCameraHue, 170.f, 255.f, 255.f );
//
//			ptSize *= shrinkFactor;
//		}
//	}
//	
//	/**
//	 * Interesting effect
//	 * @param mapWidth
//	 * @param mapHeight
//	 */
//	void drawMandalaOnMap(float mapWidth, float mapHeight)
//	{
//		int size = (int)(mapWidth / 20.f);
//		for(int i=-size/2; i<size/2; i+=size/20)
//			drawPoint( new PVector(i, 0.f, 0.f), hugePointSize * mapWidth * 20.f / (i+size/2), mapWidth, mapHeight, 180.f, 30.f, 255.f, mapMediaTransparency / 2.f );
//		for(int i=-size/2; i<size/2; i+=size/20)
//			drawPoint( new PVector(0.f, 0.f, i), hugePointSize * mapWidth * 20.f / (i+size/2), mapWidth, mapHeight, 180.f, 30.f, 255.f, mapMediaTransparency / 2.f );
//	}
}
