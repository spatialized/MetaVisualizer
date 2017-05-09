//package multimediaLocator;
//
//import java.nio.IntBuffer;
//
//import processing.core.PApplet;
//import processing.core.PImage;
//import processing.core.PShape;
//import processing.opengl.PGL;
//import processing.opengl.PShader;
//
///**
// * Cubemap object for projecting in fulldome
// * @author davidgordon
// * Based on Processing 3.0 Example sketch "DomeProjection" 
// */
//public class WMV_CubeMap 
//{
////	public PShader cubemapShader;
////	public PShape domeSphere;
//
//	public IntBuffer fbo;
//	public IntBuffer rbo;
//	public IntBuffer envMapTextureID;
//
//	public int envMapSize = 1024;   
//
//	/**
//	 * Constructor for cubemap
//	 * @param ml Parent app
//	 */
//	WMV_CubeMap(){}
//
////	public void initCubeMap(MultimediaLocator ml)
////	{
////		System.out.println("initCubeMap()...");
////		ml.sphereDetail(50);
////		domeSphere = ml.createShape(PApplet.SPHERE, ml.height/2.0f);
////		domeSphere.rotateX(PApplet.HALF_PI);
////		domeSphere.setStroke(false);
////
////		PGL pgl = ml.beginPGL();
////
////		envMapTextureID = IntBuffer.allocate(1);
////		pgl.genTextures(1, envMapTextureID);
////		pgl.bindTexture(PGL.TEXTURE_CUBE_MAP, envMapTextureID.get(0));
////		pgl.texParameteri(PGL.TEXTURE_CUBE_MAP, PGL.TEXTURE_WRAP_S, PGL.CLAMP_TO_EDGE);
////		pgl.texParameteri(PGL.TEXTURE_CUBE_MAP, PGL.TEXTURE_WRAP_T, PGL.CLAMP_TO_EDGE);
////		pgl.texParameteri(PGL.TEXTURE_CUBE_MAP, PGL.TEXTURE_WRAP_R, PGL.CLAMP_TO_EDGE);
////		pgl.texParameteri(PGL.TEXTURE_CUBE_MAP, PGL.TEXTURE_MIN_FILTER, PGL.NEAREST);
////		pgl.texParameteri(PGL.TEXTURE_CUBE_MAP, PGL.TEXTURE_MAG_FILTER, PGL.NEAREST);
////		for (int i = PGL.TEXTURE_CUBE_MAP_POSITIVE_X; i < PGL.TEXTURE_CUBE_MAP_POSITIVE_X + 6; i++) {
////			pgl.texImage2D(i, 0, PGL.RGBA8, envMapSize, envMapSize, 0, PGL.RGBA, PGL.UNSIGNED_BYTE, null);
////		}
////
////		fbo = IntBuffer.allocate(1);
////		rbo = IntBuffer.allocate(1);
////		pgl.genFramebuffers(1, fbo);
////		pgl.bindFramebuffer(PGL.FRAMEBUFFER, fbo.get(0));
////		pgl.framebufferTexture2D(PGL.FRAMEBUFFER, PGL.COLOR_ATTACHMENT0, PGL.TEXTURE_CUBE_MAP_POSITIVE_X, envMapTextureID.get(0), 0);
////
////		pgl.genRenderbuffers(1, rbo);
////		pgl.bindRenderbuffer(PGL.RENDERBUFFER, rbo.get(0));
////		pgl.renderbufferStorage(PGL.RENDERBUFFER, PGL.DEPTH_COMPONENT24, envMapSize, envMapSize);
////
////		// Attach depth buffer to FBO
////		pgl.framebufferRenderbuffer(PGL.FRAMEBUFFER, PGL.DEPTH_ATTACHMENT, PGL.RENDERBUFFER, rbo.get(0));    
////
////		ml.endPGL();
////
////		// Load cubemap shader.
////		ml.cubemapShader = ml.loadShader("shaders/cubemapfrag.glsl", "shaders/cubemapvert.glsl");
////		ml.cubemapShader.set("cubemap", 1);
////		
////		ml.cubeMapInitialized = true;
////	}
//
////	void drawCubeMap(MultimediaLocator ml) {
////		PGL pgl = ml.beginPGL();
////		pgl.activeTexture(PGL.TEXTURE1);
////		pgl.enable(PGL.TEXTURE_CUBE_MAP);  
////		pgl.bindTexture(PGL.TEXTURE_CUBE_MAP, envMapTextureID.get(0));     
////		regenerateEnvMap(ml, pgl);
////		ml.endPGL();
////
////		drawDomeMaster(ml);
////
////		pgl.bindTexture(PGL.TEXTURE_CUBE_MAP, 0);
////	}
////	
////	void drawDomeMaster(MultimediaLocator ml) {
////		ml.camera();
////		ml.ortho();
////		ml.resetMatrix();
////		ml.shader(cubemapShader);
////		ml.shape(domeSphere);
////		ml.resetShader();
////	}
//
////	// Called to regenerate the envmap
////	void regenerateEnvMap(MultimediaLocator ml, PGL pgl) {    
////		// bind fbo
////		pgl.bindFramebuffer(PGL.FRAMEBUFFER, fbo.get(0));
////
////		// generate 6 views from origin(0, 0, 0)
////		pgl.viewport(0, 0, envMapSize, envMapSize);    
////		ml.perspective(90.0f * PApplet.DEG_TO_RAD, 1.0f, 1.0f, 1025.0f);  
////		for (int face = PGL.TEXTURE_CUBE_MAP_POSITIVE_X; face < 
////				PGL.TEXTURE_CUBE_MAP_NEGATIVE_Z; face++) {
////			ml.resetMatrix();
////
////			if (face == PGL.TEXTURE_CUBE_MAP_POSITIVE_X) {
////				ml.camera(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f);
////			} else if (face == PGL.TEXTURE_CUBE_MAP_NEGATIVE_X) {
////				ml.camera(0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f);
////			} else if (face == PGL.TEXTURE_CUBE_MAP_POSITIVE_Y) {
////				ml.camera(0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f);  
////			} else if (face == PGL.TEXTURE_CUBE_MAP_NEGATIVE_Y) {
////				ml.camera(0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f);
////			} else if (face == PGL.TEXTURE_CUBE_MAP_POSITIVE_Z) {
////				ml.camera(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f);    
////			}
////
////			ml.scale(-1, 1, -1);
////			ml.translate(-ml.width * 0.5f, -ml.height * 0.5f, -500);
////
////			pgl.framebufferTexture2D(PGL.FRAMEBUFFER, PGL.COLOR_ATTACHMENT0, face, envMapTextureID.get(0), 0);
////
////			drawScene(ml); // Draw objects in the scene
////			
////			ml.flush(); // Make sure that the geometry in the scene is pushed to the GPU    
////			ml.noLights();  // Disabling lights to avoid adding many times
////			pgl.framebufferTexture2D(PGL.FRAMEBUFFER, PGL.COLOR_ATTACHMENT0, face, 0, 0);
////		}
////	}
//
////	void drawTestScene(MultimediaLocator ml) {  
////		ml.background(0);
//////		ml.pushMatrix();
////		//rotateX(rotx);
////		//rotateY(roty); // turn through change of value
//////		ml.translate(ml.world.viewer.getLocation().x, ml.world.viewer.getLocation().y, ml.world.viewer.getLocation().z+300.f);
////		
////		// DRAW SCENE HERE
////		ml.stroke(255.f, 0.f, 0.f);
////		ml.strokeWeight(2.f);
////		
////		for (int i = -ml.width; i < 2 * ml.width; i += 50) {
////			ml.line(i, -ml.height, -100, i, 2 * ml.height, -100);
////		}
////		for (int i = -ml.height; i < 2 * ml.height; i += 50) {
////			ml.line(-ml.width, i, -100, 2 * ml.width, i, -100);
////		}
////
////		ml.lights();
////		ml.noStroke();
////		ml.translate(ml.mouseX, ml.mouseY, 300);
////		ml.rotateX(ml.frameCount * 0.01f);
////		ml.rotateY(ml.frameCount * 0.01f);  
////		ml.box(100);
////		
//////		texturedSphere(2000, sphere_tex);
//////		ml.popMatrix();
////	}
//	
////	void drawTestGrid(MultimediaLocator ml)
////	{
////	  ml.stroke(255, 0, 0);
////	  ml.strokeWeight(2);
////	  for (int i = -ml.width; i < 2 * ml.width; i += 50) {
////		  ml.line(i, -ml.height, -100, i, 2 * ml.height, -100);
////	  }
////	  for (int i = -ml.height; i < 2 * ml.height; i += 50) {
////		  ml.line(-ml.width, i, -100, 2 * ml.width, i, -100);
////	  }
////
////	  ml.lights();
////	  ml.noStroke();
////	  ml.box(100);
////	}
//}
//
//
