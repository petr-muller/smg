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
package cz.afri.smg.objects.tree;

import cz.afri.smg.graphs.SMGEdgeHasValue;
import cz.afri.smg.graphs.SMGEdgePointsTo;
import cz.afri.smg.graphs.SMGValueFactory;
import cz.afri.smg.graphs.WritableSMG;
import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.objects.SMGRegion;
import cz.afri.smg.types.CPointerType;


final class TestHelpers {
  static class Treeoid {
    private final SMGObject root;
    private final SMGObject left;
    private final SMGObject right;

    Treeoid(final SMGObject pRoot, final SMGObject pLeft, final SMGObject pRight) {
      root = pRoot;
      left = pLeft;
      right = pRight;
    }

    SMGObject getRoot() {
      return root;
    }

    SMGObject getLeft() {
      return left;
    }

    SMGObject getRight() {
      return right;
    }
  }

  private static int counter = 0;

  private TestHelpers() { }

  static SMGRegion getLabelledCopy(final SMGRegion pPrototype) {
    return new SMGRegion(pPrototype.getSize(), pPrototype.getLabel() + "_" + String.valueOf(counter++));
  }

  static Treeoid createConcreteTreeoid(final WritableSMG pSmg, final SMGRegion pPrototype, final int pLeftOffset,
  																		 final int pRightOffset) {
    SMGRegion root = getLabelledCopy(pPrototype);
    SMGRegion left = getLabelledCopy(pPrototype);
    SMGRegion right = getLabelledCopy(pPrototype);

    int lVal = SMGValueFactory.getNewValue();
    int rVal = SMGValueFactory.getNewValue();

    SMGEdgePointsTo lPt = new SMGEdgePointsTo(lVal, left, 0);
    SMGEdgePointsTo rPt = new SMGEdgePointsTo(rVal, right, 0);
    SMGEdgeHasValue lHv = new SMGEdgeHasValue(CPointerType.getVoidPointer(), pLeftOffset, root, lVal);
    SMGEdgeHasValue rHv = new SMGEdgeHasValue(CPointerType.getVoidPointer(), pRightOffset, root, rVal);

    pSmg.addHeapObject(root);
    pSmg.addHeapObject(left);
    pSmg.addHeapObject(right);
    pSmg.addValue(lVal);
    pSmg.addValue(rVal);

    pSmg.addHasValueEdge(lHv);
    pSmg.addHasValueEdge(rHv);
    pSmg.addPointsToEdge(lPt);
    pSmg.addPointsToEdge(rPt);

    pSmg.addHasValueEdge(new SMGEdgeHasValue(CPointerType.getVoidPointer(), pLeftOffset, left, pSmg.getNullValue()));
    pSmg.addHasValueEdge(new SMGEdgeHasValue(CPointerType.getVoidPointer(), pRightOffset, left, pSmg.getNullValue()));
    pSmg.addHasValueEdge(new SMGEdgeHasValue(CPointerType.getVoidPointer(), pLeftOffset, right, pSmg.getNullValue()));
    pSmg.addHasValueEdge(new SMGEdgeHasValue(CPointerType.getVoidPointer(), pRightOffset, right, pSmg.getNullValue()));

    return new Treeoid(root, left, right);
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

  static SMGRegion createCompleteThreeLevelTree(final WritableSMG pSmg, final SMGRegion pPrototype, final int pLO,
  		                                          final int pRO) {
    SMGRegion root = new SMGRegion(pPrototype);
    pSmg.addHeapObject(root);
    Treeoid left = createConcreteTreeoid(pSmg, pPrototype, pLO, pRO);
    Treeoid right = createConcreteTreeoid(pSmg, pPrototype, pLO, pRO);
    connect(pSmg, root, left.getRoot(), pLO);
    connect(pSmg, root, right.getRoot(), pRO);

    return root;
  }

  static SMGEdgeHasValue createGlobalPointerToThreeLevelTree(final WritableSMG pSmg, final SMGRegion pPrototype,
  				                                                   final int pLO, final int pRO) {
    SMGRegion root = createCompleteThreeLevelTree(pSmg, pPrototype, pLO, pRO);
    SMGRegion local = pSmg.addGlobalVariable(CPointerType.getVoidPointer(), "pointer");
    return connect(pSmg, local, root, 0);
  }
}
