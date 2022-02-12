package fr.mattmouss.gates.tileentity;

import fr.mattmouss.gates.doors.AbstractTurnStile;
import fr.mattmouss.gates.doors.TurnStile;
import fr.mattmouss.gates.enum_door.TurnSPosition;
import fr.mattmouss.gates.network.Networking;
import fr.mattmouss.gates.network.movePlayerPacket;
import fr.mattmouss.gates.setup.ModSound;
import fr.mattmouss.gates.tscapability.ITSStorage;
import fr.mattmouss.gates.tscapability.TSStorage;
import fr.mattmouss.gates.tscapability.TurnStileBooleanCapability;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTurnStileTileEntity extends TileEntity {

    private final LazyOptional<TSStorage> boolStorage = LazyOptional.of(this::getBoolStorage).cast();

    public AbstractTurnStileTileEntity(TileEntityType<?> p_i48289_1_) {
        super(p_i48289_1_);
    }

    @Nonnull
    public TSStorage getBoolStorage() {
        return new TSStorage();
    }

    protected void managePlayerMovement(){
        BlockState state = this.getBlockState();
        Vector3d vector3d = Vector3d.atCenterOf(getMainPos());
        assert level != null;
        PlayerEntity player = level.getNearestPlayer(vector3d.x, vector3d.y, vector3d.z, 2, false);
        if (state.getValue(TurnStile.WAY_IS_ON)) {
            movePlayer(player,false);
        }
        movePlayer(player,true);
    }

    private void movePlayer(PlayerEntity player, boolean fromExit){
        if (this.isAnimationInWork() && checkPlayerMovement(player,fromExit)) {
            movePlayerGoingThrough(player, fromExit);
            return;
        }
        if (playerIsGoingThrough(player, fromExit)) {
            movePlayerGoingThrough(player, fromExit);
            Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(ModSound.TURN_STILE_PASS, 1.0F));
        }
    }

    public List<BlockPos> getPositionOfBlockConnected() {
        List<BlockPos> posList = new ArrayList<>();
        Direction direction = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos main_pos = getMainPos();
        posList.add(main_pos);
        posList.add(main_pos.relative(direction.getClockWise()));
        posList.add(main_pos.relative(direction.getCounterClockWise()));
        posList.add(main_pos.relative(Direction.UP));
        return posList;
    }

    public BlockPos getMainPos(){
        TurnSPosition tsp = this.getBlockState().getValue(TurnStile.TS_POSITION);
        Direction direction = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        switch (tsp.getMeta()){
            case 0:
                return worldPosition;
            case 1:
                return worldPosition.relative(direction.getClockWise());
            case 2:
                return worldPosition.relative(direction.getCounterClockWise());
            case 3:
                return worldPosition.relative(Direction.DOWN);
            default:
                throw new IllegalArgumentException("unknown meta value for tsp :"+tsp.getMeta());
        }
    }

    public void openTS() {
        BlockState state = this.getBlockState();
        assert level != null;
        level.setBlockAndUpdate(worldPosition, state.setValue(TurnStile.WAY_IS_ON, true));
    }

    public void blockTS() {
        BlockState state = this.getBlockState();
        assert level != null;
        level.setBlockAndUpdate(worldPosition, state.setValue(TurnStile.WAY_IS_ON, false));
    }

    public void changeAllAnim() {
        List<BlockPos> posList = getPositionOfBlockConnected();
        for (BlockPos pos1 : posList){
            assert level != null;
            AbstractTurnStileTileEntity tste = (AbstractTurnStileTileEntity) level.getBlockEntity(pos1);
            assert tste != null;
            tste.changeAnim();
        }
    }

    public void changeAnim(){
        BlockState state = this.getBlockState();
        int i = state.getValue(TurnStile.ANIMATION);
        assert level != null;
        level.setBlockAndUpdate(worldPosition,state.setValue(TurnStile.ANIMATION,1-i));
    }

    //this function check movement of the player
    protected boolean checkPlayerMovement(PlayerEntity entity,boolean fromExit){
        Direction facing = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (entity != null){
            Vector3d deltaMovement = entity.getDeltaMovement();
            return (Direction.getNearest(deltaMovement.x,deltaMovement.y,deltaMovement.z)) == ((fromExit)? facing : facing.getOpposite());
        }
        return false;
    }

    //this function check if the player in argument is going into this turn stile with a certain speed
    protected boolean playerIsGoingThrough(PlayerEntity entity,boolean fromExit) {
        Direction horizontalFacing = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction positionFacingToCheck = (!fromExit)? horizontalFacing : horizontalFacing.getOpposite();
        BlockPos mainPos = this.getMainPos();
        double x = mainPos.getX();
        double y = mainPos.getY();
        double z = mainPos.getZ();
        if (entity != null){
            //the player is going in the direction of the turn stile
            boolean isRightMove = checkPlayerMovement(entity,fromExit);
            Vector3d player_pos = entity.position();
            double x_player = player_pos.x;
            double y_player = player_pos.y;
            double z_player = player_pos.z;
            boolean isPlayerInFrontOfMainBlock;
            Direction.Axis perpendicularAxis = positionFacingToCheck.getAxis();
            Direction.Axis parallelAxis = positionFacingToCheck.getClockWise().getAxis();
            double playerPerpendicularProjection = perpendicularAxis.choose(x_player,y_player,z_player);
            double playerParallelProjection = parallelAxis.choose(x_player,y_player,z_player);
            double turnStilePerpendicularProjection = perpendicularAxis.choose(x,y,z);
            double turnStileParallelProjection = parallelAxis.choose(x,y,z);
            int axisDirOffset = positionFacingToCheck.getAxisDirection().getStep();
            // it is a very simplified expression which check in each direction for placement of player
            // if NORTH or SOUTH it will check the coordinate z and verify if
            // for NORTH posZ-0.3<z<posZ for SOUTH posZ+1<z<posZ+1.3
            // if EAST or WEST it will check th coordinate x and verify if
            // for WEST posX-0.3<x<posX for EAST posX+1<x<posX+1.3
            // facing is the opposite side if fromExit is true
            // check the parallel direction to ensure that only player in front of the turn stile part can be moved
            // if NORTH or SOUTH check x otherwise check z in the range posX;posX+1 or posZ;posZ+1
            isPlayerInFrontOfMainBlock =
                    (playerPerpendicularProjection>turnStilePerpendicularProjection+0.35+axisDirOffset*0.65) &&
                            (playerPerpendicularProjection<turnStilePerpendicularProjection+0.65+0.65*axisDirOffset) &&
                            (playerParallelProjection>turnStileParallelProjection) &&
                            (playerParallelProjection<turnStileParallelProjection+1);
            return isRightMove && isPlayerInFrontOfMainBlock;
        }
        return false;
    }



    //this function is aimed to move the player into the turn stile
    protected void movePlayerGoingThrough(PlayerEntity player, boolean fromExit) {
        if (player == null){
            return;
        }
        this.changeAllAnim();

        BlockPos pos = getMainPos();
        System.out.println("moving player into turn stile !!");
        Networking.INSTANCE.sendToServer(new movePlayerPacket(pos, player.getUUID(),fromExit));
        if (this.isAnimationInWork()){
            this.endAnimation();
        }else {
            this.startAnimation();
        }
    }

    //check if this TE is the control unit tile entity to avoid multiple definition of id storage that will be of no use
    public boolean isControlUnit() {
        Block block=this.getBlockState().getBlock();
        if (!(block instanceof AbstractTurnStile)){
            return false;
        }
        AbstractTurnStile turnStile = (AbstractTurnStile) block;
        return turnStile.isControlUnit(this.getBlockState());
    }

    protected boolean isAnimationInWork(){
        return boolStorage.map(ITSStorage::getAnimationInWork).orElse(false);
    }

    protected void startAnimation(){
        boolStorage.ifPresent(ITSStorage::startAnimation);
    }

    protected void endAnimation(){
        boolStorage.ifPresent(ITSStorage::endAnimation);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap,Direction side) {
        if (cap == TurnStileBooleanCapability.TURN_STILE_BOOLEAN_STORAGE){
            return boolStorage.cast();
        }
        return super.getCapability(cap,side);
    }

    protected boolean canWrite() {
        if (level == null){
            return true;
        }else {
            return isControlUnit() ;
        }
    }

    @Override
    public void load(@Nonnull BlockState state, CompoundNBT tag) {
        boolean isRightTSB = tag.getBoolean("isCU");
        if (isRightTSB) {
            CompoundNBT storage_tag;
            if (tag.contains("storage")){
                storage_tag = tag.getCompound("storage");
            }else {
                storage_tag = tag.getCompound("bool_storage");
            }
            boolStorage.ifPresent(s -> s.deserializeNBT(storage_tag));
        }
        super.load(state,tag);
    }

    @Nonnull
    @Override
    public CompoundNBT save(@Nonnull CompoundNBT tag) {
        if (canWrite()){
            boolStorage.ifPresent(e->{
                CompoundNBT nbt =e.serializeNBT();
                tag.put("bool_storage",nbt);
            });
            getCapability(TurnStileBooleanCapability.TURN_STILE_BOOLEAN_STORAGE);
        }
        if (level != null){
            tag.putBoolean("isCU", isControlUnit());
        }else{
            tag.putBoolean("isCU",false);
        }
        return super.save(tag);
    }
}
