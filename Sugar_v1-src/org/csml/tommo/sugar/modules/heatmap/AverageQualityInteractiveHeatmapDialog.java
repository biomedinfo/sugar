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

import java.util.List;

import javax.swing.JLabel;

import org.csml.tommo.sugar.heatmap.LinearPaintScale;
import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;
import org.csml.tommo.sugar.heatmap.MeanQualityMatrixChart;
import org.csml.tommo.sugar.modules.QualityHeatMapsPerTileAndBase;
import org.csml.tommo.sugar.sequence.TileBPCoordinates;
import org.jfree.chart.ChartPanel;

public class AverageQualityInteractiveHeatmapDialog extends InteractiveHeatmapDialog{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8367381777752879844L;
	
	public AverageQualityInteractiveHeatmapDialog(int row, int col, ResultsTable table){
		super(row, col, table);
	}

	protected void rebuild(){
		centerPanel.removeAll();
		List<TileBPCoordinates> tileBPCoordinateList = table.getTileBPCoordinateList(row, col);
		QualityHeatMapsPerTileAndBase heatmap = table.getModel().getQualityHeatMapsPerTileAndBase();
		for(int i=0; i<tileBPCoordinateList.size(); i++){
			TileBPCoordinates tc = tileBPCoordinateList.get(i);
			MeanQualityMatrix matrix = heatmap.getMeanQualityMatrix(tc);
			
			if (matrix != null) {
				ChartPanel panel = MeanQualityMatrixChart.createAverageQualityChartPanel(matrix, LinearPaintScale.AVERAGE_QUALITY_PAINT_SCALE);
				panel.setToolTipText("tile=" + tc.getTile() + " , bp=" + tc.getBasePosition());
				centerPanel.add(panel);
			}
			else{
				centerPanel.add(new JLabel("Missing data for tile=" + tc.getTile() + " , bp=" + tc.getBasePosition()));
			}
		}
		
		TileBPCoordinates first = tileBPCoordinateList.get(0);
		String windowTitle = "Mean Quality for tile=" + first.getTile() + " , bp=" + first.getBasePosition() + " and neighbors";
		setTitle(windowTitle);
		
		centerPanel.revalidate();
		centerPanel.repaint();
	}	
}
