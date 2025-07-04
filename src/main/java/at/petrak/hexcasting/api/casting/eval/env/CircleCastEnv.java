package at.petrak.hexcasting.api.casting.eval.env;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.PatternShapeMatch;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.circles.CircleExecutionState;
import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.MishapEnvironment;
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.casting.mishaps.MishapDisallowedSpell;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.lib.HexAttributes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class CircleCastEnv extends CastingEnvironment {
    protected final CircleExecutionState execState;

    public CircleCastEnv(ServerLevel world, CircleExecutionState execState) {
        super(world);
        this.execState = execState;
    }

    @Override
    public @Nullable LivingEntity getCastingEntity() {
        return this.execState.getCaster(this.world);
    }

    @Override
    public @Nullable ServerPlayer getCaster() {
        return this.execState.getCaster(this.world);
    }

    public @Nullable BlockEntityAbstractImpetus getImpetus() {
        var entity = this.world.getBlockEntity(execState.impetusPos);

        if (entity instanceof BlockEntityAbstractImpetus)
            return (BlockEntityAbstractImpetus) entity;
        return null;
    }

    public CircleExecutionState circleState() {
        return execState;
    }

    @Override
    public MishapEnvironment getMishapEnvironment() {
        return new CircleMishapEnv(this.world, this.execState);
    }

    @Override
    public void precheckAction(PatternShapeMatch match) throws Mishap {
        super.precheckAction(match);

        ResourceLocation key = actionKey(match);

        if (!HexConfig.server().isActionAllowedInCircles(key)) {
            throw new MishapDisallowedSpell("disallowed_circle");
        }
    }

    @Override
    public void postExecution(CastResult result) {
        super.postExecution(result);

        // we always want to play this sound one at a time
        var sound = result.getSound().sound();
        if (sound != null) {
            var soundPos = this.execState.currentPos;
            this.world.playSound(null, soundPos, sound, SoundSource.PLAYERS, 1f, 1f);
        }

        // TODO: this is gonna bite us in the bum someday
        // we check whether we should cut the execution in BlockSlate, but post the mishap here;
        // although everything should be pretty immutable here it's something to keep in mind
        // classic time-of-check/time-of-use
        var imp = this.getImpetus();
        if (imp != null) {
            for (var sideEffect : result.getSideEffects()) {
                if (sideEffect instanceof OperatorSideEffect.DoMishap doMishap) {
                    var msg = doMishap.getMishap().errorMessageWithName(this, doMishap.getErrorCtx());
                    if (msg != null) {
                        imp.postMishap(msg);
                    }
                }
            }
        }
    }

    @Override
    public Vec3 mishapSprayPos() {
        return Vec3.atCenterOf(this.execState.currentPos);
    }

    @Override
    public long extractMediaEnvironment(long cost, boolean simulate) {
        var entity = this.getImpetus();
        if (entity == null)
            return cost;

        var mediaAvailable = entity.getMedia();
        if (mediaAvailable < 0)
            return 0;

        long mediaToTake = Math.min(cost, mediaAvailable);
        cost -= mediaToTake;
        if (!simulate) {
            entity.setMedia(mediaAvailable - mediaToTake);
        }

        return cost;
    }

    @Override
    public boolean isVecInRangeEnvironment(Vec3 vec) {
        var caster = this.execState.getCaster(this.world);
        if (caster != null) {
            double sentinelRadius = caster.getAttributeValue(HexAttributes.SENTINEL_RADIUS);
            if (vec.distanceToSqr(caster.position()) <= caster.getBbHeight() * caster.getBbHeight()) {
                return true;
            }

            var sentinel = HexAPI.instance().getSentinel(caster);
            if (sentinel != null
                && sentinel.extendsRange()
                && caster.level().dimension() == sentinel.dimension()
                && vec.distanceToSqr(sentinel.position()) <= sentinelRadius * sentinelRadius + 0.00000000001
            ) {
                return true;
            }
        }

        return this.execState.bounds.contains(vec);
    }

    @Override
    public boolean isEnlightened() {
        // have unbound circles be enlightened.
        if(getCastingEntity() == null) return true;
        return super.isEnlightened();
    }

    @Override
    public boolean hasEditPermissionsAtEnvironment(BlockPos pos) {
        return true;
    }

    @Override
    public InteractionHand getCastingHand() {
        return InteractionHand.MAIN_HAND;
    }

    @Override
    // TODO: Could do something like get items in inventories adjacent to the circle?
    protected List<ItemStack> getUsableStacks(StackDiscoveryMode mode) {
        if (this.getCaster() != null)
            return getUsableStacksForPlayer(mode, null, this.getCaster());
        return new ArrayList<>();
    }

    @Override
    // TODO: Adjacent inv!
    protected List<HeldItemInfo> getPrimaryStacks() {
        if (this.getCaster() != null)
            return getPrimaryStacksForPlayer(InteractionHand.OFF_HAND, this.getCaster());
        return List.of();
    }

    @Override
    // TODO: Adjacent inv!
    public boolean replaceItem(Predicate<ItemStack> stackOk, ItemStack replaceWith, @Nullable InteractionHand hand) {
        if (this.getCaster() != null)
            return replaceItemForPlayer(stackOk, replaceWith, hand, this.getCaster());
        return false;
    }

    @Override
    public FrozenPigment getPigment() {
        var impetus = this.getImpetus();
        if (impetus == null)
            return FrozenPigment.DEFAULT.get();
        return impetus.getPigment();
    }

    @Override
    public @Nullable FrozenPigment setPigment(@Nullable FrozenPigment pigment) {
        var impetus = this.getImpetus();
        if (impetus == null)
            return null;
        return impetus.setPigment(pigment);
    }

    @Override
    public void produceParticles(ParticleSpray particles, FrozenPigment pigment) {
        particles.sprayParticles(this.world, pigment);
    }

    @Override
    public void printMessage(Component message) {
        var impetus = getImpetus();
        if (impetus == null)
            return;
        impetus.postPrint(message);
    }
}
