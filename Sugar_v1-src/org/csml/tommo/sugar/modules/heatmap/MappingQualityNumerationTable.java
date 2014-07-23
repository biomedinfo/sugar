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
 *    SUGAR is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    SUGAR is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with SUGAR; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.csml.tommo.sugar.modules.heatmap;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.csml.tommo.sugar.SugarApplication;
import org.csml.tommo.sugar.heatmap.MappingQualityMatrix;

public class MappingQualityNumerationTable extends JTable implements PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int COLUMN_WIDTH = 100;

	public MappingQualityNumerationTable(int cycleSize, int heatMapSize){
		super();
		setModel(new MappingQualityNumerationTableModel(cycleSize));
//		setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		setColumnWidth(COLUMN_WIDTH);
		setFixedRowHeight(cycleSize, heatMapSize);
		setTableHeader(null);
//		setShowGrid(false);
		setOpaque(false);
		setGridColor(Color.GRAY);
		setBackground(Color.GRAY);
		
		SugarApplication.getApplication().addTileSizePropertyChangeListener(this);

	}
	
	protected void setColumnWidth(int width) {
		for (int i=0; i<getColumnCount(); i++) {
			getColumnModel().getColumn(i).setPreferredWidth(width);
		}
	}

	private void setFixedRowHeight(int cycleSize, int height) {
		for (int i=0; i<getRowCount(); i++) {
			int rowHeight = height;
			if (i % cycleSize == 0)
				rowHeight += ResultsTable.CELL_SEPARATOR_SIZE;
			if (i % cycleSize == cycleSize -1)
				rowHeight += ResultsTable.CELL_SEPARATOR_SIZE;
			
			setRowHeight(i, rowHeight);
		}
	}
	
	private class MappingQualityNumerationTableModel extends AbstractTableModel{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private int cycleSize;

		public MappingQualityNumerationTableModel(int cycleSize){
			super();
			this.cycleSize = cycleSize;
		}
		
		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public int getRowCount() {
			return (MappingQualityMatrix.THRESHOLDS.length + 1) * cycleSize;
		}

		@Override
		public String getValueAt(int row, int col) {
			
			String result = null;
			
			if (row % cycleSize == 0) {				
				int i = row / cycleSize;				
				result = i < MappingQualityMatrix.THRESHOLDS.length ?
						"MAPQ<" + MappingQualityMatrix.THRESHOLDS[i] :
							"Average MAPQ";
			}

			return result;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			return String.class;
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return false;
		}

		public int getCycleSize() {
			return cycleSize;
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		
		int newSize = (Integer) e.getNewValue();
		setFixedRowHeight(getCycleSize(), newSize);		
	}

	private int getCycleSize() {
		return ((MappingQualityNumerationTableModel) getModel()).getCycleSize();
	}
	
	
}
