package net.silentchaos512.gear.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.item.ICoreItem;
import net.silentchaos512.gear.api.item.ICoreTool;
import net.silentchaos512.gear.api.parts.ItemPart;
import net.silentchaos512.gear.api.parts.ItemPartData;
import net.silentchaos512.gear.api.parts.ModularCoatingPart;
import net.silentchaos512.gear.api.parts.ModularMaterialRegistry;
import net.silentchaos512.gear.api.parts.ModularTipPart;
import net.silentchaos512.gear.api.parts.PartDataList;
import net.silentchaos512.gear.api.parts.PartMain;
import net.silentchaos512.gear.api.parts.PartRegistry;
import net.silentchaos512.gear.api.parts.PartType;
import net.silentchaos512.gear.block.craftingstation.ContainerCraftingStation;
import net.silentchaos512.gear.block.craftingstation.TileCraftingStation;
import net.silentchaos512.gear.config.ConfigOptionEquipment;
import net.silentchaos512.gear.init.ModBlocks;
import net.silentchaos512.gear.init.ModItems;
import net.silentchaos512.gear.init.ModMaterials;
import net.silentchaos512.gear.item.MiscUpgrades;
import net.silentchaos512.gear.item.ModularCoatingItem;
import net.silentchaos512.gear.item.ModularTipItem;
import net.silentchaos512.gear.item.ToolHead;
import net.silentchaos512.gear.item.ToolRods;
import net.silentchaos512.gear.item.gear.CoreArmor;
import net.silentchaos512.gear.util.GearData;
import net.silentchaos512.gear.util.GearHelper;
import net.silentchaos512.lib.registry.RecipeMaker;
import net.silentchaos512.lib.util.StackHelper;

import java.util.*;
import java.util.stream.Collectors;

@JEIPlugin
public class JeiPlugin implements IModPlugin {
    private static boolean initFailed = false;

    public static boolean hasInitFailed() {
        return initFailed;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
        initFailed = true;
        subtypeRegistry.registerSubtypeInterpreter(ModItems.toolHead, ToolHead::getSubtypeKey);

//        ModItems.gearClasses.forEach(
//                (key, item) -> subtypeRegistry.registerSubtypeInterpreter(item.getItem(),
//                        stack -> {
//                            ItemPartData part = GearData.getPrimaryPart(stack);
//                            return part == null ? key : key + "|" + part.getPart().getRegistryName().toString();
//                        }));
        initFailed = false;
    }

    @Override
    public void register(IModRegistry registry) {
        initFailed = true;
        RecipeMaker recipeMaker = SilentGear.registry.getRecipeMaker();

        registry.addRecipeCatalyst(new ItemStack(ModBlocks.craftingStation), VanillaRecipeCategoryUid.CRAFTING);
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(ContainerCraftingStation.class, VanillaRecipeCategoryUid.CRAFTING,
                0, 9, 15, 36 + TileCraftingStation.SIDE_INVENTORY_SIZE);

        // Add "example recipes". We can't allow these to be crafted, but it's helpful to have them
        // show in JEI. Some people can't read...
        registry.addRecipes(ModItems.toolHead.getExampleRecipes(), VanillaRecipeCategoryUid.CRAFTING);
        for (ICoreItem item : ModItems.toolClasses.values())
            registry.addRecipes(GearHelper.getExampleRecipes(item), VanillaRecipeCategoryUid.CRAFTING);
        for (ICoreItem item : ModItems.armorClasses.values())
            if (item instanceof CoreArmor)
                registry.addRecipes(((CoreArmor) item).getExampleRecipes(), VanillaRecipeCategoryUid.CRAFTING);

        // Examples of applying upgrades, yay!
        List<IRecipe> builtRecipes = new ArrayList<>();
        for (ICoreItem item : ModItems.gearClasses.values()) {
            ItemStack gear = getSampleStack(item);
            // 旧固定 tip 仅作 legacy 部件保留，JEI 中不再展示其用途示例
            if (item instanceof ICoreTool) {
                // Modular tip example (iron)
                ItemPart ironTip = ModularMaterialRegistry.get("silentgear:tip_iron", PartType.TIP);
                if (ModMaterials.modularTip.isValidFor(item) && ironTip != null) {
                    ItemStack modularIronTip = ModularTipItem.createStack(ItemPartData.instance(ironTip));
                    ItemPartData ironTipData = ItemPartData.fromStack(modularIronTip);
                    if (ironTipData != null) {
                        ItemStack gearWithModularTip = gear.copy();
                        GearData.addUpgradePart(gearWithModularTip, ironTipData);
                        GearData.recalculateStats(gearWithModularTip);
                        builtRecipes.add(recipeMaker.makeShapeless(gearWithModularTip, gear, ModItems.modularTip));
                    }
                }
            }
            // Modular coating example (gold)
            if (ModMaterials.modularCoating.isValidFor(item)) {
                ItemPartData goldCoatingData = ModularCoatingPart.getBlueprintMaterial(ModMaterials.mainGold.getCraftingStack());
                ItemStack modularGoldCoating = ModularCoatingItem.createStack(goldCoatingData);
                ItemStack gearWithCoating = gear.copy();
                ItemPartData coatingData = ItemPartData.fromStack(modularGoldCoating);
                if (coatingData != null) {
                    GearData.addUpgradePart(gearWithCoating, coatingData);
                    GearData.recalculateStats(gearWithCoating);
                    builtRecipes.add(recipeMaker.makeShapeless(gearWithCoating, gear, ModItems.modularCoating));
                }
            }
            // Misc upgrades
            for (MiscUpgrades upgrade : MiscUpgrades.values()) {
                if (upgrade.getPart().isValidFor(item)) {
                    ItemStack gearWithUpgrade = gear.copy();
                    GearData.addUpgradePart(gearWithUpgrade, ItemPartData.instance(upgrade.getPart()));
                    GearData.recalculateStats(gearWithUpgrade);
                    builtRecipes.add(recipeMaker.makeShapeless(gearWithUpgrade, gear, upgrade.getItem()));
                }
            }
        }
        // Modular tip blueprint examples (main materials + independent tip-only materials)
        ItemStack blueprintTipStack = new ItemStack(ModItems.blueprintTip);
        ItemStack templateTipStack = new ItemStack(ModItems.templateTip);
        List<ItemStack> tipIngredients = new ArrayList<>();
        for (PartMain main : Arrays.asList(ModMaterials.mainIron, ModMaterials.mainDiamond, ModMaterials.mainEmerald))
            tipIngredients.add(main.getCraftingStack());
        tipIngredients.add(new ItemStack(Items.REDSTONE));
        tipIngredients.add(new ItemStack(Items.GLOWSTONE_DUST));
        tipIngredients.add(new ItemStack(Items.DYE, 1, 4));
        tipIngredients.add(new ItemStack(Items.QUARTZ));
        for (ItemStack ingredient : tipIngredients) {
            if (ingredient.isEmpty()) continue;
            ItemPartData data = ModularTipPart.getBlueprintMaterial(ingredient);
            if (data == null) continue;
            ItemStack result = ModularTipItem.createStack(data);
            if (result.isEmpty()) continue;
            builtRecipes.add(recipeMaker.makeShapeless(result, blueprintTipStack, ingredient));
            builtRecipes.add(recipeMaker.makeShapeless(result, templateTipStack, ingredient));
        }

        // Modular coating blueprint examples
        ItemStack blueprintCoatingStack = new ItemStack(ModItems.blueprintCoating);
        ItemStack templateCoatingStack = new ItemStack(ModItems.templateCoating);
        ItemStack goldIngredient = ModMaterials.mainGold.getCraftingStack();
        ItemStack coatingResult = ModularCoatingItem.createStack(ModularCoatingPart.getBlueprintMaterial(goldIngredient));
        if (!goldIngredient.isEmpty() && !coatingResult.isEmpty()) {
            builtRecipes.add(recipeMaker.makeShapeless(coatingResult, blueprintCoatingStack, goldIngredient));
            builtRecipes.add(recipeMaker.makeShapeless(coatingResult, templateCoatingStack, goldIngredient));
        }

        registry.addRecipes(builtRecipes, VanillaRecipeCategoryUid.CRAFTING);

        // Ingredient Info Pages
        addIngredientInfoPages(registry, SilentGear.registry.getBlocks());
        addIngredientInfoPages(registry, SilentGear.registry.getItems());
        // Blueprints and templates (all in one)
        registry.addIngredientInfo(ModItems.blueprints.stream().map(ItemStack::new).collect(Collectors.toList()),
                ItemStack.class, getDescKey("blueprint"));
        // Part blueprints (modular tips)
        registry.addIngredientInfo(Arrays.asList(new ItemStack(ModItems.blueprintTip), new ItemStack(ModItems.templateTip)),
                ItemStack.class, getDescKey("part_blueprint"));
        // Modular tip
        registry.addIngredientInfo(new ItemStack(ModItems.modularTip), ItemStack.class, getDescKey("modular_tip"));
        // Part blueprints (modular coatings)
        registry.addIngredientInfo(Arrays.asList(new ItemStack(ModItems.blueprintCoating), new ItemStack(ModItems.templateCoating)),
                ItemStack.class, getDescKey("part_blueprint_coating"));
        // Modular coating
        registry.addIngredientInfo(new ItemStack(ModItems.modularCoating), ItemStack.class, getDescKey("modular_coating"));
        // Tool Heads
        for (String toolClass : ModItems.toolClasses.keySet()) {
            List<ItemStack> list = new ArrayList<>();
            for (PartMain part : PartRegistry.getMains())
                list.add(ModItems.toolHead.getStack(toolClass, part, true));
            registry.addIngredientInfo(list, ItemStack.class, getDescKey("tool_head"));
        }

        initFailed = false;
    }

    private Map<ResourceLocation, ItemStack> sampleStacks = new HashMap<>();

    private ItemStack getSampleStack(ICoreItem gearItem) {
        ResourceLocation name = gearItem.getItem().getRegistryName();
        ItemStack cached = sampleStacks.get(name);
        if (cached != null)
            return cached;

        ConfigOptionEquipment config = gearItem.getConfig();
        PartDataList parts = PartDataList.of();
        for (int i = 0; i < config.getCraftingPartCount(PartType.MAIN); ++i)
            parts.addPart(ModMaterials.mainIron);
        if (config.getRodCount() > 0)
            parts.addPart(ToolRods.WOOD.getPart());
        if (config.getBowstringCount() > 0)
            parts.addPart(ModMaterials.bowstringString);

        ItemStack stack = gearItem.construct(gearItem.getItem(), parts);

        // Add text to indicate this is a sample, using vanilla lore tag
        NBTTagList tagList = new NBTTagList();
        tagList.appendTag(new NBTTagString(SilentGear.i18n.translate("jei", "tooltip.sample1")));
        tagList.appendTag(new NBTTagString(SilentGear.i18n.translate("jei", "tooltip.sample2")));
        stack.getOrCreateSubCompound("display").setTag("Lore", tagList);
        sampleStacks.put(name, stack);
        return stack;
    }

    private void addIngredientInfoPages(IModRegistry registry, Collection<? extends IForgeRegistryEntry<?>> list) {
        for (IForgeRegistryEntry<?> obj : list) {
            String key = getDescKey(Objects.requireNonNull(obj.getRegistryName()));
//            SilentGear.log.debug("JEI info page {}: {}", key, SilentGear.i18n.hasKey(key));
            if (SilentGear.i18n.hasKey(key))
                registry.addIngredientInfo(StackHelper.fromBlockOrItem(obj), ItemStack.class, key);
        }
    }

    private String getDescKey(String name) {
        return "jei." + SilentGear.MOD_ID + "." + name + ".desc";
    }

    private String getDescKey(ResourceLocation name) {
        return "jei." + name.getNamespace() + "." + name.getPath() + ".desc";
    }
}
