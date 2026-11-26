package com.example.data

import com.example.model.FinalTeamSquad
import com.example.model.FixtureMatchStatus
import com.example.model.PlayerSlot
import com.example.model.PointsTableRow
import com.example.model.SeriesBundle
import com.example.model.SeriesFixtureItem
import com.example.model.SeriesLeague
import com.example.model.SeriesNewsItem
import com.example.model.SeriesPlayerStat
import com.example.model.SeriesSummaryMetrics
import com.example.model.SeriesVenueDetail

class SeriesRepository {

    fun getSeriesBundle(league: SeriesLeague): SeriesBundle = when (league) {
        SeriesLeague.ILT20_2026 -> ilt20Bundle
        SeriesLeague.WBBL_2026 -> wbblBundle
    }

    private val ilt20Bundle = SeriesBundle(
        league = SeriesLeague.ILT20_2026,
        statusBanner = "Matchday 5 • Gulf Giants lead 2–0 (+1.414) • Tonight: Match 6 DC vs DV (14:30 GMT)",
        summaryMetrics = SeriesSummaryMetrics(
            totalMatches = "34 (30 League + 4 Playoffs)",
            avgFirstInningsScore = "144.0 (S5 Dubai) • 158 Hist",
            chasingWinPct = "63.0% (29/46 Dubai Hist)",
            paceVsSpinSplit = "Low Nov Dew (29–44% RH) favors Spin/Cutters"
        ),
        fixtures = listOf(
            SeriesFixtureItem(
                id = "ilt20_m1",
                matchNumber = "Match 1",
                dateTimeLabel = "Completed • Dubai",
                teamACode = "DV",
                teamAName = "Desert Vipers",
                teamBCode = "MIE",
                teamBName = "MI Emirates",
                venue = "Dubai International Cricket Stadium",
                status = FixtureMatchStatus.COMPLETED,
                scoreOrPreview = "MIE 174/8 • DV chased inside 20 ov (DV Win)"
            ),
            SeriesFixtureItem(
                id = "ilt20_m3",
                matchNumber = "Match 3",
                dateTimeLabel = "Completed • Dubai",
                teamACode = "ADKR",
                teamAName = "Abu Dhabi Knight Riders",
                teamBCode = "DC",
                teamBName = "Dubai Capitals",
                venue = "Dubai International Cricket Stadium",
                status = FixtureMatchStatus.COMPLETED,
                scoreOrPreview = "ADKR 167/6 def. DC 112 all out by 55 runs (Hassan 4-0-13-0)"
            ),
            SeriesFixtureItem(
                id = "ilt20_m4",
                matchNumber = "Match 4",
                dateTimeLabel = "Completed • Sharjah",
                teamACode = "SW",
                teamAName = "Sharjah Warriorz",
                teamBCode = "DV",
                teamBName = "Desert Vipers",
                venue = "Sharjah Cricket Stadium",
                status = FixtureMatchStatus.COMPLETED,
                scoreOrPreview = "DV def. SW • Babar Azam 86 (51), Shadab 3/18, Curran 4-0-21-2"
            ),
            SeriesFixtureItem(
                id = "ilt20_m5",
                matchNumber = "Match 5",
                dateTimeLabel = "Last Night • Dubai",
                teamACode = "MIE",
                teamAName = "MI Emirates",
                teamBCode = "GG",
                teamBName = "Gulf Giants",
                venue = "Dubai International Cricket Stadium",
                status = FixtureMatchStatus.COMPLETED,
                scoreOrPreview = "GG 92/4 chased MIE 91/10 by 6 wickets (GG 2–0, NRR +1.414)"
            ),
            SeriesFixtureItem(
                id = "ilt20_m6",
                matchNumber = "Match 6",
                dateTimeLabel = "Wed Nov 25 • 14:30 GMT",
                teamACode = "DC",
                teamAName = "Dubai Capitals",
                teamBCode = "DV",
                teamBName = "Desert Vipers",
                venue = "Dubai International Cricket Stadium",
                status = FixtureMatchStatus.UPCOMING,
                scoreOrPreview = "Toss 14:00 GMT • DV 54.6% Mkt vs 53.5% AI • DC 2.20 (+1.1% / +2.3% EV)",
                quantSummary = "Primary Pick: Fakhar 10 Runs Quicker @ 2.20 (+6.5% EV, 2.0u)",
                linkedFixtureId = "ilt20_m6_dc_dv"
            ),
            SeriesFixtureItem(
                id = "ilt20_m7",
                matchNumber = "Match 7",
                dateTimeLabel = "Thu Nov 26 • 14:30 GMT",
                teamACode = "GG",
                teamAName = "Gulf Giants",
                teamBCode = "SW",
                teamBName = "Sharjah Warriorz",
                venue = "Sharjah Cricket Stadium",
                status = FixtureMatchStatus.UPCOMING,
                scoreOrPreview = "GG unbeaten (2–0) • Small square boundaries favor Total Over 168.5",
                quantSummary = "Model: GG 59.5% vs Mkt 55.2% (+4.3% Edge • 2.0u)",
                linkedFixtureId = "ilt20_m7_gg_sw"
            ),
            SeriesFixtureItem(
                id = "ilt20_m8",
                matchNumber = "Match 8",
                dateTimeLabel = "Fri Nov 27 • 14:30 GMT",
                teamACode = "ADKR",
                teamAName = "Abu Dhabi Knight Riders",
                teamBCode = "MIE",
                teamBName = "MI Emirates",
                venue = "Sheikh Zayed Cricket Stadium, Abu Dhabi",
                status = FixtureMatchStatus.UPCOMING,
                scoreOrPreview = "MIE 0–2 rebound spot • True pace bounce favors MIE batting depth",
                quantSummary = "Model: MIE 53.0% vs Mkt 48.8% (+4.2% Edge • 1.5u)",
                linkedFixtureId = "ilt20_m8_adkr_mie"
            )
        ),
        pointsTable = listOf(
            PointsTableRow(1, "GG", "Gulf Giants", 2, 2, 0, 0, 4, "+1.414", "W-W", qualifierZone = true),
            PointsTableRow(2, "ADKR", "Abu Dhabi Knight Riders", 2, 1, 1, 0, 2, "+0.910", "W-L", qualifierZone = true),
            PointsTableRow(3, "DV", "Desert Vipers", 2, 1, 1, 0, 2, "-0.793", "L-W", qualifierZone = true),
            PointsTableRow(4, "SW", "Sharjah Warriorz", 1, 0, 1, 0, 0, "-0.880", "L", qualifierZone = true),
            PointsTableRow(5, "DC", "Dubai Capitals", 1, 0, 1, 0, 0, "-2.750", "L", qualifierZone = false),
            PointsTableRow(6, "MIE", "MI Emirates", 2, 0, 2, 0, 0, "-1.950", "L-L", qualifierZone = false)
        ),
        teams = listOf(
            FinalTeamSquad(
                teamCode = "DC",
                teamName = "Dubai Capitals",
                captain = "Sam Billings (c/wk)",
                coachOrTheme = "Spin & Cutter Choke • Impact-Sub Flexibility",
                rosterSummary = "12-Man Matchday Core",
                injuryOrDutyAlert = "Phil Salt on England duty in Australia; monitor Powell & Shanaka at 14:00 GMT toss.",
                players = listOf(
                    PlayerSlot("Faf du Plessis", "Batter", "Powerplay anchor"),
                    PlayerSlot("Max Holden", "Impact Sub", "43.7% top-bat rate", isImpactOrRotation = true),
                    PlayerSlot("Sam Billings", "c / wk", "Middle-order stabilizer"),
                    PlayerSlot("Marcus Stoinis", "All-Rounder", "47 in Match 3"),
                    PlayerSlot("Hassan Khan", "Spin AR", "3.25 econ • 69% wkts 7–15"),
                    PlayerSlot("Dushmantha Chameera", "Pace", "4-0-25-2 vs ADKR"),
                    PlayerSlot("Noor Ahmad", "Wrist Spin", "2/40 vs ADKR"),
                    PlayerSlot("Mustafizur Rahman", "Pace", "Cutters on two-paced deck", isImpactOrRotation = true)
                )
            ),
            FinalTeamSquad(
                teamCode = "DV",
                teamName = "Desert Vipers",
                captain = "Shadab Khan (c)",
                coachOrTheme = "Defending Champions • Top-Order Star Power + Varied Attack",
                rosterSummary = "12-Man Matchday Core",
                injuryOrDutyAlert = "Babar Azam arrived for Match 4 and immediately hit 86 (51).",
                players = listOf(
                    PlayerSlot("Fakhar Zaman", "Opener", "SR ~135 • Powerplay aggressor"),
                    PlayerSlot("Babar Azam", "Opener", "86 (51) in Match 4"),
                    PlayerSlot("Andries Gous", "wk", "Aggressive glove/bat option"),
                    PlayerSlot("Shimron Hetmyer", "Finisher", "Death overs left-hand power"),
                    PlayerSlot("Shadab Khan", "c / Spin AR", "3/18 vs Warriorz"),
                    PlayerSlot("Tom Curran", "Pace AR", "4-0-21-2 slower balls"),
                    PlayerSlot("Khuzaima Bin Tanveer", "UAE Pace", "3/31 swing option"),
                    PlayerSlot("Naseem Shah", "Pace", "New-ball strike bowler"),
                    PlayerSlot("Mujeeb Ur Rahman", "Spin", "PP & middle overs")
                )
            ),
            FinalTeamSquad(
                teamCode = "GG",
                teamName = "Gulf Giants",
                captain = "James Vince (c)",
                coachOrTheme = "2–0 Table Leaders • Clinical Bowling & Chase Execution",
                rosterSummary = "League Leaders (+1.414 NRR)",
                players = listOf(
                    PlayerSlot("James Vince", "c / Opener", "Consistent UAE anchor"),
                    PlayerSlot("Ibrahim Zadran", "Top Order", "Controlled powerplay rotation"),
                    PlayerSlot("Gerhard Erasmus", "Spin AR", "Middle-overs matchup weapon"),
                    PlayerSlot("Aayan Afzal Khan", "UAE Spin", "Economy squeeze"),
                    PlayerSlot("Mark Adair", "Seam AR", "New-ball swing + death hitting"),
                    PlayerSlot("Blessing Muzarabani", "Pace", "Hard-length bounce")
                )
            ),
            FinalTeamSquad(
                teamCode = "ADKR",
                teamName = "Abu Dhabi Knight Riders",
                captain = "Sunil Narine (c)",
                coachOrTheme = "Mystery Spin + Power All-Rounders",
                rosterSummary = "Posted 167/6 vs DC",
                players = listOf(
                    PlayerSlot("Sunil Narine", "c / Spin AR", "Opening pinch-hitter & 4 overs"),
                    PlayerSlot("Andre Russell", "Death AR", "Boundary surge Overs 15–20"),
                    PlayerSlot("Charith Asalanka", "Middle", "Spin-hitting anchor"),
                    PlayerSlot("Alishan Sharafu", "UAE", "Top-order run accumulation"),
                    PlayerSlot("Jason Holder", "Seam AR", "Bounce at Abu Dhabi"),
                    PlayerSlot("David Willey", "Left-Arm Pace", "Powerplay swing")
                )
            ),
            FinalTeamSquad(
                teamCode = "SW",
                teamName = "Sharjah Warriorz",
                captain = "Tim Southee (c)",
                coachOrTheme = "Boundary Hitting at Small Sharjah Dimensions",
                rosterSummary = "Powerplay Swing + High-Ceiling Batters",
                players = listOf(
                    PlayerSlot("Johnson Charles", "wk / Opener", "High-SR Sharjah Powerplay"),
                    PlayerSlot("Tom Kohler-Cadmore", "Top Order", "Six-hitting specialist"),
                    PlayerSlot("Harmeet Singh", "Spin AR", "Left-arm orthodox control"),
                    PlayerSlot("Tim Southee", "c / Pace", "New-ball swing"),
                    PlayerSlot("Adil Rashid", "Wrist Spin", "Middle-overs wicket taker"),
                    PlayerSlot("Muhammad Jawadullah", "UAE Pace", "Left-arm angle")
                )
            ),
            FinalTeamSquad(
                teamCode = "MIE",
                teamName = "MI Emirates",
                captain = "Nicholas Pooran (c)",
                coachOrTheme = "0–2 Slow Start • High Variance Batting Core",
                rosterSummary = "Bowled out 91 vs GG after 174/8 in M1",
                players = listOf(
                    PlayerSlot("Nicholas Pooran", "c / wk", "Elite spin destroyer"),
                    PlayerSlot("Muhammad Waseem", "UAE Opener", "UAE conditions specialist"),
                    PlayerSlot("Kieron Pollard", "Death", "Finisher & slower balls"),
                    PlayerSlot("Trent Boult", "Pace", "Powerplay left-arm swing"),
                    PlayerSlot("Fazalhaq Farooqi", "Pace", "Death yorkers"),
                    PlayerSlot("AM Ghazanfar", "Mystery Spin", "Middle-overs threat")
                )
            )
        ),
        news = listOf(
            SeriesNewsItem(
                id = "ilt20_n1",
                title = "Gulf Giants Storm to 2–0 After Skittling MI Emirates for 91",
                tag = "Matchday 5 Recap",
                timestamp = "Nov 25, 2026",
                summary = "Gulf Giants chased 92 in clinical fashion with 6 wickets in hand to push their NRR to +1.414; Betfair shortened GG outright to 4.00.",
                tradingImpact = "Two-paced November Dubai pitch averaging 144.0 in 1st innings forces lower pre-match totals leans."
            ),
            SeriesNewsItem(
                id = "ilt20_n2",
                title = "Babar Azam Joins Desert Vipers & Strikes 86 (51) vs Warriorz",
                tag = "Squad Boost",
                timestamp = "Nov 24, 2026",
                summary = "Babar anchored Desert Vipers alongside Fakhar Zaman while Shadab Khan (3/18) and Khuzaima (3/31) sealed DV's first win.",
                tradingImpact = "Market over-indexed on Babar's Sharjah knock, creating +6.5% EV on Fakhar 10 Runs Quicker at 2.20."
            ),
            SeriesNewsItem(
                id = "ilt20_n3",
                title = "Phil Salt Remains on England Duty; Monitor DC Toss Sheet at 14:00 GMT",
                tag = "Availability Alert",
                timestamp = "Nov 25, 2026",
                summary = "Dubai Capitals remain without Phil Salt; Rovman Powell and Dasun Shanaka were omitted in Match 3 while Max Holden opened via Impact Sub.",
                tradingImpact = "Wait for 14:00 GMT Starting XI confirmation before backing Max Holden DC Top Batter (4.33)."
            )
        ),
        venues = listOf(
            SeriesVenueDetail(
                name = "Dubai International Cricket Stadium",
                city = "Dubai, UAE",
                dimensionsNote = "Large square boundaries (72–76m) • Ring-of-Fire lighting",
                firstInningsAvg = "144.0 (S5) • 157–159 (2024–26)",
                secondInningsAvg = "146–147 (2024–26)",
                chaseWinRate = "63.0% (29 of 46 matches • Captains fielded 41/46)",
                pitchAndDewProfile = "November window yields two-paced bounce & grip for cutters/spin; low 29–44% humidity suppresses heavy late-evening dew."
            ),
            SeriesVenueDetail(
                name = "Sheikh Zayed Cricket Stadium",
                city = "Abu Dhabi, UAE",
                dimensionsNote = "Expansive outfield (70–75m) • True bounce early",
                firstInningsAvg = "163.5",
                secondInningsAvg = "152.0",
                chaseWinRate = "55.0%",
                pitchAndDewProfile = "Pace bowlers extract carry with the new ball before spinners gain turn in overs 7–15."
            ),
            SeriesVenueDetail(
                name = "Sharjah Cricket Stadium",
                city = "Sharjah, UAE",
                dimensionsNote = "Compact boundaries (58–64m) • High boundary %",
                firstInningsAvg = "171.2",
                secondInningsAvg = "164.8",
                chaseWinRate = "58.0%",
                pitchAndDewProfile = "Fast scoring Powerplays; slower cutters and leg-spinners still pull back totals if batters target straight."
            )
        ),
        battingLeaders = listOf(
            SeriesPlayerStat(1, "Babar Azam", "DV", "Batting", "86 runs (51b)", "SR 168.6 (M4)", "Anchor rotation creates Fakhar PP speed contrast"),
            SeriesPlayerStat(2, "Marcus Stoinis", "DC", "Batting", "47 runs", "M3 top DC anchor", "Primary DC death-overs run hinge"),
            SeriesPlayerStat(3, "Fakhar Zaman", "DV", "Batting", "SR ~135 T20", "Powerplay 4s/6s", "+6.5% EV to reach 10 runs before Babar")
        ),
        bowlingLeaders = listOf(
            SeriesPlayerStat(1, "Shadab Khan", "DV", "Bowling", "3/18", "Middle-over wrist spin", "Matches Hassan/Noor in Overs 7–15 choke"),
            SeriesPlayerStat(2, "Khuzaima Bin Tanveer", "DV", "Bowling", "3/31", "New-ball swing (UAE)", "Targets DC fragile top order (6/3 in M3)"),
            SeriesPlayerStat(3, "Hassan Khan", "DC", "Bowling", "4-0-13-0", "3.25 Econ • 69% mid wkts", "Elite middle-overs containment on Dubai surface")
        )
    )

    private val wbblBundle = SeriesBundle(
        league = SeriesLeague.WBBL_2026,
        statusBanner = "WBBL 2026 Regular Season • Australian Spring/Summer • Tap 'Live Sync' for Today's Official Scores & Standings",
        summaryMetrics = SeriesSummaryMetrics(
            totalMatches = "40 League + 3 Finals",
            avgFirstInningsScore = "146.5 Runs",
            chasingWinPct = "54.2% (Day/Night Dew Lift)",
            paceVsSpinSplit = "Off-spin & slower cutters dominate Junction/North Sydney"
        ),
        fixtures = listOf(
            SeriesFixtureItem(
                id = "wbbl_m_ss_bh",
                matchNumber = "WBBL Match",
                dateTimeLabel = "Upcoming • 08:10 GMT (7:10 PM AEDT)",
                teamACode = "SS",
                teamAName = "Sydney Sixers",
                teamBCode = "BH",
                teamBName = "Brisbane Heat",
                venue = "North Sydney Oval, Sydney",
                status = FixtureMatchStatus.UPCOMING,
                scoreOrPreview = "Short straight boundaries (54–58m) • High-scoring Powerplay bias (Par 158+)",
                quantSummary = "Model: Sixers 54.5% vs Mkt 50.8% (+3.7% EV • 1.5u)",
                linkedFixtureId = "wbbl_s12_ss_bh"
            ),
            SeriesFixtureItem(
                id = "wbbl_m_ms_ps",
                matchNumber = "WBBL Match",
                dateTimeLabel = "Upcoming • 04:00 GMT (3:00 PM AEDT)",
                teamACode = "MS",
                teamAName = "Melbourne Stars",
                teamBCode = "PS",
                teamBName = "Perth Scorchers",
                venue = "CitiPower Centre (Junction Oval), Melbourne",
                status = FixtureMatchStatus.UPCOMING,
                scoreOrPreview = "True Junction deck with afternoon sea breeze • Mooney/Devine top-order vs Sutherland",
                quantSummary = "Model: Scorchers 57.5% vs Mkt 53.5% (+4.0% EV • 1.5u)",
                linkedFixtureId = "wbbl_s12_ms_ps"
            ),
            SeriesFixtureItem(
                id = "wbbl_m_as_hh",
                matchNumber = "WBBL Match",
                dateTimeLabel = "Upcoming • 07:40 GMT (7:10 PM ACDT)",
                teamACode = "AS",
                teamAName = "Adelaide Strikers",
                teamBCode = "HH",
                teamBName = "Hobart Hurricanes",
                venue = "Karen Rolton Oval / Adelaide Oval",
                status = FixtureMatchStatus.UPCOMING,
                scoreOrPreview = "Strikers spin trio vs Hurricanes power-hitting middle order",
                quantSummary = "Model: Strikers 53.5% vs Mkt 49.5% (+4.0% EV • 1.5u)",
                linkedFixtureId = "wbbl_s12_as_hh"
            )
        ),
        pointsTable = listOf(
            PointsTableRow(1, "PS", "Perth Scorchers", 4, 3, 1, 0, 6, "+0.840", "W-W-L-W", qualifierZone = true),
            PointsTableRow(2, "SS", "Sydney Sixers", 4, 3, 1, 0, 6, "+0.620", "W-L-W-W", qualifierZone = true),
            PointsTableRow(3, "BH", "Brisbane Heat", 4, 2, 2, 0, 4, "+0.290", "L-W-W-L", qualifierZone = true),
            PointsTableRow(4, "AS", "Adelaide Strikers", 4, 2, 2, 0, 4, "+0.110", "W-L-L-W", qualifierZone = true),
            PointsTableRow(5, "MR", "Melbourne Renegades", 4, 2, 2, 0, 4, "-0.095", "W-L-W-L", qualifierZone = false),
            PointsTableRow(6, "HH", "Hobart Hurricanes", 4, 2, 2, 0, 4, "-0.240", "L-W-L-W", qualifierZone = false),
            PointsTableRow(7, "MS", "Melbourne Stars", 4, 1, 3, 0, 2, "-0.610", "L-L-W-L", qualifierZone = false),
            PointsTableRow(8, "ST", "Sydney Thunder", 4, 1, 3, 0, 2, "-0.890", "L-W-L-L", qualifierZone = false)
        ),
        teams = listOf(
            FinalTeamSquad(
                teamCode = "SS",
                teamName = "Sydney Sixers",
                captain = "Ellyse Perry (c)",
                coachOrTheme = "North Sydney Boundary Power + World-Class Spin",
                rosterSummary = "8-Franchise Core",
                players = listOf(
                    PlayerSlot("Ellyse Perry", "c / All-Rounder", "Powerplay & middle-overs anchor"),
                    PlayerSlot("Alyssa Healy", "wk / Opener", "Powerplay strike rate >135"),
                    PlayerSlot("Ashleigh Gardner", "Spin AR", "4-over off-spin + death power"),
                    PlayerSlot("Sophie Ecclestone", "Left-Arm Spin", "Elite economy Overs 7–15")
                )
            ),
            FinalTeamSquad(
                teamCode = "BH",
                teamName = "Brisbane Heat",
                captain = "Jess Jonassen (c)",
                coachOrTheme = "Left-Arm Spin Control & Top-Order Pace Aggression",
                rosterSummary = "8-Franchise Core",
                players = listOf(
                    PlayerSlot("Grace Harris", "Opener", "High boundary % at North Sydney"),
                    PlayerSlot("Georgia Redmayne", "wk / Top", "Rotate-strike anchor"),
                    PlayerSlot("Jess Jonassen", "c / Spin AR", "Powerplay & death overs"),
                    PlayerSlot("Charli Knott", "Spin AR", "Middle-overs matchup")
                )
            ),
            FinalTeamSquad(
                teamCode = "PS",
                teamName = "Perth Scorchers",
                captain = "Sophie Devine (c)",
                coachOrTheme = "Explosive Opening + WACA Pace/Bounce",
                rosterSummary = "8-Franchise Core",
                players = listOf(
                    PlayerSlot("Beth Mooney", "wk / Opener", "League-leading consistency"),
                    PlayerSlot("Sophie Devine", "c / All-Rounder", "Powerplay six-hitting"),
                    PlayerSlot("Alana King", "Leg Spin", "Middle-overs wicket cluster"),
                    PlayerSlot("Chloe Ainsworth", "Pace", "WACA seam movement")
                )
            ),
            FinalTeamSquad(
                teamCode = "MS",
                teamName = "Melbourne Stars",
                captain = "Annabel Sutherland (c)",
                coachOrTheme = "All-Rounder Depth & Junction Oval Pace",
                rosterSummary = "8-Franchise Core",
                players = listOf(
                    PlayerSlot("Meg Lanning", "Top Order", "Chase pacing specialist"),
                    PlayerSlot("Annabel Sutherland", "c / Pace AR", "4 overs + middle-order power"),
                    PlayerSlot("Marizanne Kapp", "Seam AR", "New-ball swing threat"),
                    PlayerSlot("Kim Garth", "Pace", "Powerplay economy")
                )
            ),
            FinalTeamSquad(
                teamCode = "AS",
                teamName = "Adelaide Strikers",
                captain = "Tahlia McGrath (c)",
                coachOrTheme = "Dual-Spin Choke & Middle-Order Resilience",
                rosterSummary = "8-Franchise Core",
                players = listOf(
                    PlayerSlot("Laura Wolvaardt", "Opener", "Classic off-side tempo"),
                    PlayerSlot("Tahlia McGrath", "c / Seam AR", "Middle-over acceleration"),
                    PlayerSlot("Megan Schutt", "Pace", "Powerplay swing & death cutters"),
                    PlayerSlot("Amanda-Jade Wellington", "Leg Spin", "Wicket-taking middle squeeze")
                )
            ),
            FinalTeamSquad(
                teamCode = "HH",
                teamName = "Hobart Hurricanes",
                captain = "Elyse Villani (c)",
                coachOrTheme = "Power-Hitting Top Order & Bellerive Seam",
                rosterSummary = "8-Franchise Core",
                players = listOf(
                    PlayerSlot("Lizelle Lee", "wk / Opener", "Explosive Powerplay boundaries"),
                    PlayerSlot("Danielle Wyatt-Hodge", "Opener", "Fast starts vs new ball"),
                    PlayerSlot("Nicola Carey", "Seam AR", "Bellerive Oval seam utility"),
                    PlayerSlot("Molly Strano", "Off Spin", "Powerplay & death overs")
                )
            ),
            FinalTeamSquad(
                teamCode = "MR",
                teamName = "Melbourne Renegades",
                captain = "Sophie Molineux (c)",
                coachOrTheme = "Left-Arm Spin Trio + Aggressive Finishers",
                rosterSummary = "8-Franchise Core",
                players = listOf(
                    PlayerSlot("Hayley Matthews", "Spin AR", "Powerplay hitting + 4 overs"),
                    PlayerSlot("Sophie Molineux", "c / Spin AR", "Tight middle-over economy"),
                    PlayerSlot("Georgia Wareham", "Leg Spin", "Death-bowling variations"),
                    PlayerSlot("Deandra Dottin", "Power AR", "Boundary clearing Overs 15–20")
                )
            ),
            FinalTeamSquad(
                teamCode = "ST",
                teamName = "Sydney Thunder",
                captain = "Phoebe Litchfield (c)",
                coachOrTheme = "Youthful Strokeplay & Drummoyne Oval Spin",
                rosterSummary = "8-Franchise Core",
                players = listOf(
                    PlayerSlot("Phoebe Litchfield", "c / Top", "360-degree middle-over range"),
                    PlayerSlot("Chamari Athapaththu", "Opener / Spin", "Powerplay dominant left-hander"),
                    PlayerSlot("Heather Knight", "Middle", "Chase anchor & spin sweep"),
                    PlayerSlot("Hannah Darlington", "Pace", "Change-of-pace death overs")
                )
            )
        ),
        news = listOf(
            SeriesNewsItem(
                id = "wbbl_n1",
                title = "North Sydney Oval & Junction Oval Continue High-Scoring Over Bias",
                tag = "Venue Telemetry",
                timestamp = "Nov 25, 2026",
                summary = "Short straight hits at North Sydney Oval drive Powerplay totals above 46+ runs, while Junction Oval favors true bounce and seam-up pace.",
                tradingImpact = "Target Over Totals at North Sydney; monitor afternoon wind direction at CitiPower Centre. Tap 'Live Sync' for today's official WBBL 2026 scores."
            ),
            SeriesNewsItem(
                id = "wbbl_n2",
                title = "Spinners Dominate Overs 7–15 Across Australian Venues",
                tag = "Tactical Trend",
                timestamp = "Nov 25, 2026",
                summary = "Leg-spinners and left-arm orthodox bowlers account for over 42% of middle-over dismissals when chasing teams push above 8.0 RPO.",
                tradingImpact = "Prioritize teams with 2+ frontline spinners (Sixers, Strikers, Renegades) on drying day decks."
            )
        ),
        venues = listOf(
            SeriesVenueDetail(
                name = "North Sydney Oval",
                city = "Sydney, NSW",
                dimensionsNote = "Short straight boundaries (54–58m) • Fast outfield",
                firstInningsAvg = "158.4",
                secondInningsAvg = "149.2",
                chaseWinRate = "52.0%",
                pitchAndDewProfile = "Highest boundary-per-ball venue in WBBL; flat deck rewards clean Powerplay strikers."
            ),
            SeriesVenueDetail(
                name = "CitiPower Centre (Junction Oval)",
                city = "Melbourne, VIC",
                dimensionsNote = "True bounce • Afternoon coastal crosswind",
                firstInningsAvg = "151.0",
                secondInningsAvg = "144.6",
                chaseWinRate = "55.5%",
                pitchAndDewProfile = "Good carry for tall seamers; batters can trust pace onto the bat through middle overs."
            ),
            SeriesVenueDetail(
                name = "WACA Ground",
                city = "Perth, WA",
                dimensionsNote = "Express pace & steep bounce • Fremantle Doctor breeze",
                firstInningsAvg = "148.2",
                secondInningsAvg = "141.0",
                chaseWinRate = "51.0%",
                pitchAndDewProfile = "Extra bounce rewards back-of-length quicks and horizontal-bat cutters."
            ),
            SeriesVenueDetail(
                name = "Adelaide Oval / Karen Rolton Oval",
                city = "Adelaide, SA",
                dimensionsNote = "Long straight / shorter square pockets",
                firstInningsAvg = "145.5",
                secondInningsAvg = "139.8",
                chaseWinRate = "57.0%",
                pitchAndDewProfile = "Evening conditions assist swing early before flattening under lights."
            )
        ),
        battingLeaders = listOf(
            SeriesPlayerStat(1, "Beth Mooney", "PS", "Batting", "48.5 Avg", "SR 131.4", "High floor makes Mooney top-batter anchor"),
            SeriesPlayerStat(2, "Alyssa Healy", "SS", "Batting", "SR 142.8", "Powerplay 4s leader", "North Sydney short boundaries amplify PP edge"),
            SeriesPlayerStat(3, "Laura Wolvaardt", "AS", "Batting", "43.2 Avg", "Low dot-ball %", "Stabilizes middle-over spin choke")
        ),
        bowlingLeaders = listOf(
            SeriesPlayerStat(1, "Alana King", "PS", "Bowling", "6.24 Econ", "Middle-over wrist spin", "Generates drift & turn on hard Perth/Melbourne decks"),
            SeriesPlayerStat(2, "Sophie Ecclestone", "SS", "Bowling", "5.85 Econ", "4-over lock", "Suppresses opposing death-over acceleration"),
            SeriesPlayerStat(3, "Megan Schutt", "AS", "Bowling", "6.40 Econ", "PP swing + cutters", "High-leverage powerplay wicket rate")
        )
    )
}
