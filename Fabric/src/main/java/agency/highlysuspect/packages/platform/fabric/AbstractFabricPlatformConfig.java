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

/**
 * Fabric doesn't come with a config library so I had to write my own shitty one.
 * (Quilt has one, for what it's worth. Do I want to set that up in my dev workspace? Nah.)
 */
public abstract class AbstractFabricPlatformConfig {
	protected abstract boolean parseKeyValuePair(String key, String value);
	protected abstract List<String> write();
	protected abstract void install();
	
	//Goofy hack to suppress "unknown config key xxx" messages when we are upgrading to the version of Packages with split client/server configs.
	//I could avoid this by writing a better config upgrader that doesn't leave unknown keys everywhere, but I am lazy.
	public static boolean FIRST_RUN_WITH_SPLIT_CONFIGS = false;
	
	protected void parse(Path configPath, List<String> lines) {
		int lineNo = 0;
		for(String line : lines) {
			lineNo++;
			line = line.trim();
			if(line.startsWith("#")) continue; //comments
			
			//Split on key-value pairs
			int eqIndex = line.indexOf('=');
			if(eqIndex == -1) continue;
			String key = line.substring(0, eqIndex).trim();
			String value = line.substring(eqIndex + 1).trim();
			
			try {
				boolean knownKey = parseKeyValuePair(key, value);
				if(!knownKey && !FIRST_RUN_WITH_SPLIT_CONFIGS) Packages.LOGGER.warn("unknown config key " + key);
			} catch (Exception e) {
				Packages.LOGGER.warn("Exception while parsing line " + lineNo + " of the config file at " + configPath);
				Packages.LOGGER.warn("---");
				Packages.LOGGER.warn(line);
				Packages.LOGGER.warn("---");
				Packages.LOGGER.warn("Configuration state may be inconsistent.");
				Packages.LOGGER.warn(e);
			}
		}
	}
	
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
			if(Files.exists(configPath)) parse(configPath, Files.readAllLines(configPath));
			
			//update the config global to reflect my current state
			install();
			
			//always write the file back (in case i add a new option later, it should end up in the file)
			Files.write(configPath, write(), StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
