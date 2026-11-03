package net.silentchaos512.gear.api.parts;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.config.Config;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 模块化 TIP/COATING 的独立材料表。数据来自内置 JSON 与 config 覆盖/新增 JSON，加载期构建
 * 不可变索引；运行时只做 item+meta / oredict 查表，不写入 {@link PartRegistry} 的 stack→part
 * 单映射。
 */
public final class ModularMaterialRegistry {
    private static final String BUILTIN_DIR = "assets/silentgear/modular_materials/";
    private static final String INDEX_FILE = BUILTIN_DIR + "index.json";
    private static final String CONFIG_DIR = "modular_materials";
    private static final Gson GSON = new Gson();

    private static Map<String, Entry> byIdTip = ImmutableMap.of();
    private static Map<String, Entry> byIdCoating = ImmutableMap.of();
    private static Map<String, List<Entry>> byItem = ImmutableMap.of();
    private static Map<String, List<Entry>> byOre = ImmutableMap.of();
    private static Map<String, List<ItemPart>> byType = ImmutableMap.of();
    private static Map<String, Alias> aliases = ImmutableMap.of();
    private static boolean loaded = false;

    private ModularMaterialRegistry() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * 内置 JSON + config 覆盖/新增。1.12 无运行时数据重载，改动 JSON 需重启游戏。
     */
    public static synchronized void load() {
        if (loaded) return;

        Map<String, JsonObject> files = new LinkedHashMap<>();
        loadBuiltin(files);
        loadConfig(files);

        Map<String, Entry> idTipMap = new LinkedHashMap<>();
        Map<String, Entry> idCoatingMap = new LinkedHashMap<>();
        Map<String, List<Entry>> itemMap = new LinkedHashMap<>();
        Map<String, List<Entry>> oreMap = new LinkedHashMap<>();
        Map<String, List<ItemPart>> typeMap = new LinkedHashMap<>();
        Map<String, Alias> aliasMap = new LinkedHashMap<>();

        for (Map.Entry<String, JsonObject> file : files.entrySet()) {
            buildEntry(file.getKey(), file.getValue(), idTipMap, idCoatingMap, itemMap, oreMap, typeMap, aliasMap);
        }

        Comparator<Entry> order = Comparator
                .comparingInt((Entry e) -> e.priority).reversed()
                .thenComparing(e -> e.id.toString());
        itemMap.values().forEach(list -> list.sort(order));
        oreMap.values().forEach(list -> list.sort(order));

        ImmutableMap.Builder<String, Entry> tipBuilder = ImmutableMap.builder();
        idTipMap.forEach(tipBuilder::put);
        byIdTip = tipBuilder.build();

        ImmutableMap.Builder<String, Entry> coatingBuilder = ImmutableMap.builder();
        idCoatingMap.forEach(coatingBuilder::put);
        byIdCoating = coatingBuilder.build();

        ImmutableMap.Builder<String, List<Entry>> itemBuilder = ImmutableMap.builder();
        itemMap.forEach((key, list) -> itemBuilder.put(key, ImmutableList.copyOf(list)));
        byItem = itemBuilder.build();

        ImmutableMap.Builder<String, List<Entry>> oreBuilder = ImmutableMap.builder();
        oreMap.forEach((key, list) -> oreBuilder.put(key, ImmutableList.copyOf(list)));
        byOre = oreBuilder.build();

        ImmutableMap.Builder<String, List<ItemPart>> typeBuilder = ImmutableMap.builder();
        typeMap.forEach((key, list) -> typeBuilder.put(key, ImmutableList.copyOf(list)));
        byType = typeBuilder.build();

        ImmutableMap.Builder<String, Alias> aliasBuilder = ImmutableMap.builder();
        aliasMap.forEach(aliasBuilder::put);
        aliases = aliasBuilder.build();

        loaded = true;
        SilentGear.LOGGER.info("Loaded {} modular material definitions ({} id/type entries, {} legacy aliases)",
                files.size(), byIdTip.size() + byIdCoating.size(), aliases.size());
    }

    @Nullable
    public static ItemPart get(@Nullable String key, PartType type) {
        if (key == null || key.isEmpty()) return null;
        Map<String, Entry> map = type == PartType.TIP ? byIdTip
                : type == PartType.COATING ? byIdCoating : null;
        Entry entry = map == null ? null : map.get(key);
        return entry == null ? null : entry.part;
    }

    /**
     * 按类型返回全部材料（加载顺序）。供创造页/示例装备挑选模块化 tip/coating 材料。
     */
    public static List<ItemPart> getMaterials(PartType type) {
        return byType.getOrDefault(type.getName(), ImmutableList.of());
    }

    /**
     * 旧固定尖端/覆层部件的 part key（如 {@code silentgear:tip_iron}）映射到对应的模块化
     * 部件实例。别名表在加载期构建，运行期 O(1)；未知 key 返回 null。
     */
    @Nullable
    public static ItemPart getAliasedPart(@Nullable String legacyKey) {
        if (legacyKey == null || legacyKey.isEmpty()) return null;
        Alias alias = aliases.get(legacyKey);
        return alias == null ? null : PartRegistry.get(alias.modularPartKey);
    }

    /**
     * 重建旧工具 NBT 中的尖端/覆层数据。旧独立物品已被删除，{@code Item} 子标签可能指向
     * 不存在的物品；这里改用模块化部件的合成物品，并写入对应的材料 id，使读取、属性重算、
     * tooltip、拆解、修理与模型渲染都能继续解析到同一材料。
     */
    @Nullable
    public static ItemPartData getAliasedData(@Nullable String legacyKey, MaterialGrade grade) {
        if (legacyKey == null || legacyKey.isEmpty()) return null;
        Alias alias = aliases.get(legacyKey);
        if (alias == null) return null;

        ItemPart modularPart = PartRegistry.get(alias.modularPartKey);
        if (modularPart == null) return null;

        ItemStack craftingItem = modularPart.getCraftingStack().copy();
        if (craftingItem.isEmpty()) return null;
        craftingItem.setCount(1);

        NBTTagCompound tags = craftingItem.getTagCompound();
        tags = tags == null ? new NBTTagCompound() : tags.copy();
        tags.setString("Material", alias.materialId.toString());
        craftingItem.setTagCompound(tags);
        return ItemPartData.instance(modularPart, grade, craftingItem);
    }

    /**
     * 蓝图匹配/组装/升级时的材料查找。只查预构建索引，不触发资源或 NBT 解析。
     */
    @Nullable
    public static ItemPart find(ItemStack stack, PartType type) {
        if (stack.isEmpty() || stack.getItem().getRegistryName() == null) return null;

        String itemId = stack.getItem().getRegistryName().toString();
        Entry entry = firstMatching(byItem.get(itemId + "@" + stack.getMetadata()), type);
        if (entry == null) entry = firstMatching(byItem.get(itemId + "@*"), type);
        if (entry != null) return entry.part;

        for (int oreId : OreDictionary.getOreIDs(stack)) {
            entry = firstMatching(byOre.get(OreDictionary.getOreName(oreId)), type);
            if (entry != null) return entry.part;
        }
        return null;
    }

    @Nullable
    private static Entry firstMatching(@Nullable List<Entry> list, PartType type) {
        if (list == null) return null;
        for (Entry entry : list) {
            if (entry.type == type)
                return entry;
        }
        return null;
    }

    private static void loadBuiltin(Map<String, JsonObject> files) {
        JsonObject index = readResource(INDEX_FILE);
        if (index == null || !index.has("materials")) {
            SilentGear.LOGGER.warn("Missing or invalid modular material index '{}'", INDEX_FILE);
            return;
        }
        for (JsonElement element : index.getAsJsonArray("materials")) {
            if (!element.isJsonPrimitive()) continue;
            String name = element.getAsString();
            JsonObject json = readResource(BUILTIN_DIR + name + ".json");
            if (json == null) {
                SilentGear.LOGGER.warn("Missing modular material file '{}'", BUILTIN_DIR + name + ".json");
                continue;
            }
            files.put(name, json);
        }
    }

    private static void loadConfig(Map<String, JsonObject> files) {
        File directory = new File(Config.INSTANCE.getDirectory(), CONFIG_DIR);
        if (!directory.isDirectory() && !directory.mkdirs()) {
            SilentGear.LOGGER.warn("Could not create modular material directory '{}'", directory);
            return;
        }

        File[] list = directory.listFiles((dir, name) -> name.endsWith(".json"));
        if (list == null) return;

        for (File file : list) {
            JsonObject json = readFile(file);
            if (json == null) continue;
            String name = file.getName().substring(0, file.getName().length() - ".json".length());

            if (files.containsKey(name)) {
                // 覆盖：只替换显式提供的顶层字段，未提供的保留内置值
                files.put(name, merge(files.get(name), json));
                SilentGear.LOGGER.info("Modular material override: {}", file.getName());
            } else if (json.has("types")) {
                files.put(name, json);
                SilentGear.LOGGER.info("Modular material added from config: {}", file.getName());
            } else {
                SilentGear.LOGGER.warn("Ignoring config JSON without \"types\": {}", file.getName());
            }
        }
    }

    private static void buildEntry(String fileName, JsonObject json, Map<String, Entry> idTipMap,
                                   Map<String, Entry> idCoatingMap, Map<String, List<Entry>> itemMap,
                                   Map<String, List<Entry>> oreMap, Map<String, List<ItemPart>> typeMap,
                                   Map<String, Alias> aliasMap) {
        ResourceLocation id;
        try {
            id = new ResourceLocation(json.has("id")
                    ? json.get("id").getAsString()
                    : SilentGear.MOD_ID + ":" + fileName);
        } catch (Exception e) {
            SilentGear.LOGGER.warn("Invalid modular material id in '{}'", fileName);
            return;
        }

        if (!json.has("types") || !json.get("types").isJsonArray()) {
            SilentGear.LOGGER.warn("Modular material '{}' has no types array", id);
            return;
        }

        int priority = json.has("priority") ? json.get("priority").getAsInt() : 100;
        boolean enabled = true;
        if (json.has("availability") && json.get("availability").isJsonObject()) {
            JsonObject availability = json.getAsJsonObject("availability");
            if (availability.has("enabled"))
                enabled = availability.get("enabled").getAsBoolean();
            if (availability.has("priority"))
                priority = availability.get("priority").getAsInt();
        }
        if (!enabled) {
            SilentGear.LOGGER.info("Modular material '{}' is disabled", id);
            return;
        }

        String itemId = null;
        int itemMeta = 0;
        String oreDict = "";
        if (json.has("crafting_items") && json.get("crafting_items").isJsonObject()) {
            JsonObject crafting = json.getAsJsonObject("crafting_items");
            if (crafting.has("normal") && crafting.get("normal").isJsonObject()) {
                JsonObject normal = crafting.getAsJsonObject("normal");
                if (normal.has("item"))
                    itemId = normal.get("item").getAsString();
                if (normal.has("data"))
                    itemMeta = normal.get("data").getAsInt();
                if (normal.has("oredict"))
                    oreDict = normal.get("oredict").getAsString();
            }
        }

        for (JsonElement element : json.getAsJsonArray("types")) {
            if (!element.isJsonPrimitive()) continue;
            PartType type = PartType.get(element.getAsString().replaceAll("^.*:", ""));
            if (type != PartType.TIP && type != PartType.COATING) {
                SilentGear.LOGGER.warn("Modular material '{}' has unsupported type '{}'", id, element.getAsString());
                continue;
            }

            ItemPart part = type == PartType.TIP
                    ? new ModularTipMaterial(id)
                    : new ModularCoatingMaterial(id);
            part.loadFromJson(json);

            Entry entry = new Entry(id, type, part, priority, itemId, itemMeta, oreDict);
            if (type == PartType.TIP)
                idTipMap.put(id.toString(), entry);
            else
                idCoatingMap.put(id.toString(), entry);
            typeMap.computeIfAbsent(type.getName(), k -> new ArrayList<>()).add(part);

            // 旧独立部件 key 与内置材料文件名一致（tip_iron、coating_gold 等）。加载期一次性
            // 建表，运行期只做 O(1) 查找；不扫描登录背包，也不做 tick 检测。
            if (type == PartType.TIP && fileName.startsWith("tip_")
                    || type == PartType.COATING && fileName.startsWith("coating_")) {
                String legacyKey = SilentGear.MOD_ID + ":" + fileName;
                ResourceLocation modularPartKey = new ResourceLocation(SilentGear.MOD_ID,
                        type == PartType.TIP ? "modular_tip" : "modular_coating");
                aliasMap.put(legacyKey, new Alias(id, modularPartKey));
            }

            if (itemId != null && !itemId.isEmpty()) {
                String key = itemMeta < 0 || itemMeta == OreDictionary.WILDCARD_VALUE
                        ? itemId + "@*"
                        : itemId + "@" + itemMeta;
                itemMap.computeIfAbsent(key, k -> new ArrayList<>()).add(entry);
            }
            if (!oreDict.isEmpty()) {
                oreMap.computeIfAbsent(oreDict, k -> new ArrayList<>()).add(entry);
            }
        }
    }

    private static JsonObject merge(JsonObject base, JsonObject override) {
        JsonObject result = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : base.entrySet())
            result.add(entry.getKey(), entry.getValue());
        for (Map.Entry<String, JsonElement> entry : override.entrySet())
            result.add(entry.getKey(), entry.getValue());
        return result;
    }

    @Nullable
    private static JsonObject readResource(String path) {
        InputStream stream = ModularMaterialRegistry.class.getClassLoader().getResourceAsStream(path);
        if (stream == null) return null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return GSON.fromJson(reader, JsonObject.class);
        } catch (Exception e) {
            SilentGear.LOGGER.warn("Error reading modular material resource '{}'", path);
            SilentGear.LOGGER.catching(e);
            return null;
        }
    }

    @Nullable
    private static JsonObject readFile(File file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            return GSON.fromJson(reader, JsonObject.class);
        } catch (Exception e) {
            SilentGear.LOGGER.warn("Error reading modular material file '{}'", file.getAbsolutePath());
            SilentGear.LOGGER.catching(e);
            return null;
        }
    }

    private static final class Entry {
        final ResourceLocation id;
        final PartType type;
        final ItemPart part;
        final int priority;
        final String itemId;
        final int itemMeta;
        final String oreDict;

        Entry(ResourceLocation id, PartType type, ItemPart part, int priority,
              @Nullable String itemId, int itemMeta, String oreDict) {
            this.id = id;
            this.type = type;
            this.part = part;
            this.priority = priority;
            this.itemId = itemId;
            this.itemMeta = itemMeta;
            this.oreDict = oreDict;
        }
    }

    /**
     * 旧部件 key → 模块化部件 + 材料 id 的只读映射。加载期构建，运行期只读。
     */
    private static final class Alias {
        final ResourceLocation materialId;
        final ResourceLocation modularPartKey;

        Alias(ResourceLocation materialId, ResourceLocation modularPartKey) {
            this.materialId = materialId;
            this.modularPartKey = modularPartKey;
        }
    }
}
