package main.java.com.entoptic.metaVisualizer.media;

import processing.video.*;

//import java.time.ZonedDateTime;
import java.util.ArrayList;

import main.java.com.entoptic.metaVisualizer.MetaVisualizer;
import main.java.com.entoptic.metaVisualizer.metadata.WMV_VideoMetadata;
import main.java.com.entoptic.metaVisualizer.misc.MV_DebugSettings;
import main.java.com.entoptic.metaVisualizer.misc.WMV_Utilities;
import main.java.com.entoptic.metaVisualizer.model.WMV_Cluster;
import main.java.com.entoptic.metaVisualizer.model.WMV_Time;
import main.java.com.entoptic.metaVisualizer.world.WMV_Field;
import main.java.com.entoptic.metaVisualizer.world.WMV_Viewer;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import processing.data.IntList;

/**************************************************
 * Rectangular video in a 3D environment
 * @author davidgordon
 */

public class WMV_Video extends WMV_Media          		// Represents a video in virtual space
{
	/* General */
	public WMV_VideoState state;
	public WMV_VideoMetadata metadata;

	/* Video */
	public Movie video;									// Movie object
	public PImage frame;									// Video frame to be displayed 
	private PImage blurMask;						// Blur mask
	private PImage blurred;							// Combined pixels

	private float aspectWidthRatioFactor;
	
	/**
	 * Constructor for rectangular video in 3D space
	 * @param newID Video ID
	 * @param newVideo Movie file
	 * @param newType Media type ID
	 * @param newVideoMetadata Video metadata
	 */
	public WMV_Video ( int newID, Movie newVideo, int newType, WMV_VideoMetadata newVideoMetadata, float newAspectWidthRatioFactor )
	{
		super( newID, newType, newVideoMetadata.name, newVideoMetadata.filePath, newVideoMetadata.dateTime, newVideoMetadata.timeZone, 
			   newVideoMetadata.gpsLocation, newVideoMetadata.longitudeRef, newVideoMetadata.latitudeRef );

		metadata = newVideoMetadata;
		state = new WMV_VideoState();
		state.initialize(metadata);
		
		state.vertices = new PVector[4]; 
		state.sVertices = new PVector[4]; 

		state.origVideoWidth = metadata.videoWidth;
		state.origVideoHeight = metadata.videoHeight;

		metadata.focusDistance = state.defaultFocusDistance;
		state.origFocusDistance = metadata.focusDistance;

		initializeTime();

		if(newVideo != null)
			setLengthFromMovie(newVideo);
		
		aspectWidthRatioFactor = newAspectWidthRatioFactor;
	}  
	
	private PImage applyMask(MetaVisualizer ml, PImage source, PImage mask)
	{
		PImage result = ml.createImage(state.origVideoWidth, state.origVideoHeight, PApplet.RGB);
		
		try
		{
			if(source.width == mask.width && source.height == mask.height)
			{
				result = source.copy();
				result.mask(mask); 
			}
			else
			{
				if(getDebugSettings().video || getDebugSettings().ml)
				{
					System.out.println("Video Blur Mask different size from video!");
					System.out.println("  state.origVideoWidth:"+state.origVideoWidth+" source.width:"+source.width+" mask.width:"+mask.width);
					System.out.println("  state.origVideoHeight:"+state.origVideoHeight+" source.height:"+source.height+" mask.height:"+mask.height);
				}
			}
		}
		catch(RuntimeException ex)
		{
			if(getDebugSettings().video || getDebugSettings().ml)
			{
				System.out.println("Error with Video Blur Mask... "+ex+" state.horizBorderID:"+state.horizBordersID+" state.vertBorderID:"+state.vertBordersID);
				if(source != null && mask != null)
				{
					System.out.println("  state.origVideoWidth:"+state.origVideoWidth+" source.width:"+source.width+" mask.width:"+mask.width);
					System.out.println("  state.origVideoHeight:"+state.origVideoHeight+" source.height:"+source.height+" mask.height:"+mask.height);
				}
				else
				{
					System.out.println("  source == null?"+(source == null));
					System.out.println("  mask == null?"+(mask == null));
				}
			}
		}
		
		return result;
	}
	
	public void initializeTime()
	{
		if(metadata.dateTime == null)
		{
			try {
				metadata.dateTime = parseDateTime(metadata.dateTimeString);
				time = new WMV_Time();
				time.initialize( metadata.dateTime, metadata.dateTimeString, getID(), 2, getAssociatedClusterID(), metadata.timeZone );
			} 
			catch (Throwable t) 
			{
				System.out.println("Error in video date / time... " + t);
			}
		}
		else
		{
			time = new WMV_Time();
			time.initialize( metadata.dateTime, metadata.dateTimeString, getID(), 2, getAssociatedClusterID(), metadata.timeZone );
		}
	}

	/**
	 * Draw the video center as a colored sphere
	 * @param size Size to draw the video center
	 */
	public void displayModel(MetaVisualizer ml)
	{
		float modelAlpha = getViewerSettings().userBrightness;

		float farViewingDistance;
		if(ml.world.viewer.getSettings().showInvisibleModels)
		{
			farViewingDistance = ml.world.viewer.getFarViewingDistance() * ml.world.getState().modelDistanceVisibilityFactorFar;
		}
		else
		{
			modelAlpha *= getFadingBrightness();
			farViewingDistance = ml.world.viewer.getFarViewingDistance() * ml.world.getState().modelDistanceVisibilityFactorClose;
		}

		float alphaDistanceFactor = getDistanceBrightness( ml.world.viewer, ml.world.viewer.getFarViewingDistance() +
															   metadata.focusDistance, farViewingDistance ); 
		modelAlpha *= alphaDistanceFactor; 					// Fade brightness based on distance to camera

		float modelBrightness = PApplet.map(modelAlpha, 0.f, 1.f, 0.f, ml.world.getState().modelBrightness);	// Scale to setting for alpha range
		modelAlpha = PApplet.map(modelAlpha, 0.f, 1.f, 0.f, ml.world.getState().modelAlpha);					// Scale to setting for alpha range

//		if( getWorldState().timeFading && time != null && !ml.world.viewer.isMoving() )
//			brightness *= getTimeBrightness(); 							// Fade model brightness based on time -- Disabled

		/* Draw frame */
		ml.pushMatrix();
		ml.stroke(0.f, 0.f, modelBrightness, modelAlpha);	 
		ml.strokeWeight(2.f);
		
		ml.line( state.vertices[0].x, state.vertices[0].y, state.vertices[0].z, state.vertices[1].x, state.vertices[1].y, state.vertices[1].z );
		ml.line( state.vertices[1].x, state.vertices[1].y, state.vertices[1].z, state.vertices[2].x, state.vertices[2].y, state.vertices[2].z );
		ml.line( state.vertices[2].x, state.vertices[2].y, state.vertices[2].z, state.vertices[3].x, state.vertices[3].y, state.vertices[3].z );
		ml.line( state.vertices[3].x, state.vertices[3].y, state.vertices[3].z, state.vertices[0].x, state.vertices[0].y, state.vertices[0].z );
		ml.popMatrix();

		int clusterID = getAssociatedClusterID();
		if(clusterID >= 0 && clusterID < ml.world.getCurrentFieldClusters().size())
		{
			WMV_Cluster cluster = ml.world.getCurrentField().getCluster( clusterID );
			PVector c = cluster.getLocation();
			PVector loc = getLocation();
			PVector cl = getCaptureLocation();

			/* Draw media, cluster and capture location */
			ml.pushMatrix();
			if(getWorldState().showMediaToCluster)
			{
				ml.strokeWeight(3.f);
				ml.stroke(150, 135, 255, getViewingBrightness() * 0.8f);
				ml.line(c.x, c.y, c.z, loc.x, loc.y, loc.z);
			}

			if(getWorldState().showCaptureToMedia)
			{
				ml.strokeWeight(3.f);
				ml.stroke(160, 100, 255, getViewingBrightness() * 0.8f);
				ml.line(cl.x, cl.y, cl.z, loc.x, loc.y, loc.z);
			}

			if(getWorldState().showCaptureToCluster)
			{
				ml.strokeWeight(3.f);
				ml.stroke(120, 55, 255, getViewingBrightness() * 0.8f);
				ml.line(c.x, c.y, c.z, cl.x, cl.y, cl.z);
			}
			ml.popMatrix();
		}
		else
			ml.systemMessage("Video.displayModel()... Cluster requested: "+clusterID+" is out of range:"+ml.world.getCurrentField().getClusters().size()+" ...");
	}

	/**
	 * Fade in sound
	 */
	public void fadeSoundIn()
	{
		if(state.volume < getWorldSettings().videoMaxVolume)
		{
			state.fadingVolume = true;
			state.volumeFadingStartFrame = getWorldState().frameCount; 
			state.volumeFadingStartVal = state.volume; 
			state.volumeFadingEndFrame = getWorldState().frameCount + state.volumeFadingLength;		// Fade volume over 30 frames
			state.volumeFadingTarget = getWorldSettings().videoMaxVolume;
		}
	}
	
	/**
	 * Fade out sound
	 */
	public void fadeSoundOut(boolean pause)
	{
		if(state.volume > 0.f)
		{
			state.fadingVolume = true;
			state.volumeFadingStartFrame = getWorldState().frameCount; 
			state.volumeFadingStartVal = state.volume; 
			state.volumeFadingEndFrame = getWorldState().frameCount + state.volumeFadingLength;		// Fade volume over 30 frames
			state.volumeFadingTarget = 0.f;
			state.pauseAfterSoundFades = pause;
		}
	}
	
	/**
	 * Calculate video visibility
	 * @param viewer Virtual viewer
	 * @param utilities Utilities class
	 */
	public void calculateVisibility(WMV_Viewer viewer, WMV_Utilities utilities)
	{
		setVisible(false);
		if(getViewerSettings() == null)
		{
			if(getDebugSettings().video || getDebugSettings().ml) 
				System.out.println("Video.calculateVisibility()... Fixing getSettings().. error in video #"+getID());
			
			updateWorldState(viewer.p.getSettings(), viewer.p.getState(), viewer.getSettings(), viewer.getState());
		}
		
		if(getViewerSettings().orientationMode)									// With StaticMode ON, determine visibility based on distance of associated cluster 
		{
			if(getAssociatedClusterID() == getViewerState().getCurrentClusterID())		// If this photo's cluster is the current (closest) cluster, it is visible
				setVisible(true);

			for(int id : getViewerState().getClustersVisible())
			{
				if(getAssociatedClusterID() == id)				// If associated cluster is visible, it is visible	
					setVisible(true);
			}
		}
		else 
		{
			if(getViewerSettings().angleFading)
				setVisible( isFacingCamera(getViewerState().getLocation()) );		
			else 
				setVisible(true);     										 		
		}

		if(isVisible())
		{
			float videoAngle = getFacingAngle(getViewerState().getOrientationVector());				

			if(!utilities.isNaN(videoAngle))
				setVisible(getAngleBrightness(videoAngle) > 0.f);	 // Check if video is visible at current angle facing viewer

			if(!isFadingOut() && getViewerSettings().hideVideos)
				setVisible(false);
				
			if(isVisible() && !getViewerSettings().orientationMode)
				setVisible(getDistanceBrightness(viewer, viewer.getFarViewingDistance(), metadata.focusDistance) > 0.f);

			if(metadata.orientation != 0 && metadata.orientation != 90)          	// Hide orientations of 180 or 270 (avoid upside down images)
				setVisible(false);

			if(isBackFacing(getViewerState().getLocation()) || isBehindCamera(getViewerState().getLocation(), getViewerState().getOrientationVector()))
				setVisible(false);
		}
	}
	
	/**
	 * Update volume fading in at beginning and out at end
	 * @param ml Parent app
	 */
	public void updateVolume(MetaVisualizer ml)
	{
		int frameLength = getLengthInFrames(30);							// Get video frame length at 30 fps
		int framesBeforeEnd = getFramesBeforeEnd(ml.frameCount);		// Playback position in frames, i.e. frames from end
//		if(ml.debugSettings.video)
//			System.out.println("Video.updateVolume()... playing?"+isPlaying()+" frameLength:"+frameLength+" framesBeforeEnd:"+framesBeforeEnd);
		
		if(frameLength > 0)
		{
			if( framesBeforeEnd == 0 )	// Fade in at first frame
			{
				if(ml.debug.video)
					System.out.println("  Video.updateVolume()... id #"+getID()+" First frame, will fade sound in...");
				fadeSoundIn();
				state.playbackStartFrame = ml.frameCount;
			}
			else if( framesBeforeEnd == ml.world.viewer.getSettings().soundFadingLength && !isFadingVolume())	
			{
				if(ml.debug.video)
					System.out.println("  Video.updateVolume()... id #"+getID()+" Near end, will fade sound out...");
				fadeSoundOut(true);			// Fade out at <soundFadingLength> before end and pause video once finished
			}
		}
		else
		{
			System.out.println("Video.updateVolume()... ERROR... video #"+getID()+" has no length! frameLength:"+frameLength);
		}
	}

	/**
	 * Update video (and video sound) fading in and out
	 * @param ml Parent app
	 * @param wasVisible Whether video was visible last frame
	 */
	public void calculateFadingVisibility(MetaVisualizer ml, boolean wasVisible)
	{
		boolean visibilitySetToTrue = false;
		boolean visibilitySetToFalse = false;

		if(isFading())									// Update brightness while fading
		{
			if(isFadingOut())
				if(getFadingBrightness() == 0.f)
					setVisible(false);
		}
		else 
		{
			if(!wasVisible && isVisible())
				visibilitySetToTrue = true;

			if(getFadingBrightness() == 0.f && isVisible())
				visibilitySetToTrue = true;

			if(wasVisible && !isVisible())
				visibilitySetToFalse = true;

			if(getFadingBrightness() > 0.f && !isVisible())
				visibilitySetToFalse = true;
		}

//		if(isVisible())
//			System.out.println("Video.updateFading()... 2 "
//					+ "'frame:"+ml.frameCount+" isVisible():"+isVisible()+" visibilitySetToTrue:"+visibilitySetToTrue);
		
		if(getViewerSettings().angleThinning)										// Check Angle Thinning Mode
		{
			if(isVisible() && !state.thinningVisibility && !isFading())
			{
//				System.out.println("1  Will fade out video #"+getID());
				fadeOut(ml.world.getCurrentField(), false);
			}
			
			if(!isVisible() && state.thinningVisibility && !isFading() && !hasFadedOut()) 
			{
				if(!state.loaded) loadMedia(ml); 						// Request video frames from disk
				if(ml.debug.video)
					System.out.println("Video.calculateFadingVisibility()... will fade in video id#"+getID());
				fadeIn(ml.world.getCurrentField());
			}
		}
		else
		{
			if(visibilitySetToTrue && !isFading() && !hasFadedOut())	// If should be visible and already fading, fade in 
			{
				if(!state.loaded) loadMedia(ml);
				if(ml.debug.video)
					System.out.println("Video.calculateFadingVisibility()... will fade in video id#"+getID());
				fadeIn(ml.world.getCurrentField());											// Fade in
			}
		}

		if(visibilitySetToFalse)
		{
//			System.out.println("2  Will fade out video #"+getID());
			fadeOut(ml.world.getCurrentField(), false);
		}

		if(isFadingFocusDistance())
			updateFadingFocusDistance();

		if(hasFadedIn())					// Fade in sound once video has faded in
		{
			if(isPlaying()) fadeSoundIn();
			setFadedIn(false);						
		}

		if(hasFadedOut()) 
		{
//			fadeSoundOut(false);			// Fade sound out and clear video once finished
			setFadedOut(false);						
		}
		
		if(state.soundFadedIn) state.soundFadedIn = false;
		if(state.soundFadedOut) state.soundFadedOut = false;
		
		if(state.fadingVolume && state.loaded)
			updateVolumeFading();
	}
	
	/**
	 * Update video geometry each frame
	 */
	public void calculateVertices()									
	{
		state.vertices = initializeVertices();					// Initialize vertices
		state.sVertices = initializeVertices();					// Initialize vertices

		if (metadata.phi != 0.) state.vertices = rotateVertices(state.vertices, -metadata.phi, state.verticalAxis);        	 // Rotate around X axis
		if (getTheta() != 0.) state.vertices = rotateVertices(state.vertices, 360-getTheta(), state.azimuthAxis);         // Rotate around Z axis
		if (metadata.phi != 0.) state.sVertices = rotateVertices(state.sVertices, -metadata.phi, state.verticalAxis);        	 // Rotate around X axis
		if (getTheta() != 0.) state.sVertices = rotateVertices(state.sVertices, 360-getTheta(), state.azimuthAxis);         // Rotate around Z axis

		if(state.vertices.length == 0) setDisabled(true);
		if(state.sVertices.length == 0) setDisabled(true);
		
		state.vertices = translateVertices(state.vertices, getCaptureLocation());                       // Move image to photo capture location   

		calculateLocation();
		
//		state.disp = getDisplacementVector();
//		setLocation( new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z) );	// Location in Path Mode
//		addToLocation(state.disp);
		state.vertices = translateVertices(state.vertices, state.displacement);          // Translate image vertices from capture to viewing location
		state.sVertices = translateVertices(state.sVertices, state.displacement);      // Translate image static vertices from capture to viewing location

//		setLocation( new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z) );	// Location in Path Mode
//		addToLocation(state.disp);
	}
	
	
	/**
	 * Calculate location given displacement vector
	 */
	public void calculateLocation()
	{
//		System.out.println("Video.calculateLocation()... #" + getID() + " getCaptureLocation().x:"+getCaptureLocation().x+"   y:"+getCaptureLocation().y+"  z:"+getCaptureLocation().z);
//		System.out.println("   getGPSLocation().x:"+getGPSLocation().x+"   y:"+getGPSLocation().y+"  z:"+getGPSLocation().z);
		
		state.displacement = getDisplacementVector();
		setLocation( new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z) );
		addToLocation(state.displacement);
		
//		System.out.println(" After displacement: getCaptureLocation().x:"+getCaptureLocation().x+"   y:"+getCaptureLocation().y+"  z:"+getCaptureLocation().z);
//		System.out.println("   getGPSLocation().x:"+getGPSLocation().x+"   y:"+getGPSLocation().y+"  z:"+getGPSLocation().z);
	}

	public PVector getDisplacementVector()
	{
		float r;				  				 // Viewing sphere radius
		if(metadata.focusDistance == -1.f)
			r = state.defaultFocusDistance;		 // Use default if no focus distance in metadata					      
		else
			r = metadata.focusDistance;							

		float xDisp = r * (float)Math.sin((float)Math.toRadians(360-getTheta())) * (float)Math.sin((float)Math.toRadians(90-metadata.phi)); 
		float zDisp = r * (float)Math.cos((float)Math.toRadians(360-getTheta())) * (float)Math.sin((float)Math.toRadians(90-metadata.phi));  
		float yDisp = r * (float)Math.cos((float)Math.toRadians(90-metadata.phi)); 

		return new PVector(-xDisp, -yDisp, -zDisp);			// Displacement from capture location
	}

	/**
	 * Load video frames from disk
	 */
	public void loadMedia(MetaVisualizer ml)
	{
		if(!isDisabled())																	
		{
			video = new Movie(ml, getMetadata().filePath);
			if(getViewerSettings().autoPlayVideos)
			{
				if(ml.world.getCurrentField().getVideosPlaying() < getViewerSettings().autoPlayMaxVideoCount)
					play(ml);
			}
			else pauseVideo();
			
			video.volume(0.f);
			state.volume = 0.f;

			calculateVertices(); 

			setLength( video.duration() );				// Set video length (in seconds)
			if(ml.debug.video)
				System.out.println("Loading video file..."+getMetadata().filePath+" video.duration():"+video.duration()+" state.length:"+state.length);
			
			state.loaded = true;
		}
	}

	/**
	 * Start playing the video
	 * @param pause 
	 */
	public void play(MetaVisualizer ml)
	{
		video.loop();					// Start loop

		state.playing = true;
		state.playbackStartFrame = ml.frameCount;
		if(ml.debug.video)
			System.out.println("Video.play()... id #"+getID()+" set playbackStartFrame:"+state.playbackStartFrame);
		video.volume(0.f);
		state.volume = 0.f;
		state.startPlayback = true;
		
		fadeSoundIn();
//		state.firstFramePlaying = true;
		
		ml.world.getCurrentField().setVideosPlaying(ml.world.getCurrentField().getVideosPlaying()+1);
	}
	
	/**
	 * Stop playing the video
	 */
	public void stopVideo()
	{
		fadeSoundOut(true);				// Fade sound out and pause video once finished
		state.playing = false;
		state.playbackStartFrame = -1;
	}

	/**
	 * Pause the video
	 */
	private void pauseVideo()
	{
		video.pause();
		state.playing = false;
	}
	
	/**
	 * Stop playing and clear the video
	 */
	public void clearVideo()
	{
		try{
			if(video != null)
			{
				video.stop();
				video.dispose();
			}
		}
		catch(Throwable t)
		{
			System.out.println("Throwable in clearVideo():"+t);
		}
		
		state.loaded = false;
	}

	/**
	 * Draw the video in virtual space
	 * @param ml Parent app
	 */
	public void display(MetaVisualizer ml)
	{
		if(getViewerSettings().selection)
		{
			if (isSelected())     		// Draw outline
			{
				ml.stroke(state.outlineHue, state.outlineSaturation, state.outlineBrightness, state.outlineAlpha);
//				ml.stroke(state.outlineHue, 120, 245, state.outlineAlpha);
				ml.strokeWeight(state.outlineSize);
			}
		}
		else
		{
			if(ml.world.getState().showModel)
			{
				ml.stroke(state.outlineHue, 0, state.outlineBrightness, state.outlineAlpha);
				ml.strokeWeight(1);
			}
			else
				ml.noStroke(); 
		}

		ml.pushMatrix();
		ml.beginShape(PApplet.POLYGON);    // Begin the shape containing the video
		ml.textureMode(PApplet.IMAGE);

		if(getWorldState().useBlurMasks)
		{
			if(blurred != null)
				ml.texture(blurred);
		}
		else
		{
			if(frame != null)
				ml.texture(frame);        	// Apply the image to the face as a texture 
		}
		
		updateFrame(ml);

		if(!getWorldState().alphaMode)
			ml.tint(getViewingBrightness(), 255);          				
		else
			ml.tint(255, PApplet.map(getViewingBrightness(), 0.f, 255.f, 0.f, getWorldState().alpha));          				

		if(getViewerSettings().orientationMode)
		{
			ml.vertex(state.sVertices[0].x, state.sVertices[0].y, state.sVertices[0].z, 0, 0);           		// UPPER LEFT      
			ml.vertex(state.sVertices[1].x, state.sVertices[1].y, state.sVertices[1].z, state.origVideoWidth, 0);           // UPPER RIGHT           
			ml.vertex(state.sVertices[2].x, state.sVertices[2].y, state.sVertices[2].z, state.origVideoWidth, state.origVideoHeight); 		// LOWER RIGHT        
			ml.vertex(state.sVertices[3].x, state.sVertices[3].y, state.sVertices[3].z, 0, state.origVideoHeight);           // LOWER LEFT
		}
		else
		{
			ml.vertex(state.vertices[0].x, state.vertices[0].y, state.vertices[0].z, 0, 0);          			 // UPPER LEFT      
			ml.vertex(state.vertices[1].x, state.vertices[1].y, state.vertices[1].z, state.origVideoWidth, 0);           // UPPER RIGHT           
			ml.vertex(state.vertices[2].x, state.vertices[2].y, state.vertices[2].z, state.origVideoWidth, state.origVideoHeight); 		// LOWER RIGHT        
			ml.vertex(state.vertices[3].x, state.vertices[3].y, state.vertices[3].z, 0, state.origVideoHeight);           // LOWER LEFT
		}

		ml.endShape(PApplet.CLOSE);       // End the shape containing the image
		ml.popMatrix();
	}
	
	public void updateFrame(MetaVisualizer ml)
	{
		setFrame( new PImage( video.getImage() ));
		blurred = applyMask(ml, frame, blurMask);				// Apply blur mask once image has loaded
	}
	
	/** 
	 * Draw the image
	 */
	public void display2D(MetaVisualizer ml)
	{
		ml.noStroke(); 

		ml.pushMatrix();
		ml.beginShape(PApplet.POLYGON);    // Begin the shape containing the image
		ml.textureMode(PApplet.NORMAL);

		ml.noFill();
		if(frame != null)
			ml.texture(frame);        			// Apply the image to the face as a texture 
		frame = new PImage(video.getImage());	// Get next frame
		ml.tint(255, 255);          				

		int vidWidth = getWidth();
		int vidHeight = getHeight();

		ml.translate(-vidWidth / 2.f, -vidHeight / 2.f, 100.f);

		ml.vertex(0, 0, 0, 0, 0);             	// UPPER LEFT      
		ml.vertex(vidWidth, 0, 0, 1, 0);              	// UPPER RIGHT           
		ml.vertex(vidWidth, vidHeight, 0, 1, 1);				// LOWER RIGHT        
		ml.vertex(0, vidHeight, 0, 0, 1);              	// LOWER LEFT

		ml.endShape(PApplet.CLOSE);       // End the shape containing the image
		ml.popMatrix();
	}

	/**
	 * Draw the image metadata in Heads-Up Display
	 */
	public void displayMetadata(MetaVisualizer ml)
	{
		String strTitleVideo = "Video";
		String strTitleVideo2 = "";
		String strName = "Name: "+getName();
		String strID = "ID: "+String.valueOf(getID());
		String strCluster = "Cluster: "+String.valueOf(getAssociatedClusterID());
		String strX = "Location X: "+String.valueOf(getCaptureLocation().z);
		String strY = " Y: "+String.valueOf(getCaptureLocation().x);
		String strZ = " Z: "+String.valueOf(getCaptureLocation().y);

		String strDate = "Date: "+String.valueOf(time.getMonth()) + "-" + String.valueOf(time.getDay()) + "-" + String.valueOf(time.getYear());
		String strTime = "Time: "+String.valueOf(time.getHour()) + ":" + (time.getMinute() >= 10 ? String.valueOf(time.getMinute()) : "0"+String.valueOf(time.getMinute())) + ":" + 
				 (time.getSecond() >= 10 ? String.valueOf(time.getSecond()) : "0"+String.valueOf(time.getSecond()));

		String strLatitude = "GPS Latitude: "+String.valueOf(getGPSLocation().z);
		String strLongitude = " Longitude: "+String.valueOf(getGPSLocation().x);
		String strAltitude = "Altitude: "+String.valueOf(getGPSLocation().y);
		String strTheta = "Direction: "+String.valueOf(getTheta());
		String strElevation = "Vertical Angle: "+String.valueOf(metadata.phi);
		String strRotation = "Rotation: "+String.valueOf(metadata.rotation);

		String strTitleDebug = "--- Debugging ---";
		String strBrightness = "brightness: "+String.valueOf(getViewingBrightness());
		String strBrightnessFading = "brightnessFadingValue: "+String.valueOf(getFadingBrightness());
		
		int frameCount = getWorldState().frameCount;
		ml.display.metadata(frameCount, strTitleVideo);
		ml.display.metadata(frameCount, strTitleVideo2);
		ml.display.metadata(frameCount, "");

		ml.display.metadata(frameCount, strID);
		ml.display.metadata(frameCount, strCluster);
		ml.display.metadata(frameCount, strName);
		ml.display.metadata(frameCount, strX + strY + strZ);
		ml.display.metadata(frameCount, "");

		ml.display.metadata(frameCount, strDate);
		ml.display.metadata(frameCount, strTime);
		ml.display.metadata(frameCount, "");

		ml.display.metadata(frameCount, strLatitude + strLongitude);
		ml.display.metadata(frameCount, strAltitude);
		ml.display.metadata(frameCount, strTheta);
		ml.display.metadata(frameCount, strElevation);
		ml.display.metadata(frameCount, strRotation);

		if(getDebugSettings().video)
		{
			ml.display.metadata(frameCount, strTitleDebug);
			ml.display.metadata(frameCount, strBrightness);
			ml.display.metadata(frameCount, strBrightnessFading);
		}	
	}

	/**
	 * Find distance from camera to point in virtual space where photo appears
	 * @return How far the video is from the camera
	 */
	public float getViewingDistance(WMV_Viewer viewer)                  
	{
		PVector camLoc = viewer.getLocation();
		PVector loc = new PVector(getCaptureLocation().x, getCaptureLocation().y, getCaptureLocation().z);

		float r;

		if(metadata.focusDistance == -1.f)							// Use default if no focus distance in metadata	
			r = state.defaultFocusDistance;							      
		else
			r = metadata.focusDistance;							

		float xDisp = r * (float)Math.sin((float)Math.toRadians(360-getTheta())) * (float)Math.sin((float)Math.toRadians(90-metadata.phi)); 
		float zDisp = r * (float)Math.cos((float)Math.toRadians(360-getTheta())) * (float)Math.sin((float)Math.toRadians(90-metadata.phi));  
		float yDisp = r * (float)Math.cos((float)Math.toRadians(90-metadata.phi)); 

		state.displacement = new PVector(-xDisp, -yDisp, -zDisp);

		loc.add(state.displacement);
		float distance = PVector.dist(loc, camLoc);     

		return distance;
	}

	/** 
	 * Find video brightness due to distance (fades away in distance and as camera gets close)
	 * @param viewer Viewer 
	 * @param farViewingDistance Distance at which image starts fading out
	 * @param vanishingDistance Distance beyond far viewing distance when image fades to invisible
	 * @return Distance visibility multiplier between 0. and 1.
	 */
	public float getDistanceBrightness(WMV_Viewer viewer, float farViewingDistance, float vanishingDistance)									
	{
		float viewDist = getViewingDistance(viewer);
		float distVisibility = 1.f;

//		float farViewingDistance = getViewerSettings().getFarViewingDistance();
		float nearViewingDistance = viewer.getNearViewingDistance();
		
		if(viewDist > farViewingDistance)
		{
			float vanishingPoint = farViewingDistance + vanishingDistance;	// Distance where transparency reaches zero
			if(viewDist < vanishingPoint)
				distVisibility = PApplet.constrain(1.f - PApplet.map(viewDist, farViewingDistance, vanishingPoint, 0.f, 1.f), 0.f, 1.f);    // Fade out until cam.visibleFarDistance
			else
				distVisibility = 0.f;
		}
		else if(viewDist < nearViewingDistance) 													// Near distance at which transparency reaches zero
			distVisibility = PApplet.constrain(PApplet.map(viewDist, viewer.getNearClippingDistance(), nearViewingDistance, 0.f, 1.f), 0.f, 1.f);   					  // Fade out until visibleNearDistance

		return distVisibility;
	}

	/**
	 * Set thinning visibility of video
	 * @param state New visibility
	 */
	public void setThinningVisibility(boolean newState)
	{
		state.thinningVisibility = newState;
	}
	
	/**
	 * Get thinning visibility of image
	 * @param state New visibility
	 */
	public boolean getThinningVisibility()
	{
		return state.thinningVisibility;
	}

	/**
	 * getAngleToCamera()
	 * @return Angle between camera location and image 
	 */	
	public float getAngleToCamera()
	{
		PVector cameraPosition = getViewerState().getLocation();
		PVector centerVertex = calcCenterVertex();

		PVector cameraToFace = new PVector(  cameraPosition.x-centerVertex.x, 	//  Vector from the camera to the face.      
				cameraPosition.y-centerVertex.y, 
				cameraPosition.z-centerVertex.z   );

		PVector ab = new PVector(  state.vertices[1].x-state.vertices[0].x, 
				state.vertices[1].y-state.vertices[0].y, 
				state.vertices[1].z-state.vertices[0].z);
		PVector cb = new PVector(  state.vertices[1].x-state.vertices[2].x, 
				state.vertices[1].y-state.vertices[2].y, 
				state.vertices[1].z-state.vertices[2].z   );

		PVector faceNormal = new PVector();   
		PVector.cross(cb, ab, faceNormal);            						// Cross product of two sides of the face gives face normal (which direction the face is pointing)

		faceNormal.normalize();
		cameraToFace.normalize(); 

		return PVector.dot(faceNormal, cameraToFace);     					// Dot product gives the angle between the two vectors
	}

	/**
	 * @return Whether video is facing the camera
	 */	
	public boolean isFacingCamera(PVector cameraPosition)
	{
		return PApplet.abs(getAngleToCamera()) > getViewerSettings().visibleAngle;     			// If the result is positive, then it is facing the camera.
	}

	/**
	 * @return Is the camera behind the video?  
	 */
	public boolean isBackFacing(PVector cameraPosition)										
	{
		float captureToCam = getCaptureLocation().dist(cameraPosition);  	// Find distance from capture location to camera
		float camToVideo = getLocation().dist(cameraPosition);  		// Find distance from camera to image

//		if(captureToCam > camToVideo + p.p.viewer.getNearClippingDistance())			// If captureToCam > camToVideo, then back of video is facing the camera
		if(captureToCam > camToVideo + getViewerSettings().getNearClippingDistance() * 0.5f)			// If captureToCam > camToVideo, then back of video is facing the camera
			return true;
		else
			return false; 
	}

	/**
	 * @return Angle between the video and direction the camera is facing
	 */
	public float getFacingAngle(PVector camOrientation)
	{
//		PVector camOrientation = p.p.viewer.getOrientationVector();
		PVector faceNormal = getFaceNormal();

		PVector crossVector = new PVector();
		PVector.cross(camOrientation, faceNormal, crossVector);				// Cross vector gives angle between camera and image

		float result = crossVector.mag();
		return result;
	}

	/**
	 * @return Whether video is behind camera
	 */
	public boolean isBehindCamera(PVector camLocation, PVector camOrientation)										
	{
		PVector centerVertex = calcCenterVertex();

		PVector camToVideo = new PVector(  camLocation.x-centerVertex.x, 	//  Vector from the camera to the face.      
				camLocation.y-centerVertex.y, 
				camLocation.z-centerVertex.z   );

		camToVideo.normalize();

		float result = PVector.dot(camOrientation, camToVideo);				// Dot product gives angle between camera and image

		if(result >= 0)							// If > zero, image is behind camera
			return true;
		else									// If < zero, image is in front of camera
			return false; 						
	}

	/**
	 * @return Center vertex of video rectangle
	 */
	public PVector calcCenterVertex()
	{
		PVector vertex1 = new PVector(0,0,0);
		PVector vertex2 = new PVector(0,0,0);
		PVector diff = new PVector(0,0,0);
		PVector result = new PVector(0,0,0);

		vertex1 = state.vertices[2];
		vertex2 = state.vertices[0];

		diff = PVector.sub(vertex1, vertex2);
		diff.mult(0.5f);
		result = PVector.add(vertex2, diff);

		return result;
	}

	/**
	 * @return Normalized vector perpendicular to the image plane
	 */
	public PVector getFaceNormal()
	{
		PVector vertex1, vertex2, vertex3;
		vertex1 = new PVector(0,0,0);
		vertex2 = new PVector(0,0,0);
		vertex3 = new PVector(0,0,0);

		if(getCameraModel() == 1)
		{
			if (metadata.orientation == 90)  // Vertical Image
			{
				vertex1 = state.vertices[3];
				vertex2 = state.vertices[0];
				vertex3 = state.vertices[1];
			}
			else if (metadata.orientation == 0)    // Horizontal Image
			{
				vertex1 = state.vertices[0];
				vertex2 = state.vertices[1];
				vertex3 = state.vertices[2];
			}
			else if (metadata.orientation == 180)    // Upside Down (Horizontal) Image
			{
				vertex1 = state.vertices[2];
				vertex2 = state.vertices[3];
				vertex3 = state.vertices[0];
			}
			else  if (metadata.orientation == 270)    // Upside Down (Vertical) Image
			{
				vertex1 = state.vertices[1];
				vertex2 = state.vertices[2];
				vertex3 = state.vertices[3];
			}
		}
		else
		{
			vertex1 = state.vertices[0];
			vertex2 = state.vertices[1];
			vertex3 = state.vertices[2];
		}

		PVector ab = new PVector( vertex2.x-vertex1.x, 
				vertex2.y-vertex1.y, 
				vertex2.z-vertex1.z  );
		PVector cb = new PVector( vertex2.x-vertex3.x, 
				vertex2.y-vertex3.y, 
				vertex2.z-vertex3.z  );

		PVector faceNormal = new PVector();
		PVector.cross(cb, ab, faceNormal);

		faceNormal.normalize(); 
		return faceNormal;
	}
	
	/**
	 * Find image taken immediately before this video was captured to serve as placeholder, determining elevation and state.rotation angles
	 */
	public void findPlaceholder(ArrayList<WMV_Image> images, MV_DebugSettings debugSettings)
	{
		IntList candidates = new IntList();							// List of placeholder candidates
		
		for (int i = 0; i < images.size(); i++) 					// -- Should limit this to only cluster!!
		{
			if(time.asDate().equals(images.get(i).time.asDate()))				// Placeholder will be from same date
			{
				PVector imgLocation = images.get(i).getCaptureLocation();
				float curDist = PVector.dist(getCaptureLocation(), imgLocation);

				if(curDist < state.assocVideoDistTolerance)		// and very close in space,
					candidates.append(i);												// Add to candidates list
			}
		}
		
		int closestIdx = -1;
		float closestDist = 10000.f;
		
		if(candidates.size() == 0)
		{
			if(debugSettings.video)
				System.out.println("  Video "+getID()+" has no candidates under distance tolerance:"+state.assocVideoDistTolerance+"!");
		}
		
		for( int i : candidates )							// Compare distances of the candidates
		{
			float timeDiff = time.getAbsoluteTime() - images.get(i).time.getAbsoluteTime();

			if( timeDiff > 0.f && timeDiff < state.assocVideoTimeTolerance )			// If in very close succession with an image
			{
				if(timeDiff < closestDist)
				{
					closestDist = timeDiff;
					closestIdx = i;
				}
			}
		}
		
		if(closestIdx != -1)
		{
			if(debugSettings.video && debugSettings.detailed)
				System.out.println("--> Found image placeholder:"+images.get(closestIdx).getName()+"  for video:"+getName()+" placeholder ID:"+images.get(closestIdx).getID()+" closestIdx:"+closestIdx);
			boolean success = associateImagePlaceholder(images.get(closestIdx), closestDist, PApplet.abs(time.getAbsoluteTime() - images.get(closestIdx).time.getAbsoluteTime()));
			
			if(success)
			{
				if(debugSettings.video && debugSettings.detailed)
					System.out.println("---> Set placeholder image id:"+images.get(closestIdx).getID());
				images.get(closestIdx).associateVideo(getID());
				setAssociatedClusterID(images.get(closestIdx).getID());
			}
		}
		else
		{
			setDisabled(true);
		}
		
		if(!state.hasImagePlaceholder)
		{
			if(debugSettings.video)
				System.out.println("No image placeholder found for video:"+getID()+", will set to disabled...");
			setDisabled(true);
			setHidden(true);
		}
	}

	/**
	 * Associate image with given ID with this video
	 * @param imageID 
	 * @param newImageDist 
	 * @param newImageTimeDiff 
	 * @return Whether successful or not 
	 */
	public boolean associateImagePlaceholder(WMV_Image i, float newImageDist, float newImageTimeDiff)
	{
		boolean success = false;
		
		if(!state.hasImagePlaceholder)
			success = true;

		if(success)
		{
			state.hasImagePlaceholder = true;
			state.imagePlaceholder = i.getID();
			
			/* Set video parameters from image placeholder metadata */			// Editing metadata here -- Better way?
			metadata.focusDistance = i.getFocusDistance();		    
			metadata.focalLength = i.getFocalLength();			
			setTheta(i.getDirection());  
			metadata.orientation = i.getOrientation();       
			metadata.phi = i.getElevationAngle();            		
			metadata.rotation = i.getRotationAngle();             

			setHorizBorderID(i.getState().horizBordersID);
			setVertBorderID(i.getState().vertBordersID);
			setBlurMaskID();

			setSensorSize( i.getSensorSize() );
			setAspectRatio( calculateAspectRatio() );
			
			calculateVertices();
		}
		
		return success;
	}

	/**
	 * Update volume fading 
	 */
	private void updateVolumeFading()
	{
		if(state.fadingVolume && getWorldState().frameCount < state.volumeFadingEndFrame)	// Still fading
		{
			state.volume = PApplet.map(getWorldState().frameCount, state.volumeFadingStartFrame, state.volumeFadingEndFrame, state.volumeFadingStartVal, state.volumeFadingTarget);
			video.volume(state.volume);
		}
		else								// Reached target
		{
			state.volume = state.volumeFadingTarget;
			state.fadingVolume = false;
			if(state.volume == 1.f)
				state.soundFadedIn = true;
			else if(state.volume == 0.f)
			{
				state.soundFadedOut = true;
			
				if(state.pauseAfterSoundFades)
				{
					video.pause();
					state.pauseAfterSoundFades = false;
				}
				else
				{
					clearVideo();
				}
			}
		}
	}

	/**
	 * @return Video aspect ratio 
	 */
	float calculateAspectRatio()
	{
		float ratio = 0;

		ratio = (float) state.origVideoHeight / (float) state.origVideoWidth;
//		ratio = (float)(video.height)/(float)(video.width);

		return ratio;
	}

	/**
	 * @return Whether the vertices are null
	 */
	public boolean verticesAreNull()
	{
		if(state.vertices[0] != null && state.vertices[1] != null && state.vertices[2] != null && state.vertices[3] != null)
			return false;
		else
			return true;
	}
	
	 /**
	  * Set blur mask ID for image
	  * 	horizBorderID    0: Left  1: Center  2: Right  3: Left+Right
	  * 	vertBorderID	 0: Top  1: Center  2: Bottom  3: Top+Bottom
	  */
	 public void setBlurMaskID()
	 {
		 if(state.horizBordersID == 0)
		 {
			 switch(state.vertBordersID)
			 {
			 case 0:
//				 blurMask = p.p.blurMaskLeftTop;
				 state.blurMaskID = 0;
				 break;
			 case 1:
//				 blurMask = p.p.blurMaskLeftCenter;
				 state.blurMaskID = 1;
				 break;
			 case 2:
//				 blurMask = p.p.blurMaskLeftBottom;
				 state.blurMaskID = 2;
				 break;
			 case 3:
			 default:
//				 blurMask = p.p.blurMaskLeftBoth;
				 state.blurMaskID = 3;
				 break;
			 }
		 }
		 else if(state.horizBordersID == 1)
		 {
			 switch(state.vertBordersID)
			 {
			 case 0:
//				 blurMask = p.p.blurMaskCenterTop;
				 state.blurMaskID = 4;
				 break;
			 case 1:
//				 blurMask = p.p.blurMaskCenterCenter;
				 state.blurMaskID = 5;
				 break;
			 case 2:
//				 blurMask = p.p.blurMaskCenterBottom;
				 state.blurMaskID = 6;
				 break;
			 case 3:
			 default:
//				 blurMask = p.p.blurMaskCenterBoth;
				 state.blurMaskID = 7;
				 break;
			 }
		 }
		 else if(state.horizBordersID == 2)
		 {
			 switch(state.vertBordersID)
			 {
			 case 0:
//				 blurMask = p.p.blurMaskRightTop;
				 state.blurMaskID = 8;
				 break;
			 case 1:
//				 blurMask = p.p.blurMaskRightCenter;
				 state.blurMaskID = 9;
				 break;
			 case 2:
//				 blurMask = p.p.blurMaskRightBottom;
				 state.blurMaskID = 10;
				 break;
			 case 3:
			 default:
//				 blurMask = p.p.blurMaskRightBoth;
				 state.blurMaskID = 11;
				 break;
			 }
		 }
		 else if(state.horizBordersID == 3)
		 {
			 switch(state.vertBordersID)
			 {
			 case 0:
//				 blurMask = p.p.blurMaskBothTop;
				 state.blurMaskID = 12;
				 break;
			 case 1:
//				 blurMask = p.p.blurMaskBothCenter;
				 state.blurMaskID = 13;
				 break;
			 case 2:
//				 blurMask = p.p.blurMaskBothBottom;
				 state.blurMaskID = 14;
				 break;
			 case 3:
			 default:
//				 blurMask = p.p.blurMaskBothBoth;
				 state.blurMaskID = 15;
				 break;
			 }
		 }
	 }


	/**
	 * @return Average pixel color for this frame
	 */
	PVector getAverageColor() 
	{
		video.loadPixels();
		int r = 0, g = 0, b = 0;
		for (int i=0; i<video.pixels.length; i++) {
			int c = video.pixels[i];
			r += c>>16&0xFF;
		g += c>>8&0xFF;
		b += c&0xFF;
		}
		r /= video.pixels.length;
		g /= video.pixels.length;
		b /= video.pixels.length;
		
		return new PVector(r, g, b);
	}

	/**
	 * Calculate video brightness given viewer to video angle
	 * @param videoAngle Current angle between viewer and video
	 * @return Amount to fade video due to angle
	 */
	public float getAngleBrightness(float videoAngle)
	{
		float angleBrightness = 0.f;

		if(videoAngle > getViewerSettings().visibleAngle)
			angleBrightness = 0.f;
		else if (videoAngle < getViewerSettings().visibleAngle * 0.66f)
			angleBrightness = 1.f;
		else
			angleBrightness = PApplet.constrain((1.f-PApplet.map(videoAngle, getViewerSettings().visibleAngle * 0.66f, getViewerSettings().visibleAngle, 0.f, 1.f)), 0.f, 1.f);

		return angleBrightness;
	}
	
	/**
	 * Fade focus distance to given target while rescaling images 
	 * @param target New focus distance
	 */
	public void fadeFocusDistance(float target, int frameCount)
	{
		setFadingFocusDistance(true);
		state.fadingFocusDistanceStartFrame = frameCount;					
		state.fadingFocusDistanceEndFrame = frameCount + state.fadingFocusDistanceLength;	
		state.fadingFocusDistanceStart = metadata.focusDistance;
		state.fadingFocusDistanceTarget = target;
	}
	
	/**
	 * Fade focus distance to given target while rescaling images 
	 * @param target New focus distance
	 */
	public void startFadingFocusDistance(float target, int frameCount)
	{
		setFadingFocusDistance(true);
		
//		state.fadingFocusDistanceStartFrame = getWorldState().frameCount;					
//		state.fadingFocusDistanceEndFrame = getWorldState().frameCount + 1;		// Only one frame between start and end indicates continuous fading 
		state.fadingFocusDistanceStartFrame = frameCount;					
		state.fadingFocusDistanceEndFrame = frameCount + 1;			// Set flag for continuous fading

		state.fadingFocusDistanceStart = metadata.focusDistance;
		state.fadingFocusDistanceTarget = target;
	}

	/**
	 * Fade focus distance to given target while rescaling images 
	 * @param target New focus distance
	 */
	public void stopFadingFocusDistance()
	{
		setFadingFocusDistance(false);
		setFocusDistance( state.fadingFocusDistanceTarget );	// Set focus distance
		calculateVertices();  								// Update vertices given new focus distance
	}

	/**
	 * Update fading of object distance (focus distance and image size together)
	 */
	public void updateFadingFocusDistance()
	{
		float newFocusDistance = 0.f;

		if (getWorldState().frameCount >= state.fadingFocusDistanceEndFrame)
		{
			if(state.fadingFocusDistanceEndFrame - state.fadingFocusDistanceStartFrame > 1)
				setFadingFocusDistance(false);
			
			newFocusDistance = state.fadingFocusDistanceTarget;
		} 
		else
		{
			newFocusDistance = PApplet.map( getWorldState().frameCount, state.fadingFocusDistanceStartFrame, state.fadingFocusDistanceEndFrame, 
											state.fadingFocusDistanceStart, state.fadingFocusDistanceTarget);      // Fade with distance from current time
		}

		setFocusDistance( newFocusDistance );	// Set focus distance
		calculateVertices();  					// Update vertices given new width
	}

	
	public void resetFocusDistance(int frameCount)
	{
		float newFocusDistance = state.origFocusDistance;
		fadeFocusDistance(newFocusDistance, frameCount);
//		fadeFocusDistance(newFocusDistance, getWorldState().frameCount);
	}
	
	/**	
	 * Setup video rectangle geometry 
	 */
	private PVector[] initializeVertices()
	{
		float width = getVideoWidthInMeters();
		float height = getVideoWidthInMeters() * getAspectRatio();

		float left = -width * 0.5f;
		float right = width * 0.5f;
		float top = -height * 0.5f;
		float bottom = height * 0.5f;
		
		left *= aspectWidthRatioFactor;			/* Testing */
		right *= aspectWidthRatioFactor;

		PVector[] verts = new PVector[4]; 

		verts[0] = new PVector( left, top, 0 );    	  // UPPER LEFT  
		verts[1] = new PVector( right, top, 0 );      // UPPER RIGHT 
		verts[2] = new PVector( right, bottom, 0 );   // LOWER RIGHT
		verts[3] = new PVector( left, bottom, 0 );    // LOWER LEFT
		
		return verts;
	}
	
	/**
	 * Get playback position in frames, i.e. frames from end
	 * @param curFrameCount Current frame count
	 * @return Frames until last frame of video
	 */
	public int getFramesBeforeEnd(int curFrameCount)
	{
		int frameLength = getLengthInFrames( 30 );			// -- Use actual frame rate?
		int endFrame = state.playbackStartFrame + frameLength;
		
		return endFrame - curFrameCount;
//		return frameLength - framesSinceStart;	// Current video position between 0.f and 1.f
	}

	public int getLengthInFrames(float frameRate)
	{
//		System.out.println("Video.getLengthInFrames()... state.length:"+state.length+" video.duration"+video.duration());
		if(state.length != 0)
			return Math.round( state.length * frameRate );			// -- Use actual frame rate?
		else if(video != null)
			return Math.round( video.duration() * frameRate );			// -- Use actual frame rate?
		else 
			return 0;
	}

	/**
	 * Find video width using formula:
	 * Video Width (m.) = Object Width on Sensor (mm.) / Focal Length (mm.) * Focus Distance (m.) 
	 * @return Video width in simulation (m.)
	 */
	private float getVideoWidthInMeters()
	{
		float result = metadata.sensorSize * state.subjectSizeRatio * metadata.focusDistance / metadata.focalLength;
		return result;
	}

	 public void setState(WMV_VideoState newState)
	 {
		 state = newState;
		 setMediaState( state.getMediaState() );
		 metadata = state.getMetadata();
	 }
	 
	public WMV_VideoState getState()
	{
		return state;
	}
	 
	public WMV_VideoMetadata getMetadata()
	{
		return metadata;
	}
	
	public String getFilePath()
	{
		return getMetadata().filePath;
	}

	public void setFilePath(String newFilePath)
	{
		metadata.filePath = newFilePath;
	}

	public void updateFilePath(MetaVisualizer ml, WMV_Field parentField)
	{
		String oldFilePath = getFilePath();
		String[] parts = oldFilePath.split("/");

		parts[parts.length-4] = ml.library.getName(true);			// Library name
		parts[parts.length-3] = parentField.getName();					// Field name
		
		String newFilePath = parts[0];
		for(int i=1; i<parts.length; i++)
			newFilePath = newFilePath + "/" + parts[i];
		System.out.println("Video.updateFilePath()... Will set video path to:"+newFilePath);
		setFilePath(newFilePath);
	}

	 public void setBlurMask(PImage newBlurMask)
	 {
		 blurMask = newBlurMask;
	 }

	/**
	 * @return Save video state for exporting
	 */
	 public void captureState()
	 {
		 state.setMediaState( getMediaState(), metadata );
	 }
	 
	/**
	 * @return Image placeholder for this video
	 */
	public int getImagePlaceholder()
	{
		return state.imagePlaceholder;
	}
	
	 public float getDirection()
	 {
		 return getTheta();
	 }

	 public float getElevationAngle()
	 {
		 return metadata.phi;
	 }
	 
	 public float getRotationAngle()
	 {
		 return metadata.rotation;
	 }
	 
	 public int getWidth()
	 {
		 return metadata.videoWidth;
	 }

	 public int getHeight()
	 {
		 return metadata.videoHeight;
	 }
	 
	 public float getFocusDistance()
	 {
		 return metadata.focusDistance;
	 }

	 public float getFocalLength()
	 {
		 return metadata.focalLength;
	 }
	 
	 public void setSensorSize(float newSensorSize)
	 {
		 metadata.sensorSize = newSensorSize;
	 }

	 public float getSensorSize()
	 {
		 return state.sensorSize;
	 }
	
	 public void setHorizBorderID(int newHorizBorderID)
	 {
		 state.horizBordersID = newHorizBorderID;
	 }

	 public void setVertBorderID(int newVertBorderID)
	 {
		 state.vertBordersID = newVertBorderID;
	 }
	 
	 public void setBlurMaskID(int newBlurMaskID)
	 {
		 state.blurMaskID = newBlurMaskID;
	 }

	 /**
	  * @param newLength New video length
	  */
	 public void setLength(float newLength)
	 {
		 state.length = newLength;
//		 System.out.println("Video.setLength()... newLength:"+newLength+" state.length now:"+state.length);
	 }

	 public void setLengthFromMovie(Movie newVideo)
	 {
		 setLength( newVideo.duration() );				// Set video length (in seconds)
		 newVideo.dispose();
	 }
	 /**
	  * @return Video length
	  */
	 public float getLength()
	 {
		 return state.length;
	 }

	 public void setTheta(float newTheta)
	 {
		 metadata.theta = newTheta;
	 }

	 public float getTheta()
	 {
		 return metadata.theta;
	 }

	 public void setCameraModel(int newCameraModel)
	 {
		 state.cameraModel = newCameraModel;
		 metadata.cameraModel = newCameraModel;
	 }

	 public int getCameraModel()
	 {
		 return state.cameraModel;
	 }
	 
	 public float getBrightness()
	 {
		 return state.brightness;
	 }

	 public boolean isFadingVolume()
	 {
		 return state.fadingVolume;
	 }
	 
	 public boolean isLoaded()
	 {
		 return state.loaded;
	 }
	 
	 public boolean isPlaying()
	 {
		 return state.playing;
	 }
	 
	 public boolean hasOriginal()
	 {
		 return getMediaState().hasOriginal;
	 }

	 public void setFocusDistance(float newFocusDistance)
	 {
		 metadata.focusDistance = newFocusDistance;
	 }

	 public void setFocalLength(float newFocalLength)
	 {
		 metadata.focalLength = newFocalLength;
	 }
	 
	 public void setFrame(PImage newFrame)
	 {
		 frame = newFrame;
	 }
}