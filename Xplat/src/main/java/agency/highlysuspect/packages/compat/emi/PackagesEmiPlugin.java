package agency.highlysuspect.packages.compat.emi;

import agency.highlysuspect.packages.Packages;
import agency.highlysuspect.packages.block.PBlocks;
import agency.highlysuspect.packages.block.PackageMakerBlockEntity;
import agency.highlysuspect.packages.item.PItems;
import agency.highlysuspect.packages.junk.PTags;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@EmiEntrypoint
public class PackagesEmiPlugin implements EmiPlugin {
	public static final EmiRecipeCategory PACKAGE_MAKER_CATEGORY = new EmiRecipeCategory(
		Packages.id("package_maker"),
		EmiStack.of(PBlocks.PACKAGE_MAKER.get())
	) {
		@Override
		public Component getName() {
			return Component.translatable("block.packages.package_maker");
		}
	};
	
	@Override
	public void register(EmiRegistry registry) {
		registry.addCategory(PACKAGE_MAKER_CATEGORY);
		registry.addWorkstation(PACKAGE_MAKER_CATEGORY, EmiStack.of(PBlocks.PACKAGE_MAKER.get()));
		registry.addRecipe(new PackageRecipe());
	}
	
	static class PackageRecipe implements EmiRecipe {
		private static final int uniq = new Random().nextInt();
		private static final List<DyeColor> validDyes = new ArrayList<>();
		private static final List<Block> validFrames = new ArrayList<>();
		private static final List<Block> validInners = new ArrayList<>();
		
		static {
			for(DyeColor dyeColor : DyeColor.values()) {
				if(PackageMakerBlockEntity.matchesDyeSlot(new ItemStack(DyeItem.byColor(dyeColor)))) {
					validDyes.add(dyeColor);
				}
			}
			BuiltInRegistries.BLOCK.forEach(block -> {
				ItemStack s = new ItemStack(block);
				if(PackageMakerBlockEntity.matchesFrameSlot(s)) {
					validFrames.add(block);
				}
				if(PackageMakerBlockEntity.matchesInnerSlot(s)) {
					validInners.add(block);
				}
			});
		}
		
		@Override
		public EmiRecipeCategory getCategory() {
			return PACKAGE_MAKER_CATEGORY;
		}
		
		@Override
		public @Nullable ResourceLocation getId() {
			return Packages.id("/package_maker/package");
		}
		
		@Override
		public List<EmiIngredient> getInputs() {
			return List.of(
				EmiIngredient.of(validFrames.stream().map(EmiStack::of).toList()),
				EmiIngredient.of(validInners.stream().map(EmiStack::of).toList()),
				EmiIngredient.of(validDyes.stream().map(DyeItem::byColor).map(EmiStack::of).toList()),
				EmiIngredient.of(PTags.THINGS_YOU_NEED_FOR_PACKAGE_CRAFTING)
			);
		}
		
		@Override
		public List<EmiStack> getOutputs() {
			return List.of(EmiStack.of(PItems.PACKAGE.get()));
		}
		
		@Override
		public int getDisplayWidth() {
			return 106;
		}
		
		@Override
		public int getDisplayHeight() {
			return 38;
		}
		
		@Override
		public void addWidgets(WidgetHolder widgets) {
			widgets.addGeneratedSlot(r -> getStack(r, 0), uniq, 0, 0);
			widgets.addGeneratedSlot(r -> getStack(r, 1), uniq, 20, 0);
			widgets.addGeneratedSlot(r -> getStack(r, 2), uniq, 0, 20);
			widgets.addSlot(EmiIngredient.of(PTags.THINGS_YOU_NEED_FOR_PACKAGE_CRAFTING), 20, 20);
			
			widgets.addTexture(EmiTexture.EMPTY_ARROW, 48, 10);
			
			widgets.addGeneratedSlot(r -> getStack(r, 3), uniq, 80, 6).large(true).recipeContext(this);
		}
		
		@Override
		public boolean supportsRecipeTree() {
			return false;
		}
		
		private EmiStack getStack(Random r, int i) {
			Block frame = validFrames.get(r.nextInt(validFrames.size()));
			Block inner = validInners.get(r.nextInt(validInners.size()));
			DyeColor dye = validDyes.get(r.nextInt(validDyes.size()));
			EmiStack output = EmiStack.of(PItems.PACKAGE.get().createCustomizedStack(frame, inner, dye));
			return new EmiStack[]{
				EmiStack.of(frame),
				EmiStack.of(inner),
				EmiStack.of(DyeItem.byColor(dye)),
				output
			}[i];
		}
	}
}