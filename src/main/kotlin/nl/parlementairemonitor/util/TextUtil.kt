package nl.parlementairemonitor.util

import java.text.Normalizer

object TextUtil {

    fun makeSearchableText(input: String): String {
        val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
        val noDiacritics = normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        val noPunctuation = noDiacritics.replace(Regex("[\\p{Punct}]"), "")
        val lowercased = noPunctuation.lowercase()
        return lowercased.replace(Regex("\\s+"), " ").trim()
    }

}