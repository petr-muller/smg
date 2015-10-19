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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import cz.afri.smg.graphs.SMGValues.SMGExplicitValue;
import cz.afri.smg.graphs.SMGValues.SMGKnownSymValue;
import cz.afri.smg.objects.SMGObject;
import cz.afri.smg.objects.SMGObjectVisitor;
import cz.afri.smg.objects.SMGRegion;
import cz.afri.smg.objects.sll.SMGSingleLinkedList;
import cz.afri.smg.objects.tree.SimpleBinaryTree;

final class SMGObjectNode {
  private final String name;
  private final String definition;
  private static int counter = 0;

  public SMGObjectNode(final String pType, final String pDefinition) {
    name = "node_" + pType + "_" + counter++;
    definition = pDefinition;
  }

  public SMGObjectNode(final String pName) {
    name = pName;
    definition = null;
  }

  public String getName() {
    return name;
  }

  public String getDefinition() {
    return name + "[" + definition + "];";
  }
}

class SMGNodeDotVisitor extends SMGObjectVisitor {
  private final ReadableSMG smg;
  private SMGObjectNode node = null;

  public SMGNodeDotVisitor(final ReadableSMG pSmg) {
    smg = pSmg;
  }

  private String defaultDefinition(final String pColor, final String pShape, final String pStyle,
                                   final SMGObject pObject) {
    return "color=" + pColor + ", shape=" + pShape + ", style=" + pStyle + ", label =\"" + pObject.toString() + "\"";
  }

  @Override
  public void visit(final SMGRegion pRegion) {
    String shape = "rectangle";
    String color;
    String style;
    if (smg.isObjectValid(pRegion)) {
      color = "black"; style = "solid";
    } else {
      color = "red"; style = "dotted";
    }

    node = new SMGObjectNode("region", defaultDefinition(color, shape, style, pRegion));
  }

  @Override
  public void visit(final SMGSingleLinkedList pSll) {
    String shape = "rectangle";
    String color = "blue";

    String style = "dashed";
    node = new SMGObjectNode("sll", defaultDefinition(color, shape, style, pSll));
  }

  @Override
  public void visit(final SimpleBinaryTree pTree) {
    String shape = "rectangle";
    String color = "green";

    String style = "dashed";
    node = new SMGObjectNode("tree", defaultDefinition(color, shape, style, pTree));
  }

  @Override
  public void visit(final SMGObject pObject) {
    if (pObject.notNull()) {
      pObject.accept(this);
    } else {
      node = new SMGObjectNode("NULL");
    }
  }

  public SMGObjectNode getNode() {
    return node;
  }
}

public final class SMGPlotter {
  public static void debuggingPlot(final ReadableSMG pSmg, final String pId) throws IOException {
    SMGPlotter plotter = new SMGPlotter();
    PrintWriter writer = new PrintWriter(pId + ".dot", "UTF-8");
    writer.write(plotter.smgAsDot(pSmg, pId, pId));
    writer.close();
  }

  private final HashMap <SMGObject, SMGObjectNode> objectIndex = new HashMap<>();
  private static int nulls = 0;
  private int offset = 0;

  public SMGPlotter() { } /* utility class */

  public static String convertToValidDot(final String original) {
    return original.replaceAll("[:]", "_");
  }

  public String smgAsDot(final ReadableSMG smg, final String name, final String location) {
    StringBuilder sb = new StringBuilder();

    sb.append("digraph gr_" + name.replace('-', '_') + "{\n");
    offset += 2;
    sb.append(newLineWithOffset("label = \"Location: " + location.replace("\"", "\\\"") + "\";"));

    addStackSubgraph(smg, sb);

    SMGNodeDotVisitor visitor = new SMGNodeDotVisitor(smg);

    for (SMGObject heapObject : smg.getHeapObjects()) {
      if (!objectIndex.containsKey(heapObject)) {
        visitor.visit(heapObject);
        objectIndex.put(heapObject, visitor.getNode());
      }
      if (heapObject.notNull()) {
        sb.append(newLineWithOffset(objectIndex.get(heapObject).getDefinition()));
      }
    }

    addGlobalObjectSubgraph(smg, sb);

    for (Integer value : smg.getValues()) {
      if (value != smg.getNullValue()) {
        SMGExplicitValue explicitValue = smg.getExplicit(SMGKnownSymValue.valueOf(value));
        String explicitValueString;
        if (explicitValue.isUnknown()) {
          explicitValueString = "";
        } else {
          explicitValueString = " : " + String.valueOf(explicitValue.getAsLong());
        }
        sb.append(newLineWithOffset(smgValueAsDot(value, explicitValueString)));
      }
    }

    for (SMGEdgeHasValue edge: smg.getHVEdges()) {
      sb.append(newLineWithOffset(smgHVEdgeAsDot(edge)));
    }

    for (SMGEdgePointsTo edge: smg.getPTEdges()) {
      if (edge.getValue() != smg.getNullValue()) {
        sb.append(newLineWithOffset(smgPTEdgeAsDot(edge)));
      }
    }

    sb.append("}");

    return sb.toString();
  }

  private void addStackSubgraph(final ReadableSMG pSmg, final StringBuilder pSb) {
    pSb.append(newLineWithOffset("subgraph cluster_stack {"));
    offset += 2;
    pSb.append(newLineWithOffset("label=\"Stack\";"));

    int i = pSmg.getStackFrames().size();
    for (CLangStackFrame stackItem : pSmg.getStackFrames()) {
      addStackItemSubgraph(stackItem, pSb, i);
      i--;
    }
    offset -= 2;
    pSb.append(newLineWithOffset("}"));
  }

  private void addStackItemSubgraph(final CLangStackFrame pStackFrame, final StringBuilder pSb, final int pIndex) {
    pSb.append(newLineWithOffset("subgraph cluster_stack_" + pStackFrame.getFunctionDeclaration().getName() + "{"));
    offset += 2;
    pSb.append(newLineWithOffset("fontcolor=blue;"));
    pSb.append(newLineWithOffset("label=\"#" + pIndex + ": " + pStackFrame.getFunctionDeclaration().toString() +
                                 "\";"));

    HashMap<String, SMGRegion> toPrint = new HashMap<>();
    toPrint.putAll(pStackFrame.getVariables());

    SMGRegion returnObject = pStackFrame.getReturnObject();
    if (returnObject != null) {
      toPrint.put(CLangStackFrame.RETVAL_LABEL, returnObject);
    }

    pSb.append(newLineWithOffset(smgScopeFrameAsDot(toPrint, String.valueOf(pIndex))));

    offset -= 2;
    pSb.append(newLineWithOffset("}"));

  }

  private String smgScopeFrameAsDot(final Map<String, SMGRegion> pNamespace, final String pStructId) {
    StringBuilder sb = new StringBuilder();
    sb.append("struct" + pStructId + "[shape=record,label=\" ");

    // I sooo wish for Python list comprehension here...
    ArrayList<String> nodes = new ArrayList<>();
    for (Entry<String, SMGRegion> entry : pNamespace.entrySet()) {
      String key = entry.getKey();
      SMGObject obj = entry.getValue();

      if (key.equals("node")) {
        // escape Node1
        key = "node1";
      }

      nodes.add("<item_" + key + "> " + obj.toString());
      objectIndex.put(obj, new SMGObjectNode("struct" + pStructId + ":item_" + key));
    }
    sb.append(Joiner.on(" | ").join(nodes));
    sb.append("\"];\n");
    return sb.toString();
  }

  private void addGlobalObjectSubgraph(final ReadableSMG pSmg, final StringBuilder pSb) {
    if (pSmg.getGlobalObjects().size() > 0) {
      pSb.append(newLineWithOffset("subgraph cluster_global{"));
      offset += 2;
      pSb.append(newLineWithOffset("label=\"Global objects\";"));
      pSb.append(newLineWithOffset(smgScopeFrameAsDot(pSmg.getGlobalObjects(), "global")));
      offset -= 2;
      pSb.append(newLineWithOffset("}"));
    }
  }

  private static String newNullLabel() {
    SMGPlotter.nulls += 1;
    return "value_null_" + SMGPlotter.nulls;
  }

  private String smgHVEdgeAsDot(final SMGEdgeHasValue pEdge) {
    if (pEdge.getValue() == 0) {
      String newNull = newNullLabel();
      return newNull + "[shape=plaintext, label=\"NULL\"];" + objectIndex.get(pEdge.getObject()).getName() + " -> " +
             newNull + "[label=\"[" + pEdge.getOffset() + "]\"];";
    } else {
      return objectIndex.get(pEdge.getObject()).getName() + " -> value_" + pEdge.getValue() + "[label=\"[" +
             pEdge.getOffset() + "]\"];";
    }
  }

  private String smgPTEdgeAsDot(final SMGEdgePointsTo pEdge) {
    return "value_" + pEdge.getValue() + " -> " + objectIndex.get(pEdge.getObject()).getName() + "[label=\"+" +
           pEdge.getOffset() + "b\"];";
  }

  private static String smgValueAsDot(final int pValue, final String pExplicit) {
    return "value_" + pValue + "[label=\"#" + pValue + pExplicit +  "\"];";
  }

  @SuppressWarnings("unused")
  private static String neqRelationAsDot(final Integer v1, final Integer v2) {
    String targetNode;
    String returnString = "";
    if (v2.equals(0)) {
      targetNode = newNullLabel();
      returnString = targetNode + "[shape=plaintext, label=\"NULL\", fontcolor=\"red\"];\n";
    } else {
      targetNode = "value_" + v2;
    }
    return returnString + "value_" + v1 + " -> " + targetNode + "[color=\"red\", fontcolor=\"red\", label=\"neq\"]";
  }

  private String newLineWithOffset(final String pLine) {
    return  Strings.repeat(" ", offset) + pLine + "\n";
  }
}
