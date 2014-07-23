package org.csml.tommo.sugar.modules.heatmap;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;

import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;
import org.csml.tommo.sugar.modules.EClearLowQClustersMethod;


public class SelectableMatrixCellRenderer extends MatrixCellRenderer {
	
	EClearLowQClustersMethod clearMethod;
	
	public SelectableMatrixCellRenderer(EClearLowQClustersMethod clearMethod) {
		super();
		this.clearMethod = clearMethod;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
		if(table instanceof ResultsTable){
			ResultsTable resultsTable = (ResultsTable) table;
			MeanQualityMatrix matrix = (MeanQualityMatrix) resultsTable.getValueAt(row, col);
			
			if (matrix != null)
			{
			
				ETileSelection tileSelection = clearMethod == EClearLowQClustersMethod.DELETE ?
						resultsTable.getModel().getTileSelection(row, col) :
						matrix.getTileSelection();			

				// if is dragging check the table selection model
				if (resultsTable.isDragging() && isSelected) {
					// if not partial, set to ALL selection 
					if (!ETileSelection.PART.equals(tileSelection)) {
						tileSelection = ETileSelection.ALL; 
					}
				}

				if(label.getIcon() != null){
					ImageIcon icon = (ImageIcon)label.getIcon();
					Image image = icon.getImage();
					
					Image selectionIcon = tileSelection.getIcon();
					if(selectionIcon != null){
						icon.setImage(markImageAsSelected(image, selectionIcon));					
					}
				}
			}
		}
		return label;
	}

	private Image markImageAsSelected(Image image, Image decoration) {
		Image result = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D)result.getGraphics();
		g2.drawImage(image, 0, 0, null);
		g2.drawImage(decoration, 0, 0, image.getWidth(null) / 2,image.getHeight(null) / 2, null);
		g2.dispose();
		return result;
	}

}
