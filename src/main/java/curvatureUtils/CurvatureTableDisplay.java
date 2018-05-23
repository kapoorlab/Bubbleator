package curvatureUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.analysis.interpolation.BicubicInterpolator;
import org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.jfree.data.contour.DefaultContourDataset;

import ij.gui.Line;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import pluginTools.InteractiveSimpleEllipseFit;
import pluginTools.InteractiveSimpleEllipseFit.ValueChange;
import utility.ChartMaker;
import utility.Curvatureobject;

public class CurvatureTableDisplay {

	public static void displayclicked(InteractiveSimpleEllipseFit parent, int trackindex) {

		parent.overlay.clear();
		// Make something happen
		parent.row = trackindex;
		Integer ID = (Integer) parent.table.getValueAt(trackindex, 0);
		final ArrayList<Line> resultlineroi = new ArrayList<Line>();
		ArrayList<Curvatureobject> currentresultCurvature = new ArrayList<Curvatureobject>();
		for (ArrayList<Curvatureobject> Allcurrentcurvature : parent.AlllocalCurvature) {
			for (int index = 0; index < Allcurrentcurvature.size() - 1; ++index) {
				Curvatureobject currentcurvature = Allcurrentcurvature.get(index);
				Curvatureobject currentcurvaturenext = Allcurrentcurvature.get(index + 1);
				if (ID.equals(currentcurvature.Label) && parent.thirdDimension == currentcurvature.z
						&& parent.fourthDimension == currentcurvature.t) {

					currentresultCurvature.add(currentcurvature);
					Line currentline = new Line(currentcurvature.cord[0], currentcurvature.cord[1],
							currentcurvaturenext.cord[0], currentcurvaturenext.cord[1]);
					resultlineroi.add(currentline);
					parent.overlay.add(currentline);

				}

			}
		}

		
		

		if (parent.imp != null) {
			parent.imp.setOverlay(parent.overlay);
			parent.imp.updateAndDraw();
		}

		Double[] X = new Double[currentresultCurvature.size()];
		Double[] Y = new Double[currentresultCurvature.size()];
		Double[] Z = new Double[currentresultCurvature.size()];

		List<Pair<Double, Double>> linelist = new ArrayList<Pair<Double, Double>>();
		
		parent.contdataset.removeAllSeries();
		
		for (int index = 0; index < currentresultCurvature.size(); ++index) {

			X[index] = currentresultCurvature.get(index).cord[0];
			Y[index] = currentresultCurvature.get(index).cord[1];
			Z[index] = currentresultCurvature.get(index).radiusCurvature;

			linelist.add(new ValuePair<Double, Double>(X[index], Z[index]));
		}
        parent.contdataset.addSeries(ChartMaker.drawCurvePoints(linelist));
        parent.visdataset.initialize(X, Y, Z);
        
        
		Pair<Double, Double> minmaxX = RangePlot(currentresultCurvature, 0);
		Pair<Double, Double> minmaxY = RangePlot(currentresultCurvature, 1);


		parent.chart = utility.ChartMaker.makeChart(parent.contdataset,"Clockwise Curvature", "index", "Absolute Curvature" );
		parent.contchart = utility.ChartMaker.makeContourChart(parent.visdataset, "Curvature Measurment",
				minmaxX.getA() - 5, minmaxX.getB() + 5, minmaxY.getA() - 5, minmaxY.getB() + 5);
		parent.jFreeChartFrame.dispose();
		parent.jFreeChartFrame.repaint();
		parent.contjFreeChartFrame.dispose();
		parent.contjFreeChartFrame.repaint();

	}

	public static Pair<Double, Double> RangePlot(ArrayList<Curvatureobject> currentresultCurvature, int index) {

		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;

		for (Curvatureobject currentcurvature : currentresultCurvature) {

			if (currentcurvature.cord[index] < min)
				min = currentcurvature.cord[index];
			if (currentcurvature.cord[index] > max)
				max = currentcurvature.cord[index];

		}

		return new ValuePair<Double, Double>(min, max);

	}

}
