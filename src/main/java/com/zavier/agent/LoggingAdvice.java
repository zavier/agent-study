package com.zavier.agent;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class LoggingAdvice {

    @Advice.OnMethodEnter
    public static String before(
            @Advice.AllArguments Object[] allArguments,
            @Advice.Origin Method method) {
        if(allArguments == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();

        LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
        final String[] parameterNames = discoverer.getParameterNames(method);
        for (int i = 0; i < parameterNames.length; i++) {
            map.put(parameterNames[i], allArguments[i]);
        }
        return map.toString();
    }

    @Advice.OnMethodExit
    public static void after(
            @Advice.Enter String params,
            @Advice.Origin Method method,
            @Advice.Return(typing = Assigner.Typing.DYNAMIC) Object returnValue) {
        System.out.println("method:" + method.getName() + ",param:" + params + ",result:" + returnValue);
    }
}
