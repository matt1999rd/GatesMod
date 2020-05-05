package fr.mattmouss.gates.network;

import fr.mattmouss.gates.GatesMod;
import fr.mattmouss.gates.tileentity.CardGetterTileEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import sun.nio.ch.Net;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class PutIdsToClientPacket {
    private final BlockPos pos;
    private final HashMap<Integer,Integer> ServerCostMap;

    //Pourquoi ce fucking packet ne s'envoie pas systèmatiquement et uniquement quand j'utilise le debugger ?
    public PutIdsToClientPacket(PacketBuffer buf){
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

    public void toBytes(PacketBuffer buf){
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
            CardGetterTileEntity cgte = (CardGetterTileEntity) GatesMod.proxy.getClientWorld().getTileEntity(pos);
            if (cgte==null){
                needToMarkDirty.set(true);
            }
            HashMap<Integer,Integer> ClientCostMap = cgte.getIdPriceMap();
            //to remove id that were in toll gate or turn stile that has been destroyed
            ClientCostMap.forEach((id,price)->{
                if (!ServerCostMap.containsKey(id)){
                    cgte.removeId(id);
                    //check if value has changed server side
                }else if (ServerCostMap.get(id) != price){
                    cgte.changeCost(id,price);
                }
            });
            //to add id that are in newly created toll gate or turn stile
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
