package com.neponies;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ClientDebugUtils {

    public static void runDebugBreeding() {
        ClientPlayNetworking.send(new NotEnoughPonies.DebugBreedPayload());
    }

}