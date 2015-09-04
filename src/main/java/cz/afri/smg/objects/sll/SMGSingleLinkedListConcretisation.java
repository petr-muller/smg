/**
 * Created by Viktor Malik on 3.9.2015.
 */

package cz.afri.smg.objects.sll;

import cz.afri.smg.SMGConcretisation;
import cz.afri.smg.graphs.*;
import cz.afri.smg.objects.SMGRegion;
import cz.afri.smg.types.CPointerType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.HashMap;
import java.util.Map;

public class SMGSingleLinkedListConcretisation implements SMGConcretisation {

    private final SMGSingleLinkedList sll;

    public SMGSingleLinkedListConcretisation(final SMGSingleLinkedList pSll) {
        sll = pSll;
    }

    @SuppressFBWarnings(value = "WMI_WRONG_MAP_ITERATOR", justification = "We need to iterate over keys here")
    @Override
    public final ReadableSMG execute(final ReadableSMG pSMG) {
        WritableSMG newSMG = SMGFactory.createWritableCopy(pSMG);

        // Create new concrete object
        SMGRegion region = new SMGRegion(sll.getSize(), sll.getLabel() + "_element");

        // Replace all edges pointing to SLL with ones pointing to new region
        Map<SMGEdgePointsTo, SMGEdgePointsTo> toReplace = new HashMap<>();
        for (SMGEdgePointsTo pt : newSMG.getPTEdges()) {
            if (pt.getObject().equals(sll)) {
                SMGEdgePointsTo newPt = new SMGEdgePointsTo(pt.getValue(), region, sll.getOffset());
                toReplace.put(pt, newPt);
            }
        }

        for (SMGEdgePointsTo pt : toReplace.keySet()) {
            newSMG.removePointsToEdge(pt.getValue());
            newSMG.addPointsToEdge(toReplace.get(pt));
        }

        // Create new connection between new region and SLL
        Integer value = SMGValueFactory.getNewValue();
        newSMG.addValue(value);
        SMGEdgeHasValue hv = new SMGEdgeHasValue(new CPointerType(), sll.getOffset(), region, value);
        newSMG.addHasValueEdge(hv);
        SMGEdgePointsTo pt = new SMGEdgePointsTo(value, sll, sll.getOffset());
        newSMG.addPointsToEdge(pt);

        // Shorten SLL
        if (sll.getLength() > 0) {
            sll.addLength(-1);
        }

        return  newSMG;
    }

    public final SMGSingleLinkedList getSll() {
        return sll;
    }
}
