/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.view.vizmap.gui.internal.editor.valueeditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.LayoutStyle;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.xml.soap.Text;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.DiscreteRange;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.VisualPropertyValue;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;
import org.jdesktop.swingx.border.DropShadowBorder;

/**
 * Value chooser for any discrete values. This includes
 * <ul>
 * <li>node shape</li>
 * <li>arrow shape</li>
 * <li>line style</li>
 * <li>etc.</li>
 * </ul>
 * 
 */
public class DiscreteValueEditor<T> extends JDialog implements ValueEditor<T> {
	
	private final static long serialVersionUID = 1202339876950593L;
	
	// Default icon size.
	private static final int ICON_SIZE_LARGE = 64;

	// Value data type for this chooser.
	private final Class<T> type;
	
	// Range object.  Actual values will be provided from 
	private final DiscreteRange<T> range;
	private final VisualProperty<T> vp;
	
	private final CyApplicationManager appManager;
	
	private Map<T, Icon> iconMap;

	private boolean canceled = false;

	public DiscreteValueEditor(final CyApplicationManager appManager, final Class<T> type,
			final DiscreteRange<T> dRange, final VisualProperty<T> vp) {
		super();
		
		if (dRange == null)
			throw new NullPointerException("Range object is null.");

		this.range = dRange;
		this.type = type;
		this.appManager = appManager;
		this.vp = vp;

		this.iconMap = new HashMap<T, Icon>();

		this.setModal(true);
		this.setTitle("Select a value");

		initComponents();
		setListItems();
	}
	
	/**
	 * Use current renderer to create icons.
	 * @param values
	 */
	private void renderIcons(final Set<T> values, final int iconSize) {
		final RenderingEngine<CyNetwork> engine = appManager.getCurrentRenderingEngine();
		
		// CCurrent engine is not ready yet.
		if(engine == null)
			return;
		
		iconMap.clear();
		for(T value: values)
			iconMap.put(value, engine.createIcon(vp, value, iconSize, iconSize));
	}


	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */

	// <editor-fold defaultstate="collapsed" desc=" Generated Code ">
	private void initComponents() {
		mainPanel = new org.jdesktop.swingx.JXTitledPanel();
		iconListScrollPane = new javax.swing.JScrollPane();
		iconList = new javax.swing.JList();
		applyButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Select New Value");

		mainPanel.setTitleFont(new java.awt.Font("SansSerif", 1, 14));

		iconList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		iconListScrollPane.setViewportView(iconList);

		applyButton.setText("Apply");
		applyButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				applyButtonActionPerformed(evt);
			}
		});

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});
		// Currently not implemented
		cancelButton.setVisible(true);

		GroupLayout mainPanelLayout = new GroupLayout(mainPanel
				.getContentContainer());
		mainPanel.getContentContainer().setLayout(mainPanelLayout);
		mainPanelLayout.setHorizontalGroup(mainPanelLayout.createParallelGroup(
				GroupLayout.Alignment.LEADING)
				.addGroup(
						GroupLayout.Alignment.TRAILING,
						mainPanelLayout.createSequentialGroup()
								.addContainerGap(128, Short.MAX_VALUE)
								.addComponent(cancelButton).addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(applyButton).addContainerGap())
				.addComponent(iconListScrollPane, GroupLayout.DEFAULT_SIZE,
						291, Short.MAX_VALUE));
		mainPanelLayout.setVerticalGroup(mainPanelLayout.createParallelGroup(
				GroupLayout.Alignment.LEADING).addGroup(
				GroupLayout.Alignment.TRAILING,
				mainPanelLayout.createSequentialGroup().addComponent(
						iconListScrollPane, GroupLayout.DEFAULT_SIZE, 312,
						Short.MAX_VALUE).addPreferredGap(
						LayoutStyle.ComponentPlacement.RELATED).addGroup(
						mainPanelLayout.createParallelGroup(
								GroupLayout.Alignment.BASELINE).addComponent(
								applyButton).addComponent(cancelButton))
						.addContainerGap()));

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(
				GroupLayout.Alignment.LEADING).addComponent(mainPanel,
				GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
				Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(
				GroupLayout.Alignment.LEADING).addComponent(mainPanel,
				GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
				Short.MAX_VALUE));
		pack();
	} // </editor-fold>

	private void cancelButtonActionPerformed(ActionEvent evt) {
		dispose();
		canceled = true;
	}

	private void applyButtonActionPerformed(ActionEvent evt) {
		dispose();
	}

	// Variables declaration - do not modify
	private javax.swing.JButton applyButton;
	private javax.swing.JButton cancelButton;
	private javax.swing.JList iconList;
	private javax.swing.JScrollPane iconListScrollPane;
	private org.jdesktop.swingx.JXTitledPanel mainPanel;
	private DefaultListModel model;

	
	public T getValue() {
		if (canceled == true)
			return null;
		
		return (T) iconList.getSelectedValue();
	}

	
	private void setListItems() {

		final Set<T> values = range.values();
		renderIcons(values, ICON_SIZE_LARGE);
		
		model = new DefaultListModel();
		iconList.setModel(model);


		for (final T key : values) {
			//Icon icon = iconMap.get(key);

			//icons.add(icon);
			//orderedKeyList.add(key);
			model.addElement(key);
		}

		iconList.setCellRenderer(new IconCellRenderer());
		iconList.repaint();
	}

	// TODO: optimize icon layout
	private final class IconCellRenderer extends JLabel implements ListCellRenderer {
		
		private final static long serialVersionUID = 1202339876940871L;
		
		private final Font SELECTED_FONT = new Font("SansSerif", Font.ITALIC, 18);
		private final Font NORMAL_FONT = new Font("SansSerif", Font.BOLD, 14);
		private final Color SELECTED_COLOR = new Color(30, 30, 80, 25);
		private final Color SELECTED_FONT_COLOR = new Color(0, 150, 255, 120);

		public IconCellRenderer() {
			setOpaque(true);
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			// Get icon for the target value
			
			final Icon icon = iconMap.get(value);
			setIcon(icon);
			setFont(isSelected ? SELECTED_FONT : NORMAL_FONT);

			this.setVerticalTextPosition(SwingConstants.CENTER);
			this.setVerticalAlignment(SwingConstants.CENTER);
			this.setIconTextGap(55);

			setBackground(isSelected ? SELECTED_COLOR : list.getBackground());
			setForeground(isSelected ? SELECTED_FONT_COLOR : list.getForeground());
			
			
			if(icon != null)
				setPreferredSize(new Dimension(icon.getIconWidth() + 230, icon.getIconHeight() + 24));
			else
				setPreferredSize(new Dimension(230, 60));
			
			String labelText = null;
			// Use reflection to check exixtence of "getDisplayName" method
			final Class<? extends Object> testClass = value.getClass();
			try {
				final Method displayMethod = testClass.getMethod("getDisplayName", null);
				final Object returnVal = displayMethod.invoke(value, null);
				if(returnVal != null)
					labelText = returnVal.toString();
			} catch (Exception e) {
				// Use toString is failed.
				labelText = value.toString();
			}
			
			setText(labelText);
			
			this.setBorder(new DropShadowBorder());

			return this;
		}
	}

	@Override
	public <S extends T> T showEditor(Component parent, S initialValue) {
		setListItems();
		setLocationRelativeTo(parent);
		setVisible(true);
		
		final T newValue = getValue();
		canceled = false;
		
		if(newValue == null)
			return initialValue;
		else
			return newValue;
	}

	@Override
	public Class<T> getType() {
		return type;
	}
}
