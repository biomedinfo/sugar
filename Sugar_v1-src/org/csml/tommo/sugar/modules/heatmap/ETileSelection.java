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
package org.csml.tommo.sugar.modules.heatmap;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

public enum ETileSelection {

	ALL,
	PART,
	NONE;
	
	private static final Image SELECT_IMAGE = createSelectImage("org/csml/tommo/sugar/resources/tick_black.png");
	private static final Image PARTIAL_SELECT_IMAGE = createSelectImage("org/csml/tommo/sugar/resources/tick_green.png");
	
	private static Image createSelectImage(String resource){
		Image result = null;
		try {
			result = ImageIO.read(ClassLoader.getSystemResource(resource));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public Image getIcon() {
		
		if (this.equals(ALL))
			return SELECT_IMAGE;
		if (this.equals(PART))
			return PARTIAL_SELECT_IMAGE; 
		
		return null;
	}

	
	public static ETileSelection getTileSelection(boolean[][] matrix) {
		ETileSelection result = ETileSelection.NONE;
		boolean UnSelectedFlag = false;
		boolean anythingSelected = false;
		
		if (matrix == null)
			return result; 
			
			
		for (int i =0; i<matrix.length; i++)
		{
			for (int j =0; j<matrix[i].length; j++)
			{
				if (matrix[i][j]) {
					anythingSelected = true;
				}
				else {
					UnSelectedFlag = true;
				}
				
				if (anythingSelected && UnSelectedFlag)
					break;
			}
		}
		
		if (anythingSelected)
		{
			if (UnSelectedFlag)
				result = ETileSelection.PART;
			else 
				result = ETileSelection.ALL;
		}
		return result;
	}

	
}
