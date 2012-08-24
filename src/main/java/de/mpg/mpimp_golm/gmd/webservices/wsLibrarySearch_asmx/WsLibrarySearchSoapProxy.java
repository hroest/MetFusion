package de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx;

public class WsLibrarySearchSoapProxy implements de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.WsLibrarySearchSoap {
  private String _endpoint = null;
  private de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.WsLibrarySearchSoap wsLibrarySearchSoap = null;
  
  public WsLibrarySearchSoapProxy() {
    _initWsLibrarySearchSoapProxy();
  }
  
  public WsLibrarySearchSoapProxy(String endpoint) {
    _endpoint = endpoint;
    _initWsLibrarySearchSoapProxy();
  }
  
  private void _initWsLibrarySearchSoapProxy() {
    try {
      wsLibrarySearchSoap = (new de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.WsLibrarySearchLocator()).getwsLibrarySearchSoap();
      if (wsLibrarySearchSoap != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)wsLibrarySearchSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)wsLibrarySearchSoap)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (wsLibrarySearchSoap != null)
      ((javax.xml.rpc.Stub)wsLibrarySearchSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.WsLibrarySearchSoap getWsLibrarySearchSoap() {
    if (wsLibrarySearchSoap == null)
      _initWsLibrarySearchSoapProxy();
    return wsLibrarySearchSoap;
  }
  
  public de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.ResultOfAnnotatedMatch librarySearch(float ri, float riWindow, de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.AlkaneRetentionIndexGcColumnComposition alkaneRetentionIndexGcColumnComposition, java.lang.String spectrum) throws java.rmi.RemoteException{
    if (wsLibrarySearchSoap == null)
      _initWsLibrarySearchSoapProxy();
    return wsLibrarySearchSoap.librarySearch(ri, riWindow, alkaneRetentionIndexGcColumnComposition, spectrum);
  }
  
  public java.lang.String MPIMP_Quad_Name(java.lang.String spectrumID) throws java.rmi.RemoteException{
    if (wsLibrarySearchSoap == null)
      _initWsLibrarySearchSoapProxy();
    return wsLibrarySearchSoap.MPIMP_Quad_Name(spectrumID);
  }
  
  public java.lang.String[] getCompoundNames(java.lang.String prefixText, int count) throws java.rmi.RemoteException{
    if (wsLibrarySearchSoap == null)
      _initWsLibrarySearchSoapProxy();
    return wsLibrarySearchSoap.getCompoundNames(prefixText, count);
  }
  
  
}