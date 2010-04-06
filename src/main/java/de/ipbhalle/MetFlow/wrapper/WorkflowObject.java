/* new datatype to store information
 * about Taverna workflow files
 * found in filesystem or web
 */
package de.ipbhalle.MetFlow.wrapper;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import org.biomoby.service.dashboard.data.ParametersTable;
import org.biomoby.shared.MobyException;
import org.biomoby.shared.MobySecondaryData;
import org.biomoby.shared.MobyService;
import org.biomoby.shared.extended.ServiceInstanceParser;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLWriter;
import org.springframework.context.ApplicationContext;

import com.icesoft.faces.component.ext.HtmlDataTable;
import com.icesoft.faces.component.ext.HtmlOutputLabel;
import com.icesoft.faces.component.ext.HtmlOutputText;
import com.icesoft.faces.component.ext.UIColumn;
import com.icesoft.faces.context.Resource;

import de.ipbhalle.MetFlow.utilities.MassBank.MassBankUtilities;
import de.ipbhalle.MetFlow.utilities.MetFlow.MetFlowUtilities;
import de.ipbhalle.MetFlow.utilities.MetFrag.Similarity;
import de.ipbhalle.MetFlow.utilities.MetFrag.SimilarityGroup;
import de.ipbhalle.MetFlow.utilities.MetFrag.SimilarityGroupWorkflowOutput;
import de.ipbhalle.MetFlow.utilities.MetFrag.SimilarityWorkflowOutput;
import de.ipbhalle.MetFlow.wrapper.WorkflowInput;
import de.ipbhalle.MetFlow.wrapper.WorkflowOutput;

import net.sf.taverna.t2.activities.biomoby.BiomobyActivity;
import net.sf.taverna.t2.activities.biomoby.BiomobyActivityConfigurationBean;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.facade.WorkflowInstanceListener;
import net.sf.taverna.t2.facade.WorkflowInstanceStatus;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.ProcessIdentifier;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.platform.taverna.Enactor;
import net.sf.taverna.t2.platform.taverna.TavernaBaseProfile;
import net.sf.taverna.t2.platform.taverna.WorkflowEditKit;
import net.sf.taverna.t2.platform.taverna.WorkflowParser;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.NamedWorkflowEntity;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;

/**
 * The Class WorkflowObject.
 * 
 * @author Michael Gerlich
 */
public class WorkflowObject {

	/** The Constant KEGG. */
	public static final String KEGG = "KEGG compound";
	
	/** The Constant PubChemC. */
	public static final String PubChemC = "pccompound";
	
	/** The Constant PubChemS. */
	public static final String PubChemS = "pcsubstance";
	
	/** The Constant NIKKAJI. */
	public static final String NIKKAJI = "NIKKAJI";
	
	/** The Constant INCHI. */
	public static final String INCHI = "InChI";
	
	/** The Constant SMILES. */
	public static final String SMILES = "SMILES";
	
	/** The Constant CAS. */
	public static final String CAS = "CAS";
	
	/** The Constant KNAPSACK. */
	public static final String KNAPSACK = "KNAPSACK";
	
	/** The Constant KAPPAVIEW. */
	public static final String KAPPAVIEW = "KAPPAVIEW";
	
	/** The Constant CHEBI. */
	public static final String CHEBI = "CHEBI";
	
	/** The Constant KEIO. */
	public static final String KEIO = "KEIO";
	
	/** The Constant LIPIDBANK. */
	public static final String LIPIDBANK = "LIPIDBANK";
	
	/** The Constant CAYMAN. */
	public static final String CAYMAN = "CAYMAN";
	
	/** The Constant CHEMSPIDER. */
	public static final String CHEMSPIDER = "CHEMSPIDER";
	
	/** The Constant CHEMPDB. */
	public static final String CHEMPDB = "CHEMPDB";
	
	/** The Constant SIRIUS. */
	public static final String SIRIUS = "SIRIUS";
	
	/** The Constant SCORE. */
	public static final String SCORE = "SCORE"; // use a score instead of a DB
	
	/** The Constant NONE. */
	public static final String NONE = "none";

	/** list of allowed databases used for alignment. */
	private static final List<String> allowables;
	static {
		allowables = new ArrayList<String>();
		allowables.add(NONE);
		allowables.add(KEGG);
		allowables.add(PubChemC);
		allowables.add(PubChemS);
		allowables.add(NIKKAJI);
		allowables.add(INCHI);
		allowables.add(SMILES);
		allowables.add(CAS);
		allowables.add(KNAPSACK);
		allowables.add(KAPPAVIEW);
		allowables.add(CHEBI);
		allowables.add(KEIO);
		allowables.add(LIPIDBANK);
		allowables.add(CAYMAN);
		allowables.add(CHEMSPIDER);
		allowables.add(CHEMPDB);
		allowables.add(SIRIUS);
		allowables.add(SCORE);
	}

	/** The list si. */
	private List<SelectItem> listSI; // static final

	/**	List of possible primary alignment columns, retrieved from workflow output ports. */
	private List<SelectItem> alignCol;
	
	/** Chosen primary alignment column, must be one of the workflow output ports. */
	private String primAlignCol;
	
	/** The identifier inserted if something is missing. */
	public static final String missing = "-----";

	/** Key identifier if there are no identifiers.	 */
	public static final String noID = "no identifiers";
	
	/** The web base path where workflow files are stored. */
	private URI base;

	/** The filesystem base path where workflow files are stored. */
	private URI uWorkflow;

	/** The filesystem base path where image files are stored. */
	private URI uImage;

	/** The URL represented as String for a workflow. */
	private String sURL;

	/** The URL for a workflow image represented as String. */
	private String sImage;

	private String name;
	
	/** The image w. */
	private String imageW;
	
	/** The image h. */
	private String imageH;

	/** The input ports. */
	private List<? extends DataflowInputPort> inputPorts;
	
	/** The output ports. */
	private List<? extends DataflowOutputPort> outputPorts;
	
	/** The workflow. */
	private Dataflow workflow = null;

	/** The result ref. */
	private Map<String, T2Reference> resultRef;
	
	/** The result keys. */
	private List<String> resultKeys;
	
	/** The result index. */
	private List<String> resultIndex;
	
	/** The inputs. */
	private List<WorkflowInput> inputs;
	
	/** The outputs. */
	private List<WorkflowOutput> outputs;
	
	/** The output cols. */
	private List<String> outputCols;

	/** The alignment output cols. */
	private List<String> alignCols;
	
	/** The num inputs. */
	private int numInputs;

	/** The num outputs. */
	private String numOutputs;
	
	/** The num col in. */
	private int numColIn = 1;
	
	/** The num col out. */
	private int numColOut = 1;

	/** The app path. */
	private String appPath;

	/** The processors. */
	List<? extends Processor> processors; // processors of a workflow object

	/** boolean that indicates if this workflow contains Biomoby services or not. */
	private boolean containsBiomoby = false;
	
	/** map containing all Biomoby secondary parameters, key is Biomoby service, value is name of secondary parameter. */
	private Map<String, String> secondaries;
	
	/** map which holds keys as Biomoby service names,  and values are map of Biomoby secondary parameter (key) and its value (value). */
	private Map<String, Map<String, String>> biomobySecondaries; // for biomoby service name, store all secondaries
	
	/** The sec. */
	private List<Map<String, String>> sec;
	
	/** contains entry set from map "secondaries". */
	private Set<Entry<String, String>> entry;

	/** The service keys. */
	private List<String> serviceKeys;
	
	/** The param keys. */
	private List<String> paramKeys;
	
	/** The param values. */
	private List<String> paramValues;
	
	/** The params. */
	private List<SecParam> params;

	/** standard entry for field peaks, containing pairwise mz and int values. */
	private String peaks = "147.044 20\n153.019 30\n273.076 999\n274.083 30";

	/** database identifier chosen for alignment of results, standard is set to KEGG (therefor also standard in workflow.jsp) */
	private String alignDB = KEGG;
	
	/** database identifier chosen for alignment of results, standard is set to KEGG (therefor also standard in workflow.jsp) */
	private String metfragDB;
	
	private String similarityAlign = "tanimoto";
	
	/** original (as retrieved from Taverna workflow output) list of lists containing aligned workflow outputs. */
	private List<List<WorkflowOutputAlignment>> align;
	
	/** modified (from function alignToDB()) list of lists containing newly aligned workflow outputs. */
	private List<List<WorkflowOutputAlignment>> modAlign;

	/** determines the width of the alignment output columns */
	private String alignColWidth = "50%";

	/** determines the width of the output columns */
	private String outputColWidth = "50%";

	/** column data model for alignment results */
	private DataModel alignmentColumnDataModel;
	
	/** row data model for alignment results */
    private DataModel alignmentRowDataModel;
    
    /** hash map for the contents of the row and column data model */
    private Map cellMap = new HashMap();
    
    /** boolean indicating whether this workflow invokes an alignment of its results or not. */
    private boolean toAlign;
    
    /** output resource for all workflow results, will be stored in xls file */
    private Resource outputResource;
    
    /** the file separator specific for the current os */
    private final String sep = System.getProperty("file.separator");
    
    private SimilarityWorkflowOutput similarity;
	private List<SimilarityGroupWorkflowOutput> groupedCandidates;
	
	/**
	 * Instantiates a new workflow object.
	 */
	public WorkflowObject() {
		this.base = null;
		this.uWorkflow = null;
		this.uImage = null;
		this.sURL = "";
		this.sImage = "";
		this.workflow = null;
		this.inputs = null;
	}


	/**
	 * Instantiates a new workflow object.
	 * 
	 * @param _base the _base
	 * @param _workflow the _workflow
	 * @param _uImage the _u image
	 * @param _url the _url
	 * @param _sImage the _s image
	 */
	public WorkflowObject(URI _base, URI _workflow, URI _uImage, String _url,
			String _sImage) {
		this.base = _base;
		this.uWorkflow = _workflow;
		this.uImage = _uImage;
		this.sURL = _url;
		this.sImage = _sImage;
		this.workflow = null;
		this.inputs = null;
	}


	/**
	 * Instantiates a new workflow object.
	 * 
	 * @param _base the URL base for the workflow
	 * @param _workflow the URI of the workflow path
	 * @param _uImage the URI of the workflow image
	 * @param _url the string representation of the workflow URL
	 * @param _sImage the string representation of the image URL
	 * @param workflow the workflow
	 */
	public WorkflowObject(URI _base, URI _workflow, URI _uImage, String _url,
			String _sImage, Dataflow workflow) {
		this.base = _base;
		this.uWorkflow = _workflow;
		this.uImage = _uImage;
		this.sURL = _url;
		this.sImage = _sImage;

		// fetch workflow image from URL and set dimensions properly
		try {
			URL u = new URL(_sImage);
			System.out.println("url -> " + u.toString());
			BufferedImage bi = ImageIO.read(u);
			this.imageW = String.valueOf(bi.getWidth());
			this.imageH = String.valueOf(bi.getHeight());
		} catch (MalformedURLException e) {
			System.out.println("Error: Wrong URL for workflow image!");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error while reading workflow image!");
			e.printStackTrace();
		}

		this.workflow = workflow;
		if (workflow != null) {
			System.out.println("workflow != null == true -> numInputs and numOutputs being set...");
	
			//this.inputPorts = workflow.getInputPorts();
			//this.inputs = new ArrayList<WorkflowInput>(workflow.getInputPorts().size());
			
			// create new wrapper for each input, including value field
//			for (int i = 0; i < workflow.getInputPorts().size(); i++) {
//				inputs.add(i, new WorkflowInput(workflow.getInputPorts().get(i)
//						.getName(), workflow.getInputPorts().get(i).getDepth(),
//						"", true));
//			}
			this.processors = workflow.getProcessors();
			
			// process current workflow, retrieve inputs and outputs and retrieve Biomoby secondary parameters
			processWorkflow();
			
			this.numInputs = workflow.getInputPorts().size();
			// define number of table columns via number of output ports
			this.numOutputs = String.valueOf(workflow.getOutputPorts().size());
		}

		// add available 
		this.listSI = new ArrayList<SelectItem>();
		for (int i = 0; i < WorkflowObject.allowables.size(); i++) {
			this.listSI.add(new SelectItem(WorkflowObject.allowables.get(i), WorkflowObject.allowables.get(i)));
		}
	}

	public WorkflowObject(Dataflow workflow, String imagePath, String name) {
		this(workflow, imagePath);
		this.name = name;
	}
	
	/**
	 * Constructor for a Dataflow object and an image path.
	 * 
	 * @param workflow
	 * @param imagePath
	 */
	public WorkflowObject(Dataflow workflow, String imagePath) {
		this.sImage = imagePath;
		
		FacesContext fc = FacesContext.getCurrentInstance();
		ServletContext sc = (ServletContext) fc.getExternalContext().getContext();
		
		// fetch workflow image from URL and set dimensions properly
		try {
//			URL u = new URL(imagePath);
//			System.out.println("url -> " + u.toString());
			System.out.println("imagePath -> " + imagePath);
			File u = new File(sc.getRealPath(imagePath));
			System.out.println("image exists: " + u.exists());
			BufferedImage bi = ImageIO.read(u);
			this.imageW = String.valueOf(bi.getWidth());
			this.imageH = String.valueOf(bi.getHeight());
		} catch (MalformedURLException e) {
			System.out.println("Error: Wrong URL for workflow image!");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error while reading workflow image!");
			e.printStackTrace();
		}
		
		this.workflow = workflow;
		if (workflow != null) {
			System.out.println("workflow != null == true -> numInputs and numOutputs being set...");
	
			//this.inputPorts = workflow.getInputPorts();
			//this.inputs = new ArrayList<WorkflowInput>(workflow.getInputPorts().size());
			
			// create new wrapper for each input, including value field
//			for (int i = 0; i < workflow.getInputPorts().size(); i++) {
//				inputs.add(i, new WorkflowInput(workflow.getInputPorts().get(i)
//						.getName(), workflow.getInputPorts().get(i).getDepth(),
//						"", true));
//			}
			this.processors = workflow.getProcessors();
			
			// process current workflow, retrieve inputs and outputs and retrieve Biomoby secondary parameters
			processWorkflow();
			
			this.numInputs = workflow.getInputPorts().size();
			// define number of table columns via number of output ports
			this.numOutputs = String.valueOf(workflow.getOutputPorts().size());
		}

		// add available 
		this.listSI = new ArrayList<SelectItem>();
		for (int i = 0; i < WorkflowObject.allowables.size(); i++) {
			this.listSI.add(new SelectItem(WorkflowObject.allowables.get(i), WorkflowObject.allowables.get(i)));
		}
		
		// add available 
		this.listSI = new ArrayList<SelectItem>();
		for (int i = 0; i < WorkflowObject.allowables.size(); i++) {
			this.listSI.add(new SelectItem(WorkflowObject.allowables.get(i), WorkflowObject.allowables.get(i)));
		}
	}
	
	/**
	 * Adds the inputs.
	 * 
	 * @param workflow the workflow
	 */
	public void addInputs(Dataflow workflow) {
		if (workflow != null) {
			this.inputPorts = workflow.getInputPorts();
			this.inputs = new ArrayList<WorkflowInput>(workflow.getInputPorts().size());
			this.numInputs = workflow.getInputPorts().size();

			// create new wrapper for each input, including value field
			for (int i = 0; i < workflow.getInputPorts().size(); i++) {
				System.out.println("input -> "
						+ workflow.getInputPorts().get(i).getName());
				System.out.println("depth -> "
						+ workflow.getInputPorts().get(i).getDepth());
				if (workflow.getInputPorts().get(i).getName().equals("mz")
						| workflow.getInputPorts().get(i).getName().equals(
								"int")) {
					inputs.add(i, new WorkflowInput(workflow.getInputPorts()
							.get(i).getName(), workflow.getInputPorts().get(i)
							.getDepth(), "", false));
					System.out.println("if");
				} else {
					inputs.add(i, new WorkflowInput(workflow.getInputPorts()
							.get(i).getName(), workflow.getInputPorts().get(i)
							.getDepth(), "", true));
					System.out.println("else");
				}
			}

			numFieldsIn();
		}
	}


	/**
	 * Adds the outputs.
	 * 
	 * @param workflow the workflow
	 */
	public void addOutputs(Dataflow workflow) {
		if (workflow != null) {
			this.setOutputPorts(workflow.getOutputPorts());
			this.outputs = new ArrayList<WorkflowOutput>(workflow.getOutputPorts().size());
			this.alignCol = new ArrayList<SelectItem>();		// create new list of selectitems for primary alignment column
			
			// create new wrapper for each input, including value field
			for (int i = 0; i < workflow.getOutputPorts().size(); i++) {
				outputs.add(i, new WorkflowOutput(workflow.getOutputPorts()
						.get(i).getName(), workflow.getOutputPorts().get(i)
						.getDepth(), ""));
				
				if(i == 0) {	// set first output port as standard primary alignment column
					this.primAlignCol = workflow.getOutputPorts().get(i).getName();
				}
				
				// add all workflow output portnames to list of possible primary alignment columns
				this.alignCol.add(new SelectItem(workflow.getOutputPorts().get(i).getName(), workflow.getOutputPorts().get(i).getName()));
			}

			// add entry "NONE" to list of possible primary alignment columns
			// TODO add usage of NONE into alignment -> no alignment at all? just display output of ports as written by Taverna ???
			this.alignCol.add(new SelectItem(NONE, NONE));
			numFieldsOut();
		}
	}


	/**
	 * determine the number of input fields according to the size of field inputs
	 */
	public void numFieldsIn() {
		int size = inputs.size();
		switch (size) {

		case 0:
			setNumColIn(0);
			break;
		case 1:
			setNumColIn(1);
			break;
		case 2:
			setNumColIn(2);
			break;
		case 3:
			setNumColIn(3);
			break;
		case 4:
			setNumColIn(2);
			break;
		case 5:
			setNumColIn(3);
			break;
		case 6:
			setNumColIn(3);
			break;
		case 7:
			setNumColIn(2);
			break;

		default:
			setNumColIn(2);
			break;
		}
	}


	/**
	 * determine the number of output fields according to the size of field outputs
	 */
	public void numFieldsOut() {
		int size = outputs.size();
		switch (size) {

		case 0:
			setNumColOut(0);
			break;
		case 1:
			setNumColOut(1);
			break;
		case 2:
			setNumColOut(2);
			break;
		case 3:
			setNumColOut(3);
			break;
		case 4:
			setNumColOut(2);
			break;
		case 5:
			setNumColOut(3);
			break;
		case 6:
			setNumColOut(3);
			break;
		case 7:
			setNumColOut(2);
			break;

		default:
			setNumColOut(2);
			break;
		}
	}


	/**
	 * Retrieve biomoby secondaries.
	 */
	public void retrieveBiomobySecondaries() {
		this.biomobySecondaries = new HashMap<String, Map<String, String>>();
		this.sec = new ArrayList<Map<String, String>>();
		this.serviceKeys = new ArrayList<String>();
		this.paramKeys = new ArrayList<String>();
		this.paramValues = new ArrayList<String>();
		this.params = new ArrayList<SecParam>();
		
		// retrieve Biomoby secondaries and put them into a wrapper list
		for (Processor processor : processors) {
			List<? extends Activity<?>> act = processor.getActivityList();
			for (int i = 0; i < act.size(); i++) {
				if (act.get(i).getClass() == BiomobyActivity.class) {
					this.containsBiomoby = true;

					// create new BiomobyActivityConfigurationBean from activity
					// configuration
					BiomobyActivityConfigurationBean bean = (BiomobyActivityConfigurationBean) act
							.get(i).getConfiguration();

					Map<String, String> secondaries = bean.getSecondaries();
					Set<String> keys = secondaries.keySet();
					for (Iterator<String> iterator = keys.iterator(); iterator
							.hasNext();) {
						String string = (String) iterator.next();
						System.out.println("key=" + string);
						String val = secondaries.get(string);

						// if(string.equals("database"))
						// val = "kegg";
						System.out.println("value=" + val);
					}

					paramKeys.addAll(keys);
					paramValues.addAll(secondaries.values());

					// create new BiomobyActivity from processor activity
					BiomobyActivity ba = (BiomobyActivity) act.get(i);
					// ParametersTable table = ba.getParameterTable();

					/**
					 * TODO RDF information der BiomobyActivity vorziehen??? rdf
					 * enthaelt aktuelle Parameterbeschreibungen, BA nicht....
					 * ==> ENTFERNEN
					 */
					try {
						ServiceInstanceParser sip = new ServiceInstanceParser(
								ba.getMobyService().getSignatureURL());
						// parse the RDF document and get our service
						MobyService service = sip.getMobyServicesFromRDF()[0];

						for (int i1 = 0; i1 < service.getSecondaryInputs().length; i1++) {
							System.out.println(service.getSecondaryInputs()[i1].getDescription());
						}
					} catch (MobyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					Map<String, SecParamObj> paramInstance = new HashMap<String, SecParamObj>();
					if (ba.containsSecondaries()) {
						MobySecondaryData[] mobySec = ba.getMobyService().getSecondaryInputs();
						for (int j = 0; j < mobySec.length; j++) {
							for (int j2 = 0; j2 < paramKeys.size(); j2++) {
								if (paramKeys.get(j2).equals(mobySec[j].getName())) {
									paramInstance.put(paramKeys.get(j2),
													new SecParamObj(mobySec[j].getName(),
																	mobySec[j].getDataType(),
																	mobySec[j].getDefaultValue(),
																	mobySec[j].getMinValue(),
																	mobySec[j].getMaxValue(),
																	mobySec[j].getAllowedValues(),
																	mobySec[j].getDescription()));
									
									
									break;
								}
							}
						}
					}

					biomobySecondaries.put(ba.getMobyService().getName(), secondaries);

					// new SecParam object for service
					// this.params.add(new SecParam(ba.getMobyService().getName(), paramKeys, paramValues));
					this.params.add(new SecParam(ba.getMobyService().getName(),
							paramKeys, paramValues, paramInstance));

					// fuege Biomoby servicename zur schluesselliste hinzu
					this.serviceKeys.add(ba.getMobyService().getName());

					sec.add(secondaries);
					this.secondaries = secondaries;
					this.entry = this.secondaries.entrySet();

					// int rows = table.getModel().getRowCount();
					// for (int j = 0; j < rows; j++) {
					// String key = (String) table.getModel().getValueAt(j, 0);
					// String value = table.getModel().getValueAt(j,
					// 1).toString();
					// System.out.println("key=" + key + ", value=" + value);
					// //modify one param, standard was 20, now 8
					// if(key.equals("Num_results"))
					// value = "8";
					// secondaries.put(key, value);
					// }
					// // set modified secondary parameters
					// bean.setSecondaries(secondaries);
					// // configure Biomoby service with its appropriate bean and
					// //an edits instance
					// ba.configure(bean, edits);

					// reset aramKeys and paramValues to null
					this.paramKeys = new ArrayList<String>();
					this.paramValues = new ArrayList<String>();
				}
			}
		}
	}

	/**
	 * processes the current workflow (dataflow object), adding input and output objects 
	 * as well as retrieving Biomoby secondaries if present inside apropriate services contained
	 * in the current workflow
	 */
	public void processWorkflow() {
		if(this.workflow != null) {
			// inputs noch setzen fuer workflowobject
			this.addInputs(workflow);				
			this.addOutputs(workflow);
			
			// retrieve processors from workflow
//			List<? extends Processor> procs = workflow.getProcessors();
//			this.setProcessors(procs);
			
			// create Biomoby secondary params
			this.retrieveBiomobySecondaries();
		}
	}
	
	/**
	 * Configure biomoby secondaries.
	 * 
	 * @param profile TavernaBaseProfile
	 * 
	 * @return fdgd
	 * 
	 * @author mgerlich
	 */
	public void configureBiomobySecondaries(TavernaBaseProfile profile) {
		// WorkflowEditKit -> Edits interface
		WorkflowEditKit wek = profile.getWorkflowEditKit();
		// ReflectionHelper rh = profile.getReflectionHelper();
		Edits edits = wek.getEdits();

		for (Processor processor : processors) {
			List<? extends Activity<?>> act = processor.getActivityList();
			for (int i = 0; i < act.size(); i++) {
				if (act.get(i).getClass() == BiomobyActivity.class) {
					// create new BiomobyActivityConfigurationBean from activity
					// configuration
					BiomobyActivityConfigurationBean bean = (BiomobyActivityConfigurationBean) act.get(i).getConfiguration();
					// create new BiomobyActivity from processor activity
					BiomobyActivity ba = (BiomobyActivity) act.get(i);

					for (SecParam sp : this.params) {
						System.out.println("SecParam name = " + sp.getServiceName());
						System.out.println("BiomobyActivity name = " + ba.getMobyService().getName());
						// only change secondary params for correct service
						if (sp.getServiceName().equals(ba.getMobyService().getName())) {
							Map<String, String> newsec = new HashMap<String, String>();

							if (sp.getParamKeys().size() == sp.getParamValues().size()) {
								// set new values to each service parameter
								for (int j = 0; j < sp.getParamKeys().size(); j++) {
									// old version using 2 List of Strings
//									newsec.put(sp.getParamKeys().get(j), sp.getParamValues().get(j));
									// TODO set new version
									// new version using the SecParamObj instance to retrieve the value from the page
									newsec.put(sp.getParamKeys().get(j), sp.getParamInstance().get(sp.getParamKeys().get(j)).getValue());
									System.out.println("paramInstance Key -> " + sp.getParamInstance().get(sp.getParamKeys().get(j)).getName());
									System.out.println("paramInstance Value -> " + sp.getParamInstance().get(sp.getParamKeys().get(j)).getValue());
									
									// store MetFrag search DB
									if(sp.getServiceName().equals("MetFrag_Query") && 
											sp.getParamInstance().get(sp.getParamKeys().get(j)).getName().equals("database")) {
										this.metfragDB = sp.getParamInstance().get(sp.getParamKeys().get(j)).getValue();
										System.out.println("MetFrag DB -> " + metfragDB);
									}
								}
							}

							// set modified secondary parameters
							bean.setSecondaries(newsec);
							// configure Biomoby service with its appropriate
							// bean and an edits instance
							ba.configure(bean, edits);

							break;
						}
					}
				}
			}
		}
	}


	/**
	 * method to extract mz and rel.int. from tab- or space-separated input text
	 * in variable peaks
	 */
	public void modifyPeaks() {
		if (this.peaks != null) {
			String peaks = this.peaks;
			if(peaks.contains("\n") && peaks.contains(";")) {
				peaks = peaks.replaceAll(";", "");
			}
			else if(!peaks.contains("\n") && peaks.contains(";")) {
				peaks = peaks.replaceAll(";", "\n");
			}
			
			String[] lineSplit = peaks.split("\n");

			StringBuffer buildMZ = new StringBuffer();
			StringBuffer buildINT = new StringBuffer();

			for (int i = 0; i < lineSplit.length; i++) {
				String[] peakSplit = null; // [0] = mz, [1] = int, [2] =
				// relative int

				if (lineSplit[i].contains("\t")) {
					// modified
					lineSplit[i] = lineSplit[i].trim();
					peakSplit = lineSplit[i].split("\t");
				} else if (lineSplit[i].contains(" ")) {
					// modified
					lineSplit[i] = lineSplit[i].trim();
					peakSplit = lineSplit[i].split(" ");
				}
				else {
					// modified
					lineSplit[i] = lineSplit[i].trim();
					peakSplit = new String[1];				
					peakSplit[0] = lineSplit[i];
				}

				if(peakSplit.length == 1) {	// [0] = mz
					buildMZ.append(peakSplit[0]).append("\n");
					// use standard intensity of 100
					buildINT.append("100").append("\n");
				}
				if (peakSplit.length == 2) { // [0] = mz, [1] = relative int
					buildMZ.append(peakSplit[0]).append("\n");
					buildINT.append(peakSplit[1]).append("\n");
				}

				if (peakSplit.length == 3) { // [0] = mz, [1] = int, [2] =
					// relative int
					buildMZ.append(peakSplit[0]).append("\n");
					buildINT.append(peakSplit[2]).append("\n");
				}

			}

			for (int i = 0; i < this.inputs.size(); i++) {
				if (inputs.get(i).getName().equals("mz")) {
					inputs.get(i).setValue(buildMZ.toString());
					// inputs.set(i, new WorkflowInput(name, depth,
					// buildMZ.toString()));
					// this.getInputs().set(i, new WorkflowInput(name, depth,
					// buildMZ.toString()));
				}

				if (inputs.get(i).getName().equals("int")) {
					inputs.get(i).setValue(buildINT.toString());
					// inputs.set(i, new WorkflowInput(name, depth,
					// buildINT.toString()));
					// this.getInputs().set(i, new WorkflowInput(name, depth,
					// buildINT.toString()));
				}
			}
		}
	}


	/**
	 * method which invokes a workflow.
	 * 
	 * @param context the context
	 * @param profile the profile
	 * @param parser the parser
	 * @param path the path
	 */
	public void runWorkflow(ApplicationContext context,
			TavernaBaseProfile profile, WorkflowParser parser, String path, boolean toAlign) {
		this.appPath = path;
		System.out.println("Workflow start...");
		
		// indicate if alignment is wanted or not
		this.toAlign = toAlign;
		
		// modify peaklist from textinputarea peaks
		modifyPeaks();

		try {
			if (this.workflow == null) { // if there is no Dataflow object yet,
				// create one
				URL workflowURL = new URL(sURL);
				this.workflow = parser.createDataflow(workflowURL);
				System.out.println("workflow was null, now created!");

				// add inputPorts to this instance
				this.inputPorts = workflow.getInputPorts();
				// this.inputs = new
				// ArrayList<WorkflowInput>(workflow.getInputPorts().size());
				// // create new wrapper for each input, including value field
				// for (int i = 0; i < workflow.getInputPorts().size(); i++) {
				// inputs.add(i, new
				// WorkflowInput(workflow.getInputPorts().get(i).getName(),
				// workflow.getInputPorts().get(i).getDepth(), ""));
				// }
				this.processors = workflow.getProcessors();
			}
		}
		catch (DeserializationException e) {
			e.printStackTrace();
			System.out.println("Error when deserialising workflow xml file!");
			return;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.out.println("Error: wrong/mistyped workflow URL!");
			return;
		} catch (EditException e) {
			e.printStackTrace();
			System.out.println("Error while parsing/editing workflow!");
			return;
		} 
		
		DataflowValidationReport rep = workflow.checkValidity();
		if (!rep.isValid()) {
			System.out.println("Non-valid workflow!");
			return;
		}

		if (containsBiomoby) {
			// veraenderte secondary params an workflow uebergeben
			configureBiomobySecondaries(profile);
		}

		// run workflow
		ReferenceService rs = profile.getReferenceService();
		Enactor e = profile.getEnactor();
		WorkflowInstanceFacade instance = e.createFacade(workflow);

		/**
		 * Create and bind a new workflow listener. This allows your code to
		 * track completion and failure events rather than just waiting for
		 * a final result. It will also allow you to trap any partial
		 * results, such as individual items in the output list produced by
		 * this workflow.
		 * <p>
		 * Note - this uses a so called anonymous inner class, in effect an
		 * inline definition of a class implementing the
		 * WorkflowInstanceListener interface.
		 * <p>
		 * Implement the listener interface to handle all possible messages
		 * and print a summary to the console.
		 */
		WorkflowInstanceListener listener = new WorkflowInstanceListenerImpl();

		/**
		 * Attach your new listener to the workflow instance
		 */
		instance.addWorkflowInstanceListener(listener);

		// TODO neue features von else {} hier mit hinzufuegen
		if (inputPorts.size() == 0 || inputPorts.equals(null)) {
			instance.fire();
		} else {
			System.out.println("push data into input ports!");
			ArrayList<T2Reference> listRefs = new ArrayList<T2Reference>(inputs.size());

			if (inputs.size() > 0) {
				for (int i = 0; i < inputs.size(); i++) {
					String val = inputs.get(i).getValue();
					int depth = inputs.get(i).getDepth();
					String name = inputs.get(i).getName();
					System.out.println(val);

					if (depth == 0) { // only one value allowed
						// if val contains more than one value, only take
						// the first one
						if (val.contains("\n")) {
							if (val.split("\\s").length > 1) {
								val = val.split("\\s")[0];
							}
						}
						val = val.trim();
						
						T2Reference input = rs.register(val, depth, true, null);
						listRefs.add(input);
						e.pushData(instance, name, input);
					} else if (depth == 1) { // a list of values allowed
						ArrayList<String> list = new ArrayList<String>();

						if (val.contains("\n")) {
							String[] split = val.split("\n");
							for (int j = 0; j < split.length; j++) {
								list.add(split[j].trim());
							}
						} else if (val.contains(";")) {
							String[] split = val.split(";");
							for (int j = 0; j < split.length; j++) {
								list.add(split[j].trim());
							}
						} else {
							list.add(val.trim());
						}
						T2Reference input = rs.register(list, depth, true, null);
						listRefs.add(input);
						e.pushData(instance, name, input);
					} else { // n-deep list of lists allowed
						System.out.println("n-deep list not yet configured!");
					}
				}
			}

		}
		
		// retrieve T2References from all output ports
		Map<String, T2Reference> results = e.waitForCompletion(instance);

		this.outputs = new ArrayList<WorkflowOutput>(results.size());
		this.outputCols = new ArrayList<String>();

		// alignment
		this.align = new ArrayList<List<WorkflowOutputAlignment>>();
		//List<WorkflowOutputAlignment> toAlign = new ArrayList<WorkflowOutputAlignment>();

		Set<String> keys = results.keySet();
		Iterator<String> it = keys.iterator();
		int counter = 0;
		int counterMB = 0; // only increased for MassBank records
		while (it.hasNext()) {
			String next = it.next();
			this.outputCols.add(next);
			// this.outputCols.add(next + " Score:");
			T2Reference ref = results.get(next);

			// format T2References into something useful
			this.outputs.add(counter, new WorkflowOutput(next, ref, rs,	appPath));
			
			/** begin alignment preprocessing between
			 * MassBank and MetFrag
			 * **/
			if(toAlign) {
				List<String> compName = new ArrayList<String>(); // stores the compound names from MassBank records
				Map<String, String> dbs = new HashMap<String, String>(); // stores all DB entries from MassBank records
				
				String prevComp = ""; // previous compound
				Double score = 0.0;
				String prevRecord = "";	// previous record ID
				
				for (int i = 0; i < this.outputs.get(counter).getElements().size(); i++) {
					String out = outputs.get(counter).getElements().get(i).getValue();
					String[] split = out.split("\t");
					List<WorkflowOutputAlignment> element = new ArrayList<WorkflowOutputAlignment>();
	
					String compoundName = "";
					String db = "";
					String id = "";
					String record = "";
	
					if (split.length == 2) // MetFragOut, no compound name
					{
						compoundName = "unknown";
						id = split[0];
						String temp = split[1].substring(split[1].indexOf("=") + 1);
						score = Double.valueOf(temp.trim());
						
						IAtomContainer ac = null;
						if(metfragDB.equals("kegg") || split[1].matches("C[0-9]{5}")) {
							db = KEGG;
							ac = MetFlowUtilities.KEGGGetMolFromID(id);
						}
						else if(metfragDB.equals("pubchem")) {
							db = PubChemC;
							ac = MetFlowUtilities.PubChemGetMolFromID(id);
						}
						else if(metfragDB.equals("chemspider")) {
							db = CHEMSPIDER;
							ac =  MetFlowUtilities.ChemSpiderGetMolFromID(id);
						}
						
//						if (split[0].matches("C[0-9]{5}")) { // KEGG(it.
//							db = KEGG;
//							record = id;
//							ac = MetFlowUtilities.KEGGGetMolFromID(id);
//						} else if (split[0].matches("[0-9]*")) { // PubChem
//							db = PubChemC;
//							record = id;
//							ac = MetFlowUtilities.PubChemGetMolFromID(id);
//						} else { // ChemSpider
//							record = id;
//							db = CHEMSPIDER;
//							ac = MetFlowUtilities.ChemSpiderGetMolFromID(id);
//						}
												
						dbs.put(db, id);
						
	//						element.add(counter, new WorkflowOutputAlignment(next,
	//								alignDB, score, db, id, compoundName));
						//element.add(counter, new WorkflowOutputAlignment(next, alignDB, score, db, id, compoundName, dbs));
						element.add(counter, new WorkflowOutputAlignment(next, alignDB, score, db, id, compoundName, dbs, true, record, ac));
						
						// set flag if molfile aquired
						if(ac != null)
							element.get(counter).setAquiredMol(true);
						
						align.add(element);
	
						dbs = new HashMap<String, String>();
						
						//toAlign.add(i, new WorkflowOutputAlignment(next, alignDB, score, db, id, compoundName));
						//toAlign.get(i).print();
					} else if (split.length == 3) // MetFragOut, with compound name
					{
						if (split[0].contains("#")) // more than one compound name
						{
							String[] names = split[0].split("#");
							compoundName = names[0];
						} else
							compoundName = split[0];
	
						id = split[1];
						String temp = split[2].substring(split[2].indexOf("=") + 1);
						score = Double.valueOf(temp.trim());
						
						IAtomContainer ac = null;
						if(metfragDB.equals("kegg") || split[1].matches("C[0-9]{5}")) {
							db = KEGG;
							ac = MetFlowUtilities.KEGGGetMolFromID(id);
						}
						else if(metfragDB.equals("pubchem")) {
							db = PubChemC;
							ac = MetFlowUtilities.PubChemGetMolFromID(id);
						}
						else if(metfragDB.equals("chemspider")) {
							db = CHEMSPIDER;
							ac =  MetFlowUtilities.ChemSpiderGetMolFromID(id);
						}
												
//						if (split[1].matches("C[0-9]{5}")) { // KEGG
//							db = KEGG;
//							ac = MetFlowUtilities.KEGGGetMolFromID(id);
//						} else if (split[1].matches("[0-9]*")) { // PubChem
//							db = PubChemC;
//							ac = MetFlowUtilities.PubChemGetMolFromID(id);
//						} else { // ChemSpider
//							db = CHEMSPIDER;
//							ac =  MetFlowUtilities.ChemSpiderGetMolFromID(id);
//						}
						record = id;
						dbs.put(db, id);
						
	//						element.add(counter, new WorkflowOutputAlignment(next,
	//								alignDB, score, db, id, compoundName));
						//element.add(counter, new WorkflowOutputAlignment(next, alignDB, score, db, id, compoundName, dbs));
						element.add(counter, new WorkflowOutputAlignment(next, alignDB, score, db, id, compoundName, dbs, true, record, ac));
						
						// set flag if molfile aquired
						if(ac != null)
							element.get(counter).setAquiredMol(true);
						
						align.add(element);
	
						dbs = new HashMap<String, String>();
						
						//toAlign.add(i, new WorkflowOutputAlignment(next, alignDB, score, db, id, compoundName));
						//toAlign.get(i).print();
					}
					// TODO check conditions!
					else if (split.length >= 4) // MassBankOut
					{
						if (prevComp.isEmpty() & !compName.contains(split[0])) { // first
							// compound
							prevComp = split[0];
							compName.add(split[0]);
							dbs.put(split[1], split[2]);
							String temp = split[3].substring(split[3].indexOf("=") + 1);
							score = Double.valueOf(temp.trim());
							if(split.length > 4 && split[4].matches("[A-Z]{2,3}[0-9]{5,6}")) {
								record = split[4];
							}
							prevRecord = record;
						} else if ((!prevComp.isEmpty() & !prevComp.equals(split[0])) 
								| (i == this.outputs.get(counter).getElements().size() - 1)) { // found new compound
							if (dbs.containsKey(alignDB)) {
								db = alignDB;
								id = dbs.get(db);
							} else {
								db = missing;
								id = missing;
							}
							compoundName = prevComp;
							prevComp = split[0];
	
							if(split.length > 4 && split[4].matches("[A-Z]{2,3}[0-9]{5,6}")) {
								record = split[4];
							}
							
							// String temp = split[3].substring(split[3].indexOf("=") + 1);
							// System.out.println("score parsing -> " + temp);
							// score = Double.valueOf(temp.trim());
	
							// align.add(element);
							try {
								// element.add(counter, new WorkflowOutputAlignment(next, alignDB, score, db, id, compoundName));
								// counterMB
								
								// add next output to position counterMB, if there is no entry, CATCH clause is used
								//align.get(counterMB).add(new WorkflowOutputAlignment(next, alignDB, score, db, id, compoundName, dbs));
								
								/** retrieve MassBank mol data for current record */
								String site = MassBankUtilities.retrieveSite(prevRecord);
								String mol = MassBankUtilities.retrieveMol(compoundName, site, prevRecord);
								IAtomContainer container = MassBankUtilities.getContainer(mol);
																
								System.out.println("TRY compoundName -> " + compoundName + ",  site -> " + site);
								align.get(counterMB).add(new WorkflowOutputAlignment(next, alignDB, score, db, id,
										compoundName, dbs, true, prevRecord, container));
								
								// set flag if molfile aquired
								if(align.get(counterMB).get(align.get(counterMB).size()-1).getContainer() != null)
									align.get(counterMB).get(align.get(counterMB).size()-1).setAquiredMol(true);
									
							} catch (IndexOutOfBoundsException ie) {
								element = new ArrayList<WorkflowOutputAlignment>();
								// TODO ueberpruefen ob vorhandende eintraege ueberschrieben werden !!!
								// CATCH was triggered -> for this output port counter/next, no corresponding outputs exists from 
								// previous output ports -> fill with blank ones ==> OUTER JOIN in SQL
								if(counter == 0) {		// TODO check integrity/notwendigkeit
									//element.add(new WorkflowOutputAlignment(next, alignDB, 0.0, "", "", ""));
									//element.add(new WorkflowOutputAlignment(next, alignDB, score, db, id, compoundName, dbs));
									
									/** retrieve MassBank mol data for current record */
									String site = MassBankUtilities.retrieveSite(prevRecord);
									String mol = MassBankUtilities.retrieveMol(compoundName, site, prevRecord);
									IAtomContainer container = MassBankUtilities.getContainer(mol);
																	
									System.out.println("CATCH counter == 0: compoundName -> " + compoundName + ",  site -> " + site);
									element.add(new WorkflowOutputAlignment(next, alignDB, score, db, id, 
											compoundName, dbs, true, prevRecord, container));
									
									// set flag if molfile aquired
									if(element.get(element.size()-1).getContainer() != null)
										element.get(element.size()-1).setAquiredMol(true);
								}
								for (int j = 0; j < counter; j++) {
									//element.add(new WorkflowOutputAlignment(this.outputCols.get(j),	missing, 0.0, missing, missing, missing));
									//element.add(new WorkflowOutputAlignment(this.outputCols.get(j),	missing, 0.0, missing, missing, missing));
									// TODO change behaviour
									element.add(new WorkflowOutputAlignment(""));		//element.add(new WorkflowOutputAlignment(missing));
									//element.add(null);
								}								
								//element.add(new WorkflowOutputAlignment(next, alignDB, score, db, id, compoundName, dbs));
								
								/** retrieve MassBank mol data for current record */
								String site = MassBankUtilities.retrieveSite(prevRecord);
								String mol = MassBankUtilities.retrieveMol(compoundName, site, prevRecord);
								IAtomContainer container = MassBankUtilities.getContainer(mol);
								
								System.out.println("CATCH compoundName -> " + compoundName + ",  site -> " + site);
								element.add(new WorkflowOutputAlignment(next, alignDB, score, db, id, 
										compoundName, dbs, true, prevRecord, container));
								//element.set(counter, new WorkflowOutputAlignment(next, alignDB, score, db, id, compoundName, dbs));
								
								// set flag if molfile aquired
								if(element.get(element.size()-1).getContainer() != null)
									element.get(element.size()-1).setAquiredMol(true);
								
								align.add(element);
							}
	
	//						toAlign.add(counterMB, new WorkflowOutputAlignment(
	//								next, alignDB, score, db, id, compoundName,
	//								dbs));
	//						toAlign.get(counterMB).print();
							counterMB++;
							dbs = new HashMap<String, String>();
							//prevRecord = record;
						} else {
							dbs.put(split[1], split[2]);
						}
	
						if (!compName.contains(split[0])) { // new compound, not yet discovered
							compName.add(split[0]);
							compoundName = split[0];
							dbs.put(split[1], split[2]);
							String temp = split[3].substring(split[3].indexOf("=") + 1);
							score = Double.valueOf(temp.trim());
							if(split.length > 4 && split[4].matches("[A-Z]{2,3}[0-9]{5,6}")) {
								record = split[4];
								prevRecord = record;
							}
						} else if (compName.contains(split[0])) {
							dbs.put(split[1], split[2]);
						}
					}
					// toAlign.add(new WorkflowOutputAlignment());
				}
	
				// prepare for alignment
				// align.add(counter, toAlign);
				// toAlign.clear();
				// dbs.clear();
				prevComp = "";
				compName.clear();
	
				counter++;
	
				// determine the width of the output columns
				int w = 100 / outputCols.size();
				this.outputColWidth = String.valueOf(w) + "%";
			}
			/** end of alignment 
			 * */			
		}
		// create data model from newAlign to simpify use in ICEfaces columns
		generateAlignmentDataModel();
		
		// store result T2References in field resultRef
		resultRef = results;
		resultKeys = new ArrayList<String>();
		resultKeys.addAll(resultRef.keySet());
	
		resultIndex = new ArrayList<String>();
		for (int i = 0; i < resultKeys.size(); i++) {
			resultIndex.add(String.valueOf(i));
		}
		
		// sort output ports lexicographically
		Collections.sort(outputs);
		
		// create output resource for all workflow outputs
		generateOutputResource();
		
		System.out.println("Workflow finished...");
	}

	/** generates an output resource for the current workflow results, everything is stored inside a single Excel xls file
	 *  where each workflow output ports is stored as a separate sheet  */
	private void generateOutputResource() {
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
		HttpSession session = (HttpSession) ec.getSession(false);
		String sessionString = session.getId();	
		long time = new Date().getTime();
		String path = appPath + sep + "temp" + sep + sessionString + sep;// + "workflowResults_" + time +  ".xls";
		System.out.println("ressource path -> " + path);
		
		File dir = new File(path);
		if(!dir.exists())
			dir.mkdirs();
		
		// skip creation of output resource if file access is denied
		if(!dir.canWrite())
			return;
		String resourceName = "workflowResults_" + time +  ".xls";
		File f = new File(dir, resourceName);
		try {
			f.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// create new Excel file
		WritableSheet sheet = null;
		WritableWorkbook workbook = null;
		WorkbookSettings settings = new WorkbookSettings();
		settings.setLocale(FacesContext.getCurrentInstance().getViewRoot().getLocale());
		try {
			workbook = Workbook.createWorkbook(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String mimeType = "application/vnd.ms-excel";
		
		// for each workflow output port, create new sheet inside Excel file and store results
		for (WorkflowOutput port : outputs) {
			// set sheet name (output port) and position
			sheet = workbook.createSheet(port.getName(), outputs.indexOf(port));
			ArrayList<WorkflowOutput> elements = port.getElements();
			
			// set header for sheet, name it after output port name 
			try {
				WritableFont arial10font = new WritableFont(WritableFont.ARIAL, 10);
				WritableCellFormat arial10format = new WritableCellFormat(
						arial10font);
				arial10font.setBoldStyle(WritableFont.BOLD);
				Label label = new Label(0, 0, port.getName(), arial10format);
				sheet.addCell(label);
			} catch (WriteException we) {
				we.printStackTrace();
			}
			
			// for all output elements, store their result inside the current sheet
			// either store the image or the value part of an output 
			for (int i = 0; i < elements.size(); i++) {
				WritableCell cell = null;
				WritableImage wi = null;
				if(elements.get(i).isImage()) {		// output is image
					String imgPath = appPath + elements.get(i).getPath();
					File image = new File(imgPath);
					// write each image into the second column, leave one row space between them and 
					// resize the image to 1 column width and 2 rows height
					wi = new WritableImage(1, (i*3) + 1, 1, 2, image);
					sheet.addImage(wi);
				}
				else if(!elements.get(i).isImage()) {	// output is text
					cell = new Label(1, i, elements.get(i).getValue());
					try {
						sheet.addCell(cell);
					} catch (WriteException e) {
						System.out.println("Could not write excel cell");
						e.printStackTrace();
					}
				}
			}
		}
		
		// write the Excel file
		try {
			workbook.write();
			workbook.close();
		} catch (WriteException ioe) {
			ioe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		// store the current Excel file as output resource for the current workflow
		outputResource = new OutputResource(ec, resourceName);
	}


	/** generate a columns datamodel for easy handling of columns tag in JSF */
	public void generateAlignmentDataModel() {
		// Generate rowDataModel
        List<String> rowList = new ArrayList<String>();
        
        for (int i = 0; i < align.size(); i++) {
            rowList.add(String.valueOf(i));
        }
        if (alignmentRowDataModel == null) {
        	alignmentRowDataModel = new ListDataModel(rowList);
        } else {
        	alignmentRowDataModel.setWrappedData(rowList);
        }
        alignmentRowDataModel = new ListDataModel(rowList);

        // Generate columnDataModel
        List<String> columnList = new ArrayList<String>();
        for (int i = 0; i < outputCols.size(); i++) {
			columnList.add(outputCols.get(i));
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
    	// standard version without alignment
        if (alignmentRowDataModel.isRowAvailable() && alignmentColumnDataModel.isRowAvailable() && toAlign) {
            // get the index of the row and column for this cell
            String row = (String) alignmentRowDataModel.getRowData();
            int currentRow = Integer.parseInt(row);
            Object column = alignmentColumnDataModel.getRowData();
            int currentColumn = ((ArrayList) alignmentColumnDataModel.getWrappedData()).indexOf(column);
            // return the element at this location
            Object key = new CellKey(row, column);
            if (!cellMap.containsKey(key)) {
                cellMap.put(key, align.get(currentRow).get(currentColumn));
            }
            return (WorkflowOutputAlignment) cellMap.get(key);
        }
        // version with alignment
        else if (alignmentRowDataModel.isRowAvailable() && alignmentColumnDataModel.isRowAvailable() && !toAlign) {
            // get the index of the row and column for this cell
            String row = (String) alignmentRowDataModel.getRowData();
            int currentRow = Integer.parseInt(row);
            Object column = alignmentColumnDataModel.getRowData();
            int currentColumn = ((ArrayList) alignmentColumnDataModel.getWrappedData()).indexOf(column);
            // return the element at this location
            Object key = new CellKey(row, column);
            if (!cellMap.containsKey(key)) {
                cellMap.put(key, modAlign.get(currentRow).get(currentColumn));
            }
            return (WorkflowOutputAlignment) cellMap.get(key);
        }
        return null;
    }
    
	/**
	 * align all output ports to specific database.
	 * 
	 * @param db database used for alignment, value from alignDB
	 * @param toAlign the align
	 * @param primary index of the list which is taken as primary sorting attribute
	 * 
	 * @return new list of lists containing aligned outputs
	 */
	public List<List<WorkflowOutputAlignment>> alignToDB(String db, List<List<WorkflowOutputAlignment>> toAlign, int primary) {
		// TODO listen filtern, sortieren nach score + identifier (erst score,
		// dann id)
		List<List<WorkflowOutputAlignment>> newAlign = new ArrayList<List<WorkflowOutputAlignment>>();
		
		// tempAlign is a temporary copy of toAlign, where removes are execeuted
		List<List<WorkflowOutputAlignment>> tempAlign = new ArrayList<List<WorkflowOutputAlignment>>();
		
		if(tempAlign.addAll(toAlign)) {
			System.out.println("addAll successful!");
			tempAlign = removeFlags(tempAlign);
		}			
		else {
			System.out.println("addAll failed!");
			return toAlign;
		}
		
		// check if primary index is within range
		if(primary >= 0 & primary < tempAlign.get(0).size()) {
			this.alignCols = new ArrayList<String>();

			// first add primary column as left-most column
			alignCols.add(outputCols.get(primary));
			for (int i = 0; i < outputCols.size(); i++) {
				if(i != primary)		// add other output ports only if they are not primary column
					alignCols.add(outputCols.get(i));
			}
			// modify output column names to represent primary alignment column at the left hand side
//			if(primary != 0) {		
//				//this.outputCols.add(0, this.outputCols.remove(this.outputCols.indexOf(this.outputCols.get(primary))));
//				this.alignCols.add(0, this.alignCols.remove(this.alignCols.indexOf(this.alignCols.get(primary))));	
//			}
			for (int i = 0; i < alignCols.size(); i++) {
				System.out.println("alignCol = " + alignCols.get(i));
			}
			
			// remove primary list from list of lists, tempAlign now has one entry less inside its list
			// primList won't change, as it is the reference for the alignment
			//List<WorkflowOutputAlignment> primList = toAlign.remove(primary);
			List<WorkflowOutputAlignment> primList = new ArrayList<WorkflowOutputAlignment>();
			for (int i = 0; i < tempAlign.size(); i++) {
				// add chosen primary list elements to primList
				// remove/discard filled in objects
				if(tempAlign.get(i).get(primary).getCompound().equals(missing) & tempAlign.get(i).get(primary).getAlignDB().equals(missing)
						& tempAlign.get(i).get(primary).getScore() == 0.0)
					continue;
				
				primList.add(tempAlign.get(i).get(primary));

				List<WorkflowOutputAlignment> tempList = new ArrayList<WorkflowOutputAlignment>();
				tempList.add(tempAlign.get(i).get(primary));
				
				// add element from primary list to first positions inside each new alignment list 
				newAlign.add(i, tempList);
				
				// remove primary elements 
				// TODO remove durch get ersetzen und unten in for schleife nur compound pruefen wenn index != primary
				//tempAlign.get(i).remove(primary);
//				for (int j = 0; j < tempAlign.get(i).size(); j++) {
//					System.out.println("remaining in tempAlign["+i+"] " + tempAlign.get(i).get(j).getCompound() + " " + tempAlign.get(i).get(j).getPortName());
//				}
			}
			
			// primList is empty -> cancel alignment and return unmodified list
			if(primList.size() == 0) { 		// || newAlign.indexOf(primList) < 0) {
				newAlign = new ArrayList<List<WorkflowOutputAlignment>>();
				
				for (int i = 0; i < toAlign.size(); i++) {
					List<WorkflowOutputAlignment> list = new ArrayList<WorkflowOutputAlignment>();
					for (int j = 0; j < toAlign.get(i).size(); j++) {
						if(j == primary) {
							list.add(0, toAlign.get(i).get(j));
						}
						else if(j != primary) {
							if(list.get(0) != null)
								list.add(toAlign.get(i).get(j));
							else list.add(list.size(), toAlign.get(i).get(j));
						}
					}
					newAlign.add(list);
				}
				
				return newAlign;
			}
			
			System.out.println("primlist # = " + primList.size());
			System.out.println("primList");
			for (int i = 0; i < primList.size(); i++) {
				System.out.println(primList.get(i).getCompound());
			}
				
			// add primary list (reference list) on first position
			// TODO check
			//newAlign.add(primList);
			System.out.println("position of primList = " + newAlign.indexOf(primList));
			System.out.println("newAlign size (20) = " + newAlign.size());
			
			// fuer jeden eintrag in primList schauen, ob id vorhanden die zu db passt
			// dann entsprechende id in allen anderen listen nachschauen und auf gleiche Stelle alignen
			// oder mit --- auffuellen
			// TODO derzeit nur left outer join, nicht aber full outer join
			/**
			 * wenn kein passender compound/id gefunden wurde, entsprechende stelle mit naechstbestem score/compound
			 * auffuellen (aus anderem output port)
			 */
			// TODO Struktur der for Schleifen pruefen -> redundant?
			for (WorkflowOutputAlignment woa : primList) {
				boolean hit = false;				// change to true if primlist id was found inside the remaining ports
				//System.out.println("index of woa = " + primList.indexOf(woa));
				
				if(woa.getDb().equals(db) || (!woa.getDbLinks().isEmpty() && woa.getDbLinks().get(db) != null)) {		// current db inside WorkflowOutputAlignment equals parameter db
					
					for (int i = 0; i < tempAlign.size(); i++) {
						for (WorkflowOutputAlignment woaSec : tempAlign.get(i)) {
							//System.out.println("index of woaSec = " + tempAlign.get(i).indexOf(woaSec));		// && !woa.getDbLinks().equals(null)
							if((tempAlign.get(i).indexOf(woaSec) != primary) && !woaSec.getDbLinks().isEmpty() 
									&& !woaSec.getDbLinks().containsKey(noID) && woaSec.getDbLinks().containsKey(db) 
									&& (woaSec.getDbLinks().containsValue(woa.getDbLinks().get(db)) )) {
								
								//newAlign.get(0).add(primList.indexOf(woa), woaSec);		// use i+1 as position 0 is granted to primList
								newAlign.get(primList.indexOf(woa)).add(woaSec);
								woa.addHit(woaSec);										// add current woaSec to the list of alignment hits from woa
								//break;
								hit = true;
								woa.setHasHits(hit);			// current object (woa) has alignment hits
								woaSec.setHit(hit);
								woaSec.setAligned(hit);			// woaSec is aligned to woa, therefore has its position in the alignment list
							}
							// TODO else mit dummy object auffuellen oder ggf. objekte anderer ports dazwischen legen falls
							// deren score > woa.score
							else {
								
							}
//							System.out.println("if woa = " + woa.getCompound() + "  " + woa.getId());
//							System.out.println("if woaSec = " + woaSec.getCompound() + "  " + woaSec.getId());
						}
					}	
					
				}
				else {											// parameter db is not present inside the current WorkflowOutputAlignment
//					// TODO verhalten aendern -> wenn kein DB identifier, dann kein alignment !!! 
//					// -> insert entries with higher score than the current from the other lists into this position 
//					
//					for (int i = 0; i < tempAlign.size(); i++) {
//						for (WorkflowOutputAlignment woaSec : tempAlign.get(i)) {
//							if((tempAlign.get(i).indexOf(woaSec) != primary) && woaSec.getScore() > woa.getScore() & !woaSec.isHit()) {
//								newAlign.get(primList.indexOf(woa)).add(primary, woaSec);
//							}
//						}
//					}
					
					woa.setHasHits(false);
					
//					for (int i = 0; i < tempAlign.size(); i++) {
//						newAlign.get(0).add(primList.indexOf(woa), new WorkflowOutputAlignment());
//					}
					//newAlign.get(0).add(new WorkflowOutputAlignment(woa.getPortName()));
					
					//newAlign.get(primList.indexOf(woa)).add(new WorkflowOutputAlignment(woa.getPortName()));
					
					// TODO check proper portname - hier wird metfrag zu metfrag zugewiesen
					//woa.addHit(new WorkflowOutputAlignment(""));
					
					System.out.println("ELSE:");
					System.out.println("woa = " + woa.getCompound() + "  " + woa.getId());
//					newAlign.get(i).add(woaSec);
//					primList.indexOf(woa);
					
				}
				
				// no hit, add alignment insert (dummy WorkflowOutputAlignment()) to this position
				// do a full outer join
//				if(!hit) {
//					newAlign.get(primList.indexOf(woa)).add(new WorkflowOutputAlignment(missing));
//				}
				
				// TODO verhalten aendern -> wenn kein DB identifier, dann kein alignment !!! 
				// -> insert entries with higher score than the current from the other lists into this position 
				
				// TODO check!!!
				//for (int i = 0; i < tempAlign.size(); i++) {
//				for (WorkflowOutputAlignment woaSec : tempAlign.get(primList.indexOf(woa))) {
//					if((tempAlign.get(primList.indexOf(woa)).indexOf(woaSec) != primary) && (woaSec.getScore() > woa.getScore() & !woaSec.isHit())) {
//						List<WorkflowOutputAlignment> temp = new ArrayList<WorkflowOutputAlignment>();
//						
//						// TODO needs check -> what about 3 or more ports -> empty objects will overwrite useful ones !!!
//						temp.add(new WorkflowOutputAlignment(missing));
//						temp.add(woaSec);
//						
//						newAlign.add(primList.indexOf(woa), temp);
//						
//						//newAlign.get(primList.indexOf(woa)).add(primary, woaSec);
//						//newAlign.get(primList.indexOf(woa)).add(primary, new WorkflowOutputAlignment(missing));				
//					}
//				}
				//}
			}
			
//			for (int j = 0; j < tempAlign.size(); j++) {	// alle temp scores pruefen mit primScore und ggf. einfuegen
//				for (int j2 = 0; j2 < tempAlign.get(j).size(); j2++) {
//					if(tempAlign.get(j).get(j2).getCompound().equals(missing) & tempAlign.get(j).get(j2).getAlignDB().equals(missing)
//							& tempAlign.get(j).get(j2).getScore() == 0.0)
//						System.out.println("bool remove = " + tempAlign.get(j).remove(tempAlign.get(j).get(j2)));
//				}
//			}
			
			// new try for alignment
			List<List<WorkflowOutputAlignment>> newTry = new ArrayList<List<WorkflowOutputAlignment>>();
			// TODO check index spruenge
			outer:
			for (int i = 0; i < primList.size(); i++) {
				double primScore = primList.get(i).getScore();
				double primNextScore = -1;
				if(i < primList.size()-1)
					primNextScore = primList.get(i+1).getScore();
							
				if(primList.get(i).isHasHits()) {			// hat alignment treffer
					List<WorkflowOutputAlignment> temp = new ArrayList<WorkflowOutputAlignment>();
					
					primList.get(i).setAligned(true);
					temp.add(primList.get(i));

					// add alignment hits to current row
					for (int j = 0; j < primList.get(i).getAlignHits().size(); j++) {
						primList.get(i).getAlignHits().get(j).setAligned(true);
						temp.add(primList.get(i).getAlignHits().get(j));
					}
					
					// set proper number for output columns
					if(temp.size() > outputCols.size()) {
						//alignCols = new ArrayList<String>();
						for (int j = 0; j < temp.size(); j++) {
							if(j < outputCols.size())
								;
							//	alignCols.add(outputCols.get(j));
							else alignCols.add("Additional Alignment Hit");
						}
					}
					
					boolean oncePrim = false;
					hit:
					for (int j = 0; j < tempAlign.size(); j++) {	// alle temp scores pruefen mit primScore und ggf. einfuegen
						for (int j2 = 0; j2 < tempAlign.get(j).size(); j2++) {
							
							if(primary != j2 && !tempAlign.get(j).get(j2).isAligned() 
									&& !tempAlign.get(j).get(j2).isHit() && primScore > tempAlign.get(j).get(j2).getScore() && !oncePrim) {
								
								newTry.add(temp);
								oncePrim = true;
							}
							else if(primary != j2 && !tempAlign.get(j).get(j2).isAligned() && tempAlign.get(j).get(j2).isRendered()
									&& !tempAlign.get(j).get(j2).isHit() && primScore < tempAlign.get(j).get(j2).getScore() && !oncePrim) {
								
								List<WorkflowOutputAlignment> temp1 = new ArrayList<WorkflowOutputAlignment>();
								tempAlign.get(j).get(j2).setAligned(true);
								
								for (int k = 0; k < tempAlign.get(j).size(); k++) {
									if(k == alignCols.indexOf(tempAlign.get(j).get(j2).getPortName())) {
										temp1.add(tempAlign.get(j).get(j2));
									}											
//									if(k == j2)
//										temp1.add(tempAlign.get(j).get(j2));
									else temp1.add(new WorkflowOutputAlignment(missing));
								}	
								
								newTry.add(temp1);
							}
							else if(oncePrim)
								break hit;
						}
					}
					
					//newTry.add(temp);
				}
				else {										// hat keine alignment treffer
					if(primScore == primNextScore) {		// beide aufeinanderfolgenden prim eintraege haben gleichen score -> reihenfolge lassen
						List<WorkflowOutputAlignment> tempPrim = new ArrayList<WorkflowOutputAlignment>();
						primList.get(i).setAligned(true);
						tempPrim.add(primList.get(i));
						for (int j = 1; j < tempAlign.get(j).size(); j++) {
							tempPrim.add(new WorkflowOutputAlignment(missing));
						}
						newTry.add(tempPrim);
						
						List<WorkflowOutputAlignment> temp = new ArrayList<WorkflowOutputAlignment>();
						primList.get(i+1).setAligned(true);
						
						if(primList.get(i+1).isHasHits()) {	// prufen ob naechste prim object alignment treffer hat
							temp.add(primList.get(i+1));
							for (int j = 0; j < primList.get(i+1).getAlignHits().size(); j++) {		
								primList.get(i+1).getAlignHits().get(j).setAligned(true);
								temp.add(primList.get(i+1).getAlignHits().get(j));
							}
						}
						else {								// naechstes prim object hat keine treffer
							temp.add(primList.get(i+1));
							for (int j = 1; j < tempAlign.get(j).size(); j++) {						// j=1 da prim an position 0
								temp.add(new WorkflowOutputAlignment(missing));
							}
						}						
						newTry.add(temp);
						
						i++;								// schleifenvariable eins hoch setzen da naechstes objekt bereits behandelt
					}
					else if(primScore > primNextScore) {	// naechstes prim objekt hat kleineren score -> score von temp pruefen
						// prim erst setzen wenn score überprueft !!!
//						List<WorkflowOutputAlignment> tempPrim = new ArrayList<WorkflowOutputAlignment>();
//						primList.get(i).setAligned(true);
//						tempPrim.add(primList.get(i));
//						for (int j = 1; j < tempAlign.get(j).size(); j++) {
//							tempPrim.add(new WorkflowOutputAlignment(missing));
//						}
//						newTry.add(tempPrim);
						boolean oncePrim = false;
						
						out:
						for (int j = 0; j < tempAlign.size(); j++) {	// alle temp scores pruefen mit primScore und ggf. einfuegen
							for (int j2 = 0; j2 < tempAlign.get(j).size(); j2++) {
								// prim erst setzen wenn score überprueft !!!
								if(primary != j2 && !tempAlign.get(j).get(j2).isAligned() && tempAlign.get(j).get(j2).isRendered()
										&& !tempAlign.get(j).get(j2).isHit() && primScore > tempAlign.get(j).get(j2).getScore() && !oncePrim) {
										//&& primNextScore < tempAlign.get(j).get(j2).getScore()) {
									
									List<WorkflowOutputAlignment> tempPrim = new ArrayList<WorkflowOutputAlignment>();
									primList.get(i).setAligned(true);
									tempPrim.add(primList.get(i));
									for (int k = 1; k < tempAlign.get(k).size(); k++) {
										tempPrim.add(new WorkflowOutputAlignment(missing));
									}
									
									newTry.add(tempPrim);
									oncePrim = true;
								}
								else if(primary != j2 && !tempAlign.get(j).get(j2).isAligned() && tempAlign.get(j).get(j2).isRendered()
										&& !tempAlign.get(j).get(j2).isHit() && primScore < tempAlign.get(j).get(j2).getScore() && !oncePrim) { 
										//&& primNextScore < tempAlign.get(j).get(j2).getScore()) {
									
									List<WorkflowOutputAlignment> temp = new ArrayList<WorkflowOutputAlignment>();
									tempAlign.get(j).get(j2).setAligned(true);
									
									for (int k = 0; k < tempAlign.get(j).size(); k++) {
//										if(k == j2)
										if(k == alignCols.indexOf(tempAlign.get(j).get(j2).getPortName())) {
											temp.add(tempAlign.get(j).get(j2));
										}											
										else temp.add(new WorkflowOutputAlignment(missing));
									}	
									
									newTry.add(temp);
								}								
								// tempAlign ist kein primary object, ist nicht aligned,
								// ist kein hit eines anderen objects und score ist groeßer als vom naechsten prim object
								// TODO changed from if -> else if - CHECK !!!
								if(primary != j2 && !tempAlign.get(j).get(j2).isAligned() && tempAlign.get(j).get(j2).isRendered()
										&& !tempAlign.get(j).get(j2).isHit() && primNextScore < tempAlign.get(j).get(j2).getScore()) {
									
									List<WorkflowOutputAlignment> temp = new ArrayList<WorkflowOutputAlignment>();
									tempAlign.get(j).get(j2).setAligned(true);
									
									for (int k = 0; k < tempAlign.get(j).size(); k++) {
										//if(k == j2)
										if(k == alignCols.indexOf(tempAlign.get(j).get(j2).getPortName()))
											temp.add(tempAlign.get(j).get(j2));
										else temp.add(new WorkflowOutputAlignment(missing));
									}
									
									newTry.add(temp);
								}
								// naechster prim score ist groesser als aktueller temp score -> break
								else if(primary != j2 && !tempAlign.get(j).get(j2).isHit() && primNextScore > tempAlign.get(j).get(j2).getScore()) {
									// naechste prim object anfuegen (da wieder hoeherer score als temp) und uberspringen
									if(primList.get(i+1).isHasHits()) {			// hat alignment treffer
										List<WorkflowOutputAlignment> temp = new ArrayList<WorkflowOutputAlignment>();
										
										primList.get(i+1).setAligned(true);
										temp.add(primList.get(i+1));
										for (int j1 = 0; j1 < primList.get(i+1).getAlignHits().size(); j1++) {
											primList.get(i+1).getAlignHits().get(j1).setAligned(true);
											temp.add(primList.get(i+1).getAlignHits().get(j1));
										}
										
										//newTry.add(i+1, temp);					// liste mit alignment treffern fuellen
										newTry.add(temp);	
									}
									else {
										List<WorkflowOutputAlignment> temp = new ArrayList<WorkflowOutputAlignment>();
										
										primList.get(i+1).setAligned(true);
										temp.add(primList.get(i+1));
										for (int j1 = 1; j1 < tempAlign.get(j).size(); j1++) {	// j=1 da prim an position 0
											temp.add(new WorkflowOutputAlignment(missing));
										}
										
										//newTry.add(i+1, temp);					// liste mit dummys fuellen
										newTry.add(temp);
									}
									
									i++;
									break out;				// da primnext score > temp score -> in prim schleife weitermachen
								}
								
								// alle elemente aus temp durchgegangen und aligniert, aber noch reste in prim
								// und aktuelles prim noch nicht zugefuegt (also auch folgendes prim nicht hinzugefuegt)
								// alle verbleibenden aus prim zur alignment liste hinzuguegen und schleife abbrechen
								if((j == (tempAlign.size() -1)) && (j2 == (tempAlign.get(j).size() - 1)) && !oncePrim) {
									for (int k = i; k < primList.size(); k++) {
										if(primList.get(k).isHasHits()) {			// hat alignment treffer
											List<WorkflowOutputAlignment> temp = new ArrayList<WorkflowOutputAlignment>();
											
											primList.get(k).setAligned(true);
											temp.add(primList.get(k));
											for (int l = 0; l < primList.get(k).getAlignHits().size(); l++) {
												primList.get(k).getAlignHits().get(l).setAligned(true);
												temp.add(primList.get(k).getAlignHits().get(l));
											}
											newTry.add(temp);
										}
										else {
											List<WorkflowOutputAlignment> temp = new ArrayList<WorkflowOutputAlignment>();
											
											primList.get(k).setAligned(true);
											temp.add(primList.get(k));
											for (int j1 = 1; j1 < tempAlign.get(k).size(); j1++) {	// j=1 da prim an position 0
												temp.add(new WorkflowOutputAlignment(missing));
											}
											newTry.add(temp);
										}
									}
									
									break outer;		// alignment beenden
								}
							}
						}
					}					
				}
				
				// wenn i an letzter position ist -> pruefen ob in temp noch nich alignierte objekte sind und diese anfuegen
				if(i == (primList.size() - 1)) {
					
					for (int j = 0; j < tempAlign.size(); j++) {	// alle temp scores pruefen mit primScore und ggf. einfuegen
						for (int j2 = 0; j2 < tempAlign.get(j).size(); j2++) {
							if(primary != j2 && !tempAlign.get(j).get(j2).isAligned() 
									&& tempAlign.get(j).get(j2).isRendered() && primScore > tempAlign.get(j).get(j2).getScore()) {
								List<WorkflowOutputAlignment> temp = new ArrayList<WorkflowOutputAlignment>();
								
								for (int k = 0; k < tempAlign.get(j).size(); k++) {
									if(k == j2)
										temp.add(tempAlign.get(j).get(j2));
									else temp.add(new WorkflowOutputAlignment(missing));
								}
								
								newTry.add(temp);
							}
						}
					}
				}
			}
					
			System.out.println("##############");
			System.out.println("### newTry ###");
			System.out.println("##############");
			for (int i = 0; i < newTry.size(); i++) {
				for (int j = 0; j < newTry.get(i).size(); j++) {
					System.out.println("i="+i+"  j="+j+"    " + newTry.get(i).get(j).getCompound() + "  " + newTry.get(i).get(j).getScore());
				}
				System.out.println();
			}
			
			// assign new alignment to field modAlign
			//this.setModAlign(newAlign);
			this.setModAlign(newTry);
			
			for (int i = 0; i < outputCols.size(); i++) {
				System.out.println(outputCols.get(i));
			}
			
			// set correct width of alignment output columns
			int w = 100 / alignCols.size();
			this.alignColWidth = String.valueOf(w) + "%";
			
			return newTry;
		}
		else {
			this.setModAlign(null);
			return null;
		}
	}

	
	public List<List<WorkflowOutputAlignment>> alignToMol(List<List<WorkflowOutputAlignment>> toAlign, int primary) {
		System.out.println("started alignToMol");
		
		this.alignCols = new ArrayList<String>();

		if(primary >= 0 & primary < toAlign.get(0).size()) {
			
			// first add primary column as left-most column
			alignCols.add(outputCols.get(primary));
			for (int i = 0; i < outputCols.size(); i++) {
				if(i != primary)		// add other output ports only if they are not primary column
					alignCols.add(outputCols.get(i));
			}
			// modify output column names to represent primary alignment column at the left hand side
//			if(primary != 0) {		
//				//this.outputCols.add(0, this.outputCols.remove(this.outputCols.indexOf(this.outputCols.get(primary))));
//				this.alignCols.add(0, this.alignCols.remove(this.alignCols.indexOf(this.alignCols.get(primary))));	
//			}
			for (int i = 0; i < alignCols.size(); i++) {
				System.out.println("alignCol = " + alignCols.get(i));
			}
		}
		else {
			System.out.println("Index of primary [" + primary + "] not in bounds.");
			return toAlign;
		}
		
		// new list of lists for alignment
		List<List<WorkflowOutputAlignment>> newAlign = new ArrayList<List<WorkflowOutputAlignment>>();
		
		// fill map with all structures
		Map<WorkflowOutputAlignment, IAtomContainer> testMap = new HashMap<WorkflowOutputAlignment, IAtomContainer>();
		// list of candidates, use their ID -> better use combination of name and id ???
		List<WorkflowOutputAlignment> cands = new ArrayList<WorkflowOutputAlignment>();
		
		Map<String, IAtomContainer> stringMap = new HashMap<String, IAtomContainer>();
		List<String> stringCands = new ArrayList<String>();
		
		for (Iterator<List<WorkflowOutputAlignment>> iterator = toAlign.iterator(); iterator.hasNext();) {
			List<WorkflowOutputAlignment> list = iterator.next();
			
//			for (int i = 0; i < list.size(); i++) {
//				if(i != primary && list.get(i) != null && list.get(i).getContainer() != null)
//					//cands.add(list.get(i));
//					//stringCands.add(list.get(i).getPortName() + list.get(i).getRecord());
//					if(!list.get(i).getId().equals(missing))
//						stringCands.add(list.get(i).getId());
//			}
//			
//			if(list.get(primary) == null)
//				continue;
//			
//			// only add elements from primary list with moldata to map
//			if(list.get(primary).getContainer() != null) {
//				//testMap.put(list.get(primary), list.get(primary).getContainer());
//				//stringMap.put(list.get(primary).getPortName() + list.get(primary).getRecord(), list.get(primary).getContainer());
//				stringMap.put(list.get(primary).getId(), list.get(primary).getContainer());
//			}
			
			for (int i = 0; i < list.size(); i++) {
				if(list.get(i).getContainer() != null && !list.get(i).getId().equals(missing)) {
					cands.add(list.get(i));
					testMap.put(list.get(i), list.get(i).getContainer());
				}
			}
			
//			for (int i = 0; i < list.size(); i++) {
//				if(list.get(i).getContainer() != null && !list.get(i).getId().equals(missing)) {
//					stringCands.add(list.get(i).getId());
//					stringMap.put(list.get(i).getId(), list.get(primary).getContainer());
//				}
//			}
		}

		for(WorkflowOutputAlignment woa : cands)
			System.out.println("cands ->  " + woa.getId() + "  " + woa.getCompound() + "  " + woa.getRecord());
		
		for(WorkflowOutputAlignment woa : testMap.keySet())
			System.out.println("testMap ->   "  + woa.getId() + "  " + woa.getCompound() + "  " + woa.getRecord());
		
//		for(String cand : stringCands)
//			System.out.println("stringCand -> " + cand);
//		
//		for (Iterator<String> iterator = stringMap.keySet().iterator(); iterator.hasNext();) {
//			String string = (String) iterator.next();
//			System.out.println("stringMap -> " + string);
//		}
		
		// run similarity check
		try {
			similarity = new SimilarityWorkflowOutput(testMap, 0.95f);
			//Similarity sim = new Similarity(stringMap, 0.90f);
			
			groupedCandidates = similarity.getTanimotoDistanceList(cands);
			
			for (SimilarityGroupWorkflowOutput sg : groupedCandidates) {
				System.out.println("\ncandidate to compare -> " + sg.getCandidateTocompare().getCompound() + 
						" [" + sg.getCandidateTocompare().getId() + "]  " + sg.getCandidateTocompare().getRecord());
				for (WorkflowOutputAlignment woa : sg.getSimilarCandidatesWithBase()) {
					System.out.println("similar candidate -> " + woa.getCompound() + " [" + woa.getId() + "]  " + woa.getRecord());
				}
			}
			
			//List<SimilarityGroup> grouping = sim.getTanimotoDistanceList(stringCands);
			
//			for (SimilarityGroup similarityGroup : grouping) {
//				if(similarityGroup.getSimilarCompounds().size() == 0) {
//					System.out.print("Single: " + similarityGroup.getCandidateTocompare() + "\n");
//					
//					List<WorkflowOutputAlignment> group = new ArrayList<WorkflowOutputAlignment>();
//					for (Iterator<List<WorkflowOutputAlignment>> iterator = toAlign.iterator(); iterator.hasNext();) {
//						List<WorkflowOutputAlignment> list = iterator.next();
//						for (Iterator<WorkflowOutputAlignment> iterator2 = list.iterator(); iterator2.hasNext();) {
//							WorkflowOutputAlignment woa = iterator2.next();
//							if(woa.getContainer() != null && similarityGroup.getCandidateTocompare().equals(woa.getId())) {
//								group.add(woa);
//								group.add(new WorkflowOutputAlignment(missing));
//								newAlign.add(group);
//								break;
//							}
//						}
//					}
//				}
//				else
//				{
//					System.out.print("Group of " + similarityGroup.getSimilarCompounds().size() + " " + similarityGroup.getCandidateTocompare() +  ": ");
//					
//					boolean found = false;
//					List<WorkflowOutputAlignment> group = new ArrayList<WorkflowOutputAlignment>();
//					for (Iterator<List<WorkflowOutputAlignment>> iterator = toAlign.iterator(); iterator.hasNext();) {
//						List<WorkflowOutputAlignment> list = iterator.next();
//						for (Iterator<WorkflowOutputAlignment> iterator2 = list.iterator(); iterator2.hasNext();) {
//							WorkflowOutputAlignment woa = iterator2.next();
//							if(woa.getContainer() != null && similarityGroup.getCandidateTocompare().equals(woa.getId())) {
//								group.add(woa);
//								found = true;
////								newAlign.add(group);
//								break;
//							}
//						}
//						if(found)
//							break;
//					}
//					
//					found = false;
//					for (int i = 0; i < similarityGroup.getSimilarCompounds().size(); i++) {
//						System.out.print(similarityGroup.getSimilarCompounds().get(i) + "(" + similarityGroup.getSimilarCompoundsTanimoto().get(i) + ") ");
//						
//						for (Iterator<List<WorkflowOutputAlignment>> iterator = toAlign.iterator(); iterator.hasNext();) {
//							List<WorkflowOutputAlignment> list = iterator.next();
//							for (Iterator<WorkflowOutputAlignment> iterator2 = list.iterator(); iterator2.hasNext();) {
//								WorkflowOutputAlignment woa = iterator2.next();
//								if(woa.getContainer() != null && similarityGroup.getSimilarCompounds().get(i).equals(woa.getId())) {
//									group.add(woa);
//									found = true;
//									newAlign.add(group);
//									break;
//								}
//							}
//							if(found)
//								break;
//						}
//					}
//					System.out.println("");
//				}
//			}
			
//			System.out.println("newAlign -> getTanimotoAlignment");
//			newAlign = similarity.getTanimotoAlignment(toAlign, testMap, cands, primary);
			newAlign = new ArrayList<List<WorkflowOutputAlignment>>();
			
			for (int i = 0; i < newAlign.size(); i++) {
				for (int j = 0; j < newAlign.get(i).size(); j++) {
					if(newAlign.get(i) != null && newAlign.get(i).get(j) != null)
						System.out.println("i=" + i + " j= " + j + "   " + newAlign.get(i).get(j).getCompound() + "  "  + newAlign.get(i).get(j).getScore());
				}
				System.out.println();
				System.out.println();
			}
			
			for (SimilarityGroupWorkflowOutput similarityGroup : groupedCandidates) {
				if(similarityGroup.getSimilarCompounds().size() == 0) {
					System.out.print("Single: " + similarityGroup.getCandidateTocompare().getId() + "\n");
					
					List<WorkflowOutputAlignment> group = new ArrayList<WorkflowOutputAlignment>();
					for (Iterator<List<WorkflowOutputAlignment>> iterator = toAlign.iterator(); iterator.hasNext();) {
						List<WorkflowOutputAlignment> list = iterator.next();
						for (Iterator<WorkflowOutputAlignment> iterator2 = list.iterator(); iterator2.hasNext();) {
							WorkflowOutputAlignment woa = iterator2.next();
							if(woa.getContainer() != null && similarityGroup.getCandidateTocompare().equals(woa)) {
								group.add(woa);
								group.add(new WorkflowOutputAlignment(missing));
								newAlign.add(group);
								break;
							}
						}
					}
					
//					List<WorkflowOutputAlignment> group = new ArrayList<WorkflowOutputAlignment>();
//					for (Iterator<List<WorkflowOutputAlignment>> iterator = toAlign.iterator(); iterator.hasNext();) {
//						List<WorkflowOutputAlignment> list = iterator.next();
//						if(list.get(primary) != null && list.get(primary).getId().equals(similarityGroup.getCandidateTocompare())) {
//							group.add(0, list.get(primary));
//							newAlign.add(group);
//							break;
//						}
//					}
				}
				else
				{
					System.out.print("Group of " + similarityGroup.getSimilarCompounds().size() + " " + similarityGroup.getCandidateTocompare().getId() +  ": ");
					
					boolean found = false;
					List<WorkflowOutputAlignment> group = new ArrayList<WorkflowOutputAlignment>();
					for (Iterator<List<WorkflowOutputAlignment>> iterator = toAlign.iterator(); iterator.hasNext();) {
						List<WorkflowOutputAlignment> list = iterator.next();
						for (Iterator<WorkflowOutputAlignment> iterator2 = list.iterator(); iterator2.hasNext();) {
							WorkflowOutputAlignment woa = iterator2.next();
							if(woa.getContainer() != null && similarityGroup.getCandidateTocompare().equals(woa)) {
								group.add(woa);
								found = true;
//								newAlign.add(group);
								break;
							}
						}
						if(found)
							break;
					}
					
					found = false;
					for (int i = 0; i < similarityGroup.getSimilarCompounds().size(); i++) {
						System.out.print(similarityGroup.getSimilarCompounds().get(i).getId() + "(" + similarityGroup.getSimilarCompoundsTanimoto().get(i) + ") ");
						
						for (Iterator<List<WorkflowOutputAlignment>> iterator = toAlign.iterator(); iterator.hasNext();) {
							List<WorkflowOutputAlignment> list = iterator.next();
							for (Iterator<WorkflowOutputAlignment> iterator2 = list.iterator(); iterator2.hasNext();) {
								WorkflowOutputAlignment woa = iterator2.next();
								if(woa.getContainer() != null && similarityGroup.getSimilarCompounds().get(i).equals(woa)) {
									group.add(woa);
									found = true;
									newAlign.add(group);
									break;
								}
							}
							if(found)
								break;
						}
					}
					System.out.println("");
					
//					List<WorkflowOutputAlignment> group = new ArrayList<WorkflowOutputAlignment>();
//					for (Iterator<List<WorkflowOutputAlignment>> iterator = toAlign.iterator(); iterator.hasNext();) {
//						List<WorkflowOutputAlignment> list = iterator.next();
//						if(list.get(primary) != null && list.get(primary).getId().equals(similarityGroup.getCandidateTocompare())) {
//							group.add(0, list.get(primary));
//							newAlign.add(group);
//							break;
//						}
//					}
					
					
					for (int i = 0; i < similarityGroup.getSimilarCompounds().size(); i++) {
						System.out.print(similarityGroup.getSimilarCompounds().get(i).getId() + "(" + similarityGroup.getSimilarCompoundsTanimoto().get(i) + ") ");
					}
					System.out.println("");
				}
			}
		} catch (CDKException e) {
			e.printStackTrace();
		}
		System.out.println("end of alignToMol");		
		
		if(newAlign == null || newAlign.size() == 0) {
			System.out.println("newAlign == null -> return toAlign");
			return toAlign;
		}
		else {
			System.out.println("newAlign != null -> return newAlign");
			return newAlign;
		}
	}
	
	/**
	 * This methods sets all flags which are used for alignment to false, i.e. setting them to their defaults before alignment.
	 * 
	 * @param list list of lists containing WorkflowOutputAlignment objects, which flags should be removed (set to false)
	 * @return
	 */
	public List<List<WorkflowOutputAlignment>> removeFlags(List<List<WorkflowOutputAlignment>> list) {
		for (int i = 0; i < list.size(); i++) {
			for (int j = 0; j < list.get(i).size(); j++) {
				list.get(i).get(j).setAligned(false);
				list.get(i).get(j).setHit(false);
				list.get(i).get(j).setHasHits(false);
				list.get(i).get(j).setAlignHits(new ArrayList<WorkflowOutputAlignment>());
			}
		}
		return list;
	}
	
	/**
	 * Align to score.
	 */
	public void alignToScore() {

	}


	/**
	 * Gets the bytes.
	 * 
	 * @param obj the obj
	 * 
	 * @return the bytes
	 */
	public byte[] getBytes(Dataflow obj) {
		System.out.println(obj.getLocalName() + " getBytes...");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			oos.close();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] data = bos.toByteArray();
		System.out.println("bytes[] length = " + data.length);
		return data;
	}


	/**
	 * Prints the.
	 */
	public void print() {
		System.out.println("URI base -> " + this.getBase());
		System.out.println("URI workflow path -> " + this.getUWorkflow());
		System.out.println("URI image path -> " + this.getUImage());
		System.out.println("String workflow -> " + this.getSURL());
		System.out.println("String image -> " + this.getSImage());
		if (workflow != null)
			System.out.println("Workflow -> "
					+ this.getWorkflow().getLocalName());
	}

	
	private void writeMol(String id, String mol) throws IOException {
		File temp = new File(id + ".mol");
		FileWriter fw = new FileWriter(temp);
		BufferedWriter bw = new BufferedWriter(fw);
		
		bw.write(mol);
		bw.flush();
		bw.close();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		// System.out.println("URI base -> " + this.getBase());
		// System.out.println("URI workflow path -> " + this.getUWorkflow());
		// System.out.println("URI image path -> " + this.getUImage());
		// System.out.println("String workflow -> " + this.getSURL());
		// System.out.println("String image -> " + this.getSImage());
		// if(workflow != null)
		// System.out.println("Workflow -> " +
		// this.getWorkflow().getLocalName());
		if (this.getWorkflow() != null)
			return this.getBase() + " " + this.getWorkflow().getLocalName();
		else
			return this.getBase() + " workflow is null!";
	}


	// /**
	// * Save an object.
	// */
	// public void save_object(Serializable object, String filename) throws
	// IOException {
	// ObjectOutputStream objstream = new ObjectOutputStream(new
	// FileOutputStream(filename));
	// objstream.writeObject(object);
	// objstream.flush();
	// objstream.close();
	// }
	// 
	// /**
	// * Load an object.
	// */
	// public Object load_object(String filename) throws Exception {
	// ObjectInputStream objstream = new ObjectInputStream(new
	// FileInputStream(filename));
	// Object object = objstream.readObject();
	// objstream.close();
	// return object;
	// }

	/**
	 * Sets the base.
	 * 
	 * @param base the new base
	 */
	public void setBase(URI base) {
		this.base = base;
	}


	/**
	 * Gets the base.
	 * 
	 * @return the base
	 */
	public URI getBase() {
		return base;
	}


	/**
	 * Sets the sURL.
	 * 
	 * @param sURL the new sURL
	 */
	public void setSURL(String sURL) {
		this.sURL = sURL;
	}


	/**
	 * Gets the sURL.
	 * 
	 * @return the sURL
	 */
	public String getSURL() {
		return sURL;
	}


	/**
	 * Sets the s image.
	 * 
	 * @param sImage the new s image
	 */
	public void setSImage(String sImage) {
		this.sImage = sImage;
	}


	/**
	 * Gets the s image.
	 * 
	 * @return the s image
	 */
	public String getSImage() {
		return sImage;
	}


	/**
	 * Sets the u file.
	 * 
	 * @param uFile the new u file
	 */
	public void setUWorkflow(URI uFile) {
		this.uWorkflow = uFile;
	}


	/**
	 * Gets the u file.
	 * 
	 * @return the u file
	 */
	public URI getUWorkflow() {
		return uWorkflow;
	}


	/**
	 * Sets the u image.
	 * 
	 * @param uImage the new u image
	 */
	public void setUImage(URI uImage) {
		this.uImage = uImage;
	}


	/**
	 * Gets the u image.
	 * 
	 * @return the u image
	 */
	public URI getUImage() {
		return uImage;
	}


	/**
	 * Sets the input ports.
	 * 
	 * @param list the new input ports
	 */
	public void setInputPorts(List<? extends DataflowInputPort> list) {
		this.inputPorts = list;
	}


	/**
	 * Gets the input ports.
	 * 
	 * @return the input ports
	 */
	public List<? extends DataflowInputPort> getInputPorts() {
		return inputPorts;
	}


	/**
	 * Sets the workflow.
	 * 
	 * @param workflow the new workflow
	 */
	public void setWorkflow(Dataflow workflow) {
		this.workflow = workflow;
	}


	/**
	 * Gets the workflow.
	 * 
	 * @return the workflow
	 */
	public Dataflow getWorkflow() {
		return workflow;
	}


	/**
	 * Sets the inputs.
	 * 
	 * @param inputs the new inputs
	 */
	public void setInputs(ArrayList<WorkflowInput> inputs) {
		this.inputs = inputs;
	}


	/**
	 * Gets the inputs.
	 * 
	 * @return the inputs
	 */
	public List<WorkflowInput> getInputs() {
		return inputs;
	}


	/**
	 * Sets the result ref.
	 * 
	 * @param resultRef the result ref
	 */
	public void setResultRef(Map<String, T2Reference> resultRef) {
		this.resultRef = resultRef;
	}


	/**
	 * Gets the result ref.
	 * 
	 * @return the result ref
	 */
	public Map<String, T2Reference> getResultRef() {
		return resultRef;
	}


	/**
	 * Sets the result keys.
	 * 
	 * @param resultKeys the new result keys
	 */
	public void setResultKeys(List<String> resultKeys) {
		this.resultKeys = resultKeys;
	}


	/**
	 * Gets the result keys.
	 * 
	 * @return the result keys
	 */
	public List<String> getResultKeys() {
		return resultKeys;
	}


	/**
	 * Sets the outputs.
	 * 
	 * @param outputs the new outputs
	 */
	public void setOutputs(ArrayList<WorkflowOutput> outputs) {
		this.outputs = outputs;
	}


	/**
	 * Gets the outputs.
	 * 
	 * @return the outputs
	 */
	public List<WorkflowOutput> getOutputs() {
		return outputs;
	}


	/**
	 * Sets the output ports.
	 * 
	 * @param outputPorts the new output ports
	 */
	public void setOutputPorts(List<? extends DataflowOutputPort> outputPorts) {
		this.outputPorts = outputPorts;
	}


	/**
	 * Gets the output ports.
	 * 
	 * @return the output ports
	 */
	public List<? extends DataflowOutputPort> getOutputPorts() {
		return outputPorts;
	}


	/**
	 * Sets the num col in.
	 * 
	 * @param numColIn the new num col in
	 */
	public void setNumColIn(int numColIn) {
		this.numColIn = numColIn;
	}


	/**
	 * Gets the num col in.
	 * 
	 * @return the num col in
	 */
	public int getNumColIn() {
		return numColIn;
	}


	/**
	 * Sets the num col out.
	 * 
	 * @param numColOut the new num col out
	 */
	public void setNumColOut(int numColOut) {
		this.numColOut = numColOut;
	}


	/**
	 * Gets the num col out.
	 * 
	 * @return the num col out
	 */
	public int getNumColOut() {
		return numColOut;
	}


	/**
	 * Sets the app path.
	 * 
	 * @param appPath the new app path
	 */
	public void setAppPath(String appPath) {
		this.appPath = appPath;
	}


	/**
	 * Gets the app path.
	 * 
	 * @return the app path
	 */
	public String getAppPath() {
		return appPath;
	}


	/**
	 * Sets the contains biomoby.
	 * 
	 * @param containsBiomoby the new contains biomoby
	 */
	public void setContainsBiomoby(boolean containsBiomoby) {
		this.containsBiomoby = containsBiomoby;
	}


	/**
	 * Checks if is contains biomoby.
	 * 
	 * @return true, if is contains biomoby
	 */
	public boolean isContainsBiomoby() {
		return containsBiomoby;
	}


	/**
	 * Sets the secondaries.
	 * 
	 * @param secondaries the secondaries
	 */
	public void setSecondaries(Map<String, String> secondaries) {
		this.secondaries = secondaries;
	}


	/**
	 * Gets the secondaries.
	 * 
	 * @return the secondaries
	 */
	public Map<String, String> getSecondaries() {
		return secondaries;
	}


	/**
	 * Gets the processors.
	 * 
	 * @return the processors
	 */
	public List<? extends Processor> getProcessors() {
		return processors;
	}


	/**
	 * Sets the processors.
	 * 
	 * @param processors the new processors
	 */
	public void setProcessors(List<? extends Processor> processors) {
		this.processors = processors;
	}


	/**
	 * Sets the biomoby secondaries.
	 * 
	 * @param biomobySecondaries the biomoby secondaries
	 */
	public void setBiomobySecondaries(
			Map<String, Map<String, String>> biomobySecondaries) {
		this.biomobySecondaries = biomobySecondaries;
	}


	/**
	 * Gets the biomoby secondaries.
	 * 
	 * @return the biomoby secondaries
	 */
	public Map<String, Map<String, String>> getBiomobySecondaries() {
		return biomobySecondaries;
	}


	/**
	 * Sets the sec.
	 * 
	 * @param sec the sec
	 */
	public void setSec(List<Map<String, String>> sec) {
		this.sec = sec;
	}


	/**
	 * Gets the sec.
	 * 
	 * @return the sec
	 */
	public List<Map<String, String>> getSec() {
		return sec;
	}


	/**
	 * Sets the entry.
	 * 
	 * @param entry the entry
	 */
	public void setEntry(Set<Entry<String, String>> entry) {
		this.entry = entry;
	}


	/**
	 * Gets the entry.
	 * 
	 * @return the entry
	 */
	public Set<Entry<String, String>> getEntry() {
		return entry;
	}


	/**
	 * Sets the service keys.
	 * 
	 * @param serviceKeys the new service keys
	 */
	public void setServiceKeys(List<String> serviceKeys) {
		this.serviceKeys = serviceKeys;
	}


	/**
	 * Gets the service keys.
	 * 
	 * @return the service keys
	 */
	public List<String> getServiceKeys() {
		return serviceKeys;
	}


	/**
	 * Sets the param keys.
	 * 
	 * @param paramKeys the new param keys
	 */
	public void setParamKeys(List<String> paramKeys) {
		this.paramKeys = paramKeys;
	}


	/**
	 * Gets the param keys.
	 * 
	 * @return the param keys
	 */
	public List<String> getParamKeys() {
		return paramKeys;
	}


	/**
	 * Sets the param values.
	 * 
	 * @param paramValues the new param values
	 */
	public void setParamValues(List<String> paramValues) {
		this.paramValues = paramValues;
	}


	/**
	 * Gets the param values.
	 * 
	 * @return the param values
	 */
	public List<String> getParamValues() {
		return paramValues;
	}


	/**
	 * Sets the params.
	 * 
	 * @param params the new params
	 */
	public void setParams(List<SecParam> params) {
		this.params = params;
	}


	/**
	 * Gets the params.
	 * 
	 * @return the params
	 */
	public List<SecParam> getParams() {
		return params;
	}


	/**
	 * Sets the peaks.
	 * 
	 * @param peaks the new peaks
	 */
	public void setPeaks(String peaks) {
		this.peaks = peaks;
	}


	/**
	 * Gets the peaks.
	 * 
	 * @return the peaks
	 */
	public String getPeaks() {
		return peaks;
	}


	/**
	 * Sets the image w.
	 * 
	 * @param imageW the new image w
	 */
	public void setImageW(String imageW) {
		this.imageW = imageW;
	}


	/**
	 * Gets the image w.
	 * 
	 * @return the image w
	 */
	public String getImageW() {
		return imageW;
	}


	/**
	 * Sets the image h.
	 * 
	 * @param imageH the new image h
	 */
	public void setImageH(String imageH) {
		this.imageH = imageH;
	}


	/**
	 * Gets the image h.
	 * 
	 * @return the image h
	 */
	public String getImageH() {
		return imageH;
	}


	/**
	 * Sets the num inputs.
	 * 
	 * @param numInputs the new num inputs
	 */
	public void setNumInputs(int numInputs) {
		this.numInputs = numInputs;
	}


	/**
	 * Gets the num inputs.
	 * 
	 * @return the num inputs
	 */
	public int getNumInputs() {
		return numInputs;
	}


	/**
	 * Sets the result index.
	 * 
	 * @param resultIndex the new result index
	 */
	public void setResultIndex(List<String> resultIndex) {
		this.resultIndex = resultIndex;
	}


	/**
	 * Gets the result index.
	 * 
	 * @return the result index
	 */
	public List<String> getResultIndex() {
		return resultIndex;
	}


	/**
	 * Sets the output cols.
	 * 
	 * @param outputCols the new output cols
	 */
	public void setOutputCols(List<String> outputCols) {
		this.outputCols = outputCols;
	}


	/**
	 * Gets the output cols.
	 * 
	 * @return the output cols
	 */
	public List<String> getOutputCols() {
		return outputCols;
	}


	/**
	 * Sets the align db.
	 * 
	 * @param alignDB the new align db
	 */
	public void setAlignDB(String alignDB) {
		this.alignDB = alignDB;
	}


	/**
	 * Gets the align db.
	 * 
	 * @return the align db
	 */
	public String getAlignDB() {
		return alignDB;
	}


	/**
	 * Sets the align.
	 * 
	 * @param align the new align
	 */
	public void setAlign(List<List<WorkflowOutputAlignment>> align) {
		this.align = align;
	}


	/**
	 * Gets the align.
	 * 
	 * @return the align
	 */
	public List<List<WorkflowOutputAlignment>> getAlign() {
		return align;
	}


	/**
	 * Gets the list si.
	 * 
	 * @return the list si
	 */
	public List<SelectItem> getListSI() {
		return listSI;
	}


	public void setModAlign(List<List<WorkflowOutputAlignment>> modAlign) {
		this.modAlign = modAlign;
	}


	public List<List<WorkflowOutputAlignment>> getModAlign() {
		return modAlign;
	}


	public void setNumOutputs(String numOutputs) {
		this.numOutputs = numOutputs;
	}


	public String getNumOutputs() {
		return numOutputs;
	}


	public void setAlignCol(List<SelectItem> alignCol) {
		this.alignCol = alignCol;
	}


	public List<SelectItem> getAlignCol() {
		return alignCol;
	}


	public void setPrimAlignCol(String primAlignCol) {
		this.primAlignCol = primAlignCol;
	}


	public String getPrimAlignCol() {
		return primAlignCol;
	}


	public void setAlignCols(List<String> alignCols) {
		this.alignCols = alignCols;
	}


	public List<String> getAlignCols() {
		return alignCols;
	}


	public void setAlignColWidth(String alignColWidth) {
		this.alignColWidth = alignColWidth;
	}


	public String getAlignColWidth() {
		return alignColWidth;
	}


	public void setOutputColWidth(String outputColWidth) {
		this.outputColWidth = outputColWidth;
	}


	public String getOutputColWidth() {
		return outputColWidth;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getName() {
		return name;
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
	
	public void setToAlign(boolean toAlign) {
		this.toAlign = toAlign;
	}


	public boolean isToAlign() {
		return toAlign;
	}

	public void setOutputResource(Resource outputResource) {
		this.outputResource = outputResource;
	}


	public Resource getOutputResource() {
		return outputResource;
	}

	public void setSimilarityAlign(String similarityAlign) {
		this.similarityAlign = similarityAlign;
	}


	public String getSimilarityAlign() {
		return similarityAlign;
	}

	public void setMetfragDB(String metfragDB) {
		this.metfragDB = metfragDB;
	}


	public String getMetfragDB() {
		return metfragDB;
	}

	public void setSimilarity(SimilarityWorkflowOutput similarity) {
		this.similarity = similarity;
	}


	public SimilarityWorkflowOutput getSimilarity() {
		return similarity;
	}

	public void setGroupedCandidates(List<SimilarityGroupWorkflowOutput> groupedCandidates) {
		this.groupedCandidates = groupedCandidates;
	}


	public List<SimilarityGroupWorkflowOutput> getGroupedCandidates() {
		return groupedCandidates;
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
