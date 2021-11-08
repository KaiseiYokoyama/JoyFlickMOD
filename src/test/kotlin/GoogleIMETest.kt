import com.kyokoyama.joyflick.ime.Conversion
import com.kyokoyama.joyflick.ime.IMEHandler
import com.kyokoyama.joyflick.ime.google.GoogleIME
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

class GoogleIMETest {
    @Test
    internal fun testConvert() {
        val handler = initialize()

        var received: List<Conversion>
        val record0 = measureTimeMillis {
            handler.send("ここではきものをぬぐ")
            received = handler.receive()
        }
        println("$record0")
        println("$received")

        val record1 = measureTimeMillis {
            handler.send("へんかん")
            received = handler.receive()
        }
        println("$record1")
        println("$received")
    }

    private fun initialize() = IMEHandler(GoogleIME())
}