package at.petrak.hexcasting.common.blocks.decoration;

import at.petrak.hexcasting.annotations.SoftImplement;
import at.petrak.hexcasting.common.lib.HexBlockSetTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BlockHexTrapdoor extends TrapDoorBlock {
    public BlockHexTrapdoor(Properties $$0) {
        super(HexBlockSetTypes.EDIFIED_WOOD, $$0);
    }

    @SoftImplement("forge")
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    @SoftImplement("forge")
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 20;
    }

    @SoftImplement("forge")
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }
}
