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
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.objects.SMGRegion;

public final class CLangSMGConsistencyVerifier {
  private CLangSMGConsistencyVerifier() { } /* utility class */

  /**
   * Records a result of a single check to a logger along with a message
   *
   * @param pResult Result of the check
   * @param pLogger Logger to log the message
   * @param pMessage Message to be logged
   * @return The result of the check, i.e. equivalent to pResult
   */
  private static boolean verifyCLangSMGProperty(final boolean pResult, final String pMessage) {
    return pResult;
  }

  /**
   * Verifies that heap and global object sets are disjunct
   *
   * @param pLogger Logger to log the message
   * @param pSmg SMG to check
   * @return True if {@link pSmg} is consistent w.r.t. this criteria. False otherwise.
   */
  private static boolean verifyDisjunctHeapAndGlobal(final ReadableSMG pSmg) {
    Map<String, SMGRegion> globals = pSmg.getGlobalObjects();
    Set<SMGObject> heap = pSmg.getHeapObjects();

    boolean toReturn = Collections.disjoint(globals.values(), heap);

    if (!toReturn) {
      throw new IllegalStateException("CLangSMG inconsistent, heap and global objects are not disjoint");
    }

    return toReturn;
  }

  /**
   * Verifies that heap and stack object sets are disjunct
   *
   * @param pLogger Logger to log the message
   * @param pSmg SMG to check
   * @return True if {@link pSmg} is consistent w.r.t. this criteria. False otherwise.
   */
  private static boolean verifyDisjunctHeapAndStack(final ReadableSMG pSmg) {
    ArrayDeque<CLangStackFrame> stackFrames = pSmg.getStackFrames();
    Set<SMGObject> stack = new HashSet<>();

    for (CLangStackFrame frame: stackFrames) {
      stack.addAll(frame.getAllObjects());
    }
    Set<SMGObject> heap = pSmg.getHeapObjects();

    boolean toReturn = Collections.disjoint(stack, heap);

    if (!toReturn) {
      SetView<SMGObject> intersection = Sets.intersection(stack, heap);
      String message = "CLangSMG inconsistent, heap and stack objects are not disjoint: " + intersection;
      throw new IllegalStateException(message);
    }

    return toReturn;
  }

  /**
   * Verifies that global and stack object sets are disjunct
   *
   * @param pLogger Logger to log the message
   * @param pSmg SMG to check
   * @return True if {@link pSmg} is consistent w.r.t. this criteria. False otherwise.
   */
  private static boolean verifyDisjunctGlobalAndStack(final ReadableSMG pSmg) {
    ArrayDeque<CLangStackFrame> stackFrames = pSmg.getStackFrames();
    Set<SMGObject> stack = new HashSet<>();

    for (CLangStackFrame frame: stackFrames) {
      stack.addAll(frame.getAllObjects());
    }
    Map<String, SMGRegion> globals = pSmg.getGlobalObjects();

    boolean toReturn = Collections.disjoint(stack, globals.values());

    if (!toReturn) {
      throw new IllegalStateException("CLangSMG inconsistent, global and stack objects are not disjoint");
    }

    return toReturn;
  }

  /**
   * Verifies that heap, global and stack union is equal to the set of all objects
   *
   * @param pLogger Logger to log the message
   * @param pSmg SMG to check
   * @return True if {@link pSmg} is consistent w.r.t. this criteria. False otherwise.
   */
  private static boolean verifyStackGlobalHeapUnion(final ReadableSMG pSmg) {
    HashSet<SMGObject> objectUnion = new HashSet<>();

    objectUnion.addAll(pSmg.getHeapObjects());
    objectUnion.addAll(pSmg.getGlobalObjects().values());

    for (CLangStackFrame frame : pSmg.getStackFrames()) {
      objectUnion.addAll(frame.getAllObjects());
    }

    boolean toReturn = objectUnion.containsAll(pSmg.getObjects()) &&
                                                pSmg.getObjects().containsAll(objectUnion);

    if (!toReturn) {
      String message = "CLangSMG inconsistent: stack, heap and global object set union not identical to SMG object set";
      throw new IllegalStateException(message);
    }

    return toReturn;
  }

  /**
   * Verifies several NULL object-related properties
   * @param pLogger Logger to log the message
   * @param pSmg SMG to check
   *
   * @return True if {@link pSmg} is consistent w.r.t. this criteria. False otherwise.
   */
  private static boolean verifyNullObjectCLangProperties(final ReadableSMG pSmg) {
    // Verify that there is no NULL object in global scope
    for (SMGObject obj: pSmg.getGlobalObjects().values()) {
      if (!obj.notNull()) {
//        pLogger.log(Level.SEVERE, "CLangSMG inconsistent: null object in global object set [" + obj + "]");
        return false;
      }
    }

    // Verify there is no more than one NULL object in the heap object set
    SMGObject firstNull = null;
    for (SMGObject obj: pSmg.getHeapObjects()) {
      if (!obj.notNull()) {
        if (firstNull != null) {
          String message = "CLangSMG inconsistent: second null object in heap object set [first=" + firstNull +
                           ", second=" + obj + "]";
          throw new IllegalStateException(message);
        } else {
          firstNull = obj;
        }
      }
    }

    // Verify there is no NULL object in the stack object set
    for (CLangStackFrame frame: pSmg.getStackFrames()) {
      for (SMGObject obj: frame.getAllObjects()) {
        if (!obj.notNull()) {
//          pLogger.log(Level.SEVERE, "CLangSMG inconsistent: null object in stack object set [" + obj + "]");
          return false;
        }
      }
    }

    // Verify there is at least one NULL object
    if (firstNull == null) {
//      pLogger.log(Level.SEVERE, "CLangSMG inconsistent: no null object");
      return false;
    }

    return true;
  }

  /**
   * Verify the global scope is consistent: each record points to an
   * appropriately labeled object
   *
   * @param pLogger Logger to log the message
   * @param pSmg SMG to check
   * @return True if {@link pSmg} is consistent w.r.t. this criteria. False otherwise.
   */
  private static boolean verifyGlobalNamespace(final ReadableSMG pSmg) {
    Map<String, SMGRegion> globals = pSmg.getGlobalObjects();

    for (String label: pSmg.getGlobalObjects().keySet()) {
      String globalLabel = globals.get(label).getLabel();
      if (!globalLabel.equals(label)) {
        String message = "CLangSMG inconsistent: label [" + label + "] points to an object with label [" +
                         pSmg.getGlobalObjects().get(label).getLabel() + "]";
        throw new IllegalStateException(message);
      }
    }

    return true;
  }

  /**
   * Verify the stack name space: each record points to an appropriately
   * labeled object
   *
   * @param pLogger Logger to log the message
   * @param pSmg
   * @return True if {@link pSmg} is consistent w.r.t. this criteria. False otherwise.
   */
  private static boolean verifyStackNamespaces(final ReadableSMG pSmg) {
    HashSet<SMGObject> stackObjects = new HashSet<>();

    for (CLangStackFrame frame : pSmg.getStackFrames()) {
      for (SMGObject object : frame.getAllObjects()) {
        if (stackObjects.contains(object)) {
          String message = "CLangSMG inconsistent: object [" + object + "] present multiple times in the stack";
          throw new IllegalStateException(message);
        }
        stackObjects.add(object);
      }
    }

    return true;
  }

  /**
   * Verify all the consistency properties related to CLangSMG
   *
   * @param pLogger Logger to log results
   * @param pSmg SMG to check
   * @return True if {@link pSmg} is consistent w.r.t. this criteria. False otherwise.
   */
  public static boolean verifyCLangSMG(final ReadableSMG pReadableSMG) {
    if (!(pReadableSMG instanceof CLangSMG)) {
      throw new IllegalArgumentException("Attempted to check consistency of something that is not CLangSMG instance");
    }

    CLangSMG pSmg = (CLangSMG) pReadableSMG;
    boolean toReturn = SMGConsistencyVerifier.verifySMG(pSmg);

    toReturn = toReturn && verifyCLangSMGProperty(
        verifyDisjunctHeapAndGlobal(pSmg), "Checking CLangSMG consistency: heap and global object sets are disjunt");
    toReturn = toReturn && verifyCLangSMGProperty(
        verifyDisjunctHeapAndStack(pSmg), "Checking CLangSMG consistency: heap and stack objects are disjunct");
    toReturn = toReturn && verifyCLangSMGProperty(
        verifyDisjunctGlobalAndStack(pSmg), "Checking CLangSMG consistency: global and stack objects are disjunct");
    toReturn = toReturn && verifyCLangSMGProperty(
        verifyStackGlobalHeapUnion(pSmg),
        "Checking CLangSMG consistency: global, stack and heap object union contains all objects in SMG");
    toReturn = toReturn && verifyCLangSMGProperty(
        verifyNullObjectCLangProperties(pSmg), "Checking CLangSMG consistency: null object invariants hold");
    toReturn = toReturn && verifyCLangSMGProperty(
        verifyGlobalNamespace(pSmg), "Checking CLangSMG consistency: global namespace problem");
    toReturn = toReturn && verifyCLangSMGProperty(
        verifyStackNamespaces(pSmg), "Checking CLangSMG consistency: stack namespace");

    return toReturn;
  }
}
