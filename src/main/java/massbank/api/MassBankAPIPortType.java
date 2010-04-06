/**
 * MassBankAPIPortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package massbank.api;

public interface MassBankAPIPortType extends java.rmi.Remote {
    public massbank.api.xsd.SearchResult searchPeakDiff(java.lang.String[] mzs, java.lang.String relativeIntensity, java.lang.String tolerance, java.lang.String[] instrumentTypes, java.lang.String ionMode, java.lang.Integer maxNumResults) throws java.rmi.RemoteException;
    public java.lang.String[] getInstrumentTypes() throws java.rmi.RemoteException;
    public massbank.api.xsd.SearchResult searchSpectrum(java.lang.String[] mzs, java.lang.String[] intensities, java.lang.String unit, java.lang.String tolerance, java.lang.String cutoff, java.lang.String[] instrumentTypes, java.lang.String ionMode, java.lang.Integer maxNumResults) throws java.rmi.RemoteException;
    public massbank.api.xsd.RecordInfo[] getRecordInfo(java.lang.String[] ids) throws java.rmi.RemoteException;
    public massbank.api.xsd.SearchResult searchPeak(java.lang.String[] mzs, java.lang.String relativeIntensity, java.lang.String tolerance, java.lang.String[] instrumentTypes, java.lang.String ionMode, java.lang.Integer maxNumResults) throws java.rmi.RemoteException;
    public massbank.api.xsd.Peak[] getPeak(java.lang.String[] ids) throws java.rmi.RemoteException;
}
