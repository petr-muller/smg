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

  /**
   * Adds a symbolic value to the SMG
   *
   * @param pValue
   *          symbolic value to add
   */
  void addValue(Integer pValue);

  /**
   * Removes a symbolic value from the SMG. Edges leading to and from the SMG are *not* removed, the caller is
   * responsible for removing these, or the SMG will be left in an inconsistent state.
   *
   * @param pValue
   *          symbolic value to remove
   */
  void removeValue(Integer pValue);

  /**
   * Adds a new points-to edge to the SMG
   *
   * @param pEdge
   *          edge to add to the SMG
   */
  void addPointsToEdge(SMGEdgePointsTo pEdge);

  /**
   * Removes a points-to edge from the SMG.
   *
   * @param pValue
   *          value from which leads the removed SMG
   */
  void removePointsToEdge(Integer pValue);

  /**
   * Adds a new has-value edge to the SMG
   *
   * @param pEdge
   *          edge to add to the SMG
   */
  void addHasValueEdge(SMGEdgeHasValue pEdge);

  /**
   * Removes a has-value from the SMG
   *
   * @param pEdge
   *          edge to remove from the SMG
   */
  void removeHasValueEdge(SMGEdgeHasValue pEdge);

  /**
   * Removes all has-value edges from the SMG and adds a new set of has-value edges.
   *
   * @param pHV
   *          The set of has-value edges to replace the old set
   */
  void replaceHVSet(Set<SMGEdgeHasValue> pHV);

  /**
   * Sets validity of a region.
   *
   * @param pRegion
   *          region for which to set validity
   * @param pValidity
   *          target validity
   */
  void setValidity(SMGRegion pRegion, boolean pValidity);

  /**
   * Walks the whole SMG and removes unreachable elements (objects, values, edges) and invalid regions. If there was an
   * unreachable valid object, a memory leak property of the SMG was set to true.
   */
  void pruneUnreachable();

  /**
   * Sets the memory leak property of the SMG.
   */
  void setMemoryLeak();

  /**
   * Adds an explicit non-equality relation between two values
   *
   * @param pOp1
   *          first value
   * @param pOp2
   *          second value
   */
  void addNeqRelation(Integer pOp1, Integer pOp2);

  /**
   * Merges two values (equivalence relation between two values)
   *
   * @param pOp1
   *          first value
   * @param pOp2
   *          second value
   */
  void mergeValues(int pOp1, int pOp2);

  /**
   * Clears an explicit value for a symbolic value
   *
   * @param pKey
   *          symbolic value for which the explicit value will be cleared
   */
  void clearExplicit(SMGKnownSymValue pKey);

  /**
   * Sets an explicit value for a symbolic value
   *
   * @param pKey
   *          symbolic value
   * @param pValue
   *          explicit value
   */
  void putExplicit(SMGKnownSymValue pKey, SMGKnownExpValue pValue);
}
