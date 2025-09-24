/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.nether_api.mod.client.config;

import com.google.common.collect.Lists;
import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.IConfigElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;
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
    @Override
    public void initialize(@Nonnull final Minecraft minecraftInstance) {}

    @Override
    public boolean hasConfigGui() { return true; }

    @Nonnull
    @Override
    public GuiScreen createConfigGui(@Nonnull final GuiScreen parentScreen) {
        @Nonnull final List<IConfigElement> elements = sorted(Lists.newArrayList(
                // Creates a dummy config category filled with all the mod compatibility settings.
                new DummyConfigElement.DummyCategoryElement("nether_api/compat (dummy category)", "configgui.nether_api.compat",
                sorted(Arrays.stream(ConfigManager.getModConfigClasses(NetherAPI.MODID))
                .filter(cfg -> cfg != NetherAPIConfig.class)
                .map(ConfigElement::from)
                .collect(Collectors.toList()))),
                // Creates a dummy config category filled with all the vanilla settings (sorted).
                new DummyConfigElement.DummyCategoryElement("nether_api/vanilla", "nether_api/vanilla",
                sorted(ConfigElement.from(NetherAPIConfig.class).getChildElements()))
        ));

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
