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
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;

import org.csml.tommo.sugar.heatmap.ColorPaintScale;
import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;
import org.csml.tommo.sugar.heatmap.TemperaturePaintScale;

public class DensityMatrixCellRenderer extends DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row, int col) {

		setText(null);
		setIcon(null);
		setToolTipText("");
		setBorder(null);

		if (value instanceof MeanQualityMatrix)
		{
			// add separator (border) to group cycles
			if (table instanceof DensityResultsTable){
				DensityResultsTable resultsTable = (DensityResultsTable) table;
				String tooltip = resultsTable.getModel().getTooltipAt(row, col);
				setToolTipText(tooltip);
				
				ColorPaintScale paintScale = getPaintScale(resultsTable.getMaxDensity());

				MeanQualityMatrix matrix = (MeanQualityMatrix) value;				
				Image image = getImage(paintScale, matrix);
				Icon icon = new ImageIcon(image.getScaledInstance(resultsTable.getHeatMapSize(), resultsTable.getHeatMapSize(), Image.SCALE_FAST));
				setHorizontalAlignment(SwingConstants.CENTER);
				setIcon(icon);										

				int cycleSize = resultsTable.getModel().getTileNumeration().getCycleSize();					

				int top = (row % cycleSize == 0) ? 1 : 0; 
				int bottom = (row % cycleSize == cycleSize - 1) ? 1 : 0;
				int left = col == resultsTable.getModel().getTopBottomSeparatorColumn() + 1 ? 1 : 0;
				int right = col == resultsTable.getModel().getTopBottomSeparatorColumn() ? 1 : 0;
				//					Border inBorder = BorderFactory.createMatteBorder(top, left, bottom, right, Color.GRAY);
				Border outBorder = BorderFactory.createEmptyBorder(top * ResultsTable.CELL_SEPARATOR_SIZE, 
						left * ResultsTable.COLUMN_SEPARATOR_SIZE, 
						bottom * ResultsTable.CELL_SEPARATOR_SIZE, 
						right * ResultsTable.COLUMN_SEPARATOR_SIZE);
				//					setBorder(BorderFactory.createCompoundBorder(outBorder, inBorder));
				setBorder(outBorder);
			}
		}
		else {
			if (table instanceof DensityResultsTable){
				DensityResultsTable resultsTable = (DensityResultsTable) table;
				String tooltip = "No data for tile:" + resultsTable.getModel().getTooltipAt(row, col);
				setToolTipText(tooltip);
			}
		}
		return this;
	}

	protected Image getImage(ColorPaintScale paintScale,
			MeanQualityMatrix matrix) {
		Image image = matrix.createDensityBufferedImage(paintScale);
		return image;
	}

	public static ColorPaintScale getPaintScale(int maxValue) {
		return new TemperaturePaintScale(0, maxValue);
	}
}