/*
  Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.session.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyTableMetadata;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionSavedEvent;
import org.cytoscape.session.events.SessionSavedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.undo.UndoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link org.cytoscape.session.CySessionManager}.
 * 
 * @author Christian Lopes
 */
public class CySessionManagerImpl implements CySessionManager, SessionSavedListener {

	private String currentFileName;
	private CySession currentSession;

	private final CyEventHelper cyEventHelper;
	private final CyApplicationManager appMgr;
	private final CyNetworkManager netMgr;
	private final CyTableManager tblMgr;
	private final CyNetworkTableManager netTblMgr;
	private final VisualMappingManager vmMgr;
	private final CyNetworkViewManager nvMgr;
	private final CyRootNetworkManager rootNetMgr;
	private final CyServiceRegistrar registrar;
	private final UndoSupport undo;

	private final Set<CyProperty<?>> sessionProperties;
	private CyProperty<Bookmarks> bookmarks;

	private static final Logger logger = LoggerFactory.getLogger(CySessionManagerImpl.class);

	public CySessionManagerImpl(final CyEventHelper cyEventHelper,
								final CyApplicationManager appMgr,
								final CyNetworkManager netMgr,
								final CyTableManager tblMgr,
								final CyNetworkTableManager netTblMgr,
								final VisualMappingManager vmMgr,
								final CyNetworkViewManager nvMgr,
								final CyRootNetworkManager rootNetMgr,
								final CyServiceRegistrar registrar,
								final UndoSupport undo) {
		this.cyEventHelper = cyEventHelper;
		this.appMgr = appMgr;
		this.netMgr = netMgr;
		this.tblMgr = tblMgr;
		this.netTblMgr = netTblMgr;
		this.vmMgr = vmMgr;
		this.nvMgr = nvMgr;
		this.rootNetMgr = rootNetMgr;
		this.registrar = registrar;
		this.sessionProperties = new HashSet<CyProperty<?>>();
		this.undo = undo;
	}

	@Override
	public CySession getCurrentSession() {
		// Apps who want to save anything to a session will have to listen for this event
		// and will then be responsible for adding files through SessionAboutToBeSavedEvent.addAppFiles(..)
		final SessionAboutToBeSavedEvent savingEvent = new SessionAboutToBeSavedEvent(this);
		cyEventHelper.fireEvent(savingEvent);

		final Set<CyNetwork> networks = getSerializableNetworks();
		final Set<CyNetworkView> netViews = nvMgr.getNetworkViewSet();

		// Visual Styles Map
		final Map<CyNetworkView, String> stylesMap = new HashMap<CyNetworkView, String>();

		if (netViews != null) {
			for (final CyNetworkView nv : netViews) {
				final VisualStyle style = vmMgr.getVisualStyle(nv);

				if (style != null)
					stylesMap.put(nv, style.getTitle());
			}
		}

		final Map<String, List<File>> appMap = savingEvent.getAppFileListMap();
		final Set<CyTableMetadata> metadata = createTablesMetadata(networks);
		final Set<VisualStyle> styles = vmMgr.getAllVisualStyles();
		final Set<CyProperty<?>> props = getAllProperties();
		
		// Build the session
		final CySession sess = new CySession.Builder().properties(props).appFileListMap(appMap)
				.tables(metadata).networks(networks).networkViews(netViews).visualStyles(styles)
				.viewVisualStyleMap(stylesMap).build();

		return sess;
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends CyIdentifiable>[] TYPES = new Class[] { CyNetwork.class, CyNode.class, CyEdge.class };
	
	private Set<CyNetwork> getSerializableNetworks() {
		final Set<CyNetwork> serializableNetworks = new HashSet<CyNetwork>();
		final Set<CyNetwork> allNetworks = netTblMgr.getNetworkSet();
		
		for (final CyNetwork net : allNetworks) {
			if (net.getSavePolicy() == SavePolicy.SESSION_FILE)
				serializableNetworks.add(net);
		}
		
		return serializableNetworks;
	}
	
	private Set<CyTableMetadata> createTablesMetadata(final Set<CyNetwork> networks) {
		final Set<CyTableMetadata> result = new HashSet<CyTableMetadata>();
		
		// Merge network/global metadata into a single set
		final Map<CyTable, CyTableMetadata> networkTables = createNetworkTablesMetadata(networks);
		result.addAll(networkTables.values());
		
		final Set<CyTable> tables = new HashSet<CyTable>(tblMgr.getAllTables(true));
		
		for (final CyTable tbl : tables) {
			if (tbl.getSavePolicy() != SavePolicy.SESSION_FILE)
				continue;
			
			final CyTableMetadata metadata = networkTables.get(tbl);
			
			if (metadata == null) // Create global table metadata
				result.add(new CyTableMetadataImpl.CyTableMetadataBuilder().setCyTable(tbl));
		}
		
		return result;
	}

	private Map<CyTable, CyTableMetadata> createNetworkTablesMetadata(final Set<CyNetwork> networks) {
		final Map<CyTable, CyTableMetadata> result = new HashMap<CyTable, CyTableMetadata>();
		final Set<CyNetwork> allNetworks = new HashSet<CyNetwork>(networks);
		
		// Make sure we got the root networks
		for (final CyNetwork net : networks) {
			final CyRootNetwork rootNet = rootNetMgr.getRootNetwork(net);
			
			if (SavePolicy.SESSION_FILE == rootNet.getSavePolicy())
				allNetworks.add(rootNet);
		}
		
		// Create the metadata object for each network table
		for (final CyNetwork network : allNetworks) {
			for (final Class<? extends CyIdentifiable> type : TYPES) {
				final Map<String, CyTable> tableMap = netTblMgr.getTables(network, type);
				
				for (final Entry<String, CyTable> entry : tableMap.entrySet()) {
					final CyTable tbl = entry.getValue();
					
					if (tbl.getSavePolicy() != SavePolicy.SESSION_FILE)
						continue;
					
					final String namespace = entry.getKey();
					final CyTableMetadata metadata = new CyTableMetadataImpl.CyTableMetadataBuilder().setCyTable(tbl)
							.setNamespace(namespace).setType(type).setNetwork(network).build();
					
					result.put(tbl, metadata);
				}
			}
		}
		
		return result ;
	}

	@Override
	public void setCurrentSession(CySession sess, final String fileName) {
		boolean emptySession = sess == null;
		
		// Always remove the current session first
		disposeCurrentSession(!emptySession);

		if (emptySession) {
			logger.debug("Creating empty session...");
			final Set<VisualStyle> styles = vmMgr.getAllVisualStyles();
			final Set<CyProperty<?>> props = getAllProperties();

			sess = new CySession.Builder().properties(props).visualStyles(styles).build();
		} else {
			logger.debug("Restoring the session...");

			// Save the selected networks first, so the selection state can be restored later.
			final List<CyNetwork> selectedNetworks = new ArrayList<CyNetwork>();
			final Set<CyNetwork> networks = sess.getNetworks();

			for (CyNetwork n : networks) {
				final Boolean selected = n.getDefaultNetworkTable().getRow(n.getSUID())
						.get(CyNetwork.SELECTED, Boolean.class);
				
				if (Boolean.TRUE.equals(selected))
					selectedNetworks.add(n);
			}
			
			restoreProperties(sess);
			restoreNetworks(sess);
			restoreNetworkViews(sess);
			restoreTables(sess);
			restoreVisualStyles(sess);
			restoreNetworkSelection(sess, selectedNetworks);
		}
		
		currentSession = sess;
		currentFileName = fileName;

		cyEventHelper.fireEvent(new SessionLoadedEvent(this, currentSession, getCurrentSessionFileName()));
	}


	/**
	 * Update current session session object when session is saved.
	 */
	@Override
	public void handleEvent(SessionSavedEvent e) {
		
		if(currentSession != e.getSavedSession())
			currentSession = e.getSavedSession();
		
		if(currentFileName != e.getSavedFileName())
			currentFileName = e.getSavedFileName();
	}
	
	@Override
	public String getCurrentSessionFileName() {
		return currentFileName;
	}

	@SuppressWarnings("unchecked")
	public void addCyProperty(final CyProperty<?> newCyProperty, final Map<String, String> properties) {
		CyProperty.SavePolicy sp = newCyProperty.getSavePolicy();

		if (sp == CyProperty.SavePolicy.SESSION_FILE || sp == CyProperty.SavePolicy.SESSION_FILE_AND_CONFIG_DIR) {
			if (Bookmarks.class.isAssignableFrom(newCyProperty.getPropertyType()))
				bookmarks = (CyProperty<Bookmarks>) newCyProperty;
			else
				sessionProperties.add(newCyProperty);
		}
	}

	public void removeCyProperty(final CyProperty<?> oldCyProperty, final Map<String, String> properties) {
		CyProperty.SavePolicy sp = oldCyProperty.getSavePolicy();

		if (sp == CyProperty.SavePolicy.SESSION_FILE || sp == CyProperty.SavePolicy.SESSION_FILE_AND_CONFIG_DIR) {
			if (Bookmarks.class.isAssignableFrom(oldCyProperty.getPropertyType()))
				bookmarks = null;
			else
				sessionProperties.remove(oldCyProperty);
		}
	}

	private Set<CyProperty<?>> getAllProperties() {
		Set<CyProperty<?>> set = new HashSet<CyProperty<?>>(sessionProperties);
		
		if (bookmarks != null)
			set.add(bookmarks);

		return set;
	}

	private void restoreProperties(CySession sess) {
		for (CyProperty<?> cyProps : sess.getProperties()) {
			final Properties serviceProps = new Properties();
			serviceProps.setProperty("cyPropertyName", cyProps.getName());
			registrar.registerAllServices(cyProps, serviceProps);
		}
	}
	
	private void restoreNetworks(CySession sess) {
		logger.debug("Restoring networks...");
		Set<CyNetwork> networks = sess.getNetworks();

		for (CyNetwork n : networks) {
			netMgr.addNetwork(n);
		}
	}
	
	private void restoreNetworkViews(CySession sess) {
		logger.debug("Restoring network views...");
		Set<CyNetworkView> netViews = sess.getNetworkViews();
		
		List<CyNetworkView> selectedViews = new ArrayList<CyNetworkView>();
		for (CyNetworkView nv : netViews) {
			CyNetwork network = nv.getModel();
			if (network.getRow(network).get(CyNetwork.SELECTED, Boolean.class)) {
				selectedViews.add(nv);
			}
		}
		
		if (netViews != null) {
			for (CyNetworkView nv : netViews) {
				if (nv != null)
					nvMgr.addNetworkView(nv);
			}
		}

		appMgr.setSelectedNetworkViews(selectedViews);
	}

	private void restoreTables(CySession sess) {
		// Register all tables, if not already registered
		for (CyTableMetadata metadata : sess.getTables()) {
			final CyTable tbl = metadata.getTable();
			
			if (tblMgr.getTable(tbl.getSUID()) == null) {
				tblMgr.addTable(tbl);
			}
		}
	}
	
	private void restoreVisualStyles(final CySession sess) {
		logger.debug("Restoring visual styles...");
		// Register visual styles 
		final Set<VisualStyle> styles = sess.getVisualStyles();
		final Map<String, VisualStyle> stylesMap = new HashMap<String, VisualStyle>();

		if (styles != null) {
			for (VisualStyle vs : styles) {
				vmMgr.addVisualStyle(vs);
				stylesMap.put(vs.getTitle(), vs);
			}
		}
		
		// Set visual styles to network views
		final Map<CyNetworkView, String> viewStyleMap = sess.getViewVisualStyleMap();
		
		if (viewStyleMap != null) {
			final VisualStyle defStyle = vmMgr.getDefaultVisualStyle();
			
			for (Entry<CyNetworkView, String> entry : viewStyleMap.entrySet()) {
				final CyNetworkView netView = entry.getKey();
				final String stName = entry.getValue();
				VisualStyle vs = stylesMap.get(stName);

				if (vs == null)
					vs = defStyle;
				
				if (vs != null) {
					vmMgr.setVisualStyle(vs, netView);
					vs.apply(netView);
					netView.updateView();
				}
			}
		}
	}
	
	private void restoreNetworkSelection(final CySession sess, final List<CyNetwork> selectedNets) {
		// If the current view/network was not set, set the first selected network as current
		if (!selectedNets.isEmpty()) {
			final CyNetwork cn = selectedNets.get(0);
			appMgr.setCurrentNetwork(cn);
			
			// Also set the current view, if there is one
			final Collection<CyNetworkView> cnViews = nvMgr.getNetworkViews(cn);
			CyNetworkView cv = cnViews.isEmpty() ? null : cnViews.iterator().next();
			appMgr.setCurrentNetworkView(cv);
		
			// The selected networks must be set after setting the current one!
			if (!selectedNets.isEmpty())
				appMgr.setSelectedNetworks(selectedNets);
		}
	}

	private void disposeCurrentSession(boolean removeVisualStyles) {
		logger.debug("Disposing current session...");
		
		// Destroy network views
		Set<CyNetworkView> netViews = nvMgr.getNetworkViewSet();

		for (CyNetworkView nv : netViews) {
			nvMgr.destroyNetworkView(nv);
		}
		
		nvMgr.reset();
		
		// Destroy networks
		final Set<CyNetwork> networks = netMgr.getNetworkSet();
		
		for (CyNetwork n : networks) {
			netMgr.destroyNetwork(n);
		}
		
		netMgr.reset();

		// Destroy styles
		if (removeVisualStyles) {
			logger.debug("Removing current visual styles...");
			VisualStyle defaultStyle = vmMgr.getDefaultVisualStyle();
			List<VisualStyle> allStyles = new ArrayList<VisualStyle>(vmMgr.getAllVisualStyles());

			for (int i = 0; i < allStyles.size(); i++) {
				VisualStyle vs = allStyles.get(i);

				if (!vs.equals(defaultStyle)) {
					vmMgr.removeVisualStyle(vs);
				}
			}
		}

		// Destroy tables
		tblMgr.reset();
		
		// Unregister session properties
		final Set<CyProperty<?>> cyPropsClone = getAllProperties();
		
		for (CyProperty<?> cyProps : cyPropsClone) {
			if (cyProps.getSavePolicy().equals(CyProperty.SavePolicy.SESSION_FILE)) {
				registrar.unregisterAllServices(cyProps);
			}
		}
		
		// Clear undo stack
		undo.reset();
		
		// Reset current table and rendering engine
		appMgr.reset();
	}
}
