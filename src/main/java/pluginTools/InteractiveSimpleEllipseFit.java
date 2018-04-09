package pluginTools;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.CardLayout;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Rectangle;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeriesCollection;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import costMatrix.CostFunction;
import ellipsoidDetector.Distance;
import ellipsoidDetector.Intersectionobject;
import ellipsoidDetector.Tangentobject;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.EllipseRoi;
import ij.gui.ImageCanvas;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.io.Opener;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.ColorProcessor;

import listeners.AngleListener;
import listeners.ColorListener;
import listeners.DisplayRoiListener;
import listeners.DrawListener;
import listeners.ETrackFilenameListener;
import listeners.ETrackIniSearchListener;
import listeners.ETrackMaxSearchListener;
import listeners.EllipseNonStandardMouseListener;
import listeners.HighProbListener;
import listeners.IlastikListener;
import listeners.InsideCutoffListener;
import listeners.LowProbListener;
import listeners.MaxTryListener;
import listeners.MaxperimeterListener;
import listeners.MinpercentListener;
import listeners.MinperimeterListener;
import listeners.OutsideCutoffListener;
import listeners.RListener;
import listeners.RedoListener;
import listeners.RoiListener;
import listeners.SaveListener;
import listeners.SaverDirectory;
import listeners.TimeListener;
import listeners.TlocListener;
import listeners.TrackidListener;
import listeners.ZListener;
import listeners.ZlocListener;
import mpicbg.imglib.util.Util;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.algorithm.ransac.RansacModels.DisplayasROI;
import net.imglib2.algorithm.ransac.RansacModels.Ellipsoid;
import net.imglib2.algorithm.stats.Normalize;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import pluginTools.InteractiveSimpleEllipseFit.ValueChange;
import utility.DisplayAuto;
import utility.NearestRoi;
import utility.Roiobject;
import utility.ShowResultView;
import utility.ShowView;
import utility.Slicer;
import utility.ThreeDRoiobject;
import utility.TrackModel;

public class InteractiveSimpleEllipseFit extends JPanel implements PlugIn {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String usefolder = IJ.getDirectory("imagej");
	public String addToName = "EllipseFits";
	public final int scrollbarSize = 1000;
	public int tablesize;
	public Overlay overlay;
	public Overlay emptyoverlay;
	public int thirdDimensionslider = 1;
	public int thirdDimensionsliderInit = 1;
	public int fourthDimensionslider = 1;
	public int fourthDimensionsliderInit = 1;
	public int rowchoice;
	public int radiusdetection = 5;
	public int maxtry = 30;
	public float minpercent = 0.5f;
	public float minpercentINI = 0.65f;
	public float minpercentINIArc = 0.25f;
	public final double minSeperation = 5;
	public String selectedID;
	public float insideCutoff = 15;
	public float outsideCutoff = insideCutoff;

	public long maxsize = 100;
	public int span = 2;
	public int minperimeter = 100;
	public int maxperimeter = 1000;
	public float lowprob = 0f;
	public float highprob = 1f;

	public float lowprobmin = 0f;
	public float highprobmin = 0f;

	public boolean redoing;
	public float lowprobmax = 1.0f;
	public float highprobmax = 1.0f;

	public float insideCutoffmin = 5;
	public float outsideCutoffmin = 5;

	public float insideCutoffmax = 50;
	public float outsideCutoffmax = 50;
	public int roiindex;
	public int fourthDimension;
	public int thirdDimension;
	public int thirdDimensionSize;
	public int fourthDimensionSize;
	public ImagePlus impA, impOrig;
	public boolean isDone;
	public int MIN_SLIDER = 0;
	public int MAX_SLIDER = 500;
	public int row;
	public HashMap<String, Integer> Accountedframes;
	public HashMap<String, Integer> AccountedZ;
	public JProgressBar jpb;
	public JLabel label = new JLabel("Fitting..");
	public int Progressmin = 0;
	public int Progressmax = 100;
	public int max = Progressmax;
	public File userfile;
	public Frame jFreeChartFrame;
	public NumberFormat nf;
	public XYSeriesCollection dataset;
	public JFreeChart chart;
	public RandomAccessibleInterval<FloatType> originalimg;
	public RandomAccessibleInterval<IntType> originalimgsuper;
	public RandomAccessibleInterval<FloatType> originalimgbefore;
	ResultsTable rtAll;
	public File inputfile;
	public String inputdirectory;
	public int radiusInt = 2;
	public float radius = 50f;
	public int strokewidth = 1;
	public float radiusMin = radiusInt;
	public float radiusMax = 300f;
	public MouseMotionListener ml;
	public MouseListener mvl;
	public Roi nearestRoiCurr;
	public OvalRoi nearestIntersectionRoiCurr;
	public Roi selectedRoi;
	public TextField inputFieldIter;
	public JTable table;
	public ArrayList<Roi> Allrois;
	public HashMap<String, Roiobject> ZTRois;
	public HashMap<String, Roiobject> AutoZTRois;
	public HashMap<String, Roiobject> DefaultZTRois;
	public HashMap<String, Roiobject> IntersectionZTRois;
	public ImagePlus imp;
	public ImagePlus localimp;
	public ImagePlus resultimp;
	public ImagePlus emptyimp;
	public int ndims;
	public float initialSearchradius = 10;
	public float maxSearchradius = 15;
	public float maxSearchradiusS = 15;
	public int missedframes = 20;
	public int initialSearchradiusInit = (int) initialSearchradius;
	public CostFunction<Intersectionobject, Intersectionobject> UserchosenCostFunction;
	public float initialSearchradiusMin = 1;
	public float initialSearchradiusMax = 1000;
	public float alphaMin = 0;
	public float alphaMax = 1;
	public float betaMin = 0;
	public float betaMax = 1;
	public int maxSearchradiusInit = (int) maxSearchradius;
	public float maxSearchradiusMin = 1;
	public float maxSearchradiusMax = 1000;
	public float maxSearchradiusMinS = 1;
	public float maxSearchradiusMaxS = 1000;
	public RandomAccessibleInterval<FloatType> CurrentView;
	public RandomAccessibleInterval<FloatType> CurrentViewOrig;
	public RandomAccessibleInterval<FloatType> CurrentResultView;
	public Color confirmedRois = Color.BLUE;
	public Color defaultRois = Color.YELLOW;
	public Color colorChange = Color.GRAY;
	public Color colorInChange = Color.RED;
	public int maxlabel;
	public Color colorOval = Color.CYAN;
	public Color colorDet = Color.GREEN;
	public Color colorLineA = Color.YELLOW;
	public Color colorLineB = Color.YELLOW;
	public Color colorresult = Color.magenta;
	public double maxdistance = 10;
	public float alpha = 0.5f;
	public float beta = 0.5f;
	ImageStack prestack;
	public MouseAdapter mouseadapter;
	public ArrayList<Pair<Ellipsoid, List<Pair<RealLocalizable, BitType>>>> superReducedSamples;
	public int[] Clickedpoints;
	public int starttime;
	public int endtime;
	public ArrayList<Pair<String, Intersectionobject>> Tracklist;
	public ArrayList<Pair<String, double[]>> resultAngle;
	public HashMap<String, Pair<ArrayList<double[]>, ArrayList<Line>>> resultDraw;
	public HashMap<String, ArrayList<Line>> resultDrawLine;
	public KeyListener kl;
	public SimpleWeightedGraph<Intersectionobject, DefaultWeightedEdge> parentgraph;
	public HashMap<String, SimpleWeightedGraph<Intersectionobject, DefaultWeightedEdge>> parentgraphZ;
	public HashMap<String, ArrayList<Intersectionobject>> ALLIntersections;
	public Set<Integer> pixellist;
	ColorProcessor cp = null;
	public HashMap<String, Intersectionobject> Finalresult;
	public boolean isCreated = false;
	public RoiManager roimanager;
	public String uniqueID, tmpID, ZID, TID;
	public RandomAccessibleInterval<BitType> empty;
	public RandomAccessibleInterval<IntType> emptyWater;
	public boolean automode;
	public boolean supermode;
	public double mindistance = 200;
	public int alphaInit = 1;
	public int betaInit = 0;
	public int minSizeInit = 50;
	public int maxSizeInit = 500;

	public int maxSearchInit = 100;
	public int maxframegap = 10;
	public static enum ValueChange {
		ROI, ALL, THIRDDIMmouse, FOURTHDIMmouse, DISPLAYROI, RADIUS, INSIDE, OUTSIDE, RESULT, RectRoi, SEG
	}

	public void setTime(final int value) {

		fourthDimensionslider = value;
		fourthDimensionsliderInit = 1;
		fourthDimension = 1;
	}

	public int getTimeMax() {

		return thirdDimensionSize;
	}

	public void setZ(final int value) {
		thirdDimensionslider = value;
		thirdDimensionsliderInit = 1;
		thirdDimension = 1;
	}

	public void setInsidecut(final float insideCutoff) {

		insideslider
		.setValue(utility.Slicer.computeScrollbarPositionFromValue(insideCutoff, insideCutoffmin, insideCutoffmax, scrollbarSize));
	}

	public void setOutsidecut(final int value) {

		outsideCutoff = value;
	}

	public int getZMax() {

		return fourthDimensionSize;
	}

	public void setlowprob(final float value) {
		lowprob = value;
		lowprob = computeScrollbarPositionFromValue(lowprob, lowprobmin, lowprobmax, scrollbarSize);
	}

	public double getlowprob(final float value) {

		return lowprob;

	}
	public void setInitialsearchradius(final float value) {
		initialSearchradius = value;
		initialSearchradiusInit = computeScrollbarPositionFromValue(initialSearchradius, initialSearchradiusMin,
				initialSearchradiusMax, scrollbarSize);
	}

	public void setInitialmaxsearchradius(final float value) {
		maxSearchradius = value;
		maxSearchradiusInit = computeScrollbarPositionFromValue(maxSearchradius, maxSearchradiusMin, maxSearchradiusMax,
				scrollbarSize);
	}

	public double getInitialsearchradius(final float value) {

		return initialSearchradius;

	}
	public void setInitialAlpha(final float value) {
		alpha = value;
		alphaInit = computeScrollbarPositionFromValue(alpha, alphaMin, alphaMax, scrollbarSize);
	}

	public double getInitialAlpha(final float value) {

		return alpha;

	}

	public void setInitialBeta(final float value) {
		beta = value;
		betaInit = computeScrollbarPositionFromValue(beta, betaMin, betaMax, scrollbarSize);
	}

	public double getInitialBeta(final float value) {

		return beta;

	}

	public void sethighprob(final float value) {
		highprob = value;
		highprob = computeScrollbarPositionFromValue(highprob, highprobmin, highprobmax, scrollbarSize);
	}

	public double gethighprob(final float value) {

		return highprob;

	}

	public float computeValueFromScrollbarPosition(final int scrollbarPosition, final float min, final float max,
			final int scrollbarSize) {
		return min + (scrollbarPosition / (float) scrollbarSize) * (max - min);
	}

	public int computeScrollbarPositionFromValue(final float sigma, final float min, final float max,
			final int scrollbarSize) {
		return Util.round(((sigma - min) / (max - min)) * scrollbarSize);
	}

	public InteractiveSimpleEllipseFit() {
		nf = NumberFormat.getInstance(Locale.ENGLISH);
		nf.setMaximumFractionDigits(2);
		this.dataset = new XYSeriesCollection();
		this.chart = utility.ChartMaker.makeChart(dataset, "Angle evolution", "Timepoint", "Angle");
		this.jFreeChartFrame = utility.ChartMaker.display(chart, new Dimension(500, 500));
		this.jFreeChartFrame.setVisible(false);
		this.automode = false;
	}

	public InteractiveSimpleEllipseFit(RandomAccessibleInterval<FloatType> originalimg, File file) {
		this.inputfile = file;
		this.inputdirectory = file.getParent();
		this.originalimg = originalimg;
		this.ndims = originalimg.numDimensions();
		this.dataset = new XYSeriesCollection();
		this.chart = utility.ChartMaker.makeChart(dataset, "Angle evolution", "Timepoint", "Angle");
		this.jFreeChartFrame = utility.ChartMaker.display(chart, new Dimension(500, 500));
		this.jFreeChartFrame.setVisible(false);
		nf = NumberFormat.getInstance(Locale.ENGLISH);
		nf.setMaximumFractionDigits(2);
		this.automode = false;
	}

	public InteractiveSimpleEllipseFit(RandomAccessibleInterval<FloatType> originalimg) {
		this.inputfile = null;
		this.inputdirectory = null;
		this.originalimg = originalimg;
		this.ndims = originalimg.numDimensions();
		this.dataset = new XYSeriesCollection();
		this.chart = utility.ChartMaker.makeChart(dataset, "Angle evolution", "Timepoint", "Angle");
		this.jFreeChartFrame = utility.ChartMaker.display(chart, new Dimension(500, 500));
		this.jFreeChartFrame.setVisible(false);
		nf = NumberFormat.getInstance(Locale.ENGLISH);
		nf.setMaximumFractionDigits(2);
		this.automode = false;
		this.supermode = false;
	}

	public InteractiveSimpleEllipseFit(RandomAccessibleInterval<FloatType> originalimg, boolean automode) {
		this.inputfile = null;
		this.inputdirectory = null;
		this.originalimg = originalimg;
		this.ndims = originalimg.numDimensions();
		this.dataset = new XYSeriesCollection();
		this.chart = utility.ChartMaker.makeChart(dataset, "Angle evolution", "Timepoint", "Angle");
		this.jFreeChartFrame = utility.ChartMaker.display(chart, new Dimension(500, 500));
		this.jFreeChartFrame.setVisible(false);
		nf = NumberFormat.getInstance(Locale.ENGLISH);
		nf.setMaximumFractionDigits(2);
		this.automode = true;
		this.supermode = false;
	}

	public InteractiveSimpleEllipseFit(RandomAccessibleInterval<FloatType> originalimg,
			RandomAccessibleInterval<FloatType> originalimgbefore, boolean automode) {
		this.inputfile = null;
		this.inputdirectory = null;
		this.originalimg = originalimg;
		this.originalimgbefore = originalimgbefore;
		this.ndims = originalimg.numDimensions();
		this.dataset = new XYSeriesCollection();
		this.chart = utility.ChartMaker.makeChart(dataset, "Angle evolution", "Timepoint", "Angle");
		this.jFreeChartFrame = utility.ChartMaker.display(chart, new Dimension(500, 500));
		this.jFreeChartFrame.setVisible(false);
		nf = NumberFormat.getInstance(Locale.ENGLISH);
		nf.setMaximumFractionDigits(2);
		this.automode = automode;
		this.supermode = false;
	}

	public InteractiveSimpleEllipseFit(RandomAccessibleInterval<FloatType> originalimg,
			RandomAccessibleInterval<FloatType> originalimgbefore, RandomAccessibleInterval<IntType> originalimgsuper,
			boolean automode, boolean supermode) {
		this.inputfile = null;
		this.inputdirectory = null;
		this.originalimg = originalimg;
		this.originalimgsuper = originalimgsuper;
		this.originalimgbefore = originalimgbefore;
		this.ndims = originalimg.numDimensions();
		this.dataset = new XYSeriesCollection();
		this.chart = utility.ChartMaker.makeChart(dataset, "Angle evolution", "Timepoint", "Angle");
		this.jFreeChartFrame = utility.ChartMaker.display(chart, new Dimension(500, 500));
		this.jFreeChartFrame.setVisible(false);
		nf = NumberFormat.getInstance(Locale.ENGLISH);
		nf.setMaximumFractionDigits(2);
		this.automode = true;
		this.supermode = supermode;
	}

	public void run(String arg0) {

		FloatType minval = new FloatType(0);
		FloatType maxval = new FloatType(1);
		Normalize.normalize(Views.iterable(originalimg), minval, maxval);

		redoing = false;
		superReducedSamples = new ArrayList<Pair<Ellipsoid, List<Pair<RealLocalizable, BitType>>>>();
		pixellist = new HashSet<Integer>();
		rtAll = new ResultsTable();
		jpb = new JProgressBar();
		Allrois = new ArrayList<Roi>();
		ZTRois = new HashMap<String, Roiobject>();
		AutoZTRois = new HashMap<String, Roiobject>();
		DefaultZTRois = new HashMap<String, Roiobject>();
		IntersectionZTRois = new HashMap<String, Roiobject>();
		Clickedpoints = new int[2];
		ALLIntersections = new HashMap<String, ArrayList<Intersectionobject>>();
		Finalresult = new HashMap<String, Intersectionobject>();
		Tracklist = new ArrayList<Pair<String, Intersectionobject>>();
		resultDraw = new HashMap<String, Pair<ArrayList<double[]>, ArrayList<Line>>>();
		resultDrawLine = new HashMap<String, ArrayList<Line>>();
		Accountedframes = new HashMap<String, Integer>();
		AccountedZ = new HashMap<String, Integer>();

		setlowprob(lowprob);
		sethighprob(highprob);
		setInsidecut(insideCutoff);
		

		if (ndims < 3) {

			thirdDimensionSize = 0;
			fourthDimensionSize = 0;
		}

		if (ndims == 3) {

			fourthDimension = 1;
			thirdDimension = 1;
			fourthDimensionSize = 0;

			thirdDimensionSize = (int) originalimg.dimension(2);

		}

		if (ndims == 4) {

			fourthDimension = 1;
			thirdDimension = 1;

			thirdDimensionSize = (int) originalimg.dimension(2);
			fourthDimensionSize = (int) originalimg.dimension(3);

			prestack = new ImageStack((int) originalimg.dimension(0), (int) originalimg.dimension(1),
					java.awt.image.ColorModel.getRGBdefault());
		}

		if (ndims > 4) {

			System.out.println("Image has wrong dimensionality, upload an XYZT/XYT/XYZ/XY image");
			return;
		}

		setTime(fourthDimension);
		setZ(thirdDimension);
		CurrentView = utility.Slicer.getCurrentView(originalimg, fourthDimension, thirdDimensionSize, thirdDimension,
				fourthDimensionSize);
		if (originalimgbefore != null) {
			CurrentViewOrig = utility.Slicer.getCurrentView(originalimgbefore, fourthDimension, thirdDimensionSize,
					thirdDimension, fourthDimensionSize);

			impOrig = ImageJFunctions.show(CurrentViewOrig);
			impOrig.setTitle("Raw Image");
			impOrig.setTitle("Active image" + " " + "time point : " + fourthDimension + " " + " Z: " + thirdDimension);
		}

		if(originalimgsuper!=null)
			ImageJFunctions.show(originalimgsuper).setTitle("Super Pixel Segmentation");
		imp = ImageJFunctions.show(CurrentView);
	
		imp.setTitle("Active ProbabilityMap" + " " + "time point : " + fourthDimension + " " + " Z: " + thirdDimension);

		// Create empty Hyperstack

		empty = new ArrayImgFactory<BitType>().create(originalimg, new BitType());
		emptyWater = new ArrayImgFactory<IntType>().create(originalimg, new IntType());

		if (!automode) {
			roimanager = RoiManager.getInstance();

			if (roimanager == null) {
				roimanager = new RoiManager();
			}
		}
		updatePreview(ValueChange.ALL);
		if(automode) {
		
			lowprobslider.setValue(computeScrollbarPositionFromValue(lowprob, lowprobmin, lowprobmax, scrollbarSize));
			highprobslider.setValue(computeScrollbarPositionFromValue(highprob, highprobmin, highprobmax, scrollbarSize));
			lowprob = utility.Slicer.computeValueFromScrollbarPosition(lowprobslider.getValue(), lowprobmin, lowprobmax, scrollbarSize);
			highprob = utility.Slicer.computeValueFromScrollbarPosition(highprobslider.getValue(), highprobmin, highprobmax, scrollbarSize);
			
			updatePreview(ValueChange.SEG);
			
		}
		System.out.println(lowprob + " " + highprob);

		Cardframe.repaint();
		Cardframe.validate();
		panelFirst.repaint();
		panelFirst.validate();

		Card();
		
		
	}

	public void updatePreview(final ValueChange change) {
		if (!automode) {
			roimanager = RoiManager.getInstance();

			if (roimanager == null) {
				roimanager = new RoiManager();
			}
		}

		
		

		//
		uniqueID = Integer.toString(thirdDimension) + Integer.toString(fourthDimension);
		ZID = Integer.toString(thirdDimension);
		TID = Integer.toString(fourthDimension);
		tmpID = Float.toString(thirdDimension) + Float.toString(fourthDimension);
		overlay = imp.getOverlay();

		if (overlay == null) {

			overlay = new Overlay();
			imp.setOverlay(overlay);
		}

		if (change == ValueChange.INSIDE || change == ValueChange.OUTSIDE) {

			if (automode) {

				empty = CreateBinaryBit(originalimg, lowprob, highprob);

			}

			StartComputing();

		}

		if (change == ValueChange.SEG) {
			RandomAccessibleInterval<FloatType> tempview = CreateBinary(CurrentView, lowprob, highprob);

			if (localimp == null || !localimp.isVisible() && automode) {
				localimp = ImageJFunctions.show(tempview);

			}

			else {

				final float[] pixels = (float[]) localimp.getProcessor().getPixels();
				final Cursor<FloatType> c = Views.iterable(tempview).cursor();

				for (int i = 0; i < pixels.length; ++i)
					pixels[i] = c.next().get();

				localimp.updateAndDraw();

			}

			localimp.setTitle(
					"Candidate Points" + " " + "time point : " + fourthDimension + " " + " Z: " + thirdDimension);

		}

		if (change == ValueChange.RESULT) {
			prestack = new ImageStack((int) originalimg.dimension(0), (int) originalimg.dimension(1),
					java.awt.image.ColorModel.getRGBdefault());

			String ID = (String) table.getValueAt(rowchoice, 0);
			ArrayList<double[]> resultlist = new ArrayList<double[]>();
			ArrayList<Line> resultline = new ArrayList<Line>();

			Pair<ArrayList<double[]>, ArrayList<Line>> resultpair;
			for (Pair<String, Intersectionobject> currentangle : Tracklist) {

				if (ID.equals(currentangle.getA())) {
					resultlist.add(new double[] { currentangle.getB().t, currentangle.getB().z,
							currentangle.getB().Intersectionpoint[0], currentangle.getB().Intersectionpoint[1] });

					String currentID = Integer.toString(currentangle.getB().z)
							+ Integer.toString(currentangle.getB().t);
					resultline.addAll(ZTRois.get(currentID).resultlineroi);

				}

			}
			resultpair = new ValuePair<ArrayList<double[]>, ArrayList<Line>>(resultlist, resultline);
			resultDraw.put(ID, resultpair);

			if (originalimg.numDimensions() > 3) {
				resultimp = ImageJFunctions.show(Slicer.getCurrentViewLarge(originalimg, thirdDimension));
				for (int time = 1; time <= fourthDimensionSize; ++time)
					prestack.addSlice(resultimp.getImageStack().getProcessor(time).convertToRGB());

				for (double[] current : resultDraw.get(ID).getA()) {
					Overlay resultoverlay = new Overlay();
					int time = (int) current[0];
					int Z = (int) current[1];
					double IntersectionX = current[2];
					double IntersectionY = current[3];
					int radius = 3;
					ShowResultView showcurrent = new ShowResultView(this, time, Z);
					showcurrent.shownew();

					cp = (ColorProcessor) (prestack.getProcessor(time).duplicate());
					cp.reset();

					resultimp.setOverlay(resultoverlay);

					OvalRoi selectedRoi = new OvalRoi(Util.round(IntersectionX - radius),
							Util.round(IntersectionY - radius), Util.round(2 * radius), Util.round(2 * radius));
					resultoverlay.add(selectedRoi);

					cp.setColor(colorLineA);
					cp.setLineWidth(4);
					cp.draw(selectedRoi);

					if (prestack != null)
						prestack.setPixels(cp.getPixels(), time);

				}
			}

			else {

				if (originalimgbefore == null)
					resultimp = ImageJFunctions.show(originalimg);
				else
					resultimp = ImageJFunctions.show(originalimgbefore);
				Overlay resultoverlay = new Overlay();
				for (int time = 1; time <= thirdDimensionSize; ++time)
					prestack.addSlice(resultimp.getImageStack().getProcessor(time).convertToRGB());
				for (double[] current : resultDraw.get(ID).getA()) {
					int Z = (int) current[1];
					double IntersectionX = current[2];
					double IntersectionY = current[3];
					int radius = 3;

					String currentID = Integer.toString(Z) + Integer.toString((int) current[0]);
					ShowResultView showcurrent = new ShowResultView(this, Z);
					showcurrent.shownew();

					cp = (ColorProcessor) (prestack.getProcessor(Z).duplicate());
					cp.reset();

					resultimp.setOverlay(resultoverlay);

					OvalRoi selectedRoi = new OvalRoi(Util.round(IntersectionX - radius),
							Util.round(IntersectionY - radius), Util.round(2 * radius), Util.round(2 * radius));
					resultoverlay.add(selectedRoi);

					Roiobject currentobject = ZTRois.get(currentID);
					Line nearestline = null;
					for (Line currentline : resultDraw.get(ID).getB()) {
						cp.setColor(colorLineA);
						cp.setLineWidth(4);
						Line nearest = utility.NearestRoi.getNearestLineRois(currentobject, new double[] {IntersectionX, IntersectionY}, this);
						if (nearest == currentline) {
							cp.draw(currentline);
							nearestline = currentline;
						}

					}
					if (nearestline != null)
						currentobject.resultlineroi.remove(nearestline);
					for (Line currentline : resultDraw.get(ID).getB()) {
						cp.setColor(colorLineA);
						cp.setLineWidth(4);
						Line nearest = utility.NearestRoi.getNearestLineRois(currentobject, new double[] {IntersectionX, IntersectionY}, this);
						if (nearest == currentline) {
							cp.draw(currentline);
						}

					}

					cp.setColor(colorresult);
					cp.setLineWidth(4);
					cp.draw(selectedRoi);

					if (prestack != null)
						prestack.setPixels(cp.getPixels(), Z);

				}

			}
			new ImagePlus("Overlays", prestack).show();
			resultimp.close();

		}

		if (change == ValueChange.ROI) {

			IJ.run("Select None");
			DefaultZTRois.clear();
			// roimanager.runCommand("show all");
			Roi[] Rois = roimanager.getRoisAsArray();
			Roiobject CurrentRoi = new Roiobject(Rois, thirdDimension, fourthDimension, true);

			DefaultZTRois.put(uniqueID, CurrentRoi);

			Accountedframes.put(TID, fourthDimension);

			AccountedZ.put(ZID, thirdDimension);

			ZTRois.put(uniqueID, CurrentRoi);

			Display();

		}

		if (change == ValueChange.THIRDDIMmouse || change == ValueChange.FOURTHDIMmouse) {

			if (automode) {
				updatePreview(ValueChange.SEG);
				Accountedframes.put(TID, fourthDimension);

				AccountedZ.put(ZID, thirdDimension);
				if (originalimgbefore != null) {
					CurrentViewOrig = utility.Slicer.getCurrentView(originalimgbefore, thirdDimension,
							thirdDimensionSize, thirdDimension, fourthDimensionSize);

					if (impOrig == null || !impOrig.isVisible()) {
						impOrig = ImageJFunctions.show(CurrentViewOrig);

					}

					else {

						final float[] pixels = (float[]) impOrig.getProcessor().getPixels();
						final Cursor<FloatType> c = Views.iterable(CurrentViewOrig).cursor();

						for (int i = 0; i < pixels.length; ++i)
							pixels[i] = c.next().get();

						impOrig.updateAndDraw();

					}
					impOrig.setTitle(
							"Active image" + " " + "time point : " + fourthDimension + " " + " Z: " + thirdDimension);
				}
			}
			if (imp == null || !imp.isVisible()) {
				imp = ImageJFunctions.show(CurrentView);

			}

			else {

				final float[] pixels = (float[]) imp.getProcessor().getPixels();
				final Cursor<FloatType> c = Views.iterable(CurrentView).cursor();

				for (int i = 0; i < pixels.length; ++i)
					pixels[i] = c.next().get();

				imp.updateAndDraw();

			}
			if(!automode){
				if (ZTRois.get(uniqueID) == null)
					DisplayDefault();
				else
					Display();
			}
			else
				DisplayAuto.Display(this);
			imp.setTitle("Active image" + " " + "time point : " + fourthDimension + " " + " Z: " + thirdDimension);

		}

	}

	public void StartComputing() {

		ComputeAngles compute = new ComputeAngles(this, jpb);

		compute.execute();

	}

	public RandomAccessibleInterval<BitType> CreateBinaryBit(RandomAccessibleInterval<FloatType> source, double lowprob,
			double highprob) {

		RandomAccessibleInterval<BitType> copyoriginal = new ArrayImgFactory<BitType>().create(source, new BitType());

		final RandomAccess<BitType> ranac = copyoriginal.randomAccess();
		final Cursor<FloatType> cursor = Views.iterable(source).localizingCursor();

		while (cursor.hasNext()) {

			cursor.fwd();

			ranac.setPosition(cursor);
			if (cursor.get().getRealDouble() > lowprob && cursor.get().getRealDouble() < highprob) {

				ranac.get().setOne();
			} else {
				ranac.get().setZero();
			}

		}

		return copyoriginal;

	}

	public RandomAccessibleInterval<FloatType> CreateBinary(RandomAccessibleInterval<FloatType> source, double lowprob,
			double highprob) {

		RandomAccessibleInterval<FloatType> copyoriginal = new ArrayImgFactory<FloatType>().create(source,
				new FloatType());

		final RandomAccess<FloatType> ranac = copyoriginal.randomAccess();
		final Cursor<FloatType> cursor = Views.iterable(source).localizingCursor();

		while (cursor.hasNext()) {

			cursor.fwd();

			ranac.setPosition(cursor);
			if (cursor.get().getRealDouble() > lowprob && cursor.get().getRealDouble() < highprob) {

				ranac.get().set(cursor.get());
			} else {
				ranac.get().set(0);
			}

		}

		return copyoriginal;

	}

	public void StartComputingCurrent() {

		redoing = true;
		ComputeAnglesCurrent compute = new ComputeAnglesCurrent(this, jpb);

		compute.execute();
	}

	public void Display() {

		overlay.clear();

		if (ZTRois.size() > 0) {

			for (Map.Entry<String, Roiobject> entry : ZTRois.entrySet()) {

				Roiobject currentobject = entry.getValue();

				if (currentobject.fourthDimension == fourthDimension
						&& currentobject.thirdDimension == thirdDimension) {

					for (int indexx = 0; indexx < currentobject.roilist.length; ++indexx) {

						Roi or = currentobject.roilist[indexx];
						or.setStrokeColor(confirmedRois);
						overlay.add(or);
					}

					if (currentobject.resultroi != null) {

						for (int i = 0; i < currentobject.resultroi.size(); ++i) {

							EllipseRoi ellipse = currentobject.resultroi.get(i);
							ellipse.setStrokeColor(colorInChange);
							overlay.add(ellipse);

						}

					}

					if (currentobject.resultovalroi != null) {

						for (int i = 0; i < currentobject.resultovalroi.size(); ++i) {

							OvalRoi ellipse = currentobject.resultovalroi.get(i);
							ellipse.setStrokeColor(colorDet);
							overlay.add(ellipse);

						}

					}

					if (currentobject.resultlineroi != null) {

						for (int i = 0; i < currentobject.resultlineroi.size(); ++i) {

							Line ellipse = currentobject.resultlineroi.get(i);
							ellipse.setStrokeColor(colorLineA);

							overlay.add(ellipse);

						}

					}

					break;
				}

			}
			imp.updateAndDraw();

			mark();
			select();

		}
	}

	public void DisplayOnly() {

		overlay.clear();

		if (ZTRois.size() > 0) {

			for (Map.Entry<String, Roiobject> entry : ZTRois.entrySet()) {

				Roiobject currentobject = entry.getValue();

				if (currentobject.fourthDimension == fourthDimension
						&& currentobject.thirdDimension == thirdDimension) {

					for (int indexx = 0; indexx < currentobject.roilist.length; ++indexx) {

						Roi or = currentobject.roilist[indexx];

						if (or == nearestRoiCurr) {

							or.setStrokeColor(colorInChange);

						}

						overlay.add(or);
					}

					if (currentobject.resultroi != null) {

						for (int i = 0; i < currentobject.resultroi.size(); ++i) {

							EllipseRoi ellipse = currentobject.resultroi.get(i);
							ellipse.setStrokeColor(colorInChange);
							overlay.add(ellipse);

						}

					}
					if (currentobject.resultovalroi != null) {

						for (int i = 0; i < currentobject.resultovalroi.size(); ++i) {

							OvalRoi ellipse = currentobject.resultovalroi.get(i);
							ellipse.setStrokeColor(colorDet);
							overlay.add(ellipse);

						}

					}

					if (currentobject.resultlineroi != null) {

						for (int i = 0; i < currentobject.resultlineroi.size(); ++i) {

							Line ellipse = currentobject.resultlineroi.get(i);
							ellipse.setStrokeColor(colorLineA);
							overlay.add(ellipse);

						}

					}
					break;
				}

			}
			imp.updateAndDraw();

		}
	}

	public void DisplayDefault() {

		overlay.clear();
		if (DefaultZTRois.size() > 0) {

			for (Map.Entry<String, Roiobject> entry : DefaultZTRois.entrySet()) {

				Roiobject currentobject = entry.getValue();

				for (int indexx = 0; indexx < currentobject.roilist.length; ++indexx) {

					Roi or = currentobject.roilist[indexx];
					or.setStrokeColor(defaultRois);
					overlay.add(or);
				}

				break;

			}
			imp.updateAndDraw();
			mark();
			select();

		}
	}

	public void select() {

		
		if (mvl != null)
			imp.getCanvas().removeMouseListener(mvl);
		imp.getCanvas().addMouseListener(mvl = new MouseListener() {

			final ImageCanvas canvas = imp.getWindow().getCanvas();

			@Override
			public void mouseClicked(MouseEvent e) {

				int x = canvas.offScreenX(e.getX());
				int y = canvas.offScreenY(e.getY());
				Clickedpoints[0] = x;
				Clickedpoints[1] = y;
				if (SwingUtilities.isLeftMouseButton(e) && !e.isShiftDown() && e.isAltDown()) {

					roiindex = roimanager.getRoiIndex(nearestRoiCurr);
					roimanager.select(roiindex);
					nearestRoiCurr.setStrokeColor(colorChange);

					imp.updateAndDraw();

				}

				if (SwingUtilities.isLeftMouseButton(e) && e.isShiftDown()) {
					if (!jFreeChartFrame.isVisible())
						jFreeChartFrame = utility.ChartMaker.display(chart, new Dimension(500, 500));

					displayclicked(rowchoice);
				}

			}

			@Override
			public void mousePressed(MouseEvent e) {

			}

			@Override
			public void mouseReleased(MouseEvent e) {

			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseExited(MouseEvent e) {

			}
		});

	}

	public void mark() {

		
		if (ml != null)
			imp.getCanvas().removeMouseMotionListener(ml);
		imp.getCanvas().addMouseMotionListener(ml = new MouseMotionListener() {

			final ImageCanvas canvas = imp.getWindow().getCanvas();
			Roi lastnearest = null;

			@Override
			public void mouseMoved(MouseEvent e) {

				int x = canvas.offScreenX(e.getX());
				int y = canvas.offScreenY(e.getY());

				final HashMap<Integer, double[]> loc = new HashMap<Integer, double[]>();

				loc.put(0, new double[] { x, y });

				Color roicolor;
				Roiobject currentobject;
				if (ZTRois.get(uniqueID) == null) {
					roicolor = defaultRois;

					currentobject = DefaultZTRois.entrySet().iterator().next().getValue();

				} else {
					roicolor = confirmedRois;

					currentobject = ZTRois.get(uniqueID);

				}
				nearestRoiCurr = NearestRoi.getNearestRois(currentobject, loc.get(0), InteractiveSimpleEllipseFit.this);

				if (nearestRoiCurr != null) {
					nearestRoiCurr.setStrokeColor(colorChange);

					if (lastnearest != nearestRoiCurr && lastnearest != null)
						lastnearest.setStrokeColor(roicolor);

					lastnearest = nearestRoiCurr;

					imp.updateAndDraw();
				}

				double distmin = Double.MAX_VALUE;
				if (tablesize > 0) {
					NumberFormat f = NumberFormat.getInstance();
					for (int row = 0; row < tablesize; ++row) {
						String CordX = (String) table.getValueAt(row, 1);
						String CordY = (String) table.getValueAt(row, 2);

						String CordZ = (String) table.getValueAt(row, 3);

						double dCordX = 0, dCordZ = 0, dCordY = 0;
						try {
							dCordX = f.parse(CordX).doubleValue();

							dCordY = f.parse(CordY).doubleValue();
							dCordZ = f.parse(CordZ).doubleValue();
						} catch (ParseException e1) {

						}
						double dist = Distance.DistanceSq(new double[] { dCordX, dCordY }, new double[] { x, y });
						if (Distance.DistanceSq(new double[] { dCordX, dCordY }, new double[] { x, y }) < distmin
								&& thirdDimension == (int) dCordZ && ndims > 3) {

							rowchoice = row;
							distmin = dist;

						}
						if (Distance.DistanceSq(new double[] { dCordX, dCordY }, new double[] { x, y }) < distmin
								&& ndims <= 3) {

							rowchoice = row;
							distmin = dist;

						}

					}

					table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
						@Override
						public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
								boolean hasFocus, int row, int col) {

							super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
							if (row == rowchoice) {
								setBackground(Color.green);

							} else {
								setBackground(Color.white);
							}
							return this;
						}
					});

					table.validate();
					scrollPane.validate();
					panelFirst.repaint();
					panelFirst.validate();

				}

			}

			@Override
			public void mouseDragged(MouseEvent e) {

			}

		});

	}

	public JFrame Cardframe = new JFrame("Ellipsoid detector");
	public JPanel panelCont = new JPanel();
	public JPanel panelFirst = new JPanel();
	public JPanel panelSecond = new JPanel();
	public JPanel Timeselect = new JPanel();
	public JPanel Zselect = new JPanel();
	public JPanel Roiselect = new JPanel();
	public JPanel Probselect = new JPanel();
	public JPanel Angleselect = new JPanel();
	public  JPanel KalmanPanel = new JPanel();
	public JCheckBox IlastikAuto = new JCheckBox("Ilastik Automated run", automode);

	public TextField inputFieldT, inputtrackField, minperimeterField, maxperimeterField;
	public TextField inputFieldZ;
	public TextField inputFieldmaxtry;
	public TextField inputFieldminpercent;
	public TextField inputFieldmaxellipse;

	public Label inputLabelmaxellipse;
	public Label inputLabelminpercent;
	public Label inputLabelIter, inputtrackLabel;
	public JPanel Original = new JPanel();
	public int SizeX = 500;
	public int SizeY = 300;

	public int smallSizeX = 200;
	public int smallSizeY = 200;

	public JButton Roibutton = new JButton("Confirm current roi selection");
	public JButton DisplayRoibutton = new JButton("Display roi selection");
	public JButton Anglebutton = new JButton("Intersection points and angles");
	public JButton Savebutton = new JButton("Save Track");
	public JButton Redobutton = new JButton("Recompute for current view");

	public Label timeText = new Label("Current T = " + 1, Label.CENTER);
	public Label zText = new Label("Current Z = " + 1, Label.CENTER);
	public Label zgenText = new Label("Current Z / T = " + 1, Label.CENTER);
	final Label rText = new Label("Alt+Left Click selects a Roi");
	final Label contText = new Label("After making all roi selections");
	final Label insideText = new Label("Cutoff distance for points belonging to ellipse = " + insideCutoff,
			Label.CENTER);
	final Label outsideText = new Label("Cutoff distance for points outside ellipse = " + outsideCutoff, Label.CENTER);

	final Label minperiText = new Label("Minimum ellipse perimeter" );
	final Label maxperiText = new Label("Maximum ellipse perimeter" );

	final Label lowprobText = new Label("Lower probability level = " + lowprob, Label.CENTER);
	final Label highporbText = new Label("Higher probability level = " + highprob, Label.CENTER);

	final String timestring = "Current T";
	final String zstring = "Current Z";
	final String zgenstring = "Current Z / T";
	final String rstring = "Radius";
	final String insidestring = "Cutoff distance for points inside ellipse";
	final String outsidestring = "Cutoff distance for points outside ellipse";

	final String lowprobstring = "Lower probability level";
	final String highprobstring = "Higher probability level";

	final String minperimeterstring = "Minimum ellipse perimeter";
	final String maxperimeterstring = "Maximum ellipse perimeter";

	public final Insets insets = new Insets(10, 0, 0, 0);
	public final GridBagLayout layout = new GridBagLayout();
	public final GridBagConstraints c = new GridBagConstraints();

	public JScrollPane scrollPane;
	public JFileChooser chooserA = new JFileChooser();
	public String choosertitleA;
	public JScrollBar timeslider = new JScrollBar(Scrollbar.HORIZONTAL, fourthDimensionsliderInit, 10, 0,
			scrollbarSize + 10);
	public JScrollBar zslider = new JScrollBar(Scrollbar.HORIZONTAL, thirdDimensionsliderInit, 10, 0,
			10 + scrollbarSize);
	public JScrollBar rslider = new JScrollBar(Scrollbar.HORIZONTAL, radiusInt, 10, 0, 10 + scrollbarSize);
	public JScrollBar insideslider = new JScrollBar(Scrollbar.HORIZONTAL, 0, 10, 0, 10 + scrollbarSize);
	public JScrollBar outsideslider = new JScrollBar(Scrollbar.HORIZONTAL, 0, 10, 0, 10 + scrollbarSize);

	public JScrollBar lowprobslider = new JScrollBar(Scrollbar.HORIZONTAL, 0, 10, 0, 10 + scrollbarSize);
	public JScrollBar highprobslider = new JScrollBar(Scrollbar.HORIZONTAL, 0, 10, 0, 10 + scrollbarSize);

	public JPanel PanelSelectFile = new JPanel();
	public Border selectfile = new CompoundBorder(new TitledBorder("Select Track"), new EmptyBorder(c.insets));
	public JLabel inputLabel = new JLabel("Filename:");
	public TextField inputField = new TextField();
	public final JButton ChooseDirectory = new JButton("Choose Directory to save results in");
	public JComboBox<String> ChooseMethod;
	public JComboBox<String> ChooseColor;
	public Label lostlabel;
	public TextField lostframe;
	public Border origborder = new CompoundBorder(new TitledBorder("Enter filename for results files"),
			new EmptyBorder(c.insets));
	JPanel controlprev = new JPanel();
	JPanel controlnext = new JPanel();
	final String alphastring = "Weightage for distance based cost";
	final String betastring = "Weightage for pixel ratio based cost";
	final String maxSearchstring = "Maximum search radius";
	final String maxSearchstringS = "Maximum search radius";
	final String initialSearchstring = "Initial search radius";

	Label maxSearchText = new Label(maxSearchstring + " = " + maxSearchInit, Label.CENTER);
	Label maxSearchTextS = new Label(maxSearchstring + " = " + maxSearchInit, Label.CENTER);
	Label iniSearchText = new Label(initialSearchstring + " = " + initialSearchradiusInit, Label.CENTER);
	Label alphaText = new Label(alphastring + " = " + alphaInit, Label.CENTER);
	Label betaText = new Label(betastring + " = " + betaInit, Label.CENTER);
	public void Card() {
		lostlabel = new Label("Allow link loosing for #frames");
		lostframe = new TextField(1);
		lostframe.setText(Integer.toString(maxframegap));
		CardLayout cl = new CardLayout();

		c.insets = new Insets(5, 5, 5, 5);
		panelCont.setLayout(cl);

		panelCont.add(panelFirst, "1");
		panelCont.add(panelSecond, "2");
		panelFirst.setName("Angle Tool for ellipsoids");

		panelFirst.setLayout(layout);

		panelSecond.setLayout(layout);
		
		Timeselect.setLayout(layout);
		controlprev.setLayout(layout);
		controlnext.setLayout(layout);
		Zselect.setLayout(layout);
		Original.setLayout(layout);
		Roiselect.setLayout(layout);
		Probselect.setLayout(layout);
		Angleselect.setLayout(layout);
		KalmanPanel.setLayout(layout);
		inputFieldZ = new TextField(5);
		inputFieldZ.setText(Integer.toString(thirdDimension));

		inputField.setColumns(10);

		inputFieldT = new TextField(5);
		inputFieldT.setText(Integer.toString(fourthDimension));

		inputtrackField = new TextField(5);

		inputFieldIter = new TextField(5);
		inputFieldIter.setText(Integer.toString(maxtry));

		minperimeterField = new TextField(5);
		minperimeterField.setText(Integer.toString(minperimeter));

		maxperimeterField = new TextField(5);
		maxperimeterField.setText(Integer.toString(maxperimeter));

		inputLabelIter = new Label("Max. attempts to find ellipses");
		final JScrollBar maxSearchS = new JScrollBar(Scrollbar.HORIZONTAL, maxSearchInit, 10, 0, 10 + scrollbarSize);
		final JScrollBar initialSearchS = new JScrollBar(Scrollbar.HORIZONTAL, initialSearchradiusInit, 10, 0,
				10 + scrollbarSize);
		final JScrollBar alphaS = new JScrollBar(Scrollbar.HORIZONTAL, alphaInit, 10, 0, 10 + scrollbarSize);
		final JScrollBar betaS = new JScrollBar(Scrollbar.HORIZONTAL, betaInit, 10, 0, 10 + scrollbarSize);

		maxSearchradius = utility.ScrollbarUtils.computeValueFromScrollbarPosition(maxSearchS.getValue(),
				maxSearchradiusMin, maxSearchradiusMax, scrollbarSize);
		initialSearchradius = utility.ScrollbarUtils.computeValueFromScrollbarPosition(initialSearchS.getValue(),
				initialSearchradiusMin, initialSearchradiusMax, scrollbarSize);
		alpha = utility.ScrollbarUtils.computeValueFromScrollbarPosition(alphaS.getValue(), alphaMin, alphaMax,
				scrollbarSize);
		beta = utility.ScrollbarUtils.computeValueFromScrollbarPosition(betaS.getValue(), betaMin, betaMax,
				scrollbarSize);

		String[] DrawType = { "Closed Loops", "Semi-Closed Loops" };

		ChooseMethod = new JComboBox<String>(DrawType);

		String[] DrawColor = { "Grey", "Red", "Blue", "Pink" };

		ChooseColor = new JComboBox<String>(DrawColor);

		inputLabelmaxellipse = new Label("Max. number of ellipses");
		inputtrackLabel = new Label("Enter trackID to save");

		inputFieldminpercent = new TextField(5);
		inputFieldminpercent.setText(Float.toString(minpercent));

		inputLabelminpercent = new Label("Min. percent points to lie on ellipse");

		Object[] colnames = new Object[] { "Track Id", "Location X", "Location Y",
				"Start Z" };

		Object[][] rowvalues = new Object[0][colnames.length];
		if (Finalresult != null && Finalresult.size() > 0) {

			rowvalues = new Object[Finalresult.size()][colnames.length];

		}

		table = new JTable(rowvalues, colnames);
		Border timeborder = new CompoundBorder(new TitledBorder("Select time"), new EmptyBorder(c.insets));
		Border zborder = new CompoundBorder(new TitledBorder("Select Z"), new EmptyBorder(c.insets));
		Border roitools = new CompoundBorder(new TitledBorder("Roi and ellipse finder tools"),
				new EmptyBorder(c.insets));
		Border origborder = new CompoundBorder(new TitledBorder("Enter filename for results files"),
				new EmptyBorder(c.insets));
		Border probborder = new CompoundBorder(new TitledBorder("Enter class probability range"),
				new EmptyBorder(c.insets));

		Border ellipsetools = new CompoundBorder(new TitledBorder("Ransac and Angle computer"),
				new EmptyBorder(c.insets));
		Border Kalmanborder = new CompoundBorder(new TitledBorder("Kalman Filter Search for angle tracking"),
				new EmptyBorder(c.insets));
		c.anchor = GridBagConstraints.BOTH;
		c.ipadx = 35;

		c.gridwidth = 10;
		c.gridheight = 10;
		c.gridy = 1;
		c.gridx = 0;

	

		// Put time slider

		Timeselect.add(timeText, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		Timeselect.add(timeslider, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		Timeselect.add(inputFieldT, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		Timeselect.setBorder(timeborder);
		panelFirst.add(Timeselect, new GridBagConstraints(0, 0, 5, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		// Put z slider
		if (ndims > 3)
			Zselect.add(zText, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, insets, 0, 0));
		else
			Zselect.add(zgenText, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, insets, 0, 0));
		Zselect.add(zslider, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		Zselect.add(inputFieldZ, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		Zselect.setBorder(zborder);
		panelFirst.add(Zselect, new GridBagConstraints(5, 0, 5, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		if (ndims < 4) {

			timeslider.setEnabled(false);
			inputFieldT.setEnabled(false);
		}
		if (ndims < 3) {

			zslider.setEnabled(false);
			inputFieldZ.setEnabled(false);
		}

		if (!automode) {
			Roiselect.add(rText, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
					GridBagConstraints.HORIZONTAL, insets, 0, 0));
			Roiselect.add(ChooseMethod, new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
			Roiselect.add(ChooseColor, new GridBagConstraints(3, 1, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

			Roiselect.add(Roibutton, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, insets, 0, 0));

			Roiselect.setBorder(roitools);
			Roiselect.setPreferredSize(new Dimension(SizeX, SizeY));
			panelFirst.add(Roiselect, new GridBagConstraints(0, 1, 5, 1, 0.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL, insets, 0, 0));
		}
		if (automode) {

			Probselect.add(lowprobText, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, insets, 0, 0));

			Probselect.add(lowprobslider, new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, insets, 0, 0));

			Probselect.add(highporbText, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, insets, 0, 0));

			Probselect.add(highprobslider, new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, insets, 0, 0));

			Probselect.add(ChooseMethod, new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
			Probselect.setPreferredSize(new Dimension(SizeX, SizeY));
			Probselect.setBorder(probborder);

			panelFirst.add(Probselect, new GridBagConstraints(0, 1, 5, 1, 0.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL, insets, 0, 0));

		}

		Angleselect.add(minperiText, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		Angleselect.add(minperimeterField, new GridBagConstraints(4, 0, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.RELATIVE, insets, 0, 0));

		Angleselect.add(maxperiText, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		Angleselect.add(maxperimeterField, new GridBagConstraints(4, 2, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.RELATIVE, insets, 0, 0));
		
		
		Angleselect.add(inputLabelIter, new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		Angleselect.add(inputFieldIter, new GridBagConstraints(4, 4, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.RELATIVE, insets, 0, 0));

		Angleselect.add(inputLabelminpercent, new GridBagConstraints(0, 5, 3, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		Angleselect.add(inputFieldminpercent, new GridBagConstraints(4, 5, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.RELATIVE, insets, 0, 0));
		
		Angleselect.add(insideText, new GridBagConstraints(0, 6, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		Angleselect.add(insideslider, new GridBagConstraints(0, 7, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		

		Angleselect.setBorder(ellipsetools);
		Angleselect.setPreferredSize(new Dimension(SizeX, SizeY));
		
		panelFirst.add(Angleselect, new GridBagConstraints(5, 1, 5, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		controlprev.add(new JButton(new AbstractAction("\u22b2Prev") {

			@Override
			public void actionPerformed(ActionEvent e) {
				CardLayout cl = (CardLayout) panelCont.getLayout();
				cl.previous(panelCont);
			}
		}));

		controlnext.add(new JButton(new AbstractAction("Next\u22b3") {

			@Override
			public void actionPerformed(ActionEvent e) {
				CardLayout cl = (CardLayout) panelCont.getLayout();
				cl.next(panelCont);
			}
		}));
		
		panelSecond.add(controlprev, new GridBagConstraints(3, 4, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.RELATIVE, new Insets(10, 10, 0, 10), 0, 0));
		
		KalmanPanel.add(iniSearchText, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		KalmanPanel.add(initialSearchS, new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));


		KalmanPanel.add(lostlabel, new GridBagConstraints(3, 0, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		KalmanPanel.add(lostframe, new GridBagConstraints(3, 1, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		KalmanPanel.add(maxSearchText, new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		KalmanPanel.add(maxSearchS, new GridBagConstraints(0, 5, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		
		KalmanPanel.add(Anglebutton, new GridBagConstraints(3, 3, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		KalmanPanel.add(Redobutton, new GridBagConstraints(5, 5, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		
		KalmanPanel.add(controlnext, new GridBagConstraints(5, 6, 10, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.RELATIVE, new Insets(10, 10, 0, 10), 0, 0));
		KalmanPanel.setBorder(Kalmanborder);
		
		KalmanPanel.setPreferredSize(new Dimension(SizeX, SizeY));
		panelFirst.add(KalmanPanel, new GridBagConstraints(0, 2, 10, 1, 0.0, 0.0, GridBagConstraints.ABOVE_BASELINE,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		
		
		table.setFillsViewportHeight(true);

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		scrollPane = new JScrollPane(table);

		scrollPane.getViewport().add(table);
		scrollPane.setAutoscrolls(true);

		PanelSelectFile.add(scrollPane, BorderLayout.CENTER);

		PanelSelectFile.setBorder(selectfile);

		panelSecond.add(PanelSelectFile, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.RELATIVE, new Insets(10, 10, 0, 10), 0, 0));

		Original.add(inputLabel, new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		Original.add(inputField, new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		Original.add(inputtrackLabel, new GridBagConstraints(0, 5, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		Original.add(inputtrackField, new GridBagConstraints(0, 6, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		Original.add(ChooseDirectory, new GridBagConstraints(0, 7, 3, 1, 0.0, 0.0, GridBagConstraints.NORTH,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		Original.add(Savebutton, new GridBagConstraints(0, 8, 3, 1, 0.0, 0.0, GridBagConstraints.NORTH,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		Original.setBorder(origborder);

		
		inputField.setEnabled(false);
		inputtrackField.setEnabled(false);
		ChooseDirectory.setEnabled(false);
		Savebutton.setEnabled(false);
		panelSecond.add(Original, new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		timeslider.addAdjustmentListener(new TimeListener(this, timeText, timestring, fourthDimensionsliderInit,
				fourthDimensionSize, scrollbarSize, timeslider));
		if (ndims > 3)
			zslider.addAdjustmentListener(new ZListener(this, zText, zstring, thirdDimensionsliderInit,
					thirdDimensionSize, scrollbarSize, zslider));
		else
			zslider.addAdjustmentListener(new ZListener(this, zgenText, zgenstring, thirdDimensionsliderInit,
					thirdDimensionSize, scrollbarSize, zslider));
		rslider.addAdjustmentListener(
				new RListener(this, rText, rstring, radiusMin, radiusMax, scrollbarSize, rslider));

		insideslider.addAdjustmentListener(new InsideCutoffListener(this, insideText, insidestring, insideCutoffmin,
				insideCutoffmax, scrollbarSize, insideslider));
		outsideslider.addAdjustmentListener(new OutsideCutoffListener(this, outsideText, outsidestring,
				outsideCutoffmin, outsideCutoffmax, scrollbarSize, outsideslider));
		
		Anglebutton.addActionListener(new AngleListener(this));
		Redobutton.addActionListener(new RedoListener(this));
		Roibutton.addActionListener(new RoiListener(this));
		inputFieldZ.addTextListener(new ZlocListener(this, false));
		minperimeterField.addTextListener(new MinperimeterListener(this));
		maxperimeterField.addTextListener(new MaxperimeterListener(this));
		inputFieldT.addTextListener(new TlocListener(this, false));
		inputtrackField.addTextListener(new TrackidListener(this));
		inputFieldminpercent.addTextListener(new MinpercentListener(this));
		inputFieldIter.addTextListener(new MaxTryListener(this));
		ChooseDirectory.addActionListener(new SaverDirectory(this));
		inputField.addTextListener(new ETrackFilenameListener(this));
		Savebutton.addActionListener(new SaveListener(this));
		ChooseMethod.addActionListener(new DrawListener(this, ChooseMethod));
		ChooseColor.addActionListener(new ColorListener(this, ChooseColor));
		panelFirst.setMinimumSize(new Dimension(SizeX, SizeY));
		IlastikAuto.addItemListener(new IlastikListener(this));
		lowprobslider.addAdjustmentListener(new LowProbListener(this, lowprobText, lowprobstring, lowprobmin,
				lowprobmax, scrollbarSize, lowprobslider));
		highprobslider.addAdjustmentListener(new HighProbListener(this, highporbText, highprobstring, highprobmin,
				highprobmax, scrollbarSize, highprobslider));
	
		maxSearchS.addAdjustmentListener(new ETrackMaxSearchListener(this, maxSearchText, maxSearchstring,
				maxSearchradiusMin, maxSearchradiusMax, scrollbarSize, maxSearchS));
		initialSearchS.addAdjustmentListener(new ETrackIniSearchListener(this, iniSearchText, initialSearchstring,
				initialSearchradiusMin, initialSearchradiusMax, scrollbarSize, initialSearchS));
		panelFirst.setVisible(true);
		cl.show(panelCont, "1");
		Cardframe.add(panelCont, "Center");
		Cardframe.add(jpb, "Last");

		Cardframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Cardframe.pack();
		Cardframe.setVisible(true);
		
	}

	public void displayclicked(int trackindex) {

		// Make something happen
		row = trackindex;
		String ID = (String) table.getValueAt(trackindex, 0);
		ArrayList<Pair<String, double[]>> currentresultAngle = new ArrayList<Pair<String, double[]>>();
		for (Pair<String, double[]> currentangle : resultAngle) {

			if (ID.equals(currentangle.getA())) {

				currentresultAngle.add(currentangle);

			}

		}

		this.dataset.removeAllSeries();

		this.dataset.addSeries(utility.ChartMaker.drawPoints(currentresultAngle));
		utility.ChartMaker.setColor(chart, 0, new Color(255, 64, 64));
		utility.ChartMaker.setStroke(chart, 0, 2f);
		updatePreview(ValueChange.RESULT);

	}

	public static int round(double value) {
		return (int) (value + 0.5D * Math.signum(value));
	}

	public static void main(String[] args) {

		new ImageJ();
		JFrame frame = new JFrame("");
		EllipseFileChooser panel = new EllipseFileChooser();

		frame.getContentPane().add(panel, "Center");
		frame.setSize(panel.getPreferredSize());
	}

}
