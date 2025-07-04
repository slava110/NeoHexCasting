package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.client.ClientTickCounter;
import at.petrak.hexcasting.common.particles.ConjureParticleOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

/**
 * Sent server->client to spray particles everywhere.
 */
public record MsgCastParticleS2C(ParticleSpray spray, FrozenPigment colorizer) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MsgCastParticleS2C> TYPE = new CustomPacketPayload.Type<>(HexAPI.modLoc("cprtcl"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgCastParticleS2C> STREAM_CODEC = StreamCodec.composite(
            ParticleSpray.getSTREAM_CODEC(), MsgCastParticleS2C::spray,
            FrozenPigment.STREAM_CODEC, MsgCastParticleS2C::colorizer,
            MsgCastParticleS2C::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static final Random RANDOM = new Random();

    // https://math.stackexchange.com/questions/44689/how-to-find-a-random-axis-or-unit-vector-in-3d
    private static Vec3 randomInCircle(double maxTh) {
        var th = RANDOM.nextDouble(0.0, maxTh + 0.001);
        var z = RANDOM.nextDouble(-1.0, 1.0);
        return new Vec3(Math.sqrt(1.0 - z * z) * Math.cos(th), Math.sqrt(1.0 - z * z) * Math.sin(th), z);
    }

    public void handle() {
        Handler.handle(this);
    }

    public static final class Handler {

        public static void handle(MsgCastParticleS2C msg) {
            Minecraft.getInstance().execute(() -> {
                var colProvider = msg.colorizer().getColorProvider();
                for (int i = 0; i < msg.spray().getCount(); i++) {
                    // For the colors, pick any random time to get a mix of colors

                    var offset = randomInCircle(Mth.TWO_PI).normalize()
                            .scale(RANDOM.nextFloat() * msg.spray().getFuzziness() / 2);
                    var pos = msg.spray().getPos().add(offset);

                    var phi = Math.acos(1.0 - RANDOM.nextDouble() * (1.0 - Math.cos(msg.spray().getSpread())));
                    var theta = Math.PI * 2.0 * RANDOM.nextDouble();
                    var v = msg.spray().getVel().normalize();
                    // pick any old vector to get a vector normal to v with
                    Vec3 k;
                    if (v.x == 0.0 && v.y == 0.0) {
                        // oops, pick a *different* normal
                        k = new Vec3(1.0, 0.0, 0.0);
                    } else {
                        k = v.cross(new Vec3(0.0, 0.0, 1.0));
                    }
                    var velUnlen = v.scale(Math.cos(phi))
                            .add(k.scale(Math.sin(phi) * Math.cos(theta)))
                            .add(v.cross(k).scale(Math.sin(phi) * Math.sin(theta)));
                    var vel = velUnlen.scale(msg.spray().getVel().length() / 20);

                    var color = colProvider.getColor(ClientTickCounter.getTotal(), velUnlen);

                    Minecraft.getInstance().level.addParticle(
                            new ConjureParticleOptions(color),
                            pos.x, pos.y, pos.z,
                            vel.x, vel.y, vel.z
                    );
                }
            });
        }
    }
}
