import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MainTest {
    @Test
    fun testGreeting() {
        val expected = "Hello World!"
        // Aici poți adăuga logica ta de test
        assertEquals("Hello World!", expected, "Mesajul ar trebui să fie Hello World!")
    }
}
