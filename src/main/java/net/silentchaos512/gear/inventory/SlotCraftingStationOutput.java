/*
 * Silent Gear -- SlotCraftingStationOutput
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

package net.silentchaos512.gear.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.silentchaos512.gear.api.item.ICoreItem;
import net.silentchaos512.gear.api.parts.ItemPartData;
import net.silentchaos512.gear.block.craftingstation.ContainerCraftingStation;
import net.silentchaos512.gear.block.craftingstation.TileCraftingStation;

import java.util.Map;

/**
 * For Crafting Station output slot. Allows consumed gear parts to be removed.
 */
public class SlotCraftingStationOutput extends SlotCrafting {
    private final IInventory station;
    private final ContainerCraftingStation container;
    private int amountCrafted = 0; // TODO: Find some way to update this.

    public SlotCraftingStationOutput(ContainerCraftingStation container, EntityPlayer player, InventoryCrafting craftingInventory, IInventory inventoryIn, IInventory station, int slotIndex, int xPosition, int yPosition) {
        super(player, craftingInventory, inventoryIn, slotIndex, xPosition, yPosition);
        this.station = station;
        this.container = container;
    }

    // Shift-click path (amount is normally 2).
    @Override
    protected void onCrafting(ItemStack stack, int amount) {
        this.amountCrafted += amount;
        super.onCrafting(stack, amount);
        consumeParts(stack);
    }

    // Regular click path.
    @Override
    protected void onCrafting(ItemStack stack) {
        ++this.amountCrafted;
        super.onCrafting(stack);
    }

    @Override
    public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack) {
        super.onTake(thePlayer, stack);
        consumeParts(stack);
        this.amountCrafted = 0;
        return stack;
    }

    private void consumeParts(ItemStack stack) {
        if (stack.getItem() instanceof ICoreItem) {
            ICoreItem item = (ICoreItem) stack.getItem();

            Map<ItemPartData, Integer> partList = container.getCompatibleParts(item);
            for (Map.Entry<ItemPartData, Integer> entry : partList.entrySet()) {
                ItemPartData part = entry.getKey();
                int amount = entry.getValue();
                int slot = -1;
                for (int i = TileCraftingStation.GEAR_PARTS_START; i < TileCraftingStation.GEAR_PARTS_START + TileCraftingStation.GEAR_PARTS_SIZE; ++i) {
                    if (station.getStackInSlot(i).isItemEqual(part.getCraftingItem())) {
                        slot = i;
                        break;
                    }
                }

                if (slot >= 0) {
                    station.decrStackSize(slot, amount);
                }
            }
        }
    }
}
