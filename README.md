## Synopsis

SMG is a Java library implementing the abstract domain of **symbolic memory graphs**. The library is supposed to be
used by static analyzers for C programs, especially those that need to deal with pointer-rich programs and those working
with unbounded dynamic data structures.

## Code Example
```java
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
```

## Motivation

The initial version of SMG library was developed within the [CPAChecker](http://cpachecker.sosy-lab.org/) project, after
the original SMG implementation in [Predator](http://www.fit.vutbr.cz/research/groups/verifit/tools/predator/) was too
hard to extend. SMG library incubated in CPAChecker, closely developed alongside the program analysis for pointer
programs. Ultimately we decided to separate the domain from the analysis for reuse within other verification frameworks
and algorithms than Configurable Program Analysis.

## Installation

No easy way yet. You need build the JAR with `gradle build` :)

## API Reference

No link yet. You need to build the Javadoc with `gradle javadoc` :)


## Tests

You can run all unit-tests together with FindBugs and Checkstyle using `gradle check`.

[![Build Status](https://travis-ci.org/petr-muller/smg.svg?branch=master)](https://travis-ci.org/petr-muller/smg)

## License

The project is distributed under [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
