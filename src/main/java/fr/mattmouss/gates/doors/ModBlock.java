package fr.mattmouss.gates.doors;


import fr.mattmouss.gates.gui.TGTechContainer;
import fr.mattmouss.gates.gui.TGUserContainer;
import fr.mattmouss.gates.tileentity.GarageTileEntity;
import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.registries.ObjectHolder;

import java.util.List;

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

    @ObjectHolder("gates:garage_door")
    public static TileEntityType<GarageTileEntity> GARAGE_TILE_TYPE;




}
