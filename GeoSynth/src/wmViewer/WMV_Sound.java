package wmViewer;

import beads.*;

/**************************************************
 * @author davidgordon
 * Represents a sound in 3D virtual space
 */

public class WMV_Sound 
{
//	SoundFile sound;
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
