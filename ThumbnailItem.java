
import ij.ImagePlus;
import ij.process.ImageProcessor;
import java.awt.Image;
import java.awt.image.BufferedImage;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Juanjo Vega, Jesus Cuenca
 */
public class ThumbnailItem {

    private final static int SIZE = 50;
    private Image thumbnail;

    public ThumbnailItem(ImagePlus imp) {
        thumbnail = createThumbnail(imp);
    }

    private Image createThumbnail(ImagePlus imp) {
        // Scales image.
        int W = imp.getWidth(), H = imp.getHeight();
        int w = W >= H ? SIZE : -1;
        int h = W < H ? SIZE : -1;

        ImagePlus toThumbnail = imp;
        if(imp.getNSlices() > 1)
        	toThumbnail=getSlice(imp,imp.getCurrentSlice());
        	
        Image i = toThumbnail.getImage();
        
        if (W > SIZE || H > SIZE) {
            i = i.getScaledInstance(w, h, Image.SCALE_FAST);
        }

        // Draws it centered in a transparent image.
        BufferedImage bimage = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        w = i.getWidth(null);
        h = i.getHeight(null);
        int x = SIZE / 2 - w / 2;
        int y = SIZE / 2 - h / 2;
        bimage.getGraphics().drawImage(i, x, y, null);

        return bimage;
    }

    public Image getThumbnail() {
        return thumbnail;
    }
    
    public ImagePlus getSlice(ImagePlus imp, int z){
		int n1 = imp.getStackIndex(1, z, 1);
		ImageProcessor sliceIp = imp.getStack().getProcessor(n1);
		ImageProcessor ip=imp.getProcessor();

		sliceIp.setThreshold(ip.getMinThreshold(), ip.getMaxThreshold(), Distance_Measure.getThresholdMode());
		ImagePlus result= new ImagePlus("Slice",sliceIp);
		return result;
    }

}
