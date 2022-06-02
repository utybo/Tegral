/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package guru.zoroark.tegral.config.core

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addPathSource
import org.junit.jupiter.api.assertThrows
import java.nio.file.FileSystem
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SectionedConfigurationTest {
    data class SectionedConfigurationContainer(
        val sc: SectionedConfiguration
    )

    class SectionedConfigurationOne(sections: ConfigurationSections) : SectionedConfiguration(sections)
    class SectionedConfigurationTwo(sections: ConfigurationSections) : SectionedConfiguration(sections)

    data class DoubleConfigurationContainer(
        val scOne: SectionedConfigurationOne,
        val scTwo: SectionedConfigurationTwo
    )

    data class SimpleSection(val key: String) {
        companion object :
            ConfigurationSection<SimpleSection>("simple-section", SectionOptionality.Required, SimpleSection::class)
    }

    data class OtherSection(val key: String) {
        companion object :
            ConfigurationSection<OtherSection>("other-section", SectionOptionality.Required, OtherSection::class)
    }

    data class OptionalSection(val key: String = "default") {
        companion object : ConfigurationSection<OptionalSection>(
            "other-section",
            SectionOptionality.Optional(OptionalSection()),
            OptionalSection::class
        )
    }

    data class RandomSection(val hello: String) {
        companion object :
            ConfigurationSection<RandomSection>("random-section", SectionOptionality.Required, RandomSection::class)
    }

    private fun configLoader(sections: List<ConfigurationSection<*>>) =
        ConfigLoaderBuilder.default()
            .strict()
            .addDecoder(
                SectionedConfigurationDecoder(SectionedConfiguration::class, ::SectionedConfiguration, sections)
            )

    private fun setupFs(filePath: String, fileContent: String): FileSystem {
        val fs = Jimfs.newFileSystem(Configuration.unix())
        Files.writeString(fs.getPath(filePath), fileContent)
        return fs
    }

    private fun setupAndLoadTomlConfig(
        content: String,
        vararg sections: ConfigurationSection<*>
    ): SectionedConfigurationContainer {
        val fs = setupFs("/test.toml", content)

        return configLoader(sections.toList())
            .addPathSource(fs.getPath("/test.toml"))
            .build().loadConfigOrThrow<SectionedConfigurationContainer>()
    }

    @Test
    fun `toml, single section`() {
        val toml = """
            [sc.simple-section]
            key = "value"
        """.trimIndent()
        val config = setupAndLoadTomlConfig(toml, SimpleSection)
        assertEquals(SimpleSection("value"), config.sc[SimpleSection])
    }

    @Test
    fun `toml, two sections`() {
        val toml = """
            [sc.simple-section]
            key = "value one"
            
            [sc.other-section]
            key = "value two"
        """.trimIndent()
        val config = setupAndLoadTomlConfig(toml, SimpleSection, OtherSection)
        assertEquals(SimpleSection("value one"), config.sc[SimpleSection])
        assertEquals(OtherSection("value two"), config.sc[OtherSection])
    }

    @Test
    fun `toml, two sections, one optional absent`() {
        val toml = """
            [sc.simple-section]
            key = "value one"
        """.trimIndent()
        val config = setupAndLoadTomlConfig(toml, SimpleSection, OptionalSection)
        assertEquals(SimpleSection("value one"), config.sc[SimpleSection])
        assertEquals(OptionalSection("default"), config.sc[OptionalSection])
    }

    @Test
    fun `toml, two sections, missing required section`() {
        val toml = """
            [sc.simple-section]
            key = "value one"
        """.trimIndent()
        val exc = assertThrows<ConfigException> {
            setupAndLoadTomlConfig(toml, SimpleSection, OtherSection)
        }
        val message = exc.message
        assertNotNull(message)
        assertTrue(message.contains("other-section"))
    }

    @Test
    fun `toml, two different sectioned configs can coexist`() {
        val toml = """
            [scOne.simple-section]
            key = "value one"
            
            [scTwo.random-section]
            hello = "world"
        """.trimIndent()
        val fs = setupFs("/test.toml", toml)

        val config = ConfigLoaderBuilder.default()
            .strict()
            .addDecoder(
                SectionedConfigurationDecoder(
                    SectionedConfigurationOne::class,
                    ::SectionedConfigurationOne,
                    listOf(SimpleSection)
                )
            )
            .addDecoder(
                SectionedConfigurationDecoder(
                    SectionedConfigurationTwo::class,
                    ::SectionedConfigurationTwo,
                    listOf(RandomSection)
                )
            )
            .addPathSource(fs.getPath("/test.toml"))
            .build()
            .loadConfigOrThrow<DoubleConfigurationContainer>()

        assertEquals(SimpleSection(key = "value one"), config.scOne[SimpleSection])
        assertEquals(RandomSection(hello = "world"), config.scTwo[RandomSection])
    }

    @Test
    fun `simple to string`() {
        val configuration = SectionedConfigurationOne(
            mapOf(SimpleSection to SimpleSection("value"))
        )
        assertEquals("SectionedConfigurationOne(simple-section=SimpleSection(key=value))", configuration.toString())
    }
}
