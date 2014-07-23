package org.csml.tommo.sugar.heatmap;

public class AverageQualityMatrixDataset extends MeanQualityMatrixDataset {

	public AverageQualityMatrixDataset(MeanQualityMatrix matrix) {
		super(matrix);
	}

	@Override
    public double getZValue(int series, int item) {
        int x = (int)(getXValue(series, item));
        int y = (int)(getYValue(series, item));
        return matrix.getAverageQualityMatrix()[x][y];
    }
}
