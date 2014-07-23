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
package org.csml.tommo.sugar.analysis;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONSerializationUtils {

	public static void writeJSONFile(File file, JSONStreamAware jsonObject) throws IOException {
		Writer writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));			
			jsonObject.writeJSONString(writer);			
		}
		finally{
			if(writer != null){
				writer.close();
			}
		}					
	}

	public static void fromJSONFile(File file,
			JSONSerializable targetObject) throws IOException, ParseException {
		Reader reader= null;
		try{
			reader = new BufferedReader(new FileReader(file));
			
			JSONParser parser = new JSONParser();
			JSONObject jsonObj = (JSONObject) parser.parse(reader);
			
			targetObject.fromJSONObject(jsonObj);
			
		}
		finally{
			if(reader != null){
				try{
					reader.close();
				}
				catch(IOException ex){}
			}
		}	
	}
	
	public static void saveMapInJSONObject(JSONObject obj, Map map, String mapName) {
		JSONArray keyArray = new JSONArray();
		JSONArray valueArray = new JSONArray();
		for(Object key : map.keySet()) {
			keyArray.add(key);
			valueArray.add(map.get(key));
		}		
		
        obj.put(mapName + ".keys", keyArray);
        obj.put(mapName + ".values", valueArray);
	}
	
	public static JSONArray rect2json(Rectangle r) {
		
		JSONArray result = new JSONArray();
		
		result.add(r.x);
		result.add(r.y);
		result.add(r.width);
		result.add(r.height);
		
		return result;
		
	}
	
	public static Rectangle json2rect(JSONArray jsonArray) {
		
		return new Rectangle(
				new Integer(jsonArray.get(0).toString()), 
				new Integer(jsonArray.get(1).toString()), 
				new Integer(jsonArray.get(2).toString()), 
				new Integer(jsonArray.get(3).toString()));
	}

	public static JSONArray matrix2json(int[][] values) {
		
		JSONArray result = new JSONArray();
		
		for (int i=0; i<values.length; i++)
		{
			int N = values[i].length;
			for (int j=0; j<N; j++) 
			{
				result.add(values[i][j]);
			}
		}		

		return result;		
	}

	public static void json2matrix(JSONArray jsonArray, int[][] matrix) {
		
		for (int i=0; i<matrix.length; i++)
		{
			int N = matrix[i].length;
			for (int j=0; j<N; j++) 
			{
				matrix[i][j] = new Integer(jsonArray.get(i*N+j).toString());
			}
		}
	}

	public static JSONArray matrix2json(boolean[][] values) {
		JSONArray result = new JSONArray();
		
		for (int i=0; i<values.length; i++)
		{
			int N = values[i].length;
			for (int j=0; j<N; j++) 
			{
				result.add(values[i][j] ? "1" : "0");
			}
		}		

		return result;		
	}
	
	public static void json2matrix(JSONArray jsonArray, boolean[][] matrix) {
		
		for (int i=0; i<matrix.length; i++)
		{
			int N = matrix[i].length;
			for (int j=0; j<N; j++) 
			{
				matrix[i][j] = "1".equals(jsonArray.get(i*N+j).toString());
			}
		}
	}
}
