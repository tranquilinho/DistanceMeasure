import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SpringLayout;


/**
 *
 * @author Juanjo Vega
 */
public class RegionListCellRenderer extends JPanel implements ListCellRenderer {

	private static final long serialVersionUID = -1043924962165633428L;
	private JLabel jlIndex = new JLabel();
    private JLabel jlThumbnail = new JLabel();
    private JLabel jlInfo = new JLabel();

    public RegionListCellRenderer() {
        super();

        jlIndex.setHorizontalTextPosition(JLabel.LEFT);
        jlInfo.setHorizontalTextPosition(JLabel.LEFT);

        jlIndex.setVerticalTextPosition(JLabel.CENTER);
        jlInfo.setVerticalTextPosition(JLabel.CENTER);

        SpringLayout layout = new SpringLayout();
        setLayout(layout);

        add(jlIndex);
        add(jlThumbnail);
        add(jlInfo);

        // Horizontal layout.
        layout.putConstraint(SpringLayout.WEST, jlIndex, 5, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.WEST, jlThumbnail, 5, SpringLayout.EAST, jlIndex);
        layout.putConstraint(SpringLayout.WEST, jlInfo, 5, SpringLayout.EAST, jlThumbnail);

        layout.putConstraint(SpringLayout.SOUTH, this, 0, SpringLayout.SOUTH, jlThumbnail);

        // Vertical centering according to icon.
        layout.putConstraint(SpringLayout.VERTICAL_CENTER, jlIndex, 0, SpringLayout.VERTICAL_CENTER, jlThumbnail);
        layout.putConstraint(SpringLayout.VERTICAL_CENTER, jlInfo, 0, SpringLayout.VERTICAL_CENTER, jlThumbnail);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        ThumbnailItem item = (ThumbnailItem) value;

        jlIndex.setText("<html><b>" + index/*nformatter.format(index)*/ + ": </b></html>");
        jlThumbnail.setIcon(new ImageIcon(item.getThumbnail()));
        jlInfo.setText("<html>" + item.toString() + "</html>");

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        return this;
    }
}
