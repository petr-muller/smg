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

import java.util.HashMap;
import java.util.Map;

import cz.afri.smg.SMGAbstractionCandidate;
import cz.afri.smg.graphs.ReadableSMG;
import cz.afri.smg.graphs.SMGEdgeHasValue;
import cz.afri.smg.graphs.SMGEdgeHasValueFilter;
import cz.afri.smg.graphs.SMGEdgePointsTo;
import cz.afri.smg.graphs.SMGFactory;
import cz.afri.smg.graphs.WritableSMG;
import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.objects.SMGRegion;
import cz.afri.smg.types.CPointerType;
import cz.afri.smg.types.CType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class SMGSingleLinkedListCandidate implements SMGAbstractionCandidate {
  private final SMGObject start;
  private final int offset;
  private int length;

  public SMGSingleLinkedListCandidate(final SMGObject pStart, final int pOffset, final int pLength) {
    start = pStart;
    offset = pOffset;
    length = pLength;
  }

  @Override
  public final int getScore() {
    return 0;
  }

  @SuppressFBWarnings(value = "WMI_WRONG_MAP_ITERATOR", justification = "We need to iterate over keys here")
  @Override
  public final ReadableSMG execute(final ReadableSMG pSMG) {
    // TMP: This will result in a new SMG
    WritableSMG newSMG = SMGFactory.createWritableCopy(pSMG);

    // TMP: Create an appropriate SLL and add it to new SMG
    SMGSingleLinkedList sll = new SMGSingleLinkedList((SMGRegion) start, offset, length);
    newSMG.addHeapObject(sll);

    Map<SMGEdgePointsTo, SMGEdgePointsTo> toReplace = new HashMap<>();

    // TMP: Replace all edges pointing to starting element with ones leading to the SLL
    //TODO: Better filtering of the pointers!!!
    for (SMGEdgePointsTo pt : newSMG.getPTEdges()) {
      if (pt.getObject().equals(start)) {
        SMGEdgePointsTo newPt = new SMGEdgePointsTo(pt.getValue(), sll, pt.getOffset());
        toReplace.put(pt, newPt);
      }
    }

    for (SMGEdgePointsTo pt : toReplace.keySet()) {
      newSMG.removePointsToEdge(pt.getValue());
      newSMG.addPointsToEdge(toReplace.get(pt));
    }

    SMGObject node = start;
    Integer value = null;
    SMGEdgeHasValue edgeToFollow = null;
    for (int i = 0; i < length; i++) {
      if (value != null) {
        newSMG.removePointsToEdge(value);
        newSMG.removeValue(value);
      }

      SMGEdgeHasValueFilter filter = SMGEdgeHasValueFilter.objectFilter(node).filterAtOffset(offset);
      Iterable<SMGEdgeHasValue> outboundEdges = newSMG.getHVEdges(filter);
      edgeToFollow = null;
      for (SMGEdgeHasValue outbound : outboundEdges) {
        CType fieldType = outbound.getType();
        if (fieldType instanceof CPointerType) {
          edgeToFollow = outbound;
        }
      }
      if (edgeToFollow == null) {
        edgeToFollow = new SMGEdgeHasValue(CPointerType.getVoidPointer(), offset, node, newSMG.getNullValue());
      }

      value = edgeToFollow.getValue();
      newSMG.removeHeapObject(node);
      node = newSMG.getPointer(value).getObject();
    }
    SMGEdgeHasValue newOutbound = new SMGEdgeHasValue(edgeToFollow.getType(), offset, sll, value);
    newSMG.addHasValueEdge(newOutbound);

    return newSMG;
  }

  public final int getOffset() {
    return offset;
  }

  public final int getLength() {
    return length;
  }

  public final void addLength(final int pLength) {
    length += pLength;
  }

  public final boolean isCompatibleWith(final SMGSingleLinkedListCandidate pOther) {
    return (offset == pOther.offset) && (start.getSize() == pOther.start.getSize());
  }

  public final SMGObject getStart() {
    return start;
  }

  @Override
  public final String toString() {
    return "SLL CANDIDATE(start=" + start + ", offset=" + offset + ", length=" + length + ")";
  }
}
