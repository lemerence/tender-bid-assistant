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

/**
 * 企业知识库管理接口。
 *
 * <p>提供知识条目的查询、新增、更新、删除及附件关联能力。</p>
 */
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {
    private final KnowledgeItemRepository repository;
    private final AttachmentStorageService attachmentStorageService;

    /**
     * 创建知识库控制器。
     *
     * @param repository 知识条目仓储
     * @param attachmentStorageService 附件存储服务
     */
    public KnowledgeController(KnowledgeItemRepository repository, AttachmentStorageService attachmentStorageService) {
        this.repository = repository;
        this.attachmentStorageService = attachmentStorageService;
    }

    /**
     * 查询知识条目；提供关键词时仅返回标题或正文命中的最近记录。
     *
     * @param keyword 可选搜索关键词
     * @return 带附件元数据的知识条目列表
     */
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

    /**
     * 新增不带附件的知识条目。
     *
     * @param request 知识条目内容
     * @return 已保存的知识条目
     */
    @PostMapping
    public KnowledgeItem create(@Valid @RequestBody KnowledgeItemRequest request) {
        KnowledgeItem item = new KnowledgeItem();
        apply(item, request);
        KnowledgeItem saved = repository.save(item);
        attachFiles(saved);
        return saved;
    }

    /**
     * 新增知识条目并上传相关附件。
     *
     * @param title 标题
     * @param category 分类
     * @param content 正文
     * @param tags 可选标签
     * @param files 可选附件
     * @return 已保存且包含附件元数据的知识条目
     */
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

    /**
     * 更新指定知识条目的文本信息。
     *
     * @param id 知识条目 ID
     * @param request 更新内容
     * @return 更新后的知识条目
     */
    @PutMapping("/{id}")
    public KnowledgeItem update(@PathVariable Long id, @Valid @RequestBody KnowledgeItemRequest request) {
        KnowledgeItem item = repository.findById(id).orElseThrow();
        apply(item, request);
        KnowledgeItem saved = repository.save(item);
        attachFiles(saved);
        return saved;
    }

    /**
     * 删除指定知识条目。
     *
     * @param id 知识条目 ID
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    /**
     * 将请求字段复制到知识条目实体。
     *
     * @param item 待更新实体
     * @param request 请求数据
     */
    private void apply(KnowledgeItem item, KnowledgeItemRequest request) {
        item.setTitle(request.title());
        item.setCategory(request.category());
        item.setContent(request.content());
        item.setTags(request.tags());
    }

    /**
     * 根据 multipart 表单字段构造新的知识条目。
     *
     * @return 尚未持久化的知识条目
     */
    private KnowledgeItem newItem(String title, String category, String content, String tags) {
        KnowledgeItem item = new KnowledgeItem();
        item.setTitle(title);
        item.setCategory(category);
        item.setContent(content);
        item.setTags(tags);
        return item;
    }

    /**
     * 查询并填充不持久化在知识表中的附件元数据。
     *
     * @param item 知识条目
     */
    private void attachFiles(KnowledgeItem item) {
        item.setAttachments(attachmentStorageService.find("KNOWLEDGE", item.getId()));
    }
}
