package com.zavier.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;

public class Agent {
    /**
     * 方法名和参数不能修改，并且需要在 META-INF/MAINFEST.MF中配置此类
     * @param args
     * @param instrumentation
     */
    public static void premain(String args, Instrumentation instrumentation) {
        LogClassFileTransformer logClassFileTransformer = new LogClassFileTransformer();
        instrumentation.addTransformer(logClassFileTransformer);
    }

    static class LogClassFileTransformer implements ClassFileTransformer {

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            try {
                Path path = Paths.get("E:/" + className + ".class");
                Files.write(path, classfileBuffer);
            } catch (Exception e) {
                // ignore
            } finally {
                return classfileBuffer;
            }
        }
    }
}
