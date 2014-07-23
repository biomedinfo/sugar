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
package org.csml.tommo.sugar.menu;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.csml.tommo.sugar.SugarApplication;

public class TileSizeMenu extends JMenu
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7496578894848176538L;

	public TileSizeMenu() {
		this("Tile Size");
	}

	public TileSizeMenu(String name) {
		super(name);

		ButtonGroup group = new ButtonGroup();
		for (ETileSizeItem item : ETileSizeItem.values()) {
			JMenuItem menuItem = new JCheckBoxMenuItem(item.getName());
			menuItem.addActionListener(getTargetAction());

			this.add(menuItem);
			group.add(menuItem);
		}
	}

	@Override
	public JPopupMenu getPopupMenu() {
		JPopupMenu popup = super.getPopupMenu();
		setSelectedItem(popup);
		return popup;
	}

	public void setSelectedItem(JPopupMenu popup) {
		Integer size = SugarApplication.getApplication().getTileHeatmapSize();
		String sSize = size.toString();

		for (Component c : popup.getComponents()) {
			if (c instanceof AbstractButton) {
				AbstractButton item = (AbstractButton) c;
				if (item.getText().equals(sSize)) {
					item.setSelected(true);
				}
			}
		}

	}

	protected ActionListener getTargetAction() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = ((JMenuItem) e.getSource()).getText();
				Integer size = -1;
				
				SugarApplication application = SugarApplication.getApplication();
				
				if (ETileSizeItem.CUSTOM.getName().equals(text))
				{
					String newValue = JOptionPane.showInputDialog(application, 
							"Tile Size (0-255)", application.getTileHeatmapSize());
					
					size = new Integer(newValue);
					
				}
				else 
				{
					size = new Integer(text);
				}
				
				application.setTileHeatmapSize(size);
				
				
			}
		};
	}
}
