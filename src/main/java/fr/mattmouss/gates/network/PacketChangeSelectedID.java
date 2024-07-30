package fr.mattmouss.gates.network;

import fr.mattmouss.gates.tileentity.CardGetterTileEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class PacketChangeSelectedID {
    private final BlockPos pos;
    private final int id;

    public PacketChangeSelectedID(FriendlyByteBuf buf){
        pos = buf.readBlockPos();
        id = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeBlockPos(pos);
        buf.writeInt(id);
    }

    public PacketChangeSelectedID(BlockPos pos_in,int sel_id){
        pos = pos_in;
        this.id = sel_id;
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            CardGetterTileEntity cgte = (CardGetterTileEntity) Objects.requireNonNull(context.get().getSender()).getLevel().getBlockEntity(pos);
            assert cgte != null;
            cgte.changeSelectedId(id);
        });
        context.get().setPacketHandled(true);
    }
}
