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

import org.csml.tommo.sugar.analysis.TileNumeration;
import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;
import org.csml.tommo.sugar.modules.QualityHeatMapsPerTileAndBase;
import org.csml.tommo.sugar.sequence.LaneCoordinates;
import org.csml.tommo.sugar.sequence.TileBPCoordinates;
import org.csml.tommo.sugar.utils.StringUtils;

public class ExtendedResultsTableModel extends ResultsTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3769738873944319492L;
	
	
	public ExtendedResultsTableModel(QualityHeatMapsPerTileAndBase qualityHeatMapsPerTileAndBase, LaneCoordinates laneCoordinates, TileNumeration tileNumeration) {
		
		super(qualityHeatMapsPerTileAndBase, laneCoordinates, tileNumeration);
	}

	public String getTooltipAt(int row, int col) {
		
		TileNumeration tileNumeration = getTileNumeration();
		int cycleSize = tileNumeration.getCycleSize();
		int seqLength = getQualityHeatMapsPerTileAndBase().getMaxSequenceLength();
		
		TileBPCoordinates cds = getCoordinateAt(row, col);
		TileBPCoordinates first = getCoordinateAt(row % cycleSize, col);
		TileBPCoordinates last = getCoordinateAt((row % cycleSize) + (seqLength - 1) * cycleSize, col);
		
		MeanQualityMatrix matrix = getValueAt(row, col);
		String tooltip = cds.getTile() + ", " + cds.getBasePosition();
		if(matrix != null){
			tooltip += "<br/>density: " + matrix.getCounter();
			tooltip += "<br/>quality: " + StringUtils.FORMATTER.format(matrix.getMeanQualityValue());
		}
		tooltip += "<br/>" + cds.getTile() + ", " + first.getBasePosition() + "-" + last.getBasePosition();
		
		StringUtils.FORMATTER.setMaximumFractionDigits(1);
		tooltip += "<br/>density: " + StringUtils.FORMATTER.format(getAverageDensityForTile(row % cycleSize, col, false)) + " / " + StringUtils.FORMATTER.format(getAverageDensityForTile(row % cycleSize, col, true));
		StringUtils.FORMATTER.setMaximumFractionDigits(4);
		tooltip += "<br/>quality: " + StringUtils.FORMATTER.format(getAverageQualityForTile(row % cycleSize, col, false)) + " / " + StringUtils.FORMATTER.format(getAverageQualityForTile(row % cycleSize, col, true));
		return "<html>" + tooltip + "</html>";		
	}
	
	public double getAverageDensityForTile(int firstRow, int col, boolean excludeSelected){
		int count = 0;
		double summary = 0;
		
		TileNumeration tileNumeration = getTileNumeration();
		int cycleSize = tileNumeration.getCycleSize();
		int seqLength = getQualityHeatMapsPerTileAndBase().getMaxSequenceLength();

		for(int bp = 0; bp < seqLength; bp++){
			int nextRow = firstRow + bp * cycleSize;
			MeanQualityMatrix matrix = getValueAt(nextRow, col);
			if(matrix != null && (!excludeSelected || excludeSelected && !matrix.isAnythingSelected())){
				count++;
				summary += matrix.getCounter();
			}
		}
		return (count > 0) ? summary / count : 0;
	}
	
	public double getAverageQualityForTile(int firstRow, int col, boolean excludeSelected){
		int count = 0;
		double summary = 0;
		
		TileNumeration tileNumeration = getTileNumeration();
		int cycleSize = tileNumeration.getCycleSize();
		int seqLength = getQualityHeatMapsPerTileAndBase().getMaxSequenceLength();

		for(int bp = 0; bp < seqLength; bp++){
			int nextRow = firstRow + bp * cycleSize;
			MeanQualityMatrix matrix = getValueAt(nextRow, col);
			if(matrix != null && (!excludeSelected || excludeSelected && !matrix.isAnythingSelected())){
				count++;
				summary += matrix.getMeanQualityValue();
			}
		}		
		return (count > 0) ? summary / count : 0;			
	}
}