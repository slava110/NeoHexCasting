package at.petrak.hexcasting.common.recipe.ingredient.state;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import javax.annotation.Nullable;
import java.util.*;

public class StateIngredientBlockState implements StateIngredient {
    private final BlockState state;

    public StateIngredientBlockState(BlockState state) {
        this.state = state;
    }

    @Override
    public StateIngredientType<?> getType() {
        return StateIngredients.BLOCK_STATE;
    }

    @Override
    public boolean test(BlockState blockState) {
        return this.state == blockState;
    }

    @Override
    public BlockState pick(Random random) {
        return state;
    }

    @Override
    public List<ItemStack> getDisplayedStacks() {
        Block block = state.getBlock();
        if (block.asItem() == Items.AIR) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new ItemStack(block));
    }

    @Nullable
    @Override
    public List<Component> descriptionTooltip() {
        Map<Property<?>, Comparable<?>> map = state.getValues();
        if (map.isEmpty()) {
            return StateIngredient.super.descriptionTooltip();
        }
        List<Component> tooltip = new ArrayList<>(map.size());
        for (Map.Entry<Property<?>, Comparable<?>> entry : map.entrySet()) {
            Property<?> key = entry.getKey();
            @SuppressWarnings({"unchecked", "rawtypes"})
            String name = ((Property) key).getName(entry.getValue());

            tooltip.add(Component.literal(key.getName() + " = " + name).withStyle(ChatFormatting.GRAY));
        }
        return tooltip;
    }

    @Override
    public List<BlockState> getDisplayed() {
        return Collections.singletonList(state);
    }

    public BlockState getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return state == ((StateIngredientBlockState) o).state;
    }

    @Override
    public int hashCode() {
        return state.hashCode();
    }

    @Override
    public String toString() {
        return "StateIngredientBlockState{" + state + "}";
    }


    public static class Type implements StateIngredientType<StateIngredientBlockState> {
        public static final MapCodec<StateIngredientBlockState> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BlockState.CODEC.fieldOf("state").forGetter(StateIngredientBlockState::getState)
        ).apply(instance, StateIngredientBlockState::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, StateIngredientBlockState> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT.map(Block::stateById, Block::getId), StateIngredientBlockState::getState,
                StateIngredientBlockState::new
        );

        @Override
        public MapCodec<StateIngredientBlockState> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, StateIngredientBlockState> streamCodec() {
            return STREAM_CODEC;
        }
    }
}