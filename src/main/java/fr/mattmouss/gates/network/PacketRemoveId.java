package fr.mattmouss.gates.network;

import fr.mattmouss.gates.energystorage.IdTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class PacketRemoveId {
    private final BlockPos pos;
    private final int id;

    public PacketRemoveId(BlockPos pos,int id){
        this.pos = pos;
        this.id = id;
    }

    public PacketRemoveId(FriendlyByteBuf buf){
        this.pos = buf.readBlockPos();
        this.id = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeBlockPos(pos);
        buf.writeInt(id);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            ServerLevel world = Objects.requireNonNull(context.get().getSender()).getLevel();
            IdTracker idTracker = world.getDataStorage().computeIfAbsent(IdTracker::new,IdTracker::new, "idgates");
            idTracker.removeId(id);
        });
    }
}
