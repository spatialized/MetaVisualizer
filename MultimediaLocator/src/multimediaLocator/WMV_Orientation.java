package multimediaLocator;

/**
 * Euler angle orientation of a point in virtual space
 * @author davidgordon
 */
public class WMV_Orientation 
{
	private int id;
	private float direction, elevation, rotation;
	
	public WMV_Orientation(int newID, float newDirection, float newElevation, float newRotation)
	{
		id = newID;
		direction = newDirection;
		elevation = newElevation;
		rotation = newRotation;
	}
	
	public void setID(int newID)
	{
		id = newID;
	}
	
	public int getID()
	{
		return id;
	}
	
	public void setDirection(float newDirection)
	{
		direction = newDirection;
	}
	
	public float getDirection()
	{
		return direction;
	}

	public void setElevation(float newElevation)
	{
		elevation = newElevation;
	}
	
	public float getElevation()
	{
		return elevation;
	}

	public void setRotation(float newRotation)
	{
		rotation = newRotation;
	}
	
	public float getRotation()
	{
		return rotation;
	}
}
