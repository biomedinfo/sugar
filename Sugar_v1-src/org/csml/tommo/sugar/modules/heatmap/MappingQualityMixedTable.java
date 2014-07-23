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

import javax.swing.JDialog;

import org.csml.tommo.sugar.heatmap.IMixOperation;
import org.csml.tommo.sugar.sequence.LaneCoordinates;

public class MappingQualityMixedTable extends MappingQualityTable {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param qualityHeatMapsPerTileAndBase TODO
	 * @param arg0
	 */
	public MappingQualityMixedTable(MappingQualityMixedTableModel model) {
		super(model.getMappingQuality(), model);
	}

	@Override
	public MappingQualityMixedTableModel getModel() {
		return (MappingQualityMixedTableModel) super.getModel();
	}

	@Override
	protected JDialog createPopupHeatMapWindow(int row, int col) {
		JDialog result = null;

//		result = new DensityMixedHeatmapDialog(row, col, this);
//		result.setVisible(true);
		return result;
	}

	public void updateModel(LaneCoordinates laneCoordinates) {			
		MappingQualityMixedTableModel model = new MappingQualityMixedTableModel(getModel().getMappingQuality(), laneCoordinates, getModel().getTileNumeration());			
		setModel(model);	

		setColumnWidth();
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