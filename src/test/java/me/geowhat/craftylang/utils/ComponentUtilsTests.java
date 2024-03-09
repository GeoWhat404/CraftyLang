package me.geowhat.craftylang.utils;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ComponentUtilsTests {

//    @Test
//    @DisplayName("shouldSplitComponent")
//    void shouldSplitComponent() {
//        Component component = Component.literal("Hello").withStyle(ChatFormatting.AQUA)
//                .append(CommonComponents.space())
//                .append(Component.literal("World!").withStyle(ChatFormatting.RED));
//
//        Pair<Component, Component> actualSplitComponent = ComponentUtils.splitAt(component, 3);
//        Pair<Component, Component> expectedSplitComponent = Pair.of(
//                Component.literal("Hel").withStyle(ChatFormatting.AQUA),
//                ((Component.literal("lo").withStyle(ChatFormatting.AQUA))
//                        .append(CommonComponents.space()))
//                        .append(Component.literal("World!").withStyle(ChatFormatting.RED))
//        );
//
//        assertEquals(expectedSplitComponent, actualSplitComponent);
//    }
//
//    @Test
//    @DisplayName("shouldSplitComponent2")
//    void shouldSplitComponent2() {
//        Component component = Component.literal("Hello").withStyle(ChatFormatting.AQUA)
//                .append(CommonComponents.space())
//                .append(Component.literal("World!").withStyle(ChatFormatting.RED));
//
//        Pair<Component, Component> actualSplitComponent = ComponentUtils.splitAt(component, 8);
//        Pair<Component, Component> expectedSplitComponent = Pair.of(
//                Component.literal("Hello").withStyle(ChatFormatting.AQUA)
//                        .append(CommonComponents.space())
//                        .append(Component.literal("Wo").withStyle(ChatFormatting.RED)),
//                        Component.literal("rld!").withStyle(ChatFormatting.RED)
//        );
//
//        assertEquals(expectedSplitComponent, actualSplitComponent);
//    }
}
