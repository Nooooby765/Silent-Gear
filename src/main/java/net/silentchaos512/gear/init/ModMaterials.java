package net.silentchaos512.gear.init;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.parts.*;
import net.silentchaos512.gear.config.Config;
import net.silentchaos512.gear.item.MiscUpgrades;
import net.silentchaos512.gear.item.TipUpgrades;
import net.silentchaos512.gear.item.ToolRods;
import net.silentchaos512.gear.util.GearHelper;
import net.silentchaos512.lib.registry.IPhasedInitializer;
import net.silentchaos512.lib.registry.SRegistry;

import java.io.File;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModMaterials implements IPhasedInitializer {
    public static final ModMaterials INSTANCE = new ModMaterials();

    public static PartMain mainWood, mainStone, mainFlint, mainIron, mainGold, mainEmerald, mainDiamond, mainObsidian, mainNetherrack, mainTerracotta;
    public static PartBowstring bowstringString, bowstringSinew;
    public static PartHighlight highlight;

    @Override
    public void preInit(SRegistry registry, FMLPreInitializationEvent event) {
        mainWood = PartRegistry.putPart(new PartMain(getPath("main_wood"), PartOrigins.BUILTIN_CORE));
        mainStone = PartRegistry.putPart(new PartMain(getPath("main_stone"), PartOrigins.BUILTIN_CORE));
        mainFlint = PartRegistry.putPart(new PartMain(getPath("main_flint"), PartOrigins.BUILTIN_CORE));
        mainTerracotta = PartRegistry.putPart(new PartMain(getPath("main_terracotta"), PartOrigins.BUILTIN_CORE));
        mainNetherrack = PartRegistry.putPart(new PartMain(getPath("main_netherrack"), PartOrigins.BUILTIN_CORE));
        mainIron = PartRegistry.putPart(new PartMain(getPath("main_iron"), PartOrigins.BUILTIN_CORE));
        mainGold = PartRegistry.putPart(new PartMain(getPath("main_gold"), PartOrigins.BUILTIN_CORE));
        mainEmerald = PartRegistry.putPart(new PartMain(getPath("main_emerald"), PartOrigins.BUILTIN_CORE));
        mainDiamond = PartRegistry.putPart(new PartMain(getPath("main_diamond"), PartOrigins.BUILTIN_CORE));
        mainObsidian = PartRegistry.putPart(new PartMain(getPath("main_obsidian"), PartOrigins.BUILTIN_CORE));
//        PartRegistry.putPart(new PartMain(getPath("main_test")));
//        PartRegistry.putPart(new PartMain(getPath("main_test2"), PartOrigins.BUILTIN_CORE));
        PartRegistry.putPart(new PartMain(getPath("main_netherwood"), PartOrigins.BUILTIN_CORE));
        PartRegistry.putPart(new PartMain(getPath("main_crimson_iron"), PartOrigins.BUILTIN_CORE));
        PartRegistry.putPart(new PartMain(getPath("main_crimson_steel"), PartOrigins.BUILTIN_CORE));
        PartRegistry.putPart(new PartMain(getPath("main_end_stone"), PartOrigins.BUILTIN_CORE));
        PartRegistry.putPart(new PartMain(getPath("main_sandstone"), PartOrigins.BUILTIN_CORE));

        // Thermal Foundation add-on: register these metal materials only when thermalfoundation is present.
        // Material data lives in assets/silentgear/materials/main_*.json; crafting uses ore dictionary
        // entries (ingotCopper, ingotTin, etc.), so they also work with any mod registering those keys.
        if (Loader.isModLoaded("thermalfoundation")) {
            PartRegistry.putPart(new PartMain(getPath("main_copper"), PartOrigins.BUILTIN_CORE));
            PartRegistry.putPart(new PartMain(getPath("main_tin"), PartOrigins.BUILTIN_CORE));
            PartRegistry.putPart(new PartMain(getPath("main_lead"), PartOrigins.BUILTIN_CORE));
            PartRegistry.putPart(new PartMain(getPath("main_nickel"), PartOrigins.BUILTIN_CORE));
            PartRegistry.putPart(new PartMain(getPath("main_silver"), PartOrigins.BUILTIN_CORE));
            PartRegistry.putPart(new PartMain(getPath("main_aluminum"), PartOrigins.BUILTIN_CORE));
            PartRegistry.putPart(new PartMain(getPath("main_platinum"), PartOrigins.BUILTIN_CORE));
            PartRegistry.putPart(new PartMain(getPath("main_steel"), PartOrigins.BUILTIN_CORE));
            PartRegistry.putPart(new PartMain(getPath("main_bronze"), PartOrigins.BUILTIN_CORE));
            PartRegistry.putPart(new PartMain(getPath("main_invar"), PartOrigins.BUILTIN_CORE));
            PartRegistry.putPart(new PartMain(getPath("main_electrum"), PartOrigins.BUILTIN_CORE));
            PartRegistry.putPart(new PartMain(getPath("main_signalum"), PartOrigins.BUILTIN_CORE));
            PartRegistry.putPart(new PartMain(getPath("main_lumium"), PartOrigins.BUILTIN_CORE));
            PartRegistry.putPart(new PartMain(getPath("main_enderium"), PartOrigins.BUILTIN_CORE));
        }

        // Mekanism add-on: osmium, refined glowstone and refined obsidian, only when mekanism is present.
        // Crafting uses ore dictionary entries provided by Mekanism (ingotOsmium, ingotRefinedGlowstone,
        // ingotRefinedObsidian).
        if (Loader.isModLoaded("mekanism")) {
            PartRegistry.putPart(new PartMain(getPath("main_osmium"), PartOrigins.BUILTIN_CORE));
            PartRegistry.putPart(new PartMain(getPath("main_refined_glowstone"), PartOrigins.BUILTIN_CORE));
            PartRegistry.putPart(new PartMain(getPath("main_refined_obsidian"), PartOrigins.BUILTIN_CORE));
        }

        // Twilight Forest add-on: ironwood, knightmetal and fiery, only when twilightforest is present.
        // Crafting uses ore dictionary entries provided by Twilight Forest (ingotIronwood,
        // ingotKnightmetal, ingotFiery).
        if (Loader.isModLoaded("twilightforest")) {
            PartRegistry.putPart(new PartMain(getPath("main_ironwood"), PartOrigins.BUILTIN_CORE));
            PartRegistry.putPart(new PartMain(getPath("main_knightmetal"), PartOrigins.BUILTIN_CORE));
            PartRegistry.putPart(new PartMain(getPath("main_fiery"), PartOrigins.BUILTIN_CORE));
        }

        // Aether Legacy add-on: skyroot, holystone, zanite and enchanted gravitite, only when
        // aether_legacy is present. Crafting uses items/ore dictionary entries from Aether Legacy.
        if (Loader.isModLoaded("aether_legacy")) {
            PartRegistry.putPart(new PartMain(getPath("main_skyroot"), PartOrigins.BUILTIN_CORE));
            PartRegistry.putPart(new PartMain(getPath("main_holystone"), PartOrigins.BUILTIN_CORE));
            PartRegistry.putPart(new PartMain(getPath("main_zanite"), PartOrigins.BUILTIN_CORE));
            PartRegistry.putPart(new PartMain(getPath("main_enchanted_gravitite"), PartOrigins.BUILTIN_CORE));
        }

        for (ToolRods rod : ToolRods.values())
            PartRegistry.putPart(rod.getPart());
        PartRegistry.putPart(new PartRod(getPath("rod_end_stone"), PartOrigins.BUILTIN_CORE));
        PartRegistry.putPart(new PartRod(getPath("rod_sandstone"), PartOrigins.BUILTIN_CORE));

        for (TipUpgrades tip : TipUpgrades.values())
            PartRegistry.putPart(tip.getPart());

        for (EnumDyeColor color : EnumDyeColor.values())
            PartRegistry.putPart(new PartGrip(getPath("grip_wool_" + color.name().toLowerCase(Locale.ROOT)), PartOrigins.BUILTIN_CORE));
        PartRegistry.putPart(new PartGrip(getPath("grip_leather"), PartOrigins.BUILTIN_CORE));

        bowstringString = PartRegistry.putPart(new PartBowstring(getPath("bowstring_string"), PartOrigins.BUILTIN_CORE));
        bowstringSinew = PartRegistry.putPart(new PartBowstring(getPath("bowstring_sinew"), PartOrigins.BUILTIN_CORE));
        PartRegistry.putPart(new PartBowstring(getPath("bowstring_flax"), PartOrigins.BUILTIN_CORE));
        PartRegistry.putPart(new PartBowstring(getPath("bowstring_vine"), PartOrigins.BUILTIN_CORE));

        for (MiscUpgrades upgrade : MiscUpgrades.values())
            PartRegistry.putPart(upgrade.getPart());

        highlight = PartRegistry.putPart(new PartHighlight(getPath("highlight"), PartOrigins.BUILTIN_CORE));

        UserDefined.loadUserParts();
    }

    @Override
    public void init(SRegistry registry, FMLInitializationEvent event) {
        // Update part caches
        // All mods should have added their parts during pre-init
        PartRegistry.resetVisiblePartCaches();
        GearHelper.resetSubItemsCache();
    }

    private static ResourceLocation getPath(String key) {
        return new ResourceLocation(SilentGear.MOD_ID, key);
    }

    private static final class UserDefined {
        static void loadUserParts() {
            final File directory = new File(Config.INSTANCE.getDirectory(), "materials");
            final File[] files = directory.listFiles();
            if (!directory.isDirectory() || files == null) {
                SilentGear.log.warn("File \"{}\" is not a directory?", directory);
                return;
            }

            final Pattern typeRegex = Pattern.compile("^[a-z]+");
            for (File file : files) {
                if (!file.isFile() || !file.getName().endsWith(".json")) continue;
                SilentGear.log.info("Material file found: {}", file);
                String filename = file.getName().replace(".json", "");
                ResourceLocation name = getPath(filename);

                // Add to registered parts if it doesn't exist
                if (!PartRegistry.getKeySet().contains(name.toString())) {
                    Matcher match = typeRegex.matcher(filename);
                    if (match.find()) {
                        String typeName = match.group();
                        PartType type = PartType.get(typeName);
                        if (type != null) {
                            SilentGear.log.info("Trying to add part {}, typeName {}", name, typeName);
                            PartRegistry.putPart(type.construct(name, PartOrigins.USER_DEFINED));
                        } else {
                            SilentGear.log.warn("Unknown part typeName \"{}\" for {}", typeName, filename);
                        }
                    }
                } else {
                    SilentGear.log.info("Part already registered. Must be an override.");
                }
            }
        }
    }
}
