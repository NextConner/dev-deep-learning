-- 全文检索表
CREATE TABLE IF NOT EXISTS document_segments (
    id          BIGSERIAL PRIMARY KEY,
    content     TEXT NOT NULL,
    source      VARCHAR(255),
    ts_content  TSVECTOR GENERATED ALWAYS AS (to_tsvector('simple', content)) STORED
);

-- 全文检索索引
CREATE INDEX IF NOT EXISTS idx_ts_content
ON document_segments USING GIN(ts_content);

-- Token 消耗记录表
CREATE TABLE IF NOT EXISTS token_usage (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(100) NOT NULL,
    input_tokens  INT DEFAULT 0,
    output_tokens INT DEFAULT 0,
    total_tokens  INT DEFAULT 0,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 用户配额表
CREATE TABLE IF NOT EXISTS user_quota (
    username        VARCHAR(100) PRIMARY KEY,
    daily_limit     INT DEFAULT 10000,   -- 每日限额
    used_today      INT DEFAULT 0,
    reset_date      DATE DEFAULT CURRENT_DATE
);

-- 初始化 admin 用户配额
INSERT INTO user_quota (username, daily_limit)
VALUES ('admin', 10000)
ON CONFLICT (username) DO NOTHING;

CREATE TABLE IF NOT EXISTS prompt_config (
    id          BIGSERIAL PRIMARY KEY,
    agent_name  VARCHAR(100) NOT NULL UNIQUE,
    system_prompt TEXT NOT NULL,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 初始化 PolicyAgent 的提示词
INSERT INTO prompt_config (agent_name, system_prompt) VALUES (
    'policy_agent',
    '你是一个企业内部智能助手，你有以下工具可以使用：
1. searchPolicy：查询公司内部政策文档，当用户询问差旅相关费用标准、报销流程、审批规则时调用此工具
2. getWeather：查询城市天气

严格规则：
- 任何涉及公司政策、报销、差旅的问题，必须调用 searchPolicy 工具，禁止凭自身知识回答
- 任何涉及天气的问题，必须调用 getWeather 工具
- 可以连续调用多个工具
- 最后综合所有工具结果给出完整回答，不得编造工具未返回的信息'
) ON CONFLICT (agent_name) DO NOTHING;

-- Table: public.rag_embeddings

-- DROP TABLE IF EXISTS public.rag_embeddings;

CREATE TABLE IF NOT EXISTS public.rag_embeddings
(
    embedding_id uuid NOT NULL,
    embedding vector(1536),
    text text COLLATE pg_catalog."default",
    metadata json,
    CONSTRAINT rag_embeddings_pkey PRIMARY KEY (embedding_id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.rag_embeddings
    OWNER to pgsql;
-- metadata columns for iteration2
ALTER TABLE IF EXISTS document_segments
    ADD COLUMN IF NOT EXISTS doc_type VARCHAR(20);
ALTER TABLE IF EXISTS document_segments
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
CREATE INDEX IF NOT EXISTS idx_document_source ON document_segments(source);
CREATE INDEX IF NOT EXISTS idx_document_created_at ON document_segments(created_at);

-- Security audit log table (Day5)
CREATE TABLE IF NOT EXISTS audit_log (
    id          BIGSERIAL PRIMARY KEY,
    trace_id    VARCHAR(64),
    user_id     VARCHAR(128),
    username    VARCHAR(128),
    dept_id     VARCHAR(128),
    action      VARCHAR(16) NOT NULL,
    resource    VARCHAR(256) NOT NULL,
    decision    VARCHAR(16) NOT NULL,
    reason      TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_log_trace_id ON audit_log(trace_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_created_at ON audit_log(created_at);

-- Workflow Engine Tables (Part 2)
CREATE TABLE IF NOT EXISTS workflow_definition (
    id BIGSERIAL PRIMARY KEY,
    workflow_code VARCHAR(50) UNIQUE NOT NULL,
    workflow_name VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    version INT DEFAULT 1,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS workflow_state (
    id BIGSERIAL PRIMARY KEY,
    workflow_id BIGINT REFERENCES workflow_definition(id),
    state_code VARCHAR(50) NOT NULL,
    state_name VARCHAR(100) NOT NULL,
    state_type VARCHAR(20) NOT NULL,
    timeout_hours INT,
    UNIQUE(workflow_id, state_code)
);

CREATE TABLE IF NOT EXISTS workflow_transition (
    id BIGSERIAL PRIMARY KEY,
    workflow_id BIGINT REFERENCES workflow_definition(id),
    from_state_id BIGINT REFERENCES workflow_state(id),
    to_state_id BIGINT REFERENCES workflow_state(id),
    transition_name VARCHAR(100) NOT NULL,
    condition_expression VARCHAR(500),
    required_role VARCHAR(50),
    action_bean VARCHAR(100),
    priority INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS workflow_instance (
    id BIGSERIAL PRIMARY KEY,
    workflow_id BIGINT REFERENCES workflow_definition(id),
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    current_state_id BIGINT REFERENCES workflow_state(id),
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    UNIQUE(entity_type, entity_id)
);

CREATE TABLE IF NOT EXISTS workflow_history (
    id BIGSERIAL PRIMARY KEY,
    instance_id BIGINT REFERENCES workflow_instance(id),
    from_state_id BIGINT,
    to_state_id BIGINT NOT NULL,
    operator VARCHAR(50) NOT NULL,
    transition_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    comment TEXT
);

CREATE INDEX IF NOT EXISTS idx_workflow_instance_entity ON workflow_instance(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_workflow_history_instance ON workflow_history(instance_id);

-- Seed ORDER_APPROVAL_V1 workflow
INSERT INTO workflow_definition (workflow_code, workflow_name, entity_type, version, is_active, created_at)
VALUES ('ORDER_APPROVAL_V1', '订单审批流程V1', 'OMS_ORDER', 1, true, CURRENT_TIMESTAMP)
ON CONFLICT (workflow_code) DO NOTHING;

-- Seed workflow states
INSERT INTO workflow_state (workflow_id, state_code, state_name, state_type, timeout_hours)
SELECT id, 'PLACED', '已下单', 'START', 24 FROM workflow_definition WHERE workflow_code = 'ORDER_APPROVAL_V1'
ON CONFLICT (workflow_id, state_code) DO NOTHING;

INSERT INTO workflow_state (workflow_id, state_code, state_name, state_type, timeout_hours)
SELECT id, 'SALES_CONFIRMED', '销售确认', 'NORMAL', 48 FROM workflow_definition WHERE workflow_code = 'ORDER_APPROVAL_V1'
ON CONFLICT (workflow_id, state_code) DO NOTHING;

INSERT INTO workflow_state (workflow_id, state_code, state_name, state_type, timeout_hours)
SELECT id, 'ORDER_REVIEWED', '订单审核', 'NORMAL', 24 FROM workflow_definition WHERE workflow_code = 'ORDER_APPROVAL_V1'
ON CONFLICT (workflow_id, state_code) DO NOTHING;

INSERT INTO workflow_state (workflow_id, state_code, state_name, state_type, timeout_hours)
SELECT id, 'WAREHOUSE_CONFIRMED', '仓库确认', 'NORMAL', 48 FROM workflow_definition WHERE workflow_code = 'ORDER_APPROVAL_V1'
ON CONFLICT (workflow_id, state_code) DO NOTHING;

INSERT INTO workflow_state (workflow_id, state_code, state_name, state_type, timeout_hours)
SELECT id, 'OUTBOUND_REGISTERED', '出库登记', 'NORMAL', 24 FROM workflow_definition WHERE workflow_code = 'ORDER_APPROVAL_V1'
ON CONFLICT (workflow_id, state_code) DO NOTHING;

INSERT INTO workflow_state (workflow_id, state_code, state_name, state_type, timeout_hours)
SELECT id, 'SHIPMENT_CONFIRMED', '发货确认', 'NORMAL', 72 FROM workflow_definition WHERE workflow_code = 'ORDER_APPROVAL_V1'
ON CONFLICT (workflow_id, state_code) DO NOTHING;

INSERT INTO workflow_state (workflow_id, state_code, state_name, state_type, timeout_hours)
SELECT id, 'CUSTOMER_RECEIVED', '客户签收', 'END', NULL FROM workflow_definition WHERE workflow_code = 'ORDER_APPROVAL_V1'
ON CONFLICT (workflow_id, state_code) DO NOTHING;

-- Seed workflow transitions
INSERT INTO workflow_transition (workflow_id, from_state_id, to_state_id, transition_name, condition_expression, required_role, priority)
SELECT
    wd.id,
    (SELECT id FROM workflow_state WHERE workflow_id = wd.id AND state_code = 'PLACED'),
    (SELECT id FROM workflow_state WHERE workflow_id = wd.id AND state_code = 'SALES_CONFIRMED'),
    '销售确认', NULL, 'ROLE_SALES', 0
FROM workflow_definition wd WHERE wd.workflow_code = 'ORDER_APPROVAL_V1';

INSERT INTO workflow_transition (workflow_id, from_state_id, to_state_id, transition_name, condition_expression, required_role, priority)
SELECT
    wd.id,
    (SELECT id FROM workflow_state WHERE workflow_id = wd.id AND state_code = 'SALES_CONFIRMED'),
    (SELECT id FROM workflow_state WHERE workflow_id = wd.id AND state_code = 'ORDER_REVIEWED'),
    '订单审核', NULL, 'ROLE_REVIEWER', 0
FROM workflow_definition wd WHERE wd.workflow_code = 'ORDER_APPROVAL_V1';

INSERT INTO workflow_transition (workflow_id, from_state_id, to_state_id, transition_name, condition_expression, required_role, priority)
SELECT
    wd.id,
    (SELECT id FROM workflow_state WHERE workflow_id = wd.id AND state_code = 'ORDER_REVIEWED'),
    (SELECT id FROM workflow_state WHERE workflow_id = wd.id AND state_code = 'WAREHOUSE_CONFIRMED'),
    '仓库确认', NULL, 'ROLE_WAREHOUSE', 0
FROM workflow_definition wd WHERE wd.workflow_code = 'ORDER_APPROVAL_V1';

INSERT INTO workflow_transition (workflow_id, from_state_id, to_state_id, transition_name, condition_expression, required_role, priority)
SELECT
    wd.id,
    (SELECT id FROM workflow_state WHERE workflow_id = wd.id AND state_code = 'WAREHOUSE_CONFIRMED'),
    (SELECT id FROM workflow_state WHERE workflow_id = wd.id AND state_code = 'OUTBOUND_REGISTERED'),
    '出库登记', NULL, 'ROLE_WAREHOUSE', 0
FROM workflow_definition wd WHERE wd.workflow_code = 'ORDER_APPROVAL_V1';

INSERT INTO workflow_transition (workflow_id, from_state_id, to_state_id, transition_name, condition_expression, required_role, priority)
SELECT
    wd.id,
    (SELECT id FROM workflow_state WHERE workflow_id = wd.id AND state_code = 'OUTBOUND_REGISTERED'),
    (SELECT id FROM workflow_state WHERE workflow_id = wd.id AND state_code = 'SHIPMENT_CONFIRMED'),
    '发货确认', NULL, 'ROLE_LOGISTICS', 0
FROM workflow_definition wd WHERE wd.workflow_code = 'ORDER_APPROVAL_V1';

INSERT INTO workflow_transition (workflow_id, from_state_id, to_state_id, transition_name, condition_expression, required_role, priority)
SELECT
    wd.id,
    (SELECT id FROM workflow_state WHERE workflow_id = wd.id AND state_code = 'SHIPMENT_CONFIRMED'),
    (SELECT id FROM workflow_state WHERE workflow_id = wd.id AND state_code = 'CUSTOMER_RECEIVED'),
    '客户签收', NULL, NULL, 0
FROM workflow_definition wd WHERE wd.workflow_code = 'ORDER_APPROVAL_V1';
