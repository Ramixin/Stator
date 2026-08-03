package net.ramixin.stator.events;

import net.ramixin.stator.events.contexts.Context;
import net.ramixin.stator.metadata.DispatchersMetafile;
import net.ramixin.stator.metadata.EventsMetaFile;
import net.ramixin.stator.metadata.StatorMetaFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.classfile.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class StatorEventRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatorEventRegistry.class);
    private static final Map<Class<?>, EventDispatcher<?, ?>> dispatchers = new HashMap<>();

    public static void registerDispatchersMetafile(String loader, File file) throws StatorMetaFileException {
        DispatchersMetafile metafile = DispatchersMetafile.read(file, LOGGER);
        Set<Class<?>> dispatcherAnnotations = metafile.getDispatcherAnnotations(loader);
        for(Class<?> annotation : dispatcherAnnotations) {
            if(!annotation.isAnnotation() || annotation.getAnnotation(StatorEventAnnotation.class) == null) {
                LOGGER.error("class {} is not a stator event annotation", annotation);
                continue;
            }
            Method dispatcherMethod = metafile.getDispatcher(loader, annotation).orElseThrow();
            EventDispatcher<?, ?> dispatcher = event -> {
                try {
                    dispatcherMethod.invoke(null, event);
                } catch (Exception e) {
                    throw new RuntimeException("failed to dispatch event", e);
                }
            };
            dispatchers.put(annotation, dispatcher);

        }
    }

    public static <C extends Context, R> void registerDispatcher(Class<? extends Annotation> annotation, EventDispatcher<C, R> dispatcher, Class<C> contextClass, Class<R> resultClass) {
        validate(annotation, contextClass, resultClass);
        dispatchers.put(annotation, dispatcher);
    }

    public static void registerEventsMetaFile(File file) throws StatorMetaFileException {
        EventsMetaFile metafile = EventsMetaFile.read(file, LOGGER);
        Set<Class<?>> eventAnnotations = metafile.getEventAnnotations();
        for(Class<?> anno : eventAnnotations) {
            if(!anno.isAnnotation() || anno.getAnnotation(StatorEventAnnotation.class) == null) {
                LOGGER.error("class {} is not a stator event annotation", anno);
                continue;
            }
            EventDispatcher<?, ?> dispatcher = dispatchers.get(anno);
            if(dispatcher == null) {
                LOGGER.error("No dispatcher registered for event {}", anno);
                continue;
            }
            List<Method> methods = metafile.getEvents(anno);
            for(Method method : methods) {
                addEvent(dispatcher, method);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <C extends Context, R> void registerEvent(Class<?> annotation, Event<C, R> event, Class<C> contextClass, Class<R> resultClass) {
        validate(annotation, contextClass, resultClass);
        EventDispatcher<C, R> dispatcher = (EventDispatcher<C, R>) dispatchers.get(annotation);
        if(dispatcher == null) {
            LOGGER.error("No dispatcher registered for event  {}", annotation);
            return;
        }
        dispatcher.accept(event);
    }

    private static void validate(Class<?> annotation, Class<?> contextClass, Class<?> resultClass) {
        StatorEventAnnotation statorEventAnnotation = annotation.getAnnotation(StatorEventAnnotation.class);
        if(statorEventAnnotation == null)
            throw new IllegalArgumentException("Class " + annotation + " is not a stator event annotation");
        if(!contextClass.isAssignableFrom(statorEventAnnotation.context()))
            throw new IllegalArgumentException("Class " + contextClass + " is not assignable from " + statorEventAnnotation.context());
        if(!resultClass.isAssignableFrom(statorEventAnnotation.result()))
            throw new IllegalArgumentException("Class " + resultClass + " is not assignable from " + statorEventAnnotation.result());
    }

    @SuppressWarnings("unchecked")
    private static <C extends Context, R> void addEvent(EventDispatcher<C, R> dispatcher, Method method) {
        dispatcher.accept(c -> {
            R result;
            try {
                result = (R) method.invoke(null, c);
            } catch (Exception e) {
                throw new RuntimeException("event failed", e);
            }
            return result;
        });
    }

}
