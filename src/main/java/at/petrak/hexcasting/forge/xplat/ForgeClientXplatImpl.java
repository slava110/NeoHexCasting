package at.petrak.hexcasting.forge.xplat;

import at.petrak.hexcasting.api.client.ClientCastingStack;
import at.petrak.hexcasting.forge.lib.ForgeHexAttachments;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

public class ForgeClientXplatImpl implements IClientXplatAbstractions {
    @Override
    public void sendPacketToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }

    @Override
    public void setRenderLayer(Block block, RenderType type) {
        // For forge, handled in block models
//        ItemBlockRenderTypes.setRenderLayer(block, type);
    }

    @Override
    public void initPlatformSpecific() {
        // NO-OP
    }

    @Override
    public <T extends Entity> void registerEntityRenderer(EntityType<? extends T> type,
        EntityRendererProvider<T> renderer) {
        EntityRenderers.register(type, renderer);
    }

    @Override
    public void registerItemProperty(Item item, ResourceLocation id, ItemPropertyFunction func) {
        ItemProperties.register(item, id, func);
    }

    @Override
    public ClientCastingStack getClientCastingStack(Player player) {
        return player.getData(ForgeHexAttachments.CLIENT_CASTING_STACK);
    }

    @Override
    public void setFilterSave(AbstractTexture texture, boolean filter, boolean mipmap) {
        texture.setBlurMipmap(filter, mipmap);
    }

    @Override
    public void restoreLastFilter(AbstractTexture texture) {
        texture.restoreLastBlurMipmap();
    }

    @Override
    public boolean fabricAdditionalQuenchFrustumCheck(AABB aabb) {
        return true; // forge fixes this with a patch so we just say "yep"
    }
}
