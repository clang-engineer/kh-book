package com.clangenginner.book.web.rest

import com.clangenginner.book.IntegrationTest
import com.clangenginner.book.domain.Book
import com.clangenginner.book.repository.BookRepository
import com.clangenginner.book.repository.EntityManager
import com.clangenginner.book.service.mapper.BookMapper
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.Random
import java.util.concurrent.atomic.AtomicLong
import kotlin.test.assertNotNull

/**
 * Integration tests for the [BookResource] REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient
@WithMockUser
class BookResourceIT {
    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var bookMapper: BookMapper

    @Autowired
    private lateinit var em: EntityManager

    @Autowired
    private lateinit var webTestClient: WebTestClient

    private lateinit var book: Book

    @AfterEach
    fun cleanup() {
        deleteEntities(em)
    }

    @BeforeEach
    fun initTest() {
        deleteEntities(em)
        book = createEntity(em)
    }

    @Test
    @Throws(Exception::class)
    fun createBook() {
        val databaseSizeBeforeCreate = bookRepository.findAll().collectList().block().size
        // Create the Book
        val bookDTO = bookMapper.toDto(book)
        webTestClient.post().uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(convertObjectToJsonBytes(bookDTO))
            .exchange()
            .expectStatus().isCreated

        // Validate the Book in the database
        val bookList = bookRepository.findAll().collectList().block()
        assertThat(bookList).hasSize(databaseSizeBeforeCreate + 1)
        val testBook = bookList[bookList.size - 1]

        assertThat(testBook.title).isEqualTo(DEFAULT_TITLE)
        assertThat(testBook.description).isEqualTo(DEFAULT_DESCRIPTION)
        assertThat(testBook.author).isEqualTo(DEFAULT_AUTHOR)
    }

    @Test
    @Throws(Exception::class)
    fun createBookWithExistingId() {
        // Create the Book with an existing ID
        book.id = 1L
        val bookDTO = bookMapper.toDto(book)

        val databaseSizeBeforeCreate = bookRepository.findAll().collectList().block().size
        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient.post().uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(convertObjectToJsonBytes(bookDTO))
            .exchange()
            .expectStatus().isBadRequest

        // Validate the Book in the database
        val bookList = bookRepository.findAll().collectList().block()
        assertThat(bookList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Throws(Exception::class)
    fun checkTitleIsRequired() {
        val databaseSizeBeforeTest = bookRepository.findAll().collectList().block().size
        // set the field null
        book.title = null

        // Create the Book, which fails.
        val bookDTO = bookMapper.toDto(book)

        webTestClient.post().uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(convertObjectToJsonBytes(bookDTO))
            .exchange()
            .expectStatus().isBadRequest

        val bookList = bookRepository.findAll().collectList().block()
        assertThat(bookList).hasSize(databaseSizeBeforeTest)
    }
    @Test
    @Throws(Exception::class)
    fun checkAuthorIsRequired() {
        val databaseSizeBeforeTest = bookRepository.findAll().collectList().block().size
        // set the field null
        book.author = null

        // Create the Book, which fails.
        val bookDTO = bookMapper.toDto(book)

        webTestClient.post().uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(convertObjectToJsonBytes(bookDTO))
            .exchange()
            .expectStatus().isBadRequest

        val bookList = bookRepository.findAll().collectList().block()
        assertThat(bookList).hasSize(databaseSizeBeforeTest)
    }

    @Test

    fun getAllBooks() {
        // Initialize the database
        bookRepository.save(book).block()

        // Get all the bookList
        webTestClient.get().uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id").value(hasItem(book.id?.toInt()))
            .jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE))
            .jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION))
            .jsonPath("$.[*].author").value(hasItem(DEFAULT_AUTHOR))
    }

    @Test

    fun getBook() {
        // Initialize the database
        bookRepository.save(book).block()

        val id = book.id
        assertNotNull(id)

        // Get the book
        webTestClient.get().uri(ENTITY_API_URL_ID, book.id)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id").value(`is`(book.id?.toInt()))
            .jsonPath("$.title").value(`is`(DEFAULT_TITLE))
            .jsonPath("$.description").value(`is`(DEFAULT_DESCRIPTION))
            .jsonPath("$.author").value(`is`(DEFAULT_AUTHOR))
    }
    @Test

    fun getNonExistingBook() {
        // Get the book
        webTestClient.get().uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }
    @Test
    fun putExistingBook() {
        // Initialize the database
        bookRepository.save(book).block()

        val databaseSizeBeforeUpdate = bookRepository.findAll().collectList().block().size

        // Update the book
        val updatedBook = bookRepository.findById(book.id).block()
        updatedBook.title = UPDATED_TITLE
        updatedBook.description = UPDATED_DESCRIPTION
        updatedBook.author = UPDATED_AUTHOR
        val bookDTO = bookMapper.toDto(updatedBook)

        webTestClient.put().uri(ENTITY_API_URL_ID, bookDTO.id)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(convertObjectToJsonBytes(bookDTO))
            .exchange()
            .expectStatus().isOk

        // Validate the Book in the database
        val bookList = bookRepository.findAll().collectList().block()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
        val testBook = bookList[bookList.size - 1]
        assertThat(testBook.title).isEqualTo(UPDATED_TITLE)
        assertThat(testBook.description).isEqualTo(UPDATED_DESCRIPTION)
        assertThat(testBook.author).isEqualTo(UPDATED_AUTHOR)
    }

    @Test
    fun putNonExistingBook() {
        val databaseSizeBeforeUpdate = bookRepository.findAll().collectList().block().size
        book.id = count.incrementAndGet()

        // Create the Book
        val bookDTO = bookMapper.toDto(book)

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient.put().uri(ENTITY_API_URL_ID, bookDTO.id)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(convertObjectToJsonBytes(bookDTO))
            .exchange()
            .expectStatus().isBadRequest

        // Validate the Book in the database
        val bookList = bookRepository.findAll().collectList().block()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Throws(Exception::class)
    fun putWithIdMismatchBook() {
        val databaseSizeBeforeUpdate = bookRepository.findAll().collectList().block().size
        book.id = count.incrementAndGet()

        // Create the Book
        val bookDTO = bookMapper.toDto(book)

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient.put().uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(convertObjectToJsonBytes(bookDTO))
            .exchange()
            .expectStatus().isBadRequest

        // Validate the Book in the database
        val bookList = bookRepository.findAll().collectList().block()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Throws(Exception::class)
    fun putWithMissingIdPathParamBook() {
        val databaseSizeBeforeUpdate = bookRepository.findAll().collectList().block().size
        book.id = count.incrementAndGet()

        // Create the Book
        val bookDTO = bookMapper.toDto(book)

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient.put().uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(convertObjectToJsonBytes(bookDTO))
            .exchange()
            .expectStatus().isEqualTo(405)

        // Validate the Book in the database
        val bookList = bookRepository.findAll().collectList().block()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Throws(Exception::class)
    fun partialUpdateBookWithPatch() {
        bookRepository.save(book).block()

        val databaseSizeBeforeUpdate = bookRepository.findAll().collectList().block().size

// Update the book using partial update
        val partialUpdatedBook = Book().apply {
            id = book.id

            title = UPDATED_TITLE
            description = UPDATED_DESCRIPTION
        }

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedBook.id)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(convertObjectToJsonBytes(partialUpdatedBook))
            .exchange()
            .expectStatus()
            .isOk

// Validate the Book in the database
        val bookList = bookRepository.findAll().collectList().block()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
        val testBook = bookList.last()
        assertThat(testBook.title).isEqualTo(UPDATED_TITLE)
        assertThat(testBook.description).isEqualTo(UPDATED_DESCRIPTION)
        assertThat(testBook.author).isEqualTo(DEFAULT_AUTHOR)
    }

    @Test
    @Throws(Exception::class)
    fun fullUpdateBookWithPatch() {
        bookRepository.save(book).block()

        val databaseSizeBeforeUpdate = bookRepository.findAll().collectList().block().size

// Update the book using partial update
        val partialUpdatedBook = Book().apply {
            id = book.id

            title = UPDATED_TITLE
            description = UPDATED_DESCRIPTION
            author = UPDATED_AUTHOR
        }

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedBook.id)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(convertObjectToJsonBytes(partialUpdatedBook))
            .exchange()
            .expectStatus()
            .isOk

// Validate the Book in the database
        val bookList = bookRepository.findAll().collectList().block()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
        val testBook = bookList.last()
        assertThat(testBook.title).isEqualTo(UPDATED_TITLE)
        assertThat(testBook.description).isEqualTo(UPDATED_DESCRIPTION)
        assertThat(testBook.author).isEqualTo(UPDATED_AUTHOR)
    }

    @Throws(Exception::class)
    fun patchNonExistingBook() {
        val databaseSizeBeforeUpdate = bookRepository.findAll().collectList().block().size
        book.id = count.incrementAndGet()

        // Create the Book
        val bookDTO = bookMapper.toDto(book)

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient.patch().uri(ENTITY_API_URL_ID, bookDTO.id)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(convertObjectToJsonBytes(bookDTO))
            .exchange()
            .expectStatus().isBadRequest

        // Validate the Book in the database
        val bookList = bookRepository.findAll().collectList().block()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Throws(Exception::class)
    fun patchWithIdMismatchBook() {
        val databaseSizeBeforeUpdate = bookRepository.findAll().collectList().block().size
        book.id = count.incrementAndGet()

        // Create the Book
        val bookDTO = bookMapper.toDto(book)

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient.patch().uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(convertObjectToJsonBytes(bookDTO))
            .exchange()
            .expectStatus().isBadRequest

        // Validate the Book in the database
        val bookList = bookRepository.findAll().collectList().block()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamBook() {
        val databaseSizeBeforeUpdate = bookRepository.findAll().collectList().block().size
        book.id = count.incrementAndGet()

        // Create the Book
        val bookDTO = bookMapper.toDto(book)

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient.patch().uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(convertObjectToJsonBytes(bookDTO))
            .exchange()
            .expectStatus().isEqualTo(405)

        // Validate the Book in the database
        val bookList = bookRepository.findAll().collectList().block()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test

    fun deleteBook() {
        // Initialize the database
        bookRepository.save(book).block()
        val databaseSizeBeforeDelete = bookRepository.findAll().collectList().block().size
        // Delete the book
        webTestClient.delete().uri(ENTITY_API_URL_ID, book.id)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNoContent

        // Validate the database contains one less item
        val bookList = bookRepository.findAll().collectList().block()
        assertThat(bookList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_TITLE = "AAAAAAAAAA"
        private const val UPDATED_TITLE = "BBBBBBBBBB"

        private const val DEFAULT_DESCRIPTION = "AAAAAAAAAA"
        private const val UPDATED_DESCRIPTION = "BBBBBBBBBB"

        private const val DEFAULT_AUTHOR = "AAAAAAAAAA"
        private const val UPDATED_AUTHOR = "BBBBBBBBBB"

        private val ENTITY_API_URL: String = "/api/books"
        private val ENTITY_API_URL_ID: String = ENTITY_API_URL + "/{id}"

        private val random: Random = Random()
        private val count: AtomicLong = AtomicLong(random.nextInt().toLong() + (2 * Integer.MAX_VALUE))

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Book {
            val book = Book(
                title = DEFAULT_TITLE,

                description = DEFAULT_DESCRIPTION,

                author = DEFAULT_AUTHOR

            )

            return book
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Book {
            val book = Book(
                title = UPDATED_TITLE,

                description = UPDATED_DESCRIPTION,

                author = UPDATED_AUTHOR

            )

            return book
        }

        fun deleteEntities(em: EntityManager) {
            try {
                em.deleteAll(Book::class.java).block()
            } catch (e: Exception) {
                // It can fail, if other entities are still referring this - it will be removed later.
            }
        }
    }
}
