import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.RowSpec;
import java.awt.GridLayout;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Hashtable;
import jgc.slider.RangeSlider;

/**
 * 
 * @author Jesus Cuenca
 * @see ij.plugin.frame.ColorThresholder
 */

// TODO: decouple histograms?
public class ChannelHistogram extends JComponent implements ChangeListener {

	private static final long serialVersionUID = -2043055058095799164L;
	JCheckBox chckbxSelect;
	JComboBox comboChannelNumber;
	RangeSlider slider;
	BandPlot histogramView;

	private Hashtable<Integer, ImageHistogram> histograms;

	public BandPlot getHistogramView() {
		return histogramView;
	}

	public void setHistogram(ImageHistogram h) {
		if (h != null) {
			getHistogramView().setHistogram(h);
			slider.setValue(h.getMinThreshold());
			slider.setUpperValue(h.getMaxThreshold());
			getHistogramView().repaint();
		}
	}

	public void setNumber(int number) {
		comboChannelNumber.setSelectedIndex(number - 1);
	}

	public int getNumber() {
		return Integer.parseInt((String) comboChannelNumber.getSelectedItem());
	}

	public int getLow() {
		return slider.getValue();
	}

	public int getHigh() {
		return slider.getUpperValue();
	}

	public void setSelected(boolean s) {
		chckbxSelect.setSelected(s);
	}

	public boolean isSelected() {
		return chckbxSelect.isSelected();
	}

	public void addChangeListener(ChangeListener l) {
		slider.addChangeListener(l);
		chckbxSelect.addChangeListener(l);
	}

	/**
	 * Create the panel.
	 */
	public ChannelHistogram() {

		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("125px"),
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("125px"),
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC, }, new RowSpec[] {
				FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("50px"), FormFactory.RELATED_GAP_ROWSPEC, }));

		comboChannelNumber = new JComboBox();
		comboChannelNumber.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				String channelString = (String) cb.getSelectedItem();
				int channelNumber = Integer.parseInt(channelString);
				// System.err.println(""+channelNumber+","+getNumber());
				// if(channelNumber != getNumber()){
				setHistogram(getHistogram(channelNumber));
				// }
			}
		});
		comboChannelNumber.setModel(new DefaultComboBoxModel(new String[] {
				"1", "2", "3", "4", "5", "6", "7", "8", "9" }));
		add(comboChannelNumber, "1, 1, center, center");

		chckbxSelect = new JCheckBox("Apply");
		add(chckbxSelect, "3, 1, center, center");

		slider = new RangeSlider();
		slider.setMaximum(255);
		slider.setUpperValue(255);
		slider.setValue(0);
		add(slider, "5, 1, 3, 1, fill, fill");

		histogramView = new BandPlot();
		histogramView.setBorder(null);
		histogramView.setThresholds(slider);
		JPanel histogramPanel = new JPanel();
		histogramPanel.setBorder(null);
		histogramPanel.setLayout(new GridLayout(0, 1, 0, 0));
		histogramPanel.add(histogramView);
		add(histogramPanel, "5, 3, 3, 1, fill, fill");

		slider.addChangeListener(this);

	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		if (histogramView != null)
			histogramView.repaint();
		// histogramView.repaint(histogramView.getBounds());

	}

	private ImageHistogram getHistogram(int channel) {
		if (histograms == null)
			return null;
		return histograms.get(new Integer(channel));
	}

	public void setHistograms(Hashtable<Integer, ImageHistogram> histograms) {
		this.histograms = histograms;
		setHistogram(getHistogram(getNumber()));
	}

	public void setEnabled(boolean e) {
		chckbxSelect.setEnabled(e);
		comboChannelNumber.setEnabled(e);
		slider.setEnabled(e);
	}
}
