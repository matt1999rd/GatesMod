package fr.mattmouss.gates.doors;

import com.google.common.collect.Lists;
import fr.mattmouss.gates.enum_door.DoorPlacing;
import fr.mattmouss.gates.voxels.VoxelDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
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

public class LargeDoor extends MultipleBlockDoor {
    public static EnumProperty<DoorPlacing> PLACING = EnumProperty.create("position",DoorPlacing.class,DoorPlacing::isSide);

    public LargeDoor(String key,Material material) {
        super(Properties.of(material, MaterialColor.COLOR_BLACK).noOcclusion());
        this.setRegistryName(key);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Material material = this.material;
        if (!state.getValue(PLACING).isSide()){
            return Shapes.empty();
        }
        if (material == Material.WOOD){
            return getCircleShape(state);
        }else {
            return getSquareShape(state);
        }
    }

    private VoxelShape getSquareShape(BlockState state) {
        DoorPlacing placing = state.getValue(PLACING);
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        boolean isOpen = state.getValue(BlockStateProperties.OPEN);
        int index = placing.getMeta()*8+facing.get2DDataValue()*2+ ((isOpen)?1:0);
        if (!VoxelDefinition.isInit){
            VoxelDefinition.init();
        }
        return VoxelDefinition.largeDoorSquareShape[index];
    }

    private VoxelShape getCircleShape(BlockState state) {
        DoorPlacing placing = state.getValue(PLACING);
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        boolean isOpen = state.getValue(BlockStateProperties.OPEN);
        int index = placing.getMeta()*8+facing.get2DDataValue()*2+ ((isOpen)?1:0);
        if (!VoxelDefinition.isInit){
            VoxelDefinition.init();
        }
        return VoxelDefinition.largeDoorCircleShape[index];
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        worldIn.setBlock(pos.above(), state.setValue(PLACING, DoorPlacing.LEFT_CENTER), 3);
        worldIn.setBlock(pos.above(2), state.setValue(PLACING, DoorPlacing.LEFT_UP), 3);
        worldIn.setBlock(pos.relative(facing.getCounterClockWise()),state.setValue(PLACING,DoorPlacing.RIGHT_DOWN),3);
        worldIn.setBlock(pos.relative(facing.getCounterClockWise()).above(),state.setValue(PLACING,DoorPlacing.RIGHT_CENTER),3);
        worldIn.setBlock(pos.relative(facing.getCounterClockWise()).above(2),state.setValue(PLACING,DoorPlacing.RIGHT_UP),3);
    }

    protected List<BlockPos> getPosOfNeighborBlock(BlockPos pos,DoorPlacing placing,Direction facing){
        List<BlockPos> blockToDestroy = Lists.newArrayList();
        BlockPos offsetPos;
        BlockPos offsetPos2;
        if (placing.isUp()){
            offsetPos = pos.below();
            offsetPos2 = pos.below(2);
            blockToDestroy.add(pos.below());
            blockToDestroy.add(pos.below(2));
        }else if (placing.isCenterY()){
            offsetPos = pos.above();
            offsetPos2 = pos.below();
            blockToDestroy.add(pos.above());
            blockToDestroy.add(pos.below());
        } else {
            offsetPos = pos.above();
            offsetPos2 = pos.above(2);
            blockToDestroy.add(pos.above());
            blockToDestroy.add(pos.above(2));
        }
        if (placing.isLeft()){
            offsetPos = offsetPos.relative(facing.getCounterClockWise());
            blockToDestroy.add(offsetPos);
            offsetPos2 = offsetPos2.relative(facing.getCounterClockWise());
            blockToDestroy.add(offsetPos2);
            blockToDestroy.add(pos.relative(facing.getCounterClockWise()));
        }else {
            offsetPos = offsetPos.relative(facing.getClockWise());
            blockToDestroy.add(offsetPos);
            offsetPos2 = offsetPos2.relative(facing.getClockWise());
            blockToDestroy.add(offsetPos2);
            blockToDestroy.add(pos.relative(facing.getClockWise()));
        }
        return blockToDestroy;
    }

    @Override
    protected EnumProperty<DoorPlacing> getPlacingBSP() {
        return PLACING;
    }

    protected boolean isInnerUpdate(DoorPlacing placing, Direction facingUpdate, Direction blockFacing){
        return ( (placing.isUp()|| placing.isCenterY()) && facingUpdate == Direction.DOWN) ||
                ((!placing.isUp()|| placing.isCenterY()) && facingUpdate == Direction.UP) ||
                (placing.isLeft() && facingUpdate == blockFacing.getCounterClockWise()) ||
                (!placing.isLeft() && facingUpdate == blockFacing.getClockWise());
    }

}
