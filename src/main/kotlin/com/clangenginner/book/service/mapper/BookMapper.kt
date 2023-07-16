package com.clangenginner.book.service.mapper

import com.clangenginner.book.domain.Book
import com.clangenginner.book.service.dto.BookDTO
import org.mapstruct.*

/**
 * Mapper for the entity [Book] and its DTO [BookDTO].
 */
@Mapper(componentModel = "spring")
interface BookMapper :
    EntityMapper<BookDTO, Book>
