package nl.parlementairemonitor.hook

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File

class MotieHookTest {

    @Test
    fun `test parsing a motie pdf document`() {
        // Get the resource as a stream
        val resourceStream = javaClass.getResourceAsStream("/d7a5dc6c-3636-4acd-8630-96c14e97dcce.pdf")
        assertNotNull(resourceStream, "Resource file should exist")

        val file = File.createTempFile("motie", ".pdf")
        file.writeBytes(resourceStream!!.readAllBytes())

        val motie = MotieHook.parsePdf(file)
        assertNotNull(motie)
        assertEquals(3, motie.context.size)
        assertEquals(1, motie.motie.size)
    }

    @ParameterizedTest
    @CsvSource(
        "09b97518-8d7c-4a90-a090-28e45bc44920.docx,3,1",
        "0787ced2-629a-4d32-ab8f-4b97a7680248.docx,2,1"
    )
    fun `test parsing a motie docx document`(fileName: String, expectedContext: Int, expectedMoties: Int) {
        // Get the resource as a stream
        val resourceStream = javaClass.getResourceAsStream("/$fileName")
        assertNotNull(resourceStream, "Resource file should exist")

        val file = File.createTempFile("motie", ".docx")
        file.writeBytes(resourceStream!!.readAllBytes())

        val motie = MotieHook.parseDocx(file)
        assertNotNull(motie)
        assertEquals(expectedContext, motie.context.size)
        assertEquals(expectedMoties, motie.motie.size)
    }

    @Test
    fun `test parsing a motie doc document`() {
        // Get the resource as a stream
        val resourceStream = javaClass.getResourceAsStream("/ca71487f-866a-43aa-a9d5-ec3e2c3a236c.doc")
        assertNotNull(resourceStream, "Resource file should exist")

        val file = File.createTempFile("motie", ".doc")
        file.writeBytes(resourceStream!!.readAllBytes())

        val motie = MotieHook.parseDoc(file)
        assertNotNull(motie)
        assertEquals(3, motie.context.size)
        assertEquals(1, motie.motie.size)
    }

}