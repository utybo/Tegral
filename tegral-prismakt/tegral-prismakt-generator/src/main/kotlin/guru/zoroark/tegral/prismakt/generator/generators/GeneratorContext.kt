package guru.zoroark.tegral.prismakt.generator.generators

import guru.zoroark.tegral.prismakt.generator.parser.NiwenPrism
import guru.zoroark.tegral.prismakt.generator.parser.PRoot
import guru.zoroark.tegral.prismakt.generator.protocol.Datamodel
import java.nio.file.Path

/**
 * Context for [generators][ModelGenerator]
 */
class GeneratorContext(
    /**
     * Output directory where classes, etc. should be placed.
     */
    val outputDir: Path,
    /**
     * Prisma's DMMF Datamodel.
     */
    val datamodel: Datamodel,
    /**
     * Raw [NiwenPrism] result, or null if the Niwen Prism parsing failed.
     */
    val rawParsingResult: PRoot?
)
