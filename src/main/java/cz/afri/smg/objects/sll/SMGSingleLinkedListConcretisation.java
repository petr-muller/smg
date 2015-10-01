/**
 * Created by Viktor Malik on 3.9.2015.
 */

package cz.afri.smg.objects.sll;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import cz.afri.smg.SMGConcretisation;
import cz.afri.smg.graphs.*;
import cz.afri.smg.objects.SMGRegion;
import cz.afri.smg.types.CPointerType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class SMGSingleLinkedListConcretisation implements SMGConcretisation {

    private final SMGSingleLinkedList sll;

    public SMGSingleLinkedListConcretisation(final SMGSingleLinkedList pSll) {
        sll = pSll;
    }

    @SuppressFBWarnings(value = "WMI_WRONG_MAP_ITERATOR", justification = "We need to iterate over keys here")
    @Override
    public final HashSet<ReadableSMG> execute(final ReadableSMG pSMG) {
        HashSet<ReadableSMG> resultSet = new HashSet<>();

        WritableSMG newSMG = SMGFactory.createWritableCopy(pSMG);

        // Create new concrete object
        SMGRegion region = new SMGRegion(sll.getSize(), sll.getLabel() + "_element");
        newSMG.addHeapObject(region);

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
        Integer newValue = SMGValueFactory.getNewValue();
        newSMG.addValue(newValue);
        SMGEdgeHasValue newValueHv = new SMGEdgeHasValue(new CPointerType(), sll.getOffset(), region, newValue);
        newSMG.addHasValueEdge(newValueHv);
        SMGEdgePointsTo newValuePt = new SMGEdgePointsTo(newValue, sll, sll.getOffset());
        newSMG.addPointsToEdge(newValuePt);

        if (sll.getLength() > 0) {
            // Shorten SLL
            sll.addLength(-1);
        } else {
            // For SLL of length 0+, there is a case, when it had length 0 and can be removed though
            WritableSMG newSMGWithoutSll = SMGFactory.createWritableCopy(pSMG);

            Integer value;
            if (Iterables.isEmpty(newSMGWithoutSll.getHVEdges(SMGEdgeHasValueFilter.objectFilter(sll)
                    .filterAtOffset(sll.getOffset()).filterByType(CPointerType.getVoidPointer())))) {
                // Create new value
                value = SMGValueFactory.getNewValue();
                newSMGWithoutSll.addValue(value);
            } else {
                // Get outbound edge at binding offset (next pointer of last list item)
                value = newSMGWithoutSll.getUniqueHV(SMGEdgeHasValueFilter.objectFilter(sll)
                        .filterAtOffset(sll.getOffset()).filterByType(CPointerType.getVoidPointer()), false)
                        .getValue();
            }

            for (SMGEdgePointsTo pt : newSMGWithoutSll.getPTEdges()){
                if (pt.getObject().equals(sll)) {
                    SMGEdgeHasValue oldHv = newSMGWithoutSll.getUniqueHV(new SMGEdgeHasValueFilter().filterHavingValue(pt.getValue()), false);
                    SMGEdgeHasValue newHv = new SMGEdgeHasValue(CPointerType.getVoidPointer(), sll.getOffset(), oldHv.getObject(), value);
                    newSMGWithoutSll.addHasValueEdge(newHv);
                    newSMGWithoutSll.removeHasValueEdge(oldHv);
                    newSMGWithoutSll.removePointsToEdge(oldHv.getValue());
                    newSMGWithoutSll.removeValue(oldHv.getValue());
                }
            }

            // Remove SLL and all appropriate edges
            HashSet<SMGEdgeHasValue> toRemove = Sets.newHashSet(newSMGWithoutSll.getHVEdges(SMGEdgeHasValueFilter.objectFilter(sll)));
            for (SMGEdgeHasValue hv : toRemove) {
                newSMGWithoutSll.removeHasValueEdge(hv);
            }
            newSMGWithoutSll.removeHeapObject(sll);

            resultSet.add(newSMGWithoutSll);
        }

        resultSet.add(newSMG);

        return  resultSet;
    }

    public final SMGSingleLinkedList getSll() {
        return sll;
    }
}
