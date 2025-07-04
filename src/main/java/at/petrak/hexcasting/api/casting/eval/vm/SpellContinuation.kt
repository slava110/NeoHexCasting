package at.petrak.hexcasting.api.casting.eval.vm

import net.minecraft.network.codec.ByteBufCodecs

/**
 * A continuation during the execution of a spell.
 */
sealed interface SpellContinuation {
    object Done : SpellContinuation

    data class NotDone(val frame: ContinuationFrame, val next: SpellContinuation) : SpellContinuation

    fun pushFrame(frame: ContinuationFrame): SpellContinuation = NotDone(frame, this)

    companion object {
        fun fromList(frames: List<ContinuationFrame>): SpellContinuation {
            var accumulator: SpellContinuation = Done
            val it = frames.listIterator(frames.size)
            while (it.hasPrevious()) {
                accumulator = NotDone(it.previous(), accumulator)
            }
            return accumulator
        }

        fun toList(continuation: SpellContinuation): List<ContinuationFrame> {
            val accumulator = ArrayList<ContinuationFrame>()
            var c = continuation
            while (c != Done) {
                accumulator.add((c as NotDone).frame)
                c = c.next
            }
            return accumulator
        }

        // TODO port: maybe unit should be first
        @JvmStatic
        val CODEC = ContinuationFrame.Type.TYPED_CODEC.listOf().xmap<SpellContinuation>(
            SpellContinuation::fromList,
            SpellContinuation::toList
        )

        @JvmStatic
        val STREAM_CODEC = ContinuationFrame.Type.TYPED_STREAM_CODEC.apply(
            ByteBufCodecs.list()
        ).map(
            SpellContinuation::fromList,
            SpellContinuation::toList
        )
    }
}
