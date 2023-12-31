package kr.cosine.fishing.registry

import kr.cosine.fishing.fish.Fish
import kr.hqservice.framework.global.core.component.Bean
import org.bukkit.entity.FishHook

@Bean
class FishRegistry {

    private val fishMap = mutableMapOf<String, Fish>()

    fun contains(key: String): Boolean = fishMap.containsKey(key)

    fun findByKey(key: String): Fish? = fishMap[key]

    fun set(key: String, fish: Fish) {
        fishMap[key] = fish
    }

    fun getKeys(): List<String> = getMap().keys.toList()

    fun getValues(): List<Fish> = getMap().values.toList()

    fun getMap(): Map<String, Fish> = fishMap

    fun remove(key: String) {
        fishMap.remove(key)
    }

    fun getCatchableFishes(hook: FishHook, tick: Long): Map<Fish, Double> {
        return fishMap.values.filter { it.isCatchable(hook, tick) }.associateWith { it.chance }
    }
}