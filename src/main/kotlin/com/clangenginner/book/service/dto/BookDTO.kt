package com.clangenginner.book.service.dto

import java.io.Serializable
import java.util.Objects
import javax.validation.constraints.*

/**
 * A DTO for the [com.clangenginner.book.domain.Book] entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
data class BookDTO(

    var id: Long? = null,

    @get: NotNull(message = "must not be null")
    @get: Size(min = 5, max = 20)
    var title: String? = null,

    var description: String? = null,

    @get: NotNull(message = "must not be null")
    var author: String? = null
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BookDTO) return false
        val bookDTO = other
        if (this.id == null) {
            return false
        }
        return Objects.equals(this.id, bookDTO.id)
    }

    override fun hashCode() = Objects.hash(this.id)
}
