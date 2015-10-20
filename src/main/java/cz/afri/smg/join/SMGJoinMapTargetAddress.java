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
package cz.afri.smg.join;

import cz.afri.smg.graphs.ReadableSMG;
import cz.afri.smg.graphs.SMGEdgePointsTo;
import cz.afri.smg.graphs.SMGValueFactory;
import cz.afri.smg.graphs.WritableSMG;
import cz.afri.smg.objects.SMGObject;

final class SMGJoinMapTargetAddress {
  private WritableSMG smg;
  private SMGNodeMapping mapping1;
  private SMGNodeMapping mapping2;
  private Integer value;

  public SMGJoinMapTargetAddress(final ReadableSMG pSMG1, final ReadableSMG pSMG2, final WritableSMG destSMG,
                                 final SMGNodeMapping pMapping1, final SMGNodeMapping pMapping2,
                                 final Integer pAddress1, final Integer pAddress2) {
    smg = destSMG;
    mapping1 = pMapping1;
    mapping2 = pMapping2;
    SMGObject target = destSMG.getNullObject();

    // TODO: Ugly, refactor
    SMGEdgePointsTo pt = pSMG1.getPointer(pAddress1);
    if (pt.getObject().notNull()) {
      target = pMapping1.get(pt.getObject());
    }

    // TODO: Ugly, refactor
    Iterable<SMGEdgePointsTo> edges = smg.getPTEdges();
    for (SMGEdgePointsTo edge : edges) {
      if ((edge.getObject() == target) &&
          (edge.getOffset() == pt.getOffset())) {
        value = edge.getValue();
        return;
      }
    }

    value = SMGValueFactory.getNewValue();
    smg.addValue(value);
    smg.addPointsToEdge(new SMGEdgePointsTo(value, target, pt.getOffset()));
    mapping1.map(pAddress1, value);
    mapping2.map(pAddress2, value);
  }

  public WritableSMG getSMG() {
    return smg;
  }

  public SMGNodeMapping getMapping1() {
    return mapping1;
  }

  public SMGNodeMapping getMapping2() {
    return mapping2;
  }

  public Integer getValue() {
    return value;
  }
}
