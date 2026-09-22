package com.taegun.fantasy.service;

import com.taegun.fantasy.league.FantasyLeague;
import com.taegun.fantasy.player.Player;
import com.taegun.fantasy.player.PlayerStats;

import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;


/**
 * Sleeper League의 실제 scoring_settings를 이용해
 * 경기 Fantasy Point를 계산합니다.
 */
@Service
public class LeagueScoringService {

    private final JsonMapper jsonMapper;


    public LeagueScoringService(
            JsonMapper jsonMapper
    ) {

        this.jsonMapper =
                jsonMapper;
    }


    public double calculateScore(
            FantasyLeague league,
            Player player,
            PlayerStats stats
    ) {

        JsonNode rules =
                readRules(
                        league
                );


        String position =
                player.getPosition();


        // =========================================================
        // Kicker
        // =========================================================

        if (
                position != null
                        &&
                        position.equalsIgnoreCase("K")
        ) {

            return calculateKickerScore(
                    rules,
                    stats
            );
        }


        // =========================================================
        // Team Defense
        // =========================================================

        if (
                position != null
                        &&
                        (
                                position.equalsIgnoreCase("DEF")
                                        ||
                                        position.equalsIgnoreCase("DST")
                        )
        ) {

            return calculateDefenseScore(
                    rules,
                    stats
            );
        }


        // =========================================================
        // QB / RB / WR / TE
        // =========================================================

        double score =
                0.0;


        // =========================================================
        // Passing
        // =========================================================

        score +=
                stats.getPassingYards()
                        *
                        getRule(
                                rules,
                                "pass_yd"
                        );


        score +=
                stats.getPassingTouchdowns()
                        *
                        getRule(
                                rules,
                                "pass_td"
                        );


        score +=
                stats.getFortyPlusYardPassingTouchdowns()
                        *
                        getRule(
                                rules,
                                "pass_td_40p"
                        );

        score += stats.getFiftyPlusYardPassingTouchdowns()
                * getRule(rules, "pass_td_50p");


        score +=
                stats.getInterceptions()
                        *
                        getRule(
                                rules,
                                "pass_int"
                        );


        score +=
                stats.getPassingFirstDowns()
                        *
                        getRule(
                                rules,
                                "pass_fd"
                        );


        score +=
                stats.getPassingTwoPointConversions()
                        *
                        getRule(
                                rules,
                                "pass_2pt"
                        );


        score +=
                stats.getFortyPlusYardPassCompletions()
                        *
                        getRule(
                                rules,
                                "pass_cmp_40p"
                        );


        if (
                stats.getPassingYards()
                        >= 300
        ) {

            score +=
                    getRule(
                            rules,
                            "bonus_pass_yd_300"
                    );
        }


        if (
                stats.getPassingYards()
                        >= 400
        ) {

            score +=
                    getRule(
                            rules,
                            "bonus_pass_yd_400"
                    );
        }


        // =========================================================
        // Rushing
        // =========================================================

        score +=
                stats.getRushingYards()
                        *
                        getRule(
                                rules,
                                "rush_yd"
                        );


        score +=
                stats.getRushingTouchdowns()
                        *
                        getRule(
                                rules,
                                "rush_td"
                        );

        score += stats.getFortyPlusYardRushes()
                * getRule(rules, "rush_40p");
        score += stats.getFortyPlusYardRushingTouchdowns()
                * getRule(rules, "rush_td_40p");
        score += stats.getFiftyPlusYardRushingTouchdowns()
                * getRule(rules, "rush_td_50p");


        score +=
                stats.getRushingFirstDowns()
                        *
                        getRule(
                                rules,
                                "rush_fd"
                        );


        score +=
                stats.getRushingTwoPointConversions()
                        *
                        getRule(
                                rules,
                                "rush_2pt"
                        );


        if (
                stats.getRushingYards()
                        >= 100
        ) {

            score +=
                    getRule(
                            rules,
                            "bonus_rush_yd_100"
                    );
        }


        if (
                stats.getRushingYards()
                        >= 200
        ) {

            score +=
                    getRule(
                            rules,
                            "bonus_rush_yd_200"
                    );
        }


        // =========================================================
        // Receiving
        // =========================================================

        score +=
                stats.getReceptions()
                        *
                        getRule(
                                rules,
                                "rec"
                        );


        score +=
                stats.getReceivingYards()
                        *
                        getRule(
                                rules,
                                "rec_yd"
                        );


        score +=
                stats.getReceivingTouchdowns()
                        *
                        getRule(
                                rules,
                                "rec_td"
                        );

        score += stats.getFortyPlusYardReceptions()
                * getRule(rules, "rec_40p");
        score += stats.getFortyPlusYardReceivingTouchdowns()
                * getRule(rules, "rec_td_40p");
        score += stats.getFiftyPlusYardReceivingTouchdowns()
                * getRule(rules, "rec_td_50p");


        score +=
                stats.getReceivingFirstDowns()
                        *
                        getRule(
                                rules,
                                "rec_fd"
                        );


        score +=
                stats.getReceivingTwoPointConversions()
                        *
                        getRule(
                                rules,
                                "rec_2pt"
                        );


        if (
                stats.getReceivingYards()
                        >= 100
        ) {

            score +=
                    getRule(
                            rules,
                            "bonus_rec_yd_100"
                    );
        }


        if (
                stats.getReceivingYards()
                        >= 200
        ) {

            score +=
                    getRule(
                            rules,
                            "bonus_rec_yd_200"
                    );
        }


        // =========================================================
        // Fumble
        // =========================================================

        score +=
                stats.getFumblesLost()
                        *
                        getRule(
                                rules,
                                "fum_lost"
                        );

        score += stats.getSpecialTeamsFumblesForced() * getRule(rules, "st_ff");
        score += stats.getSpecialTeamsFumbleRecoveries() * getRule(rules, "st_fum_rec");
        score += stats.getPlayerSpecialTeamsTouchdowns() * getRule(rules, "st_td");
        score += stats.getFumbleRecoveryTouchdowns() * getRule(rules, "fum_rec_td");


        // =========================================================
        // Position Reception Bonus
        // =========================================================

        if (
                position != null
        ) {

            switch (
                    position.toUpperCase()
            ) {

                case "RB" ->

                        score +=
                                stats.getReceptions()
                                        *
                                        getRule(
                                                rules,
                                                "bonus_rec_rb"
                                        );


                case "WR" ->

                        score +=
                                stats.getReceptions()
                                        *
                                        getRule(
                                                rules,
                                                "bonus_rec_wr"
                                        );


                case "TE" ->

                        score +=
                                stats.getReceptions()
                                        *
                                        getRule(
                                                rules,
                                                "bonus_rec_te"
                                        );
            }
        }


        return round(
                score
        );
    }


    // =============================================================
    // Kicker
    // =============================================================

    private double calculateKickerScore(
            JsonNode rules,
            PlayerStats stats
    ) {

        double score =
                0.0;


        // =========================================================
        // PAT
        // =========================================================

        score +=
                stats.getExtraPointsMade()
                        *
                        getRule(
                                rules,
                                "xpm"
                        );


        score +=
                stats.getExtraPointsMissed()
                        *
                        getRule(
                                rules,
                                "xpmiss"
                        );


        // =========================================================
        // 기본 FG
        // =========================================================

        score +=
                stats.getFieldGoalsMade()
                        *
                        getRule(
                                rules,
                                "fgm"
                        );


        score +=
                stats.getFieldGoalsMissed()
                        *
                        getRule(
                                rules,
                                "fgmiss"
                        );


        // =========================================================
        // FG Made
        // =========================================================

        score +=
                stats.getFieldGoalsMade0To19()
                        *
                        getRule(
                                rules,
                                "fgm_0_19"
                        );


        score +=
                stats.getFieldGoalsMade20To29()
                        *
                        getRule(
                                rules,
                                "fgm_20_29"
                        );


        score +=
                stats.getFieldGoalsMade30To39()
                        *
                        getRule(
                                rules,
                                "fgm_30_39"
                        );


        score +=
                stats.getFieldGoalsMade40To49()
                        *
                        getRule(
                                rules,
                                "fgm_40_49"
                        );


        score +=
                stats.getFieldGoalsMade50To59()
                        *
                        getRule(
                                rules,
                                "fgm_50_59"
                        );


        score +=
                stats.getFieldGoalsMade60Plus()
                        *
                        getRule(
                                rules,
                                "fgm_60p"
                        );


        /*
         * 50+ 설정
         */
        score +=
                (
                        stats.getFieldGoalsMade50To59()
                                +
                                stats.getFieldGoalsMade60Plus()
                )
                        *
                        getRule(
                                rules,
                                "fgm_50p"
                        );


        // =========================================================
        // FG Miss
        // =========================================================

        score +=
                stats.getFieldGoalsMissed0To19()
                        *
                        getRule(
                                rules,
                                "fgmiss_0_19"
                        );


        score +=
                stats.getFieldGoalsMissed20To29()
                        *
                        getRule(
                                rules,
                                "fgmiss_20_29"
                        );


        score +=
                stats.getFieldGoalsMissed30To39()
                        *
                        getRule(
                                rules,
                                "fgmiss_30_39"
                        );


        score +=
                stats.getFieldGoalsMissed40To49()
                        *
                        getRule(
                                rules,
                                "fgmiss_40_49"
                        );


        score +=
                stats.getFieldGoalsMissed50To59()
                        *
                        getRule(
                                rules,
                                "fgmiss_50_59"
                        );


        score +=
                stats.getFieldGoalsMissed60Plus()
                        *
                        getRule(
                                rules,
                                "fgmiss_60p"
                        );


        score +=
                (
                        stats.getFieldGoalsMissed50To59()
                                +
                                stats.getFieldGoalsMissed60Plus()
                )
                        *
                        getRule(
                                rules,
                                "fgmiss_50p"
                        );


        return round(
                score
        );
    }


    // =============================================================
    // Team Defense
    // =============================================================

    private double calculateDefenseScore(
            JsonNode rules,
            PlayerStats stats
    ) {

        double score =
                0.0;


        // =========================================================
        // Basic Defense
        // =========================================================

        score +=
                stats.getDefenseSacks()
                        *
                        getRule(
                                rules,
                                "sack"
                        );


        score +=
                stats.getDefenseInterceptions()
                        *
                        getRule(
                                rules,
                                "int"
                        );


        score +=
                stats.getDefenseFumblesForced()
                        *
                        getRule(
                                rules,
                                "ff"
                        );


        score +=
                stats.getDefenseFumbleRecoveries()
                        *
                        getRule(
                                rules,
                                "fum_rec"
                        );


        score +=
                stats.getDefenseSafeties()
                        *
                        getRule(
                                rules,
                                "safe"
                        );


        score +=
                stats.getDefenseBlockedKicks()
                        *
                        getRule(
                                rules,
                                "blk_kick"
                        );


        score +=
                stats.getDefenseTouchdowns()
                        *
                        getRule(
                                rules,
                                "def_td"
                        );


        score +=
                stats.getSpecialTeamsTouchdowns()
                        *
                        getRule(
                                rules,
                                "def_st_td"
                        );

        score += stats.getDefenseSpecialTeamsFumblesForced() * getRule(rules, "def_st_ff");
        score += stats.getDefenseSpecialTeamsFumbleRecoveries() * getRule(rules, "def_st_fum_rec");


        // =========================================================
        // Points Allowed
        // =========================================================

        /*
         * 아직 PA 데이터를 동기화하지 않은 DEF에
         * 실수로 Shutout 점수를 주지 않도록 합니다.
         */
        if (
                stats.hasDefensePointsAllowed()
        ) {

            int pointsAllowed =
                    stats.getDefensePointsAllowed();


            /*
             * Points Per Point Allowed가 설정된
             * 커스텀 리그도 지원합니다.
             */
            score +=
                    pointsAllowed
                            *
                            getRule(
                                    rules,
                                    "pts_allow"
                            );


            /*
             * Range Rule은 정확히 하나만 적용합니다.
             */
            if (
                    pointsAllowed == 0
            ) {

                score +=
                        getRule(
                                rules,
                                "pts_allow_0"
                        );

            } else if (
                    pointsAllowed <= 6
            ) {

                score +=
                        getRule(
                                rules,
                                "pts_allow_1_6"
                        );

            } else if (
                    pointsAllowed <= 13
            ) {

                score +=
                        getRule(
                                rules,
                                "pts_allow_7_13"
                        );

            } else if (
                    pointsAllowed <= 20
            ) {

                score +=
                        getRule(
                                rules,
                                "pts_allow_14_20"
                        );

            } else if (
                    pointsAllowed <= 27
            ) {

                score +=
                        getRule(
                                rules,
                                "pts_allow_21_27"
                        );

            } else if (
                    pointsAllowed <= 34
            ) {

                score +=
                        getRule(
                                rules,
                                "pts_allow_28_34"
                        );

            } else {

                score +=
                        getRule(
                                rules,
                                "pts_allow_35p"
                        );
            }
        }


        return round(
                score
        );
    }


    // =============================================================
    // JSON
    // =============================================================

    private JsonNode readRules(
            FantasyLeague league
    ) {

        String json =
                league
                        .getScoringSettingsJson();


        if (
                json == null
                        ||
                        json.isBlank()
        ) {

            throw new IllegalStateException(
                    "League scoring settings are empty"
            );
        }


        try {

            JsonNode rules =
                    jsonMapper
                            .readTree(
                                    json
                            );


            if (
                    rules == null
                            ||
                            !rules.isObject()
            ) {

                throw new IllegalStateException(
                        "Invalid league scoring settings"
                );
            }


            return rules;


        } catch (
                Exception e
        ) {

            throw new IllegalStateException(
                    "Unable to parse league scoring settings",
                    e
            );
        }
    }


    private double getRule(
            JsonNode rules,
            String key
    ) {

        JsonNode value =
                rules.get(
                        key
                );


        if (
                value == null
                        ||
                        value.isNull()
        ) {

            return 0.0;
        }


        try {

            return Double.parseDouble(
                    value.asString()
            );

        } catch (
                Exception e
        ) {

            return 0.0;
        }
    }


    private double round(
            double value
    ) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }
}
