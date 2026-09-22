import {
    Route,
    Routes,
} from "react-router-dom";

import PlayerListPage
    from "./pages/PlayerListPage";

import HomePage
    from "./pages/HomePage";

import PlayerDetailPage
    from "./pages/PlayerDetailPage";

import TradeSimulatorPage
    from "./pages/TradeSimulatorPage";


function App() {

    return (

        <Routes>

            <Route
                path="/"
                element={
                    <HomePage />
                }
            />


            <Route
                path="/players"
                element={
                    <PlayerListPage />
                }
            />


            <Route
                path="/players/:playerId"
                element={
                    <PlayerDetailPage />
                }
            />


            <Route
                path="/trade"
                element={
                    <TradeSimulatorPage />
                }
            />

        </Routes>

    );
}


export default App;
