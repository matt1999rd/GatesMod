package fr.moonshade.gates.blocks;


import fr.moonshade.gates.doors.*;
import fr.moonshade.gates.gui.*;
import fr.moonshade.gates.tileentity.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class ModBlock {
    //registry with ObjectHolder of all blocks that are not doors

    @ObjectHolder("gates:toll_gate")
    public static TollGate TOLL_GATE ;

    @ObjectHolder("gates:redstone_toll_gate")
    public static RedstoneTollGate REDSTONE_TOLL_GATE;

    @ObjectHolder("gates:toll_gate")
    public static BlockEntityType<TollGateTileEntity> TOLL_GATE_ENTITY_TYPE;

    @ObjectHolder("gates:redstone_toll_gate")
    public static BlockEntityType<TollGateTileEntity> REDSTONE_TOLL_GATE_ENTITY_TYPE;

    @ObjectHolder("gates:toll_gate")
    public static MenuType<TGUserContainer> TOLLGATE_USER_CONTAINER;

    @ObjectHolder("gates:toll_gate_tech")
    public static MenuType<TGTechContainer> TOLLGATE_TECH_CONTAINER;

    @ObjectHolder("gates:turn_stile")
    public static MenuType<TSContainer> TURN_STILE_CONTAINER;

    @ObjectHolder("gates:card_getter")
    public static MenuType<CardGetterContainer> CARD_GETTER_CONTAINER;

    @ObjectHolder("gates:card_getter_choice")
    public static MenuType<CardGetterChoiceContainer> CARD_GETTER_CHOICE_CONTAINER;

    @ObjectHolder("gates:garage_door")
    public static BlockEntityType<GarageTileEntity> GARAGE_TILE_TYPE;

    @ObjectHolder("gates:turn_stile")
    public static TurnStile TURN_STILE;

    @ObjectHolder("gates:redstone_turn_stile")
    public static RedstoneTurnStile REDSTONE_TURN_STILE ;

    @ObjectHolder("gates:turn_stile")
    public static BlockEntityType<TurnStileTileEntity> TURNSTILE_TILE_TYPE;

    @ObjectHolder("gates:redstone_turn_stile")
    public static BlockEntityType<RedstoneTurnStileTileEntity> REDSTONE_TURNSTILE_TILE_TYPE;

    @ObjectHolder("gates:card_getter")
    public static CardGetter CARD_GETTER ;

    @ObjectHolder("gates:card_getter")
    public static BlockEntityType<CardGetterTileEntity> CARD_GETTER_TILE_TYPE;

    @ObjectHolder("gates:window_door")
    public static WindowDoor WINDOW_DOOR ;

    @ObjectHolder("gates:window_door")
    public static BlockEntityType<WindowDoorTileEntity> WINDOW_DOOR_TILE_TYPE;

    @ObjectHolder("gates:draw_bridge")
    public static BlockEntityType<DrawBridgeTileEntity> DRAW_BRIDGE_TILE_TYPE;



}
