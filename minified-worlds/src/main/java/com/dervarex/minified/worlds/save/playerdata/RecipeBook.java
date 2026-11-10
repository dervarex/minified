package com.dervarex.minified.worlds.save.playerdata;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import com.dervarex.minified.utils.nbt.tag.NbtString;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apiguardian.api.API;

/**
 * The recipes a player has unlocked
 */
@Getter
@Setter
@API(status = API.Status.STABLE)
public class RecipeBook {
    /**
     * Creates an empty recipe book
     */
    public RecipeBook() {
    }

    // maybe not the best solution but it sure is one
    /** The unlocked recipes */
    private String[] recipes = new String[0];
    /** The recipes that are still highlighted as new */
    private String[] toBeDisplayed = new String[0];

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private NbtCompound raw;

    /**
     * Reads the recipe book from NBT
     * @param nbt the player's recipeBook compound
     * @return the recipe book
     */
    public static RecipeBook fromNbt(NbtCompound nbt) {
        RecipeBook recipeBook = new RecipeBook();
        recipeBook.raw = nbt.copy();

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

    /** {@return the recipe book as NBT} */
    public NbtCompound toNbt() {
        NbtCompound nbt = raw != null ? raw.copy() : new NbtCompound();

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