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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.csml.tommo.sugar.SugarApplication;

/**
 * The Class SugarTitlePanel.
 */
public class SugarTitlePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Provides a small panel which gives details of the SUGAR version
	 * and copyright.  Used in both the welcome panel and the about dialog.
	 */
	public SugarTitlePanel () {
		setLayout(new BorderLayout(5,1));

		ImageIcon logo = new ImageIcon(ClassLoader.getSystemResource("org/csml/tommo/sugar/resources/sugar_icon_100.png"));
		JPanel logoPanel = new JPanel();
		logoPanel.add(new JLabel("",logo,JLabel.CENTER));
		logoPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		add(logoPanel,BorderLayout.WEST);
		JPanel c = new JPanel();
		c.setLayout(new GridBagLayout());

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx=1;
		constraints.gridy=1;
		constraints.weightx = 1;
		constraints.weighty=1;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(3, 3, 0, 0);
		constraints.fill = GridBagConstraints.NONE;

		JLabel programName = new SmoothJLabel(SugarApplication.APP_NAME + " - Subtile-based GUI-Assisted Refiner",JLabel.LEFT);
		programName.setFont(new Font("Dialog",Font.BOLD,18));
//		programName.setForeground(new Color(200,0,0));
		c.add(programName,constraints);

		constraints.gridy++;
		JLabel programDesc = new SmoothJLabel("for High Throughput Sequencing Data",JLabel.LEFT);
		programDesc.setFont(new Font("Dialog",Font.BOLD,18));
//		programDesc.setForeground(new Color(200,0,0));
		c.add(programDesc,constraints);

		constraints.gridy++;
		constraints.insets = new Insets(15, 3, 0, 0);
		JLabel version = new SmoothJLabel("Version: "+SugarApplication.VERSION, JLabel.LEFT);
		version.setFont(new Font("Dialog",Font.BOLD,15));
//		version.setForeground(new Color(0,0,200));
		c.add(version,constraints);

		constraints.gridy++;
		constraints.insets = new Insets(3, 3, 0, 0);
		// Use a text field so they can copy this
		JTextField website = new JTextField("http://nagasakilab.csml.org");
		website.setFont(new Font("Dialog",Font.PLAIN,14));
		website.setEditable(false);
		website.setBorder(null);
		website.setOpaque(false);
		website.setHorizontalAlignment(JTextField.LEFT);
		c.add(website,constraints);
		
		constraints.gridy++;
		constraints.insets = new Insets(15, 3, 0, 0);
		JLabel copyright = new JLabel("\u00a9 Masao Nagasaki, Tohoku Medical Megabank Organization, 2013", JLabel.LEFT);
		copyright.setFont(new Font("Dialog",Font.PLAIN,14));
		c.add(copyright,constraints);
		
		constraints.gridy++;
		constraints.insets = new Insets(3, 3, 0, 0);
		JLabel copyright1 = new JLabel("\u00a9 FastQC Simon Andrews, Babraham Bioinformatics, 2011", JLabel.LEFT);
		copyright1.setFont(new Font("Dialog",Font.PLAIN,14));
		c.add(copyright1,constraints);
		
		constraints.gridy++;		
		JLabel copyright2 = new JLabel("Picard BAM/SAM reader \u00a9The Broad Institute, 2009", JLabel.LEFT);
		copyright2.setFont(new Font("Dialog",Font.PLAIN,10));
		c.add(copyright2,constraints);
		
		constraints.gridy++;
		JLabel copyright3 = new JLabel("BZip decompression \u00a9Matthew J. Francis, 2011", JLabel.LEFT);
		copyright3.setFont(new Font("Dialog",Font.PLAIN,10));
		c.add(copyright3,constraints);
		
		constraints.gridy++;
		JLabel copyright4 = new JLabel("jFreeChart \u00a9 http://www.jfree.org/jfreechart/", JLabel.LEFT);
		copyright4.setFont(new Font("Dialog",Font.PLAIN,10));
		c.add(copyright4,constraints);

		constraints.gridy++;
		JLabel copyright5 = new JLabel("json-simple \u00a9 http://code.google.com/p/json-simple/", JLabel.LEFT);
		copyright5.setFont(new Font("Dialog",Font.PLAIN,10));
		c.add(copyright5,constraints);

		constraints.gridy++;
		JLabel copyright6 = new JLabel("args4j \u00a9 http://args4j.kohsuke.org/", JLabel.LEFT);
		copyright6.setFont(new Font("Dialog",Font.PLAIN,10));
		c.add(copyright6,constraints);

		add(c,BorderLayout.CENTER);
	}
	
	/**
	 * A JLabel with anti-aliasing enabled.  Takes the same constructor
	 * arguments as JLabel
	 */
	private class SmoothJLabel extends JLabel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new label
		 * 
		 * @param text The text
		 * @param position The JLabel constant position for alignment
		 */
		public SmoothJLabel (String text, int position) {
			super(text,position);
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		public void paintComponent (Graphics g) {
			if (g instanceof Graphics2D) {
				((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			}
			super.paintComponent(g);
		}

	}
	
}
