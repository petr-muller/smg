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

import org.junit.Assert;
import org.junit.Test;

import cz.afri.smg.objects.DummyAbstraction;
import cz.afri.smg.objects.SMGObjectVisitor;
import cz.afri.smg.objects.SMGRegion;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


public class SMGSingleLinkedListTest {
  private static final int SIZE16 = 16;
	private static final int SIZE32 = 32;
	private static SMGRegion prototype16 = new SMGRegion(SIZE16, "prototype");

  private static void checkSLL(final SMGSingleLinkedList pSll, final boolean pAbstract, final int pLen, final int pSize,
  												     final int pOffset) {
    Assert.assertEquals(pSll.isAbstract(), pAbstract);
    Assert.assertEquals(pLen, pSll.getLength());
    Assert.assertEquals(pSize, pSll.getSize());
    Assert.assertEquals(pOffset, pSll.getOffset());
  }

  @Test
	public final void basicsTest() {
    final int offset2 = 2;
		final int length4 = 4;
		SMGSingleLinkedList sll = new SMGSingleLinkedList(prototype16, offset2, length4);
    checkSLL(sll, true, length4, SIZE16, offset2);

    SMGSingleLinkedList sllCopy = new SMGSingleLinkedList(sll);
    checkSLL(sllCopy, true, length4, SIZE16, offset2);
  }

  @Test
	public final void matchGenericShapeTest() {
    final int length4 = 4;
    final int length7 = 7;
    final int offset8 = 8;

		SMGSingleLinkedList sll1 = new SMGSingleLinkedList(prototype16, 0, length4);
		SMGSingleLinkedList sll2 = new SMGSingleLinkedList(prototype16, 0, length7);
		SMGSingleLinkedList sll3 = new SMGSingleLinkedList(prototype16, offset8, length4);

    DummyAbstraction dummy = new DummyAbstraction(prototype16);

    Assert.assertFalse(sll1.matchGenericShape(dummy));
    Assert.assertTrue(sll1.matchGenericShape(sll2));
    Assert.assertTrue(sll2.matchGenericShape(sll3));
    Assert.assertTrue(sll1.matchGenericShape(sll3));
  }

  @Test
	public final void matchSpecificShapeTest() {
    final int length4 = 4;
    final int length7 = 7;
		SMGSingleLinkedList sll1 = new SMGSingleLinkedList(prototype16, 0, length4);
    SMGSingleLinkedList sll2 = new SMGSingleLinkedList(prototype16, 0, length7);

    final int offset8 = 8;
		SMGSingleLinkedList sll3 = new SMGSingleLinkedList(prototype16, offset8, length4);

    DummyAbstraction dummy = new DummyAbstraction(prototype16);

    Assert.assertFalse(sll1.matchSpecificShape(dummy));
    Assert.assertTrue(sll1.matchSpecificShape(sll2));
    Assert.assertFalse(sll2.matchSpecificShape(sll3));
    Assert.assertFalse(sll1.matchSpecificShape(sll3));
  }

  @Test(expected = IllegalArgumentException.class)
	public final void isMoreGenericDiffSizeTest() {
    SMGRegion prototype2 = new SMGRegion(SIZE32, "prototype_2");
    final int offset8 = 8;
    final int length8 = 8;
		SMGSingleLinkedList sll1 = new SMGSingleLinkedList(prototype16, offset8, length8);
    SMGSingleLinkedList sll2 = new SMGSingleLinkedList(prototype2, offset8, length8);
    sll1.isMoreGeneral(sll2);
  }

  @Test
	public final void isMoreGenericConcreteTest() {
    final int offset8 = 8;
		final int length8 = 8;
		SMGSingleLinkedList sll1 = new SMGSingleLinkedList(prototype16, offset8, length8);
    Assert.assertTrue(sll1.isMoreGeneral(prototype16));
  }

  @Test(expected = IllegalArgumentException.class)
	public final void isMoreGenericNonMatchTest() {
    final int length8 = 8;
		SMGSingleLinkedList sll1 = new SMGSingleLinkedList(prototype16, 0, length8);

    final int offset8 = 8;
		final int length10 = 10;
		SMGSingleLinkedList sll2 = new SMGSingleLinkedList(prototype16, offset8, length10);
    sll1.isMoreGeneral(sll2);
  }

  @Test
	public final void isMoreGenericMatchTest() {
    final int offset8 = 8;
		final int length4 = 4;
		SMGSingleLinkedList sll1 = new SMGSingleLinkedList(prototype16, offset8, length4);

    final int length12 = 12;
		SMGSingleLinkedList sll2 = new SMGSingleLinkedList(prototype16, offset8, length12);

    Assert.assertTrue(sll1.isMoreGeneral(sll2));
    Assert.assertFalse(sll2.isMoreGeneral(sll1));
  }

  @Test
	public final void toStringTest() {
    final int offset8 = 8;
		final int length4 = 4;
		Assert.assertFalse(new SMGSingleLinkedList(prototype16, offset8, length4).toString().contains("@"));
  }

  @Test
	public final void objectVisitorTest() {
    SMGObjectVisitor visitor = new SMGObjectVisitor() {
      @Override
      public void visit(final SMGSingleLinkedList pObject) {
        Assert.assertTrue(true);
      }
    };
    final int offset8 = 8;
		final int length4 = 4;
		SMGSingleLinkedList sll1 = new SMGSingleLinkedList(prototype16, offset8, length4);
    sll1.accept(visitor);
  }

  @Test
	public final void joinTest() {
    final int offset8 = 8;
    final int length4 = 4;
    final int length8 = 8;
		SMGSingleLinkedList sll4 = new SMGSingleLinkedList(prototype16, offset8, length4);
    SMGSingleLinkedList sll8 = new SMGSingleLinkedList(prototype16, offset8, length8);

    SMGSingleLinkedList sllJoinConcrete = sll4.join(prototype16);
    checkSLL(sllJoinConcrete, true, length4, SIZE16, offset8);

    SMGSingleLinkedList sllJoinIdentical = sll4.join(sll4);
    checkSLL(sllJoinIdentical, true, length4, SIZE16, offset8);

    SMGSingleLinkedList sllJoinLarger = sll8.join(sll4);
    checkSLL(sllJoinLarger, true, length4, SIZE16, offset8);
    sllJoinLarger = sll4.join(sll8);
    checkSLL(sllJoinLarger, true, length4, SIZE16, offset8);
  }

  @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_INFERRED",
                      justification = "We are testing exception is raised in join(), we do not care about return value")
  @Test(expected = UnsupportedOperationException.class)
	public final void joinIncompatibleTest() {
    final int offset8 = 8;
		final int length4 = 4;
		SMGSingleLinkedList sll4 = new SMGSingleLinkedList(prototype16, offset8, length4);
    final int offset4 = 4;
		final int length8 = 8;
		SMGSingleLinkedList sllDifferent = new SMGSingleLinkedList(prototype16, offset4, length8);

    sll4.join(sllDifferent);
  }
}
