package ellipsoidDetector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import ij.gui.Line;
import net.imglib2.AbstractEuclideanSpace;
import net.imglib2.RealLocalizable;
import net.imglib2.algorithm.ransac.RansacModels.Ellipsoid;
import net.imglib2.util.Pair;
import utility.ThreeDRoiobject;

public class Intersectionobject extends AbstractEuclideanSpace implements RealLocalizable, Comparable<Intersectionobject> {

	
	
	public final double[] Intersectionpoint;
	public final double[] CenterofMass;
	public final List<Pair<Double, Double>> linelist;
	public final double angle;
	public final Pair<Ellipsoid, Ellipsoid> ellipsepair;
	public final int t;
	public final int z;
	public final int celllabel;
	public final ArrayList<Line> linerois;
	private String name;
	private final int ID;
	private final ConcurrentHashMap< String, Double > features = new ConcurrentHashMap< String, Double >();
	public static AtomicInteger IDcounter = new AtomicInteger( -1 );
	
	public Intersectionobject(final double[] Intersectionpoint, final double angle, final Pair<Ellipsoid, Ellipsoid> ellipsepair, final ArrayList<Line> linerois,  final int t, final int z) {
		super(3);
		this.Intersectionpoint = Intersectionpoint;
		this.angle = angle;
		this.linerois = linerois;
		this.ellipsepair = ellipsepair;
		this.t = t;
		this.z = z;
		this.ID = IDcounter.incrementAndGet();
		this.name = "ID" + ID;
		this.CenterofMass = null;
		this.linelist = null;
		this.celllabel = 0;
		putFeature(Time,  (double) t);
		putFeature(ZPOSITION, (double) z);
		putFeature(XPOSITION, Intersectionpoint[0]);
		putFeature(YPOSITION, Intersectionpoint[1]);
	}
	

	
	/**
	 * For curvature calculations
	 * 
	 * @param center of mass of candidate points (geometric center)
	 * @param list of points and curvature value
	 * @param t
	 * @param z
	 * 
	 */
	public Intersectionobject(final double[] CenterofMass, List<Pair<Double, Double>> linelist, final ArrayList<Line> linerois, final int celllabel, final int t, final int z) {
		super(3);
		this.Intersectionpoint = null;
		this.CenterofMass = CenterofMass;
		this.angle = 0;
		this.celllabel = celllabel;
		this.linelist = linelist;
		this.linerois = linerois;
		this.ellipsepair = null;
		this.t = t;
		this.z = 1;
		this.ID = IDcounter.incrementAndGet();
		this.name = "ID" + ID;
		putFeature(Time,  (double) t);
		putFeature(ZPOSITION, (double) z);
		putFeature(XPOSITION, CenterofMass[0]);
		putFeature(YPOSITION, CenterofMass[1]);
	}
	
	public void setName( final String name )
	{
		this.name = name;
	}

	public int ID()
	{
		return ID;
	}
	/** The name of the blob X position feature. */
	public static final String XPOSITION = "XPOSITION";

	/** The name of the blob Y position feature. */
	public static final String YPOSITION = "YPOSITION";
	
	/** The name of the blob Y position feature. */
	public static final String ZPOSITION = "ZPOSITION";

	
	

	/** The name of the frame feature. */
	public static final String Time = "Time";
	
	
	public final Double getFeature( final String feature )
	{
		return features.get( feature );
	}

	/**
	 * Stores the specified feature value for this spot.
	 *
	 * @param feature
	 *            the name of the feature to store, as a {@link String}.
	 * @param value
	 *            the value to store, as a {@link Double}. Using
	 *            <code>null</code> will have unpredicted outcomes.
	 */
	public final void putFeature( final String feature, final Double value )
	{
		features.put( feature, value );
	}
	
	public double DistanceTo(Intersectionobject target, final double alpha, final double beta) {
		// Returns squared distance between the source Blob and the target Blob.

		final double[] sourceLocation = Intersectionpoint;
		final double[] targetLocation = target.Intersectionpoint;

		double distance = 0;

		for (int d = 0; d < sourceLocation.length; ++d) {

			distance += (sourceLocation[d] - targetLocation[d]) * (sourceLocation[d] - targetLocation[d]);
		}
		
		double cost = distance ;
		
		
			return cost;
	}
	/**
	 * Returns the difference between the location of two blobs, this operation
	 * returns ( <code>A.diffTo(B) = - B.diffTo(A)</code>)
	 *
	 * @param target
	 *            the Blob to compare to.
	 * @param int
	 *            n n = 0 for X- coordinate, n = 1 for Y- coordinate
	 * @return the difference in co-ordinate specified.
	 */
	public double diffTo(final Intersectionobject target, int n) {

		final double thisBloblocation = Intersectionpoint[n];
		final double targetBloblocation = target.Intersectionpoint[n];
		return thisBloblocation - targetBloblocation;
	}
	/**
	 * Returns the squared distance between two blobs.
	 *
	 * @param target
	 *            the Blob to compare to.
	 *
	 * @return the distance to the current blob to target blob specified.
	 */

	public double squareDistanceTo(Intersectionobject target) {
		// Returns squared distance between the source Blob and the target Blob.

		final double[] sourceLocation = Intersectionpoint;
		final double[] targetLocation = target.Intersectionpoint;

		double distance = 0;

		for (int d = 0; d < sourceLocation.length; ++d) {

			distance += (sourceLocation[d] - targetLocation[d]) * (sourceLocation[d] - targetLocation[d]);
		}

		return distance;
	}

	@Override
	public int compareTo(Intersectionobject o) {
		return hashCode() - o.hashCode();
	}

	@Override
	public double getDoublePosition(int d) {
		
		return Intersectionpoint[d];
	}

	@Override
	public float getFloatPosition(int d) {
		
		return (float) Intersectionpoint[d];
	}

	@Override
	public void localize(float[] position) {
		int n = position.length;
		for (int d = 0; d < n; ++d)
			position[d] = getFloatPosition(d);
		
	}

	@Override
	public void localize(double[] position) {
		int n = position.length;
		for (int d = 0; d < n; ++d)
			position[d] = getFloatPosition(d);
		
	}
}
