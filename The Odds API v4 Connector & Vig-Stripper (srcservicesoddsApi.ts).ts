import { apiCache } from '../utils/cache.js';

const ODDS_BASE_URL = 'https://api.the-odds-api.com/v4';

export interface ActiveSportKey {
  key: string;
  group: string;
  title: string;
  description: string;
  active: boolean;
}

export async function fetchCricketSportKeys() {
  const apiKey = process.env.ODDS_API_KEY;
  if (!apiKey) throw new Error('ODDS_API_KEY is missing in environment variables.');

  const cacheKey = 'pitchedge:oddsapi:cricket_sports';
  const cached = apiCache.get<ActiveSportKey[]>(cacheKey);
  if (cached) return { sports: cached };

  // /v4/sports does not count against usage quota
  const res = await fetch(`${ODDS_BASE_URL}/sports/?apiKey=${apiKey}`);
  if (!res.ok) throw new Error(`The Odds API Sports HTTP Error: ${res.status}`);

  const allSports: ActiveSportKey[] = await res.json();
  const cricketSports = allSports.filter(
    (s) => s.group.toLowerCase() === 'cricket' && s.active
  );

  apiCache.set(cacheKey, cricketSports, 3600);
  return { sports: cricketSports };
}

export async function fetchLiveMatchOdds(sportKey: string, teamQuery?: string) {
  const apiKey = process.env.ODDS_API_KEY;
  if (!apiKey) throw new Error('ODDS_API_KEY is missing in environment variables.');

  const cacheKey = `pitchedge:oddsapi:odds:${sportKey}`;
  let events = apiCache.get<any[]>(cacheKey);

  if (!events) {
    const url = `${ODDS_BASE_URL}/sports/${encodeURIComponent(
      sportKey
    )}/odds/?apiKey=${apiKey}&regions=uk,eu,au&markets=h2h&oddsFormat=decimal`;

    const res = await fetch(url);
    if (!res.ok) {
      const errText = await res.text();
      throw new Error(`The Odds API Error (${res.status}): ${errText}`);
    }

    const remainingCredits = res.headers.get('x-requests-remaining');
    console.log(`[PitchEdge - The Odds API] Credits remaining: ${remainingCredits}`);

    events = await res.json();
    apiCache.set(cacheKey, events, 30);
  }

  let filteredEvents = events!;
  if (teamQuery) {
    const q = teamQuery.toLowerCase();
    filteredEvents = filteredEvents.filter(
      (e) =>
        e.home_team.toLowerCase().includes(q) ||
        e.away_team.toLowerCase().includes(q)
    );
  }

  // Strip bookmaker vig (overround) and compute true market implied probabilities
  const analyzedEvents = filteredEvents.map((event) => {
    const bestBack: Record<string, { price: number; bookmaker: string }> = {};
    const exchangeLay: Record<string, { price: number; exchange: string }> = {};
    let bookmakerCount = 0;

    for (const bookie of event.bookmakers || []) {
      const h2hMarket = bookie.markets?.find((m: any) => m.key === 'h2h');
      const layMarket = bookie.markets?.find((m: any) => m.key === 'h2h_lay');

      if (h2hMarket?.outcomes) {
        bookmakerCount++;
        for (const outcome of h2hMarket.outcomes) {
          if (
            !bestBack[outcome.name] ||
            outcome.price > bestBack[outcome.name].price
          ) {
            bestBack[outcome.name] = {
              price: outcome.price,
              bookmaker: bookie.title,
            };
          }
        }
      }

      if (layMarket?.outcomes) {
        for (const outcome of layMarket.outcomes) {
          if (
            !exchangeLay[outcome.name] ||
            outcome.price < exchangeLay[outcome.name].price
          ) {
            exchangeLay[outcome.name] = {
              price: outcome.price,
              exchange: bookie.title,
            };
          }
        }
      }
    }

    const rawImpliedEntries = Object.entries(bestBack).map(([name, info]) => ({
      name,
      bestOdds: info.price,
      bookmaker: info.bookmaker,
      rawImpliedProb: 1 / info.price,
    }));

    const totalImpliedSum = rawImpliedEntries.reduce(
      (sum, item) => sum + item.rawImpliedProb,
      0
    );

    const overroundPercent =
      totalImpliedSum > 0 ? Number(((totalImpliedSum - 1) * 100).toFixed(2)) : 0;

    const marketProbabilities = rawImpliedEntries.map((item) => ({
      selection: item.name,
      bestBackOdds: item.bestOdds,
      bestBookmaker: item.bookmaker,
      exchangeLayOdds: exchangeLay[item.name]?.price ?? null,
      rawImpliedPercent: Number((item.rawImpliedProb * 100).toFixed(2)),
      trueVigFreeMarketPercent:
        totalImpliedSum > 0
          ? Number(((item.rawImpliedProb / totalImpliedSum) * 100).toFixed(2))
          : 0,
    }));

    return {
      eventId: event.id,
      sportKey: event.sport_key,
      sportTitle: event.sport_title,
      commenceTime: event.commence_time,
      homeTeam: event.home_team,
      awayTeam: event.away_team,
      marketOverroundPercent: overroundPercent,
      marketProbabilities,
      bookmakerCount,
    };
  });

  return {
    sportKey,
    eventsFound: analyzedEvents.length,
    events: analyzedEvents,
  };
}