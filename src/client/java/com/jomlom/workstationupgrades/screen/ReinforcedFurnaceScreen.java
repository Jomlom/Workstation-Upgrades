package com.jomlom.workstationupgrades.screen;

import com.jomlom.workstationupgrades.WorkstationUpgrades;
import com.jomlom.workstationupgrades.screenhandler.ReinforcedFurnaceScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ReinforcedFurnaceScreen extends HandledScreen<ReinforcedFurnaceScreenHandler> {

    private static final Identifier TEXTURE = Identifier.of(WorkstationUpgrades.MOD_ID, "textures/gui/reinforced_furnace.png");

    public ReinforcedFurnaceScreen(ReinforcedFurnaceScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        // change background width and height if not 176 x 166
        this.backgroundWidth = 175;
        this.backgroundHeight = 184;

        this.playerInventoryTitleY = 90;

    }

    // called when screen is created or redrawn
    @Override
    protected void init(){

        super.init();

        // re-centre title in case window resized
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;

    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {

        context.drawTexture(
                RenderLayer::getGuiTextured,
                TEXTURE,
                this.x, this.y,
                0, 0,
                this.backgroundWidth, this.backgroundHeight,
                256, 256
        );

        int fireHeight = MathHelper.ceil(handler.getBlockEntity().getFuelFloat() * 17);

        int arrowWidth = MathHelper.ceil(handler.getBlockEntity().getSmeltFloat() * 24);

        context.drawTexture(
                RenderLayer::getGuiTextured,
                TEXTURE,
                this.x + 50, this.y + 37 + 17 - fireHeight,
                175, 17-fireHeight,
                18, fireHeight,
                256, 256
        );

        context.drawTexture(
                RenderLayer::getGuiTextured,
                TEXTURE,
                this.x + 75, this.y + 38,
                175, 17,
                arrowWidth, 17,
                256, 256
        );

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

}
