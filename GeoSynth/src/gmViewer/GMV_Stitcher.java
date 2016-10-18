package gmViewer;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import javax.imageio.*;

import org.bytedeco.javacv.*;
import org.bytedeco.javacv.OpenCVFrameConverter.*;

import processing.core.PApplet;
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
	public PImage stitch(String library, IntList imageList, int clusterID, int segmentID)
	{
		Mat panorama = new Mat();				// Panoramic image result
//		MatVector complete = new MatVector();		

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
						panorama = pano;
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

			IplImage img = new IplImage(panorama);
			
			if(img != null)
				result = iplImageToPImage(img);
		}
		panorama.close();
		
		return result;
	}
	
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
	 * Get image names from list of image IDs
	 * @param imageList List of image IDs
	 * @return Array of image names
	 */
	private String[] getImageNames(IntList imageList)
	{
		String[] images = new String[imageList.size()];
		
//		PApplet.println("names.length:"+images.length);
//		PApplet.println("images.size():"+imageList.size());
//		PApplet.println("p.images.size():"+p.getCurrentField().images.size());
		
		int count = 0;
		for(int i:imageList)
		{
			images[count] = p.getCurrentField().images.get(i).getFilePath();
			count++;
		}
		
		return images;
	}
	
	/**
	 * Add border to image			-- Need to finish!
	 * @param source
	 * @param topBorder
	 * @param bottomBorder
	 * @param leftBorder
	 * @param rightBorder
	 * @return
	 */
	public PImage addImageBorders(PImage source, int topBorder, int bottomBorder, int leftBorder, int rightBorder)
	{
		boolean error = false;
//		 Mat image = org.bytedeco.javacpp.opencv_imgcodecs.imread(sketchPath("")+"/img.jpg");
		Mat image = new Mat( pImageToIplImage(source) );
		if (image.empty()) 
		{
			if(p.debug.stitching)
				PApplet.println("addImageBorders(): Error reading image...");
			error = true;
		}

		if(!error)
		{
			Mat resized = new Mat(image.rows() + topBorder + bottomBorder, image.cols() + rightBorder + leftBorder, image.depth());


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

			//		  org.bytedeco.javacpp.opencv_imgcodecs.imwrite(sketchPath("")+"/output.jpg", resized);
			if(p.debug.stitching)
				PApplet.println("Finished adding image border... ");
			image.close();
			return iplImageToPImage(new IplImage(resized));
		}
		else
		{
			image.close();
			return null;
		}
	}
	
	IplImage pImageToIplImage( PImage img )
	{
		BufferedImage bufferedImage = pImageToBufferedImage(img);
		return bufferedImageToIplImage(bufferedImage);
	}
	
	PImage iplImageToPImage ( IplImage iplImg ) 
	{
	  java.awt.image.BufferedImage bImg = IplImageToBufferedImage(iplImg);
	  PImage img = new PImage( bImg.getWidth(), bImg.getHeight(), PApplet.ARGB );
	  bImg.getRGB( 0, 0, img.width, img.height, img.pixels, 0, img.width );
	  img.updatePixels();
	  return img;
	}

	public BufferedImage pImageToBufferedImage( PImage img )
	{
		img.loadPixels();
		int type = (img.format == PApplet.RGB) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		BufferedImage image = new BufferedImage(img.width, img.height, type);
		WritableRaster wr = image.getRaster();
		wr.setDataElements(0, 0, img.width, img.height, img.pixels);
		return image;
	}

	IplImage bufferedImageToIplImage(BufferedImage bufImage) {

	  ToIplImage iplConverter = new OpenCVFrameConverter.ToIplImage();
	  Java2DFrameConverter java2dConverter = new Java2DFrameConverter();
	  IplImage iplImage = iplConverter.convert(java2dConverter.convert(bufImage));
	  return iplImage;
	}

	public static BufferedImage IplImageToBufferedImage(IplImage src) {
	  OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
	  Java2DFrameConverter paintConverter = new Java2DFrameConverter();
	  Frame frame = grabberConverter.convert(src);
	  return paintConverter.getBufferedImage(frame, 1);
	}
}

