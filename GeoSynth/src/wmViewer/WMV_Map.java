package wmViewer;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.data.IntList;
import toxi.math.ScaleMap;
//import shapes3d.*;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.MarkerFactory;
import de.fhpotsdam.unfolding.events.EventDispatcher;
import de.fhpotsdam.unfolding.events.PanMapEvent;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.interactions.MouseHandler;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MarkerManager;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.providers.AbstractMapProvider;
import de.fhpotsdam.unfolding.utils.DebugDisplay;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;

/***********************************
 * @author davidgordon
 * Class for displaying interactive 2D map of media-based virtual environment
 */
public class WMV_Map 
{
	/* Map */
	private UnfoldingMap map;
	private Location mapCenter;
	private EventDispatcher eventDispatcher;
	private MarkerManager<Marker> markerManager;
	private SimplePointMarker viewerMarker;
	private int clusterZoomLevel = 18;
//	private IntList imageMarkers, panoramaMarkers, videoMarkers;
//	final private int panoramaIndexOffset = 1000000, videoIndexOffset = 2000000;
	
	/* Interaction */
	private int selectedCluster = -1;
//	private ArrayList<Ellipsoid> selectableClusters;
	private IntList selectableClusterIDs;
	private ArrayList<SelectableClusterLocation> selectableClusterLocations;
	int mousePressedFrame = -1;
	int mouseDraggedFrame = -1;

	public float mapDistance = 1.f;										// Obsolete soon
	private final float mapDistanceMin = 0.04f, mapDistanceMax = 1.2f;	// Obsolete soon
	public float mapLeftEdge = 0.f, mapTopEdge = 0.f;					// Obsolete soon

	private float curMapWidth, curMapHeight;							// Obsolete soon
	private float largeMapXOffset, largeMapYOffset;						// Obsolete soon
//	private float largeMapZoomLevel = 1.f;								// Obsolete soon

	/* Clusters */
	private boolean selectableClustersCreated = false;
	public float mapClusterHue = 112.f;
	private float mapAttractorClusterHue = 222.f;
	
	/* Media */
	float smallPointSize, mediumPointSize, largePointSize, hugePointSize;	// Obsolete soon
	float cameraPointSize;

	public boolean mapImages = true, mapPanoramas = true, mapVideos = true, mapClusters = true;
	private float imageHue = 150.f, imageCaptureHue = 180.f;
	private float panoramaHue = 190.f, panoramaCaptureHue = 220.f;
	private float videoHue = 40.f, videoCaptureHue = 70.f;
	private float cameraHue = 140.f;

	private float mediaTransparency = 120.f;
	private float maxSaturation = 210.f;
	private float fieldAspectRatio;
	
	private float zoomMapLeftEdge = 0.f, zoomMapTopEdge = 0.f;
	private float zoomMapWidth, zoomMapHeight, zoomMapMaxWidth, zoomMapMaxHeight;
	private float zoomMapXOffset, zoomMapYOffset;
	private float zoomMapDefaultWidth, zoomMapDefaultHeight;

	public boolean scrollTransition = false;
	private int scrollTransitionStartFrame = 0, scrollTransitionEndFrame = 0, scrollTransitionLength = 16;	
	private float mapXTransitionStart, mapXTransitionTarget, mapYTransitionStart, mapYTransitionTarget;

	public boolean zoomToRectangleTransition = false;   
	private int zoomToRectangleTransitionStartFrame = 0, zoomToRectangleTransitionEndFrame = 0, zoomToRectangleTransitionLength = 16;

	private float zoomMapXOffsetTransitionStart, zoomMapXOffsetTransitionTarget;
	private float zoomMapYOffsetTransitionStart, zoomMapYOffsetTransitionTarget;
	private float zoomMapWidthTransitionStart, zoomMapWidthTransitionTarget;
	private float zoomMapHeightTransitionStart, zoomMapHeightTransitionTarget;
	
	private float mapDistanceTransitionStart, mapDistanceTransitionTarget;
	private float mapLeftEdgeTransitionStart, mapLeftEdgeTransitionTarget;
	private float mapTopEdgeTransitionStart, mapTopEdgeTransitionTarget;
	
	PVector mapVectorOrigin, mapVectorVector;

	WMV_Display p;

	WMV_Map(WMV_Display parent)
	{
		p = parent;

		largeMapXOffset = -p.p.p.width * 0.5f;
		largeMapYOffset = -p.p.p.height * 0.5f;

		smallPointSize = 0.0000022f * p.p.p.width;
		mediumPointSize = 0.0000028f * p.p.p.width;
		largePointSize = 0.0000032f * p.p.p.width;
		hugePointSize = 0.0000039f * p.p.p.width;
		cameraPointSize = 0.005f * p.p.p.width;
	}

	/**
	 * Initialize maps
	 */
	public void initializeMaps()
	{
		WMV_Model m = p.p.getCurrentModel();
		fieldAspectRatio = m.fieldAspectRatio;						//	Field ratio == fieldWidth / fieldLength;
		zoomMapDefaultWidth = (float)Math.log10(m.fieldWidth) * 33.3f;										// Was 240.f
		zoomMapDefaultHeight = (float)Math.log10(m.fieldWidth) * 33.3f * p.p.p.height / p.p.p.width;		// Was 180.f

		if(fieldAspectRatio >= 1.f)									
		{
			curMapWidth = p.p.p.width * 0.95f;
			curMapHeight = p.p.p.width / fieldAspectRatio;
		}
		else
		{
			curMapWidth = p.p.p.height * fieldAspectRatio;
			curMapHeight = p.p.p.height * 0.95f;
		}

		zoomToRectangle(0, 0, curMapWidth, curMapHeight);			// Start zoomed out on whole map

		initializeSatelliteMap();
		p.initializedMaps = true;
	}
	
	/**
	 * Initialize maps
	 */
	public void initializeSatelliteMap()
	{
		map = new UnfoldingMap(p.p.p, "Satellite", 0, 0, p.p.p.width, p.p.p.height, true, false, new Microsoft.AerialProvider());

		PVector gpsLoc = p.p.p.utilities.getGPSLocation(p.p.getCurrentField(), new PVector(0,0,0));
		mapCenter = new Location(gpsLoc.y, gpsLoc.x);
		
//		imageMarkers = new IntList();
//		panoramaMarkers = new IntList();
//		videoMarkers = new IntList();
		
		map.zoomAndPanTo(16, mapCenter);
		map.setZoomRange(2, 19);
		map.setTweening(true);

		eventDispatcher = new EventDispatcher();

		// Add mouse interaction to map
		MouseHandler mouseHandler = new MouseHandler(p.p.p, map);
		eventDispatcher.addBroadcaster(mouseHandler);

		eventDispatcher.register(map, "pan", map.getId());
		eventDispatcher.register(map, "zoom", map.getId());

		createPointMarkers();
		
		PVector vLoc = p.p.viewer.getGPSLocation();
		viewerMarker = new SimplePointMarker(new Location(vLoc.y, vLoc.x));
		viewerMarker.setId("viewer");
		viewerMarker.setDiameter(20.f);
		viewerMarker.setColor(p.p.p.color(0, 0, 255, 255));
	}
	
	/**
	 * Create simple point markers for each cluster
	 */
	public void createPointMarkers()
	{
		markerManager = new MarkerManager<Marker>();
		for( WMV_Cluster c : p.p.getCurrentField().clusters )	
		{
			if(!c.isEmpty() && c.mediaCount != 0)
			{
				PVector mapLoc = c.getLocation();
				PVector gpsLoc = p.p.p.utilities.getGPSLocation(p.p.getCurrentField(), mapLoc);
				SimplePointMarker marker = new SimplePointMarker(new Location(gpsLoc.y, gpsLoc.x));
				marker.setId(String.valueOf(c.getID()));
				marker.setColor(p.p.p.color(90, 225, 225, 155));
				marker.setHighlightColor(p.p.p.color(170, 255, 255, 255));
				marker.setStrokeWeight(0);
				marker.setDiameter(PApplet.sqrt(c.mediaCount) * 3.f);
				markerManager.addMarker(marker);
			}
		}

		map.addMarkerManager(markerManager);
		markerManager.enableDrawing();
	}
	
//	public void handleMouseMoved(int mouseX, int mouseY)
//	{
//		for (Marker m : map.getMarkers()) 
//			m.setSelected(false);
//
//		List<Marker> markers = map.getHitMarkers(mouseX, mouseY);
////		Marker marker = map.getFirstHitMarker(mouseX, mouseY);		// Select hit marker
//		
////		for(Marker m : markers)
////		{
////			
////		}
//		if(markers.size() > 0)
//		{
//			Marker marker = markers.get(0);
//
//			if (marker != null) 
//				marker.setSelected(true);
//		}
//
//		// -- Use getHitMarkers(x, y) to allow multiple selection.
//	}
	
	/**
	 * Reset the map to initial state
	 */
	public void reset()
	{
		resetMapZoom(false);
		initializeMaps();
	}
	
	/**
	 * Create selectable circle representing each cluster		// -- BROKEN
	 * @param mapWidth
	 * @param mapHeight
	 */
	public void createSelectableClusters(float mapWidth, float mapHeight)
	{
//		selectableClusters = new ArrayList<Ellipsoid>();
		selectableClusterIDs = new IntList();
		selectableClusterLocations = new ArrayList<SelectableClusterLocation>();
		
		for( WMV_Cluster c : p.p.getCurrentField().clusters )	
		{
			if(!c.isEmpty() && c.mediaCount != 0)
			{
				PVector mapLoc = getMapLocation(c.getLocation(), mapWidth, mapHeight);
				if( pointIsVisible(mapLoc, true) )
				{
//					Ellipsoid ellipsoid = new Ellipsoid(p.p.p, 4, 4);
					float radius = PApplet.sqrt(c.mediaCount) * 0.7f * mapDistance / PApplet.sqrt(PApplet.sqrt(mapDistance));

//					ellipsoid.setRadius(radius);
//					ellipsoid.drawMode(S3D.SOLID);
//					ellipsoid.fill(p.p.p.color(105.f, 225.f, 200.f, mediaTransparency));
//					ellipsoid.fill(p.p.p.color(105.f, 225.f, 200.f, 255.f));
//					ellipsoid.strokeWeight(0.f);

					mapLoc.add(new PVector(largeMapXOffset, largeMapYOffset, p.hudDistance * mapDistance));
					mapLoc.add(new PVector(mapLeftEdge, mapTopEdge, 0));
//					ellipsoid.moveTo(mapLoc.x, mapLoc.y, mapLoc.z);
					
					SelectableClusterLocation scl = new SelectableClusterLocation(c.getID(), mapLoc);
					
//					ellipsoid.tagNo = c.getID();
//					selectableClusters.add(ellipsoid);
					selectableClusterIDs.append(c.getID());
					selectableClusterLocations.add(scl);
				}
			}
		}
		
		selectableClustersCreated = true;
	}
	
	PVector getMapLocation(PVector point, float mapWidth, float mapHeight)
	{
		WMV_Model m = p.p.getCurrentField().model;
		float mapLocX, mapLocY;

		if(fieldAspectRatio >= 1.f)					
		{
			mapLocX = PApplet.map( point.x, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, 0, mapWidth );		
			mapLocY = PApplet.map( point.z, -0.5f * m.fieldLength, 0.5f*m.fieldLength, 0, mapWidth / fieldAspectRatio );
		}
		else
		{
			mapLocX = PApplet.map( point.x, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, 0, mapHeight * fieldAspectRatio );		
			mapLocY = PApplet.map( point.z, -0.5f * m.fieldLength, 0.5f*m.fieldLength, 0, mapHeight );
		}

		return new PVector(mapLocX, mapLocY, 0.f);
	}

	/**
	 * Create and draw selectable clusters using Shapes3D library
	 */
	void drawSelectableClusters()
	{
//		p.p.p.pushMatrix();
//		for(Ellipsoid e : selectableClusters)
//			e.draw();
//		p.p.p.popMatrix();
	}
	
	/**
	 * Draw map filling the main window
	 */
	void drawMainMap(boolean mediaOnly)
	{
		if(mediaOnly)							// Draw media map markers only (offline)
		{
			p.p.p.pushMatrix();
			p.startHUD();											

			p.p.p.fill(55, 0, 255, 255);
			p.p.p.textSize(p.veryLargeTextSize);
			float textXPos = p.centerTextXOffset;
			float textYPos = p.topTextYOffset;

			if(p.p.interactive)
				p.p.p.text("Interactive "+(p.p.hierarchical ? "Hierarchical" : "K-Means")+" Clustering", textXPos, textYPos, p.hudDistance);
			else
				p.p.p.text(p.p.getCurrentField().name, textXPos, textYPos, p.hudDistance);

			p.p.p.popMatrix();

			drawMap(curMapWidth, curMapHeight, largeMapXOffset, largeMapYOffset);
		}
		else									// Draw map using internet map server
		{
			PVector vLoc = p.p.viewer.getGPSLocation();
			Location gpsLoc = new Location(vLoc.y, vLoc.x);
			if(gpsLoc != null)
			{
				viewerMarker.setLocation(gpsLoc);						// Update location of viewer marker
				markerManager.addMarker(viewerMarker);
			}

			/* Clusters */
			if(mapClusters)
			{
//				if(!scrollTransition && !zoomToRectangleTransition && !p.p.interactive)
//				{
//					drawSelectableClusters();

					float mapWidth = curMapWidth;
					float mapHeight = curMapHeight;
					
					if(selectedCluster >= 0)
					{
						WMV_Cluster c = p.p.getCluster(selectedCluster);
//						if(!c.isEmpty() && c.mediaCount != 0)
//							highlightCluster( c.getLocation(), PApplet.sqrt(c.mediaCount)*0.5f, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, mediaTransparency );
						
						if(selectedCluster == p.p.viewer.getCurrentClusterID())
							drawClusterMedia(c, mapWidth, mapHeight, false);
						else
							drawClusterMedia(c, mapWidth, mapHeight, true);
					}

					if(selectedCluster != p.p.viewer.getCurrentClusterID() && p.p.viewer.getCurrentClusterID() >= 0)
					{
						WMV_Cluster c = p.p.getCurrentCluster();
						if(c != null)
						{
//							if(!c.isEmpty() && c.mediaCount != 0)
//								highlightCluster( c.getLocation(), PApplet.sqrt(c.mediaCount)*0.5f, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, mediaTransparency );
							drawClusterMedia(c, mapWidth, mapHeight, false);
						}
					}
//				}
//				else
//				{
//					drawSimpleClusters(mapWidth, mapHeight);
//				}
			}
			
//			float fov = PApplet.PI/3.0f;
//			float cameraZ = (p.p.p.height/2.0f) / PApplet.tan(fov/2.0f);
//			p.p.p.perspective(fov, (float)(p.p.p.width)/(float)(p.p.p.height), cameraZ/10.0f, cameraZ*10.0f);
			p.p.p.perspective();

//			p.p.p.camera(p.p.p.width/2.0f, p.p.p.height/2.0f, (p.p.p.height/2.0f) / PApplet.tan(PApplet.PI*0.1875f), p.p.p.width/2.0f, p.p.p.height/2.0f, 0, 0, 1, 0);			// Works with OCD 
			
			p.p.p.camera();												// Reset to default camera setting
			p.p.p.tint(255.f, 255.f);
			map.draw();
		}
	}
	
	/**
	 * Draw map of media and viewer without UnfoldingMaps library
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @param mapXOffset Map X offset
	 * @param mapYOffset Map Y offset
	 */
	void drawMap(float mapWidth, float mapHeight, float mapXOffset, float mapYOffset)
	{
		if(!scrollTransition && !zoomToRectangleTransition && !p.p.interactive)
		{
			if(!selectableClustersCreated)
				createSelectableClusters(mapWidth, mapHeight);
		}
		
		p.startHUD();

		/* Media */
//		if(!scrollTransition && !zoomToRectangleTransition && !p.p.interactive)
//		{
//			if((mapImages && !p.p.getCurrentField().hideImages))
//				for ( WMV_Image i : p.p.getCurrentField().images )					// Draw image capture locations on 2D Map
//					drawImageOnMap(i, false, mapWidth, mapHeight, false);
//
//			if((mapPanoramas && !p.p.getCurrentField().hidePanoramas))
//				for ( WMV_Panorama n : p.p.getCurrentField().panoramas )			// Draw panorama capture locations on 2D Map
//					drawPanoramaOnMap(n, false, mapWidth, mapHeight, false);
//
//			if((mapVideos && !p.p.getCurrentField().hideVideos))
//				for (WMV_Video v : p.p.getCurrentField().videos)					// Draw video capture locations on 2D Map
//					drawVideoOnMap(v, false, mapWidth, mapHeight, false);
//		}

		/* Clusters */
		if(mapClusters)
		{
			if(!scrollTransition && !zoomToRectangleTransition && !p.p.interactive)
			{
				drawSelectableClusters();

				if(selectedCluster >= 0)
				{
					WMV_Cluster c = p.p.getCluster(selectedCluster);
					if(!c.isEmpty() && c.mediaCount != 0)
						highlightCluster( c.getLocation(), PApplet.sqrt(c.mediaCount)*0.5f, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, mediaTransparency );
					
					if(selectedCluster == p.p.viewer.getCurrentClusterID())
						drawClusterMedia(c, mapWidth, mapHeight, false);
					else
						drawClusterMedia(c, mapWidth, mapHeight, true);
				}

				if(selectedCluster != p.p.viewer.getCurrentClusterID() && p.p.viewer.getCurrentClusterID() >= 0)
				{
					WMV_Cluster c = p.p.getCurrentCluster();
					if(c != null)
					{
						if(!c.isEmpty() && c.mediaCount != 0)
							highlightCluster( c.getLocation(), PApplet.sqrt(c.mediaCount)*0.5f, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, mediaTransparency );
						drawClusterMedia(c, mapWidth, mapHeight, false);
					}
				}
			}
			else
			{
				drawSimpleClusters(mapWidth, mapHeight);
			}
		}
		
		if(!scrollTransition && !zoomToRectangleTransition)
		{
			if(!p.p.interactive)													// While not in Clustering Mode
			{
				if(p.p.viewer.getAttractorCluster() != -1 && p.p.viewer.getAttractorCluster() < p.p.getFieldClusters().size())
					drawPoint( p.p.getAttractorCluster().getLocation(), hugePointSize * mapWidth, mapWidth, mapHeight, mapAttractorClusterHue, 255.f, 255.f, mediaTransparency );

				WMV_Cluster c = p.p.getCurrentCluster();
				if(c != null)
				{
					if(p.p.viewer.getCurrentClusterID() != -1 && p.p.viewer.getCurrentClusterID() < p.p.getFieldClusters().size())
						drawPoint( c.getLocation(), hugePointSize * mapWidth, mapWidth, mapHeight, mapAttractorClusterHue, 255.f, 255.f, mediaTransparency );
				}
				
				drawViewer(mapWidth, mapHeight);
			}

			if(p.p.viewer.getGPSTrack().size() > 0)
				drawPathOnMap(p.p.viewer.getGPSTrack(), mapWidth, mapHeight);
			
//			drawOriginOnMap(mapWidth, mapHeight);
		}

		if(p.p.p.debug.map)
			drawMapBorder(mapWidth, mapHeight);
	}
	
	/**
	 * Draw (nonselectable) points representing clusters
	 * @param mapWidth
	 * @param mapHeight
	 */
	void drawSimpleClusters(float mapWidth, float mapHeight)
	{
		for( WMV_Cluster c : p.p.getCurrentField().clusters )	
		{
			if(!c.isEmpty() && c.mediaCount > 5)
			{
				PVector point = c.getLocation();

				if( pointIsVisible(point, false) )
				{
					float radius = PApplet.sqrt(c.mediaCount) * 0.7f;
//					float radius = PApplet.sqrt(c.mediaPoints) * 0.7f  / PApplet.sqrt(PApplet.sqrt(mapDistance));
					drawPoint( point, radius, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, mediaTransparency );
				}
			}
		}
	}

	/**
	 * Zoom in on cluster
	 * @param c Cluster to zoom in on
	 */
	void zoomToCluster(WMV_Cluster c)
	{
		if(!c.isEmpty() && c.mediaCount > 0)
		{
			if(p.satelliteMap)
			{
				PVector mapLoc = c.getLocation();
				PVector gpsLoc = p.p.p.utilities.getGPSLocation(p.p.getCurrentField(), mapLoc);
				
				map.zoomAndPanTo(clusterZoomLevel, new Location(gpsLoc.y, gpsLoc.x));
			}
			else
			{
				PVector point = getMapLocation(c.getLocation(), curMapWidth, curMapHeight);
				zoomMapLeftEdge = 0.f;
				zoomMapTopEdge = 0.f;
				zoomToRectangleTransition(point.x - zoomMapDefaultWidth/2, point.y - zoomMapDefaultHeight/2.f, zoomMapDefaultWidth, 
										  zoomMapDefaultHeight);								// -- Make sure not to zoom in too much on small fields!
			}
		}
	}
	
	/**
	 * Zoom in on map
	 */
	public void zoomIn()
	{
		if (p.satelliteMap) map.zoomIn();
		else mapZoomTransition(0.85f);
	}
	
	/**
	 * Zoom out on map
	 */
	public void zoomOut()
	{
		if (p.satelliteMap) map.zoomOut();
		else mapZoomTransition(1.176f);
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
			point = getMapLocation(loc, curMapWidth, curMapHeight);

		if( point.x > xOff && point.x < zoomMapWidth + xOff && point.y > yOff && point.y < zoomMapHeight + yOff )
			return true;
		else
			return false;
	}
	
	/**
	 * Draw media in given cluster on map
	 * @param c Given cluster
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @param ignoreTime Whether to display independently of time (true) or not (false)
	 */
	void drawClusterMedia(WMV_Cluster c, float mapWidth, float mapHeight, boolean ignoreTime)
	{
		if((mapImages && !p.p.getCurrentField().hideImages))
			for ( int i : c.images )									// Draw images on Map
			{
				WMV_Image img = p.p.getCurrentField().images.get(i);
				drawImageOnMap(img, ignoreTime, mapWidth, mapHeight, false);
				if(p.p.showModel)
				{
//					drawLine(c.getLocation(), img.getLocation(), 60.f, 160.f, 255.f, mapWidth, mapHeight);
					if(p.p.showMediaToCluster)
						drawLine(c.getLocation(), img.getLocation(), 40.f, 155.f, 200.f, mapWidth, mapHeight);

					if(p.p.showCaptureToMedia)
						drawLine(img.getLocation(), img.getCaptureLocation(), 160.f, 100.f, 255.f, mapWidth, mapHeight);

					if(p.p.showCaptureToCluster)
						drawLine(c.getLocation(), img.getCaptureLocation(), 100.f, 55.f, 255.f, mapWidth, mapHeight);
				}
			}

		if((mapPanoramas && !p.p.getCurrentField().hidePanoramas))
			for ( int n : c.panoramas )									// Draw panoramas on Map
			{
				WMV_Panorama pano = p.p.getCurrentField().panoramas.get(n);
				drawPanoramaOnMap(pano, ignoreTime, mapWidth, mapHeight, false);
			}

		if((mapVideos && !p.p.getCurrentField().hideVideos))
			for (int v : c.videos)										// Draw videos on Map
			{
				WMV_Video vid = p.p.getCurrentField().videos.get(v);
				drawVideoOnMap(vid, ignoreTime, mapWidth, mapHeight, false);
				if(p.p.showModel)
				{
					if(p.p.showMediaToCluster)
						drawLine(c.getLocation(), vid.getLocation(), 140.f, 155.f, 200.f, mapWidth, mapHeight);

					if(p.p.showCaptureToMedia)
						drawLine(vid.getLocation(), vid.getCaptureLocation(), 50.f, 100.f, 255.f, mapWidth, mapHeight);

					if(p.p.showCaptureToCluster)
						drawLine(c.getLocation(), vid.getCaptureLocation(), 190.f, 55.f, 255.f, mapWidth, mapHeight);
				}
			}
	}
	
	/**
	 * Draw line between two points on map
	 * @param point1 First point
	 * @param point2 Second Point
	 * @param hue Hue
	 * @param saturation Saturation
	 * @param brightness Brightness
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 */
	void drawLine(PVector point1, PVector point2, float hue, float saturation, float brightness, float mapWidth, float mapHeight)
	{
		if(p.satelliteMap)
		{
			// -- Need to implement
		}
		else
		{
			PVector mapLoc1 = getMapLocation(point1, mapWidth, mapHeight);
			PVector mapLoc2 = getMapLocation(point2, mapWidth, mapHeight);

			if( (mapLoc1.x < mapWidth && mapLoc1.x > 0 && mapLoc1.y < mapHeight && mapLoc1.y > 0) ||
					(mapLoc2.x < mapWidth && mapLoc2.x > 0 && mapLoc2.y < mapHeight && mapLoc2.y > 0) )
			{
				float pointSize = smallPointSize * 0.1f * mapWidth / PApplet.sqrt(PApplet.sqrt(mapDistance));
				p.p.p.strokeWeight(pointSize);
				p.p.p.stroke(hue, saturation, brightness, 255.f);
				p.p.p.pushMatrix();
				p.p.p.translate(mapLeftEdge, mapTopEdge);
				p.p.p.line( largeMapXOffset + mapLoc1.x, largeMapYOffset + mapLoc1.y, p.hudDistance * mapDistance,
						largeMapXOffset + mapLoc2.x, largeMapYOffset + mapLoc2.y, p.hudDistance * mapDistance );
				p.p.p.popMatrix();
			}
		}
	}
	
	/**
	 * @param image Image to draw
	 * @param ignoreTime Force image to display even when out of time
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @param capture Draw capture location (true) or viewing location (false)
	 * Draw image location on map of specified size
	 */
	void drawImageOnMap(WMV_Image image, boolean ignoreTime, float mapWidth, float mapHeight, boolean capture)
	{
		if(p.satelliteMap)
		{
//			if(!imageMarkers.hasValue(image.getID()))
//			{
//				PVector loc = image.getGPSLocation();  
//				SimplePointMarker marker = new SimplePointMarker(new Location(loc.z, loc.x));
//				marker.setId(String.valueOf(image.getID()));
//				marker.setColor(p.p.p.color(imageHue, maxSaturation, 255.f, mediaTransparency));
//				marker.setStrokeWeight(0);
//				marker.setDiameter(10.f);
//				markerManager.addMarker(marker);
//				imageMarkers.append(image.getID());
//			}
		}
		else
		{
			float pointSize = smallPointSize * mapWidth;
			float saturation = maxSaturation;
			float imageDistance = image.getViewingDistance();   // Get photo distance from current camera position
			boolean visible = ignoreTime;

			if (imageDistance < p.p.viewer.getFarViewingDistance() && imageDistance > p.p.viewer.getNearClippingDistance())    // If image is in visible range
				visible = true;                                              

			if(visible && image.location != null && !image.disabled && !image.hidden)
			{
				float alpha = 255.f;
				if(!ignoreTime && p.p.timeFading)
					alpha = image.getTimeBrightness() * mediaTransparency;

				if(alpha > 0.f)
				{
					if(image.isSelected()) pointSize *= 5.f;
					if(capture)
						drawPoint( image.getCaptureLocation(), pointSize, mapWidth, mapHeight, imageCaptureHue, saturation, 255.f, mediaTransparency );
					else
						drawPoint( image.getLocation(),  pointSize, mapWidth, mapHeight, imageHue, saturation, 255.f, mediaTransparency );
				}
			}
		}
	}

	/**
	 * @param panorama Panorama to draw
	 * @param ignoreTime Force panorama to display even when out of time
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * Draw image location on map of specified size
	 */
	void drawPanoramaOnMap(WMV_Panorama panorama, boolean ignoreTime, float mapWidth, float mapHeight, boolean capture)
	{
		if(p.satelliteMap)
		{
//			if(!panoramaMarkers.hasValue(panorama.getID()))
//			{
//				PVector loc = panorama.getGPSLocation();
//				SimplePointMarker marker = new SimplePointMarker(new Location(loc.z, loc.x));
//				marker.setId(String.valueOf(panorama.getID()));
//				marker.setColor(p.p.p.color(imageHue, maxSaturation, 255.f, mediaTransparency));
//				marker.setStrokeWeight(0);
//				marker.setDiameter(3.f);
//				markerManager.addMarker(marker);
//				panoramaMarkers.append(panorama.getID());
//			}
		}
		else
		{
			float pointSize = mediumPointSize * mapWidth;
			float saturation = maxSaturation;
			float panoramaDistance = panorama.getViewingDistance();   // Get photo distance from current camera position
			boolean visible = ignoreTime;

			if (panoramaDistance < p.p.viewer.getFarViewingDistance() && panoramaDistance > p.p.viewer.getNearClippingDistance())    // If panorama is in visible range
				visible = true;                                              

			if(visible && panorama.location != null && !panorama.disabled && !panorama.hidden)
			{
				float alpha = 255.f;
				if(!ignoreTime && p.p.timeFading)
					alpha = panorama.getTimeBrightness() * mediaTransparency;

				if(alpha > 0.f)
				{
					if(panorama.isSelected()) pointSize *= 5.f;
					if(capture)
						drawPoint( panorama.getCaptureLocation(),  pointSize, mapWidth, mapHeight, panoramaCaptureHue, saturation, 255.f, mediaTransparency );
					else
						drawPoint( panorama.getLocation(),  pointSize, mapWidth, mapHeight, panoramaHue, saturation, 255.f, mediaTransparency );
				}
			}
		}
	}

	/**
	 * @param video Video to draw
	 * @param ignoreTime Force video to display even when out of time
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * Draw image location on map of specified size
	 */
	void drawVideoOnMap(WMV_Video video, boolean ignoreTime, float mapWidth, float mapHeight, boolean capture)
	{
		if(p.satelliteMap)
		{
//			if(!videoMarkers.hasValue(video.getID()))
//			{
//				PVector loc = video.getGPSLocation();
//				SimplePointMarker marker = new SimplePointMarker(new Location(loc.z, loc.x));
//				marker.setId(String.valueOf(video.getID()));
//				marker.setColor(p.p.p.color(imageHue, maxSaturation, 255.f, mediaTransparency));
//				marker.setStrokeWeight(0);
//				marker.setDiameter(3.f);
//				markerManager.addMarker(marker);
//				videoMarkers.append(video.getID());
//			}
		}
		else
		{
			float pointSize = mediumPointSize * mapWidth;
			float saturation = maxSaturation;
			float videoDistance = video.getViewingDistance();   // Get photo distance from current camera position
			boolean visible = ignoreTime;

			if (videoDistance < p.p.viewer.getFarViewingDistance() && videoDistance > p.p.viewer.getNearClippingDistance())    // If video is in visible range
				visible = true;                                              

			if(visible && video.location != null && !video.disabled && !video.hidden)
			{
				float alpha = 255.f;
				if(!ignoreTime && p.p.timeFading)
					alpha = video.getTimeBrightness() * mediaTransparency;

				if(alpha > 0.f)
				{
					if(video.isSelected()) pointSize *= 5.f;
					if(capture)
						drawPoint( video.getCaptureLocation(), pointSize, mapWidth, mapHeight, videoCaptureHue, saturation, 255.f, mediaTransparency );
					else
						drawPoint( video.getLocation(), pointSize, mapWidth, mapHeight, videoHue, saturation, 255.f, mediaTransparency );
				}
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
			drawPoint( new PVector(i, 0.f, 0.f), hugePointSize * mapWidth, mapWidth, mapHeight, 180.f, 30.f, 255.f, mediaTransparency / 2.f );
		for(int i=-size/2; i<size/2; i+=size/10)
			drawPoint( new PVector(0.f, 0.f, i), hugePointSize * mapWidth, mapWidth, mapHeight, 180.f, 30.f, 255.f, mediaTransparency / 2.f );
	}
	
	/**
	 * Draw viewer as arrow on map
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * Draw current viewer location and orientation on map of specified size
	 */
	void drawViewer(float mapWidth, float mapHeight)
	{
		PVector camLoc = p.p.viewer.getLocation();
		if(pointIsVisible(camLoc, false))
		{
			float camYaw = -p.p.viewer.getXOrientation() - 0.5f * PApplet.PI;

			drawPoint( camLoc, cameraPointSize, mapWidth, mapHeight, cameraHue, 255.f, 255.f, mediaTransparency );
			float ptSize = cameraPointSize;

			float arrowSize = fieldAspectRatio >= 1 ? p.p.getCurrentModel().fieldWidth : p.p.getCurrentModel().fieldLength;
			arrowSize = PApplet.round(PApplet.map(arrowSize, 0.f, 2500.f, 0.f, 100.f) * PApplet.sqrt(mapDistance));

			ScaleMap logMap;
			logMap = new ScaleMap(6., arrowSize, 6., 60.);		/* Time fading interpolation */
			logMap.setMapFunction(p.p.circularEaseOut);

			int arrowPoints = 15;								/* Number of points in arrow */

			logMap = new ScaleMap(0.f, 0.25f, 0.f, 0.25f);		/* Time fading interpolation */
			logMap.setMapFunction(p.p.circularEaseOut);

			float shrinkFactor = 0.88f;
			float mapDistanceFactor = PApplet.map(mapDistance, 0.f, 1.f, 0.f, 0.25f);
			
			for(float i=1; i<arrowPoints; i++)
			{
				p.p.p.textSize(ptSize);
				float x = i * cameraPointSize * mapDistanceFactor * (float)Math.cos( camYaw );
				float y = i * cameraPointSize * mapDistanceFactor * (float)Math.sin( camYaw );

				PVector arrowPoint = new PVector(camLoc.x + x, 0, camLoc.z + y);
				drawPoint( arrowPoint, ptSize, mapWidth, mapHeight, cameraHue, 120.f, 255.f, 255.f );

				ptSize *= shrinkFactor;
			}
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
			drawPoint( w.getLocation(), pointSize * 4.f, mapWidth, mapHeight, 30, saturation, 255.f, mediaTransparency );
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
		if(  !p.initialSetup && p.messages.size() < 0 && p.metadata.size() < 0	 )
		{
			p.p.p.hint(PApplet.DISABLE_DEPTH_TEST);						// Disable depth testing for drawing HUD
		}

		for( WMV_Cluster c : p.p.getCurrentField().clusters )								// For all clusters at current depth
		{
			drawPoint( c.getLocation(), 5.f, curMapWidth, curMapHeight, mapClusterHue, 255.f, 255.f, mediaTransparency );
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
			float ptSize = size * smallPointSize * mapWidth / PApplet.sqrt(PApplet.sqrt(mapDistance));
			drawPoint( point, ptSize, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, alpha * 0.33f );
//			drawPoint( point, size * smallPointSize * mapWidth, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, alpha * 0.33f );

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
		if(!p.p.p.utilities.isNaN(point.x) && !p.p.p.utilities.isNaN(point.y) && !p.p.p.utilities.isNaN(point.z))
		{
			PVector mapLoc = getMapLocation(point, mapWidth, mapHeight);

			if(mapLoc.x < mapWidth && mapLoc.x > 0 && mapLoc.y < mapHeight && mapLoc.y > 0)
			{
				p.p.p.stroke(hue, saturation, brightness, transparency);
//				p.p.p.strokeWeight(pointSize);
				p.p.p.strokeWeight(pointSize / PApplet.sqrt(PApplet.sqrt(mapDistance)));
				p.p.p.pushMatrix();
				p.p.p.translate(mapLeftEdge, mapTopEdge);
				p.p.p.point(largeMapXOffset + mapLoc.x, largeMapYOffset + mapLoc.y, p.hudDistance * mapDistance);
				p.p.p.popMatrix();
			}
		}
		else if(p.p.p.debug.map) 
			p.message("Map point is NaN!:"+point+" hue:"+hue);
	}
	
	public void updateMapMouse()
	{
		if(p.satelliteMap)
		{
			for (Marker m : map.getMarkers()) 
				m.setSelected(false);

			Marker marker = map.getFirstHitMarker(p.p.p.mouseX, p.p.p.mouseY);		// Select hit marker
			if (marker != null) 
			{
				marker.setSelected(true);

				String mID = marker.getId();

				if(!mID.equals("viewer"))
					selectedCluster = Integer.parseInt(mID);
			}
			else
			{
				selectedCluster = p.p.viewer.getCurrentClusterID();
			}
//			-- Use getHitMarkers(x, y) to allow multiple selection.
//			List<Marker> markers = map.getHitMarkers(p.p.p.mouseX, p.p.p.mouseY);
//			if(markers.size() > 0)
//			{
//				Marker marker = markers.get(0);
//
//				if (marker != null) 
//					marker.setSelected(true);
//			}
		}
		else
		{
//			Shape3D itemSelected = Shape3D.pickShape(p.p.p, p.p.p.mouseX, p.p.p.mouseY);

//			int clusterID = -1;
//
//			if(itemSelected == null)
//			{
//				selectedCluster = -1;
//			}
//			else
//			{
//				clusterID = itemSelected.tagNo;
//				if(clusterID >= 0 && clusterID < p.p.getCurrentField().clusters.size())
//				{
//					if(clusterID != selectedCluster)
//					{
//						selectedCluster = clusterID;
//
//						if(p.p.p.debug.map) 
//							PApplet.println("Selected new cluster:"+selectedCluster);
//
//						PVector itemSelectedLoc = new PVector(itemSelected.x(), itemSelected.y(), itemSelected.z());
//						for(SelectableClusterLocation scl : selectableClusterLocations)
//						{
//							if(scl.id == clusterID)
//							{
//								if(!scl.location.equals(itemSelectedLoc))
//								{
//									selectableClustersCreated = false;							// Fix potential bug in Shape3D library
//									selectedCluster = -1;
//									createSelectableClusters(curMapWidth, curMapHeight);
//
////									for(SelectableClusterLocation sclTest : selectableClusterLocations)
////									{
////										if(sclTest.location.equals(itemSelectedLoc))
////										{
////											selectedCluster = sclTest.id;				// -- Needed?
////											{
////												PApplet.println("sclTest.id "+sclTest.id+" location equals itemSelectedLoc");
////												WMV_Cluster c = p.p.getCluster(clusterID); 
////												PVector clusterMapLoc = getMapLocation(c.getLocation(), curMapWidth, curMapHeight);
////												clusterMapLoc.add(new PVector(largeMapXOffset, largeMapYOffset, p.hudDistance * mapDistance));
////												clusterMapLoc.add(new PVector(mapLeftEdge, mapTopEdge, 0));
////												PApplet.println("TEST: cluster map x:"+clusterMapLoc.x+" y:"+clusterMapLoc.y+" z:"+clusterMapLoc.z);
////											}
////										}
////									}
//								}
//							}
//						}
//					}
//				}
//			}
		}
	}
	
	public void handleMouseReleased(int mouseX, int mouseY)
	{
		if(mousePressedFrame > mouseDraggedFrame)
		{
			if(p.satelliteMap)				// If mouse was most recently pressed, rather than dragged
			{
				if(selectedCluster != p.p.viewer.getCurrentClusterID())
				{
					if(selectedCluster >= 0 && selectedCluster < p.p.getCurrentField().clusters.size())
					{
						if(p.p.input.shiftKey)
						{
							p.p.viewer.teleportToCluster(selectedCluster, false);
						}
						else
						{
							p.p.viewer.teleportToCluster(selectedCluster, true);
							p.displayView = 0;
//							PApplet.println("teleportToCluster... selectedCluster:"+selectedCluster+" currentCluster:"+p.p.viewer.getCurrentClusterID()+" frameCount:"+p.p.p.frameCount);
						}
					}
				}
			}
			else 				// If mouse was most recently pressed, rather than dragged
			{
				if(selectedCluster != -1)
					zoomToCluster(p.p.getCluster(selectedCluster));
			}
		}		
	}
	
	public int getSelectedClusterID()
	{
		return selectedCluster;
	}
	
	public void setSelectedCluster( int newCluster )
	{
		selectedCluster = newCluster;
	}
	
	/**
	 * Transition map zoom from current to given value
	 */
	void mapZoomTransition(float zoomFactor)
	{
		zoomToRectangleTransition(0.f, 0.f, zoomMapWidth * zoomFactor, zoomMapHeight * zoomFactor);
	}
	
	/**
	 * Draw map border
	 * @param mapWidth
	 * @param mapHeight
	 */
	void drawMapBorder(float mapWidth, float mapHeight)
	{
		// Zoom map border
		p.p.p.stroke(133,255,255,255);	
		p.p.p.strokeWeight(1);
		
		p.p.p.pushMatrix();
		p.p.p.translate(mapLeftEdge, mapTopEdge);
		p.p.p.translate(zoomMapXOffset, zoomMapYOffset);
		p.p.p.line(0.f, 0.f, p.hudDistance * mapDistance, zoomMapWidth, 0.f, p.hudDistance * mapDistance );
		p.p.p.line(zoomMapWidth, 0, p.hudDistance * mapDistance, zoomMapWidth, zoomMapHeight, p.hudDistance * mapDistance );
		p.p.p.line(zoomMapWidth, zoomMapHeight, p.hudDistance * mapDistance, 0.f, zoomMapHeight, p.hudDistance * mapDistance );
		p.p.p.line(0.f, zoomMapHeight, p.hudDistance * mapDistance,  0.f, 0.f, p.hudDistance * mapDistance );
		p.p.p.popMatrix();
		
		if(p.p.p.debug.map)		// Large map border
		{
			p.p.p.stroke(255,255,255,255);
			p.p.p.strokeWeight(2);
			p.p.p.pushMatrix();
			p.p.p.translate(mapLeftEdge, mapTopEdge);
			p.p.p.translate(largeMapXOffset, largeMapYOffset);
			p.p.p.line(0.f, 0.f, p.hudDistance * mapDistance, mapWidth, 0.f, p.hudDistance * mapDistance );
			p.p.p.line(mapWidth, 0, p.hudDistance * mapDistance,  mapWidth, mapHeight, p.hudDistance * mapDistance );
			p.p.p.line(mapWidth, mapHeight, p.hudDistance * mapDistance,  0.f, mapHeight, p.hudDistance * mapDistance );
			p.p.p.line(0.f, mapHeight, p.hudDistance * mapDistance,  0.f, 0.f, p.hudDistance * mapDistance );
			p.p.p.popMatrix();
		}
		
		if(p.p.p.debug.map && p.p.p.debug.detailed)
		{
			p.p.p.pushMatrix();
			p.p.p.stroke(0,255,255,255);
			p.p.p.strokeWeight(25);
			WMV_Cluster c = p.p.getCurrentField().getEdgeClusterOnXAxis(true);
			PVector point = getMapLocation(c.getLocation(), curMapWidth, curMapHeight);
			p.p.p.translate(mapLeftEdge, mapTopEdge);
			p.p.p.translate(largeMapXOffset, largeMapYOffset);
			p.p.p.point(point.x, point.y, p.hudDistance * mapDistance);
			p.p.p.popMatrix();

			p.p.p.pushMatrix();
			p.p.p.stroke(100,255,255,255);
			p.p.p.strokeWeight(35);
			c = p.p.getCurrentField().getEdgeClusterOnZAxis(true);
			point = getMapLocation(c.getLocation(), curMapWidth, curMapHeight);
			p.p.p.translate(mapLeftEdge, mapTopEdge);
			p.p.p.translate(largeMapXOffset, largeMapYOffset);
			p.p.p.point(point.x, point.y, p.hudDistance * mapDistance);
			p.p.p.popMatrix();
		}
	}

	/**
	 * Zoom to rectangle in large map
	 * @param rectXOffset X offset in large map of rectangle left edge (Range: 0 to mapWidth)
	 * @param rectYOffset Y offset in large map of rectangle top edge (Range: 0 to mapHeight)
	 * @param rectWidth Width of rectangle
	 * @param rectHeight Height of rectangle
	 */
	void zoomToRectangle(float rectXOffset, float rectYOffset, float rectWidth, float rectHeight)	// -- Use clusters instead of absolute?
	{
		zoomMapLeftEdge += rectXOffset;
		zoomMapTopEdge += rectYOffset;
		zoomMapXOffset = largeMapXOffset + zoomMapLeftEdge;
		zoomMapYOffset = largeMapYOffset + zoomMapTopEdge;
		zoomMapWidth = rectWidth; 
		zoomMapHeight = rectHeight;

		mapDistance = zoomMapWidth / curMapWidth;
		mapLeftEdge = (p.p.p.width - zoomMapWidth)/2 - zoomMapLeftEdge;
		mapTopEdge = (p.p.p.height - zoomMapHeight)/2 - zoomMapTopEdge;
		
		if(p.p.p.debug.map)
		{
			PApplet.println("---> zoomToRectangle()...");
			PApplet.println("Set mapLeftEdge:" + mapLeftEdge);
			PApplet.println("Set mapTopEdge:" + mapTopEdge);
			PApplet.println("Set mapDistance:" + mapDistance);
			PApplet.println("Set zoomMapXOffset:" + zoomMapXOffset);
			PApplet.println("Set zoomMapYOffset:" + zoomMapYOffset);
			PApplet.println("Set zoomMapLeftEdge:" + zoomMapLeftEdge);
			PApplet.println("Set zoomMapTopEdge:" + zoomMapTopEdge);
			PApplet.println("Set zoomMapWidth:" + zoomMapWidth);
			PApplet.println("Set zoomMapHeight:" + zoomMapHeight);
		}
//		mapZoomTransition();
		
		selectableClustersCreated = false;
	}

	/**
	 * Zoom in smoothly on a rectangular portion of the map
	 * @param rectXOffset
	 * @param rectYOffset
	 * @param rectWidth
	 * @param rectHeight
	 */
	void zoomToRectangleTransition(float rectXOffset, float rectYOffset, float rectWidth, float rectHeight)
	{
		if(!zoomToRectangleTransition)
		{
			if(rectWidth / curMapWidth > mapDistanceMin && rectWidth / curMapWidth < mapDistanceMax )
			{
				zoomMapLeftEdge += rectXOffset;
				zoomMapTopEdge += rectYOffset;

				zoomToRectangleTransition = true;   
				zoomToRectangleTransitionStartFrame = p.p.p.frameCount;
				zoomToRectangleTransitionEndFrame = zoomToRectangleTransitionStartFrame + zoomToRectangleTransitionLength;
				selectableClustersCreated = false;

				zoomMapXOffsetTransitionStart = zoomMapXOffset;
				zoomMapXOffsetTransitionTarget = largeMapXOffset + zoomMapLeftEdge;
				zoomMapYOffsetTransitionStart = zoomMapYOffset;
				zoomMapYOffsetTransitionTarget = largeMapYOffset + zoomMapTopEdge;

				zoomMapWidthTransitionStart = zoomMapWidth;
				zoomMapWidthTransitionTarget = rectWidth;
				zoomMapHeightTransitionStart = zoomMapHeight;
				zoomMapHeightTransitionTarget = rectHeight;

				mapDistanceTransitionStart = mapDistance;
				mapDistanceTransitionTarget = zoomMapWidthTransitionTarget / curMapWidth;
				mapLeftEdgeTransitionStart = mapLeftEdge;
				mapLeftEdgeTransitionTarget = (p.p.p.width - zoomMapWidthTransitionTarget)/2 - zoomMapLeftEdge;
				mapTopEdgeTransitionStart = mapTopEdge;
				mapTopEdgeTransitionTarget = (p.p.p.height - zoomMapHeightTransitionTarget)/2 - zoomMapTopEdge;

				if(p.p.p.debug.map)
					PApplet.println("Started zoomToRectangleTransition transition...");
			}
		}
	}
	
	void updateZoomToRectangleTransition()
	{
		float newZoomMapXOffset = zoomMapXOffset;
		float newZoomMapYOffset = zoomMapYOffset;
		float newZoomMapWidth = zoomMapWidth;
		float newZoomMapHeight = zoomMapHeight;
		float newMapDistance = mapDistance;
		float newMapLeftEdge = mapLeftEdge;
		float newMapTopEdge = mapTopEdge;

		if (p.p.p.frameCount >= zoomToRectangleTransitionEndFrame)		// Reached end of transition
		{
			zoomToRectangleTransition = false;
			newZoomMapXOffset = zoomMapXOffsetTransitionTarget;
			newZoomMapYOffset = zoomMapYOffsetTransitionTarget;
			newZoomMapWidth = zoomMapWidthTransitionTarget;
			newZoomMapHeight = zoomMapHeightTransitionTarget;
			newMapDistance = mapDistanceTransitionTarget;
			newMapLeftEdge = mapLeftEdgeTransitionTarget;
			newMapTopEdge = mapTopEdgeTransitionTarget;
			selectableClustersCreated = false;
		} 
		else
		{
			if(zoomMapXOffset != zoomMapXOffsetTransitionTarget)
			{
				newZoomMapXOffset = PApplet.map(p.p.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
									  		 zoomMapXOffsetTransitionStart, zoomMapXOffsetTransitionTarget); 
			}
			if(zoomMapYOffset != zoomMapYOffsetTransitionTarget)
			{
				newZoomMapYOffset = PApplet.map(p.p.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
									  		 zoomMapYOffsetTransitionStart, zoomMapYOffsetTransitionTarget);     			
			}
			if(zoomMapWidth != zoomMapWidthTransitionTarget)
			{
				newZoomMapWidth = PApplet.map(p.p.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
								  	  	   zoomMapWidthTransitionStart, zoomMapWidthTransitionTarget); 
			}
			if(zoomMapHeight != zoomMapHeightTransitionTarget)
			{
				newZoomMapHeight = PApplet.map(p.p.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
									  	    zoomMapHeightTransitionStart, zoomMapHeightTransitionTarget);     			
			}
			if(mapDistance != mapDistanceTransitionTarget)
			{
				newMapDistance = PApplet.map(p.p.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
									  	  mapDistanceTransitionStart, mapDistanceTransitionTarget); 
			}
			if(mapLeftEdge != mapLeftEdgeTransitionTarget)
			{
				newMapLeftEdge = PApplet.map(p.p.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
									  	  mapLeftEdgeTransitionStart, mapLeftEdgeTransitionTarget);     			
			}
			if(mapTopEdge != mapTopEdgeTransitionTarget)
			{
				newMapTopEdge = PApplet.map(p.p.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
									  	 mapTopEdgeTransitionStart, mapTopEdgeTransitionTarget); 
			}
		}

		if(zoomMapXOffset != newZoomMapXOffset)
			zoomMapXOffset = newZoomMapXOffset;
		if(zoomMapYOffset != newZoomMapYOffset)
			zoomMapYOffset = newZoomMapYOffset;
		if(zoomMapWidth != newZoomMapWidth)
			zoomMapWidth = newZoomMapWidth;
		
		if(zoomMapHeight != newZoomMapHeight)
			zoomMapHeight = newZoomMapHeight;
		if(mapDistance != newMapDistance)
			mapDistance = newMapDistance;
		if(mapLeftEdge != newMapLeftEdge)
			mapLeftEdge = newMapLeftEdge;
		if(mapTopEdge != newMapTopEdge)
			mapTopEdge = newMapTopEdge;
	}
	
	/**
	 * Transition map zoom from current to given value
	 */
	void zoomRectangleScroll(float mapXOffset, float mapYOffset)
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
	void zoomRectangleScrollTransition(float mapXOffset, float mapYOffset)
	{
		if(mapXOffset != 0 || mapYOffset != 0)					// Check if already at target
		{
			if(mapXOffset != 0 && mapYOffset != 0)
			{
				zoomToRectangleTransition((1.f-mapDistance)*mapXOffset, (1.f-mapDistance)*mapYOffset, zoomMapWidth, zoomMapHeight);
			}
			else if(mapXOffset != 0 && mapYOffset == 0)
			{
				zoomToRectangleTransition((1.f-mapDistance)*mapXOffset, 0, zoomMapWidth, zoomMapHeight);
			}
			else if(mapXOffset == 0 && mapYOffset != 0)
			{
				zoomToRectangleTransition(0, (1.f-mapDistance)*mapYOffset, zoomMapWidth, zoomMapHeight);
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
				scrollTransition = true;   
				scrollTransitionStartFrame = p.p.p.frameCount;
				scrollTransitionEndFrame = scrollTransitionStartFrame + scrollTransitionLength;
				selectableClustersCreated = false;
				
				if(mapXOffset != 0 && mapYOffset != 0)
				{
					mapXTransitionStart = mapLeftEdge;
					mapXTransitionTarget = mapLeftEdge + mapXOffset;
					mapYTransitionStart = mapTopEdge;
					mapYTransitionTarget = mapTopEdge + mapYOffset;
				}
				else if(mapXOffset != 0 && mapYOffset == 0)
				{
					mapXTransitionStart = mapLeftEdge;
					mapXTransitionTarget = mapLeftEdge + mapXOffset;
					mapYTransitionTarget = mapTopEdge;

				}
				else if(mapXOffset == 0 && mapYOffset != 0)
				{
					mapXTransitionTarget = mapLeftEdge;
					mapYTransitionStart = mapTopEdge;
					mapYTransitionTarget = mapTopEdge + mapYOffset;
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
		
		if(p.p.p.debug.map)
		{
			PApplet.println("Updated mapLeftEdge:"+mapLeftEdge);
			PApplet.println("Updated mapTopEdge:"+mapTopEdge);
		}
	}

	/**
	 * Zoom out on whole map
	 */
	void resetMapZoom(boolean transition)
	{
//		mapLeftEdge = 0.f;
//		mapTopEdge = 0.f;
		zoomMapLeftEdge = 0.f;
		zoomMapTopEdge = 0.f;

		if(transition)
			zoomToRectangleTransition(0, 0, curMapWidth, curMapHeight);	
		else
			zoomToRectangle(0, 0, curMapWidth, curMapHeight);	
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
	
//	void createSimpleClusters(float mapWidth, float mapHeight)
//	{
//		int pgWidth = PApplet.round(mapWidth);
//		int pgHeight = PApplet.round(mapHeight);
//
//		simpleClusters = p.p.p.createGraphics(pgWidth, pgHeight, PApplet.P3D);
//		
//		for( WMV_Cluster c : p.p.getCurrentField().clusters )	
//			if(!c.isEmpty() && c.mediaPoints > 4)
//			{
//				PVector point = c.getLocation();
//				
//				if(!p.p.p.utilities.isNaN(point.x) && !p.p.p.utilities.isNaN(point.y) && !p.p.p.utilities.isNaN(point.z))
//				{
//					PVector mapLoc = getMapLocation(point, mapWidth, mapHeight);
//
//					if(mapLoc.x < mapWidth && mapLoc.x > 0 && mapLoc.y < mapHeight && mapLoc.y > 0)
//					{
//						simpleClusters.stroke(mapClusterHue, 255.f, 255.f, 255.f);
//						float sw = PApplet.sqrt(c.mediaPoints) * 0.5f;
//						sw *= mapDistance;
//						simpleClusters.strokeWeight(sw);
//
//						simpleClusters.beginDraw();
//						simpleClusters.pushMatrix();
//						simpleClusters.point(mapLoc.x, mapLoc.y, 0.f);
//						simpleClusters.popMatrix();
//						simpleClusters.endDraw();
//					}
//				}
//			}
//		
//		simpleClustersCreated = true;
//	}
	
	private class SelectableClusterLocation
	{
		public int id;
		public PVector location;
		SelectableClusterLocation(int newID, PVector newLocation)
		{
			id = newID;
			location = newLocation;
		}
	}
}

