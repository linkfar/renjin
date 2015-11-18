package org.renjin.gcc.codegen.param;


import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.pointers.DereferencedPrimitivePtr;
import org.renjin.gcc.codegen.pointers.PrimitivePtrPtrPlus;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.ObjectPtr;
import org.renjin.gcc.runtime.Ptr;

import java.util.Collections;
import java.util.List;

public class PrimitivePtrPtrParamGenerator extends ParamGenerator {

  private final GimpleIndirectType type;

  /**
   * The {@link Ptr} subclass type
   */
  private final WrapperType pointerType;
  
  public PrimitivePtrPtrParamGenerator(GimpleType type) {
    this.type = (GimpleIndirectType) type;
    this.pointerType = WrapperType.forPointerType((GimpleIndirectType) type.getBaseType());
  }

  @Override
  public GimpleType getGimpleType() {
    return type;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(Type.getType(ObjectPtr.class));
  }

  @Override
  public ExprGenerator emitInitialization(MethodVisitor methodVisitor, int startIndex, LocalVarAllocator localVars) {
    return new PtrPtrExpr(startIndex);
  }

  @Override
  public void emitPushParameter(MethodVisitor mv, ExprGenerator parameterValueGenerator) {
    if(!parameterValueGenerator.getPointerType().equals(WrapperType.OBJECT_PTR)) {
      throw new InternalCompilerException("Type mismatch: " + parameterValueGenerator.getClass().getName());
    }
    parameterValueGenerator.emitPushPointerWrapper(mv);
  }
  
  private class PtrPtrExpr extends AbstractExprGenerator {

    private int varIndex;
    
    public PtrPtrExpr(int varIndex) {
      this.varIndex = varIndex;
    }

    @Override
    public GimpleType getGimpleType() {
      return type;
    }


    @Override
    public void emitPushPointerWrapper(MethodVisitor mv) {
      mv.visitVarInsn(Opcodes.ALOAD, varIndex);
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      emitPushPointerWrapper(mv);
      WrapperType.OBJECT_PTR.emitUnpackArrayAndOffset(mv, pointerType);
    }

    @Override
    public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
      valueGenerator.emitPushPointerWrapper(mv);
      mv.visitVarInsn(Opcodes.ASTORE, varIndex);
    }

    @Override
    public ExprGenerator valueOf() {
      return new DereferencedPrimitivePtr(this);
    }

    @Override
    public ExprGenerator pointerPlus(ExprGenerator offsetInBytes) {
      return new PrimitivePtrPtrPlus(this, offsetInBytes);
    }
  }
  
}
