package fr.mattmouss.gates.blocks;

import fr.mattmouss.gates.tileentity.CardGetterTileEntity;
import fr.mattmouss.gates.util.Functions;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
//import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;


public class CardGetter extends Block {
    public CardGetter() {
        super(Properties.of(Material.METAL)
        .strength(2f)
        .sound(SoundType.METAL)
        //1.15 function
        //.notSolid()
        .noOcclusion()
        .lightLevel(value -> 5));
        this.setRegistryName("card_getter");
    }

    private static final VoxelShape SHAPE = Block.box(0,0,0,16,32,16);

    @Override
    public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_) {
        return SHAPE;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new CardGetterTileEntity();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity != null){
            world.setBlockAndUpdate(pos,state.setValue(BlockStateProperties.HORIZONTAL_FACING, Functions.getDirectionFromEntity(entity,pos)));
        }
    }

    //1.14.4 function replaced by notSolid()
/*
    @Override
    public BlockRenderLayer func_180664_k() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }
*/



    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    //1.15 function


    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        Direction looking_direction = Functions.getDirectionFromEntity(player,pos);
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        CardGetterTileEntity cgte = (CardGetterTileEntity) world.getBlockEntity(pos);
        if (facing == looking_direction){
            assert cgte != null;
            cgte.setSide(true); // player is a user
            if (!world.isClientSide) NetworkHooks.openGui((ServerPlayerEntity) player,cgte,cgte.getBlockPos());
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }



/*
    //1.14.4 function onBlockActivated

    @Override
    public boolean func_220051_a(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand p_220051_5_, BlockRayTraceResult p_220051_6_) {
        if (player != null){
            Direction looking_direction = Functions.getDirectionFromEntity(player,pos);
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            CardGetterTileEntity cgte = (CardGetterTileEntity) world.getBlockEntity(pos);
            if (facing == looking_direction){
                cgte.setSide(true); // player is a user
                if (!world.isClientSide) NetworkHooks.openGui((ServerPlayerEntity) player,cgte,cgte.getBlockPos());
                return true;
            }
        }
        return false;
    }
*/


}
