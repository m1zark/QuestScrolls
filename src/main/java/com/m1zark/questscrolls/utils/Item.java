package com.m1zark.questscrolls.utils;

import com.google.common.collect.Lists;
import com.m1zark.m1utilities.api.Inventories;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@AllArgsConstructor
public class Item {
    private final Integer index;
    private final String id;
    private final String display;

    private final Integer meta;
    private final boolean unbreakable;
    private final Map nbt;
    private List<String> lore;
    List<Enchantment> enchantments;

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

            if(!enchantments.isEmpty()) stack.offer(Keys.ITEM_ENCHANTMENTS, enchantments);

            if(!this.nbt.isEmpty()){
                if(container.get(DataQuery.of("UnsafeData")).isPresent()) {
                    Map real = (container.getMap(DataQuery.of("UnsafeData")).get());
                    this.nbt.putAll(real);
                }
                container.set(DataQuery.of("UnsafeData"),this.nbt);
            }

            stack = ItemStack.builder().fromContainer(container).build();
            return stack;
        } else {
            return null;
        }
    }

    public String display() {
        return display != null ? display : Inventories.getItemName(this.parseItem()).toPlain();
    }
}
