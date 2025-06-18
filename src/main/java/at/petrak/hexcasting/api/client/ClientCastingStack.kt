package at.petrak.hexcasting.api.client

import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.HexDataComponents
import at.petrak.hexcasting.forge.lib.ForgeHexAttachments
import net.neoforged.neoforge.event.tick.PlayerTickEvent
import kotlin.math.min


class ClientCastingStack {
    private var patterns = ArrayList<HexPatternRenderHolder>()
    private var toRemove = mutableSetOf<HexPatternRenderHolder>()

    private var toAdd = ArrayList<HexPatternRenderHolder>()

    fun addPattern(pattern: HexPattern?, lifetime: Int) {
        if (pattern == null) return
        if (patterns.stream().anyMatch { patternRenderHolder -> patternRenderHolder.pattern.hashCode() == pattern.hashCode() }) {
            return
        }
        if (patterns.size > 100) {
            patterns.removeAt(0)
        }
        patterns.add(HexPatternRenderHolder(pattern, lifetime))
    }

    fun slowClear() {
        patterns.forEach { it.lifetime = min(it.lifetime, 140) }
    }

    fun getPatterns(): List<HexPatternRenderHolder> {
        return patterns
    }

    fun getPattern(index: Int): HexPattern? = patterns.getOrNull(index)?.pattern

    fun getPatternHolder(index: Int): HexPatternRenderHolder? = patterns.getOrNull(index)

    fun size(): Int {
        return patterns.size
    }

    fun tick() {
        // tick without getting a cme
        toAdd.forEach { pattern ->
            if (patterns.size > 100) {
                patterns.removeAt(0)
            }
            patterns.add(pattern)
        }

        toAdd.clear()

        patterns.forEach { pattern ->
            pattern.tick()
            if (pattern.lifetime <= 0) {
                toRemove.add(pattern)
            }
        }

        patterns.removeAll(toRemove)
        toRemove.clear()
    }

    companion object {

        @JvmStatic
        fun tickClientPlayer(ev: PlayerTickEvent.Pre) {
            if(!ev.entity.isDeadOrDying) {
                ev.entity.getData(ForgeHexAttachments.CLIENT_CASTING_STACK).tick()
            }
        }
    }
}