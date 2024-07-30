package fr.mattmouss.gates.doors;

import com.google.common.collect.Lists;
import fr.mattmouss.gates.enum_door.DoorPlacing;
import fr.mattmouss.gates.voxels.VoxelDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;


import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class GardenDoor extends MultipleBlockDoor {

    public static EnumProperty<DoorPlacing> PLACING = EnumProperty.create("position",DoorPlacing.class,placing -> placing.isSide() && !placing.isCenterY());


    public GardenDoor(String name) {
        super(Properties.of(Material.METAL).noOcclusion());
        this.setRegistryName(name);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        if (!state.getValue(PLACING).isSide() ||state.getValue(PLACING).isCenterY())return Shapes.empty();
        int meta=state.getValue(PLACING).getMeta();
        Direction facing=state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        boolean isOpen = state.getValue(BlockStateProperties.OPEN);
        int index=8*meta+2*facing.get2DDataValue()+(isOpen?1:0);
        if (!VoxelDefinition.isInit){
            VoxelDefinition.init();
        }
        return VoxelDefinition.gardenDoorShape[index];
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        worldIn.setBlock(pos.above(), state.setValue(PLACING, DoorPlacing.LEFT_UP), 3);
        worldIn.setBlock(pos.relative(facing.getCounterClockWise()),state.setValue(PLACING,DoorPlacing.RIGHT_DOWN),3);
        worldIn.setBlock(pos.relative(facing.getCounterClockWise()).above(),state.setValue(PLACING,DoorPlacing.RIGHT_UP),3);
    }


    protected List<BlockPos> getPosOfNeighborBlock(BlockPos pos,DoorPlacing placing,Direction facing){
        List<BlockPos> blockToDestroy = Lists.newArrayList();
        BlockPos offsetPos;
        if (placing.isUp()){
            offsetPos = pos.below();
            blockToDestroy.add(pos.below());
        } else {
            offsetPos = pos.above();
            blockToDestroy.add(pos.above());
        }
        if (placing.isLeft()){
            offsetPos = offsetPos.relative(facing.getCounterClockWise());
            blockToDestroy.add(offsetPos);
            blockToDestroy.add(pos.relative(facing.getCounterClockWise()));
        }else {
            offsetPos = offsetPos.relative(facing.getClockWise());
            blockToDestroy.add(offsetPos);
            blockToDestroy.add(pos.relative(facing.getClockWise()));
        }
        return blockToDestroy;
    }

    @Override
    protected EnumProperty<DoorPlacing> getPlacingBSP() {
        return PLACING;
    }

    public boolean isInnerUpdate(DoorPlacing placing, Direction facingUpdate, Direction blockFacing){
        return ( placing.isUp() && facingUpdate == Direction.DOWN) ||
               (!placing.isUp() && facingUpdate == Direction.UP)   ||
                (placing.isLeft() && facingUpdate == blockFacing.getCounterClockWise()) ||
                (!placing.isLeft() && facingUpdate == blockFacing.getClockWise());
    }

}
