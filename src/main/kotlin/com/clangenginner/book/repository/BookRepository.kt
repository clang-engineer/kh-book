package com.clangenginner.book.repository

import com.clangenginner.book.domain.Book
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
* Spring Data R2DBC repository for the Book entity.
*/
@SuppressWarnings("unused")
@Repository
interface BookRepository : ReactiveCrudRepository<Book, Long>, BookRepositoryInternal {

    override fun findAllBy(pageable: Pageable?): Flux<Book>

    override fun <S : Book> save(entity: S): Mono<S>

    override fun findAll(): Flux<Book>

    override fun findById(id: Long?): Mono<Book>

    override fun deleteById(id: Long): Mono<Void>
}

interface BookRepositoryInternal {
    fun <S : Book> save(entity: S): Mono<S>

    fun findAllBy(pageable: Pageable?): Flux<Book>

    fun findAll(): Flux<Book>

    fun findById(id: Long?): Mono<Book>

    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // fun findAllBy(pageable: Pageable, criteria: Criteria): Flux<Book>
}
