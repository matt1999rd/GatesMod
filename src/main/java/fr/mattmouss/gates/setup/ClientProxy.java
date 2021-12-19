package fr.mattmouss.gates.setup;

import fr.mattmouss.gates.blocks.ModBlock;
import fr.mattmouss.gates.gui.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class ClientProxy implements IProxy {

    @Override
    public void init() {
        ScreenManager.register(ModBlock.TOLLGATE_USER_CONTAINER, TGUserScreen::new);
        ScreenManager.register(ModBlock.TOLLGATE_TECH_CONTAINER, TGTechnicianScreen::new);
        ScreenManager.register(ModBlock.TURN_STILE_CONTAINER, TSScreen::new);
        ScreenManager.register(ModBlock.CARD_GETTER_CONTAINER, CardGetterScreen::new);
        ScreenManager.register(ModBlock.CARD_GETTER_CHOICE_CONTAINER, CardGetterChoiceScreen::new);
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getInstance().level;
    }

    @Override
    public PlayerEntity getClientPlayer() {
        return Minecraft.getInstance().player;
    }
}
