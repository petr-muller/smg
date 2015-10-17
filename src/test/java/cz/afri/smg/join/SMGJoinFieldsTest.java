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

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;

import cz.afri.smg.graphs.ReadableSMG;
import cz.afri.smg.graphs.SMGEdgeHasValue;
import cz.afri.smg.graphs.SMGEdgeHasValueFilter;
import cz.afri.smg.graphs.SMGEdgePointsTo;
import cz.afri.smg.graphs.SMGFactory;
import cz.afri.smg.graphs.SMGValueFactory;
import cz.afri.smg.graphs.WritableSMG;
import cz.afri.smg.objects.SMGRegion;
import cz.afri.smg.types.CType;


public class SMGJoinFieldsTest {
	private static final int SIZE4 = 4;
	private static final int SIZE8 = 8;
  private static final int SIZE16 = 16;
  private static final int SIZE32 = 32;

	private static final CType MOCKTYPE4 = CType.createTypeWithLength(SIZE4);
  private static final CType MOCKTYPE8 = CType.createTypeWithLength(SIZE8);

  private WritableSMG smg1;
  private WritableSMG smg2;

  private final Integer value1 = SMGValueFactory.getNewValue();
  private final Integer value2 = SMGValueFactory.getNewValue();

  @Before
	public final void setUp() {
    smg1 = SMGFactory.createWritableSMG();
    smg2 = SMGFactory.createWritableSMG();
  }

  @Test
	public final void getHVSetWithoutNullValuesOnObjectTest() {
    SMGRegion obj1 = new SMGRegion(SIZE8, "1");
    SMGRegion obj2 = new SMGRegion(SIZE8, "1");

    final int offset4 = 4;

    SMGEdgeHasValue obj1hv1at0 = new SMGEdgeHasValue(MOCKTYPE4, 0, obj1, value1);
		SMGEdgeHasValue obj1hv0at4 = new SMGEdgeHasValue(MOCKTYPE4, offset4, obj1, smg1.getNullValue());
    SMGEdgeHasValue obj2hv2at0 = new SMGEdgeHasValue(MOCKTYPE4, 0, obj2, value2);
    SMGEdgeHasValue obj2hv0at4 = new SMGEdgeHasValue(MOCKTYPE4, offset4, obj2, smg1.getNullValue());

    smg1.addHeapObject(obj1);
    smg1.addHeapObject(obj2);
    smg1.addValue(value1);
    smg1.addValue(value2);
    smg1.addHasValueEdge(obj1hv0at4);
    smg1.addHasValueEdge(obj1hv1at0);
    smg1.addHasValueEdge(obj2hv0at4);
    smg1.addHasValueEdge(obj2hv2at0);

    Set<SMGEdgeHasValue> hvSet = SMGJoinFields.getHVSetWithoutNullValuesOnObject(smg1, obj1);
    Assert.assertTrue(hvSet.contains(obj1hv1at0));
    Assert.assertTrue(hvSet.contains(obj2hv2at0));
    Assert.assertTrue(hvSet.contains(obj2hv0at4));
    final int expectedHVSetSize = 3;
		Assert.assertEquals(expectedHVSetSize, hvSet.size());
  }

  @Test
	public final void getHVSetOfMissingNullValuesTest() {
    SMGRegion obj1 = new SMGRegion(SIZE8, "1");
    SMGRegion obj2 = new SMGRegion(SIZE8, "2");

    smg1.addHeapObject(obj1);
    smg2.addHeapObject(obj2);
    smg2.addValue(value2);

    SMGEdgeHasValue nullifyObj1 = new SMGEdgeHasValue(SIZE8, 0, obj1, smg1.getNullValue());
    SMGEdgeHasValue nonPointer = new SMGEdgeHasValue(MOCKTYPE4, 2, obj2, value2);

    smg1.addHasValueEdge(nullifyObj1);
    smg2.addHasValueEdge(nonPointer);

    Set<SMGEdgeHasValue> hvSet = SMGJoinFields.getHVSetOfMissingNullValues(smg1, smg2, obj1, obj2);
    Assert.assertEquals(0, hvSet.size());

    smg2.addPointsToEdge(new SMGEdgePointsTo(value2, obj2, 0));

    hvSet = SMGJoinFields.getHVSetOfMissingNullValues(smg1, smg2, obj1, obj2);
    Assert.assertEquals(1, hvSet.size());

    SMGEdgeHasValue newHv = Iterables.getOnlyElement(hvSet);
    Assert.assertEquals(smg1.getNullValue(), newHv.getValue());
    Assert.assertSame(obj1, newHv.getObject());
    final int expectedSize = 4;
		Assert.assertEquals(expectedSize, newHv.getSizeInBytes());
    Assert.assertEquals(2, newHv.getOffset());
    Assert.assertTrue(newHv.isCompatibleField(nonPointer));
  }

  @Test
	public final void getHVSetOfCommonNullValuesTest() {
    final int size22 = 22;
		SMGRegion obj1 = new SMGRegion(size22, "1");

    final int offset4 = 4;
		SMGEdgeHasValue smg1at4 = new SMGEdgeHasValue(MOCKTYPE4, offset4, obj1, smg1.getNullValue());

    final int offset8 = 8;
		SMGEdgeHasValue smg2at8 = new SMGEdgeHasValue(MOCKTYPE4, offset8, obj1, smg2.getNullValue());

    final int offset14 = 14;
		SMGEdgeHasValue smg1at14 = new SMGEdgeHasValue(MOCKTYPE4, offset14, obj1, smg1.getNullValue());

    final int offset12 = 12;
		SMGEdgeHasValue smg2at12 = new SMGEdgeHasValue(MOCKTYPE4, offset12, obj1, smg2.getNullValue());

    final int offset18 = 18;
		SMGEdgeHasValue smg1at18 = new SMGEdgeHasValue(MOCKTYPE4, offset18, obj1, smg1.getNullValue());
    SMGEdgeHasValue smg2at18 = new SMGEdgeHasValue(MOCKTYPE4, offset18, obj1, smg2.getNullValue());

    smg1.addHasValueEdge(smg1at18);
    smg1.addHasValueEdge(smg1at14);
    smg1.addHasValueEdge(smg1at4);
    smg2.addHasValueEdge(smg2at18);
    smg2.addHasValueEdge(smg2at12);
    smg2.addHasValueEdge(smg2at8);

    Set<SMGEdgeHasValue> hvSet = SMGJoinFields.getHVSetOfCommonNullValues(smg1, smg2, obj1, obj1);
    Assert.assertEquals(2, hvSet.size());
    for (SMGEdgeHasValue hv : hvSet) {
      Assert.assertEquals(hv.getValue(), smg1.getNullValue());
      Assert.assertSame(hv.getObject(), obj1);
      Assert.assertTrue(hv.getOffset() == offset14 || hv.getOffset() == offset18);
      if (hv.getOffset() == offset14) {
        Assert.assertTrue(hv.getSizeInBytes() == 2);
      } else {
        final int size4 = 4;
				Assert.assertTrue(hv.getSizeInBytes() == size4);
      }
    }
  }

  @Test
	public final void getCompatibleHVEdgeSetTest() {
		SMGRegion obj = new SMGRegion(SIZE32, "Object");
		SMGRegion differentObject = new SMGRegion(SIZE16, "Different object");

    smg1.addHeapObject(obj);
    smg2.addHeapObject(obj);
    smg1.addHeapObject(differentObject);

    smg2.addValue(value1);

    SMGEdgeHasValue hv0for4at0in1 = new SMGEdgeHasValue(MOCKTYPE4, 0, obj, smg1.getNullValue());
    SMGEdgeHasValue hv0for4at0in2 = new SMGEdgeHasValue(MOCKTYPE4, 0, obj, smg2.getNullValue());

    final int offset5 = 5;
    final int offset7 = 7;
		SMGEdgeHasValue hv0for4at5in1 = new SMGEdgeHasValue(MOCKTYPE4, offset5, obj, smg1.getNullValue());
    SMGEdgeHasValue hv0for4at7in2 = new SMGEdgeHasValue(MOCKTYPE4, offset7, obj, smg2.getNullValue());

    final int offset12 = 12;
    final int offset16 = 16;
    SMGEdgeHasValue hv0for4at12in1 = new SMGEdgeHasValue(MOCKTYPE4, offset12, obj, smg1.getNullValue());
    SMGEdgeHasValue hv0for4at16in2 = new SMGEdgeHasValue(MOCKTYPE4, offset16, obj, smg2.getNullValue());

    final int offset20 = 20;
    SMGEdgeHasValue hv0for4at20in1 = new SMGEdgeHasValue(MOCKTYPE4, offset20, obj, smg1.getNullValue());
    SMGEdgeHasValue hv666for4at20in2 = new SMGEdgeHasValue(MOCKTYPE4, offset20, obj, value1);

    final int offset28 = 28;
    SMGEdgeHasValue hv666for4at28in2 = new SMGEdgeHasValue(MOCKTYPE4, offset28, obj, value1);

    SMGEdgeHasValue diffObjectNullValue = new SMGEdgeHasValue(MOCKTYPE4, 0, differentObject, smg1.getNullValue());

    smg1.addHasValueEdge(hv0for4at0in1);
    smg1.addHasValueEdge(hv0for4at5in1);
    smg1.addHasValueEdge(hv0for4at12in1);
    smg1.addHasValueEdge(hv0for4at20in1);
    smg1.addHasValueEdge(diffObjectNullValue);

    smg2.addHasValueEdge(hv0for4at0in2);
    smg2.addHasValueEdge(hv0for4at7in2);
    smg2.addHasValueEdge(hv0for4at16in2);
    smg2.addHasValueEdge(hv666for4at20in2);
    smg2.addPointsToEdge(new SMGEdgePointsTo(value1, obj, offset20));
    smg2.addHasValueEdge(hv666for4at28in2);

    final int size4 = 4;
    Set<SMGEdgeHasValue> compSet1 = SMGJoinFields.getCompatibleHVEdgeSet(smg1, smg2, obj, obj);
    Assert.assertEquals(size4, compSet1.size());

    Set<SMGEdgeHasValue> compSet2 = SMGJoinFields.getCompatibleHVEdgeSet(smg2, smg1, obj, obj);
    Assert.assertEquals(size4, compSet2.size());
  }

  @Test
	public final void mergeNonNullHVEdgesTest() {
    Set<Integer> values = new HashSet<>();
    values.add(value1);
    values.add(value2);

    final int offset4 = 4;

    SMGRegion object = new SMGRegion(SIZE16, "Object");
    SMGEdgeHasValue smg14bFrom0ToV1 = new SMGEdgeHasValue(MOCKTYPE4, 0, object, value1);
    SMGEdgeHasValue smg14bFrom2ToV2 = new SMGEdgeHasValue(MOCKTYPE4, 2, object, value2);
		SMGEdgeHasValue smg14bFrom4ToNull = new SMGEdgeHasValue(MOCKTYPE4, offset4, object, smg1.getNullValue());

    smg1.addHeapObject(object);
    smg1.addValue(value1);
    smg1.addValue(value2);
    smg1.addHasValueEdge(smg14bFrom4ToNull);
    smg1.addHasValueEdge(smg14bFrom2ToV2);
    smg1.addHasValueEdge(smg14bFrom0ToV1);

    smg2.addHeapObject(object);

    Set<SMGEdgeHasValue> hvSet = SMGJoinFields.mergeNonNullHasValueEdges(smg1, smg2, object, object);
    Assert.assertEquals(2, hvSet.size());

    boolean seenZero = false;
    boolean seenTwo = false;

    SMGEdgeHasValueFilter filter = SMGEdgeHasValueFilter.objectFilter(object);
    for (SMGEdgeHasValue edge : filter.filterSet(hvSet)) {
      if (edge.getOffset() == 0) {
        seenZero = true;
      } else if (edge.getOffset() == 2) {
        seenTwo = true;
      }
      Assert.assertTrue(edge.getOffset() == 0 || edge.getOffset() == 2);
      Assert.assertTrue(MOCKTYPE4.equals(edge.getType()));
      Assert.assertFalse(values.contains(Integer.valueOf(edge.getValue())));
      values.add(Integer.valueOf(edge.getValue()));
    }
    Assert.assertTrue(seenZero);
    Assert.assertTrue(seenTwo);

    smg2.addValue(value1);
    smg2.addHasValueEdge(smg14bFrom0ToV1);
    hvSet = SMGJoinFields.mergeNonNullHasValueEdges(smg1, smg2, object, object);
    Assert.assertEquals(1, hvSet.size());
  }

  @Test
	public final void mergeNonNullAplliedTest() {
    SMGRegion obj1 = smg1.addGlobalVariable(MOCKTYPE8, "Object 1");
    SMGRegion obj2 = smg2.addGlobalVariable(MOCKTYPE8, "Object 2");

    smg1.addValue(value1);
    smg1.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, 0, obj1, value1));

    SMGJoinFields jf = new SMGJoinFields(SMGFactory.createWritableCopy(smg1), SMGFactory.createWritableCopy(smg2), obj1,
    		                                 obj2);
    ReadableSMG resultSMG = jf.getSMG2();

    Iterable<SMGEdgeHasValue> edges = resultSMG.getHVEdges(SMGEdgeHasValueFilter.objectFilter(obj2));
    Assert.assertTrue(edges.iterator().hasNext());

    jf = new SMGJoinFields(SMGFactory.createWritableCopy(smg2), SMGFactory.createWritableCopy(smg1), obj2, obj1);
    resultSMG = jf.getSMG1();

    edges = resultSMG.getHVEdges(SMGEdgeHasValueFilter.objectFilter(obj2));
    Assert.assertTrue(edges.iterator().hasNext());
  }

  @Test
	public final void joinFieldsRelaxStatusTest() {
    SMGRegion object = new SMGRegion(SIZE8, "Object");
    smg1.addHeapObject(object);

    WritableSMG smg04 = SMGFactory.createWritableCopy(smg1);
    WritableSMG smg48 = SMGFactory.createWritableCopy(smg1);
    WritableSMG smg26 = SMGFactory.createWritableCopy(smg1);
    WritableSMG smg08 = SMGFactory.createWritableCopy(smg1);

    final int offset4 = 4;
    final int offset2 = 2;

    smg04.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, 0, object, smg04.getNullValue()));
		smg48.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, offset4, object, smg48.getNullValue()));
    smg26.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, offset2, object, smg26.getNullValue()));
    smg08.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, 0, object, smg08.getNullValue()));
    smg08.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, offset4, object, smg08.getNullValue()));

    Assert.assertEquals(SMGJoinStatus.INCOMPARABLE,
        SMGJoinFields.joinFieldsRelaxStatus(smg04, smg48,
            SMGJoinStatus.EQUAL, SMGJoinStatus.INCOMPARABLE, object));
    Assert.assertEquals(SMGJoinStatus.INCOMPARABLE,
        SMGJoinFields.joinFieldsRelaxStatus(smg04, smg26,
            SMGJoinStatus.EQUAL, SMGJoinStatus.INCOMPARABLE, object));
    Assert.assertEquals(SMGJoinStatus.EQUAL,
        SMGJoinFields.joinFieldsRelaxStatus(smg04, smg08,
            SMGJoinStatus.EQUAL, SMGJoinStatus.INCOMPARABLE, object));

    Assert.assertEquals(SMGJoinStatus.INCOMPARABLE,
        SMGJoinFields.joinFieldsRelaxStatus(smg48, smg04,
            SMGJoinStatus.EQUAL, SMGJoinStatus.INCOMPARABLE, object));
    Assert.assertEquals(SMGJoinStatus.INCOMPARABLE,
        SMGJoinFields.joinFieldsRelaxStatus(smg48, smg26,
            SMGJoinStatus.EQUAL, SMGJoinStatus.INCOMPARABLE, object));
    Assert.assertEquals(SMGJoinStatus.EQUAL,
        SMGJoinFields.joinFieldsRelaxStatus(smg48, smg08,
            SMGJoinStatus.EQUAL, SMGJoinStatus.INCOMPARABLE, object));

    Assert.assertEquals(SMGJoinStatus.INCOMPARABLE,
        SMGJoinFields.joinFieldsRelaxStatus(smg26, smg04,
            SMGJoinStatus.EQUAL, SMGJoinStatus.INCOMPARABLE, object));
    Assert.assertEquals(SMGJoinStatus.INCOMPARABLE,
        SMGJoinFields.joinFieldsRelaxStatus(smg26, smg48,
            SMGJoinStatus.EQUAL, SMGJoinStatus.INCOMPARABLE, object));
    Assert.assertEquals(SMGJoinStatus.EQUAL,
        SMGJoinFields.joinFieldsRelaxStatus(smg26, smg08,
            SMGJoinStatus.EQUAL, SMGJoinStatus.INCOMPARABLE, object));

    Assert.assertEquals(SMGJoinStatus.INCOMPARABLE,
        SMGJoinFields.joinFieldsRelaxStatus(smg08, smg04,
            SMGJoinStatus.EQUAL, SMGJoinStatus.INCOMPARABLE, object));
    Assert.assertEquals(SMGJoinStatus.INCOMPARABLE,
        SMGJoinFields.joinFieldsRelaxStatus(smg08, smg48,
            SMGJoinStatus.EQUAL, SMGJoinStatus.INCOMPARABLE, object));
    Assert.assertEquals(SMGJoinStatus.INCOMPARABLE,
        SMGJoinFields.joinFieldsRelaxStatus(smg08, smg26,
            SMGJoinStatus.EQUAL, SMGJoinStatus.INCOMPARABLE, object));
  }

  @Test(expected = IllegalArgumentException.class)
	public final void differentSizeCheckTest() {
  	final int size8 = 8;
  	final int size12 = 12;
    SMGRegion obj1 = new SMGRegion(size8, "Object 1");
    SMGRegion obj2 = new SMGRegion(size12, "Object 2");
    smg1.addHeapObject(obj1);
    smg2.addHeapObject(obj2);

    new SMGJoinFields(smg1, smg2, obj1, obj2);
  }

  @Test
	public final void consistencyCheckTest() {
		SMGRegion obj1 = new SMGRegion(SIZE32, "Object 1");
    SMGRegion obj2 = new SMGRegion(SIZE32, "Object 2");

    smg1.addHeapObject(obj1);
    smg2.addHeapObject(obj2);
    smg1.addValue(value1);
    smg2.addValue(value2);

    SMGEdgeHasValue hvAt0in1 = new SMGEdgeHasValue(MOCKTYPE4, 0, obj1, value1);
    SMGEdgeHasValue hvAt0in2 = new SMGEdgeHasValue(MOCKTYPE4, 0, obj2, value2);
    smg1.addHasValueEdge(hvAt0in1);
    smg2.addHasValueEdge(hvAt0in2);
    SMGJoinFields.checkResultConsistency(smg1, smg2, obj1, obj2);

    final int offset4 = 4;
		smg1.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, offset4, obj1, smg1.getNullValue()));
    smg2.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, offset4, obj2, smg2.getNullValue()));
    SMGJoinFields.checkResultConsistency(smg1, smg2, obj1, obj2);

    final int offset8 = 8;
    final int offset12 = 12;
		smg1.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, offset8, obj1, smg1.getNullValue()));
    smg1.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, offset12, obj1, smg1.getNullValue()));
    smg1.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE8, offset8, obj1, smg1.getNullValue()));

    smg2.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, offset8, obj2, smg2.getNullValue()));
    smg2.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, offset12, obj2, smg2.getNullValue()));
    smg2.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE8, offset8, obj2, smg2.getNullValue()));

    SMGJoinFields.checkResultConsistency(smg1, smg2, obj1, obj2);

    final int offset16 = 16;
		smg1.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, offset16, obj1, value1));
    smg1.addPointsToEdge(new SMGEdgePointsTo(value1, obj1, 0));
    smg2.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, offset16, obj2, smg2.getNullValue()));
    SMGJoinFields.checkResultConsistency(smg1, smg2, obj1, obj2);
  }

  @Test(expected = IllegalStateException.class)
	public final void consistencyCheckNegativeTest1() {
    SMGRegion obj1 = new SMGRegion(SIZE32, "Object 1");
    SMGRegion obj2 = new SMGRegion(SIZE32, "Object 2");

    smg1.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, 0, obj1, value1));
    SMGJoinFields.checkResultConsistency(smg1, smg2, obj1, obj2);
  }

  @Test(expected = IllegalStateException.class)
	public final void consistencyCheckNegativeTest2() {
    SMGRegion obj1 = new SMGRegion(SIZE32, "Object 1");
    SMGRegion obj2 = new SMGRegion(SIZE32, "Object 2");

    smg1.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, 0, obj1, smg1.getNullValue()));
    smg2.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE8, 0, obj2, smg2.getNullValue()));
    SMGJoinFields.checkResultConsistency(smg1, smg2, obj1, obj2);
  }

  @Test(expected = IllegalStateException.class)
	public final void consistencyCheckNegativeTest3() {
    SMGRegion obj1 = new SMGRegion(SIZE32, "Object 1");
    SMGRegion obj2 = new SMGRegion(SIZE32, "Object 2");

    final int offset4 = 4;

    smg1.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, 0, obj1, smg1.getNullValue()));
		smg1.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, offset4, obj1, smg1.getNullValue()));
    smg2.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE8, 0, obj2, smg2.getNullValue()));
    SMGJoinFields.checkResultConsistency(smg1, smg2, obj1, obj2);
  }

  @Test(expected = IllegalStateException.class)
	public final void consistencyCheckNegativeTest4() {
		SMGRegion obj1 = new SMGRegion(SIZE32, "Object 1");
    SMGRegion obj2 = new SMGRegion(SIZE32, "Object 2");

    final int offset4 = 4;
    smg1.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, 0, obj1, smg1.getNullValue()));
		smg1.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, offset4, obj1, smg1.getNullValue()));
    smg2.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE8, 0, obj2, smg2.getNullValue()));

    final int offset8 = 8;
    final int offset12 = 12;
    smg2.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, offset8, obj2, smg2.getNullValue()));
    smg2.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, offset12, obj2, smg2.getNullValue()));
    smg1.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE8, offset8, obj1, smg1.getNullValue()));
    SMGJoinFields.checkResultConsistency(smg1, smg2, obj1, obj2);
  }

  @Test(expected = IllegalStateException.class)
	public final void consistencyCheckNegativeTest5() {
    SMGRegion obj1 = new SMGRegion(SIZE32, "Object 1");
    SMGRegion obj2 = new SMGRegion(SIZE32, "Object 2");

    smg1.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, 0, obj1, smg1.getNullValue()));
    smg2.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, 0, obj2, value2));
    SMGJoinFields.checkResultConsistency(smg1, smg2, obj1, obj2);
  }

  @Test
	public final void consistencyCheckPositiveTest1() {
		SMGRegion obj1 = new SMGRegion(SIZE32, "Object 1");
    SMGRegion obj2 = new SMGRegion(SIZE32, "Object 2");
    SMGRegion obj3 = new SMGRegion(SIZE32, "Object 3");

    smg1.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, 0, obj1, value1));
    smg2.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, 0, obj2, value2));
    smg2.addHasValueEdge(new SMGEdgeHasValue(MOCKTYPE4, 0, obj3, value2));
    SMGJoinFields.checkResultConsistency(smg1, smg2, obj1, obj2);
  }

  @Test(expected = IllegalArgumentException.class)
	public final void nonMemberObjectTest1() {
    SMGRegion obj1 = new SMGRegion(SIZE32, "Object 1");
    SMGRegion obj2 = new SMGRegion(SIZE32, "Object 2");
    smg2.addHeapObject(obj2);

    new SMGJoinFields(smg1, smg2, obj1, obj2);
  }

  @Test(expected = IllegalArgumentException.class)
	public final void nonMemberObjectTest2() {
    SMGRegion obj1 = new SMGRegion(SIZE32, "Object 1");
    SMGRegion obj2 = new SMGRegion(SIZE32, "Object 2");
    smg1.addHeapObject(obj1);

    new SMGJoinFields(smg1, smg2, obj1, obj2);
  }
}
