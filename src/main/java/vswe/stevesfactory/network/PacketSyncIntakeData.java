package vswe.stevesfactory.network;

import com.google.common.base.Preconditions;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.NetworkEvent;
import vswe.stevesfactory.StevesFactoryManager;
import vswe.stevesfactory.blocks.ItemIntakeTileEntity;

import java.util.Objects;
import java.util.function.Supplier;

public final class PacketSyncIntakeData {

    public static void encode(PacketSyncIntakeData msg, PacketBuffer buf) {
        buf.writeResourceLocation(Objects.requireNonNull(msg.dimension.getRegistryName()));
        buf.writeBlockPos(msg.pos);
        buf.writeInt(msg.radius);
        buf.writeBoolean(msg.rendering);
    }

    public static PacketSyncIntakeData decode(PacketBuffer buf) {
        DimensionType dimension = DimensionType.byName(buf.readResourceLocation());
        BlockPos pos = buf.readBlockPos();
        int radius = buf.readInt();
        boolean rendering = buf.readBoolean();
        return new PacketSyncIntakeData(dimension, pos, radius, rendering);
    }

    public static void handle(PacketSyncIntakeData msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity sender = ctx.get().getSender();
            Preconditions.checkState(sender != null, "Invalid usage of a client to server packet");

            World world = sender.world;
            TileEntity tile = world.getTileEntity(msg.pos);
            if (tile instanceof ItemIntakeTileEntity) {
                ItemIntakeTileEntity intake = (ItemIntakeTileEntity) tile;
                intake.setRadius(msg.radius);
                intake.setRendering(msg.rendering);
            } else {
                StevesFactoryManager.logger.error("Received packet with invalid item intake position {}!", msg);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private DimensionType dimension;
    private BlockPos pos;

    private int radius;
    private boolean rendering;

    public PacketSyncIntakeData(DimensionType dimension, BlockPos pos, int radius, boolean rendering) {
        this.dimension = dimension;
        this.pos = pos;
        this.radius = radius;
        this.rendering = rendering;
    }
}
