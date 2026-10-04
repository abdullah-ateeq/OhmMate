package com.example.ohmmate.data

import com.example.ohmmate.calculator.ElectricalParameter

object HistoryJsonSerializer {

    fun serialize(items: List<HistoryItem>): String {
        val sb = StringBuilder()
        sb.append("[")
        items.forEachIndexed { index, item ->
            if (index > 0) sb.append(",")
            sb.append("{")
            sb.append("\"id\":\"").append(escape(item.id)).append("\",")
            sb.append("\"timestamp\":").append(item.timestamp).append(",")
            sb.append("\"param1\":\"").append(item.param1.name).append("\",")
            sb.append("\"value1\":\"").append(escape(item.value1)).append("\",")
            sb.append("\"param2\":\"").append(item.param2.name).append("\",")
            sb.append("\"value2\":\"").append(escape(item.value2)).append("\",")
            sb.append("\"voltage\":").append(if (item.voltage != null) "\"${escape(item.voltage)}\"" else "null").append(",")
            sb.append("\"current\":").append(if (item.current != null) "\"${escape(item.current)}\"" else "null").append(",")
            sb.append("\"resistance\":").append(if (item.resistance != null) "\"${escape(item.resistance)}\"" else "null").append(",")
            sb.append("\"power\":").append(if (item.power != null) "\"${escape(item.power)}\"" else "null").append(",")
            sb.append("\"formula\":\"").append(escape(item.formula)).append("\"")
            sb.append("}")
        }
        sb.append("]")
        return sb.toString()
    }

    fun deserialize(json: String?): List<HistoryItem> {
        if (json.isNullOrBlank()) return emptyList()
        val trimmed = json.trim()
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return emptyList()

        val content = trimmed.substring(1, trimmed.length - 1).trim()
        if (content.isEmpty()) return emptyList()

        val items = mutableListOf<HistoryItem>()
        val objects = splitObjects(content)

        for (objStr in objects) {
            val map = parseJsonObject(objStr)
            val id = map["id"] ?: continue
            val timestamp = map["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()
            val param1Str = map["param1"] ?: continue
            val value1 = map["value1"] ?: continue
            val param2Str = map["param2"] ?: continue
            val value2 = map["value2"] ?: continue
            val voltage = map["voltage"]
            val current = map["current"]
            val resistance = map["resistance"]
            val power = map["power"]
            val formula = map["formula"] ?: ""

            val param1 = runCatching { ElectricalParameter.valueOf(param1Str) }.getOrNull() ?: continue
            val param2 = runCatching { ElectricalParameter.valueOf(param2Str) }.getOrNull() ?: continue

            items.add(
                HistoryItem(
                    id = id,
                    timestamp = timestamp,
                    param1 = param1,
                    value1 = value1,
                    param2 = param2,
                    value2 = value2,
                    voltage = voltage,
                    current = current,
                    resistance = resistance,
                    power = power,
                    formula = formula
                )
            )
        }
        return items
    }

    private fun escape(s: String): String {
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun unescape(s: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c == '\\' && i + 1 < s.length) {
                when (s[i + 1]) {
                    '\\' -> { sb.append('\\'); i += 2 }
                    '"' -> { sb.append('"'); i += 2 }
                    'n' -> { sb.append('\n'); i += 2 }
                    'r' -> { sb.append('\r'); i += 2 }
                    't' -> { sb.append('\t'); i += 2 }
                    else -> { sb.append(c); i++ }
                }
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }

    private fun splitObjects(arrayContent: String): List<String> {
        val list = mutableListOf<String>()
        var depth = 0
        var start = -1
        var inQuotes = false
        var isEscaped = false

        for (i in arrayContent.indices) {
            val c = arrayContent[i]
            if (isEscaped) {
                isEscaped = false
                continue
            }
            if (c == '\\') {
                isEscaped = true
                continue
            }
            if (c == '"') {
                inQuotes = !inQuotes
                continue
            }
            if (!inQuotes) {
                if (c == '{') {
                    if (depth == 0) start = i
                    depth++
                } else if (c == '}') {
                    depth--
                    if (depth == 0 && start != -1) {
                        list.add(arrayContent.substring(start, i + 1))
                        start = -1
                    }
                }
            }
        }
        return list
    }

    private fun parseJsonObject(objStr: String): Map<String, String?> {
        val result = mutableMapOf<String, String?>()
        val trimmed = objStr.trim()
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) return result
        val inner = trimmed.substring(1, trimmed.length - 1).trim()
        if (inner.isEmpty()) return result

        val tokens = mutableListOf<String>()
        var inQuotes = false
        var isEscaped = false
        var start = 0

        for (i in inner.indices) {
            val c = inner[i]
            if (isEscaped) {
                isEscaped = false
                continue
            }
            if (c == '\\') {
                isEscaped = true
                continue
            }
            if (c == '"') {
                inQuotes = !inQuotes
                continue
            }
            if (!inQuotes && c == ',') {
                tokens.add(inner.substring(start, i).trim())
                start = i + 1
            }
        }
        if (start < inner.length) {
            tokens.add(inner.substring(start).trim())
        }

        for (token in tokens) {
            val colonIdx = findColon(token)
            if (colonIdx != -1) {
                val rawKey = token.substring(0, colonIdx).trim()
                val rawVal = token.substring(colonIdx + 1).trim()
                val key = stripQuotes(rawKey)
                val value = if (rawVal == "null") null else unescape(stripQuotes(rawVal))
                result[key] = value
            }
        }

        return result
    }

    private fun findColon(pair: String): Int {
        var inQuotes = false
        var isEscaped = false
        for (i in pair.indices) {
            val c = pair[i]
            if (isEscaped) {
                isEscaped = false
                continue
            }
            if (c == '\\') {
                isEscaped = true
                continue
            }
            if (c == '"') {
                inQuotes = !inQuotes
                continue
            }
            if (!inQuotes && c == ':') {
                return i
            }
        }
        return -1
    }

    private fun stripQuotes(s: String): String {
        val t = s.trim()
        return if (t.startsWith("\"") && t.endsWith("\"") && t.length >= 2) {
            t.substring(1, t.length - 1)
        } else {
            t
        }
    }
}
