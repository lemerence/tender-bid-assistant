package com.lemerence.tender.controller;

import com.lemerence.tender.dto.KnowledgeItemRequest;
import com.lemerence.tender.model.KnowledgeItem;
import com.lemerence.tender.repository.KnowledgeItemRepository;
import com.lemerence.tender.service.AttachmentStorageService;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {
    private final KnowledgeItemRepository repository;
    private final AttachmentStorageService attachmentStorageService;

    public KnowledgeController(KnowledgeItemRepository repository, AttachmentStorageService attachmentStorageService) {
        this.repository = repository;
        this.attachmentStorageService = attachmentStorageService;
    }

    @GetMapping
    public List<KnowledgeItem> list(@RequestParam(required = false) String keyword) {
        List<KnowledgeItem> items;
        if (keyword != null && !keyword.isBlank()) {
            items = repository.findTop20ByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByUpdatedAtDesc(keyword, keyword);
        } else {
            items = repository.findAll();
        }
        items.forEach(this::attachFiles);
        return items;
    }

    @PostMapping
    public KnowledgeItem create(@Valid @RequestBody KnowledgeItemRequest request) {
        KnowledgeItem item = new KnowledgeItem();
        apply(item, request);
        KnowledgeItem saved = repository.save(item);
        attachFiles(saved);
        return saved;
    }

    @PostMapping(path = "/with-files", consumes = "multipart/form-data")
    public KnowledgeItem createWithFiles(
            @RequestPart("title") String title,
            @RequestPart("category") String category,
            @RequestPart("content") String content,
            @RequestPart(value = "tags", required = false) String tags,
            @RequestPart(value = "files", required = false) MultipartFile[] files
    ) {
        KnowledgeItem item = repository.save(newItem(title, category, content, tags));
        item.setAttachments(attachmentStorageService.storeAll("KNOWLEDGE", item.getId(), "知识库附件", files));
        return item;
    }

    @PutMapping("/{id}")
    public KnowledgeItem update(@PathVariable Long id, @Valid @RequestBody KnowledgeItemRequest request) {
        KnowledgeItem item = repository.findById(id).orElseThrow();
        apply(item, request);
        KnowledgeItem saved = repository.save(item);
        attachFiles(saved);
        return saved;
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

    private KnowledgeItem newItem(String title, String category, String content, String tags) {
        KnowledgeItem item = new KnowledgeItem();
        item.setTitle(title);
        item.setCategory(category);
        item.setContent(content);
        item.setTags(tags);
        return item;
    }

    private void attachFiles(KnowledgeItem item) {
        item.setAttachments(attachmentStorageService.find("KNOWLEDGE", item.getId()));
    }
}
