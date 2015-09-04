/**
 * Created by Viktor Malik on 3.9.2015.
 */

package cz.afri.smg.objects.sll;

import cz.afri.smg.graphs.*;
import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.objects.SMGRegion;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class SMGSingleLinkedListConcretisationTest {

    private static final int SIZE8 = 8;
    private static final int SIZE16 = 16;

    @Test
    public final void basicTest() {
        SMGRegion region = new SMGRegion(SIZE8, "prototype");
        final int offset4 = 4;
        SMGSingleLinkedList sll = new SMGSingleLinkedList(region, offset4, 3);
        SMGSingleLinkedListConcretisation concretisation = new SMGSingleLinkedListConcretisation(sll);

        Assert.assertSame(sll, concretisation.getSll());
    }

    @Test
    public final void executeOnSimpleList() {
        WritableSMG smg = SMGFactory.createWritableSMG();

        final int listLength = 6;
        final int offset = 8;

        SMGEdgeHasValue pointer = TestHelpers.createGlobalSll(smg, listLength, SIZE16, offset, "pointer");

        Integer value = pointer.getValue();
        SMGSingleLinkedList sll = (SMGSingleLinkedList) smg.getPointer(value).getObject();
        SMGSingleLinkedListConcretisation concretisation = new SMGSingleLinkedListConcretisation(sll);

        ReadableSMG concretisedSmg = concretisation.execute(smg);
        // Test heap size
        Set<SMGObject> heap = concretisedSmg.getHeapObjects();
        final int expectedHeapSize = 2;
        Assert.assertEquals(expectedHeapSize, heap.size());
        // Test creation of concrete region
        SMGObject pointedObject = concretisedSmg.getPointer(value).getObject();
        Assert.assertTrue(pointedObject instanceof SMGRegion);
        Assert.assertFalse(pointedObject.isAbstract());
        // Test existence of new value with correct edges
        SMGEdgeHasValue newHv = concretisedSmg.getUniqueHV(
                SMGEdgeHasValueFilter.objectFilter(pointedObject), true);
        Assert.assertEquals(offset, newHv.getOffset());
        Integer newValue = newHv.getValue();
        Assert.assertNotNull(newValue);
        SMGEdgePointsTo newPt = concretisedSmg.getPointer(newValue);
        Assert.assertEquals(offset, newPt.getOffset());

        // Test edited SLL
        SMGObject newValueObj = newPt.getObject();
        Assert.assertTrue(newValueObj instanceof SMGSingleLinkedList);
        SMGSingleLinkedList editedSll = (SMGSingleLinkedList) newValueObj;
        Assert.assertEquals(sll, editedSll);
        Assert.assertEquals(editedSll.getLength(), listLength - 1);
        Assert.assertEquals(offset, editedSll.getOffset());
        Assert.assertEquals(sll.getSize(), editedSll.getSize());
    }
}
