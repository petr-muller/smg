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
package cz.afri.smg.objects;

import org.junit.Assert;
import org.junit.Test;


public class SMGObjectTest {
  private static class TestingObject extends SMGObject {

    protected TestingObject(final int pSize, final String pLabel) {
      super(pSize, pLabel);
    }

    protected TestingObject(final TestingObject pOther) {
      super(pOther);
    }

    @Override
    public boolean isAbstract() {
      return false;
    }

    @Override
    public void accept(final SMGObjectVisitor pVisitor) {
      pVisitor.visit(this);
    }

    @Override
    public boolean isMoreGeneral(final SMGObject pOther) {
      return false;
    }

    @Override
    public SMGObject join(final SMGObject pOther) {
      return pOther;
    }
  }

  private static final int SIZE8 = 8;
	private static final int SIZE12 = 12;
	

  private final TestingObject object8 = new TestingObject(8, "label");
  private final TestingObject object12 = new TestingObject(12, "another label");

  @Test
	public final void testGetNullObject() {
    SMGObject nullObject = SMGObject.getNullObject();
    Assert.assertFalse(nullObject.notNull());
    Assert.assertTrue(object8.notNull());
    Assert.assertFalse(nullObject.isAbstract());
    Assert.assertFalse(nullObject.isMoreGeneral(object8));
    Assert.assertFalse(nullObject.isMoreGeneral(nullObject));
    Assert.assertSame(nullObject, nullObject.join(nullObject));
    Assert.assertSame(object8, nullObject.join(object8));
  }

  @Test
	public final void testSMGObjectIntString() {
    Assert.assertEquals(SIZE8, object8.getSize());
    Assert.assertEquals("label", object8.getLabel());
    Assert.assertEquals(SIZE12, object12.getSize());
    Assert.assertEquals("another label", object12.getLabel());
    SMGObject object12copy = new TestingObject(object12);
    Assert.assertEquals(SIZE12, object12copy.getSize());
    Assert.assertEquals("another label", object12copy.getLabel());
  }

  @Test
	public final void testVisitorOnNull() {
    SMGObject nullObject = SMGObject.getNullObject();
    SMGObjectVisitor visitor = new SMGObjectVisitor() {
      @Override
      public void visit(final SMGObject pObject) {
        Assert.assertFalse(pObject.notNull());
      }
    };
    nullObject.accept(visitor);
  }
}
