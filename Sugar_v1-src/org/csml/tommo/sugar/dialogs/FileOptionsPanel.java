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
package org.csml.tommo.sugar.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.csml.tommo.sugar.modules.EClearLowQClustersMethod;
import org.csml.tommo.sugar.utils.Options;

public class FileOptionsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected static FileOptionsPanel INSTANCE;
	
	private JTextField matrixSizeField;
	private JTextField heatmapQualityThresholdField;
	private JTextField threadsField;
	private JTextField readRateField;
	private JCheckBox cacheCheckBox;
	private JComboBox clearLowQClustersComboBox;
		
	public FileOptionsPanel(){
		super(new GridBagLayout());
		Border inBorder = BorderFactory.createTitledBorder("Options");
		Border outBorder = BorderFactory.createLoweredBevelBorder();
		setBorder(BorderFactory.createCompoundBorder(outBorder, inBorder));
		
		int gridy = 0;
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;

		JLabel matrixSizeLabel = new JLabel("Matrix Size", JLabel.TRAILING);
		c.insets = new Insets(0, 0, 5, 15);
		c.gridx = 0;       
		c.gridy = gridy;       
		add(matrixSizeLabel, c);
		
		matrixSizeField = createTextField(3, JTextField.RIGHT, String.valueOf(Options.getMatrixSize()));
		matrixSizeLabel.setLabelFor(matrixSizeField);
		c.insets = new Insets(0, 0, 5, 0);
		c.gridx = 1;
		c.gridy = gridy;       
		c.gridwidth = 2;
		add(matrixSizeField, c);

		gridy++; 

		JLabel qualityThresholdLabel = new JLabel("Heatmap Quality Threshold", JLabel.TRAILING);
		c.insets = new Insets(0, 0, 5, 15);
		c.gridx = 0;       
		c.gridy = gridy;
		c.gridwidth = 1;
		add(qualityThresholdLabel, c);
		
		heatmapQualityThresholdField = createTextField(3, JTextField.RIGHT, String.valueOf(Options.getHeatmapQualityThreshold()));
		qualityThresholdLabel.setLabelFor(heatmapQualityThresholdField);
		c.insets = new Insets(0, 0, 5, 0);
		c.gridx = 1;
		c.gridy = gridy;       
		c.gridwidth = 2;
		add(heatmapQualityThresholdField, c);

		gridy++; 
		
		JLabel threadsLabel = new JLabel("Threads", JLabel.TRAILING);
		c.insets = new Insets(0, 0, 5, 15);
		c.gridx = 0;
		c.gridy = gridy;
		c.gridwidth = 1;
		add(threadsLabel, c);
		
		threadsField = createTextField(3, JTextField.RIGHT,String.valueOf(Options.getThreads()));
		threadsLabel.setLabelFor(threadsField);
		c.insets = new Insets(0, 0, 5, 0);
		c.gridx = 1;
		c.gridy = gridy;
		c.gridwidth = 2;
		add(threadsField, c);
		
		gridy++;
		
		JLabel readRateLabel = new JLabel("Read Interval", JLabel.TRAILING);
		c.insets = new Insets(0, 0, 5, 15);
		c.gridx = 0;
		c.gridy = gridy;
		c.gridwidth = 1;
		add(readRateLabel, c);
		
		readRateField = createTextField(3, JTextField.RIGHT,String.valueOf(Options.getReadRate()));
		readRateLabel.setLabelFor(readRateField);
		c.insets = new Insets(0, 0, 5, 0);
		c.gridx = 1;
		c.gridy = gridy;
		c.gridwidth = 2;
		add(readRateField, c);
		
		gridy++;
		
		cacheCheckBox = new JCheckBox("Use Cache");
		cacheCheckBox.setSelected(!Options.getNoCache());		
		c.insets = new Insets(0, 0, 5, 0);
		c.gridx = 0;
		c.gridy = gridy;
		c.gridwidth = 2;
		add(cacheCheckBox, c);

		gridy++;

		JLabel clearLowQClustersLabel = new JLabel("Clear LowQ Clusters", JLabel.TRAILING);
		c.insets = new Insets(0, 0, 5, 15);
		c.gridx = 0;
		c.gridy = gridy;
		c.gridwidth = 1;
		add(clearLowQClustersLabel, c);
		
		clearLowQClustersComboBox = new JComboBox(EClearLowQClustersMethod.values());
		clearLowQClustersComboBox.setSelectedItem(Options.getClearLowQClustersMethod());
		clearLowQClustersLabel.setLabelFor(clearLowQClustersComboBox);
		c.insets = new Insets(0, 0, 5, 0);
		c.gridx = 1;
		c.gridy = gridy;
		c.gridwidth = 2;
		add(clearLowQClustersComboBox, c);
	}
	
	public int getMatrixSize(){
		int result = 0;
		if(!matrixSizeField.getText().isEmpty()){
			try {
				result = Integer.parseInt(matrixSizeField.getText());
			} catch (NumberFormatException e) {}
		}
		return result;
	}
	
	public Integer[] getHeatmapQualityThresholdArray() {
		String thresholdString = heatmapQualityThresholdField.getText();		
		return Options.getHeatmapQualityThresholdArray(thresholdString);		
	}
	
	public int getThreads(){
		int result = 0;
		if(!threadsField.getText().isEmpty()){
			try {
				result = Integer.parseInt(threadsField.getText());
			} catch (NumberFormatException e) {}
		}
		return result;
	}

	public int getReadRate(){
		int result = 0;
		if(!readRateField.getText().isEmpty()){
			try {
				result = Integer.parseInt(readRateField.getText());
			} catch (NumberFormatException e) {}
		}
		return result;
	}
	
	public boolean getNoCache(){
		return !cacheCheckBox.isSelected();
	}

	public EClearLowQClustersMethod getClearLowQClustersMethod(){
		return (EClearLowQClustersMethod) clearLowQClustersComboBox.getSelectedItem();
	}
	
	private JTextField createTextField(int columns, int alignment, String initialText){
		JTextField result = new JTextField(columns);
		result.setHorizontalAlignment(alignment);
		result.setText(initialText);
		return result;
	}

	public static FileOptionsPanel getInstance() {
		if (INSTANCE == null)
			INSTANCE = new FileOptionsPanel();
			
		return INSTANCE;
	}
	
	public String validateInput() {
		String errorMessage = null;

		String thresholdString = heatmapQualityThresholdField.getText();		
		if (getMatrixSize() < 1)
		{
			errorMessage = "Invalid matrix size. Matrix size must be an integer number greater than zero";
		}
		else if (!Options.validateHeatmapQualityThreshold(thresholdString))
		{
			errorMessage = "Invalid quality threshold. It must be an integer number or range in the form min-max. These values can be separated by commas.";
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
}
