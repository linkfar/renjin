package org.renjin.gcc.codegen.call;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.GeneratorFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.PrimitiveConstValueGenerator;
import org.renjin.gcc.codegen.param.ParamGenerator;
import org.renjin.gcc.codegen.ret.ReturnGenerator;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Generates a call to an existing JVM method.
 */
public class StaticMethodCallGenerator implements CallGenerator {
  
  private GeneratorFactory factory;
  private Method method;

  private List<ParamGenerator> paramGenerators = null;
  private ReturnGenerator returnGenerator = null;

  public StaticMethodCallGenerator(GeneratorFactory factory, Method method) {
    this.factory = factory;
    this.method = method;
  }

  private ReturnGenerator returnGenerator() {
    if(returnGenerator == null) {
      returnGenerator = factory.forReturnValue(method);
    }
    return returnGenerator;
  }

  @Override
  public void emitCall(MethodVisitor mv, List<ExprGenerator> argumentGenerators) {

    checkArity(argumentGenerators);

    // The number of fixed (gimple) parameters expected, excluding var args
    // the number of Jvm arguments may be greater
    int fixedArgCount = paramGenerators.size();


    // Push all (fixed) parameters on the stack
    for (int i = 0; i < fixedArgCount; i++) {
      ParamGenerator paramGenerator = getParamGenerators().get(i);
      paramGenerator.emitPushParameter(mv, argumentGenerators.get(i));
    }
    
    // if this method accepts var args, then we pass the remaining arguments as an Object[] array
    if(method.isVarArgs()) {
      int varArgCount = argumentGenerators.size() - fixedArgCount;
      PrimitiveConstValueGenerator.emitInt(mv, varArgCount);
      mv.visitTypeInsn(Opcodes.ANEWARRAY, Type.getInternalName(Object.class));
      
      for(int i=0;i<varArgCount;++i) {
        mv.visitInsn(Opcodes.DUP);
        PrimitiveConstValueGenerator.emitInt(mv, i);
        pushVarArg(mv, argumentGenerators.get(fixedArgCount + i));
        mv.visitInsn(Opcodes.AASTORE);
      }
    }
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(method.getDeclaringClass()),
        method.getName(), Type.getMethodDescriptor(method), false);
  }

  private void pushVarArg(MethodVisitor mv, ExprGenerator exprGenerator) {
    GimpleType type = exprGenerator.getGimpleType();
    if(type instanceof GimplePrimitiveType) {
      exprGenerator.emitPushBoxedPrimitiveValue(mv);
    } else if(type instanceof GimpleIndirectType) {
      exprGenerator.emitPushPointerWrapper(mv);
    } else {
      throw new UnsupportedOperationException("type: " + type);
    }
  }

  private void checkArity(List<ExprGenerator> argumentGenerators) {
    if(method.isVarArgs()) {
      if(argumentGenerators.size() < getParamGenerators().size()) {
        throw new InternalCompilerException(String.format(
            "Arity mismatch: expected at least %d args to method %s.%s(), called with %d" ,
            paramGenerators.size(),
            method.getDeclaringClass().getName(),
            method.getName(),
            argumentGenerators.size()));
      }  
    } else {
      if(argumentGenerators.size() != getParamGenerators().size()) {
        throw new InternalCompilerException(String.format(
            "Arity mismatch: expected %d args to method %s.%s(), called with %d" ,
            paramGenerators.size(),
            method.getDeclaringClass().getName(),
            method.getName(),
            argumentGenerators.size()));
      }
    }
    
  }

  private List<ParamGenerator> getParamGenerators() {
    if(paramGenerators == null) {
      paramGenerators = factory.forParameterTypesOf(method);
    }
    return paramGenerators;
  }

  @Override
  public Type returnType() {
    return returnGenerator().getType();
  }

  @Override
  public GimpleType getGimpleReturnType() {
    return returnGenerator().getGimpleType();
  }

  @Override
  public ExprGenerator expressionGenerator(List<ExprGenerator> argumentGenerators) {
    return returnGenerator().callExpression(this, argumentGenerators);
  }
}