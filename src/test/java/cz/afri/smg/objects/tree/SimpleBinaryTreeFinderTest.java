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
package cz.afri.smg.objects.tree;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cz.afri.smg.SMGAbstractionCandidate;
import cz.afri.smg.graphs.SMGFactory;
import cz.afri.smg.graphs.WritableSMG;
import cz.afri.smg.objects.SMGRegion;

public class SimpleBinaryTreeFinderTest {

  private WritableSMG smg;
  private final SMGRegion nodePrototype = new SMGRegion(16, "prototype");
  private static final int LEFT_OFFSET = 0;
  private static final int RIGHT_OFFSET = 8;


  @Before
	public final void setUp() {
    smg = SMGFactory.createWritableSMG();
  }

  @Test
	public final void simpleTreeTest() {
    TestHelpers.createGlobalPointerToThreeLevelTree(smg, nodePrototype, LEFT_OFFSET, RIGHT_OFFSET);
    SimpleBinaryTreeFinder finder = new SimpleBinaryTreeFinder();
    Set<SMGAbstractionCandidate> candidates = finder.traverse(smg);
    Assert.assertEquals(1, candidates.size());
  }
}
