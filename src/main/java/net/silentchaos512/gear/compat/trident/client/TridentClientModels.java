/*
 * Silent Gear -- TridentClientModels
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

package net.silentchaos512.gear.compat.trident.client;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.silentchaos512.gear.SilentGear;

/**
 * Client-only model locations for the trident integration. Kept in a side-only class so the
 * common {@link net.silentchaos512.gear.compat.trident.CoreTrident} class never references client
 * types in its static initializer (dedicated servers would otherwise fail to load it).
 */
@SideOnly(Side.CLIENT)
public final class TridentClientModels {
    /**
     * The layered {@code ToolModel} registered for the "trident" gear class. Both the GUI model
     * and the hand model point here: the Trident mod's render mixins then use the normal Silent
     * Gear layered/colored rendering instead of its built-in 3D model.
     */
    public static final ModelResourceLocation INVENTORY =
            new ModelResourceLocation(SilentGear.MOD_ID + ":" + "trident", "inventory");

    private TridentClientModels() {
    }
}
