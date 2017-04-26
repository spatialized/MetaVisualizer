package multimediaLocator;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.data.IntList;
import toxi.math.ScaleMap;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.core.Coordinate;
import de.fhpotsdam.unfolding.events.EventDispatcher;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MarkerManager;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.marker.SimplePolygonMarker;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;

/***********************************
 * Methods for displaying interactive 2D maps of 3D environments
 * @author davidgordon
 */
public class ML_Map 
{
	/* Map */
	private UnfoldingMap map, plainMap;
	private List<SimplePolygonMarker> fieldMarkers;		// Markers for fields in library
	private List<Location> fieldMarkerCenters, allClusterLocations;
	private Location mapCenter, fieldsMapCenter, plainMapCenter;
	private EventDispatcher eventDispatcher, plainMapEventDispatcher;
	private MarkerManager<Marker> markerManager, plainMapMarkerManager;
	private MultiMarker allClustersMarker;
	private SimplePointMarker viewerMarker, plainMapViewerMarker;
	private int clusterZoomLevel = 18, fieldZoomLevel = 14;
	
	/* Graphics */
	private float hudDistance;			// Distance of the Heads-Up Display from the virtual camera -- Change with zoom level??
	private int screenWidth = -1;
	private int screenHeight = -1;
	
	/* Interaction */
	public int mousePressedFrame = -1;
	public int mouseDraggedFrame = -1;
	private int selectedCluster = -1, selectedField = -1;
	private IntList selectableClusterIDs;
//	private ArrayList<SelectableClusterLocation> selectableClusterLocations;

	private float mapDistance = 1.f;										// Obsolete soon
	private final float mapDistanceMin = 0.04f, mapDistanceMax = 1.2f;	// Obsolete soon
	private float mapLeftEdge = 0.f, mapTopEdge = 0.f;					// Obsolete soon

	private float curMapWidth, curMapHeight;							// Obsolete soon
	private float largeMapXOffset, largeMapYOffset;						// Obsolete soon

	/* Clusters */
	private boolean selectableClustersCreated = false;
	private float mapClusterHue = 112.f;
	private float mapAttractorClusterHue = 222.f;
	
	/* Media */
	float smallPointSize, mediumPointSize, largePointSize, hugePointSize;	// Obsolete soon
	float cameraPointSize;

	public final boolean mapMedia = true;
	public boolean mapImages = true, mapPanoramas = true, mapVideos = true;
	private float imageHue = 150.f, imageCaptureHue = 90.f;
	private float panoramaHue = 190.f, panoramaCaptureHue = 220.f;
	private float videoHue = 40.f, videoCaptureHue = 70.f;
	private float soundHue = 40.f;
	private float cameraHue = 140.f;

	private float mediaTransparency = 120.f;
	private float maxSaturation = 210.f, lowSaturation = 80.f;
	private float fieldAspectRatio;
	
	private float zoomMapLeftEdge = 0.f, zoomMapTopEdge = 0.f;
	private float zoomMapWidth, zoomMapHeight, zoomMapMaxWidth, zoomMapMaxHeight;
	private float zoomMapXOffset, zoomMapYOffset;
//	private float zoomMapDefaultWidth, zoomMapDefaultHeight;

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
	
	/* Fields Map */
	private final float fieldSelectedHue = 20.f, fieldSelectedSaturation = 255.f, fieldSelectedBrightness = 255.f;
	private final float clusterSaturation = 160.f, clusterBrightness = 185.f;
	private final float fieldTransparency = 80.f;
//	private final float fieldTransparency = 120.f;
	private final float fieldHueRangeLow = 50.f, fieldHueRangeHigh = 160.f;
	PVector mapVectorOrigin, mapVectorVector;

	PImage blankTile;
	
	WMV_Utilities utilities;
	ML_Display p;

	/**
	 * Constructor for 2D map
	 * @param parent Parent display object
	 * @param newScreenWidth Screen width
	 * @param newScreenHeight Screen height
	 * @param newHUDDistance Heads-Up Display distance
	 */
	public ML_Map(ML_Display parent, int newScreenWidth, int newScreenHeight, float newHUDDistance)
	{
		p = parent;
		screenWidth = newScreenWidth;
		screenHeight = newScreenHeight;
		hudDistance = newHUDDistance;
		
		utilities = new WMV_Utilities();
		
		largeMapXOffset = -screenWidth * 0.5f;
		largeMapYOffset = -screenHeight * 0.5f;

		smallPointSize = 0.0000022f * screenWidth;
		mediumPointSize = 0.0000028f * screenWidth;
		largePointSize = 0.0000032f * screenWidth;
		hugePointSize = 0.0000039f * screenWidth;
		cameraPointSize = 0.005f * screenWidth;
	}

	/**
	 * Initialize maps
	 */
	public void initializeMaps(WMV_World world)
	{
		System.out.println("initializeMaps()..."+" frameCount:"+world.getState().frameCount);
		blankTile = world.p.loadImage("res/blank.jpg");

		initializeSatelliteMap(world);
		initializePlainMap(world);
		
		eventDispatcher = MapUtils.createDefaultEventDispatcher(world.p, map, plainMap);
		
		world.p.delay(200);
		p.initializedMaps = true;
	}
	
	/**
	 * Reset the map to initial state
	 */
	public void reset(WMV_World world)
	{
		resetMapZoom(world, false);
		initializeMaps(world);
		setSelectedCluster( -1 );
	}

	/**
	 * Initialize maps
	 * @param p Parent world
	 */
	private void initializeSatelliteMap(WMV_World p)
	{
		map = new UnfoldingMap(p.p, "Satellite", 0, 0, screenWidth, screenHeight, true, false, new Microsoft.AerialProvider());

		p.p.delay(100);
		PVector gpsLoc = utilities.getGPSLocation(p.getCurrentField(), new PVector(0,0,0));
		mapCenter = new Location(gpsLoc.y, gpsLoc.x);
		
		if(p.p.display.satelliteMap)
			zoomToField(p, p.getCurrentField());			// Start zoomed out on whole field

		map.setTweening(true);
		map.setZoomRange(2, 19);

		createClusterMarkers(p);
		p.p.delay(100);
		
		PVector vLoc = p.viewer.getGPSLocation();
		viewerMarker = new SimplePointMarker(new Location(vLoc.y, vLoc.x));
		viewerMarker.setId("viewer");
		viewerMarker.setDiameter(20.f);
		viewerMarker.setColor(p.p.color(0, 0, 255, 255));
	}

	/**
	 * Initialize maps
	 * @param p Parent world
	 */
	private void initializePlainMap(WMV_World p)
	{
		plainMap = new UnfoldingMap(p.p, "Map", 0, 0, screenWidth, screenHeight, true, false, new BlankMapProvider());
//		plainMap = new UnfoldingMap(p.p, "Map", 0, 0, screenWidth, screenHeight, true, false, new OpenStreetMap.OpenStreetMapProvider());
//		plainMap = new UnfoldingMap(p.p, "Map", 0, 0, screenWidth, screenHeight, true, false, new Google.GoogleTerrainProvider());

		plainMap.setBackgroundColor(0);
		p.p.delay(100);
		
		PVector gpsLoc = utilities.getGPSLocation(p.getCurrentField(), new PVector(0,0,0));
		plainMapCenter = new Location(gpsLoc.y, gpsLoc.x);
		
		if(!p.p.display.satelliteMap)
			zoomToField(p, p.getCurrentField());			// Start zoomed out on whole field
		
		plainMap.setTweening(true);
		plainMap.setZoomRange(2, 21);

		createPlainMapClusterMarkers(p);
		p.p.delay(100);
		
		PVector vLoc = p.viewer.getGPSLocation();
		plainMapViewerMarker = new SimplePointMarker(new Location(vLoc.y, vLoc.x));
		plainMapViewerMarker.setId("viewer");
		plainMapViewerMarker.setDiameter(20.f);
		plainMapViewerMarker.setColor(p.p.color(0, 0, 255, 255));
	}
	
	public class BlankMapProvider extends MBTilesMapProvider 
	{
		public PImage getTile(Coordinate coord)
		{
			return blankTile;
		}
	}
	
	/**
	 * Draw map filling the main window
	 */
	void displaySatelliteMap(WMV_World world)
	{
		PVector vLoc = world.viewer.getGPSLocation();
		Location gpsLoc = new Location(vLoc.y, vLoc.x);
		if(gpsLoc != null)
		{
			if(viewerMarker != null)
			{
				viewerMarker.setLocation(gpsLoc);						// Update location of viewer marker
				markerManager.addMarker(viewerMarker);					// -- Needed??
			}
			else System.out.println("viewerMarker == null!"+" frameCount:"+world.getState().frameCount);
		}

		world.p.perspective();
		world.p.camera();												// Reset to default camera setting
		world.p.tint(255.f, 255.f);
		map.draw();														// Draw the Unfolding Map

//		if(mapMedia) displayClusters(world);				// -- Obsolete??
	}

	/**
	 * Draw map without satellite overlay 
	 * @param world Parent world
	 */
	public void displayPlainMap(WMV_World world)				// -- Need to finish
	{
		PVector vLoc = world.viewer.getGPSLocation();
		Location gpsLoc = new Location(vLoc.y, vLoc.x);
		if(gpsLoc != null)
		{
			if(viewerMarker != null)
			{
				viewerMarker.setLocation(gpsLoc);						// Update location of viewer marker
				plainMapMarkerManager.addMarker(viewerMarker);					// -- Needed??
			}
			else System.out.println("viewerMarker == null!"+" frameCount:"+world.getState().frameCount);
		}

		world.p.perspective();
		world.p.camera();												// Reset to default camera setting
		world.p.tint(255.f, 255.f);
		plainMap.draw();														// Draw the Unfolding Map
	}

	/**
	 * Display spatial clusters in the current field on map
	 * @param world Parent world
	 */
//	private void displayClusters(WMV_World world)
//	{
//		float mapWidth = curMapWidth;
//		float mapHeight = curMapHeight;
//
//		if(selectedCluster >= 0)
//		{
//			if(selectedCluster < world.getCurrentField().getClusters().size())
//			{
//				WMV_Cluster c = world.getCurrentField().getCluster(selectedCluster);
//
//				if(selectedCluster == world.viewer.getState().getCurrentClusterID())
//					drawClusterMedia(world, c, mapWidth, mapHeight, false);
//				else
//					drawClusterMedia(world, c, mapWidth, mapHeight, true);
//			}
//			else
//				setSelectedCluster( -1 );
//		}
//
//		if(selectedCluster != world.viewer.getState().getCurrentClusterID() && world.viewer.getState().getCurrentClusterID() >= 0)
//		{
//			WMV_Cluster c = world.getCurrentCluster();
//			if(c != null)
//				drawClusterMedia(world, c, mapWidth, mapHeight, false);
//		}
//	}

//	/**
//	 * Draw map of media and viewer without UnfoldingMaps library
//	 * @param world Parent world
//	 * @param mapWidth Map width
//	 * @param mapHeight Map height
//	 * @param mapXOffset Map X offset
//	 * @param mapYOffset Map Y offset
//	 */
//	private void displayMap(WMV_World world, float mapWidth, float mapHeight, float mapXOffset, float mapYOffset)	// -- Update this
//	{
//		if(!scrollTransition && !zoomToRectangleTransition && !world.p.state.interactive)
//		{
//			if(!selectableClustersCreated)
//				createSelectableClusters(world, mapWidth, mapHeight);
//		}
//		
//		p.startHUD(world);
//
//		/* Media */
////		if(!scrollTransition && !zoomToRectangleTransition && !world.interactive)
////		{
////			if((mapImages && !p.p.getCurrentField().hideImages))
////				for ( WMV_Image i : p.p.getCurrentField().images )					// Draw image capture locations on 2D Map
////					drawImageOnMap(i, false, mapWidth, mapHeight, false);
////
////			if((mapPanoramas && !p.p.getCurrentField().hidePanoramas))
////				for ( WMV_Panorama n : p.p.getCurrentField().panoramas )			// Draw panorama capture locations on 2D Map
////					drawPanoramaOnMap(n, false, mapWidth, mapHeight, false);
////
////			if((mapVideos && !p.p.getCurrentField().hideVideos))
////				for (WMV_Video v : p.p.getCurrentField().videos)					// Draw video capture locations on 2D Map
////					drawVideoOnMap(v, false, mapWidth, mapHeight, false);
////		}
//
//		/* Clusters */
//		if(mapMedia)
//		{
//			if(!scrollTransition && !zoomToRectangleTransition && !world.p.state.interactive)
//			{
////				drawSelectableClusters();
//
//				if(selectedCluster >= 0)
//				{
//					WMV_Cluster c = world.getCurrentField().getCluster(selectedCluster);
////					if(!c.isEmpty() && c.mediaCount != 0)
////						highlightCluster( c.getLocation(), PApplet.sqrt(c.mediaCount)*0.5f, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, mediaTransparency );
//					
//					if(selectedCluster == world.viewer.getState().getCurrentClusterID())
//						drawClusterMedia(world, c, mapWidth, mapHeight, false);
//					else
//						drawClusterMedia(world, c, mapWidth, mapHeight, true);
//				}
//
//				if(selectedCluster != world.viewer.getState().getCurrentClusterID() && world.viewer.getState().getCurrentClusterID() >= 0)
//				{
//					WMV_Cluster c = world.getCurrentCluster();
//					if(c != null)
//					{
////						if(!c.isEmpty() && c.mediaCount != 0)
////							highlightCluster( c.getLocation(), PApplet.sqrt(c.mediaCount)*0.5f, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, mediaTransparency );
//						drawClusterMedia(world, c, mapWidth, mapHeight, false);
//					}
//				}
//			}
//			else
//			{
//				drawSimpleClusters(world, mapWidth, mapHeight);
//			}
//		}
//		
//		if(!scrollTransition && !zoomToRectangleTransition)
//		{
//			if(!world.p.state.interactive)													// While not in Clustering Mode
//			{
//				if(world.viewer.getAttractorClusterID() != -1 && world.viewer.getAttractorClusterID() < world.getFieldClusters().size())
//					drawPoint( world, world.viewer.getAttractorCluster().getLocation(), hugePointSize * mapWidth, mapWidth, mapHeight, mapAttractorClusterHue, 255.f, 255.f, mediaTransparency );
//
//				WMV_Cluster c = world.getCurrentCluster();
//				if(c != null)
//				{
//					if(world.viewer.getState().getCurrentClusterID() != -1 && world.viewer.getState().getCurrentClusterID() < world.getFieldClusters().size())
//						drawPoint( world, c.getLocation(), hugePointSize * mapWidth, mapWidth, mapHeight, mapAttractorClusterHue, 255.f, 255.f, mediaTransparency );
//				}
//				
//				drawViewer(world, mapWidth, mapHeight);
//			}
//
//			if(world.viewer.getGPSTrack().size() > 0)
//				drawPathOnMap(world, world.viewer.getGPSTrack(), mapWidth, mapHeight);
//			
////			drawOriginOnMap(mapWidth, mapHeight);
//		}
//
//		if(world.p.debugSettings.map)
//			drawMapBorder(world, mapWidth, mapHeight);
//	}

	/**
	 * Draw map of media and viewer without UnfoldingMaps library
	 * @param world Parent world
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @param mapXOffset Map X offset
	 * @param mapYOffset Map Y offset
	 */
//	private void displayMap(WMV_World world, float mapWidth, float mapHeight, float mapXOffset, float mapYOffset)	// -- Update this
//	{
////		if(!scrollTransition && !zoomToRectangleTransition && !world.p.state.interactive)
////		{
////			if(!selectableClustersCreated)
////				createSelectableClusters(world, mapWidth, mapHeight);
////		}
//		
//		p.startHUD(world);
//
//		/* Media */
////		if(!scrollTransition && !zoomToRectangleTransition && !world.interactive)
////		{
////			if((mapImages && !p.p.getCurrentField().hideImages))
////				for ( WMV_Image i : p.p.getCurrentField().images )					// Draw image capture locations on 2D Map
////					drawImageOnMap(i, false, mapWidth, mapHeight, false);
////
////			if((mapPanoramas && !p.p.getCurrentField().hidePanoramas))
////				for ( WMV_Panorama n : p.p.getCurrentField().panoramas )			// Draw panorama capture locations on 2D Map
////					drawPanoramaOnMap(n, false, mapWidth, mapHeight, false);
////
////			if((mapVideos && !p.p.getCurrentField().hideVideos))
////				for (WMV_Video v : p.p.getCurrentField().videos)					// Draw video capture locations on 2D Map
////					drawVideoOnMap(v, false, mapWidth, mapHeight, false);
////		}
//
//		/* Clusters */
//		if(mapMedia)
//		{
//			if(!scrollTransition && !zoomToRectangleTransition && !world.p.state.interactive)
//			{
////				drawSelectableClusters();
//
//				if(selectedCluster >= 0)
//				{
//					WMV_Cluster c = world.getCurrentField().getCluster(selectedCluster);
////					if(!c.isEmpty() && c.mediaCount != 0)
////						highlightCluster( c.getLocation(), PApplet.sqrt(c.mediaCount)*0.5f, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, mediaTransparency );
//					
//					if(selectedCluster == world.viewer.getState().getCurrentClusterID())
//						drawClusterMedia(world, c, mapWidth, mapHeight, false);
//					else
//						drawClusterMedia(world, c, mapWidth, mapHeight, true);
//				}
//
//				if(selectedCluster != world.viewer.getState().getCurrentClusterID() && world.viewer.getState().getCurrentClusterID() >= 0)
//				{
//					WMV_Cluster c = world.getCurrentCluster();
//					if(c != null)
//					{
////						if(!c.isEmpty() && c.mediaCount != 0)
////							highlightCluster( c.getLocation(), PApplet.sqrt(c.mediaCount)*0.5f, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, mediaTransparency );
//						drawClusterMedia(world, c, mapWidth, mapHeight, false);
//					}
//				}
//			}
//			else
//			{
//				drawSimpleClusters(world, mapWidth, mapHeight);
//			}
//		}
//		
//		if(!scrollTransition && !zoomToRectangleTransition)
//		{
//			WMV_Field curField = world.getCurrentField();
//			if(!world.p.state.interactive)													// While not in Clustering Mode
//			{
//				if(world.viewer.getAttractorClusterID() != -1 && world.viewer.getAttractorClusterID() < world.getFieldClusters().size())
//					drawPoint( world, curField, world.viewer.getAttractorCluster().getLocation(), hugePointSize * mapWidth, mapWidth, mapHeight, mapAttractorClusterHue, 255.f, 255.f, mediaTransparency );
//
//				WMV_Cluster c = world.getCurrentCluster();
//				if(c != null)
//				{
//					if(world.viewer.getState().getCurrentClusterID() != -1 && world.viewer.getState().getCurrentClusterID() < world.getFieldClusters().size())
//						drawPoint( world, curField, c.getLocation(), hugePointSize * mapWidth, mapWidth, mapHeight, mapAttractorClusterHue, 255.f, 255.f, mediaTransparency );
//				}
//				
//				drawViewer(world, curField, mapWidth, mapHeight);
//			}
//
//			if(world.viewer.getGPSTrack().size() > 0)
//				drawPathOnMap(world, curField, world.viewer.getGPSTrack(), mapWidth, mapHeight);
//			
////			drawOriginOnMap(mapWidth, mapHeight);
//		}
//
////		if(world.p.debugSettings.map)
////			drawMapBorder(world, mapWidth, mapHeight);
//	}

	/**
	 * Draw (nonselectable) points representing clusters
	 * @param mapWidth
	 * @param mapHeight
	 */
//	void drawSimpleClusters(WMV_World world, float mapWidth, float mapHeight)
//	{
//		for( WMV_Cluster c : world.getCurrentField().getClusters() )	
//		{
//			if(!c.isEmpty() && c.getState().mediaCount > 5)
//			{
//				PVector point = c.getLocation();
//
//				if( pointIsVisible(world, point, false) )
//				{
//					float radius = PApplet.sqrt(c.getState().mediaCount) * 0.7f;
//					drawPoint(world, world.getCurrentField(), point, radius, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, mediaTransparency );
//				}
//			}
//		}
//	}

	/**
	 * Zoom in on cluster
	 * @param c Cluster to zoom in on
	 */
	void zoomToCluster(WMV_World world, WMV_Cluster c)
	{
		if(!c.isEmpty() && c.getState().mediaCount > 0)
		{
			PVector mapLoc = c.getLocation();
			PVector gpsLoc = utilities.getGPSLocation(world.getCurrentField(), mapLoc);

			if(p.displayView == 1)
			{
				if(p.satelliteMap)
					map.zoomAndPanTo(clusterZoomLevel, new Location(gpsLoc.y, gpsLoc.x));
				else
					plainMap.zoomAndPanTo(clusterZoomLevel, new Location(gpsLoc.y, gpsLoc.x));
			}
			else
				map.zoomAndPanTo(clusterZoomLevel, new Location(gpsLoc.y, gpsLoc.x));
		}
	}

	/**
	 * Zoom in on cluster
	 * @param c Cluster to zoom in on
	 */
	void zoomToField(WMV_World world, WMV_Field f)
	{
		PVector gpsLoc = new PVector(f.getModel().getState().centerLongitude, f.getModel().getState().centerLatitude);
		if(p.displayView == 1)
		{
			if(p.satelliteMap)
				map.zoomAndPanTo(fieldZoomLevel, new Location(gpsLoc.y, gpsLoc.x));
			else
				plainMap.zoomAndPanTo(fieldZoomLevel, new Location(gpsLoc.y, gpsLoc.x));
		}
		else
			map.zoomAndPanTo(fieldZoomLevel, new Location(gpsLoc.y, gpsLoc.x));
	}

	/**
	 * Zoom in map	-- Obsolete?
	 */
	public void zoomIn(WMV_World world)
	{
		if(p.displayView == 1)
		{
			if (p.satelliteMap) map.zoomIn();
			else plainMap.zoomIn();
		}
		else
			map.zoomIn();
	}
	
	/**
	 * Zoom out map	-- Obsolete?
	 */
	public void zoomOut(WMV_World world)
	{
		if(p.displayView == 1)
		{
			if (p.satelliteMap) map.zoomOut();
			else plainMap.zoomOut();
		}
		else
			map.zoomOut();
	}

	/**
	 * Check if a location is visible on map
	 * @param point The location to check
	 * @param mapCoords Whether the point is in map or in virtual world coordinates (m.)
	 * @return Whether the location is visible on map
	 */
	boolean pointIsVisible(WMV_World world, PVector loc, boolean mapCoords)
	{
		PVector point = new PVector(loc.x, loc.y, loc.z);
		
		float xOff = zoomMapXOffset - largeMapXOffset;
		float yOff = zoomMapYOffset - largeMapYOffset;
		
		if(!mapCoords)														// Convert given world coords to map coords
			point = getMapLocation(world, world.getCurrentField(), loc, curMapWidth, curMapHeight);

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
	void drawClusterMedia(WMV_World world, WMV_Cluster c, float mapWidth, float mapHeight, boolean ignoreTime)
	{
		WMV_Field curField = world.getCurrentField();
		
		if((mapImages && !world.viewer.getSettings().hideImages))
			for ( int i : c.getState().images )									// Draw images on Map
			{
				WMV_Image img = world.getCurrentField().getImage(i);
				drawImageOnMap(world, curField, img, ignoreTime, mapWidth, mapHeight, false);
				if(world.getState().showModel)
				{
//					drawLine(c.getLocation(), img.getLocation(), 60.f, 160.f, 255.f, mapWidth, mapHeight);
					if(world.getState().showMediaToCluster)
						drawLine(world, c.getLocation(), img.getLocation(), 40.f, 155.f, 200.f, mapWidth, mapHeight);

					if(world.getState().showCaptureToMedia)
						drawLine(world, img.getLocation(), img.getCaptureLocation(), 160.f, 100.f, 255.f, mapWidth, mapHeight);

					if(world.getState().showCaptureToCluster)
						drawLine(world, c.getLocation(), img.getCaptureLocation(), 100.f, 55.f, 255.f, mapWidth, mapHeight);
				}
			}

		if((mapPanoramas && !world.viewer.getSettings().hidePanoramas))
			for ( int n : c.getState().panoramas )									// Draw panoramas on Map
			{
				WMV_Panorama pano = world.getCurrentField().getPanorama(n);
				drawPanoramaOnMap(world, curField, pano, ignoreTime, mapWidth, mapHeight, false);
			}

		if((mapVideos && !world.viewer.getSettings().hideVideos))
			for (int v : c.getState().videos)										// Draw videos on Map
			{
				WMV_Video vid = world.getCurrentField().getVideo(v);
				drawVideoOnMap(world, curField, vid, ignoreTime, mapWidth, mapHeight, false);
				if(world.getState().showModel)
				{
					if(world.getState().showMediaToCluster)
						drawLine(world, c.getLocation(), vid.getLocation(), 140.f, 155.f, 200.f, mapWidth, mapHeight);

					if(world.getState().showCaptureToMedia)
						drawLine(world, vid.getLocation(), vid.getCaptureLocation(), 50.f, 100.f, 255.f, mapWidth, mapHeight);

					if(world.getState().showCaptureToCluster)
						drawLine(world, c.getLocation(), vid.getCaptureLocation(), 190.f, 55.f, 255.f, mapWidth, mapHeight);
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
	void drawLine(WMV_World world, PVector point1, PVector point2, float hue, float saturation, float brightness, float mapWidth, float mapHeight)
	{
		if(p.satelliteMap)
		{
			// -- Need to implement
		}
		else
		{
			PVector mapLoc1 = getMapLocation(world, world.getCurrentField(), point1, mapWidth, mapHeight);
			PVector mapLoc2 = getMapLocation(world, world.getCurrentField(), point2, mapWidth, mapHeight);

			if( (mapLoc1.x < mapWidth && mapLoc1.x > 0 && mapLoc1.y < mapHeight && mapLoc1.y > 0) ||
					(mapLoc2.x < mapWidth && mapLoc2.x > 0 && mapLoc2.y < mapHeight && mapLoc2.y > 0) )
			{
				float pointSize = smallPointSize * 0.1f * mapWidth / (float)Math.sqrt(Math.sqrt(mapDistance));
				world.p.strokeWeight(pointSize);
				world.p.stroke(hue, saturation, brightness, 255.f);
				world.p.pushMatrix();
				world.p.translate(mapLeftEdge, mapTopEdge);
				world.p.line( largeMapXOffset + mapLoc1.x, largeMapYOffset + mapLoc1.y, hudDistance * mapDistance,
						largeMapXOffset + mapLoc2.x, largeMapYOffset + mapLoc2.y, hudDistance * mapDistance );
				world.p.popMatrix();
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
	void drawImageOnMap(WMV_World world, WMV_Field f, WMV_Image image, boolean ignoreTime, float mapWidth, float mapHeight, boolean capture)
	{
		float pointSize = smallPointSize * mapWidth;
		float saturation = maxSaturation;
		float imageDistance = image.getViewingDistance();   // Get photo distance from current camera position
		boolean visible = ignoreTime;

		if (imageDistance < world.viewer.getSettings().getFarViewingDistance() && imageDistance > world.viewer.getSettings().getNearClippingDistance())    // If image is in visible range
			visible = true;                                              

		if(visible && image.getMediaState().location != null && !image.getMediaState().disabled && !image.getMediaState().hidden)
		{
			float alpha = 255.f;
			if(!ignoreTime && world.getState().timeFading)
				alpha = image.getTimeBrightness() * mediaTransparency;

			if(alpha > 0.f)
			{
				if(image.isSelected()) pointSize *= 5.f;
				if(capture)
					drawPoint( world, f, image.getCaptureLocation(), pointSize, mapWidth, mapHeight, imageCaptureHue, saturation, 255.f, mediaTransparency );
				else
					drawPoint( world, f, image.getLocation(),  pointSize, mapWidth, mapHeight, imageHue, saturation, 255.f, mediaTransparency );
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
	void drawPanoramaOnMap(WMV_World world, WMV_Field f, WMV_Panorama panorama, boolean ignoreTime, float mapWidth, float mapHeight, boolean capture)
	{
		float pointSize = mediumPointSize * mapWidth;
		float saturation = maxSaturation;
		float panoramaDistance = panorama.getViewingDistance();   // Get photo distance from current camera position
		boolean visible = ignoreTime;

		if (panoramaDistance < world.viewer.getSettings().getFarViewingDistance() && panoramaDistance > world.viewer.getSettings().getNearClippingDistance())    // If panorama is in visible range
			visible = true;                                              

		if(visible && panorama.getMediaState().location != null && !panorama.getMediaState().disabled && !panorama.getMediaState().hidden)
		{
			float alpha = 255.f;
			if(!ignoreTime && world.getState().timeFading)
				alpha = panorama.getTimeBrightness() * mediaTransparency;

			if(alpha > 0.f)
			{
				if(panorama.isSelected()) pointSize *= 5.f;
				drawPoint( world, f, panorama.getLocation(),  pointSize, mapWidth, mapHeight, panoramaHue, saturation, 255.f, mediaTransparency );
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
	void drawVideoOnMap(WMV_World world, WMV_Field f, WMV_Video video, boolean ignoreTime, float mapWidth, float mapHeight, boolean capture)
	{
		float pointSize = mediumPointSize * mapWidth;
		float saturation = maxSaturation;
		float videoDistance = video.getViewingDistance();   // Get photo distance from current camera position
		boolean visible = ignoreTime;

		if (videoDistance < world.viewer.getSettings().getFarViewingDistance() && videoDistance > world.viewer.getSettings().getNearClippingDistance())    // If video is in visible range
			visible = true;                                              

		if(visible && video.getMediaState().location != null && !video.getMediaState().disabled && !video.getMediaState().hidden)
		{
			float alpha = 255.f;
			if(!ignoreTime && world.getState().timeFading)
				alpha = video.getTimeBrightness() * mediaTransparency;

			if(alpha > 0.f)
			{
				if(video.isSelected()) pointSize *= 5.f;
				if(capture)
					drawPoint( world, f, video.getCaptureLocation(), pointSize, mapWidth, mapHeight, videoCaptureHue, saturation, 255.f, mediaTransparency );
				else
					drawPoint( world, f, video.getLocation(), pointSize, mapWidth, mapHeight, videoHue, saturation, 255.f, mediaTransparency );
			}
		}
	}

//	/**
//	 * Draw the map origin
//	 * @param mapWidth
//	 * @param mapHeight
//	 */
//	void drawOriginOnMap(WMV_World world, float mapWidth, float mapHeight)
//	{
//		int size = (int)(mapWidth / 40.f);
//		for(int i=-size/2; i<size/2; i+=size/10)
//			drawPoint( world, new PVector(i, 0.f, 0.f), hugePointSize * mapWidth, mapWidth, mapHeight, 180.f, 30.f, 255.f, mediaTransparency / 2.f );
//		for(int i=-size/2; i<size/2; i+=size/10)
//			drawPoint( world, new PVector(0.f, 0.f, i), hugePointSize * mapWidth, mapWidth, mapHeight, 180.f, 30.f, 255.f, mediaTransparency / 2.f );
//	}
	
	/**
	 * Draw viewer as arrow on map
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * Draw current viewer location and orientation on map of specified size
	 */
	void drawViewer(WMV_World world, WMV_Field f, float mapWidth, float mapHeight)
	{
		PVector camLoc = world.viewer.getLocation();
		if(pointIsVisible(world, camLoc, false))
		{
			float camYaw = -world.viewer.getXOrientation() - 0.5f * PApplet.PI;

			drawPoint( world, f, camLoc, cameraPointSize, mapWidth, mapHeight, cameraHue, 255.f, 255.f, mediaTransparency );
			float ptSize = cameraPointSize;

			float arrowSize = fieldAspectRatio >= 1 ? world.getCurrentModel().getState().fieldWidth : world.getCurrentModel().getState().fieldLength;
			arrowSize = PApplet.round(PApplet.map(arrowSize, 0.f, 2500.f, 0.f, 100.f) * PApplet.sqrt(mapDistance));

			ScaleMap logMap;
			logMap = new ScaleMap(6., arrowSize, 6., 60.);		/* Time fading interpolation */
			logMap.setMapFunction(world.circularEaseOut);

			int arrowPoints = 15;								/* Number of points in arrow */

			logMap = new ScaleMap(0.f, 0.25f, 0.f, 0.25f);		/* Time fading interpolation */
			logMap.setMapFunction(world.circularEaseOut);

			float shrinkFactor = 0.88f;
			float mapDistanceFactor = PApplet.map(mapDistance, 0.f, 1.f, 0.f, 0.25f);
			
			for(float i=1; i<arrowPoints; i++)
			{
				world.p.textSize(ptSize);
				float x = i * cameraPointSize * mapDistanceFactor * (float)Math.cos( camYaw );
				float y = i * cameraPointSize * mapDistanceFactor * (float)Math.sin( camYaw );

				PVector arrowPoint = new PVector(camLoc.x + x, 0, camLoc.z + y);
				drawPoint( world, f, arrowPoint, ptSize, mapWidth, mapHeight, cameraHue, 120.f, 255.f, 255.f );

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
	void drawPathOnMap(WMV_World world, WMV_Field f, ArrayList<WMV_Waypoint> path, float mapWidth, float mapHeight)
	{
//		System.out.println("drawPathOnMap..."+path.size());
		float pointSize = smallPointSize * mapWidth;
		
		float saturation = maxSaturation;                                              

		for(WMV_Waypoint w : path)
		{
			drawPoint( world, f, w.getLocation(), pointSize * 4.f, mapWidth, mapHeight, 30, saturation, 255.f, mediaTransparency );
//			System.out.println("Path ---> location.x:"+w.getLocation().x+" y:"+w.getLocation().y);
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
//	void drawGMVClusters(WMV_World world)
//	{		 
//		if(  !p.initialSetup && p.messages.size() < 0 && p.metadata.size() < 0	 )
//		{
//			world.p.hint(PApplet.DISABLE_DEPTH_TEST);						// Disable depth testing for drawing HUD
//		}
//
//		for( WMV_Cluster c : world.getCurrentField().getClusters() )								// For all clusters at current depth
//		{
//			drawPoint( world, c.getLocation(), 5.f, curMapWidth, curMapHeight, mapClusterHue, 255.f, 255.f, mediaTransparency );
//		}
//	}

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
	public void drawPoint( WMV_World world, WMV_Field f, PVector point, float pointSize, float mapWidth, float mapHeight, float hue, float saturation, float brightness, float transparency )
	{
		if(!utilities.isNaN(point.x) && !utilities.isNaN(point.y) && !utilities.isNaN(point.z))
		{
//			if(p.satelliteMap)
//			{
				PVector gpsPoint = utilities.getGPSLocation(f, point);
				Location mapPoint = new Location(gpsPoint.y, gpsPoint.x);
				ScreenPosition screenPos = plainMap.getScreenPosition(mapPoint);
				
//				System.out.println("point.x:"+point.x+" point.y:"+point.y+" point.z:"+point.z);
//				System.out.println("gpsPoint.x:"+gpsPoint.x+" gpsPoint.y:"+gpsPoint.y);
//				System.out.println("mapPoint.x:"+mapPoint.x+" mapPoint.y:"+mapPoint.y);
//				System.out.println("screenPos.x:"+screenPos.x+" screenPos.y:"+screenPos.y+" screenPos.z:"+screenPos.z+" map center x:"+
//						map.getCenter().x+" y:"+map.getCenter().y+" z:"+map.getCenter().z);
//				System.out.println(" map top left x:"+ map.getTopLeftBorder().x+" y:"+map.getTopLeftBorder().y+" z:"+map.getTopLeftBorder().z);
//				System.out.println(" map bottom right x:"+ map.getBottomRightBorder().x+" y:"+map.getBottomRightBorder().y+" z:"+map.getBottomRightBorder().z);
				
				world.p.stroke(hue, saturation, brightness, transparency);
//				world.p.strokeWeight(pointSize / PApplet.sqrt(PApplet.sqrt(mapDistance)));
				world.p.strokeWeight(pointSize * 10.f);
				
				world.p.pushMatrix();
//				world.p.point(screenPos.x, screenPos.y, screenPos.z);
				world.p.popMatrix();
//			}
//			else
//			{
//				PVector mapLoc = getMapLocation(world, point, mapWidth, mapHeight);
//
//				if(mapLoc.x < mapWidth && mapLoc.x > 0 && mapLoc.y < mapHeight && mapLoc.y > 0)
//				{
//					world.p.stroke(hue, saturation, brightness, transparency);
//					world.p.strokeWeight(pointSize / PApplet.sqrt(PApplet.sqrt(mapDistance)));
//					world.p.pushMatrix();
//					world.p.translate(mapLeftEdge, mapTopEdge);
//					world.p.point(largeMapXOffset + mapLoc.x, largeMapYOffset + mapLoc.y, hudDistance * mapDistance);
//					world.p.popMatrix();
//				}
//			}
		}
//		else if(world.p.debug.map) 
//			p.message(worldSettings, "Map point is NaN!:"+point+" hue:"+hue);
	}
	
	/**
	 * Create simple point markers for each cluster
	 */
	private void createClusterMarkers(WMV_World world)
	{
		markerManager = new MarkerManager<Marker>();
		for( WMV_Cluster c : world.getCurrentField().getClusters() )	
		{
			if(!c.isEmpty() && c.getState().mediaCount != 0)
			{
				PVector mapLoc = c.getLocation();
				PVector gpsLoc = utilities.getGPSLocation(world.getCurrentField(), mapLoc);
				SimplePointMarker marker = new SimplePointMarker(new Location(gpsLoc.y, gpsLoc.x));
				marker.setId("Cluster_"+String.valueOf(c.getID()));
				marker.setColor(world.p.color(100.f, 165.f, 215.f, 225.f));			// Same color as time segments in Time View
				marker.setHighlightColor(world.p.color(170, 255, 255, 255.f));
				marker.setStrokeWeight(0);
				marker.setDiameter((float)Math.sqrt(c.getState().mediaCount) * 3.f);
				markerManager.addMarker(marker);
			}
		}

		map.addMarkerManager(markerManager);
		markerManager.enableDrawing();
	}
	
	/**
	 * Create simple point markers for each cluster
	 * @param Parent world
	 */
	private void createPlainMapClusterMarkers(WMV_World world)
	{
		plainMapMarkerManager = new MarkerManager<Marker>();
		for( WMV_Cluster c : world.getCurrentField().getClusters() )	
		{
			if(!c.isEmpty() && c.getState().mediaCount != 0)
			{
				PVector mapLoc = c.getLocation();
				PVector gpsLoc = utilities.getGPSLocation(world.getCurrentField(), mapLoc);
				SimplePointMarker marker = new SimplePointMarker(new Location(gpsLoc.y, gpsLoc.x));
				marker.setId("Cluster_"+String.valueOf(c.getID()));
				marker.setColor(world.p.color(100.f, 165.f, 215.f, 225.f));			// Same color as time segments in Time View
				marker.setHighlightColor(world.p.color(170, 255, 255, 255.f));
				marker.setStrokeWeight(0);
				marker.setDiameter((float)Math.sqrt(c.getState().mediaCount) * 3.f);
				plainMapMarkerManager.addMarker(marker);
			}
		}
		
		plainMap.addMarkerManager(plainMapMarkerManager);
		plainMapMarkerManager.enableDrawing();
	}
	
	/**
	 * Get map location for a given point
	 * @param world Parent world
	 * @param point Given point
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @return
	 */
	PVector getMapLocation(WMV_World world, WMV_Field f, PVector point, float mapWidth, float mapHeight)
	{
		PVector gpsPoint = utilities.getGPSLocation(f, point);
		ScreenPosition screenPos = plainMap.getScreenPosition(new Location(gpsPoint.y, gpsPoint.x));
		return new PVector(screenPos.x, screenPos.y);
		
		/* Old method */
//		WMV_Model m = world.getCurrentField().getModel();
//		float mapLocX, mapLocY;
//
//		if(fieldAspectRatio >= 1.f)					
//		{
//			mapLocX = PApplet.map( point.x, -0.5f * m.getState().fieldWidth, 0.5f*m.getState().fieldWidth, 0, mapWidth );		
//			mapLocY = PApplet.map( point.z, -0.5f * m.getState().fieldLength, 0.5f*m.getState().fieldLength, 0, mapWidth / fieldAspectRatio );
//		}
//		else
//		{
//			mapLocX = PApplet.map( point.x, -0.5f * m.getState().fieldWidth, 0.5f*m.getState().fieldWidth, 0, mapHeight * fieldAspectRatio );		
//			mapLocY = PApplet.map( point.z, -0.5f * m.getState().fieldLength, 0.5f*m.getState().fieldLength, 0, mapHeight );
//		}
//
//		return new PVector(mapLocX, mapLocY, 0.f);
	}

	/**
	 * Update map settings based on current mouse position
	 * @param world Parent world
	 */
	public void updateMouse(WMV_World world)
	{
		if(p.displayView == 1)						// Main map visible
		{
			if(p.satelliteMap)
			{
				for (Marker m : map.getMarkers()) 
					m.setSelected(false);

//				Marker marker = map.getFirstHitMarker(world.p.mouseX, world.p.mouseY);		// Select hit marker
				List<Marker> hitMarkers = map.getHitMarkers(world.p.mouseX, world.p.mouseY);

				for(Marker marker : hitMarkers)
				{
					if(marker != null)
					{
						String mID = marker.getId();
						if(mID != null)
						{
							String[] parts = mID.split("_");
							if(parts.length == 2)
							{
								if(parts[0].equals("Cluster"))
								{
									marker.setSelected(true);
									setSelectedCluster( Integer.parseInt(parts[1]) );
								}
							}
						}
						else
						{
							setSelectedCluster( world.viewer.getState().getCurrentClusterID() );
						}
					}
					else
					{
						setSelectedCluster( world.viewer.getState().getCurrentClusterID() );
					}
				}
			}
		}
		else if(p.displayView == 2 && p.libraryViewMode == 0)			// Fields map visible
		{
			for (Marker m : map.getMarkers()) 
				m.setSelected(false);
			
			List<Marker> hitMarkers = map.getHitMarkers(world.p.mouseX, world.p.mouseY);
			for(Marker marker : hitMarkers)
			{
				if(marker != null)
				{
					String mID = marker.getId();
					if(mID != null)
					{
						if (!marker.getId().equals("allClusters") && !mID.equals("viewer") && !mID.contains("Cluster_")) 
						{
							if(selectedField != Integer.parseInt(mID))
							{
								System.out.println("Selected Field ID:"+Integer.parseInt(mID)+" mID:"+mID);
								marker.setSelected(true);
								setSelectedField( world, Integer.parseInt(mID) );
							}
						}
					}
				}
			}

			/* Old method */
//			Marker marker = map.getFirstHitMarker(world.p.mouseX, world.p.mouseY);		// Select hit marker
//			if (marker != null) 
//			{
//				marker.setSelected(true);
//
//				String mID = marker.getId();
//
//				if(mID != null)
//				{
//					if(!mID.equals("viewer"))
//					{
//						if(selectedField != Integer.parseInt(mID))
//						{
//							System.out.println("Will set selectedField from:"+selectedField+" to:"+mID);
//							setSelectedField( world, Integer.parseInt(mID) );
////							selectedField = Integer.parseInt(mID);
//						}
//					}
//				}
//			}
//			else
//			{
//				selectedField = world.getCurrentField().getID();
//			}
		}
	}
	
	public void handleMouseReleased(WMV_World world, int mouseX, int mouseY)
	{
//		System.out.println("handleMouseReleased()... p.displayView:"+p.displayView+" p.libraryViewMode:"+p.libraryViewMode);
//		System.out.println("mousePressedFrame:"+mousePressedFrame+" mouseDraggedFrame:"+mouseDraggedFrame);
		if(mousePressedFrame > mouseDraggedFrame)
		{
			if(p.displayView == 1)
			{
				if(p.satelliteMap)				// If mouse was most recently pressed, rather than dragged
				{
					if(selectedCluster != world.viewer.getState().getCurrentClusterID())
					{
						if(selectedCluster >= 0 && selectedCluster < world.getCurrentField().getClusters().size())
						{
							world.viewer.teleportToCluster(selectedCluster, false, -1);
						}
					}
				}
				else 							// If mouse was most recently pressed, rather than dragged
				{
					if(selectedCluster != -1)
						zoomToCluster(world, world.getCurrentField().getCluster(selectedCluster));
				}
			}
			else if(p.displayView == 2 && p.libraryViewMode == 0)	// Field map visible
			{
//				System.out.println("selectedField:"+selectedField+" world.getCurrentField().getID():"+world.getCurrentField().getID());
//				if(selectedField != world.getCurrentField().getID())
//				{
				if(selectedField >= 0 && selectedField < world.getFields().size())
				{
					zoomToField(world, world.getField(selectedField));
					p.currentDisplayCluster = 0;
				}
//				}
//				else System.out.println("Selected current field...");
			}
		}		
	}
	
	public int getSelectedFieldID()
	{
		return selectedField;
	}
	
	/**
	 * Set selected field
	 * @param newField New selected field
	 */
	public void setSelectedField( WMV_World world, int newField )
	{
		selectedField = newField;
		createFieldMarkers(world);
		createAllClusterMarkers(world);
	}
	
	public int getSelectedClusterID()
	{
		return selectedCluster;
	}
	
	/**
	 * Set selected cluster
	 * @param newCluster New selected cluster
	 */
	public void setSelectedCluster( int newCluster )
	{
		selectedCluster = newCluster;
	}
	
	/**
	 * Transition map zoom from current to given value
	 */
	void mapZoomTransition(WMV_World world, float zoomFactor)
	{
		zoomToRectangleTransition(world, 0.f, 0.f, zoomMapWidth * zoomFactor, zoomMapHeight * zoomFactor);
	}
	
	/**
	 * Draw map border
	 * @param mapWidth
	 * @param mapHeight
	 */
	void drawMapBorder(WMV_World world, float mapWidth, float mapHeight)
	{
		// Zoom map border
		world.p.stroke(133,255,255,255);	
		world.p.strokeWeight(1);
		
		world.p.pushMatrix();
		world.p.translate(mapLeftEdge, mapTopEdge);
		world.p.translate(zoomMapXOffset, zoomMapYOffset);
		world.p.line(0.f, 0.f, hudDistance * mapDistance, zoomMapWidth, 0.f, hudDistance * mapDistance );
		world.p.line(zoomMapWidth, 0, hudDistance * mapDistance, zoomMapWidth, zoomMapHeight, hudDistance * mapDistance );
		world.p.line(zoomMapWidth, zoomMapHeight, hudDistance * mapDistance, 0.f, zoomMapHeight, hudDistance * mapDistance );
		world.p.line(0.f, zoomMapHeight, hudDistance * mapDistance,  0.f, 0.f, hudDistance * mapDistance );
		world.p.popMatrix();
		
		if(world.p.debugSettings.map)		// Large map border
		{
			world.p.stroke(255,255,255,255);
			world.p.strokeWeight(2);
			world.p.pushMatrix();
			world.p.translate(mapLeftEdge, mapTopEdge);
			world.p.translate(largeMapXOffset, largeMapYOffset);
			world.p.line(0.f, 0.f, hudDistance * mapDistance, mapWidth, 0.f, hudDistance * mapDistance );
			world.p.line(mapWidth, 0, hudDistance * mapDistance,  mapWidth, mapHeight, hudDistance * mapDistance );
			world.p.line(mapWidth, mapHeight, hudDistance * mapDistance,  0.f, mapHeight, hudDistance * mapDistance );
			world.p.line(0.f, mapHeight, hudDistance * mapDistance,  0.f, 0.f, hudDistance * mapDistance );
			world.p.popMatrix();
		}
		
		if(world.p.debugSettings.map && world.p.debugSettings.detailed)
		{
			world.p.pushMatrix();
			world.p.stroke(0,255,255,255);
			world.p.strokeWeight(25);
			WMV_Cluster c = world.getCurrentField().getEdgeClusterOnXAxis(true);
			PVector point = getMapLocation(world, world.getCurrentField(), c.getLocation(), curMapWidth, curMapHeight);
			world.p.translate(mapLeftEdge, mapTopEdge);
			world.p.translate(largeMapXOffset, largeMapYOffset);
			world.p.point(point.x, point.y, hudDistance * mapDistance);
			world.p.popMatrix();

			world.p.pushMatrix();
			world.p.stroke(100,255,255,255);
			world.p.strokeWeight(35);
			c = world.getCurrentField().getEdgeClusterOnZAxis(true);
			point = getMapLocation(world, world.getCurrentField(), c.getLocation(), curMapWidth, curMapHeight);
			world.p.translate(mapLeftEdge, mapTopEdge);
			world.p.translate(largeMapXOffset, largeMapYOffset);
			world.p.point(point.x, point.y, hudDistance * mapDistance);
			world.p.popMatrix();
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
		mapLeftEdge = (screenWidth - zoomMapWidth)/2 - zoomMapLeftEdge;
		mapTopEdge = (screenHeight - zoomMapHeight)/2 - zoomMapTopEdge;
		
		selectableClustersCreated = false;
	}

	/**
	 * Zoom in smoothly on a rectangular portion of the map
	 * @param rectXOffset
	 * @param rectYOffset
	 * @param rectWidth
	 * @param rectHeight
	 */
	void zoomToRectangleTransition(WMV_World world, float rectXOffset, float rectYOffset, float rectWidth, float rectHeight)
	{
		if(!zoomToRectangleTransition)
		{
			if(rectWidth / curMapWidth > mapDistanceMin && rectWidth / curMapWidth < mapDistanceMax )
			{
				zoomMapLeftEdge += rectXOffset;
				zoomMapTopEdge += rectYOffset;

				zoomToRectangleTransition = true;   
				zoomToRectangleTransitionStartFrame = world.p.frameCount;
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
				mapLeftEdgeTransitionTarget = (screenWidth - zoomMapWidthTransitionTarget)/2 - zoomMapLeftEdge;
				mapTopEdgeTransitionStart = mapTopEdge;
				mapTopEdgeTransitionTarget = (screenHeight - zoomMapHeightTransitionTarget)/2 - zoomMapTopEdge;

				if(world.p.debugSettings.map)
					System.out.println("Started zoomToRectangleTransition transition...");
			}
		}
	}
	
	void updateZoomToRectangleTransition(WMV_World world)
	{
		float newZoomMapXOffset = zoomMapXOffset;
		float newZoomMapYOffset = zoomMapYOffset;
		float newZoomMapWidth = zoomMapWidth;
		float newZoomMapHeight = zoomMapHeight;
		float newMapDistance = mapDistance;
		float newMapLeftEdge = mapLeftEdge;
		float newMapTopEdge = mapTopEdge;

		if (world.p.frameCount >= zoomToRectangleTransitionEndFrame)		// Reached end of transition
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
				newZoomMapXOffset = PApplet.map(world.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
									  		 zoomMapXOffsetTransitionStart, zoomMapXOffsetTransitionTarget); 
			}
			if(zoomMapYOffset != zoomMapYOffsetTransitionTarget)
			{
				newZoomMapYOffset = PApplet.map(world.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
									  		 zoomMapYOffsetTransitionStart, zoomMapYOffsetTransitionTarget);     			
			}
			if(zoomMapWidth != zoomMapWidthTransitionTarget)
			{
				newZoomMapWidth = PApplet.map(world.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
								  	  	   zoomMapWidthTransitionStart, zoomMapWidthTransitionTarget); 
			}
			if(zoomMapHeight != zoomMapHeightTransitionTarget)
			{
				newZoomMapHeight = PApplet.map(world.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
									  	    zoomMapHeightTransitionStart, zoomMapHeightTransitionTarget);     			
			}
			if(mapDistance != mapDistanceTransitionTarget)
			{
				newMapDistance = PApplet.map(world.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
									  	  mapDistanceTransitionStart, mapDistanceTransitionTarget); 
			}
			if(mapLeftEdge != mapLeftEdgeTransitionTarget)
			{
				newMapLeftEdge = PApplet.map(world.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
									  	  mapLeftEdgeTransitionStart, mapLeftEdgeTransitionTarget);     			
			}
			if(mapTopEdge != mapTopEdgeTransitionTarget)
			{
				newMapTopEdge = PApplet.map(world.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
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
	void zoomRectangleScrollTransition(WMV_World world, float mapXOffset, float mapYOffset)
	{
		if(mapXOffset != 0 || mapYOffset != 0)					// Check if already at target
		{
			if(mapXOffset != 0 && mapYOffset != 0)
			{
				zoomToRectangleTransition(world, (1.f-mapDistance)*mapXOffset, (1.f-mapDistance)*mapYOffset, zoomMapWidth, zoomMapHeight);
			}
			else if(mapXOffset != 0 && mapYOffset == 0)
			{
				zoomToRectangleTransition(world, (1.f-mapDistance)*mapXOffset, 0, zoomMapWidth, zoomMapHeight);
			}
			else if(mapXOffset == 0 && mapYOffset != 0)
			{
				zoomToRectangleTransition(world, 0, (1.f-mapDistance)*mapYOffset, zoomMapWidth, zoomMapHeight);
			}
		}
	}
	
	/**
	 * Transition map zoom from current to given value
	 */
	void mapScrollTransition(WMV_World world, float mapXOffset, float mapYOffset)
	{
		if(!scrollTransition)
		{
			if(mapXOffset != 0 || mapYOffset != 0)					// Check if already at target
			{
				scrollTransition = true;   
				scrollTransitionStartFrame = world.p.frameCount;
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
	public void updateMapScrollTransition(WMV_World world)
	{
		float newMapX = mapLeftEdge;
		float newMapY = mapTopEdge;

		if (world.p.frameCount >= scrollTransitionEndFrame)
		{
			scrollTransition = false;
			newMapX = mapXTransitionTarget;
			newMapY = mapYTransitionTarget;
		} 
		else
		{
			if(mapLeftEdge != mapXTransitionTarget)
			{
				newMapX = PApplet.map(world.p.frameCount, scrollTransitionStartFrame, scrollTransitionEndFrame,
									  mapXTransitionStart, mapXTransitionTarget); 
			}
			if(mapTopEdge != mapYTransitionTarget)
			{
				newMapY = PApplet.map(world.p.frameCount, scrollTransitionStartFrame, scrollTransitionEndFrame,
									  mapYTransitionStart, mapYTransitionTarget);     			
			}
		}

		if(mapLeftEdge != newMapX)
			mapLeftEdge = newMapX;
		if(mapTopEdge != newMapY)
			mapTopEdge = newMapY;
		
		if(world.p.debugSettings.map)
		{
			System.out.println("Updated mapLeftEdge:"+mapLeftEdge);
			System.out.println("Updated mapTopEdge:"+mapTopEdge);
		}
	}

	/**
	 * Zoom out on whole map
	 */
	void resetMapZoom(WMV_World world, boolean transition)
	{
//		mapLeftEdge = 0.f;
//		mapTopEdge = 0.f;
		zoomMapLeftEdge = 0.f;
		zoomMapTopEdge = 0.f;

		if(transition)
			zoomToRectangleTransition(world, 0, 0, curMapWidth, curMapHeight);	
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
	public void drawMousePointOnMap( WMV_World world, PVector point, float pointSize, float mapWidth, float mapHeight, float hue, float saturation, float brightness, float transparency )
	{		
		if(!utilities.isNaN(point.x) && !utilities.isNaN(point.y) && !utilities.isNaN(point.z))
		{
			if(point.x < mapWidth && point.x > 0 && point.y < mapHeight && point.y > 0)
			{
				world.p.stroke(hue, saturation, brightness, transparency);
				world.p.pushMatrix();

				world.p.strokeWeight(pointSize);
				world.p.translate(mapLeftEdge, mapTopEdge);
				world.p.point(largeMapXOffset + point.x, largeMapYOffset + point.y, hudDistance);
				
				world.p.popMatrix();
			}
		}
//		else p.message(worldSettings, "Map point is NaN!:"+point+" hue:"+hue);
	}
	
	/**
	 * Initialize map for all fields in world
	 * @param world Parent world
	 */
	public void initializeFieldsMap(WMV_World world)
	{
		createFieldMarkers(world);
		createAllClusterMarkers(world);
		
		float highLongitude = -100000, lowLongitude = 100000;
		float highLatitude = -100000, lowLatitude = 100000;
		
		for(WMV_Field f : world.getFields())				// -- Precalculate
		{
			if(f.getModel().getState().lowLongitude < lowLongitude)
				lowLongitude = f.getModel().getState().lowLongitude;
			if(f.getModel().getState().lowLatitude < lowLatitude)
				lowLatitude = f.getModel().getState().lowLatitude;
			if(f.getModel().getState().highLongitude > highLongitude)
				highLongitude = f.getModel().getState().highLongitude;
			if(f.getModel().getState().highLatitude > highLatitude)
				highLatitude = f.getModel().getState().highLatitude;
		}

		fieldsMapCenter = new Location(highLatitude, highLongitude);				// -- Obsolete
		setSelectedField( world, world.getCurrentField().getID() );

		map.zoomAndPanToFit(allClusterLocations);
		p.initializedFieldMap = true;
	}
	
	/**
	 * Create simple point markers for each cluster
	 * @param world Parent world
	 */
	private void createFieldMarkers(WMV_World world)
	{
		fieldMarkers = new ArrayList<SimplePolygonMarker>();
		fieldMarkerCenters = new ArrayList<Location>();
		
		int count = 0;
		for( WMV_Field f : world.getFields() )	
		{
			ArrayList<Location> locations = new ArrayList<Location>();
			
			if(f.getBorder() == null)
				f.calculateBorderPoints();
			
			for(PVector pv : f.getBorder())			
			{
				Location loc = new Location(pv.y, pv.x);
				locations.add(loc);
			}
			
			float hue = utilities.mapValue(count, 0, world.getFields().size(), fieldHueRangeLow, fieldHueRangeHigh);
			
			SimplePolygonMarker fieldMarker = new SimplePolygonMarker(locations);
			fieldMarker.setId(String.valueOf(f.getID()));
			
			fieldMarker.setStrokeWeight(0);
//			fieldMarker.setHidden(true);

			if(f.getID() == getSelectedFieldID())
			{
				fieldMarker.setColor(world.p.color(fieldSelectedHue, fieldSelectedSaturation, fieldSelectedBrightness, 0.f));		
//				fieldMarker.setColor(world.p.color(fieldSelectedHue, fieldSelectedSaturation, fieldSelectedBrightness, fieldTransparency));		
			}
			else
			{
				fieldMarker.setColor(world.p.color(hue, 255.f, 255.f, 0.f));		
//				fieldMarker.setHighlightColor(world.p.color(fieldSelectedHue, 0.f, 0.f, 0.f));
			}
			
			fieldMarker.setHighlightColor(world.p.color(fieldSelectedHue, fieldSelectedSaturation, fieldSelectedBrightness, 0.f));
			fieldMarkers.add(fieldMarker);
			
			Location fieldCenterPoint = new Location(f.getModel().getState().centerLatitude, f.getModel().getState().centerLongitude);
			fieldMarkerCenters.add(fieldCenterPoint);
			
			// Draw field center
//			WMV_ModelState m = f.getModel().getState();
//			PVector centerPoint = new PVector(m.centerLongitude, m.centerLatitude);
//			Location loc = new Location(centerPoint.y, centerPoint.x);
//			SimplePointMarker centerMarker = new SimplePointMarker(loc);
//			centerMarker.setDiameter(10.f);
//			centerMarker.setColor(world.p.color(15.f, 15.f, 255.f, 255.f));			// Same color as time segments in Time View
//			multiMarker.addMarkers(centerMarker);
			
			count++;
		}

		for(SimplePolygonMarker marker : fieldMarkers)
			map.addMarkers(marker);
	}
	
	private void createAllClusterMarkers(WMV_World world)
	{
		allClusterLocations = new ArrayList<Location>();
		allClustersMarker = new MultiMarker();
		
		int fCount = 0;
		for( WMV_Field f : world.getFields() )	
		{
			for(WMV_Cluster c : f.getClusters())
			{
				PVector cLoc = c.getLocation();
				PVector gpsLoc = utilities.getGPSLocation(f, cLoc);
				Location loc = new Location(gpsLoc.y, gpsLoc.x);
				
				float hue = utilities.mapValue(fCount, 0, world.getFields().size(), fieldHueRangeLow, fieldHueRangeHigh);
				
				SimplePointMarker clusterMarker = new SimplePointMarker(loc);
//				clusterMarker.setId(String.valueOf(c.getID()));
				clusterMarker.setId("Cluster_"+String.valueOf(c.getID()));
				clusterMarker.setDiameter((float)Math.sqrt(c.getState().mediaCount) * 3.f);

				if(selectedField == f.getID())
					clusterMarker.setColor(world.p.color(hue, clusterSaturation, clusterBrightness, fieldTransparency));			
				else
					clusterMarker.setColor(world.p.color(hue, fieldSelectedSaturation, fieldSelectedBrightness, fieldTransparency));			

				clusterMarker.setStrokeWeight(0);

				if(mapMedia) allClustersMarker.addMarkers(clusterMarker);
				allClusterLocations.add(loc);
			}
			fCount++;
		}

		allClustersMarker.setId("allClusters");
		if(mapMedia) map.addMarkers(allClustersMarker);
	}
	
	/**
	 * Display map of all fields in library
	 */
	void displayFieldsMap(WMV_World world)
	{
		PVector vLoc = world.viewer.getGPSLocation();
		Location gpsLoc = new Location(vLoc.y, vLoc.x);
		if(gpsLoc != null)
		{
			if(viewerMarker != null)
			{
				viewerMarker.setLocation(gpsLoc);						// Update location of viewer marker
				markerManager.addMarker(viewerMarker);					// -- Needed??
			}
			else System.out.println("viewerMarker == null!");
		}

		world.p.perspective();
		world.p.camera();												// Reset to default camera setting
		world.p.tint(255.f, 255.f);
		map.draw();
	}
	
	public float getMapDistance()
	{
		return mapDistance;
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
//				if(!utilities.isNaN(point.x) && !utilities.isNaN(point.y) && !utilities.isNaN(point.z))
//				{
//					PVector mapLoc = getMapLocation(point, mapWidth, mapHeight);
//
//					if(mapLoc.x < mapWidth && mapLoc.x > 0 && mapLoc.y < mapHeight && mapLoc.y > 0)
//					{
//						simpleClusters.stroke(mapClusterHue, 255.f, 255.f, 255.f);
//						float sw = (float)Math.sqrt(c.mediaPoints) * 0.5f;
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
	
}

