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
package cz.afri.smg.join;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import cz.afri.smg.graphs.ReadableSMG;
import cz.afri.smg.graphs.SMGEdgeHasValue;
import cz.afri.smg.graphs.SMGEdgeHasValueFilter;
import cz.afri.smg.objects.SMGAbstractObject;
import cz.afri.smg.objects.SMGObject;

final class SMGJoinMatchObjects {
  private boolean defined = false;
  private SMGJoinStatus status;

  private static boolean checkNull(final SMGObject pObj1, final SMGObject pObj2) {
    if (pObj1.notNull() && pObj2.notNull()) {
      return false;
    }

    return true;
  }

  private static boolean checkMatchingMapping(final SMGObject pObj1, final SMGObject pObj2, 
                                              final SMGNodeMapping pMapping1, final SMGNodeMapping pMapping2,
                                              final ReadableSMG pSMG1, final ReadableSMG pSMG2) {
    if (pMapping1.containsKey(pObj1) && pMapping2.containsKey(pObj2) &&
        pMapping1.get(pObj1) != pMapping2.get(pObj2)) {
      return true;
    }

    return false;
  }

  private static boolean checkConsistentMapping(final SMGObject pObj1, final SMGObject pObj2,
                                                final SMGNodeMapping pMapping1, final SMGNodeMapping pMapping2,
                                                final ReadableSMG pSMG1, final ReadableSMG pSMG2) {
    if ((pMapping1.containsKey(pObj1) && pMapping2.containsValue(pMapping1.get(pObj1))) ||
        (pMapping2.containsKey(pObj2) && pMapping1.containsValue(pMapping2.get(pObj2)))) {
      return true;
    }

    return false;
  }

  private static boolean checkConsistentObjects(final SMGObject pObj1, final SMGObject pObj2, final ReadableSMG pSMG1,
                                                final ReadableSMG pSMG2) {
    if ((pObj1.getSize() != pObj2.getSize()) ||
        (pSMG1.isObjectValid(pObj1) != pSMG2.isObjectValid(pObj2))) {
      return true;
    }

    return false;
  }

  private static boolean checkConsistentFields(final SMGObject pObj1, final SMGObject pObj2,
                                               final SMGNodeMapping pMapping1, final SMGNodeMapping pMapping2,
                                               final ReadableSMG pSMG1, final ReadableSMG pSMG2) {

    List<SMGEdgeHasValue> fields = new ArrayList<>();

    Iterables.addAll(fields, pSMG1.getHVEdges(SMGEdgeHasValueFilter.objectFilter(pObj1)));
    Iterables.addAll(fields, pSMG2.getHVEdges(SMGEdgeHasValueFilter.objectFilter(pObj2)));

    //TODO: We go through some fields twice, fix
    for (SMGEdgeHasValue hv : fields) {
      SMGEdgeHasValueFilter filter1 = SMGEdgeHasValueFilter.objectFilter(pObj1);
      filter1.filterByType(hv.getType()).filterAtOffset(hv.getOffset());
      Iterable<SMGEdgeHasValue> hv1 = pSMG1.getHVEdges(filter1);

      SMGEdgeHasValueFilter filter2 = SMGEdgeHasValueFilter.objectFilter(pObj2);
      filter2.filterByType(hv.getType()).filterAtOffset(hv.getOffset());
      Iterable<SMGEdgeHasValue> hv2 = pSMG2.getHVEdges(filter2);

      if (hv1.iterator().hasNext() && hv2.iterator().hasNext()) {
        Integer v1 = Iterators.getOnlyElement(hv1.iterator()).getValue();
        Integer v2 = Iterators.getOnlyElement(hv2.iterator()).getValue();
        if (pMapping1.containsKey(v1) && pMapping2.containsKey(v2) && !(pMapping1.get(v1).equals(pMapping2.get(v2)))) {
          return true;
        }
      }
    }

    return false;
  }

  private static boolean checkMatchingAbstractions(final SMGObject pObj1, final SMGObject pObj2) {
    if (pObj1.isAbstract() && pObj2.isAbstract()) {
      SMGAbstractObject pAbstract1 = (SMGAbstractObject) pObj1;
      SMGAbstractObject pAbstract2 = (SMGAbstractObject) pObj2;

      //TODO: It should be possible to join some of the different generic shapes, i.e. a SLL
      //      might be a more general segment than a DLL
      if (!(pAbstract1.matchGenericShape(pAbstract2) && pAbstract1.matchSpecificShape(pAbstract2))) {
          return true;
      }
    }

    return false;
  }

  public SMGJoinMatchObjects(final SMGJoinStatus pStatus, final ReadableSMG pSMG1, final ReadableSMG pSMG2,
                             final SMGNodeMapping pMapping1, final SMGNodeMapping pMapping2,
                             final SMGObject pObj1, final SMGObject pObj2) {
    if ((!pSMG1.getObjects().contains(pObj1)) || (!pSMG2.getObjects().contains(pObj2))) {
      throw new IllegalArgumentException();
    }

    if (SMGJoinMatchObjects.checkNull(pObj1, pObj2)) {
      return;
    }

    if (SMGJoinMatchObjects.checkMatchingMapping(pObj1, pObj2, pMapping1, pMapping2, pSMG1, pSMG2)) {
      return;
    }

    if (SMGJoinMatchObjects.checkConsistentMapping(pObj1, pObj2, pMapping1, pMapping2, pSMG1, pSMG2)) {
      return;
    }

    if (SMGJoinMatchObjects.checkConsistentObjects(pObj1, pObj2, pSMG1, pSMG2)) {
      return;
    }

    if (SMGJoinMatchObjects.checkMatchingAbstractions(pObj1, pObj2)) {
      return;
    }

    if (SMGJoinMatchObjects.checkConsistentFields(pObj1, pObj2, pMapping1, pMapping2, pSMG1, pSMG2)) {
      return;
    }

    status = SMGJoinMatchObjects.updateStatusForAbstractions(pObj1, pObj2, pStatus);
    defined = true;
  }

  private static SMGJoinStatus updateStatusForAbstractions(final SMGObject pObj1, final SMGObject pObj2,
                                                           final SMGJoinStatus pStatus) {
    if (pObj1.isMoreGeneral(pObj2)) {
      return SMGJoinStatus.updateStatus(pStatus, SMGJoinStatus.LEFT_ENTAIL);
    } else if (pObj2.isMoreGeneral(pObj1)) {
      return SMGJoinStatus.updateStatus(pStatus, SMGJoinStatus.RIGHT_ENTAIL);
    }
    return pStatus;
  }

  public SMGJoinStatus getStatus() {
    return status;
  }

  public boolean isDefined() {
    return defined;
  }
}
