package guru.zoroark.tegral.prismakt.generator.generators

import guru.zoroark.tegral.prismakt.generator.parser.PRoot
import guru.zoroark.tegral.prismakt.generator.protocol.Datamodel
import guru.zoroark.tegral.prismakt.generator.protocol.Model
import java.nio.file.Path

class GeneratorContext(
    val outputDir: Path,
    val datamodel: Datamodel,
    val rawParsingResult: PRoot?
)
