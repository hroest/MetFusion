/**
 * created by Michael Gerlich, Oct 17, 2011 - 1:55:26 PM
 */

package de.ipbhalle.metfusion.utilities.icefaces;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

@ApplicationScoped
@ManagedBean(name = "messageUtils")
public class TutorialMessageUtils {

	private Map getLinkLabelMap = new LinkLabelMap();
	private Map getLinkUrlMap = new LinkUrlMap();
	private static ResourceBundle messages = ResourceBundle.getBundle("messages");

	/* Faux-maps to allow for parameter passing in EL 1.0 */

	/*
	 * LinkLabelMap - Returns the title for the currently selected
	 * navigation.content.list string
	 */
	private class LinkUrlMap implements Map {
		public Object get(Object o) {
			String s = (String) o;
			return messages.getString("navigation.link." + o + ".url");
		}

		public int size() {
			return 0;
		}

		public boolean isEmpty() {
			return false;
		}

		public boolean containsKey(Object o) {
			return false;
		}

		public boolean containsValue(Object o) {
			return false;
		}

		public Object put(Object o, Object o1) {
			return null;
		}

		public Object remove(Object o) {
			return null;
		}

		public void putAll(Map map) {
		}

		public void clear() {
		}

		public Set keySet() {
			return null;
		}

		public Collection values() {
			return null;
		}

		public Set entrySet() {
			return null;
		}
	}

	private class LinkLabelMap implements Map {
		public Object get(Object o) {
			String s = (String) o;
			return messages.getString("navigation.link." + o + ".label");
		}

		public int size() {
			return 0;
		}

		public boolean isEmpty() {
			return false;
		}

		public boolean containsKey(Object o) {
			return false;
		}

		public boolean containsValue(Object o) {
			return false;
		}

		public Object put(Object o, Object o1) {
			return null;
		}

		public Object remove(Object o) {
			return null;
		}

		public void putAll(Map map) {
		}

		public void clear() {
		}

		public Set keySet() {
			return null;
		}

		public Collection values() {
			return null;
		}

		public Set entrySet() {
			return null;
		}
	}

	public Map getGetLinkLabelMap() {
		return getLinkLabelMap;
	}

	public void setGetLinkLabelMap(Map getLinkLabelMap) {
		this.getLinkLabelMap = getLinkLabelMap;
	}

	public static String getMessage(String key) {
		return messages.getString(key);
	}

	public Map getGetLinkUrlMap() {
		return getLinkUrlMap;
	}

	public void setGetLinkUrlMap(Map getLinkUrlMap) {
		this.getLinkUrlMap = getLinkUrlMap;
	}
}
