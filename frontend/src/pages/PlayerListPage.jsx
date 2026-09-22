import {
    useEffect,
    useMemo,
    useState,
} from "react";

import {
    getPlayers,
} from "../api/playerApi";

import AppNavigation
    from "../components/AppNavigation";

import PlayerCard
    from "../components/PlayerCard";


function PlayerListPage() {

    const [players, setPlayers] = useState([]);
    const [searchTerm, setSearchTerm] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);


    useEffect(() => {

        const loadPlayers = async () => {

            try {
                const data = await getPlayers();
                setPlayers(data);
            } catch (err) {
                console.error(err);
                setError("선수 목록을 불러오지 못했습니다.");
            } finally {
                setLoading(false);
            }
        };

        loadPlayers();

    }, []);


    const filteredPlayers = useMemo(() => {

        const normalizedSearchTerm = searchTerm.trim().toLowerCase();

        if (!normalizedSearchTerm) {
            return players;
        }

        return players.filter(
            (player) =>
                player.name
                    ?.toLowerCase()
                    .includes(normalizedSearchTerm)
        );

    }, [players, searchTerm]);


    return (

        <div className="page-container player-search-page">

            <AppNavigation />


            <main className="content player-search-content">

                <section className="player-search-intro">

                    <span>PLAYER DATABASE</span>

                    <h1>Player Search</h1>

                    <p>
                        NFL 선수의 시즌 기록과 Fantasy 분석 데이터를 탐색하세요.
                    </p>

                </section>


                <label className="player-search-input-wrap">

                    <span className="sr-only">Search player</span>

                    <input
                        type="search"
                        value={searchTerm}
                        placeholder="Search player..."
                        onChange={(event) => setSearchTerm(event.target.value)}
                    />

                </label>


                <div className="player-search-list-header">

                    <span>
                        {loading
                            ? "선수 정보를 불러오는 중..."
                            : `${filteredPlayers.length}명 표시`}
                    </span>

                </div>


                <section className="player-search-list">

                    {loading && (
                        <div className="empty-data-box">
                            선수 정보를 불러오는 중입니다...
                        </div>
                    )}


                    {!loading && error && (
                        <div className="error-box">
                            {error}
                        </div>
                    )}


                    {!loading && !error && players.length === 0 && (
                        <div className="empty-data-box">
                            등록된 선수가 없습니다.
                        </div>
                    )}


                    {!loading && !error && players.length > 0
                        && filteredPlayers.length === 0 && (
                            <div className="empty-data-box">
                                검색 결과가 없습니다.
                            </div>
                        )}


                    {!loading && !error && filteredPlayers.map(
                        (player) => (
                            <PlayerCard
                                key={player.id}
                                player={player}
                            />
                        )
                    )}

                </section>

            </main>

        </div>

    );
}


export default PlayerListPage;
