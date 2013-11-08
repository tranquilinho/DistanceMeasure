
import ij.plugin.frame.PlugInFrame;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Juanjo Vega
 */
public class About_Distance_Measure extends PlugInFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1563781228522186516L;

	public About_Distance_Measure() {
        super("About Distance Measure...");

        JLabel jlIcon = null;
        try{
        	jlIcon = new JLabel(new ImageIcon(getClass().getResource("/resources/I2PC.png")));
        }catch (Exception ex){
        	
        }
        JScrollPane jspText = new JScrollPane();
        JTextPane jtpAbout = new JTextPane();

        jtpAbout.setContentType("text/html");
        jtpAbout.setEditable(false);
        jtpAbout.setText("<html>"
                + "Jesus Cuenca (<b>jcuenca@cnb.csic.es</b>), CO Sanchez Sorzano (<b>coss@cnb.csic.es</b>), Juanjo Vega (<b>juanjo.vega@gmail.com</b>)<br>"
                + "<hr>"
                + "Instruct Image Processing Center.<br>"
                + "Biocomputing Unit.<br>"
                + "National Center for Biotechnology (CNB/CSIC).<br>"
                + "Madrid. Version 1.0.</html>");
        jspText.setViewportView(jtpAbout);

        if(jlIcon != null)
        	add(jlIcon, java.awt.BorderLayout.WEST);
        add(jspText, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    @Override
    public void run(String arg) {
        setVisible(true);
    }
}
