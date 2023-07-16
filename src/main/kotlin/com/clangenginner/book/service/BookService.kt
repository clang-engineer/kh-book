package com.clangenginner.book.service
import com.clangenginner.book.service.dto.BookDTO
import org.springframework.data.domain.Pageable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Service Interface for managing [com.clangenginner.book.domain.Book].
 */
interface BookService {

    /**
     * Save a book.
     *
     * @param bookDTO the entity to save.
     * @return the persisted entity.
     */
    fun save(bookDTO: BookDTO): Mono<BookDTO>

    /**
     * Updates a book.
     *
     * @param bookDTO the entity to update.
     * @return the persisted entity.
     */
    fun update(bookDTO: BookDTO): Mono<BookDTO>

    /**
     * Partially updates a book.
     *
     * @param bookDTO the entity to update partially.
     * @return the persisted entity.
     */
    fun partialUpdate(bookDTO: BookDTO): Mono<BookDTO>

    /**
     * Get all the books.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    fun findAll(pageable: Pageable): Flux<BookDTO>

    /**
     * Returns the number of books available.
     * @return the number of entities in the database.
     */
    fun countAll(): Mono<Long>
    /**
     * Get the "id" book.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    fun findOne(id: Long): Mono<BookDTO>

    /**
     * Delete the "id" book.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    fun delete(id: Long): Mono<Void>
}
