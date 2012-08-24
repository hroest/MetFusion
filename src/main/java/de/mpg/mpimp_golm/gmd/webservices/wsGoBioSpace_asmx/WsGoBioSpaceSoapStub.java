/**
 * WsGoBioSpaceSoapStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx;

public class WsGoBioSpaceSoapStub extends org.apache.axis.client.Stub implements de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.WsGoBioSpaceSoap {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[14];
        _initOperationDesc1();
        _initOperationDesc2();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetDepositors");
        oper.setReturnType(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "ArrayOfDepositor"));
        oper.setReturnClass(de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Depositor[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "GetDepositorsResult"));
        param = oper.getReturnParamDesc();
        param.setItemQName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "Depositor"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetProperties");
        oper.setReturnType(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "ArrayOfProperty"));
        oper.setReturnClass(de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Property[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "GetPropertiesResult"));
        param = oper.getReturnParamDesc();
        param.setItemQName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "Property"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetAdducts");
        oper.setReturnType(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "ArrayOfAdduct"));
        oper.setReturnClass(de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Adduct[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "GetAdductsResult"));
        param = oper.getReturnParamDesc();
        param.setItemQName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "Adduct"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("CreateSession");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "DepositorIds"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "ArrayOfShort"), short[].class, false, false);
        param.setItemQName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "short"));
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "AdductIds"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "base64Binary"), byte[].class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://microsoft.com/wsdl/types/", "guid"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "CreateSessionResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("PurgeSession");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "SessionID"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://microsoft.com/wsdl/types/", "guid"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("SearchMass12C");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "SessionID"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://microsoft.com/wsdl/types/", "guid"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "mass"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"), float.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "tolerance"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"), float.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "ArrayOfMassSearchHit"));
        oper.setReturnClass(de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "SearchMass12CResult"));
        param = oper.getReturnParamDesc();
        param.setItemQName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "MassSearchHit"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[5] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("SearchMass12C_Bulk");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "SessionID"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://microsoft.com/wsdl/types/", "guid"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "masses"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "ArrayOfFloat"), float[].class, false, false);
        param.setItemQName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "float"));
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "tolerance"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"), float.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "toleranceSelector"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "ToleranceType"), de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.ToleranceType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "ArrayOfArrayOfMassSearchHit"));
        oper.setReturnClass(de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[][].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "SearchMass12C_BulkResult"));
        param = oper.getReturnParamDesc();
        param.setItemQName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "ArrayOfMassSearchHit"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[6] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("SearchMass13C");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "SessionID"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://microsoft.com/wsdl/types/", "guid"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "mass"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"), float.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "tolerance"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"), float.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "ArrayOfMassSearchHit"));
        oper.setReturnClass(de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "SearchMass13CResult"));
        param = oper.getReturnParamDesc();
        param.setItemQName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "MassSearchHit"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[7] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("SearchMass15N");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "SessionID"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://microsoft.com/wsdl/types/", "guid"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "mass"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"), float.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "tolerance"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"), float.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "ArrayOfMassSearchHit"));
        oper.setReturnClass(de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "SearchMass15NResult"));
        param = oper.getReturnParamDesc();
        param.setItemQName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "MassSearchHit"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[8] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("SearchMass34S");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "SessionID"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://microsoft.com/wsdl/types/", "guid"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "mass"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"), float.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "tolerance"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"), float.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "ArrayOfMassSearchHit"));
        oper.setReturnClass(de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "SearchMass34SResult"));
        param = oper.getReturnParamDesc();
        param.setItemQName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "MassSearchHit"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[9] = oper;

    }

    private static void _initOperationDesc2(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetSynonyms");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "SessionID"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://microsoft.com/wsdl/types/", "guid"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "FormulaID"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "ArrayOfSynonym"));
        oper.setReturnClass(de.mpg.mpimp_golm.www.GoBioSpace.v01.Synonym.Synonym[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "GetSynonymsResult"));
        param = oper.getReturnParamDesc();
        param.setItemQName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "Synonym"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[10] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetMonoisotopicWeight");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "Formula"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        oper.setReturnClass(double.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "GetMonoisotopicWeightResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[11] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetMolecularWeight");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "Formula"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        oper.setReturnClass(double.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "GetMolecularWeightResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[12] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetFormulaID");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "Formula"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(int.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "GetFormulaIDResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[13] = oper;

    }

    public WsGoBioSpaceSoapStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public WsGoBioSpaceSoapStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public WsGoBioSpaceSoapStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.2");
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "Adduct");
            cachedSerQNames.add(qName);
            cls = de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Adduct.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "AdductHit");
            cachedSerQNames.add(qName);
            cls = de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.AdductHit.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "ArrayOfAdduct");
            cachedSerQNames.add(qName);
            cls = de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Adduct[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "Adduct");
            qName2 = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "Adduct");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "ArrayOfArrayOfMassSearchHit");
            cachedSerQNames.add(qName);
            cls = de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[][].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "ArrayOfMassSearchHit");
            qName2 = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "ArrayOfMassSearchHit");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "ArrayOfDepositor");
            cachedSerQNames.add(qName);
            cls = de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Depositor[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "Depositor");
            qName2 = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "Depositor");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "ArrayOfFloat");
            cachedSerQNames.add(qName);
            cls = float[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float");
            qName2 = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "float");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "ArrayOfMassSearchHit");
            cachedSerQNames.add(qName);
            cls = de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "MassSearchHit");
            qName2 = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "MassSearchHit");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "ArrayOfProperty");
            cachedSerQNames.add(qName);
            cls = de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Property[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "Property");
            qName2 = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "Property");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "ArrayOfShort");
            cachedSerQNames.add(qName);
            cls = short[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "short");
            qName2 = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "short");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "ArrayOfSynonym");
            cachedSerQNames.add(qName);
            cls = de.mpg.mpimp_golm.www.GoBioSpace.v01.Synonym.Synonym[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://www.mpimp-golm.mpg.de/GoBioSpace/v01/Synonym", "Synonym");
            qName2 = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "Synonym");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "Depositor");
            cachedSerQNames.add(qName);
            cls = de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Depositor.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "MassSearchHit");
            cachedSerQNames.add(qName);
            cls = de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "Property");
            cachedSerQNames.add(qName);
            cls = de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Property.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "ToleranceType");
            cachedSerQNames.add(qName);
            cls = de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.ToleranceType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://microsoft.com/wsdl/types/", "guid");
            cachedSerQNames.add(qName);
            cls = java.lang.String.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://www.mpimp-golm.mpg.de/GoBioSpace/v01/Synonym", "Synonym");
            cachedSerQNames.add(qName);
            cls = de.mpg.mpimp_golm.www.GoBioSpace.v01.Synonym.Synonym.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setEncodingStyle(null);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                            java.lang.Class df = (java.lang.Class)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
                                 cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Depositor[] getDepositors() throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/GetDepositors");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "GetDepositors"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Depositor[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Depositor[]) org.apache.axis.utils.JavaUtils.convert(_resp, de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Depositor[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Property[] getProperties() throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/GetProperties");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "GetProperties"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Property[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Property[]) org.apache.axis.utils.JavaUtils.convert(_resp, de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Property[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Adduct[] getAdducts() throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/GetAdducts");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "GetAdducts"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Adduct[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Adduct[]) org.apache.axis.utils.JavaUtils.convert(_resp, de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.Adduct[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public java.lang.String createSession(short[] depositorIds, byte[] adductIds) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/CreateSession");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "CreateSession"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {depositorIds, adductIds});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public void purgeSession(java.lang.String sessionID) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/PurgeSession");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "PurgeSession"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {sessionID});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[] searchMass12C(java.lang.String sessionID, float mass, float tolerance) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/SearchMass12C");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "SearchMass12C"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {sessionID, new java.lang.Float(mass), new java.lang.Float(tolerance)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[]) org.apache.axis.utils.JavaUtils.convert(_resp, de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[][] searchMass12C_Bulk(java.lang.String sessionID, float[] masses, float tolerance, de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.ToleranceType toleranceSelector) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[6]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/SearchMass12C_Bulk");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "SearchMass12C_Bulk"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {sessionID, masses, new java.lang.Float(tolerance), toleranceSelector});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[][]) _resp;
            } catch (java.lang.Exception _exception) {
                return (de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[][]) org.apache.axis.utils.JavaUtils.convert(_resp, de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[][].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[] searchMass13C(java.lang.String sessionID, float mass, float tolerance) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[7]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/SearchMass13C");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "SearchMass13C"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {sessionID, new java.lang.Float(mass), new java.lang.Float(tolerance)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[]) org.apache.axis.utils.JavaUtils.convert(_resp, de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[] searchMass15N(java.lang.String sessionID, float mass, float tolerance) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[8]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/SearchMass15N");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "SearchMass15N"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {sessionID, new java.lang.Float(mass), new java.lang.Float(tolerance)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[]) org.apache.axis.utils.JavaUtils.convert(_resp, de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[] searchMass34S(java.lang.String sessionID, float mass, float tolerance) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[9]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/SearchMass34S");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "SearchMass34S"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {sessionID, new java.lang.Float(mass), new java.lang.Float(tolerance)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[]) org.apache.axis.utils.JavaUtils.convert(_resp, de.mpg.mpimp_golm.gmd.webservices.wsGoBioSpace_asmx.MassSearchHit[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public de.mpg.mpimp_golm.www.GoBioSpace.v01.Synonym.Synonym[] getSynonyms(java.lang.String sessionID, int formulaID) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[10]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/GetSynonyms");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "GetSynonyms"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {sessionID, new java.lang.Integer(formulaID)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (de.mpg.mpimp_golm.www.GoBioSpace.v01.Synonym.Synonym[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (de.mpg.mpimp_golm.www.GoBioSpace.v01.Synonym.Synonym[]) org.apache.axis.utils.JavaUtils.convert(_resp, de.mpg.mpimp_golm.www.GoBioSpace.v01.Synonym.Synonym[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public double getMonoisotopicWeight(java.lang.String formula) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[11]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/GetMonoisotopicWeight");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "GetMonoisotopicWeight"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {formula});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return ((java.lang.Double) _resp).doubleValue();
            } catch (java.lang.Exception _exception) {
                return ((java.lang.Double) org.apache.axis.utils.JavaUtils.convert(_resp, double.class)).doubleValue();
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public double getMolecularWeight(java.lang.String formula) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[12]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/GetMolecularWeight");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "GetMolecularWeight"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {formula});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return ((java.lang.Double) _resp).doubleValue();
            } catch (java.lang.Exception _exception) {
                return ((java.lang.Double) org.apache.axis.utils.JavaUtils.convert(_resp, double.class)).doubleValue();
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public int getFormulaID(java.lang.String formula) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[13]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/GetFormulaID");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://gmd.mpimp-golm.mpg.de/webservices/wsGoBioSpace.asmx/", "GetFormulaID"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {formula});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return ((java.lang.Integer) _resp).intValue();
            } catch (java.lang.Exception _exception) {
                return ((java.lang.Integer) org.apache.axis.utils.JavaUtils.convert(_resp, int.class)).intValue();
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

}
