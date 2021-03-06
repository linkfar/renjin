/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.compiler.pipeline.optimize;

import org.renjin.compiler.pipeline.DeferredGraph;
import org.renjin.compiler.pipeline.DeferredNode;

/**
 * Remove mathematical identies like (x^1) or (x+1) or (+x)
 */
public class IdentityRemover implements Optimizer {

  @Override
  public boolean optimize(DeferredGraph graph, DeferredNode node) {
    if(node.isComputation()) {
      DeferredNode replacementValue = trySimplify(node);
      if(replacementValue != null) {
        graph.replaceNode(node, replacementValue);
        return true;
      }
    }
    return false;
  }

  private DeferredNode trySimplify(DeferredNode node) {
    if(node.getComputation().getComputationName().equals("^") &&
            node.getOperand(1).hasValue(1.0)) {
      return node.getOperand(0);
    } else {
      return null;
    }

  }
}
