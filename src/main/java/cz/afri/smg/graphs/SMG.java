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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.SetMultimap;

import cz.afri.smg.graphs.SMGValues.SMGExplicitValue;
import cz.afri.smg.graphs.SMGValues.SMGKnownExpValue;
import cz.afri.smg.graphs.SMGValues.SMGKnownSymValue;
import cz.afri.smg.graphs.SMGValues.SMGUnknownValue;
import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.objects.SMGRegion;
import cz.afri.smg.types.CType;

class SMG {
  private final HashSet<SMGObject> objects = new HashSet<>();
  private final HashSet<Integer> values = new HashSet<>();
  private final HashSet<SMGEdgeHasValue> hvEdges = new HashSet<>();
  private final HashMap<Integer, SMGEdgePointsTo> ptEdges = new HashMap<>();
  private final HashMap<SMGObject, Boolean> objectValidity = new HashMap<>();
  private final NeqRelation neq = new NeqRelation();
  private final Map<SMGKnownSymValue, SMGKnownExpValue> explicitValues = new HashMap<>();

  /**
   * A special object representing NULL
   */
  private static final SMGObject NULL_OBJECT = SMGObject.getNullObject();

  /**
   * An address of the special object representing null
   */
  private static final int NULL_ADDRESS = 0;

  /**
   * Constructor.
   *
   * Consistent after call: yes.
   *
   * @param pMachineModel A machine model this SMG uses.
   *
   */
  public SMG() {
    SMGEdgePointsTo nullPointer = new SMGEdgePointsTo(NULL_ADDRESS, NULL_OBJECT, 0);

    addObject(NULL_OBJECT);
    objectValidity.put(NULL_OBJECT, false);

    addValue(NULL_ADDRESS);
    addPointsToEdge(nullPointer);
  }

  /**
   * Copy constructor.
   *
   * Consistent after call: yes if pHeap is consistent, no otherwise.
   *
   * @param pHeap Original SMG.
   */
  public SMG(final SMG pHeap) {
    objects.addAll(pHeap.objects);
    values.addAll(pHeap.values);
    hvEdges.addAll(pHeap.hvEdges);
    ptEdges.putAll(pHeap.ptEdges);

    objectValidity.putAll(pHeap.objectValidity);

    explicitValues.putAll(pHeap.explicitValues);

    neq.putAll(pHeap.neq);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hvEdges, neq, objectValidity, objects, ptEdges, values, explicitValues);
  }

  /**
   * Add an object {@link pObj} to the SMG.
   *
   * Keeps consistency: no.
   *
   * @param pObj object to add.
   *
   */
  public final void addObject(final SMGObject pObj) {
    addObject(pObj, true);
  }

  /**
   * Remove {@link pValue} from the SMG. This method does not remove
   * any edges leading from/to the removed value.
   *
   * Keeps consistency: no
   *
   * @param pValue Value to remove
   */
  public final void removeValue(final Integer pValue) {
    values.remove(pValue);
    neq.removeValue(pValue);
  }
  /**
   * Remove {@link pObj} from the SMG. This method does not remove
   * any edges leading from/to the removed object.
   *
   * Keeps consistency: no
   *
   * @param pObj Object to remove
   */
  public final void removeObject(final SMGObject pObj) {
    objects.remove(pObj);
    objectValidity.remove(pObj);
  }

  /**
   * Remove {@link pObj} and all edges leading from/to it from the SMG
   *
   * Keeps consistency: no
   *
   * @param pObj Object to remove
   */
  public final void removeObjectAndEdges(final SMGObject pObj) {
    removeObject(pObj);
    Iterator<SMGEdgeHasValue> hvIter = hvEdges.iterator();
    Iterator<SMGEdgePointsTo> ptIter = ptEdges.values().iterator();
    while (hvIter.hasNext()) {
      if (hvIter.next().getObject() == pObj) {
        hvIter.remove();
      }
    }

    while (ptIter.hasNext()) {
      if (ptIter.next().getObject() == pObj) {
        ptIter.remove();
      }
    }
  }

  /**
   * Add {@link pObj} object to the SMG, with validity set to {@link pValidity}.
   *
   * Keeps consistency: no.
   *
   * @param pObj      Object to add
   * @param pValidity Validity of the newly added object.
   *
   */
  public final void addObject(final SMGObject pObj, final boolean pValidity) {
    objects.add(pObj);
    objectValidity.put(pObj, pValidity);
  }

  /**
   * Add {@link pValue} value to the SMG.
   *
   * Keeps consistency: no.
   *
   * @param pValue  Value to add.
   */
  public final void addValue(final Integer pValue) {
    values.add(pValue);
  }

  /**
   * Add {@link pEdge} Points-To edge to the SMG.
   *
   * Keeps consistency: no.
   *
   * @param pEdge Points-To edge to add.
   */
  public final void addPointsToEdge(final SMGEdgePointsTo pEdge) {
    ptEdges.put(pEdge.getValue(), pEdge);
  }

  /**
   * Add {@link pEdge} Has-Value edge to the SMG.
   *
   * Keeps consistency: no
   *
   * @param pEdge Has-Value edge to add
   */
  public final void addHasValueEdge(final SMGEdgeHasValue pEdge) {
    hvEdges.add(pEdge);
  }

  /**
   * Remove {@link pEdge} Has-Value edge from the SMG.
   *
   * Keeps consistency: no
   *
   * @param pEdge Has-Value edge to remove
   */
  public final void removeHasValueEdge(final SMGEdgeHasValue pEdge) {
    hvEdges.remove(pEdge);
  }

  /**
   * Remove the Points-To edge from the SMG with the value {@link pValue} as Source.
   *
   * Keeps consistency: no
   *
   * @param pValue the Source of the Points-To edge to be removed
   */
  public final void removePointsToEdge(final Integer pValue) {
    ptEdges.remove(pValue);
  }

  /**
   * Sets the validity of the object {@link pObject} to {@link pValidity}.
   * Throws {@link IllegalArgumentException} if {@link pObject} is
   * not present in SMG.
   *
   * Keeps consistency: no
   *
   * @param pObj An object.
   * @param pValidity Validity to set.
   */
  public void setValidity(final SMGRegion pObject, final boolean pValidity) {
    if (!objects.contains(pObject)) {
      throw new IllegalArgumentException("Object [" + pObject + "] not in SMG");
    }

    objectValidity.put(pObject, pValidity);
  }

  /**
   * Replaces whole HasValue edge set with new set.
   * @param pNewHV
   *
   * Keeps consistency: no
   */
  public void replaceHVSet(final Set<SMGEdgeHasValue> pNewHV) {
    hvEdges.clear();
    hvEdges.addAll(pNewHV);
  }

  /**
   * Adds a neq relation between two values to the SMG
   *
   * @param pV1
   * @param pV2
   *
   * Keeps consistency: no
   */
  public void addNeqRelation(final Integer pV1, final Integer pV2) {
    neq.addRelation(pV1, pV2);
  }

  /* ********************************************* */
  /* Non-modifying functions: getters and the like */
  /* ********************************************* */

  /**
   * Getter for obtaining designated NULL object. Constant.
   * @return An object guaranteed to be the only NULL object in the SMG
   */
  public final SMGObject getNullObject() {
    return SMG.NULL_OBJECT;
  }

  /**
   * Getter for obtaining designated zero value. Constant.
   * @return A value guaranteed to be the only zero value in the SMG
   */
  public final int getNullValue() {
    return SMG.NULL_ADDRESS;
  }

  /**
   * Getter for obtaining string representation of values set. Constant.
   * @return String representation of values set
   */
  public final String valuesToString() {
    return "values=" + values.toString();
  }

  /**
   * Getter for obtaining string representation of has-value edges set. Constant.
   * @return String representation of has-value edges set
   */
  public final String hvToString() {
    return "hasValue=" + hvEdges.toString();
  }

  /**
   * Getter for obtaining string representation of points-to edges set. Constant.
   * @return String representation of points-to edges set
   */
  public final String ptToString() {
    return "pointsTo=" + ptEdges.toString();
  }

  /**
   * Getter for obtaining unmodifiable view on values set. Constant.
   * @return Unmodifiable view on values set.
   */
  public final Set<Integer> getValues() {
    return Collections.unmodifiableSet(values);
  }

  /**
   * Getter for obtaining unmodifiable view on objects set. Constant.
   * @return Unmodifiable view on objects set.
   */
  public final Set<SMGObject> getObjects() {
    return Collections.unmodifiableSet(objects);
  }

  /**
   * Getter for obtaining unmodifiable view on Has-Value edges set. Constant.
   * @return Unmodifiable view on Has-Value edges set.
   */
  public final Set<SMGEdgeHasValue> getHVEdges() {
    return Collections.unmodifiableSet(hvEdges);
  }

  /**
   * Getter for obtaining unmodifiable view on Has-Value edges set, filtered by
   * a certain set of criteria.
   * @param pFilter Filtering object
   * @return A set of Has-Value edges for which the criteria in p hold
   */
  public final Iterable<SMGEdgeHasValue> getHVEdges(final SMGEdgeHasValueFilter pFilter) {
    return Iterables.filter(Collections.unmodifiableSet(hvEdges), pFilter.asPredicate());
  }

  /**
   * Getter for obtaining a unique edge adhering to certain criteria.
   * @param pFilter Criteria for filtering
   * @param pCheck Defines if a check for non-uniqueness should be done.
   * @return A HVEdge adhering to filter
   */
  public final SMGEdgeHasValue getUniqueHV(final SMGEdgeHasValueFilter pFilter, final boolean pCheck) {
    Iterator<SMGEdgeHasValue> it = getHVEdges(pFilter).iterator();
    SMGEdgeHasValue hv = it.next();
    if (pCheck && it.hasNext()) {
      throw new IllegalArgumentException("Applying filter does not result in unique HV edge");
    }
    return hv;
  }

  /**
   * Getter for obtaining unmodifiable view on Points-To edges set. Constant.
   * @return Unmodifiable view on Points-To edges set.
   */
  public final Set<SMGEdgePointsTo> getPTEdges() {
    return Collections.unmodifiableSet(new HashSet<>(ptEdges.values()));
  }

  /**
   * Getter for obtaining an object, pointed by a value {@link pValue}. Constant.
   *
   * @param pValue An origin value.
   * @return The object pointed by the value {@link pValue}, if such exists.
   * Null, if {@link pValue} does not point to any
   * object.
   *
   * Throws {@link IllegalArgumentException} if {@link pValue} is
   * not present in the SMG.
   *
   * TODO: Test
   * TODO: Consistency check: no value can point to more objects
   */
  public final SMGObject getObjectPointedBy(final Integer pValue) {
    if (!values.contains(pValue)) {
      throw new IllegalArgumentException("Value [" + pValue + "] not in SMG");
    }

    if (ptEdges.containsKey(pValue)) {
      return ptEdges.get(pValue).getObject();
    } else {
      return null;
    }
  }

  /**
   * Getter for determining if the object {@link pObject} is valid. Constant.
   * Throws {@link IllegalArgumentException} if {@link pObject} is
   * not present in the SMG.
   *
   * @param pObject An object.
   * @return True if {@link pObject} is valid, False if it is invalid.
   */
  public final boolean isObjectValid(final SMGObject pObject) {
    if (!objects.contains(pObject)) {
      throw new IllegalArgumentException("Object [" + pObject + "] not in SMG");
    }

    return objectValidity.get(pObject).booleanValue();
  }

  /**
   * Obtains a bitset signifying where the object bytes are nullified.
   *
   * Constant.
   *
   * @param pObj SMGObject for which the information is to be obtained
   * @return A bitset. A bit has 1 value if the appropriate byte is guaranteed
   * to be NULL (is covered by a HasValue edge leading from an object to null value,
   * 0 otherwise.
   */
  public BitSet getNullBytesForObject(final SMGObject pObj) {
    BitSet bs = new BitSet(pObj.getSize());
    bs.clear();
    SMGEdgeHasValueFilter objectFilter = SMGEdgeHasValueFilter.objectFilter(pObj).filterHavingValue(getNullValue());

    for (SMGEdgeHasValue edge : getHVEdges(objectFilter)) {
      bs.set(edge.getOffset(), edge.getOffset() + edge.getSizeInBytes());
    }

    return bs;
  }

  /**
   * Checks, whether a {@link SMGEdgePointsTo} edge exists with the
   * given value as source.
   *
   *
   * @param value the source of the {@link SMGEdgePointsTo} edge.
   * @return true, if the {@link SMGEdgePointsTo} edge with the source
   * {@link value} exists, otherwise false.
   */
  public boolean isPointer(final Integer value) {
    return ptEdges.containsKey(value);
  }

  /**
   * Returns the {@link SMGEdgePointsTo} edge with the
   * given value as source.
   *
   * @param value the source of the {@link SMGEdgePointsTo} edge.
   * @return the {@link SMGEdgePointsTo} edge with the
   * {@link value} as source.
   * @throws SMGInconsistentException
   */
  public SMGEdgePointsTo getPointer(final Integer value) {
    if (ptEdges.containsKey(value)) {
      return ptEdges.get(value);
    }

    throw new IllegalArgumentException("Asked for a PT edge of a non-pointer");
  }

  public boolean isCoveredByNullifiedBlocks(final SMGEdgeHasValue pEdge) {
    return isCoveredByNullifiedBlocks(pEdge.getObject(), pEdge.getOffset(), pEdge.getSizeInBytes());
  }

  public boolean isCoveredByNullifiedBlocks(final SMGObject pObject, final int pOffset, final CType pType) {
    return isCoveredByNullifiedBlocks(pObject, pOffset, pType.getSize());
  }

  private boolean isCoveredByNullifiedBlocks(final SMGObject pObject, final int pOffset, final int size) {
    BitSet objectNullBytes = getNullBytesForObject(pObject);
    int expectedMinClear = pOffset + size;

    return (objectNullBytes.nextClearBit(pOffset) >= expectedMinClear);
  }

  public void mergeValues(final int pV1, final int pV2) {
    if (pV1 == pV2) {
      return;
    }

    if (pV2 == NULL_ADDRESS) {
      mergeValues(pV2, pV1);
    }

    neq.mergeValues(pV1, pV2);
    removeValue(pV2);
    HashSet<SMGEdgeHasValue> newHvEdges = new HashSet<>();
    for (SMGEdgeHasValue hv : hvEdges) {
      if (hv.getValue() != pV2) {
        newHvEdges.add(hv);
      } else {
        newHvEdges.add(new SMGEdgeHasValue(hv.getSizeInBytes(), hv.getOffset(), hv.getObject(), pV1));
      }
    }
    hvEdges.clear();
    hvEdges.addAll(newHvEdges);
  }

  public boolean haveNeqRelation(final Integer pV1, final Integer pV2) {
    return neq.neqExists(pV1, pV2);
  }

  public Set<Integer> getNeqsForValue(final Integer pV) {
    return neq.getNeqsForValue(pV);
  }

  public void putExplicit(final SMGKnownSymValue pKey, final SMGKnownExpValue pValue) {
    explicitValues.put(pKey, pValue);
  }

  public SMGExplicitValue getExplicit(final SMGKnownSymValue pKey) {
    if (explicitValues.containsKey(pKey)) {
      return explicitValues.get(pKey);
    }
    return SMGUnknownValue.getInstance();
  }

  public void clearExplicit(final SMGKnownSymValue pKey) {
    explicitValues.remove(pKey);
  }

  public boolean isIdenticalTo(final SMG pOther) {
    return (objects.equals(pOther.objects)) && (values.equals(pOther.values)) && (hvEdges.equals(pOther.hvEdges)) &&
           (ptEdges.equals(pOther.ptEdges)) && (objectValidity.equals(pOther.objectValidity)) &&
           (neq.equals(pOther.neq)) && (explicitValues.equals(pOther.explicitValues));
  }
}

final class NeqRelation {

  /**
   * The Multimap is used as Bi-Map, i.e. each pair (K,V) is also inserted as
   * pair (V,K). We avoid self-references like (A,A).
   */
  private final SetMultimap<Integer, Integer> smgValues = HashMultimap.create();

  @Override
  public int hashCode() {
    return smgValues.hashCode();
  }

  public Set<Integer> getNeqsForValue(final Integer pV) {
    return Collections.unmodifiableSet(smgValues.get(pV));
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    NeqRelation other = (NeqRelation) obj;
    return other.smgValues != null && smgValues.equals(other.smgValues);
  }

  public void addRelation(final Integer pOne, final Integer pTwo) {

    // we do not want self-references
    if (pOne.intValue() == pTwo.intValue()) {
      return;
    }

    smgValues.put(pOne, pTwo);
    smgValues.put(pTwo, pOne);
  }

  public void putAll(final NeqRelation pNeq) {
    smgValues.putAll(pNeq.smgValues);
  }

  public void removeRelation(final Integer pOne, final Integer pTwo) {
    smgValues.remove(pOne, pTwo);
    smgValues.remove(pTwo, pOne);
  }

  public boolean neqExists(final Integer pOne, final Integer pTwo) {
    return smgValues.containsEntry(pOne, pTwo);
  }

  public void removeValue(final Integer pOne) {
    for (Integer other : smgValues.get(pOne)) {
      smgValues.get(other).remove(pOne);
    }
    smgValues.removeAll(pOne);
  }

  /** transform all relations from (A->C) towards (A->B) and delete C */
  public void mergeValues(final Integer pB, final Integer pC) {
    List<Integer> values = ImmutableList.copyOf(smgValues.get(pC));
    removeValue(pC);
    for (Integer value : values) {
      addRelation(pB, value);
    }
  }

  @Override
  public String toString() {
    return "neq_rel=" + smgValues.toString();
  }
}