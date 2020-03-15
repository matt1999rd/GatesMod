package fr.mattmouss.gates.items;

import fr.mattmouss.gates.setup.ModSetup;
import fr.mattmouss.gates.tileentity.TollGateTileEntity;

import fr.mattmouss.gates.tileentity.TurnStileTileEntity;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class KeyItem extends Item {



    public KeyItem() {
        super(new Item.Properties().group(ModSetup.itemGroup).maxStackSize(1));
    }

    /*
    TollGate Position registered function
     */

    //used in various function for obtaining the position registered (with world return null if position is an old and unvalued registering)
    public BlockPos getTGPosition(ItemStack stack,@Nullable World world){
        CompoundNBT nbt=stack.getOrCreateTag();
        //choose between testing only the nbt value corresponding to "toll_gate" or the reality of blockpos with world
        boolean isTGPosReg = (world == null)? isTGPositionReg(nbt) : isTGPositionReg(nbt, world);
        //System.out.println(isTGPosReg);
        if (isTGPosReg){
            //System.out.println("****************getting the value registered*******************");
            int x = nbt.getInt("pos_x");
            int y = nbt.getInt("pos_y");
            int z = nbt.getInt("pos_z");
            //System.out.println(new BlockPos(x,y,z));
            return new BlockPos(x,y,z);
        }
        return null;
    }

    //used only in this class for the function that checks directly the pos revelancy
    private BlockPos getTGPosition(CompoundNBT nbt){
        if (isTGPositionReg(nbt)){
            int x = nbt.getInt("pos_x");
            int y = nbt.getInt("pos_y");
            int z = nbt.getInt("pos_z");
            return new BlockPos(x,y,z);
        }
        return null;
    }

    //to know whether this item tag contains the blockpos of a TollGateTileEntity(present or absent)
    protected boolean isTGPositionReg(CompoundNBT nbt){
        if (nbt.contains("te")){
            //System.out.println("checking the nbt string");
            String te_name = nbt.getString("te");
            //System.out.println("name of te string : "+te_name);
            return te_name.equals("toll_gate");
        }
        return false;
    }

    //this function also checks that this block in world still exists (if not return false and remove the pos
    protected boolean isTGPositionReg(CompoundNBT nbt, World world){
        System.out.println("first test in isTGPositionReg(nbt,world) : "+isTGPositionReg(nbt));
        if (isTGPositionReg(nbt)){
            System.out.println("pos of tile entity to check : "+getTGPosition(nbt));
            TileEntity te = world.getTileEntity(getTGPosition(nbt));
            if (te == null){
                //System.out.println("***********************removing tag of compound******************");
                nbt.remove("te");
                nbt.remove("pos_x");
                nbt.remove("pos_y");
                nbt.remove("pos_z");
                return false;
            }
            //System.out.println("class of tileentity found :"+te.getClass());
            return (te instanceof TollGateTileEntity);
        }
        return false;
    }

    //to set TG position pos in the itemstack
    public void setTGPosition(ItemStack stack,World world,BlockPos pos){
        CompoundNBT nbt = stack.getOrCreateTag();
        System.out.println("defining new blockPos");
        if (isTGPositionReg(nbt,world) || isTSPositionReg(nbt,world)){
            System.out.println("not working because nbt are already defined ");
            return;
        }
        //System.out.println("adding new attribute BlockPos : "+pos);
        nbt.putString("te","toll_gate");
        nbt.putInt("pos_x",pos.getX());
        nbt.putInt("pos_y",pos.getY());
        nbt.putInt("pos_z",pos.getZ());
    }

    /*
    Turn Stile registered function
     */

    //used in various function for obtaining the position registered (with world return null if position is an old and unvalued registering)
    public BlockPos getTSPosition(ItemStack stack,@Nullable World world){
        CompoundNBT nbt=stack.getOrCreateTag();
        //choose between testing only the nbt value corresponding to "turn_stile" or the reality of blockpos with world
        boolean isTSPosReg = (world == null)? isTSPositionReg(nbt) : isTSPositionReg(nbt, world);
        //System.out.println(isTGPosReg);
        if (isTSPosReg){
            //System.out.println("****************getting the value registered*******************");
            int x = nbt.getInt("pos_x");
            int y = nbt.getInt("pos_y");
            int z = nbt.getInt("pos_z");
            //System.out.println(new BlockPos(x,y,z));
            return new BlockPos(x,y,z);
        }
        return null;
    }

    //used only in this class for the function that checks directly the pos revelancy
    private BlockPos getTSPosition(CompoundNBT nbt){
        if (isTSPositionReg(nbt)){
            int x = nbt.getInt("pos_x");
            int y = nbt.getInt("pos_y");
            int z = nbt.getInt("pos_z");
            return new BlockPos(x,y,z);
        }
        return null;
    }

    //to know whether this item tag contains the blockpos of a TollGateTileEntity(present or absent)
    protected boolean isTSPositionReg(CompoundNBT nbt){
        if (nbt.contains("te")){
            //System.out.println("checking the nbt string");
            String te_name = nbt.getString("te");
            //System.out.println("name of te string : "+te_name);
            return te_name.equals("turn_stile");
        }
        return false;
    }

    //this function also checks that this block in world still exists (if not return false and remove the pos
    protected boolean isTSPositionReg(CompoundNBT nbt, World world){
        System.out.println("first test in isTSPositionReg(nbt,world) : "+isTGPositionReg(nbt));
        if (isTGPositionReg(nbt)){
            System.out.println("pos of tile entity to check : "+getTGPosition(nbt));
            TileEntity te = world.getTileEntity(getTGPosition(nbt));
            if (te == null){
                //System.out.println("***********************removing tag of compound******************");
                nbt.remove("te");
                nbt.remove("pos_x");
                nbt.remove("pos_y");
                nbt.remove("pos_z");
                return false;
            }
            //System.out.println("class of tileentity found :"+te.getClass());
            return (te instanceof TurnStileTileEntity);
        }
        return false;
    }

    public void setTSPosition(ItemStack stack,World world,BlockPos pos){
        CompoundNBT nbt = stack.getOrCreateTag();
        System.out.println("defining new blockPos");
        if (isTSPositionReg(nbt,world) ||  isTGPositionReg(nbt,world)){
            System.out.println("not working because nbt are already defined ");
            return;
        }
        //System.out.println("adding new attribute BlockPos : "+pos);
        nbt.putString("te","turn_stile");
        nbt.putInt("pos_x",pos.getX());
        nbt.putInt("pos_y",pos.getY());
        nbt.putInt("pos_z",pos.getZ());
    }


    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        //this first five line is a filter for when stack has no position stored
        BlockPos registeredPos = getTGPosition(stack,null);
        if (registeredPos == null){
            registeredPos = getTSPosition(stack,null);
            if (registeredPos == null){
                return super.getDisplayName(stack);
            }
        }
        int id = Functions.getIdFromBlockPos(registeredPos);
        return new TranslationTextComponent(this.getTranslationKey(stack)+" nb "+id);

    }


}
