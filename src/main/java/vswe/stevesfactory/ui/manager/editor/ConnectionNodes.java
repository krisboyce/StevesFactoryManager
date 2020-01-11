package vswe.stevesfactory.ui.manager.editor;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import vswe.stevesfactory.api.logic.Connection;
import vswe.stevesfactory.api.logic.IProcedure;
import vswe.stevesfactory.library.gui.widget.AbstractContainer;
import vswe.stevesfactory.library.gui.widget.mixin.ResizableWidgetMixin;
import vswe.stevesfactory.ui.manager.FactoryManagerGUI;

import java.awt.*;
import java.util.Map;
import java.util.function.Function;

public abstract class ConnectionNodes<N extends INode> extends AbstractContainer<N> implements ResizableWidgetMixin {

    public static ConnectionNodes<EndNode> inputNodes(int amount) {
        return new ConnectionNodes<EndNode>(amount, EndNode::new) {
            @Override
            public void readConnections(Map<IProcedure, FlowComponent<?>> m, IProcedure procedure) {
                // Only connect on output nodes
            }
        };
    }

    public static ConnectionNodes<StartNode> outputNodes(int amount) {
        return new ConnectionNodes<StartNode>(amount, StartNode::new) {
            @Override
            public void readConnections(Map<IProcedure, FlowComponent<?>> m, IProcedure procedure) {
                initializing = true;
                ImmutableList<StartNode> nodes = this.getChildren();
                Connection[] successors = procedure.successors();
                Preconditions.checkState(successors.length == nodes.size());

                for (int i = 0; i < successors.length; i++) {
                    Connection connection = successors[i];
                    if (connection == null) {
                        continue;
                    }
                    IProcedure successor = connection.getDestination();
                    FlowComponent<?> other = m.get(successor);
                    StartNode start = nodes.get(i);
                    EndNode end = other.getInputNodes().nodes.get(connection.getDestinationInputIndex());

                    INode[] allNodes = new INode[1 + connection.getPolylineNodes().size() + 1];
                    allNodes[0] = start;
                    allNodes[allNodes.length - 1] = end;
                    // Generate the intermediate nodes
                    int j = 1;
                    for (Point pos : connection.getPolylineNodes()) {
                        allNodes[j] = new IntermediateNode();
                        allNodes[j].setLocation(pos);
                        FactoryManagerGUI.getActiveGUI().getTopLevel().connectionsPanel.addChildren(allNodes[j]);
                        j++;
                    }
                    // Connect the intermediate nodes
                    for (int k = 0; k < allNodes.length - 1; k++) {
                        INode from = allNodes[k];
                        INode to = allNodes[k + 1];
                        ConnectionsPanel.connect(from, to);
                    }
                }
                initializing = false;
            }
        };
    }

    private final ImmutableList<N> nodes;
    protected boolean initializing = false;

    public ConnectionNodes(int amountNodes, Function<Integer, N> factory) {
        super(0, 0, 0, ConnectionsPanel.REGULAR_HEIGHT);
        ImmutableList.Builder<N> nodes = ImmutableList.builder();
        for (int i = 0; i < amountNodes; i++) {
            nodes.add(factory.apply(i));
        }
        this.nodes = nodes.build();
    }

    private static void removeNode(INode node) {
        ConnectionsPanel panel = FactoryManagerGUI.getActiveGUI().getTopLevel().connectionsPanel;
        panel.removeChildren(node);
    }

    @Override
    public void reflow() {
        int segments = nodes.size() + 1;
        int emptyWidth = getWidth() - nodes.size() * ConnectionsPanel.REGULAR_WIDTH;
        int segmentsWidth = emptyWidth / segments;

        int x = segmentsWidth;
        for (N node : nodes) {
            node.setX(x);
            x += node.getWidth() + segmentsWidth;
        }
    }

    @Override
    public ImmutableList<N> getChildren() {
        return nodes;
    }

    public abstract void readConnections(Map<IProcedure, FlowComponent<?>> m, IProcedure procedure);

    public void removeAllConnections() {
        for (N node : nodes) {
            // TODO no hard coding
            if (node instanceof StartNode) {
                ConnectionsPanel.removeConnection((StartNode) node);
            } else {
                ConnectionsPanel.removeConnection((EndNode) node);
            }
        }
    }
}
