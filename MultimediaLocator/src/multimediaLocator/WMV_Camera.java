package multimediaLocator;

import processing.core.*;

/**
 * Camera class
 * @author davidgordon
 * 
 * Initial values:	Camera position     - sits on the positive z-axis
 *              	Target position     - located at the world origin
 *              	Up direction        - point in the negative y
 *              	Field-of-view       - PI/3 radians (60 degrees)
 *              	Aspect ratio        - Applet width to applet height
 *              	Near clipping plane - 0.1x shot length.
 *              	Far clipping plane  - 10x the shot length.
 */ 

public class WMV_Camera
{
	private PApplet p;

	// Camera orientation 
	private float azimuth;
	private float elevation;
	private float roll;

	// Camera position
	private float cameraX;
	private float cameraY;
	private float cameraZ;

	// Target position
	private float targetX;
	private float targetY;
	private float targetZ;

	// Up vector
	private float upX;
	private float upY;
	private float upZ;

	// Distance between camera and target
	private float dX;
	private float dY;
	private float dZ;
	
	// Field of view
	private float fov;
	
	// Aspect ratio
	private float aspectRatio;
	
	// Clip planes
	private float nearClip;
	private float farClip;
	
	// View vector Length
	private float shotLength;

	// Constructors

	// Specify all parameters except the aspect ratio.
	public WMV_Camera(MultimediaLocator newParent, float newCameraX, float newCameraY, float newCameraZ,
			float newTargetX, float newTargetY, float newTargetZ, float newUpX, float newUpY, float newUpZ,
			float newFoV, float newNearClip, float newFarClip)
	{
		this(newParent,
				newCameraX, newCameraY, newCameraZ,
				newTargetX, newTargetY, newTargetZ,
				newUpX,    newUpY,    newUpZ,
				newFoV, (float)(1f * newParent.width / newParent.height), newNearClip, newFarClip);
	}

	// Specify all parameters for camera creation
	public WMV_Camera(MultimediaLocator aParent, float newCameraX, float newCameraY, float newCameraZ,
			float newTargetX, float newTargetY, float newTargetZ, float newUpX, float newUpY, float newUpZ,
			float newFoV, float newAspect, float newNearClip, float newFarClip)
	{
		p   = aParent;
		cameraX  = newCameraX;
		cameraY  = newCameraY;
		cameraZ  = newCameraZ;
		targetX  = newTargetX;
		targetY  = newTargetY;
		targetZ  = newTargetZ;
		upX      = newUpX;
		upY      = newUpY;
		upZ      = newUpZ;
		fov      = newFoV;
		aspectRatio   = newAspect;
		nearClip = newNearClip;
		farClip  = newFarClip;

		dX   = cameraX - targetX;
		dY   = cameraY - targetY;
		dZ   = cameraZ - targetZ;

		shotLength = getMagnitude(dX, dY, dZ);

		azimuth    = (float)Math.atan2(dX,
				dZ);
		elevation  = (float)Math.atan2(dY,
				(float)Math.sqrt(dZ * dZ +
						dX * dX));

		if (elevation > (float)(Math.PI * 0.5) - 0.00001f)
		{
			upY =  0;
			upZ = -1;
		}     

		if (elevation < 0.00001f - (float)(Math.PI * 0.5))
		{
			upY =  0;
			upZ =  1;
		}

		updateUp();
	}

	//--- Behaviors ----------

	/** Send what this camera sees to the view port */
	public void feed() {
		p.perspective(fov, aspectRatio, nearClip, farClip);
		p.camera(cameraX, cameraY, cameraZ,
				targetX, targetY, targetZ,
				upX,     upY,     upZ);
	}

//	/** Send what this camera sees to the view port */
//	public void feedCamera() {
//		p.camera(cameraX, cameraY, cameraZ,
//				targetX, targetY, targetZ,
//				upX,     upY,     upZ);
//	}

	/** Aim the camera at the specified target */
	public void aim(float aTargetX, float aTargetY, float aTargetZ)
	{
		// Move the target
		targetX = aTargetX;
		targetY = aTargetY;
		targetZ = aTargetZ;

		update();
	}

	/** Jump the camera to the specified position */
	public void jump(float positionX, float positionY, float positionZ)
	{
		// Move the camera
		cameraX = positionX;
		cameraY = positionY;
		cameraZ = positionZ;

		update();
	}

	/** Change the field of view between "fish-eye" and "close-up" */
	public void zoom(float anAmount)
	{
		fov = PApplet.constrain(fov + anAmount, 0.00001f, (float) Math.PI - 0.00001f);
	}

	/** Move the camera and target simultaneously along the camera's X axis */
	public void truck(float anAmount)
	{
		// Calculate the camera's X axis in world space
		float directionX = dY * upZ - dZ * upY;
		float directionY = dX * upZ - dZ * upX;
		float directionZ = dX * upY - dY * upX;

		// Normalize this vector so that it can be scaled
		float magnitude = getMagnitude(directionX, directionY, directionZ);

		directionX /= magnitude;
		directionY /= magnitude;
		directionZ /= magnitude;

		// Perform the truck, if any
		cameraX -= anAmount * directionX;
		cameraY -= anAmount * directionY;
		cameraZ -= anAmount * directionZ;
		targetX -= anAmount * directionX;
		targetY -= anAmount * directionY;
		targetZ -= anAmount * directionZ;
	}

	/** Move the camera and target simultaneously along the camera's Y axis */
	public void boom(float anAmount)
	{
		// Perform the boom, if any
		cameraX += anAmount * upX;
		cameraY += anAmount * upY;
		cameraZ += anAmount * upZ;
		targetX += anAmount * upX;
		targetY += anAmount * upY;
		targetZ += anAmount * upZ;
	}

	/** Move the camera and target along the view vector */
	public void dolly(float amount)
	{
		// Normalize the view vector
		float directionX = dX / shotLength;
		float directionY = dY / shotLength;
		float directionZ = dZ / shotLength;

		// Perform the dolly, if any
		cameraX += amount * directionX;
		cameraY += amount * directionY;
		cameraZ += amount * directionZ;
		targetX += amount * directionX;
		targetY += amount * directionY;
		targetZ += amount * directionZ;
	}

	/** Rotate the camera about its X axis */
	public void tilt(float elevationOffset)
	{
		// Calculate the new elevation for the camera
		elevation = PApplet.constrain(elevation - elevationOffset,
				0.00001f-(float)(Math.PI * 0.5), (float)(Math.PI * 0.5)-0.00001f);

		// Update the target
		updateTarget();
	}

	/** Rotate the camera about its Y axis */
	public void pan(float azimuthOffset)
	{
		// Calculate the new azimuth for the camera
		azimuth = (azimuth - azimuthOffset + (float)(2.0 * Math.PI)) % (float)(2.0 * Math.PI);

		// Update the target
		updateTarget();
	}

	/** Rotate the camera about its Z axis */
	public void roll(float rollOffset)
	{
		// Change the roll amount
		roll = (roll + rollOffset + (float)(2.0 * Math.PI)) % (float)(2.0 * Math.PI);

		// Update the up vector
		updateUp();
	}

	/** Arc the camera over (under) a center of interest along a set azimuth*/
	public void arc(float elevationOffset)
	{
		// Calculate the new elevation for the camera
		elevation = PApplet.constrain(elevation + elevationOffset,
				0.00001f-(float)(Math.PI * 0.5), (float)(Math.PI * 0.5)-0.00001f);

		// Update the camera
		updateCamera();
	}

	/** Circle the camera around a center of interest at a set elevation*/
	public void circle(float azimuthOffset)
	{
		// Calculate the new azimuth for the camera
		azimuth = (azimuth + azimuthOffset + (float)(2.0 * Math.PI)) % (float)(2.0 * Math.PI);

		// Update the camera
		updateCamera();
	}

	/** Look about the camera's position */
	public void look(float azimuthOffset, float elevationOffset)
	{
		// Calculate the new azimuth and elevation for the camera
		elevation = PApplet.constrain(elevation - elevationOffset,
				0.00001f-(float)(Math.PI * 0.5), (float)(Math.PI * 0.5)-0.00001f);

		azimuth = (azimuth - azimuthOffset + (float)(2.0 * Math.PI)) % (float)(2.0 * Math.PI);

		// Update the target
		updateTarget();
	}

	/** Tumble the camera about its target */
	public void tumble(float anAzimuthOffset, float anElevationOffset)
	{
		// Calculate the new azimuth and elevation for the camera
		elevation = PApplet.constrain(elevation + anElevationOffset,
				0.00001f-(float)(Math.PI * 0.5), (float)(Math.PI * 0.5)-0.00001f);

		azimuth   = (azimuth + anAzimuthOffset + (float)(2.0 * Math.PI)) % (float)(2.0 * Math.PI);

		// Update the camera
		updateCamera();
	}

	/** Moves the camera and target simultaneously in the camera's X-Y plane */
	public void track(float anXOffset, float aYOffset)
	{
		// Perform the truck, if any
		truck(anXOffset);

		// Perform the boom, if any
		boom(aYOffset);
	}

	//** Returns the camera position */
	public float[] position()
	{
		return new float[] {cameraX, cameraY, cameraZ};
	}

	//** Returns the camera orientation */
	public float[] attitude()
	{
		return new float[] {azimuth, elevation, roll};
	}

	//** Returns the target position */
	public float[] target()
	{
		return new float[] {targetX, targetY, targetZ};
	}

	//** Returns the "up" vector */
	public float[] up()
	{
		return new float[] {upX, upY, upZ};
	}

	//** Returns the field of view */
	public float fov()
	{
		return fov;
	}

	/** Update **/
	private void update()
	{
		// Describe the new vector between the camera and the target
		dX = cameraX - targetX;
		dY = cameraY - targetY;
		dZ = cameraZ - targetZ;

		// Describe the new azimuth and elevation for the camera
		shotLength = (float)Math.sqrt(dX * dX +
				dY * dY +
				dZ * dZ);

		azimuth    = (float)Math.atan2(dX,
				dZ);
		elevation  = (float)Math.atan2(dY,
				(float)Math.sqrt(dZ * dZ +
						dX * dX));

		// update the up vector
		updateUp();
	}

	/** Update target **/
	private void updateTarget()
	{
		// Rotate to the new orientation while maintaining the shot distance.
		targetX = cameraX - ( shotLength               *
				(float)Math.sin((float)(Math.PI * 0.5) + elevation) *
				(float)Math.sin(azimuth));
		targetY = cameraY - (-shotLength               *
				(float)Math.cos((float)(Math.PI * 0.5) + elevation));
		targetZ = cameraZ - ( shotLength               *
				(float)Math.sin((float)(Math.PI * 0.5) + elevation) *
				(float)Math.cos(azimuth));

		// update the up vector
		updateUp();
	}

	/** Update target **/
	private void updateCamera()
	{
		// Orbit to the new orientation while maintaining the shot distance.
		cameraX = targetX + ( shotLength                  *
				(float)Math.sin((float)(Math.PI * 0.5) + elevation) *
				(float)Math.sin(azimuth));
		cameraY = targetY + (-shotLength                  *
				(float)Math.cos((float)(Math.PI * 0.5) + elevation));
		cameraZ = targetZ + ( shotLength                  *
				(float)Math.sin((float)(Math.PI * 0.5) + elevation)    *
				(float)Math.cos(azimuth));

		// update the up vector
		updateUp();
	}

	/** Update the up direction **/
	private void updateUp()
	{
		// Describe the new vector between the camera and the target
		dX = cameraX - targetX;
		dY = cameraY - targetY;
		dZ = cameraZ - targetZ;

		// Calculate the new "up" vector for the camera
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

	/** Find the magnitude of a vector &*/
	private static final float getMagnitude(float x, float y, float z)
	{
		float magnitude = (float)Math.sqrt(x * x + y * y + z * z);
		return (magnitude < 0.00001f) ? 1 : magnitude;
	}
}
