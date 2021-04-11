package fr.mattmouss.gates.network;

import fr.mattmouss.gates.tileentity.TurnStileTileEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class movePlayerPacket {
    private final BlockPos te_pos;
    private final int player_id;
    private final boolean isAnimationInWork;
    private final boolean fromExit;

    public movePlayerPacket(BlockPos pos, ClientPlayerEntity player,boolean isAnimationInWork_in,boolean fromExit_in){
        te_pos = pos;
        player_id = player.getEntityId();
        isAnimationInWork = isAnimationInWork_in;
        fromExit=fromExit_in;
    }

    public movePlayerPacket(PacketBuffer buf){
        te_pos =buf.readBlockPos();
        player_id = buf.readInt();
        isAnimationInWork = buf.readBoolean();
        fromExit = buf.readBoolean();
    }

    public void toBytes(PacketBuffer buf){
        buf.writeBlockPos(te_pos);
        buf.writeInt(player_id);
        buf.writeBoolean(isAnimationInWork);
        buf.writeBoolean(fromExit);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            ServerWorld serverWorld = context.get().getSender().getServerWorld();
            TileEntity te = serverWorld.getTileEntity(te_pos);
            Entity entity =  serverWorld.getEntityByID(player_id);
            if (entity == null){
                System.out.println("no entity found with given id !!");
            }
            if (!(entity instanceof ServerPlayerEntity)){
                System.out.println("error of id !!! the given entity is "+entity.getClass());
                return;
            }
            ServerPlayerEntity player = (ServerPlayerEntity)entity;
            Direction facing = te.getBlockState().get(BlockStateProperties.HORIZONTAL_FACING);
            Vec2f rot =player.getPitchYaw();
            Direction offsetDirection=(fromExit)? facing : facing.getOpposite();
            BlockPos final_pos = (isAnimationInWork)? te_pos.offset(offsetDirection) : te_pos;
            player.moveToBlockPosAndAngles(final_pos,rot.y,rot.x);
        });
    }

}
