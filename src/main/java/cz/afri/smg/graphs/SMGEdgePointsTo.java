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

import cz.afri.smg.objects.SMGObject;

public class SMGEdgePointsTo extends SMGEdge {
  private final int offset;

  public SMGEdgePointsTo(final int pValue, final SMGObject pObject, final int pOffset) {
    super(pValue, pObject);
    offset = pOffset;
  }
  @Override
  public final String toString() {
    return getValue() + "->" + getObject().getLabel() + "+" + offset + 'b';
  }

  public final int getOffset() {
    return offset;
  }

  @Override
  public final boolean isConsistentWith(final SMGEdge other) {
    /*
     * different value- > different place
     * same value -> same place
     */
    if (!(other instanceof SMGEdgePointsTo)) {
      return false;
    }

    if (getValue() != other.getValue()) {
      if (offset == ((SMGEdgePointsTo) other).offset && getObject() == other.getObject()) {
        return false;
      }
    } else {
      if (offset != ((SMGEdgePointsTo) other).offset || getObject() != other.getObject()) {
        return false;
      }
    }

    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public final int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + offset;
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
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

    SMGEdgePointsTo other = (SMGEdgePointsTo) obj;

    if (offset != other.offset) {
      return false;
    }

    return true;
  }
}