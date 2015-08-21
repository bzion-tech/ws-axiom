/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axiom.om.impl.dom.factory;

import javax.xml.namespace.QName;

import org.apache.axiom.core.CoreCDATASection;
import org.apache.axiom.core.CoreCharacterDataNode;
import org.apache.axiom.core.CoreDocument;
import org.apache.axiom.core.CoreDocumentTypeDeclaration;
import org.apache.axiom.core.CoreNSAwareAttribute;
import org.apache.axiom.core.CoreNSAwareElement;
import org.apache.axiom.core.CoreNSUnawareAttribute;
import org.apache.axiom.core.CoreNamespaceDeclaration;
import org.apache.axiom.core.CoreProcessingInstruction;
import org.apache.axiom.dom.DOMNodeFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMComment;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMDocType;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMEntityReference;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMHierarchyException;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMProcessingInstruction;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.OMContainerEx;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.common.AxiomElement;
import org.apache.axiom.om.impl.common.AxiomNamespaceDeclaration;
import org.apache.axiom.om.impl.common.OMNamespaceImpl;
import org.apache.axiom.om.impl.common.Policies;
import org.apache.axiom.om.impl.common.factory.AxiomNodeFactory;
import org.apache.axiom.om.impl.dom.CDATASectionImpl;
import org.apache.axiom.om.impl.dom.CommentImpl;
import org.apache.axiom.om.impl.dom.DocumentImpl;
import org.apache.axiom.om.impl.dom.DocumentTypeImpl;
import org.apache.axiom.om.impl.dom.ElementImpl;
import org.apache.axiom.om.impl.dom.EntityReferenceImpl;
import org.apache.axiom.om.impl.dom.NSAwareAttribute;
import org.apache.axiom.om.impl.dom.NSUnawareAttribute;
import org.apache.axiom.om.impl.dom.NamespaceDeclaration;
import org.apache.axiom.om.impl.dom.ProcessingInstructionImpl;
import org.apache.axiom.om.impl.dom.TextImpl;
import org.apache.axiom.om.impl.util.OMSerializerUtil;
import org.apache.axiom.soap.impl.common.AxiomSOAP11Body;
import org.apache.axiom.soap.impl.common.AxiomSOAP11Fault;
import org.apache.axiom.soap.impl.common.AxiomSOAP11FaultCode;
import org.apache.axiom.soap.impl.common.AxiomSOAP11FaultDetail;
import org.apache.axiom.soap.impl.common.AxiomSOAP11FaultReason;
import org.apache.axiom.soap.impl.common.AxiomSOAP11FaultRole;
import org.apache.axiom.soap.impl.common.AxiomSOAP11Header;
import org.apache.axiom.soap.impl.common.AxiomSOAP11HeaderBlock;
import org.apache.axiom.soap.impl.common.AxiomSOAP12Body;
import org.apache.axiom.soap.impl.common.AxiomSOAP12Fault;
import org.apache.axiom.soap.impl.common.AxiomSOAP12FaultCode;
import org.apache.axiom.soap.impl.common.AxiomSOAP12FaultDetail;
import org.apache.axiom.soap.impl.common.AxiomSOAP12FaultNode;
import org.apache.axiom.soap.impl.common.AxiomSOAP12FaultReason;
import org.apache.axiom.soap.impl.common.AxiomSOAP12FaultRole;
import org.apache.axiom.soap.impl.common.AxiomSOAP12FaultSubCode;
import org.apache.axiom.soap.impl.common.AxiomSOAP12FaultText;
import org.apache.axiom.soap.impl.common.AxiomSOAP12FaultValue;
import org.apache.axiom.soap.impl.common.AxiomSOAP12Header;
import org.apache.axiom.soap.impl.common.AxiomSOAP12HeaderBlock;
import org.apache.axiom.soap.impl.common.AxiomSOAPEnvelope;
import org.apache.axiom.soap.impl.dom.SOAPEnvelopeImpl;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11BodyImpl;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11FaultCodeImpl;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11FaultDetailImpl;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11FaultImpl;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11FaultReasonImpl;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11FaultRoleImpl;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11HeaderBlockImpl;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11HeaderImpl;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12BodyImpl;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12FaultCodeImpl;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12FaultDetailImpl;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12FaultImpl;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12FaultNodeImpl;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12FaultReasonImpl;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12FaultRoleImpl;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12FaultSubCodeImpl;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12FaultTextImpl;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12FaultValueImpl;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12HeaderBlockImpl;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12HeaderImpl;

/**
 * OM factory implementation for DOOM. It creates nodes that implement
 * DOM as defined by the interfaces in {@link org.w3c.dom}.
 */
public class OMDOMFactory implements AxiomNodeFactory, DOMNodeFactory {
    private final OMDOMMetaFactory metaFactory;

    public OMDOMFactory(OMDOMMetaFactory metaFactory) {
        this.metaFactory = metaFactory;
    }

    public OMDOMFactory() {
        this(new OMDOMMetaFactory());
    }

    public OMMetaFactory getMetaFactory() {
        return metaFactory;
    }

    public OMSourcedElement createOMElement(OMDataSource source) {
        throw new UnsupportedOperationException("Not supported for DOM");
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.om.OMFactory#createOMElement(org.apache.axiom.om.OMDataSource, java.lang.String, org.apache.axiom.om.OMNamespace)
     */
    public OMSourcedElement createOMElement(OMDataSource source, String localName, OMNamespace ns) {
        throw new UnsupportedOperationException("Not supported for DOM");
    }

    /**
     * Unsupported.
     */
    public OMSourcedElement createOMElement(OMDataSource source, QName qname) {
        throw new UnsupportedOperationException("Not supported for DOM");
    }

    public OMElement createOMElement(String localName, String namespaceURI, String prefix) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException("namespaceURI must not be null");
        } else if (namespaceURI.length() == 0) {
            if (prefix != null && prefix.length() > 0) {
                throw new IllegalArgumentException("Cannot create a prefixed element with an empty namespace name");
            }
            return createOMElement(localName, null);
        } else {
            return createOMElement(localName, createOMNamespace(namespaceURI, prefix));
        }
    }

    /**
     * Creates a new OMDOM Element node and adds it to the given parent.
     *
     * @see #createOMElement(String, OMNamespace, OMContainer)
     * @see org.apache.axiom.om.OMFactory#createOMElement( javax.xml.namespace.QName,
     *      org.apache.axiom.om.OMContainer)
     */
    public OMElement createOMElement(QName qname, OMContainer parent)
            throws OMException {
        OMNamespaceImpl ns;
        if (qname.getNamespaceURI().length() == 0) {
            if (qname.getPrefix().length() > 0) {
                throw new IllegalArgumentException("Cannot create a prefixed element with an empty namespace name");
            }
            ns = null;
        } else if (qname.getPrefix().length() != 0) {
            ns = new OMNamespaceImpl(qname.getNamespaceURI(), qname.getPrefix());
        } else {
            ns = new OMNamespaceImpl(qname.getNamespaceURI(), null);
        }
        return createOMElement(qname.getLocalPart(), ns, parent);
    }

    /**
     * Create an OMElement with the given QName
     * <p/>
     * If the QName contains a prefix, we will ensure that an OMNamespace is created mapping the
     * given namespace to the given prefix.  If no prefix is passed, we'll create a generated one.
     *
     * @param qname
     * @return the new OMElement.
     */
    public OMElement createOMElement(QName qname) throws OMException {
        return createOMElement(qname, null);
    }

    /**
     * Creates a new OMNamespace.
     *
     * @see org.apache.axiom.om.OMFactory#createOMNamespace(String, String)
     */
    public OMNamespace createOMNamespace(String uri, String prefix) {
        return new OMNamespaceImpl(uri, prefix);
    }

    public final void validateOMTextParent(OMContainer parent) {
        if (parent instanceof DocumentImpl) {
            throw new OMHierarchyException(
                    "DOM doesn't support text nodes as children of a document");
        }
    }
    
    public OMAttribute createOMAttribute(String localName, OMNamespace ns,
                                         String value) {
        if (ns != null && ns.getPrefix() == null) {
            String namespaceURI = ns.getNamespaceURI();
            if (namespaceURI.length() == 0) {
                ns = null;
            } else {
                ns = new OMNamespaceImpl(namespaceURI, OMSerializerUtil.getNextNSPrefix());
            }
        }
        if (ns != null) {
            if (ns.getNamespaceURI().length() == 0) {
                if (ns.getPrefix().length() > 0) {
                    throw new IllegalArgumentException("Cannot create a prefixed attribute with an empty namespace name");
                } else {
                    ns = null;
                }
            } else if (ns.getPrefix().length() == 0) {
                throw new IllegalArgumentException("Cannot create an unprefixed attribute with a namespace");
            }
        }
        return new NSAwareAttribute(null, localName, ns, value, this);
    }

    public OMComment createOMComment(OMContainer parent, String content) {
        return createOMComment(parent, content, false);
    }
    
    public OMComment createOMComment(OMContainer parent, String content, boolean fromBuilder) {
        CommentImpl comment = new CommentImpl(content, this);
        if (parent != null) {
            ((OMContainerEx)parent).addChild(comment, fromBuilder);
        }
        return comment;
    }

    public OMEntityReference createOMEntityReference(OMContainer parent, String name) {
        return createOMEntityReference(parent, name, null, false);
    }

    public OMEntityReference createOMEntityReference(OMContainer parent, String name, String replacementText, boolean fromBuilder) {
        EntityReferenceImpl node = new EntityReferenceImpl(name, replacementText, this);
        if (parent != null) {
            ((OMContainerEx)parent).addChild(node, fromBuilder);
        }
        return node;
    }

    /**
     * This method is intended only to be used by Axiom intenals when merging Objects from different
     * Axiom implementations to the DOOM implementation.
     *
     * @param child
     */
    public OMNode importNode(OMNode child) {
        int type = child.getType();
        switch (type) {
            case (OMNode.ELEMENT_NODE): {
                OMElement childElement = (OMElement) child;
                OMElement newElement = (new StAXOMBuilder(this,
                                                          childElement.getXMLStreamReader()))
                        .getDocumentElement();
                newElement.build();
                return newElement;
            }
            case (OMNode.TEXT_NODE): {
                OMText importedText = (OMText) child;
                OMText newText;
                if (importedText.isBinary()) {
                    boolean isOptimize = importedText.isOptimized();
                    newText = createOMText(importedText
                            .getDataHandler(), isOptimize);
                } else if (importedText.isCharacters()) {
                    newText = createOMText(null, importedText.getTextCharacters(), OMNode.TEXT_NODE);
                } else {
                    newText = createOMText(importedText.getText());
                }
                return newText;
            }

            case (OMNode.PI_NODE): {
                OMProcessingInstruction importedPI = (OMProcessingInstruction) child;
                OMProcessingInstruction newPI =
                        createOMProcessingInstruction(null, importedPI.getTarget(),
                                                       importedPI.getValue());
                return newPI;
            }
            case (OMNode.COMMENT_NODE): {
                OMComment importedComment = (OMComment) child;
                OMComment newComment = createOMComment(null, importedComment.getValue());
                newComment = new CommentImpl(importedComment.getValue(), this);
                return newComment;
            }
            case (OMNode.DTD_NODE): {
                OMDocType importedDocType = (OMDocType) child;
                return createOMDocType(null, importedDocType.getRootName(),
                        importedDocType.getPublicId(), importedDocType.getSystemId(),
                        importedDocType.getInternalSubset());
            }
            default: {
                throw new UnsupportedOperationException(
                        "Not Implemented Yet for the given node type");
            }
        }
    }

    public final CoreDocument createDocument() {
        return new DocumentImpl(this);
    }

    public final CoreDocumentTypeDeclaration createDocumentTypeDeclaration() {
        return new DocumentTypeImpl(this);
    }

    public final CoreCharacterDataNode createCharacterDataNode() {
        return new TextImpl(this);
    }

    public CoreCDATASection createCDATASection() {
        return new CDATASectionImpl(this);
    }

    public final <T extends CoreNSAwareElement> T createNSAwareElement(Class<T> type) {
        CoreNSAwareElement element;
        if (type == AxiomElement.class) {
            element = new ElementImpl(this);
        } else if (type == AxiomSOAPEnvelope.class) {
            element = new SOAPEnvelopeImpl(this);
        } else if (type == AxiomSOAP11Header.class) {
            element = new SOAP11HeaderImpl(this);
        } else if (type == AxiomSOAP12Header.class) {
            element = new SOAP12HeaderImpl(this);
        } else if (type == AxiomSOAP11HeaderBlock.class) {
            element = new SOAP11HeaderBlockImpl(this);
        } else if (type == AxiomSOAP12HeaderBlock.class) {
            element = new SOAP12HeaderBlockImpl(this);
        } else if (type == AxiomSOAP11Body.class) {
            element = new SOAP11BodyImpl(this);
        } else if (type == AxiomSOAP12Body.class) {
            element = new SOAP12BodyImpl(this);
        } else if (type == AxiomSOAP11Fault.class) {
            element = new SOAP11FaultImpl(this);
        } else if (type == AxiomSOAP12Fault.class) {
            element = new SOAP12FaultImpl(this);
        } else if (type == AxiomSOAP11FaultCode.class) {
            element = new SOAP11FaultCodeImpl(this);
        } else if (type == AxiomSOAP12FaultCode.class) {
            element = new SOAP12FaultCodeImpl(this);
        } else if (type == AxiomSOAP12FaultValue.class) {
            element = new SOAP12FaultValueImpl(this);
        } else if (type == AxiomSOAP12FaultSubCode.class) {
            element = new SOAP12FaultSubCodeImpl(this);
        } else if (type == AxiomSOAP11FaultReason.class) {
            element = new SOAP11FaultReasonImpl(this);
        } else if (type == AxiomSOAP12FaultReason.class) {
            element = new SOAP12FaultReasonImpl(this);
        } else if (type == AxiomSOAP12FaultText.class) {
            element = new SOAP12FaultTextImpl(this);
        } else if (type == AxiomSOAP12FaultNode.class) {
            element = new SOAP12FaultNodeImpl(this);
        } else if (type == AxiomSOAP11FaultRole.class) {
            element = new SOAP11FaultRoleImpl(this);
        } else if (type == AxiomSOAP12FaultRole.class) {
            element = new SOAP12FaultRoleImpl(this);
        } else if (type == AxiomSOAP11FaultDetail.class) {
            element = new SOAP11FaultDetailImpl(this);
        } else if (type == AxiomSOAP12FaultDetail.class) {
            element = new SOAP12FaultDetailImpl(this);
        } else {
            throw new IllegalArgumentException();
        }
        return type.cast(element);
    }

    public final CoreNSUnawareAttribute createAttribute(CoreDocument document, String name, String value, String type) {
        NSUnawareAttribute attr = new NSUnawareAttribute((DocumentImpl)document, this);
        attr.coreSetName(name);
        attr.coreSetCharacterData(value, Policies.DETACH_POLICY);
//        attr.coreSetType(type);
        return attr;
    }

    public final CoreNSAwareAttribute createAttribute(CoreDocument document, String namespaceURI,
            String localName, String prefix, String value, String type) {
        return new NSAwareAttribute((DocumentImpl)document, localName, namespaceURI.length() == 0 ? null : new OMNamespaceImpl(namespaceURI, prefix), value, this);
    }

    public final CoreNamespaceDeclaration createNamespaceDeclaration(CoreDocument document,
            String prefix, String namespaceURI) {
        return new NamespaceDeclaration((DocumentImpl)document, new OMNamespaceImpl(namespaceURI == null ? "" : namespaceURI, prefix), this);
    }

    public final AxiomNamespaceDeclaration createNamespaceDeclaration(OMNamespace namespace) {
        return new NamespaceDeclaration(null, namespace, this);
    }

    public final CoreProcessingInstruction createProcessingInstruction() {
        return new ProcessingInstructionImpl(this);
    }
}
