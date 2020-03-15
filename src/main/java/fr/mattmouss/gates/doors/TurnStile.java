package fr.mattmouss.gates.doors;


import fr.mattmouss.gates.enum_door.TurnSPosition;
import fr.mattmouss.gates.tileentity.TurnStileTileEntity;
import fr.mattmouss.gates.tools.VoxelInts;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class TurnStile extends Block {

    public static EnumProperty<TurnSPosition> TS_POSITION ;
    public static IntegerProperty ANIMATION;
    private static VoxelInts TURN_STILE;



    static  {
        TS_POSITION = EnumProperty.create("ts_position",TurnSPosition.class);
        ANIMATION = IntegerProperty.create("animation",0,1);
    }

    public TurnStile() {
        super(Properties.create(Material.BARRIER)
                .hardnessAndResistance(2.0f)
                .sound(SoundType.METAL)
                .notSolid());
        this.setRegistryName("turn_stile");
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
        if (state.get(TS_POSITION).getMeta()!=0){
            return VoxelShapes.fullCube();
        }
        return VoxelShapes.empty();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TurnStileTileEntity();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity player, ItemStack stack) {
        if (player != null){
            Direction direction = Functions.getDirectionFromEntity(player,pos);
            DoorHingeSide dhs = Functions.getHingeSideFromEntity(player,pos,direction);
            //the main block
            world.setBlockState(pos,
                    state
                    .with(BlockStateProperties.HORIZONTAL_FACING,direction)
                    .with(BlockStateProperties.DOOR_HINGE,dhs)
                    .with(ANIMATION,0)
                    .with(TS_POSITION,TurnSPosition.MAIN)
            );
            //the left block
            world.setBlockState(pos.offset(direction.rotateY()),
                    state
                    .with(BlockStateProperties.HORIZONTAL_FACING,direction)
                    .with(BlockStateProperties.DOOR_HINGE,dhs)
                    .with(ANIMATION,0)
                    .with(TS_POSITION,TurnSPosition.LEFT_BLOCK)
            );
            //the right block
            world.setBlockState(pos.offset(direction.rotateYCCW()),
                    state
                    .with(BlockStateProperties.HORIZONTAL_FACING,direction)
                    .with(BlockStateProperties.DOOR_HINGE,dhs)
                    .with(ANIMATION,0)
                    .with(TS_POSITION,TurnSPosition.RIGHT_BLOCK)
            );
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING,TS_POSITION,ANIMATION,BlockStateProperties.DOOR_HINGE);
    }

    @Override
    public void harvestBlock(World world, PlayerEntity entity, BlockPos pos, BlockState state, @Nullable TileEntity tileEntity, ItemStack stack) {
        super.harvestBlock(world, entity, pos, Blocks.AIR.getDefaultState(), tileEntity, stack);
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity entity) {
        System.out.println("destroying all block of turn stile");
        TurnStileTileEntity tste = (TurnStileTileEntity) world.getTileEntity(pos);
        assert tste != null;
        ItemStack stack = entity.getHeldItemMainhand();
        List<BlockPos> posList = tste.getPositionOfBlockConnected();
        //je détruis tout les blocks à coté ainsi que le block lui même
        for (BlockPos pos1 : posList) {
            this.deleteBlock(pos1,world);
            BlockState state1= world.getBlockState(pos1);
            world.playEvent(entity,2001,pos1,Block.getStateId(state1));
            if (!world.isRemote && !entity.isCreative() && entity.canHarvestBlock(state1)) {
                Block.spawnDrops(state1, world, pos1, null, entity, stack);
            }
        }
    }

    public void deleteBlock(BlockPos pos, World world){
        world.setBlockState(pos,Blocks.AIR.getDefaultState(),35);
    }


    public boolean allowsMovement(BlockState state, IBlockReader reader, BlockPos pos, PathType pathType) {
        switch(pathType) {
            case LAND:
            case AIR:
                return (state.get(ANIMATION)==4);
            default:
                return false;
        }
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        TurnStileTileEntity tste = (TurnStileTileEntity) world.getTileEntity(pos);
        Direction facing = tste.getBlockState().get(BlockStateProperties.HORIZONTAL_FACING);
        if (entity instanceof PlayerEntity){
            boolean isValid = tste.checkPlayer((PlayerEntity) entity);
            if (isValid){
                Vec3d movement_vec = getVec3d(facing.getDirectionVec());
                entity.move(MoverType.PLAYER,movement_vec);
                tste.changeAllAnim();
            }
        }
    }

    private Vec3d getVec3d(Vec3i directionVec) {
        int x = directionVec.getX();
        int y = directionVec.getY();
        int z = directionVec.getZ();
        return new Vec3d(x,y,z);
    }
}
