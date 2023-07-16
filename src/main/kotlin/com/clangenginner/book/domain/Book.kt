package com.clangenginner.book.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.io.Serializable
import javax.validation.constraints.*

/**
 * A Book.
 */
@Table("book")
@SuppressWarnings("common-java:DuplicatedBlocks")
data class Book(

    @Id
    @Column("id")
    var id: Long? = null,

    @get: NotNull(message = "must not be null")
    @get: Size(min = 5, max = 20)
    @Column("title")
    var title: String? = null,
    @Column("description")
    var description: String? = null,

    @get: NotNull(message = "must not be null")
    @Column("author")
    var author: String? = null,

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Book) return false
        return id != null && other.id != null && id == other.id
    }

    override fun toString(): String {
        return "Book{" +
            "id=" + id +
            ", title='" + title + "'" +
            ", description='" + description + "'" +
            ", author='" + author + "'" +
            "}"
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
