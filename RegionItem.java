import java.text.DecimalFormat;

import ij.ImagePlus;
import ij.gui.Roi;


public class RegionItem extends ThumbnailItem {
	Roi roi;


	ImagePlus red,green;
    private double avgDistance;
    private double [] distance;
    private Histogram histogram;
	
	public Histogram getHistogram() {
		return histogram;
	}

	public void setHistogram(Histogram histogram) {
		this.histogram = histogram;
	}

	public double[] getDistance() {
		return distance;
	}

	public void setDistance(double[] distance) {
		this.distance = distance;
	}

	public ImagePlus getRed() {
		return red;
	}
	
	public ImagePlus getGreen(){
		return green;
	}
	
	public Roi getRoi() {
		return roi;
	}

	public void setRoi(Roi roi) {
		this.roi = roi;
	}

	public RegionItem(ImagePlus red,ImagePlus green, ImagePlus thumbnail, Roi roi){
		super(thumbnail);
		this.roi=roi;
		this.red = red;
		this.green = green;
	}
	
	public String toString() {
		DecimalFormat df = new DecimalFormat("0.000000"); 
		return ("x=" + getX() + ", y="+ getY() + " (" + getWidth() + "x" + getHeight() + "). <b>Dist.: " + df.format(getAvgDistance()) + "</b> micron");
	}
	
	public int getX(){
		return roi.getBounds().x;
	}
	
	public int getY(){
		return roi.getBounds().y;
	}
	
	public int getWidth(){
		return roi.getBounds().width;
	}
	
	public int getHeight(){
		return roi.getBounds().height;
	}
	
    public double getAvgDistance() {
        return avgDistance;
    }

    public void setAvgDistance(double distance) {
		this.avgDistance = distance;
	}
}
