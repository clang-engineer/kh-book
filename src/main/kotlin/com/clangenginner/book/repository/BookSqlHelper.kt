package com.clangenginner.book.repository

import org.springframework.data.relational.core.sql.Column
import org.springframework.data.relational.core.sql.Expression
import org.springframework.data.relational.core.sql.Table

class BookSqlHelper {
    fun getColumns(table: Table, columnPrefix: String): MutableList<Expression> {
        val columns = mutableListOf<Expression>()
        columns.add(Column.aliased("id", table, columnPrefix + "_id"))
        columns.add(Column.aliased("title", table, columnPrefix + "_title"))
        columns.add(Column.aliased("description", table, columnPrefix + "_description"))
        columns.add(Column.aliased("author", table, columnPrefix + "_author"))

        return columns
    }
}
