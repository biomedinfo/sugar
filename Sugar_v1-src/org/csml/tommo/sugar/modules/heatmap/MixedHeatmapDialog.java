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
package org.csml.tommo.sugar.modules.heatmap;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.csml.tommo.sugar.analysis.TileNumeration;
import org.csml.tommo.sugar.heatmap.IMixOperation;
import org.csml.tommo.sugar.heatmap.MeanQualityMatrix;
import org.csml.tommo.sugar.heatmap.MeanQualityMatrixChart;
import org.csml.tommo.sugar.heatmap.MixOperation;
import org.csml.tommo.sugar.heatmap.MixOperationComboBox;
import org.csml.tommo.sugar.modules.QualityHeatMapsPerTileAndBase;
import org.csml.tommo.sugar.sequence.TileBPCoordinates;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

public class MixedHeatmapDialog extends InteractiveHeatmapDialog{
		
	private static final long serialVersionUID = 1L;

	protected List<MeanQualityMatrix> matrixTopList;
	protected List<MeanQualityMatrix> matrixBottomList;
	protected List<MeanQualityMatrix> matrixMixedList;

	protected List<ChartPanel> mixedChartPanelList;

	protected IMixOperation mixOperation;

	public MixedHeatmapDialog(int row, int col, MixedResultsTable table) {
		super(row, col, table);
		
		add(createMixParameterSlider(), BorderLayout.WEST);
		MixOperationComboBox comboBox = createMixOperationComboBox();
		comboBox.setSelectedItem(MixOperation.fromMixer(mixOperation));
		add(comboBox, BorderLayout.NORTH);
	}
	
	protected void initData(int row, int col, ResultsTable table) {
		super.initData(row, col, table);
		
		MixedResultsTable mixedResultsTable = (MixedResultsTable) table;
		this.mixOperation = mixedResultsTable.getModel().getMixOperation();
		
		matrixTopList = new ArrayList<MeanQualityMatrix>();
		matrixBottomList = new ArrayList<MeanQualityMatrix>();
		matrixMixedList = new ArrayList<MeanQualityMatrix>();

		mixedChartPanelList = new ArrayList<ChartPanel>();

	}


	private JSlider createMixParameterSlider() {

		JSlider slider = new JSlider(JSlider.VERTICAL, 0, 100, (int) (mixOperation.getParameter()*100.0));
		slider.setMajorTickSpacing(10);
		slider.setSnapToTicks(true);
		slider.setPaintTicks(true);
		//			slider.setPaintLabels(true);

		slider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent event) {

				JSlider source = (JSlider)event.getSource();
				if (!source.getValueIsAdjusting()) {
					double param = (double)source.getValue() / (double) 100.0;
					updateMixParameter(param);
				}
			}
		});

		return slider;
	}

	public MixOperationComboBox createMixOperationComboBox(){
		MixOperationComboBox comboBox = new MixOperationComboBox();
		comboBox.addItemListener(new ItemListener() {				
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED)
				{
					MixOperationComboBox box = (MixOperationComboBox) e.getSource();
					double param = mixOperation.getParameter();
					IMixOperation newMixOperation = box.getSelectedItem().getMixer();
					newMixOperation.setParameter(param);
					updateMixOperation(newMixOperation);
				}
			}
		});
		return comboBox;
	}

	public void updateMixOperation(IMixOperation mixOperation){
		this.mixOperation = mixOperation;
		updateMatrixes();
	}

	public void updateMixParameter(double param)
	{
		mixOperation.setParameter(param);
		updateMatrixes();
	}

	protected void updateMatrixes() {
		for(int i=0; i<matrixMixedList.size(); i++) {
			if (matrixMixedList.get(i) != null)
			{
				matrixMixedList.get(i).mix(matrixTopList.get(i), matrixBottomList.get(i), mixOperation);
				//					ChartPanel chartPanel = MeanQualityMatrixChart.createChartPanel(matrixMixedList.get(i), mixOperation.getPaintScale());

				JFreeChart chart = mixedChartPanelList.get(i).getChart();
				if(chart instanceof MeanQualityMatrixChart){
					MeanQualityMatrixChart matrixChart = (MeanQualityMatrixChart) chart;
					matrixChart.setPaintScale(mixOperation.getPaintScale());
					matrixChart.setPaintScaleLegend(mixOperation.getPaintScale());
				}
				mixedChartPanelList.get(i).getChart().fireChartChanged();
			}
		}
	}
	
	@Override
	protected void rebuild(){
		centerPanel.removeAll();			
		matrixTopList.clear();			
		matrixBottomList.clear();
		matrixMixedList.clear();			
		mixedChartPanelList.clear();
		
		List<TileBPCoordinates> tileCoordinateList = table.getTileBPCoordinateList(row, col);
		TileNumeration tileNumeration = table.getModel().getTileNumeration();
		TileBPCoordinates first = tileCoordinateList.get(0);
		String windowTitle = "Mixed Mean Quality for tile=" + first.getTile() + " , bp=" + first.getBasePosition();
		setTitle(windowTitle);

		List<TileBPCoordinates> tileCoordinatesTopList = new ArrayList<TileBPCoordinates>();
		List<TileBPCoordinates> tileCoordinatesBottomList = new ArrayList<TileBPCoordinates>();

		for(int i=0; i<tileCoordinateList.size(); i++){
			TileBPCoordinates tc = tileCoordinateList.get(i);
			Integer tile = tc.getTile();
			Integer bp = tc.getBasePosition();

			TileBPCoordinates tileCoordinatesTop = new TileBPCoordinates(tc, tile, bp);
			TileBPCoordinates tileCoordinatesBottom = new TileBPCoordinates(tc, tileNumeration.getBottomTile(tile), bp);

			tileCoordinatesTopList.add(tileCoordinatesTop);
			tileCoordinatesBottomList.add(tileCoordinatesBottom);
		}
		
		for(int i=0; i<tileCoordinateList.size(); i++){
			QualityHeatMapsPerTileAndBase qualityHeatmap = table.getModel().getQualityHeatMapsPerTileAndBase();
			MeanQualityMatrix matrixTop = qualityHeatmap.getMeanQualityMatrix(tileCoordinatesTopList.get(i)); 
			MeanQualityMatrix matrixBottom = qualityHeatmap.getMeanQualityMatrix(tileCoordinatesBottomList.get(i));
			matrixTopList.add(matrixTop);
			matrixBottomList.add(matrixBottom);
			MeanQualityMatrix mixedMatrix = MeanQualityMatrix.createMixedMatrix(matrixTop, matrixBottom, mixOperation);
			matrixMixedList.add(mixedMatrix);
			if (mixedMatrix != null) {
				ChartPanel chartPanel = createChartPanel(mixedMatrix);
				mixedChartPanelList.add(chartPanel);
				centerPanel.add(chartPanel);
			}
			else{
				centerPanel.add(new JLabel("Missing data for tile=" + tileCoordinatesTopList.get(i).getTile() + " , bp=" + tileCoordinatesTopList.get(i).getBasePosition()));
			}
		}
		centerPanel.validate();
		centerPanel.repaint();
	}

	protected ChartPanel createChartPanel(MeanQualityMatrix mixedMatrix) {
		return MeanQualityMatrixChart.createChartPanel(mixedMatrix, mixOperation.getPaintScale());		
	}	
}