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
package cz.afri.smg.join;

import cz.afri.smg.graphs.ReadableSMG;
import cz.afri.smg.graphs.SMGValueFactory;
import cz.afri.smg.graphs.SMGValues.SMGExplicitValue;
import cz.afri.smg.graphs.SMGValues.SMGKnownExpValue;
import cz.afri.smg.graphs.SMGValues.SMGKnownSymValue;
import cz.afri.smg.graphs.WritableSMG;

final class SMGJoinValues {
  private SMGJoinStatus status;
  private ReadableSMG inputSMG1;
  private ReadableSMG inputSMG2;
  private WritableSMG destSMG;
  private Integer value;
  private SMGNodeMapping mapping1;
  private SMGNodeMapping mapping2;
  private boolean defined = false;

  @SuppressWarnings("unused")
  private static boolean joinValuesIdentical(final SMGJoinValues pJV, final Integer pV1, final Integer pV2) {
    if (pV1 == pV2) {
      pJV.value = pV1;
      pJV.defined = true;
    }

    return pV1.equals(pV2);
  }

  private static boolean joinValuesAlreadyJoined(final SMGJoinValues pJV, final Integer pV1, final Integer pV2) {
    if (pJV.mapping1.containsKey(pV1) && pJV.mapping2.containsKey(pV2) &&
        pJV.mapping1.get(pV1).equals(pJV.mapping2.get(pV2))) {
      pJV.value = pJV.mapping1.get(pV1);
      pJV.defined = true;
      return true;
    }

    return false;
  }

  private static boolean joinValuesNonPointers(final SMGJoinValues pJV, final Integer pV1, final Integer pV2) {
    if ((!pJV.inputSMG1.isPointer(pV1)) && (!pJV.inputSMG2.isPointer(pV2))) {
      if (pJV.mapping1.containsKey(pV1) || pJV.mapping2.containsKey(pV2)) {
        return true;
      }
      SMGExplicitValue exp1 = pJV.inputSMG1.getExplicit(SMGKnownSymValue.valueOf(pV1));
      SMGExplicitValue exp2 = pJV.inputSMG2.getExplicit(SMGKnownSymValue.valueOf(pV2));

      Integer newValue = SMGValueFactory.getNewValue();
      pJV.destSMG.addValue(newValue);
      pJV.mapping1.map(pV1, newValue);
      pJV.mapping2.map(pV2, newValue);
      pJV.defined = true;
      pJV.value = newValue;

      if ((!exp1.isUnknown()) && (!exp2.isUnknown())) {
        SMGKnownExpValue knownExp1 = (SMGKnownExpValue) exp1;
        SMGKnownExpValue knownExp2 = (SMGKnownExpValue) exp2;
        if (knownExp1.equals(knownExp2)) {
          pJV.destSMG.putExplicit(SMGKnownSymValue.valueOf(newValue), knownExp1);
        } else {
          pJV.status = SMGJoinStatus.updateStatus(pJV.status, SMGJoinStatus.INCOMPARABLE);
        }
      } else if (exp1.isUnknown() && (!exp2.isUnknown())) {
        pJV.status = SMGJoinStatus.updateStatus(pJV.status, SMGJoinStatus.LEFT_ENTAIL);
      } else if (exp2.isUnknown() && (!exp1.isUnknown())) {
        pJV.status = SMGJoinStatus.updateStatus(pJV.status, SMGJoinStatus.RIGHT_ENTAIL);
      }
      return true;
    }
    return false;
  }

  private static boolean joinValuesMixedPointers(final SMGJoinValues pJV, final Integer pV1, final Integer pV2) {
    return ((!pJV.inputSMG1.isPointer(pV1)) || (!pJV.inputSMG2.isPointer(pV2)));
  }

  private static boolean joinValuesPointers(final SMGJoinValues pJV, final Integer pV1, final Integer pV2) {
    SMGJoinTargetObjects jto = new SMGJoinTargetObjects(pJV.status,
                                                        pJV.inputSMG1, pJV.inputSMG2, pJV.destSMG,
                                                        pJV.mapping1, pJV.mapping2,
                                                        pV1, pV2);
    if (jto.isDefined()) {
      pJV.status = jto.getStatus();
      pJV.inputSMG1 = jto.getInputSMG1();
      pJV.inputSMG2 = jto.getInputSMG2();
      pJV.destSMG = jto.getDestinationSMG();
      pJV.mapping1 = jto.getMapping1();
      pJV.mapping2 = jto.getMapping2();
      pJV.value = jto.getValue();
      pJV.defined = true;
      return true;
    }
    if (jto.isRecoverable()) {
      return false;
    }

    pJV.defined = false;
    return true;
  }

  @SuppressWarnings("checkstyle:parameternumber")
  public SMGJoinValues(final SMGJoinStatus pStatus, final ReadableSMG pSMG1, final ReadableSMG pSMG2,
                       final WritableSMG pDestSMG, final SMGNodeMapping pMapping1, final SMGNodeMapping pMapping2,
                        final Integer pValue1, final Integer pValue2) {
    mapping1 = pMapping1;
    mapping2 = pMapping2;
    status = pStatus;
    inputSMG1 = pSMG1;
    inputSMG2 = pSMG2;
    destSMG = pDestSMG;

//    TODO: Currently, this happens even when we join different SMGs, which have identical sbymbolic values,
//          but are not really identical. We might need to relabel the values before the full join
//          to ensure the values are disjunct
//    if (SMGJoinValues.joinValuesIdentical(this, pValue1, pValue2)) {
//      return;
//    }

    if (SMGJoinValues.joinValuesAlreadyJoined(this, pValue1, pValue2)) {
      return;
    }

    if (SMGJoinValues.joinValuesNonPointers(this, pValue1, pValue2)) {
      return;
    }

    if (SMGJoinValues.joinValuesMixedPointers(this, pValue1, pValue2)) {
      return;
    }

    if (SMGJoinValues.joinValuesPointers(this, pValue1, pValue2)) {
      return;
    }

    // TODO: [JOIN] Recoverable failure
  }

  public SMGJoinStatus getStatus() {
    return status;
  }

  public ReadableSMG getInputSMG1() {
    return inputSMG1;
  }

  public ReadableSMG getInputSMG2() {
    return inputSMG2;
  }

  public WritableSMG getDestinationSMG() {
    return destSMG;
  }

  public Integer getValue() {
    return value;
  }

  public SMGNodeMapping getMapping1() {
    return mapping1;
  }

  public SMGNodeMapping getMapping2() {
    return mapping2;
  }

  public boolean isDefined() {
    return defined;
  }
}
