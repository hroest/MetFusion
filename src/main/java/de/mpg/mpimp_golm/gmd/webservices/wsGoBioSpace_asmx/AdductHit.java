/**
 * AdductHit.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx;

public class AdductHit  implements java.io.Serializable {
    private org.apache.axis.types.UnsignedByte adduct;

    private double monoisotopic_mass;

    private double diff;

    private float mass_ppm_error;

    public AdductHit() {
    }

    public AdductHit(
           org.apache.axis.types.UnsignedByte adduct,
           double monoisotopic_mass,
           double diff,
           float mass_ppm_error) {
           this.adduct = adduct;
           this.monoisotopic_mass = monoisotopic_mass;
           this.diff = diff;
           this.mass_ppm_error = mass_ppm_error;
    }


    /**
     * Gets the adduct value for this AdductHit.
     * 
     * @return adduct
     */
    public org.apache.axis.types.UnsignedByte getAdduct() {
        return adduct;
    }


    /**
     * Sets the adduct value for this AdductHit.
     * 
     * @param adduct
     */
    public void setAdduct(org.apache.axis.types.UnsignedByte adduct) {
        this.adduct = adduct;
    }


    /**
     * Gets the monoisotopic_mass value for this AdductHit.
     * 
     * @return monoisotopic_mass
     */
    public double getMonoisotopic_mass() {
        return monoisotopic_mass;
    }


    /**
     * Sets the monoisotopic_mass value for this AdductHit.
     * 
     * @param monoisotopic_mass
     */
    public void setMonoisotopic_mass(double monoisotopic_mass) {
        this.monoisotopic_mass = monoisotopic_mass;
    }


    /**
     * Gets the diff value for this AdductHit.
     * 
     * @return diff
     */
    public double getDiff() {
        return diff;
    }


    /**
     * Sets the diff value for this AdductHit.
     * 
     * @param diff
     */
    public void setDiff(double diff) {
        this.diff = diff;
    }


    /**
     * Gets the mass_ppm_error value for this AdductHit.
     * 
     * @return mass_ppm_error
     */
    public float getMass_ppm_error() {
        return mass_ppm_error;
    }


    /**
     * Sets the mass_ppm_error value for this AdductHit.
     * 
     * @param mass_ppm_error
     */
    public void setMass_ppm_error(float mass_ppm_error) {
        this.mass_ppm_error = mass_ppm_error;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AdductHit)) return false;
        AdductHit other = (AdductHit) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.adduct==null && other.getAdduct()==null) || 
             (this.adduct!=null &&
              this.adduct.equals(other.getAdduct()))) &&
            this.monoisotopic_mass == other.getMonoisotopic_mass() &&
            this.diff == other.getDiff() &&
            this.mass_ppm_error == other.getMass_ppm_error();
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
        if (getAdduct() != null) {
            _hashCode += getAdduct().hashCode();
        }
        _hashCode += new Double(getMonoisotopic_mass()).hashCode();
        _hashCode += new Double(getDiff()).hashCode();
        _hashCode += new Float(getMass_ppm_error()).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(AdductHit.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "AdductHit"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("adduct");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "adduct"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "unsignedByte"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("monoisotopic_mass");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "monoisotopic_mass"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("diff");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "diff"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mass_ppm_error");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "mass_ppm_error"));
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
