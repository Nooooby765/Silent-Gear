/*
 * Silent Gear -- TridentEventHandler
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

package net.silentchaos512.gear.compat.trident;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.silentchaos512.gear.SilentGear;

/**
 * Common-side registration hook for the optional Trident integration. Loaded on every launch, but
 * no Trident class is referenced outside the mod check, so the base mod is unaffected when absent.
 */
@Mod.EventBusSubscriber(modid = SilentGear.MOD_ID)
public final class TridentEventHandler {
    private TridentEventHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRegisterItems(RegistryEvent.Register<Item> event) {
        // MOD_ID is a compile-time constant, so this check does not even load TridentIntegration when absent.
        if (!Loader.isModLoaded(TridentIntegration.MOD_ID)) {
            return;
        }
        TridentIntegration.onRegisterItems(event);
    }
}
