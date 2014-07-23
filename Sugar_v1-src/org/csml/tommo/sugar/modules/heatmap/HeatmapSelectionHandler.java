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
/**
 * 
 */
package org.csml.tommo.sugar.modules.heatmap;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

/**
 * @author ltrzeciakowski
 *
 */
public abstract class HeatmapSelectionHandler extends MouseAdapter {

	protected Point anchorPoint;
	protected boolean rightButtonPressed;
	
	@Override
	public void mousePressed(MouseEvent e) {
		rightButtonPressed = e.isPopupTrigger();
		if(e.getClickCount() == 1){
			JTable table = (JTable) e.getSource();

			int row = table.rowAtPoint(e.getPoint());
			int col = table.columnAtPoint(e.getPoint());
			anchorPoint = new Point(col, row);
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e){
		if(!e.isControlDown()){

			if(anchorPoint != null){
				JTable table = (JTable) e.getSource();
				
				int row = table.rowAtPoint(e.getPoint());
				int col = table.columnAtPoint(e.getPoint());
				
				if(row < 0){
					if(e.getPoint().y <= 0){
						row = 0;
					}
					else{
						row = table.getRowCount() - 1;
					}
				}
				if(col < 0){
					if(e.getPoint().x <= 0){
						col = 0;
					}
					else{
						col = table.getColumnCount() - 1;
					}
				}

				Point releasePoint = new Point(col, row);
				
				markTile(e, releasePoint);
			}
		}
	}

	protected abstract void markTile(MouseEvent e, Point releasePoint);
}
