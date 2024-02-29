package me.geowhat.craftylang.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.geowhat.craftylang.client.screen.CraftyConfigurationScreen;
import me.geowhat.craftylang.client.util.Scheduler;
import me.geowhat.craftylang.interpreter.CraftScript;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;


public class KeyBindings {

    public static KeyMapping openConfigKey;
    public static KeyMapping killKey;
    private static final String KEY_CATEGORY = "category.craftylang.keys";
    private static final String CONFIG_KEY = "key.craftylang.config";
    private static final String KILL_KEY = "key.craftylang.kill_exec";

    public static int killKeyCode = GLFW.GLFW_KEY_K;
    public static int openConfigKeyCode = GLFW.GLFW_KEY_N;

    public static void register() {
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                CONFIG_KEY,
                InputConstants.Type.KEYSYM,
                openConfigKeyCode,
                KEY_CATEGORY
        ));

        killKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                KILL_KEY,
                InputConstants.Type.KEYSYM,
                killKeyCode,
                KEY_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfigKey.consumeClick()) {
                Minecraft.getInstance().setScreen(new CraftyConfigurationScreen(Minecraft.getInstance().screen));
            }

            while (killKey.consumeClick()) {
                if (CraftScript.running || Scheduler.isContinueExecution()) {
                    CraftScript.kill(CraftScript.USER_REQUEST_CODE);
                    CraftScript.running = false;
                }
            }
        });
    }
}
