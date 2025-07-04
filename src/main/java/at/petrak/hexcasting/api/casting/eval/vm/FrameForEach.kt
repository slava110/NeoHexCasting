package at.petrak.hexcasting.api.casting.eval.vm

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerLevel
import java.util.*
import kotlin.jvm.optionals.getOrNull

/**
 * A frame representing all the state for a Thoth evaluation.
 * Pushed by an OpForEach.
 * @property first whether the input stack state is the first one (since we don't want to save the base-stack before any changes are made)
 * @property data list of *remaining* datums to ForEach over
 * @property code code to run per datum
 * @property baseStack the stack state at Thoth entry
 * @property acc concatenated list of final stack states after Thoth exit
 */
data class FrameForEach(
    val data: SpellList,
    val code: SpellList,
    val baseStack: List<Iota>?,
    val acc: MutableList<Iota>
) : ContinuationFrame {

    /** When halting, we add the stack state at halt to the stack accumulator, then return the original pre-Thoth stack, plus the accumulator. */
    override fun breakDownwards(stack: List<Iota>): Pair<Boolean, List<Iota>> {
        val newStack = baseStack?.toMutableList() ?: mutableListOf()
        acc.addAll(stack)
        newStack.add(ListIota(acc))
        return true to newStack
    }

    /** Step the Thoth computation, enqueueing one code evaluation. */
    override fun evaluate(
        continuation: SpellContinuation,
        level: ServerLevel,
        harness: CastingVM
    ): CastResult {
        // If this isn't the very first Thoth step (i.e. no Thoth computations run yet)...
        val stack = if (baseStack == null) {
            // init stack to the VM stack...
            harness.image.stack.toList()
        } else {
            // else save the stack to the accumulator and reuse the saved base stack.
            acc.addAll(harness.image.stack)
            baseStack
        }

        // If we still have data to process...
        val (stackTop, newImage, newCont) = if (data.nonEmpty) {
            // push the next datum to the top of the stack,
            val cont2 = continuation
                // put the next Thoth object back on the stack for the next Thoth cycle,
                .pushFrame(FrameForEach(data.cdr, code, stack, acc))
                // and prep the Thoth'd code block for evaluation.
                .pushFrame(FrameEvaluate(code, true))
            Triple(data.car, harness.image.withUsedOp(), cont2)
        } else {
            // Else, dump our final list onto the stack.
            Triple(ListIota(acc), harness.image, continuation)
        }
        val tStack = stack.toMutableList()
        tStack.add(stackTop)
        return CastResult(
            ListIota(code),
            newCont,
            // reset escapes so they don't carry over to other iterations or out of thoth
            newImage.withResetEscape().copy(stack = tStack),
            listOf(),
            ResolvedPatternType.EVALUATED,
            HexEvalSounds.THOTH,
        )
    }

    override fun size() = data.size() + code.size() + acc.size + (baseStack?.size ?: 0)

    override val type: ContinuationFrame.Type<*> = TYPE

    companion object {
        @JvmField
        val TYPE: ContinuationFrame.Type<FrameForEach> = object : ContinuationFrame.Type<FrameForEach> {
            val CODEC = RecordCodecBuilder.mapCodec<FrameForEach> { inst ->
                inst.group(
                    SpellList.CODEC.fieldOf("data").forGetter { it.data },
                    SpellList.CODEC.fieldOf("code").forGetter { it.code },
                    IotaType.TYPED_CODEC.listOf().optionalFieldOf("base").forGetter { Optional.ofNullable(it.baseStack) },
                    IotaType.TYPED_CODEC.listOf().fieldOf("accumulator").forGetter { it.acc }
                ).apply(inst) { a, b, c, d ->
                    FrameForEach(a, b, c.getOrNull(), d)
                }
            }
            val STREAM_CODEC = StreamCodec.composite(
                SpellList.STREAM_CODEC, FrameForEach::data,
                SpellList.STREAM_CODEC, FrameForEach::code,
                ByteBufCodecs.optional(IotaType.TYPED_STREAM_CODEC
                    .apply(ByteBufCodecs.list())), { Optional.ofNullable(it.baseStack) },
                IotaType.TYPED_STREAM_CODEC
                    .apply(ByteBufCodecs.list()), FrameForEach::acc
            ) { a, b, c, d ->
                FrameForEach(a, b, c.getOrNull(), d)
            }


            override fun codec(): MapCodec<FrameForEach> =
                CODEC

            override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, FrameForEach> =
                STREAM_CODEC
        }
    }
}
