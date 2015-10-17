import java.io.IOException;

import com.google.common.collect.ImmutableList;

import cz.afri.smg.graphs.SMGFactory;
import cz.afri.smg.graphs.SMGPlotter;
import cz.afri.smg.graphs.WritableSMG;
import cz.afri.smg.objects.SMGRegion;
import cz.afri.smg.types.CFunctionDeclaration;
import cz.afri.smg.types.CFunctionType;
import cz.afri.smg.types.CType;

public class Example01 {

  /**
   * SMG Example 1: Adding global, stack, and heap objects to SMG
   * @param args
   */
  public static void main(String[] args) {
    // First, we use a SMGFactory method to create a new, writable, empty SMG
    WritableSMG smg = SMGFactory.createWritableSMG();

    // We will add integer variables/heap objects
    CType integerType = CType.getIntType();

    // Adds a global variable to SMG of integer type, called 'global_integer'
    smg.addGlobalVariable(integerType, "global_integer");

    // Creates a function type for function 'main': returns integer, no parameters
    CFunctionType mainFunctionType = CFunctionType.createSimpleFunctionType(integerType);
    // Creates a function declaration for function main(): int main() 
    smg.addStackFrame(new CFunctionDeclaration(mainFunctionType, "main", ImmutableList.of()));

    // Adds a local variable of integer type to SMG, called 'local_integer'
    // The variable is implicitly added to last stack frame
    smg.addLocalVariable(integerType, "local_integer");

    // Creates an object with size 8b, and label 'heap_integer'
    SMGRegion heap_object = new SMGRegion(8, "heap_integer");
    // Adds this object to the SMG, on heap
    smg.addHeapObject(heap_object);

    // Plot the SMG to stdout
    try {
      SMGPlotter.debuggingPlot(smg, "example-01");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
