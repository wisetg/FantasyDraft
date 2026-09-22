package com.taegun.fantasy.player;

import jakarta.persistence.*;


/**
 * 선수의 경기별 기록입니다.
 *
 * 공격 / Kicker / Team Defense에 필요한 Raw Stats를
 * 하나의 Entity에서 관리합니다.
 */
@Entity
@Table(
        name = "player_stats",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_player_stats_player_season_week",
                        columnNames = {
                                "player_id",
                                "season",
                                "week"
                        }
                )
        }
)
public class PlayerStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(
            name = "player_id",
            nullable = false
    )
    private Player player;


    private int season;

    private int week;


    /**
     * 기본 PPR 기준 점수입니다.
     *
     * 실제 Sleeper League 분석에서는
     * LeagueScoringService가 Raw Stats를 다시 계산합니다.
     */
    private double fantasyPoints;


    // =========================================================
    // Passing
    // =========================================================

    private int passingYards;

    private Integer passingTouchdowns;

    private Integer interceptions;

    private Integer passingFirstDowns;

    private Integer passingTwoPointConversions;

    private Integer fortyPlusYardPassCompletions;

    private Integer fortyPlusYardPassingTouchdowns;

    private Integer fiftyPlusYardPassingTouchdowns;


    // =========================================================
    // Rushing
    // =========================================================

    private int rushingYards;

    private Integer rushingTouchdowns;

    private Integer rushingFirstDowns;

    private Integer rushingTwoPointConversions;

    private Integer fortyPlusYardRushes;

    private Integer fortyPlusYardRushingTouchdowns;

    private Integer fiftyPlusYardRushingTouchdowns;


    // =========================================================
    // Receiving
    // =========================================================

    private Integer receptions;

    private int receivingYards;

    private Integer receivingTouchdowns;

    private Integer receivingFirstDowns;

    private Integer receivingTwoPointConversions;

    private Integer fortyPlusYardReceptions;

    private Integer fortyPlusYardReceivingTouchdowns;

    private Integer fiftyPlusYardReceivingTouchdowns;


    // =========================================================
    // Turnover
    // =========================================================

    private Integer fumblesLost;

    private Integer specialTeamsFumblesForced;

    private Integer specialTeamsFumbleRecoveries;

    private Integer playerSpecialTeamsTouchdowns;

    private Integer fumbleRecoveryTouchdowns;


    /**
     * 기존 Frontend 호환용 전체 TD 수
     */
    private int touchdowns;


    // =========================================================
    // Kicking
    // =========================================================

    private Integer fieldGoalsMade;

    private Integer fieldGoalsMissed;


    private Integer fieldGoalsMade0To19;

    private Integer fieldGoalsMade20To29;

    private Integer fieldGoalsMade30To39;

    private Integer fieldGoalsMade40To49;

    private Integer fieldGoalsMade50To59;

    private Integer fieldGoalsMade60Plus;


    private Integer fieldGoalsMissed0To19;

    private Integer fieldGoalsMissed20To29;

    private Integer fieldGoalsMissed30To39;

    private Integer fieldGoalsMissed40To49;

    private Integer fieldGoalsMissed50To59;

    private Integer fieldGoalsMissed60Plus;


    private Integer extraPointsMade;

    private Integer extraPointsMissed;


    // =========================================================
    // Team Defense
    // =========================================================

    /**
     * Half Sack이 존재할 수 있으므로
     * double을 사용합니다.
     */
    private Double defenseSacks;


    private Integer defenseInterceptions;

    private Integer defenseFumblesForced;

    private Integer defenseFumbleRecoveries;

    private Integer defenseTouchdowns;

    private Integer defenseSafeties;

    private Integer defenseBlockedKicks;

    private Integer specialTeamsTouchdowns;

    private Integer defenseSpecialTeamsFumblesForced;

    private Integer defenseSpecialTeamsFumbleRecoveries;


    /**
     * Sleeper 방식으로 계산한 Points Allowed입니다.
     *
     * null:
     * 아직 Points Allowed 동기화를 하지 않음
     *
     * 0:
     * 실제 Shutout
     */
    private Integer defensePointsAllowed;


    // =========================================================
    // Constructor
    // =========================================================

    public PlayerStats() {
    }


    /**
     * 기존 코드 호환용 생성자
     */
    public PlayerStats(
            Player player,
            int season,
            int week,
            double fantasyPoints,
            int passingYards,
            int rushingYards,
            int receivingYards,
            int touchdowns
    ) {

        this.player = player;
        this.season = season;
        this.week = week;
        this.fantasyPoints = fantasyPoints;

        this.passingYards = passingYards;
        this.rushingYards = rushingYards;
        this.receivingYards = receivingYards;

        this.touchdowns = touchdowns;
    }


    /**
     * 공격 선수 Raw Stats 생성자
     */
    public PlayerStats(
            Player player,
            int season,
            int week,
            double fantasyPoints,
            int passingYards,
            int passingTouchdowns,
            int interceptions,
            int rushingYards,
            int rushingTouchdowns,
            int receptions,
            int receivingYards,
            int receivingTouchdowns,
            int fumblesLost
    ) {

        this.player = player;
        this.season = season;
        this.week = week;
        this.fantasyPoints = fantasyPoints;

        this.passingYards = passingYards;
        this.passingTouchdowns = passingTouchdowns;
        this.interceptions = interceptions;

        this.rushingYards = rushingYards;
        this.rushingTouchdowns = rushingTouchdowns;

        this.receptions = receptions;
        this.receivingYards = receivingYards;
        this.receivingTouchdowns = receivingTouchdowns;

        this.fumblesLost = fumblesLost;

        this.touchdowns =
                passingTouchdowns
                        + rushingTouchdowns
                        + receivingTouchdowns;
    }


    // =========================================================
    // Update
    // =========================================================

    public void updateStats(
            double fantasyPoints,
            int passingYards,
            int passingTouchdowns,
            int interceptions,
            int rushingYards,
            int rushingTouchdowns,
            int receptions,
            int receivingYards,
            int receivingTouchdowns,
            int fumblesLost
    ) {

        this.fantasyPoints = fantasyPoints;

        this.passingYards = passingYards;
        this.passingTouchdowns = passingTouchdowns;
        this.interceptions = interceptions;

        this.rushingYards = rushingYards;
        this.rushingTouchdowns = rushingTouchdowns;

        this.receptions = receptions;
        this.receivingYards = receivingYards;
        this.receivingTouchdowns = receivingTouchdowns;

        this.fumblesLost = fumblesLost;

        this.touchdowns =
                passingTouchdowns
                        + rushingTouchdowns
                        + receivingTouchdowns;
    }


    public void updateAdvancedStats(
            int passingFirstDowns,
            int passingTwoPointConversions,
            int rushingFirstDowns,
            int rushingTwoPointConversions,
            int receivingFirstDowns,
            int receivingTwoPointConversions
    ) {

        this.passingFirstDowns = passingFirstDowns;
        this.passingTwoPointConversions = passingTwoPointConversions;

        this.rushingFirstDowns = rushingFirstDowns;
        this.rushingTwoPointConversions = rushingTwoPointConversions;

        this.receivingFirstDowns = receivingFirstDowns;
        this.receivingTwoPointConversions = receivingTwoPointConversions;
    }


    public void updateFortyPlusYardPassCompletions(
            int fortyPlusYardPassCompletions
    ) {

        this.fortyPlusYardPassCompletions =
                Math.max(
                        fortyPlusYardPassCompletions,
                        0
                );
    }


    public void updateFortyPlusYardPassingTouchdowns(
            int fortyPlusYardPassingTouchdowns
    ) {

        this.fortyPlusYardPassingTouchdowns =
                Math.max(
                        fortyPlusYardPassingTouchdowns,
                        0
                );
    }


    public void updateAdditionalOffensiveBigPlays(
            int fiftyPlusYardPassingTouchdowns,
            int fortyPlusYardReceptions,
            int fortyPlusYardReceivingTouchdowns,
            int fiftyPlusYardReceivingTouchdowns,
            int fortyPlusYardRushes,
            int fortyPlusYardRushingTouchdowns,
            int fiftyPlusYardRushingTouchdowns
    ) {

        this.fiftyPlusYardPassingTouchdowns = Math.max(fiftyPlusYardPassingTouchdowns, 0);
        this.fortyPlusYardReceptions = Math.max(fortyPlusYardReceptions, 0);
        this.fortyPlusYardReceivingTouchdowns = Math.max(fortyPlusYardReceivingTouchdowns, 0);
        this.fiftyPlusYardReceivingTouchdowns = Math.max(fiftyPlusYardReceivingTouchdowns, 0);
        this.fortyPlusYardRushes = Math.max(fortyPlusYardRushes, 0);
        this.fortyPlusYardRushingTouchdowns = Math.max(fortyPlusYardRushingTouchdowns, 0);
        this.fiftyPlusYardRushingTouchdowns = Math.max(fiftyPlusYardRushingTouchdowns, 0);
    }


    public void updateIndividualSpecialTeamsStats(
            int specialTeamsFumblesForced,
            int specialTeamsFumbleRecoveries,
            int playerSpecialTeamsTouchdowns,
            int fumbleRecoveryTouchdowns
    ) {
        this.specialTeamsFumblesForced = Math.max(specialTeamsFumblesForced, 0);
        this.specialTeamsFumbleRecoveries = Math.max(specialTeamsFumbleRecoveries, 0);
        this.playerSpecialTeamsTouchdowns = Math.max(playerSpecialTeamsTouchdowns, 0);
        this.fumbleRecoveryTouchdowns = Math.max(fumbleRecoveryTouchdowns, 0);
    }


    public void updateDefenseSpecialTeamsFumbles(
            int defenseSpecialTeamsFumblesForced,
            int defenseSpecialTeamsFumbleRecoveries
    ) {
        this.defenseSpecialTeamsFumblesForced = Math.max(defenseSpecialTeamsFumblesForced, 0);
        this.defenseSpecialTeamsFumbleRecoveries = Math.max(defenseSpecialTeamsFumbleRecoveries, 0);
    }


    public void updateKickingStats(
            int fieldGoalsMade,
            int fieldGoalsMissed,
            int fieldGoalsMade0To19,
            int fieldGoalsMade20To29,
            int fieldGoalsMade30To39,
            int fieldGoalsMade40To49,
            int fieldGoalsMade50To59,
            int fieldGoalsMade60Plus,
            int fieldGoalsMissed0To19,
            int fieldGoalsMissed20To29,
            int fieldGoalsMissed30To39,
            int fieldGoalsMissed40To49,
            int fieldGoalsMissed50To59,
            int fieldGoalsMissed60Plus,
            int extraPointsMade,
            int extraPointsMissed
    ) {

        this.fieldGoalsMade = fieldGoalsMade;
        this.fieldGoalsMissed = fieldGoalsMissed;

        this.fieldGoalsMade0To19 = fieldGoalsMade0To19;
        this.fieldGoalsMade20To29 = fieldGoalsMade20To29;
        this.fieldGoalsMade30To39 = fieldGoalsMade30To39;
        this.fieldGoalsMade40To49 = fieldGoalsMade40To49;
        this.fieldGoalsMade50To59 = fieldGoalsMade50To59;
        this.fieldGoalsMade60Plus = fieldGoalsMade60Plus;

        this.fieldGoalsMissed0To19 = fieldGoalsMissed0To19;
        this.fieldGoalsMissed20To29 = fieldGoalsMissed20To29;
        this.fieldGoalsMissed30To39 = fieldGoalsMissed30To39;
        this.fieldGoalsMissed40To49 = fieldGoalsMissed40To49;
        this.fieldGoalsMissed50To59 = fieldGoalsMissed50To59;
        this.fieldGoalsMissed60Plus = fieldGoalsMissed60Plus;

        this.extraPointsMade = extraPointsMade;
        this.extraPointsMissed = extraPointsMissed;
    }


    public void updateDefenseStats(
            double defenseSacks,
            int defenseInterceptions,
            int defenseFumblesForced,
            int defenseFumbleRecoveries,
            int defenseTouchdowns,
            int defenseSafeties,
            int defenseBlockedKicks,
            int specialTeamsTouchdowns
    ) {

        this.defenseSacks = defenseSacks;
        this.defenseInterceptions = defenseInterceptions;
        this.defenseFumblesForced = defenseFumblesForced;
        this.defenseFumbleRecoveries = defenseFumbleRecoveries;
        this.defenseTouchdowns = defenseTouchdowns;
        this.defenseSafeties = defenseSafeties;
        this.defenseBlockedKicks = defenseBlockedKicks;
        this.specialTeamsTouchdowns = specialTeamsTouchdowns;
    }


    /**
     * Sleeper 방식 Points Allowed 저장
     */
    public void updateDefensePointsAllowed(
            int defensePointsAllowed
    ) {

        this.defensePointsAllowed =
                Math.max(
                        defensePointsAllowed,
                        0
                );
    }


    // =========================================================
    // Getter - Common
    // =========================================================

    public Long getId() {
        return id;
    }


    public Player getPlayer() {
        return player;
    }


    public int getSeason() {
        return season;
    }


    public int getWeek() {
        return week;
    }


    public double getFantasyPoints() {
        return fantasyPoints;
    }


    // =========================================================
    // Getter - Passing
    // =========================================================

    public int getPassingYards() {
        return passingYards;
    }


    public int getPassingTouchdowns() {
        return passingTouchdowns == null
                ? 0
                : passingTouchdowns;
    }


    public int getInterceptions() {
        return interceptions == null
                ? 0
                : interceptions;
    }


    public int getPassingFirstDowns() {
        return passingFirstDowns == null
                ? 0
                : passingFirstDowns;
    }


    public int getPassingTwoPointConversions() {
        return passingTwoPointConversions == null
                ? 0
                : passingTwoPointConversions;
    }


    public int getFortyPlusYardPassCompletions() {
        return fortyPlusYardPassCompletions == null
                ? 0
                : fortyPlusYardPassCompletions;
    }


    public int getFortyPlusYardPassingTouchdowns() {
        return fortyPlusYardPassingTouchdowns == null
                ? 0
                : fortyPlusYardPassingTouchdowns;
    }


    public int getFiftyPlusYardPassingTouchdowns() {
        return fiftyPlusYardPassingTouchdowns == null ? 0 : fiftyPlusYardPassingTouchdowns;
    }


    // =========================================================
    // Getter - Rushing
    // =========================================================

    public int getRushingYards() {
        return rushingYards;
    }


    public int getRushingTouchdowns() {
        return rushingTouchdowns == null
                ? 0
                : rushingTouchdowns;
    }


    public int getRushingFirstDowns() {
        return rushingFirstDowns == null
                ? 0
                : rushingFirstDowns;
    }


    public int getRushingTwoPointConversions() {
        return rushingTwoPointConversions == null
                ? 0
                : rushingTwoPointConversions;
    }


    public int getFortyPlusYardRushes() {
        return fortyPlusYardRushes == null ? 0 : fortyPlusYardRushes;
    }


    public int getFortyPlusYardRushingTouchdowns() {
        return fortyPlusYardRushingTouchdowns == null ? 0 : fortyPlusYardRushingTouchdowns;
    }


    public int getFiftyPlusYardRushingTouchdowns() {
        return fiftyPlusYardRushingTouchdowns == null ? 0 : fiftyPlusYardRushingTouchdowns;
    }


    // =========================================================
    // Getter - Receiving
    // =========================================================

    public int getReceptions() {
        return receptions == null
                ? 0
                : receptions;
    }


    public int getReceivingYards() {
        return receivingYards;
    }


    public int getReceivingTouchdowns() {
        return receivingTouchdowns == null
                ? 0
                : receivingTouchdowns;
    }


    public int getReceivingFirstDowns() {
        return receivingFirstDowns == null
                ? 0
                : receivingFirstDowns;
    }


    public int getReceivingTwoPointConversions() {
        return receivingTwoPointConversions == null
                ? 0
                : receivingTwoPointConversions;
    }


    public int getFortyPlusYardReceptions() {
        return fortyPlusYardReceptions == null ? 0 : fortyPlusYardReceptions;
    }


    public int getFortyPlusYardReceivingTouchdowns() {
        return fortyPlusYardReceivingTouchdowns == null ? 0 : fortyPlusYardReceivingTouchdowns;
    }


    public int getFiftyPlusYardReceivingTouchdowns() {
        return fiftyPlusYardReceivingTouchdowns == null ? 0 : fiftyPlusYardReceivingTouchdowns;
    }


    // =========================================================
    // Getter - Turnover
    // =========================================================

    public int getFumblesLost() {
        return fumblesLost == null
                ? 0
                : fumblesLost;
    }


    public int getSpecialTeamsFumblesForced() {
        return specialTeamsFumblesForced == null ? 0 : specialTeamsFumblesForced;
    }


    public int getSpecialTeamsFumbleRecoveries() {
        return specialTeamsFumbleRecoveries == null ? 0 : specialTeamsFumbleRecoveries;
    }


    public int getPlayerSpecialTeamsTouchdowns() {
        return playerSpecialTeamsTouchdowns == null ? 0 : playerSpecialTeamsTouchdowns;
    }


    public int getFumbleRecoveryTouchdowns() {
        return fumbleRecoveryTouchdowns == null ? 0 : fumbleRecoveryTouchdowns;
    }


    public int getTouchdowns() {
        return touchdowns;
    }


    // =========================================================
    // Getter - Kicker
    // =========================================================

    public int getFieldGoalsMade() {
        return fieldGoalsMade == null
                ? 0
                : fieldGoalsMade;
    }


    public int getFieldGoalsMissed() {
        return fieldGoalsMissed == null
                ? 0
                : fieldGoalsMissed;
    }


    public int getFieldGoalsMade0To19() {
        return fieldGoalsMade0To19 == null
                ? 0
                : fieldGoalsMade0To19;
    }


    public int getFieldGoalsMade20To29() {
        return fieldGoalsMade20To29 == null
                ? 0
                : fieldGoalsMade20To29;
    }


    public int getFieldGoalsMade30To39() {
        return fieldGoalsMade30To39 == null
                ? 0
                : fieldGoalsMade30To39;
    }


    public int getFieldGoalsMade40To49() {
        return fieldGoalsMade40To49 == null
                ? 0
                : fieldGoalsMade40To49;
    }


    public int getFieldGoalsMade50To59() {
        return fieldGoalsMade50To59 == null
                ? 0
                : fieldGoalsMade50To59;
    }


    public int getFieldGoalsMade60Plus() {
        return fieldGoalsMade60Plus == null
                ? 0
                : fieldGoalsMade60Plus;
    }


    public int getFieldGoalsMissed0To19() {
        return fieldGoalsMissed0To19 == null
                ? 0
                : fieldGoalsMissed0To19;
    }


    public int getFieldGoalsMissed20To29() {
        return fieldGoalsMissed20To29 == null
                ? 0
                : fieldGoalsMissed20To29;
    }


    public int getFieldGoalsMissed30To39() {
        return fieldGoalsMissed30To39 == null
                ? 0
                : fieldGoalsMissed30To39;
    }


    public int getFieldGoalsMissed40To49() {
        return fieldGoalsMissed40To49 == null
                ? 0
                : fieldGoalsMissed40To49;
    }


    public int getFieldGoalsMissed50To59() {
        return fieldGoalsMissed50To59 == null
                ? 0
                : fieldGoalsMissed50To59;
    }


    public int getFieldGoalsMissed60Plus() {
        return fieldGoalsMissed60Plus == null
                ? 0
                : fieldGoalsMissed60Plus;
    }


    public int getExtraPointsMade() {
        return extraPointsMade == null
                ? 0
                : extraPointsMade;
    }


    public int getExtraPointsMissed() {
        return extraPointsMissed == null
                ? 0
                : extraPointsMissed;
    }


    // =========================================================
    // Getter - Defense
    // =========================================================

    public double getDefenseSacks() {
        return defenseSacks == null
                ? 0.0
                : defenseSacks;
    }


    public int getDefenseInterceptions() {
        return defenseInterceptions == null
                ? 0
                : defenseInterceptions;
    }


    public int getDefenseFumblesForced() {
        return defenseFumblesForced == null
                ? 0
                : defenseFumblesForced;
    }


    public int getDefenseFumbleRecoveries() {
        return defenseFumbleRecoveries == null
                ? 0
                : defenseFumbleRecoveries;
    }


    public int getDefenseTouchdowns() {
        return defenseTouchdowns == null
                ? 0
                : defenseTouchdowns;
    }


    public int getDefenseSafeties() {
        return defenseSafeties == null
                ? 0
                : defenseSafeties;
    }


    public int getDefenseBlockedKicks() {
        return defenseBlockedKicks == null
                ? 0
                : defenseBlockedKicks;
    }


    public int getSpecialTeamsTouchdowns() {
        return specialTeamsTouchdowns == null
                ? 0
                : specialTeamsTouchdowns;
    }


    public int getDefenseSpecialTeamsFumblesForced() {
        return defenseSpecialTeamsFumblesForced == null ? 0 : defenseSpecialTeamsFumblesForced;
    }


    public int getDefenseSpecialTeamsFumbleRecoveries() {
        return defenseSpecialTeamsFumbleRecoveries == null ? 0 : defenseSpecialTeamsFumbleRecoveries;
    }


    /**
     * 아직 PA 동기화를 하지 않은 상태와
     * 실제 0점을 구분하기 위한 메서드
     */
    public boolean hasDefensePointsAllowed() {
        return defensePointsAllowed != null;
    }


    public int getDefensePointsAllowed() {
        return defensePointsAllowed == null
                ? 0
                : defensePointsAllowed;
    }


    // =========================================================
    // 기존 Setter
    // =========================================================

    public void setSeason(int season) {
        this.season = season;
    }


    public void setWeek(int week) {
        this.week = week;
    }


    public void setPassingYards(int passingYards) {
        this.passingYards = passingYards;
    }


    public void setPassingTouchdowns(Integer passingTouchdowns) {
        this.passingTouchdowns = passingTouchdowns;
    }


    public void setInterceptions(Integer interceptions) {
        this.interceptions = interceptions;
    }


    public void setRushingYards(int rushingYards) {
        this.rushingYards = rushingYards;
    }


    public void setRushingTouchdowns(Integer rushingTouchdowns) {
        this.rushingTouchdowns = rushingTouchdowns;
    }


    public void setReceptions(Integer receptions) {
        this.receptions = receptions;
    }


    public void setReceivingYards(int receivingYards) {
        this.receivingYards = receivingYards;
    }


    public void setReceivingTouchdowns(Integer receivingTouchdowns) {
        this.receivingTouchdowns = receivingTouchdowns;
    }


    public void setFumblesLost(Integer fumblesLost) {
        this.fumblesLost = fumblesLost;
    }
}
