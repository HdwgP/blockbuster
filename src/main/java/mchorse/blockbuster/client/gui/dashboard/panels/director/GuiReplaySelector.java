package mchorse.blockbuster.client.gui.dashboard.panels.director;

import java.util.function.Consumer;

import mchorse.blockbuster.recording.scene.Scene;
import org.lwjgl.opengl.GL11;

import mchorse.blockbuster.client.gui.dashboard.GuiDashboard;
import mchorse.blockbuster.recording.scene.Replay;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.utils.ScrollArea;
import mchorse.mclib.client.gui.utils.ScrollArea.ScrollDirection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

/**
 * This GUI is responsible for drawing replays available in the 
 * director thing
 */
public class GuiReplaySelector extends GuiElement
{
    private Scene scene;
    private Consumer<Replay> callback;
    public ScrollArea scroll;
    public int current = -1;

    public GuiReplaySelector(Minecraft mc, Consumer<Replay> callback)
    {
        super(mc);

        this.callback = callback;
        this.scroll = new ScrollArea(40);
        this.scroll.direction = ScrollDirection.HORIZONTAL;
    }

    public void setScene(Scene scene)
    {
        this.scene = scene;
        this.current = -1;
        this.update();
    }

    public void setReplay(Replay replay)
    {
        if (this.scene != null)
        {
            this.current = this.scene.replays.indexOf(replay);
        }
    }

    public void update()
    {
        this.scroll.setSize(this.scene.replays.size());
        this.scroll.clamp();
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);

        this.scroll.copy(this.area);
        this.scroll.w -= 32;
        this.scroll.clamp();
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (super.mouseClicked(mouseX, mouseY, mouseButton) || this.scroll.mouseClicked(mouseX, mouseY))
        {
            return true;
        }

        if (this.scroll.isInside(mouseX, mouseY))
        {
            int index = this.scroll.getIndex(mouseX, mouseY);
            int size = this.scene.replays.size();

            if (this.callback != null && index >= 0 && index < size && size != 0)
            {
                this.current = index;
                this.callback.accept(this.scene.replays.get(index));
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(int mouseX, int mouseY, int scroll)
    {
        if (super.mouseScrolled(mouseX, mouseY, scroll))
        {
            return true;
        }

        return this.scroll.mouseScroll(mouseX, mouseY, scroll);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);

        this.scroll.mouseReleased(mouseX, mouseY);
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        super.draw(tooltip, mouseX, mouseY, partialTicks);

        /* Background and shadows */
        Gui.drawRect(this.area.x, this.area.y, this.area.getX(1), this.area.getY(1), 0x88000000);
        this.drawGradientRect(this.area.x, this.area.y - 16, this.area.getX(1), this.area.y, 0x00000000, 0x88000000);

        this.scroll.drag(mouseX, mouseY);

        if (this.scene != null && !this.scene.replays.isEmpty())
        {
            int i = 0;
            int h = this.scroll.scrollItemSize;
            int hoverX = -1;
            String hovered = null;

            GuiScreen screen = this.mc.currentScreen;
            GuiUtils.scissor(this.scroll.x, this.scroll.y, this.scroll.w, this.scroll.h, screen.width, screen.height);

            for (Replay replay : this.scene.replays)
            {
                int x = this.area.x + i * h - this.scroll.scroll + h / 2;
                boolean hover = this.scroll.isInside(mouseX, mouseY) && mouseX >= x - h / 2 && mouseX < x + h / 2;
                boolean active = i == this.current || hover;

                if (replay.morph != null)
                {
                    replay.morph.renderOnScreen(this.mc.thePlayer, x, this.area.getY(active ? 0.9F : 0.8F), active ? 32 : 24, 1);
                }
                else
                {
                    GlStateManager.color(1, 1, 1);
                    GlStateManager.enableAlpha();
                    this.mc.renderEngine.bindTexture(GuiDashboard.GUI_ICONS);
                    this.drawTexturedModalRect(x - 8, this.area.getY(0.5F) - 8, 32, active ? 16 : 0, 16, 16);
                    GlStateManager.disableAlpha();
                }

                if (hover && !replay.id.isEmpty() && hovered == null)
                {
                    hovered = replay.id;
                    hoverX = x;
                }

                i++;
            }

            if (hovered != null)
            {
                int w = this.font.getStringWidth(hovered);
                int x = hoverX - w / 2;

                Gui.drawRect(x - 2, this.scroll.getY(0.5F) - 1, x + w + 2, this.scroll.getY(0.5F) + 9, 0x88000000);
                this.font.drawStringWithShadow(hovered, x, this.scroll.getY(0.5F), 0xffffff);
            }

            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
        else
        {
            this.drawCenteredString(this.font, I18n.format("blockbuster.gui.director.no_replays"), this.area.getX(0.5F), this.area.getY(0.5F) - 6, 0xffffff);
        }

        this.scroll.drawScrollbar();
    }
}