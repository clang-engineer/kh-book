package com.clangenginner.book.web.rest

import com.clangenginner.book.repository.BookRepository
import com.clangenginner.book.service.BookService
import com.clangenginner.book.service.dto.BookDTO
import com.clangenginner.book.web.rest.errors.BadRequestAlertException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import tech.jhipster.web.util.HeaderUtil
import tech.jhipster.web.util.PaginationUtil
import tech.jhipster.web.util.reactive.ResponseUtil
import java.net.URI
import java.net.URISyntaxException
import java.util.Objects
import javax.validation.Valid
import javax.validation.constraints.NotNull

private const val ENTITY_NAME = "khBookBook"
/**
 * REST controller for managing [com.clangenginner.book.domain.Book].
 */
@RestController
@RequestMapping("/api")
class BookResource(
    private val bookService: BookService,
    private val bookRepository: BookRepository,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "khBookBook"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /books` : Create a new book.
     *
     * @param bookDTO the bookDTO to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new bookDTO, or with status `400 (Bad Request)` if the book has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/books")
    fun createBook(@Valid @RequestBody bookDTO: BookDTO): Mono<ResponseEntity<BookDTO>> {
        log.debug("REST request to save Book : $bookDTO")
        if (bookDTO.id != null) {
            throw BadRequestAlertException(
                "A new book cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        return bookService.save(bookDTO)
            .map { result ->
                try {
                    ResponseEntity.created(URI("/api/books/${result.id}"))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
                        .body(result)
                } catch (e: URISyntaxException) {
                    throw RuntimeException(e)
                }
            }
    }

    /**
     * {@code PUT  /books/:id} : Updates an existing book.
     *
     * @param id the id of the bookDTO to save.
     * @param bookDTO the bookDTO to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated bookDTO,
     * or with status `400 (Bad Request)` if the bookDTO is not valid,
     * or with status `500 (Internal Server Error)` if the bookDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/books/{id}")
    fun updateBook(
        @PathVariable(value = "id", required = false) id: Long,
        @Valid @RequestBody bookDTO: BookDTO
    ): Mono<ResponseEntity<BookDTO>> {
        log.debug("REST request to update Book : {}, {}", id, bookDTO)
        if (bookDTO.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, bookDTO.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        return bookRepository.existsById(id).flatMap {
            if (!it) {
                return@flatMap Mono.error(BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"))
            }

            bookService.update(bookDTO)
                .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND)))
                .map { result ->
                    ResponseEntity.ok()
                        .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
                        .body(result)
                }
        }
    }

    /**
     * {@code PATCH  /books/:id} : Partial updates given fields of an existing book, field will ignore if it is null
     *
     * @param id the id of the bookDTO to save.
     * @param bookDTO the bookDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated bookDTO,
     * or with status {@code 400 (Bad Request)} if the bookDTO is not valid,
     * or with status {@code 404 (Not Found)} if the bookDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the bookDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = ["/books/{id}"], consumes = ["application/json", "application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateBook(
        @PathVariable(value = "id", required = false) id: Long,
        @NotNull @RequestBody bookDTO: BookDTO
    ): Mono<ResponseEntity<BookDTO>> {
        log.debug("REST request to partial update Book partially : {}, {}", id, bookDTO)
        if (bookDTO.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, bookDTO.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        return bookRepository.existsById(id).flatMap {
            if (!it) {
                return@flatMap Mono.error(BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"))
            }

            val result = bookService.partialUpdate(bookDTO)

            result
                .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND)))
                .map {
                    ResponseEntity.ok()
                        .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, it.id.toString()))
                        .body(it)
                }
        }
    }

    /**
     * `GET  /books` : get all the books.
     *
     * @param pageable the pagination information.
     * @param request a [ServerHttpRequest] request.

     * @return the [ResponseEntity] with status `200 (OK)` and the list of books in body.
     */
    @GetMapping("/books")
    fun getAllBooks(@org.springdoc.api.annotations.ParameterObject pageable: Pageable, request: ServerHttpRequest): Mono<ResponseEntity<List<BookDTO>>> {

        log.debug("REST request to get a page of Books")
        return bookService.countAll()
            .zipWith(bookService.findAll(pageable).collectList())
            .map {
                ResponseEntity.ok().headers(
                    PaginationUtil.generatePaginationHttpHeaders(
                        UriComponentsBuilder.fromHttpRequest(request),
                        PageImpl(it.t2, pageable, it.t1)
                    )
                ).body(it.t2)
            }
    }

    /**
     * `GET  /books/:id` : get the "id" book.
     *
     * @param id the id of the bookDTO to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the bookDTO, or with status `404 (Not Found)`.
     */
    @GetMapping("/books/{id}")
    fun getBook(@PathVariable id: Long): Mono<ResponseEntity<BookDTO>> {
        log.debug("REST request to get Book : $id")
        val bookDTO = bookService.findOne(id)
        return ResponseUtil.wrapOrNotFound(bookDTO)
    }
    /**
     *  `DELETE  /books/:id` : delete the "id" book.
     *
     * @param id the id of the bookDTO to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/books/{id}")
    fun deleteBook(@PathVariable id: Long): Mono<ResponseEntity<Void>> {
        log.debug("REST request to delete Book : $id")
        return bookService.delete(id)
            .then(
                Mono.just(
                    ResponseEntity.noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build<Void>()
                )
            )
    }
}
