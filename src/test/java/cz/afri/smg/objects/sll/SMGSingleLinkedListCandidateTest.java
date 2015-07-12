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

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import cz.afri.smg.graphs.ReadableSMG;
import cz.afri.smg.graphs.SMGEdgeHasValue;
import cz.afri.smg.graphs.SMGEdgeHasValueFilter;
import cz.afri.smg.graphs.SMGFactory;
import cz.afri.smg.graphs.SMGValueFactory;
import cz.afri.smg.graphs.WritableSMG;
import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.objects.SMGRegion;
import cz.afri.smg.types.CPointerType;
import cz.afri.smg.types.CType;


public class SMGSingleLinkedListCandidateTest {

	private static final int SIZE8 = 8;
  private static final int SIZE16 = 16;


	@Test
	public final void basicTest() {
    SMGObject object = new SMGRegion(SIZE8, "object");
    final int offset4 = 4;
		SMGSingleLinkedListCandidate candidate = new SMGSingleLinkedListCandidate(object, offset4, 2);

    Assert.assertSame(object, candidate.getStart());
    Assert.assertEquals(offset4, candidate.getOffset());
    Assert.assertEquals(2, candidate.getLength());

    final int additionalLength = 4;
		candidate.addLength(additionalLength);
    Assert.assertEquals(offset4, candidate.getOffset());
    final int expectedLength = 6;
		Assert.assertEquals(expectedLength, candidate.getLength());

    Assert.assertEquals(0, candidate.getScore());
    Assert.assertFalse(candidate.toString().contains("@"));
  }


  @Test
	public final void isCompatibleWithTest() {
    SMGObject firstObject8 = new SMGRegion(SIZE8, "object 1");
    SMGObject secondObject8 = new SMGRegion(SIZE8, "object 2");
    SMGObject object16 = new SMGRegion(SIZE16, "object 3");

    final int offset4 = 4;
		SMGSingleLinkedListCandidate firstCandidate8 = new SMGSingleLinkedListCandidate(firstObject8, offset4, 2);
    final int length8 = 8;
		SMGSingleLinkedListCandidate secondCandidate8 = new SMGSingleLinkedListCandidate(secondObject8, offset4,
				                                                                             length8);
    SMGSingleLinkedListCandidate candidate16 = new SMGSingleLinkedListCandidate(object16, offset4, 2);

    Assert.assertTrue(firstCandidate8.isCompatibleWith(secondCandidate8));
    Assert.assertTrue(secondCandidate8.isCompatibleWith(firstCandidate8));
    Assert.assertFalse(candidate16.isCompatibleWith(firstCandidate8));
    Assert.assertFalse(firstCandidate8.isCompatibleWith(candidate16));

    final int offset6 = 6;
		secondCandidate8 = new SMGSingleLinkedListCandidate(secondObject8, offset6, 2);
    Assert.assertFalse(firstCandidate8.isCompatibleWith(secondCandidate8));
  }

  @Test
	public final void executeOnSimpleList() {
    WritableSMG smg = SMGFactory.createWritableSMG();

    final int nodeSize = 16;
    final int segmentLength = 18;
    final int offset = 8;

    SMGEdgeHasValue root = TestHelpers.createGlobalList(smg, segmentLength + 1, nodeSize, offset, "pointer");
    Integer value = root.getValue();

    SMGObject startObject = smg.getPointer(value).getObject();
    SMGSingleLinkedListCandidate candidate = new SMGSingleLinkedListCandidate(startObject, offset, segmentLength);

    ReadableSMG abstractedSmg = candidate.execute(smg);
    Set<SMGObject> heap = abstractedSmg.getHeapObjects();
    final int expectedHeapSize = 3;
		Assert.assertEquals(expectedHeapSize, heap.size());
    SMGObject pointedObject = abstractedSmg.getPointer(value).getObject();
    Assert.assertTrue(pointedObject instanceof SMGSingleLinkedList);
    Assert.assertTrue(pointedObject.isAbstract());
    SMGSingleLinkedList segment = (SMGSingleLinkedList) pointedObject;
    Assert.assertEquals(nodeSize, segment.getSize());
    Assert.assertEquals(segmentLength, segment.getLength());
    Assert.assertEquals(offset, segment.getOffset());

    SMGEdgeHasValue onlyOutboundEdge = abstractedSmg.getUniqueHV(SMGEdgeHasValueFilter.objectFilter(segment), true);
    Assert.assertEquals(offset, onlyOutboundEdge.getOffset());
    Assert.assertSame(CPointerType.getVoidPointer(), onlyOutboundEdge.getType());

    SMGObject stopper = abstractedSmg.getPointer(onlyOutboundEdge.getValue()).getObject();
    Assert.assertTrue(stopper instanceof SMGRegion);
    SMGRegion stopperRegion = (SMGRegion) stopper;
    Assert.assertEquals(nodeSize, stopperRegion.getSize());
    onlyOutboundEdge = abstractedSmg.getUniqueHV(SMGEdgeHasValueFilter.objectFilter(stopper), true);
    Assert.assertEquals(0, onlyOutboundEdge.getValue());
    Assert.assertEquals(0, onlyOutboundEdge.getOffset());
    Assert.assertEquals(nodeSize, onlyOutboundEdge.getSizeInBytes());
  }

  @Test
	public final void executeOnAmbiguousList() {
    WritableSMG smg = SMGFactory.createWritableSMG();

    SMGRegion pointer = smg.addGlobalVariable(CPointerType.getVoidPointer(), "pointer");
    SMGObject first = new SMGRegion(SIZE16, "first");
    smg.addHeapObject(first);
    Integer unknownValue = SMGValueFactory.getNewValue();
    smg.addValue(unknownValue);
    smg.addHasValueEdge(new SMGEdgeHasValue(CType.getIntType(), 0, first, unknownValue));
    TestHelpers.connect(smg, pointer, first, 0);
    Integer list = TestHelpers.createList(smg, 2, SIZE16, 0, "list_");
    TestHelpers.connect(smg, first, smg.getObjectPointedBy(list), 0);
    smg.removePointsToEdge(list);
    SMGSingleLinkedListCandidate candidate = new SMGSingleLinkedListCandidate(first, 0, 2);
    candidate.execute(smg);
  }

  @Test
	public final void executeOnNullTerminatedList() {
    WritableSMG smg = SMGFactory.createWritableSMG();
    final int offset8 = 8;
		SMGEdgeHasValue root = TestHelpers.createGlobalList(smg, 2, SIZE16, offset8, "pointer");

    Integer value = root.getValue();
    SMGObject startObject = smg.getPointer(value).getObject();
    SMGSingleLinkedListCandidate candidate = new SMGSingleLinkedListCandidate(startObject, offset8, 2);
    ReadableSMG abstractedSmg = candidate.execute(smg);
    Set<SMGObject> heap = abstractedSmg.getHeapObjects();
    Assert.assertEquals(2, heap.size());

    SMGObject sll = abstractedSmg.getPointer(value).getObject();
    Assert.assertTrue(sll.isAbstract());
    Assert.assertTrue(sll instanceof SMGSingleLinkedList);
    SMGSingleLinkedList realSll = (SMGSingleLinkedList) sll;
    Assert.assertEquals(2, realSll.getLength());
    SMGEdgeHasValue outbound = abstractedSmg.getUniqueHV(SMGEdgeHasValueFilter.objectFilter(realSll), true);
    Assert.assertEquals(offset8, outbound.getOffset());
    Assert.assertEquals(SIZE8, outbound.getSizeInBytes());
    Assert.assertEquals(0, outbound.getValue());
  }
}
