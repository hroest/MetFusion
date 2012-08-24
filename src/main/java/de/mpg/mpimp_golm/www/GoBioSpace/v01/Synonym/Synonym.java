/**
 * Synonym.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.mpg.mpimp_golm.www.GoBioSpace.v01.Synonym;

public class Synonym  implements java.io.Serializable {
    private java.lang.String value;

    private int ID;  // attribute

    private int formulaID;  // attribute

    private org.apache.axis.types.UnsignedByte propertyID;  // attribute

    private short depositorID;  // attribute

    private java.lang.String depositorsPrimaryKey;  // attribute

    public Synonym() {
    }

    public Synonym(
           java.lang.String value,
           int ID,
           int formulaID,
           org.apache.axis.types.UnsignedByte propertyID,
           short depositorID,
           java.lang.String depositorsPrimaryKey) {
           this.value = value;
           this.ID = ID;
           this.formulaID = formulaID;
           this.propertyID = propertyID;
           this.depositorID = depositorID;
           this.depositorsPrimaryKey = depositorsPrimaryKey;
    }


    /**
     * Gets the value value for this Synonym.
     * 
     * @return value
     */
    public java.lang.String getValue() {
        return value;
    }


    /**
     * Sets the value value for this Synonym.
     * 
     * @param value
     */
    public void setValue(java.lang.String value) {
        this.value = value;
    }


    /**
     * Gets the ID value for this Synonym.
     * 
     * @return ID
     */
    public int getID() {
        return ID;
    }


    /**
     * Sets the ID value for this Synonym.
     * 
     * @param ID
     */
    public void setID(int ID) {
        this.ID = ID;
    }


    /**
     * Gets the formulaID value for this Synonym.
     * 
     * @return formulaID
     */
    public int getFormulaID() {
        return formulaID;
    }


    /**
     * Sets the formulaID value for this Synonym.
     * 
     * @param formulaID
     */
    public void setFormulaID(int formulaID) {
        this.formulaID = formulaID;
    }


    /**
     * Gets the propertyID value for this Synonym.
     * 
     * @return propertyID
     */
    public org.apache.axis.types.UnsignedByte getPropertyID() {
        return propertyID;
    }


    /**
     * Sets the propertyID value for this Synonym.
     * 
     * @param propertyID
     */
    public void setPropertyID(org.apache.axis.types.UnsignedByte propertyID) {
        this.propertyID = propertyID;
    }


    /**
     * Gets the depositorID value for this Synonym.
     * 
     * @return depositorID
     */
    public short getDepositorID() {
        return depositorID;
    }


    /**
     * Sets the depositorID value for this Synonym.
     * 
     * @param depositorID
     */
    public void setDepositorID(short depositorID) {
        this.depositorID = depositorID;
    }


    /**
     * Gets the depositorsPrimaryKey value for this Synonym.
     * 
     * @return depositorsPrimaryKey
     */
    public java.lang.String getDepositorsPrimaryKey() {
        return depositorsPrimaryKey;
    }


    /**
     * Sets the depositorsPrimaryKey value for this Synonym.
     * 
     * @param depositorsPrimaryKey
     */
    public void setDepositorsPrimaryKey(java.lang.String depositorsPrimaryKey) {
        this.depositorsPrimaryKey = depositorsPrimaryKey;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Synonym)) return false;
        Synonym other = (Synonym) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.value==null && other.getValue()==null) || 
             (this.value!=null &&
              this.value.equals(other.getValue()))) &&
            this.ID == other.getID() &&
            this.formulaID == other.getFormulaID() &&
            ((this.propertyID==null && other.getPropertyID()==null) || 
             (this.propertyID!=null &&
              this.propertyID.equals(other.getPropertyID()))) &&
            this.depositorID == other.getDepositorID() &&
            ((this.depositorsPrimaryKey==null && other.getDepositorsPrimaryKey()==null) || 
             (this.depositorsPrimaryKey!=null &&
              this.depositorsPrimaryKey.equals(other.getDepositorsPrimaryKey())));
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
        if (getValue() != null) {
            _hashCode += getValue().hashCode();
        }
        _hashCode += getID();
        _hashCode += getFormulaID();
        if (getPropertyID() != null) {
            _hashCode += getPropertyID().hashCode();
        }
        _hashCode += getDepositorID();
        if (getDepositorsPrimaryKey() != null) {
            _hashCode += getDepositorsPrimaryKey().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Synonym.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.mpimp-golm.mpg.de/GoBioSpace/v01/Synonym", "Synonym"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("ID");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ID"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("formulaID");
        attrField.setXmlName(new javax.xml.namespace.QName("", "FormulaID"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("propertyID");
        attrField.setXmlName(new javax.xml.namespace.QName("", "PropertyID"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "unsignedByte"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("depositorID");
        attrField.setXmlName(new javax.xml.namespace.QName("", "DepositorID"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "short"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("depositorsPrimaryKey");
        attrField.setXmlName(new javax.xml.namespace.QName("", "DepositorsPrimaryKey"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("value");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.mpimp-golm.mpg.de/GoBioSpace/v01/Synonym", "value"));
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
