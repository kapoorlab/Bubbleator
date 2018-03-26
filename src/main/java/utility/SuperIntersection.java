package utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ellipsoidDetector.Intersectionobject;
import ellipsoidDetector.Tangentobject;
import ij.gui.EllipseRoi;
import ij.gui.Line;
import ij.gui.OvalRoi;
import net.imglib2.RealLocalizable;
import net.imglib2.algorithm.ransac.RansacModels.Angleobject;
import net.imglib2.algorithm.ransac.RansacModels.Ellipsoid;
import net.imglib2.algorithm.ransac.RansacModels.Intersections;
import net.imglib2.algorithm.ransac.RansacModels.Tangent2D;
import net.imglib2.type.logic.BitType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import pluginTools.InteractiveSimpleEllipseFit;

public class SuperIntersection {

	final InteractiveSimpleEllipseFit parent;
	
	public SuperIntersection(final InteractiveSimpleEllipseFit parent) {
		
		this.parent = parent;
		
	}
	
	public void Getsuperintersection( ArrayList<EllipseRoi> resultroi,
			ArrayList<OvalRoi> resultovalroi, ArrayList<Line> resultlineroi,
			final ArrayList<Tangentobject> AllPointsofIntersect, final ArrayList<Intersectionobject> Allintersection, int t, int z) {
		
		System.out.println("Super fitting post loop");
		
		final ArrayList<Pair<Ellipsoid, Ellipsoid>> fitmapspecial = new ArrayList<Pair<Ellipsoid, Ellipsoid>>();
		for (int index = 0; index < parent.superReducedSamples.size(); ++index) {
			
			for (int indexx = 0; indexx < parent.superReducedSamples.size() - 1; ++indexx) {
			
			if (index!=indexx) {
				
				
				fitmapspecial.add(new ValuePair<Ellipsoid, Ellipsoid>(parent.superReducedSamples.get(index).getA(),
						parent.superReducedSamples.get(indexx).getA()));
				
				
			}
			
			
			
			
			
			}
			
		}
		
		
		for (int i = 0; i < fitmapspecial.size(); ++i) {

			Pair<Ellipsoid, Ellipsoid> ellipsepair = fitmapspecial.get(i);

			ArrayList<double[]> pos = Intersections.PointsofIntersection(ellipsepair);

			Tangentobject PointsIntersect = new Tangentobject(pos, ellipsepair, t, z);

			for (int j = 0; j < pos.size(); ++j) {

				OvalRoi intersectionsRoi = new OvalRoi(pos.get(j)[0] - parent.radiusdetection,
						pos.get(j)[1] - parent.radiusdetection, 2 * parent.radiusdetection,
						2 * parent.radiusdetection);
				intersectionsRoi.setStrokeColor(parent.colorDet);
				resultovalroi.add(intersectionsRoi);

				double[] lineparamA = Tangent2D.GetTangent(ellipsepair.getA(), pos.get(j));

				double[] lineparamB = Tangent2D.GetTangent(ellipsepair.getB(), pos.get(j));

				Angleobject angleobject = Tangent2D.GetTriAngle(lineparamA, lineparamB, pos.get(j), ellipsepair);
				resultlineroi.add(angleobject.lineA);
				resultlineroi.add(angleobject.lineB);
				

				Intersectionobject currentintersection = new Intersectionobject(pos.get(j), angleobject.angle,
						ellipsepair, resultlineroi, t, z);

				Allintersection.add(currentintersection);

				System.out.println("Angle: " + angleobject.angle + " " + pos.get(j)[0]);

			}

			AllPointsofIntersect.add(PointsIntersect);

			fitmapspecial.remove(ellipsepair);

		}


		String uniqueID = Integer.toString(z) + Integer.toString(t);
	

			parent.ALLIntersections.put(uniqueID, Allintersection);
		

		

			// Add new result rois to ZTRois
			for (Map.Entry<String, Roiobject> entry : parent.ZTRois.entrySet()) {

				Roiobject currentobject = entry.getValue();

				if (currentobject.fourthDimension == t && currentobject.thirdDimension == z) {

					currentobject.resultroi = resultroi;
					currentobject.resultovalroi = resultovalroi;
					currentobject.resultlineroi = resultlineroi;

				}

			}

			Roiobject currentobject = new Roiobject(resultroi,resultovalroi,resultlineroi, z, t, true);
			parent.ZTRois.put(uniqueID, currentobject);

			DisplayAuto.Display(parent);
		
		
	}
	
	
}