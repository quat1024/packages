package agency.highlysuspect.packages.client.compat.dashloader;


import agency.highlysuspect.packages.client.model.PackageMakerModel;
import agency.highlysuspect.packages.client.model.PackageModel;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.oskarstrom.dashloader.DashRegistry;
import net.oskarstrom.dashloader.api.annotation.DashObject;
import net.oskarstrom.dashloader.model.DashModel;

@DashObject(PackageMakerModel.Baked.class)
@SuppressWarnings("unused") //activej; DashLoader
public class DashPackageMakerModel implements DashModel {
	public DashPackageMakerModel(@Deserialize("data") DashPackageModelBakery data) {
		this.data = data;
	}
	
	public DashPackageMakerModel(PackageMakerModel.Baked model, DashRegistry registry) {
		data = new DashPackageModelBakery(model.bakery, registry);
	}
	
	@Serialize(order = 0) public final DashPackageModelBakery data;
	
	@Override
	public PackageMakerModel.Baked toUndash(DashRegistry registry) {
		return new PackageMakerModel.Baked(data.toUndash(registry));
	}
	
	@Override
	public int getStage() {
		return 3;
	}
}
