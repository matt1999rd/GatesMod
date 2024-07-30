package fr.mattmouss.gates.network;

import fr.mattmouss.gates.GatesMod;
import fr.mattmouss.gates.tileentity.CardGetterTileEntity;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PutIdsToClientPacket {
    private final BlockPos pos;
    private final HashMap<Integer,Integer> ServerCostMap;

    public PutIdsToClientPacket(FriendlyByteBuf buf){
        pos = buf.readBlockPos();
        ServerCostMap = new HashMap<>();
        int[] id_and_price_array = buf.readVarIntArray();
        int size = id_and_price_array.length;
        if (size%2 != 0){
            throw new IllegalArgumentException("unable to read odd length array in buffer");
        }
        for (int i=0;i<size/2;i++){
            ServerCostMap.put(id_and_price_array[2*i],id_and_price_array[2*i+1]);
        }
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeBlockPos(pos);
        int size = ServerCostMap.size();
        int[] id_array = new int[2*size];
        AtomicInteger index = new AtomicInteger(0);
        ServerCostMap.forEach((id, price)->{
            id_array[index.get()] = id;
            id_array[index.incrementAndGet()] = price;
            index.incrementAndGet();
        });
        buf.writeVarIntArray(id_array);
    }

    public PutIdsToClientPacket(BlockPos pos_in,HashMap<Integer,Integer> costMap){
        pos = pos_in;
        this.ServerCostMap = costMap;
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        AtomicBoolean needToMarkDirty = new AtomicBoolean(false);
        context.get().enqueueWork(()->{
            CardGetterTileEntity cgte = (CardGetterTileEntity) GatesMod.proxy.getClientWorld().getBlockEntity(pos);
            if (cgte==null){
                needToMarkDirty.set(true);
            }
            assert cgte != null;
            HashMap<Integer,Integer> ClientCostMap = cgte.getIdPriceMap();
            //to remove id that were in tollgate or turn stile that has been destroyed
            //remove old id destroyed
            List<Integer> idToRemove =  ClientCostMap.keySet().stream().filter(id -> !ServerCostMap.containsKey(id)).collect(Collectors.toList());
            List<Map.Entry<Integer,Integer>> idWithNewCost = ClientCostMap.entrySet().stream().filter(entry -> Functions.isNonNullAndNotEqual(ServerCostMap.get(entry.getKey()),entry.getValue())).collect(Collectors.toList());
            idToRemove.forEach(cgte::removeId); //check if value has changed server side
            idWithNewCost.forEach(entry -> cgte.changeCost(entry.getKey(),entry.getValue())); //update price of an existing id

            //to add id that are in newly created tollgate or turn stile
            ServerCostMap.forEach((id,price)->{
                if (!ClientCostMap.containsKey(id)){
                    cgte.addIdAndCost(id,price);
                }
            });
            System.out.println("packet handled");
        });
        context.get().setPacketHandled(true);
        //sending of a packet dirty to server
        Networking.INSTANCE.sendToServer(new PacketMarkDirty(pos,needToMarkDirty.get()));

    }
}
