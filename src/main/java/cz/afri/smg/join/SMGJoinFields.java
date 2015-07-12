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

import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import cz.afri.smg.graphs.ReadableSMG;
import cz.afri.smg.graphs.SMGEdgeHasValue;
import cz.afri.smg.graphs.SMGEdgeHasValueFilter;
import cz.afri.smg.graphs.SMGFactory;
import cz.afri.smg.graphs.SMGValueFactory;
import cz.afri.smg.graphs.WritableSMG;
import cz.afri.smg.objects.SMGObject;

class SMGJoinFields {
    private final ReadableSMG finalSMG1;
    private final ReadableSMG finalSMG2;
    private SMGJoinStatus status = SMGJoinStatus.EQUAL;

    public SMGJoinFields(final ReadableSMG pSMG1, final ReadableSMG pSMG2, final SMGObject pObj1,
                         final SMGObject pObj2) {
        if (pObj1.getSize() != pObj2.getSize()) {
            throw new IllegalArgumentException("SMGJoinFields object arguments need to have identical size");
        }
        if (!(pSMG1.getObjects().contains(pObj1) && pSMG2.getObjects().contains(pObj2))) {
            throw new IllegalArgumentException("SMGJoinFields object arguments need to be included in parameter SMGs");
        }

        Set<SMGEdgeHasValue> h1Prime = getCompatibleHVEdgeSet(pSMG1, pSMG2, pObj1, pObj2);
        Set<SMGEdgeHasValue> h2Prime = getCompatibleHVEdgeSet(pSMG2, pSMG1, pObj2, pObj1);

        WritableSMG newSMG1 = SMGFactory.createWritableCopy(pSMG1);
        WritableSMG newSMG2 = SMGFactory.createWritableCopy(pSMG2);

        newSMG1.replaceHVSet(h1Prime);
        newSMG2.replaceHVSet(h2Prime);

        status = joinFieldsRelaxStatus(pSMG1, newSMG1, status, SMGJoinStatus.RIGHT_ENTAIL, pObj1);
        status = joinFieldsRelaxStatus(pSMG2, newSMG2, status, SMGJoinStatus.LEFT_ENTAIL, pObj2);

        Set<SMGEdgeHasValue> smg2Extension = mergeNonNullHasValueEdges(newSMG1, newSMG2, pObj1, pObj2);
        Set<SMGEdgeHasValue> smg1Extension = mergeNonNullHasValueEdges(newSMG2, newSMG1, pObj2, pObj1);

        h1Prime.addAll(smg1Extension);
        h2Prime.addAll(smg2Extension);

        newSMG1.replaceHVSet(h1Prime);
        newSMG2.replaceHVSet(h2Prime);

        finalSMG1 = newSMG1;
        finalSMG2 = newSMG2;
    }

    public SMGJoinStatus getStatus() {
        return status;
    }

    public ReadableSMG getSMG1() {
        return finalSMG1;
    }

    public ReadableSMG getSMG2() {
        return finalSMG2;
    }

  public static Set<SMGEdgeHasValue> mergeNonNullHasValueEdges(final ReadableSMG pSMG1, final ReadableSMG pSMG2,
                                                               final SMGObject pObj1, final SMGObject pObj2) {
        Set<SMGEdgeHasValue> returnSet = new HashSet<>();

        SMGEdgeHasValueFilter filterForSMG1 = SMGEdgeHasValueFilter.objectFilter(pObj1);
        SMGEdgeHasValueFilter filterForSMG2 = SMGEdgeHasValueFilter.objectFilter(pObj2);
        filterForSMG1.filterNotHavingValue(pSMG1.getNullValue());

        for (SMGEdgeHasValue edge : pSMG1.getHVEdges(filterForSMG1)) {
            filterForSMG2.filterAtOffset(edge.getOffset());
            filterForSMG2.filterByType(edge.getType());
            if (!pSMG2.getHVEdges(filterForSMG2).iterator().hasNext()) {
              returnSet.add(new SMGEdgeHasValue(edge.getType(), edge.getOffset(), pObj2,
                                                SMGValueFactory.getNewValue()));
            }
        }

        return Collections.unmodifiableSet(returnSet);
    }

    public static SMGJoinStatus joinFieldsRelaxStatus(final ReadableSMG pOrigSMG, final ReadableSMG pNewSMG,
                                                      final SMGJoinStatus pCurStatus, final SMGJoinStatus pNewStatus,
                                                      final SMGObject pObject) {
        BitSet origNull = pOrigSMG.getNullBytesForObject(pObject);
        BitSet newNull = pNewSMG.getNullBytesForObject(pObject);

        for (int i = 0; i < origNull.length(); i++) {
            if (origNull.get(i) && (!newNull.get(i))) {
                return SMGJoinStatus.updateStatus(pCurStatus, pNewStatus);
            }
        }

        return pCurStatus;
    }

    public static Set<SMGEdgeHasValue> getCompatibleHVEdgeSet(final ReadableSMG pSMG1, final ReadableSMG pSMG2,
                                                              final SMGObject pObj1, final SMGObject pObj2) {
        Set<SMGEdgeHasValue> newHVSet = SMGJoinFields.getHVSetWithoutNullValuesOnObject(pSMG1, pObj1);

        newHVSet.addAll(SMGJoinFields.getHVSetOfCommonNullValues(pSMG1, pSMG2, pObj1, pObj2));
        newHVSet.addAll(SMGJoinFields.getHVSetOfMissingNullValues(pSMG1, pSMG2, pObj1, pObj2));

        return newHVSet;
    }

    public static Set<SMGEdgeHasValue> getHVSetOfMissingNullValues(final ReadableSMG pSMG1, final ReadableSMG pSMG2,
                                                                   final SMGObject pObj1, final SMGObject pObj2) {
        Set<SMGEdgeHasValue> retset = new HashSet<>();

        SMGEdgeHasValueFilter nonNullPtrInSmg2 = SMGEdgeHasValueFilter.objectFilter(pObj2);
        nonNullPtrInSmg2.filterNotHavingValue(pSMG2.getNullValue());
        SMGEdgeHasValueFilter nonNullPtrInSmg1 = SMGEdgeHasValueFilter.objectFilter(pObj1);
        nonNullPtrInSmg1.filterNotHavingValue(pSMG1.getNullValue());

        for (SMGEdgeHasValue edge : pSMG2.getHVEdges(nonNullPtrInSmg2)) {
            if (!pSMG2.isPointer(edge.getValue())) {
                continue;
            }

            nonNullPtrInSmg1.filterAtOffset(edge.getOffset());

            if (!pSMG1.getHVEdges(nonNullPtrInSmg1).iterator().hasNext()) {
                BitSet newNullBytes = pSMG1.getNullBytesForObject(pObj1);
                int min = edge.getOffset();
                int max = edge.getOffset() + edge.getSizeInBytes();

                if (newNullBytes.get(min) && newNullBytes.nextClearBit(min) >= max) {
                    retset.add(new SMGEdgeHasValue(edge.getType(), edge.getOffset(), pObj1, pSMG1.getNullValue()));
                }
            }
        }
        return retset;
    }

    public static Set<SMGEdgeHasValue> getHVSetOfCommonNullValues(final ReadableSMG pSMG1, final ReadableSMG pSMG2,
                                                                  final SMGObject pObj1, final SMGObject pObj2) {
        Set<SMGEdgeHasValue> retset = new HashSet<>();
        BitSet nullBytes = pSMG1.getNullBytesForObject(pObj1);

        nullBytes.and(pSMG2.getNullBytesForObject(pObj2));

        int size = 0;
        for (int i = nullBytes.nextSetBit(0); i >= 0; i = nullBytes.nextSetBit(i + 1)) {
            size++;

            if (size > 0 && ((i + 1 == nullBytes.length()) || !(nullBytes.get(i + 1)))) {
                SMGEdgeHasValue newHV = new SMGEdgeHasValue(size, (i - size) + 1, pObj1, pSMG1.getNullValue());
                retset.add(newHV);
                size = 0;
            }
        }

        return Collections.unmodifiableSet(retset);
    }

    public static Set<SMGEdgeHasValue> getHVSetWithoutNullValuesOnObject(final ReadableSMG pSMG, final SMGObject pObj) {
        Set<SMGEdgeHasValue> retset = new HashSet<>();
        Iterables.addAll(retset, pSMG.getHVEdges());

        SMGEdgeHasValueFilter nullValueFilter = SMGEdgeHasValueFilter.objectFilter(pObj);
        nullValueFilter.filterHavingValue(pSMG.getNullValue());

        retset.removeAll(Sets.newHashSet(pSMG.getHVEdges(nullValueFilter)));

        return retset;
    }

    private static void checkResultConsistencySingleSide(final ReadableSMG pSMG1,
                                                         final SMGEdgeHasValueFilter nullEdges1,
                                                         final ReadableSMG pSMG2, final SMGObject pObj2,
                                                         final BitSet nullBytesInSMG2) {
        for (SMGEdgeHasValue edgeInSMG1 : pSMG1.getHVEdges(nullEdges1)) {
            int start = edgeInSMG1.getOffset();
            int byteAfterEnd = start + edgeInSMG1.getSizeInBytes();

            SMGEdgeHasValueFilter filter = SMGEdgeHasValueFilter.objectFilter(pObj2);
            filter.filterAtOffset(edgeInSMG1.getOffset()).filterByType(edgeInSMG1.getType());

            Iterable<SMGEdgeHasValue> hvInSMG2Set = pSMG2.getHVEdges(filter);

            SMGEdgeHasValue hvInSMG2;
            if (hvInSMG2Set.iterator().hasNext()) {
                hvInSMG2 = Iterables.getOnlyElement(hvInSMG2Set);
            } else {
                hvInSMG2 = null;
            }

            if (hvInSMG2 == null || (nullBytesInSMG2.nextClearBit(start) < byteAfterEnd &&
                !pSMG2.isPointer(hvInSMG2.getValue()))) {
                throw new IllegalStateException("SMGJoinFields output assertions do not hold");
            }
        }

    }

    public static void checkResultConsistency(final ReadableSMG pSMG1, final ReadableSMG pSMG2, final SMGObject pObj1,
                                              final SMGObject pObj2) {
        SMGEdgeHasValueFilter nullEdges1 = SMGEdgeHasValueFilter.objectFilter(pObj1);
        nullEdges1.filterHavingValue(pSMG1.getNullValue());

        SMGEdgeHasValueFilter nullEdges2 = SMGEdgeHasValueFilter.objectFilter(pObj2);
        nullEdges2.filterHavingValue(pSMG2.getNullValue());
        BitSet nullBytesInSMG1 = pSMG1.getNullBytesForObject(pObj1);
        BitSet nullBytesInSMG2 = pSMG2.getNullBytesForObject(pObj2);

        if (Iterables.size(pSMG1.getHVEdges(SMGEdgeHasValueFilter.objectFilter(pObj1))) !=
            Iterables.size(pSMG2.getHVEdges(SMGEdgeHasValueFilter.objectFilter(pObj2)))) {
          String message = "SMGJoinFields output assertion does not hold: objects do not have identical sets of fields";
            throw new IllegalStateException(message);
        }

        checkResultConsistencySingleSide(pSMG1, nullEdges1, pSMG2, pObj2, nullBytesInSMG2);
        checkResultConsistencySingleSide(pSMG2, nullEdges2, pSMG1, pObj1, nullBytesInSMG1);
    }
}
