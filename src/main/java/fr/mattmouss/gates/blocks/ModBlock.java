package fr.mattmouss.gates.blocks;


import fr.mattmouss.gates.doors.TollGate;
import fr.mattmouss.gates.doors.TurnStile;
import fr.mattmouss.gates.gui.CardGetterContainer;
import fr.mattmouss.gates.gui.TGTechContainer;
import fr.mattmouss.gates.gui.TGUserContainer;
import fr.mattmouss.gates.gui.TSContainer;
import fr.mattmouss.gates.tileentity.CardGetterTileEntity;
import fr.mattmouss.gates.tileentity.GarageTileEntity;
import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import fr.mattmouss.gates.tileentity.TurnStileTileEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class ModBlock {
    //enregistrement avec ObjectHolder des blocks qui ne sont pas des portes

    @ObjectHolder("gates:toll_gate")
    public static TollGate TOLL_GATE = new TollGate();

    @ObjectHolder("gates:toll_gate")
    public static TileEntityType<TollGateTileEntity> TOLL_GATE_ENTITY_TYPE;

    @ObjectHolder("gates:toll_gate")
    public static ContainerType<TGUserContainer> TOLLGATE_USER_CONTAINER;

    @ObjectHolder("gates:toll_gate_tech")
    public static ContainerType<TGTechContainer> TOLLGATE_TECH_CONTAINER;

    @ObjectHolder("gates:turn_stile")
    public static ContainerType<TSContainer> TURN_STILE_CONTAINER;

    @ObjectHolder("gates:card_getter")
    public static ContainerType<CardGetterContainer> CARD_GETTER_CONTAINER;

    @ObjectHolder("gates:garage_door")
    public static TileEntityType<GarageTileEntity> GARAGE_TILE_TYPE;

    @ObjectHolder("gates:turn_stile")
    public static TurnStile TURN_STILE = new TurnStile();

    @ObjectHolder("gates:turn_stile")
    public static TileEntityType<TurnStileTileEntity> TURNSTILE_TILE_TYPE;

    @ObjectHolder("gates:card_getter")
    public static CardGetter CARD_GETTER = new CardGetter();

    @ObjectHolder("gates:card_getter")
    public static TileEntityType<CardGetterTileEntity> CARD_GETTER_TILE_TYPE;


}
