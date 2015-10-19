import java.io.IOException;

import com.google.common.collect.ImmutableList;

import cz.afri.smg.graphs.SMGEdgeHasValue;
import cz.afri.smg.graphs.SMGEdgePointsTo;
import cz.afri.smg.graphs.SMGFactory;
import cz.afri.smg.graphs.SMGPlotter;
import cz.afri.smg.graphs.SMGValueFactory;
import cz.afri.smg.graphs.WritableSMG;
import cz.afri.smg.objects.SMGRegion;
import cz.afri.smg.types.CFunctionDeclaration;
import cz.afri.smg.types.CFunctionType;
import cz.afri.smg.types.CType;

public class Example03 {
  // We will add integer and pointer variables, so we need appropriate types
  private static CType integerType = CType.getIntType();
  private static CType pointerType = CType.getPointerType();

  // Creates a function type for function 'main': returns integer, no parameters
  private static CFunctionType mainFunctionType = CFunctionType.createSimpleFunctionType(integerType);

  private static WritableSMG smg = SMGFactory.createWritableSMG();

  private static void createMainFunction(WritableSMG pSmg) {
    // Creates a function declaration for function main(): int main()
    pSmg.addStackFrame(new CFunctionDeclaration(mainFunctionType, "main", ImmutableList.of()));
  }

  /**
   * SMG Example 3: Adding values and edges to the SMG
   * 
   * @param args
   */
  public static void main(String[] args) {
    createMainFunction(smg);
    smg.addStackFrame(new CFunctionDeclaration(mainFunctionType, "newFunction", ImmutableList.of()));

    // Adds a local variable of pointer type to SMG, called 'local_integer'
    // The variable is implicitly added to last stack frame
    SMGRegion local_pointer = smg.addLocalVariable(pointerType, "local_pointer");

    // Creates an object with size 8b, and label 'heap_integer'
    SMGRegion heap_object = new SMGRegion(8, "heap_integer");
    // Adds this object to the SMG, on heap
    smg.addHeapObject(heap_object);

    // Assigns 'local_pointer' a symbolic value, which is also a pointer to a 'heap_integer'. The symbolic value and
    // has-value edge are created and added to the SMG. Additionally, a points-to edge is added to the SMG, leading from
    // symbolic value to 'heap_integer' object.
    Integer pointerValue = SMGValueFactory.getNewValue();
    smg.addValue(pointerValue);
    smg.addHasValueEdge(new SMGEdgeHasValue(pointerType, 0, local_pointer, pointerValue));
    smg.addPointsToEdge(new SMGEdgePointsTo(pointerValue, heap_object, 0));

    try { SMGPlotter.debuggingPlot(smg, "example-03-1-before-drop"); } catch (IOException e) { e.printStackTrace(); }

    smg.dropStackFrame();
    System.out.println("Leaks immediately after dropping the stack: " + smg.hasMemoryLeaks());
    try { SMGPlotter.debuggingPlot(smg, "example-03-2-after-drop"); } catch (IOException e) { e.printStackTrace(); }

    smg.pruneUnreachable();
    System.out.println("Leaks after pruneUnreachable(): " + smg.hasMemoryLeaks());
    try { SMGPlotter.debuggingPlot(smg, "example-03-3-after-prune"); } catch (IOException e) { e.printStackTrace(); }
  }
}
