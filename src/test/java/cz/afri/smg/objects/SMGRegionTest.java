/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2013  Dirk Beyer
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
package cz.afri.smg.objects;

import org.junit.Assert;
import org.junit.Test;

public class SMGRegionTest {

  private static final int SIZE8 = 8;
  private static final int SIZE10 = 10;

  private final SMGRegion region8 = new SMGRegion(8, "region");

  @Test
  public final void testToString() {
    Assert.assertFalse(region8.toString().contains("@"));
  }

  @Test
  public final void testIsAbstract() {
    Assert.assertFalse(region8.isAbstract());
  }

  @Test
  public final void testJoin() {
    SMGRegion regionSame = new SMGRegion(SIZE8, "region");
    SMGObject objectJoint = region8.join(regionSame);
    Assert.assertTrue(objectJoint instanceof SMGRegion);
    SMGRegion regionJoint = (SMGRegion) objectJoint;

    Assert.assertEquals(SIZE8, regionJoint.getSize());
    Assert.assertEquals("region", regionJoint.getLabel());
  }

  @Test(expected = UnsupportedOperationException.class)
  public final void testJoinDiffSize() {
    SMGRegion regionDiff = new SMGRegion(SIZE10, "region");
    region8.join(regionDiff);
  }

  @Test
	public final void testPropertiesEqual() {
    SMGRegion one = new SMGRegion(SIZE8, "region");
    SMGRegion two = new SMGRegion(SIZE8, "region");
    SMGRegion three = new SMGRegion(SIZE10, "region");
    SMGRegion four = new SMGRegion(SIZE8, "REGION");

    Assert.assertTrue(one.propertiesEqual(one));
    Assert.assertTrue(one.propertiesEqual(two));
    Assert.assertFalse(one.propertiesEqual(three));
    Assert.assertFalse(one.propertiesEqual(four));
    Assert.assertFalse(one.propertiesEqual(null));
  }

  @Test
	public final void testObjectVisitor() {
    SMGObjectVisitor visitor = new SMGObjectVisitor() {

      @Override
      public void visit(final SMGRegion pObject) {
        Assert.assertTrue(true);
      }
    };
    region8.accept(visitor);
  }

  static class MockAbstraction extends SMGAbstractObject {
    protected MockAbstraction(final SMGRegion pPrototype) {
      super(pPrototype);
    }

    @Override
    public boolean matchGenericShape(final SMGAbstractObject pOther) {
      // TODO Auto-generated method stub
      Assert.fail();
      return false;
    }

    @Override
    public boolean matchSpecificShape(final SMGAbstractObject pOther) {
      // TODO Auto-generated method stub
      Assert.fail();
      return false;
    }

    @Override
    public SMGObject join(final SMGObject pOther) {
      return this;
    }

    @Override
    public void accept(final SMGObjectVisitor pVisitor) {
      pVisitor.visit(this);

    }

    @Override
    public boolean isMoreGeneral(final SMGObject pOther) {
      return false;
    }
  }

  @Test
	public final void testAbstractJoin() {
    SMGObject object = new MockAbstraction(region8);
    Assert.assertSame(object, region8.join(object));
  }

  @Test
	public final void testIsMoreGeneral() {
    Assert.assertFalse(region8.isMoreGeneral(region8));
  }
}
