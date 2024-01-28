package me.geowhat.craftylang.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class Message {

    private static final String craftyHeader = "§8[§xCrafty§8]§r ";

    public static void send(Component msg) {
        if (Minecraft.getInstance().player != null)
            Minecraft.getInstance().player.sendSystemMessage(msg);
    }

    public static void sendNewline() {
        send(Component.empty());
    }

    public static void sendInfo(String message) {
        send(Component.literal(craftyHeader.replace("§x", "§f") + "§f" + message));
    }

    public static void sendError(String message) {
        send(Component.literal(craftyHeader.replace("§x", "§c") + "§c" + message));
    }


    public static void sendSuccess(String message) {
        send(Component.literal(craftyHeader.replace("§x", "§a") + "§a" + message));
    }


    public static void sendDebug(String message) {
        send(Component.literal(craftyHeader.replace("§x", "§e") + "§e" + message));
    }
}
