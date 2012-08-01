/**
 * created by Michael Gerlich, Aug 16, 2011 - 9:31:47 AM
 */ 

package de.ipbhalle.metfusion.web.controller;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import com.mysql.jdbc.Driver;

import de.ipbhalle.metfusion.wrapper.FeedbackEntry;
import de.ipbhalle.metfusion.wrapper.MailClient;


@ManagedBean
@SessionScoped
@FacesValidator("de.ipbhalle.metfusion.web.controller.FeedbackBean")
public class FeedbackBean implements Validator {

	public static final String sep = System.getProperty("file.separator");
	public static final String escape = "%BR%";
	
	/** feedback database storage settings*/
	private String database;
	private String user;
	private String password;
	private String jdbc;
	
	/** feedback email settings */
	private String mailServer;
	private String mailTo;
	private String mailFrom;
	
	/** feedback case */
	private String name;
	private String email;
	private String comment;
	private boolean allowStorage = false;
	
	private String error;
	private String note;
	
	private PropertiesBean props;
	private boolean renderNote;
	private boolean renderAdminNote;
	private List<FeedbackEntry> feedbackEntries;
	
	private boolean hitOnce = false;
	
	/** Feedback form controls */
    private boolean feedbackRendered = false;
    public void closeFeedbackPopup() { setFeedbackRendered(false);
	    this.name = "";
		this.email = "";
		this.comment = "";
		this.hitOnce = false;
		this.error = "";
		this.note = ""; 
		this.renderNote = false;
		FacesContext.getCurrentInstance().renderResponse(); }
    public void openFeedbackPopup() { setFeedbackRendered(true); 
		FacesContext.getCurrentInstance().renderResponse(); }
    
    /** Feedback admin form controls */
    private final String adminUser;						// default admin name from settings.properties
    private final String adminPass;						// default admin password from settings.properties
    private String providedName;						// name provided in admin login 
    private String providedPass;						// password provided in admin login
    private String loginError = "";						// error string for display in login window
    private String adminNote = "";
    private boolean feedbackAdminRendered = false;
    private boolean authorized;							// indicator for valid login
    public void closeFeedbackAdminPopup() {
//    	setFeedbackAdminRendered(false);
//    	setLoginError("");
//    	setAuthorized(false); 
//    	setProvidedName("");
//    	setProvidedPass("");
    	this.feedbackAdminRendered = false;
    	this.loginError = "";
    	this.authorized = false;
    	this.providedName = "";
    	this.providedPass = "";
    	FacesContext.getCurrentInstance().renderResponse(); }
    	//FacesContext.getCurrentInstance().responseComplete(); }
    public void openFeedbackAdminPopup() { setFeedbackAdminRendered(true); 
    	FacesContext.getCurrentInstance().renderResponse();	}

    
    public FeedbackBean() {
    	FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
		this.props = (PropertiesBean) ec.getApplicationMap().get("propertiesBean");
		this.adminUser = props.getProperty("adminUser");
		this.adminPass = props.getProperty("adminPass");
		this.authorized = false;
	}
	
    private boolean checkLogin() {
    	if(this.adminUser.equals(this.providedName) && this.adminPass.equals(this.providedPass))
    		return true;
    	else return false;
    }
    
	public void submit(ActionEvent event) {
		System.out.println("submit called");
		if(!this.allowStorage) {
			this.renderNote = false;
			this.error = "You have to allow storage of the current parameter settings in order to submit feedback! (opt-in)";
			return;
		}
		else if(this.comment.isEmpty()) {
			this.renderNote = false;
			this.error = "You have to provide a comment describing your feedback!";
			return;
		}
		else {
			this.renderNote = storeFeedback();
			if(this.renderNote) {
				this.note = "Your feedback has been submitted!";
				this.hitOnce = true;
				setName("");
				setEmail("");
				setComment("");
				setAllowStorage(false);
			}
		}
	}
	
	public void loginAdmin() {
		boolean success = checkLogin();
		if(success) {	// set visibility of popup panel
			setLoginError("");
			loadFeedback();
		}
		else {
			setLoginError("Wrong username or password!");
		}
		setAuthorized(success);
	}
	
	private boolean storeFeedback() {
		boolean success = false;
		
//		if(this.name.isEmpty() && this.email.isEmpty() && this.comment.isEmpty()) {
//			this.error = "No information given!";
//			return false;
//		}
		
		FacesContext fc = FacesContext.getCurrentInstance();
		ELContext elc = fc.getELContext();
		ExternalContext ec = fc.getExternalContext();
		ELResolver el = fc.getApplication().getELResolver();
		PropertiesBean pb = (PropertiesBean) ec.getApplicationMap().get("propertiesBean");
		/** retrieve session appBean */
		MetFusionBean appBean = (MetFusionBean) el.getValue(elc, null, "appBean");
		
		Connection con = null;
		String driver = pb.getProperty("driver");;
		String db = pb.getProperty("db");
		String user = pb.getProperty("user");
		String password = pb.getProperty("password");
		Driver mysql = null;
		StringBuilder sb = new StringBuilder();
		StringBuilder sbInst = new StringBuilder();
		try {
			mysql = new com.mysql.jdbc.Driver();
			Class.forName(driver); 
			DriverManager.registerDriver (mysql); 
	        // JDBC-driver
	        Class.forName(driver);
	        con = DriverManager.getConnection(db, user, password);
	        
	        String sql = "INSERT INTO Feedback (Name, Email, Comment, Spectrum, massbankLimit, massbankThreshold, massbankIonization, "
	        		+ "massbankInstruments, metfragExactMass, metfragParentIon, metfragAdduct, metfragDatabase, metfragIdentifier, "
	        		+ "metfragFormula, metfragLimit, metfragSearchPPM, metfragMzabs, metfragMzppm, Date, massbankServer, metfusionFilter) "
	        		+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, now(),?,?)";
			
	        PreparedStatement pst = con.prepareStatement(sql);
	        
	        int position = 1;
	        /** feedback information about user*/
	        pst.setString(position, this.name);
	        position++;
	        pst.setString(position, this.email);
	        position++;
	        pst.setString(position, this.comment);
	        position++;

	        /** feedback information about MassBank settings*/
	        pst.setString(position, appBean.getInputSpectrum().replace("\n", escape));		// escape line feed
	        position++;
	        pst.setInt(position, appBean.getMblb().getLimit());
	        position++;
	        pst.setInt(position, appBean.getMblb().getCutoff());
	        position++;
	        pst.setString(position, appBean.getMblb().getSelectedIon());
	        position++;
	        
	        appBean.getMblb().collectInstruments();	// ensure instruments are collected even if no change took place
	        String[] selectedInstruments = appBean.getMblb().getSelectedInstruments();
	        if(selectedInstruments != null && selectedInstruments.length > 0) {			// use chosen instruments
	        	for (int i = 0; i < selectedInstruments.length; i++) {
	    			sbInst.append(selectedInstruments[i]).append(escape);					// escape line feed
	    			System.out.println("storeFeedback instruments -> " + selectedInstruments[i]);
	    		}
	        }
	        String inst = sbInst.toString();
	        if(inst.endsWith(escape))		// remove trailing escape
	        	inst = inst.substring(0, inst.lastIndexOf(escape));
	        pst.setString(position, inst);
	        position++;
	        
	        /** feedback information about MetFrag settings */
	        pst.setDouble(position, appBean.getMfb().getExactMass());
	        position++;
	        pst.setDouble(position, appBean.getMfb().getParentIon());
	        position++;
	        pst.setDouble(position, appBean.getMfb().getSelectedAdduct());
	        position++;
	        pst.setString(position, appBean.getMfb().getSelectedDB());
	        position++;
	        pst.setString(position, appBean.getMfb().getDatabaseID());
	        position++;
	        pst.setString(position, appBean.getMfb().getMolecularFormula());
	        position++;
	        pst.setInt(position, appBean.getMfb().getLimit());
	        position++;
	        pst.setDouble(position, appBean.getMfb().getSearchppm());
	        position++;
	        pst.setDouble(position, appBean.getMfb().getMzabs());
	        position++;
	        pst.setDouble(position, appBean.getMfb().getMzppm());
	        position++;
			
	        /** add additional information */
	        pst.setString(position, appBean.getMblb().getServerUrl());
	        position++;
	        pst.setInt(position, (appBean.getMblb().isUniqueInchi() & appBean.getMfb().isUniqueInchi()) ? 1 : 0);
	        position++;
	        
			pst.executeUpdate();
			pst.close();
			
			sb.append("Name:\t").append(this.name + "\n");
			sb.append("Email:\t").append(this.email + "\n");
			sb.append("Comment:\t").append(this.comment + "\n");
			sb.append("Unique Filter MassBank:\t").append(appBean.getMblb().isUniqueInchi() + "\n");
			sb.append("Unique Filter MetFrag:\t").append(appBean.getMfb().isUniqueInchi() + "\n");
			sb.append("Spectrum:\n").append(appBean.getInputSpectrum() + "\n");	// newline after spectrum for proper view
			sb.append("MassBank Server:\t").append(appBean.getMblb().getServerUrl() + "\n");
			sb.append("MassBank Limit:\t").append(appBean.getMblb().getLimit() + "\n");
			sb.append("MassBank Threshold:\t").append(appBean.getMblb().getCutoff() + "\n");
			sb.append("MassBank Ionization:\t").append(appBean.getMblb().getSelectedIon() + "\n");
			sb.append("MassBank Instruments:\n").append(sbInst.toString().replaceAll(escape, "\t") + "\n");			// newline after instruments for proper view
			sb.append("MetFrag Database:\t").append(appBean.getMfb().getSelectedDB() + "\n");
			sb.append("MetFrag Database IDs:\t").append(appBean.getMfb().getDatabaseID() + "\n");
			sb.append("MetFrag Formula:\t").append(appBean.getMfb().getMolecularFormula() + "\n");
			sb.append("MetFrag Limit:\t").append(appBean.getMfb().getLimit() + "\n");
			sb.append("MetFrag Exact Mass:\t").append(appBean.getMfb().getExactMass() + "\n");
			sb.append("MetFrag Parent Ion:\t").append(appBean.getMfb().getParentIon() + "\n");
			sb.append("MetFrag Adduct:\t").append(appBean.getMfb().getSelectedAdduct() + "\n");
			sb.append("MetFrag Search PPM:\t").append(appBean.getMfb().getSearchppm() + "\n");
			sb.append("MetFrag m/z abs:\t").append(appBean.getMfb().getMzabs() + "\n");
			sb.append("MetFrag m/z ppm:\t").append(appBean.getMfb().getMzppm() + "\n");
			
			success = true;
		} catch (SQLException e) {
			e.printStackTrace();
			this.error = "SQL Error while updating!";
			success = false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			this.error = "Error finding matching database driver!";
			success = false;
		}
		
		finally {
			try {
				String server = pb.getProperty("mailServer");
				String from = pb.getProperty("mailFrom");
				String to = pb.getProperty("mailTo");
				String subject = "MetFusion feedback! " + this.email;
				String message = comment;
				message += "\n\n" + sb.toString();
				
				// only send email if both sender and recipient email are not null
				if(!this.email.isEmpty() && !to.isEmpty()) {
					MailClient client = new MailClient();
					
					if(appBean.getMfb().isRenderSDF()) {
						String[] attachments = new String[1];
						attachments[0] = appBean.getMfb().getSessionPath() + appBean.getMfb().getSelectedSDF();
						client.sendMail(server, this.email, to, subject, message, attachments);		// send SDF as attachment
					}
					else client.sendMailWithoutAttach(server, this.email, to, subject, message);
				}
				else {
					MailClient client = new MailClient();	// fall back to default sender if no email was given
					client.sendMailWithoutAttach(server, from, to, subject, message);
				}
				
				// send also a copy to sender
				//client.sendMailWithoutAttach(server, from, this.email, subject, "Your message:\n\n" + message);
			} catch (AddressException e1) {
				e1.printStackTrace();
				this.error = "Error regarding email adress!";
				success = false;
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
				this.error = "Error - unsupported encoding!";
				success = false;
			} catch (MessagingException e1) {
				e1.printStackTrace();
				this.error = "Error creating email message!";
				success = false;
			}
			try {
				DriverManager.deregisterDriver(mysql);
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
				System.err.println("Error closing database connection!");
				this.error = "Error closing database connection!";
				success = false;
			}
		}
		
		return success;
	}
	
	public boolean loadFeedback() {
		boolean success = false;
		this.feedbackEntries = new ArrayList<FeedbackEntry>();
		this.error = "";
		
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
		PropertiesBean pb = (PropertiesBean) ec.getApplicationMap().get("propertiesBean");
		
		Connection con = null;
		String driver = pb.getProperty("driver");;
		String db = pb.getProperty("db");
		String user = pb.getProperty("user");
		String password = pb.getProperty("password");
		Driver mysql = null;
		
		try {
			mysql = new com.mysql.jdbc.Driver();
			Class.forName(driver); 
			DriverManager.registerDriver (mysql); 
	        // JDBC-driver
	        Class.forName(driver);
	        con = DriverManager.getConnection(db, user, password);
	        
	        String sql = "SELECT * FROM Feedback ORDER BY ID desc;";
		    Statement stmt = con.createStatement();
		    ResultSet rs = stmt.executeQuery(sql);
		    
	        while(rs.next()) {
	        	Map<String, Object> parameters = new HashMap<String, Object>();
	        	
	        	int _ID = rs.getInt("ID");
	        	String _Name = rs.getString("Name");
	        	String _Email = rs.getString("Email");
	        	String _Comment = rs.getString("Comment");
	        	boolean _answered = rs.getBoolean("answered");
	        	boolean _fixed = rs.getBoolean("fixed");
	        	Date _Date = rs.getDate("Date");
	        	
	        	// iterate all columns and store parameters
	        	parameters.put("Spectrum", rs.getString("Spectrum"));
	        	parameters.put("massbankLimit", rs.getInt("massbankLimit"));
	        	parameters.put("massbankThreshold", rs.getInt("massbankThreshold"));
	        	parameters.put("massbankIonization", rs.getString("massbankIonization"));
	        	parameters.put("massbankInstruments", rs.getString("massbankInstruments"));
	        	parameters.put("metfragExactMass", rs.getDouble("metfragExactMass"));
	        	parameters.put("metfragDatabase", rs.getString("metfragDatabase"));
	        	parameters.put("metfragIdentifier", rs.getString("metfragIdentifier"));
	        	parameters.put("metfragFormula", rs.getString("metfragFormula"));
	        	parameters.put("metfragLimit", rs.getInt("metfragLimit"));
	        	parameters.put("metfragSearchPPM", rs.getDouble("metfragSearchPPM"));
	        	parameters.put("metfragMzabs", rs.getDouble("metfragMzabs"));
	        	parameters.put("metfragMzppm", rs.getDouble("metfragMzppm"));
	        	parameters.put("metfragParentIon", rs.getDouble("metfragParentIon"));
	        	parameters.put("metfragAdduct", rs.getDouble("metfragAdduct"));
	        	parameters.put("massbankServer", rs.getString("massbankServer"));
	        	parameters.put("metfusionFilter", rs.getBoolean("metfusionFilter"));
	        	
	        	feedbackEntries.add(new FeedbackEntry(_ID, _Name, _Email, _Comment, parameters, _answered, _fixed, _Date));
	        }
			success = true;
		} catch (SQLException e) {
			e.printStackTrace();
			this.error = "SQL Error while fetching results!";
			success = false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			this.error = "Error finding matching database driver!";
			success = false;
		}
		finally {
			try {
				DriverManager.deregisterDriver(mysql); 
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
				System.err.println("Error closing database connection!");
				this.error = "Error closing database connection!";
				success = false;
			}
		}
		
		return success;
	}
	
	public void feedbackRowSelectionListener(ActionEvent event) {
		//gets the current row from the data table
		FeedbackEntry current = (FeedbackEntry) event.getComponent().getAttributes().get("currentFeedbackEntry");
		
		boolean loaded = loadValues(current);
		if(loaded) {
			this.adminNote = "Loaded values successfully!";
			this.renderAdminNote = true;
		}
		else {
			this.adminNote = "Error loading values!";
			this.renderAdminNote = false;
		}
	}
	
	private boolean loadValues(FeedbackEntry entry) {
		boolean success = false;
		
		// retrieve current session bean
		FacesContext fc = FacesContext.getCurrentInstance();
		ELContext elc = fc.getELContext();
		ELResolver el = fc.getApplication().getELResolver();
		/** retrieve session appBean */
		MetFusionBean appBean = (MetFusionBean) el.getValue(elc, null, "appBean");
		
		if(appBean == null)
			return success;
		
		Map<String, Object> settings = entry.getSettings();
		// set data into beans
		// MetFusion settings
		appBean.setUseInChIFiltering((Boolean) settings.get("metfusionFilter"));
		
		// MassBank settings
		String storedServer = (String) settings.get("massbankServer");
		if(!appBean.getMblb().getServerUrl().equals(storedServer)) {
			appBean.getMblb().setAllowOtherServer(Boolean.TRUE);	// allow setup of other servers
			appBean.getMblb().setServerUrl(storedServer);		// setup serverUrl and MassBank
		}
		appBean.getMblb().setLimit((Integer) settings.get("massbankLimit"));
		appBean.getMblb().setCutoff((Integer) settings.get("massbankThreshold"));
		appBean.getMblb().setSelectedIon((String) settings.get("massbankIonization"));
		
		// load instruments
		String selected = (String) settings.get("massbankInstruments");
		String[] selectedInstruments = selected.split(escape);
		appBean.getMblb().setSelectedInstruments(selectedInstruments);
		appBean.getMblb().loadInstruments(selectedInstruments);	// flush instruments to correct selectedInstrumentGroups

		// load spectrum
		String peaks = (String) settings.get("Spectrum");
		peaks = peaks.replaceAll(escape, "\n");
		appBean.setInputSpectrum(peaks);
		
		// MetFrag settings
		appBean.getMfb().setParentIon((Double) settings.get("metfragParentIon"));
		appBean.getMfb().setSelectedAdduct((Double) settings.get("metfragAdduct"));
		appBean.getMfb().setExactMass((Double) settings.get("metfragExactMass"));
		appBean.getMfb().setSelectedDB((String) settings.get("metfragDatabase"));
		appBean.getMfb().setDatabaseID((String) settings.get("metfragIdentifier"));
		appBean.getMfb().setMolecularFormula((String) settings.get("metfragFormula"));
		appBean.getMfb().setLimit((Integer) settings.get("metfragLimit"));
		appBean.getMfb().setSearchppm((Double) settings.get("metfragSearchPPM"));
		appBean.getMfb().setMzabs((Double) settings.get("metfragMzabs"));
		appBean.getMfb().setMzppm((Double) settings.get("metfragMzppm"));
		
		return success = true;
	}
	
	public void validateFeedback(FacesContext context, UIComponent component, Object obj) {
		System.out.println("validateFeedback");
		UIInput uiName = (UIInput) component.findComponent("form:feedbackName");
		String name = uiName.getLocalValue().toString();
		UIInput feedbackEmail = (UIInput) component.findComponent("form:feedbackEmail");
		String email = feedbackEmail.getLocalValue().toString();
		
		String comment = (String) obj;
		
		if(name.isEmpty() && email.isEmpty() && comment.isEmpty()) {
			this.error = "Feedback validation failed.";
			this.renderNote = false;
			FacesMessage msg = new FacesMessage("Feedback validation failed.", "No information given!");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);	
		}
		
	}
	
	@Override
	public void validate(FacesContext context, UIComponent component, Object obj)
			throws ValidatorException {
		System.out.println("validate");
		UIInput uiName = (UIInput) component.findComponent("form:feedbackName");
		String name = uiName.getLocalValue().toString();
		UIInput feedbackEmail = (UIInput) component.findComponent("form:feedbackEmail");
		String email = feedbackEmail.getLocalValue().toString();
		
		String comment = (String) obj;
		
		if(name.isEmpty() && email.isEmpty() && comment.isEmpty()) {
			this.error = "Feedback validation failed.";
			this.renderNote = false;
			FacesMessage msg = new FacesMessage("Feedback validation failed.", "No information given!");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);	
		}
	}
	
	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getJdbc() {
		return jdbc;
	}

	public void setJdbc(String jdbc) {
		this.jdbc = jdbc;
	}

	public String getMailServer() {
		return mailServer;
	}

	public void setMailServer(String mailServer) {
		this.mailServer = mailServer;
	}

	public String getMailTo() {
		return mailTo;
	}

	public void setMailTo(String mailTo) {
		this.mailTo = mailTo;
	}

	public String getMailFrom() {
		return mailFrom;
	}

	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getError() {
		return error;
	}

	public void setProps(PropertiesBean props) {
		this.props = props;
	}

	public PropertiesBean getProps() {
		return props;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getNote() {
		return note;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setRenderNote(boolean renderNote) {
		this.renderNote = renderNote;
	}

	public boolean isRenderNote() {
		return renderNote;
	}

	public void setFeedbackEntries(List<FeedbackEntry> feedbackEntries) {
		this.feedbackEntries = feedbackEntries;
	}

	public List<FeedbackEntry> getFeedbackEntries() {
		return feedbackEntries;
	}
	
	public void setFeedbackRendered(boolean feedbackRendered) {
		this.feedbackRendered = feedbackRendered;
	}

	public boolean isFeedbackRendered() {
		return feedbackRendered;
	}
	public void setFeedbackAdminRendered(boolean feedbackAdminRendered) {
		this.feedbackAdminRendered = feedbackAdminRendered;
	}
	public boolean isFeedbackAdminRendered() {
		return feedbackAdminRendered;
	}
	public void setHitOnce(boolean hitOnce) {
		this.hitOnce = hitOnce;
	}
	public boolean isHitOnce() {
		return hitOnce;
	}
	public boolean isAuthorized() {
		return authorized;
	}
	public void setAuthorized(boolean authorized) {
		this.authorized = authorized;
	}
	public String getAdminUser() {
		return adminUser;
	}
	public String getAdminPass() {
		return adminPass;
	}
	public String getProvidedName() {
		return providedName;
	}
	public void setProvidedName(String providedName) {
		this.providedName = providedName;
	}
	public String getProvidedPass() {
		return providedPass;
	}
	public void setProvidedPass(String providedPass) {
		this.providedPass = providedPass;
	}
	public String getLoginError() {
		return loginError;
	}
	public void setLoginError(String loginError) {
		this.loginError = loginError;
	}
	public void setAdminNote(String adminNote) {
		this.adminNote = adminNote;
	}
	public String getAdminNote() {
		return adminNote;
	}
	public void setRenderAdminNote(boolean renderAdminNote) {
		this.renderAdminNote = renderAdminNote;
	}
	public boolean isRenderAdminNote() {
		return renderAdminNote;
	}
	public void setAllowStorage(boolean allowStorage) {
		this.allowStorage = allowStorage;
	}
	public boolean isAllowStorage() {
		return allowStorage;
	}
}
