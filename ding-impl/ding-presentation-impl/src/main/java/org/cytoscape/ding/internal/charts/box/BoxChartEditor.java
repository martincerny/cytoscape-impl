package org.cytoscape.ding.internal.charts.box;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.charts.AbstractChartEditor;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class BoxChartEditor extends AbstractChartEditor<BoxChart> {

	private static final long serialVersionUID = 2428987302044041051L;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public BoxChartEditor(final BoxChart chart, final CyApplicationManager appMgr, final IconManager iconMgr,
			final CyColumnIdentifierFactory colIdFactory) {
		super(chart, Number.class, true, true, true, false, false, false, true, true, appMgr, iconMgr, colIdFactory);
		
		getDomainAxisVisibleCkb().setVisible(false);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
}
