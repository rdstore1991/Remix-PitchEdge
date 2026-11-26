import 'dotenv/config';
import express from 'express';
import cors from 'cors';
import { analyzeMatchWithPitchEdge } from './agent/pitchEdgeAnalyst.js';
import { fetchLiveCricketMatches } from './services/cricketApi.js';

const app = express();
app.use(cors());
app.use(express.json());

// Endpoint 1: Live & upcoming cricket matches feed for PitchEdge UI Sidebar
app.get('/api/matches', async (req, res) => {
  try {
    const team = typeof req.query.team === 'string' ? req.query.team : undefined;
    const data = await fetchLiveCricketMatches(team);
    res.json(data);
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
});

// Endpoint 2: Full PitchEdge Quantitative Match & Odds Analysis
app.post('/api/analyze', async (req, res) => {
  try {
    const { query } = req.body;
    if (!query || typeof query !== 'string') {
      return res
        .status(400)
        .json({ error: 'Provide a valid "query" string in the request body.' });
    }

    const analysis = await analyzeMatchWithPitchEdge(query);
    res.json({ app: 'PitchEdge', analysis });
  } catch (error: any) {
    console.error('[PitchEdge Analyze Error]:', error);
    res.status(500).json({ error: error.message });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`PitchEdge Sports Backend running on http://localhost:${PORT}`);
});