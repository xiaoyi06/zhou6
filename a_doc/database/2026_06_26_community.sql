CREATE TABLE IF NOT EXISTS community_post (
    id BIGINT PRIMARY KEY,
    author_user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    audit_status VARCHAR(32) NOT NULL DEFAULT 'APPROVED',
    publish_status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED',
    top_flag SMALLINT NOT NULL DEFAULT 0,
    featured_flag SMALLINT NOT NULL DEFAULT 0,
    view_count BIGINT NOT NULL DEFAULT 0,
    like_count BIGINT NOT NULL DEFAULT 0,
    comment_count BIGINT NOT NULL DEFAULT 0,
    favorite_count BIGINT NOT NULL DEFAULT 0,
    heat_score NUMERIC(20, 2) NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_community_post_feed ON community_post (publish_status, audit_status, deleted_at, top_flag DESC, featured_flag DESC, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_community_post_author ON community_post (author_user_id, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_community_post_heat ON community_post (heat_score DESC, id DESC);

CREATE TABLE IF NOT EXISTS community_post_resource (
    id BIGINT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    file_id BIGINT NOT NULL,
    resource_type VARCHAR(32) NOT NULL DEFAULT 'IMAGE',
    sort_order INTEGER NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_community_post_resource_post ON community_post_resource (post_id, sort_order, id);

CREATE TABLE IF NOT EXISTS community_comment (
    id BIGINT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    parent_id BIGINT NULL,
    root_id BIGINT NULL,
    author_user_id BIGINT NOT NULL,
    reply_to_user_id BIGINT NULL,
    content TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
    like_count BIGINT NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_community_comment_post ON community_comment (post_id, deleted_at, create_time, id);
CREATE INDEX IF NOT EXISTS idx_community_comment_parent ON community_comment (parent_id);
CREATE INDEX IF NOT EXISTS idx_community_comment_root ON community_comment (root_id);

CREATE TABLE IF NOT EXISTS community_post_like (
    id BIGINT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_community_post_like UNIQUE (post_id, user_id)
);

CREATE TABLE IF NOT EXISTS community_comment_like (
    id BIGINT PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_community_comment_like UNIQUE (comment_id, user_id)
);

CREATE TABLE IF NOT EXISTS community_favorite (
    id BIGINT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_community_favorite UNIQUE (post_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_community_favorite_user ON community_favorite (user_id, create_time DESC);

CREATE TABLE IF NOT EXISTS community_user_follow (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_user_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_community_user_follow UNIQUE (user_id, target_user_id)
);

CREATE TABLE IF NOT EXISTS community_user_favorite (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_user_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_community_user_favorite UNIQUE (user_id, target_user_id)
);

CREATE TABLE IF NOT EXISTS community_user_block (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_user_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_community_user_block UNIQUE (user_id, target_user_id)
);

CREATE TABLE IF NOT EXISTS community_mention (
    id BIGINT PRIMARY KEY,
    business_type VARCHAR(32) NOT NULL,
    business_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    operator_user_id BIGINT NOT NULL,
    mentioned_user_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_community_mention_user ON community_mention (mentioned_user_id, create_time DESC);

CREATE TABLE IF NOT EXISTS community_tag (
    id BIGINT PRIMARY KEY,
    tag_name VARCHAR(64) NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_community_tag_name UNIQUE (tag_name)
);

CREATE TABLE IF NOT EXISTS community_post_tag (
    id BIGINT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_community_post_tag UNIQUE (post_id, tag_id)
);

CREATE INDEX IF NOT EXISTS idx_community_post_tag_tag ON community_post_tag (tag_id, post_id);

CREATE TABLE IF NOT EXISTS community_user_tag_interest (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    browse_count BIGINT NOT NULL DEFAULT 0,
    like_count BIGINT NOT NULL DEFAULT 0,
    favorite_count BIGINT NOT NULL DEFAULT 0,
    comment_count BIGINT NOT NULL DEFAULT 0,
    interest_score INTEGER NOT NULL DEFAULT 0,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_community_user_tag_interest UNIQUE (user_id, tag_id)
);
