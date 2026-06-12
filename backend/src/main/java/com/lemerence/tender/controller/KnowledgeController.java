package com.lemerence.tender.controller;

import com.lemerence.tender.dto.KnowledgeItemRequest;
import com.lemerence.tender.model.KnowledgeItem;
import com.lemerence.tender.repository.KnowledgeItemRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {
    private final KnowledgeItemRepository repository;

    public KnowledgeController(KnowledgeItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<KnowledgeItem> list(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return repository.findTop20ByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByUpdatedAtDesc(keyword, keyword);
        }
        return repository.findAll();
    }

    @PostMapping
    public KnowledgeItem create(@Valid @RequestBody KnowledgeItemRequest request) {
        KnowledgeItem item = new KnowledgeItem();
        apply(item, request);
        return repository.save(item);
    }

    @PutMapping("/{id}")
    public KnowledgeItem update(@PathVariable Long id, @Valid @RequestBody KnowledgeItemRequest request) {
        KnowledgeItem item = repository.findById(id).orElseThrow();
        apply(item, request);
        return repository.save(item);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    private void apply(KnowledgeItem item, KnowledgeItemRequest request) {
        item.setTitle(request.title());
        item.setCategory(request.category());
        item.setContent(request.content());
        item.setTags(request.tags());
    }
}
