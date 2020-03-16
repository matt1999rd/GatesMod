package fr.mattmouss.gates.tileentity;

import fr.mattmouss.gates.doors.ModBlock;
import fr.mattmouss.gates.doors.TurnStile;
import fr.mattmouss.gates.energystorage.IdStorage;
import fr.mattmouss.gates.enum_door.TurnSPosition;
import fr.mattmouss.gates.items.CardKeyItem;
import fr.mattmouss.gates.items.ModItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import sun.security.timestamp.TSRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TurnStileTileEntity extends TileEntity implements IControlIdTE {

    public TurnStileTileEntity() {
        super(ModBlock.TURNSTILE_TILE_TYPE);
    }

    private LazyOptional<IEnergyStorage> id = LazyOptional.of(this::getIdValue).cast();

    public List<BlockPos> getPositionOfBlockConnected() {
        List<BlockPos> posList = new ArrayList<>();
        TurnSPosition tsp = this.getBlockState().get(TurnStile.TS_POSITION);
        Direction direction = this.getBlockState().get(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos main_pos = getMainPos(tsp,direction);
        posList.add(main_pos);
        posList.add(main_pos.offset(direction.rotateY()));
        posList.add(main_pos.offset(direction.rotateYCCW()));
        return posList;
    }

    public BlockPos getMainPos(TurnSPosition tsp, Direction direction){
        switch (tsp.getMeta()){
            case 0:
                return pos;
            case 1:
                return pos.offset(direction.rotateY());
            case 2:
                return pos.offset(direction.rotateYCCW());
            default:
                throw new IllegalArgumentException("unknown meta value for tsp :"+tsp.getMeta());
        }
    }
    //this function will check if the player is handling the card to get into
    public boolean checkPlayer(PlayerEntity entity) {
        ItemStack stack = entity.getHeldItem(Hand.MAIN_HAND);
        if (stack.getItem() != ModItem.CARD_KEY){
            return false;
        }
        CardKeyItem key = (CardKeyItem) stack.getItem();
        //verification de l'enregistrement d'une blockPos
        if (key.getTSPosition(stack,null) == null){
            return false;
        }
        if (key.getTSPosition(stack,null).equals(pos)){
            System.out.println("same pos registered : true !");
            return true;
        }
        System.out.println("not the same pos ! te pos :"+pos+ "' registeredPos :" +key.getTSPosition(stack,world));
        return false;
    }

    public void changeAllAnim() {
        List<BlockPos> posList = getPositionOfBlockConnected();
        for (BlockPos pos1 : posList){
            TurnStileTileEntity tste = (TurnStileTileEntity) world.getTileEntity(pos1);
            tste.changeAnim();
        }
    }

    public void changeAnim(){
        BlockState state = this.getBlockState();
        int i = state.get(TurnStile.ANIMATION);
        world.setBlockState(pos,state.with(TurnStile.ANIMATION,1-i));
    }

    @Override
    public int getId() {
        AtomicInteger id_in = new AtomicInteger(1);
        id.ifPresent(e->{
            id_in.set(e.getEnergyStored());
        });
        return id_in.get();
    }

    @Override
    public void changeId() {
        id.ifPresent(e->{
            ((IdStorage)e).changeId(world.getServer().getWorld(DimensionType.OVERWORLD));
        });
    }

    @Override
    public IEnergyStorage getIdValue() {
        return new IdStorage();
    }

    @Override
    public void setId(int id_in) {
        id.ifPresent(e->{
            ((IdStorage)e).changeId(id_in);
        });
    }

}
