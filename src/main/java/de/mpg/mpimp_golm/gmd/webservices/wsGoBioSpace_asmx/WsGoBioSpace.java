/**
 * WsGoBioSpace.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx;

public interface WsGoBioSpace extends javax.xml.rpc.Service {

/**
 * These web services provide access to methods for searching the
 * <a href="/GoBioSpace.aspx">GoBioSpace</a> database which is part of
 * the Golm Metabolome Database (GMD, <a href="http://gmd.mpimp-golm.mpg.de/">http://gmd.mpimp-golm.mpg.de/</a>).
 * Please send all feedback to <a href="mailto:hummel@mpimp-golm.mpg.de">hummel@mpimp-golm.mpg.de</a>
 */
    public java.lang.String getwsGoBioSpaceSoapAddress();

    public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.WsGoBioSpaceSoap getwsGoBioSpaceSoap() throws javax.xml.rpc.ServiceException;

    public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.WsGoBioSpaceSoap getwsGoBioSpaceSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
