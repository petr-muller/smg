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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cz.afri.smg.graphs.SMGEdgePointsTo;
import cz.afri.smg.graphs.SMGFactory;
import cz.afri.smg.graphs.SMGValueFactory;
import cz.afri.smg.graphs.WritableSMG;
import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.objects.SMGRegion;


public class SMGJoinTargetObjectsTest {
  private WritableSMG smg1;
  private WritableSMG smg2;
  private WritableSMG destSMG;

  private SMGNodeMapping mapping1;
  private SMGNodeMapping mapping2;

  private final SMGObject obj1 = new SMGRegion(8, "ze label");
  private final Integer value1 = SMGValueFactory.getNewValue();
  private final SMGEdgePointsTo pt1 = new SMGEdgePointsTo(value1, obj1, 0);

  private final SMGObject obj2 = new SMGRegion(8, "ze label");
  private final Integer value2 = SMGValueFactory.getNewValue();
  private final SMGEdgePointsTo pt2 = new SMGEdgePointsTo(value2, obj2, 0);

  private final SMGObject destObj = new SMGRegion(8, "destination");

  @Before
	public final void setUp() {
    smg1 = SMGFactory.createWritableSMG();
    smg2 = SMGFactory.createWritableSMG();
    destSMG = SMGFactory.createWritableSMG();

    mapping1 = new SMGNodeMapping();
    mapping2 = new SMGNodeMapping();
  }

  @Test
	public final void matchingObjectsWithoutMappingTest() {
    smg1.addHeapObject(obj1);
    smg1.addValue(value1);
    smg1.addPointsToEdge(pt1);

    smg2.addHeapObject(obj2);
    smg2.addValue(value2);
    smg2.addPointsToEdge(pt2);

    SMGJoinTargetObjects jto = new SMGJoinTargetObjects(SMGJoinStatus.EQUAL, smg1, smg2, destSMG, mapping1, mapping2,
    		                                                value1, value2);
    Assert.assertSame(jto.getMapping1().get(obj1), jto.getMapping2().get(obj2));
    Assert.assertNotSame(jto.getMapping1().get(obj1), obj1);
    Assert.assertTrue(((SMGRegion) jto.getMapping1().get(obj1)).propertiesEqual((SMGRegion) obj1));
  }

  @Test(expected = UnsupportedOperationException.class)
	public final void matchingObjectsWithMappingTest() {
    smg1.addHeapObject(obj1);
    smg1.addValue(value1);
    smg1.addPointsToEdge(pt1);

    smg2.addHeapObject(obj2);
    smg2.addValue(value2);
    smg2.addPointsToEdge(pt2);

    destSMG.addHeapObject(destObj);
    mapping1.map(obj1, destObj);

    SMGJoinMatchObjects mo = new SMGJoinMatchObjects(SMGJoinStatus.EQUAL, smg1, smg2, mapping1, mapping2, obj1, obj2);
    Assert.assertTrue(mo.isDefined());

    SMGJoinTargetObjects jto = new SMGJoinTargetObjects(mo.getStatus(), smg1, smg2, destSMG, mapping1, mapping2, value1,
    																								    value2);
    jto.getStatus(); // Avoid dead store warning
  }

  @Test
	public final void nonMatchingObjectsTest() {
    smg1.addHeapObject(obj1);
    smg1.addValue(value1);
    smg1.addPointsToEdge(pt1);

    SMGJoinMatchObjects mo = new SMGJoinMatchObjects(SMGJoinStatus.EQUAL, smg1, smg2, mapping1, mapping2, obj1,
    		                                             smg2.getNullObject());
    Assert.assertFalse(mo.isDefined());
    SMGJoinTargetObjects jto = new SMGJoinTargetObjects(SMGJoinStatus.EQUAL, smg1, smg2, destSMG, mapping1, mapping2,
    		                                                value1, smg2.getNullValue());
    Assert.assertFalse(jto.isDefined());
    Assert.assertTrue(jto.isRecoverable());
  }

  @Test
	public final void joinTargetObjectsDifferentOffsets() {
    SMGEdgePointsTo pt1null = new SMGEdgePointsTo(value1, smg1.getNullObject(), 2);
    SMGEdgePointsTo pt2null = new SMGEdgePointsTo(value2, smg2.getNullObject(), 1);

    smg1.addHeapObject(obj1);
    smg1.addValue(value1);
    smg1.addPointsToEdge(pt1null);

    smg2.addHeapObject(obj2);
    smg2.addValue(value2);
    smg2.addPointsToEdge(pt2null);

    SMGJoinTargetObjects jto = new SMGJoinTargetObjects(SMGJoinStatus.EQUAL, smg1, smg2, null, null, null, value1,
    		                                                value2);

    Assert.assertFalse(jto.isDefined());
    Assert.assertTrue(jto.isRecoverable());
  }

  @Test
	public final void joinTargetObjectsAlreadyJoinedNull() {
    SMGEdgePointsTo pt1null = new SMGEdgePointsTo(value1, smg1.getNullObject(), 0);
    SMGEdgePointsTo pt2null = new SMGEdgePointsTo(value2, smg2.getNullObject(), 0);

    smg1.addValue(value1);
    smg2.addValue(value2);

    smg1.addPointsToEdge(pt1null);
    smg2.addPointsToEdge(pt2null);

    SMGJoinMapTargetAddress mta = new SMGJoinMapTargetAddress(SMGFactory.createWritableCopy(smg1),
    		                                                      SMGFactory.createWritableCopy(smg2),
    		                                                      SMGFactory.createWritableCopy(destSMG),
                                                              new SMGNodeMapping(mapping1),
                                                              new SMGNodeMapping(mapping2),
                                                              value1, value2);
    SMGJoinTargetObjects jto = new SMGJoinTargetObjects(SMGJoinStatus.EQUAL, smg1, smg2, destSMG, mapping1, mapping2,
    		                                                value1, value2);
    Assert.assertTrue(jto.isDefined());
    Assert.assertEquals(SMGJoinStatus.EQUAL, jto.getStatus());
    Assert.assertSame(smg1, jto.getInputSMG1());
    Assert.assertSame(smg2, jto.getInputSMG2());
    Assert.assertTrue(mta.getSMG().isIdenticalTo(jto.getDestinationSMG()));
    Assert.assertEquals(mta.getMapping1(), jto.getMapping1());
    Assert.assertEquals(mta.getMapping2(), jto.getMapping2());
    Assert.assertEquals(mta.getValue(), jto.getValue());
  }

  @Test
	public final void joinTargetObjectsAlreadyJoinedNonNull() {
    smg1.addValue(value1);
    smg2.addValue(value2);

    smg1.addHeapObject(obj1);
    smg2.addHeapObject(obj2);
    destSMG.addHeapObject(destObj);

    smg1.addPointsToEdge(pt1);
    smg2.addPointsToEdge(pt2);

    mapping1.map(obj1, destObj);
    mapping2.map(obj2, destObj);

    // See TODO below
    // SMGMapTargetAddress mta = new SMGMapTargetAddress(new SMG(smg1), new SMG(smg2), new SMG(destSMG),
    //                                                  new SMGNodeMapping(mapping1), new SMGNodeMapping(mapping2),
    //                                                  value1, value2);
    SMGJoinTargetObjects jto = new SMGJoinTargetObjects(SMGJoinStatus.EQUAL, smg1, smg2, destSMG, mapping1, mapping2,
    		                                                value1, value2);
    Assert.assertTrue(jto.isDefined());
    Assert.assertEquals(SMGJoinStatus.EQUAL, jto.getStatus());
    Assert.assertSame(smg1, jto.getInputSMG1());
    Assert.assertSame(smg2, jto.getInputSMG2());
    // TODO: Not equal, but isomorphic (newly created values differ in mta and jto)
    //       But we currently do not have isomorphism
    //Assert.assertEquals(mta.getSMG(), jto.getDestinationSMG());

    Assert.assertTrue(jto.getMapping1().containsKey(value1));
    Assert.assertEquals(jto.getMapping1().get(value1), jto.getValue());

    Assert.assertTrue(jto.getMapping2().containsKey(value2));
    Assert.assertEquals(jto.getMapping2().get(value2), jto.getValue());
  }
}
