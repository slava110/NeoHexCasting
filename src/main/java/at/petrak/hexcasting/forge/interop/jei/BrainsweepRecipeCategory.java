package at.petrak.hexcasting.forge.interop.jei;

import at.petrak.hexcasting.client.ClientTickCounter;
import at.petrak.hexcasting.common.casting.actions.spells.OpEdifySapling;
import at.petrak.hexcasting.common.recipe.BrainsweepRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static at.petrak.hexcasting.api.HexAPI.modLoc;
import static at.petrak.hexcasting.client.render.RenderLib.renderEntity;

public class BrainsweepRecipeCategory implements IRecipeCategory<BrainsweepRecipe> {
    public static final ResourceLocation UID = modLoc("brainsweep");

    private final IDrawableStatic background;
    private final IDrawable icon;
    private final Component localizedName;

    public BrainsweepRecipeCategory(IGuiHelper guiHelper) {
        ResourceLocation location = modLoc("textures/gui/brainsweep_jei.png");
        background = guiHelper.drawableBuilder(location, 0, 0, 118, 86).setTextureSize(128, 128).build();
        var brainsweep = modLoc("brainsweep");
        localizedName = Component.translatable("hexcasting.action." + brainsweep);
        icon = new PatternDrawable(brainsweep, 16, 16);
    }

    @Override
    public @NotNull
    Component getTitle() {
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
    public @NotNull
    IDrawable getIcon() {
        return icon;
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, BrainsweepRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (37 <= mouseX && mouseX <= 37 + 26 && 19 <= mouseY && mouseY <= 19 + 48) {
            Minecraft mc = Minecraft.getInstance();
            tooltip.addAll(recipe.entityIn().getTooltip(mc.options.advancedItemTooltips));
        }
    }

    @Override
    public void draw(@NotNull BrainsweepRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics graphics, double mouseX, double mouseY) {
        background.draw(graphics);
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            var example = recipe.entityIn().exampleEntity(level);
            if (example == null)
                return;

            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            renderEntity(graphics, example, level, 50, 62.5f, ClientTickCounter.getTotal(), 20, 0);
        }
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull BrainsweepRecipe recipe,
        @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 12, 35)
            .addItemStacks(recipe.blockIn().getDisplayedStacks());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 87, 35)
            .addItemStack(new ItemStack(recipe.result().getBlock()));
    }

    @Override
    public @NotNull
    RecipeType<BrainsweepRecipe> getRecipeType() {
        return HexJEIPlugin.BRAINSWEEPING;
    }
}
