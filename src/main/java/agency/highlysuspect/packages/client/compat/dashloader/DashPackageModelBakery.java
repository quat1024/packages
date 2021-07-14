package agency.highlysuspect.packages.client.compat.dashloader;

import agency.highlysuspect.packages.client.model.PackageModelBakery;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.oskarstrom.dashloader.DashRegistry;

public class DashPackageModelBakery {
    @Serialize(order = 0)
    public final int baseModel;
    @Serialize(order = 1)
    public final int specialFrameSprite;
    @Serialize(order = 2)
    public final int specialInnerSprite;

    public DashPackageModelBakery(@Deserialize("baseModel") int baseModel,
                                  @Deserialize("specialFrameSprite") int specialFrameSprite,
                                  @Deserialize("specialInnerSprite") int specialInnerSprite) {
        this.baseModel = baseModel;
        this.specialFrameSprite = specialFrameSprite;
        this.specialInnerSprite = specialInnerSprite;
    }

    public DashPackageModelBakery(PackageModelBakery bakery, DashRegistry registry) {
        baseModel = registry.createModelPointer(bakery.baseModel());
        specialFrameSprite = registry.createSpritePointer(bakery.specialFrameSprite());
        specialInnerSprite = registry.createSpritePointer(bakery.specialInnerSprite());
    }

    public PackageModelBakery toUndash(DashRegistry registry) {
        return new PackageModelBakery(registry.getModel(baseModel), registry.getSprite(specialFrameSprite), registry.getSprite(specialInnerSprite));
    }
}
