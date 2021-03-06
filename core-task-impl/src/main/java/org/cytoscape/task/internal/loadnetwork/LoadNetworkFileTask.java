package org.cytoscape.task.internal.loadnetwork;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.io.File;
import java.util.Properties;

import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

/**
 * Specific instance of AbstractLoadNetworkTask that loads a File.
 */
public class LoadNetworkFileTask extends AbstractLoadNetworkTask {
	
	@Tunable(description = "Network file to load", params = "fileCategory=network;input=true")
	public File file;

	@ProvidesTitle
	public String getTitle() {
		return "Load Network from File";
	}
	
	public LoadNetworkFileTask(
			final CyNetworkReaderManager mgr,
			final CyNetworkManager netmgr,
			final CyNetworkViewManager networkViewManager,
			final Properties props,
			final CyNetworkNaming namingUtil,
			final VisualMappingManager vmm,
			final CyNetworkViewFactory nullNetworkViewFactory,
			final CyServiceRegistrar serviceRegistrar) {
		super(mgr, netmgr, networkViewManager, props, namingUtil, vmm, nullNetworkViewFactory, serviceRegistrar);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		
		if (file == null)
			throw new NullPointerException("No file specified.");

		reader = mgr.getReader(file.toURI(), file.getName());

		if (cancelled)
			return;

		if (reader == null)
			throw new NullPointerException("Failed to find appropriate reader for file: " + file);

		uri = file.toURI();
		name = file.getName();

		loadNetwork(reader);
	}
}
