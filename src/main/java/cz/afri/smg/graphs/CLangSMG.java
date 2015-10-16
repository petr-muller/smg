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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.Sets;

import cz.afri.smg.graphs.SMGValues.SMGKnownSymValue;
import cz.afri.smg.graphs.SMGValues.SMGSymbolicValue;
import cz.afri.smg.graphs.SMGValues.SMGUnknownValue;
import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.objects.SMGRegion;
import cz.afri.smg.types.CFunctionDeclaration;
import cz.afri.smg.types.CType;

/**
 * Extending SMG with notions specific for programs in C language:
 *  - separation of global, heap and stack objects
 *  - null object and value
 */
class CLangSMG extends SMG implements WritableSMG {
  /**
   * A container for object found on the stack:
   *  - local variables
   *  - parameters
   *
   * TODO: [STACK-FRAME-STRUCTURE] Perhaps it could be wrapped in a class?
   */
  private final ArrayDeque<CLangStackFrame> stackObjects = new ArrayDeque<>();

  /**
   * A container for objects allocated on heap
   */
  private final HashSet<SMGObject> heapObjects = new HashSet<>();

  /**
   * A container for global objects
   */
  private final HashMap<String, SMGRegion> globalObjects = new HashMap<>();

  /**
   * A flag signifying the edge leading to this state caused memory to be leaked
   * TODO: Seems pretty arbitrary: perhaps we should have a more general
   * solution, like a container with (type, message) error witness kind of
   * thing?
   */
  private boolean hasLeaks = false;

  /**
   * A flag setting if the class should perform additional consistency checks.
   * It should be useful only during debugging, when is should find bad external
   * calls closer to their origin. We probably do not want t run the checks in
   * the production build.
   */
  private static boolean performChecks = false;

  public static void setPerformChecks(final boolean pSetting) {
    CLangSMG.performChecks = pSetting;
  }

  public static boolean performChecks() {
    return CLangSMG.performChecks;
  }

  /**
   * Constructor.
   *
   * Keeps consistency: yes
   *
   * Newly constructed CLangSMG contains a single nullObject with an address
   * pointing to it, and is empty otherwise.
   */
  public CLangSMG() {
    super();
    heapObjects.add(getNullObject());
  }

  /**
   * Copy constructor.
   *
   * Keeps consistency: yes
   *
   * @param pHeap
   *          The original CLangSMG
   */
  public CLangSMG(final CLangSMG pHeap) {
    super(pHeap);

    for (CLangStackFrame stackFrame : pHeap.stackObjects) {
      CLangStackFrame newFrame = new CLangStackFrame(stackFrame);
      stackObjects.add(newFrame);
    }

    heapObjects.addAll(pHeap.heapObjects);
    globalObjects.putAll(pHeap.globalObjects);
    hasLeaks = pHeap.hasLeaks;
  }

  @Override
  public void removeHeapObject(final SMGObject pObj) {
    super.removeObject(pObj);
    if (isHeapObject(pObj)) {
      heapObjects.remove(pObj);
    } else {
      throw new IllegalArgumentException("Cannot directly remove non-heap objects");
    }
  }

  /**
   * Add a object to the heap.
   *
   * Keeps consistency: no
   *
   * With checks: throws {@link IllegalArgumentException} when asked to add an
   * object already present.
   *
   * @param pObject
   *          Object to add.
   */
  @Override
  public void addHeapObject(final SMGObject pObject) {
    if (CLangSMG.performChecks() && heapObjects.contains(pObject)) {
      throw new IllegalArgumentException("Heap object already in the SMG: [" + pObject + "]");
    }
    heapObjects.add(pObject);
    addObject(pObject);
  }

  /**
   * Add a global object to the SMG
   *
   * Keeps consistency: no
   *
   * With checks: throws {@link IllegalArgumentException} when asked to add
   * an object already present, or an global object with a label identifying
   * different object

   * @param pObject Object to add
   */
  @Override
  public void addGlobalObject(final SMGRegion pObject) {
    if (CLangSMG.performChecks() && globalObjects.values().contains(pObject)) {
      throw new IllegalArgumentException("Global object already in the SMG: [" + pObject + "]");
    }

    if (CLangSMG.performChecks() && globalObjects.containsKey(pObject.getLabel())) {
      throw new IllegalArgumentException("Global object with label [" + pObject.getLabel() + "] already in the SMG");
    }

    globalObjects.put(pObject.getLabel(), pObject);
    super.addObject(pObject);
  }

  /**
   * Adds an object to the current stack frame
   *
   * Keeps consistency: no
   *
   * @param pObject Object to add
   *
   * TODO: [SCOPES] Scope visibility vs. stack frame issues: handle cases where a variable is visible
   * but is is allowed to override (inner blocks)
   * TODO: Consistency check (allow): different objects with same label inside a frame, but in different block
   * TODO: Test for this consistency check
   *
   * TODO: Shall we need an extension for putting objects to upper frames?
   */
  @Override
  public void addStackObject(final SMGRegion pObject) {
    super.addObject(pObject);
    stackObjects.peek().addStackVariable(pObject.getLabel(), pObject);
  }

  /**
   * Add a new stack frame for the passed function.
   *
   * Keeps consistency: yes
   *
   * @param pFunctionDeclaration A function for which to create a new stack frame
   */
  @Override
  public void addStackFrame(final CFunctionDeclaration pFunctionDeclaration) {
    CLangStackFrame newFrame = new CLangStackFrame(pFunctionDeclaration);

    // Return object is NULL for void functions
    SMGObject returnObject = newFrame.getReturnObject();
    if (returnObject != null) {
      super.addObject(newFrame.getReturnObject());
    }
    stackObjects.push(newFrame);
  }

  /**
   * Sets a flag indicating this SMG is a successor over the edge causing a
   * memory leak.
   *
   * Keeps consistency: yes
   */
  @Override
  public void setMemoryLeak() {
    hasLeaks = true;
  }

  /**
   * Remove a top stack frame from the SMG, along with all objects in it, and
   * any edges leading from/to it.
   *
   * TODO: A testcase with (invalid) passing of an address of a dropped frame object
   * outside, and working with them. For that, we should probably keep those as invalid, so
   * we can spot such bug.
   *
   * Keeps consistency: yes
   */
  @Override
  public void dropStackFrame() {
    CLangStackFrame frame = stackObjects.pop();
    for (SMGObject object : frame.getAllObjects()) {
      removeObjectAndEdges(object);
    }

    if (CLangSMG.performChecks()) {
      CLangSMGConsistencyVerifier.verifyCLangSMG(this);
    }
  }

  /**
   * Prune the SMG: remove all unreachable objects (heap ones: global and stack
   * are always reachable) and values.
   *
   * TODO: Too large. Refactor into fewer pieces
   *
   * Keeps consistency: yes
   */
  @Override
  public void pruneUnreachable() {
    Set<SMGObject> seenObjects = new HashSet<>();
    Set<Integer> seenValues = new HashSet<>();
    Queue<SMGObject> workqueue = new ArrayDeque<>();

    // TODO: wrap to getStackObjects(), perhaps just internally?
    for (CLangStackFrame frame : getStackFrames()) {
      for (SMGObject stackObject : frame.getAllObjects()) {
        workqueue.add(stackObject);
      }
    }

    workqueue.addAll(getGlobalObjects().values());

    SMGEdgeHasValueFilter filter = new SMGEdgeHasValueFilter();

    /*
     * TODO: Refactor into generic methods for obtaining reachable/unreachable
     * subSMGs
     *
     * TODO: Perhaps introduce a SubSMG class which would be a SMG tied
     * to a certain (Clang)SMG and guaranteed to be a subset of it?
     */

    while (!workqueue.isEmpty()) {
      SMGObject processed = workqueue.remove();
      if (!seenObjects.contains(processed)) {
        seenObjects.add(processed);
        filter.filterByObject(processed);
        for (SMGEdgeHasValue outbound : getHVEdges(filter)) {
          SMGObject pointedObject = getObjectPointedBy(outbound.getValue());
          if (pointedObject != null && !seenObjects.contains(pointedObject)) {
            workqueue.add(pointedObject);
          }
          if (!seenValues.contains(Integer.valueOf(outbound.getValue()))) {
            seenValues.add(Integer.valueOf(outbound.getValue()));
          }
        }
      }
    }

    /*
     * TODO: Refactor into generic methods for substracting SubSMGs (see above)
     */
    Set<SMGObject> strayObjects = new HashSet<>(Sets.difference(getObjects(), seenObjects));
    for (SMGObject strayObject : strayObjects) {
      if (strayObject.notNull()) {
        if (isObjectValid(strayObject)) {
          setMemoryLeak();
        }
        removeObjectAndEdges(strayObject);
        heapObjects.remove(strayObject);

      }
    }

    Set<Integer> strayValues = new HashSet<>(Sets.difference(getValues(), seenValues));
    for (Integer strayValue : strayValues) {
      if (strayValue != getNullValue()) {
        // Here, we can't just remove stray value, we also have to remove the points-to edge
        if (isPointer(strayValue)) {
          removePointsToEdge(strayValue);
        }

        removeValue(strayValue);
      }
    }
  }

  /* ********************************************* */
  /* Non-modifying functions: getters and the like */
  /* ********************************************* */

  /**
   * Getter for obtaining a string representation of the CLangSMG. Constant.
   *
   * @return String representation of the CLangSMG
   */
  @Override
  public String toString() {
    return "CLangSMG [\n stack_objects=" + stackObjects + "\n heap_objects=" + heapObjects +
        "\n global_objects=" + globalObjects + "\n " + valuesToString() + "\n " + ptToString() + "\n " + hvToString() +
    		"\n]";
  }

  /**
   * Returns an SMGObject tied to the variable name. The name must be visible in
   * the current scope: it needs to be visible either in the current frame, or it
   * is a global variable. Constant.
   *
   * @param pVariableName A name of the variable
   * @return An object tied to the name, if such exists in the visible scope.
   *
   * TODO: [SCOPES] Test for getting visible local object hiding other local object
   * @throws SMGInconsistentException
   */
  @Override
  public SMGRegion getObjectForVisibleVariable(final String pVariableName) {
    // Look in the local frame
    if (stackObjects.size() != 0) {
      if (stackObjects.peek().containsVariable(pVariableName)) {
        return stackObjects.peek().getVariable(pVariableName);
      }
    }

    // Look in the global scope
    if (globalObjects.containsKey(pVariableName)) {
      return globalObjects.get(pVariableName);
    }

    throw new UnsupportedOperationException("No object for variable name: " + pVariableName);
  }

  @Override
  public boolean hasLocalVariable(final String pVariableName) {
    return ((stackObjects.size() > 0) && stackObjects.peek().containsVariable(pVariableName));
  }

  /**
   * Returns the stack of frames containing objects. Constant.
   *
   * @return Stack of frames
   */
  @Override
  public ArrayDeque<CLangStackFrame> getStackFrames() {
    return stackObjects;
  }

  /**
   * Constant.
   *
   * @return Unmodifiable view of the set of the heap objects
   */
  @Override
  public Set<SMGObject> getHeapObjects() {
    return Collections.unmodifiableSet(heapObjects);
  }

  /**
   * Constant.
   *
   * Checks whether given object is on the heap.
   *
   * @param object SMGObject to be checked.
   * @return True, if the given object is referenced in the set of heap objects, false otherwise.
   *
   */
  @Override
  public boolean isHeapObject(final SMGObject object) {
    return heapObjects.contains(object);
  }

  /**
   * Constant.
   *
   * @return Unmodifiable map from variable names to global objects.
   */
  @Override
  public Map<String, SMGRegion> getGlobalObjects() {
    return Collections.unmodifiableMap(globalObjects);
  }

  /**
   * Constant.
   *
   * @return True if the SMG is a successor over the edge causing some memory
   * to be leaked. Returns false otherwise.
   */
  @Override
  public boolean hasMemoryLeaks() {
    return hasLeaks;
  }

  /**
   * Constant.
   *
   * @return a {@link SMGObject} for current function return value
   */
  @Override
  public SMGRegion getStackReturnObject(final int pUp) {
    return stackObjects.peek().getReturnObject();
  }

  @Override
  public String getFunctionName(final SMGRegion pObject) {
    for (CLangStackFrame cLangStack : stackObjects) {
      if (cLangStack.getAllObjects().contains(pObject)) {
        return cLangStack.getFunctionDeclaration().getName();
      }
    }

    throw new IllegalArgumentException("No function name for non-stack object");
  }

  @Override
  public void mergeValues(final int v1, final int v2) {
    super.mergeValues(v1, v2);
  }

  public final void removeHeapObjectAndEdges(final SMGObject pObject) {
    heapObjects.remove(pObject);
    removeObjectAndEdges(pObject);
  }

  @Override
  public boolean containsValue(final Integer pValue) {
    return getValues().contains(pValue);
  }

  /**
   * Determine, whether the two given symbolic values are not equal.
   * If this method does not return true, the relation of these
   * symbolic values is unknown.
   *
   * @param value1 first symbolic value to be checked
   * @param value2 second symbolic value to be checked
   * @return true, if the symbolic values are known to be not equal, false, if it is unknown.
   * @throws SMGInconsistentException
   */
  @Override
  public boolean isUnequal(final int value1, final int value2) {

    if (isPointer(value1) && isPointer(value2)) {

      if (value1 != value2) {
        /* This is just a safety check,
        equal pointers should have equal symbolic values.*/
        SMGEdgePointsTo edge1;
        SMGEdgePointsTo edge2;
        edge1 = getPointer(value1);
        edge2 = getPointer(value2);

        return edge1.getObject() != edge2.getObject() || edge1.getOffset() != edge2.getOffset();
      }
    }
    return false;
  }

  /**
   * Get the symbolic value, that represents the address
   * pointing to the given memory with the given offset, if it exists.
   *
   * @param memory
   *          get address belonging to this memory.
   * @param offset
   *          get address with this offset relative to the beginning of the
   *          memory.
   * @return Address of the given field, or null, if such an address does not
   *         yet exist in the SMG.
   */
  @Override
  public Integer getAddress(final SMGObject pMemory, final Integer pOffset) {

    // TODO A better way of getting those edges, maybe with a filter
    // like the Has-Value-Edges

    Set<SMGEdgePointsTo> pointsToEdges = getPTEdges();

    for (SMGEdgePointsTo edge : pointsToEdges) {
      if (edge.getObject().equals(pMemory) && edge.getOffset() == pOffset) {
        return edge.getValue();
      }
    }

    return null;
  }

  /**
   * Read Value in field (object, type) of an Object.
   *
   * This method does not modify the state being read,
   * and is therefore safe to call outside of a
   * transfer relation context.
   *
   * @param pObject SMGObject representing the memory the field belongs to.
   * @param pOffset offset of field being read.
   * @param pType type of field
   * @return A Symbolic value, if found, otherwise null.
   * @throws SMGInconsistentException
   */
  @Override
  public SMGSymbolicValue readValue(final SMGObject pObject, final int pOffset, final CType pType) {
    if (!isObjectValid(pObject)) {
      throw new UnsupportedOperationException("No value can be read from an invalid object");
    }

    SMGEdgeHasValue edge = new SMGEdgeHasValue(pType, pOffset, pObject, 0);

    SMGEdgeHasValueFilter filter = SMGEdgeHasValueFilter.objectFilter(pObject).filterAtOffset(pOffset);

    for (SMGEdgeHasValue objectEdge : getHVEdges(filter)) {
      if (edge.isCompatibleFieldOnSameObject(objectEdge)) {
        return SMGKnownSymValue.valueOf(objectEdge.getValue());
      }
    }

    if (isCoveredByNullifiedBlocks(edge)) {
    	return SMGKnownSymValue.ZERO; 
    }

    return SMGUnknownValue.getInstance();
  }

  /**
   * This method simulates a free invocation. It checks,
   * whether the call is valid, and invalidates the
   * Memory the given address points to.
   * The address (address, offset, smgObject) is the argument
   * of the free invocation. It does not need to be part of the SMG.
   *
   * @param pAddress The symbolic Value of the address.
   * @param pOffset The offset of the address relative to the beginning of smgObject.
   * @param pRegion The memory the given Address belongs to.
   * @throws SMGInconsistentException
   */
  @Override
  public void free(final Integer pAddress, final Integer pOffset, final SMGRegion pRegion) {

    if (!isHeapObject(pRegion)) {
      // You may not free any objects not on the heap.
      //setInvalidFree();
      return;
    }

    if (!(pOffset == 0)) {
      // you may not invoke free on any address that you
      // didn't get through a malloc invocation.
      //setInvalidFree();
      return;
    }

    if (!isObjectValid(pRegion)) {
      // you may not invoke free multiple times on
      // the same object
      //setInvalidFree();
      return;
    }

    setValidity(pRegion, false);
    SMGEdgeHasValueFilter filter = SMGEdgeHasValueFilter.objectFilter(pRegion);

    List<SMGEdgeHasValue> toRemove = new ArrayList<>();
    for (SMGEdgeHasValue edge : getHVEdges(filter)) {
      toRemove.add(edge);
    }

    for (SMGEdgeHasValue edge : toRemove) {
      removeHasValueEdge(edge);
    }
  }

  @Override
  public boolean isGlobalObject(final SMGObject pObject) {
    if (pObject.isAbstract()) {
      return false;
    }
    return getGlobalObjects().containsValue(pObject);
  }

  /**
   * Makes SMGState create a new object and put it into the global namespace
   *
   * Keeps consistency: yes
   *
   * @param pType Type of the new object
   * @param pVarName Name of the global variable
   * @return Newly created object
   *
   * @throws SMGInconsistentException when resulting SMGState is inconsistent
   * and the checks are enabled
   */
  @Override
  public SMGRegion addGlobalVariable(final CType pType, final String pVarName) {
    int size = pType.getSize();
    SMGRegion newObject = new SMGRegion(size, pVarName);

    addGlobalObject(newObject);
    return newObject;
  }

  /**
   * Makes SMGState create a new object and put it into the current stack
   * frame.
   *
   * Keeps consistency: yes
   *
   * @param pType Type of the new object
   * @param pVarName Name of the local variable
   * @return Newly created object
   *
   * @throws SMGInconsistentException when resulting SMGState is inconsistent
   * and the checks are enabled
   */
  @Override
  public SMGRegion addLocalVariable(final CType pType, final String pVarName) {
    int size = pType.getSize();
    SMGRegion newObject = new SMGRegion(size, pVarName);

    addStackObject(newObject);
    return newObject;
  }

  @Override
  public boolean isIdenticalTo(final ReadableSMG pOther) {
    if (!(pOther instanceof CLangSMG)) {
      throw new IllegalArgumentException("Cannot compare CLangSMG to non-CLangSMG");
    }

    CLangSMG other = (CLangSMG) pOther; 

    if (!(super.isIdenticalTo(other))) {
        return false;
    }

    boolean stackIdentical = Arrays.equals(stackObjects.toArray(), other.stackObjects.toArray());
    boolean heapIdentical = heapObjects.equals(other.heapObjects);
    boolean globalIdentical = globalObjects.equals(other.globalObjects);

    return stackIdentical && heapIdentical && globalIdentical;
  }
}
