import ij.ImagePlus;
import ij.process.AutoThresholder;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

import java.awt.Color;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;


public class ImageHistogram {
	private static final int HSB=0, RGB=1, LAB=2, YUV=3;
	
	// by default, set the lower threshold so most of the image is black
	private static double THRESHOLD_PERCENTAGE = 0.98;
	
	private int[] histogram;
	private Color[] hColors;
	private int hmax;
	private int minThreshold, maxThreshold;
	public int getMinThreshold() {
		return minThreshold;
	}

	public void setMinThreshold(int minThreshold) {
		this.minThreshold = minThreshold;
	}

	public int getMaxThreshold() {
		return maxThreshold;
	}

	public void setMaxThreshold(int maxThreshold) {
		this.maxThreshold = maxThreshold;
	}

	private int colorSpace = HSB;
	
	public ImageHistogram(ImagePlus imp, int j) {
		ImageProcessor ip = imp.getProcessor();
		ImageStatistics stats = ImageStatistics.getStatistics(ip, ij.measure.Measurements.AREA + ij.measure.Measurements.MODE, null);
		int maxCount2 = 0;
		histogram = stats.histogram;
		for (int i = 0; i < stats.nBins; i++)
			if ((histogram[i] > maxCount2) ) maxCount2 = histogram[i];

		hmax = (int)(maxCount2 * 1.15);//GL was 1.5
		ColorModel cm = ip.getColorModel();
		if (!(cm instanceof IndexColorModel))
			return;
		IndexColorModel icm = (IndexColorModel)cm;
		int mapSize = icm.getMapSize();
		if (mapSize!=256)
			return;
		byte[] r = new byte[256];
		byte[] g = new byte[256];
		byte[] b = new byte[256];
		icm.getReds(r);
		icm.getGreens(g);
		icm.getBlues(b);
		hColors = new Color[256];

		if (colorSpace==RGB){
			if (j==0){
				for (int i=0; i<256; i++)
					hColors[i] = new Color(i&255, 0&255, 0&255);
				}
			else if (j==1){
				for (int i=0; i<256; i++)
					hColors[i] = new Color(0&255, i&255, 0&255);
			}
			else if (j==2){
				for (int i=0; i<256; i++)
					hColors[i] = new Color(0&255, 0&255, i&255);
			}
		}
		else if (colorSpace==HSB){
			if (j==0){
				for (int i=0; i<256; i++)
					hColors[i] = new Color(r[i]&255, g[i]&255, b[i]&255);
			}
			else if (j==1){
				for (int i=0; i<256; i++)
					hColors[i] = new Color(255&255, 255-i&255, 255-i&255);
					//hColors[i] = new Color(192-i/4&255, 192+i/4&255, 192-i/4&255);
			}
			else if (j==2){
				for (int i=0; i<256; i++)
					//hColors[i] = new Color(i&255, i&255, 0&255);
					hColors[i] = new Color(i&255, i&255, i&255);
			}
		}
		else if (colorSpace==LAB){
			if (j==0){
				for (int i=0; i<256; i++)
					hColors[i] = new Color(i&255, i&255, i&255);
			}
			else if (j==1){
				for (int i=0; i<256; i++)
					hColors[i] = new Color(i&255, 255-i&255, 0&255);
			}
			else if (j==2){
				for (int i=0; i<256; i++)
					hColors[i] = new Color(i&255, i&255, 255-i&255);
			}
		}
		else if (colorSpace==YUV){
			if (j==0){
				for (int i=0; i<256; i++)
					hColors[i] = new Color(i&255, i&255, i&255);
			}
			else if (j==1){
				for (int i=0; i<256; i++)
					hColors[i] = new Color((int)(36+(255-i)/1.4)&255, 255-i&255, i&255);
			}
			else if (j==2){
				for (int i=0; i<256; i++)
					hColors[i] = new Color(i&255, 255-i&255, (int)(83+(255-i)/2.87)&255);
			}
		}
		
		//AutoThresholder thresholder = new AutoThresholder();
		//int threshold = thresholder.getThreshold("Default", histogram);
		int total=0, i=0;
		for(i=0; i < histogram.length ; i++)
			total += histogram[i];
		double threshold = THRESHOLD_PERCENTAGE * total;
		total=0;
		for(i=0; i < histogram.length ; i++){
			total += histogram[i];
			if (total >= threshold)
				break;
		}
		minThreshold = i;
		maxThreshold = Math.min(i +20, 255);
		
		System.err.println("Threshold: " + threshold + ". I:" + i);
		
	}

	public Color getColor(int i){
		return hColors[i];
	}
	
	public int get(int i){
		return histogram[i];
	}
	
	public int getMax(){
		return hmax;
	}
	
}
