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

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;

import cz.afri.smg.SMGAbstractionCandidate;
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

public class SMGSingleLinkedListFinderTest {

	private static final int SIZE8 = 8;
	private static final CType TYPE8 = CType.createTypeWithLength(SIZE8);
  private static final int SIZE16 = 16;
	private static final int SIZE24 = 24;

	private WritableSMG smg;

  @Before
	public final void setUp() {
    smg = SMGFactory.createWritableSMG();
  }

  @Test
	public final void candidateWithUnknownEndTest() {
    final int length3 = 3;
		final int offset8 = 8;
		SMGEdgeHasValue hv = TestHelpers.createGlobalList(smg, length3, SIZE16, offset8, "list_1_");
    SMGEdgeHasValue iterator = hv;
    while (iterator.getValue() != smg.getNullValue()) {
      SMGObject next = smg.getPointer(iterator.getValue()).getObject();
      iterator = smg.getUniqueHV(SMGEdgeHasValueFilter.objectFilter(next), true);
    }
    smg.removeHasValueEdge(iterator);
    Integer newValue = SMGValueFactory.getNewValue();
    smg.addValue(newValue);
    smg.addHasValueEdge(new SMGEdgeHasValue(CPointerType.getVoidPointer(), 0, iterator.getObject(), newValue));
    SMGSingleLinkedListFinder finderOf2 = new SMGSingleLinkedListFinder(1);

    singleCandidateCheck(finderOf2, 2, offset8);
  }

  @Test
	public final void incompatibleCandidatesTest() {
    final int length3 = 3;
    final int offset8 = 8;
		Integer list1 = TestHelpers.createList(smg, length3, SIZE24, 0, "list_1_");

		SMGEdgeHasValue hv = TestHelpers.createGlobalList(smg, length3, SIZE16, offset8, "list_2_");
    SMGEdgeHasValue iterator = hv;
    while (iterator.getValue() != smg.getNullValue()) {
      SMGObject next = smg.getPointer(iterator.getValue()).getObject();
      iterator = smg.getUniqueHV(SMGEdgeHasValueFilter.objectFilter(next), true);
    }

    smg.removeHasValueEdge(iterator);
    smg.addHasValueEdge(new SMGEdgeHasValue(CPointerType.getVoidPointer(), 0, iterator.getObject(), list1));
    final int threshold5 = 5;
		SMGSingleLinkedListFinder finderOf6 = new SMGSingleLinkedListFinder(threshold5);
    Set<SMGAbstractionCandidate> candidates = finderOf6.traverse(smg);
    Assert.assertEquals(0, candidates.size());

    SMGSingleLinkedListFinder finderOf3 = new SMGSingleLinkedListFinder(2);
    singleCandidateCheck(finderOf3, length3, 0);

    SMGSingleLinkedListFinder finderOf2 = new SMGSingleLinkedListFinder(1);
    candidates = finderOf2.traverse(smg);
    Assert.assertEquals(2, candidates.size());
  }

  @Test
	public final void simpleConstructorTest() {
    final int length11 = 11;
		final int size16 = 16;
		final int offset8 = 8;
		TestHelpers.createGlobalList(smg, length11, size16, offset8, "pointer");
    SMGSingleLinkedListFinder finder = new SMGSingleLinkedListFinder();
    singleCandidateCheck(finder, length11, offset8);
  }

  private SMGSingleLinkedListCandidate singleCandidateCheck(final SMGSingleLinkedListFinder pFinder, final int pLen,
  		                                                      final int pOffset) {
    Set<SMGAbstractionCandidate> candidates = pFinder.traverse(smg);
    Assert.assertEquals(1, candidates.size());
    SMGAbstractionCandidate candidate = Iterables.getOnlyElement(candidates);
    Assert.assertTrue(candidate instanceof SMGSingleLinkedListCandidate);
    SMGSingleLinkedListCandidate sllCandidate = (SMGSingleLinkedListCandidate) candidate;
    Assert.assertEquals(pLen, sllCandidate.getLength());
    Assert.assertEquals(pOffset, sllCandidate.getOffset());
    return sllCandidate;
  }

  @Test
	public final void simpleListTest() {
    final int length5 = 5;
		final int offset8 = 8;
		SMGEdgeHasValue root = TestHelpers.createGlobalList(smg, length5, SIZE16, offset8, "pointer");
    SMGSingleLinkedListFinder finder = new SMGSingleLinkedListFinder(1);

    SMGSingleLinkedListCandidate sllCandidate = singleCandidateCheck(finder, length5, offset8);

    SMGRegion expectedStart = (SMGRegion) smg.getPointer(root.getValue()).getObject();
    Assert.assertSame(expectedStart, sllCandidate.getStart());
  }

  @Test
	public final void nullifiedPointerInferenceTest() {
    final int offset8 = 8;
		TestHelpers.createGlobalList(smg, 2, SIZE16, offset8, "pointer");

    SMGSingleLinkedListFinder finder = new SMGSingleLinkedListFinder(1);
    singleCandidateCheck(finder, 2, offset8);
  }

  @Test
	public final void listWithInboundPointersTest() {
    final int lenght4 = 4;
		final int offset8 = 8;
		Integer tail = TestHelpers.createList(smg, lenght4, SIZE16, offset8, "tail");

    final int length3 = 3;
		SMGEdgeHasValue head = TestHelpers.createGlobalList(smg, length3, SIZE16, offset8, "head");

    SMGObject inside = new SMGRegion(SIZE16, "pointed_at");
    SMGEdgeHasValue tailConnection = new SMGEdgeHasValue(CPointerType.getVoidPointer(), offset8, inside, tail);

    Integer addressOfInside = SMGValueFactory.getNewValue();
    SMGEdgePointsTo insidePT = new SMGEdgePointsTo(addressOfInside, inside, 0);
    SMGRegion inboundPointer = smg.addGlobalVariable(TYPE8, "inbound_pointer");
    SMGEdgeHasValue inboundPointerConnection = new SMGEdgeHasValue(CPointerType.getVoidPointer(), 0, inboundPointer,
                                                                   addressOfInside);

    SMGObject lastFromHead = smg.getPointer(head.getValue()).getObject();
    SMGEdgeHasValue connection = null;
    do {
      SMGEdgeHasValueFilter filter = SMGEdgeHasValueFilter.objectFilter(lastFromHead).filterAtOffset(offset8);
      Iterable<SMGEdgeHasValue> connections = smg.getHVEdges(filter);
      connection = null;
      if (connections.iterator().hasNext()) {
        connection = Iterables.getOnlyElement(connections);
        lastFromHead = smg.getPointer(connection.getValue()).getObject();
      }
    } while (connection != null);

    Set<SMGEdgeHasValue> toRemove = new HashSet<>();
    for (SMGEdgeHasValue hv : smg.getHVEdges(SMGEdgeHasValueFilter.objectFilter(lastFromHead))) {
      toRemove.add(hv);
    }
    for (SMGEdgeHasValue hv : toRemove) {
      smg.removeHasValueEdge(hv);
    }

    SMGEdgeHasValue headConnection = new SMGEdgeHasValue(CPointerType.getVoidPointer(), offset8, lastFromHead,
    		                                                 addressOfInside);

    SMGRegion tailPointer = smg.addGlobalVariable(TYPE8, "tail_pointer");
    SMGEdgeHasValue tailPointerConnection = new SMGEdgeHasValue(CPointerType.getVoidPointer(), 0, tailPointer, tail);

    smg.addHasValueEdge(tailPointerConnection);

    smg.addHeapObject(inside);
    smg.addValue(addressOfInside);
    smg.addPointsToEdge(insidePT);

    smg.addHasValueEdge(inboundPointerConnection);

    smg.addHasValueEdge(tailConnection);
    smg.addHasValueEdge(headConnection);

    SMGSingleLinkedListFinder finder = new SMGSingleLinkedListFinder(1);
    Set<SMGAbstractionCandidate> candidates = finder.traverse(smg);
    Assert.assertEquals(2, candidates.size());

    boolean sawHead = false;
    boolean sawTail = false;
    for (SMGAbstractionCandidate candidate : candidates) {
      SMGSingleLinkedListCandidate sllCandidate = (SMGSingleLinkedListCandidate) candidate;
      if (sllCandidate.getLength() == length3) {
        Assert.assertSame(smg.getPointer(head.getValue()).getObject(), sllCandidate.getStart());
        Assert.assertFalse(sawHead);
        sawHead = true;
      } else if (sllCandidate.getLength() == lenght4) {
        Assert.assertSame(smg.getPointer(tail).getObject(), sllCandidate.getStart());
        Assert.assertFalse(sawTail);
      } else {
        Assert.fail("We should not see any candidates with length other than 3 or 4");
      }
    }
  }
}
