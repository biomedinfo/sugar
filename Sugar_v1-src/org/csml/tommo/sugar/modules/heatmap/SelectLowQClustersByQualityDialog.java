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
/**
 * 
 */
package org.csml.tommo.sugar.modules.heatmap;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.csml.tommo.sugar.modules.SubtileQualityRanking;
import org.csml.tommo.sugar.sequence.SubtileProperties;
import org.csml.tommo.sugar.utils.StringUtils;

/**
 * @author ltrzeciakowski
 *
 */
public class SelectLowQClustersByQualityDialog extends JDialog implements ChangeListener, ActionListener, ItemListener{

	private static final String DEFAULT_TITLE = "No Filter";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -679557902800314701L;

	private SubtileQualityRankingPanel panel;
	private JSlider slider;
	protected JButton okButton;
	protected JButton cancelButton;

	public SelectLowQClustersByQualityDialog(JDialog parent, SubtileQualityRanking subtileQualityRanking){
		super(parent, DEFAULT_TITLE, true);
		panel = new SubtileQualityRankingPanel(subtileQualityRanking);
		panel.addPropertyTypeListener(this);
		
		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);

		// south panel
		slider = new JSlider(SwingConstants.HORIZONTAL, 0, subtileQualityRanking.getSubtilesCount() - 1, 0);
		slider.setPaintTicks(true);
		slider.addChangeListener(this);
		
		JPanel southPanel = new JPanel(new BorderLayout());	
		southPanel.add(slider, BorderLayout.CENTER);
		southPanel.add(createButtonsPanel(), BorderLayout.SOUTH);
		
		add(southPanel, BorderLayout.SOUTH);
		setLocation(400, 350);
//		setSize(250, 250);
		pack();
	}

	
	@Override
	public void stateChanged(ChangeEvent e) {
		ESubtileProperty property = panel.getSelectedProperty();
		int sliderValue = slider.getValue();
				
		panel.setMarkers(sliderValue);
		updateTitle(sliderValue);
	}


	protected void updateTitle(int sliderValue) {
		if (sliderValue > 0) {
			String title = " # sub-tile <= " + StringUtils.FORMATTER.format(sliderValue);			
			setTitle(title);			
		}
		else{
			setTitle(DEFAULT_TITLE);
		}
	}
	
	protected JPanel createButtonsPanel() {
		
		JPanel okPanel = new JPanel();
		okPanel.setLayout(new BoxLayout(okPanel, BoxLayout.X_AXIS));
		
		Dimension buttonSize = new Dimension(80, ClearLowQClustersDialog.BUTTONS_HEIGHT);
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		okButton.setPreferredSize(buttonSize);
		cancelButton.setPreferredSize(buttonSize);

		okPanel.add(Box.createHorizontalGlue());		
		okPanel.add(okButton);
		okPanel.add(cancelButton);
		
		return okPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == okButton){
			selectByFilter();
			setVisible(false);
		}
		else if(e.getSource() == cancelButton){
			setVisible(false);
		}
	}

	protected void selectByFilter() {
		ESubtileProperty property = panel.getSelectedProperty();
		if(ESubtileProperty.MAPPING_QUALITY.equals(property) && !panel.isMappingQualityActive()){
			return;
		}
		SubtilesMap subtilesMap = panel.getSubtilesMap();
		List<SubtileProperties> list = subtilesMap.getSortedValuesByProperty(property);
		for(int i = 0; i < list.size(); i++) {
			if (i < slider.getValue())
				panel.selectSubtile(list.get(i).getCoordinates());
		}
		panel.expandTileSelection();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		ESubtileProperty property = panel.getSelectedProperty();
		if(property != null){
//			minValue = panel.getSubtilesMap().getMinPropertyValue(property);
//			maxValue = panel.getSubtilesMap().getMaxPropertyValue(property);
//			
//			int value = property.hasReversedOrder() ? slider.getMaximum() : slider.getMinimum();
			
//			slider.setValue(value);			
		}
	}
}
