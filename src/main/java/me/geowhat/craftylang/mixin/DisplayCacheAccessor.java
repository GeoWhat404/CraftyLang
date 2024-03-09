package me.geowhat.craftylang.mixin;

import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BookEditScreen.DisplayCache.class)
public interface DisplayCacheAccessor {
    @Accessor("lines")
    BookEditScreen.LineInfo[] getLines();
}
