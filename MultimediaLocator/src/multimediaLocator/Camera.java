package multimediaLocator;

import java.awt.event.*;
import processing.core.*;

/**
 * Camera class, adapted from OCD library v1.4 by Kristian Linn Damkjer
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

public class Camera
{
	//--- Class Attributes ----
	private static final float TWO_PI      = (float)(2.0 * Math.PI);
	private static final float PI          = (float) Math.PI;
	private static final float HALF_PI     = (float)(Math.PI * 0.5);
	private static final float TOL         = 0.00001f;
	private static final float DEFAULT_FOV = (float) (PI / 3.0);

	//--- Attributes ----------
	private PApplet p;

	// Camera Orientation Information
	private float azimuth;
	private float elevation;
	private float roll;

	// Camera Position
	private float cameraX;
	private float cameraY;
	private float cameraZ;

	// Target Position
	private float targetX;
	private float targetY;
	private float targetZ;

	// Up Vector
	private float upX;
	private float upY;
	private float upZ;

	// Field of View
	private float fov;

	// Aspect Ratio
	private float theAspect;

	// Clip Planes
	private float theNearClip;
	private float theFarClip;

	// The length of the view vector
	private float theShotLength;

	// Distance differences between camera and target
	private float deltaX;
	private float deltaY;
	private float deltaZ;

	//--- Constructors --------

	// Create a camera that sits on the z axis
	public Camera(PApplet aParent)
	{
		this(aParent,
				aParent.height * 0.5f / tan(DEFAULT_FOV * 0.5f));
	}

	// Create a camera that sits on the z axis with a specified shot length
	public Camera(PApplet aParent,
			float   aShotLength)
	{
		this(aParent,
				0, 0, aShotLength);
	}

	// Create a camera at the specified location looking at the world origin
	public Camera(PApplet aParent,
			float   aCameraX, float aCameraY, float aCameraZ)
	{
		this(aParent,
				aCameraX, aCameraY, aCameraZ,
				0,        0,        0);
	}

	// Create a camera at the specified location with the specified target
	public Camera(PApplet aParent,
			float aCameraX, float aCameraY, float aCameraZ,
			float aTargetX, float aTargetY, float aTargetZ)
	{
		this(aParent,
				aCameraX, aCameraY, aCameraZ,
				aTargetX, aTargetY, aTargetZ,
				0,        1,        0);
	}

	// Create a camera at the specified location with the specified target and
	// up direction
	public Camera(PApplet aParent,
			float aCameraX, float aCameraY, float aCameraZ,
			float aTargetX, float aTargetY, float aTargetZ,
			float anUpX,    float anUpY,    float anUpZ)
	{
		this(aParent,
				aCameraX, aCameraY, aCameraZ,
				aTargetX, aTargetY, aTargetZ,
				anUpX,    anUpY,    anUpZ,
				DEFAULT_FOV, (float)(1f * aParent.width / aParent.height), 0, 0);

		theNearClip = theShotLength * 0.1f;
		theFarClip  = theShotLength * 10f;
	}

	// Create a camera with the specified frustum
	public Camera(PApplet aParent,
			float anFoV, float anAspect, float aNearClip, float aFarClip)
	{
		this(aParent,
				0, 0, aParent.height * 0.5f / tan(anFoV * 0.5f),
				anFoV, anAspect, aNearClip, aFarClip);
	}

	// Create a camera at the specified location with the specified frustum
	public Camera(PApplet aParent,
			float aCameraX, float aCameraY, float aCameraZ,
			float anFoV, float anAspect, float aNearClip, float aFarClip)
	{
		this(aParent,
				aCameraX, aCameraY, aCameraZ,
				0,        0,        0,
				anFoV, anAspect, aNearClip, aFarClip);
	}

	// Create a camera at the specified location with the specified target and
	// frustum
	public Camera(PApplet aParent,
			float aCameraX, float aCameraY, float aCameraZ,
			float aTargetX, float aTargetY, float aTargetZ,
			float anFoV, float anAspect, float aNearClip, float aFarClip)
	{
		this(aParent,
				aCameraX, aCameraY, aCameraZ,
				aTargetX, aTargetY, aTargetZ,
				0,        1,        0,
				anFoV, anAspect, aNearClip, aFarClip);
	}

	// Create a camera with a near and far clip plane
	public Camera (PApplet aParent,
			float aNearClip, float aFarClip)
	{
		this(aParent,
				0, 0, aParent.height * 0.5f / tan(DEFAULT_FOV * 0.5f),
				aNearClip, aFarClip);
	}

	// Create a camera at the specified location with a near and far clip plane
	public Camera (PApplet aParent,
			float aCameraX, float aCameraY, float aCameraZ,
			float aNearClip, float aFarClip)
	{
		this(aParent,
				aCameraX, aCameraY, aCameraZ,
				0,        0,        0,
				aNearClip, aFarClip);
	}

	// Create a camera at the specified location with the specified target
	// and a near and far clip plane
	public Camera (PApplet aParent,
			float aCameraX, float aCameraY, float aCameraZ,
			float aTargetX, float aTargetY, float aTargetZ,
			float aNearClip, float aFarClip)
	{
		this(aParent,
				aCameraX, aCameraY, aCameraZ,
				aTargetX, aTargetY, aTargetZ,
				0,        1,        0,
				aNearClip, aFarClip);
	}

	// Create a camera at the specified location with the specified target
	// , up direction, near and far clip plane
	public Camera(PApplet aParent,
			float aCameraX, float aCameraY, float aCameraZ,
			float aTargetX, float aTargetY, float aTargetZ,
			float anUpX,    float anUpY,    float anUpZ,
			float aNearClip, float aFarClip)
	{
		this(aParent,
				aCameraX, aCameraY, aCameraZ,
				aTargetX, aTargetY, aTargetZ,
				anUpX,    anUpY,    anUpZ,
				DEFAULT_FOV, aNearClip, aFarClip);
	}

	// Specify all parameters except the aspect ratio.
	public Camera(PApplet aParent,
			float aCameraX, float aCameraY, float aCameraZ,
			float aTargetX, float aTargetY, float aTargetZ,
			float anUpX,    float anUpY,    float anUpZ,
			float anFoV, float aNearClip, float aFarClip)
	{
		this(aParent,
				aCameraX, aCameraY, aCameraZ,
				aTargetX, aTargetY, aTargetZ,
				anUpX,    anUpY,    anUpZ,
				anFoV, (float)(1f * aParent.width / aParent.height), aNearClip, aFarClip);
	}

	// Specify all parameters for camera creation
	public Camera(PApplet aParent,
			float aCameraX, float aCameraY, float aCameraZ,
			float aTargetX, float aTargetY, float aTargetZ,
			float anUpX,    float anUpY,    float anUpZ,
			float anFoV, float anAspect, float aNearClip, float aFarClip)
	{
		p   = aParent;
		cameraX  = aCameraX;
		cameraY  = aCameraY;
		cameraZ  = aCameraZ;
		targetX  = aTargetX;
		targetY  = aTargetY;
		targetZ  = aTargetZ;
		upX      = anUpX;
		upY      = anUpY;
		upZ      = anUpZ;
		fov      = anFoV;
		theAspect   = anAspect;
		theNearClip = aNearClip;
		theFarClip  = aFarClip;

		deltaX   = cameraX - targetX;
		deltaY   = cameraY - targetY;
		deltaZ   = cameraZ - targetZ;

		theShotLength = magnitude(deltaX, deltaY, deltaZ);

		azimuth    = atan2(deltaX,
				deltaZ);
		elevation  = atan2(deltaY,
				sqrt(deltaZ * deltaZ +
						deltaX * deltaX));

		if (elevation > HALF_PI - TOL)
		{
			upY =  0;
			upZ = -1;
		}     

		if (elevation < TOL - HALF_PI)
		{
			upY =  0;
			upZ =  1;
		}

		updateUp();
	}

	//--- Behaviors ----------

	/** Send what this camera sees to the view port */
	public void feed() {
		p.perspective(fov, theAspect, theNearClip, theFarClip);
		p.camera(cameraX, cameraY, cameraZ,
				targetX, targetY, targetZ,
				upX,     upY,     upZ);
	}

	/** Send what this camera sees to the view port */
	public void feedCamera() {
		p.camera(cameraX, cameraY, cameraZ,
				targetX, targetY, targetZ,
				upX,     upY,     upZ);
	}

	/** Aim the camera at the specified target */
	public void aim(float aTargetX, float aTargetY, float aTargetZ)
	{
		// Move the target
		targetX = aTargetX;
		targetY = aTargetY;
		targetZ = aTargetZ;

		updateDeltas();
	}

	/** Jump the camera to the specified position */
	public void jump(float positionX, float positionY, float positionZ)
	{
		// Move the camera
		cameraX = positionX;
		cameraY = positionY;
		cameraZ = positionZ;

		updateDeltas();
	}

	/** Change the field of view between "fish-eye" and "close-up" */
	public void zoom(float anAmount)
	{
		fov = constrain(fov + anAmount, TOL, PI - TOL);
	}

	/** Move the camera and target simultaneously along the camera's X axis */
	public void truck(float anAmount)
	{
		// Calculate the camera's X axis in world space
		float directionX = deltaY * upZ - deltaZ * upY;
		float directionY = deltaX * upZ - deltaZ * upX;
		float directionZ = deltaX * upY - deltaY * upX;

		// Normalize this vector so that it can be scaled
		float magnitude = magnitude(directionX, directionY, directionZ);

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
		float directionX = deltaX / theShotLength;
		float directionY = deltaY / theShotLength;
		float directionZ = deltaZ / theShotLength;

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
		elevation = constrain(elevation - elevationOffset,
				TOL-HALF_PI, HALF_PI-TOL);

		// Update the target
		updateTarget();
	}

	/** Rotate the camera about its Y axis */
	public void pan(float azimuthOffset)
	{
		// Calculate the new azimuth for the camera
		azimuth = (azimuth - azimuthOffset + TWO_PI) % TWO_PI;

		// Update the target
		updateTarget();
	}

	/** Rotate the camera about its Z axis */
	public void roll(float rollOffset)
	{
		// Change the roll amount
		roll = (roll + rollOffset + TWO_PI) % TWO_PI;

		// Update the up vector
		updateUp();
	}

	/** Arc the camera over (under) a center of interest along a set azimuth*/
	public void arc(float elevationOffset)
	{
		// Calculate the new elevation for the camera
		elevation = constrain(elevation + elevationOffset,
				TOL-HALF_PI, HALF_PI-TOL);

		// Update the camera
		updateCamera();
	}

	/** Circle the camera around a center of interest at a set elevation*/
	public void circle(float azimuthOffset)
	{
		// Calculate the new azimuth for the camera
		azimuth = (azimuth + azimuthOffset + TWO_PI) % TWO_PI;

		// Update the camera
		updateCamera();
	}

	/** Look about the camera's position */
	public void look(float azimuthOffset, float elevationOffset)
	{
		// Calculate the new azimuth and elevation for the camera
		elevation = constrain(elevation - elevationOffset,
				TOL-HALF_PI, HALF_PI-TOL);

		azimuth = (azimuth - azimuthOffset + TWO_PI) % TWO_PI;

		// Update the target
		updateTarget();
	}

	/** Tumble the camera about its target */
	public void tumble(float anAzimuthOffset, float anElevationOffset)
	{
		// Calculate the new azimuth and elevation for the camera
		elevation = constrain(elevation + anElevationOffset,
				TOL-HALF_PI, HALF_PI-TOL);

		azimuth   = (azimuth + anAzimuthOffset + TWO_PI) % TWO_PI;

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

	//---- Helpers ------------------------------------------------------------

	/** Update deltas and related information */
	private void updateDeltas()
	{
		// Describe the new vector between the camera and the target
		deltaX = cameraX - targetX;
		deltaY = cameraY - targetY;
		deltaZ = cameraZ - targetZ;

		// Describe the new azimuth and elevation for the camera
		theShotLength = sqrt(deltaX * deltaX +
				deltaY * deltaY +
				deltaZ * deltaZ);

		azimuth    = atan2(deltaX,
				deltaZ);
		elevation  = atan2(deltaY,
				sqrt(deltaZ * deltaZ +
						deltaX * deltaX));

		// update the up vector
		updateUp();
	}

	/** Update target and related information */
	private void updateTarget()
	{
		// Rotate to the new orientation while maintaining the shot distance.
		targetX = cameraX - ( theShotLength               *
				sin(HALF_PI + elevation) *
				sin(azimuth));
		targetY = cameraY - (-theShotLength               *
				cos(HALF_PI + elevation));
		targetZ = cameraZ - ( theShotLength               *
				sin(HALF_PI + elevation) *
				cos(azimuth));

		// update the up vector
		updateUp();
	}

	/** Update target and related information */
	private void updateCamera()
	{
		// Orbit to the new orientation while maintaining the shot distance.
		cameraX = targetX + ( theShotLength                  *
				sin(HALF_PI + elevation) *
				sin(azimuth));
		cameraY = targetY + (-theShotLength                  *
				cos(HALF_PI + elevation));
		cameraZ = targetZ + ( theShotLength                  *
				sin(HALF_PI + elevation)    *
				cos(azimuth));

		// update the up vector
		updateUp();
	}

	/** Update the up direction and related information */
	private void updateUp()
	{
		// Describe the new vector between the camera and the target
		deltaX = cameraX - targetX;
		deltaY = cameraY - targetY;
		deltaZ = cameraZ - targetZ;

		// Calculate the new "up" vector for the camera
		upX = -deltaX * deltaY;
		upY =  deltaZ * deltaZ + deltaX * deltaX;
		upZ = -deltaZ * deltaY;

		// Normalize the "up" vector
		float magnitude = magnitude(upX, upY, upZ);

		upX /= magnitude;
		upY /= magnitude;
		upZ /= magnitude;

		// Calculate the roll if there is one
		if (roll != 0)
		{
			// Calculate the camera's X axis in world space
			float directionX = deltaY * upZ - deltaZ * upY;
			float directionY = deltaX * upZ - deltaZ * upX;
			float directionZ = deltaX * upY - deltaY * upX;

			// Normalize this vector so that it can be scaled
			magnitude = magnitude(directionX, directionY, directionZ);

			directionX /= magnitude;
			directionY /= magnitude;
			directionZ /= magnitude;

			// Perform the roll
			upX = upX * cos(roll) + directionX * sin(roll);
			upY = upY * cos(roll) + directionY * sin(roll);
			upZ = upZ * cos(roll) + directionZ * sin(roll);
		}
	}

	/** Find the magnitude of a vector */
	private static final float magnitude(float x, float y, float z)
	{
		float magnitude = sqrt(x * x + y * y + z * z);
		return (magnitude < TOL) ? 1 : magnitude;
	}

	//--- Simple Hacks ----------
	private static final float sin(float a) {
		return PApplet.sin(a);
	}

	private static final float cos(float a) {
		return PApplet.cos(a);
	}

	private static final float tan(float a) {
		return PApplet.tan(a);
	}

	private static final float sqrt(float a) {
		return PApplet.sqrt(a);
	}

	private static final float atan2(float y, float x) {
		return PApplet.atan2(y, x);
	}

	private static final float degrees(float a) {
		return PApplet.degrees(a);
	}

	private static final float constrain(float v, float l, float u) {
		return PApplet.constrain(v, l, u);
	}

}
