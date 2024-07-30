package fr.mattmouss.gates.network;

import fr.mattmouss.gates.GatesMod;
import fr.mattmouss.gates.tileentity.CardGetterTileEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class PacketMarkDirty {
    private final BlockPos pos;
    private final boolean isDirty;

    public PacketMarkDirty(BlockPos pos,boolean isDirty){
        this.pos = pos;
        this.isDirty = isDirty;
    }
    public PacketMarkDirty(FriendlyByteBuf buf){
        this.pos = buf.readBlockPos();
        isDirty = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeBoolean(isDirty);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            Level world = Objects.requireNonNull(context.get().getSender()).level;
            CardGetterTileEntity cgte = (CardGetterTileEntity)world.getBlockEntity(pos);
            if (cgte == null) GatesMod.logger.warning("No more of this fucking function that are not working !! ");
            else cgte.markIdDirty(isDirty);
        });
        context.get().setPacketHandled(true);
    }
}
