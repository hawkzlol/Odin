package com.odtheking.odin.utils.ui.rendering

import com.google.gson.JsonParser
import java.io.InputStreamReader
import kotlin.test.Test
import kotlin.test.assertTrue

class TextureSamplingMetadataTest {

    @Test
    fun convertedNanoVgImagesUseLinearClampedSampling() {
        val textures = listOf("movement_icon.png", "chevron.png", "hue_gradient.png")

        for (textureName in textures) {
            val resourcePath = "assets/odin/$textureName.mcmeta"
            val stream = checkNotNull(javaClass.classLoader.getResourceAsStream(resourcePath)) {
                "Missing texture metadata: $resourcePath"
            }
            val textureMetadata = stream.use {
                JsonParser.parseReader(InputStreamReader(it)).asJsonObject
                    .getAsJsonObject("texture")
            }

            assertTrue(textureMetadata.get("blur").asBoolean, "$textureName must use linear sampling")
            assertTrue(textureMetadata.get("clamp").asBoolean, "$textureName must clamp UV coordinates")
        }
    }
}
