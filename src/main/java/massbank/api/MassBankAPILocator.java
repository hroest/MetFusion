/**
 * MassBankAPILocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package massbank.api;

public class MassBankAPILocator extends org.apache.axis.client.Service implements massbank.api.MassBankAPI {

    public MassBankAPILocator() {
    }


    public MassBankAPILocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public MassBankAPILocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for MassBankAPIHttpSoap11Endpoint
    private java.lang.String MassBankAPIHttpSoap11Endpoint_address = "http://www.massbank.jp:80/api/services/MassBankAPI.MassBankAPIHttpSoap11Endpoint/";

    public java.lang.String getMassBankAPIHttpSoap11EndpointAddress() {
        return MassBankAPIHttpSoap11Endpoint_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String MassBankAPIHttpSoap11EndpointWSDDServiceName = "MassBankAPIHttpSoap11Endpoint";

    public java.lang.String getMassBankAPIHttpSoap11EndpointWSDDServiceName() {
        return MassBankAPIHttpSoap11EndpointWSDDServiceName;
    }

    public void setMassBankAPIHttpSoap11EndpointWSDDServiceName(java.lang.String name) {
        MassBankAPIHttpSoap11EndpointWSDDServiceName = name;
    }

    public massbank.api.MassBankAPIPortType getMassBankAPIHttpSoap11Endpoint() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(MassBankAPIHttpSoap11Endpoint_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getMassBankAPIHttpSoap11Endpoint(endpoint);
    }

    public massbank.api.MassBankAPIPortType getMassBankAPIHttpSoap11Endpoint(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            massbank.api.MassBankAPISoap11BindingStub _stub = new massbank.api.MassBankAPISoap11BindingStub(portAddress, this);
            _stub.setPortName(getMassBankAPIHttpSoap11EndpointWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setMassBankAPIHttpSoap11EndpointEndpointAddress(java.lang.String address) {
        MassBankAPIHttpSoap11Endpoint_address = address;
    }


    // Use to get a proxy class for MassBankAPIHttpSoap12Endpoint
    private java.lang.String MassBankAPIHttpSoap12Endpoint_address = "http://www.massbank.jp:80/api/services/MassBankAPI.MassBankAPIHttpSoap12Endpoint/";

    public java.lang.String getMassBankAPIHttpSoap12EndpointAddress() {
        return MassBankAPIHttpSoap12Endpoint_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String MassBankAPIHttpSoap12EndpointWSDDServiceName = "MassBankAPIHttpSoap12Endpoint";

    public java.lang.String getMassBankAPIHttpSoap12EndpointWSDDServiceName() {
        return MassBankAPIHttpSoap12EndpointWSDDServiceName;
    }

    public void setMassBankAPIHttpSoap12EndpointWSDDServiceName(java.lang.String name) {
        MassBankAPIHttpSoap12EndpointWSDDServiceName = name;
    }

    public massbank.api.MassBankAPIPortType getMassBankAPIHttpSoap12Endpoint() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(MassBankAPIHttpSoap12Endpoint_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getMassBankAPIHttpSoap12Endpoint(endpoint);
    }

    public massbank.api.MassBankAPIPortType getMassBankAPIHttpSoap12Endpoint(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            massbank.api.MassBankAPISoap12BindingStub _stub = new massbank.api.MassBankAPISoap12BindingStub(portAddress, this);
            _stub.setPortName(getMassBankAPIHttpSoap12EndpointWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setMassBankAPIHttpSoap12EndpointEndpointAddress(java.lang.String address) {
        MassBankAPIHttpSoap12Endpoint_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     * This service has multiple ports for a given interface;
     * the proxy implementation returned may be indeterminate.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (massbank.api.MassBankAPIPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                massbank.api.MassBankAPISoap11BindingStub _stub = new massbank.api.MassBankAPISoap11BindingStub(new java.net.URL(MassBankAPIHttpSoap11Endpoint_address), this);
                _stub.setPortName(getMassBankAPIHttpSoap11EndpointWSDDServiceName());
                return _stub;
            }
            if (massbank.api.MassBankAPIPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                massbank.api.MassBankAPISoap12BindingStub _stub = new massbank.api.MassBankAPISoap12BindingStub(new java.net.URL(MassBankAPIHttpSoap12Endpoint_address), this);
                _stub.setPortName(getMassBankAPIHttpSoap12EndpointWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("MassBankAPIHttpSoap11Endpoint".equals(inputPortName)) {
            return getMassBankAPIHttpSoap11Endpoint();
        }
        else if ("MassBankAPIHttpSoap12Endpoint".equals(inputPortName)) {
            return getMassBankAPIHttpSoap12Endpoint();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://api.massbank", "MassBankAPI");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://api.massbank", "MassBankAPIHttpSoap11Endpoint"));
            ports.add(new javax.xml.namespace.QName("http://api.massbank", "MassBankAPIHttpSoap12Endpoint"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("MassBankAPIHttpSoap11Endpoint".equals(portName)) {
            setMassBankAPIHttpSoap11EndpointEndpointAddress(address);
        }
        else 
if ("MassBankAPIHttpSoap12Endpoint".equals(portName)) {
            setMassBankAPIHttpSoap12EndpointEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
