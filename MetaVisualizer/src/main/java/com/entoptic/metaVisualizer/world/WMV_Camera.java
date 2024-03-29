package main.java.com.entoptic.metaVisualizer.world;

import main.java.com.entoptic.metaVisualizer.MetaVisualizer;
import processing.core.*;

/**
 * Camera class
 * @author davidgordon
 * 
 * Camera position: sits on positive z-axis      	Target position: at world origin
 * Up direction: points in negative y direction     Field-of-view: PI/3 radians (60 deg.)
 * Aspect ratio: PApplet width to applet height
 * Near clipping plane: 0.1 x shot length			Far clipping plane: 10 x the shot length.
 */ 
public class WMV_Camera
{
//	private PApplet p;
	private MetaVisualizer p;

	// Camera orientation 
	private float azimuth;			// Azimuth (yaw) in radians
	private float elevation;		// Elevation (pitch) in radians
	private float roll;				// Rotation (roll) in radians

	// Camera position
	private float cameraX;
	private float cameraY;
	private float cameraZ;

	// Target position
	private float targetX;
	private float targetY;
	private float targetZ;

	// Up vector
	private float upX, upY, upZ;

	// Distance between camera and target
	private float dX, dY, dZ;
	
	// Field of view
	private float fov, initFov;
	
	// Aspect ratio
	private float aspectRatio;
	
	// Clipping planes
	private float nearClip, initNearClip;
	private float farClip, initFarClip;
	
	// View vector Length
	private float shotLength;

	// Constructors
//	/**
//	 * Constructor without aspect ratio
//	 * @param newParent Parent app
//	 * @param newCameraX
//	 * @param newCameraY
//	 * @param newCameraZ
//	 * @param newTargetX
//	 * @param newTargetY
//	 * @param newTargetZ
//	 * @param newUpX
//	 * @param newUpY
//	 * @param newUpZ
//	 * @param newFoV
//	 * @param newNearClip
//	 * @param newFarClip
//	 */
//	public WMV_Camera(MultimediaLocator newParent, float newCameraX, float newCameraY, float newCameraZ,
//			float newTargetX, float newTargetY, float newTargetZ, float newUpX, float newUpY, float newUpZ,
//			float newFoV, float newNearClip, float newFarClip)
//	{
//		this(newParent, newCameraX, newCameraY, newCameraZ,
//				newTargetX, newTargetY, newTargetZ,
//				newUpX,    newUpY,    newUpZ,
//				newFoV, (float)(1f * newParent.width / newParent.height), newNearClip, newFarClip);
//	}

	/**
	 * Main constructor with all parameters
	 * @param newParent Parent app
	 * @param newCameraX
	 * @param newCameraY
	 * @param newCameraZ
	 * @param newTargetX
	 * @param newTargetY
	 * @param newTargetZ
	 * @param newUpX
	 * @param newUpY
	 * @param newUpZ
	 * @param newFieldOfView
	 * @param newNearClippingDistance
	 * @param newFarClippingDistance
	 */
	public WMV_Camera(MetaVisualizer newParent, float newCameraX, float newCameraY, float newCameraZ,
			float newTargetX, float newTargetY, float newTargetZ, float newUpX, float newUpY, float newUpZ,
			float newFieldOfView, float newNearClippingDistance, float newFarClippingDistance)
	{
		p = newParent;
		
		cameraX  = newCameraX;
		cameraY  = newCameraY;
		cameraZ  = newCameraZ;
		targetX  = newTargetX;
		targetY  = newTargetY;
		targetZ  = newTargetZ;
		
		upX = newUpX;
		upY = newUpY;
		upZ = newUpZ;
		
		fov = newFieldOfView;
		aspectRatio = (float)(1.f * newParent.width / newParent.height);
		nearClip = newNearClippingDistance;
		farClip = newFarClippingDistance;
		
		initFov = newFieldOfView;
		initNearClip = newNearClippingDistance;
		initFarClip = newFarClippingDistance;

		setup();

//		this(newParent, newCameraX, newCameraY, newCameraZ,
//				newTargetX, newTargetY, newTargetZ,
//				newUpX,    newUpY,    newUpZ,
//				newFoV, (float)(1f * newParent.width / newParent.height), newNearClip, newFarClip);
	}

	/**
	 * Constructor with all parameters
	 * @param newParent Parent app
	 * @param newCameraX
	 * @param newCameraY
	 * @param newCameraZ
	 * @param newTargetX
	 * @param newTargetY
	 * @param newTargetZ
	 * @param newUpX
	 * @param newUpY
	 * @param newUpZ
	 * @param newFoV
	 * @param newAspect
	 * @param newNearClip
	 * @param newFarClip
	 */
//	public void WMV_Camera(MultimediaLocator newParent, float newCameraX, float newCameraY, float newCameraZ,
//			float newTargetX, float newTargetY, float newTargetZ, float newUpX, float newUpY, float newUpZ,
//			float newFoV, float newAspect, float newNearClip, float newFarClip)
//	{
//		p = newParent;
//		cameraX  = newCameraX;
//		cameraY  = newCameraY;
//		cameraZ  = newCameraZ;
//		targetX  = newTargetX;
//		targetY  = newTargetY;
//		targetZ  = newTargetZ;
//		upX = newUpX;
//		upY = newUpY;
//		upZ = newUpZ;
//		fov = newFoV;
//		aspectRatio = newAspect;
//		nearClip = newNearClip;
//		farClip = newFarClip;
//
//		init();
//		
////		dX = cameraX - targetX;
////		dY = cameraY - targetY;
////		dZ = cameraZ - targetZ;
////
////		shotLength = getMagnitude(dX, dY, dZ);
////
////		azimuth = (float)Math.atan2(dX, dZ);
////		elevation  = (float)Math.atan2(dY, (float)Math.sqrt(dZ * dZ + dX * dX));
////
////		if (elevation < 0.00001f - (float)(Math.PI * 0.5))
////		{
////			upY =  0;
////			upZ =  1;
////		}
////
////		if (elevation > (float)(Math.PI * 0.5) - 0.00001f)
////		{
////			upY =  0;
////			upZ = -1;
////		}     
////		
////		updateUp();
//	}
	
	/**
	 * Constructor for camera at default location and target
	 */
	public WMV_Camera(MetaVisualizer newParent, float fieldOfView, float nearClippingDistance, float farClippingDistance)
	{
		p = newParent;
		initialize(fieldOfView, nearClippingDistance, farClippingDistance);
	}
	
	/**
	 * Initialize camera at default location and target
	 * @param fieldOfView
	 * @param nearClippingDistance
	 * @param farClippingDistance
	 */
	public void initialize(float fieldOfView, float nearClippingDistance, float farClippingDistance)
	{
//		REFERENCE:
//		perspective(fovy, aspect, zNear, zFar)
//		camera(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ)
//	
//		DEFAULT:
//		camera(width/2.0, height/2.0, (height/2.0) / tan(PI*30.0 / 180.0), width/2.0, height/2.0, 0, 0, 1, 0).;
//		perspective(PI/3.0, width/height, cameraZ/10.0, cameraZ*10.0);

		cameraX  = 0.f;
		cameraY  = 0.f;
		cameraZ  = 0.f;
		targetX  = 0.f;
		targetY  = 0.f;
		targetZ  = 0.f;
		
		upX = 0.f;
		upY = 1.f;
		upZ = 0.f;

		fov = fieldOfView;
		aspectRatio = (float)(1.f * p.width / p.height);
//		aspectRatio = p.displayWidth/p.displayHeight;		// ERROR HERE?
		nearClip = nearClippingDistance;
		farClip = farClippingDistance;
		
		initFov = fieldOfView;
		initNearClip = nearClippingDistance;
		initFarClip = farClippingDistance;
		
		setup();
	}
	
	/**
	 * Setup camera with current parameters
	 */
	private void setup()
	{
		dX = cameraX - targetX;
		dY = cameraY - targetY;
		dZ = cameraZ - targetZ;

		shotLength = getMagnitude(dX, dY, dZ);

		azimuth = (float)Math.atan2(dX, dZ);
		elevation  = (float)Math.atan2(dY, (float)Math.sqrt(dZ * dZ + dX * dX));

		if (elevation < 0.00001f - (float)(Math.PI * 0.5))
		{
			upY =  0;
			upZ =  1;
		}

		if (elevation > (float)(Math.PI * 0.5) - 0.00001f)
		{
			upY =  0;
			upZ = -1;
		}     
		
		updateUp();
	}
	
	public void resetPerspective()
	{
//		perspective(PI/3.0, width/height, cameraZ/10.0, cameraZ*10.0) where cameraZ is ((height/2.0) / tan(PI*60.0/360.0))
		
//		float cameraZ = ((p.displayHeight/2.0f) / (float)Math.tan((float)Math.PI*60.0f/360.0f));
//		p.perspective((float)Math.PI/3.0f, p.displayWidth/p.displayHeight, cameraZ/10.0f, cameraZ*10.0f); 	// where cameraZ is ((height/2.0) / tan(PI*60.0/360.0))

		fov = (float)Math.PI/3.0f;		// TESTING
//		fov = initFov;
		aspectRatio = (float)(1.f * p.width / p.height);
		nearClip = initNearClip;
		farClip = initFarClip;

		show();
		
//		p.perspective();

//		p.world.viewer.zoomToFieldOfView( p.world.viewer.getSettings().initFieldOfView );
	}

	/** 
	 * Set viewport to this camera's view 
	 */
	public void show() 
	{
//		Default Values: 
//		perspective(PI/3.0, width/height, cameraZ/10.0, cameraZ*10.0) where cameraZ is ((height/2.0) / tan(PI*60.0/360.0))
		
		p.perspective(fov, aspectRatio, nearClip, farClip);
		p.camera(cameraX, cameraY, cameraZ,
				targetX, targetY, targetZ,
				upX,     upY,     upZ);
	}

	/** 
	 * Aim camera at given target 
	 * @param aTargetX
	 * @param aTargetY
	 * @param aTargetZ
	 */
	public void aim(float aTargetX, float aTargetY, float aTargetZ)
	{
		targetX = aTargetX;
		targetY = aTargetY;
		targetZ = aTargetZ;
		updateMovement();
	}

	/** 
	 * Teleport camera to the specified position 
	 */
	public void teleport(float positionX, float positionY, float positionZ, boolean updateTarget)
	{
		if(updateTarget)
		{
			PVector lastPosition = new PVector(cameraX, cameraY, cameraZ);
			cameraX = positionX;
			cameraY = positionY;
			cameraZ = positionZ;

			PVector disp = new PVector(cameraX - lastPosition.x, cameraY - lastPosition.y, cameraZ - lastPosition.z);

			targetX += disp.x;
			targetY += disp.y;
			targetZ += disp.z;
		}
		else
		{
			cameraX = positionX;
			cameraY = positionY;
			cameraZ = positionZ;
		}
		updateMovement();
	}

	/** 
	 * Change the field of view between "fish-eye" and "close-up" 
	 * */
	public void zoom(float zoomAmount)
	{
		fov = PApplet.constrain(fov + zoomAmount, 0.00001f, (float) Math.PI - 0.00001f);
	}

	/** 
	 * Move camera and target simultaneously along camera's X axis 
	 * */
	public void truck(float truckAmount)
	{
		// Calculate camera's X axis in world space
		float directionX = dY * upZ - dZ * upY;
		float directionY = dX * upZ - dZ * upX;
		float directionZ = dX * upY - dY * upX;

		// Normalize vector to be scaled
		float magnitude = getMagnitude(directionX, directionY, directionZ);
		directionX /= magnitude;
		directionY /= magnitude;
		directionZ /= magnitude;

		// Perform truck
		cameraX -= truckAmount * directionX;
		cameraY -= truckAmount * directionY;
		cameraZ -= truckAmount * directionZ;
		targetX -= truckAmount * directionX;
		targetY -= truckAmount * directionY;
		targetZ -= truckAmount * directionZ;
	}

	/** 
	 * Move camera and target simultaneously along camera's Y axis 
	 */
	public void boom(float boomAmount)
	{
		// Perform boom
		cameraX += boomAmount * upX;
		cameraY += boomAmount * upY;
		cameraZ += boomAmount * upZ;
		targetX += boomAmount * upX;
		targetY += boomAmount * upY;
		targetZ += boomAmount * upZ;
	}

	/** 
	 * Move camera and target along view vector 
	 */
	public void dolly(float dollyAmount)
	{
		// Normalize view vector
		float directionX = dX / shotLength;
		float directionY = dY / shotLength;
		float directionZ = dZ / shotLength;

		// Perform dolly
		cameraX += dollyAmount * directionX;
		cameraY += dollyAmount * directionY;
		cameraZ += dollyAmount * directionZ;
		targetX += dollyAmount * directionX;
		targetY += dollyAmount * directionY;
		targetZ += dollyAmount * directionZ;
	}

	/** 
	 * Rotate camera about its X axis 
	 */
	public void tilt(float elevationOffset)
	{
		// Calculate the new elevation for camera
		elevation = PApplet.constrain(elevation - elevationOffset, 0.00001f-(float)(Math.PI * 0.5), 
				    (float)(Math.PI * 0.5)-0.00001f);

		updateTarget();		// Update target
	}

	/** 
	 * Rotate camera about its Y axis 
	 */
	public void pan(float azimuthOffset)
	{
		azimuth = (azimuth - azimuthOffset + (float)(2.0 * Math.PI)) % (float)(2.0 * Math.PI);		// Calculate the new azimuth for camera
		updateTarget();		// Update target
	}

	/** 
	 * Rotate camera about its Z axis 
	 */
	public void roll(float rollOffset)
	{
		roll = (roll + rollOffset + (float)(2.0 * Math.PI)) % (float)(2.0 * Math.PI);		// Change the roll amount
		updateUp();		// Update up vector
	}

	/** 
	 * Arc camera over a center of interest along a set azimuth
	 */
	public void arc(float elevationOffset)
	{
		elevation = PApplet.constrain(elevation + elevationOffset, 0.00001f-(float)(Math.PI * 0.5), 
				(float)(Math.PI * 0.5)-0.00001f);		// Calculate the new elevation for camera

		updateTurning();		// Update camera
	}

	/** 
	 * Circle camera around a center of interest at a set elevation
	 */
	public void circle(float azimuthOffset)
	{
		azimuth = (azimuth + azimuthOffset + (float)(2.0 * Math.PI)) % (float)(2.0 * Math.PI);		// Calculate the new azimuth for camera
		updateTurning();		// Update camera
	}

	/** 
	 * Look about camera's position 
	 * @param azimuthOffset
	 * @param elevationOffset
	 */
	public void look(float azimuthOffset, float elevationOffset)
	{
		// Calculate the new azimuth and elevation for camera
		elevation = PApplet.constrain(elevation - elevationOffset,
				0.00001f-(float)(Math.PI * 0.5), (float)(Math.PI * 0.5)-0.00001f);

		azimuth = (azimuth - azimuthOffset + (float)(2.0 * Math.PI)) % (float)(2.0 * Math.PI);

		updateTarget();		// Update target
	}

	/** 
	 * Tumble camera about target 
	 * @param anAzimuthOffset
	 * @param anElevationOffset
	 */
	public void tumble(float anAzimuthOffset, float anElevationOffset)
	{
		elevation = PApplet.constrain( elevation + anElevationOffset, 0.00001f-(float)(Math.PI * 0.5), 
					(float)(Math.PI * 0.5) - 0.00001f );		// Calculate new azimuth / elevation for camera
		azimuth   = (azimuth + anAzimuthOffset + (float)(2.0 * Math.PI)) % (float)(2.0 * Math.PI);
		updateTurning();		
	}

	/** 
	 * Move camera and target simultaneously in camera's X-Y plane 
	 * @param xOffset X offset
	 * @param yOffset Y Offset
	 */
	public void track(float xOffset, float yOffset)
	{
		truck(xOffset);		// Perform truck, if exists
		boom(yOffset);		// Perform boom, if exists
	}

	/**
	 * Get position
	 * @return Position [x, y, z]
	 */
	public float[] getPosition()
	{
		return new float[] {cameraX, cameraY, cameraZ};
	}

	/**
	 * Get attitude
	 * @return Attitude [x, y, z]
	 */
	public float[] getAttitude()
	{
		return new float[] {azimuth, elevation, roll};
	}

	/**
	 * Get target
	 * @return Target [x, y, z]
	 */
	public float[] getTarget()
	{
		return new float[] {targetX, targetY, targetZ};
	}

	/**
	 * Get up vector
	 * @return Up [x, y, z]
	 */
	public float[] getUp()
	{
		return new float[] {upX, upY, upZ};
	}

	/**
	 * Get field of view
	 * @return Field of view in radians
	 */
	public float getFov()
	{
		return fov;
	}

	/**
	 * Update camera movement
	 */
	private void updateMovement()
	{
		// Find new vector between camera and target
		dX = cameraX - targetX;
		dY = cameraY - targetY;
		dZ = cameraZ - targetZ;

		// Find new azimuth and elevation for camera
		shotLength = (float)Math.sqrt(dX * dX +	dY * dY + dZ * dZ);
		azimuth = (float)Math.atan2(dX, dZ);
		elevation  = (float)Math.atan2(dY, (float)Math.sqrt(dZ * dZ + dX * dX));

		updateUp();		// Update up vector
	}

	/** 
	 * Update camera turning
	 */
	private void updateTurning()
	{
		// Orbit to the new orientation while maintaining the shot distance.
		cameraX = targetX + ( shotLength * (float)Math.sin((float)(Math.PI * 0.5) + elevation) * (float)Math.sin(azimuth));
		cameraY = targetY + (-shotLength * (float)Math.cos((float)(Math.PI * 0.5) + elevation));
		cameraZ = targetZ + ( shotLength * (float)Math.sin((float)(Math.PI * 0.5) + elevation) * (float)Math.cos(azimuth));

		updateUp();		// Update up vector
	}

	/** 
	 * Update target 
	 */
	private void updateTarget()
	{
		// Rotate to the new orientation while maintaining the shot distance.
		targetX = cameraX - ( shotLength * (float)Math.sin((float)(Math.PI * 0.5) + elevation) *
				(float)Math.sin(azimuth));
		targetY = cameraY - (-shotLength * (float)Math.cos((float)(Math.PI * 0.5) + elevation));
		targetZ = cameraZ - ( shotLength * (float)Math.sin((float)(Math.PI * 0.5) + elevation) *
				(float)Math.cos(azimuth));

		updateUp();		// Update up vector
	}

	/** 
	 * Update up vector
	 */
	private void updateUp()
	{
		// Describe the new vector between camera and target
		dX = cameraX - targetX;
		dY = cameraY - targetY;
		dZ = cameraZ - targetZ;

		// Calculate the new "up" vector for camera
		upX = -dX * dY;
		upY =  dZ * dZ + dX * dX;
		upZ = -dZ * dY;

		// Normalize the "up" vector
		float magnitude = getMagnitude(upX, upY, upZ);

		upX /= magnitude;
		upY /= magnitude;
		upZ /= magnitude;

		// Calculate roll
		if (roll != 0)
		{
			// Calculate camera's X axis in world space
			float directionX = dY * upZ - dZ * upY;
			float directionY = dX * upZ - dZ * upX;
			float directionZ = dX * upY - dY * upX;

			// Normalize vector to be scaled
			magnitude = getMagnitude(directionX, directionY, directionZ);

			directionX /= magnitude;
			directionY /= magnitude;
			directionZ /= magnitude;

			// Perform roll
			upX = upX * (float)Math.cos(roll) + directionX * (float)Math.sin(roll);
			upY = upY * (float)Math.cos(roll) + directionY * (float)Math.sin(roll);
			upZ = upZ * (float)Math.cos(roll) + directionZ * (float)Math.sin(roll);
		}
	}

	/**
	 * Calculate magnitude of a vector
	 * @param x X value 
	 * @param y Y value
	 * @param z Z value
	 * @return Vector magnitude
	 */
	private static final float getMagnitude(float x, float y, float z)
	{
		float magnitude = (float)Math.sqrt(x * x + y * y + z * z);
		return (magnitude < 0.00001f) ? 1 : magnitude;
	}
	
	/**
	 * Set camera field of view
	 * @param newFieldOfView New field of view
	 */
	public void setFieldOfView(float newFieldOfView)
	{
		fov = newFieldOfView;
		show();
	}
}
