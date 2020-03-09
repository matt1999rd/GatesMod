package fr.mattmouss.gates.network;

import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.NetworkEvent;
import org.lwjgl.system.CallbackI;

import java.util.function.Supplier;

public class PacketLowerPrice {
    private final BlockPos pos;
    private final DimensionType type;

    public PacketLowerPrice(PacketBuffer buf){
        type = DimensionType.getById(buf.readInt());
        pos = buf.readBlockPos();
    }

    public void toBytes(PacketBuffer buf){
        buf.writeInt(type.getId());
        buf.writeBlockPos(pos);
    }

    public PacketLowerPrice(DimensionType type_in,BlockPos pos_in){
        type = type_in;
        pos = pos_in;
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            TollGateTileEntity tgte = (TollGateTileEntity) context.get().getSender().world.getServer().getWorld(type).getTileEntity(pos);
            tgte.lowerPrice();
        });
        context.get().setPacketHandled(true);
    }



}
