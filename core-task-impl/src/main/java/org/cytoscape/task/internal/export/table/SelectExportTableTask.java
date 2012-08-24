package org.cytoscape.task.internal.export.table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.io.write.CyTableWriterManager;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Collections;


public class SelectExportTableTask extends AbstractTask {

	@Tunable(description = "Select a table to export:")
	public  ListSingleSelection<String> selectTable;
	
	private final CyTableWriterManager writerManager;
	private final CyTableManager cyTableManagerServiceRef;
	private final CyNetworkManager cyNetworkManagerServiceRef;

	private HashMap<CyTable, CyNetwork> tableNetworkMap = new HashMap<CyTable, CyNetwork>();
	private HashMap<String, CyTable> titleTableMap = new HashMap<String, CyTable>();
	
	public SelectExportTableTask (CyTableWriterManager writerManager,CyTableManager cyTableManagerServiceRef, CyNetworkManager cyNetworkManagerServiceRef){
		this.cyTableManagerServiceRef = cyTableManagerServiceRef;
		this.writerManager = writerManager;
		this.cyNetworkManagerServiceRef = cyNetworkManagerServiceRef;

		populateNetworkTableMap();
		populateSelectTable();
	}

	private void populateSelectTable() {
		final List<String> options = new ArrayList<String>();
		
		for ( CyTable tbl : cyTableManagerServiceRef.getAllTables(false)) {
			String title = tbl.getTitle();
			options.add(title);			
			this.titleTableMap.put(title, tbl);
		}
		
		Collections.sort(options);
		selectTable =  new ListSingleSelection<String>(options);
	}
	
	
	private void populateNetworkTableMap() {
		
		for (CyNetwork net: cyNetworkManagerServiceRef.getNetworkSet()) {
			this.tableNetworkMap.put(net.getDefaultNetworkTable(), net);
			this.tableNetworkMap.put(net.getDefaultNodeTable(), net);
			this.tableNetworkMap.put(net.getDefaultEdgeTable(), net);
		}
	}
	

	@Override
	public void run(TaskMonitor tm) throws IOException {

		//Get the selected table
		final String selectedTitle = selectTable.getSelectedValue();		
		CyTable tbl = this.titleTableMap.get(selectedTitle);

		// Export the selected table
		this.insertTasksAfterCurrentTask(new CyTableWriter(writerManager, tbl));		
	}
}
