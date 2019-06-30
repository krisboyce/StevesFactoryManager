package vswe.stevesfactory.library.gui.widget;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import vswe.stevesfactory.library.gui.core.*;
import vswe.stevesfactory.library.gui.widget.mixin.*;

import java.awt.*;
import java.util.List;
import java.util.*;

public class Box<T extends IWidget & RelocatableWidgetMixin> extends AbstractWidget implements IContainer<T>, RelocatableWidgetMixin, ResizableWidgetMixin, ContainerWidgetMixin<T> {

    private List<T> children = new ArrayList<>();
    private List<T> childrenView = Collections.unmodifiableList(children);

    private ILayout<T> layout;
    private boolean paused = false;

    public Box(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public Box(Point location, Dimension dimensions) {
        super(location, dimensions);
    }

    @Override
    public void onParentPositionChanged() {
        int oldAbsX = getAbsoluteX();
        int oldAbsY = getAbsoluteY();
        super.onParentPositionChanged();
        if (getAbsoluteX() != oldAbsX || getAbsoluteY() != oldAbsY) {
            for (T child : children) {
                child.onParentPositionChanged();
            }
        }
    }

    @Override
    public List<T> getChildren() {
        return childrenView;
    }

    @Override
    public Box<T> addChildren(T widget) {
        children.add(widget);
        widget.onParentChanged(this);
        reflow();
        return this;
    }

    @Override
    public Box<T> addChildren(Collection<T> widgets) {
        children.addAll(widgets);
        widgets.forEach(widget -> widget.onParentChanged(this));
        reflow();
        return this;
    }

    @Override
    public Box<T> addChildren(Iterable<T> widgets) {
        ContainerWidgetMixin.super.addChildren(widgets);
        return this;
    }

    @Override
    public Box<T> addChildren(Iterator<T> widgets) {
        ContainerWidgetMixin.super.addChildren(widgets);
        return this;
    }

    // In most cases these are not necessary because widget's position rely on the layout

    @CanIgnoreReturnValue
    public Box<T> updateChildLocation(T child, Point point) {
        child.setLocation(point);
        reflow();
        return this;
    }

    @CanIgnoreReturnValue
    public Box<T> updateChildLocation(T child, int x, int y) {
        child.setLocation(x, y);
        reflow();
        return this;
    }

    @Override
    public ILayout<T> getLayout() {
        return layout;
    }

    public void setLayout(ILayout<T> layout) {
        this.layout = layout;
        reflow();
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        for (T child : children) {
            child.render(mouseX, mouseY, particleTicks);
        }
    }

    /**
     * Cancel all reflow actions until {@link #unpause()} gets triggered. This should be used as a way to avoid unnecessary reflow (layout
     * updates) when changing widget properties in batch.d
     */
    @CanIgnoreReturnValue
    public Box<T> pause() {
        paused = true;
        return this;
    }

    @CanIgnoreReturnValue
    public Box<T> unpause() {
        paused = false;
        reflow();
        return this;
    }

    public boolean isPaused() {
        return paused;
    }

    @Override
    public void reflow() {
        if (!paused && layout != null) {
            getLayout().reflow(getDimensions(), children);
        }
    }

}
