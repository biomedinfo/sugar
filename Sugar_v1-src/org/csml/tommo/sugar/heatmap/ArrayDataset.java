package org.csml.tommo.sugar.heatmap;

import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYZDataset;

public class ArrayDataset implements XYZDataset {

	private double[][] data;
	private int xDim;
	private int yDim;
	
	public ArrayDataset(double[][] data){
		super();
		this.data = data;
		xDim = data.length;
		yDim = data[0].length;
	}
	
    public int getSeriesCount() {
        return 1;
    }
    public int getItemCount(int series) {
        return xDim * yDim * 10 * 10;
    }
    public Number getX(int series, int item) {
        return new Double(getXValue(series, item));
    }
    public double getXValue(int series, int item) {
        return item % (yDim * 10);
    }
    public Number getY(int series, int item) {
        return new Double(getYValue(series, item));
    }
    public double getYValue(int series, int item) {
        return item / (yDim * 10);
    }
    public Number getZ(int series, int item) {
        return new Double(getZValue(series, item));
    }
    public double getZValue(int series, int item) {
        double x = getXValue(series, item);
        double y = getYValue(series, item);
        return data[(int)x/10][(int)y/10];
    }
    public void addChangeListener(DatasetChangeListener listener) {
        // ignore - this dataset never changes
    }
    public void removeChangeListener(DatasetChangeListener listener) {
        // ignore
    }
    public DatasetGroup getGroup() {
        return null;
    }
    public void setGroup(DatasetGroup group) {
        // ignore
    }
    public Comparable getSeriesKey(int series) {
        return "quality";
    }
    public int indexOf(Comparable seriesKey) {
        return 0;
    }
    public DomainOrder getDomainOrder() {
        return DomainOrder.ASCENDING;
    }        
}
