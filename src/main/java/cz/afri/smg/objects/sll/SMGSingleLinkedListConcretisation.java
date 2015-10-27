/*
 *  This file is part of SMG, a symbolic memory graph Java library
 *
 *  Copyright (C) 2015 Viktor Malík
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

package cz.afri.smg.objects.sll;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import cz.afri.smg.abstraction.SMGConcretisation;
import cz.afri.smg.graphs.ReadableSMG;
import cz.afri.smg.graphs.SMGEdgeHasValue;
import cz.afri.smg.graphs.SMGEdgeHasValueFilter;
import cz.afri.smg.graphs.SMGEdgePointsTo;
import cz.afri.smg.graphs.SMGFactory;
import cz.afri.smg.graphs.SMGValueFactory;
import cz.afri.smg.graphs.WritableSMG;
import cz.afri.smg.objects.SMGRegion;
import cz.afri.smg.types.CPointerType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

class SMGSingleLinkedListConcretisation implements SMGConcretisation {

  private final SMGSingleLinkedList sll;

  public SMGSingleLinkedListConcretisation(final SMGSingleLinkedList pSll) {
    sll = pSll;
  }

  @SuppressFBWarnings(value = "WMI_WRONG_MAP_ITERATOR", justification = "We need to iterate over keys here")
  @Override
  public final HashSet<ReadableSMG> execute(final ReadableSMG pSMG) {
    HashSet<ReadableSMG> resultSet = new HashSet<>();

    WritableSMG newSMG = SMGFactory.createWritableCopy(pSMG);

    // Create new concrete object
    SMGRegion region = new SMGRegion(sll.getSize(), sll.getLabel() + "_element");
    newSMG.addHeapObject(region);

    // Replace all edges pointing to SLL with ones pointing to new region
    Map<SMGEdgePointsTo, SMGEdgePointsTo> toReplace = new HashMap<>();
    for (SMGEdgePointsTo pt : newSMG.getPTEdges()) {
      if (pt.getObject().equals(sll)) {
        SMGEdgePointsTo newPt = new SMGEdgePointsTo(pt.getValue(), region, sll.getOffset());
        toReplace.put(pt, newPt);
      }
    }

    for (SMGEdgePointsTo pt : toReplace.keySet()) {
      newSMG.removePointsToEdge(pt.getValue());
      newSMG.addPointsToEdge(toReplace.get(pt));
    }

    // Create new connection between new region and SLL
    Integer newValue = SMGValueFactory.getNewValue();
    newSMG.addValue(newValue);
    SMGEdgeHasValue newValueHv = new SMGEdgeHasValue(new CPointerType(), sll.getOffset(), region, newValue);
    newSMG.addHasValueEdge(newValueHv);
    SMGEdgePointsTo newValuePt = new SMGEdgePointsTo(newValue, sll, sll.getOffset());
    newSMG.addPointsToEdge(newValuePt);

    if (sll.getLength() > 0) {
      // Shorten SLL
      sll.addLength(-1);
    } else {
      // For SLL of length 0+, there is a case, when it had length 0 and can be
      // removed though
      WritableSMG newSMGWithoutSll = SMGFactory.createWritableCopy(pSMG);

      Integer value;
      if (Iterables.isEmpty(newSMGWithoutSll.getHVEdges(SMGEdgeHasValueFilter.objectFilter(sll)
          .filterAtOffset(sll.getOffset()).filterByType(CPointerType.getVoidPointer())))) {
        // Create new value
        value = SMGValueFactory.getNewValue();
        newSMGWithoutSll.addValue(value);
      } else {
        // Get outbound edge at binding offset (next pointer of last list item)
        value = newSMGWithoutSll.getUniqueHV(SMGEdgeHasValueFilter.objectFilter(sll).filterAtOffset(sll.getOffset())
            .filterByType(CPointerType.getVoidPointer()), false).getValue();
      }

      for (SMGEdgePointsTo pt : newSMGWithoutSll.getPTEdges()) {
        if (pt.getObject().equals(sll)) {
          SMGEdgeHasValueFilter filter = new SMGEdgeHasValueFilter().filterHavingValue(pt.getValue());
          SMGEdgeHasValue oldHv = newSMGWithoutSll.getUniqueHV(filter, false);
          SMGEdgeHasValue newHv = new SMGEdgeHasValue(CPointerType.getVoidPointer(), sll.getOffset(), oldHv.getObject(),
              value);
          newSMGWithoutSll.addHasValueEdge(newHv);
          newSMGWithoutSll.removeHasValueEdge(oldHv);
          newSMGWithoutSll.removePointsToEdge(oldHv.getValue());
          newSMGWithoutSll.removeValue(oldHv.getValue());
        }
      }

      // Remove SLL and all appropriate edges
      SMGEdgeHasValueFilter filter = SMGEdgeHasValueFilter.objectFilter(sll);
      HashSet<SMGEdgeHasValue> toRemove = Sets.newHashSet(newSMGWithoutSll.getHVEdges(filter));
      for (SMGEdgeHasValue hv : toRemove) {
        newSMGWithoutSll.removeHasValueEdge(hv);
      }
      newSMGWithoutSll.removeHeapObject(sll);

      resultSet.add(newSMGWithoutSll);
    }

    resultSet.add(newSMG);

    return resultSet;
  }

  public final SMGSingleLinkedList getSll() {
    return sll;
  }
}
