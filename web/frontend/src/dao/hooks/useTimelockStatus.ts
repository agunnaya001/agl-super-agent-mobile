import { useState, useEffect, useCallback } from "react";
import { GOVERNANCE_CONTRACTS } from "../lib/contracts";

export interface TimelockOperation {
  id: string;
  target: string;
  value: string;
  eta: number;
  ready: boolean;
  done: boolean;
  pending: boolean;
  description: string;
}

export function useTimelockStatus() {
  const [minDelaySeconds, setMinDelaySeconds] = useState<number>(86400); // 24 hours standard
  const [operations, setOperations] = useState<TimelockOperation[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  const fetchStatus = useCallback(async () => {
    setLoading(true);
    try {
      setMinDelaySeconds(86400);
      setOperations([
        {
          id: "0x4a7e93f7b2c9082a6d5c5890ef99252329388147d32c0d8924b819f07a21350b",
          target: GOVERNANCE_CONTRACTS.credits,
          value: "0 ETH",
          eta: Math.floor(Date.now() / 1000) + 14400,
          ready: false,
          done: false,
          pending: true,
          description: "Update Developer AI Credit mint cap",
        },
        {
          id: "0xb799e0df39a3f2d21a2c3a502bc6fbf66ee477e6872583ecbcbc633ce098319f",
          target: GOVERNANCE_CONTRACTS.staking,
          value: "0 ETH",
          eta: Math.floor(Date.now() / 1000) - 3600,
          ready: true,
          done: false,
          pending: true,
          description: "Rebalance Staking Rewards pool tier",
        },
      ]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchStatus();
  }, [fetchStatus]);

  return {
    timelockAddress: GOVERNANCE_CONTRACTS.timelock,
    minDelaySeconds,
    operations,
    loading,
    refresh: fetchStatus,
  };
}
