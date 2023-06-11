package agency.highlysuspect.packages.platform.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public interface MyScreenConstructor<T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> {
	U create(T var1, Inventory var2, Component var3);
}
