package com.clangenginner.book.service.impl

import com.clangenginner.book.domain.Book
import com.clangenginner.book.repository.BookRepository
import com.clangenginner.book.service.BookService
import com.clangenginner.book.service.dto.BookDTO
import com.clangenginner.book.service.mapper.BookMapper
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Service Implementation for managing [Book].
 */
@Service
@Transactional
class BookServiceImpl(
    private val bookRepository: BookRepository,
    private val bookMapper: BookMapper,
) : BookService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun save(bookDTO: BookDTO): Mono<BookDTO> {
        log.debug("Request to save Book : $bookDTO")
        return bookRepository.save(bookMapper.toEntity(bookDTO))
            .map(bookMapper::toDto)
    }

    override fun update(bookDTO: BookDTO): Mono<BookDTO> {
        log.debug("Request to update Book : {}", bookDTO)
        return bookRepository.save(bookMapper.toEntity(bookDTO))
            .map(bookMapper::toDto)
    }

    override fun partialUpdate(bookDTO: BookDTO): Mono<BookDTO> {
        log.debug("Request to partially update Book : {}", bookDTO)

        return bookRepository.findById(bookDTO.id)
            .map {
                bookMapper.partialUpdate(it, bookDTO)
                it
            }
            .flatMap { bookRepository.save(it) }
            .map { bookMapper.toDto(it) }
    }

    @Transactional(readOnly = true)
    override fun findAll(pageable: Pageable): Flux<BookDTO> {
        log.debug("Request to get all Books")
        return bookRepository.findAllBy(pageable)
            .map(bookMapper::toDto)
    }

    override fun countAll() = bookRepository.count()

    @Transactional(readOnly = true)
    override fun findOne(id: Long): Mono<BookDTO> {
        log.debug("Request to get Book : $id")
        return bookRepository.findById(id)
            .map(bookMapper::toDto)
    }

    override fun delete(id: Long): Mono<Void> {
        log.debug("Request to delete Book : $id")
        return bookRepository.deleteById(id)
    }
}
