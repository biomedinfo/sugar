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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;

import org.csml.tommo.sugar.analysis.TileNumeration;
import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;
import org.csml.tommo.sugar.heatmap.MeanQualityMatrixChart;
import org.csml.tommo.sugar.heatmap.MixOperation;
import org.csml.tommo.sugar.modules.QualityHeatMapsPerTileAndBase;
import org.csml.tommo.sugar.sequence.TileBPCoordinates;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.renderer.PaintScale;

public class DensityMixedHeatmapDialog extends DensityHeatmapDialog{
		
	private static final long serialVersionUID = 1L;

	public DensityMixedHeatmapDialog(int row, int col, DensityMixedResultsTable table) {
		super(row, col, table);		
	}
	
	protected void initData(int row, int col, DensityResultsTable table) {
		super.initData(row, col, table);		
	}
	
	@Override
	protected void rebuild(){
		centerPanel.removeAll();			
		
		PaintScale paintScale = DensityMatrixCellRenderer.getPaintScale(table.getMaxDensity());

		List<TileBPCoordinates> tileCoordinateList = table.getTileBPCoordinateList(row, col);
		TileNumeration tileNumeration = table.getModel().getTileNumeration();
		TileBPCoordinates first = tileCoordinateList.get(0);
		String windowTitle = "Mixed Density for tile=" + first.getTile();
		setTitle(windowTitle);

		List<TileBPCoordinates> tileCoordinatesTopList = new ArrayList<TileBPCoordinates>();
		List<TileBPCoordinates> tileCoordinatesBottomList = new ArrayList<TileBPCoordinates>();

		for(int i=0; i<tileCoordinateList.size(); i++){
			TileBPCoordinates tc = tileCoordinateList.get(i);
			Integer tile = tc.getTile();
			Integer bp = tc.getBasePosition();

			TileBPCoordinates tileCoordinatesTop = new TileBPCoordinates(tc, tile, bp);
			TileBPCoordinates tileCoordinatesBottom = new TileBPCoordinates(tc, tileNumeration.getBottomTile(tile), bp);

			tileCoordinatesTopList.add(tileCoordinatesTop);
			tileCoordinatesBottomList.add(tileCoordinatesBottom);
		}
		
		for(int i=0; i<tileCoordinateList.size(); i++){
			QualityHeatMapsPerTileAndBase qualityHeatmap = table.getModel().getQualityHeatMapsPerTileAndBase();
			MeanQualityMatrix matrixTop = qualityHeatmap.getMeanQualityMatrix(tileCoordinatesTopList.get(i)); 
			MeanQualityMatrix matrixBottom = qualityHeatmap.getMeanQualityMatrix(tileCoordinatesBottomList.get(i));
			MeanQualityMatrix mixedMatrix = MeanQualityMatrix.createMixedMatrix(matrixTop, matrixBottom, MixOperation.AVERAGE.getMixer());
			if (mixedMatrix != null) {
				ChartPanel chartPanel = MeanQualityMatrixChart.createDensityChartPanel(mixedMatrix, paintScale);
				centerPanel.add(chartPanel);
			}
			else{
				centerPanel.add(new JLabel("Missing data for tile=" + tileCoordinatesTopList.get(i).getTile()));
			}
		}
		centerPanel.validate();
		centerPanel.repaint();
	}	
}