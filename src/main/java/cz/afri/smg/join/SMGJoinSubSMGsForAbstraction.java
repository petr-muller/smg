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

import java.util.Collections;
import java.util.Set;

import cz.afri.smg.graphs.ReadableSMG;
import cz.afri.smg.objects.SMGObject;

public final class SMGJoinSubSMGsForAbstraction {
  private SMGJoinStatus status = null;
  private final ReadableSMG resultSMG = null;
  private final SMGObject newAbstractObject = null;

  private final Set<SMGObject> nonSharedObjectsFromSMG1 = null;
  private final Set<Integer> nonSharedValuesFromSMG1 = null;
  private final Set<SMGObject> nonSharedObjectsFromSMG2 = null;
  private final Set<Integer> nonSharedValuesFromSMG2 = null;

  private boolean defined = false;

  public SMGJoinSubSMGsForAbstraction(final ReadableSMG pSMG, final SMGObject pObj1, final SMGObject pObj2) {

  }

  public boolean isDefined() {
    return defined;
  }

  public SMGJoinStatus getStatus() {
    return status;
  }

  public ReadableSMG getResultSMG() {
    return resultSMG;
  }

  public SMGObject getNewAbstractObject() {
    return newAbstractObject;
  }

  public Set<SMGObject> getNonSharedObjectsFromSMG1() {
    return Collections.unmodifiableSet(nonSharedObjectsFromSMG1);
  }

  public Set <Integer> getNonSharedValuesFromSMG1() {
    return Collections.unmodifiableSet(nonSharedValuesFromSMG1);
  }

  public Set<SMGObject> getNonSharedObjectsFromSMG2() {
    return Collections.unmodifiableSet(nonSharedObjectsFromSMG2);
  }

  public Set<Integer> getNonSharedValuesFromSMG2() {
    return Collections.unmodifiableSet(nonSharedValuesFromSMG2);
  }
}
