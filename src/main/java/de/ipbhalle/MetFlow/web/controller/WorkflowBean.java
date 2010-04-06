package de.ipbhalle.MetFlow.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.ValidatorException;
import javax.servlet.ServletContext;

import net.sf.taverna.t2.platform.taverna.TavernaBaseProfile;
import net.sf.taverna.t2.platform.taverna.WorkflowParser;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;

import org.icefaces.application.showcase.util.MessageBundleLoader;
import org.springframework.context.ApplicationContext;

import com.icesoft.faces.component.ext.HtmlDataTable;
import com.icesoft.faces.component.paneltabset.TabChangeEvent;
import com.icesoft.faces.component.paneltabset.TabChangeListener;
import com.icesoft.faces.context.effects.Effect;

import de.ipbhalle.MetFlow.io.PropertyManager;
import de.ipbhalle.MetFlow.io.T2WorkflowListing;
import de.ipbhalle.MetFlow.wrapper.WorkflowObject;
import de.ipbhalle.MetFlow.wrapper.WorkflowOutput;
import de.ipbhalle.MetFlow.wrapper.WorkflowOutputAlignment;
import de.ipbhalle.MetFlow.wrapper.WorkflowOutputHandler;

public class WorkflowBean implements TabChangeListener{

	/** The system's file separator. */
	private final String sep = System.getProperty("file.separator");
	
	/** The webapp root path. */
	private String appRootPath;
	
	/** The webapp temp path. */
	private String tempPath;
	
	/** The Taverna context. */
	private ApplicationContext context;
	
	/** The Taverna profile. */
	private TavernaBaseProfile profile;
	
	/** The Taverna workflow parser. */
	private WorkflowParser parser;
	
	/** The current Taverna workflow. */
	private WorkflowObject currentWorkflow;
	
	/** The predefined workflows. */
	private List<String> predefinedWorkflows;
	
	private Map<String, WorkflowObject> loadedWorkflows;
	
	private String ext_T2 = "t2flow";
	private String workflowPath = "workflows";
	private String ext_IMAGE = "png";
	
	/** binding for result datatable for retrieval workflow*/
	private HtmlDataTable retrievalResultTable;
	
	/** binding for result datatable for alignment workflow*/
	private HtmlDataTable alignmentResultTable;
	
	/** binding for alignment result datatable for alignment workflow*/
	private HtmlDataTable alignmentAlignResultTable;
	
	/** binding for result datatable for testing workflow*/
	private HtmlDataTable workflowResultTable;
	
	/** effect for highlighting changes in ICEfaces page */
	private Effect changeEffect;
	
	/** selected tab index*/
	private String selectedIndex = "0";
	
	/** selected alignment tab index*/
	private String selectedAlignIndex = "0";
	
	/** setting tab visibility for ICEfaces page */
	private boolean tabbedPane1Visible;
	private boolean tabbedPane2Visible;
	private boolean tabbedPane3Visible;
	private boolean tabbedPane4Visible;
	private boolean tabbedPane5Visible;
	
	/** String for the from-action element in the navigation rule */
	private String actionOutcome = "goRetrieval";
	
	/** list of list of aligned workflow outputs */ 
	private List<List<WorkflowOutputAlignment>> newAlign;
	
	/** list of list of aligned workflow outputs */ 
	private List<List<WorkflowOutputAlignment>> structAlign;
	
	/** boolean indicating if alignment has been run or not */
	private boolean runAlign = false;

	/** column data model for alignment results */
	private DataModel alignmentColumnDataModel;
	
	/** row data model for alignment results */
    private DataModel alignmentRowDataModel;
    
    /** hash map for the contents of the row and column data model */
    private Map cellMap = new HashMap();
	
    private WorkflowOutput treeNodeOutput;
    
    private WorkflowOutputHandler exporter;
    
	/** standard constructor -  fill all fields with the values stored in the TavernaBean */
	public WorkflowBean() {
		System.out.println("WorkflowBean constructor START...");
		FacesContext fc = FacesContext.getCurrentInstance();
		ELResolver el = fc.getApplication().getELResolver();
		ELContext elc = fc.getELContext();
		
		// access the application scoped PropertyManager bean
		TavernaBean tb = (TavernaBean) el.getValue(elc, null, "tavernaBean");
		this.appRootPath = tb.getAppRootPath();
		this.tempPath = tb.getTempPath();
		this.context = tb.getContext();
		this.profile = tb.getProfile();
		this.parser = tb.getParser();
		this.currentWorkflow = tb.getCurrentWorkflow();
		this.predefinedWorkflows = tb.getPredefinedWorkflows();
		
		System.out.println("WorkflowBean constructor FINISH...");
	}

	public void processTabChange(TabChangeEvent event)
		throws AbortProcessingException {
		// TODO Auto-generated method stub

	}
	
	public void refresh() {
		FacesContext fc = FacesContext.getCurrentInstance();
		ELResolver el = fc.getApplication().getELResolver();
		ELContext elc = fc.getELContext();
		
		// access the application scoped PropertyManager bean
		TavernaBean tb = (TavernaBean) el.getValue(elc, null, "tavernaBean");
		this.appRootPath = tb.getAppRootPath();
		this.tempPath = tb.getTempPath();
		this.context = tb.getContext();
		this.profile = tb.getProfile();
		this.parser = tb.getParser();
		this.currentWorkflow = tb.getCurrentWorkflow();
		reset();
	}
	
	public void runWorkflow(ActionEvent event) {
		FacesContext fc = FacesContext.getCurrentInstance();
		ELResolver el = fc.getApplication().getELResolver();
		ELContext elc = fc.getELContext();
		ServletContext sc = (ServletContext) fc.getExternalContext().getContext();
		
		// access the application scoped PropertyManager bean
		PropertyManager pm = (PropertyManager) el.getValue(elc, null, "propertyManager");
		if(!pm.getSingleValue("EXTENSION_IMAGE").equals("EXTENSION_IMAGE"))
			ext_IMAGE = pm.getSingleValue("EXTENSION_IMAGE");
		
		String attribute = MessageBundleLoader.getMessage("attNameWorkflow");
		String workflow = (String) event.getComponent().getAttributes().get(attribute);
		System.out.println("workflow -> " + workflow);
		String image = "";
		
		// only create a new Dataflow object if the workflow is available and has not been loaded previously
		if(this.predefinedWorkflows.contains(workflow) && !loadedWorkflows.containsKey(workflow)) {
			T2WorkflowListing t2w = new T2WorkflowListing(sc.getRealPath(workflowPath));
			try {
				List<String> images = t2w.listWorkflows(workflow.substring(0, workflow.indexOf(".") - 1), ext_IMAGE);
				image = images.get(0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			boolean error = false;
			Dataflow df;
			try {
				File in = new File(sc.getRealPath(workflowPath + sep + workflow));
				System.out.println("exists: " + in.exists());			
				df = this.parser.createDataflow(new FileInputStream(in));
//				this.currentWorkflow = new WorkflowObject(df, workflowPath + sep + image, workflow);				
				loadedWorkflows.put(workflow, new WorkflowObject(df, workflowPath + sep + image, workflow));
				this.currentWorkflow = null;
				this.currentWorkflow = loadedWorkflows.get(workflow);
			} catch (DeserializationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				error = true;
				if(loadedWorkflows.containsKey(workflow) && loadedWorkflows.get(workflow) == null)
					loadedWorkflows.remove(workflow);
			} catch (EditException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				error = true;
				if(loadedWorkflows.containsKey(workflow) && loadedWorkflows.get(workflow) == null)
					loadedWorkflows.remove(workflow);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				error = true;
				if(loadedWorkflows.containsKey(workflow) && loadedWorkflows.get(workflow) == null)
					loadedWorkflows.remove(workflow);
			}
			
			if(error) {
				FacesMessage message = new FacesMessage();
				message.setSeverity(FacesMessage.SEVERITY_ERROR);
				message.setSummary("Error occured while loading workflow. Please wait a few minutes and try again!");
				message.setDetail("Error occured while loading workflow. Please wait a few minutes and try again!");
				fc.addMessage("metflowForm:workflowStackPanel", message);
				error = false;
				throw new ValidatorException(message);
				// add navigation to error page ???
			}
			
		}
		else if(this.predefinedWorkflows.contains(workflow) && loadedWorkflows.containsKey(workflow)) {
			System.out.println("Workflow available and was also loaded before - do nothing!");
		}
		else if(!this.predefinedWorkflows.contains(workflow) && loadedWorkflows.containsKey(workflow)) {
			System.out.println("Workflow not available locally but was loaded before - do nothing");
		}
		else {
			System.out.println("Found no workflow with this name!!!");
			//TODO fehler handling
		}
	}
	
	public void runCurrentWorkflow(ActionEvent event) {
		FacesContext fc = FacesContext.getCurrentInstance();
		ELResolver el = fc.getApplication().getELResolver();
		ELContext elc = fc.getELContext();
		ServletContext sc = (ServletContext) fc.getExternalContext().getContext();
		
		boolean align = (Boolean) event.getComponent().getAttributes().get("align");
		
		if(this.currentWorkflow != null) {
			currentWorkflow.runWorkflow(context, profile, parser, appRootPath, align);
			this.treeNodeOutput = currentWorkflow.getOutputs().get(0);
		}
		
		selectedIndex = "2";
		tabbedPane3Visible = true;
		tabbedPane4Visible = true;
		tabbedPane5Visible = true;
	}
	
	public void fetchTreeNodeOutput(ActionEvent event) {
		WorkflowOutput wo = (WorkflowOutput) event.getComponent().getAttributes().get("output");
		this.treeNodeOutput = wo;
	}
	
//	public String runCurrentWorkflowAction() {
//		FacesContext fc = FacesContext.getCurrentInstance();
//		ELResolver el = fc.getApplication().getELResolver();
//		ELContext elc = fc.getELContext();
//		ServletContext sc = (ServletContext) fc.getExternalContext().getContext();
//		
//		if(this.currentWorkflow != null) {
//			currentWorkflow.runWorkflow(context, profile, parser, appRootPath, false);
//		}
//		
//		selectedIndex = "2";
//		tabbedPane3Visible = true;
//		
//		// handle navigation based on from-action outcome
//		if(predefinedWorkflows.contains(currentWorkflow.getName())) {
//			if(currentWorkflow.getName().equals(MessageBundleLoader.getMessage("attValueRetrieval")))
//				return "goRetrieval";
//			else if(currentWorkflow.getName().equals(MessageBundleLoader.getMessage("attValueAlign")))
//				return "goAlign";
//			else return "goWorkflow";
//		}
//		else return "error";
//	}
	
	public void alignToDBTest(ActionEvent ae) {
		// TODO aufruf von alignToDB needs parameterized version for list index
		/**
		 * use f:param and actionlistener (dropdown list of available lists?) inside jsp to 
		 * realize this  
		 */
		String primAlign = currentWorkflow.getPrimAlignCol();
		int primary = 0;
		
		if(primAlign.equalsIgnoreCase(WorkflowObject.NONE)) {
			// use none of the workflow output ports as primary alignment column
			// -> just show standard outputs as from Taverna
			primary = -1;
			
			List<List<WorkflowOutputAlignment>> newAlign = currentWorkflow.getAlign();
			this.newAlign = newAlign;
			this.runAlign = false;
		}
		else if(currentWorkflow.getOutputCols().contains(primAlign)) {
			primary = currentWorkflow.getOutputCols().indexOf(primAlign);
			
			List<List<WorkflowOutputAlignment>> newAlign = currentWorkflow.alignToDB(currentWorkflow.getAlignDB(),
					currentWorkflow.getAlign(), primary);

			this.newAlign = newAlign;
			this.runAlign = true;
		}
		
		generateAlignmentDataModel();
		
		System.out.println("primary = " + primary);
		System.out.println("#############");
		System.out.println("alignToDBTest");
		System.out.println("#############");
		for (int i = 0; i < newAlign.size(); i++) {
			for (int j = 0; j < newAlign.get(i).size(); j++) {
				System.out.println("i=" + i + " j= " + j + "   " + newAlign.get(i).get(j).getCompound() + "  "  + newAlign.get(i).get(j).getScore());
			}
			System.out.println();
			System.out.println();
		}

		// switch to resulting alignment sub tab after processing alignment
        selectedAlignIndex = "1";
	}	
	
	public void alignToMol(ActionEvent event) {
		// TODO aufruf von alignToDB needs parameterized version for list index
		/**
		 * use f:param and actionlistener (dropdown list of available lists?) inside jsp to 
		 * realize this  
		 */
		String primAlign = currentWorkflow.getPrimAlignCol();
		int primary = 0;
		
		if(primAlign.equalsIgnoreCase(WorkflowObject.NONE)) {
			// use none of the workflow output ports as primary alignment column
			// -> just show standard outputs as from Taverna
			primary = -1;
			
			List<List<WorkflowOutputAlignment>> structAlign = currentWorkflow.getAlign();
			this.newAlign = structAlign;
			this.runAlign = false;
		}
		else if(currentWorkflow.getOutputCols().contains(primAlign)) {
			primary = currentWorkflow.getOutputCols().indexOf(primAlign);
			
			List<List<WorkflowOutputAlignment>> structAlign = currentWorkflow.alignToMol(currentWorkflow.getAlign(), primary);

			this.newAlign = structAlign;
			this.runAlign = true;
		}
		
		generateAlignmentDataModel();
		
		// output alignment results
		System.out.println("primary = " + primary);
		System.out.println("#############");
		System.out.println("alignToMol Test");
		System.out.println("#############");
		for (int i = 0; i < newAlign.size(); i++) {
			for (int j = 0; j < newAlign.get(i).size(); j++) {
				if(newAlign.get(i) != null && newAlign.get(i).get(j) != null)
					System.out.println("i=" + i + " j= " + j + "   " + newAlign.get(i).get(j).getCompound() + "  "  + newAlign.get(i).get(j).getScore());
			}
			System.out.println();
			System.out.println();
		}

		// switch to resulting alignment sub tab after processing alignment
        selectedAlignIndex = "1";
	}
	
	public void generateAlignmentDataModel() {
		// Generate rowDataModel
        List rowList = new ArrayList();
        
        for (int i = 0; i < newAlign.size(); i++) {
            rowList.add(String.valueOf(i));
        }
        if (alignmentRowDataModel == null) {
        	alignmentRowDataModel = new ListDataModel(rowList);
        } else {
        	alignmentRowDataModel.setWrappedData(rowList);
        }
        alignmentRowDataModel = new ListDataModel(rowList);

        // Generate columnDataModel
        List columnList = new ArrayList();
//        for (int i = 0; i < currentWorkflow.getOutputCols().size(); i++) {
//			columnList.add(currentWorkflow.getOutputCols().get(i));
//		}
        for (int i = 0; i < currentWorkflow.getAlignCols().size(); i++) {
        	columnList.add(currentWorkflow.getAlignCols().get(i));
		}
        
        if (alignmentColumnDataModel == null) {
        	alignmentColumnDataModel = new ListDataModel(columnList);
        } else {
        	alignmentColumnDataModel.setWrappedData(columnList);
        }
        
	}

	 /**
     * Called from the ice:dataTable.  This method uses the columnDataModel and
     * rowDataModel with the CellKey utility class to display the correct cell
     * value.
     *
     * @return data which should be displayed for the given model state.
     */
    public WorkflowOutputAlignment getCellValue() {
    	// standard version with alignment
        if (alignmentRowDataModel.isRowAvailable() && alignmentColumnDataModel.isRowAvailable()) {
            // get the index of the row and column for this cell
            String row = (String) alignmentRowDataModel.getRowData();
            int currentRow = Integer.parseInt(row);
            Object column = alignmentColumnDataModel.getRowData();
            int currentColumn = ((ArrayList) alignmentColumnDataModel.getWrappedData()).indexOf(column);
            // return the element at this location
            Object key = new CellKey(row, column);
            if (!cellMap.containsKey(key)) {
                cellMap.put(key, newAlign.get(currentRow).get(currentColumn));
            }
            return (WorkflowOutputAlignment) cellMap.get(key);
        }
        return null;
    }
   
    /** reset previously set fields like selectedIndex and panel tab visibilities */
    public void reset() {
    	this.selectedIndex = "0";
    	this.tabbedPane1Visible = true;
    	this.tabbedPane2Visible = true;
    	this.tabbedPane3Visible = false;
    	this.tabbedPane4Visible = false;
    	this.tabbedPane5Visible = false;
    }
	
	public void setAppRootPath(String appRootPath) {
		this.appRootPath = appRootPath;
	}

	public String getAppRootPath() {
		return appRootPath;
	}

	public void setTempPath(String tempPath) {
		this.tempPath = tempPath;
	}

	public String getTempPath() {
		return tempPath;
	}

	public void setContext(ApplicationContext context) {
		this.context = context;
	}

	public ApplicationContext getContext() {
		return context;
	}

	public void setProfile(TavernaBaseProfile profile) {
		this.profile = profile;
	}

	public TavernaBaseProfile getProfile() {
		return profile;
	}

	public void setParser(WorkflowParser parser) {
		this.parser = parser;
	}

	public WorkflowParser getParser() {
		return parser;
	}

	public void setCurrentWorkflow(WorkflowObject currentWorkflow) {
		this.currentWorkflow = currentWorkflow;
	}

	public WorkflowObject getCurrentWorkflow() {
		return currentWorkflow;
	}

	public void setExt_T2(String ext_T2) {
		this.ext_T2 = ext_T2;
	}

	public String getExt_T2() {
		return ext_T2;
	}
	
	public List<String> getPredefinedWorkflows() {
		return predefinedWorkflows;
	}

	public void setPredefinedWorkflows(List<String> predefinedWorkflows) {
		this.predefinedWorkflows = predefinedWorkflows;
	}

	public Map<String, WorkflowObject> getLoadedWorkflows() {
		return loadedWorkflows;
	}

	public void setLoadedWorkflows(Map<String, WorkflowObject> loadedWorkflows) {
		this.loadedWorkflows = loadedWorkflows;
	}

	public String getWorkflowPath() {
		return workflowPath;
	}

	public void setWorkflowPath(String workflowPath) {
		this.workflowPath = workflowPath;
	}

	public String getExt_IMAGE() {
		return ext_IMAGE;
	}

	public void setExt_IMAGE(String extIMAGE) {
		ext_IMAGE = extIMAGE;
	}

	public void setRetrievalResultTable(HtmlDataTable retrievalResultTable) {
		this.retrievalResultTable = retrievalResultTable;
	}

	public HtmlDataTable getRetrievalResultTable() {
		return retrievalResultTable;
	}

	public void setAlignmentResultTable(HtmlDataTable alignmentResultTable) {
		this.alignmentResultTable = alignmentResultTable;
	}

	public HtmlDataTable getAlignmentResultTable() {
		return alignmentResultTable;
	}

	public void setWorkflowResultTable(HtmlDataTable workflowResultTable) {
		this.workflowResultTable = workflowResultTable;
	}

	public HtmlDataTable getWorkflowResultTable() {
		return workflowResultTable;
	}

	public void setAlignmentAlignResultTable(HtmlDataTable alignmentAlignResultTable) {
		this.alignmentAlignResultTable = alignmentAlignResultTable;
	}

	public HtmlDataTable getAlignmentAlignResultTable() {
		return alignmentAlignResultTable;
	}

	public void setChangeEffect(Effect changeEffect) {
		this.changeEffect = changeEffect;
	}

	public Effect getChangeEffect() {
		return changeEffect;
	}

	public void setTabbedPane1Visible(boolean tabbedPane1Visible) {
		this.tabbedPane1Visible = tabbedPane1Visible;
	}

	public boolean isTabbedPane1Visible() {
		return tabbedPane1Visible;
	}

	public void setTabbedPane2Visible(boolean tabbedPane2Visible) {
		this.tabbedPane2Visible = tabbedPane2Visible;
	}

	public boolean isTabbedPane2Visible() {
		return tabbedPane2Visible;
	}

	public void setTabbedPane3Visible(boolean tabbedPane3Visible) {
		this.tabbedPane3Visible = tabbedPane3Visible;
	}

	public boolean isTabbedPane3Visible() {
		return tabbedPane3Visible;
	}

	public void setTabbedPane4Visible(boolean tabbedPane4Visible) {
		this.tabbedPane4Visible = tabbedPane4Visible;
	}

	public boolean isTabbedPane4Visible() {
		return tabbedPane4Visible;
	}

	public void setSelectedIndex(String selectedIndex) {
		this.selectedIndex = selectedIndex;
	}

	public String getSelectedIndex() {
		return selectedIndex;
	}
	
	/** wrapper getter for selectedIndex to return an int representation */
	public int getFocusIndex() {
		return Integer.parseInt(selectedIndex);
	}
	
	/** wrapper setter for selectedIndex to store an int as new index */
	public void setFocusAlignIndex(int index) {
		selectedAlignIndex = String.valueOf(index);
	}

	/** wrapper getter for selectedIndex to return an int representation */
	public int getFocusAlignIndex() {
		return Integer.parseInt(selectedAlignIndex);
	}
	
	/** wrapper setter for selectedIndex to store an int as new index */
	public void setFocusIndex(int index) {
		selectedIndex = String.valueOf(index);
	}

	public void setActionOutcome(String actionOutcome) {
		this.actionOutcome = actionOutcome;
	}

	public String getActionOutcome() {
		return actionOutcome;
	}

	public List<List<WorkflowOutputAlignment>> getNewAlign() {
		return newAlign;
	}

	public void setNewAlign(List<List<WorkflowOutputAlignment>> newAlign) {
		this.newAlign = newAlign;
	}

	public boolean isRunAlign() {
		return runAlign;
	}

	public void setRunAlign(boolean runAlign) {
		this.runAlign = runAlign;
	}

	public void setAlignmentColumnDataModel(DataModel alignmentColumnDataModel) {
		this.alignmentColumnDataModel = alignmentColumnDataModel;
	}

	public DataModel getAlignmentColumnDataModel() {
		return alignmentColumnDataModel;
	}

	public void setAlignmentRowDataModel(DataModel alignmentRowDataModel) {
		this.alignmentRowDataModel = alignmentRowDataModel;
	}

	public DataModel getAlignmentRowDataModel() {
		return alignmentRowDataModel;
	}

	public void setCellMap(Map cellMap) {
		this.cellMap = cellMap;
	}

	public Map getCellMap() {
		return cellMap;
	}

	public void setTreeNodeOutput(WorkflowOutput treeNodeOutput) {
		this.treeNodeOutput = treeNodeOutput;
	}

	public WorkflowOutput getTreeNodeOutput() {
		return treeNodeOutput;
	}

	public void setExporter(WorkflowOutputHandler exporter) {
		this.exporter = exporter;
	}

	public WorkflowOutputHandler getExporter() {
		//return new WorkflowOutputHandler(tempPath + sep + "export_" + new Date().getTime(), FacesContext.getCurrentInstance(), currentWorkflow.getName());
		return new WorkflowOutputHandler(tempPath + sep + "export_" + new Date().getTime() + ".xls", FacesContext.getCurrentInstance(), "results");
		//return exporter;
	}

	public void setSelectedAlignIndex(String selectedAlignIndex) {
		this.selectedAlignIndex = selectedAlignIndex;
	}

	public String getSelectedAlignIndex() {
		return selectedAlignIndex;
	}

	public void setTabbedPane5Visible(boolean tabbedPane5Visible) {
		this.tabbedPane5Visible = tabbedPane5Visible;
	}

	public boolean isTabbedPane5Visible() {
		return tabbedPane5Visible;
	}

	public void setStructAlign(List<List<WorkflowOutputAlignment>> structAlign) {
		this.structAlign = structAlign;
	}

	public List<List<WorkflowOutputAlignment>> getStructAlign() {
		return structAlign;
	}

	/**
     * Utility class used to keep track of the cells in a table.
     */
    private class CellKey {
        private final Object row;
        private final Object column;

        /**
         * @param row
         * @param column
         */
        public CellKey(Object row, Object column) {
            this.row = row;
            this.column = column;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj instanceof CellKey) {
                CellKey other = (CellKey) obj;
                return other.row.equals(row) && other.column.equals(column);
            }
            return super.equals(obj);
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return (12345 + row.hashCode()) * (67890 + column.hashCode());
        }

        /**
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return row.toString() + "," + column.toString();
        }
    }
}
