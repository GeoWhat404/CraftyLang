package me.geowhat.craftylang.utils;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public final class ComponentUtils {

    public static Pair<Component, Component> splitAt(Component component, int index) {
        List<Component> componentList = component.toFlatList();

        int currentIndex = 0;

        for (int i = 0; i < componentList.size(); i++) {
            Component currentComponent = componentList.get(i);

            String content = currentComponent.getString();

            if (currentIndex + content.length() < index) {
                currentIndex += content.length();
                continue;
            }

            String frontString = content.substring(0, index - currentIndex);
            String backString = content.substring(index - currentIndex);

            Component frontComponent = null;

            if (i == 0) {
                frontComponent = Component.literal(frontString)
                        .setStyle(currentComponent.getStyle());
            } else {
                for (Component sibling : componentList.subList(0,  i)) {
                    if (frontComponent == null) {
                        frontComponent = sibling;
                    } else {
                        frontComponent = frontComponent.copy().append(sibling);
                    }
                }

                frontComponent = frontComponent.copy().append(Component.literal(frontString).setStyle(currentComponent.getStyle()));
            }

            MutableComponent backComponent = Component.literal(backString)
                    .withStyle(currentComponent.getStyle());

            for (Component sibling : componentList.subList(i + 1, componentList.size())) {
                backComponent = backComponent.append(sibling);
            }

            return Pair.of(frontComponent, backComponent);
        }

        return Pair.of(Component.empty(), Component.empty());
    }

    public static Component substring(Component component, int start, int end) {
        if (start == 0) {
            return splitAt(component, end).getFirst();
        }

        Component firstComponent = splitAt(component, start).getSecond();
        return splitAt(firstComponent, end - start).getFirst();
    }

    // probably doesn't work as intended, but it works for my specific case.
    public static Component stripEnd(Component component, String stripChars) {
        if (component.getString().isEmpty() || component.getString().equals(stripChars)) {
            return Component.empty();
        }

        List<Component> componentList = component.toFlatList();

        int matches = 0;

        for (int i = componentList.size() - 1; i >= 0; i--) {
            Component currentComponent = componentList.get(i);
            String string = currentComponent.getString();

            for (int charIndex = string.length() - 1; i >= 0; i--) {
                char c = string.charAt(charIndex);

                if (c != stripChars.charAt((string.length() - 1) - charIndex)) {
                    return component;
                }

                if (++matches == stripChars.length()) {
                    return substring(component, 0, (component.getString().length() - 1) - (stripChars.length() - 1));
                }
            }
        }

        return component;
    }
}
