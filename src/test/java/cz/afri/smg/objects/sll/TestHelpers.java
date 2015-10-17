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

import cz.afri.smg.graphs.SMGEdgeHasValue;
import cz.afri.smg.graphs.SMGEdgePointsTo;
import cz.afri.smg.graphs.SMGValueFactory;
import cz.afri.smg.graphs.WritableSMG;
import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.objects.SMGRegion;
import cz.afri.smg.types.CPointerType;
import cz.afri.smg.types.CType;

public final class TestHelpers {
  private static final int SIZE8 = 8;
  private static final CType TYPE8 = CType.createTypeWithLength(SIZE8);

	public static Integer createList(final WritableSMG pSmg, final int pLength, final int pSize, final int pOffset,
			                             final String pPrefix) {
    Integer value = null;
    for (int i = 0; i < pLength; i++) {
      SMGObject node = new SMGRegion(pSize, pPrefix + "list_node" + i);
      SMGEdgeHasValue hv;
      if (value == null) {
        hv = new SMGEdgeHasValue(pSize, 0, node, 0);
      } else {
        hv = new SMGEdgeHasValue(CPointerType.getVoidPointer(), pOffset, node, value);
      }
      value = SMGValueFactory.getNewValue();
      SMGEdgePointsTo pt = new SMGEdgePointsTo(value, node, 0);
      pSmg.addHeapObject(node);
      pSmg.addValue(value);
      pSmg.addHasValueEdge(hv);
      pSmg.addPointsToEdge(pt);
    }
    return value;
  }

  public static SMGEdgeHasValue createGlobalList(final WritableSMG pSmg, final int pLength, final int pSize,
  		                                           final int pOffset, final String pVariable) {
    Integer value = TestHelpers.createList(pSmg, pLength, pSize, pOffset, pVariable);
    SMGRegion globalVar = pSmg.addGlobalVariable(TYPE8, pVariable);
    SMGEdgeHasValue hv = new SMGEdgeHasValue(CPointerType.getVoidPointer(), 0, globalVar, value);
    pSmg.addHasValueEdge(hv);

    return hv;
  }

  static SMGEdgeHasValue connect(final WritableSMG pSmg, final SMGObject pFrom, final SMGObject pTo,
  		                           final int pOffset) {
    int value = SMGValueFactory.getNewValue();
    pSmg.addValue(value);
    SMGEdgeHasValue hv = new SMGEdgeHasValue(CPointerType.getVoidPointer(), pOffset, pFrom, value);
    pSmg.addHasValueEdge(hv);
    pSmg.addPointsToEdge(new SMGEdgePointsTo(value, pTo, 0));

    return hv;
  }

  public static Integer createSll(final WritableSMG pSmg, final int pLength, final int pSize, final int pOffset,
                               final String pLabel) {
    SMGRegion prototype = new SMGRegion(pSize, pLabel);
    SMGSingleLinkedList sll = new SMGSingleLinkedList(prototype, pOffset, pLength);
    Integer value = SMGValueFactory.getNewValue();
    SMGEdgePointsTo pt = new SMGEdgePointsTo(value, sll, 0);
    pSmg.addHeapObject(sll);
    pSmg.addValue(value);
    pSmg.addPointsToEdge(pt);
    return value;
  }

  public static SMGEdgeHasValue createGlobalSll(final WritableSMG pSmg, final int pLength, final int pSize,
                                                final int pOffset, final String pLabel) {
    Integer value = TestHelpers.createSll(pSmg, pLength, pSize, pOffset, pLabel);
    SMGRegion globalVar = pSmg.addGlobalVariable(TYPE8, pLabel);
    SMGEdgeHasValue hv = new SMGEdgeHasValue(CPointerType.getVoidPointer(), 0, globalVar, value);
    pSmg.addHasValueEdge(hv);

    return hv;
  }


  private TestHelpers() {
  }
}
