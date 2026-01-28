package io.github.hasselassel.waterlightlevel;

import net.fabricmc.api.ClientModInitializer;

public class WaterLightLevelClient implements ClientModInitializer {
    public static final String MOD_ID = "water-light-level";

    @Override
    public void onInitializeClient() {
        Config.loadConfig();
        UI.init();
        WaterScan.init();
        AuraRenderer.init();
    }
}

