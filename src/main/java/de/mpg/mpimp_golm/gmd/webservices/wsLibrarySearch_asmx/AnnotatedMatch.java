/**
 * AnnotatedMatch.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx;

public class AnnotatedMatch  extends de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.Match  implements java.io.Serializable {
    private java.lang.String spectrumName;

    private java.lang.String analyteName;

    private java.lang.String metaboliteID;

    public AnnotatedMatch() {
    }

    public AnnotatedMatch(
           java.lang.String spectrumID,
           java.lang.String analyteID,
           float ri,
           float riDiscrepancy,
           float dotproductDistance,
           float euclideanDistance,
           int hammingDistance,
           float jaccardDistance,
           float s12GowerLegendreDistance,
           java.lang.String spectrumName,
           java.lang.String analyteName,
           java.lang.String metaboliteID) {
        super(
            spectrumID,
            analyteID,
            ri,
            riDiscrepancy,
            dotproductDistance,
            euclideanDistance,
            hammingDistance,
            jaccardDistance,
            s12GowerLegendreDistance);
        this.spectrumName = spectrumName;
        this.analyteName = analyteName;
        this.metaboliteID = metaboliteID;
    }


    /**
     * Gets the spectrumName value for this AnnotatedMatch.
     * 
     * @return spectrumName
     */
    public java.lang.String getSpectrumName() {
        return spectrumName;
    }


    /**
     * Sets the spectrumName value for this AnnotatedMatch.
     * 
     * @param spectrumName
     */
    public void setSpectrumName(java.lang.String spectrumName) {
        this.spectrumName = spectrumName;
    }


    /**
     * Gets the analyteName value for this AnnotatedMatch.
     * 
     * @return analyteName
     */
    public java.lang.String getAnalyteName() {
        return analyteName;
    }


    /**
     * Sets the analyteName value for this AnnotatedMatch.
     * 
     * @param analyteName
     */
    public void setAnalyteName(java.lang.String analyteName) {
        this.analyteName = analyteName;
    }


    /**
     * Gets the metaboliteID value for this AnnotatedMatch.
     * 
     * @return metaboliteID
     */
    public java.lang.String getMetaboliteID() {
        return metaboliteID;
    }


    /**
     * Sets the metaboliteID value for this AnnotatedMatch.
     * 
     * @param metaboliteID
     */
    public void setMetaboliteID(java.lang.String metaboliteID) {
        this.metaboliteID = metaboliteID;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AnnotatedMatch)) return false;
        AnnotatedMatch other = (AnnotatedMatch) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.spectrumName==null && other.getSpectrumName()==null) || 
             (this.spectrumName!=null &&
              this.spectrumName.equals(other.getSpectrumName()))) &&
            ((this.analyteName==null && other.getAnalyteName()==null) || 
             (this.analyteName!=null &&
              this.analyteName.equals(other.getAnalyteName()))) &&
            ((this.metaboliteID==null && other.getMetaboliteID()==null) || 
             (this.metaboliteID!=null &&
              this.metaboliteID.equals(other.getMetaboliteID())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = super.hashCode();
        if (getSpectrumName() != null) {
            _hashCode += getSpectrumName().hashCode();
        }
        if (getAnalyteName() != null) {
            _hashCode += getAnalyteName().hashCode();
        }
        if (getMetaboliteID() != null) {
            _hashCode += getMetaboliteID().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(AnnotatedMatch.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "AnnotatedMatch"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("spectrumName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "spectrumName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("analyteName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "analyteName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("metaboliteID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "metaboliteID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
