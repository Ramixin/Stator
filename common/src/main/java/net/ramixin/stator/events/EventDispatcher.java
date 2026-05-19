package net.ramixin.stator.events;

import net.ramixin.stator.events.contexts.Context;

@FunctionalInterface
public interface EventDispatcher<C extends Context, R> {

    R dispatch(C context);

}
