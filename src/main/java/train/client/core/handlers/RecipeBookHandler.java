package train.client.core.handlers;

import net.minecraft.item.Item;
import train.common.blocks.TCBlocks;
import train.common.core.interfaces.ITCRecipe;
import train.common.core.managers.TierRecipe;
import train.common.library.BlockIDs;
import train.common.library.ItemIDs;
import train.common.recipes.ShapedTrainRecipes;
import train.common.recipes.ShapelessTrainRecipe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecipeBookHandler {
    /**
     * This is used to show if the recipe can also be crafted in vanilla workbench
     */
    public static String[] vanillaWorkTableRecipes = new String[21];

    public RecipeBookHandler() {
        vanillaWorkTableRecipes[0] = TCBlocks.assemblyTableI.getUnlocalizedName();
        vanillaWorkTableRecipes[1] = TCBlocks.assemblyTableII.getUnlocalizedName();
        vanillaWorkTableRecipes[2] = TCBlocks.assemblyTableIII.getUnlocalizedName();
        vanillaWorkTableRecipes[3] = TCBlocks.distilIdle.getUnlocalizedName();
        vanillaWorkTableRecipes[4] = TCBlocks.openFurnaceIdle.getUnlocalizedName();
        vanillaWorkTableRecipes[5] = TCBlocks.trainWorkbench.getUnlocalizedName();
        vanillaWorkTableRecipes[6] = ItemIDs.overalls.item.getUnlocalizedName();
        vanillaWorkTableRecipes[7] = ItemIDs.jacket.item.getUnlocalizedName();
        vanillaWorkTableRecipes[8] = ItemIDs.hat.item.getUnlocalizedName();
        vanillaWorkTableRecipes[11] = ItemIDs.recipeBook.item.getUnlocalizedName();
        vanillaWorkTableRecipes[12] = TCBlocks.lantern.getUnlocalizedName();
        vanillaWorkTableRecipes[14] = ItemIDs.pants_driver_paintable.item.getUnlocalizedName();
        vanillaWorkTableRecipes[15] = ItemIDs.pants_ticketMan_paintable.item.getUnlocalizedName();
        vanillaWorkTableRecipes[16] = ItemIDs.hat_driver_paintable.item.getUnlocalizedName();
        vanillaWorkTableRecipes[17] = ItemIDs.hat_ticketMan_paintable.item.getUnlocalizedName();
        vanillaWorkTableRecipes[18] = ItemIDs.jacket_driver_paintable.item.getUnlocalizedName();
        vanillaWorkTableRecipes[19] = ItemIDs.jacket_ticketMan_paintable.item.getUnlocalizedName();
        vanillaWorkTableRecipes[20] = TCBlocks.switchStand.getUnlocalizedName();
    }

    // TODO: Make parameters more specific than List
    public static List<ITCRecipe> workbenchListCleaner(List<?> recipeList) {
        Set<String> outputs = new HashSet<>();
        List<ITCRecipe> cleaned = new ArrayList<>();
        for (Object r : recipeList) {
            if (r instanceof ShapedTrainRecipes || r instanceof ShapelessTrainRecipe) {
                ITCRecipe recipe = (ITCRecipe) r;
                String output = Item.itemRegistry.getNameForObject(recipe.getRecipeOutput().getItem());
                if (!outputs.contains(output)) {
                    cleaned.add(recipe);
                    outputs.add(output);
                }
            }
        }
        return cleaned;
    }

    // TODO: Make it more generic: TierRecipe -> ITierRecipe
    public static List<TierRecipe> assemblyListCleaner(List<?> recipeList) {
        Set<String> outputs = new HashSet<>();
        List<TierRecipe> cleanedList = new ArrayList<>();
        for (Object r : recipeList) {
            if (r instanceof TierRecipe) {
                TierRecipe recipe = (TierRecipe) r;
                String output = Item.itemRegistry.getNameForObject(recipe.getOutput().getItem());
                if (!outputs.contains(output)) {
                    cleanedList.add(recipe);
                    outputs.add(output);
                }
            }
        }
        return cleanedList;
    }
}