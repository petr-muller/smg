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

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Map;
import java.util.Set;

import cz.afri.smg.graphs.SMGValues.SMGExplicitValue;
import cz.afri.smg.graphs.SMGValues.SMGKnownSymValue;
import cz.afri.smg.graphs.SMGValues.SMGSymbolicValue;
import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.objects.SMGRegion;
import cz.afri.smg.types.CType;

public interface ReadableSMG {
  Set<SMGObject> getObjects();

  Map<String, SMGRegion> getGlobalObjects();

  Set<SMGObject> getHeapObjects();

  SMGRegion getObjectForVisibleVariable(String pVariable);

  ArrayDeque<CLangStackFrame> getStackFrames();

  SMGObject getNullObject();

  boolean isHeapObject(SMGObject pObject);

  boolean isGlobalObject(SMGObject pObject);

  SMGRegion getStackReturnObject(int pUp);

  SMGObject getObjectPointedBy(Integer pValue);

  boolean isObjectValid(SMGObject pRegion);

  BitSet getNullBytesForObject(SMGObject pObject);

  Set<Integer> getValues();

  boolean containsValue(Integer pValue);

  int getNullValue();

  boolean isUnequal(int pV1, int pV2);

  SMGSymbolicValue readValue(SMGObject pObject, int pOffset, CType pType);

  SMGEdgePointsTo getPointer(Integer pValue);

  Iterable<SMGEdgePointsTo> getPTEdges();

  boolean isPointer(Integer pValue);

  Integer getAddress(SMGObject pMemory, Integer pOffset);

  Iterable<SMGEdgeHasValue> getHVEdges();

  Iterable<SMGEdgeHasValue> getHVEdges(SMGEdgeHasValueFilter pFilter);

  SMGEdgeHasValue getUniqueHV(SMGEdgeHasValueFilter pFilter, boolean pStrict);

  boolean isCoveredByNullifiedBlocks(SMGObject pObject, int pOffset, CType pType);

  boolean isCoveredByNullifiedBlocks(SMGEdgeHasValue pEdge);

  boolean hasMemoryLeaks();

  Iterable<Integer> getNeqsForValue(Integer pValue);

  boolean haveNeqRelation(Integer pOp1, Integer pOp2);

  String getFunctionName(SMGRegion pObj);

  SMGExplicitValue getExplicit(SMGKnownSymValue pValue);

  boolean hasLocalVariable(String pVarName);

  boolean isIdenticalTo(ReadableSMG pOther);
}
