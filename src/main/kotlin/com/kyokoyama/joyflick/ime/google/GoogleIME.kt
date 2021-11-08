package com.kyokoyama.joyflick.ime.google

import com.kyokoyama.joyflick.ime.IME
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.jsoup.Jsoup
import java.net.URLEncoder

class GoogleIME : IME {
    val baseURL = "http://www.google.com/transliterate?langpair=ja-Hira%7Cja&text="

    fun parameter(original: String) = URLEncoder.encode(original, "utf-8")

    override fun convert(original: String): List<com.kyokoyama.joyflick.ime.Conversion> {
        if (original.isEmpty()) return listOf()

        val url = baseURL + parameter(original)
        val jsonText = Jsoup.connect(url).get().body().text()

        return decode(jsonText, Json { isLenient = true })
    }
}

private fun decode(string: String, parser: Json): List<Conversion> {
    val element: JsonArray = parser.parseToJsonElement(string).jsonArray

    return element.map {
        val array = it.jsonArray
        Conversion(array[0].jsonPrimitive.content, array[1].jsonArray.map { e -> e.jsonPrimitive.content })
    }
}