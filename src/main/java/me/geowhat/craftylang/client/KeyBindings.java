package me.geowhat.craftylang.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.geowhat.craftylang.client.screen.CraftyConfigurationScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    private static KeyMapping openConfigKey;
    private static final String KEY_CATEGORY = "category.craftylang.keys";
    private static final String CONFIG_KEY = "key.craftylang.config";

    public static void register() {
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                CONFIG_KEY,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                KEY_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfigKey.consumeClick()) {
                Minecraft.getInstance().setScreen(new CraftyConfigurationScreen());
            }
        });
    }
}
