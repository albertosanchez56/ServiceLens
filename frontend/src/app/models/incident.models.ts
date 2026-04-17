export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface IncidentSummary {
  id: string;
  title: string;
  state: string;
  severity: string;
  startedAt: string;
  updatedAt: string;
  rootService: string | null;
  signalCount: number;
  eventCount: number;
}

export interface IncidentDetail {
  id: string;
  title: string;
  state: string;
  severity: string;
  startedAt: string;
  updatedAt: string;
  resolvedAt: string | null;
  rootService: string | null;
  summary: string | null;
  metadata: unknown;
  version: number;
}

export interface IncidentEvent {
  id: string;
  incidentId: string;
  type: string;
  occurredAt: string;
  actor: { userId: string; system: boolean };
  payload: unknown;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
  roles: string[];
}
