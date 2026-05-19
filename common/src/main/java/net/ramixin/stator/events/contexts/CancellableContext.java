package net.ramixin.stator.events.contexts;

public interface CancellableContext extends Context {

    boolean isCancelled();

    void setCancelled(boolean cancelled);

}
