package net.ramixin.stator.events;

import net.ramixin.stator.events.contexts.Context;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public final class StatorEventRegistry {

    private static final Map<Class<? extends Annotation>, Event<?, ?>> services = new ConcurrentHashMap<>();
    private static boolean loadedEvents = false;

    private static void loadServices() {
        ServiceLoader
                .load(Event.class, StatorEventRegistry.class.getClassLoader())
                .forEach(castable -> {
                    Event<? extends Context, ?> service = (Event<? extends Context, ?>) castable;
                    if(services.containsKey(service.getAnnotationClass()))
                        throw new IllegalArgumentException("Stator service for " + service.getAnnotationClass().getName() + " is already registered");
                    services.put(service.getAnnotationClass(), service);
                });
        loadedEvents = true;
    }

    public static void register(Class<?> clazz) {
        if(!loadedEvents)
            loadServices();
        for(Method method : clazz.getDeclaredMethods()) {
            Annotation anno = getServiceAnnotation(method);
            if(anno == null) continue;
            if(!Modifier.isStatic(method.getModifiers()))
                throw new IllegalStateException("Stator event method '" + method.getName() + "' must be static");
            Event<?, ?> service = services.get(anno.annotationType());
            if(service == null)
                throw new IllegalStateException("Stator service missing for event " + anno.annotationType().getName());
            if(method.getReturnType() != service.getReturnClass())
                throw new IllegalStateException("Stator event method '" + method.getName() + "' must return " + service.getReturnClass().getName());
            if(method.getParameterCount() != 1)
                throw new IllegalStateException("Context class must be the only parameter of event method '" + method.getName() + "'");
            Class<?> param = method.getParameterTypes()[0];
            if(!param.isAssignableFrom(service.getContextClass()))
                throw new IllegalStateException(String.format("Context class '%s' must be assignable to '%s' from event method '%s'", param.getName(), service.getContextClass().getName(), method.getName()));
            method.setAccessible(true);
            registerListener(service, method);
        }
    }

    @SuppressWarnings("unchecked")
    private static <R> void registerListener(Event<?, R> service, Method method) {
        service.registerNativeListener(context -> {
            R ret;
            try {
                ret = (R) method.invoke(null, context);
            } catch (Exception e) {
                throw new RuntimeException("Failed to invoke event handler: " + method, e);
            }
            return ret;
        });
    }

    private static Annotation getServiceAnnotation(Method method) {
        Annotation anno = null;
        for(Annotation annotation : method.getAnnotations()) {
            Annotation temp = annotation.annotationType().getAnnotation(StatorEventAnnotation.class);
            if(temp != null) {
                if(anno != null)
                    throw new IllegalStateException("Only one Stator event annotation is allowed per method");
                anno = annotation;
            }
        }
        return anno;
    }

}
