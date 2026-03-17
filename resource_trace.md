# Resource Trace

Backend and DB memory usage is stable across all samples. Backend fluctuates within ~5MiB, DB within ~1.3MiB. No abnormal spikes or leaks detected. Both processes operate well below total RAM capacity.

```
backend:
  Min: 344.9MiB
  Max: 350.1MiB
  Avg: 347.8MiB
  Total RAM: 15.32GiB

db:
  Min: 49.94MiB
  Max: 51.2MiB
  Avg: 50.7MiB
  Total RAM: 15.32GiB

Trace (backend/db per sample):
  1: backend: 344.9MiB | db: 49.94MiB
  2: backend: 346.3MiB | db: 51MiB
  3: backend: 345.7MiB | db: 50.68MiB
  ...
  59: backend: 349.6MiB | db: 50.5MiB
  60: backend: 349.6MiB | db: 50.5MiB
```
