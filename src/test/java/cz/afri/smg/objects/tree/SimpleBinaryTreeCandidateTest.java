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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;

import cz.afri.smg.graphs.SMGEdgeHasValue;
import cz.afri.smg.graphs.SMGEdgeHasValueFilter;
import cz.afri.smg.graphs.SMGFactory;
import cz.afri.smg.graphs.WritableSMG;
import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.objects.SMGRegion;
import cz.afri.smg.types.CPointerType;

public class SimpleBinaryTreeCandidateTest {
  private final SMGRegion prototype = new SMGRegion(16, "root");
  private WritableSMG smg;

  @Before
	public final void setUp() {
    smg = SMGFactory.createWritableSMG();
  }

  @Test
	public final void basicSimpleBinaryTreeCandidateTest() {
    final int highOffset = 8;
		TreeBinding binding = new TreeBinding(0, highOffset, prototype.getSize());
    SimpleBinaryTreeCandidate candidate = new SimpleBinaryTreeCandidate(prototype, 0, highOffset, 1);
    Assert.assertEquals(candidate.getBinding(), binding);
    Assert.assertEquals(candidate.getDepth(), 1);
  }

  @Test
	public final void basicCandidateSuitabilityTest() {
    final int highOffset = 8;
		SimpleBinaryTreeCandidate candidate = new SimpleBinaryTreeCandidate(prototype, 0, highOffset, 1);
    Assert.assertFalse(candidate.isProcessed());
    candidate.setSuitable();
    Assert.assertTrue(candidate.isProcessed());
    Assert.assertTrue(candidate.isSuitable());
    candidate.setUnsuitable();
    Assert.assertTrue(candidate.isProcessed());
    Assert.assertFalse(candidate.isSuitable());
  }

  @Test
	public final void treeExecuteTest() {
    SMGRegion pointer = smg.addGlobalVariable(CPointerType.getVoidPointer(), "pointer");
    final int rightOffset = 8;
		SMGRegion root = TestHelpers.createCompleteThreeLevelTree(smg, prototype, 0, rightOffset);
    TestHelpers.connect(smg, pointer, root, 0);
    SimpleBinaryTreeCandidate candidate = new SimpleBinaryTreeCandidate(root, 0, rightOffset, 2);
    WritableSMG abstractedSmg = SMGFactory.createWritableCopy(candidate.execute(smg));
    Integer rootValue = abstractedSmg.readValue(pointer, 0, CPointerType.getVoidPointer()).getAsInt();
    SMGObject expectedTree = abstractedSmg.getObjectPointedBy(rootValue);
    Assert.assertTrue(expectedTree instanceof SimpleBinaryTree);
    SimpleBinaryTree tree = (SimpleBinaryTree) expectedTree;
    Assert.assertEquals(2, tree.getDepth());
    Iterable<SMGEdgeHasValue> outbound = abstractedSmg.getHVEdges(SMGEdgeHasValueFilter.objectFilter(tree));
    Assert.assertEquals(2, Iterables.size(outbound));
    SMGEdgeHasValue hvPrevious = null;
    for (SMGEdgeHasValue hv : outbound) {
      Assert.assertSame(tree, hv.getObject());
      Assert.assertEquals(CPointerType.getVoidPointer().getSize(), hv.getSizeInBytes());
      Assert.assertEquals(abstractedSmg.getNullValue(), hv.getValue());
      if (hvPrevious != null) {
        Assert.assertTrue((hv.getOffset() == 0 && hvPrevious.getOffset() == rightOffset) ||
        		          (hv.getOffset() == rightOffset && hvPrevious.getOffset() == 0));
      }
      hvPrevious = hv;
    }

    abstractedSmg.pruneUnreachable();
    Assert.assertEquals(2, abstractedSmg.getHeapObjects().size());
    rootValue = abstractedSmg.readValue(pointer, 0, CPointerType.getVoidPointer()).getAsInt();
    expectedTree = abstractedSmg.getObjectPointedBy(rootValue);
    Assert.assertTrue(expectedTree instanceof SimpleBinaryTree);
    Assert.assertFalse(abstractedSmg.hasMemoryLeaks());
  }

  @Test
	public final void basicTreeBindingTest() {
    final int highOffset8 = 8;
		final int size16 = 16;
		TreeBinding binding0816 = new TreeBinding(0, highOffset8, size16);
    Assert.assertEquals(0, binding0816.getLowerOffset());
    Assert.assertEquals(highOffset8, binding0816.getHigherOffset());
    Assert.assertEquals(size16, binding0816.getSize());

    final int lowOffset8 = 8;
		TreeBinding binding8016 = new TreeBinding(lowOffset8, 0, size16);
    Assert.assertEquals(0, binding8016.getLowerOffset());
    Assert.assertEquals(lowOffset8, binding8016.getHigherOffset());

    final int size24 = 24;
		TreeBinding binding8024 = new TreeBinding(lowOffset8, 0, size24);
    Assert.assertEquals(size24, binding8024.getSize());
    Assert.assertFalse(binding8016.equals(binding8024));

    Assert.assertEquals(binding0816, binding8016);
    Assert.assertEquals(binding0816.hashCode(), binding8016.hashCode());
  }
}
