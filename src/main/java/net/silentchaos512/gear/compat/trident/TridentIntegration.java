/*
 * Silent Gear -- TridentIntegration
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
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.item.ICoreTool;
import net.silentchaos512.gear.init.ModItems;
import net.silentchaos512.gear.item.blueprint.Blueprint;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registers the Silent Gear trident and its blueprint/template once during the item registry
 * event. The existing systems (recipes, JEI, colors, models, repair and salvage) pick the items
 * up automatically through the shared gear class maps.
 */
public final class TridentIntegration {
    public static final String MOD_ID = "trident";
    public static final String GEAR_CLASS = "trident";

    @Nullable
    private static ICoreTool item;
    private static final List<Blueprint> BLUEPRINTS = new ArrayList<>();

    private TridentIntegration() {
    }

    /**
     * Called only when {@link #MOD_ID} is loaded; instantiates the Trident-specific class here to
     * keep it out of the class-loading path of every other launch.
     */
    public static void onRegisterItems(RegistryEvent.Register<Item> event) {
        if (item != null) {
            return;
        }

        ICoreTool trident = new CoreTrident();
        trident.getItem().setTranslationKey(SilentGear.MOD_ID + "." + GEAR_CLASS);
        trident.getItem().setRegistryName(SilentGear.MOD_ID, GEAR_CLASS);
        trident.getItem().setCreativeTab(SilentGear.ITEM_GROUP);
        event.getRegistry().register(trident.getItem());

        // Inserting into the gear-class maps here (instead of ModItems) is the whole integration surface.
        ModItems.toolClasses.put(GEAR_CLASS, trident);
        ModItems.gearClasses.put(GEAR_CLASS, trident);

        Blueprint blueprint = createBlueprint(trident, false, "blueprint_" + GEAR_CLASS);
        Blueprint template = createBlueprint(trident, true, "template_" + GEAR_CLASS);
        event.getRegistry().register(blueprint);
        event.getRegistry().register(template);
        BLUEPRINTS.add(blueprint);
        BLUEPRINTS.add(template);
        ModItems.blueprints.add(blueprint);
        ModItems.blueprints.add(template);

        item = trident;
    }

    private static Blueprint createBlueprint(ICoreTool trident, boolean singleUse, String key) {
        Blueprint blueprint = new Blueprint(singleUse, trident,
                parts -> ModItems.toolHead.getStack(GEAR_CLASS, parts));
        blueprint.setTranslationKey(SilentGear.MOD_ID + "." + key);
        blueprint.setRegistryName(SilentGear.MOD_ID, key);
        blueprint.setCreativeTab(SilentGear.ITEM_GROUP);
        return blueprint;
    }

    @Nullable
    public static ICoreTool getItem() {
        return item;
    }

    public static List<Blueprint> getBlueprints() {
        return Collections.unmodifiableList(BLUEPRINTS);
    }
}
