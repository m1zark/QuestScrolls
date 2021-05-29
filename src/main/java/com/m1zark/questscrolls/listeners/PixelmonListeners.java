package com.m1zark.questscrolls.listeners;

import com.google.gson.Gson;
import com.m1zark.m1utilities.api.Inventories;
import com.m1zark.questscrolls.data.Quests;
import com.m1zark.questscrolls.data.Task;
import com.m1zark.questscrolls.data.enums.QuestTasks;
import com.m1zark.questscrolls.utils.Utils;
import com.pixelmonmod.pixelmon.api.events.*;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.enums.EnumShrine;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;

import java.util.UUID;

public class PixelmonListeners {
    @SubscribeEvent
    public void pokemonCapture(CaptureEvent.SuccessfulCapture event) {
        checkQuest(event.player, QuestTasks.CATCH, event.getPokemon().getPokemonData(), null, false);
    }

    @SubscribeEvent
    public void onBattleEnd(BeatWildPixelmonEvent event) {
        for (PixelmonWrapper wrapper : event.wpp.allPokemon) {
            Pokemon pokemon = wrapper.entity.getPokemonData();
            checkQuest(event.player, QuestTasks.DEFEAT, pokemon, null, wrapper.entity.isBossPokemon());
        }
    }

    @SubscribeEvent
    public void onEggCollect(BreedEvent.CollectEgg event) {
        checkQuest((EntityPlayerMP) Sponge.getServer().getPlayer(event.owner).get(), QuestTasks.BREED, event.getEgg(), null, false);
    }

    @SubscribeEvent
    public void onEvolveEvent(EvolveEvent.PostEvolve event) {
        checkQuest(event.player, QuestTasks.EVOLVE, event.preEvo.getPokemonData(), null, false);
    }

    @SubscribeEvent
    public void onLevelUpEvent(LevelUpEvent event) {
        checkQuest(event.player, QuestTasks.LEVEL, event.pokemon.getPokemon(), null, false);
    }

    @SubscribeEvent
    public void onActivateShrine(PlayerActivateShrineEvent event) {
        checkQuest(event.player, QuestTasks.SHRINE, null, event.shrineType, false);
    }

    private void checkQuest(EntityPlayerMP player, QuestTasks type, Pokemon pokemon, EnumShrine shrine, boolean boss) {
        Inventory inv = ((Player) player).getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class)).union(((Player) player).getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class)));

        for (Inventory slot : inv.slots()) {
            ItemStack item = slot.peek().orElse(null);

            if (item == null || item.isEmpty()) continue; // if the slot is empty we skip it

            if(Inventories.doesHaveNBT(item,"quest")) {
                Quests quest = new Gson().fromJson(item.toContainer().get(DataQuery.of("UnsafeData","quest")).get().toString(), Quests.class);

                if(quest.getOwner() == null || !((Player) player).getUniqueId().equals(quest.getOwner())) return;

                switch (type) {
                    case LEVEL:
                    case EVOLVE:
                    case BREED:
                    case CATCH:
                        for(Task task : quest.getTasks()) {
                            if(task.getTask().equals(type)) {
                                if (!task.getPokemon().equals("pokemon")) {
                                    if (pokemon.getSpecies().name.equals(task.getPokemon())) {
                                        if(task.isShiny() && pokemon.isShiny()) task.setProgress(task.getProgress() + 1);
                                        else task.setProgress(task.getProgress() + 1);
                                    }
                                } else {
                                    if(task.isShiny() && pokemon.isShiny()) task.setProgress(task.getProgress() + 1);
                                    else task.setProgress(task.getProgress() + 1);
                                }
                            }
                        }
                        break;
                    case SHRINE:
                        for(Task task : quest.getTasks()) {
                            if(task.getTask().equals(type)) {
                                if(task.getShrine() != null && task.getShrine().equals(shrine)) task.setProgress(task.getProgress() + 1);
                            }
                        }
                        break;
                    case DEFEAT:
                        for(Task task : quest.getTasks()) {
                            if(task.getTask().equals(type)) {
                                if (task.isBoss()) {
                                    if (pokemon.getSpecies().name.equals(task.getPokemon()) && boss) task.setProgress(task.getProgress() + 1);
                                } else {
                                    if (!task.getPokemon().equals("pokemon")) {
                                        if (pokemon.getSpecies().name.equals(task.getPokemon())) task.setProgress(task.getProgress() + 1);
                                    } else {
                                        task.setProgress(task.getProgress() + 1);
                                    }
                                }
                            }
                        }
                        break;
                }

                Utils.updateQuest((Player) player, quest);
                //break;
            }
        }
    }
}
