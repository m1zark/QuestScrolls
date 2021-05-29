package com.m1zark.questscrolls.utils;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.m1zark.m1utilities.api.Inventories;
import com.m1zark.questscrolls.data.Quests;
import com.pixelmonmod.pixelmon.api.spawning.SpawnInfo;
import com.pixelmonmod.pixelmon.api.spawning.SpawnSet;
import com.pixelmonmod.pixelmon.api.spawning.archetypes.entities.pokemon.SpawnInfoPokemon;
import com.pixelmonmod.pixelmon.api.spawning.util.SetLoader;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.service.user.UserStorageService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Utils {
    public static Optional<String> getNameFromUUID(UUID uuid) {
        UserStorageService uss = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
        Optional<User> oUser = uss.get(uuid);

        if (oUser.isPresent()){
            String name = oUser.get().getName();
            return Optional.of(name);
        } else {
            return Optional.empty();
        }
    }

    public static String insertLinebreaks(String s, int charsPerLine) {
        char[] chars = s.toCharArray();
        int lastLinebreak = 0;
        boolean wantLinebreak = false;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            if (wantLinebreak && chars[i] == ' ') {
                sb.append('\n');
                lastLinebreak = i;
                wantLinebreak = false;
            } else {
                sb.append(chars[i]);
            }
            if (i - lastLinebreak + 1 == charsPerLine)
                wantLinebreak = true;
        }
        return sb.toString();
    }

    public static String pluralize(String word, int count) {
        List<String> vowels = Lists.newArrayList("a","e","i","o","u");
        String temp = "";

        if(word.equals("pokemon")) return word;

        if(count > 1) {
            String lastchar = word.substring(word.length() - 1);
            String lastchar2 = word.substring(word.length() - 2);

            switch(lastchar) {
                case "o":
                case "x":
                    temp = word + "es";
                    break;
                case "y":
                    if(vowels.contains(lastchar2)) temp = word + "s";
                    else temp = word.replace(lastchar,"ies");
                    break;
                case "s":
                case "h":
                    if(lastchar2.equals("s")) temp = word + "es";
                    break;
                case "f":
                    if(lastchar2.equals("f")) temp = word + "s";
                    else temp = word.replace(lastchar,"ves");
                    break;
                default:
                    temp = word + "s";
                    break;
            }
        }

        return temp;
    }

    public static void updateQuest(Player player, Quests quest) {
        Inventory inv = player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class)).union(player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class)));

        for (Inventory slot : inv.slots()) {
            ItemStack item = slot.peek().orElse(null);

            if (item == null || item.isEmpty()) continue; // if the slot is empty we skip it

            if(Inventories.doesHaveNBT(item,"quest")) {
                Quests quest2 = new Gson().fromJson(item.toContainer().get(DataQuery.of("UnsafeData","quest")).get().toString(), Quests.class);

                if(quest2.getId().equals(quest.getId())) {
                    slot.set(ItemStack.builder().fromContainer(quest.giveItem().toContainer().set(DataQuery.of("UnsafeData","owner"), player.getUniqueId().toString() + "," + player.getName())).build());
                    break;
                }
            }
        }
    }

    public static void updateOwner(Player player, String id) {
        Inventory inv = player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class)).union(player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class)));

        for (Inventory slot : inv.slots()) {
            ItemStack item = slot.peek().orElse(null);

            if (item == null || item.isEmpty()) continue; // if the slot is empty we skip it

            if(Inventories.doesHaveNBT(item,"quest")) {
                Quests quest = new Gson().fromJson(item.toContainer().get(DataQuery.of("UnsafeData","quest")).get().toString(), Quests.class);
                String qid = item.toContainer().get(DataQuery.of("UnsafeData","questID")).get().toString();

                if(id.equals(qid)) {
                    quest.setOwner(player.getUniqueId());
                    slot.set(ItemStack.builder().fromContainer(quest.giveItem().toContainer().set(DataQuery.of("UnsafeData","owner"), player.getUniqueId().toString() + "," + player.getName())).build());
                    break;
                }
            }
        }
    }

    public static boolean checkSpawning(String pokemon) {
        float rarity = -1.0f;
        for (Object set : SetLoader.getAllSets()) {
            for (SpawnInfo info : ((SpawnSet) set).spawnInfos) {
                if (!(info instanceof SpawnInfoPokemon) || !((SpawnInfoPokemon)info).getPokemonSpec().name.equalsIgnoreCase(EnumSpecies.getFromNameAnyCase(pokemon).name)) continue;
                rarity = (int) info.rarity;
            }
        }

        return rarity <= 0.0f;
    }
}
