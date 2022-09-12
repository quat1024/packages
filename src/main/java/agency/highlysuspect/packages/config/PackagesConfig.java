package agency.highlysuspect.packages.config;

import agency.highlysuspect.packages.config.ConfigShape2.Comment;
import agency.highlysuspect.packages.config.ConfigShape2.Section;
import agency.highlysuspect.packages.config.ConfigShape2.SkipDefault;
import agency.highlysuspect.packages.net.PackageAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
public class PackagesConfig {
	@SuppressWarnings("unused")
	@SkipDefault private int configVersion = 0;
	
	////////////////////////////////////////////
	@Section("Interactions (client-controlled)")
	////////////////////////////////////////////
	
	@Comment({
		"Specify at least 'use' (right click) or 'punch' (left click), and optionally add",
		"any combination of 'ctrl', 'alt', or 'sneak' to require some modifier keys.",
		"Separate multiple items with hyphens. Disable an action entirely by leaving it blank.",
		"These actions affect your game client only, not any other players on the server.",
		"",
		"How do you insert one item into the package?",
	})
	@PackageActionBinding.For(PackageAction.INSERT_ONE) //Weird annotation needed so the deserializer knows which PackageAction it's for... isn't great, but it'll do
	public PackageActionBinding insertOne = new PackageActionBinding.Builder(PackageAction.INSERT_ONE).use().build();
	
	@Comment("How do you insert one stack of items into the package?")
	@PackageActionBinding.For(PackageAction.INSERT_STACK)
	public PackageActionBinding insertStack = new PackageActionBinding.Builder(PackageAction.INSERT_STACK).use().sneak().build();
	
	@Comment("How do you insert everything in your inventory that fits into the package?")
	@PackageActionBinding.For(PackageAction.INSERT_ALL)
	public PackageActionBinding insertAll = new PackageActionBinding.Builder(PackageAction.INSERT_ALL).use().ctrl().build();
	
	@Comment("How do you take one item from the package?")
	@PackageActionBinding.For(PackageAction.TAKE_ONE)
	public PackageActionBinding takeOne = new PackageActionBinding.Builder(PackageAction.TAKE_ONE).punch().build();
	
	@Comment("How do you take one stack of items from the package?")
	@PackageActionBinding.For(PackageAction.TAKE_STACK)
	public PackageActionBinding takeStack = new PackageActionBinding.Builder(PackageAction.TAKE_STACK).punch().sneak().build();
	
	@Comment("How do you clear all items from the package?")
	@PackageActionBinding.For(PackageAction.TAKE_ALL)
	public PackageActionBinding takeAll = new PackageActionBinding.Builder(PackageAction.TAKE_ALL).punch().ctrl().build();
	
	//Bindings sorted such that the more specific ones are at the front of the list (checks ctrl-shift-alt, before ctrl-alt, before alt)
	public transient List<PackageActionBinding> sortedBindings = new ArrayList<>();
	
	public void finish() {
		sortedBindings = new ArrayList<>();
		sortedBindings.addAll(Arrays.asList(insertOne, insertStack, insertAll, takeOne, takeStack, takeAll));
		sortedBindings.sort(Comparator.naturalOrder());
	}
}
