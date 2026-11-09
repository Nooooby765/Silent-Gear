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
 * Client-only model locations, kept side-only so {@link net.silentchaos512.gear.compat.trident.CoreTrident}
 * never references client types in its static initializer (dedicated servers would fail to load).
 */
@SideOnly(Side.CLIENT)
public final class TridentClientModels {
    /**
     * Layered {@code ToolModel} for the "trident" gear class; both GUI and hand model point here,
     * so the Trident mod's render mixins use Silent Gear rendering instead of its 3D model.
     */
    public static final ModelResourceLocation INVENTORY =
            new ModelResourceLocation(SilentGear.MOD_ID + ":" + "trident", "inventory");

    private TridentClientModels() {
    }
}
