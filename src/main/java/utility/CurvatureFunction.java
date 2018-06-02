package utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import curvatureUtils.Node;
import drawUtils.DrawFunction;
import ellipsoidDetector.Distance;
import ij.ImagePlus;
import mpicbg.models.Point;
import net.imglib2.RealLocalizable;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import pluginTools.InteractiveSimpleEllipseFit;
import ransac.PointFunctionMatch.PointFunctionMatch;
import ransac.loadFiles.Tracking;
import ransacPoly.AbstractFunction2D;
import ransacPoly.LinearFunction;
import ransacPoly.QuadraticFunction;
import ransacPoly.RansacFunction;
import ransacPoly.RegressionFunction;
import ransacPoly.Sort;
import ransacPoly.Threepointfit;

public class CurvatureFunction {

	static int evendepth;

	/**
	 * 
	 * Take in a list of ordered co-ordinates and compute a curvature object
	 * containing the curvature information at each co-ordinate Makes a tree
	 * structure of the list
	 * 
	 * @param orderedtruths
	 * @param ndims
	 * @param Label
	 * @param t
	 * @param z
	 * @return
	 */
	public static ValuePair<ArrayList<RegressionFunction>, ArrayList<Curvatureobject>> getCurvature(
			InteractiveSimpleEllipseFit parent, List<RealLocalizable> truths, double maxError, int minNumInliers,
			int ndims, int Label, int t, int z) {

		ArrayList<Curvatureobject> curveobject = new ArrayList<Curvatureobject>();
		ArrayList<double[]> interpolatedCurvature = new ArrayList<double[]>();
		ArrayList<RegressionFunction> functions = new ArrayList<RegressionFunction>();

		double perimeter = 0;

		int maxdepth = Getdepth(parent);

		// Fill the node map
		MakeTree(parent, truths, 0, Integer.toString(0), maxdepth);

		for (Map.Entry<String, Node<RealLocalizable>> entry : parent.Nodemap.entrySet()) {

			System.out.println(entry.getValue().parent.size() + " Size of father");
			if (entry.getValue().parent.size() >= 0.75 * parent.minNumInliers
					&& entry.getValue().parent.size() <= 1.5 * parent.minNumInliers) {

				Node<RealLocalizable> node = entry.getValue();
				System.out.println(node.depth + " String");

				// Output is the local perimeter of the fitted function
				double perimeterlocal = FitonsubTree(parent, node, interpolatedCurvature, functions, maxError,
						minNumInliers);

				// Add local perimeters to get total perimeter of the curve
				perimeter += perimeterlocal;

			}

		}

		for (int indexx = 0; indexx < interpolatedCurvature.size(); ++indexx) {

			Curvatureobject currentobject = new Curvatureobject(interpolatedCurvature.get(indexx)[2], perimeter, Label,
					new double[] { interpolatedCurvature.get(indexx)[0], interpolatedCurvature.get(indexx)[1] }, t, z);

			curveobject.add(currentobject);

			// System.out.println("Kappa" + Math.abs(interpolatedCurvature.get(indexx)[2]) +
			// " " + perimeter + " "
			// + interpolatedCurvature.get(indexx)[0] + " " +
			// interpolatedCurvature.get(indexx)[1]);
		}

		return new ValuePair<ArrayList<RegressionFunction>, ArrayList<Curvatureobject>>(functions, curveobject);

	}

	public static int Getdepth(InteractiveSimpleEllipseFit parent) {

		int nearestk = (int) (Math.log10(parent.depth) / Math.log10(2));

		return nearestk;
	}

	public static int GetMaxStringsize(InteractiveSimpleEllipseFit parent) {

		Iterator<String> iter = parent.Nodemap.keySet().iterator();

		int maxlength = 0;

		while (iter.hasNext()) {

			String s = iter.next();
			if (s.length() > maxlength)
				maxlength = s.length();

		}

		return maxlength;

	}

	public static double FitonsubTree(InteractiveSimpleEllipseFit parent, Node<RealLocalizable> leaf,
			ArrayList<double[]> interpolatedCurvature, ArrayList<RegressionFunction> functions, double maxError,
			int minNumInliers) {

		List<RealLocalizable> Leftsubtruths = leaf.parent;

		// Fit function on left tree

		ArrayList<double[]> LeftCordlist = new ArrayList<double[]>();
		for (int i = 0; i < Leftsubtruths.size(); ++i) {

			LeftCordlist.add(new double[] { Leftsubtruths.get(i).getDoublePosition(0),
					Leftsubtruths.get(i).getDoublePosition(1) });

		}
		RegressionFunction Leftresultcurvature = getLocalcurvature(LeftCordlist, maxError, minNumInliers);

		// Draw the function

		functions.add(Leftresultcurvature);

		interpolatedCurvature.addAll(Leftresultcurvature.Curvaturepoints);

		double perimeter = Leftresultcurvature.Curvaturepoints.get(0)[3];
		return perimeter;

	}

	public static void MakeTree(InteractiveSimpleEllipseFit parent, final List<RealLocalizable> truths, int depthint,
			String depth, int maxdepth) {

		int size = truths.size();
		if (size <= parent.minNumInliers / 2)
			return;
		else {

			int splitindex;
			if (size % 2 == 0)
				splitindex = size / 2;
			else
				splitindex = (size - 1) / 2;

			final ArrayList<RealLocalizable> childA = new ArrayList<RealLocalizable>((int) size / 2);

			final ArrayList<RealLocalizable> childB = new ArrayList<RealLocalizable>((int) (size / 2 + size % 2));

			Iterator<RealLocalizable> iterator = truths.iterator();
			int index = 0;
			while (iterator.hasNext()) {

				iterator.next();

				if (index < splitindex)

					childA.add(truths.get(index));

				else

					childB.add(truths.get(index));

				index++;

			}

			Node<RealLocalizable> currentnode = new Node<RealLocalizable>(truths.get(splitindex), truths, childA,
					childB, depth);
			parent.Nodemap.put(depth, currentnode);

			depthint = depthint + 1;
			String depthleft = depth + Integer.toString(depthint) + "L";
			String depthright = depth + Integer.toString(depthint) + "R";

			MakeTree(parent, childA, depthint, depthleft, maxdepth);
			MakeTree(parent, childB, depthint, depthright, maxdepth);
		}

	}

	/**
	 * 
	 * Implementation of the curvature function to compute curvature at a point
	 * 
	 * @param previousCord
	 * @param currentCord
	 * @param nextCord
	 * @return
	 */

	public static RegressionFunction getLocalcurvature(ArrayList<double[]> Cordlist, double maxError,
			int minNumInliers) {

		double[] x = new double[Cordlist.size()];
		double[] y = new double[Cordlist.size()];

		ArrayList<Point> pointlist = new ArrayList<Point>();
		ArrayList<double[]> points = new ArrayList<double[]>();
		for (int index = 0; index < Cordlist.size() - 1; ++index) {
			x[index] = Cordlist.get(index)[0];
			y[index] = Cordlist.get(index)[1];

			points.add(new double[] { x[index], y[index] });
			pointlist.add(new Point(new double[] { x[index], y[index] }));

		}

		// Use Ransac to fit a quadratic function if it fails do it via regression

		RegressionFunction finalfunction = RansacBlock(pointlist, points, maxError, minNumInliers);

		return finalfunction;

	}

	/**
	 * 
	 * Fit a quadratic function via regression (not recommended, use Ransac instead)
	 * 
	 * @param points
	 * @return
	 */
	public static RegressionFunction RegressionBlock(ArrayList<double[]> points) {

		double[] x = new double[points.size()];
		double[] y = new double[points.size()];
		ArrayList<double[]> Curvaturepoints = new ArrayList<double[]>();

		for (int index = 0; index < points.size(); ++index) {

			x[index] = points.get(index)[0];

			y[index] = points.get(index)[1];

		}

		Threepointfit regression = new Threepointfit(x, y, 2);

		double highestCoeff = regression.GetCoefficients(2);
		double sechighestCoeff = regression.GetCoefficients(1);

		if (Math.abs(highestCoeff) > 1.0E5 && Math.abs(sechighestCoeff) > 1.0E5) {
			
			
			highestCoeff = 0;
			
			regression = new Threepointfit(x, y, 1);
			
			sechighestCoeff = regression.GetCoefficients(1);
			
		}
		double perimeter = 0.5;
		double Kappa = 0;
		for (int index = 0; index < points.size() - 1; ++index) {

			double dx = Math.abs(points.get(index)[0] - points.get(index + 1)[0]);
			double secderiv = 2 * highestCoeff;
			double firstderiv = 2 * highestCoeff * points.get(index)[0] + sechighestCoeff;

			Kappa += secderiv / Math.pow((1 + firstderiv * firstderiv), 3.0 / 2.0);

			perimeter += Math.sqrt(1 + firstderiv * firstderiv) * dx;

		}

		System.out.println(highestCoeff + " " + sechighestCoeff + " "  + perimeter + " lets c");
		for (int index = 0; index < points.size() - 1; ++index) {
			if (perimeter > 0)
				Curvaturepoints.add(new double[] { points.get(index)[0], points.get(index)[1],
						Math.abs(Kappa) / perimeter, perimeter });
		}
		RegressionFunction finalfunction = new RegressionFunction(regression, Curvaturepoints);
		return finalfunction;

	}

	/**
	 * 
	 * Fitting a quadratic or a linear function using Ransac
	 * 
	 * @param pointlist
	 * @param maxError
	 * @param minNumInliers
	 * @param maxDist
	 * @return
	 */

	public static RegressionFunction RansacBlock(final ArrayList<Point> pointlist, ArrayList<double[]> points,
			double maxError, int minNumInliers) {

		// Ransac block
		QuadraticFunction function = new QuadraticFunction();

		ArrayList<double[]> Curvaturepoints = new ArrayList<double[]>();

		System.out.println(pointlist.size() + " Input list size");
		final RansacFunction segment = Tracking.findQuadLinearFunction(pointlist, function, maxError, minNumInliers);

		if (segment != null) {
			double perimeter = 0;
			double Kappa = 0;

			double highestCoeff = segment.function.getCoefficient(2);
			double sechighestCoeff = segment.function.getCoefficient(1);

			for (int index = 0; index < segment.inliers.size() - 1; ++index) {

				PointFunctionMatch p = segment.inliers.get(index);
				PointFunctionMatch pnext = segment.inliers.get(index + 1);

				double dx = Math.abs(p.getP1().getW()[0] - pnext.getP1().getW()[0]);
				double secderiv = 2 * highestCoeff;
				double firstderiv = 2 * highestCoeff * p.getP1().getW()[0] + sechighestCoeff;
				Kappa += secderiv / Math.pow((1 + firstderiv * firstderiv), 3.0 / 2.0);
				perimeter += Math.sqrt(1 + firstderiv * firstderiv) * dx;

			}
			for (int index = 0; index < segment.inliers.size() - 1; ++index) {
				PointFunctionMatch p = segment.inliers.get(index);
				if (perimeter > 0)
					Curvaturepoints.add(new double[] { p.getP1().getW()[0], p.getP1().getW()[1],
							Math.abs(Kappa) / perimeter, perimeter });
			}
			RegressionFunction finalfunction = new RegressionFunction(segment.function, Curvaturepoints,
					segment.inliers, segment.candidates);
			return finalfunction;
		}

		else {
			System.out.println("Ransac failed, fitting via regression ");
			RegressionFunction finalfunction = RegressionBlock(points);
			return finalfunction;
		}

	}

	/**
	 * 
	 * Interpolate from (x, y) to (x, y) + 1 by filling up the values in between
	 * 
	 */

	public static Pair<double[], double[]> InterpolateValues(final double[] Xcurr, final double[] Xnext,
			Threepointfit regression) {

		double minX = Xcurr[0] < Xnext[0] ? Xcurr[0] : Xnext[0];
		double maxX = Xcurr[0] > Xnext[0] ? Xcurr[0] : Xnext[0];

		double interpolant = 0.1;
		double X = minX;
		double Y = regression.predict(X);

		int steps = (int) ((maxX - minX) / interpolant);
		if (steps > 0) {
			double[] returnValX = new double[steps];
			double[] returnValY = new double[steps];

			returnValX[0] = X;
			returnValY[0] = Y;

			for (int i = 1; i < steps; ++i) {

				returnValX[i] = X + i * interpolant;
				returnValY[i] = regression.predict(returnValX[i]);

			}

			Pair<double[], double[]> interpolXY = new ValuePair<double[], double[]>(returnValX, returnValY);

			return interpolXY;
		}

		else {
			Pair<double[], double[]> interpolXY = new ValuePair<double[], double[]>(new double[] { X, Y },
					new double[] { X, Y });

			return interpolXY;

		}
	}

	/**
	 * 
	 * Evenly or unevenly spaced data derivative is computed via Lagrangian
	 * interpolation
	 * 
	 * @param previousCord
	 * @param currentCord
	 * @param nextCord
	 */
	public static double[] InterpolatedFirstderiv(double[] previousCord, double[] currentCord, double[] nextCord) {

		double y0 = previousCord[1];
		double y1 = currentCord[1];
		double y2 = nextCord[1];

		double x0 = previousCord[0];
		double x1 = currentCord[0];
		double x2 = nextCord[0];

		double x01 = x0 - x1;
		double x02 = x0 - x2;
		double x12 = x1 - x2;
		if (x01 != 0 && x02 != 0 && x12 != 0) {
			double diffatx0 = y0 * (x01 + x02) / (x01 * x02) - y1 * x02 / (x01 * x12) + y2 * x01 / (x02 * x12);
			double diffatx2 = -y0 * x12 / (x01 * x02) + y1 * x02 / (x01 * x12) - y2 * (x02 + x12) / (x02 * x12);
			double diffatx1 = y0 * (x12) / (x01 * x02) + y1 * (1.0 / x12 - 1.0 / x01) - y2 * x01 / (x02 * x12);

			double[] threepointdiff = { diffatx0, diffatx1, diffatx2 };

			return threepointdiff;
		} else

			return new double[] { 0, 0, 0 };

	}

	/**
	 * 
	 * Compute perimeter of a curve by adding up the distance between ordered set of
	 * points
	 * 
	 * @param orderedtruths
	 * @param ndims
	 * @return
	 */
	public static double getPerimeter(List<RealLocalizable> orderedtruths, int ndims) {

		double perimeter = 0;
		for (int index = 1; index < orderedtruths.size(); ++index) {

			double[] lastpoint = new double[ndims];
			double[] currentpoint = new double[ndims];

			orderedtruths.get(index - 1).localize(lastpoint);
			orderedtruths.get(index).localize(currentpoint);
			perimeter += Distance.DistanceSq(lastpoint, currentpoint);

		}

		return perimeter;
	}

}
