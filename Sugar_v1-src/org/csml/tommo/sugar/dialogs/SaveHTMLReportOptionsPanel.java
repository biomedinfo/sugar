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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.csml.tommo.sugar.utils.Options;

public class SaveHTMLReportOptionsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected static SaveHTMLReportOptionsPanel INSTANCE;
	
	private JTextField heatmapImageSizeField;
	
	public SaveHTMLReportOptionsPanel(){
		super(new GridBagLayout());
		Border inBorder = BorderFactory.createTitledBorder("Options");
		Border outBorder = BorderFactory.createLoweredBevelBorder();
		setBorder(BorderFactory.createCompoundBorder(outBorder, inBorder));
		
		int gridy = 0;
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;

		JLabel heatmapImageSizeLabel = new JLabel("Heatmap Image Size", JLabel.TRAILING);
		c.insets = new Insets(0, 0, 5, 15);
		c.gridx = 0;
		c.gridy = gridy;
		c.gridwidth = 1;
		add(heatmapImageSizeLabel, c);
		
		heatmapImageSizeField = createTextField(3, JTextField.RIGHT, String.valueOf(Options.getHeatmapImageSize()));
		heatmapImageSizeLabel.setLabelFor(heatmapImageSizeField);
		c.insets = new Insets(0, 0, 5, 0);
		c.gridx = 1;
		c.gridy = gridy;
		c.gridwidth = 2;
		add(heatmapImageSizeField, c);
		
		gridy++; 
		
	}
	
	public int getHeatmapImageSize(){
		int result = 0;
		if(!heatmapImageSizeField.getText().isEmpty()){
			try {
				result = Integer.parseInt(heatmapImageSizeField.getText());
			} catch (NumberFormatException e) {}
		}
		return result;
	}
	
	private JTextField createTextField(int columns, int alignment, String initialText){
		JTextField result = new JTextField(columns);
		result.setHorizontalAlignment(alignment);
		result.setText(initialText);
		return result;
	}

	public static SaveHTMLReportOptionsPanel getInstance() {
		if (INSTANCE == null)
			INSTANCE = new SaveHTMLReportOptionsPanel();
			
		return INSTANCE;
	}
}
