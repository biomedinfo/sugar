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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;

import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;
import org.csml.tommo.sugar.modules.QualityHeatMapsPerTileAndBase;
import org.csml.tommo.sugar.sequence.TileBPCoordinates;

public class ResultsPerTileTable extends JTable {
	
	/**
	 * 
	 */
	protected final QualityHeatMapsPerTileAndBase qualityHeatMapsPerTileAndBase;

	final public static int CELL_SEPARATOR_SIZE = 3;
	final public static int COLUMN_SEPARATOR_SIZE = 15;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param qualityHeatMapsPerTileAndBase TODO
	 * @param arg0
	 */
	public ResultsPerTileTable(QualityHeatMapsPerTileAndBase qualityHeatMapsPerTileAndBase, ResultsPerTileTableModel model) {
		super(model);
		this.qualityHeatMapsPerTileAndBase = qualityHeatMapsPerTileAndBase;
		
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setColumnWidth();
		setRowHeight();
		setDefaultRenderer(MeanQualityMatrix.class, new AverageMatrixCellRenderer());
		setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		setCellSelectionEnabled(true);
		setTableHeader(null);
		setShowHorizontalLines(false);
		setBackground(Color.GRAY);
		setGridColor(Color.GRAY);
	}
	
	@Override
	public ResultsPerTileTableModel getModel() {
		return (ResultsPerTileTableModel) super.getModel();
	}

	public String getFlowCell() {
		return getModel().getLaneCoordinates().getFlowCell();
	}

	public Integer getLane() {
		return getModel().getLaneCoordinates().getLane();
	}


	public List<TileBPCoordinates> getTileBPCoordinateList(int row, int col){
		List<TileBPCoordinates> tileBPCoordinateList = new ArrayList<TileBPCoordinates>();
		int cycleSize = getModel().getTileNumeration().getCycleSize();
		int firstRowInCycle = row - (row % cycleSize);
		
		for(int j=0; j<cycleSize; j++){
			for(int i=0; i<2; i++){
				if(col + i < getColumnCount()){
					tileBPCoordinateList.add(getModel().getCoordinateAt(firstRowInCycle + j, col + i));
				}
			}
		}
		return tileBPCoordinateList;
	}
	
	protected void setColumnWidth() {
		for (int i=0; i<getColumnCount(); i++) {
			getColumnModel().getColumn(i).setPreferredWidth(getHeatMapSize());
			getColumnModel().getColumn(i).setMaxWidth(getHeatMapSize());

		}

		// add top/botom separator for
		int separatorColumn = getModel().getTopBottomSeparatorColumn();
		
		if (separatorColumn >=0)
		{
			getColumnModel().getColumn(separatorColumn).setPreferredWidth(getHeatMapSize() + COLUMN_SEPARATOR_SIZE);
			getColumnModel().getColumn(separatorColumn+1).setPreferredWidth(getHeatMapSize() + COLUMN_SEPARATOR_SIZE);

			getColumnModel().getColumn(separatorColumn).setMaxWidth(getHeatMapSize() + COLUMN_SEPARATOR_SIZE);
			getColumnModel().getColumn(separatorColumn+1).setMaxWidth(getHeatMapSize() + COLUMN_SEPARATOR_SIZE);
			
			getColumnModel().getColumn(separatorColumn).setMinWidth(getHeatMapSize() + COLUMN_SEPARATOR_SIZE);
			getColumnModel().getColumn(separatorColumn+1).setMinWidth(getHeatMapSize() + COLUMN_SEPARATOR_SIZE);

		}
	}
	
	public int getHeatMapSize() {
		return qualityHeatMapsPerTileAndBase.getHeatMapSize();
	}

	private void setRowHeight() {
		int cycleSize = getModel().getTileNumeration().getCycleSize();

		for (int i=0; i<getRowCount(); i++) {
			int rowHeight = getHeatMapSize();
			if (i % cycleSize == 0)
				rowHeight += CELL_SEPARATOR_SIZE;
			if (i % cycleSize == cycleSize -1)
				rowHeight += CELL_SEPARATOR_SIZE;
			
			setRowHeight(rowHeight);
		}
	}

	private boolean isDragging = false; 
	
	public boolean isDragging() {
		return isDragging;
	}
	
	public void setDragging(boolean b) {
		isDragging = b;
	}
	
	@Override
	public void tableChanged(TableModelEvent e) {
				
		if (e.getSource() == getModel().getParentModel())
		{
			getModel().fireTableDataChanged();			
		}
		else
		{
			super.tableChanged(e);
		}
	}

}