/**
 *    Copyright Masao Nagasaki
 *    Nagasaki Lab
 *    Laboratory of Biomedical Information Analysis,
 *    Department of Integrative Genomics,
 *    Tohoku Medical Megabank Organization, Tohoku University 
 *    @since 2013
 *
 *    This file is part of SUGAR (Subtile-based GUI-Assisted Refiner).
 *    SUGAR is an extension of FastQC (copyright 2010-12 Simon Andrews)
 *
 *    FastQC is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    FastQC is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with FastQC; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.csml.tommo.sugar.modules.heatmap;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JDialog;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.csml.tommo.sugar.heatmap.IMixOperation;
import org.csml.tommo.sugar.heatmap.MixOperationComboBox;
import org.csml.tommo.sugar.modules.QualityHeatMapsPerTileAndBase;
import org.csml.tommo.sugar.sequence.LaneCoordinates;

public class MixedResultsTable extends ResultsTable {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param qualityHeatMapsPerTileAndBase TODO
	 * @param arg0
	 */
	public MixedResultsTable(QualityHeatMapsPerTileAndBase qualityHeatMapsPerTileAndBase, MixedResultsTableModel model) {
		super(qualityHeatMapsPerTileAndBase, model);
	}

	@Override
	public MixedResultsTableModel getModel() {
		return (MixedResultsTableModel) super.getModel();
	}

	@Override
	protected JDialog createPopupHeatMapWindow(int row, int col) {
		JDialog result = null;

		result = new MixedHeatmapDialog(row, col, this);
		result.setVisible(true);
		return result;
	}

	public void updateModel(LaneCoordinates laneCoordinates) {			
		MixedResultsTableModel model = new MixedResultsTableModel(qualityHeatMapsPerTileAndBase, laneCoordinates, getModel().getTileNumeration());			
		setModel(model);	

		setColumnWidth();
	}

	public JSlider createMixParameterSlider() {

		double mixParameter = getModel().getMixParameter();
		JSlider slider = new JSlider(JSlider.VERTICAL, 0, 100, (int) (mixParameter*100.0));
		slider.setMajorTickSpacing(10);
		slider.setSnapToTicks(true);
		slider.setPaintTicks(true);
		//			slider.setPaintLabels(true);

		slider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent event) {

				JSlider source = (JSlider)event.getSource();
				if (!source.getValueIsAdjusting()) {
					double param = (double)source.getValue() / (double) 100.0;
					updateMixParameter(param);
				}
			}
		});

		return slider;
	}

	public MixOperationComboBox createMixOperationComboBox(){
		MixOperationComboBox comboBox = new MixOperationComboBox();
		comboBox.addItemListener(new ItemListener() {				
			@Override
			public void itemStateChanged(ItemEvent e) {
				MixOperationComboBox box = (MixOperationComboBox) e.getSource();
				double param = getModel().getMixParameter();
				IMixOperation newMixOperation = box.getSelectedItem().getMixer();
				newMixOperation.setParameter(param);
				updateMixOperation(newMixOperation);
			}
		});
		return comboBox;
	}

	public void updateMixOperation(IMixOperation mixOperation){
		getModel().setMixOperation(mixOperation);
		repaint();
	}

	public void updateMixParameter(double param)
	{
		getModel().setMixParameter(param);
		repaint();			
	}
}