package com.test.task;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {
    private static Set<Document> storage = new HashSet<Document>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        Document updatedDocument;

        if (document.getId() == null) {

            updatedDocument = document.toBuilder().id(UUID.randomUUID().toString()).created(Instant.now()).build();

        } else {

            storage.removeIf(d -> d.getId().equals(document.getId())); // Remove existing doc with same ID
            updatedDocument = document.toBuilder().created(document.getCreated()).build();

        }

        storage.add(updatedDocument);
        return updatedDocument;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        List<Document> result = new ArrayList<>();

        for (Document doc : storage) {
            if (matchesSearchRequest(doc, request)) {
                result.add(doc);
            }
        }

        return result;
    }

    /**
     * private method to check if the document matches the search request
     *
     * @param doc     - document to check
     * @param request - search request
     * @return true if document matches the request, false otherwise
     */
    private boolean matchesSearchRequest(Document doc, SearchRequest request) {
        List<Predicate<Document>> filters = new ArrayList<>();
    
        Optional.ofNullable(request.getTitlePrefixes())
                .filter(list -> !list.isEmpty())
                .ifPresent(prefixes -> filters.add(d -> prefixes.stream().anyMatch(d.getTitle()::startsWith)));
    
        Optional.ofNullable(request.getContainsContents())
                .filter(list -> !list.isEmpty())
                .ifPresent(contents -> filters.add(d -> contents.stream().anyMatch(d.getContent()::contains)));
    
        Optional.ofNullable(request.getAuthorIds())
                .filter(list -> !list.isEmpty())
                .ifPresent(authors -> filters.add(d -> authors.contains(d.getAuthor().getId())));
    
        Optional.ofNullable(request.getCreatedFrom())
                .ifPresent(from -> filters.add(d -> !d.getCreated().isBefore(from)));
    
        Optional.ofNullable(request.getCreatedTo())
                .ifPresent(to -> filters.add(d -> !d.getCreated().isAfter(to)));
    
        return filters.stream().allMatch(filter -> filter.test(doc));
    }    

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {

        return storage.stream().filter(d -> d.getId().equals(id)).findFirst();
         
    }

    @Data
    @Builder(toBuilder = true)
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder(toBuilder = true)
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}