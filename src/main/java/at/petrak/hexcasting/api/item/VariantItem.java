package at.petrak.hexcasting.api.item;

import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import net.minecraft.world.item.ItemStack;

/**
 * Items that have multiple different otherwise identical visual variants can implement this interface.
 * <p>
 * On both the Forge and Fabric sides, the registry will be scanned for all items which implement this interface,
 * and the appropriate cap/CC will be attached.
 */
public interface VariantItem {

    int numVariants();

    default int getVariant(ItemStack stack) {
        var variant = stack.get(HexDataComponents.VARIANT);
        return variant != null ? variant : 0;
    }

    default void setVariant(ItemStack stack, int variant) {
        stack.set(HexDataComponents.VARIANT, clampVariant(variant));
    }

    default int clampVariant(int variant) {
        if (variant < 0)
            return 0;
        if (variant >= numVariants())
            return numVariants() - 1;
        return variant;
    }
}
