/**
 * BaseHit.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx;

public class BaseHit  implements java.io.Serializable {
    private java.lang.String spectrumID;

    private java.lang.String analyteID;

    private float ri;

    private float riDiscrepancy;

    public BaseHit() {
    }

    public BaseHit(
           java.lang.String spectrumID,
           java.lang.String analyteID,
           float ri,
           float riDiscrepancy) {
           this.spectrumID = spectrumID;
           this.analyteID = analyteID;
           this.ri = ri;
           this.riDiscrepancy = riDiscrepancy;
    }


    /**
     * Gets the spectrumID value for this BaseHit.
     * 
     * @return spectrumID
     */
    public java.lang.String getSpectrumID() {
        return spectrumID;
    }


    /**
     * Sets the spectrumID value for this BaseHit.
     * 
     * @param spectrumID
     */
    public void setSpectrumID(java.lang.String spectrumID) {
        this.spectrumID = spectrumID;
    }


    /**
     * Gets the analyteID value for this BaseHit.
     * 
     * @return analyteID
     */
    public java.lang.String getAnalyteID() {
        return analyteID;
    }


    /**
     * Sets the analyteID value for this BaseHit.
     * 
     * @param analyteID
     */
    public void setAnalyteID(java.lang.String analyteID) {
        this.analyteID = analyteID;
    }


    /**
     * Gets the ri value for this BaseHit.
     * 
     * @return ri
     */
    public float getRi() {
        return ri;
    }


    /**
     * Sets the ri value for this BaseHit.
     * 
     * @param ri
     */
    public void setRi(float ri) {
        this.ri = ri;
    }


    /**
     * Gets the riDiscrepancy value for this BaseHit.
     * 
     * @return riDiscrepancy
     */
    public float getRiDiscrepancy() {
        return riDiscrepancy;
    }


    /**
     * Sets the riDiscrepancy value for this BaseHit.
     * 
     * @param riDiscrepancy
     */
    public void setRiDiscrepancy(float riDiscrepancy) {
        this.riDiscrepancy = riDiscrepancy;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof BaseHit)) return false;
        BaseHit other = (BaseHit) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.spectrumID==null && other.getSpectrumID()==null) || 
             (this.spectrumID!=null &&
              this.spectrumID.equals(other.getSpectrumID()))) &&
            ((this.analyteID==null && other.getAnalyteID()==null) || 
             (this.analyteID!=null &&
              this.analyteID.equals(other.getAnalyteID()))) &&
            this.ri == other.getRi() &&
            this.riDiscrepancy == other.getRiDiscrepancy();
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getSpectrumID() != null) {
            _hashCode += getSpectrumID().hashCode();
        }
        if (getAnalyteID() != null) {
            _hashCode += getAnalyteID().hashCode();
        }
        _hashCode += new Float(getRi()).hashCode();
        _hashCode += new Float(getRiDiscrepancy()).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(BaseHit.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "BaseHit"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("spectrumID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "spectrumID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("analyteID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "analyteID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ri");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "ri"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("riDiscrepancy");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "riDiscrepancy"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
