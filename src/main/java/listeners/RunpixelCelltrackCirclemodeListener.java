package listeners;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import comboSliderTextbox.SliderBoxGUI;
import pluginTools.InteractiveSimpleEllipseFit;

public class RunpixelCelltrackCirclemodeListener implements ItemListener {

	InteractiveSimpleEllipseFit parent;

	public RunpixelCelltrackCirclemodeListener(InteractiveSimpleEllipseFit parent) {
		this.parent = parent;
	}

	@Override
	public void itemStateChanged(final ItemEvent arg0) {

		if (arg0.getStateChange() == ItemEvent.DESELECTED) {

			parent.pixelcelltrackcirclefits = false;
			
			parent.distancemethod = false;
			parent.combomethod = false;

		}

		else if (arg0.getStateChange() == ItemEvent.SELECTED) {

			parent.pixelcelltrackcirclefits = true;
             parent.distancemethod = false;
             parent.combomethod = false;
			parent.resolution = 1; //Integer.parseInt(parent.resolutionField.getText());

		
		}

	}

}