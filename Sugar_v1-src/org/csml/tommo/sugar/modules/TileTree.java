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

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.csml.tommo.sugar.analysis.DefaultTileNumeration;
import org.csml.tommo.sugar.analysis.HiSeqNumeration;
import org.csml.tommo.sugar.analysis.HiSeqRapidRunNumeration;
import org.csml.tommo.sugar.analysis.JSONFileSerializable;
import org.csml.tommo.sugar.analysis.JSONSerializationUtils;
import org.csml.tommo.sugar.analysis.MiSeqNumeration;
import org.csml.tommo.sugar.analysis.TileNumeration;
import org.csml.tommo.sugar.sequence.LaneCoordinates;
import org.csml.tommo.sugar.sequence.SequenceCoordinates;
import org.csml.tommo.sugar.sequence.TileCoordinates;
import org.csml.tommo.sugar.utils.TreeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import uk.ac.babraham.FastQC.Report.HTMLReportArchive;
import uk.ac.babraham.FastQC.Sequence.Sequence;
import uk.ac.babraham.FastQC.Sequence.QualityEncoding.PhredEncoding;

public class TileTree implements SugarModule, Serializable, JSONFileSerializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5049640102335244933L;
	private static final String JSON_ATTR_XY_RANGE_MAP_VALUES = "xyRangeMap.values";
	private static final String JSON_ATTR_XY_RANGE_MAP_KEYS = "xyRangeMap.keys";
	private static final String JSON_ATTR_TILE_IDS_MAP_VALUES = "tileIDsMap.values";
	private static final String JSON_ATTR_TILE_IDS_MAP_KEYS = "tileIDsMap.keys";
	private static final String JSON_ATTR_LANE_IDS_MAP_VALUES = "laneIDsMap.values";
	private static final String JSON_ATTR_LANE_IDS_MAP_KEYS = "laneIDsMap.keys";
	private static final String JSON_ATTR_FLOW_CELL_SET = "flowCellSet";
	private static final String JSON_ATTR_LOWEST_CHAR = "lowestChar";	
	
	
	private char lowestChar = 126;
	private PhredEncoding phredEncoding;
	private TileNumeration tileNumeration;
	
	
	/**
	 * Gathers all flow cell names in an ordered set 
	 */
	private SortedSet<String> flowCellSet = new TreeSet<String>();
	
	/**
	 * Gathers all lane IDs in an ordered set 
	 * 
	 *  Assumes that different flow cells may have different lane IDs
	 */
	private Map<String, SortedSet<Integer>> laneIDsMap = new HashMap<String, SortedSet<Integer>>();
	
	/**
	 * Gathers all tile IDs in an ordered set
	 * 
	 *  Assumes that different lanes may have different tile IDs
	 */
	private Map<LaneCoordinates, SortedSet<Integer>> tileIDsMap = new HashMap<LaneCoordinates, SortedSet<Integer>>();

	/**
	 * Gathers all xCooridnates in an ordered set
	 * 
	 *  Assumes that different tiles may have different xCooridnates range 
	 */
//	private Map<TileCoordinates, SortedSet<Integer>> xCoordinatesMap = new HashMap<TileCoordinates, SortedSet<Integer>>();

	/**
	 * Gathers all yCooridnates in an ordered set
	 * 
	 *  Assumes that different tiles may have different yCooridnates range 
	 */
//	private Map<TileCoordinates, SortedSet<Integer>> yCoordinatesMap = new HashMap<TileCoordinates, SortedSet<Integer>>();

	
	/**
	 * Keeps the rang of x- and y- cooridnates for each tile
	 * 
	 *  Assumes that different tiles may have different xCooridnates range 
	 */
	private Map<TileCoordinates, Rectangle> xyRangeMap = new HashMap<TileCoordinates, Rectangle>();

	
	@Override
	public void processSequence(Sequence sequence) {
		
		char[] chars = sequence.getQualityString().toCharArray();
		for (int c=0;c<chars.length;c++) {
			if (chars[c] < lowestChar) {
				lowestChar = chars[c];
			}
		}
		
		SequenceCoordinates seqCoord = SequenceCoordinates.createSequenceCoordinates(sequence);

		// storeSequenceCoordinates(SequenceCoordinates seqCoord)
		String flowCell = seqCoord.getFlowCell();
		
		flowCellSet.add(flowCell);
		
		SortedSet<Integer> laneIDs = laneIDsMap.get(flowCell);
		if (laneIDs == null)
		{
			laneIDs = new TreeSet<Integer>();
			laneIDsMap.put(flowCell, laneIDs);
		}
		laneIDs.add(seqCoord.getLane());

		LaneCoordinates laneCoordinates = new LaneCoordinates(seqCoord.getFlowCell(), seqCoord.getLane());
		SortedSet<Integer> tileIDs = tileIDsMap.get(laneCoordinates);
		if (tileIDs == null)
		{
			tileIDs = new TreeSet<Integer>();
			tileIDsMap.put(laneCoordinates, tileIDs);
		}
		tileIDs.add(seqCoord.getTile());
		
		TileCoordinates tileCoordinates = new TileCoordinates(seqCoord.getFlowCell(), seqCoord.getLane(), seqCoord.getTile());
//		SortedSet<Integer> xCoordinates = xCoordinatesMap.get(tileCoordinates);
//		if (xCoordinates == null)
//		{
//			xCoordinates = new TreeSet<Integer>();
//			xCoordinatesMap.put(tileCoordinates, xCoordinates);
//		}
//		xCoordinates.add(seqCoord.getX());
//		
//		SortedSet<Integer> yCoordinates = yCoordinatesMap.get(tileCoordinates);
//		if (yCoordinates == null)
//		{
//			yCoordinates = new TreeSet<Integer>();
//			yCoordinatesMap.put(tileCoordinates, yCoordinates);
//		}		
//		yCoordinates.add(seqCoord.getY());
		
		Rectangle range = xyRangeMap.get(tileCoordinates);
		if (range == null)
		{
			range = new Rectangle(0,0,-1,-1);
			xyRangeMap.put(tileCoordinates, range);
		}
		range.add(seqCoord.getX(), seqCoord.getY());
		
	}

	@Override
	public JPanel getResultsPanel() {
		JPanel returnPanel = new JPanel();
		returnPanel.setLayout(new BorderLayout());
		returnPanel.add(new JLabel("Tile tree with the X-, Y-Coordinate ranges",JLabel.CENTER),BorderLayout.NORTH);
		
		JTree tileTree = new JTree(createTileTree());
		TreeUtils.expandTree(tileTree);
		returnPanel.add(new JScrollPane(tileTree), BorderLayout.CENTER);		
		return returnPanel;
	}

	public DefaultMutableTreeNode createTileTree(){
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Tile Numeration: " + getTileNumeration().getName());
		for(String flowCell: flowCellSet){
			DefaultMutableTreeNode flowCellNode = new DefaultMutableTreeNode("Flow Cell: " + flowCell);
			root.add(flowCellNode);
			SortedSet<Integer> lanes = laneIDsMap.get(flowCell);
			for(Integer lane : lanes){
				DefaultMutableTreeNode laneNode = new DefaultMutableTreeNode("Lane: " + lane);
				flowCellNode.add(laneNode);
				LaneCoordinates lc = new LaneCoordinates(flowCell, lane);
				SortedSet<Integer> tiles = tileIDsMap.get(lc);
				for(Integer tile : tiles){
					DefaultMutableTreeNode tileNode = new DefaultMutableTreeNode("Tile: " + tile);
					laneNode.add(tileNode);
					TileCoordinates tc = new TileCoordinates(flowCell, lane, tile);
//					SortedSet<Integer> xSet = xCoordinatesMap.get(tc);
//					SortedSet<Integer> ySet = yCoordinatesMap.get(tc);
//					
//					String xMessage = "X size=" + xSet.size() + ", "; 
//					String yMessage = "Y size=" + ySet.size() + ", ";
//					if(!xSet.isEmpty()){
//						xMessage += "X range: ";
//						xMessage += Collections.min(xSet) + " - " + Collections.max(xSet);
//					}
//					if(!ySet.isEmpty()){
//						yMessage += "Y range: ";
//						yMessage += Collections.min(ySet) + " - " + Collections.max(ySet);
//					}
					
					
					Rectangle range = xyRangeMap.get(tc);
					
					int maxX = range.x+range.width;
					int maxY = range.y+range.height;

					String xMessage = range != null?
							"X range: " + range.x +  " - " + maxX:
							"empty";
					
					String yMessage = range != null?
									"Y range: " + range.y +  " - " + maxY:
									"empty";
					
					DefaultMutableTreeNode xSummaryNode = new DefaultMutableTreeNode(xMessage);
					tileNode.add(xSummaryNode);
					DefaultMutableTreeNode ySummaryNode = new DefaultMutableTreeNode(yMessage);						
					tileNode.add(ySummaryNode);
					
				}
			}
		}
		return root;
	}
		
	@Override
	public String name() {
		return "Tiles tree";
	}

	@Override
	public String description() {
		return "Display the FlowCell-Lane-Tile tree with the Sequence Reads Stats";
	}

	@Override
	public void reset() {
		lowestChar = 126;
		phredEncoding = null;
		tileNumeration = null;
		
		flowCellSet.clear();
		laneIDsMap.clear();		
		tileIDsMap.clear();		
		xyRangeMap.clear();

	}
	
	@Override
	public boolean isProcessed() {
		return tileIDsMap.size() > 0;
	}

	@Override
	public boolean raisesError() {
		return false;
	}

	@Override
	public boolean raisesWarning() {
		return false;
	}

	@Override
	public boolean ignoreFilteredSequences() {
		return false;
	}

	@Override
	public void makeReport(HTMLReportArchive report) throws IOException {
		JTree tileTree = new JTree(createTileTree());
		String list = TreeUtils.listTree((TreeNode) tileTree.getModel().getRoot(), "", "<br/>");
		StringBuffer sb = report.htmlDocument();
		sb.append(list);
	}

	public Rectangle getRange(TileCoordinates tileCoordinates) {
		return xyRangeMap.get(tileCoordinates);
	}
	
	public PhredEncoding getPhredEncoding() {
		if (phredEncoding == null)
			phredEncoding = PhredEncoding.getFastQEncodingOffset(lowestChar); 
		return phredEncoding;
	}

	public SortedSet<Integer> getTiles(LaneCoordinates laneCoordinates) {
		return tileIDsMap.get(laneCoordinates);
	}

	public SortedSet<String> getFlowCells() {
		return flowCellSet;
	}
	
	public SortedSet<Integer> getLanes(String flowCell) {
		return laneIDsMap.get(flowCell);
	}
	
	public TileNumeration getTileNumeration() {
		if (tileNumeration == null)
			tileNumeration = resolveTileNumeration(); 
		return tileNumeration;
	}

	private TileNumeration resolveTileNumeration() {
		

		TileNumeration result = DefaultTileNumeration.INSTANCE; 
		
		if (!tileIDsMap.values().isEmpty()) { 

			// get tiles of first lane first
			SortedSet<Integer> tileSet = tileIDsMap.values().iterator().next();

			if (MiSeqNumeration.INSTANCE.isCompatible(tileSet))
				result = MiSeqNumeration.INSTANCE;
			else if (HiSeqRapidRunNumeration.INSTANCE.isCompatible(tileSet))
				result = HiSeqRapidRunNumeration.INSTANCE;
			else if (HiSeqNumeration.INSTANCE.isCompatible(tileSet))
				result = HiSeqNumeration.INSTANCE;			
		}
		
		return result;
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
		
		int lowestCharValue = (int) lowestChar;
        obj.put(JSON_ATTR_LOWEST_CHAR, lowestCharValue);
        
        // flowCellSet
		JSONArray flowCellSetArray = new JSONArray();
		for (String flowCell : flowCellSet)
			flowCellSetArray.add(flowCell);
        obj.put(JSON_ATTR_FLOW_CELL_SET, flowCellSetArray);

        // laneIDsMap
        JSONSerializationUtils.saveMapInJSONObject(obj, laneIDsMap, "laneIDsMap");

        // tileIDsMap
        JSONSerializationUtils.saveMapInJSONObject(obj, tileIDsMap, "tileIDsMap");

        // xyRamgeMap
		JSONArray keyArray = new JSONArray();
		JSONArray valueArray = new JSONArray();
		for(Object key : xyRangeMap.keySet()) {
			keyArray.add(key);
			Rectangle rect = xyRangeMap.get(key);
			valueArray.add(JSONSerializationUtils.rect2json(rect));
		}		
		
        obj.put(JSON_ATTR_XY_RANGE_MAP_KEYS, keyArray);
        obj.put(JSON_ATTR_XY_RANGE_MAP_VALUES, valueArray);

        
		return obj;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		
		int lowestCharValue = new Integer(jsonObject.get(JSON_ATTR_LOWEST_CHAR).toString());
		lowestChar = (char) lowestCharValue; 
                
        // flowCellSet
		JSONArray flowCellSetArray = (JSONArray) jsonObject.get(JSON_ATTR_FLOW_CELL_SET);
		for (Object flowCell : flowCellSetArray)
			flowCellSet.add(flowCell.toString());

        // laneIDsMap
		JSONArray keyArray = (JSONArray) jsonObject.get(JSON_ATTR_LANE_IDS_MAP_KEYS);
		JSONArray valueArray = (JSONArray) jsonObject.get(JSON_ATTR_LANE_IDS_MAP_VALUES);
		
		for (int i = 0 ; i < keyArray.size(); i++)
		{
			String flowCellName = keyArray.get(i).toString();
			SortedSet<Integer> lanes = new TreeSet<Integer>();
			JSONArray array = (JSONArray) valueArray.get(i);
			for (Object o : array)
				lanes.add(new Integer(o.toString()));
			laneIDsMap.put(flowCellName, lanes);
			
		}			

        // tileIDsMap
		keyArray = (JSONArray) jsonObject.get(JSON_ATTR_TILE_IDS_MAP_KEYS);
		valueArray = (JSONArray) jsonObject.get(JSON_ATTR_TILE_IDS_MAP_VALUES);
		
		for (int i = 0 ; i < keyArray.size(); i++)
		{
			JSONObject jsonLaneCoordinate = (JSONObject) keyArray.get(i);
			LaneCoordinates lc = new LaneCoordinates();
			lc.fromJSONObject(jsonLaneCoordinate); 
			SortedSet<Integer> tiles = new TreeSet<Integer>();
			JSONArray array = (JSONArray) valueArray.get(i);
			for (Object o : array)
				tiles.add(new Integer(o.toString()));
			tileIDsMap.put(lc, tiles);
			
		}			

        // xyRamgeMap
		keyArray = (JSONArray) jsonObject.get(JSON_ATTR_XY_RANGE_MAP_KEYS);
		valueArray = (JSONArray) jsonObject.get(JSON_ATTR_XY_RANGE_MAP_VALUES);
		
		for (int i = 0 ; i < keyArray.size(); i++)
		{
			JSONObject jsonTileCoordinate = (JSONObject) keyArray.get(i);
			TileCoordinates tc = new TileCoordinates();
			tc.fromJSONObject(jsonTileCoordinate); 
			Rectangle rect = JSONSerializationUtils.json2rect((JSONArray) valueArray.get(i));
			xyRangeMap.put(tc, rect);			
		}			
	}

	@Override
	public void toJSONFile(File file) throws IOException {		
		JSONSerializationUtils.writeJSONFile(file, this);
	}

	@Override
	public void fromJSONFile(File file) throws IOException, ParseException {
		JSONSerializationUtils.fromJSONFile(file, this);		
	}
	
	// customized JSON Serialization

}



