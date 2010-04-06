/**
 * MassBankAPI.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package massbank.api;

public interface MassBankAPI extends javax.xml.rpc.Service {
    public java.lang.String getMassBankAPIHttpSoap11EndpointAddress();

    public massbank.api.MassBankAPIPortType getMassBankAPIHttpSoap11Endpoint() throws javax.xml.rpc.ServiceException;

    public massbank.api.MassBankAPIPortType getMassBankAPIHttpSoap11Endpoint(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
    public java.lang.String getMassBankAPIHttpSoap12EndpointAddress();

    public massbank.api.MassBankAPIPortType getMassBankAPIHttpSoap12Endpoint() throws javax.xml.rpc.ServiceException;

    public massbank.api.MassBankAPIPortType getMassBankAPIHttpSoap12Endpoint(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
