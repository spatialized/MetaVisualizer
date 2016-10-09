package gmViewer;

/**************************************************
 * Sound (OBSOLETE)
 * @author davidgordon
 * 
 * Class for drawing to screen and exporting images
 */

public class GMV_Sound 
{

	float audibleFarDistanceMin, audibleFarDistanceMax;
	float audibleFarDistanceFadeStart, audibleFarDistanceFadeLength = 40, audibleFarDistanceStartVal,
			audibleFarDistanceDestVal;
	float audibleFarDistanceDiv = (float) 1.5;
	boolean audibleFarDistanceTransition = false;

	float audibleNearDistanceMin, audibleNearDistanceMax;
	float audibleNearDistanceFadeStart, audibleNearDistanceFadeLength = 40, audibleNearDistanceStartVal,
			audibleNearDistanceDestVal;
	float audibleNearDistanceDiv = (float) 1.2; 
	boolean audibleNearDistanceTransition = false;

}
