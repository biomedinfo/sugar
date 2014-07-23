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
package org.csml.tommo.sugar.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.csml.tommo.sugar.modules.EClearLowQClustersMethod;

public class Options {

	public static final String TRUE = "true";
	
	public static final String THREADS_OPTION = "fastqc.threads";
	public static final String QUIET_OPTION = "fastqc.quiet";

	public static final String MATRIX_SIZE_OPTION = "sugar.matrix_size";
	public static final String HEATMAP_QUALITY_THRESHOLD_OPTION = "sugar.heatmap_quality_threshold";
	public static final String HEATMAP_SIZE_OPTION = "sugar.heatmap_size";
	public static final String HEATMAP_IMAGE_SIZE_OPTION = "sugar.heatmap_image_size";
	public static final String READ_RATE_OPTION = "sugar.read_rate";
	public static final String NO_CACHE = "sugar.nocache";
	public static final String CLEAR_LOWQ_CLUSTERS = "sugar.clear_lowq_clusters";
	public static final String CLEAR_LOWQ_CLUSTERS_FILE = "sugar.clear_lowq_clusters_file";
	
	public static final String HEADLESS_ENVIRONMENT = "java.awt.headless";
	public static final String DEBUG = "sugar.debug";
	
	public static final String SUGAR_SHOW_VERSION = "sugar.show_version";

	
	public static final int DEFAULT_MATRIX_SIZE = 10;
	public static final Integer DEFAULT_QUALITY_THRESHOLD = 20;
	public static final int DEFAULT_HEATMAP_SIZE = 20;
	public static final int DEFAULT_HEATMAP_IMAGE_SIZE = 20;
	public static final int DEFAULT_THREADS = 1;
	public static final int DEFAULT_READ_RATE = 1;
		
	public static int getMatrixSize(){
		int result = DEFAULT_MATRIX_SIZE;
		if(System.getProperty(MATRIX_SIZE_OPTION) != null){
			try {
				result = Integer.parseInt(System.getProperty(MATRIX_SIZE_OPTION));
			} catch (NumberFormatException e) {
			}
		}
		return result;
	}
	
	public static String getHeatmapQualityThreshold() {
		String result = DEFAULT_QUALITY_THRESHOLD.toString();
		if(System.getProperty(HEATMAP_QUALITY_THRESHOLD_OPTION) != null){
			result = System.getProperty(HEATMAP_QUALITY_THRESHOLD_OPTION);
		}
		return result;
	}

	public static Integer[] getHeatmapQualityThresholdArray() {		
		String thresholdString = getHeatmapQualityThreshold();
		
		return getHeatmapQualityThresholdArray(thresholdString);
	}

	public static Integer[] getHeatmapQualityThresholdArray(
			String thresholdString) {
		List<Integer> result = new ArrayList<Integer>();

		// first separate by comma
		String[] parts = thresholdString.split(",");		
		for (String part : parts) {

			//then separate by minus
			String[] subparts = part.split("-");		
			
			if (subparts.length == 2) {
				
				int minValue = new Integer(subparts[0].trim());
				int maxValue = new Integer(subparts[1].trim());
				
				for (int i = minValue; i <= maxValue; i++)
				{
					if (!result.contains(i))
						result.add(i);
				}
				
			}
			else if (subparts.length == 1) {
				int i = new Integer(subparts[0].trim());
				if (!result.contains(i))
					result.add(i);
			}
		}
		
		return result.toArray(new Integer[0]);
	}

	public static boolean validateHeatmapQualityThreshold() {
		return validateHeatmapQualityThreshold(getHeatmapQualityThreshold());
	}

	public static boolean validateHeatmapQualityThreshold(String thresholdString) {
		boolean result = true;
		
		try {
			Integer[] thresholds = getHeatmapQualityThresholdArray(thresholdString);
			if (thresholds.length < 1 || thresholds.length > 50)
				result = false;
		} catch (Exception e) {
			e.printStackTrace();
			
			result = false;
			
		}
		return result;
	}

	
	
	public static int getHeatmapImageSize(){
		int result = DEFAULT_HEATMAP_IMAGE_SIZE;
		if(System.getProperty(HEATMAP_IMAGE_SIZE_OPTION) != null){
			try {
				result = Integer.parseInt(System.getProperty(HEATMAP_IMAGE_SIZE_OPTION));
			} catch (NumberFormatException e) {
			}
		}
		return result;
	}
	
	public static void setHeatmapImageSize(Integer size) {
		
		if (size > 0)
			System.setProperty(HEATMAP_IMAGE_SIZE_OPTION, size.toString());
	}

	public static int getThreads(){
		int result = DEFAULT_THREADS;
		if(System.getProperty(THREADS_OPTION) != null){
			try {
				result = Integer.parseInt(System.getProperty(THREADS_OPTION));
			} catch (NumberFormatException e) {
			}
		}
		return result;
	}

	public static int getReadRate(){
		int result = DEFAULT_READ_RATE;
		if(System.getProperty(READ_RATE_OPTION) != null){
			try {
				result = Integer.parseInt(System.getProperty(READ_RATE_OPTION));
			} catch (NumberFormatException e) {
			}
		}
		return result;
	}
	
	public static boolean getNoCache(){
		return TRUE.equals(System.getProperty(NO_CACHE));
	}

	public static EClearLowQClustersMethod getClearLowQClustersMethod(){
		return EClearLowQClustersMethod.fromString(System.getProperty(CLEAR_LOWQ_CLUSTERS));
	}
	
	public static File getClearLowQClustersFile() {
		return System.getProperty(CLEAR_LOWQ_CLUSTERS_FILE) != null ?
				new File(System.getProperty(CLEAR_LOWQ_CLUSTERS_FILE)) :
				null;
	}

	public static boolean isHeadless() {
		return TRUE.equals(System.getProperty(HEADLESS_ENVIRONMENT));
	}

	public static boolean isDebug() {
		// TODO Auto-generated method stub
		return TRUE.equals(System.getProperty(DEBUG));
	}
	
	public static boolean isQuiet() {
		return TRUE.equals(System.getProperty(QUIET_OPTION));
	}

	
	public static String validateInput() {
		String errorMessage = null;

		if (getMatrixSize() < 1)
		{
			errorMessage = "Invalid matrix size. Matrix size must be an integer number greater than zero";
		}
		else if (!Options.validateHeatmapQualityThreshold())
		{
			errorMessage = "Invalid quality threshold. It must be an integer number or range in the form min-max. These values can be separated by commas.";
		}
		if (getHeatmapImageSize() < 1)
		{
			errorMessage = "Heatmap image size matrix size. Heatmap image size must be an integer number greater than zero";
		}
		else if (getThreads() < 1)
		{
			errorMessage = "Invalid threads value. Threads value must be an integer number greater than zero";
		}
		else if (getReadRate() < 1)
		{
			errorMessage = "Invalid read rate. Read rate must be an integer number greater than zero";
		}
		
		return errorMessage;		
			
	}

	public static boolean showVersion() {
		return System.getProperty(SUGAR_SHOW_VERSION) != null && System.getProperty(SUGAR_SHOW_VERSION).equals(TRUE);
	}	

}
