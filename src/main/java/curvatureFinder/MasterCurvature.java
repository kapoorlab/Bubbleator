package curvatureFinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import curvatureUtils.PointExtractor;
import ellipsoidDetector.Distance;
import ellipsoidDetector.Intersectionobject;
import ij.IJ;
import ij.gui.Line;
import kalmanForSegments.Segmentobject;
import mpicbg.models.Point;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.ransac.RansacModels.FitLocalEllipsoid;
import net.imglib2.algorithm.ransac.RansacModels.RansacFunctionEllipsoid;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.algorithm.region.hypersphere.HyperSphereCursor;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import pluginTools.InteractiveSimpleEllipseFit;
import pluginTools.RegressionCurveSegment;
import ransacPoly.RegressionFunction;
import utility.Curvatureobject;
import utility.Listordereing;
import utility.Roiobject;

public abstract class MasterCurvature<T extends RealType<T> & NativeType<T>> implements CurvatureFinders<T> {

	/**
	 * 
	 * Function to fit on a list of points which are not tree based
	 * 
	 * @param parent
	 * @param centerpoint
	 * @param sublist
	 * @param functions
	 * @param interpolatedCurvature
	 * @param smoothing
	 * @param maxError
	 * @param minNumInliers
	 * @param degree
	 * @param secdegree
	 * @return
	 */

	static int xindex = 0;
	static int yindex = 1;
	static int curveindex = 2;
	static int periindex = 3;
	static int intensityAindex = 4;
	static int intensityBindex = 5;

	public RegressionLineProfile FitonList(InteractiveSimpleEllipseFit parent,
			RealLocalizable centerpoint, List<RealLocalizable> sublist, int strideindex) {

		ArrayList<double[]> Cordlist = new ArrayList<double[]>();

		for (int i = 0; i < sublist.size(); ++i) {

			Cordlist.add(new double[] { sublist.get(i).getDoublePosition(xindex),
					sublist.get(i).getDoublePosition(yindex) });
		}

		RegressionLineProfile resultcurvature = getLocalcurvature(Cordlist, centerpoint, strideindex);

		// Draw the function

		
		return resultcurvature;

	}
	
	public RegressionLineProfile FitCircleonList(InteractiveSimpleEllipseFit parent,
			RealLocalizable centerpoint, List<RealLocalizable> sublist, int strideindex) {

		ArrayList<double[]> Cordlist = new ArrayList<double[]>();

		for (int i = 0; i < sublist.size(); ++i) {

			Cordlist.add(new double[] { sublist.get(i).getDoublePosition(xindex),
					sublist.get(i).getDoublePosition(yindex) });
		}

		RegressionLineProfile resultcurvature = getCircleLocalcurvature(Cordlist, centerpoint, strideindex);

		// Draw the function

		
		return resultcurvature;

	}

	

	public RegressionCurveSegment CommonLoop(InteractiveSimpleEllipseFit parent, List<RealLocalizable> Ordered,
			RealLocalizable centerpoint, int ndims, int celllabel, int t, int z) {

		// Get the sparse list of points
		HashMap<Integer, RegressionCurveSegment> Bestdelta = new HashMap<Integer, RegressionCurveSegment>();

		int i = parent.increment;

		// Get the sparse list of points

		List<RealLocalizable> allorderedtruths = Listordereing.getList(Ordered, i);

		if (parent.fourthDimensionSize > 1)
			parent.timeslider.setValue(utility.Slicer.computeScrollbarPositionFromValue(parent.fourthDimension,
					parent.fourthDimensionsliderInit, parent.fourthDimensionSize, parent.scrollbarSize));
		parent.zslider.setValue(utility.Slicer.computeScrollbarPositionFromValue(parent.thirdDimension,
				parent.thirdDimensionsliderInit, parent.thirdDimensionSize, parent.scrollbarSize));

		RegressionCurveSegment resultpair = getCurvatureLineScan(parent, allorderedtruths, centerpoint, ndims, celllabel, z, t);

		
		
		
		// Here counter the segments where the number of inliers was too low
		Bestdelta.put(0, resultpair);

		parent.localCurvature = resultpair.Curvelist;

		parent.functions = resultpair.functionlist;
		return resultpair;

	}

	
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
	 * @param strideindex 
	 * @return
	 */
	public RegressionCurveSegment getCurvature(InteractiveSimpleEllipseFit parent, List<RealLocalizable> truths,
			RealLocalizable centerpoint, int ndims, int Label, int z, int t, int strideindex) {

		ArrayList<Curvatureobject> curveobject = new ArrayList<Curvatureobject>();

		ArrayList<double[]> totalinterpolatedCurvature = new ArrayList<double[]>();

		ArrayList<RegressionFunction> totalfunctions = new ArrayList<RegressionFunction>();

		
		
		double perimeter = 0;

		MakeSegments(parent, truths, parent.minNumInliers, Label);
		// Now do the fitting
		for (Map.Entry<Integer, List<RealLocalizable>> entry : parent.Listmap.entrySet()) {

			List<RealLocalizable> sublist = entry.getValue();
			/***
			 * 
			 * Main method that fits on segments a function to get the curvature
			 * 
			 */
			RegressionLineProfile localfunction = FitonList(parent, centerpoint, sublist, strideindex);

			perimeter += localfunction.regfunc.Curvaturepoints.get(0)[periindex];
			totalfunctions.add(localfunction.regfunc);
			totalinterpolatedCurvature.addAll(localfunction.AllCurvaturepoints);
			double Curvature = localfunction.AllCurvaturepoints.get(0)[curveindex];
			double IntensityA = localfunction.AllCurvaturepoints.get(0)[intensityAindex];
			double IntensityB = localfunction.AllCurvaturepoints.get(0)[intensityBindex];
			ArrayList<double[]> curvelist = new ArrayList<double[]>();

			curvelist.add(new double[] { centerpoint.getDoublePosition(xindex), centerpoint.getDoublePosition(yindex),
					Curvature, IntensityA, IntensityB });
		
		}

		for (int indexx = 0; indexx < totalinterpolatedCurvature.size(); ++indexx) {

			Curvatureobject currentobject = new Curvatureobject(totalinterpolatedCurvature.get(indexx)[curveindex],
					perimeter, totalinterpolatedCurvature.get(indexx)[intensityAindex],
					totalinterpolatedCurvature.get(indexx)[intensityBindex], Label,
					new double[] { totalinterpolatedCurvature.get(indexx)[xindex],
							totalinterpolatedCurvature.get(indexx)[yindex] },
					z, t);

			curveobject.add(currentobject);

		}

		// All nodes are returned

	
		RegressionCurveSegment returnSeg =  new RegressionCurveSegment(totalfunctions, curveobject);
		
		return returnSeg;

	}

	
	
	public RegressionCurveSegment getCurvatureLineScan(InteractiveSimpleEllipseFit parent, List<RealLocalizable> truths,
			RealLocalizable centerpoint, int ndims, int Label, int z, int t) {

		ArrayList<Curvatureobject> curveobject = new ArrayList<Curvatureobject>();

		ArrayList<double[]> totalinterpolatedCurvature = new ArrayList<double[]>();

		ArrayList<RegressionFunction> totalfunctions = new ArrayList<RegressionFunction>();

		ArrayList<LineProfileCircle> totalscan = new ArrayList<LineProfileCircle>();
		
		double perimeter = 0;

		MakeSegments(parent, truths, parent.minNumInliers, Label);
		
		// Now do the fitting
		for (Map.Entry<Integer, List<RealLocalizable>> entry : parent.Listmap.entrySet()) {

			List<RealLocalizable> sublist = entry.getValue();
			/***
			 * 
			 * Main method that fits on segments a function to get the curvature
			 * 
			 */
			RegressionLineProfile localfunction = FitCircleonList(parent, centerpoint, sublist, 0);

			if (localfunction.LineScanIntensity.size() > 0) {
				
			
				
			if(totalscan.size() == 0) {
				
			
				totalscan = localfunction.LineScanIntensity;
			
			}
			else {
				for (int indexx = 0; indexx< totalscan.size(); ++indexx) {
					for (int index = 0; index< localfunction.LineScanIntensity.size(); ++index) {
						
					
					if(totalscan.get(indexx).count == localfunction.LineScanIntensity.get(index).count) {
						
						LineProfileCircle currentscan = new LineProfileCircle(totalscan.get(indexx).count, totalscan.get(indexx).intensity + localfunction.LineScanIntensity.get(index).intensity , 
								
								totalscan.get(indexx).secintensity + localfunction.LineScanIntensity.get(index).secintensity );
						
						totalscan.set(indexx, currentscan);
						
					}
					
					}
					
				}
				
			}
			
			}
			perimeter += localfunction.regfunc.Curvaturepoints.get(0)[periindex];
			totalfunctions.add(localfunction.regfunc);
			totalinterpolatedCurvature.addAll(localfunction.AllCurvaturepoints);
			double Curvature = localfunction.AllCurvaturepoints.get(0)[curveindex];
			double IntensityA = localfunction.AllCurvaturepoints.get(0)[intensityAindex];
			double IntensityB = localfunction.AllCurvaturepoints.get(0)[intensityBindex];
			ArrayList<double[]> curvelist = new ArrayList<double[]>();

			curvelist.add(new double[] { centerpoint.getDoublePosition(xindex), centerpoint.getDoublePosition(yindex),
					Curvature, IntensityA, IntensityB });
		
		}

		for (int indexx = 0; indexx < totalinterpolatedCurvature.size(); ++indexx) {

			Curvatureobject currentobject = new Curvatureobject(totalinterpolatedCurvature.get(indexx)[curveindex],
					perimeter, totalinterpolatedCurvature.get(indexx)[intensityAindex],
					totalinterpolatedCurvature.get(indexx)[intensityBindex], Label,
					new double[] { totalinterpolatedCurvature.get(indexx)[xindex],
							totalinterpolatedCurvature.get(indexx)[yindex] },
					z, t);

			curveobject.add(currentobject);

		}

		// All nodes are returned

		RegressionCurveSegment returnSeg = null;
		if(totalscan.size() == 0)
			returnSeg =  new RegressionCurveSegment(totalfunctions, curveobject);
		else
		returnSeg = new RegressionCurveSegment(totalfunctions, curveobject, totalscan);
		return returnSeg;

	}
	
	public Pair<Intersectionobject, Intersectionobject> GetAverage(InteractiveSimpleEllipseFit parent,
			RealLocalizable centerpoint, HashMap<Integer, RegressionCurveSegment> Bestdelta, int count) {

		RegressionCurveSegment resultpair = Bestdelta.get(0);
		ArrayList<Curvatureobject> RefinedCurvature = new ArrayList<Curvatureobject>();
		ArrayList<Curvatureobject> localCurvature = resultpair.Curvelist;

		double[] X = new double[localCurvature.size()];
		double[] Y = new double[localCurvature.size()];
		double[] Z = new double[localCurvature.size()];
		double[] I = new double[localCurvature.size()];
		double[] ISec = new double[localCurvature.size()];

		for (int index = 0; index < localCurvature.size(); ++index) {

			ArrayList<Double> CurveXY = new ArrayList<Double>();
			ArrayList<Double> CurveI = new ArrayList<Double>();
			ArrayList<Double> CurveISec = new ArrayList<Double>();

			X[index] = localCurvature.get(index).cord[0];
			Y[index] = localCurvature.get(index).cord[1];
			Z[index] = localCurvature.get(index).radiusCurvature;
			I[index] = localCurvature.get(index).Intensity;
			ISec[index] = localCurvature.get(index).SecIntensity;

			CurveXY.add(Z[index]);
			CurveI.add(I[index]);
			CurveISec.add(ISec[index]);

			for (int secindex = 1; secindex < count; ++secindex) {

				RegressionCurveSegment testpair = Bestdelta.get(secindex);

				ArrayList<Curvatureobject> testlocalCurvature = testpair.Curvelist;

				double[] Xtest = new double[testlocalCurvature.size()];
				double[] Ytest = new double[testlocalCurvature.size()];
				double[] Ztest = new double[testlocalCurvature.size()];
				double[] Itest = new double[testlocalCurvature.size()];
				double[] ISectest = new double[testlocalCurvature.size()];

				for (int testindex = 0; testindex < testlocalCurvature.size(); ++testindex) {

					Xtest[testindex] = testlocalCurvature.get(testindex).cord[0];
					Ytest[testindex] = testlocalCurvature.get(testindex).cord[1];
					Ztest[testindex] = testlocalCurvature.get(testindex).radiusCurvature;
					Itest[testindex] = testlocalCurvature.get(testindex).Intensity;
					ISectest[testindex] = testlocalCurvature.get(testindex).SecIntensity;

					if (X[index] == Xtest[testindex] && Y[index] == Ytest[testindex]) {

						CurveXY.add(Ztest[testindex]);
						CurveI.add(Itest[testindex]);
						CurveISec.add(ISectest[testindex]);

					}

				}

			}

			double frequdeltaperi = localCurvature.get(0).perimeter;
			double frequdelta = Z[index];
			double intensitydelta = I[index];
			double intensitySecdelta = ISec[index];

			Iterator<Double> setiter = CurveXY.iterator();
			while (setiter.hasNext()) {

				Double s = setiter.next();

				frequdelta += s;

			}

			frequdelta /= CurveXY.size();

			Iterator<Double> Iiter = CurveI.iterator();
			while (Iiter.hasNext()) {

				Double s = Iiter.next();

				intensitydelta += s;

			}

			intensitydelta /= CurveI.size();

			Iterator<Double> ISeciter = CurveISec.iterator();
			while (ISeciter.hasNext()) {

				Double s = ISeciter.next();

				intensitySecdelta += s;

			}

			intensitySecdelta /= CurveISec.size();

			Curvatureobject newobject = new Curvatureobject((float) frequdelta, frequdeltaperi, intensitydelta,
					intensitySecdelta, localCurvature.get(index).Label, localCurvature.get(index).cord,
					localCurvature.get(index).t, localCurvature.get(index).z);

			RefinedCurvature.add(newobject);
		}

		Pair<ArrayList<RegressionFunction>, ArrayList<Curvatureobject>> Refinedresultpair = new ValuePair<ArrayList<RegressionFunction>, ArrayList<Curvatureobject>>(
				resultpair.functionlist, RefinedCurvature);
		parent.localCurvature = Refinedresultpair.getB();
		parent.functions.addAll(Refinedresultpair.getA());
		// Make intersection object here

		Pair<Intersectionobject, Intersectionobject> currentobjectpair = PointExtractor.CurvaturetoIntersection(parent,
				parent.localCurvature, parent.functions, resultpair.LineScanIntensity, centerpoint, parent.smoothing);
		Intersectionobject densecurrentobject = currentobjectpair.getA();
		Intersectionobject sparsecurrentobject = currentobjectpair.getB();

		return new ValuePair<Intersectionobject, Intersectionobject>(sparsecurrentobject, densecurrentobject);
	}

	public Pair<Intersectionobject, Intersectionobject> GetSingle(InteractiveSimpleEllipseFit parent,
			RealLocalizable centerpoint, HashMap<Integer, RegressionCurveSegment> Bestdelta) {

		RegressionCurveSegment resultpair = Bestdelta.get(0);
		ArrayList<Curvatureobject> RefinedCurvature = new ArrayList<Curvatureobject>();
		ArrayList<Curvatureobject> localCurvature = resultpair.Curvelist;

		double[] X = new double[localCurvature.size()];
		double[] Y = new double[localCurvature.size()];
		double[] Z = new double[localCurvature.size()];
		double[] I = new double[localCurvature.size()];
		double[] ISec = new double[localCurvature.size()];

		for (int index = 0; index < localCurvature.size(); ++index) {

			ArrayList<Double> CurveXY = new ArrayList<Double>();
			ArrayList<Double> CurveI = new ArrayList<Double>();
			ArrayList<Double> CurveISec = new ArrayList<Double>();

			X[index] = localCurvature.get(index).cord[0];
			Y[index] = localCurvature.get(index).cord[1];
			Z[index] = localCurvature.get(index).radiusCurvature;
			I[index] = localCurvature.get(index).Intensity;
			ISec[index] = localCurvature.get(index).SecIntensity;

			CurveXY.add(Z[index]);
			CurveI.add(I[index]);
			CurveISec.add(ISec[index]);

			double frequdeltaperi = localCurvature.get(0).perimeter;
			double frequdelta = Z[index];
			double intensitydelta = I[index];
			double intensitySecdelta = ISec[index];

			Iterator<Double> setiter = CurveXY.iterator();
			while (setiter.hasNext()) {

				Double s = setiter.next();

				frequdelta += s;

			}

			frequdelta /= CurveXY.size();

			Iterator<Double> Iiter = CurveI.iterator();
			while (Iiter.hasNext()) {

				Double s = Iiter.next();

				intensitydelta += s;

			}

			intensitydelta /= CurveI.size();

			Iterator<Double> ISeciter = CurveISec.iterator();
			while (ISeciter.hasNext()) {

				Double s = ISeciter.next();

				intensitySecdelta += s;

			}

			intensitySecdelta /= CurveISec.size();

			Curvatureobject newobject = new Curvatureobject((float) frequdelta, frequdeltaperi, intensitydelta,
					intensitySecdelta, localCurvature.get(index).Label, localCurvature.get(index).cord,
					localCurvature.get(index).t, localCurvature.get(index).z);

			RefinedCurvature.add(newobject);
		}

		Pair<ArrayList<RegressionFunction>, ArrayList<Curvatureobject>> Refinedresultpair = new ValuePair<ArrayList<RegressionFunction>, ArrayList<Curvatureobject>>(
				resultpair.functionlist, RefinedCurvature);
		parent.localCurvature = Refinedresultpair.getB();
		parent.functions.addAll(Refinedresultpair.getA());
		// Make intersection object here

		Pair<Intersectionobject, Intersectionobject> currentobjectpair = PointExtractor.CurvaturetoIntersection(parent,
				parent.localCurvature, parent.functions, resultpair.LineScanIntensity, centerpoint, parent.smoothing);
		Intersectionobject densecurrentobject = currentobjectpair.getA();
		Intersectionobject sparsecurrentobject = currentobjectpair.getB();

		return new ValuePair<Intersectionobject, Intersectionobject>(sparsecurrentobject, densecurrentobject);
	}

	public void MakeSegments(InteractiveSimpleEllipseFit parent, final List<RealLocalizable> truths, int numSeg,
			int celllabel) {

		List<RealLocalizable> copytruths = new ArrayList<RealLocalizable>(truths);
		if (truths.size() < 3)
			return;
		else {
			int size = truths.size();

			
			
			int maxpoints = size / numSeg;
			if (maxpoints <= 2)
				maxpoints = 3;
			int biggestsize = maxpoints;
			int segmentLabel = 1;

			int index = truths.size() - 1;
			do {
				
				if(index >= truths.size())
					index = 0;
				
				
			copytruths.add(truths.get(index));
			
			  index++;
			
			} while(copytruths.size() % numSeg!= 0);
			
			size = copytruths.size();
			maxpoints = size / numSeg;
			if (maxpoints <= 2)
				maxpoints = 3;
			
			List<RealLocalizable> sublist = new ArrayList<RealLocalizable>();

			int startindex = 0;
			int endindex = startindex + maxpoints;

			while (true) {

			
				
			

				sublist = copytruths.subList(startindex, Math.min(endindex, size));
				parent.Listmap.put(segmentLabel, sublist);

				if (biggestsize >= endindex - startindex)
					biggestsize = endindex - startindex;

				parent.CellLabelsizemap.put(celllabel, biggestsize);
				segmentLabel++;

				startindex = endindex;
				endindex = startindex + maxpoints;

				if (startindex >= size - 1)
					break;
				
			}

		}

	}

	/**
	 * Obtain intensity in the user defined
	 * 
	 * @param point
	 * @return
	 */

	public Pair<Double, Double> getIntensity(InteractiveSimpleEllipseFit parent, Localizable point,
			Localizable centerpoint) {

		RandomAccess<FloatType> ranac = parent.CurrentViewOrig.randomAccess();

		double Intensity = 0;
		double IntensitySec = 0;
		RandomAccess<FloatType> ranacsec;
		if (parent.CurrentViewSecOrig != null)
			ranacsec = parent.CurrentViewSecOrig.randomAccess();
		else
			ranacsec = ranac;

		ranac.setPosition(point);
		ranacsec.setPosition(ranac);
		double mindistance = getDistance(point, centerpoint);
		double[] currentPosition = new double[point.numDimensions()];

		HyperSphere<FloatType> hyperSphere = new HyperSphere<FloatType>(parent.CurrentViewOrig, ranac,
				(int) parent.insidedistance);
		HyperSphereCursor<FloatType> localcursor = hyperSphere.localizingCursor();
		int Area = 1;
		while (localcursor.hasNext()) {

			localcursor.fwd();

			ranacsec.setPosition(localcursor);

			ranacsec.localize(currentPosition);

			double currentdistance = getDistance(localcursor, centerpoint);
			if ((currentdistance - mindistance) <= parent.insidedistance) {
				Intensity += localcursor.get().getRealDouble();
				IntensitySec += ranacsec.get().getRealDouble();
				Area++;
			}
		}

		return new ValuePair<Double, Double>(Intensity / Area, IntensitySec / Area);
	}

	
	
	/**
	 * 
	 * Fit an ellipse to a bunch of points
	 * 
	 * @param pointlist
	 * @param ndims
	 * @return
	 */

	public RegressionLineProfile RansacEllipseBlock(final InteractiveSimpleEllipseFit parent, final ArrayList<RealLocalizable> pointlist,
			RealLocalizable centerpoint, int ndims, int strideindex) {

		final RansacFunctionEllipsoid ellipsesegment = FitLocalEllipsoid.findLocalEllipsoid(pointlist, ndims);

		
		
		
		double Kappa = 0;
		double perimeter = 0;
		ArrayList<double[]> Curvaturepoints = new ArrayList<double[]>();

		ArrayList<double[]> AllCurvaturepoints = new ArrayList<double[]>();

		double radii = ellipsesegment.function.getRadii();
		double[] newpos = new double[ndims];
		long[] longnewpos = new long[ndims];
		for (int i = 0; i < pointlist.size() - 1; ++i) {
			
			perimeter += Distance.DistanceSqrt(pointlist.get(i), pointlist.get(i + 1));
				
			}
			
			perimeter = perimeter * parent.calibration;
		int size = pointlist.size();
		final double[] pointA = new double[ndims];
		final double[] pointB = new double[ndims];
		final double[] pointC = new double[ndims];
		long[] longpointB = new long[ndims];
		
		double meanIntensity = 0;
		double meanSecIntensity = 0;
		int splitindex;
		if (size % 2 == 0)
			splitindex = size / 2;
		else
			splitindex = (size - 1) / 2;

		for (int i = 0; i < ndims; ++i) {
			pointA[i] = pointlist.get(0).getDoublePosition(i);
			pointB[i] = pointlist.get(splitindex).getDoublePosition(i);
			pointC[i] = pointlist.get(size - 1).getDoublePosition(i);

		}
		
		long[] centerloc = new long[] { (long) centerpoint.getDoublePosition(0),
				(long) centerpoint.getDoublePosition(1) };
		net.imglib2.Point centpos = new net.imglib2.Point(centerloc);
		
		
		for (RealLocalizable point : pointlist) {

			point.localize(newpos);

			Kappa = 1.0 / (radii * parent.calibration);
			for (int d = 0; d < newpos.length; ++d)
				longnewpos[d] = (long) newpos[d];
			net.imglib2.Point intpoint = new net.imglib2.Point(longnewpos);

			

			Pair<Double, Double> Intensity = getIntensity(parent, intpoint, centpos);

			
				
			// Average the intensity.
			meanIntensity += Intensity.getA();
			meanSecIntensity += Intensity.getB();

			AllCurvaturepoints.add(new double[] { newpos[0], newpos[1], Math.max(0,Kappa), perimeter, Intensity.getA(),
					Intensity.getB() });
		}
		meanIntensity /= size;
		meanSecIntensity /= size;
		Curvaturepoints.add(
				new double[] { pointB[0], pointB[1], Math.max(0,Kappa), perimeter, meanIntensity, meanSecIntensity });

		RegressionFunction finalfunctionransac = new RegressionFunction(ellipsesegment.function, Curvaturepoints);

		ArrayList<LineProfileCircle> LineScanIntensity = new ArrayList<LineProfileCircle>();
	
			if (strideindex == 0) {
			for (int d = 0; d < newpos.length; ++d)
				longpointB[d] = (long) pointB[d];
			net.imglib2.Point intpoint = new net.imglib2.Point(longpointB);
			
			
		LinefunctionCircle NormalLine = new LinefunctionCircle(ellipsesegment.function, intpoint);
		
		double[] NormalSlopeIntercept = NormalLine.NormalatPoint();
		
		double startNormalX = intpoint.getDoublePosition(0) - parent.insidedistance/Math.sqrt(1 + NormalSlopeIntercept[0]*NormalSlopeIntercept[0]) ;
		double startNormalY = NormalSlopeIntercept[0] * startNormalX + NormalSlopeIntercept[1];
		
		double endNormalX = intpoint.getDoublePosition(0) + parent.insidedistance/Math.sqrt(1 + NormalSlopeIntercept[0]*NormalSlopeIntercept[0]) ;
		double endNormalY = NormalSlopeIntercept[0] * endNormalX + NormalSlopeIntercept[1];
		
		long[] startNormal = { (long)startNormalX, (long)startNormalY };
		
		long[] midNormal = {(long)intpoint.getDoublePosition(0), (long)intpoint.getDoublePosition(1)};
		
		long[] endNormal = { (long)endNormalX, (long)endNormalY};
		
		
		Line line = new Line((int) startNormal[0], (int) startNormal[1], (int) endNormal[0], (int) endNormal[1], parent.imp);
		parent.overlay.add(line);
		parent.imp.updateAndDraw();
		
		LineScanIntensity = getLineScanIntensity(parent, centerloc, startNormal, midNormal, endNormal, NormalSlopeIntercept[0], NormalSlopeIntercept[1]);

			}
	
	
		
		
		
		
		
		
		
		RegressionLineProfile currentprofile = new RegressionLineProfile(finalfunctionransac, LineScanIntensity, AllCurvaturepoints);
		
		return currentprofile;
	}
	
	
	public ArrayList<LineProfileCircle> getLineScanIntensity(final InteractiveSimpleEllipseFit parent, final long[] centerpoint,
			final long[] startNormal, final long[] mindNormal, final long[] endNormal, final double slope, final double intercept){
		
		int count = 0;
		
		ArrayList<LineProfileCircle> LineScanIntensity = new ArrayList<LineProfileCircle>();
		
		
		RandomAccess<FloatType> ranac = parent.CurrentViewOrig.randomAccess();

		
		long minXdim = parent.CurrentViewOrig.min(0);
		long minYdim = parent.CurrentViewOrig.min(1);
		
		long maxXdim = parent.CurrentViewOrig.max(0);
		long maxYdim = parent.CurrentViewOrig.max(1);
		
		long[] outsidepoint = (Distance.DistanceSq(centerpoint, startNormal) < Distance.DistanceSq(centerpoint, endNormal))? endNormal:startNormal;
		long[] insidepoint = (Distance.DistanceSq(centerpoint, startNormal) > Distance.DistanceSq(centerpoint, endNormal))? endNormal:startNormal;
		
		
		double Intensity = 0;
		double IntensitySec = 0;
		RandomAccess<FloatType> ranacsec;
		if (parent.CurrentViewSecOrig != null)
			ranacsec = parent.CurrentViewSecOrig.randomAccess();
		else
			ranacsec = ranac;

		ranac.setPosition(startNormal);
		ranacsec.setPosition(ranac);
		
		Intensity = ranac.get().get();
		IntensitySec = ranacsec.get().get();
		
		LineProfileCircle linescan = new LineProfileCircle(count, Intensity, IntensitySec);
		LineScanIntensity.add(linescan);
		
		double minX = (startNormal[0] < endNormal[0])? startNormal[0]:endNormal[0];
		double maxX = (startNormal[0] > endNormal[0])? startNormal[0]:endNormal[0];
		
		
		int sign = (minX == outsidepoint[0])?1:-1;
		
		
		minX = outsidepoint[0];
		double minY = slope * minX + intercept;
		maxX = insidepoint[0];
		double maxY = insidepoint[1];
		
		double step = (maxX - minX) / (2*parent.insidedistance);
		ranac.setPosition(new long[] {Math.round(maxX), Math.round(maxY)});
		ranacsec.setPosition(ranac);
		
		Intensity = ranac.get().get();
		IntensitySec = ranacsec.get().get();
		while(true) {
			
			count++;
	
			double nextX =  maxX - step*sign;
			double nextY = slope * nextX + intercept;
			if(nextX > minXdim && nextX < maxXdim && nextY > minYdim && nextY < maxYdim) {
			
			ranac.setPosition(new long[] {Math.round(nextX), Math.round(nextY)});
			ranacsec.setPosition(ranac);
			
			Intensity = ranac.get().get();
			IntensitySec = ranacsec.get().get();
			
			}
			else
				break;
			
			maxX = nextX;
			
			
			
			linescan = new LineProfileCircle(count, Intensity, IntensitySec);
			LineScanIntensity.add(linescan);
			
			if (nextX < minX || nextY < minY)
				break;
		}
		return LineScanIntensity;
		
	}

	
	public double getDistance(Localizable point, Localizable centerpoint) {

		double distance = 0;

		int ndims = point.numDimensions();

		for (int i = 0; i < ndims; ++i) {

			distance += (point.getDoublePosition(i) - centerpoint.getDoublePosition(i))
					* (point.getDoublePosition(i) - centerpoint.getDoublePosition(i));

		}

		return Math.sqrt(distance);

	}

	public double getDistance(RealLocalizable point, RealLocalizable centerpoint) {

		double distance = 0;

		int ndims = point.numDimensions();

		for (int i = 0; i < ndims; ++i) {

			distance += (point.getDoublePosition(i) - centerpoint.getDoublePosition(i))
					* (point.getDoublePosition(i) - centerpoint.getDoublePosition(i));

		}

		return Math.sqrt(distance);

	}

}
