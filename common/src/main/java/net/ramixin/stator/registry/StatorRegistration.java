package net.ramixin.stator.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import java.util.ServiceLoader;
import java.util.function.Supplier;

public final class StatorRegistration {

    private static final RegistrationService SERVICE = ServiceLoader.load(RegistrationService.class, StatorRegistration.class.getClassLoader()).findFirst().orElseThrow(() -> new IllegalStateException("Failed to locate Dynamo RegistrationService"));

    public static <T, V extends T> Registrant<V> register(Registry<T> registry, Identifier id, Supplier<V> realValue) {
        return SERVICE.register(registry, id, realValue);
    }

}
