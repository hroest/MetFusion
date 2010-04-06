package de.ipbhalle.MetFlow.wrapper;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.faces.model.SelectItem;


public class SecParamObj {

	public static final String INTEGER_TYPE = "Integer";
	public static final String FLOAT_TYPE = "Float";
	public static final String STRING_TYPE = "String";
	public static final String DATETIME_TYPE = "DateTime";
	public static final String BOOLEAN_TYPE = "Boolean";

	private String name;
	private String id = null;

	private String dataType = STRING_TYPE;
	private String defaultValue = "";
	private String minimumValue = "";
	private String maximumValue = "";
	private Vector<String> allowedValues = new Vector<String>();
	private List<SelectItem> av;
	
	private String description = "";
	private int numAllowed;
	private String value = ""; // used value in service
	
	private boolean bool;
	private boolean qualified;
	private boolean needField;
	private boolean allowed;
	
	
	
	public SecParamObj() {
		// TODO Auto-generated constructor stub
	}
	
	public SecParamObj(String name, String def) {
		this.name = name;
		this.defaultValue = def;
	}

	public SecParamObj(String name, String datatype, String def, String min,
			String max, String[] vals, String desc) {
		this.name = name;
		try {
			setDataType(datatype);
		} catch (Exception e) {
			e.printStackTrace();
			this.dataType = "Object";
		}
		this.defaultValue = def;
		this.minimumValue = min;
		this.maximumValue = max;
		setAllowedValues(vals);
		this.description = desc;
		this.value = def;
		
		//!min.equals(null) & !max.equals(null) & !def.equals(null) & 
		if(!min.isEmpty() & !max.isEmpty() & !def.isEmpty() & datatype.equalsIgnoreCase(INTEGER_TYPE)) {
			this.qualified = true;
			System.out.println("qualified = " + this.qualified);
		}
		else if (this.bool) {
			this.needField = false;
		}
		else this.needField = true;
		
		if(vals != null & vals.length > 0) {
			this.allowed = true;
			this.qualified = false;
			this.needField = false;
		}
	}


	public void setDataType(String dataType) throws Exception {
		this.dataType = dataType;

		if (dataType.equalsIgnoreCase(INTEGER_TYPE)) {
			this.dataType = INTEGER_TYPE;
		} else if (dataType.equalsIgnoreCase(FLOAT_TYPE)) {
			this.dataType = FLOAT_TYPE;
		} else if (dataType.equalsIgnoreCase(STRING_TYPE)) {
			this.dataType = STRING_TYPE;
		} else if (dataType.equalsIgnoreCase(DATETIME_TYPE)) {
			this.dataType = DATETIME_TYPE;
		} else if (dataType.equalsIgnoreCase(BOOLEAN_TYPE)) {
			this.dataType = BOOLEAN_TYPE;
			this.bool = true;
		} else {
			throw new Exception("Data type for secondary parameter '"
					+ getName() + "' was not valid (\"" + dataType
					+ "\"), must be one of " + INTEGER_TYPE + ", " + FLOAT_TYPE
					+ ", " + STRING_TYPE + ", " + DATETIME_TYPE + ", "
					+ BOOLEAN_TYPE);
		}
	}


	public String getDataType() {
		return dataType;
	}


	public void setDefaultValue(String defaultValue) {
		this.defaultValue = (defaultValue == null ? "" : defaultValue);
	}


	public String getDefaultValue() {
		return defaultValue;
	}


	public void setMinimumValue(String minimumValue) {
		this.minimumValue = minimumValue;
	}


	public String getMinimumValue() {
		return minimumValue;
	}


	public void setMaximumValue(String maximumValue) {
		this.maximumValue = maximumValue;
	}


	public String getMaximumValue() {
		return maximumValue;
	}


	public void setAllowedValues(Vector<String> allowedValues) {
		this.allowedValues = allowedValues;
	}


	// public Vector<String> getAllowedValues() {
	// return allowedValues;
	// }

	public String[] getAllowedValues() {
		String[] result = new String[allowedValues.size()];
		allowedValues.copyInto(result);
		return result;
	}


	public void setAllowedValues(String[] value) {
		if (value == null) {
			allowedValues.clear();
			numAllowed = 0;
		} else {
			numAllowed = value.length;
			this.av = new ArrayList<SelectItem>();
			for (int i = 0; i < value.length; i++) {
				allowedValues.addElement(value[i]);
				SelectItem item = new SelectItem(value[i], value[i]);
				this.av.add(item);
			}				
		}
	}


	public void addAllowedValue(String value) {
		allowedValues.addElement(value);
	}


	public boolean isPrimary() {
		return false;
	}


	public void setDescription(String description) {
		this.description = (description == null ? "" : description);
	}


	public String getDescription() {
		return description;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getName() {
		return name;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getId() {
		return id;
	}


	public String toXML() {
		StringBuffer buf = new StringBuffer();
		buf.append("<Parameter articleName=\"");
		buf.append(name);
		buf.append("\">\n");
		buf.append("<datatype>");
		buf.append(dataType);
		buf.append("</datatype>\n");
		buf.append("<description>");
		buf.append(description);
		buf.append("</description>\n");
		if (!defaultValue.equals(""))
			buf.append("<default>" + defaultValue + "</default>\n");
		if (!"".equals(maximumValue))
			buf.append("<max>" + maximumValue + "</max>\n");
		if (!"".equals(minimumValue))
			buf.append("<min>" + minimumValue + "</min>\n");
		if (allowedValues.size() > 0) {
			for (Enumeration<String> en = allowedValues.elements(); en
					.hasMoreElements();) {
				buf.append("<enum>");
				buf.append(en.nextElement());
				buf.append("</enum>\n");
			}
		}
		buf.append("</Parameter>\n");
		return new String(buf);
	}


	public void setValue(String value) {
		this.value = (value == null ? "" : value);
	}


	public String getValue() {
		return value;
	}


	public String toString() {
		StringBuffer buf = new StringBuffer();
		if (name != null && name.length() > 0)
			buf.append("Name:      " + name + "\n");
		if (id != null)
			buf.append("ID:        " + id + "\n");
		buf.append("Data Type: " + dataType + "\n");
		if (description != null && description.length() > 0)
			buf.append("Desc:      " + description + "\n");
		if (defaultValue != null && defaultValue.length() > 0)
			buf.append("Default:   " + defaultValue + "\n");
		if (minimumValue != null && minimumValue.length() > 0)
			buf.append("Min:       " + minimumValue + "\n");
		if (maximumValue != null && maximumValue.length() > 0)
			buf.append("Max:       " + maximumValue + "\n");
		if (allowedValues.size() > 0) {
			buf.append("Allowed values: ");
			for (Enumeration<String> en = allowedValues.elements(); en
					.hasMoreElements();) {
				buf.append(en.nextElement() + " ");
			}
			buf.append("\n");
		}
		return new String(buf);
	}

	public void setNumAllowed(int numAllowed) {
		this.numAllowed = numAllowed;
	}

	public int getNumAllowed() {
		return numAllowed;
	}

	public void setBool(boolean bool) {
		this.bool = bool;
	}

	public boolean isBool() {
		return bool;
	}

	public void setQualified(boolean qualified) {
		this.qualified = qualified;
	}

	public boolean isQualified() {
		return qualified;
	}

	public void setNeedField(boolean needField) {
		this.needField = needField;
	}

	public boolean isNeedField() {
		return needField;
	}

	public void setAllowed(boolean allowed) {
		this.allowed = allowed;
	}

	public boolean isAllowed() {
		return allowed;
	}

	public void setAv(List<SelectItem> av) {
		this.av = av;
	}

	public List<SelectItem> getAv() {
		return av;
	}
}
