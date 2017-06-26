package multimediaLocator;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.List;

import org.bytedeco.javacv.*;
import org.bytedeco.javacv.OpenCVFrameConverter.*;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

import org.bytedeco.javacpp.opencv_core.*;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.MatVector;
import static org.bytedeco.javacpp.opencv_stitching.Stitcher;

/***********************************
 * Class for stitching image sets into spherical panoramas
 * @author davidgordon
 */
public class ML_Stitcher 
{
	private Stitcher stitcher;
	private final boolean try_use_gpu = true;
	private int stitchNum = 0;						// Export count for file naming

	WMV_World p;
	
	ML_Stitcher(WMV_World parent)
	{
		p = parent;
		
		stitcher = Stitcher.createDefault(try_use_gpu);
//		stitcher.setWaveCorrection(true);
//		stitcher.setWaveCorrectKind(org.bytedeco.javacpp.opencv_stitching.WAVE_CORRECT_HORIZ);

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
	 * Stitch a 360 degree panorama or panorama segment from images
	 * @param library
	 */
	public WMV_Panorama stitch(String library, List<Integer> imageList, int clusterID, int segmentID, List<Integer> selected)
	{
		Mat panorama = new Mat();							// Panoramic image result
		IplImage iplImage = null;

		boolean success = false, end = false;		
		boolean reduce = false;								// Reduce images to try to force stitching
		int count = 0;
		
		while(imageList.size() > p.settings.maxStitchingImages)		// Remove images above maximum number
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
					if(p.ml.debugSettings.stitching) System.out.println("Attempting to stitch "+imgs.size()+" images...");

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
						if(p.ml.debugSettings.stitching) p.ml.display.message(p.ml, "Error #" + status + " couldn't stitch panorama...");
						if(status == 3)				// Error estimating camera parameters
						{
							if(p.settings.persistentStitching) reduce = true;
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
					if(p.ml.debugSettings.stitching) p.ml.display.message(p.ml, "Couldn't stitch panorama... No images!");
					break;
				}
			}
			else break;
		}
	
		if(success)
		{
			String filePath = "";
			String fileName = "";
			
			if(segmentID != -1)
				fileName = p.getCurrentField().getName()+"_"+clusterID+"_"+segmentID+"_stitched.jpg";
			else
				fileName = p.getCurrentField().getName()+"_"+clusterID+"_stitched_"+(stitchNum++)+".jpg";
			
			filePath = p.getState().stitchingPath+fileName;

			org.bytedeco.javacpp.opencv_imgcodecs.imwrite(filePath, panorama);
			if(p.ml.debugSettings.stitching) p.ml.display.message(p.ml, "Panorama stitching successful, output to file: " + fileName);

			iplImage = new IplImage(panorama);
			System.out.println("panorama.toString():"+panorama.toString());
			System.out.println("panorama.depth():"+panorama.depth());
			System.out.println("panorama.channels():"+panorama.channels());
			System.out.println("iplImage.toString():"+iplImage.toString());
			System.out.println("iplImage.depth():"+iplImage.depth());
		}
		
		panorama.close();

		if(success)
		{
			WMV_MediaSegment segment;			// Segment of the panorama

			System.out.println("Calculating user selection borders...");

			/* Calculate user selection borders */
			if(segmentID == -1)
			{
				float left = 360.f;
				float right = 0.f;
				float lower = 90.f;
				float upper = -90.f;

				for(int index : selected)
				{
					WMV_Image i = p.getCurrentField().getImage(index);
					if(i.getDirection() < left)
						left = i.getDirection();
					if(i.getDirection() > right)
						right = i.getDirection();
					if(i.getElevationAngle() > upper)
						upper = i.getElevationAngle();
					if(i.getElevationAngle() < lower)
						lower = i.getElevationAngle();
				}

				float centerDirection = (right + left) / 2.f;
				float centerElevation = (upper + lower) / 2.f;

				segment = new WMV_MediaSegment( -1, selected, left, right, centerDirection, lower, upper, centerElevation, p.getSettings().stitchingMinAngle);
			}
			else
			{
				segment = p.getCurrentField().getCluster(clusterID).getMediaSegment(segmentID);
			}

			PImage result = addImageBorders(iplImage, clusterID, segment);
			
			if(p.ml.debugSettings.stitching)	// TESTING
			{	
				String filePath = "";
				String fileName = "";

				if(segmentID != -1)
					fileName = p.getCurrentField().getName()+"_"+clusterID+"_"+segmentID+"_stitched_borders.jpg";
				else
					fileName = p.getCurrentField().getName()+"_"+clusterID+"_stitched_"+stitchNum+"_borders.jpg";

				filePath = p.getState().stitchingPath+fileName;

				if(p.ml.debugSettings.stitching) p.ml.display.message(p.ml, "Debugging: output panorama with borders to file: " + fileName);
				
				result.save(filePath);
			}

			float panoDirection = segment.getCenterDirection() - 90.f;		// Why 90?
			if(panoDirection < 0.f) panoDirection += 360.f;
			if(panoDirection > 360.f) panoDirection -= 360.f;
			
			float panoElevation = segment.getCenterElevation();
			
			
			WMV_PanoramaMetadata pMetadata = new WMV_PanoramaMetadata("_stitched_"+Integer.toString(segment.getID()), "", null, null, "", "",
					panoDirection, -1, result.width, result.height, 1.f, null, ""); 

			WMV_Panorama pano = new WMV_Panorama( segment.getID(), 1, panoElevation, p.getCurrentField().getCluster(clusterID).getLocation(), 
					result, pMetadata );
		
			if(p.ml.debugSettings.stitching)
			{
				System.out.println("Final Width:"+result.width+" Height:"+result.height);
				System.out.println("Final Aspect Ratio:"+((float)result.width/(float)result.height));
			}
			return pano;
		}
		
		return null;
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
				if(p.ml.debugSettings.stitching)
					System.out.println("Image "+i+" is empty...");
			}
			else
			{
				imgs.resize(imgs.size() + 1);
				imgs.put(imgs.size() - 1, img);
				if(p.ml.debugSettings.stitching)
					System.out.println("Added image to stitching list: "+images[i]);
			}
		}
		
		return imgs;
	}

	/**
	 * Get image names from image IDs
	 * @param imageList List of image IDs
	 * @return Array of image names
	 */
//	private String[] getImageNames(IntList imageList)
	private String[] getImageNames(List<Integer> imageList)
	{
		String[] images = new String[imageList.size()];
		
		int count = 0;
		for(int i:imageList)
		{
			images[count] = p.getCurrentField().getImage(i).getFilePath();
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
	public PImage addImageBorders(IplImage src, int clusterID, WMV_MediaSegment s)
	{
		float imgVertCoverage = 45.f;		// 60 is default (vert) field of view
		float imgHorizCoverage = 60.f;		// 80 is default (horiz) field of view
		
		float sLeft = s.getLeft();
		float sRight = s.getRight();
		float sBottom = s.getBottom();
		float sTop = s.getTop();
		
		float aspect = (float)src.width() / (float)src.height();
		
		if(p.ml.debugSettings.stitching)
		{
			System.out.println("--> addImageBorders()...");
			System.out.println(" width():"+src.width()+" height:"+src.height()+" aspect:"+aspect);
			System.out.println(" sLeft:"+sLeft+" sRight:"+sRight+" sBottom:"+sBottom+" sTop:"+sTop);
		}
		float top = sTop + imgVertCoverage * 0.5f;
		float bottom = sBottom - imgVertCoverage * 0.5f;
		float left = sLeft - imgHorizCoverage * 0.5f;
		float right = sRight + imgHorizCoverage * 0.5f;
		
		if(p.ml.debugSettings.stitching)
			System.out.println(" top:"+top+" bottom:"+bottom+" left:"+left+" right:"+right);

		float xCoverage = PApplet.constrain(right - left, 0.f, 360.f);			// -- Check if constrain works
		float yCoverage = PApplet.constrain(top - bottom, 0.f, 180.f);
		if(p.ml.debugSettings.stitching)
			System.out.println(" xCoverage:"+xCoverage+" yCoverage:"+yCoverage);

		float fullWidth, fullHeight;
		
		// New Method:
		if(aspect > 2.f)		// Wider than 2/1
		{
			fullWidth = 4096;
			fullHeight = PApplet.round(fullWidth * 0.5f);
		}
		else if(aspect < 2.f)	// Taller than 2/1
		{
			fullHeight = 2048;
			fullWidth = PApplet.round(fullHeight * 2.f);
		}
		else
		{
			fullWidth = 4096;
			fullHeight = 2048;
		}
		
		// Old Method:
//		float fullWidth = 360.f * src.width() / xCoverage;
//		float fullHeight = 180.f * src.height() / yCoverage;

		float xDiff = fullWidth - src.width();
		float yDiff = fullHeight - src.height();
		
		if(p.ml.debugSettings.stitching)
			System.out.println(" fullWidth:"+fullWidth+" fullHeight:"+fullHeight+" xDiff:"+xDiff+" yDiff:"+yDiff);
		
		int topBorder = PApplet.abs(PApplet.round(yDiff / 2.f));
		int bottomBorder = PApplet.abs(PApplet.round(yDiff / 2.f));
		int leftBorder = PApplet.abs(PApplet.round(xDiff / 2.f));
		int rightBorder = PApplet.abs(PApplet.round(xDiff / 2.f));
		
		boolean error = false;

		if(p.ml.debugSettings.stitching)
			System.out.println(" topBorder:"+topBorder+" bottomBorder:"+bottomBorder+" leftBorder:"+leftBorder+" rightBorder:"+rightBorder);

		Mat image = new Mat( src );
		if (image.empty()) 
		{
			if(p.ml.debugSettings.stitching)
				System.out.println(" Error reading image...");
			error = true;
		}
		
		if(!error)
		{
			Mat resized = new Mat(image.rows() + top + bottom, image.cols() + right + left, image.depth());
			
			org.bytedeco.javacpp.opencv_core.copyMakeBorder(image, resized, topBorder, bottomBorder, leftBorder, rightBorder, 
					org.bytedeco.javacpp.opencv_core.BORDER_CONSTANT, 
					new org.bytedeco.javacpp.opencv_core.Scalar((double)0.f));

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
	 * Combine two panoramas into one, blending textures together and moving the location of the second to be identical to the first's
	 * @param first First panorama
	 * @param second Second panorama
	 * @return Combined panorama
	 */
	public WMV_Panorama combinePanoramas(WMV_Panorama first, WMV_Panorama second)
	{
		System.out.println("Combining panoramas...");
		Mat blended = linearBlend(  new Mat(pImageToIplImage(first.texture)), new Mat(pImageToIplImage(second.texture)));
		PImage newTexture = iplImageToPImage( new IplImage(blended) );
		WMV_Panorama newPano = first;
		first.texture = newTexture;
		return newPano;
	}
	
	/**
	 * Use linear blend operator on two Mat images of same size
	 * @param image1 First image
	 * @param image2 Second image
	 * @return Blended image
	 */
	private Mat linearBlend(Mat image1, Mat image2)
	{
		Mat result = image1;
		
		if (image1.empty() || image2.empty()) 
		{
			System.out.println("Blending Error... one or both images are NULL!");
			System.exit(0);
		}
		
		double alpha = 0.5; 
		double beta; 

		alpha = 0.5; 

		beta = ( 1.0 - alpha );
		org.bytedeco.javacpp.opencv_core.addWeighted( image1, alpha, image2, beta, 0.0, result);

		return result;
//		org.bytedeco.javacpp.opencv_imgcodecs.imwrite(sketchPath("")+"/output.jpg", dst);
	}
	
	/**
	 * Convert PImage to IplImage
	 * @param img Image source
	 * @return The IplImage
	 */
	IplImage pImageToIplImage( PImage img )
	{
	  BufferedImage bufferedImage = pImageToBufferedImage(img);   
	  return bufferedImageToIplImage(bufferedImage);              
	}

	/**
	 * Convert IplImage to PImage 
	 * @param img Image source
	 * @return The PImage
	 */
	PImage iplImageToPImage ( IplImage img )                      
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
	public BufferedImage pImageToBufferedImage( PImage img )           
	{
	  PGraphics pg = p.ml.createGraphics(img.width, img.height, PApplet.JAVA2D);
	  pg.colorMode(PApplet.RGB);

	  pg.beginDraw();
	  pg.image(img, 0, 0);
	  pg.endDraw();

	  BufferedImageBuilder bufImgBuilder = new BufferedImageBuilder();
	  int type = (img.format == PApplet.RGB) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
	  BufferedImage buf = bufImgBuilder.bufferImage((java.awt.Image)pg.image, type);

	  return buf;
	}

	/**
	 * Convert BufferedImage to IplImage
	 * @param img Image source
	 * @return The IplImage
	 */
	IplImage bufferedImageToIplImage(BufferedImage img) 
	{     
	  ToIplImage iplConverter = new OpenCVFrameConverter.ToIplImage();
	  Java2DFrameConverter java2dConverter = new Java2DFrameConverter();
	  Java2DFrameConverter.copy( java2dConverter.convert(img), img, (double)1, true, new Rectangle(img.getWidth(), img.getHeight()) );

	  IplImage iplImage = iplConverter.convert(java2dConverter.convert(img));
	  org.bytedeco.javacpp.opencv_core.cvMixChannels(iplImage, 1, iplImage, 1, new int[] {0,0, 1,1, 2,2, 3,3}, 4); 	// -- Needed?

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

	private class BufferedImageBuilder {
//	  private static final int DEFAULT_IMAGE_TYPE = BufferedImage.TYPE_INT_RGB;

//	  public BufferedImage bufferImage(Image image) {
//	    return bufferImage(image, DEFAULT_IMAGE_TYPE);
//	  }

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

