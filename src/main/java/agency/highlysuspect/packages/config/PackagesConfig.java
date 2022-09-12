package agency.highlysuspect.packages.config;

import agency.highlysuspect.packages.config.ConfigShape2.Comment;
import agency.highlysuspect.packages.config.ConfigShape2.Section;
import agency.highlysuspect.packages.config.ConfigShape2.SkipDefault;
import agency.highlysuspect.packages.net.PackageAction;
import com.mojang.datafixers.util.Pair;

import java.util.ArrayList;
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
		"Separate multiple items with hyphens. You can disable an action entirely by leaving it blank.",
		"These actions affect your game client only.",
		"",
		"How do you insert one item into the package?",
	})
	public PackageActionBinding insertOne = new PackageActionBinding.Builder().use().build();
	@Comment("How do you insert one stack of items into the package?")
	public PackageActionBinding insertStack = new PackageActionBinding.Builder().use().sneak().build();
	@Comment("How do you insert everything in your inventory that fits into the package?")
	public PackageActionBinding insertAll = new PackageActionBinding.Builder().use().ctrl().build();
	
	@Comment("How do you remove one item from the package?")
	public PackageActionBinding takeOne = new PackageActionBinding.Builder().punch().build();
	@Comment("How do you remove one stack of items from the package?")
	public PackageActionBinding takeStack = new PackageActionBinding.Builder().punch().sneak().build();
	@Comment("How do you clear the package?")
	public PackageActionBinding takeAll = new PackageActionBinding.Builder().punch().ctrl().build();
	
	//contains bindings sorted such that the more specific ones are at the front of the list (check ctrl-shift-alt, before ctrl-alt, before alt)
	//TODO: make the parser optionally pass the field name as context, so i don't need this Pair bs, or make my own keybindings file, or something
	public transient List<Pair<PackageAction, PackageActionBinding>> sortedBindings = new ArrayList<>();
	
	public void finish() {
		sortedBindings = new ArrayList<>();
		sortedBindings.add(Pair.of(PackageAction.INSERT_ONE, insertOne));
		sortedBindings.add(Pair.of(PackageAction.INSERT_STACK, insertStack));
		sortedBindings.add(Pair.of(PackageAction.INSERT_ALL, insertAll));
		sortedBindings.add(Pair.of(PackageAction.TAKE_ONE, takeOne));
		sortedBindings.add(Pair.of(PackageAction.TAKE_STACK, takeStack));
		sortedBindings.add(Pair.of(PackageAction.TAKE_ALL, takeAll));
		sortedBindings.sort(Comparator.<Pair<PackageAction, PackageActionBinding>>comparingInt(p -> p.getSecond().specificity()).reversed());
	}
}
