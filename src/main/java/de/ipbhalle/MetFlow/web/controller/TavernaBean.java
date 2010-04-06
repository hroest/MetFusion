package de.ipbhalle.MetFlow.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.ServletContext;

import net.sf.taverna.t2.platform.taverna.TavernaBaseProfile;
import net.sf.taverna.t2.platform.taverna.WorkflowParser;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;

import org.icefaces.application.showcase.util.MessageBundleLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import de.ipbhalle.MetFlow.io.PropertyManager;
import de.ipbhalle.MetFlow.io.T2WorkflowListing;
import de.ipbhalle.MetFlow.wrapper.WorkflowObject;

/**
 * The Class TavernaBean.
 * This class instantiates the required Taverna classes to work with the Taverna 2 platform code.
 * This bean should be used in either session or application scope.
 * 
 * @author Michael Gerlich 
 */
public class TavernaBean {

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
	
	/**
	 * Instantiates a new taverna bean.
	 * uses Raven classloader to create Spring beans via the context.xml file
	 */
	public TavernaBean() {
		System.out.println("TavernaBean constructor START...");
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
		ELResolver el = fc.getApplication().getELResolver();
		ELContext elc = fc.getELContext();
		ServletContext sc = (ServletContext) ec.getContext();
		
		this.context = WebApplicationContextUtils.getRequiredWebApplicationContext(sc);
		this.profile = new TavernaBaseProfile(this.context);
		this.parser = profile.getWorkflowParser();
		
		this.appRootPath = sc.getRealPath(".");
		this.tempPath = sc.getRealPath(sep + "temp");
		
		this.loadedWorkflows = new HashMap<String, WorkflowObject>();
		
		// access the application scoped PropertyManager bean
		PropertyManager pm = (PropertyManager) el.getValue(elc, null, "propertyManager");
		
		System.out.println("appPath -> " + appRootPath);
		System.out.println("tempPath -> " + tempPath);
		System.out.println("webapp path -> " + sc.getRealPath(sc.getServletContextName()));
		System.out.println("contextPath -> " + sc.getContextPath());
		System.out.println("TavernaBean constructor FINISH...");
		
		// "load" predefined workflows
		loadWorkflows();
	}
	
	/**
	 * Method to load the predefined set of supplied workflows.
	 */
	public void loadWorkflows() {
		FacesContext fc = FacesContext.getCurrentInstance();
		ELResolver el = fc.getApplication().getELResolver();
		ELContext elc = fc.getELContext();
		ServletContext sc = (ServletContext) fc.getExternalContext().getContext();
		System.out.println("path -> " + sc.getRealPath("workflows" + sep + "T2_kegg.t2flow"));
		System.out.println("ServletContextName -> " + sc.getServletContextName());
		
//		try {
//			File in = new File(sc.getRealPath("workflows" + sep + "T2_kegg.t2flow"));
//			System.out.println("exists: " + in.exists());
//			//Dataflow workflow = parser.createDataflow("T2_kegg.t2flow");
//			Dataflow workflow = parser.createDataflow(new FileInputStream(in));
//			//Dataflow workflow = parser.createDataflow(sc.getResource("./workflows/T2_kegg.t2flow"));
//			for (DataflowInputPort dip : workflow.getInputPorts()) {
//				System.out.println("InputPortName: " + dip.getName() + "\tInputPortDepth: " + dip.getDepth());
//			}
//		} catch (DeserializationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (EditException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		// access the application scoped PropertyManager bean
		PropertyManager pm = (PropertyManager) el.getValue(elc, null, "propertyManager");

		if(pm.getValues().containsKey("EXTENSION_T2")) {
			this.ext_T2 = pm.getSingleValue("EXTENSION_T2");
		}
		if(!pm.getSingleValue("workflowPath").equals("workflowPath"))
			this.workflowPath = pm.getSingleValue("workflowPath");
		
		T2WorkflowListing t2w = new T2WorkflowListing(sc.getRealPath(workflowPath));
		try {
			this.predefinedWorkflows = t2w.listWorkflows("", ext_T2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Load a specific workflow.
	 * 
	 * @param event the ActionEvent from the corresponding commandButton or commandLink -
	 * it is used to retrieve the workflow name and
	 * use it as parameter for a Taverna WorkflowParser
	 */
	public void loadWorkflow(ActionEvent event) {
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
				// catch not found image with default/error image file
				image = "error.png";
			}
			
			Dataflow df;
			try {
				File in = new File(sc.getRealPath(workflowPath + sep + workflow));
				System.out.println("exists: " + in.exists());			
				df = this.parser.createDataflow(new FileInputStream(in));
//				this.currentWorkflow = new WorkflowObject(df, workflowPath + sep + image, workflow);				
				loadedWorkflows.put(workflow, new WorkflowObject(df, workflowPath + "/" + image, workflow));
				this.currentWorkflow = loadedWorkflows.get(workflow);
				System.out.println("workflow loaded!!!");
				
			} catch (DeserializationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EditException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		else if(this.predefinedWorkflows.contains(workflow) && loadedWorkflows.containsKey(workflow)) {
			System.out.println("Workflow available and was also loaded before - do nothing!");
			
			// update the current workflow to the current one
			this.currentWorkflow = loadedWorkflows.get(workflow);
		}
		else if(!this.predefinedWorkflows.contains(workflow) && loadedWorkflows.containsKey(workflow)) {
			System.out.println("Workflow not available locally but was loaded before - do nothing");
			
			// update the current workflow to the current one
			this.currentWorkflow = loadedWorkflows.get(workflow);
		}
		else {
			System.out.println("Found no workflow with this name!!!");
			//TODO fehler handling
		}
		
		// provide the workflowBean with the updated values for correctly displaying the values and parameters
		WorkflowBean wb = (WorkflowBean) el.getValue(elc, null, "workflowBean");
		wb.refresh();
	}

	/**
	 * Gets the Taverna application context.
	 * 
	 * @return the context
	 */
	public ApplicationContext getContext() {
		return context;
	}

	/**
	 * Sets the Taverna application context.
	 * 
	 * @param context the new context
	 */
	public void setContext(ApplicationContext context) {
		this.context = context;
	}

	/**
	 * Gets the Taverna base profile.
	 * 
	 * @return the profile
	 */
	public TavernaBaseProfile getProfile() {
		return profile;
	}

	/**
	 * Sets the Taverna base profile.
	 * 
	 * @param profile the new profile
	 */
	public void setProfile(TavernaBaseProfile profile) {
		this.profile = profile;
	}

	/**
	 * Gets the Taverna workflow parser.
	 * 
	 * @return the parser
	 */
	public WorkflowParser getParser() {
		return parser;
	}

	/**
	 * Sets the Taverna workflow parser.
	 * 
	 * @param parser the new parser
	 */
	public void setParser(WorkflowParser parser) {
		this.parser = parser;
	}

	/**
	 * Sets the webapp root path.
	 * 
	 * @param appRootPath the new webapp root path
	 */
	public void setAppRootPath(String appRootPath) {
		this.appRootPath = appRootPath;
	}

	/**
	 * Gets the webapp root path.
	 * 
	 * @return the webapp root path
	 */
	public String getAppRootPath() {
		return appRootPath;
	}
	
	/**
	 * Gets the os's file separator.
	 * 
	 * @return the sep
	 */
	public String getSep() {
		return sep;
	}

	/**
	 * Sets the webapp temp path.
	 * 
	 * @param tempPath the new webapp temp path
	 */
	public void setTempPath(String tempPath) {
		this.tempPath = tempPath;
	}

	/**
	 * Gets the webapp temp path.
	 * 
	 * @return the webapp temp path
	 */
	public String getTempPath() {
		return tempPath;
	}

	public void setCurrentWorkflow(WorkflowObject currentWorkflow) {
		this.currentWorkflow = currentWorkflow;
	}

	public WorkflowObject getCurrentWorkflow() {
		return currentWorkflow;
	}

	public void setPredefinedWorkflows(List<String> predefinedWorkflows) {
		this.predefinedWorkflows = predefinedWorkflows;
	}

	public List<String> getPredefinedWorkflows() {
		return predefinedWorkflows;
	}

	public void setLoadedWorkflows(Map<String, WorkflowObject> loadedWorkflows) {
		this.loadedWorkflows = loadedWorkflows;
	}

	public Map<String, WorkflowObject> getLoadedWorkflows() {
		return loadedWorkflows;
	}
}
