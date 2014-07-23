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

import java.awt.Color;

import org.jfree.chart.renderer.PaintScale;

public abstract class ColorPaintScale implements PaintScale {

    protected double minValue;    
    protected double maxValue;
	
	public ColorPaintScale(double minValue, double maxValue){
        if (minValue >= maxValue) {
            throw new IllegalArgumentException(
                    "LowerBound must be smaller than upperBound.");
        }
        this.minValue = minValue;
        this.maxValue = maxValue;
	}

    public double getLowerBound() {
        return minValue;
    }

    public double getUpperBound() {
        return maxValue;
    }
    
    @Override
    public Color getPaint(double value){
    	value = validateValue(value);
    	double factor = (value - minValue) / (maxValue - minValue);
    	return new Color(getRed(factor), getGreen(factor), getBlue(factor));
    }

	protected double validateValue(double value) {
		if(value < minValue){
    		value = minValue;
    	}
    	if(value > maxValue){
    		value = maxValue;
    	}
		return value;
	}

	/**
	 * 
	 * @param factor
	 * @return intensity of red color (0-255) for factor in range (0-1)
	 */
	protected abstract int getRed(double factor);
	
	/**
	 * 
	 * @param factor
	 * @return intensity of green color (0-255) for factor in range (0-1)
	 */
	protected abstract int getGreen(double factor);
	
	/**
	 * 
	 * @param factor
	 * @return intensity of blue color (0-255) for factor in range (0-1)
	 */
	protected abstract int getBlue(double factor);

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(maxValue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(minValue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ColorPaintScale other = (ColorPaintScale) obj;
		if (Double.doubleToLongBits(maxValue) != Double
				.doubleToLongBits(other.maxValue))
			return false;
		if (Double.doubleToLongBits(minValue) != Double
				.doubleToLongBits(other.minValue))
			return false;
		return true;
	}
}
