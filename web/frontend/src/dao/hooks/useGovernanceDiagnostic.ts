/**
 * Re-export useGovernanceDiagnostic from lib
 */
export {
  useGovernanceDiagnostic,
  default,
  verifyGovernanceConnection,
  rpcCall,
} from "../lib/useGovernanceDiagnostic";

export type {
  DiagnosticLogEntry,
  RpcDiagnosticHealth,
  GovernorDiagnosticHealth,
  TimelockDiagnosticHealth,
  GovernanceDiagnosticOverallStatus,
  GovernanceDiagnosticReport,
  GovernanceDiagnosticResult,
  UseGovernanceDiagnosticOptions,
} from "../lib/useGovernanceDiagnostic";
