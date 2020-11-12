package agency.highlysuspect.packages.client.model;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public abstract class DelegatingUnbakedModel implements UnbakedModel {
	public DelegatingUnbakedModel(UnbakedModel parent) {
		this.parent = parent;
	}
	
	public final UnbakedModel parent;
	
	@Override
	public Collection<Identifier> getModelDependencies() {
		return parent.getModelDependencies();
	}
	
	@Override
	public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
		return parent.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences);
	}
	
	@Override
	public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
		return parent.bake(loader, textureGetter, rotationContainer, modelId);
	}
}
