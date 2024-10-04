package fr.moonshade.gates.network;

import fr.moonshade.gates.GatesMod;
import fr.moonshade.gates.tileentity.TollGateTileEntity;
import fr.moonshade.gates.tileentity.TurnStileTileEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetIdPacket {
    private final BlockPos pos;
    private final int server_id;


    public SetIdPacket(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        server_id = buf.readInt();
    }

    public SetIdPacket(BlockPos pos_in,int server_id_in){
        pos = pos_in;
        server_id = server_id_in;
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeBlockPos(pos);
        buf.writeInt(server_id);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            BlockEntity te =GatesMod.proxy.getClientWorld().getBlockEntity(pos);
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
