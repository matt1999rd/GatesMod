package fr.mattmouss.gates.network;

import fr.mattmouss.gates.tileentity.CardGetterTileEntity;
import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketChangeSelectedID {
    private final BlockPos pos;
    private final int id;

    public PacketChangeSelectedID(PacketBuffer buf){
        pos = buf.readBlockPos();
        id = buf.readInt();
    }

    public void toBytes(PacketBuffer buf){
        buf.writeBlockPos(pos);
        buf.writeInt(id);
    }

    public PacketChangeSelectedID(BlockPos pos_in,int sel_id){
        pos = pos_in;
        this.id = sel_id;
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            CardGetterTileEntity cgte = (CardGetterTileEntity) context.get().getSender().getServerWorld().getTileEntity(pos);
            cgte.changeSelectedId(id);
        });
        context.get().setPacketHandled(true);
    }
}
