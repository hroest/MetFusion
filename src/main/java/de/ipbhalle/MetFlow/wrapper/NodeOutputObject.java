package de.ipbhalle.MetFlow.wrapper;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.swing.tree.DefaultMutableTreeNode;

import com.icesoft.faces.component.tree.IceUserObject;

import de.ipbhalle.MetFlow.web.controller.WorkflowOutputTreeModelBean;

public class NodeOutputObject extends IceUserObject {

	private WorkflowOutput output;
	private WorkflowOutputTreeModelBean treeModelBean;
	// displayPanel to show when a node is clicked
    private String selectedNode;
    
	public NodeOutputObject(DefaultMutableTreeNode treeNode) {
		super(treeNode);

		setLeafIcon("tree_document.gif");
        setBranchContractedIcon("tree_folder_closed.gif");
        setBranchExpandedIcon("tree_folder_open.gif");
        setExpanded(true);
        
     // get a reference to the PanelStackBean from the faces context
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Object outputTreeModel = facesContext.getApplication().createValueBinding("#{workflowOutputTreeModelBean}").getValue(facesContext);
        if (outputTreeModel instanceof WorkflowOutputTreeModelBean){
        	treeModelBean = (WorkflowOutputTreeModelBean) outputTreeModel;
        }
	}

	public void setOutput(WorkflowOutput output) {
		this.output = output;
	}

	public WorkflowOutput getOutput() {
		return output;
	}

	 /**
     * ActionListener method called when a node in the tree is clicked.  Sets
     * the selected panel of the reference panelStack to the value of the instance
     * variable #displayPanel.   
     *
     * @param action JSF action event.
     */
    public void selectPanelStackPanel(ActionEvent action){
        if (treeModelBean != null){
        	treeModelBean.setSelectedNode(selectedNode);
        }
    }

	public void setTreeModelBean(WorkflowOutputTreeModelBean treeModelBean) {
		this.treeModelBean = treeModelBean;
	}

	public WorkflowOutputTreeModelBean getTreeModelBean() {
		return treeModelBean;
	}

	public String getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(String displayPanel) {
		this.selectedNode = displayPanel;
	}
}
