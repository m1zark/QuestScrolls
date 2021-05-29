package com.m1zark.questscrolls.listeners;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Inventories;
import com.m1zark.questscrolls.QuestScrolls;
import com.m1zark.questscrolls.config.ConfigMain;
import com.m1zark.questscrolls.config.ConfigMessages;
import com.m1zark.questscrolls.data.Quests;
import com.m1zark.questscrolls.data.Task;
import com.m1zark.questscrolls.data.enums.QuestTasks;
import com.m1zark.questscrolls.events.QuestEvent;
import com.m1zark.questscrolls.utils.Utils;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketCustomSound;
import net.minecraft.util.SoundCategory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackComparators;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.BookView;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;

public class PlayerListeners {
    @Listener
    public void OnItemUse(InteractItemEvent.Secondary.MainHand event, @Root Player player) {
        if (!player.getItemInHand(HandTypes.MAIN_HAND).isPresent()) return;
        ItemStack heldItem = player.getItemInHand(HandTypes.MAIN_HAND).get();

        if (Inventories.doesHaveNBT(heldItem,"quest")) {
            Quests quest = new Gson().fromJson(heldItem.toContainer().get(DataQuery.of("UnsafeData","quest")).get().toString(), Quests.class);
            String qid = heldItem.toContainer().get(DataQuery.of("UnsafeData","questID")).get().toString();

            if(quest.getOwner() == null) {
                Chat.sendMessage(player, ConfigMessages.getMessages("Messages.claim-owner"));
                Utils.updateOwner(player, qid);
                return;
            }

            if(!(player.getUniqueId().equals(quest.getOwner()))) {
                Chat.sendMessage(player, ConfigMessages.getMessages("Messages.has-owner"));
                return;
            }

            int completions = 0;
            for(Task task : quest.getTasks()) {
                if(task.isComplete()) completions++;

                if(task.getTask().equals(QuestTasks.COLLECT) && !task.isComplete()) {
                    ItemStack stack = task.getItem().parseItem();
                    Inventory inv = player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class)).union(player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class)));

                    for (Inventory slot : inv.slots()) {
                        ItemStack item = slot.peek().orElse(null);

                        if (item == null || item.isEmpty()) continue; // if the slot is empty we skip it

                        item.setQuantity(1);

                        if(ItemStackComparators.IGNORE_SIZE.compare(stack,item) >= 0) {
                            int itemCount = Inventories.getItemCount(player, stack);

                            if(itemCount >= task.getCount()) {
                                Inventories.removeItem(player, stack, task.getCount());
                                task.setProgress(task.getCount());
                                completions++;
                            } else {
                                if(task.getProgress() == itemCount) continue;

                                if(task.getProgress() + itemCount >= task.getCount()) {
                                    Inventories.removeItem(player, stack, task.getCount() - task.getProgress());
                                    task.setProgress(task.getCount());
                                    completions++;
                                } else {
                                    if(itemCount <= task.getProgress()) {
                                        task.setProgress(itemCount);
                                    } else {
                                        if(itemCount > task.getProgress()) {
                                            task.setProgress(task.getProgress() + (itemCount - task.getProgress()));
                                        } else {
                                            task.setProgress(task.getProgress() + itemCount);
                                        }
                                    }

                                }
                            }

                            Utils.updateQuest(player, quest);
                            break;
                        }
                    }
                }
            }

            if(completions >= quest.getTasks().size()) {
                if(quest.isBroadcast()) {
                    Chat.sendBroadcastMessage(player, ConfigMessages.getMessages("Messages.broadcast").replace("{player}",player.getName()).replace("{tier}", quest.getTier().getColorCode() + quest.getTier().getName() + "&r"));
                }

                Sponge.getEventManager().post(new QuestEvent(player.getName(), quest, Cause.of(EventContext.builder().add(EventContextKeys.PLUGIN, QuestScrolls.getInstance().getPluginContainer()).add(EventContextKeys.PLAYER_SIMULATED, player.getProfile()).build(), QuestScrolls.getInstance())));

                List<String> rewards = Lists.newArrayList();
                quest.getRewards().forEach(reward -> {
                    if(reward.getType().equals("command")) Sponge.getCommandManager().process(Sponge.getServer().getConsole(), reward.parseCommand(player));
                    if(reward.getType().equals("item")) {
                        if(!Inventories.giveItem(player, reward.parseItem(), reward.getCount())) {
                            Chat.sendMessage(player, ConfigMessages.getMessages("Messages.inventory-full").replace("{reward}",reward.getName()));
                        }
                    }

                    rewards.add(reward.getName());
                });

                EntityPlayerMP p = (EntityPlayerMP) player;
                p.connection.sendPacket(new SPacketCustomSound("pixelmon:pixelmon.mob.pikachu", SoundCategory.AMBIENT, p.posX, p.posY, p.posZ, 1, 1));

                //player.playSound(SoundTypes.BLOCK_ENCHANTMENT_TABLE_USE, player.getLocation().getPosition(), 1);

                Chat.sendMessage(player, ConfigMessages.getMessages("Messages.reward").replace("{rewards}", String.join(", ", rewards)));
                Inventories.removeItem(player, player.getItemInHand(HandTypes.MAIN_HAND).get(), 1);
            } else {
                BookView.Builder bookBuilder = BookView.builder().title(Text.of(quest.getTier().getColor(), quest.getTier().getName() + " " + ConfigMain.NAME)).author(Text.of(player.getName()));
                Text.Builder configuredLine = LiteralText.builder();

                bookBuilder.addPages(Text.of(TextSerializers.FORMATTING_CODE.deserialize(quest.getLore())));

                for(Task task : quest.getTasks()) {
                    if(!task.isComplete()) {
                        String objective = "";
                        String needed = "&7" + (task.getCount() - task.getProgress()) + "&a";

                        switch(task.getTask()) {
                            case DEFEAT:
                            case CATCH:
                                String type = task.isBoss() ? " boss " : task.isShiny() ? " shiny " : EnumSpecies.hasPokemonAnyCase(task.getPokemon()) ? " " : " wild ";
                                objective = "&b" + task.getTask().getTask() + " " + needed + type + task.getPokemon();
                                break;
                            case LEVEL:
                                objective = "&b" + task.getTask().getTask() + " up " + task.getPokemon() + " " + needed + " " + Utils.pluralize("time", task.getCount() - task.getProgress());
                                break;
                            case COLLECT:
                                objective = "&b" + task.getTask().getTask() + " " + needed + " " + Utils.pluralize(task.getItem().display(), task.getCount() - task.getProgress());
                                break;
                            case SHRINE:
                                objective = "&b" + task.getTask().getTask() + " " + needed + " " + task.getShrine().name() + " shrine";
                                break;
                            case EVOLVE:
                            case BREED:
                                objective = "&b" + task.getTask().getTask() + " " + needed + " " + task.getPokemon();
                                break;
                        }

                        Text.Builder lineBuilder = Text.builder().append(TextSerializers.FORMATTING_CODE.deserialize(objective));
                        configuredLine.append(lineBuilder.build());
                        configuredLine.append(LiteralText.NEW_LINE);
                    }
                }
                bookBuilder.addPage(configuredLine.build());

                player.sendBookView(bookBuilder.build());

                /*
                Chat.sendMessage(player, "&7----- &e" + quest.getTier().getName() + " Quest &7-----");
                for(Task task : quest.getTasks()) {
                    if(!task.isComplete()) {
                        String objective = "";
                        String needed = "&7" + (task.getCount() - task.getProgress()) + "&a";

                        switch(task.getTask()) {
                            case DEFEAT:
                            case CATCH:
                                String type = task.isBoss() ? " boss " : task.isShiny() ? " shiny " : EnumSpecies.hasPokemonAnyCase(task.getPokemon()) ? " " : " wild ";
                                objective = "&b" + task.getTask().getTask() + " " + needed + type + task.getPokemon();
                                break;
                            case LEVEL:
                                objective = "&b" + task.getTask().getTask() + " up " + task.getPokemon() + " " + needed + " " + Utils.pluralize("time", task.getCount() - task.getProgress());
                                break;
                            case COLLECT:
                                objective = "&b" + task.getTask().getTask() + " " + needed + " " + Utils.pluralize(task.getItem().display(), task.getCount() - task.getProgress());
                                break;
                            case SHRINE:
                                objective = "&b" + task.getTask().getTask() + " " + needed + " " + task.getShrine().name() + " shrine";
                                break;
                            case EVOLVE:
                            case BREED:
                                objective = "&b" + task.getTask().getTask() + " " + needed + " " + task.getPokemon();
                                break;
                        }

                        Chat.sendMessage(player, objective);
                    }
                }
                */
            }
        }
    }
}
