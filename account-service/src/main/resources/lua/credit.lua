local function to_units(value)
    value = tostring(value or '0')
    local integer, decimal = string.match(value, '^(%-?%d+)%.?(%d*)$')
    decimal = string.sub((decimal or '') .. '0000', 1, 4)
    return tonumber(integer .. decimal)
end

local function to_money(units)
    local sign = ''
    if units < 0 then
        sign = '-'
        units = -units
    end
    return string.format('%s%d.%04d', sign, math.floor(units / 10000), units % 10000)
end

local available = to_units(redis.call('HGET', KEYS[1], 'available') or '0')
local total = to_units(redis.call('HGET', KEYS[1], 'total') or '0')
local amount = to_units(ARGV[1])

redis.call('HSET', KEYS[1], 'available', to_money(available + amount))
redis.call('HSET', KEYS[1], 'total', to_money(total + amount))
return 1
