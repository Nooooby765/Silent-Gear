package net.silentchaos512.gear.init;

import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.item.ICoreArmor;
import net.silentchaos512.gear.api.item.ICoreItem;
import net.silentchaos512.gear.api.item.ICoreTool;
import net.silentchaos512.gear.api.parts.ItemPartData;
import net.silentchaos512.gear.api.parts.ModularCoatingPart;
import net.silentchaos512.gear.api.parts.ModularFletchingPart;
import net.silentchaos512.gear.api.parts.ModularLiningPart;
import net.silentchaos512.gear.api.parts.ModularRodPart;
import net.silentchaos512.gear.api.parts.ModularTipPart;
import net.silentchaos512.gear.api.parts.PartDataList;
import net.silentchaos512.gear.api.parts.PartType;
import net.silentchaos512.gear.item.*;
import net.silentchaos512.gear.item.blueprint.Blueprint;
import net.silentchaos512.gear.item.blueprint.PartBlueprint;
import net.silentchaos512.gear.item.blueprint.book.BlueprintBook;
import net.silentchaos512.gear.item.gear.*;
import net.silentchaos512.lib.item.IEnumItems;
import net.silentchaos512.lib.registry.SRegistry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModItems {
    public static final Map<String, ICoreTool> toolClasses = new LinkedHashMap<>();
    public static final Map<String, ICoreArmor> armorClasses = new LinkedHashMap<>();
    public static final Map<String, ICoreItem> gearClasses = new LinkedHashMap<>();
    public static final List<Blueprint> blueprints = new ArrayList<>();

    public static BlueprintPackage blueprintPackage = new BlueprintPackage(new ResourceLocation(SilentGear.MOD_ID, "starter_blueprints"));
    public static BlueprintBook blueprintBook = new BlueprintBook();

    public static Flaxseeds flaxseeds = new Flaxseeds();
    public static NetherBanana netherBanana = new NetherBanana();

    public static ToolHead toolHead = new ToolHead();

    public static ModularTipItem modularTip = new ModularTipItem();
    public static PartBlueprint blueprintTip = new PartBlueprint(false, PartType.TIP, 1,
            ModularTipPart::isValidBlueprintMaterial, ModItems::createModularTip, ModularTipPart::getBlueprintMaterial);
    public static PartBlueprint templateTip = new PartBlueprint(true, PartType.TIP, 1,
            ModularTipPart::isValidBlueprintMaterial, ModItems::createModularTip, ModularTipPart::getBlueprintMaterial);

    public static ModularCoatingItem modularCoating = new ModularCoatingItem();
    public static PartBlueprint blueprintCoating = new PartBlueprint(false, PartType.COATING, 1,
            ModularCoatingPart::hasCoating, ModItems::createModularCoating, ModularCoatingPart::getBlueprintMaterial);
    public static PartBlueprint templateCoating = new PartBlueprint(true, PartType.COATING, 1,
            ModularCoatingPart::hasCoating, ModItems::createModularCoating, ModularCoatingPart::getBlueprintMaterial);

    public static ModularRodItem modularRod = new ModularRodItem();
    public static PartBlueprint blueprintModularRod = new PartBlueprint(false, PartType.ROD, 1,
            ModularRodPart::isValidBlueprintMaterial, ModItems::createModularRod, ModularRodPart::getBlueprintMaterial);
    public static PartBlueprint templateModularRod = new PartBlueprint(true, PartType.ROD, 1,
            ModularRodPart::isValidBlueprintMaterial, ModItems::createModularRod, ModularRodPart::getBlueprintMaterial);

    public static ModularFletchingItem modularFletching = new ModularFletchingItem();
    public static PartBlueprint blueprintModularFletching = new PartBlueprint(false, PartType.FLETCHING, 1,
            ModularFletchingPart::isValidBlueprintMaterial, ModItems::createModularFletching,
            ModularFletchingPart::getBlueprintMaterial);
    public static PartBlueprint templateModularFletching = new PartBlueprint(true, PartType.FLETCHING, 1,
            ModularFletchingPart::isValidBlueprintMaterial, ModItems::createModularFletching,
            ModularFletchingPart::getBlueprintMaterial);

    public static ModularLiningItem modularLining = new ModularLiningItem();
    public static PartBlueprint blueprintLining = new PartBlueprint(false, PartType.LINING, 1,
            ModularLiningPart::isValidBlueprintMaterial, ModItems::createModularLining,
            ModularLiningPart::getBlueprintMaterial);
    public static PartBlueprint templateLining = new PartBlueprint(true, PartType.LINING, 1,
            ModularLiningPart::isValidBlueprintMaterial, ModItems::createModularLining,
            ModularLiningPart::getBlueprintMaterial);

    public static CoreSword sword = new CoreSword();
    public static CoreDagger dagger = new CoreDagger();
    public static CoreKnife knife = new CoreKnife();
    public static CoreKatana katana = new CoreKatana();
    public static CoreMachete machete = new CoreMachete();
    public static CoreMace mace = new CoreMace();
    public static CoreSpear spear = new CoreSpear();
    public static CorePickaxe pickaxe = new CorePickaxe();
    public static CoreShovel shovel = new CoreShovel();
    public static CoreHoe hoe = new CoreHoe();
    public static CorePaxel paxel = new CorePaxel();
    public static CoreSaw saw = new CoreSaw();
    public static CoreShears shears = new CoreShears();
    public static CoreAxe axe = new CoreAxe();
    public static CoreHammer hammer = new CoreHammer();
    public static CoreExcavator excavator = new CoreExcavator();
    public static CoreMattock mattock = new CoreMattock();
    public static CoreSickle sickle = new CoreSickle();
    public static CoreBow bow = new CoreBow();
    public static CoreShield shield = new CoreShield();
    public static CoreArrow arrow = new CoreArrow();
    public static CoreFishingRod fishingRod = new CoreFishingRod();

    // Declared after shield: static initializers run in declaration order and Blueprint reads
    // the gear item's class in its constructor.
    public static Blueprint blueprintShield = new Blueprint(false, shield,
            partList -> toolHead.getStack("shield", partList));
    public static Blueprint templateShield = new Blueprint(true, shield,
            partList -> toolHead.getStack("shield", partList));

    public static Blueprint blueprintFishingRod = new Blueprint(false, fishingRod,
            partList -> fishingRod.construct(fishingRod.getItem(), partList));
    public static Blueprint templateFishingRod = new Blueprint(true, fishingRod,
            partList -> fishingRod.construct(fishingRod.getItem(), partList));

    public static Blueprint blueprintArrow = new Blueprint(false, arrow,
            partList -> toolHead.getStack("arrow", partList));
    public static Blueprint templateArrow = new Blueprint(true, arrow,
            partList -> toolHead.getStack("arrow", partList));

    public static CoreArmor helmet = new CoreArmor(EntityEquipmentSlot.HEAD, "helmet");
    public static CoreArmor chestplate = new CoreArmor(EntityEquipmentSlot.CHEST, "chestplate");
    public static CoreArmor leggings = new CoreArmor(EntityEquipmentSlot.LEGS, "leggings");
    public static CoreArmor boots = new CoreArmor(EntityEquipmentSlot.FEET, "boots");

    private ModItems() {}

    public static void registerAll(SRegistry reg) {
        // Build gear maps now because blueprints need them
        toolClasses.put("sword", sword);
        toolClasses.put("dagger", dagger);
        toolClasses.put("knife", knife);
        toolClasses.put("katana", katana);
        toolClasses.put("machete", machete);
        toolClasses.put("mace", mace);
        toolClasses.put("spear", spear);
        toolClasses.put("pickaxe", pickaxe);
        toolClasses.put("shovel", shovel);
        toolClasses.put("hoe", hoe);
        toolClasses.put("paxel", paxel);
        toolClasses.put("saw", saw);
        toolClasses.put("shears", shears);
        toolClasses.put("axe", axe);
        toolClasses.put("hammer", hammer);
        toolClasses.put("excavator", excavator);
        toolClasses.put("mattock", mattock);
        toolClasses.put("sickle", sickle);
        toolClasses.put("bow", bow);

        armorClasses.put("helmet", helmet);
        armorClasses.put("chestplate", chestplate);
        armorClasses.put("leggings", leggings);
        armorClasses.put("boots", boots);

        gearClasses.putAll(toolClasses);
        gearClasses.putAll(armorClasses);
        gearClasses.put("shield", shield);
        gearClasses.put("arrow", arrow);
        gearClasses.put("fishing_rod", fishingRod);

        IEnumItems.RegistrationHelper enumItems = new IEnumItems.RegistrationHelper(reg);

        reg.registerItem(blueprintPackage, "blueprint_package");

        // Blueprints/templates
        registerBlueprints(reg, "blueprint", false);
        registerBlueprints(reg, "template", true);
        reg.registerItem(blueprintBook, "blueprint_book");

        reg.registerItem(modularTip, "modular_tip");
        reg.registerItem(blueprintTip, "blueprint_tip");
        reg.registerItem(templateTip, "template_tip");

        reg.registerItem(modularCoating, "modular_coating");
        reg.registerItem(blueprintCoating, "blueprint_coating");
        reg.registerItem(templateCoating, "template_coating");

        reg.registerItem(modularRod, "modular_rod");
        reg.registerItem(blueprintModularRod, "blueprint_modular_rod");
        reg.registerItem(templateModularRod, "template_modular_rod");

        reg.registerItem(modularFletching, "modular_fletching");
        reg.registerItem(blueprintModularFletching, "blueprint_modular_fletching");
        reg.registerItem(templateModularFletching, "template_modular_fletching");

        reg.registerItem(modularLining, "modular_lining");
        reg.registerItem(blueprintLining, "blueprint_lining");
        reg.registerItem(templateLining, "template_lining");

        reg.registerItem(fishingRod, "fishing_rod");
        reg.registerItem(blueprintFishingRod, "blueprint_fishing_rod");
        reg.registerItem(templateFishingRod, "template_fishing_rod");
        blueprints.add(blueprintFishingRod);
        blueprints.add(templateFishingRod);

        reg.registerItem(shield, "shield");
        reg.registerItem(blueprintShield, "blueprint_shield");
        reg.registerItem(templateShield, "template_shield");
        blueprints.add(blueprintShield);
        blueprints.add(templateShield);

        reg.registerItem(arrow, "arrow");
        reg.registerItem(blueprintArrow, "blueprint_arrow");
        reg.registerItem(templateArrow, "template_arrow");
        blueprints.add(blueprintArrow);
        blueprints.add(templateArrow);

        enumItems.registerItems(MiscUpgrades.values());
        enumItems.registerItems(CraftingItems.values());
        reg.registerItem(flaxseeds, "flaxseeds");
        reg.registerItem(netherBanana, "nether_banana");

        reg.registerItem(toolHead, "tool_head");

        // Tools/armor
        toolClasses.forEach((key, item) -> reg.registerItem(item.getItem(), key));
        armorClasses.forEach((key, item) -> reg.registerItem(item.getItem(), key));

        if (SilentGear.isDevBuild()) {
            reg.registerItem(new TestItem(), "test_item");
        }

        registerOreDictEntries();
        addSmeltingRecipes();
    }

    private static ItemStack createModularTip(PartDataList parts) {
        for (ItemPartData part : parts) {
            ItemStack stack = ModularTipItem.createStack(part);
            if (!stack.isEmpty())
                return stack;
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack createModularCoating(PartDataList parts) {
        for (ItemPartData part : parts) {
            ItemStack stack = ModularCoatingItem.createStack(part);
            if (!stack.isEmpty())
                return stack;
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack createModularRod(PartDataList parts) {
        for (ItemPartData part : parts) {
            ItemStack stack = ModularRodItem.createStack(part);
            if (!stack.isEmpty())
                return stack;
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack createModularFletching(PartDataList parts) {
        for (ItemPartData part : parts) {
            ItemStack stack = ModularFletchingItem.createStack(part);
            if (!stack.isEmpty())
                return stack;
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack createModularLining(PartDataList parts) {
        for (ItemPartData part : parts) {
            ItemStack stack = ModularLiningItem.createStack(part);
            if (!stack.isEmpty())
                return stack;
        }
        return ItemStack.EMPTY;
    }

    private static void registerOreDictEntries() {
        OreDictionary.registerOre("flint", Items.FLINT);
        CraftingItems.registerOreDict();
    }

    private static void addSmeltingRecipes() {
        GameRegistry.addSmelting(CraftingItems.SINEW.getStack(), CraftingItems.DRIED_SINEW.getStack(), 0.1f);
    }

    private static void registerBlueprints(SRegistry reg, String name, boolean singleUse) {
        toolClasses.forEach((key, item) -> {
            Blueprint blueprint = new Blueprint(singleUse, item,
                    partList -> toolHead.getStack(key, partList));
            blueprints.add(blueprint);
            reg.registerItem(blueprint, name + "_" + key);
        });
        armorClasses.forEach((key, item) -> {
            Blueprint blueprint = new Blueprint(singleUse, item,
                    partList -> item.construct(item.getItem(), partList));
            blueprints.add(blueprint);
            reg.registerItem(blueprint, name + "_" + key);
        });
    }
}
