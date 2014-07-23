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

public enum MixOperation {

	AVERAGE(new AverageOperation(0.5)),
	DIFF(new DiffOperation()),
	ABSOLUTE_DIFF(new AbsoluteDiffOperation());
	
	private IMixOperation mixer;
	
	private MixOperation(IMixOperation mixer){
		this.mixer = mixer;
	}
	
	public double mix(double topValue, double bottomValue){
		return mixer.mix(topValue, bottomValue);
	}
	
	public IMixOperation getMixer(){
		return mixer;
	}
	
	public static MixOperation fromMixer(IMixOperation mixer){
		MixOperation result = null;
		if(mixer != null){
			for(MixOperation mixOperation: values()){
				if(mixOperation.getMixer().getClass().equals(mixer.getClass())){
					result = mixOperation;
					break;
				}
			}			
		}
		return result;
	}
}
