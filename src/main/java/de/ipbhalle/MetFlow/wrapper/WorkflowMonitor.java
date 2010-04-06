/**
 * created by Michael Gerlich on Dec 11, 2009 - 12:25:37 PM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.MetFlow.wrapper;

import java.util.Set;

import net.sf.taverna.t2.invocation.ProcessIdentifier;
import net.sf.taverna.t2.monitor.MonitorReceiver;
import net.sf.taverna.t2.monitor.MonitorableProperty;

public class WorkflowMonitor implements MonitorReceiver {

	public WorkflowMonitor() {
		super();
	}
	
	@Override
	public void addPropertiesToNode(ProcessIdentifier arg0,
			Set<MonitorableProperty<?>> arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deregisterNode(ProcessIdentifier arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerNode(Object arg0, ProcessIdentifier arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerNode(Object arg0, ProcessIdentifier arg1,
			Set<MonitorableProperty<?>> arg2) {
		// TODO Auto-generated method stub

	}

}
