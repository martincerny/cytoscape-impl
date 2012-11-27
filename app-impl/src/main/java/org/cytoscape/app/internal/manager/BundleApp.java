package org.cytoscape.app.internal.manager;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;
import org.cytoscape.app.internal.exception.AppDisableException;
import org.cytoscape.app.internal.exception.AppInstallException;
import org.cytoscape.app.internal.exception.AppInstanceException;
import org.cytoscape.app.internal.exception.AppUninstallException;
import org.cytoscape.app.internal.manager.App.AppStatus;
import org.cytoscape.app.swing.CySwingAppAdapter;

public class BundleApp extends App {
	
	/*
	@Override
	public String getReadableStatus() {
		switch (this.getStatus()) {
		
		case DISABLED:
			if (this.getAppInstance() != null) {
				return "Disable on Restart";
			} else {
				return "Disabled";
			}
		case UNINSTALLED:
			if (this.getAppInstance() != null) {
				return "Uninstall on Restart";
			} else {
				return "Uninstalled";
			}
		case FILE_MOVED:
			return "File Moved (Needs restart to uninstall)";
		default:
			return super.getReadableStatus();
		
		}
	}
	*/
	
	@Override
	public Object createAppInstance(CySwingAppAdapter appAdapter)
			throws AppInstanceException {
		return null;
	}

	@Override
	public void install(AppManager appManager) throws AppInstallException {
		this.setStatus(AppStatus.INSTALLED);
	}

	@Override
	public void uninstall(AppManager appManager) throws AppUninstallException {
		this.setStatus(AppStatus.UNINSTALLED);
	}

	@Override
	public void disable(AppManager appManager) throws AppDisableException {
		this.setStatus(AppStatus.DISABLED);
	}

}
