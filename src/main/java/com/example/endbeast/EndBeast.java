package com.example.endbeast;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class EndBeast implements ModInitializer {
    public static final String MOD_ID = "endbeast";

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(PortalActivation::load);
        ServerLifecycleEvents.SERVER_STOPPING.register(PortalActivation::save);
        ServerTickEvents.END_SERVER_TICK.register(PortalActivation::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
            PortalActivation.onPlayerDisconnect(handler.player.getUUID()));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(
                Commands.literal("EndBeast")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .then(Commands.literal("setendplayercount")
                        .then(Commands.argument("count", IntegerArgumentType.integer(1))
                            .executes(ctx -> {
                                int count = IntegerArgumentType.getInteger(ctx, "count");
                                PortalActivation.setRequiredPlayers(count);
                                PortalActivation.save(ctx.getSource().getServer());
                                ctx.getSource().sendSuccess(() -> Component.literal(
                                    "End Portal player requirement set to " + count), true);
                                return 1;
                            })))
            )
        );
    }
}
