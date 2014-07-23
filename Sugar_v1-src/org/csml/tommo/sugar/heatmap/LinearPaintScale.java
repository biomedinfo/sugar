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

import org.csml.tommo.sugar.modules.QualityHeatMapsPerTileAndBase;
import org.jfree.util.PublicCloneable;

public class LinearPaintScale extends ColorPaintScale implements PublicCloneable{

	public static ColorPaintScale PAINT_SCALE = new LinearPaintScale(0d, QualityHeatMapsPerTileAndBase.MAX_LOWQ_READS_RATIO, Color.YELLOW.brighter(), Color.RED.darker());
	public static final ColorPaintScale AVERAGE_QUALITY_PAINT_SCALE = new LinearPaintScale(10d, 50d, Color.RED.darker(), Color.YELLOW.brighter());
	
    protected Color minColor;
    protected Color maxColor;
    
    public LinearPaintScale() {
        this(0.0, 1.0, new Color(0, 0, 0), new Color(255, 255, 255));
    }
    
    public LinearPaintScale(double minValue, double maxValue, Color minColor, Color maxColor){
    	super(minValue, maxValue);
    	this.minColor = minColor;
        this.maxColor = maxColor;
    }
    
	@Override
	protected int getRed(double factor) {
    	int red = (int)(minColor.getRed() + factor * (maxColor.getRed() - minColor.getRed()));
    	return red;
	}
	
	@Override
	protected int getGreen(double factor) {
    	int green = (int)(minColor.getGreen() + factor * (maxColor.getGreen() - minColor.getGreen()));
    	return green;
	}
	
	@Override
	protected int getBlue(double factor) {
    	int blue = (int)(minColor.getBlue() + factor * (maxColor.getBlue() - minColor.getBlue()));
    	return blue;
	}

	public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((maxColor == null) ? 0 : maxColor.hashCode());
		result = prime * result
				+ ((minColor == null) ? 0 : minColor.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		LinearPaintScale other = (LinearPaintScale) obj;
		if (maxColor == null) {
			if (other.maxColor != null)
				return false;
		} else if (!maxColor.equals(other.maxColor))
			return false;
		if (minColor == null) {
			if (other.minColor != null)
				return false;
		} else if (!minColor.equals(other.minColor))
			return false;
		return true;
	}
}
