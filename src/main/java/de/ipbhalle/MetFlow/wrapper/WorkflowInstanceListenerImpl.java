package de.ipbhalle.MetFlow.wrapper;

import net.sf.taverna.t2.facade.WorkflowInstanceListener;
import net.sf.taverna.t2.facade.WorkflowInstanceStatus;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.ProcessIdentifier;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.NamedWorkflowEntity;

/**
 * @author Michael Gerlich
 * 
 * Implement the listener interface to handle all possible workflow states
 * and print a summary to the console.
 */
public class WorkflowInstanceListenerImpl implements WorkflowInstanceListener {

	public void resultTokenProduced(WorkflowDataToken arg0,
			String arg1) {
		// TODO Auto-generated method stub
		System.out.println("Output token produced on port '" + arg1
				+ "' : " + arg0);
	}


	public void workflowCompleted(ProcessIdentifier arg0) {
		// TODO Auto-generated method stub
		System.out.println("Workflow completed!");
	}


	public void workflowFailed(ProcessIdentifier arg0,
			InvocationContext arg1, NamedWorkflowEntity arg2,
			String arg3, Throwable arg4) {
		// TODO Auto-generated method stub
		System.out.println("Workflow failed!");
	}


	public void workflowStatusChanged(WorkflowInstanceStatus arg0,
			WorkflowInstanceStatus arg1) {
		// TODO Auto-generated method stub
		System.out.println("Workflow status changed : '" + arg0
				+ "' -> '" + arg1 + "'");
	}

}
