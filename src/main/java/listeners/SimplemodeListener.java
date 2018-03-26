package listeners;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import pluginTools.IlastikEllipseFileChooser;
import pluginTools.InteractiveSimpleEllipseFit;

public class SimplemodeListener implements ItemListener {

	
	public final IlastikEllipseFileChooser parent;
	
	public SimplemodeListener(final IlastikEllipseFileChooser parent) {
		
		
		this.parent = parent;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		
		if(e.getStateChange() == ItemEvent.DESELECTED) {
			
			parent.superpixel = true;;
			parent.simple = false;
			
		}
		
		else if (e.getStateChange() == ItemEvent.SELECTED) {
			
			parent.superpixel = false;
			parent.simple = true;
			
			parent.Panelsuperfile.setEnabled(false);
			parent.ChoosesuperImage.setEnabled(false);
			parent.panelFirst.validate();
			parent.panelFirst.repaint();
			
			
		}
		
		
		
		
	}
	
	
	
	
}