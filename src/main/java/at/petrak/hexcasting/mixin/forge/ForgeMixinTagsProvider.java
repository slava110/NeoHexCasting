package at.petrak.hexcasting.mixin.forge;

import at.petrak.hexcasting.forge.datagen.TagsProviderEFHSetter;
import net.minecraft.data.tags.TagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TagsProvider.class)
public abstract class ForgeMixinTagsProvider implements TagsProviderEFHSetter {
    @Final
    @Shadow(remap = false)
    protected ExistingFileHelper existingFileHelper;

    private ExistingFileHelper actualFileHelper = null;

    @Override
    public void setEFH(ExistingFileHelper efh) {
        actualFileHelper = efh;
    }

    @Redirect(method = "missing(Lnet/minecraft/tags/TagEntry;)Z", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/data/tags/TagsProvider;existingFileHelper:Lnet/neoforged/neoforge/common/data/ExistingFileHelper;",
            opcode = Opcodes.GETFIELD),
            remap = false)
    private ExistingFileHelper hex$missingRedirect(TagsProvider instance) {
        if (actualFileHelper == null)
            return existingFileHelper;
        return actualFileHelper;
    }

    @Redirect(method = "getOrCreateRawBuilder", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/data/tags/TagsProvider;existingFileHelper:Lnet/neoforged/neoforge/common/data/ExistingFileHelper;",
            opcode = Opcodes.GETFIELD),
            remap = false)
    private ExistingFileHelper hex$getOrCreateRawBuilderRedirect(TagsProvider instance) {
        if (actualFileHelper == null)
            return existingFileHelper;
        return actualFileHelper;
    }
}
