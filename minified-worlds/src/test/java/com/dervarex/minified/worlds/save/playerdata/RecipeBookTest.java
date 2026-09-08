package com.dervarex.minified.worlds.save.playerdata;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecipeBookTest {

    @Test
    void testLoadEmptyData() {
        RecipeBook recipeBook = RecipeBook.fromNbt(new NbtCompound());
        assertEquals(0, recipeBook.getRecipes().length);
        assertEquals(0, recipeBook.getToBeDisplayed().length);
    }

    @Test
    void testSaveAndLoadRecipeBook() {
        RecipeBook original = new RecipeBook();
        original.setRecipes(new String[]{"minecraft:stick", "minecraft:torch"});
        original.setToBeDisplayed(new String[]{"minecraft:torch"});

        RecipeBook parsed = RecipeBook.fromNbt(original.toNbt());

        assertArrayEquals(original.getRecipes(), parsed.getRecipes());
        assertArrayEquals(original.getToBeDisplayed(), parsed.getToBeDisplayed());
    }
}