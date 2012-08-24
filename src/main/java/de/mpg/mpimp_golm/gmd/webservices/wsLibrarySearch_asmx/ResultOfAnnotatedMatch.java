/**
 * ResultOfAnnotatedMatch.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx;

public class ResultOfAnnotatedMatch  implements java.io.Serializable {
    private de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.Status status;

    private java.lang.String message;

    private de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.AnnotatedMatch[] results;

    private java.lang.String processingTime;

    private de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.TimeSpan elapsedTime;

    public ResultOfAnnotatedMatch() {
    }

    public ResultOfAnnotatedMatch(
           de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.Status status,
           java.lang.String message,
           de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.AnnotatedMatch[] results,
           java.lang.String processingTime,
           de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.TimeSpan elapsedTime) {
           this.status = status;
           this.message = message;
           this.results = results;
           this.processingTime = processingTime;
           this.elapsedTime = elapsedTime;
    }


    /**
     * Gets the status value for this ResultOfAnnotatedMatch.
     * 
     * @return status
     */
    public de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.Status getStatus() {
        return status;
    }


    /**
     * Sets the status value for this ResultOfAnnotatedMatch.
     * 
     * @param status
     */
    public void setStatus(de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.Status status) {
        this.status = status;
    }


    /**
     * Gets the message value for this ResultOfAnnotatedMatch.
     * 
     * @return message
     */
    public java.lang.String getMessage() {
        return message;
    }


    /**
     * Sets the message value for this ResultOfAnnotatedMatch.
     * 
     * @param message
     */
    public void setMessage(java.lang.String message) {
        this.message = message;
    }


    /**
     * Gets the results value for this ResultOfAnnotatedMatch.
     * 
     * @return results
     */
    public de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.AnnotatedMatch[] getResults() {
        return results;
    }


    /**
     * Sets the results value for this ResultOfAnnotatedMatch.
     * 
     * @param results
     */
    public void setResults(de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.AnnotatedMatch[] results) {
        this.results = results;
    }

    public de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.AnnotatedMatch getResults(int i) {
        return this.results[i];
    }

    public void setResults(int i, de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.AnnotatedMatch _value) {
        this.results[i] = _value;
    }


    /**
     * Gets the processingTime value for this ResultOfAnnotatedMatch.
     * 
     * @return processingTime
     */
    public java.lang.String getProcessingTime() {
        return processingTime;
    }


    /**
     * Sets the processingTime value for this ResultOfAnnotatedMatch.
     * 
     * @param processingTime
     */
    public void setProcessingTime(java.lang.String processingTime) {
        this.processingTime = processingTime;
    }


    /**
     * Gets the elapsedTime value for this ResultOfAnnotatedMatch.
     * 
     * @return elapsedTime
     */
    public de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.TimeSpan getElapsedTime() {
        return elapsedTime;
    }


    /**
     * Sets the elapsedTime value for this ResultOfAnnotatedMatch.
     * 
     * @param elapsedTime
     */
    public void setElapsedTime(de.mpg.mpimp_golm.gmd.webservices.wsLibrarySearch_asmx.TimeSpan elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ResultOfAnnotatedMatch)) return false;
        ResultOfAnnotatedMatch other = (ResultOfAnnotatedMatch) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.status==null && other.getStatus()==null) || 
             (this.status!=null &&
              this.status.equals(other.getStatus()))) &&
            ((this.message==null && other.getMessage()==null) || 
             (this.message!=null &&
              this.message.equals(other.getMessage()))) &&
            ((this.results==null && other.getResults()==null) || 
             (this.results!=null &&
              java.util.Arrays.equals(this.results, other.getResults()))) &&
            ((this.processingTime==null && other.getProcessingTime()==null) || 
             (this.processingTime!=null &&
              this.processingTime.equals(other.getProcessingTime()))) &&
            ((this.elapsedTime==null && other.getElapsedTime()==null) || 
             (this.elapsedTime!=null &&
              this.elapsedTime.equals(other.getElapsedTime())));
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
        if (getStatus() != null) {
            _hashCode += getStatus().hashCode();
        }
        if (getMessage() != null) {
            _hashCode += getMessage().hashCode();
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
        if (getProcessingTime() != null) {
            _hashCode += getProcessingTime().hashCode();
        }
        if (getElapsedTime() != null) {
            _hashCode += getElapsedTime().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ResultOfAnnotatedMatch.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "ResultOfAnnotatedMatch"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("status");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "Status"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "Status"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("message");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "Message"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("results");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "Results"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "AnnotatedMatch"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("processingTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "ProcessingTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("elapsedTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "ElapsedTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsLibrarySearch.asmx/", "TimeSpan"));
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
