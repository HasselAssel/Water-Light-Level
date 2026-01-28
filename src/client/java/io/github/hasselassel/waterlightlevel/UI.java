package io.github.hasselassel.waterlightlevel;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
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
                        .then(ClientCommandManager.argument("dist", IntegerArgumentType.integer(0, 64))
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
                            Config.TURNED_ON = !Config.TURNED_ON;
                            String end = Config.TURNED_ON ? "on" : "off";
                            command_ctx.getSource().sendFeedback(Text.literal("Water Light Level is now turned " + end));
                            Config.saveConfig();
                            return 1;
                        }))
        ));
    }
}
