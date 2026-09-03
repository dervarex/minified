package com.dervarex.minified.worlds.playerdata;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import com.dervarex.minified.utils.nbt.tag.NbtString;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecipeBook {
    // maybe not the best solution but it sure is one
    private String[] recipes = new String[0];
    private String[] toBeDisplayed = new String[0];

    public static RecipeBook fromNbt(NbtCompound nbt) {
        RecipeBook recipeBook = new RecipeBook();

        if (nbt.has("recipes")) {
            NbtList recipesList = nbt.getList("recipes");
            recipeBook.recipes = new String[recipesList.size()];
            for (int i = 0; i < recipesList.size(); i++) {
                NbtString entry = (NbtString) recipesList.elements().get(i);
                recipeBook.recipes[i] = entry.value();
            }
        }

        if (nbt.has("toBeDisplayed")) {
            NbtList displayedList = nbt.getList("toBeDisplayed");
            recipeBook.toBeDisplayed = new String[displayedList.size()];
            for (int i = 0; i < displayedList.size(); i++) {
                NbtString entry = (NbtString) displayedList.elements().get(i);
                recipeBook.toBeDisplayed[i] = entry.value();
            }
        }

        return recipeBook;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();

        NbtList recipesList = new NbtList((byte) 8);
        for (String recipe : recipes) {
            recipesList.add(new NbtString(recipe));
        }
        nbt.setList("recipes", recipesList);

        NbtList toBeDisplayedList = new NbtList((byte) 8);
        for (String recipe : toBeDisplayed) {
            toBeDisplayedList.add(new NbtString(recipe));
        }
        nbt.setList("toBeDisplayed", toBeDisplayedList);

        return nbt;
    }
}