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

import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.types.CType;

public class SMGEdgeHasValue extends SMGEdge {
  private final CType type;
  private final int offset;

  public SMGEdgeHasValue(final CType pType, final int pOffset, final SMGObject pObject, final int pValue) {
    super(pValue, pObject);
    type = pType;
    offset = pOffset;
  }

  public SMGEdgeHasValue(final int pSizeInBytes, final int pOffset, final SMGObject pObject, final int pValue) {
    super(pValue, pObject);
    type = CType.createTypeWithLength(pSizeInBytes);
    offset = pOffset;
  }

  @Override
  public final String toString() {
    return "sizeof(" + type.toString() + ")b @ " + getObject().getLabel() + "+" + offset + "b has value " + getValue();
  }

  public final int getOffset() {
    return offset;
  }

  public final CType getType() {
    return type;
  }

  public final int getSizeInBytes() {
    return type.getSize();
  }

  @Override
  public final boolean isConsistentWith(final SMGEdge other) {
    if (!(other instanceof SMGEdgeHasValue)) {
      return false;
    }

    if ((getObject() == other.getObject()) &&
        (offset == ((SMGEdgeHasValue) other).offset) &&
        (type == ((SMGEdgeHasValue) other).type)) {
      return (getValue() == other.getValue());
    }

    return true;
  }

  public final boolean overlapsWith(final SMGEdgeHasValue other) {
    if (getObject() != other.getObject()) {
      String message = "Call of overlapsWith() on Has-Value edges pair not originating from the same object";
      throw new IllegalArgumentException(message);
    }

    int otStart = other.getOffset();

    int otEnd = otStart + other.getType().getSize();

    return overlapsWith(otStart, otEnd);
  }

  public final boolean overlapsWith(final int pOtStart, final int pOtEnd) {

    int myStart = offset;

    int myEnd = myStart + type.getSize();

    if (myStart < pOtStart) {
      return (myEnd > pOtStart);

    } else if (pOtStart < myStart) {
      return (pOtEnd > myStart);
    }

    // Start offsets are equal, always overlap
    return true;
  }

  public final boolean isCompatibleField(final SMGEdgeHasValue other) {
    return type.equals(other.type) && (offset == other.offset);
  }

  public final boolean isCompatibleFieldOnSameObject(final SMGEdgeHasValue other) {
    return type.getSize() == other.type.getSize() && (offset == other.offset) && getObject() == other.getObject();
  }

  @Override
  @SuppressWarnings("checkstyle:avoidinlineconditionals")
  public final int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + offset;
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  @Override
  public final boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SMGEdgeHasValue other = (SMGEdgeHasValue) obj;
    if (offset != other.offset) {
      return false;
    }
    if (type == null) {
      if (other.type != null) {
        return false;
      }
    } else if (!type.equals(other.type)) {
      return false;
    }
    return true;
  }
}