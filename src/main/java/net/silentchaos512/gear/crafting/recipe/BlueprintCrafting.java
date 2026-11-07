/*
 * Silent Gear -- BlueprintCrafting
 * Copyright (C) 2018 SilentChaos512
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.silentchaos512.gear.crafting.recipe;

import com.google.gson.JsonObject;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.silentchaos512.gear.api.parts.ItemPart;
import net.silentchaos512.gear.api.parts.ItemPartData;
import net.silentchaos512.gear.api.parts.PartType;
import net.silentchaos512.gear.init.ModItems;
import net.silentchaos512.gear.inventory.InventoryCraftingStation;
import net.silentchaos512.gear.item.blueprint.IBlueprint;
import net.silentchaos512.lib.collection.StackList;
import net.silentchaos512.lib.recipe.RecipeBaseSL;
import net.silentchaos512.lib.util.StackHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlueprintCrafting implements IRecipeFactory {
    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        return new Recipe(ModItems.toolHead);
    }

    private static class Recipe extends RecipeBaseSL {
        Item outputType;

        Recipe(Item outputType) {
            this.outputType = outputType;
        }

        @Override
        public ItemStack getCraftingResult(InventoryCrafting inv) {
            StackList list = StackHelper.getNonEmptyStacks(inv);
            ItemStack blueprint = list.firstOfType(IBlueprint.class);
            list.remove(blueprint);
            return ((IBlueprint) blueprint.getItem()).getCraftingResult(blueprint, list);
        }

        @Override
        public boolean matches(InventoryCrafting inv, World world) {
            StackList list = StackHelper.getNonEmptyStacks(inv);
            ItemStack blueprint = list.uniqueOfType(IBlueprint.class);

            // Only one blueprint
            if (blueprint.isEmpty()) {
                return false;
            }

            IBlueprint blueprintItem = (IBlueprint) blueprint.getItem();
            List<ItemPartData> materials = new ArrayList<>();
            for (ItemStack stack : list) {
                ItemPartData data = blueprintItem.getMaterial(stack);
                if (data != null)
                    materials.add(data);
            }
            int materialCount = materials.size();

            // Optional parts (lining/rod) do not count against the required main material cost,
            // but each optional type is capped independently.
            int requiredCount = 0;
            Map<PartType, Integer> optionalCounts = new HashMap<>();
            for (ItemPartData data : materials) {
                PartType type = data.getPart().getType();
                int optionalLimit = type == null ? 0 : blueprintItem.getOptionalPartCount(type);
                if (optionalLimit > 0) {
                    int used = optionalCounts.getOrDefault(type, 0) + 1;
                    if (used > optionalLimit) return false;
                    optionalCounts.put(type, used);
                } else {
                    ++requiredCount;
                }
            }

            // Right number of materials and nothing else? FIXME: blueprint book support?
            if (materialCount + 1 != list.size() || requiredCount != blueprintItem.getMaterialCost(blueprint)) {
                return false;
            }

            // Blueprint may restrict the accepted material types (legacy blueprints accept all)
            for (ItemPartData data : materials) {
                ItemPart part = data.getPart();
                if (part == null || !blueprintItem.isValidMaterial(part))
                    return false;
            }

            // Inventory allows mixing? Otherwise main materials must be uniform. Only blueprints
            // that accept optional parts may mix an optional part (lining/rod) with the mains;
            // legacy blueprints keep the stricter all-parts-uniform rule.
            if (inventoryAllowsMixedMaterial(inv)) return true;
            return optionalCounts.isEmpty() ? allPartsUniform(materials) : mainsAreUniform(materials);
        }

        private static boolean allPartsUniform(List<ItemPartData> materials) {
            ItemPart first = materials.isEmpty() ? null : materials.get(0).getPart();
            for (ItemPartData data : materials) {
                if (data.getPart() != first) return false;
            }
            return true;
        }

        private static boolean mainsAreUniform(List<ItemPartData> materials) {
            ItemPart main = null;
            for (ItemPartData data : materials) {
                if (data.getPart().getType() == PartType.MAIN) {
                    if (main == null) main = data.getPart();
                    else if (main != data.getPart()) return false;
                }
            }
            return true;
        }

        @Nonnull
        @Override
        public ItemStack getRecipeOutput() {
            return new ItemStack(outputType);
        }

        private static boolean inventoryAllowsMixedMaterial(InventoryCrafting inv) {
            return inv instanceof InventoryCraftingStation;
        }
    }
}
