/**
 * MassSearchHit.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx;

public class MassSearchHit  extends de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.AdductHit  implements java.io.Serializable {
    private int fID;

    private java.lang.String formula;

    public MassSearchHit() {
    }

    public MassSearchHit(
           org.apache.axis.types.UnsignedByte adduct,
           double monoisotopic_mass,
           double diff,
           float mass_ppm_error,
           int fID,
           java.lang.String formula) {
        super(
            adduct,
            monoisotopic_mass,
            diff,
            mass_ppm_error);
        this.fID = fID;
        this.formula = formula;
    }


    /**
     * Gets the fID value for this MassSearchHit.
     * 
     * @return fID
     */
    public int getFID() {
        return fID;
    }


    /**
     * Sets the fID value for this MassSearchHit.
     * 
     * @param fID
     */
    public void setFID(int fID) {
        this.fID = fID;
    }


    /**
     * Gets the formula value for this MassSearchHit.
     * 
     * @return formula
     */
    public java.lang.String getFormula() {
        return formula;
    }


    /**
     * Sets the formula value for this MassSearchHit.
     * 
     * @param formula
     */
    public void setFormula(java.lang.String formula) {
        this.formula = formula;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof MassSearchHit)) return false;
        MassSearchHit other = (MassSearchHit) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            this.fID == other.getFID() &&
            ((this.formula==null && other.getFormula()==null) || 
             (this.formula!=null &&
              this.formula.equals(other.getFormula())));
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
        _hashCode += getFID();
        if (getFormula() != null) {
            _hashCode += getFormula().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(MassSearchHit.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "MassSearchHit"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("FID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "fID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("formula");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "formula"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
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
