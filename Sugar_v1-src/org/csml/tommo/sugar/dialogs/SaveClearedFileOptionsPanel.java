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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.csml.tommo.sugar.modules.EClearLowQClustersMethod;
import org.csml.tommo.sugar.modules.ELowQClustersSelectionMethdod;
import org.csml.tommo.sugar.utils.Options;

public class SaveClearedFileOptionsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected static SaveClearedFileOptionsPanel INSTANCE;
	
	private JComboBox lowQClustersSelectionComboBox;
	private JComboBox clearLowQClustersComboBox;
	
	protected SaveClearedFileOptionsPanel(){
		super(new GridBagLayout());
		Border inBorder = BorderFactory.createTitledBorder("Options");
		Border outBorder = BorderFactory.createLoweredBevelBorder();
		setBorder(BorderFactory.createCompoundBorder(outBorder, inBorder));
		
		int gridy = 0;
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;

		JLabel lowQClustersSelectionMethodLabel = new JLabel("LowQ Clusters Selection", JLabel.TRAILING);
		c.insets = new Insets(0, 0, 5, 15);
		c.gridx = 0;
		c.gridy = gridy;
		c.gridwidth = 1;
		add(lowQClustersSelectionMethodLabel, c);

		lowQClustersSelectionComboBox = new JComboBox(ELowQClustersSelectionMethdod.values());
		
		lowQClustersSelectionComboBox.setSelectedItem(ELowQClustersSelectionMethdod.AUTO);		
		c.insets = new Insets(0, 0, 5, 0);
		c.gridx = 1;
		c.gridy = gridy;
		c.gridwidth = 2;
		add(lowQClustersSelectionComboBox, c);
		
		gridy++;
		
		JLabel clearLowQClustersLabel = new JLabel("Clear LowQ Clusters", JLabel.TRAILING);
		c.insets = new Insets(0, 0, 5, 15);
		c.gridx = 0;
		c.gridy = gridy;
		c.gridwidth = 1;
		add(clearLowQClustersLabel, c);

		clearLowQClustersComboBox = new JComboBox(EClearLowQClustersMethod.methods());
		
		EClearLowQClustersMethod method = Options.getClearLowQClustersMethod();
		if (method == EClearLowQClustersMethod.NONE)
			method = EClearLowQClustersMethod.DELETE;
		
		clearLowQClustersComboBox.setSelectedItem(method);		
		c.insets = new Insets(0, 0, 5, 0);
		c.gridx = 1;
		c.gridy = gridy;
		c.gridwidth = 2;
		add(clearLowQClustersComboBox, c);
		
		gridy++;
	}
	

	public EClearLowQClustersMethod getClearLowQClustersMethod(){
		return (EClearLowQClustersMethod) clearLowQClustersComboBox.getSelectedItem();
	}


	public ELowQClustersSelectionMethdod getLowQClusterSelectionMethod(){
		return (ELowQClustersSelectionMethdod) lowQClustersSelectionComboBox.getSelectedItem();
	}	

	public static SaveClearedFileOptionsPanel getInstance() {
		if (INSTANCE == null)
			INSTANCE = new SaveClearedFileOptionsPanel();
			
		return INSTANCE;
	}



	
}
