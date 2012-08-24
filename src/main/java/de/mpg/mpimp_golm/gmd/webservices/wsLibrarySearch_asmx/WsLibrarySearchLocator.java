/**
 * WsLibrarySearchLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx;

public class WsLibrarySearchLocator extends org.apache.axis.client.Service implements de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.WsLibrarySearch {

/**
 * These web services provide access to methods for matching user
 * submitted spectra against the GMD.
 *             Please send all feedback to <a href="mailto:hummel@mpimp-golm.mpg.de">hummel@mpimp-golm.mpg.de</a>
 */

    public WsLibrarySearchLocator() {
    }


    public WsLibrarySearchLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public WsLibrarySearchLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for wsLibrarySearchSoap
    private java.lang.String wsLibrarySearchSoap_address = "http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx";

    public java.lang.String getwsLibrarySearchSoapAddress() {
        return wsLibrarySearchSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String wsLibrarySearchSoapWSDDServiceName = "wsLibrarySearchSoap";

    public java.lang.String getwsLibrarySearchSoapWSDDServiceName() {
        return wsLibrarySearchSoapWSDDServiceName;
    }

    public void setwsLibrarySearchSoapWSDDServiceName(java.lang.String name) {
        wsLibrarySearchSoapWSDDServiceName = name;
    }

    public de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.WsLibrarySearchSoap getwsLibrarySearchSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(wsLibrarySearchSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getwsLibrarySearchSoap(endpoint);
    }

    public de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.WsLibrarySearchSoap getwsLibrarySearchSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.WsLibrarySearchSoapStub _stub = new de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.WsLibrarySearchSoapStub(portAddress, this);
            _stub.setPortName(getwsLibrarySearchSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setwsLibrarySearchSoapEndpointAddress(java.lang.String address) {
        wsLibrarySearchSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.WsLibrarySearchSoap.class.isAssignableFrom(serviceEndpointInterface)) {
                de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.WsLibrarySearchSoapStub _stub = new de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.WsLibrarySearchSoapStub(new java.net.URL(wsLibrarySearchSoap_address), this);
                _stub.setPortName(getwsLibrarySearchSoapWSDDServiceName());
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
        if ("wsLibrarySearchSoap".equals(inputPortName)) {
            return getwsLibrarySearchSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "wsLibrarySearch");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "wsLibrarySearchSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("wsLibrarySearchSoap".equals(portName)) {
            setwsLibrarySearchSoapEndpointAddress(address);
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
