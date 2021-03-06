package org.cytoscape.task.internal.export.network;

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

import org.apache.commons.io.FilenameUtils;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyNetworkViewWriterManager;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.task.internal.export.TunableAbstractCyWriter;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Tunable;


/**
 * A utility Task implementation specifically for writing a {@link org.cytoscape.view.model.CyNetworkView}.
 */
public final class CyNetworkViewWriter extends TunableAbstractCyWriter<CyNetworkViewWriterFactory,CyNetworkViewWriterManager> {
	// the view to be written
	private final CyNetworkView view;

	/**
	 * @param writerManager The {@link org.cytoscape.io.write.CyNetworkViewWriterManager} used to determine which 
	 * {@link org.cytoscape.io.write.CyNetworkViewWriterFactory} to use to write the file.
	 * @param view The {@link org.cytoscape.view.model.CyNetworkView} to be written out. 
	 */
	public CyNetworkViewWriter(final CyNetworkViewWriterManager writerManager, final CyNetworkView view ) {
		super(writerManager);
		
		if (view == null)
			throw new NullPointerException("View is null.");
		
		this.view = view;
		// Pick XGMML as a default file format
		for(String fileTypeDesc: this.getFileFilterDescriptions()) {
			if(fileTypeDesc.contains("XGMML")) {
				this.options.setSelectedValue(fileTypeDesc);
				break;
			}
		}
	}

	void setDefaultFileFormatUsingFileExt(File file) {
		String ext = FilenameUtils.getExtension(file.getName());
		ext = ext.toLowerCase().trim();
		String searchDesc = "*." + ext;
		//Use the EXT to determine the default file format
		for(String fileTypeDesc: this.getFileFilterDescriptions() )
			if(fileTypeDesc.contains(searchDesc) )
			{
				options.setSelectedValue(fileTypeDesc);
				break;
			}
	}


	/**
	 * {@inheritDoc}  
	 */
	@Override
	protected CyWriter getWriter(CyFileFilter filter, File file)  throws Exception{
		if (!fileExtensionIsOk(file))
			file = addOrReplaceExtension(outputFile);
		else
			file = new File(file.getAbsolutePath());

		return writerManager.getWriter(view,filter,file);
	}
	
	@Tunable(description="Save Network (and View) as:", params="fileCategory=network;input=false", dependsOn="options!=")
	public  File getOutputFile() {	
		return outputFile;
	}
	
	@ProvidesTitle
	public String getTitle() {
		return "Export Network";
	}
	

	
	
}
