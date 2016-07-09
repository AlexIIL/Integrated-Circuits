package moe.nightfall.vic.integratedcircuits.client.gui.component;

import java.util.Arrays;
import java.util.List;

import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IHoverable;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IHoverableHandler;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.misc.Vec2i;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiButtonExt;

public class GuiIconButton extends GuiButtonExt implements IHoverable {

	private ResourceLocation resource;
	private Vec2i size;
	private Vec2i pos;

	private boolean isToggleable;
	private boolean isUntoggleDisabled;
	private boolean isToggled;

	private String tooltip;

	public GuiIconButton(int id, int xPos, int yPos, int width, int height, ResourceLocation resource) {
		super(id, xPos, yPos, width, height, "");
		this.resource = resource;
		size = new Vec2i(width, height);
		pos = new Vec2i(0, 0);
	}

	public GuiIconButton setIcon(int xPos, int yPos) {
		pos = new Vec2i(xPos, yPos);
		return this;
	}

	public GuiIconButton setIcon(int xPos, int yPos, int width, int height) {
		size = new Vec2i(width, height);
		return setIcon(xPos, yPos);
	}

	public GuiIconButton setToggleable(boolean isToggleable) {
		return setToggleable(isToggleable, false);
	}

	public GuiIconButton setToggleable(boolean isToggleable, boolean isUntoggleDisabled) {
		this.isToggleable = isToggleable;
		this.isUntoggleDisabled = isUntoggleDisabled;
		return this;
	}

	public GuiIconButton setToggled(boolean isToggled) {
		this.isToggled = isToggled;
		return this;
	}

	public GuiIconButton setTooltip(String tooltip) {
		this.tooltip = tooltip;
		return this;
	}

	public boolean isToggled() {
		return isToggled;
	}

	@Override
	public int getHoverState(boolean hover) {
		if (isToggleable && isToggled())
			return 2;
		return super.getHoverState(hover);
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		super.drawButton(mc, mouseX, mouseY);
		if (field_146123_n && mc.currentScreen instanceof IHoverableHandler)
			((IHoverableHandler) mc.currentScreen).setCurrentItem(this);
		mc.renderEngine.bindTexture(resource);
		drawTexturedModalRect(xPosition, yPosition, pos.x, pos.y, size.x, size.y);
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mx, int my) {
		boolean pressed = super.mousePressed(mc, mx, my);
		if (pressed && enabled && isToggleable) {
			if (isToggled && isUntoggleDisabled)
				return false;
			return isToggled = !isToggled;
		}
		return pressed;
	}

	@Override
	public List<String> getHoverInformation() {
		if (tooltip != null)
			return Arrays.asList(MiscUtils.stringNewlineSplit(tooltip));
		return null;
	}
}
