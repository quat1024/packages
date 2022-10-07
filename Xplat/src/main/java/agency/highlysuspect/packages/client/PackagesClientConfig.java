package agency.highlysuspect.packages.client;

import agency.highlysuspect.packages.net.PackageAction;
import agency.highlysuspect.packages.platform.ClientPlatformConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class PackagesClientConfig {
	public static PackagesClientConfig makeConfig(ClientPlatformConfig cfgSource) {
		PackagesClientConfig cfg = new PackagesClientConfig();

		cfg.insertOne = cfgSource.insertOneBinding();
		cfg.insertStack = cfgSource.insertStackBinding();
		cfg.insertAll = cfgSource.insertAllBinding();
		cfg.takeOne = cfgSource.takeOneBinding();
		cfg.takeStack = cfgSource.takeStackBinding();
		cfg.takeAll = cfgSource.takeAllBinding();

		cfg.punchRepeat = cfgSource.punchRepeat();
		cfg.fontVerticalShift = cfgSource.fontVerticalShift();
		cfg.meshBackend = cfgSource.meshBackend();
		cfg.cacheMeshes = cfgSource.cacheMeshes();
		cfg.swapRedAndBlue = cfgSource.swapRedAndBlue();
		cfg.frexSupport = cfgSource.frexSupport();

		cfg.finish();
		
		return cfg;
	}
	
	//todo: none of these default values get used at all, that's something i should fix
	// default values are an implementation detail of PlatformConfig2
	// (the config situation in this mod is Very Bad)
	public PackageActionBinding insertOne = new PackageActionBinding.Builder(PackageAction.INSERT_ONE).use().build();
	public PackageActionBinding insertStack = new PackageActionBinding.Builder(PackageAction.INSERT_STACK).use().sneak().build();
	public PackageActionBinding insertAll = new PackageActionBinding.Builder(PackageAction.INSERT_ALL).use().ctrl().build();
	public PackageActionBinding takeOne = new PackageActionBinding.Builder(PackageAction.TAKE_ONE).punch().build();
	public PackageActionBinding takeStack = new PackageActionBinding.Builder(PackageAction.TAKE_STACK).punch().sneak().build();
	public PackageActionBinding takeAll = new PackageActionBinding.Builder(PackageAction.TAKE_ALL).punch().ctrl().build();
	
	public int punchRepeat = -1;
	public double fontVerticalShift = 0;
	public MeshBackend meshBackend = MeshBackend.FRAPI_MESH;
	public boolean cacheMeshes = false;
	public boolean swapRedAndBlue = false;
	public boolean frexSupport = true;
	
	//Bindings sorted such that the more specific ones are at the front of the list (check ctrl-shift-alt, before ctrl-alt, before alt)
	public transient List<PackageActionBinding> sortedBindings = new ArrayList<>();
	
	public void finish() {
		sortedBindings = new ArrayList<>();
		sortedBindings.addAll(Arrays.asList(insertOne, insertStack, insertAll, takeOne, takeStack, takeAll));
		sortedBindings.sort(Comparator.naturalOrder());
	}
}
