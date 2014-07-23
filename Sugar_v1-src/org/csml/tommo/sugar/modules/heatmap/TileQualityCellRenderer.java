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

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.csml.tommo.sugar.heatmap.ColorPaintScale;
import org.csml.tommo.sugar.heatmap.LinearPaintScale;
import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;

public class TileQualityCellRenderer extends DefaultTableCellRenderer{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7461814559587014817L;

	private static final Image SELECT_IMAGE = createSelectImage("org/csml/tommo/sugar/resources/tick_green.png");

	private static Image createSelectImage(String resource){
		Image result = null;
		try {
			result = ImageIO.read(ClassLoader.getSystemResource(resource));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row, int col) {

		setText(null);
		setIcon(null);
		setToolTipText("");
		setBorder(null);

		if (value instanceof Double && table instanceof TileQualityTable)
		{
			ColorPaintScale paintScale = LinearPaintScale.PAINT_SCALE;
			setBackground(paintScale.getPaint((Double)value));
			
			TileQualityTable tileTable = (TileQualityTable) table;
			MeanQualityMatrix matrix = tileTable.getModel().getMatrix();
			boolean isEntrySelected =  matrix != null && matrix.isEntrySelected(col, table.getRowCount() - 1 - row) || (tileTable.isDragging() && isSelected);
			
			if(isEntrySelected){
				ImageIcon icon = new ImageIcon(createImageForValue(tileTable, (Double)value));
				setIcon(icon);
			}

		}
		return this;
	}
	
	private Image createImageForValue(JTable table, double value) {
		int w = table.getRowHeight();
		Image result = new BufferedImage(w, w, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D)result.getGraphics();
		ColorPaintScale paintScale = LinearPaintScale.PAINT_SCALE;
//		g2.setColor(paintScale.getPaint(value));
//		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.drawImage(SELECT_IMAGE, 0, 0, w / 2, w / 2, null);
		g2.dispose();
		return result;
	}
}