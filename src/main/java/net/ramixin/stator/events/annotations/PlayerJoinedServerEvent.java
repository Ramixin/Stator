package net.ramixin.stator.events.annotations;

import net.ramixin.stator.events.StatorEventAnnotation;
import net.ramixin.stator.events.contexts.PlayerJoinedServerContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@StatorEventAnnotation(context = PlayerJoinedServerContext.class, result = void.class)
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface PlayerJoinedServerEvent {
}
