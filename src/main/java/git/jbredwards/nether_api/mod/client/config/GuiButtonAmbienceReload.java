package git.jbredwards.nether_api.mod.client.config;

import com.google.common.collect.Lists;
import git.jbredwards.nether_api.mod.NetherAPI;
import git.jbredwards.nether_api.mod.common.config.NetherAPIConfig;
import git.jbredwards.nether_api.mod.common.config.ambience.AmbienceConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.client.config.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author jbred
 *
 */
public class GuiButtonAmbienceReload extends GuiConfigEntries.CategoryEntry
{
    @Nonnull private static final SystemToast.Type TOAST_TYPE = Objects.requireNonNull(EnumHelper.addEnum(SystemToast.Type.class, NetherAPI.MODID, new Class[0]));
    @Nonnull private static final ITextComponent TOAST_TEXT = new TextComponentTranslation("configgui." + NetherAPI.MODID + ".ambience.toast");
    @Nonnull private static final ITextComponent TOAST_TEXT_ERROR = new TextComponentTranslation("configgui." + NetherAPI.MODID + ".ambience.toastError");
    @Nonnull private static final ITextComponent TOAST_SUB = new TextComponentString(NetherAPI.MODID + "/ambience.json");

    @Nonnull protected final GuiButtonExt btnReload;
    @Nonnull protected final HoverChecker reloadHoverChecker;
    @Nonnull protected final List<String> reloadTooltip;

    public GuiButtonAmbienceReload(@Nonnull final GuiConfig owningScreen, @Nonnull final GuiConfigEntries owningEntryList, @Nonnull final IConfigElement configElement) {
        super(owningScreen, owningEntryList, configElement);
        btnReload = new GuiButtonExt(0, 0, 0, 18, 18, "⟳");
        reloadTooltip = Lists.newArrayList(I18n.format("configgui." + NetherAPI.MODID + ".ambience.tooltip.btn"));
        reloadHoverChecker = new HoverChecker(btnReload, 800);
        btnUndoChanges.visible = false;
        btnDefault.visible = false;
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partial) {
        btnReload.enabled = !NetherAPIConfig.ambienceJsonSync || !owningScreen.isWorldRunning || mc.isGamePaused();
        btnReload.x = owningEntryList.scrollBarX - 44;
        btnReload.y = y;
        btnReload.drawButton(mc, mouseX, mouseY, partial);
        super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partial);
    }

    @Override
    public void drawToolTip(final int mouseX, final int mouseY) {
        if(reloadHoverChecker.checkHover(mouseX, mouseY, mouseY < owningScreen.entryList.bottom && mouseY > owningScreen.entryList.top)) {
            owningScreen.drawToolTip(reloadTooltip, mouseX, mouseY);
        }

        super.drawToolTip(mouseX, mouseY);
    }

    @Override
    public boolean mousePressed(final int index, final int x, final int y, final int mouseEvent, final int relativeX, final int relativeY) {
        if(btnReload.mousePressed(mc, x, y)) {
            btnReload.playPressSound(mc.getSoundHandler());
            SystemToast.addOrUpdate(Minecraft.getMinecraft().getToastGui(), TOAST_TYPE, AmbienceConfig.load() ? TOAST_TEXT : TOAST_TEXT_ERROR, TOAST_SUB);
            return true;
        }

        else return super.mousePressed(index, x, y, mouseEvent, relativeX, relativeY);
    }

    @Override
    public boolean enabled() {
        return false;
    }
}
