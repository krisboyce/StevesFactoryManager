package vswe.stevesfactory.logic.procedure;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import vswe.stevesfactory.api.logic.CommandGraph;
import vswe.stevesfactory.api.logic.IExecutionContext;
import vswe.stevesfactory.api.network.INetworkController;
import vswe.stevesfactory.logic.AbstractProcedure;
import vswe.stevesfactory.logic.Procedures;
import vswe.stevesfactory.logic.item.IItemFilter;
import vswe.stevesfactory.logic.item.ItemBufferElement;
import vswe.stevesfactory.logic.item.ItemTagFilter;
import vswe.stevesfactory.logic.item.ItemTraitsFilter;
import vswe.stevesfactory.ui.manager.editor.FlowComponent;
import vswe.stevesfactory.ui.manager.menu.DirectionSelectionMenu;
import vswe.stevesfactory.ui.manager.menu.InventorySelectionMenu;
import vswe.stevesfactory.utils.IOHelper;
import vswe.stevesfactory.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemExportProcedure extends AbstractProcedure implements IInventoryTarget, IDirectionTarget, IItemFilterTarget {

    public static final int INVENTORIES = 0;
    public static final int FILTER = 0;

    private List<BlockPos> inventories = new ArrayList<>();
    private List<Direction> directions = new ArrayList<>();
    private IItemFilter filter = new ItemTraitsFilter();

    public ItemExportProcedure(CommandGraph graph) {
        super(Procedures.ITEM_EXPORT.getFactory(), graph);
    }

    public ItemExportProcedure(INetworkController controller) {
        super(Procedures.ITEM_EXPORT.getFactory(), controller);
    }

    @Override
    public void execute(IExecutionContext context) {
        pushFrame(context, 0);

        Map<Item, ItemBufferElement> buffers = context.getItemBufferElements();
        IWorld world = context.getControllerWorld();
        for (BlockPos pos : inventories) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile == null) {
                continue;
            }

            for (Direction direction : directions) {
                LazyOptional<IItemHandler> cap = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction);
                if (cap.isPresent()) {
                    IItemHandler handler = cap.orElseThrow(RuntimeException::new);
                    for (Map.Entry<Item, ItemBufferElement> entry : buffers.entrySet()) {
                        ItemBufferElement buffer = entry.getValue();
                        if (!filter.test(buffer.stack)) {
                            continue;
                        }
                        if (buffer.stack.isEmpty()) {
                            continue;
                        }

                        // Simulate limit input stack size
                        int need = calculateNeededAmount(handler, buffer.stack);
                        if (need == 0) {
                            continue;
                        }
                        int sourceCount = buffer.stack.getCount();
                        buffer.stack.setCount(need);

                        ItemStack untaken = ItemHandlerHelper.insertItem(handler, buffer.stack, false);
                        int untakenCount = untaken.getCount();
                        int takenCount = need - untakenCount;

                        buffer.used += takenCount;
                        buffer.stack = untaken;
                        buffer.stack.setCount(sourceCount - takenCount);
                    }
                }
            }
        }
    }

    private int calculateNeededAmount(IItemHandler handler, ItemStack source) {
        if (!this.filter.isMatchingAmount()) {
            return source.getCount();
        }
        int totalCount = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (source.isItemEqual(stack)) {
                totalCount += stack.getCount();
            }
        }

        // TODO generalize this operation (move to interface)
        if (filter instanceof ItemTraitsFilter) {
            ItemTraitsFilter filter = (ItemTraitsFilter) this.filter;
            int stackLimit = Integer.MAX_VALUE;
            for (int i = 0; i < filter.getItems().size(); i++) {
                if (filter.isEqual(i, source)) {
                    stackLimit = filter.getItems().get(i).getCount();
                    break;
                }
            }
            return Utils.lowerBound(stackLimit - totalCount, 0);
        } else if (filter instanceof ItemTagFilter) {
            return Utils.lowerBound(((ItemTagFilter) filter).stackLimit - totalCount, 0);
        }
        return 0;
    }

    @Override
    public FlowComponent<ItemExportProcedure> createFlowComponent() {
        FlowComponent<ItemExportProcedure> f = FlowComponent.of(this);
        f.addMenu(new InventorySelectionMenu<>(INVENTORIES));
        f.addMenu(new DirectionSelectionMenu<>(INVENTORIES));
        IItemFilterTarget.createFilterMenu(this, f, FILTER);
        return f;
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = super.serialize();
        tag.put("Inventories", IOHelper.writeBlockPoses(inventories));
        tag.putIntArray("Directions", IOHelper.direction2Index(directions));
        tag.put("Filter", IOHelper.writeItemFilter(filter));
        return tag;
    }

    @Override
    public void deserialize(CompoundNBT tag) {
        super.deserialize(tag);
        inventories = IOHelper.readBlockPoses(tag.getList("Inventories", Constants.NBT.TAG_COMPOUND), new ArrayList<>());
        directions = IOHelper.index2Direction(tag.getIntArray("Directions"));
        filter = IOHelper.readItemFilter(tag.getCompound("Filter"));
    }

    @Override
    public List<Direction> getDirections(int id) {
        return directions;
    }

    @Override
    public List<BlockPos> getInventories(int id) {
        return inventories;
    }

    @Override
    public IItemFilter getFilter(int id) {
        return filter;
    }

    @Override
    public void setFilter(int id, IItemFilter filter) {
        if (id == FILTER) {
            this.filter = filter;
        }
    }
}