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
 * Common-side registration hook for the optional Trident integration.
 * <p>
 * The class is loaded by Forge on every launch (it only contains common types), but its body is a
 * single mod check when the Trident mod is absent. No Trident class is referenced in any field,
 * method signature or static initializer, so the base mod behaves byte-for-byte as it did
 * before when the mod is not installed.
 */
@Mod.EventBusSubscriber(modid = SilentGear.MOD_ID)
public final class TridentEventHandler {
    private TridentEventHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRegisterItems(RegistryEvent.Register<Item> event) {
        // MOD_ID is a compile-time constant, so this check does not even load TridentIntegration
        // when the Trident mod is missing.
        if (!Loader.isModLoaded(TridentIntegration.MOD_ID)) {
            return;
        }
        TridentIntegration.onRegisterItems(event);
    }
}
