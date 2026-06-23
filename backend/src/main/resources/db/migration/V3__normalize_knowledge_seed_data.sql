UPDATE knowledge_items
SET title = '示例资质',
    category = '企业资质',
    content = '公司具备 ISO 认证和多个软件实施项目经验。',
    tags = 'ISO,软件'
WHERE title = 'Demo Qualification'
  AND category = 'Qualification';

UPDATE knowledge_items
SET title = '企业能力说明',
    category = '企业能力',
    content = '公司具备 AI 应用开发、企业知识库、文档审查、系统集成和私有化部署经验。',
    tags = NULL
WHERE title = '??????';

UPDATE knowledge_items
SET category = '企业能力'
WHERE category = 'company';
