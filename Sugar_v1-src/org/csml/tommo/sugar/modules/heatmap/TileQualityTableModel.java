package org.csml.tommo.sugar.modules.heatmap;

import javax.swing.table.AbstractTableModel;

import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;

public class TileQualityTableModel extends AbstractTableModel {

	private MeanQualityMatrix matrix;
	
	public TileQualityTableModel(MeanQualityMatrix matrix){
		super();
		this.matrix = matrix;
	}
	
	@Override
	public int getColumnCount() {
		return matrix.getSize();
	}

	@Override
	public int getRowCount() {
		return matrix.getSize();
	}

	@Override
	public Double getValueAt(int row, int col) {
		return matrix.getMeanValues()[col][getRowCount() - 1 - row];
	}

	@Override
	public Class<?> getColumnClass(int col) {
		return Double.class;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	public MeanQualityMatrix getMatrix() {
		return matrix;
	}
}
