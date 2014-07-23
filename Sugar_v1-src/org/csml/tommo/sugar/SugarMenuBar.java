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
package org.csml.tommo.sugar;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.csml.tommo.sugar.dialogs.SugarAboutDialog;
import org.csml.tommo.sugar.menu.TileSizeMenu;

import uk.ac.babraham.FastQC.Help.HelpDialog;

public class SugarMenuBar extends JMenuBar implements ActionListener {

	public static final String SAVE_CLEARED_SEQUENCE_FILE = "Save Cleared Sequence File";
	private static final String MENU_SAVE_CLEARED_FILE = "save_cleared_file";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SugarApplication application;
	
	public SugarMenuBar (SugarApplication application) {
		this.application = application;
		
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		JMenuItem fileOpen = new JMenuItem("Open...");
		fileOpen.setMnemonic(KeyEvent.VK_O);
		fileOpen.setAccelerator(KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileOpen.setActionCommand("open");
		fileOpen.addActionListener(this);
		fileMenu.add(fileOpen);
		
		fileMenu.addSeparator();
		
		JMenuItem fileSave = new JMenuItem("Save Report...");
		fileSave.setMnemonic(KeyEvent.VK_S);
		fileSave.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileSave.setActionCommand("save");
		fileSave.addActionListener(this);
		fileMenu.add(fileSave);
		
		JMenuItem fileSaveClearedFile = new JMenuItem(SAVE_CLEARED_SEQUENCE_FILE + "...");
		fileSaveClearedFile.setMnemonic(KeyEvent.VK_L);
//		fileSaveClearedFile.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileSaveClearedFile.setActionCommand(MENU_SAVE_CLEARED_FILE);
		fileSaveClearedFile.addActionListener(this);
		fileMenu.add(fileSaveClearedFile);
		
		fileMenu.addSeparator();
		
		JMenuItem fileClose = new JMenuItem("Close");
		fileClose.setMnemonic(KeyEvent.VK_C);
		fileClose.setAccelerator(KeyStroke.getKeyStroke('W', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileClose.setActionCommand("close");
		fileClose.addActionListener(this);
		fileMenu.add(fileClose);
		

		JMenuItem fileCloseAll = new JMenuItem("Close All");
		fileCloseAll.setMnemonic(KeyEvent.VK_A);
		fileCloseAll.setActionCommand("close_all");
		fileCloseAll.addActionListener(this);
		fileMenu.add(fileCloseAll);

		
		fileMenu.addSeparator();
		
		JMenuItem fileExit = new JMenuItem("Exit");
		fileExit.setMnemonic(KeyEvent.VK_X);
		fileExit.setActionCommand("exit");
		fileExit.addActionListener(this);
		fileMenu.add(fileExit);
		
		add(fileMenu);

		JMenu viewMenu = new JMenu("View");
		viewMenu.setMnemonic(KeyEvent.VK_V);
		
		JMenuItem tileSize = new TileSizeMenu();
		tileSize.setMnemonic(KeyEvent.VK_T);
		viewMenu.add(tileSize);
								
		add(viewMenu);

		
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		
		JMenuItem helpContents = new JMenuItem("Contents...");
		helpContents.setMnemonic(KeyEvent.VK_C);
		helpContents.setActionCommand("help_contents");
		helpContents.addActionListener(this);
		helpMenu.add(helpContents);
		
		helpMenu.addSeparator();
		
		JMenuItem helpAbout = new JMenuItem("About SUGAR");
		helpAbout.setMnemonic(KeyEvent.VK_A);
		helpAbout.setActionCommand("about");
		helpAbout.addActionListener(this);
		
		helpMenu.add(helpAbout);
		
		add(helpMenu);
		
	}

	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		
		if (command.equals("exit")) {
			System.exit(0);
		}
		else if (command.equals("open")) {
			application.openFile();
		}
		else if (command.equals("save")) {
			application.saveReport();
		}
		else if (command.equals(MENU_SAVE_CLEARED_FILE)) {
			application.saveClearedFile();
		}
		else if (command.equals("close")) {
			application.close();
		}
		else if (command.equals("close_all")) {
			application.closeAll();
		}
		else if (command.equals("view_tile_size")) {
			application.closeAll();
		}
		else if (command.equals("help_contents")) {
			new HelpDialog(application,"SUGARHelp");
		}
		else if (command.equals("about")) {
			new SugarAboutDialog(application);
		}
		else {
			JOptionPane.showMessageDialog(application, "Unknown menu command "+command, "Unknown command", JOptionPane.ERROR_MESSAGE);
		}
	}
	
}
