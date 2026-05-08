package com.wishtoday.ts.commandtranslator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinExtension implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return "";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass
            , String mixinClassName, IMixinInfo mixinInfo) {
        this.inject(mixinInfo.getClassNode(0), targetClass);
    }

    private void inject(ClassNode classNode, ClassNode targetClassNode) {
        System.out.println("triggered inject1" + classNode.name);
        if (classNode.visibleAnnotations == null || classNode.visibleAnnotations.isEmpty()) {
            return;
        }
        boolean hasAnnotation = false;
        for (AnnotationNode annotation : classNode.visibleAnnotations) {
            String descriptor = Type.getDescriptor(ServiceClass.class);
            if (annotation.desc.equals(descriptor)) {
                hasAnnotation = true;
            }
        }
        if (!hasAnnotation) {
            return;
        }
        System.out.println("triggered inject" + classNode.name);
        for (MethodNode method : targetClassNode.methods) {
            if (method.name.equals("<init>")) {
                AbstractInsnNode returnNode = this.searchReturn(method);
                if (returnNode == null) {
                    return;
                }
                InsnList insnList = new InsnList();

                //insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/wishtoday/ts/commandtranslator/Test", "println", "()V", false));
                //insnList.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
                //insnList.add(new LdcInsnNode("Hello World!"));
                //insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));
                insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));//this
                insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/wishtoday/ts/commandtranslator/ServiceInjector", "inject", "(Ljava/lang/Object;)V", false));
                method.instructions.insertBefore(returnNode, insnList);

                //method.maxStack++;
                //method.maxLocals++;

                /*for (AbstractInsnNode insn : method.instructions) {
                    if (insn instanceof FrameNode) {
                        method.instructions.remove(insn);
                    }
                }
                method.maxStack = 0;
                method.maxLocals = 0;*/

                return;
            }
        }
    }

    private AbstractInsnNode searchReturn(MethodNode method) {
        AbstractInsnNode lastReturn = null;
        for (AbstractInsnNode node : method.instructions) {
            if (node.getOpcode() == Opcodes.RETURN) {
                lastReturn = node;
            }
        }
        return lastReturn;
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
