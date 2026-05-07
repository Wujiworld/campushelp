$ErrorActionPreference = "Stop"

param(
    [string]$BaseUrl = "http://127.0.0.1:8081",
    [string]$Phone = "13800010001",
    [string]$Password = "123456",
    [switch]$WithSkyWalking
)

function Write-Step($msg) {
    Write-Host ""
    Write-Host "==== $msg ====" -ForegroundColor Cyan
}

function Invoke-JsonPost($url, $body, $headers) {
    return Invoke-RestMethod -Method Post -Uri $url -Body ($body | ConvertTo-Json -Depth 8) -Headers $headers -ContentType "application/json"
}

Write-Step "0) Preconditions"
Write-Host "Gateway: $BaseUrl"
Write-Host "User login phone: $Phone"
Write-Host "Ensure services are up: gateway/user/order/product/life/search-indexer"

Write-Step "1) Health checks"
$healthUrls = @(
    "$BaseUrl/actuator/health",
    "http://127.0.0.1:8082/actuator/health",
    "http://127.0.0.1:8083/actuator/health",
    "http://127.0.0.1:8084/actuator/health",
    "http://127.0.0.1:8085/actuator/health",
    "http://127.0.0.1:8091/actuator/health"
)
foreach ($u in $healthUrls) {
    try {
        $res = Invoke-RestMethod -Method Get -Uri $u
        Write-Host "[OK] $u => $($res.status)"
    } catch {
        Write-Host "[WARN] $u => failed"
    }
}

Write-Step "2) Login and get JWT"
$loginUrl = "$BaseUrl/campus-help-user/api/v3/auth/login"
$loginResp = Invoke-JsonPost -url $loginUrl -body @{ phone = $Phone; password = $Password } -headers @{}
$token = $loginResp.data.token
if (-not $token) {
    throw "Token not found from login response."
}
Write-Host "Token acquired."
$authHeaders = @{ Authorization = "Bearer $token" }

Write-Step "3) Feed publish"
$publishUrl = "$BaseUrl/campus-help-life/api/v3/feed/publish"
$content = "feed-demo-" + [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$publishResp = Invoke-JsonPost -url $publishUrl -body @{
    bizType = "SECONDHAND"
    bizId = "demo-1"
    content = $content
} -headers $authHeaders
Write-Host "Published feedId: $($publishResp.data.feedId)"

Write-Step "4) Feed timeline pull"
$timelineUrl = "$BaseUrl/campus-help-life/api/v3/feed/timeline?size=10"
$timelineResp = Invoke-RestMethod -Method Get -Uri $timelineUrl -Headers $authHeaders
$itemCount = 0
if ($timelineResp.data.items) { $itemCount = $timelineResp.data.items.Count }
Write-Host "Timeline items: $itemCount"

Write-Step "5) Trigger market queries (for search provider metrics)"
$marketUrl = "$BaseUrl/campus-help-life/api/v3/market/search?type=ALL&page=0&size=10"
$marketResp = Invoke-RestMethod -Method Get -Uri $marketUrl -Headers $authHeaders
$total = if ($marketResp.data.total) { $marketResp.data.total } else { 0 }
Write-Host "Market search total: $total"

Write-Step "6) Micrometer metrics snapshot"
$metricEndpoints = @(
    "http://127.0.0.1:8085/actuator/metrics/campus.feed.publish",
    "http://127.0.0.1:8085/actuator/metrics/campus.feed.timeline.read",
    "http://127.0.0.1:8085/actuator/metrics/campus.feed.fanout",
    "http://127.0.0.1:8085/actuator/metrics/campus.search.query.total",
    "http://127.0.0.1:8085/actuator/metrics/campus.cache.hit",
    "http://127.0.0.1:8091/actuator/metrics/campus.search.reconcile.run"
)
foreach ($m in $metricEndpoints) {
    try {
        $res = Invoke-RestMethod -Method Get -Uri $m
        Write-Host "[METRIC] $m"
        Write-Host ($res | ConvertTo-Json -Depth 6)
    } catch {
        Write-Host "[WARN] metric endpoint unavailable: $m"
    }
}

if ($WithSkyWalking) {
    Write-Step "7) SkyWalking checks"
    Write-Host "Open SkyWalking UI and verify trace path:"
    Write-Host "gateway -> campus-help-life -> redis/rabbit interactions"
    Write-Host "Recommended query window: last 15 minutes"
    Write-Host "Filter endpoint contains /api/v3/feed and /api/v3/market/search"
}

Write-Step "Done"
Write-Host "Linkage and observability demo finished."
