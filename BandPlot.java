import ij.measure.Measurements;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JComponent;

import slider.RangeSlider;

/**
 * @author Jesus Cuenca
 * extracted from ImageJ (it's a private inner class...)
 * @see ij.gui.HistogramWindow, ij.plugin.frame.ColorThresholder
 *
 */
// No need to use all the space available for the bands (since the histograms
// are limited to 256 bars, and the panel width is 256. Maybe in the future...
class BandPlot extends JComponent implements Measurements, MouseListener {	
	private static final long serialVersionUID = -2625959885566410861L;
	private static int LOWTHRESHOLD=0,HIGHTHRESHOLD=255;
	final int WIDTH = 256, HEIGHT=64;
	
	private RangeSlider thresholds;

	private double minHue = 0, maxHue = 255;
	// minSat=0, minBri=0;
	// private double  maxSat= 255, maxBri=255;

	private Image os=null;
	private Graphics osg;

	private ImageHistogram histogram;
	
	public void setHistogram(ImageHistogram histogram){
		this.histogram = histogram;
		os=null;
	}
	
	public void setThresholds(RangeSlider t){
		thresholds = t;
	}
	
	public int getLowThreshold() {
		if(thresholds!=null)
			return thresholds.getValue();
		return LOWTHRESHOLD;
	}

	public int getHighThreshold() {
		if(thresholds != null)
			return thresholds.getUpperValue();
		return HIGHTHRESHOLD;
	}

	public BandPlot() {
		setBackground(Color.YELLOW);
		addMouseListener(this);
		//setSize(WIDTH+1, HEIGHT+1);
	}

	public void paintComponent(Graphics g ) {
		super.paintChildren(g);
		int hHist=0;
		if (histogram!=null) {
			if (os==null) {
				os = createImage(getWidth(),getHeight());
				osg = os.getGraphics();
				osg.setColor(new Color(140,152,144));
				osg.fillRect(0, 0, getWidth(), getHeight());
				for (int i = 0; i < WIDTH; i++) {
					Color c=histogram.getColor(i);
					if (c !=null) osg.setColor(c);
					hHist=getHeight() - ((int)(getHeight() * histogram.get(i))/histogram.getMax())-6;
					osg.drawLine(i, getHeight(), i, hHist);
					osg.setColor(Color.black);
					osg.drawLine(i, hHist, i, hHist);
				}
				osg.dispose();
			}
			if (os!=null) g.drawImage(os, 0, 0, this);
		} else {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		g.setColor(Color.black);
		//g.drawLine(0, getHeight() -6, 256, getHeight()-6);
		g.drawRect(0, 0, getWidth(), getHeight());
		g.drawRect((int)minHue, 1, (int)(maxHue-minHue), getHeight()-7);
		g.setColor(Color.gray);
		g.drawLine(getLowThreshold(), 5, getLowThreshold(), getHeight() -5);
		g.drawLine(getHighThreshold(), 5, getHighThreshold(), getHeight() -5);
	}

	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
} // BandPlot class

