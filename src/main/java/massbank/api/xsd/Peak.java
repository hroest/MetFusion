/**
 * Peak.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package massbank.api.xsd;

public class Peak  implements java.io.Serializable {
    private java.lang.String id;

    private java.lang.String[] intensities;

    private java.lang.String[] mzs;

    private java.lang.Integer numPeaks;

    public Peak() {
    }

    public Peak(
           java.lang.String id,
           java.lang.String[] intensities,
           java.lang.String[] mzs,
           java.lang.Integer numPeaks) {
           this.id = id;
           this.intensities = intensities;
           this.mzs = mzs;
           this.numPeaks = numPeaks;
    }


    /**
     * Gets the id value for this Peak.
     * 
     * @return id
     */
    public java.lang.String getId() {
        return id;
    }


    /**
     * Sets the id value for this Peak.
     * 
     * @param id
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }


    /**
     * Gets the intensities value for this Peak.
     * 
     * @return intensities
     */
    public java.lang.String[] getIntensities() {
        return intensities;
    }


    /**
     * Sets the intensities value for this Peak.
     * 
     * @param intensities
     */
    public void setIntensities(java.lang.String[] intensities) {
        this.intensities = intensities;
    }

    public java.lang.String getIntensities(int i) {
        return this.intensities[i];
    }

    public void setIntensities(int i, java.lang.String _value) {
        this.intensities[i] = _value;
    }


    /**
     * Gets the mzs value for this Peak.
     * 
     * @return mzs
     */
    public java.lang.String[] getMzs() {
        return mzs;
    }


    /**
     * Sets the mzs value for this Peak.
     * 
     * @param mzs
     */
    public void setMzs(java.lang.String[] mzs) {
        this.mzs = mzs;
    }

    public java.lang.String getMzs(int i) {
        return this.mzs[i];
    }

    public void setMzs(int i, java.lang.String _value) {
        this.mzs[i] = _value;
    }


    /**
     * Gets the numPeaks value for this Peak.
     * 
     * @return numPeaks
     */
    public java.lang.Integer getNumPeaks() {
        return numPeaks;
    }


    /**
     * Sets the numPeaks value for this Peak.
     * 
     * @param numPeaks
     */
    public void setNumPeaks(java.lang.Integer numPeaks) {
        this.numPeaks = numPeaks;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Peak)) return false;
        Peak other = (Peak) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.id==null && other.getId()==null) || 
             (this.id!=null &&
              this.id.equals(other.getId()))) &&
            ((this.intensities==null && other.getIntensities()==null) || 
             (this.intensities!=null &&
              java.util.Arrays.equals(this.intensities, other.getIntensities()))) &&
            ((this.mzs==null && other.getMzs()==null) || 
             (this.mzs!=null &&
              java.util.Arrays.equals(this.mzs, other.getMzs()))) &&
            ((this.numPeaks==null && other.getNumPeaks()==null) || 
             (this.numPeaks!=null &&
              this.numPeaks.equals(other.getNumPeaks())));
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
        if (getId() != null) {
            _hashCode += getId().hashCode();
        }
        if (getIntensities() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getIntensities());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getIntensities(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getMzs() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getMzs());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getMzs(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getNumPeaks() != null) {
            _hashCode += getNumPeaks().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Peak.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://api.massbank/xsd", "Peak"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("id");
        elemField.setXmlName(new javax.xml.namespace.QName("http://api.massbank/xsd", "id"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("intensities");
        elemField.setXmlName(new javax.xml.namespace.QName("http://api.massbank/xsd", "intensities"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mzs");
        elemField.setXmlName(new javax.xml.namespace.QName("http://api.massbank/xsd", "mzs"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("numPeaks");
        elemField.setXmlName(new javax.xml.namespace.QName("http://api.massbank/xsd", "numPeaks"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
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
