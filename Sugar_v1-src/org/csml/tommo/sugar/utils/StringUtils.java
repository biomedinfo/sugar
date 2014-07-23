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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class StringUtils {

	public static NumberFormat FORMATTER = createNumberFormat();
	
	public static String[] splitString(String source, String delimiter){
		List<String> list = new ArrayList<String>();
		int idx = 0;
		while((idx = source.indexOf(delimiter)) >= 0){
			list.add(source.substring(0, idx));
			source = source.substring(idx + 1);
		}
		if(source != null && !source.isEmpty()){
			list.add(source);			
		}
		return list.toArray(new String[list.size()]);
	}
	

	public static String formatTime(long timeInSeconds){
		String result = "";
		long time = timeInSeconds;
		long seconds = time % 60;
		time /= 60;
		long minutes = time % 60;
		time /= 60;
		long hours = time;

		result += hours + ":";
		if (minutes < 10) {
			result += "0";
		}
		result += minutes + ":";
		if(seconds < 10) {
			result += "0";
		}
		result += seconds;
		return result;
	}

	
	public static String formatTime2(long timeInSeconds){
		String result = "";
		long time = timeInSeconds;
		long seconds = time % 60;
		time /= 60;
		long minutes = time % 60;
		time /= 60;
		long hours = time % 24;
		time /= 24;
		long days = time;
		if(days > 0){
			result += formatTimePart2(days, "day") + " ";
		}
		if(hours > 0){
			result += formatTimePart2(hours, "hour") + " ";
		}
		if(minutes > 0){
			result += formatTimePart2(minutes, "minute") + " ";
		}
		if(seconds > 0){
			result += formatTimePart2(seconds, "second");
		}
		return result;
	}
	
	private static String formatTimePart2(long number, String unit){
		String result = number + " " + unit;
		if(number > 1){
			result += "s";
		}
		return result;
	}
	
	private static NumberFormat createNumberFormat() {
		DecimalFormat result = new DecimalFormat();
		result.setMaximumFractionDigits(4);
		return result;
	}
}
