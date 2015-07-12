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

import java.util.Set;

import com.google.common.collect.Sets;

import cz.afri.smg.graphs.ReadableSMG;
import cz.afri.smg.graphs.SMGEdgeHasValue;
import cz.afri.smg.graphs.SMGEdgeHasValueFilter;
import cz.afri.smg.graphs.WritableSMG;
import cz.afri.smg.objects.SMGObject;


final class SMGJoinSubSMGs {
  private static boolean performChecks = false;
  public static void performChecks(final boolean pValue) {
    performChecks = pValue;
  }

  private SMGJoinStatus status;
  private boolean defined = false;

  private ReadableSMG inputSMG1;
  private ReadableSMG inputSMG2;
  private WritableSMG destSMG;

  private SMGNodeMapping mapping1 = null;
  private SMGNodeMapping mapping2 = null;

  @SuppressWarnings("checkstyle:parameternumber")
  public SMGJoinSubSMGs(final SMGJoinStatus initialStatus, final ReadableSMG pSMG1, final ReadableSMG pSMG2,
                        final WritableSMG pDestSMG, final SMGNodeMapping pMapping1, final SMGNodeMapping pMapping2,
                        final SMGObject pObj1, final SMGObject pObj2, final SMGObject pNewObject) {

    SMGJoinFields joinFields = new SMGJoinFields(pSMG1, pSMG2, pObj1, pObj2);

    inputSMG1 = joinFields.getSMG1();
    inputSMG2 = joinFields.getSMG2();

    if (SMGJoinSubSMGs.performChecks) {
      SMGJoinFields.checkResultConsistency(inputSMG1, inputSMG2, pObj1, pObj2);
    }

    destSMG = pDestSMG;
    status = SMGJoinStatus.updateStatus(initialStatus, joinFields.getStatus());
    mapping1 = pMapping1;
    mapping2 = pMapping2;

    /*
     * After joinFields, the objects have identical set of fields. Therefore, to iterate
     * over them, it is sufficient to loop over HV set in the first SMG, and just
     * obtain the (always just single one) corresponding edge from the second
     * SMG.
     */

    SMGEdgeHasValueFilter filterOnSMG1 = SMGEdgeHasValueFilter.objectFilter(pObj1);
    SMGEdgeHasValueFilter filterOnSMG2 = SMGEdgeHasValueFilter.objectFilter(pObj2);

    Set<SMGEdgeHasValue> edgesOnObject1 = Sets.newHashSet(inputSMG1.getHVEdges(filterOnSMG1));

    for (SMGEdgeHasValue hvIn1 : edgesOnObject1) {
      filterOnSMG2.filterAtOffset(hvIn1.getOffset());
      filterOnSMG2.filterByType(hvIn1.getType());
      SMGEdgeHasValue hvIn2 = inputSMG2.getUniqueHV(filterOnSMG2, performChecks);

      SMGJoinValues joinValues = new SMGJoinValues(status, inputSMG1, inputSMG2, destSMG,
          mapping1, mapping2, hvIn1.getValue(), hvIn2.getValue() /*, ldiff */);

      if (!joinValues.isDefined()) {
        return;
      }

      status = SMGJoinStatus.updateStatus(status, joinValues.getStatus());
      inputSMG1 = joinValues.getInputSMG1();
      inputSMG2 = joinValues.getInputSMG2();
      destSMG = joinValues.getDestinationSMG();
      mapping1 = joinValues.getMapping1();
      mapping2 = joinValues.getMapping2();
      SMGEdgeHasValue newHV = new SMGEdgeHasValue(hvIn1.getType(), hvIn1.getOffset(), pNewObject,
                                                  joinValues.getValue());
      destSMG.addHasValueEdge(newHV);
    }
    defined = true;
  }

  public boolean isDefined() {
    return defined;
  }

  public SMGJoinStatus getStatus() {
    return status;
  }

  public ReadableSMG getSMG1() {
    return inputSMG1;
  }

  public ReadableSMG getSMG2() {
    return inputSMG2;
  }

  public ReadableSMG getDestSMG() {
    return destSMG;
  }

  public SMGNodeMapping getMapping1() {
    return mapping1;
  }

  public SMGNodeMapping getMapping2() {
    return mapping2;
  }
}
