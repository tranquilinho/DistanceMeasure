import java.io.*; // for IOException, PrintWriter, FileOutputStream
import java.text.*; // for DecimalFormat

import javax.swing.JProgressBar;
// To compile: javac Histogram.java
// Build docs: javadoc -author -version Histogram.java

import org.apache.poi.ss.usermodel.Row;

/**
 * A simple histogram class. It can be used to accumulate a histogram and
 * calculate statistical information about it.
 * 
 * @author Simon George
 * @version 1.0 31 Aug 2001
 * Download from http://www.pp.rhul.ac.uk/~george/PH2150/downloads/Histogram.java
 */
public class Histogram {

	JProgressBar progressBar = null;
	private int progressStart, progressSlice;

	/**
	 * Constructor which sets name, number of bins, and range.
	 * 
	 * @param name
	 *            Give the histogram a text name
	 * @param nbins
	 *            the number of bins the histogram should have. The range
	 *            specified by min and max will be divided up into this many
	 *            bins.
	 * @param min
	 *            the minimum of the range covered by the histogram bins
	 * @param max
	 *            the maximum value of the range covered by the histogram bins
	 */
	public Histogram(String name, int nbins, double min, double max) {
		m_nbins = nbins;
		m_min = min;
		m_max = max;
		m_name = name;
		m_hist = new double[m_nbins];
		m_underflow = 0;
		m_overflow = 0;
	}

	public Histogram(double[] v, int nbins) {
		long startTime = System.nanoTime();

		m_min = 0;
		m_max = 0;
		for (int i = 0; i < v.length; i++) {
			if (v[i] > m_max)
				m_max = v[i];
			if (v[i] < m_min)
				m_min = v[i];
		}
		m_nbins = nbins;
		m_hist = new double[m_nbins];
		m_underflow = 0;
		m_overflow = 0;
		long finishTime = System.nanoTime();

		System.err.println("Elapsed time (new Histogram): "
				+ (finishTime - startTime) / 1000000);

		// fill(v);
	}

	public Histogram(double[] v, int nbins, JProgressBar bar,
			int progressStart, int progressSlice) {
		this(v, nbins);
		progressBar = bar;
		this.progressStart = progressStart;
		this.progressSlice = progressSlice;
	}

	public void fill(double[] v) {
		long startTime = System.nanoTime();
		int progressSkip = 0;

		for (int i = 0; i < v.length; i++) {
			fill(v[i]);
			if (progressSkip > 100) {
				progressSkip = 0;
				if (Thread.currentThread().isInterrupted())
					return;
				if (progressBar != null) {
					double progress = progressStart + (i * 1.0 * progressSlice)
							/ v.length;
					progressBar.setValue((int) progress);
				}
			}
			progressSkip++;
		}
		long finishTime = System.nanoTime();

		System.err.println("Elapsed time (fill): " + (finishTime - startTime)
				/ 1000000);

	}

	/**
	 * Enter data into the histogram. The fill method takes the given value,
	 * works out which bin this corresponds to, and increments this bin by one.
	 * 
	 * @param x
	 *            is the value to add in to the histogram
	 */
	public void fill(double x) {
		// use findBin method to work out which bin x falls in
		BinInfo bin = findBin(x);
		// check the result of findBin in case it was an overflow or underflow
		if (bin.isUnderflow) {
			m_underflow++;
		}
		if (bin.isOverflow) {
			m_overflow++;
		}
		if (bin.isInRange) {
			m_hist[bin.index]++;
		}
		// print out some debug information if the flag is set
		if (m_debug) {
			System.out.println("debug: fill: value " + x + " # underflows "
					+ m_underflow + " # overflows " + m_overflow
					+ " bin index " + bin.index);
		}
		// count the number of entries made by the fill method
		m_entries++;
	}

	/**
	 * Private class used internally to store info about which bin of the
	 * histogram to use for a number to be filled.
	 */
	private class BinInfo {
		public int index;
		public boolean isUnderflow;
		public boolean isOverflow;
		public boolean isInRange;
	}

	/**
	 * Private internal utility method to figure out which bin of the histogram
	 * a number falls in.
	 * 
	 * @return info on which bin x falls in.
	 */
	private BinInfo findBin(double x) {
		BinInfo bin = new BinInfo();
		bin.isInRange = false;
		bin.isUnderflow = false;
		bin.isOverflow = false;
		// first check if x is outside the range of the normal histogram bins
		if (x < m_min) {
			bin.isUnderflow = true;
		} else if (x > m_max) {
			bin.isOverflow = true;
		} else {
			// search for histogram bin into which x falls
			double binWidth = (m_max - m_min) / m_nbins;
			for (int i = 0; i < m_nbins; i++) {
				double highEdge = m_min + (i + 1) * binWidth;
				if (x <= highEdge) {
					bin.isInRange = true;
					bin.index = i;
					break;
				}
			}
		}
		return bin;

	}

	/**
	 * The mean estimator is calculated from the average of the bin centres,
	 * weighted by their content. It does not include over/underflows.
	 * 
	 * @return mean
	 */
	public double mean() {
		double sum = 0;
		double binWidth = (m_max - m_min) / m_nbins;
		for (int i = 0; i < m_nbins; i++) {
			double binCentreValue = m_min + (i + 0.5) * binWidth;
			sum += m_hist[i] * binCentreValue;
		}
		return sum / (m_entries - m_overflow - m_underflow);
	}

	/**
	 * Calculate area of histogram, excluding over/underflows, by summing the
	 * contents of all the bins
	 * 
	 * @return area under histogram
	 */
	public double area() {
		double sum = 0;
		for (int i = 0; i < m_nbins; i++) {
			sum += m_hist[i];
		}
		return sum;
	}

	/**
	 * Save the histogram data to a file. The file format is very simple,
	 * human-readable text so it can be imported into Excel or cut & pasted into
	 * other applications.
	 * 
	 * @param fileName
	 *            name of the file to write the histogram to. Note this must be
	 *            valid for your operating system, e.g. a unix filename might
	 *            not work under windows
	 * @exception IOException
	 *                if file cannot be opened or written to.
	 */

	public void writeToFile(String fileName) throws IOException {
		PrintWriter outfile = new PrintWriter(new FileOutputStream(fileName));
		outfile.println("// Output from Histogram class");
		outfile.println("// metaData: ");
		outfile.println("name \"" + m_name + "\"");
		outfile.println("bins " + m_nbins);
		outfile.println("min " + m_min);
		outfile.println("max " + m_max);
		outfile.println("totalEntries " + m_entries);
		outfile.println("underflow " + m_underflow);
		outfile.println("overflow " + m_overflow);
		outfile.println("// binData:");
		for (int i = 0; i < m_nbins; i++) {
			outfile.println(m_hist[i]);
		}
		outfile.println("// end.");
		outfile.close();
	}

	/**
	 * Print the histogram data to the console. Output is only basic, intended
	 * for debugging purposes. A good example of formatted output.
	 */
	public void show() {
		DecimalFormat df = new DecimalFormat(" ##0.00;-##0.00");
		double binWidth = (m_max - m_min) / m_nbins;
		System.out.println("Histogram \"" + m_name + "\", " + m_entries
				+ " entries");
		System.out.println(" bin range        height");
		for (int i = 0; i < m_nbins; i++) {
			double binLowEdge = m_min + i * binWidth;
			double binHighEdge = binLowEdge + binWidth;
			System.out.println(df.format(binLowEdge) + " to "
					+ df.format(binHighEdge) + "   " + df.format(m_hist[i]));
		}
	}

	public void exportToExcel(Row bins, Row values) {
		double binWidth = (m_max - m_min) / m_nbins;

		for (int i = 0; i < m_nbins; i++) {
			bins.createCell(i).setCellValue(m_min + i * binWidth);
			values.createCell(i).setCellValue(m_hist[i]);
		}
	}

	/**
	 * Get number of entries in the histogram. This should correspond to the
	 * number of times the fill method has been used.
	 * 
	 * @return number of entries
	 */
	public int entries() {
		return m_entries;
	}

	/**
	 * Get the name of the histogram. The name is an arbitrary label for the
	 * user, and is set by the constructor.
	 * 
	 * @return histogram name
	 */
	public String name() {
		return m_name;
	}

	/**
	 * Get the number of bins in the histogram. The range of the histogram
	 * defined by min and max is divided into this many bins.
	 * 
	 * @return number of bins
	 */
	public int numberOfBins() {
		return m_nbins;
	}

	/**
	 * Get lower end of histogram range
	 * 
	 * @return minimum x value covered by histogram
	 */
	public double min() {
		return m_min;
	}

	/**
	 * Get upper end of histogram range
	 * 
	 * @return maximum x value covered by histogram
	 */
	public double max() {
		return m_max;
	}

	/**
	 * Get the height of the overflow bin. Any value passed to the fill method
	 * which falls above the range of the histogram will be counted in the
	 * overflow bin.
	 * 
	 * @return number of overflows
	 */
	public double overflow() {
		return m_overflow;
	}

	/**
	 * Get the height of the underflow bin. Any value passed to the fill method
	 * which falls below the range of the histogram will be counted in the
	 * underflow bin.
	 * 
	 * @return number of underflows
	 */
	public double underflow() {
		return m_underflow;
	}

	/**
	 * This method gives you the bin contents in the form of an array. It might
	 * be useful for example if you want to use the histogram in some other way,
	 * for example to pass to a plotting package.
	 * 
	 * @return array of bin heights
	 */
	public double[] getArray() {
		return m_hist;
	}

	/**
	 * Set debug flag.
	 * 
	 * @param flag
	 *            debug flag (true or false)
	 */
	public void setDebug(boolean flag) {
		m_debug = flag;
	}

	/**
	 * Get debug flag.
	 * 
	 * @return value of debug flag (true or false)
	 */
	public boolean getDebug() {
		return m_debug;
	}

	// private data used internally by this class.
	private double[] m_hist;
	private String m_name;
	private double m_min;
	private double m_max;
	private int m_nbins;
	private int m_entries;
	private double m_overflow;
	private double m_underflow;
	private boolean m_debug;
}
