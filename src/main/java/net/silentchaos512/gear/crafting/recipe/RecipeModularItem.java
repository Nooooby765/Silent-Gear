/*
 * Silent Gear -- RecipeModularItem
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

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.silentchaos512.gear.api.item.ICoreItem;
import net.silentchaos512.gear.api.parts.ItemPartData;
import net.silentchaos512.gear.api.parts.PartType;
import net.silentchaos512.gear.item.ToolHead;
import net.silentchaos512.lib.recipe.RecipeBaseSL;

import javax.annotation.Nonnull;
import java.util.*;

public class RecipeModularItem extends RecipeBaseSL {
    private final ICoreItem item;

    public RecipeModularItem(ICoreItem item) {
        this.item = item;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        Collection<ItemStack> parts = getComponents(inv);
        return getCraftingResult(parts);
    }

    public ItemStack getCraftingResult(Collection<ItemStack> parts) {
        List<ItemPartData> data = new ArrayList<>();
        // LinkedHashMap keeps the result deterministic: the order of non-main parts can affect
        // floating-point stat aggregation slightly.
        Map<PartType, ItemPartData> partsByType = new LinkedHashMap<>();

        for (ItemStack stack : parts) {
            ItemPartData part = ItemPartData.fromStack(stack);
            if (stack.getItem() instanceof ToolHead) {
                if (!ToolHead.getToolClass(stack).equals(this.item.getGearClass()))
                    return ItemStack.EMPTY;
                data.addAll(ToolHead.getAllParts(stack));
            }
            else if (part != null) {
                PartType type = part.getPart().getType();
                if (partsByType.containsKey(type) && partsByType.get(type).getPart() != part.getPart())
                    return ItemStack.EMPTY;
                partsByType.put(part.getPart().getType(), part);
            }
        }

        data.addAll(partsByType.values());
        return this.item.construct((Item) this.item, data);
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        return item.matchesRecipe(getComponents(inv));
    }

    @Nonnull
    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack((Item) this.item);
    }

    private Collection<ItemStack> getComponents(InventoryCrafting inv) {
        // Tool heads first, then every other non-empty stack. matchesRecipe rejects non-part
        // stacks itself, so the old PartRegistry filter and the non-empty count check are
        // redundant. One result list instead of three temporary lists.
        List<ItemStack> parts = new ArrayList<>();
        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ToolHead)
                parts.add(stack);
        }
        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty() && !(stack.getItem() instanceof ToolHead))
                parts.add(stack);
        }
        return parts;
    }
}
