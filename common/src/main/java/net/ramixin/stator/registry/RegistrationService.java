package net.ramixin.stator.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public interface RegistrationService {

    <T, V extends T> Registrant<V> register(Registry<T> registry, Identifier id, Supplier<V> realValue);

}
