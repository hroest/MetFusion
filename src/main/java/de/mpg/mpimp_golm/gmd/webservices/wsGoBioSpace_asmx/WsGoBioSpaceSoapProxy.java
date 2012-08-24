package de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx;

public class WsGoBioSpaceSoapProxy implements de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.WsGoBioSpaceSoap {
  private String _endpoint = null;
  private de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.WsGoBioSpaceSoap wsGoBioSpaceSoap = null;
  
  public WsGoBioSpaceSoapProxy() {
    _initWsGoBioSpaceSoapProxy();
  }
  
  public WsGoBioSpaceSoapProxy(String endpoint) {
    _endpoint = endpoint;
    _initWsGoBioSpaceSoapProxy();
  }
  
  private void _initWsGoBioSpaceSoapProxy() {
    try {
      wsGoBioSpaceSoap = (new de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.WsGoBioSpaceLocator()).getwsGoBioSpaceSoap();
      if (wsGoBioSpaceSoap != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)wsGoBioSpaceSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)wsGoBioSpaceSoap)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (wsGoBioSpaceSoap != null)
      ((javax.xml.rpc.Stub)wsGoBioSpaceSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.WsGoBioSpaceSoap getWsGoBioSpaceSoap() {
    if (wsGoBioSpaceSoap == null)
      _initWsGoBioSpaceSoapProxy();
    return wsGoBioSpaceSoap;
  }
  
  public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Depositor[] getDepositors() throws java.rmi.RemoteException{
    if (wsGoBioSpaceSoap == null)
      _initWsGoBioSpaceSoapProxy();
    return wsGoBioSpaceSoap.getDepositors();
  }
  
  public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Property[] getProperties() throws java.rmi.RemoteException{
    if (wsGoBioSpaceSoap == null)
      _initWsGoBioSpaceSoapProxy();
    return wsGoBioSpaceSoap.getProperties();
  }
  
  public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Adduct[] getAdducts() throws java.rmi.RemoteException{
    if (wsGoBioSpaceSoap == null)
      _initWsGoBioSpaceSoapProxy();
    return wsGoBioSpaceSoap.getAdducts();
  }
  
  public java.lang.String createSession(short[] depositorIds, byte[] adductIds) throws java.rmi.RemoteException{
    if (wsGoBioSpaceSoap == null)
      _initWsGoBioSpaceSoapProxy();
    return wsGoBioSpaceSoap.createSession(depositorIds, adductIds);
  }
  
  public void purgeSession(java.lang.String sessionID) throws java.rmi.RemoteException{
    if (wsGoBioSpaceSoap == null)
      _initWsGoBioSpaceSoapProxy();
    wsGoBioSpaceSoap.purgeSession(sessionID);
  }
  
  public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[] searchMass12C(java.lang.String sessionID, float mass, float tolerance) throws java.rmi.RemoteException{
    if (wsGoBioSpaceSoap == null)
      _initWsGoBioSpaceSoapProxy();
    return wsGoBioSpaceSoap.searchMass12C(sessionID, mass, tolerance);
  }
  
  public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[][] searchMass12C_Bulk(java.lang.String sessionID, float[] masses, float tolerance, de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.ToleranceType toleranceSelector) throws java.rmi.RemoteException{
    if (wsGoBioSpaceSoap == null)
      _initWsGoBioSpaceSoapProxy();
    return wsGoBioSpaceSoap.searchMass12C_Bulk(sessionID, masses, tolerance, toleranceSelector);
  }
  
  public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[] searchMass13C(java.lang.String sessionID, float mass, float tolerance) throws java.rmi.RemoteException{
    if (wsGoBioSpaceSoap == null)
      _initWsGoBioSpaceSoapProxy();
    return wsGoBioSpaceSoap.searchMass13C(sessionID, mass, tolerance);
  }
  
  public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[] searchMass15N(java.lang.String sessionID, float mass, float tolerance) throws java.rmi.RemoteException{
    if (wsGoBioSpaceSoap == null)
      _initWsGoBioSpaceSoapProxy();
    return wsGoBioSpaceSoap.searchMass15N(sessionID, mass, tolerance);
  }
  
  public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[] searchMass34S(java.lang.String sessionID, float mass, float tolerance) throws java.rmi.RemoteException{
    if (wsGoBioSpaceSoap == null)
      _initWsGoBioSpaceSoapProxy();
    return wsGoBioSpaceSoap.searchMass34S(sessionID, mass, tolerance);
  }
  
  public de.mpg.mpimp_golm.www.GoBioSpace.v01.Synonym.Synonym[] getSynonyms(java.lang.String sessionID, int formulaID) throws java.rmi.RemoteException{
    if (wsGoBioSpaceSoap == null)
      _initWsGoBioSpaceSoapProxy();
    return wsGoBioSpaceSoap.getSynonyms(sessionID, formulaID);
  }
  
  public double getMonoisotopicWeight(java.lang.String formula) throws java.rmi.RemoteException{
    if (wsGoBioSpaceSoap == null)
      _initWsGoBioSpaceSoapProxy();
    return wsGoBioSpaceSoap.getMonoisotopicWeight(formula);
  }
  
  public double getMolecularWeight(java.lang.String formula) throws java.rmi.RemoteException{
    if (wsGoBioSpaceSoap == null)
      _initWsGoBioSpaceSoapProxy();
    return wsGoBioSpaceSoap.getMolecularWeight(formula);
  }
  
  public int getFormulaID(java.lang.String formula) throws java.rmi.RemoteException{
    if (wsGoBioSpaceSoap == null)
      _initWsGoBioSpaceSoapProxy();
    return wsGoBioSpaceSoap.getFormulaID(formula);
  }
  
  
}