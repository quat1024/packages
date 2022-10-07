package agency.highlysuspect.packages.platform.fabric;

import agency.highlysuspect.packages.Packages;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public abstract class AbstractFabricPlatformConfig {
	protected abstract void parse(List<String> in);
	protected abstract List<String> write();
	protected abstract void install();
	
	public void setup(PackType type, String filename) {
		Path configPath = FabricLoader.getInstance().getConfigDir().resolve(filename);
		
		//load it once now
		configStuff(configPath);
		
		//and load it again on resource reload
		ResourceManagerHelper.get(type).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public ResourceLocation getFabricId() {
				return Packages.id("fabric-config-reload");
			}
			
			@Override
			public void onResourceManagerReload(ResourceManager resourceManager) {
				configStuff(configPath);
			}
		});
	}
	
	private void configStuff(Path configPath) {
		try {
			//make the config directory
			Files.createDirectories(configPath.getParent());
			
			//if a config file exists, read it
			if(Files.exists(configPath)) parse(Files.readAllLines(configPath));
			
			//update the config global to reflect my current state
			install();
			
			//always write the file back (in case i add a new option later, it should end up in the file)
			Files.write(configPath, write(), StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("SameParameterValue") //i KNOW i'm overabstracting
	protected  <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value) {
		for(E e : enumClass.getEnumConstants()) {
			if(e.name().equalsIgnoreCase(value)) return e;
		}
		//TODO sauce this error handling up a bit
		throw new IllegalArgumentException("not valid enum value");
	}
	
	protected String writeEnum(Enum<?> e) {
		return e.name().toLowerCase(Locale.ROOT);
	}
}
