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
package org.renjin.gnur;


import org.renjin.gcc.runtime.DoublePtr;

import java.util.Arrays;

/**
 * Internal Sort routines exported to packages
 */
public class Sort {

  /**
   * Partial sort so that x[k] is in the correct place, smaller to left,
   * larger to right
   *
   */
  public static void rPsort2(DoublePtr x, int lo, int hi, int k) {
    boolean nalast=true;
    double v, w;
    int L, R, i, j;

    for (L = lo, R = hi; L < R; ) {
      v = x.get(k);
      for(i = L, j = R; i <= j;) {
        while (rcmp(x.get(i), v, nalast) < 0) {
          i++;
        }
        while (rcmp(v, x.get(j), nalast) < 0) {
          j--;
        }
        if (i <= j) {
          w = x.get(i);
          x.set(i++, x.get(j));
          x.set(j--,  w);
        }
      }
      if (j < k) {
        L = i;
      }
      if (k < i) {
        R = j;
      }
    }
  }

  public static void Rf_rPsort(DoublePtr x, int n, int k) {
    rPsort2(x, 0, n-1, k);
  }

  public static void R_rsort(DoublePtr x, int n) {
    Arrays.sort(x.array, x.offset, x.offset+n);
  }

  private static int rcmp(double x, double y, boolean nalast) {
    boolean nax = Double.isNaN(x), nay = Double.isNaN(y);
    if (nax && nay) {
      return 0;
    }
    if (nax) {
      return nalast ? 1 : -1;
    }
    if (nay) {
      return nalast ? -1 : 1;
    }
    if (x < y) {
      return -1;
    }
    if (x > y) {
      return 1;
    }
    return 0;
  }
}
