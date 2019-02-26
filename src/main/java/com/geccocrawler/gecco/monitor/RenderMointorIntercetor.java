package com.geccocrawler.gecco.monitor;

import com.geccocrawler.gecco.exception.RenderException;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class RenderMointorIntercetor implements MethodInterceptor {

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        if ("inject".equals(method.getName())) {
            try {
                return proxy.invokeSuper(obj, args);
            } catch (RenderException ex) {
                RenderMonitor.incrException(ex.getSpiderBeanClass().getName());
                throw ex;
            }
        } else {
            return proxy.invokeSuper(obj, args);
        }
    }
}
