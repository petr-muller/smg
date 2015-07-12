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

import cz.afri.smg.graphs.SMGFactory;
import cz.afri.smg.graphs.WritableSMG;
import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.objects.SMGRegion;

public class SMGJoinSubSMGsTest {

  private static final int SIZE8 = 8;

	private SMGJoinSubSMGs jssDefined;

  @Before
	public final void setUp() {
    WritableSMG smg1 = SMGFactory.createWritableSMG();
    WritableSMG smg2 = SMGFactory.createWritableSMG();
    WritableSMG destSmg = SMGFactory.createWritableSMG();

    SMGObject obj1 = new SMGRegion(SIZE8, "Test object 1");
    SMGObject obj2 = new SMGRegion(SIZE8, "Test object 2");

    smg1.addHeapObject(obj1);
    smg2.addHeapObject(obj2);

    SMGNodeMapping mapping1 = new SMGNodeMapping();
    SMGNodeMapping mapping2 = new SMGNodeMapping();

    jssDefined = new SMGJoinSubSMGs(SMGJoinStatus.EQUAL, smg1, smg2, destSmg, mapping1, mapping2, obj1, obj2, null);
  }

  @Test
	public final void testIsDefined() {
    Assert.assertTrue(jssDefined.isDefined());
  }

  @Test
	public final void testGetStatusOnDefined() {
    Assert.assertNotNull(jssDefined.getStatus());
  }

  @Test
	public final void testGetSMG1() {
    Assert.assertNotNull(jssDefined.getSMG1());
  }

  @Test
	public final void testGetSMG2() {
    Assert.assertNotNull(jssDefined.getSMG2());
  }

  @Test
	public final void testGetDestSMG() {
    Assert.assertNotNull(jssDefined.getDestSMG());
  }

  @Test
	public final void testGetMapping1() {
    Assert.assertNotNull(jssDefined.getMapping1());
  }

  @Test
	public final void testGetMapping2() {
    Assert.assertNotNull(jssDefined.getMapping2());
  }
}
