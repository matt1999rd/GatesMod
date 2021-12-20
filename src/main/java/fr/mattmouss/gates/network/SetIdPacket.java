package fr.mattmouss.gates.network;

import fr.mattmouss.gates.GatesMod;
import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import fr.mattmouss.gates.tileentity.TurnStileTileEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SetIdPacket {
    private final BlockPos pos;
    private final int server_id;


    public SetIdPacket(PacketBuffer buf) {
        pos = buf.readBlockPos();
        server_id = buf.readInt();
    }

    public SetIdPacket(BlockPos pos_in,int server_id_in){
        pos = pos_in;
        server_id = server_id_in;
    }

    public void toBytes(PacketBuffer buf){
        buf.writeBlockPos(pos);
        buf.writeInt(server_id);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            TileEntity te =GatesMod.proxy.getClientWorld().getBlockEntity(pos);
            System.out.println("packet handled");
            if (te instanceof TollGateTileEntity){
                ((TollGateTileEntity)te).setId(server_id);
            }else if (te instanceof TurnStileTileEntity){
                ((TurnStileTileEntity)te).setId(server_id);
            }
        });
        context.get().setPacketHandled(true);
    }
}
