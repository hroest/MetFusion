package massbank.api;

public class MassBankAPIPortTypeProxy implements massbank.api.MassBankAPIPortType {
  private String _endpoint = null;
  private massbank.api.MassBankAPIPortType massBankAPIPortType = null;
  
  public MassBankAPIPortTypeProxy() {
    _initMassBankAPIPortTypeProxy();
  }
  
  public MassBankAPIPortTypeProxy(String endpoint) {
    _endpoint = endpoint;
    _initMassBankAPIPortTypeProxy();
  }
  
  private void _initMassBankAPIPortTypeProxy() {
    try {
      massBankAPIPortType = (new massbank.api.MassBankAPILocator()).getMassBankAPIHttpSoap11Endpoint();
      if (massBankAPIPortType != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)massBankAPIPortType)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)massBankAPIPortType)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (massBankAPIPortType != null)
      ((javax.xml.rpc.Stub)massBankAPIPortType)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public massbank.api.MassBankAPIPortType getMassBankAPIPortType() {
    if (massBankAPIPortType == null)
      _initMassBankAPIPortTypeProxy();
    return massBankAPIPortType;
  }
  
  public massbank.api.xsd.SearchResult searchPeakDiff(java.lang.String[] mzs, java.lang.String relativeIntensity, java.lang.String tolerance, java.lang.String[] instrumentTypes, java.lang.String ionMode, java.lang.Integer maxNumResults) throws java.rmi.RemoteException{
    if (massBankAPIPortType == null)
      _initMassBankAPIPortTypeProxy();
    return massBankAPIPortType.searchPeakDiff(mzs, relativeIntensity, tolerance, instrumentTypes, ionMode, maxNumResults);
  }
  
  public java.lang.String[] getInstrumentTypes() throws java.rmi.RemoteException{
    if (massBankAPIPortType == null)
      _initMassBankAPIPortTypeProxy();
    return massBankAPIPortType.getInstrumentTypes();
  }
  
  public massbank.api.xsd.SearchResult searchSpectrum(java.lang.String[] mzs, java.lang.String[] intensities, java.lang.String unit, java.lang.String tolerance, java.lang.String cutoff, java.lang.String[] instrumentTypes, java.lang.String ionMode, java.lang.Integer maxNumResults) throws java.rmi.RemoteException{
    if (massBankAPIPortType == null)
      _initMassBankAPIPortTypeProxy();
    return massBankAPIPortType.searchSpectrum(mzs, intensities, unit, tolerance, cutoff, instrumentTypes, ionMode, maxNumResults);
  }
  
  public massbank.api.xsd.RecordInfo[] getRecordInfo(java.lang.String[] ids) throws java.rmi.RemoteException{
    if (massBankAPIPortType == null)
      _initMassBankAPIPortTypeProxy();
    return massBankAPIPortType.getRecordInfo(ids);
  }
  
  public massbank.api.xsd.SearchResult searchPeak(java.lang.String[] mzs, java.lang.String relativeIntensity, java.lang.String tolerance, java.lang.String[] instrumentTypes, java.lang.String ionMode, java.lang.Integer maxNumResults) throws java.rmi.RemoteException{
    if (massBankAPIPortType == null)
      _initMassBankAPIPortTypeProxy();
    return massBankAPIPortType.searchPeak(mzs, relativeIntensity, tolerance, instrumentTypes, ionMode, maxNumResults);
  }
  
  public massbank.api.xsd.Peak[] getPeak(java.lang.String[] ids) throws java.rmi.RemoteException{
    if (massBankAPIPortType == null)
      _initMassBankAPIPortTypeProxy();
    return massBankAPIPortType.getPeak(ids);
  }
  
  
}