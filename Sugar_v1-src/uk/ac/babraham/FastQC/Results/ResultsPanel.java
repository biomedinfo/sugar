/**
 * Copyright Copyright 2010-12 Simon Andrews
 *
 *    This file is part of FastQC.
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
package uk.ac.babraham.FastQC.Results;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.csml.tommo.sugar.SugarApplication;
import org.csml.tommo.sugar.analysis.SugarAnalysisListener;
import org.csml.tommo.sugar.utils.StringUtils;

import uk.ac.babraham.FastQC.Modules.QCModule;
import uk.ac.babraham.FastQC.Sequence.SequenceFile;

public class ResultsPanel extends JPanel implements ListSelectionListener, SugarAnalysisListener{

	private static final ImageIcon ERROR_ICON = new ImageIcon(ClassLoader.getSystemResource("uk/ac/babraham/FastQC/Resources/error.png"));
	private static final ImageIcon WARNING_ICON = new ImageIcon(ClassLoader.getSystemResource("uk/ac/babraham/FastQC/Resources/warning.png"));
	private static final ImageIcon OK_ICON = new ImageIcon(ClassLoader.getSystemResource("uk/ac/babraham/FastQC/Resources/tick.png"));

	
	private QCModule [] modules;
	private JList moduleList;
	private JPanel [] panels;
	private JPanel currentPanel = null;
	private JLabel progressLabel;
	private JLabel timeConsumedLabel;
	private JLabel timeRemainingLabel;
	private JPanel labelsPanel;
	private SequenceFile sequenceFile;
	private boolean isLoaded;
	
	public ResultsPanel (SequenceFile sequenceFile) {
		this.sequenceFile = sequenceFile;
		isLoaded = false;
		
		setLayout(new BorderLayout());
		progressLabel = new JLabel("Waiting to start...", JLabel.CENTER);
		progressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		timeConsumedLabel = new JLabel("", JLabel.CENTER);
		timeConsumedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		timeRemainingLabel = new JLabel("", JLabel.CENTER);
		timeRemainingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		labelsPanel = new JPanel();
		labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.Y_AXIS));
		
		labelsPanel.add(Box.createVerticalGlue());
		labelsPanel.add(progressLabel);
		labelsPanel.add(timeConsumedLabel);
		labelsPanel.add(timeRemainingLabel);
		labelsPanel.add(Box.createVerticalGlue());
		add(labelsPanel, BorderLayout.CENTER);		
	}

	public void valueChanged(ListSelectionEvent e) {
		int index = moduleList.getSelectedIndex();
		if (index >= 0) {
			remove(currentPanel);
			currentPanel = panels[index]; 
			add(currentPanel,BorderLayout.CENTER);
			validate();
			repaint();
		}
	}
	
	public SequenceFile sequenceFile () {
		return sequenceFile;
	}
	
	public QCModule [] modules () {
		return modules;
	}
	
	private class ModuleRenderer extends DefaultListCellRenderer {

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			if (! (value instanceof QCModule)) {
				return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			}
			
			QCModule module = (QCModule)value;
			ImageIcon icon = OK_ICON;
			if (module.raisesError()) {
				icon = ERROR_ICON;
			}
			else if (module.raisesWarning()) {
				icon = WARNING_ICON;
			}

			JLabel returnLabel = new JLabel(module.name(),icon,JLabel.LEFT);
			returnLabel.setOpaque(true);
			if (isSelected) {
				returnLabel.setBackground(Color.LIGHT_GRAY);
			}
			else {
				returnLabel.setBackground(Color.WHITE);
			}
			
			return returnLabel;
		}
		
	}

	public void analysisComplete(SequenceFile file, QCModule[] modules) {
		remove(labelsPanel);
		
		// SPECIAL CASE: "view only" modules
		modules = SugarApplication.addViewOnlyModules(modules);		
				
		this.modules = modules;
		
		panels = new JPanel[modules.length];
		
		for (int m=0;m<modules.length;m++) {
			panels[m] = modules[m].getResultsPanel();
		}

		moduleList = new JList(modules);
		moduleList.setCellRenderer(new ModuleRenderer());
		moduleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		moduleList.setSelectedIndex(0);
		moduleList.addListSelectionListener(this);
		
		add(new JScrollPane(moduleList),BorderLayout.WEST);
		
		currentPanel = panels[0];
		add(currentPanel,BorderLayout.CENTER);
		validate();
		
		setPanelTitle(sequenceFile.name());
		isLoaded = true;
	}

	private void setPanelTitle(String title) {
		Container parent = getParent();
		if(parent instanceof JTabbedPane){
			JTabbedPane tabbedPane = (JTabbedPane) parent;
			int idx = tabbedPane.indexOfComponent(this);
			if(idx >= 0){
				tabbedPane.setTitleAt(idx, title);
			}
		}
	}

	public void analysisUpdated(SequenceFile file, int sequencesProcessed, int percentComplete) {
		if (percentComplete > 99) {
			progressLabel.setText("Read "+sequencesProcessed+" sequences");
		}
		else {
			progressLabel.setText("Read "+sequencesProcessed+" sequences ("+percentComplete+"%)");
		}
		
		// #52: Add progress information as the name of tabbed pane
		String label = "Loading file " + sequenceFile.name() + " (" + percentComplete + "%)";
		setPanelTitle(label);
	}
	
	@Override
	public void analysisTimeUpdated(long timeConsumed, long timeRemaining) {
		if (timeConsumed > 0) {
			timeConsumedLabel.setText("Elapsed time: " + StringUtils.formatTime(timeConsumed));
		}
		else{
			timeConsumedLabel.setText("");
//			this.labelsPanel.setName("");
//			this.labelsPanel.cre
		}
		if (timeRemaining > 0) {
			timeRemainingLabel.setText("Remaining time: " + StringUtils.formatTime(timeRemaining));
		}
		else{
			timeRemainingLabel.setText("");
		}		
	}

	@Override
	public void analysisExceptionReceived(SequenceFile file, Exception e) {
		progressLabel.setText("Failed to process file: "+e.getLocalizedMessage());
	}

	@Override
	public void analysisStarted(SequenceFile file) {
		progressLabel.setText("Starting analysis...");
		
		// #52: Add progress information as the name of tabbed pane
		String label = sequenceFile.name() + " - Starting...";
		setPanelTitle(label);
	}

	@Override
	public void cacheFileStarted(QCModule m, long filesize, int operation) {
		
		if (operation == SugarAnalysisListener.READING_FILE)
			progressLabel.setText("Started loading cache file for module "+ m.name() + ". The file size is " + filesize );
		else if (operation == SugarAnalysisListener.WRITING_FILE)
			progressLabel.setText("Started writing cache file for module "+ m.name());

	}

	@Override
	public void cacheFileCompleted(QCModule m, long time, int operation) {

		if (operation == SugarAnalysisListener.READING_FILE)
			progressLabel.setText("Completed loading module "+ m.name() + " from cache in " + time + " milliseconds." );
		else if (operation == SugarAnalysisListener.WRITING_FILE)
			progressLabel.setText("Completed writing module "+ m.name() + " to cache in " + time + " milliseconds." );
		
	}

	public boolean isLoaded() {
		return isLoaded;
	}
	
	
}
