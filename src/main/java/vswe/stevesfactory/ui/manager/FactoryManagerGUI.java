package vswe.stevesfactory.ui.manager;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import vswe.stevesfactory.library.gui.IWidget;
import vswe.stevesfactory.library.gui.IWindow;
import vswe.stevesfactory.library.gui.background.DisplayListCaches;
import vswe.stevesfactory.library.gui.debug.RenderEventDispatcher;
import vswe.stevesfactory.library.gui.layout.StrictTableLayout;
import vswe.stevesfactory.library.gui.layout.StrictTableLayout.GrowDirection;
import vswe.stevesfactory.library.gui.screen.WidgetScreen;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.library.gui.window.mixin.NestedEventHandlerMixin;
import vswe.stevesfactory.ui.manager.components.DynamicWidthWidget;
import vswe.stevesfactory.ui.manager.components.EditorPanel;
import vswe.stevesfactory.ui.manager.selection.SelectionPanel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

public class FactoryManagerGUI extends WidgetScreen {

    public static final StrictTableLayout DOWN_RIGHT_4_STRICT_TABLE = new StrictTableLayout(GrowDirection.DOWN, GrowDirection.RIGHT, 4);

    ///////////////////////////////////////////////////////////////////////////
    // GUI code
    ///////////////////////////////////////////////////////////////////////////

    public static final float WIDTH_PROPORTION = 2F / 3F;
    public static final float HEIGHT_PROPORTION = 3F / 4F;

    private BlockPos controllerPos;

    public FactoryManagerGUI(BlockPos controllerPos) {
        super(new TranslationTextComponent("gui.sfm.factoryManager.title"));
        this.controllerPos = controllerPos;
    }

    @Override
    protected void init() {
        super.init();
        initializePrimaryWindow(new PrimaryWindow());
    }

    @Override
    public void resize(@Nonnull Minecraft mc, int newWidth, int newHeight) {
        super.resize(mc, newWidth, newHeight);
        getPrimaryWindow().updateScreenBounds();
    }

    @Override
    public PrimaryWindow getPrimaryWindow() {
        return (PrimaryWindow) super.getPrimaryWindow();
    }

    public static class PrimaryWindow implements IWindow, NestedEventHandlerMixin {

        // The location and dimensions will be set in the constructor
        private Rectangle screenBounds = new Rectangle();
        private Dimension dimensions = new Dimension();
        private Dimension contentDimensions = new Dimension();

        private int backgroundDL;

        private TopLevelWidget topLevel;
        private IWidget focusedWidget;

        private PrimaryWindow() {
            this.updateScreenBounds();
            // Make sure the width/height are initialized before we touch the children
            this.topLevel = new TopLevelWidget(this);
            this.focusedWidget = topLevel;
        }

        @Override
        public Dimension getBorder() {
            return dimensions;
        }

        @Override
        public int getBorderSize() {
            return 4;
        }

        @Override
        public Dimension getContentDimensions() {
            return contentDimensions;
        }

        @Override
        public List<? extends IWidget> getChildren() {
            return ImmutableList.of(topLevel);
        }

        @Override
        public Point getPosition() {
            return screenBounds.getLocation();
        }

        @Override
        public void render(int mouseX, int mouseY, float particleTicks) {
            RenderEventDispatcher.onPreRender(this, mouseX, mouseY);
            GlStateManager.callList(backgroundDL);
            topLevel.render(mouseX, mouseY, particleTicks);
            RenderEventDispatcher.onPostRender(this, mouseX, mouseY);
        }

        @Nonnull
        @Override
        public IWidget getFocusedWidget() {
            return focusedWidget;
        }

        @Override
        public void setFocusedWidget(@Nullable IWidget widget) {
            if (focusedWidget != widget) {
                focusedWidget.onFocusChanged(false);
                focusedWidget = MoreObjects.firstNonNull(widget, topLevel);
                focusedWidget.onFocusChanged(true);
            }
        }

        public Rectangle getScreenBounds() {
            return screenBounds;
        }

        public void setScreenBounds(int width, int height) {
            // Borders
            this.screenBounds.width = width;
            this.screenBounds.height = height;
            // Centering
            this.screenBounds.x = scaledWidth() / 2 - width / 2;
            this.screenBounds.y = scaledHeight() / 2 - height / 2;

            this.dimensions.width = width;
            this.dimensions.height = height;
            this.contentDimensions.width = width - 4 * 2;
            this.contentDimensions.height = height - 4 * 2;

            updateBackgroundDL();
        }

        private void updateBackgroundDL() {
            backgroundDL = DisplayListCaches.createVanillaStyleBackground(new Rectangle(screenBounds), 0F);
        }

        private void updateScreenBounds() {
            int width = (int) (scaledWidth() * WIDTH_PROPORTION);
            int height = (int) (scaledHeight() * HEIGHT_PROPORTION);
            setScreenBounds(width, height);
        }
    }

    public static class TopLevelWidget extends AbstractContainer<DynamicWidthWidget<?>> {

        public final SelectionPanel selectionPanel;
        public final EditorPanel editorPanel;
        private final ImmutableList<DynamicWidthWidget<?>> children;

        private TopLevelWidget(PrimaryWindow window) {
            super(window);
            this.selectionPanel = new SelectionPanel();
            this.editorPanel = new EditorPanel();
            this.children = ImmutableList.of(selectionPanel, editorPanel);
            System.out.println("init window");
            // FIXME propagating parent reference
            this.setParentWidget(this);
            this.reflow();
        }

        @Nonnull
        @Override
        public IWidget getParentWidget() {
            return this;
        }

        @Override
        public void setParentWidget(IWidget newParent) {
            Preconditions.checkArgument(newParent == this);
            super.setParentWidget(newParent);
        }

        @Override
        public void onParentPositionChanged() {
            // Do nothing because we don't really have a parent widget
        }

        @Override
        public List<DynamicWidthWidget<?>> getChildren() {
            return children;
        }

        @Override
        public void render(int mouseX, int mouseY, float particleTicks) {
            // No render events for this object because it is technically internal for the window, and it has the exact size as the window
            selectionPanel.render(mouseX, mouseY, particleTicks);
            editorPanel.render(mouseX, mouseY, particleTicks);
        }

        @Override
        public void reflow() {
            DynamicWidthWidget.reflowDynamicWidth(getDimensions(), children);
            selectionPanel.reflow();
            editorPanel.reflow();
        }
    }
}
