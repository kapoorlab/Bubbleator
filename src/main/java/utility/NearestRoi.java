package utility;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ij.gui.OvalRoi;
import ij.gui.Roi;

import net.imglib2.KDTree;
import net.imglib2.RealPoint;
import pluginTools.InteractiveSimpleEllipseFit;

public class NearestRoi {

	
	public static Roi getNearestRois(Roiobject roi, double[] Clickedpoint, final InteractiveSimpleEllipseFit parent ) {
		

		
		Roi[] Allrois = roi.roilist;
		
		Roi KDtreeroi = null;

		final List<RealPoint> targetCoords = new ArrayList<RealPoint>(Allrois.length);
		final List<FlagNode<Roi>> targetNodes = new ArrayList<FlagNode<Roi>>(Allrois.length);
		for (int index = 0; index < Allrois.length; ++index) {

			 Roi r = Allrois[index];
			 Rectangle rect = r.getBounds();
			 
			 targetCoords.add( new RealPoint(rect.x + rect.width/2.0, rect.y + rect.height/2.0 ) );
			 

			targetNodes.add(new FlagNode<Roi>(Allrois[index]));

		}

		if (targetNodes.size() > 0 && targetCoords.size() > 0) {

			final KDTree<FlagNode<Roi>> Tree = new KDTree<FlagNode<Roi>>(targetNodes, targetCoords);

			final NNFlagsearchKDtree<Roi> Search = new NNFlagsearchKDtree<Roi>(Tree);


				final double[] source = Clickedpoint;
				final RealPoint sourceCoords = new RealPoint(source);
				Search.search(sourceCoords);
				final FlagNode<Roi> targetNode = Search.getSampler().get();

				KDtreeroi = targetNode.getValue();

		}

		return KDtreeroi;
		
	}
	
	
	public static OvalRoi getNearestIntersectionRois(Roiobject roi, double[] Clickedpoint, final InteractiveSimpleEllipseFit parent ) {
		

		ArrayList<OvalRoi> Allrois = roi.resultovalroi;
		
		OvalRoi KDtreeroi = null;

		final List<RealPoint> targetCoords = new ArrayList<RealPoint>(Allrois.size());
		final List<FlagNode<OvalRoi>> targetNodes = new ArrayList<FlagNode<OvalRoi>>(Allrois.size());
		for (int index = 0; index < Allrois.size(); ++index) {

			 Roi r = Allrois.get(index);
			 Rectangle rect = r.getBounds();
			 
			 targetCoords.add( new RealPoint(rect.x + rect.width/2.0, rect.y + rect.height/2.0 ) );
			 

			targetNodes.add(new FlagNode<OvalRoi>(Allrois.get(index)));

		}

		if (targetNodes.size() > 0 && targetCoords.size() > 0) {

			final KDTree<FlagNode<OvalRoi>> Tree = new KDTree<FlagNode<OvalRoi>>(targetNodes, targetCoords);

			final NNFlagsearchKDtree<OvalRoi> Search = new NNFlagsearchKDtree<OvalRoi>(Tree);


				final double[] source = Clickedpoint;
				final RealPoint sourceCoords = new RealPoint(source);
				Search.search(sourceCoords);
				final FlagNode<OvalRoi> targetNode = Search.getSampler().get();

				KDtreeroi = targetNode.getValue();

		}

		return KDtreeroi;
		
	}
	
	
	
}
      