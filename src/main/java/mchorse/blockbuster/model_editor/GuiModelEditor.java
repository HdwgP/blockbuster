package mchorse.blockbuster.model_editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;

import mchorse.blockbuster.model_editor.elements.GuiLimbEditor;
import mchorse.blockbuster.model_editor.elements.GuiLimbsList;
import mchorse.blockbuster.model_editor.elements.GuiLimbsList.ILimbPicker;
import mchorse.blockbuster.model_editor.elements.GuiListViewer;
import mchorse.blockbuster.model_editor.elements.GuiListViewer.IListResponder;
import mchorse.blockbuster.model_editor.modal.GuiAlertModal;
import mchorse.blockbuster.model_editor.modal.GuiInputModal;
import mchorse.blockbuster.model_editor.modal.GuiModal;
import mchorse.blockbuster.model_editor.modal.IModalCallback;
import mchorse.metamorph.api.models.Model;
import mchorse.metamorph.api.models.Model.Limb;
import mchorse.metamorph.client.model.ModelCustom;
import mchorse.metamorph.client.model.parsing.ModelParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

/**
 * Model editor GUI
 *
 * This GUI is responsible providing to player tools to edit custom models,
 * just like McME, but better and up to date.
 */
public class GuiModelEditor extends GuiScreen implements IModalCallback, IListResponder, ILimbPicker
{
    /**
     * Currently data model which we are editing
     */
    private Model data;

    /**
     * Compiled data model which we are currently editing
     */
    private ModelCustom model;

    /**
     * Cached texture path
     */
    private ResourceLocation textureRL;

    /**
     * Current modal
     */
    public GuiModal currentModal;

    /* GUI fields */

    /**
     * Available poses
     */
    private GuiListViewer poses;

    /**
     * Limbs sidebar
     */
    private GuiLimbsList limbs;

    /**
     * Limb editor
     */
    private GuiLimbEditor limbEditor;

    /**
     * Texture path field
     */
    private GuiTextField texture;

    /**
     * Pose field
     */
    private GuiButton pose;

    /**
     * Save button, this will prompt user to choose a name
     */
    private GuiButton save;

    /**
     * Create clean, new, model out of existing ones or
     */
    private GuiButton clean;

    /**
     * Ticks timer for arm idling animation
     */
    private int timer;

    /**
     * Setup by default the
     */
    public GuiModelEditor()
    {
        this.poses = new GuiListViewer(null, this);
        this.limbs = new GuiLimbsList(this);
        this.limbEditor = new GuiLimbEditor(this);
        this.setupModel(ModelCustom.MODELS.get("blockbuster.steve"));
    }

    /**
     * Setup the model
     */
    private void setupModel(ModelCustom model)
    {
        this.data = ModelUtils.cloneModel(model.model);

        List<String> poses = new ArrayList<String>();
        poses.addAll(this.data.poses.keySet());

        this.poses.setStrings(poses);
        this.limbs.setModel(this.data);
        this.buildModel();
    }

    /**
     * Build the model from data model
     */
    private void buildModel()
    {
        try
        {
            this.model = new ModelParser().parseModel(this.data, ModelCustom.class);
            this.changePose("standing");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Change pose
     */
    private void changePose(String pose)
    {
        this.model.pose = this.data.getPose(pose);

        if (this.pose != null)
        {
            this.pose.displayString = pose;
        }

        this.limbEditor.setPose(this.model.pose);
    }

    @Override
    public void initGui()
    {
        /* Initiate the texture field */
        this.texture = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 49, this.height - 24, 98, 18);
        this.texture.setMaxStringLength(400);
        this.texture.setText("blockbuster.actors:steve/Walter");
        this.textureRL = new ResourceLocation(this.texture.getText());

        /* Buttons */
        this.save = new GuiButton(0, this.width - 60, 5, 50, 20, "Save");
        this.clean = new GuiButton(1, this.width - 115, 5, 50, 20, "New");
        this.pose = new GuiButton(2, this.width - 110, this.height - 25, 100, 20, "standing");

        this.buttonList.add(this.save);
        this.buttonList.add(this.clean);
        this.buttonList.add(this.pose);

        this.poses.updateRect(this.width - 110, this.height - 106, 100, 80);
        this.poses.setHidden(true);

        this.limbEditor.initiate(10, 47);
        this.limbs.updateRect(this.width - 111, 47, 102, this.height - 47 - 30);

        if (this.currentModal != null)
        {
            this.currentModal.initiate();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 0)
        {
            this.currentModal = new GuiInputModal(this, this.fontRendererObj);
            this.currentModal.label = "Say my name?";
            this.currentModal.initiate();
        }
        else if (button.id == 1)
        {
            /* New one */
        }
        else if (button.id == 2)
        {
            this.poses.setHidden(false);
        }
    }

    /**
     * It's like {@link #actionPerformed(GuiButton)}, but only comes from modal
     * windows.
     */
    @Override
    public void modalButtonPressed(GuiModal modal, GuiButton button)
    {
        if (button.id == -1)
        {
            this.currentModal = null;
        }
        else if (button.id == -2)
        {
            String input = ((GuiInputModal) this.currentModal).getInput();

            this.currentModal = new GuiAlertModal(this, this.fontRendererObj);
            this.currentModal.label = "Are you sure you want to ratched and clank with me, " + input + "?";
            this.currentModal.initiate();
        }
    }

    @Override
    public void pickedValue(String value)
    {
        this.changePose(value);
    }

    @Override
    public void pickLimb(Limb limb)
    {
        this.limbEditor.setLimb(limb);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (this.currentModal == null)
        {
            this.texture.textboxKeyTyped(typedChar, keyCode);

            if (this.texture.isFocused() && !this.texture.getText().equals(this.textureRL.toString()))
            {
                this.textureRL = new ResourceLocation(this.texture.getText());
            }

            this.limbEditor.keyTyped(typedChar, keyCode);
        }
        else
        {
            this.currentModal.keyTyped(typedChar, keyCode);
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        super.handleMouseInput();

        if (!this.poses.isInside(i, j))
        {
            this.limbs.handleMouseInput();
        }

        this.poses.handleMouseInput();
    }

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height)
    {
        super.setWorldAndResolution(mc, width, height);
        this.poses.setWorldAndResolution(mc, width, height);
        this.limbs.setWorldAndResolution(mc, width, height);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (this.poses.isInside(mouseX, mouseY))
        {
            return;
        }

        if (this.currentModal == null)
        {
            this.texture.mouseClicked(mouseX, mouseY, mouseButton);
            this.limbEditor.mouseClicked(mouseX, mouseY, mouseButton);

            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
        else
        {
            this.currentModal.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void updateScreen()
    {
        this.timer++;
    }

    /**
     * Draw the screen
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawHorizontalGradientRect(0, 0, 120, this.height, 0x55000000, 0x00000000);
        this.drawHorizontalGradientRect(this.width - 120, 0, this.width, this.height, 0x00000000, 0x55000000);

        /* Labels */
        this.fontRendererObj.drawStringWithShadow("Model Editor", 10, 10, 0xffffff);
        this.fontRendererObj.drawStringWithShadow("Limbs", this.width - 105, 35, 0xffffff);

        this.texture.drawTextBox();

        /* Draw the model */
        float scale = this.height / 3;
        float x = this.width / 2;
        float y = this.height / 2 + scale * 1.1F;
        float yaw = (x - mouseX) / this.width * 90;
        float pitch = (y + scale + mouseY) / this.height * 90 - 135;

        this.drawModel(x, y, scale, yaw, pitch);

        super.drawScreen(mouseX, mouseY, partialTicks);

        this.limbEditor.draw(mouseX, mouseY, partialTicks);
        this.limbs.drawScreen(mouseX, mouseY, partialTicks);
        this.poses.drawScreen(mouseX, mouseY, partialTicks);

        /* Draw current modal */
        if (this.currentModal != null)
        {
            this.currentModal.drawModal(mouseX, mouseY, partialTicks);
        }
    }

    /**
     * Draw currently edited model
     */
    private void drawModel(float x, float y, float scale, float yaw, float pitch)
    {
        EntityPlayer player = this.mc.thePlayer;
        float factor = 0.0625F;

        this.mc.renderEngine.bindTexture(this.textureRL);

        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 50.0F);
        GlStateManager.scale((-scale), scale, scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);

        RenderHelper.enableStandardItemLighting();

        GlStateManager.pushMatrix();
        GlStateManager.disableCull();

        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);
        GlStateManager.translate(0.0F, -1.501F, 0.0F);

        GlStateManager.enableAlpha();

        this.model.setLivingAnimations(player, 0, 0, 0);
        this.model.setRotationAngles(0, 0, this.timer, yaw, pitch, factor, player);

        GlStateManager.enableDepth();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        this.model.render(player, 0, 0, 0, 0, 0, factor);

        GlStateManager.disableDepth();

        GlStateManager.disableRescaleNormal();
        GlStateManager.disableAlpha();
        GlStateManager.popMatrix();

        GlStateManager.popMatrix();

        RenderHelper.disableStandardItemLighting();

        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    /**
     * Draws a rectangle with a horizontal gradient between the specified colors
     */
    protected void drawHorizontalGradientRect(int left, int top, int right, int bottom, int startColor, int endColor)
    {
        float a1 = (startColor >> 24 & 255) / 255.0F;
        float r1 = (startColor >> 16 & 255) / 255.0F;
        float g1 = (startColor >> 8 & 255) / 255.0F;
        float b1 = (startColor & 255) / 255.0F;
        float a2 = (endColor >> 24 & 255) / 255.0F;
        float r2 = (endColor >> 16 & 255) / 255.0F;
        float g2 = (endColor >> 8 & 255) / 255.0F;
        float b2 = (endColor & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);

        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos(right, top, this.zLevel).color(r2, g2, b2, a2).endVertex();
        vertexbuffer.pos(left, top, this.zLevel).color(r1, g1, b1, a1).endVertex();
        vertexbuffer.pos(left, bottom, this.zLevel).color(r1, g1, b1, a1).endVertex();
        vertexbuffer.pos(right, bottom, this.zLevel).color(r2, g2, b2, a2).endVertex();
        tessellator.draw();

        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
}