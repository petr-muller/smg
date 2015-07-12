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
package cz.afri.smg.graphs;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.objects.SMGRegion;
import cz.afri.smg.types.CFunctionDeclaration;
import cz.afri.smg.types.CFunctionType;
import cz.afri.smg.types.CParameterDeclaration;
import cz.afri.smg.types.CType;


public class CLangStackFrameTest {
  private static final CFunctionType FUNCTION_TYPE = CFunctionType.createSimpleFunctionType(CType.getIntType());
  private static final ImmutableList<CParameterDeclaration> FUNCTION_PARAMS = ImmutableList.<CParameterDeclaration>of();
  private static final CFunctionDeclaration FUNCTION_DECLARATION = new CFunctionDeclaration(FUNCTION_TYPE, "foo",
                                                                                            FUNCTION_PARAMS);
	private static final int SIZE8 = 8;
	private static final int SIZE16 = 16;
  private CLangStackFrame sf;

  @Before
	public final void setUp() {

    sf = new CLangStackFrame(FUNCTION_DECLARATION);
  }

  @Test
	public final void cLangStackFrameConstructorTest() {

    // Normal constructor
    Map<String, SMGRegion> variables = sf.getVariables();
    Assert.assertEquals("CLangStackFrame contains no variables after creation",
                        variables.size(), 0);
    Assert.assertFalse(sf.containsVariable("foo"));

    // Copy constructor
    CLangStackFrame sfCopy = new CLangStackFrame(sf);
    variables = sfCopy.getVariables();
    Assert.assertEquals("Empty CLangStackFrame contains no variables after copying",
        variables.size(), 0);
    Assert.assertFalse(sfCopy.containsVariable("foo"));
  }

  @Test
	public final void cLangStackFrameAddVariableTest() {
    sf.addStackVariable("fooVar", new SMGRegion(SIZE8, "fooVarObject"));
    Assert.assertTrue("Added variable is present", sf.containsVariable("fooVar"));

    Map<String, SMGRegion> variables = sf.getVariables();
    Assert.assertEquals("Variables set is nonempty after variable addition",
                        variables.size(), 1);
    SMGObject smgObject = variables.get("fooVar");
    Assert.assertEquals("Added variable present in variable map", smgObject.getLabel(), "fooVarObject");
    Assert.assertEquals("Added variable present in variable map", smgObject.getSize(), SIZE8);

    smgObject = null;
    smgObject = sf.getVariable("fooVar");
    Assert.assertEquals("Correct variable is returned: label", smgObject.getLabel(), "fooVarObject");
    Assert.assertEquals("Correct variable is returned: size", smgObject.getSize(), SIZE8);
  }

  @Test
	public final void cLangFrameGetObjectsTest() {
    Set<SMGObject> objects = sf.getAllObjects();
    // Test that there is an return value object at
    Assert.assertEquals(1, objects.size());

    sf.addStackVariable("fooVar", new SMGRegion(SIZE8, "fooVarObject"));
    objects = sf.getAllObjects();
    Assert.assertEquals(2, objects.size());
  }

  //TODO: Test void functions
  @Test
	public final void cLangFrameReturnValueTest() {
    SMGObject retval = sf.getReturnObject();
    Assert.assertEquals(CType.getIntType().getSize(), retval.getSize());
  }

  @Test(expected = IllegalArgumentException.class)
	public final void cLangStackFrameAddVariableTwiceTest() {
    sf.addStackVariable("fooVar", new SMGRegion(SIZE8, "fooVarObject"));
    sf.addStackVariable("fooVar", new SMGRegion(SIZE16, "newFooVarObject"));
  }

  @Test(expected = NoSuchElementException.class)
	public final void cLangStackFrameMissingVariableTest() {
    Assert.assertFalse("Non-added variable is not present", sf.containsVariable("fooVaz"));

    SMGObject smgObject = sf.getVariable("fooVaz");
    smgObject.getLabel(); // Avoid dead store warning
  }

  @Test
	public final void cLangStackFrameFunctionTest() {
    CFunctionDeclaration fd = sf.getFunctionDeclaration();
    Assert.assertNotNull(fd);
    Assert.assertEquals("Correct function is returned", "foo", fd.getName());
  }
}
