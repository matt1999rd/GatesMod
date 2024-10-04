package fr.moonshade.gates.network;

import fr.moonshade.gates.items.KeyItem;
import fr.moonshade.gates.items.ModItem;
import fr.moonshade.gates.items.TollKeyItem;
import fr.moonshade.gates.items.TurnStileKeyItem;
import fr.moonshade.gates.util.Functions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketReplaceBlockItemByKey {
    private final BlockPos pos;
    private final boolean isTurnStile;
    private final UUID playerUUID;

    public PacketReplaceBlockItemByKey(FriendlyByteBuf buffer){
        this.isTurnStile = buffer.readBoolean();
        this.pos = buffer.readBlockPos();
        long lsb = buffer.readLong();
        long msb = buffer.readLong();
        this.playerUUID = new UUID(msb,lsb);
    }

    public PacketReplaceBlockItemByKey(BlockPos pos, boolean isTurnStile, UUID uuid){
        this.isTurnStile = isTurnStile;
        this.pos = pos;
        this.playerUUID = uuid;
    }

    public void toBytes(FriendlyByteBuf buffer){
        buffer.writeBoolean(isTurnStile);
        buffer.writeBlockPos(pos);
        buffer.writeLong(playerUUID.getLeastSignificantBits());
        buffer.writeLong(playerUUID.getMostSignificantBits());
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            Level world = Objects.requireNonNull(context.get().getSender()).level;
            KeyItem key = (isTurnStile)?
                    (TurnStileKeyItem) ModItem.TURN_STILE_KEY.asItem() :
                    (TollKeyItem)      ModItem.TOLL_GATE_KEY.asItem();
            System.out.println("defined item : "+key);
            ItemStack newStack = new ItemStack(key);
            if (isTurnStile){
                key.setTSPosition(newStack,world,pos);
            }else {
                key.setTGPosition(newStack, world, pos);
            }
            Player entity = world.getPlayerByUUID(playerUUID);
            System.out.println("item stack of new key item : "+newStack);
            assert entity != null;
            Functions.moveMainOldStackToFreeSlot(entity);
            entity.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
            entity.setItemSlot(EquipmentSlot.MAINHAND,newStack);
        });
        context.get().setPacketHandled(true);
    }
}
