$baseUrl = "http://localhost:8080/api"


# ==========================================
# 선수 생성 또는 기존 선수 찾기
# ==========================================

function Get-OrCreatePlayer(
    $name,
    $position,
    $team
) {

    $players =
        Invoke-RestMethod `
            -Uri "$baseUrl/players" `
            -Method GET


    $existing =
        $players |
        Where-Object {
            $_.name -eq $name
        } |
        Select-Object -First 1


    if ($existing) {

        Write-Host "$name already exists."

        return $existing
    }


    $body = @{
        name = $name
        position = $position
        team = $team
    } | ConvertTo-Json


    return Invoke-RestMethod `
        -Uri "$baseUrl/players" `
        -Method POST `
        -ContentType "application/json; charset=utf-8" `
        -Body $body
}


# ==========================================
# 선수 생성
# ==========================================

$jefferson =
    Get-OrCreatePlayer `
        "Justin Jefferson" `
        "WR" `
        "MIN"


$bijan =
    Get-OrCreatePlayer `
        "Bijan Robinson" `
        "RB" `
        "ATL"


Write-Host ""
Write-Host "Justin Jefferson ID:" $jefferson.id
Write-Host "Bijan Robinson ID:" $bijan.id


# ==========================================
# Jefferson 경기 기록
# ==========================================

$jeffersonStats = @(

    @{
        season = 2026
        week = 1
        fantasyPoints = 22.4
        passingYards = 0
        rushingYards = 8
        receivingYards = 117
        touchdowns = 1
    },

    @{
        season = 2026
        week = 2
        fantasyPoints = 18.7
        passingYards = 0
        rushingYards = 3
        receivingYards = 92
        touchdowns = 1
    },

    @{
        season = 2026
        week = 3
        fantasyPoints = 24.9
        passingYards = 0
        rushingYards = 5
        receivingYards = 134
        touchdowns = 1
    },

    @{
        season = 2026
        week = 4
        fantasyPoints = 27.1
        passingYards = 0
        rushingYards = 7
        receivingYards = 148
        touchdowns = 2
    }
)


# 기록이 없을 때만 추가
$currentJeffersonStats =
    Invoke-RestMethod `
        -Uri "$baseUrl/players/$($jefferson.id)/stats"


if ($currentJeffersonStats.Count -eq 0) {

    foreach ($stat in $jeffersonStats) {

        Invoke-RestMethod `
            -Uri "$baseUrl/players/$($jefferson.id)/stats" `
            -Method POST `
            -ContentType "application/json; charset=utf-8" `
            -Body ($stat | ConvertTo-Json) |
            Out-Null
    }
}


# ==========================================
# Bijan 경기 기록
# ==========================================

$bijanStats = @(

    @{
        season = 2026
        week = 1
        fantasyPoints = 18.6
        passingYards = 0
        rushingYards = 97
        receivingYards = 32
        touchdowns = 1
    },

    @{
        season = 2026
        week = 2
        fantasyPoints = 21.3
        passingYards = 0
        rushingYards = 112
        receivingYards = 28
        touchdowns = 1
    },

    @{
        season = 2026
        week = 3
        fantasyPoints = 17.9
        passingYards = 0
        rushingYards = 85
        receivingYards = 41
        touchdowns = 1
    },

    @{
        season = 2026
        week = 4
        fantasyPoints = 24.1
        passingYards = 0
        rushingYards = 121
        receivingYards = 47
        touchdowns = 2
    }
)


$currentBijanStats =
    Invoke-RestMethod `
        -Uri "$baseUrl/players/$($bijan.id)/stats"


if ($currentBijanStats.Count -eq 0) {

    foreach ($stat in $bijanStats) {

        Invoke-RestMethod `
            -Uri "$baseUrl/players/$($bijan.id)/stats" `
            -Method POST `
            -ContentType "application/json; charset=utf-8" `
            -Body ($stat | ConvertTo-Json) |
            Out-Null
    }
}


# ==========================================
# Trade Value 확인
# ==========================================

Write-Host ""
Write-Host "Jefferson Value"

Invoke-RestMethod `
    -Uri "$baseUrl/players/$($jefferson.id)/value"


Write-Host ""
Write-Host "Bijan Value"

Invoke-RestMethod `
    -Uri "$baseUrl/players/$($bijan.id)/value"


# ==========================================
# Trade Simulation
# ==========================================

$tradeBody = @{

    teamAPlayerIds =
        @($jefferson.id)

    teamBPlayerIds =
        @($bijan.id)

} | ConvertTo-Json


Write-Host ""
Write-Host "Trade Result"


Invoke-RestMethod `
    -Uri "$baseUrl/trades/simulate" `
    -Method POST `
    -ContentType "application/json; charset=utf-8" `
    -Body $tradeBody