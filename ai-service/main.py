import json
import os
from typing import List, Optional

from dotenv import load_dotenv
from fastapi import FastAPI
from openai import OpenAI
from pydantic import BaseModel, Field

load_dotenv()
load_dotenv("../.env.local")

app = FastAPI(title="AI Tender Assistant Service", version="0.1.0")


class ReviewRequest(BaseModel):
    projectId: Optional[int] = None
    title: str
    tenderText: str
    bidText: str


class ReviewIssue(BaseModel):
    category: str
    severity: str
    requirement: str
    finding: str
    suggestion: str
    source: str


class ReviewResponse(BaseModel):
    summary: str
    riskLevel: str
    issues: List[ReviewIssue] = Field(default_factory=list)
    checklist: List[str] = Field(default_factory=list)


class DraftRequest(BaseModel):
    projectId: Optional[int] = None
    title: str
    section: str
    tenderText: str
    knowledgeContext: Optional[str] = None
    userRequirement: Optional[str] = None


class DraftResponse(BaseModel):
    title: str
    section: str
    content: str


@app.get("/health")
def health():
    return {"status": "ok", "service": "ai-service", "openaiConfigured": bool(os.getenv("OPENAI_API_KEY"))}


@app.post("/review", response_model=ReviewResponse)
def review(request: ReviewRequest):
    if os.getenv("OPENAI_API_KEY"):
        try:
            return _review_with_openai(request)
        except Exception as exc:
            fallback = _review_with_rules(request)
            fallback.summary = f"{fallback.summary}（AI 调用失败，已使用本地规则兜底：{type(exc).__name__}）"
            return fallback
    return _review_with_rules(request)


@app.post("/draft", response_model=DraftResponse)
def draft(request: DraftRequest):
    if os.getenv("OPENAI_API_KEY"):
        try:
            return _draft_with_openai(request)
        except Exception as exc:
            fallback = _draft_with_template(request)
            fallback.content = f"{fallback.content}\n\n> 注：AI 调用失败，已使用本地模板兜底：{type(exc).__name__}"
            return fallback
    return _draft_with_template(request)


def _client() -> OpenAI:
    return OpenAI(api_key=os.getenv("OPENAI_API_KEY"))


def _review_with_openai(request: ReviewRequest) -> ReviewResponse:
    prompt = f"""
你是严谨的中文招投标审标专家。请对用户上传的招标文件内容和投标文件内容进行审查。

要求：
1. 重点检查废标风险、未响应、疑似负偏离、评分点遗漏、报价/签章/格式/附件风险。
2. 每个问题必须给出风险等级、招标依据、标书发现、修改建议。
3. 输出严格 JSON，不要 Markdown。

招标文件：
{request.tenderText[:12000]}

投标文件：
{request.bidText[:12000]}
"""
    response = _client().responses.create(
        model=os.getenv("OPENAI_MODEL", "gpt-4.1-mini"),
        input=[
            {"role": "system", "content": "你只输出符合 schema 的 JSON。"},
            {"role": "user", "content": prompt},
        ],
        text={
            "format": {
                "type": "json_schema",
                "name": "tender_review",
                "schema": {
                    "type": "object",
                    "additionalProperties": False,
                    "properties": {
                        "summary": {"type": "string"},
                        "riskLevel": {"type": "string", "enum": ["低", "中", "高", "致命"]},
                        "issues": {
                            "type": "array",
                            "items": {
                                "type": "object",
                                "additionalProperties": False,
                                "properties": {
                                    "category": {"type": "string"},
                                    "severity": {"type": "string"},
                                    "requirement": {"type": "string"},
                                    "finding": {"type": "string"},
                                    "suggestion": {"type": "string"},
                                    "source": {"type": "string"},
                                },
                                "required": ["category", "severity", "requirement", "finding", "suggestion", "source"],
                            },
                        },
                        "checklist": {"type": "array", "items": {"type": "string"}},
                    },
                    "required": ["summary", "riskLevel", "issues", "checklist"],
                },
            }
        },
    )
    data = json.loads(response.output_text)
    return ReviewResponse(**data)


def _draft_with_openai(request: DraftRequest) -> DraftResponse:
    prompt = f"""
请为招投标项目生成投标文件章节初稿。

项目/文档标题：{request.title}
章节：{request.section}
用户要求：{request.userRequirement or "无"}

招标文件要点：
{request.tenderText[:10000]}

企业知识库上下文：
{(request.knowledgeContext or "暂无")[:8000]}

要求：
1. 使用正式中文投标文件风格。
2. 内容要贴合招标要求，不夸大、不承诺无法验证事项。
3. 包含必要小标题和可编辑段落。
4. 输出 JSON，content 字段可包含 Markdown。
"""
    response = _client().responses.create(
        model=os.getenv("OPENAI_MODEL", "gpt-4.1-mini"),
        input=[
            {"role": "system", "content": "你是专业中文标书编制顾问，只输出 JSON。"},
            {"role": "user", "content": prompt},
        ],
        text={
            "format": {
                "type": "json_schema",
                "name": "tender_draft",
                "schema": {
                    "type": "object",
                    "additionalProperties": False,
                    "properties": {
                        "title": {"type": "string"},
                        "section": {"type": "string"},
                        "content": {"type": "string"},
                    },
                    "required": ["title", "section", "content"],
                },
            }
        },
    )
    data = json.loads(response.output_text)
    return DraftResponse(**data)


def _review_with_rules(request: ReviewRequest) -> ReviewResponse:
    tender = request.tenderText
    bid = request.bidText
    checks = [
        ("资格", "资质", "检查投标人资质证书是否完整，并确认有效期。"),
        ("签章", "盖章", "补充所有要求签字盖章的位置，尤其投标函、报价表、授权书。"),
        ("报价", "报价", "核对总价、分项合计、大小写金额和最高限价。"),
        ("业绩", "业绩", "补充类似项目合同、中标通知书或验收证明。"),
        ("服务", "售后", "明确响应时间、服务周期、质保期和服务承诺。"),
        ("偏离", "偏离", "逐项填写商务/技术偏离表，避免实质性负偏离。"),
    ]
    issues: List[ReviewIssue] = []
    for category, keyword, suggestion in checks:
        if keyword in tender and keyword not in bid:
            issues.append(ReviewIssue(
                category=category,
                severity="高" if category in {"资格", "签章", "报价"} else "中",
                requirement=f"招标文件出现“{keyword}”相关要求。",
                finding=f"投标文件中未明显检索到“{keyword}”相关响应。",
                suggestion=suggestion,
                source="本地规则检索，建议人工核对原文页码。",
            ))

    risk = "低"
    if any(issue.severity == "高" for issue in issues):
        risk = "高"
    elif issues:
        risk = "中"

    return ReviewResponse(
        summary=f"已完成《{request.title}》规则化审查，发现 {len(issues)} 个需关注问题。",
        riskLevel=risk,
        issues=issues,
        checklist=[
            "逐项核对资格要求是否满足",
            "核对报价是否超过最高限价，大小写金额是否一致",
            "检查投标函、授权书、报价表、偏离表是否签字盖章",
            "检查技术参数和商务条款是否存在负偏离",
            "提交前确认使用的是最新澄清/答疑文件",
        ],
    )


def _draft_with_template(request: DraftRequest) -> DraftResponse:
    content = f"""# {request.section}

## 一、章节目标
本章节依据招标文件要求编制，围绕“{request.title}”项目的核心需求进行响应，确保内容完整、表述清晰、承诺可执行。

## 二、响应思路
1. 充分理解招标文件中的资格、商务、技术及服务要求。
2. 结合企业既有资质、人员、业绩和服务能力进行针对性响应。
3. 对关键评分点进行重点阐述，避免遗漏实质性条款。

## 三、实施内容
投标人将围绕项目目标建立专项工作机制，明确项目负责人、实施计划、质量控制、风险控制和售后服务安排，确保项目按期、按质完成。

## 四、保障措施
投标人将通过进度管理、质量复核、资料归档、沟通协调和问题闭环机制保障项目交付质量。
"""
    if request.userRequirement:
        content += f"\n## 五、补充要求响应\n{request.userRequirement}\n"
    if request.knowledgeContext:
        content += "\n## 六、企业能力引用\n已结合企业知识库素材进行响应，正式提交前请补充证书编号、业绩名称和证明文件页码。\n"
    return DraftResponse(title=request.title, section=request.section, content=content)
