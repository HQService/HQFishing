package kr.cosine.fishing.inventory

import kr.cosine.fishing.extension.removeColor
import kr.cosine.fishing.fish.Fish
import kr.cosine.fishing.inventory.FishSettingViewModel.Companion.setClickFunctionOnlyLeftClick
import kr.cosine.fishing.util.SignEditor
import kr.hqservice.framework.inventory.button.HQButtonBuilder
import kr.hqservice.framework.inventory.container.HQContainer
import kr.hqservice.framework.nms.extension.getDisplayName
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

class FishDetailSettingView(
    private val fishSettingViewModel: FishSettingViewModel,
    private val fishSettingView: FishSettingView,
    private val fish: Fish
) : HQContainer(9, "${fish.getItemStack().getDisplayName().removeColor()} : 세부 설정", true) {

    private companion object {
        val timeRegex = Regex("^(\\d{1,5})\\s*~\\s*(\\d{1,5})\$")
    }

    private var isPrevented = false

    override fun initialize(inventory: Inventory) {
        HQButtonBuilder(Material.PUFFERFISH_BUCKET).apply {
            setDisplayName("§f개체 수 설정")
            setLore(
                listOf(
                    "§a§l| §f현재: ${fish.chance}마리",
                    "",
                    "§7[ 가중치를 기반으로 하며, 물고기가 잡히는 확률과 거의 유사합니다. ]"
                )
            )
            setClickFunctionOnlyLeftClick { event ->
                val player = event.getWhoClicked()
                val container = event.getContainer()

                player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 2f)
                isPrevented = true

                player.closeInventory()
                SignEditor(player, arrayOf("", "개체 수를 입력해주세요.", "", "'취소' - 입력 시 창 닫기")) { texts ->
                    val text = texts[0]
                    if (text == "취소") {
                        player.sendMessage("§c설정이 취소되었습니다.")

                    } else {
                        val chance = texts[0].toDoubleOrNull() ?: run {
                            player.sendMessage("§c숫자만 입력할 수 있습니다.")
                            return@SignEditor false
                        }
                        fish.setChance(chance)
                        player.sendMessage("§a개체 수를 ${chance}마리로 설정하였습니다.")
                    }
                    fishSettingViewModel.reopen(container, player)
                    return@SignEditor true
                }.open()
            }
        }.build().setSlot(this, 0)

        HQButtonBuilder(Material.DAYLIGHT_DETECTOR).apply {
            setDisplayName("§f날씨 변경")
            setLore(listOf("§a§l| §f현재: ${fish.weather}"))
            setClickFunctionOnlyLeftClick { event ->
                val player = event.getWhoClicked()
                val container = event.getContainer()

                player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 2f)
                fish.switchWeather()

                container.refresh()
            }
        }.build().setSlot(this, 1)

        HQButtonBuilder(Material.WATER_BUCKET).apply {
            setDisplayName("§f바이옴 설정")
            val isEmptyBiome = fish.biomes.isEmpty()
            val biomes = if (isEmptyBiome) {
                listOf("§fALL")
            } else {
                fish.biomes.map { "§f${it.name}" }.chunked(4).map { it.joinToString(", ") }
            }
            setLore(listOf("§a§l| §f현재: ${biomes[0]}") + if (isEmptyBiome) emptyList() else biomes.subList(1, biomes.size))
            setClickFunctionOnlyLeftClick { event ->
                val player = event.getWhoClicked()

                player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 2f)
                isPrevented = true

                player.closeInventory()
                fishSettingViewModel.runTaskLater {
                    FishBiomeSettingView(fishSettingViewModel, this@FishDetailSettingView, fish).open(player)
                }
            }
        }.build().setSlot(this, 2)

        HQButtonBuilder(Material.COMPASS).apply {
            setDisplayName("§f틱 설정")
            setLore(
                listOf(
                    "§a§l| §f현재: ${fish.tick}틱",
                    "",
                    "§7[ 이 물고기가 몇 틱 이상부터 잡힐지 설정합니다. ]",
                    "",
                    "§7ex) 물고기가 10틱 동안 물었다고 가정했을 때",
                    "§7현재 설정 값이 10틱 이상(11틱, 13틱 등등)이면",
                    "§7시스템에서 이 물고기를 잡을 수 있다고 판단합니다. "
                )
            )
            setClickFunctionOnlyLeftClick { event ->
                val player = event.getWhoClicked()
                val container = event.getContainer()

                player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 2f)
                isPrevented = true

                player.closeInventory()
                SignEditor(player, arrayOf("", "틱을 입력해주세요.", "", "'취소' - 입력 시 창 닫기")) { texts ->
                    val text = texts[0]
                    if (text == "취소") {
                        player.sendMessage("§c설정이 취소되었습니다.")
                    } else {
                        val tick = texts[0].toLongOrNull() ?: run {
                            player.sendMessage("§c숫자만 입력할 수 있습니다.")
                            return@SignEditor false
                        }
                        fish.setTick(tick)
                        player.sendMessage("§a물고기가 찌를 물고 있는 시간을 ${tick}틱으로 설정하였습니다.")
                    }
                    fishSettingViewModel.reopen(container, player)
                    return@SignEditor true
                }.open()
            }
        }.build().setSlot(this, 3)

        HQButtonBuilder(Material.CLOCK).apply {
            setDisplayName("§f시간 설정")
            val fishTime = fish.time
            setLore(
                listOf(
                    "§a§l| §f현재: ${fishTime.first}~${fishTime.last}",
                    "",
                    "§7[ 물고기가 잡히는 시간대입니다. §8(마크 시간 기준) §7]"
                )
            )
            setClickFunctionOnlyLeftClick { event ->
                val player = event.getWhoClicked()
                val container = event.getContainer()

                player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 2f)
                isPrevented = true

                player.closeInventory()
                SignEditor(player, arrayOf("", "시간을 입력해주세요.", "형식: 0~23999", "'취소' - 입력 시 창 닫기")) { texts ->
                    val text = texts[0]
                    if (text == "취소") {
                        player.sendMessage("§c설정이 취소되었습니다.")
                    } else {
                        val matchResult = timeRegex.find(text)
                        if (matchResult == null) {
                            player.sendMessage("§c형식이 올바르지 않습니다.")
                            return@SignEditor false
                        }
                        val (start, end) = matchResult.destructured
                        val firstTime = start.toInt()
                        val lastTime = end.toInt()
                        if (lastTime < firstTime) {
                            player.sendMessage("§c처음 시간이 마지막 시간보다 클 수 없습니다.")
                            return@SignEditor false
                        }
                        if (firstTime > 23999 || lastTime > 23999) {
                            player.sendMessage("§c최대로 입력할 수 있는 값은 23999입니다.")
                            return@SignEditor false
                        }
                        fish.setTime(firstTime, lastTime)
                        player.sendMessage("§a물고기가 잡히는 시간대를 $firstTime~$lastTime(으)로 설정하였습니다.")
                    }
                    fishSettingViewModel.reopen(container, player)
                    return@SignEditor true
                }.open()
            }
        }.build().setSlot(this, 4)
    }
    
    override fun onClose(event: InventoryCloseEvent) {
        val player = event.player as Player
        if (isPrevented) {
            isPrevented = false
            return
        }
        fishSettingViewModel.runTaskLater {
            fishSettingView.open(player)
        }
    }
}