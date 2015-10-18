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

public class Example02 {
  // We will add integer and pointer variables, so we need appropriate types
  private static CType integerType = CType.getIntType();
  private static CType pointerType = CType.getPointerType();

  private static void createMainFunction(WritableSMG pSmg) {
    // Creates a function type for function 'main': returns integer, no parameters
    CFunctionType mainFunctionType = CFunctionType.createSimpleFunctionType(integerType);
    // Creates a function declaration for function main(): int main()
    pSmg.addStackFrame(new CFunctionDeclaration(mainFunctionType, "main", ImmutableList.of()));
  }

  /**
   * SMG Example 2: Adding values and edges to the SMG
   * 
   * @param args
   */
  public static void main(String[] args) {
    // First, we use a SMGFactory method to create a new, writable, empty SMG
    WritableSMG smg = SMGFactory.createWritableSMG();

    createMainFunction(smg);

    // Adds a global variable to SMG of integer type, called 'global_integer'
    SMGRegion global_integer = smg.addGlobalVariable(integerType, "global_integer");

    // Adds a local variable of pointer type to SMG, called 'local_integer'
    // The variable is implicitly added to last stack frame
    SMGRegion local_pointer = smg.addLocalVariable(pointerType, "local_pointer");

    // Creates an object with size 8b, and label 'heap_integer'
    SMGRegion heap_object = new SMGRegion(8, "heap_integer");
    // Adds this object to the SMG, on heap
    smg.addHeapObject(heap_object);

    // Assigns 'global_integer' object a symbolic value. Symbolic value is created and added to the SMG. Then, an
    // Appropriate has-value edge is created and added to the SMG.
    Integer integerValue = SMGValueFactory.getNewValue();
    smg.addValue(integerValue);
    smg.addHasValueEdge(new SMGEdgeHasValue(integerType, 0, global_integer, integerValue));

    // Assigns 'local_pointer' a symbolic value, which is also a pointer to a 'heap_integer'. The symbolic value and
    // has-value edge are created and added to the SMG. Additionally, a points-to edge is added to the SMG, leading from
    // symbolic value to 'heap_integer' object.
    Integer pointerValue = SMGValueFactory.getNewValue();
    smg.addValue(pointerValue);
    smg.addHasValueEdge(new SMGEdgeHasValue(pointerType, 0, local_pointer, pointerValue));
    smg.addPointsToEdge(new SMGEdgePointsTo(pointerValue, heap_object, 0));

    // Plot the SMG to stdout
    try {
      SMGPlotter.debuggingPlot(smg, "example-02");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
