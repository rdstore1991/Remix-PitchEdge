import {
  GoogleGenAI,
  Type,
  FunctionDeclaration,
  Content,
} from '@google/genai';
import { PITCH_EDGE_SYSTEM_PROMPT } from '../prompt.js';
import {
  fetchLiveCricketMatches,
  fetchCricketMatchScorecard,
} from '../services/cricketApi.js';
import {
  fetchCricketSportKeys,
  fetchLiveMatchOdds,
} from '../services/oddsApi.js';

const ai = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY });

const toolDeclarations: FunctionDeclaration[] = [
  {
    name: 'get_live_cricket_matches',
    description:
      'Fetches currently live and upcoming international and domestic cricket matches with match IDs, teams, venues, and summary scores.',
    parameters: {
      type: Type.OBJECT,
      properties: {
        teamFilter: {
          type: Type.STRING,
          description:
            'Optional team or tournament name to filter results (e.g., "India", "Australia", "Bangladesh", "Renegades", "IPL").',
        },
      },
    },
  },
  {
    name: 'get_cricket_match_scorecard',
    description:
      'Fetches detailed live telemetry for a specific cricket match ID, including toss winner, toss decision, venue, and innings-by-innings runs, wickets, and overs.',
    parameters: {
      type: Type.OBJECT,
      properties: {
        matchId: {
          type: Type.STRING,
          description: 'The unique match ID returned by get_live_cricket_matches.',
        },
      },
      required: ['matchId'],
    },
  },
  {
    name: 'get_cricket_sport_keys',
    description:
      'Retrieves the list of currently active cricket tournament sport keys from The Odds API (e.g., cricket_international_t20, cricket_odi, cricket_test_match, cricket_big_bash).',
    parameters: {
      type: Type.OBJECT,
      properties: {},
    },
  },
  {
    name: 'get_live_match_odds',
    description:
      'Harvests live decimal odds from UK, EU, and AU sportsbooks and exchanges for a given cricket sportKey, and calculates vig-free implied win probabilities.',
    parameters: {
      type: Type.OBJECT,
      properties: {
        sportKey: {
          type: Type.STRING,
          description:
            'Exact active sport key from get_cricket_sport_keys (e.g., "cricket_international_t20").',
        },
        teamQuery: {
          type: Type.STRING,
          description:
            'Optional team name to filter odds to a specific fixture.',
        },
      },
      required: ['sportKey'],
    },
  },
];

async function executeToolCall(
  name: string,
  args: Record<string, any>
): Promise<Record<string, any>> {
  try {
    switch (name) {
      case 'get_live_cricket_matches':
        return await fetchLiveCricketMatches(args.teamFilter);
      case 'get_cricket_match_scorecard':
        return await fetchCricketMatchScorecard(args.matchId);
      case 'get_cricket_sport_keys':
        return await fetchCricketSportKeys();
      case 'get_live_match_odds':
        return await fetchLiveMatchOdds(args.sportKey, args.teamQuery);
      default:
        return { error: `Unknown function: ${name}` };
    }
  } catch (err: any) {
    console.error(`[PitchEdge Tool Error] ${name}:`, err.message);
    return { error: err.message || 'Tool execution failed' };
  }
}

export async function analyzeMatchWithPitchEdge(userQuery: string): Promise<string> {
  const contents: Content[] = [
    {
      role: 'user',
      parts: [{ text: userQuery }],
    },
  ];

  const MAX_TOOL_TURNS = 6;

  for (let turn = 0; turn < MAX_TOOL_TURNS; turn++) {
    const response = await ai.models.generateContent({
      model: 'gemini-2.5-pro',
      contents,
      config: {
        systemInstruction: PITCH_EDGE_SYSTEM_PROMPT,
        temperature: 0.2,
        tools: [{ functionDeclarations: toolDeclarations }],
      },
    });

    const functionCalls = response.functionCalls;

    if (!functionCalls || functionCalls.length === 0) {
      return response.text || 'No PitchEdge analysis generated.';
    }

    const modelContent = response.candidates?.[0]?.content;
    if (modelContent) {
      contents.push(modelContent);
    }

    // Execute parallel tool calls requested by Gemini
    const functionResponseParts = await Promise.all(
      functionCalls.map(async (call) => {
        console.log(`[PitchEdge Tool Call] ${call.name}`, call.args);
        const result = await executeToolCall(
          call.name!,
          (call.args as Record<string, any>) || {}
        );

        return {
          functionResponse: {
            id: call.id,
            name: call.name!,
            response: result,
          },
        };
      })
    );

    contents.push({
      role: 'user',
      parts: functionResponseParts,
    });
  }

  throw new Error('PitchEdge exceeded maximum tool-calling iterations.');
}