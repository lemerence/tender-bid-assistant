DELETE FROM knowledge_items
WHERE title = '企业能力说明'
  AND category = '企业能力'
  AND content = '公司具备 AI 应用开发、企业知识库、文档审查、系统集成和私有化部署经验。'
  AND EXISTS (
      SELECT 1
      FROM knowledge_items original
      WHERE original.title = '企业能力说明'
        AND original.category = '企业能力'
        AND original.content = '公司具备AI应用开发、企业知识库、文档审查、系统集成和私有化部署经验。'
  );
