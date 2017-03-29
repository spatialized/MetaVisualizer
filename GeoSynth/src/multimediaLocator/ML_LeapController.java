package multimediaLocator;

public class ML_LeapController {

	//
	// void updateLeapMotion() {
	//// int fps = leap.getFrameRate();
	// boolean gestureReceived = false;
	//
	// // ========= HANDS =========
	//
	// for (Hand hand : leap.getHands()) {
	// // ----- BASICS -----
	//
	//// int hand_id = hand.getId();
	// PVector hand_position = hand.getPosition();
	//// PVector hand_stabilized = hand.getStabilizedPosition();
	// PVector hand_direction = hand.getDirection();
	//// PVector hand_dynamics = hand.getDynamics();
	//// float hand_roll = hand.getRoll();
	//// float hand_pitch = hand.getPitch();
	//// float hand_yaw = hand.getYaw();
	//// boolean hand_is_left = hand.isLeft();
	//// boolean hand_is_right = hand.isRight();
	//// float hand_grab = hand.getGrabStrength();
	//// float hand_pinch = hand.getPinchStrength();
	//// float hand_time = hand.getTimeVisible();
	//// PVector sphere_position = hand.getSpherePosition();
	//// float sphere_radius = hand.getSphereRadius();
	//
	// if (hand.isValid()) {
	// gestureReceived = true;
	// noGestures = false;
	// noGesturesTimer = 0;
	//
	// handDirection = new PVector(degrees(hand_direction.x),
	// degrees(hand_direction.y),
	// degrees(hand_direction.z));
	// handPosition = hand_position;
	//
	// pushX = map(handPosition.x, leapXMin, leapXMax, -pushMax, pushMax);
	// pushY = map(handPosition.y, leapYMin, leapYMax, -pushMax, pushMax); // ??
	// pushZ = map(handPosition.z, leapZMin, leapZMax, -pushMax, pushMax);
	//
	// if (debugLeap) {
	// print("handDirection.x:" + handDirection.x);
	// print(" handDirection.y:" + handDirection.y);
	// println(" handDirection.z:" + handDirection.z);
	// print("handPosition.x:" + handPosition.x);
	// print(" handPosition.y:" + handPosition.y);
	// println(" handPosition.z:" + handPosition.z);
	// println("pushX:" + pushX);
	// println("pushY:" + pushY);
	// println("pushZ:" + pushZ);
	// }
	// // finger.getDirection():[ -0.17474209, 0.5805871, -0.7952256 ]
	// // finger.getPosition():[ 1135.0599, 549.00214, 32.88534 ]
	// // finger.getVelocity():[ 3518.8914, 1915.6492, -45.786263 ]
	//
	// // ----- SPECIFIC FINGER -----
	//
	//// Finger finger_thumb = hand.getThumb();
	// // or hand.getFinger("thumb");
	// // or hand.getFinger(0);
	//
	//// Finger finger_index = hand.getIndexFinger();
	// // or hand.getFinger("index");
	// // or hand.getFinger(1);
	//
	//// Finger finger_middle = hand.getMiddleFinger();
	// // or hand.getFinger("middle");
	// // or hand.getFinger(2);
	//
	//// Finger finger_ring = hand.getRingFinger();
	// // or hand.getFinger("ring");
	// // or hand.getFinger(3);
	//
	//// Finger finger_pink = hand.getPinkyFinger();
	// // or hand.getFinger("pinky");
	// // or hand.getFinger(4);
	//
	// // ----- DRAWING -----
	// // hand.draw();
	//
	// // ========= FINGERS =========
	//
	// for (Finger finger : hand.getFingers()) {
	//
	// // ----- BASICS -----
	//
	//// int finger_id = finger.getId();
	//// PVector finger_position = finger.getPosition();
	//// PVector finger_stabilized = finger.getStabilizedPosition();
	//// PVector finger_velocity = finger.getVelocity();
	//// PVector finger_direction = finger.getDirection();
	//// float finger_time = finger.getTimeVisible();
	//
	// // println("finger.getPosition():"+finger.getPosition());
	// // println("finger.getVelocity():"+finger.getVelocity());
	// // println("finger.getDirection():"+finger.getDirection());
	//
	// /*
	// * Example data: finger.getDirection():[ -0.17474209,
	// * 0.5805871, -0.7952256 ] finger.getPosition():[ 1135.0599,
	// * 549.00214, 32.88534 ] finger.getVelocity():[ 3518.8914,
	// * 1915.6492, -45.786263 ] finger.getDirection():[
	// * -0.33528015, 0.55383927, -0.7621347 ]
	// * finger.getPosition():[ 1228.2467, 548.0558, 32.21039 ]
	// * finger.getVelocity():[ 3698.3423, 1829.1487, -53.7855 ]
	// * finger.getDirection():[ -0.1715929, 0.29996315,
	// * -0.93839115 ] finger.getPosition():[ 1301.8777, 562.6171,
	// * 27.312014 ] finger.getVelocity():[ 3455.8044, 1837.1638,
	// * -75.84998 ] finger.getDirection():[ -0.046929117,
	// * 0.1615187, -0.9857532 ] finger.getPosition():[ 1033.3398,
	// * 561.29016, 23.506165 ] finger.getVelocity():[ 3380.0732,
	// * 1816.0905, -0.23886108 ] finger.getDirection():[
	// * 0.25051847, -0.15012953, -0.95640033 ]
	// * finger.getPosition():[ 1144.7125, 554.32196, 30.21267 ]
	// * finger.getVelocity():[ 3470.5935, 1866.3955, -32.481346 ]
	// * finger.getDirection():[ -0.13661624, 0.60512877,
	// * -0.78431827 ] finger.getPosition():[ 1183.2666, 568.8314,
	// * 31.227211 ] finger.getVelocity():[ 3458.0757, 1886.7263,
	// * -45.686966 ] finger.getDirection():[ -0.29830346,
	// * 0.57283133, -0.76346534 ] finger.getPosition():[
	// * 1276.9575, 567.6435, 30.1807 ] finger.getVelocity():[
	// * 3569.1387, 1839.3916, -59.93215 ] finger.getDirection():[
	// * -0.1148113, 0.31807765, -0.9410871 ]
	// * finger.getPosition():[ 1345.7472, 582.0141, 24.857573 ]
	// * finger.getVelocity():[ 3310.6191, 1837.5499, -83.19243 ]
	// * finger.getDirection():[ 0.012389837, 0.18375699,
	// * -0.9828936 ]
	// */
	//
	// // ----- SPECIFIC FINGER -----
	//
	// switch (finger.getType()) {
	// case 0:
	// // println("thumb");
	// break;
	// case 1:
	// // println("index");
	// break;
	// case 2:
	// // println("middle");
	// break;
	// case 3:
	// // println("ring");
	// break;
	// case 4:
	// // println("pinky");
	// break;
	// }
	//
	// // ----- DRAWING -----
	//
	// // finger.draw(); // = drawLines()+drawJoints()
	// // finger.drawLines();
	// // finger.drawJoints();
	//
	// // ----- TOUCH EMULATION -----
	//
	// int touch_zone = finger.getTouchZone();
	//// float touch_distance = finger.getTouchDistance();
	//
	// switch (touch_zone) {
	// case -1: // None
	// break;
	// case 0: // Hovering
	// // println("Hovering (#"+finger_id+"):
	// // "+touch_distance);
	// break;
	// case 1: // Touching
	// // println("Touching (#"+finger_id+")");
	// break;
	// }
	// }
	//
	// }
	//
	// // ========= TOOLS =========
	//
	// for (Tool tool : hand.getTools()) {
	//
	// // ----- BASICS -----
	//
	//// int tool_id = tool.getId();
	//// PVector tool_position = tool.getPosition();
	//// PVector tool_stabilized = tool.getStabilizedPosition();
	//// PVector tool_velocity = tool.getVelocity();
	//// PVector tool_direction = tool.getDirection();
	//// float tool_time = tool.getTimeVisible();
	//
	// // ----- DRAWING -----
	//
	// // tool.draw();
	//
	// // ----- TOUCH EMULATION -----
	//
	// int touch_zone = tool.getTouchZone();
	//// float touch_distance = tool.getTouchDistance();
	//
	// switch (touch_zone) {
	// case -1: // None
	// break;
	// case 0: // Hovering
	// // println("Hovering (#"+tool_id+"): "+touch_distance);
	// break;
	// case 1: // Touching
	// // println("Touching (#"+tool_id+")");
	// break;
	// }
	// }
	// }
	//
	// if (!gestureReceived && !noGestures) {
	// noGesturesTimer++;
	// // println("noGesturesTimer:"+noGesturesTimer);
	// if (noGesturesTimer > noGestureWaitTime)
	// noGestures = true;
	// }
	//
	// }
	//
	// void leapOnInit() {
	// if (debugLeap)
	// println("Leap Motion Init");
	// }
	//
	// void leapOnConnect() {
	// if (debugLeap)
	// println("Leap Motion Connect");
	// }
	//
	// void leapOnFrame() {
	// // println("Leap Motion Frame");
	// }
	//
	// void leapOnDisconnect() {
	// if (debugLeap)
	// println("Leap Motion Disconnect");
	// }
	//
	// void leapOnExit() {
	// if (debugLeap)
	// println("Leap Motion Exit");
	// }
}
