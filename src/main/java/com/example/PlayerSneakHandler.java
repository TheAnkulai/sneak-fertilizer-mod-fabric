package com.example;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerSneakHandler {

    private static final Map<UUID, Boolean> wasSneaking = new HashMap<>();

    public static void onPlayerTick(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        boolean prev = wasSneaking.getOrDefault(id, false);
        boolean now  = player.isSneaking();

        if (!prev && now) {
            applyBonemeal(player);
        }

        wasSneaking.put(id, now);
    }

    private static void applyBonemeal(ServerPlayerEntity player) {
        ServerWorld world =(ServerWorld) player.getWorld();
        BlockPos center = player.getBlockPos();
        int radius = FertilizingMod.RADIUS;

        BlockPos.iterateOutwards(center, radius, 3, radius).forEach(pos -> {
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (block instanceof Fertilizable fertilizable && !BLACKLISTED.contains(block)) {
                if (fertilizable.isFertilizable(world, pos, state) &&
                        fertilizable.canGrow(world, world.random, pos, state)) {

                    fertilizable.grow(world, world.random, pos, state);
                    world.syncWorldEvent(1505, pos, 0);
                    world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state));
                }
            }
        });
    }

    private static final Set<Block> BLACKLISTED = Set.of(
            Blocks.GRASS_BLOCK,
            Blocks.MOSS_BLOCK,
            Blocks.PALE_MOSS_BLOCK,
            Blocks.CRIMSON_NYLIUM,
            Blocks.WARPED_NYLIUM,
            Blocks.ROOTED_DIRT,
            Blocks.AZALEA,
            Blocks.FLOWERING_AZALEA,
            Blocks.GLOW_LICHEN,
            Blocks.PALE_HANGING_MOSS,
            Blocks.SHORT_GRASS,
            Blocks.TALL_GRASS
    );
}
