/**
 * WsGoBioSpaceSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx;

public interface WsGoBioSpaceSoap extends java.rmi.Remote {

    /**
     * Retrieve all depositors.
     */
    public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Depositor[] getDepositors() throws java.rmi.RemoteException;

    /**
     * Retrieve all properties.
     */
    public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Property[] getProperties() throws java.rmi.RemoteException;

    /**
     * Retrieve all adducts.
     */
    public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Adduct[] getAdducts() throws java.rmi.RemoteException;

    /**
     * Create a new session based on given depositor and adduct IDs.
     * The returned identifier for the session must be subsequently used
     * in each search and is valid for 24 hours befor all settings are removed
     * from the database.
     *                             The session identifier '00000000-0000-0000-0000-000000000000'
     * indicates an error during data processing (most likely due to server
     * overload).
     */
    public java.lang.String createSession(short[] depositorIds, byte[] adductIds) throws java.rmi.RemoteException;

    /**
     * Remove a session from the database if not needed any more (good
     * practise). Otherwise, sessions will be automatically removed 24 hours
     * after creation.
     */
    public void purgeSession(java.lang.String sessionID) throws java.rmi.RemoteException;

    /**
     * Search a single mass against GoBioSpace where all masses are
     * calculated for <b>none isotopically labeled</b> sum formula.
     */
    public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[] searchMass12C(java.lang.String sessionID, float mass, float tolerance) throws java.rmi.RemoteException;

    /**
     * Search an array of masses against GoBioSpace. The result is
     * an array of MassSearchHits where the i-th MassSearchHit result in
     * the returned array corrresponds to the i-th mass in the input array.<br
     * /> 
     *                             The toleranceSelector specifies the kind
     * of tolerance (ppm or absolute) used in the search. In case of an absolute
     * tolerance, the tolerance value is used for all masses. In contrast
     * to the absolute tolerance, the ppm tolerance is specific for each
     * single mass. Here, each single mass is divided by 1,000,000 and multiplied
     * by the tolerance value to obtain the actual mass deviation tolerated
     * in the search.
     */
    public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[][] searchMass12C_Bulk(java.lang.String sessionID, float[] masses, float tolerance, de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.ToleranceType toleranceSelector) throws java.rmi.RemoteException;

    /**
     * Search a single mass against GoBioSpace where all masses are
     * calculated for fully <b><sup>13</sup>C</b> isotopic labeled sum formula.
     */
    public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[] searchMass13C(java.lang.String sessionID, float mass, float tolerance) throws java.rmi.RemoteException;

    /**
     * Search a single mass against GoBioSpace where all masses are
     * calculated for fully <b><sup>15</sup>N</b> isotopic labeled sum formula.
     */
    public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[] searchMass15N(java.lang.String sessionID, float mass, float tolerance) throws java.rmi.RemoteException;

    /**
     * Search a single mass against GoBioSpace where all masses are
     * calculated for fully <b><sup>34</sup>S</b> isotopic labeled sum formula.
     */
    public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[] searchMass34S(java.lang.String sessionID, float mass, float tolerance) throws java.rmi.RemoteException;

    /**
     * Returns all synonyms for a given formula and a set of depositors
     * (specified in the session before).
     */
    public de.mpg.mpimp_golm.www.GoBioSpace.v01.Synonym.Synonym[] getSynonyms(java.lang.String sessionID, int formulaID) throws java.rmi.RemoteException;

    /**
     * Returns the monoisotopic weight of a given '<b>sum formula</b>'.
     */
    public double getMonoisotopicWeight(java.lang.String formula) throws java.rmi.RemoteException;

    /**
     * Returns the molecular weight of a given '<b>sum formula</b>'.
     */
    public double getMolecularWeight(java.lang.String formula) throws java.rmi.RemoteException;

    /**
     * Returns the GoBioSpace ID for a given '<b>sum formula</b>'
     * in <a href="http://en.wikipedia.org/wiki/Hill_system">Hill notation</a>.
     */
    public int getFormulaID(java.lang.String formula) throws java.rmi.RemoteException;
}
