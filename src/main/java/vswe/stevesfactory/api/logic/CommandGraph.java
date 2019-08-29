package vswe.stevesfactory.api.logic;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.Constants;
import vswe.stevesfactory.api.network.INetworkController;
import vswe.stevesfactory.logic.execution.ProcedureExecutor;
import vswe.stevesfactory.utils.NetworkHelper;
import vswe.stevesfactory.utils.Utils;

import javax.annotation.Nonnull;
import java.util.*;

public class CommandGraph implements Iterable<IProcedure> {

    private INetworkController controller;
    private IProcedure root;

    public CommandGraph(INetworkController controller, IProcedure root) {
        this.controller = controller;
        this.root = root;
    }

    public CommandGraph(INetworkController controller) {
        this.controller = controller;
    }

    public CommandGraph(IProcedure root) {
        this.root = root;
    }

    public CommandGraph() {
    }

    public IProcedure getRoot() {
        return root;
    }

    public void setRoot(IProcedure root) {
        this.root = root;
    }

    public INetworkController getController() {
        return controller;
    }

    public void execute() {
        new ProcedureExecutor(controller, controller.getWorld()).start(root);
    }

    public Set<IProcedure> collect() {
        Set<IProcedure> result = new HashSet<>();
        if (root != null) {
            result.add(root);
            dfs(result, root);
        }
        return result;
    }

    @Nonnull
    @Override
    public Iterator<IProcedure> iterator() {
        return collect().iterator();
    }

    private Set<IProcedure> dfsCollectNoRoot() {
        Set<IProcedure> result = new HashSet<>();
        if (root != null) {
            dfs(result, root);
        }
        return result;
    }

    private void dfs(Set<IProcedure> result, IProcedure node) {
        for (Connection connection : node.successors()) {
            if (connection == null) {
                continue;
            }
            IProcedure next = connection.getDestination();
            if (result.contains(next)) {
                continue;
            }
            result.add(next);
            dfs(result, next);
        }
    }

    public CommandGraph inducedSubgraph(IProcedure node) {
        return new CommandGraph(controller, node);
    }

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();

        tag.putString("Dimension", Objects.requireNonNull(controller.getDimension().getRegistryName()).toString());
        tag.put("ControllerPos", NBTUtil.writeBlockPos(controller.getPos()));

        Set<IProcedure> nodes = dfsCollectNoRoot();
        Object2IntMap<IProcedure> idMap = createIDMap(nodes);
        ListNBT nodesNBT = new ListNBT();
        for (Object2IntMap.Entry<IProcedure> entry : idMap.object2IntEntrySet()) {
            IProcedure node = entry.getKey();
            int id = entry.getIntValue();
            nodesNBT.add(serializeNode(node, id, idMap));
        }

        ListNBT connectionsNBT = new ListNBT();
        Set<Connection> visited = new HashSet<>();
        for (IProcedure node : idMap.keySet()) {
            for (Connection successor : node.successors()) {
                if (successor != null && !visited.contains(successor)) {
                    visited.add(successor);

                    CompoundNBT nbt = new CompoundNBT();
                    nbt.putInt("From", idMap.getInt(successor.getSource()));
                    nbt.putInt("FromOut", successor.getSourceOutputIndex());
                    nbt.putInt("To", idMap.getInt(successor.getDestination()));
                    nbt.putInt("ToIn", successor.getDestinationInputIndex());
                    connectionsNBT.add(nbt);
                }
                // No need to iterate predecessors because all connections are referenced twice in both successors and predecessors
            }
        }

        // This is for compatibility reasons
        // If we ever need to make root not the first element, this might become useful
        tag.putInt("RootID", 0);
        tag.put("Nodes", nodesNBT);
        tag.put("Connections", connectionsNBT);

        return tag;
    }

    private Object2IntMap<IProcedure> createIDMap(Set<IProcedure> nodes) {
//        System.out.println(nodes);
        Object2IntMap<IProcedure> idMap = new Object2IntOpenHashMap<>(nodes.size() + 1);
        idMap.put(root, 0);
        for (IProcedure node : nodes) {
            idMap.put(node, idMap.size());
        }
        return idMap;
    }

    private CompoundNBT serializeNode(IProcedure node, int id, Object2IntMap<IProcedure> idMap) {
        CompoundNBT tag = new CompoundNBT();

//        IProcedure[] successors = node.successors();
//        int[] children = new int[successors.length];
//        for (int i = 0; i < successors.length; i++) {
//            IProcedure successor = successors[i];
//            if (successor == null) {
//                children[i] = -1;
//            } else {
//                int targetID = idMap.getOrDefault(successor, -1);
//                Validate.isTrue(targetID != -1);
//                // Input index is stored as the actualIndex + 1 to avoid dealing with complement bits
//                // On deserialization, impl should -1 from the index before use
//                int nextInputIndex = ArrayUtils.indexOf(successor.predecessors(), node);
//                children[i] = (nextInputIndex + 1) << 16 | targetID;
//            }
//        }

        tag.put("NodeData", node.serialize());
        tag.putInt("ID", id);
//        tag.putIntArray("Children", children);
        return tag;
    }

    public void deserialize(CompoundNBT tag) {
//        System.out.println(tag);
        DimensionType dimension = Objects.requireNonNull(DimensionType.byName(new ResourceLocation(tag.getString("Dimension"))));
        BlockPos pos = NBTUtil.readBlockPos(tag.getCompound("ControllerPos"));
        controller = (INetworkController) Utils.getWorldForSide(dimension).getTileEntity(pos);

        // First deserialize all the nodes themselves which makes it easier to set the connections
        ListNBT list = tag.getList("Nodes", Constants.NBT.TAG_COMPOUND);
        Int2ObjectMap<IProcedure> nodes = new Int2ObjectOpenHashMap<>();
//        Int2ObjectMap<CompoundNBT> nodesNBT = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT nodeNBT = list.getCompound(i);
//            int id = nodeNBT.getInt("ID");
//            nodesNBT.put(id, nodeNBT);
            nodes.put(nodeNBT.getInt("ID"), deserializeNode(nodeNBT));
        }


        ListNBT connectionNBT = tag.getList("Connections", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < connectionNBT.size(); i++) {
            CompoundNBT nbt = connectionNBT.getCompound(i);
            IProcedure from = nodes.get(nbt.getInt("From"));
            int fromOut = nbt.getInt("FromOut");
            IProcedure to = nodes.get(nbt.getInt("To"));
            int toIn = nbt.getInt("ToIn");
            Connection.create(from, fromOut, to, toIn);
        }
//        // Set child connection to all of the nodes
//        assert nodesNBT.size() == nodes.size();
//        for (int i = 0; i < nodes.size(); i++) {
//            IProcedure node = nodes.get(i);
//            CompoundNBT nodeNBT = nodesNBT.get(i);
//            retrieveConnections(nodes, node, nodeNBT);
//        }

        int rootID = tag.getInt("RootID");
        this.root = nodes.get(rootID);
    }

    private IProcedure deserializeNode(CompoundNBT nodeNBT) {
        return NetworkHelper.retrieveProcedure(this, nodeNBT.getCompound("NodeData"));
    }

//    private void retrieveConnections(Int2ObjectMap<IProcedure> nodes, IProcedure node, CompoundNBT nodeNBT) {
//        int[] children = nodeNBT.getIntArray("Children");
//        for (int i = 0; i < children.length; i++) {
//            int idData = children[i];
//            if (idData != -1) {
//                int key = idData & 0xffff;
//                IProcedure target = Validate.notNull(nodes.get(key));
//                // Index is stored in actualIndex + 1
//                // See serialization logic
//                int nextInputIndex = (idData >>> 16) - 1;
//                if (nextInputIndex != -1) {
//                    node.linkTo(i, target, nextInputIndex);
//                }
//            }
//        }
//    }

    public static CommandGraph deserializeFrom(CompoundNBT tag) {
        CommandGraph tree = new CommandGraph();
        tree.deserialize(tag);
        return tree;
    }
}
