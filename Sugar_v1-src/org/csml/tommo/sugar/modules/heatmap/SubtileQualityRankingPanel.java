package org.csml.tommo.sugar.modules.heatmap;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.csml.tommo.sugar.SugarApplication;
import org.csml.tommo.sugar.analysis.OpenedFileCache;
import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;
import org.csml.tommo.sugar.heatmap.SubtileQualityRankingDataset;
import org.csml.tommo.sugar.modules.QualityHeatMapsPerTileAndBase;
import org.csml.tommo.sugar.modules.SubtileQualityRanking;
import org.csml.tommo.sugar.sequence.SubtileCoordinates;
import org.csml.tommo.sugar.sequence.SubtileProperties;
import org.csml.tommo.sugar.sequence.TileCoordinates;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

public class SubtileQualityRankingPanel extends JPanel implements ItemListener, PropertyChangeListener{

	protected SubtileQualityRanking module;
	protected JComboBox qualityCombo; 

	private JComboBox propertyBox;
	private ChartPanel chartPanel;
	private IntervalMarker marker;
	private ExtendedValueMarker propertyMarker;
	private ExtendedValueMarker ratioMarker;
	
	
	private JPanel noMappingQualityPanel;
	
	public SubtileQualityRankingPanel(SubtileQualityRanking module){
		super(new BorderLayout());
		this.module = module;
		
		ESubtileProperty property = ESubtileProperty.READ_DENSITY;
		JFreeChart chart = createChart(property);
		chartPanel = new ChartPanel(chart);
		chartPanel.setDomainZoomable(true);
		
		propertyBox = new JComboBox(ESubtileProperty.values());
		propertyBox.addItemListener(this);
		
		add(chartPanel, BorderLayout.CENTER);
		add(propertyBox, BorderLayout.SOUTH);
		add(getPanelTitle(), BorderLayout.NORTH);
	}

	public JFreeChart createChart(ESubtileProperty property) {
		SubtileQualityRankingDataset dataset = new SubtileQualityRankingDataset(module.getSubtilesMap(), property);
        NumberAxis xAxis = new NumberAxis("# of subtiles sorted by the quality indicator");
        String description = property.getDescription();
        if (ESubtileProperty.RATE_OF_LowQ_READS.equals(property))
        	description += module.getQualityHeatmapsPerTileAndBase().getQualityThreshold();
        NumberAxis leftYAxis = new NumberAxis(description);
		leftYAxis.setLabelPaint(Color.RED);
		leftYAxis.setTickLabelPaint(Color.RED);
        leftYAxis.setAutoRangeIncludesZero(false);
//        XYItemRenderer renderer = new XYLineAndShapeRenderer();
        XYItemRenderer renderer = new ExtendedXYItemRenderer();
        XYPlot plot = new XYPlot(dataset, xAxis, leftYAxis, renderer);
        
        XYDataset remainingSizeDataset = createRemainingSizeDataset(module.getSubtilesMap(), property);
		NumberAxis rightYAxis = new NumberAxis("Ratio of data remaining after clearing");
		rightYAxis.setAutoRangeIncludesZero(false);
		plot.setRangeAxis(1, rightYAxis);
		plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
		plot.setDataset(1, remainingSizeDataset);
		plot.mapDatasetToRangeAxis(1, 1);
        XYItemRenderer remainingSizeRenderer = new ExtendedXYItemRenderer();
		remainingSizeRenderer.setSeriesPaint(0, Color.GREEN.darker());
		rightYAxis.setLabelPaint(Color.GREEN.darker());
		rightYAxis.setTickLabelPaint(Color.GREEN.darker());
		plot.setRenderer(1, remainingSizeRenderer);
		
        plot.addDomainMarker(getMarker(), Layer.BACKGROUND);
        plot.addRangeMarker(getPropertyMarker(), Layer.BACKGROUND);
        plot.addRangeMarker(1, getRatioMarker(), Layer.BACKGROUND);
            
        JFreeChart chart = new JFreeChart("Subtile quality ranking", plot);
        chart.getTitle().setFont(chart.getTitle().getFont().deriveFont(10f));
		return chart;
	}

	private IntervalMarker getMarker() {
		
		if (marker == null)
		{
			marker = new IntervalMarker(0, 0);
	        marker.setPaint(Color.RED);
	        marker.setAlpha(0.2f);
		}		
		return marker;			
	}
	
	private ExtendedValueMarker getPropertyMarker() {
		
		if (propertyMarker == null)
		{
			propertyMarker = new ExtendedValueMarker(0, Color.RED,
	                new BasicStroke(2.0f));
			
			propertyMarker.setLabel("Value");
			propertyMarker.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
			propertyMarker.setLabelPaint(Color.RED);
			propertyMarker.setLabelAnchor(RectangleAnchor.TOP);
			propertyMarker.setLabelTextAnchor(TextAnchor.BOTTOM_CENTER);

		}		
		return propertyMarker;			
	}
	
	private ExtendedValueMarker getRatioMarker() {
		
		if (ratioMarker == null)
		{
			ratioMarker = new ExtendedValueMarker(0, Color.GREEN,
	                new BasicStroke(2.0f));
			ratioMarker.setLabel("Ratio");
			ratioMarker.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
			ratioMarker.setLabelPaint(Color.GREEN);
			ratioMarker.setLabelAnchor(RectangleAnchor.TOP);
			ratioMarker.setLabelTextAnchor(TextAnchor.BOTTOM_CENTER);
		}		
		return ratioMarker;			
	}

	
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		ESubtileProperty property = (ESubtileProperty) propertyBox.getSelectedItem();
		if(property != null){
			refreshChart(property);
		}
	}

	protected void refreshChart(ESubtileProperty property) {
		if(ESubtileProperty.MAPPING_QUALITY.equals(property) && !module.isMappingQualityActive()){
			remove(chartPanel);
			add(getNoMappingQualityPanel(), BorderLayout.CENTER);
			revalidate();
			repaint();
		}
		else{
			if(noMappingQualityPanel != null){
				remove(noMappingQualityPanel);					
			}
			JFreeChart chart = createChart(property);
			chartPanel.setChart(chart);				
			add(chartPanel, BorderLayout.CENTER);
			
			setMarkers((int) marker.getEndValue());
		}
	}
	
	protected Component getPanelTitle() {
		
		JPanel result = new JPanel();
		
		String s = module.name();
		
		int matrixSize = module.getQualityHeatmapsPerTileAndBase().getMatrixSize();
		s += ". Matrix Size: " + matrixSize;
//		s += ", Quality Threshold: " + qualityHeatMapsPerTileAndBase.getQualityThreshold();
		s += ", Quality Threshold: " ;
		JLabel label = new JLabel(s,JLabel.CENTER);
		
		if (SugarApplication.getApplication() == null)
		{
			// CASE for CONSOLE Application
			result.add(label);
			return result;
		}
		
		Integer[] availableThresholds = OpenedFileCache.getInstance().getAvailableQualityThresholds(
				SugarApplication.getApplication().getSelectedSequenceFile(), matrixSize);

		
		qualityCombo = new JComboBox(availableThresholds);
		
		QualityHeatMapsPerTileAndBase qualityHeatMapsPerTileAndBase = module.getQualityHeatmapsPerTileAndBase();
		
		qualityCombo.setSelectedItem(qualityHeatMapsPerTileAndBase.getQualityThreshold());
		qualityCombo.addActionListener(qualityHeatMapsPerTileAndBase.createThresholdChangedListener());
		
		result.add(label);
		result.add(qualityCombo);
		
		qualityHeatMapsPerTileAndBase.addOualityThresholdPropertyChangeListener(this);
		
		return result;
	}

	public JPanel getNoMappingQualityPanel() {
		if(noMappingQualityPanel == null){
			noMappingQualityPanel = module.getMappingQuality().createEmptyPanel();
		}
		return noMappingQualityPanel;
	}
	
	public ESubtileProperty getSelectedProperty(){
		return (ESubtileProperty)propertyBox.getSelectedItem();
	}
	
	public void setMarkers(int to){
		getMarker().setStartValue(0);
		getMarker().setEndValue(to);
		
		XYPlot plot = (XYPlot) chartPanel.getChart().getPlot();
		
		getPropertyMarker().setValue(plot.getDataset(0).getYValue(0, to));
		getPropertyMarker().setEnd(to);
				
		getRatioMarker().setValue(plot.getDataset(1).getYValue(0, to));
		getRatioMarker().setStart(to);
		
	}
	
	public SubtilesMap getSubtilesMap(){
		return module.getSubtilesMap();
	}
	
	public void selectSubtile(SubtileCoordinates subtile) {
		MeanQualityMatrix matrix = module.getQualityHeatmapsPerTileAndBase().getFirstTileMatrix(subtile.getTileCoordinates());
		matrix.setSelectedEntry(subtile.getX(), subtile.getY(), true);
	}
	
	public void expandTileSelection(){
		module.getQualityHeatmapsPerTileAndBase().expandTileSelection();
	}
	
	public void addPropertyTypeListener(ItemListener l){
		propertyBox.addItemListener(l);
	}
	
	public void removePropertyTypeListener(ItemListener l){
		propertyBox.removeItemListener(l);
	}
	
	public boolean isMappingQualityActive(){
		return module.isMappingQualityActive();
	}
	
	private XYDataset createRemainingSizeDataset(SubtilesMap subtileMap, ESubtileProperty property){
		XYSeriesCollection result = new XYSeriesCollection();
		XYSeries series = new XYSeries("Remaining Size");
		List<SubtileProperties> list = subtileMap.getSortedValuesByProperty(property);

		long total = 0L;
		QualityHeatMapsPerTileAndBase qualityHeatmap = module.getQualityHeatmapsPerTileAndBase();
		Map<TileCoordinates, int[][]> densityPerTile = new HashMap<TileCoordinates, int[][]>();
		for(int i=0; i<list.size(); i++){
			SubtileProperties subtileProperties = list.get(i);
			SubtileCoordinates subtile = subtileProperties.getCoordinates();
			TileCoordinates tile = subtile.getTileCoordinates();
			List<MeanQualityMatrix> matrixList = qualityHeatmap.getMeanQualityMatrixList(tile);
			for(MeanQualityMatrix matrix: matrixList){
				if(matrix != null){
					if(!densityPerTile.containsKey(tile)){
						densityPerTile.put(tile, new int[matrix.getSize()][matrix.getSize()]);
					}
					int[][] densityArray = densityPerTile.get(tile);
					for(int x=0; x<matrix.getSize(); x++){
						for(int y=0; y<matrix.getSize(); y++){
							densityArray[x][y] += matrix.getTotalValueCounter()[x][y];
						}
					}
				}
				total += matrix.getCounter();
			}
		}
		
		int counter = 0;
		long summary = 0L;
		for(int i=0; i<list.size(); i++){
			SubtileProperties subtileProperties = list.get(i);
			SubtileCoordinates subtile = subtileProperties.getCoordinates();
			TileCoordinates tile = subtile.getTileCoordinates();
			summary += densityPerTile.get(tile)[subtile.getX()][subtile.getY()];
			
			series.add(counter, 1 - (double)summary/total);
			counter++;
		}
		
		result.addSeries(series);
		return result;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent e) {

		int newThreshold = (Integer) e.getNewValue();
		
		// update combo box
		qualityCombo.setSelectedItem(newThreshold);
		
    	// update charts
		module.initSubtilesMap();
		ESubtileProperty property = (ESubtileProperty) propertyBox.getSelectedItem();
		if (property != null){
			refreshChart(property);
		}
		
	}

}
