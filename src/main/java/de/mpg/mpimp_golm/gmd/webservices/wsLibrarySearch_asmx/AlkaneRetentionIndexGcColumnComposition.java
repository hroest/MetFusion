/**
 * AlkaneRetentionIndexGcColumnComposition.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx;

public class AlkaneRetentionIndexGcColumnComposition implements java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected AlkaneRetentionIndexGcColumnComposition(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final java.lang.String _none = "none";
    public static final java.lang.String _VAR5 = "VAR5";
    public static final java.lang.String _MDN35 = "MDN35";
    public static final AlkaneRetentionIndexGcColumnComposition none = new AlkaneRetentionIndexGcColumnComposition(_none);
    public static final AlkaneRetentionIndexGcColumnComposition VAR5 = new AlkaneRetentionIndexGcColumnComposition(_VAR5);
    public static final AlkaneRetentionIndexGcColumnComposition MDN35 = new AlkaneRetentionIndexGcColumnComposition(_MDN35);
    public java.lang.String getValue() { return _value_;}
    public static AlkaneRetentionIndexGcColumnComposition fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        AlkaneRetentionIndexGcColumnComposition enumeration = (AlkaneRetentionIndexGcColumnComposition)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static AlkaneRetentionIndexGcColumnComposition fromString(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        return fromValue(value);
    }
    public boolean equals(java.lang.Object obj) {return (obj == this);}
    public int hashCode() { return toString().hashCode();}
    public java.lang.String toString() { return _value_;}
    public java.lang.Object readResolve() throws java.io.ObjectStreamException { return fromValue(_value_);}
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumSerializer(
            _javaType, _xmlType);
    }
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumDeserializer(
            _javaType, _xmlType);
    }
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(AlkaneRetentionIndexGcColumnComposition.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "AlkaneRetentionIndexGcColumnComposition"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
