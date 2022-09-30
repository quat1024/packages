package agency.highlysuspect.packages.platform.forge.client;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.net.ActionPacket;
import agency.highlysuspect.packages.platform.ClientPlatformSupport;
import agency.highlysuspect.packages.platform.PlatformSupport;
import agency.highlysuspect.packages.platform.forge.ForgeInit;
import agency.highlysuspect.packages.platform.forge.client.model.ForgePackageModel;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.LogicalSide;
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
	
	@Override
	public void setupCustomModelLoaders() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener((ModelRegistryEvent e) -> {
			ModelLoaderRegistry.registerLoader(ForgePackageModel.Loader.ID, new ForgePackageModel.Loader());
			//TODO package maker model too, i gotta figure out one before doing both though lol
		});
		
		//Forge's model system (IModelGeometry) cannot specify dependencies between models.
		//Its documentation states that it's a superset of UnbakedModel; this is a lie and we need to help it along here
		FMLJavaModLoadingContext.get().getModEventBus().addListener((ModelRegistryEvent e) -> {
			ForgeModelBakery.addSpecialModel(Packages.id("block/package"));
			ForgeModelBakery.addSpecialModel(Packages.id("block/package_maker"));
		});
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
	
	///
	
	public final List<EarlyClientsideLeftClickCallback> earlyLeftClickCallbacks = new ArrayList<>();
	
	@Override
	public void installEarlyClientsideLeftClickCallback(EarlyClientsideLeftClickCallback callback) {
		//I was going to be like "hey, credit where it's due, I needed a kludge for this on Fabric".
		//Turns out that ClickInputEvent is fired in both Minecraft#startAttack ***and Minecraft#continueAttack***???
		//This is the exact situation I am trying to use lower-level input-based events to *avoid*!!!
		//
		//Cancelling mining the block from the LeftClickBlock event (which is more of a "start mining block" event)
		//kinda causes you to restart trying to mine the block every tick, which is a problem when I am only trying
		//to detect the first left click. This makes sense tbh and happens on Fabric too.
		//That's why I use a lower-level click event anyway, I'm only interested in the first time you try to
		//mine the block, not all the other spam times.
		//
		//But because Forge fucking fires this event in continueAttack as well, completely defeating the purpose
		//of offering a click event separate from LeftClickBlock in the first place, mixin it is then.
		//
		//See MixinMinecraft.
		earlyLeftClickCallbacks.add(callback);
	}
	
	public final List<ClientsideHoldLeftClickCallback> holdLeftClickCallbacksForCreativeMode = new ArrayList<>();
	
	@Override
	public void installClientsideHoldLeftClickCallback(ClientsideHoldLeftClickCallback callback) {
		//I originally only used PlayerInteractEvent.LeftClickBlock in all situations. However, in Creative mode, this
		//event is fired *after* sending the START_DESTROY_BLOCK action to the server, and there is no way to cancel the
		//event in a way that suppresses the packet. (See Forge's patches to MultiPlayerGameMode, ctrl-f for "LeftClick".)
		//
		//I need to suppress the packet because sending the action seems to tick up the serverside creative mode breaking
		//timer, or something? Basically after clicking the package for more than 5 cumulative ticks it would break.
		//
		//This isn't an issue on Fabric because cancelling the "i am about to start breaking this block" event on the client
		//actually suppresses the "i am about to start breaking this block" packet too, which is arguably the correct behavior.
		//I'm not sure where the other case makes sense.
		//
		//See MixinMultiPlayerGameMode.
		holdLeftClickCallbacksForCreativeMode.add(callback);
		
		//Still, let's try to be good netizens and use the standard LeftClickBlock event in noncreative.
		//In non creative gamemodes it works just fine for my purposes.
		MinecraftForge.EVENT_BUS.addListener((PlayerInteractEvent.LeftClickBlock event) -> {
			if(event.isCanceled() || event.getSide() != LogicalSide.CLIENT || event.getPlayer().isSpectator()) return;
			if(event.getPlayer().isCreative()) return; //Creative mode handled via mixin
			
			InteractionResult r = callback.interact(event.getPlayer(), event.getWorld(), event.getHand(), event.getPos(), event.getFace());
			if(r.consumesAction()) {
				event.setCanceled(true);
				event.setCancellationResult(InteractionResult.CONSUME);
				event.setUseBlock(Event.Result.DENY); //I handled it
				event.setUseItem(Event.Result.DENY);
			}
		});
	}
	
	@Override
	public void installClientsideUseBlockCallback(ClientsideUseBlockCallback callback) {
		MinecraftForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> {
			if(event.isCanceled() || event.getSide() != LogicalSide.CLIENT) return;
			
			//this SHOULD NOT BE REQUIRED because i cancel the event with InteractionResult.CONSUME
			//which should make it not try the other hand
			//if(event.getHand() != InteractionHand.MAIN_HAND) return;
			
			InteractionResult r = callback.interact(event.getPlayer(), event.getWorld(), event.getHand(), event.getHitVec());
			if(r.consumesAction()) {
				event.setCanceled(true);
				event.setCancellationResult(InteractionResult.CONSUME);
				event.setUseBlock(Event.Result.DENY); //I handled it
				event.setUseItem(Event.Result.DENY);
			}
		});
	}
	
	@Override
	public void sendActionPacket(ActionPacket packet) {
		ForgeInit.CHANNEL.sendToServer(packet);
	}
}
