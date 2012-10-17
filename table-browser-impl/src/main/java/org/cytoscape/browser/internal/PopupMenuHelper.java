/*
 Copyright (c) 2009, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.browser.internal;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.TableCellTaskFactory;
import org.cytoscape.task.TableColumnTaskFactory;
import org.cytoscape.util.swing.GravityTracker;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.util.swing.PopupMenuGravityTracker;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;

/**
 * A class that encapsulates the creation of JPopupMenus based on TaskFactory
 * services.
 */
public class PopupMenuHelper {

	private final TaskManager<?, ?> taskManager;
	private final Map<TableCellTaskFactory, Map> tableCellFactoryMap;
	private final Map<TableColumnTaskFactory, Map> tableColumnFactoryMap;
	private final StaticTaskFactoryProvisioner factoryProvisioner;
	
	private final OpenBrowser openBrowser;

	public PopupMenuHelper(final TaskManager<?, ?> taskManager, final OpenBrowser openBrowser) {
		this.taskManager = taskManager;
		this.openBrowser = openBrowser;
		
		tableCellFactoryMap = new HashMap<TableCellTaskFactory, Map>();
		tableColumnFactoryMap = new HashMap<TableColumnTaskFactory, Map>();
		factoryProvisioner = new StaticTaskFactoryProvisioner();
	}

	public void createColumnHeaderMenu(final CyColumn column, final Class<? extends CyIdentifiable> tableType, final Component invoker, final int x, final int y) {
		if (tableColumnFactoryMap.isEmpty())
			return;

		final JPopupMenu menu = new JPopupMenu();
		final PopupMenuGravityTracker tracker = new PopupMenuGravityTracker(menu);

		for (final Map.Entry<TableColumnTaskFactory, Map> mapEntry : tableColumnFactoryMap.entrySet()) {
			TableColumnTaskFactory taskFactory = mapEntry.getKey();
			TaskFactory provisioner = factoryProvisioner.createFor(taskFactory, column);
			createMenuItem(provisioner, tracker, mapEntry.getValue(), tableType);
		}
		if(menu.getSubElements().length > 0)
			menu.show(invoker, x, y);
	}

	public void createTableCellMenu(final CyColumn column, final Object primaryKeyValue, final Class<? extends CyIdentifiable> tableType, final Component invoker,
			final int x, final int y) {
		if (tableCellFactoryMap.isEmpty())
			return;

		final JPopupMenu menu = new JPopupMenu();
		
		// Add preset menue items
		final Object value = column.getTable().getRow(primaryKeyValue).get(column.getName(), column.getType());
		if (value != null)
			menu.add(getOpenLinkMenu(value.toString()));
		
		final PopupMenuGravityTracker tracker = new PopupMenuGravityTracker(menu);

		for (final Map.Entry<TableCellTaskFactory, Map> mapEntry : tableCellFactoryMap.entrySet()) {
			TableCellTaskFactory taskFactory = mapEntry.getKey();
			TaskFactory provisioner = factoryProvisioner.createFor(taskFactory, column, primaryKeyValue);
			createMenuItem(provisioner, tracker, mapEntry.getValue(), tableType);
		}
		if(menu.getSubElements().length > 0)
			menu.show(invoker, x, y);
	}

	/**
	 * This method creates popup menu submenus and menu items based on the
	 * "title" and "preferredMenu" keywords, depending on which are present in
	 * the service properties.
	 */
	private void createMenuItem(final TaskFactory tf, final PopupMenuGravityTracker tracker, final Map props, final Class<? extends CyIdentifiable> tableType) {
		if (!enabledFor(tableType, props))
			return;
		
		String menuLabel = (String) (props.get("title"));
		if (menuLabel == null)
			menuLabel = "Unidentified Task: " + Integer.toString(tf.hashCode());

		if (tf.isReady())
			tracker.addMenuItem(new JMenuItem(new PopupAction(tf, menuLabel)), GravityTracker.USE_ALPHABETIC_ORDER);
	}

	private boolean enabledFor(Class<? extends CyIdentifiable> tableType, Map props) {
		String types = (String) props.get("tableTypes");
		if (types == null) {
			return true;
		}
		
		for (String type : types.split(",")) {
			type = type.trim();
			if ("all".equals(type)) {
				return true;
			}
			if ("node".equals(type) && CyNode.class.equals(tableType)) {
				return true;
			}
			if ("edge".equals(type) && CyEdge.class.equals(tableType)) {
				return true;
			}
			if ("network".equals(type) && CyNetwork.class.equals(tableType)) {
				return true;
			}
			if ("unassigned".equals(type) && tableType == null) {
				return true;
			}
		}
		return false;
	}

	public void addTableColumnTaskFactory(final TableColumnTaskFactory newFactory, final Map properties) {
		tableColumnFactoryMap.put(newFactory, properties);
	}

	public void removeTableColumnTaskFactory(final TableColumnTaskFactory factory, final Map properties) {
		tableColumnFactoryMap.remove(factory);
	}

	public void addTableCellTaskFactory(final TableCellTaskFactory newFactory, final Map properties) {
		tableCellFactoryMap.put(newFactory, properties);
	}

	public void removeTableCellTaskFactory(final TableCellTaskFactory factory, final Map properties) {
		tableCellFactoryMap.remove(factory);
	}

	/**
	 * A simple action that executes the specified TaskFactory
	 */
	private class PopupAction extends AbstractAction {
		
		private static final long serialVersionUID = -2841342029789163004L;
		
		private final TaskFactory tf;

		PopupAction(final TaskFactory tf, final String menuLabel) {
			super(menuLabel);
			this.tf = tf;
		}

		public void actionPerformed(ActionEvent ae) {
			taskManager.execute(tf.createTaskIterator());
		}
	}
	
	// Preset menu item: open browser
	protected JMenuItem getOpenLinkMenu(final Object urlString) {
		final JMenuItem openLinkItem = new JMenuItem();
		openLinkItem.setText("Open URL in web browser...");
		if (urlString == null || urlString.toString().startsWith("http:") == false) {
			openLinkItem.setEnabled(false);
		} else {
			openLinkItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					openBrowser.openURL(urlString.toString());
				}
			});
		}

		return openLinkItem;
	}
}
