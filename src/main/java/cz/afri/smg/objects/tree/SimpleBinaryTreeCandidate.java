/*
 *  This file is part of SMG, a symbolic memory graph Java library
 *  Originally developed as part of CPAChecker, the configurable software verification platform
 *
 *  Copyright (C) 2011-2015  Petr Muller
 *  Copyright (C) 2007-2014  Dirk Beyer
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package cz.afri.smg.objects.tree;

import java.util.ArrayDeque;
import java.util.Deque;

import cz.afri.smg.abstraction.SMGAbstractionCandidate;
import cz.afri.smg.graphs.ReadableSMG;
import cz.afri.smg.graphs.SMGEdgeHasValue;
import cz.afri.smg.graphs.SMGEdgeHasValueFilter;
import cz.afri.smg.graphs.SMGEdgePointsTo;
import cz.afri.smg.graphs.SMGFactory;
import cz.afri.smg.graphs.WritableSMG;
import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.objects.SMGRegion;
import cz.afri.smg.types.CPointerType;


class SimpleBinaryTreeCandidate implements SMGAbstractionCandidate {
  private final TreeBinding binding;
  private int depth;
  private final SMGObject root;
  private Boolean suitable = null;

  public SimpleBinaryTreeCandidate(final SMGObject pRoot, final int pLowOffset, final int pHighOffset,
                                   final int pDepth) {
    root = pRoot;
    depth = pDepth;
    binding = new TreeBinding(pLowOffset, pHighOffset, root.getSize());
  }

  @Override
  public final String toString() {
    return "SimpleBinaryTreeCandidate [binding=" + binding + ", depth=" + depth + ", root=" + root + ", suitable=" +
           suitable + "]";
  }

  @Override
  public final int getScore() {
    // TODO Auto-generated method stub
    return 0;
  }

  private void cleanSubtreeAtOffset(final WritableSMG pSMG, final Deque<SMGObject> pStack, final SMGObject pOrigin,
                                    final int pOffset) {
    SMGEdgeHasValue hvLower = pSMG.getUniqueHV(SMGEdgeHasValueFilter.objectFilter(pOrigin).filterAtOffset(pOffset),
                                               true);
    SMGEdgePointsTo ptLower = pSMG.getPointer(hvLower.getValue());
    pSMG.removeHasValueEdge(hvLower);
    if (ptLower.getValue() != pSMG.getNullValue()) {
      pSMG.removePointsToEdge(ptLower.getValue());
      pSMG.removeValue(ptLower.getValue());
      pStack.push(ptLower.getObject());
    }
  }

  private void cleanAbstractedObjects(final WritableSMG pNewSMG) {
    Deque<SMGObject> stack = new ArrayDeque<>();
    stack.push(root);
    while (!stack.isEmpty()) {
      SMGObject toDelete = stack.pop();
      cleanSubtreeAtOffset(pNewSMG, stack, toDelete, binding.getLowerOffset());
      cleanSubtreeAtOffset(pNewSMG, stack, toDelete, binding.getHigherOffset());
      pNewSMG.removeHeapObject(toDelete);
    }
  }

  @Override
  public final ReadableSMG execute(final ReadableSMG pSMG) {
    WritableSMG newSMG = SMGFactory.createWritableCopy(pSMG);
    SimpleBinaryTree tree = new SimpleBinaryTree(new SMGRegion(root.getSize(), "TREE"), binding.getLowerOffset(),
    		                                     binding.getHigherOffset(), depth);
    newSMG.addHeapObject(tree);
    newSMG.addHasValueEdge(new SMGEdgeHasValue(CPointerType.getVoidPointer(), binding.getLowerOffset(), tree,
    		                                   newSMG.getNullValue()));
    newSMG.addHasValueEdge(new SMGEdgeHasValue(CPointerType.getVoidPointer(), binding.getHigherOffset(), tree,
    		                                   newSMG.getNullValue()));
    for (SMGEdgePointsTo pt : pSMG.getPTEdges()) {
      if (pt.getObject().equals(root)) {
        newSMG.addPointsToEdge(new SMGEdgePointsTo(pt.getValue(), tree, pt.getOffset()));
      }
    }

    cleanAbstractedObjects(newSMG);
    return newSMG;
  }

  public final TreeBinding getBinding() {
    return binding;
  }

  public final int getDepth() {
    return depth;
  }

  public final boolean isSuitable() {
    if (suitable == null) {
      throw new IllegalStateException("isSuitable() called without previous suitability check");
    }
    return suitable;
  }

  public final boolean isProcessed() {
    return (suitable != null);
  }

  public final void setSuitable() {
    suitable = Boolean.TRUE;
  }

  public final void setUnsuitable() {
    suitable = Boolean.FALSE;
  }

  public final void absorb(final SimpleBinaryTreeCandidate pOne, final SimpleBinaryTreeCandidate pTwo) {
    pOne.setUnsuitable();
    pTwo.setUnsuitable();
    if (pOne.getDepth() > pTwo.getDepth()) {
      depth = depth + pOne.getDepth();
    } else {
      depth = depth + pTwo.getDepth();
    }
  }
}

class TreeBinding {

  @Override
  public String toString() {
    return "TreeBinding [l=" + lowerOffset + ", h=" + higherOffset + ", size=" + size + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + higherOffset;
    result = prime * result + lowerOffset;
    result = prime * result + size;
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
    	return true;
    }
    if (obj == null) {
    	return false;
    }
    if (getClass() != obj.getClass()) {
    	return false;
    }
    TreeBinding other = (TreeBinding) obj;
    if (higherOffset != other.higherOffset) {
    	return false;
    }
    if (lowerOffset != other.lowerOffset) {
    	return false;
    }
    if (size != other.size) {
    	return false;
    }
    return true;
  }

  private final int lowerOffset;
  private final int higherOffset;
  private final int size;

  TreeBinding(final int pLowOffset, final int pHighOffset, final int pSize) {
    if (pLowOffset > pHighOffset) {
      lowerOffset = pHighOffset;
      higherOffset = pLowOffset;
    } else {
      lowerOffset = pLowOffset;
      higherOffset = pHighOffset;
    }
    size = pSize;
  }

  int getLowerOffset() {
    return lowerOffset;
  }

  int getHigherOffset() {
    return higherOffset;
  }

  public int getSize() {
    // TODO Auto-generated method stub
    return size;
  }
}
