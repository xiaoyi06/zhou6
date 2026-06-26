local action = ARGV[1]
local user_id = ARGV[2]
local event_json = ARGV[3]
local now_ms = ARGV[4]

local relation_key = KEYS[1]
local count_key = KEYS[2]
local pending_key = KEYS[3]

if action == 'VIEW' then
    redis.call('INCR', count_key)
    redis.call('ZADD', pending_key, now_ms, event_json)
    return 1
end

if action == 'LIKE' or action == 'FAVORITE' then
    local changed = redis.call('SADD', relation_key, user_id)
    if changed == 1 then
        redis.call('INCR', count_key)
        redis.call('ZADD', pending_key, now_ms, event_json)
    end
    return changed
end

if action == 'UNLIKE' or action == 'UNFAVORITE' then
    local changed = redis.call('SREM', relation_key, user_id)
    if changed == 1 then
        local current = tonumber(redis.call('GET', count_key) or '0')
        if current > 0 then
            redis.call('DECR', count_key)
        end
        redis.call('ZADD', pending_key, now_ms, event_json)
    end
    return changed
end

return -1
