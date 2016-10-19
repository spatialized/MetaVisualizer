package gmViewer;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.WritableRaster;
import java.util.ArrayList;

import javax.imageio.*;

import org.bytedeco.javacv.*;
import org.bytedeco.javacv.OpenCVFrameConverter.*;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.data.IntList;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_imgproc.*;
import org.bytedeco.javacpp.opencv_imgcodecs.*;
import org.bytedeco.javacpp.opencv_highgui.*;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.MatVector;
import static org.bytedeco.javacpp.opencv_stitching.Stitcher;

/***********************************
 * GMV_Stitcher
 * @author davidgordon
 * Class for stitching image sets into spherical panoramas
 * 
 */
public class GMV_Stitcher 
{
	private Stitcher stitcher;
	private final boolean try_use_gpu = true;
	private int stitchNum = 0;						// Export count for file naming

	GeoSynth p;
	GMV_Stitcher(GeoSynth parent)
	{
		p = parent;
		
		stitcher = Stitcher.createDefault(try_use_gpu);
//		stitcher.setWaveCorrection(true);
//		stitcher.setWaveCorrectKind(org.bytedeco.javacpp.opencv_stitching.WAVE_CORRECT_HORIZ);

		// Testing
//		stitcher.setRegistrationResol(0.3f);
//		stitcher.setSeamEstimationResol(0.05f);
		stitcher.setPanoConfidenceThresh(0.8f);

//	    Stitcher stitcher;
//	    stitcher.setRegistrationResol(0.6);
//	    stitcher.setSeamEstimationResol(0.1);
//	    stitcher.setCompositingResol(ORIG_RESOL);
//	    stitcher.setPanoConfidenceThresh(1);
//	    stitcher.setWaveCorrection(true);
//	    stitcher.setWaveCorrectKind(detail::WAVE_CORRECT_HORIZ);
//	    stitcher.setFeaturesMatcher(makePtr<detail::BestOf2NearestMatcher>(try_use_gpu));
//	    stitcher.setBundleAdjuster(makePtr<detail::BundleAdjusterRay>());
//	   
//		stitcher.setRegistrationResol(-1); 		/// ??
//		stitcher.setSeamEstimationResol(-1);   	/// ??
//		stitcher.setCompositingResol(-1);   	//??
//		stitcher.setPanoConfidenceThresh(-1);   //??
	}
	
	/**
	 * Stitch spherical panorama from images
	 * @param library
	 */
	public PImage stitch(String library, IntList imageList, int clusterID, int segmentID, IntList selected)
	{
		Mat panorama = new Mat();				// Panoramic image result
		IplImage iplImage = null;

		boolean success = false, end = false;		
		boolean reduce = false;				// Reduce images to try to stitch
		int count = 0;
		
		// Prevent fatal error
		while(imageList.size() > p.maxStitchingImages)
			imageList.remove(imageList.size()-1);

		String[] images = getImageNames(imageList);				

		while(!success || end)
		{
			if(reduce)		// Use orientation to exclude!
			{
				if(imageList.size() > 2)
				{
					imageList.remove(imageList.size()-1);
					images = getImageNames(imageList);
				}
				else
					end = true;				// Impossible to stitch less than 2 images
			}
			
			if(!end)
			{
				/* Error Codes: 	OK = 0	ERR_NEED_MORE_IMGS = 1	ERR_HOMOGRAPHY_EST_FAIL = 2	 ERR_CAMERA_PARAMS_ADJUST_FAIL = 3 	*/
				
				MatVector imgs = new MatVector();		
				imgs = getMatVectorImages(images);

				if(!imgs.isNull())
				{
					if(p.debug.stitching) PApplet.println("Attempting to stitch "+imgs.size()+" images...");

					Mat pano = new Mat();
					int status = stitcher.stitch(imgs, pano);
					
					if (status == Stitcher.OK) 
					{
						success = true;
						end = false;
						panorama = new Mat(pano);
					}
					else
					{
						if(p.debug.stitching) p.display.message("Error #" + status + " couldn't stitch panorama...");
						if(status == 3)				// Error estimating camera parameters
						{
							if(p.persistentStitching) reduce = true;
							else end = true;
						}
						else
							end = true;
					}

					imgs.close();
					if(count++ > 100) break;		// Avoid infinite while loop
				}
				else
				{
					if(p.debug.stitching) p.display.message("Couldn't stitch panorama... No images!");
					break;
				}
			}
			else break;
		}
	
		PImage result = p.createImage(0,0,PApplet.RGB);

		if(success)
		{
			String filePath = "";
			String fileName = "";
			
			if(segmentID != -1)
				fileName = p.getCurrentField().name+"_"+clusterID+"_"+segmentID+"_stitched.jpg";
			else
				fileName = p.getCurrentField().name+"_"+clusterID+"_stitched_"+(stitchNum++)+".jpg";
			
			filePath = p.stitchingPath+fileName;

			org.bytedeco.javacpp.opencv_imgcodecs.imwrite(filePath, panorama);
			if(p.debug.stitching) p.display.message("Panorama stitching successful, output to file: " + fileName);

			iplImage = new IplImage(panorama);
			PApplet.println("panorama.toString():"+panorama.toString());
			PApplet.println("panorama.depth():"+panorama.depth());
			PApplet.println("panorama.channels():"+panorama.channels());
			PApplet.println("iplImage.toString():"+iplImage.toString());
			PApplet.println("iplImage.depth():"+iplImage.depth());
			PApplet.println("iplImage.nChannels():"+iplImage.nChannels());
			PApplet.println("iplImage.dataOrder():"+iplImage.dataOrder());
			PApplet.println("iplImage.widthStep():"+iplImage.widthStep());
		}
		


//		{	// TESTING CONVERSION FUNCTIONS		-- Read panorama from disk using OpenCV??!!
//			
//			PImage test = iplImageToPImage(new IplImage(panorama));		
//			test.save(p.stitchingPath+p.getCurrentField().name+"_"+clusterID+"_stitched_"+stitchNum+"_conversionTestPImage.jpg");
//			IplImage iplTest = pImageToIplImage(test);
//			
//			String filePath = "";
//			String fileName = "";
//
//			if(segmentID != -1)
//				fileName = p.getCurrentField().name+"_"+clusterID+"_"+segmentID+"_stitched.jpg";
//			else
//				fileName = p.getCurrentField().name+"_"+clusterID+"_stitched_"+stitchNum+"_conversionTest1Ipl.jpg";
//
//			filePath = p.stitchingPath+fileName;
//
//			org.bytedeco.javacpp.opencv_imgcodecs.imwrite(filePath, new Mat(iplTest));
//			if(p.debug.stitching) p.display.message("CONVERSION FUNCTIONS test 1 successful... " + fileName);
//		}
//		
//		{
//			Mat testing = org.bytedeco.javacpp.opencv_imgcodecs.imread("/Users/davidgordon/Dropbox/Software/GeoSynth2016/Code/JavaCVPImageConversion/img.jpg");
//			org.bytedeco.javacpp.opencv_imgcodecs.imwrite(p.stitchingPath+"_testing.jpg", testing);
//
//			PImage test = iplImageToPImage(new IplImage(testing));		
//			test.save(p.stitchingPath+p.getCurrentField().name+"_"+clusterID+"_stitched_"+stitchNum+"_conversionTestImread.jpg");
//			IplImage iplTest = pImageToIplImage(test);
//			
//			String filePath = "";
//			String fileName = "";
//
//			if(segmentID != -1)
//				fileName = p.getCurrentField().name+"_"+clusterID+"_"+segmentID+"_stitched.jpg";
//			else
//				fileName = p.getCurrentField().name+"_"+clusterID+"_stitched_"+stitchNum+"_conversionTest2Ipl.jpg";
//
//			filePath = p.stitchingPath+fileName;
//
//			org.bytedeco.javacpp.opencv_imgcodecs.imwrite(filePath, new Mat(iplTest));
//			if(p.debug.stitching) p.display.message("CONVERSION FUNCTIONS test 2 successful... " + fileName);
//		}

		
		
		panorama.close();

		if(success)
		{
//			GMV_MediaSegment( GMV_Cluster parent, int newID, IntList newImages, IntList newVideos, float newLower, float newUpper, 
//				  float newCenter, float newLowerElevation, float newUpperElevation, float newCenterElevation)

			GMV_MediaSegment segment;			// Segment of the panorama

			PApplet.println("Calculating user selection borders...");

			/* Calculate user selection borders */
			if(segmentID == -1)
			{
				float left = 360.f;
				float right = 0.f;
				float lower = 90.f;
				float upper = -90.f;

				for(int index : selected)
				{
					GMV_Image i = p.getCurrentField().images.get(index);
					if(i.getDirection() < left)
						left = i.getDirection();
					if(i.getDirection() > right)
						right = i.getDirection();
					if(i.getElevation() > upper)
						upper = i.getElevation();
					if(i.getElevation() < lower)
						lower = i.getElevation();
				}

				float centerDirection = (right + left) / 2.f;
				float centerElevation = (upper + lower) / 2.f;

				segment = new GMV_MediaSegment( p.getCluster(clusterID), -1, selected, null, left, right, centerDirection,
						lower, upper, centerElevation);
			}
			else
			{
				segment = p.getCluster(clusterID).getMediaSegment(segmentID);
			}

			result = addImageBorders(iplImage, clusterID, segment);
			
			{	// TESTING
				String filePath = "";
				String fileName = "";

				if(segmentID != -1)
					fileName = p.getCurrentField().name+"_"+clusterID+"_"+segmentID+"_stitched_borders.jpg";
				else
					fileName = p.getCurrentField().name+"_"+clusterID+"_stitched_"+stitchNum+"_borders.jpg";

				filePath = p.stitchingPath+fileName;

//			org.bytedeco.javacpp.opencv_imgcodecs.imwrite(filePath, panorama);
				if(p.debug.stitching) p.display.message("Debugging: output panorama with borders to file: " + fileName);
				result.save(filePath);
			}

			
			PApplet.println("Final Width:"+result.width+" Height:"+result.height);
			PApplet.println("Final Aspect Ratio:"+((float)result.width/(float)result.height));
		}
		return result;
	}
	
	/**
	 * Get array of images as MatVector 
	 * @param images Image filepaths
	 * @return MatVector of the images
	 */
	private MatVector getMatVectorImages(String[] images)
	{
		MatVector imgs = new MatVector();		
		for(int i=0; i<images.length; i++)
		{
			Mat img = org.bytedeco.javacpp.opencv_imgcodecs.imread(images[i]);

			if( img.empty())
			{
				if(p.debug.stitching)
					PApplet.println("Image "+i+" is empty...");
			}
			else
			{
				imgs.resize(imgs.size() + 1);
				imgs.put(imgs.size() - 1, img);
				if(p.debug.stitching)
					PApplet.println("Added image to stitching list: "+images[i]);
			}
		}
		
		return imgs;
	}

	/**
	 * Get image names from image IDs
	 * @param imageList List of image IDs
	 * @return Array of image names
	 */
	private String[] getImageNames(IntList imageList)
	{
		String[] images = new String[imageList.size()];
		
		int count = 0;
		for(int i:imageList)
		{
			images[count] = p.getCurrentField().images.get(i).getFilePath();
			count++;
		}
		
		return images;
	}
	
	/**
	 * Add black border to images			
	 * @param source Image source
	 * @param clusterID Source image cluster
	 * @param segmentID Source image media segment in cluster
	 * @return Image with specified borders 
	 */
	public PImage addImageBorders(IplImage src, int clusterID, GMV_MediaSegment s)
	{
		float imgVertCoverage = 45.f;		// 60 is default (vert) field of view
		float imgHorizCoverage = 60.f;		// 80 is default (horiz) field of view
		
		float sLeft = s.getLeft();
		float sRight = s.getRight();
		float sBottom = s.getBottom();
		float sTop = s.getTop();
		
		float aspect = (float)src.width() / (float)src.height();
		
		PApplet.println("--> addImageBorders()...");
		PApplet.println(" width():"+src.width()+" height:"+src.height()+" aspect:"+aspect);
		PApplet.println(" sLeft:"+sLeft+" sRight:"+sRight+" sBottom:"+sBottom+" sTop:"+sTop);

		float top = sTop + imgVertCoverage * 0.5f;
		float bottom = sBottom - imgVertCoverage * 0.5f;
		float left = sLeft - imgHorizCoverage * 0.5f;
		float right = sRight + imgHorizCoverage * 0.5f;
		
		PApplet.println(" top:"+top+" bottom:"+bottom+" left:"+left+" right:"+right);

		float xCoverage = PApplet.constrain(right - left, 0.f, 360.f);			// -- Check if constrain works
		float yCoverage = PApplet.constrain(top - bottom, 0.f, 180.f);
		PApplet.println(" xCoverage:"+xCoverage+" yCoverage:"+yCoverage);

		float fullWidth = 360.f * src.width() / xCoverage;
		float fullHeight = 180.f * src.height() / yCoverage;

		float xDiff = fullWidth - src.width();
		float yDiff = fullHeight - src.height();
		
		int topBorder = PApplet.abs(PApplet.round(yDiff / 2.f));
		int bottomBorder = PApplet.abs(PApplet.round(yDiff / 2.f));
		int leftBorder = PApplet.abs(PApplet.round(xDiff / 2.f));
		int rightBorder = PApplet.abs(PApplet.round(xDiff / 2.f));
		
		boolean error = false;

		PApplet.println(" topBorder:"+topBorder+" bottomBorder:"+bottomBorder+" leftBorder:"+leftBorder+" rightBorder:"+rightBorder);

		Mat image = new Mat( src );
		if (image.empty()) 
		{
			if(p.debug.stitching)
				PApplet.println(" Error reading image...");
			error = true;
		}
		
		if(!error)
		{
			Mat resized = new Mat(image.rows() + top + bottom, image.cols() + right + left, image.depth());
			
//			
//			String filePath = "";
//			String fileName = "";
//			
//				fileName = p.getCurrentField().name+"_testResizedImage.jpg";
//			
//			filePath = p.stitchingPath+fileName;
//	
//			org.bytedeco.javacpp.opencv_imgcodecs.imwrite(filePath, image);
//			if(p.debug.stitching) p.display.message("Panorama stitching successful, output to file: " + fileName);
	
//			resized = new Mat(image);
		
			
		

			  //		  void copyMakeBorder(InputArray src, OutputArray dst, int top, int bottom, int left, int right, int borderType, const Scalar& value=Scalar() )¶
			  //		  src – Source image.
			  //		  dst – Destination image of the same type as src and the size Size(src.cols+left+right, src.rows+top+bottom) .
			  //		  top –
			  //		  bottom –
			  //		  left –
			  //		  right – Parameter specifying how many pixels in each direction from the source image rectangle to extrapolate. For example, top=1, bottom=1, left=1, right=1 mean that 1 pixel-wide border needs to be built.
			  //		  borderType – Border type. See borderInterpolate() for details.
			  //		  value – Border value if borderType==BORDER_CONSTANT .

			
			org.bytedeco.javacpp.opencv_core.copyMakeBorder(image, resized, topBorder, bottomBorder, leftBorder, rightBorder, 
					org.bytedeco.javacpp.opencv_core.BORDER_CONSTANT, 
					new org.bytedeco.javacpp.opencv_core.Scalar((double)0.f));

			if(p.debug.stitching)
				PApplet.println("--> Finished adding image border... ");
			
			PImage pImg = iplImageToPImage(new IplImage(resized));
			image.close();
			resized.close();
			
			return pImg;
		}
		else
		{
			image.close();
			return null;
		}
	}
	
	/**
	 * Convert PImage to IplImage
	 * @param img Image source
	 * @return The IplImage
	 */
	IplImage pImageToIplImage( PImage img )
	{
	  BufferedImage bufferedImage = pImageToBufferedImage(img);    // WORKS
	  return bufferedImageToIplImage(bufferedImage);              
	}

	/**
	 * Convert IplImage to PImage 
	 * @param img Image source
	 * @return The PImage
	 */
	PImage iplImageToPImage ( IplImage img )                       // ERROR
	{
	  java.awt.image.BufferedImage bImg = iplImageToBufferedImage(img);
	  PImage pImg = bufferedImageToPImage(bImg);
	  
	  return pImg;
	}

	/**
	 * Convert PImage to BufferedImage
	 * @param img Image source
	 * @return The BufferedImage
	 */
	public BufferedImage pImageToBufferedImage( PImage img )            // WORKS
	{
	  PGraphics pg = p.createGraphics(img.width, img.height, PApplet.JAVA2D);

	  pg.colorMode(PApplet.RGB);
	  pg.beginDraw();
	  pg.image(img, 0, 0);
	  //pg.background(0, 0, 255);
	  //pg.background(0, 255, 255);
	  pg.endDraw();

	  BufferedImageBuilder bufImgBuilder = new BufferedImageBuilder();
	  int type = (img.format == PApplet.RGB) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
	  //BufferedImage buf = bufImgBuilder.bufferImage((java.awt.Image)pg.image, 2);
	  //type = BufferedImage.TYPE_INT_ARGB_PRE;   // Testing
	  BufferedImage buf = bufImgBuilder.bufferImage((java.awt.Image)pg.image, type);

	  return buf;
	}

	/**
	 * Convert BufferedImage to IplImage
	 * @param img Image source
	 * @return The IplImage
	 */
	IplImage bufferedImageToIplImage(BufferedImage img) {      // WORKS
	  ToIplImage iplConverter = new OpenCVFrameConverter.ToIplImage();
	  Java2DFrameConverter java2dConverter = new Java2DFrameConverter();
	  Java2DFrameConverter.copy( java2dConverter.convert(img), img, (double)1, true, new Rectangle(img.getWidth(), img.getHeight()) );

	  IplImage iplImage = iplConverter.convert(java2dConverter.convert(img));
	  org.bytedeco.javacpp.opencv_core.cvMixChannels(iplImage, 1, iplImage, 1, new int[] {0,0, 1,1, 2,2, 3,3}, 4); // ARGB to BGRA 

	  iplImage.clone();
	  return iplImage;
	}

	/**
	 * Convert IplImage to BufferedImage
	 * @param img Image source
	 * @return The IplImage
	 */
	public BufferedImage iplImageToBufferedImage(IplImage img) 
	{
		ToIplImage iplConverter = new OpenCVFrameConverter.ToIplImage();
		Java2DFrameConverter java2dConverter = new Java2DFrameConverter();
		Frame frame = iplConverter.convert(img);
		return java2dConverter.getBufferedImage(frame, 1);
	}

	public PImage bufferedImageToPImage(BufferedImage bimg)
	{         
		try {
			PImage img=new PImage(bimg.getWidth(), bimg.getHeight(), PConstants.ARGB);
			bimg.getRGB(0, 0, img.width, img.height, img.pixels, 0, img.width);
			img.updatePixels();
			return img;
		}
		catch(Exception e) {
			System.err.println("Can't create image from buffer");
			e.printStackTrace();
		}
		return null;
	}

	public class BufferedImageBuilder {
	  private static final int DEFAULT_IMAGE_TYPE = BufferedImage.TYPE_INT_RGB;

	  public BufferedImage bufferImage(Image image) {
	    return bufferImage(image, DEFAULT_IMAGE_TYPE);
	  }

	  public BufferedImage bufferImage(Image image, int type) {
	    BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
	    Graphics2D g1 = bufferedImage.createGraphics();
	    g1.drawImage(image, null, null);
	    waitForImage(bufferedImage);
	    return bufferedImage;
	  }

	  private void waitForImage(BufferedImage bufferedImage) {
	    final ImageLoadStatus imageLoadStatus = new ImageLoadStatus();
	    bufferedImage.getHeight(new ImageObserver() {
	      public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
	        if (infoflags == ALLBITS) {
	          imageLoadStatus.heightDone = true;
	          return true;
	        }
	        return false;
	      }
	    }
	    );

	    bufferedImage.getWidth(new ImageObserver() {
	      public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
	        if (infoflags == ALLBITS) {
	          imageLoadStatus.widthDone = true;
	          return true;
	        }
	        return false;
	      }
	    }
	    );
	  }

	  class ImageLoadStatus {
	    public boolean widthDone = false;
	    public boolean heightDone = false;
	  }
	}
}

