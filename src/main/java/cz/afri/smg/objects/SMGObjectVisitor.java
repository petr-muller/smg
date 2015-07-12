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
package cz.afri.smg.objects;

import cz.afri.smg.objects.sll.SMGSingleLinkedList;
import cz.afri.smg.objects.tree.SimpleBinaryTree;

public abstract class SMGObjectVisitor {

  private void visitDefault(final SMGObject pObject) {
    String message = "This visitor cannot handle objects of class [" + pObject.getClass().getCanonicalName() + "]";
    throw new UnsupportedOperationException(message);
  }

  @SuppressWarnings("checkstyle:designforextension")
  public void visit(final SMGObject pObject) {
    visitDefault(pObject);
  }

  @SuppressWarnings("checkstyle:designforextension")
  public void visit(final SMGRegion pObject) {
    visitDefault(pObject);
  }

  @SuppressWarnings("checkstyle:designforextension")
  public void visit(final SMGSingleLinkedList pObject) {
    visitDefault(pObject);
  }

  @SuppressWarnings("checkstyle:designforextension")
  public void visit(final SimpleBinaryTree pObject) {
    visitDefault(pObject);
  }


}