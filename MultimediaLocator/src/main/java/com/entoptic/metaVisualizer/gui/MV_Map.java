package main.java.com.entoptic.metaVisualizer.gui;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.core.Coordinate;
import de.fhpotsdam.unfolding.events.EventDispatcher;
import de.fhpotsdam.unfolding.events.ZoomMapEvent;
import de.fhpotsdam.unfolding.geo.Location;
//import de.fhpotsdam.unfolding.interactions.KeyboardHandler;
import de.fhpotsdam.unfolding.interactions.MouseHandler;
import de.fhpotsdam.unfolding.marker.Marker;
//import de.fhpotsdam.unfolding.marker.MarkerManager;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.marker.SimplePolygonMarker;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import main.java.com.entoptic.metaVisualizer.misc.WMV_Utilities;
import main.java.com.entoptic.metaVisualizer.model.WMV_Cluster;
import main.java.com.entoptic.metaVisualizer.model.WMV_Waypoint;
import main.java.com.entoptic.metaVisualizer.world.WMV_Field;
import main.java.com.entoptic.metaVisualizer.world.WMV_World;

/***********************************
 * Interactive 2D map of virtual media environment
 * @author davidgordon
 */
public class MV_Map 
{
	/* Map */
	private UnfoldingMap satellite;				// , osm, large, small 
	private final int mapDelay = 100;

	private List<SimplePolygonMarker> fieldMarkers;		// Markers for fields in library
	private List<Location> fieldMarkerCenters, allClusterLocations;
	
	private EventDispatcher eventDispatcher; // plainMapEventDispatcher;
	private MultiMarker allClustersMarker;
	private SimplePointMarker viewerMarker;		//, plainMapViewerMarker;
	private SimpleLinesMarker gpsTrackMarker;
	public boolean createdGPSMarker = false;
	
	private int clusterZoomLevel = 18, fieldZoomLevel = 14;
	
	/* Graphics */
	private int screenWidth = -1;
	private int screenHeight = -1;
	private final float viewerDiameter = 15.f;
	private final int viewerArrowPoints = 9;
	private float viewerArrowPointSpacingFactor;
			
	/* Interaction */
	public int mousePressedFrame = -1;
	public int mouseDraggedFrame = -1;
	public int selectedCluster = -1, selectedField = -1;
	private boolean zoomingIn = false, zoomingOut = false;
	public boolean panningLeft = false, panningRight = false, panningUp = false, panningDown = false;

	/* Media */
	float smallPointSize, mediumPointSize, largePointSize, hugePointSize;	// Obsolete soon

	public final boolean mapMedia = true;
	public boolean mapImages = true, mapPanoramas = true, mapVideos = true, mapSounds = true;
	
//	private final float imageHue = 150.f, imageCaptureHue = 90.f;
//	private final float panoramaHue = 190.f;
//	private final float videoHue = 40.f, videoCaptureHue = 70.f;
//	private final float soundHue = 40.f;
//	private final float cameraHue = 140.f;
//	private final float mediaTransparency = 120.f;
	
	/* World Map Mode */
	PVector mapVectorOrigin, mapVectorVector;
	private final float fieldSelectedHue = 20.f, fieldSelectedSaturation = 255.f, fieldSelectedBrightness = 255.f;
	private final float fieldHueRangeLow = 45.f, fieldHueRangeHigh = 165.f;
	private final float fieldTransparency = 135.f;

	/* Field Map Mode */
	private final float gpsTrackHue = 150.f, gpsTrackSaturation = 195.f, 
						gpsTrackBrightness = 245.f, gpsTrackAlpha = 205.f;
	
	private final float clusterHue = 100.f, clusterSaturation = 175.f, clusterBrightness = 195.f, clusterTransparency = 185.f;
	private final float clusterSelectedHue = 105.f, clusterSelectedBrightness = 255.f, clusterSelectedSaturation = 30.f;
	
	WMV_Utilities utilities;
	MV_Display p;
	
	PImage blankTile;

	/**
	 * Constructor for 2D map
	 * @param parent Parent display object
	 * @param newScreenWidth Screen width
	 * @param newScreenHeight Screen height
	 * @param newHUDDistance Heads-Up Display distance
	 */
	public MV_Map(MV_Display parent)
	{
		p = parent;
		
		screenWidth = p.mv.displayWidth;
		screenHeight = p.mv.displayHeight;
		
//		screenWidth = p.ml.appWidth;
//		screenHeight = p.ml.appHeight;
		
		utilities = new WMV_Utilities();
		
		smallPointSize = 0.0000022f * screenWidth;
		mediumPointSize = 0.0000028f * screenWidth;
		largePointSize = 0.0000032f * screenWidth;
		hugePointSize = 0.0000039f * screenWidth;
		viewerArrowPointSpacingFactor = 0.0033f * screenWidth;
		
//		satelliteMarkerManager = new MarkerManager<Marker>();
//		gpsMarkerManager = new MarkerManager<Marker>();
		
		blankTile = p.mv.getImageResource("blank.jpg");	// -- Move to constructor?
	}
	
	/**
	 * Initialize satellite map
	 */
	public void initialize(WMV_World world)
	{
		initializeSatelliteMap(world);

//		System.out.println("Will create markers");
		createMarkers(world);

		eventDispatcher = new EventDispatcher();
		MouseHandler mouseHandler = new MouseHandler(world.mv, satellite);
//		KeyboardHandler keyboardHandler = new KeyboardHandler(world.ml, satellite);
		eventDispatcher.addBroadcaster(mouseHandler);
//		eventDispatcher.addBroadcaster(keyboardHandler);				// Added
		eventDispatcher.register(satellite, "pan", satellite.getId());
		eventDispatcher.register(satellite, "zoom", satellite.getId());
		satellite.setActive(true);					// Added
		satellite.setTweening(true);				// Added
		
		if(p.mapViewMode == 0) setSelectedField( world, world.getCurrentField().getID() );
		
		if(!p.initializedMaps)
		{
			WMV_Cluster current = world.getCurrentCluster();
			if(current != null) 
				zoomToCluster(world, current, true);	// Zoom to current cluster
			else
			{
				System.out.println("Map.initialize()... Couldn't pan to current cluster; current cluster is null!  Zooming to center");
				PVector mapLoc = new PVector(0,0);
				mapLoc.x = world.getCurrentField().getModel().getState().centerLatitude;
				mapLoc.y = world.getCurrentField().getModel().getState().centerLongitude;
				PVector gpsLoc = utilities.getGPSLocationFromCaptureLocation(world.getCurrentField(), mapLoc);
				zoomAndPanMapTo(satellite, clusterZoomLevel, new Location(gpsLoc.y, gpsLoc.x), false);

//				satellite.panTo(  );
			}
//			if(p.mapViewMode == 0)										// World Mode: Start zoomed out on multiple fields
//				zoomToWorld(true);											
//			else if(p.mapViewMode == 1)									// Field Mode: Start zoomed out on current field
//				zoomToField(world, world.getCurrentField(), false);		
		}
		
		world.mv.delay(mapDelay);
		
		System.out.println("Map.initialize()... will set selected cluster to -1");
		setSelectedClusterID( -1 );
		p.initializedMaps = true;
	}
	
	/**
	 * Reset to initial state
	 */
	public void reset()
	{
		mousePressedFrame = -1;
		mouseDraggedFrame = -1;
		
		System.out.println("Map.reset()... will set selected cluster to -1");
		setSelectedClusterID( -1 );
		selectedField = -1;
		
		zoomingIn = false; 
		zoomingOut = false;
		panningLeft = false;
		panningRight = false;
		panningUp = false;
		panningDown = false;
	}

	/**
	 * Initialize satellite map
	 * @param p Parent world
	 */
	private void initializeSatelliteMap(WMV_World p)
	{
		satellite = new UnfoldingMap(p.mv, "Satellite", 0, 0, screenWidth, screenHeight, true, false, new Microsoft.AerialProvider());
		p.mv.delay(mapDelay);

		satellite.setBackgroundColor(0);
		satellite.setTweening(true);
		satellite.setZoomRange(2, 19);

		p.mv.display.initializedSatelliteMap = true;
		p.mv.delay(mapDelay);
	}
	
	/**
	 * Blank map tile provider
	 * @author davidgordon
	 */
	public class BlankMapProvider extends MBTilesMapProvider 
	{
		public PImage getTile(Coordinate coord)
		{
			return blankTile;
		}
	}
	
	/**
	 * Draw satellite map of current field
	 */
	public void displaySatelliteMap(WMV_World world)
	{
		PVector vLoc = world.viewer.getGPSLocation();
		Location gpsLoc = new Location(vLoc.y, vLoc.x);
		if(gpsLoc != null)
		{
			if(viewerMarker != null)
				viewerMarker.setLocation(gpsLoc);						// Update location of viewer marker
			else 
				System.out.println("Map.displaySatelliteMap()... viewerMarker == null!"+" frameCount:"+world.getState().frameCount);
		}

		world.mv.perspective();
		p.startHUD();
		world.mv.tint(255.f, 255.f);
		satellite.draw();												// Draw the Unfolding Map
		displayViewerOrientation(world, world.getCurrentField(), satellite);						// Draw the viewer arrow
	}
	
	/**
	 * Display satellite map of all fields in library
	 */
	public void displayWorldMap(WMV_World world)
	{
		PVector vLoc = world.viewer.getGPSLocation();
		Location gpsLoc = new Location(vLoc.y, vLoc.x);
		if(gpsLoc != null)
		{
			if(viewerMarker != null)
				viewerMarker.setLocation(gpsLoc);						// Update location of viewer marker

			else System.out.println("viewerMarker == null!");
		}

		world.mv.perspective();
		p.startHUD();
		world.mv.tint(255.f, 255.f);
		satellite.draw();
		displayViewerOrientation(world, world.getCurrentField(), satellite);		// Draw the viewer arrow
	}
	
	/**
	 * Zoom in on cluster
	 * @param world Parent world
	 * @param c Cluster to zoom in on
	 * @param fade Whether to fade smoothly or jump
	 */
	public void zoomToCluster(WMV_World world, WMV_Cluster c, boolean fade)
	{
		if(c != null)
		{
			if(!c.isEmpty() && c.getState().mediaCount > 0)
			{
				if(world.mv.debug.map)
					System.out.println("Zooming to cluster:"+c.getID());
				PVector mapLoc = c.getLocation();
				PVector gpsLoc = utilities.getGPSLocationFromCaptureLocation(world.getCurrentField(), mapLoc);
				zoomAndPanMapTo(satellite, clusterZoomLevel, new Location(gpsLoc.y, gpsLoc.x), fade);
//				zoomAndPanMapTo(osm, clusterZoomLevel, new Location(gpsLoc.y, gpsLoc.x), fade);
			}
		}
		else
		{
			System.out.println("Map.zoomToCluster()... ERROR cluster is NULL!");
		}
	}
	
	/**
	 * Reset map zoom
	 */
	public void resetMapZoom(boolean fade)
	{
		if(p.mapViewMode == 0)										// World Mode: Start zoomed out on multiple fields
			zoomToWorld(fade);											
		else if(p.mapViewMode == 1)									// Field Mode: Start zoomed out on current field
			zoomToField(p.mv.world.getCurrentField(), fade);		
	}
	
	/**
	 * 
	 * @param fade
	 */
	private void zoomToWorld(boolean fade)
	{
		if(fade)
		{
			satellite.zoomAndPanToFit(allClusterLocations);
		}
		else
		{
			satellite.setTweening(false);
			satellite.zoomAndPanToFit(allClusterLocations);
			satellite.zoomAndPanToFit(allClusterLocations);		// Not a typo, not reliable on first call
			satellite.setTweening(true);
		}
	}

	/**
	 * Zoom in on field
	 * @param world Parent world
	 * @param f Field to zoom in on
	 * @param fade Whether to fade smoothly or jump
	 */
	public void zoomToField(WMV_Field f, boolean fade)
	{
		PVector gpsLoc = new PVector(f.getModel().getState().centerLongitude, f.getModel().getState().centerLatitude);
		zoomAndPanMapTo(satellite, fieldZoomLevel, new Location(gpsLoc.y, gpsLoc.x), fade);
//		zoomAndPanMapTo(osm, fieldZoomLevel, new Location(gpsLoc.y, gpsLoc.x), fade);
	}
	
	/**
	 * Zoom and pan map to a location and zoom level
	 * @param zoomMap Map to zoom and pan
	 * @param fieldZoomLevel New zoom level
	 * @param location New map center
	 * @param fade Whether to fade smoothly or jump
	 */
	private void zoomAndPanMapTo(UnfoldingMap zoomMap, int fieldZoomLevel, Location location, boolean fade)
	{
		if(fade)
		{
			if(!zoomMap.isTweening()) zoomMap.setTweening(true);
			zoomMap.zoomAndPanTo(fieldZoomLevel, location);
		}
		else
		{
			if(zoomMap.isTweening()) zoomMap.setTweening(false);
			zoomMap.zoomAndPanTo(fieldZoomLevel, location);
			if(!zoomMap.isTweening()) zoomMap.setTweening(true);
		}
	}

	/**
	 * Start zooming in map
	 */
	public void startZoomingIn(WMV_World world)
	{
//		satellite.panTo( satellite.getCenter() );
		zoomingIn = true;
	}
	
	/**
	 * Start zooming out map
	 */
	public void startZoomingOut(WMV_World world)
	{
//		satellite.panTo( satellite.getCenter() );
		zoomingOut = true;
	}
	
	/**
	 * Stop zooming the map
	 */
	public void stopZooming()
	{
		if(zoomingIn) zoomingIn = false;
		if(zoomingOut) zoomingOut = false;
	}
	
	/**
	 * Start panning map to left
	 */
	public void panLeft()
	{
		panningLeft = true;
	}
	
	/**
	 * Start panning map to right
	 */
	public void panRight()
	{
		panningRight = true;
	}
	
	/**
	 * Start panning map up
	 */
	public void panUp()
	{
		panningUp = true;
	}
	
	/**
	 * Start panning map down
	 */
	public void panDown()
	{
		panningDown = true;
	}
	
	/**
	 * Stop panning the map
	 */
	public void stopPanning()
	{
		if(panningLeft) panningLeft = false;
		if(panningRight) panningRight = false;
		if(panningUp) panningUp = false;
		if(panningDown) panningDown = false;
	}

	/**
	 * Stop panning the map
	 */
	public boolean isPanning()
	{
		return panningLeft || panningRight || panningUp || panningDown;
	}
	
	/**
	 * Stop panning the map
	 */
	public boolean isZooming()
	{
		return zoomingIn || zoomingOut;
	}


	/**
	 * Create satellite map markers for clusters in current field
	 * @param world Parent world
	 */
	private void createCurrentFieldClusterMarkers(WMV_World world)
	{
//		satelliteMarkerManager = new MarkerManager<Marker>();
//		System.out.println("Map.createCurrentFieldClusterMarkers()... creating clusters...");

		ArrayList<SimplePointMarker> clusterMarkers = new ArrayList<SimplePointMarker>();
		
		for( WMV_Cluster c : world.getCurrentField().getClusters() )	
		{
			if(!c.isEmpty() && c.getState().mediaCount != 0)
			{
				PVector mapLoc = c.getLocation();
				PVector gpsLoc = utilities.getGPSLocationFromCaptureLocation(world.getCurrentField(), mapLoc);
				SimplePointMarker marker = new SimplePointMarker(new Location(gpsLoc.y, gpsLoc.x));
				
				marker.setId("Cluster_"+String.valueOf(c.getID()));
				marker.setColor(world.mv.color(clusterHue, clusterSaturation, clusterBrightness, clusterTransparency));			// Same color as time segments in Time View
				marker.setHighlightColor(world.mv.color(clusterSelectedHue, clusterSelectedSaturation, clusterSelectedBrightness, clusterTransparency));
//				if(selectedField == f.getID())
//					clusterMarker.setColor(world.ml.color(hue, clusterSaturation, clusterBrightness, clusterTransparency));			
//				else
//					clusterMarker.setColor(world.ml.color(clusterSelectedHue, clusterSelectedSaturation, clusterSelectedBrightness, clusterTransparency));			

				marker.setStrokeWeight(0);
				marker.setDiameter((float)Math.sqrt(c.getMediaWeight()) * 3.f);
//				System.out.println("	Cluster #"+c.getID()+" marker :"+"Cluster_"+String.valueOf(c.getID()));
				
				clusterMarkers.add(marker);
			}
		}

		for(SimplePointMarker marker : clusterMarkers)
		{
			satellite.addMarkers(marker);
//			satelliteMarkerManager.addMarker(marker);
		}

		world.mv.delay(mapDelay);
	}
	
	/**
	 * Create map markers
	 * @param world Parent world
	 */
	public void createMarkers(WMV_World world)
	{
		if(p.initializedMaps)
		{
			satellite.getDefaultMarkerManager().clearMarkers();
//			satelliteMarkerManager.disableDrawing();
//			satelliteMarkerManager.clearMarkers();
//			satellite.removeMarkerManager(satelliteMarkerManager);

//			satelliteMarkerManager = new MarkerManager<Marker>();
		}
		
//		if(gpsMarkerManager != null)
//		{
//			gpsMarkerManager.disableDrawing();
//			gpsMarkerManager.clearMarkers();
//			if(createdGPSMarker) 
//				satellite.removeMarkerManager(gpsMarkerManager);
//			
//			gpsMarkerManager = new MarkerManager<Marker>();
//		}

//		createViewerMarker(world);									// Viewer

		if(world.mv.display.mapViewMode == 0)						// World Mode
		{
			createFieldMarkers(world);
			createWorldClusterMarkers(world);
		}
		else														// Field Mode
		{
			createCurrentFieldClusterMarkers(world);	
			if(world.getCurrentField().getGPSTracks() != null)
			{
				if(world.getCurrentField().getGPSTracks().size() > 0)
				{
					if(world.viewer.getSelectedGPSTrackID() != -1)
					{
						createGPSTrackMarker(world, world.viewer.getGPSTrack());
					}
				}
			}
		}
		
		createViewerMarker(world);							// Viewer
		
//		satellite.addMarkerManager(satelliteMarkerManager);
//		satelliteMarkerManager.enableDrawing();
		
		world.mv.delay(mapDelay);
	}
	
	/**
	 * Create viewer marker
	 * @param world
	 */
	private void createViewerMarker(WMV_World world)
	{
		PVector vLoc = world.viewer.getGPSLocation();
		Location gpsLoc = new Location(vLoc.y, vLoc.x);
		viewerMarker = new SimplePointMarker(new Location(gpsLoc.x, gpsLoc.y));
		viewerMarker.setId("viewer");
		viewerMarker.setDiameter(viewerDiameter);
		viewerMarker.setColor(p.mv.color(0, 0, 255, 255));

//		satelliteMarkerManager.addMarker(viewerMarker);					// CHANGED 7-2-17
		satellite.addMarker(viewerMarker);				
		
		p.mv.delay(mapDelay);
	}
	
	/**
	 * Create line marker from GPS track 
	 * @param world Parent world
	 * @param gpsTrack GPS track waypoint list
	 */
	private void createGPSTrackMarker(WMV_World world, ArrayList<WMV_Waypoint> gpsTrack)
	{
		List<Location> gpsPoints = new ArrayList<Location>();
		
		for(WMV_Waypoint w : gpsTrack)					 /* Waypoint GPS location format: {longitude, latitude} */
		{
			Location loc = new Location(w.getGPSLocation().y, w.getGPSLocation().x);	 /* Unfolding Location format: {latitude, longitude} */
			gpsPoints.add(loc);
		}
		
		if(gpsPoints.size() > 0)
		{
			if(world.mv.debug.gps)
				System.out.println("Map.createGPSTrackMarker()... Adding GPS track marker to GPS map marker manager...  Length:"+gpsPoints.size());

			gpsTrackMarker = new SimpleLinesMarker(gpsPoints);
			gpsTrackMarker.setColor(world.mv.color(gpsTrackHue, gpsTrackSaturation, gpsTrackBrightness, gpsTrackAlpha));
			gpsTrackMarker.setStrokeColor(world.mv.color(gpsTrackHue, gpsTrackSaturation, gpsTrackBrightness, gpsTrackAlpha));
			gpsTrackMarker.setStrokeWeight(3);
			
			satellite.addMarker(gpsTrackMarker);
			
			createdGPSMarker = true;

			world.mv.delay(mapDelay);
		}
		else if(world.mv.debug.gps)
			System.out.println("Map.createGPSTrackMarker()...  No gpsPoint markers to add!");
	}

	/**
	 * Update map
	 */
	public void update(WMV_World world)
	{
		if(zoomingIn)
		{
			float zoomOutPct = 1.f - 0.01f * satellite.getZoomLevel();
			float zoomInPct = 1.f / zoomOutPct;

			ZoomMapEvent zoomMapEvent = new ZoomMapEvent(world.mv, satellite.getId());
			zoomMapEvent.setSubType("zoomBy");
			zoomMapEvent.setZoomDelta(zoomInPct);
			zoomMapEvent.setTransformationCenterLocation(satellite.getCenter());
			eventDispatcher.fireMapEvent(zoomMapEvent);
		}
		if(zoomingOut)
		{
			float zoomOutPct = 1.f - 0.01f * satellite.getZoomLevel();
			
			ZoomMapEvent zoomMapEvent = new ZoomMapEvent(world.mv, satellite.getId());
			zoomMapEvent.setSubType("zoomBy");
			zoomMapEvent.setZoomDelta(zoomOutPct);
			zoomMapEvent.setTransformationCenterLocation(satellite.getCenter());
			eventDispatcher.fireMapEvent(zoomMapEvent);
		}
		if(panningLeft)
		{
			float panAmount = satellite.getZoomLevel();
			satellite.panBy(panAmount, 0.f);
		}
		if(panningRight)
		{
			float panAmount = satellite.getZoomLevel();
			satellite.panBy(-panAmount, 0.f);
		}
		if(panningUp)
		{
			float panAmount = satellite.getZoomLevel();
			satellite.panBy(0.f, panAmount);
		}
		if(panningDown)
		{
			float panAmount = satellite.getZoomLevel();
			satellite.panBy(0.f, -panAmount);
		}
		
		updateMouseHovering(world);
	}
	
	/**
	 * Update map settings based on current mouse position
	 * @param world Parent world
	 */
	public void updateMouseHovering(WMV_World world)
	{
		if(p.getDisplayView() == 1)							// In Map View
		{
			if(p.mapViewMode == 1)							// Field Mode
			{
				if(world.mv.display.initializedMaps)
				{
					List<Marker> hitMarkers;
//					for (Marker m : satellite.getMarkers()) 
//						if(m.isSelected()) m.setSelected(false);

					hitMarkers = satellite.getHitMarkers(world.mv.mouseX, world.mv.mouseY);
					
					for(Marker marker : hitMarkers)
					{
						if(marker != null)
						{
							String mID = marker.getId();
							if(mID != null)
							{
//								System.out.println("Map.updateMouse()... mID:"+mID);
								String[] parts = mID.split("_");
								if(parts.length == 2)
								{
//									System.out.println("parts[0]:"+parts[0]);
									if(parts[0].equals("Cluster"))
									{
										if(!marker.isSelected())
										{
											for (Marker m : satellite.getMarkers()) 
												if(m.isSelected()) m.setSelected(false);	
											marker.setSelected(true);
											int newClusterID= Integer.parseInt(parts[1]);
											if(selectedCluster != newClusterID)
												setSelectedClusterID( newClusterID );
										}
									}
								}
							}
//							else
//							{
//								setSelectedClusterID( world.viewer.getState().getCurrentClusterID() );
//							}
						}
//						else
//						{
//							setSelectedClusterID( world.viewer.getState().getCurrentClusterID() );
//						}
					}
				}
			}
			else if(p.mapViewMode == 0)						// World Mode
			{
				if(world.mv.display.initializedMaps)
				{
//					for (Marker m : satelliteMarkerManager.getMarkers()) 
//						m.setSelected(false);
					if(satellite.getMarkers() != null)
					{
						for (Marker m : satellite.getMarkers()) 
							m.setSelected(false);

//						List<Marker> hitMarkers = satelliteMarkerManager.getHitMarkers(world.ml.mouseX, world.ml.mouseY);
						List<Marker> hitMarkers = satellite.getHitMarkers(world.mv.mouseX, world.mv.mouseY);
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
											//								System.out.println("Selected Field ID:"+Integer.parseInt(mID)+" mID:"+mID);
											marker.setSelected(true);
											setSelectedField( world, Integer.parseInt(mID) );
										}
									}
								}
							}
						}
					}
					else
					{
//						System.out.println("Map.updateMouse()... satellite marker list is NULL!");
//						System.out.println("   but satelliteMarkerManager.getMarkers().size: "+satelliteMarkerManager.getMarkers().size());
					}
				}
			}
		}
	}

	/**
	 * Initialize map for all fields in world
	 * @param world Parent world
	 */
	private void initializeWorldMap(WMV_World world)
	{
		setSelectedField( world, world.getCurrentField().getID() );
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

			if(f.getID() == getSelectedFieldID())
			{
				fieldMarker.setColor(world.mv.color(fieldSelectedHue, fieldSelectedSaturation, fieldSelectedBrightness, 0.f));		
			}
			else
			{
				fieldMarker.setColor(world.mv.color(hue, 255.f, 255.f, 0.f));		
			}
			
			fieldMarker.setHighlightColor(world.mv.color(fieldSelectedHue, fieldSelectedSaturation, fieldSelectedBrightness, 0.f));
			fieldMarkers.add(fieldMarker);
			
			Location fieldCenterPoint = new Location(f.getModel().getState().centerLatitude, f.getModel().getState().centerLongitude);
			fieldMarkerCenters.add(fieldCenterPoint);
			
			count++;
		}

		for(SimplePolygonMarker marker : fieldMarkers)
		{
			satellite.addMarkers(marker);
//			satelliteMarkerManager.addMarker(marker);
		}
		
		world.mv.delay(mapDelay * 2);
	}
	
	/**
	 * Create cluster markers for world
	 * @param world
	 */
	private void createWorldClusterMarkers(WMV_World world)
	{
		allClusterLocations = new ArrayList<Location>();
		allClustersMarker = new MultiMarker();
		
		int fCount = 0;
		for( WMV_Field f : world.getFields() )	
		{
			for(WMV_Cluster c : f.getClusters())
			{
				PVector cLoc = c.getLocation();
				PVector gpsLoc = utilities.getGPSLocationFromCaptureLocation(f, cLoc);
				Location loc = new Location(gpsLoc.y, gpsLoc.x);
				
				float hue = utilities.mapValue(fCount, 0, world.getFields().size(), fieldHueRangeLow, fieldHueRangeHigh);
				
				SimplePointMarker clusterMarker = new SimplePointMarker(loc);
				clusterMarker.setId("Cluster_"+String.valueOf(c.getID()));
				clusterMarker.setDiameter((float)Math.sqrt(c.getMediaWeight()) * 3.f);

				if(selectedField == f.getID())
					clusterMarker.setColor(world.mv.color(hue, clusterSaturation, clusterBrightness, fieldTransparency));			
				else
					clusterMarker.setColor(world.mv.color(hue, fieldSelectedSaturation, fieldSelectedBrightness, fieldTransparency));			

				clusterMarker.setStrokeWeight(0);

				if(mapMedia) allClustersMarker.addMarkers(clusterMarker);
				allClusterLocations.add(loc);
			}
			fCount++;
		}

		if(fCount > 0 && mapMedia)
		{
			allClustersMarker.setId("allClusters");

			world.mv.delay(mapDelay);
			satellite.addMarkers(allClustersMarker);
//			satelliteMarkerManager.addMarker(allClustersMarker);
		}
	}

	
	/**
	 * @return Selected field ID
	 */
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
		createMarkers( world );
	}
	
	/**
	 * @return Selected cluster ID
	 */
	public int getSelectedClusterID()
	{
		return selectedCluster;
	}
	
	/**
	 * Set selected cluster
	 * @param newCluster New selected cluster
	 */
	public void setSelectedClusterID( int newCluster )
	{
//		System.out.println("setSelectedClusterID()... newCluster: "+newCluster);
		selectedCluster = newCluster;
	}

	/**
	 * Draw viewer orientation as arrow on map
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * Draw current viewer location and orientation on map of specified size
	 */
	private void displayViewerOrientation(WMV_World world, WMV_Field f, UnfoldingMap map)
	{
		PVector vLoc = world.viewer.getGPSLocation();
		Location gpsLoc = new Location(vLoc.y, vLoc.x);
		float camYaw = -world.viewer.getXOrientation() - 0.5f * PApplet.PI;

		float shrinkFactor = 0.88f;
		float ptSize = viewerDiameter * shrinkFactor;

		world.mv.stroke(world.mv.color(0, 0, 255, 255));
		for(float i=1; i<viewerArrowPoints; i++)
		{
			ScreenPosition vScreenPos = map.getScreenPosition(gpsLoc);
			float x = i * viewerArrowPointSpacingFactor * (float)Math.cos( camYaw );
			float y = i * viewerArrowPointSpacingFactor * (float)Math.sin( camYaw );
			PVector arrowPoint = new PVector(vScreenPos.x + x, vScreenPos.y + y);

			world.mv.strokeWeight(ptSize);
			world.mv.point(arrowPoint.x, arrowPoint.y);
			
			ptSize *= shrinkFactor;
		}
	}
	
	
	/**
	 * Get map location for a given point			-- In progress
	 * @param world Parent world
	 * @param point Given point
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @return
	 */
	private PVector getMapLocation(WMV_World world, WMV_Field f, PVector point, float mapWidth, float mapHeight)
	{
//		PVector gpsPoint = utilities.getGPSLocation(f, point);
//		ScreenPosition screenPos = satellite.getScreenPosition(new Location(gpsPoint.y, gpsPoint.x));
//		ScreenPosition screenPos = large.getScreenPosition(new Location(gpsPoint.y, gpsPoint.x));
//		return new PVector(screenPos.x, screenPos.y);
		return null;
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
//		float pointSize = smallPointSize * mapWidth;
//		float saturation = maxSaturation;                                              

//		for(WMV_Waypoint w : path)
//		{
//			drawPoint( world, f, w.getLocation(), pointSize * 4.f, mapWidth, mapHeight, 30, saturation, 255.f, mediaTransparency );
//			System.out.println("Path ---> location.x:"+w.getLocation().x+" y:"+w.getLocation().y);
//		}
	}
	

//	/**
//	 * Get mouse 3D location from screen location
//	 * @param mouseX
//	 * @param mouseY
//	 * @return
//	 */
//	private PVector getMapMouseLocation(float mouseX, float mouseY)
//	{
//		float offsetXFactor = 0.115f;
//		float offsetYFactor = 0.115f;
//
//		float x = mouseX;
//		float y = mouseY;
//
//		float centerX = screenWidth * 0.5f;
//		float centerY = screenHeight * 0.5f;
//		
//		float dispX = x - centerX;
//		float dispY = y - centerY;
//		
//		float offsetX = dispX * offsetXFactor;
//		float offsetY = dispY * offsetYFactor;
//
//		if(p.ml.debug.mouse) System.out.println("Map.getMapMouseLocation()... x:"+x+" y:"+y);
//		
//		x += offsetX;
//		y += offsetY;
//		
//		PVector result = new PVector(x, y);			
//		
//		if(p.ml.debug.mouse)
//		{
//			p.ml.stroke(155, 0, 255);
//			p.ml.strokeWeight(5);
//			p.ml.point(result.x, result.y, result.z);		// Show mouse location for debugging
//			System.out.println("Map Mouse Location: x:"+result.x+" y:"+result.y);
//		}
//
//		return result;
//	}
	
	/**
	 * Display OSM map  
	 * @param world Parent world
	 */
	public void displayOSMMap(WMV_World world)				// -- Need to finish
	{
//		PVector vLoc = world.viewer.getGPSLocation();
//		Location gpsLoc = new Location(vLoc.y, vLoc.x);
//		if(gpsLoc != null)
//		{
//			if(viewerMarker != null)
//			{
//				viewerMarker.setLocation(gpsLoc);						// Update location of viewer marker
////				osmMarkerManager.addMarker(viewerMarker);					// -- Needed??
//			}
//			else System.out.println("viewerMarker == null!"+" frameCount:"+world.getState().frameCount);
//		}
//
//		world.ml.perspective();
//		world.ml.camera();												// Reset to default camera setting
//		world.ml.tint(255.f, 255.f);
//		osm.draw();														// Draw the Unfolding Map
	}
	
	/**
	 * Draw large map without satellite overlay 
	 * @param world Parent world
	 */
	public void displayLargeBasicMap(WMV_World world)				// -- Need to finish
	{
//		PVector vLoc = world.viewer.getGPSLocation();
//		Location gpsLoc = new Location(vLoc.y, vLoc.x);
//		if(gpsLoc != null)
//		{
//			if(viewerMarker != null)
//				viewerMarker.setLocation(gpsLoc);						// Update location of viewer marker
//			else 
//				System.out.println("viewerMarker == null!"+" frameCount:"+world.getState().frameCount);
//		}
//
//		world.ml.perspective();
//		world.ml.camera();												// Reset to default camera setting
//		world.ml.tint(255.f, 255.f);
//		large.draw();														// Draw the Unfolding Map
	}

	/**
	 * Draw small map without satellite overlay 
	 * @param world Parent world
	 */
	public void displaySmallBasicMap(WMV_World world)				// -- Need to finish
	{
//		PVector vLoc = world.viewer.getGPSLocation();
//		Location gpsLoc = new Location(vLoc.y, vLoc.x);
//		if(gpsLoc != null)
//		{
//			if(viewerMarker != null)
//				viewerMarker.setLocation(gpsLoc);						// Update location of viewer marker
//			else 
//				System.out.println("viewerMarker == null!"+" frameCount:"+world.getState().frameCount);
//		}
//
//		world.ml.perspective();
//		world.ml.camera();												// Reset to default camera setting
//		world.ml.tint(255.f, 255.f);
//		small.draw();														// Draw the Unfolding Map
	}

	/**
	 * Create simple point markers for each cluster
	 * @param world Parent world
	 */
	private void createBasicMapsClusterMarkers(WMV_World world)
	{
//		smallMarkerManager = new MarkerManager<Marker>();
//		largeMarkerManager = new MarkerManager<Marker>();
//		for( WMV_Cluster c : world.getCurrentField().getClusters() )	
//		{
//			if(!c.isEmpty() && c.getState().mediaCount != 0)
//			{
//				PVector mapLoc = c.getLocation();
//				PVector gpsLoc = utilities.getGPSLocation(world.getCurrentField(), mapLoc);
//				SimplePointMarker marker = new SimplePointMarker(new Location(gpsLoc.y, gpsLoc.x));
//				marker.setId("Cluster_"+String.valueOf(c.getID()));
//				marker.setColor(world.ml.color(100.f, 165.f, 215.f, 225.f));			// Same color as time segments in Time View
//				marker.setHighlightColor(world.ml.color(170, 255, 255, 255.f));
//				marker.setStrokeWeight(0);
//				marker.setDiameter((float)Math.sqrt(c.getState().mediaCount) * 3.f);
//				small.addMarker(marker);
//				large.addMarker(marker);
//				smallMarkerManager.addMarker(marker);
//				largeMarkerManager.addMarker(marker);
//			}
//		}
////		
//		large.addMarkerManager(largeMarkerManager);
//		small.addMarkerManager(smallMarkerManager);
//
//		if(p.getDisplayView() == 1)
//		{
//			largeMarkerManager.enableDrawing();
//			smallMarkerManager.enableDrawing();
//		}
	}
	

	/**
	 * Initialize basic maps	-- Disabled
	 * @param p Parent world
	 */
	private void initializeBasicMaps(WMV_World p)
	{
//		large = new UnfoldingMap(p.ml, "Map", 0, 0, screenWidth, screenHeight, true, false, new BlankMapProvider());
////		large = new UnfoldingMap(p.ml, "Map", 0, 0, screenWidth, screenHeight, true, false, new Microsoft.AerialProvider());
//		small = new UnfoldingMap(p.ml, "Map", 0, 0, zoomMapWidth, zoomMapHeight, true, false, new BlankMapProvider());
//		p.ml.delay(mapDelay);
//		
//		PVector gpsLoc = utilities.getGPSLocation(p.getCurrentField(), new PVector(0,0,0));
//
//		large.setBackgroundColor(0);
//		small.setBackgroundColor(0);
//		p.ml.delay(mapDelay);
//		
//		large.setTweening(true);
//		large.setZoomRange(2, 21);
//		small.setTweening(true);
//		small.setZoomRange(2, 21);
//
//		/* Add mouse interaction */
//		eventDispatcher = new EventDispatcher();
//		MouseHandler mouseHandler = new MouseHandler(p.ml, large);
//		eventDispatcher.addBroadcaster(mouseHandler);
//		eventDispatcher.register(large, "pan", large.getId());
//		eventDispatcher.register(large, "zoom", large.getId());
//
//		createBasicMapsClusterMarkers(p);
//		p.ml.delay(mapDelay);				// -- Scale to number of clusters
//		
//		PVector vLoc = p.viewer.getGPSLocation();
//		plainMapViewerMarker = new SimplePointMarker(new Location(vLoc.y, vLoc.x));
//		plainMapViewerMarker.setId("viewer");
//		plainMapViewerMarker.setDiameter(viewerDiameter);
//		plainMapViewerMarker.setColor(p.ml.color(0, 0, 255, 255));
	}
}
