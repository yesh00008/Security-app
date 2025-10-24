from typing import List
import numpy as np


def fedavg(weight_updates: List[List[float]], weights: List[int] | None = None) -> List[float]:
    if not weight_updates:
        return []
    arrs = [np.array(w) for w in weight_updates]
    if weights is None:
        weights = [1] * len(arrs)
    weights = np.array(weights, dtype=float)
    weights = weights / (weights.sum() or 1.0)
    stacked = np.stack(arrs)
    avg = (stacked * weights[:, None]).sum(axis=0)
    return avg.tolist()

