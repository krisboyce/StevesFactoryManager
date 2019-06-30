package vswe.stevesfactory.blocks.manager.components;

import com.google.common.collect.ImmutableList;
import vswe.stevesfactory.blocks.manager.FactoryManagerGUI;
import vswe.stevesfactory.library.gui.core.*;

import java.util.Collection;
import java.util.List;

public class EditorPanel extends DynamicWidthWidget<IWidget> {

    public EditorPanel(FactoryManagerGUI.TopLevelWidget parent, IWindow window) {
        super(parent, window, WidthOccupingType.MAX_WIDTH);
    }

    // TODO
    @Override
    public List<IWidget> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public ILayout<IWidget> getLayout() {
        return null;
    }

    @Override
    public IContainer<IWidget> addChildren(IWidget widget) {
        return null;
    }

    @Override
    public IContainer<IWidget> addChildren(Collection<IWidget> widgets) {
        return null;
    }

}
