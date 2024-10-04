package fr.moonshade.gates.setup;

import fr.moonshade.gates.blocks.ModBlock;
import fr.moonshade.gates.gui.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ClientProxy implements IProxy {

    @Override
    public void init() {
        MenuScreens.register(ModBlock.TOLLGATE_USER_CONTAINER, TGUserScreen::new);
        MenuScreens.register(ModBlock.TOLLGATE_TECH_CONTAINER, TGTechnicianScreen::new);
        MenuScreens.register(ModBlock.TURN_STILE_CONTAINER, TSScreen::new);
        MenuScreens.register(ModBlock.CARD_GETTER_CONTAINER, CardGetterScreen::new);
        MenuScreens.register(ModBlock.CARD_GETTER_CHOICE_CONTAINER, CardGetterChoiceScreen::new);
    }

    @Override
    public Level getClientWorld() {
        return Minecraft.getInstance().level;
    }

    @Override
    public Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }
}
