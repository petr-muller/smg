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

import java.util.ArrayList;

import com.google.common.collect.Iterables;

import cz.afri.smg.objects.SMGObject;

final class SMGConsistencyVerifier {
  private SMGConsistencyVerifier() { } /* utility class */

  /**
   * Simply log a message about a result of a single check.
   *
   * @param pResult A result of the check
   * @param pLogger A logger instance to which the message will be sent
   * @param pMessage A message to log
   * @return True, if the check was successful (equivalent to { @link pResult }
   */
  private static boolean verifySMGProperty(final boolean pResult, final String pMessage) {
    return pResult;
  }

  /**
   * A consistency checks related to the NULL object
   *
   * @param pLogger A logger to record results
   * @param pSmg A SMG to verify
   * @return True, if {@link pSmg} satisfies all consistency criteria
   */
  private static boolean verifyNullObject(final SMG pSmg) {
    Integer nullValue = null;

    // Find a null value in values
    for (Integer value: pSmg.getValues()) {
      if (pSmg.getObjectPointedBy(value) == pSmg.getNullObject()) {
        nullValue = value;
        break;
      }
    }

    // Verify that one value pointing to NULL object is present in values
    if (nullValue == null) {
      // pLogger.log(Level.SEVERE, "SMG inconsistent: no value pointing to null object");
      return false;
    }

    // Verify that NULL value returned by getNullValue() points to NULL object
    if (pSmg.getObjectPointedBy(pSmg.getNullValue()) != pSmg.getNullObject()) {
      // pLogger.log(Level.SEVERE, "SMG inconsistent: null value not pointing to null object");
      return false;
    }

    // Verify that the value found in values is the one returned by getNullValue()
    if (pSmg.getNullValue() != nullValue) {
      // pLogger.log(Level.SEVERE, "SMG inconsistent: null value in values set not returned by getNullValue()");
      return false;
    }

    // Verify that NULL object has no value
    SMGEdgeHasValueFilter filter = SMGEdgeHasValueFilter.objectFilter(pSmg.getNullObject());

    if (pSmg.getHVEdges(filter).iterator().hasNext()) {
      // pLogger.log(Level.SEVERE, "SMG inconsistent: null object has some value");
      return false;
    }

    // Verify that the NULL object is invalid
    if (pSmg.isObjectValid(pSmg.getNullObject())) {
//      pLogger.log(Level.SEVERE, "SMG inconsistent: null object is not invalid");
      return false;
    }

    // Verify that the size of the NULL object is zero
    if (pSmg.getNullObject().getSize() != 0) {
//      pLogger.log(Level.SEVERE, "SMG inconsistent: null object does not have zero size");
      return false;
    }

    return true;
  }

  /**
   * Verifies that invalid regions do not have any Has-Value edges, as this
   * is forbidden in consistent SMGs
   *
   * @param pLogger A logger to record results
   * @param pSmg A SMG to verify
   * @return True, if {@link pSmg} satisfies all consistency criteria.
   */
  private static boolean verifyInvalidRegionsHaveNoHVEdges(final SMG pSmg) {
    for (SMGObject obj : pSmg.getObjects()) {
      if (pSmg.isObjectValid(obj)) {
        continue;
      }
      // Verify that the HasValue edge set for this invalid object is empty
      SMGEdgeHasValueFilter filter = SMGEdgeHasValueFilter.objectFilter(obj);

      if (pSmg.getHVEdges(filter).iterator().hasNext()) {
//        pLogger.log(Level.SEVERE, "SMG inconsistent: invalid object has a HVEdge");
        return false;
      }
    }

    return true;
  }

  /**
   * Verifies that any fields (as designated by Has-Value edges) do not
   * exceed the boundary of the object.
   *
   * @param pLogger A logger to record results
   * @param pObject An object to verify
   * @param pSmg A SMG to verify
   * @return True, if {@link pObject} in {@link pSmg} satisfies all consistency criteria. False otherwise.
   */
  private static boolean checkSingleFieldConsistency(final SMGObject pObject, final SMG pSmg) {

    // For all fields in the object, verify that sizeof(type)+field_offset < object_size
    SMGEdgeHasValueFilter filter = SMGEdgeHasValueFilter.objectFilter(pObject);

    for (SMGEdgeHasValue hvEdge : pSmg.getHVEdges(filter)) {
      if ((hvEdge.getOffset() + hvEdge.getSizeInBytes()) > pObject.getSize()) {
//        pLogger.log(Level.SEVERE, "SMG inconistent: field exceedes boundary of the object");
//        pLogger.log(Level.SEVERE, "Object: ", pObject);
//        pLogger.log(Level.SEVERE, "Field: ", hvEdge);
        return false;
      }
    }
    return true;
  }

  /**
   * Verify all objects satisfy the Field Consistency criteria
   * @param pLogger A logger to record results
   * @param pSmg A SMG to verify
   * @return True, if {@link pSmg} satisfies all consistency criteria. False otherwise.
   */
  private static boolean verifyFieldConsistency(final SMG pSmg) {
    for (SMGObject obj : pSmg.getObjects()) {
      if (!checkSingleFieldConsistency(obj, pSmg)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Verify that the edges are consistent in the SMG
   *
   * @param pLogger A logger to record results
   * @param pSmg A SMG to verify
   * @param pEdges A set of edges for consistency verification
   * @return True, if all edges in {@link pEdges} satisfy consistency criteria. False otherwise.
   */
  private static boolean verifyEdgeConsistency(final SMG pSmg, final Iterable<? extends SMGEdge> pEdges) {
    ArrayList<SMGEdge> toVerify = new ArrayList<>();
    Iterables.addAll(toVerify, pEdges);

    while (toVerify.size() > 0) {
      SMGEdge edge = toVerify.get(0);
      toVerify.remove(0);

      // Verify that the object assigned to the edge exists in the SMG
      if (!pSmg.getObjects().contains(edge.getObject())) {
//        pLogger.log(Level.SEVERE, "SMG inconsistent: Edge from a nonexistent object");
//        pLogger.log(Level.SEVERE, "Edge :", edge);
        return false;
      }

      // Verify that the value assigned to the edge exists in the SMG
      if (!pSmg.getValues().contains(edge.getValue())) {
//        pLogger.log(Level.SEVERE, "SMG inconsistent: Edge to a nonexistent value");
//        pLogger.log(Level.SEVERE, "Edge :", edge);
        return false;
      }

      // Verify that the edge is consistent to all remaining edges
      //  - edges of different type are inconsistent
      //  - two Has-Value edges are inconsistent iff:
      //    - leading from the same object AND
      //    - have same type AND
      //    - have same offset AND
      //    - leading to DIFFERENT values
      //  - two Points-To edges are inconsistent iff:
      //    - different values point to same place (object, offset)
      //    - same values do not point to the same place
      for (SMGEdge otherOdge : toVerify) {
        if (!edge.isConsistentWith(otherOdge)) {
//          pLogger.log(Level.SEVERE, "SMG inconsistent: inconsistent edges");
//          pLogger.log(Level.SEVERE, "First edge:  ", edge);
//          pLogger.log(Level.SEVERE, "Second edge: ", other_edge);
          return false;
        }
      }
    }
    return true;
  }

  private static boolean verifyObjectConsistency(final SMG pSmg) {
    for (SMGObject obj : pSmg.getObjects()) {
      try {
        pSmg.isObjectValid(obj);
      } catch (IllegalArgumentException e) {
//        pLogger.log(Level.SEVERE, "SMG inconsistent: object does not have validity");
        return false;
      }

      if (obj.getSize() < 0) {
//        pLogger.log(Level.SEVERE, "SMG inconsistent: object with size lower than 0");
        return false;
      }

      if ((!pSmg.isObjectValid(obj)) && obj.isAbstract()) {
//        pLogger.log(Level.SEVERE, "SMG inconsistent: abstract object is invalid");
//        pLogger.log(Level.SEVERE, obj);
        return false;
      }
    }
    return true;
  }

  //TODO: NEQ CONSISTENCY

  /**
   * Verify a single SMG if it meets all consistency criteria.
   * @param pLogger A logger to record results
   * @param pSmg A SMG to verify
   * @return True, if {@link pSmg} satisfies all consistency criteria
   */
  public static boolean verifySMG(final SMG pSmg) {
    boolean toReturn = true;
//    pLogger.log(Level.FINEST, "Starting constistency check of a SMG");

    toReturn = toReturn && verifySMGProperty(
        verifyNullObject(pSmg), "null object invariants hold");
    toReturn = toReturn && verifySMGProperty(
        verifyInvalidRegionsHaveNoHVEdges(pSmg), "invalid regions have no outgoing edges");
    toReturn = toReturn && verifySMGProperty(
        verifyFieldConsistency(pSmg), "field consistency");
    toReturn = toReturn && verifySMGProperty(
        verifyEdgeConsistency(pSmg, pSmg.getHVEdges()), "Has Value edge consistency");
    toReturn = toReturn && verifySMGProperty(
        verifyEdgeConsistency(pSmg, pSmg.getPTEdges()), "Points To edge consistency");
    toReturn = toReturn && verifySMGProperty(
        verifyObjectConsistency(pSmg), "Validity consistency");

//    pLogger.log(Level.FINEST, "Ending consistency check of a SMG");

    return toReturn;
  }
}