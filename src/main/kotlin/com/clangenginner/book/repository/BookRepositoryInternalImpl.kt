package com.clangenginner.book.repository

import com.clangenginner.book.domain.Book
import com.clangenginner.book.repository.rowmapper.BookRowMapper
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.convert.R2dbcConverter
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity
import org.springframework.data.relational.core.sql.Condition
import org.springframework.data.relational.core.sql.Conditions
import org.springframework.data.relational.core.sql.Select
import org.springframework.data.relational.core.sql.Table
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.RowsFetchSpec
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Spring Data R2DBC custom repository implementation for the Book entity.
 */
@SuppressWarnings("unused")
class BookRepositoryInternalImpl(
    val template: R2dbcEntityTemplate,
    val entityManager: EntityManager,
    val bookMapper: BookRowMapper,
    entityOperations: R2dbcEntityOperations,
    converter: R2dbcConverter
) : SimpleR2dbcRepository<Book, Long>(
    MappingRelationalEntityInformation(
        converter.mappingContext.getRequiredPersistentEntity(Book::class.java) as RelationalPersistentEntity<Book>
    ),
    entityOperations,
    converter
),
    BookRepositoryInternal {

    private val db: DatabaseClient = template.databaseClient

    companion object {
        private val entityTable = Table.aliased("book", EntityManager.ENTITY_ALIAS)
    }

    override fun findAllBy(pageable: Pageable?): Flux<Book> {
        return createQuery(pageable, null).all()
    }

    fun createQuery(pageable: Pageable?, whereClause: Condition?): RowsFetchSpec<Book> {
        val columns = BookSqlHelper().getColumns(entityTable, EntityManager.ENTITY_ALIAS)
        val selectFrom = Select.builder().select(columns).from(entityTable)

        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        val select = entityManager.createSelect(selectFrom, Book::class.java, pageable, whereClause)
        return db.sql(select).map(this::process)
    }

    override fun findAll(): Flux<Book> {
        return findAllBy(null)
    }

    override fun findById(id: Long?): Mono<Book> {
        val whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()))
        return createQuery(null, whereClause).one()
    }

    private fun process(row: Row, metadata: RowMetadata): Book {
        val entity = bookMapper.apply(row, "e")
        return entity
    }

    override fun <S : Book> save(entity: S): Mono<S> {
        return super.save(entity)
    }
}
