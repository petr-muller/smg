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
package cz.afri.smg.objects.sll;

import cz.afri.smg.abstraction.SMGConcretisation;
import cz.afri.smg.objects.SMGAbstractObject;
import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.objects.SMGObjectVisitor;
import cz.afri.smg.objects.SMGRegion;

public final class SMGSingleLinkedList extends SMGAbstractObject {
  private int length;

  // TODO: Binding is likely to be more complicated later
  private int bindingOffset;

  public SMGSingleLinkedList(final SMGRegion pPrototype, final int pOffset, final int pLength) {
    super(pPrototype.getSize(), "SLL");
    bindingOffset = pOffset;
    length = pLength;
  }

  public SMGSingleLinkedList(final SMGSingleLinkedList pOriginal) {
    super(pOriginal);
    bindingOffset = pOriginal.bindingOffset;
    length = pOriginal.length;
  }

  // TODO: Abstract interface???
  public int getLength() {
    return length;
  }

  public int getOffset() {
    return bindingOffset;
  }

  public int addLength(final int pLen) {
    length += pLen;
    return length;
  }

  @Override
  public String toString() {
    return "SLL(size=" + getSize() + ", bindingOffset=" + bindingOffset + ", len=" + length + ")";
  }

  @Override
  public void accept(final SMGObjectVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public boolean matchGenericShape(final SMGAbstractObject pOther) {
    return pOther instanceof SMGSingleLinkedList;
  }

  @Override
  public boolean matchSpecificShape(final SMGAbstractObject pOther) {
    if (!matchGenericShape(pOther)) {
      return false;
    }
    SMGSingleLinkedList otherSLL = (SMGSingleLinkedList) pOther;
    return (bindingOffset == otherSLL.bindingOffset) && (getSize() == otherSLL.getSize());
  }

  @Override
  protected SMGConcretisation createConcretisation() {
    return new SMGSingleLinkedListConcretisation(this);
  }

  @Override
  public boolean isMoreGeneral(final SMGObject pOther) {
    if (!pOther.isAbstract()) {
      return true;
    }

    if (!matchSpecificShape((SMGAbstractObject) pOther)) {
      throw new IllegalArgumentException("isMoreGeneral called on incompatible abstract objects");
    }
    return length < ((SMGSingleLinkedList) pOther).length;
  }

  @Override
  public SMGSingleLinkedList join(final SMGObject pOther) {
    if (!pOther.isAbstract()) {
      return new SMGSingleLinkedList(this);
    }

    if (matchSpecificShape((SMGAbstractObject) pOther)) {
      SMGSingleLinkedList otherSll = (SMGSingleLinkedList) pOther;
      if (getLength() < otherSll.getLength()) {
        return new SMGSingleLinkedList(this);
      } else {
        return new SMGSingleLinkedList(otherSll);
      }
    }

    throw new UnsupportedOperationException("join() called on incompatible abstract objects");
  }

}
