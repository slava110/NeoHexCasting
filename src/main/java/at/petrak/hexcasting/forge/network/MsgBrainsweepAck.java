package at.petrak.hexcasting.forge.network;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

/**
 * Sent server->client to synchronize the status of a brainswept mob.
 */
public record MsgBrainsweepAck(int target) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MsgBrainsweepAck> TYPE = new CustomPacketPayload.Type<>(HexAPI.modLoc("sweep"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgBrainsweepAck> STREAM_CODEC = ByteBufCodecs.INT.map(
            MsgBrainsweepAck::new,
            MsgBrainsweepAck::target
    ).mapStream(b -> b);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static MsgBrainsweepAck of(Entity target) {
        return new MsgBrainsweepAck(target.getId());
    }

    public void handle() {
        Handler.handle(this);
    }

    public static final class Handler {

        public static void handle(MsgBrainsweepAck msg) {
            Minecraft.getInstance().execute(() -> {
                var level = Minecraft.getInstance().level;
                if (level != null) {
                    Entity entity = level.getEntity(msg.target());
                    if (entity instanceof Mob living) {
                        IXplatAbstractions.INSTANCE.setBrainsweepAddlData(living);
                    }
                }
            });
        }
    }
}
