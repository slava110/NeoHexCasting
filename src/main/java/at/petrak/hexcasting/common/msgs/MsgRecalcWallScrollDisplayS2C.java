package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.entities.EntityWallScroll;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Sent S->C to have a wall scroll recalculate its pattern, to get readability offset.
 */
public record MsgRecalcWallScrollDisplayS2C(int entityId, boolean showStrokeOrder) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MsgRecalcWallScrollDisplayS2C> TYPE = new CustomPacketPayload.Type<>(HexAPI.modLoc("redoscroll"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgRecalcWallScrollDisplayS2C> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MsgRecalcWallScrollDisplayS2C::entityId,
            ByteBufCodecs.BOOL, MsgRecalcWallScrollDisplayS2C::showStrokeOrder,
            MsgRecalcWallScrollDisplayS2C::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle() {
        Handler.handle(this);
    }

    public static final class Handler {

        public static void handle(MsgRecalcWallScrollDisplayS2C msg) {
            Minecraft.getInstance().execute(() -> {
                var mc = Minecraft.getInstance();
                var entity = mc.level.getEntity(msg.entityId);
                if (entity instanceof EntityWallScroll scroll
                        && scroll.getShowsStrokeOrder() != msg.showStrokeOrder) {
                    scroll.recalculateDisplay();
                }
            });
        }
    }
}
