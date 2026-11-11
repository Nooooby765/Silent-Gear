package net.silentchaos512.gear.client.models;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.item.ICoreTool;
import net.silentchaos512.gear.api.parts.*;
import net.silentchaos512.gear.client.ColorHandlers;
import net.silentchaos512.gear.client.util.GearClientHelper;
import net.silentchaos512.gear.init.ModItems;
import net.silentchaos512.gear.item.gear.CoreBow;
import net.silentchaos512.gear.util.GearData;
import net.silentchaos512.gear.util.GearHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public final class ToolModel implements IModel {
    private static final IModel MODEL = new ToolModel();

    public static final Map<UUID, Float> bowPull = new HashMap<>();

    private final ImmutableMap<String, String> textures;

    private ToolModel() {
        textures = ImmutableMap.of();
    }

    private ToolModel(ImmutableMap<String, String> textures) {
        this.textures = textures;
    }

    @Override
    public IModel retexture(ImmutableMap<String, String> textures) {
        return new ToolModel(textures);
    }

    @Override
    public IModel process(ImmutableMap<String, String> customData) {
        return new ToolModel(customData);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return ImmutableList.of();
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        ImmutableSet.Builder<ResourceLocation> builder = ImmutableSet.builder();

        for (String toolClass : ModItems.toolClasses.keySet()) {
            boolean hasGuard = "sword".equals(toolClass);
            ICoreTool item = ModItems.toolClasses.get(toolClass);

            for (ItemPart part : PartRegistry.getValues()) {
                // Non-rendered positions (coating, fletching, armor, any) contribute no tool layers;
                // requesting their texture variants would only add missing sprites.
                IPartPosition position = part.getPartPosition();
                if (!IPartPosition.RENDER_LAYERS.contains(position))
                    continue;

                // Do not skip disabled parts: gear made before a material was disabled still renders
                // with it, and unstitched sprites would silently become missingno.
                ItemPartData partData = ItemPartData.instance(part);

                // Every display variant the part can select at runtime (gear type overrides such as
                // "hoe": { "texture_suffix": "generic_hc" } included), for every animation frame
                for (int frame = 0; frame < item.getAnimationFrames(); ++frame) {
                    builder.addAll(partData.getTextureVariants(toolClass, position, frame));

                    if (hasGuard && part instanceof PartMain)
                        builder.addAll(partData.getTextureVariants(toolClass, PartPositions.GUARD, frame));
                }

                // Broken texture (does not depend on the animation frame)
                ResourceLocation texBroken = partData.getBrokenTexture(ItemStack.EMPTY, toolClass, position);
                if (texBroken != null)
                    builder.add(texBroken);
            }
        }

        return builder.build();
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        ImmutableMap<TransformType, TRSRTransformation> transformMap = PerspectiveMapWrapper.getTransforms(state);
        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

        // Get textures in proper render order
        ImmutableList<ResourceLocation> textures = IPartPosition.RENDER_LAYERS.stream()
                .map(IPartPosition::getModelIndex)
                .map(this.textures::get)
                .filter(Objects::nonNull)
                .map(ResourceLocation::new)
                .collect(ImmutableList.toImmutableList());

        IBakedModel model = (new ItemLayerModel(textures)).bake(state, format, bakedTextureGetter);
        builder.addAll(model.getQuads(null, null, 0));

        int layerCount = textures.size();
        return new ToolModel.Baked(this, createQuadsMap(model, layerCount), format, Maps.immutableEnumMap(transformMap), new HashMap<>());
    }

    private static ImmutableList<ImmutableList<BakedQuad>> createQuadsMap(IBakedModel model, int layerCount) {
        List<ImmutableList.Builder<BakedQuad>> list = new ArrayList<>();
        for (int i = 0; i < layerCount; ++i)
            list.add(ImmutableList.builder());

        for (BakedQuad quad : model.getQuads(null, null, 0))
            list.get(quad.getTintIndex()).add(quad);

        ImmutableList.Builder<ImmutableList<BakedQuad>> builder = ImmutableList.builder();
        for (ImmutableList.Builder<BakedQuad> b : list)
            builder.add(b.build());

        return builder.build();
    }

    @Nonnull
    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }

    public static final class Loader implements ICustomModelLoader {
        public static Loader INSTANCE = new Loader();

        @Override
        public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {
        }

        @Override
        public boolean accepts(@Nonnull ResourceLocation modelLocation) {
            // Key lookup instead of a Stream scan; same set of paths as toolClasses' keys.
            return modelLocation.getNamespace().equals(SilentGear.MOD_ID)
                    && ModItems.toolClasses.containsKey(modelLocation.getPath());
        }

        @Nonnull
        @Override
        public IModel loadModel(@Nonnull ResourceLocation modelLocation) {
            return MODEL;
        }
    }

    private static final class OverrideHandler extends ItemOverrideList {

        public static final OverrideHandler INSTANCE = new OverrideHandler();

        OverrideHandler() {
            super(ImmutableList.of());
        }

        @Override
        @Nonnull
        public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, @Nonnull ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
            if (!(stack.getItem() instanceof ICoreTool))
                return originalModel;

            int animationFrame = getAnimationFrame(stack, world, entity);
            String key = GearData.getCachedModelKey(stack, animationFrame);
            GearClientHelper.updateDebugModelKey(stack, key);

            IBakedModel cachedModel = GearClientHelper.modelCache.get(key);
            if (cachedModel == null) {
                ICoreTool itemTool = (ICoreTool) stack.getItem();
                String toolClass = itemTool.getGearClass();
                boolean isBroken = GearHelper.isBroken(stack);

                PartDataList parts = GearData.getConstructionParts(stack);
                Map<IPartPosition, ItemPartData> renderLayers = new LinkedHashMap<>();
                for (IPartPosition position : IPartPosition.RENDER_LAYERS) {
                    // Special-case positions: these return a default part for rendering if the part
                    // is missing or invalid.
                    if (position == PartPositions.HEAD) {
                        renderLayers.put(PartPositions.HEAD, itemTool.getPrimaryPart(stack));
                    } else if (position == PartPositions.GUARD && itemTool.hasSwordGuard()) {
                        renderLayers.put(PartPositions.GUARD, itemTool.getSecondaryPart(stack));
                    } else if (position == PartPositions.ROD) {
                        renderLayers.put(PartPositions.ROD, itemTool.getRodPart(stack));
                    } else {
                        final ItemPartData part = parts.firstInPosition(position);
                        if (part != null) renderLayers.put(position, part);
                    }
                }

                ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
                renderLayers.forEach((pos, part) ->
                        processTexture(stack, toolClass, pos, part, animationFrame, isBroken, builder));

                ToolModel.Baked model = (ToolModel.Baked) originalModel;
                IModel parent = model.getParent().retexture(builder.build());
                Function<ResourceLocation, TextureAtlasSprite> textureGetter = location ->
                        Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
                IBakedModel bakedModel = parent.bake(new SimpleModelState(model.transforms), model.getVertexFormat(), textureGetter);
                GearClientHelper.modelCache.put(key, bakedModel);

                ColorHandlers.gearColorCache.put(key, renderLayers.values().stream()
                        .map(part -> part.getColor(stack, animationFrame))
                        .toArray(Integer[]::new));

                return bakedModel;
            }

            return cachedModel;
        }

        private static void processTexture(ItemStack stack, String toolClass, IPartPosition position, @Nullable ItemPartData part, int animationFrame, boolean isBroken, ImmutableMap.Builder<String, String> builder) {
            if (part != null) {
                ResourceLocation texture = isBroken
                        ? part.getBrokenTexture(stack, toolClass, position)
                        : part.getTexture(stack, toolClass, position, animationFrame);
                if (texture != null) {
                    builder.put(position.getModelIndex(), texture.toString());
                }
            }
        }

        private int getAnimationFrame(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
            if (stack.getItem() instanceof CoreBow) {
                UUID uuid = GearData.getUUID(stack);
                if (bowPull.containsKey(uuid)) {
                    float pull = bowPull.get(uuid);
                    if (pull > 0.9f)
                        return 3;
                    else if (pull > 0.65f)
                        return 2;
                    else
                        return 1;
                }
            }

            return 0;
        }
    }

    public static final class Baked extends AbstractToolModel {
        public static Baked instance;

        Baked(IModel parent, ImmutableList<ImmutableList<BakedQuad>> immutableList, VertexFormat format, ImmutableMap<TransformType, TRSRTransformation> transforms, Map<String, IBakedModel> cache) {
            super(parent, immutableList, format, transforms, cache);
            instance = this;
        }

        @Nonnull
        @Override
        public ItemOverrideList getOverrides() {
            return OverrideHandler.INSTANCE;
        }

        @Override
        public boolean isBuiltInRenderer() {
            return true;
        }
    }
}
