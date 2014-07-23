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



public class TemperaturePaintScale extends ColorPaintScale{
	
	public static final TemperaturePaintScale AVERAGE_QUALITY_DIFF_PAINT_SCALE = new TemperaturePaintScale(-10d, 10d);
	public static final TemperaturePaintScale AVERAGE_QUALITY_ABS_DIFF_PAINT_SCALE = new TemperaturePaintScale(0d, 10d);

	public TemperaturePaintScale(double minValue, double maxValue){
		super(minValue, maxValue);
	}

	@Override
	protected int getRed(double factor){
		int red = 0;
		if(factor >= 0.75d){
			red = 255;
		}
		if(factor >= 0.5d && factor < 0.75d){
			red = (int)(255 * 4 * (factor - 0.5d));
		}
		return red;
	}
	
	@Override
	protected int getGreen(double factor){
		int green = 0;
		if(factor <= 0.25d){
			green = (int)(255 * 4 * factor);			
		}
		else if(factor >= 0.75d){
			green = (int)(255 - 255 * 4 * (factor - 0.75d));
		}
		else{
			green = 255;			
		}
		return green;
	}

	@Override
	protected int getBlue(double factor){
		int blue = 0;
		if(factor <= 0.25d){
			blue = 255;
		}
		if(factor > 0.25d && factor <= 0.5d){
			blue = (int)(255 - 255 * 4 * (factor - 0.25d));
		}
		return blue;
	}	
}
