package io.github.andrewwwwwwwwwwwwwww.endbeast;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

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
                Commands.literal("endbeast")
                    .then(Commands.literal("setendplayercount")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1))
                            .executes(ctx -> {
                                int count = IntegerArgumentType.getInteger(ctx, "count");
                                PortalActivation.setRequiredPlayers(count);
                                PortalActivation.save(ctx.getSource().getServer());
                                ctx.getSource().sendSuccess(() -> Component.literal(
                                    "End Portal player requirement set to " + count), true);
                                return 1;
                            })))
                    .then(Commands.literal("portalreq")
                        .executes(ctx -> {
                            if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                PortalActivation.showRequirements(player);
                                return 1;
                            }
                            ctx.getSource().sendFailure(Component.literal("This command must be run by a player."));
                            return 0;
                        }))
                    .then(Commands.literal("reset")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ctx -> {
                            PortalActivation.reset(ctx.getSource().getServer());
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                "The End Portal has been re-locked; the ritual is required again."), true);
                            return 1;
                        }))
            )
        );
    }
}
