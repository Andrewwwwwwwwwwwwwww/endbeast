package com.example.endbeast;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PortalActivation {
    private static final com.google.gson.Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final long TIMEOUT_TICKS = 20L * 60L;
    private static final double NEARBY_RADIUS = 16.0;

    private static final Map<Item, String> ITEM_TO_ID = new LinkedHashMap<>();
    private static final Map<String, Item> ID_TO_ITEM = new HashMap<>();
    static {
        ITEM_TO_ID.put(Items.TRIDENT, "trident");
        ITEM_TO_ID.put(Items.NETHERITE_BLOCK, "netherite_block");
        ITEM_TO_ID.put(Items.SNIFFER_EGG, "sniffer_egg");
        ITEM_TO_ID.put(Items.TOTEM_OF_UNDYING, "totem_of_undying");
        ITEM_TO_ID.put(Items.ENCHANTED_GOLDEN_APPLE, "enchanted_golden_apple");
        for (Map.Entry<Item, String> e : ITEM_TO_ID.entrySet()) {
            ID_TO_ITEM.put(e.getValue(), e.getKey());
        }
    }

    public static final Set<Item> REQUIRED_ITEMS = ITEM_TO_ID.keySet();

    private static boolean activated = false;
    private static final Set<Item> consumed = new LinkedHashSet<>();
    private static final Map<Item, UUID> consumedBy = new LinkedHashMap<>();
    private static final Set<UUID> participants = new LinkedHashSet<>();
    private static long lastConsumeTick = 0L;
    private static BlockPos lastPortalPos = null;
    private static int requiredPlayers = 3;

    private static final Map<UUID, Long> lastMessageTick = new HashMap<>();
    private static Path savePath = null;

    public static void setRequiredPlayers(int count) {
        requiredPlayers = Math.max(1, count);
    }

    public static boolean isActivated(ServerLevel level) {
        return activated;
    }

    public static void load(MinecraftServer server) {
        savePath = server.getWorldPath(LevelResource.ROOT).resolve("endbeast.json");
        activated = false;
        consumed.clear();
        consumedBy.clear();
        participants.clear();
        lastConsumeTick = 0L;
        lastPortalPos = null;
        requiredPlayers = 3;
        if (!Files.exists(savePath)) return;
        try {
            JsonObject json = JsonParser.parseString(Files.readString(savePath)).getAsJsonObject();
            if (json.has("activated")) activated = json.get("activated").getAsBoolean();
            if (json.has("consumed")) {
                JsonArray list = json.getAsJsonArray("consumed");
                for (JsonElement e : list) {
                    Item item = ID_TO_ITEM.get(e.getAsString());
                    if (item != null) consumed.add(item);
                }
            }
            if (json.has("consumedBy")) {
                JsonObject byMap = json.getAsJsonObject("consumedBy");
                for (Map.Entry<String, JsonElement> e : byMap.entrySet()) {
                    Item item = ID_TO_ITEM.get(e.getKey());
                    if (item != null) {
                        try {
                            consumedBy.put(item, UUID.fromString(e.getValue().getAsString()));
                        } catch (IllegalArgumentException ignored) { }
                    }
                }
            }
            if (json.has("participants")) {
                JsonArray list = json.getAsJsonArray("participants");
                for (JsonElement e : list) {
                    try {
                        participants.add(UUID.fromString(e.getAsString()));
                    } catch (IllegalArgumentException ignored) { }
                }
            }
            if (json.has("lastConsumeTick")) lastConsumeTick = json.get("lastConsumeTick").getAsLong();
            if (json.has("lastPortalPos")) {
                JsonObject pos = json.getAsJsonObject("lastPortalPos");
                lastPortalPos = new BlockPos(
                    pos.get("x").getAsInt(),
                    pos.get("y").getAsInt(),
                    pos.get("z").getAsInt()
                );
            }
            if (json.has("requiredPlayers")) requiredPlayers = Math.max(1, json.get("requiredPlayers").getAsInt());
        } catch (Exception e) {
            System.err.println("[EndBeast] Failed to load state: " + e.getMessage());
        }
    }

    public static void save(MinecraftServer server) {
        if (savePath == null) return;
        try {
            JsonObject json = new JsonObject();
            json.addProperty("activated", activated);

            JsonArray list = new JsonArray();
            for (Item item : consumed) {
                String id = ITEM_TO_ID.get(item);
                if (id != null) list.add(id);
            }
            json.add("consumed", list);

            JsonObject byMap = new JsonObject();
            for (Map.Entry<Item, UUID> e : consumedBy.entrySet()) {
                String id = ITEM_TO_ID.get(e.getKey());
                if (id != null) byMap.addProperty(id, e.getValue().toString());
            }
            json.add("consumedBy", byMap);

            JsonArray pList = new JsonArray();
            for (UUID id : participants) pList.add(id.toString());
            json.add("participants", pList);

            json.addProperty("lastConsumeTick", lastConsumeTick);
            if (lastPortalPos != null) {
                JsonObject pos = new JsonObject();
                pos.addProperty("x", lastPortalPos.getX());
                pos.addProperty("y", lastPortalPos.getY());
                pos.addProperty("z", lastPortalPos.getZ());
                json.add("lastPortalPos", pos);
            }
            json.addProperty("requiredPlayers", requiredPlayers);

            Files.createDirectories(savePath.getParent());
            Files.writeString(savePath, GSON.toJson(json));
        } catch (Exception e) {
            System.err.println("[EndBeast] Failed to save state: " + e.getMessage());
        }
    }

    public static boolean tryConsume(ServerLevel level, ItemEntity itemEntity) {
        if (activated) return false;
        Item item = itemEntity.getItem().getItem();
        if (!REQUIRED_ITEMS.contains(item)) return false;
        if (consumed.contains(item)) return false;

        UUID throwerId = null;
        Entity owner = itemEntity.getOwner();
        if (owner != null) throwerId = owner.getUUID();

        consumed.add(item);
        if (throwerId != null) {
            consumedBy.put(item, throwerId);
            participants.add(throwerId);
        }
        lastConsumeTick = level.getGameTime();
        lastPortalPos = itemEntity.blockPosition();

        addNearbyParticipants(level);

        itemEntity.discard();

        int required = getRequiredPlayers();
        if (consumed.containsAll(REQUIRED_ITEMS) && participants.size() >= required) {
            activate(level);
        } else if (consumed.containsAll(REQUIRED_ITEMS) && throwerId != null) {
            ServerPlayer p = level.getServer().getPlayerList().getPlayer(throwerId);
            if (p != null) {
                int needed = required - participants.size();
                String witnesses = needed == 1 ? "witness" : "witnesses";
                p.sendSystemMessage(
                    Component.literal("The offerings are made, but " + needed + " more " + witnesses + " needed.")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
        }
        save(level.getServer());
        return true;
    }

    public static void tick(MinecraftServer server) {
        if (activated) return;
        if (consumed.isEmpty()) return;

        ServerLevel level = server.overworld();
        if (level == null) return;

        if (level.getGameTime() - lastConsumeTick > TIMEOUT_TICKS) {
            returnItems(server, level);
            return;
        }

        if (lastPortalPos != null && level.getGameTime() % 20L == 0L) {
            int beforeSize = participants.size();
            addNearbyParticipants(level);
            if (participants.size() > beforeSize) {
                int required = getRequiredPlayers();
                if (consumed.containsAll(REQUIRED_ITEMS) && participants.size() >= required) {
                    activate(level);
                }
                save(server);
            }
        }
    }

    private static void addNearbyParticipants(ServerLevel level) {
        if (lastPortalPos == null) return;
        AABB area = new AABB(lastPortalPos).inflate(NEARBY_RADIUS);
        for (ServerPlayer p : level.getEntitiesOfClass(ServerPlayer.class, area)) {
            participants.add(p.getUUID());
        }
    }

    private static int getRequiredPlayers() {
        return Math.max(1, requiredPlayers);
    }

    private static void activate(ServerLevel level) {
        activated = true;
        consumed.clear();
        participants.clear();
        consumedBy.clear();
        lastMessageTick.clear();
        lastPortalPos = null;
        broadcastActivation(level);
    }

    public static void onPlayerDisconnect(UUID uuid) {
        lastMessageTick.remove(uuid);
    }

    private static void returnItems(MinecraftServer server, ServerLevel level) {
        Set<UUID> notifiedPlayers = new HashSet<>();
        for (Item item : consumed) {
            UUID throwerId = consumedBy.get(item);
            ItemStack stack = new ItemStack(item);

            ServerPlayer player = throwerId != null ? server.getPlayerList().getPlayer(throwerId) : null;
            if (player != null) {
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
                notifiedPlayers.add(throwerId);
            } else if (lastPortalPos != null) {
                ItemEntity drop = new ItemEntity(level,
                    lastPortalPos.getX() + 0.5,
                    lastPortalPos.getY() + 1.0,
                    lastPortalPos.getZ() + 0.5,
                    stack);
                level.addFreshEntity(drop);
            }
        }

        Component msg = Component.literal("The portal grew impatient and returned your offerings.")
            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
        for (UUID uuid : notifiedPlayers) {
            ServerPlayer p = server.getPlayerList().getPlayer(uuid);
            if (p != null) p.sendSystemMessage(msg);
        }

        consumed.clear();
        consumedBy.clear();
        participants.clear();
        lastConsumeTick = 0L;
        lastPortalPos = null;
        save(server);
    }

    private static void broadcastActivation(ServerLevel level) {
        Component msg = Component.literal("The End Portal hungers no more. The way is open.")
            .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD);
        for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
            p.sendSystemMessage(msg);
        }
    }

    public static void repelPlayer(ServerPlayer player, BlockPos portalPos) {
        double dx = player.getX() - (portalPos.getX() + 0.5);
        double dz = player.getZ() - (portalPos.getZ() + 0.5);
        double mag = Math.sqrt(dx * dx + dz * dz);
        if (mag < 0.1) {
            double yaw = Math.toRadians(player.getYRot());
            dx = -Math.sin(yaw);
            dz = Math.cos(yaw);
        } else {
            dx /= mag;
            dz /= mag;
        }
        player.setDeltaMovement(dx * 1.2, 0.8, dz * 1.2);
        player.hurtMarked = true;

        long now = player.level().getGameTime();
        Long last = lastMessageTick.get(player.getUUID());
        if (last == null || now - last > 100) {
            sendRequiredItemsMessage(player);
            lastMessageTick.put(player.getUUID(), now);
        }
    }

    private static void sendRequiredItemsMessage(ServerPlayer player) {
        send(player, "Collect these items few", ChatFormatting.GOLD, ChatFormatting.BOLD);
        send(player, "  A Trident from the bubbling undead", ChatFormatting.AQUA);
        send(player, "  A block of Nether & Gold forged steel", ChatFormatting.DARK_PURPLE);
        send(player, "  An egg of a beast long past", ChatFormatting.GREEN);
        send(player, "  An Apple glistening with power", ChatFormatting.YELLOW);
        send(player, "  and finally a hand held savior", ChatFormatting.WHITE);
    }

    private static void send(ServerPlayer player, String text, ChatFormatting... styles) {
        player.sendSystemMessage(Component.literal(text).withStyle(styles));
    }
}
