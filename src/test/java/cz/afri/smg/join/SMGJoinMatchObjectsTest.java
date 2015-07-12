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

import cz.afri.smg.graphs.SMGEdgeHasValue;
import cz.afri.smg.graphs.SMGFactory;
import cz.afri.smg.graphs.SMGValueFactory;
import cz.afri.smg.graphs.WritableSMG;
import cz.afri.smg.objects.DummyAbstraction;
import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.objects.SMGRegion;
import cz.afri.smg.objects.sll.SMGSingleLinkedList;
import cz.afri.smg.types.CType;

public class SMGJoinMatchObjectsTest {

  private static final CType MOCKTYPE2 = CType.createTypeWithLength(2);

  private static final int SIZE8 = 8;
	private static final int SIZE16 = 16;

  private WritableSMG smg1;
  private WritableSMG smg2;

  private final SMGObject srcObj1 = new SMGRegion(8, "Source object 1");
  private final SMGObject destObj1 = new SMGRegion(8, "Destination object 1");
  private final SMGRegion srcObj2 = new SMGRegion(8, "Source object 2");
  private final SMGObject destObj2 = new SMGRegion(8, "Destination object 2");

  private SMGNodeMapping mapping1;
  private SMGNodeMapping mapping2;

  @Before
	public final void setUp() {
    smg1 = SMGFactory.createWritableSMG();
    smg2 = SMGFactory.createWritableSMG();

    mapping1 = new SMGNodeMapping();
    mapping2 = new SMGNodeMapping();
  }

  @Test
	public final void nullObjectTest() {
    SMGJoinMatchObjects mo = new SMGJoinMatchObjects(SMGJoinStatus.EQUAL, smg1, smg2, null, null, smg1.getNullObject(),
    		                                             smg2.getNullObject());
    Assert.assertFalse(mo.isDefined());

    smg1.addHeapObject(srcObj1);
    mo = new SMGJoinMatchObjects(SMGJoinStatus.EQUAL, smg1, smg2, null, null, srcObj1, smg2.getNullObject());
    Assert.assertFalse(mo.isDefined());
  }

  @Test(expected = IllegalArgumentException.class)
	public final void nonMemberObjectsTestObj1() {
    smg2.addHeapObject(srcObj2);

    SMGJoinMatchObjects mo = new SMGJoinMatchObjects(SMGJoinStatus.EQUAL, smg1, smg2, null, null, srcObj1, srcObj2);
    mo.getStatus(); // Avoid dead store warning
  }

  @Test(expected = IllegalArgumentException.class)
	public final void nonMemberObjectsTestObj2() {
    smg1.addHeapObject(srcObj1);

    SMGJoinMatchObjects mo = new SMGJoinMatchObjects(SMGJoinStatus.EQUAL, smg1, smg2, null, null, srcObj1, srcObj2);
    mo.getStatus(); // Avoid dead store warning
  }

  @Test
	public final void inconsistentMappingTest() {
    mapping1.map(srcObj1, destObj1);
    smg1.addHeapObject(srcObj1);

    smg2.addHeapObject(srcObj2);
    mapping2.map(srcObj2, destObj1);

    SMGJoinMatchObjects mo = new SMGJoinMatchObjects(SMGJoinStatus.EQUAL, smg1, smg2, mapping1, mapping2, srcObj1,
    		                                             srcObj2);
    Assert.assertFalse(mo.isDefined());
  }

  @Test
	public final void inconsistentMappingViceVersaTest() {
    mapping2.map(srcObj2, destObj2);
    smg2.addHeapObject(srcObj2);

    smg1.addHeapObject(srcObj1);
    mapping1.map(srcObj1, destObj2);

    SMGJoinMatchObjects mo = new SMGJoinMatchObjects(SMGJoinStatus.EQUAL, smg1, smg2, mapping1, mapping2, srcObj1,
    		                                             srcObj2);
    Assert.assertFalse(mo.isDefined());
  }

  @Test
	public final void inconsistentObjectsTest() {
    SMGObject diffSizeObject = new SMGRegion(SIZE16, "Object with different size");
    smg1.addHeapObject(srcObj1);
    smg2.addHeapObject(diffSizeObject);
    SMGJoinMatchObjects mo = new SMGJoinMatchObjects(SMGJoinStatus.EQUAL,  smg1, smg2, mapping1, mapping2, srcObj1,
    		                                             diffSizeObject);
    Assert.assertFalse(mo.isDefined());

    smg2.addHeapObject(srcObj2);
    smg2.setValidity(srcObj2, false);
    mo = new SMGJoinMatchObjects(SMGJoinStatus.EQUAL, smg1, smg2, mapping1, mapping2, srcObj1, srcObj2);
    Assert.assertFalse(mo.isDefined());
  }

  @Test
	public final void nonMatchingMappingTest() {
    smg1.addHeapObject(srcObj1);
    smg1.addHeapObject(destObj1);
    mapping1.map(srcObj1, destObj1);

    smg2.addHeapObject(srcObj2);
    smg2.addHeapObject(destObj2);
    mapping2.map(srcObj2, destObj2);

    SMGJoinMatchObjects mo = new SMGJoinMatchObjects(SMGJoinStatus.EQUAL, smg1, smg2, mapping1, mapping2, srcObj1,
    		                                             srcObj2);
    Assert.assertFalse(mo.isDefined());
  }

  @Test
	public final void fieldInconsistencyTest() {
    smg1.addHeapObject(srcObj1);
    smg2.addHeapObject(srcObj2);

    final int offset4 = 4;

    SMGEdgeHasValue hv1 = new SMGEdgeHasValue(MOCKTYPE2, 0, srcObj1, SMGValueFactory.getNewValue());
    SMGEdgeHasValue hv2 = new SMGEdgeHasValue(MOCKTYPE2, 2, srcObj2, SMGValueFactory.getNewValue());
		SMGEdgeHasValue hvMatching1 = new SMGEdgeHasValue(MOCKTYPE2, offset4, srcObj1, SMGValueFactory.getNewValue());
    SMGEdgeHasValue hvMatching2 = new SMGEdgeHasValue(MOCKTYPE2, offset4, srcObj2, SMGValueFactory.getNewValue());

    smg1.addHasValueEdge(hv1);
    smg1.addValue(hv1.getValue());

    smg2.addHasValueEdge(hv2);
    smg2.addValue(hv2.getValue());

    smg1.addHasValueEdge(hvMatching1);
    smg1.addValue(hvMatching1.getValue());

    smg2.addHasValueEdge(hvMatching2);
    smg2.addValue(hvMatching2.getValue());

    SMGJoinMatchObjects mo = new SMGJoinMatchObjects(SMGJoinStatus.EQUAL, smg1, smg2, mapping1, mapping2, srcObj1,
    		                                             srcObj2);
    Assert.assertTrue(mo.isDefined());

    mapping1.map(hvMatching1.getValue(), SMGValueFactory.getNewValue());
    mo = new SMGJoinMatchObjects(SMGJoinStatus.EQUAL, smg1, smg2, mapping1, mapping2, srcObj1, srcObj2);
    Assert.assertTrue(mo.isDefined());

    mapping2.map(hvMatching2.getValue(), mapping1.get(hvMatching1.getValue()));
    mo = new SMGJoinMatchObjects(SMGJoinStatus.EQUAL, smg1, smg2, mapping1, mapping2, srcObj1, srcObj2);
    Assert.assertTrue(mo.isDefined());

    mapping2.map(hvMatching2.getValue(), SMGValueFactory.getNewValue());
    mo = new SMGJoinMatchObjects(SMGJoinStatus.EQUAL, smg1, smg2, mapping1, mapping2, srcObj1, srcObj2);
    Assert.assertFalse(mo.isDefined());
  }

  @Test
	public final void sameAbstractionMatchTest() {
    SMGRegion prototype = new SMGRegion(SIZE16, "prototype");
    final int offset8 = 8;
		final int length7 = 7;
		SMGSingleLinkedList sll1 = new SMGSingleLinkedList(prototype, offset8, length7);
    SMGSingleLinkedList sll2 = new SMGSingleLinkedList(prototype, 0, length7);

    smg1.addHeapObject(sll1);
    smg2.addHeapObject(sll2);

    SMGJoinMatchObjects mo = new SMGJoinMatchObjects(SMGJoinStatus.EQUAL, smg1, smg2, mapping1, mapping2, sll1, sll2);
    Assert.assertFalse(mo.isDefined());
  }

  @Test
	public final void differentAbstractionMatch() {
    SMGRegion prototype = new SMGRegion(SIZE16, "prototype");
    final int length3 = 3;
		SMGSingleLinkedList sll = new SMGSingleLinkedList(prototype, SIZE8, length3);
    DummyAbstraction dummy = new DummyAbstraction(prototype);

    smg1.addHeapObject(sll);
    smg2.addHeapObject(dummy);

    SMGJoinMatchObjects mo = new SMGJoinMatchObjects(SMGJoinStatus.EQUAL, smg1, smg2, mapping1, mapping2, sll, dummy);
    Assert.assertFalse(mo.isDefined());
  }

  @Test
	public final void twoAbstractionsTest() {
    SMGRegion prototype = new SMGRegion(SIZE16, "prototype");

    final int offset8 = 8;
		final int length2 = 2;
		final int length4 = 4;
		final int length8 = 8;

		SMGSingleLinkedList sll1 = new SMGSingleLinkedList(prototype, offset8, length2);
    SMGSingleLinkedList sll2 = new SMGSingleLinkedList(prototype, offset8, length4);
    smg1.addHeapObject(sll1);
    smg2.addHeapObject(sll2);

    SMGJoinMatchObjects mo = new SMGJoinMatchObjects(SMGJoinStatus.EQUAL, smg1, smg2, mapping1, mapping2, sll1, sll2);
    Assert.assertTrue(mo.isDefined());
    Assert.assertEquals(SMGJoinStatus.LEFT_ENTAIL, mo.getStatus());

    sll1 = new SMGSingleLinkedList(prototype, offset8, length4);
    smg1.addHeapObject(sll1);
    mo = new SMGJoinMatchObjects(SMGJoinStatus.EQUAL, smg1, smg2, mapping1, mapping2, sll1, sll2);
    Assert.assertTrue(mo.isDefined());
    Assert.assertEquals(SMGJoinStatus.EQUAL, mo.getStatus());

    sll1 = new SMGSingleLinkedList(prototype, offset8, length8);
    smg1.addHeapObject(sll1);
    mo = new SMGJoinMatchObjects(SMGJoinStatus.EQUAL, smg1, smg2, mapping1, mapping2, sll1, sll2);
    Assert.assertTrue(mo.isDefined());
    Assert.assertEquals(SMGJoinStatus.RIGHT_ENTAIL, mo.getStatus());
  }

  @Test
	public final void oneAbstractionTest() {
    SMGRegion prototype = new SMGRegion(SIZE16, "prototype");
    final int offset8 = 8;
		final int length8 = 8;
		SMGSingleLinkedList sll = new SMGSingleLinkedList(prototype, offset8, length8);

    smg1.addHeapObject(sll);
    smg2.addHeapObject(sll);
    smg1.addHeapObject(prototype);
    smg2.addHeapObject(prototype);

    SMGJoinMatchObjects mo = new SMGJoinMatchObjects(SMGJoinStatus.EQUAL, smg1, smg2, mapping1, mapping2, sll,
    		                                             prototype);
    Assert.assertTrue(mo.isDefined());
    Assert.assertEquals(SMGJoinStatus.LEFT_ENTAIL, mo.getStatus());

    mo = new SMGJoinMatchObjects(SMGJoinStatus.EQUAL, smg1, smg2, mapping1, mapping2, prototype, sll);
    Assert.assertTrue(mo.isDefined());
    Assert.assertEquals(SMGJoinStatus.RIGHT_ENTAIL, mo.getStatus());
  }

  @Test
	public final void noAbstractionTest() {
    SMGRegion object = new SMGRegion(SIZE16, "prototype");
    smg1.addHeapObject(object);
    smg2.addHeapObject(object);
    SMGJoinMatchObjects mo = new SMGJoinMatchObjects(SMGJoinStatus.EQUAL, smg1, smg2, mapping1, mapping2, object,
    		                                             object);
    Assert.assertTrue(mo.isDefined());
    Assert.assertEquals(SMGJoinStatus.EQUAL, mo.getStatus());
  }
}
