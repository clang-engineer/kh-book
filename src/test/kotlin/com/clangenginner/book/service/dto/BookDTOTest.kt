package com.clangenginner.book.service.dto

import com.clangenginner.book.web.rest.equalsVerifier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BookDTOTest {

    @Test
    fun dtoEqualsVerifier() {
        equalsVerifier(BookDTO::class)
        val bookDTO1 = BookDTO()
        bookDTO1.id = 1L
        val bookDTO2 = BookDTO()
        assertThat(bookDTO1).isNotEqualTo(bookDTO2)
        bookDTO2.id = bookDTO1.id
        assertThat(bookDTO1).isEqualTo(bookDTO2)
        bookDTO2.id = 2L
        assertThat(bookDTO1).isNotEqualTo(bookDTO2)
        bookDTO1.id = null
        assertThat(bookDTO1).isNotEqualTo(bookDTO2)
    }
}
