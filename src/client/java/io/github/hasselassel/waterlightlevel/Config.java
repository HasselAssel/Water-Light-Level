package io.github.hasselassel.waterlightlevel;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

class Config {
    protected static int LIGHT_LEVEL = 0;
    protected static int DISTANCE = 16;
    protected static int ARGB = 0x88FF0000;
    protected static boolean TURNED_ON = false;

    private static final Path CONFIG_FILE = FabricLoader
            .getInstance().getConfigDir()
            .resolve(WaterLightLevelClient.MOD_ID).resolve("config.properties");

    protected static void loadConfig() {
        try {
            Files.createDirectory(CONFIG_FILE.getParent());
        } catch (FileAlreadyExistsException ignored) {
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Properties properties = new Properties();
        if (Files.exists(CONFIG_FILE)) {
            try (InputStream in = Files.newInputStream(CONFIG_FILE)) {
                properties.load(in);
            } catch (IOException ignored) {
            }
        }
        try {
            LIGHT_LEVEL = Integer.parseInt(properties.getProperty("lightLevel", Integer.toString(LIGHT_LEVEL)));
        } catch (NumberFormatException ignored) {
        }
        try {
            DISTANCE = Integer.parseInt(properties.getProperty("distance", Integer.toString(DISTANCE)));
        } catch (NumberFormatException ignored) {
        }
        try {
            ARGB = Integer.parseInt(properties.getProperty("argb", Integer.toString(ARGB, 16)), 16);
        } catch (NumberFormatException ignored) {
        }
        try {
            TURNED_ON = Boolean.parseBoolean(properties.getProperty("turned_on", Boolean.toString(TURNED_ON)));
        } catch (NumberFormatException ignored) {
        }
        saveConfig();
    }

    protected static void saveConfig() {
        Properties properties = new Properties();
        properties.setProperty("lightLevel", Integer.toString(LIGHT_LEVEL));
        properties.setProperty("distance", Integer.toString(DISTANCE));
        properties.setProperty("argb", Integer.toString(ARGB));
        properties.setProperty("turned_on", Boolean.toString(TURNED_ON));
        try (OutputStream out = Files.newOutputStream(CONFIG_FILE)) {
            properties.store(out, null);
        } catch (IOException ignored) {
        }
    }
}
