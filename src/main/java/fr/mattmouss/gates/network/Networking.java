package fr.mattmouss.gates.network;

import fr.mattmouss.gates.GatesMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

public class Networking {
    public static SimpleChannel INSTANCE;
    private static final String PROTOCOL_VERSION = "1";
    private static int ID =0;

    public static int nextID(){ return ID++; }

    public static void registerMessages(){
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(GatesMod.MOD_ID,"gates"),()->PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

        INSTANCE.registerMessage(nextID(),
                PacketLowerPrice.class,
                PacketLowerPrice::toBytes,
                PacketLowerPrice::new,
                PacketLowerPrice::handle);

        INSTANCE.registerMessage(nextID(),
                PacketRaisePrice.class,
                PacketRaisePrice::toBytes,
                PacketRaisePrice::new,
                PacketRaisePrice::handle);

        INSTANCE.registerMessage(nextID(),
                ChangeIdPacket.class,
                ChangeIdPacket::toBytes,
                ChangeIdPacket::new,
                ChangeIdPacket::handle);

        INSTANCE.registerMessage(nextID(),
                SetIdPacket.class,
                SetIdPacket::toBytes,
                SetIdPacket::new,
                SetIdPacket::handle);

        INSTANCE.registerMessage(nextID(),
                blockTSPacket.class,
                blockTSPacket::toBytes,
                blockTSPacket::new,
                blockTSPacket::handle);

        INSTANCE.registerMessage(nextID(),
                movePlayerPacket.class,
                movePlayerPacket::toBytes,
                movePlayerPacket::new,
                movePlayerPacket::handle);

        INSTANCE.registerMessage(nextID(),
                PutIdsToClientPacket.class,
                PutIdsToClientPacket::toBytes,
                PutIdsToClientPacket::new,
                PutIdsToClientPacket::handle);

        INSTANCE.registerMessage(nextID(),
                PacketChangeSelectedID.class,
                PacketChangeSelectedID::toBytes,
                PacketChangeSelectedID::new,
                PacketChangeSelectedID::handle);

        INSTANCE.registerMessage(nextID(),
                PacketMarkDirty.class,
                PacketMarkDirty::toBytes,
                PacketMarkDirty::new,
                PacketMarkDirty::handle);

        INSTANCE.registerMessage(nextID(),
                PacketReplaceBlockItemByKey.class,
                PacketReplaceBlockItemByKey::toBytes,
                PacketReplaceBlockItemByKey::new,
                PacketReplaceBlockItemByKey::handle);

        INSTANCE.registerMessage(nextID(),
                PacketRemoveId.class,
                PacketRemoveId::toBytes,
                PacketRemoveId::new,
                PacketRemoveId::handle);

        INSTANCE.registerMessage(nextID(),
                PacketGiveCard.class,
                PacketGiveCard::toBytes,
                PacketGiveCard::new,
                PacketGiveCard::handle);
    }





}
