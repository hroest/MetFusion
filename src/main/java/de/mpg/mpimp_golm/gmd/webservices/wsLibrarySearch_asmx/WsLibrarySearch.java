/**
 * WsLibrarySearch.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx;

public interface WsLibrarySearch extends javax.xml.rpc.Service {

/**
 * These web services provide access to methods for matching user
 * submitted spectra against the GMD.
 *             Please send all feedback to <a href="mailto:hummel@mpimp-golm.mpg.de">hummel@mpimp-golm.mpg.de</a>
 */
    public java.lang.String getwsLibrarySearchSoapAddress();

    public de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.WsLibrarySearchSoap getwsLibrarySearchSoap() throws javax.xml.rpc.ServiceException;

    public de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.WsLibrarySearchSoap getwsLibrarySearchSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
