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

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cz.afri.smg.graphs.CLangStackFrame;
import cz.afri.smg.graphs.ReadableSMG;
import cz.afri.smg.graphs.SMGFactory;
import cz.afri.smg.graphs.WritableSMG;
import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.objects.SMGRegion;
import cz.afri.smg.types.CType;

public final class SMGJoin {
  public static void performChecks(final boolean pOn) {
    SMGJoinSubSMGs.performChecks(pOn);
  }

  private boolean defined = false;
  private SMGJoinStatus status = SMGJoinStatus.EQUAL;
  private final WritableSMG smg;

  public SMGJoin(final ReadableSMG pSMG1, final ReadableSMG pSMG2) {
    ReadableSMG opSMG1 = SMGFactory.createWritableCopy(pSMG1);
    ReadableSMG opSMG2 = SMGFactory.createWritableCopy(pSMG2);
    smg = SMGFactory.createWritableSMG();

    SMGNodeMapping mapping1 = new SMGNodeMapping();
    SMGNodeMapping mapping2 = new SMGNodeMapping();

    Map<String, SMGRegion> globalsInSmg1 = opSMG1.getGlobalObjects();
    ArrayDeque<CLangStackFrame> stackInSmg1 = opSMG1.getStackFrames();
    Map<String, SMGRegion> globalsInSmg2 = opSMG2.getGlobalObjects();
    ArrayDeque<CLangStackFrame> stackInSmg2 = opSMG2.getStackFrames();

    Set<String> globalVars = new HashSet<>();
    globalVars.addAll(globalsInSmg1.keySet());
    globalVars.addAll(globalsInSmg2.keySet());

    for (String globalVar : globalVars) {
      SMGRegion globalInSMG1 = globalsInSmg1.get(globalVar);
      SMGRegion globalInSMG2 = globalsInSmg2.get(globalVar);
      if (globalInSMG1 == null || globalInSMG2 == null) {
        // This weird situation happens with function static variables, which are created
        // as globals when a declaration is met. So if one path goes through function and other
        // does not, then one SMG will have that global and the other one won't.
        // TODO: We could actually just add that object, as that should not influence the result of
        // the join. For now, we will treat this situation as unjoinable.
        return;
      }
      SMGRegion finalObject = smg.addGlobalVariable(CType.createTypeWithLength(globalInSMG1.getSize()),
                                                    globalInSMG1.getLabel());
      mapping1.map(globalInSMG1, finalObject);
      mapping2.map(globalInSMG2, finalObject);
    }

    Iterator<CLangStackFrame> smg1stackIterator = stackInSmg1.descendingIterator();
    Iterator<CLangStackFrame> smg2stackIterator = stackInSmg2.descendingIterator();

    while (smg1stackIterator.hasNext() && smg2stackIterator.hasNext()) {
      CLangStackFrame frameInSMG1 = smg1stackIterator.next();
      CLangStackFrame frameInSMG2 = smg2stackIterator.next();

      smg.addStackFrame(frameInSMG1.getFunctionDeclaration());

      Set<String> localVars = new HashSet<>();
      localVars.addAll(frameInSMG1.getVariables().keySet());
      localVars.addAll(frameInSMG2.getVariables().keySet());

      for (String localVar : localVars) {
        if ((!frameInSMG1.containsVariable(localVar)) || (!frameInSMG2.containsVariable(localVar))) {
          return;
        }
        SMGRegion localInSMG1 = frameInSMG1.getVariable(localVar);
        SMGRegion localInSMG2 = frameInSMG2.getVariable(localVar);
        SMGRegion finalObject = smg.addLocalVariable(CType.createTypeWithLength(localInSMG1.getSize()),
                                                     localInSMG1.getLabel());

        mapping1.map(localInSMG1, finalObject);
        mapping2.map(localInSMG2, finalObject);
      }
    }

    for (Entry<String, SMGRegion> entry : globalsInSmg1.entrySet()) {
      SMGObject globalInSMG1 = entry.getValue();
      SMGObject globalInSMG2 = globalsInSmg2.get(entry.getKey());
      SMGObject destinationGlobal = mapping1.get(globalInSMG1);
      SMGJoinSubSMGs jss = new SMGJoinSubSMGs(status, opSMG1, opSMG2, smg, mapping1, mapping2, globalInSMG1,
                                              globalInSMG2, destinationGlobal);
      if (!jss.isDefined()) {
        return;
      }
      status = jss.getStatus();
    }

    smg1stackIterator = stackInSmg1.iterator();
    smg2stackIterator = stackInSmg2.iterator();

    while (smg1stackIterator.hasNext() && smg2stackIterator.hasNext()) {
      CLangStackFrame frameInSMG1 = smg1stackIterator.next();
      CLangStackFrame frameInSMG2 = smg2stackIterator.next();

      for (String localVar : frameInSMG1.getVariables().keySet()) {
        SMGObject localInSMG1 = frameInSMG1.getVariable(localVar);
        SMGObject localInSMG2 = frameInSMG2.getVariable(localVar);
        SMGObject destinationLocal = mapping1.get(localInSMG1);
        SMGJoinSubSMGs jss = new SMGJoinSubSMGs(status, opSMG1, opSMG2, smg, mapping1, mapping2, localInSMG1,
                                                localInSMG2, destinationLocal);
        if (!jss.isDefined()) {
          return;
        }
        status = jss.getStatus();
      }
    }



    defined = true;
  }

  public boolean isDefined() {
    return defined;
  }

  public SMGJoinStatus getStatus() {
    return status;
  }

  public ReadableSMG getJointSMG() {
    return smg;
  }
}

class SMGNodeMapping {
  private final Map<SMGObject, SMGObject> objectMap = new HashMap<>();
  private final Map<Integer, Integer> valueMap = new HashMap<>();

  @Override
  @SuppressWarnings("checkstyle:avoidinlineconditionals")
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((objectMap == null) ? 0 : objectMap.hashCode());
    result = prime * result + ((valueMap == null) ? 0 : valueMap.hashCode());
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
    SMGNodeMapping other = (SMGNodeMapping) obj;
    if (objectMap == null) {
      if (other.objectMap != null) {
        return false;
      }
    } else if (!objectMap.equals(other.objectMap)) {
      return false;
    }
    if (valueMap == null) {
      if (other.valueMap != null) {
        return false;
      }
    } else if (!valueMap.equals(other.valueMap)) {
      return false;
    }
    return true;
  }

  public SMGNodeMapping() { }

  public SMGNodeMapping(final SMGNodeMapping origin) {
    objectMap.putAll(origin.objectMap);
    valueMap.putAll(origin.valueMap);
  }

  public Integer get(final Integer i) {
    return valueMap.get(i);
  }

  public SMGObject get(final SMGObject o) {
    return objectMap.get(o);
  }

  public void map(final SMGObject key, final SMGObject value) {
    objectMap.put(key, value);
  }

  public void map(final Integer key, final Integer value) {
    valueMap.put(key, value);
  }

  public boolean containsKey(final Integer key) {
    return valueMap.containsKey(key);
  }

  public boolean containsKey(final SMGObject key) {
    return objectMap.containsKey(key);
  }

  public boolean containsValue(final SMGObject value) {
    return objectMap.containsValue(value);
  }
}
