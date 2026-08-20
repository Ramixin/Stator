package net.ramixin.stator.registration;

import java.util.function.Consumer;
import java.util.function.Function;

public final class DeferredRegistrant<T> implements Registrant<T>, Consumer<Registration> {

    private Function<Registration, Registrant<T>> deferredCall;
    private Registrant<T> value;

    protected DeferredRegistrant(Function<Registration, Registrant<T>> deferredCall) {
        this.deferredCall = deferredCall;
    }

    @Override
    public T get() {
        if(value == null)
            throw new IllegalStateException("Deferred value not yet initialized");
        return value.get();
    }

    @Override
    public void accept(Registration registration) {
        if(deferredCall == null)
            throw new IllegalStateException("Deferred value already initialized");
        value = deferredCall.apply(registration);
        deferredCall = null;
    }
}
