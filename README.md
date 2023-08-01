Packages
========

Worse Barrels successor for ~~Fabric~~ Fabric and also Forge ~~1.15.2~~ ~~1.16.4~~ ~~1.17.0~~ ~~1.17.1~~ ~~1.18~~ ~~1.18.2~~ ~~1.19.2~~ ~~1.19.4~~ 1.20.1.

Package Crafter model by [Kat](https://kat.blue).

# Quick note about model handling

The xplat set contains `packages:block/package` and `packages:block/package_maker`. These models use the "special" textures `packages:package_special/frame` and `packages:package_special/inner` to mark which quads should be retextured, and as such these models are not expected to be displayed as-is to players. The xplat set also points the Package and Package Crafter's blockstates at the models `packages:special/package` and `packages:special/package_maker` (with `special` instead of `block`) respectively. Implemetations of the `special` model load the corresponding `block` model and perform the retexturing at runtime.

On Fabric, there is a `ModelLoadingRegistry` API. Resource providers hook requests for specific files, and variant providers hook requests for specific `ModelResourceLocation`s. Requests for `special/package` and `item/package#inventory` are diverted to the appropriate dynamic model.

On Forge, there is a `RegisterGeometryLoaders` API. I register a geometry loader for the package/packagemaker models and ship model JSONs with the `loader` field set, which causes Forge to load the appropriate dynamic model.

# License

LGPL-3.0-or-later