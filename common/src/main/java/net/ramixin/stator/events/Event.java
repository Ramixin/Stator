package net.ramixin.stator.events;

import net.ramixin.stator.events.contexts.Context;

import java.lang.annotation.Annotation;

public interface Event<C extends Context, R> {

    Class<? extends Annotation> getAnnotationClass();

    Class<? extends Context> getContextClass();

    Class<R> getReturnClass();

    void registerNativeListener(EventDispatcher<C, R> dispatcher);

}
