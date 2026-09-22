import { useNavigate } from "react-router-dom";

function PlayerCard({ player }) {
    const navigate = useNavigate();

    const handleClick = () => {
        navigate(`/players/${player.id}`);
    };

    return (
        <button
            type="button"
            className="player-card"
            onClick={handleClick}
        >
            <strong>{player.name}</strong>

            <span>
                {player.position} · {player.team}
            </span>

            <span className="player-card-arrow">→</span>
        </button>
    );
}

export default PlayerCard;
