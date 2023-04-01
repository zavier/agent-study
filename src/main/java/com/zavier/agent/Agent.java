package com.zavier.agent;

import com.sun.tools.attach.VirtualMachine;
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
import java.util.concurrent.TimeUnit;

import static net.bytebuddy.matcher.ElementMatchers.namedOneOf;

public class Agent {

    public static void main(String[] args) throws Exception {
        VirtualMachine jvm = VirtualMachine.attach("java process id");
        jvm.loadAgent("xx/agent-study-1.0-SNAPSHOT-full.jar");

        TimeUnit.SECONDS.sleep(60);
        jvm.detach();
    }

    /**
     * 方法名和参数不能修改，并且需要在 META-INF/MAINFEST.MF中配置此类
     * 通过启动参数执行agent时使用此方法
     *
     * @param args
     * @param instrumentation
     */
    public static void premain(String args, Instrumentation instrumentation) {
//        LogClassFileTransformer logClassFileTransformer = new LogClassFileTransformer();
//        instrumentation.addTransformer(logClassFileTransformer);

        createAgent("com.zavier", "sayHello").installOn(instrumentation);
    }

    /**
     * 通过attach方法连接到jvm时使用此方法
     *
     * @param agentArgs
     * @param inst
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        createAgent("com.zavier", "sayHello").installOn(inst);
    }

    private static AgentBuilder createAgent(String classNamePrefix, String... methodName) {
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
                                namedOneOf(methodName)));
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
