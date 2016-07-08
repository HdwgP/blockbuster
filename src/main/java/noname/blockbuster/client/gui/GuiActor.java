package noname.blockbuster.client.gui;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noname.blockbuster.ClientProxy;
import noname.blockbuster.client.gui.elements.GuiChildScreen;
import noname.blockbuster.client.gui.elements.GuiParentScreen;
import noname.blockbuster.client.gui.elements.GuiToggle;
import noname.blockbuster.entity.EntityActor;
import noname.blockbuster.network.Dispatcher;
import noname.blockbuster.network.common.PacketModifyActor;
import noname.blockbuster.network.common.director.PacketDirectorMapEdit;

/**
 * Actor configuration GUI
 *
 * This GUI is opened via player.openGui and has an id of 1. Most of the code
 * below is easy to understand, so no comments are needed.
 */
@SideOnly(Side.CLIENT)
public class GuiActor extends GuiChildScreen
{
    /* Cached localization strings */
    private String stringTitle = I18n.format("blockbuster.gui.actor.title");
    private String stringDefault = I18n.format("blockbuster.gui.actor.default");
    private String stringName = I18n.format("blockbuster.gui.actor.name");
    private String stringFilename = I18n.format("blockbuster.gui.actor.filename");
    private String stringSkin = I18n.format("blockbuster.gui.actor.skin");
    private String stringInvulnerability = I18n.format("blockbuster.gui.actor.invulnerability");

    /* Domain objects, they're provide data */
    private EntityActor actor;
    private BlockPos pos;
    private int id;

    private List<String> skins;
    private int skinIndex;

    /* GUI fields */
    private GuiTextField name;
    private GuiTextField filename;

    private GuiButton done;
    private GuiButton next;
    private GuiButton prev;
    private GuiButton restore;
    private GuiToggle invincibility;

    /**
     * Constructor for director map block
     */
    public GuiActor(GuiParentScreen parent, EntityActor actor, BlockPos pos, int id)
    {
        this(parent, actor);
        this.pos = pos;
        this.id = id;
    }

    /**
     * Constructor for director block and skin manager item
     */
    public GuiActor(GuiParentScreen parent, EntityActor actor)
    {
        super(parent);
        this.actor = actor;
        this.skins = ClientProxy.actorPack.getReloadedSkins();
        this.skinIndex = this.skins.indexOf(actor.skin);
    }

    /* Actions */

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 0)
        {
            this.saveAndQuit();
        }
        else if (button.id == 1)
        {
            this.updateSkin(this.skinIndex + 1);
        }
        else if (button.id == 2)
        {
            this.updateSkin(this.skinIndex - 1);
        }
        else if (button.id == 3)
        {
            this.skinIndex = -1;
            this.updateSkin();
        }
        else if (button.id == 4)
        {
            this.invincibility.toggle();
        }
    }

    /**
     * Save and quit this screen
     *
     * Depends on the fact where does this GUI was opened from, it either
     * sends modify actor packet, which modifies entity's properties directly,
     * or sends edit action to director map block
     */
    private void saveAndQuit()
    {
        SimpleNetworkWrapper dispatcher = Dispatcher.getInstance();

        String filename = this.filename.getText();
        String name = this.name.getText();
        String skin = this.getSkin();
        boolean invulnerability = this.invincibility.getValue();

        if (this.pos == null)
        {
            dispatcher.sendToServer(new PacketModifyActor(this.actor.getEntityId(), filename, name, skin, invulnerability));
        }
        else
        {
            this.actor.modify(filename, name, skin, invulnerability, false);

            dispatcher.sendToServer(new PacketDirectorMapEdit(this.pos, this.id, this.actor.toReplayString()));
        }

        this.close();
    }

    private void updateSkin(int index)
    {
        int min = 0;
        int max = this.skins.size() - 1;

        /* This expression is just like clamp, but with flipped range values */
        this.skinIndex = index < min ? max : (index > max ? min : index);
        this.updateSkin();
    }

    private void updateSkin()
    {
        this.actor.skin = this.getSkin();
    }

    private String getSkin()
    {
        return this.skinIndex >= 0 ? this.skins.get(this.skinIndex) : "";
    }

    /* Handling input */

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.name.mouseClicked(mouseX, mouseY, mouseButton);
        this.filename.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        this.name.textboxKeyTyped(typedChar, keyCode);
        this.filename.textboxKeyTyped(typedChar, keyCode);
    }

    /* Initiating GUI and drawing */

    /**
     * I think Mojang should come up with something better than hardcoded
     * positions and sizes for buttons. Something like HTML. Maybe I should
     * write this library (for constructing minecraft GUIs). Hm...
     */
    @Override
    public void initGui()
    {
        int x = 30;
        int w = 120;
        int y = 25;

        /* Initializing all GUI fields first */
        this.done = new GuiButton(0, x, this.height - 40, w, 20, I18n.format("blockbuster.gui.done"));
        this.next = new GuiButton(1, x, y + 80, w / 2 - 4, 20, I18n.format("blockbuster.gui.next"));
        this.prev = new GuiButton(2, x + w / 2 + 4, y + 80, w / 2 - 4, 20, I18n.format("blockbuster.gui.previous"));
        this.restore = new GuiButton(3, x, y + 105, w, 20, I18n.format("blockbuster.gui.restore"));
        this.invincibility = new GuiToggle(4, x, y + 145, w, 20, I18n.format("blockbuster.no"), I18n.format("blockbuster.yes"));

        this.name = new GuiTextField(5, this.fontRendererObj, x + 1, y + 1, w - 2, 18);
        this.filename = new GuiTextField(6, this.fontRendererObj, x + 1, y + 41, w - 2, 18);

        /* And then, we're configuring them and injecting input data */
        this.buttonList.add(this.done);
        this.buttonList.add(this.next);
        this.buttonList.add(this.prev);
        this.buttonList.add(this.restore);
        this.buttonList.add(this.invincibility);

        this.next.enabled = this.prev.enabled = this.skins.size() != 0;
        this.invincibility.setValue(this.actor.isEntityInvulnerable(DamageSource.anvil));

        this.name.setText(this.actor.hasCustomName() ? this.actor.getCustomNameTag() : "");
        this.name.setMaxStringLength(30);
        this.filename.setText(this.actor.filename);
        this.filename.setMaxStringLength(40);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        int centerX = this.width / 2;
        int x = 30;
        int y = 15;

        String skin = this.stringDefault;

        if (this.skinIndex != -1)
        {
            skin = this.skins.get(this.skinIndex);
        }

        this.drawDefaultBackground();
        this.drawString(this.fontRendererObj, this.stringTitle, x + 120 + 20, 15, 0xffffffff);

        this.drawString(this.fontRendererObj, this.stringName, x, y, 0xffcccccc);
        this.drawString(this.fontRendererObj, this.stringFilename, x, y + 40, 0xffcccccc);
        this.drawString(this.fontRendererObj, this.stringSkin, x, y + 80, 0xffcccccc);
        this.drawRightString(this.fontRendererObj, skin, x + 120, y + 80, 0xffffffff);
        this.drawString(this.fontRendererObj, this.stringInvulnerability, x, y + 145, 0xffcccccc);

        int size = this.height / 4;
        y = this.height / 2 + this.height / 4;
        x = x + 120 + 30;
        x = x + (this.width - x) / 2;

        this.actor.renderName = false;
        drawEntityOnScreen(x, y, size, x - mouseX, (y - size) - mouseY, this.actor);
        this.actor.renderName = true;

        this.name.drawTextBox();
        this.filename.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Draw right aligned text on the screen
     */
    public void drawRightString(FontRenderer fontRendererIn, String text, int x, int y, int color)
    {
        fontRendererIn.drawStringWithShadow(text, x - fontRendererIn.getStringWidth(text), y, color);
    }

    /**
     * Draw an entity on the screen
     *
     * Taken from minecraft's class GuiInventory
     */
    public static void drawEntityOnScreen(int posX, int posY, int scale, int mouseX, int mouseY, EntityLivingBase ent)
    {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, posY, 100.0F);
        GlStateManager.scale((-scale), scale, scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);

        float f = ent.renderYawOffset;
        float f1 = ent.rotationYaw;
        float f2 = ent.rotationPitch;
        float f3 = ent.prevRotationYawHead;
        float f4 = ent.rotationYawHead;

        ent.renderYawOffset = (float) Math.atan(mouseX / 40.0F) * 20.0F;
        ent.rotationYaw = (float) Math.atan(mouseX / 40.0F) * 40.0F;
        ent.rotationPitch = -((float) Math.atan(mouseY / 40.0F)) * 20.0F;
        ent.rotationYawHead = ent.rotationYaw;
        ent.prevRotationYawHead = ent.rotationYaw;

        GlStateManager.translate(0.0F, 0.0F, 0.0F);

        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        rendermanager.doRenderEntity(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        rendermanager.setRenderShadow(true);

        ent.renderYawOffset = f;
        ent.rotationYaw = f1;
        ent.rotationPitch = f2;
        ent.prevRotationYawHead = f3;
        ent.rotationYawHead = f4;

        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
}