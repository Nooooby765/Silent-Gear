# 最新版 Silent Gear 材料与语言移植方案（1.12.2）

> 上游：`_src\extmaster\Silent-Gear-master`（Silent Gear 4.3.0，MC 26.2 / NeoForge，MIT）
> 本地：`F:\SilentGear`（1.12.2 Forge 移植，0.8.0）
> 本文只描述方案，不代表已移植。材料/语言移植必须在架构优化验收之后进行。

## 1. 上游数据位置与格式

| 内容 | 路径 | 数量 |
|---|---|---|
| 材料 | `src/generated/resources/data/silentgear/silentgear_materials/` | 131 个 JSON（72 个可作主材料） |
| 部件 | `src/generated/resources/data/silentgear/silentgear_parts/` | 48 个 JSON |
| 特性 | `src/generated/resources/data/silentgear/silentgear_traits/` | 77 个 JSON |
| 英文 | `src/main/resources/assets/silentgear/lang/en_us.json` | 1200 键 |
| 中文 | `src/main/resources/assets/silentgear/lang/zh_cn.json` | 714 键（613 匹配当前 en_us） |

上游是 **数据包 JSON**，由 `data/MaterialsProvider.java` 生成，运行时由
`core/DataResourceManager.java` 经 `ResourceManagerReloadListener` 加载并同步客户端。
1.12.2 移植是 **每个部件一个 JSON**（`assets/silentgear/materials/main_iron.json` 等），
由 `ItemPart.Loader` 读取。因此需要 **fan-out 转换器**，不能直接拷贝。

### 上游材料 schema（精简）

```jsonc
{
  "type": "silentgear:simple",         // simple | compound | custom_compound | processed
  "parent": "silentgear:empty",        // 继承；1.12.2 需要展平
  "crafting": {
    "ingredient": "#c:ingots/iron",    // 或 {tag:...} / 自定义 ingredient
    "categories": ["metal", "intermediate"],
    "gear_type_blacklist": ["silentgear:all"],
    "part_substitutes": {"silentgear:rod": "#c:rods/iron"},
    "can_salvage": true
  },
  "display": {
    "color": "#FFFFFFFF",
    "main_texture_type": "HIGH_CONTRAST", // HIGH_CONTRAST | LOW_CONTRAST
    "name": {"translate": "material.silentgear.iron"}
  },
  "properties": {
    "silentgear:main": { "durability": 250, "harvest_speed": 6,
      "attack_damage": 2, "armor": 15, "traits": [...] },
    "silentgear:rod": { ... },
    "silentgear:tip": { ... }
  }
}
```

## 2. 1.12.2 目标 schema（当前）

```jsonc
{
  "type": "main | rod | tip | grip | bowstring | misc_upgrade | highlight",
  "stats": [{"name": "durability", "value": 250, "op": "ADD|AVG|MAX|MUL1|MUL2"}],
  "traits": [{"name": "silentgear:malleable", "level": 3}],
  "crafting_items": {
    "normal": {"item": "minecraft:iron_ingot", "data": 0, "oredict": "ingotIron"},
    "small":  {"item": "...", "data": 0, "oredict": "..."}
  },
  "display": {
    "all": {"texture_suffix": "iron", "texture_color": "FFFFFF", "broken_color": "..."},
    "hoe": {"texture_suffix": "generic_hc", "texture_color": "9E9E9E"},
    "paxel": {...}, "saw": {...}, "shears": {...},
    "mace": {...}, "spear": {...}, "knife": {...},
    "name_color": "white", "hidden": false
  },
  "availability": {"enabled": true, "tier": 2, "tool_blacklist": []}
}
```

## 3. 映射规则

### 3.1 部件 fan-out

上游一个材料按 `properties` 的部件键拆成多个文件：

| 上游 properties 键 | 1.12.2 文件 | 1.12.2 类型 |
|---|---|---|
| `silentgear:main` | `main_<id>.json` | `main` |
| `silentgear:rod` | `rod_<id>.json` | `rod` |
| `silentgear:tip` | `tip_<id>.json` | `tip` |
| `silentgear:grip` | `grip_<id>.json` | `grip` |
| `silentgear:cord` | `bowstring_<id>.json` | `bowstring`（1.12 旧名） |
| `silentgear:binding` | 暂不生成（1.12 无 PartBinding 注册） | — |
| `silentgear:coating` / `lining` / `fletching` / `setting` | 暂不生成（需新 PartType + 物品） | — |
| `silentgear:misc_upgrade` | 已有 `misc_spoon` / `misc_red_card`，手工维护 | `misc_upgrade` |

子材料（`wood/oak`、`stone/andesite`、`terracotta/red`、`wool/light_gray` 等）
必须按 `parent` 展平后分别生成文件；1.12.2 没有 `parent` 继承。

### 3.2 属性名映射

| 上游 `GearProperties` | 1.12.2 `CommonItemStats` | 备注 |
|---|---|---|
| `durability` | `durability` | |
| `armor_durability` | `armor_durability` | |
| `repair_efficiency` | `repair_efficiency` | |
| `repair_value` | 新增 `repair_value` | 1.12 没有 |
| `enchantment_value` | `enchantability` | 重命名 |
| `charging_value` | 新增 `charging_value` | 1.12 的 `chargeability` 无效键必须修正 |
| `rarity` | `rarity` | |
| `harvest_speed` | `harvest_speed` | |
| `harvest_tier` | `harvest_level` | 对象 → int（`level_hint`），另存显示名 |
| `block_reach` / `attack_reach` | 新增（或合并 `reach_distance`） | 1.12 只有 `reach_distance` |
| `attack_damage` | `melee_damage` | 重命名 |
| `attack_speed` | `attack_speed` | 支持 `attack_speed/axe` 等覆盖 → 写入对应 gear type 的 display/stat 特殊处理 |
| `magic_damage` | `magic_damage` | |
| `magic_armor` | `magic_armor` | |
| `armor` | `armor` | 支持 `armor/helmet` 等 → 1.12 无 per-slot，需扩展或取平均值 |
| `armor_toughness` | `armor_toughness` | |
| `knockback_resistance` | 新增 | |
| `ranged_damage` | `ranged_damage` | |
| `draw_speed` | `ranged_speed` | 重命名/合并 |
| `projectile_speed` / `projectile_accuracy` | 新增（可选） | 1.12 弓没有独立属性 |
| `swing_animation` / `kinetic_weapon` / `additive` | 新增或忽略 | 需要代码支持 |
| `traits` | `traits[{name,level}]` | 条件（conditions）暂丢弃 |

**操作符一一对应：** `AVERAGE/avg`→`AVG`，`MAX/max`→`MAX`，`ADD/add`→`ADD`，
`MULTIPLY_BASE/mul1`→`MUL1`，`MULTIPLY_TOTAL/mul2`→`MUL2`。

### 3.3 Ingredient 映射（常用）

| 上游 tag | 1.12.2 oredict |
|---|---|
| `#c:ingots/iron` | `ingotIron` |
| `#c:ingots/gold` | `ingotGold` |
| `#c:gems/diamond` | `gemDiamond` |
| `#c:gems/emerald` | `gemEmerald` |
| `#c:gems/lapis` | `gemLapis` |
| `#c:gems/quartz` | `gemQuartz` |
| `#c:dusts/redstone` | `dustRedstone` |
| `#c:dusts/glowstone` | `dustGlowstone` |
| `#c:rods/wooden` | `stickWood` |
| `#c:rods/iron` | `rodIron`（若存在） |
| `#c:planks` | `plankWood` |
| `#c:cobblestones` | `cobblestone` |
| `#c:obsidians` | `obsidian` |
| `#c:netherracks` | `netherrack` |
| `#c:end_stones` | `endstone` |
| `#c:sandstone/uncolored` | `sandstone` |
| `#c:string` | `string` |
| `#c:leathers` | `leather` |
| `#c:wool` | `wool`（1.12 无颜色 tag，需按 meta 拆分） |

1.12.2 没有 "optional tag" 概念：转换器应在 `OreDictionary.doesOreNameExist` 为真时生成
`oredict` 条目，否则把材料标记为 disabled 或跳过。

## 4. 分波次移植（安全优先）

### 第 0 波：数据修正（不改代码）
- `main_iron.json` / `main_crimson_steel.json` 的 `chargeability` 是无效 stat，改为
  `charging_value`（需先加 stat）或删除。
- `main_terracotta.json` 的合成物 `minecraft:brick` 改为 `minecraft:hardened_clay`。
- 删除 `main_test.json` / `main_test2.json`（未注册的开发样例）或移到 `_src`。
- 修正 `traits/crude.json` 的 `cancels_with`（应为 `synergy_boost`）。

### 第 1 波：1.12.2 原生材料（零新物品）
stone + andesite/diorite/granite、wood + 6 种木板、terracotta + 16 色（`hardened_clay` meta）、
wool + 16 色、flint、bone、obsidian、end_stone、netherrack、quartz、lapis、redstone、
glowstone、paper、leather、leaves、feather、string、vine、blaze_rod、end_rod。

### 第 2 波：矿物词典可选金属
steel、bronze、brass、invar、silver、nickel、lead、platinum、electrum、tin、aluminum、
bismuth 系、signalum/lumium/enderium/refined_*。  
**规则：** oredict 不存在时跳过，不注册半残材料；不要生成无获取途径的材料。

### 第 3 波：新部件类型（第一次代码改动）
`cord`（已有 bowstring 可复用）、`coating`、`lining`、`fletching`、`setting`、`binding`：
- 新增 `PartType` 枚举值/`ItemPart` 子类、物品注册、`equipment/*.json` 的 part count、
  `PartRegistry`、JEI/语言/贴图。
- 这一步会解锁约 50 个材料的非主部件属性；必须单独验收。

### 第 4 波：现代/模组专属材料（最后）
bamboo、basalt、blackstone、amethyst、netherite、phantom_membrane、turtle、
crushed_shulker_shell、fine_silk*、sheet_metal、tyrian_steel、compound 材料。
**前提：** 先决定替代物品/方块或跳过；合金链（Alloy Forge 等）是独立大版本。

## 5. 语言文件方案

当前 1.12.2 是 `.lang`（`key=value`），上游是 `.json`；键名也不同：

| 1.12.2 | 上游 |
|---|---|
| `material.silentgear.<partId>.name` | `material.silentgear.<materialId>` |
| `stat.silentgear.<stat>` | `property.silentgear.<key>` |
| `trait.silentgear.<trait>` | `trait.silentgear.<trait>`（描述在 `.desc`/JSON） |
| `part.silentgear.type.<type>` | `part.silentgear.type.<type>` |
| — | `gearType.silentgear.*`、`material.group.*`、`grade.*`、`harvestTier.*` |

**策略：**
1. 先冻结术语表（见下），再批量生成键，最后翻译；避免返工。
2. 1.12.2 保留现有 `material.silentgear.<partId>.name` 键，新增材料按 `main_/rod_/tip_` 规则生成。
3. 属性键统一用 `stat.silentgear.<stat>`，不要照搬上游 `property.*`（1.12 代码用的是 `stat.*`）。
4. 语言文件必须 UTF-8；用脚本读写时显式指定编码。
5. 中英文都要审校：英文以最新 `en_us.json` 为准，中文在官方 `zh_cn.json` 基础上补齐/修正，
   不直译，保留 Minecraft 官方译名。

### 术语表（冻结）

| English | 中文 |
|---|---|
| Main Part | 主要部件 |
| Tool Rod | 工具杆 |
| Tip Upgrade | 镶嵌升级 |
| Binding | 绑定结 |
| Grip | 握把 |
| Coating | 涂层 |
| Lining | 衬里 |
| Cord | 弓弦 |
| Fletching | 箭羽 |
| Jewelry Setting | 宝石座 |
| Misc Upgrade | 杂项升级 |
| Durability | 耐久 |
| Armor Durability | 盔甲耐久 |
| Armor / Armor Toughness | 护甲 / 盔甲韧性 |
| Enchantment Value | 附魔能力 |
| Harvest Tier / Speed | 挖掘等级 / 挖掘速度 |
| Attack Damage / Speed | 攻击伤害 / 攻击速度 |
| Magic Damage / Armor | 魔法伤害 / 魔法护甲 |
| Ranged Damage | 远程伤害 |
| Draw Speed | 拉弓速度 |
| Projectile Speed / Accuracy | 弹射物速度 / 弹射物精准度 |
| Charging Value | 蓄力值 |
| Rarity | 稀有度 |
| Repair Efficiency / Bonus | 修复效率 / 修复加成 |
| Block Reach / Attack Reach | 方块触及距离 / 攻击触及距离 |
| Knockback Resistance | 击退抗性 |
| Trait / Property | 特性 / 属性 |
| Grade | 等级（品级） |

材料名采用 Minecraft 官方译名：下界岩、下界木、深红铁、深红钢、荧石、青金石、
黑曜石、末地石、陶瓦、羊毛、皮革、骨、燧石、纸、幻翼膜、海晶石、黏液、藤蔓、
亚麻、筋、细丝；自定义合金保留音译（蔚蓝银、蔚蓝琥珀金、泰里安钢等）并在首次出现处
加英文括号。

## 6. 验收清单

- [ ] 转换器可重复运行，输出稳定（排序/缩进/UTF-8）。
- [ ] `python _tools/check_textures.py` → 0 missing（新材料贴图齐全后）。
- [ ] `gradlew build` 成功。
- [ ] `runClient` 启动无新增 ERROR；创造页/JEI 可见新材料。
- [ ] 每个波次单独提交/记录，失败可回滚。
- [ ] 语言文件键覆盖率 100%（en_US 与 zh_CN 键集合一致）。
- [ ] 不在每帧/每 tick 路径新增开销；属性查询仍走 NBT。
