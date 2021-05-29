package com.m1zark.questscrolls.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.*;

@Getter
@AllArgsConstructor
public class Rewards {
    private final Integer index;
    private final String type;
    private final String id;
    private final String command;
    private final String name;

    private final Integer meta;
    private final Map nbt;
    private final boolean unbreakable;
    private List<String> lore;
    private List<Enchantment> enchantments;
    private final String sprite_data;
    private final Integer cost;
    private final Integer count;

    public ItemStack parseItem() {
        Optional<ItemType> itemType = Sponge.getGame().getRegistry().getType(ItemType.class, this.id);
        if(itemType.isPresent()) {
            ItemStack stack = ItemStack.of(itemType.get(), 1);
            DataContainer container = stack.toContainer();

            if(this.meta != null) container.set(DataQuery.of("UnsafeDamage"), this.meta);
            if(this.unbreakable) {
                container.set(DataQuery.of("UnsafeData","Unbreakable"), 1);
                container.set(DataQuery.of("UnsafeData","HideFlags"), 63);
            }

            if(!this.lore.isEmpty()) {
                ArrayList<Text> realLore = new ArrayList<>();
                for(String line : this.lore) realLore.add(TextSerializers.FORMATTING_CODE.deserialize(line));
                stack.offer(Keys.ITEM_LORE, realLore);
            }

            if(!this.enchantments.isEmpty()) stack.offer(Keys.ITEM_ENCHANTMENTS, enchantments);

            if(!this.nbt.isEmpty()){
                if(container.get(DataQuery.of("UnsafeData")).isPresent()) {
                    Map real = (container.getMap(DataQuery.of("UnsafeData")).get());
                    this.nbt.putAll(real);
                }
                container.set(DataQuery.of("UnsafeData"),this.nbt);
            }

            if(this.sprite_data != null) container.set(DataQuery.of("UnsafeData","SpriteName"), this.sprite_data);

            stack = ItemStack.builder().fromContainer(container).build();
            return stack;
        } else {
            return null;
        }
    }

    public String parseCommand(Player player) {
        StringBuilder cmd = new StringBuilder();
        String[] parts = this.command.split(" ");
        for (String part : parts) {
            if (part.contains("{p}")) part = part.replace(part, player.getName());
            cmd.append(" ").append(part);
        }
        return cmd.substring(1);
    }
}
