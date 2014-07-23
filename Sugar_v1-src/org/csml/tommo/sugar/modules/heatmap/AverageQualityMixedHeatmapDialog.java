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

import org.csml.tommo.sugar.heatmap.ColorPaintScale;
import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;
import org.csml.tommo.sugar.heatmap.MeanQualityMatrixChart;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;


public class AverageQualityMixedHeatmapDialog extends MixedHeatmapDialog{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8367381777752879844L;
	
	public AverageQualityMixedHeatmapDialog(int row, int col, MixedResultsTable table){
		super(row, col, table);
	}
		
	@Override
	protected ChartPanel createChartPanel(MeanQualityMatrix mixedMatrix) {
		
		return MeanQualityMatrixChart.createAverageQualityChartPanel(mixedMatrix, AverageQualityMatrixCellRenderer.getPaintScale(table));
	}

	@Override
	protected void updateMatrixes() {
		for(int i=0; i<matrixMixedList.size(); i++) {
			if (matrixMixedList.get(i) != null)
			{
				matrixMixedList.get(i).mix(matrixTopList.get(i), matrixBottomList.get(i), mixOperation);
				//					ChartPanel chartPanel = MeanQualityMatrixChart.createChartPanel(matrixMixedList.get(i), mixOperation.getPaintScale());

				JFreeChart chart = mixedChartPanelList.get(i).getChart();
				if(chart instanceof MeanQualityMatrixChart){
					MeanQualityMatrixChart matrixChart = (MeanQualityMatrixChart) chart;
					
					ColorPaintScale paintScale =  AverageQualityMatrixCellRenderer.getPaintScale(table);
					matrixChart.setPaintScale(paintScale);
					matrixChart.setPaintScaleLegend(paintScale);
				}
				mixedChartPanelList.get(i).getChart().fireChartChanged();
			}
		}
	}
}
