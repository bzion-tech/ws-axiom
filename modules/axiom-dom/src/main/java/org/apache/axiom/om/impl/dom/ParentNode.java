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

package org.apache.axiom.om.impl.dom;

import org.apache.axiom.om.OMCloneOptions;
import org.apache.axiom.om.OMComment;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMDocType;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMProcessingInstruction;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.OMXMLStreamReaderConfiguration;
import org.apache.axiom.om.impl.OMNodeEx;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.common.OMChildrenLocalNameIterator;
import org.apache.axiom.om.impl.common.OMChildrenNamespaceIterator;
import org.apache.axiom.om.impl.common.OMChildrenQNameIterator;
import org.apache.axiom.om.impl.common.OMContainerHelper;
import org.apache.axiom.om.impl.common.OMDescendantsIterator;
import org.apache.axiom.om.impl.dom.factory.OMDOMFactory;
import org.apache.axiom.om.impl.jaxp.OMSource;
import org.apache.axiom.om.impl.traverse.OMChildrenIterator;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.sax.SAXSource;

import java.util.Iterator;

public abstract class ParentNode extends NodeImpl implements NodeList {

    protected NodeImpl firstChild;

    protected NodeImpl lastChild;

    protected ParentNode(OMFactory factory) {
        super(factory);
    }

    // /
    // /OMContainer methods
    // /

    public OMXMLParserWrapper getBuilder() {
        return this.builder;
    }

    void internalAppendChild(NodeImpl node) {
        insertBefore(node, null, false);
    }
    
    public void addChild(OMNode omNode) {
        if (omNode.getOMFactory() instanceof OMDOMFactory) {
            insertBefore((Node)omNode, null, false);
        } else {
            addChild(importNode(omNode));
        }
    }

    public void buildNext() {
        if (builder != null) {
            if (!builder.isCompleted()) {
                builder.next();
            } else {
                this.setComplete(true);
            }         
        }
    }

    public Iterator getChildren() {
        return new OMChildrenIterator(getFirstOMChild());
    }

    public Iterator getDescendants(boolean includeSelf) {
        return new OMDescendantsIterator((OMContainer)this, includeSelf);
    }

    /**
     * Returns an iterator of child nodes having a given qname.
     *
     * @see org.apache.axiom.om.OMContainer#getChildrenWithName (javax.xml.namespace.QName)
     */
    public Iterator getChildrenWithName(QName elementQName) throws OMException {
        return new OMChildrenQNameIterator(getFirstOMChild(), elementQName);
    }
    
    public Iterator getChildrenWithLocalName(String localName) {
        return new OMChildrenLocalNameIterator(getFirstOMChild(),
                                               localName);
    }


    public Iterator getChildrenWithNamespaceURI(String uri) {
        return new OMChildrenNamespaceIterator(getFirstOMChild(),
                                               uri);
    }

    /**
     * Returns the first OMElement child node.
     *
     * @see org.apache.axiom.om.OMContainer#getFirstChildWithName (javax.xml.namespace.QName)
     */
    public OMElement getFirstChildWithName(QName elementQName)
            throws OMException {
        Iterator children = new OMChildrenQNameIterator(getFirstOMChild(),
                                                        elementQName);
        while (children.hasNext()) {
            OMNode node = (OMNode) children.next();

            // Return the first OMElement node that is found
            if (node instanceof OMElement) {
                return (OMElement) node;
            }
        }
        return null;
    }

    public OMNode getFirstOMChild() {
        while ((firstChild == null) && !done) {
            buildNext();
        }
        return (OMNode)firstChild;
    }

    public OMNode getFirstOMChildIfAvailable() {
        return (OMNode)firstChild;
    }

    public void setFirstChild(OMNode omNode) {
        if (firstChild != null) {
            ((OMNodeEx) omNode).setParent((OMContainer)this);
        }
        this.firstChild = (NodeImpl) omNode;
    }

    /**
     * Forcefully set the last child
     * @param omNode
     */
    public void setLastChild(OMNode omNode) {
        this.lastChild = (NodeImpl) omNode;
    }

    // /
    // /DOM Node methods
    // /

    public final NodeList getChildNodes() {
        return this;
    }

    public final int getLength() {
        int count = 0;
        Node child = getFirstChild();
        while (child != null) {
            count++;
            child = child.getNextSibling();
        }
        return count;
    }

    public final Node item(int index) {
        int count = 0;
        Node child = getFirstChild();
        while (child != null) {
            if (count == index) {
                return child;
            } else {
                child = child.getNextSibling();
            }
            count++;
        }
        return null;
    }

    public Node getFirstChild() {
        return (Node) this.getFirstOMChild();
    }

    public Node getLastChild() {
        if (!this.done) {
            this.build();
        }
        return this.lastChild;
    }

    public boolean hasChildNodes() {
        while ((firstChild == null) && !done) {
            buildNext();
        }
        return this.firstChild != null;
    }

    public final Node appendChild(Node newChild) throws DOMException {
        return insertBefore(newChild, null);
    }

    /**
     * Inserts newChild before the refChild. If the refChild is null then the newChild is made the
     * last child.
     */
    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        return insertBefore(newChild, refChild, true);
    }
    
    private Node insertBefore(Node newChild, Node refChild, boolean useDomSemantics) {
        NodeImpl newDomChild = (NodeImpl) newChild;
        NodeImpl refDomChild = (NodeImpl) refChild;

        if (useDomSemantics) {
            checkSameOwnerDocument(newDomChild);
        }

        if (isAncestorOrSelf(newChild)) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                                   DOMMessageFormatter.formatMessage(
                                           DOMMessageFormatter.DOM_DOMAIN,
                                           DOMException.HIERARCHY_REQUEST_ERR, null));
        }

        if (newDomChild.parentNode() != null) {
            //If the newChild is already in the tree remove it
            newDomChild.parentNode().removeChild(newDomChild);
        }

        if (this instanceof Document) {
            if (newDomChild instanceof ElementImpl) {
                if (((DocumentImpl) this).getOMDocumentElement(false) != null) {
                    // Throw exception since there cannot be two document elements
                    throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                                           DOMMessageFormatter.formatMessage(
                                                   DOMMessageFormatter.DOM_DOMAIN,
                                                   DOMException.HIERARCHY_REQUEST_ERR, null));
                }
                if (newDomChild.parentNode() == null) {
                    newDomChild.setParent(this, useDomSemantics);
                }
            } else if (!(newDomChild instanceof CommentImpl
                    || newDomChild instanceof ProcessingInstructionImpl
                    || newDomChild instanceof DocumentFragmentImpl
                    || newDomChild instanceof DocumentTypeImpl)) {
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                        DOMMessageFormatter.formatMessage(
                                DOMMessageFormatter.DOM_DOMAIN,
                                DOMException.HIERARCHY_REQUEST_ERR, null));
            }
        }
        
        if (refChild == null) { // Append the child to the end of the list
            // if there are no children
            if (this.lastChild == null && firstChild == null) {
                this.lastChild = newDomChild;
                this.firstChild = newDomChild;
                this.firstChild.isFirstChild(true);
                newDomChild.setParent(this, useDomSemantics);
            } else {
                this.lastChild.internalSetNextSibling(newDomChild);
                newDomChild.internalSetPreviousSibling(this.lastChild);
                this.lastChild = newDomChild;
                this.lastChild.internalSetNextSibling(null);
            }
            if (newDomChild.parentNode() == null) {
                newDomChild.setParent(this, useDomSemantics);
            }
        } else {
            Iterator children = this.getChildren();
            boolean found = false;
            while (children.hasNext()) {
                NodeImpl tempNode = (NodeImpl) children.next();

                if (tempNode.equals(refChild)) {
                    // RefChild found
                    if (this.firstChild == tempNode) { // If the refChild is the
                        // first child

                        if (newChild instanceof DocumentFragmentImpl) {
                            // The new child is a DocumentFragment
                            DocumentFragmentImpl docFrag =
                                    (DocumentFragmentImpl) newChild;
                            
                            NodeImpl child = docFrag.firstChild;
                            while (child != null) {
                                child.setParent(this, useDomSemantics);
                                child = child.internalGetNextSibling();
                            }
                            
                            this.firstChild = docFrag.firstChild;
                            docFrag.lastChild.internalSetNextSibling(refDomChild);
                            refDomChild.internalSetPreviousSibling(docFrag.lastChild.internalGetNextSibling());

                            docFrag.firstChild = null;
                            docFrag.lastChild = null;
                        } else {

                            // Make the newNode the first Child
                            this.firstChild = newDomChild;

                            newDomChild.internalSetNextSibling(refDomChild);
                            refDomChild.internalSetPreviousSibling(newDomChild);

                            this.firstChild.isFirstChild(true);
                            refDomChild.isFirstChild(false);
                            newDomChild.internalSetPreviousSibling(null); // Just to be
                            // sure :-)

                        }
                    } else { // If the refChild is not the fist child
                        NodeImpl previousNode = refDomChild.internalGetPreviousSibling();

                        if (newChild instanceof DocumentFragmentImpl) {
                            // the newChild is a document fragment
                            DocumentFragmentImpl docFrag =
                                    (DocumentFragmentImpl) newChild;

                            NodeImpl child = docFrag.firstChild;
                            while (child != null) {
                                child.setParent(this, useDomSemantics);
                                child = child.internalGetNextSibling();
                            }
                            
                            previousNode.internalSetNextSibling(docFrag.firstChild);
                            docFrag.firstChild.internalSetPreviousSibling(previousNode);

                            docFrag.lastChild.internalSetNextSibling(refDomChild);
                            refDomChild.internalSetPreviousSibling(docFrag.lastChild);

                            docFrag.firstChild = null;
                            docFrag.lastChild = null;
                        } else {

                            previousNode.internalSetNextSibling(newDomChild);
                            newDomChild.internalSetPreviousSibling(previousNode);

                            newDomChild.internalSetNextSibling(refDomChild);
                            refDomChild.internalSetPreviousSibling(newDomChild);
                        }

                    }
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new DOMException(DOMException.NOT_FOUND_ERR,
                                       DOMMessageFormatter.formatMessage(
                                               DOMMessageFormatter.DOM_DOMAIN,
                                               DOMException.NOT_FOUND_ERR, null));
            }

            if (newDomChild.parentNode() == null) {
                newDomChild.setParent(this, useDomSemantics);
            }

        }
        
        if (!newDomChild.isComplete() && !(newDomChild instanceof OMSourcedElement)) {
            setComplete(false);
        }
        
        return newChild;
    }

    /** Replaces the oldChild with the newChild. */
    public final Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        NodeImpl newDomChild = (NodeImpl) newChild;
        NodeImpl oldDomChild = (NodeImpl) oldChild;

        if (newChild == null) {
            return this.removeChild(oldChild);
        }

        if (isAncestorOrSelf(newChild)) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                                   DOMMessageFormatter.formatMessage(
                                           DOMMessageFormatter.DOM_DOMAIN,
                                           DOMException.HIERARCHY_REQUEST_ERR, null));
        }

        checkSameOwnerDocument(newDomChild);

        Iterator children = this.getChildren();
        boolean found = false;
        while (!found && children.hasNext()) {
            NodeImpl tempNode = (NodeImpl) children.next();
            if (tempNode.equals(oldChild)) {
                if (newChild instanceof DocumentFragmentImpl) {
                    DocumentFragmentImpl docFrag =
                            (DocumentFragmentImpl) newDomChild;
                    NodeImpl child = (NodeImpl) docFrag.getFirstChild();
                    this.replaceChild(child, oldChild);
                    
                    //set the parent of all kids to me
                    while(child != null) {
                        child.setParent(this, true);
                        child = child.internalGetNextSibling();
                    }

                    this.lastChild = (NodeImpl)docFrag.getLastChild();
                    
                } else {
                    if (this.firstChild == oldDomChild) {

                        if (this.firstChild.internalGetNextSibling() != null) {
                            this.firstChild.internalGetNextSibling().internalSetPreviousSibling(newDomChild);
                            newDomChild.internalSetNextSibling(this.firstChild.internalGetNextSibling());
                        }

                        //Cleanup the current first child
                        this.firstChild.setParent(null, true);
                        this.firstChild.internalSetNextSibling(null);

                        //Set the new first child
                        this.firstChild = newDomChild;
                        

                    } else {
                        newDomChild.internalSetNextSibling(oldDomChild.internalGetNextSibling());
                        newDomChild.internalSetPreviousSibling(oldDomChild.internalGetPreviousSibling());

                        oldDomChild.internalGetPreviousSibling().internalSetNextSibling(newDomChild);

                        // If the old child is not the last
                        if (oldDomChild.internalGetNextSibling() != null) {
                            oldDomChild.internalGetNextSibling().internalSetPreviousSibling(newDomChild);
                        } else {
                            this.lastChild = newDomChild;
                        }

                    }

                    newDomChild.setParent(this, true);
                }
                found = true;

                // remove the old child's references to this tree
                oldDomChild.internalSetNextSibling(null);
                oldDomChild.internalSetPreviousSibling(null);
                oldDomChild.setParent(null, true);
            }
        }

        if (!found)
            throw new DOMException(DOMException.NOT_FOUND_ERR,
                                   DOMMessageFormatter.formatMessage(
                                           DOMMessageFormatter.DOM_DOMAIN, DOMException.NOT_FOUND_ERR,
                                           null));

        return oldChild;
    }

    /** Removes the given child from the DOM Tree. */
    public final Node removeChild(Node oldChild) throws DOMException {
        if (oldChild.getParentNode() == this) {
            ((NodeImpl)oldChild).detach(true);
            return oldChild;
        } else {
            throw new DOMException(DOMException.NOT_FOUND_ERR,
                                   DOMMessageFormatter.formatMessage(
                                           DOMMessageFormatter.DOM_DOMAIN, DOMException.NOT_FOUND_ERR,
                                           null));
        }
    }

    /**
     * Checks if the given node is an ancestor (or identical) to this node.
     * 
     * @param node
     *            the node to check
     * @return <code>true</code> if the node is an ancestor or indentical to this node
     */
    private boolean isAncestorOrSelf(Node node) {
        Node currentNode = this;
        do {
            if (currentNode == node) {
                return true;
            }
            currentNode = currentNode.getParentNode();
        } while (currentNode != null);
        return false;
    }

    final NodeImpl clone(OMCloneOptions options, ParentNode targetParent, boolean deep) {
        ParentNode clone = shallowClone(options, targetParent);
        if (deep) {
            for (Node child = getFirstChild(); child != null; child = child.getNextSibling()) {
                ((NodeImpl)child).clone(options, clone, true);
            }
        }
        return clone;
    }
    
    abstract ParentNode shallowClone(OMCloneOptions options, ParentNode targetParent);

    /**
     * This method is intended only to be used by Axiom intenals when merging Objects from different
     * Axiom implementations to the DOOM implementation.
     *
     * @param child
     */
    protected OMNode importNode(OMNode child) {
        int type = child.getType();
        switch (type) {
            case (OMNode.ELEMENT_NODE): {
                OMElement childElement = (OMElement) child;
                OMElement newElement = (new StAXOMBuilder(this.factory,
                                                          childElement.getXMLStreamReader()))
                        .getDocumentElement();
                newElement.build();
                return (OMNode)ownerDocument().importNode((Element) newElement,
                                                          true);
            }
            case (OMNode.TEXT_NODE): {
                OMText importedText = (OMText) child;
                OMText newText;
                if (importedText.isBinary()) {
                    boolean isOptimize = importedText.isOptimized();
                    newText = this.factory.createOMText(importedText
                            .getDataHandler(), isOptimize);
                } else if (importedText.isCharacters()) {
                    newText = new TextImpl(importedText.getTextCharacters(), this.factory);
                } else {
                    newText = new TextImpl(importedText.getText(), this.factory);
                }
                return newText;
            }

            case (OMNode.PI_NODE): {
                OMProcessingInstruction importedPI = (OMProcessingInstruction) child;
                OMProcessingInstruction newPI = this.factory
                        .createOMProcessingInstruction((OMContainer)this,
                                                       importedPI.getTarget(),
                                                       importedPI.getValue());
                return newPI;
            }
            case (OMNode.COMMENT_NODE): {
                OMComment importedComment = (OMComment) child;
                OMComment newComment = this.factory.createOMComment((OMContainer)this,
                                                                    importedComment.getValue());
                newComment = new CommentImpl(importedComment.getValue(),
                                             this.factory);
                return newComment;
            }
            case (OMNode.DTD_NODE): {
                OMDocType importedDocType = (OMDocType) child;
                OMDocType newDocType = this.factory.createOMDocType((OMContainer)this,
                                                                    importedDocType.getValue());
                return newDocType;
            }
            default: {
                throw new UnsupportedOperationException(
                        "Not Implemented Yet for the given node type");
            }
        }
    }
    
    public String getTextContent() throws DOMException {
        Node child = getFirstChild();
        if (child != null) {
            Node next = child.getNextSibling();
            if (next == null) {
                return hasTextContent(child) ? ((NodeImpl)child).getTextContent() : "";
            }
            StringBuffer buf = new StringBuffer();
            getTextContent(buf);
            return buf.toString();
        } else {
            return "";
        }
    }

    void getTextContent(StringBuffer buf) throws DOMException {
        Node child = getFirstChild();
        while (child != null) {
            if (hasTextContent(child)) {
                ((NodeImpl)child).getTextContent(buf);
            }
            child = child.getNextSibling();
        }
    }
    
    // internal method returning whether to take the given node's text content
    private static boolean hasTextContent(Node child) {
        return child.getNodeType() != Node.COMMENT_NODE &&
            child.getNodeType() != Node.PROCESSING_INSTRUCTION_NODE /* &&
            (child.getNodeType() != Node.TEXT_NODE ||
             ((TextImpl) child).isIgnorableWhitespace() == false)*/;
    }
    
    public void setTextContent(String textContent) throws DOMException {
        // get rid of any existing children
        // TODO: there is probably a better way to remove all children
        Node child;
        while ((child = getFirstChild()) != null) {
            removeChild(child);
        }
        // create a Text node to hold the given content
        if (textContent != null && textContent.length() != 0) {
            addChild(factory.createOMText(textContent));
        }
    }

    public XMLStreamReader getXMLStreamReaderWithoutCaching() {
        return getXMLStreamReader(false);
    }

    public XMLStreamReader getXMLStreamReader() {
        return getXMLStreamReader(true);
    }

    public XMLStreamReader getXMLStreamReader(boolean cache) {
        return OMContainerHelper.getXMLStreamReader((OMContainer)this, cache);
    }
    
    public XMLStreamReader getXMLStreamReader(boolean cache, OMXMLStreamReaderConfiguration configuration) {
        return OMContainerHelper.getXMLStreamReader((OMContainer)this, cache, configuration);
    }

    public SAXSource getSAXSource(boolean cache) {
        return new OMSource((OMContainer)this);
    }

    void notifyChildComplete() {
        if (!this.done && builder == null) {
            Iterator iterator = getChildren();
            while (iterator.hasNext()) {
                OMNode node = (OMNode) iterator.next();
                if (!node.isComplete()) {
                    return;
                }
            }
            this.setComplete(true);
        }
    }

    void normalize(DOMConfigurationImpl config) {
        OMNode child = getFirstOMChild();
        while (child != null) {
            ((NodeImpl)child).normalize(config);
            child = child.getNextOMSibling();
        }
    }
}
