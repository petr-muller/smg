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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cz.afri.smg.SMGAbstractionCandidate;
import cz.afri.smg.SMGAbstractionFinder;
import cz.afri.smg.graphs.ReadableSMG;
import cz.afri.smg.graphs.SMGEdgeHasValue;
import cz.afri.smg.graphs.SMGEdgeHasValueFilter;
import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.types.CPointerType;


class SimpleBinaryTreeFinder implements SMGAbstractionFinder {

  private ReadableSMG smg;
  private Map<SMGObject, Map<TreeBinding, SimpleBinaryTreeCandidate>> bindings = new HashMap<>();

  @Override
  public final String toString() {
    return "SimpleBinaryTreeFinder";
  }

  private void collectObviousBindingsOnObject(final SMGObject pObject) {
    Iterable<SMGEdgeHasValue> outerEdges = smg.getHVEdges(SMGEdgeHasValueFilter.objectFilter(pObject));
    Iterable<SMGEdgeHasValue> innerEdges = smg.getHVEdges(SMGEdgeHasValueFilter.objectFilter(pObject));

    Map<TreeBinding, SimpleBinaryTreeCandidate>objectBindings = new HashMap<>();
    bindings.put(pObject, objectBindings);

    for (SMGEdgeHasValue outer : outerEdges) {
      if (!smg.isPointer(outer.getValue())) {
        continue;
      }
      for (SMGEdgeHasValue inner : innerEdges) {
        if ((!smg.isPointer(inner.getValue())) || outer.overlapsWith(inner)) {
          continue;
        }
        SimpleBinaryTreeCandidate candidate = new SimpleBinaryTreeCandidate(pObject, outer.getOffset(),
                                                                            inner.getOffset(), 1);
        objectBindings.put(candidate.getBinding(), candidate);
      }
    }
  }

  private void collectObviousBindings() {
    for (SMGObject object : smg.getHeapObjects()) {
      collectObviousBindingsOnObject(object);
    }
  }

  private SMGObject getSuccessorOnOffset(final SMGObject pNode, final int pOffset) {
    SMGEdgeHasValueFilter filter = SMGEdgeHasValueFilter.objectFilter(pNode).filterAtOffset(pOffset);
    Iterable<SMGEdgeHasValue> edgesOnOffset = smg.getHVEdges(filter);
    if (edgesOnOffset.iterator().hasNext()) {
      return smg.getObjectPointedBy(edgesOnOffset.iterator().next().getValue());
    } else if (smg.isCoveredByNullifiedBlocks(pNode, pOffset, CPointerType.getVoidPointer())) {
      return smg.getNullObject();
    }
    throw new UnsupportedOperationException("Not yet implemented");
  }

  private SimpleBinaryTreeCandidate processCandidateOnNode(final SimpleBinaryTreeCandidate pCandidate, 
                                                           final SMGObject pNode) {
    TreeBinding binding = pCandidate.getBinding();
    if (!pNode.notNull()) {
      SimpleBinaryTreeCandidate candidate = new SimpleBinaryTreeCandidate(pNode, binding.getLowerOffset(),
                                                                          binding.getHigherOffset(), 0);
      candidate.setSuitable();
      return candidate;
    }

    Map<TreeBinding, SimpleBinaryTreeCandidate> myCandidates = bindings.get(pNode);
    if (!myCandidates.containsKey(binding)) {
      myCandidates.put(binding, new SimpleBinaryTreeCandidate(pNode, binding.getLowerOffset(),
                                                              binding.getHigherOffset(), 1));
    }
    SimpleBinaryTreeCandidate candidate = myCandidates.get(binding);
    if (!candidate.isProcessed()) {
      SMGObject lowNode = getSuccessorOnOffset(pNode, binding.getLowerOffset());
      SMGObject highNode = getSuccessorOnOffset(pNode, binding.getHigherOffset());
      SimpleBinaryTreeCandidate lowCandidate = processCandidateOnNode(candidate, lowNode);
      SimpleBinaryTreeCandidate highCandidate = processCandidateOnNode(candidate, highNode);
      if (lowCandidate.isSuitable() && highCandidate.isSuitable()) {
        candidate.setSuitable();
        candidate.absorb(lowCandidate, highCandidate);
      } else {
        candidate.setUnsuitable();
      }
    }

    return myCandidates.get(binding);
  }

  private void processNode(final SMGObject pObject) {
    for (SimpleBinaryTreeCandidate candidate : bindings.get(pObject).values()) {
      if (candidate.isProcessed()) {
        continue;
      }

      processCandidateOnNode(candidate, pObject);
    }
  }

  private void processAllNodes() {
    for (SMGObject object : smg.getHeapObjects()) {
      processNode(object);
    }
  }

  @Override
  public final Set<SMGAbstractionCandidate> traverse(final ReadableSMG pSmg) {
    smg = pSmg;
    collectObviousBindings();
    processAllNodes();
    Set<SMGAbstractionCandidate> found = new HashSet<>();
    for (Map<TreeBinding, SimpleBinaryTreeCandidate> map : bindings.values()) {
      for (SimpleBinaryTreeCandidate candidate : map.values()) {
        if (candidate.isSuitable() && candidate.getDepth() > 2) {
          found.add(candidate);
        }
      }
    }
    return found;
  }
}
