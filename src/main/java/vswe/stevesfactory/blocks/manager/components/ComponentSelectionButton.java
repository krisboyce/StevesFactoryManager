package vswe.stevesfactory.blocks.manager.components;

import net.minecraft.util.ResourceLocation;
import vswe.stevesfactory.StevesFactoryManager;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.widget.AbstractWidget;
import vswe.stevesfactory.library.gui.widget.mixin.LeafWidgetMixin;
import vswe.stevesfactory.library.gui.widget.mixin.RelocatableWidgetMixin;
import vswe.stevesfactory.utils.RenderingHelper;

public class ComponentSelectionButton extends AbstractWidget implements RelocatableWidgetMixin, LeafWidgetMixin {

    public enum Components {
        TRIGGER("trigger.png"),
        ITEM_IMPORT("item_import.png"),
        ITEM_EXPORT("item_export.png"),
        ITEM_CONDITION("item_condition.png"),
        FLOW_CONTROL("flow_control.png"),
        FLUID_IMPORT("fluid_import"),
        FLUID_EXPORT("fluid_export"),
        FLUID_CONDITION("fluid_condition"),
        REDSTONE_EMITTER("redstone_emitter"),
        REDSTONE_CONDITION("redstone_condition"),
        CRAFT_ITEM("craft_item"),
        FOR_EACH("for_each"),
        GROUP("group"),
        GROUP_IO("group_io"),
        CAMOUFLAGE("camouflage"),
        SIGN_UPDATER("sign_updater"),
        CONFIGURATIONS("configurations");

        public final String fileName;
        public final ResourceLocation texture;

        Components(String fileName) {
            this.fileName = fileName;
            this.texture = new ResourceLocation(StevesFactoryManager.MODID, "textures/gui/component_icon/" + fileName);
        }
    }

    private static final ResourceLocation BACKGROUND_NORMAL = new ResourceLocation(StevesFactoryManager.MODID, "textures/gui/component_background/background_normal.png");
    private static final ResourceLocation BACKGROUND_HOVERED = new ResourceLocation(StevesFactoryManager.MODID, "textures/gui/component_background/background_hovered.png");

    public static final int WIDTH = 16;
    public static final int HEIGHT = 16;

    private final ResourceLocation texture;

    public ComponentSelectionButton(SelectionPanel parent, Components component) {
        this(parent, component.texture);
    }

    public ComponentSelectionButton(SelectionPanel parent, ResourceLocation texture) {
        super(0, 0, WIDTH, HEIGHT);
        onWindowChanged(parent.getWindow(), parent);
        this.texture = texture;
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
        int x1 = getAbsoluteX();
        int y1 = getAbsoluteY();
        int x2 = getAbsoluteXBR();
        int y2 = getAbsoluteYBR();
        if (isInside(mouseX, mouseY)) {
            RenderingHelper.drawCompleteTexture(x1, y1, x2, y2, BACKGROUND_HOVERED);
        } else {
            RenderingHelper.drawCompleteTexture(x1, y1, x2, y2, BACKGROUND_NORMAL);
        }
        RenderingHelper.drawCompleteTexture(x1, y1, x2, y2, texture);
        RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
    }

}