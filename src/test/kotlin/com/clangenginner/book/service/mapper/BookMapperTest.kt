package com.clangenginner.book.service.mapper

import org.junit.jupiter.api.BeforeEach

class BookMapperTest {

    private lateinit var bookMapper: BookMapper

    @BeforeEach
    fun setUp() {
        bookMapper = BookMapperImpl()
    }
}
