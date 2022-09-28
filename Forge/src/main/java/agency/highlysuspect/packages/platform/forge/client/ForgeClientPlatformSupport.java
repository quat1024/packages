package agency.highlysuspect.packages.platform.forge.client;

import agency.highlysuspect.packages.net.ActionPacket;
import agency.highlysuspect.packages.platform.ClientPlatformSupport;
import agency.highlysuspect.packages.platform.PlatformSupport;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForgeClientPlatformSupport implements ClientPlatformSupport {
	public ForgeClientPlatformSupport() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::actuallyRegisterMenuScreens);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::actuallyBakeSpritesOnto);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::actuallySetBlockEntityRenderers);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::actuallySetRenderTypes);
	}
	
	///
	
	private static record MenuScreenEntry<T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>>(PlatformSupport.RegistryHandle<MenuType<T>> type, MyScreenConstructor<T, U> cons) {
		void register() { MenuScreens.register(type.get(), cons::create); }
	}
	private final List<MenuScreenEntry<?, ?>> menuScreensToRegister = new ArrayList<>();
	
	@Override
	public <T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> void registerMenuScreen(PlatformSupport.RegistryHandle<MenuType<T>> type, MyScreenConstructor<T, U> cons) {
		menuScreensToRegister.add(new MenuScreenEntry<>(type, cons));
	}
	
	private void actuallyRegisterMenuScreens(FMLClientSetupEvent e) {
		menuScreensToRegister.forEach(MenuScreenEntry::register);
	}
	
	///
	
	private final Map<ResourceLocation, List<ResourceLocation>> spritesToBake = new HashMap<>();
	
	@Override
	public void bakeSpritesOnto(ResourceLocation atlasTexture, ResourceLocation... sprites) {
		spritesToBake.computeIfAbsent(atlasTexture, __ -> new ArrayList<>()).addAll(Arrays.asList(sprites));
	}
	
	private void actuallyBakeSpritesOnto(TextureStitchEvent.Pre event) {
		List<ResourceLocation> sprites = spritesToBake.get(event.getAtlas().location());
		if(sprites != null) sprites.forEach(event::addSprite);
	}
	
	///
	
	private static record BlockEntityRendererEntry<T extends BlockEntity>(PlatformSupport.RegistryHandle<? extends BlockEntityType<T>> type, BlockEntityRendererProvider<? super T> renderer) {
		//generics moment
		void register(EntityRenderersEvent.RegisterRenderers e) { e.registerBlockEntityRenderer(type.get(), renderer); }
	}
	private final List<BlockEntityRendererEntry<?>> blockEntityRenderersToRegister = new ArrayList<>();
	
	@Override
	public <T extends BlockEntity> void setBlockEntityRenderer(PlatformSupport.RegistryHandle<? extends BlockEntityType<T>> type, BlockEntityRendererProvider<? super T> renderer) {
		blockEntityRenderersToRegister.add(new BlockEntityRendererEntry<>(type, renderer));
	}
	
	private void actuallySetBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		blockEntityRenderersToRegister.forEach(entry -> entry.register(event));
	}
	
	///
	
	private final Map<PlatformSupport.RegistryHandle<? extends Block>, RenderType> renderTypesToRegister = new HashMap<>();
	
	@Override
	public void setRenderType(PlatformSupport.RegistryHandle<? extends Block> block, RenderType type) {
		renderTypesToRegister.put(block, type);
	}
	
	private void actuallySetRenderTypes(ModelRegistryEvent e) {
		renderTypesToRegister.forEach((handle, layer) -> ItemBlockRenderTypes.setRenderLayer(handle.get(), layer));
	}
	
	@Override
	public void installEarlyClientsideLeftClickCallback(EarlyClientsideLeftClickCallback callback) {
		//TODO
	}
	
	@Override
	public void installClientsideHoldLeftClickCallback(ClientsideHoldLeftClickCallback callback) {
		//TODO
	}
	
	@Override
	public void installClientsideUseBlockCallback(ClientsideUseBlockCallback callback) {
		//TODO
	}
	
	@Override
	public void setupCustomModelLoaders() {
		//Big TODO
	}
	
	@Override
	public void sendActionPacket(ActionPacket packet) {
		//TODO networking
	}
}
