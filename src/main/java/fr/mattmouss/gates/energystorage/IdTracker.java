package fr.mattmouss.gates.energystorage;

import fr.mattmouss.gates.GatesMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IdTracker extends SavedData {
    private final HashMap<Integer,Integer> all_id = new HashMap<>();

    public IdTracker() {
        super(/*"idgates"*/);
    }

    public IdTracker(CompoundTag tag){
        if (tag.contains("idlist") && tag.get("idlist") instanceof ListTag){
            ListTag nbt = (ListTag) tag.get("idlist");
            assert nbt != null;
            for (Tag inbt : nbt){
                CompoundTag nbt_id = ((CompoundTag)inbt).getCompound("id");
                int id= nbt_id.getInt("id");
                CompoundTag nbt_number = ((CompoundTag)inbt).getCompound("number");
                int nb = nbt_number.getInt("nb");
                System.out.println("reading id : "+id);
                System.out.println("reading number : "+nb);
                if (!all_id.containsKey(id)){// if id is not found in id_list we put it
                    all_id.put(id,nb);
                }else if (all_id.get(id) != nb){ // if it is already there and the number of gate has changed put the new number
                    all_id.replace(id,nb);
                }
            }
        }
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        ListTag nbt = new ListTag();
        all_id.forEach((id,number)->{//for each key we add a compound intern nbt which contains
            //a compound "id" for stocking an int "id" -> id of gates
            //a compound "nb" for stocking an int "nb" -> number of gates with this id
            CompoundTag internNbt = new CompoundTag();
            CompoundTag nbt_id = new CompoundTag();
            nbt_id.putInt("id",id);
            CompoundTag nbt_number = new CompoundTag();
            nbt_number.putInt("nb",number);
            internNbt.put("id",nbt_id);
            System.out.println("writing id : "+id);
            System.out.println("writing number : "+number);
            internNbt.put("number",nbt_number);
            nbt.add(internNbt);
        });
        compound.put("idlist",nbt);
        return compound;
    }

    public void addNewId(int id){
        if (all_id.containsKey(id)){//if the id is already there just increment the number in the hashMap with the key id
            int number = all_id.get(id)+1;
            all_id.replace(id,number);
        }else { //if the id doesn't exist we should create it with 1 as number of gates with this id
            all_id.put(id,1);
        }
        this.setDirty();
    }

    public void removeId(int id){ //it returns the number of gates with this id counting 0 if no gates has this id
        int number =all_id.getOrDefault(id,0);
        if (number == 0){ //if no gates a warning impossible to delete
            GatesMod.logger.warning("Trying to remove non existing id !!");
        }else if (number == 1){ //if only one let's remove id of the HashMap
            all_id.remove(id);
        }else { //if there is more than one we decrement
            number--;
            all_id.replace(id,number);
        }
        this.setDirty();
    }

    public List<Integer> getList(){
        List<Integer> id_list=new ArrayList<>();
        all_id.forEach((key,id)-> id_list.add(key));
        return id_list;
    }
}