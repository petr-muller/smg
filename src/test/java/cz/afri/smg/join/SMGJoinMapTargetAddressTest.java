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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cz.afri.smg.graphs.ReadableSMG;
import cz.afri.smg.graphs.SMGEdgePointsTo;
import cz.afri.smg.graphs.SMGFactory;
import cz.afri.smg.graphs.SMGValueFactory;
import cz.afri.smg.graphs.WritableSMG;
import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.objects.SMGRegion;


public class SMGJoinMapTargetAddressTest {

  private WritableSMG smg1;
  private WritableSMG destSMG;
  private SMGNodeMapping mapping1;
  private SMGNodeMapping mapping2;

  private final SMGRegion obj1 = new SMGRegion(8, "ze label");
  private final Integer value1 = SMGValueFactory.getNewValue();
  private final SMGEdgePointsTo edge1 = new SMGEdgePointsTo(value1, obj1, 0);

  private final Integer value2 = SMGValueFactory.getNewValue();

  private final SMGObject destObj = new SMGRegion(8, "destination");
  private final Integer destValue = SMGValueFactory.getNewValue();

  @Before
	public final void setUp() {
    smg1 = SMGFactory.createWritableSMG();
    destSMG = SMGFactory.createWritableSMG();
    mapping1 = new SMGNodeMapping();
    mapping2 = new SMGNodeMapping();
  }

  @Test
	public final void mapTargetAddressExistingNull() {
    ReadableSMG origDestSMG = SMGFactory.createWritableCopy(destSMG);
    SMGNodeMapping origMapping1 = new SMGNodeMapping(mapping1);

    SMGJoinMapTargetAddress mta = new SMGJoinMapTargetAddress(smg1, null, destSMG, mapping1, null, smg1.getNullValue(),
    		                                                      null);
    Assert.assertTrue(origDestSMG.isIdenticalTo(mta.getSMG()));
    Assert.assertEquals(origMapping1, mta.getMapping1());
    Assert.assertNull(mta.getMapping2());
    Assert.assertSame(destSMG.getNullValue(), mta.getValue());
  }

  @Test
	public final void mapTargetAddressExisting() {
    SMGEdgePointsTo destEdge = new SMGEdgePointsTo(destValue, destObj, 0);

    smg1.addValue(value1);
    smg1.addHeapObject(obj1);
    smg1.addPointsToEdge(edge1);

    destSMG.addValue(destValue);
    destSMG.addHeapObject(destObj);
    destSMG.addPointsToEdge(destEdge);

    mapping1.map(obj1, destObj);

    SMGNodeMapping origMapping1 = new SMGNodeMapping(mapping1);
    ReadableSMG origDestSMG = SMGFactory.createWritableCopy(destSMG);

    SMGJoinMapTargetAddress mta = new SMGJoinMapTargetAddress(smg1, null, destSMG, mapping1, null, value1, null);
    Assert.assertTrue(origDestSMG.isIdenticalTo(mta.getSMG()));
    Assert.assertEquals(origMapping1, mta.getMapping1());
    Assert.assertNull(mta.getMapping2());
    Assert.assertSame(destValue, mta.getValue());
  }

  @Test
	public final void mapTargetAddressNew() {
    smg1.addValue(value1);
    smg1.addHeapObject(obj1);
    smg1.addPointsToEdge(edge1);

    destSMG.addHeapObject(destObj);

    mapping1.map(obj1, destObj);

    SMGNodeMapping origMapping1 = new SMGNodeMapping(mapping1);
    SMGNodeMapping origMapping2 = new SMGNodeMapping(mapping2);
    ReadableSMG origDestSMG = SMGFactory.createWritableCopy(destSMG);

    SMGJoinMapTargetAddress mta = new SMGJoinMapTargetAddress(smg1, null, destSMG, mapping1, mapping2, value1, value2);
    Assert.assertNotEquals(origDestSMG, mta.getSMG());
    Assert.assertNotEquals(origMapping1, mta.getMapping1());
    Assert.assertNotEquals(origMapping2, mta.getMapping2());

    Assert.assertFalse(origDestSMG.getValues().contains(mta.getValue()));

    SMGEdgePointsTo newEdge = destSMG.getPointer(mta.getValue());
    Assert.assertSame(destObj, newEdge.getObject());
    Assert.assertEquals(0, newEdge.getOffset());

    Assert.assertSame(mta.getValue(), mta.getMapping1().get(value1));
    Assert.assertSame(mta.getValue(), mta.getMapping2().get(value2));
  }
}
