package com.taegun.fantasy.service;

import com.taegun.fantasy.player.PlayerValue;
import com.taegun.fantasy.player.ScoringFormat;

import com.taegun.fantasy.trade.TradeRequest;
import com.taegun.fantasy.trade.TradeResult;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;


/**
 * Fantasy Trade 분석
 */
@Service
public class TradeSimulationService {

    private final PlayerValueService
            playerValueService;

    private final LeaguePlayerValueService
            leaguePlayerValueService;


    public TradeSimulationService(

            PlayerValueService playerValueService,

            LeaguePlayerValueService leaguePlayerValueService
    ) {

        this.playerValueService =
                playerValueService;

        this.leaguePlayerValueService =
                leaguePlayerValueService;
    }


    public TradeResult simulate(
            TradeRequest request
    ) {

        if (
                request.getTeamAPlayerIds() == null
                        ||
                        request.getTeamAPlayerIds().isEmpty()
                        ||
                        request.getTeamBPlayerIds() == null
                        ||
                        request.getTeamBPlayerIds().isEmpty()
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Both teams must contain at least one player."
            );
        }


        double teamAValue =
                0;

        double teamBValue =
                0;

        List<PlayerValue> teamAPlayerValues = new ArrayList<>();
        List<PlayerValue> teamBPlayerValues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();


        /*
         * =========================
         * Team A
         * =========================
         */

        for (
                Long playerId
                :
                request.getTeamAPlayerIds()
        ) {

            PlayerValue value =
                    calculateValue(

                            playerId,

                            request
                    );


            teamAValue +=
                    value.getValueScore();

            teamAPlayerValues.add(value);
            addNoDataWarning(warnings, value, request);
        }


        /*
         * =========================
         * Team B
         * =========================
         */

        for (
                Long playerId
                :
                request.getTeamBPlayerIds()
        ) {

            PlayerValue value =
                    calculateValue(

                            playerId,

                            request
                    );


            teamBValue +=
                    value.getValueScore();

            teamBPlayerValues.add(value);
            addNoDataWarning(warnings, value, request);
        }


        double difference =
                Math.abs(

                        teamAValue
                                -
                                teamBValue
                );


        double max =
                Math.max(

                        teamAValue,

                        teamBValue
                );


        double min =
                Math.min(

                        teamAValue,

                        teamBValue
                );


        double fairness =

                max == 0

                        ? 0

                        : (min / max)
                        * 100;


        String message;


        if (
                fairness >= 95
        ) {

            message =
                    "매우 균형 잡힌 트레이드입니다.";

        } else if (
                fairness >= 85
        ) {

            message =
                    "비교적 균형 잡힌 트레이드입니다.";

        } else if (
                fairness >= 70
        ) {

            message =
                    "한쪽이 다소 유리한 트레이드입니다.";

        } else {

            message =
                    "가치 차이가 큰 트레이드입니다.";
        }


        return new TradeResult(

                round(
                        teamAValue
                ),

                round(
                        teamBValue
                ),

                round(
                        difference
                ),

                round(
                        fairness
                ),

                message,
                teamAPlayerValues,
                teamBPlayerValues,
                warnings
        );
    }


    /**
     * Value 계산 방식 선택
     */
    private PlayerValue calculateValue(

            Long playerId,

            TradeRequest request
    ) {

        /*
         * 실제 League가 연결된 경우
         *
         * Sleeper scoring_settings 사용
         */
        if (
                request.getFantasyLeagueId()
                        != null
        ) {

            return leaguePlayerValueService
                    .calculatePlayerValue(

                            playerId,

                            request.getFantasyLeagueId()
                    );
        }


        /*
         * 아래는 기존 수동 모드
         */
        ScoringFormat scoringFormat =
                request.getScoringFormat();


        if (
                scoringFormat == null
        ) {

            scoringFormat =
                    ScoringFormat.PPR;
        }


        Integer season =
                request.getSeason();


        if (
                season == null
        ) {

            return playerValueService
                    .calculatePlayerValue(

                            playerId,

                            scoringFormat
                    );
        }


        return playerValueService
                .calculatePlayerValue(

                        playerId,

                        season,

                        scoringFormat
                );
    }


    private double round(
            double value
    ) {

        return Math.round(

                value * 100.0

        ) / 100.0;
    }


    private void addNoDataWarning(
            List<String> warnings,
            PlayerValue value,
            TradeRequest request
    ) {

        if (!"NO_DATA".equals(value.getTrend())) {
            return;
        }

        String season = request.getSeason() == null
                ? "선택한"
                : String.valueOf(request.getSeason());

        warnings.add(
                value.getPlayerName()
                        + " 선수는 "
                        + season
                        + " 시즌 기록이 없어 Trade Value가 제한적으로 계산되었습니다."
        );
    }
}
