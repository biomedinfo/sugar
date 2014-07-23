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
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;

public class TileQualityTable extends JTable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5872606760942763141L;

	/**
	 * @param qualityHeatMapsPerTileAndBase TODO
	 * @param arg0
	 */
	public TileQualityTable(TileQualityTableModel model) {
		super(model);
		
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setColumnWidth();
		setRowHeight();
		MouseAdapter adapter = new TileQualityTableMouseAdapter();
		addMouseListener(adapter);
		addMouseMotionListener(adapter);
		setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		setCellSelectionEnabled(true);
		setDefaultRenderer(Double.class, new TileQualityCellRenderer());
		setTableHeader(null);
		setShowHorizontalLines(false);
		setBackground(Color.GRAY);
		setGridColor(Color.GRAY);
	}
	
	@Override
	public TileQualityTableModel getModel() {
		return (TileQualityTableModel) super.getModel();
	}

	private class TileQualityTableMouseAdapter extends HeatmapSelectionHandler{
		
		@Override
		public void mouseReleased(MouseEvent e){
			
			setDragging(false);

			if(anchorPoint != null){
				int row = rowAtPoint(e.getPoint());
				int col = columnAtPoint(e.getPoint());
				if(row < 0){
					if(e.getPoint().y <= 0){
						row = 0;
					}
					else{
						row = getRowCount() - 1;
					}
				}
				if(col < 0){
					if(e.getPoint().x <= 0){
						col = 0;
					}
					else{
						col = getColumnCount() - 1;
					}
				}
				Point releasePoint = new Point(col, row);
				
				int minRow = Math.min(anchorPoint.y, releasePoint.y);
				int maxRow = Math.max(anchorPoint.y, releasePoint.y);
				
				int minCol = Math.min(anchorPoint.x, releasePoint.x);
				int maxCol = Math.max(anchorPoint.x, releasePoint.x);
				
//				boolean isSingleSelection = minCol == maxCol && minRow == maxRow;

				boolean isSelecting = !(e.isPopupTrigger() || rightButtonPressed);
				
				for(int r=minRow; r<=maxRow; r++){
					for(int c=minCol; c<=maxCol; c++){
						getModel().getMatrix().setSelectedEntry(c, getRowCount() - 1 - r, isSelecting);
					}
				}
				getModel().fireTableDataChanged();
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			setDragging(true);
		}

		@Override
		protected void markTile(MouseEvent e, Point releasePoint) {
			// Do nothing
		}			
	};
	
	protected void setColumnWidth() {
		for (int i=0; i<getColumnCount(); i++) {
			getColumnModel().getColumn(i).setPreferredWidth(getHeatMapSize());
			getColumnModel().getColumn(i).setMaxWidth(getHeatMapSize());

		}
	}
	
	public int getHeatMapSize() {
		return 20;
	}

	private void setRowHeight() {
		for (int i=0; i<getRowCount(); i++) {
			int rowHeight = getHeatMapSize();
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

}