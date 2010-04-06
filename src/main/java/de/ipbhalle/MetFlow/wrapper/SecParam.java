package de.ipbhalle.MetFlow.wrapper;

import java.util.List;
import java.util.Map;

public class SecParam {

	private String serviceName;
	private List<String> paramKeys;
	private List<String> paramValues;
	private Map<String, SecParamObj> paramInstance;
	
	public SecParam() {
		// TODO Auto-generated constructor stub
	}
	
	public SecParam(String serviceKeys, List<String> paramKeys, List<String> paramValues) {
		this.serviceName = serviceKeys;
		this.paramKeys = paramKeys;
		this.paramValues = paramValues;
	}
	
	public SecParam(String serviceKeys, List<String> paramKeys, List<String> paramValues, Map<String, SecParamObj> instances) {
		this.serviceName = serviceKeys;
		this.paramKeys = paramKeys;
		this.paramValues = paramValues;
		this.paramInstance = instances;
	}
	
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
	public void setParamKeys(List<String> paramKeys) {
		this.paramKeys = paramKeys;
	}

	public List<String> getParamKeys() {
		return paramKeys;
	}

	public void setParamValues(List<String> paramValues) {
		this.paramValues = paramValues;
	}

	public List<String> getParamValues() {
		return paramValues;
	}

	public void setParamInstance(Map<String, SecParamObj> paramInstance) {
		this.paramInstance = paramInstance;
	}

	public Map<String, SecParamObj> getParamInstance() {
		return paramInstance;
	}
}
