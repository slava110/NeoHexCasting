package at.petrak.hexcasting.common.blocks.entity;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.blocks.BlockConjured;
import at.petrak.hexcasting.common.blocks.BlockConjuredLight;
import at.petrak.hexcasting.common.lib.HexBlockEntities;
import at.petrak.hexcasting.common.particles.ConjureParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class BlockEntityConjured extends HexBlockEntity {
    private static final Random RANDOM = new Random();
    private FrozenPigment colorizer = FrozenPigment.DEFAULT.get();

    public static final String TAG_COLORIZER = "tag_colorizer";

    public BlockEntityConjured(BlockPos pos, BlockState state) {
        super(HexBlockEntities.CONJURED_TILE, pos, state);
    }

    public void walkParticle(Entity pEntity) {
        if (getBlockState().getBlock() instanceof BlockConjured conjured && !(conjured instanceof BlockConjuredLight)) {
            var colProvider = this.colorizer.getColorProvider();
            for (int i = 0; i < 3; ++i) {
                int color = colProvider.getColor(pEntity.tickCount, pEntity.position()
                    .add(new Vec3(RANDOM.nextFloat(), RANDOM.nextFloat(), RANDOM.nextFloat()).scale(
                        RANDOM.nextFloat() * 3)));
                assert level != null;
                level.addParticle(new ConjureParticleOptions(color),
                    pEntity.getX() + (RANDOM.nextFloat() * 0.6D) - 0.3D,
                    getBlockPos().getY() + (RANDOM.nextFloat() * 0.05D) + 0.95D,
                    pEntity.getZ() + (RANDOM.nextFloat() * 0.6D) - 0.3D,
                    RANDOM.nextFloat(-0.02f, 0.02f),
                    RANDOM.nextFloat(0.02f),
                    RANDOM.nextFloat(-0.02f, 0.02f));
            }
        }
    }

    public void particleEffect() {
        if (getBlockState().getBlock() instanceof BlockConjured) {
            int color = this.colorizer.getColorProvider().getColor(RANDOM.nextFloat() * 16384,
                new Vec3(RANDOM.nextFloat(), RANDOM.nextFloat(), RANDOM.nextFloat()).scale(
                    RANDOM.nextFloat() * 3));
            assert level != null;
            if (getBlockState().getBlock() instanceof BlockConjuredLight) {
                if (RANDOM.nextFloat() < 0.5) {
                    level.addParticle(new ConjureParticleOptions(color),
                        (double) getBlockPos().getX() + 0.45D + (RANDOM.nextFloat() * 0.1D),
                        (double) getBlockPos().getY() + 0.45D + (RANDOM.nextFloat() * 0.1D),
                        (double) getBlockPos().getZ() + 0.45D + (RANDOM.nextFloat() * 0.1D),
                        RANDOM.nextFloat(-0.005f, 0.005f),
                        RANDOM.nextFloat(-0.002f, 0.02f),
                        RANDOM.nextFloat(-0.005f, 0.005f));
                }
            } else {
                if (RANDOM.nextFloat() < 0.2) {
                    level.addParticle(new ConjureParticleOptions(color),
                        (double) getBlockPos().getX() + RANDOM.nextFloat(),
                        (double) getBlockPos().getY() + RANDOM.nextFloat(),
                        (double) getBlockPos().getZ() + RANDOM.nextFloat(),
                        RANDOM.nextFloat(-0.02f, 0.02f),
                        RANDOM.nextFloat(-0.02f, 0.02f),
                        RANDOM.nextFloat(-0.02f, 0.02f));
                }
            }
        }
    }

    public void landParticle(Entity entity, int number) {
        var colProvider = this.colorizer.getColorProvider();
        for (int i = 0; i < number * 2; i++) {
            int color = colProvider.getColor(entity.tickCount, entity.position()
                .add(new Vec3(RANDOM.nextFloat(), RANDOM.nextFloat(), RANDOM.nextFloat()).scale(
                    RANDOM.nextFloat() * 3)));
            assert level != null;
            level.addParticle(new ConjureParticleOptions(color),
                entity.getX() + (RANDOM.nextFloat() * 0.8D) - 0.2D,
                getBlockPos().getY() + (RANDOM.nextFloat() * 0.05D) + 0.95D,
                entity.getZ() + (RANDOM.nextFloat() * 0.8D) - 0.2D,
                0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void saveModData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put(TAG_COLORIZER, FrozenPigment.CODEC.encodeStart(NbtOps.INSTANCE, colorizer).getOrThrow());
    }

    @Override
    protected void loadModData(CompoundTag tag, HolderLookup.Provider registries) {
        this.colorizer = FrozenPigment.CODEC.parse(NbtOps.INSTANCE, tag.getCompound(TAG_COLORIZER)).getOrThrow();
    }

    public FrozenPigment getColorizer() {
        return this.colorizer;
    }

    public void setColorizer(FrozenPigment colorizer) {
        this.colorizer = colorizer;
        this.sync();
    }
}
