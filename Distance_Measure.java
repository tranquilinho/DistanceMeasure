import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.Roi;
import ij.gui.ScrollbarWithLabel;
import ij.gui.StackWindow;
import ij.measure.Calibration;
import ij.plugin.frame.PlugInFrame;
import ij.process.ImageProcessor;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import javax.swing.JScrollPane;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import java.awt.BorderLayout;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Vector;
import javax.swing.JProgressBar;
import javax.swing.JCheckBox;

/**
 * 
 * @author Jesus Cuenca
 * @depends: LOCI tools (load LIF, jgoodies...)
 */
/*
 * Basic workflow: open the LIF file, apply threshold to red and green channels,
 * extract regions, merge channels (optional) and finally measure distances for
 * each region. The threshold applies only once at the beginning, so it's enough
 * to save the threshold values once too The original image typically has 4
 * channels and a collection of stacks, so we may think of it as a 5D image (S
 * Series/experiments x 4 channels x N slices x 2D images). In the end for
 * measuring distances, we only need 2 channels of one of the experiments/series
 * - either as a HyperStack, or as 2 different stacks (one for each channel)
 */

// TODO: auto threshold (improve initial values)
public class Distance_Measure extends PlugInFrame {
	private static final long serialVersionUID = 5631154961754515884L;

	private static int FAST_VECTOR_SIZE=1000;
	private static int HISTOGRAM_BINS=10;
	
	private static int DEFAULT_GREEN_CHANNEL = 1, DEFAULT_RED_CHANNEL = 2,
			STACK_INDEX_OF_CHANNEL_SLIDER = 1, MAX_CHANNELS=8;
	// "Red", "Black & White", "Over/Under" or "No Update"
	// @see IJ.setThreshold
	public static String THRESHOLD_METHOD = "Red";
	public static int MEGABYTE = 1024 * 1024;

    private final static Color ROI_COLOR = Color.YELLOW;//new Color(128, 255, 255);
    private final static Color LABEL_COLOR = Color.BLUE;
    private final static Font LARGE_FONT = new Font("SansSerif", Font.PLAIN, 12);
    private final static Font SMALL_FONT = new Font("SansSerif", Font.PLAIN, 9);	
	
	private Hashtable<Integer, ImageHistogram> histograms;
	private Random rng=new Random();
	private AddRegion currentAdd;
	
	private JFrame frmDistanceMeasure;
	ChannelHistogram channelOne,channelTwo;
	JButton btnAddCurrentSelection,btnSave;

	ThresholdChangeListener thresholdListenerOne, thresholdListenerTwo;


	private DefaultListModel listModel = new DefaultListModel();
	private JList regionList = new JList(listModel);
	private RegionListCellRenderer listCellRenderer = new RegionListCellRenderer();
	private JPanel panel;
	private JProgressBar progressBar;
	private JCheckBox chckbxShowAll;
	private JButton btnFastAdd;
	private JButton btnStop;
	
	private class Point3D{
		public int x,y,z;
	}

	@Override
	public void run(String arg) {
		try {
			// Distance_Measure window = new Distance_Measure();
			if (frmDistanceMeasure != null)
				frmDistanceMeasure.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the application.
	 */
	public Distance_Measure() {
		super("Distance Measure");
		histograms = new Hashtable<Integer, ImageHistogram>();
		try {
			initialize();
		} catch (NoClassDefFoundError ex) {
			IJ.error("Problem opening plugin",
					"This plugin needs LOCI tools plugin, please verify it is installed");
			destroyGui();
		}
	}

	private void destroyGui() {
		frmDistanceMeasure.removeAll();
		frmDistanceMeasure = null;
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() throws NoClassDefFoundError {
		frmDistanceMeasure = new JFrame();
		frmDistanceMeasure.setTitle("Distance Measure");
		frmDistanceMeasure.setBounds(100, 100, 509, 603);
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmDistanceMeasure.getContentPane().setLayout(
				new FormLayout(new ColumnSpec[] { ColumnSpec
						.decode("center:default:grow"), }, new RowSpec[] {
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						RowSpec.decode("default:grow"),
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC, }));

		JButton btnImportLif = new JButton("Import LIF...");
		btnImportLif.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
					importLif();
			}
		});
		frmDistanceMeasure.getContentPane().add(btnImportLif,
				"1, 2, center, top");

		JPanel panelThreshold = new JPanel();
		panelThreshold.setBorder(new TitledBorder(new LineBorder(new Color(184,
				207, 229)), "Channels", TitledBorder.LEADING, TitledBorder.TOP,
				null, new Color(51, 51, 51)));
		frmDistanceMeasure.getContentPane().add(panelThreshold,
				"1, 4, fill, top");
		panelThreshold.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC, ColumnSpec.decode("default:grow"),
				FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.LINE_GAP_ROWSPEC, }));

		channelOne = new ChannelHistogram();
		channelOne.setEnabled(false);
		panelThreshold.add(channelOne, "1, 2, 3, 1, fill, fill");

		channelTwo = new ChannelHistogram();
		channelTwo.setEnabled(false);
		panelThreshold.add(channelTwo, "1, 4");

		initChannels();

		JPanel panelRegions = new JPanel();
		panelRegions.setBorder(new TitledBorder(null, "Regions",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		frmDistanceMeasure.getContentPane().add(panelRegions,
				"1, 6, fill, fill");
		panelRegions.setLayout(new BorderLayout(0, 0));

		regionList.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent me) {
				if (me.getClickCount() == 2) {
					RegionItem current = (RegionItem) regionList
							.getSelectedValue();
					StackWindow windowRed = new StackWindow(current.getRed());
					windowRed.setTitle("Red");
					StackWindow windowGreen = new StackWindow(current
							.getGreen());
					windowGreen.setTitle("Green");
					windowRed.pack();
					windowGreen.pack();
				}
			}

			@Override
			public void mousePressed(MouseEvent me) {
			}

			@Override
			public void mouseReleased(MouseEvent me) {
			}

			@Override
			public void mouseEntered(MouseEvent me) {
			}

			@Override
			public void mouseExited(MouseEvent me) {
			}
		});
		regionList.setCellRenderer(listCellRenderer);
		JScrollPane scrollPane = new JScrollPane(regionList);
		panelRegions.add(scrollPane);
		
		panel = new JPanel();
		scrollPane.setColumnHeaderView(panel);
		
				btnAddCurrentSelection = new JButton("Add");
				panel.add(btnAddCurrentSelection);
				btnAddCurrentSelection.setEnabled(false);
				
				btnFastAdd = new JButton("Fast add");
				btnFastAdd.setEnabled(false);
				panel.add(btnFastAdd);
				
				progressBar = new JProgressBar();
				panel.add(progressBar);
				
				chckbxShowAll = new JCheckBox("Show all");
				chckbxShowAll.setEnabled(false);
				chckbxShowAll.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						drawROIs();
					}
				});
				
				btnStop = new JButton("Stop");
				btnStop.setEnabled(false);
				btnStop.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						currentAdd.cancel(true);
					}
				});
				panel.add(btnStop);
				panel.add(chckbxShowAll);
				btnAddCurrentSelection.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							// new AddRegion(false).execute();
							currentAdd = new AddRegion(false);
							currentAdd.execute();
						} catch (OutOfMemoryError err) {
							Runtime runtime = Runtime.getRuntime();
							long total = runtime.totalMemory() / MEGABYTE, free = runtime
									.freeMemory() / MEGABYTE, used = total - free, maxMem = runtime
									.maxMemory() / MEGABYTE;
							IJ.showMessage("Not enough memory ("
									+ maxMem
									+ "MB). Please check ImageJ startup parameters (-xmx)");
							System.err.println("Used:" + used + ". Free: " + free
									+ ". Total: " + total);
							err.printStackTrace();
						} catch (NoSuchElementException ex){
							IJ.showMessage(ex.getMessage());
							ex.printStackTrace();
						}
					}
				});
				
				btnFastAdd.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							(new AddRegion(true)).execute();
						} catch (OutOfMemoryError err) {
							Runtime runtime = Runtime.getRuntime();
							long total = runtime.totalMemory() / MEGABYTE, free = runtime
									.freeMemory() / MEGABYTE, used = total - free, maxMem = runtime
									.maxMemory() / MEGABYTE;
							IJ.showMessage("Not enough memory ("
									+ maxMem
									+ "MB). Please check ImageJ startup parameters (-xmx)");
							System.err.println("Used:" + used + ". Free: " + free
									+ ". Total: " + total);
							err.printStackTrace();
						} catch (NoSuchElementException ex){
							IJ.showMessage(ex.getMessage());
							ex.printStackTrace();
						}

					}
				});

		btnSave = new JButton("Save Regions");
		btnSave.setEnabled(false);
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				save();
			}
		});
		frmDistanceMeasure.getContentPane().add(btnSave, "1, 10, center, top");

	}
	
	class AddRegion extends SwingWorker<String, Object> {
			boolean fast=false;
			public AddRegion(boolean fast){
				this.fast=fast;
			}
			
	       @Override
	       public String doInBackground() {
	           addRegion(fast);
	           return "";
	       }

	       @Override
	       protected void done() {
	           try { 
	               progressBar.setValue(100);
	           } catch (Exception ignore) {
	           }
	       }
	}

	private int getRedChannel(){
		return channelOne.getNumber();
	}

	private int getGreenChannel(){
		return channelTwo.getNumber();
	}	
	
	private void addRegion(boolean fast) throws OutOfMemoryError, NoSuchElementException {
		ImagePlus imp = WindowManager.getCurrentImage();
		
		long startTime = System.nanoTime();

		if (imp != null) {
			final Roi roi = imp.getRoi();
			if (roi != null) {
				if (roi.isArea()) {
					// @see ij.plugin.Resizer

					int redChannelNumber = getRedChannel();
					int greenChannelNumber = getGreenChannel();

					System.err.println("Red channel: " + redChannelNumber + ". Green channel: " + greenChannelNumber);
					/*byte [] pixels = (byte []) imp.getProcessor().getPixels();
					Arrays.sort(pixels);
					System.err.println("Image min " + pixels[0] + " max " + pixels[pixels.length -1]);*/

					ImagePlus red = cropChannel(imp, redChannelNumber, roi);
					ImagePlus green = cropChannel(imp, greenChannelNumber, roi);
					ImagePlus thumbnail = null;
					if (imp.getChannel() == redChannelNumber)
						thumbnail = red;
					else
						thumbnail = green;

					// select the current slice for the thumbnail made in
					// RegionItem
					red.setPosition(1, imp.getSlice(), 1);
					green.setPosition(1, imp.getSlice(), 1);
					RegionItem item = new RegionItem(red, green, thumbnail, roi);
					
					Calibration cal = imp.getCalibration();
					
					double [] distanceInMicron = measure(green, red,cal.pixelHeight,fast);
					long finishTime = System.nanoTime();
					System.err.println("Elapsed time (measure): " + (finishTime - startTime) / 1000000);
					startTime = System.nanoTime();
					
					Histogram h=new Histogram(distanceInMicron,HISTOGRAM_BINS,progressBar,60,20);
					h.fill(distanceInMicron);
					item.setHistogram(h);

					finishTime = System.nanoTime();
					System.err.println("Elapsed time (histogram): " + (finishTime - startTime) / 1000000);
					startTime = System.nanoTime();

					
					double avgDistanceinMicron=robustMean(distanceInMicron);
					
					finishTime = System.nanoTime();
					System.err.println("Elapsed time (robust mean): " + (finishTime - startTime) / 1000000);
					startTime = System.nanoTime();

					
					item.setAvgDistance(avgDistanceinMicron);
					listModel.addElement(item);
					
					btnSave.setEnabled(true);
					chckbxShowAll.setEnabled(true);
				} else {
					IJ.error("ROI is not an area.");
				}
			} else {
				IJ.error("No ROI selected.");
			}
		} else {
			IJ.error("There are no images open.");
		}
		System.err.println();
	}


	
	public static int getThresholdMode() {
		int mode = ImageProcessor.RED_LUT;
		if (Distance_Measure.THRESHOLD_METHOD.indexOf("black") != -1)
			mode = ImageProcessor.BLACK_AND_WHITE_LUT;
		else if (Distance_Measure.THRESHOLD_METHOD.indexOf("over") != -1)
			mode = ImageProcessor.OVER_UNDER_LUT;
		else if (Distance_Measure.THRESHOLD_METHOD.indexOf("no") != -1)
			mode = ImageProcessor.NO_LUT_UPDATE;
		return mode;
	}

	private ImagePlus cropChannel(ImagePlus imp, int channel, Roi roi) {
		Rectangle rect = roi.getBounds();
		int width = rect != null ? rect.width : imp.getWidth();
		int height = rect != null ? rect.height : imp.getHeight();

		ImageStack stack = imp.getStack();
		ImageStack stack2 = new ImageStack(width, height);
		/*double low[] = new double[MAX_CHANNELS];
		double high[] = new double[MAX_CHANNELS];
		low[getRedChannel()] = channelOne.getLow();
		high[getRedChannel()] = channelOne.getHigh();
		low[getGreenChannel()] = channelTwo.getLow();
		high[getGreenChannel()] = channelTwo.getHigh();*/
		
		double low=0.0, high=0.0;
		if(channel == getRedChannel()){
			low= channelOne.getLow();
			high=channelOne.getHigh();
		}else if(channel == getGreenChannel()){
			low= channelTwo.getLow();
			high=channelTwo.getHigh();			
		}

		for (int z = 1; z <= imp.getNSlices(); z++) {
			int n1 = imp.getStackIndex(channel, z, 1);
			ImageProcessor ip = stack.getProcessor(n1);
			
			/*byte [] pixels = (byte []) ip.convertToByte(false).getPixels();
			Arrays.sort(pixels);
			System.err.println("Slice " + z + " min " + pixels[0] + " max "+ pixels[pixels.length - 1]);
			*/
			String label = stack.getSliceLabel(n1);
			ip.setRoi(rect);
			ip = ip.crop();
			stack2.addSlice(label, ip);
		}
		ImagePlus imp2 = imp.createImagePlus();
		imp2.setStack("REGION_" + imp.getTitle(), stack2);
		imp2.setDimensions(2, imp.getNSlices(), 1);
		imp2.getProcessor().setThreshold(low, high,	getThresholdMode());
		return imp2;

	}

	// @deprecated - remove 
	private ImagePlus cropAsHyperstack(ImagePlus imp, Roi roi) {
		Rectangle rect = roi.getBounds();
		int width = rect != null ? rect.width : imp.getWidth();
		int height = rect != null ? rect.height : imp.getHeight();

		ImageStack stack = imp.getStack();
		ImageStack stack2 = new ImageStack(width, height);
		double low[] = new double[MAX_CHANNELS];
		double high[] = new double[MAX_CHANNELS];
		low[getRedChannel()] = channelOne.getLow();
		high[getRedChannel()] = channelOne.getHigh();
		low[getGreenChannel()] = channelTwo.getLow();
		high[getGreenChannel()] = channelTwo.getHigh();

		for (int z = 1; z <= imp.getNSlices(); z++) {
			for (int c = 1; c <= 2; c++) {
				int n1 = imp.getStackIndex(c, z, 1);
				ImageProcessor ip = stack.getProcessor(n1);
				String label = stack.getSliceLabel(n1);
				ip.setRoi(rect);
				ip = ip.crop();
				stack2.addSlice(label, ip);
			}
		}
		ImagePlus imp2 = imp.createImagePlus();
		imp2.setStack("DUP_" + imp.getTitle(), stack2);
		imp2.setDimensions(2, imp.getNSlices(), 1);
		imp2.getProcessor().setThreshold(low[1], high[1], getThresholdMode());
		imp2.setOpenAsHyperStack(true);
		return imp2;

	}

	private void initChannels() {

		thresholdListenerOne = new ThresholdChangeListener(channelOne);
		thresholdListenerTwo = new ThresholdChangeListener(channelTwo);

		channelTwo.setName("Red");
		channelTwo.setNumber(DEFAULT_RED_CHANNEL);
		channelTwo.setSelected(true);
		channelTwo.addChangeListener(thresholdListenerTwo);

		channelOne.setName("Green");
		channelOne.setNumber(DEFAULT_GREEN_CHANNEL);
		channelOne.setSelected(true);
		channelOne.addChangeListener(thresholdListenerOne);
	}

	/**
	 * For the user it's easier to work with a high threshold (that is, only some parts of the cells are marked red)
	 * This means we have to take into account the pixels within the threshold range
	 * @param green
	 * @param red
	 * @param pixelToMicronFactor conversion factor (from pixel to micron)
	 * @return distances in microns
	 * @throws OutOfMemoryError
	 */
	private double [] measure(ImagePlus green, ImagePlus red, double pixelToMicronFactor,boolean fast)throws OutOfMemoryError,NoSuchElementException {
		double progress = 0.0;
		progressBar.setValue((int)progress);
		
		// Remove noise
		int width = green.getProcessor().getWidth();
		int height = green.getProcessor().getHeight();
		int lastZ = green.getNSlices();
		int low[] = new int[MAX_CHANNELS];
		int high[] = new int[MAX_CHANNELS];
		int redChannel = getRedChannel(), greenChannel = getGreenChannel();
		low[redChannel] = channelOne.getLow();
		high[redChannel] = channelOne.getHigh();
		low[greenChannel] = channelTwo.getLow();
		high[greenChannel] = channelTwo.getHigh();

		byte INCLUDE= (byte) 255, EXCLUDE= (byte) 0;
		int maxGreen=0,maxRed=0,minGreen=255,minRed=255;
		
		for (int z = 1; z <= lastZ; z++) {
			green.setSlice(z);
			red.setSlice(z);

			byte[] greenPixels = (byte[]) green.getProcessor()
					.convertToByte(false).getPixels();
			byte[] redPixels = (byte[]) red.getProcessor().convertToByte(false)
					.getPixels();

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					// use getPixel since ImageJ stores pixels in bytes from -128 to 128 (see ByteProcessor)
					// hence accessing directly would hide values greater than 127 as negatives
					int pixel = green.getProcessor().getPixel(x, y); // greenPixels[y * width + x] ;
					if (pixel >= low[greenChannel]
							&& pixel <= high[greenChannel])
						greenPixels[y * width + x] = INCLUDE;
					else
						greenPixels[y * width + x] = EXCLUDE;
					
					maxGreen=Math.max(maxGreen, pixel);
					minGreen=Math.min(minGreen,pixel);
					
					pixel = red.getProcessor().getPixel(x,y); //redPixels[y * width + x] ;
					if (pixel >= low[redChannel] && pixel <= high[redChannel])
						redPixels[y * width + x] = INCLUDE;
					else
						redPixels[y * width + x] = EXCLUDE;
					
					maxRed=Math.max(maxRed, pixel);
					minRed=Math.min(minRed,pixel);
				}
			}
			green.getProcessor().convertToByte(false).setPixels(greenPixels);
			red.getProcessor().convertToByte(false).setPixels(redPixels);
			green.getProcessor().convertToByte(false).erode();
			red.getProcessor().convertToByte(false).erode();
		
			// This stage represents 20% of progress
			progress = (z/lastZ)*20;
			progressBar.setValue((int)progress);
		}

		// green.show();
		// red.show();
		System.err.println("Maximum - green: " + maxGreen + ", red: " + maxRed + ". Minimum - green: " + minGreen + ", red: " + minRed);

		// Keep list of green and red points
		Vector<Point3D> greenPoints = new Vector<Point3D>(25000);
		Vector<Point3D> redPoints = new Vector<Point3D>(25000);
		int Ngreen = 0, Nred = 0;
		for (int z = 1; z < lastZ; z++) {
			green.setSlice(z);
			red.setSlice(z);

			byte[] greenPixels = (byte[]) green.getProcessor()
					.convertToByte(false).getPixels();
			byte[] redPixels = (byte[]) red.getProcessor().convertToByte(false)
					.getPixels();
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					byte pixel = greenPixels[y * width + x];
					if (pixel == INCLUDE) {
						//int[] point = new int[3];
						//point[0] = x;
						// point[1] = y;
						// point[2] = z;
						Point3D p= new Point3D();
						p.x=x; p.y=y; p.z= z;
						greenPoints.add(p);
						Ngreen++;
					}
					pixel = redPixels[y * width + x];
					if (pixel == INCLUDE) {
						// int[] point = new int[3];
						// point[0] = x;
						// point[1] = y;
						// point[2] = z;
						Point3D p= new Point3D();
						p.x=x; p.y=y; p.z= z;
						redPoints.add(p);
						Nred++;
					}
				}
				if (Thread.currentThread().isInterrupted())
					return null;
			}
			
			// This stage represents 20% of progress
			progress = 25 + (z/lastZ)*20;
			progressBar.setValue((int)progress);
			
		}

		String errorTag=null;
		if(Nred == 0)
			errorTag = "red";
		if(Ngreen == 0)
			errorTag = "green";
		
		if(errorTag != null)
			throw new NoSuchElementException("Zero " + errorTag + " points found");
		
		System.err.println("Red " + Nred + ", green "+ Ngreen);
		
	    List<Integer> redIndexes,greenIndexes;
	    Iterator<Integer> redIterator=null,greenIterator=null;
	    int redNeeded=Nred, greenNeeded= Ngreen;
	    
		if(fast){
			redNeeded = Math.min(Nred, FAST_VECTOR_SIZE);
			redIndexes=randomSequence(Nred, redNeeded);
			redIterator=redIndexes.iterator();
			greenNeeded = Math.min(Ngreen, FAST_VECTOR_SIZE);
		}
		
		double[] distanceInMicron = new double[redNeeded * greenNeeded];
		
	    int last=0;
	    
		for (int i = 0; i < redNeeded; i++) {
			if (Thread.currentThread().isInterrupted())
				return null;
			int redIndex=i;
			if(fast)
				redIndex=redIterator.next();
			
			Point3D redPoint = redPoints.get(redIndex);
			
			if(fast){
				greenIndexes=randomSequence(Ngreen, greenNeeded);
				greenIterator=greenIndexes.iterator();
			}
			
			for (int j = 0; j < greenNeeded; j++) {
				int greenIndex=j;
				if(fast)
					greenIndex=greenIterator.next();
				
				Point3D greenPoint = greenPoints.get(greenIndex);
				
				int dx = redPoint.x - greenPoint.x;
				int dy = redPoint.y - greenPoint.y;
				int dz = redPoint.z - greenPoint.z;
				
				distanceInMicron[last] = Math.sqrt(dx * dx + dy * dy + dz	* dz) * pixelToMicronFactor;
				
				last++;
			}
			
			// This stage represents the last 50% of progress
			progress = 40 + (i*20.0/Nred);
			progressBar.setValue((int)progress);
		}
		
		System.err.println("Iterations: " + last);
		
		return distanceInMicron;
	}
	
	/**
	 * @param max
	 * @param numbersNeeded
	 * @return sequence of unique random numbers, in range 0..max
	 */
	private List<Integer> randomSequence(int max, int numbersNeeded){
		List<Integer> generated = new ArrayList<Integer>();
		for (int i = 0; i < numbersNeeded; i++)
		{
		    while(true)
		    {
		        Integer next = rng.nextInt(max);
		        if (!generated.contains(next))
		        {
		            // Done for this iteration
		            generated.add(next);
		            break;
		        }
		    }
		}

		return generated;
	}
	
	private double robustMean(double[] distance){
		
		// QuickSort.iterativeSort(distance);
		// Arrays.sort(distance);
		
		/* Arrays.sort(distance,new Comparator<Double>(){
			int iterations = 0;
			public int compare(Double o1, Double o2) {
				double progress = 90 + (iterations*10.0)/ size; 
				iterations++;
				progressBar.setValue((int)progress);
		        return o2.compareTo(o1);
		    }

		});*/
		
		/*
		QuickSelect qs = new QuickSelect(progressBar,80,20);
		QuickMedian qm = new QuickMedian(qs);
		// int index=distance.length / 2;
		// qs.select(distance, index);
		
		return qm.median(distance);
		*/
		
		// Surprisingly, this sort seems faster than quickmedian
		// Although it's hard to provide a progress bar with this sort...
		Arrays.sort(distance);
		
		// final int size=distance.length;
		double avgDistance = 0;
		double alpha = 0.05;
		int firstIdx = (int) (alpha * distance.length);
		int lastIdx = (int) ((1 - alpha) * distance.length);
		
		try{
			for (int i = firstIdx; i <= lastIdx; i++)
				avgDistance += distance[i];
		}catch (ArrayIndexOutOfBoundsException ex){
			ex.printStackTrace();
		}
		avgDistance /= lastIdx - firstIdx + 1;
		
		return avgDistance;
	}

	private void save() {
		FileDialog dialog = new FileDialog();
		dialog.addFilter("Excel (*.xls)", "xls");
		dialog.setupAsSaveDialog("Save data as Excel...");
		dialog.show(this);
		String path = dialog.getPath();
		if (path != "")
			saveToExcel(path);
	}

	private void saveToExcel(String path) {
		Workbook wb = new HSSFWorkbook();
		try {
			Sheet sheet = wb.createSheet("sheet 1");

			// Header
			Row row = sheet.createRow((short) 0);
			row.createCell(0).setCellValue("X");
			row.createCell(1).setCellValue("Y");
			row.createCell(2).setCellValue("Width");
			row.createCell(3).setCellValue("Height");
			row.createCell(4).setCellValue("Distance");

			ListModel regionModel = regionList.getModel();
			for (int i = 0; i < regionModel.getSize(); i++) {
				RegionItem region = (RegionItem) regionModel.getElementAt(i);
				row = sheet.createRow((short) (i*3) + 1);
				row.createCell(0).setCellValue(region.getX());
				row.createCell(1).setCellValue(region.getY());
				row.createCell(2).setCellValue(region.getWidth());
				row.createCell(3).setCellValue(region.getHeight());
				row.createCell(4).setCellValue(region.getAvgDistance());
				
				Row bins = sheet.createRow((short) (i*3) + 2);
				Row values = sheet.createRow((short) (i*3) + 3);
				region.getHistogram().exportToExcel(bins, values);
			}

			FileOutputStream fileOut = new FileOutputStream(path);

			wb.write(fileOut);
			fileOut.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void calculateHistograms(ImagePlus imp){
		for(int channel=1; channel < imp.getNChannels(); channel++){
			imp.setPositionWithoutUpdate(channel, 1, 1);
			ImageHistogram h= new ImageHistogram(imp,0);
			histograms.put(new Integer(channel), h);
		}
		imp.setPositionWithoutUpdate(1, 1, 1);
	}
	
    private void drawROIs() {
        drawROIs(WindowManager.getCurrentImage());
    }

    private void drawROIs(ImagePlus imp) {
        Object rois[] = chckbxShowAll.isSelected() ? listModel.toArray() : regionList.getSelectedValues();
        int indexes[] = chckbxShowAll.isSelected() ? null : regionList.getSelectedIndices();

        drawROIs(imp, rois, indexes); 
    }

    private static void drawROIs(ImagePlus imp, Object items[], int indexes[]) {
        ImageCanvas canvas = imp.getCanvas();
        Graphics g = canvas.getGraphics();

        canvas.update(g);

        g.setColor(ROI_COLOR);

        for (int i = 0; i < items.length; i++) {
            String label = "[" + (indexes != null ? indexes[i] : i) + "]";
            Roi roi = ((RegionItem) items[i]).getRoi();

            if (roi.getType() == Roi.COMPOSITE) {
                roi.setImage(imp);
                Color c = Roi.getColor();
                Roi.setColor(ROI_COLOR);
                roi.draw(g);
                Roi.setColor(c);
            } else {
                Polygon p = roi.getPolygon();
                int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
                for (int j = 0; j < p.npoints; j++) {
                    x2 = canvas.screenX(p.xpoints[j]);
                    y2 = canvas.screenY(p.ypoints[j]);
                    if (j > 0) {
                        g.drawLine(x1, y1, x2, y2);
                    }
                    x1 = x2;
                    y1 = y2;
                }
                if (roi.isArea() && p.npoints > 0) {
                    int x0 = canvas.screenX(p.xpoints[0]);
                    int y0 = canvas.screenY(p.ypoints[0]);
                    g.drawLine(x1, y1, x0, y0);
                }
            }
            drawRoiLabel(label, canvas, roi.getBounds());
        }
    }

    private static void drawRoiLabel(String label, ImageCanvas canvas, Rectangle r) {
        Graphics g = canvas.getGraphics();

        int x = canvas.screenX(r.x);
        int y = canvas.screenY(r.y);
        double mag = canvas.getMagnification();
        int width = (int) (r.width * mag);
        int height = (int) (r.height * mag);
        int size = width > 40 && height > 40 ? 12 : 9;
        if (size == 12) {
            g.setFont(LARGE_FONT);
        } else {
            g.setFont(SMALL_FONT);
        }

        FontMetrics metrics = g.getFontMetrics();
        int labelW = metrics.stringWidth(label);
        int labelH = metrics.getHeight();

        g.setColor(ROI_COLOR);

        g.fillRect(x, y, labelW, labelH);

        y += labelH - 3;

        g.setColor(LABEL_COLOR);
        g.drawString(label, x, y);
    }

    private void importLif(){
		// TODO: try calling lociimporter directly (if possible), in
		// order to use our
		// own filedialog (instead of awt)
		IJ.runPlugIn("loci.plugins.LociImporter",
				"location=[Local machine] windowless=false");
		try {
			calculateHistograms(IJ.getImage());
			channelOne.setHistograms(histograms);
			channelTwo.setHistograms(histograms);
			channelOne.setEnabled(true);
			channelTwo.setEnabled(true);
			
			/*thresholdSliderOne.setHistogram(histograms.get(new Integer(thresholdSliderOne.getNumber())));
			thresholdSliderTwo.setHistogram(histograms.get(new Integer(thresholdSliderTwo.getNumber())));
			 */

			StackWindow stackWindow = (StackWindow) (WindowManager
					.getCurrentWindow());
			ScrollbarWithLabel channelSlider = (ScrollbarWithLabel) stackWindow
					.getComponent(STACK_INDEX_OF_CHANNEL_SLIDER);
			channelSlider.addAdjustmentListener(thresholdListenerOne);
			channelSlider.addAdjustmentListener(thresholdListenerTwo);
			
			btnAddCurrentSelection.setEnabled(true);
			btnFastAdd.setEnabled(true);
			btnStop.setEnabled(true);

			
		} catch (RuntimeException ex) {
			// there was an error with loci plugin importing
			System.err.println("Problem importing the image");
			ex.printStackTrace();
			return;
		}
    }
    
	/*
	public static ImagePlus getImage(int channel) {
		StackWindow stackWindow = (StackWindow) (WindowManager
				.getCurrentWindow());
		if (stackWindow == null)
			return null;
		ImagePlus imp =IJ.getImage();
				//stackWindow.getImagePlus();
		int currentChannel = imp.getChannel();
		int currentSlice = imp.getCurrentSlice();
		System.err.println("" + channel + "," + currentSlice);
		//if (channel != currentChannel)
		//	imp.setPositionWithoutUpdate(channel, currentSlice, 1);
		
		n = imp.getStackIndex(channel, currentSlice, t);
		
		ImagePlus result = new ImagePlus("Single channel", imp.getProcessor());

		//if (channel != currentChannel)
		//	imp.setPositionWithoutUpdate(currentChannel, currentSlice, 1);
		return result;
	}*/

}
