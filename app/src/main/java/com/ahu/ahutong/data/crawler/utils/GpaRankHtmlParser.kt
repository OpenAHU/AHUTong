package com.ahu.ahutong.data.crawler.utils

object GpaRankHtmlParser {

    private val assignmentPattern = Regex(
        """(?i)(?:\bvar\b|\blet\b|\bconst\b)?\s*(?:window\s*\.\s*)?gpasemestermodel\s*="""
    )

    fun extractModelObject(html: String): String {
        val match = assignmentPattern.find(html)
            ?: throw Exception("未找到 gpaSemesterModel 变量")

        val objectStart = html.indexOf('{', match.range.last + 1)
        if (objectStart < 0) {
            throw Exception("gpaSemesterModel 变量不是对象")
        }

        return html.substring(objectStart, findObjectEnd(html, objectStart) + 1)
    }

    private fun findObjectEnd(text: String, start: Int): Int {
        var depth = 0
        var quote: Char? = null
        var escaped = false

        for (index in start until text.length) {
            val char = text[index]

            if (quote != null) {
                if (escaped) {
                    escaped = false
                } else if (char == '\\') {
                    escaped = true
                } else if (char == quote) {
                    quote = null
                }
                continue
            }

            when (char) {
                '\'', '"' -> quote = char
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return index
                }
            }
        }

        throw Exception("gpaSemesterModel 对象不完整")
    }
}
