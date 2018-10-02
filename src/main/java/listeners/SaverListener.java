package listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import curvatureUtils.CurvatureTableDisplay;
import ellipsoidDetector.Intersectionobject;
import ellipsoidDetector.KymoSaveobject;
import hashMapSorter.SortCoordinates;
import ij.IJ;
import kalmanForSegments.Segmentobject;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;
import pluginTools.InteractiveSimpleEllipseFit;
import utility.Curvatureobject;

public class SaverListener implements ActionListener {

	final InteractiveSimpleEllipseFit parent;
	
	
	/**
	 * 
	 * Fields
	 * 
	 */
	
	int XcordLabel = 0;
	int YcordLabel = 1;
	int CurvatureLabel = 2;
	int IntensityALabel = 3;
	int IntensityBLabel = 4;
	int perimeterLabel = 5;
	

	public SaverListener(final InteractiveSimpleEllipseFit parent) {

		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		
	
			
		if (!parent.curveautomode && !parent.curvesupermode)
			OldSave();
		else
		  KymoSave();

				
				
				
				
				IJ.log("Choosen Track saved in: " + parent.saveFile.getAbsolutePath());
			
			
		
		
	}
	
	
	public void NewSave() {
		
	
		
		String ID = parent.selectedID;
		if (!parent.curveautomode && !parent.curvesupermode) {
			try {
				File fichier = new File(
						parent.saveFile + "//" + parent.addToName + "TrackID" +ID + ".txt");

				FileWriter fw = new FileWriter(fichier);
				BufferedWriter bw = new BufferedWriter(fw);
				
				bw.write(
						"\tTime (px)\t AngleT \n");
			for (int index = 0; index< parent.resultAngle.size(); ++index) {
				
				
				if (ID.equals(parent.resultAngle.get(index).getA() )) {
					
					// Save result sin file
				
					int time = (int) parent.resultAngle.get(index).getB()[0];
					double angle = parent.resultAngle.get(index).getB()[1];
					bw.write("\t" + time + "\t" + "\t"
							+ angle + 
							
							"\n");
					

					
				}
				
			}
				
			bw.close();
			fw.close();
			}
			catch (IOException te) {
			}
			}
				
				else {
		
			try {
				File fichier = new File(
						parent.saveFile + "//" + parent.addToName + "SegmentID" +ID + ".txt");

				FileWriter fw = new FileWriter(fichier);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write("\tTrackID" + "\t" + "\t" + ID + "\n");
				bw.write("\tX-coordinates\tY-coordinates\tTime\tCurvature\t Perimeter\t \t Intensity \t \t IntensitySec\n");
				for (Pair<String, Segmentobject> currentangle : parent.SegmentTracklist) {
					
					String currentID = currentangle.getA();
					
					if(currentID.equals(ID)) {
						
						
						bw.write("\t"+ parent.nf.format(currentangle.getB().centralpoint.getDoublePosition(XcordLabel)) +  "\t" + "\t" + parent.nf.format(currentangle.getB().centralpoint.getDoublePosition(YcordLabel))
								+ "\t" + "\t" +
								"\t" + "\t" + currentangle.getB().z + 
								parent.nf.format(currentangle.getB().Curvature) + "\t"  + "\t"+  "\t" + "\t" + parent.nf.format(currentangle.getB().Perimeter) + "\t" + "\t"  
								+ parent.nf.format(currentangle.getB().IntensityA) +
								
								"\t" + "\t"  + parent.nf.format(currentangle.getB().IntensityB) + 
								"\n");
						
						
					}
				
				
			}
			
			
	    bw.close();
		fw.close();
		}
		catch (IOException te) {
		}
				}
		
	}
	
	public void KymoSave() {
		
		String ID = parent.selectedID;
		
		try {
			File fichier = new File(
					parent.saveFile + "//" + parent.addToName + "TrackID" +ID + ".txt");
			
			FileWriter fw = new FileWriter(fichier);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("\tTrackID" + "\t" + "\t" + ID+ "\n");
			
			bw.write("\tArbritaryUnit\tTime\t\tCurvature\t \t Intensity \t \t IntensitySec\n");
			
			
			KymoSaveobject Kymos = parent.KymoFileobject.get(ID);
			if(Kymos==null) {
				
				CurvatureTableDisplay.displayclicked(parent, parent.rowchoice);
				Kymos = parent.KymoFileobject.get(ID);
			}
			
			RandomAccessibleInterval<FloatType> CurvatureKymo = Kymos.CurvatureKymo;
			
			RandomAccessibleInterval<FloatType> IntensityAKymo = Kymos.IntensityAKymo;
			
			RandomAccessibleInterval<FloatType> IntensityBKymo = Kymos.IntensityBKymo;
			
			int hyperslicedimension = 1;
			
			for (long pos = 0; pos< CurvatureKymo.dimension(hyperslicedimension); ++pos) {
				
				
				RandomAccessibleInterval< FloatType > CurveView =
                        Views.hyperSlice( CurvatureKymo, hyperslicedimension, pos );
				
				RandomAccessibleInterval< FloatType > IntensityAView =
                        Views.hyperSlice( IntensityAKymo, hyperslicedimension, pos );
				
				RandomAccessibleInterval< FloatType > IntensityBView =
                        Views.hyperSlice( IntensityBKymo, hyperslicedimension, pos );
				
 				RandomAccess<FloatType> Cranac = CurveView.randomAccess();
				RandomAccess<FloatType> Aranac = IntensityAView.randomAccess();
				RandomAccess<FloatType> Branac = IntensityBView.randomAccess();
				
				Iterator<Map.Entry<String, Integer>> itZ = parent.AccountedZ.entrySet().iterator();
				
				while (itZ.hasNext()) {

					Map.Entry<String, Integer> entry = itZ.next();

					int time = entry.getValue();
					Cranac.setPosition(time - 1, 0);
					Aranac.setPosition(time - 1, 0);
					Branac.setPosition(time - 1, 0);
					
				bw.write("\t"+ pos +  "\t" + "\t" + time
				+ "\t" + "\t" +
				parent.nf.format(Cranac.get().get())
                  + "\t" + "\t" +
				parent.nf.format(Aranac.get().get()) + "\t"  + "\t"+  "\t" + "\t" + parent.nf.format(Branac.get().get()) + 
				"\n");
		
				}
			}
			
			
		    bw.close();
			fw.close();
		}
		catch (IOException te) {
		}
		
	}
	
	
	
	public void DenseSave() {
		
		String ID = parent.selectedID;
		
		try {
			File fichier = new File(
					parent.saveFile + "//" + parent.addToName + "TrackID" +ID + ".txt");

			FileWriter fw = new FileWriter(fichier);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("\tTrackID" + "\t" + "\t" + ID+ "\n");
			bw.write("\tX-coordinates\tY-coordinates\tTime\t\tCurvature\t \tPerimeter\t \t Intensity \t \t IntensitySec\n");
			for (Pair<String, Intersectionobject> currentangle : parent.Tracklist) {
				
				String currentID = currentangle.getA();
				if(currentID.equals(ID)) {
					ArrayList<double[]> linelist = currentangle.getB().linelist;
					for (int index =0; index < linelist.size(); ++index) {
					
					bw.write("\t"+ parent.nf.format(linelist.get(index)[XcordLabel]) +  "\t" + "\t" + parent.nf.format(linelist.get(index)[YcordLabel])
							+ "\t" + "\t" +
							 currentangle.getB().z
                              + "\t" + "\t" +
							parent.nf.format(linelist.get(index)[CurvatureLabel]) + "\t"  + "\t"+  "\t" + "\t" + parent.nf.format(linelist.get(index)[perimeterLabel]) + "\t" + "\t"  
							+ parent.nf.format(linelist.get(index)[IntensityALabel]) +
							
							"\t" + "\t"  + parent.nf.format(linelist.get(index)[IntensityBLabel]) + 
							"\n");
					
					
				}
				}
			
		}
		
		
    bw.close();
	fw.close();
	}
	catch (IOException te) {
	}
		
		
		
		
	}
	
	public void OldSave() {
		
		
		String ID = parent.selectedID;
		
		if (!parent.curveautomode && !parent.curvesupermode) {
		try {
			File fichier = new File(
					parent.saveFile + "//" + parent.addToName + "TrackID" +ID + ".txt");

			FileWriter fw = new FileWriter(fichier);
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write(
					"\tTime (px)\t AngleT \n");
		for (int index = 0; index< parent.resultAngle.size(); ++index) {
			
			
			if (ID.equals(parent.resultAngle.get(index).getA() )) {
				
				// Save result sin file
			
				int time = (int) parent.resultAngle.get(index).getB()[0];
				double angle = parent.resultAngle.get(index).getB()[1];
				bw.write("\t" + time + "\t" + "\t"
						+ angle + 
						
						"\n");
				

				
			}
			
		}
			
		bw.close();
		fw.close();
		}
		catch (IOException te) {
		}
		}
			
			else {
				
				try {
					File fichier = new File(
							parent.saveFile + "//" + parent.addToName + "CellID" +ID + ".txt");

					FileWriter fw = new FileWriter(fichier);
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write("\tTrackID" + "\t" + "\t" + ID+ "\n");
					bw.write("\tX-coordinates\tY-coordinates\tTime\tCurvature\t Perimeter\t \t Intensity \t \t IntensitySec\n");
					for (Pair<String, Intersectionobject> currentangle : parent.Tracklist) {
						
						String currentID = currentangle.getA();
						if(currentID.equals(ID)) {
							ArrayList<double[]> linelist = currentangle.getB().linelist;
							for (int index =0; index < linelist.size(); ++index) {
							
							bw.write("\t"+ parent.nf.format(linelist.get(index)[XcordLabel]) +  "\t" + "\t" + parent.nf.format(linelist.get(index)[YcordLabel])
									+ "\t" + "\t" +
									 currentangle.getB().z
	                                  + "\t" + "\t" +
									parent.nf.format(linelist.get(index)[CurvatureLabel]) + "\t"  + "\t" + parent.nf.format(linelist.get(index)[perimeterLabel]) + "\t" + "\t"  
									+ parent.nf.format(linelist.get(index)[IntensityALabel]) +
									
									"\t" + "\t"  + parent.nf.format(linelist.get(index)[IntensityBLabel]) + 
									"\n");
							
							
						}
						}
					
				}
				
				
		    bw.close();
			fw.close();
			}
			catch (IOException te) {
			}
				
				
				
				
			
		}
		
		
	
	
	}
		
	}
	
	
