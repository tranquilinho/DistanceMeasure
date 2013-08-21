import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * 
 * @author Jesus Cuenca
 * @see ij.plugin.Thresholder and ij.plugin.frame.ThresholdAdjuster
 */
public class ThresholdChangeListener implements ChangeListener, AdjustmentListener {
	private ChannelHistogram slider;
	
	public ChannelHistogram getSlider() {
		return slider;
	}

	public void setSlider(ChannelHistogram slider) {
		this.slider = slider;
	}

	public int getChannel() {
		if(getSlider()!= null)
			return getSlider().getNumber();
		return 0;
	}

	ThresholdChangeListener(ChannelHistogram slider) {
		this.slider=slider;
	}

	protected double defaultMaxThreshold=255, defaultMinThreshold=0;
	
	/**
	 * When the user changes channel in the Hyperstack viewer, this class is notified through this method
	 */
	public void adjustmentValueChanged(AdjustmentEvent e) {
		if(getImagePlus() != null)
			try{
				applyCurrentThreshold();
			}catch (NullPointerException ex){
				// sometimes the call to applyCurrentThreshold fails for channel #2 (with a null pointer exception),
				// hence the need to catch it and repeat the call
				applyCurrentThreshold();
			}
	}
	
	/**
	 * There was a change in one of the threshold sliders, which triggers this stateChanged method
	 */
	public void stateChanged(ChangeEvent event){
		if(updateThreshold(event))
			setThresholdForCurrentChannel(getLowThreshold(), getHighThreshold());
	}
		// System.err.println("Red low"+((JSlider)arg0.getSource()).getValue());
	
	private JSlider getSlider(ChangeEvent event){
		return (JSlider)event.getSource();
	}
	
	protected int getValue(ChangeEvent event){
			return (getSlider(event).getValue());
	}
	
	protected boolean isValueAdjusting(ChangeEvent event){
		boolean result= false;
		try{
			result = getSlider(event).getValueIsAdjusting();
		}catch (ClassCastException ex){
			result = false;
		}
		return result;
	}
	

	private ImagePlus getImagePlus(){
		if(WindowManager.getCurrentWindow() == null)
			return null;
		else
			return WindowManager.getCurrentImage();
	}
	
	protected boolean updateThreshold(ChangeEvent event){
		return (!isValueAdjusting(event)) && getImagePlus() != null;
	}
	
	protected double getHighThreshold(){
		if(getSlider() != null)	
			return getSlider().getHigh();
		else
			return defaultMaxThreshold;
	}

	protected double getLowThreshold(){
		if(getSlider() != null)	
			return getSlider().getLow();
		else
			return defaultMinThreshold;
	}
	
	protected void setThresholdForCurrentChannel(double low, double high){
		if(getImagePlus() != null){
			int currentChannel = getImagePlus().getChannel();
			// int currentChannel = getSlider().getNumber();
			if(getChannel() == currentChannel){
				if(getSlider().isSelected()){
					IJ.setThreshold(low,high,Distance_Measure.THRESHOLD_METHOD);
					// IJ.setThreshold(getImagePlus(), low, high, "black");
					// getImageProcessor().setThreshold(min, max, ImageProcessor.NO_LUT_UPDATE);
					// getImagePlus().updateAndDraw();
				}else{
					IJ.resetThreshold();
				}
					
			}
			// System.err.println("Channel (this/current):" + getChannel() + "/" + currentChannel + ", high:" + high + ", low:" + low);
		}
	}
	
	protected void applyCurrentThreshold(){
		setThresholdForCurrentChannel(getLowThreshold(), getHighThreshold());
	}
	
}
