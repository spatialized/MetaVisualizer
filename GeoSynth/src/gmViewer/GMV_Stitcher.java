package gmViewer;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
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
 */
public class GMV_Stitcher 
{
	private Stitcher stitcher;
	
	private final boolean try_use_gpu = true;

	GeoSynth p;
	GMV_Stitcher(GeoSynth parent)
	{
		p = parent;
		
		stitcher = Stitcher.createDefault(try_use_gpu);

		stitcher.setRegistrationResol(-1); /// 0.6
		stitcher.setSeamEstimationResol(-1);   /// 0.1
		stitcher.setCompositingResol(-1);   //1
		stitcher.setPanoConfidenceThresh(-1);   //1

//		stitcher.setWaveCorrection(true);
//		stitcher.setWaveCorrectKind((org.bytedeco.javacpp.opencv_imgcodecs.detail)::WAVE_CORRECT_HORIZ;
	}
	
	/**
	 * Stitch spherical panorama from images
	 * @param library
	 */
	public PImage stitch(String library, IntList imageList)
	{
		
		Mat panorama = new Mat();				// Panoramic image result
//		MatVector complete = new MatVector();		

		boolean success = false, impossible = false;		
		boolean reduce = false;				// Reduce images to try to stitch
		int count = 0;
		
		// Prevent fatal error
		while(imageList.size() > 20)
			imageList.remove(imageList.size()-1);

		String[] images = getImageNames(imageList);				

		while(!success || impossible)
		{
			if(reduce)		// Use orientation to exclude!
			{
				if(imageList.size() > 2)
				{
					imageList.remove(imageList.size()-1);
					images = getImageNames(imageList);
				}
				else
					impossible = true;
			}
			
			if(!impossible)
			{
//				@SuppressWarnings("resource")
				MatVector imgs = new MatVector();		
				imgs = getMatVectorImages(images);

//				if(count == 0)
//					complete = imgs;						// Save full image list

				if(!imgs.isNull())
				{
					if(p.debug.stitching)
						PApplet.println("Attempting to stitch "+imgs.size()+" images...");

					if(stitcher == null)
						PApplet.println("Stitcher is NULL!!!");
					
					Mat pano = new Mat();
					int status = stitcher.stitch(imgs, pano);

					if (status == Stitcher.OK) 
					{
						success = true;
						impossible = false;
						panorama = pano;
					}
					else
					{
						if(p.debug.stitching)
							System.out.println("Error code " + status + " while stitching, will try again...");
						if(status == 3)		// Not enough overlap 
							reduce = true;
						else
							impossible = true;
					}

					imgs.close();
					if(count++ > 100) break;		// Avoid infinite while loop
				}
				else
				{
					PApplet.println("Couldn't stitch images... imgs == NULL!");
					break;
				}
			}
			else break;
		}
	
		PImage result = p.createImage(0,0,PApplet.RGB);

		if(success)
		{
			// Testing
			if(p.debug.stitching)
			{
				String output_name = p.stitchingPath+"stitched.jpg";
				org.bytedeco.javacpp.opencv_imgcodecs.imwrite(output_name, panorama);
				System.out.println(""+images.length+" images stitched, output result to: " + output_name);
			}

			IplImage img = new IplImage(panorama);
			if(img != null)
				result = iplImageToPImage(img);

			if(img == null)
				PApplet.println("Panorama is null...");
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
	
	/* convert IplImage to PImage */
	PImage iplImageToPImage ( IplImage iplImg ) 
	{
	  java.awt.image.BufferedImage bImg = IplImageToBufferedImage(iplImg);
	  PImage img = new PImage( bImg.getWidth(), bImg.getHeight(), PApplet.ARGB );
	  bImg.getRGB( 0, 0, img.width, img.height, img.pixels, 0, img.width );
	  img.updatePixels();
	  return img;
	}

	IplImage toIplImage(BufferedImage bufImage) {

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

