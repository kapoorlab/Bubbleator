package listeners;

import java.awt.Label;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JScrollBar;

import pluginTools.InteractiveSimpleEllipseFit;
import pluginTools.InteractiveSimpleEllipseFit.ValueChange;

public class HighProbListener implements AdjustmentListener {
	
	final Label label;
	final String string;
	InteractiveSimpleEllipseFit parent;
	final float min, max;
	final int scrollbarSize;
	final JScrollBar deltaScrollbar;
	
	public HighProbListener(final InteractiveSimpleEllipseFit parent, final Label label, final String string, final float min, final float max, final int scrollbarSize, final JScrollBar deltaScrollbar) {
		
		this.label = label;
		this.parent = parent;
		this.string = string;
		this.min = min;
		this.max = max;
		this.scrollbarSize = scrollbarSize;
		this.deltaScrollbar = deltaScrollbar;
		
		
		
		deltaScrollbar.addMouseListener(new EllipseStandardMouseListener(parent, ValueChange.SEG));
		
		
		
	}
	
	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		
		parent.highprob = utility.Slicer.computeValueFromScrollbarPosition(e.getValue(), min, max, scrollbarSize);
		deltaScrollbar
		.setValue(utility.Slicer.computeScrollbarPositionFromValue(parent.highprob, min, max, scrollbarSize));
		label.setText(string +  " = "  + parent.highprob);
		
		parent.panelFirst.validate();
		parent.panelFirst.repaint();
		
		
	}
	
	
	
	

}
