package com.neponies;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;

import java.util.HashMap;
import java.util.Optional;

public class PonyUtils {

    private static final HashMap<String, Identifier> resData = new HashMap<>();

    public static boolean canLoadCustomPonySkin(Entity entity) {
        if (entity instanceof VillagerPonyEntityAccessor accessor) {
                String skinId = accessor.getPonySkinID();
                try {
                    return Integer.parseInt(skinId) >= 0;
                } catch (NumberFormatException e) {
                    return false;
                }
        }
        return false;
    }

    public static Identifier findCustomTexture(Entity entity) {
        if (entity instanceof VillagerPonyEntityAccessor accessor) {
            String skinIdStr = accessor.getPonySkinID();
            if (skinIdStr == null) return null;

            int skinId;
            try {
                skinId = Integer.parseInt(skinIdStr);
            } catch (NumberFormatException e) {
                return null;
            }

            if (skinId < 0) return null;

            String path = "textures/pony/" + skinId + ".png";
            return getOrCreateIdentifierOrNull(path);
        }

        return null;
    }

    public static Identifier getOrCreateIdentifierOrNull(String path) {
        if (resData.containsKey(path)) {
            return resData.get(path);
        }

        Identifier id = Identifier.of("notenoughponies", path);
        ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();

        try {
            Optional<Resource> res = resourceManager.getResource(id);
            if (res.isPresent()) {
                resData.put(path, id);
                return id;
            }
        } catch (Exception e) {
        }

        return null;
    }

}