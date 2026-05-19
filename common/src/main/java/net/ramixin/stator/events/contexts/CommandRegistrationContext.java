package net.ramixin.stator.events.contexts;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public interface CommandRegistrationContext extends Context {

    CommandDispatcher<CommandSourceStack> dispatcher();

    CommandBuildContext context();

    Commands.CommandSelection selection();

}
