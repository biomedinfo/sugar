package org.csml.tommo.sugar.heatmap;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;

import org.csml.tommo.sugar.analysis.JSONSerializable;
import org.csml.tommo.sugar.analysis.JSONSerializationUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class MappingQualityMatrix implements Serializable, JSONSerializable {

	public static final int[] THRESHOLDS = new int[]{5, 10, 15, 20, 25, 30, 35, 40};
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 289911293145133620L;

	private static final String JSON_ATTR_COUNTER = "counter";
	private static final String JSON_ATTR_SUMMARY = "summary";
	private static final String JSON_ATTR_THRESHOLDS_MAP = "thresholdsMap";

	/**
	 * Heat-map "resolution" 
	 */	

	protected int N;	
	private int[][] summaryTable;
	private int[][] counterTable;
	Map<Integer, int[][]> thresholdsMap;

	public double[][] meanValues;

	Rectangle range;
	
//	public MeanQualityMatrix(Rectangle range) {
//		this(range, Options.getMatrixSize());
//	}

	public MappingQualityMatrix(Rectangle range, int size) {
		this.range = range;
		this.N = size;
		summaryTable = new int[N][N];		
		counterTable = new int[N][N];
		thresholdsMap = createThresholdsMap();
	}

	private Map<Integer, int[][]> createThresholdsMap() {
		Map<Integer, int[][]> result = new TreeMap<Integer, int[][]>();
		for(int threshold: THRESHOLDS){
			result.put(threshold, new int[N][N]);
		}
		return result;
	}

	public void addQualityValue(int x, int y, int quality)
	{
		int xIndex = convertX(x);
		int yIndex = convertY(y);		
		
		counterTable[xIndex][yIndex]++;
		summaryTable[xIndex][yIndex] += quality;
		
		for(Integer threshold: thresholdsMap.keySet()){
			if(quality < threshold){
				thresholdsMap.get(threshold)[xIndex][yIndex]++;
			}
		}
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
		
		for (int i=0; i < N; i++)
		{
			for (int j=0; j < N; j++) 
			{
				meanValues[i][j] = counterTable[i][j] > 0 ? 
						(double) summaryTable[i][j] / (double) counterTable[i][j] : 
							0;			
			}
		}
	}
	
	public double[][] getMeanMatrixByThreshold(int threshold){
		double[][] result = new double[N][N];
		if(thresholdsMap.containsKey(threshold)){
			for (int i=0; i < N; i++)
			{
				for (int j=0; j < N; j++) 
				{
					int[][] thresholdTable = thresholdsMap.get(threshold);
					result[i][j] = counterTable[i][j] > 0 ? 
							(double) thresholdTable[i][j] / (double) counterTable[i][j] : 
								0;			
				}
			}			
		}
		return result;
	}
	
	public double[][] getMeanValues(){
		if(meanValues == null){
			createMeanMatrix();
		}
		return meanValues;
	}

	public Rectangle getRange() {
		return range;
	}
	
	public int getSize() {
		return N;
	}	
	
	public static MappingQualityMatrix createMixedMatrix(MappingQualityMatrix matrixTop,
			MappingQualityMatrix matrixBottom, IMixOperation mixOperation) {

		MappingQualityMatrix result = null;
		if (matrixTop != null && matrixBottom != null)
		{
			result = new MappingQualityMatrix(matrixTop.getRange(), matrixTop.getSize());
			result.mix(matrixTop, matrixBottom, mixOperation);
		}		
		
		return result;
	}
	
	public void mix(MappingQualityMatrix matrixTop,
			MappingQualityMatrix matrixBottom, IMixOperation mixOperation) {

		
//		if (matrixTop.getRange().equals(matrixBottom.getRange()))
		if (matrixTop != null && matrixBottom != null)
		{
			double[][] resultM = this.getMeanValues();
			double[][] topM = matrixTop.getMeanValues();
			double[][] bottomM = matrixBottom.getMeanValues();
			
			for (int i=0; i<N; i++)
				for (int j=0; j<N; j++){
					resultM[i][j] = mixOperation.mix(topM[i][j], bottomM[i][j]);
				}			
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

	public BufferedImage createBufferedImageForThreshold(int threshold, ColorPaintScale scale) {
		
		BufferedImage bi = new BufferedImage(N, N, BufferedImage.TYPE_INT_RGB);
				
		double[][] data = getMeanMatrixByThreshold(threshold);
		
		for (int i = 0; i < N; i++)
		{
			for (int j=0; j < N; j++)
			{
				Color c = scale.getPaint(data[i][j]);
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
		
        // counterTable
		JSONArray counterArray = JSONSerializationUtils.matrix2json(counterTable);
        obj.put(JSON_ATTR_COUNTER, counterArray);

        // summaryTable
        JSONArray summaryArray = JSONSerializationUtils.matrix2json(summaryTable);		
        obj.put(JSON_ATTR_SUMMARY, summaryArray);
        
        
        // thresholdsMap
		JSONArray thresholdMapKeyArray = new JSONArray();
		JSONArray thresholdMapValueArray = new JSONArray();
		
		for(Integer threshold : thresholdsMap.keySet()) {
			thresholdMapKeyArray.add(threshold);
			
			JSONArray thresholdArray = JSONSerializationUtils.matrix2json(thresholdsMap.get(threshold)); 
			thresholdMapValueArray.add(thresholdArray);
		}		
		
        obj.put(JSON_ATTR_THRESHOLDS_MAP + ".keys", thresholdMapKeyArray);
        obj.put(JSON_ATTR_THRESHOLDS_MAP + ".values", thresholdMapValueArray);

		return obj;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		
        // counterTable
		JSONArray counterArray = (JSONArray) jsonObject.get(JSON_ATTR_COUNTER);
		JSONSerializationUtils.json2matrix(counterArray, counterTable);

        // summaryTable
		JSONArray summaryArray = (JSONArray) jsonObject.get(JSON_ATTR_SUMMARY);		
		JSONSerializationUtils.json2matrix(summaryArray, summaryTable);
				
        // thresholdsMap
		JSONArray thresholdMapKeyArray = (JSONArray) jsonObject.get(JSON_ATTR_THRESHOLDS_MAP + ".keys");
		JSONArray thresholdMapValueArray = (JSONArray) jsonObject.get(JSON_ATTR_THRESHOLDS_MAP + ".values");
		
		for (int i = 0 ; i < thresholdMapKeyArray.size(); i++)
		{
			Integer threshold = new Integer(thresholdMapKeyArray.get(i).toString());
			JSONArray jsonThresholdMatrix = (JSONArray) thresholdMapValueArray.get(i);
			
			JSONSerializationUtils.json2matrix(jsonThresholdMatrix, thresholdsMap.get(threshold));
		}				
	}
	
	// customized JSON Serialization
}
