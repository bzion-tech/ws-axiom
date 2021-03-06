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
package org.apache.axiom.core.impl.mixin;

import org.apache.axiom.core.ClonePolicy;
import org.apache.axiom.core.Content;
import org.apache.axiom.core.CoreChildNode;
import org.apache.axiom.core.CoreDocument;
import org.apache.axiom.core.CoreDocumentFragment;
import org.apache.axiom.core.CoreElement;
import org.apache.axiom.core.CoreModelException;
import org.apache.axiom.core.CoreNode;
import org.apache.axiom.core.CoreParentNode;
import org.apache.axiom.core.DetachPolicy;
import org.apache.axiom.core.NoParentException;
import org.apache.axiom.core.NodeConsumedException;
import org.apache.axiom.core.NodeFilter;
import org.apache.axiom.core.SelfRelationshipException;
import org.apache.axiom.core.Semantics;
import org.apache.axiom.core.impl.Flags;

public aspect CoreChildNodeSupport {
    private CoreParentNode CoreChildNode.owner;
    CoreChildNode CoreChildNode.nextSibling;
    CoreChildNode CoreChildNode.previousSibling;
    
    public final boolean CoreChildNode.coreHasParent() {
        return internalGetFlag(Flags.HAS_PARENT);
    }
    
    public final CoreParentNode CoreChildNode.coreGetParent() {
        return internalGetFlag(Flags.HAS_PARENT) ? owner : null;
    }
    
    public final CoreElement CoreChildNode.coreGetParentElement() {
        return owner instanceof CoreElement ? (CoreElement)owner : null;
    }
    
    public void CoreChildNode.internalSetParent(CoreParentNode parent) {
        if (parent == null) {
            throw new IllegalArgumentException();
        }
        owner = parent;
        internalSetFlag(Flags.HAS_PARENT, true);
    }
    
    public final void CoreChildNode.internalUnsetParent(CoreDocument newOwnerDocument) {
        owner = newOwnerDocument;
        internalSetFlag(Flags.HAS_PARENT, false);
    }
    
    public final CoreNode CoreChildNode.getRootOrOwnerDocument() {
        if (owner == null) {
            return this;
        } else {
            return owner.getRootOrOwnerDocument();
        }
    }
    
    public final void CoreChildNode.coreSetOwnerDocument(CoreDocument document) {
        if (internalGetFlag(Flags.HAS_PARENT)) {
            throw new IllegalStateException();
        }
        owner = document;
    }
    
    public final CoreChildNode CoreChildNode.coreGetNextSiblingIfAvailable() {
        return nextSibling;
    }

    public final void CoreChildNode.internalSetNextSibling(CoreChildNode nextSibling) {
        this.nextSibling = nextSibling;
    }
    
    public final CoreChildNode CoreChildNode.coreGetPreviousSibling() {
        return previousSibling;
    }
    
    public final CoreChildNode CoreChildNode.coreGetPreviousSibling(NodeFilter filter) {
        CoreChildNode sibling = coreGetPreviousSibling();
        while (sibling != null && !filter.accept(sibling)) {
            sibling = sibling.coreGetPreviousSibling();
        }
        return sibling;
    }
    
    public final void CoreChildNode.internalSetPreviousSibling(CoreChildNode previousSibling) {
        this.previousSibling = previousSibling;
    }
    
    public final CoreChildNode CoreChildNode.coreGetNextSibling() throws CoreModelException {
        CoreChildNode nextSibling = coreGetNextSiblingIfAvailable();
        if (nextSibling == null) {
            CoreParentNode parent = coreGetParent();
            if (parent != null) {
                switch (parent.getState()) {
                    case CoreParentNode.DISCARDING:
                    case CoreParentNode.DISCARDED:
                        throw new NodeConsumedException();
                    case CoreParentNode.INCOMPLETE:
                        if (parent.coreGetBuilder() != null) {
                            do {
                                parent.internalBuildNext();
                            } while (parent.getState() == CoreParentNode.INCOMPLETE
                                    && (nextSibling = coreGetNextSiblingIfAvailable()) == null);
                        }
                }
            }
        }
        return nextSibling;
    }

    public final CoreChildNode CoreChildNode.coreGetNextSibling(NodeFilter filter) throws CoreModelException {
        CoreChildNode sibling = coreGetNextSibling();
        while (sibling != null && !filter.accept(sibling)) {
            sibling = sibling.coreGetNextSibling();
        }
        return sibling;
    }
    
    public final void CoreChildNode.coreInsertSiblingAfter(CoreChildNode sibling) throws CoreModelException {
        CoreParentNode parent = coreGetParent();
        if (parent == null) {
            throw new NoParentException("Parent can not be null");
        } else if (this == sibling) {
            throw new SelfRelationshipException("Inserting self as the sibling is not allowed");
        }
        parent.internalCheckNewChild(sibling, null);
        sibling.internalDetach(null, parent);
        CoreChildNode nextSibling = coreGetNextSibling();
        sibling.internalSetPreviousSibling(this);
        if (nextSibling == null) {
            parent.internalGetContent(true).lastChild = sibling;
        } else {
            nextSibling.internalSetPreviousSibling(sibling);
        }
        sibling.internalSetNextSibling(nextSibling);
        this.nextSibling = sibling;
    }
    
    public final void CoreChildNode.coreInsertSiblingBefore(CoreChildNode sibling) throws CoreModelException {
        CoreParentNode parent = coreGetParent();
        if (parent == null) {
            throw new NoParentException("Parent can not be null");
        } else if (this == sibling) {
            throw new SelfRelationshipException("Inserting self as the sibling is not allowed");
        }
        parent.internalCheckNewChild(sibling, null);
        sibling.internalDetach(null, parent);
        sibling.internalSetNextSibling(this);
        if (previousSibling == null) {
            parent.internalGetContent(true).firstChild = sibling;
        } else {
            previousSibling.internalSetNextSibling(sibling);
        }
        sibling.internalSetPreviousSibling(previousSibling);
        previousSibling = sibling;
    }
    
    public final void CoreChildNode.coreInsertSiblingsBefore(CoreDocumentFragment fragment) {
        Content fragmentContent = fragment.internalGetContent(false);
        if (fragmentContent == null || fragmentContent.firstChild == null) {
            // Fragment is empty; nothing to do
            return;
        }
        CoreParentNode parent = coreGetParent();
        // TODO: check parent != null
        CoreChildNode child = fragmentContent.firstChild;
        while (child != null) {
            child.internalSetParent(parent);
            child = child.coreGetNextSiblingIfAvailable();
        }
        fragmentContent.lastChild.internalSetNextSibling(this);
        if (previousSibling == null) {
            parent.internalGetContent(true).firstChild = fragmentContent.firstChild;
        } else {
            previousSibling.internalSetNextSibling(fragmentContent.firstChild);
        }
        fragmentContent.firstChild.internalSetPreviousSibling(previousSibling);
        previousSibling = fragmentContent.lastChild;
        fragmentContent.firstChild = null;
        fragmentContent.lastChild = null;
    }
    
    public final void CoreChildNode.coreDetach(Semantics semantics) {
        internalDetach(semantics.getDetachPolicy(), null);
    }
    
    public final void CoreChildNode.coreDetach(CoreDocument newOwnerDocument) {
        internalDetach(DetachPolicy.NEW_DOCUMENT, null);
        owner = newOwnerDocument;
    }
    
    public final void CoreChildNode.internalDetach(DetachPolicy detachPolicy, CoreParentNode newParent) {
        CoreParentNode parent = coreGetParent();
        if (parent != null) {
            if (previousSibling == null) {
                parent.internalGetContent(true).firstChild = nextSibling;
            } else {
                previousSibling.internalSetNextSibling(nextSibling);
            }
            if (nextSibling == null) {
                parent.internalGetContent(true).lastChild = previousSibling;
            } else {
                nextSibling.internalSetPreviousSibling(previousSibling);
            }
            nextSibling = null;
            previousSibling = null;
            if (newParent == null) {
                internalUnsetParent(detachPolicy.getNewOwnerDocument(parent));
            }
        }
        if (newParent != null) {
            internalSetParent(newParent);
        }
    }

    public final void CoreChildNode.coreReplaceWith(CoreChildNode newNode, Semantics semantics) throws CoreModelException {
        if (newNode == this) {
            return;
        }
        CoreParentNode parent = coreGetParent();
        if (parent != null) {
            parent.internalCheckNewChild(newNode, this);
            newNode.internalDetach(null, parent);
            if (previousSibling == null) {
                parent.internalGetContent(true).firstChild = newNode;
            } else {
                previousSibling.internalSetNextSibling(newNode);
                newNode.internalSetPreviousSibling(previousSibling);
                previousSibling = null;
            }
            if (nextSibling == null) {
                parent.internalGetContent(true).lastChild = newNode;
            } else {
                nextSibling.internalSetPreviousSibling(newNode);
                newNode.internalSetNextSibling(nextSibling);
                nextSibling = null;
            }
            internalUnsetParent(semantics.getDetachPolicy().getNewOwnerDocument(parent));
        }
    }

    public final <T> CoreNode CoreChildNode.coreClone(ClonePolicy<T> policy, T options, CoreParentNode targetParent) throws CoreModelException {
        return internalClone(policy, options, targetParent);
    }
}
