package com.odtheking.odin.utils.network.hypixelapi

import com.google.gson.Gson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProfileCompatibilityTest {
    private val gson = Gson()

    @Test
    fun `null profiles produce no selected member`() {
        val profiles = gson.fromJson("""{"profiles":null}""", HypixelData.ProfilesData::class.java)
        assertTrue(profiles.profiles.isEmpty())
        assertNull(HypixelData.PlayerInfo(profiles, "example", "Example").memberData)
    }

    @Test
    fun `selected member resolves the current dungeon API fields`() {
        val profiles = gson.fromJson(
            """{"profiles":[{"selected":true,"members":{"example":{"dungeons":{
              "secrets":120,"dungeon_types":{
                "catacombs":{"tier_completions":{"0":99,"1":2,"7":3}},
                "master_catacombs":{"tier_completions":{"7":5}}
              }},"player_stats":{"kills":{"watcher_summon_undead":3,"master_watcher_summon_undead":4}}
            }}}]}""",
            HypixelData.ProfilesData::class.java,
        )
        val member = HypixelData.PlayerInfo(profiles, "example", "Example").memberData!!
        assertEquals(10, member.dungeons.totalRuns)
        assertEquals(12.0, member.dungeons.avrSecrets)
        assertEquals(7, member.playerStats.bloodMobKills)
    }

    @Test
    fun `zero runs have a finite secrets average`() {
        assertEquals(0.0, HypixelData.DungeonsData(secrets = 120).avrSecrets)
    }
}
