package com.test.task;

import com.test.task.DocumentManager.Document;
import com.test.task.DocumentManager.SearchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

class DocumentManagerTest {
    private DocumentManager manager;

    @BeforeEach
    void setUp() {
        manager = new DocumentManager();
    }

    @Test
    void testSave_NewDocument_ShouldAssignIdAndSave() {
        Document doc = Document.builder()
                .title("Test Title")
                .content("Some content")
                .author(new DocumentManager.Author("1", "Author Name"))
                .build();

        Document savedDoc = manager.save(doc);

        assertThat(savedDoc.getId()).isNotNull();
        assertThat(savedDoc.getCreated()).isNotNull();
        assertThat(savedDoc.getTitle()).isEqualTo("Test Title");
    }

    @Test
    void testSave_ExistingDocument_ShouldUpdateContent() {
        Document doc = Document.builder()
                .id("123")
                .title("Old Title")
                .content("Old Content")
                .author(new DocumentManager.Author("1", "Author Name"))
                .created(Instant.now())
                .build();

        manager.save(doc);

        Document updatedDoc = doc.toBuilder().title("New Title").build();
        manager.save(updatedDoc);

        Optional<Document> result = manager.findById("123");
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("New Title");
    }

    @Test
    void testFindById_Found() {
        Document doc = Document.builder()
                .id("456")
                .title("Find Me")
                .content("Test Content")
                .author(new DocumentManager.Author("1", "Author Name"))
                .created(Instant.now())
                .build();

        manager.save(doc);

        Optional<Document> result = manager.findById("456");
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Find Me");
    }

    @Test
    void testFindById_NotFound() {
        Optional<Document> result = manager.findById("999");
        assertThat(result).isEmpty();
    }

    @Test
    void testSearch_ByTitlePrefix_ShouldReturnMatchingDocs() {
        Document doc1 = Document.builder()
                .id("1")
                .title("Hello World")
                .content("Some content")
                .author(new DocumentManager.Author("1", "Author Name"))
                .created(Instant.now())
                .build();

        Document doc2 = Document.builder()
                .id("2")
                .title("Hi there")
                .content("Other content")
                .author(new DocumentManager.Author("2", "Another Author"))
                .created(Instant.now())
                .build();

        manager.save(doc1);
        manager.save(doc2);

        SearchRequest request = SearchRequest.builder()
                .titlePrefixes(List.of("Hello"))
                .build();

        List<Document> result = manager.search(request);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Hello World");
    }

    @Test
    void testSearch_ByContent_ShouldReturnMatchingDocs() {
        Document doc1 = Document.builder()
                .id("1")
                .title("Title A")
                .content("Java programming guide")
                .author(new DocumentManager.Author("1", "Author Name"))
                .created(Instant.now())
                .build();

        Document doc2 = Document.builder()
                .id("2")
                .title("Title B")
                .content("C++ reference manual")
                .author(new DocumentManager.Author("2", "Another Author"))
                .created(Instant.now())
                .build();

        manager.save(doc1);
        manager.save(doc2);

        SearchRequest request = SearchRequest.builder()
                .containsContents(List.of("Java"))
                .build();

        List<Document> result = manager.search(request);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).contains("Java");
    }

    @Test
    void testSearch_ByAuthor_ShouldReturnMatchingDocs() {
        Document doc1 = Document.builder()
                .id("1")
                .title("Title A")
                .content("Java guide")
                .author(new DocumentManager.Author("1", "Author One"))
                .created(Instant.now())
                .build();

        Document doc2 = Document.builder()
                .id("2")
                .title("Title B")
                .content("C++ reference")
                .author(new DocumentManager.Author("2", "Author Two"))
                .created(Instant.now())
                .build();

        manager.save(doc1);
        manager.save(doc2);

        SearchRequest request = SearchRequest.builder()
                .authorIds(List.of("1"))
                .build();

        List<Document> result = manager.search(request);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAuthor().getId()).isEqualTo("1");
    }
}
