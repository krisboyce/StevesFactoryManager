package vswe.stevesfactory.utils;

import com.google.common.base.Preconditions;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import vswe.stevesfactory.api.StevesFactoryManagerAPI;
import vswe.stevesfactory.api.item.ItemBuffers;
import vswe.stevesfactory.api.logic.*;
import vswe.stevesfactory.api.network.IConnectable;
import vswe.stevesfactory.api.network.IConnectable.LinkType;
import vswe.stevesfactory.api.network.INetworkController;
import vswe.stevesfactory.logic.item.DirectBufferElement;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class NetworkHelper {

    private NetworkHelper() {
    }

    public static LinkType getLinkType(@Nullable TileEntity tile) {
        if (tile instanceof IConnectable) {
            return ((IConnectable) tile).getConnectionType();
        }
        return LinkType.DEFAULT;
    }

    public static <P extends IProcedure> P fabricateInstance(IProcedureType<P> type, INetworkController controller) {
        P procedure = type.createInstance(controller);
        CommandGraph graph = procedure.getGraph();
        controller.addCommandGraph(graph);
        return procedure;
    }

    public static IProcedure retrieveProcedureAndAdd(INetworkController controller, CompoundNBT tag) {
        IProcedure p = retrieveProcedure(new CommandGraph(controller), tag);
        controller.addCommandGraph(p.getGraph());
        return p;
    }

    public static IProcedure retrieveProcedure(CommandGraph graph, CompoundNBT tag) {
        IProcedure procedure = findTypeFor(tag).retrieveInstance(tag);
        procedure.setGraph(graph);
        return procedure;
    }

    public static IProcedureType<?> findTypeFor(CompoundNBT tag) {
        ResourceLocation id = new ResourceLocation(tag.getString("ID"));
        return findTypeFor(id);
    }

    public static IProcedureType<?> findTypeFor(ResourceLocation id) {
        IProcedureType<?> p = StevesFactoryManagerAPI.getProceduresRegistry().getValue(id);
        // Not using checkNotNull here because technically the above method returns null is a registry (game state) problem
        Preconditions.checkArgument(p != null, "Unable to find a procedure registered as " + id + "!");
        return p;
    }

    public static <P extends IProcedure> Function<INetworkController, P> wrapConstructor(Supplier<P> constructor) {
        return controller -> {
            P procedure = constructor.get();
            procedure.setGraph(new CommandGraph(controller, procedure));
            return procedure;
        };
    }

    public static ItemBuffers getOrCreateBufferContainer(Map<Item, ItemBuffers> buffers, Item item) {
        ItemBuffers container = buffers.get(item);
        if (container == null) {
            container = new ItemBuffers();
            buffers.put(item, container);
        }
        return container;
    }

    @Nullable
    public static DirectBufferElement getDirectBuffer(Map<Item, ItemBuffers> buffers, Item item) {
        ItemBuffers container = buffers.get(item);
        if (container == null) {
            return null;
        }
        return container.getBuffer(DirectBufferElement.class);
    }
}
