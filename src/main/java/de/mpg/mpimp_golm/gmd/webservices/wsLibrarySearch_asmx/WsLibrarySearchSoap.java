/**
 * WsLibrarySearchSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx;

public interface WsLibrarySearchSoap extends java.rmi.Remote {

    /**
     * Matches a single user submitted GC-EI mass spectrum against
     * the Golm Metabolome Database (GMD).
     *                 'ri' is the retention index based on the retention
     * of the alkane homologues.
     *                 'riWindow' is the retention index window.
     *                 'gccolumn' is either 'VAR5', 'MDN35' or 'none'.
     *                 'Spectrum' is the GC-MS mass spectrum. All matching
     * hits will be returned together with appropiate distance measures.
     */
    public de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.ResultOfAnnotatedMatch librarySearch(float ri, float riWindow, de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.AlkaneRetentionIndexGcColumnComposition alkaneRetentionIndexGcColumnComposition, java.lang.String spectrum) throws java.rmi.RemoteException;

    /**
     * Returns a clear name (i.e. '<strong>{A138002-13C-DL--4}</strong>
     * for a particular spectrum given by ID (i.e. 'ae7dc939-0674-40e8-ad59-db0c491a043f').
     * The returned string includes the linked Anlayte name and A-number,
     * the steroisomer code, the isotopomer code and the spectra replica
     * number.
     */
    public java.lang.String MPIMP_Quad_Name(java.lang.String spectrumID) throws java.rmi.RemoteException;

    /**
     * Returns a list of compound names from the Golm Metabolome Database
     * (GMD).
     *                 <strong>prefixText</strong> the first characters of
     * the compound name to search for.
     *                 <strong>count</strong> the number of names to return.
     * This service is consumed from an control on the <a href="/search.aspx">search.aspx</a>
     * web page, to populate the list of available compound names
     */
    public java.lang.String[] getCompoundNames(java.lang.String prefixText, int count) throws java.rmi.RemoteException;
}
