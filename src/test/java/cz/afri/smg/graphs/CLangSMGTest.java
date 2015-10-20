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
package cz.afri.smg.graphs;

import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.objects.SMGRegion;
import cz.afri.smg.types.CFunctionDeclaration;
import cz.afri.smg.types.CFunctionType;
import cz.afri.smg.types.CIdExpression;
import cz.afri.smg.types.CParameterDeclaration;
import cz.afri.smg.types.CType;

public class CLangSMGTest {
  private static final CFunctionType FUNCTION_TYPE = CFunctionType.createSimpleFunctionType(CType.getIntType());
  private static final ImmutableList<CParameterDeclaration> FUNCTION_PARAMS = ImmutableList.<CParameterDeclaration>of();
  private static final CFunctionDeclaration FUNCTION_DECLARATION = new CFunctionDeclaration(FUNCTION_TYPE, "foo",
                                                                                           FUNCTION_PARAMS);
  private CLangStackFrame sf;

  private static final CIdExpression ID_EXPRESSION = new CIdExpression("label");
  private static final int SIZE8 = 8;
  private static final int SIZE16 = 16;
  private static final int SIZE32 = 32;

  private static final int OFFSET0 = 0;
  private static final CType TYPE8 = CType.createTypeWithLength(SIZE8);
  private static final CType TYPE16 = CType.createTypeWithLength(SIZE16);
  private static final CType TYPE32 = CType.createTypeWithLength(SIZE32);

  private static CLangSMG getNewCLangSMG64() {
    return new CLangSMG();
  }

  @Before
  public final void setUp() {
    sf = new CLangStackFrame(FUNCTION_DECLARATION);
    CLangSMG.setPerformChecks(true);
  }

  @Test
  public final void cLangSMGConstructorTest() {
    CLangSMG smg = getNewCLangSMG64();

    Assert.assertEquals(0, smg.getStackFrames().size());
    Assert.assertEquals(1, smg.getHeapObjects().size());
    Assert.assertEquals(0, smg.getGlobalObjects().size());

    SMGRegion obj1 = new SMGRegion(SIZE8, "obj1");
    SMGRegion obj2 =  smg.addGlobalVariable(TYPE8, "obj2");

    Integer val1 = Integer.valueOf(1);
    Integer val2 = Integer.valueOf(2);

    SMGEdgePointsTo pt = new SMGEdgePointsTo(val1, obj1, 0);
    SMGEdgeHasValue hv = new SMGEdgeHasValue(CType.getIntType(), 0, obj2, val2.intValue());

    smg.addValue(val1);
    smg.addValue(val2);
    smg.addHeapObject(obj1);
    smg.addPointsToEdge(pt);
    smg.addHasValueEdge(hv);
    Assert.assertTrue(CLangSMGConsistencyVerifier.verifyCLangSMG(smg));

    // Copy constructor

    CLangSMG smgCopy = new CLangSMG(smg);
    Assert.assertTrue(CLangSMGConsistencyVerifier.verifyCLangSMG(smgCopy));

    Assert.assertEquals(0, smgCopy.getStackFrames().size());
    Assert.assertEquals(2, smgCopy.getHeapObjects().size());
    Assert.assertEquals(1, smgCopy.getGlobalObjects().size());

    Assert.assertEquals(obj1, smgCopy.getObjectPointedBy(val1));

    SMGEdgeHasValueFilter filter = SMGEdgeHasValueFilter.objectFilter(obj2);
    Assert.assertEquals(hv, smgCopy.getUniqueHV(filter, true));
  }

  @Test
  public final void cLangSMGaddHeapObjectTest() {
    CLangSMG smg = getNewCLangSMG64();
    SMGRegion obj1 = new SMGRegion(SIZE8, "label");
    SMGRegion obj2 = new SMGRegion(SIZE8, "label");

    smg.addHeapObject(obj1);
    Assert.assertTrue(CLangSMGConsistencyVerifier.verifyCLangSMG(smg));
    Set<SMGObject> heapObjs = smg.getHeapObjects();

    Assert.assertTrue(heapObjs.contains(obj1));
    Assert.assertFalse(heapObjs.contains(obj2));
    Assert.assertTrue(heapObjs.size() == 2);

    smg.addHeapObject(obj2);
    Assert.assertTrue(CLangSMGConsistencyVerifier.verifyCLangSMG(smg));
    heapObjs = smg.getHeapObjects();

    Assert.assertTrue(heapObjs.contains(obj1));
    Assert.assertTrue(heapObjs.contains(obj2));
    final int expectedSize = 3;
    Assert.assertEquals(heapObjs.size(), expectedSize);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void cLangSMGaddHeapObjectTwiceTest() {
    CLangSMG smg = getNewCLangSMG64();
    SMGRegion obj = new SMGRegion(SIZE8, "label");

    smg.addHeapObject(obj);
    smg.addHeapObject(obj);
  }

  @Test
  public final void cLangSMGaddHeapObjectTwiceWithoutChecksTest() {
    CLangSMG.setPerformChecks(false);
    CLangSMG smg = getNewCLangSMG64();
    SMGRegion obj = new SMGRegion(SIZE8, "label");

    smg.addHeapObject(obj);
    smg.addHeapObject(obj);
    Assert.assertTrue("Asserting the test finished without exception", true);
  }

  @Test
  public final void cLangSMGaddGlobalObjectTest() {
    CLangSMG smg = getNewCLangSMG64();
    SMGRegion obj1 = smg.addGlobalVariable(TYPE8, "label");

    Map<String, SMGRegion> globalObjects = smg.getGlobalObjects();

    Assert.assertTrue(CLangSMGConsistencyVerifier.verifyCLangSMG(smg));
    Assert.assertEquals(globalObjects.size(), 1);
    Assert.assertTrue(globalObjects.values().contains(obj1));

    SMGRegion obj2 = smg.addGlobalVariable(TYPE8, "another_label");
    globalObjects = smg.getGlobalObjects();

    Assert.assertTrue(CLangSMGConsistencyVerifier.verifyCLangSMG(smg));
    Assert.assertEquals(globalObjects.size(), 2);
    Assert.assertTrue(globalObjects.values().contains(obj1));
    Assert.assertTrue(globalObjects.values().contains(obj2));
  }

  @Test(expected = IllegalArgumentException.class)
  public final void cLangSMGaddGlobalObjectTwiceTest() {
    CLangSMG smg = getNewCLangSMG64();

    smg.addGlobalVariable(TYPE8, "label");
    smg.addGlobalVariable(TYPE8, "label");
  }

  @Test(expected = IllegalArgumentException.class)
  public final void cLangSMGaddGlobalObjectWithSameLabelTest() {
    CLangSMG smg = getNewCLangSMG64();
    smg.addGlobalVariable(TYPE8, "label");
    smg.addGlobalVariable(TYPE16, "label");
  }

  @Test
  public final void cLangSMGaddStackObjectTest() {
    CLangSMG smg = getNewCLangSMG64();

    smg.addStackFrame(sf.getFunctionDeclaration());

    SMGRegion obj1 = smg.addLocalVariable(TYPE8, "label");
    CLangStackFrame currentFrame = smg.getStackFrames().peek();

    Assert.assertTrue(CLangSMGConsistencyVerifier.verifyCLangSMG(smg));
    Assert.assertEquals(currentFrame.getVariable("label"), obj1);
    Assert.assertEquals(currentFrame.getVariables().size(), 1);

    SMGRegion diffobj1 = smg.addLocalVariable(TYPE8, "difflabel");
    currentFrame = smg.getStackFrames().peek();

    Assert.assertTrue(CLangSMGConsistencyVerifier.verifyCLangSMG(smg));
    Assert.assertEquals(currentFrame.getVariable("label"), obj1);
    Assert.assertEquals(currentFrame.getVariable("difflabel"), diffobj1);
    Assert.assertEquals(currentFrame.getVariables().size(), 2);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void cLangSMGaddStackObjectTwiceTest() {
    CLangSMG smg = getNewCLangSMG64();

    smg.addStackFrame(sf.getFunctionDeclaration());

    smg.addLocalVariable(TYPE8, "label");
    smg.addLocalVariable(TYPE8, "label");
  }

  @Test
  public final void cLangSMGgetObjectForVisibleVariableTest() {
    CLangSMG smg = getNewCLangSMG64();

    SMGRegion obj3 = smg.addGlobalVariable(TYPE32, "label");
    Assert.assertEquals(smg.getObjectForVisibleVariable(ID_EXPRESSION.getName()), obj3);

    smg.addStackFrame(sf.getFunctionDeclaration());
    SMGRegion obj1 = smg.addLocalVariable(TYPE8, "label");
    Assert.assertEquals(smg.getObjectForVisibleVariable(ID_EXPRESSION.getName()), obj1);

    smg.addStackFrame(sf.getFunctionDeclaration());
    SMGRegion obj2 = smg.addLocalVariable(TYPE16, "label");
    Assert.assertEquals(smg.getObjectForVisibleVariable(ID_EXPRESSION.getName()), obj2);
    Assert.assertNotEquals(smg.getObjectForVisibleVariable(ID_EXPRESSION.getName()), obj1);

    smg.addStackFrame(sf.getFunctionDeclaration());
    Assert.assertEquals(smg.getObjectForVisibleVariable(ID_EXPRESSION.getName()), obj3);
    Assert.assertNotEquals(smg.getObjectForVisibleVariable(ID_EXPRESSION.getName()), obj2);
  }

  @Test
  public final void cLangSMGgetStackFramesTest() {
    CLangSMG smg = getNewCLangSMG64();
    Assert.assertEquals(smg.getStackFrames().size(), 0);

    smg.addStackFrame(sf.getFunctionDeclaration());
    smg.addLocalVariable(TYPE8, "frame1_1");
    smg.addLocalVariable(TYPE8, "frame1_2");
    smg.addLocalVariable(TYPE8, "frame1_3");
    Assert.assertEquals(smg.getStackFrames().size(), 1);

    final int expectedVariableCount = 3;
    Assert.assertEquals(smg.getStackFrames().peek().getVariables().size(), expectedVariableCount);

    smg.addStackFrame(sf.getFunctionDeclaration());
    smg.addLocalVariable(TYPE8, "frame2_1");
    smg.addLocalVariable(TYPE8, "frame2_2");
    Assert.assertEquals(smg.getStackFrames().size(), 2);
    Assert.assertEquals(smg.getStackFrames().peek().getVariables().size(), 2);

    smg.addStackFrame(sf.getFunctionDeclaration());
    smg.addLocalVariable(TYPE8, "frame3_1");
    final int expectedStackSize = 3;
    Assert.assertEquals(smg.getStackFrames().size(), expectedStackSize);
    Assert.assertEquals(smg.getStackFrames().peek().getVariables().size(), 1);

    smg.addStackFrame(sf.getFunctionDeclaration());
    Assert.assertEquals(smg.getStackFrames().size(), expectedStackSize + 1);
    Assert.assertEquals(smg.getStackFrames().peek().getVariables().size(), 0);
  }

  @Test
  public final void cLangSMGgetHeapObjectsTest() {
    CLangSMG smg = getNewCLangSMG64();
    Assert.assertEquals(smg.getHeapObjects().size(), 1);

    smg.addHeapObject(new SMGRegion(SIZE8, "heap1"));
    Assert.assertEquals(smg.getHeapObjects().size(), 2);

    smg.addHeapObject(new SMGRegion(SIZE8, "heap2"));
    smg.addHeapObject(new SMGRegion(SIZE8, "heap3"));
    final int expectedHeapSize = 4;
    Assert.assertEquals(smg.getHeapObjects().size(), expectedHeapSize);
  }

  @Test
  public final void cLangSMGgetGlobalObjectsTest() {
    CLangSMG smg = getNewCLangSMG64();
    Assert.assertEquals(smg.getGlobalObjects().size(), 0);

    smg.addGlobalVariable(TYPE8, "heap1");
    Assert.assertEquals(smg.getGlobalObjects().size(), 1);

    smg.addGlobalVariable(TYPE8, "heap2");
    smg.addGlobalVariable(TYPE8, "heap3");

    final int expectedGlobalCount = 3;
    Assert.assertEquals(smg.getGlobalObjects().size(), expectedGlobalCount);
  }

  @Test
  public final void cLangSMGmemoryLeaksTest() {
    CLangSMG smg = getNewCLangSMG64();

    Assert.assertFalse(smg.hasMemoryLeaks());
    smg.setMemoryLeak();
    Assert.assertTrue(smg.hasMemoryLeaks());
  }

  @Test(expected = IllegalStateException.class)
  public final void consistencyViolationDisjunctnessTest1() {
    CLangSMG smg = getNewCLangSMG64();

    Assert.assertTrue(CLangSMGConsistencyVerifier.verifyCLangSMG(smg));
    SMGRegion obj = smg.addGlobalVariable(TYPE8, "label");
    Assert.assertTrue(CLangSMGConsistencyVerifier.verifyCLangSMG(smg));
    smg.addHeapObject(obj);
    CLangSMGConsistencyVerifier.verifyCLangSMG(smg);
  }

  @Test(expected = IllegalStateException.class)
  public final void consistencyViolationDisjunctnessTest2() {
    CLangSMG smg = getNewCLangSMG64();
    smg.addStackFrame(sf.getFunctionDeclaration());

    Assert.assertTrue(CLangSMGConsistencyVerifier.verifyCLangSMG(smg));
    SMGRegion obj = smg.addLocalVariable(TYPE8, "label");
    Assert.assertTrue(CLangSMGConsistencyVerifier.verifyCLangSMG(smg));
    smg.addHeapObject(obj);

    CLangSMGConsistencyVerifier.verifyCLangSMG(smg);
  }

  @Test(expected = IllegalStateException.class)
  public final void consistencyViolationUnionTest() {
    CLangSMG smg = getNewCLangSMG64();
    Assert.assertTrue(CLangSMGConsistencyVerifier.verifyCLangSMG(smg));
    SMGRegion heapObj = new SMGRegion(SIZE8, "heap_object");
    SMGRegion dummyObj = new SMGRegion(SIZE8, "dummy_object");

    smg.addStackFrame(sf.getFunctionDeclaration());
    Assert.assertTrue(CLangSMGConsistencyVerifier.verifyCLangSMG(smg));
    smg.addLocalVariable(TYPE8, "stack_variable");
    Assert.assertTrue(CLangSMGConsistencyVerifier.verifyCLangSMG(smg));
    smg.addGlobalVariable(TYPE8, "global_variable");
    Assert.assertTrue(CLangSMGConsistencyVerifier.verifyCLangSMG(smg));
    smg.addHeapObject(heapObj);
    Assert.assertTrue(CLangSMGConsistencyVerifier.verifyCLangSMG(smg));
    smg.addObject(dummyObj);
    CLangSMGConsistencyVerifier.verifyCLangSMG(smg);
  }

  @Test
  public final void consistencyViolationNullTest() {

    CLangSMG smg = getNewCLangSMG64();

    smg = getNewCLangSMG64();
    SMGObject nullObject = smg.getHeapObjects().iterator().next();
    Integer value = SMGValueFactory.getNewValue();
    SMGEdgeHasValue edge = new SMGEdgeHasValue(mock(CType.class), OFFSET0, nullObject, value);

    smg.addValue(value);
    smg.addHasValueEdge(edge);
    Assert.assertFalse(CLangSMGConsistencyVerifier.verifyCLangSMG(smg));
  }

  /**
   * Two objects with same label (variable name) in different frames are not inconsistent
   */
  @Test
  public final void consistencyViolationStackNamespaceTest2() {
    CLangSMG smg = getNewCLangSMG64();

    smg.addStackFrame(sf.getFunctionDeclaration());
    smg.addLocalVariable(TYPE8, "label");
    smg.addStackFrame(sf.getFunctionDeclaration());
    smg.addLocalVariable(TYPE16, "label");
    Assert.assertTrue(CLangSMGConsistencyVerifier.verifyCLangSMG(smg));
  }

  /**
   * Two objects with same label (variable name) on stack and global namespace are not inconsistent
   */
  @Test
  public final void consistencyViolationStackNamespaceTest3() {
    CLangSMG smg = getNewCLangSMG64();

    smg.addGlobalVariable(TYPE8, "label");
    smg.addStackFrame(sf.getFunctionDeclaration());
    smg.addLocalVariable(TYPE16, "label");
    Assert.assertTrue(CLangSMGConsistencyVerifier.verifyCLangSMG(smg));
  }
}
