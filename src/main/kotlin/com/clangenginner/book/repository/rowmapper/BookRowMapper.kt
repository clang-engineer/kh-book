package com.clangenginner.book.repository.rowmapper

import com.clangenginner.book.domain.Book
import io.r2dbc.spi.Row
import org.springframework.stereotype.Service
import java.util.function.BiFunction

/**
 * Converter between {@link Row} to {@link Book}, with proper type conversions.
 */
@Service
class BookRowMapper(val converter: ColumnConverter) : BiFunction<Row, String, Book> {

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Book} stored in the database.
     */
    override fun apply(row: Row, prefix: String): Book {
        val entity = Book()
        entity.id = converter.fromRow(row, prefix + "_id", Long::class.java)
        entity.title = converter.fromRow(row, prefix + "_title", String::class.java)
        entity.description = converter.fromRow(row, prefix + "_description", String::class.java)
        entity.author = converter.fromRow(row, prefix + "_author", String::class.java)
        return entity
    }
}
