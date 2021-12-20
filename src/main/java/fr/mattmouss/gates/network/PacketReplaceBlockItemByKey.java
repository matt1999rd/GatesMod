package fr.mattmouss.gates.network;

import fr.mattmouss.gates.items.KeyItem;
import fr.mattmouss.gates.items.ModItem;
import fr.mattmouss.gates.items.TollKeyItem;
import fr.mattmouss.gates.items.TurnStileKeyItem;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketReplaceBlockItemByKey {
    private final BlockPos pos;
    private final boolean isTurnStile;
    private final UUID playerUUID;

    public PacketReplaceBlockItemByKey(PacketBuffer buffer){
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

    public void toBytes(PacketBuffer buffer){
        buffer.writeBoolean(isTurnStile);
        buffer.writeBlockPos(pos);
        buffer.writeLong(playerUUID.getLeastSignificantBits());
        buffer.writeLong(playerUUID.getMostSignificantBits());
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            World world = Objects.requireNonNull(context.get().getSender()).level;
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
            PlayerEntity entity = world.getPlayerByUUID(playerUUID);
            System.out.println("item stack of new key item : "+newStack);
            assert entity != null;
            Functions.moveMainOldStackToFreeSlot(entity);
            entity.setItemSlot(EquipmentSlotType.MAINHAND,ItemStack.EMPTY);
            entity.setItemSlot(EquipmentSlotType.MAINHAND,newStack);
        });
        context.get().setPacketHandled(true);
    }
}
