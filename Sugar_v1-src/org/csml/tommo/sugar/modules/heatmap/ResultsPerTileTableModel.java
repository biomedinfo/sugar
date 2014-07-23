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

import javax.swing.table.AbstractTableModel;

import org.csml.tommo.sugar.analysis.TileNumeration;
import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;
import org.csml.tommo.sugar.sequence.LaneCoordinates;
import org.csml.tommo.sugar.sequence.TileBPCoordinates;
import org.csml.tommo.sugar.utils.StringUtils;

public class ResultsPerTileTableModel extends AbstractTableModel implements ITableModelWithTooltip{

	private ExtendedResultsTableModel parentModel;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected MeanQualityMatrix[][] averageMatrixPerTileTable;

	public ResultsPerTileTableModel(ExtendedResultsTableModel parentModel) {
		super();
		this.parentModel = parentModel;
		averageMatrixPerTileTable = createAverageMatrixTable();
	}

	private MeanQualityMatrix[][] createAverageMatrixTable() {
		TileNumeration tileNumeration = getTileNumeration();
		int cycleSize = tileNumeration.getCycleSize();
		int seqLength = parentModel.getQualityHeatMapsPerTileAndBase().getMaxSequenceLength();
		
		MeanQualityMatrix[][] result = new MeanQualityMatrix[cycleSize][getColumnCount()];
		for(int col = 0; col < getColumnCount(); col++)
		{			
			for (int cycle = 0; cycle < cycleSize; cycle++)
			{
				int nonEmptyMatrixCounter = 0;
				int[][] totalValueCounter = null;
				MeanQualityMatrix matrixPerTile = null;
					
				for(int bp = 0; bp < seqLength; bp++){
					int row = cycle + bp*cycleSize;
					MeanQualityMatrix matrix = parentModel.getValueAt(row, col);
					if(matrix != null){
						nonEmptyMatrixCounter++;
						if(matrixPerTile == null){
							matrixPerTile = new MeanQualityMatrix(matrix.getRange(), matrix.getSize(), matrix.getQualityThreshold());
							totalValueCounter = new int[matrix.getSize()][matrix.getSize()];
							for(int i=0; i<matrixPerTile.getSize(); i++){
								for(int j=0; j<matrixPerTile.getSize(); j++){
									matrixPerTile.setSelectedEntry(i, j, matrix.isEntrySelected(i, j));
								}
							}
						}
						for(int i=0; i<matrixPerTile.getSize(); i++){
							for(int j=0; j<matrixPerTile.getSize(); j++){
								totalValueCounter[i][j] += matrix.getTotalValueCounter()[i][j];
								matrixPerTile.getMeanValues()[i][j] += matrix.getMeanValues()[i][j] * matrix.getTotalValueCounter()[i][j];
								matrixPerTile.setCounter(matrixPerTile.getCounter() + matrix.getCounter());
							}
						}
					}
				}
				if (matrixPerTile != null) {
					for(int i=0; i<matrixPerTile.getSize(); i++){
						for(int j=0; j<matrixPerTile.getSize(); j++){
							matrixPerTile.getMeanValues()[i][j] /= totalValueCounter[i][j];
							matrixPerTile.setCounter(matrixPerTile.getCounter() / nonEmptyMatrixCounter);
						}
					}
				}
				result[cycle][col] = matrixPerTile;
			}
		}
		return result;
	}

	public int getColumnCount() {
		return parentModel.getColumnCount();
	}

	@Override
	public int getRowCount() {
		return averageMatrixPerTileTable.length;
	}

	public String getTooltipAt(int row, int col) {
		
		TileNumeration tileNumeration = getTileNumeration();
		int cycleSize = tileNumeration.getCycleSize();
		int seqLength = parentModel.getQualityHeatMapsPerTileAndBase().getMaxSequenceLength();
		
		TileBPCoordinates cds = getCoordinateAt(row, col);
		TileBPCoordinates first = getCoordinateAt(row % cycleSize, col);
		TileBPCoordinates last = getCoordinateAt((row % cycleSize) + (seqLength - 1) * cycleSize, col);
		String tooltip = cds.getTile() + ", " + first.getBasePosition() + "-" + last.getBasePosition();
		StringUtils.FORMATTER.setMaximumFractionDigits(1);
		tooltip += "<br/>density: " + StringUtils.FORMATTER.format(parentModel.getAverageDensityForTile(row % cycleSize, col, false)) + " / " + StringUtils.FORMATTER.format(parentModel.getAverageDensityForTile(row % cycleSize, col, true));
		StringUtils.FORMATTER.setMaximumFractionDigits(4);
		tooltip += "<br/>quality: " + StringUtils.FORMATTER.format(parentModel.getAverageQualityForTile(row % cycleSize, col, false)) + " / " + StringUtils.FORMATTER.format(parentModel.getAverageQualityForTile(row % cycleSize, col, true));
		return "<html>" + tooltip + "</html>";		
	}

	@Override
	public MeanQualityMatrix getValueAt(int row, int col) {
		return averageMatrixPerTileTable[row][col];
	}		

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return MeanQualityMatrix.class;
	}

	public LaneCoordinates getLaneCoordinates() {
		return parentModel.getLaneCoordinates();
	}

	public TileNumeration getTileNumeration() {
		return parentModel.getTileNumeration();
	}

	public TileBPCoordinates getCoordinateAt(int row, int col) {
		return parentModel.getCoordinateAt(row, col);
	}

	public int getTopBottomSeparatorColumn() {
		return parentModel.getTopBottomSeparatorColumn();
	}
	
	public ResultsTableModel getParentModel() {
		return parentModel;
	}
}