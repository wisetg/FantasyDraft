import {
    useEffect,
    useState,
} from "react";

import {
    useNavigate,
    useParams,
} from "react-router-dom";

import {
    getPlayerById,
    getPlayerStats,
    getPlayerScoredStats,
    getPlayerAnalysis,
    getPlayerValue,
} from "../api/playerApi";

import AppNavigation
    from "../components/AppNavigation";


const SCORING_OPTIONS = [

    {
        value: "PPR",
        label: "PPR",
    },

    {
        value: "HALF_PPR",
        label: "Half-PPR",
    },

    {
        value: "STANDARD",
        label: "Standard",
    },

];


function PlayerDetailPage() {

    const { playerId } =
        useParams();


    const navigate =
        useNavigate();


    const [
        player,
        setPlayer,
    ] = useState(null);


    /*
     * 시즌 목록을 찾기 위한
     * 전체 Raw Stats
     */
    const [
        allStats,
        setAllStats,
    ] = useState([]);


    /*
     * 현재 선택한 규칙으로
     * 계산된 경기 데이터
     */
    const [
        stats,
        setStats,
    ] = useState([]);


    const [
        availableSeasons,
        setAvailableSeasons,
    ] = useState([]);


    const [
        selectedSeason,
        setSelectedSeason,
    ] = useState(null);


    /*
     * 기본 Scoring Format
     */
    const [
        selectedScoring,
        setSelectedScoring,
    ] = useState("PPR");


    const [
        analysis,
        setAnalysis,
    ] = useState(null);


    const [
        value,
        setValue,
    ] = useState(null);


    const [
        loading,
        setLoading,
    ] = useState(true);


    const [
        analysisLoading,
        setAnalysisLoading,
    ] = useState(false);


    const [
        error,
        setError,
    ] = useState(null);


    const [
        analysisError,
        setAnalysisError,
    ] = useState(null);


    /*
     * 최초 진입:
     *
     * Player와 전체 Stats를 가져옵니다.
     */
    useEffect(() => {

        const loadPlayerData =
            async () => {

                try {

                    setLoading(true);

                    setError(null);


                    const [
                        playerData,
                        statsData,
                    ] = await Promise.all([

                        getPlayerById(
                            playerId
                        ),

                        getPlayerStats(
                            playerId
                        ),

                    ]);


                    setPlayer(
                        playerData
                    );


                    setAllStats(
                        statsData
                    );


                    const seasons = [
                        ...new Set(

                            statsData.map(
                                (stat) =>
                                    stat.season
                            )

                        ),
                    ];


                    seasons.sort(
                        (a, b) =>
                            b - a
                    );


                    setAvailableSeasons(
                        seasons
                    );


                    if (
                        seasons.length > 0
                    ) {

                        setSelectedSeason(
                            seasons[0]
                        );

                    }


                } catch (err) {

                    console.error(err);


                    setError(
                        "선수 정보를 불러오는 중 문제가 발생했습니다."
                    );


                } finally {

                    setLoading(false);

                }

            };


        loadPlayerData();

    }, [playerId]);


    /*
     * Season 또는
     * Scoring Format 변경
     *
     * Stats / Analysis / Value를
     * 모두 다시 계산합니다.
     */
    useEffect(() => {

        if (
            selectedSeason === null
        ) {

            return;

        }


        const loadAnalysisData =
            async () => {

                try {

                    setAnalysisLoading(
                        true
                    );


                    setAnalysisError(
                        null
                    );


                    const [
                        scoredStatsData,
                        analysisData,
                        valueData,
                    ] = await Promise.all([

                        getPlayerScoredStats(

                            playerId,

                            selectedSeason,

                            selectedScoring
                        ),


                        getPlayerAnalysis(

                            playerId,

                            selectedSeason,

                            selectedScoring
                        ),


                        getPlayerValue(

                            playerId,

                            selectedSeason,

                            selectedScoring
                        ),

                    ]);


                    setStats(
                        scoredStatsData
                    );


                    setAnalysis(
                        analysisData
                    );


                    setValue(
                        valueData
                    );


                } catch (err) {

                    console.error(err);


                    setAnalysisError(
                        "분석 정보를 불러오지 못했습니다."
                    );


                } finally {

                    setAnalysisLoading(
                        false
                    );

                }

            };


        loadAnalysisData();

    }, [
        playerId,
        selectedSeason,
        selectedScoring,
    ]);


    const handleBack = () => {

        navigate("/players");

    };


    const handleSeasonChange = (
        event
    ) => {

        setSelectedSeason(

            Number(
                event.target.value
            )

        );

    };


    const handleScoringChange = (
        event
    ) => {

        setSelectedScoring(
            event.target.value
        );

    };


    const getTrendText = (
        trend
    ) => {

        switch (trend) {

            case "UP":

                return "↑ 상승";


            case "DOWN":

                return "↓ 하락";


            case "STABLE":

                return "→ 안정";


            default:

                return "-";

        }

    };


    const getTrendClassName = (
        trend
    ) => {

        switch (trend) {

            case "UP":

                return "trend-up";


            case "DOWN":

                return "trend-down";


            case "STABLE":

                return "trend-stable";


            default:

                return "";

        }

    };


    const getScoringLabel = (
        scoring
    ) => {

        const option =
            SCORING_OPTIONS.find(

                (item) =>
                    item.value ===
                    scoring

            );


        return option
            ? option.label
            : scoring;
    };


    if (loading) {

        return (

            <div className="page-container">

                <div className="loading-container">

                    선수 정보를 불러오는 중입니다...

                </div>

            </div>

        );

    }


    if (error) {

        return (

            <div className="page-container">

                <main className="content">

                    <button
                        className="back-button"
                        onClick={handleBack}
                    >

                        ← 선수 목록으로

                    </button>


                    <div className="error-box">

                        {error}

                    </div>

                </main>

            </div>

        );

    }


    if (!player) {

        return (

            <div className="page-container">

                <main className="content">

                    선수를 찾을 수 없습니다.

                </main>

            </div>

        );

    }


    return (

        <div className="page-container">


            <AppNavigation />


            <main className="content">


                <button
                    className="back-button"
                    onClick={handleBack}
                >

                    ← 선수 목록으로

                </button>


                <div className="player-detail-card">


                    <section className="player-detail-header">


                        <div>

                            <h1>

                                {player.name}

                            </h1>


                            <p className="player-sub-info">

                                {player.position}

                                <span className="separator">
                  |
                </span>

                                {player.team}

                            </p>

                        </div>


                        <div className="detail-controls">


                            <div className="detail-control">

                                <label
                                    htmlFor="season-select"
                                >

                                    Season

                                </label>


                                <select

                                    id="season-select"

                                    value={
                                        selectedSeason
                                        ?? ""
                                    }

                                    onChange={
                                        handleSeasonChange
                                    }

                                >

                                    {
                                        availableSeasons.map(
                                            (season) => (

                                                <option
                                                    key={season}
                                                    value={season}
                                                >

                                                    {season}

                                                </option>

                                            )
                                        )
                                    }

                                </select>

                            </div>


                            <div className="detail-control">

                                <label
                                    htmlFor="scoring-select"
                                >

                                    Scoring

                                </label>


                                <select

                                    id="scoring-select"

                                    value={
                                        selectedScoring
                                    }

                                    onChange={
                                        handleScoringChange
                                    }

                                >

                                    {
                                        SCORING_OPTIONS.map(
                                            (option) => (

                                                <option

                                                    key={
                                                        option.value
                                                    }

                                                    value={
                                                        option.value
                                                    }

                                                >

                                                    {
                                                        option.label
                                                    }

                                                </option>

                                            )
                                        )
                                    }

                                </select>

                            </div>


                        </div>

                    </section>


                    <hr />


                    <section className="detail-section">

                        <h2>
                            선수 정보
                        </h2>


                        <div className="info-grid">


                            <div className="info-box">

                <span className="info-label">

                  Player ID

                </span>

                                <strong>

                                    {player.id}

                                </strong>

                            </div>


                            <div className="info-box">

                <span className="info-label">

                  Position

                </span>

                                <strong>

                                    {player.position}

                                </strong>

                            </div>


                            <div className="info-box">

                <span className="info-label">

                  Team

                </span>

                                <strong>

                                    {player.team}

                                </strong>

                            </div>


                        </div>

                    </section>


                    <section className="detail-section">


                        <div className="section-title-row">


                            <div>

                                <h2>

                                    경기별 Fantasy 기록

                                </h2>


                                <p className="section-description">

                                    {selectedSeason} 시즌 ·{" "}

                                    {
                                        getScoringLabel(
                                            selectedScoring
                                        )
                                    }

                                </p>

                            </div>


                            <span className="record-count">

                {stats.length} Games

              </span>

                        </div>


                        {
                            analysisLoading
                                ? (

                                    <div className="empty-data-box">

                                        점수를 계산하는 중입니다...

                                    </div>

                                )
                                : stats.length === 0
                                    ? (

                                        <div className="empty-data-box">

                                            해당 시즌 기록이 없습니다.

                                        </div>

                                    )
                                    : (

                                        <div className="stats-table-wrapper">

                                            <table className="stats-table">


                                                <thead>

                                                <tr>

                                                    <th>
                                                        Season
                                                    </th>

                                                    <th>
                                                        Week
                                                    </th>

                                                    <th>
                                                        Fantasy PTS
                                                    </th>

                                                    <th>
                                                        REC
                                                    </th>

                                                    <th>
                                                        Pass YDS
                                                    </th>

                                                    <th>
                                                        Rush YDS
                                                    </th>

                                                    <th>
                                                        Rec YDS
                                                    </th>

                                                    <th>
                                                        TD
                                                    </th>

                                                </tr>

                                                </thead>


                                                <tbody>

                                                {
                                                    stats.map(
                                                        (stat) => (

                                                            <tr
                                                                key={
                                                                    stat.id
                                                                }
                                                            >

                                                                <td>
                                                                    {stat.season}
                                                                </td>

                                                                <td>
                                                                    Week {stat.week}
                                                                </td>

                                                                <td className="fantasy-point-cell">

                                                                    {
                                                                        stat
                                                                            .fantasyPoints
                                                                            .toFixed(1)
                                                                    }

                                                                </td>

                                                                <td>
                                                                    {stat.receptions}
                                                                </td>

                                                                <td>
                                                                    {stat.passingYards}
                                                                </td>

                                                                <td>
                                                                    {stat.rushingYards}
                                                                </td>

                                                                <td>
                                                                    {stat.receivingYards}
                                                                </td>

                                                                <td>
                                                                    {stat.touchdowns}
                                                                </td>

                                                            </tr>

                                                        )
                                                    )
                                                }

                                                </tbody>


                                            </table>

                                        </div>

                                    )
                        }

                    </section>


                    <section className="detail-section">


                        <div className="analysis-title-row">

                            <h2>
                                Fantasy Analysis
                            </h2>


                            <div className="analysis-badges">

                <span className="analysis-season-badge">

                  {selectedSeason}

                </span>


                                <span className="analysis-season-badge">

                  {
                      getScoringLabel(
                          selectedScoring
                      )
                  }

                </span>

                            </div>

                        </div>


                        {
                            analysisLoading
                                ? (

                                    <div className="empty-data-box">

                                        분석 중입니다...

                                    </div>

                                )
                                : analysisError
                                    ? (

                                        <div className="error-box">

                                            {analysisError}

                                        </div>

                                    )
                                    : !analysis
                                        ? (

                                            <div className="empty-data-box">

                                                분석할 데이터가 없습니다.

                                            </div>

                                        )
                                        : (

                                            <>


                                                <div className="analysis-grid">


                                                    <div className="analysis-box">

                      <span className="analysis-label">

                        Season Average

                      </span>

                                                        <strong>

                                                            {
                                                                analysis
                                                                    .seasonAverage
                                                                    .toFixed(2)
                                                            }

                                                        </strong>

                                                        <small>
                                                            PTS / Game
                                                        </small>

                                                    </div>


                                                    <div className="analysis-box">

                      <span className="analysis-label">

                        Recent Average

                      </span>

                                                        <strong>

                                                            {
                                                                analysis
                                                                    .recentAverage
                                                                    .toFixed(2)
                                                            }

                                                        </strong>

                                                        <small>
                                                            최근 3경기
                                                        </small>

                                                    </div>


                                                    <div className="analysis-box">

                      <span className="analysis-label">

                        Highest Score

                      </span>

                                                        <strong>

                                                            {
                                                                analysis
                                                                    .highestScore
                                                                    .toFixed(2)
                                                            }

                                                        </strong>

                                                        <small>
                                                            PTS
                                                        </small>

                                                    </div>


                                                    <div className="analysis-box">

                      <span className="analysis-label">

                        Lowest Score

                      </span>

                                                        <strong>

                                                            {
                                                                analysis
                                                                    .lowestScore
                                                                    .toFixed(2)
                                                            }

                                                        </strong>

                                                        <small>
                                                            PTS
                                                        </small>

                                                    </div>


                                                </div>


                                                <div className="trend-container">

                    <span className="trend-label">

                      Current Trend

                    </span>


                                                    <strong
                                                        className={
                                                            getTrendClassName(
                                                                analysis.trend
                                                            )
                                                        }
                                                    >

                                                        {
                                                            getTrendText(
                                                                analysis.trend
                                                            )
                                                        }

                                                    </strong>

                                                </div>


                                            </>

                                        )
                        }

                    </section>


                    <section className="detail-section">

                        <h2>
                            Trade Value
                        </h2>


                        {
                            analysisLoading
                                ? (

                                    <div className="empty-data-box">

                                        선수 가치를 계산하는 중입니다...

                                    </div>

                                )
                                : !value
                                    ? (

                                        <div className="empty-data-box">

                                            계산할 데이터가 없습니다.

                                        </div>

                                    )
                                    : (

                                        <div className="value-section">


                                            <div className="main-value-card">

                    <span>

                      Fantasy Trade Value

                    </span>


                                                <strong>

                                                    {
                                                        value
                                                            .valueScore
                                                            .toFixed(2)
                                                    }

                                                </strong>


                                                <small>

                                                    {
                                                        getScoringLabel(
                                                            selectedScoring
                                                        )
                                                    }

                                                    {" · "}

                                                    {selectedSeason}

                                                </small>

                                            </div>


                                            <div className="value-detail-grid">


                                                <div className="value-detail-box">

                      <span>
                        Season Average
                      </span>

                                                    <strong>

                                                        {
                                                            value
                                                                .seasonAverage
                                                                .toFixed(2)
                                                        }

                                                    </strong>

                                                </div>


                                                <div className="value-detail-box">

                      <span>
                        Recent Average
                      </span>

                                                    <strong>

                                                        {
                                                            value
                                                                .recentAverage
                                                                .toFixed(2)
                                                        }

                                                    </strong>

                                                </div>


                                                <div className="value-detail-box">

                      <span>
                        Consistency
                      </span>

                                                    <strong>

                                                        {
                                                            value
                                                                .consistencyScore
                                                                .toFixed(2)
                                                        }

                                                    </strong>

                                                </div>


                                                <div className="value-detail-box">

                      <span>
                        Trend Score
                      </span>

                                                    <strong>

                                                        {
                                                            value
                                                                .trendScore
                                                                .toFixed(2)
                                                        }

                                                    </strong>

                                                </div>


                                            </div>


                                        </div>

                                    )
                        }

                    </section>


                </div>

            </main>

        </div>

    );

}


export default PlayerDetailPage;
