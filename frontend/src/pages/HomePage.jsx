import {
    Link,
} from "react-router-dom";

import AppNavigation
    from "../components/AppNavigation";


function HomePage() {

    return (

        <div className="home-page">

            <AppNavigation />


            <main className="home-content">

                <span className="home-eyebrow">
                    NFL FANTASY INTELLIGENCE
                </span>


                <h1>
                    Make every roster decision<br />
                    with better context.
                </h1>


                <p className="home-intro">
                    선수 기록을 탐색하거나, 실제 Sleeper 로스터로
                    트레이드 가치를 분석하세요.
                </p>


                <div className="home-feature-grid">

                    <Link className="home-feature-card" to="/players">

                        <span className="home-feature-index">01</span>

                        <h2>Player Search</h2>

                        <p>
                            NFL 선수를 검색하고 시즌 기록과 분석 데이터를 확인합니다.
                        </p>

                        <span className="home-feature-link">
                            선수 탐색하기 →
                        </span>

                    </Link>


                    <Link
                        className="home-feature-card home-feature-card-accent"
                        to="/trade"
                    >

                        <span className="home-feature-index">02</span>

                        <h2>Trade Analysis</h2>

                        <p>
                            Sleeper League를 연결하고 실제 로스터를 기반으로 트레이드를 분석합니다.
                        </p>

                        <span className="home-feature-link">
                            트레이드 분석하기 →
                        </span>

                    </Link>

                </div>

            </main>

        </div>

    );
}


export default HomePage;
