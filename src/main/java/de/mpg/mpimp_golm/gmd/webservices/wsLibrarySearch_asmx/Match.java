/**
 * Match.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx;

public class Match  extends de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.BaseHit  implements java.io.Serializable {
    private float dotproductDistance;

    private float euclideanDistance;

    private int hammingDistance;

    private float jaccardDistance;

    private float s12GowerLegendreDistance;

    public Match() {
    }

    public Match(
           java.lang.String spectrumID,
           java.lang.String analyteID,
           float ri,
           float riDiscrepancy,
           float dotproductDistance,
           float euclideanDistance,
           int hammingDistance,
           float jaccardDistance,
           float s12GowerLegendreDistance) {
        super(
            spectrumID,
            analyteID,
            ri,
            riDiscrepancy);
        this.dotproductDistance = dotproductDistance;
        this.euclideanDistance = euclideanDistance;
        this.hammingDistance = hammingDistance;
        this.jaccardDistance = jaccardDistance;
        this.s12GowerLegendreDistance = s12GowerLegendreDistance;
    }


    /**
     * Gets the dotproductDistance value for this Match.
     * 
     * @return dotproductDistance
     */
    public float getDotproductDistance() {
        return dotproductDistance;
    }


    /**
     * Sets the dotproductDistance value for this Match.
     * 
     * @param dotproductDistance
     */
    public void setDotproductDistance(float dotproductDistance) {
        this.dotproductDistance = dotproductDistance;
    }


    /**
     * Gets the euclideanDistance value for this Match.
     * 
     * @return euclideanDistance
     */
    public float getEuclideanDistance() {
        return euclideanDistance;
    }


    /**
     * Sets the euclideanDistance value for this Match.
     * 
     * @param euclideanDistance
     */
    public void setEuclideanDistance(float euclideanDistance) {
        this.euclideanDistance = euclideanDistance;
    }


    /**
     * Gets the hammingDistance value for this Match.
     * 
     * @return hammingDistance
     */
    public int getHammingDistance() {
        return hammingDistance;
    }


    /**
     * Sets the hammingDistance value for this Match.
     * 
     * @param hammingDistance
     */
    public void setHammingDistance(int hammingDistance) {
        this.hammingDistance = hammingDistance;
    }


    /**
     * Gets the jaccardDistance value for this Match.
     * 
     * @return jaccardDistance
     */
    public float getJaccardDistance() {
        return jaccardDistance;
    }


    /**
     * Sets the jaccardDistance value for this Match.
     * 
     * @param jaccardDistance
     */
    public void setJaccardDistance(float jaccardDistance) {
        this.jaccardDistance = jaccardDistance;
    }


    /**
     * Gets the s12GowerLegendreDistance value for this Match.
     * 
     * @return s12GowerLegendreDistance
     */
    public float getS12GowerLegendreDistance() {
        return s12GowerLegendreDistance;
    }


    /**
     * Sets the s12GowerLegendreDistance value for this Match.
     * 
     * @param s12GowerLegendreDistance
     */
    public void setS12GowerLegendreDistance(float s12GowerLegendreDistance) {
        this.s12GowerLegendreDistance = s12GowerLegendreDistance;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Match)) return false;
        Match other = (Match) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            this.dotproductDistance == other.getDotproductDistance() &&
            this.euclideanDistance == other.getEuclideanDistance() &&
            this.hammingDistance == other.getHammingDistance() &&
            this.jaccardDistance == other.getJaccardDistance() &&
            this.s12GowerLegendreDistance == other.getS12GowerLegendreDistance();
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
        _hashCode += new Float(getDotproductDistance()).hashCode();
        _hashCode += new Float(getEuclideanDistance()).hashCode();
        _hashCode += getHammingDistance();
        _hashCode += new Float(getJaccardDistance()).hashCode();
        _hashCode += new Float(getS12GowerLegendreDistance()).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Match.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "Match"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dotproductDistance");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "DotproductDistance"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("euclideanDistance");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "EuclideanDistance"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("hammingDistance");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "HammingDistance"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jaccardDistance");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "JaccardDistance"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("s12GowerLegendreDistance");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "s12GowerLegendreDistance"));
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
