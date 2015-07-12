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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Predicate;

import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.types.CType;

public class SMGEdgeHasValueFilter {

  public static SMGEdgeHasValueFilter objectFilter(final SMGObject pObject) {
    SMGEdgeHasValueFilter filter = new SMGEdgeHasValueFilter();
    filter.filterByObject(pObject);

    return filter;
  }

  private SMGObject object = null;

  private Integer value = null;
  private boolean valueComplement = false;
  private Integer offset = null;
  private CType type = null;

  public final SMGEdgeHasValueFilter filterByObject(final SMGObject pObject) {
    object = pObject;
    return this;
  }

  public final SMGEdgeHasValueFilter filterHavingValue(final Integer pValue) {
    value = pValue;
    valueComplement = false;
    return this;
  }

  public final SMGEdgeHasValueFilter filterNotHavingValue(final Integer pValue) {
    value = pValue;
    valueComplement = true;
    return this;
  }

  public final SMGEdgeHasValueFilter filterAtOffset(final Integer pOffset) {
    offset = pOffset;
    return this;
  }

  public final SMGEdgeHasValueFilter filterByType(final CType pType) {
    type = pType;
    return this;
  }

  public final boolean holdsFor(final SMGEdgeHasValue pEdge) {
    if (object != null && object != pEdge.getObject()) {
      return false;
    }

    if (value != null) {
      if (valueComplement && pEdge.getValue() == value) {
        return false;
      } else if ((!valueComplement) && pEdge.getValue() != value) {
        return false;
      }
    }

    if (offset != null && offset != pEdge.getOffset()) {
      return false;
    }

    if (type != null && !type.equals(pEdge.getType())) {
      return false;
    }

    return true;
  }

  public final Set<SMGEdgeHasValue> filterSet(final Set<SMGEdgeHasValue> pEdges) {
    Set<SMGEdgeHasValue> returnSet = new HashSet<>();
    for (SMGEdgeHasValue edge : pEdges) {
      if (holdsFor(edge)) {
        returnSet.add(edge);
      }
    }
    return Collections.unmodifiableSet(returnSet);
  }

  public final Predicate<SMGEdgeHasValue> asPredicate() {
    return new Predicate<SMGEdgeHasValue>() {
      @Override
      public boolean apply(final SMGEdgeHasValue pEdge) {
        return SMGEdgeHasValueFilter.this.holdsFor(pEdge);
      }
    };
  }
}