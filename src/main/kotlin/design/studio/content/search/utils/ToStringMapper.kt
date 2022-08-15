package design.studio.content.search.utils

import co.elastic.clients.json.JsonpSerializer
import co.elastic.clients.json.SimpleJsonpMapper

internal class ToStringMapper : SimpleJsonpMapper() {
    override fun <T> getDefaultSerializer(value: T): JsonpSerializer<T> {
        return toStringSerializer as JsonpSerializer<T>
    }

    companion object {
        val INSTANCE = ToStringMapper()
        private val toStringSerializer: JsonpSerializer<*> =
            JsonpSerializer<Any?> { value, generator, mapper ->
                if (value == null) {
                    generator.writeNull()
                } else {
                    generator.write(value.toString())
                }
            }
    }
}
