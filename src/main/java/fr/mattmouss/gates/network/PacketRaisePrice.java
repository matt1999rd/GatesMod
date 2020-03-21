package fr.mattmouss.gates.network;

import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class PacketRaisePrice {
    private final BlockPos pos;

    public PacketRaisePrice(PacketBuffer buf){
        pos = buf.readBlockPos();
    }

    public void toBytes(PacketBuffer buf){
        buf.writeBlockPos(pos);
    }

    public PacketRaisePrice(BlockPos pos_in){
        pos = pos_in;
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            TollGateTileEntity tgte = (TollGateTileEntity) context.get().getSender().getServerWorld().getTileEntity(pos);
            tgte.raisePrice();
        });
        context.get().setPacketHandled(true);
        context.get().setPacketHandled(true);
    }

}
