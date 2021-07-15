package agency.highlysuspect.packages.client.compat.dashloader;


import agency.highlysuspect.packages.client.model.PackageModel;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.oskarstrom.dashloader.DashRegistry;
import net.oskarstrom.dashloader.api.annotation.DashObject;
import net.oskarstrom.dashloader.model.DashModel;

@DashObject(PackageModel.Baked.class)
public class DashPackageModel implements DashModel {
	public DashPackageModel(@Deserialize("data") DashPackageModelBakery data) {
		this.data = data;
	}
	
	public DashPackageModel(PackageModel.Baked model, DashRegistry registry) {
		data = new DashPackageModelBakery(model.bakery, registry);
	}
	
	@Serialize(order = 0) public final DashPackageModelBakery data;
	
	@Override
	public PackageModel.Baked toUndash(DashRegistry registry) {
		return new PackageModel.Baked(data.toUndash(registry));
	}
	
	@Override
	public int getStage() {
		return 3;
	}
}
