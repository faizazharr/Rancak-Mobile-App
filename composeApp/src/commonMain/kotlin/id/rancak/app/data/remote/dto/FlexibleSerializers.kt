package id.rancak.app.data.remote.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull

/**
 * Serializer for Long fields that the backend may send as a quoted decimal string
 * (e.g. "60000.00") instead of a plain integer.
 *
 * Handles:
 *  - JSON string  "60000.00" → 60000L
 *  - JSON integer  60000     → 60000L
 *  - JSON float    60000.0   → 60000L
 */
object FlexibleLongSerializer : KSerializer<Long> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleLong", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Long) = encoder.encodeLong(value)

    override fun deserialize(decoder: Decoder): Long {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return decoder.decodeLong()
        return when (val el = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive -> when {
                el.isString -> el.content.toDoubleOrNull()?.toLong()
                    ?: el.content.toLongOrNull() ?: 0L
                else -> el.longOrNull ?: el.doubleOrNull?.toLong() ?: 0L
            }
            else -> 0L
        }
    }
}
