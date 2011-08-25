/**
 * created by Michael Gerlich, Aug 17, 2011 - 10:24:31 AM
 */ 

package de.ipbhalle.metfusion.wrapper;

import java.sql.Date;
import java.util.Map;

/**
 * A wrapper class to handle feedback entries stored inside a database.
 * 
 * @author mgerlich
 */
public class FeedbackEntry {

	private int ID;
	private String name;
	private String email;
	private String comment;
	
	private Map<String, Object> settings;
	
	private boolean answered;
	private boolean fixed;
	private Date date;
	
	public FeedbackEntry(int ID, String name, String email, String comment, Map<String, Object> settings, boolean answered, boolean fixed, Date date) {
		this.ID = ID;
		this.name = name;
		this.email = email;
		this.comment = comment;
		this.settings = settings;
		this.answered = answered;
		this.fixed = fixed;
		this.setDate(date);
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Map<String, Object> getSettings() {
		return settings;
	}

	public void setSettings(Map<String, Object> settings) {
		this.settings = settings;
	}

	public boolean isAnswered() {
		return answered;
	}

	public void setAnswered(boolean answered) {
		this.answered = answered;
	}

	public boolean isFixed() {
		return fixed;
	}

	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getDate() {
		return date;
	}
}
