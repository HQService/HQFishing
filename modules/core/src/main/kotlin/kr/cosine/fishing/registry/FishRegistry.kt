package kr.cosine.fishing.registry

import kr.cosine.fishing.fish.Fish
import kr.hqservice.framework.global.core.component.Bean
import org.bukkit.entity.FishHook

@Bean
class FishRegistry {

    private val fishMap = mutableMapOf<String, Fish>()

    fun findByKey(key: String): Fish? = fishMap[key]

    fun getCatchableFishes(hook: FishHook, tick: Long): Map<Fish, Double> {
        return fishMap.values.filter { it.isCatchable(hook, tick) }.associateWith { it.chance }
    }
}