/**
 * SearchResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package massbank.api.xsd;

public class SearchResult  implements java.io.Serializable {
    private java.lang.Integer numResults;

    private massbank.api.xsd.Result[] results;

    public SearchResult() {
    }

    public SearchResult(
           java.lang.Integer numResults,
           massbank.api.xsd.Result[] results) {
           this.numResults = numResults;
           this.results = results;
    }


    /**
     * Gets the numResults value for this SearchResult.
     * 
     * @return numResults
     */
    public java.lang.Integer getNumResults() {
        return numResults;
    }


    /**
     * Sets the numResults value for this SearchResult.
     * 
     * @param numResults
     */
    public void setNumResults(java.lang.Integer numResults) {
        this.numResults = numResults;
    }


    /**
     * Gets the results value for this SearchResult.
     * 
     * @return results
     */
    public massbank.api.xsd.Result[] getResults() {
        return results;
    }


    /**
     * Sets the results value for this SearchResult.
     * 
     * @param results
     */
    public void setResults(massbank.api.xsd.Result[] results) {
        this.results = results;
    }

    public massbank.api.xsd.Result getResults(int i) {
        return this.results[i];
    }

    public void setResults(int i, massbank.api.xsd.Result _value) {
        this.results[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SearchResult)) return false;
        SearchResult other = (SearchResult) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.numResults==null && other.getNumResults()==null) || 
             (this.numResults!=null &&
              this.numResults.equals(other.getNumResults()))) &&
            ((this.results==null && other.getResults()==null) || 
             (this.results!=null &&
              java.util.Arrays.equals(this.results, other.getResults())));
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
        if (getNumResults() != null) {
            _hashCode += getNumResults().hashCode();
        }
        if (getResults() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getResults());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getResults(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SearchResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://api.massbank/xsd", "SearchResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("numResults");
        elemField.setXmlName(new javax.xml.namespace.QName("http://api.massbank/xsd", "numResults"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("results");
        elemField.setXmlName(new javax.xml.namespace.QName("http://api.massbank/xsd", "results"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://api.massbank/xsd", "Result"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
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
