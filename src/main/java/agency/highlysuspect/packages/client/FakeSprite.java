package agency.highlysuspect.packages.client;

import net.minecraft.client.resource.metadata.AnimationFrameResourceMetadata;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.Lazy;

import java.util.Collections;

public class FakeSprite extends Sprite {
	//A sprite that always appears to fill the entire texture atlas when asked where it is.
	//This is pretty cursed and it doesn't actually belong to a texture.
	//Vanilla doesn't seem to ask many questions.  
	public FakeSprite(Identifier type) {
		super(null, new Info(
			type,
			1,
			1,
			new AnimationResourceMetadata(
				Collections.singletonList(new AnimationFrameResourceMetadata(0, -1)),
				1,
				1,
				1,
				false
			)
		), 0, 1, 1, 0, 0, IMAGE.get());
	}
	
	//This method, for some reason, returns 0.25 in MissingSprite.
	//That fucks up the UV generation in json quad emitter machinery, seems to zoom in by 25% or something, really weird.
	//Overriding this is the whole reason I make my own fake sprite and don't use MissingSprite.
	@Override
	public float getAnimationFrameDelta() {
		return 0;
	}
	
	//copy paste from mojang missingno code so I don't have to accessor it
	//this is cursed
	private static final Lazy<NativeImage> IMAGE = new Lazy<>(() -> {
		NativeImage nativeImage = new NativeImage(16, 16, false);
		for(int k = 0; k < 16; ++k) {
			for(int l = 0; l < 16; ++l) {
				if (k < 4 ^ l < 4) { //wow a different pattern
					nativeImage.setPixelRgba(l, k, 0x00FF00); //wow a different color
				} else {
					nativeImage.setPixelRgba(l, k, 0x0088FF); //amazing
				}
			}
		}
		
		nativeImage.untrack();
		return nativeImage;
	});
}
