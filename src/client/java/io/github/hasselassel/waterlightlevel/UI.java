package io.github.hasselassel.waterlightlevel;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

class UI {
    protected static KeyBinding TOGGLE_AURA_KEY;

    private static final String MAIN_COMMAND = "waterlightlevel";

    private static final String OPTION_LIGHT_LEVEL = "lightlevel";
    private static final String OPTION_DISTANCE = "distance";
    private static final String OPTION_ARGB = "color_argb";
    private static final String OPTION_TOGGLE_ON_OFF = "toggle";
    private static final String OPTION_ON = "on";
    private static final String OPTION_OFF = "off";
    private static final String OPTION_AURA_ONLY = "aura_only";

    protected static void toggle_on_off() {
        final boolean newState = !Config.AURA_ON;
        Config.AURA_ON = newState;
        Config.SCAN_ON = newState;
    }

    protected static void turn_on_off(boolean state) {
        Config.AURA_ON = state;
        Config.SCAN_ON = state;
    }

    protected static void init() {
        TOGGLE_AURA_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.water_light_level.toggle_aura",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_L,
                KeyBinding.Category.MISC
        ));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal(MAIN_COMMAND)
                .then(ClientCommandManager.literal(OPTION_LIGHT_LEVEL)
                        .then(ClientCommandManager.argument("level", IntegerArgumentType.integer(0, 15))
                                .executes(command_ctx -> {
                                    Config.LIGHT_LEVEL = IntegerArgumentType.getInteger(command_ctx, "level");
                                    command_ctx.getSource().sendFeedback(Text.literal("Light Level Threshold set to " + Config.LIGHT_LEVEL));
                                    Config.saveConfig();
                                    return 1;
                                })))
                .then(ClientCommandManager.literal(OPTION_LIGHT_LEVEL)
                        .executes(command_ctx -> {
                            command_ctx.getSource().sendFeedback(Text.literal("Light Level Threshold: " + Config.LIGHT_LEVEL));
                            return 1;
                        }))
                .then(ClientCommandManager.literal(OPTION_DISTANCE)
                        .then(ClientCommandManager.argument("dist", IntegerArgumentType.integer(0))
                                .executes(command_ctx -> {
                                    Config.DISTANCE = IntegerArgumentType.getInteger(command_ctx, "dist");
                                    command_ctx.getSource().sendFeedback(Text.literal("Light Level Distance set to " + Config.DISTANCE));
                                    Config.saveConfig();
                                    return 1;
                                })))
                .then(ClientCommandManager.literal(OPTION_DISTANCE)
                        .executes(command_ctx -> {
                            command_ctx.getSource().sendFeedback(Text.literal("Light Level Distance: " + Config.DISTANCE));
                            return 1;
                        }))
                .then(ClientCommandManager.literal(OPTION_ARGB)
                        .then(ClientCommandManager.argument("argb", StringArgumentType.word())
                                .executes(command_ctx -> {
                                    var input = StringArgumentType.getString(command_ctx, "argb");
                                    try {
                                        Config.ARGB = Integer.parseUnsignedInt(input, 16);
                                    } catch (NumberFormatException e) {
                                        throw new SimpleCommandExceptionType(
                                                Text.literal("Invalid argb hex input")
                                        ).create();
                                    }
                                    command_ctx.getSource().sendFeedback(Text.literal("ARGB Color set to: " + Integer.toUnsignedString(Config.ARGB, 16)));
                                    Config.saveConfig();
                                    return 1;
                                })))
                .then(ClientCommandManager.literal(OPTION_ARGB)
                        .executes(command_ctx -> {
                            command_ctx.getSource().sendFeedback(Text.literal("ARGB Color: " + Integer.toUnsignedString(Config.ARGB, 16)));
                            return 1;
                        }))
                .then(ClientCommandManager.literal(OPTION_TOGGLE_ON_OFF)
                        .executes(command_ctx -> {
                            toggle_on_off();
                            String end = Config.AURA_ON ? "on" : "off";
                            command_ctx.getSource().sendFeedback(Text.literal("Water Light Level has been toggled " + end));
                            Config.saveConfig();
                            return 1;
                        }))
                .then(ClientCommandManager.literal(OPTION_ON)
                        .executes(command_ctx -> {
                            turn_on_off(true);
                            command_ctx.getSource().sendFeedback(Text.literal("Water Light Level is now turned on"));
                            Config.saveConfig();
                            return 1;
                        }))
                .then(ClientCommandManager.literal(OPTION_OFF)
                        .executes(command_ctx -> {
                            turn_on_off(false);
                            command_ctx.getSource().sendFeedback(Text.literal("Water Light Level is now turned off"));
                            Config.saveConfig();
                            return 1;
                        }))
                .then(ClientCommandManager.literal(OPTION_AURA_ONLY)
                        .executes(command_ctx -> {
                            Config.SCAN_ON = false;
                            Config.AURA_ON = true;
                            var command_ctx_source = command_ctx.getSource();
                            command_ctx_source.sendFeedback(Text.literal("Water Light Level now scans the water once"));

                            if (!WaterScan.scan(MinecraftClient.getInstance())) {
                                throw new SimpleCommandExceptionType(
                                        Text.literal("Somehow the world or the player don't exist...")
                                ).create();
                            }
                            command_ctx_source.sendFeedback(Text.literal("Water Light Level now only renders the aura"));
                            Config.saveConfig();
                            return 1;
                        }))
        ));
    }
}
