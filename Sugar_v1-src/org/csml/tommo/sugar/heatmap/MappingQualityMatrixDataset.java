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
package org.csml.tommo.sugar.heatmap;

import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYZDataset;

public class MappingQualityMatrixDataset implements XYZDataset {

	protected MappingQualityMatrix matrix;
	
	public MappingQualityMatrixDataset(MappingQualityMatrix matrix){
		super();
		this.matrix = matrix;
	}

    public int getSeriesCount() {
        return 1;
    }
    
    public int getItemCount(int series) {
        return matrix.getSize() * matrix.getSize();
    }
    
    public Number getX(int series, int item) {
        return new Double(getXValue(series, item));
    }
    
    public double getXValue(int series, int item) {
        return item / matrix.getSize();
    }
    
    public Number getY(int series, int item) {
        return new Double(getYValue(series, item));
    }
    
    public double getYValue(int series, int item) {
        return item % (matrix.getSize());
    }
    
    public Number getZ(int series, int item) {
        return new Double(getZValue(series, item));
    }
    
    public double getZValue(int series, int item) {
        int x = (int)(getXValue(series, item));
        int y = (int)(getYValue(series, item));
        return matrix.getMeanValues()[x][y];
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
    
    public Comparable<String> getSeriesKey(int series) {
        return "quality";
    }
    
    public int indexOf(Comparable seriesKey) {
        return 0;
    }
    
    public DomainOrder getDomainOrder() {
        return DomainOrder.ASCENDING;
    }        
    
    public MappingQualityMatrix getMappingQualityMatrix() {
    	return matrix;
    }
}
