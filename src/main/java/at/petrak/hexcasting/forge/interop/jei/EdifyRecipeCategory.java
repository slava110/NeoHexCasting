package at.petrak.hexcasting.forge.interop.jei;

import at.petrak.hexcasting.common.casting.actions.spells.OpEdifySapling;
import at.petrak.hexcasting.common.lib.HexBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class EdifyRecipeCategory implements IRecipeCategory<OpEdifySapling> {
    public static final ResourceLocation UID = modLoc("edify_tree");

    private final IDrawableStatic background;
    private final IDrawable icon;
    private final Component localizedName;

    public EdifyRecipeCategory(IGuiHelper guiHelper) {
        ResourceLocation location = modLoc("textures/gui/edify_jei.png");
        background = guiHelper.drawableBuilder(location, 0, 0, 79, 61).setTextureSize(128, 128).build();
        var edify = modLoc("edify");
        localizedName = Component.translatable("hexcasting.action." + edify);
        icon = new PatternDrawable(edify, 16, 16).strokeOrder(false);
    }

    @Override
    public @NotNull Component getTitle() {
        return localizedName;
    }

    @Override
    public int getWidth() {
        return background.getWidth();
    }

    @Override
    public int getHeight() {
        return background.getHeight();
    }

    @Override
    public void draw(OpEdifySapling recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull OpEdifySapling recipe,
        @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 12, 22)
            .addIngredients(Ingredient.of(ItemTags.SAPLINGS));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 51, 10)
            .addItemStack(new ItemStack(HexBlocks.AMETHYST_EDIFIED_LEAVES))
            .addItemStack(new ItemStack(HexBlocks.AVENTURINE_EDIFIED_LEAVES))
            .addItemStack(new ItemStack(HexBlocks.CITRINE_EDIFIED_LEAVES));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 51, 35)
            .addItemStack(new ItemStack(HexBlocks.EDIFIED_LOG))
            .addItemStack(new ItemStack(HexBlocks.EDIFIED_LOG_AMETHYST))
            .addItemStack(new ItemStack(HexBlocks.EDIFIED_LOG_AVENTURINE))
            .addItemStack(new ItemStack(HexBlocks.EDIFIED_LOG_CITRINE));
//            .addItemStack(new ItemStack(HexBlocks.EDIFIED_LOG_PURPLE));

    }

    @Override
    public @NotNull RecipeType<OpEdifySapling> getRecipeType() {
        return HexJEIPlugin.EDIFY;
    }
}
