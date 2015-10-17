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

import com.google.common.collect.ImmutableList;

import cz.afri.smg.graphs.ReadableSMG;
import cz.afri.smg.graphs.SMGEdgeHasValue;
import cz.afri.smg.graphs.SMGEdgeHasValueFilter;
import cz.afri.smg.graphs.SMGFactory;
import cz.afri.smg.graphs.SMGValueFactory;
import cz.afri.smg.graphs.WritableSMG;
import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.objects.SMGRegion;
import cz.afri.smg.types.CFunctionDeclaration;
import cz.afri.smg.types.CFunctionType;
import cz.afri.smg.types.CParameterDeclaration;
import cz.afri.smg.types.CType;

public class SMGJoinTest {
  private static final CFunctionType FUNCTION_TYPE = CFunctionType.createSimpleFunctionType(CType.getIntType());
  private static final CFunctionDeclaration FUNCTION_DECLARATION = new CFunctionDeclaration(FUNCTION_TYPE, "foo",
  		                                                                 ImmutableList.<CParameterDeclaration>of());

  private static final int SIZE8 = 8;
  private static final CType TYPE8 = CType.createTypeWithLength(SIZE8);

  private WritableSMG smg1;
  private WritableSMG smg2;

  @Before
	public final void setUp() {
    smg1 = SMGFactory.createWritableSMG();
    smg2 = SMGFactory.createWritableSMG();
  }

  // Testing condition: adds an identical global variable to both SMGs
  private void addGlobalWithoutValueToBoth(final String pVarName) {
    smg1.addGlobalVariable(TYPE8, pVarName);
    smg2.addGlobalVariable(TYPE8, pVarName);
  }

  // Testing condition: adds an identical local variable to both SMGs
  private void addLocalWithoutValueToBoth(final String pVarName) {
    smg1.addLocalVariable(TYPE8, pVarName);
    smg2.addLocalVariable(TYPE8, pVarName);
  }

  // Testing condition: adds an identical global variable to both SMGs, with value
  private void addGlobalWithValueToBoth(final String pVarName) {
    SMGRegion global1 = smg1.addGlobalVariable(TYPE8, pVarName);
    SMGRegion global2 = smg2.addGlobalVariable(TYPE8, pVarName);
    Integer value1 = SMGValueFactory.getNewValue();
    Integer value2 = SMGValueFactory.getNewValue();

    final int size4 = 4;
		SMGEdgeHasValue hv1 = new SMGEdgeHasValue(size4, 0, global1, value1);
    SMGEdgeHasValue hv2 = new SMGEdgeHasValue(size4, 0, global2, value2);

    smg1.addValue(value1);
    smg2.addValue(value2);
    smg1.addHasValueEdge(hv1);
    smg2.addHasValueEdge(hv2);
  }

  // Testing condition: adds an identical local value to both SMGs, with value
  private void addLocalWithValueToBoth(final String pVarName) {
    SMGRegion local1 = smg1.addLocalVariable(TYPE8, pVarName);
    SMGRegion local2 = smg2.addLocalVariable(TYPE8, pVarName);
    Integer value1 = SMGValueFactory.getNewValue();
    Integer value2 = SMGValueFactory.getNewValue();

    final int size4 = 4;

    SMGEdgeHasValue hv1 = new SMGEdgeHasValue(size4, 0, local1, value1);
    SMGEdgeHasValue hv2 = new SMGEdgeHasValue(size4, 0, local2, value2);

    smg1.addValue(value1);
    smg2.addValue(value2);
    smg1.addHasValueEdge(hv1);
    smg2.addHasValueEdge(hv2);
  }

  private void assertObjectCounts(final ReadableSMG pSMG, final int pGlobals, final int pHeap, final int pFrames) {
    Assert.assertEquals(pSMG.getGlobalObjects().size(), pGlobals);
    Assert.assertEquals(pSMG.getHeapObjects().size(), pHeap);
    Assert.assertEquals(pSMG.getStackFrames().size(), pFrames);
  }

  @Test
	public final void simpleGlobalVarJoinTest() {
    String varName = "variableName";
    addGlobalWithoutValueToBoth(varName);
    SMGJoin join = new SMGJoin(smg1, smg2);
    Assert.assertTrue(join.isDefined());
    Assert.assertEquals(join.getStatus(), SMGJoinStatus.EQUAL);

    ReadableSMG resultSMG = join.getJointSMG();
    Assert.assertTrue(resultSMG.getGlobalObjects().containsKey(varName));
    assertObjectCounts(resultSMG, 1, 1, 0);
  }

  @Test
	public final void simpleLocalVarJoinTest() {
    String varName = "variableName";
    smg1.addStackFrame(FUNCTION_DECLARATION);
    smg2.addStackFrame(FUNCTION_DECLARATION);
    addLocalWithoutValueToBoth(varName);

    SMGJoin join = new SMGJoin(smg1, smg2);
    Assert.assertTrue(join.isDefined());
    Assert.assertEquals(join.getStatus(), SMGJoinStatus.EQUAL);

    ReadableSMG resultSMG = join.getJointSMG();
    Assert.assertTrue(resultSMG.getStackFrames().getFirst().containsVariable(varName));
    assertObjectCounts(resultSMG, 0, 1, 1);
  }

  @Test
	public final void globalVarWithValueJoinTest() {
    String varName = "variableName";
    addGlobalWithValueToBoth(varName);
    SMGJoin join = new SMGJoin(smg1, smg2);
    Assert.assertTrue(join.isDefined());
    Assert.assertEquals(join.getStatus(), SMGJoinStatus.EQUAL);

    ReadableSMG resultSMG = join.getJointSMG();
    Assert.assertTrue(resultSMG.getGlobalObjects().containsKey(varName));
    assertObjectCounts(resultSMG, 1, 1, 0);

    SMGObject global = resultSMG.getGlobalObjects().get(varName);
    SMGEdgeHasValueFilter filter = SMGEdgeHasValueFilter.objectFilter(global).filterAtOffset(0);

    Assert.assertTrue(resultSMG.getValues().contains(Integer.valueOf(resultSMG.getUniqueHV(filter, true).getValue())));
  }

  @Test
	public final void localVarWithValueJoinTest() {
    String varName = "variableName";
    smg1.addStackFrame(FUNCTION_DECLARATION);
    smg2.addStackFrame(FUNCTION_DECLARATION);
    addLocalWithValueToBoth(varName);
    SMGJoin join = new SMGJoin(smg1, smg2);
    Assert.assertTrue(join.isDefined());
    Assert.assertEquals(join.getStatus(), SMGJoinStatus.EQUAL);

    ReadableSMG resultSMG = join.getJointSMG();
    Assert.assertTrue(resultSMG.getStackFrames().getFirst().containsVariable(varName));
    assertObjectCounts(resultSMG, 0, 1, 1);

    SMGObject global = resultSMG.getStackFrames().getFirst().getVariable(varName);
    SMGEdgeHasValueFilter filter = SMGEdgeHasValueFilter.objectFilter(global).filterAtOffset(0);

    Assert.assertTrue(resultSMG.getValues().contains(Integer.valueOf(resultSMG.getUniqueHV(filter, true).getValue())));
  }

  private void joinUpdateUnit(final SMGJoinStatus firstOperand, final SMGJoinStatus forLe, final SMGJoinStatus forRe) {
    Assert.assertEquals(firstOperand, SMGJoinStatus.updateStatus(firstOperand, SMGJoinStatus.EQUAL));
    Assert.assertEquals(forLe, SMGJoinStatus.updateStatus(firstOperand, SMGJoinStatus.LEFT_ENTAIL));
    Assert.assertEquals(forRe, SMGJoinStatus.updateStatus(firstOperand, SMGJoinStatus.RIGHT_ENTAIL));
    Assert.assertEquals(SMGJoinStatus.INCOMPARABLE,
        SMGJoinStatus.updateStatus(firstOperand, SMGJoinStatus.INCOMPARABLE));
  }

  @Test
	public final void joinUpdateTest() {
    joinUpdateUnit(SMGJoinStatus.EQUAL, SMGJoinStatus.LEFT_ENTAIL,
        SMGJoinStatus.RIGHT_ENTAIL);
    joinUpdateUnit(SMGJoinStatus.LEFT_ENTAIL, SMGJoinStatus.LEFT_ENTAIL,
        SMGJoinStatus.INCOMPARABLE);
    joinUpdateUnit(SMGJoinStatus.RIGHT_ENTAIL, SMGJoinStatus.INCOMPARABLE,
        SMGJoinStatus.RIGHT_ENTAIL);
    joinUpdateUnit(SMGJoinStatus.INCOMPARABLE, SMGJoinStatus.INCOMPARABLE,
        SMGJoinStatus.INCOMPARABLE);
  }
}
