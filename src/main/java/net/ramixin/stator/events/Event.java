package net.ramixin.stator.events;

import net.ramixin.stator.events.contexts.Context;

public interface Event<C extends Context, R> {

    R call(C context);

}
