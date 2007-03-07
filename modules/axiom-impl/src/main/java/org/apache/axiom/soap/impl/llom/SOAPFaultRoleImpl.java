/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axiom.soap.impl.llom;

import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.OMNodeEx;
import org.apache.axiom.om.impl.serialize.StreamWriterToContentHandlerConverter;
import org.apache.axiom.om.impl.util.OMSerializerUtil;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPProcessingException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public abstract class SOAPFaultRoleImpl extends SOAPElement
        implements org.apache.axiom.soap.SOAPFaultRole {

    protected SOAPFaultRoleImpl(OMNamespace ns, SOAPFactory factory) {
        super(SOAP12Constants.SOAP_FAULT_ROLE_LOCAL_NAME, ns, factory);
    }

    public SOAPFaultRoleImpl(SOAPFault parent,
                             boolean extractNamespaceFromParent,
                             SOAPFactory factory) throws SOAPProcessingException {
        super(parent,
              SOAP12Constants.SOAP_FAULT_ROLE_LOCAL_NAME,
              extractNamespaceFromParent,
              factory);
    }

    public SOAPFaultRoleImpl(SOAPFault parent, OMXMLParserWrapper builder,
                             SOAPFactory factory) {
        super(parent, SOAP12Constants.SOAP_FAULT_ROLE_LOCAL_NAME, builder, factory);
    }

    public void setRoleValue(String uri) {
        this.setText(uri);
    }

    public String getRoleValue() {
        return this.getText();
    }

    protected void internalSerialize(XMLStreamWriter writer, boolean cache)
            throws XMLStreamException {
        // select the builder
        short builderType = PULL_TYPE_BUILDER;    // default is pull type
        if (builder != null) {
            builderType = this.builder.getBuilderType();
        }
        if ((builderType == PUSH_TYPE_BUILDER)
                && (builder.getRegisteredContentHandler() == null)) {
            builder.registerExternalContentHandler(
                    new StreamWriterToContentHandlerConverter(writer));
        }

        if (!cache) {
            //No caching
            if (this.firstChild != null) {
                OMSerializerUtil.serializeStartpart(this, writer);
                ((OMNodeEx) firstChild).internalSerializeAndConsume(writer);
                OMSerializerUtil.serializeEndpart(writer);
            } else if (!this.done) {
                if (builderType == PULL_TYPE_BUILDER) {
                    OMSerializerUtil.serializeByPullStream(this, writer);
                } else {
                    OMSerializerUtil.serializeStartpart(this, writer);
                    builder.setCache(cache);
                    builder.next();
                    OMSerializerUtil.serializeEndpart(writer);
                }
            } else {
                OMSerializerUtil.serializeNormal(this, writer, cache);
            }
            // do not serialise the siblings


        } else {
            //Cached
            OMSerializerUtil.serializeNormal(this, writer, cache);

            // do not serialise the siblings
        }


    }

}
