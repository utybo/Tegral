package guru.zoroark.tegral.openapi.cli

import com.github.ajalt.clikt.core.ProgramResult
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CliTest {
    @Test
    fun `Test running with simple command`() {
        val fs = Jimfs.newFileSystem(Configuration.unix())
        val input = fs.getPath("/input.openapi.kts")
        Files.writeString(input, """
            title = "My API"
            version = "0.0.0"
        """.trimIndent())
        val output = fs.getPath("/output.json")

        TegralOpenApiCli(fs).parse(listOf("/input.openapi.kts", "-o", "/output.json"))

        val actual = Files.readString(output)
        val expected = """
            {"openapi":"3.0.1","info":{"title":"My API","version":"0.0.0"}}
        """.trimIndent()
        assertEquals(expected, actual)
    }

    @Test
    fun `Test running with simple command, no output to file`() {
        val fs = Jimfs.newFileSystem(Configuration.unix())
        val input = fs.getPath("/input.openapi.kts")
        Files.writeString(input, """
            title = "My API"
            version = "0.0.0"
        """.trimIndent())

        val actual = captureStdout {
            TegralOpenApiCli(fs).parse(listOf("/input.openapi.kts"))
        }
        val expected = """
            {"openapi":"3.0.1","info":{"title":"My API","version":"0.0.0"}}
        """.trimIndent()
        assertTrue(actual.contains(expected), "Did not get JSON in output:\n$actual")
    }

    @Test
    fun `Test running with simple command, yaml`() {
        val fs = Jimfs.newFileSystem(Configuration.unix())
        val input = fs.getPath("/input.openapi.kts")
        Files.writeString(input, """
            title = "My API"
            version = "0.0.0"
        """.trimIndent())
        val output = fs.getPath("/output.json")

        TegralOpenApiCli(fs).parse(listOf("/input.openapi.kts", "-o", "/output.json", "-f", "yaml"))

        val actual = Files.readString(output)
        val expected = """
            openapi: 3.0.1
            info:
              title: My API
              version: 0.0.0
        """.trimIndent() + "\n"
        assertEquals(expected, actual)
    }

    @Test
    fun `Test running with invalid kotlin`() {
        val fs = Jimfs.newFileSystem(Configuration.unix())
        val input = fs.getPath("/input.openapi.kts")
        Files.writeString(input, """
            ICANNOTCOMPILE
            title = "My API"
        """.trimIndent())

        val actual = captureStdout {
            val result = assertFailsWith<ProgramResult> {
                TegralOpenApiCli(fs).parse(listOf("/input.openapi.kts"))
            }
            assertEquals(1, result.statusCode)
        }
        val expected = """
            at /input.openapi.kts:1:1
        """.trimIndent()
        assertTrue(actual.contains(expected), "Did not get JSON in output:\n$actual")
    }
}

private inline fun captureStdout(block: () -> Unit): String {
    val originalOut = System.out
    val byteArrayOutputStream = ByteArrayOutputStream()
    val capturingOut = java.io.PrintStream(byteArrayOutputStream)
    try {
        System.setOut(capturingOut)
        block()
    } finally {
        System.setOut(originalOut)
    }
    return String(byteArrayOutputStream.toByteArray())
}
