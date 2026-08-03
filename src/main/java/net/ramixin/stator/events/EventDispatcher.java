package net.ramixin.stator.events;

import net.ramixin.stator.events.contexts.Context;

import java.util.function.Consumer;

public interface EventDispatcher<C extends Context, R> extends Consumer<Event<C, R>> {
}
