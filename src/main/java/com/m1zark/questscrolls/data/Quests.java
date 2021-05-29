package com.m1zark.questscrolls.data;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Inventories;
import com.m1zark.questscrolls.config.ConfigMain;
import com.m1zark.questscrolls.data.enums.QuestTasks;
import com.m1zark.questscrolls.data.enums.QuestTiers;
import com.m1zark.questscrolls.utils.Rewards;
import com.m1zark.questscrolls.utils.Utils;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.RandomHelper;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.enums.EnumShrine;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.*;

@Getter @Setter
@AllArgsConstructor
public class Quests {
    private String id;
    private String lore;
    private QuestTiers tier;
    private List<Task> tasks;
    private List<Rewards> rewards;
    private boolean showRewards;
    private boolean broadcast;
    private UUID owner;

    public ItemStack giveItem() {
        ItemType item = Sponge.getGame().getRegistry().getType(ItemType.class, ConfigMain.ID).orElse(ItemTypes.BOOK);
        ItemStack scroll = ItemStack.builder().itemType(item).add(Keys.DISPLAY_NAME, Text.of(this.tier.getColor(), this.tier.getName() + " " + ConfigMain.NAME)).build();

        ArrayList<Text> itemLore = new ArrayList<>();
        String[] newInfo = Utils.insertLinebreaks(this.lore,40).split("\n");
        for(String s:newInfo) itemLore.add(Text.of(Chat.embedColours("&7" + s)));

        if(!this.lore.isEmpty()) itemLore.add(Text.of(Chat.embedColours("")));
        for(Task task : this.tasks) {
            String objective = "";
            String progress = task.isComplete() ? "" : "  &c" + "(" + task.getProgress() + "/" + task.getCount() + ")";
            String complete = task.isComplete() ? " &a\u2713" : "";

            switch(task.getTask()) {
                case DEFEAT:
                case CATCH:
                    String type = task.isBoss() ? " boss " : task.isShiny() ? " shiny " : EnumSpecies.hasPokemonAnyCase(task.getPokemon()) ? " " : " wild ";
                    objective = "&b" + task.getTask().getTask() + " " + task.getCount() + type + task.getPokemon() + progress + complete;
                    break;
                case LEVEL:
                    objective = "&b" + task.getTask().getTask() + " up " + task.getPokemon() + " " + task.getCount() + " " + Utils.pluralize("time", task.getCount()) + progress + complete;
                    break;
                case COLLECT:
                    objective = "&b" + task.getTask().getTask() + " " + task.getCount() + " " + Utils.pluralize(task.getItem().display(),task.getCount()) + progress + complete;
                    break;
                case SHRINE:
                    objective = "&b" + task.getTask().getTask() + " " + task.getCount() + " " + task.getShrine().name() + " shrine" + progress + complete;
                    break;
                case EVOLVE:
                case BREED:
                    objective = "&b" + task.getTask().getTask() + " " + task.getCount() + " " + task.getPokemon() + progress + complete;
                    break;
            }

            itemLore.add(Text.of(Chat.embedColours(objective)));
        }

        if(this.showRewards) {
            itemLore.add(Text.of(Chat.embedColours("")));
            itemLore.add(Text.of(Chat.embedColours("&7-------------- &6REWARDS &7--------------")));
            for (Rewards rewards : this.rewards) itemLore.add(Text.of(Chat.embedColours("&7\u2023 " + rewards.getName())));
        }

        itemLore.add(Text.of(Chat.embedColours("")));
        itemLore.add(Text.of(Chat.embedColours("&7Owner: " + (this.owner != null ? Utils.getNameFromUUID(owner).get() : "None"))));

        scroll.offer(Keys.ITEM_LORE, itemLore);
        scroll.offer(Keys.ITEM_ENCHANTMENTS, Lists.newArrayList(Enchantment.of(EnchantmentTypes.UNBREAKING, 1)));
        scroll.offer(Keys.HIDE_ENCHANTMENTS, true);

        return ItemStack.builder().fromContainer(scroll.toContainer()
                .set(DataQuery.of("UnsafeData","questID"), UUID.randomUUID().toString())
                .set(DataQuery.of("UnsafeData","quest"), new Gson().toJson(this)))
                .build();
    }
}
