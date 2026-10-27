package net.silentchaos512.gear.item;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.item.ICoreItem;
import net.silentchaos512.gear.api.item.ICoreTool;
import net.silentchaos512.gear.api.item.IStatItem;
import net.silentchaos512.gear.api.parts.*;
import net.silentchaos512.gear.api.stats.ItemStat;
import net.silentchaos512.gear.api.stats.StatInstance;
import net.silentchaos512.gear.api.traits.Trait;
import net.silentchaos512.gear.client.util.GearClientHelper;
import net.silentchaos512.gear.client.util.TooltipFlagTC;
import net.silentchaos512.gear.init.ModItems;
import net.silentchaos512.gear.init.ModMaterials;
import net.silentchaos512.gear.item.blueprint.Blueprint;
import net.silentchaos512.gear.util.GearData;
import net.silentchaos512.gear.util.GearGenerator;
import net.silentchaos512.gear.util.TraitHelper;
import net.silentchaos512.lib.util.I18nHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ToolHead extends Item implements IStatItem {
    private static final String NBT_ROOT = "ToolHeadData";
    private static final String NBT_TOOL_CLASS = "ToolClass";
    private static final String NBT_MATERIALS = "Materials";
    private static final String NBT_STAT_CACHE = "StatCache";
    private static final String NBT_IS_EXAMPLE = "IsExample";
    private static final String NBT_MODEL_KEY = "ModelKey";

    /**
     * Create a stack with the given materials. Best for getting crafting results.
     */
    public ItemStack getStack(String toolClass, PartDataList parts) {
        ItemStack result = new ItemStack(this);
        NBTTagCompound tags = getData(result);
        tags.setString(NBT_TOOL_CLASS, toolClass);

        NBTTagList tagList = new NBTTagList();
        for (ItemPartData data : parts.getMains()) {
            NBTTagCompound tagCompound = data.writeToNBT(new NBTTagCompound());
            tagList.appendTag(tagCompound);
        }
        tags.setTag(NBT_MATERIALS, tagList);

        writeStatCache(result, parts);

        // The model key is fully determined by the head; storing it avoids rebuilding it from
        // NBT every frame in the model override and color handlers.
        List<ItemPartData> mains = parts.getMains();
        ItemPartData primary = mains.isEmpty() ? null : mains.get(0);
        ItemPartData secondary = "sword".equals(toolClass) && mains.size() > 1
                ? mains.get(1) : null;
        tags.setString(NBT_MODEL_KEY, getModelKey(toolClass, primary, secondary));

        return result;
    }

    /**
     * Create a stack with a single part. Best for sub-items.
     */
    public ItemStack getStack(String toolClass, PartMain part, boolean isExample) {
        // Assign a C grade to example outputs
        ItemPartData partData = ItemPartData.instance(part, MaterialGrade.C);
        ItemStack result = new ItemStack(this);
        NBTTagCompound tags = getData(result);
        tags.setString(NBT_TOOL_CLASS, toolClass);
        tags.setBoolean(NBT_IS_EXAMPLE, isExample);

        NBTTagList tagList = new NBTTagList();
        NBTTagCompound partTags = new NBTTagCompound();
        partData.writeToNBT(partTags);

        tagList.appendTag(partTags);
        final boolean hasGuard = "sword".equals(toolClass);
        if (hasGuard) tagList.appendTag(partTags);
        tags.setTag(NBT_MATERIALS, tagList);

        writeStatCache(result, PartDataList.of(partData));
        tags.setString(NBT_MODEL_KEY, getModelKey(toolClass, partData, hasGuard ? partData : null));
        return result;
    }

    private static void writeStatCache(ItemStack stack, PartDataList parts) {
        ICoreItem item = ModItems.toolClasses.get(getToolClass(stack));
        Map<Trait, Integer> traits = TraitHelper.getTraits(stack, parts);
        double synergy = GearData.calculateSynergyValue(parts, parts.getUniqueParts(true), traits);
        Multimap<ItemStat, StatInstance> stats = GearData.getStatModifiers(new ItemStack(item.getItem()), parts, synergy);

        NBTTagCompound tags = new NBTTagCompound();
        for (ItemStat stat : stats.keySet()) {
            float value = stat.compute(0f, stats.get(stat));
            tags.setFloat(stat.getName().getPath(), value);
        }
        tags.setFloat("synergy", (float) synergy);
        getData(stack).setTag(NBT_STAT_CACHE, tags);
    }

    /**
     * Get the gear class this item matches.
     */
    @Nonnull
    public static String getToolClass(ItemStack stack) {
        return getData(stack).getString(NBT_TOOL_CLASS);
    }

    /**
     * Get the primary (first) part the gear head was constructed with.
     */
    @Nullable
    public static ItemPartData getPrimaryPart(ItemStack stack) {
        NBTTagCompound tags = getData(stack);
        if (!tags.hasKey(NBT_MATERIALS))
            return ItemPartData.instance(ModMaterials.mainWood);
        NBTTagList tagList = tags.getTagList(NBT_MATERIALS, 10);
        if (tagList.tagCount() == 0)
            return ItemPartData.instance(ModMaterials.mainWood);
        NBTTagCompound partTags = tagList.getCompoundTagAt(0);
        return ItemPartData.readFromNBT(partTags);
    }

    @Nullable
    public static ItemPartData getSecondaryPart(ItemStack stack) {
        NBTTagCompound tags = getData(stack);
        if (!tags.hasKey(NBT_MATERIALS))
            return ItemPartData.instance(ModMaterials.mainWood);
        NBTTagList tagList = tags.getTagList(NBT_MATERIALS, 10);
        if (tagList.tagCount() < 2)
            return ItemPartData.instance(ModMaterials.mainWood);
        NBTTagCompound partTags = tagList.getCompoundTagAt(1);
        return ItemPartData.readFromNBT(partTags);
    }

    /**
     * Get all parts the gear head was constructed with.
     */
    @Nonnull
    public static Collection<ItemPartData> getAllParts(ItemStack stack) {
        ImmutableList.Builder<ItemPartData> builder = ImmutableList.builder();
        NBTTagCompound tags = getData(stack);
        if (tags.hasKey(NBT_MATERIALS)) {
            NBTTagList tagList = tags.getTagList(NBT_MATERIALS, 10);
            for (NBTBase nbt : tagList) {
                ItemPartData data = ItemPartData.readFromNBT((NBTTagCompound) nbt);
                if (data != null) builder.add(data);
            }
        }
        return builder.build();
    }

    /**
     * Convenience method for getting NBT compound.
     */
    @Nonnull
    private static NBTTagCompound getData(ItemStack stack) {
        return stack.getOrCreateSubCompound(NBT_ROOT);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String toolClass = getToolClass(stack);
        if (!toolClass.isEmpty()) {
            return SilentGear.i18n.subText(this, toolClass);
        }
        return super.getItemStackDisplayName(stack);
    }

    public static String getModelKey(ItemStack stack) {
        NBTTagCompound data = getData(stack);
        String cached = data.getString(NBT_MODEL_KEY);
        if (!cached.isEmpty())
            return cached;

        // Legacy tool heads created before the key was persisted: compute it on demand.
        String toolClass = data.getString(NBT_TOOL_CLASS);
        ICoreTool toolItem = ModItems.toolClasses.get(toolClass);
        ItemPartData primary = getPrimaryPart(stack);
        ItemPartData secondary = toolItem != null && toolItem.hasSwordGuard() ? getSecondaryPart(stack) : null;
        return getModelKey(toolClass, primary, secondary);
    }

    public static String getModelKey(String toolClass, @Nullable ItemPartData primary, @Nullable ItemPartData secondary) {
        return toolClass + "_head|"
                + (primary != null ? primary.getModelIndex(0) : "null")
                + (secondary != null ? "|" + secondary.getModelIndex(0) : "");
    }

    private static final int TOOLTIP_STACK_CACHE_SIZE = 128;

    /**
     * Tooltips are rebuilt every frame while an item is hovered. Building the rod-less tool and
     * recalculating its stats is expensive, and the result is fully determined by the tool head's
     * NBT (tool class + material parts + grades). Cache a bounded number of results by that NBT.
     */
    private static final Map<String, ItemStack> TOOLTIP_STACK_CACHE = new LinkedHashMap<String, ItemStack>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, ItemStack> eldest) {
            return size() > TOOLTIP_STACK_CACHE_SIZE;
        }
    };

    private static ItemStack getTooltipStack(ItemStack toolHead, ICoreItem toolItem, Collection<ItemPartData> parts) {
        NBTTagCompound tag = toolHead.getTagCompound();
        if (tag == null)
            return toolItem.construct(toolItem.getItem(), parts);

        String key = toolItem.getGearClass() + "|" + tag;
        synchronized (TOOLTIP_STACK_CACHE) {
            ItemStack cached = TOOLTIP_STACK_CACHE.get(key);
            if (cached == null) {
                cached = toolItem.construct(toolItem.getItem(), parts);
                TOOLTIP_STACK_CACHE.put(key, cached);
            }
            return cached;
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag) {
        I18nHelper i18n = SilentGear.i18n;
        String toolClass = getToolClass(stack);
        Collection<ItemPartData> parts = getAllParts(stack);

        // Ungraded parts warning
        boolean hasUngradedParts = parts.stream().anyMatch(p -> p.getGrade() == MaterialGrade.NONE);
        if (hasUngradedParts) {
            list.add(TextFormatting.ITALIC + SilentGear.i18n.miscText("ungradedParts"));
        }

        // List materials
        GearClientHelper.tooltipListParts(stack, list, parts);

        // Example output warning
        if (getData(stack).getBoolean(NBT_IS_EXAMPLE)) {
            list.add(TextFormatting.YELLOW + i18n.translate("misc", "exampleOutput1"));
            list.add(TextFormatting.YELLOW + i18n.translate("misc", "exampleOutput2"));
        }

        // Tooltip from actual tool (minus rod and anything else)
        ICoreItem toolItem = ModItems.toolClasses.get(toolClass);
        if (toolItem != null) {
            ItemStack constructed = getTooltipStack(stack, toolItem, parts);
            TooltipFlagTC tooltipFlag = TooltipFlagTC.withModifierKeys(flag.isAdvanced(), true, false);
            GearClientHelper.addInformation(constructed, world, list, tooltipFlag);
        }
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        // TODO Auto-generated method stub
        return super.getRarity(stack);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
        if (!this.isInCreativeTab(tab)) return;

        for (String toolClass : ModItems.toolClasses.keySet()) {
            for (int i = 1; i < 4; ++i) {
                GearGenerator.selectRandom(PartMain.class, i).ifPresent(part ->
                        list.add(getStack(toolClass, (PartMain) part, true)));
            }
        }
    }

    public Collection<IRecipe> getExampleRecipes() {
        Collection<IRecipe> list = new ArrayList<>();

        ModItems.toolClasses.forEach((toolClass, item) -> {
            Ingredient blueprint = Blueprint.getBlueprintIngredientForGear(item);
            if (blueprint != null) {
                for (PartMain part : PartRegistry.getVisibleMains()) {
                    ItemStack result = getStack(toolClass, part, true);
                    NonNullList<Ingredient> ingredients = NonNullList.create();
                    ingredients.add(blueprint);
                    for (int i = 0; i < item.getConfig().getHeadCount(); ++i) {
                        ingredients.add(Ingredient.fromStacks(part.getCraftingStack()));
                    }
                    list.add(new ShapelessRecipes(SilentGear.MOD_ID, result, ingredients));
                }
            } else {
                SilentGear.log.warn("Trying to add {} example recipes, but could not find blueprint item!", item.getGearClass());
            }
        });

        return list;
    }

    @Override
    public float getStat(@Nonnull ItemStack stack, @Nonnull ItemStat stat) {
        NBTTagCompound root = getData(stack);
        if (!root.hasKey(NBT_STAT_CACHE))
            return stat.getDefaultValue();

        NBTTagCompound tags = root.getCompoundTag(NBT_STAT_CACHE);
        return tags.getFloat(stat.getName().getPath());
    }

    public static String getSubtypeKey(ItemStack stack) {
        ItemPartData part = getPrimaryPart(stack);
        return getToolClass(stack) + (part != null ? "|" + part.getModelIndex(0) : "|empty");
    }
}
