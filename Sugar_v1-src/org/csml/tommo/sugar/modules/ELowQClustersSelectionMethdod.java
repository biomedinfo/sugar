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
package org.csml.tommo.sugar.modules;

import org.csml.tommo.sugar.utils.Options;


public enum ELowQClustersSelectionMethdod {
	
	AUTO ("Auto","auto"),
	USER ("User","user"),
	FILE ("File", "file");	
	
	private String name;
	private String shortName;

	/**
	 * @param name
	 */
	private ELowQClustersSelectionMethdod(String name, String shortName) {
		this.name = name;
		this.shortName = shortName;
	}
	
	
	public String getName() {
		return name;
	}

	public String getShortName() {
		return shortName;
	}

	
	static public ELowQClustersSelectionMethdod fromString(String s)
	{
		ELowQClustersSelectionMethdod result = null;
		
		for (ELowQClustersSelectionMethdod m : values())
		{
			if (m.toString().equals(s))
			{	
				result = m;
				break;
			}
		}
		
		if (result == null)
		{
			if (s != null && !s.isEmpty())
				System.err.println("Didn't understand " + Options.CLEAR_LOWQ_CLUSTERS +  " option: '"+s+"'. Nothing will be cleared");
			
			result = AUTO;
		}			
		
		return result;
	}
	
	public String toString()
	{		
		return shortName;	
	}


	public static ELowQClustersSelectionMethdod[] methods() {

		return new ELowQClustersSelectionMethdod[] {AUTO, FILE};

		
	}

}
