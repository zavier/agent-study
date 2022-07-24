package com.zavier.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class Agent {
    /**
     * 方法名和参数不能修改，并且需要在 META-INF/MAINFEST.MF中配置此类
     * @param args
     * @param instrumentation
     */
    public static void premain(String args, Instrumentation instrumentation) {
//        LogClassFileTransformer logClassFileTransformer = new LogClassFileTransformer();
//        instrumentation.addTransformer(logClassFileTransformer);

        createAgent("com.zavier", "sayHello").installOn(instrumentation);
    }

    private static AgentBuilder createAgent(String classNamePrefix, String methodName) {
        return new AgentBuilder.Default().disableClassFormatChanges()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(AgentBuilder.RedefinitionStrategy.Listener.StreamWriting.toSystemError())
                .with(AgentBuilder.Listener.StreamWriting.toSystemError().withTransformationsOnly())
                .with(AgentBuilder.InstallationListener.StreamWriting.toSystemError())
                .type(ElementMatchers.nameStartsWith(classNamePrefix))
                .transform(new AgentBuilder.Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
                        return builder.visit(Advice.to(LoggingAdvice.class).on(
                                named(methodName)));
                    }

                });
    }

    static class LogClassFileTransformer implements ClassFileTransformer {

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            try {

                if (className.startsWith("com/zavier")) {
                    final String[] split = className.split("/");
                    final File file = new File("/Users/zhengwei/temp/" + split[split.length - 1] + "_.class");
                    try (FileOutputStream outputStream = new FileOutputStream(file)){
                        outputStream.write(classfileBuffer);
                    }
                }

                return classfileBuffer;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                return classfileBuffer;
            }
        }
    }
}
