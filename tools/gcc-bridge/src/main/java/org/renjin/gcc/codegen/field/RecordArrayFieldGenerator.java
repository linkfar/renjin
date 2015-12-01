package org.renjin.gcc.codegen.field;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.arrays.RecordArrayElement;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.pointers.AddressOfRecordArray;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

public class RecordArrayFieldGenerator extends FieldGenerator {

  private GimpleArrayType arrayType;
  private String className;
  private String fieldName;
  private RecordClassGenerator recordGenerator;
  private String fieldDescriptor;

  public RecordArrayFieldGenerator(String className, String fieldName, 
                                   RecordClassGenerator recordGenerator, GimpleArrayType arrayType) {
    this.className = className;
    this.fieldName = fieldName;
    this.recordGenerator = recordGenerator;
    this.arrayType = arrayType;
    fieldDescriptor = "[" + recordGenerator.getDescriptor();
  }

  @Override
  public GimpleType getType() {
    return arrayType;
  }

  @Override
  public void emitStaticField(ClassVisitor cv, GimpleVarDecl decl) {
    emitField(ACC_PUBLIC | ACC_STATIC, cv);
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {
    emitField(ACC_PUBLIC, cv);
  }

  private void emitField(int access, ClassVisitor cv) {
    cv.visitField(access, fieldName, fieldDescriptor, null, null).visitEnd();
  }

  @Override
  public ExprGenerator staticExprGenerator() {
    return new StaticArrayValue();
  }

  @Override
  public void emitInstanceInit(MethodVisitor mv) {
    mv.visitVarInsn(Opcodes.ALOAD, 0); // this;
    mv.visitInsn(arrayType.getElementCount());
    mv.visitTypeInsn(Opcodes.ANEWARRAY, recordGenerator.getType().getInternalName());
    mv.visitFieldInsn(Opcodes.PUTFIELD, className, fieldName, fieldDescriptor);
  }

  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    return new MemberValue(instanceGenerator);
  }

  private class StaticArrayValue extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return arrayType;
    }

    @Override
    public ExprGenerator addressOf() {
      return new AddressOfRecordArray(this);
    }

    @Override
    public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
      valueGenerator.emitPushArray(mv);
      mv.visitFieldInsn(Opcodes.PUTSTATIC, className, fieldName, fieldDescriptor);
    }

    @Override
    public void emitPushArray(MethodVisitor mv) {
      mv.visitFieldInsn(Opcodes.GETSTATIC, className, fieldName, fieldDescriptor);
    }

    @Override
    public ExprGenerator elementAt(ExprGenerator indexGenerator) {
      return new RecordArrayElement(recordGenerator, this, indexGenerator);
    }
  }
  
  private class MemberValue extends AbstractExprGenerator {

    private ExprGenerator instanceGenerator;

    public MemberValue(ExprGenerator instanceGenerator) {
      this.instanceGenerator = instanceGenerator;
    }

    @Override
    public GimpleType getGimpleType() {
      return arrayType;
    }

    @Override
    public void emitPushArray(MethodVisitor mv) {
      instanceGenerator.emitPushRecordRef(mv);
      mv.visitFieldInsn(Opcodes.GETFIELD, className, fieldName, fieldDescriptor);
    }

    @Override
    public ExprGenerator addressOf() {
      return new AddressOfRecordArray(this);
    }

    @Override
    public ExprGenerator elementAt(ExprGenerator indexGenerator) {
      return new RecordArrayElement(recordGenerator, this, indexGenerator);
    }
  }
}