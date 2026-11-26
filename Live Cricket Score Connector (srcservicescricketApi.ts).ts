import { apiCache } from '../utils/cache.js';

const CRIC_BASE_URL = 'https://api.cricapi.com/v1';

export interface CricketMatchSummary {
  id: string;
  name: string;
  matchType: string;
  status: string;
  venue: string;
  dateTimeGMT: string;
  teams: string[];
  score: Array<{ r: number; w: number; o: number; inning: string }>;
  matchStarted: boolean;
  matchEnded: boolean;
}

export async function fetchLiveCricketMatches(teamFilter?: string) {
  const apiKey = process.env.CRIC_API_KEY;
  if (!apiKey) throw new Error('CRIC_API_KEY is missing in environment variables.');

  const cacheKey = 'pitchedge:cricapi:currentMatches';
  let matches = apiCache.get<CricketMatchSummary[]>(cacheKey);

  if (!matches) {
    const res = await fetch(`${CRIC_BASE_URL}/currentMatches?apikey=${apiKey}&offset=0`);
    if (!res.ok) throw new Error(`CricAPI HTTP Error: ${res.status}`);
    const payload = await res.json();

    if (payload.status !== 'success') {
      throw new Error(`CricAPI Error: ${payload.reason || 'Failed to fetch matches'}`);
    }

    matches = (payload.data || []).map((m: any) => ({
      id: m.id,
      name: m.name,
      matchType: m.matchType,
      status: m.status,
      venue: m.venue,
      dateTimeGMT: m.dateTimeGMT,
      teams: m.teams || [],
      score: m.score || [],
      matchStarted: Boolean(m.matchStarted),
      matchEnded: Boolean(m.matchEnded),
    }));

    apiCache.set(cacheKey, matches, 30);
  }

  if (teamFilter) {
    const q = teamFilter.toLowerCase();
    matches = matches!.filter(
      (m) =>
        m.name.toLowerCase().includes(q) ||
        m.teams.some((t) => t.toLowerCase().includes(q))
    );
  }

  return {
    count: matches!.length,
    matches: matches!.slice(0, 20),
  };
}

export async function fetchCricketMatchScorecard(matchId: string) {
  const apiKey = process.env.CRIC_API_KEY;
  if (!apiKey) throw new Error('CRIC_API_KEY is missing in environment variables.');

  const cacheKey = `pitchedge:cricapi:match_info:${matchId}`;
  const cached = apiCache.get<any>(cacheKey);
  if (cached) return cached;

  const res = await fetch(
    `${CRIC_BASE_URL}/match_info?apikey=${apiKey}&id=${encodeURIComponent(matchId)}`
  );
  if (!res.ok) throw new Error(`CricAPI Match Info HTTP Error: ${res.status}`);
  const payload = await res.json();

  if (payload.status !== 'success' || !payload.data) {
    throw new Error(`CricAPI Match Info Error: ${payload.reason || 'Match not found'}`);
  }

  const d = payload.data;
  const normalized = {
    id: d.id,
    name: d.name,
    matchType: d.matchType,
    status: d.status,
    venue: d.venue,
    dateTimeGMT: d.dateTimeGMT,
    teams: d.teams,
    tossWinner: d.tossWinner || 'Pending',
    tossChoice: d.tossChoice || 'Pending',
    matchWinner: d.matchWinner || null,
    score: d.score || [],
  };

  apiCache.set(cacheKey, normalized, 25);
  return normalized;
}