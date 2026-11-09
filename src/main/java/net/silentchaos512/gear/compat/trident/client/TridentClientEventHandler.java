/*
 * Silent Gear -- TridentClientEventHandler
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
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.item.ICoreTool;
import net.silentchaos512.gear.compat.trident.TridentIntegration;
import net.silentchaos512.gear.item.blueprint.Blueprint;
import net.silentchaos512.lib.client.render.TEISRCustomEnchantedEffect;

/**
 * Client-side setup for the trident integration: models, the enchanted-effect TESR and the
 * blueprint/template item colors (gear colors come from {@code ColorHandlers}). Only subscribed on
 * the physical client and exits immediately when the Trident mod is absent.
 */
@Mod.EventBusSubscriber(modid = SilentGear.MOD_ID, value = Side.CLIENT)
public final class TridentClientEventHandler {
    private TridentClientEventHandler() {
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        if (!Loader.isModLoaded(TridentIntegration.MOD_ID)) {
            return;
        }

        ICoreTool trident = TridentIntegration.getItem();
        if (trident == null) {
            return;
        }

        Item item = trident.getItem();
        registerModel(item);
        // Same TESR applies to every ICustomEnchantColor item; renders the layered ToolModel through item colors.
        item.setTileEntityItemStackRenderer(TEISRCustomEnchantedEffect.INSTANCE);

        for (Blueprint blueprint : TridentIntegration.getBlueprints()) {
            registerModel(blueprint);
        }
    }

    @SubscribeEvent
    public static void onItemColors(ColorHandlerEvent.Item event) {
        if (!Loader.isModLoaded(TridentIntegration.MOD_ID)) {
            return;
        }

        for (Blueprint blueprint : TridentIntegration.getBlueprints()) {
            event.getItemColors().registerItemColorHandler(blueprint.getColorHandler(), blueprint);
        }
    }

    private static void registerModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
