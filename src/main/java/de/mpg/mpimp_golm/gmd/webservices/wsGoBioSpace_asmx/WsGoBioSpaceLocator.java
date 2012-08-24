/**
 * WsGoBioSpaceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx;

public class WsGoBioSpaceLocator extends org.apache.axis.client.Service implements de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.WsGoBioSpace {

/**
 * These web services provide access to methods for searching the
 * <a href="/GoBioSpace.aspx">GoBioSpace</a> database which is part of
 * the Golm Metabolome Database (GMD, <a href="http://gmd.mpimp-golm.mpg.de/">http://gmd.mpimp-golm.mpg.de/</a>).
 * Please send all feedback to <a href="mailto:hummel@mpimp-golm.mpg.de">hummel@mpimp-golm.mpg.de</a>
 */

    public WsGoBioSpaceLocator() {
    }


    public WsGoBioSpaceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public WsGoBioSpaceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for wsGoBioSpaceSoap
    private java.lang.String wsGoBioSpaceSoap_address = "http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx";

    public java.lang.String getwsGoBioSpaceSoapAddress() {
        return wsGoBioSpaceSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String wsGoBioSpaceSoapWSDDServiceName = "wsGoBioSpaceSoap";

    public java.lang.String getwsGoBioSpaceSoapWSDDServiceName() {
        return wsGoBioSpaceSoapWSDDServiceName;
    }

    public void setwsGoBioSpaceSoapWSDDServiceName(java.lang.String name) {
        wsGoBioSpaceSoapWSDDServiceName = name;
    }

    public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.WsGoBioSpaceSoap getwsGoBioSpaceSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(wsGoBioSpaceSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getwsGoBioSpaceSoap(endpoint);
    }

    public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.WsGoBioSpaceSoap getwsGoBioSpaceSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.WsGoBioSpaceSoapStub _stub = new de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.WsGoBioSpaceSoapStub(portAddress, this);
            _stub.setPortName(getwsGoBioSpaceSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setwsGoBioSpaceSoapEndpointAddress(java.lang.String address) {
        wsGoBioSpaceSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.WsGoBioSpaceSoap.class.isAssignableFrom(serviceEndpointInterface)) {
                de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.WsGoBioSpaceSoapStub _stub = new de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.WsGoBioSpaceSoapStub(new java.net.URL(wsGoBioSpaceSoap_address), this);
                _stub.setPortName(getwsGoBioSpaceSoapWSDDServiceName());
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
        if ("wsGoBioSpaceSoap".equals(inputPortName)) {
            return getwsGoBioSpaceSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "wsGoBioSpace");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "wsGoBioSpaceSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("wsGoBioSpaceSoap".equals(portName)) {
            setwsGoBioSpaceSoapEndpointAddress(address);
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
