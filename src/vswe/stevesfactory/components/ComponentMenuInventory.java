package vswe.stevesfactory.components;


import vswe.stevesfactory.blocks.ConnectionBlockType;

import java.util.List;

public class ComponentMenuInventory extends ComponentMenuContainer {
    public ComponentMenuInventory(FlowComponent parent) {
        super(parent, ConnectionBlockType.INVENTORY);
    }

    @Override
    public String getName() {
        return "Inventories";
    }

    @Override
    public void addErrors(List<String> errors) {
        if (selectedInventories.isEmpty()) {
            errors.add("No inventory selected");
        }
    }
}