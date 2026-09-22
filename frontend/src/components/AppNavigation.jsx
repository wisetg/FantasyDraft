import {
    Link,
    NavLink,
} from "react-router-dom";


function AppNavigation() {

    return (

        <nav className="app-navigation">

            <Link className="app-brand" to="/">
                FantasyDraft
            </Link>


            <div className="app-navigation-links">

                <NavLink to="/players">
                    Player Search
                </NavLink>

                <NavLink to="/trade">
                    Trade Analysis
                </NavLink>

            </div>

        </nav>

    );
}


export default AppNavigation;
