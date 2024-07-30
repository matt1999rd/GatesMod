package fr.mattmouss.gates.network;


import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import fr.mattmouss.gates.tileentity.TurnStileTileEntity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ChangeIdPacket {
    private final BlockPos pos;
    private final int key_id;


    public ChangeIdPacket(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        key_id = buf.readInt();
    }

    public ChangeIdPacket(BlockPos pos_in,int id){
        pos = pos_in;
        key_id = id;
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeBlockPos(pos);
        buf.writeInt(key_id);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        AtomicInteger val = new AtomicInteger(0);
        context.get().enqueueWork(()->{
            BlockEntity te = Objects.requireNonNull(context.get().getSender()).getLevel().getBlockEntity(pos);
            if (key_id == -1) {
                System.out.println("packet handled : no key found");
                if (te instanceof TollGateTileEntity) {
                    ((TollGateTileEntity) te).changeId();
                    val.set(((TollGateTileEntity) te).getId());
                } else if (te instanceof TurnStileTileEntity) {
                    ((TurnStileTileEntity) te).changeId();
                    val.set(((TurnStileTileEntity) te).getId());
                }
            }else {
                System.out.println("packet handled : a key found with id :"+key_id);
                if (te instanceof TollGateTileEntity) {
                    ((TollGateTileEntity) te).setId(key_id);
                    val.set(((TollGateTileEntity) te).getId());
                } else if (te instanceof TurnStileTileEntity) {
                    ((TurnStileTileEntity) te).setId(key_id);
                    val.set(((TurnStileTileEntity) te).getId());
                }
            }
        });
        Networking.INSTANCE.send(PacketDistributor.PLAYER.with(()-> context.get().getSender()),new SetIdPacket(pos,val.get()));
        context.get().setPacketHandled(true);
    }
}
