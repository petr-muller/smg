/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
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
 *
 *  CPAchecker web page:
 *    http://cpachecker.sosy-lab.org
 */
package cz.afri.smg.objects.sll;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;

import cz.afri.smg.SMGAbstractionCandidate;
import cz.afri.smg.SMGAbstractionFinder;
import cz.afri.smg.graphs.ReadableSMG;
import cz.afri.smg.graphs.SMGEdgeHasValue;
import cz.afri.smg.graphs.SMGEdgeHasValueFilter;
import cz.afri.smg.graphs.SMGEdgePointsTo;
import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.types.CPointerType;

public class SMGSingleLinkedListFinder implements SMGAbstractionFinder {
  private static final int THRESHOLD_DEFAULT = 10;
	private ReadableSMG smg;
  private Map<SMGObject, Map<Integer, SMGSingleLinkedListCandidate>> candidates = new HashMap<>();
  private Map<Integer, Integer> inboundPointers = new HashMap<>();

  private final int seqLengthThreshold;

  public SMGSingleLinkedListFinder() {
    seqLengthThreshold = THRESHOLD_DEFAULT;
  }

  public SMGSingleLinkedListFinder(final int pSeqLengthThreshold) {
    seqLengthThreshold = pSeqLengthThreshold;
  }

  @Override
  public final Set<SMGAbstractionCandidate> traverse(final ReadableSMG pSmg) {
    smg = pSmg;

    buildInboundPointers();

    for (SMGObject object : smg.getHeapObjects()) {
      startTraversal(object);
    }

    Set<SMGAbstractionCandidate> returnSet = new HashSet<>();
    for (Map<Integer, SMGSingleLinkedListCandidate> objCandidates : candidates.values()) {
      for (SMGSingleLinkedListCandidate candidate : objCandidates.values()) {
        if (candidate.getLength() > seqLengthThreshold) {
          returnSet.add(candidate);
        }
      }
    }
    return Collections.unmodifiableSet(returnSet);
  }

  private void buildInboundPointers() {
    for (SMGEdgePointsTo pt : smg.getPTEdges()) {
      int pointer = pt.getValue();
      Iterable<SMGEdgeHasValue> hvEdges = smg.getHVEdges(new SMGEdgeHasValueFilter().filterHavingValue(pointer));
      inboundPointers.put(pointer, Iterables.size(hvEdges));
    }
  }

  private void startTraversal(final SMGObject pObject) {
    if (candidates.containsKey(pObject)) {
      // Processed already in continueTraversal
      return;
    }
    candidates.put(pObject, new HashMap<Integer, SMGSingleLinkedListCandidate>());
    for (SMGEdgeHasValue hv : smg.getHVEdges(SMGEdgeHasValueFilter.objectFilter(pObject))) {
      if (smg.isPointer(hv.getValue())) {
        SMGSingleLinkedListCandidate candidate = new SMGSingleLinkedListCandidate(pObject, hv.getOffset(), 1);
        candidates.get(pObject).put(hv.getOffset(), candidate);
        continueTraversal(hv.getValue(), candidate);
      }
    }
  }

  private void continueTraversal(final int pValue, final SMGSingleLinkedListCandidate pCandidate) {
    SMGEdgePointsTo pt = smg.getPointer(pValue);
    SMGObject object = pt.getObject();
    if (!candidates.containsKey(object)) {
      startTraversal(object);
    }

    if (inboundPointers.get(pValue) > 1) {
      return;
    }

    Map<Integer, SMGSingleLinkedListCandidate> objectCandidates = candidates.get(object);
    Integer offset = pCandidate.getOffset();

    if (!objectCandidates.containsKey(offset)) {
      //try to infer a pointer presence: either NULL, or uninitialized
      if (smg.isCoveredByNullifiedBlocks(object, offset, CPointerType.getVoidPointer())) {
        objectCandidates.put(offset, new SMGSingleLinkedListCandidate(object, offset, 1));
      }
    }

    if (objectCandidates.containsKey(offset)) {
      SMGSingleLinkedListCandidate myCandidate = objectCandidates.get(offset);
      if (pCandidate.isCompatibleWith(myCandidate)) {
        objectCandidates.remove(offset);
        pCandidate.addLength(myCandidate.getLength());
      }
    }
  }
}
