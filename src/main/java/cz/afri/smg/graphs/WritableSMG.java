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

import java.util.Set;

import cz.afri.smg.graphs.SMGValues.SMGKnownExpValue;
import cz.afri.smg.graphs.SMGValues.SMGKnownSymValue;
import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.objects.SMGRegion;
import cz.afri.smg.types.CFunctionDeclaration;
import cz.afri.smg.types.CType;

public interface WritableSMG extends ReadableSMG {
  /**
   * Adds a new heap object to the SMG. The heap object may be both region and an abstract object.
   *
   * @param pObject
   *          object to add to the SMG
   */
  void addHeapObject(SMGObject pObject);

  /**
   * Removes a given object from the SMG. All has-value edges coming from this object and point-to edges coming to this
   * objects will be removed too. The method throws {@link IllegalArgumentException} if passed an object which is not a
   * heap object in the SMG.
   *
   * @param pObject
   *          object to remove from the SMG
   */
  void removeHeapObject(SMGObject pObject);

  /**
   * Adds a new stack frame to the SMG.
   *
   * @param pFunction
   *          declaration of a function for which the stack frame is added
   */
  void addStackFrame(CFunctionDeclaration pFunction);

  /**
   * Removes a top stack frame from the SMG. All objects in the stack frame will be removed along with all has-value
   * edges leading from them and points-to edges leading to them.
   */
  void dropStackFrame();

  /**
   * Adds a new global variable region to the SMG.
   *
   * @param pType
   *          type of the variable
   * @param pVarName
   *          name of the variable
   * @return newly created region for the variable
   */
  SMGRegion addGlobalVariable(CType pType, String pVarName);

  /**
   * Adds a new local variable to the top stack frame of the SMG
   *
   * @param pType
   *          type of the variable
   * @param pVarName
   *          name of the variable
   * @return newly created region for the variable
   */
  SMGRegion addLocalVariable(CType pType, String pVarName);

  void addValue(Integer pValue);

  void removeValue(Integer pValue);

  void addPointsToEdge(SMGEdgePointsTo pEdge);

  void removePointsToEdge(Integer pValue);

  void addHasValueEdge(SMGEdgeHasValue pEdge);

  void removeHasValueEdge(SMGEdgeHasValue pEdge);

  void replaceHVSet(Set<SMGEdgeHasValue> pHV);

  void setValidity(SMGRegion pRegion, boolean pValidity);

  void pruneUnreachable();

  void setMemoryLeak();

  void addNeqRelation(Integer pOp1, Integer pOp2);

  void mergeValues(int pOp1, int pOp2);

  void clearExplicit(SMGKnownSymValue pKey);

  void putExplicit(SMGKnownSymValue pKey, SMGKnownExpValue pValue);

  void free(Integer pAddress, Integer pOffset, SMGRegion pRegion);
}
