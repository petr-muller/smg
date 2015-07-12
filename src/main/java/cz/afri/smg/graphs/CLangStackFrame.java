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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.objects.SMGRegion;
import cz.afri.smg.types.CFunctionDeclaration;
import cz.afri.smg.types.CType;
import cz.afri.smg.types.CVoidType;

/**
 * Represents a C language stack frame
 */
public final class CLangStackFrame {
  static final String RETVAL_LABEL = "___cpa_temp_result_var_";

  /**
   * Function to which this stack frame belongs
   */
  private final CFunctionDeclaration stackFunction;

  /**
   * A mapping from variable names to a set of SMG objects, representing
   * local variables.
   */
  private final HashMap <String, SMGRegion> stackVariables = new HashMap<>();

  /**
   * An object to store function return value
   */
  private final SMGRegion returnValueObject;

  /**
   * Constructor. Creates an empty frame.
   *
   * @param pDeclaration Function for which the frame is created
   *
   * TODO: [PARAMETERS] Create objects for function parameters
   */
  public CLangStackFrame(final CFunctionDeclaration pDeclaration) {
    stackFunction = pDeclaration;
    CType returnType = pDeclaration.getType().getReturnType();
    if (returnType instanceof CVoidType) {
      // use a plain int as return type for void functions
      returnValueObject = null;
    } else {
      int returnValueSize = returnType.getSize();
      returnValueObject = new SMGRegion(returnValueSize, CLangStackFrame.RETVAL_LABEL);
    }
  }

  /**
   * Copy constructor.
   *
   * @param pFrame Original frame
   */
  public CLangStackFrame(final CLangStackFrame pFrame) {
    stackFunction = pFrame.stackFunction;
    stackVariables.putAll(pFrame.stackVariables);
    returnValueObject = pFrame.returnValueObject;
  }


  /**
   * Adds a SMG object pObj to a stack frame, representing variable pVariableName
   *
   * Throws {@link IllegalArgumentException} when some object is already
   * present with the name {@link pVariableName}
   *
   * @param pVariableName A name of the variable
   * @param pObject An object to put into the stack frame
   */
  public void addStackVariable(final String pVariableName, final SMGRegion pObject) {
    if (stackVariables.containsKey(pVariableName)) {
      throw new IllegalArgumentException("Stack frame for function '" +
                                       stackFunction.toString() +
                                       "' already contains a variable '" +
                                       pVariableName + "'");
    }

    stackVariables.put(pVariableName, pObject);
  }

  /* ********************************************* */
  /* Non-modifying functions: getters and the like */
  /* ********************************************* */

  /**
   * @return String representation of the stack frame
   */
  @Override
  public String toString() {
    StringBuilder toReturn = new StringBuilder("<");
    for (String key : stackVariables.keySet()) {
      toReturn.append(" ").append(stackVariables.get(key));
    }
    toReturn.append(" >");
    return toReturn.toString();
  }

  /**
   * Getter for obtaining an object corresponding to a variable name
   *
   * Throws {@link NoSuchElementException} when passed a name not present
   *
   * @param pName Variable name
   * @return SMG object corresponding to pName in the frame
   */
  public SMGRegion getVariable(final String pName) {
    SMGRegion toReturn = stackVariables.get(pName);

    if (toReturn == null) {
      throw new NoSuchElementException("No variable with name '" +
                                       pName + "' in stack frame for function '" +
                                       stackFunction.toString() + "'");
    }

    return toReturn;
  }

  /**
   * @param pName Variable name
   * @return True if variable pName is present, false otherwise
   */
  public boolean containsVariable(final String pName) {
    return stackVariables.containsKey(pName);
  }

  /**
   * @return Declaration of a function corresponding to the frame
   */
  public CFunctionDeclaration getFunctionDeclaration() {
    return stackFunction;
  }

  /**
   * @return a mapping from variables name to SMGObjects
   */
  public Map<String, SMGRegion> getVariables() {
    return Collections.unmodifiableMap(stackVariables);
  }

  /**
   * @return a set of all objects: return value object, variables, parameters
   */
  public Set<SMGObject> getAllObjects() {
    HashSet<SMGObject> retset = new HashSet<>();
    retset.addAll(stackVariables.values());
    if (returnValueObject != null) {
      retset.add(returnValueObject);
    }

    return Collections.unmodifiableSet(retset);
  }

  /**
   * @return an {@link SMGObject} reserved for function return value
   */
  public SMGRegion getReturnObject() {
    return returnValueObject;
  }
}
