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
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;

import org.csml.tommo.sugar.analysis.JSONSerializable;
import org.csml.tommo.sugar.modules.QualityHeatMapsPerTileAndBase;
import org.csml.tommo.sugar.modules.heatmap.ETileSelection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class MeanQualityMatrix implements Serializable, JSONSerializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6408492770171933916L;

	private static final String JSON_ATTR_TOTAL = "total";
	private static final String JSON_ATTR_NEGATIVE = "negative";
	private static final String JSON_ATTR_QUALITY_COUNTER = "quality_counter";


	protected int qualityThreshold;
	
	/**
	 * Heat-map "resolution" 
	 */	
	protected int N;	
	private int[][] negativeValueCounter;
	private int[][] totalValueCounter;
	private int[][] qualityCounter;
	private int counter = 0;

	public double[][] meanValues;
	public double[][] averageQualities;
	
	private boolean[][] selectedEntries;

	Rectangle range;
	
//	public MeanQualityMatrix(Rectangle range) {
//		this(range, Options.getMatrixSize());
//	}

	public MeanQualityMatrix(Rectangle range, int size, int qualityThreshold) {
		this.range = range;
		this.N = size;
		negativeValueCounter = new int[N][N];		
		totalValueCounter = new int[N][N];
		qualityCounter = new int[N][N];
		this.qualityThreshold = qualityThreshold;
	}

	public int addQualityValue(int x, int y, int quality)
	{
		int xIndex = convertX(x);
		int yIndex = convertY(y);		
		
		totalValueCounter[xIndex][yIndex]++;
		
		if (quality < qualityThreshold)
			negativeValueCounter[xIndex][yIndex]++;
		
		qualityCounter[xIndex][yIndex] += quality;
		
		return totalValueCounter[xIndex][yIndex];
	}
	
	public double getMeanQualityValue(int xCoord, int yCoord)
	{
		int xIndex = convertX(xCoord);
		int yIndex = convertY(yCoord);		

		return getMeanValues()[xIndex][yIndex];
	}

	public double getMeanQualityValue()
	{
		double summary = 0;
		for (int i =0; i < N; i++)
		{
			for (int j =0; j < N; j++) 
			{
				summary += getMeanValues()[i][j];
			}
		}
		return summary /(N * N);
	}

	private int convertY(int y) {
		return range.height == 0 ? N/2 :  
			(int) (N*(y-range.getMinY())/(range.getHeight()+1));
	}

	private int convertX(int x) {
		return range.width == 0 ? N/2 :  
			(int) (N*(x-range.getMinX())/(range.getWidth()+1));
	}

	public void createMeanMatrix()
	{
		meanValues = new double[N][N];
		counter = 0;
		
		for (int i =0; i < N; i++)
		{
			for (int j =0; j < N; j++) 
			{
				meanValues[i][j] = totalValueCounter[i][j] > 0 ? 
						(double) negativeValueCounter[i][j] / (double) totalValueCounter[i][j] : 
							0;
						
				counter += totalValueCounter[i][j]; 
			}
		}
		
		negativeValueCounter = null;
	}

	public void createAverageQualityMatrix()
	{
		averageQualities = new double[N][N];
		
		for (int i =0; i < N; i++)
		{
			for (int j =0; j < N; j++) 
			{
				averageQualities[i][j] = totalValueCounter[i][j] > 0 ? 
						(double) qualityCounter[i][j] / (double) totalValueCounter[i][j] : 
							0;						
			}
		}
		qualityCounter = null;
	}

	public double[][] getMeanValues(){
		if(meanValues == null){
			createMeanMatrix();
		}
		return meanValues;
	}

	public double[][] getAverageQualityMatrix(){
		if(averageQualities == null){
			createAverageQualityMatrix();
		}
		return averageQualities;
	}

	public int[][] getTotalValueCounter(){
		return totalValueCounter;
	}
	
	public Rectangle getRange() {
		return range;
	}
	
	public int getSize() {
		return N;
	}	
	
	public int getQualityThreshold() {
		return qualityThreshold;
	}
	
	public int getCounter() {
		return counter;
	}

	
	
	public void setCounter(int counter) {
		this.counter = counter;
	}

	public static MeanQualityMatrix createMixedMatrix(MeanQualityMatrix matrixTop,
			MeanQualityMatrix matrixBottom, IMixOperation mixOperation) {

		MeanQualityMatrix result = null;
		if (matrixTop != null && matrixBottom != null)
		{
			result = new MeanQualityMatrix(matrixTop.getRange(), matrixTop.getSize(), matrixTop.getQualityThreshold());
			result.mix(matrixTop, matrixBottom, mixOperation);
		}		
		
		return result;
	}
	
	public void mix(MeanQualityMatrix matrixTop,
			MeanQualityMatrix matrixBottom, IMixOperation mixOperation) {

		
//		if (matrixTop.getRange().equals(matrixBottom.getRange()))
		if (matrixTop != null && matrixBottom != null)
		{
			double[][] resultM = this.getMeanValues();
			double[][] topM = matrixTop.getMeanValues();
			double[][] bottomM = matrixBottom.getMeanValues();
			double[][] resultQ = this.getAverageQualityMatrix();
			double[][] topQ = matrixTop.getAverageQualityMatrix();
			double[][] bottomQ = matrixBottom.getAverageQualityMatrix();
			
			int[][] topTotal = matrixTop.totalValueCounter;
			int[][] bottomTotal = matrixBottom.totalValueCounter;
			
			for (int i=0; i<N; i++)
				for (int j=0; j<N; j++){
					totalValueCounter[i][j] = (topTotal[i][j] + bottomTotal[i][j]) / 2;
					resultM[i][j] = mixOperation.mix(topM[i][j], bottomM[i][j]);
					resultQ[i][j] = mixOperation.mix(topQ[i][j], bottomQ[i][j]);
				}
			
			counter = matrixTop.getCounter() + matrixBottom.getCounter();
		}
		else
			meanValues = null;
	}
	

	public BufferedImage createBufferedImage(ColorPaintScale scale) {
		
		BufferedImage bi = new BufferedImage(N, N, BufferedImage.TYPE_INT_RGB);
				
		for (int i = 0; i < N; i++)
		{
			for (int j=0; j < N; j++)
			{
				Color c = scale.getPaint(getMeanValues()[i][j]);
				bi.setRGB(i, N - 1 - j, c.getRGB());
			}				
		}
		
		return bi;
	}
	
	public BufferedImage createDensityBufferedImage(ColorPaintScale paintScale) {
		BufferedImage bi = new BufferedImage(N, N, BufferedImage.TYPE_INT_RGB);
		
		// make sure that the counter is initialized 
		getMeanValues();
		
		for (int i = 0; i < N; i++)
		{
			for (int j=0; j < N; j++)
			{
				Color c = paintScale.getPaint(totalValueCounter[i][j]);
				bi.setRGB(i, N - 1 - j, c.getRGB());
			}				
		}
		
		return bi;
	}
	
	public BufferedImage createAverageQualityBufferedImage(ColorPaintScale paintScale) {
		BufferedImage bi = new BufferedImage(N, N, BufferedImage.TYPE_INT_RGB);
		
		// make sure that the counter is initialized 
		getAverageQualityMatrix();
		
		for (int i = 0; i < N; i++)
		{
			for (int j=0; j < N; j++)
			{
				Color c = paintScale.getPaint(getAverageQualityMatrix()[i][j]);
				bi.setRGB(i, N - 1 - j, c.getRGB());
			}				
		}
		
		return bi;
	}	
	
	// customized JSON Serialization

	@Override
	public String toJSONString() {
        JSONObject obj = toJSONObject();
        return obj.toString();		        
	}

	@Override
	public void writeJSONString(Writer out) throws IOException {
        JSONObject obj = toJSONObject();
        JSONValue.writeJSONString(obj, out);		
	}
		
	@Override
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		
		JSONArray negativeArray = new JSONArray();
		JSONArray totalArray = new JSONArray();
		JSONArray qualityCounterArray = new JSONArray(); 
		
		for (int i=0; i<N; i++)
		{
			for (int j=0; j<N; j++) 
			{
				negativeArray.add(negativeValueCounter[i][j]);
				totalArray.add(totalValueCounter[i][j]);
				qualityCounterArray.add(qualityCounter[i][j]);
			}
		}		
		
        obj.put(JSON_ATTR_NEGATIVE, negativeArray);
        obj.put(JSON_ATTR_TOTAL, totalArray);
        obj.put(JSON_ATTR_QUALITY_COUNTER, qualityCounterArray);
        
		return obj;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		
		JSONArray negativeArray = (JSONArray) jsonObject.get(JSON_ATTR_NEGATIVE);
		JSONArray totalArray = (JSONArray) jsonObject.get(JSON_ATTR_TOTAL);
		JSONArray qualityCounterArray = (JSONArray) jsonObject.get(JSON_ATTR_QUALITY_COUNTER);
		
		for (int i=0; i<N; i++)
		{
			for (int j=0; j<N; j++) 
			{
				negativeValueCounter[i][j] = new Integer(negativeArray.get(i*N+j).toString());
				totalValueCounter[i][j] = new Integer(totalArray.get(i*N+j).toString());
				qualityCounter[i][j] = new Integer(qualityCounterArray.get(i*N+j).toString());
			}
		}		
		
        
        
	}

	public Double getMeanRatio() {
		
		Double result = 0.0;
		Integer valueCounter = 0;
		
		for (int i=0; i<N; i++)
		{
			for (int j=0; j<N; j++) 
			{
				result += meanValues[i][j]*totalValueCounter[i][j];
				valueCounter += totalValueCounter[i][j];				
			}
		}
		
		if (valueCounter > 0)
			result = result / valueCounter.doubleValue();
		
		return result;
	}
	
	// customized JSON Serialization

	public boolean[][] getSelectedEntries() {
		if (selectedEntries == null)
			selectedEntries = new boolean[N][N];

		return selectedEntries;
	}

	public boolean isEntrySelected(int x, int y){
		return selectedEntries != null && selectedEntries[x][y];
	}
	
	public ETileSelection getTileSelection() {
		return ETileSelection.getTileSelection(selectedEntries);
	}
	
	public boolean isAnythingSelected() {
		
		if (selectedEntries == null)
			return false;
		
		for (int i=0; i<N; i++)
		{
			for (int j=0; j<N; j++) 
			{
				if (selectedEntries[i][j])
					return true;
			}
		}
		
		return false;
	}
//
//	public boolean isEverythingSelected() {
//		
//		if (selectedEntries == null)
//			return false;
//		
//		for (int i=0; i<N; i++)
//		{
//			for (int j=0; j<N; j++) 
//			{
//				if (!selectedEntries[i][j])
//					return false;
//			}
//		}
//		
//		return true;
//	}

	public boolean isSelectedRange(Integer xCoord, Integer yCoord) {
		if (selectedEntries == null)
			return false;
		
		int xIndex = convertX(xCoord);
		int yIndex = convertY(yCoord);		

		return selectedEntries[xIndex][yIndex];
	}


	public void deselectAll() {
		selectedEntries = null;
	}

	public void selectAll() {
		if (selectedEntries == null)
			selectedEntries = new boolean[N][N];
		
		for (int i=0; i<N; i++)
		{
			for (int j=0; j<N; j++) 
			{
				selectedEntries[i][j] = true;
			}
		}		
	}

	public void selectRedArea() {		
		for (int i=0; i<N; i++)
		{
			for (int j=0; j<N; j++) 
			{
				if (meanValues[i][j] >= QualityHeatMapsPerTileAndBase.MAX_LOWQ_READS_RATIO)
					setSelectedEntry(i,j, true);
			}
		}
		
		
	}

	public void setSelectedEntry(int i, int j, boolean b) {
		if (selectedEntries == null)
			selectedEntries = new boolean[N][N];
		
		selectedEntries[i][j] = b;		
	}
	
	public static void copySelection(MeanQualityMatrix from, MeanQualityMatrix to){
		if(from != null && to != null && from != to){
			if(from.getSize() != to.getSize()){
				throw new IllegalStateException("Cannot copy selection between matrixes with different size");
			}
			for(int x=0; x<from.getSize(); x++){
				for(int y=0; y<from.getSize(); y++){
					to.setSelectedEntry(x, y, from.isEntrySelected(x, y));
				}
			}
		}
	}
}
