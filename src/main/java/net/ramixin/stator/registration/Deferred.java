package net.ramixin.stator.registration;

import java.util.Queue;
import java.util.function.Consumer;

public interface Deferred<T> {

    Queue<Consumer<T>> deferrals();



}
