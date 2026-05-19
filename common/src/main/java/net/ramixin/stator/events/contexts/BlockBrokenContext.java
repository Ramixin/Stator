package net.ramixin.stator.events.contexts;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockBrokenContext extends Context {

    LevelAccessor level();

    Player player();

    BlockPos blockPos();

    BlockState blockState();

    BlockEntity blockEntity();

}
