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
package cz.afri.smg;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;

import cz.afri.smg.abstraction.SMGAbstractionManager;
import cz.afri.smg.graphs.ReadableSMG;
import cz.afri.smg.graphs.SMGEdgeHasValue;
import cz.afri.smg.graphs.SMGEdgeHasValueFilter;
import cz.afri.smg.graphs.SMGEdgePointsTo;
import cz.afri.smg.graphs.SMGFactory;
import cz.afri.smg.graphs.SMGValueFactory;
import cz.afri.smg.graphs.WritableSMG;
import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.objects.SMGRegion;
import cz.afri.smg.types.CPointerType;
import cz.afri.smg.types.CType;


public class SMGAbstractionManagerTest {

	private static final int OFFSET8 = 8;
	private static final int SIZE8 = 8;
	private static final int SIZE16 = 16;
  private WritableSMG smg;

  @Before
  public final void setUp() {
    smg = SMGFactory.createWritableSMG();

    SMGRegion next = null;
    final int items = 20;
    for (int i = 0; i < items; i++) {
      SMGRegion node = new SMGRegion(SIZE16, "node " + i);
      SMGEdgeHasValue hv;
      smg.addHeapObject(node);
      if (next != null) {
        int address = SMGValueFactory.getNewValue();
        SMGEdgePointsTo pt = new SMGEdgePointsTo(address, next, 0);
        hv = new SMGEdgeHasValue(CPointerType.getVoidPointer(), OFFSET8, node, address);
        smg.addValue(address);
        smg.addPointsToEdge(pt);
      } else {
        hv = new SMGEdgeHasValue(SIZE16, 0, node, 0);
      }
      smg.addHasValueEdge(hv);
      next = node;
    }

    int address = SMGValueFactory.getNewValue();
    SMGRegion globalVar = smg.addGlobalVariable(CType.createTypeWithLength(SIZE8), "pointer");
    SMGEdgeHasValue hv = new SMGEdgeHasValue(CPointerType.getVoidPointer(), OFFSET8, globalVar, address);
    SMGEdgePointsTo pt = new SMGEdgePointsTo(address, next, 0);
    smg.addValue(address);
    smg.addPointsToEdge(pt);
    smg.addHasValueEdge(hv);
  }

  @Test
  public final void testExecute() {
    SMGAbstractionManager manager = new SMGAbstractionManager(smg);
    ReadableSMG afterAbstraction = manager.execute();

    SMGRegion globalVar = afterAbstraction.getObjectForVisibleVariable("pointer");
    Iterable<SMGEdgeHasValue> hvs = afterAbstraction.getHVEdges(SMGEdgeHasValueFilter.objectFilter(globalVar));
    Assert.assertEquals(1, Iterables.size(hvs));
    SMGEdgeHasValue hv = Iterables.getOnlyElement(hvs);
    SMGEdgePointsTo pt = afterAbstraction.getPointer(hv.getValue());
    SMGObject segment = pt.getObject();
    Assert.assertTrue(segment.isAbstract());
    Set<SMGObject> heap = afterAbstraction.getHeapObjects();
    Assert.assertEquals(2, heap.size());
  }
}
