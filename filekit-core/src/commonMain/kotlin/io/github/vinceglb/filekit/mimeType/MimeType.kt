package io.github.vinceglb.filekit.mimeType

import io.github.vinceglb.filekit.exceptions.InvalidMimeTypeException

public class MimeType private constructor(
    public val primaryType: String,
    public val subtype: String,
    public val parameters: Set<MimeTypeParameter> = emptySet(),
) {
    init {
        if (primaryType.isBlank()) {
            throw InvalidMimeTypeException("MIME type primary type must not be blank")
        }

        if (subtype.isBlank()) {
            throw InvalidMimeTypeException("MIME type subtype must not be blank")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MimeType) return false

        if (!primaryType.equals(other.primaryType, ignoreCase = true)) return false
        if (!subtype.equals(other.subtype, ignoreCase = true)) return false

        if (parameters.size != other.parameters.size) return false

        val thisParams = parameters.associateBy { it.name.lowercase() }
        val otherParams = other.parameters.associateBy { it.name.lowercase() }

        if (thisParams.keys != otherParams.keys) return false

        return thisParams.all { (key, param) ->
            val otherParam = otherParams[key]
            otherParam != null && param.value == otherParam.value
        }
    }

    override fun hashCode(): Int {
        val typeHash = primaryType.lowercase().hashCode()
        val subHash = subtype.lowercase().hashCode()
        val paramsHash = parameters
            .associateBy(keySelector = { it.name.lowercase() }, valueTransform = { it.value })
            .hashCode()
        return 31 * (31 * typeHash + subHash) + paramsHash
    }

    override fun toString(): String {
        val base = "$primaryType/$subtype"

        if (parameters.isEmpty()) return base

        return buildString {
            append(base)

            parameters.forEach { parameter ->
                append("; ")
                append(parameter.name)
                append("=")
                append(parameter.value)
            }
        }
    }

    public companion object {
        public fun parse(mimeType: String): MimeType {
            val parts = mimeType.split(";").map { it.trim() }

            if (parts.isEmpty() || parts[0].isEmpty()) {
                throw InvalidMimeTypeException("MIME type string must not be empty")
            }

            val typeParts = parts[0].split("/")

            if (typeParts.size != 2) {
                throw InvalidMimeTypeException("Invalid MIME type format: $mimeType")
            }

            val primaryType = typeParts[0].lowercase()
            val subtype = typeParts[1].lowercase()

            val parameters = if (parts.size > 1) {
                parts
                    .drop(1)
                    .map { part ->
                        val nameValuePair = part.split("=")

                        if (nameValuePair.size == 2) {
                            val name = nameValuePair[0].lowercase()
                            val value = nameValuePair[0].lowercase()
                            MimeTypeParameter(name = name, value = value)
                        } else {
                            throw InvalidMimeTypeException("Invalid parameter in MIME type: $part")
                        }
                    }.toSet()
            } else {
                emptySet()
            }

            return MimeType(primaryType = primaryType, subtype = subtype, parameters = parameters)
        }
    }
}
