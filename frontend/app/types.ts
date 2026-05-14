export type ProviderRun = {
  id: number;
  provider: string;
  source: string;
  niche: string;
  region: string;
  query: string;
  status: string;
  startedAt: string;
  completedAt: string | null;
  durationMs: number | null;
  recordsFetched: number;
  opportunitiesGenerated: number;
  scoringVersion: string | null;
  errorMessage: string | null;
  createdAt: string;
};

export type ProviderRunPage = {
  runs: ProviderRun[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type SystemStatus = {
  backendStatus: string;
  dbConnectivity: string;
  latestProviderRun: ProviderRun | null;
  latestScoringRun: {
    id: number;
    providerRunId: number;
    status: string;
    scoringVersion: string;
    startedAt: string;
    completedAt: string | null;
    durationMs: number | null;
    opportunitiesScored: number;
    errorMessage: string | null;
  } | null;
  totalOpportunitiesStored: number;
  totalSourceRecordsStored: number;
  activeProviders: Array<{
    sourceType: string;
    available: boolean;
  }>;
  generatedAt: string;
};
