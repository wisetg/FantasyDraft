package com.taegun.fantasy.service;

import com.taegun.fantasy.league.FantasyLeague;
import com.taegun.fantasy.league.FantasyLeagueRepository;
import com.taegun.fantasy.league.ScoringCompatibilityResponse;
import com.taegun.fantasy.league.ScoringRuleDetail;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 특정 Fantasy League의 scoring_settings를 분석해서
 * 현재 엔진이 지원하는 Rule을 확인합니다.
 */
@Service
public class LeagueScoringAuditService {

    private static final Set<String>
            SUPPORTED_RULES =
            Set.of(

                    // =================================================
                    // Passing
                    // =================================================

                    "pass_yd",
                    "pass_td",
                    "pass_td_40p",
                    "pass_td_50p",
                    "pass_int",
                    "pass_fd",
                    "pass_2pt",
                    "pass_cmp_40p",

                    "bonus_pass_yd_300",
                    "bonus_pass_yd_400",


                    // =================================================
                    // Rushing
                    // =================================================

                    "rush_yd",
                    "rush_td",
                    "rush_40p",
                    "rush_td_40p",
                    "rush_td_50p",
                    "rush_fd",
                    "rush_2pt",

                    "bonus_rush_yd_100",
                    "bonus_rush_yd_200",


                    // =================================================
                    // Receiving
                    // =================================================

                    "rec",
                    "rec_yd",
                    "rec_td",
                    "rec_40p",
                    "rec_td_40p",
                    "rec_td_50p",
                    "rec_fd",
                    "rec_2pt",

                    "bonus_rec_yd_100",
                    "bonus_rec_yd_200",

                    "bonus_rec_rb",
                    "bonus_rec_wr",
                    "bonus_rec_te",


                    // =================================================
                    // Turnover
                    // =================================================

                    "fum_lost",
                    "fum_rec_td",

                    "st_ff",
                    "st_fum_rec",
                    "st_td",


                    // =================================================
                    // Kicker
                    // =================================================

                    "xpm",
                    "xpmiss",

                    "fgm",
                    "fgmiss",

                    "fgm_0_19",
                    "fgm_20_29",
                    "fgm_30_39",
                    "fgm_40_49",
                    "fgm_50_59",
                    "fgm_60p",
                    "fgm_50p",

                    "fgmiss_0_19",
                    "fgmiss_20_29",
                    "fgmiss_30_39",
                    "fgmiss_40_49",
                    "fgmiss_50_59",
                    "fgmiss_60p",
                    "fgmiss_50p",


                    // =================================================
                    // Defense
                    // =================================================

                    "sack",
                    "int",
                    "ff",
                    "fum_rec",
                    "safe",
                    "blk_kick",

                    "def_td",
                    "def_st_td",
                    "def_st_ff",
                    "def_st_fum_rec",


                    // =================================================
                    // Defense - Points Allowed
                    // =================================================

                    "pts_allow",

                    "pts_allow_0",
                    "pts_allow_1_6",
                    "pts_allow_7_13",
                    "pts_allow_14_20",
                    "pts_allow_21_27",
                    "pts_allow_28_34",
                    "pts_allow_35p"
            );


    private final FantasyLeagueRepository
            fantasyLeagueRepository;

    private final JsonMapper
            jsonMapper;


    public LeagueScoringAuditService(
            FantasyLeagueRepository fantasyLeagueRepository,
            JsonMapper jsonMapper
    ) {

        this.fantasyLeagueRepository =
                fantasyLeagueRepository;

        this.jsonMapper =
                jsonMapper;
    }


    public ScoringCompatibilityResponse analyzeLeague(
            Long fantasyLeagueId
    ) {

        FantasyLeague league =
                fantasyLeagueRepository
                        .findById(
                                fantasyLeagueId
                        )
                        .orElseThrow(() ->

                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Fantasy League not found"
                                )
                        );


        String json =
                league
                        .getScoringSettingsJson();


        if (
                json == null
                        ||
                        json.isBlank()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "League scoring settings are empty"
            );
        }


        JsonNode root;


        try {

            root =
                    jsonMapper
                            .readTree(
                                    json
                            );


        } catch (
                Exception e
        ) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to parse League scoring settings"
            );
        }


        if (
                root == null
                        ||
                        !root.isObject()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Invalid League scoring settings"
            );
        }


        List<ScoringRuleDetail>
                supportedRules =
                new ArrayList<>();


        List<ScoringRuleDetail>
                unsupportedRules =
                new ArrayList<>();


        for (
                Map.Entry<String, JsonNode> entry
                :
                root.properties()
        ) {

            String key =
                    entry.getKey();


            double value =
                    parseDouble(
                            entry.getValue()
                    );


            /*
             * 0점 Rule은 실제 점수에 영향이 없으므로
             * Compatibility 검사에서 제외
             */
            if (
                    Math.abs(value)
                            < 0.000001
            ) {

                continue;
            }


            boolean supported =
                    SUPPORTED_RULES
                            .contains(
                                    key
                            );


            ScoringRuleDetail detail =
                    new ScoringRuleDetail(
                            key,
                            value,
                            supported
                    );


            if (
                    supported
            ) {

                supportedRules.add(
                        detail
                );

            } else {

                unsupportedRules.add(
                        detail
                );
            }
        }


        supportedRules.sort(
                Comparator.comparing(
                        ScoringRuleDetail::getKey
                )
        );


        unsupportedRules.sort(
                Comparator.comparing(
                        ScoringRuleDetail::getKey
                )
        );


        int supportedCount =
                supportedRules.size();


        int unsupportedCount =
                unsupportedRules.size();


        int activeCount =
                supportedCount
                        +
                        unsupportedCount;


        return new ScoringCompatibilityResponse(
                league.getId(),
                league.getName(),
                unsupportedCount == 0,
                activeCount,
                supportedCount,
                unsupportedCount,
                supportedRules,
                unsupportedRules
        );
    }


    private double parseDouble(
            JsonNode node
    ) {

        if (
                node == null
                        ||
                        node.isNull()
        ) {

            return 0.0;
        }


        try {

            return Double.parseDouble(
                    node.asString()
            );

        } catch (
                Exception e
        ) {

            return 0.0;
        }
    }
}
