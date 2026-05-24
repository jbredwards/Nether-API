/*
 * Copyright (C) <2025 to Present> <jbredwards>
 *
 * All rights are reserved, except where explicitly granted by the original
 * copyright holder or where explicitly granted by the Mod Permissions License as
 * published by Jbredwards, either version 1 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See the Mod Permissions License for more details
 * <https://www.github.com/jbredwards/mod-permissions-license>.
 */

package git.jbredwards.nether_api.mod.client.config;

import com.google.common.collect.Lists;
import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.IConfigElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author jbred
 *
 */
public final class NetherAPIGuiFactory implements IModGuiFactory
{
    @Nonnull
    private final Set<Class<?>> activeModCategories = new HashSet<>();

    @Override
    public boolean hasConfigGui() { return true; }

    @Override
    public void initialize(@Nonnull final Minecraft minecraftInstance) {
        if(NetherAPI.isBetterNetherLoaded) activeModCategories.add(NetherAPIConfig.BetterNether.class);
        if(NetherAPI.isBiomesOPlentyLoaded) activeModCategories.add(NetherAPIConfig.BOP.class);
        if(NetherAPI.isJourneyIntoTheLightLoaded) activeModCategories.add(NetherAPIConfig.JITL.class);
        if(NetherAPI.isNaturaLoaded) activeModCategories.add(NetherAPIConfig.Natura.class);
        if(NetherAPI.isNethercraftLoaded) activeModCategories.add(NetherAPIConfig.Nethercraft.class);
        if(NetherAPI.isNetherHexedKingdomLoaded) activeModCategories.add(NetherAPIConfig.NHK.class);
        if(NetherAPI.isStygianEndLoaded) activeModCategories.add(NetherAPIConfig.StygianEnd.class);
    }

    @Nonnull
    @Override
    public GuiScreen createConfigGui(@Nonnull final GuiScreen parentScreen) {
        @Nonnull final List<IConfigElement> elements = sorted(Lists.newArrayList(
                // Creates a dummy config category filled with all the mod compatibility settings.
                new DummyConfigElement.DummyCategoryElement("nether_api/compat (dummy category)", "configgui.nether_api.compat",
                sorted(activeModCategories.stream()
                .map(ConfigElement::from)
                .collect(Collectors.toList()))),
                // Creates a dummy config category filled with all the vanilla settings (sorted).
                new DummyConfigElement.DummyCategoryElement("nether_api/vanilla", "nether_api/vanilla",
                sorted(ConfigElement.from(NetherAPIConfig.class).getChildElements()))
        ));

        if(activeModCategories.isEmpty()) {
            // No mods with special compatibility settings are present. Bring all Vanilla entries to the front.
            elements.addAll(elements.get(1).getChildElements());
            // Remove categories.
            elements.remove(0);
            elements.remove(0);
        }

        return new GuiConfigTranslucent(parentScreen, elements, NetherAPI.MODID, false, false, I18n.format("configgui.nether_api.title"), null);
    }

    @Nonnull
    private static List<IConfigElement> sorted(@Nonnull final List<IConfigElement> elements) {
        elements.sort(Comparator.comparing(element -> I18n.format(element.getLanguageKey())));
        return elements;
    }

    @Nullable
    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() { return null; }
}
