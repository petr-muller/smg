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

import cz.afri.smg.SMGConcretisation;
import cz.afri.smg.objects.SMGAbstractObject;
import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.objects.SMGObjectVisitor;
import cz.afri.smg.objects.SMGRegion;


public class SimpleBinaryTree extends SMGAbstractObject {
  public SimpleBinaryTree(final SMGRegion pPrototype, final int lOffset, final int rOffset, final int pDepth) {
    super(pPrototype.getSize(), "Simple Binary Tree");
    depth = pDepth;
  }

  private final int depth;

  public final int getDepth() {
    return depth;
  }

  @Override
  public final boolean matchGenericShape(final SMGAbstractObject pOther) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public final boolean matchSpecificShape(final SMGAbstractObject pOther) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  protected final SMGConcretisation createConcretisation() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public final void accept(final SMGObjectVisitor pVisitor) {
    pVisitor.visit(this);
  }

  @Override
  public final boolean isMoreGeneral(final SMGObject pOther) {
    return false;
  }

  @Override
  public final SMGObject join(final SMGObject pOther) {
    return null;
  }
}
