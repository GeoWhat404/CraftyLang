package me.geowhat.craftylang.mixin;

import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BookEditScreen.LineInfo.class)
public interface LineInfoAccessor {

    @Accessor("asComponent")
    @Mutable
    void setAsComponent(Component component);

}
