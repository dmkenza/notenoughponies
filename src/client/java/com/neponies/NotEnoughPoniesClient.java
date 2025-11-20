package com.neponies;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.StreamSupport;

import static com.neponies.NotEnoughPonies.isDebug;

public class NotEnoughPoniesClient implements ClientModInitializer {

    public static final Logger logger = LoggerFactory.getLogger("notenoughponies");
    private static KeyBinding debugKey;

    @Override
    public void onInitializeClient() {

        com.neponies.ClientPonyConfigImpl.init();

        // Register villager sounds
        VillagerSounds.registerVillagerSounds();

        // Debug key G
        if (isDebug) {
            debugKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.notenoughponies.debug",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_G,
                    "category.notenoughponies"
            ));

            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if (debugKey.wasPressed() && client.player != null) {
                    ClientDebugUtils.runDebugBreeding();
                }
            });
        }
    }
}
