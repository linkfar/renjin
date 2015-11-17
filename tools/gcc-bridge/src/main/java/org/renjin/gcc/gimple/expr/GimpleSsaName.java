package org.renjin.gcc.gimple.expr;

/**
 * Gimple Single-Static Assignment (SSA) Variable name
 */
public class GimpleSsaName extends GimpleLValue {
  private GimpleExpr var;
  private int version;
  private boolean defaultDefinition;
  private boolean occursInAbnormalPhi;

  public GimpleExpr getVar() {
    return var;
  }

  public void setVar(GimpleExpr var) {
    this.var = var;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public boolean isDefaultDefinition() {
    return defaultDefinition;
  }

  public void setDefaultDefinition(boolean defaultDefinition) {
    this.defaultDefinition = defaultDefinition;
  }

  public boolean isOccursInAbnormalPhi() {
    return occursInAbnormalPhi;
  }

  public void setOccursInAbnormalPhi(boolean occursInAbnormalPhi) {
    this.occursInAbnormalPhi = occursInAbnormalPhi;
  }

  @Override
  public Iterable<? extends SymbolRef> getSymbolRefs() {
    return var.getSymbolRefs();
  }

  @Override
  public String toString() {
    if(defaultDefinition) {
      return var + toSubscript(0);
    } else {
      return var + toSubscript(version);
    }
  }
  
  private String toSubscript(int version) {
    String digits = Integer.toString(version);
    StringBuilder sb = new StringBuilder(digits.length());
    for(int i=0;i!=digits.length();++i) {
      int digit = digits.charAt(i) - '0';
      sb.appendCodePoint(0x2080 + digit);
    }
    return sb.toString();
  }
}
